⽬录：

⼀、 ArrayList概述 ⼆、 ArrayList的实现

- 1) 私有属性

- 2) 构造⽅法

- 3) 元素存储

- 4) 元素读取

- 5) 元素删除

- 6) 调整数组容量

- 7)转为静态数组toArray


总结

⼀、 ArrayList概述： ArrayList是基于数组实现的，是⼀个动态数组，其容量能⾃动增⻓，类似于C语⾔中的动态申请内存， 动态增⻓内存。

ArrayList不是线程安全的，只能⽤在单线程环境下，多线程环境下可以考虑⽤ Collections.synchronizedList(List l)函数返回⼀个线程安全的ArrayList类，也可以使⽤concurrent 并发包下的CopyOnWriteArrayList类。

ArrayList实现了Serializable接⼝，因此它⽀持序列化，能够通过序列化传输，实现了 RandomAccess接⼝，⽀持快速随机访问，实际上就是通过下标序号进⾏快速访问，实现了Cloneable 接⼝，能被克隆。

每个ArrayList实例都有⼀个容量，该容量是指⽤来存储列表元素的数组的⼤⼩。它总是⾄少等于列表 的⼤⼩。随着向ArrayList中不断添加元素，其容量也⾃动增⻓。⾃动增⻓会带来数据向新数组的重新拷 ⻉，因此，如果可预知数据量的多少，可在构造ArrayList时指定其容量。在添加⼤量元素前，应⽤程序 也可以使⽤ensureCapacity操作来增加ArrayList实例的容量，这可以减少递增式再分配的数量。

注意，此实现不是同步的。如果多个线程同时访问⼀个ArrayList实例，⽽其中⾄少⼀个线程从结构上 修改了列表，那么它必须保持外部同步。

⼆、 ArrayList的实现：

对于ArrayList⽽⾔，它实现List接⼝、底层使⽤数组保存所有元素。其操作基本上是对数组的操作。 下⾯我们来分析ArrayList的源代码：

- 1) 私有属性： ArrayList定义只定义类两个私有属性：


![image 1](<ArrayList的实现原理.note_images/imageFile1.png>)

/**

- * The array buffer into which the elements of the ArrayList are stored.

- * The capacity of the ArrayList is the length of this array buffer.

- */ private transient Object[] elementData;


/**

- * The size of the ArrayList (the number of elements it contains).

*

- * @serial

- */ private int size;


![image 2](<ArrayList的实现原理.note_images/imageFile2.png>)

很容易理解，elementData存储ArrayList内的元素，size表示它包含的元素的数量。 有个关键字需要解释：transient。

Java的serialization提供了⼀种持久化对象实例的机制。当持久化对象时，可能有⼀个特殊的对象数 据成员，我们不想⽤serialization机制来保存它。为了在⼀个特定对象的⼀个域上关闭serialization， 可以在这个域前加上关键字transient。 有点抽象，看个例⼦应该能明⽩。

![image 3](<ArrayList的实现原理.note_images/imageFile3.png>)

public class UserInfo implements Serializable { private static final long serialVersionUID = 996890129747019948L; private String name; private transient String psw;

public UserInfo(String name, String psw) { this.name = name; this.psw = psw;

}

public String toString() {

return "name=" + name + ", psw=" + psw; }

}

public class TestTransient {

public static void main(String[] args) { UserInfo userInfo = new UserInfo("张三", "123456"); System.out.println(userInfo); try {

// 序列化，被设置为transient的属性没有被序列化

ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(

"UserInfo.out"));

- o.writeObject(userInfo);

- o.close();


} catch (Exception e) { // TODO: handle exception e.printStackTrace();

} try {

// 重新读取内容

ObjectInputStream in = new ObjectInputStream(new FileInputStream(

"UserInfo.out")); UserInfo readUserInfo = (UserInfo) in.readObject(); //读取后psw的内容为null

System.out.println(readUserInfo.toString());

} catch (Exception e) { // TODO: handle exception e.printStackTrace();

} }

}

![image 4](<ArrayList的实现原理.note_images/imageFile4.png>)

被标记为transient的属性在对象被序列化的时候不会被保存。

接着回到ArrayList的分析中......

- 2) 构造⽅法： ArrayList提供了三种⽅式的构造器，可以构造⼀个默认初始容量为10的空列表、构造⼀个指定初始

容量的空列表以及构造⼀个包含指定collection的元素的列表，这些元素按照该collection的迭代器返回 它们的顺序排列的。

// ArrayList带容量⼤⼩的构造函数。

public ArrayList(int initialCapacity) { super(); if (initialCapacity < 0)

throw new IllegalArgumentException("Illegal Capacity: "+

initialCapacity); // 新建⼀个数组

this.elementData = new Object[initialCapacity]; }

// ArrayList⽆参构造函数。默认容量是10。 public ArrayList() {

this(10); }

// 创建⼀个包含collection的ArrayList

public ArrayList(Collection<? extends E> c) {

elementData = c.toArray(); size = elementData.length; if (elementData.getClass() != Object[].class)

elementData = Arrays.copyOf(elementData, size, Object[].class); }

- 3) 元素存储： ArrayList提供了set(int index, E element)、add(E e)、add(int index, E element)、 addAll(Collection<? extends E> c)、addAll(int index, Collection<? extends E> c)这些添加元 素的⽅法。下⾯我们⼀⼀讲解：


![image 5](<ArrayList的实现原理.note_images/imageFile5.png>)

![image 6](<ArrayList的实现原理.note_images/imageFile6.png>)

![image 7](<ArrayList的实现原理.note_images/imageFile7.png>)

- 20 // ⽤指定的元素替代此列表中指定位置上的元素，并返回以前位于该位置上的元素。

- 21 public E set(int index, E element) {

- 22 RangeCheck(index);

- 23

- 24 E oldValue = (E) elementData[index];

- 25 elementData[index] = element;

- 26 return oldValue;

- 27 }

- 28 // 将指定的元素添加到此列表的尾部。

- 29 public boolean add(E e) {

- 30 ensureCapacity(size + 1);

- 31 elementData[size++] = e;

- 32 return true;

- 33 }

- 34 // 将指定的元素插⼊此列表中的指定位置。

- 35 // 如果当前位置有元素，则向右移动当前位于该位置的元素以及所有后续元素（将其索引加1）。

- 36 public void add(int index, E element) {

- 37 if (index > size || index < 0)

- 38 throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);

- 39 // 如果数组⻓度不⾜，将进⾏扩容。

- 40 ensureCapacity(size+1); // Increments modCount!!

- 41 // 将 elementData中从Index位置开始、⻓度为size-index的元素，

- 42 // 拷⻉到从下标为index+1位置开始的新的elementData数组中。

- 43 // 即将当前位于该位置的元素以及所有后续元素右移⼀个位置。

- 44 System.arraycopy(elementData, index, elementData, index + 1, size index);

- 45 elementData[index] = element;

- 46 size++;

- 47 }

- 48 // 按照指定collection的迭代器所返回的元素顺序，将该collection中的所有元素添加到此列表的尾部。

- 49 public boolean addAll(Collection<? extends E> c) {

- 50 Object[] a = c.toArray();

- 51 int numNew = a.length;

- 52 ensureCapacity(size + numNew); // Increments modCount

- 53 System.arraycopy(a, 0, elementData, size, numNew);

- 54 size += numNew;

- 55 return numNew != 0;

- 56 }

- 57 // 从指定的位置开始，将指定collection中的所有元素插⼊到此列表中。


- 58 public boolean addAll(int index, Collection<? extends E> c) {

- 59 if (index > size || index < 0)

- 60 throw new IndexOutOfBoundsException(

- 61 "Index: " + index + ", Size: " + size);

- 62

- 63 Object[] a = c.toArray();

- 64 int numNew = a.length;

- 65 ensureCapacity(size + numNew); // Increments modCount

- 66

- 67 int numMoved = size - index;

- 68 if (numMoved > 0)

- 69 System.arraycopy(elementData, index, elementData, index + numNew, numMoved);

- 70

- 71 System.arraycopy(a, 0, elementData, index, numNew);

- 72 size += numNew;

- 73 return numNew != 0; }


![image 8](<ArrayList的实现原理.note_images/imageFile8.png>)

书上都说ArrayList是基于数组实现的，属性中也看到了数组，具体是怎么实现的呢？⽐如就这个添加元 素的⽅法，如果数组⼤，则在将某个位置的值设置为指定元素即可，如果数组容量不够了呢？

看到add(E e)中先调⽤了ensureCapacity(size+1)⽅法，之后将元素的索引赋给 elementData[size]，⽽后size⾃增。例如初次添加时，size为0，add将elementData[0]赋值为e， 然后size设置为1（类似执⾏以下两条语句elementData[0]=e;size=1）。将元素的索引赋给 elementData[size]不是会出现数组越界的情况吗？这⾥关键就在ensureCapacity(size+1)中了。

- 4) 元素读取：

// 返回此列表中指定位置上的元素。 public E get(int index) {

RangeCheck(index);

return (E) elementData[index]; }

- 5) 元素删除：


ArrayList提供了根据下标或者指定对象两种⽅式的删除功能。如下： romove(int index):

![image 9](<ArrayList的实现原理.note_images/imageFile9.png>)

- 1 // 移除此列表中指定位置上的元素。

- 2 public E remove(int index) {

- 3 RangeCheck(index);

- 4

- 5 modCount++;

- 6 E oldValue = (E) elementData[index];

- 7

- 8 int numMoved = size - index - 1;

- 9 if (numMoved > 0)

- 10 System.arraycopy(elementData, index+1, elementData, index, numMoved);

- 11 elementData[--size] = null; // Let gc do its work

- 12

- 13 return oldValue;

- 14 }


![image 10](<ArrayList的实现原理.note_images/imageFile10.png>)

⾸先是检查范围，修改modCount，保留将要被移除的元素，将移除位置之后的元素向前挪动⼀个位 置，将list末尾元素置空（null），返回被移除的元素。 remove(Object o)

![image 11](<ArrayList的实现原理.note_images/imageFile11.png>)

- 1 // 移除此列表中⾸次出现的指定元素（如果存在）。这是应为ArrayList中允许存放重复的元素。

- 2 public boolean remove(Object o) {

- 3 // 由于ArrayList中允许存放null，因此下⾯通过两种情况来分别处理。

- 4 if (o == null) {

- 5 for (int index = 0; index < size; index++)

- 6 if (elementData[index] == null) {

- 7 // 类似remove(int index)，移除列表中指定位置上的元素。

- 8 fastRemove(index);

- 9 return true;

- 10 }

- 11 } else {

- 12 for (int index = 0; index < size; index++)

- 13 if (o.equals(elementData[index])) {

- 14 fastRemove(index);

- 15 return true;

- 16 }

- 17 }

- 18 return false;

- 19 }

- 20 }


![image 12](<ArrayList的实现原理.note_images/imageFile12.png>)

⾸先通过代码可以看到，当移除成功后返回true，否则返回false。remove(Object o)中通过遍历 element寻找是否存在传⼊对象，⼀旦找到就调⽤fastRemove移除对象。为什么找到了元素就知道了 index，不通过remove(index)来移除元素呢？因为fastRemove跳过了判断边界的处理，因为找到元 素就相当于确定了index不会超过边界，⽽且fastRemove并不返回被移除的元素。下⾯是fastRemove 的代码，基本和remove(index)⼀致。

![image 13](<ArrayList的实现原理.note_images/imageFile13.png>)

- 1 private void fastRemove(int index) {

- 2 modCount++;

- 3 int numMoved = size - index - 1;

- 4 if (numMoved > 0)

- 5 System.arraycopy(elementData, index+1, elementData, index,

- 6 numMoved);

- 7 elementData[--size] = null; // Let gc do its work

- 8 }


![image 14](<ArrayList的实现原理.note_images/imageFile14.png>)

removeRange(int fromIndex,int toIndex)

![image 15](<ArrayList的实现原理.note_images/imageFile15.png>)

- 1 protected void removeRange(int fromIndex, int toIndex) {

- 2 modCount++;

- 3 int numMoved = size - toIndex;

- 4 System.arraycopy(elementData, toIndex, elementData, fromIndex,

- 5 numMoved);

- 6

- 7 // Let gc do its work

- 8 int newSize = size - (toIndex-fromIndex);

- 9 while (size != newSize)

- 10 elementData[--size] = null;

- 11 }


![image 16](<ArrayList的实现原理.note_images/imageFile16.png>)

执⾏过程是将elementData从toIndex位置开始的元素向前移动到fromIndex，然后将toIndex位置之 后的元素全部置空顺便修改size。

这个⽅法是protected，及受保护的⽅法，为什么这个⽅法被定义为protected呢？ 这是⼀个解释，但是可能不容易看明⽩。http://stackoverflow.com/questions/2289183/why-

is-javas-abstractlists-removerange-method-protected 先看下⾯这个例⼦

ArrayList<Integer> ints = new ArrayList<Integer>(Arrays.asList(0, 1, 2,

3, 4, 5, 6)); // fromIndex low endpoint (inclusive) of the subList // toIndex high endpoint (exclusive) of the subList

ints.subList(2, 4).clear(); System.out.println(ints);

输出结果是[0, 1, 4, 5, 6]，结果是不是像调⽤了removeRange(int fromIndex,int toIndex)！哈哈 哈，就是这样的。但是为什么效果相同呢？是不是调⽤了removeRange(int fromIndex,int toIndex) 呢？

- 6) 调整数组容量ensureCapacity： 从上⾯介绍的向ArrayList中存储元素的代码中，我们看到，每当向数组中添加元素时，都要去检查添


加后元素的个数是否会超出当前数组的⻓度，如果超出，数组将会进⾏扩容，以满⾜添加数据的需求。 数组扩容通过⼀个公开的⽅法ensureCapacity(int minCapacity)来实现。在实际添加⼤量元素前，我 也可以使⽤ensureCapacity来⼿动增加ArrayList实例的容量，以减少递增式再分配的数量。

public void ensureCapacity(int minCapacity) { modCount++; int oldCapacity = elementData.length; if (minCapacity > oldCapacity) {

Object oldData[] = elementData; int newCapacity = (oldCapacity * 3)/2 + 1; //增加50%+1

if (newCapacity < minCapacity) newCapacity = minCapacity;

// minCapacity is usually close to size, so this is a win:

elementData = Arrays.copyOf(elementData, newCapacity); }

}

![image 18](<ArrayList的实现原理.note_images/imageFile18.png>)

从上述代码中可以看出，数组进⾏扩容时，会将⽼数组中的元素重新拷⻉⼀份到新的数组中，每次数组 容量的增⻓⼤约是其原容量的1.5倍。这种操作的代价是很⾼的，因此在实际使⽤时，我们应该尽量避免 数组容量的扩张。当我们可预知要保存的元素的多少时，要在构造ArrayList实例时，就指定其容量，以 避免数组扩容的发⽣。或者根据实际需求，通过调⽤ensureCapacity⽅法来⼿动增加ArrayList实例的 容量。 Object oldData[] = elementData;//为什么要⽤到oldData[] 乍⼀看来后⾯并没有⽤到关于oldData， 这句话显得多此⼀举！但是这是⼀个牵涉到内存管理的类， 所 以要了解内部的问题。 ⽽且为什么这⼀句还在if的内部，这跟 elementData = Arrays.copyOf(elementData, newCapacity); 这句是有关系的，下⾯这句 Arrays.copyOf的实现时新创建了newCapacity⼤⼩的内存，然后把⽼的elementData放⼊。好像也 没有⽤到oldData，有什么问题呢。问题就在于旧的内存的引⽤是elementData， elementData指向 了新的内存块，如果有⼀个局部变量oldData变量引⽤旧的内存块的话，在copy的过程中就会⽐较安 全，因为这样证明这块⽼的内存依然有引⽤，分配内存的时候就不会被侵占掉，然后copy完成后这个局 部变量的⽣命期也过去了，然后释放才是安全的。不然在copy的的时候万⼀新的内存或其他线程的分配 内存侵占了这块⽼的内存，⽽copy还没有结束，这将是个严重的事情。

关于ArrayList和Vector区别如下：

ArrayList在内存不够时默认是扩展50% + 1个，Vector是默认扩展1倍。 Vector提供indexOf(obj, start)接⼝，ArrayList没有。 Vector属于线程安全级别的，但是⼤多数情况下不使⽤Vector，因为线程安全需要更⼤的系统开销。

ArrayList还给我们提供了将底层数组的容量调整为当前列表保存的实际元素的⼤⼩的功能。它可以通 过trimToSize⽅法来实现。代码如下：

- 127 public void trimToSize() {

- 128 modCount++;

- 129 int oldCapacity = elementData.length;

- 130 if (size < oldCapacity) {

- 131 elementData = Arrays.copyOf(elementData, size);

- 132 } }


![image 20](<ArrayList的实现原理.note_images/imageFile20.png>)

由于elementData的⻓度会被拓展，size标记的是其中包含的元素的个数。所以会出现size很⼩但 elementData.length很⼤的情况，将出现空间的浪费。trimToSize将返回⼀个新的数组给 elementData，元素内容保持不变，length和size相同，节省空间。

- 7)转为静态数组toArray 4、注意ArrayList的两个转化为静态数组的toArray⽅法。


第⼀个， 调⽤Arrays.copyOf将返回⼀个数组，数组内容是size个elementData的元素，即拷⻉ elementData从0⾄size-1位置的元素到新数组并返回。

public Object[] toArray() {

return Arrays.copyOf(elementData, size); }

第⼆个，如果传⼊数组的⻓度⼩于size，返回⼀个新的数组，⼤⼩为size，类型与传⼊数组相同。所 传⼊数组⻓度与size相等，则将elementData复制到传⼊数组中并返回传⼊的数组。若传⼊数组⻓度⼤ 于size，除了复制elementData外，还将把返回数组的第size个元素置为空。

![image 21](<ArrayList的实现原理.note_images/imageFile21.png>)

public <T> T[] toArray(T[] a) { if (a.length < size)

// Make a new array of a's runtime type, but my contents:

return (T[]) Arrays.copyOf(elementData, size, a.getClass()); System.arraycopy(elementData, 0, a, 0, size);

if (a.length > size) a[size] = null; return a;

}

![image 22](<ArrayList的实现原理.note_images/imageFile22.png>)

Fail-Fast机制： ArrayList也采⽤了快速失败的机制，通过记录modCount参数来实现。在⾯对并发的修改时，迭代器很 快就会完全失败，⽽不是冒着在将来某个不确定时间发⽣任意不确定⾏为的⻛险。具体介绍请参考这篇 ⽂章深⼊Java集合学习系列：HashMap的实现原理 中的Fail-Fast机制。

总结: 关于ArrayList的源码，给出⼏点⽐较重要的总结：

- 1、注意其三个不同的构造⽅法。⽆参构造⽅法构造的ArrayList的容量默认为10，带有Collection参

数的构造⽅法，将Collection转化为数组赋给ArrayList的实现数组elementData。

- 2、注意扩充容量的⽅法ensureCapacity。ArrayList在每次增加元素（可能是1个，也可能是⼀

组）时，都要调⽤该⽅法来确保⾜够的容量。当容量不⾜以容纳当前的元素个数时，就设置新的容量为 旧的容量的1.5倍加1，如果设置后的新容量还不够，则直接新容量设置为传⼊的参数（也就是所需的容 量），⽽后⽤Arrays.copyof()⽅法将元素拷⻉到新的数组（详⻅下⾯的第3点）。从中可以看出，当容 量不够时，每次增加元素，都要将原来的元素拷⻉到⼀个新的数组中，⾮常之耗时，也因此建议在事先 能确定元素数量的情况下，才使⽤ArrayList，否则建议使⽤LinkedList。

- 3、ArrayList的实现中⼤量地调⽤了Arrays.copyof()和System.arraycopy()⽅法。我们有必要对


这两个⽅法的实现做下深⼊的了解。

⾸先来看Arrays.copyof()⽅法。它有很多个重载的⽅法，但实现思路都是⼀样的，我们来看泛型版 本的源码： public static <T> T[] copyOf(T[] original, int newLength) {

return (T[]) copyOf(original, newLength, original.getClass()); }

很明显调⽤了另⼀个copyof⽅法，该⽅法有三个参数，最后⼀个参数指明要转换的数据的类型，其源码 如下：

![image 23](<ArrayList的实现原理.note_images/imageFile23.png>)

public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {

T[] copy = ((Object)newType == (Object)Object[].class) ? (T[]) new Object[newLength] : (T[]) Array.newInstance(newType.getComponentType(), newLength);

System.arraycopy(original, 0, copy, 0,

Math.min(original.length, newLength)); return copy;

}

![image 24](<ArrayList的实现原理.note_images/imageFile24.png>)

这⾥可以很明显地看出，该⽅法实际上是在其内部⼜创建了⼀个⻓度为newlength的数组，调⽤ System.arraycopy()⽅法，将原来数组中的元素复制到了新的数组中。

下⾯来看System.arraycopy()⽅法。该⽅法被标记了native，调⽤了系统的C/C++代码，在JDK 中是看不到的，但在openJDK中可以看到其源码。该函数实际上最终调⽤了C语⾔的memmove()函 数，因此它可以保证同⼀个数组内元素的正确复制和移动，⽐⼀般的复制⽅法的实现效率要⾼很多，很 适合⽤来批量处理数组。Java强烈推荐在复制⼤量数组元素时⽤该⽅法，以取得更⾼的效率。

- 4、ArrayList基于数组实现，可以通过下标索引直接查找到指定位置的元素，因此查找效率⾼，但每次 插⼊或删除元素，就要⼤量地移动元素，插⼊删除元素的效率低。

- 5、在查找给定元素索引值等的⽅法中，源码都将该元素的值分为null和不为null两种情况处理， ArrayList中允许元素为null。


注：参考

# java源码分析之ArrayList

