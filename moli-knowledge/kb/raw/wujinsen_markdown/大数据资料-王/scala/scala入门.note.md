scala

- 1. scala的由来

- 2. scala基础

- 2.1. scala环境变量安装

- 2.2.认识scala

- 2.3.安装scala-eclipse


- 2.3.1. 安装


scala是⼀门多范式的编程语⾔，⼀种类似java的编程语⾔[2] ，设计初衷是要集成⾯向对象编程和函数 式编程的各种特性。 java和c+的进化速度已经⼤不如从前，那么乐于使⽤更现代的语⾔特性的程序员们正在将眼光移向他 处。scala是⼀个很有吸引⼒的选择；事实上，在我看来，对于想要突破和超越java或者c+的程序员 ⽽⾔，scala是最具吸引⼒的⼀个。scala的语法⼗分简洁，相⽐java的样板代码，scala让⼈⽿⽬⼀新。 scala运⾏于java虚拟机之上，让我们可以使⽤现成的海量类库和⼯具。他在拥抱函数式编程的同时， 并没有⾮其⾯向对象，使你得以逐步了解和学习⼀种全新的编程范式。scala解释器可以让你快速运⾏ 试验代码，这使得学习scala的过程颇为轻松惬意。最后，同时也是很重要的⼀点，scala是静态类型 的，编译器能够帮助我们找到⼤部分错误，避免时间的浪费。

见安装⽂档

打开cmd，输⼊scala

将scala-SDK-3.0.1-vfinal-2.10-win32.win32.x86_64.zip解压到任意⽬录即可

- 2.3.2. 打开


- 2.3.3. 创建scala⼯程

- 2.3.4. 创建scala包

- 2.3.5. 创建scala类


点击finish

## 2.4.创建变量

Scala 定义了两种类型的变量 val 和 var ，val 类似于Java中的final 变量，⼀旦初始化之后，不可以重 新赋值（我们可以称它为常变量）。⽽var 类似于⼀般的⾮final变量。可以任意重新赋值。

### 2.4.1. 不指定类型

<table>
  <tr>
    <th>package org.apache.first object test {<br><br>def ain(args: Aray[String]): Unit = { int = 1 lmystring ="string" int2 = 1 varmystring2 ="string" intn int) rintn string) intn int2)<br><br>println(mystring2) }</th>
  </tr>
</table>


}

你可能注意到了，在变量声明和赋值语句之后，我们并没有使⽤分号。在scala中，仅当同⼀⾏代码中 存在多条语句时采⽤分号隔开，但是其余情况你也可以像java那样使⽤分号，也不会报错。

- 1.toString(); /产⽣字符串“1” 或者，更有意思的是，你可以：

1.to(10) /产⽣Range(1, 2, 3, 4,5, 6, 7, 8, 9, 10)，类似数组 并通过1.to(10)(5)获取其中的值

在scala中，+-*%等操作符完成的是和java⼀样的⼯作，位操作符&|^><也是⼀样。只有⼀点特殊的 区别：这些操作符实际上是⽅法。例如： a+b 是如下调⽤的简写： a.+(b) 这⾥的+就是⽅法名。 和java和c+相⽐，scala有⼀个显著的不同，scala并没有提供 +和—的操作。

Scala既是⾯向对象的编程语⾔，也是⾯向函数的编程语⾔，因此函数在Scala语⾔中的地位和类是同 等第⼀位的。下⾯的代码定义了⼀个简单的⽆返回值和有返回值的代码：

def main(args: Aray[String]): Unit = { option()

} defoption(): Unit = {

- 2.4.2. 指定类型

- 2.4.3. 常⽤变量


当然如果你愿意，你也可以采⽤和Java⼀样的⽅法，明确指定变量的类型，如 valmyint:Int = 1; valmystring:String=nul;

和java⼀样，scala也有7中数据类型：Byte,Char,Short,Int,Long,Float和Double，以及⼀个Bolean类 型。跟java不同的是，这些类型是类。scala不刻意区分基本数据类型和引⽤数据类型。你可以对数字 执⾏⽅法，例如：

- 2.5.算数和操作符重载

- 2.6.定义函数


- 2.6.1. 空参⽆返回值


println("ok") }

option函数的返回值类型为Unit表⽰该函数不返回任何有意义的值，Unit类似于Java中的void类型

- 1 }

def main(args: Aray[String]): Unit = { valreturnval = option(3,5) println(returnval);

} defoption(x:Int,y:Int): Int = {

if(x >y) x else

y

} Scala函数以def定义，然后是函数的名称（如option)，然后是以逗号分隔的参数。Scala中变量类型是 放在参数和变量的后⾯，以“：”隔开。同样如果函数需要返回值，它的类型也是定义在参数的后⾯（实 际上每个Scala函数都有返回值，只是有些返回值类型为Unit，类似为void类型）。 此外每个Scala表达式都有返回结果（这⼀点和Java，C#等语⾔不同），⽐如Scala的 if else 语句也是 有返回值的，因此函数返回结果⽆需使⽤return语句。实际上在Scala代码应当尽量避免使⽤return语 句。函数的最后⼀个表达式的值就可以作为函数的结果作为返回值。 同样由于Scala的”type inference”特点，本例其实⽆需指定返回值的类型。对于⼤多数函数Scala都可 以推测出函数返回值的类型，但⽬前来说回溯函数（函数调⽤⾃⾝）还是需要指明返回结果类型的。

- 2.6.2. 空参有返回值

- 2.6.3. 有参数有返回值


def main(args: Aray[String]): Unit = { valreturnval = option() println(returnval);

} defoption():Int= {

- s=1

}else{

- s=2 }

在java中，每个语句都以分号结束。⽽在scala中，与js脚本语⾔类似，⾏尾不需要分号。同样，在}和 else以及类似的位置也不需要写分号，但是如果你倾向与使⽤分号，⽤就是了，他没啥坏处。

在java中，块语句使⽤⼀个包在{}中的语句序列。每当你需要在逻辑分⽀或者循环中放置多个动作时， 你可以使⽤块语句。 在scala中，{}块包含⼀系列表达式，其结果也是⼀个表达式。块中最后⼀个表达式的值就是块的值。 例如： package org.apache.first import scala.math._ object test {

defmain(args: Aray[String]): Unit = {

valtest = { val dx = 0; val dy = 10;

- 3. 控制结构和函数


- 3.1.条件表达式if else

- 3.2.语句终⽌

- 3.3.块表达式和赋值


scala的if/else语法结构和java⼀样。不过，在scala中if/else表达式有值，这个值就跟在if或者else之后 的表达式值。例如： if(x>0) 1 else -1 上述表达式的值是1或者-1. 也可以将表达式的值赋予其他变量

- val s = if (x>0) 1 else -1 这个语句的写法也可以为： val x = 0


vars =0 if(x>0) {

min(dx,dy);

} println(test)

}

## 3.4.循环

- 3.4.1. while循环


object test {

defmain(args: Aray[String]): Unit = { varn = 10; while(n>0){ println(n) n-=1;

} }

- 3.4.2. for


def main(args: Aray[String]): Unit = { for(i <-1 to 10){

println(i) }

} 注：1 to 10，返回数字1到数字10，含头含尾。 退出循环 import scala.util.control.Breaks._ object test {

defmain(args: Aray[String]): Unit = {

for(i <-1 to 10){ if(i =3) return; println(i)

} }

在这⾥，控制权的转移是通过抛出和捕获异常完成的，因此，如果时间很重要的话，尽量避免使⽤。

- 3.4.3. ⾼级for循环


object test { defmain(args: Aray[String]): Unit = { for(i <-1 to 10;j <-1 to 3){

println(i+j) }

}

objecttest { defmain(args: Aray[String]): Unit = { for(i <-1 to 10;j <-1 to 3ifi = j){

println(i+j) }

}

## 3.5.函数

- 3.5.1. 默认参数


object test { defmain(args:Aray[String]): Unit = {

- val a = option(2); println(a);


} defoption(x:Int,y:Int=1):Int = {

if(x >y) x else

y

} } option函数有两个参数，但是y这个参数是带默认值的，所以，如果你只传⼀个参数x，那么y就会⾛默 认值。

- 3.5.2. 变长参数


objecttest { defmain(args: Aray[String]): Unit = { option(2,3,4,5,6);

} defoption(args:Int*): Unit = {

println(args(3) for(arg <- args){

println(arg); }

} }

- 3.5.3. 递归


object test {

defmain(args: Aray[String]): Unit = { var a = option(2,3,4,5,6); println(a)

} defoption(args:Int*): Int = {

if(args.length=0){ 0 }else{

args.head + option(args.tail: _*) }

} } 在这⾥，args.head是args的⾸个元素，⽽tail是所有其他的参数序列，_*是将args.tail转换成⼀个序 列。类似于js的eval（）。

## 3.6.过程

scala对于不返回值的函数有特殊的表⽰法。如果函数体包含在花括号当中，但是没有等号“=”，那么返 回值类型就是Uint。这样的函数被称为过程（procedure）。过程不返回任何值。 object test {

defmain(args: Aray[String]):Unit = { option(2);

} defoption(x:Int) = {

println(x+5)

} } 也可以声明Unit返回值。

- 3.7.懒值

- 3.8.异常


当val被声明为lazy时，它的初始化将被推迟，知道我们⾸次对它取值。例如： def main(args: Aray[String]): Unit = {

lazyvalwords =scala.io.Source.fromFile("/usr/local/a.txt").mkString;

/println(words); }

如果不访问words，那么⽂件不会被打开，但是去掉lazy或者访问words的时候，就会报错。

scala的异常⼯作机制和java的⼀样。当你抛出异常时，⽐如： throw newIlegalArgumentException(" a"); 当前的运算被中⽌。不过与java不同的是，scala不需要声明函数会抛出哪种异常。 try{ }catch{ }finaly{ }

# 4. 数组及操作

- 4.1.定长数组


/3个整数的数据，所有元素为0

- val ar1 = new Aray[Int](3)
- val ar2 = Aray(0, 0, 0)
- val ar3 = Aray("hadop","spark") /取值、赋值


val str = ar3(1) ar3(1) = "storm"

- 4.2.变长数组


ArayBufer类似Java中的ArayList

- val a = ArayBufer[Int]() 或val a = new ArayBufer[Int] a +=1/在尾端添加元素 a +=(1,2,3)/追加多个元素 a += ArayBufer(7,8,9)/追加集合 a.trimEnd(2) /移除最好两个元素 a.insert(2, 6, 7) /在下标2之前插⼊6，7

- a.remove(2, 2) /从下标2开始移除2个元素

val b = a.toAray /转变成定长数据组

- b.toBufer/转变成变长数据组




- 4.3.遍历数组


val ar = Aray(1,2,3,4,5)

/带下标的for循环 for (i <- (0 until (ar.length,2).reverse) { println(ar(i) }

/增强for循环 for (i <- ar) println(i)

- 4.4.数组转换

- 4.5.数组的常⽤算法


转换动作不会修改原始数组，⽽是产⽣⼀个全新的数组 val ar = Aray(1,2,3,4,5,6,7,8,9,10) for(e <- ar) yield e/⽣成⼀个与ar⼀样的 for(e <- ar if e % 2 = 0) yield e * 2

/思考：如何变得跟简单

val ar = Aray(4,3,5,1,2) val res = ar.sum Aray("spark","hadop","storm").min

- val b = ar.sorted
- val c = ar.sortWith(_>_)
- val d = ar.count(_>2)


- 5. 映射

- 6. 元祖

- 7. 复杂的集合操作⽅法


val map = Map("a"->1,"b"->2, "c"->3) 或 val map = Map("a",1), ("b",2),("c",3)

/取值、赋值

- map("a") map.getOrElse("d", 0)
- map("b") = 2 map += ("e"->8,"f"->9) map -= ("f")


/迭代 for(k,v) <- map) /交换k,v for(k,v) <- map) yield (v,k) /keySet和values

元组是不同类型元素的集合

- val t = ("hadop", 1 0,"spark") val a = t._3/元组中的下标是从1开始 "NewYork".partition(_.isUper)


- 7.1.1. map


讲数组中的值取出来 val a = Aray(6,7,8,9) 计算： a.map(_*2)/ Aray[Int] = Aray(12, 14, 16,18) 数组转成Aray（tuple）：

map(_,1)/ Aray(6,1), (7,1), (8,1),(9,1)

val map = Map("a"->1,"b"->2, "c"->3) map.map(x=>x._1)/取出map的第⼀个值 map.map(x=>(x._1,x._2)/取出map的所有值

- 7.1.2. flaten


把数组压平 语法： val a = Aray(Aray(6,7,8,9), Aray(10,1,12,13,14) val res = a.falten/ val a = Aray(Aray(6,7,8,9),Aray(10,1,12,13,14)

- 7.1.3. groupby
- 7.1.4. foldLeft
- 7.1.5. reduce
- 7.1.6. agregate


分组 val map = Map("a"->1,"b"->2, "c"->3) 按照key分组：map.groupBy(_._1) 结果：Map(b -> Map(b -> 2), a -> Map(a -> 1), c -> Map(c-> 3)

传⼊初始值后，多数组元素叠加

val a = Aray(1,2,3,4,5) a.foldLeft(0)(_+_)/15

val a = Aray(1,2,3,4,5) a.reduce(_+_)/15

agregate函数将每个分区⾥⾯的元素进⾏聚合，然后⽤combine函数将每个分区的结果和初始值 (zeroValue)进⾏combine操作。这个函数最终返回的类型不需要和RD中元素类型⼀致。 计算两个数组的和 val a = Aray(Aray(1,2,3),Aray(4,2,3)

1.

agregate(0)(_+_.sum,_+_)

- 7.1.7. flatMap
- 7.1.8. reduceByKey
- 7.1.9. sortByKey
- 7.1.10. sortBy


先map后foldLeft

# 8. 类

- 8.1.类的定义和getset⽅法


带geter和seter属性 var 只带geter属性 val private和private[this]

- 8.2.主构造器和辅助构造器


- 1、跟在类名后⾯的是主构造器
- 2、辅助构造器的名称为this定义，必须执⾏def
- 3、辅助构造函数相当于java中的其他的累的构造器，所接受的参数不能多于主类的 例如 clas TestScala1 (val name: String,varpasword:String ) {


defthis(name: String) = this(name,nul) }

- 8.3. object半⽣对象

- 8.4.包

- 8.5.模式匹配


⽤对象作为单例或存放⼯具⽅法 可作为伴⽣对象 ⽤类的伴⽣对象的aply⽅法创建新的实例 扩展Ap特质作为main⽅法使⽤ 枚举

包可见性private[spark] 重命名import java.util.{HashMap => JHashMap} 隐式导⼊：和Java程序⼀样java.lang总是被默认引⼊，scala也默认引⼊

Scala有⼀个⾮常强⼤的模式匹配机制，可以应⽤到很多场合：如switch、类型查询，并可以结合样例 类完成⾼级功能

<table>
  <tr>
    <th>object TestScala1 { def main(args: Aray[String]): Unit = {<br><br>val a = new TestScala2 println(a.test("zhangsan")<br><br>}<br><br>} clas TestScala2 {<br><br>def test(name: String): String = {<br><br>/和java的switch⼀样，只是关键字变成了match,分在=》后⾯的是⼀个⽅法体，⽽=》符号表⽰匿 名函数<br><br>name match { case "zhangsan" => TestScala2.test(name)<br><br>ase "lisi" => name+"fdsfas" case _ => name+"fdsfas"<br><br>} }<br><br>}<br><br>object TestScala2 { deftest(name: String): String = {<br><br><br>"wangwu" }</th>
  </tr>
</table>


}

## 8.6.样本类

<table>
  <tr>
    <th>caseclas TestScala1(valname: String, varpasword: String) {<br><br>ethis(name: String) = this(name, nul) defthis() = this(nul, nul)</th>
  </tr>
</table>


}

样本类：添加了case的类便是样本类。这种修饰符可以让Scala编译器⾃动为这个类添加⼀些语法上 的便捷设定。如下：

添加与类名⼀致的⼯⼚⽅法。也就是说，可以写成Var("x")来构造Var对象。 样本类参数列表中的所有参数隐式获得了val前缀，因此它被当作字段维护。 编译器为这个类添加了⽅法toString,hashCode和equals等⽅法。

- 9. 继承

- 10. ⾼阶函数


重新⽅法：重新⼀个⾮抽象的⽅法必须使⽤overide修饰符

- o.isInstanceOf[Cl]/java中的instanceof
- o.asInstacneOf[Cl]/java中的 (Cl) o clasOf[Cl]/java中的Cl.clas


def fun(f: Double => Double) = { f(10)

} fun(x : Double) => 2 * x) fun(sqrt _)

1. 柯⾥化

将原来接受两个参数的函数变成接受⼀个参数的函数的过程。 例如定义⼀个函数是这样滴： def first(x:Int) = (y:Int) => x + y 那么调⽤的时候应该这样写

val second=first(1) val result=second(2) 实际上柯⾥化函数将上⾯的步骤简化了： 简化成这样定义： def fun(x: Int)(y: Int) = x * y 调⽤时你可以这样： fun(6)(7) 也可以这样： val fun2 = fun(6)_ / 这次调⽤返回的是⼀个⽅法，传⼊⼀个值，但是本⾝这个函数需要两个值，所以 我们⽤”_”来站位我们不知道的另⼀个参数 val result = fun2(7) / 42

# 12. 隐式转换

隐式转换：就是将某各类中没有的⽅法实现，在另⼀个增强类中实现，然后⽤⼀个固定的⽅式将某各 类转换成这个增强类

- 1.
- 2.
- 3.
- 4.


例如：java的file类中并没有read⽅法，那么我们想读⽂件，需要利⽤scala的source这个类帮我们 读⽂件 那么，我们重新定义⼀个类RichFile，接受的参数是File类型，⽽利⽤ Source.fromFile(file.getPath().mkString帮助我们读取⽂件，但是这个⽅法位于RichFile中，⽆法 让File类直接使⽤，所以我们需要将File转换成RichFile 那么，我们写⼀个object伴⽣对象，将我们的File转换成RichFile，书写如下，注意必须有impilicit 关键字声明⽅法：implicit def file2RichFile(f:File)= new RichFile(f) 在我们需要使⽤File调⽤read⽅法时，import之前的半⽣对象即可import Context._

<table>
  <tr>
    <th>package org.apache.scala.one impotscala.colection.mutable.Map<br><br>m t aa.math._ i orscala.io.Source import java.io.File clas RichFile(val file:File){<br><br>def read = Source.fromFile(file.getPath().mkString<br><br>} object Context{<br><br>implicit def file2RichFile(f:File)= new RichFile(f)<br><br>} object ImplicitDemo extends Ap{<br><br>import Context._ println(new File("c:/ s.txt").read) 1.to(10)<br><br>}</th>
  </tr>
</table>


