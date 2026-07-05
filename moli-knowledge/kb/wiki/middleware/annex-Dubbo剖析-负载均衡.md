---
title: Dubbo剖析-负载均衡.note（原文插图 annex）
slug: annex-Dubbo剖析-负载均衡
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/面试笔试/Dubbo/Dubbo剖析-负载均衡.note.md
related: [dubbo-调用原理与分层]
created: 2026-07-05
updated: 2026-07-05
---

# ⼀、前⾔

在服务提供⽅是集群的时候，为了避免⼤量请求⼀直落到⼀个或者⼏个服务提供⽅机器上，从⽽使这 些机器负载很⾼，甚⾄打死，需要做⼀定的负载均衡策略。Dubbo 提供了多种均衡策略，缺省为 random 随机调⽤

# ⼆、dubbo负载均衡策略

Random LoadBalance 随机策略

按权重设置随机概率。

RoundRobin LoadBalance 轮询策略

轮循，按公约后的权重设置轮循⽐率

LeastActive LoadBalance 最少活跃调⽤数

最少活跃调⽤数，相同活跃数的随机，活跃数指调⽤前后计数差

ConsistentHash LoadBalance ⼀致性hash策略

⼀致性 Hash，相同参数的请求总是发到同⼀提供者。 当某⼀台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈 变动。

三、 何时加载负载均衡策略

![image 1](assets/imageFile1.png)

image.png

四、总结

## dubbo提供了⼏种常⻅的负载均衡策略，如果您需要定制⾃⼰额负载均衡策略，可以按照dubbo的规范 进⾏定制化，⽐如你可以定制均匀⼀致性hash对dubbo的⼀致性hash进⾏改良。
