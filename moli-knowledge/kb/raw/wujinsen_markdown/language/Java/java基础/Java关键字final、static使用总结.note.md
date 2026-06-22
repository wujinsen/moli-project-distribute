# Java关键字ﬁnal、static使⽤总结

⼀、ﬁnal 根据程序上下⽂环境，Java关键字ﬁnal有“这是⽆法改变的”或者“终态的”含义，它可以修饰⾮

抽象类、⾮抽象类成员⽅法和变量。你可能出于两种理解⽽需要阻⽌改变：设计或效率。 ﬁnal类不能被继承，没有⼦类，ﬁnal类中的⽅法默认是ﬁnal的。 ﬁnal⽅法不能被⼦类的⽅法覆盖，但可以被继承。 ﬁnal成员变量表示常量，只能被赋值⼀次，赋值后值不再改变。 ﬁnal不能⽤于修饰构造⽅法。 注意：⽗类的private成员⽅法是不能被⼦类⽅法覆盖的，因此private类型的⽅法默认是ﬁnal类

型的。

- 1、ﬁnal类 ﬁnal类不能被继承，因此ﬁnal类的成员⽅法没有机会被覆盖，默认都是ﬁnal的。在设计类时候，

如果这个类不需要有⼦类，类的实现细节不允许改变，并且确信这个类不会载被扩展，那么就设计为 ﬁnal类。

- 2、ﬁnal⽅法 如果⼀个类不允许其⼦类覆盖某个⽅法，则可以把这个⽅法声明为ﬁnal⽅法。 使⽤ﬁnal⽅法的原因有⼆： 第⼀、把⽅法锁定，防⽌任何继承类修改它的意义和实现。 第⼆、⾼效。编译器在遇到调⽤ﬁnal⽅法时候会转⼊内嵌机制，⼤⼤提⾼执⾏效率。 例如：


- public class Test1 { public static void main(String[] args) { } // TODO ⾃动⽣成⽅法存根 public void f1() {

- System.out.println("f1");

}

//public⽆法被⼦类覆盖的⽅法final void f2() {

- System.out.println("f2");

} public void f3() {

- System.out.println("f3");

} private void f4() {

- System.out.println("f4");


} }

- public class Test2 extends Test1 { public void f1(){


System.out.println("Test1⽗类⽅法f1被覆盖!");

} public static void main(String[] args) {

Test2 t=new Test2();

- t.f1();

- t.f2();t.f3();//t.f4();////调⽤从⽗类继承过来的调⽤从⽗类继承过来的⽅法//调⽤失败，⽆法从⽗类继承获得final⽅法


} }

- 3、ﬁnal变量（常量） ⽤ﬁnal修饰的成员变量表示常量，值⼀旦给定就⽆法改变！ ﬁnal修饰的变量有三种：静态变量、实例变量和局部变量，分别表示三种类型的常量。 从下⾯的例⼦中可以看出，⼀旦给ﬁnal变量初值后，值就不能再改变了。 另外，ﬁnal变量定义的时候，可以先声明，⽽不给初值，这中变量也称为ﬁnal空⽩，⽆论什么情


况，编译器都确保空⽩ﬁnal在使⽤之前必须被初始化。但是，ﬁnal空⽩在ﬁnal关键字ﬁnal的使⽤上提供 了更⼤的灵活性，为此，⼀个类中的ﬁnal数据成员就可以实现依对象⽽有所不同，却有保持其恒定不 变的特征。

package org.leizhimin;

- public class Test3 { private final String S = "final实例变量S"; private final int A = 100; public final int B = 90;


public static final int C = 80; private static final int D = 70;

public final int E; //final空⽩,必须在初始化对象的时候赋初值

public Test3(int x) {

E = x; }

/**

- * @param args

- */


public static void main(String[] args) { Test3 t = new Test3(2);

//t.A=101;//t.B=91;//t.C=81;//t.D=71; //////出错出错出错//出错,final,final,final,final变量的值⼀旦给定就⽆法改变变量的值⼀旦给定就⽆法改变变量的值⼀旦给定就⽆法改变变量的值⼀旦给定就⽆法改变

- System.out.println(t.A);

- System.out.println(t.B);

- System.out.println(t.C); //不推荐⽤对象⽅式访问静态字段

- System.out.println(t.D); //不推荐⽤对象⽅式访问静态字段 System.out.println(Test3.C); System.out.println(Test3.D); //System.out.println(Test3.E); //出错,因为E为final空⽩,依据不同对象值有所不同.

- System.out.println(t.E);


Test3 t1 = new Test3(3);

} System.out.println(t1.E); //final空⽩变量E依据对象的不同⽽不同

private void test() { System.out.println(new Test3(1).A); System.out.println(Test3.C); System.out.println(Test3.D);

}

public void test2() {

finalfinalfinal intintint a;bc;= 4;//final//final//空⽩局部常量空⽩,⼀直没有给赋值,在需要的时候才赋值--final⽤于局部变量的情形. a = 3;

- //a=4; 出错,已经给赋过值了.

- //b=2; 出错,已经给赋过值了.


} }

- 4、ﬁnal参数 当函数参数为ﬁnal类型时，你可以读取使⽤该参数，但是⽆法改变该参数的值。


- public class Test4 { public static void main(String[] args) {


new Test4().f1(2); }

public void f1(final int i) { //i++; //i是final类型的,值不允许改变的. System.out.print(i);

} }

⼆、static

static表示“全局”或者“静态”的意思，⽤来修饰成员变量和成员⽅法，也可以形成静态static代 码块，但是Java语⾔中没有全局变量的概念。

被static修饰的成员变量和成员⽅法独⽴于该类的任何对象。也就是说，它不依赖类特定的实 例，被类的所有实例共享。只要这个类被加载，Java虚拟机就能根据类名在运⾏时数据区的⽅法区内 定找到他们。因此，static对象可以在它的任何对象创建之前访问，⽆需引⽤任何对象。

⽤public修饰的static成员变量和成员⽅法本质是全局变量和全局⽅法，当声明它类的对象市， 不⽣成static变量的副本，⽽是类的所有实例共享同⼀个static变量。

static变量前可以有private修饰，表示这个变量可以在类的静态代码块中，或者类的其他静态成 员⽅法中使⽤（当然也可以在⾮静态成员⽅法中使⽤--废话），但是不能在其他类中通过类名来直接 引⽤，这⼀点很重要。实际上你需要搞明⽩，private是访问权限限定，static表示不要实例化就可以使 ⽤，这样就容易理解多了。static前⾯加上其它访问权限关键字的效果也以此类推。

static修饰的成员变量和成员⽅法习惯上称为静态变量和静态⽅法，可以直接通过类名来访问， 访问语法为： 类名.静态⽅法名(参数列表...) 类名.静态变量名

⽤static修饰的代码块表示静态代码块，当Java虚拟机（JVM）加载类时，就会执⾏该代码块 （⽤处⾮常⼤，呵呵）。

- 1、static变量 按照是否静态的对类成员变量进⾏分类可分两种：⼀种是被static修饰的变量，叫静态变量或类

变量；另⼀种是没有被static修饰的变量，叫实例变量。两者的区别是：

对于静态变量在内存中只有⼀个拷⻉（节省内存），JVM只为静态分配⼀次内存，在加载类的过 程中完成静态变量的内存分配，可⽤类名直接访问（⽅便），当然也可以通过对象来访问（但是这是 不推荐的）。

对于实例变量，没创建⼀个实例，就会为实例变量分配⼀次内存，实例变量可以在内存中有多个 拷⻉，互不影响（灵活）。

- 2、静态⽅法 静态⽅法可以直接通过类名调⽤，任何的实例也都可以调⽤，因此静态⽅法中不能⽤this和

super关键字，不能直接访问所属类的实例变量和实例⽅法(就是不带static的成员变量和成员成员⽅ 法)，只能访问所属类的静态成员变量和成员⽅法。因为实例成员与特定的对象关联！这个需要去理 解，想明⽩其中的道理，不是记忆！！！

因为static⽅法独⽴于任何实例，因此static⽅法必须被实现，⽽不能是抽象的abstract。

- 3、static代码块 static代码块也叫静态代码块，是在类中独⽴于类成员的static语句块，可以有多个，位置可以随


便放，它不在任何的⽅法体内，JVM加载类时会执⾏这些静态的代码块，如果static代码块有多个， JVM将按照它们在类中出现的先后顺序依次执⾏它们，每个代码块只会被执⾏⼀次。例如：

- public class Test5 { private static int a; private int b;


static { Test5.a = 3; System.out.println(a); Test5 t = new Test5(); t.f(); t.b = 1000; System.out.println(t.b);

}

static { Test5.a = 4; System.out.println(a);

}

public static void main(String[] args) {

} // TODO ⾃动⽣成⽅法存根

static { Test5.a = 5; System.out.println(a);

}

public void f() {

System.out.println("hhahhahah"); }

}

运⾏结果：

- 3 hhahhahah 1000

- 4

- 5 利⽤静态代码块可以对⼀些static变量进⾏赋值，最后再看⼀眼这些例⼦，都⼀个static的main


⽅法，这样JVM在运⾏main⽅法的时候可以直接调⽤⽽不⽤创建实例。

- 4、static和ﬁnal⼀块⽤表示什么 static ﬁnal⽤来修饰成员变量和成员⽅法，可简单理解为“全局常量”！ 对于变量，表示⼀旦给值就不可修改，并且通过类名可以访问。 对于⽅法，表示不可覆盖，并且可以通过类名直接访问。


特别要注意⼀个问题：

对于被static和ﬁnal修饰过的实例常量，实例本身不能再改变了，但对于⼀些容器类型（⽐如， ArrayList、HashMap）的实例变量，不可以改变容器变量本身，但可以修改容器中存放的对象，这⼀ 点在编程中⽤到很多。

也许说了这么多，反倒把你搞晕了，还是看个例⼦吧：

public class TestStaticFinal { private static final String strStaticFinalVar = "aaa"; private static String strStaticVar = null; private final String strFinalVar = null; private static final int intStaticFinalVar = 0; private static final Integer integerStaticFinalVar = new Integer(8); private static final ArrayList<String> alStaticFinalVar = new ArrayList<String>();

private void test() { System.out.println("-------------值处理前----------\r\n"); System.out.println("strStaticFinalVar=" + strStaticFinalVar + "\r\n"); System.out.println("strStaticVar=" + strStaticVar + "\r\n"); System.out.println("strFinalVar=" + strFinalVar + "\r\n"); System.out.println("intStaticFinalVar=" + intStaticFinalVar + "\r\n"); System.out.println("integerStaticFinalVar=" + integerStaticFinalVar + "\r\n"); System.out.println("alStaticFinalVar=" + alStaticFinalVar + "\r\n");

//strStaticFinalVar="哈哈哈哈"; //错误，final表示终态,不可以改变变量本身. strStaticVar = "哈哈哈哈"; //正确，static表示类变量,值可以改变. //strFinalVar="呵呵呵呵"; //错误, final表示终态，在定义的时候就要初值（哪怕给个null），

⼀旦给定后就不可再更改。//intStaticFinalVar=2; //错误, final表示终态，在定义的时候就要初值（哪怕给个null），

⼀旦给定后就不可再更改。//integerStaticFinalVar=new Integer(8); //错误, final表示终态，在定义的时候就要初值（哪怕给

个null），⼀旦给定后就不可再更改。alStaticFinalVar.add("aaa"); //正确，容器变量本身没有变化，但存放内容发⽣了变化。这个规则是⾮ 常常⽤的，有很多⽤途。

alStaticFinalVar.add("bbb"); //正确，容器变量本身没有变化，但存放内容发⽣了变化。这个规则是⾮ 常常⽤的，有很多⽤途。

System.out.println("-------------值处理后----------\r\n"); System.out.println("strStaticFinalVar=" + strStaticFinalVar + "\r\n"); System.out.println("strStaticVar=" + strStaticVar + "\r\n"); System.out.println("strFinalVar=" + strFinalVar + "\r\n"); System.out.println("intStaticFinalVar=" + intStaticFinalVar + "\r\n"); System.out.println("integerStaticFinalVar=" + integerStaticFinalVar + "\r\n"); System.out.println("alStaticFinalVar=" + alStaticFinalVar + "\r\n");

}

public static void main(String args[]) { new TestStaticFinal().test(); }

}

运⾏结果如下：

-------------值处理前---------strStaticFinalVar=aaa strStaticVar=null

strFinalVar=null intStaticFinalVar=0 integerStaticFinalVar=8 alStaticFinalVar=[]

-------------值处理后---------strStaticFinalVar=aaa strStaticVar=哈哈哈哈 strFinalVar=null intStaticFinalVar=0 integerStaticFinalVar=8 alStaticFinalVar=[aaa, bbb]

Process ﬁnished with exit code 0

看了上⾯这个例⼦，就清楚很多了，但必须明⽩：通过static ﬁnal修饰的容器类型变量中所“装” 的对象是可改变的。这是和⼀般基本类型和类类型变量差别很⼤的地⽅。

