//
//  AUIRoomService.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit

@objcMembers open class AUIRoomAppServer: NSObject {
    
    public init(_ serverDomain: String) {
        self.serverDomain = serverDomain
    }
    
    public let serverDomain: String
    
    open func request(path: String, body: [AnyHashable: Any]?, completed: @escaping (_ response: URLResponse?, _ data: [AnyHashable: Any]?, _ error: NSError?) -> Void) -> Void {
        let urlString = "\(self.serverDomain)\(path)"
        let url = URL(string: urlString)
        guard let url = url else {
            completed(nil, nil, AUIRoomError.createError(.Common, "path error"))
            return
        }
        
        debugPrint("AUIRoomAppServer url: \(url)")
        debugPrint("AUIRoomAppServer body: \(body?.room_jsonString ?? "nil")")
        
        var urlRequest = URLRequest(url: url)
        urlRequest.setValue("application/json", forHTTPHeaderField: "accept")
        urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if self.serverAuth != nil {
            urlRequest.setValue("Bearer \(self.serverAuth!)", forHTTPHeaderField: "Authorization")
        }
        urlRequest.httpMethod = "POST"
        if let body = body {
            let bodyData = try? JSONSerialization.data(withJSONObject: body, options: .prettyPrinted)
            guard let bodyData = bodyData else {
                completed(nil, nil, AUIRoomError.createError(.Common, "body error"))
                return
            }
            urlRequest.httpBody = bodyData
        }
        
        let config = URLSessionConfiguration.default
        let session = URLSession.init(configuration: config)
        let task = session.dataTask(with: urlRequest) { data, rsp, error in
            DispatchQueue.main.async {
                if error != nil {
                    completed(rsp, nil, error as? NSError)
                    return
                }
                
                if rsp is HTTPURLResponse {
                    let httpRsp = rsp as! HTTPURLResponse
                    if httpRsp.statusCode == 200 {
                        if let data = data {
                            let obj = try? JSONSerialization.jsonObject(with: data, options: .allowFragments)
                            completed(rsp, obj as? [AnyHashable : Any], nil)
                            return
                        }
                    }
                }
                completed(rsp, nil, AUIRoomError.createError(.Common, "network error"))
            }
        }
        task.resume()
    }
    
    open var serverAuth: String? = ""
    private func serverAuthValid() -> Bool {
        return self.serverAuth != nil && !(self.serverAuth!.isEmpty)
    }
    
    // 模拟App登录
    open func loginApp(uid: String, completed: @escaping (_ user: AUIRoomUser?, _ error: NSError?) -> Void) {
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
                    let user = AUIRoomUser(uid)
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
                    completed(nil, AUIRoomError.createError(.Common, "data error"))
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
    
    open func fetchIMLoginToken(uid: String, completed: @escaping (_ tokenData: [String : Any]?, _ error: NSError?) -> Void) {
        if !self.serverAuthValid() {
            completed(nil, AUIRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body :[String : Any] = [
            "user_id": uid,
            "role":"admin",
        ]
        self.request(path: "/api/chatroom/token", body: body) { response, data, error in
            if error == nil {
                let tokenData = data?["aliyun_im"] as? Dictionary<String, Any>
                if let tokenData = tokenData {
                    var final = tokenData
                    final["source"] = "aui-room"
                    completed(final, nil)
                }
                else {
                    completed(nil, AUIRoomError.createError(.Common, "fetch token failed"))
                }
            }
            else {
                completed(nil, error)
            }
        }
    }
    
    open func fetchRTCAuthToken(uid: String, roomId: String, completed: @escaping (_ token: String?, _ timestamp: Int64, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, 0, AUIRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "room_id": roomId,
            "user_id": uid
        ]
        self.request(path: "/api/chatroom/getRtcAuthToken", body: body) { response, data, error in
            if error == nil {
                let token = data?["auth_token"] as? String
                let timestamp = data?["timestamp"] as? Int64
                completed(token, timestamp ?? 0, nil)
            }
            else {
                completed(nil, 0, error)
            }
        }
    }
    
    open func createRoom(title: String?, uid: String, nick: String, extends:[String : Any], completed: @escaping (_ data: [AnyHashable: Any]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, AUIRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "title": title ?? "\(nick)的房间",
            "anchor": uid,
            "anchor_nick": nick,
            "extends": extends.room_jsonString
        ]
        self.request(path: "/api/chatroom/create", body: body) { response, data, error in
            completed(data, error)
        }
    }
    
    open func dismissRoom(uid: String, roomId: String, completed: @escaping (_ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(AUIRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "id": roomId,
            "user_id": uid,
        ]
        self.request(path: "/api/chatroom/dismiss", body: body) { response, data, error in
            if error == nil {
                if data?["success"] as? Bool == true {
                    completed(nil)
                }
                else {
                    completed(AUIRoomError.createError(.Common, "dimiss room failed"))
                }
            }
            else {
                completed(error)
            }
        }
    }
    
    open func getRoomList(uid: String, pageNum: Int, pageSize: Int, completed: @escaping (_ data: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, AUIRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "user_id": uid,
            "page_num": pageNum,
            "page_size": pageSize
        ]
        self.request(path: "/api/chatroom/list", body: body) { response, data, error in
            if error == nil {
                let rooms = data?["rooms"] as? [[AnyHashable: Any]]
                completed(rooms, error)
            }
            else {
                completed(nil, error)
            }
        }
    }
    
    open func getRoomDetail(uid: String, roomId: String, completed: @escaping (_ data: [AnyHashable: Any]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, AUIRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "id": roomId,
            "user_id": uid,
        ]
        self.request(path: "/api/chatroom/get", body: body) { response, data, error in
            completed(data, error)
        }
    }
    
    open func getMicList(uid: String, roomId: String, completed: @escaping (_ data: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, AUIRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "id": roomId,
            "user_id": uid,
        ]
        
        self.request(path: "/api/chatroom/getMeetingInfo", body: body) { response, data, error in
            if error == nil {
                let members = data?["members"] as? [[AnyHashable: Any]]
                completed(members, error)
            }
            else {
                completed(nil, error)
            }
        }
    }
    
    open func joinMic(uid: String, roomId: String, index: Int32, extends: String, completed: @escaping (_ data: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, AUIRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        var body: [String : Any] = [
            "id": roomId,
            "user_id": uid,
            "extends": extends
        ]
        if index > 0 {
            body["index"] = index
        }
        
        self.request(path: "/api/chatroom/joinMic", body: body) { response, data, error in
            if error == nil {
                let members = data?["members"] as? [[AnyHashable: Any]]
                if let members = members {
                    completed(members, error)
                }
                else {
                    let reason = data?["reason"] as? Int
                    var code = AUIRoomErrorCode.Common
                    if reason == 1 {
                        code = .JoinedMicErrorForNotIndex
                    }
                    else if reason == 2 {
                        code = .JoinedMicErrorForAlreadyJoined
                    }
                    let msg = data?["message"] as? String
                    completed(nil, AUIRoomError.createError(code, msg))
                }
            }
            else {
                completed(nil, error)
            }
        }
    }
    
    open func leaveMic(uid: String, roomId: String, index: Int32, completed: @escaping (_ data: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, AUIRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "id": roomId,
            "user_id": uid,
            "index": index
        ]
        
        self.request(path: "/api/chatroom/leaveMic", body: body) { response, data, error in
            if error == nil {
                let members = data?["members"] as? [[AnyHashable: Any]]
                completed(members, error)
            }
            else {
                completed(nil, error)
            }
        }
    }
}
