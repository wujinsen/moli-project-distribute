，在最⼩配置：master、slave各⼀个节点的情况下，不管是master 还是slave down掉⼀个，“完整的”读/写功能都将受影响，这在⽣产环境中显然不能接受。幸好redis提 供了sentinel（哨兵）机制，通过sentinel模式启动redis后，⾃动监控master/slave的运⾏状态，基本 原理是：⼼跳机制+投票裁决 每个sentinel会向其它sentinal、master、slave定时发送消息，以确认对⽅是否“活”着，如果发现对⽅ 在指定时间（可配置）内未回应，则暂时认为对⽅已挂（所谓的“主观认为宕机” Subjective Down，简 称SDOWN）。 若“哨兵群”中的多数sentinel，都报告某⼀master没响应，系统才认为该master"彻底死亡"(即：客观 上的真正down机，Objective Down，简称ODOWN)，通过⼀定的vote算法，从剩下的slave节点中， 选⼀台提升为master，然后⾃动修改相关配置。

上⼀节中介绍了master-slave模式

![image 1](<redis 学习笔记(4)-HA高可用方案Sentinel配置.note_images/imageFile1.png>)

最⼩化的sentinel配置⽂件为：

- 1 1 port 7031

- 2 2

- 3 3 dir /opt/app/redis/redis-2.8.17/tmp

- 4 4

- 5 5 sentinel monitor mymaster 10.6.144.155 7030 1

- 6 6 sentinel down-after-milliseconds mymaster 5000

- 7 7 sentinel parallel-syncs mymaster 1

- 8 8 sentinel failover-timeout mymaster 15000


第1⾏，指定sentinel使⽤的端⼝，不能与redis-server运⾏实例的端⼝冲突 第3⾏，指定⼯作⽬录

- 第5⾏，显示监控master节点10.6.14.15，master节点使⽤端⼝7030，最后⼀个数字表示投票需要 的"最少法定⼈数"，⽐如有10个sentinal哨兵都在监控某⼀个master节点，如果需要⾄少6个哨兵发现 master挂掉后，才认为master真正down掉，那么这⾥就配置为6，最⼩配置1台master，1台slave，在 ⼆个机器上都启动sentinal的情况下，哨兵数只有2个，如果⼀台机器物理挂掉，只剩⼀个sentinal能发 现该问题，所以这⾥配置成1，⾄于mymaster只是⼀个名字，可以随便起，但要保证5-8⾏都使⽤同⼀ 个名字
- 第6⾏，表示如果5s内mymaster没响应，就认为SDOWN 第8⾏，表示如果15秒后,mysater仍没活过来，则启动failover，从剩下的slave中选⼀个升级为master
- 第7⾏，表示如果master重新选出来后，其它slave节点能同时并⾏从新master同步缓存的台数有多少 个，显然该值越⼤，所有slave节点完成同步切换的整体速度越快，但如果此时正好有⼈在访问这些 slave，可能造成读取失败，影响⾯会更⼴。最保定的设置为1，只同⼀时间，只能有⼀台⼲这件事，这 样其它slave还能继续服务，但是所有slave全部完成缓存更新同步的进程将变慢。 另：⼀个sentinal可同时监控多个master，只要把5-8⾏重复多段，加以修改即可。


具体使⽤步骤：(约定7030是redis-server端⼝，7031是redis-sentinel端⼝，且master、slave上的 redis-server均已正常启动)

- 1、先在redis根⽬录下创建conf⼦⽬录，新建配置⽂件sentinel.conf，内容参考前⾯的内容(master和 slave上都做相同的配置)
- 2、./redis-sentinel ./conf/sentinel.conf 即可(master和slave上都启⽤sentinel，即最终有⼆个哨兵)
- 3、./redis-cli -p 7031 sentinel masters 可通过该命令查看当前的master节点情况(注，这⾥⼀定要带 sentinel的端⼝)
- 4、在master上，./redis-cli -p 7030 shutdown ，⼿动把master停掉，观察sentinel的输出


- [17569] 21 Nov1 06 56.27 # +odown master mymaster 10.6.14.15 7030 #quorum 1/1

- [17569] 21 Nov1 06 56.27 # Next failover delay: I wil not start a failover before Fri Nov 21 1 07 26 2014
- [17569] 21 Nov1 06 57.389 # +config-update-from sentinel 10.6.14.156 7031 10.6.14.156 7031 @ mymaster 10.6.14.15 7030


- [17569] 21 Nov1 06 57.389 # +switch-master mymaster 10.6.14.15 7030 10.6.14.156 7030 [17569] 21 Nov1 06 57.389 * +slave slave 10.6.53.131 7030 10.6.53.131 7030 @ mymaster 10.6.14.156 7030 从红线部分可以看出，master发⽣了迁移，等刚才停掉的master再重启后，可以观察到它将被当作 slave加⼊，类似以下输出： [36 4] 21 Nov1 1 14.540 * +convert-to-slave slave 10.6.14.15 7030 10.6.14.15 7030 @ mymaster 10.6.14.156 7030


注意事项：发⽣master迁移后，如果遇到运维需要，想重启所有redis，必须最先重启“新的”master节 点，否则sentinel会⼀直找不到master。 最后，如果想停⽌sentinel，可输⼊命令./redis-cli -p 7031 shutdown

客户端的使⽤： ⼀、Jedis

![image 2](<redis 学习笔记(4)-HA高可用方案Sentinel配置.note_images/imageFile2.png>)

View Code

4-6⾏是关键，这⾥指定了sentinel节点信息。但这段代码在运⾏时发现⼀个问题：对于1主1从的最⼩ 化配置，如果连续发⽣两次写操作，第1次set成功后，如果断点停在这⾥，down掉master，这时剩下 的slave会提升为master，但是第2次set时，会抛异常，类似：连接已断开。（注：

通过Spring-DataRedis整合Jedis与redis

时，利⽤RedisTemplate调⽤不会有这个问题，建议正式项⽬中，通过Spring整 合Redis来调⽤相关功能）

⼆、Redison

![image 3](<redis 学习笔记(4)-HA高可用方案Sentinel配置.note_images/imageFile3.png>)

View Code

同样做类似的测试，⼆次写，⼆次读，如果第1次写后，⼈⼯down掉master，剩下的slave会提升成 master，第⼆次写ok，但此时redis节点中，只剩master，没有slave了，从测试结果上看，第⼆次get 还是尝试去找slave节点，但是此时已经不存在了，所以⼀直在等候，导致后⾯的的处理被阻塞。 这不是redis的问题，⽽是Redison客户端设计不够智能。 鉴于这种现状，如果要使⽤Redison，最好做成1主2从的部署结构：(sentinel.conf中的“法定⼈数”， 建议调整成2)

![image 4](<redis 学习笔记(4)-HA高可用方案Sentinel配置.note_images/imageFile4.png>)

这样的好处是，1个master挂掉后，剩下的2台slave中，会有1台提升为master，整体仍然保证有1个 master和1个slave，读写均不受影响。

关于Sentinel的更多细节，可参考官⽹⽂档： 作者：

htp:/ w.redis.io/topics/sentinel 菩提树下的杨 过

htp:/yjmyz.cnblogs.com

出处： 本⽂版权归作者和博客园共有，欢迎转载，但未经作者同意必须保留此段声明，且在⽂章⻚⾯明显位 置给出原⽂连接，否则保留追究法律责任的权利。

