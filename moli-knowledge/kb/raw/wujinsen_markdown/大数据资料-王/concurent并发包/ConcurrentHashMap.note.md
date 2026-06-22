- 第1部分 HashMap介绍 HashMap简介 HashMap 是⼀个散列表，它存储的内容是键值对(key-value)映射。 HashMap 继承于AbstractMap，实现了Map、Cloneable、java.io.Serializable接⼝。 HashMap 的实现不是同步的，这意味着它不是线程安全的。它的key、value都可以为nul。此外， HashMap中的映射不是有序的。 HashMap 的实例有两个参数影响其性能：“初始容量” 和 “加载因⼦”。容量 是哈希表中桶的数量，初 始容量 只是哈希表在创建时的容量。加载因⼦ 是哈希表在其容量⾃动增加之前可以达到多满的⼀种尺 度。当哈希表中的条⽬数超出了加载因⼦与当前容量的乘积时，则要对该哈希表进⾏ rehash 操作（即 重建内部数据结构），从⽽哈希表将具有⼤约两倍的桶数。 通常，默认加载因⼦是 0.75, 这是在时间和空间成本上寻求⼀种折衷。加载因⼦过⾼虽然减少了空间开 销，但同时也增加了查询成本（在⼤多数 HashMap 类的操作中，包括 get 和 put 操作，都反映了这⼀ 点）。在设置初始容量时应该考虑到映射中所需的条⽬数及其加载因⼦，以便最⼤限度地减少 rehash 操作次数。如果初始容量⼤于最⼤条⽬数除以加载因⼦，则不会发⽣ rehash 操作。 HashMap的继承关系


![image 1](<ConcurrentHashMap.note_images/imageFile1.png>)

HashMap与Map关系如下图：

![image 2](<ConcurrentHashMap.note_images/imageFile2.png>)

HashMap的构造函数 HashMap共有4个构造函数,如下： 复制代码 代码如下:

/ 默认构造函数。

HashMap()

/ 指定“容量⼤⼩”的构造函数 HashMap(int capacity)

/ 指定“容量⼤⼩”和“加载因⼦”的构造函数 HashMap(int capacity, float loadFactor)

/ 包含“⼦Map”的构造函数 HashMap(Map<? extends K, ? extends V> map)

HashMap的API 复制代码 代码如下:

void clear() Object clone() bolean containsKey(Object key) bolean containsValue(Object value) Set<Entry<K, V> entrySet() V get(Object key) bolean isEmpty() Set<K> keySet() V put(K key, V value) void putAl(Map<? extends K, ? extends V> map) V remove(Object key) int size() Colection<V> values()

- 第2部分 HashMap源码解析 为了更了解HashMap的原理，下⾯对HashMap源码代码作出分析。 在阅读源码时，建议参考后⾯的说明来建⽴对HashMap的整体认识，这样更容易理解HashMap。 复制代码 代码如下:


package java.util; import java.io.*; public clas HashMap<K,V>

extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable

{

/ 默认的初始容量是16，必须是2的幂。 static final int DEFAULT_INITIAL_CAPACITY = 16;

/ 最⼤容量（必须是2的幂且⼩于2的30次⽅，传⼊容量过⼤将被这个值替换） static final int MAXIMUM_CAPACITY = 1 < 30;

/ 默认加载因⼦

static final float DEFAULT_LOAD_FACTOR = 0.75f; / 存储数据的Entry数组，⻓度是2的幂。 / HashMap是采⽤拉链法实现的，每⼀个Entry本质上是⼀个单向链表

transient Entry[] table;

/ HashMap的⼤⼩，它是HashMap保存的键值对的数量 transient int size;

/ HashMap的阈值，⽤于判断是否需要调整HashMap的容量（threshold = 容量*加载因⼦） int threshold;

/ 加载因⼦实际⼤⼩ final float loadFactor; / HashMap被改变的次数 transient volatile int modCount; / 指定“容量⼤⼩”和“加载因⼦”的构造函数 public HashMap(int initialCapacity, float loadFactor) { if (initialCapacity < 0) throw new IlegalArgumentException("Ilegal initial capacity: " + initialCapacity);

/ HashMap的最⼤容量只能是MAXIMUM_CAPACITY if (initialCapacity > MAXIMUM_CAPACITY)

initialCapacity = MAXIMUM_CAPACITY; if (loadFactor <= 0| Float.isNaN(loadFactor) throw new IlegalArgumentException("Ilegal load factor: " + loadFactor);

/ 找出“⼤于initialCapacity”的最⼩的2的幂 int capacity = 1; while (capacity < initialCapacity)

capacity <= 1; / 设置“加载因⼦”

this.loadFactor = loadFactor;

/ 设置“HashMap阈值”，当HashMap中存储数据的数量达到threshold时，就需要将HashMap的 容量加倍。

threshold = (int)(capacity * loadFactor);

/ 创建Entry数组，⽤来保存数据 table = new Entry[capacity]; init();

} / 指定“容量⼤⼩”的构造函数 public HashMap(int initialCapacity) {

this(initialCapacity, DEFAULT_LOAD_FACTOR); }

/ 默认构造函数。

public HashMap() { / 设置“加载因⼦”

this.loadFactor = DEFAULT_LOAD_FACTOR;

/ 设置“HashMap阈值”，当HashMap中存储数据的数量达到threshold时，就需要将HashMap的 容量加倍。

threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);

/ 创建Entry数组，⽤来保存数据 table = new Entry[DEFAULT_INITIAL_CAPACITY]; init();

} / 包含“⼦Map”的构造函数 public HashMap(Map<? extends K, ? extends V> m) { this(Math.max(int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,

DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR); / 将m中的全部元素逐个添加到HashMap中

putAlForCreate(m);

} static int hash(int h) {

h ^= (h > 20) ^ (h > 12); return h ^ (h > 7) ^ (h > 4);

} / 返回索引值 / h & (length-1)保证返回值的⼩于length

static int indexFor(int h, int length) { return h & (length-1);

} public int size() {

return size;

} public bolean isEmpty() {

return size = 0; }

/ 获取key对应的value public V get(Object key) { if (key = nul)

return getForNulKey(); / 获取key的hash值

int hash = hash(key.hashCode(); / 在“该hash值对应的链表”上查找“键值等于key”的元素

for (Entry<K,V> e = table[indexFor(hash, table.length)]; e != nul; e = e.next) {

Object k; if (e.hash = hash &(k = e.key) = key| key.equals(k)

return e.value;

} return nul;

} / 获取“key为nul”的元素的值 / HashMap将“key为nul”的元素存储在table[0]位置！

private V getForNulKey() { for (Entry<K,V> e = table[0]; e != nul; e = e.next) {

if (e.key = nul) return e.value;

} return nul;

} / HashMap是否包含key public bolean containsKey(Object key) {

return getEntry(key) != nul; }

/ 返回“键为key”的键值对

final Entry<K,V> getEntry(Object key) { / 获取哈希值 / HashMap将“key为nul”的元素存储在table[0]位置，“key不为nul”的则调⽤hash()计算哈希值

int hash = (key = nul) ? 0 : hash(key.hashCode(); / 在“该hash值对应的链表”上查找“键值等于key”的元素

for (Entry<K,V> e = table[indexFor(hash, table.length)]; e != nul; e = e.next) {

Object k; if (e.hash = hash &

(k = e.key) = key| (key != nul & key.equals(k ) return e;

} return nul;

}

/ 将“key-value”添加到HashMap中 public V put(K key, V value) {

/ 若“key为nul”，则将该键值对添加到table[0]中。 if (key = nul)

return putForNulKey(value);

/ 若“key不为nul”，则计算该key的哈希值，然后将其添加到该哈希值对应的链表中。 int hash = hash(key.hashCode(); int i = indexFor(hash, table.length); for (Entry<K,V> e = table[i]; e != nul; e = e.next) {

Object k; / 若“该key”对应的键值对已经存在，则⽤新的value取代旧的value。然后退出！

if (e.hash = hash &(k = e.key) = key| key.equals(k) { V oldValue = e.value; e.value = value; e.recordAces(this); return oldValue;

} }

/ 若“该key”对应的键值对不存在，则将“key-value”添加到table中 modCount+; adEntry(hash, key, value, i); return nul;

}

/ putForNulKey()的作⽤是将“key为nul”键值对添加到table[0]位置 private V putForNulKey(V value) {

for (Entry<K,V> e = table[0]; e != nul; e = e.next) {

if (e.key = nul) { V oldValue = e.value; e.value = value; e.recordAces(this); return oldValue;

} }

/ 这⾥的完全不会被执⾏到! modCount+; adEntry(0, nul, value, 0); return nul;

} / 创建HashMap对应的“添加⽅法”， / 它和put()不同。putForCreate()是内部⽅法，它被构造函数等调⽤，⽤来创建HashMap / ⽽put()是对外提供的往HashMap中添加元素的⽅法。

private void putForCreate(K key, V value) { int hash = (key = nul) ? 0 : hash(key.hashCode(); int i = indexFor(hash, table.length);

/ 若该HashMap表中存在“键值等于key”的元素，则替换该元素的value值

for (Entry<K,V> e = table[i]; e != nul; e = e.next) { Object k; if (e.hash = hash &

(k = e.key) = key| (key != nul & key.equals(k ) { e.value = value; return;

} }

/ 若该HashMap表中不存在“键值等于key”的元素，则将该key-value添加到HashMap中 createEntry(hash, key, value, i);

} / 将“m”中的全部元素都添加到HashMap中。 / 该⽅法被内部的构造HashMap的⽅法所调⽤。

private void putAlForCreate(Map<? extends K, ? extends V> m) { / 利⽤迭代器将元素逐个添加到HashMap中

for (Iterator<? extends Map.Entry<? extends K, ? extends V> i = m.entrySet().iterator(); i.hasNext(); ) {

Map.Entry<? extends K, ? extends V> e = i.next(); putForCreate(e.getKey(), e.getValue();

} }

/ 重新调整HashMap的⼤⼩，newCapacity是调整后的单位

void resize(int newCapacity) { Entry[] oldTable = table; int oldCapacity = oldTable.length; if (oldCapacity = MAXIMUM_CAPACITY) {

threshold = Integer.MAX_VALUE; return;

} / 新建⼀个HashMap，将“旧HashMap”的全部元素添加到“新HashMap”中， / 然后，将“新HashMap”赋值给“旧HashMap”。

Entry[] newTable = new Entry[newCapacity]; transfer(newTable); table = newTable; threshold = (int)(newCapacity * loadFactor);

} / 将HashMap中的全部元素都添加到newTable中

void transfer(Entry[] newTable) { Entry[] src = table; int newCapacity = newTable.length; for (int j = 0; j < src.length; j +) {

Entry<K,V> e = src[j]; if (e != nul) {

src[j] = nul; do {

Entry<K,V> next = e.next; int i = indexFor(e.hash, newCapacity); e.next = newTable[i]; newTable[i] = e; e = next;

} while (e != nul); }

} }

/ 将"m"的全部元素都添加到HashMap中 public void putAl(Map<? extends K, ? extends V> m) {

/ 有效性判断 int numKeysToBeAded = m.size(); if (numKeysToBeAded = 0)

return; / 计算容量是否⾜够， / 若“当前实际容量 < 需要的容量”，则将容量x2。

if (numKeysToBeAded > threshold) { int targetCapacity = (int)(numKeysToBeAded / loadFactor + 1); if (targetCapacity > MAXIMUM_CAPACITY)

targetCapacity = MAXIMUM_CAPACITY; int newCapacity = table.length; while (newCapacity < targetCapacity)

newCapacity <= 1; if (newCapacity > table.length)

resize(newCapacity); }

/ 通过迭代器，将“m”中的元素逐个添加到HashMap中。 for (Iterator<? extends Map.Entry<? extends K, ? extends V> i = m.entrySet().iterator();

i.hasNext(); ) { Map.Entry<? extends K, ? extends V> e = i.next(); put(e.getKey(), e.getValue();

} }

/ 删除“键为key”元素

public V remove(Object key) { Entry<K,V> e = removeEntryForKey(key); return (e = nul ? nul : e.value);

} / 删除“键为key”的元素 final Entry<K,V> removeEntryForKey(Object key) {

/ 获取哈希值。若key为nul，则哈希值为0；否则调⽤hash()进⾏计算 int hash = (key = nul) ? 0 : hash(key.hashCode(); int i = indexFor(hash, table.length); Entry<K,V> prev = table[i]; Entry<K,V> e = prev;

/ 删除链表中“键为key”的元素 / 本质是“删除单向链表中的节点”

while (e != nul) { Entry<K,V> next = e.next; Object k; if (e.hash = hash &

(k = e.key) = key| (key != nul & key.equals(k ) { modCount+; size-; if (prev = e)

table[i] = next; else

prev.next = next; e.recordRemoval(this); return e;

} prev = e; e = next;

} return e;

} / 删除“键值对” final Entry<K,V> removeMaping(Object o) { if (!(o instanceof Map.Entry)

return nul; Map.Entry<K,V> entry = (Map.Entry<K,V>) o; Object key = entry.getKey(); int hash = (key = nul) ? 0 : hash(key.hashCode(); int i = indexFor(hash, table.length); Entry<K,V> prev = table[i]; Entry<K,V> e = prev;

/ 删除链表中的“键值对e” / 本质是“删除单向链表中的节点”

while (e != nul) { Entry<K,V> next = e.next; if (e.hash = hash & e.equals(entry) {

modCount+;

size-; if (prev = e)

table[i] = next; else

prev.next = next; e.recordRemoval(this); return e;

} prev = e; e = next;

} return e;

} / 清空HashMap，将所有的元素设为nul

public void clear() { modCount+; Entry[] tab = table; for (int i = 0; i < tab.length; i +)

tab[i] = nul; size = 0;

} / 是否包含“值为value”的元素 public bolean containsValue(Object value) {

/ 若“value为nul”，则调⽤containsNulValue()查找 if (value = nul)

return containsNulValue();

/ 若“value不为nul”，则查找HashMap中是否有值为value的节点。 Entry[] tab = table;

for (int i = 0; i < tab.length ; i +) for (Entry e = tab[i] ; e != nul ; e = e.next) if (value.equals(e.value)

return true; return false; }

/ 是否包含nul值 private bolean containsNulValue() { Entry[] tab = table;

for (int i = 0; i < tab.length ; i +) for (Entry e = tab[i] ; e != nul ; e = e.next) if (e.value = nul)

return true; return false; }

/ 克隆⼀个HashMap，并返回Object对象

public Object clone() { HashMap<K,V> result = nul; try {

result = (HashMap<K,V>)super.clone(); } catch (CloneNotSuportedException e) { / asert false;

} result.table = new Entry[table.length]; result.entrySet = nul; result.modCount = 0; result.size = 0; result.init();

/ 调⽤putAlForCreate()将全部元素添加到HashMap中 result.putAlForCreate(this); return result;

} / Entry是单向链表。 / 它是 “HashMap链式存储法”对应的链表。 / 它实现了Map.Entry 接⼝，即实现getKey(), getValue(), setValue(V value), equals(Object o),

hashCode()这些函数

static clas Entry<K,V> implements Map.Entry<K,V> { final K key; V value;

/ 指向下⼀个节点 Entry<K,V> next; final int hash; / 构造函数。 / 输⼊参数包括"哈希值(h)", "键(k)", "值(v)", "下⼀节点(n)"

Entry(int h, K k, V v, Entry<K,V> n) { value = v;

next = n; key = k; hash = h;

} public final K getKey() {

return key;

} public final V getValue() {

return value;

} public final V setValue(V newValue) {

V oldValue = value; value = newValue; return oldValue;

} / 判断两个Entry是否相等 / 若两个Entry的“key”和“value”都相等，则返回true。 / 否则，返回false

public final bolean equals(Object o) { if (!(o instanceof Map.Entry) return false; Map.Entry e = (Map.Entry)o;

- Object k1 = getKey();
- Object k2 = e.getKey(); if (k1 = k2| (k1 != nul & k1.equals(k2) {


Object v1 = getValue(); Object v2 = e.getValue(); if (v1 = v2| (v1 != nul & v1.equals(v2)

return true;

} return false;

} / 实现hashCode() public final int hashCode() { return (key=nul ? 0 : key.hashCode() ^

(value=nul ? 0 : value.hashCode(); }

public final String toString() { return getKey() + "=" + getValue();

} / 当向HashMap中添加元素时，绘调⽤recordAces()。 / 这⾥不做任何处理

void recordAces(HashMap<K,V> m) { }

/ 当从HashMap中删除元素时，绘调⽤recordRemoval()。 / 这⾥不做任何处理

void recordRemoval(HashMap<K,V> m) { }

}

/ 新增Entry。将“key-value”插⼊指定位置，bucketIndex是位置索引。 void adEntry(int hash, K key, V value, int bucketIndex) {

/ 保存“bucketIndex”位置的值到“e”中

Entry<K,V> e = table[bucketIndex]; / 设置“bucketIndex”位置的元素为“新Entry”， / 设置“e”为“新Entry的下⼀个节点”

table[bucketIndex] = new Entry<K,V>(hash, key, value, e);

/ 若HashMap的实际⼤⼩ 不⼩于 “阈值”，则调整HashMap的⼤⼩ if (size+ >= threshold)

resize(2 * table.length);

} / 创建Entry。将“key-value”插⼊指定位置，bucketIndex是位置索引。 / 它和adEntry的区别是：

- / (01) adEntry()⼀般⽤在 新增Entry可能导致“HashMap的实际容量”超过“阈值”的情况下。 / 例如，我们新建⼀个HashMap，然后不断通过put()向HashMap中添加元素； / put()是通过adEntry()新增Entry的。 / 在这种情况下，我们不知道何时“HashMap的实际容量”会超过“阈值”； / 因此，需要调⽤adEntry()
- / (02) createEntry() ⼀般⽤在 新增Entry不会导致“HashMap的实际容量”超过“阈值”的情况下。 / 例如，我们调⽤HashMap“带有Map”的构造函数，它绘将Map的全部元素添加到HashMap中； / 但在添加之前，我们已经计算好“HashMap的容量和阈值”。也就是，可以确定“即使将Map中 / 的全部元素添加到HashMap中，都不会超过HashMap的阈值”。 / 此时，调⽤createEntry()即可。


void createEntry(int hash, K key, V value, int bucketIndex) { / 保存“bucketIndex”位置的值到“e”中

Entry<K,V> e = table[bucketIndex]; / 设置“bucketIndex”位置的元素为“新Entry”， / 设置“e”为“新Entry的下⼀个节点”

table[bucketIndex] = new Entry<K,V>(hash, key, value, e); size+;

} / HashIterator是HashMap迭代器的抽象出来的⽗类，实现了公共了函数。 / 它包含“key迭代器(KeyIterator)”、“Value迭代器(ValueIterator)”和“Entry迭代器(EntryIterator)”3

个⼦类。 private abstract clas HashIterator<E> implements Iterator<E> { / 下⼀个元素 Entry<K,V> next;

/ expectedModCount⽤于实现fast-fail机制。 int expectedModCount;

/ 当前索引 int index;

/ 当前元素 Entry<K,V> curent; HashIterator() {

expectedModCount = modCount; if (size > 0) {/ advance to first entry

Entry[] t = table; / 将next指向table中第⼀个不为nul的元素。 / 这⾥利⽤了index的初始值为0，从0开始依次向后遍历，直到找到不为nul的元素就退出循

环。

while (index < t.length & (next = t[index+]) = nul)

}

} public final bolean hasNext() {

return next != nul; }

/ 获取下⼀个元素 final Entry<K,V> nextEntry() { if (modCount != expectedModCount)

throw new ConcurentModificationException(); Entry<K,V> e = next;

if (e = nul)

throw new NoSuchElementException(); / 注意！！！ / ⼀个Entry就是⼀个单向链表 / 若该Entry的下⼀个节点不为空，就将next指向下⼀个节点; / 否则，将next指向下⼀个链表(也是下⼀个Entry)的不为nul的节点。

if (next = e.next) = nul) { Entry[] t = table; while (index < t.length & (next = t[index+]) = nul)

} curent = e; return e;

} / 删除当前元素

public void remove() { if (curent = nul)

throw new IlegalStateException(); if (modCount != expectedModCount)

throw new ConcurentModificationException(); Object k = curent.key; curent = nul; HashMap.this.removeEntryForKey(k); expectedModCount = modCount;

} }

/ value的迭代器 private final clas ValueIterator extends HashIterator<V> { public V next() {

return nextEntry().value; }

} / key的迭代器 private final clas KeyIterator extends HashIterator<K> { public K next() {

return nextEntry().getKey(); }

} / Entry的迭代器 private final clas EntryIterator extends HashIterator<Map.Entry<K,V> { public Map.Entry<K,V> next() {

return nextEntry(); }

} / 返回⼀个“key迭代器” Iterator<K> newKeyIterator() {

return new KeyIterator(); }

/ 返回⼀个“value迭代器” Iterator<V> newValueIterator() {

return new ValueIterator(); }

/ 返回⼀个“entry迭代器” Iterator<Map.Entry<K,V> newEntryIterator() {

return new EntryIterator(); }

/ HashMap的Entry对应的集合 private transient Set<Map.Entry<K,V> entrySet = nul; / 返回“key的集合”，实际上返回⼀个“KeySet对象”

public Set<K> keySet() { Set<K> ks = keySet; return (ks != nul ? ks : (keySet = new KeySet( );

} / Key对应的集合 / KeySet继承于AbstractSet，说明该集合中没有重复的Key。

private final clas KeySet extends AbstractSet<K> {

public Iterator<K> iterator() { return newKeyIterator();

} public int size() {

return size;

} public bolean contains(Object o) {

return containsKey(o);

} public bolean remove(Object o) {

return HashMap.this.removeEntryForKey(o) != nul;

} public void clear() {

HashMap.this.clear(); }

} / 返回“value集合”，实际上返回的是⼀个Values对象

public Colection<V> values() { Colection<V> vs = values; return (vs != nul ? vs : (values = new Values( );

} / “value集合” / Values继承于AbstractColection，不同于“KeySet继承于AbstractSet”， / Values中的元素能够重复。因为不同的key可以指向相同的value。

private final clas Values extends AbstractColection<V> { public Iterator<V> iterator() { return newValueIterator();

} public int size() {

return size;

} public bolean contains(Object o) {

return containsValue(o);

} public void clear() {

HashMap.this.clear(); }

} / 返回“HashMap的Entry集合” public Set<Map.Entry<K,V> entrySet() {

return entrySet0(); }

/ 返回“HashMap的Entry集合”，它实际是返回⼀个EntrySet对象 private Set<Map.Entry<K,V> entrySet0() {

Set<Map.Entry<K,V> es = entrySet;

return es != nul ? es : (entrySet = new EntrySet();

} / EntrySet对应的集合 / EntrySet继承于AbstractSet，说明该集合中没有重复的EntrySet。

private final clas EntrySet extends AbstractSet<Map.Entry<K,V> { public Iterator<Map.Entry<K,V> iterator() { return newEntryIterator();

} public bolean contains(Object o) {

if (!(o instanceof Map.Entry)

return false; Map.Entry<K,V> e = (Map.Entry<K,V>) o; Entry<K,V> candidate = getEntry(e.getKey(); return candidate != nul & candidate.equals(e);

} public bolean remove(Object o) {

return removeMaping(o) != nul;

} public int size() {

return size;

} public void clear() {

HashMap.this.clear(); }

} / java.io.Serializable的写⼊函数 / 将HashMap的“总的容量，实际容量，所有的Entry”都写⼊到输出流中

private void writeObject(java.io.ObjectOutputStream s)

throws IOException {

Iterator<Map.Entry<K,V> i = (size > 0) ? entrySet0().iterator() : nul;

/ Write out the threshold, loadfactor, and any hi den stuf s.defaultWriteObject();

/ Write out number of buckets s.writeInt(table.length);

/ Write out size (number of Mapings)

s.writeInt(size); / Write out keys and values (alternating)

if (i != nul) { while (i.hasNext() { Map.Entry<K,V> e = i.next(); s.writeObject(e.getKey(); s.writeObject(e.getValue(); }

}

} private static final long serialVersionUID = 36249820763181265L;

/ java.io.Serializable的读取函数：根据写⼊⽅式读出 / 将HashMap的“总的容量，实际容量，所有的Entry”依次读出

private void readObject(java.io.ObjectInputStream s) throws IOException, ClasNotFoundException {

/ Read in the threshold, loadfactor, and any hi den stuf s.defaultReadObject();

/ Read in number of buckets and alocate the bucket aray; int numBuckets = s.readInt(); table = new Entry[numBuckets]; init(); / Give subclas a chance to do its thing.

/ Read in size (number of Mapings) int size = s.readInt();

/ Read the keys and values, and put the mapings in the HashMap

for (int i=0; i<size; i +) { K key = (K) s.readObject(); V value = (V) s.readObject(); putForCreate(key, value);

} }

/ 返回“HashMap总的容量” int capacity() { return table.length; } / 返回“HashMap的加载因⼦” float loadFactor() { return loadFactor; }

说明: 在详细介绍HashMap的代码之前，我们需要了解：HashMap就是⼀个散列表，它是通过“拉链法”解决 哈希冲突的。 还需要再补充说明的⼀点是影响HashMap性能的有两个参数：初始容量(initialCapacity) 和加载因⼦ (loadFactor)。容量 是哈希表中桶的数量，初始容量只是哈希表在创建时的容量。加载因⼦ 是哈希表 在其容量⾃动增加之前可以达到多满的⼀种尺度。当哈希表中的条⽬数超出了加载因⼦与当前容量的 乘积时，则要对该哈希表进⾏ rehash 操作（即重建内部数据结构），从⽽哈希表将具有⼤约两倍的桶 数。

- 第2.1部分 HashMap的“拉链法”相关内容


- 2.1.1 HashMap数据存储数组 transient Entry[] table; HashMap中的key-value都是存储在Entry数组中的。
- 2.1.2 数据节点Entry的数据结构 复制代码 代码如下:


static clas Entry<K,V> implements Map.Entry<K,V> { final K key; V value;

/ 指向下⼀个节点 Entry<K,V> next; final int hash; / 构造函数。 / 输⼊参数包括"哈希值(h)", "键(k)", "值(v)", "下⼀节点(n)"

Entry(int h, K k, V v, Entry<K,V> n) { value = v; next = n; key = k; hash = h;

} public final K getKey() {

return key;

} public final V getValue() {

return value;

} public final V setValue(V newValue) {

V oldValue = value;

value = newValue; return oldValue;

} / 判断两个Entry是否相等 / 若两个Entry的“key”和“value”都相等，则返回true。 / 否则，返回false

public final bolean equals(Object o) { if (!(o instanceof Map.Entry) return false; Map.Entry e = (Map.Entry)o;

- Object k1 = getKey();
- Object k2 = e.getKey(); if (k1 = k2| (k1 != nul & k1.equals(k2) {


Object v1 = getValue(); Object v2 = e.getValue(); if (v1 = v2| (v1 != nul & v1.equals(v2)

return true;

} return false;

} / 实现hashCode() public final int hashCode() { return (key=nul ? 0 : key.hashCode() ^ (value=nul ? 0 : value.hashCode();

} public final String toString() {

return getKey() + "=" + getValue();

} / 当向HashMap中添加元素时，绘调⽤recordAces()。 / 这⾥不做任何处理

void recordAces(HashMap<K,V> m) { }

/ 当从HashMap中删除元素时，绘调⽤recordRemoval()。 / 这⾥不做任何处理

void recordRemoval(HashMap<K,V> m) { }

从中，我们可以看出 Entry 实际上就是⼀个单向链表。这也是为什么我们说HashMap是通过拉链法解 决哈希冲突的。 Entry 实现了Map.Entry 接⼝，即实现getKey(), getValue(), setValue(V value), equals(Object o), hashCode()这些函数。这些都是基本的读取/修改key、value值的函数。

- 第2.2部分 HashMap的构造函数 HashMap共包括4个构造函数 复制代码 代码如下:


/ 默认构造函数。

public HashMap() { / 设置“加载因⼦”

this.loadFactor = DEFAULT_LOAD_FACTOR;

/ 设置“HashMap阈值”，当HashMap中存储数据的数量达到threshold时，就需要将HashMap的容 量加倍。

threshold = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);

/ 创建Entry数组，⽤来保存数据 table = new Entry[DEFAULT_INITIAL_CAPACITY]; init();

} / 指定“容量⼤⼩”和“加载因⼦”的构造函数 public HashMap(int initialCapacity, float loadFactor) { if (initialCapacity < 0) throw new IlegalArgumentException("Ilegal initial capacity: " + initialCapacity);

/ HashMap的最⼤容量只能是MAXIMUM_CAPACITY if (initialCapacity > MAXIMUM_CAPACITY)

initialCapacity = MAXIMUM_CAPACITY; if (loadFactor <= 0| Float.isNaN(loadFactor) throw new IlegalArgumentException("Ilegal load factor: " + loadFactor);

/ Find a power of 2 >= initialCapacity int capacity = 1; while (capacity < initialCapacity)

capacity <= 1; / 设置“加载因⼦”

this.loadFactor = loadFactor;

/ 设置“HashMap阈值”，当HashMap中存储数据的数量达到threshold时，就需要将HashMap的容 量加倍。

threshold = (int)(capacity * loadFactor);

/ 创建Entry数组，⽤来保存数据 table = new Entry[capacity]; init();

} / 指定“容量⼤⼩”的构造函数 public HashMap(int initialCapacity) {

this(initialCapacity, DEFAULT_LOAD_FACTOR); }

/ 包含“⼦Map”的构造函数 public HashMap(Map<? extends K, ? extends V> m) { this(Math.max(int) (m.size() / DEFAULT_LOAD_FACTOR) + 1,

DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR); / 将m中的全部元素逐个添加到HashMap中

putAlForCreate(m); }

- 第2.3部分 HashMap的主要对外接⼝


- 2.3.1 clear() clear() 的作⽤是清空HashMap。它是通过将所有的元素设为nul来实现的。 复制代码 代码如下:

public void clear() { modCount+; Entry[] tab = table; for (int i = 0; i < tab.length; i +)

tab[i] = nul; size = 0;

}

- 2.3.2 containsKey() containsKey() 的作⽤是判断HashMap是否包含key。 复制代码 代码如下:


public bolean containsKey(Object key) {

return getEntry(key) != nul; }

containsKey() ⾸先通过getEntry(key)获取key对应的Entry，然后判断该Entry是否为nul。 getEntry()的源码如下： 复制代码 代码如下:

final Entry<K,V> getEntry(Object key) { / 获取哈希值 / HashMap将“key为nul”的元素存储在table[0]位置，“key不为nul”的则调⽤hash()计算哈希值

int hash = (key = nul) ? 0 : hash(key.hashCode(); / 在“该hash值对应的链表”上查找“键值等于key”的元素

for (Entry<K,V> e = table[indexFor(hash, table.length)]; e != nul; e = e.next) {

Object k; if (e.hash = hash &

(k = e.key) = key| (key != nul & key.equals(k ) return e;

} return nul;

}

getEntry() 的作⽤就是返回“键为key”的键值对，它的实现源码中已经进⾏了说明。 这⾥需要强调的是：HashMap将“key为nul”的元素都放在table的位置0处，即table[0]中；“key不为 nul”的放在table的其余位置！

- 2.3.3 containsValue() containsValue() 的作⽤是判断HashMap是否包含“值为value”的元素。 复制代码 代码如下:


public bolean containsValue(Object value) {

/ 若“value为nul”，则调⽤containsNulValue()查找 if (value = nul)

return containsNulValue();

/ 若“value不为nul”，则查找HashMap中是否有值为value的节点。 Entry[] tab = table;

for (int i = 0; i < tab.length ; i +) for (Entry e = tab[i] ; e != nul ; e = e.next) if (value.equals(e.value)

return true; return false;

}

从中，我们可以看出containsNulValue()分为两步进⾏处理：第⼀，若“value为nul”，则调⽤ containsNulValue()。第⼆，若“value不为nul”，则查找HashMap中是否有值为value的节点。 containsNulValue() 的作⽤判断HashMap中是否包含“值为nul”的元素。 复制代码 代码如下:

private bolean containsNulValue() { Entry[] tab = table; for (int i = 0; i < tab.length ; i +)

for (Entry e = tab[i] ; e != nul ; e = e.next) if (e.value = nul)

return true; return false;

}

- 2.3.4 entrySet()、values()、keySet() 它们3个的原理类似，这⾥以entrySet()为例来说明。 entrySet()的作⽤是返回“HashMap中所有Entry的集合”，它是⼀个集合。实现代码如下： 复制代码 代码如下:


/ 返回“HashMap的Entry集合” public Set<Map.Entry<K,V> entrySet() {

return entrySet0(); }

/ 返回“HashMap的Entry集合”，它实际是返回⼀个EntrySet对象

private Set<Map.Entry<K,V> entrySet0() { Set<Map.Entry<K,V> es = entrySet; return es != nul ? es : (entrySet = new EntrySet();

} / EntrySet对应的集合

/ EntrySet继承于AbstractSet，说明该集合中没有重复的EntrySet。 private final clas EntrySet extends AbstractSet<Map.Entry<K,V> { public Iterator<Map.Entry<K,V> iterator() { return newEntryIterator();

} public bolean contains(Object o) {

if (!(o instanceof Map.Entry)

return false; Map.Entry<K,V> e = (Map.Entry<K,V>) o; Entry<K,V> candidate = getEntry(e.getKey(); return candidate != nul & candidate.equals(e);

} public bolean remove(Object o) {

return removeMaping(o) != nul;

} public int size() {

return size;

} public void clear() {

HashMap.this.clear(); }

}

HashMap是通过拉链法实现的散列表。表现在HashMap包括许多的Entry，⽽每⼀个Entry本质上⼜是 ⼀个单向链表。那么HashMap遍历key-value键值对的时候，是如何逐个去遍历的呢？ 下⾯我们就看看HashMap是如何通过entrySet()遍历的。 entrySet()实际上是通过newEntryIterator()实现的。 下⾯我们看看它的代码： 复制代码 代码如下:

/ 返回⼀个“entry迭代器” Iterator<Map.Entry<K,V> newEntryIterator() {

return new EntryIterator(); }

/ Entry的迭代器 private final clas EntryIterator extends HashIterator<Map.Entry<K,V> { public Map.Entry<K,V> next() { return nextEntry();

}

} / HashIterator是HashMap迭代器的抽象出来的⽗类，实现了公共了函数。 / 它包含“key迭代器(KeyIterator)”、“Value迭代器(ValueIterator)”和“Entry迭代器(EntryIterator)”3个

⼦类。 private abstract clas HashIterator<E> implements Iterator<E> {

/ 下⼀个元素 Entry<K,V> next;

/ expectedModCount⽤于实现fast-fail机制。 int expectedModCount;

/ 当前索引 int index;

/ 当前元素 Entry<K,V> curent; HashIterator() {

expectedModCount = modCount; if (size > 0) {/ advance to first entry

Entry[] t = table; / 将next指向table中第⼀个不为nul的元素。 / 这⾥利⽤了index的初始值为0，从0开始依次向后遍历，直到找到不为nul的元素就退出循

环。

while (index < t.length & (next = t[index+]) = nul)

}

} public final bolean hasNext() {

return next != nul; }

/ 获取下⼀个元素 final Entry<K,V> nextEntry() { if (modCount != expectedModCount)

throw new ConcurentModificationException(); Entry<K,V> e = next; if (e = nul)

throw new NoSuchElementException(); / 注意！！！ / ⼀个Entry就是⼀个单向链表

/ 若该Entry的下⼀个节点不为空，就将next指向下⼀个节点; / 否则，将next指向下⼀个链表(也是下⼀个Entry)的不为nul的节点。

if (next = e.next) = nul) { Entry[] t = table; while (index < t.length & (next = t[index+]) = nul)

} curent = e; return e;

} / 删除当前元素

public void remove() { if (curent = nul)

throw new IlegalStateException(); if (modCount != expectedModCount)

throw new ConcurentModificationException(); Object k = curent.key; curent = nul; HashMap.this.removeEntryForKey(k); expectedModCount = modCount;

} }

当我们通过entrySet()获取到的Iterator的next()⽅法去遍历HashMap时，实际上调⽤的是 nextEntry() 。⽽nextEntry()的实现⽅式，先遍历Entry(根据Entry在table中的序号，从⼩到⼤的遍历)；然后对每个 Entry(即每个单向链表)，逐个遍历。

- 2.3.5 get() get() 的作⽤是获取key对应的value，它的实现代码如下： 复制代码 代码如下:


public V get(Object key) { if (key = nul)

return getForNulKey(); / 获取key的hash值

int hash = hash(key.hashCode(); / 在“该hash值对应的链表”上查找“键值等于key”的元素 for (Entry<K,V> e = table[indexFor(hash, table.length)];

e != nul; e = e.next) {

Object k; if (e.hash = hash &(k = e.key) = key| key.equals(k)

return e.value;

} return nul;

}

- 2.3.6 put() put() 的作⽤是对外提供接⼝，让HashMap对象可以通过put()将“key-value”添加到HashMap中。 复制代码 代码如下:


public V put(K key, V value) {

/ 若“key为nul”，则将该键值对添加到table[0]中。 if (key = nul)

return putForNulKey(value);

/ 若“key不为nul”，则计算该key的哈希值，然后将其添加到该哈希值对应的链表中。 int hash = hash(key.hashCode(); int i = indexFor(hash, table.length); for (Entry<K,V> e = table[i]; e != nul; e = e.next) {

Object k; / 若“该key”对应的键值对已经存在，则⽤新的value取代旧的value。然后退出！

if (e.hash = hash &(k = e.key) = key| key.equals(k) { V oldValue = e.value; e.value = value; e.recordAces(this); return oldValue;

} }

/ 若“该key”对应的键值对不存在，则将“key-value”添加到table中 modCount+; adEntry(hash, key, value, i); return nul;

}

若要添加到HashMap中的键值对对应的key已经存在HashMap中，则找到该键值对；然后新的value取 代旧的value，并退出！ 若要添加到HashMap中的键值对对应的key不在HashMap中，则将其添加到该哈希值对应的链表中， 并调⽤adEntry()。 下⾯看看adEntry()的代码： 复制代码 代码如下:

void adEntry(int hash, K key, V value, int bucketIndex) { / 保存“bucketIndex”位置的值到“e”中

Entry<K,V> e = table[bucketIndex]; / 设置“bucketIndex”位置的元素为“新Entry”， / 设置“e”为“新Entry的下⼀个节点”

table[bucketIndex] = new Entry<K,V>(hash, key, value, e);

/ 若HashMap的实际⼤⼩ 不⼩于 “阈值”，则调整HashMap的⼤⼩ if (size+ >= threshold)

resize(2 * table.length); }

adEntry() 的作⽤是新增Entry。将“key-value”插⼊指定位置，bucketIndex是位置索引。 说到adEntry()，就不得不说另⼀个函数createEntry()。createEntry()的代码如下： 复制代码 代码如下:

void createEntry(int hash, K key, V value, int bucketIndex) { / 保存“bucketIndex”位置的值到“e”中

Entry<K,V> e = table[bucketIndex]; / 设置“bucketIndex”位置的元素为“新Entry”， / 设置“e”为“新Entry的下⼀个节点”

table[bucketIndex] = new Entry<K,V>(hash, key, value, e); size+;

}

它们的作⽤都是将key、value添加到HashMap中。⽽且，⽐较adEntry()和createEntry()的代码，我 们发现adEntry()多了两句： if (size+ >= threshold)

resize(2 * table.length); 那它们的区别到底是什么呢？ 阅读代码，我们可以发现，它们的使⽤情景不同。

- (01) adEntry()⼀般⽤在 新增Entry可能导致“HashMap的实际容量”超过“阈值”的情况下。 例如，我们新建⼀个HashMap，然后不断通过put()向HashMap中添加元素；put()是通过

adEntry()新增Entry的。 在这种情况下，我们不知道何时“HashMap的实际容量”会超过“阈值”； 因此，需要调⽤adEntry()

- (02) createEntry() ⼀般⽤在 新增Entry不会导致“HashMap的实际容量”超过“阈值”的情况下。 例如，我们调⽤HashMap“带有Map”的构造函数，它绘将Map的全部元素添加到HashMap中；


但在添加之前，我们已经计算好“HashMap的容量和阈值”。也就是，可以确定“即使将Map中的全 部元素添加到HashMap中，都不会超过HashMap的阈值”。

此时，调⽤createEntry()即可。

- 2.3.7 putAl() putAl() 的作⽤是将"m"的全部元素都添加到HashMap中，它的代码如下： 复制代码 代码如下:


public void putAl(Map<? extends K, ? extends V> m) {

/ 有效性判断 int numKeysToBeAded = m.size(); if (numKeysToBeAded = 0)

return; / 计算容量是否⾜够， / 若“当前实际容量 < 需要的容量”，则将容量x2。

if (numKeysToBeAded > threshold) { int targetCapacity = (int)(numKeysToBeAded / loadFactor + 1); if (targetCapacity > MAXIMUM_CAPACITY)

targetCapacity = MAXIMUM_CAPACITY; int newCapacity = table.length; while (newCapacity < targetCapacity)

newCapacity <= 1; if (newCapacity > table.length)

resize(newCapacity); }

/ 通过迭代器，将“m”中的元素逐个添加到HashMap中。 for (Iterator<? extends Map.Entry<? extends K, ? extends V> i = m.entrySet().iterator();

i.hasNext(); ) { Map.Entry<? extends K, ? extends V> e = i.next(); put(e.getKey(), e.getValue();

}

- 2.3.8 remove() remove() 的作⽤是删除“键为key”元素 复制代码 代码如下:


public V remove(Object key) { Entry<K,V> e = removeEntryForKey(key); return (e = nul ? nul : e.value);

} / 删除“键为key”的元素 final Entry<K,V> removeEntryForKey(Object key) {

/ 获取哈希值。若key为nul，则哈希值为0；否则调⽤hash()进⾏计算 int hash = (key = nul) ? 0 : hash(key.hashCode(); int i = indexFor(hash, table.length); Entry<K,V> prev = table[i]; Entry<K,V> e = prev;

/ 删除链表中“键为key”的元素 / 本质是“删除单向链表中的节点”

while (e != nul) { Entry<K,V> next = e.next; Object k; if (e.hash = hash &

(k = e.key) = key| (key != nul & key.equals(k ) { modCount+; size-; if (prev = e)

table[i] = next; else

prev.next = next; e.recordRemoval(this); return e;

} prev = e; e = next;

return e; }

- 第2.4部分 HashMap实现的Cloneable接⼝ HashMap实现了Cloneable接⼝，即实现了clone()⽅法。 clone()⽅法的作⽤很简单，就是克隆⼀个HashMap对象并返回。 复制代码 代码如下:

/ 克隆⼀个HashMap，并返回Object对象

public Object clone() { HashMap<K,V> result = nul; try {

result = (HashMap<K,V>)super.clone(); } catch (CloneNotSuportedException e) { / asert false;

} result.table = new Entry[table.length]; result.entrySet = nul; result.modCount = 0; result.size = 0; result.init();

/ 调⽤putAlForCreate()将全部元素添加到HashMap中 result.putAlForCreate(this); return result;

}

- 第2.5部分 HashMap实现的Serializable接⼝ HashMap实现java.io.Serializable，分别实现了串⾏读取、写⼊功能。 串⾏写⼊函数是writeObject()，它的作⽤是将HashMap的“总的容量，实际容量，所有的Entry”都写⼊ 到输出流中。 ⽽串⾏读取函数是readObject()，它的作⽤是将HashMap的“总的容量，实际容量，所有的Entry”依次 读出 复制代码 代码如下:


/ java.io.Serializable的写⼊函数 / 将HashMap的“总的容量，实际容量，所有的Entry”都写⼊到输出流中

private void writeObject(java.io.ObjectOutputStream s)

throws IOException {

Iterator<Map.Entry<K,V> i = (size > 0) ? entrySet0().iterator() : nul;

/ Write out the threshold, loadfactor, and any hi den stuf s.defaultWriteObject();

/ Write out number of buckets s.writeInt(table.length);

/ Write out size (number of Mapings) s.writeInt(size);

/ Write out keys and values (alternating)

if (i != nul) { while (i.hasNext() { Map.Entry<K,V> e = i.next(); s.writeObject(e.getKey(); s.writeObject(e.getValue(); }

}

} / java.io.Serializable的读取函数：根据写⼊⽅式读出 / 将HashMap的“总的容量，实际容量，所有的Entry”依次读出

private void readObject(java.io.ObjectInputStream s) throws IOException, ClasNotFoundException {

/ Read in the threshold, loadfactor, and any hi den stuf s.defaultReadObject();

/ Read in number of buckets and alocate the bucket aray; int numBuckets = s.readInt(); table = new Entry[numBuckets]; init(); / Give subclas a chance to do its thing.

/ Read in size (number of Mapings) int size = s.readInt();

/ Read the keys and values, and put the mapings in the HashMap

for (int i=0; i<size; i +) { K key = (K) s.readObject(); V value = (V) s.readObject();

putForCreate(key, value); }

}

- 第3部分 HashMap遍历⽅式


- 3.1 遍历HashMap的键值对 第⼀步：根据entrySet()获取HashMap的“键值对”的Set集合。 第⼆步：通过Iterator迭代器遍历“第⼀步”得到的集合。 复制代码 代码如下:

/ 假设map是HashMap对象 / map中的key是String类型，value是Integer类型

Integer integ = nul; Iterator iter = map.entrySet().iterator(); while(iter.hasNext() {

Map.Entry entry = (Map.Entry)iter.next(); / 获取key key = (String)entry.getKey(); / 获取value

integ = (Integer)entry.getValue(); }

- 3.2 遍历HashMap的键 第⼀步：根据keySet()获取HashMap的“键”的Set集合。 第⼆步：通过Iterator迭代器遍历“第⼀步”得到的集合。 复制代码 代码如下:


/ 假设map是HashMap对象 / map中的key是String类型，value是Integer类型

String key = nul; Integer integ = nul; Iterator iter = map.keySet().iterator(); while (iter.hasNext() {

/ 获取key key = (String)iter.next(); / 根据key，获取value

integ = (Integer)map.get(key); }

- 3.3 遍历HashMap的值 第⼀步：根据value()获取HashMap的“值”的集合。 第⼆步：通过Iterator迭代器遍历“第⼀步”得到的集合。 复制代码 代码如下:


/ 假设map是HashMap对象 / map中的key是String类型，value是Integer类型

Integer value = nul; Colection c = map.values(); Iterator iter= c.iterator(); while (iter.hasNext() {

value = (Integer)iter.next(); }

遍历测试程序如下： 复制代码 代码如下:

import java.util.Map; import java.util.Random; import java.util.Iterator; import java.util.HashMap; import java.util.HashSet; import java.util.Map.Entry; import java.util.Colection; /*

- * @desc 遍历HashMap的测试程序。
- * (01) 通过entrySet()去遍历key、value，参考实现函数：
- * iteratorHashMapByEntryset()
- * (02) 通过keySet()去遍历key、value，参考实现函数：
- * iteratorHashMapByKeyset()
- * (03) 通过values()去遍历value，参考实现函数：
- * iteratorHashMapJustValues()

*

- * @author skywang


- */ public clas HashMapIteratorTest {


public static void main(String[] args) { int val = 0; String key = nul; Integer value = nul; Random r = new Random(); HashMap map = new HashMap(); for (int i=0; i<12; i +) {

/ 随机获取⼀个[0,10)之间的数字 val = r.nextInt(10);

key = String.valueOf(val); value = r.nextInt(5);

/ 添加到HashMap中 map.put(key, value); System.out.println(" key:"+key+" value:"+value);

}

/ 通过entrySet()遍历HashMap的key-value iteratorHashMapByEntryset(map) ;

/ 通过keySet()遍历HashMap的key-value iteratorHashMapByKeyset(map) ;

/ 单单遍历HashMap的value

iteratorHashMapJustValues(map); }

/*

- * 通过entry set遍历HashMap
- * 效率⾼!
- */ private static void iteratorHashMapByEntryset(HashMap map) {


if (map = nul)

return ; System.out.println("\niterator HashMap By entryset"); String key = nul;

Integer integ = nul; Iterator iter = map.entrySet().iterator(); while(iter.hasNext() {

Map.Entry entry = (Map.Entry)iter.next();

key = (String)entry.getKey(); integ = (Integer)entry.getValue(); System.out.println(key+"- "+integ.intValue();

}

} /*

- * 通过keyset来遍历HashMap
- * 效率低!
- */ private static void iteratorHashMapByKeyset(HashMap map) {


if (map = nul)

return ; System.out.println("\niterator HashMap By keyset"); String key = nul; Integer integ = nul; Iterator iter = map.keySet().iterator(); while (iter.hasNext() {

key = (String)iter.next(); integ = (Integer)map.get(key); System.out.println(key+"- "+integ.intValue();

} }

/*

- * 遍历HashMap的values
- */ private static void iteratorHashMapJustValues(HashMap map) {


if (map = nul) return ;

Colection c = map.values(); Iterator iter= c.iterator();

while (iter.hasNext() {

System.out.println(iter.next(); }

} }

- 第4部分 HashMap示例 下⾯通过⼀个实例学习如何使⽤HashMap import java.util.Map; import java.util.Random; import java.util.Iterator; import java.util.HashMap; import java.util.HashSet; import java.util.Map.Entry; import java.util.Colection; /*


- * @desc HashMap测试程序

*

- * @author skywang
- */ public clas HashMapTest {


public static void main(String[] args) {

testHashMapAPIs(); }

private static void testHashMapAPIs() { / 初始化随机种⼦ Random r = new Random(); / 新建HashMap HashMap map = new HashMap(); / 添加操作

map.put("one", r.nextInt(10); map.put("two", r.nextInt(10); map.put("thre", r.nextInt(10);

/ 打印出map System.out.println("map:"+map );

/ 通过Iterator遍历key-value Iterator iter = map.entrySet().iterator(); while(iter.hasNext() {

Map.Entry entry = (Map.Entry)iter.next(); System.out.println("next : "+ entry.getKey() +" - "+entry.getValue();

} / HashMap的键值对个数 System.out.println("size:"+map.size(); / containsKey(Object key) :是否包含键key

System.out.println("contains key two : "+map.containsKey("two"); System.out.println("contains key five : "+map.containsKey("five");

/ containsValue(Object value) :是否包含值value System.out.println("contains value 0 : "+map.containsValue(new Integer(0);

/ remove(Object key) ： 删除键key对应的键值对 map.remove("thre"); System.out.println("map:"+map );

/ clear() ： 清空HashMap map.clear();

/ isEmpty() : HashMap是否为空

System.out.println(map.isEmpty()?"map is empty":"map is not empty") ); }

}

