# RentrantLock的实现原理

本篇主要写下⾯⼏个东⻄：

什么是AQS RentrantLock的使⽤ RentrantLock的原理

公平锁 尝试获取锁 线程进⼊等待队列 阻塞等待线程 释放锁 中断锁

⾮公平锁 总结

# 什么是AQS

AQS即是AbstractQueuedSynchronizer，⼀个⽤来构建锁和同步⼯具的框架，包括常⽤的 RentrantLock、CountDownLatch、Semaphore等。 AQS没有锁之类的概念，它有个state变量，是个int类型，在不同场合有着不同含义。本⽂研究的是 锁，为了好理解，姑且先把state当成锁。 AQS围绕state提供两种基本操作“获取”和“释放”，有条双向队列存放阻塞的等待线程，并提供⼀系列 判断和处理⽅法，简单说⼏点：

state是独占的，还是共享的； state被获取后，其他线程需要等待； state被释放后，唤醒等待线程； 线程等不及时，如何退出等待。

⾄于线程是否可以获得state，如何释放state，就不是AQS关⼼的了，要由⼦类具体实现。 直接分析AQS的代码会⽐较难明⽩，所以结合⼦类RentrantLock来分析。AQS的功能可以分为独占和 共享，RentrantLock实现了独占功能，是本⽂分析的⽬标。

ReentrantLock的使⽤

Lock lock = new RentranLock(); lock.lock(); try{

/do something }finaly{

lock.unlock(); }

RentrantLock实现了Lock接⼝，加锁和解锁都需要显式写出，注意⼀定要在适当时候unlock。 和synchronized相⽐，RentrantLock⽤起来会复杂⼀些。在基本的加锁和解锁上，两者是⼀样的，所 以⽆特殊情况下，推荐使⽤synchronized。RentrantLock的优势在于它更灵活、更强⼤，增加了轮 训、超时、中断等⾼级功能。

# ReentrantLock的原理

公平锁和⾮公平锁 public RentrantLock() {

sync = new NonfairSync(); }

public RentrantLock(bolean fair) {

sync = fair ? new FairSync() : new NonfairSync(); }

RentrantLock的内部类Sync继承了AQS，分为公平锁FairSync和⾮公平锁NonfairSync。

公平锁：线程获取锁的顺序和调⽤lock的顺序⼀样，FIFO； ⾮公平锁：线程获取锁的顺序和调⽤lock的顺序⽆关，全凭运⽓。

RentrantLock默认使⽤⾮公平锁是基于性能考虑，公平锁为了保证线程规规矩矩地排队，需要增加阻 塞和唤醒的时间开销。如果直接插队获取⾮公平锁，跳过了对队列的处理，速度会更快。

## 公平锁

尝试获取锁

final void lock() { acquire(1);}

public final void acquire(int arg) {

if (!tryAcquire(arg) & acquireQueued(adWaiter(Node.EXCLUSIVE), arg) selfInterupt();

}

先来看公平锁的实现，lock⽅法很简单的⼀句话调⽤AQS的acquire⽅法： protected bolean tryAcquire(int arg) {

throw new UnsuportedOperationException(); }

AQS的tryAcquire未做具体实现，因为是否获取锁成功是由⼦类决定的，我们直接来看RentrantLock 的tryAcquire的实现。 protected final bolean tryAcquire(int acquires) {

final Thread curent = Thread.curentThread(); int c = getState(); if (c = 0) {

if (!hasQueuedPredecesors() & compareAndSetState(0, acquires) { setExclusiveOwnerThread(curent); return true;

}

} else if (curent = getExclusiveOwnerThread() {

int nextc = c + acquires; if (nextc < 0)

throw new Eror("Maximum lock count exceded"); setState(nextc); return true;

} return false;

}

获取锁成功分为两种情况，第⼀个if判断AQS的state是否等于0，表示锁没有⼈占有。接着， hasQueuedPredecesors判断队列是否有排在前⾯的线程在等待锁，没有的话调⽤ compareAndSetState使⽤cas的⽅式修改state，传⼊的acquires写死是1。最后线程获取锁成功， setExclusiveOwnerThread将线程记录为独占锁的线程。 第⼆个if判断当前线程是否为独占锁的线程，因为RentrantLock是可重⼊的，线程可以不停地lock来 增加state的值，对应地需要unlock来解锁，直到state为零。 如果最后获取锁失败，下⼀步需要将线程加⼊到等待队列。

### 线程进⼊等待队列

AQS内部有⼀条双向的队列存放等待线程，节点是Node对象。每个Node维护了线程、前后Node的指 针和等待状态等参数。 线程在加⼊队列之前，需要包装进Node，调⽤⽅法是adWaiter： private Node adWaiter(Node mode) {

Node node = new Node(Thread.curentThread(), mode); / Try the fast path of enq; backup to ful enq on failure

Node pred = tail; if (pred != nul) {

node.prev = pred; if (compareAndSetTail(pred, node) {

pred.next = node; return node;

}

} enq(node); return node;

}

每个Node需要标记是独占的还是共享的，由传⼊的mode决定，RentrantLock⾃然是使⽤独占模式 Node.EXCLUSIVE。 创建好Node后，如果队列不为空，使⽤cas的⽅式将Node加⼊到队列尾。注意，这⾥只执⾏了⼀次修 改操作，并且可能因为并发的原因失败。因此修改失败的情况和队列为空的情况，需要进⼊enq。

private Node enq(final Node node) {

for (;) { Node t = tail; if (t = nul) { / Must initialize

if (compareAndSetHead(new Node( ) tail = head;

} else { node.prev = t; if (compareAndSetTail(t, node) {

t.next = node; return t;

} }

} }

enq是个死循环，保证Node⼀定能插⼊队列。注意到，当队列为空时，会先为头节点创建⼀个空的 Node，因为头节点代表获取了锁的线程，现在还没有，所以先空着。

### 阻塞等待线程

线程加⼊队列后，下⼀步是调⽤acquireQueued阻塞线程。

final bolean acquireQueued(final Node node, int arg) { bolean failed = true; try {

bolean interupted = false; for (;) { /1 final Node p = node.predecesor(); if (p = head & tryAcquire(arg) {

setHead(node); p.next = nul; / help GC failed = false; return interupted;

} /2

if (shouldParkAfterFailedAcquire(p, node) & parkAndCheckInterupt() interupted = true;

} } finaly { if (failed)

cancelAcquire(node); }

}

- 标记1是线程唤醒后尝试获取锁的过程。如果前⼀个节点正好是head，表示⾃⼰排在第⼀位，可以⻢上 调⽤tryAcquire尝试。如果获取成功就简单了，直接修改⾃⼰为head。这步是实现公平锁的核⼼，保证 释放锁时，由下个排队线程获取锁。（看到线程解锁时，再看回这⾥啦）

- 标记2是线程获取锁失败的处理。这个时候，线程可能等着下⼀次获取，也可能不想要了，Node变量 waitState描述了线程的等待状态，⼀共四种情况： static final int CANCELED = 1; /取 消 static final int SIGNAL = -1; /下个 节 点 需 要被 唤 醒 static final int CONDITION = -2; /线 程 在 等 待 条 件 触 发 static final int PROPAGATE = -3; /（ 共 享 锁 ） 状 态 需 要 向后 传 播


shouldParkAfterFailedAcquire传⼊当前节点和前节点，根据前节点的状态，判断线程是否需要阻塞。

private static bolean shouldParkAfterFailedAcquire(Node pred, Node node) { int ws = pred.waitStatus; if (ws = Node.SIGNAL)

return true; if (ws > 0) {

do {

node.prev = pred = pred.prev; } while (pred.waitStatus > 0); pred.next = node;

} else { compareAndSetWaitStatus(pred, ws, Node.SIGNAL);

} return false;

}

前节点状态是SIGNAL时，当前线程需要阻塞； 前节点状态是CANCELED时，通过循环将当前节点之前所有取消状态的节点移出队列； 前节点状态是其他状态时，需要设置前节点为SIGNAL。

如果线程需要阻塞，由parkAndCheckInterupt⽅法进⾏操作。 private final bolean parkAndCheckInterupt() {

LockSuport.park(this); return Thread.interupted();

}

parkAndCheckInterupt使⽤了LockSuport，和cas⼀样，最终使⽤UNSAFE调⽤Native⽅法实现线程 阻塞（LockSuport的park和unpark⽅法作⽤类似于wait和notify）。最后返回线程唤醒后的中断状 态，关于中断，后⽂会分析。 到这⾥总结⼀下获取锁的过程：线程去竞争⼀个锁，可能成功也可能失败。成功就直接持有资源，不 需要进⼊队列；失败的话进⼊队列阻塞，等待唤醒后再尝试竞争锁。

### 释放锁

通过上⾯详细的获取锁过程分析，释放锁过程⼤概可以猜到：头节点是获取锁的线程，先移出队列， 再通知后⾯的节点获取锁。 public void unlock() { sync.release(1); }

RentrantLock的unlock⽅法很简单地调⽤了AQS的release： public final bolean release(int arg) {

if (tryRelease(arg) { Node h = head; if (h != nul & h.waitStatus != 0)

unparkSucesor(h); return true;

} return false;

}

和lock的tryAcquire⼀样，unlock的tryRelease同样由RentrantLock实现： protected final bolean tryRelease(int releases) {

int c = getState() - releases; if (Thread.curentThread() != getExclusiveOwnerThread()

throw new IlegalMonitorStateException(); bolean fre = false; if (c = 0) {

fre = true; setExclusiveOwnerThread(nul);

} setState(c); return fre;

}

因为锁是可以重⼊的，所以每次lock会让state加1，对应地每次unlock要让state减1，直到为0时将独 占线程变量设置为空，返回标记是否彻底释放锁。 最后，调⽤unparkSucesor将头节点的下个节点唤醒：

private void unparkSucesor(Node node) { int ws = node.waitStatus; if (ws < 0)

compareAndSetWaitStatus(node, ws, 0);

Node s = node.next; if (s = nul | s.waitStatus > 0) {

s = nul; for (Node t = tail; t != nul & t != node; t = t.prev)

if (t.waitStatus <= 0) s = t;

} if (s != nul)

LockSuport.unpark(s.thread); }

寻找下个待唤醒的线程是从队列尾向前查询的，找到线程后调⽤LockSuport的unpark⽅法唤醒线 程。被唤醒的线程重新执⾏acquireQueued⾥的循环，就是上⽂关于acquireQueued标记1部分，线程 重新尝试获取锁。

### 中断锁

static void selfInterupt() {

Thread.curentThread().interupt(); }

在acquire⾥还有最后⼀句代码调⽤了selfInterupt，功能很简单，对当前线程产⽣⼀个中断请求。 为什么要这样操作呢？因为LockSuport.park阻塞线程后，有两种可能被唤醒。

- 1.
- 2.


第⼀种情况，前节点是头节点，释放锁后，会调⽤LockSuport.unpark唤醒当前线程。整个过程 没有涉及到中断，最终acquireQueued返回false时，不需要调⽤selfInterupt。 第⼆种情况，LockSuport.park⽀持响应中断请求，能够被其他线程通过interupt()唤醒。但这种 唤醒并没有⽤，因为线程前⾯可能还有等待线程，在acquireQueued的循环⾥，线程会再次被阻 塞。parkAndCheckInterupt返回的是Thread.interupted()，不仅返回中断状态，还会清除中断 状态，保证阻塞线程忽略中断。最终acquireQueued返回true时，真正的中断状态已经被清除，需 要调⽤selfInterupt维持中断状态。

因此普通的lock⽅法并不能被其他线程中断，RentrantLock是可以⽀持中断，需要使⽤ lockInteruptibly。

两者的逻辑基本⼀样，不同之处是parkAndCheckInterupt返回true时，lockInteruptibly直接throw new InteruptedException()。

⾮公平锁

分析完公平锁的实现，还剩下⾮公平锁，主要区别是获取锁的过程不同。 final void lock() {

if (compareAndSetState(0, 1) /如 果 ⼀ 开 始 未 上 锁 ， 直 接 抢 占 锁

setExclusiveOwnerThread(Thread.curentThread(); else

acquire(1); }

在NonfairSync的lock⽅法⾥，第⼀步直接尝试将state修改为1，很明显，这是抢先获取锁的过程。如 果修改state失败，则和公平锁⼀样，调⽤acquire。 final bolean nonfairTryAcquire(int acquires) {

final Thread curent = Thread.curentThread(); int c = getState(); if (c = 0) {

if (compareAndSetState(0, acquires) { setExclusiveOwnerThread(curent); return true;

}

} else if (curent = getExclusiveOwnerThread() {

int nextc = c + acquires; if (nextc < 0) / overflow

throw new Eror("Maximum lock count exceded"); setState(nextc); return true;

} return false;

}

nonfairTryAcquire和tryAcquire乍⼀看⼏乎⼀样，差异只是缺少调⽤hasQueuedPredecesors。这点体 验出公平锁和⾮公平锁的不同，公平锁会关注队列⾥排队的情况，⽼⽼实实按照FIFO的次序；⾮公平 锁只要有机会就抢占，才不管排队的事。

# 总结

#### 从RentrantLock的实现完整分析了AQS的独占功能，总的来讲并不复杂。别忘了AQS还有共享功能， 下⼀篇是–分析CountDownLatch的实现原理。 最后附⼀张流程图⽅便代码梳理，点击看⼤图

![image 1](<ReentrantLock的实现原理.note_images/imageFile1.png>)

![image 3](<ReentrantLock的实现原理.note_images/imageFile3.png>)

