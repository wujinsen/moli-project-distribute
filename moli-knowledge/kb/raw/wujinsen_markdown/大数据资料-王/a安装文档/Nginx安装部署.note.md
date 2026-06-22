⼀般我们都需要先装pcre, zlib，前者为了重写rewrite，后者为了gzip压缩。

⽤rot⽤户

- 1.选定源码⽬录 [ nginx]# pwd /usr/local/nginx [ nginx]#l 总⽤量 2860

- drwxr-xr-x. 9 rot rot 4096 9⽉ 30 17 08 apache-tomcat-6.0.35-1
- drwxr-xr-x. 9 rot rot 4096 9⽉ 30 17 09 apache-tomcat-6.0.35-2


- -rw-r-r-. 1 rot rot790523 1⽉ 8 2014 nginx-1.5.8.tar.gz
- -rw-r-r-. 1 rot rot 0 10⽉ 10 08 52 opensl-1.0.1c.tar.gz
- -rw-r-r-. 1 rot rot 156329 10⽉ 10 08  5 pcre-8.21.tar.gz
- -rw-r-r-. 1 rot rot571091 10⽉ 10 08 52 zlib-1.2.8.tar.gz


- 2.安装PCRE库 cd /usr/local/nginx tar -zxvf pcre-8.21.tar.gz cd pcre-8.21

./configure make make instal

- 3.安装zlib库 cd /usr/local/ wget tar -zxvf zlib-1.2.8.tar.gz cd zlib-1.2.8

./configure make make instal

- 4.安装 sl


rot@hadop1client

rot@hadop1client

htp:/zlib.net/zlib-1.2.8.tar.gz

cd /usr/local/ wget tar -zxvf opensl-1.0.1c.tar.gz

htp:/ w.opensl.org/source/opensl-1.0.1c.tar.gz

./config make

make instal

- 5.安装nginx

Nginx ⼀般有两个版本，分别是稳定版和开发版，您可以根据您的⽬的来选择这两个版本的其中⼀个， 下⾯是把 Nginx 安装到 /usr/local/nginx ⽬录下的详细步骤：

cd /usr/local/ wget tar -zxvf nginx-1.2.8.tar.gz cd nginx-1.2.8

./configure --prefix=/usr/local/nginx make make instal

- -with-pcre=/usr/src/pcre-8.21 指的是pcre-8.21 的源码路径。
- -with-zlib=/usr/src/zlib-1.2.7 指的是zlib-1.2.7 的源码路径。


- 6.启动 确保系统的 80 端⼜没被其他程序占⽤， /usr/local/nginx/sbin/nginx

检查是否启动成功： netstat -ano|grep 80 有结果输⼊说明启动成功

打开浏览器访问此机器的 IP，如果浏览器出现 Welcome to nginx! 则表⽰ Nginx 已经安装并运⾏成 功。

- 7.重启 /usr/local/nginx/sbin/nginx –s reload
- 8.修改配置⽂件 cd /usr/local/nginx/conf vi nginx.conf
- 9.常⽤配置


htp:/nginx.org/download/nginx-1.2.8.tar.gz

#nginx运⾏⽤户和组 user w w; #启动进程,通常设置成和cpu的数量相等 worker_proceses 4;

#全局错误⽇志及PID⽂件 pid /var/run/nginx.pid; error_log /var/log/nginx/error.log;

events {

#epoll是多路复⽤IO(I/O Multiplexing)中的⼀种⽅式,但是仅⽤于linux2.6以上内核,可以⼤⼤ 提⾼nginx的性能 use epol;

#单个后台worker process进程的最⼤并发链接数 worker_conections 10240;

} #设定http服务器，利⽤它的反向代理功能提供负载均衡⽀持 htp {

include mime.types;

default_type aplication/octet-stream;

eror_page 40 403 50 502 503 504 /50x.html;

index index.html index.shtml

autoindex of;

fastcgi_intercept_erors on;

sendfile on;

# These are god default values. tcp_nopush on; tcp_nodelay of;

# output compresion saves bandwidth

gzip of;

#gzip_static on; #gzip_min_length 1k; gzip_htp_version 1.0; gzip_comp_level 2; gzip_bufers 4 16k; gzip_proxied any; gzip_disable "MSIE [1-6]\."; gzip_types text/plain text/html text/cs aplication/x-javascript aplication/xml

aplication/xml+rs text/javascript; #gzip_vary on;

server_name_in_redirect of;

#设定负载均衡的服务器列表 upstream portals {

- server 172.16.68.134 8082 max_fails=2 fail_timeout=30s;
- server 172.16.68.135 8082 max_fails=2 fail_timeout=30s; server 172.16.68.136 8082 max_fails=2 fail_timeout=30s;


server 172.16.68.137 8082 max_fails=2 fail_timeout=30s; }

#upstream overflow {

# server 10.248.6.34 8090 max_fails=2 fail_timeout=30s; # server 10.248.6.45 8080 max_fails=2 fail_timeout=30s;

#}

server {

#侦听8080端⼜ listen 8080; server_name 127.0.0.1;

#403、404页⾯重定向地址

- eror_page 403 =
- eror_page 404 = proxy_conect_timeout 90; proxy_send_timeout 180;


- htp:/ w.e10.cn/ebiz/other/217/403.html;
- htp:/ w.e10.cn/ebiz/other/218/404.html;


proxy_read_timeout 180;

proxy_bufer_size 64k; proxy_bufers 4 128k; proxy_busy_bufers_size 128k;

client_header_bufer_size 16k; large_client_header_bufers 4 64k;

#proxy_send_timeout 3m; #proxy_read_timeout 3m; #proxy_bufer_size 4k; #proxy_bufers 4 32k;

proxy_set_header Host $htp_host; proxy_max_temp_file_size 0; #proxy_hide_header Set-Cokie;

# if ($host != ' w.e10.cn' ) { # rewrite ^/(.*)$ $1 permanent; # }

htp:/ w.e10.cn/

location / {

deny al; }

location ~ ^/resource/res/img/blue/space.gif {

proxy_pas }

htp:/tecopera;

location = / {

rewrite ^(.*)$ /ebiz/event/517.html last; }

location = /ebiz/event/517.html { ad_header Vary Acept-Encoding; rot /data/web/html; expires 10m;

}

location = /check.html { rot /usr/local/nginx/html/; aces_log of;

}

location = /50x.html { rot /usr/local/nginx/html/; expires 1m; aces_log of;

}

location = /index.html { ad_header Vary Acept-Encoding;

#定义服务器的默认⽹站根⽬录位置 rot /data/web/html/ebiz; expires 10m;

}

#定义反向代理访问名称 location ~ ^/ecps-portal/* { # expires 10m;

#重定向集群名称 proxy_pas #proxy_pas

htp:/portals; htp:/172.16.68.134 8082;

}

location ~ ^/fetionLogin/* {

# expires 10m; proxy_pas #proxy_pas

htp:/portals; htp:/172.16.68.134 8082;

}

#location ~ ^/busines/* { ## expires 10m; # proxy_pas # #proxy_pas #}

htp:/172.16.68.132 808; htp:/172.16.68.134 8082;

location ~ ^/rsmanager/* { expires 10m; rot /data/web/; #proxy_pas

htp:/rsm;

} #定义nginx处理的页⾯后缀 location ~* (.*)\.(jpg|gif|htm|html|png|js|cs)$ {

rot /data/web/html/; #页⾯缓存时间为10分钟

expires 10m; }

#设定查看Nginx状态的地址

location ~* ^/NginxStatus/ { stub_status on; aces_log of; alow 10.1.252.126; alow 10.248.6.49; alow 127.0.0.1; deny al;

} # eror_page405 =20 @405; # location @405 # { # proxy_pas # }

htp:/10.248.6.45 8080;

aces_log /data/logs/nginx/aces.log combined; eror_log/data/logs/nginx/eror.log;

} server { listen 8082;

server_name _;

location = /check.html { rot /usr/local/nginx/html/; aces_log of;

}

}

server { listen 808; server_name _; location ~ ^/* { rot /data/web/b2bhtml/; aces_log of;

} }

server { listen 9082; server_name _;

# location ~ ^/resource/* { # expires 10m;

# rot /data/web/html/; # }

location / { rot /data/web/html/sysMaintain/; if (!-f $request_filename) {

rewrite ^/(.*)$ /sysMaintain.html last; }

}

}

}

