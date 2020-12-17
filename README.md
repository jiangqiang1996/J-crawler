# J-crawler
java爬虫框架

特色功能:
1. 支持断点续爬
2. 支持自定义接口实现数据保存数据库
3. 支持自定义过滤器
4. 支持爬虫数量统计，目前使用的是内存(断点续传时不支持继续统计)，建议使用数据库重新实现
5. 支持给URL分类，可以使得相同类型的URL走相同的处理逻辑，可通过手动分类和正则表达式或者响应码分类。处理逻辑的方法可直接使用注解
6. 多线程爬取
7. 支持爬取json接口
8. 支持请求参数,支持多种提交方式,支持自定义header
9. 支持HTTP代理

注意:
Content-Type只能为application/x-www-form-urlencoded提交和application/json提交两种,如果没有参数可以不写,否则必须写此header.
不支持form-data

使用方式：
方式一：构建maven项目，引入下面依赖即可
```XML
<dependency>
  <groupId>xin.jiangqiang</groupId>
  <artifactId>J-crawler</artifactId>
  <version>1.0</version>
</dependency>
```
方式二：拉取本框架代码，创建自己的包，然后在自己的包下编写启动类。
如果有不满足你需求的地方，可以自己实现AbstractStarter类，不建议直接修改框架原有实现类的源码。
**如果运行报错，请先注释掉pom.xml中profiles节点所有内容**

框架使用示例均在xin.jiangqiang.sample包下

已知问题:
貌似去重功能没有生效，影响不大，暂时不改。

开发计划：
未来支持爬取js加载的页面，预计春节前会完成初版。

欢迎参与维护并贡献代码。

持续更新中...
