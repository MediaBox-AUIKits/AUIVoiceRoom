//
//  ARTCRoomRTCService.swift
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

@objc public enum ARTCRoomSceneType: Int {
    case VoiceRoom
    case KTV
    case Call  // 暂不支持
}

@objc public enum ARTCRoomCameraType: Int {
    case Invalid = -1
    case Back
    case Front
}

@objc public enum ARTCRoomAudioOutputType: Int {
    case Invalid = -1
    case Speaker
    case Headset
}

public enum ARTCRoomNetworkState: String {
    case Good
    case Poor
    case Bad
    case Unknow
}

@objc public enum ARTCRoomMusicState: Int {
    case None
    case Started
    case Ended
    case Failed
}

// 变声
@objc public enum ARTCRoomVoiceChangerMode: Int {
    /** 关闭变声音效 */
    case Off = 0
    /** 老人 */
    case OldMan
    /** 男孩 */
    case BabyBoy
    /** 女孩 */
    case BabyGirl
    /** 机器人 */
    case Robot
    /** 大魔王 */
    case Daimo
    /** KTV */
    case KTV
    /** 回声 */
    case Echo
    /** 方言 */
    case Dialect
    /** 怒吼 */
    case Howl
    /** 电音 */
    case Electronic
    /** 留声机 */
    case Phonograph
}

// 混响
@objc public enum ARTCRoomVoiceReverbMode: Int {
    /** 关闭混响 */
    case Off = 0
    /** 人声I */
    case Vocal1
    /** 人声II */
    case Vocal2
    /** 澡堂 */
    case BathRoom
    /** 明亮小房间 */
    case SmallRoomBright
    /** 黑暗小房间 */
    case SmallRoomDark
    /** 中等房间 */
    case MediumRoom
    /** 大房间 */
    case LargeRoom
    /** 教堂走廊 */
    case ChurchHall
}


@objcMembers public class ARTCRoomConfig: NSObject {
    
    public var appId = "你的appID"
    public var gslb = "https://gw.rtn.aliyuncs.com"
    public var roomId = ""
    public var timestamp: Int64 = 0
    public var token = ""
    public var dimensions = CGSize(width: 360, height: 640)
    public var frameRate = 15
}


@objc public protocol ARTCRoomRTCServiceDelegate {
    
    @objc optional func onJoined(userId: String)
    @objc optional func onLeaved(userId: String)

    @objc optional func onStartedPublish(userId: String)
    @objc optional func onStopedPublish(userId: String)

    @objc optional func onMicrophoneStateChanged(userId: String, off: Bool)
    
    @objc optional func onNetworkStateChanged(userId: String, uploadState: String, downloadState: String)

    
    @objc optional func onAudioVolumeChanged(data: [String: Any])
    @objc optional func onSpeakerActived(userId: String)
    
    @objc optional func onMusicPlayStateChanged(state: ARTCRoomMusicState)
    @objc optional func onAudioEffectPlayCompleted(effectId: Int32)
    
    @objc optional func onError(_ error: Error)
    @objc optional func onJoinTokenWillExpire()
}

@objc public protocol ARTCRoomRTCServiceBridgeDelegate {
    
    @objc optional func onSetupRtcEngine(rtcEngine: AnyObject?)
    @objc optional func onWillReleaseEngine()
    @objc optional func onDataChannelMessage(uid: String, controlMsg: AnyObject)
}


@objcMembers open class ARTCRoomRTCService: NSObject {
    
    public init(mode: ARTCRoomSceneType, user: ARTCRoomUser) {
        self.mode = mode
        self.user = user
        super.init()
    }
    
    deinit {
        debugPrint("deinit: \(self)")
    }
    
    public func destroy() {
        self.leave {[weak self] error in
            self?.destroyRtcEngine()
        }
    }
    
    public let mode: ARTCRoomSceneType
    public let user: ARTCRoomUser
    
    public private(set) var roomConfig: ARTCRoomConfig?
    
    internal lazy var observerArray: NSHashTable<ARTCRoomRTCServiceDelegate> = {
        return NSHashTable<ARTCRoomRTCServiceDelegate>.weakObjects()
    }()
    
    public weak var bridgeDelegate: ARTCRoomRTCServiceBridgeDelegate? = nil
    
    private var rtcEngine: AliRtcEngine? = nil
    public func getRTCEngine() -> AnyObject? {
        return self.rtcEngine
    }
    
    private func setupRtcEngine() {
        if self.rtcEngine != nil {
            return
        }
        
        let extras: [String: String] = [:]
        let engine = AliRtcEngine.sharedInstance(self, extras:extras.artcJsonString)
        
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
        
        self.rtcEngine = engine
        self.bridgeDelegate?.onSetupRtcEngine?(rtcEngine: engine)
    }
    
    private func destroyRtcEngine() {
        self.bridgeDelegate?.onWillReleaseEngine?()
        AliRtcEngine.destroy()
        self.rtcEngine = nil
    }
    
    public func join(config: ARTCRoomConfig, completed: ((_ error: NSError?) -> Void)? = nil) -> Void {
        
        self.setupRtcEngine()
        
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
        self.rtcEngine?.joinChannel(info, name: self.user.userNick, onResultWithUserId: { errCode, channel, userId, elapsed in
            var err : NSError? = nil
            if errCode != 0 {
                debugPrint("[error]AUIRoomRTCService joinChannel failed:\(errCode)")
                err = ARTCRoomError.createError(code: errCode, message: "join channel failed")
            }
            else {
                self.roomConfig = config
            }
            completed?(err)
        })
    }
    
    public func leave(completed: ((_ error: NSError?) -> Void)? = nil) {
        self.stopPublish()
        
        self.rtcEngine?.leaveChannel()
        self.roomConfig = nil
        completed?(nil)
    }
    
    public var isJoin: Bool {
        return self.roomConfig != nil
    }
    
    public private(set) var isPublishing: Bool = false

    
    public func startPublish(completed: ((_ error: NSError?) -> Void)? = nil) {
        self.rtcEngine?.muteLocalMic(false, mode: .allAudioMode)
        self.rtcEngine?.setClientRole(AliRtcClientRole.roleInteractive)
        self.rtcEngine?.publishLocalAudioStream(true)
        self.isPublishing = true
        completed?(nil)
        
        self.switchMicrophone(off: false)
    }
    
    public func stopPublish(completed: ((_ error: NSError?) -> Void)? = nil) {
        self.isPublishing = false
        self.rtcEngine?.publishLocalAudioStream(false)
        self.rtcEngine?.setClientRole(AliRtcClientRole.rolelive)
        self.rtcEngine?.muteLocalMic(false, mode: .allAudioMode)
        self.rtcEngine?.stopAudioCapture()
        completed?(nil)
    }
    
    public func switchMicrophone(off: Bool, completed: ((_ error: NSError?) -> Void)? = nil) {
        // 打开或关闭我的麦克风
        self.rtcEngine?.muteLocalMic(off, mode: .allAudioMode)
        if off {
            self.rtcEngine?.stopAudioCapture()
        }
        else {
            self.rtcEngine?.startAudioCapture()
        }
        self.notifyOnMicrophoneStateChanged(userId: self.user.userId, off: off)
        completed?(nil)
    }
    
    public func switchAudioOutput(type: ARTCRoomAudioOutputType, completed:  ((_ error: NSError?) -> Void)? = nil) {
        if type == .Invalid {
            completed?(ARTCRoomError.createError(.Common, "invaild input"))
            return
        }

        self.rtcEngine?.enableSpeakerphone(type == .Speaker)
        completed?(nil)
    }
    
    public func getAudioOutputType() -> ARTCRoomAudioOutputType {
        let enable = self.rtcEngine?.isEnableSpeakerphone()
        guard let enable = enable else {
            return .Invalid
        }
        return enable ? .Speaker : .Headset
    }
    
    
    public func switchEarBack(on: Bool, completed:  ((_ error: NSError?) -> Void)? = nil) {
        self.rtcEngine?.enableEarBack(on)
        self.isEarBack = on
        completed?(nil)
    }
    
    private var isEarBack: Bool = false

    public func getIsEarBack() -> Bool {
        return self.isEarBack
    }
    
    // 设置音频采集音量，默认50，可调节[0~100]
    public func setRecordingVolume(volume: Int32, completed:  ((_ error: NSError?) -> Void)? = nil) {
        if volume < 0 || volume > 100 {
            completed?(ARTCRoomError.createError(.Common, "invaild input"))
            return
        }
        var value = 100
        if volume < 50 {
            // 【0~50】映射到【0~100】
            value = Int(Double(volume) / 50.0 * 100.0)
        }
        else {
            // 【50~100】映射到【100~400】
            value = Int(Double(volume - 50) / 50 * 300.0 + 100)
        }
        self.rtcEngine?.setRecordingVolume(value)
        self.recordingVolume = volume;
        completed?(nil)
    }
    
    // 获取音频采集音量，默认50
    public func getRecordingVolume() -> Int32 {
        return self.recordingVolume
    }
    
    private var recordingVolume: Int32 = 50
    
    // 开始播放音效
    public func startPlayAudioEffect(effectId: Int32, localPath: String, volume: Int32, onlyLocalPlay: Bool, completed:  ((_ error: NSError?) -> Void)? = nil) {
        let config = AliRtcAudioEffectConfig()
        config.needPublish = onlyLocalPlay
        config.loopCycles = 1
        config.playoutVolume = min(max(0, volume), 100)
        config.publishVolume = config.playoutVolume
        self.rtcEngine?.playAudioEffect(withSoundId: Int(effectId), filePath: localPath, config: config)
        completed?(nil)
    }
    
    // 设置音效音量，默认50，可调节[0~100]
    public func setAudioEffectVolume(effectId: Int32, volume: Int32, completed:  ((_ error: NSError?) -> Void)? = nil) {
        let v = min(max(0, volume), 100)
        self.rtcEngine?.setAudioEffectPlayoutVolumeWithSoundId(Int(effectId), volume: Int(v))
        self.rtcEngine?.setAudioEffectPublishVolumeWithSoundId(Int(effectId), volume: Int(v))
        completed?(nil)
    }
    
    // 停止播放音效
    public func stopPlayAudioEffect(effectId: Int32, completed:  ((_ error: NSError?) -> Void)? = nil) {
        self.rtcEngine?.stopAudioEffect(withSoundId: Int(effectId))
        completed?(nil)
    }
    
    // 播放音乐
    public func startPlayMusic(localPath: String, volume: Int32, onlyLocalPlay: Bool, completed:  ((_ error: NSError?) -> Void)? = nil) {
        let config = AliRtcAudioAccompanyConfig()
        config.onlyLocalPlay = onlyLocalPlay
        config.replaceMic = false
        config.loopCycles = 1
        config.playoutVolume = min(max(0, volume), 100)
        config.publishVolume = config.playoutVolume

        self.rtcEngine?.startAudioAccompany(withFile: localPath, config: config)
        completed?(nil)
    }
    
    // 设置播放音乐音量，默认50，可调节[0~100]
    public func setMusicPlayingVolume(volume: Int32, completed:  ((_ error: NSError?) -> Void)? = nil) {
        let v = min(max(0, volume), 100)
        self.rtcEngine?.setAudioAccompanyVolume(Int(v))
        completed?(nil)
    }
    
    // 停止播放音乐
    public func stopPlayMusic(completed:  ((_ error: NSError?) -> Void)? = nil) {
        self.rtcEngine?.stopAudioAccompany()
        completed?(nil)
    }
    
    // 设置变声类型
    public func setVoiceChangerMode(mode: ARTCRoomVoiceChangerMode, completed:  ((_ error: NSError?) -> Void)? = nil) {
        guard let changer = AliRtcAudioEffectVoiceChangerMode(rawValue: mode.rawValue) else {
            completed?(ARTCRoomError.createError(.Common, "invaild input"))
            return
        }
        self.rtcEngine?.setAudioEffectVoiceChangerMode(changer)
        self.voiceChangerMode = mode
        completed?(nil)
    }
    
    private var voiceChangerMode: ARTCRoomVoiceChangerMode = .Off
    
    // 获取变声类型
    public func getVoiceChangerMode() -> ARTCRoomVoiceChangerMode {
        return self.voiceChangerMode
    }
    
    // 设置混响类型
    public func setVoiceReverbMode(mode: ARTCRoomVoiceReverbMode, completed:  ((_ error: NSError?) -> Void)? = nil) {
        guard let reverb = AliRtcAudioEffectReverbMode(rawValue: mode.rawValue) else {
            completed?(ARTCRoomError.createError(.Common, "invaild input"))
            return
        }
        self.rtcEngine?.setAudioEffectReverbMode(reverb)
        self.voiceReverbMode = mode
        completed?(nil)
    }
    
    private var voiceReverbMode: ARTCRoomVoiceReverbMode = .Off
    
    // 获取混响类型
    public func getVoiceReverbMode() -> ARTCRoomVoiceReverbMode {
        return self.voiceReverbMode
    }
}

extension ARTCRoomRTCService: AliRtcEngineDelegate {
    
    public func onDataChannelMessage(_ uid: String, controlMsg: AliRtcDataChannelMsg) {
        self.bridgeDelegate?.onDataChannelMessage?(uid: uid, controlMsg: controlMsg)
    }
    
    public func onJoinChannelResult(_ result: Int32, channel: String, userId: String, elapsed: Int32) {
        debugPrint("ARTCRoomRTCService onJoinChannelResult:\(result) userId:\(userId)")
    }
    
    public func onLeaveChannelResult(_ result: Int32, stats: AliRtcStats) {
        debugPrint("ARTCRoomRTCService onLeaveChannelResult")

    }
    
    public func onRemoteUser(onLineNotify uid: String, elapsed: Int32) {
        debugPrint("ARTCRoomRTCService onRemoteUserOnLineNotify:\(uid)")
        
        self.notifyOnJoin(userId: uid)
    }
    
    public func onRemoteUserOffLineNotify(_ uid: String, offlineReason reason: AliRtcUserOfflineReason) {
        debugPrint("ARTCRoomRTCService onRemoteUserOffLineNotify:\(uid)")

        self.notifyOnLeave(userId: uid)
    }
    
    public func onRemoteTrackAvailableNotify(_ uid: String, audioTrack: AliRtcAudioTrack, videoTrack: AliRtcVideoTrack) {
        debugPrint("ARTCRoomRTCService onRemoteTrackAvailableNotify:\(uid)  videoTrack:\(videoTrack)")

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
        debugPrint("ARTCRoomRTCService onUserAudioMuted:\(uid) isMute:\(isMute)")

        self.notifyOnMicrophoneStateChanged(userId: uid, off: isMute)
    }
    
    public func onNetworkQualityChanged(_ uid: String, up upQuality: AliRtcNetworkQuality, downNetworkQuality downQuality: AliRtcNetworkQuality) {
        debugPrint("ARTCRoomRTCService onNetworkQualityChanged:\(uid) upQuality:\(upQuality.rawValue)  downQuality:\(downQuality.rawValue)")
        
        var uploadState = ARTCRoomNetworkState.Unknow
        if upQuality == .AlivcRtcNetworkQualityExcellent || upQuality == .AlivcRtcNetworkQualityGood {
            uploadState = .Good
        }
        else if upQuality == .AlivcRtcNetworkQualityPoor || upQuality == .AlivcRtcNetworkQualityBad {
            uploadState = .Poor
        }
        else if upQuality == .AlivcRtcNetworkQualityVeryBad || upQuality == .AlivcRtcNetworkQualityDisconnect {
            uploadState = .Bad
        }
        
        var downloadState = ARTCRoomNetworkState.Unknow
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
        debugPrint("ARTCRoomRTCService onConnectionStatusChange:\(status.rawValue) reason:\(reason.rawValue)")

    }
    
    public func onOccurError(_ error: Int32, message: String) {
        debugPrint("ARTCRoomRTCService onOccurError:\(error) message:\(message)")

        // Notify error, 信令心跳超时，检查网络连接是否正常
        let ConnectionHeartbeatTimeout = 0x0102020C
        if Int(error) == ConnectionHeartbeatTimeout {
            self.notifyOnError(error: ARTCRoomError.createError(code: ConnectionHeartbeatTimeout, message: message))
        }
    }
    
    public func onOccurWarning(_ warn: Int32, message: String) {
        debugPrint("ARTCRoomRTCService onOccurWarning:\(warn) message:\(message)")

    }
    
    public func onAuthInfoWillExpire() {
        debugPrint("ARTCRoomRTCService onAuthInfoWillExpire")

        self.notifyOnJoinTokenWillExpire()
    }
    
    public func onActiveSpeaker(_ uid: String) {
        debugPrint("ARTCRoomRTCService onActiveSpeaker:\(uid)")
        self.notifyOnSpeakerActived(userId: uid)
    }
    
    public func onAudioVolumeCallback(_ array: [AliRtcUserVolumeInfo]?, totalVolume: Int32) {
        var data = [String: Any]()
        array?.forEach({ info in
//            debugPrint("ARTCRoomRTCService onAudioVolumeCallback:\(info.uid)  speech_state:\(info.speech_state)  volume:\(info.volume)  ")
            if info.speech_state == true {
                data[info.uid] = info.volume
            }
        })
        self.notifyOnAudioVolumeChanged(data: data)
        
    }
    
    public func onAudioAccompanyStateChanged(_ playState: AliRtcAudioAccompanyStateCode, errorCode: AliRtcAudioAccompanyErrorCode) {
        debugPrint("ARTCRoomRTCService onAudioAccompanyStateChanged:\(playState.rawValue) errorCode:\(errorCode.rawValue)")
        if playState == .started {
            self.notifyOnMusicPlayStateChanged(state: .Started)
        }
        else if playState == .stopped || playState == .ended {
            self.notifyOnMusicPlayStateChanged(state: .Ended)
        }
        else if playState == .failed {
            self.notifyOnMusicPlayStateChanged(state: .Failed)
        }
    }
    
    public func onRemoteAudioAccompanyStarted(_ uid: String) {
        debugPrint("ARTCRoomRTCService onRemoteAudioAccompanyStarted:\(uid)")

    }
    
    public func onRemoteAudioAccompanyFinished(_ uid: String) {
        debugPrint("ARTCRoomRTCService onRemoteAudioAccompanyStarted:\(uid)")

    }
    
    public func onAudioEffectFinished(_ soundId: Int32) {
        debugPrint("ARTCRoomRTCService onAudioEffectFinished:\(soundId)")
        self.notifyOnAudioEffectPlayCompleted(effectId: soundId)
    }
}



extension ARTCRoomRTCService {
    
    public func addObserver(delegate: ARTCRoomRTCServiceDelegate) {
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
    
    public func removeObserver(delegate: ARTCRoomRTCServiceDelegate) {
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
    
    func notifyOnNetworkStateChanged(userId: String, uploadState: ARTCRoomNetworkState, downloadState: ARTCRoomNetworkState) {
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
    
    func notifyOnMusicPlayStateChanged(state: ARTCRoomMusicState) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onMusicPlayStateChanged?(state: state)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMusicPlayStateChanged(state: state)
            }
        }
    }
    
    func notifyOnAudioEffectPlayCompleted(effectId: Int32) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onAudioEffectPlayCompleted?(effectId: effectId)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnAudioEffectPlayCompleted(effectId: effectId)
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
