Shufle过程是MapReduce的核⼼，也被称为奇迹发⽣的地⽅。要想理解MapReduce，Shufle是必须要了解的。 我看过很多相关的资料，但每次看完都云⾥雾⾥的绕着，很难理清⼤致的逻辑，反⽽越搅越混。前段时间在做 MapReduce job性能调优的⼯作，需要深⼊代码研究MapReduce的运⾏机制，这才对Shufle探了个究竟。考虑到之 前我在看相关资料⽽看不懂时很恼⽕，所以在这⾥我尽最⼤的可能试着把Shufle说清楚，让每⼀位想了解它原理的 朋友都能有所收获。如果你对这篇⽂章有任何疑问或建议请留⾔到后⾯，谢谢！Shufle的正常意思是洗牌或弄乱， 可能⼤家更熟悉的是Java API⾥的Colections.shufle(List)⽅法，它会随机地打乱参数list⾥的元素顺序。如果你不知 道MapReduce⾥Shufle是什么，那么请看这张图：

这张是官⽅对Shufle过程的描述。但我可以肯定的是，单从这张图你基本不可能明⽩Shufle的过程，因为它与 事实相差挺多，细节也是错乱的。后⾯我会具体描述Shufle的事实情况，所以这⾥你只要清楚Shufle的⼤致范围就 成－怎样把map task的输出结果有效地传送到reduce端。也可以这样理解， Shufle描述着数据从map task输出到 reduce task输⼊的这段过程。

在Hadop这样的集群环境中，⼤部分map task与reduce task的执⾏是在不同的节点上。当然很多情况下 Reduce执⾏时需要跨节点去拉取其它节点上的map task结果。如果集群正在运⾏的job有很多，那么task的正常执⾏ 对集群内部的⽹络资源消耗会很严重。这种⽹络消耗是正常的，我们不能限制，能做的就是最⼤化地减少不必要的消 耗。还有在节点内，相⽐于内存，磁盘IO对job完成时间的影响也是可观的。从最基本的要求来说，我们对Shufle过 程的期望可以有：? 完整地从map task端拉取数据到reduce端。? 在跨节点拉取数据时，尽可能地减少对带宽的不必 要消耗。? 减少磁盘IO对task执⾏的影响。

OK，看到这⾥时，⼤家可以先停下来想想，如果是⾃⼰来设计这段Shufle过程，那么你的设计⽬标是什么。我 想能优化的地⽅主要在于减少拉取数据的量及尽量使⽤内存⽽不是磁盘。

我的分析是基于Hadop0.21.0的源码，如果与你所认识的Shufle过程有差别，不吝指出。我会以WordCount为 例，并假设它有8个map task和3个reduce task。从上图看出，Shufle过程横跨map与reduce两端，所以下⾯我也 会分两部分来展开。先看看map端的情况，如下图：

上图可能是某个map task的运⾏情况。拿它与官⽅图的左半边⽐较，会发现很多不⼀致。官⽅图没有清楚地说明 partition，sort与combiner到底作⽤在哪个阶段。我画了这张图，希望让⼤家清晰地了解从map数据输⼊到map端所 有数据准备好的全过程。

整个流程我分了四步。简单些可以这样说，每个map task都有⼀个内存缓冲区，存储着map的输出结果，当缓冲 区快满的时候需要将缓冲区的数据以⼀个临时⽂件的⽅式存放到磁盘，当整个map task结束后再对磁盘中这个map task产⽣的所有临时⽂件做合并，⽣成最终的正式输出⽂件，然后等待reduce task来拉数据。

当然这⾥的每⼀步都可能包含着多个步骤与细节，下⾯我对细节来⼀⼀说明： 1.在map task执⾏时，它的输⼊数据来源于HDFS的block，当然在MapReduce概念中，map task只读取split。

Split与block的对应关系可能是多对⼀，默认是⼀对⼀。在WordCount例⼦⾥，假设map的输⼊数据都是像“ a”这样 的字符串。

2.在经过maper的运⾏后，我们得知maper的输出是这样⼀个key/value对： key是“ a”， value是数值1。因 为当前map端只做加1的操作，在reduce task⾥才去合并结果集。前⾯我们知道这个job有3个reduce task，到底当 前的“ a”应该交由哪个reduce去做呢，是需要现在决定的。

MapReduce提供Partitioner接⼝，它的作⽤就是根据key或value及reduce的数量来决定当前的这对输出数据最 终应该交由哪个reduce task处理。默认对key hash后再以reduce task数量取模。默认的取模⽅式只是为了平均 reduce的处理能⼒，如果⽤户⾃⼰对Partitioner有需求，可以订制并设置到job上。

在我们的例⼦中，“ a”经过Partitioner后返回0，也就是这对值应当交由第⼀个reducer来处理。接下来，需要 将数据写⼊内存缓冲区中，缓冲区的作⽤是批量收集map结果，减少磁盘IO的影响。我们的key/value对以及Partition 的结果都会被写⼊缓冲区。当然写⼊之前，key与value值都会被序列化成字节数组。

整个内存缓冲区就是⼀个字节数组，它的字节索引及key/value存储结构我没有研究过。如果有朋友对它有研究， 那么请⼤致描述下它的细节吧。

3.这个内存缓冲区是有⼤⼩限制的，默认是10MB。当map task的输出结果很多时，就可能会撑爆内存，所以需 要在⼀定条件下将缓冲区中的数据临时写⼊磁盘，然后重新利⽤这块缓冲区。这个从内存往磁盘写数据的过程被称为 Spil，中⽂可译为溢写，字⾯意思很直观。这个溢写是由单独线程来完成，不影响往缓冲区写map结果的线程。溢写 线程启动时不应该阻⽌map的结果输出，所以整个缓冲区有个溢写的⽐例spil.percent。这个⽐例默认是0.8，也就是 当缓冲区的数据已经达到阈值（bufer size * spil percent = 10MB * 0.8 = 80MB），溢写线程启动，锁定这80MB 的内存，执⾏溢写过程。Map task的输出结果还可以往剩下的20MB内存中写，互不影响。

当溢写线程启动后，需要对这80MB空间内的key做排序(Sort)。排序是MapReduce模型默认的⾏为，这⾥的排 序也是对序列化的字节做的排序。

在这⾥我们可以想想，因为map task的输出是需要发送到不同的reduce端去，⽽内存缓冲区没有对将发送到相 同reduce端的数据做合并，那么这种合并应该是体现是磁盘⽂件中的。从官⽅图上也可以看到写到磁盘中的溢写⽂件 是对不同的reduce端的数值做过合并。所以溢写过程⼀个很重要的细节在于，如果有很多个key/value对需要发送到 某个reduce端去，那么需要将这些key/value值拼接到⼀块，减少与partition相关的索引记录。

在针对每个reduce端⽽合并数据时，有些数据可能像这样：“ a”/1， “ a”/1。对于WordCount例⼦，就是简单 地统计单词出现的次数，如果在同⼀个map task的结果中有很多个像“ a”⼀样出现多次的key，我们就应该把它们 的值合并到⼀块，这个过程叫reduce也叫combine。但MapReduce的术语中，reduce只指reduce端执⾏从多个map task取数据做计算的过程。除reduce外，⾮正式地合并数据只能算做combine了。其实⼤家知道的，MapReduce中 将Combiner等同于Reducer。

如果client设置过Combiner，那么现在就是使⽤Combiner的时候了。将有相同key的key/value对的value加起 来，减少溢写到磁盘的数据量。Combiner会优化MapReduce的中间结果，所以它在整个模型中会多次使⽤。那哪些 场景才能使⽤Combiner呢？从这⾥分析，Combiner的输出是Reducer的输⼊，Combiner绝不能改变最终的计算结 果。所以从我的想法来看，Combiner只应该⽤于那种Reduce的输⼊key/value与输出key/value类型完全⼀致，且不 影响最终结果的场景。⽐如累加，最⼤值等。Combiner的使⽤⼀定得慎重，如果⽤好，它对job执⾏效率有帮助，反 之会影响reduce的最终结果。

4.每次溢写会在磁盘上⽣成⼀个溢写⽂件，如果map的输出结果真的很⼤，有多次这样的溢写发⽣，磁盘上相应 的就会有多个溢写⽂件存在。当map task真正完成时，内存缓冲区中的数据也全部溢写到磁盘中形成⼀个溢写⽂件。 最终磁盘中会⾄少有⼀个这样的溢写⽂件存在(如果map的输出结果很少，当map执⾏完成时，只会产⽣⼀个溢写⽂ 件)，因为最终的⽂件只有⼀个，所以需要将这些溢写⽂件归并到⼀起，这个过程就叫做Merge。Merge是怎样的？如 前⾯的例⼦，“ a”从某个map task读取过来时值是5，从另外⼀个map 读取时值是8，因为它们有相同的key，所以 得merge成group。什么是group。对于“ a”就是像这样的：{“ a”, [5, 8, 2, …]}，数组中的值就是从不同溢写⽂件 中读取出来的，然后再把这些值加起来。请注意，因为merge是将多个溢写⽂件合并到⼀个⽂件，所以可能也有相同 的key存在，在这个过程中如果client设置过Combiner，也会使⽤Combiner来合并相同的key。⾄此，map端的所有 ⼯作都已结束，最终⽣成的这个⽂件也存放在TaskTracker够得着的某个本地⽬录内。每个reduce task不断地通过 RPC从JobTracker那⾥获取map task是否完成的信息，如果reduce task得到通知，获知某台TaskTracker上的map task执⾏完成，Shufle的后半段过程开始启动。简单地说，reduce task在执⾏之前的⼯作就是不断地拉取当前job⾥ 每个map task的最终结果，然后对从不同地⽅拉取过来的数据不断地做merge，也最终形成⼀个⽂件作为reduce task的输⼊⽂件。⻅下图：

如map 端的细节图，Shufle在reduce端的过程也能⽤图上标明的三点来概括。当前reduce copy数据的前提是 它要从JobTracker获得有哪些map task已执⾏结束，这段过程不表，有兴趣的朋友可以关注下。Reducer真正运⾏之 前，所有的时间都是在拉取数据，做merge，且不断重复地在做。如前⾯的⽅式⼀样，下⾯我也分段地描述reduce 端的Shufle细节：

1.Copy过程，简单地拉取数据。Reduce进程启动⼀些数据copy线程(Fetcher)，通过HTP⽅式请求map task所 在的TaskTracker获取map task的输出⽂件。因为map task早已结束，这些⽂件就归TaskTracker管理在本地磁盘 中。 2.Merge阶段。这⾥的merge如map端的merge动作，只是数组中存放的是不同map端copy来的数值。 Copy过来的数据会先放⼊内存缓冲区中，这⾥的缓冲区⼤⼩要⽐map端的更为灵活，它基于JVM的heap size设置， 因为Shufle阶段Reducer不运⾏，所以应该把绝⼤部分的内存都给Shufle⽤。这⾥需要强调的是，merge有三种形 式：1)内存到内存 2)内存到磁盘 3)磁盘到磁盘。默认情况下第⼀种形式不启⽤，让⼈⽐较困惑，是吧。当内存中的 数据量到达⼀定阈值，就启动内存到磁盘的merge。与map 端类似，这也是溢写的过程，这个过程中如果你设置有 Combiner，也是会启⽤的，然后在磁盘中⽣成了众多的溢写⽂件。第⼆种merge⽅式⼀直在运⾏，直到没有map端 的数据时才结束，然后启动第三种磁盘到磁盘的merge⽅式⽣成最终的那个⽂件。 3.Reducer的输⼊⽂件。不断 地merge后，最后会⽣成⼀个“最终⽂件”。为什么加引号？因为这个⽂件可能存在于磁盘上，也可能存在于内存中。 对我们来说，当然希望它存放于内存中，直接作为Reducer的输⼊，但默认情况下，这个⽂件是存放于磁盘中的。⾄ 于怎样才能让这个⽂件出现在内存中，之后的性能优化篇我再说。当Reducer的输⼊⽂件已定，整个Shufle才最终结 束。然后就是Reducer执⾏，把结果放到HDFS上。（完）

