//
//  ARTCRoomServiceInterface.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit

@objc public protocol ARTCRoomServiceInterface: ARTCRoomMessageInterface {
    
    @objc optional func getRoomList(user: ARTCRoomUser, pageNum: Int, pageSize: Int, completed: @escaping (_ roomDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void)
    @objc optional func getRoomDetail(roomId: String, user: ARTCRoomUser, completed: @escaping (_ roomData: [AnyHashable: Any]?, _ error: NSError?) -> Void)
    
    
    @objc func fetchRTCAuthToken(room: ARTCRoomInfo, user: ARTCRoomUser, completed: @escaping (_ token: String?, _ timestamp: Int64, _ error: NSError?) -> Void)
    @objc func createRoom(roomId: String?, roomName: String?, user: ARTCRoomUser, completed: @escaping (_ roomData: [AnyHashable: Any]?, _ error: NSError?) -> Void)
    @objc func dismissRoom(room: ARTCRoomInfo, user: ARTCRoomUser, completed: @escaping ARTCRoomCompleted)

    @objc func getMicList(room: ARTCRoomInfo, user: ARTCRoomUser, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void)
    @objc func requestMic(room: ARTCRoomInfo, user: ARTCRoomUser, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void)
    @objc func agreeRequestMic(completed: @escaping ARTCRoomCompleted)
    @objc func rejectRequestMic(completed: @escaping ARTCRoomCompleted)
    @objc func leaveMic(room: ARTCRoomInfo, user: ARTCRoomUser, index: Int32, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void)
    @objc func kickOutMic(completed: @escaping ARTCRoomCompleted)

    
}
