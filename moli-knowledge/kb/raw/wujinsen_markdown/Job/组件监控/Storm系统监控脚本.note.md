htp:/ w.blogchong.com/post/storm_2014128.html

- 1⽂档说明

- 2相关脚本


写这个⽂档呢，是因为前⼏天在群中有⼈讨论关于nimbus没有⾃动重启机制，如何保证系统挂掉后重 启的问题。刚好有朋友也问到了这个问题，就写了⼏个脚本，顺便写个⽂档说明说明。 顺便还写了个集群免登陆的脚本，及集群ZK操作的脚本。免登陆后再执⾏zk操作脚本，我们就可以在 ⼀台机⼦上启动集群的ZK、停⽌ZK及查看ZK状态等等。 关于nimbus监控脚本，只需要后台让他执⾏就⾏了，他会⾃动检测nimbus是否挂掉，如果挂掉就会重 启他，有点像nimbus守护进程类似的东西，顺便还给他写了monitor监控的log，⽅便查询。

- 2.1 shFreLogin.sh


- 2.1.1脚本说明


该脚本⽤于ip之间免登陆设置，在storm集群中，往往很多时候只有⼀个操作界⾯，需要登录的其他节 点进⾏操作，设置免登陆后，该操作会变的更简单。 该脚本会有⼀个ip.list配置列表。

- 2.1.2上代码

<table>
  <tr>
    <th>1<br>2<br></th>
    <th>192.168.2.240<br><br>192.168.2.241<br><br>192.168.2.242<br></th>
  </tr>
</table>


- 3


Ip.list:

shFreLogin.sh:

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>39<br><br>0 41</th>
    <th>#!/bin/bash<br><br>#author: chongyuanHuang #blog: #QQ group: 191321336 #E-mail: 874450476@qq.com<br><br>function Echo() { parameter=$1 name=$2<br><br>if [ $1 - ne 1 ]; then echo "Usage: $2 ip.list" echo " please input ip.list for free<br><br>login and password next"<br><br>exit 1 fi<br><br>}<br><br>function LocalSsh() { remote=$1 echo "---------------------------------------------<br><br>---------------" echo "Begin to set Local-Remote free login!" ssh -keygen -t rsa -P '' scp /root/. ssh /id_rsa.pub $1:/root/id_rsa.pub ssh $1 'cat /root/id_rsa.pub >> /root/.ssh/author<br><br>ized_keys'<br><br>ssh $1 'chmod 600 /root/.ssh/authorized_keys' echo "Local-Remote free login set Ok!" echo "---------------------------------------------<br><br>---------------" }<br><br>if [ $ # -eq 1 ]; then num=` awk 'END{print NR}' $1` echo 'Set free login ip.list:'<br><br>for ((i=1;i<=$num;i++)); do ip=` cat $1 | sed -n '' $i 'p' ` echo $ip<br><br>LocalSsh $ip done<br><br>else<br><br>Echo $ # $0 fi<br><br>htp:/blog.sina.com.cn/huangchongyuan</th>
  </tr>
</table>


# 2.2 optZk.sh

- 2.2.1脚本说明


该脚本主要是ZK集群操作，配合免登陆设置，可以在⼀台机⼦上进⾏ZK集群的启动，停⽌及查看状态 等操作。该脚本稍微改改就可以⽤于其它集群类型的操作。代码很简单，可以⾃⼰看看。

- 2.2.2上代码


optZk.sh:

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br><br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>19<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br><br><br>29<br><br>0<br>1<br>2<br>3<br>4<br>5<br>6<br><br><br>37</th>
    <th>#!/bin/bash<br><br>#author: chongyuanHuang #blog: #QQ group: 191321336 #E-mail: 874450476@qq.com<br><br>function Echo() { parameter=$1 name=$2<br><br>if [ $parameter - ne 2 ]; then<br><br>echo "Usage: $name ip.list start/stop/s tatus"<br><br>echo " please input ip.list opt" exit 1<br><br>fi }<br><br>function optZk() { ip=$1 opt=$2<br><br>echo "-------------------------------------------<br><br>-----------------" echo "Begin to '$opt' '$ip' ZK:" ssh $1 '/usr/local/zookeeper/bin/zkServer.sh '<br><br>$opt ''<br><br>echo "-------------------------------------------<br><br>-----------------" }<br><br>if [ $ # -eq 2 ]; then num=` awk 'END{print NR}' $1`<br><br>for ((i=1;i<=$num;i++)); do<br><br>ip=` cat $1 | sed n '' $i 'p' `<br><br>echo $ip<br><br>optZk $ip $2 done<br><br>else<br><br>Echo $ # $0 fi<br><br>htp:/blog.sina.com.cn/huangchongyuan</th>
  </tr>
</table>


# 2.3 monitorNimbus.sh

- 2.3.1脚本说明


该脚本⽤于nimbus的监控，只需后台执⾏该脚本，该脚本会⾃动监控nimbus的运⾏，当检测到挂掉以 后，会⾃动重启，并且会把监控结果以log的形式保存下来。

- 2.3.2上代码


MonitorNimbus.sh:

#!/bin/bash

#author: chongyuanHuang #blog: #QQ group: 191321336 #E-mail: 874450476@qq.com

htp:/blog.sina.com.cn/huangchongyuan

function Init() { date =` date +%Y%m%d%H%M%S` echo '' $ date ': Begin monitor nimbus.' > monitorNimbus

.log }

function MonitorNimbus() { for ((;;)); do

num=` ps aux | grep nimbus | grep vgrep | wc -l`

if [ $num - eq 0 ]; then date1=` date +%Y%m%d%H%M%S`

echo '' $date1 ': nimbus is down, and r estarting!' >> monitorNimbus.log

storm nimbus&

fi sleep 60

done

}

Init MonitorNimbus exit 1

