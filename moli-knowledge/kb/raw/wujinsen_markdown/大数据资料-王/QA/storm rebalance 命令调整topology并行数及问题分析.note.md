- 1.概述

翻译：Storm Scalable ⸺Storm弹性计算 Storm弹性计算：实时调整Topology并发数

- 2.问题
- 3.问题分析与解决


通过前⾯的介绍,我们知道Storm可以实现弹性计算,根据需要实时调整Topology的并⾏度. 参考:

- 1)
- 2)


经过试验发现, storm rebalance topology-name -n 调整worker数没有问题。 但在调整topology中某个spout或bolt的并⾏数时，有时候并不能⽣效。 如 storm rebalance topology-name -e bolt1=3 命令有时候会不⽣效。 经过进⼀步分析发现，“-e bolt1=3”, 可以⽤于减⼩bolt1的并发度，但并不能增⼤其并发度。 也就说如果默认bolt1的并发度为5（在创建topology时设定），那么我们可以⽤“-e bolt1=4”将其并发 度减⼩为4，但并不能使⽤“-e bolt1=6”将其并发发度调整为6。 “-e bolt1=6”命令的情况是：如果当前bolt1的并发度为5，则什么也做；如果bolt1当前的并发度⼩于 5，将其调整为5。

起初以为是storm的限制，后来在⽹上看到的说法是： You can only increase the paralelism (number of executors) to the number of tasks. So if your component is having for example (number of executors: 50, number of tasks: 50) then you can not increase the paralelism, however you can decrease it. 引⽤⾃：

htp:/stackoverflow.com/questions/18716780/storm-v0-8-2-rebalance-comand-not-up dating-the-number-of-executors-for-a-bolt

就是说spout和bolt的并⾏数，最多可以调整到它的taskNum，默认情况下，taskNum是和你设置的 paralismNum相同的。 可以通过如下⽅法设置Bolt或Spout的taskNum。

view plaincopy to clipboardprint? builder.setBolt("cp", new CpBolt(), 3).setNumTasks(5).noneGrouping(pre_name); 这时提交 topology后，默认cpp Bolt的excutor数是3，我们可以通过rebalance -e cpp=5 将其最⼤调整到5。

1.

