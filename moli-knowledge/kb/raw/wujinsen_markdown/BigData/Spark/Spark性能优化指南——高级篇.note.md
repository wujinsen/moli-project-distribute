# 前⾔

继 讲解了每个Spark开发⼈员都必须熟知的开发调优与资源调优之后，本⽂作为《Spark性能优 化指南》的⾼级篇，将深⼊分析数据倾斜调优与shufle调优，以解决更加棘⼿的性能问题。

基础篇

# 数据倾斜调优

调优概述

有的时候，我们可能会遇到⼤数据计算中⼀个最棘⼿的问题⸺数据倾斜，此时Spark作业的性能会⽐ 期望差很多。数据倾斜调优，就是使⽤各种技术⽅案解决不同类型的数据倾斜问题，以保证Spark作业 的性能。

数据倾斜发⽣时的现象

绝⼤多数task执⾏得都⾮常快，但个别task执⾏极慢。⽐如，总共有1 0个task， 97个task都在1 分钟之内执⾏完了，但是剩余两三个task却要⼀两个⼩时。这种情况很常⻅。

原本能够正常执⾏的Spark作业，某天突然报出 OM（内存溢出）异常，观察异常栈，是我们写的 业务代码造成的。这种情况⽐较少⻅。

## 数据倾斜发⽣的原理

数据倾斜的原理很简单：在进⾏shufle的时候，必须将各个节点上相同的key拉取到某个节点上的⼀个 task来进⾏处理，⽐如按照key进⾏聚合或join等操作。此时如果某个key对应的数据量特别⼤的话，就 会发⽣数据倾斜。⽐如⼤部分key对应10条数据，但是个别key却对应了10万条数据，那么⼤部分task 可能就只会分配到10条数据，然后1秒钟就运⾏完了；但是个别task可能分配到了10万数据，要运⾏ ⼀两个⼩时。因此，整个Spark作业的运⾏进度是由运⾏时间最⻓的那个task决定的。 因此出现数据倾斜的时候，Spark作业看起来会运⾏得⾮常缓慢，甚⾄可能因为某个task处理的数据量 过⼤导致内存溢出。 下图就是⼀个很清晰的例⼦：helo这个key，在三个节点上对应了总共7条数据，这些数据都会被拉取 到同⼀个task中进⾏处理；⽽world和you这两个key分别才对应1条数据，所以另外两个task只要分别 处理1条数据即可。此时第⼀个task的运⾏时间可能是另外两个task的7倍，⽽整个stage的运⾏速度也 由运⾏最慢的那个task所决定。

![image 1](<Spark性能优化指南——高级篇.note_images/imageFile1.png>)

## 如何定位导致数据倾斜的代码

数据倾斜只会发⽣在shufle过程中。这⾥给⼤家罗列⼀些常⽤的并且可能会触发shufle操作的算⼦： distinct、groupByKey、reduceByKey、agregateByKey、join、cogroup、repartition等。出现数据 倾斜时，可能就是你的代码中使⽤了这些算⼦中的某⼀个所导致的。

某个task执⾏特别慢的情况

⾸先要看的，就是数据倾斜发⽣在第⼏个stage中。 如果是⽤yarn-client模式提交，那么本地是直接可以看到log的，可以在log中找到当前运⾏到了第⼏个 stage；如果是⽤yarn-cluster模式提交，则可以通过Spark Web UI来查看当前运⾏到了第⼏个stage。 此外，⽆论是使⽤yarn-client模式还是yarn-cluster模式，我们都可以在Spark Web UI上深⼊看⼀下当 前这个stage各个task分配的数据量，从⽽进⼀步确定是不是task分配的数据不均匀导致了数据倾斜。 ⽐如下图中，倒数第三列显示了每个task的运⾏时间。明显可以看到，有的task运⾏特别快，只需要⼏ 秒钟就可以运⾏完；⽽有的task运⾏特别慢，需要⼏分钟才能运⾏完，此时单从运⾏时间上看就已经 能够确定发⽣数据倾斜了。此外，倒数第⼀列显示了每个task处理的数据量，明显可以看到，运⾏时 间特别短的task只需要处理⼏百KB的数据即可，⽽运⾏时间特别⻓的task需要处理⼏千KB的数据，处 理的数据量差了10倍。此时更加能够确定是发⽣了数据倾斜。

![image 2](<Spark性能优化指南——高级篇.note_images/imageFile2.png>)

知道数据倾斜发⽣在哪⼀个stage之后，接着我们就需要根据stage划分原理，推算出来发⽣倾斜的那 个stage对应代码中的哪⼀部分，这部分代码中肯定会有⼀个shufle类算⼦。精准推算stage与代码的 对应关系，需要对Spark的源码有深⼊的理解，这⾥我们可以介绍⼀个相对简单实⽤的推算⽅法：只要 看到Spark代码中出现了⼀个shufle类算⼦或者是Spark SQL的SQL语句中出现了会导致shufle的语句 （⽐如group by语句），那么就可以判定，以那个地⽅为界限划分出了前后两个stage。 这⾥我们就以Spark最基础的⼊⻔程序⸺单词计数来举例，如何⽤最简单的⽅法⼤致推算出⼀个stage 对应的代码。如下示例，在整个代码中，只有⼀个reduceByKey是会发⽣shufle的算⼦，因此就可以 认为，以这个算⼦为界限，会划分出前后两个stage。

- stage0，主要是执⾏从textFile到map操作，以及执⾏shufle write操作。shufle write操作，我们 可以简单理解为对pairs RD中的数据进⾏分区操作，每个task处理的数据中，相同的key会写⼊同 ⼀个磁盘⽂件内。

- stage1，主要是执⾏从reduceByKey到colect操作，stage1的各个task⼀开始运⾏，就会⾸先执⾏ shufle read操作。执⾏shufle read操作的task，会从stage0的各个task所在节点拉取属于⾃⼰处 理的那些key，然后对同⼀个key进⾏全局性的聚合或join等操作，在这⾥就是对key的value值进⾏ 累加。stage1在执⾏完reduceByKey算⼦之后，就计算出了最终的wordCounts RD，然后会执⾏ colect算⼦，将所有数据拉取到Driver上，供我们遍历和打印输出。


- 1 val conf = new SparkConf()

- 2 val sc = new SparkContext(conf)

- 3

- 4 val lines = sc.textFile("hdfs://...")

- 5 val words = lines.flatMap(_.split(" "))

- 6 val pairs = words.map((_, 1))

- 7 val wordCounts = pairs.reduceByKey(_ + _)

- 8

- 9 wordCounts.collect().foreach(println(_))

- 10


通过对单词计数程序的分析，希望能够让⼤家了解最基本的stage划分的原理，以及stage划分后 shufle操作是如何在两个stage的边界处执⾏的。然后我们就知道如何快速定位出发⽣数据倾斜的 stage对应代码的哪⼀个部分了。⽐如我们在Spark Web UI或者本地log中发现，stage1的某⼏个task 执⾏得特别慢，判定stage1出现了数据倾斜，那么就可以回到代码中定位出stage1主要包括了 reduceByKey这个shufle类算⼦，此时基本就可以确定是由educeByKey算⼦导致的数据倾斜问题。⽐ 如某个单词出现了10万次，其他单词才出现10次，那么stage1的某个task就要处理10万数据，整个 stage的速度就会被这个task拖慢。

某个task莫名其妙内存溢出的情况

这种情况下去定位出问题的代码就⽐较容易了。我们建议直接看yarn-client模式下本地log的异常栈， 或者是通过YARN查看yarn-cluster模式下的log中的异常栈。⼀般来说，通过异常栈信息就可以定位到 你的代码中哪⼀⾏发⽣了内存溢出。然后在那⾏代码附近找找，⼀般也会有shufle类算⼦，此时很可 能就是这个算⼦导致了数据倾斜。 但是⼤家要注意的是，不能单纯靠偶然的内存溢出就判定发⽣了数据倾斜。因为⾃⼰编写的代码的 bug，以及偶然出现的数据异常，也可能会导致内存溢出。因此还是要按照上⾯所讲的⽅法，通过 Spark Web UI查看报错的那个stage的各个task的运⾏时间以及分配的数据量，才能确定是否是由于数 据倾斜才导致了这次内存溢出。

## 查看导致数据倾斜的key的数据分布情况

知道了数据倾斜发⽣在哪⾥之后，通常需要分析⼀下那个执⾏了shufle操作并且导致了数据倾斜的 RD/Hive表，查看⼀下其中key的分布情况。这主要是为之后选择哪⼀种技术⽅案提供依据。针对不同 的key分布与不同的shufle算⼦组合起来的各种情况，可能需要选择不同的技术⽅案来解决。 此时根据你执⾏操作的情况不同，可以有很多种查看key分布的⽅式：

- 1.
- 2.


如果是Spark SQL中的group by、join语句导致的数据倾斜，那么就查询⼀下SQL中使⽤的表的 key分布情况。 如果是对Spark RD执⾏shufle算⼦导致的数据倾斜，那么可以在Spark作业中加⼊查看key分布 的代码，⽐如RD.countByKey()。然后对统计出来的各个key出现的次数，colect/take到客户端 打印⼀下，就可以看到key的分布情况。

举例来说，对于上⾯所说的单词计数程序，如果确定了是stage1的reduceByKey算⼦导致了数据倾 斜，那么就应该看看进⾏reduceByKey操作的RD中的key分布情况，在这个例⼦中指的就是pairs RD。如下示例，我们可以先对pairs采样10%的样本数据，然后使⽤countByKey算⼦统计出每个key 出现的次数，最后在客户端遍历和打印样本数据中各个key的出现次数。

- 1 val sampledPairs = pairs.sample(false, 0.1)

- 2 val sampledWordCounts = sampledPairs.countByKey()

- 3 sampledWordCounts.foreach(println(_))

- 4


## 数据倾斜的解决⽅案

解决⽅案⼀：使⽤Hive ETL预处理数据

⽅案适⽤场景：导致数据倾斜的是Hive表。如果该Hive表中的数据本身很不均匀（⽐如某个key对应了 10万数据，其他key才对应了10条数据），⽽且业务场景需要频繁使⽤Spark对Hive表执⾏某个分析 操作，那么⽐较适合使⽤这种技术⽅案。 ⽅案实现思路：此时可以评估⼀下，是否可以通过Hive来进⾏数据预处理（即通过Hive ETL预先对数 据按照key进⾏聚合，或者是预先和其他表进⾏join），然后在Spark作业中针对的数据源就不是原来的 Hive表了，⽽是预处理后的Hive表。此时由于数据已经预先进⾏过聚合或join操作了，那么在Spark作 业中也就不需要使⽤原先的shufle类算⼦执⾏这类操作了。 ⽅案实现原理：这种⽅案从根源上解决了数据倾斜，因为彻底避免了在Spark中执⾏shufle类算⼦，那 么肯定就不会有数据倾斜的问题了。但是这⾥也要提醒⼀下⼤家，这种⽅式属于治标不治本。因为毕 竟数据本身就存在分布不均匀的问题，所以Hive ETL中进⾏group by或者join等shufle操作时，还是会 出现数据倾斜，导致Hive ETL的速度很慢。我们只是把数据倾斜的发⽣提前到了Hive ETL中，避免 Spark程序发⽣数据倾斜⽽已。 ⽅案优点：实现起来简单便捷，效果还⾮常好，完全规避掉了数据倾斜，Spark作业的性能会⼤幅度提 升。 ⽅案缺点：治标不治本，Hive ETL中还是会发⽣数据倾斜。 ⽅案实践经验：在⼀些Java系统与Spark结合使⽤的项⽬中，会出现Java代码频繁调⽤Spark作业的场 景，⽽且对Spark作业的执⾏性能要求很⾼，就⽐较适合使⽤这种⽅案。将数据倾斜提前到上游的Hive ETL，每天仅执⾏⼀次，只有那⼀次是⽐较慢的，⽽之后每次Java调⽤Spark作业时，执⾏速度都会很 快，能够提供更好的⽤户体验。 项⽬实践经验：在美团·点评的交互式⽤户⾏为分析系统中使⽤了这种⽅案，该系统主要是允许⽤户通 过Java Web系统提交数据分析统计任务，后端通过Java提交Spark作业进⾏数据分析统计。要求Spark 作业速度必须要快，尽量在10分钟以内，否则速度太慢，⽤户体验会很差。所以我们将有些Spark作业 的shufle操作提前到了Hive ETL中，从⽽让Spark直接使⽤预处理的Hive中间表，尽可能地减少Spark 的shufle操作，⼤幅度提升了性能，将部分作业的性能提升了6倍以上。

解决⽅案⼆：过滤少数导致倾斜的key

⽅案适⽤场景：如果发现导致倾斜的key就少数⼏个，⽽且对计算本身的影响并不⼤的话，那么很适合 使⽤这种⽅案。⽐如 9%的key就对应10条数据，但是只有⼀个key对应了10万数据，从⽽导致了数 据倾斜。 ⽅案实现思路：如果我们判断那少数⼏个数据量特别多的key，对作业的执⾏和计算结果不是特别重要 的话，那么⼲脆就直接过滤掉那少数⼏个key。⽐如，在Spark SQL中可以使⽤where⼦句过滤掉这些 key或者在Spark Core中对RD执⾏filter算⼦过滤掉这些key。如果需要每次作业执⾏时，动态判定哪 些key的数据量最多然后再进⾏过滤，那么可以使⽤sample算⼦对RD进⾏采样，然后计算出每个key 的数量，取数据量最多的key过滤掉即可。 ⽅案实现原理：将导致数据倾斜的key给过滤掉之后，这些key就不会参与计算了，⾃然不可能产⽣数 据倾斜。 ⽅案优点：实现简单，⽽且效果也很好，可以完全规避掉数据倾斜。 ⽅案缺点：适⽤场景不多，⼤多数情况下，导致倾斜的key还是很多的，并不是只有少数⼏个。 ⽅案实践经验：在项⽬中我们也采⽤过这种⽅案解决数据倾斜。有⼀次发现某⼀天Spark作业在运⾏的 时候突然 OM了，追查之后发现，是Hive表中的某⼀个key在那天数据异常，导致数据量暴增。因此 就采取每次执⾏前先进⾏采样，计算出样本中数据量最⼤的⼏个key之后，直接在程序中将那些key给 过滤掉。

### 解决⽅案三：提⾼shufle操作的并⾏度

⽅案适⽤场景：如果我们必须要对数据倾斜迎难⽽上，那么建议优先使⽤这种⽅案，因为这是处理数 据倾斜最简单的⼀种⽅案。 ⽅案实现思路：在对RD执⾏shufle算⼦时，给shufle算⼦传⼊⼀个参数，⽐如 reduceByKey(1 0)，该参数就设置了这个shufle算⼦执⾏时shufle read task的数量。对于Spark SQL中的shufle类语句，⽐如group by、join等，需要设置⼀个参数，即spark.sql.shufle.partitions， 该参数代表了shufle read task的并⾏度，该值默认是20，对于很多场景来说都有点过⼩。 ⽅案实现原理：增加shufle read task的数量，可以让原本分配给⼀个task的多个key分配给多个 task，从⽽让每个task处理⽐原来更少的数据。举例来说，如果原本有5个key，每个key对应10条数 据，这5个key都是分配给⼀个task的，那么这个task就要处理50条数据。⽽增加了shufle read task以 后，每个task就分配到⼀个key，即每个task就处理10条数据，那么⾃然每个task的执⾏时间都会变短 了。具体原理如下图所示。 ⽅案优点：实现起来⽐较简单，可以有效缓解和减轻数据倾斜的影响。 ⽅案缺点：只是缓解了数据倾斜⽽已，没有彻底根除问题，根据实践经验来看，其效果有限。 ⽅案实践经验：该⽅案通常⽆法彻底解决数据倾斜，因为如果出现⼀些极端情况，⽐如某个key对应的 数据量有10万，那么⽆论你的task数量增加到多少，这个对应着10万数据的key肯定还是会分配到⼀ 个task中去处理，因此注定还是会发⽣数据倾斜的。所以这种⽅案只能说是在发现数据倾斜时尝试使 ⽤的第⼀种⼿段，尝试去⽤嘴简单的⽅法缓解数据倾斜⽽已，或者是和其他⽅案结合起来使⽤。

![image 3](<Spark性能优化指南——高级篇.note_images/imageFile3.png>)

### 解决⽅案四：两阶段聚合（局部聚合+全局聚合）

⽅案适⽤场景：对RD执⾏reduceByKey等聚合类shufle算⼦或者在Spark SQL中使⽤group by语句 进⾏分组聚合时，⽐较适⽤这种⽅案。 ⽅案实现思路：这个⽅案的核⼼实现思路就是进⾏两阶段聚合。第⼀次是局部聚合，先给每个key都打 上⼀个随机数，⽐如10以内的随机数，此时原先⼀样的key就变成不⼀样的了，⽐如(helo, 1) (helo, 1) (helo, 1) (helo, 1)，就会变成(1_helo, 1) (1_helo, 1) (2_helo, 1) (2_helo, 1)。接着对打上随机数后的 数据，执⾏reduceByKey等聚合操作，进⾏局部聚合，那么局部聚合结果，就会变成了(1_helo, 2) (2_helo, 2)。然后将各个key的前缀给去掉，就会变成(helo,2)(helo,2)，再次进⾏全局聚合操作，就 可以得到最终结果了，⽐如(helo, 4)。 ⽅案实现原理：将原本相同的key通过附加随机前缀的⽅式，变成多个不同的key，就可以让原本被⼀ 个task处理的数据分散到多个task上去做局部聚合，进⽽解决单个task处理数据量过多的问题。接着去 除掉随机前缀，再次进⾏全局聚合，就可以得到最终的结果。具体原理⻅下图。 ⽅案优点：对于聚合类的shufle操作导致的数据倾斜，效果是⾮常不错的。通常都可以解决掉数据倾 斜，或者⾄少是⼤幅度缓解数据倾斜，将Spark作业的性能提升数倍以上。 ⽅案缺点：仅仅适⽤于聚合类的shufle操作，适⽤范围相对较窄。如果是join类的shufle操作，还得⽤ 其他的解决⽅案。

![image 4](<Spark性能优化指南——高级篇.note_images/imageFile4.png>)

- 1 // 第⼀步，给RDD中的每个key都打上⼀个随机前缀。

- 2 JavaPairRDD<String, Long> randomPrefixRdd = rdd.mapToPair(

- 3 new PairFunction<Tuple2<Long,Long>, String, Long>() {

- 4 private static final long serialVersionUID = 1L;

- 5 @Override

- 6 public Tuple2<String, Long> call(Tuple2<Long, Long> tuple)

- 7 throws Exception {

- 8 Random random = new Random();

- 9 int prefix = random.nextInt(10);

return new Tuple2<String, Long>(prefix + "_" + tuple._1, tuple._2);

- 10

- 11 }

- 12 });

- 13

- 14 // 第⼆步，对打上随机前缀的key进⾏局部聚合。

- 15 JavaPairRDD<String, Long> localAggrRdd = randomPrefixRdd.reduceByKey(

- 16 new Function2<Long, Long, Long>() {

- 17 private static final long serialVersionUID = 1L;

- 18 @Override

- 19 public Long call(Long v1, Long v2) throws Exception {

- 20 return v1 + v2;

- 21 }

- 22 });

- 23

- 24 // 第三步，去除RDD中每个key的随机前缀。

- 25 JavaPairRDD<Long, Long> removedRandomPrefixRdd = localAggrRdd.mapToPair(

- 26 new PairFunction<Tuple2<String,Long>, Long, Long>() {

- 27 private static final long serialVersionUID = 1L;

- 28 @Override

- 29 public Tuple2<Long, Long> call(Tuple2<String, Long> tuple)

- 30 throws Exception {

- 31 long originalKey = Long.valueOf(tuple._1.split("_")[1]);

- 32 return new Tuple2<Long, Long>(originalKey, tuple._2);

- 33 }

- 34 });

- 35

- 36 // 第四步，对去除了随机前缀的RDD进⾏全局聚合。

- 37 JavaPairRDD<Long, Long> globalAggrRdd = removedRandomPrefixRdd.reduceByKey(

- 38 new Function2<Long, Long, Long>() {

- 39 private static final long serialVersionUID = 1L;


- 40 @Override

- 41 public Long call(Long v1, Long v2) throws Exception {

- 42 return v1 + v2;

- 43 }

- 44 });

- 45


### 解决⽅案五：将reduce join转为map join

⽅案适⽤场景：在对RD使⽤join类操作，或者是在Spark SQL中使⽤join语句时，⽽且join操作中的⼀ 个RD或表的数据量⽐较⼩（⽐如⼏百M或者⼀两G），⽐较适⽤此⽅案。 ⽅案实现思路：不使⽤join算⼦进⾏连接操作，⽽使⽤Broadcast变量与map类算⼦实现join操作，进⽽ 完全规避掉shufle类的操作，彻底避免数据倾斜的发⽣和出现。将较⼩RD中的数据直接通过colect 算⼦拉取到Driver端的内存中来，然后对其创建⼀个Broadcast变量；接着对另外⼀个RD执⾏map类 算⼦，在算⼦函数内，从Broadcast变量中获取较⼩RD的全量数据，与当前RD的每⼀条数据按照连 接key进⾏⽐对，如果连接key相同的话，那么就将两个RD的数据⽤你需要的⽅式连接起来。 ⽅案实现原理：普通的join是会⾛shufle过程的，⽽⼀旦shufle，就相当于会将相同key的数据拉取到 ⼀个shufle read task中再进⾏join，此时就是reduce join。但是如果⼀个RD是⽐较⼩的，则可以采 ⽤⼴播⼩RD全量数据+map算⼦来实现与join同样的效果，也就是map join，此时就不会发⽣shufle 操作，也就不会发⽣数据倾斜。具体原理如下图所示。 ⽅案优点：对join操作导致的数据倾斜，效果⾮常好，因为根本就不会发⽣shufle，也就根本不会发⽣ 数据倾斜。 ⽅案缺点：适⽤场景较少，因为这个⽅案只适⽤于⼀个⼤表和⼀个⼩表的情况。毕竟我们需要将⼩表 进⾏⼴播，此时会⽐较消耗内存资源，driver和每个Executor内存中都会驻留⼀份⼩RD的全量数据。 如果我们⼴播出去的RD数据⽐较⼤，⽐如10G以上，那么就可能发⽣内存溢出了。因此并不适合两个 都是⼤表的情况。

![image 5](<Spark性能优化指南——高级篇.note_images/imageFile5.png>)

- 1 // ⾸先将数据量⽐较⼩的RDD的数据，collect到Driver中来。

- 2 List<Tuple2<Long, Row>> rdd1Data = rdd1.collect()

// 然后使⽤Spark的⼴播功能，将⼩RDD的数据转换成⼴播变量，这样每个Executor就只有⼀份RDD的数 据。

- 3

- 4 // 可以尽可能节省内存空间，并且减少⽹络传输性能开销。

final Broadcast<List<Tuple2<Long, Row>>> rdd1DataBroadcast = sc.broadcast(rdd1Data);

- 5

- 6

- 7 // 对另外⼀个RDD执⾏map类操作，⽽不再是join类操作。

- 8 JavaPairRDD<String, Tuple2<String, Row>> joinedRdd = rdd2.mapToPair(

- 9 new PairFunction<Tuple2<Long,String>, String, Tuple2<String, Row>>() {

- 10 private static final long serialVersionUID = 1L;

- 11 @Override

public Tuple2<String, Tuple2<String, Row>> call(Tuple2<Long, String> tuple)

- 12

- 13 throws Exception {

- 14 // 在算⼦函数中，通过⼴播变量，获取到本地Executor中的rdd1数据。

- 15 List<Tuple2<Long, Row>> rdd1Data = rdd1DataBroadcast.value();

- 16 // 可以将rdd1的数据转换为⼀个Map，便于后⾯进⾏join操作。

- 17 Map<Long, Row> rdd1DataMap = new HashMap<Long, Row>();

- 18 for(Tuple2<Long, Row> data : rdd1Data) {

- 19 rdd1DataMap.put(data._1, data._2);

- 20 }

- 21 // 获取当前RDD数据的key以及value。

- 22 String key = tuple._1;

- 23 String value = tuple._2;

- 24 // 从rdd1数据Map中，根据key获取到可以join到的数据。

- 25 Row rdd1Value = rdd1DataMap.get(key);

return new Tuple2<String, String>(key, new Tuple2<String, Row> (value, rdd1Value));

- 26

- 27 }

- 28 });

- 29

- 30 // 这⾥得提示⼀下。

- 31 // 上⾯的做法，仅仅适⽤于rdd1中的key没有重复，全部是唯⼀的场景。

// 如果rdd1中有多个相同的key，那么就得⽤flatMap类的操作，在进⾏join的时候不能⽤map，⽽是得 遍历rdd1所有数据进⾏join。

- 32

- 33 // rdd2中每条数据都可能会返回多条join后的数据。

- 34


### 解决⽅案六：采样倾斜key并分拆join操作

⽅案适⽤场景：两个RD/Hive表进⾏join的时候，如果数据量都⽐较⼤，⽆法采⽤“解决⽅案五”，那么 此时可以看⼀下两个RD/Hive表中的key分布情况。如果出现数据倾斜，是因为其中某⼀个RD/Hive 表中的少数⼏个key的数据量过⼤，⽽另⼀个RD/Hive表中的所有key都分布⽐较均匀，那么采⽤这个 解决⽅案是⽐较合适的。 ⽅案实现思路：

对包含少数⼏个数据量过⼤的key的那个RD，通过sample算⼦采样出⼀份样本来，然后统计⼀下 每个key的数量，计算出来数据量最⼤的是哪⼏个key。

然后将这⼏个key对应的数据从原来的RD中拆分出来，形成⼀个单独的RD，并给每个key都打上 n以内的随机数作为前缀，⽽不会导致倾斜的⼤部分key形成另外⼀个RD。

接着将需要join的另⼀个RD，也过滤出来那⼏个倾斜key对应的数据并形成⼀个单独的RD，将每 条数据膨胀成n条数据，这n条数据都按顺序附加⼀个0~n的前缀，不会导致倾斜的⼤部分key也形成 另外⼀个RD。

再将附加了随机前缀的独⽴RD与另⼀个膨胀n倍的独⽴RD进⾏join，此时就可以将原先相同的 key打散成n份，分散到多个task中去进⾏join了。

⽽另外两个普通的RD就照常join即可。

最后将两次join的结果使⽤union算⼦合并起来即可，就是最终的join结果。

⽅案实现原理：对于join导致的数据倾斜，如果只是某⼏个key导致了倾斜，可以将少数⼏个key分拆成 独⽴RD，并附加随机前缀打散成n份去进⾏join，此时这⼏个key对应的数据就不会集中在少数⼏个 task上，⽽是分散到多个task进⾏join了。具体原理⻅下图。 ⽅案优点：对于join导致的数据倾斜，如果只是某⼏个key导致了倾斜，采⽤该⽅式可以⽤最有效的⽅ 式打散key进⾏join。⽽且只需要针对少数倾斜key对应的数据进⾏扩容n倍，不需要对全量数据进⾏扩 容。避免了占⽤过多内存。 ⽅案缺点：如果导致倾斜的key特别多的话，⽐如成千上万个key都导致数据倾斜，那么这种⽅式也不 适合。

![image 6](<Spark性能优化指南——高级篇.note_images/imageFile6.png>)

- 1 // ⾸先从包含了少数⼏个导致数据倾斜key的rdd1中，采样10%的样本数据。

- 2 JavaPairRDD<Long, String> sampledRDD = rdd1.sample(false, 0.1);

- 3

- 4 // 对样本数据RDD统计出每个key的出现次数，并按出现次数降序排序。

- 5 // 对降序排序后的数据，取出top 1或者top 100的数据，也就是key最多的前n个数据。

- 6 // 具体取出多少个数据量最多的key，由⼤家⾃⼰决定，我们这⾥就取1个作为示范。

- 7 JavaPairRDD<Long, Long> mappedSampledRDD = sampledRDD.mapToPair(

- 8 new PairFunction<Tuple2<Long,String>, Long, Long>() {

- 9 private static final long serialVersionUID = 1L;

- 10 @Override

- 11 public Tuple2<Long, Long> call(Tuple2<Long, String> tuple)

- 12 throws Exception {

- 13 return new Tuple2<Long, Long>(tuple._1, 1L);

- 14 }

- 15 });

- 16 JavaPairRDD<Long, Long> countedSampledRDD = mappedSampledRDD.reduceByKey(

- 17 new Function2<Long, Long, Long>() {

- 18 private static final long serialVersionUID = 1L;

- 19 @Override

- 20 public Long call(Long v1, Long v2) throws Exception {

- 21 return v1 + v2;

- 22 }

- 23 });

- 24 JavaPairRDD<Long, Long> reversedSampledRDD = countedSampledRDD.mapToPair(

- 25 new PairFunction<Tuple2<Long,Long>, Long, Long>() {

- 26 private static final long serialVersionUID = 1L;

- 27 @Override

- 28 public Tuple2<Long, Long> call(Tuple2<Long, Long> tuple)

- 29 throws Exception {

- 30 return new Tuple2<Long, Long>(tuple._2, tuple._1);

- 31 }

- 32 });

- 33 final Long skewedUserid = reversedSampledRDD.sortByKey(false).take(1).get(0)._2;

- 34

- 35 // 从rdd1中分拆出导致数据倾斜的key，形成独⽴的RDD。

- 36 JavaPairRDD<Long, String> skewedRDD = rdd1.filter(

- 37 new Function<Tuple2<Long,String>, Boolean>() {

- 38 private static final long serialVersionUID = 1L;

- 39 @Override


- 40 public Boolean call(Tuple2<Long, String> tuple) throws Exception {

- 41 return tuple._1.equals(skewedUserid);

- 42 }

- 43 });

- 44 // 从rdd1中分拆出不导致数据倾斜的普通key，形成独⽴的RDD。

- 45 JavaPairRDD<Long, String> commonRDD = rdd1.filter(

- 46 new Function<Tuple2<Long,String>, Boolean>() {

- 47 private static final long serialVersionUID = 1L;

- 48 @Override

- 49 public Boolean call(Tuple2<Long, String> tuple) throws Exception {

- 50 return !tuple._1.equals(skewedUserid);

- 51 }

- 52 });

- 53

- 54 // rdd2，就是那个所有key的分布相对较为均匀的rdd。

// 这⾥将rdd2中，前⾯获取到的key对应的数据，过滤出来，分拆成单独的rdd，并对rdd中的数据使⽤ flatMap算⼦都扩容100倍。

- 55

- 56 // 对扩容的每条数据，都打上0～100的前缀。

- 57 JavaPairRDD<String, Row> skewedRdd2 = rdd2.filter(

- 58 new Function<Tuple2<Long,Row>, Boolean>() {

- 59 private static final long serialVersionUID = 1L;

- 60 @Override

- 61 public Boolean call(Tuple2<Long, Row> tuple) throws Exception {

- 62 return tuple._1.equals(skewedUserid);

- 63 }

}).flatMapToPair(new PairFlatMapFunction<Tuple2<Long,Row>, String, Row> () {

- 64

- 65 private static final long serialVersionUID = 1L;

- 66 @Override

- 67 public Iterable<Tuple2<String, Row>> call(

- 68 Tuple2<Long, Row> tuple) throws Exception {

- 69 Random random = new Random();

List<Tuple2<String, Row>> list = new ArrayList<Tuple2<String, Row>>();

- 70

- 71 for(int i = 0; i < 100; i++) {

list.add(new Tuple2<String, Row>(i + "_" + tuple._1, tuple._2));

- 72

- 73 }

- 74 return list;

- 75 }

- 76

- 77 });


- 78

- 79 // 将rdd1中分拆出来的导致倾斜的key的独⽴rdd，每条数据都打上100以内的随机前缀。

- 80 // 然后将这个rdd1中分拆出来的独⽴rdd，与上⾯rdd2中分拆出来的独⽴rdd，进⾏join。

- 81 JavaPairRDD<Long, Tuple2<String, Row>> joinedRDD1 = skewedRDD.mapToPair(

- 82 new PairFunction<Tuple2<Long,String>, String, String>() {

- 83 private static final long serialVersionUID = 1L;

- 84 @Override

- 85 public Tuple2<String, String> call(Tuple2<Long, String> tuple)

- 86 throws Exception {

- 87 Random random = new Random();

- 88 int prefix = random.nextInt(100);

return new Tuple2<String, String>(prefix + "_" + tuple._1, tuple._2);

- 89

- 90 }

- 91 })

- 92 .join(skewedUserid2infoRDD)

.mapToPair(new PairFunction<Tuple2<String,Tuple2<String,Row>>, Long, Tuple2<String, Row>>() {

- 93

- 94 private static final long serialVersionUID = 1L;

- 95 @Override

- 96 public Tuple2<Long, Tuple2<String, Row>> call(

- 97 Tuple2<String, Tuple2<String, Row>> tuple)

- 98 throws Exception {

- 99 long key = Long.valueOf(tuple._1.split("_")[1]);

return new Tuple2<Long, Tuple2<String, Row>>(key, tuple._2);

- 100

- 101 }

- 102 });

- 103

- 104 // 将rdd1中分拆出来的包含普通key的独⽴rdd，直接与rdd2进⾏join。

- 105 JavaPairRDD<Long, Tuple2<String, Row>> joinedRDD2 = commonRDD.join(rdd2);

- 106

- 107 // 将倾斜key join后的结果与普通key join后的结果，uinon起来。

- 108 // 就是最终的join结果。

- 109 JavaPairRDD<Long, Tuple2<String, Row>> joinedRDD = joinedRDD1.union(joinedRDD2);

- 110


### 解决⽅案七：使⽤随机前缀和扩容RD进⾏join

⽅案适⽤场景：如果在进⾏join操作时，RD中有⼤量的key导致数据倾斜，那么进⾏分拆key也没什么 意义，此时就只能使⽤最后⼀种⽅案来解决问题了。

⽅案实现思路：

该⽅案的实现思路基本和“解决⽅案六”类似，⾸先查看RD/Hive表中的数据分布情况，找到那个造 成数据倾斜的RD/Hive表，⽐如有多个key都对应了超过1万条数据。

然后将该RD的每条数据都打上⼀个n以内的随机前缀。

同时对另外⼀个正常的RD进⾏扩容，将每条数据都扩容成n条数据，扩容出来的每条数据都依次 打上⼀个0~n的前缀。

最后将两个处理后的RD进⾏join即可。

⽅案实现原理：将原先⼀样的key通过附加随机前缀变成不⼀样的key，然后就可以将这些处理后的“不 同key”分散到多个task中去处理，⽽不是让⼀个task处理⼤量的相同key。该⽅案与“解决⽅案六”的不 同之处就在于，上⼀种⽅案是尽量只对少数倾斜key对应的数据进⾏特殊处理，由于处理过程需要扩容 RD，因此上⼀种⽅案扩容RD后对内存的占⽤并不⼤；⽽这⼀种⽅案是针对有⼤量倾斜key的情况， 没法将部分key拆分出来进⾏单独处理，因此只能对整个RD进⾏数据扩容，对内存资源要求很⾼。 ⽅案优点：对join类型的数据倾斜基本都可以处理，⽽且效果也相对⽐较显著，性能提升效果⾮常不 错。 ⽅案缺点：该⽅案更多的是缓解数据倾斜，⽽不是彻底避免数据倾斜。⽽且需要对整个RD进⾏扩 容，对内存资源要求很⾼。 ⽅案实践经验：曾经开发⼀个数据需求的时候，发现⼀个join导致了数据倾斜。优化之前，作业的执⾏ 时间⼤约是60分钟左右；使⽤该⽅案优化之后，执⾏时间缩短到10分钟左右，性能提升了6倍。

- 1 // ⾸先将其中⼀个key分布相对较为均匀的RDD膨胀100倍。

- 2 JavaPairRDD<String, Row> expandedRDD = rdd1.flatMapToPair(

- 3 new PairFlatMapFunction<Tuple2<Long,Row>, String, Row>() {

- 4 private static final long serialVersionUID = 1L;

- 5 @Override

- 6 public Iterable<Tuple2<String, Row>> call(Tuple2<Long, Row> tuple)

- 7 throws Exception {

List<Tuple2<String, Row>> list = new ArrayList<Tuple2<String, Row>>();

- 8

- 9 for(int i = 0; i < 100; i++) {

list.add(new Tuple2<String, Row>(0 + "_" + tuple._1, tuple._2));

- 10

- 11 }

- 12 return list;

- 13 }

- 14 });

- 15

- 16 // 其次，将另⼀个有数据倾斜key的RDD，每条数据都打上100以内的随机前缀。

- 17 JavaPairRDD<String, String> mappedRDD = rdd2.mapToPair(

- 18 new PairFunction<Tuple2<Long,String>, String, String>() {

- 19 private static final long serialVersionUID = 1L;

- 20 @Override

- 21 public Tuple2<String, String> call(Tuple2<Long, String> tuple)

- 22 throws Exception {

- 23 Random random = new Random();

- 24 int prefix = random.nextInt(100);

return new Tuple2<String, String>(prefix + "_" + tuple._1, tuple._2);

- 25

- 26 }

- 27 });

- 28

- 29 // 将两个处理后的RDD进⾏join即可。

JavaPairRDD<String, Tuple2<String, Row>> joinedRDD = mappedRDD.join(expandedRDD);

- 30

- 31


### 解决⽅案⼋：多种⽅案组合使⽤

在实践中发现，很多情况下，如果只是处理较为简单的数据倾斜场景，那么使⽤上述⽅案中的某⼀种 基本就可以解决。但是如果要处理⼀个较为复杂的数据倾斜场景，那么可能需要将多种⽅案组合起来 使⽤。⽐如说，我们针对出现了多个数据倾斜环节的Spark作业，可以先运⽤解决⽅案⼀和⼆，预处理 ⼀部分数据，并过滤⼀部分数据来缓解；其次可以对某些shufle操作提升并⾏度，优化其性能；最后 还可以针对不同的聚合或join操作，选择⼀种⽅案来优化其性能。⼤家需要对这些⽅案的思路和原理都 透彻理解之后，在实践中根据各种不同的情况，灵活运⽤多种⽅案，来解决⾃⼰的数据倾斜问题。

# shufle调优

调优概述

⼤多数Spark作业的性能主要就是消耗在了shufle环节，因为该环节包含了⼤量的磁盘IO、序列化、⽹ 络数据传输等操作。因此，如果要让作业的性能更上⼀层楼，就有必要对shufle过程进⾏调优。但是 也必须提醒⼤家的是，影响⼀个Spark作业性能的因素，主要还是代码开发、资源参数以及数据倾斜， shufle调优只能在整个Spark的性能调优中占到⼀⼩部分⽽已。因此⼤家务必把握住调优的基本原则， 千万不要舍本逐末。下⾯我们就给⼤家详细讲解shufle的原理，以及相关参数的说明，同时给出各个 参数的调优建议。

ShufleManager发展概述

在Spark的源码中，负责shufle过程的执⾏、计算和处理的组件主要就是ShufleManager，也即 shufle管理器。⽽随着Spark的版本的发展，ShufleManager也在不断迭代，变得越来越先进。 在Spark 1.2以前，默认的shufle计算引擎是HashShufleManager。该ShufleManager⽽ HashShufleManager有着⼀个⾮常严重的弊端，就是会产⽣⼤量的中间磁盘⽂件，进⽽由⼤量的磁盘 IO操作影响了性能。 因此在Spark 1.2以后的版本中，默认的ShufleManager改成了SortShufleManager。 SortShufleManager相较于HashShufleManager来说，有了⼀定的改进。主要就在于，每个Task在进 ⾏shufle操作时，虽然也会产⽣较多的临时磁盘⽂件，但是最后会将所有的临时⽂件合并（merge） 成⼀个磁盘⽂件，因此每个Task就只有⼀个磁盘⽂件。在下⼀个stage的shufle read task拉取⾃⼰的 数据时，只要根据索引读取每个磁盘⽂件中的部分数据即可。 下⾯我们详细分析⼀下HashShufleManager和SortShufleManager的原理。

HashShufleManager运⾏原理

未经优化的HashShufleManager

下图说明了未经优化的HashShufleManager的原理。这⾥我们先明确⼀个假设前提：每个Executor只 有1个CPU core，也就是说，⽆论这个Executor上分配多少个task线程，同⼀时间都只能执⾏⼀个task 线程。

我们先从shufle write开始说起。shufle write阶段，主要就是在⼀个stage结束计算之后，为了下⼀个 stage可以执⾏shufle类的算⼦（⽐如reduceByKey），⽽将每个task处理的数据按key进⾏“分类”。 所谓“分类”，就是对相同的key执⾏hash算法，从⽽将相同key都写⼊同⼀个磁盘⽂件中，⽽每⼀个磁 盘⽂件都只属于下游stage的⼀个task。在将数据写⼊磁盘之前，会先将数据写⼊内存缓冲中，当内存 缓冲填满之后，才会溢写到磁盘⽂件中去。 那么每个执⾏shufle write的task，要为下⼀个stage创建多少个磁盘⽂件呢？很简单，下⼀个stage的 task有多少个，当前stage的每个task就要创建多少份磁盘⽂件。⽐如下⼀个stage总共有10个task， 那么当前stage的每个task都要创建10份磁盘⽂件。如果当前stage有50个task，总共有10个 Executor，每个Executor执⾏5个Task，那么每个Executor上总共就要创建50个磁盘⽂件，所有 Executor上会创建5 0个磁盘⽂件。由此可⻅，未经优化的shufle write操作所产⽣的磁盘⽂件的数 量是极其惊⼈的。 接着我们来说说shufle read。shufle read，通常就是⼀个stage刚开始时要做的事情。此时该stage的 每⼀个task就需要将上⼀个stage的计算结果中的所有相同key，从各个节点上通过⽹络都拉取到⾃⼰ 所在的节点上，然后进⾏key的聚合或连接等操作。由于shufle write的过程中，task给下游stage的每 个task都创建了⼀个磁盘⽂件，因此shufle read的过程中，每个task只要从上游stage的所有task所在 节点上，拉取属于⾃⼰的那⼀个磁盘⽂件即可。 shufle read的拉取过程是⼀边拉取⼀边进⾏聚合的。每个shufle read task都会有⼀个⾃⼰的bufer缓 冲，每次都只能拉取与bufer缓冲相同⼤⼩的数据，然后通过内存中的⼀个Map进⾏聚合等操作。聚合 完⼀批数据后，再拉取下⼀批数据，并放到bufer缓冲中进⾏聚合操作。以此类推，直到最后将所有数 据到拉取完，并得到最终的结果。

![image 7](<Spark性能优化指南——高级篇.note_images/imageFile7.png>)

### 优化后的HashShufleManager

下图说明了优化后的HashShufleManager的原理。这⾥说的优化，是指我们可以设置⼀个参数， spark.shufle.consolidateFiles。该参数默认值为false，将其设置为true即可开启优化机制。通常来 说，如果我们使⽤HashShufleManager，那么都建议开启这个选项。

开启consolidate机制之后，在shufle write过程中，task就不是为下游stage的每个task创建⼀个磁盘 ⽂件了。此时会出现shufleFileGroup的概念，每个shufleFileGroup会对应⼀批磁盘⽂件，磁盘⽂件 的数量与下游stage的task数量是相同的。⼀个Executor上有多少个CPU core，就可以并⾏执⾏多少个 task。⽽第⼀批并⾏执⾏的每个task都会创建⼀个shufleFileGroup，并将数据写⼊对应的磁盘⽂件 内。 当Executor的CPU core执⾏完⼀批task，接着执⾏下⼀批task时，下⼀批task就会复⽤之前已有的 shufleFileGroup，包括其中的磁盘⽂件。也就是说，此时task会将数据写⼊已有的磁盘⽂件中，⽽不 会写⼊新的磁盘⽂件中。因此，consolidate机制允许不同的task复⽤同⼀批磁盘⽂件，这样就可以有 效将多个task的磁盘⽂件进⾏⼀定程度上的合并，从⽽⼤幅度减少磁盘⽂件的数量，进⽽提升shufle write的性能。 假设第⼆个stage有10个task，第⼀个stage有50个task，总共还是有10个Executor，每个Executor执 ⾏5个task。那么原本使⽤未经优化的HashShufleManager时，每个Executor会产⽣50个磁盘⽂件， 所有Executor会产⽣5 0个磁盘⽂件的。但是此时经过优化之后，每个Executor创建的磁盘⽂件的数 量的计算公式为：CPU core的数量 * 下⼀个stage的task数量。也就是说，每个Executor此时只会创建 10个磁盘⽂件，所有Executor只会创建1 0个磁盘⽂件。

![image 8](<Spark性能优化指南——高级篇.note_images/imageFile8.png>)

## SortShufleManager运⾏原理

SortShufleManager的运⾏机制主要分成两种，⼀种是普通运⾏机制，另⼀种是bypas运⾏机制。当 shufle read task的数量⼩于等于spark.shufle.sort.bypasMergeThreshold参数的值时（默认为 20），就会启⽤bypas机制。

普通运⾏机制

下图说明了普通的SortShufleManager的原理。在该模式下，数据会先写⼊⼀个内存数据结构中，此 时根据不同的shufle算⼦，可能选⽤不同的数据结构。如果是reduceByKey这种聚合类的shufle算 ⼦，那么会选⽤Map数据结构，⼀边通过Map进⾏聚合，⼀边写⼊内存；如果是join这种普通的shufle 算⼦，那么会选⽤Aray数据结构，直接写⼊内存。接着，每写⼀条数据进⼊内存数据结构之后，就会 判断⼀下，是否达到了某个临界阈值。如果达到临界阈值的话，那么就会尝试将内存数据结构中的数 据溢写到磁盘，然后清空内存数据结构。 在溢写到磁盘⽂件之前，会先根据key对内存数据结构中已有的数据进⾏排序。排序过后，会分批将数 据写⼊磁盘⽂件。默认的batch数量是1 0条，也就是说，排序好的数据，会以每批1万条数据的形 式分批写⼊磁盘⽂件。写⼊磁盘⽂件是通过Java的BuferedOutputStream实现的。 BuferedOutputStream是Java的缓冲输出流，⾸先会将数据缓冲在内存中，当内存缓冲满溢之后再⼀ 次写⼊磁盘⽂件中，这样可以减少磁盘IO次数，提升性能。 ⼀个task将所有数据写⼊内存数据结构的过程中，会发⽣多次磁盘溢写操作，也就会产⽣多个临时⽂ 件。最后会将之前所有的临时磁盘⽂件都进⾏合并，这就是merge过程，此时会将之前所有临时磁盘 ⽂件中的数据读取出来，然后依次写⼊最终的磁盘⽂件之中。此外，由于⼀个task就只对应⼀个磁盘 ⽂件，也就意味着该task为下游stage的task准备的数据都在这⼀个⽂件中，因此还会单独写⼀份索引 ⽂件，其中标识了下游各个task的数据在⽂件中的start ofset与end ofset。 SortShufleManager由于有⼀个磁盘⽂件merge的过程，因此⼤⼤减少了⽂件数量。⽐如第⼀个stage 有50个task，总共有10个Executor，每个Executor执⾏5个task，⽽第⼆个stage有10个task。由于每 个task最终只有⼀个磁盘⽂件，因此此时每个Executor上只有5个磁盘⽂件，所有Executor只有50个磁 盘⽂件。

![image 9](<Spark性能优化指南——高级篇.note_images/imageFile9.png>)

### bypas运⾏机制

下图说明了bypas SortShufleManager的原理。bypas运⾏机制的触发条件如下：

shufle map task数量⼩于spark.shufle.sort.bypasMergeThreshold参数的值。

不是聚合类的shufle算⼦（⽐如reduceByKey）。

此时task会为每个下游task都创建⼀个临时磁盘⽂件，并将数据按key进⾏hash然后根据key的hash 值，将key写⼊对应的磁盘⽂件之中。当然，写⼊磁盘⽂件时也是先写⼊内存缓冲，缓冲写满之后再溢 写到磁盘⽂件的。最后，同样会将所有临时磁盘⽂件都合并成⼀个磁盘⽂件，并创建⼀个单独的索引 ⽂件。 该过程的磁盘写机制其实跟未经优化的HashShufleManager是⼀模⼀样的，因为都要创建数量惊⼈的 磁盘⽂件，只是在最后会做⼀个磁盘⽂件的合并⽽已。因此少量的最终磁盘⽂件，也让该机制相对未 经优化的HashShufleManager来说，shufle read的性能会更好。 ⽽该机制与普通SortShufleManager运⾏机制的不同在于：第⼀，磁盘写机制不同；第⼆，不会进⾏ 排序。也就是说，启⽤该机制的最⼤好处在于，shufle write过程中，不需要进⾏数据的排序操作，也 就节省掉了这部分的性能开销。

![image 10](<Spark性能优化指南——高级篇.note_images/imageFile10.png>)

## shufle相关参数调优

以下是Shfule过程中的⼀些主要参数，这⾥详细讲解了各个参数的功能、默认值以及基于实践经验给 出的调优建议。

spark.shufle.file.bufer

默认值：32k

参数说明：该参数⽤于设置shufle write task的BuferedOutputStream的bufer缓冲⼤⼩。将数据 写到磁盘⽂件之前，会先写⼊bufer缓冲中，待缓冲写满之后，才会溢写到磁盘。

调优建议：如果作业可⽤的内存资源较为充⾜的话，可以适当增加这个参数的⼤⼩（⽐如64k）， 从⽽减少shufle write过程中溢写磁盘⽂件的次数，也就可以减少磁盘IO次数，进⽽提升性能。在 实践中发现，合理调节该参数，性能会有1%~5%的提升。

spark.reducer.maxSizeInFlight

默认值：48m

参数说明：该参数⽤于设置shufle read task的bufer缓冲⼤⼩，⽽这个bufer缓冲决定了每次能够 拉取多少数据。

调优建议：如果作业可⽤的内存资源较为充⾜的话，可以适当增加这个参数的⼤⼩（⽐如96m）， 从⽽减少拉取数据的次数，也就可以减少⽹络传输的次数，进⽽提升性能。在实践中发现，合理调 节该参数，性能会有1%~5%的提升。

### spark.shufle.io.maxRetries

默认值：3

参数说明：shufle read task从shufle write task所在节点拉取属于⾃⼰的数据时，如果因为⽹络异 常导致拉取失败，是会⾃动进⾏重试的。该参数就代表了可以重试的最⼤次数。如果在指定次数之 内拉取还是没有成功，就可能会导致作业执⾏失败。

调优建议：对于那些包含了特别耗时的shufle操作的作业，建议增加重试最⼤次数（⽐如60次）， 以避免由于JVM的ful gc或者⽹络不稳定等因素导致的数据拉取失败。在实践中发现，对于针对超 ⼤数据量（数⼗亿~上百亿）的shufle过程，调节该参数可以⼤幅度提升稳定性。

### spark.shufle.io.retryWait

默认值：5s

参数说明：具体解释同上，该参数代表了每次重试拉取数据的等待间隔，默认是5s。

调优建议：建议加⼤间隔时⻓（⽐如60s），以增加shufle操作的稳定性。

### spark.shufle.memoryFraction

默认值：0.2

参数说明：该参数代表了Executor内存中，分配给shufle read task进⾏聚合操作的内存⽐例，默 认是20%。

调优建议：在资源参数调优中讲解过这个参数。如果内存充⾜，⽽且很少使⽤持久化操作，建议调 ⾼这个⽐例，给shufle read的聚合操作更多内存，以避免由于内存不⾜导致聚合过程中频繁读写磁 盘。在实践中发现，合理调节该参数可以将性能提升10%左右。

### spark.shufle.manager

默认值：sort

参数说明：该参数⽤于设置ShufleManager的类型。Spark 1.5以后，有三个可选项：hash、sort和 tungsten-sort。HashShufleManager是Spark 1.2以前的默认选项，但是Spark 1.2以及之后的版本 默认都是SortShufleManager了。tungsten-sort与sort类似，但是使⽤了tungsten计划中的堆外内 存管理机制，内存使⽤效率更⾼。

调优建议：由于SortShufleManager默认会对数据进⾏排序，因此如果你的业务逻辑中需要该排序 机制的话，则使⽤默认的SortShufleManager就可以；⽽如果你的业务逻辑不需要对数据进⾏排 序，那么建议参考后⾯的⼏个参数调优，通过bypas机制或优化的HashShufleManager来避免排 序操作，同时提供较好的磁盘读写性能。这⾥要注意的是，tungsten-sort要慎⽤，因为之前发现了 ⼀些相应的bug。

### spark.shufle.sort.bypasMergeThreshold

默认值：20

参数说明：当ShufleManager为SortShufleManager时，如果shufle read task的数量⼩于这个阈 值（默认是20），则shufle write过程中不会进⾏排序操作，⽽是直接按照未经优化的 HashShufleManager的⽅式去写数据，但是最后会将每个task产⽣的所有临时磁盘⽂件都合并成⼀ 个⽂件，并会创建单独的索引⽂件。

调优建议：当你使⽤SortShufleManager时，如果的确不需要排序操作，那么建议将这个参数调⼤ ⼀些，⼤于shufle read task的数量。那么此时就会⾃动启⽤bypas机制，map-side就不会进⾏排 序了，减少了排序的性能开销。但是这种⽅式下，依然会产⽣⼤量的磁盘⽂件，因此shufle write 性能有待提⾼。

spark.shufle.consolidateFiles

默认值：false

参数说明：如果使⽤HashShufleManager，该参数有效。如果设置为true，那么就会开启 consolidate机制，会⼤幅度合并shufle write的输出⽂件，对于shufle read task数量特别多的情况 下，这种⽅法可以极⼤地减少磁盘IO开销，提升性能。

调优建议：如果的确不需要SortShufleManager的排序机制，那么除了使⽤bypas机制，还可以尝 试将spark.shfle.manager参数⼿动指定为hash，使⽤HashShufleManager，同时开启consolidate 机制。在实践中尝试过，发现其性能⽐开启了bypas机制的SortShufleManager要⾼出 10%~30%。

# 写在最后的话

本⽂分别讲解了开发过程中的优化原则、运⾏前的资源参数设置调优、运⾏中的数据倾斜的解决⽅ 案、为了精益求精的shufle调优。希望⼤家能够在阅读本⽂之后，记住这些性能调优的原则以及⽅ 案，在Spark作业开发、测试以及运⾏的过程中多尝试，只有这样，我们才能开发出更优的Spark作 业，不断提升其性能。

