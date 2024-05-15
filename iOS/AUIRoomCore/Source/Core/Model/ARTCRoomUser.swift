//
//  ARTCRoomUser.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit

#if canImport(AUIMessage)
import AUIMessage
#else

@objc public protocol AUIUserProtocol {
    
    var userId: String { get }
    var userNick: String { get set }
    var userAvatar: String { get set }
}

#endif



@objcMembers open class ARTCRoomUser: NSObject, AUIUserProtocol {
    
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
        let roomUser = ARTCRoomUser(self.userId)
        roomUser.userNick = self.userNick
        roomUser.userAvatar = self.userAvatar
        return roomUser
    }
    
    open func update(user: ARTCRoomUser?, force: Bool = false) {
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
