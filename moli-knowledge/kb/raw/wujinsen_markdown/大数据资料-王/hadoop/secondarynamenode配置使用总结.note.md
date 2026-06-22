⼀、环境 Hadop 0.20.2、JDK 1.6、Linux操作系统 ⼆、背景 上周五的时候，由于操作系统的原因，导致JDK出现莫名的段错误。⽆论是重启机器还是JDK重装都⽆ 济于事。更可悲的是，出问题的机器就是Master。当时⼼⾥就凉了半截，因为secondarynamenode配 置也是在这个机器上（默认的，没改过）。不过万幸的是这个集群是测试环境，所以问题不⼤。借这 个缘由，我将secondarynamenode重新配置到其他机器上，并做namenode挂掉并恢复的测试。 三、操作

- 1、关于secondarynamenode⽹上有写不错的⽂章做说明，这⾥我只是想说关键⼀点，它不是 namenode的备份进程，说⽩了，namenode挂了，如果secondarynamenode没挂，很不幸，集群⼀ 样⽆法正常⼯作。这⾥有个⽂档翻译的很好，我链接⼀下：
- 2、secondarynamenode⼀般来说不应该和namenode在⼀起，所以，我把它配置到了datanode上。 配置到datanode上，⼀般来说需要改以下配置⽂件。conf/master、conf/hdfs-site.xml和conf/coresite.xml这3个配置⽂件，修改部分如下： master：⼀般的安装⼿册都是说写上namenode机器的IP或是名称。这⾥要说明⼀下，这个master不 决定哪个是namenode，⽽决定的是secondarynamenode（决定谁是namenode的关键配置是coresite.xml中的fs.default.name这个参数）。所以，这⾥直接写上你的datanode的IP或机器名称就可以 了。⼀⾏⼀个。 hdfs-site.xml：这个配置⽂件要改1个参数： 0.0.0.0改为你的namenode的IP地址。 <property>


htp:/blog.csdn.net/AE86_FC/archive/201 0/02/03/5284181.aspx

<name>dfs.htp.adres</name> <value>0.0.0.0 5070</value> <description>

The adres and the base port where the dfs namenode web ui wil listen on. If the port is 0 then the server wil start on a fre port.

</description> </property> core-site.xml：这⾥有2个参数可配置，但⼀般来说我们不做修改。fs.checkpoint.period表示多⻓时间 记录⼀次hdfs的镜像。默认是1⼩时。fs.checkpoint.size表示⼀次记录多⼤的size，默认64M。 <property>

<name>fs.checkpoint.period</name> <value>360</value> <description>The number of seconds betwen two periodic checkpoints. </description>

</property>

<property> <name>fs.checkpoint.size</name> <value>6710864</value> <description>The size of the curent edit log (in bytes) that tri gers

a periodic checkpoint even if the fs.checkpoint.period hasn't expired. </description>

</property>

- 3、配置检查。配置完成之后，我们需要检查⼀下是否成功。我们可以通过查看运⾏ secondarynamenode的机器上⽂件⽬录来确定是否成功配置。⾸先输⼊jps查看是否存在 secondarynamenode进程。如果存在，在查看对应的⽬录下是否有备份记录。如下图：


该⽬录⼀般存在于hadop.tmp.dir/dfs/namesecondary/下⾯。 四、恢复

- 1、配置完成了，如何恢复。⾸先我们kil掉namenode进程，然后将hadop.tmp.dir⽬录下的数据删除 掉。制造master挂掉情况。
- 2、在配置参数dfs.name.dir指定的位置建⽴⼀个空⽂件夹； 把检查点⽬录的位置赋值给配置参数 fs.checkpoint.dir； 启动NameNode，并加上-importCheckpoint。（这句话抄袭的是hadop0.20.2/hadop-0.20.2/docs/cn/hdfs_user_guide.html#Secondary+NameNode，看看⽂档，有说 明）
- 3、启动namenode的时候采⽤hadop namenode –importCheckpoint 五、总结


- 1、secondarynamenode可以配置多个，master⽂件⾥⾯多写⼏个就可以。
- 2、千万记得如果要恢复数据是需要⼿动拷⻉到namenode机器上的。不是⾃动的（参看上⾯写的恢复 操作）。
- 3、镜像备份的周期时间是可以修改的，如果不想⼀个⼩时备份⼀次，可以改的时间短点。coresite.xml中的fs.checkpoint.period值


