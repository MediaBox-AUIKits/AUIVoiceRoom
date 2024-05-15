//
//  ARTCKaraokeRoomEngine.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/6.
//

import UIKit

#if canImport(AliVCSDK_ARTC)
import AliVCSDK_ARTC
#elseif canImport(AliVCSDK_InteractiveLive)
import AliVCSDK_InteractiveLive
#elseif canImport(AliVCSDK_Standard)
import AliVCSDK_Standard
#endif


@objc public enum ARTCKaraokeRoomSingerRole: Int {
    case Audience = 0       // 听众（默认状态）
    case LeadSinger         // 主唱者（点歌用户）
    case JoinSinger         // 合唱者
}

@objc public enum ARTCKaraokeRoomMusicPrepareState: Int {
    case Unprepared = 1       // 未准备
    case Preparing            // 准备中
    case Prepared             // 已准备
    case Failed               // 失败
}

@objc public enum ARTCKaraokeRoomMusicPlayState: Int {
    case Idle = 1           // 未播放状态
    case Playing            // 播放中
    case Paused             // 暂停状态
    case Completed          // 播放完成，用户需要切换到播放下一首歌，或者结束演唱
}

@objcMembers open class ARTCKaraokeRoomMusicConfig: NSObject {

    /**
     * 本地或地址
     */
    public var uri: String = ""

    /**
     * 开始播放的位置，单位：毫秒
     */
    public var startPosition: Int64 = 0

    /**
     * 设置为 true 后会在资源加载完成时自动开始播放音乐
     */
    public var autoPlay: Bool = false


    /**
     * 当音频有多音轨时（如伴奏和原唱）需设置多音轨模式
     */
    public var isMultipleTrack: Bool = false

}

@objc public protocol ARTCKaraokeRoomEngineDelegate {
    
    /**
     * 音乐资源准备状态回调 <p>
     * 听众角色不会收到这个回调
     */
    @objc optional func onMusicPrepareStateUpdate(state: ARTCKaraokeRoomMusicPrepareState)
    
    /**
     * 音乐播放状态回调 <p>
     * 听众角色不会收到这个回调
     */
    @objc optional func onMusicPlayStateUpdate(state: ARTCKaraokeRoomMusicPlayState)
    
    
    /**
     * 音乐播放进度回调
     * @param millisecond 音乐当前播放的位置
     */
    @objc optional func onMusicPlayProgressUpdate(millisecond: Int64)
    
    /**
     * 演唱角色变化回调
     */
    @objc optional func onSingerRoleUpdate(newRole: ARTCKaraokeRoomSingerRole, oldRole: ARTCKaraokeRoomSingerRole)
    
}


@objcMembers open class ARTCKaraokeRoomEngine: NSObject {

    /**
     * 回调
     */
    public weak var delegate: ARTCKaraokeRoomEngineDelegate? = nil

    /**
     * 当前角色
     */
    public private(set) var singerRole: ARTCKaraokeRoomSingerRole = .Audience
    
    /**
     * 当前音乐加载状态
     */
    public private(set) var mediaPrepareState: ARTCKaraokeRoomMusicPrepareState = .Unprepared
    
    /**
     * 当前播放状态
     */
    public private(set) var mediaPlayState: ARTCKaraokeRoomMusicPlayState = .Idle

    /**
     * 转换演唱角色，以管理相应角色的推拉流逻辑。 <p>
     * 调用时机：开始演唱前（必须在调用 {@link #playMusic()} 前），或者结束演唱后
     *
     * @param newRole 目标转换角色
     * @return 是否支持转换到目标角色
     */
    public func switchSingerRole(newRole: ARTCKaraokeRoomSingerRole) -> Bool {
        
        let oldRole = self.singerRole
        if oldRole == newRole {
            return false;
        }
        
        switch newRole {
        case .LeadSinger:
            self.rtcEngine?.publishLocalDualAudioStream(true)
            self.rtcEngine?.subscribeAllRemoteDualAudioStreams(false)
            break
        case .JoinSinger:
            self.rtcEngine?.publishLocalDualAudioStream(false)
            self.rtcEngine?.subscribeAllRemoteDualAudioStreams(false)
            break
        default:
            self.rtcEngine?.publishLocalDualAudioStream(false)
            self.rtcEngine?.subscribeAllRemoteDualAudioStreams(true)
        }
        
        debugPrint("set singer role from \(oldRole) to \(newRole)")
        self.singerRole = newRole
        self.delegate?.onSingerRoleUpdate?(newRole: newRole, oldRole: oldRole)
        return true;
    }


    /*************************************************
     * 演唱者的媒体播放管理 <p>
     * 同一时间内只能有一首歌曲在加载和播放
     *************************************************/

    /**
     * 加载音乐资源，当收到加载成功回调后即可调用 {@link #playMusic()} 播放音乐
     *
     * @param uri 本地或在线音乐地址
     */
    public func loadMusicWithUri(uri: String) {
        let config = ARTCKaraokeRoomMusicConfig()
        config.uri = uri
        self.loadMusicWithConfig(config: config)
    }

    /**
     * 加载音乐资源，当收到加载成功回调后即可调用 {@link #playMusic()} 播放音乐 <p>
     * 如果设置了 {@link ARTCKaraokeRoomMusicConfig#autoPlay} 为 true 则内部会自动调用 {@link #playMusic()}
     *
     * @param config 音乐资源加载配置
     */
    public func loadMusicWithConfig(config: ARTCKaraokeRoomMusicConfig) {
        debugPrint("load music resource: \(config.uri), auto play: \(config.autoPlay)")
        self.stopMusic()
        
        if let rtcEngine = self.rtcEngine {
            let player = rtcEngine.createMediaPlayer()
            if let player = player {
                player.setEventDelegate(self)
                player.enableAux(true)
                if config.isMultipleTrack {
                    player.setAudioDualChannelMode(.trackSwitch)
                }
                else {
                    player.setAudioDualChannelMode(.channelSwitch)
                }
                player.setProgressInterval(50)
                self.mediaPlayer = player
                self.musicConfig = config
                
                self.updateMusicPrepareStateAndNotify(state: .Preparing)
                player.loadResource(withPosition: config.uri, startPosition: config.startPosition, delegate: self)
            }
        }
    }

    /**
     * 播放音乐。需要先调用 {@link #loadMusic} 并收到加载成功回调，才能调用此方法开始播放。 <p>
     * 如果设置了 {@link ARTCKaraokeRoomMusicConfig#autoPlay} 为 true 则可以不调用此方法； <p>
     * 合唱者也可以不调用此方法，内部会自动同步主唱者播放状态
     */
    public func playMusic() {
        if let player = self.mediaPlayer {
            if self.mediaPrepareState == .Prepared {
                player.start()
            }
        }
    }

    /**
     * 停止播放音乐 <p>
     * {@link #setSingerRole(KTVSingerRole)} 设置为 {@link KTVSingerRole#Audience} 时也会自动停止播放
     */
    public func stopMusic() {
        if let player = self.mediaPlayer {
            player.stop()
            self.rtcEngine?.destroy(player)
            
            self.mediaPlayer = nil;
            self.musicConfig = nil;
            self.updateMusicPrepareStateAndNotify(state: .Unprepared)
            self.updateMusicPlayStateAndNotify(state: .Idle)
        }
    }

    /**
     * 继续播放音乐
     */
    public func resumeMusic() {
        if let player = self.mediaPlayer {
            if self.mediaPrepareState == .Prepared {
                player.resume()
            }
        }
    }

    /**
     * 暂停播放音乐
     */
    public func pauseMusic() {
        if let player = self.mediaPlayer {
            if self.mediaPrepareState == .Prepared {
                player.pause()
            }
        }
    }

    /**
     * 跳到某个位置
     */
    public func seekMusicTo(millisecond: Int64) {
        if millisecond >= 0 && self.mediaPrepareState == .Prepared {
            if let player = self.mediaPlayer {
                player.seek(to: millisecond, delegate: self)
            }
        }
    }

    /**
     * 获取音乐总时长，必须在加载音乐资源成功后调用才有效，否则返回 0
     */
    public func getMusicTotalDuration() -> Int64 {
        if let player = self.mediaPlayer {
            if self.mediaPrepareState == .Prepared {
                return player.getTotalDuration()
            }
        }
        return 0;
    }

    /**
     * 获取当前播放进度，必须在加载音乐资源成功后调用才有效，否则返回 0
     */
    public func getMusicCurrentProgress() -> Int64 {
        if let player = self.mediaPlayer {
            if self.mediaPrepareState == .Prepared {
                return player.getCurrentProgress()
            }
        }
        return 0;
    }

    /**
     * 切换伴奏/原唱模式
     * @param original 是否原唱
     */
    public func setMusicAccompanimentMode(original: Bool) {
        if let player = self.mediaPlayer {
            if self.musicConfig?.isMultipleTrack == true {
                player.setAudioTrackIndex(original ? 1 : 0);
            }
            else {
                player.setAudioTrackIndex(original ? 0 : 1);
            }
        }
    }

    /**
     * 设置伴奏（音乐播放）音量
     * @param volume 目标音量
     */
    public func setMusicVolume(volume: Int32) {
        if let player = self.mediaPlayer {
            player.setPlayVolume(volume);
        }
    }

    
    private var rtcEngine: AliRtcEngine? = nil
    private var mediaPlayer: AliRtcEngineMediaPlayer? = nil
    private var musicConfig: ARTCKaraokeRoomMusicConfig? = nil
}

extension ARTCKaraokeRoomEngine {
    
    /**
     * 处理KTV场景下需要处理的消息，桥接RTCService
     */
    public func onDataChannelMessage(uid: String, controlMsg: AnyObject) {
        DispatchQueue.main.async {
            let msg = controlMsg as? AliRtcDataChannelMsg
            if let msg = msg {
                if msg.type == .musicProgress {
                    self.onRemotePlayProgressUpdate(remoteProgress: Int64(msg.progress), remoteNtp: msg.networkTime)
                }
                else if msg.type == .custom {
                    self.onRemotePlayStateUpdate(msgData: msg.data)
                }
            }
        }
    }
    
    /**
     * 初始化rtc引擎，桥接RTCService
     */
    public func setupRtcEngine(rtcEngine: AnyObject?) {
        if let rtcEngine = rtcEngine as? AliRtcEngine {
            self.rtcEngine = rtcEngine
            self.rtcEngine?.publishLocalDualAudioStream(false)
            self.rtcEngine?.subscribeAllRemoteDualAudioStreams(true)
        }
    }
    
    /**
     * 释放rtc引擎，桥接RTCService
     */
    public func releaseRtcEngine() {
        self.stopMusic()
        self.rtcEngine = nil
    }
}

extension ARTCKaraokeRoomEngine: AliRtcEngineMediaPlayerEventDelegate {
    
    public func onMediaPlayerStateUpdate(_ mediaPlayer: AliRtcEngineMediaPlayer, state: AliRtcPlayerState, errorCode: Int32) {
        debugPrint("onMediaPlayerStateUpdate, state: \(state), error code: \(errorCode)")
        self.onLocalPlayStateUpdate(rtcPlayerState: state, errorCode: errorCode)
    }

    public func onMediaPlayerNetworkEvent(_ mediaPlayer: AliRtcEngineMediaPlayer, networkEvent: AliRtcMediaPlayerNetworkEvent) {
        
    }

    public func onMediaPlayerPlayingProgress(_ mediaPlayer: AliRtcEngineMediaPlayer, millisecond: Int64) {
        debugPrint("onMediaPlayerPlayingProgress, position(ms): \(millisecond)")
        self.onLocalPlayProgressUpdate(millisecond: millisecond)
    }

    public func onMediaPlayerFirstFrameEvent(_ mediaPlayer: AliRtcEngineMediaPlayer, event: AliRtcMediaPlayerFirstFrameEvent) {
        
    }

    public func onMediaPlayerLocalCache(_ mediaPlayer: AliRtcEngineMediaPlayer, errorCode: Int32, resource: String, cachedFile: String) {
        
    }
}

extension ARTCKaraokeRoomEngine: AliRtcEngineMediaPlayerLoadResourceDelegate {
    
    public func onLoadResourceCallback(_ errorCode: Int32) {
        debugPrint("load resource finish, result = \(errorCode)")
        DispatchQueue.main.async {
            self.setMusicAccompanimentMode(original: false)  // 默认是伴唱
            let success = errorCode == 0
            self.updateMusicPrepareStateAndNotify(state: success ? .Prepared : .Failed)
            if success && self.musicConfig?.autoPlay == true {
                self.playMusic()
            }
        }
    }
}

extension ARTCKaraokeRoomEngine: AliRtcEngineMediaPlayerSeekToDelegate {
    
    public func onSeek(toTimeCallback errorCode: Int32) {
        if errorCode != 0 {
            debugPrint("seek music error: \(errorCode)")
        }
    }
    
}


extension ARTCKaraokeRoomEngine {
    
    func transformRtcPlayerState(playerState: AliRtcPlayerState) -> ARTCKaraokeRoomMusicPlayState {
        switch playerState {
        case .noPlay: return .Idle
        case .playing: return .Playing
        case .pausing: return .Paused
        case .playEnded: return .Completed
        @unknown default:
            debugPrint("Unknown media player state!: \(playerState)")
        }
        return .Idle
    }

    func onLocalPlayProgressUpdate(millisecond: Int64) {
        self.delegate?.onMusicPlayProgressUpdate?(millisecond: millisecond)
        
        guard let rtcEngine = self.rtcEngine else { return }
        
        // 主唱者发送进度给其他用户同步，合唱者发送假进度（RTC SDK 内部实现需要用到: -1）
        let msg = AliRtcDataChannelMsg()
        msg.type = .musicProgress
        msg.networkTime = rtcEngine.getNetworkTime()
        if self.singerRole == .LeadSinger {
            msg.progress = Int32(millisecond)
        }
        else {
            msg.progress = -1
        }
        rtcEngine.sendDataChannelMessage(msg)
    }

    func onLocalPlayStateUpdate(rtcPlayerState: AliRtcPlayerState, errorCode: Int32) {
        let newState = self.transformRtcPlayerState(playerState: rtcPlayerState)
        if newState == self.mediaPlayState {
            return
        }
        
        self.updateMusicPlayStateAndNotify(state: newState)
        
        guard let rtcEngine = self.rtcEngine else { return }
        if self.singerRole == .LeadSinger {
            let msg = AliRtcDataChannelMsg()
            msg.type = .custom
            msg.networkTime = rtcEngine.getNetworkTime()
            
            let dataDict = [
                "playState": newState
            ]
            let data = try? JSONSerialization.data(withJSONObject: dataDict, options: .prettyPrinted)
            if let data = data {
                msg.data = data
                rtcEngine.sendDataChannelMessage(msg)
            }
        }
    }

    /**
     * 接收到主唱者发送的播放进度消息
     */
    func onRemotePlayProgressUpdate(remoteProgress: Int64, remoteNtp: Int64) {
        
        if self.singerRole == .Audience {
            self.delegate?.onMusicPlayProgressUpdate?(millisecond: remoteProgress)
        }
        else if self.singerRole == .JoinSinger && self.mediaPrepareState == .Prepared {
            // 1）合唱者未开始播放，则根据主唱者播放进度开始播放音乐
            if self.mediaPlayState == .Idle {
                self.playMusic()
            }
            else if self.mediaPlayState == .Playing {
                // 2）合唱者已在本地播放，则根据主唱者播放进度调整本地播放进度
                guard let rtcEngine = self.rtcEngine else { return }
                let expectProgress = remoteProgress + (rtcEngine.getNetworkTime() - remoteNtp)
                let currentProgress = self.getMusicCurrentProgress()
                if abs(expectProgress - currentProgress) > 50 {
                    debugPrint("Chorister: remote playing position diff with local position(ms): \(expectProgress - currentProgress)")
                    self.seekMusicTo(millisecond: expectProgress)
                }
            }
        }
    }

    /**
     * 接收到主唱者发送的播放状态变更消息
     */
    func onRemotePlayStateUpdate(msgData: Data) {
        if self.singerRole == .JoinSinger {
            let dict = (try? JSONSerialization.jsonObject(with: msgData, options: .allowFragments)) as? [String : Any]
            if let state = dict?["playState"] as? Int {
                let remoteState = ARTCKaraokeRoomMusicPlayState(rawValue: state)
                if remoteState != self.mediaPlayState {
                    if remoteState == .Paused {
                        self.pauseMusic()
                    }
                    else if remoteState == .Playing && self.mediaPlayState == .Paused {
                        self.resumeMusic()
                    }
                }
            }
        }
    }

    func updateMusicPrepareStateAndNotify(state: ARTCKaraokeRoomMusicPrepareState) {
        if self.mediaPrepareState != state {
            self.mediaPrepareState = state
            self.delegate?.onMusicPrepareStateUpdate?(state: state)
        }
    }

    func updateMusicPlayStateAndNotify(state: ARTCKaraokeRoomMusicPlayState) {
        if self.mediaPlayState != state {
            self.mediaPlayState = state
            self.delegate?.onMusicPlayStateUpdate?(state: state)
        }
    }

}
