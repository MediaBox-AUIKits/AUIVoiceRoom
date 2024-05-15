//
//  ARTCRoomAppServer.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/4.
//

import UIKit

@objcMembers open class ARTCRoomAppServer: NSObject {
    
    public init(_ serverDomain: String) {
        self.serverDomain = serverDomain
    }
    
    private let serverDomain: String
    
    open func getServerDomain() -> String {
        return self.serverDomain
    }
    
    open func request(path: String, body: [AnyHashable: Any]?, completed: @escaping (_ response: URLResponse?, _ data: [AnyHashable: Any]?, _ error: NSError?) -> Void) -> Void {
        let urlString = "\(self.getServerDomain())\(path)"
        let url = URL(string: urlString)
        guard let url = url else {
            completed(nil, nil, ARTCRoomError.createError(.Common, "path error"))
            return
        }
        
        debugPrint("ARTCRoomAppServer url: \(url)")
        debugPrint("ARTCRoomAppServer body: \(body?.artcJsonString ?? "nil")")
        
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
                completed(nil, nil, ARTCRoomError.createError(.Common, "body error"))
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
                completed(rsp, nil, ARTCRoomError.createError(.Common, "network error"))
            }
        }
        task.resume()
    }
    
    open var serverAuth: String? = ""
    open func serverAuthValid() -> Bool {
        return self.serverAuth != nil && !(self.serverAuth!.isEmpty)
    }
    
    open func pathPrefix() -> String {
        return "/api/chatroom"
    }
    
    open func fetchIMLoginToken(uid: String, completed: @escaping (_ tokenData: [String : Any]?, _ error: NSError?) -> Void) {
        if !self.serverAuthValid() {
            completed(nil, ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body :[String : Any] = [
            "user_id": uid,
            "role":"admin",
        ]
        self.request(path: "\(self.pathPrefix())/token", body: body) { response, data, error in
            if error == nil {
                let tokenData = data?["aliyun_im"] as? Dictionary<String, Any>
                if let tokenData = tokenData {
                    var final = tokenData
                    final["source"] = "aui-room"
                    completed(final, nil)
                }
                else {
                    completed(nil, ARTCRoomError.createError(.Common, "fetch token failed"))
                }
            }
            else {
                completed(nil, error)
            }
        }
    }
    
    open func fetchRTCAuthToken(uid: String, roomId: String, completed: @escaping (_ token: String?, _ timestamp: Int64, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, 0, ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "room_id": roomId,
            "user_id": uid
        ]
        self.request(path: "\(self.pathPrefix())/getRtcAuthToken", body: body) { response, data, error in
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
            completed(nil, ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "title": title ?? "\(nick)的房间",
            "anchor": uid,
            "anchor_nick": nick,
            "extends": extends.artcJsonString
        ]
        self.request(path: "\(self.pathPrefix())/create", body: body) { response, data, error in
            completed(data, error)
        }
    }
    
    open func dismissRoom(uid: String, roomId: String, completed: @escaping (_ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "id": roomId,
            "user_id": uid,
        ]
        self.request(path: "\(self.pathPrefix())/dismiss", body: body) { response, data, error in
            if error == nil {
                if data?["success"] as? Bool == true {
                    completed(nil)
                }
                else {
                    completed(ARTCRoomError.createError(.Common, "dimiss room failed"))
                }
            }
            else {
                completed(error)
            }
        }
    }
    
    open func getRoomList(uid: String, pageNum: Int, pageSize: Int, completed: @escaping (_ data: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "user_id": uid,
            "page_num": pageNum,
            "page_size": pageSize
        ]
        self.request(path: "\(self.pathPrefix())/list", body: body) { response, data, error in
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
            completed(nil, ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "id": roomId,
            "user_id": uid,
        ]
        self.request(path: "\(self.pathPrefix())/get", body: body) { response, data, error in
            completed(data, error)
        }
    }
    
    open func getMicList(uid: String, roomId: String, completed: @escaping (_ data: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "id": roomId,
            "user_id": uid,
        ]
        
        self.request(path: "\(self.pathPrefix())/getMeetingInfo", body: body) { response, data, error in
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
            completed(nil, ARTCRoomError.createError(.Common, "lack of auth token"))
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
        
        self.request(path: "\(self.pathPrefix())/joinMic", body: body) { response, data, error in
            if error == nil {
                let members = data?["members"] as? [[AnyHashable: Any]]
                if let members = members {
                    completed(members, error)
                }
                else {
                    let reason = data?["reason"] as? Int
                    var code = ARTCRoomErrorCode.Common
                    if reason == 1 {
                        code = .JoinedMicErrorForNotIndex
                    }
                    else if reason == 2 {
                        code = .JoinedMicErrorForAlreadyJoined
                    }
                    let msg = data?["message"] as? String
                    completed(nil, ARTCRoomError.createError(code, msg))
                }
            }
            else {
                completed(nil, error)
            }
        }
    }
    
    open func leaveMic(uid: String, roomId: String, index: Int32, completed: @escaping (_ data: [[AnyHashable: Any]]?, _ error: NSError?) -> Void) {
        
        if !self.serverAuthValid() {
            completed(nil, ARTCRoomError.createError(.Common, "lack of auth token"))
            return
        }
        
        let body: [String : Any] = [
            "id": roomId,
            "user_id": uid,
            "index": index
        ]
        
        self.request(path: "\(self.pathPrefix())/leaveMic", body: body) { response, data, error in
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
