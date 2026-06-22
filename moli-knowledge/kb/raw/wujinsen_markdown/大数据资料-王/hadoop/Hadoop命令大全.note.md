在这篇⽂章中，我们默认认为Hadop环境已经由运维⼈员配置好直接可以使⽤。 假设Hadop的安装⽬录HADOP_HOME为/home/admin/hadop。

# 启动与关闭

启动HADOP

- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/start-al.sh


关闭HADOP

- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/stop-al.sh


# ⽂件操作

Hadop使⽤的是HDFS，能够实现的功能和我们使⽤的磁盘系统类似。并且⽀持通配符，如*。

查看⽂件列表

查看hdfs中/user/admin/aron⽬录下的⽂件。

- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/hadop fs -ls /user/admin/aron 这样，我们就找到了hdfs中/user/admin/aron⽬录下的⽂件了。 我们也可以列出hdfs中/user/admin/aron⽬录下的所有⽂件（包括⼦⽬录下的⽂件）。


- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/hadop fs -lsr /user/admin/aron


创建⽂件⽬录

查看hdfs中/user/admin/aron⽬录下再新建⼀个叫做newDir的新⽬录。

- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/hadop fs -mkdir /user/admin/aron/newDir


删除⽂件

删除hdfs中/user/admin/aron⽬录下⼀个名叫nedDelete的⽂件

- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/hadop fs -rm /user/admin/aron/nedDelete 删除hdfs中/user/admin/aron⽬录以及该⽬录下的所有⽂件


2. 执⾏sh bin/hadop fs -rmr /user/admin/aron

上传⽂件

上传⼀个本机/home/admin/newFile的⽂件到hdfs中/user/admin/aron⽬录下

- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/hadop fs –put /home/admin/newFile /user/admin/aron/


下载⽂件

下载hdfs中/user/admin/aron⽬录下的newFile⽂件到本机/home/admin/newFile中

- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/hadop fs –get /user/admin/aron/newFile /home/admin/newFile


查看⽂件

我们可以直接在hdfs中直接查看⽂件，功能与类是cat类似 查看hdfs中/user/admin/aron⽬录下的newFile⽂件

- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/hadop fs –cat /home/admin/newFile


# MAPREDUCEJOB操作

提交MAPREDUCE JOB

原则上说，Hadop所有的MapReduce Job都是⼀个jar包。 运⾏⼀个/home/admin/hadop/job.jar的MapReduce Job

- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/hadop jar /home/admin/hadop/job.jar [jobMainClas] [jobArgs]


杀死某个正在运⾏的JOB

假设Job_Id为：job_20105310937_053

- 1. 进⼊HADOP_HOME⽬录。
- 2. 执⾏sh bin/hadop job -kil job_20105310937_053


# 更多HADOP的命令

上⾯介绍的这些Hadop的操作命令是我们最常⽤的。如果你希望了解更多，可以按照如下的⽅式获取 命令的说明信息。

2. 执⾏sh bin/hadop 我们可以看到更多命令的说明信息：

Usage: hadop [-config confdir] COMAND where COMAND is one of:

namenode -format format the DFS filesystem secondarynamenode run the DFS secondary namenode namenode run the DFS namenode datanode run a DFS datanode dfsadmin run a DFS admin client fsck run a DFS filesystem checking utility fs run a generic filesystem user client balancer run a cluster balancing utility jobtracker run the MapReduce job Tracker node pipes run a Pipes job tasktracker run a MapReduce task Tracker node job manipulate MapReduce jobs queue get information regarding JobQueues version print the version jar <jar> run a jar file distcp <srcurl> <desturl> copy file or directories recursively archive -archiveName NAME <src>* <dest> create a hadop archive daemonlog get/set the log level for each daemon

or

CLASNAME run the clas named CLASNAME Most co mands print help when invoked w/o parameters.

