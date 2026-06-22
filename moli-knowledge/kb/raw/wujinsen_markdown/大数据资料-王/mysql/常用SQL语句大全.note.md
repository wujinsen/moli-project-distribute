数据库基本 -SQL语句⼤全 学会数据库是很实⽤D~记录⼀些常⽤的sql语句 .有⼊⻔有提⾼有⻅都没⻅过的 .好全 .收藏下 . 其实⼀般⽤的就是查询,插⼊,删除等语句⽽已 .但学学存储过程是好事 .以后数据⽅⾯的东⻄就不⽤在 程序⾥搞喽 .⽽且程序与数据库只要⼀个来回通讯就可以搞定所有数据的操作 .

⼀、基础

- 1、说明：创建数据库 Create DATABASE database-name
- 2、说明：删除数据库 drop database dbname
- 3、说明：备份sql server

- - 创建 备份数据的 device USE master EXEC sp_adumpdevice ‘disk‘, ‘testBack‘, ‘c:\msql7backup\MyNwind_1.dat‘
- - 开始 备份 BACKUP DATABASE pubs TO testBack


- 4、说明：创建新表 create table tabname(col1 type1 [not nul] [primary key],col2 type2 [not nul],.) 根据已有的表创建新表：

- A：create table tab_new like tab_old (使⽤旧表创建新表)
- B：create table tab_new as select col1,col2… from tab_old definition only


- 5、说明：删除新表 drop table tabname
- 6、说明：增加⼀个列 Alter table tabname ad column col type 注：列增加后将不能删除。DB2中列加上后数据类型也不能改变，唯⼀能改变的是增加varchar类型的 ⻓度。
- 7、说明：添加主键： Alter table tabname ad primary key(col) 说明：删除主键： Alter table tabname drop primary key(col)
- 8、说明：创建索引：create [unique] index idxname on tabname(col….) 删除索引：drop index idxname 注：索引是不可更改的，想更改必须删除重新建。
- 9、说明：创建视图：create view viewname as select statement 删除视图：drop view viewname
- 10、说明：⼏个简单的基本的sql语句 选择：select * from table1 where 范围


插⼊：insert into table1(field1,field2) values(value1,value2) 删除：delete from table1 where 范围 更新：update table1 set field1=value1 where 范围 查找：select * from table1 where field1 like ʼ%value1%ʼ -like的语法很精妙，查资料! 排序：select * from table1 order by field1,field2 [desc] 总数：select count as totalcount from table1 求和：select sum(field1) as sumvalue from table1 平均：select avg(field1) as avgvalue from table1 最⼤：select max(field1) as maxvalue from table1 最⼩：select min(field1) as minvalue from table1

1、说明：⼏个⾼级查询运算词

- A： UNION 运算符 UNION 运算符通过组合其他两个结果表（例如 TABLE1 和 TABLE2）并消去表中任何重复⾏⽽派⽣出 ⼀个结果表。当 AL 随 UNION ⼀起使⽤时（即 UNION AL），不消除重复⾏。两种情况下，派⽣表 的每⼀⾏不是来⾃ TABLE1 就是来⾃ TABLE2。
- B： EXCEPT 运算符 EXCEPT 运算符通过包括所有在 TABLE1 中但不在 TABLE2 中的⾏并消除所有重复⾏⽽派⽣出⼀个结 果表。当 AL 随 EXCEPT ⼀起使⽤时 (EXCEPT AL)，不消除重复⾏。
- C： INTERSECT 运算符 INTERSECT 运算符通过只包括 TABLE1 和 TABLE2 中都有的⾏并消除所有重复⾏⽽派⽣出⼀个结果 表。当 AL 随 INTERSECT ⼀起使⽤时 (INTERSECT AL)，不消除重复⾏。 注：使⽤运算词的⼏个查询结果⾏必须是⼀致的。 12、说明：使⽤外连接 A、left outer join： 左外连接（左连接）：结果集⼏包括连接表的匹配⾏，也包括左连接表的所有⾏。 SQL: select a.a, a.b, a.c, b.c, b.d, b.f from a LEFT OUT JOIN b ON a.a = b.c


- B：right outer join: 右外连接(右连接)：结果集既包括连接表的匹配连接⾏，也包括右连接表的所有⾏。
- C：ful outer join： 全外连接：不仅包括符号连接表的匹配⾏，还包括两个连接表中的所有记录。


⼆、提升

- 1、说明：复制表(只复制结构,源表名：a 新表名：b) (Aces可⽤) 法⼀：select * into b from a where 1<>1 法⼆：select top 0 * into b from a
- 2、说明：拷⻉表(拷⻉数据,源表名：a ⽬标表名：b) (Aces可⽤)


- insert into b(a, b, c) select d,e,f from b;
- 3、说明：跨数据库之间表的拷⻉(具体数据使⽤绝对路径) (Aces可⽤) insert into b(a, b, c) select d,e,f from b in ‘具体数据库ʼ where 条件 例⼦： .from b in ‘"&Server.MapPath("."&"\data.mdb" &"‘ where.
- 4、说明：⼦查询(表名1：a 表名2：b) select a,b,c from a where a IN (select d from b 或者: select a,b,c from a where a IN (1,2,3)
- 5、说明：显示⽂章、提交⼈和最后回复时间 select a.title,a.username,b.a date from table a,(select max(a date) a date from table where table.title=a.title) b
- 6、说明：外连接查询(表名1：a 表名2：b) select a.a, a.b, a.c, b.c, b.d, b.f from a LEFT OUT JOIN b ON a.a = b.c
- 7、说明：在线视图查询(表名1：a select * from (Select a,b,c FROM a) T where t.a > 1;
- 8、说明：betwen的⽤法,betwen限制查询数据范围时包括了边界值,not betwen不包括 select * from table1 where time betwen time1 and time2 select a,b,c, from table1 where a not betwen 数值1 and 数值2
- 9、说明：in 的使⽤⽅法 select * from table1 where a [not] in (‘值1,ʼ值2,ʼ值4,ʼ值6ʼ)
- 10、说明：两张关联表，删除主表中已经在副表中没有的信息 delete from table1 where not exists ( select * from table2 where table1.field1=table2.field1


1、说明：四表联查问题： select * from a left i ner join b on a.a=b.b right i ner join c on a.a=c.c i ner join d on a.a=d.d where

.

- 12、说明：⽇程安排提前五分钟提醒 SQL: select * from ⽇程安排 where datedif(‘minute‘,f开始时间,getdate()>5
- 13、说明：⼀条sql 语句搞定数据库分⻚


- select top 10 b.* from (select top 20 主键字段,排序字段 from 表名 order by 排序字段 desc) a,表名 b where b.主键字段 = a.主键字段 order by a.排序字段
- 14、说明：前10条记录 select top 10 * form table1 where 范围
- 15、说明：选择在每⼀组b值相同的数据中对应的a最⼤的记录的所有信息(类似这样的⽤法可以⽤于论 坛每⽉排⾏榜,每⽉热销产品分析,按科⽬成绩排名,等等.) select a,b,c from tablename ta where a=(select max(a) from tablename tb where tb.b=ta.b)
- 16、说明：包括所有在 TableA 中但不在 TableB和TableC 中的⾏并消除所有重复⾏⽽派⽣出⼀个结果 表 (select a from tableA except (select a from tableB) except (select a from tableC)
- 17、说明：随机取出10条数据 select top 10 * from tablename order by newid()
- 18、说明：随机选择记录 select newid()
- 19、说明：删除重复记录 Delete from tablename where id not in (select max(id) from tablename group by col1,col2,.)
- 20、说明：列出数据库⾥所有的表名 select name from sysobjects where type=‘U‘
- 21、说明：列出表⾥的所有的 select name from syscolumns where id=object_id(‘TableName‘)


2、说明：列示type、vender、pcs字段，以type字段排列，case可以⽅便地实现多重选择，类似 select 中的case。 select type,sum(case vender when ‘A‘ then pcs else 0 end),sum(case vender when ‘C‘ then pcs else 0 end),sum(case vender when ‘B‘ then pcs else 0 end) FROM tablename group by type 显示结果： type vender pcs 电脑 A 1 电脑 A 1

光盘 B 2 光盘 A 2

- ⼿机 B 3
- ⼿机 C 3


- 23、说明：初始化表table1

TRUNCATE TABLE table1

- 24、说明：选择从10到15的记录 select top 5 * from (select top 15 * from table order by id asc) table_别名 order by id desc


三、技巧

- 1、1=1，1=2的使⽤，在SQL语句组合时⽤的较多

“where 1=1” 是表示选择全部 “where 1=2”全部不选， 如： if @strWhere !=‘ begin set @strSQL = ‘select count(*) as Total from [‘ + @tblName + ‘] where ‘ + @strWhere end else begin set @strSQL = ‘select count(*) as Total from [‘ + @tblName + ‘]‘ end

我们可以直接写成 set @strSQL = ‘select count(*) as Total from [‘ + @tblName + ‘] where 1=1 安定 ‘+ @strWhere

- 2、收缩数据库


- -重建索引 DBC REINDEX DBC INDEXDEFRAG
- -收缩数据和⽇志 DBC SHRINKDB DBC SHRINKFILE


- 3、压缩数据库 dbc shrinkdatabase(dbname)
- 4、转移数据库给新⽤户以已存在⽤户权限 exec sp_change_users_login ‘update_one,‘newname,‘oldname‘ go
- 5、检查备份集 RESTORE VERIFYONLY from disk=‘E:\dvbs.bak‘
- 6、修复数据库 Alter DATABASE [dvbs] SET SINGLE_USER GO DBC CHECKDB(‘dvbs‘,repair_alow_data_los) WITH TABLOCK GO Alter DATABASE [dvbs] SET MULTI_USER GO
- 7、⽇志清除 SET NOCOUNT ON DECLARE @LogicalFileName sysname,


@MaxMinutes INT, @NewSize INT

USE tablename - 要操作的数据库名 Select@LogicalFileName = ‘tablename_log‘, - ⽇志⽂件名 @MaxMinutes = 10, - Limit on time alowed to wrap log.

@NewSize = 1 - 你想设定的⽇志⽂件的⼤⼩(M)

- Setup / initialize

DECLARE @OriginalSize int Select @OriginalSize = size

FROM sysfiles Where name = @LogicalFileName

Select ‘Original Size of ‘ + db_name() + ‘ LOG is ‘ +

CONVERT(VARCHAR(30),@OriginalSize) + ‘ 8K pages or ‘ + CONVERT(VARCHAR(30),(@OriginalSize*8/1024) + ‘MB‘

FROM sysfiles Where name = @LogicalFileName

Create TABLE DumyTrans (DumyColumn char (8 0) not nul)

DECLARE @CounterINT, @StartTime DATETIME, @TruncLog VARCHAR(25)

Select@StartTime = GETDATE(), @TruncLog = ‘BACKUP LOG ‘ + db_name() + ‘ WITH TRUNCATE_ONLY‘

DBC SHRINKFILE (@LogicalFileName, @NewSize) EXEC (@TruncLog)

- Wrap the log if necesary.

WHILE @MaxMinutes > DATEDI F (mi, @StartTime, GETDATE()- time has not expired AND @OriginalSize = (Select size FROM sysfiles Where name = @LogicalFileName) AND (@OriginalSize * 8 /1024) > @NewSize

BEGIN- Outer l op. Select @Counter = 0 WHILE (@Counter < @OriginalSize / 16) AND (@Counter < 5 0)

BEGIN- update Insert DumyTrans VALUES (‘Fil Log‘) Delete DumyTrans Select @Counter = @Counter + 1

END

EXEC (@TruncLog) END

Select ‘Final Size of ‘ + db_name() + ‘ LOG is ‘ + CONVERT(VARCHAR(30),size) + ‘ 8K pages or ‘ + CONVERT(VARCHAR(30),(size*8/1024) + ‘MB‘

FROM sysfiles Where name = @LogicalFileName

Drop TABLE DumyTrans SET NOCOUNT OF

- 8、说明：更改某个表 exec sp_changeobjectowner ‘tablename,‘dbo‘
- 9、存储更改全部表


Create PROCEDURE dbo.User_ChangeObjectOwnerBatch @OldOwner as NVARCHAR(128), @NewOwner as NVARCHAR(128) AS

DECLARE @Name as NVARCHAR(128) DECLARE @Owneras NVARCHAR(128) DECLARE @OwnerName as NVARCHAR(128)

DECLARE curObject CURSOR FOR select ‘Name‘ = name,

‘Owner‘ = user_name(uid) from sysobjects where user_name(uid)=@OldOwner order by name

OPEN curObject FETCH NEXT FROM curObject INTO @Name, @Owner WHILE( @FETCH_STATUS=0) BEGIN if @Owner=@OldOwner begin

set @OwnerName = @OldOwner +‘. + rtrim(@Name) exec sp_changeobjectowner @OwnerName, @NewOwner

end

- select @name,@NewOwner,@OldOwner

FETCH NEXT FROM curObject INTO @Name, @Owner END

close curObject

dealocate curObject GO

- 10、SQL SERVER中直接循环写⼊数据 declare @i int set @i=1 while @i<30 begin


insert into test (userid) values(@i) set @i=@i+1

end

