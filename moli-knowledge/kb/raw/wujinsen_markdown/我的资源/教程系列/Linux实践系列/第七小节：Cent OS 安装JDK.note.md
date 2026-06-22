- 1、查看当前系统上是否安装了JDK 进⼊⼀个新的系统，⼀般来讲是没有安装过JDK的。但是有些linux的版本会默认安装openJDK。

open JDK实在oracle公司开源的代码上进⾏同步开发并发布的版本。 如果要查看当前系统是否已经安装了JDK，可以使⽤命令：sudo update-alternatives-config java

- 2、下载JDK 由于当前系统中并没有我们需要的oracle JDK，我们需要⾃⼰安装⼀个。 先在oracle的⽹站上寻找JDK的下载地址，然后使⽤wget命令进⾏下载。或者，你可以将数据下载

到电脑上，然后上传到linux上。

wget

- 3、解压安装包 mv jdk-8u60-linux* jdk-8u60-linux-x64.tar.gz tar -zxvf jdk-8u60-linux-x64.tar.gz -C./instal/
- 4、配置JDK部署信息到环境变量 #set java env export JAVA_HOME=/export/servers/jdk export JRE_HOME=${JAVA_HOME}/jre export CLASPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib


![image 1](<第七小节：Cent OS 安装JDK.note_images/imageFile1.png>)

htp:/download.oracle.com/otn-pub/java/jdk/8u60-b27/jdk-8u60-linux-x64.tar.gz?AuthParam

=14170527_0f80e68acf71ce8da8af237439f406a

![image 2](<第七小节：Cent OS 安装JDK.note_images/imageFile2.png>)

export PATH=${JAVA_HOME}/bin:$PATH

- 5、让配置信息⽣效 source /etc/profile
- 6、将oracle JDK 配置成默认的JDK. update-alternatives-instal /usr/bin/java java /export/servers/jdk/bin/java 30 update-alternatives-instal /usr/bin/javac javac /export/servers/jdk/bin/javac 30
- 7、产看当前机器的JDK版本


![image 3](<第七小节：Cent OS 安装JDK.note_images/imageFile3.png>)

