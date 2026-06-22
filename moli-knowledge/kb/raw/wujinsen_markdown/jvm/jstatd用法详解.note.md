jstatd，即虚拟机的jstat守护进程，主要⽤于监控JVM的创建与终⽌，并提供⼀个接⼝允许远程监控⼯ 具依附到在本地主机上运⾏的JVM。

# ⽤法摘要

1 jstatd [ options ]

options

命令⾏选项。这些选项可以是任意顺序。如果存在多余的或者⾃相⽭盾的选项，则优先考虑最后的选 项。

描述

jstatd⼯具是⼀个RMI服务器应⽤程序，主要⽤于监控HotSpot Java 虚拟机的创建与终⽌，并提供⼀个 接⼝以允许远程监控⼯具附加到本地主机上运⾏的JVM上。 jstatd服务器需要在本地主机上存在⼀个RMI注册表。jstatd服务器将尝试在默认端⼝或-p port选项指定 的端⼝附加到该RMI注册表上。如果RMI注册表不存在，jstatd应⽤程序将会⾃动创建⼀个，并绑定到-p port选项指定的端⼝上，如果省略了-p port选项，则绑定到默认的RMI注册表端⼝。你可以通过指定-nr 选项来抑制内部RMI注册表的创建。 注意：此⼯具是不受⽀持的，并且⽆法确定在未来版本的JDK中是否可⽤。⽬前，此⼯具不适⽤于 Windows 98和Windows ME平台。

选项

jstatd命令⽀持以下选项：

- -nr 当找不到现有的RMI注册表时，不尝试使⽤jstatd进程创建⼀个内部的RMI注册表。
- -p port 在指定的端⼝查找RMI注册表。如果没有找到，并且没有指定-nr选项，则在该端⼝⾃⾏创建⼀个内部 的RMI注册表。

- -n rminame RMI注册表中绑定的RMI远程对象的名称。默认的名称为JStatRemoteHost。如果多个jstatd服务器在同 ⼀主机上运⾏，你可以通过指定该选项来让每个服务器导出的RMI对象具有唯⼀的名称。不管如何，这 样做需要将唯⼀的服务器名称包含进监控客户端的hostid和vmid字符串中。

- -Joption 将选项参数传递给被javac调⽤的java启动程序。例如， -J-Xms48m 设置启动内存为48 MB。使⽤-J 将选项参数传递给执⾏Java应⽤程序的底层虚拟机，这是⼀种常⻅惯例。


# 安全性

jstatd只能够监控具有适当的本地访问权限的JVM。因此，jstatd进程必须以与⽬标JVM相同的⽤户凭 证来运⾏。某些⽤户凭据，例如基于Unix系统的rot⽤户，有权限访问系统中任何JVM导出的 instrumentation。以此凭据运⾏的jstatd进程可以监控系统上的任何JVM，但会引⼊⼀些额外的安全隐 患。 jstatd服务器不提供远程客户端的任何授权许可。因此，通过那些jstatd进程有权访问⽹络中任何⽤户 的所有JVM，运⾏jstatd进程会暴露instrumentation出⼝。这种接触可能不是你的当前环境所想要的； 在启动jstatd进程之前，尤其是和⽣产环境或者⾮安全的⽹络中，你应该考虑本地的安全策略。 如果没有安装其他的安全管理器，jstatd服务器会安装⼀个RMISecurityPolicy的实例，因此需要指定⼀ 个安全策略⽂件。策略⽂件必须遵循该默认策略实现的 。 下列策略⽂件将允许jstatd服务器在没有任何安全例外的情况下运⾏。该策略没有授权所有权限给所有 代码库那么⾃由，但却⽐授予最⼩的权限来运⾏jstatd服务器更⾃由。

策略⽂件语法

- 1 grant codebase "file:${java.home}/../lib/tools.jar" {

- 2 permission java.security.AllPermission;

- 3 };


为了使⽤此策略，请复制上述⽂本到⼀个名叫jstatd.all.policy的⽂件中，并使⽤如下命令运⾏jstatd服务 器：

1 jstatd -J-Djava.security.policy=jstatd.all.policy

对于具有更严格的安全实践的⽹络场所⽽⾔，可能使⽤⼀个⾃定义的策略⽂件来显示对特定的可信主 机或⽹络的访问，尽管这种技术容易受到IP地址欺诈攻击。 如果你的安全问题⽆法使⽤⼀个定制的策 略⽂件来处理，那么最安全的操作是不运⾏jstatd服务器，⽽是在本地使⽤jstat和jps⼯具。

远程接⼝

jstatd进程输出的接⼝是私有的，并会产⽣变化。不⿎励⽤户和开发者往此接⼝写⼊数据。

示例

这⾥有⼀些启动jstatd的示例。注意，jstatd脚本会⾃动在后台启动服务器。

使⽤内部的RMI注册表

本示例演示启动jstatd，并使⽤内部RMI注册表。本示例假定没有其它服务器绑定到默认的RMI注册表 端⼝(端⼝号 109)。

1 jstatd -J-Djava.security.policy=all.policy

## 使⽤外部的RMI注册表

本示例演示启动jstatd，并使⽤外部RMI注册表。

- 1 rmiregistry&

- 2 jstatd -J-Djava.security.policy=all.policy


本示例演示启动jstatd，并使⽤端⼝2020上的外部RMI注册表。

- 1 rmiregistry 2020&

- 2 jstatd -J-Djava.security.policy=all.policy -p 2020


本示例演示启动jstatd，并使⽤端⼝2020上的外部RMI注册表，绑定的RMI远程对象名称为 AlternateJstatdServerName。

- 1 rmiregistry 2020&

- 2 jstatd -J-Djava.security.policy=all.policy -p 2020 -n AlternateJstatdServerName


## 抑制进程中的RMI注册表的创建

本示例演示启动jstatd，⽽且即使找不到现有的RMI注册表，也不会创建⼀个新的RMI注册表。本示例 假定已经有⼀个RMI注册表在运⾏中。如果没有，则发出适当的错误信息。

1 jstatd -J-Djava.security.policy=all.policy -nr

## 启动RMI⽇志能⼒

本示例演示启动jstatd，并启⽤RMI⽇志功能。该技术对于故障排除的援助或者监控服务器活动⾮常有 ⽤。

1 jstatd -J-Djava.security.policy=all.policy -J-Djava.rmi.server.logCalls=true

# 另请参阅

java - Java应⽤启动程序

jps - java进程状态应⽤程序

jstat - Java虚拟机统计监测⼯具

rmiregistry - Java远程对象注册表 软件指南针 htp:/ w.softown.cn)

作者： ( ，转载请保留出处！

