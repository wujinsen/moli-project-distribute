# 1. Scala基础

- 1.1.声明变量

- 1.2.常⽤类型

Scala和Java⼀样，有7种数值类型Byte、Char、Short、Int、Long、Float和Double（⽆包装类型）和 ⼀个Bolean类型

- 1.3.条件表达式


<table>
  <tr>
    <th>packagecn.itcast.scala /*<br><br>* Created by ZX on 2015/1/6.<br><br>*/<br><br><br>object VariableDemo { def main(args: Aray[String]) {<br><br>/使⽤val定义的变量值是不可变的，相当于 java⾥ ⽤ final修 饰 的 变 量 vali = 1<br><br>/使⽤var定义的变量是可变得，在Scala中 ⿎励使 ⽤ val vars = "helo"<br><br>/Scala编译器会⾃动推断变量的类型，必 要 的 时候可 以 指 定 类 型 /变量名在前，类型在后<br><br>valstr: String = "itcast" }<br><br>}<br><br></th>
  </tr>
</table>


Scala的的条件表达式⽐较简洁，例如：

packagecn.itcast.scala /*

- * Created by ZX on 2015/1/7.

- */


object ConditionDemo { def main(args: Aray[String]) { valx = 1

/判断x的值，将结果赋给y valy = if (x > 0) 1else-1

- /打印y的值

- println(y)

/⽀持混合类型表达式 valz = if (x > 1) 1else"eror" /打印z的值

- println(z)




/如果缺失else，相当于if (x > 2) 1 else () valm = if (x > 2) 1

- println(m)

/在scala中每个表达式都有值，scala中 有个Unit类 ， 写做 (),相 当 于 Java中 的 void valn = if (x > 2) 1else()

- println(n)


/if和else if valk = if (x < 0) 0 else if (x >= 1) 1 else -1 println(k)

}

}

- 1.4.块表达式

- 1.5.循环


<table>
  <tr>
    <th>packagecn.itcast.scala /*<br><br>* Created by ZX on 2015/1/7.<br><br>*/<br><br><br>object BlockExpresionDemo { def main(args: Aray[String]) { valx = 0<br><br>/在scala中{}中课包含⼀系列表达式， 块 中 最后⼀ 个 表 达 式 的 值 就 是 块 的 值<br><br>/下⾯就是⼀个块表达式 valresult = {<br><br>if (x < 0){<br><br>-1 }else if(x >= 1) { 1 }else {<br><br>"eror" }<br><br>}<br><br>/result的值就是块表达式的结果 println(result)<br><br>} }<br><br></th>
  </tr>
</table>


在scala中有for循环和while循环，⽤for循环⽐较多

##### for循环语法结构：for(i <- 表达式/数组/集合)

<table>
  <tr>
    <th>packagecn.itcast.scala /*<br><br>* Created by ZX on 2015/1/7.<br><br>*/<br><br><br>object ForDemo { def main(args: Aray[String]) {<br><br>/for(i <- 表达式),表达式1to 10返 回 ⼀ 个 Range（ 区 间 ） /每次循环将区间中的⼀个值赋给i<br><br>for(i <- 1 to 10) println(i)<br><br>/for(i <- 数组) valar = Aray("a","b","c") for(i <- ar)<br><br>println(i)<br><br>/⾼级for循环 /每个⽣成器都可以带⼀个条件，注意：if前 ⾯ 没 有分号<br><br>for(i <- 1 to 3; j <- 1 to 3 if i != j)<br><br>print(10 * i + j)+ " ") println()<br><br>/for推导式：如果for循环的循环体以yield开 始 ， 则 该 循 环 会 构 建 出 ⼀ 个 集 合 /每次迭代⽣成集合中的⼀个值<br><br>valv = for (i <- 1 to 10) yield i * 10 println(v)<br><br>}<br><br>}<br><br></th>
  </tr>
</table>


## 1.6.调⽤⽅法和函数

Scala中的+ - * / %等操作符的作⽤与Java⼀样，位操作符 & | ^ > <也⼀样。只是有 ⼀点特别的：这些操作符实际上是⽅法。例如： a + b 是如下⽅法调⽤的简写：

- 1. +(b)


a ⽅法 b可以写成a.⽅法(b)

## 1.7.定义⽅法和函数

- 1.7.1.定义⽅法

⽅法的返回值类型可以不写，编译器可以⾃动推断出来，但是对于递归函数，必须指定返回类型

- 1.7.2.定义函数

- 1.7.3.⽅法和函数的区别 在函数式编程语⾔中，函数是“头等公民”，它可以像任何其他数据类型⼀样被传递和操作 案例：⾸先定义⼀个⽅法，再定义⼀个函数，然后将函数传递到⽅法⾥⾯


![image 1](<scala编程基础.note_images/imageFile1.png>)

![image 2](<scala编程基础.note_images/imageFile2.png>)

![image 3](<scala编程基础.note_images/imageFile3.png>)

<table>
  <tr>
    <th>packagecn.itcast.scala /*<br><br>* Created by ZX on 2015/1/11.<br><br>*/<br><br><br>object MethodAndFunctionDemo { /定义⼀个⽅法 /⽅法m2参数要求是⼀个函数，函数的参数必须是 两个 Int类 型 /返回值类型也是Int类型<br><br>def m1(f: (Int, Int) => Int) : Int = {<br><br>f(2, 6) }<br><br>/定义⼀个函数f1，参数是两个Int类型，返回 值 是 ⼀个Int类 型<br><br>val f1 = (x: Int, y: Int) => x + y /再定义⼀个函数f2<br><br>val f2 = (m: Int, n: Int) => m * n<br><br><br>/main⽅法 def main(args: Aray[String]) {<br><br>/调⽤m1⽅法，并传⼊f1函数<br><br>valr1 = m1(f1)<br><br>println(r1)<br><br>/调⽤m1⽅法，并传⼊f2函数 valr2 = m1(f2)<br><br>println(r2)<br><br><br><br><br><br><br>} }<br><br></th>
  </tr>
</table>


- 1.7.4.将⽅法转换成函数（神奇的下划线）


![image 4](<scala编程基础.note_images/imageFile4.png>)

# 2.数组、映射、元组、集合

- 2.1.数组


- 2.1.1.定长数组和变长数组


packagecn.itcast.scala import scala.colection.mutable.ArayBufer /*

- * Created by ZX on 2015/1/1.

- */


object ArayDemo {

def main(args: Aray[String]){

/初始化⼀个⻓度为8的定⻓数组，其所有元素均为0

- valar1 = new Aray[Int](8) /直接打印定⻓数组，内容为数组的hashcode值

println(ar1) /将数组转换成数组缓冲，就可以看到原数组中的内容了 /toBufer会将数组转换⻓数组缓冲

- println(ar1.toBufer)

/注意：如果new，相当于调⽤了数组的aply⽅法，直接为数组赋值

/初始化⼀个⻓度为1的定⻓数组 valar2 = Aray[Int](10)

- println(ar2.toBufer)


- valar3 = Aray("hadop", "storm", "spark") /使⽤()来访问元素


/定义⼀个⻓度为3的定⻓数组

println(ar3(2)

/ / / / /变⻓数组（数组缓冲）

/如果想使⽤数组缓冲，需要导⼊import scala.colection.mutable.ArayBufer包

valab = ArayBufer[Int]() /向数组缓冲的尾部追加⼀个元素

/+=尾部追加元素 ab += 1

/追加多个元素 ab += (2, 3, 4, 5)

/追加⼀个数组 += ab ++= Aray(6, 7)

/追加⼀个数组缓冲 ab ++= ArayBufer(8,9) /打印数组缓冲ab

/在数组某个位置插⼊元素⽤insert ab.insert(0, -1, 0)

/删除数组某个位置的元素⽤remove ab.remove(8, 2) println(ab)

} }

### 2.1.2.遍历数组

- 1.增强for循环
- 2.好⽤的until会⽣成脚标，0 until 10 包含0不包含10


![image 5](<scala编程基础.note_images/imageFile5.png>)

<table>
  <tr>
    <th>packagecn.itcast.scala /*<br><br>* Created by ZX on 2015/1/12.<br><br>*/<br><br><br>object ForArayDemo {<br><br>def main(args: Aray[String]) { /初始化⼀个数组 valar = Aray(1,2,3,4,5,6,7,8)<br><br>/增强for循环 for(i <- ar) println(i)<br><br>/好⽤的until会⽣成⼀个Range /reverse是将前⾯⽣成的Range反 转 for(i <- (0 until ar.length).reverse)<br><br>println(ar(i) }<br><br>}<br><br></th>
  </tr>
</table>


### 2.1.3.数组转换 yield关键字将原始的数组进⾏转换会产⽣⼀个新的数组，原始的数组不变

![image 6](<scala编程基础.note_images/imageFile6.png>)

<table>
  <tr>
    <th>packagecn.itcast.scala /*<br><br>* Created by ZX on 2015/1/12.<br><br>*/<br><br><br>object ArayYieldDemo { def main(args: Aray[String]) { /定义⼀个数组 valar = Aray(1, 2, 3, 4, 5, 6, 7, 8, 9)<br><br>/将偶数取出乘以10后再⽣成⼀个新的数组 valres = for (e <- ar if e % 2 = 0) yield e * 10 println(res.toBufer)<br><br>/更⾼级的写法,⽤着更爽 /filter是过滤，接收⼀个返回值为bolean的 函 数 /map相当于将数组中的每⼀个元素取出来， 应 ⽤ 传进去 的 函 数<br><br>valr = ar.filter(_% 2 == 0).map(_ * 10) println(r.toBufer)<br><br>} }<br><br></th>
  </tr>
</table>


### 2.1.4.数组常⽤算法 在Scala中，数组上的某些⽅法对数组进⾏相应的操作⾮常⽅便！

![image 7](<scala编程基础.note_images/imageFile7.png>)

## 2.2.映射

在Scala中，把哈希表这种数据结构叫做映射

- 2.2.1.构建映射

val scores = Map(“” -> 85, ” -> 12) val scores = Map(“”,85),()()

- 2.2.2.获取和修改映射中的值


![image 8](<scala编程基础.note_images/imageFile8.png>)

![image 9](<scala编程基础.note_images/imageFile9.png>)

好⽤的getOrElse

![image 10](<scala编程基础.note_images/imageFile10.png>)

注意：在Scala中，有两种Map，⼀个是i mutable包下的Map，该Map中的内容不可变；另⼀个是 mutable包下的Map，该Map中的内容可变 例⼦：

![image 11](<scala编程基础.note_images/imageFile11.png>)

注意：通常我们在创建⼀个集合是会⽤val这个关键字修饰⼀个变量（相当于java中的final），那么就 意味着该变量的引⽤不可变，该引⽤中的内容是不是可变，取决于这个引⽤指向的集合的类型

val scores = Map(“”->10,”->15)

## 2.3.元组

映射是K/V对偶的集合，对偶是元组的最简单形式，元组可以装着多个不同类型的值。

- 2.3.1.创建元组


![image 12](<scala编程基础.note_images/imageFile12.png>)

- 2.3.2.获取元组中的值


![image 13](<scala编程基础.note_images/imageFile13.png>)

取得元组中的元素:下划线加脚标

### 2.3.3.将对偶的集合转换成映射

![image 14](<scala编程基础.note_images/imageFile14.png>)

- 2.3.4.拉链 zip命令可以将多个值绑定在⼀起


![image 15](<scala编程基础.note_images/imageFile15.png>)

注意：如果两个数组的元素个数不⼀致，拉链操作后⽣成的数组的长度为较⼩的那个数组的元素个数

## 2.4.集合

Scala的集合有三⼤类：序列Seq、集Set、映射Map，所有的集合都扩展⾃Iterable特质 在Scala中集合有可变（mutable）和不可变（i mutable）两种类型，i mutable类型的集合初始化后 就不能改变了（注意与val修饰的变量进⾏区别）

- 2.4.1.序列 不可变的序列import scala.colection.i mutable._ 在Scala中列表要么为空（Nil表⽰空列表）要么是⼀个head元素加上⼀个tail列表。 9: List(5, 2) : 操作符是将给定的头和尾创建⼀个新的列表 注意： : 操作符是右结合的，如9: 5: 2: Nil相当于9: (5: (2: Nil)


<table>
  <tr>
    <th>packagecn.itcast.colect<br><br>object I mutListDemo {<br><br>def main(args: Array[String]) {<br><br>/创建⼀个不可变的集合<br><br>vallst1 = List(1,2,3) /将0插⼊到lst1的前⾯⽣成⼀个新的List<br><br>vallst2 = 0: lst1<br><br>vallst3 = lst1.:(0)<br><br>vallst4 = 0 +: lst1<br><br>vallst5 = lst1.+:(0)<br><br>/将 个元素添加到lst1的后⾯产⽣⼀个新的集合<br><br>vallst6 = lst1 :+ 3<br><br>vallst0 = List(4,5,6)<br><br>/将2个list合并成⼀个新的List<br><br>vallst7 = lst1 + lst0 /将lst0插⼊到lst1前⾯⽣成⼀个新的集合<br><br>vallst8 = lst1 +: lst0<br><br>/将lst0插⼊到lst1前⾯⽣成⼀个新的集合<br><br>vallst9 = lst1. :(lst0)<br><br><br>println(lst9)<br><br>}<br><br></th>
  </tr>
</table>


}

可变的序列import scala.colection.mutable._

<table>
  <tr>
    <th>packagecn.itcast.colect import scala.colection.mutable.ListBufer<br><br>object MutListDemo extends Ap{ /构建⼀个可变列表，初始有3个元素1,2,3<br><br>val lst0 = ListBufer[Int](1,2,3) /创建⼀个空的可变列表<br><br>val lst1 = new ListBufer[Int] /向lst1中追加元素，注意：没有⽣成新的集合<br><br>lst1 += 4 lst1.apend(5)<br><br>/将lst1中的元素最近到lst0中， 注意：没有⽣成新的集合 lst0 ++= lst1<br><br>/将lst0和lst1合并成⼀个新的ListBufer 注意：⽣成了⼀个集合<br><br>val lst2= lst0 + lst1<br><br>/将元素追加到lst0的后⾯⽣成⼀个新的集合<br><br>val lst3 = lst0 :+ 5<br><br><br>}<br><br></th>
  </tr>
</table>


## 2.5. Set

不可变的Set

<table>
  <tr>
    <th>packagecn.itcast.colect import scala.colection.immutable.HashSet<br><br>object I mutSetDemo extends Ap{<br><br>val set1 = new HashSet[Int]() /将元素和set1合并⽣成⼀个新的set，原有set不变<br><br>val set2 = set1 + 4 /set中元素不能重复<br><br>val set3 = set1 + Set(5, 6, 7) val set0 = Set(1,3,4) + set1 println(set0.getClas)<br><br><br>}<br><br></th>
  </tr>
</table>


##### 可变的Set

<table>
  <tr>
    <th>packagecn.itcast.colect import scala.colection.mutable<br><br>object MutSetDemo extendsAp{ /创建⼀个可变的HashSet val set1 = new mutable.HashSet[Int]()<br><br>/向HashSet中添加元素 set1 += 2<br><br>/ad等价于+= set1.ad(4) set1 ++= Set(1,3,5) println(set1)<br><br>/删除⼀个元素 set1 -= 5 set1.remove(2) println(set1)<br><br>}<br><br></th>
  </tr>
</table>


## 2.6. Map

<table>
  <tr>
    <th>packagecn.itcast.colect import scala.colection.mutable<br><br>object MutMapDemo extendsAp{ val map1 = new mutable.HashMap[String, Int]()<br><br>/向map中添加数据 map1("spark") = 1 map1 +=("hadop", 2) map1.put("storm", 3) println(map1)<br><br>/从map中移除元素 map1 -= "spark" map1.remove("hadop") println(map1)<br><br>}<br><br></th>
  </tr>
</table>


# 3.类、对象、继承、特质

Scala的类与Java、C+的类⽐起来更简洁，学完之后你会更爱Scala！！！

- 3.1.类


- 3.1.1.类的定义


<table>
  <tr>
    <th>/在 Scala中 ， 类 并 不 ⽤ 声 明 为 public。 /Scala源 ⽂ 件中 可 以 包 含 多 个 类 ， 所 有 这 些 类 都具 有 公 有 可 ⻅ 性 。<br><br>clas Person {<br><br>/⽤ val修 饰 的 变 量 是 只 读 属 性 ， 有 geter但 没 有 seter /（ 相 当 与 Java中 ⽤ final修 饰 的 变 量 ）<br><br>val id= "9527"<br><br>/⽤ var修 饰 的 变 量 既 有 geter⼜ 有 seter var age: Int = 18<br><br>/类 私 有 字 段 ,只 能 在 类 的 内 部 使 ⽤ private var name: String = "唐 伯 ⻁ "<br><br>/对 象 私 有 字 段 ,访 问 权 限 更 加 严 格 的 ， Person类 的 ⽅ 法 只 能 访 问 到 当 前 对 象 的 字 段 private[this] val pet = "⼩ 强 "<br><br></th>
  </tr>
</table>


}

<table>
  <tr>
    <th>classCounter { private var value = 0 private var privateAge = 0 def increment()={ value += 1 } def current() =value def current2 = value<br><br>defdef aagege_== priv(newateAge//getteValue: Int) =r{⽅//s法,ett叫做er⽅法,叫做age age_= if(newValue > privateAge) privateAge = newValue// 不能变年轻<br><br>} }<br><br>objecdef mtain(args:Counter{Array[String]){ val myCounter =new Counter //或者new Counter() myCounter.increment() //如果有内容修改，可以使⽤ () println(myCounter.current) //没有内容修改, 可以不使⽤ ()<br><br>} }<br><br></th>
  </tr>
</table>


privte[this]定义更加严格的访问权限 private[this] var value = 0

- 3.1.2.构造器


##### 注意：主构造器会执⾏类定义中的所有语句

<table>
  <tr>
    <th>/*<br><br>*每个类 都 有 主 构 造 器 ， 主 构 造 器 的 参 数 直 接放 置类 名后 ⾯ ，与 类 交 织 在 ⼀ 起<br><br>*/<br><br><br>clas Student(val name: String, val age: Int){ //主构 造 器 会 执 ⾏ 类 定 义中 的 所 有 语 句 println("执 ⾏ 主 构 造 器 ")<br><br>try {<br><br>println("读 取 ⽂ 件 ")<br><br>throw new IOException("io exception") } catch {<br><br>case e: NulPointerException => println("打 印 异常Exception : " + e)<br><br>case e: IOException => println("打 印 异常 Exception : "+ e) } finaly {<br><br>println("执 ⾏ finaly部 分 ") }<br><br>private var gender = "male"<br><br>//⽤this关 键 字定 义 辅 助 构 造 器 def this(name: String, age: Int, gender:String){<br><br>//每 个 辅 助 构 造 器 必 须 以主 构 造 器 或 其 他 的 辅 助 构 造 器 的 调 ⽤ 开 始<br><br>this(name, age)<br><br>println("执 ⾏ 辅 助 构 造 器 ")<br><br>this.gender = gender }<br><br>}<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>/*<br><br>*构造器 参 数 可 以 不 带val或var， 如 果 不 带val或var的 参 数⾄少 被 ⼀ 个 ⽅ 法 所 使 ⽤ ，<br><br>*那么它将 会 被 提 升 为 字 段<br><br>*/ /在类名后 ⾯ 加private就 变 成 了 私 有 的<br><br><br>clas Quen private(val name: String, prop: Aray[String], privatevar age: Int = 18){<br><br>println(prop.size)<br><br>//prop被 下 ⾯ 的 ⽅ 法 使 ⽤ 后 ，prop就 变 成 了 不 可变 得 对 象 私 有 字 段 ， 等 同 于private[this] val prop //如果 没 有 被 ⽅ 法 使 ⽤ 该 参 数 将 不 被 保 存 为 字 段 ， 仅仅 是 ⼀ 个 可 以 被 主 构 造 器 中 的 代 码 访 问 的 普 通 参 数 def description = name + " is " + age + " years old with " + prop.toBufer<br><br>} object Quen{<br><br>def main(args: Aray[String]) {<br><br>//私 有 的 构 造 器 ， 只 有 在 其 伴 ⽣ 对 象 中使 ⽤<br><br>val q = new Quen("hatano", Aray("蜡 烛", "⽪ 鞭"),20)<br><br>println(q.description)<br><br>}<br><br>}<br><br></th>
  </tr>
</table>


## 3.2.对象

- 3.2.1.单例对象 在Scala中没有静态⽅法和静态字段，但是可以使⽤object这个语法结构来达到同样的⽬的


- 1.
- 2.
- 3.


存放⼯具⽅法和常量 ⾼效共享单个不可变的实例 单例模式

<table>
  <tr>
    <th>packagecn.itcast.scala import scala.colection.mutable.ArrayBufer /*<br><br>* Created by ZX on 2015/1/14.<br><br>*/<br><br><br>object SingletonDemo {<br><br>def main(args: Array[String]){<br><br>/单例对象，不需要new，⽤【类名.⽅法】调⽤对象中的⽅法 valsesion = SesionFactory.getSesion()<br><br>println(sesion)<br><br>}<br><br>}<br><br>object SesionFactory{<br><br>/该部分相当于java中的静态块 var counts = 5 val sesions = new ArrayBufer[Sesion]() while(counts > 0){<br><br>sesions += new Session<br><br>counts -= 1<br><br>}<br><br>/在object中的⽅法相当于java中的静态⽅法 def getSesion(): Session ={<br><br>sesions.remove(0)<br><br>}<br><br>}<br><br>clas Sesion{<br><br>}<br><br></th>
  </tr>
</table>


### 3.2.2.伴⽣对象 在Scala的类中，与类名相同的对象叫做伴⽣对象，类和伴⽣对象之间可以相互访问私有的⽅法和属性

<table>
  <tr>
    <th>packagecn.itcast.scala /*<br><br>* Created by ZX on 2015/1/14.<br><br>*/<br><br><br>clas Dog { val id = 1 private var name = "itcast"<br><br>def printName(): Unit ={<br><br>/在Dog类中可以访问伴⽣对象Dog的私有 属 性 println(Dog.CONSTANT+ name )<br><br>} } /*<br><br>* 伴⽣对象<br><br>*/<br><br><br>object Dog {<br><br>/伴⽣对象中的私有属性<br><br>private val CONSTANT ="汪汪 汪 : "<br><br>def main(args: Aray[String]) { valp = new Dog<br><br>/访问私有的字段name p.name = "123" p.printName()<br><br>} }<br><br></th>
  </tr>
</table>


### 3.2.3. aply⽅法 通常我们会在类的伴⽣对象中定义aply⽅法，当遇到类名(参数1,.参数n)时aply⽅法会被调⽤

<table>
  <tr>
    <th>packagecn.itcast.scala /*<br><br>* Created by ZX on 2015/1/14.<br><br>*/<br><br><br>object AplyDemo {<br><br>def main(args: Aray[String]) { /调⽤了Aray伴⽣对象的aply⽅法 /def aply(x: Int, xs: Int*): Aray[Int] /arr1中只有⼀个元素5<br><br>valar1 = Aray(5) println(ar1.toBufer)<br><br>/new了⼀个⻓度为5的aray，数组⾥ ⾯ 包 含5个 nul varar2 = new Aray(5)<br><br>} }<br><br></th>
  </tr>
</table>


- 3.2.4.应⽤程序对象 Scala程序都必须从⼀个对象的main⽅法开始，可以通过扩展Ap特质，不写main⽅法。


<table>
  <tr>
    <th>packagecn.itcast.scala /*<br><br>* Created by ZX on 2015/1/14.<br><br>*/<br><br><br>object ApObjectDemo extends Ap{ /不⽤写main⽅法<br><br>println("I love you Scala") }<br><br></th>
  </tr>
</table>


## 3.3.继承

- 3.3.1.扩展类 在Scala中扩展类的⽅式和Java⼀样都是使⽤extends关键字


- 3.3.2.重写⽅法 在Scala中重写⼀个⾮抽象的⽅法必须使⽤overide修饰符

- 3.3.3.类型检查和转换

- 3.3.4.超类的构造


<table>
  <tr>
    <th>Scala</th>
    <th>Java</th>
  </tr>
  <tr>
    <td>obj.isInstanceOf[C]</td>
    <td>obj instanceof C</td>
  </tr>
  <tr>
    <td>obj.asInstanceOf[C]</td>
    <td>(C)obj</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
  </tr>
</table>


clasOf[C] C.clas

packagecn.itcast.scala /*

- * Created by ZX on 2015/1/10.

- */ object ClazDemo {


def main(args: Aray[String]) { /val h = new Human /println(h.fight)

} } trait Flyable{

def fly(): Unit ={

println("I can fly") }

def fight(): String } abstract clas Animal {

def run(): Int val name: String

} clas Human extends Animal with Flyable{

val name = "abc"

/打印⼏次"ABC"?

val t1,t2,(a, b, c) = { println("ABC") (1,2,3)

}

println(a)

println(t1._1)

/在Scala中重写⼀个⾮抽象⽅法必须⽤overide修 饰 overide def fight(): String = {

"fight with 棒⼦" }

/在⼦类中重写超类的抽象⽅法时，不需要使⽤override关 键 字 ， 写 了也 可 以 def run(): Int = {

1 }

}

# 4.模式匹配和样例类

Scala有⼀个⼗分强⼤的模式匹配机制，可以应⽤到很多场合：如switch语句、类型检查等。 并且Scala还提供了样例类，对模式匹配进⾏了优化，可以快速进⾏匹配

- 4.1.匹配字符串


<table>
  <tr>
    <th>packagecn.itcast.cases import scala.util.Random<br><br>object CaseDemo01 extends Ap{ val arr = Aray("YoshizawaAkiho", "YuiHatano", "AoiSola") val name = ar(Random.nextInt(ar.length) name match {<br><br>case "YoshizawaAkiho" => println("吉泽⽼师 .")<br><br>case "YuiHatano" =>println("波多⽼师 .")<br><br>case _ => println("真不知道你们在说什么 .") }<br><br>}<br><br></th>
  </tr>
</table>


## 4.2.匹配类型

<table>
  <tr>
    <th>packagecn.itcast.cases import scala.util.Random<br><br>object CaseDemo01 extends Ap{<br><br>/valv = if(x >= 5) 1 else if(x < 2) 2.0 else "helo" val arr = Aray("helo", 1, 2.0, CaseDemo) val v= ar(Random.nextInt(4)) println(v) v match {<br><br>case x: Int => println("Int " + x)<br><br>case y: Double if(y>= 0) => println("Double "+ y)<br><br>case z: String => println("String " + z) case _ => throw newException("not match exception")<br><br><br>} }<br><br></th>
  </tr>
</table>


注意：case y: Double if(y >= 0) => ... 模式匹配的时候还可以添加守卫条件。如不符合守卫条件，将掉⼊case _中

## 4.3.匹配数组、元组

<table>
  <tr>
    <th>packagecn.itcast.cases object CaseDemo03 extends Ap{<br><br>val arr = Aray(1, 3, 5) ar match {<br><br>case Aray(1, x, y) => println(x + " "+ y) case Aray(0) => println("only 0") case Aray(0, _*) =>println("0.") case _ => println("something else")<br><br>}<br><br>val lst = List(3, -1) lst match {<br><br>case 0: Nil => println("only 0") case x: y: Nil => println(s"x: $x y: $y") case 0: tail => println("0.") case _ => println("something else")<br><br>}<br><br>val tup = (2, 3, 7) tup match {<br><br>case (1, x, y) => println(s"1, $x , $y") case (_, z, 5) => println(z) case _ => println("else")<br><br>} }<br><br></th>
  </tr>
</table>


注意：在Scala中列表要么为空（Nil表⽰空列表）要么是⼀个head元素加上⼀个tail列表。 9: List(5, 2) : 操作符是将给定的头和尾创建⼀个新的列表 注意： : 操作符是右结合的，如9: 5: 2: Nil相当于9: (5: (2: Nil)

## 4.4.样例类

在Scala中样例类是⼀中特殊的类，可⽤于模式匹配。case clas是多例的，后⾯要跟构造参数，case object是单例的

<table>
  <tr>
    <th>packagecn.itcast.cases import scala.util.Random<br><br>case clas SubmitTask(id: String, name: String) case clas HeartBeat(time: Long) case object CheckTimeOutTask<br><br>object CaseDemo04 extends Ap{ val arr = Array(CheckTimeOutTask, HeartBeat(1233), SubmitTask(" 01", "task- 01")<br><br>ar(Random.nextInt(arr.length) match {<br><br>case SubmitTask(id,name) => {<br><br>println(s"$id, $name")<br><br>} case HeartBeat(time) => {<br><br>println(time) } case CheckTimeOutTask => {<br><br>println("check")<br><br>}<br><br>}<br><br>}<br><br></th>
  </tr>
</table>


## 4.5. Option类型

在Scala中Option类型⽤样例类来表⽰可能存在或也可能不存在的值(Option的⼦类有Some和None)。 Some包装了某个值，None表⽰没有值

<table>
  <tr>
    <th>packagecn.itcast.cases object OptionDemo {<br><br>def main(args: Aray[String]){ valmap = Map("a" ->1, "b"-> 2) valv = map.get("b")match {<br><br>case Some(i) => i case None => 0<br><br>} println(v)<br><br>/更好的⽅式 valv1 = map.getOrElse("c",0) println(v1)<br><br>} }<br><br></th>
  </tr>
</table>


如果想忽略None,则可以⽤for推导式

## 偏函数

被包在花括号内没有match的⼀组case语句是⼀个偏函数，它是PartialFunction[A, B]的⼀个实例，A 代表参数类型，B代表返回类型，常⽤作输⼊模式匹配

<table>
  <tr>
    <th>packagecn.itcast.cases object PartialFuncDemo {<br><br>def func1: PartialFunction[String, Int] = { case "one" => 1 case "two" => 2 case _ => -1<br><br>}<br><br>def func2(num: String): Int = num match { case "one" => 1 case "two" => 2 case _ => -1<br><br>}<br><br>def main(args: Aray[String]){<br><br>println(func1("one") println(func2("one")<br><br>}<br><br></th>
  </tr>
</table>


##### }

