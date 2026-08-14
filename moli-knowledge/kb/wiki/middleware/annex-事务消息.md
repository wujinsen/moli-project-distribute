---
title: 事务消息.note（原文插图 annex）
slug: annex-事务消息
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/源码分析/RocketMQ/事务消息.note.md
related: [rocketmq-事务消息实践]
created: 2026-07-05
updated: 2026-07-05
---

本⽂主要介绍消息队列 RocketMQ 事务消息的概念、适⽤场景以及使⽤过程中的注意事项。

概念介绍

事务消息：消息队列 RocketMQ 提供类似 X/Open XA 的分布事务功能，通过消息队列 RocketMQ 事务消息能达到分布式事务的最终⼀致。 半消息：暂不能投递的消息，发送⽅已经将消息成功发送到了消息队列 RocketMQ 服务端，但是服 务端未收到⽣产者对该消息的⼆次确认，此时该消息被标记成“暂不能投递”状态，处于该种状态下 的消息即半消息。 消息回查：由于⽹络闪断、⽣产者应⽤重启等原因，导致某条事务消息的⼆次确认丢失，消息队列 RocketMQ 服务端通过扫描发现某条消息⻓期处于“半消息”时，需要主动向消息⽣产者询问该消息 的最终状态（Commit 或是 Rollback），该过程即消息回查。

适⽤场景

事务消息的适⽤场景示例： 通过购物⻋进⾏下单的流程中，⽤户⼊⼝在购物⻋系统，交易下单⼊⼝在交易系统，两个系统之间的 数据需要保持最终⼀致，这时可以通过事务消息进⾏处理。交易系统下单之后，发送⼀条交易下单的 消息到消息队列 RocketMQ，购物⻋系统订阅消息队列 RocketMQ 的交易下单消息，做相应的业务处 理，更新购物⻋数据。

# 使⽤⽅式

交互流程 消息队列 RocketMQ 事务消息交互流程如下所示：

![image 1](assets/imageFile1.png)

事务消息

其中：

- 1.
- 2.
- 3.
- 4.
- 5.


发送⽅向消息队列 RocketMQ 服务端发送消息。 服务端将消息持久化成功之后，向发送⽅ ACK 确认消息已经发送成功，此时消息为半消息。 发送⽅开始执⾏本地事务逻辑。 发送⽅根据本地事务执⾏结果向服务端提交⼆次确认（Commit 或是 Rollback），服务端收到 Commit 状态则将半消息标记为可投递，订阅⽅最终将收到该消息；服务端收到 Rollback 状态则 删除半消息，订阅⽅将不会接受该消息。 在断⽹或者是应⽤重启的特殊情况下，上述步骤 4 提交的⼆次确认最终未到达服务端，经过固定 时间后服务端将对该消息发起消息回查。

- 6.
- 7.


发送⽅收到消息回查后，需要检查对应消息的本地事务执⾏的最终结果。 发送⽅根据检查得到的本地事务的最终状态再次提交⼆次确认，服务端仍按照步骤 4 对半消息进 ⾏操作。

说明：事务消息发送对应步骤 1、2、3、4，事务消息回查对应步骤 5、6、7。

注意事项

- 1.
- 2.
- 3.
- 4.


事务消息的 Group ID 不能与其他类型消息的 Group ID 共⽤。与其他类型的消息不同，事务消息 有回查机制，回查时消息队列 RocketMQ 服务端会根据 Group ID 去查询客户端。 通过 ONSFactory.createTransactionProducer 创建事务消息的 Producer 时必须指定 LocalTransactionChecker 的实现类，处理异常情况下事务消息的回查。 事务消息发送完成本地事务后，可在 execute ⽅法中返回以下三种状态：

TransactionStatus.CommitTransaction 提交事务，允许订阅⽅消费该消息。 TransactionStatus.RollbackTransaction 回滚事务，消息将被丢弃不允许消费。 TransactionStatus.Unknow 暂时⽆法判断状态，期待固定时间以后消息队列 RocketMQ 服务端 向发送⽅进⾏消息回查。

可通过以下⽅式给每条消息设定第⼀次消息回查的最快时间：

- a.
- b.
- c.
- d.


Message message = new Message(); // 在消息属性中添加第⼀次消息回查的最快时间，单位秒。例如，以下设置实际第⼀次回查时间为 120 秒 ~

125 秒之间

message.putUserProperties(PropertyKeyConst.CheckImmunityTimeInSeconds,"120"); // 以上⽅式只确定事务消息的第⼀次回查的最快时间，实际回查时间向后浮动0~5秒；如第⼀次回查后事务仍未提 交，后续每隔5秒回查⼀次。

示例代码

关于收发事务消息的示例代码，请参考以下⽂档：

Java 收发事务消息 C/C++ 收发事务消息

.NET 收发事务消息
