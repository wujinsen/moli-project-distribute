今天运⾏hbase的时候发现这个错误：

ERROR: org.apache.hadoop.hbase.MasterNotRunningException: Retried 7 times

查看log,发现⼤量的

2012-04-26 08:13:39,600 INFO org.apache.hadoop.hbase.util.FSUtils: Waiting for dfs to exit safe mode...

原来hdfs还处于安全模式

./hadoop fsck /

/hbase/.logs/slave1,6020,1 3159627316/slave1%2C6020%2C1 3159627316.1 3159637 4 : Under replicated blk_-4160280973 47327_1626. Target Replicas is 3 but found 2 replica(s).

. /home/hadop/tmp/mapred/staging/hadop/.staging/job_20120321238_ 02/job.jar: Under replicated blk_-78075190847542360_1012. Target Replicas is 10 but found 2 replica(s).

.Status: HEALTHY

Corupt blocks: 0 Mising replicas: 9 (3.061245 %) Number of data-nodes: 2

没有损坏的block,有9个丢失的replicas,状态健康

所以可以强制离开安全模式

hadoop dfsadmin -safemode get Warning: $HADOOP_HOME is deprecated.

Safe mode is ON hadoop dfsadmin -safemode leave Warning: $HADOOP_HOME is deprecated.

Safe mode is OFF

运⾏hbase命令成功

