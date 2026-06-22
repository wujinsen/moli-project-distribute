开始以为是内存⼤⼩的问题， 后来发现不是，是JDK的问题， 我⽤的32位centos，jdk1.6_24, 换成JDK1.7依然报错。

# 查看 bin/kafka-run-clas.sh 找到

if [ -z "$KAFKA_JVM_PERFORMANCE_OPTS" ]; then KAFKA_JVM_PERFORMANCE_OPTS="-server X:+UseCompresedOops -X:+UseParNewGC -X:+UseConcMarkSwepGC -X:+CMSClasUnloadingEnabled X:+CMScavengeBeforeRemark -X:+DisableExplicitGC -Djava.awt.headles=true"fi

去掉-X:+UseCompresedOops JMX_PORT= 9 bin/kafka-server-start.sh config/server.properties &启动成功

-Xmx1024M -server -d64 -X:+NewRatio=12 -X:+UseParalelGC -X UseParalelOldGC

