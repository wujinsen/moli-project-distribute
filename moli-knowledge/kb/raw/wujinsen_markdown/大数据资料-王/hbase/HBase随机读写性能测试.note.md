本⽂转载⾃淘宝⽹BlueDavy同学的博客，⽂章基于淘宝对HBase的⼤量应⽤，给出了⼀个HBase的

随 机读写性能测试 性能

结果，对测试环境、配置及 参数分析都有较详细的描述，推荐给各位NoSQL Fans。

根据最近⽣产环境使⽤的经验，更多的项⽬的采⽤，以及采⽤了更加⾃动的测试平台，对HBase做 了更多的场景的测试，在这篇blog中来分享下纯粹的随机写和随机读的性能数据，同时也分享下我 们调整过后的参数。 测试环境说明：

- 1、Region Server： 5台，12块1T SATA盘(720 RPM)，No Raid，物理内存24G，CPU型号为 E5620；启动参数为：-Xms16g -Xmx16g -Xmn2g -X SurvivorRatio=2 -

X:+UseCMSInitiatingOcupancyOnly -X CMSInitiatingOcupancyFraction=85

- 2、Data Node：35台，和Region Server同样的硬件配置，启动参数上-Xms2g -Xmx2g，未设 置-Xmn；


服务端参数：

hbase

.replication false

hbase.balancer.period 12 0

hfile.block.cache.size 0.4，随机读20%命中场景使⽤0.01

hbase.regionserver.global.memstore.uperLimit 0.35

hbase.hregion.memstore.block.multiplier 8

hbase.server.thread.wakefrequency 10

hbase.regionserver.handler.count 30

hbase.master.distributed.log.spliting false

hbase.regionserver.hlog.splitlog.writer.threads 3

hbase.hregion.max.filesize 1073741824

hbase.hstore.blockingStoreFiles 20

hbase.hregion.memstore.flush.size 13421728

客户端参数：

hbase.client.retries.number 1

hbase.client.pause20

hbase.ipc.client.tcpnodelay true

ipc.ping.interval 3 0

最终随机写的测试性能结果如下（点开可看⼤图）：

从写的测试来看，可以看到，当客户端线程数在250左右时，此时的响应时间在6ms左右，tps在 7.5k左右，差不多是⽐较好的⼀个状态。 在随机写的测试中，以及我们的⼀些项⽬的测试中，看到的⼀些现象和问题：

1、随着单台机器的region数变多了，tps下降的⽐较明显，team的同事做了⼀个改进，保障了 随着region数的增多，tps基本不会有太多的下降，具体请⻅同事的

这篇blog

；

- 2、当hbase.regionserver.handler.count为10（默认为10，更正常了）时，压⼒⼤的情况下差 不多10个线程都会BLOCKED，增加到30后差不多⾜够了，此时tps也到达瓶颈了；

- 3、当datanode数量⽐较少时，会导致写tps⽐较低，原因是此时compact会消耗掉太多的⽹络 IO；

- 4、当写采⽤gz压缩时，会造成堆外内存泄露，具体请参⻅同事的

这篇blog

- 5、在压⼒增⼤、region数增多的情况下，split和flush会对写的平稳性造成⽐较⼤的影响，⽽通 常内存是够⽤的，因此可以调整split file size和memstore flush size，这个要根据场景来决定是 否可调整。


；

对写的速度影响⽐较⼤的因素主要是：请求次数的分布均衡、是否出现Blocking Update或 Delaying flush、HLog数量、DataNode数量、Split File Size。 随机读的测试性能结果如下（点开可看⼤图）：

从读的测试来看，可以看到，读的tps随cache命中率降低会下降的⽐较厉害，命中率为90%时、 客户端线程数为250时，此时的响应时间和tps是⽐较不错的状况。 在随机读的测试中，以及我们的⼀些项⽬的测试中，看到的⼀些现象和问题：

- 1、随机读的tps随着命中率下降，下降的有点太快，具体原因还在查找和分析中；

- 2、当命中率很低时，读bl omfilter的索引信息需要耗费掉⽐较多的时间，主要原因是 bl omfilter的索引信息并没有在cache优先级中占优，这是⼀个可以改进的点。


对读的速度影响⽐较⼤的因素主要是：请求次数的分布均衡、StoreFile数量、Bl omFilter是否打 开、Cache⼤⼩以及命中率。 ps: 强烈推荐 ，其中记录了很多我们对HBase的改进，以及我们在运维HBase项⽬时碰 到的各种奇怪、诡异的问题。

同事的blog

blog.bluedavy.com

来源：

