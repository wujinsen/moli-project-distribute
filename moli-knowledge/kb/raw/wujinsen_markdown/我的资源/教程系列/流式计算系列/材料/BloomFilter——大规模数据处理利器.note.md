# Bl omFilter⸺⼤规模数据处理利器

Bl om Filter是由Bl om在1970年提出的⼀种多哈希函数映射的快速查找算法。通常应⽤在⼀些需要快速 判断某个元素是否属于集合，但是并不严格要求10%正确的场合。

# ⼀. 实例

为了说明Bl om Filter存在的重要意义，举⼀个实例： 假设要你写⼀个⽹络蜘蛛（web crawler）。由于⽹络间的链接错综复杂，蜘蛛在⽹络间爬⾏很可能会形

成“环”。为了避免形成“环”，就需要知道蜘蛛已经访问过那些URL。给⼀个URL，怎样知道蜘蛛是否已经访问 过呢？稍微想想，就会有如下⼏种⽅案：

- 1. 将访问过的URL保存到数据库。
- 2. ⽤HashSet将访问过的URL保存起来。那只需接近O(1)的代价就可以查到⼀个URL是否被访问过了。
- 3. URL经过MD5或SHA-1等单向哈希后再保存到HashSet或数据库。
- 4. Bit-Map⽅法。建⽴⼀个BitSet，将每个URL经过⼀个哈希函数映射到某⼀位。 ⽅法1~3都是将访问过的URL完整保存，⽅法4则只标记URL的⼀个映射位。


以上⽅法在数据量较⼩的情况下都能完美解决问题，但是当数据量变得⾮常庞⼤时问题就来了。

- ⽅法1的缺点：数据量变得⾮常庞⼤后关系型数据库查询的效率会变得很低。⽽且每来⼀个URL就启动⼀

次数据库查询是不是太⼩题⼤做了？

- ⽅法2的缺点：太消耗内存。随着URL的增多，占⽤的内存会越来越多。就算只有1亿个URL，每个URL只 算50个字符，就需要5GB内存。
- ⽅法3：由于字符串经过MD5处理后的信息摘要⻓度只有128Bit，SHA-1处理后也只有160Bit，因此⽅法 3⽐⽅法2节省了好⼏倍的内存。
- ⽅法4消耗内存是相对较少的，但缺点是单⼀哈希函数发⽣冲突的概率太⾼。还记得数据结构课上学过的 Hash表冲突的各种解决⽅法么？若要降低冲突发⽣的概率到1%，就要将BitSet的⻓度设置为URL个数的10 倍。


实质上上⾯的算法都忽略了⼀个重要的隐含条件：允许⼩概率的出错，不⼀定要10%准确！也就是说少 量url实际上没有没⽹络蜘蛛访问，⽽将它们错判为已访问的代价是很⼩的⸺⼤不了少抓⼏个⽹⻚呗。

# ⼆. Bl om Filter的算法

废话说到这⾥，下⾯引⼊本篇的主⻆⸺Bl om Filter。其实上⾯⽅法4的思想已经很接近Bl om Filter 了。⽅法四的致命缺点是冲突概率⾼，为了降低冲突的概念，Bl om Filter使⽤了多个哈希函数，⽽不是⼀ 个。

Bl om Filter算法如下： 创建⼀个m位BitSet，先将所有位初始化为0，然后选择k个不同的哈希函数。第i个哈希函数对字符串str

哈希的结果记为h（i，str），且h（i，str）的范围是0到m-1 。

- (1) 加⼊字符串过程

下⾯是每个字符串处理的过程，⾸先是将字符串str“记录”到BitSet中的过程： 对于字符串str，分别计算h（1，str），h（2，str） … h（k，str）。然后将BitSet的第h（1，str）、h

（2，str） … h（k，str）位设为1。

图1.Bl om Filter加⼊字符串过程 很简单吧？这样就将字符串str映射到BitSet中的k个⼆进制位了。

- (2) 检查字符串是否存在的过程

下⾯是检查字符串str是否被BitSet记录过的过程： 对于字符串str，分别计算h（1，str），h（2，str） … h（k，str）。然后检查BitSet的第h（1，

str）、h（2，str） … h（k，str）位是否为1，若其中任何⼀位不为1则可以判定str⼀定没有被记录过。若全 部位都是1，则“认为”字符串str存在。

若⼀个字符串对应的Bit不全为1，则可以肯定该字符串⼀定没有被Bl om Filter记录过。（这是显然的， 因为字符串被记录过，其对应的⼆进制位肯定全部被设为1了）

但是若⼀个字符串对应的Bit全为1，实际上是不能10%的肯定该字符串被Bl om Filter记录过的。（因为 有可能该字符串的所有位都刚好是被其他字符串所对应）这种将该字符串划分错的情况，称为false positive 。

- (3) 删除字符串过程 字符串加⼊了就被不能删除了，因为删除会影响到其他字符串。实在需要删除字符串的可以使⽤Counting


![image 1](<BloomFilter——大规模数据处理利器.note_images/imageFile1.png>)

bl omfilter(CBF)，这是⼀种基本Bl om Filter的变体，CBF将基本Bl om Filter每⼀个Bit改为⼀个计数器，这 样就可以实现删除字符串的功能了。

Bl om Filter跟单哈希函数Bit-Map不同之处在于：Bl om Filter使⽤了k个哈希函数，每个字符串跟k个bit 对应。从⽽降低了冲突的概率。

# 三. Bl om Filter参数选择

(1)哈希函数选择

哈希函数的选择对性能的影响应该是很⼤的，⼀个好的哈希函数要能近似等概率的将字符串映射到各个 Bit。选择k个不同的哈希函数⽐较麻烦，⼀种简单的⽅法是选择⼀个哈希函数，然后送⼊k个不同的参数。

(2)Bit数组⼤⼩选择

# 哈希函数个数k、位数组⼤⼩m、加⼊的字符串数量n的关系可以参考参考⽂献1。该⽂献证明了 对于给定的m、n，当 k = ln(2)* m/n 时出错的概率是最⼩的。

同时该⽂献还给出特定的k，m，n的出错概率。例如：根据参考⽂献1，哈希函数个数k取10，位数组⼤ ⼩m设为字符串个数n的20倍时，false positive发⽣的概率是0. 089 ，这个概率基本能满⾜⽹络爬⾍的 需求了。

四. Bl om Filter实现代码 下⾯给出⼀个简单的Bl om Filter的Java实现代码：

import java.util.BitSet;publicclass BloomFilter {/* BitSet初始分配2^24个 bit */ privatestaticfinalint DEFAULT_SIZE =1<<25; /* 不同哈希函数的种⼦，⼀般应取质 数 */privatestaticfinalint[] seeds =newint[] { 5, 7, 11, 13, 31, 37, 61 };private BitSet bits =new BitSet(DEFAULT_SIZE);/* 哈希函数对象 */ private SimpleHash[] func =new SimpleHash[seeds.length];public BloomFilter() {for (int i =0; i < seeds.length; i++) {func[i] =new SimpleHash(DEFAULT_SIZE, seeds[i]);}}// 将字符串标记到bits中publicvoid add(String value) {for (SimpleHash f : func) {bits.set(f.hash(value), true);}}//判断字符串是否已经被bits标记 publicboolean contains(String value) {if (value ==null) {returnfalse;}boolean ret =true;for (SimpleHash f : func) {ret = ret && bits.get(f.hash(value));}return ret;}/* 哈希函数 类 */publicstaticclass SimpleHash {privateint cap;privateint seed;public SimpleHash(int cap, int see d) {this.cap = cap;this.seed = seed;}//hash函数，采⽤简单的加权和hashpublicint hash(String value) {int result =0;int len = value.length();for (int i =0; i < len; i++) {result = seed * result + value.charAt(i);}return (cap -1) & result;}}}

