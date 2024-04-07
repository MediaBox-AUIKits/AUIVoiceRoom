//
//  AUIRoomService.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit
import AUIMessage

let AppServerDomain = "你的AppServer域名"

@objcMembers open class AUIRoomService: NSObject {
    
    
    public static var currrentUser: AUIRoomUser? = nil {
        willSet {
            let changedUser = currrentUser != nil && currrentUser!.userId != newValue?.userId
            if changedUser {
                // 切换用户后，需要恢复到初始状态
                self.logout(completed: nil)
            }
        }
    }
    public static var currentRoom: AUIRoomInfo? = nil
    
    public static var isInRoom: Bool {
        return self.currentRoom != nil
    }
    
    public static func getMessageService() -> AUIMessageServiceProtocol {
        return AUIMessageServiceFactory.getMessageService()
    }
    
    private static let interface: AUIRoomServiceInterface = AUIRoomServiceImpl(AppServerDomain)
    public static func getInterface() -> AUIRoomServiceInterface {
        return self.interface
    }
}

extension AUIRoomService {
    
    public static func login(completed: AUIRoomCompleted?) {
        guard let currrentUser = self.currrentUser else {
            completed?(AUIRoomError.createError(.Common, "current user is empty"))
            return
        }
        self.getInterface().fetchIMLoginToken(user: currrentUser) { tokenData, error in
            guard error == nil else {
                completed?(error as NSError?)
                return
            }
            guard let tokenData = tokenData else {
                completed?(AUIRoomError.createError(.Common, "lost token data"))
                return
            }
            
            let messageConfig = AUIMessageConfig()
            messageConfig.tokenData = tokenData
            self.getMessageService().setConfig(messageConfig);
            self.getMessageService().login(currrentUser, callback:{ error in
                completed?(error as NSError?)
            })
        }
    }
    
    public static func logout(completed: AUIRoomCompleted?) {
        self.getMessageService().logout({ error in
            completed?(error as NSError?)
        })
    }
}


@objc public protocol AUIRoomServiceInterface {
    
    @objc func fetchIMLoginToken(user: AUIRoomUser, completed:@escaping (_ tokenData: [String : Any]?, _ error: Error?) -> Void)
    @objc func fetchRTCAuthToken(room: AUIRoomInfo, user: AUIRoomUser, completed: @escaping (_ token: String?, _ timestamp: Int64, _ error: Error?) -> Void)

    @objc func createRoom(roomId: String?, roomName: String?, user: AUIRoomUser, completed: @escaping (_ roomData: [AnyHashable: Any]?, _ error: NSError?) -> Void)
    @objc func dismissRoom(room: AUIRoomInfo, user: AUIRoomUser, completed: @escaping AUIRoomCompleted)
    @objc func getRoomList(user: AUIRoomUser, pageNum: Int, pageSize: Int, completed: @escaping (_ roomDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void)
    @objc func getRoomDetail(roomId: String, user: AUIRoomUser, completed: @escaping (_ roomData: [AnyHashable: Any]?, _ error: NSError?) -> Void)
    
    @objc func getMicList(room: AUIRoomInfo, user: AUIRoomUser, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void)
    @objc func requestMic(room: AUIRoomInfo, user: AUIRoomUser, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void)
    @objc func agreeRequestMic(completed: @escaping AUIRoomCompleted)
    @objc func rejectRequestMic(completed: @escaping AUIRoomCompleted)
    @objc func leaveMic(room: AUIRoomInfo, user: AUIRoomUser, index: Int32, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void)
    @objc func kickOutMic(completed: @escaping AUIRoomCompleted)

}

@objcMembers public class AUIRoomServiceImpl: NSObject, AUIRoomServiceInterface {
    
    init(_ serverDomain: String) {
        self.roomAppServer = AUIRoomAppServer(serverDomain)
    }
    
    public let roomAppServer: AUIRoomAppServer
    
    public func fetchIMLoginToken(user: AUIRoomUser, completed: @escaping ([String : Any]?, Error?) -> Void) {
        self.roomAppServer.fetchIMLoginToken(uid: user.userId, completed: completed)
    }
    
    public func fetchRTCAuthToken(room: AUIRoomInfo, user: AUIRoomUser, completed: @escaping (String?, Int64, Error?) -> Void) {
        self.roomAppServer.fetchRTCAuthToken(uid: user.userId, roomId: room.roomId, completed: completed)
        
    }
    
    public func createRoom(roomId: String?, roomName: String?, user: AUIRoomUser, completed: @escaping ([AnyHashable: Any]?, NSError?) -> Void) {
        self.roomAppServer.createRoom(title: roomName, uid: user.userId, nick: user.getFinalNick(), extends: ["anchor_avatar": user.userAvatar], completed: completed)
    }
    
    public func dismissRoom(room: AUIRoomInfo, user: AUIRoomUser, completed: @escaping AUIRoomCompleted) {
        self.roomAppServer.dismissRoom(uid: user.userId, roomId: room.roomId, completed: completed)
    }
    
    public func getRoomList(user: AUIRoomUser, pageNum: Int, pageSize: Int, completed: @escaping ([[AnyHashable: Any]]?, NSError?) -> Void) {
        self.roomAppServer.getRoomList(uid: user.userId, pageNum: pageNum, pageSize: pageSize, completed: completed)
    }
    
    public func getRoomDetail(roomId: String, user: AUIRoomUser, completed: @escaping ([AnyHashable: Any]?, NSError?) -> Void) {
        self.roomAppServer.getRoomDetail(uid: user.userId, roomId: roomId, completed: completed)
    }
    
    public func getMicList(room: AUIRoomInfo, user: AUIRoomUser, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        self.roomAppServer.getMicList(uid: user.userId, roomId: room.roomId, completed: completed)
    }
    
    public func requestMic(room: AUIRoomInfo, user: AUIRoomUser, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        let extends = ["user_nick": user.userNick, "user_avatar": user.userAvatar]
        self.roomAppServer.joinMic(uid: user.userId, roomId: room.roomId, index: 0, extends: extends.room_jsonString, completed: completed)
    }
    
    public func agreeRequestMic(completed: @escaping (NSError?) -> Void) {
        // TODO: 待实现
    }
    
    public func rejectRequestMic(completed: @escaping (NSError?) -> Void) {
        // TODO: 待实现
    }
    
    public func leaveMic(room: AUIRoomInfo, user: AUIRoomUser, index: Int32, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        self.roomAppServer.leaveMic(uid: user.userId, roomId: room.roomId, index: index, completed: completed)
    }
    
    public func kickOutMic(completed: @escaping (NSError?) -> Void) {
        // TODO: 待实现
    }
}
