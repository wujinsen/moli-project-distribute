# Hive集成HBase，通过JDBC访问HBase映射Hive的表做统计查询时报错（报错信息如 下），错误提示是说HBaseSplit类找不到，但是在claspath中有这个类。后来gogle⼀ 下，⽹上解决⽅案是需要提供auxpath jar包，修改⼀下配置⽂件hive-site.xml，添加以下配 置，问题即解决。Mark ⼀下。。

- 1 <property>

- 2 <name>hive.aux.jars.path</name>

<value>file:///home/⽤户⽬录/hive-0.10.0/lib/hive-hbase-handler0.10.0.jar,file:///home/⽤户⽬录/hive-0.10.0/lib/hbase-0.92.0.jar,file:///home/⽤ 户⽬录/hive-0.10.0/lib/zookeeper-3.4.3.jar</value>

- 3

- 4 </property>


java.io.IOException: Cannot create an instance of InputSplit class = org.apache.hadoop.hive.hbase.HBaseSplit:org.apache.hadoop.hive.hbase.HBaseSplitat org.apache.hadoop.hive.ql.io.HiveInputFormat$HiveInputSplit.readFields(HiveInputFormat.java:146)at org.apache.hadoop.io.serializer.WritableSerialization$WritableDeserializer.deserialize(WritableSerialization.java:67) at org.apache.hadoop.io.serializer.WritableSerialization$WritableDeserializer.deserialize(WritableSerialization.java:40) at org.apache.hadoop.mapred.MapTask.getSplitDetails(MapTask.java:396)at org.apache.hadoop.mapred.MapTask.runOldMapper(MapTask.java:412)at org.apache.hadoop.mapred.MapTask.run(MapTask.java:372)at org.apache.hadoop.mapred.Child$4.run(Child.java:255)at java.security.AccessController.doPrivileged(Native Method)at javax.security.auth.Subject.doAs(Subject.java:396)at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1121)at org.apache.hadoop.mapred.Child.main(Child.java:249)Caused by: java.lang.ClassNotFoundException: org.apache.hadoop.hive.hbase.HBaseSplitat java.net.URLClassLoader$1.run(URLClassLoader.java:202)at java.security.AccessController.doPrivileged(Native Method)at java.net.URLClassLoader.findClass(URLClassLoader.java:190)at java.lang.ClassLoader.loadClass(ClassLoader.java:306)at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:301)at java.lang.ClassLoader.loadClass(ClassLoader.java:247)at java.lang.Class.forName0(Native Method)at java.lang.Class.forName(Class.java:249)at org.apache.hadoop.conf.Configuration.getClassByName(Configuration.java:820)at org.apache.hadoop.hive.ql.io.HiveInputFormat$HiveInputSplit.readFields(HiveInputFormat.java:143)

