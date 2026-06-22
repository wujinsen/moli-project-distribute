Kafka结构说明

![image 1](<第六章 kafka增强.note_images/imageFile1.png>)

Kafka名词解释和⼯作⽅式

Producer ：消息⽣产者，就是向kafka broker发消息的客户端。

Consumer ：消息消费者，向kafka broker取消息的客户端

Topic ：咋们可以理解为⼀个队列。

Consumer Group （CG）：这是kafka⽤来实现⼀个topic消息的⼴播（发给所有的consumer）和 单播（发给任意⼀个consumer）的⼿段。⼀个topic可以有多个CG。topic的消息会复制（不是真的 复制，是概念上的）到所有的CG，但每个CG只会把消息发给该CG中的⼀个consumer。如果需要 实现⼴播，只要每个consumer有⼀个独⽴的CG就可以了。要实现单播只要所有的consumer在同⼀ 个CG。⽤CG还可以将consumer进⾏⾃由的分组⽽不需要多次发送消息到不同的topic。

Broker ：⼀台kafka服务器就是⼀个broker。⼀个集群由多个broker组成。⼀个broker可以容纳多个 topic。

Partition：为了实现扩展性，⼀个⾮常⼤的topic可以分布到多个broker（即服务器）上，⼀个topic 可以分为多个partition，每个partition是⼀个有序的队列。partition中的每条消息都会被分配⼀个有 序的id（ofset）。kafka只保证按⼀个partition中的顺序将消息发给consumer，不保证⼀个topic的 整体（多个partition间）的顺序。

Ofset：kafka的存储⽂件都是按照ofset.kafka来命名，⽤ofset做名字的好处是⽅便查找。例如你 想找位于2049的位置，只要找到2048.kafka的⽂件即可。当然the first ofset就是

0.kafka

### Consumer与topic关系

本质上kafka只⽀持Topic； 每个group中可以有多个consumer，每个consumer属于⼀个consumer group；

通常情况下，⼀个group中会包含多个consumer，这样不仅可以提⾼topic中消息的并发消费能 ⼒，⽽且还能提⾼"故障容错"性，如果group中的某个consumer失效那么其消费的partitions将会 有其他consumer⾃动接管。

对于Topic中的⼀条特定的消息，只会被订阅此Topic的每个group中的其中⼀个consumer消费，此 消息不会发送给⼀个group的多个consumer；

那么⼀个group中所有的consumer将会交错的消费整个Topic，每个group中consumer消息消费互 相独⽴，我们可以认为⼀个group是⼀个"订阅"者。

在kafka中,⼀个partition中的消息只会被group中的⼀个consumer消费(同⼀时刻)；

⼀个Topic中的每个partions，只会被⼀个"订阅者"中的⼀个consumer消费，不过⼀个consumer可 以同时消费多个partitions中的消息。 kafka的设计原理决定,对于⼀个topic，同⼀个group中不能有多于partitions个数的consumer同时 消费，否则将意味着某些consumer将⽆法得到消息。

kafka只能保证⼀个partition中的消息被某个consumer消费时是顺序的；事实上，从Topic⻆度来说, 当有多个partitions时,消息仍不是全局有序的。

Producer客户端负责消息的分发

kafka集群中的任何⼀个broker都可以向producer提供metadata信息,这些metadata中包含"集群中 存活的servers列表"/"partitions leader列表"等信息；

当producer获取到metadata信⼼之后, producer将会和Topic下所有partition leader保持socket连 接；

消息由producer直接通过socket发送到broker，中间不会经过任何"路由层"，事实上，消息被路由 到哪个partition上由producer客户端决定；

⽐如可以采⽤"random"key-hash"轮询"等,如果⼀个topic中有多个partitions,那么在producer端 实现"消息均衡分发"是必要的。

在producer端的配置⽂件中,开发者可以指定partition路由的⽅式。

Consumer的负载均衡

当⼀个group中,有consumer加⼊或者离开时,会触发partitions均衡.均衡的最终⽬的,是提升topic的并发 消费能⼒，步骤如下：

- 1.
- 2.


假如topic1,具有如下partitions: P0,P1,P2,P3 加⼊group中,有如下consumer: C0,C1

- 3.
- 4.
- 5.
- 6.


⾸先根据partition索引号对partitions排序: P0,P1,P2,P3 根据consumer.id排序: C0,C1 计算倍数: M = [P0,P1,P2,P3].size / [C0,C1].size,本例值M=2(向上取整) 然后依次分配partitions: C0 = [P0,P1],C1=[P2,P3],即Ci = [P(i * M),P(i + 1) * M -1)]

![image 2](<第六章 kafka增强.note_images/imageFile2.png>)

## kafka客户端访问流程

![image 3](<第六章 kafka增强.note_images/imageFile3.png>)

上图中客户端访问流程主要分为三步：

- 1.
- 2.
- 3.


当建⽴连接请求时，⾸先客户端向kafka broker发送连接请求，broker中由Aceptor thread线程接 收并建⽴连接后，把client的socket以轮询⽅式转交给相应的procesor thread； 当client向broker发送数据请求，由procesor thread处理并接收client数据放到request缓冲区 中，以待IO thread进⾏逻辑处理和计算并把返回result放到response缓冲区中； 接着唤醒procesor thread，procesor thread抱住response队列循环发送所有response数据给 client；

# kafka⽂件存储-topic中partition存储分布

创建2个topic名称分别为report_push、launch_info。 partitions数量都为partitions=4

存储路径和⽬录规则为：

x/mesage-folder

- |-report_push-0

- |-report_push-1

- |-report_push-2

- |-report_push-3


- |-launch_info-0

- |-launch_info-1

- |-launch_info-2

- |-launch_info-3


在Kafka⽂件存储中，同⼀个topic下有多个不同partition，每个partition为⼀个⽬录，partiton命名规 则为topic名称+有序序号，第⼀个partiton序号从0开始，序号最⼤值为partitions数量减1。

## kafka⽂件存储-partiton中⽂件存储⽅式

下⾯示意图形象说明了partition中⽂件存储⽅式：

![image 4](<第六章 kafka增强.note_images/imageFile4.png>)

每个partion(⽬录)相当于⼀个巨型⽂件被平均分配到多个⼤⼩相等segment(段)数据⽂件中。但每 个段segment file消息数量不⼀定相等，这种特性⽅便old segment file快速被删除。

每个partiton只需要⽀持顺序读写就⾏了，segment⽂件⽣命周期由服务端配置参数决定。

这样做的好处就是能快速删除⽆⽤⽂件，有效提⾼磁盘利⽤率。

## kafka⽂件存储-partiton中segment⽂件存储结构

segment file组成：由2⼤部分组成，分别为index file和data file，此2个⽂件⼀⼀对应，成对出现，后 缀".index"和“.log”分别表示为segment索引⽂件、数据⽂件. segment⽂件命名规则：partion全局的第⼀个segment从0开始，后续每个segment⽂件名为上⼀个 segment⽂件最后⼀条消息的ofset值。数值最⼤为64位long⼤⼩，19位数字字符⻓度，没有数字⽤0 填充。

![image 5](<第六章 kafka增强.note_images/imageFile5.png>)

以上述图2中⼀对segment file⽂件为例，说明segment中index<—->data file对应关系物理结构如下：

![image 6](<第六章 kafka增强.note_images/imageFile6.png>)

上述图3中索引⽂件存储⼤量元数据，数据⽂件存储⼤量消息，索引⽂件中元数据指向对应数据⽂ 件中mesage的物理偏移地址。

其中以索引⽂件中元数据3,497为例，依次在数据⽂件中表示第3个mesage(在全局partiton表示 第36872个mesage)、以及该消息的物理偏移地址为497。

从上述图3了解到segment data file由许多mesage组成，下⾯详细说明mesage物理结构如下：

![image 7](<第六章 kafka增强.note_images/imageFile7.png>)

<table>
  <tr>
    <th>关键字</th>
    <th>解释说明</th>
  </tr>
  <tr>
    <td>8 byte ofset</td>
    <td>在parition(分区)内的每条消息都有⼀个有序的id 号，这个id号被称为偏移(ofset),它可以唯⼀确定 每条消息在parition(分区)内的位置。即ofset表</td>
  </tr>
  <tr>
    <td>4 byte mesage size</td>
    <td>示partion的第多少mesage mesage⼤⼩</td>
  </tr>
  <tr>
    <td>4 byte CRC32</td>
    <td>⽤crc32校验mesage</td>
  </tr>
  <tr>
    <td>1 byte “magic"</td>
    <td>表示本次发布Kafka服务程序协议版本号</td>
  </tr>
  <tr>
    <td>1 byte “atributes"</td>
    <td>表示为独⽴版本、或标识压缩类型、或编码类 型。</td>
  </tr>
  <tr>
    <td>4 byte key length</td>
    <td>表示key的⻓度,当key为-1时，K byte key字段不 填</td>
  </tr>
  <tr>
    <td>K byte key</td>
    <td>可选</td>
  </tr>
  <tr>
    <td> </td>
    <td>表示实际消息数据。</td>
  </tr>
</table>


value bytes payload

# kafka⽂件存储-在partition中如何通过offset查找message

例如读取ofset=36876的mesage，需要通过下⾯2个步骤查找。

第⼀步查找segment file 上述图2为例，其中 0.index表示最开始的⽂件，起始偏移量(ofset)为0； 第⼆个⽂件 0368769.index的消息量起始偏移量为36870 = 368769 + 1； 第三个⽂件 073737.index的起始偏移量为73738=73737 + 1 其他后续⽂件依次类推。 以起始偏移量命名并排序这些⽂件，只要根据ofset *⼆分查找 *⽂件列表，就可以快速定位到具体⽂ 件。 当ofset=36876时定位到 0368769.index|log

第⼆步通过segment file查找mesage 通过第⼀步定位到segment file，当ofset=36876时，依次定位到 0368769.index 的元数据物理位置和 0368769.log的物理偏移地址 然后再通过 0368769.log顺序查找直到ofset=36876为⽌。

#### segment index file采取稀疏索引存储⽅式，它减少索引⽂件⼤⼩，通过 map可以直接内存操作，稀 疏索引为数据⽂件的每个对应mesage设置⼀个元数据指针,它⽐稠密索引节省了更多的存储空间，但 查找起来需要消耗更多的时间。

