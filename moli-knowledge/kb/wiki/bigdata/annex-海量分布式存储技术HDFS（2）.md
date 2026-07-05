---
title: 海量分布式存储技术HDFS（2）.note（原文插图 annex）
slug: annex-海量分布式存储技术HDFS（2）
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/海量分布式存储技术HDFS（2）.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

3. The Hadop Distributed File System

- 3.1. The Design of HDFS

HDFS设计的针对对象：适合流式访问的超⼤⽂件、在使⽤便宜的硬件搭建的集群上运⾏。

HDFS不⾜：

低延迟数据访问（Hbase是个好选择）、⼩⽂件多的时候出现问题（HDFS将⽂件Meta信息存储在内存 中，内存限制了可以控制的⽂件数量）、对⽂件的多个wirter进⾏写⼊或者任意位置的修改。

- 3.2. HDFS Concept


- 3.2.1. Blocks

HDFS中Block的⼤⼩默认是64M，⼩于块⼤⼩的的⽂件并不占据整个块的全部空间（⼀个块可能存有 多个⽂件）。

使⽤Blocks的好处：

- 1） 可以存储⼤⽂件，⼀个⽂件的⼤⼩可以⼤于任何⼀个单块硬盘的容量
- 2） 把存储单元抽象成块⽽不是⽂件，简化了存储⼦系统：简化了数据管理、取消元数据关注
- 3） 能很好适应数据复制，数据复制保证系统的容错和可⽤性。


- 3.2.2. Namenodes and Datanodes


Namenode：master

Datanode：worker

Namenode管理⽂件系统名字空间（filesystem namespace），它维持了⼀个filesystem tre,所有⽂ 件的metadata和⽬录都在⾥⾯。信息被以两种⽂件的形式持久化在硬盘上，namespace image,edit log.

Hdfs提供了两种namenode的容错机制：

- 1) 备份存储持久化状态的⽂件系统元数据的⽂件
- 2) 提供secondary namenode。Secondary的主要⻆⾊是合并namespace image和edit log，防⽌edit log过⼤。但是secondary namenode的数据较master namenode的数据有所延迟，所有数据恢复以后 肯定会有数据丢失


- 3.3. The Comand-line Interface


以伪分布式为例

基本的⽂件系统操作：

- 1） 将本地数据拷⻉到hdfs上

% hadop fs -copyFromLocal input/docs/quangle.txt hdfs:/localhost/user/tom/quangle.txt

hdfs:/可省去，这样变成

% hadop fs -copyFromLocal input/docs/quangle.txt /user/tom/quangle.txt

也可以使⽤相对路径：

% hadop fs -copyFromLocal input/docs/quangle.txt quangle.txt

- 2） 将数据从hdfs上拷⻉到本地硬盘并检查⽂件时候⼀致

% hadop fs -copyToLocal quangle.txt quangle.copy.txt

% md5 input/docs/quangle.txt quangle.copy.txt

MD5 (input/docs/quangle.txt) = a16f231da6b05e2ba7a39320e7dacd9

MD5 (quangle.copy.txt) = a16f231da6b05e2ba7a39320e7dacd9

- 3） Hdfs⽂件列表


% hadop fs -mkdir boks

% hadop fs -ls .

Found 2 items

drwxr-xr-x - tom supergroup 0 209-04-02 2 41 /user/tom/boks

-rw-r-r- 1 tom supergroup 18 209-04-02 2 29 /user/tom/quangle.txt

第⼀列：⽂件模式（类似posix）

第⼆列：⽂件被复制的份数

第三列：⽂件拥有者

第四列：⽂件拥有者的group

第五列：⽂件⼤⼩，⽬录显示为0

第六列：⽂件最后修改⽇期

第七列：⽂件最后修改时间

第⼋列：⽂件的绝对路径

- 3.4. Hadop Filesystems


Hadop有⼀个对⽂件系统的抽象，HDFS只是其中的⼀个实现。Java的抽象类 org.apache.hadop.fs.FileSystem代表了Hadop中的⽂件系统，还有其他的⼏种实现（48⻚）：

- 3.4.1. Interfaces


Hadop⽤Java写成，所有Hadop⽂件的交互都通过Java api来完成。

还有另外的与Hadop⽂件系统交互的库：Thrift、C、FUSE、WebDAV等

- 3.5. The Java Interface


- 3.5.1. Reading Data from a Hadop URL


最简单的⽅式是⽤java.net.URL对象打开⼀个流来读取。如下：

InputStream in = nul;

try {

in = new URL("hdfs:/host/path").openStream();

/ proces in

} finaly {

IOUtils.closeStream(in);

}

这⾥需要进⾏⼀点额外的⼯作才能使得URL识别hdfs的uri。我们要使⽤java.net.URL的 setURLStreamHandlerFactory()⽅法设置URLStreamHandlerFactory，这⾥需要传递⼀个 FsUrlStreamHandlerFactory。这个操作对⼀个jvm只能使⽤⼀次，我们可以在静态块中调⽤。

public clas URLCat {

static {

URL.setURLStreamHandlerFactory(new FsUrlStreamHandlerFactory();

}

public static void main(String[] args) throws Exception {

InputStream in = nul;

try {

in = new URL(args[0]).openStream();

IOUtils.copyBytes(in, System.out, 4096, false);

} finaly {

IOUtils.closeStream(in);

}

}

}

IOUtils是⼀个⼯具类，⽤来在finaly从句中关闭流，也可以⽤来拷⻉数据到输出流中。copyBytes⽅法 的四个参数代表的含义分别是：拷⻉的来源，去处，拷⻉的字节数已经在拷⻉完成之后是否关闭流。 本例会有如下结果呈现：

% hadop URLCat hdfs:/localhost/user/tom/quangle.txt

On the top of the Crumpety Tre

The Quangle Wangle sat,

But his face you could not se,

On acount of his Beaver Hat.

- 3.5.2. Reading Data Using the FileSystem API


在某些情况下设置URLStreamHandlerFactory的⽅式并不⼀定回⽣效。在这种情况下，需要⽤ FileSystem API来打开⼀个⽂件的输⼊流。

⽂件的位置是使⽤Hadop Path呈现在Hadop中的，与java.io中的不⼀样。

有两种⽅式获取FileSystem的实例：

public static FileSystem get(Configuration conf) throws IOException

public static FileSystem get(URI uri, Configuration conf) throws IOException

Configuration封装了client或者server的配置，这些配置从claspath中读取，⽐如被claspath指向的 conf/core-site.xml⽂件.

第⼀个⽅法从默认位置（conf/core-site.xml）读取配置，第⼆个⽅法根据传⼊的uri查找适合的配置⽂ 件，若找不到则返回使⽤第⼀个⽅法，即从默认位置读取。

在获得FileSystem实例之后，我们可以调⽤open()⽅法来打开输⼊流：

public FSDataInputStream open(Path f) throws IOException

public abstract FSDataInputStream open(Path f, int buferSize) throws IOException

第⼀个⽅法的参数f是⽂件位置，第⼆个⽅法的buferSize就是输⼊流的缓冲⼤⼩。

下⾯的代码是使⽤FileSystem打开输⼊流的示例：

public clas FileSystemCat {

public static void main(String[] args) throws Exception {

String uri = args[0];

Configuration conf = new Configuration();

FileSystem fs = FileSystem.get(URI.create(uri), conf);

InputStream in = nul;

try {

in = fs.open(new Path(uri);

IOUtils.copyBytes(in, System.out, 4096, false);

} finaly {

IOUtils.closeStream(in);

}

}

}

输出结果如下：

% hadop FileSystemCat hdfs:/localhost/user/tom/quangle.txt

On the top of the Crumpety Tre

The Quangle Wangle sat,

But his face you could not se,

On acount of his Beaver Hat.

- 3.5.2.1. FSDataInputStream


FileSystem的open⽅法返回了FSDataInputStream对象，⽽不是标准的java.io。

package org.apache.hadop.fs;

public clas FSDataInputStream extends DataInputStream

implements Sekable, PositionedReadable {

/ implementation elided

}

FSDataInputStream实现了Sekable接⼝，这样使其具有了随机访问的能⼒。

下⾯是Sekable接⼝的定义。

public interface Sekable {

void sek(long pos) throws IOException;

long getPos() throws IOException;

bolean sekToNewSource(long targetPos) throws IOException;

}

sek()⽅法提供了从⽂件开始查找某⼀位置的能⼒。getPos()⽅法则返回当前相对于⽂件起始位置的偏 移量。sekToNewSource⽅法不常⽤。

下⾯的程序将hdfs中的⽂件显示了两次：

public clas FileSystemDoubleCat {

public static void main(String[] args) throws Exception {

String uri = args[0];

Configuration conf = new Configuration();

FileSystem fs = FileSystem.get(URI.create(uri), conf);

FSDataInputStream in = nul;

try {

in = fs.open(new Path(uri);

IOUtils.copyBytes(in, System.out, 4096, false);

in.sek(0); / go back to the start of the file

IOUtils.copyBytes(in, System.out, 4096, false);

} finaly {

IOUtils.closeStream(in);

}

}

}

运⾏结果如下：

% hadop FileSystemDoubleCat hdfs:/localhost/user/tom/quangle.txt

On the top of the Crumpety Tre

The Quangle Wangle sat,

But his face you could not se,

On acount of his Beaver Hat.

On the top of the Crumpety Tre

The Quangle Wangle sat,

But his face you could not se,

On acount of his Beaver Hat.

FSDataInputStream也实现了PositionedReadable接⼝，从⽽能读取指定ofset开始的数据。

public interface PositionedReadable {

public int read(long position, byte[] bufer, int ofset, int length) throws IOException;

public void readFuly(long position, byte[] bufer, int ofset, int length) throws IOException;

public void readFuly(long position, byte[] bufer) throws IOException;

}

read()⽅法最多读取length bytes。Position是相对ofset的偏移，bufer存放读取的数据。

readFuly()⽅法读取length bytes的数据到bufer中，第⼆个readFuly则是读取bufer.length bytes的 数据到bufer中。以上的⽅法均不会改变ofset的值。

最后，sek()是⼀个开销⽐较⼤的操作，注意节省使⽤。

- 3.5.3. Writing Data


创建⽂件的最简单⽅法：

public FSDataOutputStream create(Path f) throws IOException

这个⽅法有⼏个重载，实现了⼀些例如：⽂件复制多少份，bufer的⼤⼩等。

此⽅法将创建⽂件的任何不存在的上级⽬录。例如，要创建⽂件/usr/a/text.txt，但是/usr/a/这个⽬录不 存在，这时候本⽅法将创建/usr/a/这个⽬录。

还有的重载函数传递了progresable接⼝，这使得我们可以获得写⼊过程的进度信息，progresable接 ⼝定义如下：

package org.apache.hadop.util;

public interface Progresable {

public void progres();

}

下⾯的⽅法⽤于追加数据：

public FSDataOutputStream apend(Path f) throws IOException

下⾯程序演示了如何拷⻉⼀个本地⽂件到Hadop filesystem中：

public clas FileCopyWithProgres {

public static void main(String[] args) throws Exception {

String localSrc = args[0];

String dst = args[1];

InputStream in = new BuferedInputStream(new FileInputStream(localSrc);

Configuration conf = new Configuration();

FileSystem fs = FileSystem.get(URI.create(dst), conf);

OutputStream out = fs.create(new Path(dst), new Progresable() {

public void progres() {

System.out.print(".");

}

});

IOUtils.copyBytes(in, out, 4096, true);

}

}

该程序每当写⼊64k数据之后就调⽤⼀次progres()⽅法。

结果如下： % hadop FileCopyWithProgres input/docs/140-8.txt hdfs:/localhost/user/tom/1408.txt .

- 3.5.3.1. FSDataOutPutStream

FileSystem的create()⽅法返回了⼀个FSDataOutPutStream。提供了获取当前位置的⽅法，但是没有 提供sek⽅法。

package org.apache.hadop.fs;

public clas FSDataOutputStream extends DataOutputStream implements Syncable {

public long getPos() throws IOException {

/ implementation elided

}

/ implementation elided

}

- 3.5.4. Directories


下⾯的⽅法创建⽬录，可以创建不存在的上级⽬录。因为在FileSystem的creat()⽅法中也能创建不存 在的上级⽬录，所以⼀般不⽤这个⽅法。

public bolean mkdirs(Path f) throws IOException

- 3.5.5. Querying the FileSystem


- 3.5.5.1. File metadata：FileStatus


FileStatus封装了⽂件和⽬录的信息。包括他们的⻓度，块⼤⼩，复制的份数，修改时间， ownership，权限等信息。

FileSystem的getFileStatus()提供了获取某⼀⽂件或者⽬录的FileStatus的⽅法。

下⾯是⼀个例⼦：

public clas ShowFileStatusTest {

private MiniDFSCluster cluster; / use an in-proces HDFS cluster for

/ testing

private FileSystem fs;

@Before

public void setUp() throws IOException {

Configuration conf = new Configuration();

if (System.getProperty("test.build.data") = nul) {

System.setProperty("test.build.data", "/tmp");

}

cluster = new MiniDFSCluster(conf, 1, true, nul);

fs = cluster.getFileSystem();

OutputStream out = fs.create(new Path("/dir/file");

out.write("content".getBytes("UTF-8");

out.close();

}

@After

public void tearDown() throws IOException {

if (fs != nul) {

fs.close();

}

if (cluster != nul) {

cluster.shutdown();

}

}

@Test(expected = FileNotFoundException.clas)

public void throwsFileNotFoundForNonExistentFile() throws IOException {

fs.getFileStatus(new Path("no-such-file");

}

@Test

public void fileStatusForFile() throws IOException {

Path file = new Path("/dir/file");

FileStatus stat = fs.getFileStatus(file);

asertThat(stat.getPath().toUri().getPath(), is("/dir/file");

asertThat(stat.isDir(), is(false);

asertThat(stat.getLen(), is(7L);

asertThat(stat.getModificationTime(), is(lesThanOrEqualTo(System

.curentTimeMilis( );

asertThat(stat.getReplication(), is(short) 1);

asertThat(stat.getBlockSize(), is(64 * 1024 * 1024L);

asertThat(stat.getOwner(), is("tom");

asertThat(stat.getGroup(), is("supergroup");

asertThat(stat.getPermision().toString(), is("rw-r-r-");

}

@Test

public void fileStatusForDirectory() throws IOException {

Path dir = new Path("/dir");

FileStatus stat = fs.getFileStatus(dir);

asertThat(stat.getPath().toUri().getPath(), is("/dir");

asertThat(stat.isDir(), is(true);

asertThat(stat.getLen(), is(0L);

asertThat(stat.getModificationTime(), is(lesThanOrEqualTo(System

.curentTimeMilis( );

asertThat(stat.getReplication(), is(short) 0);

asertThat(stat.getBlockSize(), is(0L);

asertThat(stat.getOwner(), is("tom");

asertThat(stat.getGroup(), is("supergroup");

asertThat(stat.getPermision().toString(), is("rwxr-xr-x");

}

}

如果⽂件不存在则会抛出FileNotFoundException。如果你仅对⽂件是否存在感兴趣，那么下⾯的⽅法 更加适合：

public bolean exists(Path f) throws IOException

- 3.5.5.2. Listing Files


有时候需要获取⼀个⽬录⾥⾯的内容，这时候下⾯的⽅法就⽐较⽤户了，他能得到⼀个⽬录的 FileStatus，⽅法有四个重载的函数。

public FileStatus[] listStatus(Path f) throws IOException

public FileStatus[] listStatus(Path f, PathFilter filter) throws IOException

public FileStatus[] listStatus(Path[] files) throws IOException

public FileStatus[] listStatus(Path[] files, PathFilter filter) throws IOException

当参数是⼀个⽂件的时候，返回的FileStatus的⻓度为1。当参数是⽬录的时候，返回数组的⻓度为0或 者⽬录中所有⽂件的FileStatus.

Pathfilter起过滤作⽤。

下⾯的程序是以上⽅法的⼀个演示：

public clas ListStatus {

public static void main(String[] args) throws Exception {

String uri = args[0];

Configuration conf = new Configuration();

FileSystem fs = FileSystem.get(URI.create(uri), conf);

Path[] paths = new Path[args.length];

for (int i = 0; i < paths.length; i +) {

paths[i] = new Path(args[i]);

}

FileStatus[] status = fs.listStatus(paths);

Path[] listedPaths = FileUtil.stat2Paths(status);

for (Path p : listedPaths) {

System.out.println(p);

}

}

}

FileUtil.stat2Paths(status);将FileStatus转化成Path。

执⾏效果如下：

% hadop ListStatus hdfs:/localhost/ hdfs:/localhost/user/tom

hdfs:/localhost/user

hdfs:/localhost/user/tom/boks

hdfs:/localhost/user/tom/quangle.txt

- 3.5.5.3. File patern

Hadop⽀持使⽤通配符来获取FileStatus的⽅法：

public FileStatus[] globStatus(Path pathPatern) throws IOException

public FileStatus[] globStatus(Path pathPatern, PathFilter filter) throws IOException

其中的pathPatern就可以含有通配符，通配符的具体说明⻅书61⻚。

- 3.5.5.4. PathFilter


PathFilter接⼝提供了更加灵活的扩充。

package org.apache.hadop.fs;

public interface PathFilter {

bolean acept(Path path);

}

Filter只允许不符合PathFilter的⽂件通过。

下⾯是⼀个实现pathfilter接⼝的例⼦：

public clas RegexExcludePathFilter implements PathFilter {

private final String regex;

public RegexExcludePathFilter(String regex) {

this.regex = regex;

}

public bolean acept(Path path) {

return !path.toString().matches(regex);

}

}

- 3.5.6. Deleting Data


public bolean delete(Path f, bolean recursive) throws IOException

如果f是⼀个⽂件或者空⽬录recursive的值就会被忽略。当⼀个⽬录不为空的时候：recursive为true 时，⽬录将连同内部的内容都会被删除，否则抛出IOException异常。

- 3.5.7. Data Flow


- 3.5.7.1. Anatomy of a File Read


- 1） 客户端通过调⽤FileSystem对象的open()⽅法打开需要读取的⽂件，对HDFS来说是调⽤ DistributedFileSystem
- 2） DistributedFileSystem通过RPC调⽤namenode确定⽂件的前⼏个block的位置。对于每⼀个 block，namenode返回⼀含有那个block拷⻉的datanode地址；接下来，datanode按照距离client的距 离进⾏排序（确定距离的⽅法后⾯有介绍）。如果client本身就是⼀个datanode，那么就从本地 datanode节点上读取数据。


DistributedFileSystem返回⼀个FSDataInputStream给客户端，让他从FSDataInputStream中读取数 据。FSDataInputStream接着包装⼀个DFSInputStream，他⽤来管理datanode和namenode的I/O

- 3) client调⽤流的read()⽅法。
- 4) DFSInputStream开始的时候存放了前⼏个blocks的datanode的地址，这时候开始连接到最近 datanode上。客户端反复调⽤read()⽅法，以流式⽅式从datanode读取数据。
- 5) 当读到block的结尾的时候，DFSInputStream会关闭到当前datanode的链接，然后查找下⼀个block 的最好的datanode。这些操作对客户端都是透明的，客户端感觉到的是连续的流。（读取的时候就开 始查找下⼀个块所在的地址）
- 6) 读取完成之后关闭FSDataInputStream


容错处理：

在读取期间，当client与datanode通信的时候如果发⽣错误的话，它会尝试读取下个紧接着的含有那个 block的datanode。Client会记住发⽣错误datanode，这样它就不必在读取以后的块的时候再尝试这个 datanode了。Client也验证从datanode传递过来的数据的checksum。如果错误的block被发现，它将 在尝试从另⼀个datanode读取数据前被报告给namenode。

这个设计的⼀个重要⽅⾯是：客户端联系datanodes直接接收数据，并且客户端被namenode导向包含 每块数据的最佳datanode。这样的设计可以使HDFS扩展⽽适应⼤量的客户端，因为数据传输线路是 通过集群中的所有datanode的，namenode只需要相应块的位置查询服务即可（⽽namenode是将块的 位置信息存放在内存中的，这样效率就⾮常⾼），namenode不需要提供数据服务，因为数据服务随着 客户端的增加将很快成为瓶颈。

最短路径问题：

Hadop计算路径是按照如下⽅式进⾏的：

- 1） 把⽹络看成树结构
- 2） 两个节点之间的距离=第⼀个节点到两个节点共同祖先节点的距离+第⼆个节点到两个节点共同祖 先节点的距离


下⾯是⼀个例⼦：

- distance(/d1/r1/n1, /d1/r1/n1) = 0 (proceses on the same node)
- distance(/d1/r1/n1, /d1/r1/n2) = 2 (diferent nodes on the same rack)


- distance(/d1/r1/n1, /d1/r2/n3) = 4 (nodes on diferent racks in the same data center)
- distance(/d1/r1/n1, /d2/r3/n4) = 6 (nodes in diferent data centers)


下⾯的图是书⼭的摘录，但是感觉有⼏处错误（如果我错了欢迎各位指出）：

R2上⾯的n1应该为n3，第⼆个datacenter上的n1应该为n4

下图为更加直⽩的表示，已经画成了树形，并且抽象出了两个数据中⼼共有的⽗节点

- 3.5.7.2. Anatomy of a File Write


![image 1](assets/imageFile1.png)

写的过程如下：

- 1） client通过调⽤DistributedFileSystem的Create⽅法来请求创建⽂件
- 2） DistributedFileSystem通过对namenode发出rpc请求，在namenode的namespace⾥⾯创建⼀个 新的⽂件，但是这时候并不关联任何的块。Namenode进⾏很多检查来保证不存在要创建的⽂件已经 存在于⽂件系统中，还有检查是否有相应的权限来创建⽂件。如果这些检查都完成了，那么namenode 将记录下来这个新⽂件的信息，否则⽂件创建失败，并且客户端会收到⼀个IOExpection。 DistributedFileSystem返回⼀个FSDataOutputStream给客户端⽤来写⼊数据。和读的情形⼀样， FSDataOutputStream将包装⼀个DFSOutputStream⽤于和datanode及namenode通信。
- 3） 客户端开始写数据。DFSDataOutputStream把要写⼊的数据分成包（packet），并将它们写⼊到 中间队列（data queue）中。Data queue中的数据由DataStreamer来读取。DataStreamer的职责是让 namenode分配新的块⸺通过找出合适的datanodes⸺来存储作为备份⽽复制的数据。这些 datanodes组成提个流⽔线，我们假设这个流⽔线是个三级流⽔线，那么⾥⾯将含有三个节点。 DataStreamer将数据⾸先写⼊到流⽔线中的第⼀个节点，
- 4） 然后由第⼀个节点将数据包传送并写⼊到第⼆个节点，然后第⼆个将数据包传送并写⼊到第三个 节点。


- 5） DFSOutputStream维护了⼀个内部关于packets的队列，⾥⾯存放等待被datanode确认⽆误的 packets的信息。这个队列称为ack queue。⼀个packet的信息被移出本队列当且仅当packet被流⽔线 中的所有节点都确认⽆误。

当正在写⼊数据的时候datanode发⽣错误的处理策略： 发现错误之后关闭流⽔线，然后将没有被确认的数据放到数据队列的开头，当前的块被赋予⼀个新的 标识，这信息将发给namenode，以便在损坏的数据节点恢复之后删除这个没有被完成的块。然后从流 ⽔线中移除损坏的datanode。之后将这个块剩下的数据写⼊到剩下的两个节点中。Namenode注意到 这个块的信息还没有被复制完成，他就在其他⼀个datanode上安排复制。接下来的block写⼊操作就和 往常⼀样了。

尽管可能在写⼊数据的时候多个节点都出现故障，但是只要默认的⼀个节点（dfs.replication.min）被 写⼊了，那么这个操作就会完成。因为数据块将会在集群间复制，直到复制完定义好的次数 （dfs.replication，默认3份）

- 6） 当完成数据写⼊之后客户端调⽤流的close⽅法，在通知namenode完成写⼊之前，这个⽅法将 flush残留的packets，并等待确认信息（acknowledgement）。
- 7） 因为先前已经存在DataStream请求namenode分配块这个操作，所以在这个阶段namenode会持有 构成⽂件的块的信息。在block完成复制到最少的份数之后，namenode将成功返回。


备份⽂件的放置策略：

- 1） 第⼀份存放在客户端（如果客户端没在集群上，那么这个节点将被随机选择，尽管这样，系统也不 会选择磁盘容量快满的，或者是⽐较忙的节点）
- 2） 第⼆份存放在与第⼀份不同机架的⼀个随机节点中
- 3） 第三份存放在与第⼆份相同的机架中，但是不在同⼀个节点
- 4） 接下来的就存放在集群中的随机节点中了，系统尽量避免在⼀个机架中存放多份备份⽂件。


- 3.5.7.3. Coherency Model ⽂件系统的连贯性模型描述了读写⽂件过程中的数据可⻅性。HDFS去掉了⼀些POSIX对性能的要求， 所以⼀些操作可能与你的预想不⼤⼀致。


- 1） 在⽂件被创建之后，它在⽂件系统的名字空间中是可⻅的 Path p = new Path("p"); fs.create(p); asertThat(fs.exists(p), is(true);
- 2） 但是任何没写⼊到⽂件的内容不保证可⻅，尽管你可能去flush流。所以⽂件看起来⻓度为0 Path p = new Path("p"); OutputStream out = fs.create(p); out.write("content".getBytes("UTF-8");


- out.flush(); asertThat(fs.getFileStatus(p).getLen(), is(0L);
- 3） 当超过⼀个block的数据被写⼊之后，第⼀个block对reader将是可⻅的，接下来的也是⼀样：当前 正在写的block总是不可⻅的，已经被写⼊的block是可⻅的。
- 4） HDFS通过FSDataOutputStream的sync()⽅法提供了⼀种强制使所有bufer同步到datanode⽅ 法。当sync()成功返回之后，HDFS保证sync之前的数据被持久化并且对所有reader可⻅。遇到client 的crash时间，数据也不会丢失。 Path p = new Path("p"); FSDataOutputStream out = fs.create(p); out.write("content".getBytes("UTF-8"); out.flush(); out.sync(); asertThat(fs.getFileStatus(p).getLen(),is(long) "content".length( ); 这个⾏为 有点像unix系统的fsync系统调⽤，他把⽂件描述符的数据进⾏提交。 例如使⽤Java API来写本地⽂件，我们可能保证在flush stream和synchronizing之后数据是可⻅的： FileOutputStream out = new FileOutputStream(localFile); out.write("content".getBytes("UTF-8"); out.flush(); / flush to operating system out.getFD().sync(); / sync to disk asertThat(localFile.length(), is(long) "content".length( ); 在HDFS中关闭⼀个⽂件其实是进⾏了⼀个隐含的sync()操作： Path p = new Path("p"); OutputStream out = fs.create(p); out.write("content".getBytes("UTF-8"); out.close(); asertThat(fs.getFileStatus(p).getLen(), is(long) "content".length( );


- 3.5.7.4. Consequences for aplication design 由于连贯性模型的原因，如果在写程序的时候没有调⽤sync函数，那么很有可能在客户端或者服务器 出错的情况下丢失⼀个block的数据。对于很多程序来说这是不能接受的，所以要在程序中适时的调⽤ sync()，例如在写⼊⼀定数量的记录或者⼀定量的数据之后就调⽤⼀下sync()。尽管sync()在设计的时 候致⼒于不给HDFS增加太⼤的负荷，但是它确实要有些开销的，所以这样就有⼀个在数据的鲁棒性和 吞吐量之间的权衡问题。⼀个可以接受的权衡是具有程序依赖性的，并且合适的值可以在测试系统的 性能与不同的sync()调⽤频率之后被确定。


- 3.6. Paralel Copying with distcp 之前我们看到的HDFS访问模式都是单线程的访问。它可以通过指定⽂件通配符来做访问⼀批数据，但 是对于⾼效、并⾏处理这些⽂件的时候你就必须⾃⼰写程序了。 Hadop提供了⼀个⾮常有⽤的⼯具⸺distcp，来在Hadop⽂件系统之间拷⻉⼤量数据。


distcp的⼀个典型⽤途就是在两个HDFS集群之间传递数据。如果两个集群运⾏着不同版本的 Hadop，那么前缀hdfs需要加上： % hadop distcp hdfs:/namenode1/fo hdfs:/namenode2/bar 上⾯的代码将会把/fo⽬录连同他内部的⽂件从第⼀个集群拷⻉到第⼆个集群的/bar⽬录。之后第⼆个 集群将呈现出/bar/fo这样的⽬录结构，如果拷⻉前bar⽬录不存在的话，它将会被先创建出来，源⽂ 件可以有多个，但是源⽂件的路径必须是绝对路径。 在默认情况下，distcp将会跳过已经存在的⽂件，但是在提供 –overwrite参数的情况下，存在的⽂件将 会被覆盖。也可以选择-update参数来更新⽂件。

使⽤-overwrite或者-update选项改变了以前源路径和⽬标路径的使⽤⽅式，下⾯⽤⼀个例⼦解释： 假如在经过上⾯的拷⻉操作之后，我们⼜改变了/fo⽬录下的⼀个⽂件的内容，更新的时候我们需要这 样写： % hadop distcp -update hdfs:/namenode1/fo hdfs:/namenode2/bar/fo Namenode2的/fo⼦⽬录需要加上。

除了上⾯介绍的选项之外还有很多选项可以控制distcp的⾏为，包括忽略失败，限制⽂件数量或者要拷 ⻉的数据量等。调⽤distcp的时候不加参数可以看到这些选项的使⽤说明。

distcp是作为⼀个MapReduce job实现的。拷⻉⼯作是并⾏运⾏在集群中的map节点完成的。没有 reduce节点。每个⽂件由⼀个map来完成拷⻉，并且distcp尝试通过分配给每个map近似相同分量的 数据。（Each file is copied by a single map, and distcp tries to give each map aproximately the same amount of data, by bucketing files into roughly equal alocations.） map的数量由如下⽅式确定： 由于让每个map拷⻉⼀个合理分量的数据来最⼩化task机构的开销是⼀个不错的注意，所以每个map⾄ 少拷⻉256MB数据（除⾮数据量不够，这种情况下⼀个拷⻉由⼀个map来完成）。例如：给⼀个1GB 的⽂件分配4个map任务。当数据太⼤的时候限制map的数量来限制带宽和集群利⽤就有必要了。默认 情况下，map的最⼤数量是20/tasktracker.例如拷⻉1 0GB⽂件的情况下，对于⼀个有10个节点的 集群来说会启动2 0个map（按照每个节点20个）。这样的情形下，每个节点将平均拷⻉512MB的数 据。可以启动的map数量可以通过调整distcp的-m参数来调整。例如-m 1 0将分配1 0个maps，每 个拷⻉1GB数据。

如果想在运⾏不同版本的HDFS集群之间拷⻉使⽤hdfs协议运⾏distcp的话会产⽣错误。因为不同系统 的rpc系统不兼容。为了补救，可以使⽤hftp从源⽂件中读取数据。但job就必须在拷⻉的⽬标机器上运 ⾏，以便HDFS的rpc版本兼容。上⾯的例⼦可以写成下⾯的样⼦： % hadop distcp h hdfs:/namenode2/bar 注意：必须在uri中指定namenod的web端⼝号。这个端⼝的默认值是5070，由dfs.htp.adres属性 值来决定。

ftp:/namenode1 5070/fo

- 3.6.1. Keping an HDFS Cluster Balanced 当拷⻉数据到HDFS的时候，考虑集群平衡很重要。当block在集群间均匀分布的时候性能最佳，所以 要尽量保证distcp不要北打扰。 还拿刚才1 0G的那个⽂件距离，如果指定-m为1的话，将只有⼀个map来进⾏拷⻉⼯作⸺先不考虑 速度问题和资源没有被充分利⽤的问题⸺这样就⼀位置每个block的第⼀个复制份将驻留在运⾏map 的那个node上，直到硬盘被填满。第⼆和第三个复制品将分散在集群中，但是运⾏map的节点就不平 衡了。通过启⽤超过集群中节点数量的map可以避免这个问题，也正是因为这样，最好是以默认是以 默认的20个map每⼀个节点来运⾏distcp。 但并不可能总是阻⽌⼀个集群变得不平衡。可能你会想限制map的数量来使某些节点执⾏其他的任 务。这种情况下，你可以使⽤⼀些均衡⼯具（balancer tol）来在接下来的时间中提⾼block在集群中 的分布。
- 3.7. Hadop Archives HDFS存放⼩⽂件是低效⼤的，因为每个⽂件都存放在⼀个block中，⽽block的metadata保存在 namenode的内存中。因此，⼤量的⼩⽂件会吃掉namenode的很多内存。注意：⼩⽂件并不需要占据 ⽐他⾃身更多的存储空间。1MB的⽂件储存在128MB的块中只占⽤1MB的磁盘空间，不占⽤128MB的 空间。


Hadop Archive（HAR）是⼀个⽂件打包⼯具，他在⾼效的将⽂件打包到HDFS block的同时，也可以 减少namenode的内存使⽤量，并且仍旧允许客户端透明的访问⽂件。 HAR可以作为MapReduce的输⼊。

- 3.7.1. Using Hadop Archives HAR是使⽤archive⼯具打包⼀些⽂件创建的。Archive⼯具运⾏⼀个MapReduce job来并⾏处理输⼊⽂ 件。所以你需要在⼀个运⾏MapReduce的集群上使⽤它。 下⾯是⼀些要打包的⽂件： % hadop fs -lsr /my/files


- -rw-r-r- 1 tom supergroup 1 209-04-09 19 13 /my/files/a drwxr-xr-x - tom supergroup 0 209-04-09 19 13 /my/files/dir
- -rw-r-r- 1 tom supergroup 1 209-04-09 19 13 /my/files/dir/b 运⾏⼀下命令打包： % hadop archive -archiveName files.har /my/files /my 第⼀个参数要打包成的⽂件名（,har必须），第⼆个是要打包的⽂件，可以是⽬录，也可以有多个要打 包的源，最后⼀个才参数是打包之后⽂件存放的位置。 运⾏上述命令之后，产⽣的.har⽂件信息如下： % hadop fs -ls /my


- Found 2 items drwxr-xr-x - tom supergroup 0 209-04-09 19 13 /my/files


- drwxr-xr-x - tom supergroup 0 209-04-09 19 13 /my/files.har % hadop fs -ls /my/files.har
- Found 3 items


- -rw-r-r- 10 tom supergroup 165 209-04-09 19 13 /my/files.har/_index
- -rw-r-r- 10 tom supergroup 23 209-04-09 19 13 /my/files.har/_masterindex
- -rw-r-r- 1 tom supergroup 2 209-04-09 19 13 /my/files.har/part-0 上⾯的结果显示了：两个索引，⼀个part⽂件。对本例来说part⽂件只有⼀个。 Part⽂件包含了原始⽂件的内容，index⽤来索引这些数据。 使⽤har URI scheme与har⽂件交互： % hadop fs -lsr har:/my/files.har drw-r-r- - tom supergroup 0 209-04-09 19 13 /my/files.har/my drw-r-r- - tom supergroup 0 209-04-09 19 13 /my/files.har/my/files
- -rw-r-r- 10 tom supergroup 1 209-04-09 19 13 /my/files.har/my/files/a drw-r-r- - tom supergroup 0 209-04-09 19 13 /my/files.har/my/files/dir
- -rw-r-r- 10 tom supergroup 1 209-04-09 19 13 /my/files.har/my/files/dir/b


以上都是在default FileSystem上⾯的操作，若是操作不同的FileSystem，可以⽤如下的形式： % hadop fs -lsr har:/hdfs-localhost:8020/myfiles.har/my/files/dir 注意有⼀个hdfs指定FileSystem的scheme，localhost是主机地址，8082为端⼝ 这与在⽬标机器上⾯运⾏：% hadop fs -lsr har:/my/files.har效果是⼀样的

删除HAR⽂件： %hadop fs –rmr /my/files.har 3.7.2. Limitations

- 1） 创建的是归档⽂件，没有压缩功能，所以不会节省空间
- 2） 归档⽂件创建之后不能被修改，若要添加、删除⽂件的话，需要重新建⽴归档⽂件
- 3） 虽然HAR⽂件可以作为MapReduce的输⼊，但是InputFormat不⽀持将多个⽂件打包到⼀个 MapReduce split中。所以处理⼤量的⼩⽂件，即使是在har⽂件中，都将是低效的。
