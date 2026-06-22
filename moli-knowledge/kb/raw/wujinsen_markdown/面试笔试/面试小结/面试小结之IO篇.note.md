最近⾯试⼀些公司，被问到的关于Java NIO编程的问题，以及⾃⼰总结的回答。

谈谈对Java IO的认识。

对于I/O操作来说, 其根本的作⽤在于传输数据。输⼊和输出指的仅是数据的流向，实际传输是通过 某些具体的媒介来完成的，其中最主要的是⽂件系统和⽹络连接； 早期的java.io包把I/O操作抽象成数据的流动，进⽽有了流的概念；在Java NIO中，则把I/O操作抽象 成端到端的⼀个数据连接，这就有了通道（chanel）的概念； Java中最基本的流是在字节这个层次上进⾏操作的；在read⽅法的调⽤是阻塞的，这可能会成为应 ⽤中的瓶颈（可以通过available⽅法获取在不阻塞的情况下可以获取到的字节数）；流⽆法重新使 ⽤，BuferedInputStream通过mark和reset操作可以实现流中部分内容的重复读取；另外⼀种重⽤ 输⼊流的⽅式是把它转换成数据来使⽤； 输出流是通过write⽅法把数据存放在缓冲区（缓冲区满了会⾃动执⾏写⼊），使⽤flush⽅法强制进 ⾏实际的写⼊操作；

其 他 常 ⽤ 流 ： FileInput(Output)Stream、 ByteArayInput(Output)Stream、 字 符 流 （ new BuferedReader(new InputStreamReader(inputStream)）；

介绍⼀下Java NIO中的Bufer、Chanel和Selector的概念和作⽤。

Java NIO的缓冲区：使⽤数组的⽅式不够灵活且性能差，Java NIO的缓冲区功能更加强⼤；容量 (capacity)表示缓冲区的额定⼤⼩，需要在创建时指定（alocate静态⽅法）；读写限制(limit)表示 缓冲区在进⾏读写操作时的最⼤允许位置；读写位置(position)表示当前进⾏读写操作时的位置；缓 冲区的很多操作（clear、flip、rewind）都是操作limit和position的值来实现重复读写； Java NIO的通道：chanel表示为⼀个已经建⽴好的到⽀持I/O操作的实体（如⽂件和⽹络）的连 接，在此连接上进⾏数据的读写操作，使⽤的是缓冲区来实现读写；

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br></th>
    <th>public void openAndWrite() throws IOException {<br><br>FileChanel chanel = FileChanel.open(Paths.get("my.txt"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);<br><br>ByteBufer bufer = ByteBufer.alocate(64); bufer.putChar('A').flip(); chanel.write(bufer);</th>
  </tr>
</table>


}

Socket和ServerSocket类中提供的建⽴连接和数据传输相关的⽅法都是阻塞式的；对服务端通常使 ⽤线程池的⽅式来调⽤ServerSocket.acept⽅法来监听连接请求；Java NIO提供了⾮阻塞式和多路 复⽤的套接字连接；

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br></th>
    <th>public void startSimpleServer() throws IOException{<br><br>ServerSocketChanel chanel = ServerSocketChanel.open();<br><br>chanel.bind(new InetSocketAdres("localhost",<br><br>1080); while(true){ try(SocketChanel sc = chanel.acept(){ sc.write(ByteBufer.wrap("Helo".getBytes("UTF-8" ); } }</th>
  </tr>
</table>


}

套接字通道的多路复⽤的思想⽐较简单，通过⼀个专⻔的选择器（Selector）来同时对多个套接字 通道进⾏监听；当其中的某些套接字通道上有它感兴趣的事件发⽣时，这些通道就会变为可⽤状 态，可以在选择器的选择操作中被选中；可⽤通道的选择⼀般是通过操作系统提供的底层操作系统 调⽤来实现的，性能也⽐较⾼；

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10


public clas LoadWebPageUseSelector {

/ 通过Selector同时下载多个⽹⻚的内容 public void load(Set<URL> urls) throws IOException { Map<SocketAdres, String> maping =

urlToSocketAdres(urls);

- / 1. 创建Selector

Selector selector = Selector.open();

- / 2. 将套接字Chanel注册到Selector上

for (SocketAdres adres : maping.keySet() { register(selector, adres); } int finished = 0; int total = maping.size(); ByteBufer bufer = ByteBufer.alocate(32 * 1024); int len = -1; while (finished < total) {

- / 3. 调⽤select⽅法进⾏通道选择，该⽅法会阻塞，直到⾄


- 1

- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21


- 2

- 23
- 24
- 25
- 26
- 27
- 28
- 29
- 30
- 31
- 32


- 3

- 34
- 35
- 36
- 37
- 38
- 39
- 40
- 41
- 42
- 43


- 4

- 45
- 46
- 47
- 48
- 49
- 50
- 51
- 52
- 53
- 54


- 5


少有⼀个他们所感兴趣的事件发⽣，然后可以通过 selectedKeys获取被选中的通道的对象集合

selector.select(); Iterator<SelectionKey> iterator =

selector.selectedKeys().iterator(); while (iterator.hasNext() { SelectionKey key = iterator.next(); iterator.remove(); if (key.isValid() & key.isConectable() { SocketChanel chanel = (SocketChanel)

key.chanel(); / 4. 如果连接成功，则发送HTP请求；失败则取消该连

接； bolean suces = chanel.finishConect(); if (!suces) { finished+; key.cancel(); } else { InetSocketAdres adres = (InetSocketAdres)

chanel.getRemoteAdres(); String path = maping.get(adres); String request = "GET" + path + "HTP/1.0\r\n\r\nHost:"

+ adres.getHostString() + "\r\n\r\n"; ByteBufer header =

ByteBufer.wrap(request.getBytes("UTF-8"); chanel.write(header); } } else if (key.isValid() & key.isReadable() {

/ 5. 当chanel处于可读时则读取chanel的数据并写⼊⽂件 SocketChanel chanel = (SocketChanel)

key.chanel(); InetSocketAdres adres = (InetSocketAdres)

chanel.getRemoteAdres(); String filename = adres.getHostName() + ".txt"; FileChanel destChanel =

FileChanel.open(Paths.get(filename), StandardOpenOption.APEND, StandardOpenOption.CREATE);

- 56
- 57
- 58
- 59
- 60
- 61
- 62
- 63
- 64


bufer.clear();

/ 6. 当返回0时表示本次没有数据可读不需要操作；如果 为-1则表示所有数据亿级读取完毕，可以关闭；

while(len = chanel.read(bufer) > 0| bufer.position()

!= 0) { bufer.flip(); destChanel.write(bufer);

65 6

bufer.compact(); } if (len = -1) { finished+; key.cancel(); } } } } } private void register(Selector selector, SocketAdres

- 67
- 68
- 69
- 70
- 71
- 72
- 73
- 74
- 75
- 76
- 77
- 78
- 79
- 80
- 81
- 82
- 83
- 84
- 85
- 86
- 87 8


adres) throws IOException { SocketChanel chanel = SocketChanel.open();

/ 设置为⾮阻塞模式 chanel.configureBlocking(false); chanel.conect(adres);

/ 注册时需要指定感兴趣的事件类型 chanel.register(selector, SelectionKey.OP_CONECT |

SelectionKey.OP_READ); } private Map<SocketAdres, String>

urlToSocketAdres(Set<URL> urls) { Map<SocketAdres, String> maping = new

HashMap<>(); for (URL url : urls) { int port = url.getPort() != -1 ? url.getPort() :

- 89
- 90
- 91
- 92


url.getDefaultPort(); SocketAdres adres = new

InetSocketAdres(url.getHost(), port); String path = url.getPath(); if (url.getQuery() != nul) { path = path + "?" + url.getQuery(); } maping.put(adres, path); } return maping; }

}

Java 7的版本对Java NIO有哪些增强？

Java 7中的NIO.2进⼀步增强，主要包括⽂件系统访问和异步I/O通道； 引⼊Path接⼝作为⽂件系统中路径的⼀种抽象，来代替之前字符串处理的⽅式，更加语义化；引⼊ DirectoryStream来⽀持⽬录下⼦⽬录和⽂件的遍历，它的优势在于它渐进式地遍历，每次只读取⼀ 定 数 量 的 内 容 ， 从 ⽽ 可 以 降 低 遍 历 时 的 开 销 （ DirectoryStreamstream= Files.newDirectoryStream(path, “*.java”)）；如果要递归地遍历⼦⽬录下的⼦⽬录，对整个⽬录树 进⾏遍历，可以使⽤FileVisitor；通过引⼊⽂件视图FileAtributeView来获取和设置⽂件的各种属 性；另外还提供了新的⽬录监视服务，当指定⽬录下的⼦⽬录或⽂件被创建、更新或删除时可以得 到事件通知；Files⼯具类提供了⼀系列静态⽅法可以满⾜常⻅的需求；

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br><br>23<br>24<br>25<br>26<br>27<br>28<br></th>
    <th>public void calculate() throws IOException, InteruptedException {<br><br>WatchService service =<br><br>FileSystems.getDefault().newWatchService(); Path path = Paths.get(").toAbsolutePath(); path.register(service,<br><br>StandardWatchEventKinds.ENTRY_CREATE); while (true) { WatchKey watchKey = service.take(); for (WatchEvent<?> event : watchKey.polEvents() { Path createdPath = (Path) event.context(); createdPath = path.resolve(createdPath); long size = Files.size(createdPath); System.out.println(createdPath + "=>" + size); } watchKey.reset(); }<br><br>} public void manipulateFiles() throws IOException {<br><br>Path newFile =<br><br>Files.createFile(Paths.get("new.txt").toAbsolutePath(); List<String> content = Arays.asList("Helo", "World"); Files.write(newFile, content, Charset.forName("UTF-<br><br>8"); Files.size(newFile); byte[] bytes = Files.readAlBytes(newFile); ByteArayOutputStream outputStream = new<br><br>ByteArayOutputStream(); Files.copy(newFile, outputStream); Files.delete(newFile);</th>
  </tr>
</table>


}

异步I/O通道⼀般提供两种使⽤⽅式：⼀会⾛是通过Future类的对象来表示异步操作的结果，另外⼀ 种是在执⾏操作时传⼊⼀个CompletionHandler接⼝的实现对象作为操作完成时的回调⽅法；异步 ⽂件通道由AsynchronousFileChanel类表示，它没有当前读写位置的概念。

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br><br>23<br>24<br></th>
    <th>public void asyncWrite() throws IOException, ExecutionException, InteruptedException {<br><br>AsynchronousFileChanel chanel = AsynchronousFileChanel.open(Paths.get("large.bin"),<br><br>StandardOpenOption.CREATE, StandardOpenOption.WRITE);<br><br>ByteBufer bufer = ByteBufer.alocate(32 * 1024 *<br><br>1024); Future<Integer> result = chanel.write(bufer, 0); Integer len = result.get();<br><br>} public void startAsyncSimpleServer() throws IOException {<br><br>AsynchronousChanelGroup group = AsynchronousChanelGroup.withFixedThreadPol(10, Executors.defaultThreadFactory();<br><br>final AsynchronousServerSocketChanel serverChanel<br><br>= AsynchronousServerSocketChanel.open(group).bind(ne w InetSocketAdres(1080);<br><br>serverChanel.acept(nul, new CompletionHandler<AsynchronousSocketChanel, Void> () {<br><br>@Overide public void completed(AsynchronousSocketChanel<br><br>result, Void atachment) { serverChanel.acept(nul, this);<br><br>/ 使⽤clientChanel } @Overide public void failed(Throwable exc, Void atachment) {<br><br>/ 错误处理 } });</th>
  </tr>
</table>


}

其他⾯试⼩结

⾯试⼩结之Elasticsearch篇

⾯试⼩结之JVM篇 ⾯试⼩结之并发篇 ⾯试⼩结之IO篇

⾯试⼩结之综合篇

