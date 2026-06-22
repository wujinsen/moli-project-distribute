问题导读：1.Flume-NG与Scribe对⽐，Flume-NG的优势在什么地⽅？

- 2.架构设计考虑需要考虑什么问题？

- 3.Agent死机该如何解决？

- 4.Collector死机是否会有影响？

- 5.Flume-NG可靠性(reliability)⽅⾯做了哪些措施？


![image 1](<基于Flume的美团日志收集系统(一)架构和设计.note_images/imageFile1.png>)

美团的⽇志收集系统负责美团的所有业务⽇志的收集，并分别给Hadop平台提供离线数据和Storm平 台提供实时数据流。美团的⽇志收集系统基于Flume设计和搭建⽽成。 《基于Flume的美团⽇志收集系统》将分两部分给读者呈现美团⽇志收集系统的架构设计和实战经验。 第⼀部分架构和设计，将主要着眼于⽇志收集系统整体的架构设计，以及为什么要做这样的设计。 第⼆部分改进和优化，将主要着眼于实际部署和使⽤过程中遇到的问题，对Flume做的功能修改和优化 等。1 ⽇志收集系统简介 ⽇志收集是⼤数据的基⽯。 许多公司的业务平台每天都会产⽣⼤量的⽇志数据。收集业务⽇志数据，供离线和在线的分析系统使 ⽤，正是⽇志收集系统的要做的事情。⾼可⽤性，⾼可靠性和可扩展性是⽇志收集系统所具有的基本 特征。 ⽬前常⽤的开源⽇志收集系统有Flume, Scribe等。Flume是Cloudera提供的⼀个⾼可⽤的，⾼可靠 的，分布式的海量⽇志采集、聚合和传输的系统，⽬前已经是Apache的⼀个⼦项⽬。Scribe是 Facebok开源的⽇志收集系统，它为⽇志的分布式收集，统⼀处理提供⼀个可扩展的，⾼容错的简单 ⽅案。2 常⽤的开源⽇志收集系统对⽐ 下⾯将对常⻅的开源⽇志收集系统Flume和Scribe的各⽅⾯进⾏对⽐。对⽐中Flume将主要采⽤Apache 下的Flume-NG为参考对象。同时，我们将常⽤的⽇志收集系统分为三层（Agent层，Colector层和 Store层）来进⾏对⽐。[td]

<table>
  <tr>
    <th>对⽐项</th>
    <th>Flume-NG</th>
    <th>Scribe</th>
  </tr>
  <tr>
    <td>使⽤语⾔</td>
    <td>Java</td>
    <td>c/c+</td>
  </tr>
  <tr>
    <td>容错性</td>
    <td>Agent和Colector间，Colector 和Store间都有容错性，且提供 三种级别的可靠性保证；</td>
    <td>Agent和Colector间, Colector 和Store之间有容错性；</td>
  </tr>
  <tr>
    <td>负载均衡</td>
    <td>Agent和Colector间，Colector 和Store间有LoadBalance和<br><br>两种模式</td>
    <td>⽆</td>
  </tr>
  <tr>
    <td>可扩展性</td>
    <td>Failover 好</td>
    <td>好</td>
  </tr>
  <tr>
    <td>Agent丰富程度</td>
    <td>提供丰富的Agent，包括<br><br>等</td>
    <td>主要是thrift端⼝</td>
  </tr>
  <tr>
    <td>Store丰富程度</td>
    <td>avro/thrift socket, text, tail 可以直接写hdfs, text, console, tcp；写hdfs时⽀持对text和<br><br>的压缩；</td>
    <td>提供bufer, network, file(hdfs, text)等</td>
  </tr>
  <tr>
    <td>代码结构</td>
    <td>sequence 系统框架好，模块分明，易于开 发</td>
    <td>代码简单</td>
  </tr>
</table>


3 美团⽇志收集系统架构 美团的⽇志收集系统负责美团的所有业务⽇志的收集，并分别给Hadop平台提供离线数据和Storm平 台提供实时数据流。美团的⽇志收集系统基于Flume设计和搭建⽽成。⽬前每天收集和处理约T级别的 ⽇志数据。 下图是美团的⽇志收集系统的整体框架图。

![image 2](<基于Flume的美团日志收集系统(一)架构和设计.note_images/imageFile2.png>)

- a. 整个系统分为三层：Agent层，Colector层和Store层。其中Agent层每个机器部署⼀个进程，负责 对单机的⽇志收集⼯作；Colector层部署在中⼼服务器上，负责接收Agent层发送的⽇志，并且将⽇志 根据路由规则写到相应的Store层中；Store层负责提供永久或者临时的⽇志存储服务，或者将⽇志流导 向其它服务器。
- b. Agent到Colector使⽤LoadBalance策略，将所有的⽇志均衡地发到所有的Colector上，达到负载均 衡的⽬标，同时并处理单个Colector失效的问题。
- c. Colector层的⽬标主要有三个：SinkHdfs, SinkKafka和SinkBypas。分别提供离线的数据到Hdfs， 和提供实时的⽇志流到Kafka和Bypas。其中SinkHdfs⼜根据⽇志量的⼤⼩分为SinkHdfs_b， SinkHdfs_m和SinkHdfs_s三个Sink，以提⾼写⼊到Hdfs的性能，具体⻅后⾯介绍。
- d. 对于Store来说，Hdfs负责永久地存储所有⽇志；Kafka存储最新的7天⽇志，并给Storm系统提供实 时⽇志流；Bypas负责给其它服务器和应⽤提供实时⽇志流。 下图是美团的⽇志收集系统的模块分解图，详解Agent, Colector和Bypas中的Source, Chanel和 Sink的关系。


![image 3](<基于Flume的美团日志收集系统(一)架构和设计.note_images/imageFile3.png>)

- a. 模块命名规则：所有的Source以src开头，所有的Chanel以ch开头，所有的Sink以sink开头；
- b. Chanel统⼀使⽤美团开发的DualChanel，具体原因后⾯详述；对于过滤掉的⽇志使⽤ NulChanel，具体原因后⾯详述；
- c. 模块之间内部通信统⼀使⽤Avro接⼝；4 架构设计考虑


下⾯将从可⽤性，可靠性，可扩展性和兼容性等⽅⾯，对上述的架构做细致的解析。4.1 可⽤性 (availablity) 对⽇志收集系统来说，可⽤性(availablity)指固定周期内系统⽆故障运⾏总时间。要想提⾼系统的可⽤ 性，就需要消除系统的单点，提⾼系统的冗余度。下⾯来看看美团的⽇志收集系统在可⽤性⽅⾯的考 虑。4.1.1 Agent死掉 Agent死掉分为两种情况：机器死机或者Agent进程死掉。 对于机器死机的情况来说，由于产⽣⽇志的进程也同样会死掉，所以不会再产⽣新的⽇志，不存在不 提供服务的情况。 对于Agent进程死掉的情况来说，确实会降低系统的可⽤性。对此，我们有下⾯三种⽅式来提⾼系统的 可⽤性。⾸先，所有的Agent在supervise的⽅式下启动，如果进程死掉会被系统⽴即重启，以提供服 务。其次，对所有的Agent进⾏存活监控，发现Agent死掉⽴即报警。最后，对于⾮常重要的⽇志，建 议应⽤直接将⽇志写磁盘，Agent使⽤spoldir的⽅式获得最新的⽇志。4.1.2 Collector死掉 由于中⼼服务器提供的是对等的且⽆差别的服务，且Agent访问Colector做了LoadBalance和重试机 制。所以当某个Colector⽆法提供服务时，Agent的重试策略会将数据发送到其它可⽤的Colector上 ⾯。所以整个服务不受影响。4.1.3 Hdfs正常停机 我们在Colector的HdfsSink中提供了开关选项，可以控制Colector停⽌写Hdfs，并且将所有的events 缓存到FileChanel的功能。4.1.4 Hdfs异常停机或不可访问 假如Hdfs异常停机或不可访问，此时Colector⽆法写Hdfs。由于我们使⽤DualChanel，Colector可 以将所收到的events缓存到FileChanel，保存在磁盘上，继续提供服务。当Hdfs恢复服务以后，再将 FileChanel中缓存的events再发送到Hdfs上。这种机制类似于Scribe，可以提供较好的容错性。4.1.5 Collector变慢或者Agent/Collector⽹络变慢 如果Colector处理速度变慢（⽐如机器load过⾼）或者Agent/Colector之间的⽹络变慢，可能导致 Agent发送到Colector的速度变慢。同样的，对于此种情况，我们在Agent端使⽤DualChanel，Agent 可以将收到的events缓存到FileChanel，保存在磁盘上，继续提供服务。当Colector恢复服务以后， 再将FileChanel中缓存的events再发送给Colector。4.1.6 Hdfs变慢 当Hadop上的任务较多且有⼤量的读写操作时，Hdfs的读写数据往往变的很慢。由于每天，每周都有 ⾼峰使⽤期，所以这种情况⾮常普遍。 对于Hdfs变慢的问题，我们同样使⽤DualChanel来解决。当Hdfs写⼊较快时，所有的events只经过 MemChanel传递数据，减少磁盘IO，获得较⾼性能。当Hdfs写⼊较慢时，所有的events只经过 FileChanel传递数据，有⼀个较⼤的数据缓存空间。4.2 可靠性(reliability) 对⽇志收集系统来说，可靠性(reliability)是指Flume在数据流的传输过程中，保证events的可靠传递。 对Flume来说，所有的events都被保存在Agent的Chanel中，然后被发送到数据流中的下⼀个Agent或 者最终的存储服务中。那么⼀个Agent的Chanel中的events什么时候被删除呢？当且仅当它们被保存 到下⼀个Agent的Chanel中或者被保存到最终的存储服务中。这就是Flume提供数据流中点到点的可 靠性保证的最基本的单跳消息传递语义。 那么Flume是如何做到上述最基本的消息传递语义呢？

⾸先，Agent间的事务交换。Flume使⽤事务的办法来保证event的可靠传递。Source和Sink分别被封 装在事务中，这些事务由保存event的存储提供或者由Chanel提供。这就保证了event在数据流的点对 点传输中是可靠的。在多级数据流中，如下图，上⼀级的Sink和下⼀级的Source都被包含在事务中， 保证数据可靠地从⼀个Chanel到另⼀个Chanel转移。

![image 4](<基于Flume的美团日志收集系统(一)架构和设计.note_images/imageFile4.png>)

其次，数据流中 Chanel的持久性。Flume中MemoryChanel是可能丢失数据的（当Agent死掉时）， ⽽FileChanel是持久性的，提供类似mysql的⽇志机制，保证数据不丢失。4.3 可扩展性(scalability) 对⽇志收集系统来说，可扩展性(scalability)是指系统能够线性扩展。当⽇志量增⼤时，系统能够以简 单的增加机器来达到线性扩容的⽬的。 对于基于Flume的⽇志收集系统来说，需要在设计的每⼀层，都可以做到线性扩展地提供服务。下⾯将 对每⼀层的可扩展性做相应的说明。4.3.1 Agent层 对于Agent这⼀层来说，每个机器部署⼀个Agent，可以⽔平扩展，不受限制。⼀个⽅⾯，Agent收集 ⽇志的能⼒受限于机器的性能，正常情况下⼀个Agent可以为单机提供⾜够服务。另⼀⽅⾯，如果机器 ⽐较多，可能受限于后端Colector提供的服务，但Agent到Colector是有Load Balance机制，使得 Colector可以线性扩展提⾼能⼒。4.3.2 Collector层 对于Colector这⼀层，Agent到Colector是有Load Balance机制，并且Colector提供⽆差别服务，所 以可以线性扩展。其性能主要受限于Store层提供的能⼒。4.3.3 Store层 对于Store这⼀层来说，Hdfs和Kafka都是分布式系统，可以做到线性扩展。Bypas属于临时的应⽤， 只对应于某⼀类⽇志，性能不是瓶颈。4.4 Channel的选择 Flume1.4.0中，其官⽅提供常⽤的MemoryChanel和FileChanel供⼤家选择。其优劣如下：

MemoryChanel: 所有的events被保存在内存中。优点是⾼吞吐。缺点是容量有限并且Agent死掉 时会丢失内存中的数据。

FileChanel: 所有的events被保存在⽂件中。优点是容量较⼤且死掉时数据可恢复。缺点是速度较 慢。

上述两种Chanel，优缺点相反，分别有⾃⼰适合的场景。然⽽，对于⼤部分应⽤来说，我们希望 Chanel可以同提供⾼吞吐和⼤缓存。基于此，我们开发了DualChanel。

DualChanel：基于 MemoryChanel和 FileChanel开发。当堆积在Chanel中的events数⼩于阈 值时，所有的events被保存在MemoryChanel中，Sink从MemoryChanel中读取数据； 当堆积在 Chanel中的events数⼤于阈值时， 所有的events被⾃动存放在FileChanel中，Sink从FileChanel 中读取数据。这样当系统正常运⾏时，我们可以使⽤MemoryChanel的⾼吞吐特性；当系统有异常 时，我们可以利⽤FileChanel的⼤缓存的特性。4.5 和scribe兼容

在设计之初，我们就要求每类⽇志都有⼀个category相对应，并且Flume的Agent提供AvroSource和 ScribeSource两种服务。这将保持和之前的Scribe相对应，减少业务的更改成本。4.6 权限控制 在⽬前的⽇志收集系统中，我们只使⽤最简单的权限控制。只有设定的category才可以进⼊到存储系 统。所以⽬前的权限控制就是category过滤。 如果权限控制放在Agent端，优势是可以较好地控制垃圾数据在系统中流转。但劣势是配置修改麻烦， 每增加⼀个⽇志就需要重启或者重载Agent的配置。 如果权限控制放在Colector端，优势是⽅便进⾏配置的修改和加载。劣势是部分没有注册的数据可能 在Agent/Colector之间传输。 考虑到Agent/Colector之间的⽇志传输并⾮系统瓶颈，且⽬前⽇志收集属内部系统，安全问题属于次 要问题，所以选择采⽤Colector端控制。4.7 提供实时流 美团的部分业务，如实时推荐，反爬⾍服务等服务，需要处理实时的数据流。因此我们希望Flume能够 导出⼀份实时流给Kafka/Storm系统。 ⼀个⾮常重要的要求是实时数据流不应该受到其它Sink的速度影响，保证实时数据流的速度。这⼀ 点，我们是通过Colector中设置不同的Chanel进⾏隔离，并且DualChanel的⼤容量保证了⽇志的处 理不受Sink的影响。5 系统监控 对于⼀个⼤型复杂系统来说，监控是必不可少的部分。设计合理的监控，可以对异常情况及时发现， 只要有⼀部⼿机，就可以知道系统是否正常运作。对于美团的⽇志收集系统，我们建⽴了多维度的监 控，防⽌未知的异常发⽣。5.1 发送速度，拥堵情况，写Hdfs速度 通过发送给zabix的数据，我们可以绘制出发送数量、拥堵情况和写Hdfs速度的图表，对于超预期的 拥堵，我们会报警出来查找原因。 下⾯是Flume Colector HdfsSink写数据到Hdfs的速度截图：

![image 5](<基于Flume的美团日志收集系统(一)架构和设计.note_images/imageFile5.png>)

下⾯是Flume Colector的FileChanel中拥堵的events数据量截图：

![image 6](<基于Flume的美团日志收集系统(一)架构和设计.note_images/imageFile6.png>)

5.2 flume写hfds状态的监控 Flume写⼊Hdfs会先⽣成tmp⽂件，对于特别重要的⽇志，我们会每15分钟左右检查⼀下各个Colector 是否都产⽣了tmp⽂件，对于没有正常产⽣tmp⽂件的Colector和⽇志我们需要检查是否有异常。这样 可以及时发现Flume和⽇志的异常.5.3 ⽇志⼤⼩异常监控 对于重要的⽇志，我们会每个⼩时都监控⽇志⼤⼩周同⽐是否有较⼤波动，并给予提醒，这个报警有 效的发现了异常的⽇志，且多次发现了应⽤⽅⽇志发送的异常，及时给予了对⽅反馈，帮助他们及早 修复⾃身系统的异常。 通过上述的讲解，我们可以看到，基于Flume的美团⽇志收集系统已经是具备⾼可⽤性，⾼可靠性，可 扩展等特性的分布式服务。

# 下⼀篇： 基于Flume的美团⽇志收集系统(⼆)改进和优化

