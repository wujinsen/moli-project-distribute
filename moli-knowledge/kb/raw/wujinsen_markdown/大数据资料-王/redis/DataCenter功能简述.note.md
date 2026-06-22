源码： redis官⽹ htp:/redis.readthedocs.org/en/latest/topic/cluster-tutorial.html

- 1、⼀致性哈希环设置节点原理：

- a、将key取哈希值，并对25*25*25*25 mod取余（25*25*25*25为最⼤机器数） 为什么对25*25*25*25取余，因为如果不这样，添加机器的时候添加不上
- b、将多个redis设置has余数节点，并根据跨度设置虚拟节点
- c、将key取余后的余数分配到对应的节点或者虚拟节点上
- d、如果没有对应的节点，会顺时针存到最近的节点
- e、取时同理


- 2、DataCenter分配redis原理： a、DataCenter中有个管理⼯具的redis，DataCenter访问redis时，管理redis会创建连接池供 DataCenter访问，

并返回给DataCenter相应集群中的多个IP,Port b、DataCenter会通过多IP、Port创建多个连接池（通过shardingPol），再通过key拿到连接（哈希 取余拿连接）

- 3、动态加机器：


- a、新机器加进来，客户端读还是从⽼redis中取，存的时候同时存新redis和⽼redis
- b、同时，DataCenter中有个运维⼯具，负责轮询每个redis，取出新数据、⽼数据、保留数据、迁 移数据的key，将

key存到运维⼯具中的redis中，并进⾏标记。

- c、根据key和标记，将⽼redis中的迁移数据，添加到新redis中，并删除⽼redis中的数据。
- d、此迁移数据操作，会在新redis顺时针最近的节点，及其虚拟节点中操作。
- e、通知DataCenter数据迁移完毕，并添加新ip，port
- f、正常⼯作


