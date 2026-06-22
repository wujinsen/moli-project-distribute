如何使⽤

Zokeper 作为⼀个分布式的服务框架，主要⽤来解决分布式集群中应⽤系统的⼀致性问题，它能提 供基于类似于⽂件系统的⽬录节点树⽅式的数据存储，但是 Zokeper 并不是⽤来专⻔存储数据的， 它的作⽤主要是⽤来维护和监控你存储的数据的状态变化。通过监控这些数据状态的变化，从⽽可以 达到基于数据的集群管理，后⾯将会详细介绍 Zokeper 能够解决的⼀些典型问题，这⾥先介绍⼀ 下，Zokeper 的操作接⼝和简单使⽤示例。

常⽤接⼝列表

客户端要连接 Zokeper 服务器可以通过创建 org.apache.zokeper. ZoKeper 的⼀个实例对象， 然后调⽤这个类提供的接⼝来和服务器交互。 前⾯说了 ZoKeper 主要是⽤来维护和监控⼀个⽬录节点树中存储的数据的状态，所有我们能够操作 ZoKeper 的也和操作⽬录节点树⼤体⼀样，如创建⼀个⽬录节点，给某个⽬录节点设置数据，获取 某个⽬录节点的所有⼦⽬录节点，给某个⽬录节点设置权限和监控这个⽬录节点的状态变化。 这些接⼝如下表所示： 表 1 org.apache.zokeper. ZoKeper ⽅法列表 ⽅法名⽅法功能描述

<table>
  <tr>
    <th>String (String path, byte[] data, List<ACL> acl,CreateMode createM ode)<br><br>create</th>
    <th>创建⼀个给定的⽬录节点 path, 并给它设置数 据，<br><br>标识有四种形式的⽬录节点，分别是 PERSISTENT：持久化⽬录节点，这个⽬录节点 存储的数据不会丢失； PERSISTENT_SEQUENTIAL：顺序⾃动编号的⽬ 录节点，这种⽬录节点会根据当前已近存在的节 点数⾃动加 1，然后返回给客户端已经成功创建的 ⽬录节点名；EPHEMERAL：临时⽬录节点，⼀ 旦创建这个节点的客户端与服务器端⼝也就是 sesion 超时，这种节点会被⾃动删除；<br><br>：临时⾃动编号节点<br><br>CreateMode</th>
  </tr>
  <tr>
    <td>Stat (String path, bolean watch) exists</td>
    <td>EPHEMERAL_SEQUENTIAL<br><br>判断某个 path 是否存在，并设置是否监控这个⽬ 录节点，这⾥的 watcher 是在创建 ZoKeper 实 例时指定的 watcher，<br><br>exists</td>
  </tr>
  <tr>
    <td>Stat (String path,Watcher watcher) exists</td>
    <td>⽅法还有⼀个重载⽅法，可以指定特定的watcher 重载⽅法，这⾥给某个⽬录节点设置特定的 watcher，Watcher 在 ZoKeper 是⼀个核⼼功 能，Watcher 可以监控⽬录节点的数据变化以及 ⼦⽬录的变化，⼀旦这些状态发⽣变化，服务器 就会通知所有设置在这个⽬录节点上的 Watcher，从⽽每个客户端都很快知道它所关注 的⽬录节点的状态发⽣变化，⽽做出相应的反应</td>
  </tr>
  <tr>
    <td>void (<br><br>delete String</td>
    <td>删除 path 对应的⽬录节点，version 为 -1 可以匹 配任何版本，也就删除了这个⽬录节点所有数据</td>
  </tr>
  <tr>
    <td>path, int version) List<String> (String path, bolean watch) getChildren</td>
    <td>获取指定 path 下的所有⼦⽬录节点，同样<br><br>⽅法也有⼀个重载⽅法可以设置特定的 watcher 监控⼦节点的状态<br><br>getChildren</td>
  </tr>
  <tr>
    <td>Stat setData</td>
    <td>给 path 设置数据，可以指定这个数据的版本号， 如果 version 为 -1 怎可以匹配任何版本</td>
  </tr>
  <tr>
    <td>(String path, byte[] data, int version) byte[] (String path, bolean watch, Stat stat) getData</td>
    <td>获取这个 path 对应的⽬录节点存储的数据，数据 的版本等信息可以通过 stat 来指定，同时还可以 设置是否监控这个⽬录节点数据的状态</td>
  </tr>
  <tr>
    <td>void adAuthInfo</td>
    <td>客户端将⾃⼰的授权信息提交给服务器，服务器 将根据这个授权信息验证客户端的访问权限。</td>
  </tr>
</table>


## (String scheme, byte[] auth)

<table>
  <tr>
    <th>Stat (String path,List<ACL> acl, int version) setACL</th>
    <th>给某个⽬录节点重新设置访问权限，需要注意的 是 Zokeper 中的⽬录节点权限不具有传递性， ⽗⽬录节点的权限不能传递给⼦⽬录节点。⽬录 节点 ACL 由两部分组成：perms 和 id。<br><br>Perms 有 AL、READ、WRITE、CREATE、 DELETE、ADMIN ⼏种<br><br>⽽ id 标识了访问⽬录节点的身份列表，默认情况 下有以下两种：<br><br>ANYONE_ID_UNSAFE = new Id("world", "anyone") 和 AUTH_IDS = new Id("auth", ") 分 别表示任何⼈都可以访问和创建者拥有访问权 限。</th>
  </tr>
  <tr>
    <td>List<ACL> getACL</td>
    <td>获取某个⽬录节点的访问权限列表</td>
  </tr>
</table>


(String path,Stat stat)

除了以上这些上表中列出的⽅法之外还有⼀些重载⽅法，如都提供了⼀个回调类的重载⽅法以及可以 设置特定 Watcher 的重载⽅法，具体的⽅法可以参考 org.apache.zokeper. ZoKeper 类的 API 说 明。

# 基本操作

下⾯给出基本的操作 ZoKeper 的示例代码，这样你就能对 ZoKeper 有直观的认识了。下⾯的清 单包括了创建与 ZoKeper 服务器的连接以及最基本的数据操作：

ZoKeper 基本的操作示例

- 1 // 创建⼀个与服务器的连接

- 2 ZooKeeper zk = new ZooKeeper("localhost:" + CLIENT_PORT,

- 3 ClientBase.CONNECTION_TIMEOUT, new Watcher() {

- 4 // 监控所有被触发的事件

- 5 public void process(WatchedEvent event) {

- 6 System.out.println("已经触发了" + event.getType() + "事件！");

- 7 }

- 8 });

- 9 // 创建⼀个⽬录节点

- 10 zk.create("/testRootPath", "testRootData".getBytes(), Ids.OPEN_ACL_UNSAFE,

- 11 CreateMode.PERSISTENT);

- 12 // 创建⼀个⼦⽬录节点

- 13 zk.create("/testRootPath/testChildPathOne", "testChildDataOne".getBytes(),

- 14 Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);

- 15 System.out.println(new String(zk.getData("/testRootPath",false,null)));

- 16 // 取出⼦⽬录节点列表

- 17 System.out.println(zk.getChildren("/testRootPath",true));

- 18 // 修改⼦⽬录节点数据

zk.setData("/testRootPath/testChildPathOne","modifyChildDataOne".getBytes(),-1);

- 19

- 20 System.out.println("⽬录节点状态：["+zk.exists("/testRootPath",true)+"]");

- 21 // 创建另外⼀个⼦⽬录节点

- 22 zk.create("/testRootPath/testChildPathTwo", "testChildDataTwo".getBytes(),

- 23 Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);

System.out.println(new String(zk.getData("/testRootPath/testChildPathTwo",true,null)));

- 24

- 25 // 删除⼦⽬录节点

- 26 zk.delete("/testRootPath/testChildPathTwo",-1);

- 27 zk.delete("/testRootPath/testChildPathOne",-1);

- 28 // 删除⽗⽬录节点

- 29 zk.delete("/testRootPath",-1);

- 30 // 关闭连接

- 31 zk.close();


输出的结果如下：

- 1 已经触发了 None 事件！

- 2 testRootData

- 3 [testChildPathOne]

- 4 ⽬录节点状态：[5,5,1281804532336,1281804532336,0,1,0,0,12,1,6]

- 5 已经触发了 NodeChildrenChanged 事件！

- 6 testChildDataTwo

- 7 已经触发了 NodeDeleted 事件！

- 8 已经触发了 NodeDeleted 事件！


当对⽬录节点监控状态打开时，⼀旦⽬录节点的状态发⽣变化，Watcher 对象的 proces ⽅法就会被 调⽤。

# ZoKeper 典型的应⽤场景

Zokeper 从设计模式⻆度来看，是⼀个基于观察者模式设计的分布式服务管理框架，它负责存储和 管理⼤家都关⼼的数据，然后接受观察者的注册，⼀旦这些数据的状态发⽣变化，Zokeper 就将负 责通知已经在 Zokeper 上注册的那些观察者做出相应的反应，从⽽实现集群中类似 Master/Slave 管 理模式，关于 Zokeper 的详细架构等内部细节可以阅读 Zokeper 的源码 下⾯详细介绍这些典型的应⽤场景，也就是 Zokeper 到底能帮我们解决那些问题？下⾯将给出答 案。

统⼀命名服务（Name Service） 分布式应⽤中，通常需要有⼀套完整的命名规则，既能够产⽣唯⼀的名称⼜便于⼈识别和记住，通常 情况下⽤树形的名称结构是⼀个理想的选择，树形的名称结构是⼀个有层次的⽬录结构，既对⼈友好 ⼜不会重复。说到这⾥你可能想到了 JNDI，没错 Zokeper 的 Name Service 与 JNDI 能够完成的功 能是差不多的，它们都是将有层次的⽬录结构关联到⼀定资源上，但是 Zokeper 的 Name Service 更加是⼴泛意义上的关联，也许你并不需要将名称关联到特定资源上，你可能只需要⼀个不会重复名 称，就像数据库中产⽣⼀个唯⼀的数字主键⼀样。 Name Service 已经是 Zokeper 内置的功能，你只要调⽤ Zokeper 的 API 就能实现。如调⽤ create 接⼝就可以很容易创建⼀个⽬录节点。

配置管理（Configuration Management）

配置的管理在分布式应⽤环境中很常⻅，例如同⼀个应⽤系统需要多台 PC Server 运⾏，但是它们运 ⾏的应⽤系统的某些配置项是相同的，如果要修改这些相同的配置项，那么就必须同时修改每台运⾏ 这个应⽤系统的 PC Server，这样⾮常麻烦⽽且容易出错。 像这样的配置信息完全可以交给 Zokeper 来管理，将配置信息保存在 Zokeper 的某个⽬录节点 中，然后将所有需要修改的应⽤机器监控配置信息的状态，⼀旦配置信息发⽣变化，每台应⽤机器就 会收到 Zokeper 的通知，然后从 Zokeper 获取新的配置信息应⽤到系统中。

- 图 2. 配置管理结构图


![image 1](<Zookeeper Api(java)入门与应用.note_images/imageFile1.png>)

集群管理（Group Membership） Zokeper 能够很容易的实现集群管理的功能，如有多台 Server 组成⼀个服务集群，那么必须要⼀个 “总管”知道当前集群中每台机器的服务状态，⼀旦有机器不能提供服务，集群中其它集群必须知道， 从⽽做出调整重新分配服务策略。同样当增加集群的服务能⼒时，就会增加⼀台或多台 Server，同样 也必须让“总管”知道。 Zokeper 不仅能够帮你维护当前的集群中机器的服务状态，⽽且能够帮你选出⼀个“总管”，让这个 总管来管理集群，这就是 Zokeper 的另⼀个功能 Leader Election。 它们的实现⽅式都是在 Zokeper 上创建⼀个 EPHEMERAL 类型的⽬录节点，然后每个 Server 在它 们创建⽬录节点的⽗⽬录节点上调⽤ ( path, bolean watch) ⽅法并设置 watch 为 true，由于是 EPHEMERAL ⽬录节点，当创建它的 Server 死去，这个⽬录节点也随之被删除，所以 Children 将会变化，这时 上的 Watch 将会被调⽤，所以其它 Server 就知道已经有某台 Server 死去了。新增 Server 也是同样的原理。

getChildrenString

getChildren

Zokeper 如何实现 Leader Election，也就是选出⼀个 Master Server。和前⾯的⼀样每台 Server 创 建⼀个 EPHEMERAL ⽬录节点，不同的是它还是⼀个 SEQUENTIAL ⽬录节点，所以它是个 EPHEMERAL_SEQUENTIAL ⽬录节点。之所以它是 EPHEMERAL_SEQUENTIAL ⽬录节点，是因为我 们可以给每台 Server 编号，我们可以选择当前是最⼩编号的 Server 为 Master，假如这个最⼩编号的 Server 死去，由于是 EPHEMERAL 节点，死去的 Server 对应的节点也被删除，所以当前的节点列表 中⼜出现⼀个最⼩编号的节点，我们就选择这个节点为当前 Master。这样就实现了动态选择 Master， 避免了传统意义上单 Master 容易出现单点故障的问题。

- 图 3. 集群管理结构图


![image 2](<Zookeeper Api(java)入门与应用.note_images/imageFile2.png>)

这部分的示例代码如下，完整的代码请看附件： Leader Election 关键代码

- 1 void findLeader() throws InterruptedException {

- 2 byte[] leader = null;

- 3 try {

- 4 leader = zk.getData(root + "/leader", true, null);

- 5 } catch (Exception e) {

- 6 logger.error(e);

- 7 }

- 8 if (leader != null) {

- 9 following();

- 10 } else {

- 11 String newLeader = null;

- 12 try {

- 13 byte[] localhost = InetAddress.getLocalHost().getAddress();

- 14 newLeader = zk.create(root + "/leader", localhost,

- 15 ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);

- 16 } catch (Exception e) {

- 17 logger.error(e);

- 18 }

- 19 if (newLeader != null) {

- 20 leading();

- 21 } else {

- 22 mutex.wait();

- 23 }

- 24 }

- 25 }


共享锁（Locks） 共享锁在同⼀个进程中很容易实现，但是在跨进程或者在不同 Server 之间就不好实现了。Zokeper 却很容易实现这个功能，实现⽅式也是需要获得锁的 Server 创建⼀个 EPHEMERAL_SEQUENTIAL ⽬ 录节点，然后调⽤ ⽅法获取当前的⽬录节点列表中最⼩的⽬录节点是不是就是⾃⼰创建的 ⽬录节点，如果正是⾃⼰创建的，那么它就获得了这个锁，如果不是那么它就调⽤ ( path, bolean watch) ⽅法并监控 Zokeper 上⽬录节点列表的变化，⼀直到⾃⼰创建的节点是列表中最⼩ 编号的⽬录节点，从⽽获得锁，释放锁很简单，只要删除前⾯它⾃⼰所创建的⽬录节点就⾏了。

getChildren

existsString

- 图 4. Zokeper 实现 Locks 的流程图


![image 3](<Zookeeper Api(java)入门与应用.note_images/imageFile3.png>)

## 同步锁的实现代码如下，完整的代码请看附件： 同步锁的关键思路

- 1 加锁：

- 2 ZooKeeper 将按照如下⽅式实现加锁的操作：

1 ） ZooKeeper 调⽤ create （）⽅法来创建⼀个路径格式为“ _locknode_/lock- ”的节点，此节 点类型为sequence （连续）和 ephemeral （临时）。也就是说，创建的节点为临时节点，并且所有的节 点连续编号，即“ lock-i ”的格式。

- 3

2 ）在创建的锁节点上调⽤ getChildren （）⽅法，来获取锁⽬录下的最⼩编号节点，并且不设置 watch 。

- 4

3 ）步骤 2 中获取的节点恰好是步骤 1 中客户端创建的节点，那么此客户端获得此种类型的锁，然后退出 操作。

- 5

4 ）客户端在锁⽬录上调⽤ exists （）⽅法，并且设置 watch 来监视锁⽬录下⽐⾃⼰⼩⼀个的连续临 时节点的状态。

- 6

- 7 5 ）如果监视节点状态发⽣变化，则跳转到第 2 步，继续进⾏后续的操作，直到退出锁竞争。

- 8

- 9 解锁：

- 10 ZooKeeper 解锁操作⾮常简单，客户端只需要将加锁操作步骤 1 中创建的临时节点删除即可。


同步锁的关键代码

- 1 void getLock() throws KeeperException, InterruptedException{

- 2 List<String> list = zk.getChildren(root, false);

- 3 String[] nodes = list.toArray(new String[list.size()]);

- 4 Arrays.sort(nodes);

- 5 if(myZnode.equals(root+"/"+nodes[0])){

- 6 doAction();

- 7 }

- 8 else{

- 9 waitForLock(nodes[0]);

- 10 }

- 11 }

void waitForLock(String lower) throws InterruptedException, KeeperException {

- 12

- 13 Stat stat = zk.exists(root + "/" + lower,true);

- 14 if(stat != null){

- 15 mutex.wait();

- 16 }

- 17 else{

- 18 getLock();

- 19 }

- 20 }


队列管理 Zokeper 可以处理两种类型的队列：

- 1.
- 2.


当⼀个队列的成员都聚⻬时，这个队列才可⽤，否则⼀直等待所有成员到达，这种是同步队列。 队列按照 FIFO ⽅式进⾏⼊队和出队操作，例如实现⽣产者和消费者模型。

同步队列⽤ Zokeper 实现的实现思路如下：

创建⼀个⽗⽬录 /synchronizing，每个成员都监控标志（Set Watch）位⽬录 /synchronizing/start 是 否存在，然后每个成员都加⼊这个队列，加⼊队列的⽅式就是创建 /synchronizing/member_i 的临时 ⽬录节点，然后每个成员获取 / synchronizing ⽬录的所有⽬录节点，也就是 member_i。判断 i 的值 是否已经是成员的个数，如果⼩于成员个数等待 /synchronizing/start 的出现，如果已经相等就创建 /synchronizing/start。 ⽤下⾯的流程图更容易理解：

- 图 5. 同步队列流程图


![image 4](<Zookeeper Api(java)入门与应用.note_images/imageFile4.png>)

同步队列的关键代码如下，完整的代码请看附件： 同步队列

- 1 void addQueue() throws KeeperException, InterruptedException{

- 2 zk.exists(root + "/start",true);

- 3 zk.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE,

- 4 CreateMode.EPHEMERAL_SEQUENTIAL);

- 5 synchronized (mutex) {

- 6 List<String> list = zk.getChildren(root, false);

- 7 if (list.size() < size) {

- 8 mutex.wait();

- 9 } else {

- 10 zk.create(root + "/start", new byte[0], Ids.OPEN_ACL_UNSAFE,

- 11 CreateMode.PERSISTENT);

- 12 }

- 13 }

- 14 }


当队列没满是进⼊ wait()，然后会⼀直等待 Watch 的通知，Watch 的代码如下：

- 1 public void process(WatchedEvent event) {

- 2 if(event.getPath().equals(root + "/start") &&

- 3 event.getType() == Event.EventType.NodeCreated){

- 4 System.out.println("得到通知");

- 5 super.process(event);

- 6 doAction();

- 7 }

- 8 }


FIFO 队列⽤ Zokeper 实现思路如下： 实现的思路也⾮常简单，就是在特定的⽬录下创建 SEQUENTIAL 类型的⼦⽬录 /queue_i，这样就能保 证所有成员加⼊队列时都是有编号的，出队列时通过 getChildren( ) ⽅法可以返回当前所有的队列中的 元素，然后消费其中最⼩的⼀个，这样就能保证 FIFO。 下⾯是⽣产者和消费者这种队列形式的示例代码，完整的代码请看附件：

⽣产者代码

- 1 boolean produce(int i) throws KeeperException, InterruptedException{

- 2 ByteBuffer b = ByteBuffer.allocate(4);

- 3 byte[] value;

- 4 b.putInt(i);

- 5 value = b.array();

- 6 zk.create(root + "/element", value, ZooDefs.Ids.OPEN_ACL_UNSAFE,

- 7 CreateMode.PERSISTENT_SEQUENTIAL);

- 8 return true;

- 9 }


消费者代码

- 1 int consume() throws KeeperException, InterruptedException{

- 2 int retvalue = -1;

- 3 Stat stat = null;

- 4 while (true) {

- 5 synchronized (mutex) {

- 6 List<String> list = zk.getChildren(root, true);

- 7 if (list.size() == 0) {

- 8 mutex.wait();

- 9 } else {

- 10 Integer min = new Integer(list.get(0).substring(7));

- 11 for(String s : list){

- 12 Integer tempValue = new Integer(s.substring(7));

- 13 if(tempValue < min) min = tempValue;

- 14 }

- 15 byte[] b = zk.getData(root + "/element" + min,false, stat);

- 16 zk.delete(root + "/element" + min, 0);

- 17 ByteBuffer buffer = ByteBuffer.wrap(b);

- 18 retvalue = buffer.getInt();

- 19 return retvalue;

- 20 }

- 21 }

- 22 }

- 23 }


# 总结

Zokeper 作为 Hadop 项⽬中的⼀个⼦项⽬，是 Hadop 集群管理的⼀个必不可少的模块，它主要 ⽤来控制集群中的数据，如它管理 Hadop 集群中的 NameNode，还有 Hbase 中 Master Election、 Server 之间状态同步等。 本⽂介绍的 Zokeper 的基本知识，以及介绍了⼏个典型的应⽤场景。这些都是 Zokeper 的基本功 能，最重要的是 Zopkeper 提供了⼀套很好的分布式集群管理的机制，就是它这种基于层次型的⽬ 录树的数据结构，并对树中的节点进⾏有效管理，从⽽可以设计出多种多样的分布式的数据管理模 型，⽽不仅仅局限于上⾯提到的⼏个常⽤应⽤场景。

