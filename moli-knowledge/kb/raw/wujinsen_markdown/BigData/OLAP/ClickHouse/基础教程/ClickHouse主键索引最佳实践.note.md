htps:/clickhouse.com/docs/zh/guides/improving-query-performance/sparse-primary-indexes

htps:/clickhouse.com/docs/zh/guides/best-practices

在本⽂中，我们将深⼊研究ClickHouse索引。我们将对此进⾏详细说明和讨论：

ClickHouse的索引与传统的关系数据库有何不同

ClickHouse是怎样构建和使⽤主键稀疏索引的

ClickHouse索引的最佳实践

您可以选择在⾃⼰的机器上执⾏本⽂给出的所有Clickhouse SQL语句和查询。 如何安装和搭建 ClickHouse请查看 NOTE 这篇⽂章主要关注稀疏索引。 如果想了解 ，请查看 .

快速上⼿

⼆级跳数索引 教程

# 数据集

在本⽂中，我们将使⽤⼀个匿名的web流量数据集。

我们将使⽤样本数据集中的 87万⾏(事件)的⼦集。

未压缩的数据⼤⼩为 87万个事件和⼤约70mb。当存储在ClickHouse时，压缩为20mb。

在我们的⼦集中，每⾏包含三列，表示在特定时间(EventTime列)单击URL (URL列)的互联⽹⽤户 (UserID列)。

通过这三个列，我们已经可以制定⼀些典型的web分析查询，如：

某个⽤户点击次数最多的前10个url是什么？

点击某个URL次数最多的前10名⽤户是谁？

⽤户点击特定URL的最频繁时间(⽐如⼀周中的⼏天)是什么？

测试环境

本⽂档中给出的所有运⾏时数据都是在带有Aple M1 Pro芯⽚和16GB RAM的MacBok Pro上本地运⾏ ClickHouse 2.2.1。

全表扫描

为了了解在没有主键的情况下如何对数据集执⾏查询，我们通过执⾏以下SQL DL语句(使⽤ MergeTre表引擎)创建了⼀个表：

CREATE TABLE hits_NoPrimaryKey( `UserID` UInt32, `URL` String, `EventTime` DateTime)ENGINE = MergeTreePRIMARY KEY tuple();

1

接下来，使⽤以下插⼊SQL将命中数据集的⼀个⼦集插⼊到表中。这个SQL使⽤ 和 从clickhouse.com加载⼀个数据集的⼀部分数据：

URL表函数 类型推断

INSERT INTO hits_NoPrimaryKey SELECT intHash32(c11::UInt64) AS UserID, c15 AS URL, c5 AS EventTimeFROM url('https://datasets.clickhouse.com/hits/tsv/hits_v1.tsv.xz')WHERE URL != '';

1

结果：

Ok.0 rows in set. Elapsed: 145.993 sec. Processed 8.87 million rows, 18.40 GB (60.78 thousand rows/s., 126.06 MB/s.)

1

ClickHouse客户端输出了执⾏结果，插⼊了 87万⾏数据。 最后，为了简化本⽂后⾯的讨论，并使图表和结果可重现，我们使⽤FINAL关键字 该表：

optimize

1 OPTIMIZE TABLE hits_NoPrimaryKey FINAL;

NOTE ⼀般来说，不需要也不建议在加载数据后⽴即执⾏optimize。对于这个示例，为什么需要这样做是很明 显的。 现在我们执⾏第⼀个web分析查询。以下是⽤户id为74927693的互联⽹⽤户点击次数最多的前10个 url：

SELECT URL, count(URL) as CountFROM hits_NoPrimaryKeyWHERE UserID = 749927693GROUP BY URLORDER BY Count DESCLIMIT 10;

1

结果：

┌─URL────────────────────────────┬─Count─┐│ http://auto.ru/chatay-barana.. │ 170 ││ http://auto.ru/chatay-id=371...│ 52 ││ http://public_search │ 45 ││ http://kovrik-medvedevushku-...│ 36 ││ http://forumal

1

│ 33 ││ http://korablitz.ru/L_1OFFER...│ 14 ││ http://auto.ru/chatayid=371...│ 14 ││ http://auto.ru/chatay-john-D...│ 13 ││ http://auto.ru/chatay-john-D...│ 10 ││ http://wot/html?page/23600_m...│ 9 │└────────────────────────────────┴───────┘10 rows in set. Elapsed: 0.022 sec.Processed 8.87 million rows, 70.45 MB (398.53 million rows/s., 3.17 GB/s.)

ClickHouse客户端输出表明，ClickHouse执⾏了⼀个完整的表扫描！我们的表的 87万⾏中的每⼀⾏ 都被加载到ClickHouse中，这不是可扩展的。 为了使这种(⽅式)更有效和更快，我们需要使⽤⼀个具有适当主键的表。这将允许ClickHouse⾃动(基 于主键的列)创建⼀个稀疏的主索引，然后可以⽤于显著加快我们示例查询的执⾏。

# 包含主键的表

创建⼀个包含联合主键UserID和URL列的表：

CREATE TABLE hits_UserID_URL( `UserID` UInt32, `URL` String, `EventTime` DateTime)ENGINE = MergeTreePRIMARY KEY (UserID, URL)ORDER BY (UserID, URL, EventTime)SETTINGS index_granularity = 8192, index_granularity_bytes = 0;

1

DL详情 上⾯ DL语句中的主键会基于两个指定的键列创建主索引。插⼊数据：

INSERT INTO hits_UserID_URL SELECT intHash32(c11::UInt64) AS UserID, c15 AS URL, c5 AS EventTimeFROM url('https://datasets.clickhouse.com/hits/tsv/hits_v1.tsv.xz')WHERE URL != '';

1

结果：

0 rows in set. Elapsed: 149.432 sec. Processed 8.87 million rows, 18.40 GB (59.38 thousand rows/s., 123.16 MB/s.)

1

optimize表：

1 OPTIMIZE TABLE hits_UserID_URL FINAL;

我们可以使⽤下⾯的查询来获取关于表的元数据：

SELECT part_type, path, formatReadableQuantity(rows) AS rows, formatReadableSize(data_uncompressed_bytes) AS data_uncompressed_bytes, formatReadableSize(data_compressed_bytes) AS data_compressed_bytes, formatReadableSize(primary_key_bytes_in_memory) AS primary_key_bytes_in_memory,

1

marks, formatReadableSize(bytes_on_disk) AS bytes_on_diskFROM system.partsWHERE (table = 'hits_UserID_URL') AND (active = 1)FORMAT Vertical;

结果：

part_type: Widepath:

1

./store/d9f/d9f36a1a-d2e6-46d4-8fb5-ffe9ad0d5aed/all_1_9_2/rows: 8.87 milliondata_uncompressed_bytes: 733.28

MiBdata_compressed_bytes: 206.94 MiBprimary_key_bytes_in_memory: 96.93 KiBmarks: 1083bytes_on_disk: 207.07 MiB1 rows in set. Elapsed: 0.003 sec.

客户端输出表明：

表数据以

wide format

存储在⼀个特定⽬录，每个列有⼀个数据⽂件和mark⽂件。

表有 87万⾏数据。

未压缩的数据有73.28 MB。

压缩之后的数据有206.94 MB。

有1083个主键索引条⽬，⼤⼩是96.93 KB。

在磁盘上，表的数据、标记⽂件和主索引⽂件总共占⽤207.07 MB。

# 针对海量数据规模的索引设计

在传统的关系数据库管理系统中，每个表⾏包含⼀个主索引。对于我们的数据集，这将导致主索引⸺ 通常是⼀个 的数据结构⸺包含 87万个条⽬。 这样的索引允许快速定位特定的⾏，从⽽提⾼查找点查和更新的效率。在B(+)-Tre数据结构中搜索⼀ 个条⽬的平均时间复杂度为O(log2n)。对于⼀个有 87万⾏的表，这意味着需要23步来定位任何索引 条⽬。 这种能⼒是有代价的:额外的磁盘和内存开销，以及向表中添加新⾏和向索引中添加条⽬时更⾼的插⼊ 成本(有时还需要重新平衡B-Tre)。 考虑到与B-Te索引相关的挑战，ClickHouse中的表引擎使⽤了⼀种不同的⽅法。ClickHouse

B(+)-Tre

MergeTr e Engine

引擎系列被设计和优化⽤来处理⼤量数据。 这些表被设计为每秒接收数百万⾏插⼊，并存储⾮常⼤(10 pb)的数据量。 数据被 快速写⼊表中，并在后台应⽤合并规则。 在ClickHouse中，每个数据部分（data part）都有⾃⼰的主索引。当他们被合并时，合并部分的主索 引也被合并。 在⼤规模中情况下，磁盘和内存的效率是⾮常重要的。因此，不是为每⼀⾏创建索引，⽽是为⼀组数 据⾏（称为颗粒（granule））构建⼀个索引条⽬。 之所以可以使⽤这种稀疏索引，是因为ClickHouse会按照主键列的顺序将⼀组⾏存储在磁盘上。 与直接定位单个⾏(如基于B-Tre的索引)不同，稀疏主索引允许它快速(通过对索引项进⾏⼆分查找)识 别可能匹配查询的⾏组。 然后潜在的匹配⾏组(颗粒)以并⾏的⽅式被加载到ClickHouse引擎中，以便找到匹配的⾏。 这种索引设计允许主索引很⼩(它可以⽽且必须完全适合主内存)，同时仍然显著加快查询执⾏时间：特 别是对于数据分析⽤例中常⻅的范围查询。 下⾯详细说明了ClickHouse是如何构建和使⽤其稀疏主索引的。在本⽂后⾯，我们将讨论如何选择、 移除和排序⽤于构建索引的表列(主键列)的⼀些最佳实践。

⼀批⼀批的

# 数据按照主键排序存储在磁盘上

上⾯创建的表有：

联合 主键

(UserID, URL)

联合

排序键

(UserID, URL, EventTime)

。

NOTE

如果我们只指定了排序键，那么主键将隐式定义为排序键。

为了提⾼内存效率，我们显式地指定了⼀个主键，只包含查询过滤的列。基于主键的主索引被完全 加载到主内存中。

为了上下⽂的⼀致性和最⼤的压缩⽐例，我们单独定义了排序键，排序键包含当前表所有的列（和 压缩算法有关，⼀般排序之后⼜更好的压缩率）。

如果同时指定了主键和排序键，则主键必须是排序键的前缀。

插⼊的⾏按照主键列(以及排序键的附加EventTime列)的字典序(从⼩到⼤)存储在磁盘上。 NOTE ClickHouse允许插⼊具有相同主键列的多⾏数据。在这种情况下(参⻅下图中的第1⾏和第2⾏)，最终 的顺序是由指定的排序键决定的，这⾥是EventTime列的值。 如下图所示：ClickHouse是 。

列存数据库

在磁盘上，每个表都有⼀个数据⽂件(*.bin)，该列的所有值都以

压缩

格式存储，并且

在这个例⼦中，这 87万⾏按主键列(以及附加的排序键列)的字典升序存储在磁盘上

UserID 第⼀位， 然后是

URL

，

最后是

EventTime

：UserID.bin，URL.bin，和EventTime.bin是

UserID， URL，和 EventTime列的数据⽂件。 NOTE

因为主键定义了磁盘上⾏的字典顺序，所以⼀个表只能有⼀个主键。

我们从0开始对⾏进⾏编号，以便与ClickHouse内部⾏编号⽅案对⻬，该⽅案也⽤于记录消息。

# 数据被组织成颗粒以进⾏并⾏数据处理

出于数据处理的⽬的，表的列值在逻辑上被划分为多个颗粒。颗粒是流进ClickHouse进⾏数据处理的 最⼩的不可分割数据集。这意味着，ClickHouse不是读取单独的⾏，⽽是始终读取(以流⽅式并并⾏地) 整个⾏组（颗粒）。 NOTE 列值并不物理地存储在颗粒中，颗粒只是⽤于查询处理的列值的逻辑组织⽅式。

下图显示了如何将表中的 87万⾏(列值)组织成1083个颗粒，这是表的 DL语句包含设置 index_granularity(设置为默认值8192)的结果。 第⼀个(根据磁盘上的物理顺序)8192⾏(它们的列值)在逻辑上属于颗粒0，然后下⼀个8192⾏(它们的 列值)属于颗粒1，以此类推。 NOTE

最后⼀个颗粒（1082颗粒）是少于8192⾏的。

我们将主键列(UserID, URL)中的⼀些列值标记为橙⾊。

这些橙⾊标记的列值是每个颗粒中每个主键列的最⼩值。这⾥的例外是最后⼀个颗粒(上图中的颗粒 1082)，最后⼀个颗粒我们标记的是最⼤的值。

正如我们将在下⾯看到的，这些橙⾊标记的列值将是表主索引中的条⽬。

我们从0开始对⾏进⾏编号，以便与ClickHouse内部⾏编号⽅案对⻬，该⽅案也⽤于记录消息。

# 每个颗粒对应主索引的⼀个条⽬

主索引是基于上图中显示的颗粒创建的。这个索引是⼀个未压缩的扁平数组⽂件(primary.idx)，包含从 0开始的所谓的数字索引标记。 下⾯的图显示了索引存储了每个颗粒的最⼩主键列值(在上⾯的图中⽤橙⾊标记的值)。 例如：

第⼀个索引条⽬(下图中的“mark 0”)存储上图中颗粒0的主键列的最⼩值，

第⼆个索引条⽬(下图中的“mark 1”)存储上图中颗粒1的主键列的最⼩值，以此类推。

在我们的表中，索引总共有1083个条⽬， 87万⾏数据和1083个颗粒: NOTE

最后⼀个索引条⽬(上图中的“mark 1082”)存储了上图中颗粒1082的主键列的最⼤值。

索引条⽬(索引标记)不是基于表中的特定⾏，⽽是基于颗粒。例如，对于上图中的索引条⽬‘mark 0ʼ，在我们的表中没有UserID为240.923且URL为“goal:/metry=1 046796a41…”的⾏，相 反，对于该表，有⼀个颗粒0，在该颗粒中，最⼩UserID值是240.923，最⼩URL值是 “goal:/metry=1 046796a41…”，这两个值来⾃不同的⾏。

主索引⽂件完全加载到主内存中。如果⽂件⼤于可⽤的空闲内存空间，则ClickHouse将发⽣错误。

主键条⽬称为索引标记，因为每个索引条⽬都标志着特定数据范围的开始。对于示例表:

UserID index marks:主索引中存储的UserID值按升序排序。上图中的‘mark 1ʼ指示颗粒1中所有表⾏ 的UserID值，以及随后所有颗粒中的UserID值，都保证⼤于或等于4.073.710。

, 当查询对主键的第⼀列进⾏过滤时，此全局有序使ClickHouse能够对第⼀ 个键列的索引标记使⽤⼆分查找算法。

正如我们稍后将看到的

URL index marks:主键列UserID和URL有相同的基数，这意味着第⼀列之后的所有主键列的索引标 记通常只表示每个颗粒的数据范围。例如，‘mark 0ʼ中的URL列所有的值都⼤于等于 goal:/metry=1 046796a41.， 然后颗粒1中的URL并不是如此，这是因为‘mark 1‘与‘mark 0‘具有不同的UserID列值。

稍后我们将更详细地讨论这对查询执⾏性能的影响。

# 主索引被⽤来选择颗粒

现在，我们可以在主索引的⽀持下执⾏查询。下⾯计算UserID 74927693点击次数最多的10个url。

SELECT URL, count(URL) AS CountFROM hits_UserID_URLWHERE UserID = 749927693GROUP BY URLORDER BY Count DESCLIMIT 10;

1

结果：

┌─URL────────────────────────────┬─Count─┐│ http://auto.ru/chatay-barana.. │ 170 ││ http://auto.ru/chatay-id=371...│ 52 ││ http://public_search │ 45 ││ http://kovrik-medvedevushku-...│ 36 ││ http://forumal

1

│ 33 ││ http://korablitz.ru/L_1OFFER...│ 14 ││ http://auto.ru/chatayid=371...│ 14 ││ http://auto.ru/chatay-john-D...│ 13 ││ http://auto.ru/chatay-john-D...│ 10 ││ http://wot/html?page/23600_m...│ 9 │└────────────────────────────────┴───────┘10 rows in set. Elapsed: 0.005 sec.Processed 8.19 thousand rows, 740.18 KB (1.53 million rows/s., 138.59 MB/s.)

ClickHouse客户端的输出显示，没有进⾏全表扫描，只有8.19万⾏流到ClickHouse。 如果 打开了，那ClickHouse服务端⽇志会显示ClickHouse正在对1083个UserID索引标记 执⾏ 以便识别可能包含UserID列值为74927693的⾏的颗粒。这需要19个步骤，平均时间复 杂度为O(log2 n)：

trace loging ⼆分查找

...Executor): Key condition: (column 0 in [749927693, 749927693])...Executor): Running binary search on index range for part all_1_9_2 (1083 marks)...Executor): Found (LEFT) boundary mark: 176...Executor): Found (RIGHT) boundary mark: 177...Executor): Found continuous range in 19 steps...Executor): Selected 1/1 parts by partition key, 1 parts by primary key, 1/1083 marks by primary key, 1 marks to read from 1 ranges...Reading ...approx. 8192 rows starting from 1441792

1

我们可以在上⾯的跟踪⽇志中看到，1083个现有标记中有⼀个满⾜查询。 Trace Log详情 我们也可以通过使⽤ 来重现这个结果：

## EXPLAIN

EXPLAIN indexes = 1SELECT URL, count(URL) AS CountFROM hits_UserID_URLWHERE UserID = 749927693GROUP BY URLORDER BY Count DESCLIMIT 10;

1

结果如下：

┌─explain─────────────────────────────────────────────────────────────────────── ────────┐│ Expression (Projection)

1

││ Limit (preliminary LIMIT (without OFFSET)) ││ Sorting (Sorting for ORDER BY) ││ Expression (Before ORDER BY) ││ Aggregating

││ Expression (Before GROUP BY) ││ Filter (WHERE) ││

SettingQuotaAndLimits (Set limits and quota after reading from storage) ││ ReadFromMergeTree

││ Indexes: ││ PrimaryKey ││ Keys: ││ UserID ││ Condition: (UserID in [749927693, 749927693]) ││ Parts: 1/1 ││ Granules: 1/1083

│└────────────────────────────────────────────────────────────────────────────── ─────────┘16 rows in set. Elapsed: 0.003 sec.

客户端输出显示，在1083个颗粒中选择了⼀个可能包含UserID列值为74927693的⾏。 CONCLUSION 当查询对联合主键的⼀部分并且是第⼀个主键进⾏过滤时，ClickHouse将主键索引标记运⾏⼆分查找 算法。 正如上⾯所讨论的，ClickHouse使⽤它的稀疏主索引来快速(通过⼆分查找算法)选择可能包含匹配查询 的⾏的颗粒。 这是ClickHouse查询执⾏的第⼀阶段(颗粒选择)。 在第⼆阶段(数据读取中), ClickHouse定位所选的颗粒，以便将它们的所有⾏流到ClickHouse引擎中， 以便找到实际匹配查询的⾏。 我们将在下⼀节更详细地讨论第⼆阶段。

![image 1](<ClickHouse主键索引最佳实践.note_images/imageFile1.png>)

![image 2](<ClickHouse主键索引最佳实践.note_images/imageFile2.png>)

![image 3](<ClickHouse主键索引最佳实践.note_images/imageFile3.png>)

![image 4](<ClickHouse主键索引最佳实践.note_images/imageFile4.png>)

