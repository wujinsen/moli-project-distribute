如要监控Storm集群和运⾏在其上的Topology，该如何做呢？ Storm已经为你考虑到了，Storm⽀持Thrift的C/S ，在部署Nimbus组件的机器上启动⼀个Thrift Server进程来提供服务，我们可以通过编写⼀个Thrift Client来请求Thrift Server，来获取你想得到的 集群和Topology的相关数据，来接⼊监控平台，如Zabix等，我⽬前使⽤的就是Zabix。 整体的流程已经清楚了，下⾯就来实践吧。

架构

- 1 安装Thrift 由于我们要使⽤Thrift来编译Storm的源代码来获得Thrift Client相关的 源代码，所以需要先安装 Thrift，这⾥选取的版本为0.9.2。 到官⽹下载好安装包： 编译安装：configure & make & make instal 验证：thrift -version 如果打印出Thrift version 0.9.2，代表安装成功。
- 2 编译Thrift Client代码 ⾸先下载Storm源代码，这⾥使⽤最新的0.9.3版本：

解压后进⾏编译：thrift -gen java apache-storm-0.9.3/storm-core/src/storm.thrift 在当前⽬录下出现gen-java⽂件夹，此⽂件夹下就是Thrift Client的Java源代码了。

- 3 使⽤Thrift Client API 然后创建⼀个Maven项⽬来进⾏执⾏监控数据的获取。 项⽬⽣成⼀个Jar⽂件，输⼊⼀些命令和⾃定义参数，然后输出结果。 以命令⾏的形式进⾏调⽤，这样可以⽅便的接⼊监控系统，当然使⽤形式可以根据⾃身情况施⾏。 创建好后，把gen-java⽣成的代码拷⻉进来。 在pom.xml⾥引⼊Thrift对应版本的库：[html]


Java

htp:/thrift.apache.org/

htp:/mirors.hust.edu.cn/apache/storm/apache

-storm-0.9.3/apache-storm-0.9.3-src.tar.gz

view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.


<dependency> <groupId>org.apache.thrift</groupId> <artifactId>libthrift</artifactId> <version>0.9.2</version>

</dependency>

⾸先写⼀些Thrift相关的辅助类。 ClientInfo.java[java]

view plain copy

- 1.


package com.damacheng09.storm.monitor.thrift;

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
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.


import org.apache.thrift.protocol.TBinaryProtocol; import org.apache.thrift.transport.TFramedTransport; import org.apache.thrift.transport.TSocket;

import backtype.storm.generated.Nimbus;

/*

- * 代表⼀个Thrift Client的信息
- * @author jb-xingchencheng

*

- */ publicclas ClientInfo {


private TSocket tsocket; private TFramedTransport tTransport; private TBinaryProtocol tBinaryProtocol; private Nimbus.Client client;

public TSocket getTsocket() {

return tsocket; }

publicvoid setTsocket(TSocket tsocket) {

this.tsocket = tsocket; }

public TFramedTransport getTransport() {

return tTransport; }

publicvoid setTransport(TFramedTransport tTransport) {

this.tTransport = tTransport; }

public TBinaryProtocol getBinaryProtocol() {

return tBinaryProtocol; }

- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.


publicvoid setBinaryProtocol(TBinaryProtocol tBinaryProtocol) {

this.tBinaryProtocol = tBinaryProtocol; }

public Nimbus.Client getClient() {

return client; }

publicvoid setClient(Nimbus.Client client) {

this.client = client; }

}ClientManager.java

[java]

view plain copy

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
- 18.
- 19.


package com.damacheng09.storm.monitor.thrift;

import org.apache.thrift.protocol.TBinaryProtocol; import org.apache.thrift.transport.TFramedTransport; import org.apache.thrift.transport.TSocket; import org.apache.thrift.transport.TransportException;

import backtype.storm.generated.Nimbus;

/*

- * Thrift Client管理类
- * @author jb-xingchencheng

*

- */ publicclas ClientManager {


publicstatic ClientInfo getClient(String nimbusHost, int nimbusPort) throws TransportExc

eption { ClientInfo client = new ClientInfo(); TSocket tsocket = new TSocket(nimbusHost, nimbusPort); TFramedTransport tTransport = new TFramedTransport(tsocket);

- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.


TBinaryProtocol tBinaryProtocol = new TBinaryProtocol(tTransport); Nimbus.Client c = new Nimbus.Client(tBinaryProtocol); tTransport.open(); client.setTsocket(tsocket); client.setTransport(tTransport); client.setBinaryProtocol(tBinaryProtocol); client.setClient(c);

return client; }

publicstaticvoid closeClient(ClientInfo client) { if (nul = client) {

return; }

if (nul != client.getTransport() { client.getTransport().close(); }

if (nul != client.getTsocket() { client.getTsocket().close(); }

} }然后就可以写⾃⼰的逻辑去获取集群和拓扑的数据了，Storm提供的UI界⾯上展示的数据基本都 可以获取到，这⾥只举出⼀个简单的例⼦，我们想获得某个拓扑发⽣异常的次数，和发⽣的异常 的堆栈。剩下的项⽬你可以随意的定制。

下⾯是⼊⼝类： Main.java[java]

view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.


package com.damacheng09.storm.monitor;

import com.damacheng09.storm.monitor.logic.Logic;

/*

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
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.


- * ⼊⼝类
- * @author jb-xingchencheng

*

- */ publicclas Main {


/ NIMBUS的信息 publicstatic String NIMBUS_HOST = "192.168.180.36"; publicstaticint NIMBUS_PORT = 627;

/*

- * 命令格式 CMD（命令） [ARG0] [ARG1].（更多参数）
- * @param args
- */ publicstaticvoid main(String[] args) {


if (args.length < 3) {

return; }

NIMBUS_HOST = args[0]; NIMBUS_PORT = Integer.parseInt(args[1]);

String cmd = args[2]; String result = "-1"; if (cmd.equals("get_topo_exp_size") {

String topoName = args[3]; result = Logic.getTopoExpSize(topoName);

} elseif (cmd.equals("get_topo_exp_stack_trace") { String topoName = args[3]; result = Logic.getTopoExpStackTrace(topoName);

}

System.out.println(result); }

}

测试的时候把具体的HOST和PORT改⼀下即可。然后是具体的逻辑类。 Logic.java[java]

view plain copy

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
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.


package com.damacheng09.storm.monitor.logic;

import java.util.Date; import java.util.List; import java.util.Set;

import com.damacheng09.storm.monitor.Main; import com.damacheng09.storm.monitor.thrift.ClientInfo; import com.damacheng09.storm.monitor.thrift.ClientManager;

import backtype.storm.generated.ClusterSumary; import backtype.storm.generated.ErorInfo; import backtype.storm.generated.TopologyInfo; import backtype.storm.generated.TopologySumary;

publicclas Logic { /*

- * 取得某个拓扑的异常个数
- * @param topoName
- * @return
- */ publicstatic String getTopoExpSize(String topoName) {


ClientInfo client = nul; int erorTotal = 0;

try { client = ClientManager.getClient(Main.NIMBUS_HOST, Main.NIMBUS_PORT);

ClusterSumary clusterSumary = client.getClient().getClusterInfo(); List<TopologySumary> topoSumaryList = clusterSumary.getTopologies(); for (TopologySumary ts : topoSumaryList) {

if (ts.getName().equals(topoName) { TopologyInfo topologyInfo = client.getClient().getTopologyInfo(ts.getId(); Set<String> erorKeySet = topologyInfo.getErors().keySet(); for (String erorKey : erorKeySet) {

- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.


List<ErorInfo> listErorInfo = topologyInfo.getErors().get(erorKey); erorTotal += listErorInfo.size();

} break;

} }

return String.valueOf(erorTotal); } catch (Exception e) {

return"-1"; } finaly {

ClientManager.closeClient(client); }

}

/*

- * 返回某个拓扑的异常堆栈
- * @param topoName
- * @return
- */ publicstatic String getTopoExpStackTrace(String topoName) {


ClientInfo client = nul; StringBuilder eror = new StringBuilder();

try { client = ClientManager.getClient(Main.NIMBUS_HOST, Main.NIMBUS_PORT);

ClusterSumary clusterSumary = client.getClient().getClusterInfo(); List<TopologySumary> topoSumaryList = clusterSumary.getTopologies(); for (TopologySumary ts : topoSumaryList) {

if (ts.getName().equals(topoName) { TopologyInfo topologyInfo = client.getClient().getTopologyInfo(ts.getId();

/ 得到错误信息 Set<String> erorKeySet = topologyInfo.getErors().keySet(); for (String erorKey : erorKeySet) {

List<ErorInfo> listErorInfo = topologyInfo.getErors().get(erorKey); for (ErorInfo ei : listErorInfo) {

- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.
- 92.
- 93.
- 94.
- 95.
- 96.
- 97.
- 98.
- 99.
- 100.


/ 发⽣异常的时间 long expTime = (long) ei.getEror_time_secs() * 1 0; / 现在的时间 long now = System.curentTimeMilis();

/ 由于获取的是全量的错误堆栈，我们可以设置⼀个范围来获取指定范围的错 误，看情况⽽定

/ 如果超过5min，那么就不⽤记录了，因为5min检查⼀次 if (now - expTime > 1 0 * 60 * 5) {

continue; }

eror.apend(new Date(expTime) + "\n"); eror.apend(ei.getEror() + "\n");

} }

break; }

}

return eror.toString().isEmpty() ? "none" : eror.toString(); } catch (Exception e) {

return"-1"; } finaly {

ClientManager.closeClient(client); }

} }

最后打成⼀个Jar包，就可以跑起来接⼊监控系统了，如在Zabix中，可以把各个监控项设置为⾃定义 的item，在Zabix Client中配置命令⾏来运⾏Jar取得数据。 接下来的测试过程先略过。 对于Storm监控的实践，⽬前就是这样了。

