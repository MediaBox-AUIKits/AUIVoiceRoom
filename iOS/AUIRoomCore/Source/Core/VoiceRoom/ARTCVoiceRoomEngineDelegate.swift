//
//  ARTCVoiceRoomEngineDelegate.swift
//  AUIRoomCore
//
//  Created by Bingo on 2024/3/11.
//

import UIKit

@objc public protocol ARTCVoiceRoomEngineDelegate {
    
    // 有人进入语聊房了
    @objc optional func onJoinedRoom(user: ARTCRoomUser)
    // 有人离开语聊房了
    @objc optional func onLeavedRoom(user: ARTCRoomUser)
    // 自己被踢出语聊房了
    @objc optional func onKickoutRoom()
    // 语聊房解散了
    @objc optional func onDismissedRoom()
    // 房间内在线人数发生了变化
    @objc optional func onOnlineCountChanged(count: Int)

    // 有人上麦
    @objc optional func onJoinedMic(seatIndex: Int32, user: ARTCRoomUser)
    // 有人下麦
    @objc optional func onLeavedMic(seatIndex: Int32, user: ARTCRoomUser)
    
    // 麦上用户语音流发生了变化（推流 or 停止推流）
    @objc optional func onMicUserStreamChanged(seatIndex: Int32, user: ARTCRoomUser, publishing: Bool)
    // 麦上用户麦克风打开/关闭
    @objc optional func onMicUserMicrophoneChanged(seatIndex: Int32, user: ARTCRoomUser, off: Bool)
    // 麦上用户是否正在说话中
    @objc optional func onMicUserSpeakStateChanged(seatIndex: Int32, isSpeaking: Bool)
    // 网络出现了变化
    @objc optional func onNetworkStateChanged(user: ARTCRoomUser, state: String)
    
    // 音乐播放状态发生了变化(自己)
    @objc optional func onBackgroundMusicStatusChanged(status: ARTCVoiceRoomAudioPlayingStatus)
    
    // 音效播放结束(自己)
    @objc optional func onAudioEffectPlayCompleted(effectId: Int32)
    
    // 自己收到了弹幕
    @objc optional func onReceivedTextMessage(user: ARTCRoomUser, text: String)
    
    // 发生了错误
    @objc optional func onError(_ error: NSError)
}

extension ARTCVoiceRoomEngine {
    
    func notifyOnJoinedRoom(user: ARTCRoomUser) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onJoinedRoom?(user: user)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnJoinedRoom(user: user)
            }
        }
    }
    
    func notifyOnLeavedRoom(user: ARTCRoomUser) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onLeavedRoom?(user: user)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnLeavedRoom(user: user)
            }
        }
    }
    
    func notifyOnKickoutRoom() {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onKickoutRoom?()
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnKickoutRoom()
            }
        }
    }
    
    func notifyOnDismissedRoom() {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onDismissedRoom?()
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnDismissedRoom()
            }
        }
    }
    
    func notifyOnOnlineCountChanged(count: Int) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onOnlineCountChanged?(count: count)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnOnlineCountChanged(count: count)
            }
        }
    }
    
    func notifyOnJoinedMic(seatIndex: Int32, user: ARTCRoomUser) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onJoinedMic?(seatIndex: seatIndex, user: user)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnJoinedMic(seatIndex: seatIndex, user: user)
            }
        }
    }
    
    func notifyOnLeavedMic(seatIndex: Int32, user: ARTCRoomUser) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onLeavedMic?(seatIndex: seatIndex, user: user)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnLeavedMic(seatIndex: seatIndex, user: user)
            }
        }
    }
    
    func notifyOnMicUserMicrophoneChanged(seatIndex: Int32, user: ARTCRoomUser, off: Bool) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onMicUserMicrophoneChanged?(seatIndex: seatIndex, user: user, off: off)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMicUserMicrophoneChanged(seatIndex: seatIndex, user: user, off: off)
            }
        }
    }
    
    func notifyOnMicUserSpeakStateChanged(seatIndex: Int32, isSpeaking: Bool) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onMicUserSpeakStateChanged?(seatIndex: seatIndex, isSpeaking: isSpeaking)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMicUserSpeakStateChanged(seatIndex: seatIndex, isSpeaking: isSpeaking)
            }
        }
    }
    
    func notifyOnMicUserStreamChanged(seatIndex: Int32, user: ARTCRoomUser, publishing: Bool) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onMicUserStreamChanged?(seatIndex: seatIndex, user: user, publishing: publishing)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnMicUserStreamChanged(seatIndex: seatIndex, user: user, publishing: publishing)
            }
        }
    }
    
    func notifyOnNetworkStateChanged(user: ARTCRoomUser, state: String) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onNetworkStateChanged?(user: user, state: state)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnNetworkStateChanged(user: user, state: state)
            }
        }
    }
    
    func notifyOnBackgroundMusicStatusChanged(status: ARTCVoiceRoomAudioPlayingStatus) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onBackgroundMusicStatusChanged?(status: status)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnBackgroundMusicStatusChanged(status: status)
            }
        }
    }
    
    func notifyOnAudioEffectPlayCompleted(effectId: Int32) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onAudioEffectPlayCompleted?(effectId: effectId)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnAudioEffectPlayCompleted(effectId: effectId)
            }
        }
    }
    
    func notifyOnReceivedTextMessage(user: ARTCRoomUser, text: String) {
        if Thread.isMainThread {
            for delegate in self.observerArray.allObjects {
                delegate.onReceivedTextMessage?(user: user, text: text)
            }
        }
        else {
            DispatchQueue.main.async {
                self.notifyOnReceivedTextMessage(user: user, text: text)
            }
        }
    }
}
