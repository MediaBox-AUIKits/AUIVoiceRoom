//
//  ARTCKaraokeRoomController.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/6.
//

import UIKit

@objcMembers open class ARTCKaraokeRoomController: ARTCVoiceRoomEngine {
    
    private override init(_ roomInfo: ARTCVoiceRoomInfo) {
        super.init(roomInfo)
    }
    
    public convenience init(ktvRoomInfo: ARTCKaraokeRoomInfo) {
        self.init(ktvRoomInfo)
    }
    
    public func addObserver(karaoDelegate: ARTCKaraokeRoomControllerDelegate) {
        self.addObserver(delegate: karaoDelegate)
    }
    
    public func removeObserver(karaoDelegate: ARTCVoiceRoomEngineDelegate) {
        self.removeObserver(delegate: karaoDelegate)
    }
    
    /*=============================点歌===========================*/
    // 播放列表（当前播放中+未播放）
    public private(set) var musicPlayingList: [ARTCKaraokeRoomMusicInfo] = []
    
    public func updatePlayingList() {
        self.playingList.fetchPlayingMusicList {[weak self] musicInfoList, curr, error in
            self?.musicPlayingList = musicInfoList
            self?.curMusicPlaying = curr
            if let curMusicPlaying = self?.curMusicPlaying {
                self?.playingList.fetchJoinerList(musicInfo: curMusicPlaying, completed: { joinerIdList, error in
                    curMusicPlaying.joinSingUserIds = joinerIdList
                })
            }
        }
    }
    
    // 能否点歌，权限：麦上成员
    public var canAddMusic: Bool {
        get {
            if self.isJoinMic {
                return true
            }
            return false
        }
    }
    
    // 能否删歌，权限：房主+点歌成员
    public func checkCanRemoveMusic(songId: String) -> Bool {
        return false
    }
    
    // 点歌，歌曲下载完成后才能加入到播放列表
    public func addMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        if self.canAddMusic == false {
            completed?(ARTCRoomError.createError(.NoPermission, "必须上麦才能点歌哦"))
            return
        }
        
    }
    
    // 删除已点歌曲
    public func removeMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        if self.checkCanRemoveMusic(songId: musicInfo.songID) == false {
            completed?(ARTCRoomError.createError(.NoPermission, "仅能删除自己点的歌曲"))
            return
        }
        
        
    }
    
    // 能否置顶已点歌曲，权限：房主+点歌成员
    public func checkCanPinMusic(songId: String) -> Bool {
        return false
    }
    
    // 置顶已点歌曲
    public func pinMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        if self.checkCanPinMusic(songId: musicInfo.songID) == false {
            completed?(ARTCRoomError.createError(.NoPermission, "仅能置顶自己点的歌曲"))
            return
        }
        
        
    }
    
    /*=============================播放&演唱===========================*/
    // 当前播放歌曲
    public private(set) var curMusicPlaying: ARTCKaraokeRoomMusicInfo? = nil
    
    // 自己是否是主唱
    public var isLeadSinger: Bool {
        get {
            return false
        }
    }
    
    // 能否切歌/播放下一首，权限：房主+主唱
    public var canPlayNextMusic: Bool {
        get {
            return false
        }
    }
    
    // 切歌/播放下一首
    public func playNextMusic(completed: ARTCRoomCompleted?) {
        
    }
    
    // 能否暂停/继续播放，权限：主唱
    public var canPauseMusic: Bool {
        get {
            return false
        }
    }
    
    // 暂停播放
    public func pauseMusic(completed: ARTCRoomCompleted?) {
        
    }
    
    // 继续播放
    public func resumeMusic(completed: ARTCRoomCompleted?) {
        
    }
    
    // 查询歌曲是否可以伴奏
    public func checkMusicCanAccompany(songId: String) -> Bool {
        return false
    }
    
    // 能否切换播放模式，权限：主唱+伴唱
    public var canChangeMusicPlayMode: Bool {
        get {
            return false
        }
    }
    
    // 切换当前的播放列表播放模式：伴奏/原声
    public func changeMusicPlayMode(isAccompany: Bool, completed: ARTCRoomCompleted?) {
        
    }
    
    // 能否加入合唱，权限：麦上且不是主唱
    public var canJoinSinging: Bool {
        get {
            return false
        }
    }
    
    // 自己是否是合唱者
    public var isJoinSinger: Bool {
        get {
            return false
        }
    }
    
    // 加入合唱
    public func joinSinging(completed: ARTCRoomCompleted?) {
        
    }
    
    // 退出合唱
    public func leaveSinging(completed: ARTCRoomCompleted?) {
        
    }
    
    
    private lazy var ktvEngine: ARTCKaraokeRoomEngine = {
        let service = ARTCKaraokeRoomEngine()
        service.delegate = self
        self.rtcService.bridgeDelegate = self
        return service
    }()
    
    private lazy var playingList: ARTCKaraokeRoomMusicPlayingList = {
        let service = ARTCKaraokeRoomMusicPlayingList(self.me ,self.roomInfo as! ARTCKaraokeRoomInfo, (self.roomService as? ARTCRoomServiceImpl)!.roomAppServer as! ARTCKaraokeRoomAppServer)
        return service
    }()
}


extension ARTCKaraokeRoomController: ARTCRoomRTCServiceBridgeDelegate {
    
    public func onSetupRtcEngine(rtcEngine: AnyObject?) {
        self.ktvEngine.setupRtcEngine(rtcEngine: rtcEngine)
    }
    
    public func onWillReleaseEngine() {
        self.ktvEngine.releaseRtcEngine()
    }
    
    public func onDataChannelMessage(uid: String, controlMsg: AnyObject) {
        self.ktvEngine.onDataChannelMessage(uid: uid, controlMsg: controlMsg)
    }
    
}

extension ARTCKaraokeRoomController: ARTCKaraokeRoomEngineDelegate {
    
    public func onMusicPrepareStateUpdate(state: ARTCKaraokeRoomMusicPrepareState) {
        
    }
    
    public func onMusicPlayStateUpdate(state: ARTCKaraokeRoomMusicPlayState) {
        
    }
    
    public func onMusicPlayProgressUpdate(millisecond: Int64) {
        
    }
    
    public func onSingerRoleUpdate(newRole: ARTCKaraokeRoomSingerRole, oldRole: ARTCKaraokeRoomSingerRole) {
        
    }
}
