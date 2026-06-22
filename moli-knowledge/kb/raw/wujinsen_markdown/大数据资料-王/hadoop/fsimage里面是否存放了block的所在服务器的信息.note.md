- 1、在edits中保存着每个⽂件的操作详细信息
- 2、在fsimage中保存着⽂件的名字、id、分块信息、⼤⼩等信息，但是不保存datanode的ip
- 3、在hdfs启动的时候，处于安全模式，datanode向namenode汇报⾃⼰的ip和持有的block信息
- 4、安全模式结束，⽂件块和datanode的ip关联上


验证过程

- 1、启动namenode，离开safemode，cat某个⽂件，看log，没有显示⽂件关联的datanode
- 2、启动datanode，cat⽂件，内容显示
- 3、停⽌datanode，cat⽂件，看log，看不到⽂件，但是现实了⽂件快的关联datanode


