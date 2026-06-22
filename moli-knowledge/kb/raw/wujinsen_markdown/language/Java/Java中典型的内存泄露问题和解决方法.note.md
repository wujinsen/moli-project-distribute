Q：在Java中怎么可以产⽣内存泄露？ A：Java中，造成内存泄露的原因有很多种。典型的例⼦是⼀个没有实现hasCode和 equals⽅法的Key类在HashMap中保存的情况。最后会⽣成很多重复的对象。所有的内存泄露 最后都会抛出OutOfMemoryEror异常，下⾯通过⼀段简短的通过⽆限循环模拟内存泄露 的例⼦说明⼀下。 复制代码 代码如下:

import java.util.HashMap; import java.util.Map;

public clas MemoryLeak {

public static void main(String[] args) { Map<Key, String> map = new HashMap<Key, String>(1 0);

int counter = 0; while (true) {

/ creates duplicate objects due to bad Key clas map.put(new Key("dumyKey"), "value"); counter+; if (counter % 1 0 = 0) {

System.out.println("map size: " + map.size(); System.out.println("Fre memory after count " + counter

+ " is " + getFreMemory() + "MB");

sl ep(1 0); }

} }

/ i ner clas key without hashcode() or equals()- bad implementation static clas Key {

private String key;

public Key(String key) {

this.key = key; }

}

/delay for a given period in mili seconds public static void sl ep(long sl epFor) {

try { Thread.sl ep(sl epFor); } catch (InteruptedException e) {

e.printStackTrace(); }

}

/get available memory in MB public static long getFreMemory() {

return Runtime.getRuntime().freMemory() / (1024 * 1024); }

} 结果如下： 复制代码 代码如下:

- map size: 1 0

- Fre memory after count 1 0 is 4MB

map size: 2 0

- Fre memory after count 2 0 is 4MB




- map size: 1396 0

- Fre memory after count 1396 0 is 2MB

map size: 1397 0

- Fre memory after count 1397 0 is 2MB

map size: 1398 0

- Fre memory after count 1398 0 is 2MB map size: 139 0 Fre memory after count 139 0 is 1MB map size: 14 0 Fre memory after count 14 0 is 1MB




map size: 1401 0 Fre memory after count 1401 0 is 1MB

. .

- map size: 1452 0

- Fre memory after count 1452 0 is 0MB

map size: 1453 0

- Fre memory after count 1453 0 is 0MB Exception in thread "main" java.lang.OutOfMemoryEror: Java heap space




at java.util.HashMap.adEntry(HashMap.java:753) at java.util.HashMap.put(HashMap.java:385) at MemoryLeak.main(MemoryLeak.java:10)

Q:怎么解决上⾯的内存泄露？ A：实现Key类的equals和hasCode⽅法。

复制代码 代码如下:

. static clas Key { private String key;

public Key(String key) {

this.key = key; }

@Overide public bolean equals(Object obj) {

if (obj instanceof Key)

return key.equals(Key) obj).key); else

return false;

}

@Overide

public int hashCode() {

return key.hashCode(); }

}

.

重新执⾏程序会得到如下结果：

复制代码 代码如下:

map size: 1

- Fre memory after count 1 0 is 4MB map size: 1
- Fre memory after count 2 0 is 4MB map size: 1
- Fre memory after count 3 0 is 4MB map size: 1
- Fre memory after count 4 0 is 4MB


.

- Fre memory after count 73 0 is 4MB map size: 1
- Fre memory after count 74 0 is 4MB map size: 1
- Fre memory after count 75 0 is 4MB


Q：在实际场景中，你怎么查找内存泄露？ A：通过以下代码获取线程ID 复制代码 代码如下:

C:\>jps 5808 Jps 4568 MemoryLeak 3860 Main 通过命令⾏打开jconsole 复制代码 代码如下:

C:\>jconsole 4568

实现了hasCode和equals的Key类和没有实现的图表如下所示：

没有内存泄露的：

![image 1](<Java中典型的内存泄露问题和解决方法.note_images/imageFile1.png>)

造成内存泄露的：

![image 2](<Java中典型的内存泄露问题和解决方法.note_images/imageFile2.png>)

您可能感兴趣的⽂章:

解析Java的JNI编程中的对象引⽤与内存泄漏问题

Java中内存分配的⼏种⽅法 详细介绍Java内存泄露原因

基于Java内存溢出的解决⽅法详解 深⼊分析Java内存区域的使⽤详解 java 序列化对象 serializable 读写数据的实例

JavaScipt对象的基本知识

深⼊理解Java对象的序列化与反序列化的应⽤

深⼊JAVA对象深度克隆的详解

深⼊Java对象的地址的使⽤分析

解析Java程序中对象内存的分配和控制的基本⽅法 Java内存泄露

Tags：

相关⽂章

2013-05-05java实现单链表中是否有环的⽅法详解

- 2013-09-09浅析java中Integer传参⽅式的问题

- 2013-1-1哲学家就餐问题中的JAVA多线程学习

2015-06-06Java 连接Aces数据库的两种⽅式

- 2015-05-05Java实现数字转成英⽂的⽅法

- 2016-01-01Java的⾯向对象编程基本概念学习笔记整理


- 2014-01-01简单的java socket客户端和服务端示例


- 2014-03-03java获取当前⽇期和时间的⼆种⽅法分享


- 2013-03-03java实现⼤⽂件分割与合并的实例代码

- 2014-04-04java 2d画图示例分享(⽤java画图)


# 最新评论

评论(0⼈参与， 0条评论)

![image 3](<Java中典型的内存泄露问题和解决方法.note_images/imageFile3.png>)

搜狐“我来说两句”⽤户公约

![image 4](<Java中典型的内存泄露问题和解决方法.note_images/imageFile4.png>)

![image 5](<Java中典型的内存泄露问题和解决方法.note_images/imageFile5.png>)

![image 6](<Java中典型的内存泄露问题和解决方法.note_images/imageFile6.png>)

微博登录 Q登录 ⼿机登录

等级不够，发表评论升⾄指定级别才能获得该特权。详情请参⻅等级说明。 还没有评论，快来抢沙发吧！

Powered by 畅⾔

⼤家感兴趣的内容

1java使double保留两位⼩数的多⽅

- 2JAVA8 ⼗⼤新特性详解

- 3JAVA ⼗六进制与字符串的转换

- 4Java环境变量的设置⽅法(图⽂教程
- 5java.net.SocketException: Con

- 6java写⼊⽂件的⼏种⽅法分享

- 7java 读写⽂件[多种⽅法]

- 8java中File类的使⽤⽅法
- 9Java中的两种for循环介绍

- 10Java中HashMap和TreMap的区别深


![image 7](<Java中典型的内存泄露问题和解决方法.note_images/imageFile7.png>)

![image 8](<Java中典型的内存泄露问题和解决方法.note_images/imageFile8.png>)

最近更新的内容

JAVA应⽤系统⼯具快捷托盘实例代码

扩展Hibernate使⽤⾃定义数据库连接池的⽅

解析Linux系统中JVM内存2GB上限的详解

Mybatis实战教程之⼊⻔到精通（经典）

解决MyEclipse中的Building workspace问题

java实现⽆符号数转换、字符串补⻬、md5、

java压缩zip⽂件中⽂乱码问题解决⽅法

快速排序的深⼊详解以及java实现

深⼊解析Java中volatile关键字的作⽤

将内容写到txt⽂档⾥⾯并读取及删除的⽅法

![image 9](<Java中典型的内存泄露问题和解决方法.note_images/imageFile9.png>)

![image 10](<Java中典型的内存泄露问题和解决方法.note_images/imageFile10.png>)

常⽤在线⼩⼯具

JavaScript代码在线加密⼯具

在线图⽚格式转换(jpg/bmp/gif/png)⼯具

JavaScript压缩/格式化/加密⼯具

php代码在线格式化美化⼯具

CS代码⼯具

在线XML/JSON互相转换⼯具

Unix时间戳(timestamp)转换⼯具

JavaScript代码格式化⼯具

歇后语在线查询

在线JSON代码检验/检验/美化/格式化

![image 11](<Java中典型的内存泄露问题和解决方法.note_images/imageFile11.png>)

![image 12](<Java中典型的内存泄露问题和解决方法.note_images/imageFile12.png>)

- - - - - ©CopyRight 206-2016 JB51.Net Inc Al Rights Reserved. 脚本之家 版权所有

关于我们 ⼴告合作 联系我们 免责声明 ⽹站地图 投诉建议 在线投稿

站⻓统计 站⻓统计 站⻓统计

查看标识获取更多信息

国内最好的⽂件管理最好的软件

![image 13](<Java中典型的内存泄露问题和解决方法.note_images/imageFile13.png>)

国内最好的⽂件管理最好的软件 上海开始软件推出的企业级电⼦⽂件管理 解决⽅案.热线电话:408-9108 ⽂档管理 ⽂件集中存储 ⽂件权限管理 ⽂件版本管理 去看看 X

图⽚相关信息

查看标识获取更多信息

史上最难PHPer笔试题

![image 14](<Java中典型的内存泄露问题和解决方法.note_images/imageFile14.png>)

史上最难PHPer笔试题

