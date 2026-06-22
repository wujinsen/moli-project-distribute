## class⽂件简介及加载

编译器编译好Java⽂件之后，产⽣.class ⽂件在磁盘中。这种class⽂件是⼆进制⽂件，内容是只有JVM虚拟机能够识别的 机器码。JVM虚拟机读取字节码⽂件，取出⼆进制数据，加载到内存中，解析.class ⽂件内的信息，⽣成对应的 Class对象:

Java

![image 1](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile1.png>)

class字节码⽂件是根据JVM虚拟机规范中规定的字节码组织规则⽣成的、具体class⽂件是怎样组织类信息的，可以参考 此博 ⽂： ⽂件格式系列。或者是 。

深⼊理解Java Class Java虚拟机规范

下⾯通过⼀段代码演示⼿动加载 class⽂件字节码到系统内，转换成class对象，然后再实例化的过程：

- a. 定义⼀个 Programmer类： [java]

- b. ⾃定义⼀个类加载器： [java]


view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.


package samples; /**

- * 程序猿类

- * @author louluan

- */


public class Programmer {

public void code() {

System.out.println("I'm a Programmer,Just Coding....."); }

}

view plain copy print?

- 1.
- 2.


package samples; /**

- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.


- * ⾃定义⼀个类加载器，⽤于将字节码转换为class对象

- * @author louluan

- */


public class MyClassLoader extends ClassLoader {

public Class<?> defineMyClass( byte[] b, int off, int len) {

return super.defineClass(b, off, len); }

}

- c. 然后编译成Programmer.class⽂件，在程序中读取字节码，然后转换成相应的class对象，再实例化： [java]


view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.


package samples;

import java.io.File; import java.io.FileInputStream; import java.io.FileNotFoundException; import java.io.IOException; import java.io.InputStream; import java.net.URL;

### public class MyTest {

public static void main(String[] args) throws IOException { //读取本地的class⽂件内的字节码，转换成字节码数组 File file = new File("."); InputStream input = new FileInputStream(file.getCanonicalPath()+"\\bin\\samples

\\Programmer.class");

byte[] result = new byte[1024];

int count = input.read(result); // 使⽤⾃定义的类加载器将 byte字节码数组转换为对应的class对象 MyClassLoader loader = new MyClassLoader(); Class clazz = loader.defineMyClass( result, 0, count); //测试加载是否成功，打印class 对象的名称 System.out.println(clazz.getCanonicalName());

//实例化⼀个Programmer对象 Object o= clazz.newInstance(); try {

//调⽤Programmer的code⽅法 clazz.getMethod("code", null).invoke(o, null); } catch (IllegalArgumentException | InvocationTargetException

| NoSuchMethodException | SecurityException e) { e.printStackTrace();

} }

}

以上代码演示了，通过字节码加载成class 对象的能⼒，下⾯看⼀下在代码中如何⽣成class⽂件的字节码。

在运⾏期的代码中⽣成⼆进制字节码

由于JVM通过字节码的⼆进制信息加载类的，那么，如果我们在运⾏期系统中，遵循Java编译系 统组织.class⽂件的格式和结构，⽣成相应的⼆进制数据，然后再把这个⼆进制数据加载转换成对 应的类，这样，就完成了在代码中，动态创建⼀个类的能⼒了。

![image 2](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile2.png>)

在运⾏时期可以按照Java虚拟机规范对class⽂件的组织规则⽣成对应的⼆进制字节码。当前有很多开源框架可以完成这些功能，如 ASM，Javassist。

## Java字节码⽣成开源框架介绍--ASM：

ASM 是⼀个 Java 字节码操控框架。它能够以⼆进制形式修改已有类或者动态⽣成类。ASM 可以直接产⽣⼆进制 class ⽂件，也可 以在类被加载⼊ Java 虚拟机之前动态改变类⾏为。ASM 从类⽂件中读⼊信息后，能够改变类⾏为，分析类信息，甚⾄能够根据⽤ 户要求⽣成新类。 不过ASM在创建class字节码的过程中，操纵的级别是底层JVM的汇编指令级别，这要求ASM使⽤者要对class组织结构和JVM汇 编指令有⼀定的了解。 下⾯通过ASM ⽣成下⾯类Programmer的class字节码：

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


package com.samples; import java.io.PrintStream;

public class Programmer {

public void code() {

System.out.println("I'm a Programmer,Just Coding....."); }

}

使⽤ASM框架提供了ClassWriter 接⼝，通过访问者模式进⾏动态创建class字节码，看下⾯的例⼦：

[java] view plain copy

print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.


package samples;

import java.io.File; import java.io.FileOutputStream; import java.io.IOException;

import org.objectweb.asm.ClassWriter; import org.objectweb.asm.MethodVisitor; import org.objectweb.asm.Opcodes; public class MyGenerator {

public static void main(String[] args) throws IOException {

System.out.println(); ClassWriter classWriter = new ClassWriter(0); // 通过visit⽅法确定类的头部信息 classWriter.visit(Opcodes.V1_7,// java版本

Opcodes.ACC_PUBLIC,// 类修饰符 "Programmer", // 类的全限定名 null, "java/lang/Object", null);

//创建构造函数 MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "

()V", null, null); mv.visitCode(); mv.visitVarInsn(Opcodes.ALOAD, 0); mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>","()V"); mv.visitInsn(Opcodes.RETURN); mv.visitMaxs(1, 1); mv.visitEnd();

// 定义code⽅法 MethodVisitor methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "code"

, "()V",

null, null); methodVisitor.visitCode(); methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",

"Ljava/io/PrintStream;"); methodVisitor.visitLdcInsn("I'm a Programmer,Just Coding....."); methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "pri

ntln",

"(Ljava/lang/String;)V"); methodVisitor.visitInsn(Opcodes.RETURN); methodVisitor.visitMaxs(2, 2); methodVisitor.visitEnd(); classWriter.visitEnd(); // 使classWriter类已经完成 // 将classWriter转换成字节数组写到⽂件⾥⾯去 byte[] data = classWriter.toByteArray(); File file = new File("D://Programmer.class"); FileOutputStream fout = new FileOutputStream(file); fout.write(data); fout.close();

- 51.
- 52.


} }

上述的代码执⾏过后，⽤Java反编译⼯具（如JD_GUI）打开D盘下⽣成的Programmer.class，可以看到以下信息：

![image 3](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile3.png>)

再⽤上⾯我们定义的类加载器将这个class⽂件加载到内存中，然后 创建class对象，并且实例化⼀个对象，调⽤code⽅法， 会看到下⾯的结果：

![image 4](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile4.png>)

以上表明：在代码⾥⽣成字节码，并动态地加载成class对象、创建实例是完全可以实现的。

Java字节码⽣成开源框架介绍--Javassist： Javassist是⼀个开源的分析、编辑和创建Java字节码的类库。是由东京⼯业⼤学的数学和计算机科学系的 Shigeru Chiba （千叶 滋）所创建的。它已加⼊了开放源代码JBoss 应⽤服务器项⽬,通过使⽤Javassist对字节码操作为JBoss实现动态AOP框架。 javassist是 的⼀个⼦项⽬，其主要的优点，在于简单，⽽且快速。直接使⽤java编码的形式，⽽不需要了解 指令，就能 动态改变类的结构，或者动态⽣成类。 下⾯通过Javassist创建上述的Programmer类：

jboss 虚拟机

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.


import javassist.ClassPool; import javassist.CtClass; import javassist.CtMethod; import javassist.CtNewMethod;

public class MyGenerator {

public static void main(String[] args) throws Exception { ClassPool pool = ClassPool.getDefault(); //创建Programmer类 CtClass cc= pool.makeClass("com.samples.Programmer"); //定义code⽅法 CtMethod method = CtNewMethod.make("public void code(){}", cc); //插⼊⽅法代码 method.insertBefore("System.out.println(\"I'm a Programmer,Just Coding.....\");"

);

cc.addMethod(method);

- 17.
- 18.
- 19.
- 20.


//保存⽣成的字节码 cc.writeFile("d://temp");

} }

通过JD-gui反编译⼯具打开Programmer.class 可以看到以下代码：

![image 5](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile5.png>)

## 代理的基本构成：

代理模式上，基本上有Subject⻆⾊，RealSubject⻆⾊，Proxy⻆⾊。其中：Subject⻆⾊负责定义RealSubject和Proxy⻆⾊应 该实现的接⼝；RealSubject⻆⾊⽤来真正完成业务服务功能；Proxy⻆⾊负责将⾃身的Request请求，调⽤realsubject 对应的 request功能来实现业务功能，⾃⼰不真正做业务。

![image 6](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile6.png>)

上⾯的这幅代理结构图是典型的静态的代理模式：

当在代码阶段规定这种代理关系，Proxy类通过编译器编译成class⽂件，当系统运⾏时，此class已经存在了。这种静态的代 理模式固然在访问⽆法访问的资源，增强现有的接⼝业务功能⽅⾯有很⼤的优点，但是⼤量使⽤这种静态代理，会使我们系统内的 类的规模增⼤，并且不易维护；并且由于Proxy和RealSubject的功能 本质上是相同的，Proxy只是起到了中介的作⽤，这种代理 在系统中的存在，导致系统结构⽐较臃肿和松散。

为了解决这个问题，就有了动态地创建Proxy的想法：在运⾏状态中，需要代理的地⽅，根据Subject 和RealSubject，动态地 创建⼀个Proxy，⽤完之后，就会销毁，这样就可以避免了Proxy ⻆⾊的class在系统中冗杂的问题了。

下⾯以⼀个代理模式实例阐述这⼀问题：

将⻋站的售票服务抽象出⼀个接⼝TicketService,包含问询，卖票，退票功能，⻋站类Station实现了TicketService接⼝，⻋票代售 点StationProxy则实现了代理⻆⾊的功能，类图如下所示。

![image 7](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile7.png>)

对应的静态的代理模式代码如下所示：

[java] view plain copy

print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.


package com.foo.proxy;

/**

- * 售票服务接⼝实现类，⻋站

- * @author louluan

- */


### public class Station implements TicketService {

@Override public void sellTicket() {

System.out.println("\n\t售票.....\n"); }

@Override public void inquire() {

System.out.println("\n\t问询。。。。\n"); }

@Override public void withdraw() {

System.out.println("\n\t退票......\n"); }

}

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.


package com.foo.proxy; /**

- * 售票服务接⼝

- * @author louluan

- */


### public interface TicketService {

//售票 public void sellTicket();

//问询 public void inquire();

//退票 public void withdraw();

}

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.


package com.foo.proxy;

/**

- * ⻋票代售点

- * @author louluan

*

- */


public class StationProxy implements TicketService {

private Station station;

public StationProxy(Station station){

this.station = station; }

@Override public void sellTicket() {

- // 1.做真正业务前，提示信息 this.showAlertInfo("××××您正在使⽤⻋票代售点进⾏购票，每张票将会收取5元⼿续费！××××");

- // 2.调⽤真实业务逻辑 station.sellTicket();

- // 3.后处理 this.takeHandlingFee(); this.showAlertInfo("××××欢迎您的光临，再⻅！××××\n");


}

@Override public void inquire() {

// 1做真正业务前，提示信息

- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.


this.showAlertInfo("××××欢迎光临本代售点，问询服务不会收取任何费⽤，本问询信息仅供参考，具

体信息以⻋站真实数据为准！××××"); // 2.调⽤真实逻辑 station.inquire(); // 3。后处理 this.showAlertInfo("××××欢迎您的光临，再⻅！××××\n");

}

@Override public void withdraw() {

// 1。真正业务前处理 this.showAlertInfo("××××欢迎光临本代售点，退票除了扣除票额的20%外，本代理处额外加收2元⼿续

费！××××");

- // 2.调⽤真正业务逻辑 station.withdraw();

- // 3.后处理 this.takeHandlingFee();


}

/*

- * 展示额外信息

- */


private void showAlertInfo(String info) {

System.out.println(info); }

/*

- * 收取⼿续费

- */


private void takeHandlingFee() {

System.out.println("收取⼿续费，打印发票。。。。。\n"); }

}

由于我们现在不希望静态地有StationProxy类存在，希望在代码中，动态⽣成器⼆进制代码，加载进来。为此，使⽤Javassist开源 框架，在代码中动态地⽣成StationProxy的字节码：

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.


package com.foo.proxy;

import java.lang.reflect.Constructor;

import javassist.*; public class Test {

public static void main(String[] args) throws Exception {

createProxy(); }

/*

* ⼿动创建字节码

- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.


*/ private static void createProxy() throws Exception {

ClassPool pool = ClassPool.getDefault();

CtClass cc = pool.makeClass("com.foo.proxy.StationProxy");

//设置接⼝ CtClass interface1 = pool.get("com.foo.proxy.TicketService"); cc.setInterfaces(new CtClass[]{interface1});

//设置Field CtField field = CtField.make("private com.foo.proxy.Station station;", cc);

cc.addField(field);

CtClass stationClass = pool.get("com.foo.proxy.Station"); CtClass[] arrays = new CtClass[]{stationClass}; CtConstructor ctc = CtNewConstructor.make(arrays,null,CtNewConstructor.PASS_NONE

,null,null, cc); //设置构造函数内部信息 ctc.setBody("{this.station=$1;}"); cc.addConstructor(ctc);

//创建收取⼿续 takeHandlingFee⽅法 CtMethod takeHandlingFee = CtMethod.make("private void takeHandlingFee() {}", cc

);

takeHandlingFee.setBody("System.out.println(\"收取⼿续费，打印发票。。。。。\");"); cc.addMethod(takeHandlingFee);

//创建showAlertInfo ⽅法 CtMethod showInfo = CtMethod.make("private void showAlertInfo(String info) {}",

cc);

showInfo.setBody("System.out.println($1);"); cc.addMethod(showInfo);

//sellTicket CtMethod sellTicket = CtMethod.make("public void sellTicket(){}", cc); sellTicket.setBody("{this.showAlertInfo(\"××××您正在使⽤⻋票代售点进⾏购票，每张票将会

收取5元⼿续费！××××\");"

+ "station.sellTicket();"

+ "this.takeHandlingFee();"

+ "this.showAlertInfo(\"××××欢迎您的光临，再⻅！××××\");}"); cc.addMethod(sellTicket);

//添加inquire⽅法 CtMethod inquire = CtMethod.make("public void inquire() {}", cc); inquire.setBody("{this.showAlertInfo(\"××××欢迎光临本代售点，问询服务不会收取任何费⽤，

本问询信息仅供参考，具体信息以⻋站真实数据为准！××××\");"

+ "station.inquire();"

+ "this.showAlertInfo(\"××××欢迎您的光临，再⻅！××××\");}" ); cc.addMethod(inquire);

//添加widthraw⽅法

- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.


CtMethod withdraw = CtMethod.make("public void withdraw() {}", cc); withdraw.setBody("{this.showAlertInfo(\"××××欢迎光临本代售点，退票除了扣除票额的20%外，

本代理处额外加收2元⼿续费！××××\");"

+ "station.withdraw();"

+ "this.takeHandlingFee();}" );

cc.addMethod(withdraw);

//获取动态⽣成的class Class c = cc.toClass(); //获取构造器 Constructor constructor= c.getConstructor(Station.class); //通过构造器实例化 TicketService o = (TicketService)constructor.newInstance(new Station()); o.inquire();

cc.writeFile("D://test"); }

}

上述代码执⾏过后，会产⽣StationProxy的字节码，并且⽤⽣成字节码加载如内存创建对象，调⽤inquire()⽅法，会得到以下结果：

![image 8](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile8.png>)

通过上⾯动态⽣成的代码，我们发现，其实现相当地麻烦在创造的过程中，含有太多的业务代码。我们使⽤上述创建Proxy代理类 的⽅式的初衷是减少系统代码的冗杂度，但是上述做法却增加了在动态创建代理类过程中的复杂度：⼿动地创建了太多的业务代 码，并且封装性也不够，完全不具有可拓展性和通⽤性。如果某个代理类的⼀些业务逻辑⾮常复杂，上述的动态创建代理的⽅式是 ⾮常不可取的！

# InvocationHandler⻆⾊的由来

仔细思考代理模式中的代理Proxy⻆⾊。Proxy⻆⾊在执⾏代理业务的时候，⽆⾮是在调⽤真正业务之前或者之后做⼀些“额外”业 务。

![image 9](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile9.png>)

有上图可以看出，代理类处理的逻辑很简单：在调⽤某个⽅法前及⽅法后做⼀些额外的业务。换⼀种思路就是：在触发 （invoke）真实⻆⾊的⽅法之前或者之后做⼀些额外的业务。那么，为了构造出具有通⽤性和简单性的代理类，可以将所有的触发 真实⻆⾊动作交给⼀个触发的管理器，让这个管理器统⼀地管理触发。这种管理器就是Invocation Handler。 动态代理模式的结构跟上⾯的静态代理模式稍微有所不同，多引⼊了⼀个InvocationHandler⻆⾊。 先解释⼀下InvocationHandler的作⽤：

在静态代理中，代理Proxy中的⽅法，都指定了调⽤了特定的realSubject中的对应的⽅法： 在上⾯的静态代理模式下，Proxy所做的事情，⽆⾮是调⽤在不同的request时，调⽤触发realSubject对应的⽅法；更抽象点 看，Proxy所作的事情；在Java中 ⽅法（Method）也是作为⼀个对象来看待了， 动态代理⼯作的基本模式就是将⾃⼰的⽅法功能的实现交给 InvocationHandler⻆⾊，外界对Proxy⻆⾊中的每⼀个⽅法的调 ⽤，Proxy⻆⾊都会交给InvocationHandler来处理，⽽InvocationHandler则调⽤具体对象⻆⾊的⽅法。如下图所示：

![image 10](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile10.png>)

在这种模式之中：代理Proxy 和RealSubject 应该实现相同的功能，这⼀点相当重要。（我这⾥说的功能，可以理解为某个类的 public⽅法） 在⾯向对象的编程之中，如果我们想要约定Proxy 和RealSubject可以实现相同的功能，有两种⽅式：

- a.⼀个⽐较直观的⽅式，就是定义⼀个功能接⼝，然后让Proxy 和RealSubject来实现这个接⼝。

- b.还有⽐较隐晦的⽅式，就是通过继承。因为如果Proxy 继承⾃RealSubject，这样Proxy则拥有了RealSubject的功能，


Proxy还可以通过重写RealSubject中的⽅法，来实现多态。 其中JDK中提供的创建动态代理的机制，是以a 这种思路设计的，⽽cglib 则是以b思路设计的。

## JDK的动态代理创建机制----通过接⼝

⽐如现在想为RealSubject这个类创建⼀个动态代理对象，JDK主要会做以下⼯作：

- 1. 获取 RealSubject上的所有接⼝列表；

- 2. 确定要⽣成的代理类的类名，默认为：com.sun.proxy.$ProxyXXXX ；

- 3. 根据需要实现的接⼝信息，在代码中动态创建 该Proxy类的字节码；

- 4 . 将对应的字节码转换为对应的class 对象；

- 5. 创建InvocationHandler 实例handler，⽤来处理Proxy所有⽅法调⽤；

- 6. Proxy 的class对象 以创建的handler对象为参数，实例化⼀个proxy对象


JDK通过 java.lang.reflect.Proxy包来⽀持动态代理，⼀般情况下，我们使⽤下⾯的newProxyInstance⽅法

<table>
  <tr>
    <th>static Object</th>
    <th>newProxyInstance(ClasLoader loader,Clas<?>[] interfaces,InvocationHandler h)<br><br>返回⼀个指定接⼝的代理类实例，该接⼝可以将⽅法调⽤指派到 指定的调⽤处理程序。</th>
  </tr>
</table>


⽽对于InvocationHandler，我们需要实现下列的invoke⽅法： 在调⽤代理对象中的每⼀个⽅法时，在代码内部，都是直接调⽤了InvocationHandler 的invoke⽅法，⽽invoke⽅法根据代理类传递 给⾃⼰的method参数来区分是什么⽅法。

<table>
  <tr>
    <th>Object</th>
    <th>invoke(Object proxy,Method method,Object[] args) 在代理实例上处理⽅法调⽤并返回结果。</th>
  </tr>
</table>


讲的有点抽象，下⾯通过⼀个实例来演示⼀下吧：

JDK动态代理示例

现在定义两个接⼝Vehicle和Rechargable，Vehicle表示交通⼯具类，有drive()⽅法；Rechargable接⼝表示可充电的（⼯ 具），有recharge() ⽅法；

定义⼀个实现两个接⼝的类ElectricCar，类图如下：

![image 11](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile11.png>)

通过下⾯的代码⽚段，来为ElectricCar创建动态代理类：

[java]

view plain copy print?

- 1.
- 2.
- 3.


package com.foo.proxy;

import java.lang.reflect.InvocationHandler;

- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.


import java.lang.reflect.Proxy;

### public class Test {

public static void main(String[] args) {

ElectricCar car = new ElectricCar();

- // 1.获取对应的ClassLoader ClassLoader classLoader = car.getClass().getClassLoader();

- // 2.获取ElectricCar 所实现的所有接⼝ Class[] interfaces = car.getClass().getInterfaces();

- // 3.设置⼀个来⾃代理传过来的⽅法调⽤请求处理器，处理所有的代理对象上的⽅法调⽤ InvocationHandler handler = new InvocationHandlerImpl(car); /*


4.根据上⾯提供的信息，创建代理对象 在这个过程中，

a.JDK会通过根据传⼊的参数信息动态地在内存中创建和.class ⽂件等同的字节码 b.然后根据相应的字节码转换成对应的class，

c.然后调⽤newInstance()创建实例

*/ Object o = Proxy.newProxyInstance(classLoader, interfaces, handler); Vehicle vehicle = (Vehicle) o; vehicle.drive(); Rechargable rechargeable = (Rechargable) o; rechargeable.recharge();

} }

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


package com.foo.proxy; /**

- * 交通⼯具接⼝

- * @author louluan

- */


public interface Vehicle {

public void drive(); }

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


package com.foo.proxy; /**

- * 可充电设备接⼝

- * @author louluan

- */


### public interface Rechargable {

public void recharge(); }

[java]

view plain copy print?

- 1.
- 2.


package com.foo.proxy; /**

- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.


- * 电能⻋类，实现Rechargable，Vehicle接⼝

- * @author louluan

- */


public class ElectricCar implements Rechargable, Vehicle {

@Override public void drive() {

System.out.println("Electric Car is Moving silently..."); }

@Override public void recharge() {

System.out.println("Electric Car is Recharging..."); }

}

#### [java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.


package com.foo.proxy;

import java.lang.reflect.InvocationHandler; import java.lang.reflect.Method;

public class InvocationHandlerImpl implements InvocationHandler {

private ElectricCar car;

public InvocationHandlerImpl(ElectricCar car) {

this.car=car; }

@Override public Object invoke(Object paramObject, Method paramMethod,

Object[] paramArrayOfObject) throws Throwable { System.out.println("You are going to invoke "+paramMethod.getName()+" ..."); paramMethod.invoke(car, null); System.out.println(paramMethod.getName()+" invocation Has Been finished..."); return null;

}

}

来看⼀下代码执⾏后的结果：

![image 12](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile12.png>)

⽣成动态代理类的字节码并且保存到硬盘中：

JDK提供了sun.misc.ProxyGenerator.generateProxyClass(String proxyName,class[] interfaces) 底层⽅法来产⽣动态代 理类的字节码： 下⾯定义了⼀个⼯具类，⽤来将⽣成的动态代理类保存到硬盘中：

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.


package com.foo.proxy;

import java.io.FileOutputStream; import java.io.IOException; import java.lang.reflect.Proxy; import sun.misc.ProxyGenerator;

### public class ProxyUtils {

/*

- * 将根据类信息 动态⽣成的⼆进制字节码保存到硬盘中，

- * 默认的是clazz⽬录下
- * params :clazz 需要⽣成动态代理类的类

- * proxyName : 为动态⽣成的代理类的名称

- */


public static void generateClassFile(Class clazz,String proxyName) {

//根据类信息和提供的代理类名称，⽣成字节码

byte[] classFile = ProxyGenerator.generateProxyClass(proxyName, clazz.ge

tInterfaces()); String paths = clazz.getResource(".").getPath(); System.out.println(paths); FileOutputStream out = null;

try { //保留到硬盘中 out = new FileOutputStream(paths+proxyName+".class"); out.write(classFile); out.flush();

} catch (Exception e) {

e.printStackTrace(); } finally {

### try {

out.close(); } catch (IOException e) {

e.printStackTrace(); }

} }

}

现在我们想将⽣成的代理类起名为“ElectricCarProxy”，并保存在硬盘，应该使⽤以下语句：

[java]

view plain copy print?

1.

ProxyUtils.generateClassFile(car.getClass(), "ElectricCarProxy");

这样将在ElectricCar.class 同级⽬录下产⽣ ElectricCarProxy.class⽂件。⽤反编译⼯具如jd-gui.exe 打开，将会看到以下信 息：

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.


import com.foo.proxy.Rechargable; import com.foo.proxy.Vehicle; import java.lang.reflect.InvocationHandler; import java.lang.reflect.Method; import java.lang.reflect.Proxy; import java.lang.reflect.UndeclaredThrowableException; /**

⽣成的动态代理类的组织模式是继承Proxy类，然后实现需要实现代理的类上的所有接⼝，⽽在实现的过程中，则是通 过将所有的⽅法都交给了InvocationHandler来处理

*/

### public final class ElectricCarProxy extends Proxy

implements Rechargable, Vehicle {

private static Method m1; private static Method m3; private static Method m4; private static Method m0; private static Method m2;

public ElectricCarProxy(InvocationHandler paramInvocationHandler)

### throws {

super(paramInvocationHandler); }

public final boolean equals(Object paramObject)

### throws {

try { // ⽅法功能实现交给InvocationHandler处理

return ((Boolean)this.h.invoke(this, m1, new Object[] { paramObject })).booleanVal

ue(); } catch (Error|RuntimeException localError) {

throw localError;

} catch (Throwable localThrowable) {

throw new UndeclaredThrowableException(localThrowable); }

}

public final void recharge()

throws {

try {

// ⽅法功能实现交给InvocationHandler处理

### this.h.invoke(this, m3, null); return;

} catch (Error|RuntimeException localError)

- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.
- 92.
- 93.
- 94.
- 95.
- 96.
- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.
- 105.
- 106.
- 107.
- 108.


{

throw localError;

} catch (Throwable localThrowable) {

throw new UndeclaredThrowableException(localThrowable); }

}

public final void drive()

throws {

try {

// ⽅法功能实现交给InvocationHandler处理

### this.h.invoke(this, m4, null); return;

} catch (Error|RuntimeException localError) {

throw localError;

} catch (Throwable localThrowable) {

throw new UndeclaredThrowableException(localThrowable); }

}

public final int hashCode()

throws {

try {

// ⽅法功能实现交给InvocationHandler处理

return ((Integer)this.h.invoke(this, m0, null)).intValue();

} catch (Error|RuntimeException localError) {

throw localError;

} catch (Throwable localThrowable) {

throw new UndeclaredThrowableException(localThrowable); }

}

public final String toString()

throws {

try {

- 109.
- 110.
- 111.
- 112.
- 113.
- 114.
- 115.
- 116.
- 117.
- 118.
- 119.
- 120.
- 121.
- 122.
- 123.
- 124.
- 125.
- 126.
- 127.
- 128.
- 129.
- 130.
- 131.
- 132.
- 133.
- 134.
- 135.
- 136.
- 137.
- 138.
- 139.
- 140.
- 141.
- 142.
- 143.


// ⽅法功能实现交给InvocationHandler处理

return (String)this.h.invoke(this, m2, null);

} catch (Error|RuntimeException localError) {

throw localError;

} catch (Throwable localThrowable) {

throw new UndeclaredThrowableException(localThrowable); }

}

### static {

try { //为每⼀个需要⽅法对象，当调⽤相应的⽅法时，分别将⽅法对象作为参数传递给InvocationHandler处

理

m1 = Class.forName("java.lang.Object").getMethod("equals", new Class[] { Class.fo rName("java.lang.Object") });

- m3 = Class.forName("com.foo.proxy.Rechargable").getMethod("recharge", new Class[0 ]);

- m4 = Class.forName("com.foo.proxy.Vehicle").getMethod("drive", new Class[0]); m0 = Class.forName("java.lang.Object").getMethod("hashCode", new Class[0]); m2 = Class.forName("java.lang.Object").getMethod("toString", new Class[0]); return;


} catch (NoSuchMethodException localNoSuchMethodException) {

throw new NoSuchMethodError(localNoSuchMethodException.getMessage());

} catch (ClassNotFoundException localClassNotFoundException) {

throw new NoClassDefFoundError(localClassNotFoundException.getMessage()); }

} }

仔细观察可以看出⽣成的动态代理类有以下特点:

- 1.继承⾃ java.lang.reflect.Proxy，实现了 Rechargable,Vehicle 这两个ElectricCar实现的接⼝；

- 2.类中的所有⽅法都是final 的；

- 3.所有的⽅法功能的实现都统⼀调⽤了InvocationHandler的invoke()⽅法。


![image 13](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile13.png>)

## cglib ⽣成动态代理类的机制----通过类继承：

JDK中提供的⽣成动态代理类的机制有个鲜明的特点是： 某个类必须有实现的接⼝，⽽⽣成的代理类也只能代理某个类接⼝定 义的⽅法，⽐如：如果上⾯例⼦的ElectricCar实现了继承⾃两个接⼝的⽅法外，另外实现了⽅法bee() ,则在产⽣的动态代理类中不 会有这个⽅法了！更极端的情况是：如果某个类没有实现接⼝，那么这个类就不能同JDK产⽣动态代理了！

幸好我们有cglib。“CGLIB（Code Generation Library），是⼀个强⼤的，⾼性能，⾼质量的Code⽣成类库，它可以在运⾏期扩 展Java类与实现Java接⼝。” cglib 创建某个类A的动态代理类的模式是：

- 1. 查找A上的所有⾮final 的public类型的⽅法定义；

- 2. 将这些⽅法的定义转换成字节码；

- 3. 将组成的字节码转换成相应的代理的class对象；

- 4. 实现 MethodInterceptor接⼝，⽤来处理 对代理类上所有⽅法的请求（这个接⼝和JDK动态代理InvocationHandler的功能 和⻆⾊是⼀样的）


⼀个有趣的例⼦：定义⼀个Programmer类，⼀个Hacker类

[java] view plain copy

print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.


package samples; /**

- * 程序猿类

- * @author louluan

- */


public class Programmer {

### public void code() {

System.out.println("I'm a Programmer,Just Coding....."); }

}

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.


package samples;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor; import net.sf.cglib.proxy.MethodProxy; /*

- * 实现了⽅法拦截器接⼝

- */


public class Hacker implements MethodInterceptor { @Override public Object intercept(Object obj, Method method, Object[] args,

MethodProxy proxy) throws Throwable {

System.out.println("**** I am a hacker,Let's see what the poor programmer is doing N ow...");

proxy.invokeSuper(obj, args); System.out.println("**** Oh,what a poor programmer....."); return null;

}

}

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.


package samples;

import net.sf.cglib.proxy.Enhancer;

### public class Test {

public static void main(String[] args) {

Programmer progammer = new Programmer();

Hacker hacker = new Hacker(); //cglib 中加强器，⽤来创建动态代理 Enhancer enhancer = new Enhancer();

//设置要创建动态代理的类 enhancer.setSuperclass(progammer.getClass()); // 设置回调，这⾥相当于是对于代理类上所有⽅法的调⽤，都会调⽤CallBack，⽽Callback则需要实

⾏intercept()⽅法进⾏拦截 enhancer.setCallback(hacker); Programmer proxy =(Programmer)enhancer.create(); proxy.code();

} }

程序执⾏结果：

![image 14](<Java动态代理机制详解（JDK 和CGLIB，Javassist，ASM）.note_images/imageFile14.png>)

让我们看看通过cglib⽣成的class⽂件内容：

[java]

view plain copy print?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.


package samples;

import java.lang.reflect.Method; import net.sf.cglib.core.ReflectUtils; import net.sf.cglib.core.Signature; import net.sf.cglib.proxy.Callback; import net.sf.cglib.proxy.Factory; import net.sf.cglib.proxy.MethodInterceptor; import net.sf.cglib.proxy.MethodProxy;

public class Programmer

EnhancerByCGLIB

fa7aa2cd extends Programmer

implements Factory {

//......省略

private MethodInterceptor CGLIB$CALLBACK_0; // Enchaner传⼊的methodInterceptor

// ....省略 public final void code() {

MethodInterceptor tmp4_1 = this.CGLIB$CALLBACK_0; if (tmp4_1 == null) {

tmp4_1; CGLIB$BIND_CALLBACKS(this);//若callback 不为空，则调⽤methodInterceptor 的intercept()⽅

法

} if (this.CGLIB$CALLBACK_0 != null)

return; //如果没有设置callback回调函数，则默认执⾏⽗类的⽅法 super.code();

}

//....后续省略 }

