下载kafka

tar -xzf kafka_2.11-0.9.0.0.tgz

cd kafka_2.11-0.9.0.0

kafka⾃带⾃启动zokeper命令:

bin/zookeeper-server-start.sh config/zookeeper.properties

启动kafka:

bin/kafka-server-start.sh config/server.properties

说明：如果不⽤kafka⾃带的zookeeper，需要修改server.properties添加 zookeeper配置

创建topic:

bin/kafka-topics.sh --create --zookeeper localhost:2181 -replication-factor 1 --partitions 1 --topic test

查看当前服务器所有topic:

bin/kafka-topics.sh --list --zookeeper localhost:2181

启动⽣产者:

bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test2

This is a message

This is another message

启动消费者:

bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test2 --from-beginning

说明: -from-begi ning表示从头开始读 ⾼版本kafka启动命令:

bin/kafka-console-consumer.sh -botstrap-server localhost:9092-topic test2-frombegi ning

查看某个Topic的详情 bin/kafka-topics.sh-topic test2-describe-zokeper zokeper01 2181

查看消费者位置： bin/kafka-run-clas.sh kafka.tols.ConsumerOfsetChecker-zokeper zokeper01 2181

-group test2

bin/kafka-consumer-offset-checker.sh

bin/kafka-consumer-ofset-checker.sh-zokeper zokeper01 2181-group test2

conf/server.properties

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agrements. Se the NOTICE file distributed with this work for aditional information regarding copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

htp:/ w.apache.org/licenses/LICENSE-2.0

Unles required by aplicable law or agred to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARANTIES OR CONDITIONS OF ANY KIND, either expres or implied. Se the License for the specific language governing permisions and limitations under the Licese.

# se kafka.server.KafkaConfig for aditional details and defaults # Server Basics #

# The id of the broker. This must be set to a unique integer for each broker. broker.id=0

# Socket Server Setings #

# listeners=PLAINTEXT:/:9092 # The port the socket server listens on port=9092

Hostname the broker wil bind to. If not set, the server wil bind to al interfaces #host.name=localhost

Hostname the broker wil advertise to producers and consumers. If not set, it uses the value for "host.name" if configured. Otherwise, it wil use the value returned from java.net.InetAdres.getCanonicalHostName().

#advertised.host.name=<hostname routable by clients>

The port to publish to ZoKeper for clients to use. If this is not set, it wil publish the same port that the broker binds to.

#advertised.port=<port acesible by clients> # The number of threads handling network requests num.network.threads=3 # The number of threads doing disk I/O num.io.threads=8 # The send bufer (SO_SNDBUF) used by the socket server socket.send.bufer.bytes=10240 # The receive bufer (SO_RCVBUF) used by the socket server socket.receive.bufer.bytes=10240 # The maximum size of a request that the socket server wil acept (protection against OM) socket.request.max.bytes=10485760

# Log Basics #

# A coma seperated list of directories under which to store log files log.dirs=/usr/local/kafka/logs

The default number of log partitions per topic. More partitions alow greater paralelism for consumption, but this wil also result in more files acros

# the brokers. num.partitions=2

# The number of threads per data directory to be used for log recovery at startup and flushing at shutdown. # This value is recomended to be increased for instalations with data dirs located in RAID aray. num.recovery.threads.per.data.dir=1

# Log Flush Policy #

Mesages are i mediately writen to the filesystem but by default we only fsync() to sync the OS cache lazily. The folowing configurations control the flush of data to disk. There are a few important trade-ofs here:

1. Durability: Unflushed data may be lost if you are not using replication.

- # 2. Latency: Very large flush intervals may lead to latency spikes when the flush does ocur as there wil be a lot of data to flush.
- # 3. Throughput: The flush is generaly the most expensive operation, and a smal flush interval may lead to excesive seks.


The setings below alow one to configure the flush policy to flush data after a period of time or # every N mesages (or both). This can be done globaly and overi den on a per-topic basis.

The number of mesages to acept before forcing a flush of data to disk #log.flush.interval.mesages=1 0

The maximum amount of time a mesage can sit in a log before we force a flush #log.flush.interval.ms=1 0

# Log Retention Policy # #

The folowing configurations control the disposal of log segments. The policy can be set to delete segments after a period of time, or after a given size has acumulated. A segment wil be deleted whenever *either* of these criteria are met. Deletion always hapens

# from the end of the log. # The minimum age of a log file to be eligible for deletion log.retention.hours=168 # A sizebased retention policy for logs. Segments are pruned from the log as long as the remaining

segments don't drop below log.retention.bytes. #log.retention.bytes=1073741824 # The maximum size of a log segment file. When this size is reached a new log segment wil be cr eated. log.segment.bytes=1073741824

The interval at which log segments are checked to se if they can be deleted acording # to the retention policies log.retention.check.interval.ms=3 0

# Zokeper #

Zokeper conection string (se zokeper docs for details). This is a coma separated host:port pairs, each coresponding to a zk server. e.g. "127.0.0.1 3 0,127.0.0.1 301,127.0.0.1 302". You can also apend an optional chrot string to the urls to specify the

# rot directory for al kafka znodes. zokeper.conect=zokeper01 2181,zokeper02 2181,zokeper03 2181

# Timeout in ms for conecting to zokeper zokeper.conection.timeout.ms=6 0

