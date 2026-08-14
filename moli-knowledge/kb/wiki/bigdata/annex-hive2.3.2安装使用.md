---
title: hive2.3.2安装使用.note（原文插图 annex）
slug: annex-hive2.3.2安装使用
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Hive/安装部署/hive2.3.2安装使用.note.md
related: [hive-数仓与-sql]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/garfieldcgf/p/813452.html

hive的安装简单⼀些,使⽤也⽐较简单,基础hadop搭建好之后,只要初始化⼀些⽬录和数据库就好了 安装需要做⼏件事:

- 1.设⽴⼀个数据源作为元数据存储的地⽅,默认是derby内嵌数据库,不过不允许远程连接,所以换成mysql
- 2.配置java路径和claspath路径 下载地址: htp:/mirors.shuosc.org/apache/hive/hive-2.3.2/ 发现⼀个问题:该地址会变化,所以不⼀定有效,可以到官⽹选 择: htp:/ w.apache.org/dyn/closer.cgi/hive/


解压后先配置hive环境变量

vi /etc/profile

添加:

export HIVE_HOME=/home/sri_udap/ap/apache-hive-2.3.2-bin export PATH=$PATH:$HIVE_HOME/bin

⽣效:

source /etc/profile

在conf⽬录下,拷⻉模板进⾏配置:

mv hive-default.xml.template hive-site.xml mv hive-env.sh.template hive-env.sh

先修改其他两个配置⽂件: 修改hadop的配置⽂件hadop-env.sh，修改内容如下：

export HADOP_CLASPATH=.:$CLASPATH:$HADOP_CLASPATH:$HADOP_HOME/bin

这⾥配置的claspath后,在后⾯执⾏hive初始化时仍然⼀直报java的类错误,查阅资料后,把他改成另⼀种 更可靠的⽅式:

<table>
  <tr>
    <th>![image 1](assets/imageFile1.png)</th>
  </tr>
</table>


for f in $HADOP_HOME/hadop-*.jar;do CLASPATH=${CLASPATH}:$f done

for f in $HADOP_HOME/lib/*.jar; do CLASPATH=${CLASPATH}:$f done

for f in $HIVE_HOME/lib/*.jar; do CLASPATH=${CLASPATH}:$f done

<table>
  <tr>
    <th>![image 2](assets/imageFile2.png)</th>
  </tr>
</table>


在⽬录$HIVE_HOME/bin下⾯，修改⽂件hive-env.sh，增加以下内容：

export HADOP_HOME=/home/sri_udap/ap/hadop-2.7.2 export HIVE_CONF_DIR=/home/sri_udap/ap/apache-hive-2.3.2-bin/conf export HIVE_AUX_JARS_PATH=/home/sri_udap/ap/apache-hive-2.3.2-bin/lib

修改hive-site.xml⽂件，修改内容如下：

<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


<property> <name>javax.jdo.option.ConectionURL</name> <value>jdbc:mysql:/master: 306/hive?createDatabaseIfNotExist=true</value>

</property> <property>

<name>javax.jdo.option.ConectionDriverName</name> <value>com.mysql.jdbc.Driver</value>

</property> <property>

<name>javax.jdo.option.ConectionUserName</name> <value>hivetest</value>

</property> <property>

<name>javax.jdo.option.ConectionPasword</name> <value>hivetest</value>

</property>

<table>
  <tr>
    <th>![image 4](assets/imageFile4.png)</th>
  </tr>
</table>


# 拷⻉⼀个mysql的连接jar包到lib⽬录下,我⽤的是 mysql-conector-java-5.1.30.jar 然后到hdfs上建⽴⼀些基础⽬录hive-site.xml中配置的仓库地址等,⼿⼯创建(包括配置的hive的数据⽬ 录,仓库地址,⽇志等,并赋权):

<table>
  <tr>
    <th>![image 5](assets/imageFile5.png)</th>
  </tr>
</table>


bin/hadop fs -mkdir -p /user/hive/warehouse bin/hadop fs -mkdir -p /user/hive/tmp bin/hadop fs -mkdir -p /user/hive/log bin/hadop fs -chmod -R 777 /user/hive/warehouse bin/hadop fs -chmod -R 777 /user/hive/tmp bin/hadop fs -chmod -R 777 /user/hive/log

<table>
  <tr>
    <th>![image 6](assets/imageFile6.png)</th>
  </tr>
</table>


这样就可以开始初始化了,先启动hadop,然后在bin⽬录下执⾏命令

./schematol -initSchema -dbType mysql

此时应该有个错误:

<table>
  <tr>
    <th>![image 7](assets/imageFile7.png)</th>
  </tr>
</table>


Exception in thread "main"java.lang.RuntimeException: java.lang.IlegalArgumentException:java.net.URISyntaxException: Relative path in absolute URI:${system:java.io.tmpdir%7D/$%7Bsystem:user.name%7D

atorg.apache.hadop.hive.ql.sesion.SesionState.start(SesionState.java: 4) atorg.apache.hadop.hive.cli.CliDriver.run(CliDriver.java:672) atorg.apache.hadop.hive.cli.CliDriver.main(CliDriver.java:616) atsun.reflect.NativeMethodAcesorImpl.invoke0(Native Method) atsun.reflect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:57) atsun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:43) atjava.lang.reflect.Method.invoke(Method.java:606) atorg.apache.hadop.util.RunJar.main(RunJar.java:160)

Caused by: java.lang.IlegalArgumentException:java.net.URISyntaxException: Relative path in absolute URI:${system:java.io.tmpdir%7D/$%7Bsystem:user.name%7D atorg.apache.hadop.fs.Path.initialize(Path.java:148) atorg.apache.hadop.fs.Path.<init>(Path.java:126) atorg.apache.hadop.hive.ql.sesion.SesionState.createSesionDirs(SesionState.java:487) atorg.apache.hadop.hive.ql.sesion.SesionState.start(SesionState.java:430)

. 7more Caused by: java.net.URISyntaxException:Relative path in absolute URI:${system:java.io.tmpdir%7D/$%7Bsystem:user.name%7D

atjava.net.URI.checkPath(URI.java:1804) atjava.net.URI.<init>(URI.java:752) atorg.apache.hadop.fs.Path.initialize(Path.java:145)

. 10more

<table>
  <tr>
    <th>![image 8](assets/imageFile8.png)</th>
  </tr>
</table>


这是因为⽆法识别"system:java.io.tmpdir",换成⾃⼰建⽴的临时⽬录就好,⽐如我的 是:/home/sri_udap/ap/apache-hive-2.3.2-bin/temp. 把hive-site.xml中有这个配置的都换掉.其实${system:user.name}这个变量也是不识别的,勤快的话把 这个也替换⼀下,把system:去掉即可,否则会出现跟我⼀样的情况,会建⽴奇怪的⽬录:

[rot@master temp]# ls 9c985e-f160-48d4-ab74-9d597c81b13_resources c1d4876-f1c9-4f97-bc3a-f9743fec417_resources ${system:user.name}

再进⾏⼀次初始化,然后可以看到mysql中建⽴了⼀些表,这样就完成了建⽴⼯作 简单使⽤:

建⽴⼏张表:(hive建⽴表后会在hdfs上多出⼀个和表明⼀样的⽬录,然后加载数据后会在⽬录下多出 ⽂件,在hive中,数据就是⽬录和⽂件) 新建两张表:

- hive>CREATE TABLE t1(idint); / 创建内部表t1，只有⼀个int类型的id字段

- hive>CREATE TABLE t2(idint, name string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'; / 创建内部表t2，有两个字 段，它们之间通过tab分隔 然后,按照字段分隔要求弄两个txt⽂件,并加载到表⾥⾯:


<table>
  <tr>
    <th>![image 9](assets/imageFile9.png)</th>
  </tr>
</table>


- [rot@master temp]# cat t1.txt


- 1

- 2

- 3

- 4

- 5

- 6

- 7 9


<table>
  <tr>
    <th>![image 10](assets/imageFile10.png)</th>
  </tr>
</table>


- [rot@master temp]# cat t2.txt


- 1 a

- 2 b

- 3 c 9 x 加载数据: hive>LOAD DATA LOCAL INPATH '/t1.txt' INTO TABLE t1; / 从本地⽂件加载 hive>LOAD DATA INPATH 't2.txt' INTO TABLE t1; / 从HDFS中加载 此时可以⽤⼀些简单的查询语句来查询hive,但是为了⽣成MapReduce作业,我们将语句写得稍微复杂 些:


<table>
  <tr>
    <th>![image 11](assets/imageFile11.png)</th>
  </tr>
</table>


hive> select t2.name from t1 left join t2 on t1.id = t2.id; WARNING: Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a diferent execution engine (i.e. spark, tez) or using Hive 1.X releases. Query ID= rot_2017128104347_a6396e5-d32a-41c9-a363-79aef39cac63 Total jobs= 1 SLF4J: Clas path contains multiple SLF4J bindings. SLF4J: Found binding in [jar:file:/home/sri_udap/ap/apache-hive-2.3.2-bin/lib/log4j-slf4j-impl2.6.2.jar!/org/slf4j/impl/StaticLogerBinder.clas] SLF4J: Found binding in [jar:file:/home/sri_udap/ap/hadop-2.7.2/share/hadop/co mon/lib/slf4j-log4j121.7.10.jar!/org/slf4j/impl/StaticLogerBinder.clas] SLF4J: Se htp:/ w.slf4j.org/codes.html#multiple_bindings for an explanation. SLF4J: Actual binding is of type [org.apache.loging.slf4j.Log4jLogerFactory]

- 2017-12-28 10 43 53 Starting to launch local task to proces map join; maximum memory = 932184064

- 2017-12-28 10 43 54 Dump the side-tablefor tag: 1 with group count: 4 into file: file:/home/sri_udap/ap/apache-hive-2.3.2bin/temp/${system:user.name}/9c985e-f160-48d4-ab74-9d597c81b13/hive_2017-12-28_10-4347_56_6806776839820490-1/-local-1 04/HashTable-Stage-3/MapJoin-mapfile31-.hashtable 2017-12-28 10 43 54 Uploaded 1 File to: file:/home/sri_udap/ap/apache-hive-2.3.2bin/temp/${system:user.name}/9c985e-f160-48d4-ab74-9d597c81b13/hive_2017-12-28_10-4347_56_6806776839820490-1/-local-1 04/HashTable-Stage-3/MapJoin-mapfile31-.hashtable (364 bytes) 2017-12-28 10 43 54 End of local task; Time Taken: 1.103 sec. Execution completed sucesfuly MapredLocal task suceded Launching Job1 out of 1 Number of reduce tasks is set to0 since there's no reduce operator Starting Job = job_15142421956_ 04, Tracking URL = htp:/master:808/proxy/aplication_15142421956_ 04/ Kil Co mand = /home/sri_udap/ap/hadop-2.7.2/bin/hadop job -kil job_15142421956_ 04 Hadop job informationfor Stage-3: number of mapers: 1; number of reducers: 0 2017-12-28 10  4 10,516 Stage-3 map = 0%, reduce = 0% 2017-12-28 10  4 16,416 Stage-3 map = 10%, reduce = 0%, Cumulative CPU 1. 8 sec MapReduce Total cumulative CPU time:1 seconds 80 msec Ended Job= job_15142421956_ 04 MapReduce Jobs Launched: Stage-Stage-3: Map: 1 Cumulative CPU: 1. 8 sec HDFS Read: 568 HDFS Write: 205 SUCES Total MapReduce CPU Time Spent:1 seconds 80 msec OK


- a

- b

- c


<table>
  <tr>
    <th>![image 12](assets/imageFile12.png)</th>
  </tr>
</table>


# 完,有问题欢迎交流
