htps:/mp.weixin.q.com/s/xapDYm_n07Tmem1FoMlyMQ

- 1：HashMap 的数据结构？

- 2：HashMap 的⼯作原理？
- 3.当两个对象的 hashCode 相同会发⽣什么？

- 4.你知道 hash 的实现吗？为什么要这样实现？

- 5.为什么要⽤异或运算符？

- 6.HashMap 的 table 的容量如何确定？loadFactor 是什么？该容量如何变化？这种变化会带来什么 问题？

- 7.HashMap中put⽅法的过程？

- 8.数组扩容的过程？

- 9.拉链法导致的链表过深问题为什么不⽤⼆叉查找树代替，⽽选择红⿊树？为什么不⼀直使⽤红⿊ 树？

- 10.说说你对红⿊树的⻅解？

- 11.jdk8中对HashMap做了哪些改变？

- 12.HashMap，LinkedHashMap，TreeMap 有什么区别？

- 13.HashMap & TreeMap & LinkedHashMap 使⽤场景？

- 14.HashMap 和 HashTable 有什么区别？

- 15.Java 中的另⼀个线程安全的与 HashMap 极其类似的类是什么？同样是线程安全，它与 HashTab le 在线程同步上有什么不同？

- 16.HashMap & ConcurrentHashMap 的区别？

- 17.为什么 ConcurrentHashMap ⽐ HashTable 效率要⾼？

- 18.针对 ConcurrentHashMap 锁机制具体分析（JDK 1.7 VS JDK 1.8）

- 19.ConcurrentHashMap 在 JDK 1.8 中，为什么要使⽤内置锁 synchronized 来代替重⼊锁 Reentrant Lock？

- 20.ConcurrentHashMap 简单介绍？

- 21.ConcurrentHashMap 的并发度是什么？


![image 1](<面试：HashMap 夺命二十一问！.note_images/imageFile1.png>)

- 1：HashMap 的数据结构？
- 2：HashMap 的⼯作原理？
- 3.当两个对象的 hashCode 相同会发⽣什么？


A：哈希表结构（链表散列：数组+链表）实现，结合数组和链表的优点。当链表⻓度超过 8 时，链表 转换为红⿊树。

transient Node<K,V>\[\] table;

HashMap 底层是 hash 数组和单向链表实现，数组中的每个元素都是链表，由 Node 内部类（实现 Map.Entry接⼝）实现，HashMap 通过 put & get ⽅法存储和获取。 存储对象时，将 K/V 键值传给 put() ⽅法：

- ①、调⽤ hash(K) ⽅法计算 K 的 hash 值，然后结合数组⻓度，计算得数组下标；
- ②、调整数组⼤⼩（当容器中的元素个数⼤于 capacity * loadfactor 时，容器会进⾏扩容resize 为 2n）；
- ③、i.如果 K 的 hash 值在 HashMap 中不存在，则执⾏插⼊，若存在，则发⽣碰撞；


- ii.如果 K 的 hash 值在 HashMap 中存在，且它们两者 equals 返回 true，则更新键值对；
- iii. 如果 K 的 hash 值在 HashMap 中存在，且它们两者 equals 返回 false，则插⼊链表的尾部（尾插法） 或者红⿊树中（树的添加⽅式）。 （JDK 1.7 之前使⽤头插法、JDK 1.8 使⽤尾插法）（注意：当碰撞导致链表⼤于 TREEIFY_THRESHOLD = 8 时，就把链表转换成红⿊树） 获取对象时，将 K 传给 get() ⽅法：①、调⽤ hash(K) ⽅法（计算 K 的 hash 值）从⽽获取该键值所在 链表的数组下标；②、顺序遍历链表，equals()⽅法查找相同 Node 链表中 K 值对应的 V 值。 hashCode 是定位的，存储位置；equals是定性的，⽐较两者是否相等。


因为 hashCode 相同，不⼀定就是相等的（equals⽅法⽐较），所以两个对象所在数组的下标相同，"碰 撞"就此发⽣。⼜因为 HashMap 使⽤链表存储对象，这个 Node 会存储到链表中。为什么要重写 hashcode 和 equals ⽅法？推荐看下。

- 4.你知道 hash 的实现吗？为什么要这样实现？
- 5.为什么要⽤异或运算符？
- 6.HashMap 的 table 的容量如何确定？loadFactor 是什么？该容量如 何变化？这种变化会带来什么问题？
- 7.HashMap中put⽅法的过程？
- 8.数组扩容的过程？
- 9.拉链法导致的链表过深问题为什么不⽤⼆叉查找树代替，⽽选择红 ⿊树？为什么不⼀直使⽤红⿊树？


JDK 1.8 中，是通过 hashCode() 的⾼ 16 位异或低 16 位实现的：(h = k.hashCode()) ^ (h >>> 16)，主要是 从速度，功效和质量来考虑的，减少系统的开销，也不会造成因为⾼位没有参与下标的计算，从⽽引 起的碰撞。

保证了对象的 hashCode 的 32 位值只要有⼀位发⽣改变，整个 hash() 返回值就会改变。尽可能的减少 碰撞。

- ①、table 数组⼤⼩是由 capacity 这个参数确定的，默认是16，也可以构造时传⼊，最⼤限制是1<<30；
- ②、loadFactor 是装载因⼦，主要⽬的是⽤来确认table 数组是否需要动态扩展，默认值是0.75，⽐如 table 数组⼤⼩为 16，装载因⼦为 0.75 时，threshold 就是12，当 table 的实际⼤⼩超过 12 时，table就需 要动态扩容；
- ③、扩容时，调⽤ resize() ⽅法，将 table ⻓度变为原来的两倍（注意是 table ⻓度，⽽不是 threshold）
- ④、如果数据很⼤的情况下，扩展时将会带来性能的损失，在性能要求很⾼的地⽅，这种损失很可能 很致命。


答：“调⽤哈希函数获取Key对应的hash值，再计算其数组下标； 如果没有出现哈希冲突，则直接放⼊数组；如果出现哈希冲突，则以链表的⽅式放在链表后⾯； 如果链表⻓度超过阀值( TREEIFY THRESHOLD==8)，就把链表转成红⿊树，链表⻓度低于6，就把红 ⿊树转回链表; 如果结点的key已经存在，则替换其value即可； 如果集合中的键值对⼤于12，调⽤resize⽅法进⾏数组扩容。”

创建⼀个新的数组，其容量为旧数组的两倍，并重新计算旧数组中结点的存储位置。结点在新数组中 的位置只有两种，原下标位置或原下标+旧数组的⼤⼩。

之所以选择红⿊树是为了解决⼆叉查找树的缺陷，⼆叉查找树在特殊情况下会变成⼀条线性结构（这 就跟原来使⽤链表结构⼀样了，造成很深的问题），遍历查找会⾮常慢。推荐：⾯试问红⿊树，我脸 都绿了。 ⽽红⿊树在插⼊新数据后可能需要通过左旋，右旋、变⾊这些操作来保持平衡，引⼊红⿊树就是为了 查找数据快，解决链表查询深度的问题，我们知道红⿊树属于平衡⼆叉树，但是为了保持“平衡”是需 要付出代价的，但是该代价所损耗的资源要⽐遍历线性链表要少，所以当⻓度⼤于8的时候，会使⽤红 ⿊树，如果链表⻓度很短的话，根本不需要引⼊红⿊树，引⼊反⽽会慢。

- 10.说说你对红⿊树的⻅解？

每个节点⾮红即⿊ 根节点总是⿊⾊的 如果节点是红⾊的，则它的⼦节点必须是⿊⾊的（反之不⼀定）

每个叶⼦节点都是⿊⾊的空节点（NIL节点）

从根节点到叶节点或空⼦节点的每条路径，必须包含相同数⽬的⿊⾊节点（即相同的⿊⾊⾼度）

- 11.jdk8中对HashMap做了哪些改变？
- 12.HashMap，LinkedHashMap，TreeMap 有什么区别？
- 13.HashMap & TreeMap & LinkedHashMap 使⽤场景？
- 14.HashMap 和 HashTable 有什么区别？


在java 1.8中，如果链表的⻓度超过了8，那么链表将转换为红⿊树。（桶的数量必须⼤于64，⼩于64的 时候只会扩容） 发⽣hash碰撞时，java 1.7 会在链表的头部插⼊，⽽java 1.8会在链表的尾部插⼊ 在java 1.8中，Entry被Node替代(换了⼀个⻢甲。

HashMap 参考其他问题； LinkedHashMap 保存了记录的插⼊顺序，在⽤ Iterator 遍历时，先取到的记录肯定是先插⼊的；遍历⽐ HashMap 慢； TreeMap 实现 SortMap 接⼝，能够把它保存的记录根据键排序（默认按键值升序排序，也可以指定排 序的⽐较器）

⼀般情况下，使⽤最多的是 HashMap。 HashMap：在 Map 中插⼊、删除和定位元素时； TreeMap：在需要按⾃然顺序或⾃定义顺序遍历键的情况下； LinkedHashMap：在需要输出的顺序和输⼊的顺序相同的情况下。

- ①、HashMap 是线程不安全的，HashTable 是线程安全的；
- ②、由于线程安全，所以 HashTable 的效率⽐不上 HashMap；
- ③、HashMap最多只允许⼀条记录的键为null，允许多条记录的值为null，⽽ HashTable不允许；


- ④、HashMap 默认初始化数组的⼤⼩为16，HashTable 为 11，前者扩容时，扩⼤两倍，后者扩⼤两倍

+1；

- ⑤、HashMap 需要重新计算 hash 值，⽽ HashTable 直接使⽤对象的 hashCode


- 15.Java 中的另⼀个线程安全的与 HashMap 极其类似的类是什么？ 同样是线程安全，它与 HashTable 在线程同步上有什么不同？
- 16.HashMap & ConcurrentHashMap 的区别？
- 17.为什么 ConcurrentHashMap ⽐ HashTable 效率要⾼？

- JDK 1.7 中使⽤分段锁（ReentrantLock + Segment + HashEntry），相当于把⼀个 HashMap 分成多个 段，每段分配⼀把锁，这样⽀持多线程访问。锁粒度：基于 Segment，包含多个 HashEntry。

- JDK 1.8 中使⽤ CAS + synchronized + Node + 红⿊树。锁粒度：Node（⾸结


- 18.针对 ConcurrentHashMap 锁机制具体分析（JDK 1.7 VS JDK 1. 8）


ConcurrentHashMap 类（是 Java并发包 java.util.concurrent 中提供的⼀个线程安全且⾼效的 HashMap 实 现）。 HashTable 是使⽤ synchronize 关键字加锁的原理（就是对对象加锁）； ⽽针对 ConcurrentHashMap，在 JDK 1.7 中采⽤ 分段锁的⽅式；JDK 1.8 中直接采⽤了CAS（⽆锁算 法）+ synchronized。

除了加锁，原理上⽆太⼤区别。另外，HashMap 的键值对允许有null，但是ConCurrentHashMap 都不允 许。

HashTable 使⽤⼀把锁（锁住整个链表结构）处理并发问题，多个线程竞争⼀把锁，容易阻塞； ConcurrentHashMap

点）（实现 Map.Entry）。锁粒度降低了。

- JDK 1.7 中，采⽤分段锁的机制，实现并发的更新操作，底层采⽤数组+链表的存储结构，包括两个核 ⼼静态内部类 Segment 和 HashEntry。


- ①、Segment 继承 ReentrantLock（重⼊锁） ⽤来充当锁的⻆⾊，每个 Segment 对象守护每个散列映射 表的若⼲个桶；
- ②、HashEntry ⽤来封装映射表的键-值对；
- ③、每个桶是由若⼲个 HashEntry 对象链接起来的链表


![image 2](<面试：HashMap 夺命二十一问！.note_images/imageFile2.png>)

- JDK 1.8 中，采⽤Node + CAS + Synchronized来保证并发安全。取消类 Segment，直接⽤ table 数组存储 键值对；当 HashEntry 对象组成的链表⻓度超过 TREEIFY_THRESHOLD 时，链表转换为红⿊树，提升 性能。底层变更为数组 + 链表 + 红⿊树。


![image 3](<面试：HashMap 夺命二十一问！.note_images/imageFile3.png>)

- 19.ConcurrentHashMap 在 JDK 1.8 中，为什么要使⽤内置锁 synchr onized 来代替重⼊锁 ReentrantLock？
- 20.ConcurrentHashMap 简单介绍？


- ①、粒度降低了；
- ②、JVM 开发团队没有放弃 synchronized，⽽且基于 JVM 的 synchronized 优化空间更⼤，更加⾃然。
- ③、在⼤量的数据操作下，对于 JVM 的内存压⼒，基于 API 的 ReentrantLock 会开销更多的内存。


- ①、重要的常量： private transient volatile int sizeCtl; 当为负数时，-1 表示正在初始化，-N 表示 N - 1 个线程正在进⾏扩容； 当为 0 时，表示 table 还没有初始化； 当为其他正数时，表示初始化或者下⼀次进⾏扩容的⼤⼩。
- ②、数据结构：


Node 是存储结构的基本单元，继承 HashMap 中的 Entry，⽤于存储数据； TreeNode 继承 Node，但是数据结构换成了⼆叉树结构，是红⿊树的存储结构，⽤于红⿊树中存储数 据； TreeBin 是封装 TreeNode 的容器，提供转换红⿊树的⼀些条件和锁的控制。

- ③、存储对象时（put() ⽅法）： 如果没有初始化，就调⽤ initTable() ⽅法来进⾏初始化； 如果没有 hash 冲突就直接 CAS ⽆锁插⼊； 如果需要扩容，就先进⾏扩容； 如果存在 hash 冲突，就加锁来保证线程安全，两种情况：⼀种是链表形式就直接遍历 到尾端插⼊，⼀种是红⿊树就按照红⿊树结构插⼊； 如果该链表的数量⼤于阀值 8，就要先转换成红⿊树的结构，break 再⼀次进⼊循环 如果添加成功就调⽤ addCount() ⽅法统计 size，并且检查是否需要扩容。
- ④、扩容⽅法 transfer()：默认容量为 16，扩容时，容量变为原来的两倍。 helpTransfer()：调⽤多个⼯作线程⼀起帮助进⾏扩容，这样的效率就会更⾼。
- ⑤、获取对象时（get()⽅法）： 计算 hash 值，定位到该 table 索引位置，如果是⾸结点符合就返回； 如果遇到扩容时，会调⽤标记正在扩容结点 ForwardingNode.find()⽅法，查找该结点，匹配就返回； 以上都不符合的话，就往下遍历结点，匹配就返回，否则最后就返回 null。


# 21.ConcurrentHashMap 的并发度是什么？

程序运⾏时能够同时更新 ConccurentHashMap 且不产⽣锁竞争的最⼤线程数。默认为 16，且可以在构 造函数中设置。 当⽤户设置并发度时，ConcurrentHashMap 会使⽤⼤于等于该值的最⼩2幂指数作为实际并发度（假如 ⽤户设置并发度为17，实际并发度则为32）

