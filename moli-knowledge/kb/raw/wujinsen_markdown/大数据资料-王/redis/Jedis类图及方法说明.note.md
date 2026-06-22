# Jedis设计

Jedis作为推荐的java语⾔redis客户端，其抽象封装为三部分：

- 1.
- 2.
- 3.


对象池设计：Pol，JedisPol，GenericObjectPol，BasePolableObjectFactory， JedisFactory ⾯向⽤户的redis操作封装：BinaryJedisComands，JedisComands，BinaryJedis，Jedis ⾯向redis服务器的操作封装：Comands，Client，BinaryClient，Conection，Protocol

其类设计图如下：

![image 1](<Jedis类图及方法说明.note_images/imageFile1.png>)

关于comon-pol的相关内容，可以参⻅：

htp:/macrochen.iteye.com/blog/3207

其他类的设计作⽤如下：

<table>
  <tr>
    <th>类名</th>
    <th>职责</th>
  </tr>
  <tr>
    <td>Pol</td>
    <td>抽象Jedis对象池操作；并委托给操作给</td>
  </tr>
  <tr>
    <td>JedisPol</td>
    <td>GenericObjectPol 实现Pol并提供JedisFactory⼯⼚</td>
  </tr>
  <tr>
    <td>JedisFactory</td>
    <td>实现BasePolableObjectFactory，提供创建，销 ⽅法</td>
  </tr>
  <tr>
    <td>BinaryJedisComands</td>
    <td>毁Jedis 抽象⾯向客户端操作的Redis命令；key，value都<br><br>数组</td>
  </tr>
  <tr>
    <td>JedisComands</td>
    <td>为序列化后的byte 抽象⾯向客户端操作的Redis命令；提供String类</td>
  </tr>
  <tr>
    <td>BinaryJedis</td>
    <td>型的key，value 实现BinaryJedisComands接⼝，并将实际操作</td>
  </tr>
  <tr>
    <td>Jedis</td>
    <td>委托给Client 实现JedisComands接⼝，并将操作委托给</td>
  </tr>
  <tr>
    <td>Comands</td>
    <td>Client 抽象Redis操作接⼝，提供String类型的key， 调⽤</td>
  </tr>
  <tr>
    <td>Conection</td>
    <td>value操作；被Jedis 抽象了Redis连接；包括host，port，pas， socket，inputstream,outputstream,protocol 完<br><br>服务器的通信</td>
  </tr>
  <tr>
    <td>Protocol</td>
    <td>成与Redis 抽象了Redis协议处理</td>
  </tr>
  <tr>
    <td>BinaryClient</td>
    <td>继承Conection类，封装了基于Byte[]的key， 操作</td>
  </tr>
  <tr>
    <td>Client</td>
    <td>value 继承BinaryClient同时实现了Comands，对上层<br><br>类型的操作</td>
  </tr>
</table>


提供基于String

# ShardedJedis实现分析

ShardedJedis是基于⼀致性哈希算法实现的分布式Redis集群客户端；ShardedJedis的设计分为以下 ⼏块：

- 1.
- 2.
- 3.


对象池设计：Pol，ShardedJedisPol，ShardedJedisFactory ⾯向⽤户的操作封装：BinaryShardedJedis，BinaryShardedJedis ⼀致性哈希实现：Sharded

关于ShardedJedis设计，忽略了Jedis的设计细节，设计类图如下：

![image 2](<Jedis类图及方法说明.note_images/imageFile2.png>)

关于ShardedJedis类图设计，省略了对象池，以及Jedis设计的以下细节介绍：

<table>
  <tr>
    <th>类名</th>
    <th>职责</th>
  </tr>
  <tr>
    <td>Sharded</td>
    <td>抽象了基于⼀致性哈希算法的划分设计，设计思 路<br><br>基于hash算法划分redis服务器 保持每台Redis服务器的Jedis客户端 提供基于Key的划分⽅法；提供了<br><br>实现<br><br>1.<br>2.<br>3.<br></td>
  </tr>
  <tr>
    <td>BinaryShardedJedis</td>
    <td>ShardKeyTag 同BinaryJedis类似，实现BinaryJedisComands 操作</td>
  </tr>
  <tr>
    <td>ShardedJedis</td>
    <td>对外提供基于Byte[]的key，value 同Jedis类似，实现JedisComands对外提供基 操作</td>
  </tr>
</table>


于String的key，value

## Sharded⼀致性哈希实现

shared⼀致性哈希采⽤以下⽅案：

- 1.
- 2.
- 3.


Redis服务器节点划分：将每台服务器节点采⽤hash算法划分为160个虚拟节点(可以配置划分权 重) 将划分虚拟节点采⽤TreMap存储 对每个Redis服务器的物理连接采⽤LinkedHashMap存储

4.

对Key or KeyTag 采⽤同样的hash算法，然后从TreMap获取⼤于等于键hash值得节点，取最邻 近节点存储；当key的hash值⼤于虚拟节点hash值得最⼤值时，存⼊第⼀个虚拟节点

sharded采⽤的hash算法：MD5 和 MurmurHash两种；默认采⽤64位的MurmurHash算法；有兴趣的 可以研究下，MurmurHash是⼀种⾼效，低碰撞的hash算法；参考地址：

htp:/blog.csdn.net/yfkis/article/details/737382 htps:/sites.gogle.com/site/murmurhash/

