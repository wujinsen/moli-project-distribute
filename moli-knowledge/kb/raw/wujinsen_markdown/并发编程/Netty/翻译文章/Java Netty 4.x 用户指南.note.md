htp:/ w.cnblogs.com/damowang/p/625628.html

#### Java Netty 4.x ⽤户指南

# 问题

今天，我们使⽤通⽤的应⽤程序或者类库来实现互相通讯，⽐如，我们经常使⽤⼀个 HTTP 客户端库 来从 web 服务器上获取信息，或者通过 web 服务来执⾏⼀个远程的调⽤。 然⽽，有时候⼀个通⽤的协议或他的实现并没有很好的满⾜需求。⽐如我们⽆法使⽤⼀个通⽤的 HTTP 服务器来处理⼤⽂件、电⼦邮件以及近实时消息，⽐如⾦融信息和多⼈游戏数据。我们需要⼀ 个⾼度优化的协议来处理⼀些特殊的场景。例如你可能想实现⼀个优化了的 Ajax 的聊天应⽤、媒体 流传输或者是⼤⽂件传输器，你甚⾄可以⾃⼰设计和实现⼀个全新的协议来准确地实现你的需求。 另⼀个不可避免的情况是当你不得不处理遗留的专有协议来确保与旧系统的互操作性。在这种情况 下，重要的是我们如何才能快速实现协议⽽不牺牲应⽤的稳定性和性能。

# 解决⽅案

Netty 是⼀个提供 asynchronous event-driven （异步事件驱动）的⽹络应⽤框架，是⼀个⽤以快 速开发⾼性能、⾼可靠性协议的服务器和客户端。 换句话说，Netty 是⼀个 NIO 客户端服务器框架，使⽤它可以快速简单地开发⽹络应⽤程序，⽐如服 务器和客户端的协议。Netty ⼤⼤简化了⽹络程序的开发过程⽐如 TCP 和 UDP 的 socket 服务的开 发。 “快速和简单”并不意味着应⽤程序会有难维护和性能低的问题，Netty 是⼀个精⼼设计的框架，它从 许多协议的实现中吸收了很多的经验⽐如 FTP、SMTP、HTTP、许多⼆进制和基于⽂本的传统协议.因 此，Netty 已经成功地找到⼀个⽅式,在不失灵活性的前提下来实现开发的简易性，⾼性能，稳定性。 有⼀些⽤户可能已经发现其他的⼀些⽹络框架也声称⾃⼰有同样的优势，所以你可能会问是 Netty 和 它们的不同之处。答案就是 Netty 的哲学设计理念。Netty 从开始就为⽤户提供了⽤户体验最好的 API 以及实现设计。正是因为 Netty 的哲学设计理念，才让您得以轻松地阅读本指南并使⽤ Netty。

# 开始

本章围绕 Netty 的核⼼架构，通过简单的示例带你快速⼊⻔。当你读完本章节，你⻢上就可以⽤ Netty 写出⼀个客户端和服务器。 如果你在学习的时候喜欢“top-down（⾃顶向下）”，那你可能需要要从第⼆章《Architectural Overview （架构总览）》开始，然后再回到这⾥。

开始之前

在运⾏本章示例之前，需要准备：最新版的 Netty 以及 JDK 1.6 或以上版本。最新版的 Netty 在这

。⾃⾏下载 JDK。 阅读本章节过程中，你可能会对相关类有疑惑，关于这些类的详细的信息请请参考 API 说明⽂档。为 了⽅便，所有⽂档中涉及到的类名字都会被关联到⼀个在线的 API 说明。当然，如果有任何错误信 息、语法错误或者你有任何好的建议来改进⽂档说明，那么请 。

下载

联系Netty社区

写个丢弃服务器

世上最简单的协议不是'Hello, World!' ⽽是 。这个协议将会丢掉任何收到的数据， ⽽不响应。 为了实现 DISCARD 协议，你只需忽略所有收到的数据。让我们从 handler （处理器）的实现开 始，handler 是由 Netty ⽣成⽤来处理 I/O 事件的。

DISCARD(丢弃)

![image 1](<Java Netty 4.x 用户指南.note_images/imageFile1.png>)

复制代码

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerContext; import io.netty.channel.ChannelInboundHandlerAdapter;

/**

- * 处理服务端 channel.

- */


public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)

@Override public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)

// 默默地丢弃收到的数据 ((ByteBuf) msg).release(); // (3)

}

@Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)

// 当出现异常就关闭连接 cause.printStackTrace(); ctx.close();

} }

![image 2](<Java Netty 4.x 用户指南.note_images/imageFile2.png>)

复制代码

- 1.DiscardServerHandler 继承⾃ ，这个类实现了 接⼝，ChannelInboundHandler 提供了许多事件处理的接⼝⽅法，然后你可以覆盖

这些⽅法。现在仅仅只需要继承 ChannelInboundHandlerAdapter 类⽽不是你⾃⼰去实现接⼝⽅ 法。

- 2.这⾥我们覆盖了 chanelRead() 事件处理⽅法。每当从客户端收到新的数据时，这个⽅法会在收到 消息时被调⽤，这个例⼦中，收到的消息的类型是

- 3.为了实现 DISCARD 协议，处理器不得不忽略所有接受到的消息。ByteBuf 是⼀个引⽤计数对象， 这个对象必须显示地调⽤ release() ⽅法来释放。请记住处理器的职责是释放所有传递到处理器的引 ⽤计数对象。通常，channelRead() ⽅法的实现就像下⾯的这段代码：


ChannelInboundHandlerAdapter ChannelInb oundHandler

ByteBuf

![image 3](<Java Netty 4.x 用户指南.note_images/imageFile3.png>)

复制代码

@Override public void channelRead(ChannelHandlerContext ctx, Object msg) {

try {

// Do something with msg } finally {

ReferenceCountUtil.release(msg); }

}

![image 4](<Java Netty 4.x 用户指南.note_images/imageFile4.png>)

复制代码

- 4.exceptionCaught() 事件处理⽅法是当出现 Throwable 对象才会被调⽤，即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。在⼤部分情况下，捕获的异常应该被记录下来并且把关 联的 channel 给关闭掉。然⽽这个⽅法的处理⽅式会在遇到不同异常的情况下有不同的实现，⽐如你 可能想在关闭连接之前发送⼀个错误码的响应消息。 ⽬前为⽌⼀切都还不错，我们已经实现了 DISCARD 服务器的⼀半功能，剩下的需要编写⼀个 main() ⽅法来启动服务端的 DiscardServerHandler。


![image 5](<Java Netty 4.x 用户指南.note_images/imageFile5.png>)

复制代码

import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture; import io.netty.channel.ChannelInitializer; import io.netty.channel.ChannelOption; import io.netty.channel.EventLoopGroup; import io.netty.channel.nio.NioEventLoopGroup; import io.netty.channel.socket.SocketChannel; import io.netty.channel.socket.nio.NioServerSocketChannel;

/**

- * 丢弃任何进⼊的数据

- */


public class DiscardServer {

private int port;

public DiscardServer(int port) {

this.port = port; }

public void run() throws Exception { EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1) EventLoopGroup workerGroup = new NioEventLoopGroup(); try {

ServerBootstrap b = new ServerBootstrap(); // (2) b.group(bossGroup, workerGroup)

.channel(NioServerSocketChannel.class) // (3)

.childHandler(new ChannelInitializer<SocketChannel>() { // (4) @Override public void initChannel(SocketChannel ch) throws Exception {

ch.pipeline().addLast(new DiscardServerHandler()); }

})

.option(ChannelOption.SO_BACKLOG, 128) // (5) .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

// 绑定端⼝，开始接收进来的连接 ChannelFuture f = b.bind(port).sync(); // (7)

// 等待服务器 socket 关闭 。 // 在这个例⼦中，这不会发⽣，但你可以优雅地关闭你的服务器。 f.channel().closeFuture().sync();

} finally { workerGroup.shutdownGracefully(); bossGroup.shutdownGracefully();

} }

public static void main(String[] args) throws Exception { int port; if (args.length > 0) {

port = Integer.parseInt(args[0]); } else {

port = 8080;

} new DiscardServer(port).run();

} }

![image 6](<Java Netty 4.x 用户指南.note_images/imageFile6.png>)

复制代码

- 1. 是⽤来处理I/O操作的多线程事件循环器，Netty 提供了许多不同的 的实现⽤来处理不同的传输。在这个例⼦中我们实现了⼀个服务端的应⽤，因此会有2个

NioEventLoopGroup 会被使⽤。第⼀个经常被叫做‘boss’，⽤来接收进来的连接。第⼆个经常被叫 做‘worker’，⽤来处理已经被接收的连接，⼀旦‘boss’接收到连接，就会把连接信息注册 到‘worker’上。如何知道多少个线程已经被使⽤，如何映射到已经创建的 上都需要依赖于 EventLoopGroup 的实现，并且可以通过构造函数来配置他们的关系。

- 2. 是⼀个启动 NIO 服务的辅助启动类。你可以在这个服务中直接使⽤ Channel， 但是这会是⼀个复杂的处理过程，在很多情况下你并不需要这样做。

- 3.这⾥我们指定使⽤ 类来举例说明⼀个新的 Channel 如何接收进来的连 接。

- 4.这⾥的事件处理类经常会被⽤来处理⼀个最近的已经接收的 Channel。 是⼀个 特殊的处理类，他的⽬的是帮助使⽤者配置⼀个新的 Channel。也许你想通过增加⼀些处理类⽐如 DiscardServerHandler 来配置⼀个新的 Channel 或者其对应的 来实现你的⽹络 程序。当你的程序变的复杂时，可能你会增加更多的处理类到 pipline 上，然后提取这些匿名类到最 顶层的类上。

- 5.你可以设置这⾥指定的 Channel 实现的配置参数。我们正在写⼀个TCP/IP 的服务端，因此我们被 允许设置 socket 的参数选项⽐如tcpNoDelay 和 keepAlive。请参考 和详细的

实现的接⼝⽂档以此可以对ChannelOption 的有⼀个⼤概的认识。

- 6.你关注过 option() 和 childOption() 吗？option() 是提供给 ⽤来接 收进来的连接。childOption() 是提供给由⽗管道 接收到的连接，在这个例⼦中也 是 NioServerSocketChannel。

- 7.我们继续，剩下的就是绑定端⼝然后启动服务。这⾥我们在机器上绑定了机器所有⽹卡上的 8080 端⼝。当然现在你可以多次调⽤ bind() ⽅法(基于不同绑定地址)。 恭喜！你已经熟练地完成了第⼀个基于 Netty 的服务端程序。 查看收到的数据 现在我们已经编写出我们第⼀个服务端，我们需要测试⼀下他是否真的可以运⾏。最简单的测试⽅法 是⽤ telnet 命令。例如，你可以在命令⾏上输⼊telnet localhost 8080或者其他类型参数。


NioEventLoopGroup EventL oopGroup

Channel

ServerBootstrap

NioServerSocketChannel

ChannelInitializer

ChannelPipeline

ChannelOption C hannelConfig

NioServerSocketChannel ServerChannel

![image 7](<Java Netty 4.x 用户指南.note_images/imageFile7.png>)

![image 8](<Java Netty 4.x 用户指南.note_images/imageFile8.png>)

然⽽我们能说这个服务端是正常运⾏了吗？事实上我们也不知道，因为他是⼀个 discard 服务，你根 本不可能得到任何的响应。为了证明他仍然是在正常⼯作的，让我们修改服务端的程序来打印出他到 底接收到了什么。 我们已经知道 channelRead() ⽅法是在数据被接收的时候调⽤。让我们放⼀些代码到 DiscardServerHandler 类的 channelRead() ⽅法。

![image 9](<Java Netty 4.x 用户指南.note_images/imageFile9.png>)

复制代码

@Override public void channelRead(ChannelHandlerContext ctx, Object msg) {

ByteBuf in = (ByteBuf) msg; try {

while (in.isReadable()) { // (1) System.out.print((char) in.readByte()); System.out.flush();

} } finally {

ReferenceCountUtil.release(msg); // (2) }

}

![image 10](<Java Netty 4.x 用户指南.note_images/imageFile10.png>)

复制代码

- 1.这个低效的循环事实上可以简化 为:System.out.println(in.toString(io.netty.util.CharsetUtil.US_ASCII))

- 2.或者，你可以在这⾥调⽤ in.release()。 如果你再次运⾏ telnet 命令，你将会看到服务端打印出了他所接收到的消息。


![image 11](<Java Netty 4.x 用户指南.note_images/imageFile11.png>)

完整的discard server代码放在了 包下⾯。

io.netty.example.discard

https://github.com/waylau/netty-4-user-guide-demos

译 者 注 ： 翻 译 版 本 的 项 ⽬ 源 码 ⻅ 中 的 com.waylau.netty.demo.discard 包 下

### 写个应答服务器

到⽬前为⽌，我们虽然接收到了数据，但没有做任何的响应。然⽽⼀个服务端通常会对⼀个请求作出 响应。让我们学习怎样在 协议的实现下编写⼀个响应消息给客户端，这个协议针对任何接收的 数据都会返回⼀个响应。 和 discard server 唯⼀不同的是把在此之前我们实现的 channelRead() ⽅法，返回所有的数据替代 打印接收数据到控制台上的逻辑。因此，需要把 channelRead() ⽅法修改如下：

ECHO

@Override public void channelRead(ChannelHandlerContext ctx, Object msg) {

ctx.write(msg); // (1) ctx.flush(); // (2)

}

对象提供了许多操作，使你能够触发各种各样的 I/O 事件和操作。这 ⾥我们调⽤了 write(Object) ⽅法来逐字地把接受到的消息写⼊。请注意不同于 DISCARD 的例 ⼦我们并没有释放接受到的消息，这是因为当写⼊的时候 Netty 已经帮我们释放了。

ChannelHandlerContext

- 1.
- 2.


ctx.write(Object) ⽅法不会使消息写⼊到通道上，他被缓冲在了内部，你需要调⽤ ctx.flush() ⽅法来把缓冲区中数据强⾏输出。或者你可以⽤更简洁的 cxt.writeAndFlush(msg) 以达到同样 的⽬的。

如果你再⼀次运⾏ telnet 命令，你会看到服务端会发回⼀个你已经发送的消息。 完整的echo服务的代码放在了 包下⾯。

io.netty.example.echo https://github.com/waylau/netty-4-user-guide-demos

译 者 注 ： 翻 译 版 本 的 项 ⽬ 源 码 ⻅ 中 的 com.waylau.netty.demo.echo 包 下

### 写个时间服务器

TIME

在这个部分被实现的协议是 协议。和之前的例⼦不同的是在不接受任何请求时他会发送⼀个含 32位的整数的消息，并且⼀旦消息发送就会⽴即关闭连接。在这个例⼦中，你会学习到如何构建和发 送⼀个消息，然后在完成时关闭连接。 因为我们将会忽略任何接收到的数据，⽽只是在连接被创建发送⼀个消息，所以这次我们不能使⽤ channelRead() ⽅法了，代替他的是，我们需要覆盖 channelActive() ⽅法，下⾯的就是实现的内 容：

![image 12](<Java Netty 4.x 用户指南.note_images/imageFile12.png>)

复制代码

public class TimeServerHandler extends ChannelInboundHandlerAdapter {

@Override public void channelActive(final ChannelHandlerContext ctx) { // (1)

final ByteBuf time = ctx.alloc().buffer(4); // (2) time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));

final ChannelFuture f = ctx.writeAndFlush(time); // (3) f.addListener(new ChannelFutureListener() {

@Override public void operationComplete(ChannelFuture future) {

assert f == future; ctx.close();

}

}); // (4) }

@Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

cause.printStackTrace(); ctx.close();

} }

![image 13](<Java Netty 4.x 用户指南.note_images/imageFile13.png>)

复制代码

- 1.channelActive() ⽅法将会在连接被建⽴并且准备进⾏通信时被调⽤。因此让我们在这个⽅法⾥完 成⼀个代表当前时间的32位整数消息的构建⼯作。

- 2.为了发送⼀个新的消息，我们需要分配⼀个包含这个消息的新的缓冲。因为我们需要写⼊⼀个32位 的整数，因此我们需要⼀个⾄少有4个字节的 。通过 ChannelHandlerContext.alloc() 得 到⼀个当前的 ，然后分配⼀个新的缓冲。

- 3.和往常⼀样我们需要编写⼀个构建好的消息。但是等⼀等，flip 在哪？难道我们使⽤ NIO 发送消息 时不是调⽤ java.nio.ByteBuffer.flip() 吗？ByteBuf 之所以没有这个⽅法因为有两个指针，⼀个对 应读操作⼀个对应写操作。当你向 ByteBuf ⾥写⼊数据的时候写指针的索引就会增加，同时读指针的 索引没有变化。读指针索引和写指针索引分别代表了消息的开始和结束。 ⽐较起来，NIO 缓冲并没有提供⼀种简洁的⽅式来计算出消息内容的开始和结尾，除⾮你调⽤ flip ⽅ 法。当你忘记调⽤ flip ⽅法⽽引起没有数据或者错误数据被发送时，你会陷⼊困境。这样的⼀个错误 不会发⽣在 Netty 上，因为我们对于不同的操作类型有不同的指针。你会发现这样的使⽤⽅法会让你 过程变得更加的容易，因为你已经习惯⼀种没有使⽤ flip 的⽅式。 另外⼀个点需要注意的是 ChannelHandlerContext.write() (和 writeAndFlush() )⽅法会返回⼀ 个 对象，⼀个 ChannelFuture 代表了⼀个还没有发⽣的 I/O 操作。这意味着任何 ⼀个请求操作都不会⻢上被执⾏，因为在 Netty ⾥所有的操作都是异步的。举个例⼦下⾯的代码中在


ByteBuf ByteBufAllocator

ChannelFuture

消息被发送之前可能会先关闭连接。

Channel ch = ...; ch.writeAndFlush(message); ch.close();

因此你需要在 write() ⽅法返回的 ChannelFuture 完成后调⽤ close() ⽅法，然后当他的写操作已 经完成他会通知他的监听者。请注意,close() ⽅法也可能不会⽴⻢关闭，他也会返回⼀个 ChannelFuture。

- 4.当⼀个写请求已经完成是如何通知到我们？这个只需要简单地在返回的 ChannelFuture 上增加⼀ 个 。这⾥我们构建了⼀个匿名的 ChannelFutureListener 类⽤来在操作完 成时关闭 Channel。


ChannelFutureListener

或者，你可以使⽤简单的预定义监听器代码:

f.addListener(ChannelFutureListener.CLOSE);

为了测试我们的time服务如我们期望的⼀样⼯作，你可以使⽤ UNIX 的 rdate 命令

$ rdate -o <port> -p <host>

Port 是你在main()函数中指定的端⼝，host 使⽤ locahost 就可以了。

### 写个时间客户端

不像 DISCARD 和 ECHO 的服务端，对于 TIME 协议我们需要⼀个客户端,因为⼈们不能把⼀个32位 的⼆进制数据翻译成⼀个⽇期或者⽇历。在这⼀部分，我们将会讨论如何确保服务端是正常⼯作的， 并且学习怎样⽤Netty 编写⼀个客户端。 在 Netty 中,编写服务端和客户端最⼤的并且唯⼀不同的使⽤了不同的 和 的实 现。请看⼀下下⾯的代码：

BootStrap Channel

![image 14](<Java Netty 4.x 用户指南.note_images/imageFile14.png>)

复制代码

public class TimeClient {

public static void main(String[] args) throws Exception {

String host = args[0]; int port = Integer.parseInt(args[1]); EventLoopGroup workerGroup = new NioEventLoopGroup();

try { Bootstrap b = new Bootstrap(); // (1) b.group(workerGroup); // (2) b.channel(NioSocketChannel.class); // (3) b.option(ChannelOption.SO_KEEPALIVE, true); // (4) b.handler(new ChannelInitializer<SocketChannel>() {

@Override public void initChannel(SocketChannel ch) throws Exception {

ch.pipeline().addLast(new TimeClientHandler()); }

});

// 启动客户端 ChannelFuture f = b.connect(host, port).sync(); // (5)

// 等待连接关闭 f.channel().closeFuture().sync();

} finally {

workerGroup.shutdownGracefully(); }

} }

![image 15](<Java Netty 4.x 用户指南.note_images/imageFile15.png>)

复制代码

- 1.BootStrap 和 类似,不过他是对⾮服务端的 channel ⽽⾔，⽐如客户端或者⽆ 连接传输模式的 channel。


ServerBootstrap

- 2.如果你只指定了⼀个 ，那他就会即作为⼀个 boss group ，也会作为⼀个 workder group，尽管客户端不需要使⽤到 boss worker 。

- 3.代替 的是 ,这个类在客户端channel 被创建时使 ⽤。

- 4.不像在使⽤ ServerBootstrap 时需要⽤ childOption() ⽅法，因为客户端的 没 有⽗亲。

- 5.我们⽤ connect() ⽅法代替了 bind() ⽅法。 正如你看到的，他和服务端的代码是不⼀样的。 是如何实现的?他应该从服务端接受 ⼀个32位的整数消息，把他翻译成⼈们能读懂的格式，并打印翻译好的时间，最后关闭连接:


EventLoopGroup

NioServerSocketChannel NioSocketChannel

SocketChannel

ChannelHandler

![image 16](<Java Netty 4.x 用户指南.note_images/imageFile16.png>)

复制代码

import java.util.Date;

public class TimeClientHandler extends ChannelInboundHandlerAdapter { @Override public void channelRead(ChannelHandlerContext ctx, Object msg) {

ByteBuf m = (ByteBuf) msg; // (1) try {

long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L; System.out.println(new Date(currentTimeMillis)); ctx.close();

} finally {

m.release(); }

}

@Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

cause.printStackTrace(); ctx.close();

} }

![image 17](<Java Netty 4.x 用户指南.note_images/imageFile17.png>)

复制代码

1.在TCP/IP中，Netty 会把读到的数据放到 ByteBuf 的数据结构中。

![image 18](<Java Netty 4.x 用户指南.note_images/imageFile18.png>)

这样看起来⾮常简单，并且和服务端的那个例⼦的代码也相差不多。然⽽，处理器有时候会因为抛出 IndexOutOfBoundsException ⽽拒绝⼯作。在下个部分我们会讨论为什么会发⽣这种情况。

### 处理⼀个基于流的传输

关于 Socket Buffer的⼀个⼩警告

基于流的传输⽐如 TCP/IP, 接收到数据是存在 socket 接收的 buffer 中。不幸的是，基于流的传输 并不是⼀个数据包队列，⽽是⼀个字节队列。意味着，即使你发送了2个独⽴的数据包，操作系统也不 会作为2个消息处理⽽仅仅是作为⼀连串的字节⽽⾔。因此这是不能保证你远程写⼊的数据就会准确地 读取。举个例⼦，让我们假设操作系统的 TCP/TP 协议栈已经接收了3个数据包：

![image 19](<Java Netty 4.x 用户指南.note_images/imageFile19.png>)

由于基于流传输的协议的这种普通的性质，在你的应⽤程序⾥读取数据的时候会有很⾼的可能性被分 成下⾯的⽚段

![image 20](<Java Netty 4.x 用户指南.note_images/imageFile20.png>)

因此，⼀个接收⽅不管他是客户端还是服务端，都应该把接收到的数据整理成⼀个或者多个更有意思 并且能够让程序的业务逻辑更好理解的数据。在上⾯的例⼦中，接收到的数据应该被构造成下⾯的格 式：

![image 21](<Java Netty 4.x 用户指南.note_images/imageFile21.png>)

The First Solution 办法⼀ 回到 TIME 客户端例⼦。同样也有类似的问题。⼀个32位整型是⾮常⼩的数据，他并不⻅得会被经常 拆分到到不同的数据段内。然⽽，问题是他确实可能会被拆分到不同的数据段内，并且拆分的可能性 会随着通信量的增加⽽增加。 最简单的⽅案是构造⼀个内部的可积累的缓冲，直到4个字节全部接收到了内部缓冲。下⾯的代码修改 了 TimeClientHandler 的实现类修复了这个问题

![image 22](<Java Netty 4.x 用户指南.note_images/imageFile22.png>)

复制代码

public class TimeClientHandler extends ChannelInboundHandlerAdapter { private ByteBuf buf;

@Override public void handlerAdded(ChannelHandlerContext ctx) {

buf = ctx.alloc().buffer(4); // (1) }

@Override public void handlerRemoved(ChannelHandlerContext ctx) {

buf.release(); // (1) buf = null;

}

@Override public void channelRead(ChannelHandlerContext ctx, Object msg) {

ByteBuf m = (ByteBuf) msg; buf.writeBytes(m); // (2) m.release();

if (buf.readableBytes() >= 4) { // (3) long currentTimeMillis = (buf.readUnsignedInt() - 2208988800L) * 1000L; System.out.println(new Date(currentTimeMillis)); ctx.close();

} }

@Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

cause.printStackTrace(); ctx.close();

} }

![image 23](<Java Netty 4.x 用户指南.note_images/imageFile23.png>)

复制代码

- 1. 有2个⽣命周期的监听⽅法：handlerAdded()和 handlerRemoved()。你可以 完成任意初始化任务只要他不会被阻塞很⻓的时间。

- 2.⾸先，所有接收的数据都应该被累积在 buf 变量⾥。

- 3.然后，处理器必须检查 buf 变量是否有⾜够的数据，在这个例⼦中是4个字节，然后处理实际的业 务逻辑。否则，Netty 会重复调⽤channelRead() 当有更多数据到达直到4个字节的数据被积累。 The Second Solution ⽅法⼆ 尽管第⼀个解决⽅案已经解决了 TIME 客户端的问题了，但是修改后的处理器看起来不那么的简洁， 想象⼀下如果由多个字段⽐如可变⻓度的字段组成的更为复杂的协议时，你的


ChannelHandler

ChannelInboundHan dler

的实现将很快地变得难以维护。 正如你所知的，你可以增加多个 到 ,因此你可以把⼀整个 ChannelHandler 拆分成多个模块以减少应⽤的复杂程度，⽐如你可以把TimeClientHandler 拆分 成2个处理器：

ChannelHandler ChannelPipeline

TimeDecoder 处理数据拆分的问题 TimeClientHandler 原始版本的实现

幸运地是，Netty 提供了⼀个可扩展的类，帮你完成 TimeDecoder 的开发。

![image 24](<Java Netty 4.x 用户指南.note_images/imageFile24.png>)

复制代码

public class TimeDecoder extends ByteToMessageDecoder { // (1) @Override protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { // (2)

if (in.readableBytes() < 4) {

return; // (3) }

out.add(in.readBytes(4)); // (4) }

}

![image 25](<Java Netty 4.x 用户指南.note_images/imageFile25.png>)

复制代码

- 1. 是 的⼀个实现类，他可以在处理数据拆分 的问题上变得很简单。

- 2.每当有新数据接收的时候，ByteToMessageDecoder 都会调⽤ decode() ⽅法来处理内部的那个 累积缓冲。

- 3.Decode() ⽅法可以决定当累积缓冲⾥没有⾜够数据时可以往 out 对象⾥放任意数据。当有更多的 数据被接收了 ByteToMessageDecoder 会再⼀次调⽤ decode() ⽅法。

- 4.如果在 decode() ⽅法⾥增加了⼀个对象到 out 对象⾥，这意味着解码器解码消息成功。 ByteToMessageDecoder 将会丢弃在累积缓冲⾥已经被读过的数据。请记得你不需要对多条消息调 ⽤ decode()，ByteToMessageDecoder 会持续调⽤ decode() 直到不放任何数据到 out ⾥。 现在我们有另外⼀个处理器插⼊到 ⾥，我们应该在 TimeClient ⾥修改


ByteToMessageDecoder ChannelInboundHandler

ChannelPipeline

ChannelInitializer 的实现：

b.handler(new ChannelInitializer<SocketChannel>() { @Override public void initChannel(SocketChannel ch) throws Exception {

ch.pipeline().addLast(new TimeDecoder(), new TimeClientHandler()); }

});

ReplayingDecoder

如果你是⼀个⼤胆的⼈，你可能会尝试使⽤更简单的解码类 。不过你还是需要参 考⼀下 API ⽂档来获取更多的信息。

![image 26](<Java Netty 4.x 用户指南.note_images/imageFile26.png>)

复制代码

public class TimeDecoder extends ReplayingDecoder<Void> { @Override protected void decode(

ChannelHandlerContext ctx, ByteBuf in, List<Object> out) { out.add(in.readBytes(4));

} }

![image 27](<Java Netty 4.x 用户指南.note_images/imageFile27.png>)

复制代码

此外，Netty还提供了更多开箱即⽤的解码器使你可以更简单地实现更多的协议，帮助你避免开发⼀个 难以维护的处理器实现。请参考下⾯的包以获取更多更详细的例⼦：

对于⼆进制协议请看 io.netty.example.factorial 对于基于⽂本协议请看 io.netty.example.telnet

https://github.com/waylau/netty-4-user-guide-demos

译 者 注 ： 翻 译 版 本 的 项 ⽬ 源 码 ⻅ 中 的 com.waylau.netty.demo.factorial 和 com.waylau.netty.demo.telnet 包 下

## ⽤POJO代替ByteBuf

我们回顾了迄今为⽌的所有例⼦使⽤ 作为协议消息的主要数据结构。在本节中,我们将改善 的 TIME 协议客户端和服务器例⼦，使⽤ POJO 代替 ByteBuf。 在 使⽤ POIO 的好处很明显：通过从ChannelHandler 中提取出 ByteBuf 的代 码，将会使 ChannelHandler的实现变得更加可维护和可重⽤。在 TIME 客户端和服务器的例⼦中， 我们读取的仅仅是⼀个32位的整形数据，直接使⽤ ByteBuf 不会是⼀个主要的问题。然⽽，你会发 现当你需要实现⼀个真实的协议，分离代码变得⾮常的必要。 ⾸先，让我们定义⼀个新的类型叫做 UnixTime。

ByteBuf

ChannelHandler

![image 28](<Java Netty 4.x 用户指南.note_images/imageFile28.png>)

复制代码

public class UnixTime {

private final long value;

public UnixTime() {

this(System.currentTimeMillis() / 1000L + 2208988800L); }

public UnixTime(long value) {

this.value = value; }

public long value() { return value; }

@Override public String toString() {

return new Date((value() - 2208988800L) * 1000L).toString(); }

}

![image 29](<Java Netty 4.x 用户指南.note_images/imageFile29.png>)

复制代码

现在我们可以修改下 TimeDecoder 类，返回⼀个 UnixTime，以替代ByteBuf

![image 30](<Java Netty 4.x 用户指南.note_images/imageFile30.png>)

复制代码

@Override protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

if (in.readableBytes() < 4) {

return; }

out.add(new UnixTime(in.readUnsignedInt())); }

![image 31](<Java Netty 4.x 用户指南.note_images/imageFile31.png>)

复制代码

下⾯是修改后的解码器，TimeClientHandler 不再任何的 ByteBuf 代码了。

@Override public void channelRead(ChannelHandlerContext ctx, Object msg) {

UnixTime m = (UnixTime) msg; System.out.println(m); ctx.close();

}

是不是变得更加简单和优雅了？相同的技术可以被运⽤到服务端。让我们修改⼀下 TimeServerHandler 的代码。

@Override public void channelActive(ChannelHandlerContext ctx) {

ChannelFuture f = ctx.writeAndFlush(new UnixTime()); f.addListener(ChannelFutureListener.CLOSE);

}

现在,唯⼀缺少的功能是⼀个编码器,是 的实现，⽤来将 UnixTime 对象 重新转化为⼀个 ByteBuf。这是⽐编写⼀个解码器简单得多,因为没有需要处理的数据包编码消息时拆 分和组装。

ChannelOutboundHandler

![image 32](<Java Netty 4.x 用户指南.note_images/imageFile32.png>)

复制代码

public class TimeEncoder extends ChannelOutboundHandlerAdapter { @Override public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {

UnixTime m = (UnixTime) msg; ByteBuf encoded = ctx.alloc().buffer(4); encoded.writeInt((int)m.value()); ctx.write(encoded, promise); // (1)

} }

![image 33](<Java Netty 4.x 用户指南.note_images/imageFile33.png>)

复制代码

1.在这⼏⾏代码⾥还有⼏个重要的事情。第⼀，通过 ，当编码后的数据被写到了通 道上 Netty 可以通过这个对象标记是成功还是失败。第⼆， 我们不需要调⽤ cxt.flush()。因为处理 器已经单独分离出了⼀个⽅法 void flush(ChannelHandlerContext cxt),如果像⾃⼰实现 flush() ⽅法内容可以⾃⾏覆盖这个⽅法。 进⼀步简化操作，你可以使⽤ :

ChannelPromise

MessageToByteEncode

public class TimeEncoder extends MessageToByteEncoder<UnixTime> { @Override protected void encode(ChannelHandlerContext ctx, UnixTime msg, ByteBuf out) {

out.writeInt((int)msg.value()); }

}

最后的任务就是在 TimeServerHandler 之前把 TimeEncoder 插⼊到ChannelPipeline。 但这是不 那么重要的⼯作。

### 关闭你的应⽤

关闭⼀个 Netty 应⽤往往只需要简单地通过 shutdownGracefully() ⽅法来关闭你构建的所有的

Ev entLoopGroup channel

。当EventLoopGroup 被完全地终⽌,并且对应的所有 都已经被关闭时， Netty 会返回⼀个 对象来通知你。

Future

总结

在这⼀章节中，我们快速地回顾下如果在熟练掌握 Netty 的情况下编写出⼀个健壮能运⾏的⽹络应⽤ 程序。在 Netty 接下去的章节中还会有更多更相信的信息。我们也⿎励你去重新复习下在

io.netty.e xample 社区

包下的例⼦。请注意 ⼀直在等待你的问题和想法以帮助 Netty 的持续改进，Netty 的⽂ 档也是基于你们的快速反馈上。

https://github.com/waylau/netty-4-user-guide-demos https://github.com/waylau/netty-4-user-guide/issues

译 者 注 ： 翻 译 版 本 的 项 ⽬ 源 码 ⻅ 。 如 对 本 翻 译 有 任何 建 议 ， 可 以 在 留 ⾔

# 架构总览

![image 34](<Java Netty 4.x 用户指南.note_images/imageFile34.png>)

在本章中，我们将研究 Netty 提供的核⼼功能以及他们是如何构成⼀个完整的⽹络应⽤开发堆栈顶部 的核⼼。你阅读本章时，请把这个图记住。

丰富的缓冲实现

Netty 使⽤⾃建的 buffer API，⽽不是使⽤ NIO 的 来表示⼀个连续的字节序列。与 ByteBuffer 相⽐这种⽅式拥有明显的优势。Netty 使⽤新的 buffer 类型 ，被设计为⼀个可 从底层解决 ByteBuffer 问题，并可满⾜⽇常⽹络应⽤开发需要的缓冲类型。这些很酷的特性包括：

ByteBuffer

ByteBuf

如果需要，允许使⽤⾃定义的缓冲类型。 复合缓冲类型中内置的透明的零拷⻉实现。 开箱即⽤的动态缓冲类型，具有像 StringBuffer ⼀样的动态缓冲能⼒。 不再需要调⽤的flip()⽅法。 正常情况下具有⽐ ByteBuffer 更快的响应速度。

io.netty.buffer 包描述

更多信息请参考： Extensibility 可扩展性

ByteBuf 具有丰富的操作集,可以快速的实现协议的优化。例如，ByteBuf 提供各种操作⽤于访问⽆ 符号值和字符串，以及在缓冲区搜索⼀定的字节序列。你也可以扩展或包装现有的缓冲类型⽤来提供 ⽅便的访问。⾃定义缓冲式仍然实现⾃ ByteBuf 接⼝，⽽不是引⼊⼀个不兼容的类型

Transparent Zero Copy 透明的零拷⻉

举⼀个⽹络应⽤到极致的表现，你需要减少内存拷⻉操作次数。你可能有⼀组缓冲区可以被组合以形 成⼀个完整的消息。⽹络提供了⼀种复合缓冲，允许你从现有的任意数的缓冲区创建⼀个新的缓冲区 ⽽⽆需没有内存拷⻉。例如，⼀个信息可以由两部分组成；header 和 body。在⼀个模块化的应⽤， 当消息发送出去时，这两部分可以由不同的模块⽣产和装配。

<pre> +--------+----------+

| header | body | +--------+----------+ </pre>

如果你使⽤的是 ByteBuffer ，你必须要创建⼀个新的⼤缓存区⽤来拷⻉这两部分到这个新缓存区 中。或者，你可以在 NiO做⼀个收集写操作，但限制你将复合缓冲类型作为 ByteBuffer 的数组⽽不 是⼀个单⼀的缓冲区，打破了抽象，并且引⼊了复杂的状态管理。此外，如果你不从 NIO channel 读或写，它是没有⽤的。

// 复合类型与组件类型不兼容。 ByteBuffer[] message = new ByteBuffer[] { header, body };

通过对⽐， ByteBuf 不会有警告，因为它是完全可扩展并有⼀个内置的复合缓冲区。

![image 35](<Java Netty 4.x 用户指南.note_images/imageFile35.png>)

复制代码

// 复合类型与组件类型是兼容的。 ByteBuf message = Unpooled.wrappedBuffer(header, body);

// 因此，你甚⾄可以通过混合复合类型与普通缓冲区来创建⼀个复合类型。 ByteBuf messageWithFooter = Unpooled.wrappedBuffer(message, footer);

// 由于复合类型仍是 ByteBuf，访问其内容很容易， //并且访问⽅法的⾏为就像是访问⼀个单独的缓冲区， //即使你想访问的区域是跨多个组件。 //这⾥的⽆符号整数读取位于 body 和 footer messageWithFooter.getUnsignedInt( messageWithFooter.readableBytes() - footer.readableBytes() - 1);

![image 36](<Java Netty 4.x 用户指南.note_images/imageFile36.png>)

复制代码

#### Automatic Capacity Extension ⾃动容量扩展

许多协议定义可变⻓度的消息，这意味着没有办法确定消息的⻓度，直到你构建的消息。或者，在计 算⻓度的精确值时，带来了困难和不便。这就像当你建⽴⼀个字符串。你经常估计得到的字符串的⻓ 度，让 StringBuffer 扩⼤了其本身的需求。

![image 37](<Java Netty 4.x 用户指南.note_images/imageFile37.png>)

复制代码

// ⼀种新的动态缓冲区被创建。在内部，实际缓冲区是被“懒”创建，从⽽避免潜在的浪费内存空间。 ByteBuf b = Unpooled.buffer(4);

// 当第⼀个执⾏写尝试，内部指定初始容量 4 的缓冲区被创建

- b.writeByte('1');

- b.writeByte('2');

- b.writeByte('3');

- b.writeByte('4');

// 当写⼊的字节数超过初始容量 4 时， //内部缓冲区⾃动分配具有较⼤的容量

- b.writeByte('5');


Better Performance 更好的性能 最频繁使⽤的缓冲区 ByteBuf 的实现是⼀个⾮常薄的字节数组包装器（⽐如，⼀个字节）。与 ByteBuffer 不同，它没有复杂的边界和索引检查补偿，因此对于 JVM 优化缓冲区的访问更加简单。 更多复杂的缓冲区实现是⽤于拆分或者组合缓存，并且⽐ ByteBuffer 拥有更好的性能。

## I/O API 统⼀的异步 I/O API

传统的 Java I/O API 在应对不同的传输协议时需要使⽤不同的类型和⽅法。例如：java.net.Socket 和 java.net.DatagramSocket 它们并不具有相同的超类型，因此，这就需要使⽤不同的调⽤⽅式执 ⾏ socket 操作。 这种模式上的不匹配使得在更换⼀个⽹络应⽤的传输协议时变得繁杂和困难。由于（Java I/O API） 缺乏协议间的移植性，当你试图在不修改⽹络传输层的前提下增加多种协议的⽀持，这时便会产⽣问 题。并且理论上讲，多种应⽤层协议可运⾏在多种传输层协议之上例如TCP/IP,UDP/IP,SCTP和串⼝ 通信。 让这种情况变得更糟的是，Java 新的 I/O（NIO）API与原有的阻塞式的I/O（OIO）API 并不兼 容，NIO.2(AIO)也是如此。由于所有的API⽆论是在其设计上还是性能上的特性都与彼此不同，在进 ⼊开发阶段，你常常会被迫的选择⼀种你需要的API。 例如，在⽤户数较⼩的时候你可能会选择使⽤传统的 OIO(Old I/O) API，毕竟与 NIO 相⽐使⽤

OIO 将更加容易⼀些。然⽽，当你的业务呈指数增⻓并且服务器需要同时处理成千上万的客户连接时 你便会遇到问题。这种情况下你可能会尝试使⽤ NIO，但是复杂的 NIO Selector 编程接⼝⼜会耗费 你⼤量时间并最终会阻碍你的快速开发。

Netty 有⼀个叫做 的统⼀的异步 I/O 编程接⼝，这个编程接⼝抽象了所有点对点的通信操 作。也就是说，如果你的应⽤是基于 Netty 的某⼀种传输实现，那么同样的，你的应⽤也可以运⾏在 Netty 的另⼀种传输实现上。Netty 提供了⼏种拥有相同编程接⼝的基本传输实现：

Channel

基于 NIO 的 TCP/IP 传输 (⻅ io.netty.channel.nio), 基于 OIO 的 TCP/IP 传输 (⻅ io.netty.channel.oio), 基于 OIO 的 UDP/IP 传输, 和 本地传输 (⻅ io.netty.channel.local).

ChannelFactory

切换不同的传输实现通常只需对代码进⾏⼏⾏的修改调整，例如选择⼀个不同的 实 现。 此外，你甚⾄可以利⽤新的传输实现没有写⼊的优势，只需替换⼀些构造器的调⽤⽅法即可，例如串 ⼝通信。⽽且由于核⼼ API 具有⾼度的可扩展性，你还可以完成⾃⼰的传输实现。

### 基于拦截链模式的事件模型

⼀个定义良好并具有扩展能⼒的事件模型是事件驱动开发的必要条件。Netty 具有定义良好的 I/O 事 件模型。由于严格的层次结构区分了不同的事件类型，因此 Netty 也允许你在不破坏现有代码的情况 下实现⾃⼰的事件类型。这是与其他框架相⽐另⼀个不同的地⽅。很多 NIO 框架没有或者仅有有限的 事件模型概念；在你试图添加⼀个新的事件类型的时候常常需要修改已有的代码，或者根本就不允许 你进⾏这种扩展。 在⼀个 内部⼀个 [ChannelEvent]() 被⼀组 处理。这个管道是

ChannelPipeline ChannelHandler Intercepting Filter (拦截过滤器)

模式的⼀种⾼级形式的实现，因此对于⼀个事件如何被处理以及管 道内部处理器间的交互过程，你都将拥有绝对的控制⼒。例如，你可以定义⼀个从 socket 读取到数 据后的操作：

public class MyReadHandler implements SimpleChannelHandler {

public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) { Object message = evt.getMessage(); // Do something with the received message.

...

// And forward the event to the next handler. ctx.sendUpstream(evt);

} }

![image 40](<Java Netty 4.x 用户指南.note_images/imageFile40.png>)

复制代码

同时你也可以定义⼀种操作响应其他处理器的写操作请求：

![image 41](<Java Netty 4.x 用户指南.note_images/imageFile41.png>)

复制代码

public class MyWriteHandler implements SimpleChannelHandler {

public void writeRequested(ChannelHandlerContext ctx, MessageEvent evt) { Object message = evt.getMessage(); // Do something with the message to be written.

...

// And forward the event to the next handler. ctx.sendDownstream(evt);

} }

![image 42](<Java Netty 4.x 用户指南.note_images/imageFile42.png>)

复制代码

有关事件模型的更多信息，请参考 API ⽂档 ChannelEvent 和ChannelPipeline 部分。

### 适⽤快速开发的⾼级组件

上述所提及的核⼼组件已经⾜够实现各种类型的⽹络应⽤，除此之外，Netty 也提供了⼀系列的⾼级 组件来加速你的开发过程。 Codec 框架 就像“ ”⼀节所展示的那样，从业务逻辑代码中分离协议处理部分总是⼀ 个很不错的想法。然⽽如果⼀切从零开始便会遭遇到实现上的复杂性。你不得不处理分段的消息。⼀ 些协议是多层的（例如构建在其他低层协议之上的协议）。⼀些协议过于复杂以致难以在⼀台独⽴状 态机上实现。 因此，⼀个好的⽹络应⽤框架应该提供⼀种可扩展，可重⽤，可单元测试并且是多层的 codec 框架， 为⽤户提供易维护的 codec 代码。 Netty 提供了⼀组构建在其核⼼模块之上的 codec 实现，这些简单的或者⾼级的 codec 实现帮你解 决了⼤部分在你进⾏协议处理开发过程会遇到的问题，⽆论这些协议是简单的还是复杂的，⼆进制的 或是简单⽂本的。 SSL / TLS ⽀持

使⽤POJO代替ChannelBuffer

不同于传统阻塞式的 I/O 实现，在 NIO 模式下⽀持 SSL 功能是⼀个艰难的⼯作。你不能只是简单的 包装⼀下流数据并进⾏加密或解密⼯作，你不得不借助于 javax.net.ssl.SSLEngine，SSLEngine 是⼀个有状态的实现，其复杂性不亚于 SSL ⾃身。你必须管理所有可能的状态，例如密码套件，密钥 协商（或重新协商），证书交换以及认证等。此外，与通常期望情况相反的是 SSLEngine 甚⾄不是 ⼀个绝对的线程安全实现。 在 Netty 内部， 封装了所有艰难的细节以及使⽤ SSLEngine 可 能带来的陷阱。你所做 的仅是配置并将该 SslHandler 插⼊到你的 中。同样 Netty 也允许你实现像

SslHandler

ChannelPipeline Star tTlS

那样所拥有的⾼级特性，这很容易。 HTTP 实现 HTTP⽆ 疑是互联⽹上最受欢迎的协议，并且已经有了⼀些例如 Servlet 容器这样的 HTTP 实现。因 此，为什么 Netty 还要在其核⼼模块之上构建⼀套 HTTP 实现？ 与现有的 HTTP 实现相⽐ Netty 的 HTTP 实现是相当与众不同的。在HTTP 消息的低层交互过程中你 将拥有绝对的控制⼒。这是因为 Netty 的HTTP 实现只是⼀些 HTTP codec 和 HTTP 消息类的简单 组合，这⾥不存在任何限制——例如那种被迫选择的线程模型。你可以随⼼所欲的编写那种可以完全 按照你期望的⼯作⽅式⼯作的客户端或服务器端代码。这包括线程模型，连接⽣命期，快编码，以及 所有 HTTP 协议允许你做的，所有的⼀切，你都将拥有绝对的控制⼒。 由于这种⾼度可定制化的特性，你可以开发⼀个⾮常⾼效的HTTP服务器，例如：

要求持久化链接以及服务器端推送技术的聊天服务（如，Comet ) 需要保持链接直⾄整个⽂件下载完成的媒体流服务（如，2⼩时⻓的电影） 需要上传⼤⽂件并且没有内存压⼒的⽂件服务（如，上传1GB⽂件的请求） ⽀持⼤规模混合客户端应⽤⽤于连接以万计的第三⽅异步 web 服务。

#### WebSockets 实现

允许双向，全双⼯通信信道，在 TCP socket 中。它被设计为允许⼀个 Web 浏览器和 Web 服务器之间通过数据流交互。 WebSocket 协议已经被 IETF 列为 规范。 Netty 实现了 RFC 6455 和⼀些⽼版本的规范。请参阅 包和相关的 。 Google Protocol Buffer 整合

WebSockets

RFC 6455

io.netty.handler.codec.http.websocketx 例⼦

Google Protocol Buffers ProtobufEncod er ProtobufDecoder

是快速实现⼀个⾼效的⼆进制协议的理想⽅案。通过使⽤

和 ，你可以把 Google Protocol Buffers 编译器 (protoc) ⽣成的消息类放⼊ 到 Netty 的codec 实现中。请参考“ ”实例，这个例⼦也同时显示出开发⼀个由简单协议定 义 的客户及服务端是多么的容易。

LocalTime

https://github.com/waylau/netty-4-user-guide-demos

译 者 注 ： 翻 译 版 本 的 项 ⽬ 源 码 ⻅

### 总结

在这⼀章节，我们从功能特性的⻆度回顾了 Netty 的整体架构。Netty 有⼀个简单却不失强⼤的架 构。这个架构由三部分组成——缓冲（buffer），通道（channel），事件模型（event model）

——所有的⾼级特性都构建在这三个核⼼组件之上。⼀旦你理解了它们之间的⼯作原理，你便不难理 解在本章简要提及的更多⾼级特性。 你可能对 Netty 的整体架构以及每⼀部分的⼯作原理仍旧存有疑问。如果是这样，最好的⽅式是

告诉 我们

应该如何改进这份指南。

https://github.com/waylau/netty-4-user-guide/issues

译 者 注 ： 对 本 翻 译 有 任何 疑 问 ， 在 提 问

