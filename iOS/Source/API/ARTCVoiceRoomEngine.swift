//
//  ARTCVoiceRoomEngine.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/6.
//

import UIKit
import AUIRoomCore
import AUIMessage

@objcMembers open class ARTCVoiceRoomEngine: NSObject {

    public init(_ roomInfo: AUIVoiceRoomInfo) {
        self.roomInfo = roomInfo
        self.roomInfo.anchorSeatInfo.isMe = self.roomInfo.anchor.userId == AUIRoomService.currrentUser!.userId
        self.roomInfo.seatInfoList.forEach { info in
            if info.user?.userId == AUIRoomService.currrentUser!.userId {
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
    
    public let roomInfo: AUIVoiceRoomInfo
    
    private lazy var rtcService: AUIRoomRTCService = {
        let service = AUIRoomRTCService(mode: .VoiceRoom, user: self.me)
        return service
    }()
    
    internal lazy var observerArray: NSHashTable<ARTCVoiceRoomEngineDelegate> = {
        return NSHashTable<ARTCVoiceRoomEngineDelegate>.weakObjects()
    }()
    
    public var anchor: AUIRoomUser {
        return self.roomInfo.anchor
    }
    
    public var me: AUIRoomUser {
        return AUIRoomService.currrentUser!
    }
    
    public var isAnchor: Bool {
        return self.anchor.userId == self.me.userId
    }
    
    public var isJoinRoom: Bool {
        return self.rtcService.isJoin
    }
    
    public private(set) var isTextChatValid: Bool = false
    
    private var currentSpeakerId: String? = nil
    
    private var remoteUsersPublishStatus = [String: Bool]()
    private var remoteUsersMicrophoneStatus = [String: Bool]()
    
    private static var leavingList = [ARTCVoiceRoomEngine]()
    
    public func joinRoom(completed: AUIRoomCompleted?) {
        if self.isJoinRoom {
            completed?(AUIRoomError.createError(.Common, "already in the room, please leave the room first"))
            return
        }
        AUIRoomService.getInterface().fetchRTCAuthToken(room: self.roomInfo, user: self.me) { token, timestamp, error in
            
            if token != nil {
                let config = AUIRoomConfig()
                config.roomId = self.roomInfo.roomId
                config.timestamp = timestamp
                config.token = token!
                self.rtcService.join(config: config) {[weak self] error in
                    if error == nil {
                        AUIRoomService.currentRoom = self?.roomInfo
                        if let micSeatInfo = self?.micSeatInfo {
                            self?.startPublishing()
                            micSeatInfo.isPublishStream = true
                        }
                    }
                    completed?(error)
                }
            }
            else {
                completed?(AUIRoomError.createError(.Common, ""))
            }
        }
        
        // 这里加入im group
        let req = AUIMessageJoinGroupRequest()
        req.groupId = self.roomInfo.chatId
        AUIRoomService.getMessageService().joinGroup(req) {[weak self] error in
            if let self = self {
                self.isTextChatValid = error == nil
                
                self.roomInfo.onlineCount = self.roomInfo.onlineCount + 1
                self.notifyOnOnlineCountChanged(count: self.roomInfo.onlineCount)
            }
        }
        AUIRoomService.getMessageService().getListenerObserver().addListener(self)
    }
    
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
            strongSelf.rtcService.leave { error in
                AUIRoomService.currentRoom = nil
                self?.remoteUsersPublishStatus.removeAll()
                self?.remoteUsersMicrophoneStatus.removeAll()
                ARTCVoiceRoomEngine.leavingList.removeAll { controller in
                    return controller == self
                }
                completed?()
            }
            
            // 这里离开im group
            let req = AUIMessageLeaveGroupRequest()
            req.groupId = strongSelf.roomInfo.chatId
            AUIRoomService.getMessageService().leaveGroup(req) { error in
                if let self = self {
                    self.isTextChatValid = false

                    self.roomInfo.onlineCount = max(self.roomInfo.onlineCount - 1, 1)
                    self.notifyOnOnlineCountChanged(count: self.roomInfo.onlineCount)
                }
            }
            AUIRoomService.getMessageService().getListenerObserver().removeListener(strongSelf)
        }
        
    }
    
    public func dismissRoom(completed: AUIRoomCompleted?) {
        if self.isAnchor == false {
            completed?(AUIRoomError.createError(.Common, "仅限房主操作"))
            return
        }
        
        // 1. 发送消息给服务端，进行解散
        AUIRoomService.getInterface().dismissRoom(room: self.roomInfo, user:self.me) { error in
            if error != nil {
                completed?(error)
                return
            }
            
            if self.isJoinRoom {
                // 2. 广播消息
                self.sendCommand(data: nil, type: AUIRoomMessageType.DismissRoom.rawValue) { [weak self] error in
                    
                    // 3. leave room
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
    
    public var micSeatInfo: AUIVoiceRoomMicSeatInfo? {
        if self.isAnchor {
            return self.roomInfo.anchorSeatInfo
        }
        return self.roomInfo.seatInfoList.first { info in
            return info.isMe
        }
    }
    
    public var isJoinMic: Bool {
        return self.micSeatInfo != nil
    }
    
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
    
    public func joinMic(seatIndex: Int32, completed: AUIRoomCompleted?) {
        guard self.isJoinRoom else {
            completed?(AUIRoomError.createError(.Common, "please join room first"))
            return
        }
        
        if self.isJoinMic {
            completed?(AUIRoomError.createError(.Common, "already joined mic"))
            return
        }
        
        let micSeatInfo = self.roomInfo.getMicSeatInfo(index: seatIndex)
        if let micSeatInfo = micSeatInfo {
            if micSeatInfo.isJoin {
                completed?(AUIRoomError.createError(.Common, "seat index already joined by other user"))
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
                self.sendCommand(data: data, type: AUIRoomMessageType.JoinedMic.rawValue) {[weak self] error in
                    if error == nil {
                        self?.startPublishing()
                        self?.notifyOnJoinedMic(seatIndex:seatIndex, user: micSeatInfo.user!)
                    }
                    completed?(error)
                }
            }
        }
        else {
            completed?(AUIRoomError.createError(.Common, "input error 'seatIndex'"))
        }
    }
    
    public func leaveMic(completed: AUIRoomCompleted?) {
        guard self.isJoinRoom else {
            completed?(AUIRoomError.createError(.Common, "please join room first"))
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
        
        AUIRoomService.getInterface().leaveMic(room: self.roomInfo, user: self.me, index: micSeatInfo.index) { micDataList, error in
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
            self.sendCommand(data: data, type: AUIRoomMessageType.LeavedMic.rawValue)
            self.notifyOnLeavedMic(seatIndex: micSeatInfo.index, user: self.me)
            completed?(nil)
        }
    }
    
    public func requestMic(completed: AUIRoomCompleted?) {
        guard self.isJoinRoom else {
            completed?(AUIRoomError.createError(.Common, "please join room first"))
            return
        }
        
        if self.isJoinMic {
            completed?(AUIRoomError.createError(.Common, "already joined mic"))
            return
        }

        AUIRoomService.getInterface().requestMic(room: self.roomInfo, user: self.me) { micDataList, error in
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
    
    public func switchMicrophone(off: Bool, completed: AUIRoomCompleted? = nil) {
        if !self.isPublishing {
            completed?(AUIRoomError.createError(.Common, "please join mic first"))
            return
        }
        self.rtcService.switchMicrophone(off: off, completed: completed)
    }
    
    public func switchAudioOutput(type: AUIRoomAudioOutputType, completed:  AUIRoomCompleted? = nil) {
        self.rtcService.switchAudioOutput(type: type, completed: completed)
    }
    
    public func getAudioOutputType() -> AUIRoomAudioOutputType {
        return self.rtcService.getAudioOutputType()
    }
}

// 房间管理
extension ARTCVoiceRoomEngine {
    
    // 创建房间
    public static func createRoom(roomName: String, completed: @escaping (_ roomInfo: AUIVoiceRoomInfo?, _ error: NSError?)->Void) {
        
        guard let currentUser = AUIRoomService.currrentUser else {
            completed(nil, AUIRoomError.createError(.Common, "请先设置登录用户"))
            return
        }
        
        AUIRoomService.getInterface().createRoom(roomId: nil, roomName: roomName, user: currentUser) { roomData, error in
            let roomInfo = AUIVoiceRoomInfo(data: roomData)
            completed(roomInfo, error)
        }
        
    }
    
    // 获取房间列表
    public static func getRoomList(pageNum: Int, pageSize: Int, completed: @escaping (_ roomInfoList: [AUIVoiceRoomInfo], _ error: NSError?)->Void) {
        
        AUIRoomService.getInterface().getRoomList(user: AUIRoomService.currrentUser ?? AUIRoomUser(""), pageNum: pageNum, pageSize: pageSize) { roomDataList, error in
            var array = [AUIVoiceRoomInfo]()
            roomDataList?.forEach({ roomData in
                let roomInfo = AUIVoiceRoomInfo(data: roomData)
                if let roomInfo = roomInfo {
                    array.append(roomInfo)
                }
            })
            completed(array, error)
        }
    }
    
    // 获取房间详情
    public static func getRoomDetail(roomId: String, completed: @escaping (_ roomInfo: AUIVoiceRoomInfo?, _ error: NSError?)->Void) {
        
        AUIRoomService.getInterface().getRoomDetail(roomId: roomId, user: AUIRoomService.currrentUser ?? AUIRoomUser("")) { roomData, error in
            let roomInfo = AUIVoiceRoomInfo(data: roomData)
            completed(roomInfo, error)
        }
        
    }
}


// 信令&评论
extension ARTCVoiceRoomEngine {
    
    // 发送评论
    public func sendTextMessage(text: String, completed: ((_ error: NSError?)->Void)? = nil) {
        self.sendMessageToRoom(type: AUIRoomMessageType.TextMessage.rawValue, data: ["content": text], isCommand: false, completed: completed)
    }
    
    // 发送信令，userId为空时，在房间内群发
    public func sendCommand(data:[AnyHashable: Any]?, type: Int, userId: String? = nil, completed: ((_ error: NSError?)->Void)? = nil) {
        
        if let userId = userId {
            self.sendMessageToUser(userId: userId, type: type, data: data, isCommand: true, completed: completed)
        }
        else {
            self.sendMessageToRoom(type: type, data: data, isCommand: true, completed: completed)
        }
    }
    
    // 在当前的chatId里单点发送，调用前需确保入会
    func sendMessageToRoom(type: Int, data: [AnyHashable: Any]?, isCommand: Bool, completed: ((_ error: NSError?)->Void)?) {
        
        // 房间内群发
        let req = AUIMessageSendMessageToGroupRequest()
        req.groupId = self.roomInfo.chatId
        req.msgType = type
        req.skipAudit = isCommand
        req.skipMuteCheck = isCommand
        req.storage = !isCommand
        if data != nil {
            req.data = AUIMessageDefaultData(data: data!)
        }
        AUIRoomService.getMessageService().sendMessage(toGroup: req) { rsp, err in
            completed?(err as NSError?)
        }
    }
    
    // 1v1发送，需确保已经登录
    func sendMessageToUser(userId: String, type: Int, data:[AnyHashable: Any]?, isCommand: Bool, completed: ((_ error: NSError?)->Void)?) {
        
        // 单发
        let req = AUIMessageSendMessageToGroupUserRequest()
        req.groupId = self.roomInfo.chatId
        req.msgType = type
        req.receiverId = userId
        req.skipAudit = isCommand
        if data != nil {
            req.data = AUIMessageDefaultData(data: data!)
        }
        AUIRoomService.getMessageService().sendMessage(toGroupUser: req) { rsp, err in
            completed?(err as NSError?)
        }
    }
}

extension ARTCVoiceRoomEngine: AUIMessageListenerProtocol {
    
    public func onMessageReceived(_ model: AUIMessageModel) {
        if let groupId = model.groupId {
            if (!groupId.isEmpty && groupId != self.roomInfo.chatId) {
                return
            }
        }
        
        let data:[AnyHashable : Any] = model.data != nil ? model.data! : [:]
        debugPrint("AUIVoiceRoomController onMessageReceived:\(data.room_jsonString)")

        let sender = model.sender != nil ?  AUIRoomUser(user: model.sender!) : AUIRoomUser("")

        let msgType = AUIRoomMessageType(rawValue: model.msgType)
        if let msgType = msgType {
            switch msgType {
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
                            let joinedUser = AUIRoomUser(data["userId"] as? String ?? "")
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
    
    public func onJoinGroup(_ model: AUIMessageModel) {
        guard model.groupId == self.roomInfo.chatId else { return }
        debugPrint("AUIVoiceRoomController onJoinGroup:\(model.sender?.userId ?? "unknow")")
        
        if let sender = model.sender {
            self.notifyOnJoinedRoom(user: AUIRoomUser(user: sender))
        }
        self.roomInfo.onlineCount = self.roomInfo.onlineCount + 1
        self.notifyOnOnlineCountChanged(count: self.roomInfo.onlineCount)
    }
    
    public func onLeaveGroup(_ model: AUIMessageModel) {
        guard model.groupId == self.roomInfo.chatId else { return }
        debugPrint("AUIVoiceRoomController onLeaveGroup:\(model.sender?.userId ?? "unknow")")
        
        if let sender = model.sender {
            self.notifyOnLeavedRoom(user: AUIRoomUser(user: sender))
        }
        self.roomInfo.onlineCount = max(self.roomInfo.onlineCount - 1, 1)
        self.notifyOnOnlineCountChanged(count: self.roomInfo.onlineCount)
    }
    
    public func onExitedGroup(_ groupId: String) {
        guard groupId == self.roomInfo.chatId else { return }
        debugPrint("AUIVoiceRoomController onExitedGroup:\(groupId)")
        
        self.notifyOnKickoutRoom()
    }
}

extension ARTCVoiceRoomEngine: AUIRoomRTCServiceDelegate {
    
    public func onJoined(userId: String) {
        debugPrint("AUIVoiceRoomController onJoined userId:\(userId)")
    }
    
    public func onLeaved(userId: String) {
        debugPrint("AUIVoiceRoomController onLeaved userId:\(userId)")
        if let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: userId) {
            micSeatInfo.isPublishStream = false
            self.notifyOnMicUserStreamChanged(seatIndex: micSeatInfo.index, user: AUIRoomUser(userId), publishing: false)
        }
        self.remoteUsersPublishStatus.removeValue(forKey: userId)
        self.remoteUsersMicrophoneStatus.removeValue(forKey: userId)
    }
    
    public func onStartedPublish(userId: String) {
        debugPrint("AUIVoiceRoomController onStartedPublish userId:\(userId)")
        if let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: userId) {
            micSeatInfo.isPublishStream = true
            self.notifyOnMicUserStreamChanged(seatIndex: micSeatInfo.index, user: AUIRoomUser(userId), publishing: true)
        }
        self.remoteUsersPublishStatus[userId] = true
    }
    
    public func onStopedPublish(userId: String) {
        debugPrint("AUIVoiceRoomController onStopedPublish userId:\(userId)")
        if let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: userId) {
            micSeatInfo.isPublishStream = false
            self.notifyOnMicUserStreamChanged(seatIndex: micSeatInfo.index, user: AUIRoomUser(userId), publishing: false)
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
            micSeatInfo.networkStatus = AUIRoomNetworkState(rawValue: downloadState) ?? .Unknow
        }
        self.notifyOnNetworkStateChanged(user: AUIRoomUser(userId), state: downloadState)
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
        debugPrint("AUIVoiceRoomController onAudioVolumeChanged:\(currentSpeakerId) ")
        self.notifyOnMicUserSpeakStateChanged(seatIndex: micSeatInfo.index, isSpeaking: false)
    }
    
    public func onJoinTokenWillExpire() {
        debugPrint("AUIVoiceRoomController onJoinTokenWillExpire")
    }
    
    public func onError(_ error: Error) {
        debugPrint("AUIVoiceRoomController onError:\(error)")
    }
}
