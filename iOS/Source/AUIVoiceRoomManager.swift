//
//  AUIVoiceRoomManager.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/6.
//

import UIKit
import AUIRoomCore
import AUIFoundation

public let VoiceRoomServerDomain = "你的AppServer域名"

@objcMembers  public class AUIVoiceRoomManager: NSObject {

    public static let shared = AUIVoiceRoomManager()
    
    override init() {
        
    }
    
    private var roomServiceInterface: ARTCRoomServiceInterface? = nil
    
    public func getRoomServiceInterface() -> ARTCRoomServiceInterface {
        return self.roomServiceInterface!
    }
    
    public func getRoomAppServer() -> ARTCRoomAppServer {
        return (self.roomServiceInterface as? ARTCRoomServiceImpl)!.roomAppServer
    }
    
    public func setup(currentUser: ARTCRoomUser, serverAuth: String) {
        
        ARTCRoomService.currrentUser = currentUser
        ARTCRoomMessageService.logout { error in
            let roomAppServer = ARTCRoomAppServer(VoiceRoomServerDomain)
            roomAppServer.serverAuth = serverAuth
            self.roomServiceInterface = ARTCRoomServiceImpl(roomAppServer)
        }
    }
    
    public func isInRoom() -> Bool {
        return ARTCRoomService.isInRoom
    }
    
    public func createRoom(currVC: UIViewController? = nil, completed: (()->Void)? = nil) {
        
        AVDeviceAuth.checkMicAuth { auth in
            if auth == false {
                return
            }
            
            let topVC = currVC ?? UIViewController.av_top()
            let hud = AVProgressHUD.showAdded(to: topVC.view, animated: true)
            hud.iconType = .loading
            hud.labelText = "创建房间中..."
            ARTCRoomMessageService.login(server: self.getRoomAppServer()) { error in
                if let error = error {
                    hud.hide(animated: false)
                    AVToastView.show("创建房间失败：登录失败（\(error.artcMessage)）", view: topVC.view, position: .mid)
                    return
                }
                
                ARTCVoiceRoomEngine.createVoiceRoom(roomName: "\(ARTCRoomService.currrentUser!.userNick)的聊天室") { roomInfo, error in
                    hud.hide(animated: false)
                    if let error = error {
                        AVToastView.show("创建房间失败：\(error.artcMessage)", view: topVC.view, position: .mid)
                        return
                    }
                    if let roomInfo = roomInfo {
                        let controller = ARTCVoiceRoomEngine(roomInfo)
                        controller.roomService = self.getRoomServiceInterface()
                        let viewController = AUIVoiceRoomViewController(controller)
                        viewController.show(topVC: topVC)
                    }
                    else {
                        AVToastView.show("创建房间失败：未知错误", view: topVC.view, position: .mid)
                    }
                }
            }
        }
        
    }
    
    public func enterRoom(roomId: String, currVC: UIViewController? = nil, completed: (()->Void)? = nil) {
        let topVC = currVC ?? UIViewController.av_top()
        let hud = AVProgressHUD.showAdded(to: topVC.view, animated: true)
        hud.iconType = .loading
        hud.labelText = "进入房间中..."
        ARTCRoomMessageService.login(server: self.getRoomAppServer()) { error in
            if let error = error {
                hud.hide(animated: false)
                AVToastView.show("进入房间失败：登录失败（\(error.artcMessage)）", view: topVC.view, position: .mid)
                return
            }
            
            ARTCVoiceRoomEngine.getVoiceRoomDetail(roomId: roomId) { roomInfo, error in
                hud.hide(animated: false)
                if let error = error {
                    AVToastView.show("进入房间失败：无法获取房间详情（\(error.artcMessage)）", view: topVC.view, position: .mid)
                    return
                }
                if let roomInfo = roomInfo {
                    let controller = ARTCVoiceRoomEngine(roomInfo)
                    controller.roomService = self.getRoomServiceInterface()
                    let viewController = AUIVoiceRoomViewController(controller)
                    viewController.show(topVC: topVC)
                }
                else {
                    AVToastView.show("进入房间失败：未知错误", view: topVC.view, position: .mid)
                }
            }
        }
    }
}
