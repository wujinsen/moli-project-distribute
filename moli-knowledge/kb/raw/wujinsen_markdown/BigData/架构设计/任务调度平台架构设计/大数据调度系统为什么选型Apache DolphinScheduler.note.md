htps:/zhangboyi.blog.csdn.net/article/details/14017156

⼀ .背景 ⼆ .定位 三 .案例 四 .竟品对⽐ 五 .DolphinScheduler简介 六 .社区发展 七.性能数据参考 ⼀ .背景

今天跟客户聊天的时候,聊到了调度系统,抛出三个顾虑 :

调度系统现在市⾯上的调度系统那么多,⽐如⽼牌的Airflow, Oozie,Ketle,xl-job ,Spring Batch等等, 为 什么要选DolphinScheduler ? 其他的调度产品⽤的都是主流的语⾔⽐如java,调度底层都是⽤的quartz,稳定性都不错啊,功能/技术都差 不多啊,为啥选他 ? DolphinScheduler我之前都没听过,这么新的东⻄我为什么要选他,为他承担⻛险 ? 别⼈来质疑我的选型 的时候,咋怼回去 ? 仔细⼀听都没⽑病.是差不多啊. 那么为啥要选DolphinScheduler ?

在我的观念⾥⾯,没有最好的, 只有结合⾃身的业务/技术情况挑选最合适的技术产品. 那么接下来, 我从⾃身的使⽤情况来说⼀下,为什么要选型DolphinScheduler ?

注 : 本⽂的内容仅仅是个⼈观点,如果有不合适的地⽅欢迎指正… ⼆ .定位

为什么上来就说定位呢, 主要是为了缩⼩对⽐的范围.

DolphinScheduler 的定位是 ⼤数据 ⼯作流 调度 . 我把 ⼤数据 和 ⼯作流 做了重点标注. 从⽽可以知道 DolphinScheduler的定位是针对于⼤数据体系.

我这搞的是⼤数据平台, ⽬前主流的⼤数据调度组件有 : Oozie、Azkaban、Airflow 所以本⽂只是针对 这三个技术做对⽐.

注 : ketle,xl-job ,Spring batch 的定位任务调度,⾮⼤数据体系,对⼤数据体系的⽀持较弱,⽽且普遍都 ⾮HA,存在单点故障. 因为产品定位的不同,所以不做对⽐. 三 .案例

市场的认可度是检验产品价值的唯⼀真理. 我在调研技术的时候,如果市场认可度不⾼,那么基本就直接放弃了. ⽼牌调度Oozie、Azkaban、Airflow 我就不细说了,毕竟是⽼牌调度.市场的认可度和使⽤情况⼤家应该 都使⽤过或者听说过.

DolphinScheduler从2019年3⽉正式发布第⼀个开源1.0.0版本到现在, 貌似到现在才两年的时间, 所以 重点说⼀下.

Apache DolphinScheduler 部分⽤户 (排名不分先后) 看到下⾯的图我们发现很多⼤的企业在使⽤调度, 据不完全统计,⾄少有40家企业在使⽤. 官⽅统计地 址

易观千帆

360奇安信

优路科技

T3 出⾏

四 .竟品对⽐

在⼤数据领域, DolphinScheduler 对标的开源产品是 ozie、Azkaban、Airflow .

因为公司的主流开发语⾔是Java , 考虑到成本维护之类的因素,因为Airflow使⽤的语⾔是Python , 跟公 司的技术栈不匹配. 所以先排除掉 . 对⽐情况如下 :

DolphinScheduler AzkabanOozie 社区状况 所属社区 apache Linkedinapache 社区活跃度 ⾼ 中 低 稳定性 单点故障 去中⼼化的多Master和多Worker 是 单个Web和调度程序组合节点 是 HA ⽀持 (HA需要依赖ZK,资源中⼼ ⽀持 不⽀持[待确认] 过载处理 任务队列+多种任务分配策略+⾃我保护机制 任务过多服务器卡顿 任务过多服务器卡 顿 易⽤性 DAG监控界⾯ 任务状态、任务类型、重试次数、任务运⾏机器、可视化变量等关键信息⼀⽬了然 部分任务相关信息 部分任务相关信息 可视化流程定义 ⽀持 [所有的流程定义都是可视化的,通过拖拽任务来绘制DAG, 配置数据源以及资源.对于第三⽅系统,提供 api操作]否[编码配置] 否[编码配置] 快速部署 ⼀键部署 部署相对复杂 部署相对复杂 容器化部署 ⽀持 否 否 功能 是否⽀持暂停和恢复 ⽀持 否 否 是否⽀持多租户 ⽀持 否 否 任务类型 传统的shel 、python任务, 同时⽀持⼤数据平台任务调度: MR、Spark、Flink、SQL、 DataX、Sqop等等 shel、goblin、hadopJava、java、hive、pig、spark、hdfsToTeradata、 teradataToHdfs等 Pig，Hive，Sqop和Distcp，Spark 可视化数据源管理 ⽀持 不⽀持 不⽀持 可视化⽂件管理 ⽀持 [需要依赖外部存储⽐如HDFS,S3A,minIO] 不⽀持 不⽀持 ⼿动/定时触发任务 ⽀持 ⽀持 ⽀持 告警 ⽀持 [ 邮件/企业微信/钉钉/⾃扩展 ]⽀持 待确认 扩展 ⾃定义任务类型 ⽀持 ⽀持 不⽀持 ⽀持集群扩展 是

调度器使⽤分布式调度,整体的调度能⼒会随集群的规模线性增⻓,Master/Worker⽀持动态扩容/缩容 是 [相对复杂]表格中的信息如果有不严谨的地⽅,欢迎指正. 我们在看⼀下DolphinScheduler技术栈 :

后端: SpringBot (2.x) 前端: VUE 编译: Maven(3.3+) , 元数据存储: Mysql5.5+ 分布式⽆中⼼化设计: ZoKeper(3.4.6+) 统⼀资源管理 : 共享存储[HDFS、S3A、MinIO] 主流技术栈,⼆次开发基本零⻔槛 .

五 .DolphinScheduler简介

Apache DolphinScheduler 于 17 年在易观数科⽴项， 19 年 3 ⽉开源，8 ⽉进⼊ Apache 孵化器， 已 累计有 40+ 公司在⽣产上使⽤.

⼀个分布式易扩展的可视化DAG⼯作流任务调度系统。致⼒于解决数据处理流程中错综复杂的依赖关 系，使调度系统在数据处理流程中开箱即⽤。 其主要⽬标如下：

* 以DAG图的⽅式将Task按照任务的依赖关系关联起来，可实时可视化监控任务的运⾏状态 * ⽀持丰富的任务类型：Shel、MR、Spark、SQL(mysql、postgresql、hive、 sparksql),Python,Sub_Proces、Procedure等

* ⽀持⼯作流定时调度、依赖调度、⼿动调度、⼿动暂停/停⽌/恢复，同时⽀持失败重试/告 警、- 从指定节点恢复失败、Kil任务等操作

- * ⽀持⼯作流优先级、任务优先级及任务的故障转移及任务超时告警/失败
- * ⽀持⼯作流全局参数及节点⾃定义参数设置
- * ⽀持资源⽂件的在线上传/下载，管理等，⽀持在线⽂件创建、编辑
- * ⽀持任务⽇志在线查看及滚动、在线下载⽇志等
- * 实现集群HA，通过Zokeper实现Master集群和Worker集群去中⼼化
- * ⽀持对Master/Worker cpu load，memory，cpu在线查看
- * ⽀持⼯作流运⾏历史树形/⽢特图展示、⽀持任务状态统计、流程状态统计
- * ⽀持补数
- * ⽀持多租户
- * 其他 .


- 9
- 10 1


- 12
- 13 系统优势


主要能⼒

可视化流程

K8s ⽀持

六 .社区发展

DolphinScheduler为国内开源项⽬,相对其他国外项⽬,有天然的本⼟优势. 截⽌⽬前 DolphinScheduler 共建⽴了8个⽤户群,1个开发种⼦群,1个开发者群. 总⼈数4 0+, 截⽌⽬前 对DolphinScheduler做过贡献的开发者有 20+ . 社区活跃度很⾼.

⽬前开源对⽐情况如下(数据统计时间截⽌⽉2021年2⽉底):

项⽬名称 Start数量 Fork数量 Isue数量 Contributors 数量 DolphinScheduler 5.1k1.8k4862 180 ⼈ Azkaban3.6k 1.4k2769 104⼈ Oozie 0.59k 0.43k – 16⼈ Airflow 20.5k 8k 2837 2836⼈ 七.性能数据参考

⽣产环境 易观千帆是每天需要处理数百亿条数据，⽉活 6.2亿，6.8 PB 的⼤数据集群经过每天上万个任务 ETL 处理加⼯⽽产⽣的 SaS 服务应⽤。

压测 因为执⾏任务的类型不同,所消耗的cpu/内存/⽹络/磁盘的资源也不同,因为执⾏⼤数据体系任务都是将 任务统⼀提交到Yarn集群,仅仅是提交⼀个shel指令. 为了模拟, 所以该测试数据仅为执⾏shel脚本, 数 据仅为参考,请以实际环境为准.

机配置为 : 5台物理 16核 32g内存 千兆⽹卡.

线程总数: 16 * 5 = 80 内存总数: 32 * 5 = 160G

压测结果:

并⾏任务数量在 8 0+ 数据库连接数据: 10+

cpu占⽤ 10% + 内存占⽤ 40%+

调整参数: master.exec.threads=6 0 master.exec.task.num=20

worker.exec.threads=6 0

内存配置: Api : 4G Master : 4G Worker: 4G

- 1
- 2


- 1

- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21


- 2


- 23
- 24
- 25
- 26 参考资料来源清单:


htps:/github.com/apache/incubator-dolphinscheduler htps:/github.com/apache/incubator-dolphinscheduler-website htps:/mp.weixin.q.com/s/s0oi5woJs_gb1Sgkp1jkyA htps:/blog.csdn.net/DolphinScheduler/article/details/1217473 htps:/github.com/azkaban/azkaban htps:/github.com/apache/airflow

⸻版权声明：本⽂为CSDN博主「张伯毅」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原⽂ 出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/zhanglong_ 4/article/details/14017156

