Java 8 (⼜称为 jdk 1.8) 是 Java 语⾔开发的⼀个主要版本。 Oracle 公司于 2014 年 3 ⽉ 18 ⽇发布 Java 8 ，它⽀持函数式编程，新的 JavaScript 引擎，新的⽇期 API，新的Stream API 等。

# 新特性

Java8 新增了⾮常多的特性，我们主要讨论以下⼏个：

Lambda 表达式 Lambda允许把函数作为⼀个⽅法的参数（函数作为参数传递进⽅法中。 ⽅法引⽤ ⽅法引⽤提供了⾮常有⽤的语法，可以直接引⽤已有Java类或对象（实例）的⽅法或构造器。与lambda联合使⽤，⽅法 引⽤可以使语⾔的构造更紧凑简洁，减少冗余代码。 默认⽅法 默认⽅法就是⼀个在接⼝⾥⾯有了⼀个实现的⽅法。 新⼯具 新的编译⼯具，如：Nashorn引擎 jjs、 类依赖分析器jdeps。 Stream API 新添加的Stream API（java.util.stream） 把真正的函数式编程⻛格引⼊到Java中。 Date Time API 加强对⽇期与时间的处理。 Optional 类 Optional 类已经成为 Java 8 类库的⼀部分，⽤来解决空指针异常。 Nashorn, JavaScript 引擎 Java 8提供了⼀个新的Nashorn javascript引擎，它允许我们在JVM上运⾏特定的javascript应⽤。

What's New in JDK 8

更多的新特性可以参阅官⽹： 在关于 Java 8 ⽂章的实例，我们均使⽤ jdk 1.8 环境，你可以使⽤以下命令查看当前 jdk 的版本： $ java -version java version "1.8.0_31" Java(TM) SE Runtime Environment (build 1.8.0_31-b13) Java HotSpot(TM) 64-Bit Server VM (build 25.31-b07, mixed mode)

# 编程⻛格

Java 8 希望有⾃⼰的编程⻛格，并与 Java 7 区别开，以下实例展示了 Java 7 和 Java 8 的编程格式：

import java.util.Collections; import java.util.List; import java.util.ArrayList; import java.util.Comparator;

public class Java8Tester { public static void main(String args[]){

- List<String> names1 = new ArrayList<String>();

- names1.add("Google ");

- names1.add("Runoob ");

- names1.add("Taobao ");

- names1.add("Baidu ");

- names1.add("Sina ");

List<String> names2 = new ArrayList<String>();

- names2.add("Google ");


- names2.add("Runoob ");


- names2.add("Taobao ");


- names2.add("Baidu ");


- names2.add("Sina ");




Java8Tester tester = new Java8Tester();

- System.out.println("使⽤ Java 7 语法: ");

- tester.sortUsingJava7(names1);

- System.out.println(names1);

System.out.println("使⽤ Java 8 语法: ");

tester.sortUsingJava8(names2);

- System.out.println(names2);






}

- // 使⽤ java 7 排序

- private void sortUsingJava7(List<String> names){ Collections.sort(names, new Comparator<String>() {

@Override public int compare(String s1, String s2) {

return s1.compareTo(s2); }

}); }

// 使⽤ java 8 排序

- private void sortUsingJava8(List<String> names){ Collections.sort(names, (s1, s2) -> s1.compareTo(s2));




}

} 执⾏以上脚本，输出结果为：

$ javac Java8Tester.java $ java Java8Tester

- 使⽤ Java 7 语法: [Baidu , Google , Runoob , Sina , Taobao ]

- 使⽤ Java 8 语法: [Baidu , Google , Runoob , Sina , Taobao ] 接下来我们将详细为⼤家简介 Java 8 的新特性：

<table>
  <tr>
    <th>序号</th>
    <th>特性</th>
  </tr>
  <tr>
    <td>1</td>
    <td>Lambda 表达式</td>
  </tr>
  <tr>
    <td>2</td>
    <td>⽅法引⽤</td>
  </tr>
  <tr>
    <td>3</td>
    <td>函数式接⼝</td>
  </tr>
  <tr>
    <td>4</td>
    <td>默认⽅法</td>
  </tr>
  <tr>
    <td>5</td>
    <td>Stream</td>
  </tr>
  <tr>
    <td>6</td>
    <td>Optional 类</td>
  </tr>
  <tr>
    <td>7</td>
    <td>Nashorn, JavaScript 引擎</td>
  </tr>
  <tr>
    <td>8</td>
    <td>新的⽇期时间 API</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
  </tr>
</table>


- 9 Base64


