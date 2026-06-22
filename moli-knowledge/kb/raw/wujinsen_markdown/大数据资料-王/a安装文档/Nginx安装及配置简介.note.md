Nginx作为⼀个后起之秀，他的迷⼈之处已经让很多⼈都投⼊了他的怀抱。配置简单，实现原理简单。 做⼀个负载平衡的再好不过了。 其原理： 简单介绍⼀下他的安装及配置过程 官⽅⽹站

htp:/wiki.codemongers.com/Main

⼀、依赖的程序

- 1. gzip module requires zlib library
- 2. rewrite module requires pcre library
- 3. sl suport requires opensl library ⼆、安装


./configure make make instal 默认安装的路径是/usr/local/nginx 更多的安装配置

./configure-prefix=/usr/local/nginx

- -with-opensl=/usr/include (启⽤ sl)
- -with-pcre=/usr/include/pcre/ (启⽤正规表达式)
- -with-htp_stub_status_module (安装可以查看nginx状态的程序)
- -with-htp_memcached_module (启⽤memcache缓存)
- -with-htp_rewrite_module (启⽤⽀持url重写)


三、启动及重启 启动：nginx 重启：kil -HUP `cat /usr/local/nginx/logs/nginx.pid` 测试配置⽂件：nginx -t 简单吧，安装，启动都⽐较⽅便。 四、配置⽂件

htp:/wiki.codemongers.com/NginxFulExample

#运⾏⽤户 1usernobody nobody; 2#启动进程 3worker_proceses 5; 4#全局错误⽇志及PID⽂ 件 5eror_log logs/eror.log notice; 6pid logs/nginx.pid; 7#⼯作模式及连接数上限 8events { 9# ⼯作模式有：select(标准模式),pol(标准模式),kqueue(⾼效模式，适⽤ FreBSD 4.1+, OpenBSD 2.9+, NetBSD 2.0 and MacOS X),10#epol(⾼效模式，本例⽤的。适⽤ Linux 2.6+,SuSE 8.2,), #/dev/pol(⾼效模式，适⽤ Solaris 71/ 9+, HP/UX1.2+ (eventport), IRIX 6.5.15+ 和 Tru64 UNIX 5.1A+)1use epol;12worker_ conections 1024;13}14#设定htp服务器，利⽤它的反向代理功能提供负载均衡⽀持15htp {16#设 定mime类型17include conf/mime.types;18default_type aplication/octet-stream;19#设定⽇志格 式 20log_format main '$remote_adr - $remote_user[$time_local] '21 '"$request" $ status $bytes_sent ' 2 '"$htp_referer" "$htp_user_agent" '23 '"$gzip_r atio"';2425log_format download '$remote_adr - $remote_user[$time_local] ' '"$r equest" $status $bytes_sent ' '"$htp_referer" "$htp_user_agent" ' '"$h

tp_range" "$sent_htp_content_range"';26#设定请求缓冲 27client_header_bufer_size 10k;28large_client_header_bufers 4 4k;2930#开启gzip模块，要求 安装gzip 在运⾏./config时要指定 31gzip on;32gzip_min_length 10; 3gzip_bufers 4 8k;34gzip_types text/plain;35output_bu

fers 1 32k;36postpone_output1460;37#设定访问⽇志

38aces_log logs/aces.log main;39client_header_timeout3m;40client_body_timeout 3m;41 send_timeout 3m;42sendfile on;43tcp_nopush on; 4tcp_nodelay on;4 5kepalive_timeout65;4647#设定负载均衡的服务器列表48upstream backserver {49#weigth参数

表示权值，权值越⾼被分配到的⼏率越⼤50#本例是指在同⼀台服务器，多台服务器改变ip即可 51server 127.0.0.1 8081 weight=5;52server 127.0.0.1 8082;53server 127.0.0.1 8083;54} #Deny aces to any host other than ( w).4535.com server { server_name _; #default return 404; } 5#设定虚拟主机，默认为监听80端⼝，改成其他端⼝会出现问题 56server {57listen 80;58server_name test.com w.test.com;59charset utf8;60#设定本虚 拟主机的访问⽇志61aces_log logs/test.com.log main;62#如果访问 /images/*, /js/*, /cs/* 资源， 则直接取本地⽂件，不⽤转发。但如果⽂件较多效果不是太好。 63location ~ ^/(images|js|cs)/ {64rot /usr/local/testweb;65expires 30m; 6}67#对 "/" 启⽤负载 均衡 68location / {69proxy_pas of;71proxy_set_header

htp:/backserver;70proxy_redirect

Host $host;72proxy_set_header X-Real-IP $remote_adr;73proxy_set_header XForwardedFor $proxy_ad_x_forwarded_for;74client_max_body_size 10m;75client_body_bufer_size 128k;

76proxy_conect_timeout90; 7proxy_send_timeout 90;78proxy_read_timeout 90;79proxy_ bufer_size 4k;80proxy_bufers 4 32k;81proxy_busy_bufers_size 64k;82proxy_temp_file_ write_size 64k;}83#设定查看Nginx状态的地址,在运⾏./config 要指定，默认是不安装的。

84location /NginxStatus {85stub_status on;86aces_log on;87auth_basic "N ginxStatus"; 8#是否要通过⽤户名和密码访问，测试时可以不加上。conf/htpaswd ⽂件的内容 ⽤ apache 提供的 htpaswd ⼯具来产⽣即可#auth_basic_user_file conf/htpaswd;89}90}91

⽣活是⽤脚⼀步⼀步⾛出来的 .

评论

# re: Nginx安装及配置简介 回复 更多评论 209-09-30 15 51 by智能 ⼿机中⽂⽹

还是有点问题

# nginx回复 更多评论 201-05-30 02 09 byJ控 奔途

看了半天类似的资料,愣是没找到解决问题的办法,不过,后来找了个视频看了会才让我想到解决办法. 我 的问题出在fastcgi没有启动. 我找到的资料⾥⾯⼤多都没有提及这⼀点,fastcgi如何启动,都只说了怎么 怎么装完就⾏~ 其实很简单,⾸先找到php的安装⽬录 ⽐如/usr/local/php,然后执 ⾏"/usr/local/php/bin/php-cgi -b 9 0"这样就能在所有IP上侦听9 0端⼝(nginx⾥fastcgi默认端⼝)

# re: Nginx安装及配置简介 回复 更多评论 2014-02-12 16  3 bywoshi ney

谢谢,楼主很详细,如果⼤家要看在线视频 nginx的教程视频 在 w. pst.c 上有好多，请关注，也可以 通过录视频 来获取收益

