MyISAM是MySQL的默认存储引擎，基于传统的ISAM类型，⽀持全⽂搜索，但不是事务安全的，⽽且不⽀持外键。每张 MyISAM表存放在三个⽂件中：frm ⽂件存放表格定义；数据⽂件是MYD (MYData)；索引⽂件是MYI (MYIndex)。 InnoDB是事务型引擎，⽀持回滚、崩溃恢复能⼒、多版本并发控制、ACID事务，⽀持⾏级锁定（InnoDB表的⾏锁不是绝对 的，如果在执⾏⼀个SQL语句时MySQL不能确定要扫描的范围，InnoDB表同样会锁全表，如like操作时的SQL语句），以及 提供与Oracle类型⼀致的不加锁读取⽅式。InnoDB存储它的表和索引在⼀个表空间中，表空间可以包含数个⽂件。 主要区别：

MyISAM是⾮事务安全型的，⽽InnoDB是事务安全型的。 MyISAM锁的粒度是表级，⽽InnoDB⽀持⾏级锁定。 MyISAM⽀持全⽂类型索引，⽽InnoDB不⽀持全⽂索引。 MyISAM相对简单，所以在效率上要优于InnoDB，⼩型应⽤可以考虑使⽤MyISAM。 MyISAM表是保存成⽂件的形式，在跨平台的数据转移中使⽤MyISAM存储会省去不少的麻烦。 InnoDB表⽐MyISAM表更安全，可以在保证数据不会丢失的情况下，切换⾮事务表到事务表（alter table tablename type=innodb）。

应⽤场景：

MyISAM管理⾮事务表。它提供⾼速存储和检索，以及全⽂搜索能⼒。如果应⽤中需要执⾏⼤量的SELECT查询，那么 MyISAM是更好的选择。 InnoDB⽤于事务处理应⽤程序，具有众多特性，包括ACID事务⽀持。如果应⽤中需要执⾏⼤量的INSERT或UPDATE操 作，则应该使⽤InnoDB，这样可以提⾼多⽤户并发操作的性能。

常⽤命令：

- （1）查看表的存储类型（三种）：

- （2）修改表的存储引擎：

- （3）启动mysql数据库的命令⾏中添加以下参数使新发布的表都默认使⽤事务：

- （4）临时改变默认表类型：


show create table tablename show table status from dbname where name=tablename mysqlshow -u user -p password --status dbname tablename

alter table tablename type=InnoDB

--default-table-type=InnoDB

set table_type=InnoDB show variables like 'table_type'

写代码是⼀种艺术，甚于蒙娜丽莎的微笑。

