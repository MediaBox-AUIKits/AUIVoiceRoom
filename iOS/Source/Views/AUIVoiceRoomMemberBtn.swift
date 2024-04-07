//
//  AUIVoiceRoomTakeMicBtn.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/22.
//

import UIKit
import SnapKit
import AUIFoundation

open class AUIVoiceRoomMemberBtn: UIView {
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.backgroundColor = AVTheme.tsp_fill_weak

        self.addSubview(self.iconView)
        self.addSubview(self.countLabel)
        
        self.iconView.snp.makeConstraints { make in
            make.width.height.equalTo(20)
            make.right.equalTo(-8)
            make.centerY.equalToSuperview()
        }
        self.countLabel.snp.makeConstraints { make in
            make.right.equalTo(self.iconView.snp.left).offset(-4)
            make.left.equalToSuperview().offset(10)
            make.top.bottom.equalToSuperview()
        }
        
        self.addGestureRecognizer(self.tapGesture)
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public lazy var iconView: UIImageView = {
        let view = UIImageView()
        view.image = AUIVoiceRoomBundle.getCommonImage("ic_default_avatar")
        view.backgroundColor = .clear
        view.layer.cornerRadius = 10
        view.layer.masksToBounds = true
        return view
    }()
    
    public lazy var countLabel: UILabel = {
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
    
    open var clickBlock: ((_ sender: AUIVoiceRoomMemberBtn)->Void)? = nil
    
    open func updateCount(count: Int) {
        var text = "\(count)"
        if count > 10000 {
            text = String(format: "%.1f万", Double(count) / 10000.0)
        }
        self.countLabel.text = text
    }
}
