htp:/hi.baidu.com/itdreams209/blog/item/62a5ef18fbe854e42a9ad13.html

转⾃： 1、问题描述：三台机⼦搭建的hadop集群，⼀台是namenode，另外两台是datanode。今天执⾏ hadop fs -copyFromLocal 的时候报错。File /home/hexianghui/tmp/mapred/system/jobtracker.info could only be replicated to 0 nodes, instead of 1 由这个可以认为是没有找到datanode。我⽤jps查看进程都正常。但是⽤web查看的话， live nodes为0. 这说明datanode没有正常启动，但是datanode进程⼜启动了，这是为何?⽹友可以跟 帖提出意⻅或者指导 …

209-12-30 2 02 19,190 INFO org.apache.hadop.mapred.JobTracker: STARTUP_MSG: / * ST T S Starting JobTracker ST T S host hexianghui/192.168.0.4 ST T S arg= [] ST T S version = 0.20.1 STARTUP_MSG: build =

htp:/svn.apache.org/repos/asf/hadop/comon/tags/release-0.20.1-rc

- 1


-r 81020; compiled by 'om' on Tue Sep 1 20  5 56 UTC 209

*/ 209-12-30 2 02 19,280 INFO org.apache.hadop.mapred.JobTracker: Scheduler configured with (memSizeForMapSlotOnJT, memSizeForReduceSlotOnJT, limitMaxMemForMapTasks, limitMaxMemForReduceTasks) (-1, -1, -1, -1) 209-12-30 2 02 19,32 INFO org.apache.hadop.ipc.metrics.RpcMetrics: Initializing RPC Metrics with hostName=JobTracker, port=901 209-12-30 2 02 24,54 INFO org.mortbay.log: Loging to org.slf4j.impl.Log4jLogerAdapter(org.mortbay.log) via org.mortbay.log.Slf4jLog

- 209-12-30 2 02 24,691 INFO org.apache.hadop.htp.HtpServer: Port returned by webServer.getConectors()[0].getLocalPort() before open() is -1. Opening the listener on 5030
- 209-12-30 2 02 24,692 INFO org.apache.hadop.htp.HtpServer: listener.getLocalPort() returned 5030 webServer.getConectors()[0].getLocalPort() returned 5030 209-2-30 2 02 2 92 or.apache.hadop.htp.HtpServer: Jety bound to port 5030 209-12-0 2 02 24,692 INFO org.mortbay.log: jety-6.1.14 209-12-30 2 04 01,351 INFO org.mortbay.log: Started


SelectChanelConector@0.0.0.0 503

- 0


209-12-30 2 04 01,353 INFO org.apache.hadop.metrics.jvm.JvmMetrics: Initializing JVM Metrics with procesName=JobTracker, sesionId= 209-12-30 2 0 0133 or.apache.hadop. apred.JobTracker: JobTracker up at: 901 209-12-30 2 04 01,353 INFO org.apache.hadop.mapred.JobTracker: JobTracker webserver: 5030 209-12-30 2 04 01,468 INFO org.apache.hadop.mapred.JobTracker: Cleaning up the system directory 209-12-30 2 04 01,51 INFO org.apache.hadop.mapred.CompletedJobStatusStore: Completed job store is inactive 209-12-30 2 04 01, 5 WARN org.apache.hadop.hdfs.DFSClient: DataStreamer Exception: org.apache.hadop.ipc.RemoteException: java.io.IOException: File /home/hexianghui/tmp/mapred/system/jobtracker.info could only be replicated to 0 nodes, instead of 1 at org.apache.hadop.hdfs.server.namenode.FSNamesystem.getAditionalBlock(FSNamesystem.ja va:1267) aorg.apache.hadop.hdfs.server.namenode.NameNode.adBlock(NameNode.java:42) at sn.relect.NativeMethod cesor l.invoe0(Native Method) atsn.relect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:39) atsun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:25) atjava.lang.reflect.Method.invoke(Method.java:597) aor.apache.ha op.pc.RPC$Server.cal(RPC.java:508) aor.apache.hadop.pc.Server$ander$.rnServer.ava99 at org.apache.hadop.ipc.Server$Handler$1.run(Server.java:95)

at ava.security.AcesControler.doPrivileged(Native Method) atjavax.security.auth.Subject.doAs(Subject.java:396) aor.apache.hadop.pc.Server$Handler.run(Server.java:953) ato .apache.ha op.ipc.Client.cal(Client.java:739) aorg.apache.hadop.ipc.RPC$Invoker.invoke(RPC.java: 20) a $Proxy4.adBlock(Unknown Source) at sn.relect.NativeMethod cesor l.invoe0(Native Method) atsn.relect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:39) atsun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:25) at java.lang.reflect.Method.invoke(Method.java:597) at org.apache.hadop.io.retry.RetryInvocationHandler.invokeMethod(RetryInvocationHandler.java:8

- 2) atorg.apache.hadop.io.retry.RetryInvocationHandler.invoke(RetryInvocationHandler.java:59) a $Proxy4.adBlock(Unknown Source) at org.apache.hadop.hdfs.DFSClient$DFSOutputStream.locateFolowingBlock(DFSClient.java:290 4) at org.apache.hadop.hdfs.DFSClient$DFSOutputStream.nextBlockOutputStream(DFSClient.java:2 786) at org.apache.hadop.hdfs.DFSClient$DFSOutputStream.aces$2 0(DFSClient.java:2076) at org.apache.hadop.hdfs.DFSClient$DFSOutputStream$DataStreamer.run(DFSClient.java: 262) 209-12-30 2 04 01, 5 WARN org.apache.hadop.hdfs.DFSClient: Eror Recovery for block nul bad datanode[0] nodes = nul 209-12-30 2 04 01, 5 WARN org.apache.hadop.hdfs.DFSClient: Could not get block locations. Source file "/home/hexianghui/tmp/mapred/system/jobtracker.info" - Aborting. 209-12-30 2 04 01,56 WARN org.apache.hadop.mapred.JobTracker: Writing to file hdfs:/hexianghui:9 0/home/hexianghui/tmp/mapred/system/jobtracker.info failed! 209-12-30 2 04 01,56 WARN org.apache.hadop.mapred.JobTracker: FileSystem is not ready yet! 209-12-30 2 04 01,568 WARN org.apache.hadop.mapred.JobTracker: Failed to initialize recovery manager. org.apache.hadop.ipc.RemoteException: java.io.IOException: File /home/hexianghui/tmp/mapred/system/jobtracker.info could only be replicated to 0 nodes, instead of 1at org.apache.hadop.hdfs.server.namenode.FSNamesystem.getAditionalBlock(FSNamesystem.ja va:1267) aorg.apache.hadop.hdfs.server.namenode.NameNode.adBlock(NameNode.java:42) at sn.relect.NativeMethod cesor l.invoe0(Native Method) atsn.relect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:39) atsun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:25) atjava.lang.reflect.Method.invoke(Method.java:597) aor.apache.ha op.pc.RPC$Server.cal(RPC.java:508) aor.apache.hadop.pc.Server$ander$.rnServer.ava99 aorg.apache.hadop.ipc.Server$Handler$1.run(Server.java:95) at ava.security.AcesControler.doPrivileged(Native Method) atjavax.security.auth.Subject.doAs(Subject.java:396) aor.apache.hadop.pc.Server$Handler.run(Server.java:953) ato .apache.ha op.ipc.Client.cal(Client.java:739) aorg.apache.hadop.ipc.RPC$Invoker.invoke(RPC.java: 20) a $Proxy4.adBlock(Unknown Source) at sn.relect.NativeMethod cesor l.invoe0(Native Method) atsn.relect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:39) atsun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:25) at java.lang.reflect.Method.invoke(Method.java:597)


at org.apache.hadop.io.retry.RetryInvocationHandler.invokeMethod(RetryInvocationHandler.java:8 2) atorg.apache.hadop.io.retry.RetryInvocationHandler.invoke(RetryInvocationHandler.java:59) a $Proxy4.adBlock(Unknown Source) at org.apache.hadop.hdfs.DFSClient$DFSOutputStream.locateFolowingBlock(DFSClient.java:290 4) at org.apache.hadop.hdfs.DFSClient$DFSOutputStream.nextBlockOutputStream(DFSClient.java:2 786) at org.apache.hadop.hdfs.DFSClient$DFSOutputStream.aces$2 0(DFSClient.java:2076) at org.apache.hadop.hdfs.DFSClient$DFSOutputStream$DataStreamer.run(DFSClient.java: 262) 209-12-30 2 04 1,569 WARN org.apache.hadop.mapred.JobTracker: Retrying. 以下是我在⽹上找的解决⽅法集锦：

- ⽅案1： 是否是防⽕墙未关闭，查看。确实忘记关闭防⽕墙了，因为我换了⼏台机器，以前是在虚拟机下⽤的 是redhat，现在⽤的是ubuntu8.0.4。所以跟这个很可能相关。关闭iptables后，出现的错误信息没那 么多了，但是还有错误。如下： 2010-01-03 2 08 25,073 INFO org.apache.hadop.ipc.Server: IPC Server handler 8 on 9 0,

- cal adBlock(/user/hexianghui/input/file01, DFSClient_10586075) from 192.168.0.4 53604:

- eror: java.io.IOException: File /user/hexianghui/input/file01 could only be replicated to 0 nodes, instead of 1

- java.io.IOException: File /user/hexianghui/input/file01 could only be replicated to 0 nodes, instead of 1 at org.apache.hadop.hdfs.server.namenode.FSNamesystem.getAditionalBlock(FSNamesystem.ja va:1267) aorg.apache.hadop.hdfs.server.namenode.NameNode.adBlock(NameNode.java:42) at sn.relect.NativeMethod cesor l.invoe0(Native Method) atsn.relect.NativeMethodAcesorImpl.invoke(NativeMethodAcesorImpl.java:39) atsun.reflect.DelegatingMethodAcesorImpl.invoke(DelegatingMethodAcesorImpl.java:25) atjava.lang.reflect.Method.invoke(Method.java:597) aor.apache.ha op.pc.RPC$Server.cal(RPC.java:508) aor.apache.hadop.pc.Server$ander$.rnServer.ava99 aorg.apache.hadop.ipc.Server$Handler$1.run(Server.java:95) at ava.security.AcesControler.doPrivileged(Native Method) atjavax.security.auth.Subject.doAs(Subject.java:396) at org.apache.hadop.ipc.Server$Handler.run(Server.java:953) 2010-01-03 2 08 25,087 INFO org.apache.hadop.hdfs.server.namenode.FSNamesystem.audit: ugi=hexianghui,hexianghui,adm,dialout,cdrom,flopy,audio,dip,video,plugdev,fuse,lpadmin,admin ip=/192.168.0.4 cmd=create src=/user/hexianghui/input/file02 dst=nul perm=hexianghui:supergroup:rw-r-r2010-01-03 2 08 25,08 INFO org.apache.hadop.ipc.Server: IPC Server handler 2 on 9 0,

cal adBlock(/user/hexianghui/input/file02, DFSClient_10586075) from 192.168.0.4 53604: eror: java.io.IOException: File /user/hexianghui/input/file02 could only be replicated to 0 nodes, instead of 1

- java.io.IOException: File /user/hexianghui/input/file02 could only be replicated to 0 nodes, instead of 1 虽然有类似的信息：could only be replicated to 0 nodes, instead of 1。但情况不⼀样了，可以对照 上⾯的错误信息。继续接着找 …






- ⽅案2：Hadop DFSClient警告NotReplicatedYetException信息


有时，当你申请到⼀个HOD集群后⻢上尝试上传⽂件到HDFS时，DFSClient会警告 NotReplicatedYetException。通常会有⼀个这样的信息 -

<table>
  <tr>
    <th>WARN hdfs.DFSClient: NotReplicatedYetException sl eping <filename> retries left 3</th>
  </tr>
  <tr>
    <td>08/01/25 16 31 40 INFO hdfs.DFSClient: org.apache.hadop.ipc.RemoteException:java.io.IOException: File <filename> could only be replicated<br><br></td>
  </tr>
</table>


to 0 nodes, instead of 1

当你向⼀个DataNodes正在和NameNode联络的集群上传⽂件的时候，这种现象就会发⽣。在上传 新⽂件到HDFS之前多等待⼀段时间就可以解决这个问题，因为这使得⾜够多的DataNode启动并且 联络上了NameNode。

PS:我的实践：等待了⼏分钟，还是依然报错，此法不通。 ⽅案3： 這個錯誤訊息意思是，他想要放檔案，但沒半個node可以給存取，因此我們需要檢查：

htp:/trac.nchc.org.tw/cloud/wiki/waue/209/0709

- 1.
- 2.
- 3.
- 4.


系統或hdfs是否還有空間 （像我就是） datanode數是否正常 是否在safemode 讀寫的權限

什麼都檢查過都正常的話，也只好砍掉重練了

PS:检查上⾯⼏个，1）系统空间够。df -hl查看。 2）datanode数是2.datanode⽤jps查看进程，都启 动了。3，是否在safemode下。hadop dfsadmin -safemode leave.使⽤后，可以正常拷⻉了。也许 之前的操作和这⼀步操作起作⽤了。

htp:/blog.csdn.net/wh6259285/archive/2010/07/18/574158.aspx

转⾃： 10/07/18 12 31 1 WARN hdfs.DFSClient: Eror Recovery for block nul bad datanode[0] nodes = nul 10/07/18 12 31 1 WARN hdfs.DFSClient: Could not get block locations. Source file "/user/rot/input/log4j.properties" - Aborting. put: java.io.IOException: File /user/rot/input/log4j.properties could only be replicated to 0 nodes, instead of 1 好⻓到⼀段错误代码，呵呵。刚碰到这个问题到时候上⽹搜了以下，也没有⼀个很标准的解决⽅法。 ⼤致上说是由于不⼀致状态导致的。 办法倒是有⼀个，只不过会丢失掉已有数据，请慎重使⽤。

- 1、先把服务都停掉
- 2、格式化namenode
- 3、重新启动所有服务
- 4、可以进⾏正常操作了


下⾯是我到解决步骤

rot@scutshuxue-desktop:/home/rot/hadop-0.19.2

# bin/stop-al.sh

stoping jobtracker localhost: stoping tasktracker no namenode to stop localhost: no datanode to stop localhost: stoping secondarynamenode

rot@scutshuxue-desktop:/home/rot/hadop-0.19.2

# bin/hadop namenode -format

- 10/07/18 12 46 23 INFO namenode.NameNode: STARTUP_MSG: / * STARTUP_MSG: Starting NameNode STARTUP_MSG: host = scutshuxue-desktop/127.0.1.1 STARTUP_MSG: args = [-format] STARTUP_MSG: version = 0.19.2 STARTUP_MSG: build =

-r 789657; compiled by 'rot' on Tue Jun 30 12 40 50 EDT 209

*/ Re-format filesystem in /tmp/hadop-rot/dfs/name ? (Y or N) Y

- 10/07/18 12 46 24 INFO namenode.FSNamesystem: fsOwner=rot,rot

- 10/07/18 12 46 24 INFO namenode.FSNamesystem: supergroup=supergroup

- 10/07/18 12 46 24 INFO namenode.FSNamesystem: isPermisionEnabled=true
- 10/07/18 12 46 25 INFO comon.Storage: Image file of size 94 saved in 0 seconds.


- 10/07/18 12 46 25 INFO comon.Storage: Storage directory /tmp/hadop-rot/dfs/name has ben sucesfuly formated.


- 10/07/18 12 46 25 INFO namenode.NameNode: SHUTDOWN_MSG: / * SHUTDOWN_MSG: Shuting down NameNode at scutshuxue-desktop/127.0.1.1


htps:/svn.apache.org/repos/asf/hadop/comon/branches/branch0.19

*/ # ls

rot@scutshuxue-desktop:/home/rot/hadop-0.19.2

bin docs lib README.txt build.xml hadop-0.19.2-ant.jar libhdfs src c+ hadop-0.19.2-core.jar librecordio test-txt CHANGES.txt hadop-0.19.2-examples.jar LICENSE.txt webaps conf hadop-0.19.2-test.jar logs contrib hadop-0.19.2-tols.jar NOTICE.txt

rot@scutshuxue-desktop:/home/rot/hadop-0.19.2

# bin/start-al.sh

starting namenode, loging to /home/rot/hadop-0.19.2/bin/./logs/hadop-rot-namenodescutshuxue-desktop.out localhost: starting datanode, loging to /home/rot/hadop-0.19.2/bin/./logs/hadop-rotdatanode-scutshuxue-desktop.out localhost: starting secondarynamenode, loging to /home/rot/hadop-0.19.2/bin/./logs/hadoprot-secondarynamenode-scutshuxue-desktop.out starting jobtracker, loging to /home/rot/hadop-0.19.2/bin/./logs/hadop-rot-jobtrackerscutshuxue-desktop.out localhost: starting tasktracker, loging to /home/rot/hadop-0.19.2/bin/./logs/hadop-rottasktracker-scutshuxue-desktop.out

rot@scutshuxue-desktop:/home/rot/hadop-0.19.2 rot@scutshuxue-desktop:/home/rot/hadop-0.19.2

# bin/hadop fs -put conf input # bin/hadop dfs -ls

Found 1 items drwxr-xr-x - rot supergroup 0 2010-07-18 12 47 /user/rot/input

htp:/blog.csdn.net/wh6259285/archive/2010/07/18/574 158.aspx

本⽂来⾃CSDN博客，转载请标明出处：

