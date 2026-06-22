⼀、⼀般来说nginx 配置⽂件中对优化⽐较有作⽤的为以下⼏项：

- 1. worker_proceses 8; nginx 进程数，建议按照cpu 数⽬来指定，⼀般为它的倍数 (如,2个四核的cpu计为8)。
- 2. worker_cpu_afinity 01 010 010 01 0 01 0 01 0 01 0 1 0; 为每个进程分配cpu，上例中将8 个进程分配到8 个cpu，当然可以写多个，或者将⼀ 个进程分配到多个cpu。
- 3.worker_rlimit_nofile 6535; 这个指令是指当⼀个nginx 进程打开的最多⽂件描述符数⽬，理论值应该是最多打开⽂ 件数（ulimit -n）与nginx 进程数相除，但是nginx 分配请求并不是那么均匀，所以最好与ulimit -n 的 值保持⼀致。 现在在linux 2.6内核下开启⽂件打开数为6535，worker_rlimit_nofile就相应应该填写6535。 这是因为nginx调度时分配请求到进程并不是那么的均衡，所以假如填写10240，总并发量达到3-4万 时就有进程可能超过10240了，这时会返回502错误。 查看linux系统⽂件描述符的⽅法： [rot@web01 ~]# sysctl -a | grep fs.file fs.file-max = 78972 fs.file-nr = 510 0 78972
- 4.use epol; 使⽤epol 的I/O 模型 ( 补充说明: 与apache相类，nginx针对不同的操作系统，有不同的事件模型

- A）标准事件模型 Select、pol属于标准事件模型，如果当前系统不存在更有效的⽅法，nginx会选择select或pol
- B）⾼效事件模型


Kqueue：使⽤于 FreBSD 4.1+, OpenBSD 2.9+, NetBSD 2.0 和 MacOS X. 使⽤双处理器的MacOS X 系统使⽤kqueue可能会造成内核崩溃。 Epol: 使⽤于Linux内核2.6版本及以后的系统。 /dev/pol：使⽤于 Solaris 71/ 9+, HP/UX1.2+ (eventport), IRIX 6.5.15+ 和 Tru64 UNIX 5.1A+。

Eventport：使⽤于 Solaris 10. 为了防⽌出现内核崩溃的问题， 有必要安装安全补丁。 )

- 5.worker_conections 6535; 每个进程允许的最多连接数， 理论上每台nginx 服务器的最⼤连接数为 worker_proceses*worker_conections。
- 6.kepalive_timeout 60; kepalive 超时时间。


- 7.client_header_bufer_size 4k; 客户端请求头部的缓冲区⼤⼩，这个可以根据你的系统分⻚⼤⼩来设置，⼀般⼀个请求头的⼤⼩不会 超过1k，不过由于⼀般系统分⻚都要⼤于1k，所以这⾥设置为分⻚⼤⼩。 分⻚⼤⼩可以⽤命令getconf PAGESIZE 取得。 [rot@web01 ~]# getconf PAGESIZE 4096 但也有client_header_bufer_size超过4k的情况，但是client_header_bufer_size该值必须设置为“系 统分⻚⼤⼩”的整倍数。
- 8.open_file_cache max=6535 inactive=60s; 这个将为打开⽂件指定缓存，默认是没有启⽤的，max 指定缓存数量，建议和打开⽂件数⼀致， inactive 是指经过多⻓时间⽂件没被请求后删除缓存。
- 9.open_file_cache_valid 80s; 这个是指多⻓时间检查⼀次缓存的有效信息。
- 10.open_file_cache_min_uses 1; open_file_cache 指令中的inactive 参数时间内⽂件的最少使⽤次数，如果超过这个数字，⽂件描述符 ⼀直是在缓存中打开的，如上例，如果有⼀个⽂件在inactive 时间内⼀次没被使⽤，它将被移除。


⼆、关于内核参数的优化： net.ipv4.tcp_max_tw_buckets = 6 0 timewait 的数量，默认是18 0。 net.ipv4.ip_local_port_range = 1024 65 0 允许系统打开的端⼝范围。 net.ipv4.tcp_tw_recycle = 1 启⽤timewait 快速回收。 net.ipv4.tcp_tw_reuse = 1 开启重⽤。允许将TIME-WAIT sockets 重新⽤于新的TCP 连接。 net.ipv4.tcp_syncokies = 1 开启SYN Cokies，当出现SYN 等待队列溢出时，启⽤cokies 来处理。 net.core.somaxcon = 26214 web 应⽤中listen 函数的backlog 默认会给我们内核参数的net.core.somaxcon 限制到128，⽽nginx 定义的NGX_LISTEN_BACKLOG 默认为51，所以有必要调整这个值。 net.core.netdev_max_backlog = 26214 每个⽹络接⼝接收数据包的速率⽐内核处理这些包的速率快时，允许送到队列的数据包的最⼤数⽬。 net.ipv4.tcp_max_orphans = 26214 系统中最多有多少个TCP 套接字不被关联到任何⼀个⽤户⽂件句柄上。如果超过这个数字，孤⼉连接 将即刻被复位并打印出警告信息。这个限制仅仅是为了防⽌简单的DoS 攻击，不能过分依靠它或者⼈ 为地减⼩这个值，更应该增加这个值(如果增加了内存之后)。

net.ipv4.tcp_max_syn_backlog = 26214 记录的那些尚未收到客户端确认信息的连接请求的最⼤值。对于有128M 内存的系统⽽⾔，缺省值是 1024，⼩内存的系统则是128。 net.ipv4.tcp_timestamps = 0 时间戳可以避免序列号的卷绕。⼀个1Gbps 的链路肯定会遇到以前⽤过的序列号。时间戳能够让内核 接受这种“异常”的数据包。这⾥需要将其关掉。 net.ipv4.tcp_synack_retries = 1 为了打开对端的连接，内核需要发送⼀个SYN 并附带⼀个回应前⾯⼀个SYN 的ACK。也就是所谓三次 握⼿中的第⼆次握⼿。这个设置决定了内核放弃连接之前发送SYN+ACK 包的数量。 net.ipv4.tcp_syn_retries = 1 在内核放弃建⽴连接之前发送SYN 包的数量。 net.ipv4.tcp_fin_timeout = 1 如 果套接字由本端要求关闭，这个参数决定了它保持在FIN-WAIT-2 状态的时间。对端可以出错并永远 不关闭连接，甚⾄意外当机。缺省值是60 秒。2.2 内核的通常值是180 秒，3你可以按这个设置，但要 记住的是，即使你的机器是⼀个轻载的WEB 服务器，也有因为⼤量的死套接字⽽内存溢出的⻛险， FIN- WAIT-2 的危险性⽐FIN-WAIT-1 要⼩，因为它最多只能吃掉1.5K 内存，但是它们的⽣存期⻓些。 net.ipv4.tcp_kepalive_time = 30 当kepalive 起⽤的时候，TCP 发送kepalive 消息的频度。缺省是2 ⼩时。

三、下⾯贴⼀个完整的内核优化设置: vi /etc/sysctl.conf CentOS5.5中可以将所有内容清空直接替换为如下内容: net.ipv4.ip_forward = 0 net.ipv4.conf.default.rp_filter = 1 net.ipv4.conf.default.acept_source_route = 0 kernel.sysrq = 0 kernel.core_uses_pid = 1 net.ipv4.tcp_syncokies = 1 kernel.msgmnb = 6536 kernel.msgmax = 6536 kernel.shmax = 68719476736 kernel.shmal = 4294967296 net.ipv4.tcp_max_tw_buckets = 6 0 net.ipv4.tcp_sack = 1 net.ipv4.tcp_window_scaling = 1 net.ipv4.tcp_rmem = 4096 87380 4194304 net.ipv4.tcp_wmem = 4096 16384 4194304 net.core.wmem_default = 838608

net.core.rmem_default = 838608 net.core.rmem_max = 16 7216 net.core.wmem_max = 16 7216 net.core.netdev_max_backlog = 26214 net.core.somaxcon = 26214 net.ipv4.tcp_max_orphans = 327680 net.ipv4.tcp_max_syn_backlog = 26214 net.ipv4.tcp_timestamps = 0 net.ipv4.tcp_synack_retries = 1 net.ipv4.tcp_syn_retries = 1 net.ipv4.tcp_tw_recycle = 1 net.ipv4.tcp_tw_reuse = 1 net.ipv4.tcp_mem = 945 0 915 0 927 0 net.ipv4.tcp_fin_timeout = 1 net.ipv4.tcp_kepalive_time = 30 net.ipv4.ip_local_port_range = 1024 65 0 使配置⽴即⽣效可使⽤如下命令： /sbin/sysctl -p 四、下⾯是关于系统连接数的优化 linux 默认值 open files 和 max user proceses 为 1024 #ulimit -n 1024 #ulimit –u 1024 问题描述： 说明 server 只允许同时打开 1024 个⽂件，处理 1024 个⽤户进程 使⽤ulimit -a 可以查看当前系统的所有限制值，使⽤ulimit -n 可以查看当前的最⼤打开⽂件数。 新装的linux 默认只有1024 ，当作负载较⼤的服务器时，很容易遇到eror: to many open files 。因 此，需要将其改⼤。

解决⽅法： 使⽤ ulimit –n 6535 可即时修改，但重启后就⽆效了。（注ulimit -SHn 6535 等效 ulimit -n 6535 ，-S 指soft ，-H 指hard) 有如下三种修改⽅式：

- 1. 在/etc/rc.local 中增加⼀⾏ ulimit -SHn 6535
- 2. 在/etc/profile 中增加⼀⾏ ulimit -SHn 6535
- 3. 在/etc/security/limits.conf 最后增加：


* soft nofile 6535* hard nofile 6535* soft nproc 6535* hard nproc 6535

具体使⽤哪种，在 CentOS 中使⽤第1 种⽅式⽆效果，使⽤第3 种⽅式有效果，⽽在Debian 中使⽤第2 种有效果

# ulimit -n 6535 # ulimit -u 6535

备注：ulimit 命令本身就有分软硬设置，加-H 就是硬，加-S 就是软默认显示的是软限制 soft 限制指的是当前系统⽣效的设置值。 hard 限制值可以被普通⽤户降低。但是不能增加。 soft 限制 不能设置的⽐ hard 限制更⾼。 只有 rot ⽤户才能够增加 hard 限制值。

五、下⾯是⼀个简单的nginx 配置⽂件： user w w; worker_proceses 8; worker_cpu_afinity 01 010 010 01 0 01 0 01 0 01 0; eror_log / w/log/nginx_eror.log crit; pid /usr/local/nginx/nginx.pid; worker_rlimit_nofile 20480; events { use epol; worker_conections 20480; } htp { include mime.types; default_type aplication/octet-stream; charset utf-8; server_names_hash_bucket_size 128; client_header_bufer_size 2k; large_client_header_bufers 4 4k; client_max_body_size 8m; sendfile on; tcp_nopush on; kepalive_timeout 60; fastcgi_cache_path /usr/local/nginx/fastcgi_cache levels=1 2

keys_zone=TEST 10m inactive=5m; fastcgi_conect_timeout 30; fastcgi_send_timeout 30; fastcgi_read_timeout 30; fastcgi_bufer_size 4k; fastcgi_bufers 8 4k; fastcgi_busy_bufers_size 8k; fastcgi_temp_file_write_size 8k; fastcgi_cache TEST; fastcgi_cache_valid 20 302 1h; fastcgi_cache_valid 301 1d; fastcgi_cache_valid any 1m; fastcgi_cache_min_uses 1; fastcgi_cache_use_stale eror timeout invalid_header htp_50; open_file_cache max=20480 inactive=20s; open_file_cache_min_uses 1; open_file_cache_valid 30s; tcp_nodelay on; gzip on; gzip_min_length 1k; gzip_bufers 4 16k; gzip_htp_version 1.0; gzip_comp_level 2; gzip_types text/plain aplication/x-javascript text/cs aplication/xml; gzip_vary on; server { listen 8080; server_name backup.aiju.com; index index.php index.htm; rot / w/html/; location /status { stub_status on; } location ~ .*\.(php|php5)?$

{ fastcgi_pas 127.0.0.1 9 0; fastcgi_index index.php; include fcgi.conf; } location ~ .*\.(gif|jpg|jpeg|png|bmp|swf|js|cs)$ { expires 30d; } log_format aces '$remote_adr- $remote_user [$time_local] "$request" ' '$status $body_bytes_sent "$htp_referer" ' '"$htp_user_agent" $htp_x_forwarded_for'; aces_log / w/log/aces.log aces; } } 六、关于FastCGI 的⼏个指令： fastcgi_cache_path /usr/local/nginx/fastcgi_cache levels=1 2 keys_zone=TEST 10minactive=5m; 这个指令为FastCGI 缓存指定⼀个路径，⽬录结构等级，关键字区域存储时间和⾮活动删除时间。 fastcgi_conect_timeout 30; 指定连接到后端FastCGI 的超时时间。 fastcgi_send_timeout 30; 向FastCGI 传送请求的超时时间，这个值是指已经完成两次握⼿后向FastCGI 传送请求的超时时间。 fastcgi_read_timeout 30; 接收FastCGI 应答的超时时间，这个值是指已经完成两次握⼿后接收FastCGI 应答的超时时间。 fastcgi_bufer_size 4k; 指定读取FastCGI 应答第⼀部分需要⽤多⼤的缓冲区，⼀般第⼀部分应答不会超过1k，由于⻚⾯⼤⼩为 4k，所以这⾥设置为4k。 fastcgi_bufers 8 4k; 指定本地需要⽤多少和多⼤的缓冲区来缓冲FastCGI 的应答。 fastcgi_busy_bufers_size 8k; 这个指令我也不知道是做什么⽤，只知道默认值是fastcgi_bufers 的两倍。 fastcgi_temp_file_write_size 8k; 在写⼊fastcgi_temp_path 时将⽤多⼤的数据块，默认值是fastcgi_bufers 的两倍。 fastcgi_cache TEST 开启FastCGI 缓存并且为其制定⼀个名称。个⼈感觉开启缓存⾮常有⽤，可以有效降低CPU 负载，并 且防⽌502 错误。 fastcgi_cache_valid 20 302 1h;fastcgi_cache_valid 301 1d;fastcgi_cache_valid any 1m;

为指定的应答代码指定缓存时间，如上例中将20，302 应答缓存⼀⼩时，301 应答缓存1 天，其他为 1 分钟。 fastcgi_cache_min_uses 1; 缓存在fastcgi_cache_path 指令inactive 参数值时间内的最少使⽤次数，如上例，如果在5 分钟内某⽂ 件1 次也没有被使⽤，那么这个⽂件将被移除。 fastcgi_cache_use_stale eror timeout invalid_header htp_50; 不知道这个参数的作⽤，猜想应该是让nginx 知道哪些类型的缓存是没⽤的。以上为nginx 中FastCGI 相关参数，另外，FastCGI ⾃身也有⼀些配置需要进⾏优化，如果你使⽤php-fpm 来管理FastCGI，可 以修改配置⽂件中的以下值： <value name="max_children">60</value> 同时处理的并发请求数，即它将开启最多60 个⼦线程来处理并发连接。 <value name="rlimit_files">10240</value> 最多打开⽂件数。 <value name="max_requests">20480</value> 每个进程在重置之前能够执⾏的最多请求数。

