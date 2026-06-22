起因是我重装了mysql数据库。

安装之后 把访问权限都配置好 ：

GRANT ALL PRIVILEGES ON*.* TO Identified by 'hive';

'hive'@'%'

GRANT ALL PRIVILEGES ON*.* TO Identified by 'hive';

'hive'@'localhost'

GRANT ALL PRIVILEGES ON*.* TO Identified by 'hive';

'hive'@'127.0.0.1'

本机地址： 192.168.103.43 机器名字：192-168-103-43

flush privileges;

启动hive 发⽣下⾯的错误：

hive> show tables;

FAILED: Error in metadata: java.lang.RuntimeException: Unable to instantiate org.apache.hadoop.hive.metastore.HiveMetaStoreClient

FAILED: Execution Error, return code 1 from org.apache.hadoop.hive.ql.exec.DDLTask

FAILED: Error in metadata: java.lang.RuntimeException: Unable to instantiate org.apache.hadoop.hive.metastore.HiveMetaStoreClientFAILED: Execution Error, return code 1 from org.apache.hadoop.hive.ql.exec.DDLTask

cd ${HIVE_HOME}/bin

./hive -hiveconf hive.root.logger=DEBUG,console

hive> show tables;

得到如下的错误信息(当然 不同的问题所产⽣的⽇志是不同的)：

Caused by: javax.jdo.JDOFatalDataStoreException: Access denied for user (using password: YES)

'hive'@'192-168-103

-43'

NestedThrowables:

java.sql.SQLException: Access denied for user (using password: YES)

'hive'@'192-168-103-43'

at org.datanucleus.jdo.NucleusJDOHelper.getJDOExceptionForNucleusException(NucleusJDOHel per.java:298)

at org.datanucleus.jdo.JDOPersistenceManagerFactory.freezeConfiguration(JDOPersistenceMana gerFactory.java:601)

at org.datanucleus.jdo.JDOPersistenceManagerFactory.createPersistenceManagerFactory(JDOPe rsistenceManagerFactory.java:286)

at org.datanucleus.jdo.JDOPersistenceManagerFactory.getPersistenceManagerFactory(JDOPersis tenceManagerFactory.java:182)

at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)

at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)

at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)

at java.lang.reflect.Method.invoke(Method.java:597)

at javax.jdo.JDOHelper$16.run(JDOHelper.java:1958)

at java.security.AccessController.doPrivileged(Native Method)

at javax.jdo.JDOHelper.invoke(JDOHelper.java:1953)

at javax.jdo.JDOHelper.invokeGetPersistenceManagerFactoryOnImplementation(JDOHelper.java:1 159)

at javax.jdo.JDOHelper.getPersistenceManagerFactory(JDOHelper.java:803)

at javax.jdo.JDOHelper.getPersistenceManagerFactory(JDOHelper.java:698)

at org.apache.hadoop.hive.metastore.ObjectStore.getPMF(ObjectStore.java:262)

at org.apache.hadoop.hive.metastore.ObjectStore.getPersistenceManager(ObjectStore.java:291)

at org.apache.hadoop.hive.metastore.ObjectStore.initialize(ObjectStore.java:224)

at org.apache.hadoop.hive.metastore.ObjectStore.setConf(ObjectStore.java:199)

at org.apache.hadoop.util.ReflectionUtils.setConf(ReflectionUtils.java:62)

at org.apache.hadoop.util.ReflectionUtils.newInstance(ReflectionUtils.java:117)

at org.apache.hadoop.hive.metastore.RetryingRawStore.<init>(RetryingRawStore.java:62)

at org.apache.hadoop.hive.metastore.RetryingRawStore.getProxy(RetryingRawStore.java:71)

at org.apache.hadoop.hive.metastore.HiveMetaStore$HMSHandler.newRawStore(HiveMetaStore.j ava:413)

at org.apache.hadoop.hive.metastore.HiveMetaStore$HMSHandler.getMS(HiveMetaStore.java:401 )

at org.apache.hadoop.hive.metastore.HiveMetaStore$HMSHandler.createDefaultDB(HiveMetaStor e.java:439)

at org.apache.hadoop.hive.metastore.HiveMetaStore$HMSHandler.init(HiveMetaStore.java:325)

at org.apache.hadoop.hive.metastore.HiveMetaStore$HMSHandler.<init> (HiveMetaStore.java:285)

at org.apache.hadoop.hive.metastore.RetryingHMSHandler.<init> (RetryingHMSHandler.java:53)

at org.apache.hadoop.hive.metastore.RetryingHMSHandler.getProxy(RetryingHMSHandler.java:58 )

at org.apache.hadoop.hive.metastore.HiveMetaStore.newHMSHandler(HiveMetaStore.java:4102)

at org.apache.hadoop.hive.metastore.HiveMetaStoreClient.<init> (HiveMetaStoreClient.java:121)

... 28 more

发现数据库的权限 HIVE需要的是

'hive'@'192-168-103-43'

这个IP地址

然后试着在mysql中加上权限：

GRANT ALL PRIVILEGES ON*.* TO Identified by 'hive';

'hive'@'192-168-103-43'

flush privileges;

再次登录hive

hive> show tables;

OK

能正常的查询表了，希望能帮到有同样问题的⼈。

