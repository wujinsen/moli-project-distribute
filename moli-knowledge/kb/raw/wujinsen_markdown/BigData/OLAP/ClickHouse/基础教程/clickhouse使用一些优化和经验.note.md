htp:/events.jianshu.io/p/52f31fafc2a

# ⼀些经验

- 1，查询强烈要求带上分区键过滤和主键过滤，如 where day = today() and itime = now()。
- 2，建表的时候，选择合适的分区键和排序键是优化的关键。
- 3，如果不允许重复主键(且不要求去重时效性)，建议使⽤表类型：ReplicatedReplacingMergeTre 建 表语句可参考 ,注 意只能保证单节点的数据不重复，⽆法保证集群的。
- 4，如果要对某⼀列过滤，且该列⾮partition key和orderby key, 且该列过滤前后数据量差异较⼤，建 议使⽤prewhere clause过滤。参考：

。

- 5，⽇期和时间使⽤Date, DateTime类型，不要⽤String类型。
- 6，建表时，强烈建议低基数(基数⼩于1 0)且类型为String的列，使⽤LowCardinality特性，例如 国家(country)，操作系统(os)皆可⽤LowCardinality。查询效益提⾼可以40~50%，具体参考

。

- 7，为了使复杂查询尽量本地完成，提前减⼩数据量和⽹络传输，加快查询速度，创建分布式表时，尽 量按照主键hash分shard。例如欲加快select count(distinct uid) from table_al group by country, os 的查询速度. 创建分布式表table_al时，shard key为cityHash64(country, os)，hash函数参考

。

- 8，计算不同维度组合的指标值时，⽤with rolup或with cube替代union al⼦句。
- 9，建表时，请遵守命名规范：分布式表名 = 本地表名 + 后缀"_al"。 select请直接操作分布式表。
- 10，官⽅已经指出Nulable类型⼏乎总是会拖累性能，因为存储Nulable列时需要创建⼀个额外的⽂件 来存储NUL的标记，并且Nulable列⽆法被索引。因此除⾮极特殊情况，应直接使⽤字段默认值表示 空，或者⾃⾏指定⼀个在业务中⽆意义的值（例如⽤-1表示没有商品ID）


htps:/clickhouse.yandex/docs/en/operations/table_engines/replacingmergetre/

htps:/clickhouse.yandex/docs/en/query_language/select/#pre where-clause

htps:/al tinity.com/blog/2019/3/27/low-cardinality

htps:/cl ickhouse.tech/docs/en/sql-reference/functions/hash-functions/

1，稀疏索引不同于mysql的B+树，不存在最左的原则，所以在ck查询的时候，where条件中，基数较 ⼤的列（即区分度较⾼的列）在前，基数较⼩的列（区分度较低的列）在后。

- 12，多表Join时要满⾜⼩表在右的原则，右表关联时被加载到内存中与左表进⾏⽐较
- 13，多维分析, 查询列不宜过多, 过滤条件带上分区筛选 (select dim1, dim2, ag1( x), ag2( x) from table where x group by dim1, dim2 )
- 14，禁⽌SELECT *, 不能拉取原始数据 ! (clickhouse不是数据仓库, 纯粹是拉原始表数据的查询应该 禁⽌,如 select a, b, c, f, e, country from x )


# 分区键和排序键

分区键和排序键理论上不能修改，在建表建库的时候尽量考虑清楚。

- 0，事实表必须分区，分区粒度根据业务特点决定，不宜过粗或过细。我们当前都是按天分区，按⼩ 时、周、⽉分区也⽐较常⻅（系统表中的query_log、trace_log表默认就是按⽉分区的）。
- 1，分区键能过滤⼤量数据，分区键建议使⽤to YMD()按天分区，如果数据量很少，10w左 右，建议使⽤to YM()按⽉分区，过多的分区会占⽤⼤量的资源，会对集群的稳定性造成很⼤的影 响。
- 2，分区键必须使⽤date和datetime字段，避免string类型的分区键
- 3，每个sql必须要⽤分区键，否则会导致⼤量的数据被读取，到了集群的内存限制直接拒绝
- 4，排序键也是⼀个⾮常重要的过滤条件，考虑到ck是OLAP 库，排序键默认也是ck的主键，loap库建 议分区键要使⽤基数⽐较少的字段，⽐如country就⽐timestramp要好。
- 5，不要使⽤过⻓的分区键，主键 。
- 6，CK的索引⾮MySQL的B树索引，⽽是类似Kafka log⻛格的稀疏索引，故不⽤考虑最左原则，但是 建议基数较⼤的列（即区分度较⾼的列）在前，基数较⼩的列（区分度较低的列）在后。另外，基数 特别⼤的列（如订单ID等）不建议直接⽤作索引。


# 分区数

分区数过多会导致⼀些致命的集群问题。不建议分区数粒度过细，不建议分区数过多，经验来看，10 亿数据建议1-10个分区差不多了，当然需要参考你的硬件资源如何。

- 1，select 查询性能降低，分区数过多会导致打开⼤量⽂件句柄，影响集群。
- 2，分区数过多会导致集群重启变慢，zk压⼒变⼤，insert变慢等问题。 htps:/clickhouse.tech/docs/en/engines/table-engines/mergetre-family/custom-partitioning-key/


