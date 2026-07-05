---
title: kafka.note（原文插图 annex）
slug: annex-kafka
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/kafka/kafka.note.md
related: [kafka-大数据管道]
created: 2026-07-05
updated: 2026-07-05
---

# Table of Contents

1 kafka

1.1 Kafka: a Distributed Mesaging System for Log Procesing

- 1.1.1 ABSTRACT

- 1.1.2 Introduction

- 1.1.3 Related Work

- 1.1.4 Kafka Architecture and Design Principles

- 1.1.5 Kafka Usage at LinkedIn

- 1.1.6 Experimental Results

- 1.1.7 Conclusion and Future Works


# 1 kafka

Apache Kafka htp:/incubator.apache.org/kafka/

Kafka: a Distributed Mesaging System for Log Procesing

htp:/research.microsoft.com/en-us/um/people/srikanth/netdb1/netdb1papers/netdb1-fin al12.pdf

1.1 Kafka: a Distributed Mesaging System for Log Procesing

- 1.1.1 ABSTRACT
- 1.1.2 Introduction

Many early systems for procesing this kind of data relied on physicaly scraping log files of production servers for analysis. In recent years, several specialized distributed log agregators have ben built, including Facebokʼs Scribe , Yahoʼs Data Highway , and Clouderaʼs Flume. Those systems are primarily designed for colecting and loading the log data into a data warehouse or Hadop for ofline consumption. （⼀些现有的⽇志系统都是⾮实时系统，将数据导 ⼊到数据仓库或是Hadop做离线处理）

At LinkedIn (a social network site), we found that in adition to traditional ofline analytics, we neded to suport most of the real-time aplications mentioned above with delays of no more than a few seconds.（kafka主要⽤来针对⽇志做实时分析和在线处理的）

On the one hand, Kafka is distributed and scalable, and ofers high throughput. （分布式， 可扩展，⾼吞吐的架构）

On the other hand, Kafka provides an API similar to a mesaging system and alows aplications to consume log events in real time.（提供类似消息系统的API）

- 1.1.3 Related Work


Traditional enterprise mesaging systems have existed for a long time and often play a critical role as an event bus for procesing asynchronous data flows. However, there are a few reasons why they tend not to be a god fit for log procesing. （传统的消息系统不适合做⽇志处理）

First, there is a mismatch in features ofered by enterprise systems. Those systems often focus on ofering a rich set of delivery guarantes. Such delivery guarantes are often overkil for colecting log data. For instance, losing a few pageview events ocasionaly is certainly not the end of the world. Those uneded features tend to increase the complexity of both the API and the underlying implementation of those systems.（企业系统提供了⼀些 ⽆⽤的特性，并且这些特性复杂了API以及底层实现）

Second, many systems do not focus as strongly on throughput as their primary design constraint.（不强调吞吐）

Third, those systems are weak in distributed suport. There is no easy way to partition and store mesages on multiple machines.（分布式⽀持不太好）

Finaly, many mesaging systems asume near i mediate consumption of mesages, so the queue of unconsumed mesages is always fairly smal. Their performance degrades significantly if mesages are alowed to acumulate, as is the case for ofline consumers such as data warehousing aplications that do periodic large loads rather than continuous consumption. （不能够⽀持存储时间过⻓的历史数据）

A number of specialized log agregators have ben built over the last few years.

Facebok uses a system caled Scribe. Each frontend machine can send log data to a set of Scribe machines over sockets. Each Scribe machine agregates the log entries and periodicaly dumps them to HDFS or an NFS device.

Yahoʼs data highway project has a similar dataflow. A set of machines agregate events from the clients and rol out “minute” files, which are then aded to HDFS.

Flume is a relatively new log agregator developed by Cloudera. It suports extensible “pipes” and “sinks”, and makes streaming log data very flexible. It also has more integrated distributed suport

However, most of those systems are built for consuming the log data ofline, and often expose implementation details unecesarily (e.g. “minute files”) to the consumer（只是⽐ 较适合离线处理）

Aditionaly, most of them use a “push” model in which the broker forwards data to consumers.（并且使⽤的都是push模型）

At LinkedIn, we find the “pul” model more suitable for our aplications since each consumer can retrieve the mesages at the maximum rate it can sustain and avoid being fl oded by mesages pushed faster than it can handle. The pul model also makes it easy to rewind a consumer（使⽤ pul模型能够由client来控制速率，并且能够很容易地由client做rewind）

## 1.1.4 Kafka Architecture and Design Principles

We first introduce the basic concepts in Kafka

A stream of mesages of a particular type is defined by a topic.

A producer can publish mesages to a topic.

The published mesages are then stored at a set of servers caled brokers.

A consumer can subscribe to one or more topics from the brokers, and consume the subscribed mesages by puling data from the brokers.

Each mesage stream provides an iterator interface over the continual stream of mesages being produced. The consumer then iterates over every mesage in the stream and proceses the payload of the mesage.

Unlike traditional iterators, the mesage stream iterator never terminates. If there are curently no more mesages to consume, the iterator blocks until new mesages are published to the topic.（以iterators⽅式提供消息）

We suport both the point-topoint delivery model in which multiple consumers jointly consume a single copy of al mesages in a topic, as wel as the publish/subscribe model in which multiple consumers each retrieve its own copy of a topic.（多个consumer可以 共同消费⼀份，或者是各⾃消费⼀份）

Since Kafka is distributed in nature, an Kafka cluster typicaly consists of multiple brokers. To balance load, a topic is divided into multiple partitions and each broker stores one or more of those partitions. Multiple producers and consumers can publish and retrieve mesages at the same time.（⼀个topic被分割成多个partition, 每个broker上⾯可以host多个partition)

![image 1](assets/imageFile1.png)

Producer Code

- 1 producer = new Producer(...);

- 2 message = new Message(“test message str”.getBytes());

- 3 set = new MessageSet(message);

- 4 producer.send(“topic1”, set);

- 5


Consumer Code

- 1 streams[] = Consumer.createMessageStreams(“topic1”, 1);

- 2 for (message : streams[0]) {

- 3 bytes = message.payload();

- 4 // do something with the bytes

- 5 }

- 6


Eficiency on a Single Partition

Simple storage

Each partition of a topic coresponds to a logical log. Physicaly, a log is implemented as a set of segment files of aproximately the same size (e.g., 1GB).(每个partition对应⼀个 logical log, 每个logical log对应多个segment file，这些⽂件都近似⼤⼩） Every time a producer publishes a mesage to a partition, the broker simply apends the mesage to the last segment file.（每个追加到最后⼀个segment file上）

For beter performance, we flush the segment files to disk only after a configurable number of mesages have ben published or a certain amount of time has elapsed. A mesage is only exposed to the consumers after it is flushed.（积累到⼀定的数据量才会 进⾏刷新）

Unlike typical mesaging systems, a mesage stored in Kafka doesnʼt have an explicit mesage id. Instead, each mesage is adresed by its logical ofset in the log. This avoids the overhead of maintaining auxiliary, sek-intensive random-aces index structures that map the mesage ids to the actual mesage locations. （没有mesageid, 但是可以通过logical ofset来定位log. 这样可以免去从mesage id对应到mesage这个过 程，因为这个过程需要maping需要random aces index. 这样的mesage id⾃然不是连 续的，但是却是递增的）

A consumer always consumes mesages from a particular partition sequentialy. If the consumer acknowledges a particular mesage ofset, it implies that the consumer has received al mesages prior to that ofset in the partition.（consumer只能通过偏移顺序 读取内容） Under the covers, the consumer is isuing asynchronous pul requests to the broker to have a bufer of data ready for the aplication to consume. Each pul request contains the ofset of the mesage from which the consumption begins and an aceptable number of bytes to fetch.

Each broker keps in memory a sorted list of ofsets, including the ofset of the first mesage in every segment file. The broker locates the segment file where the requested mesage resides by searching the ofset list, and sends the data back to the consumer. After a consumer receives a mesage, it computes the ofset of the next mesage to consume and uses it in the next pul request.（每个broker在内存保存各个⽂件的起始的 ofset，这样就可以很容易地进⾏定位）

Eficient transfer

Although the end consumer API iterates one mesage at a time, under the covers, each pul request from a consumer also retrieves multiple mesages up to a certain size, typicaly hundreds of kilobytes.(批量传输）

Another unconventional choice that we made is to avoid explicitly caching mesages in memory at the Kafka layer. Instead, we rely on the underlying file system page cache. （在kafka层⾯不进⾏cache，⽽由system完成page cache简化⼯作）This has the main benefit of avoiding double bufering—mesages are only cached in the page cache. This has the aditional benefit of retaining warm cache even when a broker proces is restarted. Since Kafka doesnʼt cache mesages in proces at al, it has very litle overhead in garbage colecting its memory, making eficient implementation in a VMbased language feasible.（这样也避免了GC带来的额外开销）

Finaly, since both the producer and the consumer aces the segment files sequentialy, with the consumer often laging the producer by a smal amount, normal operating system caching heuristics are very efective (specificaly write-through caching and read- ahead).

On Linux and other Unix operating systems, there exists a sendfile API that can directly transfer bytes from a file chanel to a socket chanel.（通过sendfile这个API减少系统调 ⽤次数） Stateles broker

However, this makes it tricky to delete a mesage, since a broker doesnʼt know whether al subscribers have consumed the mesage. Kafka solves this problem by using a simple time-based SLA for the retention policy. A mesage is automaticaly deleted if it has ben retained in the broker longer than a certain period, typicaly 7 days. This solution works wel in practice.（可以通过保存最近7天的⽇志来显示删除）

There is an important side benefit of this design. A consumer can deliberately rewind back to an old ofset and re-consume data. This violates the comon contract of a queue, but proves to be an esential feature for many consumers. （consumer可以指定 某个ofset然后从这个点开始重新消费数据）

Distributed Cordination

Each producer can publish a mesage to either a randomly selected partition or a partition semanticaly determined by a partitioning key and a partitioning function.（procuder可以根 据指定partition算法或者是随机选择发送到哪个partition. 就现在来说每个partition只能够在某 ⼀个broker上⾯）

Kafka has the concept of consumer groups. Each consumer group consists of one or more consumers that jointly consume a set of subscribed topics, i.e., each mesage is delivered to only one of the consumers within the group. Diferent consumer groups each independently consume the ful set of subscribed mesages and no cordination is neded acros consumer groups.（consumer group可能由多个consumer组成，每个consumer group只能够消费⼀个或者是多个topic, ⽽这个topic⾥⾯所有的内容会被⾥⾯的consumers处 理，每个consumer处理部分。不同的group之间没有关系）

Our first decision is to make a partition within a topic the smalest unit of paralelism. This means that at any given time, al mesages from one partition are consumed only by a single consumer within each consumer group. Had we alowed multiple consumers to simultaneously consume a single partition, they would have to cordinate who consumes what mesages, which necesitates locking and state maintenance overhead. （每个partition只能够被某⼀个consumer所消费，不然没有办法决定哪个consumer消费某 个partition⾥⾯的具体信息）In contrast, in our design consuming proceses only ned co-ordinate when the consumers rebalance the load, an infrequent event. In order for the load to be truly balanced, we require many more partitions in a topic than the consumers in each group. We can easily achieve this by over partitioning a topic.（通常 来说partition的数量要⼤于consumer数量这样consumer才不会空闲）

The second decision that we made is to not have a central “master” node, but instead let consumers cordinate among themselves in a decentralized fashion. Ading a master can complicate the system since we have to further wory about master failures. （没有 使⽤master节点来进⾏cordinate，不然需要考虑matser挂掉的情况） To facilitate the cordination, we employ a highly available consensus service Zokeper

Kafka uses Zokeper for the folowing tasks:

detecting the adition and the removal of brokers and consumers

when each broker or consumer starts up, it stores its information in a broker or consumer registry in Zokeper.（启动时候在上⾯进⾏注册）

The broker registry contains the brokerʼs host name and port, and the set of topics and partitions stored on it.（broker注册hostname和port,管理的topics以及 partitions)

The consumer registry includes the consumer group to which a consumer belongs and the set of topics that it subscribes to.(consumer注册consumer group，以及订阅的topics)

Each consumer group is asociated with an ownership registry and an ofset registry in Zokeper.

The ownership registry has one path for every subscribed partition and the path value is the id of the consumer curently consuming from this partition （每个订阅partition是⼀个path, path value是这个consumer id, 这个consumer 来消费这个partition的）

The ofset registry stores for each subscribed partition, the ofset of the last consumed mesage in the partition.（记录订阅partition的最后⼀个ofset）

tri gering a rebalance proces in each consumer when the above events hapen,

maintaining the consumption relationship and keping track of the consumed ofset of each partition.

During the initial startup of a consumer or when the consumer is notified about a broker/consumer change through the watcher, the consumer initiates a rebalance proces to determine the new subset of partitions that it should consume from.（consumer或者是 broker发⽣变化的话，那么就会触发balance)

When there are multiple consumers within a group, each of them wil be notified of a broker or a consumer change. However, the notification may come at slightly diferent times at the consumers. So, it is posible that one consumer tries to take ownership of a partition stil owned by another consumer. When this hapens, the first consumer simply releases al the partitions that it curently owns, waits a bit and retries the rebalance proces. In practice, the rebalance proces often stabilizes after only a few retries.（可 能会出现⼀些颠簸的情况，但是这个情况最终是会稳定下来的）

When a new consumer group is created, no ofsets are available in the ofset registry. In this case, the consumers wil begin with either the smalest or the largest ofset (depending on a configuration) available on each subscribed partition, using an API that we provide on the brokers.（新增的consume group可以选择最⽼的点开始读取，也可以 选择最新的点开始读取）

Delivery Guarantes

In general, Kafka only guarantes at-least-once delivery. Exactly- once delivery typicaly requires two-phase comits and is not necesary for our aplications.（⾄少保证⼀次投递） Most of the time, a mesage is delivered exactly once to each consumer group. However, in the case when a consumer proces crashes without a clean shutdown, the consumer proces that takes over those partitions owned by the failed consumer may get some duplicate mesages that are after the last ofset sucesfuly comited to zokeper. （consumer crash然后切换到其他consumer处理的时候，可能会处理相同的数据）

Kafka guarantes that mesages from a single partition are delivered to a consumer in order. However, there is no guarante on the ordering of mesages coming from diferent partitions.（单个partition⾥⾯的数据是确保有序的，⽽partition之间的数据顺序没有保证）

To avoid log coruption, Kafka stores a CRC for each mesage in the log. If there is any I/O eror on the broker, Kafka runs a recovery proces to remove those mesages with inconsistent CRCs. Having the CRC at the mesage level also alows us to check network erors after a mesage is produced or consumed.（使⽤CRC做读取和传输校验）

If a broker goes down, any mesage stored on it not yet consumed becomes unavailable. If the storage system on a broker is permanently damaged, any unconsumed mesage is lost forever. In the future, we plan to ad built-in replication in Kafka to redundantly store each mesage on multiple brokers.（现在broker没有做replication, 也就是说如果down的话那么上 ⾯数据读取不到，如果磁盘坏的话那么数据就发⽣丢失）

## 1.1.5 Kafka Usage at LinkedIn

![image 2](assets/imageFile2.png)

We rely on a hardware load-balancer to distribute the publish requests to the set of Kafka brokers evenly. （硬件负载均衡）

We also deploy a cluster of Kafka in a separate datacenter for ofline analysis, located geographicaly close to our Hadop cluster and other data warehouse infrastructure. Without to much tuning, the end-to-end latency for the complete pipeline is about 10 seconds on average, god enough for our requirements.（跨机房数据延迟在10s以内）

Our tracking also includes an auditing system to verify that there is no data los along the whole pipeline.（检验数据是否丢失）

To facilitate that, each mesage caries the timestamp and the server name when they are generated. We instrument each producer such that it periodicaly generates a monitoring event, which records the number of mesages published by that producer for each topic within a fixed time window.

The producer publishes the monitoring events to Kafka in a separate topic. The consumers can then count the number of mesages that they have received from a given topic and validate those counts with the monitoring events to validate the corectnes of data.

Loading into the Hadop cluster is acomplished by implementing a special Kafka input format that alows MapReduce jobs to directly read data from Kafka.

## 1.1.6 Experimental Results

## 1.1.7 Conclusion and Future Works

There are a number of directions that weʼd like to pursue in the future.

First, we plan to ad built-in replication of mesages acros multiple brokers to alow durability and data availability guarantes even in the case of unrecoverable machine failures.（broker replicaiton需要线上，这样可以确保durability以及availability) Weʼd like to suport both asynchronous and synchronous replication models to alow some tradeof betwen producer latency and the strength of the guarantes provided. An aplication can chose the right level of redundancy based on its requirement on durability, availability and throughput. (在replication上 ⾯可以选择同步还是异步⽅式）

Second, we want to ad some stream procesing capability in Kafka.（提供⼀些流式处理⽅⾯的 能⼒）

After retrieving mesages from Kafka, real time aplications often perform similar operations such as window-based counting and joining each mesage with records in a secondary store or with mesages in another stream.

At the lowest level this is suported by semanticaly partitioning mesages on the join key during publishing so that al mesages sent with a particular key go to the same partition and hence arive at a single consumer proces. This provides the foundation for procesing distributed streams acros a cluster of consumer machines.

On top of this we fel a library of helpful stream utilities, such as diferent windowing functions or join techniques wil be beneficial to this kind of aplications.

Date: 2014-07-01T07 45+080 version 7.9.3f with version 24

Org Emacs
