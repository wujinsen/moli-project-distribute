htps:/olnrao.wordpres.com/2015/05/15/apache-kafka-case-of-mysterious-rebalances/

⽂章⼤意是说，两个consumer使⽤同⼀个groupid消费不同的topic，导致kafka rebalance 从业务⻆度讲，topic本来就是给不同业务场景⽤的，也不该公⽤group

kafka rebalance主要影响有：

- 1.可能重复消费
- 2.影响消费速度：频繁的Rebalance反⽽降低了消息的消费速度，⼤部分时间都在重复消费和 Rebalance
- 3.集群不稳定


