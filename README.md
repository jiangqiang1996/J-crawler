# java爬虫框架4.0
[![Fork me on Gitee](https://gitee.com/qianyi-community/J-crawler/widgets/widget_4.svg)](https://gitee.com/qianyi-community/J-crawler)

![Maven Central](https://img.shields.io/maven-central/v/top.jiangqiang.crawler/J-crawler)
![JAVA](https://img.shields.io/badge/JAVA-17+-green.svg)
[![star](https://gitee.com/qianyi-community/J-crawler/badge/star.svg?theme=dark)](https://gitee.com/qianyi-community/J-crawler/stargazers)
[![fork](https://gitee.com/qianyi-community/J-crawler/badge/fork.svg?theme=dark)](https://gitee.com/qianyi-community/J-crawler/members)
[![千异社区/J-crawler](https://gitee.com/qianyi-community/J-crawler/widgets/widget_card.svg?colors=4183c4,ffffff,ffffff,e3e9ed,666666,9b9b9b)](https://gitee.com/qianyi-community/J-crawler)

### 特色功能

1. 支持统计爬取的任务，默认实现内存记录器，只需要实现Recorder接口，即可轻松定义一个基于数据库的记录器，重写对应的方法即可实现断点续爬等功能。
2. 只需要关注业务逻辑，而不需要过多关注技术实现。
4. 多线程异步爬取
5. 支持HTTP代理,爬取墙外内容，可以针对每一个链接配置请求参数，请求方式，请求头以及请求代理，轻松对接各种ip池
6. 内置完整示例，帮助了解学习。

注意:
Content-Type只能为application/x-www-form-urlencoded提交和application/json提交两种,如果没有参数可以不写,否则必须写此header.不支持form-data。

### 使用方式
1. 拉取本框架代码，具体参考top.jiangqiang.crawler.sample包下示例，可以重写top.jiangqiang.crawler.core.app.GenericStarter类，实现自己的启动类。
2. 使用maven构建项目，引入下面依赖：
```XML
<dependency>
    <groupId>top.jiangqiang.crawler</groupId>
    <artifactId>J-crawler</artifactId>
    <version>4.0.4</version>
</dependency>
```

**如果运行报错，注意jdk版本，最低17**

**本项目仅学习使用，切勿用在非法用途。否则后果自负，技术无罪。**
