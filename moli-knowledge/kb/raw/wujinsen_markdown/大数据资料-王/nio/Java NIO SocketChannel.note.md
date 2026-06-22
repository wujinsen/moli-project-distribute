Java NIO中的SocketChanel是⼀个连接到TCP⽹络套接字的通道。可以通过以下2种⽅式创建 SocketChanel：

- 1.
- 2.


打开⼀个SocketChanel并连接到互联⽹上的某台服务器。 ⼀个新连接到达ServerSocketChanel时，会创建⼀个SocketChanel。

# 打开 SocketChanel

下⾯是SocketChanel的打开⽅式：

<table>
  <tr>
    <th>ServerSocketChanel serverSocketChanel = ServerSocketChanel.open();ServerSocket s = se 绑定地址<br><br>1 SocketChannel socketChannel = SocketChannel.open();<br><br>2 socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80));<br></th>
  </tr>
</table>


rverSocketChanel.socket();s.bind(new InetSocketAdres("localhost", 9026);/

# 关闭 SocketChanel

当⽤完SocketChanel之后调⽤SocketChanel.close()关闭SocketChanel：

<table>
  <tr>
    <th>socketChannel.close();</th>
  </tr>
</table>


从 SocketChanel 读取数据

要从SocketChanel中读取数据，调⽤⼀个read()的⽅法之⼀。以下是例⼦：

<table>
  <tr>
    <th>1 ByteBuffer buf = ByteBuffer.allocate(48);<br><br>2 int bytesRead = socketChannel.read(buf);<br></th>
  </tr>
</table>


⾸先，分配⼀个Bufer。从SocketChanel读取到的数据将会放到这个Bufer中。 然后，调⽤SocketChanel.read()。该⽅法将数据从SocketChanel 读到Bufer中。 read()⽅法返回的int值表示读了多少字节进Bufer⾥。 如果返回的是-1，表示已经读到了流的末尾（连接关闭了）。

写⼊ SocketChanel

写数据到SocketChanel⽤的是SocketChanel.write()⽅法，该⽅法以⼀个Bufer作为参数。示例如 下：

<table>
  <tr>
    <th>String newData = "New String to write to file..." + System.currentTimeMillis();<br><br>1<br><br>2<br><br>3 ByteBuffer buf = ByteBuffer.allocate(48);<br><br>4 buf.clear();<br><br>5 buf.put(newData.getBytes());<br><br>6<br><br>7 buf.flip();<br><br>8 while(buf.hasRemaining()) {<br><br>9 channel.write(buf);<br><br>10 }<br></th>
  </tr>
</table>


注意SocketChanel.write()⽅法的调⽤是在⼀个while循环中的。Write()⽅法⽆法保证能写多少字节到 SocketChanel。所以，我们重复调⽤write()直到Bufer没有要写的字节为⽌。

# ⾮阻塞模式

可以设置 SocketChanel 为⾮阻塞模式（non-blocking mode）.设置之后，就可以在异步模式下调⽤ conect(), read() 和write()了。

conect()

如果SocketChanel在⾮阻塞模式下，此时调⽤conect()，该⽅法可能在连接建⽴之前就返回了。为 了确定连接是否建⽴，可以调⽤finishConect()的⽅法。像这样：

<table>
  <tr>
    <th>1 socketChannel.configureBlocking(false);//⾮阻塞<br><br>2 socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80));<br><br>3<br><br>4 while(! socketChannel.finishConnect() ){<br><br>5 //wait, or do something else...<br><br>6 }<br></th>
  </tr>
</table>


write()

⾮阻塞模式下，write()⽅法在尚未写出任何内容时可能就返回了。所以需要在循环中调⽤write()。

read()

⾮阻塞模式下,read()⽅法在尚未读取到任何数据时可能就返回了。所以需要关注它的int返回值，它会 告诉你读取了多少字节。

# ⾮阻塞模式与选择器

⾮阻塞模式与选择器搭配会⼯作的更好，通过将⼀或多个SocketChanel注册到Selector，可以询问选 择器哪个通道已经准备好了读取，写⼊等。

