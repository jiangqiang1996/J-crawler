# J-crawler
[![License](https://img.shields.io/github/license/jiangqiang2020/J-crawler)](https://www.apache.org/licenses/LICENSE-2.0)
![JAVA](https://img.shields.io/badge/JAVA-11+-green.svg)
![issues](https://img.shields.io/github/issues/jiangqiang2020/J-crawler)
![stars](https://img.shields.io/github/stars/jiangqiang2020/J-crawler)
![forks](	https://img.shields.io/github/forks/jiangqiang2020/J-crawler)

### java爬虫框架

旨在用最少的代码开发爬虫框架,即使现有框架不满足你,你也可以使用最少的代码重构框架
比如自定义一个Application类,只需要继承现有抽象接口,然后自定义一个内部类,重写内部类的run方法而已,大部分关于线程的复杂代码都以及实现,完全不必关注过多的资源控制.

特色功能:
1. 支持断点续爬,数据库记录器直接开启断点续爬功能,内存记录器需要手动配置是否断点续爬,以及存储路径
2. 支持内存记录器和数据库记录器,默认实现内存记录器,数据库记录器需要自己实现数据库增删改查操作(必须实现现有Recorder接口).实现接口方法时,注意事务,以及去重操作
3. 支持自定义过滤器,此过滤器是过滤当前爬虫的子代爬虫,而不是过滤全局所有爬虫中的重复爬虫,至于全局去重,在记录器中实现.
4. 支持爬虫数量统计,数据库爬虫使用Recorder自行实现,内存记录器默认已经实现.
5. 支持给URL分类，可以使得相同类型的URL走相同的处理逻辑，可通过手动分类和正则表达式或者响应码分类。处理逻辑的方法可直接使用注解
6. 多线程爬取
7. 支持请求json接口,支持请求参数,支持多种提交方式,支持自定义header,类似于实现了postman的部分功能,继承TradApplication类即可
8. 支持HTTP代理,爬取墙外内容(注意,即使你电脑开了全局代理,但是在cmd窗口里面都是不会生效的,因此使用请求工具如果不添加代理参数一样是网络不通的)
9. 支持爬取js加载的网站,这些高级功能均继承SeleniumApplication类
10. 支持模拟登录
11. 支持下载防盗链的图片,支持下载墙外图片(可以通过代理参数)

注意:
Content-Type只能为application/x-www-form-urlencoded提交和application/json提交两种,如果没有参数可以不写,否则必须写此header.不支持form-data。

使用方式：
方式一：构建maven项目，引入下面依赖即可
```XML
<dependency>
  <groupId>xin.jiangqiang</groupId>
  <artifactId>J-crawler</artifactId>
  <version>2.0</version>
</dependency>
```

方式二：拉取本框架代码，创建自己的包，然后在自己的包下编写启动类。
如果有不满足你需求的地方，可以自己实现AbstractStarter类，不建议直接修改框架原有实现类的源码。
**如果运行报错，请先注释掉pom.xml中profiles节点所有内容**

框架使用示例均在xin.jiangqiang.sample包下

持续更新中...
如有疑问或bug，欢迎去我个人[博客](https://www.qianyi.xin)留言

**本项目仅学习使用，切勿用在非法用途。否则后果自负，技术无罪。**