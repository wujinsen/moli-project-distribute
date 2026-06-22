Nutch 是⼀个开源Java 实现的搜索引擎。它提供了我们运⾏⾃⼰的搜索引擎所需的全部⼯具。包括全 ⽂搜索和Web爬⾍。

- 1简介Nutch是⼀个开源Java实现的搜索引擎。它提供了我们运⾏⾃⼰

- 2⽬标编辑

- 3爬⾍编辑


的搜索引擎所需的全部⼯具。包括全⽂搜索和Web爬⾍。 尽管Web搜索是漫游Internet的基本要求, 但是现有web搜索引擎的数⽬却在下降. 并且这很有可能进⼀ 步演变成为⼀个公司垄断了⼏乎所有的web搜索为其谋取商业利益.这显然 不利于⼴⼤Internet⽤户. Nutch为我们提供了这样⼀个不同的选择. 相对于那些商⽤的搜索引擎, Nutch作为开放源代码 搜索引擎 将会更加透明, 从⽽更值得⼤家信赖. 现在所有主要的搜索引擎都采⽤私有的排序算法, ⽽不会解释为什 么⼀个⽹⻚会排在⼀个特定的位置. 除此之外, 有的搜索引擎依照⽹站所付的 费⽤, ⽽不是根据它们本 身的价值进⾏排序. 与它们不同, Nucth没有什么需要隐瞒, 也没有 动机去扭曲搜索的结果. Nutch将尽 ⾃⼰最⼤的努⼒为⽤户提供最好的搜索结果. Nutch⽬前最新的版本为version2.2.1。

Nutch 致⼒于让每个⼈能很容易, 同时花费很少就可以配置世界⼀流的Web搜索引擎. 为了完成这⼀宏 伟的⽬标, Nutch必须能够做到:

- * 每个⽉取⼏⼗亿⽹⻚
- * 为这些⽹⻚维护⼀个索引
- * 对索引⽂件进⾏每秒上千次的搜索
- * 提供⾼质量的搜索结果 组成 爬⾍crawler和查询searcher。Crawler主要⽤于从⽹络上抓取⽹⻚并为这些⽹⻚建⽴索引。Searcher主 要利⽤这些索引检索⽤户的查找关键词来产⽣查找结果。两者之间的接⼝是索引，所以除去索引部 分，两者之间的耦合度很低。 Crawler和Searcher两部分尽量分开的⽬的主要是为了使两部分可以分布式配置在硬件平台上，例如将 Crawler和Searcher分别放在两个主机上，这样可以提升性能。


Crawler的重点在两个⽅⾯，Crawler的⼯作流程和涉及的数据⽂件的格式和含义。数据⽂件主要包括 三类，分别是web database，⼀系列的segment加上index，三者的物理⽂件分别存储在爬⾏结果⽬录 下的db⽬录下webdb⼦⽂件夹内，segments⽂件夹和index⽂件夹。那么三者分别存储的信息是什么 呢？

⼀次爬⾏会产⽣很多个segment，每个segment内存储的是爬⾍Crawler在单独⼀次抓取循环中抓到的 ⽹⻚以及这些⽹⻚的索引。Crawler爬⾏时会根据WebDB中的link关系按照⼀定的爬⾏策略⽣成每次抓 取循环所需的fetchlist，然后Fetcher通过fetchlist中的URLs抓取这些⽹⻚并索引，然后将其存⼊ segment。Segment是有时限的，当这些⽹⻚被Crawler重新抓取后，先前抓取产⽣的segment就作废 了。在存储中。Segment⽂件夹是以产⽣时间命名的，⽅便我们删除作废的segments以节省存储空 间。 Index是Crawler抓取的所有⽹⻚的索引，它是通过对所有单个segment中的索引进⾏合并处理所得的。 Nutch利⽤Lucene技术进⾏索引，所以Lucene中对索引进⾏操作的接⼝对Nutch中的index同样有效。 但是需要注意的是，Lucene中的segment和Nutch中的不同，Lucene中的segment是索引index的⼀部 分，但是Nutch中的segment只是WebDB中各个部分⽹⻚的内容和索引，最后通过其⽣成的index跟这 些segment已经毫⽆关系了。 Web database，也叫WebDB，其中存储的是爬⾍所抓取⽹⻚之间的链接结构信息，它只在爬⾍ Crawler⼯作中使⽤⽽和Searcher的⼯作没有任何关系。WebDB内存储了两种实体的信息：page和 link。Page实体通过描述⽹络上⼀个⽹⻚的特征信息来表征⼀个实际的⽹⻚，因为⽹⻚有很多个需要描 述，WebDB中通过⽹⻚的URL和⽹⻚内容的MD5两种索引⽅法对这些⽹⻚实体进⾏了索引。Page实体 描述的⽹⻚特征主要包括⽹⻚内的link数⽬，抓取此⽹⻚的时间等相关抓取信息，对此⽹⻚的重要度评 分等。同样的，Link实体描述的是两个page实体之间的链接关系。WebDB构成了⼀个所抓取⽹⻚的链 接结构图，这个图中Page实体是图的结点，⽽Link实体则代表图的边。

# 4⼯作流程编辑

在创建⼀个WebDB之后(步骤1), “产⽣/抓取/更新”循环(步骤3－6)根据⼀些种⼦URLs开始启动。当这 个循环彻底结束，Crawler根据抓取中⽣成的segments创建索引（步骤7－10）。在进⾏重复URLs清 除（步骤9）之前，每个segment的索引都是独⽴的（步骤8）。最终，各个独⽴的segment索引被合 并为⼀个最终的索引index（步骤10）。 其中有⼀个细节问题，Dedup操作主要⽤于清除segment索引中的重复URLs，但是我们知道，在 WebDB中是不允许重复的URL存在的，那么为什么这⾥还要进⾏清除呢？原因在于抓取的更新。⽐⽅ 说⼀个⽉之前你抓取过这些⽹⻚，⼀个⽉后为了更新进⾏了重新抓取，那么旧的segment在没有删除 之前仍然起作⽤，这个时候就需要在新旧segment之间进⾏除重。 Nutch和Lucene Nutch是基于Lucene的。Lucene为Nutch提供了⽂本索引和搜索的API。 ⼀个常⻅的问题是：我应该使⽤Lucene还是Nutch？ 最简单的回答是：如果你不需要抓取数据的话，应该使⽤Lucene。 常⻅的应⽤场合是：你有数据源，需要为这些数据提供⼀个搜索⻚⾯。在这种情况下，最好的⽅式是 直接从数据库中取出数据并⽤Lucene API 建⽴索引。 在你没有本地数据源，或者数据源⾮常分散的情况下，应该使⽤Nutch。

在分析了Crawler⼯作中设计的⽂件之后，接下来我们研究Crawler的抓取流程以及这些⽂件在抓取中 扮演的⻆⾊。Crawler的⼯作原理：⾸先Crawler根据WebDB⽣成⼀个待抓取⽹⻚的URL集合叫做 Fetchlist，接着下载线程Fetcher根据Fetchlist将⽹⻚抓取回来，如果下载线程有很多个，那么就⽣成 很多个Fetchlist，也就是⼀个Fetcher对应⼀个Fetchlist。然后Crawler⽤抓取回来的⽹⻚更新 WebDB，根据更新后的WebDB⽣成新的Fetchlist，⾥⾯是未抓取的或者新发现的URLs，然后下⼀轮 抓取循环重新开始。这个循环过程可以叫做“产⽣/抓取/更新”循环。 指向同⼀个主机上Web资源的URLs通常被分配到同⼀个Fetchlist中，这可防⽌过多的Fetchers对⼀个 主机同时进⾏抓取造成主机负担过重。另外Nutch遵守Robots Exclusion Protocol，⽹站可以通过⾃定 义Robots.txt控制Crawler的抓取。 在Nutch中，Crawler操作的实现是通过⼀系列⼦操作的实现来完成的。这些⼦操作Nutch都提供了⼦ 命令⾏可以单独进⾏调⽤。下⾯就是这些⼦操作的功能描述以及命令⾏，命令⾏在括号中。

- 1. 创建⼀个新的WebDb (admin db -create).
- 2. 将抓取起始URLs写⼊WebDB中 (inject).
- 3. 根据WebDB⽣成fetchlist并写⼊相应的segment(generate).
- 4. 根据fetchlist中的URL抓取⽹⻚ (fetch).
- 5. 根据抓取⽹⻚更新WebDb (updatedb).
- 6. 循环进⾏3－5步直⾄预先设定的抓取深度。
- 7. 根据WebDB得到的⽹⻚评分和links更新segments (updatesegs).
- 8. 对所抓取的⽹⻚进⾏索引(index).
- 9. 在索引中丢弃有重复内容的⽹⻚和重复的URLs (dedup).
- 10. 将segments中的索引进⾏合并⽣成⽤于检索的最终index(merge).


