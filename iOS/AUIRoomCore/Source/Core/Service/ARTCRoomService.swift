//
//  ARTCRoomService.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit

@objcMembers open class ARTCRoomService: NSObject {
    
    
    public static var currrentUser: ARTCRoomUser? = nil
    public static var currentRoom: ARTCRoomInfo? = nil
    
    public static var isInRoom: Bool {
        return self.currentRoom != nil
    }
}
