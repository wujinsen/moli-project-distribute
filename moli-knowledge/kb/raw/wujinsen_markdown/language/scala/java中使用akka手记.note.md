什么是actor?

Actor模型在并发编程中是⽐较常⻅的⼀种模型。很多开发语⾔都提供了原⽣的Actor模型。例如 erlang,scala等。 它由Carl Hewit于上世纪70年代早期提出，⽬的是为了解决分布式编程中⼀系列的编程问题。 Actor模型的本质已经被强调了⽆数遍：万物皆Actor。Actor之间只有发送消息这⼀种通信⽅式。 ⼀个Actor如何处理多个Actor的请求呢？它先建⽴⼀个消息队列，每次收到消息后，就放⼊队列， ⽽它每次也从队列中取出消息体来处理。通常我们都使得这个过程是循环的。让Actor可以时刻处理 发送来的消息。

什么是aka?

Aka是⼀个⽤Scala编写的库，⽤于简化编写容错的、⾼可伸缩性的Java和Scala的Actor模型应⽤。

下⾯以在java项⽬中使⽤aka写代码为例⼦。

依赖

maven项⽬ java6 or 7 添加aka相关的包

<table>
  <tr>
    <th> </th>
    <th>com.typesafe.aka aka-actor_2.10 2.3.1 com.typesafe.aka aka-remote_2.10 2.3.1 com.gogle.protobuf protobuf-java</th>
  </tr>
</table>


2.5.0 com.typesafe.aka aka-testkit_2.10 2.3.1

依赖包解析

aka-actor 核⼼包，有这个包就可以写简单的代码了 aka-remote 远程包，有这个包，才能够跨进程和⽹络调⽤ protobuf-java 不解释了，之所有是要声明版本，是因为pb的版本太低会造成消息传递过程中序列 化反序列化有问题 aka-testkit 测试集，有这个包，写test case⽅便

常⻅问题

Q:shuting down JVM since ‘aka.jvm-exit-on-fatal-erorʼ is enabled A:所有出错的时候都会有这个提示，快速错误退出是⼀个常⻅的机制，让系统最快时间发现错误。 Q:java.lang.ClasNotFoundException: aka.remote.RemoteActorRefProvider A:没有添加进来aka-remote的时候会这样 Q:clas aka.remote.WireFormats$AkaControlMesage overides final method getUnknownFiel ds.()Lcom/gogle/protobuf/UnknownFieldSet A:这是pb版本太低导致的，声明到2.5.0或以上

例⼦

这是typesafe的经典例⼦。 所有actor的配置都在claspath中。 此例启动了两个system（简称为worker与creator）： startRemoteWorkerSystem & startRemoteCreationSystem

worker使⽤calculator.conf，在252端⼝侦听。 creator使⽤remotecreation.conf，定义了它的worker在远程的252端⼝，路径在creationActor 下，⾃⼰的端⼝为254。

creator中的逻辑

creator启动后，调⽤了schedule，进⾏了⼀秒⼀次的随机调⽤乘法或除法。 具体的计算，在creationActor这个actor中完成。 ⽽creationActor这个actor被定义到了远程252端⼝的进程中执⾏。

<table>
  <tr>
    <th> </th>
    <th>aka { actor { deployment { "/creationActor/*" { remote =</th>
  </tr>
</table>


"aka.tcp:/CalculatorWorkerSystem@127.0.0.1 252" } } }

运⾏中进程观察

run CreationAplication.java ⼀个进程 启动了⼀个端⼝ 进程通过这个端⼝，产⽣随机算式，交给另⼀个进程（这⾥是同⼀个进程）。

代码

本⽂提及代码在 htps:/github.com/XiaoMi/rose/tre/master/rose-example

