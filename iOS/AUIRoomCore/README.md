# AUIRoomCore
阿里云 · AUI Kits 互娱与通话场景API

## 介绍
AUI Kits 互娱与通话场景API，是基于阿里云提供的跨平台音视频实时通信服务，为业务方提供语音聊天、KTV、通话、连麦等多人实时互动等场景的能力，借助通信的稳定、流畅、灵活的产品能力，以低代码的方式助力业务方快速发布应用。

## 源码说明

### 源码结构
```
├── AUIRoomCore
│   ├── AUIRoomCore.podspec                // pod描述文件
│       ├── Core                           // 场景化API
│           ├── Model                      // 房间、用户等模型
│           ├── Service                    // 房间&rtc服务
│           ├── VoiceRoom                  // 语聊场景化API
│           ├── KaraokeRoom                // KTV场景化API
│       ├── Imp                            // 提供房间服务接口的默认实现
│       ├── Login                          // 用于阿里AUI Demo的的登录，客户不需要用到
│   ├── README.md                          // Readme  

```

### 环境要求
- Xcode 14.0 及以上版本，推荐使用最新正式版本
- CocoaPods 1.9.3 及以上版本
- 准备 iOS 10.0 及以上版本的真机

### 前提条件
需要开通应用，并且在你的服务端上开发相关接口或直接部署提供的Server源码，详情参考官网文档[前置准备](https://help.aliyun.com/zh/apsara-video-sdk/use-cases/pre-preparation)

## 快速集成
以下几个步骤快速集成互娱与通话场景API到你的APP中

- 导入AUIRoomCore：仓库代码下载后，拷贝AUIRoomCore文件夹到你的APP代码目录下，与你的Podfile文件在同一层级
- 修改你的Podfile，引入：
  - AliVCSDK_ARTC：适用于互动直播的音视频终端SDK，也可以使用：AliVCSDK_Standard或AliVCSDK_InteractiveLive，参考[快速集成](https://help.aliyun.com/document_detail/2412571.htm)
  - AUIMessage：互动消息组件源码，房间服务接口的默认实现需要用到AUIMessage及互动消息SDK，从仓库里拷贝源码，一般在AUIBaseKits目录下，拷贝到你的Podfile文件层级下
  - AUIRoomCore：互娱与通话场景API源码，目前支持语聊和KTV的场景化API，根据需要进行集成
```ruby

#需要iOS10.0及以上才能支持
platform :ios, '10.0'

target '你的App target' do
    # 根据自己的业务场景，集成合适的音视频终端SDK，支持：AliVCSDK_ARTC、AliVCSDK_Standard、AliVCSDK_InteractiveLive
    pod 'AliVCSDK_ARTC', '~> 6.10.0'

    # 互动消息组件源码，房间服务接口的默认实现需要用到AUIMessage及互动消息SDK
    pod 'AUIMessage/AliVCIM', :path => "./AUIMessage/"

    # 互娱语聊场景核心组件，指定依赖的SDK为AliVCSDK_ARTC，需要与上面指定的SDK名字一致
    pod 'AUIRoomCore/AliVCSDK_ARTC', :path => "./AUIRoomCore/"

    # 引入语聊API（如果有需要）
    pod 'AUIRoomCore/VoiceRoomAPI', :path => "./AUIRoomCore/"

    # 引入KTV API（如果有需要）
    pod 'AUIRoomCore/KaraokeRoomAPI', :path => "./AUIRoomCore/"
end
```
- 执行“pod install --repo-update”
- 源码集成完成

### 工程配置
- 打开工程info.Plist，添加NSMicrophoneUsageDescription权限
- 打开工程设置，在”Signing & Capabilities“中开启“Background Modes”

### 源码配置
- 完成前提条件后，进入文件ARTCRoomRTCService.swift，修改互动直播应用appID
```swift
// ARTCRoomRTCService.swift
@objcMembers public class ARTCRoomConfig: NSObject {
    
    public var appId = "你的appID"
    ...
}
```

### 调用API
前面工作完成后，在开发自身UI后，可以调用API接口实现业务功能。以下以语聊房为例

``` Swift
// 创建roomService
let roomAppServer = ARTCRoomAppServer("https://xxx.com")
roomAppServer.serverAuth = "xxx"
self.roomService = ARTCRoomServiceImpl(roomAppServer)

// 创建语聊房Engine
let controller = ARTCVoiceRoomEngine(roomInfoModel)
controller.roomService = self.roomService
self.roomController = controller

// 进入语聊房，执行该动作后，才能进行推拉流、接发消息等
self.roomController.joinRoom { [weak self] error in
    if let error = error {
        ...
    }
}

// 申请上麦
self.roomController.requestMic(completed: { error in
    if error == nil {
        
    }
    else {
        let code = ARTCRoomError.getErrorCode(error: error)
        if code == .JoinedMicErrorForNotIndex {
            AVToastView.show("麦上人员已满，请稍后尝试", view: self!.view, position: .mid)
        }
        else {
            AVToastView.show("上麦失败：\(error!.artcMessage))", view: self!.view, position: .mid)
        }
    }
})

// 下麦
self.roomController.leaveMic(completed: { error in
    
})


// 离开房间
self.roomController.leaveRoom()
self.navigationController?.dismiss(animated: ani)  // 关闭你的界面


// 房主解散房间
self.roomController.dismissRoom(completed: { [weak self] error in
    if error != nil {
        AVAlertController.show(withTitle: "解散失败，是否强制退出房间？", message: "",  cancelTitle:"暂不离开", okTitle: "强制离开") { isCanced in
            if isCanced == false {
                self?.roomController.leaveRoom()
                self?.navigationController?.dismiss(animated: ani)  // 关闭你的界面
            }
        }
    }
    else {
        self?.navigationController?.dismiss(animated: ani)  // 关闭你的界面
    }
})



```

