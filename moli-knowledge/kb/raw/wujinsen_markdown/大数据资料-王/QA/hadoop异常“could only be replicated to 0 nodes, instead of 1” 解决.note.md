异常分析

“could only be replicated to 0 nodes, instead of 1”异常 错误检查：

- 1、看看namenode和datanode的namespaceid是否相同
- 2、看看磁盘空间是不是满了，不能备份了


解决⽅法 j将两个id改成⼀个，或者重新格式化集群。 删除data和tmp和namenode⽂件夹下的⽂件。

