# AUIVoiceRoom
阿里云 · AUI Kits 语聊场景集成工具

## 介绍
AUI Kits 语聊房场景集成工具是阿里云提供的跨平台音视频实时通信服务，为业务方提供语音聊天、多人实时互动等场景的能力，借助通信的稳定、流畅、灵活的产品能力，以低代码的方式助力业务方快速发布应用。


## 源码说明

### 源码下载
下载地址[请参见](https://github.com/MediaBox-AUIKits/AUIVoiceRoom/tree/main/Android)

### 源码结构
```
├── Android       //Android平台工程结构跟目录
│   ├── AUIBaseKits    //AUI基础组件
│   ├── AUIVoiceRoom   //UI组件
│   ├── AUIVoiceRoomEngine //场景接口与实现
│   ├── README.md
│   ├── app           //Demo
│   ├── build.gradle  
│   └── settings.gradle

```

### 环境要求
- Android Studio 插件版本4.1.3
- Gradle 7.0.2
- Android Studio自带 jdk11

### 前提条件
需要开通应用，并且在你的服务端上开发相关接口或直接部署提供的Server源码，详情参考官网文档[前置准备]


## 跑通demo

- 源码下载后，使用Android Studio打开Android目录
- 打开工程文件“build.gradle”，修改包Id
- 完成前提条件后，进入文件ChatRoomApi.java，修改服务端域名
```java
// ChatRoomApi.java
public static final String HOST = "你的应用服务器域名";
```
- 完成前提条件后，进入文件ChatRoomApi.java，修改互动直播应用appID
```java
// ChatRoomApi.java
public static final String APP_ID = "你的互动直播应用appID";
```

- 进行编译运行


## 快速开发自己的语聊房功能
可通过以下几个步骤快速集成AUIVoiceRoom到你的APP中，让你的APP具备语聊房功能

### 集成源码
1. 导入AUIVoiceRoom与AUIVoiceRoomEngine：仓库代码下载后，Android Studio菜单选择: File -> New -> Import Module，导入选择文件夹
2. 修改文件夹下的build.gradle的三方库依赖项

```gradle
dependencies {

    implementation 'androidx.appcompat:appcompat:x.x.x'                     //修改x.x.x为你工程适配的版本
    implementation 'com.google.android.material:material:x.x.x'             //修改x.x.x为你工程适配的版本
    androidTestImplementation 'androidx.test.espresso:espresso-core:x.x.x'  //修改x.x.x为你工程适配的版本
    implementation 'com.aliyun.aio:AliVCSDK_Standard:x.x.x'                  //修改x.x.x为你工程适配的版本
}
```
3. 等待gradle同步完成，完成源码集成

### 源码配置
- 完成前提条件后，进入文件ChatRoomApi.java，修改服务端域名
```java
// ChatRoomApi.java
public static final String HOST = "你的应用服务器域名";
```
- 完成前提条件后，进入文件ChatRoomApi.java，修改互动直播应用appID
```java
// ChatRoomApi.java
public static final String APP_ID = "你的互动直播应用appID";
```

### 调用API
前面工作完成后，接下来可以根据自身的业务场景和交互，可以在你APP其他模块或主页上通过组件接口快速实现语聊房功能，也可以根据自身的需求修改源码。

``` java
// 设置个人信息并初始化
String roomId = "xxx";
AUIVoiceRoom auiVoiceRoom = ChatRoomManager.getInstance().createVoiceRoom(roomId);
UserInfo userInfo = new UserInfo("xxx", "xxx");
userInfo.userName = currentUser.getName();
userInfo.avatarUrl = currentUser.getAvatar();

auiVoiceRoom.init(ChatEntryActivity.this.getApplicationContext(),ChatRoomApi.APP_ID,  ChatEntryActivity.this.authorization, userInfo, ChatEntryActivity.this.im_token, new ActionCallback() {
    @Override
    public void onResult(int code, String msg, Map<String, Object> params) {
        if(code == ChatRoomManager.CODE_SUCCESS) {
            Log.v(TAG, "init room success");
        } else {
            Log.v(TAG, "init room fail:code:" + code + ",msg:" + msg );
        }
    }
});


// 加入房间
RoomInfo roomInfo = new RoomInfo("xxx");
RtcInfo rtcInfo = new RtcInfo("xxx", "xxx");
auiVoiceRoom.joinRoom(roomInfo, rtcInfo, new ActionCallback() {
    @Override
    public void onResult(int code, String msg, Map<String, Object> params) {
        Log.v(TAG, "join room:" + code + ",msg:" + msg + ",roomId:" + roomInfo.roomId);
    }
});
```

### 运行结果
参考Demo

## 常见问题
更多AUIKits问题咨询及使用说明，请搜索钉钉群（35685013712）加入AUI客户支持群联系我们。
