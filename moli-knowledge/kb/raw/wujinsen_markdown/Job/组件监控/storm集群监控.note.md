# storm

所谓兵⻢未动，粮草先⾏，准备将 ⽤在某个项⽬中做实时数据分析。⽆论任何系统，⼀定要有监控系统并存，当故障发⽣的时候 你能第⼀个知道，⽽不是让别⼈告诉你，那处理故障就很被动了。

因此我写了这么个项⽬，取名叫storm-monitor，放在了github上

https://github.com/killme2008/storm-monitor

主要功能如下：

- 1.监控supervisor数⽬是否正确，当supervisor挂掉的时候会发送警告。

- 2.监控nimbus是否正常运⾏，monitor会尝试连接nimbus，如果连接失败就认为nimbus挂掉。

- 3.监控topology是否正常运⾏，包括它是否正常部署，是否有运⾏中的任务。


当故障发⽣的时候通过alarm⽅法警告⽤户，开放出去的只是简单地打⽇志。因为每个公司的告警接⼝不⼀样，所以你需要⾃⼰扩 展，修改alarm.clj即可。我们这⼉就⽀持旺旺告警和⼿机短信告警。

基本的原理很简单，对supervisor和topology的监控是通过zookeeper来间接地监控，通过定期查看path是否存在。对nimbus的 监控是每次起⼀个短连接连上去，连不上去即认为挂掉。

整个项⽬也是⽤clojure写。你的机器需要安装 和 插件，然后将你的storm.yaml拷⻉到conf⽬录下，编辑monitor.yaml设 定监控参数如检查间隔等，最后启动start.sh脚本即可。默认⽇志输出在logs/monitor.log。

lein exec

