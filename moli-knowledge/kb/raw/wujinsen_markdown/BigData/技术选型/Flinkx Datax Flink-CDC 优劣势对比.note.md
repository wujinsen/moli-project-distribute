htps:/blog.csdn.net/HiBoyljw/article/details/1932859

⼀、FlinkX简介 FlinkX是⼀个基于Flink的批流统⼀的数据同步⼯具，既可以采集静态的数据，⽐如MySQL，HDFS等， 也可以采集实时变化的数据，⽐如MySQL binlog，Kafka等。FlinkX⽬前包含下⾯这些特性：

- ● ⼤部分插件⽀持并发读写数据，可以⼤幅度提⾼读写速度；
- ● 部分插件⽀持失败恢复的功能，可以从失败的位置恢复任务，节约运⾏时间；失败恢复
- ● 关系数据库的Reader插件⽀持间隔轮询功能，可以持续不断的采集变化的数据；间隔轮询
- ● 部分数据库⽀持开启Kerberos安全认证；Kerberos
- ● 可以限制reader的读取速度，降低对业务数据库的影响；
- ● 可以记录writer插件写数据时产⽣的脏数据；
- ● 可以限制脏数据的最⼤数量；
- ● ⽀持多种运⾏模式； FlinkX⽬前⽀持下⾯这些数据库：

github：htps:/github.com/oceanos/flinkx ⼆、DataX简介 DataX 是⼀个异构数据源离线同步⼯具，致⼒于实现包括关系型数据库(MySQL、Oracle等)、HDFS、 Hive、ODPS、HBase、FTP等各种异构数据源之间稳定⾼效的数据同步功能。

- ● 设计理念 为了解决异构数据源同步问题，DataX将复杂的⽹状的同步链路变成了星型数据链路，DataX作为中间 传输载体负责连接各种数据源。当需要接⼊⼀个新的数据源的时候，只需要将此数据源对接到DataX， 便能跟已有的数据源做到⽆缝数据同步。
- ● 当前使⽤现状 DataX在阿⾥巴巴集团内被⼴泛使⽤，承担了所有⼤数据的离线同步业务，并已持续稳定运⾏了6年之 久。⽬前每天完成同步8w多道作业，每⽇传输数据量超过30TB。 DataX本身作为离线数据同步框架，将数据源读取和写⼊抽象成为Reader/Writer插件，纳⼊到整个同 步框架中。


Reader：Reader为数据采集模块，负责采集数据源的数据，将数据发送给Framework。 Writer： Writer为数据写⼊模块，负责不断向Framework取数据，并将数据写⼊到⽬的端。

Framework：Framework⽤于连接reader和writer，作为两者的数据传输通道，并处理缓冲，流控，并 发，数据转换等核⼼技术问题。 github：htps:/github.com/alibaba/DataX 三、FlinkX与DataX对⽐ 在Flink的⽣态圈⾥⾯与DataX对标的就是FlinkX，有可能就是同⼀批开发⼈员。 相同点：

- ● ⽀持多种数据库的数据同步
- ● ⽀持⾼并发数据读写
- ● ⽀持⼤批量数据批量读写 不同点：
- ● DataX任务是单机多线程的，资源占⽤多。FlinX提交Flink 任务⾄Flink 集群，可以分布式运⾏，且 可以使⽤Yarn进⾏任务调度，与Hadop⽣态⽆缝结合。
- ● DataX只⽀持离线批处理。FlinkX⽀持批处理和部分数据库的流式处理。 总结： FlinkX与DataX 都数据⼤数据数据同步的第三⽅插件，但是FlinX很多功能都是对标DataX的，并在其原 有基础上进⾏了改进。DataX是⼀个单机同步⼯具，核⼼底层通道的分布式⽀持不友好,⽽FlinkX任务是 基于Flink集群的，可以⽆缝结合Hadop⽣态。所以在分布式、⾼效性和易于拓展⽅⾯ FlinX是优于 DataXD的。

四、Flink-CDC 简介 Flink CDC 连接器是 Apache Flink 的⼀组源连接器，使⽤变更数据捕获 (CDC) 从不同数据库中获取变 更。

- ● ⽀持读取数据库快照并继续读取binlog，即使发⽣故障也只处理⼀次。
- ● DataStream API 的 CDC 连接器，⽤户可以在单个作业中使⽤对多个数据库和表的更改，⽽⽆需部 署 Debezium 和 Kafka。
- ● Table/SQL API 的 CDC 连接器，⽤户可以使⽤ SQL DL 创建 CDC 源来监视单个表上的更改。

github：htps:/github.com/oceanos/flinkx/blob/1.8_release/docs/rdbwriter.md

五、FlinkX与Flink-CDC对⽐ 相同点：

- ● 都是基于Flink,提交Flink 任务
- ● 对MySQL/Kafka/Postgres 都⽀持流式变更(CDC),数据实时变更、实时抓取
- ● 针对流式处理都是基于⽇志的CDC


不同点：

- ● FlinkX属于第三⽅插件，⽽Flink-CDC属于Flink的⼀组连接器。相对⽽⾔，Flink-CDC更轻，且使⽤ 更⽅便。
- ● FlinkX⽀持的数据源更多，Flink-CDC⽬前只⽀持MySQL/Kafka/Postgres的实时CDC
- ● FlinkX⽀持批处理和流处理、Flink-CDC只⽀持流处理。

实时处理： 如果需要对数据同步采⽤实时处理的话，由于云⼤学数据是存储在Mysql中，所以⽆论是FlinkX和 Flink-CDC都是⽀持的。它们的底层都是基于Debezium 的binlog来进⾏数据同步的。但是由于FlinkX 属于第三⽅插件，⽽Flink-CDC属于Flink的⼀组连接器。相对⽽⾔，Flink-CDC更轻，且使⽤更⽅便。 缺点：

- ● 需要修改binlog模式，⽇志存储空间需要增⼤。

离线批处理： 如果需要采⽤离线批处理⽅式，建议采⽤FlinkX。因为其⾼效、稳定，且⽀持多种数据源。 缺点：

- ● 采⽤离线批处理的话，其本质是基于查询的CDC。与基于⽇志的CDC还是有所区别的，具体对⽐如 下：

总结：

- ● 实时数据同步带来的⻛险是需要修改binlog模式，导致⽇志存储空间增⼤。且⽀持的数据库⽐较 少。
- ● 离线批量同步的本质是基于查询的CDC，需要部署第三⽅插件，同时其带来的⻛险是⽆法实时捕获 所有数据变化、相对基于⽇志的CDC延迟性更⾼、⾼频率和⼤批量的数据同步会增加数据库的负担， 因为它在同步的时候需要⼀直去连接数据库进⾏数据查询、⽆法捕获删除事件和旧记录状态。


⸻版权声明：本⽂为CSDN博主「HiBoyljw」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原⽂ 出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/HiBoyljw/article/details/1932859

