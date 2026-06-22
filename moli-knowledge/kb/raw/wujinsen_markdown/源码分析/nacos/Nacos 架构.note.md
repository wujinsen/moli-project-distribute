# Nacos 架构

## 基本架构及概念

![image 1](<Nacos 架构.note_images/imageFile1.png>)

服务 (Service)

服务是指⼀个或⼀组软件功能（例如特定信息的检索或⼀组操作的执⾏），其⽬的是不同的客户端可 以为不同的⽬的重⽤（例如通过跨进程的⽹络调⽤）。Nacos ⽀持主流的服务⽣态，如 Kubernetes Service、gRPC|Dubo RPC Service 或者 Spring Cloud RESTful Service。

服务注册中⼼ (Service Registry)

服务注册中⼼，它是服务，其实例及元数据的数据库。服务实例在启动时注册到服务注册表，并在关 闭时注销。服务和路由器的客户端查询服务注册表以查找服务的可⽤实例。服务注册中⼼可能会调⽤ 服务实例的健康检查 API 来验证它是否能够处理请求。

服务元数据 (Service Metadata)

服务元数据是指包括服务端点(endpoints)、服务标签、服务版本号、服务实例权重、路由规则、安全 策略等描述服务的数据。

服务提供⽅ (Service Provider)

是指提供可复⽤和可调⽤服务的应⽤⽅。

服务消费⽅ (Service Consumer)

是指会发起对某个服务调⽤的应⽤⽅。

配置 (Configuration)

在系统开发过程中通常会将⼀些需要变更的参数、变量等从代码中分离出来独⽴管理，以独⽴的配置 ⽂件的形式存在。⽬的是让静态的系统⼯件或者交付物（如 WAR，JAR 包等）更好地和实际的物理运 ⾏环境进⾏适配。配置管理⼀般包含在系统部署的过程中，由系统管理员或者运维⼈员完成这个步 骤。配置变更是调整系统运⾏时的⾏为的有效⼿段之⼀。

配置管理 (Configuration Management)

在数据中⼼中，系统中所有配置的编辑、存储、分发、变更管理、历史版本管理、变更审计等所有与 配置相关的活动统称为配置管理。

### 名字服务 (Naming Service)

提供分布式系统中所有对象(Object)、实体(Entity)的“名字”到关联的元数据之间的映射管理服务，例 如 ServiceName -> Endpoints Info, Distributed Lock Name -> Lock Owner/Status Info, DNS Domain Name -> IP List, 服务发现和 DNS 就是名字服务的2⼤场景。

### 配置服务 (Configuration Service)

在服务或者应⽤运⾏过程中，提供动态配置或者元数据以及配置管理的服务提供者。

