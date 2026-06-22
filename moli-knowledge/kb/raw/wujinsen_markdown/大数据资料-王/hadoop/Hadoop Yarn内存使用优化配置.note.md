# 第⼀部分 Hadop Yarn内存使⽤优化配置

在Hadop2.0中, YARN负责管理MapReduce中的资源(内存, CPU等)并且将其打包成Container.这样可以精简 MapReduce,使之专注于其擅长的数据处理任务,将⽆需考虑资源调度。

YARN会管理集群中所有机器的可⽤计算资源.基于这些资源YARN会调度应⽤(⽐如MapReduce)发来的资源请求, 然后YARN会通过分配Container来给每个应⽤提供处理能⼒, Container是YARN中处理能⼒的基本单元,是对内存, CPU等的封装。

Yarn架构做资源管理，在每个节点上⾯运⾏NodeManager负责节点资源的分配，在Yarn上⾯Container是资源的分 配的最⼩单元。

Yarn集群的内存分配配置在yarn-site.xml⽂件中配置：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.


<property>

<name>yarn.nodemanager.resource.memory-mb</name> <value>8192</value>

<discription>每个节点可⽤内存,单位MB</discription>

</property>

<property>

<name>yarn.scheduler.minimum-alocation-mb</name> <value>1024</value>

<discription>单个任务可申请最少内存，默认1024MB</discription>

</property>

<property> <name>yarn.scheduler.maximum-alocation-mb</name> <value>8192</value>

<discription>单个任务可申请最⼤内存，默认8192MB</discription>

</property>

⽽Mapreduce的任务的内存配置：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


<property> <name>mapreduce.map.memory.mb</name> <value>1024</value>

<description>每个Map任务的物理内存限制</description>

</property>

<property> <name>mapreduce.reduce.memory.mb</name> <value>1024</value>

- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.


<description>每个Reduce任务的物理内存限制</description>

</property>

<property> <name>mapred.child.java.opts</name> <value>-Xmx1024m</value> </property>

其中mapreduce.map.memory.mb配置每个map任务的内存，应该是⼤于或者等于Container的最⼩内存。 按照上⾯的配置：每个slave可以运⾏map的数据<=yarn.nodemanager.resource.memory-

mb/mapreduce.map.memory.mb ，reduce任务的数量<=yarn.nodemanager.resource.memory-mb/mapreduce.reduce.memory.mb

# 第⼆部分 Hadop YARN配置参数剖析

⼀、RM与NM相关参数（yarn-site.xml）

- 1、ResourceManager相关配置参数


- （1）yarn.resourcemanager.adres 参数解释：ResourceManager 对客户端暴露的地址。客户端通过该地址向RM提交应⽤程序，杀死应⽤程序等。 默认值：${yarn.resourcemanager.hostname}:8032

- （2）yarn.resourcemanager.scheduler.adres 参数解释：ResourceManager 对AplicationMaster暴露的访问地址。AplicationMaster通过该地址向RM申请资 源、释放资源等。

- 默认值：${yarn.resourcemanager.hostname}:8030

（3）yarn.resourcemanager.resource-tracker.adres 参数解释：ResourceManager 对NodeManager暴露的地址.。NodeManager通过该地址向RM汇报⼼跳，领取任务 等。

- 默认值：${yarn.resourcemanager.hostname}:8031


- （4）yarn.resourcemanager.admin.adres 参数解释：ResourceManager 对管理员暴露的访问地址。管理员通过该地址向RM发送管理命令等。 默认值：${yarn.resourcemanager.hostname}:803

- （5）yarn.resourcemanager.webap.adres 参数解释：ResourceManager对外web ui地址。⽤户可通过该地址在浏览器中查看集群各类信息。 默认值：${yarn.resourcemanager.hostname}:808

- （6）yarn.resourcemanager.scheduler.clas 参数解释：启⽤的资源调度器主类。⽬前可⽤的有FIFO、Capacity Scheduler和Fair Scheduler。 默认值：org.apache.hadop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler

- （7）yarn.resourcemanager.resource-tracker.client.thread-count 参数解释：处理来⾃NodeManager的RPC请求的Handler数⽬。 默认值：50

- （8）yarn.resourcemanager.scheduler.client.thread-count 参数解释：处理来⾃AplicationMaster的RPC请求的Handler数⽬。


默认值：50

- （9）yarn.scheduler.minimum-alocation-mb/yarn.scheduler.maximum-alocation-mb 参数解释：单个可申请的最⼩/最⼤内存资源量。⽐如设置为1024和3072，则运⾏MapRedce作业时，每个Task最少 可申请1024MB内存，最多可申请3072MB内存。 默认值：1024/8192

- （10）yarn.scheduler.minimum-alocation-vcores/yarn.scheduler.maximum-alocation-vcores 参数解释：单个可申请的最⼩/最⼤虚拟CPU个数。⽐如设置为1和4，则运⾏MapRedce作业时，每个Task最少可申 请1个虚拟CPU，最多可申请4个虚拟CPU。什么是虚拟CPU，可阅读我的这篇⽂章：“YARN 资源调度器剖析”。 默认值：1/32 （ 1）yarn.resourcemanager.nodes.include-path /yarn.resourcemanager.nodes.exclude-path 参数解释：NodeManager⿊⽩名单。如果发现若⼲个NodeManager存在问题，⽐如故障率很⾼，任务运⾏失败率 ⾼，则可以将之加⼊⿊名单中。注意，这两个配置参数可以动态⽣效。（调⽤⼀个refresh命令即可） 默认值：“” （12）yarn.resourcemanager.nodemanagers.heartbeat-interval-ms 参数解释：NodeManager⼼跳间隔 默认值：1 0（毫秒）


- 2、NodeManager相关配置参数


- （1）yarn.nodemanager.resource.memory-mb 参数解释：NodeManager总的可⽤物理内存。注意，该参数是不可修改的，⼀旦设置，整个运⾏过程中不可动态修 改。另外，该参数的默认值是8192MB，即使你的机器内存不够8192MB，YARN也会按照这些内存来使⽤（傻不 傻？），因此，这个值通过⼀定要配置。不过，Apache已经正在尝试将该参数做成可动态修改的。 默认值：8192

- （2）yarn.nodemanager.vmem-pmem-ratio 参数解释：每使⽤1MB物理内存，最多可⽤的虚拟内存数。 默认值：2.1

- （3）yarn.nodemanager.resource.cpu-vcores 参数解释：NodeManager总的可⽤虚拟CPU个数。 默认值：8

- （4）yarn.nodemanager.local-dirs 参数解释：中间结果存放位置，类似于1.0中的mapred.local.dir。注意，这个参数通常会配置多个⽬录，已分摊磁盘 IO负载。 默认值：${hadop.tmp.dir}/nm-local-dir

- （5）yarn.nodemanager.log-dirs 参数解释：⽇志存放地址（可配置多个⽬录）。 默认值：${yarn.log.dir}/userlogs

- （6）yarn.nodemanager.log.retain-seconds 参数解释：NodeManager上⽇志最多存放时间（不启⽤⽇志聚集功能时有效）。 默认值：1080（3⼩时）

- （7）yarn.nodemanager.aux-services 参数解释：NodeManager上运⾏的附属服务。需配置成mapreduce_shufle，才可运⾏MapReduce程序 默认值：“” ⼆、权限与⽇志聚集相关参数（yarn-site.xml）


- 1、权限相关配置参数 这⾥的权限由三部分组成，分别是：（1）管理员和普通⽤户如何区分 （2）服务级别的权限，⽐如哪些⽤户可以

向集群提交ResourceManager提交应⽤程序，（3）队列级别的权限，⽐如哪些⽤户可以向队列A提交作业等。 管理员列表由参数yarn.admin.acl指定。 服务级别的权限是通过配置hadop-policy.xml实现的，这个与Hadop1.0类似。 队列级别的权限是由对应的资源调度器内部配置的，⽐如Fair Scheduler或者CapacityScheduler等，具体见后

⾯。

- 2、⽇志聚集相关配置参数 ⽇志聚集是YARN提供的⽇志中央化管理功能，它能将运⾏完成的Container/任务⽇志上传到HDFS上，从⽽减轻


NodeManager负载，且提供⼀个中央化存储和分析机制。默认情况下，Container/任务⽇志存在在各个 NodeManager上，如果启⽤⽇志聚集功能需要额外的配置。

- （1）yarn.log-agregation-enable 参数解释：是否启⽤⽇志聚集功能。 默认值：false

- （2）yarn.log-agregation.retain-seconds 参数解释：在HDFS上聚集的⽇志最多保存多长时间。 默认值：-1

- （3）yarn.log-agregation.retain-check-interval-seconds 参数解释：多长时间检查⼀次⽇志，并将满⾜条件的删除，如果是0或者负数，则为上⼀个值的1/10。 默认值：-1

- （4）yarn.nodemanager.remote-ap-log-dir 参数解释：当应⽤程序运⾏结束后，⽇志被转移到的HDFS⽬录（启⽤⽇志聚集功能时有效）。 默认值：/tmp/logs

- （5）yarn.log-agregation.retain-seconds 参数解释：远程⽇志⽬录⼦⽬录名称（启⽤⽇志聚集功能时有效）。 默认值：⽇志将被转移到⽬录${yarn.nodemanager.remote-ap-log-dir}/${user}/${thisParam}下 三、MapReduce相关参数（mapred-site.xml）


MapReduce相关配置参数分为两部分，分别是JobHistory Server和应⽤程序参数，JobHistory可运⾏在⼀个独⽴ 节点上，⽽应⽤程序参数则可存放在mapred-site.xml中作为默认参数，也可以在提交应⽤程序时单独指定，注意， 如果⽤户指定了参数，将覆盖掉默认参数。

以下这些参数全部在mapred-site.xml中设置。

- 1、MapReduce JobHistory相关配置参数 在JobHistory所在节点的mapred-site.xml中配置。


- （1）mapreduce.jobhistory.adres 参数解释：MapReduce JobHistory Server地址。 默认值： 0.0.0.0 1020

- （2）mapreduce.jobhistory.webap.adres 参数解释：MapReduce JobHistory Server Web UI地址。 默认值： 0.0.0.0 19 8

- （3）mapreduce.jobhistory.intermediate-done-dir 参数解释：MapReduce作业产⽣的⽇志存放位置。 默认值： /mr-history/tmp


- （4）mapreduce.jobhistory.done-dir 参数解释：MR JobHistory Server管理的⽇志的存放位置。 默认值： /mr-history/done


- 2、MapReduce作业配置参数 可在客户端的mapred-site.xml中配置，作为MapReduce作业的缺省配置参数。也可以在作业提交时，个性化指定


这些参数。

<table>
  <tr>
    <th>参数名称</th>
    <th>缺省值</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>mapreduce.job.name</td>
    <td> </td>
    <td>作业名称</td>
  </tr>
  <tr>
    <td>mapreduce.job.priority</td>
    <td>NORMAL</td>
    <td>作业优先级</td>
  </tr>
  <tr>
    <td>yarn.ap.mapreduce.am.resource.</td>
    <td>1536</td>
    <td>MR AplicationMaster占⽤的内存量</td>
  </tr>
  <tr>
    <td>mb yarn.ap.mapreduce.am.resource.</td>
    <td>1</td>
    <td>MR AplicationMaster占⽤的虚拟 个数</td>
  </tr>
  <tr>
    <td>cpu-vcores<br><br>mapreduce.am.max-atempts</td>
    <td>2</td>
    <td>CPU MR AplicationMaster最⼤失败尝试 次数</td>
  </tr>
  <tr>
    <td>mapreduce.map.memory.mb</td>
    <td>1024</td>
    <td>每个Map Task需要的内存量</td>
  </tr>
  <tr>
    <td>mapreduce.map.cpu.vcores</td>
    <td>1</td>
    <td>每个Map Task需要的虚拟CPU个数</td>
  </tr>
  <tr>
    <td>mapreduce.map.maxatempts</td>
    <td>4</td>
    <td>Map Task最⼤失败尝试次数</td>
  </tr>
  <tr>
    <td>mapreduce.reduce.memory.mb</td>
    <td>1024</td>
    <td>每个Reduce Task需要的内存量</td>
  </tr>
  <tr>
    <td>mapreduce.reduce.cpu.vcores</td>
    <td>1</td>
    <td>每个Reduce Task需要的虚拟CPU个 数</td>
  </tr>
  <tr>
    <td>mapreduce.reduce.maxatempts</td>
    <td>4</td>
    <td>Reduce Task最⼤失败尝试次数</td>
  </tr>
  <tr>
    <td>mapreduce.map.speculative</td>
    <td>false</td>
    <td>是否对Map Task启⽤推测执⾏机制</td>
  </tr>
  <tr>
    <td>mapreduce.reduce.speculative</td>
    <td>false</td>
    <td>是否对Reduce Task启⽤推测执⾏机 制</td>
  </tr>
  <tr>
    <td>mapreduce.job.queuename</td>
    <td>default</td>
    <td>作业提交到的队列</td>
  </tr>
  <tr>
    <td>mapreduce.task.io.sort.mb</td>
    <td>10</td>
    <td>任务内部排序缓冲区⼤⼩</td>
  </tr>
  <tr>
    <td>mapreduce.map.sort.spil.percent</td>
    <td>0.8</td>
    <td>Map阶段溢写⽂件的阈值（排序缓冲 区⼤⼩的百分⽐）</td>
  </tr>
  <tr>
    <td>mapreduce.reduce.shufle.paralel</td>
    <td>5</td>
    <td>Reduce Task启动的并发拷贝数据的 线程数⽬</td>
  </tr>
</table>


copies

注意，MRv2重新命名了MRv1中的所有配置参数，但兼容MRv1中的旧参数，只不过会打印⼀条警告⽇志提⽰⽤户 参数过期。MapReduce新旧参数对照表可参考Java类org.apache.hadop.mapreduce.util.ConfigUtil，举例如下：

<table>
  <tr>
    <th>过期参数名</th>
    <th>新参数名</th>
  </tr>
  <tr>
    <td>mapred.job.name</td>
    <td>mapreduce.job.name</td>
  </tr>
  <tr>
    <td>mapred.job.priority</td>
    <td>mapreduce.job.priority</td>
  </tr>
  <tr>
    <td>mapred.job.queue.name</td>
    <td>mapreduce.job.queuename</td>
  </tr>
  <tr>
    <td>mapred.map.tasks.speculative.execution</td>
    <td>mapreduce.map.speculative</td>
  </tr>
  <tr>
    <td>mapred.reduce.tasks.speculative.execution</td>
    <td>mapreduce.reduce.speculative</td>
  </tr>
  <tr>
    <td>io.sort.factor</td>
    <td>mapreduce.task.io.sort.factor</td>
  </tr>
  <tr>
    <td>io.sort.mb</td>
    <td>mapreduce.task.io.sort.mb</td>
  </tr>
</table>


四、Fair Scheduler相关参数 参考：

htp:/dongxicheng.org/mapreduce-nextgen/hadop-yarn-configurations-fair-scheduler/

五、Capacity Scheduler相关参数 Capacity Scheduler是YARN中默认的资源调度器。 参考：

htp:/dongxicheng.org/mapreduce-nextgen/hadop-yarn-configurations-capacity-schedul er/

