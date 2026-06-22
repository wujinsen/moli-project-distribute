Java NIO 由以下⼏个核⼼部分组成：

Chanels

Bufers

Selectors

Chanel 和 Bufer 基本上，所有的 IO 在NIO 中都从⼀个Chanel 开始。Chanel 有点象流。 数据可以从Chanel读到 Bufer中，也可以从Bufer 写到Chanel中。这⾥有个图示：

![image 1](<Java NIO 概述.note_images/imageFile1.png>)

Chanel和Bufer有好⼏种类型： FileChanel 从⽂件中读写数据。 DatagramChanel 能通过UDP读写⽹络中的数据。 SocketChanel 能通过TCP读写⽹络中的数据。 ServerSocketChanel可以监听新进来的TCP连接，像Web服务器那样。对每⼀个新进来的连接都会

创建⼀个SocketChanel。 正如你所看到的，这些通道涵盖了UDP 和 TCP ⽹络IO，以及⽂件IO。

以下是Java NIO⾥关键的Bufer实现：

ByteBufer CharBufer DoubleBufer

FloatBufer

IntBufer

LongBufer

ShortBufer

# Selector

Selector允许单线程处理多个 Chanel。

如果你的应⽤打开了多个连接（通道），但每个连接的流量都很低，使⽤Selector就会很⽅便。例如， 在⼀个聊天服务器中。 这是在⼀个单线程中使⽤⼀个Selector处理3个Chanel的图示：

![image 2](<Java NIO 概述.note_images/imageFile2.png>)

要使⽤Selector，得向Selector注册Chanel，然后调⽤它的select()⽅法。这个⽅法会⼀直阻塞到某个 注册的通道有事件就绪。⼀旦这个⽅法返回，线程就可以处理这些事件，事件的例⼦有如新连接进 来，数据接收等。

