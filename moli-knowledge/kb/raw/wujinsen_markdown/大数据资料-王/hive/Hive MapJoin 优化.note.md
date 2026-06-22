- 1、Hive本地MR 如果在hive中运⾏的sql本身数据量很⼩，那么使⽤本地mr的效率要⽐分布式的快很多。但是hive本地

MR对内存使⽤很敏感，查询的数据不能太⼤，否则本地内存是吃不消的。 So the query procesor wil launch this task in a child jvm, which has the same heap size as the Maper's. Since the Local Task may run out of memory, the query procesor wil measure the memory usage of the local task very carefuly. Once the memory usage of the Local Task is higher than a threshold number. This Local Task wil abort itself and tels the user that this table is to large to hold in the memory. User can change this threshold bysethive.mapjoin.localtask.max.memory.usage = 0. 9 查询处理器会在⼀个⼦的jvm⾥运作这个任务，jvm堆⼤⼩跟Maper的堆⼤⼩⼀样。本地MR可能内存 消耗殆尽，查询处理器⽤精确的计算本地MR的内存⼤⼩，⼀旦内存超过了设定的值，那么这个MR就 会⾃动kil掉。可以通过设置hive.mapjoin.localtask.max.memory.usage =0.9，这个值太保守。 set hive.exec.mode.local.auto=true; /开启本地mr

/设置local mr的最⼤输⼊数据量,当输⼊数据量⼩于这个值的时候会采⽤local mr的⽅式 set hive.exec.mode.local.auto.inputbytes.max=5 0;

/设置local mr的最⼤输⼊⽂件个数,当输⼊⽂件个数⼩于这个值的时候会采⽤local mr的⽅式 set hive.exec.mode.local.auto.tasks.max=10; 当这三个参数同时成⽴时候，才会采⽤本地mr

- 2、Mapjoin使⽤ 就是把⼩的表加⼊内存，可以配置以下参数，是hive⾃动根据sql，选择使⽤comon join或者map


join set hive.auto.convert.join = true; hive.mapjoin.smaltable.filesize 默认值是25mb 参考⾃:htps:/cwiki.apache.org/confluence/display/Hive/MapJoinOptimization

