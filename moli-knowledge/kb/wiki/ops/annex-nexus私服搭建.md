---
title: nexus私服搭建.note（原文插图 annex）
slug: annex-nexus私服搭建
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/DevOps/nexus/nexus私服搭建.note.md
related: [jenkins-ci入门]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/qdhxhz/p/9801325.html

# Linux搭建Nexus3.X私服

备注:linux版本: ubuntu 同时已经部署好JDK8环境

⼀、linux安装nexus

- 1、创建⽂件夹并进⼊该⽬录

cd /usr/local && mkdir nexus && cd nexus

- 2、下载nexus安装包

wget http://sonatype-download.global.ssl.fastly.net/nexus/3/nexus-3.6.0-02-unix.tar.gz

- 3、解压、重命名

tar -zxvf nexus-3.6.0-02-unix.tar.gz //解压 mv nexus-3.6.0-02 nexus //重命名

- 4、⾃定义配置虚拟机可打开 nexus.vmoptions ⽂件进⾏配置


如果Linux硬件配置⽐较低的话，建议修改为合适的⼤⼩，否则会出现运⾏崩溃的现象

# vim nexus/bin/nexus.vmoptions //虚拟机选项配置⽂件

5、启动nexus

启动 Nexus（默认端⼝是8081），Nexus 常⽤的⼀些命令包 括：/usr/local/nexus/nexus/bin/nexus {start|stop|run|run-redirect|status|restart|forcereload}，下⾯我们启动Nexus： 启动命令：bin/nexus start

![image 1](assets/imageFile1.png)

说明已经安装启动成功，上⾯报错意思是nexus建议不要⽤root⽤户来启动nexus，但并不影响启动, 如果要去除这个可以百度。

6、其它说明

- 1）Nexus默认的端⼝是8081，可以在etc/nexus-default.properties配置中修改。
- 2）Nexus默认的⽤户名密码是admin/admin123
- 3）当遇到奇怪问题时，重启nexus，启动时间会⽐较慢 要1分钟左右后才能访问。
- 4）Nexus的⼯作⽬录是sonatype-work（路径⼀般在nexus同级⽬录下）


⼆、仓库介绍

当访问：http://ip:8081/ 会有nexus界⾯ ⽤户登陆，进⼊Repositories⻚⾯点击Nexus“Log in”，输⼊默认⽤户名（admin）和默认密码 （admin123）登录。后找到Repositories

![image 2](assets/imageFile2.png)

1、四种仓库类型介绍

默认仓库介绍

- 1）maven-central： maven中央库，默认从https://repo1.maven.org/maven2/拉取jar
- 2）maven-releases： 私库发⾏版jar
- 3）maven-snapshots：私库快照（调试版本）jar
- 4）maven-public： 仓库分组，把上⾯三个仓库组合在⼀起对外提供服务，在本地maven基础


配置settings.xml中使⽤。 Nexus默认的仓库类型有以下四种：（上⾯的名字可以随便取，关键是它对应的是什么仓库类型）

- 1）group(仓库组类型)：⼜叫组仓库，⽤于⽅便开发⼈员⾃⼰设定的仓库；
- 2）hosted(宿主类型)：内部项⽬的发布仓库（内部开发⼈员，发布上去存放的仓库）；
- 3）proxy(代理类型)： 从远程中央仓库中寻找数据的仓库（可以点击对应的仓库的Configuration

⻚签下Remote Storage Location属性的值即被代理的远程仓库的路径）；

- 4）virtual(虚拟类型)： 虚拟仓库（这个基本⽤不到，重点关注上⾯三个仓库的使⽤）；


Policy(策略):表示该仓库为发布(Release)版本仓库还是快照(Snapshot)版本仓库；

2、仓库拉取jar包流程

- 1）Maven可直接从宿主仓库下载构件,也可以从代理仓库下载构件,⽽代理仓库间接的从远程仓库下载 并缓存构件
- 2）为了⽅便,Maven可以从仓库组下载构件,⽽仓库组并没有时间的内容(下图中⽤虚线表示,它会转向 包含的宿主仓库或者代理仓库获得实际构件的内容).


![image 3](assets/imageFile3.png)

## ⼆、创建新⽤户和新仓库

- 1、创建⽤户


登陆admin后，可以点击上⾯的“设置”图标，在“设置”⾥可以添加⽤户、⻆⾊，对接LDAP等的设置， 如下：

![image 4](assets/imageFile4.png)

![image 5](assets/imageFile5.png)

创建⽤户好后，重新登陆创建的⽤户

![image 6](assets/imageFile6.png)

- 2、创建仓库


上⾯对仓库的概率清楚后，我们开始创建新仓库，主要创建3个仓库 proxy仓库 作⽤是去远程拉取jar包 hosted仓库 作⽤是存放本地上传的三⽅jar包 group仓库 作⽤是将上⾯来个放到这个组⾥，进⾏统⼀管理

- （1）proxy 代理仓库创建 中央仓库的代理默认为https://repo1.maven.org/maven2/ 我们可以更换成阿⾥云中央仓库。
- （2）hosted 仓库创建


![image 7](assets/imageFile7.png)

host仓库这⾥了两种不同的存储类型

![image 8](assets/imageFile8.png)

![image 9](assets/imageFile9.png)

#### （3）group仓库 主要就是把上⾯三个仓库放到该组⾥，然后让这个组给外⾯访问。

![image 10](assets/imageFile10.png)

### 全家福来⼀个

![image 11](assets/imageFile11.png)

有关在linux服务器上搭建nexus的⼯作已经完成，接下来就需要配置maven相关配置⽂件，来测试该 仓库的是否成功，下⼀篇博客详细写配置信息。

### 参考

- 1、 使⽤ Nexus 3.x 搭建 Maven2 私服
- 2、 Linux 使⽤ Nexus3.x 搭建 Maven 私服指南
- 3、linux部署Nexus OSS


（对组解释很好）

（⼀整套讲的还是蛮清晰的） （这⾥⾯将默认镜像改成了阿⾥云镜像）

![image 12](assets/imageFile12.png)

如果⼀个⼈充满快乐，正⾯的思想，那么好的⼈事物就会和他共鸣，⽽且被他吸引过来。同样，⼀个 ⼈⽼带悲伤，倒霉的事情也会跟过来。

——在⾃⼰⼼情低落的时候，告 诫⾃⼰不要把负能量带给别⼈。（⼤校1
