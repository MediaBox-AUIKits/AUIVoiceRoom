//
//  AUIRoomUser.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit

@objc public enum AUIRoomInfoStatus: Int {
    case Started = 1
    case Ended
}

@objcMembers open class AUIRoomInfo: NSObject {
    
    public init(_ roomId: String, _ anchor: AUIRoomUser) {
        self.roomId = roomId
        self.roomCode = 0
        self.anchor = anchor
        self.chatId = roomId
    }
    
    public init?(data: [AnyHashable: Any]?) {
        guard let data = data else { return nil }
        
        let roomId = data["id"] as? String
        guard let roomId = roomId else { return nil }
                
        let anchor = AUIRoomUser(data["anchor_id"] as? String ?? "")
        anchor.userNick = data["anchor_nick"] as? String ?? ""
        
        self.roomId = roomId
        self.roomCode = data["show_code"] as? Int ?? 0
        self.roomName = data["title"] as? String ?? ""
        self.chatId = data["chat_id"] as? String ?? roomId
        self.anchor = anchor
        self.status = AUIRoomInfoStatus(rawValue: data["status"] as? Int ?? 0) ?? .Started
        
        if let metrics = data["metrics"] as? [String : Any] {
            self.onlineCount = metrics["online_count"] as? Int ?? 0
        }
        
        let extendsString = data["extends"] as? String
        if let extendsString = extendsString {
            let obj = (try? JSONSerialization.jsonObject(with: extendsString.data(using: .utf8)!, options: .allowFragments)) as? [String : Any]
            self.extends = obj
            anchor.userAvatar = obj?["anchor_avatar"] as? String ?? ""
        }
    }
    
    public let roomId: String
    public let roomCode: Int
    public let anchor: AUIRoomUser
    open var roomName: String?
    open var status: AUIRoomInfoStatus = .Started
    open var onlineCount: Int = 0
    public let chatId: String
    open var extends: [String: Any]?
    
    open func toData() -> [AnyHashable: Any] {
        return [
            "id": self.roomId,
            "show_code": self.roomCode,
            "title": self.roomName ?? "",
            "status": self.status,
            "chat_id": self.chatId,
            "anchor_id": self.anchor.userId,
            "anchor_nick": self.anchor.userNick,
            "metrics":[
                "online_count": self.onlineCount
            ],
            "extends": self.extends?.room_jsonString ?? "{}",
        ]
    }
}

@objcMembers open class AUIRoomMicInfo: NSObject {
    
    public init(_ index: Int32) {
        self.index = index
    }
    
    public init?(data: [AnyHashable: Any]?) {
        guard let data = data else { return nil }
        
        let index = data["index"] as? Int32
        guard let index = index else { return nil }
        self.index = index

        if data["joined"] as? Bool == true {
            let userId = data["user_id"] as? String ?? ""
            let user = AUIRoomUser(userId)
            self.user = user
        }
        
        let extendsString = data["extends"] as? String
        if let extendsString = extendsString {
            let obj = (try? JSONSerialization.jsonObject(with: extendsString.data(using: .utf8)!, options: .allowFragments)) as? [String : Any]
            self.extends = obj
            self.user?.userNick = obj?["user_nick"] as? String ?? ""
            self.user?.userAvatar = obj?["user_avatar"] as? String ?? ""
        }
    }
    
    open func toData() -> [AnyHashable: Any] {
        return [
            "index": self.index,
            "user_id": self.user?.userId ?? "",
            "joined": self.isJoin,
            "extends": self.extends?.room_jsonString ?? "{}",
        ]
    }
    
    public let index: Int32
    open var user: AUIRoomUser? = nil
    open var extends: [String: Any]?

    open var isJoin: Bool {
        return self.user != nil
    }
}
