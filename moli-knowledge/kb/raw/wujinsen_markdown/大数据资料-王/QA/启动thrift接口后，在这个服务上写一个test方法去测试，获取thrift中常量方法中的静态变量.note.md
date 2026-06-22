误以为是同⼀静态变量，test改变常量的值，thrift就能取到，殊不知，test和thrift是两个 进程，test和thrift获取的同⼀静态变量是存到两个进程的两个静态块中，不能共⽤。 那为什么⽤client调⽤thrift后，改变静态变量后，thrift就会⽣效呢？ 是因为client端调⽤的是thrift提供的接⼝，是⽤tcp协议提供的接⼝，实现了线程间的通信， ⽤test测试的时候，直接调⽤的是thrift应⽤源码的⽅法，实际是创建了另⼀个进程。

