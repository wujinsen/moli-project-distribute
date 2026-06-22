⼀、 Bl om-Filter算法简介。

Bl om-Filter，即布隆过滤器，1970年由Bl om中提出。它可以⽤于检索⼀个元素是否在⼀个集合 中，其优点是空间效率和查询时间都远远超过其他算法，其不⾜在于Bl om- Filter存在着误判。 ⼆、 Bl om-Filter的基本思想。

Bl om-Filter算法的核⼼思想就是利⽤多个不同的Hash函数来解决“冲突”。 计算某元素x是否在⼀ 个集合中，⾸先能想到的⽅法就是将所有的已知元素保存起来构成⼀个集合R，然后⽤元素x跟这些R中 的元素⼀⼀⽐较来判断是否存在于集合R中；我们可以采⽤链表等数据结构来实现。但是，随着集合R 中元素的增加，其占⽤的内存将越来越⼤。试想，如果有⼏千万个不同⽹⻚需要下载，所需的内存将 ⾜以占⽤掉整个进程的内存地址空间。即使⽤MD5， UID这些⽅法将URL转成固定的短⼩的字符串， 内存占⽤也是相当巨⼤的.

⽇常⽣活中，包括在设计计算机软件时，我们经常要判断⼀个元素是否在⼀个集合中。⽐如在字处 理软件中，需要检查⼀个英语单词是否拼写正确（也就是要判断它是否在已知的字典中）；在 FBI，⼀ 个嫌疑⼈的名字是否已经在嫌疑名单上；在⽹络爬⾍⾥，⼀个⽹址是否被访问过等等。最直接的⽅法 就是将集合中全部的元素存在计算机中，遇到⼀个新元素时，将它和集合中的元素直接⽐较即可。

⼀般来讲，计算机中的集合是⽤哈希表（hash table）来存储的。它的好处是快速准确，缺点是费 存储空间。当集合⽐较⼩时，这个问题不显著，但是当集合巨⼤时，哈希表存储效率低的问题就显现 出来了。 三、 Bl om-Filter的应⽤。

Bl om-Filter⼀般⽤于在⼤数据量的集合中判定某元素是否存在。例如邮件服务器中的垃圾邮件过 滤器。在搜索引擎领域，Bl om-Filter最常⽤于⽹络蜘蛛(Spider)的URL过滤，⽹络蜘蛛通常有⼀个 URL列表，保存着将要下载和已经下载的⽹⻚的URL，⽹络蜘蛛下载了⼀个⽹⻚，从⽹⻚中提取到新的 URL后，需要判断该URL是否已经存在于列表中。此时，Bl om-Filter算法是最好的选择。

⽐如说，⼀个象 Yaho,Hotmail 和 Gmai 那样的公众电⼦邮件（email）提供商，总是需要过滤来 ⾃发送垃圾邮件的⼈（spamer）的垃圾邮件。⼀个办法就是记录下那些发垃圾邮件的 email 地址。由 于那些发送者不停地在注册新的地址，全世界少说也有⼏⼗亿个发垃圾邮件的地址，将他们都存起来 则需要⼤量的⽹络服务器。

布隆过滤器是由巴顿.布隆于⼀九七零年提出的。它实际上是⼀个很⻓的⼆进制向量和⼀系列随机映 射函数。我们通过上⾯的例⼦来说明起⼯作原理。

假定我们存储⼀亿个电⼦邮件地址，我们先建⽴⼀个⼗六亿⼆进制（⽐特），即两亿字节的向量， 然后将这⼗六亿个⼆进制位全部设置为零。对于每⼀个电⼦邮件地址 X，我们⽤⼋个不同的随机数产 ⽣器（F1,F2, .,F8） 产⽣⼋个信息指纹（f1, f2, ., f8）。再⽤⼀个随机数产⽣器 G 把这⼋个信息指纹 映射到 1 到⼗六亿中的⼋个⾃然数 g1, g2, .,g8。现在我们把这⼋个位置的⼆进制位全部设置为⼀。当 我们对这⼀亿个 email 地址都进⾏这样的处理后。⼀个针对这些 email 地址的布隆过滤器就建成了。 （⻅下图） 现在，让我们看看如何⽤布隆过滤器来检测⼀个可疑的电⼦邮件地址 Y 是否在⿊名单 中。我们⽤相同的⼋个随机数产⽣器（F1, F2, ., F8）对这个地址产⽣⼋个信息指纹 s1,s2,.,s8，然后 将这⼋个指纹对应到布隆过滤器的⼋个⼆进制位，分别是 t1,t2,.,t8。如果 Y 在⿊名单中，显然， t1,t2,.,t8 对应的⼋个⼆进制⼀定是⼀。这样在遇到任何在⿊名单中的电⼦邮件地址，我们都能准确地 发现。

布隆过滤器决不会漏掉任何⼀个在⿊名单中的可疑地址。但是，它有⼀条不⾜之处。也就是它有极 ⼩的可能将⼀个不在⿊名单中的电⼦邮件地址判定为在⿊名单中，因为有可能某个好的邮件地址正巧 对应⼋个都被设置成⼀的⼆进制位。好在这种可能性很⼩。我们把它称为误识概率。在上⾯的例⼦ 中，误识概率在万分之⼀以下。

布隆过滤器的好处在于快速，省空间。但是有⼀定的误识别率。常⻅的补救办法是在建⽴⼀个⼩的 ⽩名单，存储那些可能别误判的邮件地址。

1. 使⽤Java ⾃带的 private BitSet bits = new BitSet(defaultSize);

- 1 import java.util.BitSet;

- 2

- 3 public class bloomFilter {

- 4

- 5 private int defaultSize = 5000 << 10000;

- 6 private int basic = defaultSize -1;

- 7 private String key = null;

- 8 private BitSet bits = new BitSet(defaultSize);

- 9

- 10 public bloomFilter(String key){

- 11 this.key = key;

- 12 }

- 13

- 14 private int[] lrandom(){

- 15 int[] randomsum = new int[8];

- 16 int random1 = hashCode(key,1);

- 17 int random2 = hashCode(key,2);

- 18 int random3 = hashCode(key,3);

- 19 int random4 = hashCode(key,4);

- 20 int random5 = hashCode(key,5);

- 21 int random6 = hashCode(key,6);

- 22 int random7 = hashCode(key,7);

- 23 int random8 = hashCode(key,8);

- 24 randomsum[0] = random1;

- 25 randomsum[1] = random2;

- 26 randomsum[2] = random3;

- 27 randomsum[3] = random4;

- 28 randomsum[4] = random5;

- 29 randomsum[5] = random6;

- 30 randomsum[6] = random7;

- 31 randomsum[7] = random8;

- 32 return randomsum;

- 33 }

- 34

- 35 private int[] sameLrandom(){

- 36 int[] randomsum = new int[8];

- 37 int random1 = hashCode(key,1);

- 38 int random2 = hashCode(key,1);

- 39 int random3 = hashCode(key,1);


- 40 int random4 = hashCode(key,1);

- 41 int random5 = hashCode(key,1);

- 42 int random6 = hashCode(key,1);

- 43 int random7 = hashCode(key,1);

- 44 int random8 = hashCode(key,1);

- 45 randomsum[0] = random1;

- 46 randomsum[1] = random2;

- 47 randomsum[2] = random3;

- 48 randomsum[3] = random4;

- 49 randomsum[4] = random5;

- 50 randomsum[5] = random6;

- 51 randomsum[6] = random7;

- 52 randomsum[7] = random8;

- 53 return randomsum;

- 54 }

- 55

- 56 private void add(){

- 57 if(exist()){

- 58 System.out.println("已经包含("+key+")");

- 59 return;

- 60 }

- 61 int keyCode[] = lrandom();

- 62 bits.set(keyCode[0]);

- 63 bits.set(keyCode[1]);

- 64 bits.set(keyCode[2]);

- 65 bits.set(keyCode[3]);

- 66 bits.set(keyCode[4]);

- 67 bits.set(keyCode[5]);

- 68 bits.set(keyCode[6]);

- 69 bits.set(keyCode[7]);

- 70 }

- 71

- 72 private boolean exist(){

- 73 int keyCode[] = lrandom();

- 74 if(bits.get(keyCode[0])&&

- 75 bits.get(keyCode[1])

- 76 &&bits.get(keyCode[2])

- 77 &&bits.get(keyCode[3])

- 78 &&bits.get(keyCode[4])

- 79 &&bits.get(keyCode[5])


- 80 &&bits.get(keyCode[6])

- 81 &&bits.get(keyCode[7])){

- 82 return true;

- 83 }

- 84 return false;

- 85 }

- 86

- 87 private boolean set0(){

- 88 if(exist()){

- 89 int keyCode[] = lrandom();

- 90 bits.clear(keyCode[0]);

- 91 bits.clear(keyCode[1]);

- 92 bits.clear(keyCode[2]);

- 93 bits.clear(keyCode[3]);

- 94 bits.clear(keyCode[4]);

- 95 bits.clear(keyCode[5]);

- 96 bits.clear(keyCode[6]);

- 97 bits.clear(keyCode[7]);

- 98 return true;

- 99 }

- 100 return false;

- 101 }

- 102

- 103 private int hashCode(String key,int Q){

- 104 int h = 0;

- 105 int off = 0;

- 106 char val[] = key.toCharArray();

- 107 int len = key.length();

- 108 for (int i = 0; i < len; i++) {

- 109 h = (30 + Q) * h + val[off++];

- 110 }

- 111 return changeInteger(h);

- 112 }

- 113

- 114 private int changeInteger(int h) {

- 115 return basic & h;

- 116 }

- 117

- 118 public static void main(String[] args) {

- 119 // TODO Auto-generated method stub


- 2. 还有⼀个java 版的 ，也是 使⽤ bitset
- 120 bloomFilter f = new bloomFilter("http://www.agrilink.cn/");

- 121

- 122 System.out.println(f.defaultSize);

- 123 f.add();

- 124 System.out.println(f.exist());

- 125 f.set0();

- 126 System.out.println(f.exist());

- 127 }

- 128

- 129 }


- 1 import java.util.BitSet;

- 2 public class SimpleBloomFilter {

- 3 private static final int DEFAULT_SIZE =2 << 24 ;

private static final int [] seeds =new int []{5,7, 11 , 13 , 31 , 37 , 61};

- 4

- 5 private BitSet bits= new BitSet(DEFAULT_SIZE);

- 6 private SimpleHash[] func=new SimpleHash[seeds.length];

- 7

- 8

- 9

- 10 public SimpleBloomFilter() {

- 11 for( int i= 0 ; i< seeds.length; i ++ ) {

- 12 func[i]=new SimpleHash(DEFAULT_SIZE, seeds[i]);

- 13 }

- 14 }

- 15 public void add(String value) {

- 16 for(SimpleHash f : func) {

- 17 bits.set(f.hash(value), true );

- 18 }

- 19 }

- 20 public boolean contains(String value) {

- 21 if(value ==null ) {

- 22 return false ;

- 23 }

- 24 boolean ret = true ;

- 25 for(SimpleHash f : func) {

- 26 ret=ret&& bits.get(f.hash(value));

- 27 }

- 28 return ret;

- 29 }

- 30

- 31 //内部类，simpleHash

- 32 public static class SimpleHash {

- 33 private int cap;

- 34 private int seed;

- 35 public SimpleHash( int cap, int seed) {

- 36 this.cap= cap;

- 37 this.seed =seed;

- 38 }

- 39 public int hash(String value) {


- 40 int result=0 ;

- 41 int len= value.length();

- 42 for (int i= 0 ; i< len; i ++ ) {

- 43 result =seed* result + value.charAt(i);

- 44 }

- 45 return (cap - 1 ) & result;

- 46 }

- 47 }

- 48

- 49

- 50

- 51

- 52

- 53

- 54

- 55 public static void main(String[] args) {

- 56 String value = "stone2083@yahoo.cn" ;

- 57 SimpleBloomFilter filter=new SimpleBloomFilter();

- 58 System.out.println(filter.contains(value));

- 59 filter.add(value);

- 60 System.out.println(filter.contains(value));

- 61 }

- 62

- 63

- 64

- 65

- 66 }


⼀个不会敲代码的程序员

