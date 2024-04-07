//
//  ARTCVoiceRoomEngineDelegate.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/3/11.
//

import UIKit
import AUIRoomCore

@objc public protocol ARTCVoiceRoomEngineDelegate {
    
    @objc optional func onJoinedRoom(user: AUIRoomUser)
    @objc optional func onLeavedRoom(user: AUIRoomUser)
    @objc optional func onKickoutRoom()
    @objc optional func onDismissedRoom()
    
    @objc optional func onOnlineCountChanged(count: Int)

    @objc optional func onJoinedMic(seatIndex: Int32, user: AUIRoomUser)
    @objc optional func onLeavedMic(seatIndex: Int32, user: AUIRoomUser)
    
    @objc optional func onMicUserStreamChanged(seatIndex: Int32, user: AUIRoomUser, publishing: Bool)
    @objc optional func onMicUserMicrophoneChanged(seatIndex: Int32, user: AUIRoomUser, off: Bool)
    @objc optional func onMicUserSpeakStateChanged(seatIndex: Int32, isSpeaking: Bool)

    @objc optional func onNetworkStateChanged(user: AUIRoomUser, state: String)
    @objc optional func onReceivedTextMessage(user: AUIRoomUser, text: String)
    
    @objc optional func onError(_ error: NSError)
}

extension ARTCVoiceRoomEngine {
    
    
    public func addObserver(delegate: ARTCVoiceRoomEngineDelegate) {
        if Thread.isMainThread {
            if !self.observerArray.contains(delegate) {
                self.observerArray.add(delegate)
            }
        }
        else {
            DispatchQueue.main.async {
                self.addObserver(delegate: delegate)
            }
        }
    }
    
    public func removeObserver(delegate: ARTCVoiceRoomEngineDelegate) {
        if Thread.isMainThread {
            self.observerArray.remove(delegate)
        }
        else {
            DispatchQueue.main.async {
                self.removeObserver(delegate: delegate)
            }
        }
    }
    
    func notifyOnJoinedRoom(user: AUIRoomUser) {
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
    
    func notifyOnLeavedRoom(user: AUIRoomUser) {
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
    
    func notifyOnJoinedMic(seatIndex: Int32, user: AUIRoomUser) {
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
    
    func notifyOnLeavedMic(seatIndex: Int32, user: AUIRoomUser) {
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
    
    func notifyOnMicUserMicrophoneChanged(seatIndex: Int32, user: AUIRoomUser, off: Bool) {
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
    
    func notifyOnMicUserStreamChanged(seatIndex: Int32, user: AUIRoomUser, publishing: Bool) {
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
    
    func notifyOnNetworkStateChanged(user: AUIRoomUser, state: String) {
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
    
    func notifyOnReceivedTextMessage(user: AUIRoomUser, text: String) {
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
