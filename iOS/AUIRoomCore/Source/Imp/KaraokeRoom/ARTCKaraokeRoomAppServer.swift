//
//  ARTCKaraokeRoomAppServer.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit

@objcMembers open class ARTCKaraokeRoomAppServer: ARTCRoomAppServer {
    
    open override func pathPrefix() -> String {
        return "/api/ktv"
    }
    
    open func getMusicList(uid: String, roomId: String, completed: @escaping (_ songListData: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "room_id": roomId,
            "user_id": uid,
        ]
        
        self.request(path: "\(self.pathPrefix())/listSongs", body: body) { response, data, error in
            if error == nil {
                let songs = data?["songs"] as? [[AnyHashable: Any]]
                completed(songs, error)
            }
            else {
                completed(nil, error)
            }
        }
    }
    
    open func addMusic(uid: String, roomId: String, songId: String, userExtends: String, songExtends: String, completed: @escaping (_ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "room_id": roomId,
            "user_id": uid,
            "user_extends": userExtends,
            "song_id": songId,
            "song_extends": songExtends,
        ]

        self.request(path: "\(self.pathPrefix())/selectSong", body: body) { response, data, error in
            if error == nil {
                let success = data?["success"] as? Bool
                if success == true {
                    completed(nil)
                }
                else {
                    let reason = data?["reason"] as? Int
                    var code = ARTCRoomErrorCode.Common
                    if reason == 1 {
                        code = .MusicErrorForAddRepeat
                    }
                    let msg = data?["message"] as? String
                    completed(ARTCRoomError.createError(code, msg))
                }
            }
            else {
                completed(error)
            }
        }
    }
    
    open func removeMusic(uid: String, roomId: String, songIds: [String], singUserId: String, completed: @escaping (_ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "room_id": roomId,
            "operator": uid,
            "song_ids": songIds.joined(separator: ","),
            "user_id": singUserId,
        ]

        self.request(path: "\(self.pathPrefix())/deleteSong", body: body) { response, data, error in
            if error == nil {
                let success = data?["success"] as? Bool
                if success == true {
                    completed(nil)
                }
                else {
                    let reason = data?["reason"] as? Int
                    var code = ARTCRoomErrorCode.Common
                    if reason == 1 {
                        code = .MusicErrorForNotFound
                    }
                    else if reason == 2 {
                        code = .MusicErrorForNoPermission
                    }
                    let msg = data?["message"] as? String
                    completed(ARTCRoomError.createError(code, msg))
                }
            }
            else {
                completed(error)
            }
        }
    }
    
    
    open func pinMusic(uid: String, roomId: String, songId: String, singUserId: String, completed: @escaping (_ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "room_id": roomId,
            "operator": uid,
            "song_id": songId,
            "user_id": singUserId,
        ]

        self.request(path: "\(self.pathPrefix())/pinSong", body: body) { response, data, error in
            if error == nil {
                let success = data?["success"] as? Bool
                if success == true {
                    completed(nil)
                }
                else {
                    let reason = data?["reason"] as? Int
                    var code = ARTCRoomErrorCode.Common
                    if reason == 1 {
                        code = .MusicErrorForNotFound
                    }
                    else if reason == 2 {
                        code = .MusicErrorForNoPermission
                    }
                    let msg = data?["message"] as? String
                    completed(ARTCRoomError.createError(code, msg))
                }
            }
            else {
                completed(error)
            }
        }
    }
    
    // nextSongId: 下一个要播放的歌曲标识。可能为空，为空表示后面无播放歌曲：如果和你当前的列表的下一个不一致的情况下，需要你刷新列表
    open func playMusic(uid: String, roomId: String, songId: String, singUserId: String, completed: @escaping (_ nextSongId: String?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "room_id": roomId,
            "operator": uid,
            "song_id": songId,
            "user_id": singUserId,
        ]

        self.request(path: "\(self.pathPrefix())/playSong", body: body) { response, data, error in
            if error == nil {
                let nextSongId = data?["next_song_id"] as? String
                if let nextSongId = nextSongId {
                    completed(nextSongId, nil)
                }
                else {
                    let reason = data?["reason"] as? Int
                    var code = ARTCRoomErrorCode.Common
                    if reason == 1 {
                        code = .MusicErrorForNotFound
                    }
                    else if reason == 2 {
                        code = .MusicErrorForNotMatch
                    }
                    else if reason == 3 {
                        code = .MusicErrorForNoPermission
                    }
                    let msg = data?["message"] as? String
                    completed(nil, ARTCRoomError.createError(code, msg))
                }
            }
            else {
                completed(nil, error)
            }
        }
    }
    
    open func joinSinging(uid: String, roomId: String, songId: String, completed: @escaping (_ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "room_id": roomId,
            "user_id": uid,
            "song_id": songId,
        ]

        self.request(path: "\(self.pathPrefix())/joinInSinging", body: body) { response, data, error in
            if error == nil {
                let success = data?["success"] as? Bool
                if success == true {
                    completed(nil)
                }
                else {
                    let reason = data?["reason"] as? Int
                    var code = ARTCRoomErrorCode.Common
                    if reason == 1 {
                        code = .MusicErrorForNotFound
                    }
                    else if reason == 2 {
                        code = .MusicErrorForNotMatch
                    }
                    else if reason == 3 {
                        code = .MusicErrorForJoinRepeat
                    }
                    let msg = data?["message"] as? String
                    completed(ARTCRoomError.createError(code, msg))
                }
            }
            else {
                completed(error)
            }
        }
    }
    
    open func leaveSinging(uid: String, roomId: String, songId: String, completed: @escaping (_ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "room_id": roomId,
            "user_id": uid,
            "song_id": songId,
        ]

        self.request(path: "\(self.pathPrefix())/leaveSinging", body: body) { response, data, error in
            if error == nil {
                let success = data?["success"] as? Bool
                if success == true {
                    completed(nil)
                }
                else {
                    let reason = data?["reason"] as? Int
                    var code = ARTCRoomErrorCode.Common
                    if reason == 1 {
                        code = .MusicErrorForNotFound
                    }
                    else if reason == 2 {
                        code = .MusicErrorForNotMatch
                    }
                    else if reason == 3 {
                        code = .MusicErrorForNotJoinSinging
                    }
                    let msg = data?["message"] as? String
                    completed(ARTCRoomError.createError(code, msg))
                }
            }
            else {
                completed(error)
            }
        }
    }
    
    open func getJoinerList(uid: String, roomId: String, songId: String, completed: @escaping (_ joinerDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "room_id": roomId,
            "user_id": uid,
            "song_id": songId,
        ]
        
        self.request(path: "\(self.pathPrefix())/getSinging", body: body) { response, data, error in
            if error == nil {
                let members = data?["members"] as? [[AnyHashable: Any]]
                completed(members, error)
            }
            else {
                completed(nil, error)
            }
        }
    }
    
}
