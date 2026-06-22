# ⼀、CAS简介

CAS：Compare and Swap, 翻译成⽐较并交换。 java.util.concurrent包中借助CAS实现了区别于synchronouse同步锁的⼀种乐观锁，使⽤这些类在多核 CPU的机器上会有⽐较好的性能.

CAS有3个操作数，内存值V，旧的预期值A，要修改的新值B。当且仅当预期值A和内存值V相同时， 将内存值V修改为B，否则什么都不做。

今天我们主要是针对AtomicInteger的incrementAndGet做深⼊分析。

# ⼆、JAVA实现部分

Java代码

![image 1](<深入理解并发之CompareAndSet(CAS).note_images/imageFile1.png>)

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


/**

- * Atomically increments by one the current value.

*

- * @return the updated value

- */


public final int incrementAndGet() {

for (;;) { int current = get(); int next = current + 1; if (compareAndSet(current, next))

return next; }

}

循环的内容是

- 1.取得当前值

- 2.计算+1后的值

- 3.如果当前值没有被覆盖的话设置那个+1后的值

- 4.如果设置没成功, 再从1开始


在这个⽅法中可以看到compareAndSet这个⽅法，我们进⼊看⼀下。

Java代码

![image 2](<深入理解并发之CompareAndSet(CAS).note_images/imageFile2.png>)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


/**

- * Atomically sets the value to the given updated value

- * if the current value {@code ==} the expected value.

*

- * @param expect the expected value

- * @param update the new value

- * @return true if successful. False return indicates that


- 8.
- 9.
- 10.
- 11.
- 12.


- * the actual value was not equal to the expected value.

- */


public final boolean compareAndSet(int expect, int update) {

return unsafe.compareAndSwapInt(this, valueOffset, expect, update); }

调⽤UnSafe这个类的compareAndSwapInt

Java代码

![image 3](<深入理解并发之CompareAndSet(CAS).note_images/imageFile3.png>)

1.

public final native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);

JAVA程序也就跟踪到这⾥为⽌了，剩下的就是通过JNI调⽤C程序了，可是我奇怪的是为什么变量名都 是var1，var2这样的命名呢？JAVA编程规范不是说不使⽤1，2等没有含义的字符命名吗？

# 三、JNI原⽣实现部分

在openJDK中找到找到unsafe.cpp这个⽂件，代码如下：

Java代码

![image 4](<深入理解并发之CompareAndSet(CAS).note_images/imageFile4.png>)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


UNSAFE_ENTRY(jboolean, Unsafe_CompareAndSwapInt(JNIEnv *env, jobject unsafe, jobject obj, jlong offset, jint e, jint x))

UnsafeWrapper("Unsafe_CompareAndSwapInt"); oop p = JNIHandles::resolve(obj); jint* addr = (jint *) index_oop_from_field_offset_long(p, offset); return (jint)(Atomic::cmpxchg(x, addr, e)) == e;

UNSAFE_END

核⼼⽅法是compxchg，这个⽅法所属的类⽂件是在OS_CPU⽬录下⾯，由此可以看出这个类是和CPU 操作有关，进⼊代码如下：

Java代码

![image 5](<深入理解并发之CompareAndSet(CAS).note_images/imageFile5.png>)

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


inline jint Atomic::cmpxchg (jint exchange_value, volatile jint* dest, jint c ompare_value) {

// alternative for InterlockedCompareExchange int mp = os::is_MP(); __asm {

mov edx, dest mov ecx, exchange_value mov eax, compare_value LOCK_IF_MP(mp) cmpxchg dword ptr [edx], ecx

} }

这个⽅法⾥⾯都是汇编指命，看到LOCK_IF_MP也有锁指令实现的原⼦操作，其实CAS也算是有锁操 作，只不过是由CPU来触发，⽐synchronized性能好的多。

