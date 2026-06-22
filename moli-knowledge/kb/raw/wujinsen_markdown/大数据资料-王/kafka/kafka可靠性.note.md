kafka最初是被LinkedIn设计⽤来处理log的分布式消息系统，因此它的着眼点不在数据的安全性（log 偶尔丢⼏条⽆所谓），换句话说kafka并不能完全保证数据不丢失。

尽管kafka官⽹声称能够保证at-least-once，但如果consumer进程数⼩于partition_num，这个结论不 ⼀定成⽴。

考虑这样⼀个case，partiton_num=2，启动⼀个consumer进程订阅这个topic，对应的，stream_num 设为2，也就是说启两个线程并⾏处理mesage。

如果auto.comit.enable=true，当consumer fetch了⼀些数据但还没有完全处理掉的时候，刚好到 comit interval出发了提交ofset操作，接着consumer crash掉了。这时已经fetch的数据还没有处理 完成但已经被comit掉，因此没有机会再次被处理，数据丢失。

如果auto.comit.enable=false，假设consumer的两个fetcher各⾃拿了⼀条数据，并且由两个线程同 时处理，这时线程t1处理完partition1的数据，⼿动提交ofset，这⾥需要着重说明的是，当⼿动执⾏ comit的时候，实际上是对这个consumer进程所占有的所有partition进⾏comit，kafka暂时还没有 提供更细粒度的comit⽅式，也就是说，即使t2没有处理完partition2的数据，ofset也被t1提交掉 了。如果这时consumer crash掉，t2正在处理的这条数据就丢失了。

如果希望能够严格的不丢数据，解决办法有两个：

⼿动comit ofset，并针对partition_num启同样数⽬的consumer进程，这样就能保证⼀个consumer 进程占有⼀个partition，comit ofset的时候不会影响别的partition的ofset。但这个⽅法⽐较局限， 因为partition和consumer进程的数⽬必须严格对应。

# 另⼀个⽅法同样需要⼿动comit ofset，另外在consumer端再将所有fetch到的数据缓存到queue ⾥，当把queue⾥所有的数据处理完之后，再批量提交ofset，这样就能保证只有处理完的数据才被 comit。当然这只是基本思路，实际上操作起来不是这么简单，具体做法以后我再另开⼀篇。

