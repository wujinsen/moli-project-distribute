基于0.8.0版本。

#查看topic分布情况kafka-list-topic.sh

bin/kafka-list-topic.sh-zokeper 192.168.197.170 2181,192.168.197.171 2181 （列出所有topic的分区情况） bin/kafka-list-topic.sh-zokeper 192.168.197.170 2181,192.168.197.171 2181-topic test （查看 test的分区情况）

其实kafka-list-topic.sh⾥⾯就⼀句

exec $(dirname $0)/kafka-run-clas.sh kafka.admin.ListTopicComand $@实际是通过 kafka-run-clas.sh脚本执⾏的包kafka.admin下⾯的类

#创建TOPIC kafka-create-topic.sh

bin/kafka-create-topic.sh -replica 2-partition 8-topic note-topiczokeper 192.168.10.20 2181,192.168.10.201 2181 bin/kafka-create-topic.sh -replica 2-partition 8-topicnotebok-topiczokeper 192.168.10.20 2181,192.168.10.201 2181 创建名为test的topic， 8个分区分别存放数据，数据备份总共2份

bin/kafka-create-topic.sh -replica 1-partition 1-topic test2 zokeper 192.168.197.170 2181,192.168.197.171 2181 结果 topic: test2 partition: 0 leader: 170 replicas: 170 isr: 170

#重新分配分区kafka-reasign-partitions.sh

这个命令可以分区指定到想要的 -broker-list上 bin/kafka-reasign-partitions.sh-topics-to-move-json-file topics-to-move.json-broker-list "171"

-zokeper 192.168.197.170 2181,192.168.197.171 2181-execute cat topic-to-move.json {"topics":

[{"topic": "test2"}], "version":1

}

#为Topic增加 partition数⽬kafka-ad-partitions.sh

bin/kafka-ad-partitions.sh-topic test -partition 2 zokeper192.168.197.170 2181,192.168.197.171 2181 （为topic test增加2个分区）

#控制台接收消息

bin/kafka-console-consumer.sh-zokeper192.168.197.170 2181,192.168.197.171 2181 -from-begi ningtopic test

#控制台发送消息

bin/kafka-console-producer.sh-broker -list192.168.10.20 9092,192.168.10.201: 9092 -topic notetopic

#⼿动均衡topic, kafka-prefered-replica-election.sh

bin/kafka-prefered-replica-election.shzokeper 192.168.197.170 2181,192.168.197.171 2181 -path-to-json-file preferedclick.json cat prefered-click.json{ "partitions": [ {"topic": "click", "partition": 0}, {"topic": "click", "partition": 1}, {"topic": "click", "partition": 2}, {"topic": "click", "partition": 3}, {"topic": "click", "partition": 4}, {"topic": "click", "partition": 5}, {"topic": "click", "partition": 6}, {"topic": "click", "partition": 7},

{"topic": "play", "partition": 0}, {"topic": "play", "partition": 1}, {"topic": "play", "partition": 2}, {"topic": "play", "partition": 3}, {"topic": "play", "partition": 4}, {"topic": "play", "partition": 5},

{"topic": "play", "partition": 6}, {"topic": "play", "partition": 7} ]}

#删除topic,慎⽤，只会删除zokeper中的元数据，消息⽂件须⼿动删除

bin/kafka-run-clas.sh kafka.admin.DeleteTopicComand-topic test 6-zokeper 192.168.197.170 2181 ,192.168.197.171 2181

