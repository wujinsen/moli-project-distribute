# ⼀、安装编译⼯具及库⽂件

yum -y install make zlib zlib-devel gcc-c++ libtool openssl openssl-devel

# ⼀.⾸先要安装 PCRE

⽤户：develop ⽬录: /home/develop

- 1.下载解压:

tar -xvf pcre-8.35.tar.gz

- 2.编译安装 cd /home/develop/software/pcre-8.35


./conﬁgure --preﬁx=/home/develop/webserver/pcre-8.35

1 make && make install

# 安装 Nginx

下载解压: tar -xvf nginx-1.6.2.tar.gz cd /home/develop/software/nginx-1.6.2

./conﬁgure --preﬁx=/home/develop/webserver/nginx --with-http_stub_status_module -with-http_ssl_module --with-pcre=/home/develop/software/pcre-8.35

1 make && make install

注: --with-pcre=xxx 是源码路径，不是pcre安装后的路径

./conﬁgure --preﬁx=/home/xinwu/webserver/pcre-8.35

./conﬁgure --preﬁx=/home/xinwu/webserver/nginx --with-http_stub_status_module -with-http_ssl_module --with-pcre=/opt/pcre-8.35

