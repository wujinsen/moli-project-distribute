在java⾯试过程中，曾多次问到过String与StringBufer之间的区别。 回答：JAVA平台提供了两个类：String和StringBufer，它们可以储存和操作字符串，即包含多个字符的字符数据。 这个String类提供了数值不可改变的字符串。⽽这个StringBufer类提供的字符串进⾏修改。当你知道字符数据要改 变的时候你就可以使⽤StringBufer。典型地，你可以使⽤ StringBufer来动态构造字符数据。

由上可以看出StringBufer可以动态改变字符数据，那么如果定义了⼀个final的StringBufer后，还可以改变字符数 据吗？例如下列代码有错误吗？

final StringBufer sb = new StringBufer();

- sb.apend("a");

- sb.apend("b"); System.out.println(sb.toString();


答案是：正确的。定义为final类型的数据是不可以修改的，那为啥上⾯的代码为啥是正确的呢？我们要看⼀下final 的定义了：对于基本类型，final使数值恒定不变；⽽对于对象引⽤，final使引⽤恒定不变。⼀旦引⽤被初始化指向 ⼀个对象，就⽆法再把它改为指向另⼀个对象。然⽽，对象其⾃身却是可以被修改的，Java并未提供使任何对象恒定 不变的途径。

根据上⾯的定义，那么下⾯的代码是错误的。 final StringBufer sb = new StringBufer();

- sb.apend("a");

- sb.apend("b"); System.out.println(sb.toString();


sb = new StringBufer(); 因为变量sb定义为final类型的了，所以它的引⽤不能改变；所以sb = new StringBufer()；代码是错误的，编译不通 过。

