Home

mapreduce

海量数据存储与处理

互联⽹应⽤

编程点滴 求职技术 成⻓历程 ⽹站碎⽚

# HDFS HA-Quorum Journal Manager

七⽉ 8th, 2013 by klose | Posted under , .

互联⽹应⽤ 海量数据存储与处理

- 1、背景


HDFS HA，即NameNode单点故障问题，⼀直是关系到HDFS稳定性最为重要的特性。之前 Hadop0.23初探系列⽂章中，介绍了HDFS的Federeation概况、配置与部署的情况，以及有关HA的 相关概念。

- Hadop0.23.0初探1—前因后果
- Hadop0.23.0初探2—HDFS Federation部署
- Hadop0.23.0初探3—HDFS N,SN,BN和HA


HDFS HA的发展经历了如下⼏个阶段：

- 1）⼿动恢复阶段。⼿动备份fsimage、fsedits数据， N故障之后，重启hdfs。这是最早期使⽤的办 法，由于早期数据量、机器规模、以及对应⽤的影响还⽐较⼩，该⽅案勉强坚持了⼀段时间。
- 2）借助DRBD、HeartbeatHA实现主备切换。 使⽤DRBD实现两台物理机器之间块设备的同步，即通过⽹络实现Raid1，辅以Heartbeat HA实现两台 机器动态⻆⾊切换，对外(DataNode、DFSClient)使⽤虚IP来统⼀配置。这种策略，可以很好地规避因 为物理机器损坏造成的hdfs元数据丢失，(这⾥的元数据简单地说，就是⽬录树，以及每个⽂件有哪些 block组成以及它们之间的顺序)，但block与机器位置的对应关系仅会存储在NameNode的内存中，需 要DataNode定期向NameNode做block report来构建。因此，在数据量较⼤的情况下，blockMap的重 建过程也需要等待⼀段时间，对服务会有⼀定的影响。


![image 1](<HDFS HA-Quorum Journal Manager.note_images/imageFile1.png>)

- 3）DataNode同时向主备 N汇报block信息。这种⽅案以Facebok AvatarNode为代表。 PrimaryN与StandbyN之间通过NFS来共享FsEdits、FsImage⽂件，这样主备 N之间就拥有了⼀致 的⽬录树和block信息；⽽block的位置信息，可以根据DN向两个 N上报的信息过程中构建起来。这样 再辅以虚IP，可以较好达到主备 N快速热切的⽬的。但是显然，这⾥的NFS⼜引⼊了新的SPOF。


![image 2](<HDFS HA-Quorum Journal Manager.note_images/imageFile2.png>)

在主备 N共享元数据的过程中，也有⽅案通过主 N将FsEdits的内容通过与备 N建⽴的⽹络IO流，实 时写⼊备 N，并且保证整个过程的原⼦性。这种⽅案，解决了NFS共享元数据引⼊的SPOF，但是主备

N之间的⽹络连接⼜会成为新的问题。

总结：在开源技术的推动下，针对HDFS NameNode的单点问题，技术发展经历以上三个阶段，虽 然，在⼀定程度上缓解了hdfs的安全性和稳定性的问题，但仍然存在⼀定的问题。直到hadop2.0.*之 后，Quorum Journal Manager给出了⼀种更好的解决思路和⽅案。

- 2、Quorum Journal Manager原理 在⼀个典型的HA集群，两个独⽴的物理节点配置为NameNodes。在任何时间点，其中之⼀ NameNodes是处于Active状态，另⼀种是在Standby状态。 Active NameNode负责所有的客户端的操 作，⽽Standby NameNode尽⽤来保存好⾜够多的状态，以提供快速的故障恢复能⼒。 为了保证Active N与Standby N节点状态同步，即元数据保持⼀致。除了DataNode需要向两个 N 发送block位置信息外，还构建了⼀组独⽴的守护进程”JournalNodes”,⽤来FsEdits信息。当Active N 执⾏任何有关命名空间的修改，它需要持久化到⼀半以上的JournalNodes上。⽽Standby N负责观察 JNs的变化，读取从Active N发送过来的FsEdits信息，并更新其内部的命名空间。⼀旦ActiveN遇到 错误，Standby N需要保证从JNs中读出了全部的FsEdits,然后切换成Active状态。

预防脑裂现象”Brain Split”。HA需要保证在任何⼀个时间点，最多只有⼀个NameNode处于Active状 态。否则的话，在两个 N的NameSpace下的状态会出现分歧，从⽽引起数据丢失、或者其它不可预 ⻅的错误。为了预防该问题的发送，在任何时间点内JNs仅允许⼀个 N向其写FsEdits信息，保证故障 迁移的正常执⾏。

- 3、HDFS HA — JQM的配置 硬件资源：8台物理主机，hostname为GS-CIX-SEV 01~ 08 软件版本：hadop-2.0.5-alpha


![image 3](<HDFS HA-Quorum Journal Manager.note_images/imageFile3.png>)

系统配置⽬标：

- 1) 配置hbasecluster,comoncluster两个NameSpace，保证hbase集群和comon集群命名空间 的分离。
- 2) 对于每⼀个NameSpace下使⽤JQM配置HA。
- 3) 使⽤3个节点 系统环境清单： 系统设置两个NameSpace：hbasecluster, comoncluster


<table>
  <tr>
    <th> </th>
    <th>hbasecluster</th>
    <th>comoncluster</th>
  </tr>
  <tr>
    <td>NameNode</td>
    <td>S- -S 01,</td>
    <td>S- -S 03,</td>
  </tr>
  <tr>
    <td>DataNode</td>
    <td colspan="2">GS-CIX-SEV 02 GS-CIX-SEV 04 GS-CIX-SEV 01~ 08</td>
  </tr>
  <tr>
    <td>JournalNode</td>
    <td colspan="2">GS-CIX-SEV 01,GS-CIX-SEV 02,GS-CIX-SEV 03</td>
  </tr>
  <tr>
    <td>DFSZKFailoverControler</td>
    <td>S- -S 01,</td>
    <td>S- -S 03,</td>
  </tr>
</table>


GS-CIX-SEV 02 GS-CIX-SEV 04

主要的配置⽂件有：hdfs-site.xml、core-site.xml hdfs-site.xml

?

View Code XML

configuration> property>

<name>dfs.replication</name> <value>3</value> </property>

<property> <name>dfs.nameservices</name> <value>hbasecluster,comoncluster</value>

</property> <property>

<name>dfs.ha.namenodes.hbasecluster</name> <value>hn1,hn2</value>

</property> <property>

<name>dfs.ha.namenodes.comoncluster</name> <value>cn1,cn2</value> /property> !-config rpc->

<property> <name>dfs.namenode.rpc-adres.hbasecluster.hn1</name> <value>GS-CIX-SEV 01 910</value> /property>

<property> <name>dfs.namenode.rpc-adres.hbasecluster.hn2</name>

- <value>GS-CIX-SEV 02 910</value>

</property> <property>

<name>dfs.namenode.rpc-adres.comoncluster.cn1</name>

- <value>GS-CIX-SEV 03 910</value> /property>


<property> <name>dfs.namenode.rpc-adres.comoncluster.cn2</name> <value>GS-CIX-SEV 04 910</value>

</property>

!-config htp-adres->

<property> <name>dfs.namenode.htp-adres.hbasecluster.hn1</name> <value>GS-CIX-SEV 01 5071</value> /property>

<property> <name>dfs.namenode.htp-adres.hbasecluster.hn2</name> <value>GS-CIX-SEV 02 5071</value> /property>

<property> <name>dfs.namenode.htp-adres.comoncluster.cn1</name> <value>GS-CIX-SEV 03 5071</value> /property>

<property> <name>dfs.namenode.htp-adres.comoncluster.cn2</name>

<value>GS-CIX-SEV 04 5071</value> </property> <!- qjournal config->

<!-dfs.namenode.shared.edits.dir dfs.namenode.shared.edits.dir->

<property> <name>dfs.journalnode.edits.dir</name> <value>/var/lib/sd/disk1/hadop/hdfs/journal/</value>

</property> <property>

- <name>dfs.namenode.shared.edits.dir.hbasecluster.hn1</name> <value>qjournal:/GS-CIX-SEV 01 8485;GS-CIX-SEV 02 8485;GS-CIX-

SEV 03 8485/hbasecluster</value> </property>

<property>

- <name>dfs.namenode.shared.edits.dir.hbasecluster.hn2</name> <value>qjournal:/GS-CIX-SEV 01 8485;GS-CIX-SEV 02 8485;GS-CIX-


SEV 03 8485/hbasecluster</value> </property>

<property> <name>dfs.namenode.shared.edits.dir.comoncluster.cn1</name> <value>qjournal:/GS-CIX-SEV 01 8485;GS-CIX-SEV 02 8485;GS-CIX-

SEV 03 8485/comoncluster</value> /property>

<property> <name>dfs.namenode.shared.edits.dir.comoncluster.cn</name> <value>qjournal:/GS-CIX-SEV 01 8485;GS-CIX-SEV 02 8485;GS-CIX-

SEV 03 8485/comoncluster</value> /property>

<property> <name>dfs.client.failover.proxy.provider.hbasecluster</name> <value>org.apache.hadop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value> /property>

<property> <name>dfs.client.failover.proxy.provider.comoncluster</name> <value>org.apache.hadop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>

</property> <property>

<name>dfs.ha.fencing.methods</name> <value>shfence</value>

</property> <property>

<name>dfs.ha.fencing.sh.private-key-files</name> <value>/home/hbase/.sh/id_rsa</value> /property> property>

<name>dfs.namenode.name.dir</name> <value>file:/var/lib/sd/disk1/hadop/hdfs/name</value> <final>true</final>

</property>

property> <name>dfs.datanode.data.dir</name> <value>file:/var/lib/sd/disk1/hadop/hdfs/data</value> <final>true</final> </property>

<property> <name>dfs.ha.automatic-failover.enabled</name> <value>true</value>

property>

</configuration> core-site.xml:

core-site.xml: View Code XML

?

<table>
  <tr>
    <th>configuration> property><br><br><name>hadop.tmp.dir</name> <value>/var/lib/sd/disk1/hadop/tmp</value> <description>A base for other temporarydirectories.</description><br><br>/property> prpr<br><br><property> <name>hadop.proxyuser.hbase.hosts</name> <value>GS-CIX-SEV 01.goso.com</value> <description>设置代理的主机</description><br><br>/property><br><br><property> <name>hadop.proxyuser.hbase.groups</name> <value>*</value><br><br>/property><br><br><property> <name>fs.defaultFS</name> <value>hdfs:/hbasecluster</value><br><br><description>设置默认前缀的形式，如果不设置的话，需要按照hdfs:/${service_name}访问<br><br>description> /property><br><br><property> <name>ha.zokeper.quorum</name> <value>10.10.1.1 2181,10.10.1.2 2181,10.10.1.3 2181</value> <description>设置ha所依赖的zk-server的路径</description> /property><br><br><property> <name>ha.zokeper.parent-znode</name> <value>/hbase/hadop-ha</value> <description>设置ha的zk路径</description><br><br>property></th>
  </tr>
</table>


</configuration>

- 4、HDFS HA 启动过程


- 1） 设置HADOP环境。 在${HADOP_HOME_DIR}/etc/hadop/hadop-env.sh中设置：


export HADOP_HOME=/opt/hadop/hadop/ #设置hadop根⽬录 export JAVA_HOME=/usr/local/jdk1.6.0_38/ #设置jdk的环境 export HADOP_CONF_DIR=${HADOP_HOME}/etc/hadop #设置hadop conf⽬录 (ps:需要节点 sh⽆密码登录，具体⽅式可以参考⽹上内容) 按照本⽂第三部分的内容，根据⾃⼰环境配置hdfs-site.xml以及core-site.xml。

- 2） 启动JournalNodes。为此，可以准备如下的脚本。注意该部分不属于官⽅配置⽅式。 ${HADOP_HOME}/etc/hadop/journalnodes 配置journalnodes


- GS-CIX-SEV 01
- GS-CIX-SEV 02
- GS-CIX-SEV 03 启动脚本：${HADOP_HOME}/start-journalnodes.sh #!/bin/bash bin=`dirname “${BASH_SOURCE-$0}”` bin=`cd “$bin”; pwd` DEFAULT_LIBEXEC_DIR=”$bin”/./libexec HADOP_LIBEXEC_DIR=${HADOP_LIBEXEC_DIR:-$DEFAULT_LIBEXEC_DIR}


. $HADOP_LIBEXEC_DIR/hdfs-config.sh JOURNAL_NODES=$(cat ${HADOP_CONF_DIR}/journalnodes)

echo “Starting journal nodes [$JOURNAL_NODES]“ “$HADOP_PREFIX/sbin/hadop-daemons.sh” \

- –config “$HADOP_CONF_DIR” \
- –hostnames “$JOURNAL_NODES” \
- –script “$bin/hdfs” start journalnode


使⽤ sbin/start-journalnodes.sh启动JournaNodes。

3） 启动NameNode。 配置HA，需要保证ActiveN与StandByN有相同的NameSpace ID，在format⼀台机器之后，让另外 ⼀台 N同步⽬录下的数据。 配置Federation，需要在启动多个NameNode上format时，指定clusterid，从⽽保证2个NameService 可以共享所有的DataNodes，否则两个NameService在fornat之后，⽣成的clusterid不⼀致， DataNode会随机注册到不同的NameNode上。如下所示：

![image 4](<HDFS HA-Quorum Journal Manager.note_images/imageFile4.png>)

![image 5](<HDFS HA-Quorum Journal Manager.note_images/imageFile5.png>)

两个NameService下各出现了4个DataNodes，并没有达到DataNode共⽤的效果。因此在启动 N的过 程中，需要按照如下的⽅式进⾏： l 在hbasecluster的⼀台 N上执⾏：bin/hdfs namenode –format –clusterid cluster,然后启动 N， sbin/hadop-daemon.sh start namenode l 在hbasecluster的另外⼀台 N上执⾏：bin/hdfs namenode –botstrapStandby 同步 N的⽂件，然 后执⾏sbin/hadop-daemon.sh start namenode 此时，hbasecluster上的2个 N都处于Standby状态，需要启动DFSZKFailoverControler，该部分可以 利⽤修改的sbin/start-dfs.sh启动。 对于comoncluster的两个NameNode，使⽤如上同样的⽅式进⾏启动。

- 4） 启动zkfc，具体的⽅式，可以参考sbin/start-dfs.sh提供的脚本进⾏。
- 5） 启动datanode，具体参⻅sbin/start-dfs.sh提供的启动⽅式。 启动之后，可以看到如下的⽬录结构。


![image 6](<HDFS HA-Quorum Journal Manager.note_images/imageFile6.png>)

针对每⼀个NameService，在journal下有对应⽬录存储edits_*- *，⽽在name下是NameNode保存 的本地fsimage和fsedits信息。 可以通过bin/hadop fs –mkdir hdfs:/hbasecluster/hbase bin/hadop fs –ls hdfs:/hbasecluster/ 测试环境是否正常。

参考⽂献： htp:/hadop.apache.org/docs/curent/hadop-yarn/hadop-yarnsite/HDFSHighAvailabilityWithQJM.html 本系列⽂章属于 在 个⼈技术博客原创，原⽂链接为

Binos_ICT Binospace htp:/ w.binospace.com/in dex.php/hdfs-ha-quorum-journal-manager/

,未经允许，不得转载。

From , post ⽂章的脚注信息由WordPres的 ⾃动⽣成

Binospace HDFS HA-Quorum Journal Manager wp-posturl插件

0

Tags: , , , , ,

federationHAhdfsJournal NameSpaceQuorum

