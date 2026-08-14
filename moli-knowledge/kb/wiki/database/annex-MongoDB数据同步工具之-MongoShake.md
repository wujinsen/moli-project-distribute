---
title: MongoDB数据同步工具之 MongoShake.note（原文插图 annex）
slug: annex-MongoDB数据同步工具之-MongoShake
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/MongoDB/MongoDB数据同步工具之 MongoShake.note.md
related: [mongodb与文档库选型]
created: 2026-07-05
updated: 2026-07-05
---

htps:/blog.51cto.com/l e90/239083

之前360出的那个mongodb数据同步⼯具⽐较⽼，对于3.X版本的mongodb⽀持不太好。 阿⾥巴巴出了个 MongoShake ， ⽬前可以⽀持到MongoDB4.X（我测试从mongodb3.2.16同步数据 到mongodb4.0.4没问题）

官⽅地址： 中⽂介绍地址：

htps:/github.com/alibaba/MongoShake htps:/yq.aliyun.com/articles/60329

具体的介绍，可以参考上⾯第⼆个链接。 实际原理类似于 我们在mysql环境下常⽤的canal （MongoShake 通过订阅oplog， 然后给下游消费或者直接发送给下游mongodb实例）

MongoShake应⽤场景举例

- 1. MongoDB集群间数据的异步复制，免去业务双写开销。
- 2. MongoDB集群间数据的镜像备份（当前1.0开源版本⽀持受限）
- 3. ⽇志离线分析
- 4. ⽇志订阅
- 5. 数据路由。根据业务需求，结合⽇志订阅和过滤机制，可以获取关注的数据，达到数据路由的功

能。

- 6. Cache同步。⽇志分析的结果，知道哪些Cache可以被淘汰，哪些Cache可以进⾏预加载，反向

推动Cache的更新。

- 7. 基于⽇志的集群监控


MongoShake功能介绍 MongoShake从源库抓取oplog数据，然后发送到各个不同的tunnel通道。源库⽀持：ReplicaSet， Sharding，Mongod，⽬的库⽀持：Mongos，Mongod。现有通道类型有：

- 1. Direct：直接写⼊⽬的MongoDB

- 2. RPC：通过net/rpc⽅式连接

- 3. TCP：通过tcp⽅式连接

- 4. File：通过⽂件⽅式对接

- 5. Kafka：通过Kafka⽅式对接

- 6. Mock：⽤于测试，不写⼊tunnel，抛弃所有数据


![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

其它的介绍，可以参考上⾯的地址，这⾥就不⼤段贴了。

直接上实操吧：

环境： centos7 源库： mongodb 3.2.16 ⽬的库： mongodb 4.0.4

mongo-shake的编译安装 yum install golang golang-bin golang-src # 我这⾥安装的是1.9.4的go包

mkdir /home/gocode/ export GOPATH=/home/gocode/ echo 'export GOPATH=/home/gocode/' >> /root/.bashrc source /root/.bashrc

git clone https://github.com/aliyun/mongo-shake.git cd mongo-shake/src/vendor GOPATH=`pwd`/../..

go get -u -v github.com/kardianos/govendor # 依赖到这个包，需要先安装下 govendor

/home/gocode/bin/govendor sync

cd ../../ && ./build.sh 这样，就会在 bin⽬录下⽣成可执⾏的⼆进制⽂件

我这⾥编写的collector.conf 配置⽂件内容如下：

mongo_urls = mongodb://root:123456@192.168.2.4:27019 ## 如果是复制集环 境，建议这⾥填从节点地址以减少主的压⼒ collector.id = mongoshake checkpoint.interval = 5000

http_profile = 9100 system_profile = 9200

log_level = debug log_file = collector.log log_buffer = true

# 配置同步的⿊⽩名单 filter.namespace.black = filter.namespace.white =

oplog.gids = shard_key = auto syncer.reader.buffer_time = 1 worker = 8 worker.batch_queue_size = 64 adaptive.batching_max_size = 16384 fetcher.buffer_capacity = 256 worker.oplog_compressor = none

tunnel = direct # 拿到的oplog 直接写到⽬标实例 tunnel.address = mongodb://127.0.0.1:28017 # ⽬标库地址是 28017端⼝ context.storage = database context.address = ckpt_default context.start_position = 2000-01-01T00:00:01Z master_quorum = false replayer.dml_only = true ## 我这⾥只允许dml数据的同步，如果要允许ddl也传说到⽬ 标实例，需要把这个设置为false，具体参考官⽅的说明 replayer.executor = 1 replayer.executor.upsert = false replayer.executor.insert_on_dup_update = false

replayer.conflict_write_to = none replayer.durable = true

启动⽅式：

./bin/collector -conf=conf/collector.conf

⽇志在 logs ⽬录下： tailf logs/collector.log

在源实例上测试写⼊： use testdb; for (i=1;i<=10000;i++) db.tb3.insert( {name:"student"+i, age:(i%120), address: "shanghai" } ); db.tb3.count()

然后，在⽬标节点执⾏验证操作: use testdb; db.tb3.count()

可以看到，数据记录是⼀致的。 实际上测试下来，会有2秒左右的延迟。

其他⾛kafka，file ，rpc 等操作，没⽤到过，暂时不具备实验的条件。 如果只是单次的数据迁移，⽤dircet⽅式基本上就够了。 如果⽤在跨机房同步，⼀般建议⾛kafka的⽅ 式。
