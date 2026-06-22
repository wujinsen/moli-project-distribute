在公司的虚拟机上运⾏hive计算，因为要计算的数据量较⼤，频繁，导致了服务器负载过⾼，mysql也 出现⽆法连接的问题，最后虚拟机出现The remote system refused the conection.重启虚拟机后，进 ⼊hive。 hive> show tables; 出现了下⾯的问题： FAILED: Eror in metadata: java.lang.RuntimeException: Unable to instantiate org.apache.hadop.hive.metastore.HiveMetaStoreClient FAILED: Execution Eror, return code 1 from org.apache.hadop.hive.ql.exec. DLTask FAILED: Eror in metadata: java.lang.RuntimeException: Unable to instantiate org.apache.hadop.hive.metastore.HiveMetaStoreClientFAILED: Execution Eror, return code 1 from org.apache.hadop.hive.ql.exec. DLTask ⽹上查找解决的办法： 看到有⼀些别⼈说明的原因，如：mysql的权限不够。修改数据库⽤户权限，或者hive/bin⽬录权限。 还有告诉要在hadop bin/hadop namenode -format⽬录的（这个办法很扯，⾃⼰去查看⼀下 hadop fs -ls /，hadop运⾏正常）。还有说要情况/tmp⽬录的。这些办法都不是真正解决问题的办 法。 ⽤下⾯的命令，重新启动hive

./hive -hiveconf hive.rot.loger=DEBUG,console hive> show tables; 能够看到更深层次的原因的是： Caused by: java.lang.reflect.InvocationTargetException at sun.reflect.NativeConstructorAcesorImpl.newInstance0(Native Method) at sun.reflect.NativeConstructorAcesorImpl.newInstance(NativeConstructorAcesorImpl.java:39) at sun.reflect.DelegatingConstructorAcesorImpl.newInstance(DelegatingConstructorAcesorImpl.j ava:27) at java.lang.reflect.Constructor.newInstance(Constructor.java:513) at org.apache.hadop.hive.metastore.MetaStoreUtils.newInstance(MetaStoreUtils.java:1076) … 23 more Caused by: javax.jdo.JDODataStoreException: Exception thrown obtaining schema column information from datastore NestedThrowables: com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErorException: Table ‘hive.DELETEME1370713761025′ doesnʼt exist 根据提示的信息，登陆mysql或者mysql客户端查看hive的数据库的表信息 mysql -u rot -p

mysql> use hive; mysql> show tables;

+ ⸻+ | Tables_in_hive |

+ ⸻+ | BUCKETING_COLS | | CDS | | COLUMNS_V2 | | DATABASE_PARAMS | | DBS | | DELETEME137067637267 | | DELETEME1370712928271 | | DELETEME13707134235 | | DELETEME137071358972 | | DELETEME1370713761025 | | DELETEME1370713792915 | | IDXS | | INDEX_PARAMS | | PARTITIONS | | PARTITION_KEYS | | PARTITION_KEY_VALS | | PARTITION_PARAMS | | PART_COL_PRIVS | | PART_COL_STATS | | PART_PRIVS | | SDS | | SD_PARAMS | | SEQUENCE_TABLE | | SERDES | | SERDE_PARAMS | | SKEWED_COL_NAMES | | SKEWED_COL_VALUE_LOC_MAP | | SKEWED_STRING_LIST | | SKEWED_STRING_LIST_VALUES | | SKEWED_VALUES | | SORT_COLS | | TABLE_PARAMS |

| TAB_COL_STATS | | TBLS | | TBL_COL_PRIVS | | TBL_PRIVS |

+ ⸻+ 36 rows in set (0.0 sec) 能够看到“DELETEME1370713792915”这个表，问题明确了，由于计算的压⼒过⼤，服务器停⽌响 应，mysql也停⽌了响应，mysql进程被异常终⽌，在运⾏中的mysql表数据异常，hive的元数据表异 常。 解决问题的办法有两个：

- 1. 直接在mysql中drop 异常提示中的table; mysql>drop table DELETEME1370713761025;
- 2. 保守的做法，根据DELETEME*表的结构，创建不存在的表 CREATE TABLE `DELETEME1370713792915` ( `UNUSED` int(1) NOT NUL ) ENGINE=I noDB DEFAULT CHARSET=latin1; 通过实践，第⼀个⽅法就能够解决问题，如果不⾏可以尝试第⼆个⽅法。 总结：hive、hadop的上层异常原因可能很多情况导致，⼀定要找到真正的问题原因，不能急于尝试 ⽹上异常的解决办法。hive的元数据依赖关系型数据库，⼀定做好数据库的备份 ⼜碰到⼀个hive的Exception，Caused by: org.apache.hadop.hive.ql.parse.SemanticException: Unable to fetch table X，追溯更直接的原因，找到Caused by: java.sql.SQLException: Got eror 28 from storage engine。 问题原因：服务器的根⽬录的/tmp中⽇志⽂件⽣成过多，磁盘空间满了。以SemanticException: Unable to fetch table关键字在gogle中搜索，发现结果很少，也没什么有价值的信息，搜索结果是这 种情况，说明这个问题不常⻅，很可能是其他低级错误导致的问题。标签:


