# 1.ThreadLocal的使⽤场景

- 1.1 场景1 每个线程需要⼀个独享对象（通常是⼯具类，典型需要使⽤的类有SimpleDateFormat和Random） 每个Thread内有⾃⼰的实例副本，不共享 ⽐喻：教材只有⼀本，⼀起做笔记有线程安全问题。复印后没有问题，使⽤ThradLocal相当于复印了教材。

ThreadLocal

- 1.2 场景2 每个线程内需要保存全局变量（例如在拦截器中获取⽤户信息），可以让不同⽅法直接使⽤，避免参数传递 的麻烦


⼀个故事讲明⽩线程的私家领地：ThreadLocal 深⼊理解 ThreadLocal (这些细节不应忽略)

# 2.对以上场景的实践

- 2.1 实践场景1 /**


- * 两个线程打印⽇期

- */


- public class ThreadLocalNormalUsage00 {


public static void main(String[] args) throws InterruptedException {

new Thread(new Runnable() { @Override public void run() {

String date = new ThreadLocalNormalUsage00().date(10); System.out.println(date);

} }).start();

new Thread(new Runnable() { @Override public void run() {

String date = new ThreadLocalNormalUsage00().date(104707); System.out.println(date);

} }).start();

}

public String date(int seconds) {

//参数的单位是毫秒，从1970.1.1 00:00:00 GMT 开始计时 Date date = new Date(1000 * seconds); SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); return dateFormat.format(date);

} }

运⾏结果

![image 1](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile1.png>)

因为中国位于东⼋区，所以时间从1970年1⽉1⽇的8点开始计算的

/**

- * 三⼗个线程打印⽇期

- */


- public class ThreadLocalNormalUsage01 {


public static void main(String[] args) throws InterruptedException {

for (int i = 0; i < 30; i++) { int finalI = i; new Thread(new Runnable() {

@Override public void run() {

String date = new ThreadLocalNormalUsage01().date(finalI); System.out.println(date);

} }).start(); //线程启动后，休眠100ms Thread.sleep(100);

} }

public String date(int seconds) {

//参数的单位是毫秒，从1970.1.1 00:00:00 GMT 开始计时 Date date = new Date(1000 * seconds); SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); return dateFormat.format(date);

} }

运⾏结果

![image 2](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile2.png>)

多个线程打印⾃⼰的时间（如果线程超级多就会产⽣性能问题），所以要使⽤线程池。

/**

- * 1000个线程打印⽇期，⽤线程池来执⾏

- */


- public class ThreadLocalNormalUsage02 {


public static ExecutorService threadPool = Executors.newFixedThreadPool(10);

public static void main(String[] args) throws InterruptedException {

for (int i = 0; i < 1000; i++) { int finalI = i; //提交任务 threadPool.submit(new Runnable() {

@Override public void run() {

String date = new ThreadLocalNormalUsage02().date(finalI); System.out.println(date);

} });

} threadPool.shutdown();

}

public String date(int seconds) {

//参数的单位是毫秒，从1970.1.1 00:00:00 GMT 开始计时 Date date = new Date(1000 * seconds); SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); return dateFormat.format(date);

}

}

运⾏结果

![image 3](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile3.png>)

但是使⽤线程池时就会发现每个线程都有⼀个⾃⼰的SimpleDateFormat对象，没有必要，所以将 SimpleDateFormat声明为静态，保证只有⼀个

/**

- * 1000个线程打印⽇期，⽤线程池来执⾏，出现线程安全问题

- */


- public class ThreadLocalNormalUsage03 {


public static ExecutorService threadPool = Executors.newFixedThreadPool(10); //只创建⼀次 SimpleDateFormat 对象，避免不必要的资源消耗 static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

public static void main(String[] args) throws InterruptedException {

for (int i = 0; i < 1000; i++) { int finalI = i; //提交任务 threadPool.submit(new Runnable() {

@Override public void run() {

String date = new ThreadLocalNormalUsage03().date(finalI); System.out.println(date);

} });

} threadPool.shutdown();

}

public String date(int seconds) {

//参数的单位是毫秒，从1970.1.1 00:00:00 GMT 开始计时 Date date = new Date(1000 * seconds); return dateFormat.format(date);

} }

运⾏结果 出现了秒数相同的打印结果，这显然是不正确的。

![image 4](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile4.png>)

出现问题的原因

![image 5](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile5.png>)

多个线程的task指向了同⼀个SimpleDateFormat对象，SimpleDateFormat是⾮线程安全的。

## 解决问题的⽅案

- ⽅案1：加锁 格式化代码是在最后⼀句return dateFormat.format(date);,所以可以为最后⼀句代码添加 synchronized锁 public String date(int seconds) {

//参数的单位是毫秒，从1970.1.1 00:00:00 GMT 开始计时 Date date = new Date(1000 * seconds); String s; synchronized (ThreadLocalNormalUsage04.class) {

s = dateFormat.format(date);

} return s;

}

运⾏结果

运⾏结果中没有发现相同的时间，达到了线程安全的⽬的 缺点：因为添加了synchronized，所以会保证同⼀时间只有⼀条线程可以执⾏，这在⾼并发场景下肯定不是 ⼀个好的选择，所以看看其他⽅案吧。

- ⽅案2：使⽤ThreadLocal /**


![image 6](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile6.png>)

- * 利⽤ ThreadLocal 给每个线程分配⾃⼰的 dateFormat 对象

- * 不但保证了线程安全，还⾼效的利⽤了内存

- */


- public class ThreadLocalNormalUsage05 {


public static ExecutorService threadPool = Executors.newFixedThreadPool(10);

public static void main(String[] args) throws InterruptedException {

for (int i = 0; i < 1000; i++) { int finalI = i; //提交任务 threadPool.submit(new Runnable() {

@Override public void run() {

String date = new ThreadLocalNormalUsage05().date(finalI); System.out.println(date);

} });

} threadPool.shutdown();

}

public String date(int seconds) {

//参数的单位是毫秒，从1970.1.1 00:00:00 GMT 开始计时 Date date = new Date(1000 * seconds); //获取 SimpleDateFormat 对象 SimpleDateFormat dateFormat = ThreadSafeFormatter.dateFormatThreadLocal.get(); return dateFormat.format(date);

} }

class ThreadSafeFormatter {

public static ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>(){

//创建⼀份 SimpleDateFormat 对象 @Override protected SimpleDateFormat initialValue() {

return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); }

}; }

运⾏结果

![image 7](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile7.png>)

使⽤了ThreadLocal后不同的线程不会有共享的 SimpleDateFormat 对象，所以也就不会有线程安全问题

- 2.2 实践场景2 当前⽤户信息需要被线程内的所有⽅法共享


- ⽅案1：传递参数

可以将user作为参数在每个⽅法中进⾏传递， 缺点：但是这样做会产⽣代码冗余问题，并且可维护性差。

- ⽅案2：使⽤Map 对此进⾏改进的⽅案是使⽤⼀个Map，在第⼀个⽅法中存储信息，后续需要使⽤直接get()即可，


![image 8](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile8.png>)

![image 9](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile9.png>)

缺点：如果在单线程环境下可以保证安全，但是在多线程环境下是不可以的。如果使⽤加锁和 ConcurentHashMap都会产⽣性能问题。

- ⽅案3：使⽤ThreadLocal，实现不同⽅法间的资源共享 使⽤ ThreadLocal 可以避免加锁产⽣的性能问题，也可以避免层层传递参数来实现业务需求，就可以实现不 同线程中存储不同信息的要求。


![image 10](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile10.png>)

/**

- * 演示 ThreadLocal 的⽤法2：避免参数传递的麻烦

- */


- public class ThreadLocalNormalUsage06 { public static void main(String[] args) {


new Service1().process(); }

}

- class Service1 {


public void process() { User user = new User("鲁毅"); //将User对象存储到 holder 中 UserContextHolder.holder.set(user); new Service2().process();

}

}

- class Service2 {

public void process() { User user = UserContextHolder.holder.get(); System.out.println("Service2拿到⽤户名: " + user.name); new Service3().process();

} }

- class Service3 {


public void process() { User user = UserContextHolder.holder.get(); System.out.println("Service3拿到⽤户名: " + user.name);

} }

class UserContextHolder {

public static ThreadLocal<User> holder = new ThreadLocal<>(); }

class User {

String name;

public User(String name) {

this.name = name; }

}

运⾏结果

![image 11](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile11.png>)

# 3.对ThreadLocal的总结

让某个需要⽤到的对象实现线程之间的隔离（每个线程都有⾃⼰独⽴的对象）

可以在任何⽅法中轻松的获取到该对象

根据共享对象⽣成的时机选择使⽤initialValue⽅法还是set⽅法

对象初始化的时机由我们控制的时候使⽤initialValue ⽅式

如果对象⽣成的时机不由我们控制的时候使⽤ set ⽅式

- 4.使⽤ThreadLocal的好处

- 5.ThreadLocal原理


达到线程安全的⽬的

不需要加锁，执⾏效率⾼ 更加节省内存，节省开销 免去传参的繁琐，降低代码耦合度

![image 12](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile12.png>)

Thread

ThreadLocal

ThreadLocalMap

在Thread类内部有有ThreadLocal.ThreadLocalMap threadLocals = nul;这个变量，它⽤于存储 ThreadLocal，因为在同⼀个线程当中可以有多个ThreadLocal，并且多次调⽤get()所以需要在内部维护⼀个 ThreadLocalMap⽤来存储多个ThreadLocal

- 5.1 ThreadLocal相关⽅法 T initialValue() 该⽅法⽤于设置初始值，并且在调⽤get()⽅法时才会被触发，所以是懒加载。


但是如果在get()之前进⾏了set()操作，这样就不会调⽤initialValue()。 通常每个线程只能调⽤⼀次本⽅法，但是调⽤了remove()后就能再次调⽤

public T get() { Thread t = Thread.currentThread(); ThreadLocalMap map = getMap(t);

//获取到了值直接返回resule if (map != null) {

ThreadLocalMap.Entry e = map.getEntry(this); if (e != null) {

@SuppressWarnings("unchecked") T result = (T)e.value; return result;

}

} //没有获取到才会进⾏初始化 return setInitialValue();

}

private T setInitialValue() { //获取initialValue⽣成的值，并在后续操作中进⾏set，最后将值返回 T value = initialValue(); Thread t = Thread.currentThread(); ThreadLocalMap map = getMap(t); if (map != null)

map.set(this, value); else

createMap(t, value); return value;

}

public void remove() { ThreadLocalMap m = getMap(Thread.currentThread()); if (m != null)

m.remove(this); }

void set(T t) 为这个线程设置⼀个新值

public void set(T value) { Thread t = Thread.currentThread(); ThreadLocalMap map = getMap(t); if (map != null)

map.set(this, value); else

createMap(t, value); }

T get()

获取线程对应的value

public T get() { Thread t = Thread.currentThread(); ThreadLocalMap map = getMap(t); if (map != null) {

ThreadLocalMap.Entry e = map.getEntry(this); if (e != null) {

@SuppressWarnings("unchecked") T result = (T)e.value; return result;

}

} return setInitialValue();

}

void remove() 删除对应这个线程的值

# 6.ThreadLocal注意点

- 6.1 内存泄漏 内存泄露；某个对象不会再被使⽤，但是该对象的内存却⽆法被收回


![image 13](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile13.png>)

static class ThreadLocalMap {

static class Entry extends WeakReference<ThreadLocal<?>> { /** The value associated with this ThreadLocal. */ Object value;

Entry(ThreadLocal<?> k, Object v) { //调⽤⽗类，⽗类是⼀个弱引⽤ super(k); //强引⽤ value = v;

} }

强引⽤：当内存不⾜时触发GC，宁愿抛出 OM也不会回收强引⽤的内存 弱引⽤：触发GC后便会回收弱引⽤的内存 正常情况 当Thread运⾏结束后，ThreadLocal中的value会被回收，因为没有任何强引⽤了 ⾮正常情况 当Thread⼀直在运⾏始终不结束，强引⽤就不会被回收，存在以下调⽤链 Thread-->ThreadLocalMap->Entry(key为null)-->value因为调⽤链中的 value 和 Thread 存在强引⽤，所以value⽆法被回收，就

有可能出现 OM。 JDK的设计已经考虑到了这个问题，所以在set()、remove()、resize()⽅法中会扫描到key为nul的Entry，并 且把对应的value设置为nul，这样value对象就可以被回收。

private void resize() { Entry[] oldTab = table; int oldLen = oldTab.length; int newLen = oldLen * 2; Entry[] newTab = new Entry[newLen]; int count = 0;

for (int j = 0; j < oldLen; ++j) { Entry e = oldTab[j]; if (e != null) {

ThreadLocal<?> k = e.get(); //当ThreadLocal为空时，将ThreadLocal对应的value也设置为null if (k == null) {

e.value = null; // Help the GC

} else { int h = k.threadLocalHashCode & (newLen - 1); while (newTab[h] != null)

h = nextIndex(h, newLen); newTab[h] = e; count++;

} }

}

setThreshold(newLen); size = count; table = newTab;

}

但是只有在调⽤set()、remove()、resize()这些⽅法时才会进⾏这些操作，如果没有调⽤这些⽅法并且线程不 停⽌，那么调⽤链就会⼀直存在，所以可能会发⽣内存泄漏。

- 6.2 如何避免内存泄漏（阿⾥规约） 调⽤remove()⽅法，就会删除对应的Entry对象，可以避免内存泄漏，所以使⽤完ThreadLocal后，要调⽤ remove()⽅法。

- class Service1 {

public void process() { User user = new User("鲁毅"); //将User对象存储到 holder 中 UserContextHolder.holder.set(user); new Service2().process();

} }

- class Service2 {

public void process() { User user = UserContextHolder.holder.get(); System.out.println("Service2拿到⽤户名: " + user.name); new Service3().process();

} }

- class Service3 {


public void process() { User user = UserContextHolder.holder.get(); System.out.println("Service3拿到⽤户名: " + user.name); //⼿动释放内存，从⽽避免内存泄漏 UserContextHolder.holder.remove();

} }

- 6.3 ThreadLocal的空指针异常问题 /**


- * ThreadLocal的空指针异常问题

- */


public class ThreadLocalNPE {

ThreadLocal<Long> longThreadLocal = new ThreadLocal<>();

public void set() {

longThreadLocal.set(Thread.currentThread().getId()); }

public Long get() {

return longThreadLocal.get(); }

public static void main(String[] args) {

ThreadLocalNPE threadLocalNPE = new ThreadLocalNPE();

//如果get⽅法返回值为基本类型，则会报空指针异常，如果是包装类型就不会出错 System.out.println(threadLocalNPE.get());

Thread thread1 = new Thread(new Runnable() { @Override public void run() {

threadLocalNPE.set(); System.out.println(threadLocalNPE.get());

}

}); thread1.start();

} }

- 6.4 空指针异常问题的解决 如果get⽅法返回值为基本类型，则会报空指针异常，如果是包装类型就不会出错。这是因为基本类型和包装 类型存在装箱和拆箱的关系，造成空指针问题的原因在于使⽤者。

- 6.5 共享对象问题 如果在每个线程中ThreadLocal.set()进去的东⻄本来就是多个线程共享的同⼀对象，⽐如static对象，那么多 个线程调⽤ThreadLocal.get()获取的内容还是同⼀个对象，还是会发⽣线程安全问题。

- 6.6 可以不使⽤ThreadLocal就不要强⾏使⽤ 如果在任务数很少的时候，在局部⽅法中创建对象就可以解决问题，这样就不需要使⽤ThreadLocal。

- 6.7 优先使⽤框架的⽀持，⽽不是⾃⼰创造 例如在Spring框架中，如果可以使⽤RequestContextHolder，那么就不需要⾃⼰维护ThreadLocal，因为⾃ ⼰可能会忘记调⽤remove()⽅法等，造成内存泄漏。


![image 14](<使用 ThreadLocal 一次解决老大难问题.note_images/imageFile14.png>)

欢迎在留⾔区留下你的观点，⼀起讨论提⾼。如果今天的⽂章让你有新的启发，学习能⼒的提升上有 新的认识，欢迎转发分享给更多⼈。 欢迎各位读者加⼊程序员⼩乐技术群，在公众号后台回复“加群”或者“学习”即可。

