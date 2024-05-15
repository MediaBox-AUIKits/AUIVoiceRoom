//
//  AUIVoiceRoomInfo.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/8.
//

import UIKit

@objcMembers open class ARTCVoiceRoomInfo: ARTCRoomInfo {

    public override init?(data: [AnyHashable : Any]?) {
        super.init(data: data)
        
        self.anchorSeatInfo.user = self.anchor
        self.anchorSeatInfo.isAnchor = true
        
        guard let data = data else { return nil }
        if let meetingInfoString = data["meetingInfo"] as? String {
            let meetingInfo = (try? JSONSerialization.jsonObject(with: meetingInfoString.data(using: .utf8)!, options: .allowFragments)) as? [String : Any]
            if let members = meetingInfo?["members"] as? [[String : Any]] {
                members.forEach { micData in
                    if let micInfo = AUIVoiceRoomMicSeatInfo(data: micData) {
                        self.seatInfoList.append(micInfo)
                    }
                }
            }
        }
    }
    
    public override init(_ roomId: String, _ anchor: ARTCRoomUser) {
        super.init(roomId, anchor)

        self.anchorSeatInfo.user = self.anchor
        self.anchorSeatInfo.isAnchor = true
    }
    
    public let anchorSeatInfo: AUIVoiceRoomMicSeatInfo = AUIVoiceRoomMicSeatInfo(0)
    open private(set) lazy var seatInfoList: [AUIVoiceRoomMicSeatInfo] = {
        var list = [AUIVoiceRoomMicSeatInfo]()
        return list
    }()
    
    open func getMicSeatInfo(index: Int32) -> AUIVoiceRoomMicSeatInfo? {
        if index == self.anchorSeatInfo.index {
            return self.anchorSeatInfo
        }
        return self.seatInfoList.first { info in
            return info.index == index
        }
    }
    
    open func getMicSeatInfo(uid: String) -> AUIVoiceRoomMicSeatInfo? {
        if uid == self.anchorSeatInfo.user?.userId {
            return self.anchorSeatInfo
        }
        return self.seatInfoList.first { info in
            return info.user?.userId == uid
        }
    }
}


@objcMembers open class AUIVoiceRoomMicSeatInfo: ARTCRoomMicInfo {
    
    open override var user: ARTCRoomUser? {
        didSet {
            if self.user == nil {
                // 设置下麦后初始状态
                self._isAnchor = false
                self._isMe = false
                self._isPublishStream = false
                self._isMuteMic = false
                self._networkStatus = .Unknow
            }
            else {
                // 设置上麦后初始状态
                self._isAnchor = false
                self._isMe = false
                self._isPublishStream = false
                self._isMuteMic = false
                self._networkStatus = .Good
            }
        }
    }
    
    private var _isAnchor: Bool = false
    open var isAnchor: Bool {
        get {
            return self._isAnchor
        }
        set {
            if self.isJoin {
                self._isAnchor = newValue
            }
        }
    }
    
    private var _isMe: Bool = false
    open var isMe: Bool {
        get {
            return self._isMe
        }
        set {
            if self.isJoin {
                self._isMe = newValue
            }
        }
    }
    
    private var _isPublishStream: Bool = false
    open var isPublishStream: Bool {
        get {
            return self._isPublishStream
        }
        set {
            if self.isJoin {
                self._isPublishStream = newValue
            }
        }
    }
    
    private var _isMuteMic: Bool = false
    open var isMuteMic: Bool {
        get {
            return self._isMuteMic
        }
        set {
            if self.isJoin {
                self._isMuteMic = newValue
            }
        }
    }
    
    private var _networkStatus: ARTCRoomNetworkState = .Unknow
    open var networkStatus: ARTCRoomNetworkState {
        get {
            return self._networkStatus
        }
        set {
            if self.isJoin {
                self._networkStatus = newValue
            }
        }
    }
}
