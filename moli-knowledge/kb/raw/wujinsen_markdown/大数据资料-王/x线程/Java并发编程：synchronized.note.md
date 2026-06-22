Java并发编程：synchronized

虽然多线程编程极⼤地提⾼了效率，但是也会带来⼀定的隐患。⽐如说两个线程同时往⼀个 数据库表中插⼊不重复的数据，就可能会导致数据库中插⼊了相同的数据。今天我们就来⼀起讨 论下线程安全问题，以及Java中提供了什么机制来解决线程安全问题。

以下是本⽂的⽬录⼤纲：

⼀.什么时候会出现线程安全问题？

⼆.如何解决线程安全问题？

三.synchronized同步⽅法或者同步块

若有不正之处，请多多谅解并欢迎批评指正。

请尊重作者劳动成果，转载请标明原⽂链接：

http://www.cnblogs.com/dolphin0520/p/3923737.html

# ⼀.什么时候会出现线程安全问题？

在单线程中不会出现线程安全问题，⽽在多线程编程中，有可能会出现同时访问同⼀个资源 的情况，这种资源可以是各种类型的的资源：⼀个变量、⼀个对象、⼀个⽂件、⼀个数据库表 等，⽽当多个线程同时访问同⼀个资源的时候，就会存在⼀个问题：

由于每个线程执⾏的过程是不可控的，所以很可能导致最终的结果与实际上的愿望相违背或 者直接导致程序出错。

举个简单的例⼦：

现在有两个线程分别从⽹络上读取数据，然后插⼊⼀张数据库表中，要求不能插⼊重复的数 据。

那么必然在插⼊数据的过程中存在两个操作：

- 1）检查数据库中是否存在该条数据；

- 2）如果存在，则不插⼊；如果不存在，则插⼊到数据库中。


假如两个线程分别⽤thread-1和thread-2表示，某⼀时刻，thread-1和thread-2都读取到 了数据X，那么可能会发⽣这种情况：

thread-1去检查数据库中是否存在数据X，然后thread-2也接着去检查数据库中是否存在数 据X。

结果两个线程检查的结果都是数据库中不存在数据X，那么两个线程都分别将数据X插⼊数据 库表当中。

这个就是线程安全问题，即多个线程同时访问⼀个资源时，会导致程序运⾏结果并不是想看 到的结果。

这⾥⾯，这个资源被称为：临界资源（也有称为共享资源）。

也就是说，当多个线程同时访问临界资源（⼀个对象，对象中的属性，⼀个⽂件，⼀个数据 库等）时，就可能会产⽣线程安全问题。

不过，当多个线程执⾏⼀个⽅法，⽅法内部的局部变量并不是临界资源，因为⽅法是在栈上 执⾏的，⽽Java栈是线程私有的，因此不会产⽣线程安全问题。

# ⼆.如何解决线程安全问题？

那么⼀般来说，是如何解决线程安全问题的呢？

基本上所有的并发模式在解决线程安全问题时，都采⽤“序列化访问临界资源”的⽅案，即在 同⼀时刻，只能有⼀个线程访问临界资源，也称作同步互斥访问。

通常来说，是在访问临界资源的代码前⾯加上⼀个锁，当访问完临界资源后释放锁，让其他 线程继续访问。

在Java中，提供了两种⽅式来实现同步互斥访问：synchronized和Lock。

本⽂主要讲述synchronized的使⽤⽅法，Lock的使⽤⽅法在下⼀篇博⽂中讲述。

# 三.synchronized同步⽅法或者同步块

在了解synchronized关键字的使⽤⽅法之前，我们先来看⼀个概念：互斥锁，顾名思义：能 到达到互斥访问⽬的的锁。

举个简单的例⼦：如果对临界资源加上互斥锁，当⼀个线程在访问该临界资源时，其他线程 便只能等待。

在Java中，每⼀个对象都拥有⼀个锁标记（monitor），也称为监视器，多线程同时访问某 个对象时，线程只有获取了该对象的锁才能访问。

在Java中，可以使⽤synchronized关键字来标记⼀个⽅法或者代码块，当某个线程调⽤该对

象的synchronized⽅法或者访问synchronized代码块时，这个线程便获得了该对象的锁，其他 线程暂时⽆法访问这个⽅法，只有等待这个⽅法执⾏完毕或者代码块执⾏完毕，这个线程才会释 放该对象的锁，其他线程才能执⾏这个⽅法或者代码块。

下⾯通过⼏个简单的例⼦来说明synchronized关键字的使⽤：

1.synchronized⽅法

下⾯这段代码中两个线程分别调⽤insertData对象插⼊数据：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br>30<br></th>
    <th>public class Test { public static voidmain(String[] args) { final InsertData insertData<br><br>= new InsertData();<br><br>new Thread() { public void run() {<br><br>insertData.insert(Thre ad.currentThread());<br><br>}; }.start();<br><br>new Thread() { public void run() {<br><br>insertData.insert(Thre ad.currentThread());<br><br>}; }.start(); }<br><br>} class InsertData {<br><br>private ArrayList<Integer> arrayList = new ArrayList<Integer>();<br><br>public void insert(Thread thread) {<br><br>for(int i=0;i<5;i++){ System.out.println(thread<br><br>.getName()+"在插⼊数据"+i);<br><br>arrayList.add(i); }<br><br>} }</th>
  </tr>
</table>


## 此时程序的输出结果为：

![image 1](<Java并发编程：synchronized.note_images/imageFile1.png>)

说明两个线程在同时执⾏insert⽅法。

⽽如果在insert⽅法前⾯加上关键字synchronized的话，运⾏结果为：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br></th>
    <th>class InsertData {<br><br>private ArrayList<Integer> arrayList = new ArrayList<Integer> ();<br><br>public synchronized void insert( Thread thread){<br><br>for(int i=0;i<5;i++){ System.out.println(thread<br><br>.getName()+"在插⼊数据"+i);<br><br>arrayList.add(i); }<br><br>} }</th>
  </tr>
</table>


![image 2](<Java并发编程：synchronized.note_images/imageFile2.png>)

从上输出结果说明，Thread-1插⼊数据是等Thread-0插⼊完数据之后才进⾏的。说明 Thread-0和Thread-1是顺序执⾏insert⽅法的。

这就是synchronized⽅法。

不过有⼏点需要注意：

- 1）当⼀个线程正在访问⼀个对象的synchronized⽅法，那么其他线程不能访问该对象的其

他synchronized⽅法。这个原因很简单，因为⼀个对象只有⼀把锁，当⼀个线程获取了该对象的 锁之后，其他线程⽆法获取该对象的锁，所以⽆法访问该对象的其他synchronized⽅法。

- 2）当⼀个线程正在访问⼀个对象的synchronized⽅法，那么其他线程能访问该对象的⾮

synchronized⽅法。这个原因很简单，访问⾮synchronized⽅法不需要获得该对象的锁，假如 ⼀个⽅法没⽤synchronized关键字修饰，说明它不会使⽤到临界资源，那么其他线程是可以访问 这个⽅法的，

- 3）如果⼀个线程A需要访问对象object1的synchronized⽅法fun1，另外⼀个线程B需要访


问对象object2的synchronized⽅法fun1，即使object1和object2是同⼀类型），也不会产⽣ 线程安全问题，因为他们访问的是不同的对象，所以不存在互斥问题。

2.synchronized代码块

synchronized代码块类似于以下这种形式：

<table>
  <tr>
    <th>1<br>2<br></th>
    <th>synchronized(synObject) { }</th>
  </tr>
</table>


3

当在某个线程中执⾏这段代码块，该线程会获取对象synObject的锁，从⽽使得其他线程⽆ 法同时访问该代码块。

synObject可以是this，代表获取当前对象的锁，也可以是类中的⼀个属性，代表获取该属性 的锁。

⽐如上⾯的insert⽅法可以改成以下两种形式：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br><br><br>12</th>
    <th>class InsertData {<br><br>private ArrayList<Integer> arrayList = new ArrayList<Integer> ();<br><br>public void insert(Thread thread){<br><br>synchronized (this) { for(int i=0;i<100;i++){<br><br>System.out.println(th read.getName()+"在插⼊数据"+i);<br><br>arrayList.add(i); }<br><br>} }<br><br>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br><br><br>13</th>
    <th>class InsertData {<br><br>private ArrayList<Integer> arrayList = new ArrayList<Integer> ();<br><br>private Object object<br><br>= newObject();<br><br>public void insert(Thread thread){<br><br>synchroniz for(inetdi=0(o;i<100;bject) i{++){<br><br>System.out.println(th read.getName()+"在插⼊数据"+i);<br><br>arrayList.add(i); }<br><br>} }<br><br>}</th>
  </tr>
</table>


从上⾯可以看出，synchronized代码块使⽤起来⽐synchronized⽅法要灵活得多。因为也 许⼀个⽅法中只有⼀部分代码只需要同步，如果此时对整个⽅法⽤synchronized进⾏同步，会影 响程序执⾏效率。⽽使⽤synchronized代码块就可以避免这个问题，synchronized代码块可以 实现只对需要同步的地⽅进⾏同步。

另外，每个类也会有⼀个锁，它可以⽤来控制对static数据成员的并发访问。

并且如果⼀个线程执⾏⼀个对象的⾮static synchronized⽅法，另外⼀个线程需要执⾏这个 对象所属类的static synchronized⽅法，此时不会发⽣互斥现象，因为访问static synchronized⽅法占⽤的是类锁，⽽访问⾮static synchronized⽅法占⽤的是对象锁，所以不 存在互斥现象。

看下⾯这段代码就明⽩了：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br><br>0<br>1<br>2<br>3<br>4<br><br><br>35</th>
    <th>public class Test { public static voidmain(String[] args) { final InsertData insertData<br><br>= new InsertData();<br><br>new Thread(){ @Override public void run() {<br><br>insertData.insert();<br><br>}.st }art(); new Thread(){<br><br>@Override public void run() {<br><br>insertData.insert1();<br><br>}.st }art(); }<br><br>} class InsertData {<br><br>public synchronized void insert() {<br><br>System.out.println("执⾏ insert");<br><br>try { Thread.sleep(5000);<br><br>} catch (InterruptedException e) {<br><br>e.printStackTrace();<br><br>} System.out.println("执⾏insert<br><br>完毕"); } public synchronized static void i<br><br>nsert1() { System.out.println("执⾏<br><br>insert1") Syst; em.out.println("执⾏ insert1完毕");<br><br>} }</th>
  </tr>
</table>


## 执⾏结果;

![image 3](<Java并发编程：synchronized.note_images/imageFile3.png>)

第⼀个线程⾥⾯执⾏的是insert⽅法，不会导致第⼆个线程执⾏insert1⽅法发⽣阻塞现象。

下⾯我们看⼀下synchronized关键字到底做了什么事情，我们来反编译它的字节码看⼀下， 下⾯这段代码反编译后的字节码为：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br><br><br>17</th>
    <th>public class InsertData { private Object object<br><br>= new Object(); public void insert(Thread thread){ synchronized (object) { }<br><br>} public synchronized void insert1<br><br>(Thread thread){ } public void insert2(Thread<br><br>thread){<br><br>} }</th>
  </tr>
</table>


![image 4](<Java并发编程：synchronized.note_images/imageFile4.png>)

从反编译获得的字节码可以看出，synchronized代码块实际上多了monitorenter和 monitorexit两条指令。monitorenter指令执⾏时会让对象的锁计数加1，⽽monitorexit指令执 ⾏时会让对象的锁计数减1，其实这个与操作系统⾥⾯的PV操作很像，操作系统⾥⾯的PV操作就 是⽤来控制多个线程对临界资源的访问。对于synchronized⽅法，执⾏中的线程识别该⽅法的 method_info 结构是否有 ACC_SYNCHRONIZED 标记设置，然后它⾃动获取对象的锁，调⽤ ⽅法，最后释放锁。如果有异常发⽣，线程⾃动释放锁。

有⼀点要注意：对于synchronized⽅法或者synchronized代码块，当出现异常时，JVM会 ⾃动释放当前线程占⽤的锁，因此不会由于异常导致出现死锁现象。

