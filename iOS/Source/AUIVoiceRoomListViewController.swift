//
//  AUIVoiceRoomListViewController.swift
//  AUIVoiceRoom
//
//  Created by Bingo on 2024/2/20.
//

import UIKit
import AUIFoundation
import SDWebImage
import AUIRoomCore
import MJRefresh

@objcMembers open class AUIVoiceRoomListCell: UICollectionViewCell {
    
    public override init(frame: CGRect) {
        super.init(frame: frame)
        
        self.backgroundColor = AVTheme.fill_weak
        self.layer.cornerRadius = 12
        self.av_setLayerBorderColor(AVTheme.border_weak, borderWidth: 0.5)
        self.layer.masksToBounds = true
        
        self.contentView.addSubview(self.nameLabel)
        self.contentView.addSubview(self.idLabel)
        self.contentView.addSubview(self.lineView)
        self.contentView.addSubview(self.enterView)
        self.contentView.addSubview(self.userContainerView)
        self.contentView.addSubview(self.countLabel)
        
        self.nameLabel.snp.makeConstraints { make in
            make.left.top.equalToSuperview().offset(16)
            make.right.equalToSuperview().offset(-16)
            make.height.equalTo(18)
        }
        self.lineView.snp.makeConstraints { make in
            make.width.equalTo(23)
            make.height.equalTo(1)
            make.top.equalTo(self.nameLabel.snp.bottom).offset(4)
            make.left.equalToSuperview().offset(16)
        }
        self.idLabel.snp.makeConstraints { make in
            make.left.equalToSuperview().offset(16)
            make.right.equalToSuperview().offset(-16)
            make.top.equalTo(self.lineView.snp.bottom).offset(8)
            make.height.equalTo(18)
        }
        self.enterView.snp.makeConstraints { make in
            make.width.equalTo(40)
            make.height.equalTo(18)
            make.right.bottom.equalToSuperview().offset(-16)
        }
        
        self.userContainerView.snp.remakeConstraints { make in
            make.left.equalToSuperview().offset(16)
            make.height.equalTo(14)
            make.width.equalTo(0)
            make.centerY.equalTo(self.enterView)
        }
        
        self.countLabel.snp.makeConstraints { make in
            make.left.equalTo(self.userContainerView.snp.right).offset(4)
            make.right.equalTo(self.enterView.snp.left).offset(4)
            make.centerY.equalTo(self.enterView)
            make.height.equalTo(16)
        }
    }
    
    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public lazy var nameLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_strong
        label.font = AVTheme.mediumFont(12)
        label.text = ""
        return label
    }()
    
    public lazy var idLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_medium
        label.font = AVTheme.regularFont(12)
        label.text = ""
        return label
    }()
    
    public lazy var lineView: UIImageView = {
        let view = UIImageView()
        view.image = AUIVoiceRoomBundle.getImage("ic_list_line")
        return view
    }()
    
    public lazy var enterView: UIImageView = {
        let view = UIImageView()
        view.image = AUIVoiceRoomBundle.getImage("ic_list_enter")
        return view
    }()
    
    public lazy var countLabel: UILabel = {
        let label = UILabel()
        label.textColor = AVTheme.text_weak
        label.font = AVTheme.regularFont(10)
        label.text = "999+"
        return label
    }()
    
    public lazy var userContainerView: UIView = {
        let view = UIView()
        return view
    }()
    
    private var userViewList: [UIView] = []
    
    open var roomInfo: ARTCVoiceRoomInfo? = nil {
        didSet {
            self.userViewList.forEach { view in
                view.removeFromSuperview()
            }
            self.userViewList.removeAll()
            
            if let roomInfo = self.roomInfo {
                self.nameLabel.text = roomInfo.roomName
                self.idLabel.text = roomInfo.roomCode > 0 ? "\(roomInfo.roomCode)" : roomInfo.roomId
                if roomInfo.onlineCount > 999 {
                    self.countLabel.text = "999+"
                }
                else {
                    self.countLabel.text = "\(roomInfo.onlineCount)"
                }
                
//                let userView = UIImageView(frame: CGRect(x: 0, y: 0, width: 14, height: 14))
//                userView.layer.cornerRadius = 7
//                userView.layer.borderWidth = 1
//                userView.layer.borderColor = AVTheme.border_medium.cgColor
//                userView.layer.masksToBounds = true
//                userView.sd_setImage(with: URL(string: roomInfo.anchor.userAvatar), placeholderImage: AUIVoiceRoomBundle.getCommonImage("ic_default_avatar"))
//                self.userViewList.append(userView)
//                self.userContainerView.addSubview(userView)
                
                roomInfo.seatInfoList.forEach { seatInfo in
                    if self.userViewList.count >= 3 {
                        return
                    }
                    if let seatUser = seatInfo.user {
                        let index = self.userViewList.count
                        let userView = UIImageView(frame: CGRect(x: index * 11, y: 0, width: 14, height: 14))
                        userView.layer.cornerRadius = 7
                        userView.layer.borderWidth = 1
                        userView.layer.borderColor = AVTheme.border_medium.cgColor
                        userView.layer.masksToBounds = true
                        userView.sd_setImage(with: URL(string: seatUser.userAvatar), placeholderImage: AUIVoiceRoomBundle.getCommonImage("ic_default_avatar"))
                        self.userViewList.append(userView)
                        if index == 0 {
                            self.userContainerView.addSubview(userView)
                        }
                        else {
                            self.userContainerView.insertSubview(userView, belowSubview: self.userViewList[index - 1])
                        }
                    }
                }
                self.userContainerView.snp.remakeConstraints { make in
                    make.left.equalToSuperview().offset(16)
                    make.height.equalTo(14)
                    make.width.equalTo((self.userViewList.count - 1) * 11 + 14)
                    make.centerY.equalTo(self.enterView)
                }
                
            }
            else {
                self.nameLabel.text = ""
                self.idLabel.text = ""
                self.countLabel.text = ""
                self.userContainerView.snp.remakeConstraints { make in
                    make.left.equalToSuperview().offset(16)
                    make.height.equalTo(14)
                    make.width.equalTo(0)
                    make.centerY.equalTo(self.enterView)
                }
            }
        }
    }
}

@objcMembers open class AUIVoiceRoomListViewController: AVBaseCollectionViewController {
    
    open override func viewDidLoad() {
        super.viewDidLoad()
        
        self.titleView.text = AUIVoiceRoomBundle.getString("语音聊天室")
        self.hiddenMenuButton = true
        
        self.collectionView.register(AUIVoiceRoomListCell.self, forCellWithReuseIdentifier: "cell")

        self.contentView.addSubview(self.creatBtn)
        self.creatBtn.snp.makeConstraints { make in
            make.bottom.equalToSuperview().offset(-(UIView.av_safeBottom + 16))
            make.left.equalToSuperview().offset(72)
            make.right.equalToSuperview().offset(-72)
            make.height.equalTo(44)
        }
        
        self.setupRefreshHeader()
        self.setupLoadMoreFooter()
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            self.collectionView.mj_header?.beginRefreshing()
        }
    }
    
    open var lastPageNumber: Int = 1
    
    open var emptyView: UIImageView?
    
    public lazy var creatBtn: AVBlockButton = {
        let btn = AVBlockButton()
        btn.layer.cornerRadius = 22.0
        btn.layer.masksToBounds = true
        btn.setTitle(AUIVoiceRoomBundle.getString("创建聊天室"), for: .normal)
        btn.setBackgroundColor(AVTheme.colourful_fill_strong, for: .normal)
        btn.setBackgroundColor(AVTheme.colourful_fill_disabled, for: .disabled)
        btn.setTitleColor(AVTheme.text_strong, for: .normal)
        btn.titleLabel?.font = AVTheme.regularFont(16)
        btn.clickBlock = {[weak self] sender in
            AUIVoiceRoomManager.shared.createRoom(currVC: self)
        }
        return btn
    }()
    
    open lazy var roomList: [ARTCVoiceRoomInfo] = [ARTCVoiceRoomInfo]()
    
    open func getRoomList(pageNum: Int, pageSize: Int, completed: @escaping (_ roomInfoList: [ARTCVoiceRoomInfo], _ error: NSError?)->Void) {
        ARTCVoiceRoomEngine.getVoiceRoomList(pageNum: pageNum, pageSize: pageSize, completed: completed)
    }
}

extension AUIVoiceRoomListViewController {
    
    open override func collectionView(_ collectionView: UICollectionView, didSelectItemAt indexPath: IndexPath) {
        let roomInfo = self.roomList[indexPath.row]
        AUIVoiceRoomManager.shared.enterRoom(roomId: roomInfo.roomId, currVC: self)
    }
    
    open override func collectionView(_ collectionView: UICollectionView, cellForItemAt indexPath: IndexPath) -> UICollectionViewCell {
        let cell = self.collectionView.dequeueReusableCell(withReuseIdentifier: "cell", for: indexPath) as! AUIVoiceRoomListCell
        cell.roomInfo = self.roomList[indexPath.row]
        return cell
    }
    
    
    open override func collectionView(_ collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.roomList.count
    }
    
    open override func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAt indexPath: IndexPath) -> CGSize {
        return CGSize(width: (self.collectionView.av_width - 20 - 20 - 13) / 2.0, height: 113)
    }
    
    open override func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAt section: Int) -> UIEdgeInsets {
        return UIEdgeInsets(top: 32, left: 20, bottom: UIView.av_safeBottom + 16 + 44 + 8, right: 20)
    }
    
    open override func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumLineSpacingForSectionAt section: Int) -> CGFloat {
        return 24
    }
    
    open override func collectionView(_ collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, minimumInteritemSpacingForSectionAt section: Int) -> CGFloat {
        return 13
    }
}

extension AUIVoiceRoomListViewController {
    
    open func setupRefreshHeader() {
        let header = MJRefreshNormalHeader(refreshingTarget: self, refreshingAction: #selector(refreshRoomList))
        header.lastUpdatedTimeLabel?.isHidden = true
        header.loadingView?.style = .gray
        header.stateLabel?.font = AVTheme.regularFont(14)
        header.stateLabel?.textColor = AVTheme.text_weak
        self.collectionView.mj_header = header
    }
    
    open func setupLoadMoreFooter() {
        let footer = MJRefreshAutoNormalFooter(refreshingTarget: self, refreshingAction: #selector(loadMoreRoomList))
        footer.loadingView?.style = .gray
        footer.stateLabel?.font = AVTheme.regularFont(14)
        footer.stateLabel?.textColor = AVTheme.text_weak
        footer.setTitle("", for: .noMoreData)
        footer.setTitle("", for: .idle)
        self.collectionView.mj_footer = footer
    }
    
    @objc func refreshRoomList() {
        if self.collectionView.mj_footer!.isRefreshing {
            self.collectionView.mj_header!.endRefreshing()
            return
        }
        
        self.getRoomList(pageNum: 1, pageSize: 16) { roomInfoList, error in
            self.collectionView.mj_header!.endRefreshing()
            if error == nil {
                self.roomList.removeAll()
                self.roomList.append(contentsOf: roomInfoList)
                self.collectionView.reloadData()
                
                if self.roomList.isEmpty {
                    self.lastPageNumber = 1
                    self.collectionView.mj_footer!.endRefreshingWithNoMoreData()
                    self.showEmptyView()
                }
                else {
                    self.lastPageNumber = 2
                    self.hideEmptyView()
                    self.collectionView.mj_footer!.endRefreshing()
                }
            }
            else {
                if self.roomList.isEmpty {
                    self.collectionView.mj_footer!.endRefreshingWithNoMoreData()
                }
                AVAlertController.show("出错了：\(error!.code)")
            }
        }
    }
    
    @objc func loadMoreRoomList() {
        if self.collectionView.mj_header!.isRefreshing {
            self.collectionView.mj_footer!.endRefreshing()
            return
        }
        
        if self.lastPageNumber == 1 {
            self.collectionView.mj_footer?.endRefreshing()
            return
        }
        
        self.getRoomList(pageNum: self.lastPageNumber, pageSize: 16) { roomInfoList, error in
            self.collectionView.mj_footer!.endRefreshing()
            if error == nil {
                if roomInfoList.isEmpty {
                    self.collectionView.mj_footer?.endRefreshingWithNoMoreData()
                }
                else {
                    self.lastPageNumber = self.lastPageNumber + 1
                    self.roomList.append(contentsOf: roomInfoList)
                    self.collectionView.reloadData()
                }
            }
            else {
                AVAlertController.show("出错了：\(error!.code)")
            }
        }
    }
    
    open func showEmptyView() {
        self.hideEmptyView()
        
        let view = UIImageView()
        view.image = AUIVoiceRoomBundle.getImage("bg_list_empty")
        self.emptyView = view
        
        self.contentView.addSubview(self.emptyView!)
        self.emptyView!.snp.makeConstraints { make in
            make.height.equalTo(152)
            make.width.equalTo(192)
            make.centerX.equalToSuperview()
            make.centerY.equalToSuperview().offset(-(UIView.av_safeBottom + 16 + 44) / 2.0)
        }
    }
    
    open func hideEmptyView() {
        self.emptyView?.removeFromSuperview()
        self.emptyView = nil
    }
}
