本章内容

安装JDK

安装SCALA

安装Spark

第⼀部分：安装JDK

- 1、查看当前系统上是否安装了JDK 进⼊⼀个新的系统，⼀般来讲是没有安装过JDK的。但是有些linux的版本会默认安装openJDK。

open JDK实在oracle公司开源的代码上进⾏同步开发并发布的版本。 如果要查看当前系统是否已经安装了JDK，可以使⽤命令：sudo update-alternatives-config java

- 2、下载JDK 由于当前系统中并没有我们需要的oracle JDK，我们需要⾃⼰安装⼀个。 先在oracle的⽹站上寻找JDK的下载地址，然后使⽤wget命令进⾏下载。或者，你可以将数据下载


![image 1](<1、安装spark环境.note_images/imageFile1.png>)

到电脑上，然后上传到linux上。

wget

htp:/download.oracle.com/otn-pub/java/jdk/8u60-b27/jdk-8u60-linux-x64.tar.gz?AuthParam

=14170527_0f80e68acf71ce8da8af237439f406a

![image 2](<1、安装spark环境.note_images/imageFile2.png>)

- 3、解压安装包 mv jdk-8u60-linux* jdk-8u60-linux-x64.tar.gz tar -zxvf jdk-8u60-linux-x64.tar.gz -C./instal/
- 4、配置JDK部署信息到环境变量 #set java env export JAVA_HOME=/export/servers/jdk export JRE_HOME=${JAVA_HOME}/jre export CLASPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib export PATH=${JAVA_HOME}/bin:$PATH

- 5、让配置信息⽣效 source /etc/profile
- 6、将oracle JDK 配置成默认的JDK. update-alternatives-instal /usr/bin/java java /export/servers/jdk/bin/java 30 update-alternatives-instal /usr/bin/javac javac /export/servers/jdk/bin/javac 30
- 7、产看当前机器的JDK版本


![image 3](<1、安装spark环境.note_images/imageFile3.png>)

# 第⼆部分：安装scala

- 1、下载scala 拖动到⻚⾯最下⾯ ，找到linux版本 wget
- 2、解压scala并安装 mv scala-2.1.8.tgz /export/software/ tar -xzvf scala-2.1.8.tgz -C./servers/ cd./servers/ ln -s scala-2.1.8 scala
- 3、配置环境变量 vi /etc/profile


htp:/ w.scala-lang.org/download/2.1.8.html htp:/downloads.lightbend.com/scala/2.1.8/scala-2.1.8.tgz

输⼊以下内容：#set scala env export SCALA_HOME=/export/servers/scala

export PATH=${SCALA_HOME}/bin:$PATH 使配置⽣效：

source /etc/profile

- 4、在每台机器上验证scala是否安装成功 输⼊命令：scala 第三部分：安装Spark1、下载spark安


装包

htp:/spark.apache.org/downloads.html htp:/apache.openca s.org/spark/spark-1.6.1/spark-1.6.1-bin-hadop2.6.tgz

选择对应的hadop版本号 点击镜像地址2、解压spark并安装

mv spark-1.6.1-bin-hadop2.6.tgz /export/software/ tar -zxvf spark-1.6.1-bin-hadop2.6.tgz -C /export/servers/ cd /export/servers/ ln -s spark-1.6.1-bin-hadop2.6 spark

- 3、配置环境变量 vi /etc/profile 输⼊以下内容：

#set scala env

export SPARK_HOME=/export/servers/spark export PATH=${SPARK_HOME}/bin:$PATH 使配置⽣效：

source /etc/profile

- 4、修改配置⽂件 cd $SPARK_HOME/conf cp spark-env.sh.template spark-env.sh

vispark-env.sh 输⼊以下内容：export SPARK_MASTER_IP=127.0.0.1 export SPARK_LOCAL_IP=127.0.0.1

- 5、启动spark 在控制台输⼊命令： spark-shel


![image 4](<1、安装spark环境.note_images/imageFile4.png>)

声明：本系列博⽂是在学习耿嘉安《深⼊理解Spark 核⼼思想与源码分析》、⾼彦杰《Spark⼤数据处理》、张安战 《Spark技术内幕》及互联⽹公开博客资料后，摘抄或者拷⻉相关内容整理⽽成，个别知识点会有⾃⼰的理解并输 出。欢迎转载、使⽤、重新发布，但务必保留相关图书的信息，并且不得⽤于商业⽬的，基于本⽂修改后的作品务必 以相同的声明及许可发布。如有任何疑问，请与我联系。

## 技术讨论群： 138712835（需付费-会定期以发群红包的⽅式，将⼊群⾦额返回到群⾥⾯，本⼈不赚取 ⼀分钱）

