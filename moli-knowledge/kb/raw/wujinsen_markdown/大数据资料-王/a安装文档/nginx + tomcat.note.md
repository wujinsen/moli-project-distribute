- 1、安装依赖包yum -y instal pcre-devel yum -y instal opensl-devel yum -y instal gc

- 2、安装nginx： ⼀个nginx抗5万 tar -xzvf Nignx-0.8.31.tar.gz 进⼊解压后的⽬录，指定安装路径 不指定prefix,则可执⾏⽂件默认放在/usr /local/bin,库⽂件默认放在/usr/local/lib,配置 ⽂件默认放在/usr/local/etc

./

configure-prefix=/usr/local/nginx-conf-path=/usr/local/nginx/nginx.conf make make instal 启动 /usr/local/nginx/sbin/nginx 。 查看

试试， welcome Nginx，就安装ok了。

- 3、安装jdk(⽂件在百度云/资料/JDK-LINUX) su - rot 切换成rot⽤户 sudo -i 不需要密码直接切换成rot


htp:/127.0.0.1

- 1）.进⼊usr⽬录 cd /usr
- 2）.在usr⽬录下建⽴java安装⽬录 mkdir java
- 3）.将jdk-6u24-linux-i586.bin拷⻉到java⽬录下 cp /home/itcast/Desktop/jdk-6u24-linux-i586.bin /usr/java
- 4）.安装jdk cd /usr/java 如果出现/lib/ld-linux.so.2: bad ELF interpreter: No such file or directory，安装下glic即可

yum instal glibc.i686

./jdk-6u24-linux-i586.bin‘ʼ

- 5）.安装完毕为他建⽴⼀个链接以节省⽬录⻓度 ln -s /usr/java/jdk1.6.0_24/ /usr/jdk 6).编辑配置⽂件 vim /etc/profile 配置环境变量：添加内容 vi /etc/profile export JAVA_HOME=/opt/jdk


export PATH=$PATH:$JAVA_HOME/bin export CLASPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tols.jar export JAVA_HOME PATH CLASPATH 执⾏下命令（source命令也称为“点命令”，也就是⼀个点符号（.）。source命令通常⽤于重新执⾏刚 修改的初始化⽂件，使之⽴即⽣效，⽽不必注销并重新登录。） source /etc/profile

- 4、安装tomcat 解压： tar -zxvf apache-tomcat-6.0.37.tar.gz 重命名：

- mvapache-tomcat-6.0.37 tomcat1 解压： tar -zxvf apache-tomcat-6.0.37.tar.gz 重命名：

- mvapache-tomcat-6.0.37 tomcat2 修改配置⽂件： vi /usr/local/tomcat/tomcat2/conf/server.xml 将tomcat2的端⼝号修改 805-》806 8080-》8081 843-》8 4 809-》8010


- 5、nginx+tomcat tomcat配置


- tomcat1（192.168.56. 9 8080）

- tomcat2（192.168.56. 9 8081）


nginx安装在192.168.56. 9。7、其次，配置nginx /usr/local/nginx/ngin

upstream redisxycom {

- server 192.168.56. 9 8080;

- server 192.168.56. 9 8081;


}

log_format w_xy_com '$remote_adr - $remote_user [$time_local] $request '

'"$status" $body_bytes_sent "$htp_referer"'

'"$htp_user_agent" "$htp_x_forwarded_for"';

server

{

listen 80;

server_name redis.xy.com;

location / {

proxy_pas htp:/redisxycom;

proxy_set_header Host $host;

proxy_set_header X-Real-IP $remote_adr;

proxy_set_header X-Forwarded-For$proxy_ad_x_forwarded_for;

}

aces_log /data/base_files/logs/redis.xy.log w_xy_com;

}

- 8、创建logs所需要的⽂件夹 /data/base_files/logs/

- 9、将你的应⽤放到两个tomcat中，并依次启动tomcat、nginx。

- 10、修改hosts，加⼊ 192.168.56.99 redis.xxy.com


1、访问htp:/redis.xy.com

