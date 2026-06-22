本⽂将介绍如何⼿动更新 存在Zokeper中的偏移量。我们有时候需要⼿动将某个主题的偏移量 设置成某个值，这时候我们就需要更新Zokeper中的数据了。 内置为我们提供了修改偏移量的 类： kafka.tools.UpdateOffsetsInZK ，我们可以通过它修改Zokeper中某个主题的偏移 量，具体操作如下：

Kafka

Kafka

<table>
  <tr>
    <th>1</th>
    <th>[iteblog@www.iteblog.com ~]$ bin/kafka-run-class.sh kafka.tools.UpdateOffsetsInZK</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>USAGE: kafka.tools.UpdateOffsetsInZK$ [earliest | latest] consumer.properties topic</th>
  </tr>
</table>


在不输⼊参数的情况下，我们可以得知 kafka.tools.UpdateOffsetsInZK 类需要输⼊的参数。 我们的 consumer.properties ⽂件配置内容如下：

<table>
  <tr>
    <th>1</th>
    <th>zookeeper.connect=www.iteblog.com:2181</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th># timeout in ms for connecting to zookeeper</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>zookeeper.connection.timeout.ms=6000</th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>#consumer group id</th>
  </tr>
</table>


<table>
  <tr>
    <th>7</th>
    <th>group. id<br><br>=group</th>
  </tr>
</table>


这个⼯具只能把Zokeper中偏移量设置成 earliest 或者 latest ，如下：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>[iteblog@www.iteblog.com ~]$ bin/kafka-run-class.sh kafka.tools.UpdateOffsetsInZK \</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>earliest config/consumer.properties iteblog</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>updating partition 0 with new offset: 276022922</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>updating partition 1 with new offset: 234360148</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>updating partition 2 with new offset: 157237157</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>updating partition 3 with new offset: 106968019</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>updating partition 4 with new offset: 80696130</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>updating partition 5 with new offset: 317144986</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>updating partition 6 with new offset: 299182459</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>updating partition 7 with new offset: 197012246</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>updating partition 8 with new offset: 230433681</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>updating partition 9 with new offset: 120971431</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>updating partition 10 with new offset: 51200673</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>updated the offset for 11 partitions<br><br></th>
  </tr>
</table>


在有些场景下，这个⼯具不满⾜我们的需求，我们需要的是能够⼿动设置分区的偏移量为任何有 意义的值，⽽不仅仅是earliest或者latest。那咋办？

我们都知道，Kafka topic的偏移量⼀般都是存储在Zokeper中，具体的路径 为 /consumers/[groupId]/offsets/[topic]/[partitionId] ，⽐如iteblog主题分区10的 偏移量获取如下：

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>[zk: www.iteblog.com(CONNECTED) 7] get /consumers/group/offsets/iteblog/10</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>70332526</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>cZxid = 0x1ec272a4c</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>ctime = Tue Apr 12 19:15:19 CST 2016</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>mZxid = 0x256b4306a</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>mtime = Tue Apr 19 18:55:34 CST 2016</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>pZxid = 0x1ec272a4c</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>cversion = 0</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>dataVersion = 1768</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>aclVersion = 0</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>ephemeralOwner = 0x0</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>dataLength = 8</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>numChildren = 0</th>
  </tr>
</table>


所以，我们可以通过set命令来设置某个分区的偏移量，如下；

8. Consumer ofset: /consumers/[groupId]/ofsets/[topic]/[partitionId] -> long (ofset)

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>[zk: www.iteblog.com(CONNECTED) 11] set /consumers/group/offsets/iteblog/10 1024<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>cZxid = 0x1ec272a4c</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>ctime = Tue Apr 12 19:15:19 CST 2016</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>mZxid = 0x256ca2bd7</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>mtime = Tue Apr 19 19:03:39 CST 2016</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>pZxid = 0x1ec272a4c</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>cversion = 0</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th>dataVersion = 1771</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>aclVersion = 0</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>ephemeralOwner = 0x0</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>dataLength = 8</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>numChildren = 0</th>
  </tr>
</table>


这样我们就将iteblog主题的分区10的偏移量设置成1024了。 本⽂提供的两种⽅式⽤于更新Zokeper中Topic的偏移量要么不能满⾜我们的需求（使⽤

kafka.tools.UpdateOffsetsInZK ），要么就是太麻烦了很容易出错（直接通过Zokeper客 户端更新），后期我将会介绍另外⼀种⽅式来更新Kafka中Topic的偏移量，欢迎关注。

