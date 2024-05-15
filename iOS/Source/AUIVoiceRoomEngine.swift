//
//  AUIVoiceRoomEngine.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/4.
//

import UIKit
import AUIRoomCore

// 房间管理
extension ARTCVoiceRoomEngine {
    
    // 创建房间
    public static func createVoiceRoom(roomName: String, completed: @escaping (_ roomInfo: ARTCVoiceRoomInfo?, _ error: NSError?)->Void) {
        
        guard let currentUser = ARTCRoomService.currrentUser else {
            completed(nil, ARTCRoomError.createError(.Common, "请先设置登录用户"))
            return
        }
        
        AUIVoiceRoomManager.shared.getRoomServiceInterface().createRoom(roomId: nil, roomName: roomName, user: currentUser) { roomData, error in
            let roomInfo = ARTCVoiceRoomInfo(data: roomData)
            completed(roomInfo, error)
        }
        
    }
    
    // 获取房间列表
    public static func getVoiceRoomList(pageNum: Int, pageSize: Int, completed: @escaping (_ roomInfoList: [ARTCVoiceRoomInfo], _ error: NSError?)->Void) {
        
        AUIVoiceRoomManager.shared.getRoomServiceInterface().getRoomList!(user: ARTCRoomService.currrentUser ?? ARTCRoomUser(""), pageNum: pageNum, pageSize: pageSize) { roomDataList, error in
            var array = [ARTCVoiceRoomInfo]()
            roomDataList?.forEach({ roomData in
                let roomInfo = ARTCVoiceRoomInfo(data: roomData)
                if let roomInfo = roomInfo {
                    array.append(roomInfo)
                }
            })
            completed(array, error)
        }
    }
    
    // 获取房间详情
    public static func getVoiceRoomDetail(roomId: String, completed: @escaping (_ roomInfo: ARTCVoiceRoomInfo?, _ error: NSError?)->Void) {
        
        AUIVoiceRoomManager.shared.getRoomServiceInterface().getRoomDetail!(roomId: roomId, user: ARTCRoomService.currrentUser ?? ARTCRoomUser("")) { roomData, error in
            let roomInfo = ARTCVoiceRoomInfo(data: roomData)
            completed(roomInfo, error)
        }
        
    }
}
