测试说明 写2G⽂件，分批次写⼊，每批次写⼊128MB； 分别在Win7系统（3G内存，双核，32位，T系列处理器）和MacOS系统（8G内存，四核，64位，i7

系列处理器）下运⾏测试。理论上跟硬盘类型和配置也有关系，这⾥不再贴出了。

测试代码 ?

packagerwbigfile; importjava.io.ByteArrayInputStream

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9


;

importjava.io.File; importjava.io.IOException; importjava.io.RandomAccessFile; importjava.lang.reflect.Method; importjava.nio.ByteBuffer; importjava.nio.MappedByteBuffer; importjava.nio.channels.Channels; importjava.nio.channels.FileChanne

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


l; importjava.nio.channels.FileChannel

.MapMode;

importjava.nio.channels.ReadableByt eChannel;

19

importjava.security.AccessControll er;

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


importjava.security.PrivilegedActi

on; importutil.StopWatch; /**

- * NIO写⼤⽂件⽐较

- * @author Will

*

- */


29

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


publicclassWriteBigFileComparison {

// data chunk be written per time

privatestaticfinalintDATA_CHUNK

= 128* 1024* 1024; // total data size is 2G privatestaticfinallongLEN = 2L

39

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


* 1024* 1024* 1024L;

publicstaticvoidwriteWithFileCh annel() throwsIOException {

File file = newFile("e:/test/fc.dat"); if(file.exists()) { file.delete();

49

} RandomAccessFile raf =

- 0
- 1
- 2
- 3
- 4
- 5


newRandomAccessFile(file, "rw");

FileChannel fileChannel = raf.getChannel();

byte[] data = null;

56

longlen = LEN; ByteBuffer buf =

- 7
- 8


ByteBuffer.allocate(DATA_CHUNK);

59

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


intdataChunk = DATA_CHUNK / (1024* 1024);

while(len >= DATA_CHUNK) {

System.out.println("wri te a data chunk: "+ dataChunk + "MB");

buf.clear(); // clear for re-write

69

data = newbyte[DATA_CHUNK];

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


for(inti = 0; i < DATA_CHUNK; i++) {

buf.put(data[i]);

} data = null; buf.flip(); // switches

a Buffer from writing mode to reading mode

79

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


fileChannel.write(buf) ;

fileChannel.force(true );

len -= DATA_CHUNK;

} if(len > 0) {

89

System.out.println("wri te rest data chunk: "+ len + "B");

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9 0


buf = ByteBuffer.allocateDirect((int) len);

data = newbyte[(int) len];

for(inti = 0; i < len; i++) {

buf.put(data[i]);

} buf.flip(); // switches

- 11

- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 09
- 10 1


- 12
- 13
- 14


a Buffer from writing mode to reading mode, position to 0, limit not changed

fileChannel.write(buf) ;

fileChannel.force(true );

data = null; }

fileChannel.close(); raf.close();

- 15
- 16
- 17
- 18
- 19 0


} /**

* write big file with MappedByteBuffer

- 11


- * @throws IOException

- */


- 2
- 3
- 4
- 5
- 6
- 7
- 8


publicstaticvoidwriteWithMapped ByteBuffer() throwsIOException {

File file = newFile("e:/test/mb.dat"); if(file.exists()) { file.delete();

29

0 11

} RandomAccessFile raf =

- 2
- 3
- 4
- 5
- 6
- 7
- 8


newRandomAccessFile(file, "rw"); FileChannel fileChannel =

raf.getChannel(); intpos = 0; MappedByteBuffer mbb =

39

null;

0 11

byte[] data = null; longlen = LEN; intdataChunk = DATA_CHUNK /

- 2
- 3
- 4
- 5
- 6
- 7
- 8


(1024* 1024); while(len >= DATA_CHUNK) {

System.out.println("wri te a data chunk: "+ dataChunk + "MB");

49

mbb = fileChannel.map(MapMode.READ_WRITE, pos, DATA_CHUNK);

0 11

- 2
- 3
- 4
- 5
- 6
- 7
- 8


data =

newbyte[DATA_CHUNK]; mbb.put(data); data = null; len -= DATA_CHUNK; pos += DATA_CHUNK;

59

0 11

} if(len > 0) {

- 2
- 3
- 4
- 5
- 6
- 7
- 8


System.out.println("wri te rest data chunk: "+ len + "B");

mbb = fileChannel.map(MapMode.READ_ONLY, pos, len);

data = newbyte[(int) len];

69

0 11 172

mbb.put(data);

} data = null; unmap(mbb); // release

- 3
- 4
- 5
- 6
- 7
- 8


MappedByteBuffer

fileChannel.close(); }

79

0 11

publicstaticvoidwriteWithTransf erTo() throwsIOException {

- 2
- 3
- 4
- 5
- 6
- 7
- 8


File file = newFile("e:/test/transfer.dat"); if(file.exists()) {

file.delete(); }

89

0 11

RandomAccessFile raf = newRandomAccessFile(file, "rw");

- 2
- 3
- 4
- 5
- 6
- 7
- 8


FileChannel toFileChannel = raf.getChannel();

longlen = LEN; byte[] data = null; ByteArrayInputStream bais =

19

null;

- 0
- 1


ReadableByteChannel

fromByteChannel = null; longposition = 0; intdataChunk = DATA_CHUNK /

22

- 3
- 4
- 5
- 6
- 7
- 8
- 09


(1024* 1024); while(len >= DATA_CHUNK) {

System.out.println("wri te a data chunk: "+ dataChunk + "MB");

- 0
- 1


- 22

- 3
- 4
- 5
- 6
- 7
- 8


- 19
- 20
- 21 2


- 23
- 24
- 25
- 26


data = newbyte[DATA_CHUNK]; bais = newByteArrayInputStream(data);

fromByteChannel = Channels.newChannel(bais);

longcount = DATA_CHUNK;

toFileChannel.transferF rom(fromByteChannel, position, count);

data = null;

position += DATA_CHUNK;

len -= DATA_CHUNK; }

if(len > 0) {

System.out.println("wri te rest data chunk: "+ len + "B");

data = newbyte[(int) len];

bais = newByteArrayInputStream(data);

fromByteChannel = Channels.newChannel(bais);

longcount = len; toFileChannel.transferF

rom(fromByteChannel, position, count);

}

data = null; toFileChannel.close(); fromByteChannel.close();

}

/**

- * 在MappedByteBuffer释放后再对它 进⾏读操作的话就会引发jvm crash，在并发情 况下很容易发⽣

- * 正在释放时另⼀个线程正开始读取， 于是crash就发⽣了。所以为了系统稳定性释放 前⼀般需要检
- * 查是否还有线程在读或写

- * @param mappedByteBuffer

- */


publicstaticvoidunmap(finalMapp edByteBuffer mappedByteBuffer) {

try{

if(mappedByteBuffer == null) {

return; }

mappedByteBuffer.force ();

AccessController.doPriv ileged(newPrivilegedAction<Object>() {

@Override @SuppressWarnings("

restriction")

publicObject run() {

try{

Method

getCleanerMethod = mappedByteBuffer.getClass()

.ge tMethod("cleaner", newClass[0]);

getCleanerM ethod.setAccessible(true);

sun.misc.Cl eaner cleaner =

(su n.misc.Cleaner) getCleanerMethod

.invoke(mappedByteBuffer, newObject[0]);

cleaner.cle an();

} catch(Exception e) {

e.printStac kTrace();

} System.out.prin

tln("clean MappedByteBuffer completed");

returnnull; }

}); } catch(Exception e) {

e.printStackTrace(); }

} publicstaticvoidmain(String[]

args) throwsIOException {

StopWatch sw = newStopWatch();

sw.startWithTaskName("write with file channel's write(ByteBuffer)");

writeWithFileChannel(); sw.stopAndPrint();

sw.startWithTaskName("write

with file channel's transferTo"); writeWithTransferTo(); sw.stopAndPrint();

sw.startWithTaskName("write with MappedByteBuffer");

writeWithMappedByteBuffer( );

sw.stopAndPrint(); }

}

测试结果（Y轴是耗时秒数）

- 1.
- 2.
- 3.
- 4.


显然writeWithMapedByteBufer⽅式性能最好，且在硬件配置较⾼情况下优势越加明显 在硬件配置较低情况下，writeWithTransferTo⽐writeWithFileChanel性能稍好 在硬件配置较⾼情况下，writeWithTransferTo和writeWithFileChanel的性能基本持平 此外，注意writeWithMapedByteBufer⽅式除了占⽤JVM堆内存外，还要占⽤额外的native内存

（Direct Byte Bufer内存）

内存映射⽂件使⽤经验

- 1.
- 2.
- 3.
- 4.
- 5.


MapedByteBufer需要占⽤“双倍”的内存（对象JVM堆内存和Direct Byte Bufer内存），可以通 过-X MaxDirectMemorySize参数设置后者最⼤⼤⼩

不要频繁调⽤MapedByteBufer的force()⽅法，因为这个⽅法会强制OS刷新内存中的数据到磁 盘，从⽽只能获得些微的性能提升（相⽐IO⽅式），可以⽤后⾯的代码实例进⾏定时、定量刷新

如果突然断电或者服务器突然Down，内存映射⽂件数据可能还没有写⼊磁盘，这时就会丢失⼀些 数据。为了降低这种⻛险，避免⽤MapedByteBufer写超⼤⽂件，可以把⼤⽂件分割成⼏个⼩⽂ 件，但不能太⼩（否则将失去性能优势）

ByteBufer的rewind()⽅法将position属性设回为0，因此可以重新读取bufer中的数据；limit属性 保持不变，因此可读取的字节数不变

ByteBufer的flip()⽅法将⼀个Bufer由写模式切换到读模式

6.

ByteBufer的clear()和compact()可以在我们读完ByteBufer中的数据后重新切回写模式。不同的 是clear()会将 position设置为0，limit设为capacity，换句话说Bufer被清空了，但Bufer内的数据并没有被清 空。如果Bufer中还 有未被读取的数据，那调⽤clear()之后，这些数据会被“遗忘”，再写⼊就会覆盖这些未读数据。⽽ 调⽤compcat()之后，这些未被读取的数据仍 然可以保留，因为它将所有还未被读取的数据拷⻉到Bufer的左端，然后设置position为紧随未读 数据之后，limit被设置为 capacity，未读数据不会被覆盖

定时、定量刷新内存映射⽂件到磁盘

?

importjava.io.File; importjava.io.IOException; importjava.io.RandomAccessFile; importjava.nio.MappedByteBuffer; importjava.nio.channels.FileChanne

1 2 3 4 5 6 7 8 9

l; publicclassMappedFile {

// ⽂件名 privateString fileName; // ⽂件所在⽬录路径 privateString fileDirPath; // ⽂件对象 privateFile file; privateMappedByteBuffer

0 1 2 3 4 5 6 7 8

mappedByteBuffer;

19 0 1 2 3 4 5 6 7 8

privateFileChannel fileChannel;

privatebooleanboundSuccess =

false; // ⽂件最⼤只能为50MB privatefinalstaticlongMAX_FILE_

SIZE = 1024* 1024* 50;

// 最⼤的脏数据量512KB,系统必须触发 ⼀次强制刷

29 0 1 2 3 4 5 6 7 8

privatelongMAX_FLUSH_DATA_SIZE

= 1024* 512;

// 最⼤的刷间隔,系统必须触发⼀次强制 刷

privatelongMAX_FLUSH_TIME_GAP = 1000;

// ⽂件写⼊位置 privatelongwritePosition = 0; // 最后⼀次刷数据的时候 privatelonglastFlushTime; // 上⼀次刷的⽂件位置 privatelonglastFlushFilePositio

39 0 1 2 3 4 5 6 7 8

n = 0;

publicMappedFile(String

49 0 1 2 3 4 5

fileName, String fileDirPath) { super(); this.fileName = fileName; this.fileDirPath =

fileDirPath;

56

this.file = newFile(fileDirPath + "/"+ fileName);

7 8

59 0 1 2 3 4 5 6 7 8

if(!file.exists()) { try{

file.createNewFile ();

} catch(IOException e) {

e.printStackTrace( );

69 0 1 2 3 4 5 6 7 8

} }

} /** *

- * 内存映照⽂件绑定

- * @return

- */


79 0 1 2 3 4 5 6 7 8

publicsynchronizedbooleanboundC hannelToByteBuffer() { try{

RandomAccessFile raf = newRandomAccessFile(file, "rw");

this.fileChannel = raf.getChannel();

} catch(Exception e) { e.printStackTrace(); this.boundSuccess =

89 0 1 2 3 4 5 6 7 8 9

false;

returnfalse;

} try{

this.mappedByteBuffer = this.fileChannel

.map(FileChanne l.MapMode.READ_WRITE, 0, MAX_FILE_SIZE);

0 11

2 3 4 5 6 7 8

} catch(IOException e) { e.printStackTrace(); this.boundSuccess =

false;

returnfalse;

} this.boundSuccess = true; returntrue;

09 10 1 12 13 14

}

/**

15 16 17 18 19

* 写数据：先将之前的⽂件删除然后重 新

- * @param data

- * @return

- */


0 11

publicsynchronizedbooleanwriteD ata(byte[] data) {

2 3 4 5 6 7 8

returnfalse; }

/**

29

- * 在⽂件末尾追加数据

- * @param data

- * @return

- * @throws Exception

- */


0 11

2 3 4 5 6 7 8

publicsynchronizedbooleanappend Data(byte[] data) throwsException {

if(!boundSuccess) {

39

boundChannelToByteBuff er();

0 11

}

2 3 4 5 6 7 8

writePosition = writePosition + data.length;

if(writePosition >= MAX_FILE_SIZE) { // 如果写⼊data会超 出⽂件⼤⼩限制，不写⼊

49

flush(); writePosition =

0 11

2 3 4 5 6 7 8

writePosition - data.length;

System.out.println("Fi le="

+ file.toURI().toString()

+ " is written full.");

59

0 11

System.out.println("alr eady write data length:"

2 3 4 5 6 7 8

+ writePosition

+ ", max file size="+ MAX_FILE_SIZE); returnfalse;

} this.mappedByteBuffer.put(d

69

0 11 172

ata);

// 检查是否需要把内存缓冲刷到磁 盘

- 3
- 4
- 5


if( (writePosition lastFlushFilePosition > this.MAX_FLUSH_DATA_SIZE)

176

|| (System.currentTimeMil

lis() - lastFlushTime > this.MAX_FLUSH_TIME_GAP

&& writePosition > lastFlushFilePosition) ) {

flush(); // 刷到磁盘 }

returntrue;

} publicsynchronizedvoidflush()

{

this.mappedByteBuffer.forc e();

this.lastFlushTime = System.currentTimeMillis();

this.lastFlushFilePosition

= writePosition; } publiclonggetLastFlushTime() {

returnlastFlushTime;

} publicString getFileName() {

returnfileName;

} publicString getFileDirPath()

{

returnfileDirPath;

} publicbooleanisBundSuccess() {

returnboundSuccess;

} publicFile getFile() {

returnfile;

} publicstaticlonggetMaxFileSize(

) {

returnMAX_FILE_SIZE;

} publiclonggetWritePosition() {

returnwritePosition; }

publiclonggetLastFlushFilePosit ion() {

returnlastFlushFilePositio n;

} publiclonggetMAX_FLUSH_DATA_SIZ

E() {

returnMAX_FLUSH_DATA_SIZE;

} publiclonggetMAX_FLUSH_TIME_GAP

() {

returnMAX_FLUSH_TIME_GAP; }

}

