代码

- 1 1 object HelloWorld {

- 2 2 def main(args: Array[String]): Unit = {

- 3 3 println("Hello world")

- 4 4 }

- 5 5 }


解说 object关键字相当于static（更确切的说法是单例模式的⼀种语⾔层⾯的⽀持），def关键字定义⽅法， ⽅法参数和返回值都采⽤后缀形式，即：“：类型”，Unit代表void，数组的表示采⽤Aray[T]，main是 特殊的⼊⼝⽅法。 在控制台执⾏“scala 对象名”，传⼊的参数必须是使⽤object声明的对象且成员⾥⾥包含main⽅法。 另外⼀种⽅式：继承AP

- 1 1 object HelloWorldThatExtendsApp extends App {

- 2 2 println("Hello world")

- 3 3 }


可以猜到Ap中已经包含main⽅法的定义，所有对象体中的代码，都会在对象被调⽤时⽴即执⾏（仅 执⾏⼀次）。

