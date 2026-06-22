提交spark应⽤使⽤spark_home/bin⽬录中的⼀个脚本 “spark-submit”，它统⼀了各种集群管理模式 下aplication的提交形式，如下：

./bin/spark-submit \

--class <main-class>

--master <master-url> \

--deploy-mode <deploy-mode> \

--conf <key>=<value> \

... # other options <application-jar> \ [application-arguments]

对各种常⻅集群管理模式下提交⽤户aplication的具体示例如下：

# Run application locally on 8 cores

./bin/spark-submit \

--class org.apache.spark.examples.SparkPi \

--master local[8] \ /path/to/examples.jar \ 100

# Run on a Spark Standalone cluster in client deploy mode

./bin/spark-submit \

--class org.apache.spark.examples.SparkPi \

--master spark://207.184.161.138:7077 \

--executor-memory 20G \

--total-executor-cores 100 \ /path/to/examples.jar \ 1000

# Run on a Spark Standalone cluster in cluster deploy mode with supervise

./bin/spark-submit \

--class org.apache.spark.examples.SparkPi \

--master spark://207.184.161.138:7077 \

--deploy-mode cluster

--supervise

--executor-memory 20G \

--total-executor-cores 100 \ /path/to/examples.jar \ 1000

# Run on a YARN cluster export HADOOP_CONF_DIR=XXX

./bin/spark-submit \

--class org.apache.spark.examples.SparkPi \

--master yarn-cluster \ # can also be `yarn-client` for client mode

--executor-memory 20G \

--num-executors 50 \ /path/to/examples.jar \ 1000

# Run a Python application on a Spark Standalone cluster

./bin/spark-submit \

--master spark://207.184.161.138:7077 \ examples/src/main/python/pi.py \ 1000

