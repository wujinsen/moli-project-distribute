为了增加hbase集群的可⽤性，可以为hbase增加多个backup master。当master挂掉后，backup master可以⾃动接管整个hbase的集群。 配置backup master的⽅式：

- 1、在hbase的conf下增加⽂件backup-masters，在该⽂件⾥⾯增加backup master的机器列表，每台 机器⼀条记录。 如：

- [hadop@hadop01 conf]$ cat backup-masters hadop02

2、整个集群启动后，在hadop02的机器上也会启动hmaster的进程：

- [hadop@hadop02 logs]$ jps 4301 Jps 4175 HMaster


- 3、查看hadop02上该master的log，可以看到如下的信息：

- 2012-04-10 05 53 10,120 INFO org.apache.hadop.hbase.master.ActiveMasterManager: Another master is the active master, hadop01,6 0,13408045435; waiting to become the next active master 该信息说明，当前hbase集群有活动的master节点，该master节点为hadop01，所以hadop02节点 开始等待，直到hadop01上的master挂掉。hadop02会变成新的hmaster节点。

4、当当前的master挂掉后，backup master会接管，进⽽变成新的active master

- 2012-04-10 06 48 52,436 DEBUG org.apache.hadop.hbase.master.ActiveMasterManager: No master available. Notifying waiting threads 2012-04-10 06 48 52,438 INFO org.apache.hadop.hbase.master.ActiveMasterManager: Master=hadop02,6 0,13401638701 2012-04-10 06 48 52, 43 DEBUG org.apache.hadop.hbase.master.ActiveMasterManager: A master is now available 原⽂地址：htp:/ w.oratea.net/?p=17




