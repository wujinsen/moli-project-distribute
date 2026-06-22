参考，

# https://cwiki.apache.org/confluence/display/KAFKA/System+Tools https://cwiki.apache.org/confluence/display/KAFKA/Replication+tools http://kafka.apache.org/documentation.html#quickstart

为了便于使⽤，kafka提供了⽐较强⼤的Tools，把经常需要使⽤的整理⼀下

开关kafka Server

bin/kafka-server-start.sh config/server.properties bin/kafka-server-stop.sh

## 创建topic和显示topics

bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 -

-topic test

bin/kafka-topics.sh --list --zookeeper localhost:2181

describe topic的详细情况

bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic test

## Producer console

> bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test

This is a message

This is another message

后⾯可以任意的输⼊message，都会发到broker的topic中

## Comsumer console

bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning

从头读这个topic，可以重复读到所有数据

我在想为啥，每次都能replay，原来每次都是随机产⽣⼀个groupid

### consumerProps.put("group.id","console-consumer-" + new Random().nextInt(100000))

## Consumer Offset Checker

这个会显示出consumer group的offset情况， 必须参数为--group， 不指定--topic，默认为所有 topic

Displays the: Consumer Group, Topic, Partitions, Offset, logSize, Lag, Owner for the specified set of Topics and Consumer Group

bin/kafka-run- class .sh kafka.tools.ConsumerOffsetChecker

required argument: [group]

Option Description

------ -----------

--broker-info Print broker info

--group Consumer group.

--help Print this message.

--topic Comma-separated list of consumer

topics (all topics if absent).

--zkconnect ZooKeeper connect string. (default: )

# localhost:2181

## Export Zookeeper Offsets

将Zk中的offset信息以下⾯的形式打到file⾥⾯去

A utility that retrieves the offsets of broker partitions in ZK and prints to an output file in the following format:

- /consumers/group1/offsets/topic1/1-0:286894308

- /consumers/group1/offsets/topic1/2-0:284803985 bin/kafka-run- class .sh kafka.tools.ExportZkOffsets


required argument: [zkconnect]

Option Description

------ -----------

--group Consumer group.

--help Print this message.

--output-file Output file

--zkconnect ZooKeeper connect string. (default: )

# localhost:2181

## Update Offsets In Zookeeper

这个挺有⽤，⽤于replay

A utility that updates the offset of every broker partition to the offset of earliest or latest log segment file, in ZK.

bin/kafka-run- class .sh kafka.tools.UpdateOffsetsInZK

USAGE: kafka.tools.UpdateOffsetsInZK$ [earliest | latest] consumer.properties topic

更加直接的⽅式是，直接去Zookeeper⾥⾯看

通过zkCli.sh连上后，通过ls查看

Broker Node Registry

/brokers/ids/[0...N] --> host:port (ephemeral node)

Broker Topic Registry

/brokers/topics/[topic]/[0...N] --> nPartions (ephemeral node)

Consumer Id Registry

/consumers/[group_id]/ids/[consumer_id] --> {"topic1": #streams, ..., "topicN": #streams} (ephemeral node)

Consumer Offset Tracking

/consumers/[group_id]/offsets/[topic]/[broker_id-partition_id] --> offset_counter_value ((persistent node)

Partition Owner registry

/consumers/[group_id]/owners/[topic]/[broker_id-partition_id] --> consumer_node_id (ephemeral node)

