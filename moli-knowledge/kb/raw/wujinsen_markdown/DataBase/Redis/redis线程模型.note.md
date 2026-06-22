基于 开发了⾃⼰的⽹络事件处理器： 这个处理器被称为⽂件事件处理器（file event handler）：

Redis Reactor 模式

⽂件事件处理器使⽤ 程序来同时监听多个套接字， 并根据套接字⽬前 执⾏的任务来为套接字关联不同的事件处理器。

I/O 多路复⽤（multiplexing）

当被监听的套接字准备好执⾏连接应答（acept）、读取（read）、写⼊（write）、关闭 （close）等操作时， 与操作相对应的⽂件事件就会产⽣， 这时⽂件事件处理器就会调⽤套接字之 前关联好的事件处理器来处理这些事件。

虽然⽂件事件处理器以单线程⽅式运⾏， 但通过使⽤ I/O 多路复⽤程序来监听多个套接字， ⽂件事件 处理器既实现了⾼性能的⽹络通信模型， ⼜可以很好地与 服务器中其他同样以单线程⽅式运⾏ 的模块进⾏对接， 这保持了 Redis 内部单线程设计的简单性。

redis

# ⽂件事件处理器的构成

图 IMAGE_CONSTRUCT_OF_FILE_EVENT_HANDLER 展示了⽂件事件处理器的四个组成部分， 它们 分别是套接字、 I/O 多路复⽤程序、 ⽂件事件分派器（dispatcher）、 以及事件处理器。

![image 1](<redis线程模型.note_images/imageFile1.png>)

digraph{ label ="\n图 IMAGE_CONSTRUCT_OF_FILE_EVENT_HANDLER ⽂件事件处理器的四个组成部分"; rankdir =LR; node [shape = box]; subgraph cluster_sockets { style = dashed label = "套接字"; c1 [label = "s1", shape = circle]; c2[label ="s2", shape=circle]; other_client [label =".", width=1.1, shape=plaintext]; c3[label ="sN", shape= circle]; } io_multiplexing [label = "I\n/\nO\n多\n路\n复\n⽤\n程\n序"]; file_event_procesor [label = "⽂\n件\n事\n件\n分\n派 \n器"]; subgraph cluster_handlers { style = dashed label = "事件处理器"; write_handler [label = "命令请求处理 器"]; read_handler [label = "命令回复处理器"]; conect_handler [label = "连接应答处理器"]; other_handlers [label = ".", width=1.6]; } c1->io_multiplexing; c2->io_multiplexing; other_client ->io_multiplexing[style=invis]; c3-> io_multiplexing; io_multiplexing -> file_event_procesor; file_event_procesor -> write_handler; file_event_procesor -> read_handler; file_event_procesor -> conect_handler; file_event_procesor -> other_handlers;}

⽂件事件是对套接字操作的抽象， 每当⼀个套接字准备好执⾏连接应答（acept）、写⼊、读取、关 闭等操作时， 就会产⽣⼀个⽂件事件。 因为⼀个服务器通常会连接多个套接字， 所以多个⽂件事件有 可能会并发地出现。 I/O 多路复⽤程序负责监听多个套接字， 并向⽂件事件分派器传送那些产⽣了事件的套接字。

尽管多个⽂件事件可能会并发地出现， 但 I/O 多路复⽤程序总是会将所有产⽣事件的套接字都⼊队到 ⼀个队列⾥⾯， 然后通过这个队列， 以有序（sequentialy）、同步（synchronously）、每次⼀个套 接字的⽅式向⽂件事件分派器传送套接字： 当上⼀个套接字产⽣的事件被处理完毕之后（该套接字为 事件所关联的事件处理器执⾏完毕）， I/O 多路复⽤程序才会继续向⽂件事件分派器传送下⼀个套接 字， 如图 IMAGE_DISPATCH_EVENT_VIA_QUEUE 。

![image 2](<redis线程模型.note_images/imageFile2.png>)

digraph { rankdir = LR; node [shape = record]; label = "\n图 IMAGE_DISPATCH_EVENT_VIA_QUEUE I/O 多路复⽤程序通过 队列向⽂件事件分派器传送套接字"; / subgraph cluster_io_multiplexing { /style = dashed label = "队列"; queue [label = " { 套接字 sN | 套接字 sN-1 | . | 套接字 s3 | 套接字 s2 } "]; } file_event_procesor [label = "⽂\n件\n事\n件\n分\n派\n 器"]; / queue -> file_event_procesor [label = "传送\n 套接字 s1", style = dashed];}

⽂件事件分派器接收 I/O 多路复⽤程序传来的套接字， 并根据套接字产⽣的事件的类型， 调⽤相应的 事件处理器。 服务器会为执⾏不同任务的套接字关联不同的事件处理器， 这些处理器是⼀个个函数， 它们定义了某 个事件发⽣时， 服务器应该执⾏的动作。

# I/O多路复⽤程序的实现

Redis 的 I/O 多路复⽤程序的所有功能都是通过包装常⻅的 select 、 epoll 、 evport 和 kqueue 这些 I/O 多路复⽤函数库来实现的， 每个 I/O 多路复⽤函数库在 Redis 源码中都对应⼀个单独的⽂件， ⽐ 如 ae_select.c 、 ae_epoll.c 、 ae_kqueue.c ， 诸如此类。 因为 Redis 为每个 I/O 多路复⽤函数库都实现了相同的 API ， 所以 I/O 多路复⽤程序的底层实现是可 以互换的， 如图 IMAGE_MULTI_LIB 所示。

![image 3](<redis线程模型.note_images/imageFile3.png>)

digraph { label = "图 IMAGE_MULTI_LIB Redis 的 I/O 多路复⽤程序有多个 I/O 多路复⽤库实现可选"; node [shape = box]; io_multiplexing [label = "I/O 多路复⽤程序"]; subgraph cluster_imp { style = dashed label = "底层实现"; labeloc = "b"; kqueue [label = "kqueue"]; evport [label = "evport"]; epol [label = "epol"]; select [label = "select"]; }

/ edge [dir = back]; io_multiplexing -> select; io_multiplexing -> epol; io_multiplexing -> evport; io_multiplexing -> kqueue;}

Redis 在 I/O 多路复⽤程序的实现源码中⽤ #include 宏定义了相应的规则， 程序会在编译时⾃动选择 系统中性能最⾼的 I/O 多路复⽤函数库来作为 Redis 的 I/O 多路复⽤程序的底层实现：

/* Include the best multiplexing layer suported by this system.

* The folowing should be ordered by performances, descending. */ #ifdef HAVE_EVPORT #include "ae_evport.c" #else

#ifdef HAVE_EPOL #include "ae_epol.c" #else

#ifdef HAVE_KQUEUE #include "ae_kqueue.c" #else #include "ae_select.c" #endif

#endif #endif

# 事件的类型

I/O 多路复⽤程序可以监听多个套接字的 ae.h/AE_READABLE 事件和 ae.h/AE_WRITABLE 事件， 这两类事 件和套接字操作之间的对应关系如下：

当套接字变得可读时（客户端对套接字执⾏ write 操作，或者执⾏ close 操作）， 或者有新的可应 答（aceptable）套接字出现时（客户端对服务器的监听套接字执⾏ connect 操作）， 套接字产 ⽣ AE_READABLE 事件。 当套接字变得可写时（客户端对套接字执⾏ read 操作）， 套接字产⽣ AE_WRITABLE 事件。

I/O 多路复⽤程序允许服务器同时监听套接字的 AE_READABLE 事件和 AE_WRITABLE 事件， 如果⼀个套接 字同时产⽣了这两种事件， 那么⽂件事件分派器会优先处理 AE_READABLE 事件， 等到 AE_READABLE 事 件处理完之后， 才处理 AE_WRITABLE 事件。 这也就是说， 如果⼀个套接字⼜可读⼜可写的话， 那么服务器将先读套接字， 后写套接字。

ae.c/aeCreateFileEventAPI 函数接受⼀个套接字描述符、 ⼀个事件类型、 以及⼀个事件处理器作为参 数， 将给定套接字的给定事件加⼊到 I/O 多路复⽤程序的监听范围之内， 并对事件和事件处理器进⾏ 关联。 ae.c/aeDeleteFileEvent 函数接受⼀个套接字描述符和⼀个监听事件类型作为参数， 让 I/O 多路复⽤ 程序取消对给定套接字的给定事件的监听， 并取消事件和事件处理器之间的关联。 ae.c/aeGetFileEvents 函数接受⼀个套接字描述符， 返回该套接字正在被监听的事件类型：

如果套接字没有任何事件被监听， 那么函数返回 AE_NONE 。

如果套接字的读事件正在被监听， 那么函数返回 AE_READABLE 。 如果套接字的写事件正在被监听， 那么函数返回 AE_WRITABLE 。 如果套接字的读事件和写事件正在被监听， 那么函数返回 AE_READABLE | AE_WRITABLE 。

ae.c/aeWait 函数接受⼀个套接字描述符、⼀个事件类型和⼀个毫秒数为参数， 在给定的时间内阻塞 并等待套接字的给定类型事件产⽣， 当事件成功产⽣， 或者等待超时之后， 函数返回。

ae.c/aeApiPoll 函数接受⼀个 sys/time.h/struct timeval 结构为参数， 并在指定的时间內， 阻塞并 等待所有被 aeCreateFileEvent 函数设置为监听状态的套接字产⽣⽂件事件， 当有⾄少⼀个事件产 ⽣， 或者等待超时后， 函数返回。

ae.c/aeProcessEvents 函数是⽂件事件分派器， 它先调⽤ aeApiPoll 函数来等待事件产⽣， 然后遍历 所有已产⽣的事件， 并调⽤相应的事件处理器来处理这些事件。 ae.c/aeGetApiName 函数返回 I/O多路复⽤程序底层所使⽤的 I/O多路复⽤函数库的名称： 返 回 "epoll" 表示底层为 epoll 函数库， 返回"select" 表示底层为 select 函数库， 诸如此类。

# ⽂件事件的处理器

Redis 为⽂件事件编写了多个处理器， 这些事件处理器分别⽤于实现不同的⽹络通讯需求， ⽐如说：

为了对连接服务器的各个客户端进⾏应答， 服务器要为监听套接字关联连接应答处理器。 为了接收客户端传来的命令请求， 服务器要为客户端套接字关联命令请求处理器。 为了向客户端返回命令的执⾏结果， 服务器要为客户端套接字关联命令回复处理器。 当主服务器和从服务器进⾏复制操作时， 主从服务器都需要关联特别为复制功能编写的复制处理 器。 等等。

在这些事件处理器⾥⾯， 服务器最常⽤的要数与客户端进⾏通信的连接应答处理器、 命令请求处理器 和命令回复处理器。

连接应答处理器

networking.c/acceptTcpHandler 函数是 Redis 的连接应答处理器， 这个处理器⽤于对连接服务器监听 套接字的客户端进⾏应答， 具体实现为sys/socket.h/accept 函数的包装。 当 Redis服务器进⾏初始化的时候， 程序会将这个连接应答处理器和服务器监听套接字 的 AE_READABLE 事件关联起来， 当有客户端⽤sys/socket.h/connect 函数连接服务器监听套接字的时 候， 套接字就会产⽣ AE_READABLE 事件， 引发连接应答处理器执⾏， 并执⾏相应的套接字应答操作， 如图 IMAGE_SERVER_ACEPT_CONECT 所示。

![image 4](<redis线程模型.note_images/imageFile4.png>)

digraph{ label ="\n图 IMAGE_SERVER_ACEPT_CONECT 服务器对客户端的连接请求进⾏应答"; rankdir =LR; client [label = "客户端", shape = circle]; server [label = "服务器\n\n\n服务器监听套接字产⽣\nAE_READABLE 事件\n执⾏连接应答处理 器", shape = box, height = 2]; client -> server [label = "连接监听套接字"];}

## 命令请求处理器

networking.c/readQueryFromClient 函数是 Redis 的命令请求处理器， 这个处理器负责从套接字中读 ⼊客户端发送的命令请求内容， 具体实现为 unistd.h/read 函数的包装。 当 ⼀ 个 客 户 端 通 过 连 接 应 答 处 理 器 成 功 连 接 到 服 务 器 之 后 ， 服务器会将客户端套接字 的 AE_READABLE 事件和命令请求处理器关联起来， 当客户端向服务器发送命令请求的时候， 套接字就 会产⽣ AE_READABLE 事件， 引发命令请求处理器执⾏， 并执⾏相应的套接字读⼊操作， 如图 IMAGE_SERVER_RECIVE_COMAND_REQUEST 所示。

![image 5](<redis线程模型.note_images/imageFile5.png>)

digraph { label = "\n图 IMAGE_SERVER_RECIVE_COMAND_REQUEST 服务器接收客户端发来的命令请求"; rankdir = LR; client [label = "客户端", shape = circle]; server [label = "服务器\n\n\n客户端套接字产⽣\nAE_READABLE 事件\n执⾏命令请求处理 器", shape = box, height = 2]; client -> server [label = "发送命令请求"];}

在客户端连接服务器的整个过程中， 服务器都会⼀直为客户端套接字的 AE_READABLE 事件关联命令请 求处理器。

## 命令回复处理器

networking.c/sendReplyToClient 函数是 Redis 的命令回复处理器， 这个处理器负责将服务器执⾏命 令后得到的命令回复通过套接字返回给客户端， 具体实现为 unistd.h/write 函数的包装。 当服务器有命令回复需要传送给客户端的时候， 服务器会将客户端套接字的 AE_WRITABLE 事件和命令 回复处理器关联起来， 当客户端准备好接收服务器传回的命令回复时， 就会产⽣ AE_WRITABLE 事件， 引发命令回复处理器执⾏， 并执⾏相应的套接字写⼊操作， 如图 IMAGE_SERVER_SEND_REPLY 所 示。

![image 6](<redis线程模型.note_images/imageFile6.png>)

digraph { label = "\n图 IMAGE_SERVER_SEND_REPLY 服务器向客户端发送命令回复"; rankdir = LR; client [label = "客户端", shape = circle]; server [label = "服务器\n\n\n客户端套接字产⽣\nAE_WRITABLE 事件\n执⾏命令回复处理器", shape = box, height = 2]; client -> server [dir = back, label = "发送命令回复"];}

当命令回复发送完毕之后， 服务器就会解除命令回复处理器与客户端套接字的 AE_WRITABLE 事件之间 的关联。

## ⼀次完整的客户端与服务器连接事件示例

让我们来追踪⼀次 Redis 客户端与服务器进⾏连接并发送命令的整个过程， 看看在过程中会产⽣什么 事件， ⽽这些事件⼜是如何被处理的。 假设⼀个 Redis 服务器正在运作， 那么这个服务器的监听套接字的 AE_READABLE 事件应该正处于监听 状态之下， ⽽该事件所对应的处理器为连接应答处理器。 如果这时有⼀个 Redis 客户端向服务器发起连接， 那么监听套接字将产⽣ AE_READABLE 事件， 触发连 接应答处理器执⾏： 处理器会对客户端的连接请求进⾏应答， 然后创建客户端套接字， 以及客户端状 态， 并将客户端套接字的 AE_READABLE 事件与命令请求处理器进⾏关联， 使得客户端可以向主服务器 发送命令请求。 之后， 假设客户端向主服务器发送⼀个命令请求， 那么客户端套接字将产⽣ AE_READABLE 事件， 引发 命令请求处理器执⾏， 处理器读取客户端的命令内容， 然后传给相关程序去执⾏。 执⾏命令将产⽣相应的命令回复， 为了将这些命令回复传送回客户端， 服务器会将客户端套接字 的 AE_WRITABLE 事件与命令回复处理器进⾏关联： 当客户端尝试读取命令回复的时候， 客户端套接字 将产⽣ AE_WRITABLE 事件， 触发命令回复处理器执⾏， 当命令回复处理器将命令回复全部写⼊到套接 字之后， 服务器就会解除客户端套接字的 AE_WRITABLE 事件与命令回复处理器之间的关联。 图 IMAGE_COMAND_PROGRES 总结了上⾯描述的整个通讯过程， 以及通讯时⽤到的事件处理 器。

![image 7](<redis线程模型.note_images/imageFile7.png>)

digraph { label = "\n图 IMAGE_COMAND_PROGRES 客户端和服务器的通讯过程"; splines = ortho; rankdir = LR; node [shape = box, height = 3.0]; client [label = "客\n户\n端"]; server [label = "服\n务\n器"]; client -> server [label = "客户端向服 务器发送连接请求\n服务器执⾏连接应答处理器"]; client -> server [label = "\n\n客户端向服务器发送命令请求\n服务器执⾏命令请求 处理器"]; server -> client [label = "\n\n服务器向客户端发送命令回复\n服务器执⾏命令回复处理器"];}

