---
title: JIRA 7.8 版本的安装与破解.note（原文插图 annex）
slug: annex-JIRA-7.8-版本的安装与破解
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/DevOps/jira/JIRA 7.8 版本的安装与破解.note.md
related: [jenkins-ci入门]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/houchaoying/p/909618.html

jira的运⾏是依赖java环境的，也就是说需要安装jdk并且要是1.8以上版本

![image 1](assets/imageFile1.png)

除此之外，我们还需要安装MySQL，为jira创建对应的数据库、⽤户名和密码，如下： 注意：建库名jira,字符集为UTF-8 mysql -urot -p'kans123QWE' -e "create database jira default character set utf8 colate utf8_bin;grant al on jira.* to 'jira@ʼ%' identified by 'jirapaswd';"

![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)

![image 4](assets/imageFile4.png)

以上环境准备完毕后，我们现在开始下载并安装jira。 查看Linux系统是多少位的下载相应的版本

![image 5](assets/imageFile5.png)

jira 的下载⽹站

htps:/ w.atlasian.com/software/jira/update

![image 6](assets/imageFile6.png)

wget htps:/downloads.atlasian.com/software/jira/downloads/atlasian-jira-software-7.8.1-x64.bin

![image 7](assets/imageFile7.png)

现在下载完成后开始安装jira

![image 8](assets/imageFile8.png)

![image 9](assets/imageFile9.png)

![image 10](assets/imageFile10.png)

![image 11](assets/imageFile11.png)

![image 12](assets/imageFile12.png)

通过上图，我们可以很明显的看出jira安装到了/opt/atlasian/jira和/var/atlasian/aplication-data/jira⽬录下，并且jira监听 的端⼝是8080。 jira的主要配置⽂件，存放在/opt/atlasian/jira/conf/server.xml⽂件中

vim /opt/atlasian/jira/conf/server.xml

![image 13](assets/imageFile13.png)

现在启动

![image 14](assets/imageFile14.png)

![image 15](assets/imageFile15.png)

现在我们先关闭jira，然后把破解包⾥⾯的atlasian-extras-3.2.jar和mysql-conector-java-5.1.39-bin.jar两个⽂件复制 到/opt/atlasian/jira/atlasian-jira/WEB-INF/lib/⽬录下。 其中atlasian-extras-3.2.jar是⽤来替换原来的atlasian-extras-3.2.jar⽂件，⽤作破解jira系统的。 ⽽mysql-conector-java-5.1.39-bin.jar是⽤来连接mysql数据库的驱动软件包。

关闭

![image 16](assets/imageFile16.png)

在/opt/atlasian/jira/atlasian-jira/WEB-INF/lib/这个⽬录下，找到atlasian-extras-的包看看是3点⼏的 然后现在对应的破 解包，替换这个

![image 17](assets/imageFile17.png)

替换

![image 18](assets/imageFile18.png)

放置连接mysql数据库的包

![image 19](assets/imageFile19.png)

然后启动 就可以ip:8080访问了

![image 20](assets/imageFile20.png)

ip:8080⻚⾯安装

![image 21](assets/imageFile21.png)

![image 22](assets/imageFile22.png)

填写好后测试连接⼀下看看是否成功，在下⼀步

![image 23](assets/imageFile23.png)

然后下⼀步，因为要初始化数据库 要等会

![image 24](assets/imageFile24.png)

⽽连接数据库的配置是/var/atlasian/aplication-data/jira/dbconfig.xml，如下： cat /var/atlasian/aplication-data/jira/dbconfig.xml

![image 25](assets/imageFile25.png)

下⾯的配置就⽐较简单了，⾃定义也可以，默认也可以。

![image 26](assets/imageFile26.png)

注意：上图中的Mode中，我们在此使⽤的是Private（私有）模式，在这个模式下，⽤户的创建需要由管理员创建。⽽在 Public（共⽤）模式下，⽤户是可以⾃⼰进⾏注册。 下⾯这个⻚⾯是需要我们输⼊jira的license，如下：

![image 27](assets/imageFile27.png)

注意：上图中的Server ID：BSG9-24QF-8M40-O1CT 因为我们没有正式的license，所以需要我们在jira官⽹注册⼀个账号，然后利⽤这个账号申请⼀个可以试⽤30天的license，点 击⽣成jira许可证。如下：

![image 28](assets/imageFile28.png)

注意：这个图中的Server ID就是我们上⾯刚刚截图的Server ID。

![image 29](assets/imageFile29.png)

点击yes 上⾯的key 就会⾃动复制到你的许可征

![image 30](assets/imageFile30.png)

密码我设的lilili⽤户lili

![image 31](assets/imageFile31.png)

![image 32](assets/imageFile32.png)

![image 33](assets/imageFile33.png)

![image 34](assets/imageFile34.png)

## 创建第⼀个项⽬，如下：

![image 35](assets/imageFile35.png)

![image 36](assets/imageFile36.png)

![image 37](assets/imageFile37.png)

![image 38](assets/imageFile38.png)

![image 39](assets/imageFile39.png)

到此 jira 7.8的安装就好了，现在看看jira的破解 破解jira，其实我们已经破解了在前⾯复制atlasian-extras-3.1.2.jar到/opt/atlasian/jira/atlasian-jira/WEB-INF/lib/⽬录下 时，再次启动jira时就已经破解了。

我们现在登陆到jira中查看授权信息，如下：

![image 40](assets/imageFile40.png)

![image 41](assets/imageFile41.png)

通过上图，我们可以很明显的看到jira我们可以使⽤到203年，。到此有关jira的安装、破解就已经全部结束。 如何修改内存？ vim /opt/atlasian/jira/bin/setenv.sh

![image 42](assets/imageFile42.png)

⽇志查看： tail -f /opt/atlasian/jira/logs/catalina.out

# 软件包连接

jira 软件包 ： 链接：htps:/pan.baidu.com/s/1uQzTpTRxgEndi8ZyiaKOIA 提取码：v4v6 复制这段内容后打开百度⽹盘⼿机Ap，操作更⽅便哦

破解包： 链接：htps:/pan.baidu.com/s/1PBGgxijNd1W5d4rMqKJ2Lg 提取码：k1pk 复制这段内容后打开百度⽹盘⼿机Ap，操作更⽅便哦

mysql驱动包： 链接：htps:/pan.baidu.com/s/163QIGmVOznzXamEJG8JQw 提取码：4nak 复制这段内容后打开百度⽹盘⼿机Ap，操作更⽅便哦
