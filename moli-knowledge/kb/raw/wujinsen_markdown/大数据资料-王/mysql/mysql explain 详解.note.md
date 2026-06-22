在 explain的帮助下，您就知道什么时候该给表添加索引，以使⽤索引来查找记录从⽽让select 运⾏更快。 如果由于不恰当使⽤索引⽽引起⼀些问题的话，可以运⾏ analyze table来更新该表的统计信息，例如键的基数，它能帮您在优化⽅⾯ 做出更好的选择。

explain 返回了⼀⾏记录，它包括了 select语句中⽤到的各个表的信息。这些表在结果中按照mysql即将执⾏的查询中读取的顺序列出 来。mysql⽤⼀次扫描多次连接（single- sweep,multi-join）的⽅法来解决连接。这意味着mysql从第⼀个表中读取⼀条记录，然后 在第⼆个表中查找到对应的记录，然后在第三个表 中查找，依次类推。当所有的表都扫描完了，它输出选择的字段并且回溯所有的表， 直到找不到为⽌，因为有的表中可能有多条匹配的记录下⼀条记录将从该表读 取，再从下⼀个表开始继续处理。 在mysql version 4.1中，explain输出的结果格式改变了，使得它更适合例如 union语句、⼦查询以及派⽣表的结构。更令⼈注意的 是，它新增了2个字段： id和 select_type。当你使⽤早于mysql4.1的版本就看不到这些字段了。 explain结果的每⾏记录显示了每个表的相关信息，每⾏记录都包含以下⼏个字段：

id ：本次 select 的标识符。在查询中每个 select都有⼀个顺序的数值。 select_type ：select 的类型，可能会有以下⼏种：

simple: 简单的 select （没有使⽤ 表连接或⼦查询） primary: 最外层的 select，。 union: 第⼆层，在select 之后使⽤了 union做表连接。 dependent union: union 语句中的第⼆个select，依赖于外部⼦查询 subquery: ⼦查询中的第⼀个 select dependent subquery: ⼦查询中的第⼀个 subquery依赖于外部的⼦查询 derived: 派⽣表 select（from⼦句中的⼦查询），即⼦表

table：记录查询引⽤的表。 type：表连接类型。以下列出了各种不同类型的表连接，依次是从最好的到最差的：

system:表只有⼀⾏记录（等于系统表）。这是 const表连接类型的⼀个特例。 const:表中最多只有⼀⾏匹配的记录，它在查询⼀开始的时候就会被读取出来。 由于只有⼀⾏记录，在余下的优化程序⾥该⾏记录的字段值可以被当作是⼀个 恒定值。const表查 询起来⾮常快，因为只要读取⼀次！const ⽤于在和 primary key 或unique 索引中有固定值⽐ 较的情形。下⾯的⼏个查询中，tbl_name 就是 c表了： select * from tbl_name where primary_key=1; select * from tbl_namewhere primary_key_part1=1 and primary_key_part2=2; eq_ref:从该表中会有⼀⾏记录被读取出来以和从前⼀个表中读取出来的记录做联合。 与const类型不同的是，这是最好的连接类型。它⽤在索引所有部 分都⽤于做连接并且这个索引是 ⼀个primary key 或 unique 类型。eq_ref可以⽤于在进⾏"="做⽐较时检索字段。⽐较的值可以 是固定值或者是表达式，表达示中可以使⽤表⾥的字段，它们在读表之前已经准备好 了。以下的⼏ 个例⼦中，mysql使⽤了eq_ref 连接来处理 ref_table：select * from ref_table,other_table whereref_table.key_column=other_table.column; select * fromref_table,other_table whereref_table.key_column_part1=other_table.column andref_table.key_column_part2=1; ref: 该表中所有符合检索值的记录都会被取出来和从上⼀个表中取出来的记录作联合。

ref⽤于连接程序使⽤键的最左前缀或者是该键不是 primary key 或 unique索引（换句话说，就 是连接程序⽆法根据键值只取得⼀条记录）的情况。当根据键值只查询到少数⼏条匹配的记录时， 这就是⼀个不错的连接类型。 ref还可以⽤于检索字段使⽤ =操作符来⽐较的时候。以下的⼏个例 ⼦中，mysql将使⽤ ref 来处理ref_table：

select * from ref_table where key_column=expr; select * fromref_table,other_table whereref_table.key_column=other_table.column; select * fromref_table,other_table whereref_table.key_column_part1=other_table.column andref_table.key_column_part2=1;

ref_or_null: 这种连接类型类似 ref，不同的是mysql会在检索的时候额外的搜索包含null 值的记 录。

这种连接类型的优化是从mysql4.1.1开始的，它经常⽤于⼦查询。在以下的例⼦中，mysql使⽤ ref_or_null 类型来处理 ref_table：select * from ref_table where key_column=expr or key_column is null;

unique_subquery: 这种类型⽤例如⼀下形式的 in ⼦查询来替换

ref：value in (select primary_key from single_table where some_expr) unique_subquery: 只是⽤来完全替换⼦查询的索引查找函数效率更⾼了。 index_subquery: 这种连接类型类似 unique_subquery。 它⽤⼦查询来代替in，不过它⽤于在⼦查询中没有唯⼀索引的情况下，例如以下形式： value in (select key_column from single_table where some_expr) range: 只有在给定范围的记录才会被取出来，利⽤索引来取得⼀条记录。 key字段表示使⽤了哪个索引。key_len字段包括了使⽤的键的最⻓部分。这种类型时 ref 字段值 是 null。range⽤于将某个字段和⼀个定植⽤以下任何操作符⽐较时 =, <>, >,>=, <, <=, is null, <=>, between, 或 in： select * from tbl_name where key_column = 10; select * fromtbl_name where key_column between 10 and 20; select * from tbl_namewhere key_column in (10,20,30); select * from tbl_name wherekey_part1= 10 and key_part2 in (10,20,30); index: 连接类型跟 all ⼀样，不同的是它只扫描索引树。 它通常会⽐ all快点，因为索引⽂件通常⽐数据⽂件⼩。mysql在查询的字段知识单独的索引的⼀部 分的情况下使⽤这种连接类型。 all: 将对该表做全部扫描以和从前⼀个表中取得的记录作联合。 这时候如果第⼀个表没有被标识为const的话就不⼤好了，在其他情况下通常是⾮常糟糕的。正常 地，可以通过增加索引使得能从表中更快的取得记录以避免all。

possible_keys：possible_keys字段是指 mysql在搜索表记录时可能使⽤哪个索引。 注意，这个字段完全独⽴于explain 显示的表顺序。这就意味着 possible_keys⾥⾯所包含的索引可能在实际的使⽤中没⽤到。如果这 个字段的值是null，就表示没有索引被⽤到。这种情况下，就可以检查 where⼦句中哪些字段那些字段适合增加索引以提⾼查询的性 能。就这样，创建⼀下索引，然后再⽤explain 检查⼀下。详细的查看章节"14.2.2 alter tablesyntax"。想看表都有什么索引，可以 通过 show index from tbl_name来看。 key：key字段显示了mysql实际上要⽤的索引。 当没有任何索引被⽤到的时候，这个字段的值就是null。想要让mysql强⾏使⽤或者忽略在 possible_keys字段中的索引列表，可以在 查询语句中使⽤关键字force index, use index,或 ignore index。如果是 myisam 和 bdb 类型表，可以使⽤ analyzetable 来帮助 分析使⽤使⽤哪个索引更好。如果是 myisam类型表，运⾏命令 myisamchk --analyze也是⼀样的效果。详细的可以查看章 节"14.5.2.1 analyze tablesyntax"和"5.7.2 table maintenance and crash recovery"。 key_len：key_len 字段显示了mysql使⽤索引的⻓度。 当 key 字段的值为 null时，索引的⻓度就是 null。注意，key_len的值可以告诉你在联合索引中mysql会真正使⽤了哪些索引。 ref：ref 字段显示了哪些字段或者常量被⽤来和 key配合从表中查询记录出来。 rows：rows 字段显示了mysql认为在查询中应该检索的记录数。 extra：本字段显示了查询中mysql的附加信息。以下是这个字段的⼏个不同值的解释：

distinct:mysql当找到当前记录的匹配联合结果的第⼀条记录之后，就不再搜索其他记录了。 not exists:mysql在查询时做⼀个 left join优化时，当它在当前表中找到了和前⼀条记录符合 left join条件后，就不再搜索更多的记录了。

下⾯是⼀个这种类型的查询例⼦：select * from t1 left join t2 on t1.id=t2.id where t2.id isnull;

假使 t2.id 定义为 not null。这种情况下，mysql将会扫描表 t1并且⽤ t1.id 的值在 t2 中查找记录。当在 t2中找到⼀条匹配的记录时，这就意味着 t2.id 肯定不会都是 null，就不会再在 t2 中查找相同 id值的其他记录了。也可以这么说，对于 t1 中的每个 记录，mysql只需要在t2 中做⼀次查找，⽽不管在 t2 中实际有多少匹配的记录。

range checked for each record (index map: #):mysql没找到合适的可⽤的索引。

取代的办法是，对于前⼀个表的每⼀个⾏连接，它会做⼀个 检验以决定该使⽤哪个索引（如果有的话），并且使⽤这个索 引来从表⾥取得记录。 这个过程不会很快，但总⽐没有任 何索引时做表连接来得快。

using filesort: mysql需要额外的做⼀遍从⽽以排好的顺序取得记录。 排序程序根据连接的类型遍历所有的记录，并且将所有符合 where条件的记录的要排序 的键和指向记录的指针存储起来。这些键已经排完序了，对应的记录也会按照排好的顺 序取出来。详情请看"7.2.9how mysql optimizes order by"。kllklksdddddusing index:字段的信息直接从索引树中的信息取得，⽽不再去扫描实际的记录。这种策略⽤ 于查询时的字段是⼀个独⽴索引的⼀部分。

using temporary: mysql需要创建临时表存储结果以完成查询。这种情况通常发⽣在查询时包含 了groupby 和 order by ⼦句，它以不同的⽅式列出了各个字段。 using where:where⼦句将⽤来限制哪些记录匹配了下⼀个表或者发送给客户端。

除⾮你特别地想要取得或者检查表种的所有记录，否则的话当查询的extra 字段值不是 using where 并且表连接类型是 all 或 index时可能表示有问题。

如果你想要让查询尽可能的快，那么就应该注意 extra 字段的值为usingfilesort 和 using temporary 的情况。

你可以通过 explain 的结果中 rows字段的值的乘积⼤概地知道本次连接表现如何。 它可以粗略地告诉我们mysql在查询过程中会查询多少条记录。如果是使⽤系统变量 max_join_size 来取得查询结果，这个乘积还可 以⽤来确定会执⾏哪些多表select 语句。

下⾯的例⼦展示了如何通过 explain提供的信息来较⼤程度地优化多表联合查询的性能。 假设有下⾯的 select 语句，正打算⽤ explain 来检测：

EXPLAIN SELECT

tt.ticketnumber, tt.timein, tt.projectreference, tt.estimatedshipdate, tt.actualshipdate, tt.clientid,tt.servicecodes, tt.repetitiveid, tt.currentprocess, tt.currentdppers tt.recordvolume, tt.dpprinted, et.country, et_1.country, do.custname

FROM tt, et, et AS et_1, DO WHERE

tt.submittime IS NULL AND tt.actualpc = et.employid AND tt.assignedpc = et_1.employid AND tt.clientid = do.custnmbr;

在这个例⼦中，先做以下假设： 要⽐较的字段定义如下：

table column columntype tt actualpc char(10) tt assignedpc char(10) tt clientid char(10) et employid char(15) do custnmbr char(15)

数据表的索引如下： table index tt actualpc tt assignedpc tt clientid et employid (primary key) do custnmbr (primary key)

tt.actualpc 的值是不均匀分布的。

在任何优化措施未采取之前，经过 explain分析的结果显示如下： table type possible_keys key key_len ref rows extra et all primary null null null 74 do all primary null null null 2135 et_1 all primary null null null 74 tt all assignedpc, null null null 3872 clientid, actualpc range checked for each record (key map: 35)

由于字段 type 的对于每个表值都是all，这个结果意味着mysql对所有的表做⼀个迪卡尔积；这就是说，每条记录的组合。 这将需要花很⻓的时间，因为需要扫描每个表总 记录数乘积的总和。在这情况下，它的积是74 * 2135 * 74 * 3872 = 45,268,558,720条记录。 如果数据表更⼤的话，你可以想象⼀下需要多⻓的时间。 在这⾥有个问题是当字段定义⼀样的时候，mysql就可以在这些字段上更快的是⽤索引（对isam类型的表来说，除⾮字段定义完全⼀ 样，否则不会使⽤索 引）。 在这个前提下，varchar和 char是⼀样的除⾮它们定义的⻓度不⼀致。由于 tt.actualpc 定义为char(10)，et.employid 定义为 char(15)，⼆者⻓度不⼀致。 为了解决这个问题，需要⽤ alter table 来加⼤ actualpc的⻓度从10到15个字符：mysql> alter table tt modify actualpc varchar(15);

现在 tt.actualpc 和 et.employid 都是 varchar(15)了。再来执⾏⼀次 explain 语句看看结果： table type possible_keys key key_len ref rows

extra tt all assignedpc, null null null 3872 using clientid, where actualpc do all primary null null null 2135 range checked for each record (keymap: 1)

et_1 all primary null null null 74

range checked for eachrecord (key map: 1) et eq_ref primary primary 15 tt.actualpc 1 这还不够，它还可以做的更好：现在 rows值乘积已经少了74倍。这次查询需要⽤2秒钟。

第⼆个改变是消除在⽐较 tt.assignedpc = et_1.employid 和 tt.clientid= do.custnmbr 中字段的⻓度不⼀致问题： mysql> alter table tt modify assignedpc varchar(15), ->modify clientid varchar(15); 现在 explain 的结果如下： table type possible_keys key key_len ref rows

extra et all primary null null null 74 tt ref assignedpc, actualpc 15 et.employid 52

using clientid, where actualpc et_1 eq_ref primary primary 15 tt.assignedpc 1 do eq_ref primary primary 15 tt.clientid 1 这看起来已经是能做的最好的结果了。 遗留下来的问题是，mysql默认地认为字段 tt.actualpc的值是均匀分布的，然⽽表 tt并⾮如此。幸好，我们可以很⽅便的让mysql分析 索引的分布：mysql> analyze table tt;

到此为⽌，表连接已经优化的很完美了，explain 的结果如下： table type possible_keys key key_len ref rows extra tt all assignedpc null null null 3872 using clientid, where actualpc et eq_ref primary primary 15 tt.actualpc 1 et_1 eq_ref primary primary 15 tt.assignedpc 1 do eq_ref primary primary 15 tt.clientid 1

请注意，explain 结果中的 rows字段的值也是mysql的连接优化程序⼤致猜测的， 请检查这个值跟真实值是否基本⼀致。如果不是，可以通过在select 语句中使⽤ straight_join 来取得更好的性能， 同时可以试着在from分句中⽤不同的次序列出各个表。

