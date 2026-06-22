HBase性能优化

1、修改Linux配置 Linux系统最⼤可打开⽂件数⼀般默认的参数值是1024，如果你不进⾏修改并发量上来的时候会出现 “To Many Open Files”的错误，导致整个HBase不可运⾏，你可以⽤ulimit -n 命令进⾏修改，或者修 改/etc/security/limits.conf 和/proc/sys/fs/file-max的参数，具体如何修改可以去Gogle 关键字“linux

limits.conf ”

- 2、修改 JVM 配置 修改hbase-env.sh⽂件中的配置参数

HBASE_HEAPSIZE 4 0 #HBase使⽤的 JVM 堆的⼤⼩ HBASE_OPTS "-server -X:+UseConcMarkSwepGC"JVM #GC 选项 HBASE_MANAGES_ZK false #是否使⽤Zokeper进⾏分布式管理

- 3、 修改HBase配置

zokeper.sesion.timeout 默认值：3分钟（18 0ms）

说明：RegionServer与Zokeper间的连接超时时间。当超时时间到后，ReigonServer会被 Zokeper从RS集群清单中移除，HMaster收到移除通知后，会对这台server负责的regions重新 balance，让其他存活的RegionServer接管. 调优： 这个timeout决定了RegionServer是否能够及时的failover。设置成1分钟或更低，可以减少因等待超时 ⽽被延⻓的failover时间。 不过需要注意的是，对于⼀些Online应⽤，RegionServer从宕机到恢复时间本身就很短的（⽹络闪 断，crash等故障，运维可快速介⼊），如果调低timeout时间，反⽽会得不偿失。因为当 ReigonServer被正式从RS集群中移除时，HMaster就开始做balance了（让其他RS根据故障机器记录 的WAL⽇志进⾏恢复）。当故障的RS在⼈⼯介⼊恢复后，这个balance动作是毫⽆意义的，反⽽会使 负载不均匀，给RS带来更多负担。特别是那些固定分配regions的场景。

- 4、修改HBase配置:hbase-site.xml


hbase.regionserver.handler.count

默认值：10 说明：RegionServer的请求处理IO线程数。 调优：

这个参数的调优与内存息息相关。 较少的IO线程，适⽤于处理单次请求内存消耗较⾼的Big PUT场景（⼤容量单次PUT或设置了较⼤ cache的scan，均属于Big PUT）或ReigonServer的内存⽐较紧张的场景。 较多的IO线程，适⽤于单次请求内存消耗低，TPS要求⾮常⾼的场景。设置该值的时候，以监控内存为 主要参考。 这⾥需要注意的是如果server的region数量很少，⼤量的请求都落在⼀个region上，因快速充满 memstore触发flush导致的读写锁会影响全局TPS，不是IO线程数越⾼越好。 压测时，开启Enabling RPC-level loging，可以同时监控每次请求的内存消耗和GC的状况，最后通过 多次压测结果来合理调节IO线程数。

- 5、修改HBase配置


hbase.hregion.max.filesize

默认值：256M 说明：在当前ReigonServer上单个Reigon的最⼤存储空间，单个Region超过该值时，这个Region会被 ⾃动split成更⼩的region。 调优： ⼩region对split和compaction友好，因为拆分region或compact⼩region⾥的storefile速度很快，内存 占⽤低。缺点是split和compaction会很频繁。 特别是数量较多的⼩region不停地split, compaction，会导致集群响应时间波动很⼤，region数量太多 不仅给管理上带来麻烦，甚⾄会引发⼀些Hbase的bug。 ⼀般512以下的都算⼩region。 ⼤region，则不太适合经常split和compaction，因为做⼀次compact和split会产⽣较⻓时间的停顿， 对应⽤的读写性能冲击⾮常⼤。此外，⼤region意味着较⼤的storefile，compaction时对内存也是⼀个 挑战。 当然，⼤region也有其⽤武之地。如果你的应⽤场景中，某个时间点的访问量较低，那么在此时做 compact和split，既能顺利完成split和compaction，⼜能保证绝⼤多数时间 既然split和compaction如此影响性能，有没有办法去掉？ compaction是⽆法避免的，split倒是可以从⾃动调整为⼿动。 只要通过将这个参数值调⼤到某个很难达到的值，⽐如10G，就可以间接禁⽤⾃动split （RegionServer不会对未到达10G的region做split）。 再配合RegionSpliter这个⼯具，在需要split时，⼿动split。 ⼿动split在灵活性和稳定性上⽐起⾃动split要⾼很多，相反，管理成本增加不多，⽐较推荐online实时 系统使⽤。平稳的读写性能。 内存⽅⾯，⼩region在设置memstore的⼤⼩值上⽐较灵活，⼤region则过⼤过⼩都不⾏，过⼤会导致 flush时ap的IO wait增⾼，过⼩则因store file过多影响读性能。

- 6、 修改HBase配置

hbase.regionserver.global.memstore.uperLimit/lowerLimit

默认值：0.4/0.35 uperlimit说明：hbase.hregion.memstore.flush.size 这个参数的作⽤是当单个Region内所有的 memstore⼤⼩总和超过指定值时，flush该region的所有memstore。RegionServer的flush是通过将请 求添加⼀个队列，模拟⽣产消费模式来异步处理的。那这⾥就有⼀个问题，当队列来不及消费，产⽣ ⼤量积压请求时，可能会导致内存陡增，最坏的情况是触发 OM。 这个参数的作⽤是防⽌内存占⽤过⼤，当ReigonServer内所有region的memstores所占⽤内存总和达 到heap的40%时，HBase会强制block所有的更新并flush这些region以释放所有memstore占⽤的内 存。 lowerLimit说明： 同uperLimit，只不过lowerLimit在所有region的memstores所占⽤内存达到Heap的 35%时，不flush所有的memstore。它会找⼀个memstore内存占⽤最⼤的region，做个别flush，此时 写更新还是会被block。lowerLimit算是⼀个在所有region强制flush导致性能降低前的补救措施。在⽇ 志中，表现为“* Flush thread woke up with memory above low water.” 调优：这是⼀个Heap内存保护参数，默认值已经能适⽤⼤多数场景。 参数调整会影响读写，如果写的压⼒⼤导致经常超过这个阀值，则调⼩读缓存hfile.block.cache.size增 ⼤该阀值，或者Heap余量较多时，不修改读缓存⼤⼩。 如果在⾼压情况下，也没超过这个阀值，那么建议你适当调⼩这个阀值再做压测，确保触发次数不要 太多，然后还有较多Heap余量的时候，调⼤hfile.block.cache.size提⾼读性能。 还有⼀种可能性是hbase.hregion.memstore.flush.size保持不变，但RS维护了过多的region，要知 道 region数量直接影响占⽤内存的⼤⼩。

- 7、 修改HBase配置


hfile.block.cache.size

默认值：0.2 说明：storefile的读缓存占⽤Heap的⼤⼩百分⽐，0.2表示20%。该值直接影响数据读的性能。 调优：当然是越⼤越好，如果写⽐读少很多，开到0.4-0.5也没问题。如果读写较均衡，0.3左右。如果 写⽐读多，果断默认吧。设置这个值的时候，你同时要参考 “hbase.regionserver.global.memstore.uperLimit”，该值是memstore占heap的最⼤百分⽐，两个参 数⼀个影响读，⼀个影响写。如果两值加起来超过80-90%，会有 OM的⻛险，谨慎设置。

HBase上Regionserver的内存分为两个部分，⼀部分作为Memstore，主要⽤来写；另外⼀部分作为 BlockCache，主要⽤于读。

写请求会先写⼊Memstore，Regionserver会给每个region提供⼀个Memstore，当Memstore满 64MB以后，会启动 flush刷新到磁盘。当Memstore的总⼤⼩超过限制时 （heapsize * hbase.regionserver.global.memstore.uperLimit * 0.9），会强⾏启动flush进程，从最 ⼤的Memstore开始flush直到低于限制。

读请求先到Memstore中查数据，查不到就到BlockCache中查，再查不到就会到磁盘上读，并把读 的结果放⼊BlockCache。由于BlockCache采⽤的是LRU策略，因此BlockCache达到上限 (heapsize * hfile.block.cache.size * 0.85)后，会启动淘汰机制，淘汰掉最⽼的⼀批数据。

⼀个Regionserver上有⼀个BlockCache和N个Memstore，它们的⼤⼩之和不能⼤于等于heapsize *

0.8，否则HBase不能启动。默认BlockCache为0.2，⽽Memstore为0.4。对于注重读响应时间的系 统，可以将 BlockCache设⼤些，⽐如设置BlockCache=0.4，Memstore=0.39，以加⼤缓存的命中 率。

- 8、 修改HBase配置 hbase.hstore.blockingStoreFiles

默认值：7 说明：在flush时，当⼀个region中的Store（Coulmn Family）内有超过7个storefile时，则block所有的 写请求进⾏compaction，以减少storefile数量。 调优：block写请求会严重影响当前regionServer的响应时间，但过多的storefile也会影响读性能。从 实际应⽤来看，为了获取较平滑的响应时间，可将值设为⽆限⼤。如果能容忍响应时间出现较⼤的波 峰波⾕，那么默认或根据⾃身场景调整即可。

- 9、 修改HBase配置 hbase.hregion.memstore.block.multiplier


默认值：2 说明：当⼀个region⾥的memstore占⽤内存⼤⼩超过hbase.hregion.memstore.flush.size两倍的⼤⼩ 时，block该region的所有请求，进⾏flush，释放内存。 虽然我们设置了region所占⽤的memstores总内存⼤⼩，⽐如64M，但想象⼀下，在最后63.9M的时 候，我Put了⼀个20M的数据，此时memstore的⼤⼩会瞬间暴涨到超过预期的 hbase.hregion.memstore.flush.size的⼏倍。这个参数的作⽤是当memstore的⼤⼩增⾄超过 hbase.hregion.memstore.flush.size 2倍时，block所有请求，遏制⻛险进⼀步扩⼤。 调优： 这个参数的默认值还是⽐较靠谱的。如果你预估你的正常应⽤场景（不包括异常）不会出现突 发写或写的量可控，那么保持默认值即可。如果正常情况下，你的写请求量就会经常暴⻓到正常的⼏ 倍，那么你应该调⼤这个倍数并调整其他参数值，⽐如hfile.block.cache.size和 hbase.regionserver.global.memstore.uperLimit/lowerLimit，以预留更多内存，防⽌ HBase server OM。

- 10、 修改HBase配置


hbase.hregion.memstore.mslab.enabled

默认值：true 说明：减少因内存碎⽚导致的Ful GC，提⾼整体性能。 调优： Arena Alocation，是⼀种GC优化技术，它可以有效地减少因内存碎⽚导致的Ful GC，从⽽提 ⾼系统的整体性能。本⽂介绍Arena Alocation的原理及其在Hbase中的应⽤-MSLAB。 开启MSLAB ：

hbase.hregion.memstore.mslab.enabled=true/ 开启MSALB hbase.hregion.memstore.mslab.chunksize=2m/ chunk的⼤⼩，越⼤内存连续性越好，但内存平

均利⽤率会降低

hbase.hregion.memstore.mslab.max.alocation=256K/ 通过MSLAB分配的对象不能超过256K， 否则直接在Heap上分配，256K够⼤了

1.

