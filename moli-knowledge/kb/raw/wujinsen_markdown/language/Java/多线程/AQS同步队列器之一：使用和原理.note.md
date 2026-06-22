JDK1.5之前都是通过synchronized关键字实现并发同步，⽽JDK1.5以后Doug Lea⼤师开发了curent包 下的类，通过Java代码实现了synchronized关键字的语义。

然⽽在curent包下的这些类的实现⼤部分都离不开⼀个基础组件 AQS(AbstractQueuedSynchronizer)也就是同步队列器。

AQS，AbstractQueuedSynchronizer，即队列同步器。它是构建锁或者其他同步组件的基础框

架（如RentrantLock、RentrantReadWriteLock、Semaphore等），JUC并发包的作者期望它能够 成为实现⼤部分同步需求的基础。它是JUC并发包中的核⼼基础组件。AQS解决了⼦类实现同步器时 涉及当的⼤量细节问题，例如获取同步状态、FIFO同步队列。基于AQS来构建同步器可以带来很多好 处。它不仅能够极⼤地减少实现⼯作，⽽且也不必处理在多个位置上发⽣的竞争问题。在基于AQS构 建的同步器中，只能在⼀个时刻发⽣阻塞，从⽽降低上下⽂切换的开销，提⾼了吞吐量。同时在设计 AQS时充分考虑了可伸缩⾏，因此J.U.C中所有基于AQS构建的同步器均可以获得这个优势。

AQS的主要使⽤⽅式是继承，⼦类通过继承同步器并实现它的抽象⽅法来管理同步状态。

# ⼆、简单使⽤示例

在使⽤AQS基础组件前，先了解⼀下内部的基本的⽅法，这些⽅法可以分为两类： 第⼀类：⼦类实现的⽅法，AQS不作处理（模板⽅法）

tryAcquire(int arg)：独占式的获取锁，返回值是bolean类型的，true代表获取锁， false代表获取失败。

tryRelease(int arg)：释放独占式同步状态，释放操作会唤醒其后继节点获取同步状 态。

tryAcquireShared(int arg)：共享式的获取同步状态，返回⼤于0代表获取成功，否 则就是获取失败。

tryReleaseShared(int arg)：共享式的释放同步状态。 isHeldExclusively()：判断当前的线程是否已经获取到了同步状态。

这些⽅法是⼦类实现时必须实现的⽅法，通过上⾯的这些⽅法来判断是否获取了锁，然后再通过AQS 本身的⽅法执⾏获取锁与未获取锁的过程。

第⼆类：AQS本身的实现的⽅法，定义给⼦类通⽤实现的⽅法

acquire(int arg)：独占式的获取锁操作，独占式获取同步状态都调⽤这个⽅法，通 过⼦类实现的tryAcquire⽅法判断是否获取了锁。

acquireShared(int arg)：共享式的获取锁操作，在读写锁中⽤到，通过 tryAcquireShared⽅法判断是否获取到了同步状态。

release(int arg)：独占式的释放同步状态，通过tryRelease⽅法判断是否释放了独占 式同步状态。

releaseShared(int arg)：共享式的释放同步状态，通过tryReleaseShared⽅法判断 是否已经释放了共享同步状态。

## 从这两类⽅法可以看出，AQS为⼦类定义了⼀套获取锁和释放锁以后的操作，⽽具体的如何判断是否 获取锁和释放锁都是交由不同的⼦类⾃⼰去实现其中的逻辑，这也是Java设计模式之⼀：模板模式的 实现。有了AQS我们就可以实现⼀个属于⾃⼰的Lock，下⾯就是⼀个AQS源码层的⼀个Demo：

<table>
  <tr>
    <th>![image 1](<AQS同步队列器之一：使用和原理.note_images/imageFile1.png>)</th>
  </tr>
</table>


- 1 public class Muteximplements Lock,java.io.Serializable{ //内部⾃定义实现的队列同步器

- 2 private static class Syncextends AbstractQueuedSynchronizer{ //判断是否同步状态已经被占⽤了

- 3 protected boolean isHeldExclusively(){

- 4 return getState() = 1;

- 5 }

- 6 /获取锁的操作

- 7 public boolean tryAcquire(int acquires){

- 8 if(compareAndSetState(0,1)){//CAS操作获取锁状态

- 9 setExclusiveOwnerThread(Thread.currentThread());//将当前线程设置为

获取同步状态的线程

- 10 return true;

- 11 }

- 12 return false;

- 13 } //释放锁操作

- 14 protected boolean tryRelease(int releases){

- 15 if(getState() = 0){//当前同步状态值为0代表已经释放

- 16 setExclusiveOwnerThread(null);

- 17 setState(0);

- 18 return true;

- 19 }

- 20 }

- 21 public void lock(){ sync.acquire(1);}//最终调⽤AQS中的acquire⽅法

- 22 public boolean tryLock(){return sync.tryAcquire(1);}

- 23 public void unlock(){ sync.release(1);}

- 24 public Bolean isLocked(){return sync.isHeldExclusively();}

- 25 }


<table>
  <tr>
    <th>![image 2](<AQS同步队列器之一：使用和原理.note_images/imageFile2.png>)</th>
  </tr>
</table>


上⾯的Mutex是⾃定义实现的⼀个独占式锁，通过tryAcquire操作判断线程是否获取到了同步状态，这 个⽅法是Mutex⾃身实现的⼀个⽅法。通过tryRelease⽅法判断是否释放了同步状态。通过⼦类⾃定义 实现获取和释放的操作最终调⽤AQS中的⽅法实现锁操作。

# 三、源码分析以及原理

![image 3](<AQS同步队列器之一：使用和原理.note_images/imageFile3.png>)

AQS类结构 从图中可以看出来，AbstractQueuedSynchronizer内部维护了⼀个Node节点类和⼀个 ConditionObject内部类。Node内部类是⼀个双向的FIFO队列，⽤来保存阻塞中的线程以及获取同步 状态的线程，⽽ConditionObject对应的是下⼀篇要讲的Lock中的等待和通知机制。

![image 4](<AQS同步队列器之一：使用和原理.note_images/imageFile4.png>)

node类结构

![image 5](<AQS同步队列器之一：使用和原理.note_images/imageFile5.png>)

同步队列 除了Node节点的这个FIFO队列，还有⼀个重要的概念就是waitStatus⼀个volatile关键字修饰的节点等 待状态。在AQS中waitstatus有五种值：

SIGNAL 值为-1、后继节点的线程处于等待的状态、当前节点的线程如果释放了同步状态或者 被取消、会通知后继节点、后继节点会获取锁并执⾏（当⼀个节点的状态为SIGNAL时就意味着在等待 获取同步状态，前节点是头节点也就是获取同步状态的节点）

CANCELED 值为1、因为超时或者中断，结点会被设置为取消状态，被取消状态的结点不应 该去竞争锁，只能保持取消状态不变，不能转换为其他状态。处于这种状态的结点会被踢出队列，被 GC回收（⼀旦节点状态值为1说明被取消，那么这个节点会从同 步队列中删除）

CONDITION 值为-2、节点在等待队列中、节点线程等待在Condition、当其它线程对 Condition调⽤了singal()⽅法该节点会从等待队列中移到同步队列中

PROPAGATE 值为-3、表示下⼀次共享式同步状态获取将会被⽆条件的被传播下去（读写锁 中存在的状态，代表后续还有资源，可以多个线程同时拥有同步状态） initial 值为0、表示当前没有线程获取锁（初始状态）

了解了节点等待的状态以及同步队列的作⽤，AQS中还通过了⼀个volatile关键字修饰的status对象⽤ 来管理锁的状态并提供了getState()、setState()、compareAndSetStatus()三个⽅法改变status的状 态。知道了这些就可以开始真正看AQS是如何处理没有获取锁的线程的。在真正了解底层实现AQS之 前还要介绍⼀下独占锁和共享锁：

独占锁：在同⼀个时刻只能有⼀个线程获得同步状态，⼀旦这个线程获取同步状态，其它线 程就⽆法再获取将会进⼊阻塞的状态。

共享锁：在同⼀个时刻可以存在多个线程获取到同步状态。 接下来就从源码的⻆度了解AQS中的锁操作机制： acquire(int arg)：独占式的获取锁，此⽅法不响应中断，在这过程中中断，线程不会从同步队列中移 除也不会⽴⻢中断

<table>
  <tr>
    <th>![image 6](<AQS同步队列器之一：使用和原理.note_images/imageFile6.png>)</th>
  </tr>
</table>


- 1 public final void acquire(int arg){

- 2 if(!tryAcquire(arg) &acquireQueued(addWaiter(Node.EXCLUSIVE))){

- 3 selfInterupt();//如果这个过程中出现中断，在整个过程结束后再⾃我中断

- 4 }

- 5 }


<table>
  <tr>
    <th>![image 7](<AQS同步队列器之一：使用和原理.note_images/imageFile7.png>)</th>
  </tr>
</table>


acquire⽅法代码很少，但是它做了很多事，⾸先前⾯介绍过tryAcquire()⽅法是⼦类实现的具体获取锁 的⽅法，当锁获取到了就会⽴刻退出if条件也就代表获取锁具体的就是啥也不⼲。那么看锁获取失败具 体⼲了啥呢。⾸先是adWaiter(Node.EXCLUSIVE)⽅法

adWaiter(Node mode)：往同步队列中添加元素

<table>
  <tr>
    <th>![image 8](<AQS同步队列器之一：使用和原理.note_images/imageFile8.png>)</th>
  </tr>
</table>


- 1 private Node addWaiter(Node mode){

- 2 //通过当前线程和锁模式创建了⼀个Node节点

- 3 Node node = new Node(Thread.currentThread(),mode);

- 4 //获取尾节点

- 5 Node pred = tail;

- 6 if(pred != null){

- 7 node.prev = pred;//新增的节点每次都是加在同步队列的尾部

- 8 //通过CAS操作设置尾节点防⽌线程不安全

- 9 if(compareAndSetTail(pred,node)){

- 10 pred.next = node;

- 11 return node;

- 12 }

- 13 }

- 14 enq(node);//防⽌CAS操作失败，再次处理

- 15 return node;

- 16 }


<table>
  <tr>
    <th>![image 9](<AQS同步队列器之一：使用和原理.note_images/imageFile9.png>)</th>
  </tr>
</table>


adWaiter⽅法主要做的就是创建⼀个节点，如果通过CAS操作成功就直接将节点加⼊同步队列的尾 部，否则需要enq⽅法的帮忙再次进⾏处理。设置尾节点的操作必须是CAS类型的，因为会有多个线程 同时去获取同步状态防⽌并发不安全。

![image 10](<AQS同步队列器之一：使用和原理.note_images/imageFile10.png>)

添加到队列尾节点操作 enq(Node node)：在adWaiter⽅法处理失败的时候进⼀步进⾏处理

<table>
  <tr>
    <th>![image 11](<AQS同步队列器之一：使用和原理.note_images/imageFile11.png>)</th>
  </tr>
</table>


- 1 private Node enq(final Node node){

- 2 //死循环【发现很多的底层死循环都是这么写不知道是不是有什么优化点】

- 3 for(;;){

- 4 Node t =tail;

- 5 if(t =null){//如果尾节点为null

- 6 if(compareAndSetHead(new Node( ){//创建⼀个新的节点并添加到队列中初始化

- 7 tail =head;

- 8 }else{

- 9 node.prev =t;

- 10 //还是通过CAS操作添加到尾部

- 11 if(compareAndSetTail(t,node)){

- 12 t.next =node;

- 13 return t;

- 14 }

- 15 }

- 16 }

- 17 }

- 18 }


<table>
  <tr>
    <th>![image 12](<AQS同步队列器之一：使用和原理.note_images/imageFile12.png>)</th>
  </tr>
</table>


enq⽅法就是通过死循环，不断的通过CAS操作设置尾节点，直到添加成功才返回。

acquireQueued(final Node node,int arg)：当线程获取锁失败并加⼊同步队列以后，就进⼊了⼀个⾃ 旋的状态，如果获取到了这个状态就退出阻塞状态否则就⼀直阻塞

<table>
  <tr>
    <th>![image 13](<AQS同步队列器之一：使用和原理.note_images/imageFile13.png>)</th>
  </tr>
</table>


- 1 final boolean acquireQueued(final Node node,int arg){

- 2 boolean failed =true;//⽤来判断是否获取了同步状态

- 3 try{

- 4 boolean interupted =false;//判断⾃旋过程中是否被中断过

- 5 for(;;){

- 6 final Node p = node.predecesor();//获取前继节点

- 7 if(p = head & tryAcquire(arg){//如果当前的这个节点的前继节点是头节点就去尝试获取了同步

状态

- 8 setHead(node);//设为头节点

- 9 p.next =null;

- 10 failed =false;//代表获取了同步状态

- 11 return interrupted;

- 12 }

- 13 //判断⾃⼰是否已经阻塞了检查这个过程中是否被中断过

- 14 if(shouldParkAfterFailedAcquire(p,node) &parkAndCheckInterrupt() ){

- 15 interupted =true;

- 16 }

- 17 }finally{

- 18 if(failed){

- 19 cancelAcquired(node);

- 20 }

- 21 }

- 22 }

- 23 }


<table>
  <tr>
    <th>![image 14](<AQS同步队列器之一：使用和原理.note_images/imageFile14.png>)</th>
  </tr>
</table>


acquireQueued⽅法主要是让线程通过⾃旋的⽅式去获取同步状态，当然也不是每个节点都有获取的 资格，因为是FIFO先进先出队列，acquireQueued⽅法保证了只有头节点的后继节点才有资格去获取 同步状态，如果线程可以休息了就让该线程休息然后记录下这个过程中是否被中断过，当线程获取了 同步状态就会从这个同步队列中移除这个节点。同时还会设置获取同步状态的线程为头节点，在设置 头节点的过程中不需要任何的同步操作，因为独占式锁中能获取同步状态的必定是同⼀个线程。

![image 15](<AQS同步队列器之一：使用和原理.note_images/imageFile15.png>)

设置头节点操作

![image 16](<AQS同步队列器之一：使用和原理.note_images/imageFile16.png>)

同步队列中节点⾃旋操作 shouldParkAfterFailedAcquire(Node node,Node node)：判断⼀个线程是否阻塞

<table>
  <tr>
    <th>![image 17](<AQS同步队列器之一：使用和原理.note_images/imageFile17.png>)</th>
  </tr>
</table>


- 1 private static boolean shouldPArkAfterFailedAcquire(Node pred,Node node){

- 2 int ws = pred.waitStatus;//获取节点的等待状态

- 3 if(ws = Node.SIGNAL){//如果是SIGNAL就代表当头节点释放后，这个节点就会去尝试获取状态

- 4 return true;//代表阻塞中

- 5 }

- 6 if(ws > 0){//代表前继节点放弃了

- 7 do {

- 8 node.prev = pred = pred.prev;//循环不停的往前找知道找到节点的状态是正常的

- 9 }while(pred.waitStatus > 0);

- 10 pred.next =node;

- 11 }else{

- 12 compareAndSetWaitStatus(pred,ws,Node.SIGNAL);//通过CAS操作设置状态为SIGNAL

- 13 }

- 14 return false;

- 15 }


<table>
  <tr>
    <th>![image 18](<AQS同步队列器之一：使用和原理.note_images/imageFile18.png>)</th>
  </tr>
</table>


整个流程中，如果前驱结点的状态不是SIGNAL，那么⾃⼰就不能安⼼去休息，也就是只有当前驱节点 为SIGNAL时这个线程才可以进⼊等待状态。

parkAndCheckInterupt()：前⾯的⽅法是判断是否阻塞，⽽这个⽅法就是真正的执⾏阻塞的⽅法同时 返回中断状态

1 private final boolean parkAndCheckInterupt(){ 3 LockSuport.park(this);//阻塞当前线程 5 return Thread.interupted();//返回中断状态 7 }

经过了上⾯的这么多⽅法，再次回头看acquire⽅法的时候。会发现其实整个流程也没有想象中的那么 难以理解。acquire⽅法流程

⾸先通过⼦类判断是否获取了锁，如果获取了就什么也不⼲。 如果没有获取锁、通过线程创建节点加⼊同步队列的队尾。 当线程在同步队列中不断的通过⾃旋去获取同步状态，如果获取了锁，就把其设为同步队列

中的头节点，否则在同步队列中不停的⾃旋等待获取同步状态。

如果在获取同步状态的过程中被中断过最后⾃⾏调⽤interupted⽅法进⾏中断操作。 这⾥可以看⼀下acquire也就是独占式获取锁的整个流程

![image 19](<AQS同步队列器之一：使用和原理.note_images/imageFile19.png>)

## AQS之aquire独占式获取锁流程 release(int arg)：独占式的释放锁

<table>
  <tr>
    <th>![image 20](<AQS同步队列器之一：使用和原理.note_images/imageFile20.png>)</th>
  </tr>
</table>


1 public final boolean release(int arg){

- 3 if(tryRelease(arg){//⼦类⾃定义实现

- 4 Node h =head;

- 5 if(h !=null & h.waitStatus != 0){

- 6 unparkSucesor(h);//唤醒下⼀个节点

- 7 }

- 8 return true;

- 9 }

- 10 return false;

- 11 }


<table>
  <tr>
    <th>![image 21](<AQS同步队列器之一：使用和原理.note_images/imageFile21.png>)</th>
  </tr>
</table>


释放锁的流程很简单，⾸先⼦类⾃定义的⽅法如果释放了同步状态，如果头节点不为空并且头节点的 等待状态不为0就唤醒其后继节点。主要依赖的就是⼦类⾃定义实现的释放操作。

unparkSucesor(Node node)：唤醒后继节点获取同步状态

<table>
  <tr>
    <th>![image 22](<AQS同步队列器之一：使用和原理.note_images/imageFile22.png>)</th>
  </tr>
</table>


- 1 private void unparkSuccessor(Node node){

- 2 //获取头节点的状态

- 3 int ws =node.waitStatus;

- 4 if(ws < 0){

- 5 compareAndSetWaitStatus(node,ws,0);//通过CAS将头节点的状态设置为初始状态

- 6 }

- 7 Node s = node.next;//后继节点

- 8 if(s =null | s.waitStatus >0){//不存在或者已经取消

- 9 s =null;

- 10 for(Node t = tail;t !=null & t != node;t = t.prev){//从尾节点开始往前遍历，寻找离头节点最近的等 待状态正常的节点

- 11 if(t.waitStatus <= 0){

- 12 s =t;

- 13 }

- 14 }

- 15 }

- 16 if(s !=null){

- 17 LockSuport.unpark(s.thread);//真正的唤醒操作

- 18 }

- 19 }


<table>
  <tr>
    <th>![image 23](<AQS同步队列器之一：使用和原理.note_images/imageFile23.png>)</th>
  </tr>
</table>


唤醒操作，通过判断后继节点是否存在，如果不存在就寻找等待时间最⻓的适合的节点将其唤醒唤醒 操作通过LockSuport中的unpark⽅法唤醒底层也就是unsafe类的操作。 以上就是独占式的获取锁以及释放锁的过程总结的来说：线程获取锁，如果获取了锁就啥也不⼲，如 果没获取就创造⼀个节点通过compareAndSetTail(CAS操作)操作的⽅式将创建的节点加⼊同步队列的 尾部，在同步队列中的节点通过⾃旋的操作不断去获取同步状态【当然由于FIFO先进先出的特性】等 待时间越⻓就越先被唤醒。当头节点释放同步状态的时候，⾸先查看是否存在后继节点，如果存在就 唤醒⾃⼰的后继节点，如果不存在就获取等待时间最⻓的符合条件的线程。

acquireShared(int arg)：共享式的获取锁

<table>
  <tr>
    <th>![image 24](<AQS同步队列器之一：使用和原理.note_images/imageFile24.png>)</th>
  </tr>
</table>


- 1 public final void acquireShared(int arg){

- 2 //⼦类⾃定义实现的获取状态【也就是当返回为>=0的时候就代表获取锁】

- 3 if(tryAcquireShared(arg) < 0){

- 4 doAcquiredShared(arg);//具体的处理没有获取锁的线程的⽅法

- 5 }

- 6 }


<table>
  <tr>
    <th>![image 25](<AQS同步队列器之一：使用和原理.note_images/imageFile25.png>)</th>
  </tr>
</table>


## doAcquiredShared(int arg)：处理未获取同步状态的线程

<table>
  <tr>
    <th>![image 26](<AQS同步队列器之一：使用和原理.note_images/imageFile26.png>)</th>
  </tr>
</table>


- 1 private void doAcquire(int arg){

- 2 final Node node = addWaiter(Node.SHARED);//创建⼀个节点加⼊同步队列尾部

- 3 boolean failed = true;//判断获取状态

- 4 try{

- 5 boolean interrupted = false;//是否被中断过

- 6 for(;;){

- 7 final Node p =node.predecessor();//获取前驱节点

- 8 if(p == head){

- 9 int r = tryAcquireShared(arg);//获取同步状态

- 10 if(r >= 0 ){//⼤于0代表获取到了

- 11 setHeadAndPropagate(node,r);//设置为头节点并且如果有多余资 源⼀并唤醒

- 12 p.next = null;

- 13 if(interrupted){

- 14 selfInterrupted();//⾃我中断

- 15 }

- 16 failed = false;

- 17 return;

- 18 }

- 19 }

- 20 //判断线程是否可以进⾏休息如果可以休息就调⽤park⽅法

- 21 if(shouldParkAfterFailedAcquire(p,node) && parkAndCheckInterrupt()){

- 22 interrupted = true;

- 23 } }

- 24 }finally{

- 25 if(failed){

- 26 cancelAcquire(node);

- 27 }

- 28 }

- 29 }


<table>
  <tr>
    <th>![image 27](<AQS同步队列器之一：使用和原理.note_images/imageFile27.png>)</th>
  </tr>
</table>


共享式获取锁和独占式唯⼀的区别在于setHeadAndPropagate这个⽅法，独占式的锁会去判断是否为 后继节点，只有后继节点才有资格在头节点释放了同步状态以后获取到同步状态⽽共享式的实现依靠 着setHeadAndPropagate这个⽅法

setHeadAndPorpagate(Node node,int arg)：获取共享同步状态以后的操作

<table>
  <tr>
    <th>![image 28](<AQS同步队列器之一：使用和原理.note_images/imageFile28.png>)</th>
  </tr>
</table>


- 1 private void setHeadAndPropaGate(Node node,int propagate){

- 2 Node h =head;

- 3 setHead(node);//设置为头节点

- 4 if(propagate >0| h =null | h.waitStatus < 0){//⼤于0代表还有其他资源⼀并可以唤醒

- 5 Node s = node.next;//下⼀个节点

- 6 if(s =null | s.isShared()){

- 7 doReleaseShared();

- 8 }

- 9 }

- 10 }


<table>
  <tr>
    <th>![image 29](<AQS同步队列器之一：使用和原理.note_images/imageFile29.png>)</th>
  </tr>
</table>


这个⽅法主要的⽬的就是将获取到同步状态的节点设置为头节点、如果存在多个资源就将多个资源⼀ 并唤醒

doReleaseShared()：唤醒后继节点

<table>
  <tr>
    <th>![image 30](<AQS同步队列器之一：使用和原理.note_images/imageFile30.png>)</th>
  </tr>
</table>


- 1 private void doReleaseShared(int arg){

- 2 for(;;){

- 3 Node h =head;

- 4 if(h !=null & h !=tail){

- 5 int ws = h.waitStatus;//获取头节点的等待状态

- 6 if(!compareAndSetWaitStatus(h,Node.SIGNAL,0){//设置不成功就⼀直进⾏设置

- 7 continue;

- 8 }

- 9 unparkSucesor(h);//唤醒后继节点

- 10 }else if (ws = 0 &!compareAndSetWaitStatus(h, 0, Node.PROPAGATE))

- 11 continue;

- 12 }

- 13 if (h =head)

- 14 break;

- 15 }


<table>
  <tr>
    <th>![image 31](<AQS同步队列器之一：使用和原理.note_images/imageFile31.png>)</th>
  </tr>
</table>


OK，⾄此，共享式的获取锁也研究过了。让我们再梳理⼀下它的流程

- 1.
- 2.


tryAcquireShared()尝试获取资源，成功则直接返回； 失败则通过doAcquireShared()进⼊同步队列中，直到头节点释放同步状态后唤醒后继节点并成功 获取到资源才返回。整个等待过程也是忽略中断的。

其实跟acquire()的流程⼤同⼩异，只不过多了个⾃⼰拿到资源后，还会去唤醒后继队友的操作（这才 是共享嘛）

releaseShared()：释放共享同步状态

<table>
  <tr>
    <th>![image 32](<AQS同步队列器之一：使用和原理.note_images/imageFile32.png>)</th>
  </tr>
</table>


- 1 public final boolean releaseShared(int arg){

- 2 //⼦类⾃定义释放锁操作true代表释放

- 3 if(tryReleaseShared(arg)){

- 4 doReleaseShared();//处理释放的操作

- 5 return true;

- 6 }

- 7 }


<table>
  <tr>
    <th>![image 33](<AQS同步队列器之一：使用和原理.note_images/imageFile33.png>)</th>
  </tr>
</table>


通过⼦类⾃定义实现的释放锁操作判断，如果未释放就什么也不⼲，⽽doReleased⽅法就是去唤醒当 前的后继节点

# 四、总结

AQS在并发中是⼀个⾮常重要的基础类，它定义了很多同步组件需要的⽅法。通过这些⽅法开发 者可以简单的实现⼀个相关的锁。我们详解了独占和共享两种模式下获取-释放资源(acquirerelease、acquireShared-releaseShared)的源码，相信⼤家都有⼀定认识了。值得注意的是， acquire()和acquireSahred()两种⽅法下，线程在等待队列中都是忽略中断的。AQS也⽀持响应中断 的，acquireInteruptibly()/acquireSharedInteruptibly()即是，这⾥相应的源码跟acquire()和 acquireSahred()差不多，这⾥就简单阐述⼀下。

对于响应中断的获取同步状态操作⽽⾔：其会判断获取同步状态的线程是否处于被中断的状 态，如果处于被中断的操作就会抛出InteruptedException异常

对于超时响应的获取同步状态⽽⾔：内部多了⼀个时间判断。其实这些都是在最基础的获取锁 上做了⼀些加强基本的原理还是相同的。

===================================================================

===============

不管岁⽉⾥经历多少⾟酸和艰难，告诉⾃⼰风⾬本⾝就是⼀种内涵，努⼒的⾯对，不过就是⼀场命运 的漂流，既然在路上，那么⽬的地必然也就是前⽅。

