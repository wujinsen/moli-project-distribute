Java NIO的通道类似流，但⼜有些不同：

既可以从通道中读取数据，⼜可以写数据到通道。但流的读写通常是单向的。

通道可以异步地读写。

通道中的数据总是要先读到⼀个Bufer，或者总是要从⼀个Bufer中写⼊。

正如上⾯所说，从通道读取数据到缓冲区，从缓冲区写⼊数据到通道。如下图所示：

![image 1](<Java NIO Channel.note_images/imageFile1.png>)

Chanel的实现

这些是Java NIO中最重要的通道的实现： FileChannel 从⽂件中读写数据。

DatagramChanel 能通过UDP读写⽹络中的数据。 SocketChanel 能通过TCP读写⽹络中的数据。 ServerSocketChanel可以监听新进来的TCP连接，像Web服务器那样。对每⼀个新进来的连接都会

创建⼀个SocketChanel。

基本的 Chanel 示例

下⾯是⼀个使⽤FileChanel读取数据到Bufer中的示例：

<table>
  <tr>
    <th>1 RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");<br><br>2 FileChannel inChannel = aFile.getChannel();<br><br>3<br><br>4 ByteBuffer buf = ByteBuffer.allocate(48);<br><br>5<br><br>6 int bytesRead = inChannel.read(buf);<br><br>7 while (bytesRead != -1) {<br><br>8<br><br>9 System.out.println("Read " + bytesRead);<br><br>10 buf.flip();<br><br>11<br><br>12 while(buf.hasRemaining()){<br><br>13 System.out.print((char) buf.get());<br><br>14 }<br><br>15<br><br>16 buf.clear();<br><br>17 bytesRead = inChannel.read(buf);<br><br>18 }<br><br>19 aFile.close();<br></th>
  </tr>
</table>


# 注意 buf.flip() 的调⽤，⾸先读取数据到Bufer，然后反转Bufer,接着再从Bufer中读取数据。下⼀节 会深⼊讲解Bufer的更多细节。

