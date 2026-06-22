Hadop集群：Hadop2.6.0，系统：windows7，开发环境：eclipse

Eclipse调⽤Hadop运⾏MapReduce程序其实就是普通的java程序可以提交MR任务到集群执⾏⽽已。

- 1、⾸先需要配置环境变量： 在系统变量中新增：

然后再Path中增加：%HADOP_HOME%\bin;

- 2、需要在开发的MapReduce的main函数中指定配置如下： Configuration conf = new Configuration(); conf.setBolean("mapreduce.ap-submision.cros-platform",true);/ 配置使⽤跨平台提交任务 conf.set("fs.defaultFS","hdfs:/imageHandler1 9 0/tmp"); / 指定namenode conf.set("mapreduce.framework.name","yarn"); / 指定使⽤yarn框架 conf.set("yarn.resourcemanager.adres","imageHandler1 8032"); / 指定ResourceManager conf.set("yarn.resourcemanager.scheduler.adres","imageHandler1 8030");/指定资源分配器

- 3、在eclipse中运⾏main函数： 例如运⾏wordcount。


- （1）⾸先是出现类似下⾯的错误：

:30)

:47)

这个错误可以不⽤管，也可以在配置环境变量中的hadop的bin⽬录下加⼊winutils.exe⽂件。 然后会报⼀个权限错误，需要调整相应⽬录的权限，例如修改conf配置中namenode相应⽬录的权限，这⾥是/tmp， 在Hadop集群中执⾏：hdfs dfs -chmod 7 /tmp。

- （2）然后出现类似下⾯的错误：


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


2014-04-03 21 20 21,568 EROR [main] util.Shel (Shel.java:getWinUtilsPath(303)

- Failed to locate the winutils binary in the hadop binary path java.io.IOException: Could not locate executable nul\bin\winutils.exe in the Hadop binaries.

at org.apache.hadop.util.Shel.getQualifiedBinPath(Shel.java:278) at org.apache.hadop.util.Shel.getWinUtilsPath(Shel.

java

at org.apache.hadop.util.Shel.<clinit>(Shel.java:293) at

org.apache.hadop.util.StringUtils.<clinit>(StringUtils.java:76) at

org.apache.hadop.yarn.conf.YarnConfiguration.<clinit>(YarnConfiguration.java:345) at org.fansy.hadop.mr.WordCount.getConf(WordCount.java:104) at

org.fansy.hadop.mr.WordCount.runJob(WordCount.java:84) at org.fansy.hadop.mr.WordCount.main(WordCount.

java

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.


2014-04-03 20 32 36,596 EROR [main] security.UserGroupInformation (UserGroupInformation.java:doAs(1494) - PriviledgedActionException as:Administrator (auth:SIMPLE) cause:java.io.IOException: Failed to run job : Aplication aplication_1396459813671_ 01 failed 2 times due to AM Container for apatempt_1396459813671_ 01_ 02 exited with exitCode: 1 due to: Exception from container-launch: org.apache.hadop.util.Shel$ExitCodeException: /bin/bash: line 0: fg: no job control

at org.apache.hadop.util.Shel.runComand(Shel.

java

:464)

at

org.apache.hadop.util.Shel.run(Shel.java:379) at org.apache.hadop.util.Shel$ShelComandExecutor.execute(Shel.java:589) at

org.apache.hadop.yarn.server.nodemanager.DefaultContainerExecutor.launchContainer(Defa ultContainerExecutor.java:195)

at org.apache.hadop.yarn.server.nodemanager.containermanager.launcher.ContainerLaunch.cal (ContainerLaunch.java:283)

at org.apache.hadop.yarn.server.nodemanager.containermanager.launcher.ContainerLaunch.cal (ContainerLaunch.

java

:79)

at java.util.concurent.FutureTask$Sync.i nerRun(FutureTask.java: 34) at

java.util.concurent.FutureTask.run(FutureTask.java:16) at java.util.concurent.ThreadPolExecutor.runWorker(ThreadPolExecutor.java:145) at

java.util.concurent.ThreadPolExecutor$Worker.run(ThreadPolExecutor.java:615) at

java

.lang.Thread.run(Thread.java:724)

.Failing this atempt. Failing the aplication.

这时基本成功了⼀半，⽤上⾯的错误去gogle，可以得到这个⽹页： 。Hadop2.6.0只需修改

htps:/isues.apache.org/jira/browse/MA PREDUCE-565

YARNRuner.java即可，MRAps.java不需要修改。 YARNRuner.java修改如下：

在该类中搜索“ / Setup the comand to run the AM”，然后注释掉 “vargs.ad(MRAps.crosPlatformifyMREnv(jobConf,Environment.JAVA_HOME) +"/bin/java");”，在该⾏下⾯增 加： String remoteOs =conf.get("mapred.remote.os");

vargs.ad("Linux".equals(remoteOs)? "$JAVA_HOME/bin/java" : MRAps.crosPlatformifyMREnv(jobConf,Environment.JAVA_HOME) + "/bin/java"); 修改完该类后替换Eclipse中的jar包hadop-mapreduce-client-jobclient-2.6.0.jar中相应的类。 修改hadop-mapreduce-client-core-2.6.0.jar中的mapred-default.xml（只需修改eclipse中引⼊的jar包即可），增 加：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


<property> <name>mapred.remote.os</name> <value>Linux</value> <description>

Remote MapReduce framework's OS, can be either Linux or Windows

</description>

</property>

（题外话，添加了这个属性后，按说我new⼀个Configuration后，我使⽤conf.get("mapred.remote.os")的时候应该 是可以得到Linux的，但是我得到的却是nul，这个就不清楚是怎么了。）

- （3）这时再运⾏程序，还是报错，登录yarn主监控页⾯，查看log⽇志，可以看到下⾯的错误：


1.

Eror: Could not find or load main clas org.apache.hadop.mapreduce.v2.ap.MRApMaster

按照 中的解决办法，修改mapred-default.xml和 yarn-default.xml，分别在hadop-mapreduce-client-core-2.6.0.jar和hadop-yarn-comon-2.6.0.jar中（只需修 改eclipse中引⼊的jar包即可）。 在mapred-default.xml找到mapreduce.aplication.claspath，修改如下：

htps:/isues.apache.org/jira/browse/MAPREDUCE-565

<property> <name>mapreduce.aplication.claspath</name> <value> $HADOP_CONF_DIR,

$HADOP_COMON_HOME/share/hadop/co mon/*, $HADOP_COMON_HOME/share/hadop/co mon/lib/*, $HADOP_HDFS_HOME/share/hadop/hdfs/*, $HADOP_HDFS_HOME/share/hadop/hdfs/lib/*, $HADOP_MAPRED_HOME/share/hadop/mapreduce/*, $HADOP_MAPRED_HOME/share/hadop/mapreduce/lib/*, $HADOP_YARN_HOME/share/hadop/yarn/*, $HADOP_YARN_HOME/share/hadop/yarn/lib/*,

</value> </property>

在yarn-default.xml中找到yarn.aplication.claspath，修改如下：

<property> <name>yarn.aplication.claspath</name>

<value> $HADOP_CONF_DIR, $HADOP_COMON_HOME/share/hadop/co mon/*, $HADOP_COMON_HOME/share/hadop/co mon/lib/*, $HADOP_HDFS_HOME/share/hadop/hdfs/*,

$HADOP_HDFS_HOME/share/hadop/hdfs/lib/*, $HADOP_MAPRED_HOME/share/hadop/mapreduce/*, $HADOP_MAPRED_HOME/share/hadop/mapreduce/lib/*, $HADOP_YARN_HOME/share/hadop/yarn/*, $HADOP_YARN_HOME/share/hadop/yarn/lib/*

</value> </property>

- （4）经过上⾯的修改再次运⾏报错类似：


- 1.
- 2.
- 3.
- 4.


Caused by: java.lang.ClasNotFoundException: Clas org.fansy.hadop.mr.WordCount$WCMaper not found

at org.apache.hadop.conf.Configuration.getClasByName(Configuration.

java

:1626)

at org.apache.hadop.conf.Configuration.getClas(Configuration.java:1718)

. 8 more

需要上传wordcount程序的jar⽂件到$HADOP_HOME/share/hadop/mapreduce/lib下⾯（集群每台机器都要上 传），然后再次运⾏，成功了。

