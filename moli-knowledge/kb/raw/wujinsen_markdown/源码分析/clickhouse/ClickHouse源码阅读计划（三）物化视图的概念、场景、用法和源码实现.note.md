htps:/ w.aboutyun.com/forum.php?mod=viewthread&tid=31658

问题导读：

- 1.POPULATE关键字有什么⽤？
- 2.ClickHouse的物化视图如何实现？
- 3.如何写⼊物化视图?


源码分析版本：21.7 物化视图是什么？ View 我理解为⼀个saved query，是⼀种虚拟表，不存储任何数据。每当从view读取数据的时候，对 应⼀次从物理表的read操作。 ⽽物化视图则是对应⼀份持久化的存储，可以是物理表的⼀份数据⼦集拷⻉，也可以是多表JOIN或者 预聚合的⼀个结果或⼦集。ClickHouse的物化视图实现更像是触发器，如果view中预先定义了聚合函 数，那么（在不指定populate关键字的情况下）聚合函数仅适⽤于新插⼊的数据。对源表数据的更改 都不会更改物化视图。

物化视图的场景？ 假设场景：计算每个⽤户的⽇下载量

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.


CREATE TABLE download ( when DateTime, userid UInt32, bytes Float32 ) ENGINE=MergeTree PARTITION BY toYYYYMM(when) ORDER BY (userid, when);

# 插 ⼊ 数据 INSERT INTO download SELECT now() + number * 60 as when, 25, rand() % 100000000 FROM system.numbers LIMIT 5000

# 查 看 报 表 数据

- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.


SELECT toStartOfDay(when) AS day, userid, count() as downloads, sum(bytes) AS bytes FROM download GROUP BY userid, day

复制代码

如果不使⽤物化视图，需要每次运⾏查询以交互⽅式统计结果。但是对于⼤型表，提前计算它们更 快，更节省资源。因此，最好将结果放在单独的物化视图中，该表就可以连续跟踪每天每个⽤户的下 载总数。 可以理解为把总计结果提前计算好，配合SumingMergeTre/AgregatingMergeTre 还会对新插⼊ 物化视图的数据做相应的求和/聚合/去重等操作，存到物化视图表中，实现实时的预聚合。那么就不需 要每次运⾏查询去得到结果了。 另外⼀个⽤法，就是可以通过`AS SELECT`语法灵活改变表的排序顺序/表结构的变更。排序key变 了，那么针对该key的filter scan能够实现更⾼效的索引剪枝。

语法介绍

1.

CREATE MATERIALIZED VIEW [IF NOT EXISTS] [db.]table_name [ON CLUSTER] [TO[db.]name] [ENGINE = engine] [POPULATE] AS SELECT ...

复制代码

不指定 TO [db].[table]的时候，必须要指定 ENGINE

指定TO [db].[table] ⽬标表的时候，不能使⽤POPULATE关键字

POPULATE关键字有什么⽤？ 若指定了POPULATE关键字，会把表中现有数据存储到视图中，否则只会写⼊创建视图之后的数据。 然⽽如果对数据的精确度要求⽐较⾼，不建议使⽤POPULATE关键字，因为在创建视图过程中插⼊表 中的数据并不会写⼊视图，会造成数据的丢失。

使⽤指定ENGINE⽅法创建

使⽤POPULATE 创建⼀个每⽇计数下载量的物化视图表

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


CREATE MATERIALIZED VIEW download_daily_mv ENGINE = SummingMergeTree PARTITION BY toYYYYMM(day) ORDER BY (userid, day) POPULATE AS SELECT

toStartOfDay(when) AS day, userid,

- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.


count() AS downloads, sum(bytes) AS bytes

FROM download GROUP BY

userid, day;

SELECT * FROM download_daily_mv ORDER BY day,userid LIMIT 5;

复制代码

源表中的插⼊的5 0条数据已经存在在物化视图表中

不使⽤POPULATE 创建⼀个每⼩时计数下载量的物化视图表

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


CREATE MATERIALIZED VIEW download_hour_mv ENGINE = SummingMergeTree PARTITION BY toYYYYMM(hour) ORDER BY (userid, hour) AS SELECT

toStartOfHour(when) AS hour, userid, count() as downloads, sum(bytes) AS bytes

FROM download WHERE when >= toDateTime('2021-07-23 00:00:00') GROUP BY userid, hour

复制代码

这⾥加了个时间点，意味着只有在该时间段之后的数据才会物化到mv中。 可以看到现在还是个空表，如何物化数据呢？

你可以直接把源表数据直接insert到mv中,⼿动把⾃动同步时间点前的源数据插⼊到物化视图表中，这 样就实现了原有数据表的同步。

- 1.
- 2.
- 3.
- 4.


INSERT INTO download_hour_mv SELECT toStartOfHour(when) AS hour, userid, count() AS downloads,

- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


sum(bytes) AS bytes FROM download WHERE when <= toDateTime('2021-07-23 00:00:00') GROUP BY

userid, hour

复制代码

当然你也可以直接写数据到源表，实现数据的⾃动同步

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


INSERT INTO download SELECT toDateTime('2021-09-01 04:00:00') + (number * (1 / 3)) AS when, 19, rand() % 1000000

FROM system.numbers LIMIT 10

复制代码

物化视图创建⼀个具有特殊名称的私有表来保存数据。如果通过键⼊“ DROP TABLE download_daily_mv”删除实例化视图，则私有表将消失。如果需要更改视图，则需要将其删除并使⽤ 新数据重新创建。

我们可以通过show tables看到私有表：

我们之前也讲了，物化视图实际上也是占⽤了disk上的datapart，我们可以看⼀下私有表和mv表对应 的datapart情况：

- 1.
- 2.
- 3.


SELECT partition, name,

- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.


rows, bytes_on_disk, modification_time, min_date, max_date, engine

FROM system.parts WHERE table = '.inner_id.40fd3d25-a09b-4a4d-80fd-3d25a09bba4d'

SELECT partition, name, rows, bytes_on_disk, modification_time, min_date, max_date, engine

FROM system.parts WHERE table = 'download_hour_mv'

复制代码

可以看到物化表其实只是⼀个视图，不存储datapart，⽽只有其对应的私有表才存储相应的datapart。

使⽤ to db.table⽅法创建 ⽤这种⽅法创建mv需要⽤[to db.table]⼿动指定⽬标私有表 先创建源表并且创建数据

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


CREATE TABLE counter ( when DateTime DEFAULT now(), device UInt32, value Float32

) ENGINE=MergeTree PARTITION BY toYYYYMM(when) ORDER BY (device, when);

- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.


INSERT INTO counter

SELECT toDateTime('2015-01-01 00:00:00') + toInt64(number/10) AS when, (number % 10) + 1 AS device, (device * 3) + (number/10000) + (rand() % 53) * 0.1 AS value

FROM system.numbers LIMIT 1000000;

复制代码

假设我们需要查询全时间段设备的点击率，最⼤最⼩和平均值，我们可⽤以下SQL

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


SELECT device, sum(count) AS count,

maxMerge(max_value_state) AS max, minMerge(min_value_state) AS min, avgMerge(avg_value_state) AS avg

FROM counter GROUP BY device ORDER BY device ASC

复制代码

但是这么做的话查询速度会很慢，因为需要扫全表的数据。如果我们创建⼀个物化视图表，计算⼀个 ⽇聚合的数据，那么就可以直接汇总每⽇聚合的数据返回结果。注意这⾥的maxMerge函数可以理解为 在SQL语法层⾯暴露⼀个部分值局部聚合的这么⼀个功能。换句话说，使⽤SumingMergeTre的物 化视图也可以实现AgregatingMergeTre的聚合功能，因此⽽推荐使⽤SumingMergeTre。

创建⽬标表

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


CREATE TABLE counter_daily ( day DateTime, device UInt32, count UInt64,

max_value_state AggregateFunction(max, Float32), min_value_state AggregateFunction(min, Float32), avg_value_state AggregateFunction(avg, Float32)

) ENGINE = SummingMergeTree() PARTITION BY tuple() ORDER BY (device, day);

复制代码

在定义物化视图的时候使⽤select从源表把数据注⼊到⽬标表中

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.


CREATE MATERIALIZED VIEW counter_daily_mv TO counter_daily AS SELECT

toStartOfDay(when) as day, device, count(*) as count,

maxState(value) AS max_value_state, minState(value) AS min_value_state, avgState(value) AS avg_value_state

FROM counter WHERE when >= toDate('2019-01-01 00:00:00') GROUP BY device, day ORDER BY device, day;

复制代码

⼀开始物化视图是没有数据的，需要⼿动把数据加载到⽬标表中

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.


INSERT INTO counter_daily SELECT

toStartOfDay(when) as day, device, count(*) AS count,

maxState(value) AS max_value_state, minState(value) AS min_value_state, avgState(value) AS avg_value_state

FROM counter WHERE when < toDateTime('2019-01-01 00:00:00') GROUP BY device, day ORDER BY device, day

复制代码

现在数据都写⼊到⽬标表中了，就可以查了

- 1.
- 2.
- 3.
- 4.
- 5.


SELECT device, sum(count) AS count,

maxMerge(max_value_state) AS max, minMerge(min_value_state) AS min,

- 6.
- 7.
- 8.
- 9.


avgMerge(avg_value_state) AS avg FROM counter_daily_mv GROUP BY device ORDER BY device ASC

复制代码

这么⼀对⽐，通过引⼊预聚合的物化视图，就可以⼤⼤减少要计算聚合结果时候扫描的数据量，从⽽ 提⾼了查询速度。

ClickHouse的物化视图如何实现？创建物化视图 StorageMaterializedView:StorageMaterializedView 构造函数

- 1.校验语法
- 2.从`AS SELECT.` ⼦句的AST提取出SELEC subquery的信息
- 3.如果没有指定⽬标表，会⾃⼰⽣成⼀条创建i ner table的query交由InterpreterCreateQuery执⾏，同 时mv还可以atach上i ner table来指定⽬标表
- 4.在Database 的catalog中会维护⼀个


1.

ViewDependencies = std::map<StorageID, std::set<StorageID>>;

复制代码

保存Table -> set of 实际上依赖的源表的映射关系，因此需要更新物化视图表到其映射的源表的映射关 系。注意，这⾥的源表不是⾃动⽣成的i ner table，⽽是创建mv时指定的AS SELECT⼦句中的表

查询物化视图 mv实际上都是基于⽬标表来进⾏plan的构建，步骤如下：

- 1.构建query_plan，详细逻辑看下⾯的read函数
- 2.从query_plan构建Pipeline

复制代码

- 3.交由Pipeline Executor执⾏


- 1.
- 2.


plan.convertToPipe buildQueryPipeline

- 1.
- 2.
- 3.
- 4.


/// Remove columns from target_header that does not exists in src_header static void removeNonCommonColumns(const Block & src_header, Block & target_header) {

std::set<size_t> target_only_positions;

- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.


for (const auto & column : target_header) {

if (!src_header.has(column.name)) target_only_positions.insert(target_header.getPositionByName(column.name));

} target_header.erase(target_only_positions);

}

void StorageMaterializedView::read( QueryPlan & query_plan, const Names & column_names, const StorageMetadataPtr & metadata_snapshot, SelectQueryInfo & query_info, ContextPtr local_context, QueryProcessingStage::Enum processed_stage,

const size_t max_block_size, const unsigned num_streams){

// 实 际 操 作 的 是 ⽬ 标 表 auto storage = getTargetTable();

// 获 取 mv的 datastream的 第 ⼀ 个 block auto mv_header = getHeaderForProcessingStage(*this, column_names,

metadata_snapshot, query_info, local_context, processed_stage);

// 获 取 查 询语 句 的 列 对 应 的 第 ⼀ 个 block auto target_header = query_plan.getCurrentDataStream().header;

//... /// No need to convert columns that does not exists in MV // 从 查 询 的 列 中 去 除 那 些 mv不 存 在 的 列 removeNonCommonColumns(mv_header, target_header);

/// No need to convert columns that does not exists in the result header. /// /// Distributed storage may process query up to the specific stage, and /// so the result header may not include all the columns from the /// materialized view.

- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.


// 从 mv的 列 中 去 除 那 些 查 询 不 需 要 的 列 removeNonCommonColumns(target_header, mv_header);

// 如 果 两个 block含 的 列 不 同 ， 则 把 ⽬ 标 所 要 查 询 的 列 结 构 转 化 为 mv的 结 构 ， 并 把 // 转 换 算 ⼦ 写 ⼊ query plan if (!blocksHaveEqualStructure(mv_header, target_header)) {

auto converting_actions = ActionsDAG::makeConvertingActions(target_header.getColumnsWithTypeAndName(),

mv_header.getColumnsWithTypeAndName(),

ActionsDAG::MatchColumnsMode::Name);

auto converting_step = std::make_unique<ExpressionStep> (query_plan.getCurrentDataStream(), converting_actions);

converting_step->setStepDescription("Convert target table structure to MaterializedView structure");

query_plan.addStep(std::move(converting_step)); }

// 给 ⽬ 标 表 上 lock， 限 流 等 参 数 ， auto adding_limits_and_quota = std::make_unique<SettingQuotaAndLimitsStep>(

query_plan.getCurrentDataStream(), storage, std::move(lock), limits, leaf_limits, nullptr, nullptr);

// 转 换 算 ⼦ 写 ⼊ query plan adding_limits_and_quota->setStepDescription("Lock destination table for

MaterializedView");

query_plan.addStep(std::move(adding_limits_and_quota)); }

复制代码

写⼊物化视图

1.

InterpreterInsertQuery.cpp

- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.


有两处与物化视图相关：

// 构 造 物 化到 view的 输 出 流 out = std::make_shared<PushingToViewsBlockOutputStream>(table, metadata_snapshot,

getContext(), query_ptr, no_destination);

// 在 执 ⾏ pipeline中 增 加 对 源 表 table/内 部 表 的 引 ⽤ res.pipeline.addStorageHolder(table); if (const auto * mv = dynamic_cast<const StorageMaterializedView *>(table.get())) {

if (auto inner_table = mv->tryGetTargetTable())

res.pipeline.addStorageHolder(inner_table); }

复制代码

写⼊源表的数据如何与⾃动⽣成的i ner table对应起来呢？靠的就是 PushingToViewsBlockOutputStream。在写⼊源表的同时还会写⼊依赖其的所有物化视图的⽬标表 中。

1.

PushingToViewsBlockOutputStream.cpp

复制代码

构造函数：

具体的写⼊函数实现在proces函数： 每调⽤⼀次proces写⼊⼀个Block

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


void PushingToViewsBlockOutputStream::process(const Block & block, ViewInfo & view)

// 绑 定 mv的 数据 源 为 写 ⼊ 表 local_context->addViewSource(

StorageValues::create(storage->getStorageID(), metadata_snapshot>getColumns(), block, storage->getVirtuals()));

in = std::make_shared<MaterializingBlockInputStream>(select>execute().getInputStream());

in = std::make_shared<SquashingBlockInputStream>(

- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.


in, getContext()->getSettingsRef().min_insert_block_size_rows, getContext()->getSettingsRef().min_insert_block_size_bytes);

in = std::make_shared<ConvertingBlockInputStream>(in, view.out>getHeader(), ConvertingBlockInputStream::MatchColumnsMode::Name);

while (Block result_block = in->read()) {

Nested::validateArraySizes(result_block); // 写 ⼊ 物 化 视 图 的 inner table view.out->write(result_block); }

复制代码

MaterializingBlockInputStream

把从写⼊表select出来的data block stream的列数据进⾏物化.因为从select语句从写⼊表读出来的 block stream中，block的column都是ColumnConst类型。 所谓ColumnConst可以代表另外⼀个列的任意常数引⽤，但是列中的元素不是所代表列中的元素，⽽ 只存储⼀个值，还存储着真是列数据的⼀个指针。我才想这么实现是为了加快block中传输，从stream 读取出block之后再做物化操作，从指针还原数据。

SquashingBlockInputStream

把连续的⼏个block合并成⼀个block，减少因group by操作产⽣的block数量

ConvertingBlockInputStream

把写⼊表block转化为物化视图内部表block的结构

删除物化视图

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


void StorageMaterializedView::drop() {

auto table_id = getStorageID(); const auto & select_query = getInMemoryMetadataPtr()->getSelectQuery(); if (!select_query.select_table_id.empty())

DatabaseCatalog::instance().removeDependency(select_query.select_table_id, table_id);

dropInnerTableIfAny(true, getContext()); }

- 11.
- 12.
- 13.
- 14.
- 15.


void StorageMaterializedView::dropInnerTableIfAny(bool no_delay, ContextPtr local_context) {

if (has_inner_table && tryGetTargetTable())

InterpreterDropQuery::executeDropQuery(ASTDropQuery::Kind::Drop, getContext(), local_context, target_table_id, no_delay); }

复制代码

- 1.删除物化视图到其对应的⽬标表集合的映射关系
- 2.drop i ner表；如果其⽬标表不是i ner 表，那么⽬标表不会被删除


