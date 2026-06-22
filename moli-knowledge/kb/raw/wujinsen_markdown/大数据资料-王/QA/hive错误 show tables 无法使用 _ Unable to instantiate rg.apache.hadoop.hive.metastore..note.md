hive异常 show tables ⽆法使⽤ : Unable to instantiate rg.apache.hadoop.hive.metastore.HiveMetaStoreClient

异常： hive> show tables; FAILED: Eror in metadata: java.lang.RuntimeException: Unable to instantiate rg.apache.hadop.hive.metastore.HiveMetaStoreClient FAILED: Execution Eror, return code 1 from org.apache.hadop.hive.ql.exec. DLTask

原因：在其他shel 开了hive 没有关闭 使⽤ ps -ef | grep hive kil -9 杀死进程

