Java NIO中的Bufer⽤于和NIO chanel 进⾏交互。 如你所知，数据是从通道读⼊缓冲区，从缓冲区写⼊到通道中的。 缓冲区本质上是⼀块可以写⼊数据，然后可以从中读取数据的内存。这块内存被包装成NIO Bufer对 象，并提供了⼀组⽅法，⽤来⽅便的访问该块内存。

# Bufer的基本⽤法

使⽤Bufer读写数据⼀般遵循以下四个步骤：

- 1.
- 2.
- 3.
- 4.


写⼊数据到Bufer 调⽤ flip() ⽅法 从Bufer中读取数据 调⽤ clear() ⽅法或者 compact() ⽅法

当向bufer写⼊数据时，bufer会记录下写了多少数据。⼀旦要读取数据，需要通过flip()⽅法将Bufer 从写模式切换到读模式。在读模式下，可以读取之前写⼊到bufer的所有数据。 ⼀旦读完了所有的数据，就需要清空缓冲区，让它可以再次被写⼊。有两种⽅式能清空缓冲区：调⽤ clear()或compact()⽅法。clear()⽅法会清空整个缓冲区。compact()⽅法只会清除已经读过的数据。 任何未读的数据都被移到缓冲区的起始处，新写⼊的数据将放到缓冲区未读数据的后⾯。

<table>
  <tr>
    <th>1 RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");<br><br>2 FileChannel inChannel = aFile.getChannel();<br><br>3<br><br>4 //create buffer with capacity of 48 bytes<br><br>5 ByteBuffer buf = ByteBuffer.allocate(48);<br><br>6<br><br>7 int bytesRead = inChannel.read(buf); //read into buffer.<br><br>8 while (bytesRead != -1) {<br><br>9<br><br>10 buf.flip(); //make buffer ready for read<br><br>11<br><br>12 while(buf.hasRemaining()){<br><br>13 System.out.print((char) buf.get()); // read 1 byte at a time<br><br>14 }<br><br>15<br><br>16 buf.clear(); //make buffer ready for writing<br><br>17 bytesRead = inChannel.read(buf);<br><br>18 }<br><br>19 aFile.close();<br></th>
  </tr>
</table>


# Bufer的capacity,position和limit

为了理解Bufer的⼯作原理，需要熟悉它的三个属性：

capacity position limit

position和limit的含义取决于Bufer处在读模式还是写模式。不管Bufer处在什么模式，capacity的含 义总是⼀样的。 这⾥有⼀个关于capacity，position和limit在读写模式中的说明，详细的解释在插图后⾯。

![image 1](<Java NIO Buffer.note_images/imageFile1.png>)

capacity

作为⼀个内存块，Bufer有⼀个固定的⼤⼩值，也叫“capacity”. 你只能往⾥写capacity个byte、long，char等类型。 ⼀旦Bufer满了，需要将其清空（通过读数据或者清除数据）才能继续写数据往⾥写数据。

position

当你写数据到Bufer中时，position表示当前的位置。 初始的position值为0.当⼀个byte、long等数据写到Bufer后， position会向前移动到下⼀个可插⼊数 据的Bufer单元。 position最⼤可为capacity – 1. 当读取数据时，也是从某个特定位置读。 当将Bufer从写模式切换到读模式，position会被重置为0.

当从Bufer的position处读取数据时，position向前移动到下⼀个可读的位置。

limit

在写模式下，Bufer的limit表示你最多能往Bufer⾥写多少数据。 写模式下，limit等于Bufer的capacity。 当切换Bufer到读模式时， limit表示你最多能读到多少数据。 因此，当切换Bufer到读模式时，limit会被设置成写模式下的position值。

# Bufer的类型

Java NIO 有以下Bufer类型

ByteBufer

MapedByteBufer

CharBufer

DoubleBufer

FloatBufer

IntBufer

LongBufer

ShortBuferBufer的分配

要想获得⼀个Bufer对象⾸先要进⾏分配。 每⼀个Bufer类都有⼀个alocate⽅法。下⾯是⼀个分配48 字节capacity的ByteBufer的例⼦。

<table>
  <tr>
    <th>ByteBuffer buf = ByteBuffer.allocate(<br><br>48 );<br><br></th>
  </tr>
</table>


这是分配⼀个可存储1024个字符的CharBufer：

<table>
  <tr>
    <th>CharBuffer buf = CharBuffer.allocate( 1024 );<br><br></th>
  </tr>
</table>


# 向Bufer中写数据

写数据到Bufer有两种⽅式：

从Chanel写到Bufer。

通过Bufer的put()⽅法写到Bufer⾥。

从Chanel写到Bufer的例⼦

<table>
  <tr>
    <th>int bytesRead = inChannel.read(buf); //read into buffer.<br><br></th>
  </tr>
</table>


通过put⽅法写Bufer的例⼦：

<table>
  <tr>
    <th>buf.put( 127 );<br><br></th>
  </tr>
</table>


put⽅法有很多版本，允许你以不同的⽅式把数据写⼊到Bufer中。例如， 写到⼀个指定的位置，或者 把⼀个字节数组写⼊到Bufer。 更多Bufer实现的细节参考JavaDoc。

## flip()⽅法

flip⽅法将Bufer从写模式切换到读模式。调⽤flip()⽅法会将position设回0，并将limit设置成之前 position的值。

# 从Bufer中读取数据

从Bufer中读取数据有两种⽅式：

- 1.
- 2.


从Bufer读取数据到Chanel。 使⽤get()⽅法从Bufer中读取数据。

从Bufer读取数据到Chanel的例⼦：

<table>
  <tr>
    <th>//read from buffer into channel. int<br><br>bytesWritten = inChannel.write(buf);</th>
  </tr>
</table>


使⽤get()⽅法从Bufer中读取数据的例⼦

<table>
  <tr>
    <th>byte aByte = buf.get();<br><br></th>
  </tr>
</table>


get⽅法有很多版本，允许你以不同的⽅式从Bufer中读取数据。例如，从指定position读取，或者从 Bufer中读取数据到字节数组。更多Bufer实现的细节参考JavaDoc。

rewind()⽅法

Bufer.rewind()将position设回0，所以你可以重读Bufer中的所有数据。limit保持不变，仍然表示能从 Bufer中读取多少个元素（byte、char等）。

clear()与compact()⽅法

clear()⽅法，position将被设回0，limit被设置成 capacity的值。 换句话说，Bufer 被清空了。 Bufer中的数据并未清除，只是这些标记告诉我们可以从哪⾥开始往Bufer⾥写数据。 如果Bufer中有⼀些未读的数据，调⽤clear()⽅法，数据将“被遗忘”，意味着不再有任何标记会告诉你 哪些数据被读过，哪些还没有。

compact():Bufer中仍有未读的数据，且后续还需要这些数据，但是此时想要先先写些数据，那么使⽤ compact()⽅法。⽅法将所有未读的数据拷⻉到Bufer起始处。然后将position设到最后⼀个未读元素 正后⾯。limit属性依然像clear()⽅法⼀样，设置成capacity。现在Bufer准备好写数据了，但是不会覆 盖未读的数据。

mark()与reset()⽅法

通过调⽤Bufer.mark()⽅法，可以标记Bufer中的⼀个特定position。之后可以通过调⽤Bufer.reset() ⽅法恢复到这个position。例如：

<table>
  <tr>
    <th>buffer.mark();</th>
  </tr>
</table>


<table>
  <tr>
    <th>//call buffer.get() a couple of times, e.g. during parsing.</th>
  </tr>
</table>


<table>
  <tr>
    <th>buffer.reset(); //set position back to mark.<br><br></th>
  </tr>
</table>


# equals()与compareTo()⽅法

可以使⽤equals()和compareTo()⽅法⽐较两个Bufer。

equals()

当满⾜下列条件时，表示两个Bufer相等：

- 1.
- 2.
- 3.


有相同的类型（byte、char、int等）。 Bufer中剩余的byte、char等的个数相等。 Bufer中所有剩余的byte、char等都相同。

如你所⻅，equals只是⽐较Bufer的⼀部分，不是每⼀个在它⾥⾯的元素都⽐较。实际上，它只⽐较 Bufer中的剩余元素。

## compareTo()⽅法

compareTo()⽅法⽐较两个Bufer的剩余元素(byte、char等)， 如果满⾜下列条件，则认为⼀个 Bufer“⼩于”另⼀个Bufer：

- 1.
- 2.


第⼀个不相等的元素⼩于另⼀个Bufer中对应的元素 。 所有元素都相等，但第⼀个Bufer⽐另⼀个先耗尽(第⼀个Bufer的元素个数⽐另⼀个少)。

