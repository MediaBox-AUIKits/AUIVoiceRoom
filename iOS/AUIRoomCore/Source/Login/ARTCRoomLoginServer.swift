//
//  ARTCRoomAppServer.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit

// 用于Demo的登录
@objcMembers open class ARTCRoomLoginServer: ARTCRoomAppServer {
    
    public static let shared = ARTCRoomLoginServer("")

    public var loginServerDomain: String? = nil
    open override func getServerDomain() -> String {
        if let loginServerDomain = self.loginServerDomain {
            return loginServerDomain
        }
        return super.getServerDomain()
    }

    // 模拟App登录
    open func loginApp(uid: String, completed: @escaping (_ user: ARTCRoomUser?, _ error: NSError?) -> Void) {
        self.request(path: "/login", body: ["password": uid, "username": uid]) { response, data, error in
            if error == nil {
                let auth = data?["token"] as? String
                if auth != nil && !auth!.isEmpty {
                    self.serverAuth = auth
                    
                    let avatarList: [String] = [
                        "https://img.alicdn.com/imgextra/i3/O1CN01RScCaG1Ogg7EHqMU8_!!6000000001735-2-tps-174-174.png",
                        "https://img.alicdn.com/imgextra/i1/O1CN01fwrnjZ1HbugVT1prp_!!6000000000777-2-tps-174-174.png",
                        "https://img.alicdn.com/imgextra/i2/O1CN01Izsial1HimcrLB7hW_!!6000000000792-2-tps-174-174.png",
                        "https://img.alicdn.com/imgextra/i2/O1CN011QCZqK1arvEDOqARU_!!6000000003384-2-tps-174-174.png",
                        "https://img.alicdn.com/imgextra/i4/O1CN01nBP9CO22Cz4DJw50t_!!6000000007085-2-tps-174-174.png",
                        "https://img.alicdn.com/imgextra/i4/O1CN01rgx31a1ZVBxNVVC7Q_!!6000000003199-2-tps-174-174.png",
                        "https://img.alicdn.com/imgextra/i1/O1CN01p5nNVQ1eRavOtp5iU_!!6000000003868-2-tps-174-174.png",
                        "https://img.alicdn.com/imgextra/i1/O1CN01puPUSh1wE0FtPOMtf_!!6000000006275-2-tps-174-174.png",
                        "https://img.alicdn.com/imgextra/i2/O1CN01vqjRRH1V9t4PV8ORg_!!6000000002611-2-tps-174-174.png",
                    ]
                    let user = ARTCRoomUser(uid)
                    user.userNick = uid
                    let first = uid[uid.startIndex]
                    if let value = first.asciiValue {
                        user.userAvatar = avatarList[Int(value) % avatarList.count]
                    }
                    else {
                        user.userAvatar = avatarList[0]
                    }
                    completed(user, error)
                }
                else {
                    completed(nil, ARTCRoomError.createError(.Common, "data error"))
                }
            }
            else {
                completed(nil, error)
            }
        }
    }
    
    // 模拟App登出
    open func logoutApp() {
        self.serverAuth = nil
    }
}
