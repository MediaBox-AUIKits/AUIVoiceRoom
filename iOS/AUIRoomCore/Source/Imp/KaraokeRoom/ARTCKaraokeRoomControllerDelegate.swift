//
//  ARTCKaraokeRoomControllerDelegate.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/11.
//

import UIKit

@objc public enum ARTCKaraokeRoomMusicPlayCompletedReason: Int {
    case PlayEnd = 0 // 播放完成
    case PlayFailed  // 播放出错
    case PlaySkip    // 切歌
}

@objc public protocol ARTCKaraokeRoomControllerDelegate: ARTCVoiceRoomEngineDelegate {
    
    // 播放列表（当前播放中+未播放）更新：添加、删除、置顶、切歌、播放结束
    @objc optional func onMusicPlayingListUpdated()
    
    // 当前歌曲结束播放，原因：0：完成播放，1：播放
    @objc optional func onMusicPlayCompleted(musicInfo: ARTCKaraokeRoomMusicInfo, reason: ARTCKaraokeRoomMusicPlayCompletedReason)
    
    // 即将播放下一首歌曲
    @objc optional func onMusicWillPlayNext(musicInfo: ARTCKaraokeRoomMusicInfo)
    
    // 播放进度更新
    @objc optional func onMusicPlayProgressChanged(musicInfo: ARTCKaraokeRoomMusicInfo, millisecond: Int64)

    // 播放状态更新，只针对伴唱成员
    @objc optional func onMusicPlayStateChanged(musicInfo: ARTCKaraokeRoomMusicInfo, millisecond: Int64)
    
    // 角色变化
    @objc optional func onMusicSingerRoleChanged()

}

extension ARTCKaraokeRoomController {
    
}
