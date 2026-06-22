# 基础篇

## 基本功

⾯向对象的特征

⾯向对象的三个基本特征是：封装、继承、多态。 封装 封装最好理解了。封装是⾯向对象的特征之⼀，是对象和类概念的主要特性。 封装，也就是把客观事物封装成抽象的类，并且类可以把⾃⼰的数据和⽅法只让可信的类或者对象 操作，对不可信的进⾏信息隐藏。 继承 ⾯向对象编程 (OOP) 语⾔的⼀个主要功能就是“继承”。继承是指这样⼀种能⼒：它可以使⽤现有类 的所有功能，并在⽆需重新编写原来的类的情况下对这些功能进⾏扩展。 多态 多态性（polymorphisn）是允许你将⽗对象设置成为和⼀个或更多的他的⼦对象相等的技术，赋值 之后，⽗对象就可以根据当前赋值给它的⼦对象的特性以不同的⽅式运作。简单的说，就是⼀句 话：允许将⼦类类型的指针赋值给⽗类类型的指针。 实现多态，有⼆种⽅式，覆盖，重载。

final, finally, finalize 的区别

final ⽤于声明属性,⽅法和类, 分别表示属性不可变, ⽅法不可覆盖, 类不可继承. finally 是异常处理语句结构的⼀部分，表示总是执⾏. finalize 是Object类的⼀个⽅法，在垃圾收集器执⾏的时候会调⽤被回收对象的此⽅法，可以覆盖 此⽅法提供垃圾收集时的其他资源回收，例如关闭⽂件等. JVM不保证此⽅法总被调⽤.

int 和 Integer 有什么区别 https://www.nowcoder.com/questionTerminal/aad1b52a4d98454da9d1d66d0c243a49

链接： 来源：⽜客⽹ int是java提供的8种原始数据类型之⼀。Java为每个原始类型提供了封装类，Integer是java为int提 供的封装类。 int的默认值为0，⽽Integer的默认值为null，是引⽤类型，即Integer可以区分出未赋值和值为0的区 别，int则⽆法表达出未赋值的情况， Java中int和Integer关系是⽐较微妙的。关系如下：

- 1、int是基本的数据类型；

- 2、Integer是int的封装类；

- 3、int和Integer都可以表示某⼀个数值；

- 4、int和Integer不能够互⽤，因为他们两种不同的数据类型；


重载和重写的区别

重载 Overload 表示同⼀个类中可以有多个名称相同的⽅法，但这些⽅法的参数列表各不相同（即 参数个数或类型不同）。

重写 Override 表示⼦类中的⽅法可以与⽗类中的某个⽅法的名称和参数完全相同，通过⼦类创建 的实例对象调⽤这个⽅法时，将调⽤⼦类中的定义⽅法，这相当于把⽗类中定义的那个完全相同的 ⽅法给覆盖了，这也是⾯向对象编程的多态性的⼀种表现。⼦类覆盖⽗类的⽅法时，只能⽐⽗类抛 出更少的异常，或者是抛出⽗类抛出的异常的⼦异常，因为⼦类可以解决⽗类的⼀些问题，不能⽐ ⽗类有更多的问题。⼦类⽅法的访问权限只能⽐⽗类的更⼤，不能更⼩。如果⽗类的⽅法是private 类型，那么，⼦类则不存在覆盖的限制，相当于⼦类中增加了⼀个全新的⽅法。 作者：天天向上 链接： 来源：知乎 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

https://www.zhihu.com/question/35874324/answer/144589616

抽象类和接⼝有什么区别

## 抽象类和接⼝的对⽐

<table>
  <tr>
    <th>参数</th>
    <th>抽象类</th>
    <th>接⼝</th>
  </tr>
  <tr>
    <td>默认的⽅法实现</td>
    <td>它可以有默认的⽅法实现</td>
    <td>接⼝完全是抽象的。它根本不存在⽅ 法的实现</td>
  </tr>
  <tr>
    <td>实现</td>
    <td>⼦类使⽤extends关键字来继承抽象 类。如果⼦类不是抽象类的话，它需 要提供抽象类中所有声明的⽅法的实 现。</td>
    <td>⼦类使⽤关键字implements来实现 接⼝。它需要提供接⼝中所有声明的 ⽅法的实现</td>
  </tr>
  <tr>
    <td>构造器</td>
    <td>抽象类可以有构造器</td>
    <td>接⼝不能有构造器</td>
  </tr>
  <tr>
    <td>与正常Java类的区别</td>
    <td>除了你不能实例化抽象类之外，它和 类没有任何区别</td>
    <td>接⼝是完全不同的类型</td>
  </tr>
  <tr>
    <td>访问修饰符</td>
    <td>普通Java 抽象⽅法可以有public、protected<br><br>这些修饰符</td>
    <td>接⼝⽅法默认修饰符是public。你不 可以使⽤其它修饰符。</td>
  </tr>
  <tr>
    <td>main⽅法</td>
    <td>和default 抽象⽅法可以有main⽅法并且我们可 以运⾏它</td>
    <td>接⼝没有main⽅法，因此我们不能运 ⾏它。</td>
  </tr>
  <tr>
    <td>多继承</td>
    <td>抽象⽅法可以继承⼀个类和实现多个 接⼝</td>
    <td>接⼝只可以继承⼀个或多个其它接⼝</td>
  </tr>
  <tr>
    <td>速度</td>
    <td>它⽐接⼝速度要快</td>
    <td>接⼝是稍微有点慢的，因为它需要时 间去寻找在类中实现的⽅法。</td>
  </tr>
  <tr>
    <td>添加新⽅法</td>
    <td>如果你往抽象类中添加新的⽅法，你 可以给它提供默认的实现。因此你不 需要改变你现在的代码。</td>
    <td>如果你往接⼝中添加⽅法，那么你必 须改变实现该接⼝的类。</td>
  </tr>
</table>


说说反射的⽤途及实现

Java反射机制是⼀个⾮常强⼤的功能，在很多的项⽬⽐如Spring，Mybatis都都可以看到反射的身 影。通过反射机制，我们可以在运⾏期间获取对象的类型信息。利⽤这⼀点我们可以实现⼯⼚模式 和代理模式等设计模式，同时也可以解决java泛型擦除等令⼈苦恼的问题。 获取⼀个对象对应的反射类，在Java中有三种⽅法可以获取⼀个对象的反射类，

通过getClass()⽅法 通过Class.forName()⽅法； 使⽤类.class 通过类加载器实现，getClassLoader()

说说⾃定义注解的场景及实现

登陆、权限拦截、⽇志处理，以及各种Java框架，如Spring，Hibernate，JUnit 提到注解就不能不 说反射，Java⾃定义注解是通过运⾏时靠反射获取注解。实际开发中，例如我们要获取某个⽅法的 调⽤⽇志，可以通过AOP（动态代理机制）给⽅法添加切⾯，通过反射来获取⽅法包含的注解，如 果包含⽇志注解，就进⾏⽇志记录。反射的实现在 Java 应⽤层⾯上讲，是通过对 Class 对象的操 作实现的，Class 对象为我们提供了⼀系列⽅法对类进⾏操作。在 Jvm 这个⻆度来说，Class ⽂件 是⼀组以 8 位字节为基础单位的⼆进制流，各个数据项⽬按严格的顺序紧凑的排列在 Class ⽂件 中，⾥⾯包含了类、⽅法、字段等等相关数据。通过对 Claas 数据流的处理我们即可得到字段、⽅ 法等数据。 作者：LeopPro 链接： 来源：掘⾦ 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

https://juejin.im/post/5a9fad016fb9a028b77a5ce9

HTTP 请求的 GET 与 POST ⽅式的区别

- 1.根据HTTP规范，GET⽤于信息获取，⽽且应该是安全的和幂等的。

- 2.根据HTTP规范，POST表示可能修改变服务器上的资源的请求。

- 3.⾸先是"GET⽅式提交的数据最多只能是1024字节"，因为GET是通过URL提交数据，那么GET可 提交的数据量就跟URL的⻓度有直接关系了。⽽实际上，URL不存在参数上限的问题，HTTP协议 规范没有对URL⻓度进⾏限制。这个限制是特定的浏览器及服务器对它的限制。IE对URL⻓度的限 制是2083字节(2K+35)。对于其他浏览器，如Netscape、FireFox等，理论上没有⻓度限制，其限 制取决于操作系统的⽀持。注意这是限制是整个URL⻓度，⽽不仅仅是你的参数值数据⻓度。


POST是没有⼤⼩限制的，HTTP协议规范也没有进⾏⼤⼩限制 session 与 cookie 区别

- 1、cookie数据存放在客户的浏览器上，session数据放在服务器上。

- 2、cookie不是很安全，别⼈可以分析存放在本地的COOKIE并进⾏COOKIE欺骗 考虑到安全应当使⽤session。

- 3、session会在⼀定时间内保存在服务器上。当访问增多，会⽐较占⽤你服务器的性能 考虑到减轻服务器性能⽅⾯，应当使⽤COOKIE。

- 4、单个cookie保存的数据不能超过4K，很多浏览器都限制⼀个站点最多保存20个cookie。

- 5、所以个⼈建议： 将登陆信息等重要信息存放为SESSION 其他信息如果需要保留，可以放在COOKIE中


session 分布式处理

- 1.Session复制 在⽀持Session复制的Web服务器上，通过修改Web服务器的配置，可以实现将Session同步到其 它Web服务器上，达到每个Web服务器上都保存⼀致的Session。 优点：代码上不需要做⽀持和修改。 缺点：需要依赖⽀持的Web服务器，⼀旦更换成不⽀持的Web服务器就不能使⽤了，在数据量很⼤ 的情况下不仅占⽤⽹络资源，⽽且会导致延迟。 适⽤场景：只适⽤于Web服务器⽐较少且Session数据量少的情况。 可⽤⽅案：开源⽅案tomcat-redis-session-manager，暂不⽀持Tomcat8。

- 2.Session粘滞 将⽤户的每次请求都通过某种⽅法强制分发到某⼀个Web服务器上，只要这个Web服务器上存储了 对应Session数据，就可以实现会话跟踪。 优点：使⽤简单，没有额外开销。 缺点：⼀旦某个Web服务器重启或宕机，相对应的Session数据将会丢失，⽽且需要依赖负载均衡 机制。 适⽤场景：对稳定性要求不是很⾼的业务情景。

- 3.Session集中管理 在单独的服务器或服务器集群上使⽤缓存技术，如Redis存储Session数据，集中管理所有的 Session，所有的Web服务器都从这个存储介质中存取对应的Session，实现Session共享。 优点：可靠性⾼，减少Web服务器的资源开销。 缺点：实现上有些复杂，配置较多。 适⽤场景：Web服务器较多、要求⾼可⽤性的情况。 可⽤⽅案：开源⽅案Spring Session，也可以⾃⼰实现，主要是重写HttpServletRequestWrapper 中的getSession⽅法，博主也动⼿写了⼀个，github搜索joincat⽤户，然后⾃取。

- 4.基于Cookie管理 这种⽅式每次发起请求的时候都需要将Session数据放到Cookie中传递给服务端。 优点：不需要依赖额外外部存储，不需要额外配置。 缺点：不安全，易被盗取或篡改；Cookie数量和⻓度有限制，需要消耗更多⽹络带宽。 适⽤场景：数据不重要、不敏感且数据量⼩的情况。 总结 这四种⽅式，相对来说，Session集中管理更加可靠，使⽤也是最多的。 作者：JavaQ 链接： 來源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。


https://www.jianshu.com/p/3dd4e06bdfa4

JDBC 流程

（1）向DriverManager类注册驱动数据库驱动程序

- （2）调⽤DriverManager.getConnection⽅法， 通过JDBC URL，⽤户名，密码取得数据库连接的 Connection对象。

- （3）获取Connection后， 便可以通过createStatement创建Statement⽤以执⾏SQL语句。

- （4） 有时候会得到查询结果，⽐如select，得到查询结果，查询（SELECT）的结果存放于结果 集（ResultSet）中。

- （5）关闭数据库语句，关闭数据库连接。


MVC 设计思想

MVC是三个单词的⾸字⺟缩写，它们是Model（模型）、View（视图）和Controller（控制）。 这个模式认为，程序不论简单或复杂，从结构上看，都可以分成三层。

- 1）最上⾯的⼀层，是直接⾯向最终⽤户的"视图层"（View）。它是提供给⽤户的操作界⾯，是程 序的外壳。

- 2）最底下的⼀层，是核⼼的"数据层"（Model），也就是程序需要操作的数据或信息。

- 3）中间的⼀层，就是"控制层"（Controller），它负责根据⽤户从"视图层"输⼊的指令，选取"数据 层"中的数据，然后对其进⾏相应的操作，产⽣最终结果。


equals 与 == 的区别

==与equals的主要区别是：==常⽤于⽐较原⽣类型，⽽equals()⽅法⽤于检查对象的相等性。另⼀ 个不同的点是：如果==和equals()⽤于⽐较对象，当两个引⽤地址相同，==返回true。⽽equals() 可以返回true或者false主要取决于重写实现。最常⻅的⼀个例⼦，字符串的⽐较，不同情况==和 equals()返回不同的结果。 使⽤==⽐较原⽣类型如：boolean、int、char等等，使⽤equals()⽐较对象。

==返回true如果两个引⽤指向相同的对象，equals()的返回结果依赖于具体业务实现 字符串的对⽐使⽤equals()代替==操作符 使⽤==⽐较原⽣类型如：boolean、int、char等等，使⽤equals()⽐较对象。

==返回true如果两个引⽤指向相同的对象，equals()的返回结果依赖于具体业务实现 字符串的对⽐使⽤equals()代替==操作符

## 集合

List 和 Set 区别

- 1、List,Set都是继承⾃Collection接⼝

- 2、List特点：元素有放⼊顺序，元素可重复 ，Set特点：元素⽆放⼊顺序，元素不可重复（注意： 元素虽然⽆放⼊顺序，但是元素在set中的位置是有该元素的HashCode决定的，其位置其实是固定 的）

- 3、List接⼝有三个实现类：LinkedList，ArrayList，Vector ，Set接⼝有两个实现类：HashSet(底 层由HashMap实现)，LinkedHashSet


List 和 Map 区别

List特点：元素有放⼊顺序，元素可重复; Map特点：元素按键值对存储，⽆放⼊顺序 ; List接⼝有三个实现类：LinkedList，ArrayList，Vector;

LinkedList：底层基于链表实现，链表内存是散乱的，每⼀个元素存储本身内存地址的同时还存储 下⼀个元素的地址。链表增删快，查找慢; Map接⼝有三个实现类：HashMap，HashTable，LinkeHashMap Map相当于和Collection⼀个级别的；Map该集合存储键值对，且要求保持键的唯⼀性；

Arraylist 与 LinkedList 区别

- 1) 因为Array是基于索引(index)的数据结构，它使⽤索引在数组中搜索和读取数据是很快的。Array 获取数据的时间复杂度是O(1),但是要删除数据却是开销很⼤的，因为这需要重排数组中的所有数 据。

- 2) 相对于ArrayList，LinkedList插⼊是更快的。因为LinkedList不像ArrayList⼀样，不需要改变数组 的⼤⼩，也不需要在数组装满的时候要将所有的数据重新装⼊⼀个新的数组，这是ArrayList最坏的 ⼀种情况，时间复杂度是O(n)，⽽LinkedList中插⼊或删除的时间复杂度仅为O(1)。ArrayList在插 ⼊数据时还需要更新索引（除了插⼊数组的尾部）。

- 3) 类似于插⼊数据，删除数据时，LinkedList也优于ArrayList。

- 4) LinkedList需要更多的内存，因为ArrayList的每个索引的位置是实际的数据，⽽LinkedList中的每 个节点中存储的是实际的数据和前后节点的位置。

- 5) 你的应⽤不会随机访问数据。因为如果你需要LinkedList中的第n个元素的时候，你需要从第⼀ 个元素顺序数到第n个数据，然后读取数据。

- 6) 你的应⽤更多的插⼊和删除元素，更少的读取数据。因为插⼊和删除元素不涉及重排数据，所 以它要⽐ArrayList要快。


ArrayList 与 Vector 区别 https://www.nowcoder.com/questionTerminal/0953369f92054cbfbf1024a1e723e04f

链接： 来源：⽜客⽹

- 1） 同步性:Vector是线程安全的，也就是说是同步的 ，⽽ArrayList 是线程序不安全的，不是同步 的 数2。

- 2）数据增⻓:当需要增⻓时,Vector默认增⻓为原来⼀倍 ，⽽ArrayList却是原来的50% ，这


样,ArrayList就有利于节约内存空间。

如果涉及到堆栈，队列等操作，应该考虑⽤Vector，如果需要快速随机访问元素，应该使⽤ArrayList 。

HashMap 和 Hashtable 的区别

- 1)HashMap⼏乎可以等价于Hashtable，除了HashMap是⾮synchronized的，并可以接受 null(HashMap可以接受为null的键值(key)和值(value)，⽽Hashtable则不⾏)。

- 2) HashMap是⾮synchronized，⽽Hashtable是synchronized，这意味着Hashtable是线程安全 的，多个线程可以共享⼀个Hashtable；⽽如果没有正确的同步的话，多个线程是不能共享 HashMap的。Java 5提供了ConcurrentHashMap，它是HashTable的替代，⽐HashTable的扩展性 更好。


- 3) 另⼀个区别是HashMap的迭代器(Iterator)是fail-fast迭代器，⽽Hashtable的enumerator迭代器不 是fail-fast的。所以当有其它线程改变了HashMap的结构（增加或者移除元素），将会抛出 ConcurrentModificationException，但迭代器本身的remove()⽅法移除元素则不会抛出 ConcurrentModificationException异常。但这并不是⼀个⼀定发⽣的⾏为，要看JVM。这条同样也 是Enumeration和Iterator的区别。

- 4) 由于Hashtable是线程安全的也是synchronized，所以在单线程环境下它⽐HashMap要慢。如果 你不需要同步，只需要单⼀线程，那么使⽤HashMap性能要好过Hashtable。

- 5) HashMap不能保证随着时间的推移Map中的元素次序是不变的。


HashSet 和 HashMap 区别

| HashMap | HashSet | | ------------------------------------------- | ------------------------------------------------------------ | | HashMap实现了Map接⼝ | HashSet实现了Set接⼝ | | HashMap储存键值对 | HashSet仅仅存储对象 | | 使⽤put()⽅法将元素放⼊map中 | 使⽤add()⽅法将元素放⼊set中 | | HashMap中使⽤键对象来计算hashcode值 | HashSet使⽤成员对象来计算hashcode值，对于两 个对象来说hashcode可能相同，所以equals()⽅法⽤来判断对象的相等性，如果两个对象不同的 话，那么返回false | | HashMap⽐较快，因为是使⽤唯⼀的键来获取对象 | HashSet较HashMap来说⽐较慢 |

HashMap 和 ConcurrentHashMap 的区别

1）放⼊HashMap的元素是key-value对。

- （2）底层说⽩了就是以前数据结构课程讲过的散列结构。

- （3）要将元素放⼊到hashmap中，那么key的类型必须要实现实现hashcode⽅法，默认这个⽅法 是根据对象的地址来计算的，具体我也记不太清楚了，接着还必须覆盖对象的equal⽅法。

- （4）ConcurrentHashMap对整个桶数组进⾏了分段，⽽HashMap则没有

- （5）ConcurrentHashMap在每⼀个分段上都⽤锁进⾏保护，从⽽让锁的粒度更精细⼀些，并发性 能更好，⽽HashMap没有锁机制，不是线程安全的。。。


HashMap 的⼯作原理及代码实现

HashMap基于hashing原理，我们通过put()和get()⽅法储存和获取对象。当我们将键值对传递给 put()⽅法时，它调⽤键对象的hashCode()⽅法来计算hashcode，让后找到bucket位置来储存值对 象。当获取对象时，通过键对象的equals()⽅法找到正确的键值对，然后返回值对象。HashMap使 ⽤链表来解决碰撞问题，当发⽣碰撞了，对象将会储存在链表的下⼀个节点中。 HashMap在每个 链表节点中储存键值对对象。

ConcurrentHashMap 的⼯作原理及代码实现

ConcurrentHashMap采⽤了⾮常精妙的"分段锁"策略，ConcurrentHashMap的主⼲是个Segment 数组。Segment继承了ReentrantLock，所以它就是⼀种可重⼊锁（ReentrantLock)。在 ConcurrentHashMap，⼀个Segment就是⼀个⼦哈希表，Segment⾥维护了⼀个HashEntry数组， 并发环境下，对于不同Segment的数据进⾏操作是不⽤考虑锁竞争的。

## 线程

创建线程的⽅式及实现 继承Thread类创建线程类

- a.
- b.
- c.


- （1）定义Thread类的⼦类，并重写该类的run⽅法，该run⽅法的⽅法体就代表了线程要完成的任 务。因此把run()⽅法称为执⾏体。

- （2）创建Thread⼦类的实例，即创建了线程对象。

- （3）调⽤线程对象的start()⽅法来启动该线程。


通过Runnable接⼝创建线程类

- （1）定义runnable接⼝的实现类，并重写该接⼝的run()⽅法，该run()⽅法的⽅法体同样是该线程 的线程执⾏体。

- （2）创建 Runnable实现类的实例，并依此实例作为Thread的target来创建Thread对象，该 Thread对象才是真正的线程对象。

- （3）调⽤线程对象的start()⽅法来启动该线程。


通过Callable和Future创建线程

- （1）创建Callable接⼝的实现类，并实现call()⽅法，该call()⽅法将作为线程执⾏体，并且有返回 值。

- （2）创建Callable实现类的实例，使⽤FutureTask类来包装Callable对象，该FutureTask对象封装 了该Callable对象的call()⽅法的返回值。

- （3）使⽤FutureTask对象作为Thread对象的target创建并启动新线程。

- （4）调⽤FutureTask对象的get()⽅法来获得⼦线程执⾏结束后的返回值 采⽤实现Runnable、Callable接⼝的⽅式创⻅多线程时，优势是： 线程类只是实现了Runnable接⼝或Callable接⼝，还可以继承其他类。 在这种⽅式下，多个线程可以共享同⼀个target对象，所以⾮常适合多个相同线程来处理同⼀份资 源的情况，从⽽可以将CPU、代码和数据分开，形成清晰的模型，较好地体现了⾯向对象的思想。 劣势是： 编程稍微复杂，如果要访问当前线程，则必须使⽤Thread.currentThread()⽅法。 使⽤继承Thread类的⽅式创建多线程时优势是： 编写简单，如果需要访问当前线程，则⽆需使⽤Thread.currentThread()⽅法，直接使⽤this即可获 得当前线程。 劣势是： 线程类已经继承了Thread类，所以不能再继承其他⽗类。


sleep() 、join（）、yield（）有什么区别

sleep()

sleep()⽅法需要指定等待的时间，它可以让当前正在执⾏的线程在指定的时间内暂停执⾏，进 ⼊阻塞状态，该⽅法既可以让其他同优先级或者⾼优先级的线程得到执⾏的机会，也可以让低优先 级的线程得到执⾏机会。但是sleep()⽅法不会释放“锁标志”，也就是说如果有synchronized同步 块，其他线程仍然不能访问共享数据。

wait()

wait()⽅法需要和notify()及notifyAll()两个⽅法⼀起介绍，这三个⽅法⽤于协调多个线程对共享 数据的存取，所以必须在synchronized语句块内使⽤，也就是说，调⽤wait()，notify()和notifyAll() 的任务在调⽤这些⽅法前必须拥有对象的锁。注意，它们都是Object类的⽅法，⽽不是Thread类的 ⽅法。

wait()⽅法与sleep()⽅法的不同之处在于，wait()⽅法会释放对象的“锁标志”。当调⽤某⼀对象 的wait()⽅法后，会使当前线程暂停执⾏，并将当前线程放⼊对象等待池中，直到调⽤了notify()⽅ 法后，将从对象等待池中移出任意⼀个线程并放⼊锁标志等待池中，只有锁标志等待池中的线程可 以获取锁标志，它们随时准备争夺锁的拥有权。当调⽤了某个对象的notifyAll()⽅法，会将对象等 待池中的所有线程都移动到该对象的锁标志等待池。

除了使⽤notify()和notifyAll()⽅法，还可以使⽤带毫秒参数的wait(long timeout)⽅法，效果是在 延迟timeout毫秒后，被暂停的线程将被恢复到锁标志等待池。

此外，wait()，notify()及notifyAll()只能在synchronized语句中使⽤，但是如果使⽤的是 ReenTrantLock实现同步，该如何达到这三个⽅法的效果呢？解决⽅法是使⽤ ReenTrantLock.newCondition()获取⼀个Condition类对象，然后Condition的await()，signal()以及 signalAll()分别对应上⾯的三个⽅法。 yield()

yield()⽅法和sleep()⽅法类似，也不会释放“锁标志”，区别在于，它没有参数，即yield()⽅法

只是使当前线程重新回到可执⾏状态，所以执⾏yield()的线程有可能在进⼊到可执⾏状态后⻢上⼜ 被执⾏，另外yield()⽅法只能使同优先级或者⾼优先级的线程得到执⾏机会，这也和sleep()⽅法不 同。

join()

join()⽅法会使当前线程等待调⽤join()⽅法的线程结束后才能继续执⾏

说说 CountDownLatch 原理

CountDownLatch 内部维护了⼀个整数n，n（要⼤于等于0）在==当前线程== 初始化 CountDownLatch⽅法指定。当前线程调⽤ CountDownLatch的await()⽅法阻塞当前线程，等待其 他调⽤CountDownLatch对象的CountDown()⽅法的线程执⾏完毕。 其他线程调⽤该 CountDownLatch的CountDown()⽅法，该⽅法会把n-1，直到所有线程执⾏完成，n等于0，==当 前线程==就恢复执⾏。

说说 CyclicBarrier 原理

CyclicBarrier简介CyclicBarrier是⼀个同步辅助类,允许⼀组线程互相等待,直到到达某个公共屏障点 (commonbarrierpoint)。因为该barrier在释放等待线程后可以重⽤,所以称它为循环的barrier。

说说 Semaphore 原理

Semaphore直译为信号。实际上Semaphore可以看做是⼀个信号的集合。不同的线程能够从 Semaphore中获取若⼲个信号量。当Semaphore对象持有的信号量不⾜时，尝试从Semaphore中 获取信号的线程将会阻塞。直到其他线程将信号量释放以后，阻塞的线程会被唤醒，重新尝试获取 信号量。

说说 Exchanger 原理

当⼀个线程到达exchange调⽤点时，如果它的伙伴线程此前已经调⽤了此⽅法，那么它的伙伴会 被调度唤醒并与之进⾏对象交换，然后各⾃返回。如果它的伙伴还没到达交换点，那么当前线程将 会被挂起，直⾄伙伴线程到达——完成交换正常返回；或者当前线程被中断——抛出中断异常；⼜ 或者是等候超时——抛出超时异常。

说说 CountDownLatch 与 CyclicBarrier 区别

- (01) CountDownLatch的作⽤是允许1或N个线程等待其他线程完成执⾏;⽽CyclicBarrier则是允许N 个线程相互等待。

- (02) CountDownLatch的计数器⽆法被重置;CyclicBarrier的计数器可以被重置后使⽤,因此它被称为 是循环的barrier。


ThreadLocal 原理分析

ThreadLocal提供了线程本地变量，它可以保证访问到的变量属于当前线程，每个线程都保存有⼀ 个变量副本，每个线程的变量都不同。ThreadLocal相当于提供了⼀种线程隔离，将变量与线程相 绑定。

讲讲线程池的实现原理

当提交⼀个新任务到线程池时，线程池的处理流程如下。

- 1）线程池判断核⼼线程池⾥的线程是否都在执⾏任务。如果不是，则创建⼀个新的⼯作 线程来执⾏任务。如果核⼼线程池⾥的线程都在执⾏任务，则进⼊下个流程。

- 2）线程池判断⼯作队列是否已经满。如果⼯作队列没有满，则将新提交的任务存储在这 个⼯作队列⾥。如果⼯作队列满了，则进⼊下个流程。

- 3）线程池判断线程池的线程是否都处于⼯作状态。如果没有，则创建⼀个新的⼯作线程 来执⾏任务。如果已经满了，则交给饱和策略来处理这个任务。


线程池的⼏种⽅式

在Executors类⾥⾯提供了⼀些静态⼯⼚，⽣成⼀些常⽤的线程池。

- 1、newFixedThreadPool：创建固定⼤⼩的线程池。线程池的⼤⼩⼀旦达到最⼤值就会保持不变， 如果某个线程因为执⾏异常⽽结束，那么线程池会补充⼀个新线程。

- 2、newCachedThreadPool：创建⼀个可缓存的线程池。如果线程池的⼤⼩超过了处理任务所需要 的线程，那么就会回收部分空闲（60秒不执⾏任务）的线程，当任务数增加时，此线程池⼜可以智 能的添加新线程来处理任务。此线程池不会对线程池⼤⼩做限制，线程池⼤⼩完全依赖于操作系统 （或者说JVM）能够创建的最⼤线程⼤⼩。

- 3、newSingleThreadExecutor：创建⼀个单线程的线程池。这个线程池只有⼀个线程在⼯作，也 就是相当于单线程串⾏执⾏所有任务。如果这个唯⼀的线程因为异常结束，那么会有⼀个新的线程 来替代它。此线程池保证所有任务的执⾏顺序按照任务的提交顺序执⾏。

- 4、newScheduledThreadPool：创建⼀个⼤⼩⽆限的线程池。此线程池⽀持定时以及周期性执⾏ 任务的需求。

- 5、newSingleThreadScheduledExecutor：创建⼀个单线程的线程池。此线程池⽀持定时以及周期 性执⾏任务的需求。


线程的⽣命周期

新建(New)、就绪（Runnable）、运⾏（Running）、阻塞(Blocked)和死亡(Dead)5种状态

## 锁机制

说说线程安全问题

线程安全是多线程领域的问题，线程安全可以简单理解为⼀个⽅法或者⼀个实例可以在多线程环境 中使⽤⽽不会出现问题。 在Java多线程编程当中，提供了多种实现Java线程安全的⽅式：

最简单的⽅式，使⽤Synchronization关键字:Java Synchronization介绍 使⽤java.util.concurrent.atomic 包中的原⼦类，例如 AtomicInteger 使⽤java.util.concurrent.locks 包中的锁 使⽤线程安全的集合ConcurrentHashMap 使⽤volatile关键字，保证变量可⻅性（直接从内存读，⽽不是从线程cache读）

volatile 实现原理 在JVM底层volatile是采⽤“内存屏障”来实现的。 缓存⼀致性协议（MESI协议）它确保每个缓存中使⽤的共享变量的副本是⼀致的。其核⼼ 思想如下：当某个CPU在写数据时，如果发现操作的变量是共享变量，则会通知其他CPU 告知该变量的缓存⾏是⽆效的，因此其他CPU在读取该变量时，发现其⽆效会重新从主存 中加载数据。

synchronize 实现原理

同步代码块是使⽤monitorenter和monitorexit指令实现的，同步⽅法（在这看不出来需要看JVM底 层实现）依靠的是⽅法修饰符上的ACC_SYNCHRONIZED实现。

synchronized 与 lock 的区别

⼀、synchronized和lock的⽤法区别

- （1）synchronized(隐式锁)：在需要同步的对象中加⼊此控制，synchronized可以加在⽅法上，也 可以加在特定代码块中，括号中表示需要锁的对象。

- （2）lock（显示锁）：需要显示指定起始位置和终⽌位置。⼀般使⽤ReentrantLock类做为锁，多 个线程中必须要使⽤⼀个ReentrantLock类做为对 象才能保证锁的⽣效。且在加锁和解锁处需要通 过lock()和unlock()显示指出。所以⼀般会在finally块中写unlock()以防死锁。 ⼆、synchronized和lock性能区别 synchronized是托管给JVM执⾏的，⽽lock是java写的控制锁的代码。在Java1.5中，synchronize 是性能低效的。因为 这是⼀个重量级操作，需要调⽤操作接⼝，导致有可能加锁消耗的系统时间 ⽐加锁以外的操作还多。相⽐之下使⽤Java提供的Lock对象，性能更⾼⼀些。但 是到了Java1.6， 发⽣了变化。synchronize在语义上很清晰，可以进⾏很多优化，有适应⾃旋，锁消除，锁粗化， 轻量级锁，偏向锁等等。导致 在Java1.6上synchronize的性能并不⽐Lock差。 三、synchronized和lock机制区别 （1）synchronized原始采⽤的是CPU悲观锁机制，即线程获得的是独占锁。独占锁意味着其 他线 程只能依靠阻塞来等待线程释放锁。


（2）Lock⽤的是乐观锁⽅式。所谓乐观锁就是，每次不加锁⽽是假设没有冲突⽽去完成某项操 作，如果因为冲突失败就重试，直到成功为⽌。乐观锁实现的机制就 是CAS操作（Compare and Swap）。

CAS 乐观锁

CAS是项乐观锁技术，当多个线程尝试使⽤CAS同时更新同⼀个变量时，只有其中⼀个线程能更新 变量的值，⽽其它线程都失败，失败的线程并不会被挂起，⽽是被告知这次竞争中失败，并可以再 次尝试。

CAS 操作包含三个操作数 —— 内存位置（V）、预期原值（A）和新值(B)。如果内存位置的值与 预期原值相匹配，那么处理器会⾃动将该位置值更新为新值。否则，处理器不做任何操作。⽆论哪 种情况，它都会在 CAS 指令之前返回该位置的值。（在 CAS 的⼀些特殊情况下将仅返回 CAS 是 否成功，⽽不提取当前值。）CAS 有效地说明了“我认为位置 V 应该包含值 A；如果包含该值，则 将 B 放到这个位置；否则，不要更改该位置，只告诉我这个位置现在的值即可。”这其实和乐观锁 的冲突检查+数据更新的原理是⼀样的。

ABA 问题

CAS会导致“ABA问题”。 CAS算法实现⼀个重要前提需要取出内存中某时刻的数据，⽽在下时刻⽐较并替换，那么在这个时 间差类会导致数据的变化。 ⽐如说⼀个线程one从内存位置V中取出A，这时候另⼀个线程two也从内存中取出A，并且two进⾏ 了⼀些操作变成了B，然后two⼜将V位置的数据变成A，这时候线程one进⾏CAS操作发现内存中 仍然是A，然后one操作成功。尽管线程one的CAS操作成功，但是不代表这个过程就是没有问题 的。 部分乐观锁的实现是通过版本号（version）的⽅式来解决ABA问题，乐观锁每次在执⾏数据的修 改操作时，都会带上⼀个版本号，⼀旦版本号和数据的版本号⼀致就可以执⾏修改操作并对版本号 执⾏+1操作，否则就执⾏失败。因为每次操作的版本号都会随之增加，所以不会出现ABA问题， 因为版本号只会增加不会减少。

乐观锁的业务场景及实现⽅式

乐观锁（Optimistic Lock）： 每次获取数据的时候，都不会担⼼数据被修改，所以每次获取数据的时候都不会进⾏加锁，但是在 更新数据的时候需要判断该数据是否被别⼈修改过。如果数据被其他线程修改，则不进⾏数据更 新，如果数据没有被其他线程修改，则进⾏数据更新。由于数据没有进⾏加锁，期间该数据可以被 其他线程进⾏读写操作。 ⽐较适合读取操作⽐较频繁的场景，如果出现⼤量的写⼊操作，数据发⽣冲突的可能性就会增⼤， 为了保证数据的⼀致性，应⽤层需要不断的重新获取数据，这样会增加⼤量的查询操作，降低了系 统的吞吐量。

