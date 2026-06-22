enum 的全称为 enumeration， 是 JDK 1.5 中引⼊的新特性，存放在 java.lang 包中。 下⾯是我在使⽤ enum 过程中的⼀些经验和总结，主要包括如下内容：

- 1.

- 2.

- 3.

- 4.

- 5.

- 6.

- 7.

- 8. 原始的接⼝定义常量


原始的接⼝定义常量 语法（定义） 遍历、switch 等常⽤操作 enum 对象的常⽤⽅法介绍 给 enum ⾃定义属性和⽅法 EnumSet，EnumMap 的应⽤ enum 的原理分析 总结

<table>
  <tr>
    <th>public interface IConstants { String MON = "Mon"; String TUE = "Tue"; String WED = "Wed"; String THU = "Thu"; String FRI = "Fri"; String SAT = "Sat"; String SUN = "Sun";</th>
  </tr>
</table>


}

## 语法（定义）

创建枚举类型要使⽤ enum 关键字，隐含了所创建的类型都是 java.lang.Enum 类的⼦类 （java.lang.Enum 是⼀个抽象类）。枚举类型符合通⽤模式 Class Enum<E extends Enum<E>>， ⽽ E 表示枚举类型的名称。枚举类型的每⼀个值都将映射到 protected Enum(String name, int ordinal) 构造函数中，在这⾥，每个值的名称都被转换成⼀个字符串，并且序数设置表示了此设置被 创建的顺序。

<table>
  <tr>
    <th>package com.hmw.test; /*<br><br>* 枚举测试类<br>* @author <a href="mailto:hemingwang0902@126.com">何明旺</a><br>*/ public enum EnumTest {<br><br><br>MON, TUE, WED, THU, FRI, SAT, SUN;</th>
  </tr>
</table>


}

这段代码实际上调⽤了7次 Enum(String name, int ordinal)：

<table>
  <tr>
    <th>new Enum<EnumTest>("MON",0); new Enum<EnumTest>("TUE",1); new Enum<EnumTest>("WED",2);</th>
  </tr>
</table>


. .

## 遍历、switch 等常⽤操作

对enum进⾏遍历和switch的操作示例代码：

<table>
  <tr>
    <th>public clas Test { public static void main(String[] args) { for (EnumTest e : EnumTest.values() { System.out.println(e.toString(); }<br><br>System.out.println(" -我是分隔线 -");<br><br>EnumTest test = EnumTest.TUE; switch (test) { case MON: System.out.println("今天是星期⼀"); break; case TUE: System.out.println("今天是星期⼆"); break;<br><br>/ . . default: System.out.println(test); break; } }</th>
  </tr>
</table>


}

输出结果：

<table>
  <tr>
    <th>MON TUE WED THU FRI SAT SUN<br><br>-我是分隔线 今天是星期⼆</th>
  </tr>
</table>


## enum 对象的常⽤⽅法介绍

int compareTo(E o)

⽐较此枚举与指定对象的顺序。

Class<E> getDeclaringClass()

返回与此枚举常量的枚举类型相对应的 Class 对象。

String name()

返回此枚举常量的名称，在其枚举声明中对其进⾏声明。

int ordinal()

返回枚举常量的序数（它在枚举声明中的位置，其中初始常量序数为零）。

String toString()

返回枚举常量的名称，它包含在声明中。

static <T extends Enum<T>> T valueOf(Class<T> enumType, String name)

返回带指定名称的指定枚举类型的枚举常量。

<table>
  <tr>
    <th>public clas Test { public static void main(String[] args) { EnumTest test = EnumTest.TUE;<br><br>/compareTo(E o) switch (test.compareTo(EnumTest.MON) { case -1: System.out.println("TUE 在 MON 之前"); break; case 1: System.out.println("TUE 在 MON 之后"); break; default: System.out.println("TUE 与 MON 在同⼀位置"); break; }<br><br>/getDeclaringClas() System.out.println("getDeclaringClas(): " + test.getDeclaringClas().getName();<br><br>/name() 和 toString() System.out.println("name(): " + test.name(); System.out.println("toString(): " + test.toString();<br><br>/ordinal()， 返回值是从 0 开始 System.out.println("ordinal(): " + test.ordinal(); }</th>
  </tr>
</table>


}

输出结果：

<table>
  <tr>
    <th>TUE 在 MON 之后 getDeclaringClas(): com.hmw.test.EnumTest name(): TUE toString(): TUE</th>
  </tr>
</table>


ordinal(): 1

## 给 enum ⾃定义属性和⽅法

给 enum 对象加⼀下 value 的属性和 getValue() 的⽅法：

<table>
  <tr>
    <th>package com.hmw.test;<br><br>/*<br><br>* 枚举测试类<br><br>*<br><br>* @author <a href="mailto:hemingwang0902@126.com">何明旺</a><br>*/ public enum EnumTest {<br><br><br>MON(1), TUE(2), WED(3), THU(4), FRI(5), SAT(6) { @Override public bolean isRest() { return true; } }, SUN(0) { @Override public bolean isRest() { return true; } };<br><br>private int value;<br><br>private EnumTest(int value) { this.value = value; }<br><br>public int getValue() { return value; }<br><br>public bolean isRest() { return false; }</th>
  </tr>
</table>


}

<table>
  <tr>
    <th>public clas Test { public static void main(String[] args) { System.out.println("EnumTest.FRI 的 value = " + EnumTest.FRI.getValue(); }</th>
  </tr>
</table>


}

输出结果：

<table>
  <tr>
    <th>EnumTest.FRI 的 value = 5</th>
  </tr>
</table>


# EnumSet，EnumMap 的应⽤

<table>
  <tr>
    <th>public clas Test { public static void main(String[] args) {<br><br>/ EnumSet的使⽤ EnumSet<EnumTest> wekSet = EnumSet.alOf(EnumTest.clas); for (EnumTest day : wekSet) { System.out.println(day); }<br><br>/ EnumMap的使⽤ EnumMap<EnumTest, String> wekMap = new EnumMap(EnumTest.clas); wekMap.put(EnumTest.MON, "星期⼀"); wekMap.put(EnumTest.TUE, "星期⼆");<br><br>/ . . for (Iterator<Entry<EnumTest, String> iter = wekMap.entrySet().iterator(); iter.hasNext();) { Entry<EnumTest, String> entry = iter.next(); System.out.println(entry.getKey().name() + ":" + entry.getValue(); } }</th>
  </tr>
</table>


}

## 原理分析

enum 的语法结构尽管和 class 的语法不⼀样，但是经过编译器编译之后产⽣的是⼀个class⽂ 件。该class⽂件经过反编译可以看到实际上是⽣成了⼀个类，该类继承了java.lang.Enum<E>。 EnumTest 经过反编译(javap com.hmw.test.EnumTest 命令)之后得到的内容如下：

<table>
  <tr>
    <th>public clas com.hmw.test.EnumTest extends java.lang.Enum{ public static final com.hmw.test.EnumTest MON; public static final com.hmw.test.EnumTest TUE; public static final com.hmw.test.EnumTest WED; public static final com.hmw.test.EnumTest THU; public static final com.hmw.test.EnumTest FRI; public static final com.hmw.test.EnumTest SAT; public static final com.hmw.test.EnumTest SUN; static {}; public int getValue(); public bolean isRest(); public static com.hmw.test.EnumTest[] values(); public static com.hmw.test.EnumTest valueOf(java.lang.String); com.hmw.test.EnumTest(java.lang.String, int, int, com.hmw.test.EnumTest);</th>
  </tr>
</table>


}

所以，实际上 enum 就是⼀个 class，只不过 java 编译器帮我们做了语法的解析和编译⽽已。

## 总结

可以把 enum 看成是⼀个普通的 class，它们都可以定义⼀些属性和⽅法，不同之处是：enum 不能使⽤ extends 关键字继承其他类，因为 enum 已经继承了 java.lang.Enum（java是单⼀继 承）。

