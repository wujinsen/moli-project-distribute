其实对于⼤多数⽤kafka的⼈来说，⼀般都会选择两个开源的⼯具：KafkaOffsetMonitor和kafka-webconsole，这两款我都有⽤过，⽽且各有优缺点。

：最⼤的好处就是配置简单，只需要配个zookeeper的地址就能⽤了，坑爹的地⽅就 是不能⾃动刷新，⼿动刷新时耗时较⻓，⽽且有时候都刷不出来，另外就是图像⽤了⼀段时间就完全显 示不了了，不知道⼤家是不是这样。

KafkaOffsetMonitor

：相⽐与前者，数据是落地的，因此刷新较快，⽽且⽀持在前端⾃定义zookeeper的 地址，还能列出实时的topic⾥的具体内容。但是搭建⽐较复杂，⽽且github上的默认数据库是H2的，像 我们⼀般⽤mysql的，还得⾃⼰转化。另外在⽤的过程中，我遇到⼀个问题，在连接kafka的leader失败 的时候，会⼀直重试，其结果就是导致我kafka的那台机⼦连接数过⾼，都到2w了，不知道是不是它的⼀ 个bug。

kafka-web-console

⽽且我们还得关⼼其他指标吧， ⾥的momitor部分不是列出 了那么多监控项么，迫不得已，我得靠⾃⼰去另辟新法，我现在的做法是⽤ganglia来做监控。 哈哈，

http://kafka.apache.org/documentation.html

github上⼀搜，有戏， ，只需要在server.properties⾥添加⼏个配 置项就解决问题了，结果反复试验都没有成功，⼀看都⼀年没更新了，估计是版本问题吧，也懒得管他 了。

https://github.com/criteo/kafka-ganglia

当然还有个⽐较傻⼀些的办法，⽤CSVMetricsReporter，在配置⽂件中开启之后，就会把相应的指标分别 写⼊到csv⽂件中，然后再⽤脚本去采集即可，这个的确是可⾏的，但是对资源的消耗⽐较⼤， 等等， 不是还有这个嘛 ，⽤JMXTrans来做，修改kafka配置， 将其jmx端⼝暴露出来，然后⽤JMXTrans把数据发到ganglia，你的JMXTrans的配置⽂件可以是这样：

https://github.com/adambarthelson/kafka-ganglia

{

"servers": [ {

"port": "9999", "host": "xxxxxx", "queries": [

{

"outputWriters": [ {

"@class": "com.googlecode.jmxtrans.model.output.GangliaWriter",

"settings": { "groupName": "jvmheapmemory", "port": 8649, "host": "xxxxx"

} }

], "obj": "java.lang:type=Memory", "resultAlias": "heap", "attr": [

"HeapMemoryUsage", "NonHeapMemoryUsage"

] },

................####其他配置 ] }

这⾥可以把 ⾥列出来的mbean都加⼊进来，⽽且jmxtrans还 ⽀持GraphiteWriter，这样数据就落地了，你再想怎么处理就很easy啦。

http://kafka.apache.org/documentation.html

若是你仅仅是想监控lag和logsize这些指标，亦如KafkaOffsetMonitor中展示的那样，这⾥提供两个⽅ 法：

⼀、⽤bin/kafka-run-class.sh kafka.tools.ConsumerOffsetChecker --zkconnect localhost:2181 -group test就能列出你想要的，你可以写个脚本去定时获取lag，再制定出⼤于多少就发告警邮件啊什么 的。

⼆、⽤命令⾏的⽅式总感觉不像脚本该做的，有没有client可以去获取呢，答案是有，我就把我⽤ KafkaClient和KazooClient获取lag的脚本贡献给⼤家吧：

#!/usr/local/bin/python from kafka.client import KafkaClient from kafka.consumer import SimpleConsumer from kazoo.client import KazooClient

# Zookeepers - no need to add ports zookeepers="localhost"

# Kafka broker kafka="localhost:9092"

#consumer group group="test"

if __name__ == '__main__': broker = KafkaClient(kafka) lags = {} zk = KazooClient(hosts=zookeepers, read_only=True) #zookeeper客户端，

read_only确保不会对zookeeper更改 zk.start() logsize=0 topics = zk.get_children("/consumers/%s/owners" %(group)) for topic in topics:

logsize =0 consumer = SimpleConsumer(broker, group, str(topic)) latest_offset = consumer.pending() partitions = zk.get_children("/consumers/%s/offsets/%s" %(group,

topic))

for partition in partitions: log = "/consumers/%s/offsets/%s/%s" % (group, topic, partition) if zk.exists(log):

data, stat = zk.get(log) logsize += int(data)

lag = latest_offset - logsize lags[topic] = lag

zk.stop()

上⾯的lags就是⼀个当前topic的lag的字典咯，其实⼤体的逻辑就是通过SimpleConsumer获取到当前的 offset，再由KazooClient对zookeeper层层剥⽪，获取topic和partition的信息，得到每个partition的 logsize后累加与offset⽐较，就能有lag信息了，之后你想⼲嘛⼲嘛了，⽐如发报警邮件等等。当然也可 以像KafkaOffsetMonitor那样做⾃⼰的展示了。

http://www.opscoder.info/kafka_monitor.html

转载请注明出处：

