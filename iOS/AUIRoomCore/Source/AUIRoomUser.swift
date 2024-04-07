//
//  AUIRoomUser.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit
import AUIMessage

@objcMembers open class AUIRoomUser: NSObject, AUIUserProtocol {
    
    public init(_ userId: String) {
        self.userId = userId
        self.userNick = self.userId
        self.userAvatar = ""
    }
    
    public init(user: AUIUserProtocol) {
        self.userId = user.userId
        self.userNick = user.userNick
        self.userAvatar = user.userAvatar
    }
    
    open private(set) var userId: String
    
    open var userNick: String
    
    open var userAvatar: String
    
    open func getFinalNick() -> String {
        if self.userNick.isEmpty {
            return self.userId
        }
        return self.userNick
    }
    
    open override func copy() -> Any {
        let roomUser = AUIRoomUser(self.userId)
        roomUser.userNick = self.userNick
        roomUser.userAvatar = self.userAvatar
        return roomUser
    }
    
    open func update(user: AUIRoomUser?, force: Bool = false) {
        if let user = user {
            if force {
                self.userId = user.userId
            }
            if user.userId == self.userId {
                self.userNick = user.userNick
                self.userAvatar = user.userAvatar
            }
        }
    }
    
}
