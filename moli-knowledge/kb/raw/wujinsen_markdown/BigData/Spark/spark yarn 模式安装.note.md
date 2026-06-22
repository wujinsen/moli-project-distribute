由于最近学习⼤数据开发，spark作为分布式内存计算框架，当前⼗分⽕热，因此作为⾸选学习技术之⼀。 Spark官⽅提供了三种集群部署⽅案： Standalone, Mesos, Yarn。其中 Standalone 为spark本⾝提供的集群模式， 搭建过程可以参考官⽹，本⽂介绍Spark on Yarn集群部署过程。使⽤3台普通机器搭建Spark集群， 软件环境： Ubuntu 16.04 LTS Ubuntu 16.04 LTS CentOS7 Scala-2.10.6 Hadop-2.7.2 spark-1.6.1-bin-hadop2.6 Java-1.8.0_7 硬件环境： ⼀个Master节点 Intel® Core™ i5-2310 CPU @ 2.90GHz × 4 4G内存 30G硬盘 两个Slave节点 Intel® Core™ i3-210 CPU @ 3.10GHz × 4 4G内存 50G硬盘

# ⼀、配置/etc/hosts及免密码登录

本⽂下载安装的软件都放在 home ⽬录下。

1. 主机hosts⽂件配置 在每台主机上修改host⽂件

![image 1](<spark yarn 模式安装.note_images/imageFile1.png>)

sudo vim /etc/hosts 218.19.92.27 fang-ubuntu1(Master) 218.19.92.26 fang-centos(Slave) 218.19.92.25 fang-Lenovo(Slave)

- 127.0.0.1 localhost


- 127.0.1.1 localhost 注：若此地未配置，或者未配置正确会导致集群启动不正常或者失败(

) 配置之后ping⼀下各机器名称检查是否⽣效，例如ssh fang@fang-centos。

- 2. 配置SSH 免密码登录 如果没有安装ssh，需要安装Openssh server，命令为sudo apt-get install openssh-server。


nodemanager did not stop gracefuly after 5 seconds

- 1) 在所有机器上都⽣成私钥和公钥 sh-keygen -t rsa #⼀路回车
- 2) 需要让机器间都能相互访问，就把每个机⼦上的id_rsa.pub发给master节点，传输公钥可以⽤scp来传输。 scp ~/.sh/id_rsa.pub fang@fang-ubuntu1:~/.sh/id_rsa.pub.slave1
- 3) 在master上，将所有公钥加到⽤于认证的公钥⽂件authorized_keys中 cat ~/.sh/id_rsa.pub* > ~/.sh/authorized_keys
- 4) 将公钥⽂件authorized_keys分发给每台slave scp ~/.sh/authorized_keys fang@fang-centos:~/.sh/
- 5) 在每台机⼦上验证SSH⽆密码登录


![image 2](<spark yarn 模式安装.note_images/imageFile2.png>)

在终端中输⼊登录命令，例如：ssh fang@fang-centos 如果直接登录成功⽽不需要登录密码，则表⽰设置正 确；如果登录不成功，即仍然需要登录密码，则可能需要修改⽂件authorized_keys的权限。

注：.sh ⽂件夹的权限必须为700，authorized_keys⽂件权限必须为600 使⽤如下命令改变⽂件夹权限：chmod 60 ~/.sh/authorized_keys

# ⼆、安装 Java

从官⽹下载最新版 Java，Spark官⽅说明 Java 只要是6以上的版本都可以，本⽂使⽤的是 jdk-8u91-linuxx64.tar.gz。 在下载⽬录下直接解压tar -zcvf jdk-8u91-linux-x64.tar.gz并复制⽂件到/usr/lib/jvm中，命令如下：

sudo cp -r jdk1.8.0_7 /usr/lib/jvm(如果没有jvm⽂件夹，则⼿动创建⼀个)，修改环境变量 sudo vim /etc/profile，添加下列内容： export JAVA_HOME=/usr/lib/jvm/jdk1.8.0_7 export JRE_HOME=$JAVA_HOME/jre

export PATH=$JAVA_HOME/bin:$JAVA_HOME/jre/bin:$PATH export CLASPATH=$CLASPATH:.:$JAVA_HOME/lib:$JAVA_HOME/jre/lib

然后使环境变量⽣效，并验证 Java 是否安装成功 $ source /etc/profile #⽣效环境变量

$ java -version#如果打印出如下版本信息，则说明安装成功

![image 3](<spark yarn 模式安装.note_images/imageFile3.png>)

# 三、安装 Scala

Spark官⽅要求 Scala 版本为 2.10.x，注意不要下错版本，我这⾥下了 2.10.6同样我们在~/中解压 tar -zcvf scala-2.10.6.tar.gz 再次修改环境变量sudo vim /etc/profile，添加以下内容： export SCALA_HOME=/home/fang/scala-2.10.6

export PATH=$PATH:$SCALA_HOME/bin 同样的⽅法使环境变量⽣效，并验证 scala 是否安装成功 $ source /etc/profile #⽣效环境变量

$ scala -version #如果打印出如下版本信息，则说明安装成功。

![image 4](<spark yarn 模式安装.note_images/imageFile4.png>)

# 四、安装配置 Hadop YARN

从官⽹下载 hadoop2.7.2版本,在⽤户根⽬录解压tar -zcvf hadop-2.7.2.tar.gz 再次修改环境变量sudo vim /etc/profile，添加以下内容： export HADOP_HOME=/home/fang/hadop-2.7.2 export HADOP_CONF_DIR=${HADOP_HOME}/etc/hadop export YARN_HOME=/home/fang/hadop-2.7.2 export YARN_CONF_DIR=${YARN_HOME}/etc/hadop 同样的⽅法使环境变量⽣效 $ source /etc/profile #⽣效环境变量 注：有时候修改了/etc/profile⽂件，执⾏命令source之后还是不能达到正常的效果，则需要重新机器，看问题 是否能解决。 配置 Hadoop，cd ~/hadop-2.7.2/etc/hadop进⼊hadoop配置⽬录，需要配置有以下7个⽂件：hadoopenv.sh，yarn-env.sh，slaves，core-site.xml，hdfs-site.xml，maprd-site.xml，yarn-site.xml。 在hadoop-env.sh中配置JAVA_HOME # The java implementation to use. export JAVA_HOME=/usr/lib/jvm/jdk1.8.0_7 在yarn-env.sh中配置JAVA_HOME # some Java parameters export JAVA_HOME=/usr/lib/jvm/jdk1.8.0_7 在slaves中配置slave节点的ip或者host， fang-centos fang-Lenovo

修改core-site.xml <configuration> <property> <name>fs.defaultFS</name> <value>hdfs:/fang-ubuntu1 9 0/</value>

</property> <property>

<name>hadop.tmp.dir</name> <value>file:/home/fang/hadop-2.7.2/tmp</value>

</property> </configuration> 修改hdfs-site.xml <configuration> <property> <name>dfs.namenode.secondary.htp-adres</name> <value>fang-ubuntu1 901</value>

</property> <property>

<name>dfs.namenode.name.dir</name> <value>file:/home/fang/hadop-2.7.2/dfs/name</value>

</property> <property>

<name>dfs.datanode.data.dir</name> <value>file:/home/fang/hadop-2.7.2/dfs/data</value>

</property> <property>

<name>dfs.replication</name> <value>3</value>

</property> </configuration> 修改mapred-site.xml <configuration> <property> <name>mapreduce.framework.name</name> <value>yarn</value>

</property>

</configuration> 修改yarn-site.xml <configuration> <property> <name>yarn.nodemanager.aux-services</name> <value>mapreduce_shufle</value>

</property> <property>

<name>yarn.nodemanager.aux-services.mapreduce.shufle.clas</name> <value>org.apache.hadop.mapred.ShufleHandler</value>

</property> <property>

<name>yarn.resourcemanager.adres</name> <value>fang-ubuntu1 8032</value>

</property> <property>

<name>yarn.resourcemanager.scheduler.adres</name> <value>fang-ubuntu1 8030</value>

</property> <property>

<name>yarn.resourcemanager.resource-tracker.adres</name> <value>fang-ubuntu1 8035</value>

</property> <property>

<name>yarn.resourcemanager.admin.adres</name> <value>fang-ubuntu1 803</value>

</property> <property>

<name>yarn.resourcemanager.webap.adres</name> <value>fang-ubuntu1 808</value>

</property> </configuration> 将配置好的hadoop-2.7.2⽂件夹分发给所有slaves节点 scp -r ~/hadop-2.6.0 fang@fang-centos:~/ 启动 Hadoop 在 master节点上执⾏以下操作，就可以启动 hadoop 了。 cd ~/hadop-2.7.2 #进⼊hadoop⽬录

bin/hadop namenode -format #格式化namenode 注：若格式化之后重新修改了配置⽂件，重新格式化之前需要删除tmp，dfs，logs⽂件夹。 sbin/start-dfs.sh #启动dfs sbin/start-yarn.sh #启动yarn 验证 Hadoop 是否安装成功，可以通过jps命令查看各个节点启动的进程是否正常。 在 master 上应该有以下⼏个进程：

![image 5](<spark yarn 模式安装.note_images/imageFile5.png>)

在每个slave上应该有以下⼏个进程：

![image 6](<spark yarn 模式安装.note_images/imageFile6.png>)

在浏览器中输⼊ http://fang-ubuntu1:8088 ，可以看到hadop 的管理界⾯。

![image 7](<spark yarn 模式安装.note_images/imageFile7.png>)

# 五、Spark安装

下载解压，进⼊ 下载最新版 Spark。我下载的是 spark-1.6.1-bin-hadop2.6.tar.gz。 在~/⽬录下解压，tar -zcvf spark-1.6.1-bin-hadop2.6.tar.gz 配置 Spark cd ~spark-1.6.1-bin-hadop2.6/conf #进⼊spark配置⽬录 cp spark-env.sh.template spark-env.sh #从配置模板复制 vim spark-env.sh #添加配置内容 在spark-env.sh末尾添加以下内容（这是我的配置，你可以⾃⾏修改）： export SPARK_HOME=/home/fang/spark-1.6.1-bin-hadop2.6 export SCALA_HOME=/home/fang/scala-2.10.6

官⽅下载地址

export JAVA_HOME=/usr/lib/jvm/jdk1.8.0_7 export HADOP_HOME=/home/fang/hadop-2.7.2 export PATH=$PATH:$JAVA_HOME/bin:$HADOP_HOME/bin

:$HADOP_HOME/sbin:$SCALA_HOME/bin export HADOP_CONF_DIR=$HADOP_HOME/etc/hadop export YARN_CONF_DIR=$YARN_HOME/etc/hadop export SPARK_MASTER_IP=218.19.92.27 SPARK_LOCAL_DIRS=/home/fang/spark-1.6.1-bin-hadop2.6 SPARK_DRIVER_MEMORY=1G export SPARK_LIBARY_PATH=.:$JAVA_HOME/lib:$JAVA_HOME/jre/lib

:$HADOP_HOME/lib/native 注：在设置Worker进程的CPU个数和内存⼤⼩，要注意机器的实际硬件条件，如果配置的超过当前Worker节 点的硬件条件，Worker进程会启动失败。 vim slaves在slaves⽂件下填上slave主机名：

- slave1
- slave2 将配置好的spark-1.6.1-bin-hadop2.6⽂件夹分发给所有slaves吧 scp -r ~/spark-1.6.1-bin-hadop2.6 fang@fang-cenos:~/ 启动Spark ,sbin/start-all.sh 验证 Spark 是否安装成功 主节点上启动了Master进程：


![image 8](<spark yarn 模式安装.note_images/imageFile8.png>)

在 slave 上启动了Worker进程：

![image 9](<spark yarn 模式安装.note_images/imageFile9.png>)

进⼊Spark的Web管理页⾯：http://fang-ubuntu1:8080

![image 10](<spark yarn 模式安装.note_images/imageFile10.png>)

# 六、运⾏示例

本例以集群模式运⾏SparkPi实例程序(deploy-mode 设置为cluster)

./bin/spark-submit -clas org.apache.spark.examples.SparkPi -master yarn-deploy-mode clusterdriver-memory 1G-executor-memory 1G-executor-cores 1 lib/spark-examples-1.6.1hadop2.6.0.jar 40

![image 11](<spark yarn 模式安装.note_images/imageFile11.png>)

任务提交时web界⾯

![image 12](<spark yarn 模式安装.note_images/imageFile12.png>)

作业运⾏完成web界⾯

![image 13](<spark yarn 模式安装.note_images/imageFile13.png>)

注意 Spark on YARN ⽀持两种运⾏模式，分别为yarn-cluster和yarn-client，yarn-cluster适⽤于⽣产环境；⽽ yarn-client适⽤于交互和调试，因为能在客户端终端看到程序输出。客户端模式实例和上⾯集群模式运⾏过程 类似，在此不在赘述。

SparkHigh Availability 配置: 修改spark-env.sh

//设置下⾯三项JVM参数 //spark.deploy.recoveryMode=ZOOKEEPER //spark.deploy.zookeeper.url=192.168.1.100:2181,192.168.1.101:2181 // /spark是默认的，可以不写 //spark.deploy.zookeeper.dir=/spark HA配置: 注释掉SPARK_MASTER_IP 添加zookeeper依赖：export SPARK_DAEMON_JAVA_OPTS="Dspark.deploy.recoveryMode=ZOOKEEPER Dspark.deploy.zookeeper.url=hadoop.Master:2181,hadoop.SlaveT1:2181,hadoop.SlaveT2:2181"

Sparn on yarn运⾏模式

- 1.
- 2.


spark-submit --class org.apache.spark.examples.SparkPi --deploy-mode client /usr/lib/spark/examples/lib/spark-examples_2.10-1.0.0-cdh5.1.0.jar terminal output：

