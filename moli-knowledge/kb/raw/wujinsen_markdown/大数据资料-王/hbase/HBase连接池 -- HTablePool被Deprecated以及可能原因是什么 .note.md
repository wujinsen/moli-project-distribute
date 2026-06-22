问题导读：

- 1.官⽅如何解释HTablePool被弃⽤的

- 2.使⽤哪个类，代替HTablePool？

- 3.使⽤HConnectionManager如何创建表？


![image 1](<HBase连接池 -- HTablePool被Deprecated以及可能原因是什么 .note_images/imageFile1.png>)

1.连接

HTable是HBase的client，负责从meta表中找到⽬标数据所在的RegionServers，当定位到⽬标 RegionServers后，client直接和RegionServers交互，⽽不⽐再经过master。 HTable实例并不是线程安全的。 当需要创建HTable实例时，明智的做法是使⽤相同的HBaseConfiguration实例，这使得共享连接到 RegionServers的ZK和socket实例，例如，应该使⽤这样的代码：

- 1.
- 2.
- 3.


HBaseConfiguration conf = HBaseConfiguration.create();

- HTable table1 = new HTable(conf, "myTable");
- HTable table2 = new HTable(conf, "myTable");复制代码


⽽不是这样的代码：

- 1.
- 2.
- 3.
- 4.


- HBaseConfiguration conf1 = HBaseConfiguration.create();

- HTable table1 = new HTable(conf1, "myTable");

HBaseConfiguration conf2 = HBaseConfiguration.create();

- HTable table2 = new HTable(conf2, "myTable");复制代码2.连接池




当⾯对多线程访问需求时，我们可以预先建⽴HConection，参⻅以下代码：

Example 9.1. Pre-Creating a HConection

- 1.
- 2.
- 3.
- 4.


/ Create a conection to the cluster. HConection conection = HConectionManager.createConection(Configuration); HTableInterface table = conection.getTable("myTable");

/ use table as neded, the table returned is lightweight

5. 6. 7.

table.close();

/ use the conection for other aces to the cluster conection.close();复制代码

构建HTableInterface实现是⾮常轻量级的，并且资源是可控的。

注意： HTablePol是HBase连接池的⽼⽤法，该类在0.94，0.95和0.96中已经不建议使⽤，在0.98.1版本 以后已经移除。（简陋的官⽅⽂档到此为⽌。）

# 3.HConectionManager

该类是连接池的关键，专⻔介绍。 HConectionManager是⼀个不可实例化的类，专⻔⽤于创建HConection。 最简单的创建HConection实例的⽅式是 HConectionManager.createConection(config)，该⽅法创建了⼀个连接到集群的HConection实 例，该实例被创建的程序管理。通过这个HConection实例，可以使⽤ HConection.getTable(byte[])⽅法取得 HTableInterface implementations的实现，例如 :

HConection conection = HConectionManager

. createConection ( config );

HTableInterface table = conection.getTable("tablename");

try {

/ Use the table as neded, for a single operation and a single thread

} finaly {

table.close();

conection.close();

}

- 3.1构造函数 ⽆，不可实例化。


- 3.2常⽤⽅法


- （1）static HConection createConection(org.apache.hadop.conf.Configuration conf) 创建⼀个新的HConection实例。 该⽅法绕过了常规的HConection⽣命周期管理，常规是通过 getConection(Configuration)来获取连接。调⽤⽅负责执⾏ Closeable.close() 来关闭获得的连接实例。

推荐的创建HConection的⽅法是：

HConection conection = HConectionManager.createConection(conf);

HTableInterface table = conection.getTable("mytable");

table.get(.);

.

table.close();

conection.close();

- （2）public static HConection getConection(org.apache.hadop.conf.Configuration conf)


根据conf获取连接实例。如果没有对应的连接实例存在，该⽅法创建⼀个新的连接。

注意：该⽅法在0.96和0.98版本中都被

Deprecated了，不建议使⽤，但是在最新的未发布代码版本中⼜复活了！！！

- 3.3实例代码


package fulong.bigdata.hbase;

import java.io.IOException;

import org.apache.hadop.conf.Configuration;

import org.apache.hadop.hbase.Cel;

import org.apache.hadop.hbase.CelUtil;

import org.apache.hadop.hbase.HBaseConfiguration;

import org.apache.hadop.hbase.client.HConection;

import org.apache.hadop.hbase.client.HConectionManager;

import org.apache.hadop.hbase.client.HTableInterface;

import org.apache.hadop.hbase.client.Result;

import org.apache.hadop.hbase.client.ResultScaner;

import org.apache.hadop.hbase.client.Scan;

import org.apache.hadop.hbase.util.Bytes;

public clas ConectionPolTest {

privatestaticfinal String QUORUM = "FBI 01,FBI 02,FBI 03";

privatestaticfinal String CLIENTPORT = "2181";

privatestaticfinal String TABLENAME = "rd_ns:itable";

privatestatic Configuration conf = nul;

privatestatic HConection con = nul;

static{

try {

conf = HBaseConfiguration.create();

conf.set("hbase.zokeper.quorum", QUORUM);

conf.set("hbase.zokeper.property.clientPort", CLIENTPORT);

con = HConectionManager.createConection(conf);

} catch (IOException e) {

e.printStackTrace();

}

}

publicstaticvoid main(String[] args) throws IOException {

HTableInterface htable = ConectionPolTest.con.getTable(TABLENAME);

try {

Scan scan = new Scan();

ResultScaner rs = htable.getScaner(scan);

for (Result r : rs.next(5) {

for (Cel cel : r.rawCels() {

System.out.println("Rowkey : " + Bytes.toString(r.getRow()

+ " Familiy:Quilifier : "

+ Bytes.toString(CelUtil.cloneQualifier(cel)

+ " Value : "

+ Bytes.toString(CelUtil.cloneValue(cel)

+ " Time : " + cel.getTimestamp();

}

}

} finaly {

htable.close();

}

# 4.阅读源码的新发现

- 4.1消失的HConectionManager.getConection


从0.96和0.98版本HConectionManager的源码中可以看到

staticfinal Map<HConectionKey, HConectionImplementation> CONECTION_INSTANCES;

就是连接池，连接池中的每个连接⽤HConectionKey来标识，然⽽， HConectionManager 源码中所有涉及

CONECTION_INSTANCES 的⽅法全都被Deprcated了。

我们来看已经被Deprecated的getConection⽅法：

/*

- * Get the conection that goes with the pased <code>conf</code> configuration instance.

- * If no curent conection exists, method creates a new conection and keys it using
- * conection-specific properties from the pased {@link Configuration}; se
- * {@link HConectionKey}.
- * @param conf configuration
- * @return HConection object for <code>conf</code>

- * @throws ZoKeperConectionException
- */


@Deprecated

publicstatic HConection getConection(final Configuration conf)

throws IOException {

HConectionKey conectionKey = new HConectionKey(conf);

synchronized (CONECTION_INSTANCES) {

HConectionImplementation conection = CONECTION_INSTANCES.get(conectionKey);

if (conection = nul) {

conection = (HConectionImplementation)createConection(conf, true);

CONECTION_INSTANCES.put(conectionKey, conection);

} elseif (conection.isClosed() {

HConectionManager.deleteConection(conectionKey, true);

conection = (HConectionImplementation)createConection(conf, true);

CONECTION_INSTANCES.put(conectionKey, conection);

}

conection.incCount();

return conection;

该⽅法逻辑很简单：

根据传⼊的conf构建 HConectionKey，然后以 HConectionKey 实例为key到连接池Map对象 CONECTION_INSTANCES 中去查找conection，如果找到就返回conection，如果找不到就新建，如果找到但已被关闭，就删 除再新建。

我们来看HConectionKey的构造 ：

函数

HConectionKey(Configuration conf) {

Map<String, String> m = new HashMap<String, String>();

if (conf != nul) {

for (String property : CONECTION_PROPERTIES) {

String value = conf.get(property);

if (value != nul) {

m.put(property, value);

}

}

}

this.properties = Colections.unmodifiableMap(m);

try {

UserProvider provider = UserProvider.instantiate(conf);

User curentUser = provider.getCurent();

if (curentUser != nul) {

username = curentUser.getName();

}

} catch (IOException ioe) {

HConectionManager.LOG.warn("Eror obtaining curent user, ski ping username in HConectionKey", ioe);

}

}

由以上源码可知，接收conf构造 HConectionKey 实例时，其实是将conf配置⽂件中的属性赋值给 HConectionKey ⾃身的属性，换句话说，不管你new⼏次，只要conf的属性相同，new出来的 HConectionKey 实例的属性都相同。

结论⼀：conf的属性 -》 HConectionKey实例的属性

接下来，回到getConection源码中看到这样⼀句话：

HConectionImplementation conection = CONECTION_INSTANCES

.

get(conectionKey) ;

该代码是以 HConectionKey 实例为key来查找 CONECTION_INSTANCES 这个 LinkedHashMap 中是否已经包含了 HConectionKey 实例 为key的键值对，这⾥要注意的是，map的get⽅法，其实获取的是key的hashcode，这个⾃⼰读JDK 源码就能看到。

然⽽ HConectionKey 已经重载了hashcode⽅法：

@Overide

publicint hashCode() {

finalint prime = 31;

int result = 1;

if (username != nul) {

result = username.hashCode();

}

for (String property : CONECTION_PROPERTIES) {

String value = properties.get(property);

if (value != nul) {

result = prime * result + value.hashCode();

}

}

return result;

}

在该代码中，最终返回的hashcode取决于当前⽤户名及当前conf配置⽂件的属性。所以，只要conf配 置⽂件的属性和⽤户相同， HConectionKey 实例的hashcode就相同！

结论⼆：conf的属性 -》HConectionKey实例的hashcode

再来看刚才这句代码：

HConectionImplementation conection = CONECTION_INSTANCES

. get(conectionKey) ;

对于get⽅法的参数conectionKey，不管conectionKey是不是同⼀个对象，只要conectionKey的属 性相同，那conectionKey的hasecode就相同，对于get⽅法⽽⾔，也就是同样的key！！！ 所以，可以得出 结论三： conf的属性 -》HConectionKey实例的hashcode-》 get返回的conection实例

结论三换句话说说：

conf的属性相同 -》 CONECTION_INSTANCES.get返回同⼀个conection实例

然⽽，假设我们的HBase集群只有⼀个，那我们的HBase集群的conf配置⽂件也就只有⼀个（固定的 ⼀组属性），除⾮你有多个HBase集群另当别论。

在这样⼀个机制下，如果只有⼀个conf配置⽂件，则连接池中永远只会有⼀个conection实例！那 “池”的意义就不⼤了！

所以，代码中才将基于 getConection 获取池中物的机制 Deprecated了，转⽽在官⽅⽂档中建议：

*

*

当⾯对多线程访问需求时，我们可以预先建⽴HConection，参⻅以下代码：

Example 9.1. Pre-Creating a HConection

/ Create a conection to the cluster.HConection conection = HConectionManager.createConection(Configuration);HTableInterface table = conection.getTable("myTable");/ use table as neded, the table returned is lightweightable.close();/ use the conection for other aces to the clusterconection.close();

构建HTableInterface实现是⾮常轻量级的，并且资源是可控的。

*

*

（以上重新拷⻉了⼀次官⽅⽂档的翻译） 如果⼤家按照官⽅⽂档的建议做了，也就是预先创建了⼀个连接，以后的访问都共享该连接，这样的 效果其实和过去的

getConection 完全⼀样，都是在玩⼀个conection实例！

- 4.2 HBase的新时代


我查看了Git上最新版本的代码（ ），发 现getConection复活了：

htps:/git-wip-us.apache.org/repos/asf?p=hbase.git;a=tre

/*

- * Get the conection that goes with the pased <code>conf</code> configuration instance.

- * If no curent conection exists, method creates a new conection and keys it using
- * conection-specific properties from the pased {@link Configuration}; se
- * {@link HConectionKey}.
- * @param conf configuration
- * @return HConection object for <code>conf</code>

- * @throws ZoKeperConectionException
- */


publicstatic HConection getConection(final Configuration conf) throws IOException {

return ConectionManager.getConectionInternal(conf);

}

这个不是重点，重点是最新版本代码的pom：

<groupId>org.apache.hbase</groupId>

<artifactId>hbase</artifactId>

<packaging>pom</packaging>

<version>

2.0.0-SNAPSHOT </version>

<name>HBase</name>

<description>

Apache HBase\ 9 is the Hadop database. Use it when you ned

random, realtime read/write aces to your Big Data.

This project's goal is the hosting of very large tables- bilions of rows X milions of columnsatop clusters

of comodity hardware.

</description>

![image 2](<HBase连接池 -- HTablePool被Deprecated以及可能原因是什么 .note_images/imageFile2.png>)

关于释放HTable实例与释放连接的问题 HTable实例相关的两个连接，⼀个是对zookeeper,⼀个是regionServer 如果没有其他HTable实例 （在HTablePool尺⼨⼤于0的情况不可能出现这种情况），及没有zookeeper的 连接计数为0，此时才会释放zookeeper连接 regionServer的连接有HBaseClient$Connection这个线程单独维护，与HTable实例基本没啥关系，注意 HBaseClient$Connection这个线程绑定了连接 总体看HTablePool 容纳了多个HTable实例 多个HTable实例会共享同⼀个zookeeper连接 多个HTable实例，如果同在⼀个RegionServer会共享同⼀个连接HBaseClient$Connection 很容易让⼈误解每个HTable实例都有⼀个HBaseClient$Conection，就像连接池那样，其实不是 虽然HTablePool有最⼤尺⼨，但并没有限制HTable实例不得⼤于这个尺⼨，⼀旦超过这个尺⼨就会实例 化，但归还到实例池的时候，如果池满了会弃⽤，因此 HTablePool就是⼀个对象池⽽不是连接池

使⽤HTablePool的意义？ 《HBase-The-Definitive-Guide》 作者是这么说的 实例化HTable实例⽐较耗时，最好启动时初始化（这个理由不是很充分，完全可以使⽤HTable单例） HTable实例线程不安全，特别是在auto flash为false的情况，因为存在本地的write buffer ，即使 auto flash为true，也不建议使⽤（对此作者并没说为什么 ）建议每个线程⼀个HTable实例 HTablePool存在的问题 PooledHTable的代码很恶⼼，PooledHTable作为⼀个HTable的wrapper,两者的关系应该是包含，但源码 中却是继承 HTablePool并不是连接池，就是直接使⽤ HBaseClient$Connection【如果是同⼀个region的话就是单线 程】来完成⽹络通讯的，后在多个线程使⽤ 单个HBaseClient$Connection⽽带来同步和阻塞的问题

