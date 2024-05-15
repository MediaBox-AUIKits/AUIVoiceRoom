//
//  AUIRoomCommon.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit


public typealias ARTCRoomCompleted = (_ error: NSError?) -> Void


extension Dictionary {
    
    public var artcJsonString: String {
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
