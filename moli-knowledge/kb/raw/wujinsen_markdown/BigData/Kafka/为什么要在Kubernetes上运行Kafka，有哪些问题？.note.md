# htps:/ w.aboutyun.com/thread-26252-1-1.html

问题导读

- 1.为什么要在Kubernetes上运⾏Kafka？
- 2.Kafka在Kubernetes需要注意哪些问题？
- 3.在Kubernetes上的部署Kafka时，需要什么团队⽅⾯的合作？


关注最新经典⽂章，欢迎关注公众号

为什么要在Kubernetes上运⾏Kafka？ 我看到⼤型组织的情况，未在Kubernetes部署Kafka，导致严重的组织问题。在这种情况下，我通

常会说这不是⼀个好的⽅案。推荐在Kubernetes上运⾏Kafka，如果这样做，将更快地分配你的环 境，并且能够利⽤时间进⾏富有成效的⼯作，⽽不是组织战⽃。如果出现问题，将从内部基础架构团 队获得更好的服务，因为将在他们熟悉的环境中运⾏。

其次，⼤多数组织都低估了他们最终会部署多少个Kafka集群。随着⽤例数量的增加，最终会拥有多 个⽣产集群。当然还有开发环境，测试环境，试⽤新版环境，各种部署环境等等。

Kubernetes确实可以更轻松地部署和管理新集群。可以使⽤其他部署⼯具，如Confluent的Ansible 脚本，但它们没有内置的扩展，监控，重启，升级等规定。

⼀旦你习惯了Kubernetes（并且不需要很⻓时间），你会发现Kafka的管理变得更加容易。扩展添 加新broker变得更容易，是单个命令或配置⽂件中的单个⾏。并且更容易在所有broker和所有集群

上执⾏配置更改，升级和重新启动。

Kafka在Kubernetes需要注意的问题 Kafka是⼀项有状态的服务，这确实使Kubernetes配置⽐⽆状态微服务更复杂。 配置存储和⽹络时 将⾯临最⼤的挑战，您需要确保两个⼦系统都能提供⼀致的低延迟。

kafka on Kubernetes和其他有状态服务需要使⽤共享存储。对Kubernetes的本地持久存储的⽀持 仍处于测试阶段，不建议⽤于⽣产，尽管现在可能是开始在试点项⽬上测试它的好时机，因为它可能 在⼏个⽉内成为GA。

遗憾的是，许多组织仍⽆法在其共享存储设备上提供⼀致的低延迟。如果想在Kubernetes上成功运 ⾏Kafka，需要确保存储团队了解这些要求，并确保⼀起验证它们是否符合要求。

Kafka还提出了⼤多数有状态服务都没有的挑战：Brokers不可互换，客户端需要直接与包含他们⽣ 产或消费的每个分区的主要副本的Broker进⾏通信。不能将所有Brokers置于单个负载均衡器地址之 后。需要⼀种⽅法将消息路由到特定的Broker。

这并不是特别困难。在Kubernetes上的部署Apache Kafka，在⼤多数情况下，该过程需要⽹络团 队的合作。

这⾥的主要教训是，只有当拥有熟练的存储和⽹络团队的合作时，才能成功地在Kubernetes上运⾏ Kafka。如果你没有那些，你将遇到麻烦。但同样⼀直如此 - Kafka依赖于良好的基础设施，缺乏良 好企业基础设施的客户⽆论如何都会遇到问题。

另外，建议不要选择Kafka作为第⼀个在Kubernetes上运⾏的服务。让基础架构团队⾸先获得部 署，监控，更新和排除⽆状态服务故障的经验，例如Kafka Streams应⽤程序。

如前所述，Kubernetes上的任何单个应⽤程序都不会为你带来太多好处。当你使⽤它来管理所有应 ⽤程序和基础架构时，Kubernetes真的很棒。如果业务应⽤程序也在Kubernetes上运⾏，那么在 Kubernetes上运⾏Kafka brokers是最有益的。

现在，⼀些⼯程师会考虑这样⼀个事实：您需要使⽤负载均衡器策略在共享存储和headles services上配置持久卷，并将这些视为“workarounds”，并指出Kubernetes上的StatefulSets还不是很 成熟。在我们看来，Kafka有特定的要求，Kubernetes提供⽀持Kafka的机制。在1.9版本中， StatefulSets是Kubernetes⽣态系统中的 first-clas。

相关⽂章推荐： 知乎基于 Kubernetes 的 Kafka 平台探索和实践 htp:// w.aboutyun.com/forum.php?mod=viewthread&tid=24786

相关代码下载： kubernetes-kafka-master.zip (48.67 KB, 下载次数: 1)

源码地址： htps:/github.com/Yolean/kubernetes-kafka

![image 1](<为什么要在Kubernetes上运行Kafka，有哪些问题？.note_images/imageFile1.png>)

