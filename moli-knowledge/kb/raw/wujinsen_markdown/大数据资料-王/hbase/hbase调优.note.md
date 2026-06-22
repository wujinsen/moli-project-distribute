HBase 性能优化

- 1. 修改Linux最⼤⽂件数

- 2. 修改 JVM配置

- 3. 修改HBase配置：hbase-site.xml


Linux系统最⼤可打开⽂件数⼀般默认的参数值是1024，如果你不进⾏修改并发量上来的时候会出现 “To Many Open Files”的错误，导致整个HBase不可运⾏ 查看：ulimit -a 结果：openfiles (-n) 1024 临时修改： ulimit -n 4096 持久修改： vi/etc/security/limits.conf在⽂件最后加上：

- * soft nofile 6535
- * hard nofile 6535
- * soft nproc 6535
- * hard nproc 6535


修改hbase-env.sh⽂件中的配置参数 HBASE_HEAPSIZE 4 0 #HBase使⽤的 JVM 堆的⼤⼩ HBASE_OPTS "-server -X:+UseConcMarkSwepGC"JVM #GC选项 参数解释：

- -client，-server 这两个参数⽤于设置虚拟机使⽤何种运⾏模式，client模式启动⽐较快，但运⾏时性能和内存管理效率 不如server模式，通常⽤于客户端应⽤程序。相反，server模式启动⽐client慢，但可获得更⾼的运⾏ 性能。

- -X:+UseConcMarkSwepGC：设置为并发收集


- 3.1. zokeper.sesion.timeout


默认值：3分钟（18 0ms）,可以改成1分钟说明：RegionServer与Zokeper间的连接超时时 间。当超时时间到后，ReigonServer会被Zokeper从RS集群清单中移除，HMaster收到移除通知 后，会对这台server负责的regions重新balance，让其他存活的RegionServer接管. 调优：这个timeout决定了RegionServer是否能够及时的failover。设置成1分钟或更低，可以减少因等 待超时⽽被延⻓的failover时间。不过需要注意的是，对于⼀些Online应⽤，RegionServer从宕机到恢 复时间本身就很短的（⽹络闪断，crash等故障，运维可快速介⼊），如果调低timeout时间，反⽽会 得不偿失。因为当ReigonServer被正式从RS集群中移除时，HMaster就开始做balance了（让其他RS 根据故障机器记录的WAL⽇志进⾏恢复）。当故障的RS在⼈⼯介⼊恢复后，这个balance动作是毫⽆ 意义的，反⽽会使负载不均匀，给RS带来更多负担。特别是那些固定分配regions的场景。

- 3.2. hbase.regionserver.handler.count
- 3.3. hbase.hregion.max.filesize


默认值：10说明：RegionServer的请求处理IO线程数。调优：这个参数的调优与内存息息相关。较 少的IO线程，适⽤于处理单次请求内存消耗较⾼的Big PUT场景（⼤容量单次PUT或设置了较⼤cache 的scan，均属于Big PUT）或ReigonServer的内存⽐较紧张的场景。较多的IO线程，适⽤于单次请求 内存消耗低，TPS（吞吐量）要求⾮常⾼的场景。设置该值的时候，以监控内存为主要参考。这⾥需要 注意的是如果server的region数量很少，⼤量的请求都落在⼀个region上，因快速充满memstore触发 flush导致的读写锁会影响全局TPS，不是IO线程数越⾼越好。压测时，开启Enabling RPC-level loging，可以同时监控每次请求的内存消耗和GC的状况，最后通过多次压测结果来合理调节IO线程 数。

默认值：256M说明：在当前ReigonServer上单个Reigon的最⼤存储空间，单个Region超过该值时， 这个Region会被⾃动split成更⼩的region。调优：⼩region对split和compaction友好，因为拆分region 或compact⼩region⾥的storefile速度很快，内存占⽤低。缺点是split和compaction会很频繁。特别是 数量较多的⼩region不停地split,compaction，会导致集群响应时间波动很⼤，region数量太多不仅给 管理上带来麻烦，甚⾄会引发⼀些Hbase的bug。⼀般512以下的都算⼩region。⼤region，则不太适 合经常split和compaction，因为做⼀次compact和split会产⽣较⻓时间的停顿，对应⽤的读写性能冲 击⾮常⼤。此外，⼤region意味着较⼤的storefile，compaction时对内存也是⼀个挑战。当然，⼤ region也有其⽤武之地。如果你的应⽤场景中，某个时间点的访问量较低，那么在此时做compact和 split，既能顺利完成split和compaction，⼜能保证绝⼤多数时间既然split和compaction如此影响性 能，有没有办法去掉？compaction是⽆法避免的，split倒是可以从⾃动调整为⼿动。只要通过将这个 参数值调⼤到某个很难达到的值，⽐如10G，就可以间接禁⽤⾃动split（RegionServer不会对未到达 10G的region做split）。再配合RegionSpliter这个⼯具，在需要split时，⼿动split。⼿动split在灵活 性和稳定性上⽐起⾃动split要⾼很多，相反，管理成本增加不多，⽐较推荐online实时系统使⽤。平稳 的读写性能。内存⽅⾯，⼩region在设置memstore的⼤⼩值上⽐较灵活，⼤region则过⼤过⼩都不 ⾏，过⼤会导致flush时ap的IO wait增⾼，过⼩则因storefile过多影响读性能。

- 3.4. hbase.regionserver.global.memstore.uperLimit/lowerLimit
- 3.5. hfile.block.cache.size


默认值：0.4/0.35uperlimit说明：hbase.hregion.memstore.flush.size 这个参数的作⽤是当单个 Region内所有的memstore⼤⼩总和超过指定值时，flush该region的所有memstore。RegionServer的 flush是通过将请求添加⼀个队列，模拟⽣产消费模式来异步处理的。那这⾥就有⼀个问题，当队列来 不及消费，产⽣⼤量积压请求时，可能会导致内存陡增，最坏的情况是触发 OM。这个参数的作⽤是 防⽌内存占⽤过⼤，当ReigonServer内所有region的memstores所占⽤内存总和达到heap的40%时， HBase会强制block所有的更新并flush这些region以释放所有memstore占⽤的内存。lowerLimit说明： 同uperLimit，只不过lowerLimit在所有region的memstores所占⽤内存达到Heap的35%时，不flush 所有的memstore。它会找⼀个memstore内存占⽤最⼤的region，做个别flush，此时写更新还是会被 block。lowerLimit算是⼀个在所有region强制flush导致性能降低前的补救措施。在⽇志中，表现为 “* Flush thread woke up with memory above low water.”调优：这是⼀个Heap内存保护参数，默认 值已经能适⽤⼤多数场景。参数调整会影响读写，如果写的压⼒⼤导致经常超过这个阀值，则调⼩读 缓存hfile.block.cache.size增⼤该阀值，或者Heap余量较多时，不修改读缓存⼤⼩。如果在⾼压情况 下，也没超过这个阀值，那么建议你适当调⼩这个阀值再做压测，确保触发次数不要太多，然后还有 较多Heap余量的时候，调⼤hfile.block.cache.size提⾼读性能。还有⼀种可能性是 hbase.hregion.memstore.flush.size保持不变，但RS维护了过多的region，要知道 region数量直接影 响占⽤内存的⼤⼩。

默认值：0.2说明：storefile的读缓存占⽤Heap的⼤⼩百分⽐，0.2表示20%。该值直接影响数据读的 性能。调优：当然是越⼤越好，如果写⽐读少很多，开到0.4-0.5也没问题。如果读写较均衡，0.3左 右。如果写⽐读多，果断默认吧。设置这个值的时候，你同时要参考 “hbase.regionserver.global.memstore.uperLimit”，该值是memstore占heap的最⼤百分⽐，两个参 数⼀个影响读，⼀个影响写。如果两值加起来超过80-90%，会有 OM的⻛险，谨慎设置。 HBase 上Regionserver的内存分为两个部分，⼀部分作为Memstore，主要⽤来写；另外⼀部分作为 BlockCache，主要⽤于读。 写请求会先写⼊Memstore，Regionserver会给每个region提供⼀个 Memstore，当Memstore满64MB以后，会启动 flush刷新到磁盘。当Memstore的总⼤⼩超过限制时 （heapsize * hbase.regionserver.global.memstore.uperLimit *0.9），会强⾏启动flush进程，从最 ⼤的Memstore开始flush直到低于限制。 读请求先到Memstore中查数据，查不到就到BlockCache中 查，再查不到就会到磁盘上读，并把读的结果放⼊BlockCache。由于BlockCache采⽤的是LRU策略， 因此BlockCache达到上限(heapsize * hfile.block.cache.size * 0.85)后，会启动淘汰机制，淘汰掉最⽼ 的⼀批数据。 ⼀个Regionserver上有⼀个BlockCache和N个Memstore，它们的⼤⼩之和不能⼤于等 于heapsize * 0.8，否则HBase不能启动。默认BlockCache为0.2，⽽Memstore为0.4。对于注重读响 应时间的系统，可以将 BlockCache设⼤些，⽐如设置BlockCache=0.4，Memstore=0.39，以加⼤缓 存的命中率。

- 3.6. hbase.hstore.blockingStoreFiles
- 3.7. hbase.hregion.memstore.block.multiplier


默认值：7说明：在flush时，当⼀个region中的Store（Coulmn Family）内有超过7个storefile时，则 block所有的写请求进⾏compaction，以减少storefile数量。调优：block写请求会严重影响当前 regionServer的响应时间，但过多的storefile也会影响读性能。从实际应⽤来看，为了获取较平滑的响 应时间，可将值设为⽆限⼤。如果能容忍响应时间出现较⼤的波峰波⾕，那么默认或根据⾃身场景调 整即可。

默认值：2说明：当⼀个region⾥的memstore占⽤内存⼤⼩超过hbase.hregion.memstore.flush.size两 倍的⼤⼩时，block该region的所有请求，进⾏flush，释放内存。虽然我们设置了region所占⽤的 memstores总内存⼤⼩，⽐如64M，但想象⼀下，在最后63.9M的时候，我Put了⼀个20M的数据， 此时memstore的⼤⼩会瞬间暴涨到超过预期的hbase.hregion.memstore.flush.size的⼏倍。这个参数 的作⽤是当memstore的⼤⼩增⾄超过hbase.hregion.memstore.flush.size 2倍时，block所有请求，遏 制⻛险进⼀步扩⼤。调优： 这个参数的默认值还是⽐较靠谱的。如果你预估你的正常应⽤场景（不包 括异常）不会出现突发写或写的量可控，那么保持默认值即可。如果正常情况下，你的写请求量就会 经常暴⻓到正常的⼏倍，那么你应该调⼤这个倍数并调整其他参数值，⽐如hfile.block.cache.size和 hbase.regionserver.global.memstore.uperLimit/lowerLimit，以预留更多内存，防⽌ HBase server OM。

# 3.8. hbase.hregion.memstore.mslab.enabled

默认值：true说明：减少因内存碎⽚导致的Ful GC，提⾼整体性能。调优： Arena Alocation，是⼀种 GC优化技术，它可以有效地减少因内存碎⽚导致的Ful GC，从⽽提⾼系统的整体性能。本⽂介绍 Arena Alocation的原理及其在Hbase中的应⽤-MSLAB。开启MSLAB ： hbase.hregion.memstore.mslab.enabled=true/ 开启 MSALB hbase.hregion.memstore.mslab.chunksize=2m/chunk的⼤⼩，越⼤内存连续性越好，但 内存平均利⽤率会降低 hbase.hregion.memstore.mslab.max.alocation=256K/ 通过MSLAB分配的 对象不能超过256K，否则直接在Heap上分配，256K够⼤了

## 其他

启⽤LZO压缩 LZO对⽐Hbase默认的GZip，前者性能较⾼，后者压缩⽐较⾼，具体参⻅ Using LZO Compresion 。 对于想提⾼HBase读写性能的开发者，采⽤LZO是⽐较好的选择。对于⾮常在乎存储空间的开发者，则 建议保持默认。 不要在⼀张表⾥定义太多的Column Family Hbase⽬前不能良好的处理超过包含2-3个CF的表。因为某个CF在flush发⽣时，它邻近的CF也会因关 联效应被触发flush，最终导致系统产⽣更多IO。 批量导⼊ 在批量导⼊数据到Hbase前，你可以通过预先创建regions，来平衡数据的负载。详⻅

Table Creation: Pre-Creating Regions

避免CMS concurent mode failure HBase使⽤CMS GC。默认触发GC的时机是当年⽼代内存达到90%的时候，这个百分⽐由 -

X CMSInitiatingOcupancyFraction=N 这个参数来设置。concurent mode failed发⽣在这样⼀个场 景： 当年⽼代内存达到90%的时候，CMS开始进⾏并发垃圾收集，于此同时，新⽣代还在迅速不断地晋升 对象到年⽼代。当年⽼代CMS还未完成并发标记时，年⽼代满了，悲剧就发⽣了。CMS因为没内存可 ⽤不得不暂停mark，并触发⼀次stop the world（挂起所有jvm线程），然后采⽤单线程拷⻉⽅式清理 所有垃圾对象。这个过程会⾮常漫⻓。为了避免出现concurent mode failed，建议让GC在未到90% 时，就触发。 通过设置 -X CMSInitiatingOcupancyFraction=N

这个百分⽐， 可以简单的这么计算。如果你的 hfile.block.cache.size 和 hbase.regionserver.global.memstore.uperLimit 加起来有60%（默认），那么你可以设置 7080，⼀般⾼10%左右差不多。

## Hbase客户端优化

AutoFlush 将 的setAutoFlush设为false，可以⽀持客户端批量更新。即当Put填满客户端flush缓存时，才 发送到服务端。 默认是true。 Scan Caching scaner⼀次缓存多少数据来scan（从服务端⼀次抓多少数据回来scan）。 默认值是 1，⼀次只取⼀条。 Scan Atribute Selection scan时建议指定需要的Column Family，减少通信量，否则scan操作默认会返回整个row的所有数据 （所有Coulmn Family）。 Close ResultScaners 通过scan取完数据后，记得要关闭ResultScaner，否则RegionServer可能会出现问题（对应的Server 资源⽆法释放）。 Optimal Loading of Row Keys 当你scan⼀张表的时候，返回结果只需要row key（不需要CF, qualifier,values,timestaps）时，你可以 在scan实例中添加⼀个filterList，并设置 MUST_PAS_AL操作，filterList中ad 或

HTable

FirstKeyOnlyFilter KeyOnlyFilter

。这样可以减少⽹络通信量。 Turn of WAL on Puts 当Put某些⾮重要数据时，你可以设置writeToWAL(false)，来进⼀步提⾼写性能。writeToWAL(false)会 在Put时放弃写WAL log。⻛险是，当RegionServer宕机时，可能你刚才Put的那些数据会丢失，且⽆法 恢复。 启⽤Bl om Filter

Bl om Filter

通过空间换时间，提⾼读操作性能。 转载请注明原⽂链接： 感谢 同学对”hbase.hregion.memstore.flush.size”和“hbase.hstore.blockingStoreFiles”错误观 点的修正。 转⾃

htp:/kenwublog.com/hbase-performance-tuning 嬴北望

htp:/rdc.taobao.com/team/jm/archives/975

