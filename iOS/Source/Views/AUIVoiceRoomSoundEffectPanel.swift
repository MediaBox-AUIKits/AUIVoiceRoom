//
//  AUIVoiceRoomSoundEffectPanel.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/26.
//

import UIKit
import AUIFoundation
import SnapKit

public typealias AUIVoiceRoomAudioEffectPlayBlock = (_ item: AUIVoiceRoomAudioEffectItem, _ isPlaying: Bool) -> Void
public typealias AUIVoiceRoomAudioEffectVolumeBlock = (_ item: AUIVoiceRoomAudioEffectItem) -> Void


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
        view.tryBtn.clickBlock = {[weak self] sender in
            self?.tryPlayBlock?(self!.laughEffectView.item!, sender.isSelected)
        }
        view.applyBtn.clickBlock = {[weak self] sender in
            self?.applyPlayBlock?(self!.laughEffectView.item!, sender.isSelected)
        }
        view.sliderView.onValueChangedByGesture = {[weak self] value, gesture in
            if gesture.state == .ended || gesture.state == .cancelled || gesture.state == .failed {
                self?.volumeBlock?(self!.laughEffectView.item!)
            }
        }
        
        view.item = AUIVoiceRoomSoundEffectPanel.audioEffectItems[0]
        return view
    }()
    
    open  lazy var clapEffectView: AUIVoiceRoomSoundEffectView = {
        let view = AUIVoiceRoomSoundEffectView()
        view.tryBtn.clickBlock = {[weak self] sender in
            self?.tryPlayBlock?(self!.clapEffectView.item!, sender.isSelected)
        }
        view.applyBtn.clickBlock = {[weak self] sender in
            self?.applyPlayBlock?(self!.clapEffectView.item!, sender.isSelected)
        }
        view.sliderView.onValueChangedByGesture = {[weak self] value, gesture in
            if gesture.state == .ended || gesture.state == .cancelled || gesture.state == .failed {
                self?.volumeBlock?(self!.clapEffectView.item!)
            }
        }
        
        view.item = AUIVoiceRoomSoundEffectPanel.audioEffectItems[1]
        return view
    }()
    
    static var audioEffectItems: [AUIVoiceRoomAudioEffectItem] = {
        let item1 = AUIVoiceRoomAudioEffectItem()
        item1.effectId = 1
        item1.effectName = AUIVoiceRoomBundle.getString("开场")!
        item1.localPath = "kaichang.aac"
        
        let item2 = AUIVoiceRoomAudioEffectItem()
        item2.effectId = 2
        item2.effectName = AUIVoiceRoomBundle.getString("掌声")!
        item2.localPath = "clap.aac"
        return [item1, item2]
    }()
    
    open var tryPlayBlock: AUIVoiceRoomAudioEffectPlayBlock? = nil
    open var applyPlayBlock: AUIVoiceRoomAudioEffectPlayBlock? = nil
    open var volumeBlock: AUIVoiceRoomAudioEffectVolumeBlock? = nil
}

open class AUIVoiceRoomAudioEffectItem: NSObject {
    open var effectId: Int32 = 0
    open var effectName: String = ""
    open var volume: Int32 = 50
    open var localPath: String = ""
}

open class AUIVoiceRoomSoundEffectView: UIView {
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.titleLabel)
        self.addSubview(self.sliderView)
        self.addSubview(self.volumeLabel)
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
        
        self.volumeLabel.snp.makeConstraints { make in
            make.centerX.equalTo(self.sliderView.snp.left).offset(7)
            make.centerY.equalTo(self.sliderView.snp.top)
            make.height.equalTo(16)
            make.width.equalTo(48)
        }
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        DispatchQueue.main.async {
            self.volumeLabel.transform = CGAffineTransform(translationX: (self.sliderView.av_width - 14) * CGFloat(self.sliderView.value), y: 0)
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
    
    public lazy var volumeLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(12)
        label.textAlignment = .center
        label.textColor = AVTheme.text_strong
        label.isUserInteractionEnabled = false
        return label
    }()
    
    public lazy var sliderView: AVSliderView = {
        let view = AVSliderView()
        view.onValueChanged = {[weak self] value in
            let volume = Int32(value * 100)
            self?.item?.volume = volume
            self?.volumeLabel.text = "\(volume)"
            self?.volumeLabel.transform = CGAffineTransform(translationX: (self!.sliderView.av_width - 14) * CGFloat(value), y: 0)
        }
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
    
    public var item: AUIVoiceRoomAudioEffectItem? {
        didSet {
            if let item = self.item {
                self.titleLabel.text = item.effectName
                self.sliderView.value = Float(item.volume) / 100.0
            }
        }
    }
}
