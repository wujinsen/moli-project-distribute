---
title: Cloudera Manager、CDH零基础入门、线路指导.note（原文插图 annex）
slug: annex-Cloudera-Manager、CDH零基础入门、线路指导
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Cloudera/cloudera安装部署/Cloudera Manager、CDH零基础入门、线路指导.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

问题导读：

- 1.什么是cloudera CM 、CDH?

- 2.CDH、CM有哪些版本？

- 3.CDH、CM有哪些安装⽅式？

- 4.CDH如何开发？


![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

我们知道cloudera CDH 是为简化hadoop的安装，也对对hadoop做了⼀些封装。那么我们就像尝试学习 cloudera。 cloudera本质hadoop的封装，那么学起来，应该不难。不就是下载下来，然后⾃动安装，最后管理 hadoop⽣态系统的⼀些服务。 的确是这样的,但是也没有我们想象的那么简单。

## 基础知识

那么我们就开始第⼀步吧，第⼀步之前，那就是你的基础，这个是很关键的，如果不会使⽤Linux，那 么你需要看⼀下另外⼀篇⽂章： 。可以看看Linux的 基础知识模块。需要掌握的内容还是不少的：

零基础学习hadoop到上⼿⼯作线路指导（初级篇）

集群搭建：主机宽带拨号上⽹，虚拟机使⽤桥接模式，该如何ping通外⽹ 集群搭建必备：虚拟机之⼀实现Host-only⽅式上⽹ 集群搭建必备：nat模式设置静态ip，达到上⽹与主机相互通信 云技术基础：学习hadoop使⽤零基础linux(Ubuntu)笔记 搭建集群必知：Linux常⽤命令及修改⽂件总结（不断更新） Linux⽹络接⼝ifconfig命令及认识⽹络接⼝lo linux⼊⻔详细介绍 虚拟机安装linux⽹络配置资料⼤全 解决遇到Linux⽹络配置，从熟悉⽹络配置⽂件⼊⼿ linux⼊⻔⼤全：包括零基础⼊⻔，Linux详细介绍 Linux重启⽅式init 0 init1 init 3 init 5 init 6 这⼏个启动级别都代表什么意思？ Ubuntu常⽤命令总结及修改DNS的多种⽅法总结 Linux关机各种关机命令总结 Linux基础必懂：eth0,eth1,eth2,lo是什么意思？

（⼤体了解即可）

Linux掌握了，那么我们是否需要掌握虚拟机的基础知识，在搭建hadoop集群中，可以这些是必须掌握 的，那么cloudera是否需要，也是需要的，这⾥就不在罗列了，详细参考

零基础学习hadoop到上⼿⼯ 作线路指导（初级篇）

。但是cloudera CM的安装⽐起hadoop集群的安装对硬件的要求更⾼。内存⾄少 10G，为什么会这么多，如果少于10G是否可以，答案是可以的，但是后⾯你会遇到各种问题，或许都 找不到答案。 对于cloudera-scm-server就需要⾄少4G的内存，cloudera-scm-agent的内存⾄少也需要1.5G以上。那 么如果你的机器是8G的，还是很吃⼒的。 在安装的过程中，后⾯有很多的服务安装遇到问题的可能性是很⼤的。

## 什么是CDH

hadoop是⼀个开源项⽬，所以很多公司在这个基础进⾏商业化，Cloudera对hadoop做了相应的改变。 Cloudera公司的发⾏版，我们将该版本称为CDH。

很多新⼿问的最多的问题是，哪个是收费的，那个是免费的。 Cloudera Express版本是免费的 Cloudera Enterprise是需要购买注册码的

更多内容：

Cloudera Hadoop什么是CDH及CDH版本介绍

CDH（Cloudera）与hadoop（apache）对⽐

⼤数据架构师基础：hadoop家族，Cloudera产品系列等各种技术

![image 3](assets/imageFile3.png)

## 官⽹介绍

![image 4](assets/imageFile4.png)

当我们学习的时候，我们该如何学习，有两种⽅式：第⼀种：先理论，后实践 第⼆种：先实践，后理论

上⾯两种⽅式各有各的场合，如果你要去⾯试、考试、搞理论思想类，第⼀种⽅式是没有问题的。因 为这些都是停留在思想层⾯的。 对于⼀个编程技术⼈员来讲，本⼈觉得第⼆种⽅式更加的短平快。 上⾯两种⽅式都有些绝对，最佳的⽅式就是实践与理论相结合。 这⾥撰⽂⼀下，引⽤孔⼦的⼀句话

学⽽不思则罔 思⽽不学则殆

只看书，不实践，或则只倒腾部署、停留在某些问题得不到解决，但是⼜不去看书、百度、⾕歌查找 解决问题，这样也是不⾏的。

好了，开始我们的官⽹： 官⽹是我们学习标准，所以我们⾸先要记住官⽹地址： 主⻚：

http://www.cloudera.com/content/cloudera/en/home.html

进⼊官⽹我们可以做些什么事情：

- 1.提供API

- 2.查看部署⽂档

- 3.下载安装包 那么我们如何查找API，如何查找部署⽂档，如何下载安装包？ 详细可以查看


cloudera(CDH)官⽹介绍：安装包、离线包该如何下载、官⽅⽂档等介绍

![image 5](assets/imageFile5.png)

## 安装包下载：

同时附上各个版本包的地址： Cloudera⽂档汇总

http://www.cloudera.com/content/support/en/documentation.html

CDH4、CDH5包汇总

- http://archive.cloudera.com/cdh4/

- http://archive.cloudera.com/cdh5/


CM4、CM5包汇总

http://archive.cloudera.com/cm4/ http://archive.cloudera.com/cm5/

官⽹CDH5下载

http://www.cloudera.com/content/ ... /cdh/cdh-5-1-0.html

以前版本地址：

CDH1~CDH3

http://archive-primary.cloudera.com/cdh/

当我们看到安装的时候，同时也困扰着我们，我们该选择哪个安装包。

⾸先需要介绍下CM(Cloudera Manager)及CDH的安装⽅式：

### CM(Cloudera Manager)有三种安装⽅式：

- 1.第⼀种使⽤cloudera-manager-installer.bin安装 这种安装⽅式，只要从官⽹下载cloudera-manager-installer.bin，然后执⾏这个bin⽂件，剩下的就是等 待下载和安装。但是这个时间不是⼀般的⻓，最好吃个饭，睡个觉，最后看到还在安装过程中。此帖 安装步骤及遇到问题记录很详细，可参考


##### Cloudera Manager5及CDH5在线（cloudera-manager-installer.bin）安装详细⽂档

Cloudera Manager5及CDH5安装指导（终极在线安装）

遇到的问题： 当我们安装完毕CM，那么我们就要添加主机，主机添加完毕，我们会看到安装进度：

![image 6](assets/imageFile6.png)

但是并不是每个⼈都能看到上⾯进度，⽽是看到下⾯错误：

Detecting Cloudera Manager Server... Detecting Cloudera Manager Server... BEGIN host -t PTR 192.168.1.198

198.1.168.192.in-addr.arpa domain name pointer localhost. END (0) using localhost as scm server hostname BEGIN which python /usr/bin/python

- END (0) BEGIN python -c 'import socket; import sys; s = socket.socket(socket.AF_INET); s.settimeout(5.0); s.connect((sys.argv[1], int(sys.argv[2]))); s.close();' localhost 7182 Traceback (most recent call last): File "<string>", line 1, in <module> File "<string>", line 1, in connect socket.error: [Errno 111] Connection refused
- END (1) could not contact scm server at localhost:7182, giving up waiting for rollback request


上⾯错误该如何解决： 可以使⽤下⾯⽅法解决：

1.

mv /usr/bin/host /usr/bin/host.bak

复制代码

- 2.第⼆种使⽤rpm、yum、apt-get⽅式在线安装 这种安装⽅式，在⽹上看的有点眼花缭乱，可能是因为我们对yum、apt、rpm了解的不多造成的，对 于Linux基础缺乏的，可以参考： ，总的来说：由于 rpm依赖关系不好，所以产⽣了yum，⽽yum和apt则都是Linux的包管理⼯具，并且解决了包与包的依 赖关系。


yum与rpm、apt的区别：rpm的缺陷及yum的优势

这⾥列出⼀些安装⽂档

ubuntu 12.04 安装 Cloudera Manager5及CDH5(Mysql)【添加yum源⽅式安装集群】

- 遇到问题1：内存过⼩ 使⽤yum安装因为mysql的配置⾄少需要对Cloudera server 4G的内存，否则mysql的重启过程中，就会 遇到unknown instance.


- 遇到问题2：界⾯⽆法访问 界⾯⽆法访问，分为两种情况 ⼀种是Cloudera-manager-server没有完全起来


当我们运⾏下⾯命令：

![image 7](assets/imageFile7.png)

如果现在，输⼊url，访问web，可能会是⽆法访问 host/ip:7180,我这⾥是172.16.77.60：7180

![image 8](assets/imageFile8.png)

⼤概需要等10分钟左右，界⾯就可以访问了。

![image 9](assets/imageFile9.png)

⼀种则是权限问题 所以我们初次安装尽量使⽤root.还有我们需要会看⽇志。

Linux⽇志的位置： 下⾯为server⽇志

![image 10](assets/imageFile10.png)

下⾯为agent⽇志

![image 11](assets/imageFile11.png)

同时在我们安装服务的过程中，我们还需要会查看⻆⾊⽇志，这样从web界⾯就是可以看到，因为在我 们安装过程中，服务的安装没有那么顺利。 这⾥暂时没有截图，我们只要记住⻆⾊⽇志，在界⾯中找到即可。

- 遇到问题3：⽆法找到cloudera agent客户端


我们使⽤下⾯命令安装了agent

1.

sudo apt-get install cloudera-manager-agent cloudera-manager-daemons

复制代码

当然还有很多其它需要安装，当我们的agent安装成功后，为什么我们的的界⾯中不能发现agent.

本来我们有三台，但是却只有⼀台，⽽且是cloudera server本地的agent

![image 12](assets/imageFile12.png)

这是因为我们的在安装的过程中，agent都指向了本地localhost. 我们打开配置⽂件

1.

sudo nano /etc/cloudera-scm-agent/config.ini

复制代码

修改 server_host为server ip地址 server_port为7180

下图CDH即为cloudera server的hostname

![image 13](assets/imageFile13.png)

修改完毕： ⾸先重启Agent sudo service cloudera-scm-agent restart 然后重启server sudo service cloudera-scm-server restart 这时候会发现已经⽣效。 更详细内容可以参考

记录cloudera Manager安装Cloudera-Scm-Agent如何指向Cloudera-Scm-Server

更多⽂档可以参考：

Cloudera Manager 和 CDH 4 终极安装（⼀）

ClouderaManager以及CHD5.1.0集群部署安装

Cloudera Manager （centos）离线安装详细介绍

CM5、CDH5安装（CDH5半⾃动离线安装）

Cloudera Manager 5 和 CDH5 本地（离线）安装指南

Cloudera Manager 和 CDH5 本地（离线）安装指导

- 3.第三种使⽤是Tarballs的⽅式

这种⽅式⽹上资料很少 安装的命令如下：

复制代码

这个跟hadoop的安装⽅式是⼀样的 安装的⽅法详细可参考：

这⾥在列出官⽹给出的三种安装⽅式：

如果想安装CM，这次再次强调⼀定要有⾜够的内存。并且我们安装完毕CM，⼀般会

- 4.安装失败处理 上⾯如果我们安装失败该如何处理：

- 5.⽬录结构： 由于我们⼤多采⽤在线安装的⽅式，所以出⾎，我们根本不知道cloudera安装在了什么位置，详细可以 参考下⾯内容


1.

$ tar xzf cloudera-manager*.tar.gz -C /opt/cloudera-manager

Installation Path C - Manual Installation Using Cloudera Manager Tarballs

- Installation Path A - Automated Installation by Cloudera Manager

- Installation Path B - Manual Installation Using Cloudera Manager Packages

- Installation Path C - Manual Installation Using Cloudera Manager Tarballs


使⽤ cloudera-manager-installer.bin(Parcles)安装失败后卸载cloudera

卸载 Cloudera Manager 5.1.x.和 相关软件【官⽹翻译：⾼可⽤】

解析Cloudera Manager内部结构、功能包括配置⽂件、⽬录位置等

亦可参考下⾯帖⼦：

卸载 Cloudera Manager 5.1.x.和 相关软件【官⽹翻译：⾼可⽤】

![image 14](assets/imageFile14.png)

#### CDH安装⽅式：

Yum/Apt包，Tar包，RPM包，CM安装 这些包的下载，可以在上⽂安装包汇总中可以找到。 CDH1~CDH3

http://archive-primary.cloudera.com/cdh/

CDH4～CDH5

- http://archive.cloudera.com/cdh4/

- http://archive.cloudera.com/cdh5/


安装⽂档，下⾯可以参考

Linux（ubuntu12.04）单节点伪分布安装CDH5.1.X及提交wordcount到yarn⾼可靠⽂档 各个版本Linux单节点伪分布安装CDH5.1.X及提交wordcount到yarn⾼可靠⽂档

Hadoop CDH5 ⼿动安装伪分布式模式

通过CM⽅式安装，⼤多在安装CM的时候，已经安装了CDH。

![image 15](assets/imageFile15.png)

# CDH开发

安装完毕，我们还想开发提到开发，我们就想到eclipse插件，其实我们在开发过程中，插件作⽤就是 帮助我们能够⽅便的看到在Linux的⽂件。 所以开发⽅式也有两种， ⼀种插件开发 我们如何找到eclipse插件，可以参考

cloudera CDH（5）开发⽅式及CDH eclipse插件编译总结

⼀种是⽆插件开发 ⽆插件开发，也就是直接添加开发包

hadoop开发⽅式总结及操作指导

可以参考：

先总结到此，希望⼤家有所收获。

![image 16](assets/imageFile16.png)

有的同学反映写的不够细，这⾥在补充⼀些内容：

### Cloudera Manager Server启动后⼜挂掉的原因总结

我们安装CDH的时候，会碰到cloudera server启动后⼜挂掉了，这是什么原因？ 查看⽇志：包下⾯问题

可是明明已经安装jdk，并且已经设置了JAVA_HOME

1. java -version

复制代码

也能返回版本 这是怎么回事，猜测cloudera server（5）认oracle jdk,对于sun jdk也需要是在线安装（⾄于是不识别 sun jdk还未验证）。同时还必须是jdk1.7.

![image 17](assets/imageFile17.png)

![image 18](assets/imageFile18.png)

也就是需要执⾏下⾯命令：

1.

sudo apt-get -o Dpkg::Options::=--force-confdef -o Dpkg::Options::=--force-confold -y install oracle-j2sdk1.7

复制代码

原因是在后⾯执⾏scm-server数据库配合的时候，如果找不到Java_home可能会初始化失败。 Java_home默认安装路径如下。

1.

/usr/lib/jvm/java-7-oracle-cloudera

复制代码

从上⾯我们得出，如果想安装顺序，我们最好使⽤下⾯命令来安装jdk1.7 sudo apt-get -o Dpkg::Options::=--force-confdef -o Dpkg::Options::=--force-confold -y install oracle-j2sdk1.7

执⾏上⾯命令，那么还需要做下⾯⼯作：

![image 19](assets/imageFile19.png)

然后我们接着执⾏命令：

1. service cloudera-scm-server restart

复制代码

认为终于好了，但是不幸的是，这次坚持了不到两分钟⼜挂掉了。

![image 20](assets/imageFile20.png)

这次为什么会挂掉，不得不查看⽇志了：

Caused by: org.springframework.beans.factory.BeanCreationException: Error creati ng bean with name 'entityManagerFactoryBean': FactoryBean threw exception on obj ect creation; nested exception is javax.persistence.PersistenceException: org.hi bernate.exception.GenericJDBCException: Could not open connection

at org.springframework.beans.factory.support.FactoryBeanRegistrySupport. doGetObjectFromFactoryBean(FactoryBeanRegistrySupport.java:149)

at org.springframework.beans.factory.support.FactoryBeanRegistrySupport. getObjectFromFactoryBean(FactoryBeanRegistrySupport.java:102)

at org.springframework.beans.factory.support.AbstractBeanFactory.getObje ctForBeanInstance(AbstractBeanFactory.java:1440)

at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBe an(AbstractBeanFactory.java:247)

at org.springframework.beans.factory.support.AbstractBeanFactory.getBean (AbstractBeanFactory.java:192)

at org.springframework.beans.factory.support.BeanDefinitionValueResolver

.resolveReference(BeanDefinitionValueResolver.java:322)

... 17 more Caused by: javax.persistence.PersistenceException: org.hibernate.exception.Gener icJDBCException: Could not open connection

at org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityMan agerImpl.java:1387)

at org.hibernate.ejb.AbstractEntityManagerImpl.convert(AbstractEntityMan agerImpl.java:1310)

at org.hibernate.ejb.AbstractEntityManagerImpl.throwPersistenceException

(AbstractEntityManagerImpl.java:1397) at org.hibernate.ejb.TransactionImpl.begin(TransactionImpl.java:62) at com.cloudera.enterprise.AbstractWrappedEntityManager.beginForRollback

AndReadonly(AbstractWrappedEntityManager.java:85)

at com.cloudera.enterprise.dbutil.DbUtil.isInnoDbEnabled(DbUtil.java:472 )

2014-09-22 17:51:27,328 ERROR [main:spi.SqlExceptionHelper@147] An attempt by a client to checkout a Connection has timed out. 2014-09-22 17:51:27,339 INFO [main:support.DefaultListableBeanFactory@422] Dest roying singletons in org.springframework.beans.factory.support.DefaultListableBe anFactory@715dcdeb: defining beans [commandLineConfigurationBean,entityManagerFa ctoryBean,com.cloudera.server.cmf.TrialState,com.cloudera.server.cmf.TrialManage r,com.cloudera.cmf.crypto.LicenseLoader]; root of factory hierarchy 2014-09-22 17:51:27,341 ERROR [main:cmf.Main@202] Server failed. org.springframework.beans.factory.BeanCreationException: Error creating bean wit h name 'com.cloudera.server.cmf.TrialState': Cannot resolve reference to bean 'e ntityManagerFactoryBean' while setting constructor argument; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean wi th name 'entityManagerFactoryBean': FactoryBean threw exception on object creati on; nested exception is javax.persistence.PersistenceException: org.hibernate.ex ception.GenericJDBCException: Could not open connection

at org.springframework.beans.factory.support.BeanDefinitionValueResolver

.resolveReference(BeanDefinitionValueResolver.java:328) at org.springframework.beans.factory.support.BeanDefinitionValueResolver

.resolveValueIfNecessary(BeanDefinitionValueResolver.java:106)

at org.springframework.beans.factory.support.ConstructorResolver.resolve ConstructorArguments(ConstructorResolver.java:616)

at org.springframework.beans.factory.support.ConstructorResolver.autowir eConstructor(ConstructorResolver.java:148)

at org.springframework.beans.factory.support.AbstractAutowireCapableBean Factory.autowireConstructor(AbstractAutowireCapableBeanFactory.java:1003)

at org.springframework.beans.factory.support.AbstractAutowireCapableBean Factory.createBeanInstance(AbstractAutowireCapableBeanFactory.java:907)

at org.springframework.beans.factory.support.AbstractAutowireCapableBean

Factory.doCreateBean(AbstractAutowireCapableBeanFactory.java:485)

at org.springframework.beans.factory.support.AbstractAutowireCapableBean Factory.createBean(AbstractAutowireCapableBeanFactory.java:456)

at org.springframework.beans.factory.support.AbstractBeanFactory$1.getOb ject(AbstractBeanFactory.java:293)

at org.springframework.beans.factory.support.DefaultSingletonBeanRegistr y.getSingleton(DefaultSingletonBeanRegistry.java:222)

看完⽇志，你是否还是云⾥雾⾥，这不得不说⼀下，我们该如何利⽤⽇志。 ⽇志的作⽤是什么？⽇志的作⽤是提示，它⼤部分会给我们明确说遇到了什么问题，⽐如权限拒 绝，等，但是有些你是看不明⽩的，那我们该如何处理？只能靠你的灵感了，没有灵感怎么办？那 就需要⾃⼰缕顺序，⾃⼰是怎么安装、怎么操作的，然后从中能够发现问题。渐渐的你就能培养出 灵感了。

上⾯其实我只知道报的是⼀个Java异常，⽽且还跟链接有关系。 灵感来了，就像到了，是因为没有执⾏下⾯命令：

配置cloudera-manager-server数据库

1.

sudo /usr/share/cmf/schema/scm_prepare_database.sh mysql -uroot -p --scm-host localhost scm scm scm_password

复制代码

但是⼜出问题了，如下：

![image 21](assets/imageFile21.png)

其实这个问题已经解决了，安装我们可以进⼊上⾯步骤。 当我们安装完毕，ok了，终于好了：

当我们看到下⾯内容的时候，all done，说明我们配置成功了。

![image 22](assets/imageFile22.png)

然后我们再次重启

1. service cloudera-scm-server restart

复制代码

ok始终在坚挺着，没有挂机，问题得到解决
