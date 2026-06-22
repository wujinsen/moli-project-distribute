# 1 Canot execute statement: imposible to write to binary log since BINLOG_FORMAT = STATEMENT…

当启动 Hive 的时候报错：

Caused by: javax.jdo.JDOException: Couldnt obtain a new sequence (unique id) : Cannot execute statement: impossible to write to binary log since BINLOG_FORMAT

- 1

- 2 NestedThrowables:

java.sql.SQLException: Cannot execute statement: impossible to write to binary log since BINLOG_FORMAT = STATEMENT and at least one table uses a storage engine limited to row-based logging. InnoDB is limited to row-logging when transaction isolation level is READ COMMITTED or READ UNCOMMITTED.123

- 3


= STATEMENT and at least one table uses a storage engine limited to row-based logging. InnoDB is limited to row-logging when transaction isolation level is READ COMMITTED or READ UNCOMMITTED.

这个问题是由于 hive 的元数据存储 MySQL 配置不当引起的，可以这样解决：

- 1 mysql> set global binlog_format='MIXED';1
- 2 For direct MetaStore DB conections, we donʼt suport retries at the client level.


当在 Hive 中创建表的时候报错：

create table years (year string, event string) row format delimited fields terminated by '\t';

- 1

FAILED: Execution Error, return code 1 from org.apache.hadoop.hive.ql.exec.DDLTask. MetaException(message:For direct MetaStore DB connections, we don't support retries at the client level.)12

- 2

1 mysql> alter database hive character set latin1;1

- 3 HiveConf of name hive.metastore.local does not exist


这是由于字符集的问题，需要配置 MySQL 的字符集：

当执⾏ Hive 客户端时候出现如下错误：

1 WARN conf.HiveConf: HiveConf of name hive.metastore.local does not exist1

这是由于在0.10 0.1或者之后的HIVE版本 hive.metastore.local 属性不再使⽤。将该参数从 hivesite.xml 删除即可。

# 4 Permision denied: user=anonymous, aces=EXECUTE, inode=”/tmp”

在启动 Hive 报如下错误：

(Permission denied: user=anonymous, access=EXECUTE, inode="/tmp":hadoop:supergroup:drwx------1

1

这是由于 Hive 没有 hdfs:/tmp ⽬录的权限，赋权限即可：

1 hadoop dfs -chmod -R 777 /tmp

