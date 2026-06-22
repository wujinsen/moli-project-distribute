问题导读：

- 1.Flume的存在些什么问题？

- 2.基于开源的Flume美团增加了哪些功能？

- 3.Flume系统如何调优？


![image 1](<基于Flume的美团日志收集系统(二)改进和优化 .note_images/imageFile1.png>)

在《 》中，我们详述了基于Flume的美团⽇志收集 系统的架构设计，以及为什么做这样的设计。在本节中，我们将会讲述在实际部署和使⽤过程中 遇到的问题，对Flume的功能改进和对系统做的优化。

基于Flume的美团⽇志收集系统(⼀)架构和设计

- 1 Flume的问题总结

- 2 Flume的功能改进和优化点


在Flume的使⽤过程中，遇到的主要问题如下：

- a. Channel“⽔⼟不服”：使⽤固定⼤⼩的MemoryChannel在⽇志⾼峰时常报队列⼤⼩不够的异常； 使⽤FileChannel⼜导致IO繁忙的问题；

- b. HdfsSink的性能问题：使⽤HdfsSink向Hdfs写⽇志，在⾼峰时间速度较慢；

- c. 系统的管理问题：配置升级，模块重启等；


从上⾯的问题中可以看到，有⼀些需求是原⽣Flume⽆法满⾜的，因此，基于开源的Flume我们增 加了许多功能，修改了⼀些Bug，并且进⾏⼀些调优。下⾯将对⼀些主要的⽅⾯做⼀些说明。

- 2.1 增加Zabbix monitor服务


⼀⽅⾯，Flume本身提供了http, ganglia的监控服务，⽽我们⽬前主要使⽤zabbix做监控。因此， 我们为Flume添加了zabbix监控模块，和sa的监控服务⽆缝融合。

另⼀⽅⾯，净化Flume的metrics。只将我们需要的metrics发送给zabbix，避免 zabbix server造成 压⼒。⽬前我们最为关⼼的是Flume能否及时把应⽤端发送过来的⽇志写到Hdfs上， 对应关注的 metrics为：

Source : 接收的event数和处理的event数 Channel : Channel中拥堵的event数 Sink : 已经处理的event数

- 2.2 为HdfsSink增加⾃动创建index功能


⾸先，我们的HdfsSink写到hadoop的⽂件采⽤lzo压缩存储。 HdfsSink可以读取hadoop配置⽂件中 提供的编码类列表，然后通过配置的⽅式获取使⽤何种压缩编码，我们⽬前使⽤lzo压缩数据。采 ⽤lzo压缩⽽⾮bz2压缩，是基于以下测试数据：

<table>
  <tr>
    <th>event⼤⼩</th>
    <th>sink.batch<br><br>-size</th>
    <th>hdfs.batch Size</th>
    <th>压缩格式</th>
    <th>总数据⼤</th>
    <th>耗时(s)</th>
    <th>平均</th>
    <th>压缩后⼤</th>
  </tr>
  <tr>
    <td>(Byte) 54</td>
    <td>30</td>
    <td>1 0</td>
    <td>bz2</td>
    <td>⼩(G) 9.1</td>
    <td>248</td>
    <td>events/s 683</td>
    <td>⼩(G) 1.36</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


54 30 1 0 lzo 9.1 612 27 3 3.49

其次，我们的HdfsSink增加了创建lzo⽂件后⾃动创建index功能。Hadoop提供了对lzo创建索引， 使得压缩⽂件是可切分的，这样Hadoop Job可以并⾏处理数据⽂件。HdfsSink本身lzo压缩，但写 完lzo⽂件并不会建索引，我们在close⽂件之后添加了建索引功能。

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


/**

- * Rename bucketPath file from .tmp to permanent location.
- */


private void renameBucket() throws IOException, InterruptedException { if(bucketPath.equals(targetPath)) {

return; }

final Path srcPath = new Path(bucketPath); final Path dstPath = new Path(targetPath);

callWithTimeout(new CallRunner<Object>() { @Override public Object call() throws Exception {

if(fileSystem.exists(srcPath)) { // could block

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


LOG.info("Renaming " + srcPath + " to " + dstPath); fileSystem.rename(srcPath, dstPath); // could block

//index the dstPath lzo file if (codeC != null && ".lzo".equals(codeC.getDefaultExtension()) ) {

LzoIndexer lzoIndexer = new LzoIndexer(new Configuration()); lzoIndexer.index(dstPath);

}

} return null;

} });

}

复制代码

- 2.3 增加HdfsSink的开关

- 2.4 增加DualChannel


我们在HdfsSink和DualChannel中增加开关，当开关打开的情况下，HdfsSink不再往Hdfs上写数 据，并且数据只写向DualChannel中的FileChannel。以此策略来防⽌Hdfs的正常停机维护。

Flume本身提供了MemoryChannel和FileChannel。MemoryChannel处理速度快，但缓存⼤⼩有 限，且没有持久化；FileChannel则刚好相反。我们希望利⽤两者的优势，在Sink处理速度够快， Channel没有缓存过多⽇志的时候，就使⽤MemoryChannel，当Sink处理速度跟不上，⼜需要 Channel能够缓存下应⽤端发送过来的⽇志时，就使⽤FileChannel，由此我们开发了 DualChannel，能够智能的在两个Channel之间切换。

其具体的逻辑如下：

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
- 36.


/***

- * putToMemChannel indicate put event to memChannel or fileChannel

- * takeFromMemChannel indicate take event from memChannel or fileChannel

- * */ private AtomicBoolean putToMemChannel = new AtomicBoolean(true); private AtomicBoolean takeFromMemChannel = new AtomicBoolean(true);


void doPut(Event event) {

if (switchon && putToMemChannel.get()) { //往memChannel中写数据 memTransaction.put(event);

if ( memChannel.isFull() || fileChannel.getQueueSize() > 100) {

putToMemChannel.set(false); }

} else { //往fileChannel中写数据 fileTransaction.put(event);

} }

Event doTake() { Event event = null; if ( takeFromMemChannel.get() ) {

//从memChannel中取数据 event = memTransaction.take(); if (event == null) {

takeFromMemChannel.set(false); }

} else { //从fileChannel中取数据 event = fileTransaction.take(); if (event == null) {

takeFromMemChannel.set(true);

putToMemChannel.set(true);

- 37.
- 38.
- 39.
- 40.


}

} return event;

}

复制代码

- 2.5 增加NullChannel

- 2.6 增加KafkaSink


Flume提供了NullSink，可以把不需要的⽇志通过NullSink直接丢弃，不进⾏存储。然⽽，Source需 要先将events存放到Channel中，NullSink再将events取出扔掉。为了提升性能，我们把这⼀步移到 了Channel⾥⾯做，所以开发了NullChannel。

为⽀持向Storm提供实时数据流，我们增加了KafkaSink⽤来向Kafka写实时数据流。其基本的逻辑 如下：

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
- 36.
- 37.
- 38.
- 39.


public class KafkaSink extends AbstractSink implements Configurable { private String zkConnect; private Integer zkTimeout; private Integer batchSize; private Integer queueSize; private String serializerClass; private String producerType; private String topicPrefix;

private Producer<String, String> producer;

public void configure(Context context) {

//读取配置，并检查配置 }

@Override public synchronized void start() {

//初始化producer }

@Override public synchronized void stop() {

//关闭producer }

@Override public Status process() throws EventDeliveryException {

Status status = Status.READY;

Channel channel = getChannel(); Transaction tx = channel.getTransaction(); try {

tx.begin();

//将⽇志按category分队列存放 Map<String, List<String>> topic2EventList = new HashMap<String,

List<String>>();

//从channel中取batchSize⼤⼩的⽇志，从header中获取category，⽣成topic，并存放 于上述的Map中；

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


//将Map中的数据通过producer发送给kafka

tx.commit();

} catch (Exception e) { tx.rollback(); throw new EventDeliveryException(e);

} finally { tx.close();

} return status;

} }

复制代码

- 2.7 修复和scribe的兼容问题


Scribed在通过ScribeSource发送数据包给Flume时，⼤于4096字节的包，会先发送⼀个Dummy包 检查服务器的反应，⽽Flume的ScribeSource对于logentry.size()=0的包返回TRY_LATER，此时 Scribed就认为出错，断开连接。这样循环反复尝试，⽆法真正发送数据。现在在ScribeSource的 Thrift接⼝中，对size为0的情况返回OK，保证后续正常发送数据。

- 3. Flume系统调优经验总结3.1 基础参数调优经验


HdfsSink中默认的serializer会每写⼀⾏在⾏尾添加⼀个换⾏符，我们⽇志本身带有换⾏符，这 样会导致每条⽇志后⾯多⼀个空⾏，修改配置不要⾃动添加换⾏符；

1.

lc.sinks.sink_hdfs.serializer.appendNewline = false

复制代码

调⼤MemoryChannel的capacity，尽量利⽤MemoryChannel快速的处理能⼒；

调⼤HdfsSink的batchSize，增加吞吐量，减少hdfs的flush次数； 适当调⼤HdfsSink的callTimeout，避免不必要的超时错误；

- 3.2 HdfsSink获取Filename的优化

- 3.3 HdfsSink的b/m/s优化


HdfsSink的path参数指明了⽇志被写到Hdfs的位置，该参数中可以引⽤格式化的参数，将⽇志写到 ⼀个动态的⽬录中。这⽅便了⽇志的管理。例如我们可以将⽇志写到category分类的⽬录，并且按 天和按⼩时存放：

1. lc.sinks.sink_hdfs.hdfs.path = /user/hive/work/orglog.db/%{category}/dt=%Y%m%d/hour=%H

复制代码

HdfsS ink中处理每条event时，都要根据配置获取此event应该写⼊的Hdfs path和filename，默认 的获取⽅法是通过正则表达式替换配置中的变量，获取真实的path和filename。因为此过程是每条 event都要做的操作，耗时很⻓。通过我们的测试，20万条⽇志，这个操作要耗时6-8s左右。

由于我们⽬前的path和filename有固定的模式，可以通过字符串拼接获得。⽽后者⽐正则匹配快⼏ ⼗倍。拼接定符串的⽅式，20万条⽇志的操作只需要⼏百毫秒。

在我们初始的设计中，所有的⽇志都通过⼀个Channel和⼀个HdfsSink写到Hdfs上。我们来看⼀看 这样做有什么问题。

⾸先，我们来看⼀下HdfsSink在发送数据的逻辑：

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


//从Channel中取batchSize⼤⼩的events for (txnEventCount = 0; txnEventCount < batchSize; txnEventCount++) {

//对每条⽇志根据category append到相应的bucketWriter上； bucketWriter.append(event);

｝

for (BucketWriter bucketWriter : writers) { //然后对每⼀个bucketWriter调⽤相应的flush⽅法将数据flush到Hdfs上 bucketWriter.flush();

｝

复制代码

假设我们的系统中有100个category，batchSize⼤⼩设置为20万。则每20万条数据，就需要对100 个⽂件进⾏append或者flush操作。

其次，对于我们的⽇志来说，基本符合80/20原则。即20%的category产⽣了系统80%的⽇志量。 这样对⼤部分⽇志来说，每20万条可能只包含⼏条⽇志，也需要往Hdfs上flush⼀次。

上述的情况会导致HdfsSink写Hdfs的效率极差。下图是单Channel的情况下每⼩时的发送量和写 hdfs的时间趋势图。

![image 2](<基于Flume的美团日志收集系统(二)改进和优化 .note_images/imageFile2.png>)

# 鉴于这种实际应⽤场景，我们把⽇志进⾏了⼤⼩归类，分为big, middle和small三类，这样可以有 效的避免⼩⽇志跟着⼤⽇志⼀起频繁的flush，提升效果明显。下图是分队列后big队列的每⼩时的 发送量和写hdfs的时间趋势图。

![image 3](<基于Flume的美团日志收集系统(二)改进和优化 .note_images/imageFile3.png>)

