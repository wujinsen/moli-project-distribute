---
title: Java后台面试 常见问题.note（原文插图 annex）
slug: annex-Java后台面试-常见问题
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/面试笔试/面试题整理/Java后台面试 常见问题.note.md
related: [mysql-复合索引与最左前缀]
created: 2026-07-05
updated: 2026-07-05
---

从三⽉份找实习到现在，⾯了⼀些公司，挂了不少，但最终还是拿到⼩⽶、百度、阿⾥、京东、新 浪、CVTE、乐视家的研发岗ofer。我找的是java后台开发，把常⻅的问题分享给⼤家，有⼀些是⾃⼰ 的总结，有⼀些是⽹上借鉴的内容。希望能帮助到各位。预祝各位同学拿到⾃⼰⼼仪的ofer！

## Nginx负载均衡

轮询、轮询是默认的，每⼀个请求按顺序逐⼀分配到不同的后端服务器，如果后端服务器down掉 了，则能⾃动剔除

ip_hash、个请求按访问IP的hash结果分配，这样来⾃同⼀个IP的访客固定访问⼀个后端服务器，有 效解决了动态⽹⻚存在的sesion共享问题。

weight、weight是设置权重，⽤于后端服务器性能不均的情况，访问⽐率约等于权重之⽐

fair(第三⽅)、这是⽐上⾯两个更加智能的负载均衡算法。此种算法可以依据⻚⾯⼤⼩和加载时间⻓ 短智能地进⾏负载均衡，也就是根据后端服务器的响应时间来分配请求，响应时间短的优先分配。 Nginx本身是不⽀持fair的，如果需要使⽤这种调度算法，必须下载Nginx的upstream_fair模块。

url_hash(第三⽅)此⽅法按访问url的hash结果来分配请求，使每个url定向到同⼀个后端服务器，可 以进⼀步提⾼后端缓存服务器的效率。Nginx本身是不⽀持url_hash的，如果需要使⽤这种调度算 法，必须安装Nginx 的hash软件包。

代理的概念 正向代理，也就是传说中的代理, 简单的说，我是⼀个⽤户，我访问不了某⽹站，但是我能访问⼀个代 理服务器，这个代理服务器呢，他能访问那个我不能访问的⽹站，于是我先连上代理服务器，告诉他 我需要那个⽆法访问⽹站的内容，代理服务器去取回来，然后返回给我。从⽹站的⻆度，只在代理服 务器来取内容的时候有⼀次记录，有时候并不知道是⽤户的请求，也隐藏了⽤户的资料，这取决于代 理告不告诉⽹站。 反向代理： 结论就是，反向代理正好相反，对于客户端⽽⾔它就像是原始服务器，并且客户端不需要 进⾏任何特别的设置。客户端向反向代理的命名空间(name-space)中的内容发送普通请求，接着反向 代理将判断向何处(原始服务器)转交请求，并将获得的内容返回给客户端，就像这些内容原本就是它⾃ ⼰的⼀样。

## Volatile的特征：

- A、原⼦性 ：对任意单个volatile变量的读/写具有原⼦性，但类似于volatile+这种复合操作不具有原⼦ 性。
- B、可⻅性：对⼀个volatile变量的读，总是能看到（任意线程）对这个volatile变量最后的写⼊。


## Volatile的内存语义：

当写⼀个volatile变量时，J M会把线程对应的本地内存中的共享变量值刷新到主内存。

<table>
  <tr>
    <th>![image 1](assets/imageFile1.png)</th>
  </tr>
</table>


#### 当读⼀个volatile变量时，J M会把线程对应的本地内存置为⽆效，线程接下来将从主内存中读取共享 变量。

<table>
  <tr>
    <th>![image 2](assets/imageFile2.png)</th>
  </tr>
</table>


## Volatile的重排序

- 1、当第⼆个操作为volatile写操做时,不管第⼀个操作是什么(普通读写或者volatile读写),都不能进⾏重 排序。这个规则确保volatile写之前的所有操作都不会被重排序到volatile之后;
- 2、当第⼀个操作为volatile读操作时,不管第⼆个操作是什么,都不能进⾏重排序。这个规则确保volatile 读之后的所有操作都不会被重排序到volatile之前;
- 3、当第⼀个操作是volatile写操作时,第⼆个操作是volatile读操作,不能进⾏重排序。 这个规则和前⾯两个规则⼀起构成了:两个volatile变量操作不能够进⾏重排序； 除以上三种情况以外可以进⾏重排序。 ⽐如：


- 1、第⼀个操作是普通变量读/写,第⼆个是volatile变量的读；
- 2、第⼀个操作是volatile变量的写,第⼆个是普通变量的读/写；


## 内存屏障/内存栅栏

内存屏障（Memory Barier，或有时叫做内存栅栏，Memory Fence）是⼀种CPU指令，⽤于控制 特定条件下的重排序和内存可⻅性问题。 编译器也会根据内存屏障的规则禁⽌重排序。（也就 是让⼀个CPU处理单元中的内存状态对其它处理单元可⻅的⼀项技术。） 内存屏障可以被分为以下⼏种类型： LoadLoad屏障：对于这样的语句Load1; LoadLoad; Load2，在Load2及后续读取操作要读取的数 据被访问前，保证Load1要读取的数据被读取完毕。 StoreStore屏障：对于这样的语句Store1; StoreStore; Store2，在Store2及后续写⼊操作执⾏前， 保证Store1的写⼊操作对其它处理器可⻅。 LoadStore屏障：对于这样的语句Load1; LoadStore; Store2，在Store2及后续写⼊操作被刷出前， 保证Load1要读取的数据被读取完毕。 StoreLoad屏障：对于这样的语句Store1; StoreLoad; Load2，在Load2及后续所有读取操作执⾏ 前，保证Store1的写⼊对所有处理器可⻅。它的开销是四种屏障中最⼤的。 在⼤多数处理器的实现中，这个屏障是个万能屏障，兼具其它三种内存屏障的功能。 内存屏障阻碍了CPU采⽤优化技术来降低内存操作延迟，必须考虑因此带来的性能损失。为了达到 最佳性能，最好是把要解决的问题模块化，这样处理器可以按单元执⾏任务，然后在任务单元的边 界放上所有需要的内存屏障。采⽤这个⽅法可以让处理器不受限的执⾏⼀个任务单元。合理的内存 屏障组合还有⼀个好处是：缓冲区在第⼀次被刷后开销会减少，因为再填充改缓冲区不需要额外⼯ 作了。

Java

## hapens-before原则

如果⼀个操作执⾏的结果需要对另⼀个操作可⻅，那么这两个操作之间必须要存在hapens-before关 系。

<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


Java是如何实现跨平台的？

跨平台是怎样实现的呢？这就要谈及Java虚拟机（ Virtual Machine，简称 JVM）。 JVM也是⼀个软件，不同的平台有不同的版本。我们编写的Java源码，编译后会⽣成⼀种 .clas ⽂件，称为字节码⽂件。Java虚拟机就是负责将字节码⽂件翻译成特定平台下的机器码然后运⾏。 也就是说，只要在不同平台上安装对应的JVM，就可以运⾏字节码⽂件，运⾏我们编写的Java程 序。 ⽽这个过程中，我们编写的Java程序没有做任何改变，仅仅是通过JVM这⼀”中间层“，就能在不同 平台上运⾏，真正实现了”⼀次编译，到处运⾏“的⽬的。 JVM是⼀个”桥梁“，是⼀个”中间件“，是实现跨平台的关键，Java代码⾸先被编译成字节码⽂件， 再由JVM将字节码⽂件翻译成机器语⾔，从⽽达到运⾏Java程序的⽬的。 注意：编译的结果不是⽣成机器码，⽽是⽣成字节码，字节码不能直接运⾏，必须通过JVM翻译成 机器码才能运⾏。不同平台下编译⽣成的字节码是⼀样的，但是由JVM翻译成的机器码却不⼀样。 所以，运⾏Java程序必须有JVM的⽀持，因为编译的结果不是机器码，必须要经过JVM的再次翻译 才能执⾏。即使你将Java程序打包成可执⾏⽂件（例如 .exe），仍然需要JVM的⽀持。 注意：跨平台的是Java程序，不是JVM。JVM是⽤C/C+开发的，是编译后的机器码，不能跨平 台，不同平台下需要安装不同版本的JVM。

Java

垃圾搜集器

- 1.

- a.
- b.


- 2.

- a.
- b.


- 3.

- a.
- b.


- 4.


按照线程数量来分： 串⾏ 串⾏垃圾回收器⼀次只使⽤⼀个线程进⾏垃圾回收 并⾏ 并⾏垃圾回收器⼀次将开启多个线程同时进⾏垃圾回收。

按照⼯作模式来分： 并发 并发式垃圾回收器与应⽤程序线程交替⼯作，以尽可能减少应⽤程序的停顿时间 独占 ⼀旦运⾏，就停⽌应⽤程序中的其他所有线程，直到垃圾回收过程完全结束

按照碎⽚处理⽅式： 压缩式 压缩式垃圾回收器会在回收完成后，对存活对象进⾏压缩整消除回收后的碎⽚； ⾮压缩式 ⾮压缩式的垃圾回收器不进⾏这步操作。

按⼯作的内存区间 可分为新⽣代垃圾回收器和⽼年代垃圾回收器

新⽣代串⾏收集器 serial 它仅仅使⽤单线程进⾏垃圾回收；第⼆，它独占式的垃圾回收。使⽤复制 算法。

⽼年代串⾏收集器 serial old 年代串⾏收集器使⽤的是标记-压缩算法。和新⽣代串⾏收集器⼀样， 它也是⼀个串⾏的、独占式的垃圾回收器

并⾏收集器 parnew 并⾏收集器是⼯作在新⽣代的垃圾收集器，它只简单地将串⾏回收器多线程 化。它的回收策略、算法以及参数和串⾏回收器⼀样 并⾏回收器也是独占式的回收器，在收集过程 中，应⽤程序会全部暂停。但由于并⾏回收器使⽤多线程进⾏垃圾回收，因此，在并发能⼒⽐较强 的 CPU 上，它产⽣的停顿时间要短于串⾏回收器，⽽在单 CPU 或者并发能⼒较弱的系统中，并⾏ 回收器的效果不会⽐串⾏回收器好，由于多线程的压⼒，它的实际表现很可能⽐串⾏回收器差。

新⽣代并⾏回收 (Paralel Scavenge) 收集器 新⽣代并⾏回收收集器也是使⽤复制算法的收集器。从 表⾯上看，它和并⾏收集器⼀样都是多线程、独占式的收集器。但是，并⾏回收收集器有⼀个重要 的特点：它⾮常关注系统的吞吐量。

⽼年代并⾏回收收集器 paralel old ⽼年代的并⾏回收收集器也是⼀种多线程并发的收集器。和新⽣ 代并⾏回收收集器⼀样，它也是⼀种关注吞吐量的收集器。⽼年代并⾏回收收集器使⽤标记-压缩算 法，JDK1.6 之后开始启⽤。

CMS 收集器 CMS 收集器主要关注于系统停顿时间。CMS 是 Concurent Mark Swep 的缩写，意 为并发标记清除，从名称上可以得知，它使⽤的是标记-清除算法，同时它⼜是⼀个使⽤多线程并发 回收的垃圾收集器。

CMS ⼯作时，主要步骤有：初始标记、并发标记、重新标记、并发清除和并发重置。其中初始 标记和重新标记是独占系统资源的，⽽并发标记、并发清除和并发重置是可以和⽤户线程⼀起 执⾏的。因此，从整体上来说，CMS 收集不是独占式的，它可以在应⽤程序运⾏过程中进⾏垃 圾回收。

根据标记-清除算法，初始标记、并发标记和重新标记都是为了标记出需要回收的对象。并发清理则是 在标记完成后，正式回收垃圾对象；并发重置是指在垃圾回收完成后，重新初始化 CMS 数据结构和数 据，为下⼀次垃圾回收做好准备。并发标记、并发清理和并发重置都是可以和应⽤程序线程⼀起执⾏ 的。

G1 收集器 G1 收集器是基于标记-压缩算法的。因此，它不会产⽣空间碎⽚，也没有必要在收集完 成后，进⾏⼀次独占式的碎⽚整理⼯作。G1 收集器还可以进⾏⾮常精确的停顿控制。

# ⽹络基本概念

OSI模型

OSI 模型(Open System Interconection model)是⼀个由国际标准化组织 提出的概念模型,试图 供 ⼀个使各种不同的计算机和⽹络在世界范围内实现互联的标准框架。 它将计算机⽹络体系结构划分为七层,每层都可以 供抽象良好的接⼝。了解 OSI 模型有助于理解实际 上互联⽹络的⼯业标准⸺TCP/IP 协议。 OSI 模型各层间关系和通讯时的数据流向如图所示： OSI 模型.png 显然、如果⼀个东⻄想包罗万象、⼀般时不可能的；在实际的开发应⽤中⼀般时在此模型的基础上进 ⾏裁剪、整合！ 七层模型介绍

物理层：

物理层负责最后将信息编码成电流脉冲或其它信号⽤于⽹上传输；

eg：RJ45等将数据转化成0和1；

数据链路层:

数据链路层通过物理⽹络链路 供数据传输。不同的数据链路层定义了不同的⽹络和协 议特征,其中包 括物理编址、⽹络拓扑结构、错误校验、数据帧序列以及流控;

可以简单的理解为：规定了0和1的分包形式，确定了⽹络数据包的形式；

⽹络层

⽹络层负责在源和终点之间建⽴连接;

可以理解为，此处需要确定计算机的位置，怎么确定？IPv4，IPv6！

传输层

传输层向⾼层 提供可靠的端到端的⽹络数据流服务。

可以理解为：每⼀个应⽤程序都会在⽹卡注册⼀个端⼝号，该层就是端⼝与端⼝的通信！常⽤的（TCP／IP）协议；

会话层

会话层建⽴、管理和终⽌表示层与实体之间的通信会话；

建⽴⼀个连接（⾃动的⼿机信息、⾃动的⽹络寻址）;

表示层:

表示层 供多种功能⽤于应⽤层数据编码和转化,以确保以⼀个系统应⽤层发送的信息 可以被另⼀个系 统应⽤层识别;

可以理解为：解决不同系统之间的通信，eg：Linux下的QQ和Windows下的QQ可以通信；

应⽤层:

OSI 的应⽤层协议包括⽂件的传输、访问及管理协议(FTAM) ,以及⽂件虚拟终端协议(VIP)和公⽤管理 系统信息(CMIP)等;

规定数据的传输协议；

常⻅的应⽤层协议： 常⻅的应⽤层协议.png 互联⽹分层结构的好处: 上层的变动完全不影响下层的结构。

## TCP/IP协议基本概念

OSI 模型所分的七层,在实际应⽤中,往往有⼀些层被整合,或者功能分散到其他层去。TCP/IP 没有照搬 OSI 模型,也没有 ⼀个公认的 TCP/IP 层级模型,⼀般划分为三层到五层模型来 述 TCP/IP 协议。

在此描述⽤⼀个通⽤的四层模型来描述,每⼀层都和 OSI 模型有较强的相关性但是⼜可能会有交叉。

TCP/IP 的设计,是吸取了分层模型的精华思想⸺封装。每层对上⼀层 供服务的时 候,上⼀层的数 据结构是⿊盒,直接作为本层的数据,⽽不需要关⼼上⼀层协议的任何细节。

TCP/IP 分层模型的分层以以太⽹上传输 UDP 数据包如图所示; UDP 数据包.png

数据包

宽泛意义的数据包:每⼀个数据包都包含"标头"和"数据"两个部分."标头"包含本数据包的⼀些说明."数 据"则是本数据包的内容.

细分数据包：

应⽤程序数据包: 标头部分规定应⽤程序的数据格式.数据部分传输具体的数据内容.* ⸺对应上图 中的数据！ *

TCP/UDP数据包:标头部分包含双⽅的发出端⼝和接收端⼝. UDP数据包:'标头'⻓度:8个字节,"数据 包"总⻓度最⼤为6535字节,正好放进⼀个IP数据包. TCP数据包:理论上没有⻓度限制,但是,为了保 证⽹络传输效率,通常不会超过IP数据⻓度,确保单个包不会被分割. ⸺对应上图中的UDP数据！

IP数据包: 标头部分包含通信双⽅的IP地址,协议版本,⻓度等信息. '标头'⻓度:20~60字节,"数据包"总 ⻓度最⼤为6535字节. ⸺对应上图中的IP数据

以太⽹数据包: 最基础的数据包.标头部分包含了通信双⽅的MAC地址,数据类型等. '标头'⻓度:18字 节,'数据'部分⻓度:46~150字节. ⸺对应上图中的以太⽹数据

### 四层模型

- 1.
- 2.
- 3.
- 4.


⽹络接⼝层

⽹络接⼝层包括⽤于协作IP数据在已有⽹络介质上传输的协议。 它定义像地址解析协议(Adres Resolution Protocol,ARP)这样的协议, 供 TCP/IP 协议的数据结构和 实际物理硬件之间的接⼝。 可以理解为：确定了⽹络数据包的形式。

⽹间层

⽹间层对应于 OSI 七层参考模型的⽹络层，本层包含 IP 协议、RIP 协议(Routing Information Protocol,路由信息协议),负责数据的包装、寻址和路由。同时还包含⽹间控制报⽂协议(Internet Control Mesage Protocol,ICMP)⽤来 供⽹络诊断信息； 可以理解为：该层时确定计算机的位置。

传输层

传输层对应于 OSI 七层参考模型的传输层,它 供两种端到端的通信服务。其中 TCP 协议 (Transmision Control Protocol) 供可靠的数据流运输服务,UDP 协议(Use Datagram Protocol) 供 不可靠的⽤户数据报服务。

TCP:三次握⼿、四次挥⼿;UDP:只发不管别⼈收不收得到--任性哈

应⽤层

应⽤层对应于 OSI 七层参考模型的应⽤层和表达层； 不明⽩的再看看7层参考模型的描述。

### TCP/IP 协议族常⽤协议

应⽤层：TFTP，HTP，SNMP，FTP，SMTP，DNS，Telnet 等等

传输层：TCP，UDP

⽹络层：IP，ICMP，OSPF，EIGRP，IGMP

数据链路层：SLIP，CSLIP， P，MTU

重要的 TCP/IP 协议族协议进⾏简单介绍:

IP(Internet Protocol,⽹际协议)是⽹间层的主要协议,任务是在源地址和和⽬的地址之间传输数据。 IP 协议只是尽最⼤努⼒来传输数据包,并不保证所有的包都可以传输 到⽬的地,也不保证数据包的顺 序和唯⼀。

IP 定义了 TCP/IP 的地址,寻址⽅法,以及路由规则。现在⼴泛使⽤的 IP 协议有 IPv4 和 IPv6 两 种:IPv4 使⽤ 32 位⼆进制整数做地址,⼀般使⽤点分⼗进制⽅式表示,⽐如 192.168.0.1。

IP 地址由两部分组成,即⽹络号和主机号。故⼀个完整的 IPv4 地址往往表示 为 192.168.0.1/24 或192.168.0.1/25.25.25.0 这种形式。

IPv6 是为了解决 IPv4 地址耗尽和其它⼀些问题⽽研发的最新版本的 IP。使⽤ 128 位 整数表示 地址,通常使⽤冒号分隔的⼗六进制来表示,并且可以省略其中⼀串连续的 0, 如:fe80:20 1f:fe0 1。

⽬前使⽤并不多！

ICMP(Internet Control Mesage Protocol,⽹络控制消息协议)是 TCP/IP 的 核⼼协议之⼀,⽤于在 IP ⽹络中发送控制消息, 供通信过程中的各种问题反馈。 ICMP 直接使⽤ IP 数据包传输,但 ICMP 并 不被视为 IP 协议的⼦协议。常⻅的联⽹状态诊断⼯具⽐如依赖于 ICMP 协议;

TCP(TransmisionControlProtocol,传输控制协议)是⼀种⾯向连接的,可靠的, 基于字节流传输的通 信协议。TCP 具有端⼝号的概念,⽤来标识同⼀个地址上的不 同应⽤。 述 TCP 的标准⽂档是 RFC793。

UDP(UserDatagramProtocol,⽤户数据报协议)是⼀个⾯向数据报的传输层协 议。UDP 的传输是不 可靠的,简单的说就是发了不管,发送者不会知道⽬标地址 的数据通路是否发⽣拥塞,也不知道数据是 否到达,是否完整以及是否还是原来的 次序。它同 TCP ⼀样有⽤来标识本地应⽤的端⼝号。所以应 ⽤ UDP 的应⽤,都能 够容忍⼀定数量的错误和丢包,但是对传输性能敏感的,⽐如流媒体、DNS 等。 ECHO(EchoProtocol,回声协议)是⼀个简单的调试和检测⼯具。服务器器会 原样回发它收到的任何 数据,既可以使⽤ TCP 传输,也可以使⽤ UDP 传输。使⽤ 端⼝号 7 。

DHCP(DynamicHostConfigrationProtocol,动态主机配置协议)是⽤于局域 ⽹⾃动分配 IP 地址和主 机配置的协议。可以使局域⽹的部署更加简单。

DNS(DomainNameSystem,域名系统)是互联⽹的⼀项服务,可以简单的将⽤“.” 分隔的⼀般会有意义 的域名转换成不易记忆的 IP 地址。⼀般使⽤ UDP 协议传输, 也可以使⽤ TCP,默认服务端⼝号 53。

FTP(FileTransferProtocol,⽂件传输协议)是⽤来进⾏⽂件传输的标准协议。 FTP 基于 TCP 使⽤端⼝ 号 20 来传输数据,21 来传输控制信息。

TFTP(Trivial File Transfer Protocol,简单⽂件传输协议)是⼀个简化的⽂ 件传输协议,其设计⾮常简 单,通过少量存储器就能轻松实现,所以⼀般被⽤来通 过⽹络引导计算机过程中传输引导⽂件等⼩⽂ 件;

SH(SecureShel,安全Shel),因为传统的⽹络服务程序⽐如TELNET本质上都极不安全,明⽂传说数 据和⽤户信息包括密码, SH 被开发出来避免这些问题, 它其实是⼀个协议框架,有⼤量的扩展冗余能 ⼒,并且 供了加密压缩的通道可以 为其他协议使⽤。

POP(PostOficeProtocol,邮局协议)是⽀持通过客户端访问电⼦邮件的服务, 现在版本是 POP3,也有 加密的版本 POP3S。协议使⽤ TCP,端⼝ 10。

SMTP(Simple Mail Transfer Protocol,简单邮件传输协议)是现在在互联⽹ 上发送电⼦邮件的事实标 准。使⽤ TCP 协议传输,端⼝号 25。

HTP(HyperTextTransferProtocol,超⽂本传输协议)是现在⼴为流⾏的WEB ⽹络的基础,HTPS 是 HTP 的加密安全版本。协议通过 TCP 传输,HTP 默认 使⽤端⼝ 80,HTPS 使⽤ 43。

以上就是今天回顾的内容。 下篇回顾⼀下socket、TCP、UDP！

## 线程池

Executor 框架便是 Java 5 中引⼊的，其内部使⽤了线程池机制

好处

第⼀：降低资源消耗 通过重复利⽤已创建的线程降低线程创建和销毁造成的消耗。 第⼆：提⾼响应速度。当任务到达时，任务可以不需要等到线程创建就能⽴即执⾏。 第三：提⾼线程的可管理性。线程是稀缺资源，如果⽆限制的创建，不仅会消耗系统资源，还会降 低系统的稳定性，使⽤线程池可以进⾏统⼀的分配，调优和监控。但是要做到合理的利⽤线程池， 必须对其原理了如指掌。

## Java线程间的通信⽅式 wait()⽅法

wait()⽅法使得当前线程必须要等待，等到另外⼀个线程调⽤notify()或者notifyAl()⽅法。 当前的线程必须拥有当前对象的monitor，也即lock，就是锁。 线程调⽤wait()⽅法，释放它对锁的拥有权，然后等待另外的线程来通知它（通知的⽅式是notify() 或者notifyAl()⽅法），这样它才能重新获得锁的拥有权和恢复执⾏。 要确保调⽤wait()⽅法的时候拥有锁，即，wait()⽅法的调⽤必须放在synchronized⽅法或 synchronized块中。 ⼀个⼩⽐较： 当线程调⽤了wait()⽅法时，它会释放掉对象的锁。 另⼀个会导致线程暂停的⽅法：Thread.sl ep()，它会导致线程睡眠指定的毫秒数，但线程在睡眠 的过程中是不会释放掉对象的锁的。

## notify()⽅法

notify()⽅法会唤醒⼀个等待当前对象的锁的线程。 如果多个线程在等待，它们中的⼀个将会选择被唤醒。这种选择是随意的，和具体实现有关。（线 程等待⼀个对象的锁是由于调⽤了wait⽅法中的⼀个）。 被唤醒的线程是不能被执⾏的，需要等到当前线程放弃这个对象的锁。 被唤醒的线程将和其他线程以通常的⽅式进⾏竞争，来获得对象的锁。也就是说，被唤醒的线程并 没有什么优先权，也没有什么劣势，对象的下⼀个线程还是需要通过⼀般性的竞争。

notify()⽅法应该是被拥有对象的锁的线程所调⽤。 （This method should only be caled by a thread that is the owner of this object's monitor.） 换句话说，和wait()⽅法⼀样，notify⽅法调⽤必须放在synchronized⽅法或synchronized块中。 wait()和notify()⽅法要求在调⽤时线程已经获得了对象的锁，因此对这两个⽅法的调⽤需要放在 synchronized⽅法或synchronized块中。

⼀个线程变为⼀个对象的锁的拥有者是通过下列三种⽅法：

- 1.执⾏这个对象的synchronized实例⽅法。
- 2.执⾏这个对象的synchronized语句块。这个语句块锁的是这个对象。
- 3.对于Clas类的对象，执⾏那个类的synchronized、static⽅法。


## Java线程有哪些状态，这些状态之间是如何转化的？

<table>
  <tr>
    <th>![image 4](assets/imageFile4.png)</th>
  </tr>
</table>


这⾥写图⽚描述

- 1.
- 2.
- 3.
- 4.


新建(new)：新创建了⼀个线程对象。 可运⾏(runable)：线程对象创建后，其他线程(⽐如main线程）调⽤了该对象的start()⽅法。该 状态的线程位于可运⾏线程池中，等待被线程调度选中，获取cpu 的使⽤权 。 运⾏(runing)：可运⾏状态(runable)的线程获得了cpu 时间⽚（timeslice） ，执⾏程序代码。 阻塞(block)：阻塞状态是指线程因为某种原因放弃了cpu 使⽤权，也即让出了cpu timeslice，暂 时停⽌运⾏。直到线程进⼊可运⾏(runable)状态，才有机会再次获得cpu timeslice 转到运⾏ (runing)状态。阻塞的情况分三种：

(⼀). 等待阻塞：运⾏(runing)的线程执⾏o.wait()⽅法，JVM会把该线程放⼊等待队列(waiting queue)中。同时释放对象锁 (⼆). 同步阻塞：运⾏(runing)的线程在获取对象的同步锁时，若该同步锁被别的线程占⽤，则JVM会 把该线程放⼊锁池(lock pol)中。 (三). 其他阻塞：运⾏(runing)的线程执⾏Thread.sl ep(long ms)或t.join()⽅法，或者发出了I/O请求 时，JVM会把该线程置为阻塞状态。当sl ep()状态超时、join()等待线程终⽌或者超时、或者I/O处理完 毕时，线程重新转⼊可运⾏(runable)状态。

1.

死亡(dead)：线程run()、main() ⽅法执⾏结束，或者因异常退出了run()⽅法，则该线程结束⽣命 周期。死亡的线程不可再次复⽣。

## List接⼝、Set接⼝和Map接⼝的区别

- 1、List和Set接⼝⾃Colection接⼝，⽽Map不是继承的Colection接⼝ Collection表示⼀组对象,这些对象也称为collection的元素;⼀些 collection允许有重复的元素,⽽另⼀些则不 允许;⼀些collection是有序的,⽽另⼀些则是⽆序的;JDK中不提供此接⼝的任何直接实 现,它提供更具体的⼦接⼝ (如 Set 和 List)实现;Map没有继承Collection接⼝,Map提供key到value的映射;⼀个Map中不能包含相同key, 每个key只能映射⼀个value;Map接⼝提供3种集合的视图,Map的内容可以被当做⼀组key集合,⼀组value集合,或者 ⼀组key-value映射;
- 2、List接⼝ 元素有放⼊顺序，元素可重复 List接⼝有三个实现类：LinkedList，ArrayList，Vector LinkedList：底层基于链表实现，链表内存是散乱的，每⼀个元素存储本身内存地址的同时还存储下⼀个元素的地址。 链表增删快，查找慢 ArrayList和Vector的区别：ArrayList是⾮线程安全的，效率⾼；Vector是基于线程安全的，效率低 List是⼀种有序的Collection，可以通过索引访问集合中的数据,List⽐Collection多了10个⽅法，主要是有关索 引的⽅法。


- 1).所有的索引返回的⽅法都有可能抛出⼀个IndexOutOfBoundsException异常
- 2).subList(int fromIndex, int toIndex)返回的是包括fromIndex，不包括toIndex的视图，该列表


的size()=toIndex-fromIndex。 所有的List中只能容纳单个不同类型的对象组成的表，⽽不是Key－Value键值对。例如：[ tom,1,c ]; 所有的List中可以有相同的元素，例如Vector中可以有 [ tom,koo,too,koo ]; 所有的List中可以有null元素，例如[ tom,null,1 ]; 基于Array的List（Vector，ArrayList）适合查询，⽽LinkedList（链表）适合添加，删除操作;

- 3、Set接⼝ 元素⽆放⼊顺序，元素不可重复（注意：元素虽然⽆放⼊顺序，但是元素在set中的位置是有该元素的HashCode决定 的，其位置其实是固定的） Set接⼝有两个实现类：HashSet(底层由HashMap实现)，LinkedHashSet

SortedSet接⼝有⼀个实现类：TreeSet（底层由平衡⼆叉树实现） Query接⼝有⼀个实现类：LinkList Set具有与Collection完全⼀样的接⼝，因此没有任何额外的功能，不像前⾯有两个不同的List。实际上Set就是

Collection,只是⾏为不同。(这是继承与多态思想的典型应⽤：表现不同的⾏为。)Set不保存重复的元素(⾄于如何 判断元素相同则较为负责)

Set : 存⼊Set的每个元素都必须是唯⼀的，因为Set不保存重复元素。加⼊Set的元素必须定义equals()⽅法以确

保对象的唯⼀性。Set与Collection有完全⼀样的接⼝。Set接⼝不保证维护元素的次序。 HashSet : 为快速查找设计的Set。存⼊HashSet的对象必须定义hashCode()。 TreeSet : 保存次序的Set, 底层为树结构。使⽤它可以从Set中提取有序的序列。 LinkedHashSet : 具有HashSet的查询速度，且内部使⽤链表维护元素的顺序(插⼊的次序)。于是在使⽤迭代器

遍历Set时，结果会按元素插⼊的次序显示。

- 4、map接⼝ 以键值对的⽅式出现的 Map接⼝有三个实现类：HashMap，HashTable，LinkeHashMap


HashMap⾮线程安全，⾼效，⽀持null；

HashTable线程安全，低效，不⽀持null

SortedMap有⼀个实现类：TreeMap

## Sesion机制

⼀、术语sesion sesion，中⽂经常翻译为会话，其本来的含义是指有始有终的⼀系列动作/消息，⽐如打电话时从 拿起电话拨号到挂断电话这中间的⼀系列过程可以称之为⼀个sesion。有时候我们可以看到这样 的话“在⼀个浏览器会话期间， .”，这⾥的会话⼀词⽤的就是其本义，是指从⼀个浏览器窗⼝打开 到关闭这个期间①。最混乱的是“⽤户（客户端）在⼀次会话期间”这样⼀句话，它可能指⽤户的⼀ 系列动作（⼀般情况下是同某个具体⽬的相关的⼀系列动作，⽐如从登录到选购商品到结账登出这 样⼀个⽹上购物的过程，有时候也被称为⼀个transaction），然⽽有时候也可能仅仅是指⼀次连 接，也有可能是指含义①，其中的差别只能靠上下⽂来推断②。 然⽽当sesion⼀词与⽹络协议相关联时，它⼜往往隐含了“⾯向连接”和/或“保持状态”这样两个含 义，“⾯向连接”指的是在通信双⽅在通信之前要先建⽴⼀个通信的渠道，⽐如打电话，直到对⽅接 了电话通信才能开始，与此相对的是写信，在你把信发出去的时候你并不能确认对⽅的地址是否正 确，通信渠道不⼀定能建⽴，但对发信⼈来说，通信已经开始了。“保持状态”则是指通信的⼀⽅能 够把⼀系列的消息关联起来，使得消息之间可以互相依赖，⽐如⼀个服务员能够认出再次光临的⽼ 顾客并且记得上次这个顾客还⽋店⾥⼀块钱。这⼀类的例⼦有“⼀个TCP sesion”或者“⼀个POP3 sesion”③。

⽽到了web服务器蓬勃发展的时代，sesion在web开发语境下的语义⼜有了新的扩展，它的含义 是指⼀类⽤来在客户端与服务器之间保持状态的解决⽅案④。有时候sesion也⽤来指这种解决⽅ 案的存储结构，如“把 x保存在sesion⾥”⑤。由于各种⽤于web开发的语⾔在⼀定程度上都提供 了对这种解决⽅案的⽀持，所以在某种特定语⾔的语境下，sesion也被⽤来指代该语⾔的解决⽅ 案，⽐如经常把Java⾥提供的javax.servlet.htp.HtpSesion简称为sesion⑥。 鉴于这种混乱已不可改变，本⽂中sesion⼀词的运⽤也会根据上下⽂有不同的含义，请⼤家注意 分辨。 在本⽂中，使⽤中⽂“浏览器会话期间”来表达含义①，使⽤“sesion机制”来表达含义④，使⽤ “sesion”表达含义⑤，使⽤具体的“HtpSesion”来表达含义⑥

- * ⼆、HTP协议与状态保持 * HTP协议本身是⽆状态的，这与HTP协议本来的⽬的是相符的，客户端只需要简单的向服务器 请求下载某些⽂件，⽆论是客户端还是服务器都没有必要纪录彼此过去的⾏为，每⼀次请求之间都 是独⽴的，好⽐⼀个顾客和⼀个⾃动售货机或者⼀个普通的（⾮会员制）⼤卖场之间的关系⼀样。 然⽽聪明（或者贪⼼？）的⼈们很快发现如果能够提供⼀些按需⽣成的动态信息会使web变得更加 有⽤，就像给有线电视加上点播功能⼀样。这种需求⼀⽅⾯迫使HTML逐步添加了表单、脚本、 DOM等客户端⾏为，另⼀⽅⾯在服务器端则出现了CGI规范以响应客户端的动态请求，作为传输载 体的HTP协议也添加了⽂件上载、cokie这些特性。其中cokie的作⽤就是为了解决HTP协议⽆ 状态的缺陷所作出的努⼒。⾄于后来出现的sesion机制则是⼜⼀种在客户端与服务器之间保持状 态的解决⽅案。 让我们⽤⼏个例⼦来描述⼀下cokie和sesion机制之间的区别与联系。笔者曾经常去的⼀家咖啡 店有喝5杯咖啡免费赠⼀杯咖啡的优惠，然⽽⼀次性消费5杯咖啡的机会微乎其微，这时就需要某 种⽅式来纪录某位顾客的消费数量。想象⼀下其实也⽆外乎下⾯的⼏种⽅案：

- 1、该店的店员很厉害，能记住每位顾客的消费数量，只要顾客⼀⾛进咖啡店，店员就知道该怎么 对待了。这种做法就是协议本身⽀持状态。
- 2、发给顾客⼀张卡⽚，上⾯记录着消费的数量，⼀般还有个有效期限。每次消费时，如果顾客出 示这张卡⽚，则此次消费就会与以前或以后的消费相联系起来。这种做法就是在客户端保持状态。
- 3、发给顾客⼀张会员卡，除了卡号之外什么信息也不纪录，每次消费时，如果顾客出示该卡⽚， 则店员在店⾥的纪录本上找到这个卡号对应的纪录添加⼀些消费信息。这种做法就是在服务器端保 持状态。 由于HTP协议是⽆状态的，⽽出于种种考虑也不希望使之成为有状态的，因此，后⾯两种⽅案就 成为现实的选择。具体来说cokie机制采⽤的是在客户端保持状态的⽅案，⽽sesion机制采⽤的 是在服务器端保持状态的⽅案。同时我们也看到，由于采⽤服务器端保持状态的⽅案在客户端也需 要保存⼀个标识，所以sesion机制可能需要借助于cokie机制来达到保存标识的⽬的，但实际上 它还有其他选择。


- *三、理解cokie机制 * cokie机制的基本原理就如上⾯的例⼦⼀样简单，但是还有⼏个问题需要解决：“会员卡”如何分 发；“会员卡”的内容；以及客户如何使⽤“会员卡”。


正统的cokie分发是通过扩展HTP协议来实现的，服务器通过在HTP的响应头中加上⼀⾏特殊的 指示以提示浏览器按照指示⽣成相应的cokie。然⽽纯粹的客户端脚本如JavaScript或者VBScript 也可以⽣成cokie。 ⽽cokie的使⽤是由浏览器按照⼀定的原则在后台⾃动发送给服务器的。浏览器检查所有存储的 cokie，如果某个cokie所声明的作⽤范围⼤于等于将要请求的资源所在的位置，则把该cokie附 在请求资源的HTP请求头上发送给服务器。意思是⻨当劳的会员卡只能在⻨当劳的店⾥出示，如 果某家分店还发⾏了⾃⼰的会员卡，那么进这家店的时候除了要出示⻨当劳的会员卡，还要出示这 家店的会员卡。 cokie的内容主要包括：名字，值，过期时间，路径和域。 其中域可以指定某⼀个域⽐如.gogle.com，相当于总店招牌，⽐如宝洁公司，也可以指定⼀个域 下的具体某台机器⽐如 或者frogle.gogle.com，可以⽤飘柔来做⽐。 路径就是跟在域名后⾯的URL路径，⽐如/或者/fo等等，可以⽤某飘柔专柜做⽐。路径与域合在⼀ 起就构成了cokie的作⽤范围。如果不设置过期时间，则表示这个cokie的⽣命期为浏览器会话期 间，只要关闭浏览器窗⼝，cokie就消失了。这种⽣命期为浏览器会话期的cokie被称为会话 cokie。会话cokie⼀般不存储在硬盘上⽽是保存在内存⾥，当然这种⾏为并不是规范规定的。如 果设置了过期时间，浏览器就会把cokie保存到硬盘上，关闭后再次打开浏览器，这些cokie仍然 有效直到超过设定的过期时间。 存储在硬盘上的cokie可以在不同的浏览器进程间共享，⽐如两个IE窗⼝。⽽对于保存在内存⾥的 cokie，不同的浏览器有不同的处理⽅式。对于IE，在⼀个打开的窗⼝上按Ctrl-N（或者从⽂件菜 单）打开的窗⼝可以与原窗⼝共享，⽽使⽤其他⽅式新开的IE进程则不能共享已经打开的窗⼝的内 存cokie；对于Mozila Firefox0.8，所有的进程和标签⻚都可以共享同样的cokie。⼀般来说是⽤ javascript的window.open打开的窗⼝会与原窗⼝共享内存cokie。浏览器对于会话cokie的这种 只认cokie不认⼈的处理⽅式经常给采⽤sesion机制的web应⽤程序开发者造成很⼤的困扰。

w.gogle.com

## Cokie和Sesion的区别

HTP请求是⽆状态的。 共同之处： cokie和sesion都是⽤来跟踪浏览器⽤户身份的会话⽅式。 区别：

cokie数据保存在客户端，sesion数据保存在服务器端。简单的说，当你登录⼀个⽹站的时候, 如果web服务器端使⽤的是sesion，那么所有的数据都保存在服务器上，客户端每次请求服务 器的时候会发送当前会话的sesionid，服务器根据当前sesionid判断相应的⽤户数据标志，以 确定⽤户是否登录或具有某种权限。由于数据是存储在服务器上⾯，所以你不能伪造。

sesionid是服务器和客户端链接时候随机分配的. 如果浏览器使⽤的是cokie，那么所有的数据 都保存在浏览器端，⽐如你登录以后，服务器设置了cokie⽤户名，那么当你再次请求服务器 的时候，浏览器会将⽤户名⼀块发送给服务器，这些变量有⼀定的特殊标记。服务器会解释为 cokie变量，所以只要不关闭浏览器，那么cokie变量⼀直是有效的，所以能够保证⻓时间不 掉线。如果你能够截获某个⽤户的 cokie变量，然后伪造⼀个数据包发送过去，那么服务器还 是认为你是合法的。所以，使⽤ cokie被攻击的可能性⽐较⼤。

如果设置了的有效时间，那么它会将 cokie保存在客户端的硬盘上，下次再访问该⽹站的时候， 浏览器先检查有没有 cokie，如果有的话，就读取该 cokie，然后发送给服务器。如果你在机器 上⾯保存了某个论坛 cokie，有效期是⼀年，如果有⼈⼊侵你的机器，将你的 cokie拷⾛，然后 放在他的浏览器的⽬录下⾯，那么他登录该⽹站的时候就是⽤你的的身份登录的。所以 cokie是 可以伪造的。当然，伪造的时候需要主意，直接copy cokie⽂件到 cokie⽬录，浏览器是不认 的，他有⼀个index.dat⽂件，存储了 cokie⽂件的建⽴时间，以及是否有修改，所以你必须先要 有该⽹站的 cokie⽂件，并且要从保证时间上骗过浏览器 两个都可以⽤来存私密的东⻄，同样也都有有效期的说法,区别在于sesion是放在服务器上的，过 期与否取决于服务期的设定，cokie是存在客户端的，过去与否可以在cokie⽣成的时候设置进 去。

- (1)cokie数据存放在客户的浏览器上，sesion数据放在服务器上
- (2)cokie不是很安全，别⼈可以分析存放在本地的COKIE并进⾏COKIE欺骗,如果主要考虑到安 全应当使⽤sesion
- (3)sesion会在⼀定时间内保存在服务器上。当访问增多，会⽐较占⽤你服务器的性能，如果主要 考虑到减轻服务器性能⽅⾯，应当使⽤COKIE
- (4)单个cokie在客户端的限制是3K，就是说⼀个站点在客户端存放的COKIE不能3K。
- (5)所以：将登陆信息等重要信息存放为SESION;其他信息如果需要保留，可以放在COKIE中


## Java中的equals和hashCode⽅法详解

equals()⽅法是⽤来判断其他的对象是否和该对象相等. equals()⽅法在object类中定义如下： public boolean equals(Object obj) {

return (this == obj); }

很明显是对两个对象的地址值进⾏的⽐较（即⽐较引⽤是否相同）。但是我们知道，String 、 Math、Integer、Double等这些封装类在使⽤equals()⽅法时，已经覆盖了object类的equals()⽅ 法。 ⽐如在String类中如下： [

<table>
  <tr>
    <th>![image 5](assets/imageFile5.png)</th>
  </tr>
</table>


复制代码 ](javascript:void(0);)

public boolean equals(Object anObject) { if (this == anObject) { return true;

} if (anObject instanceof String) {

String anotherString = (String)anObject; int n = count; if (n == anotherString.count) {

char v1[] = value; char v2[] = anotherString.value;

- int i = offset;
- int j = anotherString.offset; while (n– != 0) {


if (v1[i++] != v2[j++]) return false;

} return true;

}

} return false;

}

很明显，这是进⾏的内容⽐较，⽽已经不再是地址的⽐较。依次类推Math、Integer、Double等这 些类都是重写了equals()⽅法的，从⽽进⾏的是内容的⽐较。当然，基本类型是进⾏值的⽐较。 它的性质有：

⾃反性（reflexive）。对于任意不为null的引⽤值x，x.equals(x)⼀定是true。

对称性（sy metric）。对于任意不为null的引⽤值x和y，当且仅当x.equals(y)是true时， y.equals(x)也是true。

传递性（transitive）。对于任意不为null的引⽤值x、y和z，如果x.equals(y)是true，同时 y.equals(z)是true，那么x.equals(z)⼀定是true。

⼀致性（consistent）。对于任意不为null的引⽤值x和y，如果⽤于equals⽐较的对象信息没有 被修改的话，多次调⽤时x.equals(y)要么⼀致地返回true要么⼀致地返回false。

对于任意不为null的引⽤值x，x.equals(null)返回false。

对于Object类来说，equals()⽅法在对象上实现的是差别可能性最⼤的等价关系，即，对于任意⾮ null的引⽤值x和y，当且仅当x和y引⽤的是同⼀个对象，该⽅法才会返回true。 需要注意的是当equals()⽅法被overide时，hashCode()也要被overide。按照⼀般 hashCode()⽅法的实现来说，相等的对象，它们的hash code⼀定相等。

## hashcode()⽅法详解

hashCode()⽅法给对象返回⼀个hash code值。这个⽅法被⽤于hash tables，例如HashMap。

它的性质是：

在⼀个Java应⽤的执⾏期间，如果⼀个对象提供给equals做⽐较的信息没有被修改的话，该对 象多次调⽤hashCode()⽅法，该⽅法必须始终如⼀返回同⼀个integer。

如果两个对象根据equals(Object)⽅法是相等的，那么调⽤⼆者各⾃的hashCode()⽅法必须产⽣ 同⼀个integer结果。

并不要求根据equals(java.lang.Object)⽅法不相等的两个对象，调⽤⼆者各⾃的hashCode()⽅ 法必须产⽣不同的integer结果。然⽽，程序员应该意识到对于不同的对象产⽣不同的integer结 果，有可能会提⾼hash table的性能。

## Java中CAS算法 -乐观锁的⼀种实现⽅式

悲观者与乐观者的做事⽅式完全不⼀样，悲观者的⼈⽣观是⼀件事情我必须要百分之百完全控制才 会去做，否则就认为这件事情⼀定会出问题；⽽乐观者的⼈⽣观则相反，凡事不管最终结果如何， 他都会先尝试去做，⼤不了最后不成功。这就是悲观锁与乐观锁的区别，悲观锁会把整个对象加锁 占为⾃有后才去做操作，乐观锁不获取锁直接做操作，然后通过⼀定检测⼿段决定是否更新数据。 这⼀节将对乐观锁进⾏深⼊探讨。 上节讨论的Synchronized互斥锁属于悲观锁，它有⼀个明显的缺点，它不管数据存不存在竞争都 加锁，随着并发量增加，且如果锁的时间⽐较⻓，其性能开销将会变得很⼤。有没有办法解决这个 问题？答案是基于冲突检测的乐观锁。这种模式下，已经没有所谓的锁概念了，每条线程都直接先 去执⾏操作，计算完成后检测是否与其他线程存在共享数据竞争，如果没有则让此操作成功，如果 存在共享数据竞争则可能不断地重新执⾏操作和检测，直到成功为⽌，可叫CAS⾃旋。 乐观锁的核⼼算法是CAS（Compareand Swap，⽐较并交换），它涉及到三个操作数：内存值、 预期值、新值。当且仅当预期值和内存值相等时才将内存值修改为新值。这样处理的逻辑是，⾸先 检查某块内存的值是否跟之前我读取时的⼀样，如不⼀样则表示期间此内存值已经被别的线程更改 过，舍弃本次操作，否则说明期间没有其他线程对此内存值操作，可以把新值设置给此块内存。如 图2-5-4-1，有两个线程可能会差不多同时对某内存操作，线程⼆先读取某内存值作为预期值，执 ⾏到某处时线程⼆决定将新值设置到内存块中，如果线程⼀在此期间修改了内存块，则通过CAS即 可以检测出来，假如检测没问题则线程⼆将新值赋予内存块。

<table>
  <tr>
    <th>![image 6](assets/imageFile6.png)</th>
  </tr>
</table>


img 图2-5-4-1 假如你⾜够细⼼你可能会发现⼀个疑问，⽐较和交换，从字⾯上就有两个操作了，更别说实际CAS 可能会有更多的执⾏指令，他们是原⼦性的吗？如果⾮原⼦性⼜怎么保证CAS操作期间出现并发带 来的问题？我是不是需要⽤上节提到的互斥锁来保证他的原⼦性操作？CAS肯定是具有原⼦性的， 不然就谈不上在并发中使⽤了，但这个原⼦性是由CPU硬件指令实现保证的，即使⽤JNI调⽤ native⽅法调⽤由C+编写的硬件级别指令，jdk中提供了Unsafe类执⾏这些操作。另外，你可能 想着CAS是通过互斥锁来实现原⼦性的，这样确实能实现，但⽤这种⽅式来保证原⼦性显示毫⽆意 义。下⾯⼀个伪代码加深对CAS的理解：

public class AtomicInt { private volatile int value; public final int get() {

return value; }

public final int getAndIncrement() {

for (;;) { int current = get(); int next = current + 1; if (compareAndSet(current, next))

return current; }

} public final boolean compareAndSet(int expect, int update) {

Unsafe类提供的硬件级别的compareAndSwapInt⽅法; }

}

其中最重要的⽅法是getAndIncrement⽅法，它⾥⾯实现了基于CAS的⾃旋。 现在已经了解乐观锁及CAS相关机制，乐观锁避免了悲观锁独占对象的现象，同时也提⾼了并发性 能，但它也有缺点：

- ① 观锁只能保证⼀个共享变量的原⼦操作。如上例⼦，⾃旋过程中只能保证value变量的原⼦性， 这时如果多⼀个或⼏个变量，乐观锁将变得⼒不从⼼，但互斥锁能轻易解决，不管对象数量多少及 对象颗粒度⼤⼩。
- ② ⻓时间⾃旋可能导致开销⼤。假如CAS⻓时间不成功⽽⼀直⾃旋，会给CPU带来很⼤的开销。
- ③ ABA问题。CAS的核⼼思想是通过⽐对内存值与预期值是否⼀样⽽判断内存值是否被改过，但这 个判断逻辑不严谨，假如内存值原来是A，后来被⼀条线程改为B，最后⼜被改成了A，则CAS认为 此内存值并没有发⽣改变，但实际上是有被其他线程改过的，这种情况对依赖过程值的情景的运算 结果影响很⼤。解决的思路是引⼊版本号，每次变量更新都把版本号加⼀。 乐观锁是对悲观锁的改进，虽然它也有缺点，但它确实已经成为提⾼并发性能的主要⼿段，⽽且 jdk中的并发包也⼤量使⽤基于CAS的乐观锁。


TimSort原理

comparable与comparator的区别

# Comparable和Comparator的区别

初次碰到这个问题是之前有⼀次电话⾯试，问了⼀个⼩时的问题，其中有⼀个问题就问到 Comparable和Comparator的区别，当时没答出 来。之后是公司⼊职时候做的⼀套Java编程题， ⾥⾯⽤JUnit跑⽤例的时候也⽤到了Comparator接⼝，再加上JDK的⼤量的类包括常⻅的 String、 Byte、Char、Date等都实现了Comparable接⼝，因此要学习⼀下这两个类的区别以及⽤法。 Comparable Comparable可以认为是⼀个内⽐较器，实现了Comparable接⼝的类有⼀个特点，就是这些类是可 以和⾃⼰⽐较的，⾄于具体和另⼀个实现了Comparable接⼝的类如何⽐较，则依赖compareTo⽅ 法的实现，compareTo⽅法也被称为⾃然⽐较⽅法。如果开发者ad进⼊⼀个Colection的对象想 要Colections的sort⽅法帮你⾃动进⾏排序的话，那么这个对象必须实现Comparable接⼝。 compareTo⽅法的返回值是int，有三种情况：

- 1、⽐较者⼤于被⽐较者（也就是compareTo⽅法⾥⾯的对象），那么返回正整数
- 2、⽐较者等于被⽐较者，那么返回0
- 3、⽐较者⼩于被⽐较者，那么返回负整数 写个很简单的例⼦：


public class Domain implements Comparable<Domain> {

private String str;

public Domain(String str) {

this.str = str; }

public int compareTo(Domain domain) {

if (this.str.compareTo(domain.str) > 0) return 1; else if (this.str.compareTo(domain.str) == 0)

return 0; else

return -1; }

public String getStr() {

return str; }

} public static void main(String[] args)

{

Domain d1 = new Domain("c"); Domain d2 = new Domain("c"); Domain d3 = new Domain("b"); Domain d4 = new Domain("d"); System.out.println(d1.compareTo(d2)); System.out.println(d1.compareTo(d3)); System.out.println(d1.compareTo(d4));

}

运⾏结果为：

- 0
- 1


-1

注意⼀下，前⾯说实现Comparable接⼝的类是可以⽀持和⾃⼰⽐较的，但是其实代码⾥⾯ Comparable的泛型未必就⼀定要是Domain，将泛型指定为String或者指定为其他任何任何类型都 可以 -只要开发者指定了具体的⽐较算法就⾏。 Comparator Comparator可以认为是是⼀个外⽐较器，个⼈认为有两种情况可以使⽤实现Comparator接⼝的⽅ 式：

- 1、⼀个对象不⽀持⾃⼰和⾃⼰⽐较（没有实现Comparable接⼝），但是⼜想对两个对象进⾏⽐较
- 2、⼀个对象实现了Comparable接⼝，但是开发者认为compareTo⽅法中的⽐较⽅式并不是⾃⼰想 要的那种⽐较⽅式 Comparator接⼝⾥⾯有⼀个compare⽅法，⽅法有两个参数T o1和T o2，是泛型的表示⽅式，分 别表示待⽐较的两个对象，⽅法返回值和Comparable接⼝⼀样是int，有三种情况：


- 1、o1⼤于o2，返回正整数
- 2、o1等于o2，返回0
- 3、o1⼩于o3，返回负整数 写个很简单的例⼦，上⾯代码的Domain不变（假设这就是第2种场景，我对这个compareTo算法实


现不满意，要⾃⼰写实现）：

public class DomainComparator implements Comparator<Domain> {

public int compare(Domain domain1, Domain domain2) {

if (domain1.getStr().compareTo(domain2.getStr()) > 0) return 1; else if (domain1.getStr().compareTo(domain2.getStr()) == 0)

return 0; else

return -1; }

} public static void main(String[] args) {

Domain d1 = new Domain("c"); Domain d2 = new Domain("c"); Domain d3 = new Domain("b"); Domain d4 = new Domain("d"); DomainComparator dc = new DomainComparator(); System.out.println(dc.compare(d1, d2)); System.out.println(dc.compare(d1, d3)); System.out.println(dc.compare(d1, d4));

}

看⼀下运⾏结果：

- 0
- 1


-1

当然因为泛型指定死了，所以实现Comparator接⼝的实现类只能是两个相同的对象（不能⼀个 Domain、⼀个String）进⾏⽐较了，因此实现Comparator接⼝的实现类⼀般都会以"待⽐较的实体 类+Comparator"来命名 总结 总结⼀下，两种⽐较器Comparable和Comparator，后者相⽐前者有如下优点：

- 1、如果实现类没有实现Comparable接⼝，⼜想对两个类进⾏⽐较（或者实现类实现了 Comparable接⼝，但是对compareTo⽅法内的⽐较算法不满意），那么可以实现Comparator接 ⼝，⾃定义⼀个⽐较器，写⽐较算法
- 2、实现Comparable接⼝的⽅式⽐实现Comparator接⼝的耦合性 要强⼀些，如果要修改⽐较算 法，要修改Comparable接⼝的实现类，⽽实现Comparator的类是在外部进⾏⽐较的，不需要对实 现类有任何修 改。从这个⻆度说，其实有些不太好，尤其在我们将实现类的.clas⽂件打成⼀ 个.jar⽂件提供给开发者使⽤的时候。实际上实现Comparator 接⼝的⽅式后⾯会写到就是⼀种典型 的策略模式。


## ⼿写单例模式（线程安全）

解法⼀：只适合单线程环境（不好）

package test; /**

- * @author xiaoping

*

- */ public class Singleton {


private static Singleton instance=null; private Singleton(){

} public static Singleton getInstance(){

if(instance==null){ instance=new Singleton();

} return instance;

} }

注解:Singleton的静态属性instance中，只有instance为nul的时候才创建⼀个实例，构造函数私 有，确保每次都只创建⼀个，避免重复创建。 缺点：只在单线程的情况下正常运⾏，在多线程的情况下，就会出问题。例如：当两个线程同时运 ⾏到判断instance是否为空的if语句，并且instance确实没有创建好时，那么两个线程都会创建⼀ 个实例。 解法⼆：多线程的情况可以⽤。（懒汉式，不好）

private static Singleton instance=null; private Singleton(){

} public static synchronized Singleton getInstance(){

if(instance==null){ instance=new Singleton();

} return instance;

} }

注解：在解法⼀的基础上加上了同步锁，使得在多线程的情况下可以⽤。例如：当两个线程同时想 创建实例，由于在⼀个时刻只有⼀个线程能得到同步锁，当第⼀个线程加上锁以后，第⼆个线程只 能等待。第⼀个线程发现实例没有创建，创建之。第⼀个线程释放同步锁，第⼆个线程才可以加上 同步锁，执⾏下⾯的代码。由于第⼀个线程已经创建了实例，所以第⼆个线程不需要创建实例。保 证在多线程的环境下也只有⼀个实例。 缺点：每次通过getInstance⽅法得到singleton实例的时候都有⼀个试图去获取同步锁的过程。⽽ 众所周知，加锁是很耗时的。能避免则避免。 解法三：加同步锁时，前后两次判断实例是否存在（可⾏）

public class Singleton { private static Singleton instance=null; private Singleton(){ } public static Singleton getInstance(){

if(instance==null){ synchronized(Singleton.class){ if(instance==null){

instance=new Singleton(); }

}

} return instance;

} }

注解：只有当instance为nul时，需要获取同步锁，创建⼀次实例。当实例被创建，则⽆需试图加 锁。 缺点：⽤双重if判断，复杂，容易出错。 解法四：饿汉式（建议使⽤）

private static Singleton instance=new Singleton(); private Singleton(){ } public static Singleton getInstance(){

return instance; }

}

注解：初试化静态的instance创建⼀次。如果我们在Singleton类⾥⾯写⼀个静态的⽅法不需要创建 实例，它仍然会早早的创建⼀次实例。⽽降低内存的使⽤率。 缺点：没有lazy loading的效果，从⽽降低内存的使⽤率。 解法五：静态内部内。（建议使⽤）

public class Singleton { private Singleton(){

} private static class SingletonHolder{

private final static Singleton instance=new Singleton();

} public static Singleton getInstance(){

return SingletonHolder.instance; }

}

注解：定义⼀个私有的内部类，在第⼀次⽤这个嵌套类时，会创建⼀个实例。⽽类型为 SingletonHolder的类，只有在Singleton.getInstance()中调⽤，由于私有的属性，他⼈⽆法使⽤ SingleHolder，不调⽤Singleton.getInstance()就不会创建实例。 优点：达到了lazy loading的效果，即按需创建实例。

## JVM参数初始值

初始堆⼤⼩：1/64内存-Xms 最⼤堆⼤⼩：1/4内存-Xmx 初始永久代⼤⼩：1/64内存-X PermSize 最⼤堆⼤⼩：1/4内存-X MaxPermSize

## Java8的内存分代改进

JAVA 8持久代已经被彻底删除了

取代它的是另⼀个内存区域也被称为元空间。 元空间 ⸺ 快速⼊⻔

它是本地内存中的⼀部分

最直接的表现就是 OM（内存溢出）问题将不复存在，因为直接利⽤的是本地内存。

它可以通过-X MetaspaceSize和-X MaxMetaspaceSize来进⾏调整

当到达 X MetaspaceSize所指定的阈值后会开始进⾏清理该区域

如果本地空间的内存⽤尽了会收到java.lang.OutOfMemoryEror: Metadata space的错误信息。

和持久代相关的JVM参数-X PermSize及-X MaxPermSize将会被忽略掉，并且在启动的时候 给出警告信息。

充分利⽤了Java语⾔规范中的好处：类及相关的元数据的⽣命周期与类加载器的⼀致

元空间 ⸺ 内存分配模型绝⼤多数的类元数据的空间都从本地内存中分配。⽤来描述类元数据的 类也被删除了，分元数据分配了多个虚拟内存空间给每个类加载器分配⼀个内存块的列表，只进⾏ 线性分配。块的⼤⼩取决于类加载器的类型， sun/反射/代理对应的类加载器的块会⼩⼀些。不会 单独回收某个类，如果GC发现某个类加载器不再存活了，会把相关的空间整个回收掉。这样减少 了碎⽚，并节省GC扫描和压缩的时间。 元空间 ⸺ 调优使⽤-X MaxMetaspaceSize参数可以设置元空间的最⼤值，默认是没有上限的， 也就是说你的系统内存上限是多少它就是多少。使⽤-X MetaspaceSize选项指定的是元空间的初 始⼤⼩，如果没有指定的话，元空间会根据应⽤程序运⾏时的需要动态地调整⼤⼩。 ⼀旦类元数 据的使⽤量达到了“MaxMetaspaceSize”指定的值，对于⽆⽤的类和类加载器，垃圾收集此时会触 发。为了控制这种垃圾收集的频率和延迟，合适的监控和调整Metaspace⾮常有必要。过于频繁的 Metaspace垃圾收集是类和类加载器发⽣内存泄露的征兆，同时也说明你的应⽤程序内存⼤⼩不合 适，需要调整。

* 快速过⼀遍JVM的内存结构，JVM中的内存分为5个虚拟的区域：（程序计数器、 虚拟机栈、本地⽅法栈、堆区、⽅法区）

<table>
  <tr>
    <th>![image 7](assets/imageFile7.png)</th>
  </tr>
</table>


Java8的JVM持久代 - 何去何从？

## 堆

你的Java程序中所分配的每⼀个对象都需要存储在内存⾥。堆是这些实例化的对象所存储的地 ⽅。是的⸺都怪new操作符，是它把你的Java堆都占满了的！

它由所有线程共享

当堆耗尽的时候，JVM会抛出java.lang.OutOfMemoryEror 异常

堆的⼤⼩可以通过JVM选项-Xms和-Xmx来进⾏调整

堆被分为：

Eden区 ⸺ 新对象或者⽣命周期很短的对象会存储在这个区域中，这个区的⼤⼩可以通过X NewSize和-X MaxNewSize参数来调整。新⽣代GC（垃圾回收器）会清理这⼀区域。

Survivor区 ⸺ 那些历经了Eden区的垃圾回收仍能存活下来的依旧存在引⽤的对象会待在这个 区域。这个区的⼤⼩可以由JVM参数-X SurvivorRatio来进⾏调节。

⽼年代 ⸺ 那些在历经了Eden区和Survivor区的多次GC后仍然存活下来的对象（当然了，是拜 那些挥之不去的引⽤所赐）会存储在这个区⾥。这个区会由⼀个特殊的垃圾回收器来负责。年 ⽼代中的对象的回收是由⽼年代的GC（major GC）来进⾏的。

## ⽅法区

也被称为⾮堆区域（在HotSpot JVM的实现当中）

它被分为两个主要的⼦区域

持久代 ⸺ 这个区域会 存储包括类定义，结构，字段，⽅法（数据及代码）以及常量在内的类相 关数据。它可以通过-X PermSize及 -X MaxPermSize来进⾏调节。如果它的空间⽤完了，会导 致java.lang.OutOfMemoryEror: PermGen space的异常。 代码缓存⸺这个缓存区域是⽤来存储编译后的代码。编译后的代码就是本地代码（硬件相关 的），它是由JIT（Just In Time)编译器⽣成的，这个编译器是Oracle HotSpot JVM所特有的。

JVM栈

和Java类中的⽅法密切相关

它会存储局部变量以及⽅法调⽤的中间结果及返回值

Java中的每个线程都有⾃⼰专属的栈，这个栈是别的线程⽆法访问的。

可以通过JVM选项-Xs来进⾏调整

本地栈

⽤于本地⽅法（⾮Java代码）

按线程分配

PC寄存器

特定线程的程序计数器

包含JVM正在执⾏的指令的地址（如果是本地⽅法的话它的值则未定义）

好吧，这就是JVM内存分区的基础知识了。现在再说说持久代这个话题吧。 对Java内存模型的理解以及其在并发当中的作⽤

## 概述

Java平台⾃动集成了线程以及多处理器技术，这种集成程度⽐Java以前诞⽣的计算机语⾔要厉害 很多，该语⾔针对多种异构平台的平台独⽴性⽽使⽤的多线程技术⽀持也是具有开拓性的⼀⾯，有 时候在开发Java同步和线程安全要求很严格的程序时，往往容易混淆的⼀个概念就是内存模型。究 竟什么是内存模型？内存模型描述了程序中各个变量（实例域、静态域和数组元素）之间的关系， 以及在实际计算机系统中将变量存储到内存和从内存中取出变量这样的底层细节，对象最终是存储 在内存⾥⾯的，这点没有错，但是编译器、运⾏库、处理器或者系统缓存可以有特权在变量指定内 存位置存储或者取出变量的值。【J M】（Java Memory Model的缩写）允许编译器和缓存以数 据在处理器特定的缓存（或寄存器）和主存之间移动的次序拥有重要的特权，除⾮程序员使⽤了 final或synchronized明确请求了某些可⻅性的保证。在Java中应为不同的⽬的可以将java划分为两 种内存模型：gc内存模型。并发内存模型。

## gc内存模型

java与c+之间有⼀堵由内存动态分配与垃圾收集技术所围成的“⾼墙”。墙外⾯的⼈想进去，墙⾥ ⾯的⼈想出来。java在执⾏java程序的过程中会把它管理的内存划分若⼲个不同功能的数据管理区 域。如图：

<table>
  <tr>
    <th>![image 8](assets/imageFile8.png)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 9](assets/imageFile9.png)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 10](assets/imageFile10.png)</th>
  </tr>
</table>


## hotspot中的gc内存模型

整体上。分为三部分：栈，堆，程序计数器，他们每⼀部分有其各⾃的⽤途；虚拟机栈保存着每⼀ 条线程的执⾏程序调⽤堆栈；堆保存着类对象、数组的具体信息；程序计数器保存着每⼀条线程下 ⼀次执⾏指令位置。这三块区域中栈和程序计数器是线程私有的。也就是说每⼀个线程拥有其独⽴ 的栈和程序计数器。我们可以看看具体结构：

虚拟机/本地⽅法栈

在栈中，会为每⼀个线程创建⼀个栈。线程越多，栈的内存使⽤越⼤。对于每⼀个线程栈。当⼀个 ⽅法在线程中执⾏的时候，会在线程栈中创建⼀个栈帧(stack frame)，⽤于存放该⽅法的上下⽂ (局部变量表、操作数栈、⽅法返回地址等等)。每⼀个⽅法从调⽤到执⾏完毕的过程，就是对应着 ⼀个栈帧⼊栈出栈的过程。 本地⽅法栈与虚拟机栈发挥的作⽤是类似的，他们之间的区别不过是虚拟机栈为虚拟机执⾏ java(字节码)服务的，⽽本地⽅法栈是为虚拟机执⾏native⽅法服务的。

⽅法区/堆

在hotspot的实现中，⽅法区就是在堆中称为永久代的堆区域。⼏乎所有的对象/数组的内存空间都 在堆上(有少部分在栈上)。在gc管理中，将虚拟机堆分为永久代、⽼年代、新⽣代。通过名字我们 可以知道⼀个对象新建⼀般在新⽣代。经过⼏轮的gc。还存活的对象会被移到⽼年代。永久代⽤ 来保存类信息、代码段等⼏乎不会变的数据。堆中的所有数据是线程共享的。

新⽣代：应为gc具体实现的优化的原因。hotspot⼜将新⽣代划分为⼀个eden区和两个survivor 区。每⼀次新⽣代gc时候。只⽤到⼀个eden区，⼀个survivor区。新⽣代⼀般的gc策略为 mark-copy。

⽼年代：当新⽣代中的对象经过若⼲轮gc后还存活/或survisor在gc内存不够的时候。会把当前 对象移动到⽼年代。⽼年代⼀般gc策略为mark-compact。

永久代：永久代⼀般可以不参与gc。应为其中保存的是⼀些代码/常量数据/类信息。在永久代 gc。清楚的是类信息以及常量池。

JVM内存模型中分两⼤块，⼀块是 NEW Generation, 另⼀块是Old Generation. 在New Generation 中，有⼀个叫Eden的空间，主要是⽤来存放新⽣的对象，还有两个Survivor Spaces（from,to）, 它们⽤来存放每次垃圾回收后存活下来的对象。在Old Generation中，主要存放应⽤程序中⽣命周 期⻓的内存对象，还有个Permanent Generation，主要⽤来放JVM⾃⼰的反射对象，⽐如类对象 和⽅法对象等。

### 程序计数器

如同其名称⼀样。程序计数器⽤于记录某个线程下次执⾏指令位置。程序计数器也是线程私有的。

并发内存模型

java试图定义⼀个Java内存模型(Java memory model j m)来屏蔽掉各种硬件/操作系统的内存访 问差异，以实现让java程序在各个平台下都能达到⼀致的内存访问效果。java内存模型主要⽬标是 定义程序中各个变量的访问规则，即在虚拟机中将变量存储到内存和从内存中取出变量这样的底层 细节。模型图如下：

<table>
  <tr>
    <th>![image 11](assets/imageFile11.png)</th>
  </tr>
</table>


img

### java并发内存模型以及内存操作规则

java内存模型中规定了所有变量都存贮到主内存（如虚拟机物理内存中的⼀部分）中。每⼀个线程 都有⼀个⾃⼰的⼯作内存(如cpu中的⾼速缓存)。线程中的⼯作内存保存了该线程使⽤到的变量的 主内存的副本拷⻉。线程对变量的所有操作（读取、赋值等）必须在该线程的⼯作内存中进⾏。不 同线程之间⽆法直接访问对⽅⼯作内存中变量。线程间变量的值传递均需要通过主内存来完成。 关于主内存与⼯作内存之间的交互协议，即⼀个变量如何从主内存拷⻉到⼯作内存。如何从⼯作内 存同步到主内存中的实现细节。java内存模型定义了8种操作来完成。这8种操作每⼀种都是原⼦操 作。8种操作如下：

lock(锁定)：作⽤于主内存，它把⼀个变量标记为⼀条线程独占状态；

unlock(解锁)：作⽤于主内存，它将⼀个处于锁定状态的变量释放出来，释放后的变量才能够被 其他线程锁定；

read(读取)：作⽤于主内存，它把变量值从主内存传送到线程的⼯作内存中，以便随后的load动 作使⽤；

load(载⼊)：作⽤于⼯作内存，它把read操作的值放⼊⼯作内存中的变量副本中；

use(使⽤)：作⽤于⼯作内存，它把⼯作内存中的值传递给执⾏引擎，每当虚拟机遇到⼀个需要 使⽤这个变量的指令时候，将会执⾏这个动作；

asign(赋值)：作⽤于⼯作内存，它把从执⾏引擎获取的值赋值给⼯作内存中的变量，每当虚拟 机遇到⼀个给变量赋值的指令时候，执⾏该操作；

store(存储)：作⽤于⼯作内存，它把⼯作内存中的⼀个变量传送给主内存中，以备随后的write 操作使⽤；

write(写⼊)：作⽤于主内存，它把store传送值放到主内存中的变量中。

Java内存模型还规定了执⾏上述8种基本操作时必须满⾜如下规则:

不允许read和load、store和write操作之⼀单独出现，以上两个操作必须按顺序执⾏，但没有保 证必须连续执⾏，也就是说，read与load之间、store与write之间是可插⼊其他指令的。

不允许⼀个线程丢弃它的最近的asign操作，即变量在⼯作内存中改变了之后必须把该变化同 步回主内存。

不允许⼀个线程⽆原因地（没有发⽣过任何asign操作）把数据从线程的⼯作内存同步回主内 存中。

⼀个新的变量只能从主内存中“诞⽣”，不允许在⼯作内存中直接使⽤⼀个未被初始化（load或 asign）的变量，换句话说就是对⼀个变量实施use和store操作之前，必须先执⾏过了asign和 load操作。

⼀个变量在同⼀个时刻只允许⼀条线程对其执⾏lock操作，但lock操作可以被同⼀个条线程重复 执⾏多次，多次执⾏lock后，只有执⾏相同次数的unlock操作，变量才会被解锁。

如果对⼀个变量执⾏lock操作，将会清空⼯作内存中此变量的值，在执⾏引擎使⽤这个变量 前，需要重新执⾏load或asign操作初始化变量的值。

如果⼀个变量实现没有被lock操作锁定，则不允许对它执⾏unlock操作，也不允许去unlock⼀ 个被其他线程锁定的变量。

对⼀个变量执⾏unlock操作之前，必须先把此变量同步回主内存（执⾏store和write操作）。

volatile型变量的特殊规则 关键字volatile可以说是Java虚拟机提供的最轻量级的同步机制，但是它并不容易完全被正确、完 整的理解，以⾄于许多程序员都不习惯去使⽤它，遇到需要处理多线程的问题的时候⼀律使⽤ synchronized来进⾏同步。了解volatile变量的语义对后⾯了解多线程操作的其他特性很有意义。 Java内存模型对volatile专⻔定义了⼀些特殊的访问规则，当⼀个变量被定义成volatile之后，他将 具备两种特性：

保证此变量对所有线程的可⻅性。第⼀保证此变量对所有线程的可⻅性，这⾥的“可⻅性”是指 当⼀条线程修改了这个变量的值，新值对于其他线程来说是可以⽴即得知的。⽽普通变量是做 不到这点，普通变量的值在线程在线程间传递均需要通过住内存来完成，例如，线程A修改⼀个 普通变量的值，然后向主内存进⾏会写，另外⼀个线程B在线程A回写完成了之后再从主内存进 ⾏读取操作，新变量值才会对线程B可⻅。另外，java⾥⾯的运算并⾮原⼦操作，会导致volatile 变量的运算在并发下⼀样是不安全的。

禁⽌指令重排序优化。普通的变量仅仅会保证在该⽅法的执⾏过程中所有依赖赋值结果的地⽅ 都能获得正确的结果，⽽不能保证变量赋值操作的顺序与程序中的执⾏顺序⼀致，在单线程 中，我们是⽆法感知这⼀点的。

由于volatile变量只能保证可⻅性，在不符合以下两条规则的运算场景中，我们仍然要通过加锁来 保证原⼦性。

- 1.运算结果并不依赖变量的当前值，或者能够确保只有单⼀的线程修改变量的值。

- 2.变量不需要与其他的状态⽐阿尼浪共同参与不变约束。


### 原⼦性、可⻅性与有序性

Java内存模型是围绕着在并发过程中如何处理原⼦性、可⻅性和有序性这三个特征来建⽴的，我们 逐个看下哪些操作实现了这三个特性。

原⼦性（Atomicity）：由Java内存模型来直接保证的原⼦性变量包括read、load、asign、 use、store和write，我们⼤致可以认为基本数据类型的访问读写是具备原⼦性的。如果应⽤场 景需要⼀个更⼤⽅位的原⼦性保证，Java内存模型还提供了lock和unlock操作来满⾜这种需 求，尽管虚拟机未把lock和unlock操作直接开放给⽤户使⽤，但是却提供了更⾼层次的字节码 指令monitorenter和monitorexit来隐式的使⽤这两个操作，这两个字节码指令反应到Java代码 中就是同步块 -synchronized关键字，因此在synchronized块之间的操作也具备原⼦性。

可⻅性（Visibility）：可⻅性是指当⼀个线程修改了共享变量的值，其他线程能够⽴即得知这个 修改。上⽂在讲解volatile变量的时候我们已详细讨论过这⼀点。Java内存模型是通过在变量修 改后将新值同步回主内存，在变量读取前从主内存刷新变量值这种依赖主内存作为传递媒介的 ⽅式来实现可⻅性的，⽆论是普通变量还是volatile变量都是如此，普通变量与volatile变量的区 别是，volatile的特殊规则保证了新值能⽴即同步到主内存，以及每次使⽤前⽴即从主内存刷 新。因此，可以说volatile保证了多线程操作时变量的可⻅性，⽽普通变量则不能保证这⼀点。 除了volatile之外，Java还有两个关键字能实现可⻅性，即synchronized和final.同步快的可⻅性 是由“对⼀个变量执⾏unlock操作前，必须先把此变量同步回主内存”这条规则获得的，⽽final关 键字的可⻅性是指：被final修饰的字段在构造器中⼀旦初始化完成，并且构造器没有把"this"的 引⽤传递出去，那么在其他线程中就能看⻅final字段的值。

有序性（Ordering）：Java内存模型的有序性在前⾯讲解volatile时也详细的讨论过了，Java程 序中天然的有序性可以总结为⼀句话：如果在本线程内观察，所有的操作都是有序的：如果在 ⼀个线程中观察另外⼀个线程，所有的线程操作都是⽆序的。前半句是指“线程内表现为串⾏的 语义”，后半句是指“指令重排序”现象和“⼯作内存与主内存同步延迟”现象。Java语⾔提供了 volatile和synchronized两个关键字来保证线程之间操作的有序性，volatile关键字本身就包含了 禁⽌指令重排序的语义，⽽synchronized则是由“⼀个变量在同⼀个时刻只允许⼀条线程对其进 ⾏lock操作”这条规则获得的，这条规则决定了持有同⼀个锁的两个同步块只能串⾏的进⼊。

## Arays和Colections对于sort的不同实现原理

- 1、Arays.sort() 该算法是⼀个经过调优的快速排序，此算法在很多数据集上提供N*log(N)的性能，这导致其他快速 排序会降低⼆次型性能。
- 2、Colections.sort() 该算法是⼀个经过修改的合并排序算法（其中，如果低⼦列表中的最⾼元素效益⾼⼦列表中的最低 元素，则忽略合并）。此算法可提供保证的N*log(N)的性能，此实现将指定列表转储到⼀个数组 中，然后再对数组进⾏排序，在重置数组中相应位置处每个元素的列表上进⾏迭代。这避免了由于 试图原地对链接列表进⾏排序⽽产⽣的n2log(n)性能。


## Java中object常⽤⽅法

- 1、clone()
- 2、equals()
- 3、finalize()
- 4、getclas()
- 5、hashcode()
- 6、notify()
- 7、notifyAl()
- 8、toString()


## 对于Java中多态的理解

所谓多态就是指程序中定义的引⽤变量所指向的具体类型和通过该引⽤变量发出的⽅法调⽤在编程 时并不确定，⽽是在程序运⾏期间才确定，即⼀个引⽤变量到底会指向哪个类的实例对象，该引⽤ 变量发出的⽅法调⽤到底是哪个类中实现的⽅法，必须在由程序运⾏期间才能决定。因为在程序运 ⾏时才确定具体的类，这样，不⽤修改源程序代码，就可以让引⽤变量绑定到各种不同的类实现 上，从⽽导致该引⽤调⽤的具体⽅法随之改变，即不修改程序代码就可以改变程序运⾏时所绑定的 具体代码，让程序可以选择多个运⾏状态，这就是多态性。 多态的定义：指允许不同类的对象对同⼀消息做出响应。即同⼀消息可以根据发送对象的不同⽽采 ⽤多种不同的⾏为⽅式。（发送消息就是函数调⽤） Java实现多态有三个必要条件：继承、重写、⽗类引⽤指向⼦类对象。 继承：在多态中必须存在有继承关系的⼦类和⽗类。 重写：⼦类对⽗类中某些⽅法进⾏重新定义，在调⽤这些⽅法时就会调⽤⼦类的⽅法。 ⽗类引⽤指向⼦类对象：在多态中需要将⼦类的引⽤赋给⽗类对象，只有这样该引⽤才能够具备技 能调⽤⽗类的⽅法和⼦类的⽅法。 实现多态的技术称为：动态绑定（dynamic binding），是指在执⾏期间判断所引⽤对象的实际类 型，根据其实际的类型调⽤其相应的⽅法。 多态的作⽤：消除类型之间的耦合关系。

Java序列化与反序列化是什么？为什么需要序列化与反序列化？如 何实现Java序列化与反序列化 springAOP实现原理

什么是AOP

AOP（Aspect-OrientedProgra ming，⾯向⽅⾯编程），可以说是 OP（Object-Oriented Programing，⾯向对象编程）的补充和完善。 OP引⼊封装、继承和多态性等概念来建⽴⼀种对 象层次结构，⽤以模拟公共⾏为的⼀个集合。当我们需要为分散的对象引⼊公共⾏为的时候， OP 则显得⽆能为⼒。也就是说， OP允许你定义从上到下的关系，但并不适合定义从左到右的关系。 例如⽇志功能。⽇志代码往往⽔平地散布在所有对象层次中，⽽与它所散布到的对象的核⼼功能毫 ⽆关系。对于其他类型的代码，如安全性、异常处理和透明的持续性也是如此。这种散布在各处的 ⽆关的代码被称为横切（cros-cuting）代码，在 OP设计中，它导致了⼤量代码的重复，⽽不 利于各个模块的重⽤。

⽽AOP技术则恰恰相反，它利⽤⼀种称为“横切”的技术，剖解开封装的对象内部，并将那些影响了 多个类的公共⾏为封装到⼀个可重⽤模块，并将其名为“Aspect”，即⽅⾯。所谓“⽅⾯”，简单地 说，就是将那些与业务⽆关，却为业务模块所共同调⽤的逻辑或责任封装起来，便于减少系统的重 复代码，降低模块间的耦合度，并有利于未来的可操作性和可维护性。AOP代表的是⼀个横向的关 系，如果说“对象”是⼀个空⼼的圆柱体，其中封装的是对象的属性和⾏为；那么⾯向⽅⾯编程的⽅ 法，就仿佛⼀把利刃，将这些空⼼圆柱体剖开，以获得其内部的消息。⽽剖开的切⾯，也就是所谓 的“⽅⾯”了。然后它⼜以巧夺天功的妙⼿将这些剖开的切⾯复原，不留痕迹。 使⽤“横切”技术，AOP把软件系统分为两个部分：核⼼关注点和横切关注点。业务处理的主要流程 是核⼼关注点，与之关系不⼤的部分是横切关注点。横切关注点的⼀个特点是，他们经常发⽣在核 ⼼关注点的多处，⽽各处都基本相似。⽐如权限认证、⽇志、事务处理。Aop 的作⽤在于分离系统 中的各种关注点，将核⼼关注点和横切关注点分离开来。正如Avanade公司的⾼级⽅案构架师 Adam Mage所说，AOP的核⼼思想就是“将应⽤程序中的商业逻辑同对其提供⽀持的通⽤服务进 ⾏分离。” 实现AOP的技术，主要分为两⼤类：⼀是采⽤动态代理技术，利⽤截取消息的⽅式，对该消息进⾏ 装饰，以取代原有对象⾏为的执⾏；⼆是采⽤静态织⼊的⽅式，引⼊特定的语法创建“⽅⾯”，从⽽ 使得编译器可以在编译期间织⼊有关“⽅⾯”的代码。

## AOP使⽤场景

AOP⽤来封装横切关注点，具体可以在下⾯的场景中使⽤: Authentication 权限 Caching 缓存 Context pasing 内容传递 Eror handling 错误处理 Lazy loading 懒加载 Debuging 调试 loging, tracing, profiling and monitoring 记录跟踪 优化 校准 Performance optimization 性能优化 Persistence 持久化 Resource poling 资源池 Synchronization 同步 Transactions 事务

## AOP相关概念

⽅⾯（Aspect）：⼀个关注点的模块化，这个关注点实现可能另外横切多个对象。事务管理是 J2E应⽤中⼀个很好的横切关注点例⼦。⽅⾯⽤ 的 Advisor或拦截器实现。 连接点（Joinpoint）: 程序执⾏过程中明确的点，如⽅法的调⽤或特定的异常被抛出。

spring

通知（Advice）: 在特定的连接点，AOP框架执⾏的动作。各种类型的通知包括“around”、 “before”和“throws”通知。通知类型将在下⾯讨论。许多AOP框架包括Spring都是以拦截器做通知 模型，维护⼀个“围绕”连接点的拦截器链。Spring中定义了四个advice: BeforeAdvice, AfterAdvice, ThrowAdvice和DynamicIntroductionAdvice 切⼊点（Pointcut）: 指定⼀个通知将被引发的⼀系列连接点的集合。AOP框架必须允许开发者指 定切⼊点：例如，使⽤正则表达式。 Spring定义了Pointcut接⼝，⽤来组合MethodMatcher和 ClasFilter，可以通过名字很清楚的理解， MethodMatcher是⽤来检查⽬标类的⽅法是否可以被 应⽤此通知，⽽ClasFilter是⽤来检查Pointcut是否应该应⽤到⽬标类上 引⼊（Introduction）: 添加⽅法或字段到被通知的类。 Spring允许引⼊新的接⼝到任何被通知的 对象。例如，你可以使⽤⼀个引⼊使任何对象实现 IsModified接⼝，来简化缓存。Spring中要使⽤ Introduction, 可有通过DelegatingIntroductionInterceptor来实现通知，通过 DefaultIntroductionAdvisor来配置Advice和代理类要实现的接⼝ ⽬标对象（Target Object）: 包含连接点的对象。也被称作被通知或被代理对象。POJO AOP代理（AOP Proxy）: AOP框架创建的对象，包含通知。 在Spring中，AOP代理可以是JDK动 态代理或者CGLIB代理。 织⼊（Weaving）: 组装⽅⾯来创建⼀个被通知对象。这可以在编译时完成（例如使⽤AspectJ编译 器），也可以在运⾏时完成。Spring和其他纯 AOP框架⼀样，在运⾏时完成织⼊。

Java

## SpringAOP组件

下⾯这种类图列出了Spring中主要的AOP组件

<table>
  <tr>
    <th>![image 12](assets/imageFile12.png)</th>
  </tr>
</table>


img

## 如何使⽤SpringAOP

可以通过配置⽂件或者编程的⽅式来使⽤Spring AOP。 配置可以通过xml⽂件来进⾏，⼤概有四种⽅式： \1. 配置ProxyFactoryBean，显式地设置advisors, advice, target等

- 1.
- 2.
- 3.


配置AutoProxyCreator，这种⽅式下，还是如以前⼀样使⽤定义的bean，但是从容器中获得的其实已经是代理 对象

通过<aop:config>来配置 通过<aop: aspectj-autoproxy>来配置，使⽤AspectJ的注解来标识通知及切⼊点

也可以直接使⽤ProxyFactory来以编程的⽅式使⽤Spring AOP，通过ProxyFactory提供的⽅法可以 设置target对象, advisor等相关配置，最终通过 getProxy()⽅法来获取代理对象 具体使⽤的示例可以gogle. 这⾥略去

## SpringAOP代理对象的⽣成

Spring提供了两种⽅式来⽣成代理对象: JDKProxy和Cglib，具体使⽤哪种⽅式⽣成由 AopProxyFactory根据AdvisedSuport对象的配置来决定。默认的策略是如果⽬标类是接⼝，则使 ⽤JDK动态代理技术，否则使⽤Cglib来⽣成代理。下⾯我们来研究⼀下Spring如何使⽤JDK来⽣成 代理对象，具体的⽣成代码放在JdkDynamicAopProxy这个类中，直接上相关代码： 友情链接 ：

Spring AOP 实现原理

/**

- * <ol>
- * <li>获取代理类要实现的接⼝,除了Advised对象中配置的,还会加上SpringProxy,


Advised(opaque=false)

- * <li>检查上⾯得到的接⼝中有没有定义 equals或者hashcode的接⼝
- * <li>调⽤Proxy.newProxyInstance创建代理对象
- * </ol>
- */


public Object getProxy(ClassLoader classLoader) { if (logger.isDebugEnabled()) { logger.debug("Creating JDK dynamic proxy: target source is "

+this.advised.getTargetSource()); } Class[] proxiedInterfaces =AopProxyUtils.completeProxiedInterfaces(this.advised); findDefinedEqualsAndHashCodeMethods(proxiedInterfaces); return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);

}

那这个其实很明了，注释上我也已经写清楚了，不再赘述。 下⾯的问题是，代理对象⽣成了，那切⾯是如何织⼊的？

我们知道InvocationHandler是JDK动态代理的核⼼，⽣成的代理对象的⽅法调⽤都会委托到 InvocationHandler.invoke()⽅法。⽽通过JdkDynamicAopProxy的签名我们可以看到这个类其实也 实现了InvocationHandler，下⾯我们就通过分析这个类中实现的invoke()⽅法来具体看下Spring AOP是如何织⼊切⾯的。

Servlet⼯作原理

# Servlet ⼯作原理解析

从 Servlet容器说起

前⾯说了 Servlet 容器作为⼀个独⽴发展的标准化产品，⽬前它的种类很多，但是它们都有⾃⼰的 市场定位，很难说谁优谁劣，各有特点。例如现在⽐较流⾏的 Jety，在定制化和移动领域有不错 的发展，我们这⾥还是以⼤家最为熟悉 Tomcat 为例来介绍 Servlet 容器如何管理 Servlet。 Tomcat 本身也很复杂，我们只从 Servlet 与 Servlet 容器的接⼝部分开始介绍，关于 Tomcat 的详 细介绍可以参考我的另外⼀篇⽂章《 Tomcat 系统架构与模式设计分析》。 Tomcat 的容器等级中，Context 容器是直接管理 Servlet 在容器中的包装类 Wraper，所以 Context 容器如何运⾏将直接影响 Servlet 的⼯作⽅式。

- 图 1 . Tomcat 容器模型 从上图可以看出 Tomcat 的容器分为四个等级，真正管理 Servlet 的容器是 Context 容器，⼀个 Context 对应⼀个 Web ⼯程，在 Tomcat 的配置⽂件中可以很容易发现这⼀点，如下：


- 清单 1 Context 配置参数 <Context path="/projectOne " docBase="D:\projects\projectOne" reloadable="true" /> 下⾯详细介绍⼀下 Tomcat 解析 Context 容器的过程，包括如何构建 Servlet 的过程。 Servlet 容器的启动过程 Tomcat7 也开始⽀持嵌⼊式功能，增加了⼀个启动类 org.apache.catalina.startup.Tomcat。创建 ⼀个实例对象并调⽤ start ⽅法就可以很容易启动 Tomcat，我们还可以通过这个对象来增加和修 改 Tomcat 的配置参数，如可以动态增加 Context、Servlet 等。下⾯我们就利⽤这个 Tomcat 类来 管理新增的⼀个 Context 容器，我们就选择 Tomcat7 ⾃带的 examples Web ⼯程，并看看它是如 何加到这个 Context 容器中的。
- 清单 2 . 给 Tomcat 增加⼀个 Web ⼯程 Tomcat tomcat = getTomcatInstance(); File appDir = new File(getBuildDirectory(), "webapps/examples"); tomcat.addWebapp(null, "/examples", appDir.getAbsolutePath()); tomcat.start(); ByteChunk res = getUrl("http://localhost:" + getPort() +


"/examples/servlets/servlet/HelloWorldExample"); assertTrue(res.toString().indexOf("<h1>Hello World!</h1>") > 0);

清单 1 的代码是创建⼀个 Tomcat 实例并新增⼀个 Web 应⽤，然后启动 Tomcat 并调⽤其中的⼀ 个 HeloWorldExample Servlet，看有没有正确返回预期的数据。 Tomcat 的 adWebap ⽅法的代码如下：

- 清单 3 .Tomcat.adWebap public Context addWebapp(Host host, String url, String path) {


silence(url); Context ctx = new StandardContext(); ctx.setPath( url ); ctx.setDocBase(path); if (defaultRealm == null) {

initSimpleAuth();

} ctx.setRealm(defaultRealm); ctx.addLifecycleListener(new DefaultWebXmlListener()); ContextConfig ctxCfg = new ContextConfig(); ctx.addLifecycleListener(ctxCfg); ctxCfg.setDefaultWebXml("org/apache/catalin/startup/NO_DEFAULT_XML"); if (host == null) {

getHost().addChild(ctx); } else {

host.addChild(ctx);

} return ctx;

}

前⾯已经介绍了⼀个 Web 应⽤对应⼀个 Context 容器，也就是 Servlet 运⾏时的 Servlet 容器，添 加⼀个 Web 应⽤时将会创建⼀个 StandardContext 容器，并且给这个 Context 容器设置必要的参 数，url 和 path 分别代表这个应⽤在 Tomcat 中的访问路径和这个应⽤实际的物理路径，这个两个 参数与清单 1 中的两个参数是⼀致的。其中最重要的⼀个配置是 ContextConfig，这个类将会负责 整个 Web 应⽤配置的解析⼯作，后⾯将会详细介绍。最后将这个 Context 容器加到⽗容器 Host 中。 接下去将会调⽤ Tomcat 的 start ⽅法启动 Tomcat，如果你清楚 Tomcat 的系统架构，你会容易理 解 Tomcat 的启动逻辑，Tomcat 的启动逻辑是基于观察者模式设计的，所有的容器都会继承 Lifecycle 接⼝，它管理者容器的整个⽣命周期，所有容器的的修改和状态的改变都会由它去通知 已经注册的观察者（Listener），关于这个设计模式可以参考《 Tomcat 的系统架构与设计模式， 第⼆部分：设计模式》。Tomcat 启动的时序图可以⽤图 2 表示。

- 图 2. Tomcat 主要类的启动时序图（ ） 上图描述了 Tomcat 启动过程中，主要类之间的时序关系，下⾯我们将会重点关注添加 examples 应⽤所对应的 StandardContext 容器的启动过程。 当 Context 容器初始化状态设为 init 时，添加在 Contex 容器的 Listener 将会被调⽤。 ContextConfig 继承了 LifecycleListener 接⼝，它是在调⽤清单 3 时被加⼊到 StandardContext 容器中。ContextConfig 类会负责整个 Web 应⽤的配置⽂件的解析⼯作。


查看⼤图

ContextConfig 的 init ⽅法将会主要完成以下⼯作：

- 1.
- 2.
- 3.
- 4.
- 5.


创建⽤于解析 xml 配置⽂件的 contextDigester 对象 读取默认 context.xml 配置⽂件，如果存在解析它 读取默认 Host 配置⽂件，如果存在解析它 读取默认 Context ⾃身的配置⽂件，如果存在解析它 设置 Context 的 DocBase

ContextConfig 的 init ⽅法完成后，Context 容器的会执⾏ startInternal ⽅法，这个⽅法启动逻辑 ⽐较复杂，主要包括如下⼏个部分：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


创建读取资源⽂件的对象 创建 ClasLoader 对象 设置应⽤的⼯作⽬录 启动相关的辅助类如：loger、realm、resources 等 修改启动状态，通知感兴趣的观察者（Web 应⽤的配置） ⼦容器的初始化 获取 ServletContext 并设置必要的参数 初始化“load on startup”的 Servlet

### Web 应⽤的初始化⼯作

Web 应⽤的初始化⼯作是在 ContextConfig 的 configureStart ⽅法中实现的，应⽤的初始化主要 是要解析 web.xml ⽂件，这个⽂件描述了⼀个 Web 应⽤的关键信息，也是⼀个 Web 应⽤的⼊ ⼝。 Tomcat ⾸先会找 globalWebXml 这个⽂件的搜索路径是在 engine 的⼯作⽬录下寻找以下两个⽂ 件中的任⼀个 org/apache/catalin/startup/NO_DEFAULT_XML 或 conf/web.xml。接着会找 hostWebXml 这个⽂件可能会在 System.getProperty("catalina.base")/conf/${EngineName}/${HostName}/web.xml.default，接着 寻找应⽤的配置⽂件 examples/WEB-INF/web.xml。web.xml ⽂件中的各个配置项将会被解析成相 应的属性保存在 WebXml 对象中。如果当前应⽤⽀持 Servlet3.0，解析还将完成额外 9 项⼯作， 这个额外的 9 项⼯作主要是为 Servlet3.0 新增的特性，包括 jar 包中的 META-INF/webfragment.xml 的解析以及对 anotations 的⽀持。 接下去将会将 WebXml 对象中的属性设置到 Context 容器中，这⾥包括创建 Servlet 对象、filter、 listener 等等。这段代码在 WebXml 的 configureContext ⽅法中。下⾯是解析 Servlet 的代码⽚ 段：

- 清单 4. 创建 Wraper 实例


for (ServletDef servlet : servlets.values()) { Wrapper wrapper = context.createWrapper(); String jspFile = servlet.getJspFile(); if (jspFile != null) {

wrapper.setJspFile(jspFile);

} if (servlet.getLoadOnStartup() != null) {

wrapper.setLoadOnStartup(servlet.getLoadOnStartup().intValue());

} if (servlet.getEnabled() != null) {

wrapper.setEnabled(servlet.getEnabled().booleanValue());

} wrapper.setName(servlet.getServletName()); Map<String,String> params = servlet.getParameterMap(); for (Entry<String, String> entry : params.entrySet()) {

wrapper.addInitParameter(entry.getKey(), entry.getValue());

} wrapper.setRunAs(servlet.getRunAs()); Set<SecurityRoleRef> roleRefs = servlet.getSecurityRoleRefs(); for (SecurityRoleRef roleRef : roleRefs) {

wrapper.addSecurityReference( roleRef.getName(), roleRef.getLink());

} wrapper.setServletClass(servlet.getServletClass()); MultipartDef multipartdef = servlet.getMultipartDef(); if (multipartdef != null) {

if (multipartdef.getMaxFileSize() != null && multipartdef.getMaxRequestSize()!= null && multipartdef.getFileSizeThreshold() != null) {

wrapper.setMultipartConfigElement(new MultipartConfigElement(

multipartdef.getLocation(), Long.parseLong(multipartdef.getMaxFileSize()), Long.parseLong(multipartdef.getMaxRequestSize()), Integer.parseInt(

multipartdef.getFileSizeThreshold()))); } else {

wrapper.setMultipartConfigElement(new MultipartConfigElement(

multipartdef.getLocation())); }

} if (servlet.getAsyncSupported() != null) {

wrapper.setAsyncSupported( servlet.getAsyncSupported().booleanValue());

} context.addChild(wrapper);

}

这段代码清楚的描述了如何将 Servlet 包装成 Context 容器中的 StandardWraper，这⾥有个疑 问，为什么要将 Servlet 包装成 StandardWraper ⽽不直接是 Servlet 对象。这⾥ StandardWraper 是 Tomcat 容器中的⼀部分，它具有容器的特征，⽽ Servlet 为了⼀个独⽴的 web 开发标准，不应该强耦合在 Tomcat 中。 除了将 Servlet 包装成 StandardWraper 并作为⼦容器添加到 Context 中，其它的所有 web.xml 属性都被解析到 Context 中，所以说 Context 容器才是真正运⾏ Servlet 的 Servlet 容器。⼀个 Web 应⽤对应⼀个 Context 容器，容器的配置属性由应⽤的 web.xml 指定，这样我们就能理解 web.xml 到底起到什么作⽤了。

回⻚⾸

## 创建 Servlet实例

前⾯已经完成了 Servlet 的解析⼯作，并且被包装成 StandardWraper 添加在 Context 容器中， 但是它仍然不能为我们⼯作，它还没有被实例化。下⾯我们将介绍 Servlet 对象是如何创建的，以 及如何被初始化的。

创建 Servlet 对象

如果 Servlet 的 load-on-startup 配置项⼤于 0，那么在 Context 容器启动的时候就会被实例化， 前⾯提到在解析配置⽂件时会读取默认的 globalWebXml，在 conf 下的 web.xml ⽂件中定义了⼀ 些默认的配置项，其定义了两个 Servlet，分别是：org.apache.catalina.servlets.DefaultServlet 和 org.apache.jasper.servlet.JspServlet 它们的 load-on-startup 分别是 1 和 3，也就是当 Tomcat 启动时这两个 Servlet 就会被启动。 创建 Servlet 实例的⽅法是从 Wraper. loadServlet 开始的。loadServlet ⽅法要完成的就是获取 servletClas 然后把它交给 InstanceManager 去创建⼀个基于 servletClas.clas 的对象。如果这 个 Servlet 配置了 jsp-file，那么这个 servletClas 就是 conf/web.xml 中定义的 org.apache.jasper.servlet.JspServlet 了。 创建 Servlet 对象的相关类结构图如下：

- 图 3. 创建 Servlet 对象的相关类结构 初始化 Servlet 初始化 Servlet 在 StandardWraper 的 initServlet ⽅法中，这个⽅法很简单就是调⽤ Servlet 的 init 的⽅法，同时把包装了 StandardWraper 对象的 StandardWraperFacade 作为 ServletConfig 传给 Servlet。Tomcat 容器为何要传 StandardWraperFacade 给 Servlet 对象将在 后⾯做详细解析。 如果该 Servlet 关联的是⼀个 jsp ⽂件，那么前⾯初始化的就是 JspServlet，接下去会模拟⼀次简 单请求，请求调⽤这个 jsp ⽂件，以便编译这个 jsp ⽂件为 clas，并初始化这个 clas。


这样 Servlet 对象就初始化完成了，事实上 Servlet 从被 web.xml 中解析到完成初始化，这个过程 ⾮常复杂，中间有很多过程，包括各种容器状态的转化引起的监听事件的触发、各种访问权限的控 制和⼀些不可预料的错误发⽣的判断⾏为等等。我们这⾥只抓了⼀些关键环节进⾏阐述，试图让⼤ 家有个总体脉络。 下⾯是这个过程的⼀个完整的时序图，其中也省略了⼀些细节。

- 图 4. 初始化 Servlet 的时序图（ ）

Servlet体系结构

我们知道 Java Web 应⽤是基于 Servlet 规范运转的，那么 Servlet 本身⼜是如何运转的呢？为何 要设计这样的体系结构。

- 图 5.Servlet 顶层类关联图 从上图可以看出 Servlet 规范就是基于这⼏个类运转的，与 Servlet 主动关联的是三个类，分别是 ServletConfig、ServletRequest 和 ServletResponse。这三个类都是通过容器传递给 Servlet 的， 其中 ServletConfig 是在 Servlet 初始化时就传给 Servlet 了，⽽后两个是在请求达到时调⽤ Servlet 时传递过来的。我们很清楚 ServletRequest 和 ServletResponse 在 Servlet 运⾏的意义， 但是 ServletConfig 和 ServletContext 对 Servlet 有何价值？仔细查看 ServletConfig 接⼝中声明 的⽅法发现，这些⽅法都是为了获取这个 Servlet 的⼀些配置属性，⽽这些配置属性可能在 Servlet 运⾏时被⽤到。⽽ ServletContext ⼜是⼲什么的呢？ Servlet 的运⾏模式是⼀个典型的“握 ⼿型的交互式”运⾏模式。所谓“握⼿型的交互式”就是两个模块为了交换数据通常都会准备⼀个交 易场景，这个场景⼀直跟随个这个交易过程直到这个交易完成为⽌。这个交易场景的初始化是根据 这次交易对象指定的参数来定制的，这些指定参数通常就会是⼀个配置类。所以对号⼊座，交易场 景就由 ServletContext 来描述，⽽定制的参数集合就由 ServletConfig 来描述。⽽ ServletRequest 和 ServletResponse 就是要交互的具体对象了，它们通常都是作为运输⼯具来传 递交互结果。 ServletConfig 是在 Servlet init 时由容器传过来的，那么 ServletConfig 到底是个什么对象呢？ 下图是 ServletConfig 和 ServletContext 在 Tomcat 容器中的类关系图。
- 图 6. ServletConfig 在容器中的类关联图 上图可以看出 StandardWraper 和 StandardWraperFacade 都实现了 ServletConfig 接⼝，⽽ StandardWraperFacade 是 StandardWraper ⻔⾯类。所以传给 Servlet 的是 StandardWraperFacade 对象，这个类能够保证从 StandardWraper 中拿到 ServletConfig 所规 定的数据，⽽⼜不把 ServletConfig 不关⼼的数据暴露给 Servlet。 同样 ServletContext 也与 ServletConfig 有类似的结构，Servlet 中能拿到的 ServletContext 的实 际对象也是 AplicationContextFacade 对象。AplicationContextFacade 同样保证 ServletContex 只能从容器中拿到它该拿的数据，它们都起到对数据的封装作⽤，它们使⽤的都是 ⻔⾯设计模式。 通过 ServletContext 可以拿到 Context 容器中⼀些必要信息，⽐如应⽤的⼯作路径，容器⽀持的 Servlet 最⼩版本等。


查看⼤图

回⻚⾸

Servlet 中定义的两个 ServletRequest 和 ServletResponse 它们实际的对象⼜是什么呢？，我们在 创建⾃⼰的 Servlet 类时通常使⽤的都是 HtpServletRequest 和 HtpServletResponse，它们继承 了 ServletRequest 和 ServletResponse。为何 Context 容器传过来的 ServletRequest、 ServletResponse 可以被转化为 HtpServletRequest 和 HtpServletResponse 呢？

- 图 7.Request 相关类结构图 上图是 Tomcat 创建的 Request 和 Response 的类结构图。Tomcat ⼀接受到请求⾸先将会创建 org.apache.coyote.Request 和 org.apache.coyote.Response，这两个类是 Tomcat 内部使⽤的描 述⼀次请求和相应的信息类它们是⼀个轻量级的类，它们作⽤就是在服务器接收到请求后，经过简 单解析将这个请求快速的分配给后续线程去处理，所以它们的对象很⼩，很容易被 JVM 回收。接 下去当交给⼀个⽤户线程去处理这个请求时⼜创建 org.apache.catalina.conector. Request 和 org.apache.catalina.conector. Response 对象。这两个对象⼀直穿越整个 Servlet 容器直到要传 给 Servlet，传给 Servlet 的是 Request 和 Response 的⻔⾯类 RequestFacade 和 RequestFacade，这⾥使⽤⻔⾯模式与前⾯⼀样都是基于同样的⽬的⸺封装容器中的数据。⼀次 请求对应的 Request 和 Response 的类转化如下图所示：
- 图 8.Request 和 Response 的转变过程

Servlet如何⼯作

我们已经清楚了 Servlet 是如何被加载的、Servlet 是如何被初始化的，以及 Servlet 的体系结构， 现在的问题就是它是如何被调⽤的。 当⽤户从浏览器向服务器发起⼀个请求，通常会包含如下信息： : port /contextpath/servletpath，hostname 和 port 是⽤来与服务器建⽴ TCP 连接，⽽后⾯的 URL 才是 ⽤来选择服务器中那个⼦容器服务⽤户的请求。那服务器是如何根据这个 URL 来达到正确的 Servlet 容器中的呢？ Tomcat7.0 中这件事很容易解决，因为这种映射⼯作有专⻔⼀个类来完成的，这个就是 org.apache.tomcat.util.htp.maper，这个类保存了 Tomcat 的 Container 容器中的所有⼦容器的 信息，当 org.apache.catalina.conector. Request 类在进⼊ Container 容器之前，maper 将会根 据这次请求的 hostnane 和 contextpath 将 host 和 context 容器设置到 Request 的 mapingData 属性中。所以当 Request 进⼊ Container 容器之前，它要访问那个⼦容器这时就已经确定了。

- 图 9.Request 的 Maper 类关系图 可能你有疑问，maper 中怎么会有容器的完整关系，这要回到图 2 中 19 步 MaperListener 类的 初始化过程，下⾯是 MaperListener 的 init ⽅法代码 :


回⻚⾸

htp:/hostname

- 清单 5. MaperListener.init


public void init() { findDefaultHost(); Engine engine = (Engine) connector.getService().getContainer(); engine.addContainerListener(this); Container[] conHosts = engine.findChildren(); for (Container conHost : conHosts) {

Host host = (Host) conHost; if (!LifecycleState.NEW.equals(host.getState())) {

host.addLifecycleListener(this); registerHost(host);

} }

}

这段代码的作⽤就是将 MaperListener 类作为⼀个监听者加到整个 Container 容器中的每个⼦容 器中，这样只要任何⼀个容器发⽣变化，MaperListener 都将会被通知，相应的保存容器关系的 MaperListener 的 maper 属性也会修改。for 循环中就是将 host 及下⾯的⼦容器注册到 maper 中。

- 图 10.Request 在容器中的路由图 上图描述了⼀次 Request 请求是如何达到最终的 Wraper 容器的，我们现正知道了请求是如何达 到正确的 Wraper 容器，但是请求到达最终的 Servlet 还要完成⼀些步骤，必须要执⾏ Filter 链， 以及要通知你在 web.xml 中定义的 listener。 接下去就要执⾏ Servlet 的 service ⽅法了，通常情况下，我们⾃⼰定义的 servlet 并不是直接去实 现 javax.servlet.servlet 接⼝，⽽是去继承更简单的 HtpServlet 类或者 GenericServlet 类，我们 可以有选择的覆盖相应⽅法去实现我们要完成的⼯作。 Servlet 的确已经能够帮我们完成所有的⼯作了，但是现在的 web 应⽤很少有直接将交互全部⻚⾯ 都⽤ servlet 来实现，⽽是采⽤更加⾼效的 MVC 框架来实现。这些 MVC 框架基本的原理都是将所 有的请求都映射到⼀个 Servlet，然后去实现 service ⽅法，这个⽅法也就是 MVC 框架的⼊⼝。 当 Servlet 从 Servlet 容器中移除时，也就表明该 Servlet 的⽣命周期结束了，这时 Servlet 的 destroy ⽅法将被调⽤，做⼀些扫尾⼯作。


回⻚⾸

## Sesion与 Cokie

前⾯我们已经说明了 Servlet 如何被调⽤，我们基于 Servlet 来构建应⽤程序，那么我们能从 Servlet 获得哪些数据信息呢？

Servlet 能够给我们提供两部分数据，⼀个是在 Servlet 初始化时调⽤ init ⽅法时设置的 ServletConfig，这个类基本上含有了 Servlet 本身和 Servlet 所运⾏的 Servlet 容器中的基本信 息。根据前⾯的介绍 ServletConfig 的实际对象是 StandardWraperFacade，到底能获得哪些容 器信息可以看看这类提供了哪些接⼝。还有⼀部分数据是由 ServletRequest 类提供，它的实际对 象是 RequestFacade，从提供的⽅法中发现主要是描述这次请求的 HTP 协议的信息。所以要掌 握 Servlet 的⼯作⽅式必须要很清楚 HTP 协议，如果你还不清楚赶紧去找⼀些参考资料。关于这 ⼀块还有⼀个让很多⼈迷惑的 Sesion 与 Cokie。 Sesion 与 Cokie 不管是对 Java Web 的熟练使⽤者还是初学者来说都是⼀个令⼈头疼的东⻄。 Sesion 与 Cokie 的作⽤都是为了保持访问⽤户与后端服务器的交互状态。它们有各⾃的优点也 有各⾃的缺陷。然⽽具有讽刺意味的是它们优点和它们的使⽤场景⼜是⽭盾的，例如使⽤ Cokie 来传递信息时，随着 Cokie 个数的增多和访问量的增加，它占⽤的⽹络带宽也很⼤，试想假如 Cokie 占⽤ 20 个字节，如果⼀天的 PV 有⼏亿的时候，它要占⽤多少带宽。所以⼤访问量的时 候希望⽤ Sesion，但是 Sesion 的致命弱点是不容易在多台服务器之间共享，所以这也限制了 Sesion 的使⽤。 不管 Sesion 和 Cokie 有什么不⾜，我们还是要⽤它们。下⾯详细讲⼀下，Sesion 如何基于 Cokie 来⼯作。实际上有三种⽅式能可以让 Sesion 正常⼯作：

- 1.
- 2.
- 3.


基于 URL Path Parameter，默认就⽀持 基于 Cokie，如果你没有修改 Context 容器个 cokies 标识的话，默认也是⽀持的 基于 SL，默认不⽀持，只有 conector.getAtribute("SLEnabled") 为 TRUE 时才⽀持

第⼀种情况下，当浏览器不⽀持 Cokie 功能时，浏览器会将⽤户的 SesionCokieName 重写到 ⽤户请求的 URL 参数中，它的传递格式如 /path/Servlet;name=value;name2=value2? Name3=value3，其中“Servlet；”后⾯的 K-V 对就是要传递的 Path Parameters，服务器会从这个 Path Parameters 中拿到⽤户配置的 SesionCokieName。关于这个 SesionCokieName，如果 你在 web.xml 中配置 sesion-config 配置项的话，其 cokie-config 下的 name 属性就是这个 SesionCokieName 值，如果你没有配置 sesion-config 配置项，默认的 SesionCokieName 就是⼤家熟悉的“JSESIONID”。接着 Request 根据这个 SesionCokieName 到 Parameters 拿 到 Sesion ID 并设置到 request.setRequestedSesionId 中。 请注意如果客户端也⽀持 Cokie 的话，Tomcat 仍然会解析 Cokie 中的 Sesion ID，并会覆盖 URL 中的 Sesion ID。 如果是第三种情况的话将会根据 javax.servlet.request.sl_sesion 属性值设置 Sesion ID。 有了 Sesion ID 服务器端就可以创建 HtpSesion 对象了，第⼀次触发是通过 request. getSesion() ⽅法，如果当前的 Sesion ID 还没有对应的 HtpSesion 对象那么就创建⼀个新 的，并将这个对象加到 org.apache.catalina. Manager 的 sesions 容器中保存，Manager 类将管 理所有 Sesion 的⽣命周期，Sesion 过期将被回收，服务器关闭，Sesion 将被序列化到磁盘 等。只要这个 HtpSesion 对象存在，⽤户就可以根据 Sesion ID 来获取到这个对象，也就达到 了状态的保持。

图 1.Sesion 相关类图

上从图中可以看出从 request.getSesion 中获取的 HtpSesion 对象实际上是 StandardSesion 对象的⻔⾯对象，这与前⾯的 Request 和 Servlet 是⼀样的原理。下图是 Sesion ⼯作的时序图：

- 图 12.Sesion ⼯作的时序图（ ） 还有⼀点与 Sesion 关联的 Cokie 与其它 Cokie 没有什么不同，这个配置的配置可以通过 web.xml 中的 sesion-config 配置项来指定。

Servlet中的 Listener

整个 Tomcat 服务器中 Listener 使⽤的⾮常⼴泛，它是基于观察者模式设计的，Listener 的设计对 开发 Servlet 应⽤程序提供了⼀种快捷的⼿段，能够⽅便的从另⼀个纵向维度控制程序和数据。⽬ 前 Servlet 中提供了 5 种两类事件的观察者接⼝，它们分别是：4 个 EventListeners 类型的， ServletContextAtributeListener、ServletRequestAtributeListener、ServletRequestListener、 HtpSesionAtributeListener 和 2 个 LifecycleListeners 类型的，ServletContextListener、 HtpSesionListener。如下图所示：

- 图 13.Servlet 中的 Listener（ ） 它们基本上涵盖了整个 Servlet ⽣命周期中，你感兴趣的每种事件。这些 Listener 的实现类可以配 置在 web.xml 中的 <listener> 标签中。当然也可以在应⽤程序中动态添加 Listener，需要注意的 是 ServletContextListener 在容器启动之后就不能再添加新的，因为它所监听的事件已经不会再出 现。掌握这些 Listener 的使⽤，能够让我们的程序设计的更加灵活


查看⼤图

回⻚⾸

查看⼤图

## JavaNIO和IO的区别

下表总结了Java NIO和IO之间的主要差别，我会更详细地描述表中每部分的差异。 代码如下:

复制代码

IO NIO ⾯向流 ⾯向缓冲 阻塞IO ⾮阻塞IO ⽆ 选择器 ⾯向流与⾯向缓冲 Java NIO和IO之间第⼀个最⼤的区别是，IO是⾯向流的，NIO是⾯向缓冲区的。 Java IO⾯向流意 味着每次从流中读⼀个或多个字节，直⾄读取所有字节，它们没有被缓存在任何地⽅。此外，它不 能前后移动流中的数据。如果需要前后移动从流中读取的数据，需要先将它缓存到⼀个缓冲区。 Java NIO的缓冲导向⽅法略有不同。数据读取到⼀个它稍后处理的缓冲区，需要时可在缓冲区中前 后移动。这就增加了处理过程中的灵活性。但是，还需要检查是否该缓冲区中包含所有您需要处理 的数据。⽽且，需确保当更多的数据读⼊缓冲区时，不要覆盖缓冲区⾥尚未处理的数据。 阻塞与⾮阻塞IO

Java IO的各种流是阻塞的。这意味着，当⼀个线程调⽤read() 或 write()时，该线程被阻塞，直到 有⼀些数据被读取，或数据完全写⼊。该线程在此期间不能再⼲任何事情了。 Java NIO的⾮阻塞 模式，使⼀个线程从某通道发送请求读取数据，但是它仅能得到⽬前可⽤的数据，如果⽬前没有数 据可⽤时，就什么都不会获取。⽽不是保持线程阻塞，所以直⾄数据变的可以读取之前，该线程可 以继续做其他的事情。 ⾮阻塞写也是如此。⼀个线程请求写⼊⼀些数据到某通道，但不需要等待 它完全写⼊，这个线程同时可以去做别的事情。 线程通常将⾮阻塞IO的空闲时间⽤于在其它通道 上执⾏IO操作，所以⼀个单独的线程现在可以管理多个输⼊和输出通道（chanel）。 选择器（Selectors） Java NIO的选择器允许⼀个单独的线程来监视多个输⼊通道，你可以注册多个通道使⽤⼀个选择 器，然后使⽤⼀个单独的线程来“选择”通道：这些通道⾥已经有可以处理的输⼊，或者选择已准备 写⼊的通道。这种选择机制，使得⼀个单独的线程很容易来管理多个通道。 NIO和IO如何影响应⽤程序的设计 ⽆论您选择IO或NIO⼯具箱，可能会影响您应⽤程序设计的以下⼏个⽅⾯：

- 1.对NIO或IO类的API调⽤。
- 2.数据处理。
- 3.⽤来处理数据的线程数。 API调⽤ 当然，使⽤NIO的API调⽤时看起来与使⽤IO时有所不同，但这并不意外，因为并不是仅从⼀个 InputStream逐字节读取，⽽是数据必须先读⼊缓冲区再处理。 数据处理 使⽤纯粹的NIO设计相较IO设计，数据处理也受到影响。 在IO设计中，我们从InputStream或 Reader逐字节读取数据。假设你正在处理⼀基于⾏的⽂本数据 流，例如：


代码如下: Name: Ana Age: 25 Email: Phone: 1234567890 该⽂本⾏的流可以这样处理：

复制代码

ana@mailserver.com

代码如下: BuferedReader reader = new BuferedReader(new InputStreamReader(input); String nameLine = reader.readLine(); String ageLine = reader.readLine(); String emailLine = reader.readLine(); String phoneLine = reader.readLine();

复制代码

请注意处理状态由程序执⾏多久决定。换句话说，⼀旦reader.readLine()⽅法返回，你就知道肯定 ⽂本⾏就已读完， readline()阻塞直到整⾏读完，这就是原因。你也知道此⾏包含名称；同样，第 ⼆个readline()调⽤返回的时候，你知道这⾏包含年龄等。 正如你可以看到，该处理程序仅在有新 数据读⼊时运⾏，并知道每步的数据是什么。⼀旦正在运⾏的线程已处理过读⼊的某些数据，该线 程不会再回退数据（⼤多如此）。下图也说明了这条原则：

<table>
  <tr>
    <th>![image 13](assets/imageFile13.png)</th>
  </tr>
</table>


（Java IO: 从⼀个阻塞的流中读数据） ⽽⼀个NIO的实现会有所不同，下⾯是⼀个简单的例⼦：

代码如下: ByteBufer bufer = ByteBufer.alocate(48); int bytesRead = inChanel.read(bufer); 注意第⼆⾏，从通道读取字节到ByteBufer。当这个⽅法调⽤返回时，你不知道你所需的所有数据 是否在缓冲区内。你所知道的是，该缓冲区包含⼀些字节，这使得处理有点困难。 假设第⼀次 read(bufer)调⽤后，读⼊缓冲区的数据只有半⾏，例如，“Name:An”，你能处理数据 吗？显然不能，需要等待，直到整⾏数据读⼊缓存，在此之前，对数据的任何处理毫⽆意义。 所以，你怎么知道是否该缓冲区包含⾜够的数据可以处理呢？好了，你不知道。发现的⽅法只能查 看缓冲区中的数据。其结果是，在你知道所有数据都在缓冲区⾥之前，你必须检查⼏次缓冲区的数 据。这不仅效率低下，⽽且可以使程序设计⽅案杂乱不堪。例如：

复制代码

代码如下: ByteBufer bufer = ByteBufer.alocate(48); int bytesRead = inChanel.read(bufer); while(! buferFul(bytesRead) ) { bytesRead = inChanel.read(bufer); } buferFul()⽅法必须跟踪有多少数据读⼊缓冲区，并返回真或假，这取决于缓冲区是否已满。换句 话说，如果缓冲区准备好被处理，那么表示缓冲区满了。 buferFul()⽅法扫描缓冲区，但必须保持在buferFul（）⽅法被调⽤之前状态相同。如果没有， 下⼀个读⼊缓冲区的数据可能⽆法读到正确的位置。这是不可能的，但却是需要注意的⼜⼀问题。 如果缓冲区已满，它可以被处理。如果它不满，并且在你的实际案例中有意义，你或许能处理其中 的部分数据。但是许多情况下并⾮如此。下图展示了“缓冲区数据循环就绪”：

复制代码

<table>
  <tr>
    <th>![image 14](assets/imageFile14.png)</th>
  </tr>
</table>


3) ⽤来处理数据的线程数 NIO可让您只使⽤⼀个（或⼏个）单线程管理多个通道（⽹络连接或⽂件），但付出的代价是解析 数据可能会⽐从⼀个阻塞流中读取数据更复杂。 如果需要管理同时打开的成千上万个连接，这些连接每次只是发送少量的数据，例如聊天服务器， 实现NIO的服务器可能是⼀个优势。同样，如果你需要维持许多打开的连接到其他计算机上，如 P2P⽹络中，使⽤⼀个单独的线程来管理你所有出站连接，可能是⼀个优势。⼀个线程多个连接的 设计⽅案如

<table>
  <tr>
    <th>![image 15](assets/imageFile15.png)</th>
  </tr>
</table>


#### Java NIO: 单线程管理多个连接 如果你有少量的连接使⽤⾮常⾼的带宽，⼀次发送⼤量的数据，也许典型的IO服务器实现可能⾮常 契合。下图说明了⼀个典型的IO服务器设计：

<table>
  <tr>
    <th>![image 16](assets/imageFile16.png)</th>
  </tr>
</table>


Java IO: ⼀个典型的IO服务器设计- ⼀个连接通过⼀个线程处理

## Java中堆内存和栈内存区别

Java把内存分成两种，⼀种叫做栈内存，⼀种叫做堆内存 在函数中定义的⼀些基本类型的变量和对象的引⽤变量都是在函数的栈内存中分配。当在⼀段代码 块中定义⼀个变量时，java就在栈中为这个变量分配内存空间，当超过变量的作⽤域后，java会⾃ 动释放掉为该变量分配的内存空间，该内存空间可以⽴刻被另作他⽤。 堆内存⽤于存放由new创建的对象和数组。在堆中分配的内存，由java虚拟机⾃动垃圾回收器来管 理。在堆中产⽣了⼀个数组或者对象后，还可以在栈中定义⼀个特殊的变量，这个变量的取值等于 数组或者对象在堆内存中的⾸地址，在栈中的这个特殊的变量就变成了数组或者对象的引⽤变量， 以后就可以在程序中使⽤栈内存中的引⽤变量来访问堆中的数组或者对象，引⽤变量相当于为数组 或者对象起的⼀个别名，或者代号。 引⽤变量是普通变量，定义时在栈中分配内存，引⽤变量在程序运⾏到作⽤域外释放。⽽数组＆对 象本身在堆中分配，即使程序运⾏到使⽤new产⽣数组和对象的语句所在地代码块之外，数组和对 象本身占⽤的堆内存也不会被释放，数组和对象在没有引⽤变量指向它的时候，才变成垃圾，不能 再被使⽤，但是仍然占着内存，在随后的⼀个不确定的时间被垃圾回收器释放掉。这个也是java⽐ 较占内存的主要原因， *实际上，栈中的变量指向堆内存中的变量，这就是 Java 中的指针!

java中内存分配策略及堆和栈的⽐较 1 内存分配策略 按照编译原理的观点,程序运⾏时的内存分配有三种策略,分别是静态的,栈式的,和堆式的. 静态存储分配是指在编译时就能确定每个数据⽬标在运⾏时刻的存储空间需求,因⽽在编译时

就可以给他们分配固定的内存空间.这种分配策略要求程序代码中不允许有可变数据结构(⽐如可变 数组)的存在,也不允许有嵌套或者递归的结构出现,因为它们都会导致编译程序⽆法计算准确的存储 空间需求.

栈式存储分配也可称为动态存储分配,是由⼀个类似于堆栈的运⾏栈来实现的.和静态存储分配 相反,在栈式存储⽅案中,程序对数据区的需求在编译时是完全未知的,只有到运⾏的时候才能够知道, 但是规定在运⾏中进⼊⼀个程序模块时,必须知道该程序模块所需的数据区⼤⼩才能够为其分配内 存.和我们在数据结构所熟知的栈⼀样,栈式存储分配按照先进后出的原则进⾏分配。

静态存储分配要求在编译时能知道所有变量的存储要求,栈式存储分配要求在过程的⼊⼝处必 须知道所有的存储要求,⽽堆式存储分配则专⻔负责在编译时或运⾏时模块⼊⼝处都⽆法确定存储 要求的数据结构的内存分配,⽐如可变⻓度串和对象实例.堆由⼤⽚的可利⽤块或空闲块组成,堆中的 内存可以按照任意顺序分配和释放.

2 堆和栈的⽐较 上⾯的定义从编译原理的教材中总结⽽来,除静态存储分配之外,都显得很呆板和难以理解,下⾯

撇开静态存储分配,集中⽐较堆和栈:

从堆和栈的功能和作⽤来通俗的⽐较,堆主要⽤来存放对象的，栈主要是⽤来执⾏程序的.⽽这 种不同⼜主要是由于堆和栈的特点决定的:

在编程中，例如C/C+中，所有的⽅法调⽤都是通过栈来进⾏的,所有的局部变量,形式参数都 是从栈中分配内存空间的。实际上也不是什么分配,只是从栈顶向上⽤就⾏,就好像⼯⼚中的传送带 (conveyor belt)⼀样,Stack Pointer会⾃动指引你到放东⻄的位置,你所要做的只是把东⻄放下来就 ⾏.退出函数的时候，修改栈指针就可以把栈中的内容销毁.这样的模式速度最快, 当然要⽤来运⾏程 序了.需要注意的是,在分配的时候,⽐如为⼀个即将要调⽤的程序模块分配数据区时,应事先知道这个 数据区的⼤⼩,也就说是虽然分配是在程序运⾏时进⾏的,但是分配的⼤⼩多少是确定的,不变的,⽽这 个"⼤⼩多少"是在编译时确定的,不是在运⾏时.

堆是应⽤程序在运⾏的时候请求操作系统分配给⾃⼰内存，由于从操作系统管理的内存分配, 所以在分配和销毁时都要占⽤时间，因此⽤堆的效率⾮常低.但是堆的优点在于,编译器不必知道要 从堆⾥分配多少存储空间，也不必知道存储的数据要在堆⾥停留多⻓的时间,因此,⽤堆保存数据时 会得到更⼤的灵活性。事实上,⾯向对象的多态性,堆内存分配是必不可少的,因为多态变量所需的存 储空间只有在运⾏时创建了对象之后才能确定.在C+中，要求创建⼀个对象时，只需⽤ new命令 编制相关的代码即可。执⾏这些代码时，会在堆⾥⾃动进⾏数据的保存.当然，为达到这种灵活 性，必然会付出⼀定的代价:在堆⾥分配存储空间时会花掉更⻓的时间!这也正是导致我们刚才所说 的效率低的原因,看来列宁同志说的好,⼈的优点往往也是⼈的缺点,⼈的缺点往往也是⼈的优点(晕 ~).

3 JVM中的堆和栈 JVM是基于堆栈的虚拟机.JVM为每个新创建的线程都分配⼀个堆栈.也就是说,对于⼀个Java程

序来说，它的运⾏就是通过对堆栈的操作来完成的。堆栈以帧为单位保存线程的状态。JVM对堆栈 只进⾏两种操作:以帧为单位的压栈和出栈操作。

我们知道,某个线程正在执⾏的⽅法称为此线程的当前⽅法.我们可能不知道,当前⽅法使⽤的帧 称为当前帧。当线程激活⼀个Java⽅法,JVM就会在线程的 Java堆栈⾥新压⼊⼀个帧。这个帧⾃然 成为了当前帧.在此⽅法执⾏期间,这个帧将⽤来保存参数,局部变量,中间计算过程和其他数据.这个 帧在这⾥和编译原理中的活动纪录的概念是差不多的.

从Java的这种分配机制来看,堆栈⼜可以这样理解:堆栈(Stack)是操作系统在建⽴某个进程时或 者线程(在⽀持多线程的操作系统中是线程)为这个线程建⽴的存储区域，该区域具有先进后出的特 性。

每⼀个Java应⽤都唯⼀对应⼀个JVM实例，每⼀个实例唯⼀对应⼀个堆。应⽤程序在运⾏中所 创建的所有类实例或数组都放在这个堆中,并由应⽤所有的线程共享.跟C/C+不同，Java中分配堆 内存是⾃动初始化的。Java中所有对象的存储空间都是在堆中分配的，但是这个对象的引⽤却是在 堆栈中分配,也就是说在建⽴⼀个对象时从两个地⽅都分配内存，在堆中分配的内存实际建⽴这个 对象，⽽在堆栈中分配的内存只是⼀个指向这个堆对象的指针(引⽤)⽽已。

Java 中的堆和栈 Java把内存划分成两种：⼀种是栈内存，⼀种是堆内存。 在函数中定义的⼀些基本类型的变量和对象的引⽤变量都在函数的栈内存中分配。

当在⼀段代码块定义⼀个变量时，Java就在栈中为这个变量分配内存空间，当超过变量的作⽤

域后，Java会⾃动释放掉为该变量所分配的内存空间，该内存空间可以⽴即被另作他⽤。 堆内存⽤来存放由new创建的对象和数组。 在堆中分配的内存，由Java虚拟机的⾃动垃圾回收器来管理。 在堆中产⽣了⼀个数组或对象后，还可以在栈中定义⼀个特殊的变量，让栈中这个变量的取值

等于数组或对象在堆内存中的⾸地址，栈中的这个变量就成了数组或对象的引⽤变量。 引⽤变量就相当于是为数组或对象起的⼀个名称，以后就可以在程序中使⽤栈中的引⽤变量来

访问堆中的数组或对象。 具体的说： 栈与堆都是Java⽤来在Ram中存放数据的地⽅。与C+不同，Java⾃动管理栈和堆，程序员不

能直接地设置栈或堆。

Java的堆是⼀个运⾏时数据区,类的(对象从中分配空间。这些对象通过new、newaray、 anewaray和multianewaray等指令建⽴，它们不需要程序代码来显式的释放。堆是由垃圾回收来 负责的，堆的优势是可以动态地分配内存⼤⼩，⽣存期也不必事先告诉编译器，因为它是在运⾏时 动态分配内存的，Java的垃圾收集器会⾃动收⾛这些不再使⽤的数据。但缺点是，由于要在运⾏时 动态分配内存，存取速度较慢。

栈的优势是，存取速度⽐堆要快，仅次于寄存器，栈数据可以共享。但缺点是，存在栈中的数 据⼤⼩与⽣存期必须是确定的，缺乏灵活性。栈中主要存放⼀些基本类型的变量(,int, short, long, byte, float, double, bolean, char)和对象句柄。

栈有⼀个很重要的特殊性，就是存在栈中的数据可以共享。假设我们同时定义：

- int a = 3;
- int b = 3; 编译器先处理int a = 3;⾸先它会在栈中创建⼀个变量为a的引⽤，然后查找栈中是否有3这个


值，如果没找到，就将3存放进来，然后将a指向3。接着处理int b = 3;在创建完b的引⽤变量后， 因为在栈中已经有3这个值，便将b直接指向3。这样，就出现了a与b同时均指向3的情况。这时， 如果再令a=4;那么编译器会重新搜索栈中是否有4值，如果没有，则将4存放进来，并令a指向4;如 果已经有了，则直接将a指向这个地址。因此a值的改变不会影响到b的值。要注意这种数据的共享 与两个对象的引⽤同时指向⼀个对象的这种共享是不同的，因为这种情况a的修改并不会影响到b, 它是由编译器完成的，它有利于节省空间。⽽⼀个对象引⽤变量修改了这个对象的内部状态，会影 响到另⼀个对象引⽤变量

## 反射讲⼀讲，主要是概念,都在哪需要反射机制，反射的性能，如何 优化

反射机制的定义： 是在运⾏状态中，对于任意的⼀个类，都能够知道这个类的所有属性和⽅法，对任意⼀个对象都能 够通过反射机制调⽤⼀个类的任意⽅法，这种动态获取类信息及动态调⽤类对象⽅法的功能称为 java的反射机制。

反射的作⽤：

- 1、动态地创建类的实例，将类绑定到现有的对象中，或从现有的对象中获取类型。
- 2、应⽤程序需要在运⾏时从某个特定的程序集中载⼊⼀个特定的类


## 如何保证RESTfulAPI安全性

如何设计好的RESTful API之安全性

友情链接：

## 如何预防MySQL注⼊

所谓SQL注⼊，就是通过把SQL命令插⼊到Web表单递交或输⼊域名或⻚⾯请求的查询字符串，最 终达到欺骗服务器执⾏恶意的SQL命令。 我们永远不要信任⽤户的输⼊，我们必须认定⽤户输⼊的数据都是不安全的，我们都需要对⽤户输 ⼊的数据进⾏过滤处理。

- 1.以下实例中，输⼊的⽤户名必须为字⺟、数字及下划线的组合，且⽤户名⻓度为 8 到 20 个字符

之间：

if (preg_match("/^\w{8,20}$/", $_GET['username'], $matches)) { $result = mysql_query("SELECT * FROM users

WHERE username=$matches[0]");

} else { echo "username 输⼊异常"; }

让我们看下在没有过滤特殊字符时，出现的SQL情况：

/ 设定$name 中插⼊了我们不需要的SQL语句 $name = "Qadir'; DELETE FROM users;"; mysql_query("SELECT * FROM users WHERE name='{$name}'"); 以上的注⼊语句中，我们没有对 $name 的变量进⾏过滤，$name 中插⼊了我们不需要的SQL语 句，将删除 users 表中的所有数据。

- 2.在PHP中的 mysql_query() 是不允许执⾏多个SQL语句的，但是在 SQLite 和 PostgreSQL 是 可以同时执⾏多条SQL语句的，所以我们对这些⽤户的数据需要进⾏严格的验证。 防⽌SQL注⼊，我们需要注意以下⼏个要点：


- 1.永远不要信任⽤户的输⼊。对⽤户的输⼊进⾏校验，可以通过正则表达式，或限制⻓度；对单引 号和 双"-"进⾏转换等。
- 2.永远不要使⽤动态拼装sql，可以使⽤参数化的sql或者直接使⽤存储过程进⾏数据查询存取。
- 3.永远不要使⽤管理员权限的数据库连接，为每个应⽤使⽤单独的权限有限的数据库连接。
- 4.不要把机密信息直接存放，加密或者hash掉密码和敏感的信息。
- 5.应⽤的异常信息应该给出尽可能少的提示，最好使⽤⾃定义的错误信息对原始错误信息进⾏包装


- 6.sql注⼊的检测⽅法⼀般采取辅助软件或⽹站平台来检测，软件⼀般采⽤sql注⼊检测⼯具jsky， ⽹站平台就有亿思⽹站安全平台检测⼯具。MDCSOFT SCAN等。采⽤MDCSOFT-IPS可以有效的 防御SQL注⼊，XS攻击等。


- 3.防⽌SQL注⼊ 在脚本语⾔，如Perl和PHP你可以对⽤户输⼊的数据进⾏转义从⽽来防⽌SQL注⼊。

PHP的MySQL扩展提供了mysql_real_escape_string()函数来转义特殊的输⼊字符。

if (get_magic_quotes_gpc()) { $name = stripslashes($name); } $name = mysql_real_escape_string($name); mysql_query("SELECT * FROM users WHERE name='{$name}'");

- 4.Like语句中的注⼊ like查询时，如果⽤户输⼊的值有""和 "%"， 则 会 出 现 这 种 情 况 ： ⽤ 户 本来 只 是 想 查 询 "abcd"，查 询结果中却有"abcd_"、"abcde"、"abcdf"等等；⽤户要查询"30%"（注：百分之三⼗）时也会出 现问题。


在PHP脚本中我们可以使⽤adcslashes()函数来处理以上情况，如下实例：

$sub = addcslashes(mysql_real_escape_string("%something_"), "%_"); // $sub == \%something\_ mysql_query("SELECT * FROM messages WHERE subject LIKE '{$sub}%'");

adcslashes()函数在指定的字符前添加反斜杠。 语法格式: adcslashes(string,characters) 参数 描述 string 必需。规定要检查的字符串。 characters 可选。规定受 adcslashes() 影响的字符或字符范围。

## ThreadLocal(线程变量副本)

Synchronized实现内存共享，ThreadLocal为每个线程维护⼀个本地变量。 采⽤空间换时间，它⽤于线程间的数据隔离，为每⼀个使⽤该变量的线程提供⼀个副本，每个线程 都可以独⽴地改变⾃⼰的副本，⽽不会和其他线程的副本冲突。 ThreadLocal类中维护⼀个Map，⽤于存储每⼀个线程的变量副本，Map中元素的键为线程对象， ⽽值为对应线程的变量副本。 ThreadLocal在 中发挥着巨⼤的作⽤，在管理Request作⽤域中的Bean、事务管理、任务调 度、AOP等模块都出现了它的身影。 Spring中绝⼤部分Bean都可以声明成Singleton作⽤域，采⽤ThreadLocal进⾏封装，因此有状态的 Bean就能够以singleton的⽅式在多线程中正常⼯作了。

spring

## 你能不能谈谈， GC是在什么时候，对什么东⻄，做了什么事 情？

## Java

在什么时候：

- 1.新⽣代有⼀个Eden区和两个survivor区，⾸先将对象放⼊Eden区，如果空间不⾜就向其中的⼀个 survivor区上放，如果仍然放不下就会引发⼀次发⽣在新⽣代的minor GC，将存活的对象放⼊另⼀ 个survivor区中，然后清空Eden和之前的那个survivor区的内存。在某次GC过程中，如果发现仍然 ⼜放不下的对象，就将这些对象放⼊⽼年代内存⾥去。
- 2.⼤对象以及⻓期存活的对象直接进⼊⽼年区。
- 3.当每次执⾏minor GC的时候应该对要晋升到⽼年代的对象进⾏分析，如果这些⻢上要到⽼年区的 ⽼年对象的⼤⼩超过了⽼年区的剩余⼤⼩，那么执⾏⼀次Ful GC以尽可能地获得⽼年区的空间。 对什么东⻄：从GC Rots搜索不到，⽽且经过⼀次标记清理之后仍没有复活的对象。 做什么： 新⽣代：复制清理； ⽼年代：标记-清除和标记-压缩 ； 永久代：存放Java中的类和加载类的类加载器本身。 GC Rots都有哪些：


算法

- \1. 虚拟机栈中的引⽤的对象
- \2. ⽅法区中静态属性引⽤的对象，常量引⽤的对象
- \3. 本地⽅法栈中JNI（即⼀般说的Native⽅法）引⽤的对象。


## Volatile和Synchronized四个不同点：

- 1 粒度不同，前者锁对象和类，后者针对变量
- 2 syn阻塞，volatile线程不阻塞
- 3 syn保证三⼤特性，volatile不保证原⼦性
- 4 syn编译器优化，volatile不优化 volatile具备两种特性：


- \1. 保证此变量对所有线程的可⻅性，指⼀条线程修改了这个变量的值，新值对于其他线程来说是 可⻅的，但并不是多线程安全的。
- \2. 禁⽌指令重排序优化。 Volatile如何保证内存可⻅性:


- 1.当写⼀个volatile变量时，J M会把该线程对应的本地内存中的共享变量刷新到主内存。
- 2.当读⼀个volatile变量时，J M会把该线程对应的本地内存置为⽆效。线程接下来将从主内存中 读取共享变量。 同步：就是⼀个任务的完成需要依赖另外⼀个任务，只有等待被依赖的任务完成后，依赖任务才能 完成。


异步：不需要等待被依赖的任务完成，只是通知被依赖的任务要完成什么⼯作，只要⾃⼰任务完成 了就算完成了，被依赖的任务是否完成会通知回来。（异步的特点就是通知）。 打电话和发短信来⽐喻同步和异步操作。 阻塞：CPU停下来等⼀个慢的操作完成以后，才会接着完成其他的⼯作。 ⾮阻塞：⾮阻塞就是在这个慢的执⾏时，CPU去做其他⼯作，等这个慢的完成后，CPU才会接着完 成后续的操作。 ⾮阻塞会造成线程切换增加，增加CPU的使⽤时间能不能补偿系统的切换成本需要考虑。

## 线程池的作⽤：

在程序启动的时候就创建若⼲线程来响应处理，它们被称为线程池，⾥⾯的线程叫⼯作线程 第⼀：降低资源消耗。通过重复利⽤已创建的线程降低线程创建和销毁造成的消耗。 第⼆：提⾼响应速度。当任务到达时，任务可以不需要等到线程创建就能⽴即执⾏。 第三：提⾼线程的可管理性。 常⽤线程池：ExecutorService 是主要的实现类，其中常⽤的有 Executors.newSingleThreadPol(),newFixedThreadPol(),newcachedTheadPol(),newSchedule dThreadPol()。

⼀致性哈希：

数据结构:String—字符串（key-value类型） 索引：B+，B-,全⽂索引

## Redis

MySQL

的索引是⼀个数据结构，旨在使数据库⾼效的查找数据。 常⽤的数据结构是B+Tre，每个叶⼦节点不但存放了索引键的相关信息还增加了指向相邻叶⼦节 点的指针，这样就形成了带有顺序访问指针的B+Tre，做这个优化的⽬的是提⾼不同区间访问的 性能。 什么时候使⽤索引：

- 1.
- 2.
- 3.
- 4.


经常出现在group by,order by和distinc关键字后⾯的字段 经常与其他表进⾏连接的表，在连接字段上应该建⽴索引 经常出现在Where⼦句中的字段 经常出现⽤作查询选择的字段

## SpringIOCAOP（控制反转，依赖注⼊）

IOC容器：就是具有依赖注⼊功能的容器，是可以创建对象的容器，IOC容器负责实例化、定位、 配置应⽤程序中的对象及建⽴这些对象间的依赖。通常new⼀个实例，控制权由程序员控制， ⽽"控制反转"是指new实例⼯作不由程序员来做⽽是交给Spring容器来做。。在Spring中 BeanFactory是IOC容器的实际代表者。 DI(依赖注⼊Dependency injection) ：在容器创建对象后，处理对象的依赖关系。 Spring⽀持三种依赖注⼊⽅式，分别是属性（Seter⽅法）注⼊，构造注⼊和接⼝注⼊。

在Spring中，那些组成应⽤的主体及由Spring IOC容器所管理的对象被称之为Bean。 Spring的IOC容器通过反射的机制实例化Bean并建⽴Bean之间的依赖关系。 简单地讲，Bean就是由Spring IOC容器初始化、装配及被管理的对象。 获取Bean对象的过程，⾸先通过Resource加载配置⽂件并启动IOC容器，然后通过getBean⽅法获 取bean对象，就可以调⽤他的⽅法。 Spring Bean的作⽤域： Singleton：Spring IOC容器中只有⼀个共享的Bean实例，⼀般都是Singleton作⽤域。 Prototype：每⼀个请求，会产⽣⼀个新的Bean实例。 Request：每⼀次htp请求会产⽣⼀个新的Bean实例。 AOP就是纵向的编程，如业务1和业务2都需要⼀个共同的操作，与其往每个业务中都添加同样的 代码，不如写⼀遍代码，让两个业务共同使⽤这段代码。在⽇常有订单管理、商品管理、资⾦管 理、库存管理等业务，都会需要到类似⽇志记录、事务控制、 *权限控制、性能统计、异常处理 及事务处理等。AOP把所有共有代码全部抽取出来，放置到某个地⽅集中管理，然后在具体运⾏ 时，再由容器动态织⼊这些共有代码。 Spring AOP应⽤场景 性能检测，访问控制，⽇志管理，事务等。 默认的策略是如果⽬标类实现接⼝，则使⽤JDK动态代理技术，如果⽬标对象没有实现接⼝，则默 认会采⽤CGLIB代理 友情链接： 友情链接： 友情链接：

Spring框架IOC容器和AOP解析 浅谈Spring框架注解的⽤法分析 关于Spring的69个⾯试问答⸺终极列表

## 代理的共有优点：业务类只需要关注业务逻辑本身，保证了业务类 的重⽤性。

Java静态代理： 代理对象和⽬标对象实现了相同的接⼝，⽬标对象作为代理对象的⼀个属性，具体接⼝实现中，代 理对象可以在调⽤⽬标对象相应⽅法前后加上其他业务处理逻辑。 缺点：⼀个代理类只能代理⼀个业务类。如果业务类增加⽅法时，相应的代理类也要增加⽅法。 Java动态代理： Java动态代理是写⼀个类实现InvocationHandler接⼝，重写Invoke⽅法，在Invoke⽅法可以进⾏增 强处理的逻辑的编写，这个公共代理类在运⾏的时候才能明确⾃⼰要代理的对象，同时可以实现该 被代理类的⽅法的实现，然后在实现类⽅法的时候可以进⾏增强处理。 实际上：代理对象的⽅法 = 增强处理 + 被代理对象的⽅法 JDK和CGLIB⽣成动态代理类的区别： JDK动态代理只能针对实现了接⼝的类⽣成代理（实例化⼀个类）。此时代理对象和⽬标对象实现 了相同的接⼝，⽬标对象作为代理对象的⼀个属性，具体接⼝实现中，可以在调⽤⽬标对象相应⽅ 法前后加上其他业务处理逻辑

CGLIB是针对类实现代理，主要是对指定的类⽣成⼀个⼦类（没有实例化⼀个类），覆盖其中的⽅ 法 。

## SpringMVC运⾏原理

- \1. 客户端请求提交到DispatcherServlet
- \2. 由DispatcherServlet控制器查询HandlerMaping，找到并分发到指定的Controler中。 \4. Controler调⽤业务逻辑处理后，返回ModelAndView \5. DispatcherServlet查询⼀个或多个ViewResoler视图解析器，找到ModelAndView指定的视图 \6. 视图负责将结果显示到客户端 友情链接： 友情链接： 友情链接： 友情链接：


Spring：基于注解的Spring MVC（上）

Spring：基于注解的Spring MVC（下） SpringMVC与Struts2区别与⽐较总结 SpringMVC与Struts2的对⽐

## TCP三次握⼿，四次挥⼿

TCP作为⼀种可靠传输控制协议，其核⼼思想：既要保证数据可靠传输，⼜要提⾼传输的效率，⽽⽤ 三次恰恰可以满⾜以上两⽅⾯的需求！ *双⽅都需要确认⾃⼰的发信和收信功能正常，收信功能通过 接收对⽅信息得到确认，发信功能需要发出信息—>对⽅回复信息得到确认。

三次握⼿过程：

- 1.
- 2.
- 3.


第⼀次握⼿：建⽴连接。客户端发送连接请求报⽂段，将SYN位置为1，Sequence Number为x； 然后，客户端进⼊SYN_SEND状态，等待服务器的确认； 第⼆次握⼿：服务器收到客户端的SYN报⽂段，需要对这个SYN报⽂段进⾏确认，设置ACK为

- x+1(Sequence Number+1)；同时，⾃⼰还要发送SYN请求信息，将SYN位置为1，Sequence Number为y；服务器端将上述所有信息放到⼀个报⽂段（即SYN+ACK报⽂段）中，⼀并发送给客 户端，此时服务器进⼊SYN_RECV状态； 第三次握⼿：客户端收到服务器的SYN+ACK报⽂段。然后将Acknowledgment Number设置为
- y+1，向服务器发送ACK报⽂段，这个报⽂段发送完毕以后，客户端和服务器端都进⼊ ESTABLISHED状态，完成TCP三次握⼿。


TCP⼯作在⽹络OSI的七层模型中的第四层⸺Transport层，IP在第三层⸺Network层

ARP在第⼆层⸺Data Link层；在第⼆层上的数据，我们把它叫Frame，在第三层上的数据叫 Packet，第四层的数据叫Segment。

四次挥⼿过程：

1.

第⼀次分⼿：主机1（可以使客户端，也可以是服务器端），设置Sequence Number和 Acknowledgment Number，向主机2发送⼀个FIN报⽂段；此时，主机1进⼊FIN_WAIT_1状态；这 表示主机1没有数据要发送给主机2了；

- 2.
- 3. 4.


第⼆次分⼿：主机2收到了主机1发送的FIN报⽂段，向主机1回⼀个ACK报⽂段， Acknowledgment Number为Sequence Number加1；主机1进⼊FIN_WAIT_2状态；主机2告诉主机 1，我“同意”你的关闭请求； 第三次分⼿：主机2向主机1发送FIN报⽂段，请求关闭连接，同时主机2进⼊LAST_ACK状态； 第四次分⼿：主机1收到主机2发送的FIN报⽂段，向主机2发送ACK报⽂段，然后主机1进⼊ TIME_WAIT状态；主机2收到主机1的ACK报⽂段以后，就关闭连接；此时，主机1等待2MSL后依 然没有收到回复，则证明Server端已正常关闭，那好，主机1也可以关闭连接了。

（2）⽽关闭连接却是四次挥⼿呢？ 这是因为服务端在LISTEN状态下，收到建⽴连接请求的SYN报⽂后，把ACK和SYN放在⼀个报⽂⾥发送给客户端。

为什么建⽴连接是三次握⼿

这是因为服务端在LISTEN状态下，收到建⽴连接请求的SYN报⽂后，把ACK和SYN放在⼀个报⽂⾥发 送给客户端。

关闭连接却是四次挥⼿呢

⽽关闭连接时，当收到对⽅的FIN报⽂时，仅仅表示对⽅不再发送数据了但是还能接收数据，⼰⽅也未 必全部数据都发送给对⽅了，所以⼰⽅可以⽴即close，也可以发送⼀些数据给对⽅后，再发送FIN报 ⽂给对⽅来表示同意现在关闭连接，因此，⼰⽅ACK和FIN⼀般都会分开发送。

## HTPS和HTP为什么更安全，先看这些

htp默认端⼝是80 htps是 43 htp是HTP协议运⾏在TCP之上。所有传输的内容都是明⽂，客户端和服务器端都⽆法验证对⽅的身 份。 htps是HTP运⾏在 SL/TLS之上， SL/TLS运⾏在TCP之上。所有传输的内容都经过加密，加密采⽤ 对称加密，但对称加密的密钥⽤服务器⽅的证书进⾏了⾮对称加密。此外客户端可以验证服务器端的 身份，如果配置了客户端验证，服务器⽅也可以验证客户端的身份。HTP(应⽤层) 和TCP(传输层)之 间插⼊⼀个 SL协议,

## ⼀个Htp请求

DNS域名解析 –> 发起TCP的三次握⼿ –> 建⽴TCP连接后发起htp请求 –> 服务器响应htp请求， 浏览器得到html代码 –> 浏览器解析html代码，并请求html代码中的资源（如js、cs、图⽚等） – > 浏览器对⻚⾯进⾏渲染呈现给⽤户 友情链接： 友情链接： 友情链接： 友情链接：

HTP与HTPS的区别 HTPS 为什么更安全，先看这些 HTP请求报⽂和HTP响应报⽂ HTP 请求⽅式: GET和POST的⽐较

Mybatis

每⼀个Mybatis的应⽤程序都以⼀个SqlSesionFactory对象的实例为核⼼。⾸先⽤字节流通过 Resource将配置⽂件读⼊，然后通过SqlSesionFactoryBuilder().build⽅法创建 SqlSesionFactory，然后再通过sqlSesionFactory.openSesion()⽅法创建⼀个sqlSesion为每 ⼀个数据库事务服务。 经历了Mybatis初始化 –>创建SqlSesion –>运⾏SQL语句 返回结果三个过程

## Servlet和Filter的区别：

整的流程是：Filter对⽤户请求进⾏预处理，接着将请求交给Servlet进⾏处理并⽣成响应，最后 Filter再对服务器响应进⾏后处理。 Filter有如下⼏个⽤处： Filter可以进⾏对特定的url请求和相应做预处理和后处理。 在HtpServletRequest到达Servlet之前，拦截客户的HtpServletRequest。 根据需要检查HtpServletRequest，也可以修改HtpServletRequest头和数据。 在HtpServletResponse到达客户端之前，拦截HtpServletResponse。 根据需要检查HtpServletResponse，也可以修改HtpServletResponse头和数据。 实际上Filter和Servlet极其相似，区别只是Filter不能直接对⽤户⽣成响应。实际上Filter⾥ doFilter()⽅法⾥的代码就是从多个Servlet的service()⽅法⾥抽取的通⽤代码，通过使⽤Filter可以 实现更好的复⽤。 Filter和Servlet的⽣命周期：

- 1.Filter在web服务器启动时初始化
- 2.如果某个Servlet配置了 1 ，该Servlet也是在Tomcat（Servlet容器）启动时初始化。
- 3.如果Servlet没有配置1 ，该Servlet不会在Tomcat启动时初始化，⽽是在请求到来时初始化。
- 4.每次请求， Request都会被初始化，响应请求后，请求被销毁。
- 5.Servlet初始化后，将不会随着请求的结束⽽注销。
- 6.关闭Tomcat时，Servlet、Filter依次被注销。


## HashMap和TreMap区别

HashMap：基于哈希表实现。使⽤HashMap要求添加的键类明确定义了hashCode()和equals() [可以重写hashCode()和equals()]，为了优化HashMap空间的使⽤，您可以调优初始容量和负载因 ⼦。 适合查找和删除

- (1)HashMap(): 构建⼀个空的哈希映像
- (2)HashMap(Map m): 构建⼀个哈希映像，并且添加映像m的所有映射
- (3)HashMap(int initialCapacity): 构建⼀个拥有特定容量的空的哈希映像
- (4)HashMap(int initialCapacity, float loadFactor): 构建⼀个拥有特定容量和加载因⼦的空的哈希 映像 TreMap：基于红⿊树实现。TreMap没有调优选项，因为该树总处于平衡状态。 适合按照⾃然 顺序或者⾃定义的顺序排序遍历key


- (1)TreMap():构建⼀个空的映像树


- (2)TreMap(Map m): 构建⼀个映像树，并且添加映像m中所有元素
- (3)TreMap(Comparator c): 构建⼀个映像树，并且使⽤特定的⽐较器对关键字进⾏排序
- (4)TreMap(SortedMap s): 构建⼀个映像树，添加映像树s中所有映射，并且使⽤与有序映像s相 同的⽐较器排序 友情链接： HashMap冲突 友情链接： 友情链接： 友情链接： 友情链接：


Java中HashMap和TreMap的区别深⼊理解

HashMap冲突的解决⽅法以及原理分析 HashMap的⼯作原理 HashMap和Hashtable的区别 2种办法让HashMap线程安全

## HashMap，ConcurentHashMap与LinkedHashMap的区别

- 1.
- 2.
- 3.
- 4.


ConcurentHashMap是使⽤了锁分段技术技术来保证线程安全的，锁分段技术：⾸先将数据分成 ⼀段⼀段的存储，然后给每⼀段数据配⼀把锁，当⼀个线程占⽤锁访问其中⼀个段数据的时候， 其他段的数据也能被其他线程访问 ConcurentHashMap 是在每个段（segment）中线程安全的 LinkedHashMap维护⼀个双链表，可以将⾥⾯的数据按写⼊的顺序读出 ConcurentHashMap应⽤场景

- 1：ConcurentHashMap的应⽤场景是⾼并发，但是并不能保证线程安全，⽽同步的HashMap和 HashMap的是锁住整个容器，⽽加锁之后ConcurentHashMap不需要锁住整个容器，只需要锁住 对应的Segment就好了，所以可以保证⾼并发同步访问，提升了效率。
- 2：可以多线程写。 ConcurentHashMap把HashMap分成若⼲个Segmenet


- 1.get时，不加锁，先定位到segment然后在找到头结点进⾏读取操作。⽽value是volatile变量，所 以可以保证在竞争条件时保证读取最新的值，如果读到的value是nul，则可能正在修改，那么久调 ⽤ReadValueUnderLock函数，加锁保证读到的数据是正确的。
- 2.Put时会加锁，⼀律添加到hash链的头部。
- 3.Remove时也会加锁，由于next是final类型不可改变，所以必须把删除的节点之前的节点都复制 ⼀遍。
- 4.ConcurentHashMap允许多个修改操作并发进⾏，其关键在于使⽤了锁分离技术。它使⽤了多 个锁来控制对Hash表的不同Segment进⾏的修改。 ConcurentHashMap的应⽤场景是⾼并发，但是并不能保证线程安全，⽽同步的HashMap和 HashTable的是锁住整个容器，⽽加锁之后ConcurentHashMap不需要锁住整个容器，只需要锁住 对应的segment就好了，所以可以保证⾼并发同步访问，提升了效率。 友情链接：


Java集合—ConcurentHashMap原理分析

ThreadPolExecutor的内部⼯作原理

## 进程间的通信⽅式

- 1.
- 2.
- 3.


管道( pipe )：管道是⼀种半双⼯的通信⽅式，数据只能单向流动，⽽且只能在具有亲缘关系 的进程间使⽤。进程的亲缘关系通常是指⽗⼦进程关系。 有名管道 (named pipe) ： 有名管道也是半双⼯的通信⽅式，但是它允许⽆亲缘关系进程间的 通信。

3.信号量( semophore ) ： 信号量是⼀个计数器，可以⽤来控制多个进程对共享资源的访问。它常 作为⼀种锁机制，防⽌某进程正在访问共享资源时，其他进程也访问该资源。因此，主要作为进程 间以及同⼀进程内不同线程之间的同步⼿段。

消息队列( mesage queue ) ： 消息队列是由消息的链表，存放在内核中并由消息队列标识符 标识。消息队列克服了信号传递信息少、管道只能承载⽆格式字节流以及缓冲区⼤⼩受限等缺 点。

- 5.信号 ( sinal ) ： 信号是⼀种⽐较复杂的通信⽅式，⽤于通知接收进程某个事件已经发⽣。
- 6.共享内存( shared memory ) ：共享内存就是映射⼀段能被其他进程所访问的内存，这段共享内 存由⼀个进程创建，但多个进程都可以访问。共享内存是最快的 IPC ⽅式，它是针对其他进程间通 信⽅式运⾏效率低⽽专⻔设计的。它往往与其他通信机制，如信号量，配合使⽤，来实现进程间的 同步和通信。
- 7.套接字( socket ) ： 套解⼝也是⼀种进程间通信机制，与其他通信机制不同的是，它可⽤于不同 机器间的进程通信。


## 死锁的必要条件

- 1.
- 2.
- 3.
- 4.


互斥 ⾄少有⼀个资源处于⾮共享状态 占有并等待 ⾮抢占 循环等待

解决死锁，第⼀个是死锁预防，就是不让上⾯的四个条件同时成⽴。⼆是，合理分配资源。 三是使⽤银⾏家算法，如果该进程请求的资源 剩余量可以满⾜，那么就分配。

操作系统

作者：时芥蓝 链接：htps:/ w.jianshu.com/p/1acdfac2b4e4 來源：简书 简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
