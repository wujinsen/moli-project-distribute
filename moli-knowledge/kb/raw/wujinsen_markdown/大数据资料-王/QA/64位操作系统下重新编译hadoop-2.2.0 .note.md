操作系统oracle linux 5.8 64bit

1.yum光盘源设置 vi /etc/yum.repos.d/rhel-debuginfo.repo [base] name=Server Local Sources baseurl=file:///media/Server enabled=1 gpcheck=1 gpgkey=file:///media/RPM-GPG-KEY-oracle

- 2.因为是最⼩化安装的系统，需要安装如下软件包 [root@hadoop1 ~]# yum install lzo-devel zlib-devel gcc autoconf automake libtool ncurses-devel openssl-deve
- 3.安装maven 根据⽹上查得，最新的maven3.1.1与hadoop2.2.0有兼容性问题，这⾥⽤maven3.0.5

[root@hadoop1 ~]# wget

因为下载的是已经编译好的，所以直接解压就可以了 [root@hadoop1 ~]# tar zxvf apache-maven-3.0.5-bin.tar.gz -C /usr/local/ [root@hadoop1 ~]# mv /usr/local/apache-maven-3.0.5/ /usr/local/maven

设置环境变量 [root@hadoop1 local]# vi /etc/profile 添加以下内容 export MAVEN_HOME=/usr/local/maven

- 4.安装ant

[root@hadoop1 ~]# wget [root@hadoop1 ~]# tar zxvf apache-ant-1.9.3-bin.tar.gz -C /usr/local/ [root@hadoop1 ~]# mv /usr/local/apache-ant-1.9.3/ /usr/local/ant [root@hadoop1 ~]# vi /etc/profile export ANT_HOME=/usr/local/ant

- 5.安装findbugs


htp:/mirors.cnic.cn/apache/maven/maven-3/3.0.5/binaries/apache-m aven-3.0.5-bin.tar.gz

htp:/miror.esoc.com/apache/ant/binaries/apache-ant-1.9.3-bin.tar.gz

[root@hadoop1 ~]# wget

htp:/prdownloads.sourceforge.net/findbugs/findbugs-2.0.3.tar.gz?dow nload

[root@hadoop1 ~]# tar zxvf findbugs-2.0.3.tar.gz -C /usr/local/ [root@hadoop1 ~]# mv /usr/local/findbugs-2.0.3/ /usr/local/findbugs [root@hadoop1 ~]# vi /etc/profile export FINDBUGS_HOME=/usr/local/findbugs

- 6.安装protobuf

[root@hadoop1 ~]# wget [root@hadoop1 ~]# tar zxvf protobuf-2.5.0.tar.gz [root@hadoop1 ~]# cd protobuf-2.5.0 [root@hadoop1 protobuf-2.5.0]# ./configure

这⾥报了⼀个错：configure: error: in `/root/protobuf-2.5.0': configure: error: C++ preprocessor "/lib/cpp" fails sanity check

是c++的编译器没有安装

[root@hadoop1 protobuf-2.5.0]# yum install gcc-c++

然后重新编译protobuf并安装 [root@hadoop1 protobuf-2.5.0]# ./configure [root@hadoop1 protobuf-2.5.0]# make [root@hadoop1 protobuf-2.5.0]# make install

- 7.把上⾯的⼏个软件加到PATH变量⾥ [root@hadoop1 ~]# vi /etc/profile

export PATH=$PATH:$MAVEN_HOME/bin:$ANT_HOME/bin:$FINDBUGS_HOME/bin 运⾏profile使得这些配置⽣效 [root@hadoop1 ~]# source /etc/profile

- 8.编译hadoop [root@hadoop1 ~]# wget


htps:/protobuf.goglecode.com/files/protobuf-2.5.0.tar.gz

htp:/miror.esoc.com/apache/hadop/comon/hadop-2.2.0/hadop2.2.0-src.tar.gz

[root@hadoop1 ~]# tar zxvf hadoop-2.2.0-src.tar.gz

这⾥⽹上查得hadoop2.2.0的源码包⾥⾯还有⼀个bug:

htps:/isues.apache.org/jira/browse/HADOP-1010

根据HADOOP-10110.patch，做如下修改： [root@hadoop1 hadoop-2.2.0-src]# vi hadoop-common-project/hadoop-auth/pom.xml

</dependency> <dependency>

<groupId>org.mortbay.jetty</groupId>

+ <artifactId>jetty-util</artifactId>

+ <scope>test</scope>

+ </dependency>

+ <dependency>

+ <groupId>org.mortbay.jetty</groupId> <artifactId>jetty</artifactId> <scope>test</scope>

</dependency>

然后就开始编译hadoop了 [root@hadoop1 hadoop-2.2.0-src]# mvn package -DskipTests -Pdist,native -Dtar

没想到编译过程也是需要配JAVA_HOME的 [root@hadoop1 hadoop-2.2.0-src]# export JAVA_HOME=/home/hadoop/jdk [root@hadoop1 hadoop-2.2.0-src]# export PATH=$PATH:$JAVA_HOME/bin 再重新来 [root@hadoop1 hadoop-2.2.0-src]# mvn package -DskipTests -Pdist,native -Dtar

之后就是漫⻓的等待了。这⾥还有⼀个要注意的是这个过程⾥⾯它会⾃⼰到⽹上下载很多东⻄的，可别把⽹断了。

最终还是报了⼀个错： [ERROR] Failed to execute goal org.apache.maven.plugins:maven-antrun-plugin:1.6:run (make) on project hadoop-common: An Ant BuildException has occured: Execute failed: java.io.IOException: Cannot run program "cmake" (in directory "/root/hadoop-2.2.0src/hadoop-common-project/hadoop-common/target/native"): error=2, No such file or directory -> [Help 1]

似乎是没有安装cmake [root@hadoop1 ~]# cd [root@hadoop1 ~]# wget [root@hadoop1 ~]# tar zxvf cmake-2.8.12.2.tar.gz [root@hadoop1 ~]# cd cmake-2.8.12.2 [root@hadoop1 ~]# ./configure [root@hadoop1 ~]# gmake [root@hadoop1 ~]# gmake install

# htp:/ w.cmake.org/files/v2.8/cmake-2.8.12.2.tar.gz

安装好cmake后再来⼀次 [root@hadoop1 ~]# cd [root@hadoop1 ~]# cd hadoop-2.2.0-src [root@hadoop1 hadoop-2.2.0-src]# mvn package -DskipTests -Pdist,native -Dtar

这次报了这么⼀个错 [ERROR] Failed to execute goal org.apache.maven.plugins:maven-antrun-plugin:1.6:run (make) on project hadoop-hdfs: An Ant BuildException has occured: exec returned: 1 -> [Help 1] [ERROR] [ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch. [ERROR] Re-run Maven using the -X switch to enable full debug logging. [ERROR] [ERROR] For more information about the errors and possible solutions, please read the following articles:[ERROR] [Help 1]

# htp:/cwiki.apache.org/confluence/display/MAVEN/MojoEx ecutionException

[ERROR] [ERROR] After correcting the problems, you can resume the build with the command[ERROR] mvn <goals> -rf :hadoop-hdfs

没看出什么个意思，再往上看，注意到了这⼀段

[exec] CMake Error at /usr/local/share/cmake2.8/Modules/FindPackageHandleStandardArgs.cmake:108 (message): [exec] Could NOT find PkgConfig (missing: PKG_CONFIG_EXECUTABLE) [exec] Call Stack (most recent call first): [exec] /usr/local/share/cmake-2.8/Modules/FindPkgConfig.cmake:102 (find_package_handle_standard_args) [exec] main/native/fuse-dfs/CMakeLists.txt:23 (find_package)

安装pkgconfig [root@hadoop1 hadoop-2.2.0-src]# yum install pkgconfig

继续，这次把安装提示重新定向到⼀个⽂件⾥，⽅便查看错误： [root@hadoop1 hadoop-2.2.0-src]# mvn package -DskipTests -Pdist,native -Dtar > install2.log

错误：

[exec] CMake Error at /usr/local/share/cmake2.8/Modules/FindPackageHandleStandardArgs.cmake:108 (message): [exec] Could NOT find OpenSSL, try to set the path to OpenSSL root folder in the

[exec] system variable OPENSSL_ROOT_DIR (missing: OPENSSL_LIBRARIES [exec] OPENSSL_INCLUDE_DIR) [exec] Call Stack (most recent call first): [exec] /usr/local/share/cmake-2.8/Modules/FindPackageHandleStandardArgs.cmake:315 (_FPHSA_FAILURE_MESSAGE) [exec] /usr/local/share/cmake-2.8/Modules/FindOpenSSL.cmake:313 (find_package_handle_standard_args) [exec] CMakeLists.txt:20 (find_package) [exec] [exec] [exec] -- Configuring incomplete, errors occurred! [exec] /usr/local/share/cmake-2.8/Modules/FindPackageHandleStandardArgs.cmake:315 (_FPHSA_FAILURE_MESSAGE)

[root@hadoop1 ~]# yum install openssl Loaded plugins: rhnplugin, securityThis system is not registered with ULN.ULN support will be disabled.Setting up Install ProcessPackage openssl-0.9.8e-22.el5.x86_64 already installed and latest versionPackage openssl-0.9.8e-22.el5.i686 already installed and latest versionNothing to do

openssl已经安装了，那就把openssl-devel安装⼀下 [root@hadoop1 ~]# yum install openssl-devel

继续： [root@hadoop1 hadoop-2.2.0-src]# mvn package -DskipTests -Pdist,native -Dtar > install3.log

这次终于完成了，验证⼀下： [root@hadoop1 hadoop-2.2.0-src]# cd hadoop-dist/target/hadoop-2.2.0/lib/native/ [root@hadoop1 native]# file libhadoop.so.1.0.0 libhadoop.so.1.0.0: ELF 64-bit LSB shared object, AMD x86-64, version 1 (SYSV), not stripped hadoop-dist/target⽬录下hadoop-2.2.0.tar.gz也有了，以后应该就可以直接⽤了。

# 懒得再编译的朋友可以直接在这⾥下载我编译好的 htp:/pan.baidu.com/s/1eQAB4rg

