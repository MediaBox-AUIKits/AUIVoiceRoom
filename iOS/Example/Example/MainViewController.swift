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
                
        AVLoginManager.shared.doLogin = { uid, completed in
            self.appServer.loginApp(uid: uid) { user, error in
                AUIVoiceRoomManager.shared.setCurrentUser(user)
                completed(error)
            }
        }
        
        AVLoginManager.shared.doLogout = { uid, completed in
            AUIRoomService.logout { error in
                AUIVoiceRoomManager.shared.setCurrentUser(nil)
                completed(nil)
            }
        }
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.showListViewController(ani: false)
        }
    }
    
    var appServer: AUIRoomAppServer {
        return (AUIRoomService.getInterface() as! AUIRoomServiceImpl).roomAppServer
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

