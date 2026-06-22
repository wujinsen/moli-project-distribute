Redis现在在业务中应⽤已经很⼴泛了，但是如何监控redis，实时的观察redis的性能，却很少的提 及，现在常⻅的监控⽅案基本上都是使⽤redis⾃带的info命令和monitor命令获取相关信息，然后提取 出来显示。 测试环境：

redis版本:2.4.17 IP 10.20. 1.18

- 1 redis-faina ⼀个使⽤redis⾃带命令monitor的输出结果做分析的python脚本，在命令⾏下使⽤，可以做实时分析


使⽤。 官⽹： 下载试⽤：

htps:/github.com/Instagram/redis-faina

cd /opt/test git clone https://github.com/Instagram/redis-faina.git cd redis-faina/ redis-cli -p 6379 MONITOR | head -n 100 | ./redis-faina.py --redis-version=2.4

测试结果如下：

![image 1](<Redis监控方案.note_images/imageFile1.png>)

可以看到⼀些实时的数据，并且有⼀定的统计数据，可以作为⼀个命令⾏⼯具使⽤。推荐使⽤，不过 redis版本要⼤于2.4。

- 2 redis-live ⼀个⽤来监控redis实例，分析查询语句并且有web界⾯的监控⼯具，python编写。 官⽹： 下载试⽤：


htps:/github.com/nkrode/RedisLive

htp:/ w.nkrode.com/article/real-time-dashboard-for-redis

运⾏环境依赖包安装： redis-live安装：

cd /root git clone https://github.com/nkrode/RedisLive.git cd RedisLive/src ###修改redis-live.conf⽂件 {

"RedisServers": [

{

"server": "10.20.111.188", "port" : 6379

}

],

"DataStoreType" : "redis",

"RedisStatsServer": {

"server" : "10.20.111.188", "port" : 6380

},

"SqliteStatsStore" : {

"path": "to your sql lite file" }

} ###修改完毕 ###启动监控服务，每30秒监控⼀次

./redis-monitor.py --duration=30 ###再次开启⼀个终端，进⼊/root/RedisLive/src⽬录，启动web服务

./redis-live.py

在浏览器输⼊： 即可看到下图：

htp:/10.20. 1.18  8/index.html

![image 2](<Redis监控方案.note_images/imageFile2.png>)

⼀个web界⾯，可以同时监控多个redis实例，做集中监控⽐较好。

- 3 redis-stat ⼀个⽤ruby写成的监控redis的程序，基于info命令获取信息，⽽不是通过monitor获取信息，性能应


该⽐monitor要好。 官⽹： 运⾏环境安装：

htps:/github.com/junegun/redis-stat

apt-get install ruby apt-get install rubygems

redis-stat安装：

cd /root git clone https://github.com/junegunn/redis-stat.git cd /root/redis-stat/bin ###./redis-stat --help 可以看到使⽤帮助

./redis-stat 1

下⾯看看redis-stat的具体⽤法

usage: redis-stat [HOST[:PORT] ...] [INTERVAL [COUNT]]

- -a, --auth=PASSWORD Password

- -v, --verbose Show more info


--style=STYLE Output style: unicode|ascii

--no-color Suppress ANSI color codes

--csv=OUTPUT_CSV_FILE_PATH Save the result in CSV format

--server[=PORT] Launch redis-stat web server (default port: 63790)

--daemon Daemonize redis-stat. Must be used with --server option.

--version Show version

--help Show this message

redis-stat命令⾏模式：

redis-stat redis-stat 1 redis-stat 1 10 redis-stat --verbose redis-stat localhost:6380 1 10 redis-stat localhost localhost:6380 localhost:6381 5 redis-stat localhost localhost:6380 1 10 --csv=/tmp/output.csv --verbose

redis-stat web模式：

redis-stat --server redis-stat --verbose --server=8080 5 redis-stat --server --daemon

效果如下：

![image 3](<Redis监控方案.note_images/imageFile3.png>)

运⾏web模式

cd /root/redis-stat/bin

./redis-stat --server=8080 5 --daemon

在浏览器输⼊： htp:/10.20. 1.18 8080/ 结果如下：

![image 4](<Redis监控方案.note_images/imageFile4.png>)

不错的⼯具，既有命令⾏⼜有web界⾯，可以放到后台运⾏，数据⽐redis-live感觉直观 ，ruby开发 的，唯⼀的缺点是如果同时监控多个redis实例，不能单独显示每⼀个实例的数据信息，貌似是总和。

- 4 redis-monitor ⼀个国⼈⽤java写的，官⽹的是在win下编译的，看着不错，不过我在linux下没调试起来 . 官⽹：htps:/github.com/litiebiao2012/redis-monitor


个⼈感觉，做集中监控可以使⽤redis-live，在命令⾏使⽤可以使⽤redis-stat，也可以根据⾃⼰的情 况⾃⾏编写，总之就是根据info和monitor命令获取并展示信息。

