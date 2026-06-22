在云计算、⼤数据领域，对数据的处理有两个⽐较流⾏的框架，他们分别是Hadop和Spark。⽬前 hadop属于普遍运⽤中，⽽Spark还只有少数公司还调研或尝试使⽤。Hadop包含了两个部分：分布 式的⽂件存储系统(HDFS)与分布式的计算系统（Map-Reduce）,这两个框架都是使⽤java写的。如果 对照Hadop的两个框架来说的话，Spark是⼀个分布式的计算框架，与Hadop基于⽂件系统计算不同 的是，Spark是基于内存进⾏计算的。基于内存操作⾃然是⽐就⽂件操作要快上很多，并且随着未来内 存这⼀类硬件越来越便宜，可以预想到Spark会越来越流⾏。所以，作为⼀个研发⼈员来讲，可以考虑 早点接触Spark，因为趋势在哪⾥。

假设你对Spark的概念已经很熟悉了，⾮常迫切的想了解和使⽤Spark，那么这⼀个系列的⽂章，或许 会对你有所帮助。因为Spark框架是由scala语⾔的编写的。scala是可伸缩的语⾔，是⼀⻔多范式的编 程语⾔，⼀种类似java的编程语⾔，设计初衷是要集成⾯向对象编程和函数式编程的各种特性。更多的 信息可以百度百科的说明，地址⻅附录《scala的百度百科》。

第1章 windows下安装scala及scala的基本⽤法

第⼀部分：windows下安装scala

- 1、下载：在scala官⽅⽹站下载scala安装包。 下载的时候会根据你当前的操作系统，⾃动为你适配不同系统的安装包。⽹址⻅附录《scala⽹

站》。

scala⽹站提供了web版本的api查询，如果有不明⽩的问题，可以再⽹站进⾏查询。⽹址⻅附录 《scala api 》。

- 2、安装：安装scala-2.1.7.msi 下载完成之后，得到scala-2.1.7.msi安装包。双击运⾏即可，然后⼀路下⼀步。中间可以根据⾃⼰

的喜好更改安装⽬录。

- 3、使⽤：直接打开cmd命令使⽤ 安装完成之后，系统的环境变量中，path变量的值，会追加以下内容：D:\Program Files


(x86)\scala\bin。这意味着我们可以直接通过cmd命令使⽤scala了。

打开cmd窗框，输⼊scala，然后按回⻋，即可进⼊scala的命令⾏⼯具。依次输⼊scala -version、 scala的显示如下。

![image 1](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile1.png>)

这⾥有必要说⼀下，我在安装scala之前已经安装了JDK1.8。因为Scala运⾏在JVM上，所以需要提 前安装好JDK。scala程序在执⾏的时候会被编译成JAVA字节码，然后字节码交由JAVA虚拟机执⾏。

第⼆部分：在IDEA编辑器上安装scala插件并创建maven项⽬ 说明：在IDEA编辑器上安装scala，⾸先需要你安装好了IDEA编辑器。 ⽬前scala官⽅⽀持三种编辑器，⼀个是Scala IDE，另外两个是IDEA和NetBeans。针对IDEA和 NetBeans放出了两个插件，分别是 InteliJ IDEA with the Scala plugin 和 NetBeans IDE with the Scala plugin。接下来，我们要演示如何在IDEA上安装scala的插件。

- 1、打开IDEA编辑器，依次点击File ->Seting。 然后依次执⾏以下操作：第⼀步，在新弹出窗⼝中输⼊plugin关键字。第⼆步，在plugins窗⼝输⼊

框中输⼊scala。如果第⼆步没有任何结果显示，执⾏第三步：点击browse。

- 2、在插件市场寻找scala插件。 第⼀步：在新窗⼝中，滑动滚动条，找到scala条⽬，并选中。 第⼆步：在右侧的窗⼝中，确实是否是scala plugin for IDEA 第三步：点击Instal Plugin。经过⼀段时间的等待，就能下载并安装完成。安装完成后需要重新启


![image 2](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile2.png>)

动下IDEA编辑器。

![image 3](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile3.png>)

- 3、在IDEA中创建scala项⽬ 第⼀步：依次操作File ->new->project。在project类型⾯板中，选中maven。然后图⽚中的⼀⼆


三进⾏操作。之后点击next。

![image 4](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile4.png>)

第⼆步：输⼊项⽬信息，然后⼀路下⼀步。

![image 5](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile5.png>)

第三步：输⼊项⽬名称，然后点击完成即可。

![image 6](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile6.png>)

第四步：点开项⽬的src⽬录，在main⽬录下创建⼀个⽂件夹scala。 选中scala⽂件夹，点击右键，依次选择Mark Directory As 和 Sources Rot。

![image 7](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile7.png>)

第五步：在scala⽂件夹下创建包路径：cn.ytcx.scala.learn

![image 8](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile8.png>)

第六步：在cn.ytcx.scala.learn创建⼀个File，⽂件名的后缀⼀定要是scala。⼀定要按照此步骤操 作，切记。

![image 9](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile9.png>)

![image 10](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile10.png>)

第七步：创建完⽂件之后，注意观察编辑器的右上⻆，会出现⼀个配置scala环境的选项框。选择你 安装的scala⽬录就可以了。

![image 11](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile11.png>)

![image 12](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile12.png>)

第⼋步：编写内容，并运⾏LocalPi.scala类，和运⾏java类⼀样的操作。

package cn.ytcx.scala.learn; import scala.math.random object LocalPi { def main(args: Array[String]) { var count = 0 for (i <- 1 to 100000) {

- val x = random* 2 - 1

- val y = random* 2 - 1 if (x*x + y*y < 1) count += 1 } println("Pi is roughly " + 4 * count / 100000.0)


}

}

运⾏结果如下：

![image 13](<第1章 在windows下安装scala、在IDEA编辑器上安装scala插件及创建maven项目.note_images/imageFile13.png>)

附录：

- 1、《scala的百度百科》：

- 2、《scala⽹站》：

- 3、《scala api 》：


htp:/baike.baidu.com/link?url=Tt74e8Eo3J7fTPG-SjKq94JSq3V3vbKH

hREMDtP3ZKj0HnG_17QEDhrC8RwCbOqS3UIbcy56ZKpIn8WoUCSK htp:/ w.scala-lang.org/ htp:/ w.scala-lang.org/api/curent/#package

