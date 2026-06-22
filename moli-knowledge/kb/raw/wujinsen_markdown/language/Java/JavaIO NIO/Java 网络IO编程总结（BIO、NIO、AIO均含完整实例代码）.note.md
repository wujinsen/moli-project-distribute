http://blog.csdn.net/anxpp/article/details/51512200

转载请注明出处： ，谢谢！ 本⽂会从传统的BIO到NIO再到AIO⾃浅⾄深介绍，并附上完整的代码讲解。 下⾯代码中会使⽤这样⼀个例⼦：客户端发送⼀段算式的字符串到服务器，服务器计算后返回结果

到客户端。

代码的所有说明，都直接作为注释，嵌⼊到代码中，看代码时就能更容易理解，代码中会⽤到⼀个 计算结果的⼯具类，⻅⽂章代码部分。

相关的基础知识⽂章推荐：

Linux ⽹络 I/O 模型简介（图⽂） Java 并发（多线程）

1、BIO编程

## 1.1、传统的BIO编程

⽹络编程的基本模型是C/S模型，即两个进程间的通信。 服务端提供IP和监听端⼝，客户端通过连接操作想服务端监听的地址发起连接请求，通过三次握⼿

连接，如果连接成功建⽴，双⽅就可以通过套接字进⾏通信。

传统的同步阻塞模型开发中，ServerSocket负责绑定IP地址，启动监听端⼝；Socket负责发起连接操 作。连接成功后，双⽅通过输⼊和输出流进⾏同步阻塞式通信。

简单的描述⼀下BIO的服务端通信模型：采⽤BIO通信模型的服务端，通常由⼀个独⽴的Acceptor线 程负责监听客户端的连接，它接收到客户端连接请求之后为每个客户端创建⼀个新的线程进⾏链路处 理没处理完成后，通过输出流返回应答给客户端，线程销毁。即典型的⼀请求⼀应答通宵模型。

传统BIO通信模型图：

![image 1](<Java 网络IO编程总结（BIO、NIO、AIO均含完整实例代码）.note_images/imageFile1.png>)

- 01


该模型最⼤的问题就是缺乏弹性伸缩能⼒，当客户端并发访问量增加后，服务端的线程个数和客户 端并发访问数呈1:1的正⽐关系，Java中的线程也是⽐较宝贵的系统资源，线程数量快速膨胀后，系统 的性能将急剧下降，随着访问量的继续增⼤，系统最终就死-掉-了。

同步阻塞式I/O创建的Server源码：

[java]

view plain copy

- 1.
- 2.
- 3.
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
- 22.
- 23.
- 24.


package com.anxpp.io.calculator.bio; import java.io.IOException; import java.net.ServerSocket; import java.net.Socket; /**

- * BIO服务端源码

- * @author yangtao__anxpp.com

- * @version 1.0

- */


public final class ServerNormal { //默认的端⼝号 private static int DEFAULT_PORT = 12345; //单例的ServerSocket private static ServerSocket server; //根据传⼊参数设置监听端⼝，如果没有参数调⽤以下⽅法并使⽤默认值 public static void start() throws IOException{

//使⽤默认值 start(DEFAULT_PORT);

} //这个⽅法不会被⼤量并发访问，不太需要考虑效率，直接进⾏⽅法同步就⾏了 public synchronized static void start(int port) throws IOException{

### if(server != null) return; try{

//通过构造函数创建ServerSocket

- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.


//如果端⼝合法且空闲，服务端就监听成功 server = new ServerSocket(port); System.out.println("服务器已启动，端⼝号：" + port);

//通过⽆线循环监听客户端连接 //如果没有客户端接⼊，将阻塞在accept操作上。 while(true){

Socket socket = server.accept(); //当有新的客户端接⼊时，会执⾏下⾯的代码 //然后创建⼀个新的线程处理这条Socket链路 new Thread(new ServerHandler(socket)).start();

} }finally{ //⼀些必要的清理⼯作 if(server != null){

System.out.println("服务器已关闭。"); server.close(); server = null;

} }

} }

客户端消息处理线程ServerHandler源码：

[java]

view plain copy

- 1.
- 2.
- 3.
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
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.


package com.anxpp.io.calculator.bio; import java.io.BufferedReader; import java.io.IOException; import java.io.InputStreamReader; import java.io.PrintWriter; import java.net.Socket;

import com.anxpp.io.utils.Calculator; /**

- * 客户端线程

- * @author yangtao__anxpp.com

- * ⽤于处理⼀个客户端的Socket链路

- */


public class ServerHandler implements Runnable{ private Socket socket; public ServerHandler(Socket socket) {

this.socket = socket;

} @Override public void run() {

BufferedReader in = null; PrintWriter out = null; try{

in = new BufferedReader(new InputStreamReader(socket.getInputStream())); out = new PrintWriter(socket.getOutputStream(),true); String expression; String result; while(true){

//通过BufferedReader读取⼀⾏ //如果已经读到输⼊流尾部，返回null,退出循环 //如果得到⾮空值，就尝试计算结果并返回

- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.


if((expression = in.readLine())==null) break; System.out.println("服务器收到消息：" + expression); try{

result = Calculator.cal(expression).toString(); }catch(Exception e){

result = "计算错误：" + e.getMessage();

} out.println(result);

} }catch(Exception e){ e.printStackTrace();

}finally{ //⼀些必要的清理⼯作 if(in != null){

### try {

in.close(); } catch (IOException e) { e.printStackTrace();

} in = null;

} if(out != null){

out.close(); out = null;

} if(socket != null){

### try {

socket.close(); } catch (IOException e) { e.printStackTrace();

} socket = null;

} }

} }

同步阻塞式I/O创建的Client源码：

[java]

view plain copy

- 1.
- 2.
- 3.
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


package com.anxpp.io.calculator.bio; import java.io.BufferedReader; import java.io.IOException; import java.io.InputStreamReader; import java.io.PrintWriter; import java.net.Socket; /**

- * 阻塞式I/O创建的客户端

- * @author yangtao__anxpp.com

- * @version 1.0

- */


public class Client { //默认的端⼝号 private static int DEFAULT_SERVER_PORT = 12345; private static String DEFAULT_SERVER_IP = "127.0.0.1"; public static void send(String expression){

- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.


send(DEFAULT_SERVER_PORT,expression);

} public static void send(int port,String expression){

System.out.println("算术表达式为：" + expression); Socket socket = null; BufferedReader in = null; PrintWriter out = null; try{

socket = new Socket(DEFAULT_SERVER_IP,port); in = new BufferedReader(new InputStreamReader(socket.getInputStream())); out = new PrintWriter(socket.getOutputStream(),true); out.println(expression); System.out.println("___结果为：" + in.readLine());

}catch(Exception e){ e.printStackTrace();

}finally{ //⼀下必要的清理⼯作 if(in != null){

### try {

in.close(); } catch (IOException e) { e.printStackTrace();

} in = null;

} if(out != null){

out.close(); out = null;

} if(socket != null){

### try {

socket.close(); } catch (IOException e) { e.printStackTrace();

} socket = null;

} }

} }

测试代码，为了⽅便在控制台看输出结果，放到同⼀个程序（jvm）中运⾏：

[java]

view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.


package com.anxpp.io.calculator.bio; import java.io.IOException; import java.util.Random; /**

- * 测试⽅法

- * @author yangtao__anxpp.com

- * @version 1.0

- */


public class Test { //测试主⽅法 public static void main(String[] args) throws InterruptedException {

//运⾏服务器

- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.


new Thread(new Runnable() { @Override public void run() {

### try {

ServerBetter.start(); } catch (IOException e) {

e.printStackTrace(); }

} }).start(); //避免客户端先于服务器启动前执⾏代码 Thread.sleep(100); //运⾏客户端 char operators[] = {'+','-','*','/'}; Random random = new Random(System.currentTimeMillis()); new Thread(new Runnable() {

@SuppressWarnings("static-access") @Override public void run() {

while(true){ //随机产⽣算术表达式 String expression = random.nextInt(10)+""+operators[random.nextInt(4)]+

(random.nextInt(10)+1); Client.send(expression); try {

Thread.currentThread().sleep(random.nextInt(1000)); } catch (InterruptedException e) {

e.printStackTrace(); }

} }

}).start(); }

}

其中⼀次的运⾏结果：

- 1.
- 2.
- 3.
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


服务器已启动，端⼝号：12345

- 算术表达式为：4-2

- 服务器收到消息：4-2 _结果为：2

算术表达式为：5-10

- 服务器收到消息：5-10 _结果为：-5




算术表达式为：0-9 服务器收到消息：0-9

_结果为：-9 算术表达式为：0+6 服务器收到消息：0+6

_结果为：6

- 14.
- 15.
- 16.
- 17.


算术表达式为：1/6 服务器收到消息：1/6

_结果为：0.1 6

.

从以上代码，很容易看出，BIO主要的问题在于每当有⼀个新的客户端请求接⼊时，服务端必须创 建⼀个新的线程来处理这条链路，在需要满⾜⾼性能、⾼并发的场景是没法应⽤的（⼤量创建新的线 程会严重影响服务器性能，甚⾄罢⼯）。

## 1.2、伪异步I/O编程

为了改进这种⼀连接⼀线程的模型，我们可以使⽤线程池来管理这些线程（需要了解更多请参考前 ⾯提供的⽂章），实现1个或多个线程处理N个客户端的模型（但是底层还是使⽤的同步阻塞I/O），通 常被称为“伪异步I/O模型“。

伪异步I/O模型图：

![image 2](<Java 网络IO编程总结（BIO、NIO、AIO均含完整实例代码）.note_images/imageFile2.png>)

- 02


实现很简单，我们只需要将新建线程的地⽅，交给线程池管理即可，只需要改动刚刚的Server代码 即可：

[java]

view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


package com.anxpp.io.calculator.bio; import java.io.IOException; import java.net.ServerSocket; import java.net.Socket; import java.util.concurrent.ExecutorService; import java.util.concurrent.Executors;

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
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.


/**

- * BIO服务端源码__伪异步I/O

- * @author yangtao__anxpp.com

- * @version 1.0

- */


public final class ServerBetter { //默认的端⼝号 private static int DEFAULT_PORT = 12345; //单例的ServerSocket private static ServerSocket server; //线程池 懒汉式的单例 private static ExecutorService executorService = Executors.newFixedThreadPool(60); //根据传⼊参数设置监听端⼝，如果没有参数调⽤以下⽅法并使⽤默认值 public static void start() throws IOException{

//使⽤默认值 start(DEFAULT_PORT);

} //这个⽅法不会被⼤量并发访问，不太需要考虑效率，直接进⾏⽅法同步就⾏了 public synchronized static void start(int port) throws IOException{

### if(server != null) return; try{

//通过构造函数创建ServerSocket //如果端⼝合法且空闲，服务端就监听成功 server = new ServerSocket(port); System.out.println("服务器已启动，端⼝号：" + port); //通过⽆线循环监听客户端连接 //如果没有客户端接⼊，将阻塞在accept操作上。 while(true){

Socket socket = server.accept(); //当有新的客户端接⼊时，会执⾏下⾯的代码 //然后创建⼀个新的线程处理这条Socket链路 executorService.execute(new ServerHandler(socket));

} }finally{ //⼀些必要的清理⼯作 if(server != null){

System.out.println("服务器已关闭。"); server.close(); server = null;

} }

} }

测试运⾏结果是⼀样的。 我们知道，如果使⽤CachedThreadPool线程池（不限制线程数量，如果不清楚请参考⽂⾸提供的⽂ 章），其实除了能⾃动帮我们管理线程（复⽤），看起来也就像是1:1的客户端：线程数模型，⽽使⽤ FixedThreadPool我们就有效的控制了线程的最⼤数量，保证了系统有限的资源的控制，实现了N:M的 伪异步I/O模型。

但是，正因为限制了线程数量，如果发⽣⼤量并发请求，超过最⼤数量的线程就只能等待，直到线 程池中的有空闲的线程可以被复⽤。⽽对Socket的输⼊流就⾏读取时，会⼀直阻塞，直到发⽣：

有数据可读 可⽤数据以及读取完毕 发⽣空指针或I/O异常

所以在读取数据较慢时（⽐如数据量⼤、⽹络传输慢等），⼤量并发的情况下，其他接⼊的消息， 只能⼀直等待，这就是最⼤的弊端。

⽽后⾯即将介绍的NIO，就能解决这个难题。

2、NIO 编程

JDK 1.4中的java.nio.*包中引⼊新的Java I/O库，其⽬的是提⾼速度。实际上，“旧”的I/O包已经使 ⽤NIO重新实现过，即使我们不显式的使⽤NIO编程，也能从中受益。速度的提⾼在⽂件I/O和⽹络I/O 中都可能会发⽣，但本⽂只讨论后者。

## 2.1、简介

NIO我们⼀般认为是New I/O（也是官⽅的叫法），因为它是相对于⽼的I/O类库新增的（其实在JDK 1.4中就已经被引⼊了，但这个名词还会继续⽤很久，即使它们在现在看来已经是“旧”的了，所以也提 示我们在命名时，需要好好考虑），做了很⼤的改变。但⺠间跟多⼈称之为Non-block I/O，即⾮阻塞 I/O，因为这样叫，更能体现它的特点。⽽下⽂中的NIO，不是指整个新的I/O库，⽽是⾮阻塞I/O。

NIO 提 供 了 与 传 统 BIO 模 型 中 的 Socket 和 ServerSocket 相 对 应 的 SocketChannel 和

ServerSocketChannel两种不同的套接字通道实现。 新增的着两种通道都⽀持阻塞和⾮阻塞两种模式。 阻塞模式使⽤就像传统中的⽀持⼀样，⽐较简单，但是性能和可靠性都不好；⾮阻塞模式正好与之

相反。

对于低负载、低并发的应⽤程序，可以使⽤同步阻塞I/O来提升开发速率和更好的维护性；对于⾼负 载、⾼并发的（⽹络）应⽤，应使⽤NIO的⾮阻塞模式来开发。

下⾯会先对基础知识进⾏介绍。

# 2.2、缓冲区 Buffer

Buffer是⼀个对象，包含⼀些要写⼊或者读出的数据。 在NIO库中，所有数据都是⽤缓冲区处理的。在读取数据时，它是直接读到缓冲区中的；在写⼊数

据时，也是写⼊到缓冲区中。任何时候访问NIO中的数据，都是通过缓冲区进⾏操作。 缓冲区实际上是⼀个数组，并提供了对数据结构化访问以及维护读写位置等信息。

具 体 的 缓 存 区 有 这 些 ： ByteBuffe 、 CharBuffer 、 ShortBuffer 、 IntBuffer 、 LongBuffer 、 FloatBuffer、DoubleBuffer。他们实现了相同的接⼝：Buffer。

# 2.3、通道 Channel

我们对数据的读取和写⼊要通过Channel，它就像⽔管⼀样，是⼀个通道。通道不同于流的地⽅就是 通道是双向的，可以⽤于读、写和同时读写操作。

底层的操作系统的通道⼀般都是全双⼯的，所以全双⼯的Channel⽐流能更好的映射底层操作系统的 API。

Channel主要分两⼤类：

SelectableChannel：⽤户⽹络读写 FileChannel：⽤于⽂件操作

后⾯代码会涉及的ServerSocketChannel和SocketChannel都是SelectableChannel的⼦类。

## 2.4、多路复⽤器 Selector

Selector是Java NIO 编程的基础。 Selector提供选择已经就绪的任务的能⼒：Selector会不断轮询注册在其上的Channel，如果某个

Channel上⾯发⽣读或者写事件，这个Channel就处于就绪状态，会被Selector轮询出来，然后通过 SelectionKey可以获取就绪Channel的集合，进⾏后续的I/O操作。

⼀个Selector可以同时轮询多个Channel，因为JDK使⽤了epoll()代替传统的select实现，所以没有最 ⼤连接句柄1024/2048的限制。所以，只需要⼀个线程负责Selector的轮询，就可以接⼊成千上万的客 户端。

## 2.5、NIO服务端

代码⽐传统的Socket编程看起来要复杂不少。 直接贴代码吧，以注释的形式给出代码说明。 NIO创建的Server源码：

[java]

view plain copy

- 1.
- 2.
- 3.
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


package com.anxpp.io.calculator.nio; public class Server {

private static int DEFAULT_PORT = 12345; private static ServerHandle serverHandle; public static void start(){

start(DEFAULT_PORT);

### } public static synchronized void start(int port){

if(serverHandle!=null)

serverHandle.stop(); serverHandle = new ServerHandle(port); new Thread(serverHandle,"Server").start();

} public static void main(String[] args){

start(); }

}

ServerHandle：

[java]

view plain copy

- 1.
- 2.
- 3.
- 4.


package com.anxpp.io.calculator.nio; import java.io.IOException; import java.net.InetSocketAddress; import java.nio.ByteBuffer;

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
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.


import java.nio.channels.SelectionKey; import java.nio.channels.Selector; import java.nio.channels.ServerSocketChannel; import java.nio.channels.SocketChannel; import java.util.Iterator; import java.util.Set;

import com.anxpp.io.utils.Calculator; /**

- * NIO服务端

- * @author yangtao__anxpp.com

- * @version 1.0

- */


public class ServerHandle implements Runnable{ private Selector selector; private ServerSocketChannel serverChannel; private volatile boolean started; /**

- * 构造⽅法

- * @param port 指定要监听的端⼝号

- */


public ServerHandle(int port) { try{

//创建选择器 selector = Selector.open(); //打开监听通道 serverChannel = ServerSocketChannel.open(); //如果为 true，则此通道将被置于阻塞模式；如果为 false，则此通道将被置于⾮阻塞模式 serverChannel.configureBlocking(false);//开启⾮阻塞模式 //绑定端⼝ backlog设为1024 serverChannel.socket().bind(new InetSocketAddress(port),1024); //监听客户端连接请求 serverChannel.register(selector, SelectionKey.OP_ACCEPT);

//标记服务器已开启 started = true; System.out.println("服务器已启动，端⼝号：" + port);

}catch(IOException e){ e.printStackTrace(); System.exit(1);

}

### } public void stop(){

started = false;

} @Override public void run() {

//循环遍历selector while(started){

### try{

//⽆论是否有读写事件发⽣，selector每隔1s被唤醒⼀次 selector.select(1000); //阻塞,只有当⾄少⼀个注册的事件发⽣的时候才会继续.

// selector.select(); Set<SelectionKey> keys = selector.selectedKeys(); Iterator<SelectionKey> it = keys.iterator();

- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.
- 92.
- 93.
- 94.
- 95.
- 96.
- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.
- 105.
- 106.
- 107.
- 108.
- 109.
- 110.
- 111.
- 112.
- 113.
- 114.


SelectionKey key = null; while(it.hasNext()){

key = it.next(); it.remove(); try{

handleInput(key); }catch(Exception e){

if(key != null){ key.cancel(); if(key.channel() != null){

key.channel().close(); }

} }

} }catch(Throwable t){

t.printStackTrace(); }

} //selector关闭后会⾃动释放⾥⾯管理的资源 if(selector != null)

### try{

selector.close(); }catch (Exception e) {

e.printStackTrace(); }

} private void handleInput(SelectionKey key) throws IOException{

if(key.isValid()){ //处理新接⼊的请求消息 if(key.isAcceptable()){

ServerSocketChannel ssc = (ServerSocketChannel) key.channel(); //通过ServerSocketChannel的accept创建SocketChannel实例 //完成该操作意味着完成TCP三次握⼿，TCP物理链路正式建⽴ SocketChannel sc = ssc.accept(); //设置为⾮阻塞的 sc.configureBlocking(false); //注册为读 sc.register(selector, SelectionKey.OP_READ);

}

//读消息 if(key.isReadable()){

SocketChannel sc = (SocketChannel) key.channel(); //创建ByteBuffer，并开辟⼀个1M的缓冲区 ByteBuffer buffer = ByteBuffer.allocate(1024); //读取请求码流，返回读取到的字节数 int readBytes = sc.read(buffer); //读取到字节，对字节进⾏编解码 if(readBytes>0){

//将缓冲区当前的limit设置为position=0，⽤于后续对缓冲区的读取操作 buffer.flip(); //根据缓冲区可读字节数创建字节数组 byte[] bytes = new byte[buffer.remaining()]; //将缓冲区可读字节数组复制到新建的数组中 buffer.get(bytes);

- 115.
- 116.
- 117.
- 118.
- 119.
- 120.
- 121.
- 122.
- 123.
- 124.
- 125.
- 126.
- 127.
- 128.
- 129.
- 130.
- 131.
- 132.
- 133.
- 134.
- 135.
- 136.
- 137.
- 138.
- 139.
- 140.
- 141.
- 142.
- 143.
- 144.
- 145.
- 146.
- 147.
- 148.
- 149.
- 150.
- 151.


String expression = new String(bytes,"UTF-8"); System.out.println("服务器收到消息：" + expression); //处理数据 String result = null; try{

result = Calculator.cal(expression).toString(); }catch(Exception e){

result = "计算错误：" + e.getMessage();

} //发送应答消息 doWrite(sc,result);

} //没有读取到字节 忽略

// else if(readBytes==0); //链路已经关闭，释放资源 else if(readBytes<0){ key.cancel(); sc.close();

} }

}

} //异步发送应答消息 private void doWrite(SocketChannel channel,String response) throws IOException{

//将消息编码为字节数组 byte[] bytes = response.getBytes(); //根据数组容量创建ByteBuffer ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length); //将字节数组复制到缓冲区 writeBuffer.put(bytes); //flip操作 writeBuffer.flip(); //发送缓冲区的字节数组 channel.write(writeBuffer); //****此处不含处理“写半包”的代码

} }

可以看到，创建NIO服务端的主要步骤如下：

打开ServerSocketChannel，监听客户端连接 绑定监听端⼝，设置连接为⾮阻塞模式 创建Reactor线程，创建多路复⽤器并启动线程 将ServerSocketChannel注册到Reactor线程中的Selector上，监听ACCEPT事件 Selector轮询准备就绪的key Selector监听到新的客户端接⼊，处理新的接⼊请求，完成TCP三次握⼿，简历物理链路 设置客户端链路为⾮阻塞模式 将新接⼊的客户端连接注册到Reactor线程的Selector上，监听读操作，读取客户端发送的

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


⽹络消息 异步读取客户端消息到缓冲区 对Buffer编解码，处理半包消息，将解码成功的消息封装成Task 将应答消息编码为Buffer，调⽤SocketChannel的write将消息异步发送给客户端

因为应答消息的发送，SocketChannel也是异步⾮阻塞的，所以不能保证⼀次能吧需要发送的数据发 送完，此时就会出现写半包的问题。我们需要注册写操作，不断轮询Selector将没有发送完的消息发送 完毕，然后通过Buffer的hasRemain()⽅法判断消息是否发送完成。

## 2.6、NIO客户端

还是直接上代码吧，过程也不需要太多解释了，跟服务端代码有点类似。 Client：

[java]

view plain copy

- 1.
- 2.
- 3.
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
- 22.
- 23.
- 24.


package com.anxpp.io.calculator.nio; public class Client {

private static String DEFAULT_HOST = "127.0.0.1"; private static int DEFAULT_PORT = 12345; private static ClientHandle clientHandle; public static void start(){

start(DEFAULT_HOST,DEFAULT_PORT);

### } public static synchronized void start(String ip,int port){

if(clientHandle!=null)

clientHandle.stop(); clientHandle = new ClientHandle(ip,port); new Thread(clientHandle,"Server").start();

} //向服务器发送消息 public static boolean sendMsg(String msg) throws Exception{

if(msg.equals("q")) return false; clientHandle.sendMsg(msg); return true;

} public static void main(String[] args){

start(); }

}

ClientHandle：

[java]

view plain copy

- 1.
- 2.
- 3.
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


package com.anxpp.io.calculator.nio; import java.io.IOException; import java.net.InetSocketAddress; import java.nio.ByteBuffer; import java.nio.channels.SelectionKey; import java.nio.channels.Selector; import java.nio.channels.SocketChannel; import java.util.Iterator; import java.util.Set; /**

- * NIO客户端

- * @author yangtao__anxpp.com

- * @version 1.0

- */


public class ClientHandle implements Runnable{ private String host; private int port;

- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.


private Selector selector; private SocketChannel socketChannel; private volatile boolean started;

public ClientHandle(String ip,int port) { this.host = ip; this.port = port; try{

//创建选择器 selector = Selector.open(); //打开监听通道 socketChannel = SocketChannel.open(); //如果为 true，则此通道将被置于阻塞模式；如果为 false，则此通道将被置于⾮阻塞模式 socketChannel.configureBlocking(false);//开启⾮阻塞模式 started = true;

}catch(IOException e){ e.printStackTrace(); System.exit(1);

}

### } public void stop(){

started = false;

} @Override public void run() {

### try{

doConnect();

}catch(IOException e){ e.printStackTrace(); System.exit(1);

} //循环遍历selector while(started){

### try{

//⽆论是否有读写事件发⽣，selector每隔1s被唤醒⼀次 selector.select(1000); //阻塞,只有当⾄少⼀个注册的事件发⽣的时候才会继续.

// selector.select(); Set<SelectionKey> keys = selector.selectedKeys(); Iterator<SelectionKey> it = keys.iterator(); SelectionKey key = null; while(it.hasNext()){

key = it.next(); it.remove(); try{

handleInput(key); }catch(Exception e){

if(key != null){ key.cancel(); if(key.channel() != null){

key.channel().close(); }

} }

}

- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.
- 92.
- 93.
- 94.
- 95.
- 96.
- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.
- 105.
- 106.
- 107.
- 108.
- 109.
- 110.
- 111.
- 112.
- 113.
- 114.
- 115.
- 116.
- 117.
- 118.
- 119.
- 120.
- 121.
- 122.
- 123.
- 124.
- 125.
- 126.
- 127.


}catch(Exception e){ e.printStackTrace(); System.exit(1);

}

} //selector关闭后会⾃动释放⾥⾯管理的资源 if(selector != null)

### try{

selector.close(); }catch (Exception e) {

e.printStackTrace(); }

} private void handleInput(SelectionKey key) throws IOException{

if(key.isValid()){ SocketChannel sc = (SocketChannel) key.channel(); if(key.isConnectable()){

if(sc.finishConnect()); else System.exit(1);

} //读消息 if(key.isReadable()){

//创建ByteBuffer，并开辟⼀个1M的缓冲区 ByteBuffer buffer = ByteBuffer.allocate(1024); //读取请求码流，返回读取到的字节数 int readBytes = sc.read(buffer); //读取到字节，对字节进⾏编解码

if(readBytes>0){ //将缓冲区当前的limit设置为position=0，⽤于后续对缓冲区的读取操作 buffer.flip(); //根据缓冲区可读字节数创建字节数组 byte[] bytes = new byte[buffer.remaining()]; //将缓冲区可读字节数组复制到新建的数组中 buffer.get(bytes); String result = new String(bytes,"UTF-8"); System.out.println("客户端收到消息：" + result);

} //没有读取到字节 忽略

// else if(readBytes==0); //链路已经关闭，释放资源 else if(readBytes<0){ key.cancel(); sc.close();

} }

}

} //异步发送消息 private void doWrite(SocketChannel channel,String request) throws IOException{

//将消息编码为字节数组 byte[] bytes = request.getBytes(); //根据数组容量创建ByteBuffer ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length); //将字节数组复制到缓冲区 writeBuffer.put(bytes);

- 128.
- 129.
- 130.
- 131.
- 132.
- 133.
- 134.
- 135.
- 136.
- 137.
- 138.
- 139.
- 140.
- 141.
- 142.


//flip操作 writeBuffer.flip(); //发送缓冲区的字节数组 channel.write(writeBuffer); //****此处不含处理“写半包”的代码

} private void doConnect() throws IOException{

if(socketChannel.connect(new InetSocketAddress(host,port))); else socketChannel.register(selector, SelectionKey.OP_CONNECT);

} public void sendMsg(String msg) throws Exception{

socketChannel.register(selector, SelectionKey.OP_READ); doWrite(socketChannel, msg);

} }

## 2.7、演示结果

⾸先运⾏服务器，顺便也运⾏⼀个客户端：

[java]

view plain copy

- 1.
- 2.
- 3.
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


package com.anxpp.io.calculator.nio; import java.util.Scanner; /**

- * 测试⽅法

- * @author yangtao__anxpp.com

- * @version 1.0

- */


public class Test { //测试主⽅法 @SuppressWarnings("resource") public static void main(String[] args) throws Exception{

//运⾏服务器 Server.start(); //避免客户端先于服务器启动前执⾏代码 Thread.sleep(100); //运⾏客户端 Client.start(); while(Client.sendMsg(new Scanner(System.in).nextLine()));

} }

我们也可以单独运⾏客户端，效果都是⼀样的。 ⼀次测试的结果：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


服务器已启动，端⼝号：12345 1+2+3+4+5+6 服务器收到消息：1+2+3+4+5+6 客户端收到消息：21 1*2/3-4+5*6/7-8 服务器收到消息：1*2/3-4+5*6/7-8 客户端收到消息：-7.0476190476190474

运⾏多个客户端，都是没有问题的。

3、AIO编程

NIO 2.0引⼊了新的异步通道的概念，并提供了异步⽂件通道和异步套接字通道的实现。 异步的套接字通道时真正的异步⾮阻塞I/O，对应于UNIX⽹络编程中的事件驱动I/O（AIO）。他不

需要过多的Selector对注册的通道进⾏轮询即可实现异步读写，从⽽简化了NIO的编程模型。 直接上代码吧。

# 3.1、Server端代码

Server：

[java]

view plain copy

- 1.
- 2.
- 3.
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
- 22.
- 23.


package com.anxpp.io.calculator.aio.server; /**

- * AIO服务端

- * @author yangtao__anxpp.com

- * @version 1.0

- */


public class Server { private static int DEFAULT_PORT = 12345; private static AsyncServerHandler serverHandle; public volatile static long clientCount = 0; public static void start(){

start(DEFAULT_PORT);

### } public static synchronized void start(int port){

if(serverHandle!=null)

return; serverHandle = new AsyncServerHandler(port); new Thread(serverHandle,"Server").start();

} public static void main(String[] args){

Server.start(); }

}

AsyncServerHandler：

[java]

view plain copy

- 1.
- 2.
- 3.
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


package com.anxpp.io.calculator.aio.server; import java.io.IOException; import java.net.InetSocketAddress; import java.nio.channels.AsynchronousServerSocketChannel; import java.util.concurrent.CountDownLatch; public class AsyncServerHandler implements Runnable {

public CountDownLatch latch; public AsynchronousServerSocketChannel channel; public AsyncServerHandler(int port) {

try { //创建服务端通道 channel = AsynchronousServerSocketChannel.open(); //绑定端⼝ channel.bind(new InetSocketAddress(port)); System.out.println("服务器已启动，端⼝号：" + port);

} catch (IOException e) { e.printStackTrace();

- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.


}

} @Override public void run() {

//CountDownLatch初始化 //它的作⽤：在完成⼀组正在执⾏的操作之前，允许当前的现场⼀直阻塞 //此处，让现场在此阻塞，防⽌服务端执⾏完成后退出 //也可以使⽤while(true)+sleep //⽣成环境就不需要担⼼这个问题，以为服务端是不会退出的 latch = new CountDownLatch(1); //⽤于接收客户端的连接 channel.accept(this,new AcceptHandler()); try {

latch.await(); } catch (InterruptedException e) {

e.printStackTrace(); }

} }

AcceptHandler：

[java]

view plain copy

- 1.
- 2.
- 3.
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
- 22.
- 23.


package com.anxpp.io.calculator.aio.server; import java.nio.ByteBuffer; import java.nio.channels.AsynchronousSocketChannel; import java.nio.channels.CompletionHandler; //作为handler接收客户端连接 public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, AsyncServerHa ndler> {

@Override public void completed(AsynchronousSocketChannel channel,AsyncServerHandler serverHandler) {

//继续接受其他客户端的请求 Server.clientCount++; System.out.println("连接的客户端数：" + Server.clientCount); serverHandler.channel.accept(serverHandler, this); //创建新的Buffer ByteBuffer buffer = ByteBuffer.allocate(1024); //异步读 第三个参数为接收消息回调的业务Handler channel.read(buffer, buffer, new ReadHandler(channel));

} @Override public void failed(Throwable exc, AsyncServerHandler serverHandler) {

exc.printStackTrace(); serverHandler.latch.countDown();

} }

ReadHandler：

[java]

view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


package com.anxpp.io.calculator.aio.server; import java.io.IOException; import java.io.UnsupportedEncodingException; import java.nio.ByteBuffer; import java.nio.channels.AsynchronousSocketChannel; import java.nio.channels.CompletionHandler;

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
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.


import com.anxpp.io.utils.Calculator; public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

//⽤于读取半包消息和发送应答 private AsynchronousSocketChannel channel; public ReadHandler(AsynchronousSocketChannel channel) {

this.channel = channel;

} //读取到消息后的处理 @Override public void completed(Integer result, ByteBuffer attachment) {

//flip操作 attachment.flip(); //根据 byte[] message = new byte[attachment.remaining()]; attachment.get(message); try {

String expression = new String(message, "UTF-8"); System.out.println("服务器收到消息: " + expression); String calrResult = null; try{

calrResult = Calculator.cal(expression).toString(); }catch(Exception e){

calrResult = "计算错误：" + e.getMessage();

} //向客户端发送消息 doWrite(calrResult);

} catch (UnsupportedEncodingException e) {

e.printStackTrace(); }

} //发送消息 private void doWrite(String result) {

byte[] bytes = result.getBytes(); ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length); writeBuffer.put(bytes); writeBuffer.flip(); //异步写数据 参数与前⾯的read⼀样 channel.write(writeBuffer, writeBuffer,new CompletionHandler<Integer, ByteBuffer>() {

@Override public void completed(Integer result, ByteBuffer buffer) {

//如果没有发送完，就继续发送直到完成 if (buffer.hasRemaining())

channel.write(buffer, buffer, this);

else{ //创建新的Buffer ByteBuffer readBuffer = ByteBuffer.allocate(1024); //异步读 第三个参数为接收消息回调的业务Handler channel.read(readBuffer, readBuffer, new ReadHandler(channel));

}

} @Override public void failed(Throwable exc, ByteBuffer attachment) {

### try {

channel.close(); } catch (IOException e) {

- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.


} }

});

} @Override public void failed(Throwable exc, ByteBuffer attachment) {

### try {

this.channel.close(); } catch (IOException e) {

e.printStackTrace(); }

} }

OK，这样就已经完成了，其实说起来也简单，虽然代码感觉很多，但是API⽐NIO的使⽤起来真的简 单多了，主要就是监听、读、写等各种CompletionHandler。此处本应有⼀个WriteHandler的，确实， 我们在ReadHandler中，以⼀个匿名内部类实现了它。

下⾯看客户端代码。

# 3.2、Client端代码

Client：

[java]

view plain copy

- 1.
- 2.
- 3.
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
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.


package com.anxpp.io.calculator.aio.client; import java.util.Scanner; public class Client {

private static String DEFAULT_HOST = "127.0.0.1"; private static int DEFAULT_PORT = 12345; private static AsyncClientHandler clientHandle; public static void start(){

start(DEFAULT_HOST,DEFAULT_PORT);

### } public static synchronized void start(String ip,int port){

if(clientHandle!=null)

return; clientHandle = new AsyncClientHandler(ip,port); new Thread(clientHandle,"Client").start();

} //向服务器发送消息 public static boolean sendMsg(String msg) throws Exception{

if(msg.equals("q")) return false; clientHandle.sendMsg(msg); return true;

} @SuppressWarnings("resource") public static void main(String[] args) throws Exception{

Client.start(); System.out.println("请输⼊请求消息："); Scanner scanner = new Scanner(System.in); while(Client.sendMsg(scanner.nextLine()));

} }

AsyncClientHandler：

[java] view plain copy

- 1.
- 2.
- 3.
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
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.


package com.anxpp.io.calculator.aio.client; import java.io.IOException; import java.net.InetSocketAddress; import java.nio.ByteBuffer; import java.nio.channels.AsynchronousSocketChannel; import java.nio.channels.CompletionHandler; import java.util.concurrent.CountDownLatch; public class AsyncClientHandler implements CompletionHandler<Void, AsyncClientHandler>, Runnable

{

private AsynchronousSocketChannel clientChannel; private String host; private int port; private CountDownLatch latch; public AsyncClientHandler(String host, int port) {

this.host = host; this.port = port; try {

//创建异步的客户端通道 clientChannel = AsynchronousSocketChannel.open();

} catch (IOException e) {

e.printStackTrace(); }

} @Override public void run() {

//创建CountDownLatch等待 latch = new CountDownLatch(1); //发起异步连接操作，回调参数就是这个类本身，如果连接成功会回调completed⽅法 clientChannel.connect(new InetSocketAddress(host, port), this, this); try {

latch.await(); } catch (InterruptedException e1) { e1.printStackTrace();

### } try {

clientChannel.close(); } catch (IOException e) {

e.printStackTrace(); }

} //连接服务器成功 //意味着TCP三次握⼿完成 @Override public void completed(Void result, AsyncClientHandler attachment) {

System.out.println("客户端成功连接到服务器...");

} //连接服务器失败 @Override public void failed(Throwable exc, AsyncClientHandler attachment) {

System.err.println("连接服务器失败..."); exc.printStackTrace(); try {

clientChannel.close(); latch.countDown();

- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.


} catch (IOException e) {

e.printStackTrace(); }

} //向服务器发送消息 public void sendMsg(String msg){

byte[] req = msg.getBytes(); ByteBuffer writeBuffer = ByteBuffer.allocate(req.length); writeBuffer.put(req); writeBuffer.flip(); //异步写 clientChannel.write(writeBuffer, writeBuffer,new WriteHandler(clientChannel, latch));

} }

WriteHandler：

[java]

view plain copy

- 1.
- 2.
- 3.
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
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.


package com.anxpp.io.calculator.aio.client; import java.io.IOException; import java.nio.ByteBuffer; import java.nio.channels.AsynchronousSocketChannel; import java.nio.channels.CompletionHandler; import java.util.concurrent.CountDownLatch; public class WriteHandler implements CompletionHandler<Integer, ByteBuffer> {

private AsynchronousSocketChannel clientChannel; private CountDownLatch latch; public WriteHandler(AsynchronousSocketChannel clientChannel,CountDownLatch latch) {

this.clientChannel = clientChannel; this.latch = latch;

} @Override public void completed(Integer result, ByteBuffer buffer) {

//完成全部数据的写⼊ if (buffer.hasRemaining()) {

clientChannel.write(buffer, buffer, this);

### } else {

//读取数据 ByteBuffer readBuffer = ByteBuffer.allocate(1024); clientChannel.read(readBuffer,readBuffer,new ReadHandler(clientChannel, latch));

}

} @Override public void failed(Throwable exc, ByteBuffer attachment) {

System.err.println("数据发送失败..."); try {

clientChannel.close(); latch.countDown();

} catch (IOException e) { }

} }

ReadHandler：

[java]

view plain copy

- 1.
- 2.
- 3.
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
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.


package com.anxpp.io.calculator.aio.client; import java.io.IOException; import java.io.UnsupportedEncodingException; import java.nio.ByteBuffer; import java.nio.channels.AsynchronousSocketChannel; import java.nio.channels.CompletionHandler; import java.util.concurrent.CountDownLatch; public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

private AsynchronousSocketChannel clientChannel; private CountDownLatch latch; public ReadHandler(AsynchronousSocketChannel clientChannel,CountDownLatch latch) {

this.clientChannel = clientChannel; this.latch = latch;

} @Override public void completed(Integer result,ByteBuffer buffer) {

buffer.flip(); byte[] bytes = new byte[buffer.remaining()]; buffer.get(bytes); String body; try {

body = new String(bytes,"UTF-8"); System.out.println("客户端收到结果:"+ body);

} catch (UnsupportedEncodingException e) {

e.printStackTrace(); }

} @Override public void failed(Throwable exc,ByteBuffer attachment) {

System.err.println("数据读取失败..."); try {

clientChannel.close(); latch.countDown();

} catch (IOException e) { }

} }

这个API使⽤起来真的是很顺⼿。

## 3.3、测试

Test：

[java]

view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.


package com.anxpp.io.calculator.aio; import java.util.Scanner; import com.anxpp.io.calculator.aio.client.Client; import com.anxpp.io.calculator.aio.server.Server; /**

- * 测试⽅法

- * @author yangtao__anxpp.com

- * @version 1.0

- */


public class Test { //测试主⽅法 @SuppressWarnings("resource")

- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.


public static void main(String[] args) throws Exception{ //运⾏服务器 Server.start(); //避免客户端先于服务器启动前执⾏代码 Thread.sleep(100); //运⾏客户端 Client.start(); System.out.println("请输⼊请求消息："); Scanner scanner = new Scanner(System.in); while(Client.sendMsg(scanner.nextLine()));

} }

我们可以在控制台输⼊我们需要计算的算数字符串，服务器就会返回结果，当然，我们也可以运⾏

⼤量的客户端，都是没有问题的，以为此处设计为单例客户端，所以也就没有演示⼤量客户端并发。 读者可以⾃⼰修改Client类，然后开辟⼤量线程，并使⽤构造⽅法创建很多的客户端测试。 下⾯是其中⼀次参数的输出：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


服务器已启动，端⼝号：12345 请输⼊请求消息： 客户端成功连接到服务器 . 连接的客户端数：1 123456+789+456 服务器收到消息:123456+789+456 客户端收到结果:124701 9526*56 服务器收到消息:9526*56 客户端收到结果:53456

.

AIO是真正的异步⾮阻塞的，所以，在⾯对超级⼤量的客户端，更能得⼼应⼿。 下⾯就⽐较⼀下，⼏种I/O编程的优缺点。

- 4、各种I/O的对⽐ 先以⼀张表来直观的对⽐⼀下：


![image 3](<Java 网络IO编程总结（BIO、NIO、AIO均含完整实例代码）.note_images/imageFile3.png>)

03

具体选择什么样的模型或者NIO框架，完全基于业务的实际应⽤场景和性能需求，如果客户端很 少，服务器负荷不重，就没有必要选择开发起来相对不那么简单的NIO做服务端；相反，就应考虑使⽤ NIO或者相关的框架了。

- 5、附录 上⽂中服务端使⽤到的⽤于计算的⼯具类：


1. 1. 1. 1. 1. 1.

package com.anxp.utils; import javax.script.ScriptEngine; import javax.script.ScriptEngineManager; import javax.script.ScriptException; publicfinalclasCalculator{ privatefinalstaticScriptEngine jse=newScriptEngineManager().getEngineByName("JavaScri pt"); publicstaticObject cal(String expresion)throwsScriptException{ return jse.eval(expresion);

1. 1. 1. 1.

} }

更多⽂章：

Java NIO框架Netty简单使⽤

后续会写⼀篇NIO框架Netty的教程，不过这段时间有⼀点⼩忙。

