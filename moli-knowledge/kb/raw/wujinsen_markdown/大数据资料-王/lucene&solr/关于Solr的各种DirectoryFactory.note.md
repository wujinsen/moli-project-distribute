htp:/ w.solrcn.c om/boks/

看到⼀个关于Solr的中⽂Bok⽹站，介绍的有些简易，不过专注于Solr值得关注：

最近快被Solr的Map Failed快弄崩溃了 =b，继续调研。。。，还没好。下⾯从中摘⼀段关于 SolrDirectoryFactory的配置， Apache Lucene 以及 Solr ⼀个重要的属性是 Lucene ⽬录实现。⽬录接⼝为 Lucene 提供抽象的 IO 操 作层。尽管选⼀个⽬录实现看起来很简单，但是在极端情况下，它会影响性能。以下将为你讲解如何 选择正确的⽬录实现。 为了使⽤期望的⽬录，要做的就是选择正确的⽬录实现的⼯⼚类，然后告知 solr。假设你打算使⽤ NRTCachingDirectory 作为你的⽬录实现，为了实现这个，需要在 solrconfig.xml 中设置以下内容： 这就是所有要做的，很简单，但是有哪些⼯⼚可⽤呢，在Solr4.2版本中，有以下⼯⼚可⽤： solr.StandardDirectoryFactory solr.SimpleFSDirectoryFactory solr.NIOFSDirectoryFactory solr. MapDirectoryFactory solr.NRTCachingDirectoryFactory solr.RAMDirectoryFactory 现在，让我们分别看看每个⼯⼚。 在深⼊了解每个⼯⼚的细节前，我们先了解⼀些⼯⼚的配置参数。directoryFactory 的参数⼀共两个， ⼀个是 name，name 要设置成 “DirectoryFactory”,另外⼀个是 clas，clas 设置成我们所选的⼯⼚实 现类。 打算让 solr ⾃⾏决定使⽤哪个⼯⼚时，可以使⽤ solr. StandardDirectoryFactory。该实现是基于⽂件 系统形式的，依赖当前操作系统和 Java 虚拟机。 如果在⼀个⼩系统中使⽤，没有很多线程，这样可以使⽤ solr.SimpleFSDirectoryFactory，这个⼯⼚ 将索引⽂件保存在本地的⽂件系统中，它不适⽤于⼤量线程访问的情况。 solr.NIOFSDirectoryFactory 可以适⽤于多线程同时访问，但是它在 Windows 平台不能很好⼯作，这 是因为 JVM 的 bug。 solr. MapDirectoryFactory 是从 solr 3.1 到 4.0 在 64 位 Linux 系统上的默认⽬录⼯⼚。这个⽬录实 现类适⽤虚拟内存和内核中⼀个叫 map 的特性来访问存在磁盘上的⽂件。它允许 Lucene 直接访问 I/O 缓存，当不需要准实时搜索时，这个⽬录实现是⼀个⾮常不错的选择。 如果需要准实时搜索，你需要使⽤ solr.NRTCachingDirectoryFactory，它设计成将索引的⼀部分放在 内存中来加速准实时操作。 最后⼀个⽬录⼯⼚是 solr.RAMDirectoryFactory，这是唯⼀⼀个⾮持久化的⼯程，整个索引是存放在 RAM 内存中，这样在系统重启或崩溃时，将丢失所有索引。这样你可能会问，那为什么使⽤这个⼯ 程？在⾃动完成功能和单元测试时，这种⼯程还是有⽤的。另外需要注意的是这种⼯程也不是为⼤数 据设计的。 但是在服务器上建1亿数据时各种内存吃紧，有朋友这么建议:

“你那个内存消耗，肯定不适合 map的，StandardDirectoryFactory太吃io了， solr.NRTCachingDirectoryFactory会导致jvmGC的压⼒太⼤，⽤NIO吧” 另外：”MergeFactor要根据数 据要合理的调整，默认的10不⾏设⼤⼀点” 参考

htp:/blog.csdn.net/pangliyewanmei/article/details/5 73921

更专业的Factory解释还是要看Lucene in action第⼆版中⽂版2.10节 P52 此条⽬由 发表在 分类⽬录，并贴了 、 标签。将 加⼊收藏夹。

jacoxu Big Data Lucene Solr 固定链接

