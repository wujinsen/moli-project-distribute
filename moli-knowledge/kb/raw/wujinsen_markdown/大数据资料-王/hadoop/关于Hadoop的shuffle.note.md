我们知道每个reduce task输⼊的key都是按照key排序的。

但是每个map的输出只是简单的key-value⽽⾮key-valuelist，所以洗牌的⼯作就是将map输出转化 为reducer的输⼊的过程。

在map结束之后shufle要做的事情： map的输出不是简单的写⼊本地⽂件，⽽是更多的利⽤内 存缓存和预排序⼯作，以提⾼效率。io.sort.mb ⽤于控制map 输出时候的内存⼤⼩，默认100Mb。 当map所使⽤的buffer达到⼀定⽐例的时候，会启动⼀个线程来将内存中数据写⼊磁盘。此时map过程 不会暂停直到内存消耗完位置。这个线程会先将内存中的数据按照reducer的数据切分成多块,可能是按 照reducer⼤⼩hash，然后对于每个块⾥⾯的数据按照key进⾏sort排序，此时假如定义了⼀个 combiner函数，那么排序的结果就是combiner的输⼊。每当数据缓存⼤⼩达到了限制，⼀个新的spill ⽂件就会被创建。所以，当map所有的数据都被处理了之后，就需要对多个spill⽂件进⾏合并操作。

combiner的作⽤是为了压缩mapper的输出结果，另外combiner函数需要满⾜n次combiner之后，输出 结果都保持⼀致。当然，合并成⼀个⽂件的时候hadoop默认不会压缩数据，但是可以通过设置参数指 定某个压缩类对数据进⾏压缩。

在reducer开始之前shuffle要做的事情分为两步copy和sort 阶段： copy phrase 每个reducer task新建⼏个thread⽤于将mapper的输出并⾏copy过来，copy时机是当⼀个mapper

完成之后就可以进⾏。 但是reducer是如何知晓某个mapper是否完成了任务呢，mapper完成之后会给tasktracker发送⼀个状 态更新，然后tasktraker会将该信息发送给jobtrack。然后reducer中的⼀个线程负责询问jobtracker 每 个map的输出位置。⽽每个mapper上的输出数据需要等到整个job完成之后，jobtracker会通知删除。

sort phrase 将多个map输出合并成⼀个输⼊。 example：50个map输出 分5 round进⾏⽂件合并，每次将10个⽂件合并成⼀个。

最后5个⽂件可能直接进⼊reducer阶段。

关于Task中所谓的Speculative Execution 是指当⼀个job的所有task都在running的时候，当某个task的进度⽐平均进度慢时才会启动⼀个和当前 Task⼀模⼀样的任务，当其中⼀个task完成之后另外⼀个会被中⽌，所以Speculative Task不是重复 Task⽽是对Task执⾏时候的⼀种优化策略

