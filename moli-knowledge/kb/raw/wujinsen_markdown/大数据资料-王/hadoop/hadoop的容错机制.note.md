针对⽂件内容：⽂件拆成⼩块后，每个⼩块在不同的datanode上存上N份。任意⼀个datanode挂了， 还有N-1份数据是正确的。N越⼤资源占⽤越多，可靠性越⼤。

针对⽂件记录：可以在磁盘上不同⽬录、不同分区存上N份。通过硬件本身提供的容错能⼒保证总有 ⼀份正确数据被保留下来。 同时hadop⾃⼰也会做块的内容验证的：

针对⽂件内容：hadop会记录每块内容的“内容摘要”，⽤于判断⽂件内容是否与⽂件记录相符合 针对⽂件记录：hadop有版本验证、检查点、⽇志记录等⽅式保证内容正确

Hadop实现容错的主要⽅法就是重新执⾏任务，单个任务节点(TaskTracker)会不断的与系统的核⼼节 点（JobTracker）进⾏通信，如果⼀个TaskTracker在⼀定时间内（默认是1分钟）⽆法与JobTracker进 ⾏通信，那JobTracker会假设这个TaskTracker出问题挂了，JobTracker了解给每个TaskTracker赋予了 那些map和reduce任务。 如果作业仍然在maping阶段，其它的TaskTracker会被要求重新执⾏所有的由前⼀个失败的 TaskTracker所执⾏的map任务。如果作业在reduce阶段，则其它的TaskTracker会被要求重新执⾏所 有的由前⼀个失败的TaskTracker所执⾏的reduce任务。Reduce任务⼀旦完成会把数据写到HDFS。因

此，如果⼀个TaskTracker已经完成赋予它的3个reduce任务中的2个，那只有第三个任务会被重新执 ⾏。Map任务则更复杂⼀点：即使⼀个节点已经完成了10个map任务，reducer仍可能⽆法获取这些

map任务的所有的输出。如果此时节点挂了，那它的mapper输出就不可访问了。所以已经完成的map 任务也必须被重新执⾏以使它们的输出结果对剩下的reducing机器可⽤，所有的这些都是由Hadoop平 台⾃动操作完成的。

