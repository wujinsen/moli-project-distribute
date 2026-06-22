# logstash 和filebeat都具有⽇志收集功能，filebeat更轻量，占⽤资源更少，但logstash 具有filter功 能，能过滤分析⽇志。⼀般结构都是filebeat采集⽇志，然后发送到消息队列，redis，kafaka。然后 logstash去获取，利⽤filter功能过滤分析，然后存储到elasticsearch中

