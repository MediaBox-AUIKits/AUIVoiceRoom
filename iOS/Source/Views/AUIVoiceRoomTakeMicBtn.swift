//
//  AUIVoiceRoomTakeMicBtn.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/22.
//

import UIKit
import SnapKit
import AUIFoundation

open class AUIVoiceRoomTakeMicBtn: UIView {
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.iconView)
        self.addSubview(self.titleLabel)
        
        self.iconView.snp.makeConstraints { make in
            make.width.height.equalTo(28)
            make.left.equalTo(2)
            make.centerY.equalToSuperview()
        }
        self.titleLabel.snp.makeConstraints { make in
            make.left.equalTo(self.iconView.snp.right)
            make.right.equalToSuperview().offset(-4)
            make.top.bottom.equalToSuperview()
        }
        
        self.addGestureRecognizer(self.tapGesture)
        
        self.refreshUI()
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public lazy var iconView: UIImageView = {
        let view = UIImageView()
        view.image = AUIVoiceRoomBundle.getCommonImage("ic_mic_request")
        view.backgroundColor = .clear
        return view
    }()
    
    public lazy var titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.mediumFont(12)
        label.textAlignment = .center
        return label
    }()
    
    lazy var tapGesture: UITapGestureRecognizer = {
        let ges = UITapGestureRecognizer(target: self, action: #selector(onTap(recognizer:)))
        return ges
    }()
    
    @objc func onTap(recognizer: UIGestureRecognizer) {
        self.clickBlock?(self)
    }
    
    open func refreshUI() {
        if self.isJoined {
            self.backgroundColor = AVTheme.tsp_fill_weak
            self.titleLabel.text = AUIVoiceRoomBundle.getString("下麦")
        }
        else {
            self.backgroundColor = AVTheme.colourful_fill_strong
            self.titleLabel.text = AUIVoiceRoomBundle.getString("上麦")
        }
    }
    
    open var isJoined = false {
        didSet {
            self.refreshUI()
        }
    }
    
    open var clickBlock: ((_ sender: AUIVoiceRoomTakeMicBtn)->Void)? = nil
}
