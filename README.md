# java爬虫框架
[![Fork me on Gitee](https://gitee.com/qianyi-community/J-crawler/widgets/widget_4.svg)](https://gitee.com/qianyi-community/J-crawler)

![Maven Central](https://img.shields.io/maven-central/v/top.jiangqiang.crawler/J-crawler)
![JAVA](https://img.shields.io/badge/JAVA-17+-green.svg)
[![star](https://gitee.com/qianyi-community/J-crawler/badge/star.svg?theme=dark)](https://gitee.com/qianyi-community/J-crawler/stargazers)
[![fork](https://gitee.com/qianyi-community/J-crawler/badge/fork.svg?theme=dark)](https://gitee.com/qianyi-community/J-crawler/members)
[![star](https://img.shields.io/github/stars/jiangqiang1996/J-crawler?style=social)](https://github.com/jiangqiang1996/J-crawler/stargazers)
[![fork](https://img.shields.io/github/forks/jiangqiang1996/J-crawler?style=social)](https://github.com/jiangqiang1996/J-crawler/network/members)
[![千异社区/J-crawler](https://gitee.com/qianyi-community/J-crawler/widgets/widget_card.svg?colors=4183c4,ffffff,ffffff,e3e9ed,666666,9b9b9b)](https://gitee.com/qianyi-community/J-crawler)

此工具自从2020年12月17日第一版发布到现在，前后经历了四个版本，侧重点也在发生各种各样的变化。
至今为止，第二版仍然是功能最强大的版本，支持爬取基于Vue之类的js框架编写的网站。
但是从第三版开始，直接剔除了爬取单页面网站相关的功能，转而侧重于此工具的易用性，虽然功能上虽然弱了一些，但是爬取某些静态网站却更加方便快捷，同时增加了防盗链解决方案等功能。
目前打算完全重构为第五版本，功能上主要侧重于爬虫服务的通用架构编写，目前大概构思如下：
1. 设置种子，种子来源可以来自数据库，或者上次爬取的结果，此步骤可以交给开发者自行编写。
2. 定制请求，此步骤主要负责控制请求参数，包括请求代理相关配置，还包括请求时间间隔相关配置。
3. 处理响应，根据响应类型进行自动归类。

此外，开发者还可以针对请求工具的线程池进行相应的定制。

### 特色功能

1. 支持统计爬取的任务，默认实现内存记录器，只需要实现Recorder接口，即可轻松定义一个基于数据库的记录器，重写对应的方法即可实现断点续爬等功能。
2. 只需要关注业务逻辑，而不需要过多关注技术实现。
4. 多线程异步爬取
5. 支持HTTP代理,爬取墙外内容，可以针对每一个链接配置请求参数，请求方式，请求头以及请求代理，轻松对接各种ip池
6. 内置完整示例，帮助了解学习。
7. 保存当前任务的来源链接
8. 错误原因持久化
9. 封装了完善的http请求工具类，支持各种常见请求
10. 支持配置登录接口，在所有任务开始前进行登录
11. 自定义去重逻辑

注意:
Content-Type只能为application/x-www-form-urlencoded提交和application/json提交两种,如果没有参数可以不写,否则必须写此header.不支持form-data。

### 使用方式
1. 拉取本框架代码，具体参考top.jiangqiang.crawler.sample包下示例，可以重写top.jiangqiang.crawler.core.app.GenericStarter类，实现自己的启动类。
2. 使用maven构建项目，引入下面依赖：
```XML
<dependency>
    <groupId>top.jiangqiang.crawler</groupId>
    <artifactId>J-crawler</artifactId>
    <version>4.1.1</version>
</dependency>
```

**如果运行报错，注意jdk版本，最低17**

[GitHub地址](https://github.com/jiangqiang1996/J-crawler)
[Gitee地址](https://gitee.com/qianyi-community/J-crawler)


**本项目仅学习使用，切勿用在非法用途。否则后果自负，技术无罪。**
