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
3. 处理响应。

此外，开发者还可以针对请求工具的线程池进行相应的定制。

### 特色功能

1. 仅关注爬虫业务，不需要关注过多的模板代码。
2. 全自动解决防盗链问题。
3. 多线程异步爬取，爬虫效率高。
4. 爬虫业务与持久化分离，方便实现分布式爬虫工具。
5. 支持代理池，支持根据不同URL配置不同的代理池。
6. 使用简单，方便定制。

注意:
Content-Type只能为application/x-www-form-urlencoded提交和application/json提交两种,如果没有参数可以不写,否则必须写此header.不支持form-data。

### 使用方式
1. 拉取本框架代码，具体参考top.jiangqiang.crawler.sample包下示例。
2. 使用maven构建项目，引入下面依赖：
```XML
<dependency>
    <groupId>top.jiangqiang.crawler</groupId>
    <artifactId>J-crawler</artifactId>
    <version>5.0.0</version>
</dependency>
```

**如果运行报错，注意jdk版本，最低17**

[GitHub地址](https://github.com/jiangqiang1996/J-crawler)
[Gitee地址](https://gitee.com/qianyi-community/J-crawler)


**本项目仅学习使用，切勿用在非法用途。否则后果自负，技术无罪。**
