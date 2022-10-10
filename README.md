# java爬虫框架4.0

[![License](https://img.shields.io/github/license/jiangqiang1996/J-crawler)](https://www.apache.org/licenses/LICENSE-2.0)
![JAVA](https://img.shields.io/badge/JAVA-11+-green.svg)
![issues](https://img.shields.io/github/issues/jiangqiang1996/J-crawler)
![stars](https://img.shields.io/github/stars/jiangqiang1996/J-crawler)
![forks](https://img.shields.io/github/forks/jiangqiang1996/J-crawler)

### java爬虫框架

旨在用最少的代码开发爬虫框架,即使现有框架不满足你,你也可以使用最少的代码重构框架
只需要提供一个URL链接，即可获取到该响应内容中的所有其他链接。不需要关注线程问题，以及同步还是异步问题。只需要定义一个ResultHandler即可实现自己的逻辑，或者定义一个Recorder即可轻松接入各种持久层数据库。
内置大量常用正则表达式，可以让你不写一行代码即可获取html文档中的所有url链接。

特色功能:

1. 支持统计爬取的任务，默认实现内存记录器，只需要实现Recorder接口，即可轻松定义一个基于数据库的记录器，重写对应的方法即可实现断点续爬等功能。
2. 只需要关注业务逻辑，而不需要过多关注技术实现。
4. 多线程异步爬取
5. 支持HTTP代理,爬取墙外内容，可以针对每一个链接配置请求参数，请求方式，请求头以及请求代理，轻松对接各种ip池
6. 内置完整示例，帮助了解学习。

注意:
Content-Type只能为application/x-www-form-urlencoded提交和application/json提交两种,如果没有参数可以不写,否则必须写此header.不支持form-data。

有两种使用方式：
1. 拉取本框架代码，创建自己的包，然后在自己的包下编写启动类，具体参照Sample模块。
如果有不满足你需求的地方，可以自己实现AbstractStarter类，不建议直接修改框架原有实现类的源码。目前尚不支持爬取单页面项目，后续会逐渐支持。
2. 使用maven构建项目，引入下面依赖：
```XML
<!-- https://mvnrepository.com/artifact/xin.jiangqiang/J-crawler -->
<dependency>
    <groupId>top.jiangqiang.crawler</groupId>
    <artifactId>J-crawler</artifactId>
    <version>4.0.0</version>
</dependency>
```

**如果运行报错，请先注释掉pom.xml中profiles节点所有内容**


持续更新中...

爬虫框架4.0重构中...

如有疑问或bug，欢迎去我个人[博客](https://www.jiangqiang.top)留言，博客地址目前暂时不能访问。

**本项目仅学习使用，切勿用在非法用途。否则后果自负，技术无罪。**
