整个环境使⽤rot安装：

⼀、配置安装环境

- 1.解压缩libevent-1.4.14b-stable.tar.gz 并且重命名为libevent

./configure-prefix=/usr/local/libevent make make instal ln -s /usr/local/libevent/lib/libevent-2.0.so.5 /usr/lib64/libevent-2.0.so.5

- 2.解压缩pcre-8.20.tar.gz 并重命名为pcre

./configure make make instal

- 3.安装zlib和opensl yum instal zlib* 解压缩opensl-1.0.0d.tar.gz 并且重命名为opensl

./config-prefix=/usr/local/opensl make make instal

- 4.解压缩yamdi-1.4.tar.gz并且重命名为yamdi（拖动模块） make make instal
- 5.mkdir -p /usr/local/nginx/html/flv_file
- 6.解压缩Nginx-aceskey-2.0.3.tar.gz并重命名为nginx-aceskey（防盗链） 编辑config⽂件, 把"$HTP_ACESKEY_MODULE"替换成"ngx_htp_aceskey_module" ⼆、安装nginx


- 1.解压缩nginx-1.0.10.tar.gz 并且重命名为nginx


./configure-user=rot -group=rot -prefix=/usr/local/nginx-with-htp_realip_module -withhtp_stub_status_module-with-htp_sl_module-with-opensl=/rot/opensl -withhtp_sub_module-with-md5=/usr/lib-with-sha1=/usr/lib-with-htp_gzip_static_module-withhtp_flv_module-with-c=gc-with-c-opt='-O3' -ad-module=/rot/nginx-aceskey make make instal cd /usr/local/nginx/conf/ vi nginx.conf

=

=

userrot rot;#修改过 worker_proceses 8;#修改过 #eror_log logs/eror.log; #eror_log logs/eror.log notice; #eror_log logs/eror.log info; pid logs/nginx.pid;#去掉注释 events {

use epol;#添加 worker_conections 6535;#修改过

} htp {

include mime.types; default_type aplication/octet-stream; log_formatmain '$remote_adr - $remote_user [$time_local] "$request" '

'$status $body_bytes_sent "$htp_referer" ' '"$htp_user_agent" "$htp_x_forwarded_for"';#去掉注释

aces_log logs/aces.log main;#去掉注释 sendfile on; tcp_nopush on;#去掉注释 #kepalive_timeout0; kepalive_timeout65; gzip on;#去掉注释 server {

listen 80; server_name 192.168.137.41; charset utf-8;#修改过 limit_rate_after 5m;#添加 limit_rate 512k;#添加 aces_log logs/host.aces.log main;#去掉注释 #加⼊视频⽀持『 location / {#静态分离

rot /usr/local/nginx/html/flv_file/;#修改 index index.html index.htm;

} location ~\.flv{ aceskey on; aceskey_hashmethod md5;

aceskey_arg "key"; aceskey_signature "123$remote_adr"; flv;

}#』

eror_page50 502 503 504 /50x.html; location = /50x.html {

rot html; }

} }

=

=

- 2.启动测试视频模块 将视频和播放器放⼊/usr/local/nginx/html/flv_file yamdi -i a.flv -o b.flv


./nginx -t

./nginx -s stop

./nginx 测试防盗链

htp:/192.168.137.41/jw.swf?type=htp&file=b.flv?key= x

其中 x本例中是123和客户端ip的md5码 cd /usr/local/nginx/conf/ vi nginx.conf（本次加⼊tomcat负载均衡配置,为了保证之前的配置，顺便配置了动静分离）

=

= userrot rot;#修改过，哪个⽤户启动nigix worker_proceses 8;#修改过,线程数 #eror_log logs/eror.log; #eror_log logs/eror.log notice; #eror_log logs/eror.log info; pid logs/nginx.pid;#去掉注释，配置pid⽬录 events {

use epol;#添加，多路复⽤：epol，并发最快 worker_conections 6535;#修改过，最⼤连接数

} htp {

include mime.types;#

default_type aplication/octet-stream; log_formatmain '$remote_adr - $remote_user [$time_local] "$request" '

'$status $body_bytes_sent "$htp_referer" ' '"$htp_user_agent" "$htp_x_forwarded_for"';#去掉注释

aces_log logs/aces.log main;#去掉注释 sendfile on; tcp_nopush on;#去掉注释，不发⼼跳 #kepalive_timeout0;#⻓连接时常 kepalive_timeout65; gzip on;#去掉注释 #[

upstream 192.168.137.41 {#vip，nigix

- server 192.168.137.42 8080; #tomcat
- server 192.168.137.43 8080;


} #] server { listen 80; server_name 192.168.137.41; charset utf-8;#修改过 limit_rate_after 5m;#添加 limit_rate 512k;#添加 aces_log logs/host.aces.log main;#去掉注释 location ~ .*.(jsp|do)$ {#动态分离：动

rot html;#修改 index index.html index.htm; proxy_pas proxy_set_headerX-Real-IP $remote_adr;

htp:/192.168.137.41;

} location / {#动态分离：动

rot html;#修改 index index.html index.htm; proxy_pas proxy_set_headerX-Real-IP $remote_adr;

htp:/192.168.137.41;

} #加⼊视频⽀持『 location ~ .*.(gif|jpg|jpeg|png|bmp|swf|flv|html|htm|cs|js|xml)$ {#静态分离

rot /usr/local/nginx/html/flv_file/;#修改，静态资源位置

index index.html index.htm; location ~\.flv{

aceskey on; aceskey_hashmethod md5; aceskey_arg "key"; aceskey_signature "123$remote_adr"; flv;

}

}#』 eror_page50 502 503 504 /50x.html; location = /50x.html {

rot html; }

} }

=

= ⼆、安装varnish（另⼀个独⽴的服务器）

- 1.解压缩pcre-8.20.tar.gz 并重命名为pcre

./configure-prefix=/usr/local/pcre make make instal

- 2.解压缩varnish-3.0.2.tar.gz并重命名为varnish 配置环境变量PKG_CONFIG_PATH=/usr/local/pcre/lib/pkgconfig/

./configure-prefix=/usr/local/varnish make make instal

- 3.vi /usr/local/varnish/etc/varnish/default.vcl


=

= backend default {#nginx的链接代理

.host = "192.168.137.41";

.port = "80";

.conect_timeout=20s; }

acl purge {#允许三个来源IP通过PURGE⽅法清除缓存 "localhost";

"127.0.0.1"; "192.168.137.41"/24;

} sub vcl_recv {

if (req.request = "PURGE") { if (!client.ip ~ purge) { eror 405 "Not alowed.";

} return (l okup);

} if (req.htp.host ~ "^192.168.137.47") {#Varnish对域名为192.168.137.47的请求进⾏处理，⾮

192.168.137.47域名的请求则返回"caoqing Cache Server" set req.backend = default;

if (req.request != "GET" & req.request != "HEAD") {#Varnish对HTP协议中的GET、HEAD请 求进⾏缓存，对POST请求透过，让其直接访问后端Web服务器

return (pipe); }else{

return (l okup); } }else{ eror 404 "caoqing Cache Server"; return (l okup);

}

} sub vcl_hit {

if (req.request = "PURGE") { set obj.tl = 0s; eror 20 "Purged.";

}

} sub vcl_mis {

if (req.request = "PURGE") { eror 404 "Not in cache."; }

}

=

=

- 4.启动varnish cd /usr/local/varnish/sbin/

./varnishd -f /usr/local/varnish/etc/varnish/default.vcl -s file,/var/varnish_cache,1G -T 192.168.137.47 2 0 -a 0.0.0.0 80

- 5.写⼊⽇志 varnishncsa -w /usr/local/varnish/logs/varnish.log &
- 6.验证 访问192.168.137.47(varnish的服务器)会得到nginx访问tomcat负载后的数据 三、安装rsync服务端（独⽴机器作为服务器，⼀般是hadop可以访问到的服务器）


- 1.yum instal rsync* yum instal xinetd
- 2.配置rsync服务 /rsync服务需要三个⽂件 /rsyncd.conf rsync服务的配置⽂件 /rsyncd.secrets rsync服务的⽤户密码保存⽂件 ⽤户必须为服务器上存在的⽤户 /rsyncd.motd rsync服务的登陆提示信息


cd /etc touch rsyncd.conf touch rsyncd.secrets touch rsyncd.motd chmod 060 rsyncd.secrets vi /etc/rsyncd.conf

=

pid file=/var/run/rsyncd.pid #rsync服务的pid存放⽂件位置 port=873 #端⼝号 adres=192.168.137.48 #rsync服务所在地址 uid=rot #⽤户 gid=rot #⽤户组 usechrot=yes #chrot设定 read only=no #是否只读 hosts alow=* #允许访问的ip hosts deny=192.168.137.1 #禁⽌访问的ip max conections=5 #最⼤连接数 motd file=/etc/rsyncd.motd #提示信息⽂件所在位置 log file=/var/log/rsyncd.log #⽇志⽂件所在位置 log format=%t %a %m %f%b #⽇志⽂件格式 syslog facility=local3

timeout=30 #连接超时时间 secrets file = /etc/rsyncd.secrets #密码所在⽂件 [backup] path = /test auth users=rot lsit=true ignore erors secrets file=/etc/rsyncd.secrets

=

=

/为rsync⽤户指定密码 vi /etc/rsyncd.secrets rot: 1 vi /etc/xinetd.d/rsync

/disable = yes 改成 disable = no vi /etc/ld.so.conf mkdir -p backup

- 3.启动rsync服务 service xinetd start rsync-daemon-config=/etc/rsyncd.conf 四.安装rsync客户端(在nginx机器上配置即可，最好配置上 sh免密码登录)


- 1.yum instal rsync
- 2.rsync-list-only rot@192.168.137.48:backup #查看rsync设置的backup⽬录中的⽂件信息
- 3.同步⽂件 rsync -azuvP /usr/local/nginx/logs rot@192.168.137.48:backup 如果想要定期执⾏ crontab -e 20 0 * * * rsync -azuvP /usr/local/nginx/logs rot@192.168.137.48:backup > o.txt 2>&1


