//
//  AUIVoiceRoomSoundEffectPanel.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/26.
//

import UIKit
import AUIFoundation
import SnapKit

open class AUIVoiceRoomSoundEffectPanel: AVBaseControllPanel {

    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.titleView.text = AUIVoiceRoomBundle.getString("音效")
        
        self.contentView.addSubview(self.laughEffectView)
        self.contentView.addSubview(self.clapEffectView)
        
        self.laughEffectView.frame = CGRect(x: 0, y: 12, width: self.contentView.av_width, height: 46)
        self.clapEffectView.frame = CGRect(x: 0, y: self.laughEffectView.av_bottom, width: self.contentView.av_width, height: 46)
    }

    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override class func panelHeight() -> CGFloat {
        return 196
    }
    
    open  lazy var laughEffectView: AUIVoiceRoomSoundEffectView = {
        let view = AUIVoiceRoomSoundEffectView()
        view.titleLabel.text = AUIVoiceRoomBundle.getString("笑声")
        view.sliderView.value = 0.5
        return view
    }()
    
    open  lazy var clapEffectView: AUIVoiceRoomSoundEffectView = {
        let view = AUIVoiceRoomSoundEffectView()
        view.titleLabel.text = AUIVoiceRoomBundle.getString("掌声")
        view.sliderView.value = 0.5
        return view
    }()
}

open  class AUIVoiceRoomSoundEffectView: UIView {
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.titleLabel)
        self.addSubview(self.sliderView)
        self.addSubview(self.tryBtn)
        self.addSubview(self.applyBtn)
        
        self.applyBtn.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-20)
            make.width.equalTo(48)
            make.height.equalTo(22)
            make.centerY.equalToSuperview()
        }
        
        self.tryBtn.snp.makeConstraints { make in
            make.right.equalTo(self.applyBtn.snp.left).offset(-12)
            make.width.equalTo(48)
            make.height.equalTo(22)
            make.centerY.equalToSuperview()
        }
        
        self.titleLabel.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.width.greaterThanOrEqualTo(48)
            make.height.equalTo(22)
            make.centerY.equalToSuperview()
        }
        
        self.sliderView.snp.makeConstraints { make in
            make.left.equalTo(self.titleLabel.snp.right).offset(8)
            make.right.equalTo(self.tryBtn.snp.left).offset(-16)
            make.height.equalToSuperview()
            make.centerY.equalToSuperview()
        }
    }
    
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(12)
        label.textColor = AVTheme.text_strong
        return label
    }()
    
    public lazy var sliderView: AVSliderView = {
        let view = AVSliderView()
        return view
    }()
    
    public lazy var tryBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.layer.cornerRadius = 11
        btn.layer.masksToBounds = true
        btn.layer.borderWidth = 1.0
        btn.titleLabel?.font = AVTheme.lightFont(12)
        btn.setBorderColor(AVTheme.border_strong, for: .normal)
        btn.setBorderColor(AVTheme.colourful_border_strong, for: .selected)
        btn.setTitleColor(AVTheme.text_strong, for: .normal)
        btn.setTitleColor(AVTheme.colourful_text_strong, for: .selected)
        btn.setTitle(AUIVoiceRoomBundle.getString("试听"), for: .normal)
        btn.setTitle(AUIVoiceRoomBundle.getString("停止"), for: .selected)
        return btn
    }()
    
    public lazy var applyBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.layer.cornerRadius = 11
        btn.layer.masksToBounds = true
        btn.layer.borderWidth = 1.0
        btn.titleLabel?.font = AVTheme.lightFont(12)
        btn.setBorderColor(AVTheme.border_strong, for: .normal)
        btn.setBorderColor(AVTheme.colourful_border_strong, for: .selected)
        btn.setTitleColor(AVTheme.text_strong, for: .normal)
        btn.setTitleColor(AVTheme.colourful_text_strong, for: .selected)
        btn.setTitle(AUIVoiceRoomBundle.getString("使用"), for: .normal)
        btn.setTitle(AUIVoiceRoomBundle.getString("停止"), for: .selected)
        return btn
    }()
}
