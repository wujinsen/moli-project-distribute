⾸先在远程主机创建⼀个安全策略⽂件

- 1 grant codebase "file:${java.home}/../lib/tools.jar" {

- 2 permission java.security.AllPermission;

- 3 };


保存为⽂件：/home/hadoop/apps/jdk1.7.0_51/bin/jstatd.all.policy

然后在远程启动

./jstatd -J-Djava.security.policy=/home/hadoop/apps/jdk1.7.0_51/bin/jstatd.all.policy -J-Djava.rmi.server.hostname=hdp-node-01然后在本 地启动java visualvm⼯具创建远程主机即可

