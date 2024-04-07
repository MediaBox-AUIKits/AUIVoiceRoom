//
//  AUIRoomCommon.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit


@objc public enum AUIRoomSceneType: Int {
    case VoiceRoom
    case KTV
    case Call  // 暂不支持
}

@objcMembers public class AUIRoomConfig: NSObject {
    
    public var appId = "你的appID"
    public var gslb = "https://gw.rtn.aliyuncs.com"
    public var roomId = ""
    public var timestamp: Int64 = 0
    public var token = ""
    public var dimensions = CGSize(width: 360, height: 640)
    public var frameRate = 15
}

@objc public enum AUIRoomCameraType: Int {
    case Invalid = -1
    case Back
    case Front
}

@objc public enum AUIRoomAudioOutputType: Int {
    case Invalid = -1
    case Speaker
    case Headset
}

public enum AUIRoomNetworkState: String {
    case Good
    case Poor
    case Bad
    case Unknow
}

@objc public enum AUIRoomMessageType: Int {
    
    case DismissRoom = 21001
    
    case JoinedMic = 21101
    case LeavedMic
    case KickoutMic
    
    case SwitchCamera = 21201
    case SwitchMicrophone
    
    case TextMessage = 21301
}

public typealias AUIRoomCompleted = (_ error: NSError?)->Void



@objc public enum AUIRoomErrorCode: Int {
    case Common = -1
    case JoinedMicErrorForAlreadyJoined = 30000  // 已经上麦了
    case JoinedMicErrorForNotIndex = 30001       // 没有麦位了
}

@objcMembers public class AUIRoomError: NSObject {
    
    public static func createError(_ code: AUIRoomErrorCode, _ message: String?) -> NSError {
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
    
    public static func getErrorCode(error: NSError?) -> AUIRoomErrorCode? {
        guard let error = error else { return nil }
        return AUIRoomErrorCode.init(rawValue: error.code)
    }
}

extension NSError {
    
    @objc public var auiMessage: String {
        return AUIRoomError.getErrorMessage(error: self)
    }
}

extension Dictionary {
    
    public var room_jsonString: String {
        do {
            let stringData = try JSONSerialization.data(withJSONObject: self as NSDictionary, options: JSONSerialization.WritingOptions.prettyPrinted)
            if let string = String(data: stringData, encoding: String.Encoding.utf8){
                return string
            }
        } catch _ {
            
        }
        return "{}"
    }
}
