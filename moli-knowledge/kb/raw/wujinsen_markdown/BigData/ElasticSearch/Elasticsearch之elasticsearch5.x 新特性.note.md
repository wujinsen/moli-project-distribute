其实，elasticsearch5.x 和 elasticsearch2.x 并不区别很⼤。 是因为，ELK⾥之前版本各种很混乱，直接升级到5.0了。 其实，elasticsearch5.x 按理来说是elasticsearch3.x，只是为了跟随ELK整体版本的统⼀。

![image 1](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile1.png>)

## 下⾯给⼤家介绍⼀下 5.0 版⾥⾯的⼀些新的特性和改进

5.0？ 天啦噜，你是不是觉得版本跳的太快了。 好吧，先来说说背后的原因吧。

![image 2](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile2.png>)

相信⼤家都听说 ELK 吧，是 Elasticsearch 、 Logstash 、 Kibana 三个产品的⾸字⺟缩写， 现在 Elastic ⼜新增了⼀个新的开源项⽬成员： Beats。

![image 3](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile3.png>)

有⼈建议以后这么叫： ELKB ？

为了未来更好的扩展性:) ELKBS？ELKBSU？..... 所以我们打算将产品线命名为 ElasticStack 同时由于现在的版本⽐较混乱，每个产品的版本号都不⼀样， Elasticsearch和Logstash⽬前是

2.3.4；Kibana是4.5.3；Beats是1.2.3；

![image 4](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile4.png>)

版本号太乱了有没有，什么版本的 ES ⽤什么版本的 Kibana ？有没有兼容性问题？ 所以我们打算将这些的产品版本号也统⼀⼀下，即 v5.0 ，为什么是 5.0 ，因

为 Kibana 都 4.x 了，下个版本就只能是 5.0 了，其他产品就跟着跳跃⼀把，第⼀个 5.0 正式版将 在今年的秋季发布，⽬前最新的测试版本是： 5.0 Alpha 4。

![image 5](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile5.png>)

⽬前各团队正在紧张的开发测试中，每天都有新的功能和改进，本次分享主要介绍⼀ 下 Elasticsearch 的主要变化。

# Elasticsearch5.0新增功能

⾸先来看看 5.0 ⾥⾯都引⼊了哪些新的功能吧。

- 1、⾸先看看跟性能有关的


1.1 第⼀个就是Lucene 6.x 的⽀持。 Elasticsearch5.0率先集成了Lucene6版本，其中最重要的特性就是 Dimensional Point Fields，多维浮点字段，ES⾥⾯相关的字段如date, numeric，ip 和 Geospatial 都将⼤⼤提升性 能。

这么说吧，磁盘空间少⼀半；索引时间少⼀半；查询性能提升25%；IPV6也⽀持了。 为什么快，底层使⽤的是Block k-d trees，核⼼思想是将数字类型编码成定⻓的字节数组，对定

⻓的字节数组内容进⾏编码排序，然后来构建⼆叉树，然后依次递归构建，⽬前底层⽀持8个维度和最 多每个维度16个字节，基本满⾜⼤部分场景。

说了这么多，看图⽐较直接。

![image 6](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile6.png>)

图中从 2015 /10/32 total bytes 飙升是因为 es 启⽤了 docvalues ，我们关注红线，最近的 引⼊新的数据结构之后，红⾊的索引⼤⼩只有原来的⼀半⼤⼩。

索引⼩了之后， merge 的时间也响应的减少了，看下图：

![image 7](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile7.png>)

相应的 Java 堆内存占⽤只原来的⼀半：

![image 8](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile8.png>)

- 1.2 再看看 索引的性能 ，也是飙升：


![image 9](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile9.png>)

当然 Lucene6 ⾥⾯还有很多优化和改进，这⾥没有⼀⼀列举。

- 1.3 我们再看看索引性能⽅⾯的其他优化。 ES5.0在Internal engine级别移除了⽤于避免同⼀⽂档并发更新的竞争锁，带来15%-20%的性


### 能提升 #18060 。

![image 10](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile10.png>)

以上截图来⾃ ES 的每⽇持续性能监控： https://benchmarks.elastic.co/index.html

1.4 另⼀个 和 aggregation 的改进也是⾮常⼤， Instant Aggregations。 Elasticsearch已经在Shard层⾯提供了Aggregation缓存，如果你的数据没有变化，ES能够直

接返回上次的缓存结果，但是有⼀个场景⽐较特殊，就是 date histogram，⼤家kibana上⾯的条件 是不是经常设置的相对时间，如：from:now-30d to:now，好吧，now是⼀个变量，每时每刻都在 变，所以query条件⼀直在变，缓存也就是没有利⽤起来。

经过⼀年时间⼤量的重构，现在可以做到对查询做到灵活的重写： ⾸先，`now`关键字最终会被重写成具体的值； 其次 ， 每个shard会根据⾃⼰的数据的范围来重写查询为 `match_all`或者是

`match_none`查询，所以现在的查询能够被有效的缓存，并且只有个别数据有变化的Shard才需要 重新计算，⼤⼤提升查询速度。

1.5 另外再看看和Scroll相关的吧。 现在新增了⼀个：Sliced Scroll类型

⽤过Scroll接⼝吧，很慢？如果你数据量很⼤，⽤Scroll遍历数据那确实是接受不了，现在Scroll 接⼝可以并发来进⾏数据遍历了。

每个Scroll请求，可以分成多个Slice请求，可以理解为切⽚，各Slice独⽴并⾏，利⽤Scroll重建 或者遍历要快很多倍。

看看这个demo

![image 11](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile11.png>)

可以看到两个 scroll 请求， id 分别是 0 和 1 ， max 是最⼤可⽀持的并⾏任务，可以各⾃独⽴ 进⾏数据的遍历获取。

- 2、我们再看看es在查询优化这块做的⼯作


2.1 新增了⼀个Profile API。 #https://www.elastic.co/guide/en/elasticsearch/reference/master/searchprofile.html#_usage_3

都说要致富先修路，要调优当然需要先监控啦，elasticsearch在很多层⾯都提供了stats⽅便你 来监控调优，但是还不够，其实很多情况下查询速度慢很⼤⼀部分原因是糟糕的查询引起的，玩过 SQL的⼈都知道，数据库服务的执⾏计划（execution plan）⾮常有⽤，可以看到那些查询⾛没⾛索 引和执⾏时间，⽤来调优，elasticsearch现在提供了Profile API来进⾏查询的优化，只需要在查询的 时候开启profile：true就可以了，⼀个查询执⾏过程中的每个组件的性能消耗都能收集到。

![image 12](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile12.png>)

那个⼦查询耗时多少，占⽐多少，⼀⽬了然。 同时⽀持search和aggregation的profile。 还有⼀个和翻⻚相关的问题，就是深度分⻚ ，是个⽼⼤难的问题，因为需要全局排序

（ number_of_shards * (from + size) ），所以需要消耗⼤量内存，以前的 es 没有限制，有些 同学翻到⼏千⻚发现 es 直接内存溢出挂了，后⾯ elasticsearch 加上了限制， from+size 不能超 过 1w 条，并且如果需要深度翻⻚，建议使⽤ scroll 来做。

但是 scroll 有⼏个问题，第⼀个是没有顺序，直接从底层 segment 进⾏遍历读取，第⼆个实时 性没法保证， scroll 操作有状态， es 会维持 scroll 请求的上下⽂⼀段时间，超时后才释放，另外你 在 scroll 过程中对索引数据进⾏了修改了，这个时候 scroll接⼝是拿不到的，灵活性较差， 现在有⼀ 个新的 Search After 机制，其实和 scroll 类似，也是游标的机制，它的原理是对⽂档按照多个字段 进⾏排序，然后利⽤上⼀个结果的最后⼀个⽂档作为起始值，拿 size 个⽂档，⼀般我们建议使 ⽤ _uid 这个字段，它的值是唯⼀的 id 。 #（Search After https://github.com/elastic/elasticsearch/blob/148f9af5857f287666aead37f249f204a870 ab39/docs/reference/search/request/search-after.asciidoc ） 来看⼀个Search After 的demo 吧，⽐较直观的理解⼀下：

![image 13](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile13.png>)

上⾯的 demo ， search_after 后⾯带的两个参数，就是 sort 的两个结果。 根据你的排序条件来的，三个排序条件，就传三个参数。

- 3、再看看跟索引与分⽚管理相关的新功能吧。


3.1 新增了⼀个 Shrink API #https://www.elastic.co/guide/en/elasticsearch/reference/master/indices-shrinkindex.html#_shrinking_an_index

相信⼤家都知道elasticsearch索引的shard数是固定的，设置好了之后不能修改，如果发现 shard太多或者太少的问题，之前如果要设置Elasticsearch的分⽚数，只能在创建索引的时候设置 好，并且数据进来了之后就不能进⾏修改，如果要修改，只能重建索引。

现在有了Shrink接⼝，它可将分⽚数进⾏收缩成它的因数，如之前你是15个分⽚，你可以收缩成 5个或者3个⼜或者1个，那么我们就可以想象成这样⼀种场景，在写⼊压⼒⾮常⼤的收集阶段，设置 ⾜够多的索引，充分利⽤shard的并⾏写能⼒，索引写完之后收缩成更少的shard，提⾼查询性能。

这⾥是⼀个API调⽤的例⼦

![image 14](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile14.png>)

上⾯的例⼦对 my_source_index 伸缩成⼀个分⽚的 my_targe_index, 使⽤了最佳压缩。 有⼈肯定会问慢不慢？⾮常快！ Shrink的过程会借助操作系统的Hardlink进⾏索引⽂件的链接，

这个操作是⾮常快的，毫秒级Shrink就可收缩完成，当然windows不⽀持hard link，需要拷⻉⽂ 件，可能就会很慢了。

再来看另外⼀个⽐较有意思的新特性，除了有意思，当然还很强⼤。 3.2 新增了⼀个Rollover API。

https://www.elastic.co/guide/en/elasticsearch/reference/master/indices-rolloverindex.html#indices-rollover-index

前⾯说的这种场景对于⽇志类的数据⾮常有⽤，⼀般我们按天来对索引进⾏分割（数据量更⼤还 能进⼀步拆分），我们以前是在程序⾥设置⼀个⾃动⽣成索引的模板，⼤家⽤过logstash应该就记得 有这么⼀个模板logstash-[YYYY-MM-DD]这样的模板，现在es5.0⾥⾯提供了⼀个更加简单的⽅式： Rollover API

API调⽤⽅式如下：

![image 15](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile15.png>)

从上⾯可以看到，⾸先创建⼀个 logs-0001 的索引，它有⼀个别名是 logs_write, 然后我们给 这个 logs_write 创建了⼀个 rollover 规则，即这个索引⽂档不超过 1000 个或者最多保存 7 天的 数据，超过会⾃动切换别名到 logs-0002, 你也可以设置索引的 setting 、 mapping 等参数 , 剩下 的 es 会⾃动帮你处理。这个特性对于存放⽇志数据的场景是极为友好的。

3.3 新增：Reindex。 另外关于索引数据，⼤家之前经常重建，数据源在各种场景，重建起来很是头痛，那就不得不说

说现在新加的Reindex接⼝了，Reindex可以直接在Elasticsearch集群⾥⾯对数据进⾏重建，如果你 的mapping因为修改⽽需要重建，⼜或者索引设置修改需要重建的时候，借助Reindex可以很⽅便的 异步进⾏重建，并且⽀持跨集群间的数据迁移。

⽐如按天创建的索引可以定期重建合并到以⽉为单位的索引⾥⾯去。 当然索引⾥⾯要启⽤_source。 来看看这个demo吧，重建过程中，还能对数据就⾏加⼯。

![image 16](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile16.png>)

3.4 再看看跟Java开发者最相关的吧，就是 RestClient了 5.0⾥⾯提供了第⼀个Java原⽣的REST客户端SDK，相⽐之前的TransportClient，版本依赖绑

定，集群升级麻烦，不⽀持跨Java版本的调⽤等问题，新的基于HTTP协议的客户端对Elasticsearch 的依赖解耦，没有jar包冲突，提供了集群节点⾃动发现、⽇志处理、节点请求失败⾃动进⾏请求轮 询，充分发挥Elasticsearch的⾼可⽤能⼒，并且性能不相上下。 #19055 。

- 4、然后我们再看看其他的特性吧：


4.1 新增了⼀个 Wait for refresh 功能。 简单来说相当于是提供了⽂档级别的Refresh：

https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-refresh.html。

索引操作新增refresh参数，⼤家知道elasticsearch可以设置refresh时间来保证数据的实时性， refresh时间过于频繁会造成很⼤的开销，太⼩会造成数据的延时，之前提供了索引层⾯的_refresh接 ⼝，但是这个接⼝⼯作在索引层⾯，我们不建议频繁去调⽤，如果你有需要修改了某个⽂档，需要客 户端实时可⻅怎么办？

在 5.0中，Index、Bulk、Delete、Update这些数据新增和修改的接⼝能够在单个⽂档层⾯进⾏ refresh控制了，有两种⽅案可选，⼀种是创建⼀个很⼩的段，然后进⾏刷新保证可⻅和消耗⼀定的开 销，另外⼀种是请求等待es的定期refresh之后再返回。

调⽤例⼦：

![image 17](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile17.png>)

4.2 # 新增： Ingest Node # #https://www.elastic.co/guide/en/elasticsearch/reference/master/ingest.html# 再⼀个⽐较重要的特性就是IngestNode了，⼤家之前如果需要对数据进⾏加⼯，都是在索引之前

进⾏处理，⽐如logstash可以对⽇志进⾏结构化和转换，现在直接在es就可以处理了，⽬前es提供了 ⼀些常⽤的诸如convert、grok之类的处理器，在使⽤的时候，先定义⼀个pipeline管道，⾥⾯设置 ⽂档的加⼯逻辑，在建索引的时候指定pipeline名称，那么这个索引就会按照预先定义好的pipeline来 处理了；

Demo again：

![image 18](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile18.png>)

上图⾸先创建了⼀个名为my-pipeline-id的处理管道，然后接下来的索引操作就可以直接使⽤这 个管道来对foo字段进⾏操作了，上⾯的例⼦是设置foo字段为bar值。

上⾯的还不太酷，我们再来看另外⼀个例⼦，现在有这么⼀条原始的⽇志，内容如下：

{ "message": "55.3.244.1 GET /index.html 15824 0.043”

}

google之后得知其Grok的pattern如下：） %{IP:client} %{WORD:method} %{URIPATHPARAM:request} %{NUMBER:bytes} % {NUMBER:duration}

那么我们使⽤Ingest就可以这么定义⼀个pipeline：

![image 19](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile19.png>)

那么通过我们的 pipeline 处理之后的⽂档⻓什么样呢，我们获取这个⽂档的内容看看：

![image 20](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile20.png>)

很明显，原始字段 message 被拆分成了更加结构化的对象了。

- 5、再看看脚本⽅⾯的改变


5.1 #新增Painless Scripting# 还记得Groove脚本的漏洞吧，Groove脚本开启之后，如果被⼈误⽤可能带来的漏洞，为什么

呢，主要是这些外部的脚本引擎太过于强⼤，什么都能做，⽤不好或者设置不当就会引起安全⻛险， 基于安全和性能⽅⾯，我们⾃⼰开发了⼀个新的脚本引擎，名字就叫Painless，顾名思义，简单安 全，⽆痛使⽤，和Groove的沙盒机制不⼀样，Painless使⽤⽩名单来限制函数与字段的访问，针对es 的场景来进⾏优化，只做es数据的操作，更加轻量级，速度要快好⼏倍，并且⽀持Java静态类型，语 法保持Groove类似，还⽀持Java的lambda表达式。

我们对⽐⼀下性能，看下图

![image 21](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile21.png>)

Groovy 是弱弱的绿⾊的那根。 再看看如何使⽤：

def first = input.doc.first_name.0; def last = input.doc.last_name.0; return first + " " + last;

是不是和之前的写法差不多 或者还可以是强类型（10倍速度于上⾯的动态类型）

String first = (String)((List)((Map)input.get("doc")).get("first_name")).get(0); String last = (String)((List)((Map)input.get("doc")).get("last_name")).get(0); return first + " " + last;

脚本可以在很多地⽅使⽤，⽐如搜索⾃定义评分；更新时对字段进⾏加⼯等 如：

![image 22](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile22.png>)

- 6、再来看看基础架构⽅⾯的变化


6.1 新增：Task Manager 这个是5.0 引⼊任务调度管理机制，⽤来做 离线任务的管理，⽐如⻓时间运⾏的reindex和

update_by_query等都是运⾏在TaskManager机制之上的，并且任务是可管理的，你可以随时 cancel掉，并且任务状态持久化，⽀持故障恢复；

6.2 还新增⼀个： Depreated logging ⼤家在⽤ES的时候，其实有些接⼝可能以及打上了Depreated标签，即废弃了，在将来的某个版

本中就会移除，你当前能⽤是因为⼀般废弃的接⼝都不会⽴即移除，给⾜够的时间迁移，但是也是需 要知道哪些不能⽤了，要改应⽤代码了，所以现在有了Depreated⽇志，当打开这个⽇志之后，你调 ⽤的接⼝如果已经是废弃的接⼝，就会记录下⽇志，那么接下来的事情你就知道你应该怎么做了。

6.3 新增 : Cluster allocation explain API 『谁能给我⼀个shard不能分配的理由』，现在有了，⼤家如果之前遇到过分⽚不能正常分配的问

题，但是不知道是什么原因，只能尝试⼿动路由或者重启节点，但是不⼀定能解决，其实⾥⾯有很多 原因，现在提供的这个explain接⼝就是告诉你⽬前为什么不能正常分配的原因，⽅便你去解决。

6.4 另外在数据结构这块，新增 : half_float 类型

https://www.elastic.co/guide/en/elasticsearch/reference/master/number.html

只使⽤ 16 位 ⾜够满⾜⼤部分存储监控数值类型的场景，⽀持范围：2负24次⽅ 到 65504，但 是只占⽤float⼀半的存储空间。

### 6.5 Aggregation 新增 : Matrix Stats Aggregation # 18300

⾦融领域⾮常有⽤的，可计算多个向量元素协⽅差矩阵、相关系数矩阵等等

#### 6.6 另外⼀个重要的特性：为索引写操作添加顺序号 # 10708 ⼤家知道es是在primary上写完然后同步写副本，这些请求都是并发的，虽然可以通过version来

控制冲突，

但是没法保证其他副本的操作顺序，通过写的时候产⽣顺序号，并且在本地也写⼊checkpoint来 记录操作点，

这样在副本恢复的时候也可以知道当前副本的数据位置，⽽只需要从指定的数据开始恢复就⾏ 了，⽽不是像以前的粗暴的做完整的⽂件同步 ，另外这些顺序号也是持久化的，重启后也可以快速恢 复副本信息，想想以前的⼤量⽆⽤拷⻉吧和来回倒腾数据吧。

# 7、Elasticsearch5.0其他⽅⾯的改进

7.1 我们再看看 mapping 这块的改进 吧。

### 引⼊新的字段类型 Text/Keyword 来替换 String

以前的string类型被分成Text和Keyword两种类型，keyword类型的数据只能完全匹配，适合那 些不需要分词的数据，

对过滤、聚合⾮常友好，text当然就是全⽂检索需要分词的字段类型了。将类型分开的好处就是 使⽤起来更加简单清晰，以前需要设置analyzer和index，并且有很多都是⾃定义的分词器，从名称 根本看不出来到底分词没有，⽤起来很麻烦。

另外string类型暂时还在的，6.0会移除。 7.2 还有关于 Index Settings 的改进 Elasticsearch的配置实在太多，在以前的版本间，还移除过很多⽆⽤的配置，经常弄错有没有？ 现在，配置验证更加严格和保证原⼦性，如果其中⼀项失败，那个整个都会更新请求都会失败，

不会⼀半成功⼀半失败。下⾯主要说两点：

- 1.设置可以重设会默认值，只需要设置为 `null`即可

- 2.获取设置接⼝新增参数`?include_defaults`,可以直接返回所有设置和默认值


7.3 集群处理的改进 : Deleted Index Tombstones 在以前的es版本中，如果你的旧节点包含了部分索引数据，但是这个索引可能后⾯都已经删掉 了，你启动这个节点之后，会把索引重新加到集群中，是不是觉得有点阴魂不散，现在es5.0会在集群 状态信息⾥⾯保留500个删除的索引信息，所以如果发现这个索引是已经删除过的就会⾃动清理，不 会再重复加进来了。

⽂档对象的改进 : 字段名重新⽀持英⽂句号，再 2.0 的时候移除过 dot 在字段名中的⽀持，现在 问题解决了，⼜重新⽀持了。

es会认为下⾯两个⽂档的内容⼀样：

![image 23](<Elasticsearch之elasticsearch5.x 新特性.note_images/imageFile23.png>)

7.4 还有其他的⼀些改进 Cluster state 的修改现在会和所有节点进⾏ ack 确认。 Shard 的⼀个副本如果失败了， Primary 标记失败的时候会和 Master 节点确认完毕再返回。 使⽤ UUID 来作为索引的物理的路径名，有很多好处，避免命名的冲突。 _timestamp 和 _ttl 已经移除，需要在 Ingest 或者程序端处理。 ES 可直接⽤ HDFS 来进⾏备份还原（ Snapshot/Restore ）了 #15191 。 Delete-by-query 和 Update-by-query 重新回到 core ，以前是插件，现在可以直接使⽤了，

也是构建在 Reindex 机制之上。(es1.x版本是直接⽀持，在es2.x中提取为插件，5.x继续回归直接 ⽀持)

HTTP 请求默认⽀持压缩，当然 http 调⽤端需要在 header 信息⾥⾯传对应的⽀持信息。 创建索引不会再让集群变红了，不会因为这个卡死集群了。 默认使⽤ BM25 评分算法，效果更佳，之前是 TF/IDF。 快照 Snapshots 添加 UUID 解决冲突 #18156 。 限制索引请求⼤⼩，避免⼤量并发请求压垮 ES #16011。 限制单个请求的 shards 数量，默认 1000 个 #17396。 移除 site plugins ，就是说 head 、 bigdesk 都不能直接装 es ⾥⾯了，不过可以部署独⽴站

点（反正都是静态⽂件）或开发 kibana 插件 #16038 。 允许现有 parent 类型新增 child 类型 #17956。 这个功能对于使⽤parent-child特性的⼈应该⾮常有⽤。 ⽀持分号（；）来分割 url 参数，与符号（ & ）⼀样 #18175 。 ⽐如下⾯这个例⼦：

curl http://localhost:9200/_cluster/health?level=indices;pretty=true

好吧，貌似很多，其实上⾯说的还只是众多特性和改进的⼀部分， es5.0 做了⾮常⾮常多⼯作， 本来还打算讲讲 bug 修复的，但是太多了，时间有限， ⼀些重要的 bug在 2.x 都已经第⼀时间解决 了。

## 8、⼤家可以查看下⾯的链接了解更多更详细的更新⽇志

https://www.elastic.co/guide/en/elasticsearch/reference/master/release-notes-5.0.0alpha1-2x.html https://www.elastic.co/guide/en/elasticsearch/reference/master/release-notes-5.0.0-

- alpha1.html https://www.elastic.co/guide/en/elasticsearch/reference/master/release-notes-5.0.0-

- alpha2.html https://www.elastic.co/guide/en/elasticsearch/reference/master/release-notes-5.0.0-

- alpha3.html https://www.elastic.co/guide/en/elasticsearch/reference/master/release-notes-5.0.0-

- alpha4.html


#### 下载体验最新的版本 ： https://www.elastic.co/v5 升级向导：https://github.com/elastic/elasticsearch-migration/blob/2.x/README.asciidoc

