//
//  AUIVoiceRoomManager.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/6.
//

import UIKit
import AUIRoomCore
import AUIFoundation

@objcMembers  public class AUIVoiceRoomManager: NSObject {

    public static let shared = AUIVoiceRoomManager()
    
    override init() {
    }
    
    public func setCurrentUser(_ user: AUIRoomUser?) {
        AUIRoomService.currrentUser = user
    }
    
    public func isInRoom() -> Bool {
        return AUIRoomService.isInRoom
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
            AUIRoomService.login { error in
                if let error = error {
                    hud.hide(animated: false)
                    AVToastView.show("创建房间失败：登录失败（\(error.auiMessage)）", view: topVC.view, position: .mid)
                    return
                }
                
                ARTCVoiceRoomEngine.createRoom(roomName: "\(AUIRoomService.currrentUser!.userNick)的聊天室") { roomInfo, error in
                    hud.hide(animated: false)
                    if let error = error {
                        AVToastView.show("创建房间失败：\(error.auiMessage)", view: topVC.view, position: .mid)
                        return
                    }
                    if let roomInfo = roomInfo {
                        let controller = ARTCVoiceRoomEngine(roomInfo)
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
        AUIRoomService.login { error in
            if let error = error {
                hud.hide(animated: false)
                AVToastView.show("进入房间失败：登录失败（\(error.auiMessage)）", view: topVC.view, position: .mid)
                return
            }
            
            ARTCVoiceRoomEngine.getRoomDetail(roomId: roomId) { roomInfo, error in
                hud.hide(animated: false)
                if let error = error {
                    AVToastView.show("进入房间失败：无法获取房间详情（\(error.auiMessage)）", view: topVC.view, position: .mid)
                    return
                }
                if let roomInfo = roomInfo {
                    let controller = ARTCVoiceRoomEngine(roomInfo)
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
