⼀个Hive查询⽣成多个map reduce job，⼀个map reduce job⼜有map，reduce，spil，shufle， sort等多个阶段，所以针对hive查询的优化可以⼤致分为针对M/R中单个步骤的优化，针对M/R全局的 优化，和针对整个查询（多M/R job）的优化，下⽂会分别阐述。 在开始之前，先把MR的流程图帖出来（摘⾃Hadop权威指南），⽅便后⾯对照。另外要说明的是， 这个优化只是针对Hive 0.9版本。由于Hortonwork发起了Stinger项⽬，Hive后续版本应该能更加快速 的响应查询。⽬前已经发布的Hive 0.1就有不少新feature，⽐如针对数据仓库中常⽤的星型模型的优 化等等，这些就不在本⽂的讨论范围之内了。

![image 1](<数据仓库中的SQL性能优化（Hive篇）.note_images/imageFile1.png>)

Map阶段的优化 Map阶段的优化，主要是确定合适的map数。那么⾸先要了解map数的计算公式，即：

- 1 num_map_tasks = max[${mapred.min.split.size},

- 2 min(${dfs.block.size}, ${mapred.max.split.size})]


其中mapred.min.split.size指的是数据的最⼩分割单元⼤⼩；mapred.max.split.size指的是数据的最⼤ 分割单元⼤⼩；dfs.block.size指的是HDFS设置的数据块⼤⼩。 ⼀般来说dfs.block.size这个值是⼀个已经指定好的值，⽽且这个参数默认情况下hive是识别不到的 （除⾮在hive-site.xml中明确指定），即：

- 1 hive> set dfs.block.size;

- 2 dfs.block.size is undefined


所以默认情况下只有mapred.min.split.size和mapred.max.split.size这两个参数（本节内容后⾯就以 min和max指代这两个参数）来决定map数量。 在hive中min的默认值是1B，max的默认值是256MB，即：

- 1 hive> set mapred.min.split.size;

- 2 mapred.min.split.size=1

- 3 hive> set mapred.max.split.size;

- 4 mapred.max.split.size=256000000


所以如果不做修改的话，就是1个map task处理256MB数据，我们就以调整max为主。通过调整max可 以起到调整map数的作⽤，减⼩max可以增加map数，增⼤max可以减少map数。需要提醒的是，直接 调整mapred.map.tasks这个参数是没有效果的。 调整⼤⼩的时机根据查询的不同⽽不同，总的来讲可以通过观察map task的完成时间来确定是否需要 增加map资源。如果map task的完成时间都是接近1分钟，甚⾄⼏分钟了，那么往往增加map数量，使 得每个map task处理的数据量减少，能够让map task更快完成；⽽如果map task的运⾏时间已经很少 了，⽐如10-20秒，这个时候增加map不太可能让map task更快完成，反⽽可能因为map需要的初始化 时间反⽽让job总体速度变慢，这个时候反⽽需要考虑是否可以把map的数量减少，这样可以节省更多 资源给其他Job。 Reduce阶段的优化 这⾥说的reduce阶段，是指前⾯流程图中的reduce phase（实际的reduce计算）⽽⾮图中整个reduce task。Reduce阶段优化的主要⼯作也是选择合适的reduce task数量，跟上⾯的map优化类似。 与map优化不同的是，reduce优化时，可以直接设置mapred.reduce.tasks参数从⽽直接指定reduce的 个数。当然直接指定reduce个数虽然⽐较⽅便，但是不利于⾃动扩展。Reduce数的设置虽然相较map 更灵活，但是也需要像map⼀样设定⼀个⾃动⽣成规则，这样运⾏定时job的时候就不⽤担⼼原来设置 的固定reduce数会由于数据量的变化⽽不合适。 Hive估算reduce数量的时候，使⽤的是下⾯的公式：

- 1 num_reduce_tasks = min(${hive.exec.reducers.max},

- 2 ${input.size} / ${ hive.exec.reducers.bytes.per.reducer})


也就是说，根据输⼊的数据量⼤⼩来决定reduce的个数，默认hive.exec.reducers. bytes.per.reducer 为1G，⽽且reduce个数不能超过⼀个上限参数值，这个参数的默认取值为 9。所以我们以调整 hive.exec.reducers.bytes.per.reducer为主来设置reduce个数。 设置reduce数同样也是根据运⾏时间作为参考调整，并且可以根据特定的业务需求、⼯作负载类型总 结出经验，所以不再赘述。 Map与Reduce之间的优化 所谓map和reduce之间，主要有3道⼯序。⾸先要把map输出的结果进⾏排序后做成中间⽂件，其次这 个中间⽂件就能分发到各个reduce，最后reduce端在执⾏reduce phase之前把收集到的排序⼦⽂件合 并成⼀个排序⽂件。

第⼀个阶段中，由于内存不够，数据可能没办法在内存中⼀次性排序完成，那么就只能把局部排序的 ⽂件先保存到磁盘上，这个动作叫spil，然后spil出来的多个⽂件可以在最后进⾏merge。如果发⽣ spil，可以通过设置io.sort.mb来增⼤maper输出bufer的⼤⼩，避免spil的发⽣。另外合并时可以通 过设置io.sort.factor来使得⼀次性能够合并更多的数据。调试参数的时候，⼀个要看spil的时间成本， ⼀个要看merge的时间成本，还需要注意不要撑爆内存（io.sort.mb是算在map的内存⾥⾯的）。 Reduce端的merge也是⼀样可以⽤io.sort.factor。⼀般情况下这两个参数很少需要调整，除⾮很明确 知道这个地⽅是瓶颈。 关于⽂件从map端copy到reduce端，默认情况下在5%的map完成的情况下reduce就开始启动copy， 这个有时候是很浪费资源的，因为reduce⼀旦启动就被占⽤，⼀直等到map全部完成，收集到所有数 据才可以进⾏后⾯的动作，所以我们可以等⽐较多的map完成之后再启动reduce流程，这个⽐例可以 通过mapred.reduce.slowstart. completed.maps去调整，他的默认值就是5%。如果觉得这么做会减 慢reduce端copy的进度，可以把copy过程的线程增⼤。tasktracker.htp.threads可以决定作为server 端的map⽤于提供数据传输服务的线程，mapred.reduce.paralel.copies可以决定作为client端的 reduce同时从map端拉取数据的并⾏度（⼀次同时从多少个map拉数据），修改参数的时候这两个注 意协调⼀下，server端能处理client端的请求即可。 ⽂件格式的优化 ⽂件格式⽅⾯有两个问题，⼀个是给输⼊和输出选择合适的⽂件格式，另⼀个则是⼩⽂件问题。⼩⽂ 件问题在⽬前的hive环境下已经得到了⽐较好的解决，hive的默认配置中就可以在⼩⽂件输⼊时⾃动把 多个⽂件合并给1个map处理（当然，如果能直接读取⼤⽂件更好），输出时如果⽂件很⼩也会进⾏⼀ 轮单独的合并，所以这⾥就不专⻔讨论了。相关的参数可以在 找到。 关于⽂件格式，Hive中⽬前主要是3种，textfile，sequencefile和rcfile。总体上来说，rcfile的压缩⽐ 例和查询时间稍好⼀点，所以推荐使⽤。 关于使⽤⽅法，在建表结构时可以指定格式，然后指定压缩插⼊：

这⾥

- 1 create table rc_file_test( col int ) stored as rcfile;

- 2 set hive.exec.compress.output = true;

- 3 insert overwrite table rc_file_test

- 4 select * from source_table;


另外create table as select时也可以指定输出格式，这个时候就要通过hive.default. fileformat来设定：

- 1 set hive.default.fileformat = SequenceFile;

- 2 set hive.exec.compress.output = true;

set mapred.output.compression.type = BLOCK; /*对于sequence file，压缩⽅式有record和 block两种可选择，block压缩⽐更⾼*/

- 3

- 4 insert overwrite table seq_file_test

- 5 select * from source_table;


最后要说的是，sequencefile和rcfile都是不⽀持空表要导⼊本地数据的，但是textfile格式的表可以⽀ 持⽂本在本地压缩完成之后直接以压缩格式导⼊，具体的做法可以看 的详细介绍。 Job整体优化 有⼀些问题必须从job的整体⻆度去观察。这⾥讨论⼏个问题：Job执⾏模式（本地执⾏v.s.分布式执 ⾏）、索引、Join算法、以及数据倾斜。 Job执⾏模式 Hadop的map reduce job可以有3种模式执⾏，即本地模式，伪分布式，还有真正的分布式。本地模 式和伪分布式都是在最初学习hadop的时候往往被说成是做单机开发的时候⽤到。但是实际上对于处 理数据量⾮常⼩的job，直接启动分布式job会消耗⼤量资源，⽽真正执⾏计算的时间反⽽⾮常少。这个 时候就应该使⽤本地模式执⾏mr job，这样执⾏的时候不会启动分布式job，执⾏速度就会快很多。⽐ 如⼀般来说启动分布式job，⽆论多⼩的数据量，执⾏时间⼀般不会少于20s，⽽使⽤本地mr模式，10 秒左右就能出结果。 设置执⾏模式的主要参数有三个，⼀个是hive.exec.mode.local.auto，把他设为true就能够⾃动开启 local mr模式。但是这还不⾜以启动local mr，输⼊的⽂件数量和数据量⼤⼩必须要控制，这两个参数 分别为hive.exec.mode.local.auto.tasks.max和hive.exec.mode.local.auto.inputbytes.max，默认值分 别为4和128MB，即默认情况下，map处理的⽂件数不超过4个并且总⼤⼩⼩于128MB就启⽤local mr 模式。 索引 总体上来说，hive的索引⽬前还是⼀个不太适合使⽤的东⻄，这⾥只是考虑到叙述完整性，对其进⾏基 本的介绍。 Hive中的索引架构开放了⼀个接⼝，允许你根据这个接⼝去实现⾃⼰的索引。⽬前hive⾃⼰有⼀个参考 的索引实现（CompactIndex），后来在0.8版本中⼜加⼊位图索引。这⾥就讲讲CompactIndex。 CompactIndex的实现原理类似⼀个l okup table，⽽⾮传统数据库中的B树。如果你对table A的col1做 了索引，索引⽂件本身就是⼀个table，这个table会有3列，分别是col1的枚举值，每个值对应的数据 ⽂件位置，以及在这个⽂件位置中的偏移量。通过这种⽅式，可以减少你查询的数据量（偏移量可以 告诉你从哪个位置开始找，⾃然只需要定位到相应的block），起到减少资源消耗的作⽤。但是就其性 能来说，并没有很⼤的改善，很可能还不如构建索引需要花的时间。所以在集群资源充⾜的情况下， 没有太⼤必要考虑索引。

这⾥

CompactIndex的还有⼀个缺点就是使⽤起来不友好，索引建完之后，使⽤之前还需要根据查询条件做 ⼀个同样剪裁才能使⽤，索引的内部结构完全暴露，⽽且还要花费额外的时间。具体看看下⾯的使⽤ ⽅法就了解了：

- 1 /*在index_test_table表的id字段上创建索引*/

- 2 create index idx on table index_test_table(id)

- 3 as 'org.apache.hadoop.hive.ql.index.compact.CompactIndexHandler'

- 4 with deferred rebuild;

- 5 alter index idx on index_test_table rebuild;

- 6

/*索引的剪裁。找到上⾯建的索引表，根据你最终要⽤的查询条件剪裁⼀下。如果你想跟RDBMS⼀样建完索 引就⽤，那是不⾏的，会直接报错，这也是其麻烦的地⽅。*/

- 7

- 8 create table my_index

- 9 as select `_bucketname`, `_offsets`

- 10 from default__index_test_table_idx__ where id = 10;

- 11

- 12 /*现在可以⽤索引了，注意最终查询条件跟上⾯的剪裁条件⼀致*/

- 13 set hive.index.compact.file = /user/hive/warehouse/my_index;

set hive.input.format = org.apache.hadoop.hive.ql.index.compact.HiveCompactIndexInputFormat;

- 14

- 15 select count(*) from index_test_table where id = 10;


Join算法 处理分布式join，⼀般有两种⽅法。⼀种是replication join：把其中⼀个表复制到所有节点，这样另⼀ 个表在每个节点上⾯的分⽚就可以跟这个完整的表join了；另⼀种⽅法是repartition join：把两份数据 按照join key进⾏hash重分布，让每个节点处理hash值相同的join key数据，也就是做局部的join。这两 种⽅式在M/R Job中分别对应了map side join和reduce side join。在⼀些MP DB中，数据可以按照某 列字段预先进⾏hash分布，这样在跟这个表以这个字段为join key进⾏join的时候，该表肯定不需要做 数据重分布了，这种功能是以HDFS作为底层⽂件系统的hive所没有的。 在默认情况下，hive的join策略是进⾏reduce side join。当两个表中有⼀个是⼩表的时候，就可以考虑 ⽤map join了，因为⼩表复制的代价会好过⼤表shufle的代价。使⽤map join的配置⽅法有两种，⼀种 直接在sql中写hint，语法是/*+MAPJOIN (tbl)*/，其中tbl就是你想要做replication的表。另⼀种⽅法是 设置hive.auto.convert.join = true，这样hive会⾃动判断当前的join操作是否合适做map join，主要是 找join的两个表中有没有⼩表。⾄于多⼤的表算⼩表，则是由hive.smaltable.filesize决定，默认 25MB。

但是有的时候，没有⼀个表⾜够⼩到能够放进内存，但是还是想⽤map join怎么办？这个时候就要⽤到 bucket map join。其⽅法是两个join表在join key上都做hash bucket，并且把你打算复制的那个（相 对）⼩表的bucket数设置为⼤表的倍数。这样数据就会按照join key做hash bucket。⼩表依然复制到 所有节点，map join的时候，⼩表的每⼀组bucket加载成hashtable，与对应的⼀个⼤表bucket做局部 join，这样每次只需要加载部分hashtable就可以了。 然后在两个表的join key都具有唯⼀性的时候（也就是可做主键），还可以进⼀步做sort merge bucket map join。做法还是两边要做hash bucket，⽽且每个bucket内部要进⾏排序。这样⼀来当两边bucket 要做局部join的时候，只需要⽤类似merge sort算法中的merge操作⼀样把两个bucket顺序遍历⼀遍即 可完成，这样甚⾄都不⽤把⼀个bucket完整的加载成hashtable，这对性能的提升会有很⼤帮助。 然后这⾥以⼀个完整的实验说明这⼏种join算法如何操作。 ⾸先建表要带上bucket：

- 1 create table map_join_test(id int)

- 2 clustered by (id) sorted by (id) into 32 buckets

- 3 stored as textfile;


然后插⼊我们准备好的80万⾏数据，注意要强制划分成bucket（也就是⽤reduce划分hash值相同的 数据到相同的⽂件）：

- 1 set hive.enforce.bucketing = true;

- 2 insert overwrite table map_join_test

- 3 select * from map_join_source_data;


这样这个表就有了80万id值（且⾥⾯没有重复值，所以可以做sort merge），占⽤80MB左右。 接下来我们就可以⼀⼀尝试map join的算法了。⾸先是普通的map join：

- 1 select /*+mapjoin(a) */count(*)

- 2 from map_join_test a

- 3 join map_join_test b on a.id = b.id;


然后就会看到分发hash table的过程：

2013-08-31 09:08:43 Starting to launch local task to process map join; maximum memory = 1004929024

- 1

2013-08-31 09:08:45 Processing rows: 200000 Hashtable size: 199999 Memory usage: 38823016 rate: 0.039

- 2

2013-08-31 09:08:46 Processing rows: 300000 Hashtable size: 299999 Memory usage: 56166968 rate: 0.056

- 3

- 4 ……

- 5 ……

- 6 ……

2013-08-31 09:12:39 Processing rows: 4900000 Hashtable size: 4899999 Memory usage: 896968104 rate: 0.893

- 7

2013-08-31 09:12:47 Processing rows: 5000000 Hashtable size: 4999999 Memory usage: 922733048 rate: 0.918

- 8

- 9 Execution failed with exit status: 2

- 10 Obtaining error information

- 11

- 12 Task failed!

- 13 Task ID:

- 14 Stage-4


不幸的是，居然内存不够了，直接做map join失败了。但是80MB的⼤⼩为何⽤1G的heap size都放不 下？观察整个过程就会发现，平均⼀条记录需要⽤到20字节的存储空间，这个overhead太⼤了。不 过这⾥我也搞不清楚hive为什么需要这么⼤空间，是否可以修改，总之对于map join的⼩表size⼀定要 好好评估，如果有⼏⼗万记录数就要⼩⼼了。 所以接下来我们就⽤bucket map join，之前分的bucket就派上⽤处了。只需要在上述sql的前⾯加上如 下的设置：

1 set hive.optimize.bucketmapjoin = true;

然后还是会看到hash table分发：

2013-08-31 09:20:39 Starting to launch local task to process map join; maximum memory = 1004929024

- 1

2013-08-31 09:20:41 Processing rows: 200000 Hashtable size: 199999 Memory usage: 38844832 rate: 0.039

- 2

2013-08-31 09:20:42 Processing rows: 275567 Hashtable size: 275567 Memory usage: 51873632 rate: 0.052

- 3

2013-08-31 09:20:42 Dump the hashtable into file: file:/tmp/hadoop/hive_2013-08-31_21-20-37_444_1135806892100127714/-local10003/HashTable-Stage-1/MapJoin-a-10-000000_0.hashtable

- 4

2013-08-31 09:20:46 Upload 1 File to: file:/tmp/hadoop/hive_2013-08-31_2120-37_444_1135806892100127714/-local-10003/HashTable-Stage-1/MapJoin-a-10000000_0.hashtable File size: 11022975

- 5

2013-08-31 09:20:47 Processing rows: 300000 Hashtable size: 24432 Memory usage: 8470976 rate: 0.008

- 6

2013-08-31 09:20:47 Processing rows: 400000 Hashtable size: 124432 Memory usage: 25368080 rate: 0.025

- 7

2013-08-31 09:20:48 Processing rows: 500000 Hashtable size: 224432 Memory usage: 42968080 rate: 0.043

- 8

2013-08-31 09:20:49 Processing rows: 551527 Hashtable size: 275960 Memory usage: 52022488 rate: 0.052

- 9

2013-08-31 09:20:49 Dump the hashtable into file: file:/tmp/hadoop/hive_2013-08-31_21-20-37_444_1135806892100127714/-local10003/HashTable-Stage-1/MapJoin-a-10-000001_0.hashtable

- 10

- 11 ……


这次就会看到每次构建完⼀个hash table（也就是所对应的对应⼀个bucket），会把这个hash table写 ⼊⽂件，重新构建新的hash table。这样⼀来由于每个hash table的量⽐较⼩，也就不会有内存不⾜的 问题，整个sql也能成功运⾏。不过光光是这个复制动作就要花去3分半的时间，所以如果整个job本来 就花不了多少时间的，那这个时间就不可⼩视。 最后我们试试sort merge bucket map join，在bucket map join的基础上加上下⾯的设置即可：

- 1 set hive.optimize.bucketmapjoin.sortedmerge = true;

- 2 set hive.input.format = org.apache.hadoop.hive.ql.io.BucketizedHiveInputFormat;


sort merge bucket map join是不会产⽣hash table复制的步骤的，直接开始做实际map端join操作了， 数据在join的时候边做边读。跳过复制的步骤，外加join算法的改进，使得sort merge bucket map join 的效率要明显好于bucket map join。

关于join的算法虽然有这么些选择，但是个⼈觉得，对于⽇常使⽤，掌握默认的reduce join和普通的 （⽆bucket）map join已经能解决⼤多数问题。如果⼩表不能完全放内存，但是⼩表相对⼤表的size量 级差别也⾮常⼤的时候也可以试试bucket map join，不过其hash table分发的过程会浪费不少时间，需 要评估下是否能够⽐reduce join更⾼效。⽽sort merge bucket map join虽然性能不错，但是把数据做 成bucket本身也需要时间，另外其发动条件⽐较特殊，就是两边join key必须都唯⼀（很多介绍资料中 都不提这⼀点。强调下必须都是唯⼀，哪怕只有⼀个表不唯⼀，出来的结果也是错的）。这样的场景 相对⽐较少⻅，“⽤户基本表 join ⽤户扩展表”以及“⽤户今天的数据快照 join ⽤户昨天的数据快照”这 类场景可能⽐较合适。 数据倾斜 所谓数据倾斜，说的是由于数据分布不均匀，个别值集中占据⼤部分数据量，加上hadop的计算模 式，导致计算资源不均匀引起性能下降。 还是拿博客⽹站的访问⽇志说事吧。假设⽹站访问⽇志中会记录⽤户的user_id，并且对于注册⽤户使 ⽤其⽤户表的user_id，对于⾮注册⽤户使⽤⼀个user_id=0代表。那么鉴于⼤多数⽤户是⾮注册⽤户 （只看不写），所以user_id=0占据了绝⼤多数。⽽如果进⾏计算的时候如果以user_id作为group by 的维度或者是join key，那么个别reduce会收到⽐其他reduce多得多的数据⸺因为它要接收所有 user_id=0的记录进⾏处理，使得其处理效果会⾮常差，其他reduce都跑完很久了它还在运⾏。 group by造成的倾斜和join造成的倾斜需要分开看。group by造成的倾斜有两个参数可以解决，⼀个是 hive.map.agr，默认值已经为true，意思是会做map端的combiner。所以如果你的group by查询只是 做count(*)的话，其实是看不出倾斜效果的，但是如果你做的是count(distinct)，那么还是会看出⼀点 倾斜效果。另⼀个参数是hive.groupby.skewindata。这个参数的意思是做reduce操作的时候，拿到的 key并不是所有相同值给同⼀个reduce，⽽是随机分发，然后reduce做聚合，做完之后再做⼀轮MR， 拿前⾯聚合过的数据再算结果。所以这个参数其实跟hive.map.agr做的是类似的事情，只是拿到 reduce端来做，⽽且要额外启动⼀轮job，所以其实不怎么推荐⽤，效果不明显。 join造成的倾斜就⽐如上⾯描述的⽹站访问⽇志和⽤户表两个表join：

1 select a.* from logs a join users b on a.user_id = b.user_id;

hive给出的解决⽅案是，把这种user_id = 0的特殊值先不在reduce端计算掉，⽽是先写⼊hdfs，然后 启动⼀轮map join专⻔做这个特殊值的计算，期望能提⾼计算这部分值的处理速度。当然你要告诉hive 这个join是个skew join，即set hive.optimize.skewjoin = true;还有要告诉hive如何判断特殊值，根据 hive.skewjoin.key设置的数量hive可以知道，⽐如默认值是1 0，那么超过1 0条记录的值就是 特殊值。 另外对于特殊值的处理往往跟业务有关系，所以也可以从业务⻆度重写sql解决。⽐如前⾯这种倾斜 join，可以把特殊值隔离开来（从业务⻆度说，users表应该不存在user_id = 0的情况，但是这⾥还是 假设有这个值，使得这个写法更加具有通⽤性）：

- 1 select a.* from

- 2 (

- 3 select a.*

- 4 from (select * from logs where user_id = 0) a

- 5 join (select * from users where user_id = 0) b

- 6 on a.user_id = b.user_id

- 7 union all

- 8 select a.*

- 9 from logs a join users b

- 10 on a.user_id <> 0 and a.user_id = b.user_id

- 11 )t;


SQL整体优化 前⾯对于单个job如何做优化已经做过详细讨论，但是hive查询会⽣成多个job，针对多个job，有什么 地⽅需要优化？ ⾸先，在hive⽣成的多个job中，在有些情况下job之间是可以并⾏的，典型的就是⼦查询。当需要执⾏ 多个⼦查询union al或者join操作的时候，job间并⾏就可以使⽤了。⽐如下⾯的代码就是⼀个可以并⾏ 的场景示意：

- 1 select * from

- 2 (

- 3 select count(*) from logs

- 4 where log_date = 20130801 and item_id = 1

- 5 union all

- 6 select count(*) from logs

- 7 where log_date = 20130802 and item_id = 2

- 8 union all

- 9 select count(*) from logs

- 10 where log_date = 20130803 and item_id = 3

- 11 )t


设置job间并⾏的参数是hive.exec.paralel，将其设为true即可。默认的并⾏度最⾼为8，也就是允许 sql中8个job并⾏。如果想要更⾼的并⾏度，可以通过hive.exec.paralel. thread.number参数进⾏设 置，但要避免设置过⼤⽽占⽤过多资源。

另外在实际开发过程中也发现，⼀些实现思路会导致⽣成多余的job⽽显得不够⾼效。⽐如这个需求： 取出cnblog某⼀天访问⽇志中同时看过博主“⼩张”和博主“⼩李”的⼈数。低效的思路是⾯向明细的，先 取出看过博主“⼩张”的⽤户，再取出看过博主“⼩李”的⽤户，然后取交集，代码如下：

- 1 select count(*) from

- 2 (select distinct user_id

- 3 from cnblogs_visit_20130801 where blog_owner = ‘⼩张’) a

- 4 join

- 5 (select distinct user_id

- 6 from cnblogs_visit_20130801 where blog_owner = ‘⼩李’) b

- 7 on a.user_id = b.user_id;


这样⼀来，就要产⽣2个求⼦查询的job（当然，可以并⾏），⼀个join job，还有⼀个计算count的 job。 但是我们直接⽤⾯向统计的⽅法去计算的话，则会更加符合M/R的模式：

- 1 select count(*) from

- 2 (

- 3 select user_id,

- 4 count(case when blog_owner = ‘⼩张’ then 1 end) as visit_z,

- 5 count(case when blog_owner = ‘⼩李’ then 1 end) as visit_l

- 6 from cnblogs_visit_20130801 group by user_id

- 7 ) t

- 8 where visit_z > 0 and visit_l > 0;


这种实现⽅式转换成job就只会有2个：内层的⼦查询和外层的统计，更少的job也就带来更⾼效的执⾏ 结果。 第⼀种查询⽅法符合思考问题的直觉，是⼯程师和分析师在实际查数据中最先想到的写法，然⽽想要 更加快速的跑出结果，懂⼀点⼯具的内部机理，也是必须的。 来源： 如⽆转载说明，则均为本站原创⽂章，转载请注明：来源：⼦猴博客

htp:/ w.cnblogs.com/sunyi514/p/327957.html

0

# 相关内容

- 2013 年 04 ⽉ 01 ⽇Hive与Hbase的整合

- 2013 年 04 ⽉ 02 ⽇Hive的快速使⽤说明⼀

2013 年 05 ⽉ 20 ⽇hive数据操作(翻译⾃Hive wiki+实例讲解)

- 2013 年 04 ⽉ 03 ⽇Hive的快速使⽤说明⼆


(0)

(0)

(0)

(0)

- 2013 年 05 ⽉ 17 ⽇hive配置参数说明

- 2013 年 07 ⽉ 30 ⽇hive：MySQLSyntaxErorException: Specified key was to long; max key len gth is 767 bytes

- 2013 年 05 ⽉ 15 ⽇hive QL（HQL）简明指南

- 2014 年 02 ⽉ 12 ⽇hive sql 优化


- 2014 年 01 ⽉ 23 ⽇使⽤Jdbc连接Hive错误记录


- 2014 年 01 ⽉ 29 ⽇hive安装错误：FAILED: Eror in metadata


(0)

(0)

(0)

(0)

(0)

(0)

- 2013 年 05 ⽉ 09 ⽇Hive导出数据为本地⽂件

- 2014 年 04 ⽉ 29 ⽇使⽤Hive调试模式定位错误


(0)

(0)

2013 年 07 ⽉ 30 ⽇hive错误：FAILED Eror in metadata:java.lang.IlegalArgumentException:URI: does not have a scheme

- (0)
- (1)

(10)

- (2)


201 年 01 ⽉ 27 ⽇Java:HashTable和HashMap的区别

2010 年 02 ⽉ 23 ⽇关于Casandra删除记录的困惑

2010 年 10 ⽉ 10 ⽇设计模式之单件模式（Singleton）

209 年 12 ⽉ 21 ⽇java笔记：RandomAcesFile例⼦和浅析

(0)

- 2012 年 12 ⽉ 12 ⽇Warning: Mising argument 2 for wpdb:prepare()

2014 年 03 ⽉ 18 ⽇python错误：IndentationEror: expected an indented block

201 年 12 ⽉ 14 ⽇发⼈深省的201年底总结

209 年 1 ⽉ 21 ⽇Sun java Mobility Day 209【图】

- 2012 年 04 ⽉ 01 ⽇如何优化提⾼tomcat启动速度

- 2012 年 05 ⽉ 15 ⽇Hadop本地开发环境搭建


201 年 01 ⽉ 27 ⽇Java：String和StringBufer的区别

201 年 07 ⽉ 14 ⽇ActiveMQ消息中间件使⽤

- 2013 年 01 ⽉ 07 ⽇Zokeper集群安装配置

- 2014 年 03 ⽉ 27 ⽇mysql查看索引


(12)

- (0)
- (1)

(0)

(0)

(0)

- (2)


(4)

(0)

- (0)
- (1)

(0)

- (2)


2014 年 01 ⽉ 16 ⽇elasticsearch搭建与使⽤（⼀）

2012 年 04 ⽉ 20 ⽇Lucene-3.6.0和Solr-3.6.0发布

201 年 04 ⽉ 06 ⽇中国历史上最⾼⽔平的36⾸诗词（值得⼀⽣去品味）

