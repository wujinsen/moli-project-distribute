htps:/ w.cnblogs.com/javazhiyin/p/13597319.html

和其他所有的计算框架⼀样，flink也有⼀些基础的开发步骤以及基础，核⼼的API，从开发步骤的⻆度 来讲，主要分为四⼤部分

![image 1](<Flink从入门到入土（详细教程）.note_images/imageFile1.png>)

# 1.Environment

![image 2](<Flink从入门到入土（详细教程）.note_images/imageFile2.png>)

Flink Job在提交执⾏计算时，需要⾸先建⽴和Flink框架之间的联系，也就指的是当前的flink运⾏环 境，只有获取了环境信息，才能将task调度到不同的taskManager执⾏。⽽这个环境对象的获取⽅式相 对⽐较简单

// 批处理环境 val env = ExecutionEnvironment.getExecutionEnvironment // 流式数据处理环境 val env = StreamExecutionEnvironment.getExecutionEnvironment

# 2.Source

![image 3](<Flink从入门到入土（详细教程）.note_images/imageFile3.png>)

Flink框架可以从不同的来源获取数据，将数据提交给框架进⾏处理, 我们将获取数据的来源称之为数据 源.

- 2.1.从集合读取数据 ⼀般情况下，可以将数据临时存储到内存中，形成特殊的数据结构后，作为数据源使⽤。这⾥的数据 结构采⽤集合类型是⽐较普遍的


<table>
  <tr>
    <th>![image 4](<Flink从入门到入土（详细教程）.note_images/imageFile4.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0

*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：从集合读取数据

- */


object SourceList {

def main(args: Array[String]): Unit = { //1.创建执⾏的环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

- //2.从集合中读取数据 val sensorDS: DataStream[WaterSensor] = env.fromCollection(

// List(1,2,3,4,5) List(

- WaterSensor("ws_001", 1577844001, 45.0),

- WaterSensor("ws_002", 1577844015, 43.0),

- WaterSensor("ws_003", 1577844020, 42.0)


) )

- //3.打印 sensorDS.print()

- //4.执⾏ env.execute("sensor")


}

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号

- * @param ts 时间戳

- * @param vc 空⾼

- */


#### case class WaterSensor(id: String, ts: Long, vc: Double) }

![image 5](<Flink从入门到入土（详细教程）.note_images/imageFile5.png>)

## 2.2从⽂件中读取数据 通常情况下，我们会从存储介质中获取数据，⽐较常⻅的就是将⽇志⽂件作为数据源

<table>
  <tr>
    <th>![image 6](<Flink从入门到入土（详细教程）.note_images/imageFile6.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0

*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：从⽂件读取数据

- */


object SourceFile {

def main(args: Array[String]): Unit = {

- //1.创建执⾏的环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

- //2.从指定路径获取数据 val fileDS: DataStream[String] = env.readTextFile("input/data.log")

- //3.打印 fileDS.print()

- //4.执⾏ env.execute("sensor")


}

} /**

- * 在读取⽂件时，⽂件路径可以是⽬录也可以是单⼀⽂件。如果采⽤相对⽂件路径，会从当前系统参数user.dir中获取路径

- * System.getProperty("user.dir")

- */


/**

- * 如果在IDEA中执⾏代码，那么系统参数user.dir⾃动指向项⽬根⽬录，

- * 如果是standalone集群环境, 默认为集群节点根⽬录，当然除了相对路径以外，

- * 也可以将路径设置为分布式⽂件系统路径，如HDFS val fileDS: DataStream[String] = env.readTextFile( "hdfs://hadoop02:9000/test/1.txt")

- */


<table>
  <tr>
    <th>![image 7](<Flink从入门到入土（详细教程）.note_images/imageFile7.png>)</th>
  </tr>
</table>


![image 8](<Flink从入门到入土（详细教程）.note_images/imageFile8.png>)

如果是standalone集群环境, 默认为集群节点根⽬录，当然除了相对路径以外，也可以将路径设置为分 布式⽂件系统路径，如HDFS

val fileDS: DataStream[String] = env.readTextFile( "hdfs://hadoop02:9000/test/1.txt")

默认读取时，flink的依赖关系中是不包含Hadop依赖关系的，所以执⾏上⾯代码时，会出现错误。

![image 9](<Flink从入门到入土（详细教程）.note_images/imageFile9.png>)

解决⽅法就是增加相关依赖jar包就可以了

![image 10](<Flink从入门到入土（详细教程）.note_images/imageFile10.png>)

- 2.3 kafka读取数据 Kafka作为消息传输队列，是⼀个分布式的，⾼吞吐量，易于扩展地基于主题发布/订阅的消息系统。在 现今企业级开发中，Kafka 和 Flink成为构建⼀个实时的数据处理系统的⾸选


- 2.3.1 引⼊kafka连接器的依赖


<table>
  <tr>
    <th>![image 11](<Flink从入门到入土（详细教程）.note_images/imageFile11.png>)</th>
  </tr>
</table>


<!-- https://mvnrepository.com/artifact/org.apache.flink/flink-connector-kafka-0.11 --> <dependency>

<groupId>org.apache.flink</groupId> <artifactId>flink-connector-kafka-0.11_2.11</artifactId> <version>1.10.0</version>

</dependency>

<table>
  <tr>
    <th>![image 12](<Flink从入门到入土（详细教程）.note_images/imageFile12.png>)</th>
  </tr>
</table>


### 2.3.2 代码实现参考

<table>
  <tr>
    <th>![image 13](<Flink从入门到入土（详细教程）.note_images/imageFile13.png>)</th>
  </tr>
</table>


import java.util.Properties

import org.apache.flink.streaming.api.scala._ import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011 import org.apache.flink.streaming.util.serialization.SimpleStringSchema

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0

*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：从kafka读取数据

- */


object SourceKafka {

def main(args: Array[String]): Unit = { val env: StreamExecutionEnvironment =

StreamExecutionEnvironment.getExecutionEnvironment

val properties = new Properties() properties.setProperty("bootstrap.servers", "hadoop02:9092") properties.setProperty("group.id", "consumer-group") properties.setProperty("key.deserializer",

"org.apache.kafka.common.serialization.StringDeserializer") properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer") properties.setProperty("auto.offset.reset", "latest")

val kafkaDS: DataStream[String] = env.addSource(

new FlinkKafkaConsumer011[String]( "sensor", new SimpleStringSchema(), properties)

) kafkaDS.print() env.execute("sensor")

} }

<table>
  <tr>
    <th>![image 14](<Flink从入门到入土（详细教程）.note_images/imageFile14.png>)</th>
  </tr>
</table>


- 2.4 ⾃定义数据源 ⼤多数情况下，前⾯的数据源已经能够满⾜需要，但是难免会存在特殊情况的场合，所以flink也提供 了能⾃定义数据源的⽅式


- 2.4.1 创建⾃定义数据源


<table>
  <tr>
    <th>![image 15](<Flink从入门到入土（详细教程）.note_images/imageFile15.png>)</th>
  </tr>
</table>


import com.atyang.day01.Source.SourceList.WaterSensor import org.apache.flink.streaming.api.functions.source.SourceFunction

import scala.util.Random

/**

- * description: ss

- * date: 2020/8/28 20:36

- * version: 1.0

*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：⾃定义数据源

- */


class MySensorSource extends SourceFunction[WaterSensor] { var flg = true override def run(ctx: SourceFunction.SourceContext[WaterSensor]): Unit = {

while ( flg ) { // 采集数据 ctx.collect(

WaterSensor( "sensor_" +new Random().nextInt(3), 1577844001, new Random().nextInt(5)+40

)

) Thread.sleep(100)

} }

override def cancel(): Unit = {

flg = false; }

}

<table>
  <tr>
    <th>![image 16](<Flink从入门到入土（详细教程）.note_images/imageFile16.png>)</th>
  </tr>
</table>


![image 17](<Flink从入门到入土（详细教程）.note_images/imageFile17.png>)

# 3.Transform

![image 18](<Flink从入门到入土（详细教程）.note_images/imageFile18.png>)

在Spark中，算⼦分为转换算⼦和⾏动算⼦，转换算⼦的作⽤可以通过算⼦⽅法的调⽤将⼀个RD转换 另外⼀个RD，Flink中也存在同样的操作，可以将⼀个数据流转换为其他的数据流。 转换过程中，数据流的类型也会发⽣变化，那么到底Flink⽀持什么样的数据类型呢，其实我们常⽤的 数据类型，Flink都是⽀持的。⽐如：Long, String, Integer, Int, 元组，样例类，List, Map等。

- 3.1 map


映射：将数据流中的数据进⾏转换, 形成新的数据流，消费⼀个元素并产出⼀个元素

参数：Scala匿名函数或MapFunction

返回：DataStream

<table>
  <tr>
    <th>![image 19](<Flink从入门到入土（详细教程）.note_images/imageFile19.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：从集合读取数据

- */ object Transfrom_map {


def main(args: Array[String]): Unit = { //1.创建执⾏的环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

- //2.从集合中读取数据 val sensorDS: DataStream[WaterSensor] = env.fromCollection(

// List(1,2,3,4,5) List(

- WaterSensor("ws_001", 1577844001, 45.0),

- WaterSensor("ws_002", 1577844015, 43.0),

- WaterSensor("ws_003", 1577844020, 42.0)


) )

val sensorDSMap = sensorDS.map(x => (x.id+"_1",x.ts+"_1",x.vc + 1))

- //3.打印 sensorDSMap.print()

- //4.执⾏ env.execute("sensor")


}

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号

- * @param ts 时间戳

- * @param vc 空⾼

- */


case class WaterSensor(id: String, ts: Long, vc: Double)

}

<table>
  <tr>
    <th>![image 20](<Flink从入门到入土（详细教程）.note_images/imageFile20.png>)</th>
  </tr>
</table>


![image 21](<Flink从入门到入土（详细教程）.note_images/imageFile21.png>)

- 3.1.1 MapFunction Flink为每⼀个算⼦的参数都⾄少提供了Scala匿名函数和函数类两种的⽅式，其中如果使⽤函数类作为 参数的话，需要让⾃定义函数继承指定的⽗类或实现特定的接⼝。例如：MapFunction sensor-data.log ⽂件数据


<table>
  <tr>
    <th>![image 22](<Flink从入门到入土（详细教程）.note_images/imageFile22.png>)</th>
  </tr>
</table>


- sensor_1,1549044122,10

- sensor_1,1549044123,20

- sensor_1,1549044124,30


- sensor_2,1549044125,40

- sensor_1,1549044126,50

- sensor_2,1549044127,60 sensor_1,1549044128,70

- sensor_3,1549044129,80


- sensor_3,1549044130,90 sensor_3,1549044130,100 import org.apache.flink.streaming.api.scala._


/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0

*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：从⽂件读取数据

- */


object SourceFileMap {

def main(args: Array[String]): Unit = {

- //1.创建执⾏的环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

- //2.从指定路径获取数据 val fileDS: DataStream[String] = env.readTextFile("input/sensor-data.log")

val MapDS = fileDS.map(

lines => { //更加逗号切割 获取每个元素 val datas: Array[String] = lines.split(",") WaterSensor(datas(0), datas(1).toLong, datas(2).toInt)

} )

- //3.打印 MapDS.print()

- //4.执⾏ env.execute("map")


}

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号


- * @param ts 时间戳

- * @param vc 空⾼

- */


case class WaterSensor(id: String, ts: Long, vc: Double)

}

<table>
  <tr>
    <th>![image 23](<Flink从入门到入土（详细教程）.note_images/imageFile23.png>)</th>
  </tr>
</table>


![image 24](<Flink从入门到入土（详细教程）.note_images/imageFile24.png>)

<table>
  <tr>
    <th>![image 25](<Flink从入门到入土（详细教程）.note_images/imageFile25.png>)</th>
  </tr>
</table>


import org.apache.flink.api.common.functions.MapFunction import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0

*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：从⽂件读取数据

- */


object Transform_MapFunction {

def main(args: Array[String]): Unit = {

- //1.创建执⾏的环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

- //2.从指定路径获取数据 val sensorDS: DataStream[String] = env.readTextFile("input/sensor-data.log")

sensorDS.map()

- //3.打印

// MapDS.print()

- //4.执⾏ env.execute("map")


}

/**

- * ⾃定义继承 MapFunction

- * MapFunction[T,O]

- * ⾃定义输⼊和输出

*

- */


class MyMapFunction extends MapFunction[String,WaterSensor]{ override def map(t: String): WaterSensor = {

val datas: Array[String] = t.split(",")

WaterSensor(datas(0),datas(1).toLong,datas(2).toInt) }

}

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号


- * @param ts 时间戳

- * @param vc 空⾼

- */


case class WaterSensor(id: String, ts: Long, vc: Double)

}

<table>
  <tr>
    <th>![image 26](<Flink从入门到入土（详细教程）.note_images/imageFile26.png>)</th>
  </tr>
</table>


![image 27](<Flink从入门到入土（详细教程）.note_images/imageFile27.png>)

- 3.1.2 RichMapFunction 所有Flink函数类都有其Rich版本。它与常规函数的不同在于，可以获取运⾏环境的上下⽂，并拥有⼀ 些⽣命周期⽅法，所以可以实现更复杂的功能。也有意味着提供了更多的，更丰富的功能。例如： RichMapFunction sensor-data.log ⽂件数据 同上⼀致


<table>
  <tr>
    <th>![image 28](<Flink从入门到入土（详细教程）.note_images/imageFile28.png>)</th>
  </tr>
</table>


import org.apache.flink.api.common.functions.{MapFunction, RichMapFunction} import org.apache.flink.configuration.Configuration import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：从⽂件读取数据

- */ object Transform_RichMapFunction {


def main(args: Array[String]): Unit = {

- //1.创建执⾏的环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

- //2.从指定路径获取数据 val sensorDS: DataStream[String] = env.readTextFile("input/sensor-data.log")

val myMapDS: DataStream[WaterSensor] = sensorDS.map(new MyRichMapFunction)

- //3.打印 myMapDS.print()

- //4.执⾏ env.execute("map")


}

/**

- * ⾃定义继承 MapFunction

- * MapFunction[T,O]

- * ⾃定义输⼊和输出

*

- */


class MyRichMapFunction extends RichMapFunction[String,WaterSensor]{

override def map(value: String): WaterSensor = { val datas: Array[String] = value.split(",") // WaterSensor(datas(0), datas(1).toLong, datas(2).toInt) WaterSensor(getRuntimeContext.getTaskName, datas(1).toLong, datas(2).toInt)

}

// 富函数提供了⽣命周期⽅法 override def open(parameters: Configuration): Unit = {}

override def close(): Unit = {}

}

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号

- * @param ts 时间戳

- * @param vc 空⾼

- */


case class WaterSensor(id: String, ts: Long, vc: Double)

}

<table>
  <tr>
    <th>![image 29](<Flink从入门到入土（详细教程）.note_images/imageFile29.png>)</th>
  </tr>
</table>


Rich Function有⼀个⽣命周期的概念。典型的⽣命周期⽅法有：

open()⽅法是rich function的初始化⽅法，当⼀个算⼦例如map或者filter被调 ⽤之前open()会被调 ⽤

close()⽅法是⽣命周期中的最后⼀个调⽤的⽅法，做⼀些清理⼯作

getRuntimeContext()⽅法提供了函数的RuntimeContext的⼀些信息，例如函数执⾏ 的并⾏ 度，任务的名字，以及state状态

- 3.1.3 flatMap


扁平映射：将数据流中的整体拆分成⼀个⼀个的个体使⽤，消费⼀个元素并产⽣零到多个元素

参数：Scala匿名函数或FlatMapFunction

返回：DataStream

![image 30](<Flink从入门到入土（详细教程）.note_images/imageFile30.png>)

<table>
  <tr>
    <th>![image 31](<Flink从入门到入土（详细教程）.note_images/imageFile31.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：FlatMap

- */ object Transform_FlatMap {


def main(args: Array[String]): Unit = {

- // 1.创建执⾏环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment env.setParallelism(1)

- // 2.读取数据 val listDS: DataStream[List[Int]] = env.fromCollection(


List( List(1, 2, 3, 4), List(5, 6, 7,1,1,1)

) )

val resultDS: DataStream[Int] = listDS.flatMap(list => list)

resultDS.print()

// 4. 执⾏ env.execute()

}

}

<table>
  <tr>
    <th>![image 32](<Flink从入门到入土（详细教程）.note_images/imageFile32.png>)</th>
  </tr>
</table>


![image 33](<Flink从入门到入土（详细教程）.note_images/imageFile33.png>)

## 3.2. filter

过滤：根据指定的规则将满⾜条件（true）的数据保留，不满⾜条件(false)的数据丢弃

参数：Scala匿名函数或FilterFunction

返回：DataStream

<table>
  <tr>
    <th>![image 34](<Flink从入门到入土（详细教程）.note_images/imageFile34.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：Filter

- */ object Transform_Filter {


def main(args: Array[String]): Unit = {

- // 1.创建执⾏环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment env.setParallelism(1)

- // 2.读取数据 val listDS: DataStream[List[Int]] = env.fromCollection(


List( List(1, 2, 3, 4,1, 2, 3, 4), List(5, 6, 7,1,1,1,1, 2, 3, 4,1, 2, 3, 4), List(1, 2, 3, 4), List(5, 6, 7,1,1,1), List(1, 2, 3, 4), List(5, 6, 7,1,1,1)

)

) // true就留下，false就抛弃 listDS.filter(num => {

num.size>5 })

.print("filter") // 4. 执⾏ env.execute()

} }

<table>
  <tr>
    <th>![image 35](<Flink从入门到入土（详细教程）.note_images/imageFile35.png>)</th>
  </tr>
</table>


![image 36](<Flink从入门到入土（详细教程）.note_images/imageFile36.png>)

- 3.3 keyBy 在Spark中有⼀个GroupBy的算⼦，⽤于根据指定的规则将数据进⾏分组，在flink中也有类似的功能， 那就是keyBy，根据指定的key对数据进⾏分流


分流：根据指定的Key将元素发送到不同的分区，相同的Key会被分到⼀个分区（这⾥分区指的就是 下游算⼦多个并⾏节点的其中⼀个）。keyBy()是通过哈希来分区的

![image 37](<Flink从入门到入土（详细教程）.note_images/imageFile37.png>)

参数：Scala匿名函数或POJO属性或元组索引，不能使⽤数组

返回：KeyedStream

![image 38](<Flink从入门到入土（详细教程）.note_images/imageFile38.png>)

<table>
  <tr>
    <th>![image 39](<Flink从入门到入土（详细教程）.note_images/imageFile39.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：FlatMap

- */ object Transform_KeyBy {


def main(args: Array[String]): Unit = {

- // 1.创建执⾏环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment env.setParallelism(1)

- // 2.读取数据 val sensorDS: DataStream[String] = env.readTextFile("input/sensor-data.log")

- //3.转换为样例类 val mapDS = sensorDS.map(


lines => { val datas = lines.split(",") WaterSensor(datas(0), datas(1).toLong, datas(2).toInt)

} )

// 4. 使⽤keyby进⾏分组 // TODO 关于返回的key的类型：

- // 1. 如果是位置索引 或 字段名称 ，程序⽆法推断出key的类型，所以给⼀个java的Tuple类型

- // 2. 如果是匿名函数 或 函数类 的⽅式，可以推断出key的类型，⽐较推荐使⽤ // *** 分组的概念：分组只是逻辑上进⾏分组,打上了记号(标签)，跟并⾏度没有绝对的关系 // 同⼀个分组的数据在⼀起（不离不弃） // 同⼀个分区⾥可以有多个不同的组


// val sensorKS: KeyedStream[WaterSensor, Tuple] = mapDS.keyBy(0) // val sensorKS: KeyedStream[WaterSensor, Tuple] = mapDS.keyBy("id") val sensorKS: KeyedStream[WaterSensor, String] = mapDS.keyBy(_.id) // val sensorKS: KeyedStream[WaterSensor, String] = mapDS.keyBy( // new KeySelector[WaterSensor, String] { // override def getKey(value: WaterSensor): String = { // value.id // } // } // )

sensorKS.print().setParallelism(5)

// 4. 执⾏ env.execute()

}

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号

- * @param ts 时间戳

- * @param vc 空⾼

- */


case class WaterSensor(id: String, ts: Long, vc: Double) }

<table>
  <tr>
    <th>![image 40](<Flink从入门到入土（详细教程）.note_images/imageFile40.png>)</th>
  </tr>
</table>


![image 41](<Flink从入门到入土（详细教程）.note_images/imageFile41.png>)

## 3.4 shufle

打乱重组（洗牌）：将数据按照均匀分布打散到下游

参数：⽆

返回：DataStream

![image 42](<Flink从入门到入土（详细教程）.note_images/imageFile42.png>)

<table>
  <tr>
    <th>![image 43](<Flink从入门到入土（详细教程）.note_images/imageFile43.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：FlatMap

- */ object Transform_Shuffle {


def main(args: Array[String]): Unit = {

- // 1.创建执⾏环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment env.setParallelism(1)

- // 2.读取数据 val sensorDS: DataStream[String] = env.readTextFile("input/sensor-data.log")


val shuffleDS = sensorDS.shuffle

sensorDS.print("data")

shuffleDS.print("shuffle") // 4. 执⾏ env.execute()

} }

<table>
  <tr>
    <th>![image 44](<Flink从入门到入土（详细教程）.note_images/imageFile44.png>)</th>
  </tr>
</table>


![image 45](<Flink从入门到入土（详细教程）.note_images/imageFile45.png>)

- 3.5. split 在某些情况下，我们需要将数据流根据某些特征拆分成两个或者多个数据流，给不同数据流增加标记 以便于从流中取出。


![image 46](<Flink从入门到入土（详细教程）.note_images/imageFile46.png>)

需求：将⽔位传感器数据按照空⾼⾼低（以40cm,30cm为界），拆分成三个流

<table>
  <tr>
    <th>![image 47](<Flink从入门到入土（详细教程）.note_images/imageFile47.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：FlatMap

- */ object Transform_Split {


def main(args: Array[String]): Unit = {

- // 1.创建执⾏环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment env.setParallelism(1)

- // 2.读取数据 val sensorDS: DataStream[String] = env.readTextFile("input/sensor-data.log")

- // 3.转换成样例类 val mapDS: DataStream[WaterSensor] = sensorDS.map(

lines => { val datas: Array[String] = lines.split(",") WaterSensor(datas(0), datas(1).toLong, datas(2).toInt)

}

) val splitSS: SplitStream[WaterSensor] = mapDS.split(

sensor => { if (sensor.vc < 40) { Seq("normal") } else if (sensor.vc < 80) {

Seq("Warn") } else {

Seq("alarm") }

} )

- // 4. 执⾏ env.execute()


}

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号


- * @param ts 时间戳

- * @param vc 空⾼

- */


case class WaterSensor(id: String, ts: Long, vc: Double) }

<table>
  <tr>
    <th>![image 48](<Flink从入门到入土（详细教程）.note_images/imageFile48.png>)</th>
  </tr>
</table>


## 3.6 select 将数据流进⾏切分后，如何从流中将不同的标记取出呢，这时就需要使⽤select算⼦了。

![image 49](<Flink从入门到入土（详细教程）.note_images/imageFile49.png>)

<table>
  <tr>
    <th>![image 50](<Flink从入门到入土（详细教程）.note_images/imageFile50.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：FlatMap

- */ object Transform_Split {


def main(args: Array[String]): Unit = {

- // 1.创建执⾏环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment env.setParallelism(1)

- // 2.读取数据 val sensorDS: DataStream[String] = env.readTextFile("input/sensor-data.log")

- // 3.转换成样例类 val mapDS: DataStream[WaterSensor] = sensorDS.map(

lines => { val datas: Array[String] = lines.split(",") WaterSensor(datas(0), datas(1).toLong, datas(2).toInt)

}

) val splitDS: SplitStream[WaterSensor] = mapDS.split(

sensor => { if (sensor.vc < 40) { Seq("info") } else if (sensor.vc < 80) {

Seq("warn") } else {

Seq("error") }

}

) val errorDS: DataStream[WaterSensor] = splitDS.select("error") val warnDS: DataStream[WaterSensor] = splitDS.select("warn") val infoDS: DataStream[WaterSensor] = splitDS.select("info")

infoDS.print("info") warnDS.print("warn") errorDS.print("error")

- // 4. 执⾏


env.execute() }

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号

- * @param ts 时间戳

- * @param vc 空⾼

- */


case class WaterSensor(id: String, ts: Long, vc: Double) }

<table>
  <tr>
    <th>![image 51](<Flink从入门到入土（详细教程）.note_images/imageFile51.png>)</th>
  </tr>
</table>


![image 52](<Flink从入门到入土（详细教程）.note_images/imageFile52.png>)

- 3.7 conect 在某些情况下，我们需要将两个不同来源的数据流进⾏连接，实现数据匹配，⽐如订单⽀付和第三⽅ 交易信息，这两个信息的数据就来⾃于不同数据源，连接后，将订单⽀付和第三⽅交易信息进⾏对 账，此时，才能算真正的⽀付完成。 Flink中的conect算⼦可以连接两个保持他们类型的数据流，两个数据流被Conect之后，只是被放在 了⼀个同⼀个流中，内部依然保持各⾃的数据和形式不发⽣任何变化，两个流相互独⽴。


![image 53](<Flink从入门到入土（详细教程）.note_images/imageFile53.png>)

<table>
  <tr>
    <th>![image 54](<Flink从入门到入土（详细教程）.note_images/imageFile54.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：FlatMap

- */ object Transform_Connect {


def main(args: Array[String]): Unit = {

- // 1.创建执⾏环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment env.setParallelism(1)

- // 2.读取数据 val sensorDS: DataStream[String] = env.readTextFile("input/sensor-data.log")

- // 3.转换成样例类 val mapDS: DataStream[WaterSensor] = sensorDS.map(

lines => { val datas: Array[String] = lines.split(",") WaterSensor(datas(0), datas(1).toLong, datas(2).toInt)

} )

- // 4. 从集合中再读取⼀条流 val numDS: DataStream[Int] = env.fromCollection(List(1, 2, 3, 4, 5, 6))


val resultCS: ConnectedStreams[WaterSensor, Int] = mapDS.connect(numDS)

// coMap表示连接流调⽤的map，各⾃都需要⼀个 function resultCS.map(

sensor=>sensor.id, num=>num+1

).print()

// 4. 执⾏ env.execute()

}

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号


- * @param ts 时间戳

- * @param vc 空⾼

- */


case class WaterSensor(id: String, ts: Long, vc: Double) }

<table>
  <tr>
    <th>![image 55](<Flink从入门到入土（详细教程）.note_images/imageFile55.png>)</th>
  </tr>
</table>


![image 56](<Flink从入门到入土（详细教程）.note_images/imageFile56.png>)

- 3.8 union 对两个或者两个以上的DataStream进⾏union操作，产⽣⼀个包含所有DataStream元素的新 DataStream


![image 57](<Flink从入门到入土（详细教程）.note_images/imageFile57.png>)

conect与 union 区别： union之前两个流的类型必须是⼀样，conect可以不⼀样 conect只能操作两个流，union可以操作多个。

- 1.
- 2.


<table>
  <tr>
    <th>![image 58](<Flink从入门到入土（详细教程）.note_images/imageFile58.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：FlatMap

- */ object Transform_Union {


def main(args: Array[String]): Unit = {

- // 1.创建执⾏环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment env.setParallelism(1)

- // 2. 从集合中读取流 val num1DS: DataStream[Int] = env.fromCollection(List(1, 2, 3, 4)) val num2DS: DataStream[Int] = env.fromCollection(List(7, 8, 9, 10)) val num3DS: DataStream[Int] = env.fromCollection(List(17, 18, 19, 110))


// TODO union 真正将多条流合并成⼀条流 // 合并的流，类型必须⼀致 // 可以合并多条流，只要类型⼀致 num1DS.union(num2DS).union(num3DS)

.print()

// 4. 执⾏ env.execute()

}

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号

- * @param ts 时间戳

- * @param vc 空⾼

- */


case class WaterSensor(id: String, ts: Long, vc: Double) }

<table>
  <tr>
    <th>![image 59](<Flink从入门到入土（详细教程）.note_images/imageFile59.png>)</th>
  </tr>
</table>


![image 60](<Flink从入门到入土（详细教程）.note_images/imageFile60.png>)

- 3.9 Operator Flink作为计算框架，主要应⽤于数据计算处理上， 所以在keyBy对数据进⾏分流后，可以对数据进⾏ 相应的统计分析


- 3.9.1 滚动聚合算⼦（Roling Agregation） 这些算⼦可以针对KeyedStream的每⼀个⽀流做聚合。执⾏完成后，会将聚合的结果合成⼀个流返 回，所以结果都是DataStream sum()


![image 61](<Flink从入门到入土（详细教程）.note_images/imageFile61.png>)

min()

![image 62](<Flink从入门到入土（详细教程）.note_images/imageFile62.png>)

max()

![image 63](<Flink从入门到入土（详细教程）.note_images/imageFile63.png>)

### 3.9.2 reduce ⼀个分组数据流的聚合操作，合并当前的元素和上次聚合的结果，产⽣⼀个新的值，返回的流中包含 每⼀次聚合的结果，⽽不是只返回最后⼀次聚合的最终结果。

![image 64](<Flink从入门到入土（详细教程）.note_images/imageFile64.png>)

<table>
  <tr>
    <th>![image 65](<Flink从入门到入土（详细教程）.note_images/imageFile65.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.scala._

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：Reduce

- */ object Transform_Reduce {


def main(args: Array[String]): Unit = {

- // 1.创建执⾏环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment env.setParallelism(1)

- // 2.读取数据 val sensorDS: DataStream[String] = env.readTextFile("input/sensor-data.log")

- // 3.转换成样例类 val mapDS: DataStream[WaterSensor] = sensorDS.map(

lines => { val datas: Array[String] = lines.split(",") WaterSensor(datas(0), datas(1).toLong, datas(2).toInt)

}

) val sensorKS: KeyedStream[WaterSensor, String] = mapDS.keyBy(_.id) // 输⼊的类型⼀样，输出类型和输出类型也要⼀样 // 组内的第⼀条数据，不进⼊reduce计算 val reduceDS: DataStream[WaterSensor] = sensorKS.reduce(

(ws1, ws2) => { println(ws1 + "<===>" + ws2) WaterSensor(ws1.id, System.currentTimeMillis(), ws1.vc + ws2.vc)

}

) reduceDS.print("reduce")

- // 4. 执⾏ env.execute()


}

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号

- * @param ts 时间戳

- * @param vc 空⾼


- */


case class WaterSensor(id: String, ts: Long, vc: Double) }

<table>
  <tr>
    <th>![image 66](<Flink从入门到入土（详细教程）.note_images/imageFile66.png>)</th>
  </tr>
</table>


![image 67](<Flink从入门到入土（详细教程）.note_images/imageFile67.png>)

## 3.9.3proces

Flink在数据流通过keyBy进⾏分流处理后，如果想要处理过程中获取环境相关信息，可以采⽤proces 算⼦⾃定义实现 1)继承KeyedProcesFunction抽象类，并定义泛型：[KEY, IN, OUT]

<table>
  <tr>
    <th>![image 68](<Flink从入门到入土（详细教程）.note_images/imageFile68.png>)</th>
  </tr>
</table>


class MyKeyedProcessFunction extends KeyedProcessFunction[String, WaterSensor, String]{} 重写⽅法 // ⾃定义KeyedProcessFunction,是⼀个特殊的富函数

- // 1.实现KeyedProcessFunction，指定泛型：K - key的类型， I - 上游数据的类型， O - 输出的数据类型

- // 2.重写 processElement⽅法，定义 每条数据来的时候 的 处理逻辑


/**

- * 处理逻辑：来⼀条处理⼀条

*

- * @param value ⼀条数据

- * @param ctx 上下⽂对象

- * @param out 采集器：收集数据，并输出

- */


override def processElement(value: WaterSensor, ctx: KeyedProcessFunction[String, WaterSensor,

String]#Context, out: Collector[String]): Unit = { out.collect("我来到process啦，分组的key是="+ctx.getCurrentKey+",数据=" + value) // 如果key是tuple，即keyby的时候，使⽤的是 位置索引 或 字段名称，那么key获取到是⼀个tuple

// ctx.getCurrentKey.asInstanceOf[Tuple1].f0 //Tuple1需要⼿动引⼊Java的Tuple }

<table>
  <tr>
    <th>![image 69](<Flink从入门到入土（详细教程）.note_images/imageFile69.png>)</th>
  </tr>
</table>


### 完整代码：

<table>
  <tr>
    <th>![image 70](<Flink从入门到入土（详细教程）.note_images/imageFile70.png>)</th>
  </tr>
</table>


import org.apache.flink.streaming.api.functions.KeyedProcessFunction import org.apache.flink.streaming.api.scala._ import org.apache.flink.util.Collector

/**

- * description: SourceList

- * date: 2020/8/28 19:02

- * version: 1.0


*

- * @author 阳斌

- * 邮箱：1692207904@qq.com

- * 类的说明：Reduce

- */ object Transform_Process {


def main(args: Array[String]): Unit = {

- // 1.创建执⾏环境 val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment env.setParallelism(1)

- // 2.读取数据 val sensorDS: DataStream[String] = env.readTextFile("input/sensor-data.log")

- // 3.转换成样例类 val mapDS: DataStream[WaterSensor] = sensorDS.map(

lines => { val datas: Array[String] = lines.split(",") WaterSensor(datas(0), datas(1).toLong, datas(2).toInt)

}

) //按照ID 进⾏分组 val sensorKS: KeyedStream[WaterSensor, String] = mapDS.keyBy(_.id)

sensorKS.process(new MyKeyedProcessFunction)

- // 4. 执⾏ env.execute()


}

// ⾃定义KeyedProcessFunction,是⼀个特殊的富函数

- // 1.实现KeyedProcessFunction，指定泛型：K - key的类型， I - 上游数据的类型， O - 输出的数据类型

- // 2.重写 processElement⽅法，定义 每条数据来的时候 的 处理逻辑 class MyKeyedProcessFunction extends KeyedProcessFunction[String, WaterSensor, String] {


/**

- * 处理逻辑：来⼀条处理⼀条

*

- * @param value ⼀条数据

- * @param ctx 上下⽂对象


- * @param out 采集器：收集数据，并输出

- */


override def processElement(value: WaterSensor, ctx: KeyedProcessFunction[String, WaterSensor, String]#Context, out: Collector[String]): Unit = {

out.collect("我来到process啦，分组的key是="+ctx.getCurrentKey+",数据=" + value)

// 如果key是tuple，即keyby的时候，使⽤的是 位置索引 或 字段名称，那么key获取到是⼀个tuple // ctx.getCurrentKey.asInstanceOf[Tuple1].f0 //Tuple1需要⼿动引⼊Java的Tuple

} }

/**

- * 定义样例类：⽔位传感器：⽤于接收空⾼数据

*

- * @param id 传感器编号

- * @param ts 时间戳

- * @param vc 空⾼

- */


case class WaterSensor(id: String, ts: Long, vc: Double) }

<table>
  <tr>
    <th>![image 71](<Flink从入门到入土（详细教程）.note_images/imageFile71.png>)</th>
  </tr>
</table>


# 4.Sink

![image 72](<Flink从入门到入土（详细教程）.note_images/imageFile72.png>)

Sink有下沉的意思，在Flink中所谓的Sink其实可以表示为将数据存储起来的意思，也可以将范围扩 ⼤，表示将处理完的数据发送到指定的存储系统的输出操作 之前我们⼀直在使⽤的print⽅法其实就是⼀种Sink。

@PublicEvolving

public DataStreamSink<T> print(String sinkIdentifier) { PrintSinkFunction<T> printFunction = new PrintSinkFunction(sinkIdentifier, false); return this.addSink(printFunction).name("Print to Std. Out");

}

官⽅提供了⼀部分的框架的sink。除此以外，需要⽤户⾃定义实现sink

![image 73](<Flink从入门到入土（详细教程）.note_images/imageFile73.png>)

![image 74](<Flink从入门到入土（详细教程）.note_images/imageFile74.png>)

