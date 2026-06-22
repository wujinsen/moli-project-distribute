# 简介

Java为数据结构中的映射定义了⼀个接⼝java.util.Map，此接⼝主要有四个常⽤的实现类，分别是 HashMap、Hashtable、LinkedHashMap和TreMap，类继承关系如下图所示：

![image 1](<Java8系列之重新认识HashMap.note_images/imageFile1.png>)

java.util.map类图

下⾯针对各个实现类的特点做⼀些说明：

- (1) HashMap：它根据键的hashCode值存储数据，⼤多数情况下可以直接定位到它的值，因⽽具有很 快的访问速度，但遍历顺序却是不确定的。 HashMap最多只允许⼀条记录的键为nul，允许多条记录 的值为nul。HashMap⾮线程安全，即任⼀时刻可以有多个线程同时写HashMap，可能会导致数据的 不⼀致。如果需要满⾜线程安全，可以⽤ Colections的synchronizedMap⽅法使HashMap具有线程安 全的能⼒，或者使⽤ConcurentHashMap。

- (2) Hashtable：Hashtable是遗留类，很多映射的常⽤功能与HashMap类似，不同的是它承⾃ Dictionary类，并且是线程安全的，任⼀时间只有⼀个线程能写Hashtable，并发性不如 ConcurentHashMap，因为ConcurentHashMap引⼊了分段锁。Hashtable不建议在新代码中使⽤， 不需要线程安全的场合可以⽤HashMap替换，需要线程安全的场合可以⽤ConcurentHashMap替换。

- (3) LinkedHashMap：LinkedHashMap是HashMap的⼀个⼦类，保存了记录的插⼊顺序，在⽤Iterator 遍历LinkedHashMap时，先得到的记录肯定是先插⼊的，也可以在构造时带参数，按照访问次序排 序。

- (4) TreMap：TreMap实现SortedMap接⼝，能够把它保存的记录根据键排序，默认是按键值的升序 排序，也可以指定排序的⽐较器，当⽤Iterator遍历TreMap时，得到的记录是排过序的。如果使⽤排 序的映射，建议使⽤TreMap。在使⽤TreMap时，key必须实现Comparable接⼝或者在构造 TreMap传⼊⾃定义的Comparator，否则会在运⾏时抛出java.lang.ClasCastException类型的异常。 对于上述四种Map类型的类，要求映射中的key是不可变对象。不可变对象是该对象在创建后它的哈希 值不会被改变。如果对象的哈希值发⽣变化，Map对象很可能就定位不到映射的位置了。 通过上⾯的⽐较，我们知道了HashMap是Java的Map家族中⼀个普通成员，鉴于它可以满⾜⼤多数场 景的使⽤条件，所以是使⽤频度最⾼的⼀个。下⽂我们主要结合源码，从存储结构、常⽤⽅法分析、 扩容以及安全性等⽅⾯深⼊讲解HashMap的⼯作原理。


内部实现

搞清楚HashMap，⾸先需要知道HashMap是什么，即它的存储结构-字段；其次弄明⽩它能⼲什么， 即它的功能实现-⽅法。下⾯我们针对这两个⽅⾯详细展开讲解。

## 存储结构-字段

从结构实现来讲，HashMap是数组+链表+红⿊树（JDK1.8增加了红⿊树部分）实现的，如下如所示。

![image 2](<Java8系列之重新认识HashMap.note_images/imageFile2.png>)

hashMap内存结构图

这⾥需要讲明⽩两个问题：数据底层具体存储的是什么？这样的存储⽅式有什么优点呢？

- (1) 从源码可知，HashMap类中有⼀个⾮常重要的字段，就是 Node[] table，即哈希桶数组，明显它是 ⼀个Node的数组。我们来看Node[JDK1.8]是何物。

Node是HashMap的⼀个内部类，实现了Map.Entry接⼝，本质是就是⼀个映射(键值对)。上图中的每 个⿊⾊圆点就是⼀个Node对象。

- (2) HashMap就是使⽤哈希表来存储的。哈希表为解决冲突，可以采⽤开放地址法和链地址法等来解决 问题，Java中HashMap采⽤了链地址法。链地址法，简单来说，就是数组加链表的结合。在每个数组 元素上都⼀个链表结构，当数据被Hash后，得到数组下标，把数据放在对应下标元素的链表上。例如 程序执⾏下⾯代码：


<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12<br>13<br>14<br></th>
    <th>static clas Node<K,V> implements Map.Entry<K,V> { final int hash; /⽤来定位数组索引位置 final K key; V value; Node<K,V> next; /链表的下⼀个node<br><br>Node(int hash, K key, V value, Node<K,V> next) { . } public final K getKey(){ . } public final V getValue() { . } public final String toString() { . } public final int hashCode() { . } public final V setValue(V newValue) { . } public final bolean equals(Object o) { . }</th>
  </tr>
</table>


}

<table>
  <tr>
    <th>1</th>
    <th>map.put("美团","⼩美");</th>
  </tr>
</table>


系统将调⽤”美团”这个key的hashCode()⽅法得到其hashCode 值（该⽅法适⽤于每个Java对象），然 后再通过Hash算法的后两步运算（⾼位运算和取模运算，下⽂有介绍）来定位该键值对的存储位置， 有时两个key会定位到相同的位置，表示发⽣了Hash碰撞。当然Hash算法计算结果越分散均匀，Hash 碰撞的概率就越⼩，map的存取效率就会越⾼。 如果哈希桶数组很⼤，即使较差的Hash算法也会⽐较分散，如果哈希桶数组数组很⼩，即使好的Hash 算法也会出现较多碰撞，所以就需要在空间成本和时间成本之间权衡，其实就是在根据实际情况确定 哈希桶数组的⼤⼩，并在此基础上设计好的hash算法减少Hash碰撞。那么通过什么⽅式来控制map使 得Hash碰撞的概率⼜⼩，哈希桶数组（Node[] table）占⽤空间⼜少呢？答案就是好的Hash算法和扩 容机制。 在理解Hash和扩容流程之前，我们得先了解下HashMap的⼏个字段。从HashMap的默认构造函数源 码可知，构造函数就是对下⾯⼏个字段进⾏初始化，源码如下：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br></th>
    <th>int threshold; / 所能容纳的key-value对极限 final float loadFactor; / 负载因⼦ int modCount;</th>
  </tr>
</table>


int size;

⾸先，Node[] table的初始化⻓度length(默认值是16)，Load factor为负载因⼦(默认值是0.75)， threshold是HashMap所能容纳的最⼤数据量的Node(键值对)个数。threshold = length * Load factor。也就是说，在数组定义好⻓度之后，负载因⼦越⼤，所能容纳的键值对个数越多。 结合负载因⼦的定义公式可知，threshold就是在此Load factor和length(数组⻓度)对应下允许的最⼤ 元素数⽬，超过这个数⽬就重新resize(扩容)，扩容后的HashMap容量是之前容量的两倍。默认的负载 因⼦0.75是对空间和时间效率的⼀个平衡选择，建议⼤家不要修改，除⾮在时间和空间⽐较特殊的情 况下，如果内存空间很多⽽⼜对时间效率要求很⾼，可以降低负载因⼦Load factor的值；相反，如果 内存空间紧张⽽对时间效率要求不⾼，可以增加负载因⼦loadFactor的值，这个值可以⼤于1。 size这个字段其实很好理解，就是HashMap中实际存在的键值对数量。注意和table的⻓度length、容 纳最⼤键值对数量threshold的区别。⽽modCount字段主要⽤来记录HashMap内部结构发⽣变化的次 数，主要⽤于迭代的快速失败。强调⼀点，内部结构发⽣变化指的是结构发⽣变化，例如put新键值 对，但是某个key对应的value值被覆盖不属于结构变化。 在HashMap中，哈希桶数组table的⻓度length⼤⼩必须为2的n次⽅(⼀定是合数)，这是⼀种⾮常规的 设计，常规的设计是把桶的⼤⼩设计为素数。相对来说素数导致冲突的概率要⼩于合数，具体证明可 以参考 ，Hashtable初始化桶⼤⼩为 1，就是桶⼤⼩设 计为素数的应⽤（Hashtable扩容后不能保证还是素数）。HashMap采⽤这种⾮常规设计，主要是为 了在取模和扩容时做优化，同时为了减少冲突，HashMap定位哈希桶索引位置时，也加⼊了⾼位参与 运算的过程。 这⾥存在⼀个问题，即使负载因⼦和Hash算法设计的再合理，也免不了会出现拉链过⻓的情况，⼀旦 出现拉链过⻓，则会严重影响HashMap的性能。于是，在JDK1.8版本中，对数据结构做了进⼀步的优 化，引⼊了红⿊树。⽽当链表⻓度太⻓（默认超过8）时，链表就转换为红⿊树，利⽤红⿊树快速增删 改查的特点提⾼HashMap的性能，其中会⽤到红⿊树的插⼊、删除、查找等算法。本⽂不再对红⿊树 展开讨论，想了解更多红⿊树数据结构的⼯作原理可以参考

htp:/blog.csdn.net/liuqiyao_01/article/details/1475159

htp:/blog.csdn.net/v_july_v/article/details/61056 30

。

## 功能实现-⽅法

HashMap的内部功能实现很多，本⽂主要从根据key获取哈希桶数组索引位置、put⽅法的详细执⾏、 扩容过程三个具有代表性的点深⼊展开讲解。

- 1. 确定哈希桶数组索引位置


不管增加、删除、查找键值对，定位到哈希桶数组的位置都是很关键的第⼀步。前⾯说过HashMap的 数据结构是数组和链表的结合，所以我们当然希望这个HashMap⾥⾯的元素位置尽量分布均匀些，尽 量使得每个位置上的元素数量只有⼀个，那么当我们⽤hash算法求得这个位置的时候，⻢上就可以知 道对应位置的元素就是我们要的，不⽤遍历链表，⼤⼤优化了查询的效率。HashMap定位数组索引位 置，直接决定了hash⽅法的离散性能。先看看源码的实现(⽅法⼀+⽅法⼆):

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br></th>
    <th>⽅法⼀： static final int hash(Object key) { /jdk1.8 & jdk1.7<br><br>int h; / h = key.hashCode() 为第⼀步 取hashCode值 / h ^ (h > 16) 为第⼆步 ⾼位参与运算<br><br>return (key = nul) ? 0 : (h = key.hashCode() ^ (h > 16); } ⽅法⼆： static int indexFor(int h, int length) { /jdk1.7的源码，jdk1.8没 有这个⽅法，但是实现原理⼀样的<br><br>return h & (length-1); /第三步 取模运算</th>
  </tr>
</table>


}

这⾥的Hash算法本质上就是三步：取key的hashCode值、⾼位运算、取模运算。 对于任意给定的对象，只要它的hashCode()返回值相同，那么程序调⽤⽅法⼀所计算得到的Hash码值 总是相同的。我们⾸先想到的就是把hash值对数组⻓度取模运算，这样⼀来，元素的分布相对来说是 ⽐较均匀的。但是，模运算的消耗还是⽐较⼤的，在HashMap中是这样做的：调⽤⽅法⼆来计算该对 象应该保存在table数组的哪个索引处。 这个⽅法⾮常巧妙，它通过h & (table.length -1)来得到该对象的保存位，⽽HashMap底层数组的⻓度 总是2的n次⽅，这是HashMap在速度上的优化。当length总是2的n次⽅时，h& (length-1)运算等价于 对length取模，也就是h%length，但是&⽐%具有更⾼的效率。 在JDK1.8的实现中，优化了⾼位运算的算法，通过hashCode()的⾼16位异或低16位实现的：(h = k.hashCode() ^ (h > 16)，主要是从速度、功效、质量来考虑的，这么做可以在数组table的length ⽐较⼩的时候，也能保证考虑到⾼低Bit都参与到Hash的计算中，同时不会有太⼤的开销。 下⾯举例说明下，n为table的⻓度。

![image 3](<Java8系列之重新认识HashMap.note_images/imageFile3.png>)

hashMap哈希算法例图

- 2. 分析HashMap的put⽅法 HashMap的put⽅法执⾏过程可以通过下图来理解，⾃⼰有兴趣可以去对⽐源码更清楚地研究学习。


![image 4](<Java8系列之重新认识HashMap.note_images/imageFile4.png>)

hashMap put⽅法执⾏流程图

- ①.判断键值对数组table[i]是否为空或为nul，否则执⾏resize()进⾏扩容；

- ②.根据键值key计算hash值得到插⼊的数组索引i，如果table[i]=nul，直接新建节点添加，转向⑥， 如果table[i]不为空，转向③；

- ③.判断table[i]的⾸个元素是否和key⼀样，如果相同直接覆盖value，否则转向④，这⾥的相同指的是 hashCode以及equals；

- ④.判断table[i] 是否为treNode，即table[i] 是否是红⿊树，如果是红⿊树，则直接在树中插⼊键值 对，否则转向⑤；

- ⑤.遍历table[i]，判断链表⻓度是否⼤于8，⼤于8的话把链表转换为红⿊树，在红⿊树中执⾏插⼊操 作，否则进⾏链表的插⼊操作；遍历过程中若发现key已经存在直接覆盖value即可；

- ⑥.插⼊成功后，判断实际存在的键值对数量size是否超多了最⼤容量threshold，如果超过，进⾏扩 容。 JDK1.8HashMap的put⽅法源码如下:


<table>
  <tr>
    <th>1 public V put(K key, V value) {<br>2/ 对key的hashCode()做hash<br>3 return putVal(hash(key), key, value, false, true);<br>4 }<br>5<br>6 final V putVal(int hash, K key, V value, bolean onlyIfAbsent,<br>7 bolean evict) {<br>8 Node<K,V>[] tab; Node<K,V> p; int n, i;<br>9/ 步骤①：tab为空则创建<br>10 if (tab = table) = nul | (n = tab.length) = 0)<br><br><br>1 n = (tab = resize().length;<br><br>12/ 步骤②：计算index，并对nul做处理<br>13 if (p = tab[i = (n - 1) & hash]) = nul)<br>14 tab[i] = newNode(hash, key, value, nul);<br>15 else {<br>16 Node<K,V> e; K k;<br>17/ 步骤③：节点key存在，直接覆盖value<br>18 if (p.hash = hash & 19(k = p.key) = key| (key != nul & key.equals(k )<br><br><br>20 e = p;<br>21/ 步骤④：判断该链为红⿊树<br><br><br>2 else if (p instanceof TreNode)<br><br><br>23 e =(TreNode<K,V>)p).putTreVal(this, tab, hash, key, value);<br>24/ 步骤⑤：该链为链表<br>25 else {<br>26 for (int binCount = 0; ; +binCount) {<br>27 if (e = p.next) = nul) {<br>28 p.next = newNode(hash, key,value,nul); /链表⻓度⼤于8转换为红⿊树进⾏处理<br>29 if (binCount >= TREIFY_THRESHOLD - 1) / -1 for 1st<br>30 treifyBin(tab, hash);<br>31 break;<br>32 } / key已经存在直接覆盖value<br><br><br>3 if (e.hash = hash &<br><br>34(k = e.key) = key| (key != nul & key.equals(k ) break;<br><br>36 p = e;<br>37 }<br>38 }<br>39<br>40 if (e != nul) { / existing maping for key<br>41 V oldValue = e.value;<br>42 if (!onlyIfAbsent | oldValue = nul)<br>43 e.value = value;<br><br><br>4 afterNodeAces(e);<br><br><br>45 return oldValue;<br>46 }<br>47 }<br>48 +modCount;<br>49/ 步骤⑥：超过最⼤容量 就扩容<br>50 if (+size > threshold)<br>51 resize();<br>52 afterNodeInsertion(evict);<br>53 return nul;<br></th>
  </tr>
</table>


54 }

- 3. 扩容机制 扩容(resize)就是重新计算容量，向HashMap对象⾥不停的添加元素，⽽HashMap对象内部的数组⽆ 法装载更多的元素时，对象就需要扩⼤数组的⻓度，以便能装⼊更多的元素。当然Java⾥的数组是⽆ 法⾃动扩容的，⽅法是使⽤⼀个新的数组代替已有的容量⼩的数组，就像我们⽤⼀个⼩桶装⽔，如果 想装更多的⽔，就得换⼤⽔桶。


我们分析下resize的源码，鉴于JDK1.8融⼊了红⿊树，较复杂，为了便于理解我们仍然使⽤JDK1.7的 代码，好理解⼀些，本质上区别不⼤，具体区别后⽂再说。

<table>
  <tr>
    <th>1 void resize(int newCapacity) { /传⼊新的容量<br>2 Entry[] oldTable = table; /引⽤扩容前的Entry数组<br>3 int oldCapacity = oldTable.length;<br>4 if (oldCapacity = MAXIMUM_CAPACITY) { /扩容前的数组⼤⼩如果已经达到最⼤(2^30)了<br>5 threshold = Integer.MAX_VALUE; /修改阈值为int的最⼤值(2^31-1)，这样以后就不会扩容了<br>6 return;<br>7 }<br>8<br>9 Entry[] newTable = new Entry[newCapacity]; /初始化⼀个新的Entry数组<br>10 transfer(newTable); /！！将数据转移到新的Entry数组⾥ 1 table = newTable; /HashMap的table属性引⽤新的Entry数组<br><br><br>12 threshold = (int)(newCapacity * loadFactor);/修改阈值</th>
  </tr>
</table>


13 }

这⾥就是使⽤⼀个容量更⼤的数组来代替已有的容量⼩的数组，transfer()⽅法将原有Entry数组的元素 拷⻉到新的Entry数组⾥。

<table>
  <tr>
    <th>1 void transfer(Entry[] newTable) {<br>2 Entry[] src = table; /src引⽤了旧的Entry数组<br>3 int newCapacity = newTable.length;<br>4 for (int j = 0; j < src.length; j +) { /遍历旧的Entry数组<br>5 Entry<K,V> e = src[j]; /取得旧Entry数组的每个元素<br>6 if (e != nul) {<br>7 src[j] = nul;/释放旧Entry数组的对象引⽤（for循环后，旧的Entry数组不再引⽤任何对象）<br>8 do {<br>9 Entry<K,V> next = e.next;<br>10 int i = indexFor(e.hash, newCapacity); /！！重新计算每个元素在数组中的位置 1 e.next = newTable[i]; /标记[1]<br><br><br>12 newTable[i] = e; /将元素放在数组上<br>13 e = next; /访问下⼀个Entry链上的元素<br>14 } while (e != nul);<br>15 }<br>16 }<br></th>
  </tr>
</table>


17 }

newTable[i]的引⽤赋给了e.next，也就是使⽤了单链表的头插⼊⽅式，同⼀位置上新元素总会被放在 链表的头部位置；这样先放在⼀个索引上的元素终会被放到Entry链的尾部(如果发⽣了hash冲突的 话），这⼀点和Jdk1.8有区别，下⽂详解。在旧数组中同⼀条Entry链上的元素，通过重新计算索引位 置后，有可能被放到了新数组的不同位置上。 下⾯举个例⼦说明下扩容过程。假设了我们的hash算法就是简单的⽤key mod ⼀下表的⼤⼩（也就是 数组的⻓度）。其中的哈希桶数组table的size=2， 所以key = 3、7、5，put顺序依次为 5、7、3。在 mod 2以后都冲突在table[1]这⾥了。这⾥假设负载因⼦ loadFactor=1，即当键值对的实际⼤⼩size ⼤ 于 table的实际⼤⼩时进⾏扩容。接下来的三个步骤是哈希桶数组 resize成4，然后所有的Node重新 rehash的过程。

![image 5](<Java8系列之重新认识HashMap.note_images/imageFile5.png>)

jdk1.7扩容例图

下⾯我们讲解下JDK1.8做了哪些优化。经过观测可以发现，我们使⽤的是2次幂的扩展(指⻓度扩为原 来2倍)，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。看下图可以明⽩这 句话的意思，n为table的⻓度，图（a）表示扩容前的key1和key2两种key确定索引位置的示例，图 （b）表示扩容后key1和key2两种key确定索引位置的示例，其中hash1是key1对应的哈希与⾼位运算 结果。

- hashMap 1.8 哈希算法例图1

![image 6](<Java8系列之重新认识HashMap.note_images/imageFile6.png>)

- hashMap 1.8 哈希算法例图2


元素在重新计算hash之后，因为n变为2倍，那么n-1的mask范围在⾼位多1bit(红⾊)，因此新的index 就会发⽣这样的变化：

![image 7](<Java8系列之重新认识HashMap.note_images/imageFile7.png>)

因此，我们在扩充HashMap的时候，不需要像JDK1.7的实现那样重新计算hash，只需要看看原来的 hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”，可以 看看下图为16扩充为32的resize示意图：

![image 8](<Java8系列之重新认识HashMap.note_images/imageFile8.png>)

jdk1.8 hashMap扩容例图

这个设计确实⾮常的巧妙，既省去了重新计算hash值的时间，⽽且同时，由于新增的1bit是0还是1可以 认为是随机的，因此resize的过程，均匀的把之前的冲突的节点分散到新的bucket了。这⼀块就是 JDK1.8新增的优化点。有⼀点注意区别，JDK1.7中rehash的时候，旧链表迁移新链表的时候，如果在 新表的数组索引位置相同，则链表元素会倒置，但是从上图可以看出，JDK1.8不会倒置。有兴趣的同 学可以研究下JDK1.8的resize源码，写的很赞，如下:

- 1 final Node<K,V>[] resize() {
- 2 Node<K,V>[] oldTab = table;
- 3 int oldCap = (oldTab = nul) ? 0 : oldTab.length;
- 4 int oldThr = threshold;
- 5 int newCap, newThr = 0;
- 6 if (oldCap > 0) {
- 7/ 超过最⼤值就不再扩充了，就只好随你碰撞去吧
- 8 if (oldCap >= MAXIMUM_CAPACITY) {
- 9 threshold = Integer.MAX_VALUE;
- 10 return oldTab;


- 1 }

- 12/ 没超过最⼤值，就扩充为原来的2倍
- 13 else if (newCap = oldCap < 1) < MAXIMUM_CAPACITY &
- 14 oldCap >= DEFAULT_INITIAL_CAPACITY)
- 15 newThr = oldThr < 1; / double threshold
- 16 }
- 17 else if (oldThr > 0) / initial capacity was placed in threshold
- 18 newCap = oldThr;
- 19 else { / zero initial threshold signifies using defaults
- 20 newCap = DEFAULT_INITIAL_CAPACITY;
- 21 newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);


- 2 }

- 23/ 计算新的resize上限
- 24 if (newThr = 0) {
- 25
- 26 float ft = (float)newCap * loadFactor;
- 27 newThr = (newCap < MAXIMUM_CAPACITY & ft < (float)MAXIMUM_CAPACITY ?
- 28 (int)ft : Integer.MAX_VALUE);
- 29 }
- 30 threshold = newThr;
- 31 @SupresWarnings({"rawtypes"，"unchecked"})
- 32 Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];


- 3 table = newTab;

- 34 if (oldTab != nul) {
- 35/ 把每个bucket都移动到新的buckets中
- 36 for (int j = 0; j < oldCap; +j) {
- 37 Node<K,V> e;
- 38 if (e = oldTab[j]) != nul) {
- 39 oldTab[j] = nul;
- 40 if (e.next = nul)
- 41 newTab[e.hash & (newCap - 1)] = e;
- 42 else if (e instanceof TreNode) 43(TreNode<K,V>)e).split(this, newTab, j, oldCap);


- 4 else { / 链表优化重hash的代码块

- 45 Node<K,V> loHead = nul, loTail = nul;
- 46 Node<K,V> hiHead = nul, hiTail = nul;
- 47 Node<K,V> next;
- 48 do {
- 49 next = e.next;
- 50/ 原索引
- 51 if (e.hash & oldCap) = 0) {
- 52 if (loTail = nul)
- 53 loHead = e;
- 54 else


- 5 loTail.next = e;

- 56 loTail = e;
- 57 }
- 58/ 原索引+oldCap
- 59 else {
- 60 if (hiTail = nul)
- 61 hiHead = e;
- 62 else
- 63 hiTail.next = e;
- 64 hiTail = e;
- 65 }


- 6 } while(e = next) != nul);


67/ 原索引放到bucket⾥

- 68 if (loTail != nul) {
- 69 loTail.next = nul;
- 70 newTab[j] = loHead;
- 71 }
- 72/ 原索引+oldCap放到bucket⾥
- 73 if (hiTail != nul) {
- 74 hiTail.next = nul;
- 75 newTab[j + oldCap] = hiHead;
- 76 }
- 77 }
- 78 }
- 79 }
- 80 }
- 81 return newTab;
- 82 }


# 线程安全性

在多线程使⽤场景中，应该尽量避免使⽤线程不安全的HashMap，⽽使⽤线程安全的 ConcurentHashMap。那么为什么说HashMap是线程不安全的，下⾯举例⼦说明在并发的多线程使⽤ 场景中使⽤HashMap可能造成死循环。代码例⼦如下(便于理解，仍然使⽤JDK1.7的环境)：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br></th>
    <th>public clas HashMapInfiniteLop {<br><br>private static HashMap<Integer,String> map = new<br><br>HashMap<Integer,String>(2，0.75f); public static void main(String[] args) { map.put(5， "C");<br><br>new Thread("Thread1") { public void run() { map.put(7, "B"); System.out.println(map); }; }.start(); new Thread("Thread2") { public void run() { map.put(3, "A); System.out.println(map); }; }.start(); }</th>
  </tr>
</table>


}

其中，map初始化为⼀个⻓度为2的数组，loadFactor=0.75，threshold=2*0.75=1，也就是说当put第 ⼆个key的时候，map就需要进⾏resize。 通过设置断点让线程1和线程2同时debug到transfer⽅法(3.3⼩节代码块)的⾸⾏。注意此时两个线程已 经成功添加数据。放开thread1的断点⾄transfer⽅法的“Entry next = e.next;” 这⼀⾏；然后放开线程2 的的断点，让线程2进⾏resize。结果如下图。

- jdk1.7 hashMap死循环例图1

![image 9](<Java8系列之重新认识HashMap.note_images/imageFile9.png>)

- jdk1.7 hashMap死循环例图2

![image 10](<Java8系列之重新认识HashMap.note_images/imageFile10.png>)

- jdk1.7 hashMap死循环例图3


注意，Thread1的 e 指向了key(3)，⽽next指向了key(7)，其在线程⼆rehash后，指向了线程⼆重组后 的链表。 线程⼀被调度回来执⾏，先是执⾏ newTalbe[i] = e， 然后是e = next，导致了e指向了key(7)，⽽下⼀ 次循环的next = e.next导致了next指向了key(3)。

![image 11](<Java8系列之重新认识HashMap.note_images/imageFile11.png>)

e.next = newTable[i] 导致 key(3).next 指向了 key(7)。注意：此时的key(7).next 已经指向了key(3)， 环形链表就这样出现了。

![image 12](<Java8系列之重新认识HashMap.note_images/imageFile12.png>)

jdk1.7 hashMap死循环例图4

于是，当我们⽤线程⼀调⽤map.get(1)时，悲剧就出现了⸺Infinite Lop。

# JDK1.8与JDK1.7的性能对⽐

HashMap中，如果key经过hash算法得出的数组索引位置全部不相同，即Hash算法⾮常好，那样的 话，getKey⽅法的时间复杂度就是O(1)，如果Hash算法技术的结果碰撞⾮常多，假如Hash算极其差， 所有的Hash算法结果得出的索引位置⼀样，那样所有的键值对都集中到⼀个桶中，或者在⼀个链表 中，或者在⼀个红⿊树中，时间复杂度分别为O(n)和O(lgn)。 鉴于JDK1.8做了多⽅⾯的优化，总体性 能优于JDK1.7，下⾯我们从两个⽅⾯⽤例⼦证明这⼀点。

Hash较均匀的情况

为了便于测试，我们先写⼀个类Key，如下：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br><br>23<br>24<br>25<br>26<br></th>
    <th>clas Key implements Comparable<Key> {<br><br>private final int value;<br><br>Key(int value) { this.value = value; }<br><br>@Override public int compareTo(Key o) { return Integer.compare(this.value, o.value); }<br><br>@Override public bolean equals(Object o) { if (this = o) return true; if (o = nul | getClas() != o.getClas() return false; Key key = (Key) o; return value = key.value; }<br><br>@Override public int hashCode() { return value; }</th>
  </tr>
</table>


27 }

这个类复写了equals⽅法，并且提供了相当好的hashCode函数，任何⼀个值的hashCode都不会相 同，因为直接使⽤value当做hashcode。为了避免频繁的GC，我将不变的Key实例缓存了起来，⽽不 是⼀遍⼀遍的创建它们。代码如下：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12<br>13<br>14<br>15<br></th>
    <th>public clas Keys {<br><br>public static final int MAX_KEY = 10_ 0_ 0; private static final Key[] KEYS_CACHE = new<br><br>Key[MAX_KEY];<br><br>static { for (int i = 0; i < MAX_KEY; +i) { KEYS_CACHE[i] = new Key(i); } }<br><br>public static Key of(int value) { return KEYS_CACHE[value]; }</th>
  </tr>
</table>


}

现在开始我们的试验，测试需要做的仅仅是，创建不同size的HashMap（1、10、10、 … 1 0），屏蔽了扩容的情况，代码如下：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br></th>
    <th>static void test(int mapSize) {<br><br>HashMap<Key, Integer> map = new HashMap<Key,Integer><br><br>(mapSize); for (int i = 0; i < mapSize; +i) { map.put(Keys.of(i), i); }<br><br>long beginTime = System.nanoTime(); /获取纳秒 for (int i = 0; i < mapSize; i +) { map.get(Keys.of(i); } long endTime = System.nanoTime(); System.out.println(endTime - beginTime); }<br><br>public static void main(String[] args) { for(int i=10;i<= 1 0 0;i*= 10){ test(i); }</th>
  </tr>
</table>


}

在测试中会查找不同的值，然后度量花费的时间，为了计算getKey的平均时间，我们遍历所有的get⽅ 法，计算总的时间，除以key的数量，计算⼀个平均值，主要⽤来⽐较，绝对值可能会受很多环境因素 的影响。结果如下：

![image 13](<Java8系列之重新认识HashMap.note_images/imageFile13.png>)

性能⽐较表1.png

通过观测测试结果可知，JDK1.8的性能要⾼于JDK1.7 15%以上，在某些size的区域上，甚⾄⾼于 10%。由于Hash算法较均匀，JDK1.8引⼊的红⿊树效果不明显，下⾯我们看看Hash不均匀的的情 况。

## Hash极不均匀的情况

假设我们⼜⼀个⾮常差的Key，它们所有的实例都返回相同的hashCode值。这是使⽤HashMap最坏的 情况。代码修改如下：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br></th>
    <th>clas Key implements Comparable<Key> {<br><br>/.<br><br>@Override public int hashCode() { return 1; }</th>
  </tr>
</table>


9 }

仍然执⾏main⽅法，得出的结果如下表所示：

![image 14](<Java8系列之重新认识HashMap.note_images/imageFile14.png>)

性能⽐较表2.png

从表中结果中可知，随着size的变⼤，JDK1.7的花费时间是增⻓的趋势，⽽JDK1.8是明显的降低趋 势，并且呈现对数增⻓稳定。当⼀个链表太⻓的时候，HashMap会动态的将它替换成⼀个红⿊树，这 话的话会将时间复杂度从O(n)降为O(logn)。hash算法均匀和不均匀所花费的时间明显也不相同，这两 种情况的相对⽐较，可以说明⼀个好的hash算法的重要性。

测试环境：处理器为2.2 GHz Intel Core i7，内存为16 GB 160 MHz DR3， SD硬盘，使⽤默认的JVM参数，运⾏在 64位的OS X 10.10.1上。

# ⼩结

- (1) 扩容是⼀个特别耗性能的操作，所以当程序员在使⽤HashMap的时候，估算map的⼤⼩，初始化的 时候给⼀个⼤致的数值，避免map进⾏频繁的扩容。

- (2) 负载因⼦是可以修改的，也可以⼤于1，但是建议不要轻易修改，除⾮情况⾮常特殊。

- (3) HashMap是线程不安全的，不要在并发的环境中同时操作HashMap，建议使⽤ ConcurentHashMap。

- (4) JDK1.8引⼊红⿊树⼤程度优化了HashMap的性能。

- (5) 还没升级JDK1.8的，现在开始升级吧。HashMap的性能提升仅仅是JDK1.8的冰⼭⼀⻆。


参考

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


JDK1.7&JDK1.8 源码。 CSDN博客频道，HashMap多线程死循环问题，2014。 红⿊联盟，Java类集框架之HashMap(JDK1.8)源码剖析，2015。 CSDN博客频道， 教你初步了解红⿊树，2010。 Java Code Geks，HashMap performance improvements in Java 8，2014。 Importnew，危险！在HashMap中将可变对象⽤作Key，2014。 CSDN博客频道，为什么⼀般hashtable的桶数会取⼀个素数，2013。

