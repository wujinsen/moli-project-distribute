---
title: Java并发编程：Lock.note（原文插图 annex）
slug: annex-Java并发编程：Lock
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/x线程/Java并发编程：Lock.note.md
related: [java-并发面试题]
created: 2026-07-05
updated: 2026-07-05
---

在上⼀篇⽂章中我们讲到了如何使⽤关键字synchronized来实现同步访问。本⽂我们继续来探讨 这个问题，从Java 5之后，在java.util.concurrent.locks包下提供了另外⼀种⽅式来实现同步 访问，那就是Lock。

也许有朋友会问，既然都可以通过synchronized来实现同步访问了，那么为什么还需要提供 Lock？这个问题将在下⾯进⾏阐述。本⽂先从synchronized的缺陷讲起，然后再讲述 java.util.concurrent.locks包下常⽤的有哪些类和接⼝，最后讨论以下⼀些关于锁的概念⽅⾯的 东⻄

以下是本⽂⽬录⼤纲：

⼀.synchronized的缺陷

⼆.java.util.concurrent.locks包下常⽤的类

三.锁的相关概念介绍

若有不正之处请多多谅解，并欢迎批评指正。

请尊重作者劳动成果，转载请标明原⽂链接：

http://www.cnblogs.com/dolphin0520/p/3923167.html

# ⼀.synchronized的缺陷

synchronized是java中的⼀个关键字，也就是说是Java语⾔内置的特性。那么为什么会出 现Lock呢？

在上⾯⼀篇⽂章中，我们了解到如果⼀个代码块被synchronized修饰了，当⼀个线程获取了 对应的锁，并执⾏该代码块时，其他线程便只能⼀直等待，等待获取锁的线程释放锁，⽽这⾥获 取锁的线程释放锁只会有两种情况：

- 1）获取锁的线程执⾏完了该代码块，然后线程释放对锁的占有；

- 2）线程执⾏发⽣异常，此时JVM会让线程⾃动释放锁。


那么如果这个获取锁的线程由于要等待IO或者其他原因（⽐如调⽤sleep⽅法）被阻塞了，但 是⼜没有释放锁，其他线程便只能⼲巴巴地等待，试想⼀下，这多么影响程序执⾏效率。

因此就需要有⼀种机制可以不让等待的线程⼀直⽆期限地等待下去（⽐如只等待⼀定的时间 或者能够响应中断），通过Lock就可以办到。

再举个例⼦：当有多个线程读写⽂件时，读操作和写操作会发⽣冲突现象，写操作和写操作 会发⽣冲突现象，但是读操作和读操作不会发⽣冲突现象。

但是采⽤synchronized关键字来实现同步的话，就会导致⼀个问题：

如果多个线程都只是进⾏读操作，所以当⼀个线程在进⾏读操作时，其他线程只能等待⽆法 进⾏读操作。

因此就需要⼀种机制来使得多个线程都只是进⾏读操作时，线程之间不会发⽣冲突，通过 Lock就可以办到。

另外，通过Lock可以知道线程有没有成功获取到锁。这个是synchronized⽆法办到的。

总结⼀下，也就是说Lock提供了⽐synchronized更多的功能。但是要注意以下⼏点：

- 1）Lock不是Java语⾔内置的，synchronized是Java语⾔的关键字，因此是内置特性。

Lock是⼀个类，通过这个类可以实现同步访问；

- 2）Lock和synchronized有⼀点⾮常⼤的不同，采⽤synchronized不需要⽤户去⼿动释放


锁，当synchronized⽅法或者synchronized代码块执⾏完之后，系统会⾃动让线程释放对锁的 占⽤；⽽Lock则必须要⽤户去⼿动释放锁，如果没有主动释放锁，就有可能导致出现死锁现象。

# ⼆.java.util.concurent.locks包下常⽤的类

下⾯我们就来探讨⼀下java.util.concurrent.locks包中常⽤的类和接⼝。

## 1.Lock

⾸先要说明的就是Lock，通过查看Lock的源码可知，Lock是⼀个接⼝：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br></th>
    <th>public interface Lock { void lock(); void lockInterruptibly() throws I<br><br>nterruptedException; boolean tryLock(); boolean tryLock(longtime,<br><br>TimeUnit unit) throws InterruptedException;<br><br>void unlock(); Condition newCondition();<br><br>}</th>
  </tr>
</table>


下⾯来逐个讲述Lock接⼝中每个⽅法的使⽤，lock()、tryLock()、tryLock(long time, TimeUnit unit)和lockInterruptibly()是⽤来获取锁的。unLock()⽅法是⽤来释放锁的。 newCondition()这个⽅法暂且不在此讲述，会在后⾯的线程协作⼀⽂中讲述。

在Lock中声明了四个⽅法来获取锁，那么这四个⽅法有何区别呢？

⾸先lock()⽅法是平常使⽤得最多的⼀个⽅法，就是⽤来获取锁。如果锁已被其他线程获 取，则进⾏等待。

由于在前⾯讲到如果采⽤Lock，必须主动去释放锁，并且在发⽣异常时，不会⾃动释放锁。 因此⼀般来说，使⽤Lock必须在try{}catch{}块中进⾏，并且将释放锁的操作放在finally块中 进⾏，以保证锁⼀定被被释放，防⽌死锁的发⽣。通常使⽤Lock来进⾏同步的话，是以下⾯这种 形式去使⽤的：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br></th>
    <th>Lock lock = ...; lock.lock(); try{<br><br>//处理任务 }catch(Exception ex){ }finally{ } lock.unlock(); //释放锁</th>
  </tr>
</table>


9

tryLock()⽅法是有返回值的，它表示⽤来尝试获取锁，如果获取成功，则返回true，如果获 取失败（即锁已被其他线程获取），则返回false，也就说这个⽅法⽆论如何都会⽴即返回。在拿 不到锁时不会⼀直在那等待。

tryLock(long time, TimeUnit unit)⽅法和tryLock()⽅法是类似的，只不过区别在于这个 ⽅法在拿不到锁时会等待⼀定的时间，在时间期限之内如果还拿不到锁，就返回false。如果如果 ⼀开始拿到锁或者在等待期间内拿到了锁，则返回true。

所以，⼀般情况下通过tryLock来获取锁时是这样使⽤的：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br><br><br>12</th>
    <th>Lock lock = ...; if(lock.tryLock()) {<br><br>try{<br><br>//处理任务 }catch(Exception ex){ }finally{ } lock.unlock(); //释放锁<br><br>}else {<br><br>} //如果不能获取锁，则直接做其他事情</th>
  </tr>
</table>


lockInterruptibly()⽅法⽐较特殊，当通过这个⽅法去获取锁时，如果线程正在等待获取 锁，则这个线程能够响应中断，即中断线程的等待状态。也就使说，当两个线程同时通过 lock.lockInterruptibly()想获取某个锁时，假若此时线程A获取到了锁，⽽线程B只有在等待， 那么对线程B调⽤threadB.interrupt()⽅法能够中断线程B的等待过程。

由于lockInterruptibly()的声明中抛出了异常，所以lock.lockInterruptibly()必须放在try 块中或者在调⽤lockInterruptibly()的⽅法外声明抛出InterruptedException。

因此lockInterruptibly()⼀般的使⽤形式如下：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br></th>
    <th>public void method() throws Interrupt edException {<br><br>lock.lockInterruptibly(); try{<br><br>}//..... finally {<br><br>lock.unlock(); }<br><br>}</th>
  </tr>
</table>


注意，当⼀个线程获取了锁之后，是不会被interrupt()⽅法中断的。因为本身在前⾯的⽂章 中讲过单独调⽤interrupt()⽅法不能中断正在运⾏过程中的线程，只能中断阻塞过程中的线程。

因此当通过lockInterruptibly()⽅法获取某个锁时，如果不能获取到，只有进⾏等待的情况 下，是可以响应中断的。

⽽⽤synchronized修饰的话，当⼀个线程处于等待某个锁的状态，是⽆法被中断的，只有⼀ 直等待下去。

## 2.ReentrantLock

ReentrantLock，意思是“可重⼊锁”，关于可重⼊锁的概念在下⼀节讲述。ReentrantLock 是唯⼀实现了Lock接⼝的类，并且ReentrantLock提供了更多的⽅法。下⾯通过⼀些实例看具体 看⼀下如何使⽤ReentrantLock。

例⼦1，lock()的正确使⽤⽅法

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br><br>0<br>1<br>2<br>3<br><br><br>34</th>
    <th>public class Test {<br><br>private ArrayList<Integer> arrayList = new ArrayList<Integer> ();<br><br>public static void main(String[] args) {<br><br>final Test test = new Test();<br><br>new Thread(){ public void run() {<br><br>test.insert(Thread.cu rrentThread());<br><br>}; }.start();<br><br>new Thread(){ public void run() {<br><br>test.insert(Thread.cu rrentThread());<br><br>}; }.start();<br><br>} public void insert(Thread<br><br>thread) { Lock lock<br><br>= new ReentrantLock(); //注意这个地 ⽅<br><br>lock try .{lock(); System.out.println(thread<br><br>.getName()+"得到了锁"); for(int i=0;i<5;i++) { arrayList.add(i);<br><br>} ca }tch (Exception e) {<br><br>// TODO: handle exception }finally {<br><br>System.out.println(thread<br><br>.getName()+"释放了锁");<br><br>lock.unlock(); }<br><br>} }</th>
  </tr>
</table>


各位朋友先想⼀下这段代码的输出结果是什么？

![image 1](assets/imageFile1.png)

View Code

也许有朋友会问，怎么会输出这个结果？第⼆个线程怎么会在第⼀个线程释放锁之前得到了 锁？原因在于，在insert⽅法中的lock变量是局部变量，每个线程执⾏该⽅法时都会保存⼀个副 本，那么理所当然每个线程执⾏到lock.lock()处获取的是不同的锁，所以就不会发⽣冲突。

知道了原因改起来就⽐较容易了，只需要将lock声明为类的属性即可。

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br><br>0<br>1<br>2<br>3<br><br><br>34</th>
    <th>public class Test {<br><br>private ArrayList<Integer> arrayList = new ArrayList<Integer> ();<br><br>private Lock lock<br><br>= new ReentrantLock(); //注意这个 地⽅<br><br>public static void main(String[] args) {<br><br>final Test test = new Test();<br><br>new Thread(){ public void run() {<br><br>test.insert(Thread.cu rrentThread());<br><br>}; }.start();<br><br>new Thread(){ public void run() {<br><br>test.insert(Thread.cu rrentThread());<br><br>}; }.start();<br><br>} public void insert(Thread<br><br>thread) {<br><br>lock try .{lock(); System.out.println(thread<br><br>.getName()+"得到了锁"); for(int i=0;i<5;i++) { arrayList.add(i);<br><br>} ca }tch (Exception e) {<br><br>// TODO: handle exception }finally {<br><br>System.out.println(thread<br><br>.getName()+"释放了锁");<br><br>lock.unlock(); }<br><br>} }</th>
  </tr>
</table>


这样就是正确地使⽤Lock的⽅法了。

例⼦2，tryLock()的使⽤⽅法

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br><br><br>37</th>
    <th>public class Test {<br><br>private ArrayList<Integer> arrayList = new ArrayList<Integer> ();<br><br>private Lock lock<br><br>= new ReentrantLock(); //注意这个 地⽅<br><br>public static void main(String[] args) {<br><br>final Test test = new Test();<br><br>new Thread(){ public void run() {<br><br>test.insert(Thread.cu rrentThread());<br><br>}; }.start();<br><br>new Thread(){ public void run() {<br><br>test.insert(Thread.cu rrentThread());<br><br>}; }.start();<br><br>} public void insert(Thread<br><br>thread) { if(lock.tryLock()) { try {<br><br>System.out.println(thr ead.getName()+"得到了锁");<br><br>for(int i=0;i<5;i++) {<br><br>arrayList.add(i); }<br><br>} catch (Exception e) { // TODO: handle exception<br><br>}finally{<br><br>System.out.println(thr ead.getName()+"释放了锁");<br><br>lock.unlock();<br><br>} el }se { System.out.println(thread<br><br>.getName()+"获取锁失败");<br><br>} }<br><br>}</th>
  </tr>
</table>


输出结果：

![image 2](assets/imageFile2.png)

View Code

例⼦3，lockInterruptibly()响应中断的使⽤⽅法：

public class Test { private Lock lock

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9


= new ReentrantLock(); public static void main(String[]

args) { Test test = new Test(); MyThread thread1

= new MyThread(test); MyThread thread2

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


= new MyThread(test);

thread1.start(); thread2.start();

try { Thread.sleep(2000);

} catch (InterruptedException e) {

19

} e.printStackTrace(); thread2.interrupt();

- 0
- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8


} public void insert(Thread

thread) throws InterruptedException{

lock.lockInterruptibly(); // 注意，如果需要正确中断等待锁的线程，必须将 获取锁放在外⾯，然后将 InterruptedException抛出

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


try { System.out.println(thread

.getName()+"得到了锁"); long startTime = System.currentTimeMillis(); for( ; ;) {

if(System.currentTime Millis() - startTime >=

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


Integer.MAX_VALUE) break; //插⼊数据

} } finally {

System.out.println(Threa d.currentThread().getName()+"执⾏ finally");

49

0 51

lock.unlock(); System.out.println(thread

.getName()+"释放了锁");

} }

}

class MyThread extends Thread { private Test test = null;

public thisM.testyThread= test(Test; test) { }

@Opuvberridlic veoid run() {

try { test.insert(Thread.curren

tThread()) } ca; tch (InterruptedException e) {

System.out.println(Threa d.currentThread().getName()+"被中 断");

} }

}

运⾏之后，发现thread2能够被正确中断。

## 3.ReadWriteLock

ReadWriteLock也是⼀个接⼝，在它⾥⾯只定义了两个⽅法：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br><br><br>15</th>
    <th>public interface ReadWriteLock {<br><br>/***Returns the lock used for reading* .<br><br>* @return the lock used for<br><br>readingLo*/ck. readLock();<br><br>/***Returns the lock used for writing* .<br><br>* @return the lock used for<br><br>writingLo*/ck. writeLock(); }</th>
  </tr>
</table>


⼀个⽤来获取读锁，⼀个⽤来获取写锁。也就是说将⽂件的读写操作分开，分成2个锁来分 配给线程，从⽽使得多个线程可以同时进⾏读操作。下⾯的ReentrantReadWriteLock实现了 ReadWriteLock接⼝。

## 4.ReentrantReadWriteLock

ReentrantReadWriteLock⾥⾯提供了很多丰富的⽅法，不过最主要的有两个⽅法： readLock()和writeLock()⽤来获取读锁和写锁。

下⾯通过⼏个例⼦来看⼀下ReentrantReadWriteLock具体⽤法。

假如有多个线程要同时进⾏读操作的话，先看⼀下synchronized达到的效果：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br><br><br>28</th>
    <th>public class Test { private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();<br><br>public static void main(String[] args) {<br><br>final Test test = new Test();<br><br>new Thread(){ public void run() {<br><br>test.get(Thread.curren tThread());<br><br>}; }.start();<br><br>new Thread(){ public void run() {<br><br>test.get(Thread.curren tThread());<br><br>}; }.start();<br><br>} public synchronized void get(Thre<br><br>ad thread) {<br><br>System.cur longrentTimeMillis();start =<br><br>while(System.currentTimeMill is() - start <= 1) {<br><br>System.out.println(thread<br><br>.getName()+"正在进⾏读操作"); } System.out.println(thread.get<br><br>Name()+"读操作完毕");<br><br>} }</th>
  </tr>
</table>


这段程序的输出结果会是，直到thread1执⾏完读操作之后，才会打印thread2执⾏读操作 的信息。

![image 3](assets/imageFile3.png)

View Code

⽽改成⽤读写锁的话：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br><br>0<br>1<br>2<br>3<br><br><br>34</th>
    <th>public class Test { private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();<br><br>public static void main(String[] args) {<br><br>final Test test = new Test();<br><br>new Thread(){ public void run() {<br><br>test.get(Thread.curren tThread());<br><br>}; }.start();<br><br>new Thread(){ public void run() {<br><br>test.get(Thread.curren tThread());<br><br>}; }.start();<br><br>} public void get(Thread thread) {<br><br>rwl.readLock().lock(); try {<br><br>long start = System.currentTimeMillis();<br><br>while(System.currentTimeM illis() - start <= 1) {<br><br>System.out.println(thr<br><br>ead.getName()+"正在进⾏读操作"); } System.out.println(thread<br><br>.getName()+"读操作完毕"); } finally {<br><br>rwl.readLock().unlock(); }<br><br>} }</th>
  </tr>
</table>


此时打印的结果为：

![image 4](assets/imageFile4.png)

View Code

说明thread1和thread2在同时进⾏读操作。

这样就⼤⼤提升了读操作的效率。

不过要注意的是，如果有⼀个线程已经占⽤了读锁，则此时其他线程如果要申请写锁，则申 请写锁的线程会⼀直等待释放读锁。

如果有⼀个线程已经占⽤了写锁，则此时其他线程如果申请写锁或者读锁，则申请的线程会 ⼀直等待释放写锁。

关于ReentrantReadWriteLock类中的其他⽅法感兴趣的朋友可以⾃⾏查阅API⽂档。

## 5.Lock和synchronized的选择

总结来说，Lock和synchronized有以下⼏点不同：

- 1）Lock是⼀个接⼝，⽽synchronized是Java中的关键字，synchronized是内置的语⾔实

现；

- 2）synchronized在发⽣异常时，会⾃动释放线程占有的锁，因此不会导致死锁现象发⽣；

⽽Lock在发⽣异常时，如果没有主动通过unLock()去释放锁，则很可能造成死锁现象，因此使⽤ Lock时需要在finally块中释放锁；

- 3）Lock可以让等待锁的线程响应中断，⽽synchronized却不⾏，使⽤synchronized时，

等待的线程会⼀直等待下去，不能够响应中断；

- 4）通过Lock可以知道有没有成功获取锁，⽽synchronized却⽆法办到。

- 5）Lock可以提⾼多个线程进⾏读操作的效率。


在性能上来说，如果竞争资源不激烈，两者的性能是差不多的，⽽当竞争资源⾮常激烈时 （即有⼤量线程同时竞争），此时Lock的性能要远远优于synchronized。所以说，在具体使⽤时 要根据适当情况选择。

# 三.锁的相关概念介绍

在前⾯介绍了Lock的基本使⽤，这⼀节来介绍⼀下与锁相关的⼏个概念。

1.可重⼊锁

如果锁具备可重⼊性，则称作为可重⼊锁。像synchronized和ReentrantLock都是可重⼊ 锁，可重⼊性在我看来实际上表明了锁的分配机制：基于线程的分配，⽽不是基于⽅法调⽤的分 配。举个简单的例⼦，当⼀个线程执⾏到某个synchronized⽅法时，⽐如说method1，⽽在 method1中会调⽤另外⼀个synchronized⽅法method2，此时线程不必重新去申请锁，⽽是可 以直接执⾏⽅法method2。

看下⾯这段代码就明⽩了：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br></th>
    <th>class MyClass { public synchronized void method1<br><br>() { method2(); } public synchronized void method2<br><br>() {<br><br>} }</th>
  </tr>
</table>


上述代码中的两个⽅法method1和method2都⽤synchronized修饰了，假如某⼀时刻，线 程A执⾏到了method1，此时线程A获取了这个对象的锁，⽽由于method2也是synchronized⽅ 法，假如synchronized不具备可重⼊性，此时线程A需要重新申请锁。但是这就会造成⼀个问 题，因为线程A已经持有了该对象的锁，⽽⼜在申请获取该对象的锁，这样就会线程A⼀直等待永 远不会获取到的锁。

⽽由于synchronized和Lock都具备可重⼊性，所以不会发⽣上述现象。

2.可中断锁

可中断锁：顾名思义，就是可以相应中断的锁。

在Java中，synchronized就不是可中断锁，⽽Lock是可中断锁。

如果某⼀线程A正在执⾏锁中的代码，另⼀线程B正在等待获取该锁，可能由于等待时间过 ⻓，线程B不想等待了，想先处理其他事情，我们可以让它中断⾃⼰或者在别的线程中中断它，这 种就是可中断锁。

在前⾯演示lockInterruptibly()的⽤法时已经体现了Lock的可中断性。

3.公平锁

公平锁即尽量以请求锁的顺序来获取锁。⽐如同是有多个线程在等待⼀个锁，当这个锁被释 放时，等待时间最久的线程（最先请求的线程）会获得该所，这种就是公平锁。

⾮公平锁即⽆法保证锁的获取是按照请求锁的顺序进⾏的。这样就可能导致某个或者⼀些线 程永远获取不到锁。

在Java中，synchronized就是⾮公平锁，它⽆法保证等待的线程获取锁的顺序。

⽽对于ReentrantLock和ReentrantReadWriteLock，它默认情况下是⾮公平锁，但是可以 设置为公平锁。

看⼀下这2个类的源代码就清楚了：

![image 5](assets/imageFile5.png)

在ReentrantLock中定义了2个静态内部类，⼀个是NotFairSync，⼀个是FairSync，分别 ⽤来实现⾮公平锁和公平锁。

我们可以在创建ReentrantLock对象时，通过以下⽅式来设置锁的公平性：

<table>
  <tr>
    <th>1</th>
    <th>ReentrantLock lock<br><br>= new ReentrantLock(true);</th>
  </tr>
</table>


如果参数为true表示为公平锁，为fasle为⾮公平锁。默认情况下，如果使⽤⽆参构造器，则 是⾮公平锁。

![image 6](assets/imageFile6.png)

另外在ReentrantLock类中定义了很多⽅法，⽐如：

isFair() //判断锁是否是公平锁

isLocked() //判断锁是否被任何线程获取了

isHeldByCurrentThread() //判断锁是否被当前线程获取了

hasQueuedThreads() //判断是否有线程在等待该锁

在ReentrantReadWriteLock中也有类似的⽅法，同样也可以设置为公平锁和⾮公平锁。不 过要记住，ReentrantReadWriteLock并未实现Lock接⼝，它实现的是ReadWriteLock接⼝。

4.读写锁

读写锁将对⼀个资源（⽐如⽂件）的访问分成了2个锁，⼀个读锁和⼀个写锁。

正因为有了读写锁，才使得多个线程之间的读操作不会发⽣冲突。

ReadWriteLock就是读写锁，它是⼀个接⼝，ReentrantReadWriteLock实现了这个接⼝。

可以通过readLock()获取读锁，通过writeLock()获取写锁。

上⾯已经演示过了读写锁的使⽤⽅法，在此不再赘述。

参考资料：

http://blog.csdn.net/ns_code/article/details/17487337

http://houlinyan.iteye.com/blog/1112535

http://ifeve.com/locks/

http://ifeve.com/read-write-locks/

http://blog.csdn.net/fancyerii/article/details/6783224

http://blog.csdn.net/ghsau/article/details/7461369/

http://blog.csdn.net/zhaozhenzuo/article/details/37109015
