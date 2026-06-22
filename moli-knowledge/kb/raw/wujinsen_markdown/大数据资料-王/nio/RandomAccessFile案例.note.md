## [java]

view plaincopy import java.io.IOException; import java.io.RandomAcesFile;

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


publicclas TestRandomAcesFile {

publicstaticvoid main(String[] args) throws IOException { RandomAcesFile rf = new RandomAcesFile("rtest.dat", "rw"); for (int i = 0; i < 10; i +) {

/写⼊基本类型double数据 rf.writeDouble(i * 1.414);

} rf.close(); rf = new RandomAcesFile("rtest.dat", "rw");

/直接将⽂件指针移到第5个double数据后⾯ rf.sek(5 * 8);

/覆盖第6个double数据 rf.writeDouble(47. 01); rf.close(); rf = new RandomAcesFile("rtest.dat", "r"); for (int i = 0; i < 10; i +) {

System.out.println("Value " + i + ": " + rf.readDouble();

} rf.close();

} }

内存映射⽂件

内存映射⽂件能让你创建和修改那些因为太⼤⽽⽆法放⼊内存的⽂件。有了内存映射⽂件，你就可以 认为⽂件已经全部读进了内存，然后把它当成⼀个⾮常⼤的数组来访问。这种解决办法能⼤⼤简化修 改⽂件的代码。fileChanel.map(FileChanel.MapMode mode, long position, long size)将此通道的 ⽂件区域直接映射到内存中。注意，你必须指明，它是从⽂件的哪个位置开始映射的，映射的范围⼜ 有多⼤；也就是说，它还可以映射⼀个⼤⽂件的某个⼩⽚断。

MapedByteBufer是ByteBufer的⼦类，因此它具备了ByteBufer的所有⽅法，但新添了force()将缓 冲区的内容强制刷新到存储设备中去、load()将存储设备中的数据加载到内存中、isLoaded()位置内存 中的数据是否与存储设置上同步。这⾥只简单地演示了⼀下put()和get()⽅法，除此之外，你还可以使 ⽤asCharBufer( )之类的⽅法得到相应基本类型数据的缓冲视图后，可以⽅便的读写基本类型数据。 [java]

view plaincopy import java.io.RandomAcesFile; import java.nio.MapedByteBufer; import java.nio.chanels.FileChanel;

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


publicclas LargeMapedFiles { staticint length = 0x8 0; / 128 Mb

publicstaticvoid main(String[] args) throws Exception {

/ 为了以可读可写的⽅式打开⽂件，这⾥使⽤RandomAcesFile来创建⽂件。 FileChanel fc = new RandomAcesFile("test.dat", "rw").getChanel();

/注意，⽂件通道的可读可写要建⽴在⽂件流本身可读写的基础之上 MapedByteBufer out = fc.map(FileChanel.MapMode.READ_WRITE, 0, length); /写128M的内容 for (int i = 0; i < length; i +) { out.put(byte) 'x');

} System.out.println("Finished writing");

/读取⽂件中间6个字节内容 for (int i = length / 2; i < length / 2 + 6; i +) { System.out.print(char) out.get(i);

} fc.close();

} }

尽管映射写似乎要⽤到FileOutputStream，但是映射⽂件中的所有输出 必须使⽤ RandomAcesFile，但如果只需要读时可以使⽤FileInputStream，写映射⽂件时⼀定要使 ⽤随机访问⽂件，可能写时要读的原因吧。

该程序创建了⼀个128Mb的⽂件，如果⼀次性读到内存可能导致内存溢出，但这⾥访问好像只是⼀瞬 间的事，这是因为，真正调⼊内存的只是其中的⼀⼩部分，其余部分则被放在交换⽂件上。这样你就 可以很⽅便地修改超⼤型的⽂件了(最⼤可以到2 GB)。注意，Java是调⽤操作系统的"⽂件映射机 制"来提升性能的。

# RandomAcesFile类的应⽤：

[java]view plaincopy /*

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


- * 程序功能：演示了RandomAcesFile类的操作，同时实现了⼀个⽂件复制操作。
- */ package com.lwj.demo;


import java.io.*;

publicclas RandomAcesFileDemo { publicstaticvoid main(String[] args) throws Exception { RandomAcesFile file = new RandomAcesFile("file", "rw");

/ 以下向file⽂件中写数据 file.writeInt(20);/ 占4个字节 file.writeDouble(8.236598);/ 占8个字节 file.writeUTF("这是⼀个UTF字符串");/ 这个⻓度写在当前⽂件指针的前两个字节处，可⽤

readShort()读取 file.writeBolean(true);/ 占1个字节 file.writeShort(395);/ 占2个字节 file.writeLong(2325451l);/ 占8个字节 file.writeUTF("⼜是⼀个UTF字符串"); file.writeFloat(35.5f);/ 占4个字节 file.writeChar('a');/ 占2个字节

file.sek(0);/ 把⽂件指针位置设置到⽂件起始处

/ 以下从file⽂件中读数据，要注意⽂件指针的位置 System.out.println(" ⸻从file⽂件指定位置读数据 ⸻"); System.out.println(file.readInt(); System.out.println(file.readDouble(); System.out.println(file.readUTF();

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


file.skipBytes(3);/ 将⽂件指针跳过3个字节，本例中即跳过了⼀个bolean值和short值。 System.out.println(file.readLong();

file.skipBytes(file.readShort(); / 跳过⽂件中“⼜是⼀个UTF字符串”所占字节，注意readShort() ⽅法会移动⽂件指针，所以不⽤加2。

System.out.println(file.readFloat();

/以下演示⽂件复制操作 System.out.println(" ⸻⽂件复制（从file到fileCopy） ⸻"); file.sek(0); RandomAcesFile fileCopy=new RandomAcesFile("fileCopy","rw"); int len=(int)file.length();/取得⽂件⻓度（字节数） byte[] b=newbyte[len]; file.readFuly(b); fileCopy.write(b); System.out.println("复制完成！");

} }

# RandomAcesFile 插⼊写示例：

[java]

view plaincopy /* *

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


- * @param skip 跳过多少过字节进⾏插⼊数据
- * @param str 要插⼊的字符串
- * @param fileName ⽂件路径
- */ publicstaticvoid beiju(long skip, String str, String fileName){


try { RandomAcesFile raf = new RandomAcesFile(fileName,"rw"); if(skip < 0| skip > raf.length(){

System.out.println("跳过字节数⽆效"); return;

} byte[] b = str.getBytes(); raf.setLength(raf.length() + b.length); for(long i = raf.length() - 1; i > b.length + skip - 1; i-){

raf.sek(i - b.length);

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


byte temp = raf.readByte(); raf.sek(i); raf.writeByte(temp);

} raf.sek(skip); raf.write(b); raf.close();

} catch (Exception e) {

e.printStackTrace(); }

}

# 利⽤RandomAcesFile实现⽂件的多线程下载，即多线程下载⼀个⽂件时，将⽂件分成⼏ 块，每块⽤不同的线程进⾏下载。下⾯是⼀个利⽤多线程在写⽂件时的例⼦，其中预先分 配⽂件所需要的空间，然后在所分配的空间中进⾏分块，然后写⼊：

[java]

view plaincopy import java.io.FileNotFoundException; import java.io.IOException; import java.io.RandomAcesFile;

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


/*

- * 测试利⽤多线程进⾏⽂件的写操作
- */ publicclas Test {


publicstaticvoid main(String[] args) throws Exception {

/ 预分配⽂件所占的磁盘空间，磁盘中会创建⼀个指定⼤⼩的⽂件 RandomAcesFile raf = new RandomAcesFile("D:/abc.txt", "rw"); raf.setLength(1024*1024); / 预分配 1M 的⽂件空间 raf.close();

/ 所要写⼊的⽂件内容

- String s1 = "第⼀个字符串";
- String s2 = "第⼆个字符串";
- String s3 = "第三个字符串";
- String s4 = "第四个字符串";


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


- String s5 = "第五个字符串";


/ 利⽤多线程同时写⼊⼀个⽂件

- new FileWriteThread(1024*1,s1.getBytes().start(); / 从⽂件的1024字节之后开始写⼊数据
- new FileWriteThread(1024*2,s2.getBytes().start(); / 从⽂件的2048字节之后开始写⼊数

据

- new FileWriteThread(1024*3,s3.getBytes().start(); / 从⽂件的3072字节之后开始写⼊数

据

- new FileWriteThread(1024*4,s4.getBytes().start(); / 从⽂件的4096字节之后开始写⼊数

据

- new FileWriteThread(1024*5,s5.getBytes().start(); / 从⽂件的5120字节之后开始写⼊数


据

}

/ 利⽤线程在⽂件的指定位置写⼊指定数据

staticclas FileWriteThread extends Thread{ privateint skip; privatebyte[] content;

public FileWriteThread(int skip,byte[] content){ this.skip = skip; this.content = content;

}

publicvoid run(){ RandomAcesFile raf = nul; try {

raf = new RandomAcesFile("D:/abc.txt", "rw"); raf.sek(skip); raf.write(content);

} catch (FileNotFoundException e) {

e.printStackTrace(); } catch (IOException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} finaly { try {

- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.


raf.close(); } catch (Exception e) { }

} }

}

}

