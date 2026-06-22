在翻《深⼊理解Java虚拟机》的书时，⼜看到了2-7的 String.intern()返回引⽤的测试。 其实要搞明⽩String.intern()，我总结了下⾯⼏条规则： ⼀、new String都是在堆上创建字符串对象。当调⽤ intern() ⽅法时，编译器会将字符串添加到常量池 中（stringTable维护），并返回指向该常量的引⽤。

⼆、通过字⾯量赋值创建字符串（如：String str=”twm”）时，会先在常量池中查找是否存在相同的字 符串，若存在，则将栈中的引⽤直接指向该字符串；若不存在，则在常量池中⽣成⼀个字符串，再将 栈中的引⽤指向该字符串。

三、常量字符串的“+”操作，编译阶段直接会合成为⼀个字符串。如string str=”JA”+”VA”，在编译阶段 会直接合并成语句String str=”JAVA”，于是会去常量池中查找是否存在”JAVA”,从⽽进⾏创建或引⽤。

四、对于final字段，编译期直接进⾏了常量替换（⽽对于⾮final字段则是在运⾏期进⾏赋值处理的）。

- final String str1=”ja”;
- final String str2=”va”; String str3=str1+str2; 在编译时，直接替换成了String str3=”ja”+”va”，根据第三条规则，再次替换成String str3=”JAVA”


五、常量字符串和变量拼接时（如：String str3=baseStr + “01”;）会调⽤stringBuilder.apend()在堆 上创建新的对象。

六、JDK 1.7后，intern⽅法还是会先去查询常量池中是否有已经存在，如果存在，则返回常量池中的 引⽤，这⼀点与之前没有区别，区别在于，如果在常量池找不到对应的字符串，则不会再将字符串拷 ⻉到常量池，⽽只是在常量池中⽣成⼀个对原字符串的引⽤。简单的说，就是往常量池放的东⻄变 了：原来在常量池中找不到时，复制⼀个副本放到常量池，1.7后则是将在堆上的地址引⽤复制到常量 池。

举例说明：

String str2 = new String("str")+new String("01"); str2.intern();

- String str1 = "str01";


System.out.println(str2=str1);

- 1
- 2
- 3
- 4 在JDK 1.7下，当执⾏str2.intern();时，因为常量池中没有“str01”这个字符串，所以会在常量池中⽣成 ⼀个对堆中的“str01”的引⽤(注意这⾥是引⽤ ，就是这个区别于JDK 1.6的地⽅。在JDK1.6下是⽣成原 字符串的拷⻉)，⽽在进⾏String str1 = “str01”;字⾯量赋值的时候，常量池中已经存在⼀个引⽤，所以 直接返回了该引⽤，因此str1和str2都指向堆中的同⼀个字符串，返回true。


- String str2 = new String("str")+new String("01"); String str1 = "str01"; str2.intern(); System.out.println(str2=str1);


- 1
- 2
- 3
- 4 将中间两⾏调换位置以后，因为在进⾏字⾯量赋值（String str1 = “str01″）的时候，常量池中不存在， 所以str1指向的常量池中的位置，⽽str2指向的是堆中的对象，再进⾏intern⽅法时，对str1和str2已经 没有影响了，所以返回false。


常⻅试题解答

有了对以上的知识的了解，我们现在再来看常⻅的⾯试或笔试题就很简单了： Q：下列程序的输出结果：

- String s1 = “abc”;
- String s2 = “abc”; System.out.println(s1 = s2); A：true，均指向常量池中对象。


Q：下列程序的输出结果：

- String s1 = new String(“abc”);
- String s2 = new String(“abc”); System.out.println(s1 = s2); A：false，两个引⽤指向堆中的不同对象。


Q：下列程序的输出结果： String s1 = “abc”; String s2 = “a”; String s3 = “bc”; String s4 = s2 + s3; System.out.println(s1 = s4); A：false，因为s2+s3实际上是使⽤StringBuilder.apend来完成，会⽣成不同的对象。

Q：下列程序的输出结果： String s1 = “abc”;

- final String s2 = “a”;
- final String s3 = “bc”; String s4 = s2 + s3; System.out.println(s1 = s4); A：true，因为final变量在编译后会直接替换成对应的值，所以实际上等于s4=”a”+”bc”，⽽这种情况 下，编译器会直接合并为s4=”abc”，所以最终s1=s4。


Q：下列程序的输出结果： String s = new String(“abc”);

- String s1 = “abc”;
- String s2 = new String(“abc”);


- System.out.println(s = s1.intern();
- System.out.println(s = s2.intern(); System.out.println(s1 = s2.intern(); A：false，false，true。


-

作者：唐⼤⻨ 来源：CSDN 原⽂：htps:/blog.csdn.net/sonfly/article/details/70147205 版权声明：本⽂为博主原创⽂章，转载请附上博⽂链接！

