⼀、Storm的任务分配流程及算法如下：

- 1、 先由nimbus来计算拓扑的⼯作量，及计算多少个task，task的数⽬是指spout和bolt的并发度的分 别的和，例如⼀个拓扑中有⼀个spout和⼀个bolt，并且spout的并发度为2，bolt的并发度为3，则task 数为5；
- 2、 nimbus会把计算好的⼯作分配给supervisor去做，⼯作分配的单位是task，即把计算好的⼀堆task 分配给supervisor去做，即将task-id映射到supervisor-id+port上去，具体分配算法如下：

(1)从zk上获得已有的asignment(新的toplogy当然没有了） (2)查找所有可⽤的slot，所谓slot就是可 ⽤的worker，在所有supervisor上配置的多个worker的端⼜。 (3)将任务均匀地分配给可⽤的worker

- 3、 Supervisor会根据nimbus分配给他的任务信息来让⾃⼰的worker做具体的⼯作
- 4、 Worker会到zokeper上去查找给他分配了哪些task，并且根据这些task-id来找到相应的 spout/bolt，它还需要计算出这些spout/bolt会给哪些task发送消息，然后建⽴与这些task的连接，然 后在需要发消息的时候就可以给相应的task发消息。 ⼆、Nimbus的任务分配算法特点如下：


- 1、 在slot充沛的情况下，能够保证所有topology的task被均匀的分配到整个机器的所有机器上
- 2、 在slot不⾜的情况下，它会把topology的所有的task分配到仅有的slot上去，这时候其实不是理想状 态，所以在nimbus发现有多余slot的时候，它会重新分配topology的task分配到空余的slot上去以达到 理想状态。
- 3、 在没有slot的时候，它什么也不做 三、可插拔的任务分配器 Storm在0.8.0之后引⼊了可插拔式的任务分配器，使得Storm的任务分配更加灵活。


- 1、 默认调度器 Storm默认的调度实现是DefaultScheduler.clj;Storm调度可以通过storm.sheduler配置，如果不配置， 则使⽤backtype.storm.sheduler. DefaultScheduler.clj；上述的Nimbus的任务分配算法使⽤的就是该 默认调度器；
- 2、 调度隔离机制 在Storm的0.8.2之后加⼊了隔离调度机制，它使得⼀些拓扑中分享集群变得简单和安全。隔离调度程 序允许指定哪些拓扑应该孤⽴，这意味着它们运⾏在集群中的⼀组专⽤的机器上，这些专⽤的机器上 将没有其他拓扑运⾏。⼀旦所有隔离拓扑分配完，剩余的机器将在所有⾮隔离的机器中共享集群拓 扑。 它通过backtype.storm.sheduler.IsolationScheduler来实现，即将storm.sheduler配置为 backtype.storm.sheduler.IsolationScheduler，如下： storm.sheduler：backtype.storm.sheduler.IsolationScheduler 使⽤isolation.scheduler.machines配置确定多少机器在⼀个拓扑中，该配置是⼀个拓扑名称映射到机 器的数量，例如： isolation.scheduler.machines:


- “ap1”: 5
- “ap2”: 3


- “ap3”: 2


- 3、 ⾃定义调度器 Storm的这种实现⽅式实现了可插拔的任务分配器，我们也可以写⼀个⾃⼰的任务调度器来实现我们的 特定的场景以及需求，例如：如果想将某个spout或者bolt分配到固定的机器上去；或者想将两个很耗 CPU的拓扑分开来，即不让他们运⾏在同⼀机器上等。要实现⼀个我们⾃⼰的Scheduler只需要实现 IScheduler接⼜即可 四、我们对Storm分配机制的丰富 Storm⾃⾝的分配机制会尽量保证⼀个Topology会被平均分配到当前集群上，但是它没有考虑整个集 群的负载均衡；例如现在集群有三台机器（三台Supervisor），每个上⾯的可⽤Slot数⽬均为四个，那 么现在提交Topology，并且Topology占⽤1个worker，提交多个Topology后，它会先将整个集群中的 ⼀个机器占满，然后再去给别的机器分配。这种分配⽅式对有些场景是不太适⽤的，因此我们对Storm ⾃⾝的分配机制增加了额外的⼀个配置； 配置项如下： default.schedule.mode: "average" 如果default.schedule.mode配置为average，则在使⽤默认的分配机制时会优先将任务分配给空闲Slot 数⽬最多的机器。


