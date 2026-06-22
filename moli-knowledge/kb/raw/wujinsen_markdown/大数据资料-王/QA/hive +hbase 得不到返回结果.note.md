在hive中建⽴外表链接到hbase的表，在做复杂查询时发现得不到结果返回。都是hive 0.9 版本。 远程客户端错误： Caused by: java.sql.SQLException: Query returned non-zero code: 9, cause: FAILED: Execution Eror, return code 2 from org.apache.hadop.hive.ql.exec.MapRedTask

at org.apache.hadop.hive.jdbc.HivePreparedStatement.executeI mediate(HivePreparedStatement.j ava:17)

at org.apache.hadop.hive.jdbc.HivePreparedStatement.executeQuery(HivePreparedStatement.java:1 40)

at sun.reflect.NativeMethodAcesorImpl.invoke0(Native Method) at sun.reflect.NativeMethodAcesorImpl.invoke(Unknown Source) at sun.reflect.DelegatingMethodAcesorImpl.invoke(Unknown Source) at java.lang.reflect.Method.invoke(Unknown Source) at

org.hibernate.engine.jdbc.internal.proxy.AbstractStatementProxyHandler.continueInvocation(Abstr actStatementProxyHandler.java:12)

. 8 more Caused by: HiveServerException(mesage:Query returned non-zero code: 9, cause: FAILED: Execution Eror, return code 2 from org.apache.hadop.hive.ql.exec.MapRedTask, erorCode:9, SQLState:08S01)

at org.apache.hadop.hive.service.ThriftHive$execute_result.read(ThriftHive.java:1318) at org.apache.thrift.TServiceClient.receiveBase(TServiceClient.java:78) at org.apache.hadop.hive.service.ThriftHive$Client.recv_execute(ThriftHive.java:105) at org.apache.hadop.hive.service.ThriftHive$Client.execute(ThriftHive.java:92) at

org.apache.hadop.hive.jdbc.HivePreparedStatement.executeI mediate(HivePreparedStatement.j ava:175)

. 94 more

hadop ⽇志：

发帖于 3年前 回/238阅 标签：

- 0

1年前 把相关的包导⼊进hive 修改hive-site.xml 添加（以下是cloudera的）：

java.io.IOException: Cannot create an instance of InputSplit class = org.apache.hadoop.hive.hbase.HBaseSplit:org.apache.hadoop.hive.hbase.HBaseSplit

- 1

> at org.apache.hadoop.hive.ql.io.HiveInputFormat$HiveInputSplit.readFields(HiveInput Format.java:145)

- 2

> at org.apache.hadoop.io.serializer.WritableSerialization$WritableDeserializer.deser ialize(WritableSerialization.java:67)

- 3

> at org.apache.hadoop.io.serializer.WritableSerialization$WritableDeserializer.deser ialize(WritableSerialization.java:40)

- 4

- 5 > at org.apache.hadoop.mapred.MapTask.getSplitDetails(MapTask.java:348)

- 6 > at org.apache.hadoop.mapred.MapTask.runOldMapper(MapTask.java:364)

- 7 > at org.apache.hadoop.mapred.MapTask.run(MapTask.java:324)

- 8 > at org.apache.hadoop.mapred.Child$4.run(Child.java:268)

- 9 > at java.security.AccessController.doPrivileged(Native Method)

- 10 > at javax.security.auth.Subject.doAs(Subject.java:415)

> at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1 115)

- 11

- 12 > at org.apache.hadoop.mapred.Child.main(Child.java:262)


蓝必照

- 1 HiveHBase


举报

| 分享到 收藏(0)

# 按票数排序 显示最新答案 共有1个答案 (最后回答: 1年前)

rainys.

- 1 <property>

- 2 <name>hive.aux.jars.path</name>

<value>file:///opt/cloudera/parcels/CDH-4.4.01.cdh4.4.0.p0.39/lib/hbase/hbase.jar,file:///opt/cloudera/parcels/CDH-4.4.01.cdh4.4.0.p0.39/lib/hive/lib/hive-hbase-handler-0.10.0cdh4.4.0.jar,file:///opt/cloudera/parcels/CDH-4.4.01.cdh4.4.0.p0.39/lib/zookeeper/zookeeper.jar</value>

- 3

- 4 </property>

- 5 <property>

- 6 <name>hbase.zookeeper.quorum</name>

- 7 <value>zookeeper的主机名</value>

- 8 </property>


