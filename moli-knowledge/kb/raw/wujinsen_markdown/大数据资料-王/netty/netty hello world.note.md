# EchoClient

package cn.itcast_04_nety.client; i portio. t.botstrap.Botstrap; i ortio.net.hanel. hanelFuture; i ortio.net.hanel.ChanelInitializer; i portio.net. ne.EventLopGroup; i portio.net. ne.nio.NioEventLopGroup; i otio.net.chanel.ocket.SocketChanel; import io.nety.chanel.socket.nio.NioSocketChanel; import java.net.InetSocketAdres; /*

- * • 连接服务器 • 写数据到服务器 • 等待接受服务器返回相同的数据 • 关闭连接 @author wilson
- */ public clas EchoClient {


riat ina String host; private final int port; public EchoClient(String host, int port) {

thshost = host; this.port = port;

} public void start() throws Exception {

EventLopGroup nioEventLopGroup = nul; try {

/ 创建Botstrap对象⽤来引导启动客户端 Botstrap botstrap = new Botstrap();

/ 创建EventLopGroup对象并设置到Botstrap中，EventLopGroup可以理解为是⼀个线程 池，这个线程池⽤来处理连接、接受数据、发送数据

nioEventLopGroup = new NioEventLopGroup();

/ 创建InetSocketAdres并设置到Botstrap中，InetSocketAdres是指定连接的服务器地 址

botstrap.group(nioEventLopGroup)/

.chanel(NioSocketChanel.clas)/ remoteAdres(new InetSocketAdres(host, port)/

.handler(new ChanelInitializer<SocketChanel>() {/

/ 添加⼀个ChanelHandler，客户端成功连接服务器后就会被执⾏ @Overide protected void initChanel(SocketChanel ch)

throws Exception { ch.pipeline().adLast(

new EchoClientHandler(); }

}); / • 调⽤Botstrap.conect()来连接服务器 ChanelFuture f = botstrap.conect().sync();

/ • 最后关闭EventLopGroup来释放资源 f.chanel().closeFuture().sync();

} finaly {

nioEventLopGroup.shutdownGracefuly().sync(); }

} public static void main(String[] args) throws Exception {

new EchoClient("localhost", 2 0).start(); }

}

EchoClientHandler

<table>
  <tr>
    <th>package cn.itcast_04_nety.client; i ortio.ety. ufer.ByteBuf; i portio.net.bufer.Unpoled; i ortio.net.hanel.ChanelHandlerContext; import io.nety.chanel.SimpleChanelInboundHandler; public clas EchoClientHandler extends SimpleChanelInboundHandler<ByteBuf> {<br><br>/ 客户端连接服务器后被调⽤ @Overide public void chanelActive(ChanelHandlerContext ctx) throws Exception {<br><br>System.out.println("客户端连接服务器，开始发送数据 …"); byte[] req = "QUERY TIME ORDER".getBytes(); ByteBuf firstMesage = Unpoled.bufer(req.length); firstMesage.writeBytes(req); ctx.writeAndFlush(firstMesage);<br><br>}<br><br>/ • 从服务器接收到数据后调⽤ @Overide protected void chanelRead0(ChanelHandlerContext ctx, ByteBuf msg)<br><br>throws Exception { System.out.println("client 读取server数据 .");<br><br>/ 服务端返回消息后 ByteBuf buf = (ByteBuf) msg; byte[] req = new byte[buf.readableBytes()]; buf.readBytes(rq); String body = new String(req, "UTF-8"); System.out.println("服务端数据为 :" + body);<br><br>}<br><br>/ • 发⽣异常时被调⽤ @Overide public void exceptionCaught(ChanelHandlerContext ctx, Throwable cause)<br><br>throws Exception { System.out.println("client exceptionCaught.");<br><br>/ 释放资源 ctx.close(); }<br><br>}</th>
  </tr>
</table>


EchoServer

package cn.itcast_04_nety.server; i portio.et.botstrap.ServerBotstrap; i otio.net.hanel. hanel; i ortio.net.hanel. hanelFuture; i ortio.net.hanel.ChanelInitializer; i portio.net. ne.EventLopGroup; i portio.net. ne.nio.NioEventLopGroup; import io.nety.chanel.socket.nio.NioServerSocketChanel; /*

- * • 配置服务器功能，如线程、端⼝ • 实现服务器处理程序，它包含业务逻辑，决定当有⼀个请求连 接或接收数据时该做什么

@author wilson

- */ public clas EchoServer {


private final int port; public EchoServer(int port) {

this.port = port;

} public void start() throws Exception {

EventLopGroup eventLopGroup = nul; try {

/创建ServerBotstrap实例来引导绑定和启动服务器 ServerBotstrap serverBotstrap = new ServerBotstrap();

/创建NioEventLopGroup对象来处理事件，如接受新连接、接收数据、写数据等等 eventLopGroup = new NioEventLopGroup();

/指定通道类型为NioServerSocketChanel，⼀种异步模式，OIO阻塞模式为 OioServerSocketChanel

/设置InetSocketAdres让服务器监听某个端⼝已等待客户端连接。

serverBotstrap.group(eventLopGroup).chanel(NioServerSocketChanel.clas).localAd dres("localhost",port)

.childHandler(new ChanelInitializer<Chanel>() {

/设置childHandler执⾏所有的连接请求 @Overide protected void initChanel(Chanel ch) throws Exception {

/添加⼀个⼊站的handler到ChanelPipeline

ch.pipeline().adLast(new EchoServerHandler(); }

}); / 最后绑定服务器等待直到绑定完成，调⽤sync()⽅法会阻塞直到服务器完成绑定,然后服务

器等待通道关闭，因为使⽤sync()，所以关闭操作也会被阻塞。 ChanelFuture chanelFuture = serverBotstrap.bind().sync(); System.out.println("开始监听，端⼝为：" + chanelFuture.chanel().localAdres(); chanelFuture.chanel().closeFuture().sync();

} finaly {

eventLopGroup.shutdownGracefuly().sync(); }

}

public static void main(String[] args) throws Exception {

new EchoServer(2 0).start(); }

}

EchoServerHandler

<table>
  <tr>
    <th>package cn.itcast_04_nety.server; i ortio.ety. ufer.ByteBuf; i portio.net.bufer.Unpoled; i ortio.net.hanel.ChanelHandlerContext; import io.nety.chanel.ChanelInboundHandlerAdapter; import java.util.Date; public clas EchoServerHandler extends ChanelInboundHandlerAdapter {<br><br>@Overide public void chanelRead(ChanelHandlerContext ctx, Object msg)<br><br>throws Exception { System.out.println("server 读取数据 …");<br><br>/读取数据 ByteBuf buf = (ByteBuf) msg; byte[] req = new byte[buf.readableBytes()]; buf.readBytes(rq); String body = new String(req, "UTF-8"); System.out.println("接收客户端数据:" + body);<br><br>/向客户端写数据 System.out.println("server向client发送数据"); String curentTime = new Date(System.curenti eMilis().toString(); ByteBuf resp = Unpoled.copiedBufer(curentTime.getBytes(); ctx.write(resp);<br><br>} @Overide public void chanelReadComplete(ChanelHandlerContext ctx) throws Exception {<br><br>System.out.println("server 读取数据完毕 ."); ctx.flush();/刷新后才将数据发出到SocketChanel<br><br>} @Overide public void exceptionCaught(ChanelHandlerContext ctx, Throwable cause)<br><br>throws Exception { cause.printStackTrace(); ctx.close();<br><br>} }</th>
  </tr>
</table>


