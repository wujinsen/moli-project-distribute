- 1.在随便⼀台有hadop环境的机器上解压缩：hive-0.9.0-bin.tar.gz tar -zxvf hive-0.9.0-bin.tar.gz mv hive-0.9.0-bin.tar.gz hive

- 2.修改hive/conf下的配置⽂件: cp hive-default.xml.template hive-default.xml cp hive-default.xml hive-site.xml cp hive-env.sh.template hive-env.sh

- 3.然后在hive-env.sh配置HADOP_HOME export HADOP_HOME=/home/soft01/hadop 最好配置⼀下jvm堆⼤⼩，否则使⽤jdbc服务的时候很容易内存溢出

- 4.配置环境变量HIVE_HOME和PATH export HIVE_HOME=/home/soft01/hive export PATH=$PATH:$HIVE_HOME/bin

- 5.通过命令⾏和web验证安装 hive >show databases; 运⾏web接⼝：hive-service hwi htp:/client: 9/hwi 如果不成功，⻅：《hive web⻚⾯的搭建》

- 6.启动hive的jdbc服务端thrift服务接⼝ hive-service hiveserver 5 0


hive异常 show tables ⽆法使⽤ : Unable to instantiate rg.apache.hadoop.hive.metastore.HiveMetaStoreClient 异常： hive> show tables; FAILED: Eror in metadata: java.lang.RuntimeException: Unable to instantiate rg.apache.hadop.hive.metastore.HiveMetaStoreClient FAILED: Execution Eror, return code 1 from org.apache.hadop.hive.ql.exec. DLTask

原因：在其他shel 开了hive 没有关闭 使⽤ ps -ef | grep hive

kil -9 杀死进程

create external table external_table1 (key string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' location '/home/hadop/hive/testable/partitionTable.txt';

