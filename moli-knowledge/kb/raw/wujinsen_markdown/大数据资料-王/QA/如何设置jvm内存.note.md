本⽂向⼤家简单介绍⼀下进⾏JVM内存设置⼏种⽅法，安装Java开发软件时，默认安装包含两个 ⽂件夹，⼀个JDK(Java开发⼯具箱)，⼀个JRE(Java运⾏环境，内含JVM)，其中JDK内另含⼀个 JRE。如果只是运⾏Java程序，则JRE已⾜够；⽽JDK则只有开发⼈员才⽤到。这⾥将为⼤家介绍 设置JVM内存分配的⼏招。

# ⼯具/原料

⼀台电脑

# ⽅法/步骤

- 1.
- 2.


- -Xmx Java Heap最⼤值,默认值为物理内存的1/4,最佳设值应该视物理内存⼤⼩及计算机内其

他内存开销⽽定;

- -Xms Java Heap初始值,Server端JVM最好将-Xms和-Xmx设为相同值,开发测试机JVM可以保


留默认值;

-Xmn Java Heap Young区⼤⼩,不熟悉最好保留默认值; -Xs 每个线程的Stack⼤⼩,不熟 悉最好保留默认值;

2. 如何分配JVM内存设置:

(1)当在命令提示符下启动并使⽤JVM时(只对当前运⾏的类Test⽣效): java -Xmx128m Xms64m -Xmn32m -Xs16m Test (2)当在集成开发环境下(如eclipse)启动并使⽤JVM时:

a. 在eclipse根⽬录下打开eclipse.ini,默认内容为(这⾥设置的是运⾏当前开发⼯具的JVM内存分 配): -vmargs -Xms40m -Xmx256m

-vmargs表示以下为虚拟机设置参数,可修改其中的参数值,也可添加-Xmn,-Xs,另外,eclipse.ini内 还可以设置⾮堆内存,如:-X PermSize=56m,-X MaxPermSize=128m.

此处设置的参数值可以通过以下配置在开发⼯具的状态栏显示: 在eclipse根⽬录下创建⽂件 options,⽂件内容为:org.eclipse.ui/perf/showHeapStatus=true

修改eclipse根⽬录下的eclipse.ini⽂件,在开头处添加如下内容: -debug options -vm javaw.exe

重新启动eclipse,就可以看到下⽅状态条多了JVM信息.

- b. 打开eclipse-窗⼝-⾸选项-Java-已安装的JRE(对在当前开发环境中运⾏的java程序皆⽣效) 编辑当前使⽤的JRE,在缺省VM参数中输⼊:-Xmx128m -Xms64m -Xmn32m -Xs16m

- c. 打开eclipse-运⾏-运⾏-Java应⽤程序(只对所设置的java类⽣效) 选定需设置内存分配的


类-⾃变量,在VM⾃变量中输⼊:-Xmx128m -Xms64m

选定需设置内存分配的类-⾃变量,在VM⾃变量中输⼊:-Xmx128m -Xms64m -Xmn32m -Xs16m

注:如果在同⼀开发环境中同时进⾏了b和c设置,则b设置⽣效,c设置⽆效,如:

开发环境的设置为:-Xmx256m,⽽类Test的设置为:-Xmx128m -Xms64m,则运⾏Test时⽣效的 设置为: -Xmx256m -Xms64m

(3)当在服务器环境下(如Tomcat)启动并使⽤JVM时(对当前服务器环境下所以Java程序⽣效):

- a. 设置环境变量: 变量名:CATALINA_OPTS 变量值:-Xmx128m -Xms64m -Xmn32m -Xs16m

3

- b. 打开Tomcat根⽬录下的bin⽂件夹,编辑catalina.bat,将其中 的%CATALINA_OPTS%(共有四处)替换为:-Xmx128m -Xms64m -Xmn32m -Xs16m


- 3.


