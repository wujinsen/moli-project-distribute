>号外：往期⾯试题，10篇为⼀个单位归置到本公众号菜单栏->⾯试题，有需要的欢迎翻阅。

我们知道，ConcurentHashmap(1.8)这个并发集合框架是线程安全的，当你看到源码的get操作时，会发现 get操作全程是没有加任何锁的，这也是这篇博⽂讨论的问题⸺为什么它不需要加锁呢？ ConcurentHashMap的简介

我想有基础的同学知道在jdk1.7中是采⽤Segment + HashEntry + RentrantLock的⽅式进⾏实现的，⽽1.8中放弃了Segment臃肿的设计，取⽽代 之的是采⽤Node + CAS + Synchronized来保证并发安全进⾏实现。

JDK1.8的实现降低锁的粒度，JDK1.7版本锁的粒度是基于Segment的，包含多个HashEntry，⽽ JDK1.8锁的粒度就是HashEntry（⾸节点）

JDK1.8版本的数据结构变得更加简单，使得操作也更加清晰流畅，因为已经使⽤synchronized来进 ⾏同步，所以不需要分段锁的概念，也就不需要Segment这种数据结构了，由于粒度的降低，实现 的复杂度也增加了

JDK1.8使⽤红⿊树来优化链表，基于⻓度很⻓的链表的遍历是⼀个很漫⻓的过程，⽽红⿊树的遍历 效率是很快的，代替⼀定阈值的链表，这样形成⼀个最佳拍档

![image 1](<【67期】谈谈ConcurrentHashMap是如何保证线程安全的？.note_images/imageFile1.png>)

get操作源码

⾸先计算hash值，定位到该table索引位置，如果是⾸节点符合就返回

如果遇到扩容的时候，会调⽤标志正在扩容节点ForwardingNode的find⽅法，查找该节点，匹配就 返回

以上都不符合的话，就往下遍历节点，匹配就返回，否则最后就返回nul

//会 发 现 源 码 中 没 有 ⼀ 处 加 了 锁 public V get(Object key) {

Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek; int h = spread(key.hashCode()); //计 算 hash if ((tab = table) != null && (n = tab.length) > 0 &&

(e = tabAt(tab, (n - 1) & h)) != null) {//读 取 ⾸ 节 点 的 Node元 素 if ((eh = e.hash) == h) { //如 果 该 节 点 就 是 ⾸ 节 点 就 返 回

if ((ek = e.key) == key || (ek != null && key.equals(ek))) return e.val;

} //hash值 为 负 值 表 示 正 在 扩 容 ， 这 个 时 候 查 的 是 ForwardingNode的 find⽅ 法 来 定 位 到 nextTable来

- //eh=-1， 说 明 该 节 点 是 ⼀ 个 ForwardingNode， 正 在 迁 移 ， 此 时 调 ⽤ ForwardingNode的 find⽅ 法 去

nextTable⾥ 找 。

- //eh=-2， 说 明 该 节 点 是 ⼀ 个 TreeBin， 此 时 调 ⽤ TreeBin的 find⽅ 法 遍 历 红 ⿊ 树 ， 由 于 红 ⿊ 树有 可 能 正 在 旋 转


变 ⾊ ， 所 以 find⾥ 会 有 读 写 锁 。 //eh>=0， 说 明 该 节 点 下 挂 的 是 ⼀ 个 链 表 ， 直 接 遍 历 该 链 表 即可 。 else if (eh < 0)

return (p = e.find(h, key)) != null ? p.val : null;

while ((e = e.next) != null) {//既 不 是 ⾸ 节 点 也 不 是 ForwardingNode， 那 就 往 下 遍 历

if (e.hash == h && ((ek = e.key) == key || (ek != null && key.equals(ek))))

return e.val; }

} return null;

}

get没有加锁的话，ConcurrentHashMap是如何保证读到的数据不是脏数据的呢？

volatile登场 对于可⻅性，Java提供了volatile关键字来保证可⻅性、有序性。但不保证原⼦性。 普通的共享变量不能保证可⻅性，因为普通共享变量被修改之后，什么时候被写⼊主存是不确定的，当其他 线程去读取时，此时内存中可能还是原来的旧值，因此⽆法保证可⻅性。

volatile关键字对于基本类型的修改可以在随后对多个线程的读保持⼀致，但是对于引⽤类型如数 组，实体bean，仅仅保证引⽤的可⻅性，但并不保证引⽤内容的可⻅性。。

禁⽌进⾏指令重排序。

背景：为了提⾼处理速度，处理器不直接和内存进⾏通信，⽽是先将系统内存的数据读到内部缓存（L1，L2 或其他）后再进⾏操作，但操作完不知道何时会写到内存。

如果对声明了volatile的变量进⾏写操作，JVM就会向处理器发送⼀条指令，将这个变量所在缓存⾏ 的数据写回到系统内存。但是，就算写回到内存，如果其他处理器缓存的值还是旧的，再执⾏计算 操作就会有问题。

在多处理器下，为了保证各个处理器的缓存是⼀致的，就会实现缓存⼀致性协议，当某个CPU在写 数据时，如果发现操作的变量是共享变量，则会通知其他CPU告知该变量的缓存⾏是⽆效的，因此 其他CPU在读取该变量时，发现其⽆效会重新从主存中加载数据。

![image 2](<【67期】谈谈ConcurrentHashMap是如何保证线程安全的？.note_images/imageFile2.png>)

总结下来： 第⼀：使⽤volatile关键字会强制将修改的值⽴即写⼊主存； 第⼆：使⽤volatile关键字的话，当线程2进⾏修改时，会导致线程1的⼯作内存中缓存变量的缓存⾏⽆效（反 映到硬件层的话，就是CPU的L1或者L2缓存中对应的缓存⾏⽆效）； 第三：由于线程1的⼯作内存中缓存变量的缓存⾏⽆效，所以线程1再次读取变量的值时会去主存读取。 是加在数组上的volatile吗?

/**

- * The array of bins. Lazily initialized upon first insertion.

- * Size is always a power of two. Accessed directly by iterators.

- */


transient volatile Node<K,V>[] table;

我们知道volatile可以修饰数组的，只是意思和它表⾯上看起来的样⼦不同。举个栗⼦，volatile int aray[10] 是指aray的地址是volatile的⽽不是数组元素的值是volatile的. ⽤volatile修饰的Node get操作可以⽆锁是由于Node的元素val和指针next是⽤volatile修饰的，在多线程环境下线程A修改结点的val 或者新增节点的时候是对线程B可⻅的。

static class Node<K,V> implements Map.Entry<K,V> { final int hash; final K key; //可以看到这些都⽤了volatile修饰 volatile V val; volatile Node<K,V> next;

Node(int hash, K key, V val, Node<K,V> next) { this.hash = hash; this.key = key; this.val = val; this.next = next;

}

public final K getKey() { return key; }

public final V getValue() { return val; } public final int hashCode() { return key.hashCode() ^ val.hashCode(); } public final String toString(){ return key + "=" + val; } public final V setValue(V value) {

throw new UnsupportedOperationException(); }

public final boolean equals(Object o) { Object k, v, u; Map.Entry<?,?> e; return ((o instanceof Map.Entry) &&

(k = (e = (Map.Entry<?,?>)o).getKey()) != null && (v = e.getValue()) != null && (k == key || k.equals(key)) && (v == (u = val) || v.equals(u)));

}

/**

- * Virtualized support for map.get(); overridden in subclasses.

- */ Node<K,V> find(int h, Object k) {


Node<K,V> e = this; if (k != null) {

do {

K ek; if (e.hash == h &&

((ek = e.key) == k || (ek != null && k.equals(ek)))) return e; } while ((e = e.next) != null);

} return null;

} }

既然volatile修饰数组对get操作没有效果那加在数组上的volatile的⽬的是什么呢？

其实就是为了使得Node数组在扩容的时候对其他线程具有可⻅性⽽加的volatile 总结

在1.8中ConcurentHashMap的get操作全程不需要加锁，这也是它⽐其他并发集合⽐如 hashtable、⽤Colections.synchronizedMap()包装的hashmap;安全效率⾼的原因之⼀。

get操作全程不需要加锁是因为Node的成员val是⽤volatile修饰的和数组⽤volatile修饰没有关系。

数组⽤volatile修饰主要是保证在数组扩容的时候保证可⻅性。

来 源： w.cnblogs.com/keya/p/9632958.html

最近五期

- 【61期】MySQL⾏锁和表锁的含义及区别（MySQL⾯试第四弹）
- 【62期】解释⼀下MySQL中内连接，外连接等的区别（MySQL⾯试第五弹）
- 【63期】谈谈MySQL 索引，B+树原理，以及建索引的⼏⼤原则（MySQL⾯试第六弹）
- 【64期】MySQL 服务占⽤cpu 10%，如何排查问题? （MySQL⾯试第七弹）
- 【65期】Spring的IOC是啥?有什么好处?


