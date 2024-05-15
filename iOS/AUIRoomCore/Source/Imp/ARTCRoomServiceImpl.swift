//
//  ARTCRoomServiceImpl.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit
import AUIMessage

@objcMembers public class ARTCRoomServiceImpl: NSObject, ARTCRoomServiceInterface {
    
    public init(_ roomAppServer: ARTCRoomAppServer) {
        self.roomAppServer = roomAppServer
    }
    
    public let roomAppServer: ARTCRoomAppServer
    
    public func fetchRTCAuthToken(room: ARTCRoomInfo, user: ARTCRoomUser, completed: @escaping (String?, Int64, NSError?) -> Void) {
        self.roomAppServer.fetchRTCAuthToken(uid: user.userId, roomId: room.roomId, completed: completed)
        
    }
    
    public func createRoom(roomId: String?, roomName: String?, user: ARTCRoomUser, completed: @escaping ([AnyHashable: Any]?, NSError?) -> Void) {
        self.roomAppServer.createRoom(title: roomName, uid: user.userId, nick: user.getFinalNick(), extends: ["anchor_avatar": user.userAvatar], completed: completed)
    }
    
    public func dismissRoom(room: ARTCRoomInfo, user: ARTCRoomUser, completed: @escaping ARTCRoomCompleted) {
        self.roomAppServer.dismissRoom(uid: user.userId, roomId: room.roomId, completed: completed)
    }
    
    public func getRoomList(user: ARTCRoomUser, pageNum: Int, pageSize: Int, completed: @escaping ([[AnyHashable: Any]]?, NSError?) -> Void) {
        self.roomAppServer.getRoomList(uid: user.userId, pageNum: pageNum, pageSize: pageSize, completed: completed)
    }
    
    public func getRoomDetail(roomId: String, user: ARTCRoomUser, completed: @escaping ([AnyHashable: Any]?, NSError?) -> Void) {
        self.roomAppServer.getRoomDetail(uid: user.userId, roomId: roomId, completed: completed)
    }
    
    public func getMicList(room: ARTCRoomInfo, user: ARTCRoomUser, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        self.roomAppServer.getMicList(uid: user.userId, roomId: room.roomId, completed: completed)
    }
    
    public func requestMic(room: ARTCRoomInfo, user: ARTCRoomUser, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        let extends = ["user_nick": user.userNick, "user_avatar": user.userAvatar]
        self.roomAppServer.joinMic(uid: user.userId, roomId: room.roomId, index: 0, extends: extends.artcJsonString, completed: completed)
    }
    
    public func agreeRequestMic(completed: @escaping (NSError?) -> Void) {
        // TODO: 待实现
    }
    
    public func rejectRequestMic(completed: @escaping (NSError?) -> Void) {
        // TODO: 待实现
    }
    
    public func leaveMic(room: ARTCRoomInfo, user: ARTCRoomUser, index: Int32, completed: @escaping (_ micDataList: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        self.roomAppServer.leaveMic(uid: user.userId, roomId: room.roomId, index: index, completed: completed)
    }
    
    public func kickOutMic(completed: @escaping (NSError?) -> Void) {
        // TODO: 待实现
    }
    
    public weak var receivedMessageDelegate: ARTCRoomMessageDelegate? = nil
    
    public func sendMessage(model: ARTCRoomMessageSendModel, completed: ARTCRoomCompleted?) {
        
        if model.type == .JoinRoom {
            let req = AUIMessageJoinGroupRequest()
            req.groupId = model.chatId!
            ARTCRoomMessageService.getMessageService().joinGroup(req) { res, error in
                if let res = res {
                    self.onGroup(res.groupId, onlineCountChanged: res.onlineCount)
                }
                completed?(error as NSError?)
            }
            ARTCRoomMessageService.getMessageService().getListenerObserver().addListener(self)
            return
        }
        
        if model.type == .LeaveRoom {
            let req = AUIMessageLeaveGroupRequest()
            req.groupId = model.chatId!
            ARTCRoomMessageService.getMessageService().leaveGroup(req) { error in
                completed?(error as NSError?)
            }
            ARTCRoomMessageService.getMessageService().getListenerObserver().removeListener(self)
            return
        }
        
        if model.receivedUserId != nil {
            // 单发
            let req = AUIMessageSendMessageToGroupUserRequest()
            req.groupId = model.chatId
            req.msgType = model.type.rawValue
            req.receiverId = model.receivedUserId!
            req.skipAudit = model.isCommand
            if model.data != nil {
                req.data = AUIMessageDefaultData(data: model.data!)
            }
            ARTCRoomMessageService.getMessageService().sendMessage(toGroupUser: req) { rsp, err in
                completed?(err as NSError?)
            }
        }
        else {
            // 组内群发
            let req = AUIMessageSendMessageToGroupRequest()
            req.groupId = model.chatId!
            req.msgType = model.type.rawValue
            req.skipAudit = model.isCommand
            req.skipMuteCheck = model.isCommand
            req.storage = !model.isCommand
            if model.data != nil {
                req.data = AUIMessageDefaultData(data: model.data!)
            }
            ARTCRoomMessageService.getMessageService().sendMessage(toGroup: req) { rsp, err in
                completed?(err as NSError?)
            }
        }
    }
}

extension ARTCRoomServiceImpl: AUIMessageListenerProtocol {
    
    public func onMessageReceived(_ model: AUIMessageModel) {

        if let msgType = ARTCRoomMessageType(rawValue: model.msgType) {
            let receivedModel = ARTCRoomMessageReceiveModel()
            receivedModel.chatId = model.groupId
            receivedModel.sender =  model.sender != nil ?  ARTCRoomUser(user: model.sender!) : nil
            receivedModel.type = msgType
            receivedModel.data = model.data
            self.receivedMessageDelegate?.onReceivedMessage(model: receivedModel)
        }
    }
    
    public func onJoinGroup(_ model: AUIMessageModel) {
        let receivedModel = ARTCRoomMessageReceiveModel()
        receivedModel.chatId = model.groupId
        receivedModel.sender =  model.sender != nil ?  ARTCRoomUser(user: model.sender!) : nil
        receivedModel.type = .JoinRoom
        receivedModel.data = model.data
        self.receivedMessageDelegate?.onReceivedMessage(model: receivedModel)
    }
    
    public func onLeaveGroup(_ model: AUIMessageModel) {
        let receivedModel = ARTCRoomMessageReceiveModel()
        receivedModel.chatId = model.groupId
        receivedModel.sender =  model.sender != nil ?  ARTCRoomUser(user: model.sender!) : nil
        receivedModel.type = .LeaveRoom
        receivedModel.data = model.data
        self.receivedMessageDelegate?.onReceivedMessage(model: receivedModel)
    }
    
    public func onExitedGroup(_ groupId: String) {
        let receivedModel = ARTCRoomMessageReceiveModel()
        receivedModel.chatId = groupId
        receivedModel.type = .KickoutRoom
        self.receivedMessageDelegate?.onReceivedMessage(model: receivedModel)
    }
    
    public func onGroup(_ groupId: String, onlineCountChanged onlineCount: Int) {
        let receivedModel = ARTCRoomMessageReceiveModel()
        receivedModel.chatId = groupId
        receivedModel.type = .OnlineCountChanged
        receivedModel.data = ["onlineCount": onlineCount]
        self.receivedMessageDelegate?.onReceivedMessage(model: receivedModel)
    }
}
