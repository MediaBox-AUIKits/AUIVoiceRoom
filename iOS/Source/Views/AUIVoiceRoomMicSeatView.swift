//
//  AUIVoiceRoomMicSeatView.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/21.
//

import UIKit
import SnapKit
import AUIFoundation
import SDWebImage
import AUIRoomCore


open class AUIVoiceRoomMicSeatCell: UIView {
    
    public convenience init(_ seatInfo: AUIVoiceRoomMicSeatInfo) {
        self.init(frame: CGRect.zero, seatInfo: seatInfo)
    }
    
    public init(frame: CGRect, seatInfo: AUIVoiceRoomMicSeatInfo) {
        self.seatInfo = seatInfo
        super.init(frame: frame)
        
        
        self.addSubview(self.avatarView)
        self.avatarView.snp.makeConstraints { make in
            make.width.height.equalTo(48)
            make.top.equalToSuperview().offset(6)
            make.centerX.equalToSuperview()
        }
        
        self.insertSubview(self.speakingAniView, belowSubview: self.avatarView)
        self.speakingAniView.snp.makeConstraints { make in
            make.width.height.equalTo(48)
            make.centerX.centerY.equalTo(self.avatarView)
        }
        
        let nameContainerView = UIView()
        self.addSubview(nameContainerView)
        nameContainerView.snp.makeConstraints { make in
            make.height.equalTo(18)
            make.bottom.equalToSuperview()
            make.width.lessThanOrEqualToSuperview()
            make.centerX.equalToSuperview()
        }

        nameContainerView.addSubview(self.networkStatusView)
        self.networkStatusView.snp.makeConstraints { make in
            make.left.equalToSuperview()
            make.width.height.equalTo(12)
            make.centerY.equalToSuperview()
        }
        
        nameContainerView.addSubview(self.nameLabel)
        self.nameLabel.snp.makeConstraints { make in
            make.top.bottom.right.equalToSuperview()
            make.left.equalTo(self.networkStatusView.snp.right).offset(2)
        }
        
        self.addSubview(self.roleLabel)
        self.roleLabel.snp.makeConstraints { make in
            make.width.equalTo(21)
            make.height.equalTo(12)
            make.bottom.equalTo(self.avatarView)
            make.centerX.equalTo(self.avatarView)
        }
        
        self.addSubview(self.muteView)
        self.muteView.snp.makeConstraints { make in
            make.width.height.equalTo(14)
            make.right.bottom.equalTo(self.avatarView)
        }
        
        self.updateSeatUser(startIndex: 0)
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public lazy var speakingAniView: AUIVoiceRoomSpeakingAnimationView = {
        let view = AUIVoiceRoomSpeakingAnimationView()
        view.isHidden = true
        return view
    }()
    
    public lazy var avatarView: UIImageView = {
        let view = UIImageView()
        view.layer.cornerRadius = 24
        view.layer.masksToBounds = true
        view.backgroundColor = .clear
        return view
    }()
    
    public lazy var muteView: UIImageView = {
        let view = UIImageView()
        view.layer.cornerRadius = 7
        view.layer.masksToBounds = true
        view.image = AUIVoiceRoomBundle.getCommonImage("ic_mic_seat_mute")
        return view
    }()
    
    public lazy var networkStatusView: UIImageView = {
        let view = UIImageView()
        return view
    }()
    
    public lazy var nameLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.regularFont(12)
        return label
    }()
    
    public lazy var roleLabel: UILabel = {
        let label = UILabel()
        label.textColor = UIColor.white
        label.textAlignment = .center
        label.font = AVTheme.mediumFont(7)
        label.layer.cornerRadius = 6.0
        label.layer.masksToBounds = true
        label.isHidden = true
        return label
    }()
    
    public let seatInfo: AUIVoiceRoomMicSeatInfo
    
    
    open func updateSeatUser(startIndex: Int32) {
        if let user = self.seatInfo.user {
            self.nameLabel.text = user.userNick
            self.avatarView.sd_setImage(with: URL(string: user.userAvatar), placeholderImage: AUIVoiceRoomBundle.getCommonImage("ic_default_avatar"))
        }
        else {
            self.nameLabel.text = "\(self.seatInfo.index + startIndex)" + (AUIVoiceRoomBundle.getString("号麦") ?? "号麦")
            self.avatarView.image = AUIVoiceRoomBundle.getCommonImage("ic_mic_seat")
        }
        self.updateSeatUserMuteMic()
        self.updateSeatUserNetworkStatus()
        self.updateRole()
    }
    
    open func updateSeatUserMuteMic() {
        if self.seatInfo.isMuteMic == true {
            self.speakingAniView.stopAnimation()
            self.speakingAniView.isHidden = true
            self.muteView.isHidden = false
        }
        else {
            self.speakingAniView.isHidden = !self.seatInfo.isJoin
            self.muteView.isHidden = true
        }
    }
    
    open func updateSeatUserNetworkStatus() {
        
        var status = ARTCRoomNetworkState.Unknow
        if self.seatInfo.isJoin {
            if self.seatInfo.isPublishStream == false {
                status = .Bad
            }
            else {
                status = self.seatInfo.networkStatus
            }
        }
        
        if status != .Unknow {
            self.networkStatusView.isHidden = false
            self.networkStatusView.image = AUIVoiceRoomBundle.getCommonImage("ic_network_status_\(status.rawValue)")
            self.networkStatusView.snp.remakeConstraints { make in
                make.left.equalToSuperview()
                make.width.height.equalTo(12)
                make.centerY.equalToSuperview()
            }
            self.nameLabel.snp.remakeConstraints { make in
                make.top.bottom.right.equalToSuperview()
                make.left.equalTo(self.networkStatusView.snp.right).offset(2)
            }
        }
        else {
            self.networkStatusView.isHidden = true
            self.networkStatusView.snp.remakeConstraints { make in
                make.left.equalToSuperview()
                make.width.height.equalTo(12)
                make.centerY.equalToSuperview()
            }
            self.nameLabel.snp.remakeConstraints { make in
                make.top.bottom.right.left.equalToSuperview()
            }
        }
    }
    
    open func updateRole() {
        if self.seatInfo.isAnchor {
            self.roleLabel.isHidden = false
            self.roleLabel.text = AUIVoiceRoomBundle.getString("主持")
            self.roleLabel.backgroundColor = UIColor.av_color(withHexString: "#00BCD4")
        }
        else if self.seatInfo.isMe {
            self.roleLabel.isHidden = false
            self.roleLabel.text = AUIVoiceRoomBundle.getString("自己")
            self.roleLabel.backgroundColor = UIColor.av_color(withHexString: "#0077FA")
        }
        else {
            self.roleLabel.isHidden = true
        }
    }
}

open class AUIVoiceRoomMicSeatView: UIView {
    
    public init(roomInfo: ARTCVoiceRoomInfo) {
        self.roomInfo = roomInfo
        super.init(frame: CGRect.zero)
        
        self.addSubview(self.anchorSeatView)
        for seat in self.seatViewList {
            self.addSubview(seat)
        }
        
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        let length = 80.0
        let margin = 18.0
        let count_per_row = 4
        var startRow = 0

        var list = Array(self.seatViewList)
        if self.isAnchorSeatViewMiddle == true {
            self.anchorSeatView.frame = CGRect(x: 0, y: 0, width: length, height: length)
            self.anchorSeatView.av_centerX = self.av_width / 2.0
            startRow = 1
        }
        else  {
            list.insert(self.anchorSeatView, at: 0)
        }
        
        for (index, seat) in list.enumerated() {
            self.addSubview(seat)
            let row = Double(index / count_per_row + startRow)
            let col = Double(index % count_per_row)
            seat.frame = CGRect(x: 0, y: 0, width: length, height: length)
            seat.av_centerY = row * (length + margin) + length / 2.0
            let width_per_col = (self.av_width - 14 * 2) / Double(count_per_row)
            seat.av_centerX = 14 + width_per_col * (col + 0.5)
        }
    }
    
    public let roomInfo: ARTCVoiceRoomInfo
    public var isAnchorSeatViewMiddle: Bool = true {
        didSet {
            self.seatViewList.forEach { cell in
                cell.updateSeatUser(startIndex: self.isAnchorSeatViewMiddle ? 0 : 1)
            }
            self.setNeedsLayout()
        }
    }
    
    public lazy var anchorSeatView: AUIVoiceRoomMicSeatCell = {
        let view = AUIVoiceRoomMicSeatCell(self.roomInfo.anchorSeatInfo)
        return view
    }()
    
    public lazy var seatViewList: [AUIVoiceRoomMicSeatCell] = {
        var list = [AUIVoiceRoomMicSeatCell]()
        self.roomInfo.seatInfoList.forEach { seatInfo in
            list.append(AUIVoiceRoomMicSeatCell(seatInfo))
        }
        return list
    }()
    
    open func findMicSeatCell(uid: String) -> AUIVoiceRoomMicSeatCell? {
        if (self.anchorSeatView.seatInfo.user?.userId == uid) {
            return self.anchorSeatView
        }
        return self.seatViewList.first { cell in
            if let user = cell.seatInfo.user {
                return user.userId == uid
            }
            return false
        }
    }
    
    open func findMicSeatCell(seatIndex: Int32) -> AUIVoiceRoomMicSeatCell? {
        if (self.anchorSeatView.seatInfo.index == seatIndex) {
            return self.anchorSeatView
        }
        return self.seatViewList.first { cell in
            return cell.seatInfo.index == seatIndex
        }
    }
    
    open func updateMicSeatInfo(seatIndex: Int32) {
        if let seatCell = self.findMicSeatCell(seatIndex: seatIndex) {
            seatCell.updateSeatUser(startIndex: self.isAnchorSeatViewMiddle ? 0 : 1)
        }
    }
    
    open func updateMuteMic(seatIndex: Int32) {
        if let seatCell = self.findMicSeatCell(seatIndex: seatIndex) {
            seatCell.updateSeatUserMuteMic()
        }
    }
    
    open func updateStreamState(seatIndex: Int32) {
        if let seatCell = self.findMicSeatCell(seatIndex: seatIndex) {
            seatCell.updateSeatUserNetworkStatus()
        }
    }
    
    open func updateNetworkStatus(uid: String) {
        if let seatCell = self.findMicSeatCell(uid: uid) {
            seatCell.updateSeatUserNetworkStatus()
        }
    }
    
    open func updateSpeaking(seatIndex: Int32, isSpeaking: Bool) {
        if let seatCell = self.findMicSeatCell(seatIndex: seatIndex) {
            if isSpeaking == true && seatCell.speakingAniView.isHidden == false {
                seatCell.speakingAniView.startAnimation()
            }
            else {
                seatCell.speakingAniView.stopAnimation()
            }
        }
    }
}
