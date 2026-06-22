本节探讨⼀下storm具体怎么使⽤，明⽩怎么在windows下开发storm程序。 功能描述：实时随机输出⼀字符串。 在开发前记得导⼊storm需要的jar包。

- 1、SimpleSpout类继承BaseRichSpout类，⽤来产⽣数据并且向topology⾥⾯发出消息：tuple。


- 3 import java.util.Map;

- 4 import java.util.Random;

- 5

- 6 import backtype.storm.spout.SpoutOutputCollector;

- 7 import backtype.storm.task.TopologyContext;

- 8 import backtype.storm.topology.OutputFieldsDeclarer;

- 9 import backtype.storm.topology.base.BaseRichSpout;

- 10 import backtype.storm.tuple.Fields;

- 11 import backtype.storm.tuple.Values;

- 12

- 13 /**

* Spout起到和外界沟通的作⽤，他可以从⼀个数据库中按照某种规则取数据，也可以从分布式队列中取任 务

- 14

- 15 *

- 16 * @author Administrator

- 17 *

- 18 */

- 19 @SuppressWarnings("serial")

- 20 public class SimpleSpout extends BaseRichSpout{

- 21 //⽤来发射数据的⼯具类

- 22 private SpoutOutputCollector collector;

- 23 private static String[] info = new String[]{

- 24 "comaple\t,12424,44w46,654,12424,44w46,654,",

- 25 "lisi\t,435435,6537,12424,44w46,654,",

- 26 "lipeng\t,45735,6757,12424,44w46,654,",

- 27 "hujintao\t,45735,6757,12424,44w46,654,",

- 28 "jiangmin\t,23545,6457,2455,7576,qr44453",

- 29 "beijing\t,435435,6537,12424,44w46,654,",

- 30 "xiaoming\t,46654,8579,w3675,85877,077998,",

- 31 "xiaozhang\t,9789,788,97978,656,345235,09889,",

- 32 "ceo\t,46654,8579,w3675,85877,077998,",

- 33 "cto\t,46654,8579,w3675,85877,077998,",

- 34 "zhansan\t,46654,8579,w3675,85877,077998,"};

- 35

- 36 Random random=new Random();

- 37

- 38 /**

- 39 * 初始化collector


- 40 */

public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {

- 41

- 42 this.collector = collector;

- 43 }

- 44

- 45 /**

* 在SpoutTracker类中被调⽤，每调⽤⼀次就可以向storm集群中发射⼀条数据（⼀个tuple元 组），该⽅法会被不停的调⽤

- 46

- 47 */

- 48 @Override

- 49 public void nextTuple() {

- 50 try {

- 51 String msg = info[random.nextInt(11)];

- 52 // 调⽤发射⽅法

- 53 collector.emit(new Values(msg));

- 54 // 模拟等待100ms

- 55 Thread.sleep(100);

- 56 } catch (InterruptedException e) {

- 57 e.printStackTrace();

- 58 }

- 59 }

- 60

- 61 /**

- 62 * 定义字段id，该id在简单模式下没有⽤处，但在按照字段分组的模式下有很⼤的⽤处。

* 该declarer变量有很⼤作⽤，我们还可以调⽤declarer.declareStream();来定义 stramId，该id可以⽤来定义更加复杂的流拓扑结构

- 63

- 64 */

- 65 @Override

- 66 public void declareOutputFields(OutputFieldsDeclarer declarer) {

declarer.declare(new Fields("source")); //collector.emit(new Values(msg));参数要对应

- 67

- 68 }

- 69

- 70 }


- 2、SimpleBolt类继承BaseBasicBolt类，处理⼀个输⼊tuple。


- 3 import backtype.storm.topology.BasicOutputCollector;

- 4 import backtype.storm.topology.OutputFieldsDeclarer;

- 5 import backtype.storm.topology.base.BaseBasicBolt;

- 6 import backtype.storm.tuple.Fields;

- 7 import backtype.storm.tuple.Tuple;

- 8 import backtype.storm.tuple.Values;

- 9

- 10 /**

- 11 * 接收喷发节点(Spout)发送的数据进⾏简单的处理后，发射出去。

- 12 *

- 13 * @author Administrator

- 14 *

- 15 */

- 16 @SuppressWarnings("serial")

- 17 public class SimpleBolt extends BaseBasicBolt {

- 18

- 19 public void execute(Tuple input, BasicOutputCollector collector) {

- 20 try {

- 21 String msg = input.getString(0);

- 22 if (msg != null){

- 23 //System.out.println("msg="+msg);

- 24 collector.emit(new Values(msg + "msg is processed!"));

- 25 }

- 26

- 27 } catch (Exception e) {

- 28 e.printStackTrace();

- 29 }

- 30 }

- 31

- 32 public void declareOutputFields(OutputFieldsDeclarer declarer) {

- 33 declarer.declare(new Fields("info"));

- 34 }

- 35

- 36 }


# 3、SimpleTopology类包含⼀个main函数，是Storm程序执⾏的⼊⼝点，包括⼀个数据喷发节点spout 和⼀个数据处理节点bolt。

- 3 import backtype.storm.Config;

- 4 import backtype.storm.LocalCluster;

- 5 import backtype.storm.StormSubmitter;

- 6 import backtype.storm.topology.TopologyBuilder;

- 7

- 8 /**

- 9 * 定义了⼀个简单的topology，包括⼀个数据喷发节点spout和⼀个数据处理节点bolt。

- 10 *

- 11 * @author Administrator

- 12 *

- 13 */

- 14 public class SimpleTopology {

- 15 public static void main(String[] args) {

- 16 try {

- 17 // 实例化TopologyBuilder类。

- 18 TopologyBuilder topologyBuilder = new TopologyBuilder();

- 19 // 设置喷发节点并分配并发数，该并发数将会控制该对象在集群中的线程数。

- 20 topologyBuilder.setSpout("SimpleSpout", new SimpleSpout(), 1);

- 21 // 设置数据处理节点并分配并发数。指定该节点接收喷发节点的策略为随机⽅式。

topologyBuilder.setBolt("SimpleBolt", new SimpleBolt(), 3).shuffleGrouping("SimpleSpout");

- 22

- 23 Config config = new Config();

- 24 config.setDebug(true);

- 25 if (args != null && args.length > 0) {

- 26 config.setNumWorkers(1);

StormSubmitter.submitTopology(args[0], config, topologyBuilder.createTopology());

- 27

- 28 } else {

- 29 // 这⾥是本地模式下运⾏的启动代码。

- 30 config.setMaxTaskParallelism(1);

- 31 LocalCluster cluster = new LocalCluster();

cluster.submitTopology("simple", config, topologyBuilder.createTopology());

- 32

- 33 }

- 34

- 35 } catch (Exception e) {

- 36 e.printStackTrace();

- 37 }


- 38 }

- 39 }


