使⽤logstash收集⽇志的可靠性验证

htp:/ w w.logstash.net/docs/1.4.2/

实时计算⾥，需要对⽇志实时收集，logstash可以做到。⽬前的版本是1.4.2，官⽅⽂档在

，⾥⾯有详细的配置说明，使⽤也很简单。这⾥主要对logstash的可靠性做 了简单的验证

intput为file，kil掉logstash进程

每10ms打印⼀条⽇志，⽤logstash读取；每隔20s杀掉logstash进程，⼜重启。发现logstash会有 ⾼概率重发⽇志，也有少量发送空消息，要注意代码中要过滤重复消息和空消息

关闭output output为redis，kil掉redis后，logstash向redis的写操作会阻塞。等到redis恢复后，会接着写，不会 丢数据 output为kafka，使⽤logstash-kafka这个插件( 。kafka通 常是集群，kil掉其中⼀个进程，会有短暂的kafka服务不可⽤，logstash侧会做失败重试，只要重试次 数⾜够多，不会丢数据；如果kafka所有进程都kil掉，logstash侧还是会⼀直重试，超过上限阈值后， 就会丢弃数据，这⾥就会存在丢数据的可能 logstash单点

htps:/github.com/joekiler/logstash-kafka)

⼀台服务器⼀般只允许⼀个logstash进程，如果进程挂掉了，没有⾃动恢复机制，要想办法⼿⼯拉 起它

⼩结 这⾥只试验了input为file的情况，总的来说logstash不会少传数据，但有可能多传

