//
//  AUIRoomRTCService.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit

#if canImport(AliVCSDK_ARTC)
import AliVCSDK_ARTC
#elseif canImport(AliVCSDK_InteractiveLive)
import AliVCSDK_InteractiveLive
#elseif canImport(AliVCSDK_Standard)
import AliVCSDK_Standard
#endif

@objc public protocol AUIRoomRTCServiceDelegate {
    
    @objc optional func onJoined(userId: String)
    @objc optional func onLeaved(userId: String)

    @objc optional func onStartedPublish(userId: String)
    @objc optional func onStopedPublish(userId: String)

    @objc optional func onMicrophoneStateChanged(userId: String, off: Bool)
    
    @objc optional func onNetworkStateChanged(userId: String, uploadState: String, downloadState: String)

    
    @objc optional func onAudioVolumeChanged(data: [String: Any])
    @objc optional func onSpeakerActived(userId: String)
    
    @objc optional func onError(_ error: Error)
    @objc optional func onJoinTokenWillExpire()
}

@objcMembers open class AUIRoomRTCService: NSObject {
    
    public init(mode: AUIRoomSceneType, user: AUIRoomUser) {
        self.mode = mode
        self.user = user
        super.init()
    }
    
    public func destroy() {
        self.leave {[weak self] error in
            self?.destroyCommunicatingEngine()
        }
    }
    
    public let mode: AUIRoomSceneType
    public let user: AUIRoomUser
    
    public private(set) var roomConfig: AUIRoomConfig?
    
    internal lazy var observerArray: NSHashTable<AUIRoomRTCServiceDelegate> = {
        return NSHashTable<AUIRoomRTCServiceDelegate>.weakObjects()
    }()
    
    private var communicatingEngine: AliRtcEngine? = nil
    public func getRTCEngine() -> AnyObject? {
        return self.communicatingEngine
    }
    
    private func setupCommunicatingEngine() {
        if self.communicatingEngine != nil {
            return
        }
        
        let extras: [String: String] = [:]
        let engine = AliRtcEngine.sharedInstance(self, extras:extras.room_jsonString)
        
        // 设置日志级别
        engine.setLogLevel(.info)
        
        // 设置频道模式
        engine.setChannelProfile(AliRtcChannelProfile.interactivelive)
        // 设置角色
        engine.setClientRole(AliRtcClientRole.rolelive)
        
        // 音频配置
        engine.setAudioSessionOperationRestriction(AliRtcAudioSessionOperationRestriction.none)
        engine.enableAudioVolumeIndication(500, smooth: 3, reportVad: 1)
        engine.enableSpeakerphone(true)
        engine.setDefaultSubscribeAllRemoteAudioStreams(true)
        engine.subscribeAllRemoteAudioStreams(true)

        if self.mode == .VoiceRoom {
            engine.setAudioProfile(AliRtcAudioProfile.engineLowQualityMode, audio_scene: AliRtcAudioScenario.sceneMusicMode)
            engine.setAudioProfile(AliRtcAudioProfile.engineBasicQualityMode, audio_scene: AliRtcAudioScenario.sceneMusicMode)
            engine.setAudioProfile(AliRtcAudioProfile.engineHighQualityMode, audio_scene: AliRtcAudioScenario.sceneMusicMode)
            engine.setAudioProfile(AliRtcAudioProfile.engineStereoHighQualityMode, audio_scene: AliRtcAudioScenario.sceneMusicMode)
            engine.setAudioProfile(AliRtcAudioProfile.engineSuperHighQualityMode, audio_scene: AliRtcAudioScenario.sceneMusicMode)
            engine.setAudioProfile(AliRtcAudioProfile.engineStereoSuperHighQualityMode, audio_scene: AliRtcAudioScenario.sceneMusicMode)
            engine.setAudioOnlyMode(true)
        }
        else if self.mode == .KTV {
            engine.setAudioProfile(AliRtcAudioProfile.engineLowQualityMode, audio_scene: AliRtcAudioScenario.sceneKtvMode)
            engine.setAudioProfile(AliRtcAudioProfile.engineBasicQualityMode, audio_scene: AliRtcAudioScenario.sceneKtvMode)
            engine.setAudioProfile(AliRtcAudioProfile.engineHighQualityMode, audio_scene: AliRtcAudioScenario.sceneKtvMode)
            engine.setAudioProfile(AliRtcAudioProfile.engineStereoHighQualityMode, audio_scene: AliRtcAudioScenario.sceneKtvMode)
            engine.setAudioProfile(AliRtcAudioProfile.engineSuperHighQualityMode, audio_scene: AliRtcAudioScenario.sceneKtvMode)
            engine.setAudioProfile(AliRtcAudioProfile.engineStereoSuperHighQualityMode, audio_scene: AliRtcAudioScenario.sceneKtvMode)
            engine.setAudioOnlyMode(true)
        }
        else if self.mode == .Call {
            /* 暂不支持
            let config = AliRtcVideoEncoderConfiguration()
            config.dimensions = config.dimensions
            config.frameRate = config.frameRate
            config.bitrate = 512
            config.orientationMode = AliRtcVideoEncoderOrientationMode.fixedPortrait
            engine.setVideoEncoderConfiguration(config)
            
            engine.setDefaultSubscribeAllRemoteVideoStreams(true)
            engine.subscribeAllRemoteVideoStreams(true)
             */
        }
        
        // 推流配置
        engine.publishLocalVideoStream(false)
        engine.publishLocalAudioStream(false)
        
        self.communicatingEngine = engine
    }
    
    private func destroyCommunicatingEngine() {
        AliRtcEngine.destroy()
        self.communicatingEngine = nil
    }
    
    public func join(config: AUIRoomConfig, completed: AUIRoomCompleted? = nil) -> Void {
        
        self.setupCommunicatingEngine()
        
        var gslb = config.gslb
        if gslb == "" {
            gslb = "https://gw.rtn.aliyuncs.com"
        }
        
        let info = AliRtcAuthInfo()
        info.gslb = [gslb]
        info.channelId = config.roomId
        info.appId = config.appId
        info.token = config.token  // TODO: token过期时，joinChannel无任何回调
        info.timestamp = config.timestamp
        info.userId = self.user.userId
        self.communicatingEngine?.joinChannel(info, name: self.user.userNick, onResultWithUserId: { errCode, channel, userId, elapsed in
            var err : NSError? = nil
            if errCode != 0 {
                debugPrint("[error]AUIRoomRTCService joinChannel failed:\(errCode)")
                err = AUIRoomError.createError(code: errCode, message: "join channel failed")
            }
            else {
                self.roomConfig = config
            }
            completed?(err)
        })
    }
    
    public func leave(completed: AUIRoomCompleted? = nil) {
        self.stopPublish()
        
        self.communicatingEngine?.leaveChannel()
        self.roomConfig = nil
        completed?(nil)
    }
    
    public var isJoin: Bool {
        return self.roomConfig != nil
    }
    
    public private(set) var isPublishing: Bool = false

    
    public func startPublish(completed: AUIRoomCompleted? = nil) {
        self.communicatingEngine?.muteLocalMic(false, mode: .allAudioMode)
        self.communicatingEngine?.setClientRole(AliRtcClientRole.roleInteractive)
        self.communicatingEngine?.publishLocalAudioStream(true)
        self.isPublishing = true
        completed?(nil)
        
        self.switchMicrophone(off: false)
    }
    
    public func stopPublish(completed: AUIRoomCompleted? = nil) {
        self.isPublishing = false
        self.communicatingEngine?.publishLocalAudioStream(false)
        self.communicatingEngine?.setClientRole(AliRtcClientRole.rolelive)
        self.communicatingEngine?.muteLocalMic(false, mode: .allAudioMode)
        self.communicatingEngine?.stopAudioCapture()
        completed?(nil)
    }
    
    public func switchMicrophone(off: Bool, completed: AUIRoomCompleted? = nil) {
        // 打开或关闭我的麦克风
        self.communicatingEngine?.muteLocalMic(off, mode: .allAudioMode)
        if off {
            self.communicatingEngine?.stopAudioCapture()
        }
        else {
            self.communicatingEngine?.startAudioCapture()
        }
        self.notifyOnMicrophoneStateChanged(userId: self.user.userId, off: off)
        completed?(nil)
    }
    
    public func switchAudioOutput(type: AUIRoomAudioOutputType, completed:  AUIRoomCompleted? = nil) {
        if type == .Invalid {
            completed?(AUIRoomError.createError(.Common, "invaild input"))
            return
        }

        self.communicatingEngine?.enableSpeakerphone(type == .Speaker)
        completed?(nil)
    }
    
    public func getAudioOutputType() -> AUIRoomAudioOutputType {
        let enable = self.communicatingEngine?.isEnableSpeakerphone()
        guard let enable = enable else {
            return .Invalid
        }
        return enable ? .Speaker : .Headset
    }
}

extension AUIRoomRTCService: AliRtcEngineDelegate {
    
    public func onJoinChannelResult(_ result: Int32, channel: String, userId: String, elapsed: Int32) {
        debugPrint("AUIRoomRTCService onJoinChannelResult:\(result) userId:\(userId)")
    }
    
    public func onLeaveChannelResult(_ result: Int32, stats: AliRtcStats) {
        debugPrint("AUIRoomRTCService onLeaveChannelResult")

    }
    
    public func onRemoteUser(onLineNotify uid: String, elapsed: Int32) {
        debugPrint("AUIRoomRTCService onRemoteUserOnLineNotify:\(uid)")
        
        self.notifyOnJoin(userId: uid)
    }
    
    public func onRemoteUserOffLineNotify(_ uid: String, offlineReason reason: AliRtcUserOfflineReason) {
        debugPrint("AUIRoomRTCService onRemoteUserOffLineNotify:\(uid)")

        self.notifyOnLeave(userId: uid)
    }
    
    public func onRemoteTrackAvailableNotify(_ uid: String, audioTrack: AliRtcAudioTrack, videoTrack: AliRtcVideoTrack) {
        debugPrint("AUIRoomRTCService onRemoteTrackAvailableNotify:\(uid)  videoTrack:\(videoTrack)")

        DispatchQueue.main.async {
            if audioTrack == AliRtcAudioTrack.no {
                self.notifyOnStopedPublish(userId: uid)
            }
            else {
                self.notifyOnStartedPublish(userId: uid)
            }
        }
    }
    
    public func onUserAudioMuted(_ uid: String, audioMuted isMute: Bool) {
        debugPrint("AUIRoomRTCService onUserAudioMuted:\(uid) isMute:\(isMute)")

        self.notifyOnMicrophoneStateChanged(userId: uid, off: isMute)
    }
    
    public func onNetworkQualityChanged(_ uid: String, up upQuality: AliRtcNetworkQuality, downNetworkQuality downQuality: AliRtcNetworkQuality) {
        debugPrint("AUIRoomRTCService onNetworkQualityChanged:\(uid) upQuality:\(upQuality.rawValue)  downQuality:\(downQuality.rawValue)")
        
        var uploadState = AUIRoomNetworkState.Unknow
        if upQuality == .AlivcRtcNetworkQualityExcellent || upQuality == .AlivcRtcNetworkQualityGood {
            uploadState = .Good
        }
        else if upQuality == .AlivcRtcNetworkQualityPoor || upQuality == .AlivcRtcNetworkQualityBad {
            uploadState = .Poor
        }
        else if upQuality == .AlivcRtcNetworkQualityVeryBad || upQuality == .AlivcRtcNetworkQualityDisconnect {
            uploadState = .Bad
        }
        
        var downloadState = AUIRoomNetworkState.Unknow
        if downQuality == .AlivcRtcNetworkQualityExcellent || downQuality == .AlivcRtcNetworkQualityGood {
            downloadState = .Good
        }
        else if downQuality == .AlivcRtcNetworkQualityPoor || downQuality == .AlivcRtcNetworkQualityBad {
            downloadState = .Poor
        }
        else if downQuality == .AlivcRtcNetworkQualityVeryBad || downQuality == .AlivcRtcNetworkQualityDisconnect {
            downloadState = .Bad
        }
        self.notifyOnNetworkStateChanged(userId: uid, uploadState: uploadState, downloadState: downloadState)
    }
    

    public func onConnectionStatusChange(_ status: AliRtcConnectionStatus, reason: AliRtcConnectionStatusChangeReason) {
        debugPrint("AUIRoomRTCService onConnectionStatusChange:\(status.rawValue) reason:\(reason.rawValue)")

    }
    
    public func onOccurError(_ error: Int32, message: String) {
        debugPrint("AUIRoomRTCService onOccurError:\(error) message:\(message)")

        // Notify error, 信令心跳超时，检查网络连接是否正常
        let ConnectionHeartbeatTimeout = 0x0102020C
        if Int(error) == ConnectionHeartbeatTimeout {
            self.notifyOnError(error: AUIRoomError.createError(code: ConnectionHeartbeatTimeout, message: message))
        }
    }
    
    public func onOccurWarning(_ warn: Int32, message: String) {
        debugPrint("AUIRoomRTCService onOccurWarning:\(warn) message:\(message)")

    }
    
    public func onAuthInfoWillExpire() {
        debugPrint("AUIRoomRTCService onAuthInfoWillExpire")

        self.notifyOnJoinTokenWillExpire()
    }
    
    public func onActiveSpeaker(_ uid: String) {
        debugPrint("AUIRoomRTCService onActiveSpeaker:\(uid)")
        self.notifyOnSpeakerActived(userId: uid)
    }
    
    public func onAudioVolumeCallback(_ array: [AliRtcUserVolumeInfo]?, totalVolume: Int32) {
        var data = [String: Any]()
        array?.forEach({ info in
//            debugPrint("AUIRoomRTCService onAudioVolumeCallback:\(info.uid)  speech_state:\(info.speech_state)  volume:\(info.volume)  ")
            if info.speech_state == true {
                data[info.uid] = info.volume
            }
        })
        self.notifyOnAudioVolumeChanged(data: data)
        
    }
}



extension AUIRoomRTCService {
    
    public func addObserver(delegate: AUIRoomRTCServiceDelegate) {
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
    
    public func removeObserver(delegate: AUIRoomRTCServiceDelegate) {
        if Thread.isMainThread {
            self.observerArray.remove(delegate)
        }
        else {
            DispatchQueue.main.async {
                self.removeObserver(delegate: delegate)
            }
        }
    }
    
    func notifyOnJoin(userId: String) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onJoined?(userId: userId)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnJoin(userId: userId)
            }
        }
    }
    
    func notifyOnLeave(userId: String) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onLeaved?(userId: userId)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnLeave(userId: userId)
            }
        }
    }
    
    func notifyOnStartedPublish(userId: String) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onStartedPublish?(userId: userId)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnStartedPublish(userId: userId)
            }
        }
    }
    
    func notifyOnStopedPublish(userId: String) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onStopedPublish?(userId: userId)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnStopedPublish(userId: userId)
            }
        }
    }
    
    func notifyOnMicrophoneStateChanged(userId: String, off: Bool) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onMicrophoneStateChanged?(userId: userId, off: off)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMicrophoneStateChanged(userId: userId, off: off)
            }
        }
    }
    
    func notifyOnNetworkStateChanged(userId: String, uploadState: AUIRoomNetworkState, downloadState: AUIRoomNetworkState) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onNetworkStateChanged?(userId: userId, uploadState: uploadState.rawValue, downloadState: downloadState.rawValue)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnNetworkStateChanged(userId: userId, uploadState: uploadState, downloadState: downloadState)
            }
        }
    }
    
    func notifyOnAudioVolumeChanged(data: [String: Any]) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onAudioVolumeChanged?(data: data)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnAudioVolumeChanged(data: data)
            }
        }
    }
    
    func notifyOnSpeakerActived(userId: String) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onSpeakerActived?(userId: userId)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnSpeakerActived(userId: userId)
            }
        }
    }
    
    func notifyOnJoinTokenWillExpire() {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                if let onJoinTokenWillExpire = delegate.onJoinTokenWillExpire {
                    onJoinTokenWillExpire()
                }
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnJoinTokenWillExpire()
            }
        }
    }
    
    func notifyOnError(error: Error) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                if let onError = delegate.onError {
                    onError(error)
                }
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnError(error: error)
            }
        }
    }
}
