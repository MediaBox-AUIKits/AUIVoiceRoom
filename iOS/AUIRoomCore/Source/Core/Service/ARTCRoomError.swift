//
//  ARTCRoomError.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit


@objc public enum ARTCRoomErrorCode: Int {
    case Common = -1
    case NoPermission = -2
    case JoinedMicErrorForAlreadyJoined = 30000  // 已经上麦了
    case JoinedMicErrorForNotIndex = 30001       // 没有麦位了
    
    case MusicErrorForNotFound = 40000      // 无此歌曲
    case MusicErrorForNoPermission          // 歌曲无操作权限
    case MusicErrorForNotMatch              // 歌曲Id不匹配
    case MusicErrorForAddRepeat             // 重复点歌了
    case MusicErrorForJoinRepeat            // 重复加入合唱了
    case MusicErrorForNotJoinSinging        // 未加入合唱
}

@objcMembers public class ARTCRoomError: NSObject {
    
    public static func createError(_ code: ARTCRoomErrorCode, _ message: String?) -> NSError {
        return self.createError(code: code.rawValue, message: message)
    }
    
    public static func createError(code: Int, message: String?) -> NSError {
        let error = NSError(domain: "aui.room", code: code, userInfo: [NSLocalizedDescriptionKey:message ?? "unknown"])
        return error
    }
    
    public static func getErrorMessage(error: NSError?) -> String {
        guard let error = error else { return "" }
        let ret = error.userInfo[NSLocalizedDescriptionKey] as? String
        return "(\(error.code))\(ret ?? "")"
    }
    
    public static func getErrorCode(error: NSError?) -> ARTCRoomErrorCode? {
        guard let error = error else { return nil }
        return ARTCRoomErrorCode.init(rawValue: error.code)
    }
}

extension NSError {
    
    @objc public var artcMessage: String {
        return ARTCRoomError.getErrorMessage(error: self)
    }
}
