//
//  ARTCVoiceRoomEngine.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/6.
//

import UIKit

@objcMembers open class ARTCVoiceRoomAudioPlayingStatus: NSObject {
    
    public fileprivate(set) var audioId: Int32 = 0
    public fileprivate(set) var onlyLocalPlay: Bool = false
    public fileprivate(set) var volume: Int32 = 50
    public fileprivate(set) var playState: ARTCRoomMusicState = .None
    public fileprivate(set) var filePath: String = ""
}


@objcMembers open class ARTCVoiceRoomEngine: NSObject {

    // 初始化
    public init(_ roomInfo: ARTCVoiceRoomInfo) {
        self.roomInfo = roomInfo
        self.roomInfo.anchorSeatInfo.isMe = self.roomInfo.anchor.userId == ARTCRoomService.currrentUser!.userId
        self.roomInfo.seatInfoList.forEach { info in
            if info.user?.userId == ARTCRoomService.currrentUser!.userId {
                info.isMe = true
            }
        }
        super.init()
        
        self.rtcService.addObserver(delegate: self)
    }
    
    deinit {
        debugPrint("deinit: \(self)")
        self.rtcService.destroy()
    }
    
    // 房间model
    public let roomInfo: ARTCVoiceRoomInfo
    
    // RoomService
    public var roomService: ARTCRoomServiceInterface? = nil {
        didSet {
            self.roomService?.receivedMessageDelegate = self
        }
    }
    
    // 添加监听
    public func addObserver(delegate: ARTCVoiceRoomEngineDelegate) {
        if Thread.isMainThread {
            if !self.observerArray.contains(delegate) {
                self.observerArray.add(delegate)
            }
        }
        else {
            DispatchQueue.main.async {
                self.addObserver(delegate: delegate)
            }
        }
    }
    
    // 移除监听
    public func removeObserver(delegate: ARTCVoiceRoomEngineDelegate) {
        if Thread.isMainThread {
            self.observerArray.remove(delegate)
        }
        else {
            DispatchQueue.main.async {
                self.removeObserver(delegate: delegate)
            }
        }
    }
    
    // 房主model
    public var anchor: ARTCRoomUser {
        return self.roomInfo.anchor
    }
    
    // 自己model
    public var me: ARTCRoomUser {
        return ARTCRoomService.currrentUser!
    }
    
    // 是否房主
    public var isAnchor: Bool {
        return self.anchor.userId == self.me.userId
    }
    
    // 是否在语聊房里
    public private(set) var isJoinRoom: Bool = false
        
    // 进入语聊房
    public func joinRoom(completed: ARTCRoomCompleted?) {
        if self.isJoinRoom {
            completed?(ARTCRoomError.createError(.Common, "already in the room, please leave the room first"))
            return
        }
        
        let group = DispatchGroup()
        var error1: NSError? = nil
        var error2: NSError? = nil
        
        group.enter()
        // 这里加入im group
        self.sendCommand(type: .JoinRoom) { error in
            error1 = error
            group.leave()
        }
        
        group.enter()
        self.getRoomServiceInterface().fetchRTCAuthToken(room: self.roomInfo, user: self.me) { [weak self] token, timestamp, error in
            
            guard let strongSelf = self else {
                group.leave()
                return
            }
            
            if token != nil {
                let config = ARTCRoomConfig()
                config.roomId = strongSelf.roomInfo.roomId
                config.timestamp = timestamp
                config.token = token!
                strongSelf.rtcService.join(config: config) { error in
                    error2 = error
                    group.leave()
                }
            }
            else {
                error2 = error ?? ARTCRoomError.createError(.Common, "")
                group.leave()
            }
        }
        
        group.notify(queue: .main) {
            if error1 == nil && error2 == nil {
                debugPrint("joinRoom result: success")
                ARTCRoomService.currentRoom = self.roomInfo
                
                self.isJoinRoom = true
                
                if let micSeatInfo = self.micSeatInfo {
                    self.startPublishing()
                    micSeatInfo.isPublishStream = true
                }
                completed?(nil)
            }
            else {
                var finalError: NSError? = nil
                if let error1 = error1, let error2 = error2 {
                    debugPrint("joinRoom with group result:\(error1)")
                    debugPrint("joinRoom with channel result:\(error2)")
                    finalError = ARTCRoomError.createError(.Common, "join group and join channel failed")
                }
                else if let error1 = error1 {
                    debugPrint("joinRoom with channel result:\(error1)")
                    finalError = error1
                }
                else if let error2 = error2 {
                    debugPrint("joinRoom with channel result:\(error2)")
                    finalError = error2
                }
                
                completed?(finalError)
            }
        }
    }
    
    // 离开语聊房
    public func leaveRoom(_ completed: (()->Void)? = nil) {
        if self.isJoinRoom == false {
            completed?()
            return
        }
        
        ARTCVoiceRoomEngine.leavingList.append(self)
        self.leaveMic { [weak self] error in
            guard let strongSelf = self else {
                return
            }
            
            let group = DispatchGroup()
            group.enter()
            strongSelf.rtcService.leave { error in
                group.leave()
            }
            
            group.enter()
            strongSelf.sendCommand(type: .LeaveRoom) { error in
                group.leave()
            }
            
            group.notify(queue: .main) {
                
                strongSelf.isJoinRoom = false
                strongSelf.roomInfo.onlineCount = max(strongSelf.roomInfo.onlineCount - 1, 1)
                strongSelf.notifyOnOnlineCountChanged(count: strongSelf.roomInfo.onlineCount)
                
                ARTCRoomService.currentRoom = nil
                strongSelf.remoteUsersPublishStatus.removeAll()
                strongSelf.remoteUsersMicrophoneStatus.removeAll()
                ARTCVoiceRoomEngine.leavingList.removeAll { controller in
                    return controller == strongSelf
                }
                completed?()
            }
        }
    }
    
    // 解散语聊房（仅限主播）
    public func dismissRoom(completed: ARTCRoomCompleted?) {
        if self.isAnchor == false {
            completed?(ARTCRoomError.createError(.Common, "仅限房主操作"))
            return
        }
        
        // 发送消息给服务端，进行解散
        self.getRoomServiceInterface().dismissRoom(room: self.roomInfo, user:self.me) { error in
            // 解散失败，返回错误
            if error != nil {
                completed?(error)
                return
            }
            
            // 解散成功，退出房间
            if self.isJoinRoom {
                // 广播消息
                self.sendCommand(type: ARTCRoomMessageType.DismissRoom) { [weak self] error in
                    
                    // leave room
                    self?.leaveRoom({
                        completed?(nil)
                    })
                }
            }
            else {
                completed?(nil)
            }
        }
    }
    
    // 麦位model
    public var micSeatInfo: AUIVoiceRoomMicSeatInfo? {
        if self.isAnchor {
            return self.roomInfo.anchorSeatInfo
        }
        return self.roomInfo.seatInfoList.first { info in
            return info.isMe
        }
    }
    
    // 是否已上麦
    public var isJoinMic: Bool {
        return self.micSeatInfo != nil
    }
    
    // 上麦
    public func joinMic(seatIndex: Int32, completed: ARTCRoomCompleted?) {
        guard self.isJoinRoom else {
            completed?(ARTCRoomError.createError(.Common, "please join room first"))
            return
        }
        
        if self.isJoinMic {
            completed?(ARTCRoomError.createError(.Common, "already joined mic"))
            return
        }
        
        let micSeatInfo = self.roomInfo.getMicSeatInfo(index: seatIndex)
        if let micSeatInfo = micSeatInfo {
            if micSeatInfo.isJoin {
                completed?(ARTCRoomError.createError(.Common, "seat index already joined by other user"))
            }
            else {
                let me = self.me
                micSeatInfo.user = me
                micSeatInfo.isPublishStream = true
                micSeatInfo.isMe = true
                let data: [String : Any] = [
                    "seatIndex" : seatIndex,
                    "userId" : me.userId,
                    "userNick" : me.userNick,
                    "userAvatar" : me.userAvatar,
                ]
                self.sendCommand(type: ARTCRoomMessageType.JoinedMic, data: data) {[weak self] error in
                    if error == nil {
                        self?.startPublishing()
                        self?.notifyOnJoinedMic(seatIndex:seatIndex, user: micSeatInfo.user!)
                    }
                    completed?(error)
                }
            }
        }
        else {
            completed?(ARTCRoomError.createError(.Common, "input error 'seatIndex'"))
        }
    }
    
    // 下麦
    public func leaveMic(completed: ARTCRoomCompleted?) {
        guard self.isJoinRoom else {
            completed?(ARTCRoomError.createError(.Common, "please join room first"))
            return
        }
        
        if self.isAnchor {
            self.stopPublishing()
            completed?(nil)
            return
        }
        
        guard let micSeatInfo = self.micSeatInfo else {
            completed?(nil)
            return
        }
        
        self.getRoomServiceInterface().leaveMic(room: self.roomInfo, user: self.me, index: micSeatInfo.index) { micDataList, error in
            if error != nil {
                completed?(error)
                return
            }
            
            self.stopPublishing()
            micSeatInfo.user = nil
            let data: [String : Any] = [
                "seatIndex" : micSeatInfo.index,
                "userId": self.me.userId,
            ]
            self.sendCommand(type: ARTCRoomMessageType.LeavedMic, data: data)
            self.notifyOnLeavedMic(seatIndex: micSeatInfo.index, user: self.me)
            completed?(nil)
        }
    }
    
    // 申请上麦
    public func requestMic(completed: ARTCRoomCompleted?) {
        guard self.isJoinRoom else {
            completed?(ARTCRoomError.createError(.Common, "please join room first"))
            return
        }
        
        if self.isJoinMic {
            completed?(ARTCRoomError.createError(.Common, "already joined mic"))
            return
        }

        self.getRoomServiceInterface().requestMic(room: self.roomInfo, user: self.me) { micDataList, error in
            if error != nil {
                completed?(error)
                return
            }
            
            if let micDataList = micDataList {
                for micData in micDataList {
                    if let micSeatInfo = AUIVoiceRoomMicSeatInfo(data: micData) {
                        if micSeatInfo.user?.userId == self.me.userId {
                            self.joinMic(seatIndex: micSeatInfo.index, completed: completed)
                            return
                        }
                    }
                }
            }
            
            completed?(nil)
        }
    }
    
    // 打开/关闭一个自己的麦克风
    public func switchMicrophone(off: Bool, completed: ARTCRoomCompleted? = nil) {
        if !self.isJoinMic {
            completed?(ARTCRoomError.createError(.Common, "please join mic first"))
            return
        }
        self.rtcService.switchMicrophone(off: off, completed: completed)
    }
    
    // 切换自己的音频输出（麦克风 or 听筒）
    public func switchAudioOutput(type: ARTCRoomAudioOutputType, completed:  ARTCRoomCompleted? = nil) {
        self.rtcService.switchAudioOutput(type: type, completed: completed)
    }
    
    public func switchEarBack(on: Bool, completed: ARTCRoomCompleted? = nil) {
        if !self.isJoinMic {
            completed?(ARTCRoomError.createError(.Common, "please join mic first"))
            return
        }
        self.rtcService.switchEarBack(on: on, completed: completed)
    }
    
    public func getIsEarBack() -> Bool {
        return self.rtcService.getIsEarBack()
    }
    
    // 设置音频采集音量，默认50，可调节[0~100]
    public func setRecordingVolume(volume: Int32, completed: ARTCRoomCompleted? = nil) {
        self.rtcService.setRecordingVolume(volume: volume, completed: completed)
    }
    
    // 获取音频采集音量，默认50
    public func getRecordingVolume() -> Int32 {
        return self.rtcService.getRecordingVolume()
    }

    public func startPlayAudioEffect(effectId: Int32, localPath: String, volume: Int32, onlyLocalPlay: Bool, completed:  ARTCRoomCompleted? = nil) {
        if self.isJoinMic == false {
            completed?(ARTCRoomError.createError(.Common, "please join mic first"))
            return
        }
        self.rtcService.startPlayAudioEffect(effectId: effectId, localPath: localPath, volume: volume, onlyLocalPlay: onlyLocalPlay, completed: completed)
    }
    
    public func stopPlayAudioEffect(effectId: Int32, completed: ARTCRoomCompleted? = nil) {
        if self.isJoinMic == false {
            completed?(ARTCRoomError.createError(.Common, "please join mic first"))
            return
        }
        self.rtcService.stopPlayAudioEffect(effectId: effectId, completed: completed)
    }
    
    // 设置音效音量
    public func setAudioEffectVolume(effectId: Int32, volume: Int32, completed: ARTCRoomCompleted? = nil) {
        if self.isJoinMic == false {
            completed?(ARTCRoomError.createError(.Common, "please join mic first"))
            return
        }
        self.rtcService.setAudioEffectVolume(effectId: effectId, volume: volume, completed: completed)
    }
    
    public func startPlayBackgroundMusic(musicId: Int32, localPath: String, volume: Int32, onlyLocalPlay: Bool, completed:  ARTCRoomCompleted? = nil) {
        if self.isAnchor == false {
            completed?(ARTCRoomError.createError(.Common, "仅限房主操作"))
            return
        }
        self.rtcService.startPlayMusic(localPath: localPath, volume: volume, onlyLocalPlay: onlyLocalPlay, completed: completed)
        self.bgMusicStatus.audioId = musicId
        self.bgMusicStatus.volume = volume
        self.bgMusicStatus.onlyLocalPlay = onlyLocalPlay
        self.bgMusicStatus.filePath = localPath
    }
    
    public func stopPlayBackgroundMusic(completed:  ARTCRoomCompleted? = nil) {
        if self.isAnchor == false {
            completed?(ARTCRoomError.createError(.Common, "仅限房主操作"))
            return
        }
        self.rtcService.stopPlayMusic(completed: completed)
    }
    
    // 设置播放音乐合成音量
    public func setBackgroundMusicVolume(volume: Int32, completed: ARTCRoomCompleted? = nil) {
        if self.isAnchor == false {
            completed?(ARTCRoomError.createError(.Common, "仅限房主操作"))
            return
        }
        self.rtcService.setMusicPlayingVolume(volume: volume, completed: completed)
        self.bgMusicStatus.volume = volume
    }
    
    // 获取背景音乐播放状态
    public lazy var bgMusicStatus: ARTCVoiceRoomAudioPlayingStatus = {
        let status = ARTCVoiceRoomAudioPlayingStatus()
        return status
    }()
    
    // 设置变声类型
    public func setVoiceChangerMode(mode: ARTCRoomVoiceChangerMode, completed: ARTCRoomCompleted? = nil) {
        if self.isJoinMic == false {
            completed?(ARTCRoomError.createError(.Common, "please join mic first"))
            return
        }
        self.rtcService.setVoiceChangerMode(mode: mode, completed: completed)
    }
    
    // 获取当前变声类型
    public func getVoiceChangerMode() -> ARTCRoomVoiceChangerMode {
        return self.rtcService.getVoiceChangerMode()
    }
    
    // 设置混响类型
    public func setVoiceReverbMode(mode: ARTCRoomVoiceReverbMode, completed: ARTCRoomCompleted? = nil) {
        if self.isJoinMic == false {
            completed?(ARTCRoomError.createError(.Common, "please join mic first"))
            return
        }
        self.rtcService.setVoiceReverbMode(mode: mode, completed: completed)
    }
    
    // 获取混响类型
    public func getVoiceReverbMode() -> ARTCRoomVoiceReverbMode {
        return self.rtcService.getVoiceReverbMode()
    }
    
    // 发送评论
    public func sendTextMessage(text: String, completed: ARTCRoomCompleted? = nil) {
        // 房间内群发
        let model = ARTCRoomMessageSendModel()
        model.chatId = self.roomInfo.chatId
        model.type = ARTCRoomMessageType.TextMessage
        model.isCommand = false
        model.data = ["content": text]
        self.getRoomServiceInterface().sendMessage(model: model, completed: completed)
    }
    
    // 发送信令，userId为空时，在房间内群发
    public func sendCommand(type: ARTCRoomMessageType, data:[AnyHashable: Any]? = nil , userId: String? = nil, completed: ARTCRoomCompleted? = nil) {
        
        if let userId = userId {
            // 单发
            let model = ARTCRoomMessageSendModel()
            model.chatId = self.roomInfo.chatId
            model.type = type
            model.receivedUserId = userId
            model.isCommand = true
            model.data = data
            self.getRoomServiceInterface().sendMessage(model: model, completed: completed)
        }
        else {
            // 房间内群发
            let model = ARTCRoomMessageSendModel()
            model.chatId = self.roomInfo.chatId
            model.type = type
            model.isCommand = true
            model.data = data
            self.getRoomServiceInterface().sendMessage(model: model, completed: completed)
        }
    }
    
    //====================内部定义的属性和使用的方法=======================================
    
    internal lazy var observerArray: NSHashTable<ARTCVoiceRoomEngineDelegate> = {
        return NSHashTable<ARTCVoiceRoomEngineDelegate>.weakObjects()
    }()
    
    internal func getRoomServiceInterface() -> ARTCRoomServiceInterface {
        return self.roomService!
    }

    internal lazy var rtcService: ARTCRoomRTCService = {
        let service = ARTCRoomRTCService(mode: .VoiceRoom, user: self.me)
        return service
    }()
    
    private var currentSpeakerId: String? = nil

    private var remoteUsersPublishStatus = [String: Bool]()
    private var remoteUsersMicrophoneStatus = [String: Bool]()
    private static var leavingList = [ARTCVoiceRoomEngine]()
        
    private var isPublishing: Bool {
        return self.rtcService.isPublishing
    }
    
    private func startPublishing() {
        if self.isPublishing {
            return
        }
        
        self.rtcService.startPublish(completed: nil)
    }
    
    private func stopPublishing() {
        guard self.isPublishing else {
            return
        }
        
        self.rtcService.stopPublish(completed: nil)
    }

}

extension ARTCVoiceRoomEngine: ARTCRoomMessageDelegate {
    
    public func onReceivedMessage(model: ARTCRoomMessageReceiveModel) {
        if let chatId = model.chatId {
            if (!chatId.isEmpty && chatId != self.roomInfo.chatId) {
                return
            }
        }
        
        if model.type == .JoinRoom {
            if let sender = model.sender {
                self.notifyOnJoinedRoom(user: sender)
            }
            return
        }
        
        if model.type == .LeaveRoom {
            if let sender = model.sender {
                self.notifyOnLeavedRoom(user:sender)
            }
            return
        }
        
        if model.type == .KickoutRoom {
            self.notifyOnKickoutRoom()
            return
        }
        
        if model.type == .OnlineCountChanged {
            if let onlineCount = model.data?["onlineCount"] as? Int {
                self.roomInfo.onlineCount = onlineCount
                self.notifyOnOnlineCountChanged(count: onlineCount)
            }
            return
        }
        
        let sender = model.sender ?? ARTCRoomUser("")
        let data:[AnyHashable : Any] = model.data != nil ? model.data! : [:]
        debugPrint("AUIVoiceRoomController onMessageReceived:\(data.artcJsonString)")

        switch model.type {
        case .TextMessage:
            let text = data["content"] as? String
            if let text = text {
                self.notifyOnReceivedTextMessage(user: sender, text: text)
            }
            break
        case .JoinedMic:
            if sender.userId != self.me.userId {
                if let seatIndex = data["seatIndex"] as? Int32 {
                    if let micSeatInfo = self.roomInfo.getMicSeatInfo(index: seatIndex) {
                        let joinedUser = ARTCRoomUser(data["userId"] as? String ?? "")
                        joinedUser.userNick = data["userNick"] as? String ?? ""
                        joinedUser.userAvatar = data["userAvatar"] as? String ?? ""
                        micSeatInfo.user = joinedUser
                        self.notifyOnJoinedMic(seatIndex: seatIndex, user: joinedUser)
                        if self.remoteUsersPublishStatus[joinedUser.userId] == true {
                            micSeatInfo.isPublishStream = true
                            self.notifyOnMicUserStreamChanged(seatIndex: micSeatInfo.index, user: micSeatInfo.user!, publishing: true)
                        }
                        if self.remoteUsersMicrophoneStatus[joinedUser.userId] == true {
                            micSeatInfo.isMuteMic = true
                            self.notifyOnMicUserMicrophoneChanged(seatIndex: micSeatInfo.index, user: micSeatInfo.user! , off: true)
                        }
                    }
                }
            }
            break
        case .LeavedMic:
            if sender.userId != self.me.userId {
                if let seatIndex = data["seatIndex"] as? Int32 {
                    if let micSeatInfo = self.roomInfo.getMicSeatInfo(index: seatIndex) {
                        if let leavedUser = micSeatInfo.user {
                            micSeatInfo.user = nil
                            self.notifyOnLeavedMic(seatIndex: seatIndex, user: leavedUser)
                        }
                    }
                }
            }
            break
        case .DismissRoom:
            if sender.userId != self.me.userId {
                self.notifyOnDismissedRoom()
            }
            break
        default: break
            
        }
    }
}

extension ARTCVoiceRoomEngine: ARTCRoomRTCServiceDelegate {
    
    public func onJoined(userId: String) {
        debugPrint("AUIVoiceRoomController onJoined userId:\(userId)")
    }
    
    public func onLeaved(userId: String) {
        debugPrint("AUIVoiceRoomController onLeaved userId:\(userId)")
        if let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: userId) {
            micSeatInfo.isPublishStream = false
            self.notifyOnMicUserStreamChanged(seatIndex: micSeatInfo.index, user: ARTCRoomUser(userId), publishing: false)
        }
        self.remoteUsersPublishStatus.removeValue(forKey: userId)
        self.remoteUsersMicrophoneStatus.removeValue(forKey: userId)
    }
    
    public func onStartedPublish(userId: String) {
        debugPrint("AUIVoiceRoomController onStartedPublish userId:\(userId)")
        if let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: userId) {
            micSeatInfo.isPublishStream = true
            self.notifyOnMicUserStreamChanged(seatIndex: micSeatInfo.index, user: ARTCRoomUser(userId), publishing: true)
        }
        self.remoteUsersPublishStatus[userId] = true
    }
    
    public func onStopedPublish(userId: String) {
        debugPrint("AUIVoiceRoomController onStopedPublish userId:\(userId)")
        if let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: userId) {
            micSeatInfo.isPublishStream = false
            self.notifyOnMicUserStreamChanged(seatIndex: micSeatInfo.index, user: ARTCRoomUser(userId), publishing: false)
        }
        self.remoteUsersPublishStatus.removeValue(forKey: userId)
    }
    
    public func onMicrophoneStateChanged(userId: String, off: Bool) {
        debugPrint("AUIVoiceRoomController onMicrophoneStateChanged off:\(off) userId:\(userId)")
        if let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: userId) {
            micSeatInfo.isMuteMic = off
            self.notifyOnMicUserMicrophoneChanged(seatIndex: micSeatInfo.index, user: micSeatInfo.user! , off: off)
        }
        self.remoteUsersMicrophoneStatus[userId] = off
    }
    
    public func onNetworkStateChanged(userId: String, uploadState: String, downloadState: String) {
        debugPrint("AUIVoiceRoomController onNetworkStateChanged userId:\(userId) uploadState:\(uploadState) downloadState:\(downloadState)")
        if let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: userId) {
            micSeatInfo.networkStatus = ARTCRoomNetworkState(rawValue: uploadState) ?? .Unknow
        }
        self.notifyOnNetworkStateChanged(user: ARTCRoomUser(userId), state: uploadState)
    }
    
    public func onSpeakerActived(userId: String) {
        if userId == "0" {
            self.currentSpeakerId = self.me.userId
        }
        else {
            self.currentSpeakerId = userId
        }
    }
    
    public func onAudioVolumeChanged(data: [String : Any]) {
        guard let currentSpeakerId = self.currentSpeakerId else {
            return
        }
        guard let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: currentSpeakerId) else {
            return
        }
        var key = currentSpeakerId
        if currentSpeakerId == self.me.userId {
            key = "0"
        }
        if let volume = data[key] as? Int32 {
            if volume > 10 {
                debugPrint("AUIVoiceRoomController onAudioVolumeChanged:\(currentSpeakerId) speaking")
                self.notifyOnMicUserSpeakStateChanged(seatIndex: micSeatInfo.index, isSpeaking: true)
                return
            }
        }
        debugPrint("AUIVoiceRoomController onAudioVolumeChanged:\(currentSpeakerId)")
        self.notifyOnMicUserSpeakStateChanged(seatIndex: micSeatInfo.index, isSpeaking: false)
    }
    
    public func onMusicPlayStateChanged(state: ARTCRoomMusicState) {
        debugPrint("AUIVoiceRoomController onMusicPlayStateChanged:\(state.rawValue)")
        DispatchQueue.main.async {
            self.bgMusicStatus.playState = state
            self.notifyOnBackgroundMusicStatusChanged(status: self.bgMusicStatus)
        }
    }
    
    public func onAudioEffectPlayCompleted(effectId: Int32) {
        debugPrint("AUIVoiceRoomController onAudioEffectPlayCompleted:\(effectId)")
        self.notifyOnAudioEffectPlayCompleted(effectId: effectId)
    }
    
    public func onJoinTokenWillExpire() {
        debugPrint("AUIVoiceRoomController onJoinTokenWillExpire")
    }
    
    public func onError(_ error: Error) {
        debugPrint("AUIVoiceRoomController onError:\(error)")
    }
}
