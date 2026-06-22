问题导读

- 1、当前集群的可⽤资源不能满⾜应⽤程序的需求，怎么解决？
- 2、内存⾥堆的东⻄太多了，有什么好办法吗？


- 1、WARN TaskSchedulerImpl: Initial job has not accepted any resources; check your cluster uito ensure that workers are registered and have sufficient memory 当前的集群的可⽤资源不能满⾜应⽤程序所请求的资源。 资源分2类： cores 和 ram Core代表对执⾏可⽤的executor slots Ram代表每个Worker上被需要的空闲内存来运⾏你的Application。

解决⽅法：

应⽤不要请求多余空闲可⽤资源的 关闭掉已经执⾏结束的Application

- 2、Application isn’t using all of the Cores: How to set the Cores used by a Spark App 设置每个App所能获得的core

解决⽅法：

spark-env.sh⾥设置spark.deploy.defaultCores 或 spark.cores.max

- 3、Spark Executor OOM: How to set Memory Parameters on Spark OOM是内存⾥堆的东⻄太多了


- 1、增加job的并⾏度，即增加job的partition数量，把⼤数据集切分成更⼩的数据，可以减少⼀次性load到内存中的数 据量。InputFomart， getSplit来确定。

- 2、spark.storage.memoryFraction 管理executor中RDD和运⾏任务时的内存⽐例，如果shuffle⽐较⼩，只需要⼀点点shuffle memory，那么就调⼤这个 ⽐例。默认是0.6。不能⽐⽼年代还要⼤。⼤了就是浪费。

- 3、spark.executor.memory如果还是不⾏，那么就要加Executor的内存了，改完executor内存后，这个需要重启。

- 4、Shark Server/ Long Running Application Metadata Cleanup Spark程序的元数据是会往内存中⽆限存储的。spark.cleaner.ttl来防⽌OOM，主要出现在Spark Steaming和Shark Server⾥。 export SPARK_JAVA_OPTS +="-Dspark.kryoserializer.buffer.mb=10 -Dspark.cleaner.ttl=43200"


- 5、Class Not Found: Classpath Issues


- 问题1、缺少jar，不在classpath⾥。

- 问题2、jar包冲突，同⼀个jar不同版本。


- 解决1： 将所有依赖jar都打⼊到⼀个fatJar包⾥，然后⼿动设置依赖到指定每台机器的DIR。 val conf = new SparkConf().setAppName(appName).setJars(Seq(System.getProperty("user.dir") + "/target/scala2.10/sparktest.jar"))

- 解决2： 把所需要的依赖jar包都放到default classpath⾥，分发到各个worker node上。


关于性能优化： 第⼀个是sort-based shuffle。这个功能⼤⼤的减少了超⼤规模作业在shuffle⽅⾯的内存占⽤量，使得我们可以⽤更多 的内存去排序。 第⼆个是新的基于Netty的⽹络模块取代了原有的NIO⽹络模块。这个新的模块提⾼了⽹络传输的性能，并且脱离JVM 的GC⾃⼰管理内存，降低了GC频率。 第三个是⼀个独⽴于Spark executor的external shuffle service。这样⼦executor在GC的时候其他节点还可以通过这个 service来抓取shuffle数据，所以⽹络传输本身不受到GC的影响。

过去⼀些的参赛系统软件⽅⾯的处理都没有能⼒达到硬件的瓶颈，甚⾄对硬件的利⽤率还不到10%。⽽这次我们的参 赛系统在map期间⽤满了3GB/s的硬盘带宽，达到了这些虚拟机上⼋块SSD的瓶颈，在reduce期间⽹络利⽤率到了 1.1GB/s，接近物理极限。

