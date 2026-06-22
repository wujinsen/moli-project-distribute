安装

# 1. 依赖软件

- 1.1.安装jdk

- 1.2.安装 sh

- 1.3.安装zokeper

- 1.4.安装hadop2.6


略

略

略

略

# 2. 安装spark

- 2.1.上传

- 2.2.解压

- 2.3.重命名

- 2.4.修改环境变量


略

略

略

<table>
  <tr>
    <th>eporSPARK_HOME=/home/hadop/spark</th>
  </tr>
</table>


export PATH=$PATH:$SPARK_HOME/bin

- 2.5.修改配置⽂件

spark-env.sh

<table>
  <tr>
    <th>#指定JAVA_HOME位置 export JAVA_HOME=/usr/jdk #指定spark⽼⼤Master的IP export SPARK_MASTER_IP=master1 #指定spark的Master的端⼜<br><br>ex指定可⽤的port SPARCK_PMU内核数量ASTER_P(O默认RT=:所有可⽤707 ) e作业可使⽤的内存容量，默认格式为xport SPARK_WORKER_CORES=11 0m或者2g(默认:所有RAM去掉给操作系统⽤的1GB) e机器上运⾏xport SPAwRKo_rkWeOr数量RKER (默认_ME:M1)O。当你有⼀个⾮常强⼤的计算机的时可启动多个RY=50m worker进程。 e设置xpohratdo SPApR集群的配置⽂件所在⽬录K_WORKER_INSTANCES=1 exportHADOP_CONF_DIR=/home/hadop/hadop/etc/hadop<br><br>#(可选)配置两个Spark Master实现⾼可靠(⾸先要配置zokeper集群，在spark-env.sh添加 SPARK_DAEMON_JAVA_OPTS) #exportSPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=ZOKEPER Dspark.deploy.zokeper.url=master1ha:2181-Dspark.deploy.zokeper.dir=/spark"<br><br>S RK_ORKER_CORES=2 RK_ORKER_MEMORY=2g</th>
  </tr>
</table>


#export SPARK_WORKER_INSTANCES=1

slaves

<table>
  <tr>
    <th>1<br>2<br></th>
  </tr>
</table>


slave3

- 2.6.下发

- 2.7.启动

- 2.8.访问


发送到从节点

sbin/start-al.sh sbin/start-master.sh sbin/start-slaves.sh

htp:/master1 8080

