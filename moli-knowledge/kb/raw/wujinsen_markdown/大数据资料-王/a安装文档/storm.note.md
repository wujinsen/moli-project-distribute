# 一、Storm集群安装部署

- 1、Storm下载地址 https://dl.dropboxusercontent.com/s/dj86w8ojecgsam7/storm-0.9.0.1.zip

- 2、Storm集群部署

- 3、编写启动脚本


⽬前Storm部署三个集群，只需在主控节点（任选⼀个作为主控节点）启动nimbus及ui，所有节点都 需要启动supervisor和logviewer。步骤如下：

- （1）解压storm-0.9.0.1.zip；
- （2）修改conf/storm.yaml⽂件⽰例如下：


#配置storm使⽤的zookeeper集群地址 storm.zookeeper.servers:

- - "192.168.20.101"
- - "192.168.20.103"
- - "192.168.20.104" #配置zookeeper端⼜号 storm.zookeeper.port: 2181 #⽤于存储storm少量状态⽬录，需要提前创建该⽬录并给⾜够的访问权限 storm.local.dir: "/home/storm/storm-0.9.0.1/mnt/storm" #Storm主控节点地址，storm集群中只有⼀个主控节点 nimbus.host: "192.168.20.104" #控制台端⼜ ui.port: 8080 #worker端⼜ supervisor.slots.ports:
- - 6700
- - 6701
- - 6702
- - 6703 #⽇志浏览端⼜ logviewer.port: 8000 #配置netty storm.messaging.transport: "backtype.storm.messaging.netty.Context" storm.messaging.netty.buffer_size: 5242880 storm.messaging.netty.max_retries: 100 storm.messaging.netty.max_wait_ms: 1000 storm.messaging.netty.min_wait_ms: 100


需要分别对Storm的nimbus和supervisor编写启动、停⽌和重启脚本（共六个脚本），分别命名为 start.sh、stop.sh、restart.sh，并在start.sh中将nimbus和supervisor启动的进程号分别写⼊⽂件中。 Storm各组件的启动都是通过bin⽂件夹下的storm进⾏的，将bin/storm写到环境变量中⽅便启动。

- （1）nimbus启动、停⽌、重启脚本编写： nimbus启动命令：nohup storm nimbus >/dev/null 2>&1 & 在bin中新建⽂件夹命名为nimbus，并在nimbus中新建start.sh、stop.sh和restart.sh⽂件，并新建 nimbus.pid⽂件（记录nimbus启动的进程号）。 nimbus启动脚本start.sh：将上⾯nimbus启动命令写⼊到start.sh中，并等待nimbus启动完毕后将nimbus 的进程号写⼊nimbus.pid中（如果不等待nimbus启动完毕，记录的nimbus进程号可能不正确）。 nimbus的停⽌和重启脚本没有特殊说明。
- （2）supervisor启动、停⽌、重启脚本编写： supervisor启动命令：nohup storm supervisor >/dev/null 2>&1 & 在bin中新建⽂件夹命名为supervisor，在supervisor中新建start.sh、stop.sh和restart.sh⽂件，并新建 supervisor.pid⽂件（记录supervisor启动的进程号）。 supervisor启动脚本start.sh：将上⾯supervisor启动命令写⼊到start.sh中，并等待supervisor启动完毕后将 supervisor的进程号写⼊supervisor.pid中（如果不等待supervisor启动完毕，记录的supervisor进程号可能 不正确）。 supervisor的停⽌和重启脚本没有特殊说明。


- 4、启动storm各组件


（1）在storm集群主控节点上启动nimbus：执⾏3中的nimbus下的start.sh启动； （2）在storm集群各个节点上启动supervisor：执⾏3中supervisor下的start.sh启动； （3）在storm集群主控节点上启动ui：执⾏命令nohup storm ui >/dev/null 2>&1 &； （4）在storm集群各个节点上启动logviewer：执⾏命令nohup storm logviewer >/dev/null 2>&1 &。

# 二、Storm运维

Storm的nimbus和supervisor都是快速失败和⽆状态的，要求down掉后能够⾃动重启。选⽤monit对 nimbus和supervisor进⾏监控和⾃动重启。

- 1、monit下载地址 http://mmonit.com/monit/dist/binary/5.8.1/monit-5.8.1-linux-x64.tar.gz

- 2、monit安装配置


monit的⼀个缺点是可以监控⾃⼰所在服务器上的服务，监控远程服务器上的服务存在困难。因此需要 在部署Storm的服务器上分别部署monit进⾏监控（即有三个storm集群，需要在各个storm部署的服务器 上分别部署monit）。

- （1）解压monit-5.8.1-linux-x64.tar.gz；


- （2）修改conf/monitrc⽂件⽰例如下： #设置检测间隔30s set daemon 30 #设置monit⽇志路径 set logﬁle /home/storm/monit-5.8.1/log/monit.log #设置monit报警邮箱服务器 set mailserver mail.whaty.com USERNAME "service@webtrn.cn" PASSWORD "serviceyanfa123"


with timeout 15 seconds #设置发送的邮件格式 set mail-format {

from: service@webtrn.cn subject: monit alert -- $EVENT $SERVICE message: $EVENT Service $SERVICE

Date: $DATE Action: $ACTION Host: $HOST Description: $DESCRIPTION

#设置报警邮件收件⼈ set alert #设置monit内置http，访问监控页⾯ set httpd port 2812 and #端⼜号

houbailing@whaty.com

use address 192.168.20.104 #访问ip allow 0.0.0.0/0.0.0.0 #允许任意机器访问

allow admin:monit #访问监控页⾯的⽤户名和密码 #设置监控storm的nimbus和supervisor，以下路径均为⼀中3启动脚本和pid⽂ #件的路径 check process nimbus with pidﬁle /home/storm/storm-0.9.0.1/bin/nimbus/nimbus.pid

start program = "/home/storm/storm-0.9.0.1/bin/nimbus/start.sh" stop program = "/home/storm/storm-0.9.0.1/bin/nimbus/stop.sh" restart program = "/home/storm/storm-0.9.0.1/bin/nimbus/restart.sh" if changed pid then alert

check process supervisor with pidﬁle /home/storm/storm-0.9.0.1/bin/supervisor/ supervisor.pid

start program = "/home/storm/storm-0.9.0.1/bin/supervisor/start.sh" stop program = "/home/storm/storm-0.9.0.1/bin/supervisor/stop.sh" restart program = "/home/storm/storm-0.9.0.1/bin/supervisor/restart.sh" if changed pid then alert

- （3）复制monitrc 将conf⽂件夹下的monitrc复制到/etc/下，路径为/etc/monitrc。


## 3、monit监控页⾯访问

如果使⽤2的配置，在浏览器中输⼊192.168.20.104:2812进⾏访问。

