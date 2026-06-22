使⽤hive⼀段时间以后，今天在使⽤的时候突然报错，如下：

hive> show databases;

FAILED: Error in metadata: java.lang.RuntimeException: Unable to instantiate org.apache.hadoop.hive.metastore.HiveMetaStoreClient

FAILED: Execution Error, return code 1 from org.apache.hadoop.hive.ql.exec.DDLTask

hive> exit;

退出后使⽤debug模式，发现有如下错误：

Caused by: java.util.zip.ZipException: error in opening zip file

at java.util.zip.ZipFile.open(Native Method)

at java.util.zip.ZipFile.<init>(ZipFile.java:132)

at java.util.jar.JarFile.<init>(JarFile.java:151)

at java.util.jar.JarFile.<init>(JarFile.java:115)

at org.datanucleus.plugin.NonManagedPluginRegistry.registerBundle(NonManagedPluginRegistry.java:350)

解决⽅法：

将HADOOP_HOME下的build⽬录删除或者重命名，这个错误的原因是我之前在本地⽤ant build了hadoop，hive在连 接元数据库读取相关表是会加载本地库，如果有版本或者编译错误，会导致hive的连接元数据读取数据异常。

