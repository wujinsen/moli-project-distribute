Hadop集群启动的时候⼀切正常，但⼀直处于safemode，只能读不能写，这种时候应该查看 namenode的logs，当然这可能会出现不同的情况 . 下⾯仅介绍⼀种错误处理⽅案，希望能抛砖引⽟， 能对⼤家有所启发。

org.apache.hadop.hdfs.server.namenode.SafeModeException: Canot delete /home/hadop/tmp/mapred/system. Name node is in safe mode.

The ratio of reported blocks 0. 0 has not reached the threshold 0. 90. Safe mode wil be turned of automaticaly.

由⽇志可以看出⽆法删除/home/hadop/tmp/mapred/system.（其实这只是⼀种假象，往往我们会去 纠结于这个⽬录，其实不然）

解决⽅案：

1：终极办法强制退出安全模式（safemode） hadop dfsadmin -safemode leave 这种⽅式虽然 快，但会有遗留问题，我在⽤habse的时候就遇到过，很麻烦,然后你就⽤“hadop fsck /”⼯具慢慢恢 复吧。

- 2：删除namenode下/home/hadop/tmp下的所有⽂件，重新format，当然这种⽅式⾮常暴⼒，因为 你的数据完全⽊有了（对于format后可能会遇到的问题我的另⼀篇⽂章

- 3：参考源码可发现这个错误是在检查file的时候抛出来的，基本也就是file的block丢失、错误等原因造 成的。这种情况在副本数为1的情况下会很棘⼿，其他的时候hadop基本能⾃⾏解决，错误数很多的情 况下就会⼀直处于safemode下，当然你关于集群修改配置⽂件后的分发，本⼈写了⼀个配置⽂件分发 ⼯具可以强制离开安全模式，先保证正常读写，然后再启⽤“hadop fsck /”⼯具慢慢修复。


