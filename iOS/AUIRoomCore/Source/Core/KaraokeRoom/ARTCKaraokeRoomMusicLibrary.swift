//
//  ARTCKaraokeRoomMusicLibrary.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/6.
//

import UIKit


/*=============================曲库===========================*/
@objcMembers open class ARTCKaraokeRoomMusicLibrary: NSObject {
    
    public static let shared = ARTCKaraokeRoomMusicLibrary()

    // 榜单列表
    public var musicChartList: [ARTCKaraokeRoomMusicChartInfo] = []
    
    // 获取最新的榜单列表
    public func getMusicChartList(completed: ARTCRoomCompleted?) {
        
    }
    
    // 获取指定榜单下的音乐
    public func getMusicList(chartId: String, page: Int, pageSize: Int, completed: ARTCRoomCompleted?) {
        
    }
    
    // 获取指定歌曲id下的歌曲详情
    public func getMusicInfo(songId: String, completed: (_ musicInfo: ARTCKaraokeRoomMusicInfo?, _ error: NSError?) -> Void) {
        
    }
    
    // 搜索符合关键词的音乐
    public func searchMusic(keywork: String, completed: (_ musicList: [ARTCKaraokeRoomMusicInfo]?, _ error: NSError?) -> Void) {
        
    }
    
    // 下载歌曲
    public func downloadMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        
    }
    
    // 获取已经下载歌曲的本地路径
    public func getLocalPath(songId: String) -> String? {
        return nil
    }
    
    // 查询歌曲是完成下载
    public func checkMusicIsDownloaded(songId: String) -> Bool {
        return false
    }
    
    // 查询歌曲是否下载中
    public func checkMusicIsDownloading(songId: String) -> Bool {
        return false
    }
}


@objc public protocol ARTCKaraokeRoomMusicLibraryDelegate {
    
    // 歌曲下载完成
    @objc optional func onMusicDownloadCompleted(musicInfo: ARTCKaraokeRoomMusicInfo)
}
