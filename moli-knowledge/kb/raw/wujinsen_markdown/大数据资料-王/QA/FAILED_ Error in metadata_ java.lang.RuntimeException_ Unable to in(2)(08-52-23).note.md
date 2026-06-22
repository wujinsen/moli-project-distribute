安装配置Hive时报错： FAILED: Eror in metadata: java.lang.RuntimeException: Unable to instantiate org.apache.hadop. hive.metastore.HiveMetaStoreClient FAILED: Execution Eror, return code 1 from org.apache.hadop.hive.ql.exec. DLTask

⽤调试模式报错如下： [rot@hadop1 bin]# hive -hiveconf hive.rot.loger=DEBUG,console 13/10/09 16 16 27 DEBUG comon.LogUtils: Using hivesite.xml found on CLASPATH at /opt/hive-0.1.0/conf/hive-site.xml 13/10/09 16 16 27 DEBUG conf.Configuration: java.io.IOException: config()

at org.apache.hadop.conf.Configuration.<init>(Configuration.java: 27) at org.apache.hadop.conf.Configuration.<init>(Configuration.java:214) at org.apache.hadop.hive.conf.HiveConf.<init>(HiveConf.java:1039) at org.apache.hadop.hive.cli.CliDriver.run(CliDriver.java:636) at org.apache.hadop.hive.cli.CliDriver.main(CliDriver.java:614) at sun.reflect.NativeMethodAcesorImpl.invoke0(Native Method) at sun.reflect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:39) at sun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:2

5)

at java.lang.reflect.Method.invoke(Method.java:597) at org.apache.hadop.util.RunJar.main(RunJar.java:156)

13/10/09 16 16 27 DEBUG conf.Configuration: java.io.IOException: config() at org.apache.hadop.conf.Configuration.<init>(Configuration.java: 27) at org.apache.hadop.conf.Configuration.<init>(Configuration.java:214) at org.apache.hadop.mapred.JobConf.<init>(JobConf.java: 30) at org.apache.hadop.hive.conf.HiveConf.initialize(HiveConf.java:1073) at org.apache.hadop.hive.conf.HiveConf.<init>(HiveConf.java:1040) at org.apache.hadop.hive.cli.CliDriver.run(CliDriver.java:636) at org.apache.hadop.hive.cli.CliDriver.main(CliDriver.java:614) at sun.reflect.NativeMethodAcesorImpl.invoke0(Native Method)

at sun.reflect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:39) at sun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:2

5)

at java.lang.reflect.Method.invoke(Method.java:597) at org.apache.hadop.util.RunJar.main(RunJar.java:156)

Loging initialized using configuration in file:/opt/hive-0.1.0/conf/hive-log4j.properties 13/10/09 16 16 27 INFO SesionState: Loging initialized using configuration in file:/opt/hive-0.1.0/conf/hive-log4j.properties 13/10/09 16 16 27 DEBUG parse.VariableSubstitution: Substitution is on: hive Hive history file=/tmp/rot/hive_job_log_rot_4 6@hadop1_20131091616_106970621.txt 13/10/09 16 16 27 INFO exec.HiveHistory: Hive history file=/tmp/rot/hive_job_log_rot_4 6@h adop1_20131091616_106970621.txt 13/10/09 16 16 27 DEBUG conf.Configuration: java.io.IOException: config()

at org.apache.hadop.conf.Configuration.<init>(Configuration.java: 27) at org.apache.hadop.conf.Configuration.<init>(Configuration.java:214) at org.apache.hadop.security.UserGroupInformation.ensureInitialized(UserGroupInformatio

n.java:187)

at org.apache.hadop.security.UserGroupInformation.isSecurityEnabled(UserGroupInformati on.java:239)

at org.apache.hadop.security.UserGroupInformation.getLoginUser(UserGroupInformation.ja va:438)

at org.apache.hadop.security.UserGroupInformation.getCurentUser(UserGroupInformation

.java:424) at org.apache.hadop.hive.shims.HadopShimsSecure.getUGIForConf(HadopShimsSecure

.java:491) at org.apache.hadop.hive.ql.security.HadopDefaultAuthenticator.setConf(HadopDefaultA

uthenticator.java:51) at org.apache.hadop.util.ReflectionUtils.setConf(ReflectionUtils.java:62) at org.apache.hadop.util.ReflectionUtils.newInstance(ReflectionUtils.java:17) at org.apache.hadop.hive.ql.metadata.HiveUtils.getAuthenticator(HiveUtils.java:365) at org.apache.hadop.hive.ql.sesion.SesionState.start(SesionState.java:270) at org.apache.hadop.hive.cli.CliDriver.run(CliDriver.java:670)

at org.apache.hadop.hive.cli.CliDriver.main(CliDriver.java:614) at sun.reflect.NativeMethodAcesorImpl.invoke0(Native Method) at sun.reflect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:39) at sun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:2

5)

at java.lang.reflect.Method.invoke(Method.java:597) at org.apache.hadop.util.RunJar.main(RunJar.java:156)

13/10/09 16 16 27 DEBUG security.Groups: Creating new Groups object 13/10/09 16 16 27 DEBUG security.Groups: Group maping impl=org.apache.hadop.security.She

lBasedUnixGroupsMaping; cacheTimeout=3 0 13/10/09 16 16 27 DEBUG security.UserGroupInformation: hadop login 13/10/09 16 16 27 DEBUG security.UserGroupInformation: hadop login comit 13/10/09 16 16 27 DEBUG security.UserGroupInformation: using local user:UnixPrincipal锛?rot 13/10/09 16 16 27 DEBUG security.UserGroupInformation: UGI loginUser:rot 13/10/09 16 16 27 DEBUG security.Groups: Returning fetched groups for 'rot' 13/10/09 16 16 27 DEBUG security.Groups: Returning cached groups for 'rot' 13/10/09 16 16 27 DEBUG conf.Configuration: java.io.IOException: config(config) at org.apache.hadop.conf.Configuration.<init>(Configuration.java:260) at org.apache.hadop.hive.conf.HiveConf.<init>(HiveConf.java:104) at org.apache.hadop.hive.ql.security.authorization.DefaultHiveAuthorizationProvider.init(De

faultHiveAuthorizationProvider.java:30) at org.apache.hadop.hive.ql.security.authorization.HiveAuthorizationProviderBase.setConf(

HiveAuthorizationProviderBase.java:108) at org.apache.hadop.util.ReflectionUtils.setConf(ReflectionUtils.java:62) at org.apache.hadop.util.ReflectionUtils.newInstance(ReflectionUtils.java:17) at org.apache.hadop.hive.ql.metadata.HiveUtils.getAuthorizeProviderManager(HiveUtils.jav

a: 39) at org.apache.hadop.hive.ql.sesion.SesionState.start(SesionState.java:272) at org.apache.hadop.hive.cli.CliDriver.run(CliDriver.java:670) at org.apache.hadop.hive.cli.CliDriver.main(CliDriver.java:614)

at sun.reflect.NativeMethodAcesorImpl.invoke0(Native Method) at sun.reflect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:39) at sun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:2

5)

at java.lang.reflect.Method.invoke(Method.java:597) at org.apache.hadop.util.RunJar.main(RunJar.java:156)

13/10/09 16 16 27 DEBUG conf.Configuration: java.io.IOException: config() at org.apache.hadop.conf.Configuration.<init>(Configuration.java: 27) at org.apache.hadop.conf.Configuration.<init>(Configuration.java:214) at org.apache.hadop.mapred.JobConf.<init>(JobConf.java: 30) at org.apache.hadop.hive.conf.HiveConf.initialize(HiveConf.java:1073) at org.apache.hadop.hive.conf.HiveConf.<init>(HiveConf.java:1045) at org.apache.hadop.hive.ql.security.authorization.DefaultHiveAuthorizationProvider.init(De

faultHiveAuthorizationProvider.java:30) at org.apache.hadop.hive.ql.security.authorization.HiveAuthorizationProviderBase.setConf(

HiveAuthorizationProviderBase.java:108) at org.apache.hadop.util.ReflectionUtils.setConf(ReflectionUtils.java:62) at org.apache.hadop.util.ReflectionUtils.newInstance(ReflectionUtils.java:17) at org.apache.hadop.hive.ql.metadata.HiveUtils.getAuthorizeProviderManager(HiveUtils.jav

a: 39) at org.apache.hadop.hive.ql.sesion.SesionState.start(SesionState.java:272) at org.apache.hadop.hive.cli.CliDriver.run(CliDriver.java:670) at org.apache.hadop.hive.cli.CliDriver.main(CliDriver.java:614) at sun.reflect.NativeMethodAcesorImpl.invoke0(Native Method) at sun.reflect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:39) at sun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:2

5)

at java.lang.reflect.Method.invoke(Method.java:597) at org.apache.hadop.util.RunJar.main(RunJar.java:156)

0

更多 分享到： 相关主题推荐： 相关帖⼦推荐：

logingobject调试login91

hive 或者 impala分⻚

新⼿求问，hadop集群中，master节点上会跑map/reduce任务吗

hadop遇到从节点没运⾏起来

⽤sqop从hive向mysql中导⼊数据时报错，求解

Nutch1.8 + Hadop2.3.0 在 inject 时出现 java.lang.IlegalArgumentException: Wrong FS

MapReduce中Partition问题

请教sqop1. 9.3将Oracle导⼊HDFS

wordcount程序在linux系统上运⾏成功，在windows上运⾏失败

| | 管理 回复次数： 1

对我有⽤[0]丢个板砖[0]引⽤ 举报

<table>
  <tr>
    <th rowspan="2">s060403072 等级：<br><br>关注 s060403072<br><br>![image 1](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile1.png>)<br><br>![image 2](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile2.png>)</th>
    <th>得分：0回复于： 2013-10-09 17 08 41 这个错误应该是你集成了mysql，从⽽报错。解决 ⽅法是修改hive-site.xml，参照：<br><br>也可参考：<br><br>#1<br><br>1 <property><br><br>2<br><br><name>javax.jdo.option.ConnectionU RL</name><br><br>3<br><br>4<br><br><value>jdbc:mysql://192.168.1.101: 3306/hive? createDatabaseIfNotExist=true</value ><br><br>5<br><br>6<br><br><description>JDBC connect string f or a JDBC metastore</description><br><br>7<br><br>8<br><br>9 </property><br><br><br>htp:/write.blog.csdn.net/postlist</th>
  </tr>
  <tr>
    <td>引⽤ 管理</td>
  </tr>
</table>


对我有⽤[0]丢个板砖[0] |举报 |

<table>
  <tr>
    <th rowspan="2">等级：<br><br>jxlhc09<br><br>![image 3](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile3.png>)</th>
    <th>得分：0回复于： 2013-10-09 18 05 12 引⽤ 1 楼 s060403072 的回复: 这个错误应该是你集成了mysql，从⽽报错。解决 ⽅法是修改hive-site.xml，参照：<br><br>也可参考：<br><br>嗯 +1看错误应该是元数据库配置有问题。<br><br>#2<br><br>1 <property><br><br>2<br><br><name>javax.jdo.option.Connect ionURL</name><br><br>3<br><br>4<br><br><value>jdbc:mysql://192.168.1. 101:3306/hive? createDatabaseIfNotExist=true</v alue><br><br>5<br><br>6<br><br><description>JDBC connect stri ng for a JDBC metastore</descrip tion><br><br>7<br><br>8<br><br>9 </property><br><br><br>htp:/write.blog.csdn.net/postlist</th>
  </tr>
  <tr>
    <td>引⽤ 管理</td>
  </tr>
</table>


# 对我有⽤[0]丢个板砖[0] |举报 |

得分：0回复于： 2013-10-09 18 10 32 hite-site.xml已经配置过了，如下： <property>

#3

- b0

等级：

关注

- b01


<name>hive.metastore.warehouse.dir </name>

![image 4](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile4.png>)

<value>/user/hive/warehouse</value> <description>locationof default datab

ase for the warehouse</description> </property>

<property> <name>hive.exec.scratchdir</name> <value>/usr/hive/temp</value> <description>Scratchspace for Hive jo

bs</description> </property>

<property>

<name>javax.jdo.option.ConectionUR L</name>

<value>jdbc:mysql:/hadop4  306/hi ve?characterEncoding=UTF-8</value>

</property>

<property>

<name>javax.jdo.option.ConectionDri verName</name>

<value>com.mysql.jdbc.Driver</value >

</property>

<property>

<name>javax.jdo.option.ConectionUs erName</name>

<value>hive</value>

<table>
  <tr>
    <th rowspan="2"> </th>
    <th></property><br><br><property><br><br><name>javax.jdo.option.ConectionPa sword</name><br><br><value>pasword</value> </property></th>
  </tr>
  <tr>
    <td>引⽤ 管理</td>
  </tr>
</table>


对我有⽤[0]丢个板砖[0] |举报 |

<table>
  <tr>
    <th rowspan="2">ohanaoh han 等级：<br><br>关注 zuochanxiaoheshang<br><br>![image 5](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile5.png>)</th>
    <th>得分：0回复于： 2013-10-09 2 59 27 检查⼀下MySQL的hive⽤户 （avax.jdo.option.ConectionUserName：hive) 在Hive安装的机器的IP上是否有⾜够的权限。<br><br>#4</th>
  </tr>
  <tr>
    <td>引⽤ 管理</td>
  </tr>
</table>


对我有⽤[0]丢个板砖[0] |举报 |

<table>
  <tr>
    <th rowspan="2">014040 等级：<br><br>关注 u01450470<br><br>![image 6](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile6.png>)</th>
    <th>得分：0回复于： 2014-03-25 21 19 02 我也遇到这个问题了 请问你是怎么解决的？<br><br>#5</th>
  </tr>
  <tr>
    <td>对我有⽤[0]丢个板砖[0]引⽤ |举报 | 管理</td>
  </tr>
</table>


<table>
  <tr>
    <th rowspan="2">erer 等级：<br><br>关注 mearer<br><br>![image 7](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile7.png>)</th>
    <th>得分：0回复于： 2014-03-26 10 13 23 我也遇到这个问题，搞了⼀天也没解决，求⽅法<br><br>#6<br><br>![image 8](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile8.png>)</th>
  </tr>
  <tr>
    <td>引⽤ 管理</td>
  </tr>
</table>


- 对我有⽤[0]丢个板砖[0] |举报 |


<table>
  <tr>
    <th rowspan="2">erer 等级：<br><br>关注 mearer<br><br>![image 9](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile9.png>)</th>
    <th>得分：0回复于： 2014-03-26 10 14 01 楼主，请问你解决这个问题了吗，能不能分享⼀ 下<br><br>#7</th>
  </tr>
  <tr>
    <td>对我有⽤[0]丢个板砖[0]引⽤ |举报 | 管理</td>
  </tr>
</table>


<table>
  <tr>
    <th rowspan="2">erer 等级：<br><br>关注 mearer<br><br>![image 10](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile10.png>)</th>
    <th>得分：0回复于： 2014-03-26 10 28 1 引⽤ 5 楼 u01450470 的回复: 我也遇到这个问题了 请问你是怎么解决的？<br><br>我找到问题所在了，如果你登陆mysql的IP是 locahost的话，那这⾥的配置应该修改为<br><br><property><br><br><name>javax.jdo.option.ConectionURL</nam e><br><br><value>jdc:mysql:/192.168.1.101  306/hive? createDatabaseIfNotExist=true</value><br><br><description>JDBC conect string for a JDBC metastore</description> </property> 把192.168.1.101改为localhost<br><br>#8</th>
  </tr>
  <tr>
    <td>引⽤ 管理</td>
  </tr>
</table>


# 对我有⽤[1]丢个板砖[0] |举报 |

得分：0回复于： 2014-05-14 2 15 25 楼主你好，我配置的hive创建了表，但是在 show databases、show tables的时候出现以下异 常：

#9

关注 u01326971

13 1 等级：

![image 11](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile11.png>)

FAILED: Execution Eror, return code 1 from or. apache.hadop.hive.ql.exec. DLTask. java.lang. RuntimeException: Unable to instantiate org.ap ache.hadop.hive.metastore.HiveMetaStoreClie nt

我⽤调试模式报的异常是 14/05/14 19 34 54 DEBUG conf.Configuration: j ava.io.IOException: config() at org.apache.hadop.conf.Configuration.<init> (Configuration.java:21) at org.apache.hadop.conf.Configuration.<init> (Configuration.java:198) at org.apache.hadop.hive.conf.HiveConf.<init>

- (HiveConf.java:1091)


at org.apache.hadop.hive.cli.CliDriver.run(CliDr iver.java:636)

at org.apache.hadop.hive.cli.CliDriver.main(Cli Driver.java:614)

at sun.reflect.NativeMethodAcesorImpl.invok e0(Native Method)

at sun.reflect.NativeMethodAcesorImpl.invok e(NativeMethodAcesorImpl.java:39)

14/05/14 19 34 54 DEBUG conf.Configuration: j ava.io.IOException: config()

at org.apache.hadop.conf.Configuration.<init> (Configuration.java:21)

at org.apache.hadop.conf.Configuration.<init> (Configuration.java:198)

at org.apache.hadop.mapred.JobConf.<init> (JobConf.java:173)

at org.apache.hadop.hive.conf.HiveConf.initiali ze(HiveConf.java:125)

at org.apache.hadop.hive.conf.HiveConf.<init>

- (HiveConf.java:1092)


at org.apache.hadop.hive.cli.CliDriver.run(CliDr iver.java:636)

at org.apache.hadop.hive.cli.CliDriver.main(Cli Driver.java:614)

at sun.reflect.NativeMethodAcesorImpl.invok e0(Native Method)

at sun.reflect.NativeMethodAcesorImpl.invok e(NativeMethodAcesorImpl.java:39)

at sun.reflect.DelegatingMethodAcesorImpl.i nvoke(DelegatingMethodAcesorImpl.java:25) at java.lang.reflect.Method.invoke(Method.java: 597) at org.apache.hadop.util.RunJar.main(RunJar.j ava:156)

Loging initialized using configuration in file:/op t/hive/conf/hive-log4j.properties

14/05/14 19 34  5 INFO SesionState: Loging initialized using configuration in file:/op t/hive/conf/hive-log4j.properties 14/05/14 19 34  5 DEBUG parse.VariableSubstit ution: Substitution is on: hive 14/05/14 19 34  5 DEBUG security.UsrGroupIn formation: Unix Login: rot,rot,bin,daemon,sys ,adm,disk,whel 14/05/14 19 34  5 DEBUG conf.Configuration: ja va.io.IOException: config(config) at org.apache.hadop.conf.Configuration.<init> (Configuration.java: 26) at org.apache.hadop.hive.conf.HiveConf.<init>

- (HiveConf.java:1096)


at org.apache.hadop.hive.ql.security.authoriza tion.DefaultHiveAuthorizationProvider.init(Defau ltHiveAuthorizationProvider.java:30)

at org.apache.hadop.hive.ql.security.authoriza tion.HiveAuthorizationProviderBase.setConf(Hiv eAuthorizationProviderBase.java:12)

at org.apache.hadop.util.ReflectionUtils.setCo nf(ReflectionUtils.java:62)

at org.apache.hadop.util.ReflectionUtils.newIn stance(ReflectionUtils.java:17)

at org.apache.hadop.hive.ql.metadata.HiveUtil s.getAuthorizeProviderManager(HiveUtils.java:3 39)

at org.apache.hadop.hie.ql.sesion.SesionSt ate.start(SesionState.java:280)

at org.apache.hadop.hive.cli.CliDriver.run(CliDr iver.java:670)

at org.apache.hadop.hive.cli.CliDriver.main(Cli Driver.java:614)

at sun.reflect.NativeMethodAcesorImpl.invok e0(Native Method)

at sun.reflect.NativeMethodAcesorImpl.invok e(NativeMethodAcesorImpl.java:39)

at sun.reflect.DelegatingMethodAcesorImpl.i nvoke(DelegatingMethodAcesorImpl.java:25) at java.lang.reflect.Method.invoke(Method.java: 597) at org.apache.hadop.util.RunJar.main(RunJar.j ava:156)

14/05/14 19 34  5 DEBUG conf.Configuration: ja va.io.IOException: config()

at org.apache.hadop.conf.Configuration.<init> (Configuration.java:21)

at org.apache.hadop.conf.Configuration.<init> (Configuration.java:198)

at org.apache.hadop.mapred.JobConf.<init> (JobConf.java:173)

at org.apache.hadop.hive.conf.HiveConf.initiali ze(HiveConf.java:125)

at org.apache.hadop.hive.conf.HiveConf.<init>

- (HiveConf.java:1097)


at org.apache.hadop.hive.ql.security.authoriza tion.DefaultHiveAuthorizationProvider.init(Defau ltHiveAuthorizationProvider.java:30)

<table>
  <tr>
    <th rowspan="2"> </th>
    <th>at org.apache.hadop.hive.ql.security.authoriza tion.HiveAuthorizationProviderBase.setConf(Hiv eAuthorizationProviderBase.java:12)<br><br>at org.apache.hadop.util.ReflectionUtils.setCo nf(ReflectionUtils.java:62)<br><br>at org.apache.hadop.util.ReflectionUtils.newIn stance(ReflectionUtils.java:17)<br><br>at org.apache.hadop.hive.ql.metadata.HiveUtil s.getAuthorizeProviderManager(HiveUtils.java:3 39)<br><br>at org.apache.hadop.hie.ql.sesion.SesionSt ate.start(SesionState.java:280)<br><br>at org.apache.hadop.hive.cli.CliDriver.run(CliDr iver.java:670)<br><br>at org.apache.hadop.hive.cli.CliDriver.main(Cli Driver.java:614)<br><br>at sun.reflect.NativeMethodAcesorImpl.invok e0(Native Method)<br><br>at sun.reflect.NativeMethodAcesorImpl.invok e(NativeMethodAcesorImpl.java:39)<br><br>at sun.reflect.DelegatingMethodAcesorImpl.i nvoke(DelegatingMethodAcesorImpl.java:25) at java.lang.reflect.Method.invoke(Method.java: 597) at org.apache.hadop.util.RunJar.main(RunJar.j ava:156)<br><br>请楼主帮忙看⼀下，可能是什么原因造成的</th>
  </tr>
  <tr>
    <td>引⽤ 管理</td>
  </tr>
</table>


# 对我有⽤[0]丢个板砖[0] |举报 |

<table>
  <tr>
    <th rowspan="2">yyy 等级：<br><br>关注 lyayfy<br><br>![image 12](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile12.png>)</th>
    <th>得分：0回复于： 2014-06-25 17 13 15 楼主，我报了同样的问题，想问你解决这个问题 没有。！！！！<br><br>#10</th>
  </tr>
  <tr>
    <td>对我有⽤[0]丢个板砖[0]引⽤ |举报 | 管理</td>
  </tr>
</table>


<table>
  <tr>
    <th rowspan="2">t t 等级：<br><br>关注 tjytad1982<br><br>![image 13](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile13.png>)</th>
    <th>得分：0回复于： 2014-06-25 17 59  3 学习<br><br>#1<br><br>![image 14](<FAILED_ Error in metadata_ java.lang.RuntimeException_ Unable to in(2)(08-52-23).note_images/imageFile14.png>)</th>
  </tr>
  <tr>
    <td> </td>
  </tr>
</table>


