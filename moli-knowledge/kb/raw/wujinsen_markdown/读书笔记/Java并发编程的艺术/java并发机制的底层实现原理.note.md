volatile是轻量级的synchronized，它在多处理器开发中保证了共享变量的"可⻅性"。可⻅性是说当⼀ 个线程修改⼀个共享变量时，另外⼀个线程能读到这个修改的值。 volatile的定义和实现原理 java语⾔规范第三版中对volatile的定义如下：Java编程语⾔允许线程访问共享变量，为了共享变量能 被准确和⼀致的更新，线程应该确保通过排它锁单独获得这个变量。

instance = new Singleton();/instance是volatile共享变量 有volatile变量修饰的共享变量在进⾏写操作时会多出第⼆⾏汇编代码，其中包含lock前缀指令

![image 1](<java并发机制的底层实现原理.note_images/imageFile1.png>)

volatile两条实现原理：

- 1.Lock前缀指令会引起处理器缓存会写到内存
- 2.⼀个处理器的缓存会写到内存，会导致其他处理器的缓存⽆效 处理器不会和内存通信，因为速度问题，是先将系统内存的数据读到内部缓存在操作，操作完不知何 时回写回系统内存。 如果使⽤volatile的变量进⾏写操作，JVM会发送lock前缀指令，将变量所在缓存⾏的数据写回系统内 存。


volatile的使⽤优化 队列集合类LinkedTransferQueue，在使⽤volatile变量时，追加64字节的⽅式来优化队列出队和⼊队 的性能。

synchronized的实现原理和应⽤ 普通同步⽅法，锁是当前实例对象 静态同步⽅法，锁是当前类的Clas对象 同步⽅法块，锁是synchronized括号⾥配置的对象

