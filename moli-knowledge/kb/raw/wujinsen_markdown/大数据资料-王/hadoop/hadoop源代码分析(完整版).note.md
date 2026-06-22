# adop源代码分析(完整版)

2013-2-25阅读3584

评论0

Hadop源代码分析（⼀）

关键字: 分布式云计算

Gogle的核⼼竞争技术是它的计算平台。Gogle的⼤⽜们⽤了下⾯5篇⽂章，介绍了它们的计算设 施。 GogleCluster：htp:/research.gogle.com/archive/goglecluster.html Chuby：htp:/labs.gogle.com/papers/chuby.html GFS：htp:/labs.gogle.com/papers/gfs.html BigTable：htp:/labs.gogle.com/papers/bigtable.html MapReduce：htp:/labs.gogle.com/papers/mapreduce.html 很快，Apache上就出现了⼀个类似的解决⽅案，⽬前它们都属于Apache的Hadop项⽬，对应的分别 是： Chuby->ZoKeper GFS->HDFS BigTable->HBase MapReduce->Hadop ⽬前，基于类似思想的Open Source项⽬还很多，如Facebok⽤于⽤户分析的Hive。 HDFS作为⼀个分布式⽂件系统，是所有这些项⽬的基础。分析好HDFS，有利于了解其他系统。由于 Hadop的HDFS和MapReduce是同⼀个项⽬，我们就把他们放在⼀块，进⾏分析。 下图是MapReduce整个项⽬的顶层包图和他们的依赖关系。Hadop包之间的依赖关系⽐较复杂，原 因是HDFS提供了⼀个分布式⽂件系统，该系统提供API，可以屏蔽本地⽂件系统和分布式⽂件系统， 甚⾄象Amazon S3这样的在线存储系统。这就造成了分布式⽂件系统的实现，或者是分布式⽂件系统 的底层的实现，依赖于某些貌似⾼层的功能。功能的相互引⽤，造成了蜘蛛⽹型的依赖关系。⼀个典 型的例⼦就是包conf，conf⽤于读取系统配置，它依赖于fs，主要是读取配置⽂件的时候，需要使⽤⽂ 件系统，⽽部分的⽂件系统的功能，在包fs中被抽象了。 Hadop的关键部分集中于图中蓝⾊部分，这也是我们考察的重点。

⼤⼩: 78.3 KB

Hadop源代码分析（⼆）

下⾯给出了Hadop的包的功能分析。

<table>
  <tr>
    <th>Package</th>
    <th>Dependences</th>
  </tr>
  <tr>
    <td>tol</td>
    <td>提供⼀些命令⾏⼯具，如DistCp，archive</td>
  </tr>
  <tr>
    <td>mapreduce</td>
    <td>Hadop的Map/Reduce实现</td>
  </tr>
  <tr>
    <td>filecache</td>
    <td>提供HDFS⽂件的本地缓存，⽤于加快 的数据访问速度</td>
  </tr>
  <tr>
    <td>fs</td>
    <td>Map/Reduce ⽂件系统的抽象，可以理解为⽀持多种⽂件系统 实现的统⼀⽂件访问接⼝</td>
  </tr>
  <tr>
    <td>hdfs</td>
    <td>HDFS，Hadop的分布式⽂件系统实现</td>
  </tr>
  <tr>
    <td>ipc</td>
    <td>⼀个简单的IPC的实现，依赖于io提供的编解码功 能 参考：</td>
  </tr>
  <tr>
    <td>io</td>
    <td>htp:/zhangyu8374.javaeye.com/blog/86306 表示层。将各种数据编码/解码，⽅便于在⽹络上 传输</td>
  </tr>
  <tr>
    <td>net</td>
    <td>封装部分⽹络功能，如DNS，socket</td>
  </tr>
  <tr>
    <td>security</td>
    <td>⽤户和⽤户组信息</td>
  </tr>
  <tr>
    <td>conf</td>
    <td>系统的配置参数</td>
  </tr>
  <tr>
    <td>metrics</td>
    <td>系统统计数据的收集，属于⽹管范畴</td>
  </tr>
  <tr>
    <td>util</td>
    <td>⼯具类</td>
  </tr>
  <tr>
    <td>record</td>
    <td>根据 DL（数据描述语⾔）⾃动⽣成他们的编解</td>
  </tr>
  <tr>
    <td>htp</td>
    <td>码函数，⽬前可以提供C+和Java 基于Jety的HTP Servlet，⽤户通过浏览器可以 观察⽂件系统的⼀些状态信息和⽇志</td>
  </tr>
  <tr>
    <td>log</td>
    <td>提供HTP访问⽇志的HTP Servlet</td>
  </tr>
</table>


## Hadop源代码分析（三）

由于Hadop的MapReduce和HDFS都有通信的需求，需要对通信的对象进⾏序列化。Hadop并没有 采⽤Java的序列化，⽽是引⼊了它⾃⼰的系统。 org.apache.hadop.io中定义了⼤量的可序列化对象，他们都实现了Writable接⼝。实现了Writable接 ⼝的⼀个典型例⼦如下： Java代码

- 1.
- 2.
- 3.


public clas MyWritable implements Writable { / Some data private int counter;

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


private long timestamp;

public void write(DataOutput out) throws IOException { out.writeInt(counter); out.writeLong(timestamp);

}

public void readFields(DataInput in) throws IOException { counter = in.readInt(); timestamp = in.readLong();

}

public static MyWritable read(DataInput in) throws IOException { MyWritable w = new MyWritable(); w.readFields(in); return w;

} }

public clas MyWritable implements Writable {

/ Some data private int counter; private long timestamp;

public void write(DataOutput out) throws IOException { out.writeInt(counter); out.writeLong(timestamp);

}

public void readFields(DataInput in) throws IOException { counter = in.readInt(); timestamp = in.readLong();

}

public static MyWritable read(DataInput in) throws IOException { MyWritable w = new MyWritable(); w.readFields(in); return w;

} }

其中的write和readFields分别实现了把对象序列化和反序列化的功能，是Writable接⼝定义的两个⽅ 法。下图给出了庞⼤的org.apache.hadop.io中对象的关系。

这⾥，我把ObjectWritable标为红⾊，是因为相对于其他对象，它有不同的地位。当我们讨论Hadop 的RPC时，我们会提到RPC上交换的信息，必须是Java的基本类型，String和Writable接⼝的实现类， 以及元素为以上类型的数组。ObjectWritable对象保存了⼀个可以在RPC上传输的对象和对象的类型信 息。这样，我们就有了⼀个万能的，可以⽤于客户端/服务器间传输的Writable对象。例如，我们要把 上⾯例⼦中的对象作为RPC请求，需要根据MyWritable创建⼀个ObjectWritable，ObjectWritable往流 ⾥会写如下信息

对 象 类 名 ⻓ 度 ， 对 象 类 名 ， 对 象 ⾃ ⼰ 的 串 ⾏ 化 结 果

这样，到了对端，ObjectWritable可以根据对象类名创建对应的对象，并解串⾏。应该注意到， ObjectWritable依赖于WritableFactories，那存储了Writable⼦类对应的⼯⼚。我们需要把MyWritable 的⼯⼚，保存在WritableFactories中（通过WritableFactories.setFactory）。

## Hadop源代码分析（五）

介绍完org.apache.hadop.io以后，我们开始来分析org.apache.hadop.rpc。RPC采⽤客户机/服务器 模式。请求程序就是⼀个客户机，⽽服务提供程序就是⼀个服务器。当我们讨论HDFS的，通信可能发 ⽣在：

Client-NameNode之间，其中NameNode是服务器

Client-DataNode之间，其中DataNode是服务器

DataNode-NameNode之间，其中NameNode是服务器

DataNode-DateNode之间，其中某⼀个DateNode是服务器，另⼀个是客户端

如果我们考虑Hadop的Map/Reduce以后，这些系统间的通信就更复杂了。为了解决这些客户机/服务 器之间的通信，Hadop引⼊了⼀个RPC框架。该RPC框架利⽤的Java的反射能⼒，避免了某些RPC解 决⽅案中需要根据某种接⼝语⾔（如CORBA的IDL）⽣成存根和框架的问题。但是，该RPC框架要求调 ⽤的参数和返回结果必须是Java的基本类型，String和Writable接⼝的实现类，以及元素为以上类型的 数组。同时，接⼝⽅法应该只抛出IOException异常。（参考⾃

htp:/zhangyu8374.javaeye.com/blog/ 86306

） 既然是RPC，当然就有客户端和服务器，当然，org.apache.hadop.rpc也就有了类Client和类 Server。但是类Server是⼀个抽象类，类RPC封装了Server，利⽤反射，把某个对象的⽅法开放出来， 变成RPC中的服务器。 下图是org.apache.hadop.rpc的类图。

⼤⼩: 130.3 KB

## Hadop源代码分析（六）

既然是RPC，⾃然就有客户端和服务器，当然，org.apache.hadop.rpc也就有了类Client和类 Server。在这⾥我们来仔细考察org.apache.hadop.rpc.Client。下⾯的图包含了 org.apache.hadop.rpc.Client中的关键类和关键⽅法。 由于Client可能和多个Server通信，典型的⼀次HDFS读，需要和NameNode打交道，也需要和某个/某 些DataNode通信。这就意味着某⼀个Client需要维护多个连接。同时，为了减少不必要的连接，现在 Client的做法是拿ConectionId（图中最右侧）来做为Conection的ID。ConectionId包括⼀个 InetSocketAdres（IP地址+端⼝号或主机名+端⼝号）对象和⼀个⽤户信息对象。这就是说，同⼀个 ⽤户到同⼀个InetSocketAdres的通信将共享同⼀个连接。

连接被封装在类Client.Conection中，所有的RPC调⽤，都是通过Conection，进⾏通信。⼀个RPC 调⽤，⾃然有输⼊参数，输出参数和可能的异常，同时，为了区分在同⼀个Conection上的不同调 ⽤，每个调⽤都有唯⼀的id。调⽤是否结束也需要⼀个标记，所有的这些都体现在对象Client.Cal中。 Conection对象通过⼀个Hash表，维护在这个连接上的所有Cal： Java代码

1.

private Hashtable<Integer, Cal> cals = new Hashtable<Integer, Cal>();

private Hashtable<Integer, Cal> cals = new Hashtable<Integer, Cal>();

⼀个RPC调⽤通过adCal，把请求加到Conection⾥。为了能够在这个框架上传输Java的基本类型， String和Writable接⼝的实现类，以及元素为以上类型的数组，我们⼀般把Cal需要的参数打包成为 ObjectWritable对象。 Client.Conection会通过socket连接服务器，连接成功后回校验客户端/服务器的版本号 （Client.ConectionwriteHeader()⽅法），校验成功后就可以通过Writable对象来进⾏请求的发送/应 答了。注意，每个Client.Conection会起⼀个线程，不断去读取socket，并将收到的结果解包，找出 对应的Cal，设置Cal并通知结果已经获取。 Cal使⽤Obejct的wait和notify，把RPC上的异步消息交互转成同步调⽤。 还有⼀点需要注意，⼀个Client会有多个Client.Conection，这是⼀个很⾃然的结果。

## Hadop源代码分析（七）

聊完了Client聊Server，按惯例，先把类图贴出来。

需要注意的是，这⾥的Server类是个抽象类，唯⼀抽象的地⽅，就是 Java代码

1.

public abstract Writable cal(Writable param, long receiveTime) throws IOException;

public abstract Writable cal(Writable param, long receiveTime) throws IOException;

这表明，Server提供了⼀个架⼦，Server的具体功能，需要具体类来完成。⽽具体类，当然就是实现 cal⽅法。

我们先来分析Server.Cal，和Client.Cal类似，Server.Cal包含了⼀次请求，其中，id和param的含义和 Client.Cal是⼀致的。不同点在后⾯三个属性，conection是该Cal来⾃的连接，当然，当请求处理结 束时，相应的结果会通过相同的conection，发送给客户端。属性timestamp是请求到达的时间戳，如 果请求很⻓时间没被处理，对应的连接会被关闭，客户端也就知道出错了。最后的response是请求处 理的结果，可能是⼀个Writable的串⾏化结果，也可能⼀个异常的串⾏化结果。 Server.Conection维护了⼀个来之客户端的socket连接。它处理版本校验，读取请求并把请求发送到 请求处理线程，接收处理结果并把结果发送给客户端。 Hadop的Server采⽤了Java的NIO，这样的话就不需要为每⼀个socket连接建⽴⼀个线程，读取 socket上的数据。在Server中，只需要⼀个线程，就可以acept新的连接请求和读取socket上的数 据，这个线程，就是上⾯图⾥的Listener。 请求处理线程⼀般有多个，它们都是Server.Handle类的实例。它们的run⽅法循环地取出⼀个 Server.Cal，调⽤Server.cal⽅法，搜集结果并串⾏化，然后将结果放⼊Responder队列中。 对于处理完的请求，需要将结果写回去，同样，利⽤NIO，只需要⼀个线程，相关的逻辑在Responder ⾥。

## Hadop源代码分析（⼋）

（注：本节需要⽤到⼀些Java反射的背景） 有了Client和Server，很⾃然就能RPC啦。下⾯轮到RPC.java啦。 ⼀般来说，分布式对象⼀般都会要求根据接⼝⽣成存根和框架。如CORBA，可以通过IDL，⽣成存根 和框架。但是，在org.apache.hadop.rpc，我们就不需要这样的步骤了。上类图。 为了分析Invoker，我们需要介绍⼀些Java反射实现Dynamic Proxy的背景。 Dynamic Proxy是由两个clas实现的：java.lang.reflect.Proxy 和 java.lang.reflect.InvocationHandler，后者是⼀个接⼝。所谓DynamicProxy是这样⼀种clas：它是在 运⾏时⽣成的clas，在⽣成它时你必须提供⼀组interface给它，然后该clas就宣称它实现了这些 interface。 这个Dynamic Proxy其实就是⼀个典型的Proxy模式，它不会替你作实质性的⼯作，在⽣成它的实例时 你必须提供⼀个handler，由它接管实际的⼯作。这个handler，在Hadop的RPC中，就是Invoker对 象。 我们可以简单地理解：就是你可以通过⼀个接⼝来⽣成⼀个类，这个类上的所有⽅法调⽤，都会传递 到你⽣成类时传递的InvocationHandler实现中。 在Hadop的RPC中，Invoker实现了InvocationHandler的invoke⽅法（invoke⽅法也是 InvocationHandler的唯⼀⽅法）。Invoker会把所有跟这次调⽤相关的调⽤⽅法名，参数类型列表，参 数列表打包，然后利⽤前⾯我们分析过的Client，通过socket传递到服务器端。就是说，你在proxy类 上的任何调⽤，都通过Client发送到远⽅的服务器上。 Invoker使⽤Invocation。Invocation封装了⼀个远程调⽤的所有相关信息，它的主要属性 有:methodName，调⽤⽅法名，parameterClases，调⽤⽅法参数的类型列表和parameters，调⽤⽅ 法参数。注意，它实现了Writable接⼝，可以串⾏化。

RPC.Server实现了org.apache.hadop.ipc.Server，你可以把⼀个对象，通过RPC，升级成为⼀个服务 器。服务器接收到的请求（通过Invocation），解串⾏化以后，就变成了⽅法名，⽅法参数列表和参数 列表。利⽤Java反射，我们就可以调⽤对应的对象的⽅法。调⽤的结果再通过socket，返回给客户 端，客户端把结果解包后，就可以返回给Dynamic Proxy的使⽤者了。

## Hadop源代码分析（九）

⼀个典型的HDFS系统包括⼀个NameNode和多个DataNode。NameNode维护名字空间；⽽ DataNode存储数据块。 DataNode负责存储数据，⼀个数据块在多个DataNode中有备份；⽽⼀个DataNode对于⼀个块最多只 包含⼀个备份。所以我们可以简单地认为DataNode上存了数据块ID和数据块内容，以及他们的映射关 系。 ⼀个HDFS集群可能包含上千DataNode节点，这些DataNode定时和NameNode通信，接受 NameNode的指令。为了减轻NameNode的负担，NameNode上并不永久保存那个DataNode上有那些 数据块的信息，⽽是通过DataNode启动时的上报，来更新NameNode上的映射表。 DataNode和NameNode建⽴连接以后，就会不断地和NameNode保持⼼跳。⼼跳的返回其还也包含了 NameNode对DataNode的⼀些命令，如删除数据库或者是把数据块复制到另⼀个DataNode。应该注 意的是：NameNode不会发起到DataNode的请求，在这个通信过程中，它们是严格的客户端/服务器 架构。 DataNode当然也作为服务器接受来⾃客户端的访问，处理数据块读/写请求。DataNode之间还会相互 通信，执⾏数据块复制任务，同时，在客户端做写操作的时候，DataNode需要相互配合，保证写操作 的⼀致性。 下⾯我们就来具体分析⼀下DataNode的实现。DataNode的实现包括两部分，⼀部分是对本地数据块 的管理，另⼀部分，就是和其他的实体打交道。我们先来看本地数据块管理部分。 安装Hadop的时候，我们会指定对应的数据块存放⽬录，当我们检查数据块存放⽬录⽬录时，我们回 发现下⾯有个叫dfs的⽬录，所有的数据就存放在dfs/data⾥⾯。 其中有两个⽂件，storage⾥存的东⻄是⼀些出错信息，貌似是版本不对…云云。in_use.lock是⼀个空 ⽂件，它的作⽤是如果需要对整个系统做排斥操作，应⽤应该获取它上⾯的⼀个锁。 接下来是3个⽬录，curent存的是当前有效的数据块，detach存的是快照（snapshot，⽬前没有实 现），tmp保存的是⼀些操作需要的临时数据块。 但我们进⼊curent⽬录以后，就会发现有⼀系列的数据块⽂件和数据块元数据⽂件。同时还有⼀些⼦ ⽬录，它们的名字是subdir0到subdir63，⼦⽬录下也有数据块⽂件和数据块元数据。这是因为HDFS 限定了每个⽬录存放数据块⽂件的数量，多了以后会创建⼦⽬录来保存。 数据块⽂件显然保存了HDFS中的数据，数据块最⼤可以到64M。每个数据块⽂件都会有对应的数据块 元数据⽂件。⾥⾯存放的是数据块的校验信息。下⾯是数据块⽂件名和它的元数据⽂件名的例⼦： blk_3148782637964391313 blk_3148782637964391313_242812.meta 上⾯的例⼦中，3148782637964391313是数据块的ID号，242812是数据块的版本号，⽤于⼀致性检 查。

在curent⽬录下还有下⾯⼏个⽂件： VERSION，保存了⼀些⽂件系统的元信息。 dncp_block_verification.log.cur和dncp_block_verification.log.prev，它记录了⼀些DataNode对⽂ 件系定时统做⼀致性检查需要的信息。

## Hadop源代码分析（⼀零）

在继续分析DataNode之前，我们有必要看⼀下系统的⼯作状态。启动HDFS的时候，我们可以选择以 下启动参数：

FORMAT("-format")：格式化系统

REGULAR("-regular")：正常启动

UPGRADE("-upgrade")：升级

ROLBACK("-rolback")：回滚

FINALIZE("-finalize")：提交

IMPORT("-importCheckpoint")：从Checkpoint恢复。

htp:/wiki.apache.org/hadop/Hado op_Upgrade

作为⼀个⼤型的分布式系统，Hadop内部实现了⼀套升级机制（

）。upgrade参数就是为了这个⽬的⽽存在的，当然，升级可能成功，也可能失败。如果 失败了，那就⽤rolback进⾏回滚；如果过了⼀段时间，系统运⾏正常，那就可以通过finalize，正式提 交这次升级(跟数据库有点像啊)。 importCheckpoint选项⽤于NameNode发⽣故障后，从某个检查点恢复。 有了上⾯的描述，我们得到下⾯左边的状态图： ⼤家应该注意到，上⾯的升级/回滚/提交都不可能⼀下就搞定，就是说，系统故障时，它可能处于上⾯ 右边状态中的某⼀个。特别是分布式的各个节点上，甚⾄可能出现某些节点已经升级成功，但有些节 点可能处于中间状态的情况，所以Hadop采⽤类似于数据库事务的升级机制也就不是很奇怪。 ⼤家先理解⼀下上⾯的状态图，它是下⾯我们要介绍DataNode存储的基础。

## Hadop源代码分析（⼀⼀）

我们来看⼀下升级/回滚/提交时的DataNode上会发⽣什么（在类DataStorage中实现）。 前⾯我们提到过VERSION⽂件，它保存了⼀些⽂件系统的元信息，这个⽂件在系统升级时，会发⽣对 应的变化。 升级时，NameNode会将新的版本号，通过DataNode的登录应答返回。DataNode收到以后，会将当 前的数据块⽂件⽬录改名，从curent改名为previous.tmp，建⽴⼀个snapshot，然后重建curent⽬ 录。重建包括重建VERSION⽂件，重建对应的⼦⽬录，然后建⽴数据块⽂件和数据块元数据⽂件到 previous.tmp的硬连接。建⽴硬连接意味着在系统中只保留⼀份数据块⽂件和数据块元数据⽂件， curent和previous.tmp中的相应⽂件，在存储中，只保留⼀份。当所有的这些⼯作完成以后，会在 curent⾥写⼊新的VERSION⽂件，并将previous.tmp⽬录改名为previous，完成升级。 了解了升级的过程以后，回滚就相对简单。因为说有的旧版本信息都保存在previous⽬录⾥。回滚⾸先 将curent⽬录改名为removed.tmp，然后将previous⽬录改名为curent，最后删除removed.tmp⽬ 录。 提交的过程，就是将上⾯的previous⽬录改名为finalized.tmp，然后启动⼀个线程，将该⽬录删除。

下图给出了上⾯的过程： 需要注意的是，HDFS的升级，往往只是⽀持从某⼀个特点的⽼版本升级到当前版本。回滚时能够恢复 到的版本，也是previous中记录的版本。 下⾯我们继续分析DataNode。 ⽂字分析完DataNode存储在⽂件上的数据以后，我们来看⼀下运⾏时对应的数据结构。从⼤到⼩， Hadop中最⼤的结构是Storage，最⼩的结构，在DataNode上是block。 类Storage保存了和存储相关的信息，它继承了StorageInfo，应⽤于DataNode的DataStorage，则继 承了Storage，总体类图如下：

StorageInfo包含了3个字段，分别是layoutVersion：版本号，如果Hadop调整⽂件结构布局，版本号 就会修改，这样可以保证⽂件结构和应⽤⼀致。namespaceID是Storage的ID，cTime，creation time。 和StorageInfo相⽐，Storage就是个⼤家伙了。 Storage可以包含多个根（参考配置项dfs.data.dir的说明），这些根通过Storage的内部类 StorageDirectory来表示。StorageDirectory中最重要的⽅法是analyzeStorage，它将根据系统启动时 的参数和我们上⾯提到的⼀些判断条件，返回系统现在的状态。StorageDirectory可能处于以下的某⼀ 个状态（与系统的⼯作状态⼀定的对应）：

NON_EXISTENT：指定的⽬录不存在； NOT_FORMATED：指定的⽬录存在但未被格式化；

COMPLETE_UPGRADE：previous.tmp存在，curent也存在 RECOVER_UPGRADE：previous.tmp存在，curent不存在

COMPLETE_FINALIZE：finalized.tmp存在，curent也存在

COMPLETE_ROLBACK：removed.tmp存在，curent也存在，previous不存在 RECOVER_ROLBACK：removed.tmp存在，curent不存在，previous存在

COMPLETE_CHECKPOINT：lastcheckpoint.tmp存在，curent也存在 RECOVER_CHECKPOINT：lastcheckpoint.tmp存在，curent不存在

NORMAL：普通⼯作模式。

StorageDirectory处于某些状态是通过发⽣对应状态改变需要的⼯作⽂件夹和正常⼯作的curent夹来 进⾏判断。状态改变需要的⼯作⽂件夹包括：

previous：⽤于升级后保存以前版本的⽂件 previous.tmp：⽤于升级过程中保存以前版本的⽂件

removed.tmp：⽤于回滚过程中保存⽂件 finalized.tmp：⽤于提交过程中保存⽂件 lastcheckpoint.tmp：应⽤于从NameNode中，导⼊⼀个检查点 previous.checkpoint：应⽤于从NameNode中，结束导⼊⼀个检查点

有了这些状态，就可以对系统进⾏恢复（通过⽅法doRecover）。恢复的动作如下（结合上⾯的状态转 移图）：

COMPLETE_UPGRADE：mvprevious.tmp -> previous RECOVER_UPGRADE：mv previous.tmp -> curent

COMPLETE_FINALIZE：rm finalized.tmp

COMPLETE_ROLBACK：rm removed.tmp RECOVER_ROLBACK：mv removed.tmp -> curent

COMPLETE_CHECKPOINT：mv lastcheckpoint.tmp -> previous.checkpoint RECOVER_CHECKPOINT：mv lastcheckpoint.tmp -> curent

我们以RECOVER_UPGRADE为例，分析⼀下。根据升级的过程，

- 1. curent->previous.tmp
- 2. 重建curent
- 3. previous.tmp->previous


当我们发现previous.tmp存在，curent不存在，我们知道只需要将previous.tmp改为curent，就能恢 复到未升级时的状态。 StorageDirectory还管理着⽂件系统的元信息，就是我们上⾯提过StorageInfo信息，当然， StorageDirectory还保存每个具体⽤途⾃⼰的信息。这些信息，其实都存储在VERSION⽂件中， StorageDirectory中的read/write⽅法，就是⽤于对这个⽂件进⾏读/写。下⾯是某⼀个DataNode的 VERSION⽂件的例⼦：

配置⽂件代码 #Fri Nov 14 10 27 35 CST208 namespaceID=195097968 storageID=DS-697414267-127.0.0.1-5010-1262965026 cTime=0 storageType=DATA_NODE

- 1.
- 2.
- 3.
- 4.
- 5.


6.

layoutVersion=-16

#Fri Nov 14 10 27 35 CST 208 namespaceID=195097968 storageID=DS-697414267-127.0.0.1-5010-1262965026 cTime=0 storageType=DATA_NODE layoutVersion=-16

对StorageDirectory的排他操作需要锁，还记得我们在分析系统⽬录时提到的in_use.lock⽂件吗？它就 是⽤来给整个系统加/解锁⽤的。StorageDirectory提供了对应的lock和unlock⽅法。 分析完StorageDirectory以后，Storage类就很简单了。基本上都是对⼀系列StorageDirectory的操 作，同时Storage提供⼀些辅助⽅法。 DataStorage是Storage的⼦类，专⻔应⽤于DataNode。上⾯我们对DataNode的升级/回滚/提交过 程，就是对DataStorage的doUpgrade/doRolback/doFinalize分析得到的。 DataStorage提供了format⽅法，⽤于创建DataNode上的Storage，同时，利⽤StorageDirectory， DataStorage管理存储系统的状态。

Hadop源代码分析（⼀⼆）

分析完Storage相关的类以后，我们来看下⼀个⼤家伙，FSDataset相关的类。 上⾯介绍Storage时，我们并没有涉及到数据块Block的操作，所有和数据块相关的操作，都在 FSDataset相关的类中进⾏处理。下⾯是类图：

Block是对⼀个数据块的抽象，通过前⾯的讨论我们知道⼀个Block对应着两个⽂件，其中⼀个存数 据，⼀个存校验信息，如下： blk_3148782637964391313 blk_3148782637964391313_242812.meta 上⾯的信息中，blockId是3148782637964391313，242812是数据块的版本号，当然，系统还会保存 数据块的⼤⼩，在类中是属性numBytes。Block提供了⼀系列的⽅法来操作对象的属性。 DatanodeBlockInfo存放的是Block在⽂件系统上的信息。它保存了Block存放的卷（FSVolume），⽂ 件名和detach状态。这⾥有必要解释⼀下detach状态：我们前⾯分析过，系统在升级时会创建⼀个 snapshot，snapshot的⽂件和curent⾥的数据块⽂件和数据块元⽂件是通过硬链接，指向了相同的内 容。当我们需要改变curent⾥的⽂件时，如果不进⾏detach操作，那么，修改的内容就会影响 snapshot⾥的⽂件，这时，我们需要将对应的硬链接解除掉。⽅法很简单，就是在临时⽂件夹⾥，复 制⽂件，然后将临时⽂件改名成为curent⾥的对应⽂件，这样的话，curent⾥的⽂件和snapshot⾥的 ⽂件就detach了。这样的技术，也叫copy-on-write，是⼀种有效提⾼系统性能的⽅法。 DatanodeBlockInfo中的detachBlock，能够对Block对应的数据⽂件和元数据⽂件进⾏detach操作。

介绍完类Block和DatanodeBlockInfo后，我们来看FSVolumeSet，FSVolume和FSDir。我们知道在⼀ 个DataNode上可以指定多个Storage来存储数据块，由于HDFS规定了⼀个⽬录能存放Block的数⽬， 所以⼀个Storage上存在多个⽬录。对应的，FSDataset中⽤FSVolume来对应⼀个Storage，FSDir对应 ⼀个⽬录，所有的FSVolume由FSVolumeSet管理，FSDataset中通过⼀个FSVolumeSet对象，就可以 管理它的所有存储空间。 FSDir对应着HDFS中的⼀个⽬录，⽬录⾥存放着数据块⽂件和它的元⽂件。FSDir的⼀个重要的操作， 就是在添加⼀个Block时，根据需要有时会扩展⽬录结构，上⾯提过，⼀个Storage上存在多个⽬录， 所有的⽬录，都对应着⼀个FSDir，⽬录的关系，也由FSDir保存。FSDir的getBlockInfo⽅法分析⽬录 下的所有数据块⽂件信息，⽣成Block对象，存放到⼀个集合中。getVolumeMap⽅法能，则会建⽴ Block和DatanodeBlockInfo的关系。以上两个⽅法，⽤于系统启动时搜集所有的数据块信息，便于后 ⾯快速访问。 FSVolume对应着是某⼀个Storage。数据块⽂件，detach⽂件和临时⽂件都是通过FSVolume来管理 的，这个其实很⾃然，在同⼀个存储系统上移动⽂件，往往只需要修改⽂件存储信息，不需要搬数 据。FSVolume有⼀个recoverDetachedBlocks的⽅法，⽤于恢复detach⽂件。和Storage的状态管理 ⼀样，detach⽂件有可能在复制⽂件时系统崩溃，需要对detach的操作进⾏回复。FSVolume还会启动 ⼀个线程，不断更新FSVolume所在⽂件系统的剩余容量。创建Block的时候，系统会根据各个 FSVolume的容量，来确认Block的存放位置。 FSVolumeSet就不讨论了，它管理着所有的FSVolume。 HDFS中，对⼀个chunk的写会使⽂件处于活跃状态，FSDataset中引⼊了类ActiveFile。ActiveFile对象 保存了⼀个⽂件，和操作这个⽂件的线程。注意，线程有可能有多个。ActiveFile的构造函数会⾃动地 把当前线程加⼊其中。 有了上⾯的基础，我们可以开始分析FSDataset。FSDataset实现了接⼝FSDatasetInterface。 FSDatasetInterface是DataNode对底层存储的抽象。 下⾯给出了FSDataset的关键成员变量：

FSVolumeSet volumes; privateHashMap<Block,ActiveFile>ongoingCreates=newHashMap<Block,ActiveFile>(); privateHashMap<Block,DatanodeBlockInfo>volumeMap=nul;

其中，volumes就是FSDataset使⽤的所有Storage，ongoingCreates是Block到ActiveFile的映射，也 就是说，说有正在创建的Block，都会记录在ongoingCreates⾥。 下⾯我们讨论FSDataset中的⽅法。 public long getMetaDataLength(Block b)throws IOException; 得到⼀个block的元数据⻓度。通过block的ID，找对应的元数据⽂件，返回⽂件⻓度。

public MetaDataInputStream getMetaDataInputStream(Block b) throwsIOException; 得到⼀个block的元数据输⼊流。通过block的ID，找对应的元数据⽂件，在上⾯打开输⼊流。下⾯对 于类似的简单⽅法，我们就不再仔细讨论了。

public boleanmetaFileExists(Block b)throwsIOException; 判断block的元数据的元数据⽂件是否存在。简单⽅法。

public longgetLength(Block b)throwsIOException; block的⻓度。简单⽅法。

public Block getStoredBlock(longblkid)throwsIOException; 通过Block的ID，找到对应的Block。简单⽅法。

public InputStream getBlockInputStream(Block b) throwsIOException; public InputStream getBlockInputStream(Block b,longsekOfset) throws IOException; 得到Block数据的输⼊流。简单⽅法。

public BlockInputStreams getTmpInputStreams(Block b, long blkof,long ckof) throws IOException; 得到Block的临时输⼊流。注意，临时输⼊流是指对应的⽂件处于tmp⽬录中。新创建块时，块数据应 该写在tmp⽬录中，直到写操作成功，⽂件才会被移动到curent⽬录中，如果失败，就不会影响 curent⽬录了。简单⽅法。

public BlockWriteStreams writeToBlock(Block b, boleanisRecovery)throws IOException; 得到⼀个block的输出流。BlockWriteStreams既包含了数据输出流，也包含了元数据（校验⽂件）输 出流，这是⼀个相当复杂的⽅法。 参数isRecovery说明这次写是不是对以前失败的写的⼀次恢复操作。我们先看正常的写操作流程：⾸ 先，如果输⼊的block是个正常的数据块，或当前的block已经有线程在写，writeToBlock会抛出⼀个异 常。否则，将创建相应的临时数据⽂件和临时元数据⽂件，并把相关信息，创建⼀个ActiveFile对象， 记录到ongoingCreates中，并创建返回的BlockWriteStreams。前⾯我们已经提过，建⽴新的 ActiveFile时，当前线程会⾃动保存在ActiveFile的threads中。 我们以blk_3148782637964391313为例，当DataNode需要为Block ID为3148782637964391313创 建写流时，DataNode创建⽂件tmp/blk_3148782637964391313做为临时数据⽂件，对应的meta⽂件 是tmp/blk_3148782637964391313_ X.meta。其中 X是版本号。 isRecovery为true时，表明我们需要从某⼀次不成功的写中恢复，流程相对于正常流程复杂。如果不成 功的写是由于提交（参考finalizeBlock⽅法）后的确认信息没有收到，先创建⼀个detached⽂件（备 份）。接着，writeToBlock检查是否有还有对⽂件写的线程，如果有，则通过线程的interupt⽅法，强 制结束线程。这就是说，如果有线程还在写对应的⽂件块，该线程将被终⽌。同时，从 ongoingCreates中移除对应的信息。接下来将根据临时⽂件是否存在，创建/复⽤临时数据⽂件和临时 数据元⽂件。后续操作就和正常流程⼀样，根据相关信息，创建⼀个ActiveFile对象，记录到 ongoingCreates中 …

由于这块涉及了⼀些HDFS写⽂件时的策略，以后我们还会继续讨论这个话题。

public voidupdateBlock(Block oldblock, Block newblock) throws IOException; 更新⼀个block。这也是⼀个相当复杂的⽅法。 updateBlock的最外层是⼀个死循环，循环的结束条件，是没有任何和这个数据块相关的写线程。每次 循环，updateBlock都会去调⽤⼀个叫tryUpdateBlock的内部⽅法。tryUpdateBlock发现已经没有线程 在写这个块，就会跟新和这个数据块相关的信息，包括元⽂件和内存中的映射表volumeMap。如果 tryUpdateBlock发现还有活跃的线程和该块关联，那么，updateBlock会试图结束该线程，并等在join 上等待。

public voidfinalizeBlock(Block b)throwsIOException; 提交（或叫：结束finalize）通过writeToBlock打开的block，这意味着写过程没有出错，可以正式把 Block从tmp⽂件夹放到curent⽂件夹。 在FSDataset中，finalizeBlock将从ongoingCreates中删除对应的block，同时将block对应的 DatanodeBlockInfo，放⼊volumeMap中。我们还是以blk_3148782637964391313为例，当 DataNode提交Block ID为3148782637964391313数据块⽂件时，DataNode将把 tmp/blk_3148782637964391313移到curent下某⼀个⽬录，以subdir12为例，这是 tmp/blk_3148782637964391313将会挪到curent/subdir12/blk_3148782637964391313。对应的 meta⽂件也在⽬录curent/subdir12下。

public voidunfinalizeBlock(Block b)throwsIOException; 取消通过writeToBlock打开的block，与finalizeBlock⽅法作⽤相反。简单⽅法。

public boleanisValidBlock(Block b); 该Block是否有效。简单⽅法。

public voidinvalidate(Block invalidBlks[])throwsIOException; 使block变为⽆效。简单⽅法。

public void validateBlockMetadata(Block b) throws IOException; 检查block的有效性。简单⽅法。

## Hadop源代码分析（⼀三）

通过上⾯的⼀系列介绍，我们知道了DataNode⼯作时的⽂件结构和⽂件结构在内存中的对应对象。下 ⾯我们可以来开始分析DataNode上的动态⾏为。⾸先我们来分析DataXceiverServer和DataXceiver。 DataNode上数据块的接受/发送并没有采⽤我们前⾯介绍的RPC机制，原因很简单，RPC是⼀个命令式 的接⼝，⽽DataNode处理数据部分，往往是⼀种流式机制。DataXceiverServer和DataXceiver就是这 个机制的实现。其中，DataXceiver还依赖于两个辅助类：BlockSender和BlockReceiver。下⾯是类 图： （为了简单起⻅，BlockSender和BlockReceiver的成员变量没有进⼊UML模型中） DataXceiverServer很简单，它打开⼀个端⼝，然后每接收到⼀个连接，就创建⼀个DataXceiver，服 务于该连接，并记录该连接的socket，对应的实现在DataXceiverServer的run⽅法⾥。当系统关闭 时，DataXceiverServer将关闭监听的socket和所有DataXceiver的socket，这样就导致了DataXceiver 出错并结束线程。 DataXceiver才是真正⼲活的地⽅，⽬前，DataXceiver⽀持的操作总共有六条，分别是： OP_WRITE_BLOCK (80)：写数据块 OP_READ_BLOCK (81)：读数据块 OP_READ_METADATA (82)：读数据块元⽂件 OP_REPLACE_BLOCK (83)：替换⼀个数据块 OP_COPY_BLOCK (84)：拷⻉⼀个数据块 OP_BLOCK_CHECKSUM (85)：读数据块检验码 DataXceiver⾸先读取客户端的版本号并检验，然后再读取⼀个字节的操作码，并转⼊相关的⼦程序进 ⾏处理。我们先看⼀下读数据块的过程吧。 ⾸先看输⼊，下图是读数据块时，客户端发送过来的信息： 包括了要读取的Block的ID，时间戳，开始偏移和读取的⻓度，最后是客户端的名字（貌似只是在写⽇ 志的时候⽤到了）。根据上⾯的信息，我们可以创建⼀个BlockSender，如果BlockSender没有出错， 返回客户端⼀个正确指示后，否则，返回错误码。成功创建BlockSender以后，就可以开始通过 BlockSender.sendBlock发送数据。 下⾯我们就来分析BlockSender。BlockSender的构造函数看似很复杂，其实就是根据需求（特别是在 处理checksum上，因为checksum是基于块的），打开相应的数据流。close()⽤于释放各种资源，如 已经打开的数据流。sendBlock⽤于发送数据，数据发送包括应答头和后续的数据包。应答头如下（包 含DataXceiver中发送的成功标识）：

然后后⾯的数据就组织成数据包来发送，包结构如下： 各个字段含义： packetLen：包⻓度，包括包头 ofset：偏移量 seqno：包序列号 tail：是否是最后⼀个包 len：数据⻓度

checksum：检验数据 data：数据块数据 需要注意的，在写数据前，BlockSender会校验数据，保证数据包中的checksum和数据的⼀致性。同 时，如果数据出错，将会有ChecksumException抛出。 数据传输结束的标志，是⼀个packetLen⻓度为0的包。客户端可以返回⼀个两字节的应答 OP_STATUS_CHECKSUM_OK(5)

## Hadop源代码分析（⼀四）

继续DataXceiver分析，下⼀块硬⻣头是写数据块。HDFS的写数据操作，⽐读数据复杂N多倍。读数据 的时候，只需要在多个数据块⽂件的选⼀个读，就可以了；但是，写数据需要同时写到多个数据块⽂ 件上，这就⽐较复杂了。HDFS实现了了Gogle写⽂件时的机制，如下图：

数据流从客户端开始，流经⼀系列的节点，到达最后⼀个DataNode。图中的所有DataNode只需要写 ⼀次硬盘，DataNode1和DataNode2会将从socket上接受到的数据，直接写到到下个节点的socket 上。 我们来看⼀下写数据块的请求。

⾸先是客户端的版本号和⼀个字节的操作码，接下来是我们熟悉的blockId和generationStamp。参数 pipelineSize是整个数据流链的⻓度，以上⾯为例，pipelineSize=3。isRecovery指示这次写是否是⼀ 次恢复操作，还记得我们在讨论FSDataset.writeToBlock时的那个参数吗？isRecovery来⾃客户端。 client是客户端的名字，就是发起请求的节点名，需要特别注意的是，如果是从NameNode来的复制请 求，client为空。hasSrcDataNode是⼀个标志位，如果被设置，表明源节点是个DataNode，接下来 读取的数据就是DataNode的信息。numTargets是⽬标节点的数⽬，包括当前节点，以上⾯的图为例， DataNode1上这个参数值为3，到了DataNode3，就只有1了。targets包含了⽬标节点的相关信息，根 据这些信息，就可以创建到它们上⾯的socket连接。targets后跟着的是校验头。 writeBlock最开始是处理上⾯提到的消息包，然后创建⼀个BlockReceiver。接下来就是创建⼀堆⽤于 读写的流，如下图（图中除了in外，都是在writeBlock中创建，这个图还不涉及在BlockReceiver对本 地⽂件读写的流）：

在进⾏实际的数据写之前，上⾯的这些流会被建⽴起来（也就是说，DataNode1到DataNode3都可写 以后，才开始处理写数据）。如果其中某⼀个点出错了，那么，出错的节点名会通过mirorIn发送回 来，⼀直沿着这条链，传播到客户端。 如果⼀切正常，那么，BlockReceiver.receiveBlock就开始⼲活了。 BlockReceiver的构造函数会创建写数据块和校验数据的输出流。剩下的就交给receiveBlock这个⼤家 伙了。⾸先receiveBlock会再启动⼀个线程（⼀般来说，BlockReceiver就跑在它⾃⼰的线程上），⽤ 于处理应答（内部类PacketResponder定义了该线程），然后就不断调⽤receivePacket读数据。 数据是以分块的形式传送，格式和读Block的时候是⼀样的。如下图（很奇怪，为啥不抽象为类）：

注意：如果当前DataNode处于数据流的中间，该数据包会发送到下⼀个节点。 接下来的处理，就是处理数据和校验，并分别写到数据块⽂件和数据块元数据⽂件。如果出错，抛出 的异常会导致receiveBlock关闭相关的输出流，并终⽌传输。注意，数据校验出错还会上报到 NameNode上。 PacketResponder⽤于处理应答。也就是上⾯讲的mirorIn和replyOut。PacketResponder⾥有⼀个队 列ackQueue，receivePacket每收到⼀个包，都会往队列⾥添加⼀项。PacketResponder的run⽅法， 根据⼯作的DataNode所处的位置，⾏为不⼀样。 最后⼀个DataNode由于没有后续节点，PacketResponder的ackQueue每收到⼀项，表明对应的数据 块已经处理完毕，那么就可以发送成功应答。如果该应答是最后⼀个包的，PacketResponder会关闭 相关的输出流，并提交（前⾯讲FSDataset时后我们讨论过的finalizeBlock⽅法）。 如果DataNode有后续节点，那么，它必须等到后续节点的成功应答，才可以发送应答到它前⾯的节 点。 PacketResponder的run⽅法还引⼊了⼼跳机制，⽤于检测连接是否还存在。

注意：所有改变DataNode的操作，需要把信息更新到NameNode上，这是通过 DataNode.notifyNamenodeReceivedBlock⽅法，然后通过DataNode统⼀发送到NameNode上。

## Hadop源代码分析（⼀五）

DataXceiver⽀持的的6条操作，我们已经分析完最重要的两条。剩下的分别是： OP_READ_METADATA (82)：读数据块元⽂件 OP_REPLACE_BLOCK (83)：替换⼀个数据块 OP_COPY_BLOCK (84)：拷⻉⼀个数据块 OP_BLOCK_CHECKSUM (85)：读数据块检验码 我们逐个讨论。 读数据块元⽂件的请求如图（操作码82）：

应答很简单，应答码（如OP_STATUS_SUCES），⽂件⻓度（int），数据。 拷⻉数据块和替换数据块是⼀对相对应操作。 替换数据块的请求如图（操作码83）。这个⽐起上⾯的读数据块元⽂件请求，有点复杂。替换⼀个数 据块是系统平衡操作的⼀部分，⽤于接收⼀个数据块。它和普通的数据块写的差别是，它只发⽣在两 个节点上，⼀个写，⼀个读，⽽不需要建⽴数据链。我们可以⽐较⼀下它们在创建BlockReceiver对象 时的差别：

Java代码

- 1.
- 2.
- 3.
- 4.


blockReceiver = new BlockReceiver(block, proxyReply, proxySock.getRemoteSocketAdres().toString(), proxySock.getLocalSocketAdres().toString(), false, ", nul, datanode); /OP_REPLACE_BLOCK

5. 6. 7. 8.

blockReceiver = new BlockReceiver(block, in, s.getRemoteSocketAdres().toString(), s.getLocalSocketAdres().toString(), isRecovery, client, srcDataNode, datanode); /OP_WRITE_BLOCK

blockReceiver = new BlockReceiver(block, proxyReply, proxySock.getRemoteSocketAdres().toString(), proxySock.getLocalSocketAdres().toString(), false, ", nul, datanode); /OP_REPLACE_BLOCK

blockReceiver = new BlockReceiver(block, in, s.getRemoteSocketAdres().toString(), s.getLocalSocketAdres().toString(), isRecovery, client, srcDataNode, datanode); /OP_WRITE_BLOCK

⾸先，proxyReply和in不⼀样，这是因为发起请求的节点和提供数据的节点并不是同⼀个。写数据块发 起请求⽅也提供数据，替换数据块请求⽅不提供数据，⽽是提供了⼀个数据源（proxySource参数）， 由replaceBlock发起⼀个拷⻉数据块的请求，建⽴数据源。对于拷⻉数据块操作，isRecovery=false， client=”， srcDataNode=nul。注意，我们在分析BlockReceiver是，讨论过client=”的情况，就是应 ⽤于这种场景。 在创建BlockReceiver对象前，需要利⽤下⾯介绍的拷⻉数据块的请求建⽴到数据源的socket连接并发 送拷⻉数据块请求。然后通过BlockReceiver.receiveBlock接收数据。任务成功后将结果通知 notifyNamenodeReceivedBlock。 拷⻉数据块的请求如图（操作码84）。和读数据块操作请求类似，但是读取的是整个数据块，所以少 了很多参数。

读数据块检验码的请求如图（操作码85）。它能够读取某个数据块的检验和的MD5结果，实现的⽅法 很简单。

## Hadop源代码分析（⼀六）

通过上⾯的讨论，DataNode上的读/写流程已经基本清楚了。我们来看下⼀个⾮主流流程， DataBlockScaner⽤于定时对数据块⽂件进⾏校验。类图如下： DataBlockScaner拥有它单独的线程，能定时地从⽬前DataNode管理的数据块⽂件进⾏校验。其实 最重要的⽅法就是verifyBlock，我们来看这个⽅法最关键的地⽅： Java代码

- 1.
- 2.
- 3.


blockSender = new BlockSender(block, 0, -1, false, false, true, datanode); DataOutputStream out = new DataOutputStream(new IOUtils.NulOutputStream(); blockSender.sendBlock(out, nul, throtler);

blockSender = new BlockSender(block, 0, -1, false, false, true, datanode);

DataOutputStream out = new DataOutputStream(new IOUtils.NulOutputStream(); blockSender.sendBlock(out, nul, throtler);

校验利⽤了BlockSender，因为我们知道BlockSender中，发送数据的同时，会对数据进⾏校验。 verifyBlock只需要读⼀个Block到⼀个空输出设备（NulOutputStream），如果有异常，那么校验失 败，如果正常，校验成功。 DataBlockScaner其他的辅助⽅法⽤于对DataBlockScaner管理的数据块⽂件信息进⾏增加/删除， 排序操作。同时，校验的信息还会保持在Storage上，保存在dncp_block_verification.log.cur和 dncp_block_verification.log.prev中。

## Hadop源代码分析（⼀七）

周围的障碍扫清以后，我们可以开始分析类DataNode。类图如下： public clas DataNode extendsConfigured

implementsInterDatanodeProtocol, ClientDatanodeProtocol, FSConstants, Runable 上⾯给出了DataNode的继承关系，我们发现，DataNode实现了两个通信接⼝，其中 ClientDatanodeProtocol是⽤于和Client交互的，InterDatanodeProtocol，就是我们前⾯提到的 DataNode间的通信接⼝。ipcServer（类图的左下⽅）是DataNode的⼀个成员变量，它启动了⼀个 IPC服务，这样，DataNode就能提供ClientDatanodeProtocol和InterDatanodeProtocol的能⼒了。 我们从main函数开始吧。这个函数很简单，调⽤了createDataNode的⽅法，然后就等着DataNode的 线程结束。createDataNode⾸先调⽤instantiateDataNode初始化DataNode，然后执⾏ runDatanodeDaemon。runDatanodeDaemon会向NameNode注册，如果成功，才启动DataNode线 程，DataNode就开始⼲活了。 初始化DataNode的⽅法instantiateDataNode会读取DataNode需要的配置⽂件，同时读取配置的 storage⽬录（可能有多个，看storage的讨论部分），然后把这两参数送到makeInstance中， makeInstance会先检查⽬录（存在，是⽬录，可读，可写），然后调⽤： new DataNode(conf,dirs); 接下来控制流就到了构造函数上。构造函数调⽤startDataNode，完成和DataNode相关的初始化⼯作 （注意，DataNode⼯作线程不在这个函数⾥启动）。⾸先是初始化⼀堆的配置参数，什么NameNode 地址，socket参数等等。然后，向NameNode请求配置信息（DatanodeProtocol.versionRequest）， 并检查返回的NamespaceInfo和本地的版本是否⼀致。 正常情况的下⼀步是检查⽂件系统的状态并做必要的恢复，初始化FSDataset（到这个时候，上⾯图中 storage和data成员变量已经初始化）。 然后，找⼀个端⼝并创建DataXceiverServer（run⽅法⾥启动），创建DataBlockScaner（根据需要 在oferService中启动，只启动⼀次），创建DataNode上的HtpServer，启动ipcServer。这样就结束 了DataNode相关的初始化⼯作。 在启动DataNode⼯作线程前，DataNode需要向NameNode注册。注册信息在初始化的时候已经构造 完毕，包括DataXceiverServer端⼝，ipcServer端⼝，⽂件布局版本号等重要信息。注册成功后就可以 启动DataNode线程。

DataNode的run⽅法，循环⾥有两种选择，升级（暂时不讨论）/正常⼯作。我们来看正常⼯作的 oferService⽅法。oferService也是个循环，在循环⾥，oferService会定时向NameNode发送⼼跳， 报告系统中Block状态的变化，报告DataNode现在管理的Block状态。发送⼼跳和Block状态报告时， NameNode会返回⼀些命令，DataNode将执⾏这些命令。 ⼼跳的处理⽐较简单，以heartBeatInterval间隔发送。 Block状态变化报告，会利⽤保存在receivedBlockList和delHints两个列表中的信息。 receivedBlockList表明在这个DataNode成功创建的新的数据块，⽽delHints，是可以删除该数据块的 节点。如在DataXceiver的replaceBlock中，有调⽤： datanode.notifyNamenodeReceivedBlock(block,sourceID) 这表明，DataNode已经从sourceID上接收了⼀个Block，sourceID上对应的Block可以删除了（这个场 景出现在当系统需要做负载均衡时，Block在DataNode之间拷⻉）。 Block状态变化报告通过NameNode.blockReceived来报告。 Block状态报告也⽐较简单，以blockReportInterval间隔发送。 ⼼跳和Block状态报告可以返回命令，这也是NameNode先DataNode发起请求的唯⼀⽅法。我们来看 ⼀下都有那些命令：

DNA_TRANSFER：拷⻉数据块到其他DataNode DNA_INVALIDATE：删除数据块（简单⽅法） DNA_SHUTDOWN：关闭DataNode（简单⽅法） DNA_REGISTER：DataNode重新注册（简单⽅法） DNA_FINALIZE：提交升级（简单⽅法） DNA_RECOVERBLOCK：恢复数据块

拷⻉数据块到其他DataNode由transferBlocks⽅法执⾏。注意，返回的命令可以包含多个数据块，每 ⼀个数据块可以包含多个⽬标地址。transferBlocks⽅法将为每⼀个Block启动⼀个DataTransfer线程， ⽤于传输数据。 DataTransfer是⼀个DataNode的内部类，它利⽤我们前⾯介绍的OP_WRITE_BLOCK写数据块操作， 发送数据到多个⽬标上⾯。 恢复数据块和NameNode的租约（lease）恢复有关，我们后⾯再讨论。

## Hadop源代码分析（⼀⼋）

DataNode的介绍基本告⼀段落。我们开始来分析NameNode。相⽐于DataNode，NameNode⽐较复 杂。系统中只有⼀个NameNode，作为系统⽂件⽬录的管理者和“inode表”（熟悉UNIX的同学们应该了 解inode）。为了⾼可⽤性，系统中还存在着从NameNode。 先前我们分析DataNode的时候，关注的是数据块。NameNode作为HDFS中⽂件⽬录和⽂件分配的管 理者，它保存的最重要信息，就是下⾯两个映射： ⽂件名à数据块 数据块àDataNode列表 其中，⽂件名à数据块保存在磁盘上（持久化）；但NameNode上不保存数据块àDataNode列表，该列 表是通过DataNode上报建⽴起来的。

下图包含了NameNode和DataNode往外暴露的接⼝，其中，DataNode实现了InterDatanodeProtocol 和ClientDatanodeProtocol，剩下的，由NameNode实现。

ClientProtocol提供给客户端，⽤于访问NameNode。它包含了⽂件⻆度上的HDFS功能。和GFS⼀ 样，HDFS不提供POSIX形式的接⼝，⽽是使⽤了⼀个私有接⼝。⼀般来说，程序员通过 org.apache.hadop.fs.FileSystem来和HDFS打交道，不需要直接使⽤该接⼝。 DatanodeProtocol：⽤于DataNode向NameNode通信，我们已经在DataNode的分析过程中，了解部 分接⼝，包括：register，⽤于DataNode注册；sendHeartbeat/blockReport/blockReceived，⽤于 DataNode的oferService⽅法中；erorReport我们没有讨论，它⽤于向NameNode报告⼀个错误的 Block，⽤于BlockReceiver和DataBlockScaner；nextGenerationStamp和 comitBlockSynchronization⽤于lease管理，我们在后⾯讨论到lease时，会统⼀说明。 NamenodeProtocol⽤于从NameNode到NameNode的通信。 下图补充了接⼝⾥使⽤的数据的关系。

⼤⼩: 12 KB

⼤⼩: 15.5 KB

## Hadop源代码分析（⼀九）

我们先分析INode*.java，类INode*抽象了⽂件层次结构。如果我们对⽂件系统进⾏⾯向对象的抽象， ⼀定会得到和下⾯⼀样类似的结构图（类INode*）： INode是⼀个抽象类，它的两个字类，分别对应着⽬录（INodeDirectory）和⽂件（INodeFile）。 INodeDirectoryWithQuota，如它的名字隐含的，是带了容量限制的⽬录。 INodeFileUnderConstruction，抽象了正在构造的⽂件，当我们需要在HDFS中创建⽂件的时候，由于 创建过程⽐较⻓，⽬录系统会维护对应的信息。 INode中的成员变量有：name，⽬录/⽂件名；modificationTime和acesTime是最后的修改时间和访 问时间；parent指向了⽗⽬录；permision是访问权限。HDFS采⽤了和UNIX/Linux类似的访问控制机 制。系统维护了⼀个类似于UNIX系统的组表（group）和⽤户表（user），并给每⼀个组和⽤户⼀个 ID，permision在INode中是long型，它同时包含了组和⽤户信息。 INode中存在⼤量的get和set⽅法，当然是对上⾯提到的属性的操作。导出属性，⽐较重要的有： colectSubtreBlocksAndClear，⽤于收集这个INode所有后继中的Block； computeContentSumary⽤于递归计算INode包含的⼀些相关信息，如⽂件数，⽬录数，占⽤磁盘空 间。 INodeDirectory是HDFS管理的⽬录的抽象，它最重要的成员变量是： privateList<INode> children; 就是这个⽬录下的所有⽬录/⽂件集合。INodeDirectory也是有⼤量的get和set⽅法，都很简单。 INodeDirectoryWithQuota进⼀步加强了INodeDirectory，限制了INodeDirectory可以使⽤的空间（包 括NameSpace和磁盘空间）。 INodeFile是HDFS中的⽂件，最重要的成员变量是： protectedBlockInfo blocks[] = nul;

这是这个⽂件对应的Block列表，BlockInfo增强了Block类。 INodeFileUnderConstruction保存了正在构造的⽂件的⼀些信息，包括clientName，这是⽬前拥有租 约的节点名（创建⽂件时，只有⼀个节点拥有租约，其他节点配合这个节点⼯作）。clientMachine是 构造该⽂件的客户端名称，如果构造请求由DataNode发起，clientNode会保持相应的信息，targets保 存了配合构造⽂件的所有节点。 上⾯描述了INode*类的关系。下⾯我们顺便考察⼀下⼀些NameNode上的数据类。 BlocksMap保存了Block和它在NameNode上⼀些相关的信息。其核⼼是⼀个map：Map<Block, BlockInfo>。BlockInfo扩展了Block，保存了该Block归属的INodeFile和DatanodeDescriptor，同时还 包括了它的前继和后继Block。有了BlocksMap，就可以通过Block找对应的⽂件和这个Block存放的 DataNode的相关信息。

接下来我们来分析类Datanode*。DatanodeInfo和DatanodeID都定义在包 org.apache.hadop.hdfs.protocol。DatanodeDescriptor是DatanodeInfo的⼦类，包含了NameNode 需要的附加信息。DatanodeID只包含了⼀些配置信息，DatanodeInfo增加了⼀些动态信息， DatanodeDescriptor更进⼀步，包含了DataNode上⼀些Block的动态信息。DatanodeDescriptor包含 了内部类BlockTargetPair，它保存Block和对应DatanodeDescriptor的关联，BlockQueue是 BlockTargetPair队列。 DatanodeDescriptor包含了两个BlockQueue，分别记录了该DataNode上正在复制 （replicateBlocks）和Lease恢复（recoverBlocks）的Block。同时还有⼀个Block集合，保存的是该 DataNode上已经失效的Block。DatanodeDescriptor提供⼀系列⽅法，⽤于操作上⾯保存的队列和集 合。也提供get*Comand⽅法，⽤于⽣成发送到DataNode的命令。 当NameNode收到DataNode对现在管理的Block状态的汇报是，会调⽤reportDif，找出和现在 NameNode上的信息差别，以供后续处理⽤。 readFieldsFromFSEditLog⽅法⽤于从⽇志中恢复DatanodeDescriptor。

## Hadop源代码分析（⼆零）

前⾯我们提过关系：⽂件名à数据块持久化在磁盘上，所有对⽬录树的更新和⽂件名à数据块关系的修 改，都必须能够持久化。为了保证每次修改不需要从新保存整个结构，HDFS使⽤操作⽇志，保存更 新。 现在我们可以得到NameNode需要存储在Disk上的信息了，包括： [hadop@localhostdfs]$ ls -R name name: curent image in_use.lock

name/curent: edits fsimage fstime VERSION

name/image:

fsimage in_use.lock的功能和DataNode的⼀致。fsimage保存的是⽂件系统的⽬录树，edits则是⽂件树上的操 作⽇志，fstime是上⼀次新打开⼀个操作⽇志的时间（long型）。 image/fsimage是⼀个保护⽂件，防⽌0.13以前的版本启动（0.13以前版本将fsimage存放在 name/image⽬录下，如果⽤0.13版本启动，显然在读fsimage会出错J）。 我们可以开始讨论FSImage了，类FSImage如下图：

分析FSImage，不免要跟DataStorage去做⽐较（上图也保留了类DataStorage）。前⾯我们已经分析 过DataStorage的状态变化，包括升级/回滚/提交，FSImage也有类似的升级/回滚/提交动作，⽽且这部 分的⾏为和DataStorage是⽐较⼀致，如下状态转移图。图中update⽅法和DataStorage的差别⽐较 ⼤，是因为处理数据库和处理⽂件系统名字空间不⼀样，其他的地⽅都⽐较⼀致。FSImage也能够管 理多个Storage，⽽且还能够区分Storage为IMAGE(⽬录结构)/EDITS（⽇志）/IMAGE_AND_EDITS （前⾯两种的组合）。

我们可以看到，FSImage和DataStorage都有recoverTransitionRead⽅法。FSImage的 recoverTransitionRead⽅法主要步骤是检查系统⼀致性（analyzeStorage）并尝试恢复，初始化新的 storage，然后根据启动NameNode的参数，做升级/回滚等操作。 FSImage需要⽀持参数-importCheckpoint，该参数⽤于在某⼀个checkpoint⽬录⾥加载HDFS的⽬录 信息，并更新到当前系统，该参数的主要功能在⽅法doImportCheckpoint中。该⽅法很简单，通过读 取配置的checkpoint⽬录来加载fsimage⽂件和⽇志⽂件，然后利⽤saveFSImage（下⾯讨论）保存到 当前的⼯作⽬录，完成导⼊。 loadFSImage(File curFile)⽤于在fsimage中读⼊NameNode持久化的信息，是FSImage中最重要的⽅ 法之⼀，该⽂件的结构如下：

最开始是版本号（注意，各版本⽂件布局不⼀样，⽂中分析的样本是0.17的），然后是命名空间的ID 号，⽂件个数和最⾼⽂件版本号（就是说，下⼀次产⽣⽂件版本号的初始值）。接下来就是⽂件的信 息啦，⾸先是⽂件名，然后是该⽂件的副本数，接下来是修改时间/访问时间，数据块⼤⼩，数据块数 ⽬。数据块数⽬如果⼤于0，表明这是个⽂件，那么接下来就是numBlocks个数据块（浅蓝），如果数 据块数⽬等于0，那该条⽬是⽬录，接下来是应⽤于该⽬录的quota。最后是访问控制的⼀些信息。⽂ 件信息⼀共有numFiles个，接下来是处于构造状态的⽂件的信息。（有些版本可能还会保留DataNode 的信息，但0.17已经不保存这样的信息啦）。loadFSImage(File curFile)的对应⽅法是 saveFSImage(FilenewFile)，FSImage中还有⼀系列的⽅法（⼤概7，8个）⽤于配合这两个⽅法⼯作， 我们就不再深⼊讨论了。

loadFSEdits(StorageDirectory sd)⽤于加载⽇志⽂件，并把⽇志⽂件记录的内容应⽤到NameNode， loadFSEdits只是简单地调⽤FSEditLog中对应的⽅法。 loadFSImage()和saveFSImage()是另外⼀对重要的⽅法。 loadFSImage()会在所有的Storage中，读取最新的NameNode持久化信息，并应⽤相应的⽇志，当 loadFSImage()调⽤返回以后，内存中的⽬录树就是最新的。loadFSImage()会返回⼀个标记，如果 Storage中有任何和内存中最终⽬录树中不⼀致的Image（最常⻅的情况是⽇志⽂件不为空，那么，内 存中的Image应该是Storage的Image加上⽇志，当然还有其它情况），那么，该标记为true。 saveFSImage()的功能正好相反，它将内存中的⽬录树持久化，很⾃然，⽬录树持久化后就可以把⽇志 清空。saveFSImage()会创建edits.new，并把当前内存中的⽬录树持久化到fsimage.ckpt（fsimage现 在还存在），然后重新打开⽇志⽂件edits和edits.new，这会导致⽇志⽂件edits和edits.new被清空。 最后，saveFSImage()调⽤rolFSImage()⽅法。 rolFSImage()上来就把所有的edits.new都改为edits（经过了⽅法saveFSImage，它们都已经为空）， 然后再把fsimage.ckpt改为fsimage。如下图：

为了防⽌误调⽤rolFSImage()，系统引⼊了状态CheckpointStates.UPLOAD_DONE。 有了上⾯的状态转移图，我们就很好理解⽅法recoverInteruptedCheckpoint了。

图中存在另⼀条路径，应⽤于GetImageServlet中。GetImageServlet是和从NameNode进⾏⽂件通信 的接⼝，这个场景留到我们分析从NameNode时再进⾏分析。

最后我们分析⼀下和检查点相关的⼀个类，rolFSImage()会返回这个类的⼀个实例。 CheckpointSignature⽤于标识⼀个⽇志的检查点，它是StorageInfo的⼦类，同时实现了 WritableComparable接⼝，出了StorageInfo的信息，它还包括了两个属性：editsTime和 checkpointTime。editsTime是⽇志的最后修改时间，checkpointTime是⽇志建⽴时间。在和从 NameNode节点的通信中，需要⽤CheckpointSignature，来保证从NameNode获得的⽇志是最新的。

## Hadop源代码分析（⼆⼀）

不好意思，突然间需要忙项⽬的其他事情了，更新有点慢下来，争取⽉底搞定HDFS吧。 我们来分析FSEditLog.java，该类提供了NameNode操作⽇志和⽇志⽂件的相关⽅法，相关类图如下： ⾸先是FSEditLog依赖的输⼊/输出流。输⼊流基本上没有新添加功能；输出流在打开的时候，会写⼊ ⽇志的版本号（最前⾯的4字节），同时，每次将内存刷到硬盘时，会为⽇志尾部写⼊⼀个特殊的标识 （OP_INVALID）。 FSEditLog有打开/关闭的⽅法，它们都是很简单的⽅法，就是关闭的时候，要等待所有正在写⽇志的 操作都完成写以后，才能关闭。procesIOEror⽤于处理IO出错，⼀般这会导致对于的Storage的⽇志 ⽂件被关闭（还记得loadFSImage要找出最后写的⽇志⽂件吧，这也是提⾼系统可靠性的⼀个⽅法）， 如果系统再也找不到可⽤的⽇志⽂件，NameNode将会退出。

loadFSEdits是个⼤家伙，它读取⽇志⽂件，并把⽇志应⽤到内存中的⽬录结构中。这家伙⼤是因为它 需要处理所有类型的⽇志记录，其实就⼀⼤case语句。logEdit的作⽤和loadFSEdits相反，它向⽇志⽂ 件中写⼊⽇志记录。我们来分析⼀下什么操作需要写log，还有就是需要log那些参数： logOpenFile（OP_AD）：申请lease path(路径)/replication（副本数，⽂本形式）/modificationTime（修改时间，⽂本形式）/acesTime （访问时间，⽂本形式）/preferedBlockSize（块⼤⼩，⽂本形式）/BlockInfo[]（增强的数据块信 息，数组）/permisionStatus（访问控制信息）/clientName（客户名）/clientMachine（客户机器 名） logCloseFile（OP_CLOSE）：归还lease path/replication/modificationTime/acesTime/preferedBlockSize/BlockInfo[]/permisionStatus logMkDir（OP_MKDIR）：创建⽬录 path/modificationTime/acesTime/permisionStatus logRename（OP_RENAME）：改⽂件名 src（原⽂件名）/dst（新⽂件名）/timestamp（时间戳） logSetReplication（OP_SET_REPLICATION）：更改副本数 src/replication logSetQuota（OP_SET_QUOTA）：设置空间额度 path/nsQuota（⽂件空间额度）/dsQuota（磁盘空间额度） logSetPermisions（OP_SET_PERMI SIONS）：设置⽂件权限位 src/permisionStatus logSetOwner（OP_SET_OWNER）：设置⽂件组和主 src/username（所有者）/groupname（所在组） logDelete（OP_DELETE）：删除⽂件 src/timestamp logGenerationStamp（OP_SET_GENSTAMP）：⽂件版本序列号 genstamp（序列号） logTimes（OP_TIMES）：更改⽂件更新/访问时间 src/modificationTime/acesTime 通过上⾯的分析，我们应该清楚⽇志⽂件⾥记录了那些信息。 rolEditLog()我们在前⾯已经提到过（配合saveFSImage和rolFSImage），它⽤于关闭edits，打开⽇ 志到edits.new。purgeEditLog()的作⽤正好相反，它删除⽼的edits⽂件，然后把edits.new改名为 edits。这也是Hadop在做更新修改时经常采⽤的策略。

## Hadop源代码分析（⼆⼆）

我们开始对租约Lease进⾏分析，下⾯是类图。Lease可以认为是⼀个⽂件写锁，当客户端需要写⽂件 的时候，它需要申请⼀个Lease，NameNode负责记录那个⽂件上有Lease，Lease的客户是谁，超时 时间（分布式处理的⼀种常⽤技术）等，所有这些⼯作由下⾯3个类完成。⾄于租约过期NameNode需 要采取什么动作，并不是这部分code要完成的功能。

LeaseManager（左）管理着系统中的所有Lease（右），同时，LeaseManager有⼀个线程Monitor， ⽤于检查是否有Lease到期。 ⼀个租约由⼀个holder（客户端名），lastUpdate（上次更新时间）和paths（该客户端操作的⽂件集 合）构成。了解了这些属性，相关的⽅法就很好理解了。LeaseManager的⽅法也就很好理解，就是对 Lease进⾏操作。注意，LeaseManager的adLease并没有检查⽂件上是否已经有Lease，这个是由 LeaseManager的调⽤者来保证的，这使LeaseManager跟简单。内部类Monitor通过对Lease的最后跟 新时间来检测Lease是否过期，如果过期，简单调⽤FSNamesystem的internalReleaseLease⽅法。 这部分的代码⽐我想象的简单，主要是⼤部分的⼀致性逻辑都存在于LeaseManager的使⽤者。在开始 分析FSNamesystem.java这个4.5k多⾏的庞然⼤物之前，我们继续来扫除外围的障碍。下⾯是关于访 问控制的⼀些类：

Hadop⽂件保护采⽤的UNIX的机制，⽂件⽤户分⽂件属主、⽂件组和其他⽤户，权限读，写和执⾏ （FsAction中抽象了所有组合）。 我们先分析包org.apache.hadop.fs.permision的⼏个类吧。FsAction抽象了操作权限， FsPermision记录了某⽂件/路径的允许情况，分⽂件属主、⽂件组和其他⽤户，同时提供了⼀系列的 转换⽅法，aplyUMask⽤于去掉某些权限，如某些操作需要去掉⽂件的写权限，那么可以通过该⽅ 法，⽣成对应的去掉写权限的FsPermision对象。PermisionStatus⽤于描述⼀个⽂件的⽂件属主、 ⽂件组和它的FsPermision。 INode在保存PermisionStatus时，⽤了不同的⽅法，它⽤⼀个long变量，和SerialNumberManager配 合，保存了PermisionStatus的所有信息。 SerialNumberManager保存了⽂件主和⽂件主号，⽤户组和⽤户组号的对应关系。注意，在持久化信 息FSImage中，不保存⽂件主号和⽤户组号，它们只是SerialNumberManager分配的，只保存在内存 的信息。通过SerialNumberManager得到某⽂件主的⽂件主号时，如果找不到⽂件主号，会往对应关 系中添加⼀条记录。 INode的long变量作为⼀个位串，分组保存了FsPermision（MODE），⽂件主号（USER）和⽤户组 号（GROUP）。 PermisionChecker⽤于权限检查。

## Hadop源代码分析（⼆三）

下⾯我们来分析FSDirectory。其实分析FSDirectory最好的地⽅，应该是介绍完INode*以后， FSDirectory在INode*的基础上，保存了HDFS的⽂件⽬录状态。系统加载FSImage时，FSImage会在 FSDirectory对象上重建⽂件⽬录状态，HDFS⽂件⽬录状态的变化，也由FSDirectory写⽇志，同时， 它保存了⽂件名à数据块的映射关系。 FSDirectory只有很少的成员变量，如下：

finalFSNamesystem namesystem; finalINodeDirectoryWithQuotarotDir; FSImage fsImage; boleanready =false;

其中，namesystem，fsImage是指向FSNamesystem对象和FSImage对象的引⽤，rotDir是⽂件系统 的根，ready初值为false，当系统成功加载FSImage以后，ready会变成true，FSDirectory的使⽤者就 可以调⽤其它FSDirectory功能了。 FSDirectory中剩下的，就是⼀堆的⽅法（我们不讨论和MBean相关的类，⽅法和过程）。

loadFSImage⽤于加载⽬录树结构，它会去调⽤FSImage的⽅法，完成持久化信息的导⼊以后，它会把 成员变量ready置为true。系统调⽤loadFSImage是在FSNamesystem.java的initialize⽅法，那是系统 初始化重要的⼀步。 adFile⽤于创建⽂件或追加数据时创建INodeFileUnderConstruction，下图是它的Cal Hierachy图：

adFile⾸先会试图在系统中创建到⽂件的路径，如果⽂件为/home/hadop/Hadop.tar，adFile会调 ⽤mkdirs（创建路径为/home/hadop，这也会涉及到⼀系列⽅法），保证⽂件路径存在，然后创建 INodeFileUnderConstruction节点，并把该节点加到⽬录树中（通过adNode，也是需要调⽤⼀系列 ⽅法），如果成功，就写操作⽇志（logOpenFile）。 unprotectedAdFile也⽤于在系统中创建⼀个⽬录或⽂件（⾮UnderConstruction），如果是⽂件，还 会建⽴对应的block。FSDirectory中还有好⼏个unprotected*⽅法，它们不检查成员变量ready，不写 ⽇志，它们⼤量⽤于loadFSEdits中（这个时候ready当然是false，⽽且因为正在恢复⽇志，也不需要 写⽇志）。 adToParent添加⼀个INode到⽬录树中，并返回它的上⼀级⽬录，它的实现和unprotectedAdFile是 类似的。 persistBlocks⽐较有意思，⽤于往⽇志⾥记录某inode的block信息，其实并没有⼀个对应于 persistBlocks的写⽇志⽅法，它⽤的是logOpenFile。这个⼤家可以去检查⼀下logOpenFile记录的信 息。closeFile对应了logCloseFile。 adBlock和removeBlock对应，⽤于添加/删除数据块信息，同时它们还需要更新FSNamesystem.java 中对应的信息。 unprotectedRenameTo和renameTo实现了UNIX的mv命令，主要的功能都在unprotectedRenameTo中 完成，复杂的地⽅在于对各种各样情况的讨论。 setReplication和unprotectedSetReplication⽤于更新数据块的副本数，很简单的⽅法，注意，改变产 ⽣的对数据块的删除/复制是在FSNamesystem.java中实现。 setPermision，unprotectedSetPermision，setOwner和unprotectedSetOwner都是简单的⽅法。 Delete和unprotectedDelete⼜是⼀对⽅法，删除如果需要删除数据块，将通过FSNamesystem的 removePathAndBlocks进⾏。

…(后续的⽅法和前⾯介绍的，都⽐较类似，都是⼀些过程性的东⻄，就不再讨论了)

## Hadop源代码分析（⼆四）

下⾯轮到FSNamesystem出场了。FSNamesystem.java⼀共有4573⾏，⽽整个namenode⽬录下所有 的Java程序总共也只有16876⾏，把FSNamesystem搞定了，NameNode也就基本搞定。 FSNamesystem是NameNode实际记录信息的地⽅，保存在FSNamesystem中的数据有：

l ⽂件名à数据块列表（存放在FSImage和⽇志中） l 合法的数据块列表（上⾯关系的逆关系） l 数据块àDataNode（只保存在内存中，根据DataNode发过来的信息动态建⽴） l DataNode上保存的数据块（上⾯关系的逆关系） l 最近发送过⼼跳信息的DataNode（LRU） 我们先来分析FSNamesystem的成员变量。

private boleanisPermisionEnabled; 是否打开权限检查，可以通过配置项dfs.permisions来设置。

privateUserGroupInformation fsOwner; 本地⽂件的⽤户⽂件属主和⽂件组，可以通过hadop.job.ugi设置，如果没有设置，那么将使⽤启动 HDFS的⽤户（通过whoami获得）和该⽤户所在的组（通过groups获得）作为值。

private Stringsupergroup; 对应配置项dfs.permisions.supergroup，应⽤在defaultPermision中，是系统的超级组。

privatePermisionStatus defaultPermision; 缺省权限，缺省⽤户为fsOwner，缺省⽤户组为supergroup，缺省权限为0 7，可以通过 dfs.upgrade.permision修改。

private longcapacityTotal,capacityUsed, capacityRemaining; 系统总容量/已使⽤容量/剩余容量

private int totalLoad = 0; 系统总连接数，根据DataNode⼼跳信息跟新。

private longpendingReplicationBlocksCount, underReplicatedBlocksCount,scheduledReplicationBlocksCount; 分别是成员变量pendingReplications（正在复制的数据块），nededReplications（需要复制的数据 块）的⼤⼩，scheduledReplicationBlocksCount是当前正在处理的复制⼯作数⽬。

public FSDirectorydir; 指向系统使⽤的FSDirectory对象。

BlocksMap blocksMap=newBlocksMap(); 保存数据块到INode和DataNode的映射关系 public CoruptReplicasMap coruptReplicas =newCoruptReplicasMap();

保存损坏（如：校验没通过）的数据块到对应DataNode的关系，CoruptReplicasMap类图如下，类只 有⼀个成员变量，保存Block到⼀个DatanodeDescriptor的集合的映射和这个映射上的⼀系列操作：

Map<String, DatanodeDescriptor> datanodeMap = newTreMap<String, DatanodeDescriptor>(); 保存了StorageID à DatanodeDescriptor的映射，⽤于保证DataNode使⽤的Storage的⼀致性。

privateMap<String, Colection<Block> recentInvalidateSets 保存了每个DataNode上⽆效但还存在的数据块（StorageIDà ArayList<Block>）。

Map<String,Colection<Block> recentInvalidateSets 保存了每个DataNode上有效，但需要删除的数据块（StorageIDà TreSet<Block>），这种情况可能 发⽣在⼀个DataNode故障后恢复后，上⾯的数据块在系统中副本数太多，需要删除⼀些数据块。

HtpServer infoServer; int infoPort;

Date startTime; ⽤于内部信息传输的HTP请求服务器（Servlet的容器）。现在 有/fsck，/getimage，/listPaths/*，/data/*和/fileChecksum/*，我们后⾯还会继续讨论。

ArayList<DatanodeDescriptor>heartbeats; 所有⽬前活着的DataNode，线程HeartbeatMonitor会定期检查。 privateUnderReplicatedBlocks nededReplications 需要进⾏复制的数据块。UnderReplicatedBlocks的类图如下，它其实是⼀个数组，数组的下标是优先 级（0的优先级最⾼，如果数据块只有⼀个副本，它的优先级是0），数组的内容是⼀个Block集合。 UnderReplicatedBlocks提供⼀些⽅法，对Block进⾏增加，修改，查找和删除。

privatePendingReplicationBlocks pendingReplications; 保存正在复制的数据块的相关信息。PendingReplicationBlocks的类图如下：

其中，pendingReplications保存了所有正在进⾏复制的数据块，使⽤Map是需要⼀些附加的信息 PendingBlockInfo。这些信息包括时间戳，⽤于检测是否已经超时，和现在进⾏复制的数⽬ numReplicasInProgres。timedOutItems是超时的复制项，超时的复制项在FSNamesystem的 procesPendingReplications⽅法中被删除，并从新复制。timerThread是⽤于检测复制超时的线程的 句柄，对应的线程是PendingReplicationMonitor的⼀个实例，它的run⽅法每隔⼀段会检查是否有超时 的复制项，如果有，将该数据块加到timedOutItems中。Timeout是run⽅法的检查间隔， defaultRecheckInterval是缺省值。PendingReplicationBlocks和PendingBlockInfo的⽅法都很简单。

public LeaseManagerleaseManager =newLeaseManager(this); 租约管理器。

## Hadop源代码分析（⼆五）

继续对FSNamesystem进⾏分析。

Daemonhbthread = nul; / HeartbeatMonitor thread public Daemonlmthread = nul; / LeaseMonitor thread

Daemon s mthread = nul; / SafeModeMonitor thread public Daemon replthread = nul; / Replication thread NameNode上的线程，分别对应DataNode⼼跳检查，租约检查，安全模式检查和数据块复制，我们会 在后⾯介绍这些线程对应的功能。

volatile bolean fsRuning = true; long systemStart =0;

系统运⾏标志和系统启动时间。

接下来是⼀堆系统的参数，⽐⽅说系统每个DataNode节点允许的最⼤数据块数，⼼跳检查间隔时间 等… …

/ The maximum number ofreplicates we should alow for a single block private int maxReplication; / How many outgoing replicationstreams a given node should have at one time private intmaxReplicationStreams;

/MIN_REPLICATION is how many copies we ned in place or else we disalow thewrite private int minReplication;

/Default replication private intdefaultReplication;

/heartbeatRecheckInterval is how often namenode checks for expired datanodes private longheartbeatRecheckInterval;

/heartbeatExpireInterval is how long namenode waits for datanode to report

/heartbeat private longheartbeatExpireInterval; /replicationRecheckInterval is how often namenode checks for newreplication work private longreplicationRecheckInterval; /decomisionRecheckInterval is how often namenode checks if a node hasfinished

decomision private longdecomisionRecheckInterval; /default block size of a file private longdefaultBlockSize = 0;

private int replIndex =0; 和nededReplications配合，记录下⼀个进⾏复制的数据块位置。 public staticFSNamesystemfsNamesystemObject; 哈哈，不⽤介绍了，还是static的。

private String localMachine; private int port;

本机名字和RPC端⼝。 private SafeModeInfo safeMode; /safe mode information 记录安全模式的相关信息。 安全模式是这样⼀种状态，系统处于这个状态时，不接受任何对名字空间的修改，同时也不会对数据 块进⾏复制或删除数据块。NameNode启动的时候会⾃动进⼊安全模式，同时也可以⼿⼯进⼊（不会 ⾃动离开）。系统启动以后，DataNode会报告⽬前它拥有的数据块的信息，当系统接收到的Block信 息到达⼀定⻔槛，同时每个Block都有dfs.replication.min个副本后，系统等待⼀段时间后就离开安全模 式。这个⻔槛定义的参数包括： l dfs.safemode.threshold.pct：接受到的Block的⽐例，缺省为95%，就是说，必须DataNode报 告的数据块数⽬占总数的95%，才到达⻔槛； l dfs.replication.min：缺省为1，即每个副本都存在系统中； l dfs.replication.min：等待时间，缺省为0，单位秒。 SafeModeInfo的类图如下：

threshold，extension和safeReplication保存的是上⾯说的3个参数。Reached等于-1表明安全模式是 关闭的，0表示安全模式打开但是系统还没达到threshold。blockTotal是计算threshold时的分⺟， blockSafe是分⼦，lastStatusReport⽤于控制写⽇志的间隔。

SafeModeInfo(Configuration conf)使⽤配置⽂件的参数，是NameNode正常启动时使⽤的构造函数， SafeModeInfo()中，this.threshold = 1.5f使得系统⽤于处于安全模式。 enter()使系统进⼊安全模式，leave()会使系统离开安全模式，canLeave()⽤于检查是否能离开安全模 式⽽nedEnter()，则判断是否应该进⼊安全模式。checkMode()检查系统状态，如果必要，则进⼊安 全模式。其他的⽅法都⽐价简单，⼤多为对成员变量的访问。

讨论完类SafeModeInfo，我们来分析⼀下SafeModeMonitor，它⽤于定期检查系统是否能够离开安全 模式（s mthread就是它的⼀个实例）。系统离开安全模式后，s mthread会被重新赋值为nul。

⼤⼩: 19.3 KB ⼤⼩: 98.7 KB

## Hadop源代码分析（⼆六）

(没想到需要分⻚啦) private Host2NodesMap host2DataNodeMap = newHost2NodesMap(); 保存了主机名（String）到DatanodeDescriptor数组的映射（Host2NodesMap唯⼀的成员变量为 HashMap<String,DatanodeDescriptor[]> map，它的⽅法都是对这个map进⾏操作）。

NetworkTopology clusterMap =newNetworkTopology();

privateDNSToSwitchMaping dnsToSwitchMaping; 定义了HDFS的⽹络拓扑，⽹络拓扑对应选择数据块副本的位置很重要。如在⼀个层次型的⽹络中，接 到同⼀个交换机的两个节点间的⽹络速度，会⽐跨越多个交换机的两个节点间的速度快，但是，如果 某交换机故障，那么它对接到它上⾯的两个节点会同时有影响，但跨越多个交换机的两个节点，这种 影响会⼩得多。下⾯是NetworkTopology相关的类图： Hadop实现了⼀个树状的拓扑结构抽象，其中，Node接⼝，定义了⽹络节点的⼀些⽅法， NodeBase是Node的⼀个实现，提供了叶⼦节点的⼀些⽅法（明显它没有⼦节点），⽽I nerNode则 实现了树的内部节点，如果我们考虑⼀个⽹络部署的话，那么叶⼦节点是服务器，⽽I nerNode则是 服务器所在的机架或交换机或路由器。Node提供了对⽹络位置信息（采⽤类似⽂件树的⽅式），节点 名称和Node所在的树的深度的⽅法。NodeBase提供了⼀个简单的实现。I nerNode是 NetworkTopology的内部类，对⽐NodeBase，它的clildren保存了所有的⼦节点，这样的话，就可 以构造⼀个拓扑树。这棵树的叶⼦可能是服务器，也可能是机架，内部则是机架或者是路由器等设 备，I nerNode提供了⼀系列的⽅法区分处理这些信息。 NetworkTopology的ad⽅法和remove⽤于在拓扑结构中加⼊节点和删除节点，同时也给出⼀些 get*⽅法，⽤于获取⼀些对象内部的信息，如getDistance，可以获取两个节点的距离，⽽ isOnSameRack可以判断两个节点是否处于同⼀个机架。choseRandom有两个实现，⽤于在⼀定范 围内（另⼀个还有⼀个排除选项）随机选取⼀个节点。choseRandom在选择数据块副本位置的时候 调⽤。

DNSToSwitchMaping配合上⾯NetworkTopology，⽤于确定某⼀个节点的⽹络位置信息，它的唯 ⼀⽅法，可以通过⼀系列机器的名字找出它们对应的⽹络位置信息。⽬前有⽀持两种⽅法，⼀是通过 命令⾏⽅式，将节点名作为输⼊，输出为⽹络位置信息（RawScriptBasedMaping执⾏命令 CachedDNSToSwitchMaping缓存结果），还有⼀种就是利⽤配置参数 hadop.configured.node.maping静态配置（StaticMaping）。

ReplicationTargetChoser replicator; ⽤于为数据块备份选择⽬标，例如，⽤户写⽂件时，需要选择⼀些DataNode，作为数据块的存放位 置，这时候就利⽤它来选择⽬标地址。choseTarget是ReplicationTargetChoser中最重要的⽅ 法，它通过内部的⼀个NetworkTopology对象，计算出⼀个DatanodeDescriptor数组，该数组就是 选定的DataNode，同时，顺序就是最佳的数据流顺序（还记得我们讨论DataXceiver些数据的那个图 吗？）。

privateHostsFileReader hostsReader; 保存了系统中允许/不允许连接到NameNode的机器列表。

private Daemondnthread = nul; 线程句柄，该线程⽤于检测DataNode上的Decomision进程。例如，某节点被列⼊到不允许连接 到NameNode的机器列表中（HostsFileReader），那么，该节点会进⼊Decomision状态，它上 ⾯的数据块会被复制到其它节点，复制结束后机器进⼊ DatanodeInfo.AdminStates.DECOMI SIONED，这台机器就可以从HDFS中撤掉。

private long maxFsObjects =0; / maximum number of fsobjects 系统能拥有的INode最⼤数（配置项dfs.max.objects，0为⽆限制）。

private finalGenerationStamp generationStamp =newGenerationStamp(); 系统的时间戳⽣产器。

private intblockInvalidateLimit = FSConstants.BLOCK_INVALIDATE_CHUNK;

发送给DataNode删除数据块消息中，能包含的最⼤数据块数。⽐⽅说，如果某DataNode上有250个 Block需要被删除，⽽这个参数是10，那么⼀共会有3条删除数据块消息消息，前⾯两条包含了10 个数据块，最后⼀条是50个。

private longacesTimePrecision = 0; ⽤于控制⽂件的aces时间的精度，也就是说，⼩于这个精度的两次对⽂件访问，后⾯的那次就不做 记录了。

## Hadop源代码分析（⼆七）

我们接下来分析NameNode.java的成员变量，然后两个类综合起来，分析它提供的接⼝，并配合说明 接⼝上请求对应的处理流程。 前⾯已经介绍过了，NameNode实现了接⼝ClientProtocol，DatanodeProtocol和 NamenodeProtocol，分别提供给客户端/DataNode/从NameNode访问。由于NameNode的⼤部分功 能在类FSNamesystem中实现，那么NameNode.java的成员变量就很少了。

public FSNamesystemnamesystem; 指向FSNamesystem对象。

private Serverserver; NameNode的RPC服务器实例。

private Threademptier; 处理回收站的线程句柄。

private int handlerCount =2; 还记得我们分析RPC的服务器时提到的服务器请求处理线程（Server.Handle）吗？这个参数给出了 server中服务器请求处理线程的数⽬，对应配置参数为dfs.namenode.handler.count。

private boleansuportApends = true; 是否⽀持apend操作，对应配置参数为dfs.suport.apend。

privateInetSocketAdres nameNodeAdres = nul; NameNode的地址，包括IP地址和监听端⼝。 下⾯我们来看NameNode的启动过程。main⽅法是系统的⼊⼝，它会调⽤createNameNode创建 NameNode实例。createNameNode分析命令⾏参数，如果是FORMAT或FINALIZE，调⽤对应的⽅法 后退出，如果是其他的参数，将创建NameNode对象。NameNode的构造函数会调⽤initialize，初始化 NameNode的成员变量，包括创建RPC服务器，初始化FSNamesystem，启动RPC服务器和回收站线 程。 FSNamesystem的构造函数会调⽤initialize⽅法，去初始化上⾯我们分析过的⼀堆成员变量。⼏个重要 的步骤包括加载FSImage，设置系统为安全模式，启动各个⼯作线程和HTP服务器。系统的⼀些参数 是在setConfigurationParameters中初始化的，其中⼀些值的计算⽐较麻烦，⽽且也可能被其它部分的 code引⽤的，就独⽴出来了，如getNamespaceDirs和getNamespaceEditsDirs。initialize对应的是 close⽅法，很简单，主要是停⽌initialize中启动的线程。 对应于initialize⽅法，NameNode也提供了对应的stop⽅法，⽤于初始化时出错系统能正确地退出。 NameNode的format和finalize操作，都是先构造FSNamesystem，然后利⽤FSNamesystem的 FSImage提供的对应⽅法完成的。我们在分析FSImage.java时，已经了解了这部分的功能。

## Hadop源代码分析（⼆⼋）

万事俱备，我们可以来分析NameNode上的流程啦。

⾸先我们来看NameNode上实现的ClientProtocol，客户端通过这个接⼝，可以对⽬录树进⾏操作，打 开/关闭⽂件等。 getBlockLocations⽤于确定⽂件内容的位置，它的输⼊参数为：⽂件名，偏移量，⻓度，返回值是⼀ 个LocatedBlocks对象（如下图），它携带的信息很多，⼤部分字段我们以前都讨论过。

getBlockLocations直接调⽤NameSystem的同名⽅法。NameSystem中这样的⽅法⾸先会检查权限和 对参数进⾏检查（如偏移量和⻓度要⼤于0），然后再调⽤实际的⽅法。找LocatedBlocks先找src对应 的INode，然后通过INode的getBlocks⽅法，可以拿到该节点的Block列表，如果返回为空，表明该 INode不是⽂件，返回nul；如果Block列表⻓度为0，以空的Block数组构造返回的LocatedBlocks。 如果Block数组不为空，则通过请求的偏移量和⻓度，就可以把这个区间涉及的Block找出来，对于每 ⼀个block，执⾏： l 通过BlocksMap我们可以找到它存在于⼏个DataNode上（BlocksMap.numNodes⽅法）； l 计算包含该数据块但数据块是坏的DataNode的数⽬（通过NameSystem.countNodes⽅法，间 接访问CoruptReplicasMap中的信息）； l 计算坏数据块的数⽬（CoruptReplicasMap.numCoruptReplicas⽅法，应该和上⾯的数相 等）； l 通过上⾯的计算，我们得到现在还OK的数据块数⽬； l 从BlocksMap中找出所有OK的数据块对应的DatanodeDescriptor（DatanodeInfo的⽗类）； l 创建对应的LocatedBlock。 收集到每个数据块的LocatedBlock信息后，很⾃然就能构造LocatedBlocks对象。getBlockLocations 其实只是⼀个读的⽅法，请求到了NameNode以后只需要查表就⾏了。 create⽅法，该⽅法⽤于在⽬录树上创建⽂件（创建⽬录使⽤mkdir），需要的参数⽐较多，包括⽂件 名，权限，客户端名，是否覆盖已存在⽂件，副本数和块⼤⼩。NameNode的create调⽤ NameSystem的startFile⽅法（startFile需要的参数clientMachine从线程局部变量获取）。 startFile⽅法先调⽤startFileInternal完成操作，然后调⽤logSync，等待⽇志写完后才返回。 startFileInternal不但服务于startFile，也被apendFile调⽤（通过参数apend区分）。⽅法的最开始 是⼀堆检查，包括：安全模式，⽂件名src是否正确，权限，租约，replication参数，overwrite参数 （对apend操作是判断src指向是否存在并且是⽂件）。租约检查很简单，如果通过 FSDirectory.getFileINode(src)得到的⽂件是出于构造状态，表明有客户正在操作该⽂件，这时会抛出 异常AlreadyBeingCreatedException。

如果对于创建操作，会通过FSDirectory的adFile往⽬录树上添加⼀个⽂件并在租约管理器⾥添加⼀条 记录。 对于apend操作，执⾏的是构造⼀个新的INodeFileUnderConstruction并替换原有的节点，然后在租 约管理器⾥添加⼀条记录。 总的来说，最简单的create流程就是在⽬录树上创建⼀个INodeFileUnderConstruction对象并往租约管 理器⾥添加⼀条记录。

我们顺便分析⼀下apend吧，它的返回值是LocatedBlock，⽐起getBlockLocations，它只需要返回 数组的⼀项。apendFile是NameSystem的实现⽅法，它⾸先调⽤上⾯讨论的startFileInternal⽅法 （已经在租约管理器⾥添加了⼀条记录）然后写⽇志。然后寻找对应⽂件INodeFile中记录的最后⼀个 block，并通过BlocksMap.getStoredBlock()⽅法得到BlockInfo，然后再从BlocksMap中获得所有的 DatanodeDescriptor，就可以构造LocatedBlock了。需要注意的，如果该Block在需要被复制的集合 （UnderReplicatedBlocks）中，移除它。 如果⽂件刚被创建或者是最后⼀个数据块已经写满，那么apend会返回nul，这是客户端需要使⽤ adBlock，为⽂件添加数据块。

## Hadop源代码分析（⼆九）

public boleansetReplication(String src, short replication ) throwsIOException;

setReplication，设置⽂件src的副本数为replication，返回值为bolean，在FSNameSystem中，调⽤ ⽅法setReplicationInternal，然后写⽇志。 setReplicationInternal上来⾃然是检查参数了，然后通过FSDirectory的setReplication，设置新的副本 数，并获取⽼的副本数。根据新旧数，决定删除/复制数据块。 增加副本数通过调⽤updateNededReplications，为了获取UnderReplicatedBlocks. update需要的参 数，FSNameSystem提供了内部⽅法countNodes和getReplication，获得对应的数值（这两个函数都 很简单）。 procesOverReplicatedBlock⽤于减少副本数，它被多个⽅法调⽤：

主要参数有block，副本数，⽬标DataNode，源DataNode（⽤于删除）。 procesOverReplicatedBlock⾸先找出block所在的，处于⾮Decomision状态的DataNode的信 息，然后调⽤choseExcesReplicates。choseExcesReplicates执⾏： l 按机架位置，对DatanodeDescriptor进⾏分组； l 将DataNode分为两个集合，分别是⼀个机架包含⼀个以上的数据块的和剩余的； l 选择可以删除的数据块（顺序是：源DataNode，同⼀个机架上的，剩余的），把它加到 recentInvalidateSets中。

public voidsetPermision(String src, FsPermision permision

)throwsIOException; setPermision，⽤于设置⽂件的访问权限。⾮常简单，⾸先检查是否有权限，然后调⽤ FSDirectory.setPermision修改⽂件访问权限。

public void setOwner(Stringsrc, String username, String groupname

) throws IOException; public voidsetTimes(String src, longmtime,long atime) throws IOException; public void setQuota(Stringpath,longnamespaceQuota,longdiskspaceQuota)

throws IOException; setOwner，设置⽂件的⽂件主和⽂件组，setTimes，设置⽂件的访问时间和修改时间，setQuota,设 置某路径的空间限额和空间额度，和setPermision类似，调⽤FSDirectory的对应⽅法，简单。

public boleansetSafeMode(FSConstants.SafeModeAction action)throws IOException; 前⾯我们已经介绍了NameNode的安全模式，客户端通过上⾯的⽅法，可以让NameNode进⼊ （SAFEMODE_ENTER）/退出（SAFEMODE_LEAVE）安全模式或查询（SAFEMODE_GET）状态。 FSNamesystem的setSafeMode处理这个命令，对于进⼊安全模式的请求，如果系统现在不处于安全 模式，那么创建⼀个SafeModeInfo对象（创建的这个对象有别于启动时创建的那个SafeModeInfo，它 不会⾃动退出，因为threshold=1.5f），这标志着系统进⼊安全模式。退出安全模式很简单，将 safeMode赋空就可以啦。

public FileStatus[]getListing(String src)throwsIOException; 分析完set*以后，我们来看get*。getListing对应于UNIX系统的ls命令，返回值是FileStatus数组， FileStatus的类图如下，它其实给出了⽂件的详细信息，如⼤⼩，⽂件主等等。其实，这些信息都存在 INode*中，我们只需要把这些信息搬到FileStatus中就OK啦。FSNamesystem和FSDirectory中都有同 名⽅法，真正⼲活的地⽅在FSDirectory中。getListing不需要写⽇志。

public long[] getStats() throwsIOException; getStatus得到的是⽂件系统的信息，UNIX对应命令为du，它的实现更简单，所有的信息都存放在 FSNamesystem对象⾥。

publicDatanodeInfo[]getDatanodeReport(FSConstants.DatanodeReportType type) throws IOException;

getDatanodeReport，获取当前DataNode的状态，可能的选项有DatanodeReportType.AL, IVE和 DEAD。FSNamesystem的同名⽅法调⽤getDatanodeListForReport，通过HostsFileReader读取对应 信息。

public longgetPreferedBlockSize(String filename) throwsIOException; getPreferedBlockSize，返回INodeFile.preferedBlockSize，数据块⼤⼩。

public FileStatusgetFileInfo(String src)throwsIOException; 和getListing类似，不再分析。

publicContentSumary getContentSumary(String path) throwsIOException; 得到⽂件树的⼀些信息，如下图：

public void metaSave(Stringfilename)throwsIOException; 这个也很简单，它把系统的metadata输出/添加到指定⽂件上（NameNode所在的⽂件系统）。

## Hadop源代码分析（三零）

软柿⼦都捏完了，我们开始啃硬⻣头。前⾯已经分析过getBlockLocations，create，apend， setReplication，setPermision和setOwner，接下来我们继续回来讨论和⽂件内容相关的操作。

public voidabandonBlock(Block b, String src, String holder

) throws IOException; abandonBlock⽤于放弃⼀个数据块。普通的⽂件系统中并没有“放弃”操作，HDFS出现放弃数据块的 原因，如下图所示。当客户端通过其他操作（如下⾯要介绍的adBlock⽅法）获取LocatedBlock后， 可以打开到⼀个block的输出流，由于从DataNode出错到NameNode发现这个信息，需要有⼀段时间 （NameNode⻓时间收到DataNode⼼跳），打开输出流可能出错，这时客户端可以向NameNode请求 放弃这个数据块。

abandonBlock的处理不是很复杂，⾸先检查租约（调⽤checkLease⽅法。block对应的⽂件存在，⽂ 件处于构造状态，租约拥有者匹配），如果通过检查，调⽤FSDirectory的removeBlock，从 INodeFileUnderConstruction/BlocksMap/CoruptReplicasMap中删除block，然后通过logOpenFile() 记录变化（logOpenFile真是万能啊）。

public LocatedBlockadBlock(String src, String clientName) throwsIOException; 写HDFS的⽂件时，如果数据块被写满，客户端可以通过adBlock创建新的数据块。具体的创建⼯作由 FSNamesystem的getAditionalBlock⽅法完成，当然上来就是⼀通检查（是否安全模式，命名/存储空 间限额，租约，数据块副本数，保证DataNode已经上报数据块状态），然后通过 ReplicationTargetChoser，选择复制的⽬标（如果⽬标数不够副本数，⼜是⼀个异常），然后，就可 以分配数据块了。alocateBlock创建⼀个新的Block对象，然后调⽤adBlock，检查参数后把数据块加 到BlocksMap对象和对应的INodeFile对象中。alocateBlock返回后，getAditionalBlock还会继续更新 ⼀些需要记录的信息，最后返回⼀个新构造的LocatedBlock。

public boleancomplete(String src, String clientName) throwsIOException;

当客户端完成对数据块的写操作后，调⽤complete完成写操作。⽅法complete如果返回是false，那 么，客户端需要继续调⽤complete⽅法。 FSNamesystem的同名⽅法调⽤completeFileInternal，它会： l 检查环境； l 获取src对应的INode； l 如果INode存在，并且处于构造状态，获取数据块； l 如果获取数据块返回空，返回结果CompleteFileStatus.OPERATION_FAILED，FSNamesystem 的complete会抛异常返回； l 如果上报⽂件完成的DataNode数不够系统最⼩的副本数，返回STI L_WAITING； l 调⽤finalizeINodeFileUnderConstruction； l 返回成功COMPLETE_SUCES 其中，对finalizeINodeFileUnderConstruction的处理包括： l 释放租约； l 将对应的INodeFileUnderConstruction对象转换为INodeFile对象，并在FSDirectory进⾏替换； l 调⽤FSDirectory.closeFile关闭⽂件，其中会写⽇志logCloseFile(path, file)。 l 检查副本数，如果副本数⼩于INodeFile中的⽬标数，那么添加数据块复制任务。 我们可以看到，complete⼀个⽂件还是⽐较复杂的，需要释放很多的资源。

public voidreportBadBlocks(LocatedBlock[] blocks) throwsIOException; 调⽤reportBadBlocks的地⽅⽐较多，客户端可能调⽤，DataNode上也可能调⽤。

由于上报的是个数组，reportBadBlocks会循环处理，调⽤FSNamesystem的markBlockAsCorupt⽅ 法。markBlockAsCorupt⽅法需要两个参数，blk（数据块）和dn（所在的DataNode信息）。如果系 统⽬前副本数⼤于要求，那么直接调⽤invalidateBlock⽅法。 ⽅法invalidateBlock很简单，在检查完系统环境以后，先调⽤adToInvalidates⽅法往 FSNamesystem.recentInvalidateSets添加⼀项，然后调⽤removeStoredBlock⽅法。 removeStoredBlock被多个⽅法调⽤，它会执⾏： l 从BlocksMap中删除记录removeNode(block,node)； l 如果⽬前系统中还有其他副本，调⽤decrementSafeBlockCount（可能的调整安全模式参数） 和updateNededReplications（跟新可能存在的block复制信息，例如，现在系统中需要复制1个数据 块，那么更新后，需要复制2个数据块）； l 如果⽬前系统中有多余数据块等待删除（在excesReplicateMap中），那么移除对应记录； l 删除在CoruptReplicasMap中的记录（可能有）。

removeStoredBlock其实也是涉及了多处表操作，包括BlocksMap，excesReplicateMap和 CoruptReplicasMap。

我们回到markBlockAsCorupt，如果系统⽬前副本数⼩于要求，那么很显然，我们需要对数据块进⾏ 复制。⾸先将现在的数据块加⼊到CoruptReplicasMap中，然后调⽤updateNededReplications，跟 新复制信息。 markBlockAsCorupt这个流程太复杂了，我们还是画个图吧：

⼤⼩: 96.2 KB ⼤⼩: 57.9 KB ⼤⼩: 34.9 KB ⼤⼩: 31.8 KB

## Hadop源代码分析（三⼀）

下⾯是和⽬录树相关的⽅法。

public boleanrename(String src, String dst) throwsIOException; 更改⽂件名。调⽤FSNamesystem的renameTo，⼲活的是renameToInternal，最终调⽤FSDirectory的 renameTo⽅法，如果成功，更新租约的⽂件名，如下：

changeLease(src, dst, dinfo); public boleandelete(String src)throwsIOException; public boleandelete(String src,boleanrecursive)throwsIOException;

第⼀个已经废弃不⽤，使⽤第⼆个⽅法。 最终使⽤deleteInternal，该⽅法调⽤FSDirectory.delete()。

public boleanmkdirs(String src, FsPermision masked)throwsIOException; 在做完⼀系列检查以后，调⽤FSDirectory.mkdirs()。

publicFileStatus[] getListing(String src)throws IOException; 前⾯我们已经讨论了。

下⾯是其它和系统维护管理的⽅法。 public voidrenewLease(String clientName) throws IOException; 就是调⽤了⼀下leaseManager.renewLease(holder)，没有其他的事情需要做，简单。

public void refreshNodes() throwsIOException; 还记得我们前⾯分析过NameNode上有个DataNode在线列表和DataNode离线列表吗，这个命令可以 让NameNode从新读这两个⽂件。当然，根据前后DataNode的状态，⼀共有4种情况，其中有3种需 要修改。 对于从⼯作状态变为离线的，需要将上⾯的DataNode复制到其他的DataNode，需要调⽤ updateNededReplications⽅法（前⾯我们已经讨论过这个⽅法了）。 对于从离线变为⼯作的DataNode，只需要改变⼀下状态。

public voidfinalizeUpgrade()throwsIOException;

finalize⼀个升级，确认客户端有超级⽤户权限以后，调⽤FSImage.finalizeUpgrade()。

public void fsync(Stringsrc, String client) throwsIOException; 将⽂件信息持久化。在检查租约信息后，调⽤FSDirectory的persistBlocks，将⽂件的原信息通过 logOpenFile(path, file)写⽇志。

## Hadop源代码分析（三⼆）

搞定ClientProtocol，接下来是DatanodeProtocol部分。接⼝如下：

public DatanodeRegistration register(DatanodeRegistration nodeReg ) throwsIOException ⽤于DataNode向NameNode登记。输⼊和输出参数都是DatanodeRegistration，类图如下：

前⾯讨论DataNode的时候，我们已经讲过了DataNode的注册过程，我们来看NameNode的过程。下 ⾯是主要步骤： l 检查该DataNode是否能接⼊到NameNode； l 准备应答，更新请求的DatanodeID； l 从datanodeMap（保存了StorageID à DatanodeDescriptor的映射，⽤于保证DataNode使⽤的 Storage的⼀致性）得到对应的DatanodeDescriptor，为nodeS； l 从Host2NodesMap（主机名到DatanodeDescriptor数组的映射）中获取DatanodeDescriptor， 为nodeN； l 如果nodeN!=nul同时nodeS!=nodeN（后⾯的条件表明表明DataNode上使⽤的Storage发⽣变 化），那么我们需要先在系统中删除nodeN（removeDatanode，下⾯再讨论），并在 Host2NodesMap中删除nodeN； l 如果nodeS存在，表明前⾯已经注册过，则：

- 1. 更新⽹络拓扑（保存在NetworkTopology），⾸先在NetworkTopology中删除nodeS，然后跟新 nodeS的相关信息，调⽤resolveNetworkLocation，获得nodeS的位置，并从新加到NetworkTopology ⾥；
- 2. 更新⼼跳信息（register也是⼼跳）； l 如果nodeS不存在，表明这是⼀个新注册的DataNode，执⾏


- 1. 如果注册信息的storageID为空，表明这是⼀个全新的DataNode，分配storageID；
- 2. 创建DatanodeDescriptor，调⽤resolveNetworkLocation，获得位置信息；
- 3. 调⽤unprotectedAdDatanode（后⾯分析）添加节点；


- 4. 添加节点到NetworkTopology中；
- 5. 添加到⼼跳数组中。 上⾯的过程，我们遗留了两个⽅法没分析，removeDatanode的流程如下： l 更新系统的状态，包括capacityTotal，capacityUsed，capacityRemaining和totalLoad； l 从⼼跳数组中删除节点，并标记节点isAlive属性为false； l 从BlocksMap中删除这个节点上的所有block，⽤了（三零）分析到的removeStoredBlock⽅ 法； l 调⽤unprotectedAdDatanode； l 从NetworkTopology中删除节点信息。 unprotectedAdDatanode很简单，它只是更新了Host2NodesMap的信息。


## Hadop源代码分析（三三）

下⾯来看⼀个⼤家伙： public DatanodeComand sendHeartbeat(DatanodeRegistration nodeReg,

long capacity, long dfsUsed, longremaining, intxmitsInProgres, int xceiverCount)throws IOException

DataNode发送到NameNode的⼼跳信息。细⼼的⼈会发现，请求的内容还是DatanodeRegistration， 应答换成DatanodeComand了。DatanodeComand类图如下： 前⾯介绍DataNode时，已经分析过了DatanodeComand⽀持的命令：

DNA_TRANSFER：拷⻉数据块到其他DataNode DNA_INVALIDATE：删除数据块 DNA_SHUTDOWN：关闭DataNode DNA_REGISTER：DataNode重新注册 DNA_FINALIZE：提交升级 DNA_RECOVERBLOCK：恢复数据块

有了上⾯这些基础，我们来看FSNamesystem.handleHeartbeat的处理过程： l 调⽤getDatanode⽅法找对应的DatanodeDescriptor，保存于变量nodeinfo（可能为nul）中， 如果现有NameNode上记录的StorageID和请求的不⼀样，返回DatanodeComand.REGISTER，让 DataNode从新注册。 l 如果发现当前节点需要关闭（已经isDecomisioned），抛异常 DisalowedDatanodeException。

l nodeinfo是空或者现在状态不是活的，返回DatanodeComand.REGISTER，让DataNode从新 注册。 l 更新系统的状态，包括capacityTotal，capacityUsed，capacityRemaining和totalLoad； l 接下来按顺序看有没有可能的恢复数据块/拷⻉数据块到其他DataNode/删除数据块/升级命令 （不讨论）。⼀次返回只能有⼀条命令，按上⾯优先顺序。

下⾯分析应答的命令是如何构造的。 ⾸先是DNA_RECOVERBLOCK（恢复数据块），那是个⾮常⻓的流程，同时需要回去讨论DataNode 上的⼀些功能，我们在后⾯介绍它。

对于DNA_TRANSFER（拷⻉数据块到其他DataNode），从DatanodeDescriptor.replicateBlocks中取 出尽可能多的项⽬，放到BlockComand中。在DataNode中，命令由transferBlocks执⾏，前⾯我们 已经分析过啦。

删除数据块DNA_INVALIDATE也很简单，从DatanodeDescriptor.invalidateBlocks中获取尽可能多的项 ⽬，放到BlockComand中，DataNode中的动作，我们也分析过。

我们来讨论DNA_RECOVERBLOCK（恢复数据块），在讨论DataNode的过程中，我们没有讲这个命 令是⽤来⼲什么的，还有它在DataNode上的处理流程，是好好分析分析这个流程的时候了。 DNA_RECOVERBLOCK命令通过DatanodeDescriptor.getLeaseRecoveryComand获取，获取过程很 简单，将DatanodeDescriptor对象中队列recoverBlocks的所有内容取出，放⼊BlockComand的 Block中，设置BlockComand为DNA_RECOVERBLOCK，就OK了。 关键是，这个队列⾥的信息是⽤来⼲什么的。我们先来看那些操作会向这个队列加东⻄，调⽤关系图 如下：

租约有两个超时时间，⼀个被称为软超时（1分钟），另⼀个是硬超时（1⼩时）。如果租约软超时， 那么就会触发internalReleaseLease⽅法，如下：

voidinternalReleaseLease(Lease lease, String src) throws IOException 该⽅法执⾏： l 检查src对应的INodeFile，如果不存在，不处于构造状态，返回； l ⽂件处于构造状态，⽽⽂件⽬标DataNode为空，⽽且没有数据块，则finalize该⽂件（该过程在 completeFileInternal中已经讨论过，租约在过程中被释放），并返回； l ⽂件处于构造状态，⽽⽂件⽬标DataNode为空，数据块⾮空，则将最后⼀个数据块存放的 DataNode⽬标取出（在BlocksMap中），然后设置为⽂件现在的⽬标DataNode；

l 调⽤INodeFileUnderConstruction.asignPrimaryDatanode，该过程会挑选⼀个⽬前还活着的 DataNode，作为租约的主节点，并把<block，block⽬标DataNode数组>加到该DataNode的 recoverBlocks队列中； l 更新租约。 上⾯分析了租约软超时的情况下NameNode发⽣租约恢复的过程。DataNode上收到这个命令后，将会 启动⼀个新的线程，该线程为每个Block调⽤recoverBlock⽅法：recoverBlock(blocks[i], false, targets[i], true)。

private LocatedBlockrecoverBlock(Block block, boleankepLength, DatanodeID[] datanodeids,boleancloseFile) throwsIOException

它的流程并不复杂，但是分⽀很多，如下图（蓝线是上⾯输⼊，没有异常⾛的流程）：

⾸先是判断进来的Block是否在ongoingRecovery中，如果存在，返回，不存在，加到 ongoingRecovery中。 接下来是个循环（框内部分是循环体，奇怪，没找到表示循环的符号），对每⼀个DataNode，获取 Block的BlockMetaDataInfo（下⾯还会分析），这需要调⽤到DataNode间通信的接⼝上的⽅法 getBlockMetaDataInfo。然后分情况看要不要把信息保存下来（图中间的⼏个判断），其中包括要进 ⾏同步的节点。 根据参数，更新数据块信息，然后调⽤syncBlock并返回syncBlock⽣产的LocatedBlock。 上⾯的这⼀圈，对于我们这个输⼊常数来说，就是把Block的⻓度，更新成为拥有最新时间戳的最⼩⻓ 度值，并得到要更新的节点列表，然后调⽤syncBlock更新各节点。 getBlockMetaDataInfo⽤于获取Block的BlockMetaDataInfo，包括Block的generationStamp，最后校 验时间，同时它还会检查数据块⽂件的元信息，如果出错，会抛出异常。 syncBlock定义如下： private LocatedBlock syncBlock(Block block, List<BlockRecord>syncList,

boleancloseFile) 它的流程是： l 如果syncList为空，通过comitBlockSynchronization向NameNode提交这次恢复； l syncList不为空，那么先NameNode申请⼀个新的Stamp，并根据上⾯得到的⻓度，构造⼀个新 的数据块信息newblock； l 对于没⼀个syncList中的DataNode，调⽤它们上⾯的updateBlock，更新信息；更新信息如果返 回OK，记录下来； l 如果更新了信息的DataNode不为空，调⽤comitBlockSynchronization提交这次恢复；并⽣成 LocatedBlock； l 如果更新的DataNode为空，抛异常。 通过syncBlock，所有需要恢复的DataNode上的Block信息都被更新。

DataNode上的updateBlock⽅法我们前⾯已经介绍了，就不再分析。 下⾯我们来看NameNode的comitBlockSynchronization⽅法，它在上⾯的过程中⽤于提交数据块恢 复： public voidcomitBlockSynchronization(Block block,

longnewgenerationstamp,longnewlength, boleancloseFile,boleandeleteblock, DatanodeID[] newtargets )

参数分别是block，数据块；newgenerationstamp，新的时间戳；newlength，新⻓度；closeFile，是 否关闭⽂件，deleteblock，是否删除⽂件；newtargets，新的⽬标列表。 上⾯的两次调⽤，输⼊参数分别是： comitBlockSynchronization(block, 0, 0, closeFile,true,DatanodeID.EMPTY_ARAY); comitBlockSynchronization(block,newblock.getGenerationStamp(), newblock.getNumBytes(), closeFile,false, nlist); 处理流程是： l 参数检查； l 获取对应的⽂件，记为pendingFile； l BlocksMap中删除⽼的信息； l 如果deleteblock为true，从pendingFile删除Block记录； l 否则，更新Block的信息； l 如果不关闭⽂件，那么写⽇志保存更新，返回； l 关闭⽂件的话，调⽤finalizeINodeFileUnderConstruction。 这块⽐较复杂，不仅涉及了NameNode和DataNode间的通信，⽽且还存在对于DataNode和DataNode 间的通信（DataNode间的通信就只⽀持这两个⽅法，如下图）。后⾯介绍DFSClient的时候，我们还 会再回来分析它的功能，以获取全⾯的理解。

## Hadop源代码分析（三四）

继续对NameNode实现的接⼝做分析。 public DatanodeComand blockReport(DatanodeRegistration nodeReg,

long[] blocks)throws IOException DataNode向NameNode报告它拥有的所有数据块，其中，参数blocks包含了数组化以后数据块的信 息。FSNamesystem.procesReport处理这个请求。⼀番检查以后，调⽤DatanodeDescriptor的 reportDif，将上报的数据块分成三组，分别是： l 删除：其它情况； l 加⼊：BlocksMap中有数据块，但⽬前的DatanodeDescriptor上没有对应信息； l 使⽆效：BlocksMap中没有找到数据块。

对于删除的数据块，调⽤removeStoredBlock，这个⽅法我们前⾯已经分析过啦。 对应需要加⼊的数据块，调⽤adStoredBlock⽅法，处理流程如下： l 从BlocksMap获取现在的信息，记为storedBlock；如果为空，返回； l 记录block和DatanodeDescriptor的关系； l 新旧数据块记录不是同⼀个（我们这个流程是肯定不是啦）：

- 1. 如果现有数据块⻓度为0，更新为上报的block的值；
- 2. 如果现有数据块⻓度⽐新上报的⻓，invalidateBlock（前⾯分析过，很简单的⼀个⽅法）当前数 据块；
- 3. 如果现有数据块⻓度⽐新上报的⼩，那么会删除所有⽼的数据块（还是通过invalidateBlock）， 并更新BlocksMap中数据块的⼤⼩信息；
- 4. 跟新可⽤存储空间等信息； l 根据情况确定数据块需要复制的数⽬和⽬前副本数； l 如果⽂件处于构建状态或系统现在是安全模式，返回； l 处理当前副本数和⽂件的⽬标副本数不⼀致的情况； l 如果当前副本数⼤于系统设定⻔限，开始删除标记为⽆效的数据块。 还是给个流程图吧：


对于标记为使⽆效的数据块，调⽤adToInvalidates⽅法，很简单的⽅法，直接加到FSNamesystem的 成员变量recentInvalidateSets中。 public voidblockReceived(DatanodeRegistration registration,

Blockblocks[], String[] delHints)

DataNode可以通过blockReceived，向NameNode报告它最近接受到的数据块，同时给出如果数据块 副本数太多时，可以删除数据块的节点（参数delHints）。在DataNode中，这个信息是通过⽅法 notifyNamenodeReceivedBlock，记录到对应的列表中。

NameNode上的处理不算复杂，对输⼊参数进⾏检查以后，调⽤上⾯分析的adStoredBlock⽅法。然 后在PendingReplicationBlocks对象中删除相应的block。

public voiderorReport(DatanodeRegistration registration, int erorCode, String msg)

向NameNode报告DataNode上的⼀个错误，如果错误是硬盘错，会删除该DataNode，其它情况只是 简单地记录收到⼀条出错信息。

publicNamespaceInfo versionRequest()throws IOException; 从NameNode上获取NamespaceInfo，该信息⽤于构造DataNode上的DataStorage。

UpgradeComand procesUpgradeComand(UpgradeComand com) throws IOException; 我们不讨论。

public voidreportBadBlocks(LocatedBlock[] blocks) throws IOException 报告错误的数据块。NameNode会循环调⽤FSNamesystem的markBlockAsCorupt⽅法。处理流程不 是很复杂，找对应的INodeFile，如果副本数够，那么调⽤invalidateBlock，使该DataNode上的Block ⽆效；如果副本数不够，加Block到CoruptReplicasMap中，然后准备对好数据块进⾏复制。 ⽬前为⽌，我们已经完成了NameNode上的ClientProtocol和DatanodeProtocol的分析了， NamenodeProtocol我们在理解从NameNode的时候，才会进⾏分析。

## Hadop源代码分析（三五）

除了对外提供的接⼝，NameNode上还有⼀系列的线程，不断检查系统的状态，下⾯是这些线程的功 能分析。 在NameNode中，定义了如下线程：

Daemon hbthread= nul; / HeartbeatMonitor thread publicDaemon lmthread =nul; / LeaseMonitor thread Daemon s mthread= nul; / SafeModeMonitor thread publicDaemon replthread =nul; / Replication thread privateDaemon dnthread =nul;

PendingReplicationBlocks中也有⼀个线程： Daemon timerThread= nul; NameNode内嵌的HTP服务器中⾃然也有线程，这块我们就不分析啦。

HtpServer infoServer; ⼼跳线程⽤于对DataNode的⼼态进⾏检查，以间隔heartbeatRecheckInterval运⾏heartbeatCheck⽅ 法。如果在⼀定时间内没收到DataNode的⼼跳信息，我们就认为该节点已经死掉，调⽤ removeDatanode（前⾯分析过）将DataNode标记为⽆效。 租约lmthread⽤于检查租约的硬超时，如果租约硬超时，调⽤前⾯分析过的internalReleaseLease，释 放租约。 s mthread运⾏的SafeModeMonitor我们前⾯已经分析过了。 replthread运⾏ReplicationMonitor，这个线程会定期调⽤computeDatanodeWork和 procesPendingReplications。

computeDatanodeWork会执⾏computeDatanodeWork或computeInvalidateWork。 computeDatanodeWork从nededReplications中扫描，取出需要复制的项，然后： l 检查⽂件不存在或者处于构造状态；如果是，从队列中删除复制项，退出对复制项的处理（接 着处理下⼀个）； l 得到当前数据块副本数并选择复制的源DataNode，如果空，退出对复制项的处理； l 再次检查副本数（很可能有DataNode从故障中恢复），如果发现不需要复制，从队列中删除复 制项，退出对复制项的处理； l 选择复制的⽬标，如果⽬标空，退出对复制项的处理； l 将复制的信息（数据块和⽬标DataNode）加⼊到源⽬标DataNode中；在⽬标DataNode中记录 复制请求； l 从队列中将复制项移动到pendingReplications。 可⻅，这个⽅法执⾏后，复制项从nededReplications挪到pendingReplications中。DataNode在某次 ⼼跳的应答中，可以拿到相应的信息，执⾏复制操作。 computeInvalidateWork当然是⽤于删除⽆效的数据块。它的主要⼯作在invalidateWorkForOneNode 中完成。和上⾯computeDatanodeWork类似，不过它的处理更简单，将recentInvalidateSets的数据 通过DatanodeDescriptor.adBlocksToBeInvalidated挪到DataNode中。 dnthread执⾏的是DecomisionedMonitor，它的run⽅法周期调⽤ decomisionedDatanodeCheck，再到checkDecomisionStateInternal，定期将完成 Decomision任务的DataNode状态从DECOMI SION_INPROGRES改为DECOMI SIONED。 PendingReplicationMonitor中的线程⽤于对处在等待复制状态的数据块进⾏检查。如果发现⻓时间该 数据块没被复制，那么会将它挪到timedOutItems中。请参考PendingReplicationBlocks的讨论。 infoServer的相关线程我们就不分析了，它们都⽤于处理HTP请求。 上⾯已经总结了NameNode上的⼀些为特殊任务启动的线程，除了这些线程，NameNode上还运⾏着 RPC服务器的相关线程，具体可以看前⾯章节。 在我们开始分析Secondary NameNode前，我们给出了以NameNode上⼀些状态转移图，⼤家可以通 过这个图，更好理解NameNode。 NameNode：

DataNode：

⽂件：

Block，⽐较复杂：

上⾯的图不是很严格，只是⽤于帮助⼤家理解NameNode对Block复杂的处理过程。 稍微说明⼀下，“Block in inited DataNode”表明这个数据块在⼀个刚初始化的DataNode上。“Block in INodeFile”是数据块属于某个⽂件，“Block inINodeFileUnderConstruction”表明这数据块属于⼀个正 在构建的⽂件，当然，处于这个状态的Block可能因为租约恢复⽽转移到“Block in Recover”。右上⽅ 描述了需要复制的数据块的状态，UnderReplicatedBlocks和PendingReplicationBlocks的区别在于 Block是否被插⼊到某⼀个DatanodeDescriptor中。Corupt和Invalidate的就好理解啦。

## Hadop源代码分析（三六）

转战进⼊Secondary NameNode，前⾯的分析我们有事也把它称为从NameNode，从NameNode在 HDFS⾥是个⼩配⻆。 跟Secondary NameNode有关系的类不是很多，如下图：

⾸先要讨论的是NameNode和Secondary NameNode间的通信。NameNode上实现了接⼝ NamenodeProtocol（如下图），就是⽤于NameNode和Secondary NameNode间的命令通信。

NameNode和Secondary NameNode间数据的通信，使⽤的是HTP协议，HTP的容器⽤的是jety， TransferFsImage是⽂件传输的辅助类。

GetImageServlet的doGet⽅法⽬前⽀持取FSImage(getimage)，取⽇志(getedit)和存 FSImage(putimage)。例如：

htp:/localhost:5070/getimage?getimage

可以获取FSImage。

htp:/localhost:5070/getimage?getedit

可以获取⽇志⽂件。

保存FSImage需要更多的参数，它的流程很好玩，SecondaryNameNode发送⼀个HTP请求到 NameNode，启动NameNode上⼀个HTP客户端到SecondaryNameNode上去下载FSImage，下载需 要的⼀些信息，都放在从NameNode的HTP请求中。 我们先来考察Secondary NameNode持久化保存的信息： [hadop@localhostnamesecondary]$ ls –R

.: curent image in_use.lock previous.checkpoint

./curent: edits fsimage fstime VERSION

./image: fsimage

./previous.checkpoint: edits fsimage fstime VERSION in_use.lock的⽤法和前⾯NameNode，DataNode的是⼀样的。对⽐NameNode保存的信息，我们可以 发现Secondary NameNode上保存多了⼀个previous.checkpoint。CheckpointStorage就是应⽤于 Secondary NameNode的存储类，它继承⾃FSImage，只添加了很少的⽅法。 previous.checkpoint⽬录保存了上⼀个checkpoint的信息（curent⾥的永远是最新的），临时⽬录⽤ 于创建新checkpoint，成功后，⽼的checkpoint保存在previous.checkpoint⽬录中。状态图如下（基 类FSImage⽤的是⿊⾊）：

⾄于上⾯⽬录下⽂件的内容，和FSImage是⼀样的。 CheckpointStorage除了上⾯图中的startCheckpoint和endCheckpoint⽅法（上图给出了正常流 程），还有：

voidrecoverCreate(Colection<File> dataDirs,

Colection<File> editsDirs)throwsIOException 和FSImage.coverTransitionRead类似，⽤于分析现有⽬录，创建⽬录（如果不存在）并从可能的错误 中恢复。

privatevoiddoMerge(CheckpointSignature sig)throwsIOException doMerge被类SecondaryNameNode的同名⽅法调⽤，我们后⾯再分析。

## Hadop源代码分析（三七）

Secondary NameNode的成员变量很少，主要的有：

privateCheckpointStorage checkpointImage; Secondary NameNode使⽤的Storage

privateNamenodeProtocol namenode; 和NameNode通信的接⼝

privateHtpServer infoServer; 传输⽂件⽤的HTP服务器 main⽅法是Secondary NameNode的⼊⼝，它最终启动线程，执⾏SecondaryNameNode的run。启动 前的对SecondaryNameNode的构造过程也很简单，主要是创建和NameNode通信的接⼝和启动HTP 服务器。 SecondaryNameNode的run⽅法每隔⼀段时间执⾏doCheckpoint()，从NameNode的主要⼯作都在这 ⼀个⽅法⾥。这个⽅法，总的来说，会从NameNode上取下FSImage和⽇志，然后再本地合并，再上 传回NameNode。这个过程结束后，从NameNode上保持了NameNode上持久化信息的⼀个备份，同 时，NameNode上已经完成合并到FSImage的⽇志可以抛弃，⼀箭双雕。 具体的的流程是：

- 1：调⽤startCheckpoint，为接下来的⼯作准备空间。startCheckpoint会在内部做⼀系列的检查，然 后调⽤CheckpointStorage的startCheckpoint⽅法，创建⽬录。
- 2：调⽤namenode的rolEditLog⽅法，开始⼀次新的检查点过程。调⽤会返回⼀个 CheckpointSignature（检查点签名），在上传合并完的FSImage时，会使⽤这个签名。 Namenode的rolEditLog⽅法最终调⽤的是FSImage的同名⽅法，前⾯提到过这个⽅法，作⽤是关闭往 edits上写的⽇志，打开⽇志到edits.new。明显，在Secondary NameNode下载fsimage和⽇志的时 候，对命名空间的修改，将保持在edits.new的⽇志中。 注意，如果FSImage这个时候的状态（看下⾯的状态机，前⾯出现过⼀次）不是出于 CheckpointStates.ROLED_EDITS，将抛异常结束这个过程。
- 3：通过downloadCheckpointFiles下载fsimage和⽇志，并设置本地检查点状态为 CheckpointStates.UPLOAD_DONE。
- 4：合并⽇志的内容到fsimage中。过程很简单，CheckpointStorage利⽤继承⾃FSImage的 loadFSImage加载fsimage，loadFSEdits应⽤⽇志，然后通过saveFSImage保存。很明显，现在保存在 硬盘上的fsimage是合并⽇志的内容以后的⽂件。
- 5：使⽤putFSImage上传合并⽇志后的fsimage（让NameNode通过HTP到从NameNode取⽂件）。 这个过程中，NameNode会： 调⽤NameNode的FSImage.validateCheckpointUpload，检查现在的状态； 利⽤HTP，从Secondary NameNode获取新的fsimage； 更新结束后设置新状态。
- 6：调⽤NameNode的rolFsImage，最终调⽤FSImage的rolFsImage⽅法，前⾯我们已经分析过了。
- 7：调⽤本地endCheckpoint⽅法，结束⼀次doCheckpoint流程。


其实前⾯在分析FSImage的时候，我们在不了解SecondaryNameNode的情况下，分析了很多和 Checkpoint相关的⽅法，现在我们终于可以有⼀个⽐较统⼀的了解了，下⾯给出NameNode和 Secondary NameNode的存储系统在这个流程中的状态转移图，⽅便⼤家理解。

图中右侧的状态转移图：

⽂件系统上的⽬录的变化（三六中出现）：

## Hadop源代码分析（三⼋）

我们可以开始从系统的外部来了解HDFS了，DFSClient提供了连接到HDFS系统并执⾏⽂件操作的基本 功能。DFSClient也是个⼤家伙，我们先分析它的⼀些内部类。我们先看LeaseChecker。租约是客户 端对⽂件写操作时需要获取的⼀个凭证，前⾯分析NameNode时，已经了解了租约， INodeFileUnderConstruction的关系，INodeFileUnderConstruction只有在⽂件写的时候存在。客户端 的租约管理很简单，包括了增加的put和删除的remove⽅法，run⽅法会定期执⾏，并通过 ClientProtocl的renewLease，⾃动延⻓租约。

接下来我们来分析内部为⽂件读引⼊的类。

InputStream是系统的虚类，提供了3个read⽅法，⼀个skip（跳过数据）⽅法，⼀个available⽅法 （⽬前流中可读的字节数），⼀个close⽅法和⼏个在输⼊流中做标记的⽅法（mark：标记，reset： 回到标记点和markSuported：能⼒查询）。 FSInputStream也是⼀个虚类，它将接⼝Sekable和PositionedReadable混插到类中。Sekable提供 了可以在流中定位的能⼒（sek，getPos和sekToNewSource），⽽PositionedReadable提⾼了从某 个位置开始读的⽅法（⼀个read⽅法和两个readFuly⽅法）。 FSInputChecker在FSInputStream的基础上，加⼊了HDFS中需要的校验功能。校验在 readChecksumChunk中实现，并在内部的read1⽅法中调⽤。所有的read调⽤，最终都是使⽤read1读 数据并做校验。如果校验出错，抛出异常ChecksumException。 有了⽀持校验功能的输⼊流，就可以开始构建基于Block的输⼊流了。我们先回顾前⾯提到的读数据块 的请求协议：

然后我们来分析⼀下创建BlockReader需要的参数，newBlockReader最复杂的请求如下：

public staticBlockReader newBlockReader( Socket sock, String file, longblockId, longgenStamp,

longstartOfset,long len, intbuferSize,bolean verifyChecksum,

StringclientName) throwsIOException

其中，sock为到DataNode的socket连接，file是⽂件名（只是⽤于⽇志输出），其它的参数含义都很 清楚，和协议基本是⼀⼀对应的。该⽅法会和DataNode进⾏对话，发送上⾯的读数据块的请求，处理 应答并构造BlockReader对象（BlockReader的构造函数基本上只有赋值操作）。 BlockReader的readChunk⽤于处理DataNode送过来的数据，格式前⾯我们已经讨论过了，如下图。

读数据⽤的read，会调⽤⽗类FSInputChecker的read，最后调⽤readChunk，如下：

read如果发现读到正确的校验码，则⽤过checksumOk⽅法，向DataNode发送成功应达。 BlockReader的主要流程就介绍完了，接下来分析DFSInputStream，它封装了DFSClient读⽂件内容的 功能。在它的内部，不但要处理和NameNode的通信，同时通过BlockReader，处理和DataNode的交 互。 DFSInputStream记录Block的成员变量是：

privateLocatedBlocks locatedBlocks =nul; 它不但保持了⽂件对应的Block序列，还保持了管理Block的DataNode的信息，是DFSInputStream中 最重要的成员变量。DFSInputStream的构造函数，通过类内部的openInfo⽅法，获取这个变量的值。 openInfo间接调⽤了NameNode的getBlockLocations，获取LocatedBlocks。 DFSInputStream中处理数据块位置的还有下⾯⼀些函数：

synchronizedList<LocatedBlock> getAlBlocks()throwsIOException privateLocatedBlock getBlockAt(longofset)throwsIOException privatesynchronizedList<LocatedBlock> getBlockRange(longofset,

longlength)

private synchronizedDatanodeInfo blockSekTo(longtarget)throwsIOException 它们的功能都很清楚，需要注意的是他们处理过程中可能会调⽤再次调⽤NameNode的 getBlockLocations，使得流程⽐较复杂。blockSekTo还会创建对应的BlockReader对象，它被⼏个重 要的⽅法调⽤（如下图）。在打开到DataNode之前，blockSekTo会调⽤choseDataNode，选择⼀ 个现在活着的DataNode。

通过上⾯的分析，我们已经知道了在什么时候会连接NameNode，什么时候会打开到DataNode的连 接。下⾯我们来看读数据。read⽅法定义如下：

public intread(longposition,byte[]bufer, int ofset,intlength) 该⽅法会从流的position位置开始，读取最多length个byte到bufer中ofset开始的空间中。参数检测完 以后，通过getBlockRange获取要读取的数据块对应的block范围，然后，利⽤fetchBlockByteRange ⽅法，读取需要的数据。 fetchBlockByteRange从某⼀个数据块中读取⼀段数据，定义如下：

privatevoidfetchBlockByteRange(LocatedBlock block, longstart,

long end,byte[] buf,intofset) 由于读取的内容都在⼀个数据块内部，这个⽅法会创建BlockReader，然后利⽤BlockReader的readAl ⽅法，读取数据。读的过程中如果发⽣校验错，那么，还会通过reportBadBlocks，向NameNode报告 校验错。 另⼀个读⽅法是：

public synchronized intread(bytebuf[],int of,int len)throwsIOException 它在流的当前位置（可以通过sek⽅法调整）读取数据。⾸先它会判断当前流的位置，如果已经越过 了对象现在的blockReader能读取的范围（当上次read读到数据块的尾部时，会发⽣这中情况），那么 通过blockSekTo打开到下⼀个数据块的blockReader。然后，read在当前的这个数据块中通过 readBufer读数据。主要，这个read⽅法只在⼀块数据块中读取数据，就是说，如果还有空间可以存放 数据但已经到了数据块的尾部，它不会打开到下⼀个数据块的BlockReader继续读，⽽是返回，返回值 包含了以读取数据的⻓度。 DFSDataInputStream是⼀个Wraper(DFSInputStream)，我们就不讨论了。

## Hadop源代码分析（三九）

接下来当然是分析输出流了。 处于继承体系的最上⽅是OutputStream，它实现了Closeable（⽅法close）和Flushable（⽅法flush） 接⼝，提供了3个不同形式的write⽅法，这些⽅法的含义都很清楚。接下来的是FSOutputSumer， 它引⼊了HDFS写数据时需要的计算校验和的功能。FSOutputSumer的write⽅法会调⽤write1， write1中计算校验和并将⽤户输⼊的数据拷⻉到对象的缓冲区中，缓冲区满了以后会调⽤flushBufer， flushBufer最终调⽤还是虚⽅法的writeChunk，这个时候，缓冲区对应的校验和缓冲区对的内容都已 经准备好了。通过这个类，HDFS可以把⼀个流转换成为DataNode数据接⼝上的包格式（前⾯我们讨 论过这个包的格式，如下）。

DFSOutputStream继承⾃FSOutputSumer，是⼀个⾮常复杂的类，它包含了⼏个内部类。我们先分 析Packet，其实它对应了上⾯的数据包，有了上⾯的图，这个类就很好理解了，它的成员变量和上⾯ 数据块包含的信息基本⼀⼀对应。构造函数需要的参数有pktSize，包的⼤⼩，chunksPerPkt，chunk 的数⽬（chunk是⼀个校验单元）和该包在Block中的偏移量ofsetInBlock。writeData和 writeChecksum⽤于往缓冲区⾥写数据/校验和。getBufer⽤户获得整个包，包括包头和数据。 DataStreamer和ResponseProcesor⽤于写包/读应答，和我们前⾯讨论DataNode的Pipe写时类似， 客户端写数据也需要两个线程，下图扩展了我们在讨论DataNode处理写时的示意图，包含了客户端：

DataStreamer启动后进⼊⼀个循环，在没有错误和关闭标记为false的情况下，该循环⾸先调⽤ procesDatanodeEror，处理可能的IO错误，这个过程⽐较复杂，我们在后⾯再讨论。 接着DataStreamer会在dataQueue（数据队列）上等待，直到有数据出现在队列上。DataStreamer获 取⼀个数据包，然后判断到DataNode的连接是否是打开的，如果不是，通过 DFSOutputStream.nextBlockOutputStream打开到DataNode的连接，并启动ResponseProcesor线 程。 DataNode的连接准备好以后，DataStreamer获取数据包缓冲区，然后将数据包从dataQueue队列挪到 ackQueue队列，最后通过blockStream，写数据。如果数据包是最后⼀个，那么，DataStreamer将会 写⼀个⻓度域为0的包，指示DataNode数据传输结束。 DataStreamer的循环在最后⼀个数据包写出去以后，会等待直到ackQueue队列为空（表明所有的应答 已经被接收），然后做清理动作（包括关闭socket连接，ResponseProcesor线程等），退出线程。 ResponseProcesor相对来说⽐较简单，就是等待来⾃DataNode的应答。如果是成功的应答，则删除 在ackQueue的包，如果有错误，那么，记录出错的DataNode，并设置标志位。

## Hadop源代码分析（四零）

有了上⾯的基础，我们可以来解剖DFSOutputStream了。先看构造函数： privateDFSOutputStream(String src,longblockSize, Progresable progres, intbytesPerChecksum)throwsIOException

DFSOutputStream(String src, FsPermisionmasked, bolean overwrite, shortreplication, longblockSize,Progresable progres, intbufersize, intbytesPerChecksum)throwsIOException

DFSOutputStream(String src,intbufersize, Progresable progres, LocatedBlock lastBlock, FileStatustat, intbytesPerChecksum)throwsIOException {

这些构造函数的参数主要有：⽂件名src；进度回调函数progres（预留接⼝，⽬前未使⽤）；数据块 ⼤⼩blockSize；Block副本数replication；每个校验chunk的⼤⼩bytesPerChecksum；⽂件权限 masked；是否覆盖原⽂件标记overwrite；⽂件状态信息stat；⽂件的最后⼀个Block信息lastBlock； bufersize（？未⻅引⽤）。 后⾯两个构造函数会调⽤第⼀个构造函数，这个函数会调⽤⽗类的构造函数，并设置对象的src， blockSize，progres和checksum属性。 第⼆个构造函数会调⽤namenode.create⽅法，在⽂件空间中建⽴⽂件，并启动DataStreamer，它被 DFSClient的create⽅法调⽤。第三个构造函数被DFSClient的apend⽅法调⽤，显然，这种情况⽐价 复杂，⽂件拥有⼀些数据块，添加数据往往添加在最后的数据块上。同时，apend⽅法调⽤时， Client已经知道了最后⼀个Block的信息和⽂件的⼀些信息，如FileStatus中包含的Block⼤⼩，⽂件权 限位等等。结合这些信息，构造函数需要计算并设置⼀些对象成员变量的值，并试图从可能的错误中 恢复（调⽤procesDatanodeEror），最后启动DataStreamer。 我们先看正常流程，前⾯已经分析过，通过FSOutputSumer，HDFS客户端能将流转换成package， 这个包是通过writeChunk，发送出去的，下⾯是它们的调⽤关系。

在检查完⼀系列的状态以后，writeChunk先等待，直到dataQueue中未发送的包⼩于⻔限值。如果现 在没有可⽤的Packet对象，则创建⼀个Packet对象，往Packet中写数据，包括校验值和数据。如果数 据包被写满，那么，将它放⼊发送队列dataQueue中。writeChunk的过程⽐较简单，这⾥的写⼊，也 只是把数据写到本地队列，等待DataStreamer发送，没有实际写到DataNode上。 createBlockOutputStream⽤于建⽴到第⼀个DataNode的连接，它的声明如下： privateboleancreateBlockOutputStream(DatanodeInfo[] nodes, String client,

boleanrecoveryFlag) nodes是所有接收数据的DataNode列表，client就是客户端名称，recoveryFlag指示是否是为错误恢复 建⽴的连接。createBlockOutputStream很简单，打开到第⼀个DataNode的连接，然后发送下⾯格式 的数据包，并等待来⾃DataNode的Ack。如果出错，记录出错的DataNode在nodes中的位置，设置 erorIndex并返回false。

当recoveryFlag指示为真时，意味着这次写是⼀次恢复操作，对于DataNode来说，这意味着为写准备 的临时⽂件（在tmp⽬录中）可能已经存在，需要进⾏⼀些特殊处理，具体请看FSDataset的实现。 当Client写数据需要⼀个新的Block的时候，可以调⽤nextBlockOutputStream⽅法。

privateDatanodeInfo[] nextBlockOutputStream(String client)throwsIOException 这个⽅法的实现很简单，⾸先调⽤locateFolowingBlock（包含了重试和出错处理），通过 namenode.adBlock获取⼀个新的数据块，返回的是DatanodeInfo列表，有了这个列表，就可以建⽴ 写数据的pipe了。下⼀个⼤动作就是调⽤上⾯的createBlockOutputStream，建⽴到DataNode的连接 了。

有了上⾯的准备，我们来分析procesDatanodeEror，它的主要流程是： l 参数检查； l 关闭可能还打开着的blockStream和blockReplyStream； l 将未收到应答的数据块（在ackQueue中）挪到dataQueue中； l 循环执⾏：

- 1. 计算⽬前还活着的DataNode列表；
- 2. 选择⼀个主DataNode，通过DataNode RPC的recoverBlock⽅法启动它上⾯的恢复过程；
- 3. 处理可能的出错；
- 4. 处理恢复后Block可能的变化（如Stamp变化）；
- 5. 调⽤createBlockOutputStream到DataNode的连接。 l 启动ResponseProcesor。 这个过程涉及了DataNode上的recoverBlock⽅法和createBlockOutputStream中可能的Block恢复，是 ⼀个相当耗资源的⽅法，当系统出错的概率⽐较⼩，⽽且数据块上能恢复的数据很多（平均32M）， 还是值得这样做的。 写的流程就分析到着，接下来我们来看流的关闭，这个过程也涉及了⼀系列的⽅法，它们的调⽤关系 如下：


flushInternal会⼀直等待到发送队列（包括可能的curentPacket）和应答队列都为空，这意味着数据 都被DataNode顺利接收。 sync作⽤和UNIX的sync类似，将写⼊数据持久化。它⾸先调⽤⽗类的flushBufer⽅法，将可能还没拷 ⻉到DFSOutputStream的数据拷⻉回来，然后调⽤flushInternal，等待所有的数据都写完。然后调⽤ namenode.fsync，持久化命名空间上的数据。 closeInternal⽐较复杂⼀点，它⾸先调⽤⽗类的flushBufer⽅法，将可能还没拷⻉到 DFSOutputStream的数据拷⻉回来，然后调⽤flushInternal，等待所有的数据都写完。接着结束两个 ⼯作线程，关闭socket，最后调⽤amenode.complete，通知NameNode结束⼀次写操作。close⽅法 先调⽤closeInternal，然后再本地的leasechecker中移除对应的信息。

## Hadop源代码分析（四⼀）

前⾯分析的DFSClient内部类，占据了这个类的实现部分的2/3，我们来看剩下部分。 DFSClient的成员变量不多，⽽且⼤部分是系统的缺省配置参数，其中⽐较重要的是到NameNode的 RPC客户端：

public final ClientProtocol namenode; final private ClientProtocolrpcNamenode;

它们的差别是namenode在rpcNamenode的基础上，增加了失败重试功能。DFSClient中提供可各种构 造它们的static函数，createClientDatanodeProtocolProxy⽤于⽣成到DataNode的RPC客户端。

DFSClient的构造函数也⽐价简单，就是初始化成员变量，close⽤于关闭DFSClient。 下⾯的功能，DFSClient只是简单地调⽤NameNode的对应⽅法（加⼀些简单的检查），就不罗嗦了： setReplication/rename/delete/exists（通过getFileInfo的返回值是否为空判 断）/listPaths/getFileInfo/setPermision/setOwner/getDiskStatus/totalRawCapacity/totalRawUsed/d atanodeReport/setSafeMode/refreshNodes/metaSave/finalizeUpgrade/mkdirs/getContentSumary/ setQuota/setTimes DFSClient提供了各种create⽅法，它们最后都是构造⼀个OutputStream，并将⽂件名和⽣成的 OutputStream加到leasechecker，完成创建动作。 apend操作是通过namenode.apend，获取最后的Block信息，然后构造⼀个OutputStream，并将⽂ 件名和⽣成的OutputStream加到leasechecker，完成apend动作。 getFileChecksum⽤于获取⽂件的校验信息，它在得到数据块的位置信息后利⽤DataNode提供的 OP_BLOCK_CHECKSUM操作，获取需要的数据，并综合起来。过程简单，⽅法主要是在处理 OP_BLOCK_CHECKSUM需要交换的数据包。 DFSClient内部还有⼀些其它的辅助⽅法，都⽐较简单，就不再分析了。

## Hadop源代码分析（MapReduce概论）

⼤家都熟悉⽂件系统，在对HDFS进⾏分析前，我们并没有花很多的时间去介绍HDFS的背景，毕竟⼤ 家对⽂件系统的还是有⼀定的理解的，⽽且也有很好的⽂档。在分析Hadop的MapReduce部分前， 我们还是先了解系统是如何⼯作的，然后再进⼊我们的分析部分。下⾯的图来⾃

htp:/horicky.blogspo t.com/208/1/hadop-mapreduce-implementation.html

，是我看到的讲MapReduce最好的图。

以Hadop带的wordcount为例⼦（下⾯是启动⾏）： hadop jar hadop-0.19.0-examples.jar wordcount /usr/input/usr/output ⽤户提交⼀个任务以后，该任务由JobTracker协调，先执⾏Map阶段（图中M1，M2和M3），然后执 ⾏Reduce阶段（图中R1和R2）。Map阶段和Reduce阶段动作都受TaskTracker监控，并运⾏在独⽴于 TaskTracker的Java虚拟机中。 我们的输⼊和输出都是HDFS上的⽬录（如上图所示）。输⼊由InputFormat接⼝描述，它的实现如 ASCI⽂件，JDBC数据库等，分别处理对于的数据源，并提供了数据的⼀些特征。通过InputFormat实 现，可以获取InputSplit接⼝的实现，这个实现⽤于对数据进⾏划分（图中的splite1到splite5，就是划 分以后的结果），同时从InputFormat也可以获取RecordReader接⼝的实现，并从输⼊中⽣成<k,v> 对。有了<k,v>，就可以开始做map操作了。 map操作通过context.colect（最终通过OutputCollector. colect）将结果写到context中。当Maper 的输出被收集后，它们会被Partitioner类以指定的⽅式区分地写出到输出⽂件⾥。我们可以为Maper 提供Combiner，在Maper输出它的<k,v>时，键值对不会被⻢上写到输出⾥，他们会被收集在list⾥ （⼀个key值⼀个list），当写⼊⼀定数量的键值对时，这部分缓冲会被Combiner中进⾏合并，然后再 输出到Partitioner中（图中M1的⻩颜⾊部分对应着Combiner和Partitioner）。

Map的动作做完以后，进⼊Reduce阶段。这个阶段分3个步骤：混洗（Shufle），排序（sort）和 reduce。 混洗阶段，Hadop的MapReduce框架会根据Map结果中的key，将相关的结果传输到某⼀个Reducer 上（多个Maper产⽣的同⼀个key的中间结果分布在不同的机器上，这⼀步结束后，他们传输都到了 处理这个key的Reducer的机器上）。这个步骤中的⽂件传输使⽤了HTP协议。 排序和混洗是⼀块进⾏的，这个阶段将来⾃不同Maper具有相同key值的<key,value>对合并到⼀起。 Reduce阶段，上⾯通过Shufle和sort后得到的<key, (list of values)>会送到Reducer. reduce⽅法中处 理，输出的结果通过OutputFormat，输出到DFS中。

## Hadop源代码分析（MapTask）

接下来我们来分析Task的两个⼦类，MapTask和ReduceTask。MapTask的相关类图如下：

MapTask其实不是很复杂，复杂的是⽀持MapTask⼯作的⼀些辅助类。MapTask的成员变量少，只有 split和splitClas。我们知道，Map的输⼊是split，是原始数据的⼀个切分，这个切分由 org.apache.hadop.mapred.InputSplit的⼦类具体描述（前⾯我们是通过 org.apache.hadop.mapreduce.InputSplit介绍了InputSplit，它们对外的API是⼀样的）。splitClas 是InputSplit⼦类的类名，通过它，我们可以利⽤Java的反射机制，创建出InputSplit⼦类。⽽split是⼀ 个BytesWritable，它是InputSplit⼦类串⾏化以后的结果，再通过InputSplit⼦类的readFields⽅法，我 们可以回复出对应的InputSplit对象。 MapTask最重要的⽅法是run。run⽅法相当简单，配置完系统的TaskReporter后，就根据情况执⾏ runJobCleanupTask，runJobSetupTask，runTaskCleanupTask或执⾏Maper。由于MapReduce现在 有两套API，MapTask需要⽀持这两套API，使得MapTask执⾏Maper分为runNewMaper和 runOldMaper，run*Maper后，MapTask会调⽤⽗类的done⽅法。 接下来我们来分析runOldMaper，最开始部分是构造Maper处理的InputSplit，更新Task的配置，然 后就开始创建Maper的RecordReader，rawIn是原始输⼊，然后分正常（使⽤ TrackedRecordReader，后⾯讨论）和跳过部分记录（使⽤Ski pingRecordReader，后⾯讨论）两种 情况，构造对应的真正输⼊in。 跳过部分记录是Map的⼀种出错恢复策略，我们知道，MapReduce处理的数据集合⾮常⼤，⽽有些任 务对⼀部分出错的数据不进⾏处理，对结果的影响很⼩（如⼤数据集合的⼀些统计量），那么，⼀⼩ 部分的数据出错导致已处理的⼤量结果⽆效，是得不偿失的，跳过这部分记录，成了Maper的⼀种选 择。 Maper的输出，是通过MapOutputColector进⾏的，也分两种情况，如果没有Reducer，那么，⽤ DirectMapOutputColector（后⾯讨论），否则，⽤MapOutputBufer（后⾯讨论）。 构造完Maper的输⼊输出，通过构造配置⽂件中配置的MapRunable，就可以执⾏Maper了。⽬前 系统有两个MapRunable：MapRuner和MultithreadedMapRuner，如下图。

原有API在这块的处理上和新API有很⼤的不⼀样。接⼝MapRunable是原有API中Maper的执⾏器， run⽅法就是⽤于执⾏⽤户的Maper。MapRuner是单线程执⾏器，相当简单，⾸先，当MapTask调 ⽤：

MapRunable<INKEY,INVALUE,OUTKEY,OUTVALUE>runer =

ReflectionUtils.newInstance(job.getMapRunerClas(),job); MapRuner的configure会在newInstance的最后被调⽤，configure执⾏的过程中，对应的Maper会 通过反射机制构造出来。 MapRuner的run⽅法，会先创建对应的key，value对象，然后，对InputSplit的每⼀对<key， value>，调⽤Maper的map⽅法，循环结束后，Maper对应的清理⽅法会被调⽤。我们需要注意， key，value对象在run⽅法中是被重复使⽤的，就是说，每次传⼊Maper的map⽅法的key，value都是 同⼀个对象，只不过是⾥⾯的内容变了，对象并没有变。如果你需要保留key，value的内容，需要实 现clone机制，克隆出对象的⼀个新备份。 相对于新API的多线程执⾏器，⽼API的MultithreadedMapRuner就⽐较复杂了，总体来说，就是通过 阻塞队列配合Java的多线程执⾏器，将<key，value>分发到多个线程中去处理。需要注意的是，在这 个过程中，这些线程共享⼀个Maper实例，如果Maper有共享的资源，需要有⼀定的保护机制。 runNewMaper⽤于执⾏新版本的Maper，⽐runOldMaper稍微复杂，我们就不再讨论了。

## Hadop源代码分析（MapTask辅助类 I）

MapTask的辅助类主要针对Maper的输⼊和输出。⾸先我们来看MapTask中⽤的的Maper输⼊，在类 图中，这部分位于右上⻆。 MapTask.TrackedRecordReader是⼀个Wraper，在原有输⼊RecordReader的基础上，添加了收集上 报统计数据的功能。 MapTask.Ski pingRecordReader也是⼀个Wraper，它在MapTask.TrackedRecordReader的基础上， 添加了忽略部分输⼊的功能。在分析MapTask.Ski pingRecordReader之前，我们先看⼀下类 SortedRanges和它相关的类。

类SortedRanges.Ranges表示了⼀个范围，以开始位置和范围⻓度（这样的话就可以表示⻓度为0的范 围）来表示⼀个范围，并提供了⼀系列的范围操作⽅法。注意，⽅法getEndIndex得到的右端点并不包 含在范围内（应理解为开区间）。SortedRanges包含了⼀系列不重叠的范围，为了保证包含的范围不 重叠，在ad⽅法和remove⽅法上需要做⼀些处理，保证不重叠的约束。SkipRangeIterator是访问 SortedRanges包含的Ranges的迭代器。 MapTask.Ski pingRecordReader的实现很简单，因为要忽略的输⼊都保持在SortedRanges.Ranges， 只需要在next⽅法中，判断⽬前范围时候落在SortedRanges.Ranges中，如果是，忽略，并将忽略的 记录写⽂件（可配置）

NewTrackingRecordReader和NewOutputColector被新API使⽤，我们不分析。 MapTask的输出辅助类都继承⾃MapOutputColector，它只是在OutputColector的基础上添加了close 和flush⽅法。 DirectMapOutputColector⽤在Reducer的数⽬为0，就是不需要Reduce阶段的时候。它是直接通过 out =job.getOutputFormat().getRecordWriter(fs,job, finalName, reporter); 得到对应的RecordWriter，colect直接到RecordWriter上。 如果Maper后续有reduce任务，系统会使⽤MapOutputBufer做为输出，这是个⽐较复杂的类，有1k ⾏左右的代码。 我们知道，Maper是通过OutputColector将Map的结果输出，输出的量很⼤，Hadop的机制是通过 ⼀个circle bufer 收集Maper的输出, 到了io.sort.mb * percent量的时候，就spil到disk，如下图。图 中出现了两个数组和⼀个缓冲区，kvindices保持了记录所属的（Reduce）分区，key在缓冲区开始的 位置和value在缓冲区开始的位置，通过kvindices，我们可以在缓冲区中找到对应的记录。kvofets⽤ 于在缓冲区满的时候对kvindices的partition进⾏排序，排完序的结果将输出到输出到本地磁盘上，其 中索引（kvindices）保持在spil{spil号}.out.index中，数据保存在spil{spil号}.out中。

当Maper任务结束后，有可能会出现多个spil⽂件，这些⽂件会做⼀个归并排序，形成Maper的⼀个 输出（spil.out和spil.out.index），如下图：

这个输出是按partition排序的，这样的话，Maper的输出被分段，Reducer要获取的就是spil.out中的 ⼀段。（注意，内存和硬盘上的索引结构不⼀样） （感谢彭帅的Hadop Map Stage流程分析 htp:/ w.cnblogs.com/OnlyXP/archive/209/05/25/14 81.html）

## Hadop源代码分析（MapTask辅助类， I）

有了上⾯Maper输出的内存存储结构和硬盘存储结构讨论，我们来仔细分析MapOutputBufer的流 程。 ⾸先是成员变量。最先初始化的是作业配置job和统计功能reporter。通过配置，MapOutputBufer可 以获取本地⽂件系统（localFs和rfs），Reducer的数⽬和Partitioner。 SpilRecord是⽂件spil.out{spil号}.index在内存中的对应抽象（内存数据和⽂件数据就差最后的校验 和），该⽂件保持了⼀系列的IndexRecord，如下图：

IndexRecord有3个字段，分别是startOfset：记录偏移量，rawLength：初始⻓度，partLength：实 际⻓度（可能有压缩）。SpilRecord保持了⼀系列的IndexRecord，并提供⽅法⽤于添加记录（没有删 除记录的操作，因为不需要），获取记录，写⽂件，读⽂件（通过构造函数）。 接下来是⼀些和输出缓存区kvbufer，缓存区记录索引kvindices和缓存区记录索引排序⼯作数组 kvofsets相关的处理，下⾯的图有助于说明这段代码。

这部分依赖于3个配置参数，io.sort.spil.percent是kvbufer，kvindices和kvofsets的总⼤⼩（以M为 单位，缺省是10，就是10M，这⼀部分是MapOutputBufer中占⽤存储最多的）。 io.sort.record.percent是kvindices和kvofsets占⽤的空间⽐例（缺省是0.05）。前⾯的分析我们已经 知道kvindices和kvofsets，如果记录数是N的话，它占⽤的空间是4N*4bytes，根据这个关系和 io.sort.record.percent的值，我们可以计算出kvindices和kvofsets最多能有多少个记录，并分配相应 的空间。参数io.sort.spil.percent指示当输出缓冲区或kvindices和kvofsets记录数量到达对应的占⽤ 率的时候，会启动spil，将内存缓冲区的记录存放到硬盘上，softBuferLimit和softRecordLimit为对应 的字节数。 值对<key, value>输出到缓冲区是通过Serializer串⾏化的，这部分的初始化跟在上⾯输出缓存后⾯。接 下来是⼀些计数器和可能的数据压缩处理器的初始化，可能的Combiner和combiner⼯作的⼀些配置。 最后是启动spilThread，该Thread会检查内存中的输出缓存区，在满⾜⼀定条件的时候将缓冲区中的 内容spil到硬盘上。这是⼀个标准的⽣产者-消费者模型，MapTask的colect⽅法是⽣产者， spilThread是消费者，它们之间同步是通过spilLock（RentrantLock）和spilLock上的两个条件变量 （spilDone和spilReady）完成的。 先看⽣产者，MapOutputBufer.colect的主要流程是： l 报告进度和参数检测（<K,V>符合Maper的输出约定）； l spilLock.lock()，进⼊临界区； l 如果达到spil条件，设置变量并通过spilReady.signal()，通知spilThread；并等待spil结束 （通过spilDone.await()等待）； l spilLock.unlock()； l 输出key，value并更新kvindices和kvofsets（注意，⽅法colect是synchronized，key和value 各⾃输出，它们也会占⽤连续的输出缓冲区）； kvstart，kvend和kvindex三个变量在判断是否需要spil和spil是否结束的过程中很重要，kvstart是有 效记录开始的下标，kvindex是下⼀个可做记录的位置，kvend的作⽤⽐较特殊，它在⼀般情况下 kvstart=kvend，但开始spil的时候它会被赋值为kvindex的值，spil结束时，它的值会被赋给 kvstart，这时候kvstart=kvend。这就是说，如果kvstart不等于kvend，系统正在spil，否则， kvstart=kvend，系统处于普通⼯作状态。其实在代码中，我们可以看到很多kvstart=kvend的判 断。 下⾯我们分情况，讨论kvstart，kvend和kvindex的配合。初始化的时候，它们都被赋值0。

下图给出了⼀个没有spil的记录添加过程：

注意kvindex和kvnext的关系，取模实现了循环缓冲区 如果在添加记录的过程中，出现spil（多种条件），那么，主要的过程如下：

⾸先还是计算kvnext，主要，这个时候kvend=kvstart（图中没有画出来）。如果spil条件满⾜，那 么，kvindex的值会赋给kvend（这是kvend不等于kvstart），从kvstart和kvend的⼤⼩关系，我们可 以知道记录位于数组的那⼀部分（左边是kvstart<kvend的情况，右边是另外的情况）。Spil结束的时 候，kvend值会被赋给kvstart，kvend=kvstart⼜重新满⾜，同时，我们可以发现kvindex在这个过程 中没有变化，新的记录还是写在kvindex指向的位置，然后，kvindex=kvnect，kvindex移到下⼀个可 ⽤位置。 ⼤家体会⼀下上⾯的过程，特别是kvstart，kvend和kvindex的配合，其实，<key，value>对输出使⽤ 的缓冲区，也有类似的过程。 Colect在处理<key，value>输出时，会处理⼀个MapBuferToSmalException，这是value的串⾏化 结果太⼤，不能⼀次放⼊缓冲区的指示，这种情况下我们需要调⽤spilSingleRecord，特殊处理。

## Hadop源代码分析（MapTask辅助类， I）

接下来讨论的是key，value的输出，这部分⽐较复杂，不过有了前⾯kvstart，kvend和kvindex配合的 分析，有利于我们理解这部分的代码。 输出缓冲区中，和kvstart，kvend和kvindex对应的是bufstart，bufend和bufmark。这部分还涉及到 变量bufvoid，⽤于表明实际使⽤的缓冲区结尾（⻅后⾯BlockingBufer.reset分析），和变量 bufmark，⽤于标记记录的结尾。这部分代码需要bufmark，是因为key或value的输出是变⻓的，（前 ⾯元信息记录⼤⼩是常量，就不需要这样的变量）。 最好的情况是缓冲区没有翻转和value串⾏化结果很⼩，如下图：

先对key串⾏化，然后对value做串⾏化，临时变量keystart，valstart和valend分别记录了key结果的开 始位置，value结果的开始位置和value结果的结束位置。 串⾏化过程中，往缓冲区写是最终调⽤了Bufer.write⽅法，我们后⾯再分析。

如果key串⾏化后出现bufindex < keystart，那么会调⽤BlockingBufer的reset⽅法。原因是在spil的 过程中需要对<key，value>排序，这种情况下，传递给RawComparator的必须是连续的⼆进制缓冲 区，通过BlockingBufer.reset⽅法，解决这个问题。下图解释了如何解决这个问题：

当发现key的串⾏化结果出现不连续的情况时，我们会把bufvoid设置为bufmark，⻅缓冲区开始部分往 后挪，然后将原来位于bufmark到bufvoid出的结果，拷到缓冲区开始处，这样的话，key串⾏化的结果 就连续存放在缓冲区的最开始处。 上⾯的调整有⼀个条件，就是bufstart前⾯的缓冲区能够放下整个key串⾏化的结果，如果不能，处理 的⽅式是将bufindex置0，然后调⽤BlockingBufer内部的out的write⽅法直接输出，这实际调⽤了 Bufer.write⽅法，会启动spil过程，最终我们会成功写⼊key串⾏化的结果。 下⾯我们看write⽅法。key，value串⾏化过程中，往缓冲区写数据是最终调⽤了Bufer.write⽅法，⼜ 是⼀个复杂的⽅法。 l do-while循环，直到我们有⾜够的空间可以写数据（包括缓冲区和kvindices和kvofsets） u ⾸先我们计算缓冲区连续写是否写满标志buful和缓冲区⾮连续情况下有⾜够写空间标志wrap （这个实在拗⼝），⻅下⾯的讨论；条件（buful & !wrap）⽤于判断⽬前有没有⾜够的写空间； u 在spil没启动的情况下（kvstart = kvend），分两种情况，如果数组中有记录(kvend !=kvindex)，那么，根据需要（⽬前输出空间不⾜或记录数达到spil条件）启动spil过程；否则，如果 空间还是不够（buful & !wrap），表明这个记录⾮常⼤，以⾄于我们的内存缓冲区不能容下这么⼤ 的数据量，抛MapBuferToSmalException异常； u 如果空间不⾜同时spil在运⾏，等待spilDone； l 写数据，注意，如果buful，则写数据会不连续，则写满剩余缓冲区，然后设置bufindex=0， 并从bufindex处接着写。否则，就是从bufindex处开始写。 下图给出了缓冲区连续写是否写满标志buful和缓冲区⾮连续情况下有⾜够写空间标志wrap计算的⼏ 种可能:

情况1和情况2中，buful判断为从bufindex到bufvoid是否有⾜够的空间容纳写的内容，wrap是图中⽩ 颜⾊部分的空间是否⽐输⼊⼤，如果是，wrap为true；情况3和情况4中，buful判断bufindex到 bufstart的空间是否满⾜条件，⽽wrap肯定是false。明显，条件（buful & !wrap）满⾜时，⽬前的 空间不够⼀次写。 接下来我们来看spilSingleRecord，只是⽤于写放不进内存缓冲区的<key，value>对。过程很流⽔， ⾸先是创建SpilRecord记录，输出⽂件和IndexRecord记录，然后循环，构造SpilRecord并在恰当的 时候输出记录（如下图），最后输出spil{n}.index⽂件。

前⾯我们提过spilThread，在这个系统中它是消费者，这个消费者相当简单，需要spil时调⽤函数 sortAndSpil，进⾏spil。sortAndSpil和spilSingleRecord类似，函数的开始也是创建SpilRecord记 录，输出⽂件和IndexRecord记录，然后，需要在kvofsets上做排序，排完序后顺序访问kvofsets， 也就是按partition顺序访问记录。 按partition循环处理排完序的数组，如果没有combiner，则直接输出记录，否则，调⽤ combineAndSpil，先做combin然后输出。循环的最后记录IndexRecord到SpilRecord。 sortAndSpil最后是输出spil{n}.index⽂件。 combineAndSpil⽐价简单，我们就不分析了。 BlockingBufer中最后要分析的⽅法是flush⽅法。调⽤flush⽅法，意味着Maper的结果都已经colect 了，需要对缓冲区做⼀些最后的清理，并合并spil{n}⽂件产⽣最后的输出。 缓冲区处理部分很简单，先等待可能的spil过程完成，然后判断缓冲区是否为空，如果不是，则调⽤ sortAndSpil，做最后的spil，然后结束spil线程。 flush合并spil{n}⽂件是通过mergeParts⽅法。如果Maper最后只有⼀个spil{n}⽂件，简单修改该⽂ 件的⽂件名就可以。如果Maper没有任何输出，那么我们需要创建哑输出（dumy files）。如果 spil{n}⽂件多于1个，那么按partition循环处理所有⽂件，将处于处理partition的记录输出。处理 partition的过程中可能还会再次调⽤combineAndSpil，最记录再做⼀次combination，其中还涉及到 ⼯具类Merger，我们就不再深⼊研究了。

## Hadop源代码分析（Task的内部类和辅助类）

从前⾯的图中，我们可以发现Task有很多内部类，并拥有⼤量类成员变量，这些类配合Task完成相关 的⼯作，如下图。

MapOutputFile管理着Maper的输出⽂件，它提供了⼀系列get⽅法，⽤于获取Maper需要的各种⽂ 件，这些⽂件都存放在⼀个⽬录下⾯。 我们假设传⼊MapOutputFile的JobID为job_2070712173_ 03，TaskID为 task_2070712173_ 03_m_ 05。MapOutputFile的根为 {mapred.local.dir}/taskTracker/jobcache/{jobid}/{taskid}/output 在下⾯的讨论中，我们把上⾯的路径记为{MapOutputFileRot} 以上⾯JogID和TaskID为例，我们有： {mapred.local.dir}/taskTracker/jobcache/job_2070712173_ 03/task_2070712173_ 03_m_0

05/output

需要注意的是，{mapred.local.dir}可以包含⼀系列的路径，那么，Hadop会在这些根路径下找⼀个满 ⾜要求的⽬录，建⽴所需的⽂件。MapOutputFile的⽅法有两种，结尾带ForWrite和不带ForWrite，带 ForWrite⽤于创建⽂件，它需要⼀个⽂件⼤⼩作为参数，⽤于检查磁盘空间。不带ForWrite⽤于获取以 建⽴的⽂件。 getOutputFile：⽂件名为{MapOutputFileRot}/file.out； getOutputIndexFile：⽂件名为{MapOutputFileRot}/file.out.index getSpilFile：⽂件名为{MapOutputFileRot}/spil{spilNumber}.out getSpilIndexFile：⽂件名为{MapOutputFileRot}/spil{spilNumber}.out.index 以上四个⽅法⽤于Task⼦类MapTask中； getInputFile：⽂件名为{MapOutputFileRot}/map_{mapId}.out ⽤于ReduceTask中。我们到使⽤到他们的地⽅再介绍相应的应⽤场景。

介绍完临时⽂件管理以后，我们来看Task.CombineOutputColector，它继承⾃ org.apache.hadop.mapred.OutputColector，很简单，只是⼀个OutputColector到IFile.Writer的 Adapter，活都让IFile.Writer⼲了。

ValuesIterator⽤于从RawKeyValueIterator（Key，Value都是DataInputBufer，ValuesIterator要求该 输⼊已经排序）中获取符合RawComparator<KEY>comparator的值的迭代器。它在Task中有⼀个简单 ⼦类，CombineValuesIterator。

Task.TaskReporter⽤于向JobTracker提交计数器报告和状态报告，它实现了计数器报告Reporter和状 态报告StatusReporter。为了不影响主线程的⼯作，TaskReporter有⼀个独⽴的线程，该线程通过 TaskUmbilicalProtocol接⼝，利⽤Hadop的RPC机制，向JobTracker报告Task执⾏情况。

FileSystemStatisticUpdater⽤于记录对⽂件系统的对/写操作字节数，是个简单的⼯具类。

## Hadop源代码分析（mapreduce.lib.partition/reduce/output）

Map的结果，会通过partition分发到Reducer上，Reducer做完Reduce操作后，通过OutputFormat， 进⾏输出，下⾯我们就来分析参与这个过程的类。

Maper的结果，可能送到可能的Combiner做合并，Combiner在系统中并没有⾃⼰的基类，⽽是⽤ Reducer作为Combiner的基类，他们对外的功能是⼀样的，只是使⽤的位置和使⽤时的上下⽂不太⼀ 样⽽已。

Maper最终处理的结果对<key, value>，是需要送到Reducer去合并的，合并的时候，有相同key的键/ 值对会送到同⼀个Reducer那，哪个key到哪个Reducer的分配过程，是由Partitioner规定的，它只有⼀ 个⽅法，输⼊是Map的结果对<key, value>和Reducer的数⽬，输出则是分配的Reducer（整数编 号）。系统缺省的Partitioner是HashPartitioner，它以key的Hash值对Reducer的数⽬取模，得到对应 的Reducer。 Reducer是所有⽤户定制Reducer类的基类，和Maper类似，它也有setup，reduce，cleanup和run⽅ 法，其中setup和cleanup含义和Maper相同，reduce是真正合并Maper结果的地⽅，它的输⼊是key 和这个key对应的所有value的⼀个迭代器，同时还包括Reducer的上下⽂。系统中定义了两个⾮常简单 的Reducer，IntSumReducer和LongSumReducer，分别⽤于对整形/⻓整型的value求和。 Reduce的结果，通过Reducer.Context的⽅法colect输出到⽂件中，和输⼊类似，Hadop引⼊了 OutputFormat。OutputFormat依赖两个辅助接⼝：RecordWriter和OutputComiter，来处理输出。 RecordWriter提供了write⽅法，⽤于输出<key, value>和close⽅法，⽤于关闭对应的输出。 OutputComiter提供了⼀系列⽅法，⽤户通过实现这些⽅法，可以定制OutputFormat⽣存期某些阶 段需要的特殊操作。我们在TaskInputOutputContext中讨论过这些⽅法（明显， TaskInputOutputContext是OutputFormat和Reducer间的桥梁）。 OutputFormat和RecordWriter分别对应着InputFormat和RecordReader，系统提供了空输出 NulOutputFormat（什么结果都不输出，NulOutputFormat.RecordWriter只是示例，系统中没有定 义），LazyOutputFormat（没在类图中出现，不分析），FilterOutputFormat（不分析）和基于⽂件 FileOutputFormat的SequenceFileOutputFormat和TextOutputFormat输出。 基于⽂件的输出FileOutputFormat利⽤了⼀些配置项配合⼯作，包括mapred.output.compres：是否 压缩；mapred.output.compresion.codec：压缩⽅法；mapred.output.dir：输出路径； mapred.work.output.dir：输出⼯作路径。FileOutputFormat还依赖于FileOutputComiter，通过 FileOutputComiter提供⼀些和Job，Task相关的临时⽂件管理功能。如FileOutputComiter的 setupJob，会在输出路径下创建⼀个名为_temporary的临时⽬录，cleanupJob则会删除这个⽬录。 SequenceFileOutputFormat输出和TextOutputFormat输出分别对应输⼊的SequenceFileInputFormat 和TextInputFormat，我们就不再详细分析啦。

## Hadop源代码分析（IFile）

Maper的输出，在发送到Reducer前是存放在本地⽂件系统的，IFile提供了对Maper输出的管理。我 们已经知道，Maper的输出是<Key，Value>对，IFile以记录<key-len, value-len, key,value>的形式存 放了这些数据。为了保存键值对的边界，很⾃然IFile需要保存key-len和value-len。 和IFile相关的类图如下：

其中，⽂件流形式的输⼊和输出是由IFIleInputStream和IFIleOutputStream抽象。以记录形式的读/写 操作由IFile.Reader/IFile.Writer提供，IFile.InMemoryReader⽤于读取存在于内存中的IFile⽂件格式数 据。

我们以输出为例，来分析这部分的实现。⾸先是下图的和序列化反序列化相关的 Serialization/Deserializer，这部分的code是在包org.apache.hadop.io.serializer。序列化由Serializer 抽象，通过Serializer的实现，⽤户可以利⽤serialize⽅法把对象序列化到通过open⽅法打开的输出流 ⾥。Deserializer提供的是相反的过程，对应的⽅法是deserialize。hadop.io.serializer中还实现了配 合⼯作的Serialization和对应的⼯⼚SerializationFactory。两个具体的实现是WritableSerialization和 JavaSerialization，分别对应了Writeble的序列化反序列化和Java本身带的序列化反序列化。

有了Serializer/Deserializer，我们来分析IFile.Writer。Writer的构造函数是：

public Writer(Configuration conf,FSDataOutputStream out, Clas<K> keyClas, Clas<V>valueClas, CompresionCodec codec,Counters.CounterwritesCounter)

conf，配置参数，out是Writer的输出，keyClas 和valueClas是输出的Kay，Value的clas属性， codec是对输出进⾏压缩的⽅法，参数writesCounter⽤于对输出字节数进⾏统计的 Counters.Counter。通过这些参数，我们可以构造我们使⽤的⽀持压缩功能的输出流（类成员out，类 成员rawOut保存了构造函数传⼊的out），相关的计数器，还有就是Kay，Value的Serializer⽅法。 Writer最主要的⽅法是apend⽅法（居然不是write⽅法，呵呵），有两种形式： public void apend(K key, V value)throws IOException { public void apend(DataInputBufer key,DataInputBufer value) apend(K key, V value)的主要过程是检查参数，然后将key和value序列化到DataOutputBufer中，并 获取序列化后的⻓度，最后把⻓度（2个）和DataOutputBufer中的结果写到输出，并复位 DataOutputBufer和计数。apend(DataInputBuferkey, DataInputBufer value)处理过程也⽐较类 似，就不再分析了。 close⽅法中需要注意的是，我们需要标记⽂件尾，或者是流结束。⽬前是通过写2个值为 EOF_MARKER的⻓度来做标记。 IFileOutputStream是⽤于配合Writer的输出流，它会在IFiles的最后添加校验数据。当Writer调⽤ IFileOutputStream的write操作时，IFileOutputStream计算并保持校验和，流被close的时候，校验结 果会写到对应⽂件的⽂件尾。实际上存放在磁盘上的⽂件是⼀系列的<key-len, value-len, key, value> 记录和校验结果。

Reader的相关过程，我们就不再分析了。

## Hadop源代码分析（*IDs类和*Context类）

我们开始来分析Hadop MapReduce的内部的运⾏机制。⽤户向Hadop提交Job（作业），作业在 JobTracker对象的控制下执⾏。Job被分解成为Task（任务），分发到集群中，在TaskTracker的控制 下运⾏。Task包括MapTask和ReduceTask，是MapReduce的Map操作和Reduce操作执⾏的地⽅。这 中任务分布的⽅法⽐较类似于HDFS中NameNode和DataNode的分⼯，NameNode对应的是 JobTracker，DataNode对应的是TaskTracker。JobTracker，TaskTracker和MapReduce的客户端通过 RPC通信，具体可以参考HDFS部分的分析。 我们先来分析⼀些辅助类，⾸先是和ID有关的类，ID的继承树如下：

这张图可以看出现在Hadop的org.apache.hadop.mapred向org.apache.hadop.mapreduce迁移带 来的⼀些问题，其中灰⾊是标注为@Deprecated的。ID携带⼀个整型，实现了WritableComparable接 ⼝，这表明它可以⽐较，⽽且可以被Hadop的io机制串⾏化/解串⾏化（必须实现 compareTo/readFields/write⽅法）。JobID是系统分配给作业的唯⼀标识符，它的toString结果是 job_<jobtrackerID>_<jobNumber>。例⼦：job_2070712173_ 03表明这是 jobtracker2070712173（利⽤jobtracker的开始时间作为ID）的第3号作业。 作业分成任务执⾏，任务号TaskID包含了它所属的作业ID，同时也有任务ID，同时还保持了这是否是 ⼀个Map任务（成员变量isMap）。任务号的字符串表示为 task_<jobtrackerID>_<jobNumber>_[m|r]_<taskNumber>，如 task_2070712173_ 03_m_ 05表示作业2070712173_ 03的 05号任务，改任务是 ⼀个Map任务。 ⼀个任务有可能有多个执⾏（错误恢复/消除Straglers等），所以必须区分任务的多个执⾏，这是通 过类TaskAtemptID来完成，它在任务号的基础上添加了尝试号。⼀个任务尝试号的例⼦是 atempt_2070712173_ 03_m_ 05_0，它是任务task_2070712173_ 03_m_ 05的 第0号尝试。 JVMId⽤于管理任务执⾏过程中的Java虚拟机，我们后⾯再讨论。 为了使Job和Task⼯作，Hadop提供了⼀系列的上下⽂，这些上下⽂保存了Job和Task⼯作的信息。

处于继承树的最上⽅是org.apache.hadop.mapreduce.JobContext，前⾯我们已经介绍过了，它提供 了Job的⼀些只读属性，两个成员变量，⼀个保存了JobID，另⼀个类型为JobConf，JobContext中除 了JobID外，其它的信息都保持在JobConf中。它定义了如下配置项： l mapreduce.inputformat.clas：InputFormat的实现 l mapreduce.map.clas：Maper的实现 l mapreduce.combine.clas:Reducer的实现 l mapreduce.reduce.clas：Reducer的实现

l mapreduce.outputformat.clas:OutputFormat的实现 l mapreduce.partitioner.clas:Partitioner的实现 同时，它提供⽅法，使得通过类名，利⽤Java反射提供的Clas.forName⽅法，获得类对应的Clas。 org.apache.hadop.mapred的JobContext对象⽐org.apache.hadop.mapreduce.JobContext多了成 员变量progres，⽤于获取进度信息，它类型为JobConf成员job指向mapreduce.JobContext对应的 成员，没有添加任何新功能。 JobConf继承⾃Configuration，保持了MapReduce执⾏需要的⼀些配置信息，它管理着46个配置参 数，包括上⾯mapreduce配置项对应的⽼版本形式，如mapreduce.map.clas 对应 mapred.maper.clas。这些配置项我们在使⽤到它们的时候再介绍。 org.apache.hadop.mapreduce.JobContext的⼦类Job前⾯也已经介绍了，后⾯在讨论系统的动态⾏ 为时，再回来看它。 TaskAtemptContext⽤于任务的执⾏，它引⼊了标识任务执⾏的TaskAtemptID和任务状态status，并 提供新的访问接⼝。org.apache.hadop.mapred的TaskAtemptContext继承⾃mapreduce的对应版 本，只是增加了记录进度的progres。 TaskInputOutputContext和它的⼦类都在包org.apache.hadop.mapreduce中，前⾯已经分析过了， 我们就不再罗嗦。

## Hadop源代码分析（包hadop.mapred中的MapReduce接⼝）

前⾯已经完成了对org.apache.hadop.mapreduce的分析，这个包提供了Hadop MapReduce部分的 应⽤API，⽤于⽤户实现⾃⼰的MapReduce应⽤。但这些接⼝是给未来的MapReduce应⽤的，⽬前 MapReduce框架还是使⽤⽼系统（参考补丁 ）。下⾯我们来分析 org.apache.hadop.mapred，⾸先还是从mapred的MapReduce框架开始分析，下⾯的类图（灰⾊部 分为标记为@Deprecated的类/接⼝）：

HADOP-1230

我们把包mapreduce的类图附在下⾯，对⽐⼀下，我们就会发现，org.apache.hadop.mapred中的 MapReduce API相对来说很简单，主要是少了和Context相关的类，那么，好多在mapreduce中通过 context来完成的⼯作，就需要通过参数来传递，如Map中的输出，⽼版本是：

output.colect(key,result); / outputʼs type is:OutputColector 新版本是：

context.write(key, result); / outputʼs type is: Context 它们分别使⽤OutputColector和Maper.Context来输出map的结果，显然，原有OutputColector的新 API中就不再需要。总体来说，⽼版本的API⽐较简单，MapReduce过程中关键的对象都有，但可扩展 性不是很强。同时，⽼版中提供的辅助类也很多，我们前⾯分析的FileOutputFormat，也有对应的实 现，我们就不再讨论了。

⼤⼩: 141 KB

## Hadop源代码分析（包mapreduce.lib.input）

接下来我们按照MapReduce过程中数据流动的顺序，来分解org.apache.hadop.mapreduce.lib.*的相 关内容，并介绍对应的基类的功能。⾸先是input部分，它实现了MapReduce的数据输⼊部分。类图如 下：

类图的右上⻆是InputFormat，它描述了⼀个MapReduceJob的输⼊，通过InputFormat，Hadop可 以： l 检查MapReduce输⼊数据的正确性； l 将输⼊数据切分为逻辑块InputSplit，这些块会分配给Maper； l 提供⼀个RecordReader实现，Maper⽤该实现从InputSplit中读取输⼊的<K,V>对。 在org.apache.hadop.mapreduce.lib.input中，Hadop为所有基于⽂件的InputFormat提供了⼀个虚 基类FileInputFormat。下⾯⼏个参数可以⽤于配置FileInputFormat： l mapred.input.pathFilter.clas：输⼊⽂件过滤器，通过过滤器的⽂件才会加⼊InputFormat； l mapred.min.split.size：最⼩的划分⼤⼩； l mapred.max.split.size：最⼤的划分⼤⼩； l mapred.input.dir：输⼊路径，⽤逗号做分割。 类中⽐较重要的⽅法有：

protectedList<FileStatus> listStatus(Configuration job) 递归获取输⼊数据⽬录中的所有⽂件（包括⽂件信息），输⼊的job是系统运⾏的配置Configuration， 包含了上⾯我们提到的参数。

publicList<InputSplit> getSplits(JobContext context) 将输⼊划分为InputSplit，包含两个循环，第⼀个循环处理所有的⽂件，对于每⼀个⽂件，根据输⼊的 划分最⼤/最⼩值，循环得到⽂件上的划分。注意，划分不会跨越⽂件。 FileInputFormat没有实现InputFormat的createRecordReader⽅法。 FileInputFormat有两个⼦类，SequenceFileInputFormat是Hadop定义的⼀种⼆进制形式存放的键/值 ⽂件（参考 htp:/hadop.apache.org/core/docs/curent/api/org/apache/hadop/io/SequenceFile.html），它有 ⾃⼰定义的⽂件布局。由于它有特殊的扩展名，所以SequenceFileInputFormat重载了listStatus，同 时，它实现了createRecordReader，返回⼀个SequenceFileRecordReader对象。TextInputFormat处 理的是⽂本⽂件，createRecordReader返回的是LineRecordReader的实例。这两个类都没有重载 FileInputFormat的getSplits⽅法，那么，在他们对于的RecordReader中，必须考虑FileInputFormat对 输⼊的划分⽅式。 FileInputFormat的getSplits，返回的是FileSplit。这是⼀个很简单的类，包含的属性（⽂件名，起始偏 移量，划分的⻓度和可能的⽬标机器）已经⾜以说明这个类的功能。

RecordReader⽤于在划分中读取<Key,Value>对。RecordReader有五个虚⽅法，分别是： l initialize：初始化，输⼊参数包括该Reader⼯作的数据划分InputSplit和Job的上下⽂context； l nextKey：得到输⼊的下⼀个Key，如果数据划分已经没有新的记录，返回空； l nextValue：得到Key对应的Value，必须在调⽤nextKey后调⽤； l getProgres：得到现在的进度； l close，来⾃java.io的Closeable接⼝，⽤于清理RecordReader。 我们以LineRecordReader为例，来分析RecordReader的构成。前⾯我们已经分析过FileInputFormat对 ⽂件的划分了，划分完的Split包括了⽂件名，起始偏移量，划分的⻓度。由于⽂件是⽂本⽂件， LineRecordReader的初始化⽅法initialize会创建⼀个基于⾏的读取对象LineReader（定义在 org.apache.hadop.util中，我们就不分析啦），然后跳过输⼊的最开始的部分（只在Split的起始偏移 量不为0的情况下进⾏，这时最开始的部分可能是上⼀个Split的最后⼀⾏的⼀部分）。nextKey的处理 很简单，它使⽤当前的偏移量作为Key，nextValue当然就是偏移量开始的那⼀⾏了（如果⾏很⻓，可 能出现截断）。进度getProgres和close都很简单。

## Hadop源代码分析（包mapreduce.lib.map）

Hadop的MapReduce框架中，Map动作通过Maper类来抽象。⼀般来说，我们会实现⾃⼰特殊的 Maper，并注册到系统中，执⾏时，我们的Maper会被MapReduce框架调⽤。Maper类很简单，包 括⼀个内部类和四个⽅法，静态结构图如下：

内部类Context继承⾃MapContext，并没有引⼊任何新的⽅法。 Maper的四个⽅法是setup，map，cleanup和run。其中，setup和cleanup⽤于管理Maper⽣命周期 中的资源，setup在完成Maper构造，即将开始执⾏map动作前调⽤，cleanup则在所有的map动作完 成后被调⽤。⽅法map⽤于对⼀次输⼊的key/value对进⾏map动作。run⽅法执⾏了上⾯描述的过程， 它调⽤setup，让后迭代所有的key/value对，进⾏map，最后调⽤cleanup。 org.apache.hadop.mapreduce.lib.map中实现了Maper的三个⼦类，分别是InverseMaper（将输 ⼊<key, value> map为输出<value, key>），MultithreadedMaper（多线程执⾏map⽅法）和 TokenCounterMaper（对输⼊的value分解为token并计数）。其中最复杂的是 MultithreadedMaper，我们就以它为例，来分析Maper的实现。

MultithreadedMaper会启动多个线程执⾏另⼀个Maper的map⽅法，它会启动 mapred.map.multithreadedruner.threads（配置项）个线程执⾏Maper： mapred.map.multithreadedruner.clas（配置项）。MultithreadedMaper重写了基类Maper的run ⽅法，启动N个线程（对应的类为MapRuner）执⾏mapred.map.multithreadedruner.clas（我们称 为⽬标Maper）的run⽅法（就是说，⽬标Maper的setup和cleanup会被执⾏多次）。⽬标Maper 共享同⼀份InputSplit，这就意味着，对InputSplit的数据读必须线程安全。为此， MultithreadedMaper引⼊了内部类SubMapRecordReader，SubMapRecordWriter， SubMapStatusReporter，分别继承⾃RecordReader，RecordWriter和StatusReporter，它们通过互 斥访问MultithreadedMaper的Maper.Context，实现了对同⼀份InputSplit的线程安全访问，为 Maper提供所需的Context。这些类的实现⽅法都很简单。

## Hadop源代码分析（包org.apache.hadop.mapreduce）

有了前⼀节的分析，我们来看⼀下具体的接⼝，它们都处于包org.apache.hadop.mapreduce中。

上⾯的图中，类可以分为4种。右上⻆的是从Writeable继承的，和Counter（还有CounterGroup和 Counters，也在这个包中，并没有出现在上⾯的图⾥）和ID相关的类，它们保持MapReduce过程中需 要的⼀些计数器和标识；中间⼤部分是和Context相关的*Context类，它为Maper和Reducer提供了相 关的上下⽂；关于Map和Reduce，对应的类是Maper，Reducer和描述他们的Job（在Hadop中⼀ 次计算任务称之为⼀个job，下⾯的分析中，中⽂为“作业”，相应的task我们称为“任务”）；图中其他 类是配合Maper和Reduce⼯作的⼀些辅助类。 如果你熟悉HTPServlet， 那就能很轻松地理解Hadop采⽤的结构，把整个Hadop看作是容器，那 么Maper和Reduce就是容器⾥的组件，*Context保存了组件的⼀些配置信息，同时也是和容器通信 的机制。 和ID相关的类我们就不再讨论了。我们先看JobContext，它位于*Context继承树的最上⽅，为Job提供 ⼀些只读的信息，如Job的ID，名称等。下⾯的信息是MapReduce过程中⼀些较关键的定制信息： （来⾃ ）：

htp:/ w.ibm.com/developerworks/cn/opensource/os-cn-hadop2/index.html

<table>
  <tr>
    <th>参数</th>
    <th>作⽤</th>
    <th>缺省值</th>
    <th>其它实现</th>
  </tr>
  <tr>
    <td>InputFormat</td>
    <td>将输⼊的数据集切割成 ⼩数据集 InputSplits, 每⼀个 InputSplit 将由 ⼀个 Maper 负责处 理。此外 InputFormat 中还提供⼀个 RecordReader 的实现, 将⼀个 InputSplit 解析 成 <key,value> 对提供 函数。</td>
    <td>TextInputFormat (针对⽂本⽂件，按⾏将 ⽂本⽂件切割成 InputSplits, 并⽤ LineRecordReader 将 InputSplit 解析成 <key,value> 对，key 是 ⾏在⽂件中的位置， value 是⽂件中的⼀⾏)</td>
    <td>SequenceFileInputFor mat</td>
  </tr>
  <tr>
    <td>OutputFormat</td>
    <td>给 map 提供⼀个 RecordWriter 的实现，负责输出最终 结果</td>
    <td>TextOutputFormat (⽤ LineRecordWriter 将最终结果写成纯⽂件 ⽂件,每个 <key,value> 对⼀⾏，key 和 value</td>
    <td>SequenceFileOutputFo rmat</td>
  </tr>
  <tr>
    <td>OutputKeyClas</td>
    <td>输出的最终结果中 key 的类型</td>
    <td>之间⽤ tab 分隔) LongWritable</td>
    <td> </td>
  </tr>
  <tr>
    <td>OutputValueClas</td>
    <td>输出的最终结果中 的类型</td>
    <td>Text</td>
    <td> </td>
  </tr>
  <tr>
    <td>MaperClas</td>
    <td>value Maper 类，实现 map 函数，完成输⼊的 <key,value> 到中间结 果的映射</td>
    <td>IdentityMaper (将输⼊的 <key,value> 原封不动的输出为中间 结果)</td>
    <td>ngSumReducer, LogRegexMaper, InverseMaper</td>
  </tr>
  <tr>
    <td>CombinerClas</td>
    <td>实现 combine 函数，将 中间结果中的重复 key 做合并</td>
    <td>nul (不对中间结果中的重复 key 做合并)</td>
    <td> </td>
  </tr>
  <tr>
    <td>ReducerClas</td>
    <td>Reducer 类，实现 reduce 函数，对中间结 果做合并，形成最终结 果</td>
    <td>IdentityReducer (将中间结果直接输出为 最终结果)</td>
    <td>AcumulatingReducer, LongSumReducer</td>
  </tr>
  <tr>
    <td>InputPath</td>
    <td>设定 job 的输⼊⽬录, job 运⾏时会处理输⼊ ⽬录下的所有⽂件</td>
    <td>nul</td>
    <td> </td>
  </tr>
  <tr>
    <td>OutputPath</td>
    <td>设定 job 的输出⽬录， job 的最终结果会写⼊ 输出⽬录下</td>
    <td>nul</td>
    <td> </td>
  </tr>
  <tr>
    <td>MapOutputKeyClas</td>
    <td>设定 map 函数输出的中 间结果中 key 的类型</td>
    <td>如果⽤户没有设定的 话，使⽤</td>
    <td> </td>
  </tr>
</table>


### OutputKeyClas

<table>
  <tr>
    <th>MapOutputValueClas s</th>
    <th>设定 map 函数输出的中 间结果中 value 的类型</th>
    <th>如果⽤户没有设定的 话，使⽤</th>
    <th> </th>
  </tr>
  <tr>
    <td>OutputKeyComparat or</td>
    <td>对结果中的 key 进⾏排 序时的使⽤的⽐较器</td>
    <td>OutputValuesClas WritableComparable</td>
    <td> </td>
  </tr>
  <tr>
    <td>PartitionerClas</td>
    <td>对中间结果的 key 排序 后，⽤此 Partition 函数 将其划分为R份,每份由 ⼀个 Reducer 负责处 理。</td>
    <td>HashPartitioner (使⽤ Hash 函数做 partition)</td>
    <td>KeyFieldBasedPartition er PipesPartitioner</td>
  </tr>
</table>


Job继承⾃JobContext，提供了⼀系列的set⽅法，⽤于设置Job的⼀些属性（Job更新属性， JobContext读属性），同时，Job还提供了⼀些对Job进⾏控制的⽅法，如下： l mapProgres：map的进度（0—1.0）； l reduceProgres：reduce的进度（0—1.0）； l isComplete：作业是否已经完成； l isSucesful：作业是否成功； l kilJob：结束⼀个在运⾏中的作业； l getTaskCompletionEvents：得到任务完成的应答（成功/失败）； l kilTask：结束某⼀个任务；

