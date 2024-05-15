//
//  ARTCKaraokeRoomMusicPlayingList.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/6.
//

import UIKit

/*=============================播放列表===========================*/
@objcMembers open class ARTCKaraokeRoomMusicPlayingList: NSObject {
    
    init(_ userInfo: ARTCRoomUser, _ roomInfo: ARTCKaraokeRoomInfo, _ roomAppServer: ARTCKaraokeRoomAppServer) {
        self.userInfo = userInfo
        self.roomInfo = roomInfo
        self.roomAppServer = roomAppServer
    }
    
    public let roomInfo: ARTCKaraokeRoomInfo
    public let userInfo: ARTCRoomUser
    public let roomAppServer: ARTCKaraokeRoomAppServer
    
    // 获取点歌列表
    public func fetchPlayingMusicList(completed: @escaping (_ musicInfoList: [ARTCKaraokeRoomMusicInfo], _ currPlaying: ARTCKaraokeRoomMusicInfo?, _ error: NSError?) -> Void) {
        
        self.roomAppServer.getMusicList(uid: self.userInfo.userId, roomId: self.roomInfo.roomId) { songListData, error in
            var array: [ARTCKaraokeRoomMusicInfo] = []
            var curr: ARTCKaraokeRoomMusicInfo? = nil
            songListData?.forEach({ songData in
                let musicInfo = ARTCKaraokeRoomMusicInfo.create(playingData: songData)
                if let musicInfo = musicInfo {
                    array.append(musicInfo)
                }
                if curr == nil && songData["status"] as? Int == 2 {
                    curr = musicInfo
                }
            })
            completed(array, curr, error)
        }
    }
    
    // 点歌
    public func addMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        
        let micSeatInfo = self.roomInfo.getMicSeatInfo(uid: self.userInfo.userId)
        guard let micSeatInfo = micSeatInfo else {
            completed?(ARTCRoomError.createError(.Common, "please join room first"))
            return
        }
        
        self.roomAppServer.addMusic(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songId: musicInfo.songID, userExtends: micSeatInfo.toData().artcJsonString, songExtends: musicInfo.toPlayingExtentData().artcJsonString) { error in
            completed?(error)
        }
    }
    
    // 从列表里删除谁的已点歌曲
    public func removeMusic(playingMusicList: [ARTCKaraokeRoomMusicInfo], who: String, completed: ((_ removedMusicList: [ARTCKaraokeRoomMusicInfo], _ error: NSError?) -> Void)?) {
        
        var removedMusicList: [ARTCKaraokeRoomMusicInfo] = []
        var songIds: [String] = []
        playingMusicList.forEach { pmi in
            if pmi.singUserId == who {
                removedMusicList.append(pmi)
                songIds.append(pmi.songID)
            }
        }
        if songIds.isEmpty {
            completed?(removedMusicList, nil)
            return
        }
        
        self.roomAppServer.removeMusic(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songIds: songIds, singUserId: who) { error in
            completed?(removedMusicList, error)
        }
    }

    // 置顶已点歌曲
    public func pinMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        
        self.roomAppServer.pinMusic(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songId: musicInfo.songID, singUserId: musicInfo.singUserId) { error in
            completed?(error)
        }
    }
    
    // 播放
    public func playMusic(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ((_ nextSongId: String?, _ error: NSError?) -> Void)?) {
        
        self.roomAppServer.playMusic(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songId: musicInfo.songID, singUserId: musicInfo.singUserId) { nextSongId, error in
            completed?(nextSongId, error)
        }
        
    }

    // 加入合唱
    public func joinSinging(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        
        self.roomAppServer.joinSinging(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songId: musicInfo.songID) { error in
            completed?(error)
        }
    }
    
    // 退出合唱
    public func leaveSinging(musicInfo: ARTCKaraokeRoomMusicInfo, completed: ARTCRoomCompleted?) {
        self.roomAppServer.leaveSinging(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songId: musicInfo.songID) { error in
            completed?(error)
        }
    }

    // 获取歌曲的合唱者
    public func fetchJoinerList(musicInfo: ARTCKaraokeRoomMusicInfo, completed: @escaping (_ joinerIdList: [String], _ error: NSError?) -> Void) {
        
        self.roomAppServer.getJoinerList(uid: self.userInfo.userId, roomId: self.roomInfo.roomId, songId: musicInfo.songID) { joinerDataList, error in
            var joinerIdList: [String] = []
            joinerDataList?.forEach({ data in
                let uid = data["user_id"] as? String
                if let uid = uid {
                    joinerIdList.append(uid)
                }
            })
            completed(joinerIdList, error)
        }
        
    }
}

extension ARTCKaraokeRoomMusicInfo {
    
    open func toPlayingExtentData() -> [AnyHashable: Any] {
        return [
            "id": self.songID,
            "song_name": self.songName,
            "singer_name": self.singerName,
            "album_img": self.albumImg,
            "remote_url": self.remoteUrl,
        ]
    }
    
    open func parseExtentData(playingData: [AnyHashable: Any]?) {
        let songId = playingData?["id"] as? String
        if self.songID == songId {
            self.songName = playingData?["song_name"] as? String ?? ""
            self.singerName = playingData?["singer_name"] as? String ?? ""
            self.albumImg = playingData?["album_img"] as? String ?? ""
            self.remoteUrl = playingData?["remote_url"] as? String ?? ""
            self.localPath = ARTCKaraokeRoomMusicLibrary.shared.getLocalPath(songId: self.songID) ?? ""
        }
    }
    
    public static func create(playingData: [AnyHashable: Any]) -> ARTCKaraokeRoomMusicInfo? {
        let songId = playingData["song_id"] as? String
        if let songId = songId {
            let musicInfo = ARTCKaraokeRoomMusicInfo(songID: songId)
            musicInfo.singUserId = playingData["user_id"] as? String ?? ""
            
            if let song_extends = playingData["song_extends"] as? String {
                let playingData = (try? JSONSerialization.jsonObject(with: song_extends.data(using: .utf8)!, options: .allowFragments)) as? [String : Any]
                musicInfo.parseExtentData(playingData: playingData)
            }
            return musicInfo
        }
        return nil
    }
}

extension ARTCRoomMicInfo {
    
}
