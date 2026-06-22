- 1.topic注册信息 /brokers/topics/[topic] : 存储某个topic的partitions所有分配信息

/brokers/topics/[topic]/partitions/[0.N] 其中[0.N]表示partition索引号

/brokers/topics/[topic]/partitions/[partitionId]/state

/brokers/ids/[0.N]

每个broker的配置⽂件中都需要指定⼀个数字类型的id(全局不可重复),此节点为临时 znode(EPHEMERAL)

/controler_epoch -> int (epoch)

此值为⼀个数字,kafka集群中第⼀个broker第⼀次启动时为1，以后只要集群中center controler中央控 制器所在broker变更或挂掉，就会重新选举新的center controler，每次center controler变更controler_epoch值 就会 + 1;

/controler -> int (broker id of the controler) 存储center controler中央控制器所在kafka broker的信息

<table>
  <tr>
    <th>Schema:<br><br>{ "version": "版本编号⽬前固定为数字1", "partitions": { "partitionId编号": [ 同步副本组brokerId列 表 ], "partitionId编号": [ 同步副本组brokerId列表 ], . }<br><br>Example:<br><br>{"version": 1,"partitions": {"0": [1, 2],"1": [2, 1],"2": [1, 2],} 列表</th>
  </tr>
</table>


说明：紫红⾊为patitions编号，蓝⾊为同步副本组brokerId

- 2.partition状态信息

<table>
  <tr>
    <th>Schema:<br><br>{"controler_epoch": 表示kafka集群中的中央控制器选举次数,"leader": 表示该partition选举leader的 brokerId,"version": 版本编号默认为1,"leader_epoch": 该partition leader选举次数,"isr": [同步副本组brokerId列表]}<br><br>Example:</th>
  </tr>
</table>


{"controler_epoch": 1,"leader": 2,"version": 1,"leader_epoch": 0,"isr": [2, 1]}

- 3. Broker注册信息

<table>
  <tr>
    <th>Schema:<br><br>{"jmx_port": jmx端⼝号,"timestamp": kafka broker初始启动时的时间戳,"host": 主机名或ip地址,"version": 版本 编号默认为1,"port": kafka broker的服务端端⼝号,由server.properties中参数port确定}<br><br>Example:<br><br>{"jmx_port": 6061,</th>
  </tr>
</table>


"timestamp":"140306189859"version": 1,"host": "192.168.1.148","port": 9092}

- 4. Controler epoch:
- 5. Controler注册信息:


<table>
  <tr>
    <th>Schema:<br><br>{ "version": 版本编号默认为1,"brokerid": kafka集群中broker唯⼀编号,"timestamp": kafka broker中央控制器变更时 的时间戳 }<br><br>Example:</th>
  </tr>
</table>


{"version": 1,"brokerid": 3,"timestamp": "1403061802981"}

# Consumer and Consumer group概念:

a.每个consumer客户端被创建时,会向zokeper注册⾃⼰的信息;b.此作⽤主要是为了"负载均衡".c.同⼀个 Consumer Group中的Consumers，Kafka将相应Topic中的每个消息只发送给其中⼀个Consumer。d.Consumer Group中的每个Consumer读取Topic的⼀个或多个Partitions，并且是唯⼀的Consumer；e.⼀个Consumer group的 多个consumer的所有线程依次有序地消费⼀个topic的所有partitions,如果Consumer group中所有consumer总线程 ⼤于partitions数量，则会出现空闲情况;

举例说明： kafka集群中创建⼀个topic为report-log 4 partitions 索引编号为0,1,2,3 假如有⽬前有三个消费者node：注意 ->⼀个consumer中⼀个消费线程可以消费⼀个或多个partition. 如果每个consumer创建⼀个consumer thread线程,各个node消费情况如下，node1消费索引编号为0,1分 区，node2费索引编号为2,node3费索引编号为3 如果每个consumer创建2个consumer thread线程，各个node消费情况如下(是从consumer node先后启动 状态来确定的)，node1消费索引编号为0,1分区；node2费索引编号为2,3；node3为空闲状态

总结：从以上可知，Consumer Group中各个consumer是根据先后启动的顺序有序消费⼀个topic的所有partitions 的。

如果Consumer Group中所有consumer的总线程数⼤于partitions数量，则可能consumer thread或consumer会 出现空闲状态。

## Consumer均衡算法

当⼀个group中,有consumer加⼊或者离开时,会触发partitions均衡.均衡的最终⽬的,是提升topic的并发消费能⼒.

- 1) 假如topic1,具有如下partitions: P0,P1,P2,P3
- 2) 加⼊group中,有如下consumer: C0,C1
- 3) ⾸先根据partition索引号对partitions排序: P0,P1,P2,P3
- 4) 根据(consumer.id + '-'+ thread序号)排序: C0,C1
- 5) 计算倍数: M = [P0,P1,P2,P3].size / [C0,C1].size,本例值M=2(向上取整)
- 6) 然后依次分配partitions: C0 = [P0,P1],C1=[P2,P3],即Ci = [P(i * M),P(i + 1) * M -1)]


## 6. Consumer注册信息:

每个consumer都有⼀个唯⼀的ID(consumerId可以通过配置⽂件指定,也可以由系统⽣成),此id⽤来标记消 费者信息. /consumers/[groupId]/ids/[consumerIdString] 是⼀个临时的znode,此节点的值为请看consumerIdString产⽣规则,即表示此consumer⽬前所消费的topic + partitions 列表. consumerId产⽣规则：

StringconsumerUuid = nul; if(config.consumerId!=nul & config.consumerId) consumerUuid = consumerId; else { String uid = UID.randomUID() consumerUuid = "%s-%d-%s".format( InetAdres.getLocalHost.getHostName, System.curentTimeMilis,

uid.getMostSignificantBits().toHexString.substring(0,8); } StringconsumerIdString = config.groupId + "_" + consumerUuid;

<table>
  <tr>
    <th>Schema:<br><br>{"version": 版本编号默认为1,"subscription": {/订阅topic列表"topic名称": consumer中topic消费者线程 数},"patern": "static","timestamp": "consumer启动时的时间戳"}<br><br>Example:<br><br>{<br><br>"version": 1,"subscription": {"open_platform_opt_push_plus1": 5},"patern": "static","timestamp": "141294187842"<br><br>}</th>
  </tr>
  <tr>
    <td> </td>
  </tr>
</table>


- 7. Consumer owner:
- 8. Consumer ofset:


/consumers/[groupId]/owners/[topic]/[partitionId] -> consumerIdString + threadId索引编号 当consumer启动时,所触发的操作:

- a) ⾸先进⾏"Consumer Id注册";
- b) 然后在"Consumer id 注册"节点下注册⼀个watch⽤来监听当前group中其他consumer的"退 出"和"加⼊";只要此znode path下节点列表变更,都会触发此group下consumer的负载均衡.(⽐如⼀个 consumer失效,那么其他consumer接管partitions).
- c) 在"Broker id 注册"节点下,注册⼀个watch⽤来监听broker的存活情况;如果broker列表变更,将会触发 所有的groups下的consumer重新balance.


/consumers/[groupId]/ofsets/[topic]/[partitionId] -> long (ofset) ⽤来跟踪每个consumer⽬前所消费的partition中最⼤的ofset 此znode为持久节点,可以看出ofset跟group_id有关,以表明当消费者组(consumer group)中⼀个消费 者失效, 重新触发balance,其他consumer可以继续消费.

- 9. Re-asign partitions /admin/reasign_partitions


{

"fields":[ {

"name":"version", "type":"int", "doc":"version id"

}, {

"name":"partitions", "type":{

"type":"array", "items":{

"fields":[ {

"name":"topic", "type":"string", "doc":"topic of the partition to be reassigned"

}, {

"name":"partition", "type":"int", "doc":"the partition to be reassigned"

}, {

"name":"replicas", "type":"array", "items":"int", "doc":"a list of replica ids"

} ],

} "doc":"an array of partitions to be reassigned to new

replicas"

} }

]

} Example: {

"version": 1, "partitions":

[

{

"topic": "Foo", "partition": 1, "replicas": [0, 1, 3]

} ]

}

- 10. Prefered replication election /admin/prefered_replica_election


<table>
  <tr>
    <th>{<br><br>"fields":[ {<br><br>"name":"version", "type":"int", "doc":"version id"<br><br>}, {<br><br>"name":"partitions", "type":{<br><br>"type":"array", "items":{<br><br>"fields":[ {<br><br>"name":"topic", "type":"string", "doc":"topic of the partition for which preferred<br><br>replica election should be triggered" }, {<br><br>"name":"partition", "type":"int", "doc":"the partition for which preferred replica<br><br>election should be triggered"<br><br>} ],<br><br>} "doc":"an array of partitions for which preferred replica<br><br>election should be triggered"<br><br>} }<br><br>]<br><br>} 例⼦: {<br><br>"version": 1, "partitions":<br><br>[<br><br>{<br><br>"topic": "Foo", "partition": 1<br><br>}, {<br><br>"topic": "Bar", "partition": 0<br><br>} ]<br><br>}</th>
  </tr>
</table>


## 1. 删除topics

/admin/delete_topics

<table>
  <tr>
    <th>Schema: { "fields":<br><br>[ {"name": "version", "type": "int", "doc": "version id"}, {"name": "topics",<br><br>"type": { "type": "array", "items": "string", "doc": "an array of topics to be deleted"}<br><br>} ]<br><br>} 例⼦: {<br><br>"version": 1, "topics": ["foo", "bar"]<br><br>}</th>
  </tr>
</table>


### Topic配置 /config/topics/[topic_name] 例⼦

<table>
  <tr>
    <th>{<br><br>"version": 1, "config": {<br><br>"config.a": "x",<br><br>"config.b": "y",<br><br><br>... }<br><br>}</th>
  </tr>
</table>


