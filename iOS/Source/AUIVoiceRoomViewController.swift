//
//  AUIVoiceRoomViewController.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/5.
//

import UIKit
import AUIFoundation
import SnapKit
import SDWebImage
import AUIRoomCore

@objcMembers open class AUIVoiceRoomViewController: UIViewController {

    public init(_ roomController: ARTCVoiceRoomEngine) {
        self.roomController = roomController
        super.init(nibName: nil, bundle: nil)
        
        self.roomController.addObserver(delegate: self)
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        self.roomController.removeObserver(delegate: self)
        UIViewController.av_setIdleTimerDisabled(false)
        debugPrint("deinit: \(self)")
    }
    
    open override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
        self.view.addSubview(self.bgView)
        self.bgView.snp.makeConstraints { make in
            make.left.right.top.bottom.equalToSuperview()
        }
        
        self.view.addSubview(self.exitBtn)
        self.exitBtn.snp.makeConstraints { make in
            make.width.height.equalTo(44)
            make.right.equalToSuperview().offset(-8)
            make.top.equalToSuperview().offset(UIView.av_safeTop)
        }
        
        self.view.addSubview(self.memberBtn)
        self.memberBtn.snp.makeConstraints { make in
            make.width.greaterThanOrEqualTo(48)
            make.height.equalTo(28)
            make.right.equalTo(self.exitBtn.snp.left).offset(4)
            make.centerY.equalTo(self.exitBtn)
        }
        
        self.view.addSubview(self.titleLabel)
        self.titleLabel.snp.makeConstraints { make in
            make.height.equalTo(22)
            make.left.equalToSuperview().offset(20)
            make.width.lessThanOrEqualTo(335 / 2.0)
            make.top.equalToSuperview().offset(UIView.av_safeTop)
        }
        
        self.view.addSubview(self.infoLabel)
        self.infoLabel.snp.makeConstraints { make in
            make.height.equalTo(18)
            make.left.equalTo(self.titleLabel)
            make.width.lessThanOrEqualTo(335 / 2.0 - 16)
            make.top.equalTo(self.titleLabel.snp.bottom)
        }
        
        self.view.addSubview(self.copyBtn)
        self.copyBtn.snp.makeConstraints { make in
            make.height.equalTo(18)
            make.left.equalTo(self.infoLabel.snp.right)
            make.width.equalTo(20)
            make.centerY.equalTo(self.infoLabel)
        }
        
        self.view.addSubview(self.statusView)
        self.statusView.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-19)
            make.top.equalTo(self.exitBtn.snp.bottom)
            make.height.equalTo(16)
            make.width.greaterThanOrEqualTo(40)
        }
        
        self.view.addSubview(self.bottomView)
        self.bottomView.snp.makeConstraints { make in
            make.height.equalTo(44 + UIView.av_safeBottom)
            make.width.equalToSuperview()
            make.left.equalToSuperview()
            make.bottom.equalToSuperview()
        }
        
        self.view.addSubview(self.micSeatView)
        self.micSeatView.snp.makeConstraints { make in
            make.left.right.equalToSuperview()
            make.top.equalTo(self.exitBtn.snp.bottom).offset(26)
            make.height.equalTo(240 + 36)
        }
        
        self.view.addSubview(self.commentView)
        self.commentView.snp.makeConstraints { make in
            make.left.equalToSuperview().offset(20)
            make.right.equalToSuperview().offset(-110)
            make.bottom.equalTo(self.bottomView.snp.top).offset(-18)
            make.top.equalTo(self.micSeatView.snp.bottom).offset(26)
        }
        
        if self.roomController.isAnchor == false {
            self.view.addSubview(self.micBtn)
            self.micBtn.snp.makeConstraints { make in
                make.width.greaterThanOrEqualTo(76)
                make.height.equalTo(32)
                make.right.equalToSuperview().offset(-26)
                make.bottom.equalTo(self.bottomView.snp.top).offset(-18)
            }
        }
        self.view.bringSubviewToFront(self.bottomView)
        
        self.titleLabel.text = self.roomInfo.roomName
        self.infoLabel.text = "房间号：\(self.roomInfo.roomCode > 0 ? "\(self.roomInfo.roomCode)" : self.roomInfo.roomId)"
        self.memberBtn.updateCount(count: self.roomInfo.onlineCount)
        if self.roomInfo.anchor.userAvatar.isEmpty == false {
            self.memberBtn.iconView.sd_setImage(with: URL(string: self.roomInfo.anchor.userAvatar), placeholderImage: AUIVoiceRoomBundle.getCommonImage("ic_default_avatar"))
        }

        if self.roomInfo.status == .Ended {
            AVAlertController.show(withTitle: "该房间已经解散", message: "", needCancel: false) { cancel in
                self.close()
            }
            return
        }
        
        self.roomController.joinRoom { [weak self] error in
            if let error = error {
                AVAlertController.show(withTitle: "进入房间失败，请稍后重试~", message: error.auiMessage, needCancel: false) { cancel in
                    self?.close()
                }
            }
        }
        self.updateMicState()
                
        DispatchQueue.main.async {
            self.insertTips(self.warningText)
        }
        UIViewController.av_setIdleTimerDisabled(true)
    }
    
    open override var preferredStatusBarStyle: UIStatusBarStyle {
        return .lightContent
    }
    
    open override var shouldAutorotate: Bool {
        return false
    }
    
    open override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .portrait
    }
    
    open override var preferredInterfaceOrientationForPresentation: UIInterfaceOrientation {
        return .portrait
    }
    
    open var warningText: String {
        return "欢迎来到语聊房，房间禁止谈论政治、低俗色情、吸烟酗酒或发布虚假信息等内容，若有违反将踢出、封停账号。"
    }
    
    public let roomController: ARTCVoiceRoomEngine
    open var roomInfo: AUIVoiceRoomInfo {
        return self.roomController.roomInfo
    }
    
    public lazy var bgView: UIImageView = {
        let view = UIImageView(image: AUIVoiceRoomBundle.getCommonImage("bg"))
        return view
    }()

    public lazy var exitBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_close"), for: .normal)
        btn.clickBlock = {[weak self] sender in
            if self?.roomController.isAnchor == true {
                AVAlertController.show(withTitle: "退出房间", message: "离开后会解散房间，确认离开吗？", cancelTitle:"暂不离开", okTitle: "确认离开") { isCanced in
                    if isCanced == false {
                        let hud = AVProgressHUD.showAdded(to: self!.view, animated: true)
                        hud.iconType = .loading
                        hud.labelText = "正在解散房间中..."
                        self?.roomController.dismissRoom(completed: { error in
                            hud.hide(animated: false)
                            if error != nil {
                                AVAlertController.show(withTitle: "解散失败，是否强制退出房间？", message: "",  cancelTitle:"暂不离开", okTitle: "强制离开") { isCanced in
                                    if isCanced == false {
                                        self?.roomController.leaveRoom()
                                        self?.close()
                                    }
                                }
                            }
                            else {
                                self?.close()
                            }
                        })
                    }
                }
            }
            else if self?.roomController.isJoinMic == true {
                AVAlertController.show(withTitle: "退出房间", message: "你正在连麦中，是否下麦并退出房间？",  cancelTitle:"暂不离开", okTitle: "确认离开") { isCanced in
                    if isCanced == false {
                        self?.roomController.leaveRoom()
                        self?.close()
                    }
                }
            }
            else {
                AVAlertController.show(withTitle: "退出房间", message: "离开房间后还可以再次进入房间",  cancelTitle:"暂不离开", okTitle: "确认离开") { isCanced in
                    if isCanced == false {
                        self?.roomController.leaveRoom()
                        self?.close()
                    }
                }
            }
        }
        return btn
    }()
    
    public lazy var memberBtn: AUIVoiceRoomMemberBtn = {
        let btn = AUIVoiceRoomMemberBtn()
        btn.layer.cornerRadius = 14
        btn.layer.masksToBounds = true
        return btn
    }()
    
    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.mediumFont(12)
        return label
    }()
    
    public lazy var infoLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.mediumFont(12)
        return label
    }()
    
    public lazy var copyBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.setImage(AUIVoiceRoomBundle.getCommonImage("ic_copy"), for: .normal)
        btn.clickBlock = {[weak self] sender in
            if let self = self {
                let pasteboard = UIPasteboard.general
                pasteboard.string = "\(self.roomInfo.roomCode)"
                
                AVToastView.show("已复制您的ID", view: self.view, position: .mid)
            }
        }
        return btn
    }()
    
    public lazy var statusView: AVNetworkStatusView = {
        let view = AVNetworkStatusView()
        return view
    }()
    
    public lazy var bottomView: AUIVoiceRoomBottomView = {
        let view = AUIVoiceRoomBottomView()
        view.commentTextField.sendCommentBlock = {[weak self] sender, comment in
            self?.roomController.sendTextMessage(text: comment, completed: { error in
                if error != nil {
                    if let self = self {
                        AVToastView.show("发送评论失败：\(error!.auiMessage)", view: self.view, position: .mid)
                    }
                }
            })
        }
        view.soundEffectBtn.clickBlock = { [weak self] btn in
            let panel = AUIVoiceRoomSoundEffectPanel(frame: CGRect(x: 0, y: 0, width: self!.view.av_width, height: 0))
            panel.show(on: self!.view, with: .clickToClose)
        }
        view.mixerBtn.clickBlock = { [weak self] btn in
            let panel = AUIVoiceRoomMixerPanel(frame: CGRect(x: 0, y: 0, width: self!.view.av_width, height: 0))
            panel.show(on: self!.view, with: .clickToClose)
        }
        view.switchSpeakerBtn.clickBlock = { [weak self] btn in
            self?.roomController.switchAudioOutput(type: btn.isSelected == false ? .Headset : .Speaker, completed: { error in
                if error == nil {
                    btn.isSelected = !btn.isSelected
                    AVToastView.show(btn.isSelected ? "扬声器已关闭" : "扬声器已开启", view: self!.view, position: .mid)
                }
            })
        }
        view.switchMicrophoneBtn.clickBlock = { [weak self] btn in
            self?.roomController.switchMicrophone(off: !btn.isSelected, completed:nil)
        }
        return view
    }()
    
    public lazy var commentView: AVCommentView = {
        let view = AVCommentView()
        return view
    }()
    
    public lazy var commentMeFlagImage: UIImage = {
        let image = AVCommentModel.flagImage(AUIVoiceRoomBundle.getString("自己")!, fontSize: 7, textColor: .white, bgColor: UIColor.av_color(withHexString: "#0077FA"), cornerRadius: 6, minWidth: 21, height: 12)
        return image
    }()
    
    public lazy var commentAnchorFlagImage: UIImage = {
        let image = AVCommentModel.flagImage(AUIVoiceRoomBundle.getString("主持")!, fontSize: 7, textColor: .white, bgColor: UIColor.av_color(withHexString: "#00BCD4"), cornerRadius: 6, minWidth: 21, height: 12)
        return image
    }()
    
    public lazy var micSeatView: AUIVoiceRoomMicSeatView = {
        let view = AUIVoiceRoomMicSeatView(roomInfo: self.roomInfo)
        return view
    }()
    
    public lazy var micBtn: AUIVoiceRoomTakeMicBtn = {
        let btn = AUIVoiceRoomTakeMicBtn()
        btn.layer.cornerRadius = 16
        btn.layer.masksToBounds = true
        btn.clickBlock = { [weak self] sender in
            if (sender.isJoined) {
                self?.roomController.leaveMic(completed: { error in
                    
                })
            }
            else {
                AVDeviceAuth.checkMicAuth { auth in
                    if auth == false {
                        return
                    }
                    if let strongSelf = self {
                        let panel = AUIVoiceRoomTakeMicPanel(frame: CGRect(x: 0, y: 0, width: strongSelf.view.av_width, height: 0))
                        panel.okBtn.clickBlock = { [weak panel] btn in
                            panel?.isUserInteractionEnabled = false
                            let hud = AVProgressHUD.showAdded(to: self!.view, animated: true)
                            hud.iconType = .loading
                            hud.labelText = "上麦中..."
                            self?.roomController.requestMic(completed: { error in
                                panel?.isUserInteractionEnabled = true
                                hud.hide(animated: true)
                                if error == nil {
                                    if panel?.muteBtn.isSelected == true {
                                        self?.roomController.switchMicrophone(off: true)
                                    }
                                    panel?.hide()
                                }
                                else {
                                    let code = AUIRoomError.getErrorCode(error: error)
                                    if code == .JoinedMicErrorForNotIndex {
                                        AVToastView.show("麦上人员已满，请稍后尝试", view: self!.view, position: .mid)
                                    }
                                    else {
                                        AVToastView.show("上麦失败：\(error!.auiMessage))", view: self!.view, position: .mid)
                                    }
                                }
                            })
                        }
                        panel.show(on: strongSelf.view, with: .clickToClose)
                    }
                }
            }
        }
        return btn
    }()
    
    open func updateMicState() {
        if let micSeatInfo = self.roomController.micSeatInfo {
            self.micBtn.isJoined = true
            self.bottomView.switchMicrophoneBtn.isSelected = micSeatInfo.isMuteMic
            self.bottomView.switchMicrophoneBtn.isEnabled = true
        }
        else {
            self.micBtn.isJoined = false
            self.bottomView.switchMicrophoneBtn.isSelected = false
            self.bottomView.switchMicrophoneBtn.isEnabled = false
        }
    }
    
    open func insertComment(text: String, sender: AUIRoomUser) {
        let model = AVCommentModel()
        model.sentContent = text
        model.useFlag = true;
        model.isMe = sender.userId == self.roomController.me.userId;
        model.isAnchor = sender.userId == self.roomController.anchor.userId;
        model.senderNick = sender.userNick
        model.senderNickColor = AVTheme.colourful_text_strong
        model.sentContentColor = AVTheme.text_strong
        model.meFlagImage = self.commentMeFlagImage
        model.anchorFlagImage = self.commentAnchorFlagImage
        model.flagOriginPoint = CGPoint(x: 0, y: -1)
        self.commentView.insertLiveComment(model)
    }
    
    open func insertTips(_ tips: String) {
        let model = AVCommentModel()
        model.sentContent = tips
        model.senderNickColor = AVTheme.colourful_text_strong
        model.sentContentColor = AVTheme.text_strong
        self.commentView.insertLiveComment(model)
    }
}

extension AUIVoiceRoomViewController {
    
    open func show(topVC: UIViewController, _ ani: Bool = true) {
        let nav = AVNavigationController(rootViewController: self)
        topVC.av_presentFullScreenViewController(nav, animated: ani)
    }
    
    open func close(_ ani: Bool = true) {
        self.navigationController?.dismiss(animated: ani)
    }
}

extension AUIVoiceRoomViewController: AVUIViewControllerInteractivePopGesture {
    
    open func disableInteractivePopGesture() -> Bool {
        return true
    }
    
}

extension AUIVoiceRoomViewController: ARTCVoiceRoomEngineDelegate {
    
    open func onDismissedRoom() {
        self.roomController.leaveRoom()
        self.close()
        if let mainWindow = UIView.av_mainWindow {
            AVToastView.show("主持人已解散房间", view: mainWindow, position: .mid)
        }
    }
    
    open func onJoinedRoom(user: AUIRoomUser) {
        self.insertTips("\(user.getFinalNick()) 进入房间")
    }
    
    open func onLeavedRoom(user: AUIRoomUser) {
        
    }
    
    open func onKickoutRoom() {
        AVAlertController.show(withTitle: "你已经被踢出房间了", message: "", needCancel: false) { _ in
            self.roomController.leaveRoom()
            self.close()
        }
    }
    
    open func onOnlineCountChanged(count: Int) {
        self.memberBtn.updateCount(count: count)
    }
    
    open func onJoinedMic(seatIndex: Int32, user: AUIRoomUser) {
        self.insertTips("\(user.getFinalNick()) 上 \(seatIndex) 号麦")
        self.micSeatView.updateMicSeatInfo(seatIndex: seatIndex)
        if user.userId == self.roomController.me.userId {
            self.updateMicState()
        }
    }
    
    open func onLeavedMic(seatIndex: Int32, user: AUIRoomUser) {
        self.insertTips("\(user.getFinalNick()) 下 \(seatIndex) 号麦")
        self.micSeatView.updateMicSeatInfo(seatIndex: seatIndex)
        if user.userId == self.roomController.me.userId {
            self.updateMicState()
            AVToastView.show("你已下麦", view: self.view, position: .mid)
        }
    }
    
    open func onMicUserStreamChanged(seatIndex: Int32, user: AUIRoomUser, publishing: Bool) {
        self.micSeatView.updateNetworkStatus(uid: user.userId)
    }
    
    open func onMicUserMicrophoneChanged(seatIndex: Int32, user: AUIRoomUser, off: Bool) {
        self.micSeatView.updateMuteMic(seatIndex: seatIndex)
        if user.userId == self.roomController.me.userId {
            if let micSeatInfo = self.roomController.micSeatInfo {
                let isSelected = self.bottomView.switchMicrophoneBtn.isSelected
                if isSelected != micSeatInfo.isMuteMic {
                    self.bottomView.switchMicrophoneBtn.isSelected = micSeatInfo.isMuteMic
                    AVToastView.show(off ? "静音模式已开启" : "静音模式已关闭", view: self.view, position: .mid)
                }
            }
        }
    }
    
    open func onNetworkStateChanged(user: AUIRoomUser, state: String) {
        if user.userId == self.roomController.me.userId {
            var status = AVNetworkStatus.fluent
            if state == AUIRoomNetworkState.Good.rawValue {
                status = AVNetworkStatus.fluent
            }
            else if state == AUIRoomNetworkState.Poor.rawValue {
                status = AVNetworkStatus.stuttering
            }
            else {
                status = AVNetworkStatus.brokenOff
            }
            self.statusView.status = status
        }
        
        self.micSeatView.updateNetworkStatus(uid: user.userId)
    }
    
    open func onMicUserSpeakStateChanged(seatIndex: Int32, isSpeaking: Bool) {
        self.micSeatView.updateSpeaking(seatIndex: seatIndex, isSpeaking: isSpeaking)
    }
    
    open func onReceivedTextMessage(user: AUIRoomUser, text: String) {
        self.insertComment(text: text, sender: user)
    }
}
