本⽂主要分析storm的worker进程间消息传递机制，消息的接收和处理的⼤概流程⻅下图

![image 1](<Storm进程通信机制分析.note_images/imageFile1.png>)

在Storm中，worker进程内部的thread通信与worker进程间的通信有⼀些差别，worker间的通信经常 需要通过⽹络跨节点进⾏，Storm使⽤ZeroMQ或Nety(0.9以后默认使⽤)作为进程间通信的消息框 架。worker进程内部通信或在同⼀个节点的不同worker的thread通信使⽤LMAX Disruptor来完成。 对于worker进程来说，为了管理流⼊和传出的消息，每个worker进程有⼀个独⽴的接收线程(对配置的 TCP端⼝supervisor.slots.ports进⾏监听)。参数topology.receiver.bufer.size代表接收线程⼀次最多能 接收多少条消息，⽤户可以⾃定义配置。接收线程将收到的消息传递给对应的executor(⼀个或多个)的 incoming-queues。对应接收线程，每个worker存在⼀个独⽴的发送线程，它负责从worker的 transfer-queue中读取消息，并通过⽹络发送给其他worker，transfer-queue的⼤⼩由参数 topology.transfer.bufer.size来设置。transfer-queue的每个元素实际上代表⼀个tuple的集合，当 executor的outgoing-queue中的tuple达到⼀定的阀值，executor的发送线程将批量获取outgoingqueue中的tuple,并发送到transfer-queue中。 每个worker进程控制⼀个或多个executor线程，⽤户可在代码中进⾏配置。每个executor有⾃⼰的 incoming-queue和outgoing-queue。⼀个worker进程运⾏⼀个专⽤的接收线程来负责将外部发送过来 的消息移动到对应的executor线程的incoming-queue中，executor中的发送线程在outgoing-queue到 达⼀定的阀值后，将outgoing-queue中的消息批量发送给所在worker的transfer-queue。executor的 incoming-queue和outgoing-queue的⼤⼩⽤户可以⾃定义配置。每个executor有单独的线程分别来处 理spout/bolt的业务逻辑和从outgoing-queue消费数据并发送到transfer-queue中。

Disruptor在Storm中的应⽤如下图所示：

![image 2](<Storm进程通信机制分析.note_images/imageFile2.png>)

