//
//  AUIVoiceRoomTheme.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/20.
//

import UIKit
import AUIFoundation

public class AUIVoiceRoomBundleImp: NSObject {
    public init(_ bundleName: String) {
        self.bundleName = bundleName
    }
    
    public let bundleName: String
    
    public func getImage(_ key: String?) -> UIImage? {
        guard let key = key else { return nil }
        return AVTheme.image(withNamed: key, withModule: self.bundleName)
    }
    
    public func getCommonImage(_ key: String?) -> UIImage? {
        guard let key = key else { return nil }
        return AVTheme.image(withCommonNamed: key, withModule: self.bundleName)
    }
    
    public func getString(_ key: String?) -> String? {
        guard let key = key else { return nil }
        return AVLocalization.string(withKey: key, withModule: self.bundleName)
    }
    
    public func getResourceFullPath(_ path: String) -> String {
        let final = Bundle.main.resourcePath
        if let final = final {
            return final + "/" + self.bundleName + ".bundle/" + path
        }
        return path
    }
}

public let AUIVoiceRoomBundle = AUIVoiceRoomBundleImp("AUIVoiceRoom")
