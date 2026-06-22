Java NIO中的FileChanel是⼀个连接到⽂件的通道。可以通过⽂件通道读写⽂件。 FileChanel⽆法设置为⾮阻塞模式，它总是运⾏在阻塞模式下。

# 打开FileChanel

在使⽤FileChanel之前，必须先打开它。但是，我们⽆法直接打开⼀个FileChanel，需要通过使⽤⼀ 个InputStream、OutputStream或RandomAcesFile来获取⼀个FileChanel实例。 下⾯是通过RandomAcesFile打开FileChanel的示例：

<table>
  <tr>
    <th>1 RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");<br><br>2 FileChannel inChannel = aFile.getChannel();<br></th>
  </tr>
</table>


从FileChanel读取数据

调⽤多个read()⽅法之⼀从FileChanel中读取数据。如：

<table>
  <tr>
    <th>1 ByteBuffer buf = ByteBuffer.allocate(48);<br><br>2 int bytesRead = inChannel.read(buf);<br></th>
  </tr>
</table>


⾸先，分配⼀个Bufer。从FileChanel中读取的数据将被读到Bufer中。 然后，调⽤FileChanel.read()⽅法。该⽅法将数据从FileChanel读取到Bufer中。 read()⽅法返回的int值表示了有多少字节被读到了Bufer中。 如果返回-1，表示到了⽂件末尾。

向FileChanel写数据

使⽤FileChanel.write()⽅法向FileChanel写数据，该⽅法的参数是⼀个Bufer。如：

<table>
  <tr>
    <th>String newData = "New String to write to file..." + System.currentTimeMillis();<br><br>1<br><br>2<br><br>3 ByteBuffer buf = ByteBuffer.allocate(48);<br><br>4 buf.clear();<br><br>5 buf.put(newData.getBytes());<br><br>6<br><br>7 buf.flip();<br><br>8<br><br>9 while(buf.hasRemaining()) {<br><br>10 channel.write(buf);<br><br>11 }<br></th>
  </tr>
</table>


注意FileChanel.write()是在while循环中调⽤的。因为⽆法保证write()⽅法⼀次能向FileChanel写⼊ 多少字节，因此需要重复调⽤write()⽅法，直到Bufer中已经没有尚未写⼊通道的字节。

# 关闭FileChanel

⽤完FileChanel后必须将其关闭。如：

<table>
  <tr>
    <th>channel.close();</th>
  </tr>
</table>


# FileChanel的position⽅法

有时可能需要在FileChanel的某个特定位置进⾏数据的读/写操作。可以通过调⽤position()⽅法获取 FileChanel的当前位置。 也可以通过调⽤position(long pos)⽅法设置FileChanel的当前位置。 这⾥有两个例⼦:

<table>
  <tr>
    <th>1 long pos = channel.position();<br><br>2 channel.position(pos +123);<br></th>
  </tr>
</table>


如果将位置设置在⽂件结束符之后，然后试图从⽂件通道中读取数据，读⽅法将返回-1 ⸺ ⽂件结束 标志。

如果将位置设置在⽂件结束符之后，然后向通道中写数据，⽂件将撑⼤到当前位置并写⼊数据。这可 能导致“⽂件空洞”，磁盘上物理⽂件中写⼊的数据间有空隙。

# FileChanel的size⽅法

FileChanel实例的size()⽅法将返回该实例所关联⽂件的⼤⼩。如:

<table>
  <tr>
    <th>long fileSize = channel.size();<br><br></th>
  </tr>
</table>


# FileChanel的truncate⽅法

可以使⽤FileChanel.truncate()⽅法截取⼀个⽂件。截取⽂件时，⽂件将中指定⻓度后⾯的部分将被 删除。如：

<table>
  <tr>
    <th>channel.truncate( 1024 );<br><br></th>
  </tr>
</table>


这个例⼦截取⽂件的前1024个字节。

# FileChanel的force⽅法

FileChanel.force()⽅法将通道⾥尚未写⼊磁盘的数据强制写到磁盘上。出于性能⽅⾯的考虑，操作系 统会将数据缓存在内存中，所以⽆法保证写⼊到FileChanel⾥的数据⼀定会即时写到磁盘上。要保证 这⼀点，需要调⽤force()⽅法。 force()⽅法有⼀个bolean类型的参数，指明是否同时将⽂件元数据（权限信息等）写到磁盘上。 下⾯的例⼦同时将⽂件数据和元数据强制写到磁盘上：

<table>
  <tr>
    <th>channel.force( true );<br><br></th>
  </tr>
</table>


