还记得我们的worcount的案例，在加载⽂件的时候使⽤了以下代码： val lines = sc.textFile("/rot/content.log",2) ，这⾥的变量sc就是SparkContext的实例。通常⽽⾔， Driver Aplication的执⾏与输出都是通过SparkContext来完成的。

在正式提交Aplication之前，⾸先需要初始化SparkConxt。SparkConxt隐藏了⽹络通信、分布式部 署、消息通信、存储能⼒、计算能⼒、缓存、测量系统、⽂件服务、Web服务等内容，应⽤程序只需 要使⽤SparkContext提供的API完成功能的开发。关于隐藏内容的线程启动，可以参考本系列的第3篇 ⽂章。 SparkContext内置的DAGScheduler负责创建Job，将DAG中的RD划分到不同的Stage，提交Stage等 功能；内置的TaskScheduler负责资源的申请、任务的提交及请求集群对任务的调度等⼯作。

SparkContext的初始化步骤如下：

- 1、创建Spark执⾏环境SparkEnv。
- 2、创建RD清理器metadataCleaner。
- 3、创建并初始化Spark UI。
- 4、Hadop相关配置及Executor环境变量的设置。
- 5、创建任务调度TaskScheduler。
- 6、创建和启动DAGScheduler。
- 7、TaskScher的启动。
- 8、初始化块管理器BlockManager（BlockManager是存储体系的主要组件之⼀）

- 9、启动测量系统MetricsSystem。Metrics是⼀个给JAVA服务的各项指标提供度量⼯具的包，在JAVA 代码中嵌⼊Metrics代码，可以⽅便的对业务代码的各个指标进⾏监控，同时，Metrics能够很好的跟 Ganlia、Graphite结合，⽅便的提供图形化接⼝。
- 10、创建和启动Executor分配管理器ExecutorAlocationManager 1、ContextCleaner的创建与启动


- 12、Spark环境更新
- 13、创建DAGSchedulerSource和BolckManagerSource；
- 14、将SparkContext标记为激活。


待续 .

声明：本系列博⽂是在学习耿嘉安《深⼊理解Spark 核⼼思想与源码分析》、⾼彦杰《Spark⼤数据处理》、张安战 《Spark技术内幕》及互联⽹公开博客资料后，摘抄或者拷⻉相关内容整理⽽成，个别知识点会有⾃⼰的理解并输 出。欢迎转载、使⽤、重新发布，但务必保留相关图书的信息，并且不得⽤于商业⽬的，基于本⽂修改后的作品务必 以相同的声明及许可发布。如有任何疑问，请与我联系。

# 技术讨论群： 138712835（需付费-会定期以发群红包的⽅式，将⼊群⾦额返回到群⾥⾯，本⼈不赚取 ⼀分钱）

