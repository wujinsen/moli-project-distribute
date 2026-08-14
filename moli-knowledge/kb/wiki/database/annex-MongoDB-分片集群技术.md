---
title: MongoDB 分片集群技术.note（原文插图 annex）
slug: annex-MongoDB-分片集群技术
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/DataBase/mongodb/MongoDB 分片集群技术.note.md
related: [mongodb与文档库选型]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/clsn/p/8214345.html#auto-id-0

在了解分⽚集群之前，务必要先了解复制集技术！

- 1.1 MongoDB复制集简介 ⼀组Mongodb复制集，就是⼀组mongod进程，这些进程维护同⼀个数据集合。复制集提供了数

据冗余和⾼等级的可靠性，这是⽣产部署的基础。

- 1.1.1 复制集的⽬的 保证数据在⽣产部署时的冗余和可靠性，通过在不同的机器上保存副本来保证数据的不会因为单

点损坏⽽丢失。能够随时应对数据丢失、机器损坏带来的⻛险。

换⼀句话来说，还能提⾼读取能⼒，⽤户的读取服务器和写⼊服务器在不同的地⽅，⽽且，由不 同的服务器为不同的⽤户提供服务，提⾼整个系统的负载。

- 1.1.2 简单介绍 ⼀组复制集就是⼀组mongod实例掌管同⼀个数据集，实例可以在不同的机器上⾯。实例中包含⼀


个主导，接受客户端所有的写⼊操作，其他都是副本实例，从主服务器上获得数据并保持同步。

主服务器很重要，包含了所有的改变操作（写）的⽇志。但是副本服务器集群包含有所有的主服 务器数据，因此当主服务器挂掉了，就会在副本服务器上重新选取⼀个成为主服务器。

每个复制集还有⼀个仲裁者，仲裁者不存储数据，只是负责通过⼼跳包来确认集群中集合的数 量，并在主服务器选举的时候作为仲裁决定结果。

- 1.2 复制的基本架构 基本的架构由3台服务器组成，⼀个三成员的复制集，由三个有数据，或者两个有数据，⼀个作为


仲裁者。

- 1.2.1 三个存储数据的复制集 具有三个存储数据的成员的复制集有： ⼀ 个 主 库 ； 两个 从 库 组 成 ， 主 库 宕 机时 ， 这 两个 从 库 都 可 以 被 选 为主 库 。


![image 1](assets/imageFile1.png)

当主库宕机后,两个从库都会进⾏竞选，其中⼀个变为主库，当原主库恢复后，作为从库加⼊当前的 复制集群即可。

![image 2](assets/imageFile2.png)

- 1.2.2 当存在arbiter节点 在三个成员的复制集中，有两个正常的主从，及⼀台arbiter节点：


### ⼀ 个 主 库 ⼀ 个 从 库 ， 可 以 在 选 举中 成 为主 库 ⼀ 个 aribiter节 点 ， 在 选 举中 ， 只 进 ⾏ 投 票 ，不 能 成 为主 库

![image 3](assets/imageFile3.png)

说明：

### 由 于 arbiter节 点 没 有 复 制 数据 ， 因 此 这 个 架构 中仅 提 供 ⼀ 个 完 整 的 数据 副 本 。 arbiter节 点 只 需 要 更 少 的 资 源 ， 代价 是更有 限 的 冗 余 和 容 错 。

当主库宕机时，将会选择从库成为主，主库修复后，将其加⼊到现有的复制集群中即可。

![image 4](assets/imageFile4.png)

- 1.2.3 Primary选举 复制集通过replSetInitiate命令（或mongo shel的rs.initiate()）进⾏初始化，初始化后各个成员间


开始发送⼼跳消息，并发起Priamry选举操作，获得『⼤多数』成员投票⽀持的节点，会成为 Primary，其余节点成为Secondary。

『⼤多数』的定义

假设复制集内投票成员（后续介绍）数量为N，则⼤多数为 N/2 + 1，当复制集内存活成员数量不 ⾜⼤多数时，整个复制集将⽆法选举出Primary，复制集将⽆法提供写服务，处于只读状态。

<table>
  <tr>
    <th>投票成员数</th>
    <th>⼤多数</th>
    <th>容忍失效数</th>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>1</td>
    <td>1</td>
    <td>0</td>
  </tr>
  <tr>
    <td>2</td>
    <td>2</td>
    <td>0</td>
  </tr>
  <tr>
    <td>3</td>
    <td>2</td>
    <td>1</td>
  </tr>
  <tr>
    <td>4</td>
    <td>3</td>
    <td>1</td>
  </tr>
  <tr>
    <td>5</td>
    <td>3</td>
    <td>2</td>
  </tr>
  <tr>
    <td>6</td>
    <td>4</td>
    <td>2</td>
  </tr>
</table>


7 4 3

通常建议将复制集成员数量设置为奇数，从上表可以看出3个节点和4个节点的复制集都只能容忍1 个节点失效，从『服务可⽤性』的⻆度看，其效果是⼀样的。（但⽆疑4个节点能提供更可靠的数据存 储）

# 1.3复制集中成员说明

- 1.3.1 所有成员说明


<table>
  <tr>
    <th>成员</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>Secondary</td>
    <td>正常情况下，复制集的Seconary会参与Primary选 举（⾃身也可能会被选为Primary），并从 Primary同步最新写⼊的数据，以保证与Primary 存储相同的数据。 Secondary可以提供读服务，增加Secondary节点 可以提供复制集的读服务能⼒，同时提升复制集 的可⽤性。另外，Mongodb⽀持对复制集的 Secondary节点进⾏灵活的配置，以适应多种场 景的需求。</td>
  </tr>
  <tr>
    <td>Arbiter</td>
    <td>Arbiter节点只参与投票，不能被选为Primary，并 且不从Primary同步数据。 ⽐如你部署了⼀个2个节点的复制集，1个 Primary，1个Secondary，任意节点宕机，复制 集将不能提供服务了（⽆法选出Primary），这时 可以给复制集添加⼀个Arbiter节点，即使有节点 宕机，仍能选出Primary。 Arbiter本身不存储数据，是⾮常轻量级的服务， 当复制集成员为偶数时，最好加⼊⼀个Arbiter节 点，以提升复制集可⽤性。</td>
  </tr>
  <tr>
    <td>Priority0</td>
    <td>Priority0节点的选举优先级为0，不会被选举为 Primary ⽐如你跨机房A、B部署了⼀个复制集，并且想指 定Primary必须在A机房，这时可以将B机房的复 制集成员Priority设置为0，这样Primary就⼀定会 是A机房的成员。 （注意：如果这样部署，最好将『⼤多数』节点 部署在A机房，否则⽹络分区时可能⽆法选出<br><br>）</td>
  </tr>
  <tr>
    <td>Vote0</td>
    <td>Primary Mongodb 3.0⾥，复制集成员最多50个，参与 Primary选举投票的成员最多7个，其他成员 （Vote0）的vote属性必须设置为0，即不参与投 票。</td>
  </tr>
  <tr>
    <td>Hi den</td>
    <td>Hi den节点不能被选为主（Priority为0），并且 对Driver不可⻅。因Hi den节点不会接受Driver的 请求，可使⽤Hi den节点做⼀些数据备份、离线 计算的任务，不会影响复制集的服务。</td>
  </tr>
  <tr>
    <td>Delayed</td>
    <td>Delayed节点必须是Hi den节点，并且其数据落 后与Primary⼀段时间（可配置，⽐如1个⼩ 时）。 因Delayed节点的数据⽐Primary落后⼀段时间， 当错误或者⽆效的数据写⼊Primary时，可通过<br><br>节点的数据来恢复到之前的时间点。</td>
  </tr>
</table>


#### Delayed

- 1.3.2 Priority 0节点 作为⼀个辅助可以作为⼀个备⽤。在⼀些复制集中，可能⽆法在合理的时间内添加新成员的时

候。备⽤成员保持数据的当前最新数据能够替换不可⽤的成员。

- 1.3.3 Hi den 节点（隐藏节点） 客户端将不会把读请求分发到隐藏节点上，即使我们设定了 复制集读选项 。 这些隐藏节点将不会收到来⾃应⽤程序的请求。我们可以将隐藏节点专⽤于报表节点或是备份节

点。 延时节点也应该是⼀个隐藏节点。

- 1.3.4 Delayed 节点（延时节点） 延时节点的数据集是延时的，因此它可以帮助我们在⼈为误操作或是其他意外情况下恢复数据。 举个例⼦，当应⽤升级失败，或是误操作删除了表和数据库时，我们可以通过延时节点进⾏数据


![image 5](assets/imageFile5.png)

![image 6](assets/imageFile6.png)

恢复。

![image 7](assets/imageFile7.png)

## 1.4 配置MongoDB复制集

- 1.4.1 环境说明 系统 环 境 说 明 ： [root@MongoDB ~]# cat /etc/redhat-release CentOS release 6.9 (Final) [root@MongoDB ~]# uname -r

- 2.6.32-696.el6.x86_64 [root@MongoDB ~]# /etc/init.d/iptables status iptables: Firewall is not running. [root@MongoDB ~]# getenforce Disabled [root@MongoDB ~]# hostname -I 10.0.0.152 172.16.1.152


软 件 版 本 说 明

本次使⽤的mongodb版本为：mongodb-linux-x86_64-3.2.8.tgz

- 1.4.2 前期准备，在rot⽤户下操作 本次复制集复制采⽤Mongodb多实例进⾏ 所有的操作都基于安装完成的mongodb服务，详情参照：

#创建mongod⽤户 useradd -u800 mongod echo 123456|passwd --stdin mongod

# 安装mongodb mkdir -p /mongodb/bin cd /mongodb wget http://downloads.mongodb.org/linux/mongodb-linux-x86_64-rhel62-3.2.8.tgz tar xf mongodb-linux-x86_64-3.2.8.tgz cd mongodb-linux-x86_64-3.2.8/bin/ &&\ cp * /mongodb/bin chown -R mongod.mongod /mongodb

# 切换到mongod⽤户进⾏后续操作 su - mongod

- 1.4.3 创建所需⽬录


htp:/ w.cnblogs.com/clsn/p/82141 94.html#_label3

do

mkdir -p /mongodb/$i/conf mkdir -p /mongodb/$i/data mkdir -p /mongodb/$i/log

done

- 1.4.4 配置多实例环境


编辑第⼀个实例配置⽂件

cat >>/mongodb/28017/conf/mongod.conf<<'EOF' systemLog:

destination: file path: /mongodb/28017/log/mongodb.log logAppend: true

storage: journal:

enabled: true dbPath: /mongodb/28017/data directoryPerDB: true #engine: wiredTiger wiredTiger:

engineConfig: # cacheSizeGB: 1 directoryForIndexes: true

collectionConfig:

blockCompressor: zlib indexConfig:

prefixCompression: true processManagement:

fork: true net:

port: 28017

replication: oplogSizeMB: 2048 replSetName: my_repl

EOF

复制配置⽂件

for i in 28018 28019 28020 do

\cp /mongodb/28017/conf/mongod.conf /mongodb/$i/conf/ done

修改配置⽂件

for i in 28018 28019 28020 do

sed -i "s#28017#$i#g" /mongodb/$i/conf/mongod.conf done

启动服务

do

mongod -f /mongodb/$i/conf/mongod.conf done

# 关闭服务的⽅法

for i in 28017 28018 28019 28020 do

mongod --shutdown -f /mongodb/$i/conf/mongod.conf done

- 1.4.5 配置复制集 登陆数据库，配置mongodb复制 shell> mongo --port 28017

config = {_id: 'my_repl', members: [

- {_id: 0, host: '10.0.0.152:28017'},

- {_id: 1, host: '10.0.0.152:28018'},

- {_id: 2, host: '10.0.0.152:28019'}]


}

初始化这个配置

> rs.initiate(config)

到此复制集配置完成

- 1.4.6 测试主从复制


在主节点插⼊数据

my_repl:PRIMARY> db.movies.insert([ { "title" : "Jaws", "year" : 1975, "imdb_rating" : 8.1 },

{ "title" : "Batman", "year" : 1989, "imdb_rating" : 7.6 }, ] );

在主节点查看数据

my_repl:PRIMARY> db.movies.find().pretty() {

"_id" : ObjectId("5a4d9ec184b9b2076686b0ac"), "title" : "Jaws", "year" : 1975, "imdb_rating" : 8.1

} {

"_id" : ObjectId("5a4d9ec184b9b2076686b0ad"), "title" : "Batman", "year" : 1989, "imdb_rating" : 7.6

}

注：在mongodb复制集当中，默认从库不允许读写。 在从库打开配置（危险）

注意：严禁在从库做任何修改操作

my_repl:SECONDARY> rs.slaveOk() my_repl:SECONDARY> show tables; movies my_repl:SECONDARY> db.movies.find().pretty() {

"_id" : ObjectId("5a4d9ec184b9b2076686b0ac"), "title" : "Jaws", "year" : 1975, "imdb_rating" : 8.1

} {

"_id" : ObjectId("5a4d9ec184b9b2076686b0ad"), "title" : "Batman", "year" : 1989, "imdb_rating" : 7.6

}

在从库查看完成在登陆到主库

- 1.4.7 复制集管理操作

- （1）查看复制集状态： rs.status(); # 查看整体复制集状态 rs.isMaster(); # 查看当前是否是主节点

- （2）添加删除节点 rs.add("ip:port"); # 新增从节点 rs.addArb("ip:port"); # 新增仲裁节点 rs.remove("ip:port"); # 删除⼀个节点 注： 添 加 特 殊 节 点 时 ，

- 1>可 以 在 搭 建 过 程 中 设 置 特 殊 节 点
- 2>可 以 通过 修 改 配 置 的 ⽅ 式 将 普 通 从 节 点 设 置 为 特 殊 节 点 /*找 到 需 要 改 为 延 迟 性 同 步 的 数 组 号 */;


- （3）配置延时节点（⼀般延时节点也配置成hi den） cfg=rs.conf() cfg.members[2].priority=0 cfg.members[2].slaveDelay=120 cfg.members[2].hidden=true


注：这⾥的2是rs.conf()显示的顺序（除 主 库 之 外 ），⾮ID 重写复制集配置

rs.reconfig(cfg)

也可将延时节点配置为arbiter节点

cfg.members[2].arbiterOnly=true

配置成功后，通过以下命令查询配置后的属性

rs.conf();

- 1.4.8 副本集其他操作命令 查看副本集的配置信息


my_repl:PRIMARY> rs.config()

查看副本集各成员的状态

my_repl:PRIMARY> rs.status()

- 1.4.8.1 副本集⻆⾊切换（不要⼈为随便操作） rs.stepDown() rs.freeze(300) # 锁定从，使其不会转变成主库，freeze()和stepDown单位都是秒。 rs.slaveOk() # 设置副本节点可读：在副本节点执⾏

插⼊数据

> use app switched to db app app> db.createCollection('a') { "ok" : 0, "errmsg" : "not master", "code" : 10107 }

查看副本节点

> rs.printSlaveReplicationInfo() source: 192.168.1.22:27017

syncedTo: Thu May 26 2016 10:28:56 GMT+0800 (CST) 0 secs (0 hrs) behind the primary

MongoDB分⽚（Sharding）技术

分⽚（sharding）是MongoDB⽤来将⼤型集合分割到不同服务器（或者说⼀个集群）上所采⽤的 ⽅法。尽管分⽚起源于关系型数据库分区，但MongoDB分⽚完全⼜是另⼀回事。

和MySQL分区⽅案相⽐，MongoDB的最⼤区别在于它⼏乎能⾃动完成所有事情，只要告诉 MongoDB要分配数据，它就能⾃动维护数据在不同服务器之间的均衡。

- 2.1 MongoDB分⽚介绍


- 2.1.1 分⽚的⽬的 ⾼数据量和吞吐量的数据库应⽤会对单机的性能造成较⼤压⼒,⼤的查询量会将单机的CPU耗尽,⼤

的数据量对单机的存储压⼒较⼤,最终会耗尽系统的内存⽽将压⼒转移到磁盘IO上。

为了解决这些问题,有两个基本的⽅法: 垂直扩展和⽔平扩展。 垂直扩展：增加更多的CPU和存储资源来扩展容量。 ⽔平扩展：将数据集分布在多个服务器上。⽔平扩展即分⽚。

- 2.1.2 分⽚设计思想 分⽚为应对⾼吞吐量与⼤数据量提供了⽅法。使⽤分⽚减少了每个分⽚需要处理的请求数，因


此，通过⽔平扩展，集群可以提⾼⾃⼰的存储容量和吞吐量。举例来说，当插⼊⼀条数据时，应⽤只 需要访问存储这条数据的分⽚.

使⽤分⽚减少了每个分⽚存储的数据。 例如，如果数据库1tb的数据集，并有4个分⽚，然后每个分⽚可能仅持有256 GB的数据。如果有

40个分⽚，那么每个切分可能只有25GB的数据。

![image 8](assets/imageFile8.png)

#### 2.1.3 分⽚机制提供了如下三种优势

- 1.对集群进⾏抽象，让集群“不可⻅” MongoDB⾃带了⼀个叫做mongos的专有路由进程。mongos就是掌握统⼀路⼝的路由器，其会将

客户端发来的请求准确⽆误的路由到集群中的⼀个或者⼀组服务器上，同时会把接收到的响应拼装起 来发回到客户端。

- 2.保证集群总是可读写 MongoDB通过多种途径来确保集群的可⽤性和可靠性。将MongoDB的分⽚和复制功能结合使

⽤，在确保数据分⽚到多台服务器的同时，也确保了每分数据都有相应的备份，这样就可以确保有服 务器换掉时，其他的从库可以⽴即接替坏掉的部分继续⼯作。

- 3.使集群易于扩展 当系统需要更多的空间和资源的时候，MongoDB使我们可以按需⽅便的扩充系统容量。


#### 2.1.4 分⽚集群架构

<table>
  <tr>
    <th>组件</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>Config Server</td>
    <td>存储集群所有节点、分⽚数据路由信息。默认需 节点。</td>
  </tr>
  <tr>
    <td>Mongos</td>
    <td>要配置3个Config Server 提供对外应⽤访问，所有操作均通过mongos执 ⾏。⼀般有多个mongos节点。数据迁移和数据⾃ 动平衡。</td>
  </tr>
  <tr>
    <td>Mongod</td>
    <td>存储应⽤数据记录。⼀般有多个Mongod节点，达 到数据分⽚⽬的。</td>
  </tr>
</table>


![image 9](assets/imageFile9.png)

分⽚集群的构造

- （ 1） mongos ： 数据 路 由 ， 和 客 户 端 打 交 道 的 模 块 。 mongos本 身 没 有 任何 数据 ， 他也 不 知 道 该 怎 么 处 理 这 数据 ， 去 找 config server
- （ 2） config server： 所 有 存 、 取 数据 的 ⽅ 式 ， 所 有 shard节 点 的 信 息 ， 分 ⽚ 功 能 的 ⼀ 些 配 置 信 息 。 可 以 理 解 为 真 实 数据 的 元 数据 。
- （ 3） shard： 真 正 的 数据 存 储 位 置 ， 以 chunk为 单 位 存 数据 。 Mongos本身并不持久化数据，Sharded cluster所有的元数据都会存储到Config Server，⽽⽤户


的数据会议分散存储到各个shard。Mongos启动后，会从配置服务器加载元数据，开始提供服务，将 ⽤户的请求正确路由到对应的碎⽚。

Mongos的路由功能

当数据写⼊时，MongoDB Cluster根据分⽚键设计写⼊数据。 当外部语句发起数据查询时，MongoDB根据数据分布⾃动路由⾄指定节点返回数据。

## 2.2 集群中数据分布

- 2.2.1 Chunk是什么 在⼀个shard server内部，MongoDB还是会把数据分为chunks，每个chunk代表这个shard server


内部⼀部分数据。chunk的产⽣，会有以下两个⽤途：

Spliting：当⼀个chunk的⼤⼩超过配置中的chunk size时，MongoDB的后台进程会把这个chunk 切分成更⼩的chunk，从⽽避免chunk过⼤的情况

Balancing：在MongoDB中，balancer是⼀个后台进程，负责chunk的迁移，从⽽均衡各个shard server的负载，系统初始1个chunk，chunk size默认值64M,⽣产库上选择适合业务的chunk size是最 好的。ongoDB会⾃动拆分和迁移chunks。

分⽚集群的数据分布（shard节点）

- （ 1） 使 ⽤ chunk来 存 储 数据
- （ 2） 进 群 搭 建 完 成 之 后 ， 默 认 开 启 ⼀ 个 chunk， ⼤ ⼩ 是 64M，
- （ 3） 存 储 需 求 超 过 64M， chunk会 进 ⾏ 分 裂 ， 如 果 单 位 时 间 存 储 需 求 很 ⼤ ， 设 置 更 ⼤ 的 chunk
- （ 4） chunk会 被 ⾃ 动 均 衡 迁 移 。


- 2.2.2 chunksize的选择 适合业务的chunksize是最好的。 chunk的分裂和迁移⾮常消耗IO资源；chunk分裂的时机：在插⼊和更新，读数据不会分裂。 chunksize的选择： ⼩的chunksize：数据均衡是迁移速度快，数据分布更均匀。数据分裂频繁，路由节点消耗更多资

源。⼤的chunksize：数据分裂少。数据块移动集中消耗IO资源。通常10-20M

- 2.2.3 chunk分裂及迁移 随着数据的增⻓，其中的数据⼤⼩超过了配置的chunk size，默认是64M，则这个chunk就会分裂


成两个。数据的增⻓会让chunk分裂得越来越多。

![image 10](assets/imageFile10.png)

这时候，各个shard 上的chunk数量就会不平衡。这时候，mongos中的⼀个组件balancer 就会执 ⾏⾃动平衡。把chunk从chunk数量最多的shard节点挪动到数量最少的节点。

![image 11](assets/imageFile11.png)

chunkSize 对分裂及迁移的影响

MongoDB 默认的 chunkSize 为64MB，如⽆特殊需求，建议保持默认值；chunkSize 会直接影响 到 chunk 分裂、迁移的⾏为。

chunkSize 越⼩，chunk 分裂及迁移越多，数据分布越均衡；反之，chunkSize 越⼤，chunk 分 裂及迁移会更少，但可能导致数据分布不均。

chunkSize 太⼩，容易出现 jumbo chunk（即shardKey 的某个取值出现频率很⾼，这些⽂档只能 放到⼀个 chunk ⾥，⽆法再分裂）⽽⽆法迁移；chunkSize 越⼤，则可能出现 chunk 内⽂档数太多 （chunk 内⽂档数不能超过 25 0 ）⽽⽆法迁移。

chunk ⾃动分裂只会在数据写⼊时触发，所以如果将 chunkSize 改⼩，系统需要⼀定的时间来将 chunk 分裂到指定的⼤⼩。

chunk 只会分裂，不会合并，所以即使将 chunkSize 改⼤，现有的 chunk 数量不会减少，但 chunk ⼤⼩会随着写⼊不断增⻓，直到达到⽬标⼤⼩。

## 2.3 数据区分

- 2.3.1 分⽚键shard key MongoDB中数据的分⽚是、以集合为基本单位的，集合中的数据通过⽚键（Shard key）被分成


多部分。其实⽚键就是在集合中选⼀个键，⽤该键的值作为数据拆分的依据。 所以⼀个好的⽚键对分⽚⾄关重要。⽚键必须是⼀个索引，通过sh.shardColection加会⾃动创建

索引（前提是此集合不存在的情况下）。⼀个⾃增的⽚键对写⼊和数据均匀分布就不是很好，因为⾃ 增的⽚键总会在⼀个分⽚上写⼊，后续达到某个阀值可能会写到别的分⽚。但是按照⽚键查询会⾮常 ⾼效。

随机⽚键对数据的均匀分布效果很好。注意尽量避免在多个分⽚上进⾏查询。在所有分⽚上查 询，mongos会对结果进⾏归并排序。

对集合进⾏分⽚时，你需要选择⼀个⽚键，⽚键是每条记录都必须包含的，且建⽴了索引的单个 字段或复合字段，MongoDB按照⽚键将数据划分到不同的数据块中，并将数据块均衡地分布到所有分 ⽚中。

为了按照⽚键划分数据块，MongoDB使⽤基于范围的分⽚⽅式或者 基于哈希的分⽚⽅式。

注意：

分 ⽚ 键 是 不 可变 。 分 ⽚ 键 必 须 有 索 引 。 分 ⽚ 键 ⼤ ⼩ 限 制 512bytes。 分 ⽚ 键 ⽤ 于 路 由 查 询 。 MongoDB不 接 受 已 进 ⾏ colection级 分 ⽚ 的 colection上 插 ⼊ ⽆ 分 ⽚ 键 的 ⽂ 档 （ 也 不 ⽀ 持 空 值 插 ⼊ ）

- 2.3.2 以范围为基础的分⽚Sharded Cluster Sharded Cluster⽀持将单个集合的数据分散存储在多shard上，⽤户可以指定根据集合内⽂档的某


个字段即shard key来进⾏范围分⽚（range sharding）。

![image 12](assets/imageFile12.png)

对于基于范围的分⽚，MongoDB按照⽚键的范围把数据分成不同部分。 假设有⼀个数字的⽚键:想象⼀个从负⽆穷到正⽆穷的直线，每⼀个⽚键的值都在直线上画了⼀个

点。MongoDB把这条直线划分为更短的不重叠的⽚段，并称之为数据块，每个数据块包含了⽚键在⼀ 定范围内的数据。在使⽤⽚键做范围划分的系统中，拥有”相近”⽚键的⽂档很可能存储在同⼀个数据 块中，因此也会存储在同⼀个分⽚中。

- 2.3.3 基于哈希的分⽚ 分⽚过程中利⽤哈希索引作为分⽚的单个键，且哈希分⽚的⽚键只能使⽤⼀个字段，⽽基于哈希


⽚键最⼤的好处就是保证数据在各个节点分布基本均匀。

![image 13](assets/imageFile13.png)

对于基于哈希的分⽚，MongoDB计算⼀个字段的哈希值，并⽤这个哈希值来创建数据块。在使⽤ 基于哈希分⽚的系统中，拥有”相近”⽚键的⽂档很可能不会存储在同⼀个数据块中，因此数据的分离 性更好⼀些。

Hash分⽚与范围分⽚互补，能将⽂档随机的分散到各个chunk，充分的扩展写能⼒，弥补了范围 分⽚的不⾜，但不能⾼效的服务范围查询，所有的范围查询要分发到后端所有的Shard才能找出满⾜条 件的⽂档。

- 2.3.4 分⽚键选择建议


- 1、递增的sharding key 数据 ⽂ 件 挪 动 ⼩ 。 （ 优 势 ） 因 为 数据 ⽂ 件 递 增 ， 所 以会 把 insert的 写 IO永 久 放 在 最 后 ⼀ ⽚ 上， 造 成 最 后 ⼀ ⽚ 的 写 热点 。 同 时 ， 随 着 最 后 ⼀ ⽚ 的 数据 量 增 ⼤ ， 将 不 断 的 发 ⽣ 迁 移 ⾄ 之 前 的 ⽚ 上 。
- 2、随机的sharding key 数据 分 布 均 匀 ， insert的 写 IO均 匀分 布 在 多 个 ⽚ 上 。 （ 优 势 ） ⼤ 量 的 随 机 IO， 磁 盘 不 堪 重 荷 。
- 3、混合型key ⼤ ⽅ 向 随 机 递 增 ， ⼩ 范 围 随 机 分 布 。 为了 防 ⽌ 出 现 ⼤ 量 的 chunk均 衡 迁 移 ， 可 能 造 成 的 IO压 ⼒ 。 我 们 需 要设 置 合 理 分 ⽚ 使 ⽤ 策 略 （ ⽚ 键 的 选 择 、 分 ⽚ 算 法 （ range、 hash）） 分⽚注意：


分⽚键是不可变、分⽚键必须有索引、分⽚键⼤⼩限制512bytes、分⽚键⽤于路由查询。 MongoDB不接受已进⾏colection级分⽚的colection上插⼊⽆分⽚键的⽂档（也不⽀持空值插⼊）

- 2.4 部署分⽚集群 本集群的部署基于1.1的复制集搭建完成。


- 2.4.1 环境准备 创建程序所需的⽬录


- for i in 17 18 19 20 21 22 23 24 25 26 do mkdir -p /mongodb/280$i/conf mkdir -p /mongodb/280$i/data mkdir -p /mongodb/280$i/log


done

- 2.4.2 shard集群配置 编辑shard集群配置⽂件


cat > /mongodb/28021/conf/mongod.conf <<'EOF' systemLog:

destination: file path: /mongodb/28021/log/mongodb.log logAppend: true

storage: journal:

enabled: true dbPath: /mongodb/28021/data directoryPerDB: true #engine: wiredTiger wiredTiger:

engineConfig: cacheSizeGB: 1 directoryForIndexes: true

collectionConfig:

blockCompressor: zlib indexConfig:

prefixCompression: true

net: bindIp: 10.0.0.152 port: 28021

replication: oplogSizeMB: 2048 replSetName: sh1

sharding:

clusterRole: shardsvr processManagement:

fork: true EOF

复制shard集群配置⽂件

for i in 22 23 24 25 26 do

\cp /mongodb/28021/conf/mongod.conf /mongodb/280$i/conf/ done

修改配置⽂件端⼝

for i in 22 23 24 25 26 do

sed -i "s#28021#280$i#g" /mongodb/280$i/conf/mongod.conf done

修改配置⽂件复制集名称（replSetName）

for i in 24 25 26 do

sed -i "s#sh1#sh2#g" /mongodb/280$i/conf/mongod.conf done

启动shard集群

##### for i in 21 22 23 24 25 26 do

mongod -f /mongodb/280$i/conf/mongod.conf done

- 配置复制集1 mongo --host 10.0.0.152 --port 28021 admin

# 配置复制集

- config = {_id: 'sh1', members: [

- {_id: 0, host: '10.0.0.152:28021'},

- {_id: 1, host: '10.0.0.152:28022'},

- {_id: 2, host: '10.0.0.152:28023',"arbiterOnly":true}]


} # 初始化配置

rs.initiate(config)

配置复制集2

mongo --host 10.0.0.152 --port 28024 admin

# 配置复制集

- config = {_id: 'sh2', members: [




- {_id: 0, host: '10.0.0.152:28024'},

- {_id: 1, host: '10.0.0.152:28025'},

- {_id: 2, host: '10.0.0.152:28026',"arbiterOnly":true}]


}

# 初始化配置 rs.initiate(config)

- 2.4.3 config集群配置 创建主节点配置⽂件


cat > /mongodb/28018/conf/mongod.conf <<'EOF' systemLog:

destination: file path: /mongodb/28018/log/mongodb.conf logAppend: true

storage: journal:

enabled: true dbPath: /mongodb/28018/data directoryPerDB: true #engine: wiredTiger wiredTiger:

engineConfig: cacheSizeGB: 1 directoryForIndexes: true

collectionConfig:

blockCompressor: zlib indexConfig:

prefixCompression: true

net: bindIp: 10.0.0.152 port: 28018

replication: oplogSizeMB: 2048 replSetName: configReplSet

sharding:

clusterRole: configsvr processManagement:

fork: true EOF

将配置⽂件分发到从节点

for i in 19 20 do

\cp /mongodb/28018/conf/mongod.conf /mongodb/280$i/conf/ done

修改配置⽂件端⼝信息

for i in 19 20 do

sed -i "s#28018#280$i#g" /mongodb/280$i/conf/mongod.conf done

启动config server集群

- for i in 18 19 20 do


mongod -f /mongodb/280$i/conf/mongod.conf done

配置config server复制集

mongo --host 10.0.0.152 --port 28018 admin

# 配置复制集信息

config = {_id: 'configReplSet', members: [

- {_id: 0, host: '10.0.0.152:28018'},

- {_id: 1, host: '10.0.0.152:28019'},

- {_id: 2, host: '10.0.0.152:28020'}]


}

# 初始化配置 rs.initiate(config)

注：config server 使⽤复制集不⽤有arbiter节点。3.4版本以后config必须为复制集

- 2.4.4 mongos节点配置


修改配置⽂件

cat > /mongodb/28017/conf/mongos.conf <<'EOF' systemLog:

destination: file path: /mongodb/28017/log/mongos.log logAppend: true

net: bindIp: 10.0.0.152 port: 28017

sharding:

configDB: configReplSet/10.0.0.152:28108,10.0.0.152:28019,10.0.0.152:28020 processManagement:

fork: true EOF

启动mongos

mongos -f /mongodb/28017/conf/mongos.conf

登陆到mongos

mongo 10.0.0.152:28017/admin

添加分⽚节点

- db.runCommand( { addshard : "sh1/10.0.0.152:28021,10.0.0.152:28022,10.0.0.152:28023",name:"shard1"} )

- db.runCommand( { addshard : "sh2/10.0.0.152:28024,10.0.0.152:28025,10.0.0.152:28026",name:"shard2"} )


列出分⽚

mongos> db.runCommand( { listshards : 1 } ) {

"shards" : [ {

"_id" : "shard2", "host" : "sh2/10.0.0.152:28024,10.0.0.152:28025"

}, {

"_id" : "shard1", "host" : "sh1/10.0.0.152:28021,10.0.0.152:28022"

}

], "ok" : 1

}

整体状态查看

mongos> sh.status();

⾄此MongoDB的分⽚集群就搭建完成。

- 2.4.5 数据库分⽚配置


激活数据库分⽚功能

语法：( { enablesharding : "数据库名称" } )

mongos> db.runCommand( { enablesharding : "test" } )

指定分⽚建对集合分⽚，范围⽚键 -创建索引

mongos> use test mongos> db.vast.ensureIndex( { id: 1 } ) mongos> use admin mongos> db.runCommand( { shardcollection : "test.vast",key : {id: 1} } )

集合分⽚验证

mongos> use test mongos> for(i=0;i<20000;i++){ db.vast1.insert({"id":i,"name":"clsn","age":70,"date":new Date()}); } mongos> db.vast.stats()

插⼊数据的条数尽量⼤些，能够看出更好的效果。

## 2.5 分⽚集群的操作

- 2.5.1 不同分⽚键的配置


范围⽚键

admin> sh.shardCollection("数据库名称.集合名称",key : {分⽚键: 1} ) 或 admin> db.runCommand( { shardcollection : "数据库名称.集合名称",key : {分⽚键: 1} } )

eg：

admin > sh.shardCollection("test.vast",key : {id: 1} ) 或 admin> db.runCommand( { shardcollection : "test.vast",key : {id: 1} } )

哈希⽚键

admin > sh.shardCollection( "数据库名.集合名", { ⽚键: "hashed" } )

创建哈希索引

admin> db.vast.ensureIndex( { a: "hashed" } ) admin > sh.shardCollection( "test.vast", { a: "hashed" } )

- 2.5.2 分⽚集群的操作


判断是否Shard集群

admin> db.runCommand({ isdbgrid : 1})

列出所有分⽚信息

admin> db.runCommand({ listshards : 1})

列出开启分⽚的数据库

admin> use config config> db.databases.find( { "partitioned": true } ) config> db.databases.find() //列出所有数据库分⽚情况

查看分⽚的⽚键

config> db.collections.find() {

"_id" : "test.vast", "lastmodEpoch" : ObjectId("58a599f19c898bbfb818b63c"), "lastmod" : ISODate("1970-02-19T17:02:47.296Z"), "dropped" : false, "key" : {

"id" : 1

}, "unique" : false

}

查看分⽚的详细信息

admin> db.printShardingStatus() 或 admin> sh.status()

删除分⽚节点

sh.getBalancerState() mongos> db.runCommand( { removeShard: "shard2" } )

## 2.6 balance操作

查看mongo集群是否开启了 balance 状态

mongos> sh.getBalancerState() true

当然你也可以通过在路由节点mongos上执⾏sh.status() 查看balance状态。 如果balance开启，查看是否正在有数据的迁移

连接mongo集群的路由节点

mongos> sh.isBalancerRunning() false

- 2.6.1 设置balance 窗⼝


- （1）连接mongo集群的路由节点
- （2）切换到配置节点 use config

- （3）确定balance 开启中 sh.getBalancerState()

如果未开启，执⾏命令

sh.setBalancerState( true )

- （4）修改balance 窗⼝的时间 db.settings.update(


{ _id: "balancer" }, { $set: { activeWindow : { start : "<start-time>", stop : "<stop-time>" } } }, { upsert: true }

)

eg：

db.settings.update({ _id : "balancer" }, { $set : { activeWindow : { start : "00:00", stop : "5:00" } } }, true )

当你设置了activeWindow，就不能⽤sh.startBalancer() 启动balance NOTE The balancer window must be suficient to complete the migration of al data inserted during the day. As data insert rates can change based on activity and usage paterns, it is important to ensure that the balancing window you select wil be suficient to suport the neds of your deployment.

- （5）删除balance 窗⼝ use config db.settings.update({ _id : "balancer" }, { $unset : { activeWindow : true } })


- 2.6.2 关闭balance 默认balance 的运⾏可以在任何时间，只迁移需要迁移的chunk，如果要关闭balance运⾏，停⽌


⼀段时间可以⽤下列⽅法：

- （1） 连接到路由mongos节点
- （2） 停⽌balance sh.stopBalancer()

- （3） 查看balance状态 sh.getBalancerState()

- （4）停⽌balance 后，没有迁移进程正在迁移，可以执⾏下列命令 use config while( sh.isBalancerRunning() ) {


print("waiting..."); sleep(1000);

}

- 2.6.3 重新打开balance 如果你关闭了balance，准备重新打开balance

- （1） 连接到路由mongos节点
- （2） 打开balance sh.setBalancerState(true)


如果驱动没有命令 sh.startBalancer()，可以⽤下列命令

use config db.settings.update( { _id: "balancer" }, { $set : { stopped: false } } , { upsert: true } )

- 2.6.4 关于集合的balance 关闭某个集合的balance sh.disableBalancing("students.grades") 打开某个集合的balance sh.enableBalancing("students.grades") 确定某个集合的balance是开启或者关闭 db.getSiblingDB("config").collections.findOne({_id : "students.grades"}).noBalance;

- 2.6.5 问题解决 mongodb在做⾃动分⽚平衡的时候，或引起数据库响应的缓慢，可以通过禁⽤⾃动平衡以及设置⾃动 平衡进⾏的时间来解决这⼀问题。


- （1）禁⽤分⽚的⾃动平衡 // connect to mongos > use config > db.settings.update( { _id: "balancer" }, { $set : { stopped: true } } , true );

- （2）⾃定义 ⾃动平衡进⾏的时间段 // connect to mongos > use config > db.settings.update({ _id : "balancer" }, { $set : { activeWindow : { start : "21:00", stop : "9:00" } } }, true )


- 2.7 参考⽂献 htps:/ w.jianshu.com/p/dc3643aec9 htps:/docs.mongodb.com/manual/replication/ htps:/docs.mongodb.com/manual/core/replica-set-architecture-thre-members/ htp:/ w.mongoing.com/archives/215 htps:/docs.mongodb.com/manual/sharding/ htp:/ w.ywnds.com/?p=3476 htps:/yq.aliyun.com/articles/32434


- [1]
- [2]
- [3]
- [4]
- [5]
- [6]
- [7]
