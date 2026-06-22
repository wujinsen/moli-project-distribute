Botstrap

- 1、客户端的引导类，⽤来连接远程主机

- 2、通过调⽤botstrap.conect()链接服务端

- 3、连接服务端时指定ip、port

- 4、有1个EventLopGroup


ServerBotstrap

- 1、服务端的引导类，⽤来接受客户端请求
- 2、通过调⽤serverBotstrap.bind()接受客户端

- 3、在服务器监听⼀个端⼝轮询客户端的“Botstrap”或DatagramChanel是否连接服务器

- 4、⽤来绑定本地端⼝，有2个EventLopGroup，⼀个⽤来接受链接。⼀个⽤来处理消息


EventLop 1、就是⼀个Chanel执⾏实际⼯作的线程,⼀个事件循环线程，也就是线程中有while（true）

EventLopGroup

- 1、线程池，⽤来执⾏EventLop，
- 2、EventLoopGroup包含⼀个或多个EventLoop


Chanel 1、包含消息事件的管道

ChanelPipeline

- 1、⽤来管理ChanelHandler的⼀个容器，ChanelHandler在pipe中有序的执⾏

- 2、每个ChanelHandler处理各⾃的数据(例如⼊站数据只能由ChanelInboundHandler处理)
- 3、处理完成后将转换的数据放到ChanelPipeline中交给下⼀个ChanelHandler继续处理，直到最后 ⼀个ChanelHandler处理完成。
- 4、 在ChanelPipeline中，如果消息被读取或有任何其他的⼊站事件，消息将从ChanelPipeline的头 部开始传递给第⼀个ChanelInboundHandler，这个ChanelInboundHandler可以处理该消息或将消 息传递到下⼀个ChanelInboundHandler中，⼀旦在ChanelPipeline中没有剩余的 ChanelInboundHandler后，ChanelPipeline就知道消息已被所有的饿Handler处理完成了。
- 5、反过来也是如此，任何出站事件或写⼊将从ChanelPipeline的尾部开始，并传递到最后⼀个 ChanelOutboundHandler。ChanelOutboundHandler的作⽤和ChanelInboundHandler相同，它可 以传递事件消息到下⼀个Handler或者⾃⼰处理消息。不同的是ChanelOutboundHandler是从 ChanelPipeline的尾部开始，⽽ChanelInboundHandler是从ChanelPipeline的头部开始，当处理完 第⼀个ChanelOutboundHandler处理完成后会出发⼀些操作，⽐如⼀个写操作。


ChanelInitializer

- 1、⽤来配置Handlers
- 2、通过ChannelPipeline来添加ChannelHandler，如发送和接收消息，这些Handlers将确定发的是什 么消息。
- 3、ChannelInitializer⾃⾝也是⼀个ChannelHandler，在添加完其他的handlers之后会⾃动从 ChannelPipeline中删除⾃⼰


ChanelHandler

- 1、ChanelHandler处理业务数据的代码

- 2、ChanelHandler会在程序的“引导”阶段被添加ChanelPipeline中，依赖于ChanelPipeline来决

定它们执⾏的顺序

- 3、handler是⽗接⼝，ChanelInboundHandler和ChanelOutboundHandler都实现

ChanelHandler接⼝

- 4、ChanelInboundHandler：若数据时从server到client则是“⼊站(inbound)”

- 5、ChanelOutboundHandler：数据是从client到server则是“出站(outbound)”


SimpleChanelInboundHandler 继承ChanelInBoundHandler，应⽤程序在实现逻辑的时候，只需扩展这个类即可，处理消息⽤ chanelRead0()⽅法

ChannelHandlerContext 每个handler都会获取⼀个ChannelHandlerContext ChannelHandlerContext的作⽤是，在pipeline中⼀个handler可以通过ChannelHandlerContext调⽤下 ⾯的handler，继续向后发送数据

Encoders:编码器

- 1、继承MesageToByteEncoder，将object转换成byte[]
- 2、实际上也是⼀个handler，继承ChanelOutboundHandlerAdapter


decoders：解码器

- 1、继承ByteToMesageDecoder，将byte[]转换为object
- 2、实际上也是⼀个handler，继承ChanelInboundHandlerAdapter


OioServerSocketChanel 传输消息的时候采⽤阻塞模式

ByteBufer 1、字节数组缓冲区，将channel中的数据缓存在buﬀer中

- 1、⽤来创建缓冲区的⼯具类

- 2、⼀个很好的经过优化的数据容器，我们可以将字节数据有效的添加到ByteBuf中或从ByteBuf中 获取数据。ByteBuf有2部分：⼀个⽤于读，⼀个⽤于写。我们可以按顺序的读取数据，并且可以 跳到开始重新读⼀遍。所有的数据操作，我们只需要做的是调整读取数据索引和再次开始读操 作。

- 3、分为3种：


- 1、Heap Bufer，将数据放到jvm的堆中
- 2、Direct Bufer：堆内存之外的直接内存
- 3、Composite Bufer(复合缓冲区)


Unpoled

/创建复合缓冲区

CompositeByteBuf compBuf = Unpoled.compositeBufer();

/创建堆缓冲区

ByteBuf heapBuf = Unpoled.bufer(8);

/创建直接缓冲区

ByteBuf directBuf = Unpoled.directBufer(16);

