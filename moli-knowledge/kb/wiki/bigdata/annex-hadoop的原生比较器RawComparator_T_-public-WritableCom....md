---
title: hadoop的原生比较器RawComparator_T_ public WritableCom....note（原文插图 annex）
slug: annex-hadoop的原生比较器RawComparator_T_-public-WritableCom...
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/hadoop的原生比较器RawComparator_T_ public WritableCom....note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

hadop为序列化提供了优化，类型的⽐较对M/R⽽⾔⾄关重要，Key和Key的⽐较也是在排序阶段 完成的，hadop提供了原⽣的⽐较器接⼝RawComparator<T>⽤于序列化字节间的⽐较，该接⼜允 许其实现直接⽐较数据流中的记录，⽆需反序列化为对象，RawComparator是⼀个原⽣的优化接⼜类，它只是简单的 提供了⽤于数据流中简单的数据对⽐⽅法，从⽽提供优化：

<table>
  <tr>
    <th>1</th>
    <th>public interface RawComparator<T> extends Com parator<T> {</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>public int compare(byte[]<br><br>b1, int s1, int l1, byte[]<br>b2, int s2, int l2);<br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>}</th>
  </tr>
</table>


该类并⾮被多数的衍⽣类所实现，其具体的⼦类为WritableComparator，多数情况下是作为 实现Writable接⼝的类的内置类，提供序列化字节的⽐较。下⾯是RawComparator接⼝内置类 的实现类图：

![image 1](assets/imageFile1.png)

⾸先，我们看 RawComparator的具体实现类WritableComparator：

![image 2](assets/imageFile2.png)

WritableComparator类类似于⼀个注册表，⾥⾯记录了所有Comparator类的集合。

Comparators成员⽤⼀张Hash表记录Key=Clas，value=WritableComprator的注册信息.

WritableComparator主要提供了两个功能

- 1. 提供了对原始compare()⽅法的⼀个默认实现


默认实现是 先反序列化为对像 再通过 对像⽐较（有开销的问题）

public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {

try {

- bufer.reset(b1, s1, l1); / parse key1

- key1.readFields(bufer);

bufer.reset(b2, s2, l2); / parse key2

- key2.readFields(bufer);




} catch (IOException e) {

throw new RuntimeException(e);

}

return compare(key1, key2); / compare them

}

⽽对应的基础数据类型的compare()的实现却巧妙的利⽤了特定类型的泛化：（利⽤了writableComparable的 compareTo⽅法）

public int compare(WritableComparable a, WritableComparable b) {

return a.compareTo(b);

}

例如IntWritable实例是调⽤了IntWritable⾥的compareTo⽅法

public int compareTo(Object o) {

int thisValue = this.value;

int thatValue =(IntWritable)o).value;

return (thisValue<thatValue ? -1 : (thisValue=thatValue ? 0 : 1);

}

- 2. 充当RawComparable实例的⼯⼚，以注册Writable的实现


例如,为了获取IntWritable的Comparator，可以直接调⽤其get⽅法。

WritableComparator：

关键代码：

- 代码1：registry注册器

-

/ registry注册器：记载了WritableComparator类的集合

privatestatic HashMap<Clas,WritableComparator>comparators =

new HashMap<Clas, WritableComparator>();

- 代码2：获取WritableComparator实例


说明：hashMap作为容器类线程不安全，故需要synchronized同步，get⽅法根据key=Class返回对应 的WritableComparator,若返回的是空值NUll，则调⽤protected Constructor进⾏构造，⽽其两个 protected的构造函数实则是调⽤了newKey()⽅法进⾏NewInstance

<table>
  <tr>
    <th>1</th>
    <th>public static synchronized WritableComparator get(Class<? extends WritableComparable> c) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>WritableComparator comparator = comparators.get(c);</th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>if (comparator == null)</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>comparator<br><br>= new WritableComparator(c, true);</th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>return comparator;</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>}</th>
  </tr>
</table>


- 代码3：构造⽅法


-

<table>
  <tr>
    <th>01</th>
    <th>new WritableComparator(c, true)</th>
  </tr>
</table>


<table>
  <tr>
    <th>02</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>WritableComparator的构造函数源码如下：</th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th>/*</th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>07</th>
    <th>* keyClass,key1,key2和buffer都是⽤于 WritableComparator的构造函数</th>
  </tr>
</table>


<table>
  <tr>
    <th>08</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>09</th>
    <th>*/</th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>11</th>
    <th>private final Class<? extends WritableCompa rable> keyClass;</th>
  </tr>
</table>


<table>
  <tr>
    <th>12</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>13</th>
    <th>private final WritableComparable key1; //WritableComparable接⼝</th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>15</th>
    <th>private finalWritableComparable key2;</th>
  </tr>
</table>


<table>
  <tr>
    <th>16</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>17</th>
    <th>private final DataInputBuffer buffer; //输⼊缓冲流</th>
  </tr>
</table>


<table>
  <tr>
    <th>18</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>19</th>
    <th>protected WritableComparator(Class<? extends WritableComparable> keyClass,</th>
  </tr>
</table>


<table>
  <tr>
    <th>20</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>21</th>
    <th>boolean createInstances) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>22</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>23</th>
    <th>this.keyClass = keyClass;</th>
  </tr>
</table>


<table>
  <tr>
    <th>24</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>25</th>
    <th>if (createInstances) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>26</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>27</th>
    <th>key1 = newKey();</th>
  </tr>
</table>


<table>
  <tr>
    <th>28</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>29</th>
    <th>key2 = newKey();</th>
  </tr>
</table>


<table>
  <tr>
    <th>30</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>31</th>
    <th>buffer = new DataInputBuffer();</th>
  </tr>
</table>


<table>
  <tr>
    <th>32</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>33</th>
    <th>} else {</th>
  </tr>
</table>


<table>
  <tr>
    <th>34</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>35</th>
    <th>key1 = key2 = null;</th>
  </tr>
</table>


<table>
  <tr>
    <th>36</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>37</th>
    <th>buffer = null;</th>
  </tr>
</table>


<table>
  <tr>
    <th>38</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>39</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>40</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>41</th>
    <th>}</th>
  </tr>
</table>


上述的keyClas，key1,key2,bufer是记录HashMap对应的key值，⽤于WritableComparator的构造函数，但由其构造 函数中我们可以看出WritableComparator根据Bolean createInstance来判断是否实例化key1,key2和bufer,⽽ key1,key2作为实现了WritableComparable接⼜的标识，在WritableComparator的构造函数⾥⾯通过newKey()的 ⽅法去实例化实现WritableComparable接⼜的⼀个对象，下⾯是newKey（）的源码，通过hadop⾃⾝的反射去实 例化了⼀个WritableComparable接⼜对象。

<table>
  <tr>
    <th>1</th>
    <th><b> public WritableComparable newKey() {</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>return ReflectionUtils.newInstance(keyCla ss, null);</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>7</th>
    <th></b></th>
  </tr>
</table>


- 代码4：Compare（）⽅法


-

- 1. publicint compare(Object a, Object b)；

- 2. publicint compare(WritableComparable a, WritableComparable b)；

- 3. publicintcompare(byte[] b1,int s1,int l1,byte[] b2,int s2,int l2)；


三个compare（）重载⽅法中，compare(Object a, Object b)利⽤⼦类塑形为WritableComparable⽽调⽤了第 2个compare⽅法，⽽第2个Compare（）⽅法则调⽤了Writable.compaerTo();最后⼀个compare(byte[] b1,int s1,int l1,byte[] b2,int s2,int l2)⽅法源码如下：

<table>
  <tr>
    <th>01</th>
    <th>public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>02</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>try {</th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th>buffer.reset(b1, s1, l1); // parse key1</th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>07</th>
    <th>key1.readFields(buffer);</th>
  </tr>
</table>


<table>
  <tr>
    <th>08</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>09</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>11</th>
    <th>buffer.reset(b2, s2, l2); // parse key2</th>
  </tr>
</table>


<table>
  <tr>
    <th>12</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>13</th>
    <th>key2.readFields(buffer);</th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>15</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>16</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>17</th>
    <th>} catch (IOException e) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>18</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>19</th>
    <th>throw new RuntimeException(e);</th>
  </tr>
</table>


<table>
  <tr>
    <th>20</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>21</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>22</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>23</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>24</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>25</th>
    <th>return compare(key1,<br><br>key2); // compare them</th>
  </tr>
</table>


<table>
  <tr>
    <th>26</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>27</th>
    <th>}</th>
  </tr>
</table>


Compare⽅法的⼀个缺省实现⽅式，根据接⼜key1,ke2反序列化为对象再进⾏⽐较。

利⽤Bufer为桥接中介，把字节数组存储为bufer后，调⽤key1（WritableComparable）的反序列化⽅法，再来⽐较 key1,ke2，由此处可以看出，该compare⽅法是将要⽐较的⼆进制流反序列化为对象，再调⽤⽅法第2个重载⽅法进⾏⽐ 较。

- 代码5：⽅法deﬁne⽅法


该⽅法⽤于注册WritebaleComparaor对象到注册表中，注意同时该⽅法也需要同步，代码如下：

<table>
  <tr>
    <th>1</th>
    <th>public static synchronized void define(Class c,</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>Wri tableComparator comparator) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>comparators.put(c, comparator);</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>}</th>
  </tr>
</table>


代码5：余下诸如readInt的静态⽅法

-

这些⽅法⽤于实现WritableComparable的各种实例，例如 IntWritable实例：内部类Comparator类需要根据⾃⼰的 IntWritable类型重载WritableComparator⾥⾯的compare（）⽅法，可以说WritableComparator⾥⾯的compare（）⽅ 法只是提供了⼀个缺省的实现，⽽真正的compare（）⽅法实现需要根据⾃⼰的类型如IntWritable进⾏重载，所以 WritableComparator⽅法中的那些readInt.等⽅法只是底层的封装的⼀个实现，⽅便内部Comparator进⾏调⽤⽽已。

下⾯我们着重看下BooleanWritable类的内置RawCompartor<T>的实现过程:

<table>
  <tr>
    <th>01</th>
    <th>/**</th>
  </tr>
</table>


<table>
  <tr>
    <th>02</th>
    <th>* A Comparator optimized for BooleanWritable.</th>
  </tr>
</table>


<table>
  <tr>
    <th>03</th>
    <th>*/</th>
  </tr>
</table>


<table>
  <tr>
    <th>04</th>
    <th>public static class Comparator extends Writa bleComparator {</th>
  </tr>
</table>


<table>
  <tr>
    <th>05</th>
    <th>public Comparator() {//调⽤⽗类的<br><br>Constructor初始化keyClass=BooleanWrite.class</th>
  </tr>
</table>


<table>
  <tr>
    <th>06</th>
    <th>super(BooleanWritable.class);</th>
  </tr>
</table>


<table>
  <tr>
    <th>07</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>08</th>
    <th>//重写⽗类的序列化⽐较⽅法，⽤些类⽤到⽗类提供的 缺省⽅法</th>
  </tr>
</table>


<table>
  <tr>
    <th>09</th>
    <th>public int compare(byte[] b1, int s1, int l1,</th>
  </tr>
</table>


<table>
  <tr>
    <th>10</th>
    <th>byte[] b2, int s2, int l2) {</th>
  </tr>
</table>


<table>
  <tr>
    <th>11</th>
    <th>boolean a = (readInt(b1, s1) == 1) ? true : false;</th>
  </tr>
</table>


<table>
  <tr>
    <th>12</th>
    <th>boolean b = (readInt(b2, s2) == 1) ? true : false;</th>
  </tr>
</table>


<table>
  <tr>
    <th>13</th>
    <th>return ((a == b) ? 0 : (a == false) ?<br><br>-1 : 1);</th>
  </tr>
</table>


<table>
  <tr>
    <th>14</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>15</th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>16</th>
    <th>//注册</th>
  </tr>
</table>


<table>
  <tr>
    <th>17</th>
    <th>static {</th>
  </tr>
</table>


<table>
  <tr>
    <th>18</th>
    <th>WritableComparator.define(BooleanWritabl e.class, new Comparator());</th>
  </tr>
</table>


<table>
  <tr>
    <th>19</th>
    <th>}</th>
  </tr>
</table>


总结：

hadop 类似于Java的类包，即提供了Comparable接⼝（对应于writableComparable接⼝） 和Comparator类（对应于RawComparator类）⽤于实现序列化的⽐较，在hadop 的IO包中已经 封装了JAVA的基本数据类型⽤于序列化和反序列化，⼀般⾃⼰写的类实现序列化和反序列化需要 继承WritableComparable接⼝并且内置⼀个Comparator（继承于WritableComparator）的格式来 实现⾃⼰的对象。
