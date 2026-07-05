---
title: cpu 100%.note（原文插图 annex）
slug: annex-cpu-100%
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/jvm/cpu 100%.note.md
related: [jvm-gc调优实战]
created: 2026-07-05
updated: 2026-07-05
---

htps:/my.oschina.net/l ejun205/blog/1524687

- 0、背景

经常做后端服务开发的同学，或多或少都遇到过 CPU 负载特别⾼的问题。尤其是在周末或⼤半夜，突 然群⾥有⼈反馈线上机器负载特别⾼，不熟悉定位流程和思路的同学可能登上服务器⼀通⼿忙脚乱， 定位过程百转千回。

对此，也有不少同学曾经整理过相关流程或⽅法论，类似把⼤象放进冰箱要⼏步，传统的⽅案⼀般是4 步：

但是对于线上问题定位来说，分秒必争，上⾯的 4 步还是太繁琐耗时了，有没有可能封装成为⼀个⼯ 具，在有问题的时候⼀键定位，秒级找到有问题的代码⾏呢？ 当然可以！⼯具链的成熟与否不仅体现了⼀个开发者的运维能⼒，也体现了开发者的效率意识。

就将上⾯的流程封装为了⼀个⼯具： .sh（点击可直接下载， 或参考⽂末链接下载），可以很⽅便的定位线上的这类问题，下⾯我会举两个例⼦来看实际的效果。 快速安装使⽤：

source <(curl -fsSL https://raw.githubusercontent.com/oldratlee/useful-scripts/master/testcases/self-installer.sh)

- 1、java 正则表达式回溯造成 CPU 10%


![image 1](assets/imageFile1.png)

- 1.
- 2.
- 3.
- 4.


top oder by with P：1040/ ⾸先按进程负载排序找到 axLoad(pid) top -Hp 进程PID：1073 / 找到相关负载 线程PID printf “0x%x\n”线程PID： 0x431 / 将线程PID转换为 16进制，为后⾯查找 jstack ⽇志做准备 jstack 进程PID | vim +/⼗六进制线程PID - / 例如：jstack 1040|vim +/0x431 -

淘宝 的oldratl e 同学 show-busy-java-threads

import java.util.ArrayList; import java.util.List; import java.util.regex.Matcher; import java.util.regex.Pattern;

public class RegexLoad { public static void main(String[] args) { String[] patternMatch = {"([\\w\\s]+)+([+\\-/*])+([\\w\\s]+)",

"([\\w\\s]+)+([+\\-/*])+([\\w\\s]+)+([+\\-/*])+([\\w\\s]+)"}; List<String> patternList = new ArrayList<String>();

patternList.add("Avg Volume Units product A + Volume Units product A"); patternList.add("Avg Volume Units / Volume Units product A"); patternList.add("Avg retailer On Hand / Volume Units Plan / Store Count"); patternList.add("Avg Hand Volume Units Plan Store Count"); patternList.add("1 - Avg merchant Volume Units"); patternList.add("Total retailer shipment Count");

for (String s :patternList ){

for(int i=0;i<patternMatch.length;i++){ Pattern pattern = Pattern.compile(patternMatch[i]);

Matcher matcher = pattern.matcher(s); System.out.println(s); if (matcher.matches()) {

System.out.println("Passed"); }else

System.out.println("Failed;"); }

} }

}

编译、运⾏上述代码之后，咱们就能观察到服务器多了⼀个 10% CPU 的 java 进程：

![image 2](assets/imageFile2.png)

怎么使⽤呢？ show-busy-java-threads.sh # 从 所有的 Java进程中找出最消耗CPU的线程（缺省5个），打印出其线程栈。 show-busy-java-threads.sh -c <要显示的线程栈数> show-busy-java-threads.sh -c <要显示的线程栈数> -p <指定的Java Proces> # -F选项：执⾏jstack命令时加上-F选项（强制jstack），⼀般情况不需要使⽤ show-busy-java-threads.sh -p <指定的Java Proces> -F

- show-busy-java-threads.sh -s <指定jstack命令的全路径> # 对于sudo⽅式的运⾏，JAVA_HOME环境变量不能传递给rot， # ⽽rot⽤户往往没有配置JAVA_HOME且不⽅便配置， # 显式指定jstack命令的路径就反⽽显得更⽅便了 show-busy-java-threads.sh -a <输出记录到的⽂件>

- show-busy-java-threads.sh -t <重复执⾏的次数> -i <重复执⾏的间隔秒数> # 缺省执⾏⼀次；执⾏间隔缺省是3秒


# # 注意：

# # 如果Java进程的⽤户 与 执⾏脚本的当前⽤户 不同，则jstack不了这个Java进程。 # 为了能切换到Java进程的⽤户，需要加sudo来执⾏，即可以解决： sudo show-busy-java-threads.sh 示例：

- work@dev_zz_Master 10.48.186.32 23:45:50 ~/demo > bash show-busy-java-threads.sh

- [1] Busy(96.2%) thread(8577/0x2181) stack of java process(8576) under user(work): "main" prio=10 tid=0x00007f0c64006800 nid=0x2181 runnable [0x00007f0c6a64a000]

java.lang.Thread.State: RUNNABLE at java.util.regex.Pattern$GroupHead.match(Pattern.java:4168) at java.util.regex.Pattern$Loop.match(Pattern.java:4295)

... at java.util.regex.Matcher.match(Matcher.java:1127) at java.util.regex.Matcher.matches(Matcher.java:502) at RegexLoad.main(RegexLoad.java:27)

- [2] Busy(1.5%) thread(8591/0x218f) stack of java process(8576) under user(work): "C2 CompilerThread1" daemon prio=10 tid=0x00007f0c64095800 nid=0x218f waiting on condition [0x0000000000000000]

java.lang.Thread.State: RUNNABLE

- [3] Busy(0.8%) thread(8590/0x218e) stack of java process(8576) under user(work): "C2 CompilerThread0" daemon prio=10 tid=0x00007f0c64093000 nid=0x218e waiting on condition [0x0000000000000000]

java.lang.Thread.State: RUNNABLE

- [4] Busy(0.2%) thread(8593/0x2191) stack of java process(8576) under user(work): "VM Periodic Task Thread" prio=10 tid=0x00007f0c640a2800 nid=0x2191 waiting on condition

- [5] Busy(0.1%) thread(25159/0x6247) stack of java process(25137) under user(work): "VM Periodic Task Thread" prio=10 tid=0x00007f13340b4000 nid=0x6247 waiting on condition


- work@dev_zz_Master 10.48.186.32 23:46:04 ~/demo > 可以看到，⼀键直接定位异常代码⾏，是不是很⽅便？


# 2、线程死锁，程序 hang 住

import java.util.*; public class SimpleDeadLock extends Thread {

public static Object l1 = new Object(); public static Object l2 = new Object(); private int index; public static void main(String[] a) {

- Thread t1 = new Thread1();

- Thread t2 = new Thread2();


- t1.start();

- t2.start();


} private static class Thread1 extends Thread {

public void run() {

synchronized (l1) { System.out.println("Thread 1: Holding lock 1..."); try { Thread.sleep(10); } catch (InterruptedException e) {} System.out.println("Thread 1: Waiting for lock 2..."); synchronized (l2) {

System.out.println("Thread 2: Holding lock 1 & 2..."); }

} }

} private static class Thread2 extends Thread {

public void run() {

synchronized (l2) { System.out.println("Thread 2: Holding lock 2..."); try { Thread.sleep(10); } catch (InterruptedException e) {} System.out.println("Thread 2: Waiting for lock 1..."); synchronized (l1) {

System.out.println("Thread 2: Holding lock 2 & 1..."); }

} }

} }

执⾏之后的效果：

![image 3](assets/imageFile3.png)

如何⽤⼯具定位：

![image 4](assets/imageFile4.png)

⼀键定位：可以清晰的看到线程互相锁住了对⽅等待的资源，导致死锁，直接定位到代码⾏和具体原 因。 通过上⾯两个例⼦，我想各位同学应该对这个⼯具和⼯具能解决什么问题有了⽐较深刻的了解了，遇 到 CPU 10% 问题可以从此不再慌乱。但是更多的还是依赖⼤家⾃⼰去实践，毕竟实践出真知嘛~

# 3、免费实⽤的脚本⼯具⼤礼包

除了正⽂提到的 show-busy-java-threads.sh，oldratl e 同学还整合和不少常⻅的开发、运维过程中 涉及到的脚本⼯具，觉得特别有⽤的我简单列下：

- （1）show-duplicate-java-clases


偶尔会遇到本地开发、测试都正常，上线后却莫名其妙的 clas 异常，历经千⾟万苦找到的原因竟然是 Jar冲突！这个⼯具就可以找出Java Lib（Java库，即Jar⽂件）或Clas⽬录（类⽬录）中的重复类。 Java开发的⼀个麻烦的问题是Jar冲突（即多个版本的Jar），或者说重复类。会出NoSuchMethod等 的问题，还不⻅得当时出问题。找出有重复类的Jar，可以防患未然。 # 查找当前⽬录下所有Jar中的重复类 show-duplicate-java-clases # 查找多个指定⽬录下所有Jar中的重复类 show-duplicate-java-clases path/to/lib_dir1 /path/to/lib_dir2 # 查找多个指定Clas⽬录下的重复类。 Clas⽬录 通过 -c 选项指定 show-duplicate-java-clases -c path/to/clas_dir1 -c /path/to/clas_dir2 # 查找指定Clas⽬录和指定⽬录下所有Jar中的重复类的Jar show-duplicate-java-clases path/to/lib_dir1 /path/to/lib_dir2 -c path/to/clas_dir1 -c path/to/clas_dir2 例如：

# 在war模块⽬录下执⾏，⽣成war⽂件 $ mvn install

... # 解压war⽂件，war⽂件中包含了应⽤的依赖的Jar⽂件 $ unzip target/*.war -d target/war

... # 检查重复类 $ show-duplicate-java-classes -c target/war/WEB-INF/classes target/war/WEB-INF/lib

...

## （2）find-in-jars

在当前⽬录下所有jar⽂件⾥，查找类或资源⽂件。 ⽤法：注意，后⾯Patern是grep的 扩展正则表达式。

find-in-jars 'log4j\.properties' find-in-jars 'log4j\.xml$' -d /path/to/find/directory find-in-jars log4j\\.xml find-in-jars 'log4j\.properties|log4j\.xml'

示例：

$ ./find-in-jars 'Service.class$'

./WEB-INF/libs/spring-2.5.6.SEC03.jar!org/springframework/stereotype/Service.class

./rpc-benchmark-0.0.1-SNAPSHOT.jar!com/taobao/rpc/benchmark/service/HelloService.class

## （3）housemd pid [java_home]

很早的时候，我们使⽤BTrace排查问题，在感叹BTrace的强⼤之余，也曾好⼏次将线上系统折腾挂 掉。2012年淘宝的聚⽯写了HouseMD，将常⽤的⼏个Btrace脚本整合在⼀起形成⼀个独⽴⻛格的应 ⽤，其核⼼代码⽤的是Scala，HouseMD是基于字节码技术的诊断⼯具, 因此除了Java以外, 任何最终 以字节码形式运⾏于JVM之上的语⾔, HouseMD都⽀持对它们进⾏诊断, 如Clojure(感谢@Kilme208 提供了它的使⽤⼊⻔), scala, Grovy, JRuby, Jython, kotlin等. 使⽤housemd对java程序进⾏运⾏时跟踪，⽀持的操作有：

查看加载类

跟踪⽅法

查看环境变量

查看对象属性值

详细信息请参考: htps:/github.com/CSUG/HouseMD/wiki/UserGuideCN

## （4）jvm pid

执⾏jvm debug⼯具，包含对java栈、堆、线程、gc等状态的查看，⽀持的功能有：

=线程相关 =

- 1 :查看占⽤cpu最⾼的线程情况

- 2 :打印所有线程

- 3 :打印线程数

- 4 :按线程状态统计线程数

=GC相关 =

- 5 :垃圾收集统计（包含原因）可以指定间隔时间及执⾏次数，默认1秒, 10次

- 6 :显示堆中各代的空间可以指定间隔时间及执⾏次数，默认1秒，5次

- 7 :垃圾收集统计。可以指定间隔时间及执⾏次数，默认1秒, 10次

- 8 :打印perm区内存情况*会使程序暂停响应*

- 9 :查看directbufer情况

=堆对象相关 =

- 10 :dumpheap到⽂件*会使程序暂停响应*默认保存到`pwd`/dump.bin,可指定其它路径 1 :触发fulgc。*会使程序暂停响应*


- 12 :打印jvmheap统计*会使程序暂停响应*

- 13 :打印jvmheap中top20的对象。*会使程序暂停响应*参数：1:按实例数量排序,2:按内存占⽤排序，默认为1

- 14 :触发fulgc后打印jvmheap中top20的对象。*会使程序暂停响应*参数：1:按实例数量排序,2:按内存占⽤排序，默认为1

- 15 :输出所有类装载器在perm⾥产⽣的对象。可以指定间隔时间及执⾏次数

=其它 =

- 16 :打印finalzer队列情况

- 17 :显示clasloader统计

- 18 :显示jit编译统计

- 19 :死锁检测

- 20 :等待X秒，默认为1 q :exit 进⼊jvm⼯具后可以输⼊序号执⾏对应命令 可以⼀次执⾏多个命令，⽤分号";"分隔，如：1;3;4;5;6 每个命令可以带参数，⽤冒号":"分隔，同⼀命令的参数之间⽤逗号分隔，如： Enter comand queue:1;5 1 0,10;10:/data1/output.bin


## （5）greys <PID>[ :PORT]

## @IP

PS：⽬前Greys仅⽀持Linux/Unix/Mac上的Java6+，Windows暂时⽆法⽀持 Greys是⼀个JVM进程执⾏过程中的异常诊断⼯具，可以在不中断程序执⾏的情况下轻松完成问题排查 ⼯作。和HouseMD⼀样，Greys-Anatomy取名同名美剧“实习医⽣格蕾”，⽬的是向前辈致敬。代码编 写的时候参考了BTrace和HouseMD两个前辈的思路。 使⽤greys对java程序进⾏运⾏时跟踪(不传参数，需要先greys -C pid,再greys)。⽀持的操作有：

查看加载类，⽅法信息 查看JVM当前基础信息 ⽅法执⾏监控（调⽤量，失败率，响应时间等）

⽅法执⾏数据观测、记录与回放（参数，返回结果，异常信息等）

⽅法调⽤追踪渲染

详细信息请参考: htps:/github.com/oldmanpushcart/greys-anatomy/wiki

- （6）sjk <cmd> <arguments> sjk-co mands sjk-help


<cmd>

使⽤sjk对Java诊断、性能排查、优化⼯具

top:监控指定jvm进程的各个线程的cpu使⽤情况

jps: 强化版

h: jmap -histo强化版

gc: 实时报告垃圾回收信息

更多信息请参考: htps:/github.com/aragozin/jvm-tols

# Refer：

- [1] oldratl e/useful-scripts
- [2] awesome-scripts
- [3] JDK⾃带⼯具之问题排查场景示例
- [4] Java调优经验谈
- [5] jvm排查⼯具箱jvm-tols
- [6] alibaba/arthas


htps:/github.com/oldratl e/useful-scripts

htps:/github.com/superhj1987/awesome-scripts

htp:/bit.ly/2xtukcb

htp:/bit.ly/2xCIj2L

htps:/segmentfault.com/a/19 01265814

htps:/github.com/alibaba/arthas/blob/7f236219dbd040764d821cbcbd489d57c90/READM E.md
