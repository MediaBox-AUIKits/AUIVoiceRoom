# aui-appserver 帮助文档


## 前期准备
使用该项目，需要您拥有以下的产品权限 / 策略：

| 服务/业务 | 权限信息               |
| --- |----------------------|
| 权限/策略 | AliyunLiveFullAccess |

AliyunLiveFullAccess权限用于请求IM相关服务

## 应用详情

- 关于技术选型 
  - 基于主流的Java8 + Springboot2搭建框架
  - 基于Mybatis plus(https://baomidou.com/)作为Repository层选型
  - 基于SpringSecurity + JWT 来实现权限控制
- 关于部署
  - 理论上只要安装了Java8即可运行在各个ECS或容器上。可以考虑使用Serverless平台(https://help.aliyun.com/product/50980.html)来快速部署


## 工程配置说明
见下描述
```yaml
server:
  port: 8080

# mysql.sql
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    # 配置DB地址、用户名及密码
    url: jdbc:mysql://*****:3306/****?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: r*****
    password: p*****
  jackson:
    time-zone: GMT+8
    date-format: yyyy-MM-dd'T'HH:mm:ss
    default-property-inclusion: non_null

#mybatis, 无需调整
mybatis-plus:
  #实体扫描，多个package用逗号或者分号分隔, 无需调整
  typeAliasesPackage: com.aliyuncs.aui.entity
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    default-statement-timeout: 10

# pop配置，用于调用IM相关服务
biz:
  openapi:
    access:
      key: daeewe*****
      secret: we2wewe******
  # IM相关配置
  new_im:
      appId: "0c8xxxxx"
      appKey: "586fxxxxxx"
      appSign:  "232sfxxxxxx"
  # 连麦相关配置
  live_mic:
    app_id: 7c61********
    app_key: c461b*********
    
# 配置跨域，无需调整
http:
  cors:
    host: "*"
```

## 打包&启动
以监听9000为示例，见下
```shell
#!/usr/bin/env bash

mvn package -DskipTests
cp target/*.jar target/webframework.jar
java -Dserver.port=9000 -jar target/webframework.jar
```

