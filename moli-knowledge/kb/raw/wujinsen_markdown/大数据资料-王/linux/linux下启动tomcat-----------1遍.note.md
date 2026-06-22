打开终端

cd /java/tomcat

#执⾏

bin/startup.sh #启动tomcat

bin/shutdown.sh #停⽌tomcat

tail -f logs/catalina.out #看tomcat的控制台输出；

#看是否已经有tomcat在运⾏了

ps -ef |grep tomcat#如果有，⽤kil; kil -9 pid #pid 为相应的进程号 例如 ps -ef |grep tomcat 输出如下 sun 514 1 0 10 21 pts/1 0  0 06 /java/jdk/bin/java Djava.util.loging.manager=org.apache.juli.ClasLoaderLogManager Djava.endorsed.dirs=/java/tomcat/comon/endorsed -claspath :/java/tomcat/bin/botstrap.jar:/java/tomcat/bin/comons-loging-api.jar Dcatalina.base=/java/tomcat -Dcatalina.home=/java/tomcat -Djava.io.tmpdir=/java/tomcat/temp org.apache.catalina.startup.Botstrap start则 514 就为进程号 pid = 514 kil -9 514 就可以彻底杀死tomcat

#直接查看指定端⼝的进程pid netstat -anp|grep 9217 #结果为 tcp 0 0:9217 :* LISTEN 26127/java #则26127为9217这个端⼝的tomcat进程的pid,然后就可以kil这个进程 kil -9 26127 #然后再启动tomcat即可

