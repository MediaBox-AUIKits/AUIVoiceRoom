#
# Be sure to run `pod lib lint AUIRoomCore.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see https://guides.cocoapods.org/syntax/podspec.html
#

Pod::Spec.new do |s|
  s.name             = 'AUIRoomCore'
  s.version          = '1.0.0'
  s.summary          = 'A short description of AUIRoomCore.'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/MediaBox-AUIKits/AUIRoomCore'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :text => 'LICENSE' }
  s.author           = { 'aliyunvideo' => 'videosdk@service.aliyun.com' }
  s.source           = { :git => 'https://github.com/MediaBox-AUIKits/AUIRoomCore', :tag =>"v#{s.version}" }
  # s.social_media_url = 'https://twitter.com/<TWITTER_USERNAME>'

  s.ios.deployment_target = '10.0'
  s.static_framework = true
  s.swift_version = '5.0'
  s.default_subspecs='Service'
  
  s.subspec 'Service' do |ss|
    ss.source_files = 'Source/Core/{Model,Service}/**/*.{swift,h,m,mm}'
  end
  
  s.subspec 'VoiceRoomCore' do |ss|
    ss.dependency 'AUIRoomCore/Service'
    ss.source_files = 'Source/Core/VoiceRoom/**/*.{swift,h,m,mm}'
  end
  
  s.subspec 'KaraokeRoomCore' do |ss|
    ss.dependency 'AUIRoomCore/VoiceRoomCore'
    ss.source_files = 'Source/Core/KaraokeRoom/**/*.{swift,h,m,mm}'
  end
  
  s.subspec 'VoiceRoomAPI' do |ss|
    ss.dependency 'AUIRoomCore/VoiceRoomCore'
    ss.dependency 'AUIMessage'
    ss.source_files = 'Source/Imp/*.{swift,h,m,mm}', 'Source/Imp/VoiceRoom/**/*.{swift,h,m,mm}', 'Source/Login/**/*.{swift,h,m,mm}'
    ss.pod_target_xcconfig = {'SWIFT_ACTIVE_COMPILATION_CONDITIONS' => 'ARTC_VOICE_ROOM'}
  end
  
  s.subspec 'KaraokeRoomAPI' do |ss|
    ss.dependency 'AUIRoomCore/KaraokeRoomCore'
    ss.dependency 'AUIRoomCore/VoiceRoomAPI'
    ss.source_files = 'Source/Imp/KaraokeRoom/**/*.{swift,h,m,mm}'
    ss.pod_target_xcconfig = {'SWIFT_ACTIVE_COMPILATION_CONDITIONS' => 'ARTC_KARAOKE_ROOM'}
  end
  
  s.subspec 'API' do |ss|
    ss.dependency 'AUIRoomCore/Service'
    ss.dependency 'AUIMessage'
    ss.source_files = 'Source/Imp/**/*.{swift,h,m,mm}', 'Source/Core/{VoiceRoom,KaraokeRoom}/**/*.{swift,h,m,mm}', 'Source/Login/**/*.{swift,h,m,mm}'
    ss.pod_target_xcconfig = {'SWIFT_ACTIVE_COMPILATION_CONDITIONS' => 'ARTC_KARAOKE_ROOM ARTC_VOICE_ROOM'}
  end
  
  s.subspec 'AliVCSDK_ARTC' do |ss|
    ss.dependency 'AliVCSDK_ARTC'
  end
  
  s.subspec 'AliVCSDK_Standard' do |ss|
    ss.dependency 'AliVCSDK_Standard'
  end
  
  s.subspec 'AliVCSDK_InteractiveLive' do |ss|
    ss.dependency 'AliVCSDK_InteractiveLive'
  end
  
end
