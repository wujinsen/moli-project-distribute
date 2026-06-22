本⽂主要解释下storm ui上各项属性的含义。

- 1. mainpage ⾸⻚主要分为3块:

- a. Cluster Sumary Nimbus uptime: nimbus的启动时间 Supervisors: storm集群中supervisor的数⽬ used slots: 使⽤了的slots数 fre slots: 剩余的slots数 total slots: 总的slots数 Runing tasks: 运⾏的任务数
- b. topology sumary Name: topology name id: topology id (由storm⽣成) status: topology的状态，包括(ACTIVE, INACTIVE, KI LED, REBALANCING) uptime: topology运⾏的时间 num workers: 运⾏的workers数 num tasks: 运⾏的task数
- c. supervisor sumary host: supervisor(主机)的主机名 uptime: supervisor启动的时间 slots: supervisor的端⼝数 used slots: 使⽤的端⼝数


- 2. topology page topology⻚⾯主要包括4个部分


- a. topology sumary(同主⻚)
- b. topology stats window: 时间窗⼝，显示10m、3h、1d和al time的运⾏状况 emited: emited tuple数 transfered: transfered tuple数, 说下与emited的区别：如果⼀个task，emited⼀个tuple到2个


task中，则transfered tuple数是emited tuple数的两倍 complete latency: spout emiting ⼀个tuple到spout ack这个tuple的平均时间 acked: ack tuple数59 failed: 失败的tuple数 c. spouts id: spout id paralelism: 任务数

last eror: 最近的错误数，只显示最近的前20个错误 emited, transfered, complete latency, acked和failed上⾯已解释 d. bolts proces latency: bolt收到⼀个tuple到bolt ack这个tuple的平均时间 其他参数都解释过了

还有componentpage和taskpage, 参数的解释同上。 taskpage中的Component指的是spoutid 或者 boltid, time指的是错误发⽣的时间，eror是指错误的

具体内容。

