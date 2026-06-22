- 1、脚本：名字，redis vi/etc/init.d/redis

- 2、把上述代码存为redis,放到/etc/init.d/下⾯ chmod 75 /etc/init.d/redis
- 3、开机启动 chkconfig redis on


<table>
  <tr>
    <th>!/bin/sh chkonfig: 2345 80 90<br><br>#description:auto_run PATH=/usr/local/bin:/sbin:/usr/bin:/bin REDISPORT=6379 EXEC=/usr/local/bin/redis-server REDIS_CLI=/usr/local/bin/redis-cli PIDFILE=/var/run/redis.pid CONF="/usr/local/redis-2.8.19/redis.conf" cae "$1" in<br><br>start) if [ -f $PIDFILE ] then<br><br>echo "$PIDFILE exists, proces is already runing or crashed"<br><br>else echo "Starting Redis server." $EXEC $CONF<br><br>fi if [ "$?"="0" ] then<br><br>echo "Redis is runing." fi<br><br>;<br><br>stop) if [ ! -f $PIDFILE ] then<br><br>echo "$PIDFILE does not exist, proces is not runing"<br><br>else PID=$(cat $PIDFILE) echo "Stoping." $REDIS_CLI -p $REDISPORT SHUTDOWN while [ -x ${PIDFILE} ]<br><br>do echo "Waiting for Redis to shutdown." sl ep 1<br><br>done echo "Redis stoped"<br><br>fi ;<br><br>restart|force-reload)<br><br>op ${0} start<br><br>;<br><br>*) echo "Usage: /etc/init.d/redis {start|stop|restart|force-reload}" >&2 exit 1</th>
  </tr>
</table>


esac

- 4、启动,停⽌redis


- 1. service redis start #或者 /etc/init.d/redis start
- 2. service redis stop#或者 /etc/init.d/redis stop


