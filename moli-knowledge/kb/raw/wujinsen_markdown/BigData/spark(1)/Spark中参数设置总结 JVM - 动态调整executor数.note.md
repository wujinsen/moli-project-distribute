Spark中各个⻆⾊的JVM参数设置：

- (1)Driver的JVM参数：

-Xmx，-Xms，如果是yarn-client模式，则默认读取spark-env⽂件中的 SPARK_DRIVER_MEMORY值，-Xmx，-Xms值⼀样⼤⼩；如果是yarn-cluster模式，则读取 的是spark-default.conf⽂件中的spark.driver.extraJavaOptions对应的JVM参数值。

PermSize，如果是yarn-client模式，则是默认读取spark-class⽂件中的JAVA_OPTS="XX:MaxPermSize=256m $OUR_JAVA_OPTS"值；如果是yarn-cluster模式，读取的是 spark-default.conf⽂件中的spark.driver.extraJavaOptions对应的JVM参数值。

GC⽅式，如果是yarn-client模式，默认读取的是spark-class⽂件中的JAVA_OPTS；如果是 yarn-cluster模式，则读取的是spark-default.conf⽂件中的spark.driver.extraJavaOptions 对应的参数值。

以上值最后均可被spark-submit⼯具中的--driver-java-options参数覆盖。

- (2)Executor的JVM参数：

-Xmx，-Xms，如果是yarn-client模式，则默认读取spark-env⽂件中的 SPARK_EXECUTOR_MEMORY值，-Xmx，-Xms值⼀样⼤⼩；如果是yarn-cluster模式，则读 取的是spark-default.conf⽂件中的spark.executor.extraJavaOptions对应的JVM参数值。

PermSize，两种模式都是读取的是spark-default.conf⽂件中的 spark.executor.extraJavaOptions对应的JVM参数值。

GC⽅式，两种模式都是读取的是spark-default.conf⽂件中的 spark.executor.extraJavaOptions对应的JVM参数值。

- (3)Executor数⽬及所占CPU个数


如果是yarn-client模式，Executor数⽬由spark-env中的SPARK_EXECUTOR_INSTANCES指 定，每个实例的core数⽬由SPARK_EXECUTOR_CORES指定；如果是yarn-cluster模式， Executor的数⽬由spark-submit⼯具的--num-executors参数指定，默认是2个实例，⽽每个 Executor使⽤的core数⽬由--executor-cores指定，默认为1核。

每个Executor运⾏时的信息可以通过yarn logs命令查看到，类似于如下：

- -master yarn \

- -deploy-mode client \

14/08/1318:12:59 INFO org.apache.spark.Logging$class.logInfo(Logging.scala:58): Setting up executor with commands: List($JAVA_HOME/bin/java, -server, -XX:OnOutOfMemoryError='kill %p', Xms1024m -Xmx1024m , -XX:PermSize=256M -XX:MaxPermSize=256M -verbose:gc -XX:+PrintGCDetails XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -Xloggc:/tmp/spark_gc.log, -Djava.io.tmpdir=$PWD/tmp,

- -Dlog4j.configuration=log4j-spark-container.properties, org.apache.spark.executor.CoarseGrainedExecutorBackend, akka.tcp://spark@sparktest1:41606/user/CoarseGrainedScheduler, 1, sparktest2, 3, 1>, <LOG_DIR>/stdout, 2>, <LOG_DIR>/stderr)


其中，akka.tcp://spark@sparktest1:41606/user/CoarseGrainedScheduler表示当前 的Executor进程所在节点，后⾯的1表示Executor编号，sparktest2表示ApplicationMaster的 host，接着的3表示当前Executor所占⽤的CPU数⽬。

Spark On YARN模式的Spark Application根据Task⾃动调整Executor数，要启⽤该功能，需做以下操作：

- 1/在所有的NodeManager中，修改yarn-site.xml，为yarn.nodemanager.aux-services添加spark_shuffle值，设 置yarn.nodemanager.aux-services.spark_shuffle.class值为 org.apache.spark.network.yarn.YarnShuffleService，如下：

- 2/将 $SPARK_HOME/lib/spark-1.2.0-yarn-shuffle.jar⽂件拷⻉到hadoop-yarn/lib⽬录下（即yarn的库⽬录）

- 3/配置 $SPARK_HOME/conf/spark-default.xml，添加以下两项


<property> <name>yarn.nodemanager.aux-services</name> <value>mapreduce_shufle,spark_shufle<alue> </property>

<property> <name>yarn.nodemanager.aux-services.spark_shufle.clas</name> <value>org.apache.spark.network.yarn.YarnShufleService</value> </property>

spark.dynamicAllocation.minExecutors 1#最⼩Executor数 spark.dynamicAllocation.maxExecutors 100#最⼤Executor数

4/执⾏时开启⾃动调整Executor数开关，以spark-sql yarn client模式为例： spark-sql \

- -conf spark.shufle.service.enabled=true \

- -conf spark.dynamicAlocation.enabled=true \

- -e "SELECT COUNT(*) FROMx" 对于使⽤spark-submit也是⼀样：


spark-submit \

- -clasx.y.z \

- -master yarn-client \

- -conf spark.shufle.service.enabled=true \

- -conf spark.dynamicAlocation.enabled=true \


/data/jars/xyz.jar \

