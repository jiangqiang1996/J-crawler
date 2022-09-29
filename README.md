# java爬虫框架4.0，整合springboot相关技术，重构持久化相关代码

# J-crawler3.0.2基本功能已经实现

[![License](https://img.shields.io/github/license/jiangqiang1996/J-crawler)](https://www.apache.org/licenses/LICENSE-2.0)
![JAVA](https://img.shields.io/badge/JAVA-11+-green.svg)
![issues](https://img.shields.io/github/issues/jiangqiang1996/J-crawler)
![stars](https://img.shields.io/github/stars/jiangqiang1996/J-crawler)
![forks](https://img.shields.io/github/forks/jiangqiang1996/J-crawler)

### java爬虫框架

旨在用最少的代码开发爬虫框架,即使现有框架不满足你,你也可以使用最少的代码重构框架
比如自定义一个Application类,只需要继承现有抽象接口,然后自定义一个内部类,重写内部类的run方法而已,大部分关于线程的复杂代码都以及实现,完全不必关注过多的资源控制.

特色功能:

1. 支持内存记录器和数据库记录器,默认实现内存记录器,数据库记录器需要自己实现数据库增删改查操作(必须实现现有Recorder接口).实现接口方法时,注意事务,以及去重操作
2. 支持自定义过滤器,此过滤器是过滤当前爬虫的子代爬虫,而不是过滤全局所有爬虫中的重复爬虫,至于全局去重,在记录器中实现.
3. 支持爬虫数量统计,数据库爬虫使用Recorder自行实现,内存记录器默认已经实现.
4. 支持给URL分类，可以使得相同类型的URL走相同的处理逻辑，可通过手动分类和正则表达式或者响应码分类。处理逻辑的方法可直接使用注解
5. 多线程爬取
6. 支持请求json接口,支持请求参数,支持多种提交方式,支持自定义header,类似于实现了postman的部分功能,继承SimpleStarter类即可
7. 支持HTTP代理,爬取墙外内容(注意,即使你电脑开了全局代理,但是在cmd窗口里面都是不会生效的,因此使用请求工具如果不添加代理参数一样是网络不通的)
8. 已经支持续爬功能

注意:
Content-Type只能为application/x-www-form-urlencoded提交和application/json提交两种,如果没有参数可以不写,否则必须写此header.不支持form-data。

有两种使用方式：
1. 拉取本框架代码，创建自己的包，然后在自己的包下编写启动类，具体参照Sample类。
如果有不满足你需求的地方，可以自己实现AbstractStarter类，不建议直接修改框架原有实现类的源码。目前尚不支持爬取单页面项目，后续会逐渐支持。
2. 使用maven构建项目，引入下面依赖：
```XML
<!-- https://mvnrepository.com/artifact/xin.jiangqiang/J-crawler -->
<dependency>
    <groupId>xin.jiangqiang</groupId>
    <artifactId>J-crawler</artifactId>
    <version>3.0.2</version>
</dependency>
```

**如果运行报错，请先注释掉pom.xml中profiles节点所有内容**

框架使用示例均在xin.jiangqiang.sample包下

持续更新中...

爬虫框架3.0重构中...

如有疑问或bug，欢迎去我个人[博客](https://www.qianyi.xin)留言

**本项目仅学习使用，切勿用在非法用途。否则后果自负，技术无罪。**
