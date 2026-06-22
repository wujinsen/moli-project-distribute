Selector（选择器）是Java NIO中能够检测⼀到多个NIO通道，并能够知晓通道是否为诸如读写事件做 好准备的组件。这样，⼀个单独的线程可以管理多个chanel，从⽽管理多个⽹络连接。

为什么使⽤Selector?

仅⽤单个线程来处理多个Chanels的好处是，只需要更少的线程来处理通道。事实上，可以只⽤⼀个 线程处理所有的通道。对于操作系统来说，线程之间上下⽂切换的开销很⼤，⽽且每个线程都要占⽤ 系统的⼀些资源（如内存）。因此，使⽤的线程越少越好。 但是，需要记住，现代的操作系统和CPU在多任务⽅⾯表现的越来越好，所以多线程的开销随着时间 的推移，变得越来越⼩了。实际上，如果⼀个CPU有多个内核，不使⽤多任务可能是在浪费CPU能 ⼒。不管怎么说，关于那种设计的讨论应该放在另⼀篇不同的⽂章中。在这⾥，只要知道使⽤Selector 能够处理多个通道就⾜够了。

Selector的创建

通过调⽤Selector.open()⽅法创建⼀个Selector，如下：

- 1 Selector selector = Selector.open();

- 2


# 向Selector注册通道

为了将Chanel和Selector配合使⽤，必须将chanel注册到selector上。通过 SelectableChanel.register()⽅法来实现，如下：

- 1 channel.configureBlocking(false);

- 2 SelectionKey key = channel.register(selector,

- 3 Selectionkey.OP_READ);

- 4


与Selector⼀起使⽤时，Chanel必须处于⾮阻塞模式下。这意味着不能将FileChanel与Selector⼀起 使⽤，因为FileChanel不能切换到⾮阻塞模式。⽽套接字通道都可以。 注意register()⽅法的第⼆个参数。这是⼀个“interest集合”，意思是在通过Selector监听Chanel时对 什么事件感兴趣。可以监听四种不同类型的事件：

- 1.
- 2.
- 3.


Conect Acept Read

4.

Write

通道触发了⼀个事件意思是该事件已经就绪。所以，某个chanel成功连接到另⼀个服务器称为“连接 就绪”。⼀个server socket chanel准备好接收新进⼊的连接称为“接收就绪”。⼀个有数据可读的通道 可以说是“读就绪”。等待写数据的通道可以说是“写就绪”。 这四种事件⽤SelectionKey的四个常量来表示：

- 1.
- 2.
- 3.
- 4.


SelectionKey.OP_CONECT SelectionKey.OP_ACEPT SelectionKey.OP_READ SelectionKey.OP_WRITE

如果你对不⽌⼀种事件感兴趣，那么可以⽤“位或”操作符将常量连接起来，如下：

- 1 int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

- 2


在下⾯还会继续提到interest集合。

# SelectionKey

在上⼀⼩节中，当向Selector注册Chanel时，register()⽅法会返回⼀个SelectionKey对象。这个对象 包含了⼀些你感兴趣的属性：

interest集合

ready集合

Chanel Selector 附加的对象（可选）

下⾯我会描述这些属性。

interest集合

就像 ⼀节中所描述的，interest集合是你所选择的感兴趣的事件集合。可以通过 SelectionKey读写interest集合，像这样：

向Selector注册通道

- 1 int interestSet = selectionKey.interestOps();

- 2

boolean isInterestedInAccept = (interestSet & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT；

- 3

- 4 boolean isInterestedInConnect = interestSet & SelectionKey.OP_CONNECT;

- 5 boolean isInterestedInRead = interestSet & SelectionKey.OP_READ;

- 6 boolean isInterestedInWrite = interestSet & SelectionKey.OP_WRITE;

- 7


可以看到，⽤“位与”操作interest 集合和给定的SelectionKey常量，可以确定某个确定的事件是否在 interest 集合中。

## ready集合

ready 集合是通道已经准备就绪的操作的集合。在⼀次选择(Selection)之后，你会⾸先访问这个ready set。Selection将在下⼀⼩节进⾏解释。可以这样访问ready集合：

- 1 int readySet = selectionKey.readyOps();

- 2


可以⽤像检测interest集合那样的⽅法，来检测chanel中什么事件或操作已经就绪。但是，也可以使 ⽤以下四个⽅法，它们都会返回⼀个布尔类型：

- 1 selectionKey.isAcceptable();

- 2 selectionKey.isConnectable();

- 3 selectionKey.isReadable();

- 4 selectionKey.isWritable();

- 5


## Chanel + Selector

从SelectionKey访问Chanel和Selector很简单。如下：

- 1 Channel channel = selectionKey.channel();

- 2 Selector selector = selectionKey.selector();

- 3


## 附加的对象

可以将⼀个对象或者更多信息附着到SelectionKey上，这样就能⽅便的识别某个给定的通道。例如，可 以附加 与通道⼀起使⽤的Bufer，或是包含聚集数据的某个对象。使⽤⽅法如下：

- 1 selectionKey.attach(theObject);

- 2 Object attachedObj = selectionKey.attachment();

- 3


还可以在⽤register()⽅法向Selector注册Chanel的时候附加对象。如：

- 1 SelectionKey key = channel.register(selector, SelectionKey.OP_READ, theObject);

- 2


# 通过Selector选择通道

⼀旦向Selector注册了⼀或多个通道，就可以调⽤⼏个重载的select()⽅法。这些⽅法返回你所感兴趣 的事件（如连接、接受、读或写）已经准备就绪的那些通道。换句话说，如果你对“读就绪”的通道感 兴趣，select()⽅法会返回读事件已经就绪的那些通道。 下⾯是select()⽅法：

int select()

int select(long timeout)

int selectNow()

select() 阻塞到⾄少有⼀个通道在你注册的事件上就绪了。 select(long timeout) 和select()⼀样，除了最⻓会阻塞timeout毫秒(参数)。 selectNow() 不会阻塞，不管什么通道就绪都⽴刻返回（译 者 注 ： 此 ⽅ 法 执 ⾏ ⾮ 阻 塞 的 选 择 操 作 。

如 果 ⾃ 从 前 ⼀ 次 选 择 操 作 后 ， 没 有 通道 变 成 可 选 择 的 ， 则 此 ⽅ 法 直 接 返 回 零 。）。 select()⽅法返回的int值表示有多少通道已经就绪。亦即，⾃上次调⽤select()⽅法后有多少通道变成 就绪状态。如果调⽤select()⽅法，因为有⼀个通道变成就绪状态，返回了1，若再次调⽤select()⽅ 法，如果另⼀个通道就绪了，它会再次返回1。如果对第⼀个就绪的chanel没有做任何操作，现在就 有两个就绪的通道，但在每次select()⽅法调⽤之间，只有⼀个通道就绪了。

## selectedKeys()

⼀旦调⽤了select()⽅法，并且返回值表明有⼀个或更多个通道就绪了，然后可以通过调⽤selector的 selectedKeys()⽅法，访问“已选择键集（selected key set）”中的就绪通道。如下所示：

- 1 Set selectedKeys = selector.selectedKeys();

- 2


当像Selector注册Chanel时，Chanel.register()⽅法会返回⼀个SelectionKey 对象。这个对象代表了 注册到该Selector的通道。可以通过SelectionKey的selectedKeySet()⽅法访问这些对象。 可以遍历这个已选择的键集合来访问就绪的通道。如下：

- 1 Set selectedKeys = selector.selectedKeys();

- 2 Iterator keyIterator = selectedKeys.iterator();

- 3 while(keyIterator.hasNext()) {

- 4 SelectionKey key = keyIterator.next();

- 5 if(key.isAcceptable()) {

- 6 // a connection was accepted by a ServerSocketChannel.

- 7 } else if (key.isConnectable()) {

- 8 // a connection was established with a remote server.

- 9 } else if (key.isReadable()) {

- 10 // a channel is ready for reading

- 11 } else if (key.isWritable()) {

- 12 // a channel is ready for writing

- 13 }

- 14 keyIterator.remove();

- 15 }

- 16


这个循环遍历已选择键集中的每个键，并检测各个键所对应的通道的就绪事件。 注意每次迭代末尾的keyIterator.remove()调⽤。Selector不会⾃⼰从已选择键集中移除SelectionKey实 例。必须在处理完通道时⾃⼰移除。下次该通道变成就绪时，Selector会再次将其放⼊已选择键集中。 SelectionKey.chanel()⽅法返回的通道需要转型成你要处理的类型，如ServerSocketChanel或 SocketChanel等。

# wakeUp()

某个线程调⽤select()⽅法后阻塞了，即使没有通道已经就绪，也有办法让其从select()⽅法返回。只 要让其它线程在第⼀个线程调⽤select()⽅法的那个对象上调⽤Selector.wakeup()⽅法即可。阻塞在 select()⽅法上的线程会⽴⻢返回。 如果有其它线程调⽤了wakeup()⽅法，但当前没有线程阻塞在select()⽅法上，下个调⽤select()⽅法 的线程会⽴即“醒来（wake up）”。

close()

⽤完Selector后调⽤其close()⽅法会关闭该Selector，且使注册到该Selector上的所有SelectionKey实 例⽆效。通道本身并不会关闭。

完整的示例

这⾥有⼀个完整的示例，打开⼀个Selector，注册⼀个通道注册到这个Selector上(通道的初始化过程 略去),然后持续监控这个Selector的四种事件（接受，连接，读，写）是否就绪。

- 1 Selector selector = Selector.open();

- 2 channel.configureBlocking(false);

- 3 SelectionKey key = channel.register(selector, SelectionKey.OP_READ);

- 4 while(true) {

- 5 int readyChannels = selector.select();

- 6 if(readyChannels == 0) continue;

- 7 Set selectedKeys = selector.selectedKeys();

- 8 Iterator keyIterator = selectedKeys.iterator();

- 9 while(keyIterator.hasNext()) {

- 10 SelectionKey key = keyIterator.next();

- 11 if(key.isAcceptable()) {

- 12 // a connection was accepted by a ServerSocketChannel.

- 13 } else if (key.isConnectable()) {

- 14 // a connection was established with a remote server.

- 15 } else if (key.isReadable()) {

- 16 // a channel is ready for reading

- 17 } else if (key.isWritable()) {

- 18 // a channel is ready for writing

- 19 }

- 20 keyIterator.remove();

- 21 }

- 22 }

- 23


