PC寄存器:每个拥有⼀个PC寄存器，在线程创建时创建 ⽅法区: 对应PermanetGeneration，可通过-X PermSize和-X MaxPermSize指定最⼤最⼩值 ⽅法区全局共享，保存load类的信息(名称、修饰符等) ⽅法区内存超出会抛出OutofMemory异常

# Java代码执⾏机制:

- 1.编译机制


⼀个java⽂件编译为.class⽂件的流程图。 Parsean Enter阶段: Parse:将代码字符串转换为token序列(com.sun.tools.javac.parser.Scanner),再将token转换为语法抽象树 (com.sun.tools.javac.parser.Parser)

Enter:将符号输⼊到符号表.内容包括确定超类型和接⼜，添加类默认构造器

Annotation阶段:

主要处理⽤户⾃定义的注解

Analyseand Generate阶段:

Analyse:将语法抽象树进⾏⼀系列分析，包括:检查变量使⽤前是否声明，检查类型匹配,有返回值的⽅ 法必须有返回值等等

Generate:⽣成.class⽂件(com.sun.tools.javac.jvm.Gen)

⽣成的class⽂件包括:

结构信息:class⽂件格式版本号及各部分数量,⼤⼩

元数据：类，⽗类，接⼜信息，⽅法声明，常量池

⽅法信息:对应java源码中语句、表达式部分

## 2.类加载机制(ClassLoader)

- 1.Load(装载): 找到⼆进制字节码并加载到JVM中

- 2.Link(链接): VerifyàPrepareàResolve 校验阶段: 校验⼆进制字节码格式，初始化装载类中的静态变量及解析类中调⽤的接⼜、类 如果格式不符合，会抛出VerifyError异常;校验过程中碰到要引⽤其他的接⼜和类，也要进⾏加载;如果 加载失败，抛出NoClassDefFoundError⼀样 Prepare准备阶段 完成校验后，JVM初始化类中的静态变量，并赋予默认值 最后，对类中的所有属性、⽅法进⾏验证，保证对应的属性、⽅法存在，否则抛出 NoSuchMethodError、NoSuchFiledError

- 3.Intialize(初始化): 要想初始化，必须通过校验及准备阶段 初始化就是执⾏类的static初始化代码、构造器代码、static属性的初始化。初始化被触发的⼏种情况:


- -|调⽤new
- -|反射调⽤类中的⽅法
- -|之类调⽤了初始化
- -|JVM启动过程中指定的初始化类


ClassLoader⽅⾯的常见异常 ClassNotFoundException: 很常见的异常,加载的类不在Classpath中就会抛出此异常。对于⾃定义的类加载器(继承ClassLoader)，需 要查看这个ClassLoader加载类的过程来分析类的加载路径 NoClassDefFoundError:

<table>
  <tr>
    <th>public class A{<br><br>public B b = new B(); }</th>
  </tr>
</table>


类A引⽤类B，若B不存在或ClassLoader没法加载B，则抛出此异常

- 3.LinkageError 重复加载类造成的异常，⾃定义ClassLoader情况下容易出现,因为此类已经在ClassLoader加载过了

- 4.ClassCastException 类型转换异常，较容易查找，⽐较难的是两个类A对象被不同的ClassLoader加载所引发的异常


- 3.类执⾏机制


编译执⾏ 反射执⾏

# JVM内存管理

## 1.内存空间

JVM内存结构图:

⽅法区:

对应Permanet Generation(持久代)，默认最⼩值为16M，最⼤值为64M，可通过-X PermSize和X MaxPermSize指定最⼤最⼩值

⽅法区全局共享，保存load类的信息(名称、修饰符等)，类中的静态变量,final常量,filed信息.⽅法信 息。

⽅法区内存超出会抛出OutofMemory异常

堆

存储对象实例及数组值。new创建对象会产⽣Heap内存，因此尽可能少new。堆内存通过-Xms和Xmx设置，-Xms为JVM启动时申请的最⼩堆内存,-Xmx为JVM可申请的最⼤堆内存，通常设成⼀样， 以免运⾏时JVM频繁调整Heap⼤⼩ 堆的分代管理:

1.

New Generation(新⽣代)

1.

Old Generation(旧⽣代)

本地⽅法栈: ⽤于⽀持native⽅法的执⾏,在SunJDK实现中本地⽅法栈和JVM⽅法栈是同⼀个 PC寄存器和JVM⽅法栈: 每个线程都会创建PC寄存器和JVM⽅法栈,JVM⽅法栈占⽤操作系统内存 JVM⽅法栈为线程私有，内存分配极为⾼效，JVM⽅法栈空间不⾜(可通过-Xs设置)，抛出 StackOverflowEror错误

内存分配

TLAB(Thread Local Alocation Bufer),是新⽣代的EdenSpace上为每个新的创建的线程分配的独⽴空 间，可通过-X TLABWasteTargetPercent来设置

内存回收

三种算法: 复制(Coping)：从根集合扫描，标记存活的对象，把存活的对象放⼊新的空间 当要回收空间中存活对象较少时，改算法效率⾼ 标记-清除(Mark-Swep):从根集合扫描，标记存活的对象，在扫描⼀遍，把未标记的对象清除 当要回收空间中存活对象较多时，该算法效率⾼,但是会产⽣内存碎⽚ 标记-压缩(Mark-Compact)：

收集器 JVM通过GC来回收堆和⽅法区中的内存

1.

引⽤计数收集器

通过记录对象是否被引⽤。SunJDK未采⽤这种⽅式

1.

跟踪收集器

采⽤集中式的管理⽅式,全局记录数据的引⽤状态

