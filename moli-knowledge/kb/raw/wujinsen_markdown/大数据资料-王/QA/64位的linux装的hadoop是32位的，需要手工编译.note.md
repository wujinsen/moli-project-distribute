/64位的linux装的hadop是32位的 -coco # by coco # 2014-07-02 64位的linux装的hadop是32位的，需要⼿⼯编译。遇到的问题描述： [root@db96 hadoop]# hadoop dfs -put ./in DEPRECATED: Use of this script to execute hdfs command is deprecated. Instead use the hdfs command for it.

14/07/17 17:07:22 WARN util.NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable put: `./in': No such file or directory 原因查找： 查看本地⽂件： [root@db96 hadoop]# file /usr/local/hadoop/lib/native/libhadoop.so.1.0.0 /usr/local/hadoop/lib/native/libhadoop.so.1.0.0: ELF 32-bit LSB shared object,

Intel 80386, version 1 (SYSV), dynamically linked, not stripped 是32位的hadoop，安装在了64位的linux系统上。lib包编译环境不⼀样，所以不能使⽤。 悲剧了，装好的集群没法⽤。

解决办法：重新编译hadoop.//就是重新编译hadoop软件。 (本例⽂是在从库db99上编译。你也可以在 master db96上编译

//只要机器的环境⼀直。) 下载程序代码：(linux下安装svn) # yum install svn [root@db99 data]# /usr/local/svn/bin/svn checkout '

htp:/svn.apache.org/repos/asf/hadop/como n/tags/release-2.2.0

'

[root@db99 release-2.2.0]# ll 总⽤量 84

-rw-r--r-- 1 root root 9968 7⽉ 17 18:51 BUILDING.txt

- drwxr-xr-x 3 root root 4096 7⽉ 17 18:51 dev-support

- drwxr-xr-x 4 root root 4096 7⽉ 17 18:51 hadoop-assemblies drwxr-xr-x 3 root root 4096 7⽉ 17 18:51 hadoop-client drwxr-xr-x 9 root root 4096 7⽉ 17 18:51 hadoop-common-project


- drwxr-xr-x 3 root root 4096 7⽉ 17 18:50 hadoop-dist drwxr-xr-x 7 root root 4096 7⽉ 17 18:51 hadoop-hdfs-project


- drwxr-xr-x 11 root root 4096 7⽉ 17 18:51 hadoop-mapreduce-project


- drwxr-xr-x 4 root root 4096 7⽉ 17 18:51 hadoop-maven-plugins


- drwxr-xr-x 3 root root 4096 7⽉ 17 18:50 hadoop-minicluster

- drwxr-xr-x 4 root root 4096 7⽉ 17 18:50 hadoop-project


- drwxr-xr-x 3 root root 4096 7⽉ 17 18:50 hadoop-project-dist

drwxr-xr-x 12 root root 4096 7⽉ 17 18:50 hadoop-tools

- drwxr-xr-x 4 root root 4096 7⽉ 17 18:51 hadoop-yarn-project


-rw-r--r-- 1 root root 16569 7⽉ 17 18:51 pom.xml

安装开发环境

- 1. 安装必要的包： [root@db99 data]# yum install autoconfautomake libtool cmake ncurses-devel openssl-devel gcc* -nogpgcheck

- 2. 安装maven,下载并解压。 //下载对应的压缩包

apache-maven-3.2.1-bin.tar [root@db99 ~]# tar -xvf apache-maven-3.2.1-bin.tar [root@db99 ~]# tar -xvf apache-maven-3.2.1-bin.tar [root@db99 ~]# ln -s /usr/local/apache-maven-3.2.1/ /usr/local/maven [root@db99 local]# vim /etc/profile //添加环境变量中 export MAVEN_HOME=/usr/local/maven export PATH=$MAVEN_HOME/bin:$PATH

- 3. 安装protobuf


htp:/maven.apache.org/download.cgi

htps:/code.gogle.com/p/protobuf/downloads/detail?name=protobuf-2.5.0.tar.gz

下载：protobuf-2.5.0.tar.gz 并解压 [root@db99 protobuf-2.5.0]# pwd /root/protobuf-2.5.0 [root@db99 protobuf-2.5.0]# ./configure --prefix=/usr/local/protoc/ [root@db99 protobuf-2.5.0]# make [root@db99 protobuf-2.5.0]# make check [root@db99 protobuf-2.5.0]# make install [root@db99 protobuf-2.5.0]# protoc --version libprotoc 2.5.0

安装成功。 添加环境变量： vi /etc/profile export MAVEN_HOME=/usr/local/maven export JAVA_HOME=/usr/java/latest export HADOOP_HOME=/usr/local/hadoop export PATH=.:/usr/local/protoc/bin:$MAVEN_HOME/bin:$JAVA_HOME/bin:$PATH

- 4. 编译hadoop [root@db99 release-2.2.0]# pwd /data/release-2.2.0 [root@db99 release-2.2.0]# ls BUILDING.txt hadoop-common-project hadoop-maven-plugins hadoop-tools dev-support hadoop-dist hadoop-minicluster hadoop-yarn-project hadoop-assemblies hadoop-hdfs-project hadoop-project pom.xml hadoop-client hadoop-mapreduce-project hadoop-project-dist [root@db99 release-2.2.0]# mvn package -Pdist,native -DskipTests -Dtar


..............编译需要较⻓时间⼤概1个⼩时左右。 如果出现如下错误：

[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:2.5.1:testCompile (default-testCompile) on project hadoop-auth: Compilation failure: Compilation failure: [ERROR] /home/hduser/hadoop-2.2.0-src/hadoop-common-project/hadoopauth/src/test/java/org/apache/hadoop/security/authentication/client/AuthenticatorTestCase.java: [88,11] error: cannot access AbstractLifeCycle [ERROR] class file for org.mortbay.component.AbstractLifeCycle not found [ERROR] /home/hduser/hadoop-2.2.0-src/hadoop-common-project/hadoopauth/src/test/java/org/apache/hadoop/security/authentication/client/AuthenticatorTestCase.java: [96,29] error: cannot access LifeCycle [ERROR] class file for org.mortbay.component.LifeCycle not found [ERROR] /home/hduser/hadoop-2.2.0-src/hadoop-common-project/hadoopauth/src/test/java/org/apache/hadoop/security/authentication/client/AuthenticatorTestCase.java: [98,10] error: cannot find symbol [ERROR] symbol: method start() [ERROR] location: variable server of type Server

[ERROR] /home/hduser/hadoop-2.2.0-src/hadoop-common-project/hadoopauth/src/test/java/org/apache/hadoop/security/authentication/client/AuthenticatorTestCase.java: [104,12] error: cannot find symbol [ERROR] -> [Help 1]

需要修改源码下边的hadoop-common-project/hadoop-auth/pom.xml vi ~/hadoop-common-project/hadoop-auth/pom.xml [root@db99 release-2.2.0]# vim /data/release-2.2.0/hadoop-common-project/hadoop-auth/pom.xml 在第55⾏下添加：

- 56 <dependency>

- 57 <groupId>org.mortbay.jetty</groupId>

- 58 <artifactId>jetty-util</artifactId>

- 59 <scope>test</scope>

- 60 </dependency> 保存退出，重新编译即可。


最后编译成功： [INFO] --- maven-javadoc-plugin:2.8.1:jar (module-javadocs) @ hadoop-minicluster --[INFO] Building jar: /data/release-2.2.0/hadoop-minicluster/target/hadoop-minicluster-2.2.0javadoc.jar [INFO] -----------------------------------------------------------------------[INFO] Reactor Summary: [INFO] [INFO] Apache Hadoop Main ................................ SUCCESS [ 1.386 s] [INFO] Apache Hadoop Project POM ......................... SUCCESS [ 1.350 s] [INFO] Apache Hadoop Annotations ......................... SUCCESS [ 2.732 s] [INFO] Apache Hadoop Assemblies .......................... SUCCESS [ 0.358 s] [INFO] Apache Hadoop Project Dist POM .................... SUCCESS [ 2.048 s] [INFO] Apache Hadoop Maven Plugins ....................... SUCCESS [ 3.450 s] [INFO] Apache Hadoop Auth ................................ SUCCESS [ 16.114 s] [INFO] Apache Hadoop Auth Examples ....................... SUCCESS [ 13.317 s] [INFO] Apache Hadoop Common .............................. SUCCESS [05:22 min] [INFO] Apache Hadoop NFS ................................. SUCCESS [ 16.925 s] [INFO] Apache Hadoop Common Project ...................... SUCCESS [ 0.044 s] [INFO] Apache Hadoop HDFS ................................ SUCCESS [02:51 min] [INFO] Apache Hadoop HttpFS .............................. SUCCESS [ 28.601 s] [INFO] Apache Hadoop HDFS BookKeeper Journal ............. SUCCESS [ 27.589 s]

[INFO] Apache Hadoop HDFS-NFS ............................ SUCCESS [ 3.966 s] [INFO] Apache Hadoop HDFS Project ........................ SUCCESS [ 0.044 s] [INFO] hadoop-yarn ....................................... SUCCESS [ 52.846 s] [INFO] hadoop-yarn-api ................................... SUCCESS [ 41.700 s] [INFO] hadoop-yarn-common ................................ SUCCESS [ 25.945 s] [INFO] hadoop-yarn-server ................................ SUCCESS [ 0.105 s] [INFO] hadoop-yarn-server-common ......................... SUCCESS [ 8.436 s] [INFO] hadoop-yarn-server-nodemanager .................... SUCCESS [ 15.659 s] [INFO] hadoop-yarn-server-web-proxy ...................... SUCCESS [ 3.647 s] [INFO] hadoop-yarn-server-resourcemanager ................ SUCCESS [ 12.495 s] [INFO] hadoop-yarn-server-tests .......................... SUCCESS [ 0.684 s] [INFO] hadoop-yarn-client ................................ SUCCESS [ 5.266 s] [INFO] hadoop-yarn-applications .......................... SUCCESS [ 0.102 s] [INFO] hadoop-yarn-applications-distributedshell ......... SUCCESS [ 2.666 s] [INFO] hadoop-mapreduce-client ........................... SUCCESS [ 0.093 s] [INFO] hadoop-mapreduce-client-core ...................... SUCCESS [ 20.092 s] [INFO] hadoop-yarn-applications-unmanaged-am-launcher .... SUCCESS [ 2.783 s] [INFO] hadoop-yarn-site .................................. SUCCESS [ 0.225 s] [INFO] hadoop-yarn-project ............................... SUCCESS [ 36.636 s] [INFO] hadoop-mapreduce-client-common .................... SUCCESS [ 16.645 s] [INFO] hadoop-mapreduce-client-shuffle ................... SUCCESS [ 3.058 s] [INFO] hadoop-mapreduce-client-app ....................... SUCCESS [ 9.441 s] [INFO] hadoop-mapreduce-client-hs ........................ SUCCESS [ 5.482 s] [INFO] hadoop-mapreduce-client-jobclient ................. SUCCESS [ 7.615 s] [INFO] hadoop-mapreduce-client-hs-plugins ................ SUCCESS [ 2.473 s] [INFO] Apache Hadoop MapReduce Examples .................. SUCCESS [ 6.183 s] [INFO] hadoop-mapreduce .................................. SUCCESS [ 6.454 s] [INFO] Apache Hadoop MapReduce Streaming ................. SUCCESS [ 4.802 s] [INFO] Apache Hadoop Distributed Copy .................... SUCCESS [ 27.635 s] [INFO] Apache Hadoop Archives ............................ SUCCESS [ 2.850 s] [INFO] Apache Hadoop Rumen ............................... SUCCESS [ 6.092 s] [INFO] Apache Hadoop Gridmix ............................. SUCCESS [ 4.742 s] [INFO] Apache Hadoop Data Join ........................... SUCCESS [ 3.155 s] [INFO] Apache Hadoop Extras .............................. SUCCESS [ 3.317 s] [INFO] Apache Hadoop Pipes ............................... SUCCESS [ 9.791 s] [INFO] Apache Hadoop Tools Dist .......................... SUCCESS [ 2.680 s] [INFO] Apache Hadoop Tools ............................... SUCCESS [ 0.036 s]

[INFO] Apache Hadoop Distribution ........................ SUCCESS [ 20.765 s] [INFO] Apache Hadoop Client .............................. SUCCESS [ 6.476 s] [INFO] Apache Hadoop Mini-Cluster ........................ SUCCESS [ 0.215 s] [INFO] -----------------------------------------------------------------------[INFO] BUILD SUCCESS [INFO] -----------------------------------------------------------------------[INFO] Total time: 16:32 min [INFO] Finished at: 2014-07-18T01:18:24+08:00 [INFO] Final Memory: 117M/314M [INFO] ------------------------------------------------------------------------

此时编译好的⽂件位于 ~/hadoop-dist/target/hadoop-2.2.0/ ⽬录中 拷⻉hadoop-2.2.0到安装⽬录下，/usr/local/ 重新修改其配置⽂件，重新并格式化，启动，即可。 到此已经不会报错，可以使⽤。 [root@db96 hadoop]# hadoop dfs -put ./in DEPRECATED: Use of this script to execute hdfs command is deprecated. Instead use the hdfs command for it.

put: `.': No such file or directory [root@db96 hadoop]# file /usr/local/hadoop/lib/native/libhadoop.so.1.0.0 /usr/local/hadoop/lib/native/libhadoop.so.1.0.0: ELF 64-bit LSB shared object, x86-64, version 1 (SYSV), dynamically linked, not stripped

测试使⽤：上传⼀个⽂件，下载⼀个⽂件，查看上传⽂件的内容：

[root@db96 ~]# cat wwn.txt # This is a text txt # by coco # 2014-07-18 [root@db96 ~]# hdfs dfs -mkdir /test [root@db96 ~]# hdfs dfs -put wwn.txt /test [root@db96 ~]# hdfs dfs -cat /test/wwn.txt [root@db96 ~]# hdfs dfs -get /test/wwn.txt /tmp

[root@db96 hadoop]# hdfs dfs -rm /test/wwn.txt [root@db96 tmp]# ll 总⽤量 6924

- -rw-r--r-- 1 root root 70 7⽉ 18 11:50 wwn.txt [root@db96 ~]# hadoop dfs -ls /test DEPRECATED: Use of this script to execute hdfs command is deprecated. Instead use the hdfs command for it.

Found 2 items

- -rw-r--r-- 2 root supergroup 6970105 2014-07-18 11:44 /test/gc_comweight.txt

- -rw-r--r-- 2 root supergroup 59 2014-07-18 14:56 /test/hello.txt 到此我们的hdfs⽂件系统已经能正常使⽤。


