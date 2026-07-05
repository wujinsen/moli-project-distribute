---
title: 最新、最全、最详细的 K8S 学习笔记总结（2021最新版）（一）.note（原文插图 annex）
slug: annex-最新、最全、最详细的-K8S-学习笔记总结（2021最新版）（一）
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/容器/k8s/最新、最全、最详细的 K8S 学习笔记总结（2021最新版）（一）.note.md
related: [k8s入门与容器编排]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.jianshu.com/p/2cbdf5b65b7

问题导读：

- 1.你熟悉Docker和 吗？
- 2. 解决了那些核⼼问题？
- 3.熟悉K8S吗？


Kubernetes Kubernetes

虽然 已经很强⼤了，但是在实际使⽤上还是有诸多不便，⽐如集群管理、资源调度、⽂件管理 等等。那么在这样⼀个百花⻬放的容器时代涌现出了很多解决⽅案，⽐如 Mesos、Swarm、 Kubernetes 等等，其中⾕歌开源的 是作为⽼⼤哥的存在。

Docker

Kubernetes

kubernetes 已经成为容器编排领域的王者，它是基于容器的集群编排引擎，具备扩展集群、滚动升级 回滚、弹性伸缩、⾃动治愈、服务发现等多种特性能⼒。 kubernetes 介绍

Kubernetes 解决的核⼼问题服务发现和负载均衡 Kubernetes 可以使⽤ DNS 名称或⾃⼰的 IP 地址公开容器，如果到容器的流量很⼤， 可以 负载均衡并分配⽹络流量，从⽽使部署稳定。

Kubernetes

存储编排 Kubernetes 允许您⾃动挂载您选择的存储系统，例如本地存储、公共云提供商等。

⾃动部署和回滚 您可以使⽤ 描述已部署容器的所需状态，它可以以受控的速率将实际状态更改为所需状 态。例如，您可以⾃动化 Kubernetes 来为您的部署创建新容器，删除现有容器并将它们的所有资源⽤ 于新容器。

Kubernetes

⾃动⼆进制打包 Kubernetes 允许您指定每个容器所需 CPU 和内存（RAM）。当容器指定了资源请求时，

Kubernetes

可以做出更好的决策来管理容器的资源。

⾃我修复 Kubernetes 重新启动失败的容器、替换容器、杀死不响应⽤户定义的运⾏状况检查的容器，并且在准 备好服务之前不将其通告给客户端。

密钥与配置管理

允许您存储和管理敏感信息，例如密码、OAuth 令牌和 sh 密钥。您可以在不重建容器镜 像的情况下部署和更新密钥和应⽤程序配置，也⽆需在堆栈配置中暴露密钥。 Kubernetes 的出现不仅主宰了容器编排的市场，更改变了过去的运维⽅式，不仅将开发与运维之间边 界变得更加模糊，⽽且让 DevOps 这⼀⻆⾊变得更加清晰，每⼀个软件⼯程师都可以通 过 来定义服务之间的拓扑关系、线上的节点个数、资源使⽤量并且能够快速实现⽔平扩 容、蓝绿部署等在过去复杂的运维操作。

Kubernetes

Kubernetes

知识图谱 主要介绍学习⼀些什么知识

软件架构 传统的客户端服务端架构

架构说明 Kubernetes 遵循⾮常传统的客户端/服务端的架构模式，客户端可以通过 RESTful 接⼝或者直接使⽤ kubectl 与 集群进⾏通信，这两者在实际上并没有太多的区别，后者也只是对 Kubernetes 提供的 RESTful API 进⾏封装并提供出来。每⼀个 集群都是由⼀组 Master 节点和⼀系列 的 Worker 节点组成，其中 Master 节点主要负责存储集群的状态并为 Kubernetes 对象分配和调度资 源。

Kubernetes

Kubernetes

主节点服务 - Master 架构

作为管理集群状态的 Master 节点，它主要负责接收客户端的请求，安排容器的执⾏并且运⾏控制循 环，将集群的状态向⽬标状态进⾏迁移。Master 节点内部由下⾯三个组件构成： API Server: 负责处理来⾃⽤户的请求，其主要作⽤就是对外提供 RESTful 的接⼝，包括⽤于查看集群 状态的读请求以及改变集群状态的写请求，也是唯⼀⼀个与 etcd 集群通信的组件。 etcd: 是兼具⼀致性和⾼可⽤性的键值数据库，可以作为保存 Kubernetes 所有集群数据的后台数据 库。 Scheduler: 主节点上的组件，该组件监视那些新创建的未指定运⾏节点的 Pod，并选择节点让 Pod 在 上⾯运⾏。调度决策考虑的因素包括单个 Pod 和 Pod 集合的资源需求、硬件/软件/策略约束、亲和性 和反亲和性规范、数据位置、⼯作负载间的⼲扰和最后时限。 controler-manager: 在主节点上运⾏控制器的组件，从逻辑上讲，每个控制器都是⼀个单独的进程， 但是为了降低复杂性，它们都被编译到同⼀个可执⾏⽂件，并在⼀个进程中运⾏。这些控制器包括： 节点控制器(负责在节点出现故障时进⾏通知和响应)、副本控制器(负责为系统中的每个副本控制器对 象维护正确数量的 Pod)、端点控制器(填充端点 Endpoints 对象，即加⼊ Service 与 Pod)、服务帐户 和令牌控制器(为新的命名空间创建默认帐户和 API 访问令牌)。

⼯作节点 - Node 架构 其他的 Worker 节点实现就相对⽐较简单了，它主要由 kubelet 和 kube-proxy 两部分组成。 kubelet: 是⼯作节点执⾏操作的 agent，负责具体的容器⽣命周期管理，根据从数据库中获取的信息来 管理容器，并上报 pod 运⾏状态等。 kube-proxy: 是⼀个简单的⽹络访问代理，同时也是⼀个 Load Balancer。它负责将访问到某个服务的 请求具体分配给⼯作节点上同⼀类标签的 Pod。kube-proxy 实质就是通过操作防⽕墙规则(iptables或 者ipvs)来实现 Pod 的映射。 Container Runtime: 容器运⾏环境是负责运⾏容器的软件，Kubernetes ⽀持多个容器运⾏环境: Docker、 containerd、cri-o、 rktlet 以及任何实现 Kubernetes CRI(容器运⾏环境接⼝)。

组件说明 主要介绍关于 K8s 的⼀些基本概念

主要由以下⼏个核⼼组件组成： apiserver 所有服务访问的唯⼀⼊⼝，提供认证、授权、访问控制、API 注册和发现等机制 controler manager 负责维护集群的状态，⽐如副本期望数量、故障检测、⾃动扩展、滚动更新等 scheduler 负责资源的调度，按照预定的调度策略将 Pod 调度到相应的机器上 etcd 键值对数据库，保存了整个集群的状态 kubelet 负责维护容器的⽣命周期，同时也负责 Volume 和⽹络的管理 kube-proxy 负责为 Service 提供 cluster 内部的服务发现和负载均衡 Container runtime 负责镜像管理以及 Pod 和容器的真正运⾏ 除了核⼼组件，还有⼀些推荐的插件： CoreDNS 可以为集群中的 SVC 创建⼀个域名 IP 的对应关系解析的 DNS 服务 Dashboard 给 K8s 集群提供了⼀个 B/S 架构的访问⼊⼝ Ingres Controler 官⽅只能够实现四层的⽹络代理，⽽ Ingres 可以实现七层的代理 Prometheus 给 K8s 集群提供资源监控的能⼒ Federation 提供⼀个可以跨集群中⼼多 K8s 的统⼀管理功能，提供跨可⽤区的集群 以上内容参考链接:

htps:/ w.escapelife.site/p.

安装 安装v1.16.0版本，竟然成功了。记录在此，避免后来者踩坑。 本篇⽂章，安装⼤步骤如下： 安装docker-ce 18.09.9（所有机器） 设置k8s环境前置条件（所有机器） 安装k8s v1.16.0 master管理节点 安装k8s v1.16.0 node⼯作节点

安装flanel（master） 详细安装步骤参考： 集群安装教程请参考：

CentOS 搭建 K8S，⼀次性成功，收藏了！ 全⽹最新、最详细基于V1.20版本，⽆坑部署最⼩化 K8S 集群教程

Pod 实现原理 Pod 就是最⼩并且最简单的 Kubernetes 对象

Pod、Service、Volume 和 Namespace 是 Kubernetes 集群中四⼤基本对象，它们能够表示系统中部 署的应⽤、⼯作负载、⽹络和磁盘资源，共同定义了集群的状态。Kubernetes 中很多其他的资源其实 只对这些基本的对象进⾏了组合。 Pod -> 集群中的基本单元 Service -> 解决如何访问 Pod ⾥⾯服务的问题 Volume -> 集群中的存储卷 Namespace -> 命名空间为集群提供虚拟的隔离作⽤ 详细介绍请参考： 未完待续 .

Kubernetes 之 Pod 实现原理

最新经典⽂章，欢迎关注公众号

![image 1](assets/imageFile1.png)

htps:/ w.jianshu.com/p/2cbdf5b65b7

原⽂链接：
