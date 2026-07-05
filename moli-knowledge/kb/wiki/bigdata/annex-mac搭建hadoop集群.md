---
title: mac搭建hadoop集群.note（原文插图 annex）
slug: annex-mac搭建hadoop集群
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Hadoop/安装部署/mac搭建hadoop集群.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

## htps:/ w.cnblogs.com/taojietaoge/p/10803537.html

You have to work very hard to believe that you are realy powerles.

Mac-搭建Hadop集群

我⽤到了：VMware Fusion、CentOS7、FileZila、jdk-8u181-linux-x64.tar.gz和hadop-2.7.6.tar.gz

- 1、集群部署规划 NameNode单点部署：
- 2、三台客户机相关准备


<table>
  <tr>
    <th>节点名称</th>
    <th>N1</th>
    <th>N2</th>
    <th>DN</th>
    <th>RM</th>
    <th>NM</th>
    <th>规划IP</th>
    <th>other</th>
  </tr>
  <tr>
    <td>tjt01</td>
    <td>NameNode</td>
    <td> </td>
    <td>DataNode</td>
    <td> </td>
    <td>NodeMana</td>
    <td>172.16.14.</td>
    <td>hive/hdfs</td>
  </tr>
  <tr>
    <td>tjt02</td>
    <td> </td>
    <td>Secondary</td>
    <td>DataNode</td>
    <td>ResourceM</td>
    <td>ger NodeMana</td>
    <td>130 172.16.14.</td>
    <td>hbase/kms</td>
  </tr>
  <tr>
    <td>tjt03</td>
    <td> </td>
    <td>NameNode</td>
    <td>DataNode</td>
    <td>anager</td>
    <td>ger NodeMana</td>
    <td>131 172.16.14.</td>
    <td>mysql/spar</td>
  </tr>
</table>


ger 132 k

- 2.1、安装VMware虚拟机 在虚拟机中安装CentOS镜像，由初始安装的CentOS7版本的镜像，完整克隆出另外两台虚拟机


![image 1](assets/imageFile1.png)

- 2.2、修改主机名 [root@tjt01 tjt]# vi /etc/hostname 分别修改三台虚拟机主机名：tjt01、tjt02、tjt03

- 2.3、修改host⽂件 配置主机host： [root@tjt01 tjt]# vi /etc/hosts


![image 2](assets/imageFile2.png)

将配置发送到其他的主机，同时在其他主机上配置：

- scp -r /etc/hosts root@tjt02:/etc/

- scp -r /etc/hosts root@tjt03:/etc/


![image 3](assets/imageFile3.png)

测试host⽂件修改结果：

- ping tjt01

- ping tjt02

- ping tjt03


![image 4](assets/imageFile4.png)

- 2.4、设置 SH免密登录 每两台主机之间设置免密码，⾃⼰的主机与⾃⼰的主机之间也要求设置免密码； 输⼊： sh-keygen -t rsa 然后按下四次回⻋，之后在把密匙发到其他主机上，输⼊： sh-copy-id tjt01 并按提示输⼊密码，然后是


sh-copy-id 02和 sh-copy-id 03同样的操作； 之后，在另外两台虚拟机上也执⾏相同的步骤：

ssh-keygen -t rsa

- ssh-copy-id tjt01

- ssh-copy-id tjt02

- ssh-copy-id tjt03


测试 SH免密登录：

ssh tjt01、ssh tjt02、ssh tjt03

![image 5](assets/imageFile5.png)

- 2.5、安装配置JDK 虚拟机默认安装了⼀个openjdk，卸载掉： rpm -qa | grep jdk


可以查看已经安装了的openjdk yum remove *openjdk* ⼲掉openjdk yum remove copy-jdk-configs-3.3-10.el7_5.noarch ⼲掉jdk-configs

![image 6](assets/imageFile6.png)

![image 7](assets/imageFile7.png)

将JDK安装包上传到/opt下，可以通过XShel的rz上传，也可以⽤FileZila：

![image 8](assets/imageFile8.png)

到/opt ⽬录下解压：tar xzvf jdk-8u181-linux-x64.tar.gz

![image 9](assets/imageFile9.png)

设置JAVA_HOME： 输⼊：vi /etc/profile，在profile⽂件中的编辑模式下加上下⽅export配置： export JAVA_HOME=/opt/jdk1.8.0_181 export PATH=$PATH:$JAVA_H-eOME/bin:$JAVA_HOME/sbin 修改好之后使⽂件⽣效：source /etc/profile

![image 10](assets/imageFile10.png)

复制JDK到另外两个节点： 在这之前要先把另外两台虚拟机上的openjdk也⼲掉： yum remove *openjdk* yum remove copy-jdk-confi gs-3.3-10.el7_5.noarch 然后复制jdk到另外两个虚拟机上：

- scp -r /opt/jdk1.8.0_181 root@tjt02:/opt/

- scp -r /opt/jdk1.8.0_181 root@tjt03:/opt/ 向其他节点复制profile⽂件：


- scp /etc/profile root@tjt02:/etc/

- scp /etc/profile root@tjt03:/etc/


![image 11](assets/imageFile11.png)

然后每个节点分别执⾏ source /etc/profile ，使profile⽣效下，并通过java-version简单测试下，jdk复制是否 成功：

- tjt02:


![image 12](assets/imageFile12.png)

- tjt03:


![image 13](assets/imageFile13.png)

# 3、安装Hadop

- 3.1、上传并解压Hadop

解压：tar zxvf hadop-2.7.6.tar.gz

- 3.2、搭建Hadop集群 配置⽂件在hadop2.7.6/etc/hadop/下，修改设置hadop2.7.6⽬录下的可执⾏权限


![image 14](assets/imageFile14.png)

![image 15](assets/imageFile15.png)

- 3.2.1、修改 core-site.xml [root@tjt01 hadoop]# vi core-site.xml 然后在core-site.xml⽂件中编辑如下：


<table>
  <tr>
    <th>![image 16](assets/imageFile16.png)</th>
  </tr>
</table>


<!-- 指定HDFS中NameNode的地址 --> <property> <name>fs.defaultFS</name>

<value>hdfs://tjt01:9000</value> </property>

<!-- 指定hadoop运⾏时产⽣⽂件的存储⽬录 --> <property> <name>hadoop.tmp.dir</name> <value>/opt/hadoop-2.7.6/data/full/tmp</value> </property>

<table>
  <tr>
    <th>![image 17](assets/imageFile17.png)</th>
  </tr>
</table>


![image 18](assets/imageFile18.png)

- 3.2.2、修改hadop-env.sh [root@tjt01 hadoop]# vi hadoop-env.sh 修改JAVA_HOME：
- 3.2.3 修改hdfs-site.xml [root@tjt01 hadoop]# vi hdfs-site.xml 修改 hdfs-site.xml 的配置如下：


![image 19](assets/imageFile19.png)

<table>
  <tr>
    <th>![image 20](assets/imageFile20.png)</th>
  </tr>
</table>


<configuration>  <!-- 设置dfs副本数，不设置默认是3个 -->

<property> <name>dfs.replication</name> <value>2</value>

</property>  <!-- 设置secondname的端⼝ --> <property>

<name>dfs.namenode.secondary.http-address</name>

<value>tjt02:50090</value> </property>

</configuration>

<table>
  <tr>
    <th>![image 21](assets/imageFile21.png)</th>
  </tr>
</table>


![image 22](assets/imageFile22.png)

- 3.2.4 修改 slaves [root@tjt01 hadoop]# vi slaves 增加slaves 配置如下：

- tjt01

- tjt02

- tjt03


- 3.2.5 修改mapred-env.sh [root@tjt01 hadoop]# vi mapred-env.sh 修改其JAVA_HOME如下： export JAVA_HOME=/opt/jdk1.8.0_181

- 3.2.6 修改mapred-site.xml [root@tjt01 hadoop]# mv mapred-site.xml.template mapred-site.xml [root@tjt01 hadoop]# vi mapred-site.xml 修改其configuration如下：


![image 23](assets/imageFile23.png)

![image 24](assets/imageFile24.png)

<table>
  <tr>
    <th>![image 25](assets/imageFile25.png)</th>
  </tr>
</table>


<configuration> <!-- 指定mr运⾏在yarn上 -->

<property> <name>mapreduce.framework.name</name> <value>yarn</value>

</property> </configuration>

<table>
  <tr>
    <th>![image 26](assets/imageFile26.png)</th>
  </tr>
</table>


![image 27](assets/imageFile27.png)

- 3.2.7 修改yarn-env.sh [root@tjt01 hadoop]# vi yarn-env.sh 修改其JAVA_HOME如下： export JAVA_HOME=/opt/jdk1.8.0_181

- 3.2.8 修改yarn-site.xml [root@tjt01 hadoop]# vi yarn-site.xml 修改配置如下：


![image 28](assets/imageFile28.png)

<table>
  <tr>
    <th>![image 29](assets/imageFile29.png)</th>
  </tr>
</table>


<configuration> <!-- reducer获取数据的⽅式 --> <property>

<name>yarn.nodemanager.aux-services</name> <value>mapreduce_shuffle</value>

</property> <!-- 指定YARN的ResourceManager的地址 --> <property>

<name>yarn.resourcemanager.hostname</name> <value>tjt02</value>

</property>

<property> <name>yarn.nodemanager.vmem-check-enabled</name> <value>false</value> <description>Whether virtual memory limits will be enforced for

containers</description> </property> <property>

<name>yarn.nodemanager.vmem-pmem-ratio</name> <value>4</value> <description>Ratio between virtual memory to physical memory when setting memory

limits for containers</description>

</property> </configuration>

<table>
  <tr>
    <th>![image 30](assets/imageFile30.png)</th>
  </tr>
</table>


![image 31](assets/imageFile31.png)

- 3.3 分发hadop到各个节点

- [root@tjt01 hadoop]# scp -r /opt/hadoop-2.7.6/ root@tjt02:/opt

- [root@tjt01 hadoop]# scp -r /opt/hadoop-2.7.6/ root@tjt03:/opt


- 3.4 配置环境变量 [root@tjt01 hadoop]# vi /etc/profile 修改配置如下： export HADOOP_HOME=/opt/hadoop-2.7.6 export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin

source /etc/profile 使profile⽂件⽣效； [root@tjt01 hadoop]# source /etc/profile

- 3.5 分发profile到各个节点


![image 32](assets/imageFile32.png)

- [root@tjt01 hadoop]# scp /etc/profile root@tjt02:/etc/

- [root@tjt01 hadoop]# scp /etc/profile root@tjt03:/etc/


![image 33](assets/imageFile33.png)

到各⾃的服务节点上是profile ⽣效：

<table>
  <tr>
    <th>1</th>
    <th>[root@tjt02 ~]# source /etc/profile<br><br></th>
  </tr>
</table>


[root@tjt03 ~]# source /etc/profile

# 4 启动验证集群

- 4.1 启动集群


- 4.1.1 如果集群是第⼀次启动，需要格式化namenode [root@tjt01 hadoop]# hdfs namenode -format 当看到19/05/03 03 45 47 INFO comon.Storage: Storage directory /opt/hadop2.7.6/data/ful/tmp/dfs/name has ben sucesfuly formated. 就格式化OK了； 到此为⽌，上⾯存在有⼀个错误，不能在hdfs-site.xml⽂件中这么写注释【#tjt】：
- 4.1.2 启动Hdfs

- [root@tjt01 hadoop-2.7.6]# start-dfs.sh


- 4.1.3 启动Yarn 如果Namenode和ResourceManager不是同⼀台虚拟机的话，不能在NameNode上启动yarn，应该在


![image 34](assets/imageFile34.png)

![image 35](assets/imageFile35.png)

![image 36](assets/imageFile36.png)

ResourceManager所在的机器上启动yarn；我的yarn配置在tjt02服务器上，⼀次需要到tjt02机器上启动yarn

![image 37](assets/imageFile37.png)

- [root@tjt02 hadoop]# start-yarn.sh


![image 38](assets/imageFile38.png)

- 4.1.4 jps查看进程

- [root@tjt01 hadoop]# jps

- [root@tjt02 hadoop]# jps

- [root@tjt03 hadoop]# jps


- 4.1.5 web⻚⾯访问


![image 39](assets/imageFile39.png)

![image 40](assets/imageFile40.png)

![image 41](assets/imageFile41.png)

- 在虚拟机tjt01上访问：htp:/172.16.14.130 5070


![image 42](assets/imageFile42.png)

Datanode:

![image 43](assets/imageFile43.png)

需要先在tjt3这台机器上关闭防⽕墙后，才可以在⾮linux服务器中的浏览器访问：

//临时关闭 systemctl stop firewalld //禁⽌开机启动 systemctl disable firewalld

本机访问：htp:/172.16.14.130 5070/

![image 44](assets/imageFile44.png)

- 在虚拟机tjt02上访问：htp:/172.16.14.131 808/cluster


![image 45](assets/imageFile45.png)

4.2、Hadop停⽌启动⽅式 1）各个服务组件逐⼀启动 分别启动hdfs 组件： hadoop-deamon.sh start | stop namenode | datnode | secondarynamenode 启动yarn： yarn-deamon.sh start | stop resourcemanager | nodemanager

- 2) 各个模块分开启动(常⽤) start | stop-dfs.sh start | stop-yarn.sh

- 3) 全部启动 start | stop-all.sh 其他


- 1、关闭防⽕墙


//临时关闭 systemctl stop firewalld //禁⽌开机启动

systemctl disable firewalld

![image 46](assets/imageFile46.png)

- 2、创建⽤户，设置⽂件权限 创建⽤户，修改密码： [root@tjt01 ~]# useradd tjt [root@tjt01 ~]# passwd tjt


![image 47](assets/imageFile47.png)

设置tjt⽤户具有rot权限 修改 /etc/sudoers ⽂件，找到下⾯⼀⾏，在rot下⾯添加⼀⾏，如下所示:

![image 48](assets/imageFile48.png)

修改完毕，现在可以⽤tjt帐号登录，然后⽤命令 su - ，即可获得rot权限进⾏操作。

![image 49](assets/imageFile49.png)

其实，当Web⻚⾯访问：htp:/172.16.14.130 5070，可以看到hadop⻚⾯时Hadop集群就成功构建好 了。
