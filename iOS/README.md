# AUIVoiceRoom
阿里云 · AUI Kits 语聊场景集成工具

## 介绍
AUI Kits 语聊房场景集成工具是阿里云提供的跨平台音视频实时通信服务，为业务方提供语音聊天、多人实时互动等场景的能力，借助通信的稳定、流畅、灵活的产品能力，以低代码的方式助力业务方快速发布应用。


## 源码说明

### 源码下载
下载地址[请参见](https://github.com/MediaBox-AUIKits/AUIVoiceRoom/tree/main/iOS)

### 源码结构
```
├── iOS  // iOS平台的根目录
│   ├── AUIVoiceRoom.podspec                // pod描述文件
│   ├── Source                                    // 源代码文件
│   ├── Resources                                 // 资源文件
│   ├── Example                                   // Demo代码
│   ├── AUIRoomCore                               // 互娱语聊场景核心组件 
│   ├── AUIBaseKits                               // 基础UI组件   
│   ├── README.md                                 // Readme  

```

### 环境要求
- Xcode 14.0 及以上版本，推荐使用最新正式版本
- CocoaPods 1.9.3 及以上版本
- 准备 iOS 10.0 及以上版本的真机

### 前提条件
需要开通应用，并且在你的服务端上开发相关接口或直接部署提供的Server源码，详情参考官网文档[前置准备](https://help.aliyun.com/zh/apsara-video-sdk/use-cases/pre-preparation)


## 跑通demo

- 源码下载后，进入Example目录
- 在Example目录里执行命令“pod install  --repo-update”，自动安装依赖SDK
- 打开工程文件“AUIVoiceRoomExample.xcworkspace”，修改包Id
- 完成前提条件后，进入文件AUIVoiceRoomManager.swift，修改服务端域名
```swift
// AUIVoiceRoomManager.swift
let VoiceRoomServerDomain = "你的应用服务器域名"
```
- 完成前提条件后，进入文件AUIRoomCommon.swift，修改互动直播应用appID
```swift
// AUIRoomCommon.swift
@objcMembers public class AUIRoomConfig: NSObject {
    
    public var appId = "你的appID"
    ...
}
```

- 选择”Example“Target 进行编译运行


## 快速开发自己的语聊房功能
可通过以下几个步骤快速集成AUIVoiceRoom到你的APP中，让你的APP具备语聊房功能

### 集成源码
- 导入AUIVoiceRoom：仓库代码下载后，拷贝iOS文件夹到你的APP代码目录下，改名为AUIVoiceRoom，与你的Podfile文件在同一层级，可以删除Example目录
- 修改你的Podfile，引入：
  - AliVCSDK_ARTC：适用于互动直播的音视频终端SDK，也可以使用：AliVCSDK_Standard或AliVCSDK_InteractiveLive，参考[快速集成](https://help.aliyun.com/document_detail/2412571.htm)
  - AUIFoundation：基础UI组件
  - AUIMessage：互动消息组件
  - AUIRoomCore：互娱语聊场景核心组件
  - AUIVoiceRoom：语聊场景UI组件源码
```ruby

#需要iOS10.0及以上才能支持
platform :ios, '10.0'

target '你的App target' do
    # 根据自己的业务场景，集成合适的音视频终端SDK，支持：AliVCSDK_ARTC、AliVCSDK_Standard、AliVCSDK_InteractiveLive
    pod 'AliVCSDK_ARTC', '~> 6.10.0'

    # 基础UI组件
    pod 'AUIFoundation', :path => "./AUIVoiceRoom/AUIBaseKits/AUIFoundation/"

    # 互动消息组件
    pod 'AUIMessage/AliVCIM', :path => "./AUIVoiceRoom/AUIBaseKits/AUIMessage/"

    # 互娱语聊场景核心组件
    pod 'AUIRoomCore/AliVCSDK_ARTC', :path => "./AUIVoiceRoom/AUIRoomCore/"
    
    # 语聊房UI组件
    pod 'AUIVoiceRoom',  :path => "./AUIVoiceRoom/"
end
```
- 执行“pod install --repo-update”
- 源码集成完成

### 工程配置
- 打开工程info.Plist，添加NSMicrophoneUsageDescription权限
- 打开工程设置，在”Signing & Capabilities“中开启“Background Modes”


### 源码配置
- 完成前提条件后，进入文件AUIVoiceRoomManager.swift，修改服务端域名
```swift
// AUIVoiceRoomManager.swift
let VoiceRoomServerDomain = "你的应用服务器域名"
```
- 完成前提条件后，进入文件ARTCRoomRTCService.swift，修改互动直播应用appID
```swift
// ARTCRoomRTCService.swift
@objcMembers public class ARTCRoomConfig: NSObject {
    
    public var appId = "你的appID"
    ...
}
```
### 调用API
前面工作完成后，接下来可以根据自身的业务场景和交互，可以在你APP其他模块或主页上通过组件接口快速实现语聊房功能，也可以根据自身的需求修改源码。

``` Swift
// 初始化，进入语聊房前（需确保已经App登录）
let user = ARTCRoomUser(uid) // 当前登录用户信息
user.userNick = nick
user.userAvatar = avatar
let serverAuth = auth  // app登录后的token，用于服务端安全校验
AUIVoiceRoomManager.shared.setup(currentUser: user!, serverAuth: serverAuth)


// 打开语聊房间列表
let listVC = AUIVoiceRoomListViewController()
self.navigationController?.pushViewController(listVC, animated: false)

// 创建语聊房
AUIVoiceRoomManager.shared.createRoom(currVC: self)

// 加入语聊房
AUIVoiceRoomManager.shared.enterRoom(roomId: roomInfo.roomId, currVC: self)

```

### 运行结果
参考Demo

## 常见问题
更多AUIKits问题咨询及使用说明，请搜索钉钉群（35685013712）加入AUI客户支持群联系我们。
