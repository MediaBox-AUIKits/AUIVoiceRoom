//
//  ARTCRoomMessageInterface.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit

@objc public enum ARTCRoomMessageType: Int {
    
    case None = 0
    case JoinRoom = 10001
    case LeaveRoom
    case KickoutRoom
    case OnlineCountChanged
    
    case DismissRoom = 21001
    
    case JoinedMic = 21101
    case LeavedMic
    case KickoutMic
    
    case SwitchCamera = 21201
    case SwitchMicrophone
    
    case TextMessage = 21301
}

@objcMembers open class ARTCRoomMessageSendModel: NSObject {
    
    open var type: ARTCRoomMessageType = .None
    open var data: [AnyHashable: Any]? = nil
    open var isCommand: Bool = true
    open var receivedUserId: String? = nil  // 不为空时，1v1单点发送；为空时，群发
    open var chatId: String? = nil
}

@objcMembers open class ARTCRoomMessageReceiveModel: NSObject {
    open var chatId: String? = nil
    open var data: [AnyHashable: Any]? = nil
    open var type: ARTCRoomMessageType = .None
    open var sender: ARTCRoomUser? = nil

}

@objc public protocol ARTCRoomMessageDelegate {
    @objc func onReceivedMessage(model: ARTCRoomMessageReceiveModel)
}


@objc public protocol ARTCRoomMessageInterface {
    
    @objc func sendMessage(model: ARTCRoomMessageSendModel, completed: ARTCRoomCompleted?)

    @objc weak var receivedMessageDelegate: ARTCRoomMessageDelegate? { get set }

}

