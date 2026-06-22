并⾏操作 Streams 和CompletableFutures ⽐较

- 1. 如果有⼤量计算的操作⽽没有I/O 操作（包括连接互联⽹），那么使⽤异步的 Streams 可以得到最 好的性能。
- 2. 相反如果有很多io操作， 使⽤ CompletableFutures可以得到更好的编弹性。


alOf&anyOf 这两个⽅法的⼊参是⼀个completableFuture组、alOf就是所有任务都完成时返回。但是是个Void的返 回值。 anyOf是当⼊参的completableFuture组中有⼀个任务执⾏完毕就返回。返回结果是第⼀个完成的任务 的结果。

# thenAply相当于回调函数（calback）

thenAplyAsync默认是异步执⾏的。这⾥所谓的异步指的是不在当前线程内执⾏。

thenAcept和thenRun都是⽆返回值的:

thenAcept接收上⼀阶段的输出作为本阶段的输⼊

thenRun根本不关⼼前⼀阶段的输出，根本不不关⼼前⼀阶段的计算结果，因为它不需要输⼊参 数

