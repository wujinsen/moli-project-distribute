- 1下载Sqop
- 2上载和解压缩
- 3⼀系列配置


- 3.1配置环境变量
- 3.2sqop配置⽂件修改


3.2.1 sqop-env.sh⽂件

- 3.2.1.1新建
- 3.2.1.2编辑内容


- 3.3将MySQL驱动包上载到Sqop的lib下

4使⽤sqop

- 4.1使⽤help命令


- 4.2使⽤Sqop查看MySQL中的数据表
- 4.3基于MySQL的表创建hive表


- 4.3.1创建

- 4.3.2测试

4.4将MySQL中的数据导⼊到hive中

- 4.4.1执⾏导⼊命令


- 4.4.2执⾏hive命令测试上⾯的操作是否成功


5报错和解决

- 5.1 java.net.NoRouteToHostException: No route to host(Host unreachable)
- 5.2 EROR tol.ImportTol: Eror during import: Importjob failed!


关键字：Linux CentOS Sqop Hadop Hive Java

版本号：CetOS7 Sqop1.4.6 Hadop2.8.0 Hive2.1.1

注意：本⽂只讲Sqop1.4.6的安装。和hive⼀样，sqop只需要在hadop的namenode上安装即 可。本例安装sqop的机器上已经安装了hdop2.8.0和hive2.1.1，hadop2.8.0的安装请参考博⽂：

htp:/blog.csdn.net/pucao_cug/article/details/71698903

hive2.1.1的安装请参考博⽂：

htp:/blog.csdn.net/pucao_cug/article/details/717365

1下载Sqop

因为官⽅并不建议在⽣产环境中使⽤1. 9.7版本，所以我们还是等2.0版本出来在⽤新的吧，现在 依然使⽤1.4.6版本

打开官⽹：

htp:/sqop.apache.org/

如图：

点击上图的nearby miror

相当于是直接打开：htp:/ w.apache.org/dyn/closer.lua/sqop/

如图：

我选择的是htp:/miror.bit.edu.cn/apache/sqop/

如图：

点击1.4.6，相当于是直接打开地址：

htp:/miror.bit.edu.cn/apache/sqop/1.4.6/

如图：

2上载和解压缩

在opt⽬录下新建⼀个名为sqop的⽬录，将下载得到的⽂件sqop-1.4.6.bin_hadop-2.0.4alpha.tar上载到该⽬录内

如图：

进⼊到该⽬录下，执⾏解压缩，也就是执⾏命令：

cd /opt/sqop

tar -xvf sqop-1.4.6.bin_hadop-2.0.4-alpha.tar.gz

命令执⾏完成后得到了/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha⽬录

3⼀系列配置

- 3.1 配置环境变量


编辑/etc/profile⽂件，添加SQOP_HOME变量，并且将$SQOP_HOME/bin添加到PATH变量 中，编辑⽅法很多，可以将profile⽂件下载到本地编辑，也可以直接⽤vim命令编辑。

添加的内容如下：

export JAVA_HOME=/opt/java/jdk1.8.0_121

export HADOP_HOME=/opt/hadop/hadop-2.8.0

export HADOP_CONF_DIR=${HADOP_HOME}/etc/hadop

export HADOP_COMON_LIB_NATIVE_DIR=${HADOP_HOME}/lib/native

export HADOP_OPTS="-Djava.library.path=${HADOP_HOME}/lib"

export HIVE_HOME=/opt/hive/apache-hive-2.1.1-bin

export HIVE_CONF_DIR=${HIVE_HOME}/conf

export SQOP_HOME=/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha

export CLAS_PATH=.:${JAVA_HOME}/lib:${HIVE_HOME}/lib:$CLAS_PATH

export PATH=.:${JAVA_HOME}/bin:${HADOP_HOME}/bin:${HADOP_HOME}/sbin: ${HIVE_HOME}/bin:${SQOP_HOME}/bin:$PATH

/etc/profile⽂件编辑完成后，执⾏命令：

source /etc/profile

- 3.2 Sqop配置⽂件修改


- 3.2.1 sqop-env.sh⽂件


- 3.2.1.1 新建

进⼊到/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/conf⽬录下，也就是执⾏命令：

cd /opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/conf

将sqop-env-template.sh复制⼀份，并取名为sqop-env.sh，也就是执⾏命令：

cp sqop-env-template.sh sqop-env.sh

如图：

- 3.2.1.2 编辑内容


编辑这个新建的sqop-env.sh⽂件，编辑⽅法有很多，可以下载到本地编辑，也可是使⽤vim命令 编辑。

在该⽂件末尾加⼊下⾯的配置：

export HADOP_COMON_HOME=/opt/hadop/hadop-2.8.0

export HADOP_MAPRED_HOME=/opt/hadop/hadop-2.8.0

export HIVE_HOME=/opt/hive/apache-hive-2.1.1-bin

说明：上⾯的路径修改为⾃⼰的hadop路径和hive路径。

- 3.3 将MySQL驱动包上载到Sqop的lib下

将MySQL的驱动包上载到Sqop安装⽬录的lib⼦⽬录下

如图：

说明：该驱动不是越旧越好，也不是越新越好，5.1.31我这⾥测试是可以的。

- 4 使⽤sqop


sqop是⼀个⼯具，安装完成后，如果操作的命令不涉及hive和hadop的，可以实现不启动hive和 hadop，直接输⼊sqop命令即可，例如sqop help命令。要使⽤hive相关的命令，必须事先启动 hive和hadop。

hadop的安装和启动可以参考该博⽂：

htp:/blog.csdn.net/pucao_cug/article/details/71698903

hive的安装和启动可以参考该博⽂：

htp:/blog.csdn.net/pucao_cug/article/details/717365

- 4.1 使⽤help命令


⾸先看看sqop都有哪些命令，在终上输⼊命令：

sqop help

如图：

上图中的内容是：

[rot@hserver1 ~]# sqop help Warning:/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./hbase does not exist! HBaseimports wil fail. Please set $HBASE_HOME to the rot of yourHBase instalation. Warning:/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./hcatalog does not exist! HCatalogjobs wil fail. Please set $HCAT_HOME to the rot of yourHCatalog instalation. Warning:/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./acumulo does not exist!Acumulo imports wil fail. Please set $ACUMULO_HOME to the rot ofyour Acumulo instalation. Warning:/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./zokeper does not exist!Acumulo imports wil fail. Please set $ZOKEPER_HOME to the rot ofyour Zokeper instalation. 17/05/14 16 21 30 INFO sqop.Sqop: RuningSqop version: 1.4.6 usage: sqop COMAND [ARGS]

Available comands: codegen Generate codeto interact with database records create-hive-table Import a tabledefinition into Hive eval Evaluate a SQLstatement and display the results

export Export an HDFS directory to adatabase table help List availablecomands import Import a tablefrom a database to HDFS import-al-tables Import tablesfrom a database to HDFS import-mainframeImport datasetsfrom a mainframe server to HDFS

job Work with savedjobs list-databases List availabledatabases on a server list-tables List availabletables in a database merge Merge resultsof incremental imports metastore Run astandalone Sqop metastore version Display versioninformation

Se 'sqop help COMAND' for information ona specific comand. [rot@hserver1 ~]#

说明：因为我们没有基于hadop安装HBase，所以HBase相关的命令不能⽤，但是操作hadop分 布式⽂件系统的命令是可以⽤的。

- 4.2 使⽤Sqop查看MySQL中的数据表

下⾯是使⽤命令查看MySQL数据库中的数据表list，命令是(命令中不能有回⻋，必须是在同⼀ ⾏，复制粘贴时候请注意)：

sqoplist-tables -username rot -pasword 'cj' -conect jdbc:mysql:/192.168.27.132  306/helo?characterEncoding=UTF-8

如图：

- 4.3 基于MySQL的表创建hive表


- 4.3.1 创建


注意：hive是基于hadop的HDFS的，在运⾏下⾯的导⼊命令请，请确保hadop和hive都在正常 运⾏。

现在MySQL数据库服务器上有⼀个数据库名为 helo，下⾯有⼀张表名为people

如图：

执⾏命令，在已经存在的db_hive_deu这个库中创建名为place的表：

sqop create-hive-table -conect jdbc:mysql:/192.168.19.132  306/helo? characterEncoding=UTF-8 -table people -usernamerot -pasword 'cj' -hivedatabase db_hive_edu

如图：

上图中的内容是：

[rot@hserver1 ~]# sqop create-hive-table -conect jdbc:mysql:/192.168.19.132  306/helo? characterEncoding=UTF-8-table people -username rot -pasword 'cj' -hive-database db_hive_edu Warning: /opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./hbasedoes not exist! HBase imports wil fail. Please set $HBASE_HOME to the rot of your HBase instalation. Warning: /opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./hcatalogdoes not exist! HCatalog jobs wil fail. Please set $HCAT_HOME to the rot of your HCatalog instalation. Warning: /opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./acumulodoes not exist! Acumulo imports wil fail. Please set $ACUMULO_HOME to the rot of your Acumulo instalation. Warning: /opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./zokeperdoes not exist! Acumulo imports wil fail. Please set $ZOKEPER_HOME to the rot of your Zokeperinstalation.

17/05/14 21 06 06 INFO sqop.Sqop: Runing Sqop version: 1.4.6 17/05/14 21 06 06 WARN tol.BaseSqopTol: Seting your pasword onthe comand-line is insecure. Consider using -P instead. 17/05/14 21 06 06 INFO tol.BaseSqopTol: Using Hive-specificdelimiters for output. You can overide 17/05/14 21 06 06 INFO tol.BaseSqopTol: delimiters with-fields-terminated-by, etc. 17/05/14 21 06 07 INFO manager.MySQLManager: Preparing to use aMySQL streaming resultset. 17/05/14 21 06 09 INFO manager.SqlManager: Executing SQL statement:SELECT t.* FROM `people` AS t LIMIT 1 17/05/14 21 06 09 INFO manager.SqlManager: Executing SQL statement:SELECT t.* FROM `people` AS t LIMIT 1 17/05/14 21 06 1 WARN util.NativeCodeLoader: Unable to loadnative-hadop library for your platform. using builtin-java clases whereaplicable 17/05/14 21 06 16 INFO hive.HiveImport: Loading uploaded data intoHive 17/05/14 21 06 26 INFO hive.HiveImport: which: no hbase in (.:/opt/java/jdk1.8.0_121/bin:/opt/hadop/hadop-2.8.0/bin:/opt/hadop/hadop2.8.0/sbin:/opt/hive/apache-hive-2.1.1-bin/bin:/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4alpha/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/rot/bin) 17/05/14 21 06 29 INFO hive.HiveImport: SLF4J: Clas path containsmultiple SLF4J bindings. 17/05/14 21 06 29 INFO hive.HiveImport: SLF4J: Found binding in[jar:file:/opt/hive/apache-hive2.1.1-bin/lib/log4j-slf4j-impl-2.4.1.jar!/org/slf4j/impl/StaticLogerBinder.clas] 17/05/14 21 06 29 INFO hive.HiveImport: SLF4J: Found binding in[jar:file:/opt/hadop/hadop2.8.0/share/hadop/comon/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLogerBinder.clas]

- 17/05/14 21 06 29 INFO hive.HiveImport: SLF4J: Sehtp:/ w.slf4j.org/codes.html#multiple_bindings for an explanation.

- 17/05/14 21 06 58 INFO hive.HiveImport:

- 17/05/14 21 06 58 INFO hive.HiveImport: Loging initialized usingconfiguration injar:file:/opt/hive/apache-hive-2.1.1-bin/lib/hive-comon-2.1.1.jar!/hive-log4j2.propertiesAsync: true
- 17/05/14 21 07 32 INFO hive.HiveImport: OK


- 17/05/14 21 07 32 INFO hive.HiveImport: Time taken: 6.657 seconds


- 17/05/14 21 07 32 INFO hive.HiveImport: Hive import complete. [rot@hserver1 ~]#


注意：create-hive-table只是创建hive表，并没有导⼊数据，导⼊数据的命令在4.4中讲到。

- 4.3.2 测试


在hive命令模式下，输⼊以下命令，切换到db_hive_edu数据库中，命令是：

use db_hive_edu;

如图：

在hive命令模式下，输⼊以下命令：

show tables;

如图：

- 4.4 将MySQL中的数据导⼊到hive中


- 4.4.1 执⾏导⼊命令


注意：hive是基于hadop的HDFS的，在运⾏下⾯的导⼊命令请，请确保hadop和hive都在正常 运⾏。需要额外说明的是，下⾯的命令不需要以4.3为前提，也就是说直接执⾏下⾯的命令即可，不需 要事先在hive上创建对应的表。

执⾏下⾯的命令(下⾯的命令在同⼀⾏内):

sqop import -conect jdbc:mysql:/192.168.19.132  306/helo?characterEncoding=UTF-8

- -table place -usernamerot-pasword 'cj' -fields-terminated-by ',' -hive-import -
- -hive-database db_hive_edu -m 1


如图：

上图中的内容是(完整的)：

[rot@hserver1~]# sqop import -conect jdbc:mysql:/192.168.19.132  306/helo? characterEncoding=UTF-8-table place -username rot-pasword 'cj' -fields-terminated-by ',' -hive-import -hive-databasedb_hive_edu-m 1 Warning: /opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./hbasedoes not exist! HBase imports wil fail. Please set $HBASE_HOME to the rot of yourHBase instalation. Warning:/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./hcatalog does not exist!HCatalog jobs wil fail. Please set $HCAT_HOME to the rot of yourHCatalog instalation. Warning:/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./acumulo does not exist!Acumulo imports wil fail. Please set $ACUMULO_HOME to the rot ofyour Acumulo instalation. Warning: /opt/sqop/sqop-1.4.6.bin_hadop-2.0.4-alpha/./zokeperdoes not exist! Acumulo imports wil fail. Please set $ZOKEPER_HOME to the rot ofyour Zokeper instalation.

- 17/05/14 20  4 07 INFO sqop.Sqop: RuningSqop version: 1.4.6
- 17/05/14 20  4 08 WARN tol.BaseSqopTol:Seting your pasword on the comand-line is insecure. Consider using -Pinstead. 17/05/14 20  4 08 INFOmanager.MySQLManager: Preparing to use a MySQL streaming resultset. 17/05/14 20  4 08 INFO tol.CodeGenTol:Begi ning code generation 17/05/14 20  4 10 INFO manager.SqlManager:Executing SQL statement: SELECT t.* FROM `place` AS t LIMIT 1 17/05/14 20  4 10 INFO manager.SqlManager:Executing SQL statement: SELECT t.* FROM `place` AS t LIMIT 1 17/05/14 20  4 10 INFOorm.CompilationManager: HADOP_MAPRED_HOME is /opt/hadop/hadop-2.8.0 Note:/tmp/sqop-rot/compile/382b047c7e935d9ba301474728b5f7/place.java uses oroverides a deprecated API. Note: Recompile with -Xlint:deprecation fordetails.


17/05/14 20  4 18 INFOorm.CompilationManager: Writing jar file:/tmp/sqoprot/compile/382b047c7e935d9ba301474728b5f7/place.jar 17/05/14 20  4 18 WARNmanager.MySQLManager: It l oks like you are importing from mysql. 17/05/14 20  4 18 WARNmanager.MySQLManager: This transfer can be faster! Use the-direct 17/05/14 20  4 18 WARNmanager.MySQLManager: option to exercise a MySQL-specific fast path. 17/05/14 20  4 18 INFOmanager.MySQLManager: Seting zero DATETIME behavior to convertToNul (mysql) 17/05/14 20  4 18 INFOmapreduce.ImportJobBase: Begi ning import of place 17/05/14 20  4 18 INFOConfiguration.deprecation: mapred.job.tracker is deprecated. Instead, usemapreduce.jobtracker.adres 17/05/14 20  4 20 WARNutil.NativeCodeLoader: Unable to load native-hadop library for yourplatform. using builtin-java clases where aplicable 17/05/14 20  4 20 INFOConfiguration.deprecation: mapred.jar is deprecated. Instead, usemapreduce.job.jar 17/05/14 20  4 24 INFOConfiguration.deprecation: mapred.map.tasks is deprecated. Instead, usemapreduce.job.maps 17/05/14 20  4 25 INFO client.RMProxy:Conecting to ResourceManager at hserver1/192.168.19.128 8032

- 17/05/14 20  4 41 INFO db.DBInputFormat:Using read comited transaction isolation
- 17/05/14 20  4 42 INFOmapreduce.JobSubmiter: number of splits:1
- 17/05/14 20  4 43 INFOmapreduce.JobSubmiter: Submiting tokens for job: job_149476474368_ 01 17/05/14 20  4 49 INFO impl.YarnClientImpl:Submited aplication aplication_149476474368_ 01 17/05/14 20  4 49 INFO mapreduce.Job: Theurl to track the job:htp:/hserver1 808/proxy/aplication_149476474368_ 01/ 17/05/14 20  4 49 INFO mapreduce.Job:Runing job: job_149476474368_ 01


- 17/05/14 20 45 40 INFO mapreduce.Job: Jobjob_149476474368_ 01 runing in uber mode : false

- 17/05/14 20 45 40 INFO mapreduce.Job: map 0% reduce 0%
- 17/05/14 20 46 21 INFO mapreduce.Job: map 10% reduce 0%


- 17/05/14 20 46 25 INFO mapreduce.Job: Jobjob_149476474368_ 01 completed sucesfuly 17/05/14 20 46 25 INFO mapreduce.Job:Counters: 30


File System Counters FILE: Number of bytes read=0 FILE: Number of byteswriten=154570 FILE: Number of readoperations=0

FILE: Number of large readoperations=0 FILE: Number of writeoperations=0 HDFS: Number of bytes read=87 HDFS: Number of byteswriten=10 HDFS: Number of readoperations=4 HDFS: Number of large readoperations=0 HDFS: Number of writeoperations=2

Job Counters Launched map tasks=1 Other local map tasks=1 Total time spent by al maps inocupied slots (ms)=34902 Total time spent by al reducesin ocupied slots (ms)=0

Total time spent by al maptasks (ms)=34902 Total vcore-miliseconds takenby al map tasks=34902 Total megabyte-milisecondstaken by al map tasks=35739648

Map-Reduce Framework Map input records=2 Map output records=2 Input split bytes=87 Spiled Records=0 Failed Shufles=0 Merged Map outputs=0 GC time elapsed (ms)=190 CPU time spent (ms)=2490 Physical memory (bytes)snapshot=102395904 Virtual memory (bytes)snapshot=2082492416 Total comited heap usage(bytes)=16318464

File Input Format Counters Bytes Read=0 File Output Format Counters Bytes Writen=10

- 17/05/14 20 46 25 INFOmapreduce.ImportJobBase: Transfered 10 bytes in 121.381 seconds (0.0824bytes/sec)

- 17/05/14 20 46 25 INFOmapreduce.ImportJobBase: Retrieved 2 records.
- 17/05/14 20 46 26 INFO manager.SqlManager:Executing SQL statement: SELECT t.* FROM `place` AS t LIMIT 1


- 17/05/14 20 46 26 INFO hive.HiveImport:Loading uploaded data into Hive


17/05/14 20 48 25 INFO hive.HiveImport:which: no hbase in(.:/opt/java/jdk1.8.0_121/bin:/opt/hadop/hadop-2.8.0/bin:/opt/hadop/hadop2.8.0/sbin:/opt/hive/apache-hive-2.1.1-bin/bin:/opt/sqop/sqop-1.4.6.bin_hadop-2.0.4alpha/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/rot/bin) 17/05/14 20 48 28 INFO hive.HiveImport:SLF4J: Clas path contains multiple SLF4J bindings. 17/05/14 20 48 28 INFO hive.HiveImport:SLF4J: Found binding in[jar:file:/opt/hive/apache-hive2.1.1-bin/lib/log4j-slf4j-impl-2.4.1.jar!/org/slf4j/impl/StaticLogerBinder.clas]

- 17/05/14 20 48 28 INFO hive.HiveImport:SLF4J: Found binding in[jar:file:/opt/hadop/hadop2.8.0/share/hadop/comon/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLogerBinder.clas]

- 17/05/14 20 48 28 INFO hive.HiveImport:SLF4J: Se htp:/ w.slf4j.org/codes.html#multiple_bindings for anexplanation.
- 17/05/14 20 49 48 INFO hive.HiveImport:

17/05/14 20 49 48 INFO hive.HiveImport:Loging initialized using configuration injar:file:/opt/hive/apache-hive-2.1.1-bin/lib/hive-comon-2.1.1.jar!/hive-log4j2.propertiesAsync: true 17/05/14 20 50 16 INFO hive.HiveImport: OK

- 17/05/14 20 50 16 INFO hive.HiveImport:Time taken: 3.735 seconds 17/05/14 20 50 18 INFO hive.HiveImport:Loading data to table db_hive_edu.place




- 17/05/14 20 50 20 INFO hive.HiveImport: OK

- 17/05/14 20 50 20 INFO hive.HiveImport:Time taken: 4.872 seconds
- 17/05/14 20 50 21 INFO hive.HiveImport:Hive import complete.


- 17/05/14 20 50 21 INFO hive.HiveImport:Export directory is contains the _SUCES file only, removing the directory. [rot@hserver1 ~]#


- 4.4.2 执⾏hive命令测试上⾯的操作是否成功


切换到刚才hive命令⾏（为了⽅便，我⼀般都是开了个连接，⼀个是普通的Linux命令模式，⼀ 个是启动hive，⽤于执⾏hive命令）。

在hive命令模式下，输⼊以下命令，切换到db_hive_edu数据库中，命令是：

use db_hive_edu;

如图：

在hive命令模式下，输⼊以下命令：

show tables;

如图：

在hive命令模式下，输⼊以下命令查看people表⾥的数据：

select * from place;

如图：

- 5 报错和解决


- 5.1 java.net.NoRouteToHostException: No route to host (Host unreachable)


检查你命令中的IP地址是否写错了，检查命令中的 -conect后⾯的IP地址，出现这个错误，往往 是这个地⽅写错了。

5.2 EROR tol.ImportTol: Eror during import: Import job failed!

在执⾏sqop import从MySql导出数据到hive时候报错，完整报错是：

java.lang.Exception:java.lang.RuntimeException: java.lang.ClasNotFoundException: Clas place notfound

atorg.apache.hadop.mapred.LocalJobRuner$Job.runTasks(LocalJobRuner.java:489)

atorg.apache.hadop.mapred.LocalJobRuner$Job.run(LocalJobRuner.java:549)

Caused by:java.lang.RuntimeException: java.lang.ClasNotFoundException: Clas place notfound

atorg.apache.hadop.conf.Configuration.getClas(Configuration.java: 216)

at org.apache.sqop.mapreduce.db.DBConfiguration.getInputClas(DBConfiguration.java:403)

atorg.apache.sqop.mapreduce.db.DataDrivenDBInputFormat.createDBRecordReader(DataDriven DBInputFormat.java:237)

atorg.apache.sqop.mapreduce.db.DBInputFormat.createRecordReader(DBInputFormat.java:263)

atorg.apache.hadop.mapred.MapTask$NewTrackingRecordReader.<init>(MapTask.java:515)

atorg.apache.hadop.mapred.MapTask.runNewMaper(MapTask.java:758)

atorg.apache.hadop.mapred.MapTask.run(MapTask.java:341)

atorg.apache.hadop.mapred.LocalJobRuner$Job$MapTaskRunable.run(LocalJobRuner.java:2 70)

atjava.util.concurent.Executors$RunableAdapter.cal(Executors.java:51)

atjava.util.concurent.FutureTask.run(FutureTask.java:26)

atjava.util.concurent.ThreadPolExecutor.runWorker(ThreadPolExecutor.java:142)

atjava.util.concurent.ThreadPolExecutor$Worker.run(ThreadPolExecutor.java:617)

atjava.lang.Thread.run(Thread.java:745)

Caused by: java.lang.ClasNotFoundException:Clas place not found

atorg.apache.hadop.conf.Configuration.getClasByName(Configuration.java:212)

atorg.apache.hadop.conf.Configuration.getClas(Configuration.java: 214)

. 12 more

- 17/05/14 2 17 45 INFO mapreduce.Job:Job job_local612327026_ 01 failed with state FAILED due to: NA

- 17/05/14 2 17 45 INFOmapreduce.Job: Counters: 0
- 17/05/14 2 17 46 WARNmapreduce.Counters: Group FileSystemCounters is deprecated. Useorg.apache.hadop.mapreduce.FileSystemCounter instead


- 17/05/14 2 17 46 INFOmapreduce.ImportJobBase: Transfered 0 bytes in 19.156 seconds (0 bytes/sec)


17/05/14 2 17 46 WARNmapreduce.Counters: Group org.apache.hadop.mapred.Task$Counter is deprecated.Use org.apache.hadop.mapreduce.TaskCounter instead

17/05/14 2 17 46 INFOmapreduce.ImportJobBase: Retrieved 0 records.

17/05/14 2 17 46 ERORtol.ImportTol: Eror during import: Import job failed!

原因：

和hadop的配置和有关系，需要启⽤yar这个mapreduce框架。

解决⽅法：

我的hadop安装路径是/opt/hadop/hadop-2.8.0/。

找到/opt/hadop/hadop-2.8.0/etc/hadop/mapred-site.xml配置⽂件，加⼊配置：

<property>

<name>mapreduce.framework.name</name>

<value>yarn</value>

</property>

如图：

⸻版权声明：本⽂为CSDN博主「陈南志」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原⽂ 出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/pucao_cug/article/details/72083172

