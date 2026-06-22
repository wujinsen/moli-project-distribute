⼀、任务执⾏及通信的单元 Storm中关于任务执⾏及通信的三个概念：Worker（进程）、Executor（线程）和Task（Spout、 Bolt）

- 1、 ⼀个worker进程执⾏的是⼀个Topology的⼦集（不会出现⼀个worker进程为多个Topology服 务），⼀个worker进程会启动⼀个或多个executor线程来执⾏⼀个topology的component（Spout或 Bolt），因此，⼀个运⾏中的topology就是由集群中多台物理机上的多个worker进程组成的；
- 2、 Executor是⼀个被Worker进程启动的单独线程，每个executor只会运⾏⼀个topology的⼀个 component（spout或bolt）的task（task可以是⼀个或多个，Storm默认是⼀个component只⽣成⼀ 个task，executor线程会在每次循环⾥顺序调⽤所有task实例）；
- 3、 Task是最终运⾏spout或bolt中代码的单元（⼀个task即为spout或bolt的⼀个实例，executor线程 在执⾏期间会调⽤该task的nextTuple或execute⽅法）topology启动后，⼀个component（spout或 bolt）的task数⽬是固定不变的，但该component使⽤的executor线程可以动态调整（例如：⼀个 executor线程可以执⾏该component的⼀个或多个task实例）这意味着，对于⼀个component存在这 样的条件，threads<=tasks（即，线程数⼩于task数⽬）。默认情况下task的数⽬等于executor线程数 ⽬，即⼀个executor线程只运⾏⼀个task。


⼆、Storm内部通信机制简单介绍

- 1、 同⼀worker间消息的发送使⽤的是LMAX Disruptor，它负责同⼀节点（同⼀进程内）上线程间的 通信；

- A、Disruptor使⽤了⼀个RingBufer替代队列，⽤⽣产者消费者指针替代锁。
- B、⽣产者消费者指针使⽤CPU⽀持的整数⾃增，⽆需加锁并且速度很快。Java的实现在Unsafe package中。


- 2、 不同worker间通信使⽤ZeroMQ（0.8）或Nety（0.9.0）；
- 3、 不同topologey之间的通信，Storm不负责，我们需要⾃⼰想办法实现，例如使⽤kafka等； Worker进程内部的结构图如下所⽰：


每⼀个worker进程都有⼀个单独的线程来监听该worker的端⼜号，并接收发送到该端⼜的数据，它将 通过⽹络发送过来的数据放到worker的接收队列⾥⾯。 它监听的端⼜号是通过supervisor.slots.ports定义的。 接收队列的⼤⼩是通过topology.receiver.bufer.size定义的，默认值为8.

Disruptor在Storm中的应⽤如下图所⽰：

三、与通信相关的⼏个配置项介绍：

- 1、 supervisor.slots.ports:worker进程的接收线程的监听端⼜；
- 2、 topology.receiver.bufer.size:worker接收线程缓存消息的⼤⼩，它将该缓存消息发送给executor 线程；需要为2的倍数
- 3、 topology.transfer.bufer.size:worker进程中向外发送消息的缓存⼤⼩；


- 4、 topology.executor.receive.bufer.size:executor线程的接收队列⼤⼩；需要为2的倍数
- 5、 topology.executor.send.bufer.size:executor线程的发送队列⼤⼩；需要为2的倍数


⽂章中作者给出的初始建议配置如 下：

htp:/ w.michael-nol.com/blog/2013/06/21/understanding-storm-internal-mesage-bufers/

Try the folowing setings as a first start and se whether it improves the performance of your Storm topology

conf.put(Conﬁg.TOPOLOGY_RECEIVER_BUFFER_SIZE, 8); conf.put(Conﬁg.TOPOLOGY_TRANSFER_BUFFER_SIZE, 32); conf.put(Conﬁg.TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE, 16384); conf.put(Conﬁg.TOPOLOGY_EXECUTOR_SEND_BUFFER_SIZE, 16384);

