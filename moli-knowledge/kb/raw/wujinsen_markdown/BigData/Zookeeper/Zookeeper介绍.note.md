Zokeper

# 1. Zokeper概念简介：

Zokeper是⼀个分布式协调服务；就是为⽤户的分布式应⽤程序提供协调服务

- A、zokeper是为别的分布式程序服务的
- B、Zokeper本身就是⼀个分布式程序（只要有半数以上节点存活，zk就能正常服务）
- C、Zokeper所提供的服务涵盖：主从协调、服务器节点动态上下线、统⼀配置管理、分布式共享 锁、统⼀名称服务……
- D、虽然说可以提供各种服务，但是zokeper在底层其实只提供了两个功能： 管理(存储，读取)⽤户提交的数据； 并为数据提供监听服务；


Zokeper常⽤应⽤场景： 《⻅图》

Zokeper集群的⻆⾊： Leader 和 folower （Observer） 只要集群中有半数以上节点存活，集群就能提供服务

# 2. zokeper集群机制

半数机制：集群中半数以上机器存活，集群可⽤。 zokeper适合装在奇数台机器上！！！

# 3. 安装

- 3.1. 安装


- 3.1.1. 机器部署 安装到3台虚拟机上 安装好JDK
- 3.1.2. 上传 上传⽤⼯具。
- 3.1.3. 解压 su – hadop（切换到hadop⽤户） tar -zxvf zokeper-3.4.5.tar.gz（解压）
- 3.1.4. 重命名


mv zokeper-3.4.5 zokeper（重命名⽂件夹zokeper-3.4.5为zokeper）

### 3.1.5. 修改环境变量

- 1、su – rot(切换⽤户到rot)
- 2、vi /etc/profile(修改⽂件)
- 3、添加内容：
- 4、重新编译⽂件： source /etc/profile
- 5、注意：3台zokeper都需要修改
- 6、修改完成后切换回hadop⽤户： su - hadop


<table>
  <tr>
    <th>export ZOKEPER_HOME=/home/hadop/zokeper</th>
  </tr>
</table>


export PATH=$PATH:$ZOKEPER_HOME/bin

### 3.1.6. 修改配置⽂件

- 1、⽤hadop⽤户操作 cd zokeper/conf cp zo_sample.cfg zo.cfg
- 2、vi zo.cfg
- 3、添加内容：
- 4、创建⽂件夹： cd /home/hadop/zokeper/ mkdir -m 755 data mkdir -m 755 log
- 5、在data⽂件夹下新建myid⽂件，myid的⽂件内容为： cd data vi myid 添加内容：


<table>
  <tr>
    <th>dataDir=/home/hadop/zokeper/data dataLogDir=/home/hadop/zokeper/log server.1=slave1 2 8 3 8 (⼼跳端⼝、数据端⼝) server2sve2  8 3 8</th>
  </tr>
</table>


server.3=slave3 2 8 3 8

<table>
  <tr>
    <th> </th>
  </tr>
</table>


1

### 3.1.7. 将集群下发到其他机器上

- scp -r /home/hadop/zokeper hadop@slave2:/home/hadop/
- scp -r /home/hadop/zokeper hadop@slave3:/home/hadop/


- 3.1.8. 修改其他机器的配置⽂件

- 到slave2上：修改myid为：2
- 到slave3上：修改myid为：3


- 3.1.9. 启动（每台机器） bin/zkServer.sh start

- 3.1.10. 查看集群状态


- 1、 jps（查看进程）
- 2、 zkServer.sh status（查看集群状态，主从信息）


# 4. zokeper结构和命令

- 4.1. zokeper特性


- 1、Zokeper：⼀个leader，多个folower组成的集群
- 2、全局数据⼀致：每个server保存⼀份相同的数据副本，client⽆论连接到哪个server，数据都是⼀致 的
- 3、分布式读写，更新请求转发，由leader实施
- 4、更新请求顺序进⾏，来⾃同⼀个client的更新请求按其发送顺序依次执⾏
- 5、数据更新原⼦性，⼀次数据更新要么成功，要么失败
- 6、实时性，在⼀定时间范围内，client能读到最新数据


## 4.2. zokeper数据结构

- 1、层次化的⽬录结构，命名符合常规⽂件系统规范(⻅下图)
- 2、每个节点在zokeper中叫做znode,并且其有⼀个唯⼀的路径标识

- 3、节点Znode可以包含数据和⼦节点（但是EPHEMERAL类型的节点不能有⼦节点，下⼀⻚详细讲 解）
- 4、客户端应⽤可以在节点上设置监视器（后续详细讲解）


## 4.3. 数据结构的图

![image 1](<Zookeeper介绍.note_images/imageFile1.png>)

## 4.4. 节点类型

- 1、Znode有两种类型： 短暂（ephemeral）（断开连接⾃⼰删除） 持久（persistent）（断开连接不删除）
- 2、Znode有四种形式的⽬录节点（默认是persistent ） PERSISTENT PERSISTENT_SEQUENTIAL（持久序列/test 019 ） EPHEMERAL EPHEMERAL_SEQUENTIAL
- 3、创建znode时设置顺序标识，znode名称后会附加⼀个值，顺序号是⼀个单调递增的计数器，由⽗ 节点维护
- 4、在分布式系统中，顺序号可以被⽤于为所有的事件进⾏全局排序，这样客户端可以通过顺序号推断 事件的顺序


## 4.5. zokeper命令⾏操作

运⾏ zkCli.sh –server <ip>进⼊命令⾏⼯具

![image 2](<Zookeeper介绍.note_images/imageFile2.png>)

- 1、使⽤ ls 命令来查看当前 ZoKeper 中所包含的内容：

- [zk: 202.15.36.251 2181(CONECTED) 1] ls /

2、创建⼀个新的 znode ，使⽤ create /zk myData 。这个命令创建了⼀个新的 znode 节点“ zk ”以及 与它关联的字符串：

- [zk: 202.15.36.251 2181(CONECTED) 2] create /zk "myData“

3、我们运⾏ get 命令来确认 znode 是否包含我们所创建的字符串：

- [zk: 202.15.36.251 2181(CONECTED) 3] get /zk




![image 3](<Zookeeper介绍.note_images/imageFile3.png>)

- 4、下⾯我们通过 set 命令来对 zk 所关联的字符串进⾏设置：

- [zk: 202.15.36.251 2181(CONECTED) 4] set /zk "zsl“ 修改”testData”为”zsl”

5、下⾯我们将刚才创建的 znode 删除：

- [zk: 202.15.36.251 2181(CONECTED) 5] delete /zk


- 6、删除节点：rmr [zk: 202.15.36.251 2181(CONECTED) 5] rmr /zk


## 4.6. zokeper-api应⽤

- 4.6.1. 基本使⽤ org.apache.zookeeper.Zookeeper是客户端⼊⼝主类，负责建⽴与server的会话


它提供了表 1 所示⼏类主要⽅法 ：

<table>
  <tr>
    <th>功能</th>
    <th>描述</th>
  </tr>
  <tr>
    <td>create</td>
    <td>在本地⽬录树中创建⼀个节点</td>
  </tr>
  <tr>
    <td>delete</td>
    <td>删除⼀个节点</td>
  </tr>
  <tr>
    <td>exists</td>
    <td>测试本地是否存在⽬标节点</td>
  </tr>
  <tr>
    <td>get/set data</td>
    <td>写数据</td>
  </tr>
  <tr>
    <td>get/set ACL</td>
    <td>从⽬标节点上读取 / 设置⽬标节点访问控制列表信息</td>
  </tr>
  <tr>
    <td>get children</td>
    <td>获取 / 检索⼀个⼦节点上的列表</td>
  </tr>
  <tr>
    <td> </td>
    <td>等待要被传送的数据</td>
  </tr>
</table>


sync

表 1： ZoKeper API描述

### 4.6.2. demo增删改查

<table>
  <tr>
    <th>public clas SimpleDemo { / 会话超时时间，设置为与系统默认时间⼀致 private static final int SESION_TIMEOUT = 3 0;<br><br>/ 创建 ZoKeper 实例 ZoKeper zk;<br><br>/ 创建 Watcher 实例 Watcher wh = new Watcher() { public void proces(org.apache.zokeper.WatchedEvent event) { System.out.println(event.toString(); } };<br><br>/ 初始化 ZoKeper 实例 private void createZKInstance() throws IOException { zk = new ZoKeper("wekend01 2181", SimpleDemo.SESION_TIMEOUT, this.wh); } private void ZKOperations() throws IOException, InterruptedException, KeperException {<br><br>System.out.println("/n1. 创建 ZoKeper 节点 (znode ： zo2, 数据： myData2 ，权限： OPEN_ACL_UNSAFE ，节点类型：<br><br>Persistent"); zk.create("/zo2", "myData2".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);<br><br>System.out.println("/n2. 查看是否创建成功： "); System.out.println(new String(zk.getData("/zo2", false, nul );<br>System.out.println("/n3. 修改节点数据 "); zk.setData("/zo2", "shenlan21314".getBytes(), -1);<br>System.out.println("/n4. 查看是否修改成功： "); System.out.println(new String(zk.getData("/zo2", false, nul );<br>System.out.println("/n5. 删除节点 "); zk.delete("/zo2", -1);<br>System.out.println("/n6. 查看节点是否被删除： "); System.out.println(" 节点状态： [" + zk.exists("/zo2", false) + "]"); } private void ZKClose() throws InterruptedException { zk.close(); } public static void main(String[] args) throws IOException, InterruptedException, KeperException { SimpleDemo dm = new SimpleDemo(); dm.createZKInstance(); dm.ZKOperations(); dm.ZKClose(); }<br></th>
  </tr>
</table>


}

Zookeeper的监听器⼯作机制

![image 4](<Zookeeper介绍.note_images/imageFile4.png>)

监听器是⼀个接⼝，我们的代码中可以实现Wather这个接⼝，实现其中的process⽅法，⽅法中即我 们⾃⼰的业务逻辑

监听器的注册是在获取数据的操作中实现： getData(path,watch?)监听的事件是：节点数据变化事件 getChildren(path,watch?)监听的事件是：节点下的⼦节点增减变化事件

## 4.7. zokeper应⽤案例（分布式应⽤HA|分布式锁）

- 3.7.1 实现分布式应⽤的(主节点HA)及客户端动态更新主节点状态 某分布式系统中，主节点可以有多台，可以动态上下线 任意⼀台客户端都能实时感知到主节点服务器的上下线


![image 5](<Zookeeper介绍.note_images/imageFile5.png>)

#### A、客户端实现

<table>
  <tr>
    <th>public clas ApClient { private String groupNode = "sgroup"; private ZoKeper zk; private Stat stat = new Stat(); private volatile List<String> serverList;<br><br>/*<br><br>* 连接zokeper<br>*/ public void conectZokeper() throws Exception { zk<br><br><br>= new ZoKeper("localhost:4180,localhost:4181,localhost:4182", 5 0, new Watcher() { public void proces(WatchedEvent event) {<br><br>/ 如果发⽣了"/sgroup"节点下的⼦节点变化事件, 更新server列表, 并重新注册监听 if (event.getType() = EventType.NodeChildrenChanged<br><br>& ("/" + groupNode).equals(event.getPath( ) { try { updateServerList(); } catch (Exception e) { e.printStackTrace(); } } } });<br><br>updateServerList(); }<br><br>/*<br><br>* 更新server列表<br>*/ private void updateServerList() throws Exception { List<String> newServerList = new ArrayList<String>();<br><br>/ 获取并监听groupNode的⼦节点变化 / watch参数为true, 表示监听⼦节点变化事件. / 每次都需要重新注册监听, 因为⼀次注册, 只能监听⼀次事件, 如果还想继续保持监听, 必须重新注册<br><br>List<String> subList = zk.getChildren("/" + groupNode, true); for (String subNode : subList) {<br><br>/ 获取每个⼦节点下关联的server地址 byte[] data = zk.getData("/" + groupNode + "/" + subNode, false, stat); newServerList.ad(new String(data, "utf-8"); }<br><br>/ 替换server列表 serverList = newServerList;<br><br>System.out.println("server list updated: " + serverList); }<br><br>/*<br><br>* client的⼯作逻辑写在这个⽅法中<br>* 此处不做任何处理, 只让client sl ep<br>*/ public void handle() throws InterruptedException { Thread.sl ep(Long.MAX_VALUE); }<br><br><br>public static void main(String[] args) throws Exception { ApClient ac = new ApClient(); ac.conectZokeper();<br><br>ac.handle(); }</th>
  </tr>
</table>


##### }

- B、服务器端实现


<table>
  <tr>
    <th>public clas ApServer { private String groupNode = "sgroup"; private String subNode = "sub";<br><br>/*<br><br>* 连接zokeper<br>* @param adres server的地址<br>*/ public void conectZokeper(String adres) throws Exception { ZoKeper zk = new ZoKeper(<br><br><br>"localhost:4180,localhost:4181,localhost:4182", 5 0, new Watcher() {<br><br>public void proces(WatchedEvent event) {<br><br>/ 不做处理 } });<br><br>/ 在"/sgroup"下创建⼦节点 / ⼦节点的类型设置为EPHEMERAL_SEQUENTIAL, 表明这是⼀个临时节点, 且在⼦节点的名称后⾯加上⼀串数字后缀 / 将server的地址数据关联到新创建的⼦节点上<br><br>String createdPath = zk.create("/" + groupNode + "/" + subNode, adres.getBytes("utf-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL); System.out.println("create: " + createdPath); }<br><br>/*<br><br>* server的⼯作逻辑写在这个⽅法中<br>* 此处不做任何处理, 只让server sl ep<br>*/ public void handle() throws InterruptedException { Thread.sl ep(Long.MAX_VALUE); }<br><br><br>public static void main(String[] args) throws Exception {<br><br>/ 在参数中指定server的地址 if (args.length = 0) { System.err.println("The first argument must be server adres"); System.exit(1); }<br><br>ApServer as = new ApServer(); as.conectZokeper(args[0]); as.handle(); }</th>
  </tr>
</table>


}

3.7.2分布式共享锁的简单实现

 客户端A

public clas DistributedClient { / 超时时间 private static final int SESION_TIMEOUT = 5 0;

/ zokeper server列表 private String hosts = "localhost:4180,localhost:4181,localhost:4182"; private String groupNode = "locks"; private String subNode = "sub";

private ZoKeper zk;

/ 当前client创建的⼦节点 private String thisPath;

/ 当前client等待的⼦节点 private String waitPath;

private CountDownLatch latch = new CountDownLatch(1);

/*

- * 连接zokeper
- */ public void conectZokeper() throws Exception { zk = new ZoKeper(hosts, SESION_TIMEOUT, new Watcher() { public void proces(WatchedEvent event) { try {


/ 连接建⽴时, 打开latch, 唤醒wait在该latch上的线程 if (event.getState() = KeperState.SyncConected) { latch.countDown(); }

/ 发⽣了waitPath的删除事件 if (event.getType() = EventType.NodeDeleted & event.getPath().equals(waitPath) { doSomething(); } } catch (Exception e) { e.printStackTrace(); } } });

/ 等待连接建⽴ latch.await();

/ 创建⼦节点 thisPath = zk.create("/" + groupNode + "/" + subNode, nul, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

/ wait⼀⼩会, 让结果更清晰⼀些 Thread.sl ep(10);

/ 注意, 没有必要监听"/locks"的⼦节点的变化情况 List<String> childrenNodes = zk.getChildren("/" + groupNode, false);

/ 列表中只有⼀个⼦节点, 那肯定就是thisPath, 说明client获得锁 if (childrenNodes.size() = 1) { doSomething(); } else { String thisNode = thisPath.substring("/" + groupNode + "/").length();

/ 排序 Colections.sort(childrenNodes); int index = childrenNodes.indexOf(thisNode); if (index = -1) {

/ never hapened } else if (index = 0) {

/ indx = 0, 说明thisNode在列表中最⼩, 当前client获得锁 doSomething(); } else {

/ 获得排名⽐thisPath前1位的节点

this.waitPath = "/" + groupNode + "/" + childrenNodes.get(index - 1);

/ 在waitPath上注册监听器, 当waitPath被删除时, zokeper会回调监听器的proces⽅法 zk.getData(waitPath, true, new Stat(); } } }

private void doSomething() throws Exception { try { System.out.println("gain lock: " + thisPath); Thread.sl ep(2 0);

/ do something } finaly { System.out.println("finished: " + thisPath);

/ 将thisPath删除, 监听thisPath的client将获得通知 / 相当于释放锁

zk.delete(this.thisPath, -1);

} }

public static void main(String[] args) throws Exception { for (int i = 0; i < 10; i +) { new Thread() { public void run() { try { DistributedClient dl = new DistributedClient(); dl.conectZokeper(); } catch (Exception e) { e.printStackTrace(); } } }.start(); }

Thread.sl ep(Long.MAX_VALUE); }

}

 分布式多进程模式实现：

public clas DistributedClientMy {

/ 超时时间 private static final int SESION_TIMEOUT = 5 0;

/ zokeper server列表 private String hosts =

"spark01 2181,spark02 2181,spark03 2181"; private String groupNode = "locks"; private String subNode = "sub"; private bolean haveLock = false;

private ZoKeper zk; / 当前client创建的⼦节点 private volatile String thisPath;

/*

- * 连接zokeper
- */ public void conectZokeper() throws Exception { zk = new ZoKeper("spark01 2181", SESION_TIMEOUT,


new Watcher() { public void proces(WatchedEvent event) { try {

/ ⼦节点发⽣变化

if (event.getType() = EventType.NodeChildrenChanged & event.getPath().equals("/" + groupNode) {

/ thisPath是否是列表中的最⼩节点

List<String> childrenNodes = zk.getChildren("/" + groupNode, true);

String thisNode = thisPath.substring("/" + groupNode + "/").length();

/ 排序 Colections.sort(childrenNodes); if (childrenNodes.indexOf(thisNode) = 0) { doSomething(); thisPath = zk.create("/" + groupNode + "/" + subNode, nul,

Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL); } } } catch (Exception e) { e.printStackTrace(); } } });

/ 创建⼦节点

thisPath = zk.create("/" + groupNode + "/" + subNode, nul, Ids.OPEN_ACL_UNSAFE,

CreateMode.EPHEMERAL_SEQUENTIAL);

/ wait⼀⼩会, 让结果更清晰⼀些 Thread.sl ep(new Random().nextInt(1 0);

/ 监听⼦节点的变化

List<String> childrenNodes = zk.getChildren("/" + groupNode, true);

/ 列表中只有⼀个⼦节点, 那肯定就是thisPath, 说明client获得

锁 if (childrenNodes.size() = 1) { doSomething(); thisPath = zk.create("/" + groupNode + "/" + subNode, nul,

Ids.OPEN_ACL_UNSAFE,

CreateMode.EPHEMERAL_SEQUENTIAL); } }

/*

- * 共享资源的访问逻辑写在这个⽅法中
- */ private void doSomething() throws Exception { try { System.out.println("gain lock: " + thisPath); Thread.sl ep(2 0);


/ do something } finaly { System.out.println("finished: " + thisPath);

/ 将thisPath删除, 监听thisPath的client将获得通知 / 相当于释放锁

zk.delete(this.thisPath, -1); } }

public static void main(String[] args) throws Exception { DistributedClientMy dl = new DistributedClientMy(); dl.conectZokeper(); Thread.sl ep(Long.MAX_VALUE); }

}

动⼿练习

# 5. zokeper原理

Zokeper虽然在配置⽂件中并没有指定master和slave 但是，zokeper⼯作时，是有⼀个节点为leader，其他则为folower Leader是通过内部的选举机制临时产⽣的

- 5.1. zokeper的选举机制（全新集群paxos）


以⼀个简单的例⼦来说明整个选举的过程. 假设有五台服务器组成的zokeper集群,它们的id从1-5,同时它们都是最新启动的,也就是没有历史数 据,在存放数据量这⼀点上,都是⼀样的.假设这些服务器依序启动,来看看会发⽣什么.

- 1) 服务器1启动,此时只有它⼀台服务器启动了,它发出去的报没有任何响应,所以它的选举状态⼀直是 LOKING状态


- 2) 服务器2启动,它与最开始启动的服务器1进⾏通信,互相交换⾃⼰的选举结果,由于两者都没有历史数 据,所以id值较⼤的服务器2胜出,但是由于没有达到超过半数以上的服务器都同意选举它(这个例⼦中的 半数以上是3),所以服务器1,2还是继续保持LOKING状态.
- 3) 服务器3启动,根据前⾯的理论分析,服务器3成为服务器1,2,3中的⽼⼤,⽽与上⾯不同的是,此时有三台 服务器选举了它,所以它成为了这次选举的leader.
- 4) 服务器4启动,根据前⾯的分析,理论上服务器4应该是服务器1,2,3,4中最⼤的,但是由于前⾯已经有半 数以上的服务器选举了服务器3,所以它只能接收当⼩弟的命了.
- 5) 服务器5启动,同4⼀样,当⼩弟.


## 5.2. ⾮全新集群的选举机制(数据恢复)

那么，初始化的时候，是按照上述的说明进⾏选举的，但是当zokeper运⾏了⼀段时间之后，有机器 down掉，重新选举时，选举过程就相对复杂了。 需要加⼊数据id、leader id和逻辑时钟。 数据id：数据新的id就⼤，数据每次更新都会更新id。 Leader id：就是我们配置的myid中的值，每个机器⼀个。 逻辑时钟：这个值从0开始递增,每次选举对应⼀个值,也就是说: 如果在同⼀次选举中,那么这个值应该 是⼀致的 ; 逻辑时钟值越⼤,说明这⼀次选举leader的进程更新. 选举的标准就变成：

- 1、逻辑时钟⼩的选举结果被忽略，重新投票
- 2、统⼀逻辑时钟后，数据id⼤的胜出
- 3、数据id相同的情况下，leader id⼤的胜出


根据这个规则选出leader。

