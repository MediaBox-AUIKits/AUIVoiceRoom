//
//  ARTCRoomMessageService.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit
import AUIMessage


@objcMembers open class ARTCRoomMessageService: NSObject, AUIMessageServiceConnectionDelegate {
    
    static let shared = ARTCRoomMessageService()
    
    public override init() {
        super.init()
        
        self.messageService.setConnectionDelegate(self)
    }
    
    var messageService: AUIMessageServiceProtocol {
        return AUIMessageServiceFactory.getMessageService()
    }
    
    var server: ARTCRoomAppServer? = nil
    
    public func onTokenExpire(_ updateTokenCompleted: AUIMessageDefaultCallback? = nil) {
        
        guard let currrentUser = ARTCRoomService.currrentUser else {
            updateTokenCompleted?(ARTCRoomError.createError(.Common, "current user is empty"))
            return
        }
        
        guard let server = self.server else {
            updateTokenCompleted?(ARTCRoomError.createError(.Common, "please login first"))
            return
        }
        
        server.fetchIMLoginToken(uid: currrentUser.userId) { [weak self] tokenData, error in
            guard error == nil else {
                updateTokenCompleted?(error as NSError?)
                return
            }
            guard let tokenData = tokenData else {
                updateTokenCompleted?(ARTCRoomError.createError(.Common, "lost token data"))
                return
            }
            
            self?.messageService.getConfig().tokenData = tokenData
            updateTokenCompleted?(nil)
        }
    }
    
    public static func getMessageService() -> AUIMessageServiceProtocol {
        return ARTCRoomMessageService.shared.messageService
    }
    
    public static func login(server: ARTCRoomAppServer, completed: ARTCRoomCompleted?) {
        guard let currrentUser = ARTCRoomService.currrentUser else {
            completed?(ARTCRoomError.createError(.Common, "current user is empty"))
            return
        }
        if self.getMessageService().isLogin() {
            completed?(nil)
            return
        }
        ARTCRoomMessageService.shared.server = server
        server.fetchIMLoginToken(uid: currrentUser.userId) { tokenData, error in
            guard error == nil else {
                completed?(error as NSError?)
                return
            }
            guard let tokenData = tokenData else {
                completed?(ARTCRoomError.createError(.Common, "lost token data"))
                return
            }
            
            let messageConfig = AUIMessageConfig()
            messageConfig.tokenData = tokenData
            self.getMessageService().setConfig(messageConfig);
            self.getMessageService().login(currrentUser, callback:{ error in
                completed?(error as NSError?)
            })
        }
    }
    
    public static func logout(completed: ARTCRoomCompleted?) {
        if self.getMessageService().isLogin() {
            self.getMessageService().logout({ error in
                completed?(error as NSError?)
            })
        }
        else {
            completed?(nil)
        }
    }
}
