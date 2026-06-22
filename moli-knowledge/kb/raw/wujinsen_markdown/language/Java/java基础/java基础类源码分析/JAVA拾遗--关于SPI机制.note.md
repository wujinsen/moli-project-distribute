htps:/mp.weixin.q.com/s/Y-PFZwzSORsznJYRfiM3DA

JDK提供的SPI(Service Provider Interface)机制，可能很多⼈不太熟悉，因为这个机制是针对⼚商或者 插件的，也可以在⼀些框架的扩展中看到。其核⼼类 java.util.ServiceLoader可以在jdk1.8的⽂档中 看到详细的介绍。虽然不太常⻅，但并不代表它不常⽤，恰恰相反，你⽆时⽆刻不在⽤它。⽞乎了， 莫急，思考⼀下你的项⽬中是否有⽤到第三⽅⽇志包，是否有⽤到数据库驱动？其实这些都和SPI有 关。再来思考⼀下，现代的框架是如何加载⽇志依赖，加载数据库驱动的，你可能会对 clas.forName("com.mysql.jdbc.Driver")这段代码不陌⽣，这是每个java初学者必定遇到过的，但如 今的数据库驱动仍然是这样加载的吗？你还能找到这段代码吗？这⼀切的疑问，将在本篇⽂章结束后 得到解答。 ⾸先介绍SPI机制是个什么东⻄

# 实现⼀个⾃定义的SPI

- 1 项⽬结构

这个简单的demo就是让⼤家体验，在不改变invoker代码，只更改依赖的前提下，切换interface的实现 ⼚商。

- 2 interface模块

- 2.1 moe.cnkirito.spi.api.Printer

interface只定义⼀个接⼝，不提供实现。规范的制定⽅⼀般都是⽐较⽜叉的存在，这些接⼝通常位于 java，javax前缀的包中。这⾥的Printer就是模拟⼀个规范接⼝。

3 god-printer模块

- 3.1 god-printer\pom.xml




![image 1](<JAVA拾遗--关于SPI机制.note_images/imageFile1.png>)

- 1.
- 2.
- 3.


invoker是我们的⽤来测试的主项⽬。 interface是针对⼚商和插件商定义的接⼝项⽬，只提供接⼝，不提供实现。 god-printer,bad-printer分别是两个⼚商对interface的不同实现，所以他们会依赖于interface项 ⽬。

- 1.
- 2.
- 3.


public interface Printer {

void print(); }

- 1.
- 2.
- 3.


<dependencies> <dependency> <groupId>moe.cnkirito</groupId>

- 4.
- 5.
- 6.
- 7.


<artifactId>interface</artifactId> <version>1.0-SNAPSHOT</version>

</dependency> </dependencies>

规范的具体实现类必然要依赖规范接⼝

- 3.2 moe.cnkirito.spi.api.GodPrinter

作为Printer规范接⼝的实现⼀

- 3.3 resources\META-INF\services\moe.cnkirito.spi.api.Printer

这⾥需要重点说明，每⼀个SPI接⼝都需要在⾃⼰项⽬的静态资源⽬录中声明⼀个services⽂件，⽂件 名为实现规范接⼝的类名全路径，在此例中便是 moe.cnkirito.spi.api.Printer，在⽂件中，则写上 ⼀⾏具体实现类的全路径，在此例中便是 moe.cnkirito.spi.api.GoodPrinter。 这样⼀个⼚商的实现便完成了。

- 4 bad-printer模块 我们在按照和god-printer模块中定义的⼀样的⽅式，完成另⼀个⼚商对Printer规范的实现。

- 4.1 bad-printer\pom.xml
- 4.2 moe.cnkirito.spi.api.BadPrinter
- 4.3 resources\META-INF\services\moe.cnkirito.spi.api.Printer


这样，另⼀个⼚商的实现便完成了。

- 5 invoker模块 这⾥的invoker便是我们⾃⼰的项⽬了。如果⼀开始我们想使⽤⼚商god-printer的Printer实现，是需 要将其的依赖引⼊。


- 1.
- 2.
- 3.
- 4.
- 5.


public class GoodPrinter implements Printer { public void print() {

System.out.println("你是个好⼈~"); }

}

1.

moe.cnkirito.spi.api.GoodPrinter

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


<dependencies>

<dependency> <groupId>moe.cnkirito</groupId> <artifactId>interface</artifactId> <version>1.0-SNAPSHOT</version>

</dependency> </dependencies>

- 1.
- 2.
- 3.
- 4.
- 5.


public class BadPrinter implements Printer { public void print() {

System.out.println("我抽烟，喝酒，蹦迪，但我知道我是好⼥孩~"); }

}

1.

moe.cnkirito.spi.api.BadPrinter

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


<dependencies>

<dependency> <groupId>moe.cnkirito</groupId> <artifactId>interface</artifactId> <version>1.0-SNAPSHOT</version>

</dependency> <dependency>

<groupId>moe.cnkirito</groupId> <artifactId>good-printer</artifactId> <version>1.0-SNAPSHOT</version>

</dependency> </dependencies>

- 5.1 编写调⽤主类


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


public class MainApp {

public static void main(String[] args) { ServiceLoader<Printer> printerLoader = ServiceLoader.load(Printer.class); for (Printer printer : printerLoader) {

printer.print(); }

} }

ServiceLoader是 java.util提供的⽤于加载固定类路径下⽂件的⼀个加载器，正是它加载了对应接⼝ 声明的实现类。

- 5.2 打印结果1


1.

你是个好⼈~

如果在后续的⽅案中，想替换⼚商的Printer实现，只需要将依赖更换

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


<dependencies>

<dependency> <groupId>moe.cnkirito</groupId> <artifactId>interface</artifactId> <version>1.0-SNAPSHOT</version>

</dependency> <dependency>

<groupId>moe.cnkirito</groupId> <artifactId>bad-printer</artifactId> <version>1.0-SNAPSHOT</version>

</dependency> </dependencies>

调⽤主类⽆需变更代码，这符合开闭原则

- 5.3 打印结果2


1.

我抽烟，喝酒，蹦迪，但我知道我是好⼥孩~

是不是很神奇呢？这⼀切对于调⽤者来说都是透明的，只需要切换依赖即可！

# SPI在实际项⽬中的应⽤

先总结下有什么新知识，resources/META-INF/services下的⽂件似乎我们之前没怎么接触过， ServiceLoader也没怎么接触过。那么现在我们打开⾃⼰项⽬的依赖，看看有什么发现。

- 1.

- a.
- b.


- 2.

a.

- 3.


在mysql-conector-java- x.jar中发现了META-INF\services\java.sql.Driver⽂件，⾥⾯只有两⾏ 记录：

我们可以分析出， java.sql.Driver是⼀个规范接⼝， com.mysql.jdbc.Driver com.mysql.fabric.jdbc.FabricMySQLDriver则是mysql-conector-java- x.jar对这个规范的实现接 ⼝。

com.mysql.jdbc.Driver com.mysql.fabric.jdbc.FabricMySQLDriver

在jcl-over-slf4j- x.jar中发现了META-INF\services\org.apache.comons.loging.LogFactory ⽂件，⾥⾯只有⼀⾏记录：

相信不⽤我赘述，⼤家都能理解这是什么含义了

org.apache.commons.logging.impl.SLF4JLogFactory

更多的还有很多，有兴趣可以⾃⼰翻⼀翻项⽬路径下的那些jar包

既然说到了数据库驱动，索性再多说⼀点，还记得⼀道经典的⾯试题： clas.forName("com.mysql.jdbc.Driver")到底做了什么事？ 先思考下：⾃⼰会怎么回答？ 都知道clas.forName与类加载机制有关，会触发执⾏com.mysql.jdbc.Driver类中的静态⽅法，从⽽使 主类加载数据库驱动。如果再追问，为什么它的静态块没有⾃动触发？可答：因为数据库驱动类的特 殊性质，JDBC规范中明确要求Driver类必须向DriverManager注册⾃⼰，导致其必须由clas.forName ⼿动触发，这可以在java.sql.Driver中得到解释。完美了吗？还没，来到最新的DriverManager源码 中，可以看到这样的注释,翻译如下： DriverManager 类的⽅法 getConnection 和 getDrivers 已经得到提⾼以⽀持 Java Standard Edition Service Provider 机制。 JDBC 4.0 Drivers 必须包括 META-INF/services/java.sql.Driver ⽂件。此⽂ 件包含 java.sql.Driver 的 JDBC 驱动程序实现的名称。例如，要加载 my.sql.Driver 类， METAINF/services/java.sql.Driver⽂件需要包含下⾯的条⽬：

1.

my.sql.Driver

应⽤程序不再需要使⽤ Class.forName() 显式地加载 JDBC 驱动程序。当前使⽤ Class.forName() 加载 JDBC 驱动程序的现有程序将在不作修改的情况下继续⼯作。 可以发现，Clas.forName已经被弃⽤了，所以，这道题⽬的最佳回答，应当是和⾯试官牵扯到JAVA 中的SPI机制，进⽽聊聊加载驱动的演变历史。 java.sql.DriverManager

- 1.
- 2.
- 3.
- 4.
- 5.


public Void run() { ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class); Iterator<Driver> driversIterator = loadedDrivers.iterator(); try{

while(driversIterator.hasNext()) {

- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.


driversIterator.next();

} } catch(Throwable t) { // Do nothing } return null;

}

当然那，本节的内容还是主要介绍SPI，驱动这⼀块这是引申⽽出，如果不太理解，可以多去翻⼀翻 jdk1.8中Driver和DriverManager的源码，相信会有不⼩的收获。

# SPI在扩展⽅⾯的应⽤

SPI不仅仅是为⼚商指定的标准，同样也为框架扩展提供了⼀个思路。框架可以预留出SPI接⼝，这样 可以在不侵⼊代码的前提下，通过增删依赖来扩展框架。前提是，框架得预留出核⼼接⼝，也就是本 例中interface模块中类似的接⼝，剩下的适配⼯作便留给了开发者。 例如我的上⼀篇⽂章中介绍的motan中Filter的扩展，便是采⽤了SPI机制，熟悉这个设定之后再回头去 了解⼀些框架的SPI扩展就不会太陌⽣了。

