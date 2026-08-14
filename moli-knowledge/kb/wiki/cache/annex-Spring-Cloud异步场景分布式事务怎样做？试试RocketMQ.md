---
title: Spring Cloud异步场景分布式事务怎样做？试试RocketMQ.note（原文插图 annex）
slug: annex-Spring-Cloud异步场景分布式事务怎样做？试试RocketMQ
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/消息队列/RocketMQ/Spring Cloud异步场景分布式事务怎样做？试试RocketMQ.note.md
related: [延迟消息与队列]
created: 2026-07-05
updated: 2026-07-05
---

htps:/segmentfault.com/a/19 020464038?utm_source=tag-newest

# ⼀、背景

在微服务架构中，我们常常使⽤异步化的⼿段来提升系统的 吞吐量 和 解耦 上下游，⽽构建异步架构 最常⽤的⼿段就是使⽤ 消息队列(MQ)，那异步架构怎样才能实现数据⼀致性呢？本⽂主要介绍如何使 ⽤RocketMQ的事务消息来解决⼀致性问题。 RocketMQ 是阿⾥巴巴开源的分布式消息中间件，⽬前已成为 Apache 的顶级项⽬。历经多次天猫双 ⼗⼀海量消息考验，具有⾼性能、低延时和⾼可靠等特性 PS：同步场景怎样保证⼀致性？请看⽂章《 》

Spring Cloud同步场景分布式事务怎样做？试试Seata

# ⼆、MQ选型

可以看到在 业务处理 ⽅⾯来说 RocketMQ 优于其他对⼿，⽽且原⽣⽀持 事务消息 PS：业务系统⽤的是其他 MQ 产品但是⼜需要 事务消息 怎么办？学习原理⾃⼰开发实现！

# 三、什么是事务消息

例如下图的场景：⽣成订单记录 -> MQ -> 增加积分 我们是应该先 创建订单记录，还是先 发送MQ消息 呢？

- 1.
- 2.


先发送MQ消息：这个明显是不⾏的，因为如果消息发送成功，⽽订单创建失败的话是没办法把消 息收回来的 先创建订单记录：如果订单创建成功后MQ消息发送失败 抛出异常，因为两个操作都在本地事务中 所以订单数据是可以 回滚 的

上⾯的 ⽅式⼆ 看似没问题，但是 ⽹络是不可靠的！如果 MQ 的响应因为⽹络原因没有收到，所以在⾯ 对不确定的结果只好进⾏回滚；但是 MQ 端⼜确实是收到了这条消息的，只是回给客户端的 响应丢失 了！

所以 事务消息 就是⽤来保证 本地事务 与 MQ消息发送 的原⼦性！

# 四、RocketMQ事务消息原理

主要的逻辑分为两个流程：

事务消息发送及提交： 发送 half消息 MQ服务端 响应消息写⼊结果 根据发送结果执⾏ 本地事务（如果写⼊失败，此时half消息对业务 不可⻅，本地逻辑不执 ⾏）

- a.
- b.
- c.


d.

根据本地事务状态执⾏ Commit 或者 Rollback（Comit操作⽣成消息索引，消息对消费 者 可⻅）

回查流程： 对于⻓时间没有 Commit/Rollback 的事务消息（pending 状态的消息），从服务端发起⼀ 次 回查 Producer 收到回查消息，检查回查消息对应的 本地事务状态 根据本地事务状态，重新 Commit 或者 Rollback

- a.
- b.
- c.


逻辑时序图

![image 1](assets/imageFile1.png)

# 五、异步架构⼀致性实现思路

从上⾯的原理可以发现 事务消息 仅仅只是保证本地事务和MQ消息发送形成整体的 原⼦性，⽽投递到 MQ服务器后，并⽆法保证消费者⼀定能消费成功！

如果 消费端消费失败 后的处理⽅式，建议是记录异常信息然后 ⼈⼯处理，并不建议回滚上游服务的数 据(因为两者是 解耦 的，⽽且 复杂度 太⾼)

我们可以利⽤ MQ 的两个特性 重试 和 死信队列 来协助消费端处理：

- 1.
- 2.
- 3.


消费失败后进⾏⼀定次数的 重试 重试后也失败的话该消息丢进 死信队列 ⾥ 另外起⼀个线程监听消费 死信队列 ⾥的消息，记录⽇志并且预警！

因为有 重试 所以消费者需要实现 幂等性

# 六、分布式事务场景样例

下⾯就⽤刚刚提到的场景：⽣成订单记录 -> MQ -> 增加积分；来简单讲⼀下 Spring Cloud 中应该 怎么做，详细代码请 查看。 PS：怎样安装部署RocketMQ可以参考《 》

下载demo

Apache RocketMQ 消息队列部署与可视化界⾯安装

- 6.1. 引⼊依赖 使⽤ spring-cloud-stream 框架来访问 RocketMQ

Spring Cloud Stream 是⼀个构建消息驱动的框架，通过抽象的定义实现应⽤与MQ消息队列之间的解 耦，⽬前⽀持 RabbitMQ、kafka 和 RocketMQ

- 6.2. 开启事务消息 消息⽣产者需要添加 transactional: true 开启 事务消息


![image 2](assets/imageFile2.png)

![image 3](assets/imageFile3.png)

### 6.3. 订单服务发送half消息

![image 4](assets/imageFile4.png)

因为开启了 事务消息 所以这⾥发送的是 half消息 对于消费端是 不可⻅ 的

- 6.4. 订单服务监听half消息 使⽤ @RocketMQTransactionListener 注解监听 半消息，并实 现 RocketMQLocalTransactionListener 接⼝，该接⼝有两个⽅法

如果提交事务消息失败，需等待约1分钟左右 事务回查 ⽅法才会被调⽤

- 6.5. 积分服务消费消息 注意：因为有 重试，这⾥如果是真实的业务需要⾃⾏实现 幂等性
- 6.6. 消费死信队列预警


executeLocalTransaction：⽤于提交本地事务

checkLocalTransaction：⽤于事务回查

![image 5](assets/imageFile5.png)

监听并消费死信队列中的消息，⽤于记录错误⽇志，并且预警通知运维⼈员等

- 6.7. 测试⽤例 demo中提供了3个接⼝分别测试不同的场景：


事务成功 htp:/localhost

:102/suces 流程如下：

- a.
- b.
- c.


订单创建 成功 提交事务消息 成功 消费消息增加积分 成功 订单创建成功但提交事务消息失败 htp:/localhost

:102/produceEror 流程如下：

- a.
- b.
- c.
- d.
- e.


订单创建 成功 提交事务消息 失败 事务回查(等待1分钟左右) 成功 提交事务消息 成功 消费消息增加积分 成功

消费消息失败 htp:/localhost

:102/consumeEror 流程如下：

- a.
- b.
- c.
- d.
- e.
- f.
- g.


订单创建 成功 提交事务消息 成功 消费消息增加积分 失败 重试消费消息 失败 进⼊死信队列 成功 消费死信队列的消息 成功 记录⽇志并发出预警 成功

# 七、demo下载地址

htps:/gite.com/zlt2 0/microservices-platform/tre/master/zlt-demo/rocketmq-demo/rocketmqtransactional

推荐阅读

⽇志排查问题困难？分布式⽇志链路跟踪来帮你

zul集成Sentinel最新的⽹关流控组件

阿⾥注册中⼼Nacos⽣产部署⽅案

Spring Bot⾃定义配置项在IDE⾥⾯实现⾃动提示

Spring Cloud Zul的动态路由怎样做？集成Nacos实现很简单

## Spring Cloud开发⼈员如何解决服务冲突和实例乱窜？ Spring Cloud同步场景分布式事务怎样做？试试Seata
