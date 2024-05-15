//
//  MainViewController.swift
//  Example
//
//  Created by Bingo on 2024/1/10.
//

import UIKit
import AUIFoundation
import AUIRoomCore
import AUIVoiceRoom

class MainViewController: AVBaseViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        
        self.titleView.text = "APP首页"
                
        ARTCRoomLoginServer.shared.loginServerDomain = VoiceRoomServerDomain
        AVLoginManager.shared.doLogin = { uid, completed in
            ARTCRoomLoginServer.shared.loginApp(uid: uid) { user, error in
                if error == nil {
                    AUIVoiceRoomManager.shared.setup(currentUser: user!, serverAuth: ARTCRoomLoginServer.shared.serverAuth!)
                }
                completed(error)
            }
        }
        
        AVLoginManager.shared.doLogout = { uid, completed in
            ARTCRoomMessageService.logout { error in
                ARTCRoomService.currrentUser = nil
                completed(nil)
            }
            ARTCRoomLoginServer.shared.logoutApp()
        }
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.showListViewController(ani: false)
        }
    }
    
    func showListViewController(ani: Bool) {
        if AVLoginManager.shared.isLogin == true {
            let listVC = AUIVoiceRoomListViewController()
            self.navigationController?.pushViewController(listVC, animated: false)
        }
        else {
            let loginVC = AVLoginViewController()
            loginVC.hiddenBackButton = true
            loginVC.loginSuccessBlock = { [weak self] loginVC in
                self?.showListViewController(ani: true)
            }
            self.navigationController?.pushViewController(loginVC, animated: false)
        }
    }
}

