⼀、 Hadoop伪分布配置

<table>
  <tr>
    <th> </th>
  </tr>
</table>


- 1. 在conf/hadop-env.sh⽂件中增加：export JAVA_HOME=/home/Java/jdk1.6
- 2. 在conf/core-site.xml⽂件中增加如下内容： <!- fs.default.name-这是⼀个描述集群中NameNode结点的URI(包括协议、主机名称、端⼝号)，集群⾥⾯的 每⼀台机器都需要知道NameNode的地址。DataNode结点会先在NameNode上注册，这样它们的数据才可以被使 ⽤。独⽴的客户端程序通过这个URI跟DataNode交互，以取得⽂件的块列表。 -> <property> <name>fs.default.name</name> <value>hdfs:/localhost:9 0</value> </property>

<!—hadop.tmp.dir 是hadop⽂件系统依赖的基础配置，很多路径都依赖它。如果hdfs-site.xml中不 配 置namenode和datanode的存放位置，默认就放在这个路径中 -> <property> <name>hadop.tmp.dir</name> <value>/home/hdfs/tmp</value> </property>

- 3. 在conf/hdfs-site.xml中增加如下内容： <!-dfs.replication-它决定着 系统⾥⾯的⽂件块的数据备份个数。对于⼀个实际的应⽤，它 应该被 设为3（这个 数字并没有上限，但更多的备份可能并没有作⽤，⽽且会占⽤更多的空间）。少于三个的 备份，可能会影响到数据的 可靠性(系统故障时，也许会造成数据丢失)-> <property> <name>dfs.replication</name> <value>1</value> </property> <!- dfs.data.dir- 这是DataNode结点被指定要存储数据的本地⽂件系统路径。DataNode结点上 的这个路径没 有必要完全相同，因为每台机器的环境很可能是不⼀样的。但如果每台机器上的这 个路径都是统⼀配置的话，会使⼯ 作变得简单⼀些。默认的情况下，它的值hadop.tmp.dir, 这 个路径只能⽤于测试的⽬的，因为，它很可能会丢失掉⼀ 些数据。所以，这个值最好还是被覆 盖。 dfs.name.dir- 这是NameNode结点存储hadop⽂件系统信息的本地系统路径。这个值只对NameNode有效， DataNode并不需要使⽤到它。上⾯对于/temp类型的警告，同样也适⽤于这⾥。在实际应⽤中，它最好被覆盖掉。 > <property> <name>dfs.name.dir</name> <value>/home/hdfs/name</value> </property>


<property> <name>dfs.data.dir</name> <value>/home/hdfs/data</value> </property> <!—解决：org.apache.hadop.security.AcesControlException:Permision denied:user=Administrator,aces=WRITE,inode="tmp":rot:supergroup:rwxr-xr-x。 因为Eclipse使⽤hadop插件提交作业时，会默认以 DrWho身份去将作业写⼊hdfs⽂件系统中，对应 的也就是 HDFS 上的/user/hadop , 由于 DrWho⽤户对hadop⽬录并没有写⼊权限，所以导致异常的 发⽣。解决⽅法为：放开 hadop ⽬录的权限， 命令如下 ：$ hadop fs -chmod 7 /user/hadop> <property> <name>dfs.permisions</name> <value>false</value> <description> If "true", enable permision checking in HDFS. If "false", permision checking is turned of, but al other behavior is unchanged. Switching from one parameter value to the other does not change the mode, owner or group of files or directories </description> </property>

- 4. 在conf/mapred-site.xml中增加如下内容： <!-mapred.job.tracker -JobTracker的主机（或者IP）和端⼝。 -> <property> <name>mapred.job.tracker</name> <value>localhost:901</value> </property>


