# ⼀、前⾔

在进⾏系统设计时候，不仅要考虑正常逻辑该如何⾛，还要考虑异常逻辑。dubbo中当服务消费⽅调⽤ 服务提供⽅的服务出现错误时候，提供了多种容错⽅案，缺省为 failover 重试。

# ⼆、Dubbo集群容错模式

Failover Cluster

重试。当服务消费⽅调⽤服务提供者失败后⾃动切换，重试其它服务提供者。这通常⽤于读操作或者 具有幂等的写操作，需要注意的是重试会带来更⻓延迟。可通过 retries="2" 来设置重试次数(不含第⼀ 次)。

Failfast Cluster

快速失败。当服务消费⽅调⽤服务提供者失败，失败⽴即报错。通常⽤于⾮幂等性的写操作。

Failsafe Cluster

失败安全。出现异常时，直接忽略。通常⽤于写⼊审计⽇志等操作。

Failback Cluster

失败⾃动恢复。后台记录失败请求，并按照⼀定的策略后期在进⾏重试。通常⽤于消息通知操作。

Forking Cluster

并⾏调⽤多个服务提供者的服务，只要⼀个成功即返回。通常⽤于实时性要求较⾼的读操作，但需要 浪费更多服务资源。可通过 forks="2" 来设置最⼤并⾏数。

Broadcast Cluster

⼴播调⽤所有服务提供者，逐个调⽤，任意⼀台报错则报错 。通常⽤于通知所有提供者更新缓存或⽇ 志等本地资源信息

# 三、原理

- 3.1何时加载集群容错扩展实现类

- 3.2 FailoverClusterInvoker原理


![image 1](<Dubbo剖析-集群容错.note_images/imageFile1.png>)

image.png

![image 2](<Dubbo剖析-集群容错.note_images/imageFile2.png>)

image.png

# 四、总结

dubbo本身提供了丰富的集群容错模式，如果您有定制化需求，可以根据dubbo提供的扩展接⼝进⾏定 制。

