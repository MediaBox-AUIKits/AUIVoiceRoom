//
//  AUIVoiceRoomMusicPanel.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/26.
//

import UIKit
import AUIFoundation
import SnapKit
import AUIRoomCore

public typealias AUIVoiceRoomMusicPlayBlock = (_ item: AUIVoiceRoomMusicItem, _ volume: Int32, _ isPlaying: Bool) -> Void
public typealias AUIVoiceRoomMusicVolumeBlock = (_ value: Int32) -> Void


open class AUIVoiceRoomMusicPanel: AVBaseCollectionControllPanel {

    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.titleView.text = AUIVoiceRoomBundle.getString("背景音乐")
        
        self.contentView.addSubview(self.recordingVolumeView)
        self.contentView.addSubview(self.musicVolumeView)
        
        self.recordingVolumeView.frame = CGRect(x: 0, y: 12, width: self.contentView.av_width, height: 46)
        self.musicVolumeView.frame = CGRect(x: 0, y: self.recordingVolumeView.av_bottom, width: self.contentView.av_width, height: 46)
        
        self.collectionView.frame = CGRect(x: 0, y: self.musicVolumeView.av_bottom, width: self.contentView.av_width, height: self.contentView.av_height - self.musicVolumeView.av_bottom)
        self.collectionView.register(AUIVoiceRoomMusicCell.self, forCellWithReuseIdentifier: "cell")
    }

    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override class func panelHeight() -> CGFloat {
        return 320
    }
    
    open  lazy var recordingVolumeView: AUIVoiceRoomVolumeView = {
        let view = AUIVoiceRoomVolumeView()
        view.titleLabel.text = AUIVoiceRoomBundle.getString("人声音量")
        view.sliderView.value = 0.5
        view.sliderView.onValueChangedByGesture = {[weak self] value, gesture in
            if gesture.state == .ended || gesture.state == .cancelled || gesture.state == .failed {
                self?.recordingVolumeBlock?(Int32(value * 100))
            }
        }
        return view
    }()
    
    open  lazy var musicVolumeView: AUIVoiceRoomVolumeView = {
        let view = AUIVoiceRoomVolumeView()
        view.titleLabel.text = AUIVoiceRoomBundle.getString("音乐音量")
        view.sliderView.value = 0.5
        view.sliderView.onValueChangedByGesture = {[weak self] value, gesture in
            if gesture.state == .ended || gesture.state == .cancelled || gesture.state == .failed {
                self?.musicVolumeBlock?(Int32(value * 100))
            }
        }
        return view
    }()
    
    open lazy var musicList: [AUIVoiceRoomMusicItem] = {
        var list = [AUIVoiceRoomMusicItem]()
        let item1 = AUIVoiceRoomMusicItem()
        item1.songId = 1
        item1.songName = "家庭蒙太奇"
        item1.singerName = "未知歌手"
        item1.localPath = "家庭蒙太奇.mp3"
        list.append(item1)
        
        let item2 = AUIVoiceRoomMusicItem()
        item2.songId = 2
        item2.songName = "刚刚呼吸"
        item2.singerName = "未知歌手"
        item2.localPath = "刚刚呼吸.mp3"
        list.append(item2)
        
        let item3 = AUIVoiceRoomMusicItem()
        item3.songId = 3
        item3.songName = "Seagull"
        item3.singerName = "未知歌手"
        item3.localPath = "Seagull.mp3"
        list.append(item3)
        
        return list
    }()
    
    open var tryPlayBlock: AUIVoiceRoomMusicPlayBlock? = nil
    open var applyPlayBlock: AUIVoiceRoomMusicPlayBlock? = nil
    open var recordingVolumeBlock: AUIVoiceRoomMusicVolumeBlock? = nil
    open var musicVolumeBlock: AUIVoiceRoomMusicVolumeBlock? = nil
    
    open var playingStatus: ARTCVoiceRoomAudioPlayingStatus? {
        didSet {
            self.musicVolumeView.sliderView.value = self.playingStatus == nil ? 0.5 : Float(self.playingStatus!.volume) / 100.0
            self.collectionView.reloadData()
        }
    }
    
    open func updateRecordingVolume(volume: Int32) {
        self.recordingVolumeView.sliderView.value = Float(volume) / 100.0
    }
}

extension AUIVoiceRoomMusicPanel {
    
    open override func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.musicList.count
    }
    
    open override func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: self.contentView.av_width, height: 46)
    }
    
    open override func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    open override func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 0
    }
    
    open override func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        return UIEdgeInsets(top: 0, left: 0, bottom: UIView.av_safeBottom, right: 0)
    }
    
    open override func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = self.collectionView.dequeueReusableCell(withReuseIdentifier: "cell", for: indexPath) as! AUIVoiceRoomMusicCell
        cell.item = self.musicList[indexPath.row]
        cell.updatePlayingStatus(status: self.playingStatus)
        cell.tryBtn.clickBlock = {[weak self, weak cell] sender in
            if let item = cell?.item {
                self?.tryPlayBlock?(item, Int32((self == nil ? 0.5 : self!.musicVolumeView.sliderView.value) * 100), sender.isSelected)
            }
        }
        cell.applyBtn.clickBlock = {[weak self, weak cell] sender in
            if let item = cell?.item {
                self?.applyPlayBlock?(item, Int32((self == nil ? 0.5 : self!.musicVolumeView.sliderView.value) * 100), sender.isSelected)
            }
        }
        return cell
    }
}

extension AUIVoiceRoomMusicPanel: ARTCVoiceRoomEngineDelegate {
    
    public func onBackgroundMusicStatusChanged(status: ARTCVoiceRoomAudioPlayingStatus) {
        self.collectionView.reloadData()
    }
    
}


open class AUIVoiceRoomVolumeView: UIView {
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.titleLabel)
        self.addSubview(self.sliderView)
        self.addSubview(self.volumeLabel)
        
        self.volumeLabel.snp.makeConstraints { make in
            make.right.equalToSuperview().offset(-20)
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
            make.right.equalTo(self.volumeLabel.snp.left)
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
        view.onValueChanged = {[weak self] value in
            self?.volumeLabel.text = "\(Int(value * 100))"
        }
        return view
    }()
    
    public lazy var volumeLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(12)
        label.textAlignment = .center
        label.textColor = AVTheme.text_strong
        return label
    }()
    
    
}

open class AUIVoiceRoomMusicItem: NSObject {
    open var songId: Int32 = 0
    open var songName: String = ""
    open var singerName: String = ""
    open var localPath: String = ""
}

open class AUIVoiceRoomMusicCell: UICollectionViewCell {
    override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.titleLabel)
        self.addSubview(self.infoLabel)
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
            make.top.equalTo(6)
            make.right.equalTo(self.tryBtn.snp.left).offset(-12)
            make.height.equalTo(18)
        }
        
        self.infoLabel.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.top.equalTo(self.titleLabel.snp.bottom)
            make.right.equalTo(self.titleLabel)
            make.height.equalTo(16)
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
    
    public lazy var infoLabel: UILabel = {
        let label = UILabel()
        label.font = AVTheme.regularFont(10)
        label.textColor = AVTheme.text_ultraweak
        return label
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
    
    public var item: AUIVoiceRoomMusicItem? {
        didSet {
            self.titleLabel.text = self.item?.songName
            self.infoLabel.text = self.item?.singerName
        }
    }
    
    public func updatePlayingStatus(status: ARTCVoiceRoomAudioPlayingStatus?) {
        if let item = self.item, let status = status {
            if item.songId == status.audioId {
                if status.onlyLocalPlay {
                    self.tryBtn.isSelected = status.playState == .Started
                    self.applyBtn.isSelected = false
                }
                else {
                    self.applyBtn.isSelected = status.playState == .Started
                    self.tryBtn.isSelected = false
                }
                return
            }
        }
        self.tryBtn.isSelected = false
        self.applyBtn.isSelected = false
    }
}
