原⼦更新基本类型

AtomicInteger AtomicLong AtomicBolean

简单例⼦: public class AtomicIntegerTest {

static AtomicInteger ai = new AtomicInteger(1); public static void main(String[] a r g s ) {

System.o u t .println(ai.getAndIncrement());//原⼦性⾃增1，返回⾃增前的值 System.o u t .println(ai.get()); System.o u t .println(ai.getAndSet(5)); System.o u t .println(ai.get()); System.o u t .println(ai.getAndAdd(6)); System.o u t .println(ai.get());

} }

public ﬁnal int getAndIncrement() {

return u n s a f e .getAndAddInt(this, v a l u e O ﬀ s e t , 1); }

public ﬁnal int getAndAddInt(Object var1, long var2, int var4) { int var5; do {

var5 = this.getIntVolatile(var1, var2); } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));

return var5; }

public ﬁnal native boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);

原⼦更新数组

原⼦更新引⽤类型

原⼦更新字段类

