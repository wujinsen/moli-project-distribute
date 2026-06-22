在Java NIO中，如果两个通道中有⼀个是FileChanel，那你可以直接将数据从⼀个chanel传输到另 外⼀个chanel。

transferFrom() 可以将数据从源通道传输到FileChanel中，下⾯是⼀个简单的例⼦：

<table>
  <tr>
    <th>1 RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");<br><br>2 FileChannel fromChannel = fromFile.getChannel();<br><br>3<br><br>4 RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");<br><br>5 FileChannel toChannel = toFile.getChannel();<br><br>6<br><br>7 long position = 0;<br><br>8 long count = fromChannel.size();<br><br>9 toChannel.transferFrom(position, count, fromChannel);<br></th>
  </tr>
</table>


- 1、⽅法的输⼊参数position表示从position处开始向⽬标⽂件写⼊数据
- 2、count表示最多传输的字节数。
- 3、如果源通道的剩余空间⼩于 count 个字节，则所传输的字节数要⼩于请求的字节数。
- 4、此外要注意，在SoketChanel的实现中，SocketChanel只会传输此刻准备好的数据 （可能不⾜count字节）。因此，SocketChanel可能不会将请求的所有数据(count个字节)全部传输到 FileChanel中。


transferTo() 将数据从FileChanel传输到其他的chanel中。下⾯是⼀个简单的例⼦：

<table>
  <tr>
    <th>1 RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");<br><br>2 FileChannel fromChannel = fromFile.getChannel();<br><br>3<br><br>4 RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");<br><br>5 FileChannel toChannel = toFile.getChannel();<br><br>6<br><br>7 long position = 0;<br><br>8 long count = fromChannel.size();<br><br>9 fromChannel.transferTo(position, count, toChannel);<br></th>
  </tr>
</table>


# 和上⾯的例⼦⽐较，除了调⽤⽅法的FileChanel对象不⼀样外，其他的都⼀样。 上⾯所说的关于SocketChanel的问题在transferTo()⽅法中同样存在。SocketChanel会⼀直传输数 据直到⽬标bufer被填满。

