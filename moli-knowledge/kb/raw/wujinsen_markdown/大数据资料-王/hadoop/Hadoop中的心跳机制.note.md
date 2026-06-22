主节点和从节点之间的通信是通过⼼跳机制实现的，如NameNode与DataNode之间，JobTracker和 TaskTracker之间。所谓“⼼跳”是⼀种形象化描述，指的是持续的按照⼀定频率在运⾏，类似于⼼脏在 永⽆休⽌的跳动。 图7-6指的是dataNode向NameNode发送⼼跳的周期是3秒。

- 图7-6 当⻓时间没有发送⼼跳时，NameNode就判断DataNode的连接已经中断,不能继续⼯作了,就把他定性 为”dead node”。NameNode会检查dead node中的副本数据，复制到其他的data node中。 我们现在来看⼀下他们之间通过⼼跳是如何实现通信的。

通过前⾯的RPC机制介绍，我们知道NameNode与DataNode直接的通信是通过DataNodeProtocol接 ⼝实现的。如图7-7所示。

- 图7-7 该接⼝的实现类是NameNode，是由DataNode调⽤的。下⾯我们就来分析⼀下他们之间是如何通信 的。 先看⼀下DataNode中的代码，如图7-8所示。
- 图7-8 该⽅法是DataNode中⾮常重要的⽅法，该⽅法在系统运⾏时会进⼊⼀个死循环中，定期的调⽤⽅法访 问NameNode，访问周期⻅第963⾏代码，这⾥的变量heatBeatInterval是从配置⽂件读取的，如图7-
- 9所示，默认值是3 0毫秒，也就说循环间隔不低于3秒钟。快过频繁的运⾏会增加系统的负载。 那么，当进⼊if条件语句时，就会执⾏第972⾏的代码。这⾥就是通过namenode调⽤其sendHearbeat ⽅法。这⾥的namenode其实是DataNode获得的NameNode实例的代理对象，如图7-10所示。 DataNode与NameNode的通信是通过DataNodeProtocol接⼝实现的。那么，我们下⾯重点看⼀下调 ⽤的sendHeartbeat⽅法，如图7-1。


# NameNode与DataNode之间的⼼跳

- 图7-9
- 图7-10


图7-1

在图7-1中，sendHeartbeat⽅法的原型定义位于DataNodeProtocol接⼝的定义中。该传递的第⼀个 形参是包含DataNode注册信息的类，包含DataNode的唯⼀标示、名称、版本、ipc端⼝等信息，这些 信息可以让NameNode把这个DataNode与其他DataNode区分开。第⼆、三、四个形参表示当前 DataNode上⾯的容量空间、使⽤量、剩余量。DataNode把这些信息告诉NameNode后，供 NameNode做决策。 还要注意，该⽅法有返回值，是⼀个DatanodeComand数组。也就是说NameNode根据DataNode送 来的信息做出决策，并把这些决策封装为DatanodeComand，发送回DataNode。DataNode拿到 NameNode送来的命令后，就要进⾏处理，⻅代码第980⾏。 从上⾯的分析可以看出，所有的DataNode是通过不断的死循环来向NameNode发送⾃身状况信息， NameNode在拿到所有DataNode的汇报信息后，综合权衡各种情况，然后向DataNode发回命令。这 正是前⾯分析的RPC机制的客户机与服务器关系的体现。

# JobTracker与TaskTracker之间的⼼跳

MapReduce的计算机制是通过⼀个JobTracker和很多的TaskTracker之间的协作完成的。JobTracker作 为管理端，是负责接收⽤户的作业请求，然后分配秩序任务给TaskTracker去执⾏的。在TaskTracker执 ⾏过程中，通过⼼跳机制会不断的向JobTracker汇报⾃⼰的执⾏情况，供JobTracker做出决策。下⾯ 分析⼀下⼼跳的过程。 ⼆者的⼼跳通信是通过接⼝InterTrackerProtocol实现的，如图7-12所示。

- 图7-12 该接⼝的实现类是JobTracker，是被TaskTracker调⽤的。下⾯我们看⼀下TaskTracker是如何调⽤的。 TaskTracker会调⽤接⼝的heartbeat⽅法，如图7-13所示。
- 图7-13 该⽅法的形参有5个。第⼀个形参是TaskTrackerStatus最重要，包括TaskTracker的通信端⼝、最多运 ⾏Map任务数、最多运⾏Reduce任务数、失败任务数等。⽬的是给JobTracker提供⾜够的信息做出决 策。JobTracker在综合了所有TaskTracker提交的各种状态报告后，会对不同TaskTracker做出决策，通 过返回值HeartbeatResponse实现的，该返回值中包含对TaskTracker的各种指示，具体信息如图7-14 所示。
- 图7-14 在图7-14中，TaskTracker发送⼼跳请求，接收到JobTracker发送回来的HeartbeatResponse对象。该 对象中含有新的任务，TaskTracker就需要处理新的任务了。这就是⼼跳机制主要做的事情。 那么，什么情况下TaskTracker可以接受新任务哪？如图7-15所示。
- 图7-15


从图7-15中可以看到，是否有能⼒接受新任务要看⼏个参数的值。分别是maxMapSlots、 maxReduceSlots、aceptNewTasks。前两者相当于map任务、reduce任务执⾏最⼤限制数。如果超 过这个数，那么就不再接受新的任务。默认值，⼆者都是2。参数aceptNewTasks的取值，取决于本 地⽂件系统(linux系统)的剩余空间。如果本地⽂件系统还有剩余磁盘空间，⾜够任务运⾏，那么改值就 是true。否则，就是false。默认情况下，只要磁盘有剩余空间，aceptNewTasks的值就是true。 以上就是对MapReduce的⼼跳机制的简单分析。通过上⾯的分析，我们看到只要TaskTracker条件允 许，就不断的接受JobTracker分配的任务。这就保证了整个作业的正常运⾏。

