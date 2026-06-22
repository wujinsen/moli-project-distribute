记录⼀下今天服务器中的⽊⻢病毒——kdevtmpfsi 这是⼀个挖矿病毒，通过我docker的redis进⼊的，⼀开始没设置密码的隐患啊。 应该配置好密码，做好端⼝映射，别傻乎乎的⽤默认的主机端⼝~ 先将相应⽊⻢⽂件删除 kthreadk kthread kdevtmpfsi sudo find / -name kdevtmpfsi* sudo rm -rf . 12kdevtmpfsi 再将守护进程的⽂件删除 sudo find / -name kdevtmpfsi* sudo find / -name kinsing*

sudo rm -rf . 12pr 杀死进程 ps -aux | grep kinsing

ps -aux | grep rcu_sched ps -aux | grep stargate

sudo kil -9 PID

ps -aux | grep kdevtmpfsi ps -aux | grep kinsing

sudo kil -9 PID 1234567 我的定时任务⾥倒是没发现有什么问题 不过可以检查⼀下 crontab -l sudo crontab -l 查看rot账户定时任务 crontab -e 删除定时任务

12 还有/etc/rc.local以及/etc/init.d都检查⼀遍 最后建议您修改腾讯云服务器密码，做好安全。

netstat -ltnp命令查看奇怪的监听端⼝，杀掉该进程

ls -l /proc/30902/exe

perf top -s comm,pid,symbol 查看进程 ls -l /proc/30902/exe 查看病毒所在⽬录

上⾯的步骤多重复⼏遍

