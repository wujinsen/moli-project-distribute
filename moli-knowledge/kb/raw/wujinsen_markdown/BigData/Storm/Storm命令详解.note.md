在部署好Storm集群后，可以使⽤Storm提供的命令⾏客户端提供的帮助信息

[root@sc1 ~]# storm Commands:

activate 激活指定的拓扑 classpath 获取Storm客户端运⾏命令时使⽤的类路径（classpath） deactivate 禁⽤指定的拓扑 dev-zookeeper 以dev.zookeeper.path配置的值作为本地⽬录，以storm.zookeeper.port配置的值

作为端⼝，启动⼀个新的zookeeper服务，仅⽤来开发测试。 drpc 启动⼀个drpc服务 help 查看storm的命令⾏帮助，输⼊storm时会默认启动storm help jar 在storm集群中运⾏拓扑 kill 杀死集群中正在运⾏的拓扑，storm会先在拓扑的消息超时时间旗舰禁⽤Spout，

以允许所有正在处理的消息完成处理。然后关闭Worker并清理它们的状态。 list 查看集群中正在运⾏的所有拓扑及其状态 localconfvalue 查看本地storm配置的conf-name的值 logviewer 启动logviewer守护进程 monitor nimbus 启动storm集群的主控节点的守护进程，该守护进程与Hadoop的JobTracker类似 rebalance 重新平衡集群中正在运⾏的Worker。如果需要扩展Worker，当前集群中有10个

节点且每个节点运⾏4个Worker，然后需要添加另外10个节点到集群中。这时候希望

Spout能扩散正在运⾏中的拓扑的Worker，让每个节点运⾏2个Worker。解决⽅法 有两种，⼀种是杀死拓扑重新提交拓扑，⼀种是Storm提供的rebalance命令。

rebalance受限会在消息超时时间内禁⽤拓扑，使⽤-w可以覆盖超时时间，然后重 新均衡分配集群的Worker，拓扑会返回到它原来的状态，即禁⽤的拓扑仍将禁⽤

激活的拓扑则继续运⾏。 remoteconfvalue 打印出远程Storm集群配置的conf-name的值，集群配置使 $STORM_PATH/conf/storm.yaml与defaults.yaml合并的结果且该命令必须在集群节点上运⾏。 repl 打开⼀个包含类路径（classpath）中的jar⽂件和配置的Clojure REPL，⽅便在调

试时使⽤。 shell 执⾏shell脚本 supervisor 启动Storm集群的⼯作节点的守护进程，该守护进程与Hadoop的TaskTracker类

似。 ui 启动Storm集群的Web界⾯并显示集群和正在运⾏拓扑的详细信息。 version 查看Storm的发⾏版本号

Help:

help help <command>

Documentation for the storm client can be found at http://storm.incubator.apache.org/documentation/Command-line-client.html Configs can be overridden using one or more -c flags, e.g. "storm list -c nimbus.host=nimbus.mycompany.com"

上述描述参考Storm官⽅⽂档http://storm.apache.org/documentation/Command-line-client.html

