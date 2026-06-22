# hadop HA namenode⽆法启动错误(2014-12-1611:14:34)

转载▼

<table>
  <tr>
    <th>标签：<br><br>宠物</th>
    <th>分类： 计算机与 Internet</th>
  </tr>
</table>


2014-12-15 14 27  5,24 INFO org.apache.hadop.hdfs.server.namenode.FSImage: No edit log streams selected.2014-12-15 14 27  5,30 EROR org.apache.hadop.hdfs.server.namenode.FSImage: Failed to load image from FSImageFile(file=/usr/hadop/tmp/dfs/name/curent/fsimage_ 030732, cpktTxId= 030732)java.io.IOException: Premature EOF from inputStreamat org.apache.hadop.io.IOUtils.readFuly(IOUtils.java:194) at org.apache.hadop.hdfs.server.namenode.FSImageFormat$LoaderDelegator.load(FSImageFormat. java: 21) at org.apache.hadop.hdfs.server.namenode.FSImage.loadFSImage(FSImage.java:913) at org.apache.hadop.hdfs.server.namenode.FSImage.loadFSImage(FSImage.java:89) at org.apache.hadop.hdfs.server.namenode.FSImage.loadFSImageFile(FSImage.java:72) at org.apache.hadop.hdfs.server.namenode.FSImage.loadFSImage(FSImage.java: 60) at org.apache.hadop.hdfs.server.namenode.FSImage.recoverTransitionRead(FSImage.java:279) at org.apache.hadop.hdfs.server.namenode.FSNamesystem.loadFSImage(FSNamesystem.java:95)

at org.apache.hadop.hdfs.server.namenode.FSNamesystem.loadFromDisk(FSNamesystem.java:70)

at org.apache.hadop.hdfs.server.namenode.NameNode.loadNamesystem(NameNode.java:529) at org.apache.hadop.hdfs.server.namenode.NameNode.initialize(NameNode.java:585) at org.apache.hadop.hdfs.server.namenode.NameNode.(NameNode.java:751) at org.apache.hadop.hdfs.server.namenode.NameNode.(NameNode.java:735) at org.apache.hadop.hdfs.server.namenode.NameNode.createNameNode(NameNode.java:1407) a t org.apache.hadop.hdfs.server.namenode.NameNode.main(NameNode.java:1473)2014-12-15 14 27  5,438 WARN org.apache.hadop.hdfs.server.namenode.FSNamesystem: Encountered exception loading fsimagejava.io.IOException: Failed to load an FSImage file! at org.apache.hadop.hdfs.server.namenode.FSImage.loadFSImage(FSImage.java:671) at org.apache.hadop.hdfs.server.namenode.FSImage.recoverTransitionRead(FSImage.java:279) at org.apache.hadop.hdfs.server.namenode.FSNamesystem.loadFSImage(FSNamesystem.java:95)

at org.apache.hadop.hdfs.server.namenode.FSNamesystem.loadFromDisk(FSNamesystem.java:70)

at org.apache.hadop.hdfs.server.namenode.NameNode.loadNamesystem(NameNode.java:529) at org.apache.hadop.hdfs.server.namenode.NameNode.initialize(NameNode.java:585) at org.apache.hadop.hdfs.server.namenode.NameNode.(NameNode.java:751) at org.apache.hadop.hdfs.server.namenode.NameNode.(NameNode.java:735) at org.apache.hadop.hdfs.server.namenode.NameNode.createNameNode(NameNode.java:1407) a t org.apache.hadop.hdfs.server.namenode.NameNode.main(NameNode.java:1473)2014-12-15

14 27  5, 40 INFO org.mortbay.log: Stoped HtpServer2$SelectChanelConectorWithSafeStartup@HM0 50702014-12-15 14 27  5,540 INFO org.apache.hadop.metrics2.impl.MetricsSystemImpl: Stoping NameNode metrics system.2014-12-15 14 27  5,541 INFO org.apache.hadop.metrics2.impl.MetricsSystemImpl: NameNode metrics system stoped.2014-12-15 14 27  5,541 INFO org.apache.hadop.metrics2.impl.MetricsSystemImpl: NameNode metrics system shutdown complete.2014-12-15 14 27  5,541 FATAL org.apache.hadop.hdfs.server.namenode.NameNode: Exception in namenode joinjava.io.IOException: Failed to load an FSImage file! at org.apache.hadop.hdfs.server.namenode.FSImage.loadFSImage(FSImage.java:671) at org.apache.hadop.hdfs.server.namenode.FSImage.recoverTransitionRead(FSImage.java:279) at org.apache.hadop.hdfs.server.namenode.FSNamesystem.loadFSImage(FSNamesystem.java:95)

at org.apache.hadop.hdfs.server.namenode.FSNamesystem.loadFromDisk(FSNamesystem.java:70)

at org.apache.hadop.hdfs.server.namenode.NameNode.loadNamesystem(NameNode.java:529) at org.apache.hadop.hdfs.server.namenode.NameNode.initialize(NameNode.java:585) at org.apache.hadop.hdfs.server.namenode.NameNode.(NameNode.java:751) at org.apache.hadop.hdfs.server.namenode.NameNode.(NameNode.java:735) at org.apache.hadop.hdfs.server.namenode.NameNode.createNameNode(NameNode.java:1407) a t org.apache.hadop.hdfs.server.namenode.NameNode.main(NameNode.java:1473)查看⽂件⼤⼩ fsimage_ 030732 ⼤⼩为0，⽽在另⼀台服务器⻓度不为0，应该是这个fsimage⽂件 的问题， 可以从另⼀台服务器上拷⻉该⽂件 ，或者删除这个⽂件及对应的md5⽂件即可。 建议：从其他机器拷⻉fsimage⽂件

