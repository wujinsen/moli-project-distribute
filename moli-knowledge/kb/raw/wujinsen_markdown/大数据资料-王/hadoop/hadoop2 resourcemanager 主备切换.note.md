Hadop主备查询和切换

- （1）主备查询 hdfs hadmin -getServiceState n1 yarn rmadmin -getServiceState rm1
- （2）主备切换 在Hadop的各种HA中，有个隐藏属性是很多⼈不知道的，就是强制切换，⼀般来说，我们通过命令 ⾏切换HA，需要去运⾏ hdfs hadmin -transitionToActive/transitionToStandby n2 yarn rmadmin -transitionToActive/transitionToStandby rm2


但是，这种⽅式在启⽤了ZKFC做⾃动失效恢复的状态下是不允许修改的，提示信息⾥只说了可以强 制执⾏，但是没有提供命令，其实强制切换主备命令很简单。加个forcemanual就好了。 hdfs hadmin -transitionToActive/transitionToStandby n2-forcemanual

但是这样做的后果是，ZKFC将停⽌⼯作（我实践发现没停⽌⼯作，为啥？），你将不会再有⾃动故 障切换的保障，但是有些时候，这是必须的，特别是有时候，Hadop的 N在ZKFC正常⼯作的情况 下，也会出现两个standby，两个standby的问题就在于诸如Hive和Pig这种东⻄，会直接报⼀个什 么 Operation category READ is not suported in state standby 什么什么的，甚⾄你看着明明⼀个是 active，⼀个是standby，也会报这个错误，这时候就必须⼿动强制切换了，强制切换完以后，别忘了 重新启动ZKFC就好了。这个强制切换的要求就是⽤户必须没有任何对元数据的操作，这样才能有效的 防⽌脑裂的发⽣。应该来说，进⼊安全模式再切换会⽐较稳妥⼀些。

