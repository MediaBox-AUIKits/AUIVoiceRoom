//
//  AUIVoiceRoomSpeakingAnimationView.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/26.
//

import UIKit
import AUIFoundation

open class AUIVoiceRoomSpeakingAnimationView: UIView {

    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.addSubview(self.firstView)
        self.addSubview(self.secondView)
        self.firstView.snp.makeConstraints { make in
            make.left.width.top.bottom.equalToSuperview()
        }
        self.secondView.snp.makeConstraints { make in
            make.left.width.top.bottom.equalToSuperview()
        }
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    open override func layoutSubviews() {
        super.layoutSubviews()
        
        self.firstView.layer.cornerRadius = self.av_width / 2.0
        self.secondView.layer.cornerRadius = self.av_width / 2.0
    }
    
    public lazy var firstView: UIView = {
        let view = UIView()
        view.backgroundColor = AVTheme.fill_infrared.withAlphaComponent(0.2)
        return view
    }()
    
    public lazy var secondView: UIView = {
        let view = UIView()
        view.backgroundColor = AVTheme.fill_infrared.withAlphaComponent(0.2)
        return view
    }()
    
    private var isAnimation: Bool = false
    
    open func startAnimation() {
        if self.isAnimation == true {
            return
        }
        self.isAnimation = true
        let time = 0.3
        let scale = 1.25
        self.firstView.transform = CGAffineTransform.identity
        UIView.animate(withDuration: time, delay: 0, options: .repeat) {
            self.firstView.transform = CGAffineTransform(scaleX: scale, y: scale)
        }
        self.secondView.transform = CGAffineTransform.identity
        UIView.animate(withDuration: time, delay: time / 2.0, options: .repeat) {
            self.secondView.transform = CGAffineTransform(scaleX: scale, y: scale)
        }
    }
    
    open func stopAnimation() {
        self.isAnimation = false
        self.firstView.layer.removeAllAnimations()
        self.secondView.layer.removeAllAnimations()
        self.firstView.transform = CGAffineTransform.identity
        self.secondView.transform = CGAffineTransform.identity
    }
}
