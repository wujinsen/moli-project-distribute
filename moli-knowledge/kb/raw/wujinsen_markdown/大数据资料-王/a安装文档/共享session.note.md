- 1、安装pcreyum instal pcre-devel yum instal opensl-devel

- 2、安装nginx： ⼀个nginx抗5万 tar -xzvf Nignx-0.8.31.tar.gz 进⼊解压后的⽬录

./configure-prefix=/usr/local/nginx make make instal 启动 /usr/local/nginx/sbin/nginx 。 查看

试试， welcome Nginx，就安装ok了。

- 3、安装jdk(⽂件在百度云/资料/JDK-LINUX) su - rot 切换成rot⽤户 sudo -i 不需要密码直接切换成rot


htp:/127.0.0.1

- 1）.进⼊usr⽬录 cd /usr
- 2）.在usr⽬录下建⽴java安装⽬录 mkdir java
- 3）.将jdk-6u24-linux-i586.bin拷⻉到java⽬录下 cp /home/itcast/Desktop/jdk-6u24-linux-i586.bin /usr/java
- 4）.安装jdk cd /usr/java 如果出现/lib/ld-linux.so.2: bad ELF interpreter: No such file or directory，安装下glic即可

yum instal glibc.i686

./jdk-6u24-linux-i586.bin‘ʼ

- 5）.安装完毕为他建⽴⼀个链接以节省⽬录⻓度 ln -s /usr/java/jdk1.6.0_24/ /usr/jdk 6).编辑配置⽂件 vim /etc/profile 配置环境变量：添加内容 vi /etc/profile export JAVA_HOME=/opt/jdk export PATH=$PATH:$JAVA_HOME/bin export CLASPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tols.jar export JAVA_HOME PATH CLASPATH


执⾏下命令（source命令也称为“点命令”，也就是⼀个点符号（.）。source命令通常⽤于重新执⾏刚 修改的初始化⽂件，使之⽴即⽣效，⽽不必注销并重新登录。） source /etc/profile

- 4、安装tomcat

⾸先，是配置tomcat，使其将sesion保存到redis上。有两种⽅法，也是在server.xml或 context.xml中配置，不同的是memcached只需要添加⼀个manager标签，⽽redis需要增加的内 容如下：（注意：valve标签⼀定要在manager前⾯。）

6、context.xml：

- 5、nginx+tomcat+redis (依赖包下载)


- 1).redis配置（192.168.159.131:16300）（v2.8.3）

- 2).tomcat配置

- tomcat1（192.168.159.130:8081）

- tomcat2（192.168.159.130:8082）


- 3).nginx安装在192.168.159.131。


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


<Valve clasName="com.radiadesign.catalina.sesion.RedisSesionHandlerValve" />

<Manager clasName="com.radiadesign.catalina.sesion.RedisSesionManager"

host="192.168.159.131"

port="1630" database="0" maxInactiveInterval="60"/>

- 7、其次，配置nginx，⽤于测试session保持共享。

- 8、最后，将你的应⽤放到两个tomcat中，并依次启动redis、tomcat、nginx。访问你的nginx， 可以发现两个tomcat中的sesion可以保持共享了。


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.


upstream redis.xy.com {

- server 192.168.159.130 8081;

- server 192.168.159.130 8082;


}

log_format w_xy_com '$remote_adr - $remote_user [$time_local] $request '

'"$status" $body_bytes_sent "$htp_referer"'

'"$htp_user_agent" "$htp_x_forwarded_for"';

server

{

listen 80;

server_name redis.xy.com;

location / {

proxy_pas htp:/redis.xy.com;

proxy_set_header Host $host;

proxy_set_header X-Real-IP $remote_adr;

proxy_set_header X-Forwarded-For$proxy_ad_x_forwarded_for;

}

aces_log /data/base_files/logs/redis.xy.log w_xy_com;

}

