1、RandomAcesFile是⽤来访问那些保存数据记录的⽂件的，你就可以⽤sek( )⽅法来访问记录， 并进⾏读写了。这些记录的⼤⼩不必相同；但是其⼤⼩和位置必须是可知的。但是该类仅限于操作⽂ 件。2、RandomAcesFile不属于InputStream和OutputStream类系的。除了实现DataInput和

DataOutput接⼝之外，和InputStream、OutputStream⼀点关系有没有，⽅法也完全不同，踏实独⽴继 承object的类。 3、RandomAcesFile的⼯作⽅式是，把DataInputStream和DataOutputStream结合起来，再加上它 ⾃⼰的⼀些⽅法，⽐如定位⽤的getFilePointer( )，在⽂件⾥移动⽤的sek( )，以及判断⽂件⼤⼩的 length( )、skipBytes()跳过多少字节数。此外，它的构造函数还要⼀个表示以只读⽅式("r")，还是以读 写⽅式("rw")打开⽂件的参数 (和C的fopen( )⼀模⼀样)。它不⽀持只写⽂件。

<table>
  <tr>
    <th colspan="2">构造⽅法摘要</th>
  </tr>
  <tr>
    <td>Rmode)andomAcesFile( File file, String<br><br>创建从中读取和向其中写⼊（可选）的随机 访问⽂件流，该⽂件由<br><br>参数指定。<br><br>File</td>
    <td> </td>
  </tr>
  <tr>
    <td>RandomAcesFile( String name, String mode)<br><br>创建从中读取和向其中写⼊（可选）的随机 访问⽂件流，该⽂件具有指定名称。</td>
    <td> </td>
  </tr>
</table>


<table>
  <tr>
    <th colspan="2">⽅法摘要</th>
  </tr>
  <tr>
    <td>void</td>
    <td>close()<br><br>关闭此随机访问⽂件流并释放与该流关联的 所有系统资源。</td>
  </tr>
  <tr>
    <td>FileChanel</td>
    <td>getChanel ()<br><br>返回与此⽂件关联的唯⼀<br><br>对象。<br><br>FileChannel</td>
  </tr>
  <tr>
    <td>FileDescriptor</td>
    <td>getFD()<br><br>返回与此流关联的不透明⽂件描述符对象。</td>
  </tr>
  <tr>
    <td>long</td>
    <td>getFilePointer()<br><br>返回此⽂件中的当前偏移量。</td>
  </tr>
</table>


<table>
  <tr>
    <th>long</th>
    <th>length()<br><br>返回此⽂件的⻓度。</th>
  </tr>
  <tr>
    <td>int</td>
    <td>read()<br><br>从此⽂件中读取⼀个数据字节。</td>
  </tr>
  <tr>
    <td>int</td>
    <td>read(byte[] b)<br><br>将最多 b.length<br><br>数组。</td>
  </tr>
  <tr>
    <td>int</td>
    <td>个数据字节从此⽂件读⼊ byte<br><br>read(byte[] b, int off, int len)<br><br>将最多 len<br><br>数组。</td>
  </tr>
  <tr>
    <td>boolean</td>
    <td>个数据字节从此⽂件读⼊ byte readBolean()<br><br>从此⽂件读取⼀个 boolean<br><br>。</td>
  </tr>
  <tr>
    <td>byte</td>
    <td>readByte()<br><br>从此⽂件读取⼀个有符号的⼋位值。</td>
  </tr>
  <tr>
    <td>char</td>
    <td>readChar()<br><br>从此⽂件读取⼀个字符。</td>
  </tr>
  <tr>
    <td>double</td>
    <td>readDouble()<br><br>从此⽂件读取⼀个 double<br><br>。</td>
  </tr>
  <tr>
    <td>float</td>
    <td>readFloat ()<br><br>从此⽂件读取⼀个 float<br><br>。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>readFuly(byte[] b)<br><br>将 b.length<br><br>个字节从此⽂件读⼊ byte 数组，并从当前⽂件指 针开始。</td>
  </tr>
</table>


<table>
  <tr>
    <th>void<br><br></th>
    <th>readFuly(byte[] b, int off, int len)<br><br>将正好 len<br><br>个字节从此⽂件读⼊ byte 数组，并从当前⽂件指 针开始。</th>
  </tr>
  <tr>
    <td>int</td>
    <td>readInt ()<br><br>位整数。</td>
  </tr>
  <tr>
    <td>String</td>
    <td>从此⽂件读取⼀个有符号的 32 readLine()<br><br>从此⽂件读取⽂本的下⼀⾏。</td>
  </tr>
  <tr>
    <td>long</td>
    <td>readLong()<br><br>位整数。</td>
  </tr>
  <tr>
    <td>short</td>
    <td>从此⽂件读取⼀个有符号的 64 readShort ()<br><br>位数。</td>
  </tr>
  <tr>
    <td>int</td>
    <td>从此⽂件读取⼀个有符号的 16 readUnsignedByte()<br><br>从此⽂件读取⼀个⽆符号的⼋位数。</td>
  </tr>
  <tr>
    <td>int</td>
    <td>readUnsignedShort ()<br><br>位数。</td>
  </tr>
  <tr>
    <td>String</td>
    <td>从此⽂件读取⼀个⽆符号的 16 readUTF()<br><br>从此⽂件读取⼀个字符串。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>sek(long pos)<br><br>设置到此⽂件开头测量到的⽂件指针偏移 量，在该位置发⽣下⼀个读取或写⼊操作。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>setLength(long newLength)<br><br>设置此⽂件的⻓度。</td>
  </tr>
  <tr>
    <td>int</td>
    <td>skipBytes(int n)<br><br>尝试跳过输⼊的 n<br><br>个字节以丢弃跳过的字节。</td>
  </tr>
</table>


<table>
  <tr>
    <th>void</th>
    <th>write(byte[] b)<br><br>将 b.length<br><br>个字节从指定 byte 数组写⼊到此⽂件，并从当前 ⽂件指针开始。</th>
  </tr>
  <tr>
    <td>void</td>
    <td>write(byte[] b, int off, int len)<br><br>将 len<br><br>个字节从指定 byte 数组写⼊到此⽂件，并从偏移 量<br><br>off 处开始。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>write(int b)<br><br>向此⽂件写⼊指定的字节。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>writeBolean(boolean v)<br><br>按单字节值将 boolean<br><br>写⼊该⽂件。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>writeByte(int v)<br><br>按单字节值将 byte<br><br>写⼊该⽂件。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>writeBytes( String s)<br><br>按字节序列将该字符串写⼊该⽂件。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>writeChar(int v)<br><br>按双字节值将 char<br><br>写⼊该⽂件，先写⾼字节。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>writeChars( String s)<br><br>按字符序列将⼀个字符串写⼊该⽂件。</td>
  </tr>
</table>


<table>
  <tr>
    <th>void</th>
    <th>writeDouble(double v)<br><br>使⽤ Double<br><br>类中的 doubleToLongBits ⽅法将双精度参数转换为⼀个 long ，然后按⼋字节数量将该 long 值写⼊该⽂件，先定⾼字节。<br><br></th>
  </tr>
  <tr>
    <td>void</td>
    <td>writeFloat (float v)<br><br>使⽤ Float<br><br>类中的 floatToIntBits ⽅法将浮点参数转换为⼀个 int ，然后按四字节数量将该 int 值写⼊该⽂件，先写⾼字节。<br><br></td>
  </tr>
  <tr>
    <td>void</td>
    <td>writeInt (int v)<br><br>按四个字节将 int<br><br>写⼊该⽂件，先写⾼字节。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>writeLong(long v)<br><br>按⼋个字节将 long<br><br>写⼊该⽂件，先写⾼字节。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>writeShort (int v)<br><br>按两个字节将 short<br><br>写⼊该⽂件，先写⾼字节。</td>
  </tr>
  <tr>
    <td>void</td>
    <td>writeUTF( String str)<br><br>使⽤<br><br>编码以与机器⽆关的⽅式将⼀个字符串写⼊该⽂ 件。<br><br>modified UTF-8</td>
  </tr>
</table>


<table>
  <tr>
    <th>从类 java.lang.Object 继承的⽅法</th>
  </tr>
  <tr>
    <td>clone, equals, finalize, getClas, hashCode, notify, notifyAl , , , ,<br><br></td>
  </tr>
</table>


toString wait wait wait

<table>
  <tr>
    <th>构造⽅法详细信息</th>
  </tr>
</table>


# RandomAcesFile

public RandomAcesFile( name, mode) throws

String String FileNotFoundE xception

Fil eDescriptor

创建从中读取和向其中写⼊（可选）的随机访问⽂件流，该⽂件具有指定名称。将创建⼀个新的

对象来表示到⽂件的连接。 mode 参数指定⽤以打开⽂件的访问模式。允许的值及 其含意如 RandomAccessFile(File,String) 构造⽅法所指定的那样。 如果存在安全管理器， 则使⽤ name 作为其参数调⽤其 checkRead ⽅法，以查看是否允许对该⽂件进⾏读取访问。如 果该模式允许写⼊，那么还使⽤ name 作为安全管理器的参数来调⽤其 checkWrite ⽅法，以查 看是否允许对该⽂件进⾏写⼊访问。 参数： name - 取决于系统的⽂件名 mode - 此访问 mode 抛 出： IlegalArgumentException - 如果此模式参数与 "r" 、 "rw" 、 "rws" 或 "rwd" 的其中 ⼀个不相等 FileNotFoundException - 如果该模式为 "r" ，但给定的字符串表示⼀个现有的常规⽂ 件，或者该模式以 "rw" 开头，但给定的字符串不表示⼀个现有的可写常规⽂件，⽽且⽆法创建具 有该名称的新常规⽂件，或者在打开或创建该⽂件时发⽣⼀些其他错误 SecurityException - 如果存在 安全管理器，并且其 checkRead ⽅法拒绝对该⽂件的读取访问，或者该模式为 "rw"，并且该安全 管理器的 checkWrite ⽅法拒绝对该⽂件的写⼊访问另请参⻅： SecurityException ,

SecurityManager.checkRead(java.lang.String) , SecurityManager.checkWrite(java.lang.String)

# RandomAcesFile

public RandomAcesFile( file, mode) throws

File String FileNotFoundExcep tion

File FileDescriptor

创建从中读取和向其中写⼊（可选）的随机访问⽂件流，该⽂件由 参数指定。将创建⼀个新 的 对象来表示此⽂件的连接。 mode 参数指定⽤以打开⽂件的访问模式。允 许的值及其含意为： 值含意 "r" 以只读⽅式打开。调⽤结果对象的任何 write ⽅法都将导致抛 出 IOException 。 "rw" 打开以便读取和写⼊。如果该⽂件尚不存在，则尝试创建该⽂件。

"rws" 打开以便读取和写⼊，对于 "rw" ，还要求对⽂件的内容或元数据的每个更新都同步写⼊ 到底层存储设备。 "rwd" 打开以便读取和写⼊，对于 "rw" ，还要求对⽂件内容的每个更新都 同步写⼊到底层存储设备。 "rws" 和 "rwd" 模式的⼯作⽅式极其类似 类的

FileChannel force(boolean)

⽅法，分别传递 true 和 false 参数，除⾮它们始终应⽤于每个 I/O 操 作，并因此通常更为⾼效。如果该⽂件位于本地存储设备上，那么当返回此类的⼀个⽅法的调⽤时，可 以保证由该调⽤对此⽂件所做的所有更改均被写⼊该设备。这对确保在系统崩溃时不会丢失重要信息特 别有⽤。如果该⽂件不在本地设备上，则⽆法提供这样的保证。 "rwd" 模式可⽤于减少执⾏的 I/O 操作数量。使⽤ "rwd" 仅要求更新要写⼊存储的⽂件的内容；使⽤ "rws" 要求更新要写⼊的⽂ 件内容及其元数据，这通常要求⾄少⼀个以上的低级别 I/O 操作。 如果存在安全管理器，则使⽤

file 参数的路径名作为其参数调⽤它的 checkRead ⽅法，以查看是否允许对该⽂件进⾏读取 访问。如果该模式允许写⼊，那么还使⽤该路径参数调⽤该安全管理器的 checkWrite ⽅法，以查 看是否允许对该⽂件进⾏写⼊访问。 参数： file - 该⽂件对象 mode - 访问模式，如上所述 抛 出： IlegalArgumentException - 如果此模式参数与 "r" 、 "rw" 、 "rws" 或 "rwd" 的其中 ⼀个不相等 FileNotFoundException - 如果该模式为 "r" ，但给定的⽂件对象不表示⼀个现有的常规 ⽂件，或者该模式以 "rw" 开头，但给定的⽂件对象不表示⼀个现有的可写常规⽂件，⽽且⽆法创 建具有该名称的新常规⽂件，或者在打开或创建该⽂件时发⽣⼀些其他错误 SecurityException - 如果 存在安全管理器，并且其 checkRead ⽅法拒绝对该⽂件的读取访问，或者该模式为 "rw"，并且该 安全管理器的 checkWrite ⽅法拒绝对该⽂件的写⼊访问另请参⻅：

SecurityManager.checkRead(java.lang.String) , SecurityManager.checkWrite(java.lang.String) , FileChannel.force(boolean)

<table>
  <tr>
    <th>⽅法详细信息</th>
  </tr>
</table>


# getFD

public final getFD() throws 返回与此流关联的不透明⽂件描述符对象。

FileDescriptor IOException

返回：与此流关联的不透明⽂件描述符对象。 抛出： IOException - 如果发⽣ I/O 错误。另请参⻅： FileDescriptor

# getChanel

public final getChanel()

FileChanel

FileChannel

返回与此⽂件关联的唯⼀ 对象。 返回通道的

java.nio.channels.FileChannel#position() position 将始终等于 getFilePointer ⽅法返回的此对象的⽂件指针偏移量。显式或者通过读取或写⼊字节来更改此对象的⽂件指针偏移量将 更改通道的位置，反之亦然。通过此对象更改此⽂件的⻓度将更改通过⽂件通道看到的⻓度，反之亦 然。

返回：与此⽂件关联的⽂件通道从以下版本开始： 1.4

# read

public int read() throws 从此⽂件中读取⼀个数据字节。以整数形式返回此字节，范围在 0 到 25 ( 0x00-0x0ff )。如果尚 ⽆输⼊可⽤，将阻塞此⽅法。 尽管 RandomAccessFile 不是 InputStream 的⼦类，但此⽅法 的⾏为与 InputStream 的 InputStream.read() ⽅法完全⼀样。

IOException

返回：下⼀个数据字节，如果已到达⽂件的末尾，则返回 -1 。 抛出： IOException - 如果发⽣ I/O 错误。如果已到达⽂件的末尾，则不抛出此异常。

# read

public int read(byte[] b, int of, int len) throws 将最多 len 个数据字节从此⽂件读⼊ byte 数组。在⾄少⼀个输⼊字节可⽤前，此⽅法⼀直阻塞。 尽管 RandomAccessFile 不是 InputStream 的⼦类，但此⽅法的⾏为与 InputStream 的 InputStream.read(byte[], int, int) ⽅法完全⼀样。

IOException

参数： b - 读⼊数据的缓冲区。 off - 写⼊数据的数组 b 中的初始偏移量。 len - 读取的最 多字节数。 返回：读⼊缓冲区的总字节数，如果由于已到达⽂件的末尾⽽不再有数据，则返回 -1 。 抛出： IOException - 如果由于⽂件结束之外的某种原因不能读取第⼀个字节，或者随机访问⽂件已关 闭，或者发⽣其他 I/O 错误。 NulPointerException - 如果 b 为 null 。 IndexOutOfBoundsException - 如果 off 为负， len 为负，或者 len ⼤于 b.length off

# read

public int read(byte[] b) throws

将最多 b.length 个数据字节从此⽂件读⼊ byte 数组。在⾄少⼀个输⼊字节可⽤前，此⽅法⼀直 阻塞。 尽管 RandomAccessFile 不是 InputStream 的⼦类，但此⽅法的⾏为与

InputStream 的 InputStream.read(byte[]) ⽅法完全⼀样。

参数： b - 将数据读⼊的缓冲区。 返回：读⼊缓冲区的总字节数，如果由于已到达此⽂件的末尾⽽ 不再有数据，则返回 -1 。 抛出： IOException - 如果由于⽂件结束之外的某种原因不能读取第⼀个 字节，或者随机访问⽂件已关闭，或者发⽣其他 I/O 错误。 NulPointerException - 如果 b 为

null 。

# readFuly

public final void readFuly(byte[] b) throws 将 b.length 个字节从此⽂件读⼊ byte 数组，并从当前⽂件指针开始。在读取到请求数量的字节 之前，此⽅法将从该⽂件重复读取。在读取了请求数量的字节、检测到流的末尾或者抛出异常前，此⽅ 法⼀直阻塞。 指定者：接⼝ DataInput 中的 readFuly 参数： b - 将数据读⼊的缓冲区。 抛出： EOFException - 如果在读取所有字节之前此⽂件已到达末 尾。 IOException - 如果发⽣ I/O 错误。

IOException

# readFuly

public final void readFuly(byte[] b, int of, int len) throws 将正好 len 个字节从此⽂件读⼊ byte 数组，并从当前⽂件指针开始。在读取到请求数量的字节之 前，此⽅法将从该⽂件重复读取。在读取了请求数量的字节、检测到流的末尾或者抛出异常前，此⽅法 ⼀直阻塞。 指定者：接⼝ DataInput 中的 readFuly 参数： b - 读⼊数据的缓冲区。 off - 数据的初始偏移量。 len - 要读取的字节数。 抛出： EOFException - 如果在读取所有字节之前此⽂件已到达末尾。 IOException - 如果发⽣ I/O 错误。

IOException

# skipBytes

public int skipBytes(int n) throws 尝试跳过输⼊的 n 个字节以丢弃跳过的字节。 此⽅法可能跳过⼀些较少数量的字节（可能包括 零）。这可能由任意数量的条件引起；在跳过 n 个字节之前已到达⽂件的末尾只是其中的⼀种可 能。此⽅法从不抛出 EOFException 。返回跳过的实际字节数。如果 n 为负数，则不跳过任何 字节。

IOException

指定者：接⼝ DataInput 中的 skipBytes 参数： n - 要跳过的字节数。 返回：跳过的实际字节数。 抛出： IOException - 如果发⽣ I/O 错误。

# write

public void write(int b) throws 向此⽂件写⼊指定的字节。从当前⽂件指针开始写⼊。 指定者：接⼝ DataOutput 中的 write 参数： b - 要写⼊的 byte 。 抛出： IOException - 如果发⽣ I/O 错误。

IOException

# write

public void write(byte[] b) throws 将 b.length 个字节从指定 byte 数组写⼊到此⽂件，并从当前⽂件指针开始。 指定者：接⼝ DataOutput 中的 write 参数： b - 数据。 抛出： IOException - 如果发⽣ I/O 错误。

IOException

# write

public void write(byte[] b, int of, int len) throws 将 len 个字节从指定 byte 数组写⼊到此⽂件，并从偏移量 off 处开始。 指定者：接⼝ DataOutput 中的 write 参数： b - 数据。 off - 数据的初始偏移量。 len - 要写⼊的字节数。 抛出： IOException - 如 果发⽣ I/O 错误。

IOException

# getFilePointer

public long getFilePointer() throws 返回此⽂件中的当前偏移量。

IOException

返回：到此⽂件开头的偏移量（以字节为单位），在该位置发⽣下⼀个读取或写⼊操作。 抛出： IOException - 如果发⽣ I/O 错误。

# sek

public void sek(long pos) throws

设置到此⽂件开头测量到的⽂件指针偏移量，在该位置发⽣下⼀个读取或写⼊操作。偏移量的设置可能 会超出⽂件末尾。偏移量的设置超出⽂件末尾不会改变⽂件的⻓度。只有在偏移量的设置超出⽂件末尾 的情况下对⽂件进⾏写⼊才会更改其⻓度。

参数： pos - 从⽂件开头以字节为单位测量的偏移量位置，在该位置设置⽂件指针。 抛出： IOException - 如果 pos ⼩于 0 或者发⽣ I/O 错误。

# length

public long length() throws 返回此⽂件的⻓度。

IOException

返回：按字节测量的此⽂件的⻓度。 抛出： IOException - 如果发⽣ I/O 错误。

# setLength

public void setLength(long newLength) throws 设置此⽂件的⻓度。 如果 length ⽅法返回的⽂件的现有⻓度⼤于 newLength 参数，则该⽂件 将被截短。在此情况下，如果 getFilePointer ⽅法返回的⽂件偏移量⼤于 newLength ，那 么在返回此⽅法后，该偏移量将等于 newLength 。 如果 length ⽅法返回的⽂件的现有⻓度⼩ 于 newLength 参数，则该⽂件将被扩展。在此情况下，未定义⽂件扩展部分的内容。

IOException

参数： newLength - ⽂件的所需⻓度 抛出： IOException - 如果发⽣ I/O 错误从以下版本开始： 1.2

# close

public void close() throws 关闭此随机访问⽂件流并释放与该流关联的所有系统资源。关闭的随机访问⽂件不能执⾏输⼊或输出操 作，⽽且不能重新打开。 如果此⽂件具有⼀个关联的通道，那么该通道也会被关闭。 指定者：接⼝ Closeable 中的 close 抛出： IOException - 如果发⽣ I/O 错误。

IOException

# readBolean

public final bolean readBolean() throws

IOException

从此⽂件读取⼀个 boolean 。此⽅法从该⽂件的当前⽂件指针开始读取单个字节。值 0 表示

false 。其他任何值表示 true 。在读取了该字节、检测到流的末尾或者抛出异常前，此⽅法⼀ 直阻塞。 指定者：接⼝ DataInput 中的 readBolean 返回：读取的 boolean 值。 抛出： EOFException - 如果此⽂件已到达末尾。 IOException - 如果 发⽣ I/O 错误。

# readByte

public final byte readByte() throws 从此⽂件读取⼀个有符号的⼋位值。此⽅法从该⽂件的当前⽂件指针开始读取⼀个字节。如果读取的字 节为 b ，其中 0 <= b <= 255 ，则结果将是： (byte)(b) 在读取了该字节、检测到流的末尾或者 抛出异常前，此⽅法⼀直阻塞。 指定者：接⼝ DataInput 中的 readByte 返回：以有符号的⼋位 byte 形式返回此⽂件的下⼀个字节。 抛出： EOFException - 如果此⽂件已 到达末尾。 IOException - 如果发⽣ I/O 错误。

IOException

# readUnsignedByte

public final int readUnsignedByte() throws 从此⽂件读取⼀个⽆符号的⼋位数。此⽅法从此⽂件的当前⽂件指针开始读取⼀个字节，并返回该字 节。 在读取了该字节、检测到流的末尾或者抛出异常前，此⽅法⼀直阻塞。 指定者：接⼝ DataInput 中的 readUnsignedByte 返回：此⽂件的下⼀个字节，解释为⼀个⽆符号的⼋位数。 抛出： EOFException - 如果此⽂件已到达 末尾。 IOException - 如果发⽣ I/O 错误。

IOException

# readShort

public final short readShort() throws 从此⽂件读取⼀个有符号的 16 位数。此⽅法从此⽂件的当前⽂件指针开始读取两个字节。如果按顺序 读取的两个字节为 b1 和 b2 ，其中两个值都在 0 和 255 之间（包含），则此结果等于： (short)(b1 < 8) | b2) 在读取了这两个字节、检测到流的末尾或者抛出异常前，此⽅法⼀直阻塞。 指定者：接⼝ DataInput 中的 readShort 返回：此⽂件的下两个字节，解释为⼀个有符号的 16 位数。 抛出： EOFException - 如果在读取两个 字节之前此⽂件已到达末尾。 IOException - 如果发⽣ I/O 错误。

IOException

# readUnsignedShort

public final int readUnsignedShort() throws 从此⽂件读取⼀个⽆符号的 16 位数。此⽅法从该⽂件的当前⽂件指针开始读取两个字节。如果按顺序 读取的字节为 b1 和 b2 ，其中 0 <= b1, b2 <= 255 ，则结果将等于： (b1 < 8) | b2 在读 取了这两个字节、检测到流的末尾或者抛出异常前，此⽅法⼀直阻塞。 指定者：接⼝ DataInput 中的 readUnsignedShort 返回：此⽂件的下两个字节，解释为⼀个⽆符号的 16 位整数。 抛出： EOFException - 如果在读取两 个字节之前此⽂件已到达末尾。 IOException - 如果发⽣ I/O 错误。

IOException

# readChar

public final char readChar() throws 从此⽂件读取⼀个字符。此⽅法从该⽂件的当前⽂件指针开始读取两个字节。如果按顺序读取的字节为 b1 和 b2 ，其中 0 <= b1, b2 <= 255 ，则结果将等于： (char)(b1 < 8) | b2) 在读取了这 两个字节、检测到流的末尾或者抛出异常前，此⽅法⼀直阻塞。 指定者：接⼝ DataInput 中的 readChar 返回：此⽂件的下两个字节，解释为 char 。 抛出： EOFException - 如果在读取两个字节之前此⽂ 件已到达末尾。 IOException - 如果发⽣ I/O 错误。

IOException

# readInt

public final int readInt() throws 从此⽂件读取⼀个有符号的 32 位整数。此⽅法从该⽂件的当前⽂件指针开始读取 4 个字节。如果按顺 序读取的字节为 b1 、 b2 、 b3 和 b4 ，其中 0 <= b1, b2, b3, b4 <= 255 ，则结果 将等于： (b1 < 24) | (b2 < 16) + (b3 < 8) + b4 在读取了这四个字节、检测到流的末尾或者抛出异 常前，此⽅法⼀直阻塞。 指定者：接⼝ DataInput 中的 readInt 返回：此⽂件的下四个字节，解释为⼀个 int 。 抛出： EOFException - 如果在读取四个字节之前 此⽂件已到达末尾。 IOException - 如果发⽣ I/O 错误。

IOException

# readLong

public final long readLong() throws

IOException

从此⽂件读取⼀个有符号的 64 位整数。此⽅法从该⽂件的当前⽂件指针开始读取⼋个字节。如果按顺 序读取的字节为 b1 、 b2 、 b3 、 b4 、 b5 、 b6 、 b7 和 b8 ，其中： 0 <= b1, b2, b3, b4, b5, b6, b7, b8 <=25, 则结果将等于： (long)b1 < 56) +(long)b2 < 48)+(long)b3 <

40) +(long)b4 < 32) +(long)b5 < 24) +(long)b6 < 16) +(long)b7 < 8) + b8 在读取了 这⼋个字节、检测到流的末尾或者抛出异常前，此⽅法⼀直阻塞。 指定者：接⼝ DataInput 中的 readLong 返回：此⽂件的下⼋个字节，解释为⼀个 long 。 抛出： EOFException - 如果在读取⼋个字节之前 此⽂件已到达末尾。 IOException - 如果发⽣ I/O 错误。

# readFloat

public final float readFloat() throws 从此⽂件读取⼀个 float 。此⽅法从当前⽂件指针开始读取⼀个 int 值，类似于使⽤

IOException

readInt ⽅法，然后使⽤ Float 类中的 intBitsToFloat ⽅法将该 int 转换为⼀个 float 。 在读取了这四个字节、检测到流的末尾或者抛出异常前，此⽅法⼀直阻塞。

指定者：接⼝ DataInput 中的 readFloat 返回：此⽂件的下四个字节，解释为⼀个 float 。 抛出： EOFException - 如果在读取四个字节之 前此⽂件已到达末尾。 IOException - 如果发⽣ I/O 错误。另请参⻅： readInt() ,

Float.intBitsToFloat(int)

# readDouble

public final double readDouble() throws 从此⽂件读取⼀个 double 。此⽅法从当前⽂件指针开始读取⼀个 long 值，类似于使⽤

IOException

readLong ⽅法，然后使⽤ Double 类中的 longBitsToDouble ⽅法将该 long 转换为 ⼀个 double 。 在读取了这⼋个字节、检测到流的末尾或者抛出异常前，此⽅法⼀直阻塞。 指定者：接⼝ DataInput 中的 readDouble 返回：此⽂件的下⼋个字节，解释为⼀个 double 。 抛出： EOFException - 如果在读取⼋个字节之 前此⽂件已到达末尾。 IOException - 如果发⽣ I/O 错误。另请参⻅： readLong() ,

Double.longBitsToDouble(long)

# readLine

public final readLine() throws

String IOException

从此⽂件读取⽂本的下⼀⾏。此⽅法可以从该⽂件的当前⽂件指针处成功地读取字节，直到到达⾏结束 符或⽂件的末尾。每个字节都转换为⼀个字符，⽅法是采⽤该字符的低⼋位字节值，并将该字符的⾼⼋ 位设置为零。因此，此⽅法不⽀持完整的 Unicode 字符集。 ⽂本⾏由回⻋符 ( '\r' ) 和⼀个换⾏符 ( '\n' ) 结束，回⻋符后⾯紧跟⼀个换⾏符，或者是⽂件的末尾。不使⽤⾏结束符，并且在返回的字 符串中不包括结束符。 在读取了⼀个换⾏符、读取了⼀个回⻋符和它后⾯的字节（查看是否为⼀个新 ⾏），到达⽂件的末尾或者抛出异常之前，此⽅法⼀直阻塞。 指定者：接⼝ DataInput 中的 readLine 返回：此⽂件⽂本的下⼀⾏，如果连⼀个字节也没有读取就已到达⽂件的末尾，则返回 nul。 抛出： IOException - 如果发⽣ I/O 错误。

# readUTF

public final readUTF() throws 从此⽂件读取⼀个字符串。该字符串已使⽤ 格式进⾏编码。 从当前⽂件指针开始读取前 两个字节，类似于使⽤ readUnsignedShort 。此值给出已编码字符串中随后的字节数，⽽不是结 果字符串的⻓度。随后的字节然后解释为 UTF-8 修改版格式的字节编码字符，并转换为字符。 在读取 了所有字节、检测到流的末尾或者抛出异常前，此⽅法⼀直阻塞。 指定者：接⼝ DataInput 中的 readUTF 返回：⼀个 Unicode 字符串。 抛出： EOFException - 如果在读取所有字节之前此⽂件已到达末尾。 IOException - 如果发⽣ I/O 错误。 UTFDataFormatException - 如果这些字节不表示 Unicode 字符串 的有效 UTF-8 修改版编码。另请参⻅： readUnsignedShort()

String IOException UTF-8 修改版

# writeBolean

public final void writeBolean(bolean v) throws 按单字节值将 boolean 写⼊该⽂件。值 true 写出为值 (byte)1 ；值 false 写出为值

IOException

(byte)0 。写⼊从⽂件指针的当前位置开始。 指定者：接⼝ DataOutput 中的 writeBolean 参数： v - 要写⼊的 boolean 值。 抛出： IOException - 如果发⽣ I/O 错误。

# writeByte

public final void writeByte(int v) throws 按单字节值将 byte 写⼊该⽂件。写⼊从⽂件指针的当前位置开始。 指定者：接⼝ DataOutput 中的 writeByte 参数： v - 要写⼊的 byte 值。 抛出： IOException - 如果发⽣ I/O 错误。

IOException

# writeShort

public final void writeShort(int v) throws 按两个字节将 short 写⼊该⽂件，先写⾼字节。写⼊从⽂件指针的当前位置开始。 指定者：接⼝ DataOutput 中的 writeShort 参数： v - 要写⼊的 short 。 抛出： IOException - 如果发⽣ I/O 错误。

IOException

# writeChar

public final void writeChar(int v) throws 按双字节值将 char 写⼊该⽂件，先写⾼字节。写⼊从⽂件指针的当前位置开始。 指定者：接⼝ DataOutput 中的 writeChar 参数： v - 要写⼊的 char 值。 抛出： IOException - 如果发⽣ I/O 错误。

IOException

# writeInt

public final void writeInt(int v) throws 按四个字节将 int 写⼊该⽂件，先写⾼字节。写⼊从⽂件指针的当前位置开始。 指定者：接⼝ DataOutput 中的 writeInt 参数： v - 要写⼊的 int 。 抛出： IOException - 如果发⽣ I/O 错误。

IOException

# writeLong

public final void writeLong(long v) throws 按⼋个字节将 long 写⼊该⽂件，先写⾼字节。写⼊从⽂件指针的当前位置开始。 指定者：接⼝ DataOutput 中的 writeLong 参数： v - 要写⼊的 long 。 抛出： IOException - 如果发⽣ I/O 错误。

IOException

# writeFloat

public final void writeFloat(float v) throws 使⽤ Float 类中的 floatToIntBits ⽅法将浮点参数转换为⼀个 int ，然后按四字节数量 将该 int 值写⼊该⽂件，先写⾼字节。写⼊从⽂件指针的当前位置开始。 指定者：接⼝ DataOutput 中的 writeFloat 参数： v - 要写⼊的 float 值。 抛出： IOException - 如果发⽣ I/O 错误。另请参⻅：

IOException

Float.floatToIntBits(float)

# writeDouble

public final void writeDouble(double v) throws 使⽤ Double 类中的 doubleToLongBits ⽅法将双精度参数转换为⼀个 long ，然后按⼋字 节数量将该 long 值写⼊该⽂件，先定⾼字节。写⼊从⽂件指针的当前位置开始。 指定者：接⼝ DataOutput 中的 writeDouble 参数： v - 要写⼊的 double 值。 抛出： IOException - 如果发⽣ I/O 错误。另请参⻅：

IOException

Double.doubleToLongBits(double)

# writeBytes

public final void writeBytes( s) throws 按字节序列将该字符串写⼊该⽂件。该字符串中的每个字符均按顺序写出，并丢弃其⾼⼋位。写⼊从⽂ 件指针的当前位置开始。 指定者：接⼝ DataOutput 中的 writeBytes 参数： s - 要写⼊的字节的字符串。 抛出： IOException - 如果发⽣ I/O 错误。

String IOException

# writeChars

public final void writeChars( s) throws 按字符序列将⼀个字符串写⼊该⽂件。每个字符均写⼊数据输出流，类似于使⽤ writeChar ⽅ 法。写⼊从⽂件指针的当前位置开始。 指定者：接⼝ DataOutput 中的 writeChars 参数： s - 要写⼊的 boolean 值。 抛出： IOException - 如果发⽣ I/O 错误。另请参⻅：

String IOException

writeChar(int)

# writeUTF

public final void writeUTF( str) throws

String IOException

使⽤ 编码以与机器⽆关的⽅式将⼀个字符串写⼊该⽂件。 ⾸先，把两个字节从⽂件的 当前⽂件指针写⼊到此⽂件，类似于使⽤ writeShort ⽅法并给定要跟随的字节数。此值是实际写 出的字节数，⽽不是该字符串的⻓度。在该⻓度之后，按顺序输出该字符串的每个字符，并对每个字符 使⽤ UTF-8 修改版编码。 指定者：接⼝ DataOutput 中的 writeUTF 参数： str - 要写⼊的字符串。 抛出： IOException - 如果发⽣ I/O 错误。

modified UTF-8

