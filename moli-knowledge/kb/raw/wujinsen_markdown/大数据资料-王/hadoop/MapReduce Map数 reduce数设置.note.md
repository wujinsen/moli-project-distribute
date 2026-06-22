JobConf.setNumMapTasks(n)是有意义的，结合block size会具体影响到map任务的个数，详⻅ FileInputFormat.getSplits源码。假设没有设置mapred.min.split.size，缺省为1的情况下，针对每个⽂ 件会按照min (totalsize[所有⽂件总⼤⼩]/mapnum[jobconf设置的mapnum], blocksize)为⼤⼩来拆 分，并不是说⽂件⼩于block size就不去拆分。

- 2. 不知道你是要提⾼整个集群的map/reduce任务数，还是单个节点可并⾏运⾏的map/reduce任务数？对 于前者是⼀般只设置reduce任务数，⽽map任务数是由Splits个数决定的; 对于后者，是可以在配置中 设置的，分别为：mapred.tasktracker.map.tasks.maximum mapred.tasktracker.reduce.tasks.maximum

另外，还有个参数mapred.jobtracker.taskScheduler.maxRuningTasksPerJob，⽤来控制⼀个job最⼤ 并⾏tasks数，这个是指在集群最⼤并⾏数。

- 3.我的理解：具体看FileInputFormat.java的代码 map tasks的个数只要是看splitSize，⼀个⽂件根据splitSize分成多少份就有多少个map tasks。⽽ splitSize的计算(看FileInputFormat的源码)：splitSize = Math.max(minSize, Math.min(maxSize, blockSize);⽽ minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job);即是某种格式的⽂件的最⼩分割 size(如看源码sequenceFile是2 0)和整个job配置的最⼩分割size（即mapred-default.xml中 mapred.min.split.size的值）之间的较⼤的那个 maxSize是mapred.max.split.size（mapred-default.xml中竟然没有，我试了⼀下，在mapredsite.xml中配置覆盖也没有⽤，具体⽤法参照


htp:/hadop.hadopor.com/thread-238-1-1.html

htp:/osdir.com/ml/mahout-user.lucene.apache.org/201 0-01/msg0231.html

⽤参数配置： hadop jar /rot/mahout-core-0.2.job org.apache.mahout.clustering.lda.LDADriver -Dmapred.max.split.size=90.）,如果不配置，默认值 是long类型的最⼤值。（mapred.max.split.size不推荐配置（试）） blockSize是即hdfs-default.xml中dfs.block.size的值,可在hdf-site.xml中覆盖.这个值必须是512的倍 数，如果想要数量更多的map的tasks的个数，可以把dfs.block.size设得⼩⼀点，512，1024等等，反 正上⾯的公式保证了即使你这个blocksize设得⽐某种格式的⽂件的最⼩分割size要⼩，最后还是选者 这种格式的最⼩分割size，如果blocksize⽐它⼤，则选⽤blocksize作为splitSize的⼤⼩.

总结：如果想要多⼀点的map tasks，(1)可以设置dfs.block.size⼩⼀点，sequenceFile推荐 2048。。。（试）在eclipse运⾏时，dfs.block.size是由eclipse中mapreduce的设置 （dfs.block.size）⽣效的，⽽不是hadop的conf中的配置⽂件，但是如果⽤终端hadop jar命令跑的 话，应该是由hadop的conf中的配置⽂件决定⽣效的

(2)推荐： 可以分成多个sequenceFile来作为输⼊（把上层⽬录作为输⼊路径即可，上层⽬录下包括的 必为清⼀⾊的sequenceFile）,输⼊路径 "./"或指定上层⽬录⽂件名

reduce task的个数：

可通过job.setNumReduceTasks(n);设定。多个reduce task的话就会有多个reduce结果，part-r-00000, part-r-00001, ...part-r-0000n

增加task的数量，⼀⽅⾯增加了系统的开销，另⼀⽅⾯增加了负载平衡和减⼩了任务失败的代价； map task的数量即mapred.map.tasks的参数值，⽤户不能直接设置这个参数。Input Split的⼤⼩， 决定了⼀个Job拥有多少个map。默认input split的⼤⼩是64M（与dfs.block.size的默认值相同）。 然⽽，如果输⼊的数据量巨⼤，那么默认的64M的block会有⼏万甚⾄⼏⼗万的Map Task，集群的 ⽹络传输会很⼤，最严重的是给Job Tracker的调度、队列、内存都会带来很⼤压⼒。 mapred.min.split.size这个配置项决定了每个 Input Split的最⼩值，⽤户可以修改这个参数，从⽽改 变map task的数量。

⼀个恰当的map并⾏度是⼤约每个节点10-10个map，且最好每个map的执⾏时间⾄少⼀分钟。

reduce task的数量由mapred.reduce.tasks这个参数设定，默认值是1。

合适的reduce task数量是0.95或者0.75*( nodes * mapred.tasktracker.reduce.tasks.maximum), 其 中，mapred.tasktracker.tasks.reduce.maximum的数量⼀般设置为各节点cpu core数量，即能同时 计算的slot数量。对于0.95，当map结束时，所有的reduce能够⽴即启动；对于1.75，较快的节点结 束第⼀轮reduce后，可以开始第⼆轮的reduce任务，从⽽提⾼负载均衡

由Hive来执⾏相关的查询 hadop中默认的mapred.tasktracker.map.tasks.maximum设置是2 也即：每⼀个tasktracker同时运⾏的map任务数为2 照此默认设置，查询80天某⽤户的操作⽇志，耗时5mins, 45sec 经过测试，发现将mapred.tasktracker.map.tasks.maximum设置为节点的cpu cores数⽬或者数⽬减1 ⽐较合适 此时的运⾏效率最⾼，⼤概花费3mins, 25sec 我们现在的机器都是8核的，所以最终配置如下：

<property> <name>mapred.tasktracker.map.tasks.maximum</name> <value>8</value> <description>The maximum number of map tasks that wil be run simultaneously by a task tracker. </description>

</property> ⽽对于mapred.map.tasks（每个job的map任务数）值，hadop默认值也为2 可以在执⾏hive前，通过set mapred.map.tasks=24来设定 但由于使⽤hive，会操作多个input⽂件，所以hive默认会把map的任务数设置成输⼊的⽂件数⽬ 即使你通过set设置了数⽬，也不起作⽤…

