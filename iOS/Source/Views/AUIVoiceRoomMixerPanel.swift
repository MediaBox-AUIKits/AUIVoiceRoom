//
//  AUIVoiceRoomMixerPanel.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/26.
//

import UIKit
import AUIFoundation

open class AUIVoiceRoomMixerPanel: AVBaseControllPanel {

    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.titleView.text = AUIVoiceRoomBundle.getString("调音台")
        
        self.contentView.addSubview(self.iemLabel)
        self.contentView.addSubview(self.switchIEMBtn)
        self.contentView.addSubview(self.mixEffectView)
        self.contentView.addSubview(self.soundEffectView)
        
        self.iemLabel.frame = CGRect(x: 20, y: 18, width: 48, height: 18)
        self.switchIEMBtn.center = CGPoint(x: self.iemLabel.av_right + 8 + self.switchIEMBtn.av_width / 2.0, y: self.iemLabel.av_centerY)
        self.mixEffectView.frame = CGRect(x: 0, y: self.iemLabel.av_bottom + 24, width: self.contentView.av_width, height: 90)
        self.soundEffectView.frame = CGRect(x: 0, y: self.mixEffectView.av_bottom + 24, width: self.contentView.av_width, height: 90)
        
        self.mixEffectView.selectedItem = self.mixEffectView.itemList?.first
        self.soundEffectView.selectedItem = self.soundEffectView.itemList?.first
    }

    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override class func panelHeight() -> CGFloat {
        return 370
    }
    
    public lazy var iemLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(12)
        label.textColor = AVTheme.text_strong
        label.text = AUIVoiceRoomBundle.getString("耳返")
        return label
    }()
    
    public lazy var switchIEMBtn: UISwitch = {
        let btn = UISwitch()
        btn.onTintColor = AVTheme.colourful_fg_strong
        btn.tintColor = AVTheme.fill_weak
        return btn
    }()
    
    public lazy var mixEffectView: AUIVoiceRoomMixerEffectView = {
        let view = AUIVoiceRoomMixerEffectView()
        view.titleLabel.text = AUIVoiceRoomBundle.getString("混响")
        view.itemList = [
            AUIVoiceRoomMixerEffectItem("0", "无", "ic_mix_empty"),
            AUIVoiceRoomMixerEffectItem("1", "人声l", "ic_mix_1"),
            AUIVoiceRoomMixerEffectItem("2", "人声ll", "ic_mix_2"),
            AUIVoiceRoomMixerEffectItem("3", "澡堂", "ic_mix_3"),
            AUIVoiceRoomMixerEffectItem("4", "明亮小房间", "ic_mix_4"),
            AUIVoiceRoomMixerEffectItem("5", "黑暗小房间", "ic_mix_5"),
            AUIVoiceRoomMixerEffectItem("6", "中等房间", "ic_mix_6"),
            AUIVoiceRoomMixerEffectItem("7", "大房间", "ic_mix_7"),
            AUIVoiceRoomMixerEffectItem("8", "教堂走廊", "ic_mix_8"),
        ]
        return view
    }()
    
    public lazy var soundEffectView: AUIVoiceRoomMixerEffectView = {
        let view = AUIVoiceRoomMixerEffectView()
        view.titleLabel.text = AUIVoiceRoomBundle.getString("变声")
        view.itemList = [
            AUIVoiceRoomMixerEffectItem("0", "无", "ic_sound_effect_empty"),
            AUIVoiceRoomMixerEffectItem("1", "萝莉", "ic_sound_effect_1"),
            AUIVoiceRoomMixerEffectItem("2", "大叔", "ic_sound_effect_2"),
            AUIVoiceRoomMixerEffectItem("3", "回音", "ic_sound_effect_3"),
            AUIVoiceRoomMixerEffectItem("4", "KTV", "ic_sound_effect_4"),
            AUIVoiceRoomMixerEffectItem("5", "小黄人", "ic_sound_effect_5"),
            AUIVoiceRoomMixerEffectItem("6", "机器人", "ic_sound_effect_6"),
            AUIVoiceRoomMixerEffectItem("7", "大魔王", "ic_sound_effect_7"),
            AUIVoiceRoomMixerEffectItem("8", "方言", "ic_sound_effect_8"),
        ]
        return view
    }()
}

open class AUIVoiceRoomMixerEffectItem: NSObject {
    
    public init(_ id: String, _ titleKey: String, _ iconPath: String) {
        self.id = id
        self.title = AUIVoiceRoomBundle.getString(titleKey)
        self.icon = AUIVoiceRoomBundle.getImage(iconPath)
        
        super.init()
    }
    
    public let id:String
    public let title: String?
    public let icon: UIImage?
    open var isSelected: Bool = false
}

open class AUIVoiceRoomMixerEffectView: UIView {
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.titleLabel)
        self.addSubview(self.collectionView)
        
        self.titleLabel.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalToSuperview().offset(-20)
            make.height.equalTo(18)
            make.top.equalTo(0)
        }
        self.collectionView.snp.makeConstraints { make in
            make.left.equalTo(0)
            make.right.equalToSuperview()
            make.top.equalTo(self.titleLabel.snp.bottom).offset(8)
            make.bottom.equalToSuperview()
        }
    }
    
    required public init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(12)
        label.textColor = AVTheme.text_strong
        return label
    }()
    
    public lazy var collectionView: UICollectionView = {
        let layout = UICollectionViewFlowLayout()
        layout.scrollDirection = .horizontal
        let view = UICollectionView(frame: CGRectZero, collectionViewLayout: layout)
        view.backgroundColor = .clear
        view.delegate = self
        view.dataSource = self
        view.bounces = true
        view.alwaysBounceHorizontal = false
        view.showsHorizontalScrollIndicator = false
        view.register(AUIVoiceRoomMixerEffectCell.self, forCellWithReuseIdentifier: "cell")
        return view
    }()
    
    open var itemList: [AUIVoiceRoomMixerEffectItem]?
    open var selectedItem: AUIVoiceRoomMixerEffectItem? {
        willSet {
            self.selectedItem?.isSelected = false
        }
        didSet {
            self.selectedItem?.isSelected = true
            self.collectionView.reloadData()
        }
    }
}

extension AUIVoiceRoomMixerEffectView: UICollectionViewDataSource, UICollectionViewDelegate, UICollectionViewDelegateFlowLayout {
    
    open func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.itemList?.count ?? 0
    }
    
    open func numberOfSections(in collectionView: UICollectionView) -> Int {
        return 1
    }
    
    open func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = self.collectionView.dequeueReusableCell(withReuseIdentifier: "cell", for: indexPath) as! AUIVoiceRoomMixerEffectCell
        cell.item = self.itemList![indexPath.row]
        return cell
    }
    
    open func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: 56, height: 64)
    }
    
    open func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        return UIEdgeInsets(top: 0, left: 12, bottom: 0, right: 12)
    }
    
    open func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    open func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        self.selectedItem = self.itemList?[indexPath.row]
    }
}

open class AUIVoiceRoomMixerEffectCell: UICollectionViewCell {
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.contentView.addSubview(self.iconView)
        self.contentView.addSubview(self.titleLabel)
        self.iconView.snp.makeConstraints { make in
            make.width.height.equalTo(40)
            make.top.equalTo(0)
            make.centerX.equalToSuperview()
        }
        self.titleLabel.snp.makeConstraints { make in
            make.left.right.bottom.equalToSuperview()
            make.height.equalTo(16)
        }
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(10)
        label.textColor = AVTheme.text_strong
        label.textAlignment = .center
        return label
    }()
    
    public lazy var iconView: UIImageView = {
        let view = UIImageView()
        view.layer.cornerRadius = 6
        view.clipsToBounds = true
        view.backgroundColor = .clear
        view.layer.borderWidth = 0
        view.layer.borderColor = AVTheme.colourful_border_strong.cgColor
        return view
    }()
    
    open var item: AUIVoiceRoomMixerEffectItem? {
        didSet {
            self.titleLabel.text = self.item?.title
            self.iconView.image = self.item?.icon
            self.updateSelection()
        }
    }
    
    open func updateSelection() {
        if self.item != nil && self.item!.isSelected == true {
            self.iconView.layer.borderWidth = 1.0
        }
        else {
            self.iconView.layer.borderWidth = 0.0
        }
    }
}
