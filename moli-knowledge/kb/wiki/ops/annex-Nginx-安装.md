---
title: Nginx 安装.note（原文插图 annex）
slug: annex-Nginx-安装
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/DevOps/nginx/Nginx 安装.note.md
related: [jenkins-ci入门]
created: 2026-07-05
updated: 2026-07-05
---

# Nginx安装

系统平台：CentOS release 6.6 (Final) 64位。

⼀、安装编译⼯具及库⽂件

yum -y install make zlib zlib-devel gcc-c++ libtool openssl openssl-devel

⼆、⾸先要安装 PCRE

PCRE 作⽤是让 Nginx ⽀持 Rewrite 功能。 1、下载 PCRE 安装包，下载地址：

htp:/downloads.sourceforge.net/project/pcre/pcre/8.35/pcre-8.35.tar.gz

[root@bogon src]# cd /usr/local/src/ [root@bogon src]# wget http://downloads.sourceforge.net/project/pcre/pcre/8.35/pcre-8.35.tar.gz

2[root@bogon、解压安装包:src]# tar zxvf pcre-8.35.tar.gz

- 3、进⼊安装包⽬录 [root@bogon src]# cd pcre-8.35

- 4、编译安装 [root@bogon pcre-8.35]# ./configure [root@bogon pcre-8.35]# make && make install

- 5、查看pcre版本 [root@bogon pcre-8.35]# pcre-config --version


<table>
  <tr>
    <th>![image 1](assets/imageFile1.png)</th>
  </tr>
</table>


## 安装 Nginx

- 1、下载 Nginx，下载地址： [root@bogon src]# cd /usr/local/src/ [root@bogon src]# wget http://nginx.org/download/nginx-1.6.2.tar.gz


htps:/nginx.org/en/download.html

<table>
  <tr>
    <th>![image 2](assets/imageFile2.png)</th>
  </tr>
</table>


- 2、解压安装包 [root@bogon src]# tar zxvf nginx-1.6.2.tar.gz

- 3、进⼊安装包⽬录 [root@bogon src]# cd nginx-1.6.2

- 4、编译安装 [root@bogon nginx-1.6.2]# ./configure --prefix=/usr/local/webserver/nginx --withhttp_stub_status_module --with-http_ssl_module --with-pcre=/opt/pcre-8.35 [root@bogon nginx-1.6.2]# make [root@bogon nginx-1.6.2]# make install

- 5、查看nginx版本 [root@bogon nginx-1.6.2]# /usr/local/webserver/nginx/sbin/nginx -v


<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


到此，nginx安装完成。

# Nginx配置

创建 Nginx 运⾏使⽤的⽤户 w：

[root@bogon conf]# /usr/sbin/groupadd www [root@bogon conf]# /usr/sbin/useradd -g www www

配置nginx.conf ，将/usr/local/webserver/nginx/conf/nginx.conf替换为以下内容

[root@bogon conf]# cat /usr/local/webserver/nginx/conf/nginx.conf

user www www; worker_processes 2; #设置值和CPU核⼼数⼀致 error_log /usr/local/webserver/nginx/logs/nginx_error.log crit; #⽇志位置和⽇志级别 pid /usr/local/webserver/nginx/nginx.pid; #Specifies the value for maximum file descriptors that can be opened by this process. worker_rlimit_nofile 65535; events {

use epoll; worker_connections 65535;

} http {

include mime.types; default_type application/octet-stream; log_format main '$remote_addr - $remote_user [$time_local] "$request" '

'$status $body_bytes_sent "$http_referer" ' '"$http_user_agent" $http_x_forwarded_for';

#charset gb2312;

server_names_hash_bucket_size 128; client_header_buffer_size 32k; large_client_header_buffers 4 32k; client_max_body_size 8m;

sendfile on; tcp_nopush on; keepalive_timeout 60; tcp_nodelay on; fastcgi_connect_timeout 300; fastcgi_send_timeout 300; fastcgi_read_timeout 300; fastcgi_buffer_size 64k; fastcgi_buffers 4 64k; fastcgi_busy_buffers_size 128k; fastcgi_temp_file_write_size 128k; gzip on; gzip_min_length 1k; gzip_buffers 4 16k; gzip_http_version 1.0; gzip_comp_level 2; gzip_types text/plain application/x-javascript text/css application/xml; gzip_vary on;

#limit_zone crawler $binary_remote_addr 10m; #下⾯是server虚拟主机的配置

server {

listen 80;#监听端⼝ server_name localhost;#域名 index index.html index.htm index.php; root /usr/local/webserver/nginx/html;#站点⽬录

location ~ .*\.(php|php5)?$ {

#fastcgi_pass unix:/tmp/php-cgi.sock; fastcgi_pass 127.0.0.1:9000; fastcgi_index index.php; include fastcgi.conf;

} location ~ .*\.(gif|jpg|jpeg|png|bmp|swf|ico)$ {

expires 30d;

# access_log off; } location ~ .*\.(js|css)?$ {

expires 15d;

# access_log off; } access_log off;

}

}

检查配置⽂件nginx.conf的正确性命令：

[root@bogon conf]# /usr/local/webserver/nginx/sbin/nginx -t

<table>
  <tr>
    <th>![image 4](assets/imageFile4.png)</th>
  </tr>
</table>


# 启动 Nginx

Nginx 启动命令如下：

[root@bogon conf]# /usr/local/webserver/nginx/sbin/nginx

<table>
  <tr>
    <th>![image 5](assets/imageFile5.png)</th>
  </tr>
</table>


# 访问站点

从浏览器访问我们配置的站点ip：

<table>
  <tr>
    <th>![image 6](assets/imageFile6.png)</th>
  </tr>
</table>


# Nginx其他命令

以下包含了 Nginx 常⽤的⼏个命令：

/usr/local/webserver/nginx/sbin/nginx -s reload # 重新载⼊配置⽂件

/usr/local/webserver/nginx/sbin/nginx -s reopen # 重启 Nginx /usr/local/webserver/nginx/sbin/nginx -s stop # 停⽌ Nginx
