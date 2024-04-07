//
//  AUIVoiceRoomTakeMicPanel.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/22.
//

import UIKit
import AUIFoundation

open class AUIVoiceRoomTakeMicPanel: AVBaseControllPanel {
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.headerView.isHidden = true
        
        self.addSubview(self.cancelBtn)
        self.addSubview(self.okBtn)
        self.addSubview(self.muteBtn)
        
        let width = (self.av_width - 20 * 2 - 15) / 2.0
        let top = self.av_height - UIView.av_safeBottom - 16 - 38
        self.cancelBtn.frame = CGRectMake(20, top, width, 38)
        self.okBtn.frame = CGRectMake(self.cancelBtn.av_right + 15, top, width, 38)
        self.muteBtn.frame = CGRectMake(self.av_width / 2.0 - 30, 50, 60, 60)
    }

    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override class func panelHeight() -> CGFloat {
        return 242
    }
    
    public lazy var cancelBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.setBorderColor(AVTheme.border_strong, for: .normal)
        btn.setTitle(AUIVoiceRoomBundle.getString("取消"), for: .normal)
        btn.setTitleColor(AVTheme.text_strong, for: .normal)
        btn.layer.cornerRadius = 19
        btn.layer.borderWidth = 1
        btn.titleLabel?.font = AVTheme.regularFont(14)
        btn.clickBlock = {[weak self] sender in
            self?.hide()
        }
        return btn
    }()
    
    public lazy var okBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.setBackgroundColor(AVTheme.colourful_fill_strong, for: .normal)
        btn.setTitle(AUIVoiceRoomBundle.getString("上麦"), for: .normal)
        btn.setTitleColor(AVTheme.text_strong, for: .normal)
        btn.layer.cornerRadius = 19
        btn.layer.borderWidth = 1
        btn.titleLabel?.font = AVTheme.regularFont(14)
        btn.clickBlock = {[weak self] sender in
            self?.hide()
        }
        return btn
    }()
    
    public lazy var muteBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.setImage(AUIVoiceRoomBundle.getImage("ic_take_mic_mute"), for: .normal)
        btn.setImage(AUIVoiceRoomBundle.getImage("ic_take_mic_mute_selected"), for: .selected)
        btn.layer.cornerRadius = 30
        btn.clickBlock = {[weak self] sender in
            sender.isSelected = !sender.isSelected
        }
        return btn
    }()
}
