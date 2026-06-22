# = Nginx介绍和安装 =

Nginx是⼀个⾃由、开源、⾼性能及轻量级的HTP服务器及反转代理服务器， 其性能与IMAP/POP3代理服务器相当。Nginx以其⾼性能、稳定、功能丰富、配置简单及占⽤系统资源 少⽽著称。 Nginx 超越 Apache 的⾼性能和稳定性，使得国内使⽤ Nginx 作为 Web 服务器的⽹站也越来越多.

- *基础功能 处理静态⽂件，索引⽂件以及⾃动索引； 反向代理加速(⽆缓存)，简单的负载均衡和容错； FastCGI，简单的负载均衡和容错； 模块化的结构。过滤器包括gzi ping, byte ranges, chunked responses, 以及 SI-filter 。在 SI过滤器 中，到同⼀个 proxy 或者 FastCGI 的多个⼦请求并发处理；

SL 和 TLS SNI ⽀持；

- *优势 Nginx专为性能优化⽽开发，性能是其最重要的考量, 实现上⾮常注重效率 。它⽀持内核Pol模型，能 经受⾼负载的考验, 有报告表明能⽀持⾼达 50, 0 个并发连接数。 Nginx作为负载均衡服务器: Nginx 既可以在内部直接⽀持 Rails 和 PHP 程序对外进⾏服务, 也可以⽀持 作为 HTP代理服务器对外进⾏服务。 Nginx具有很⾼的稳定性。其它HTP服务器，当遇到访问的峰值，或者有⼈恶意发起慢速连接时，也 很可能会导致服务器物理内存耗尽频繁交换，失去响应，只能重启服务器。 例如当前apache⼀旦上到20个以上进程，web响应速度就明显⾮常缓慢了。⽽Nginx采取了分阶段资 源分配技术，使得它的CPU与内存占⽤率⾮常低。 nginx官⽅表示保持10, 0个没有活动的连接，它只占2.5M内存，就稳定性⽽⾔, nginx⽐lighthtpd更 胜⼀筹。 Nginx⽀持热部署。它的启动特别容易, 并且⼏乎可以做到7*24不间断运⾏，即使运⾏数个⽉也不需要 重新启动。你还能够在不间断服务的情况下，对软件版本进⾏进⾏升级。 Nginx采⽤C进⾏编写, 不论是系统资源开销还是CPU使⽤效率都⽐ Perlbal 要好很多。
- *nginx的安装


开发稳定版: Nginx 0.8.X 当前稳定版: Nginx 0.7.X 历史稳定版: Nginx 0.6.X [python]view plaincopyprint?

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
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.


- 1)pcre安装，⽀持正则表达式

htp:/ w.pcre.org/

# tar zxvf pcre-7.9.tar.gz

# cd pcre-7.9

#./configure

# make & make instal

- 2)opensl安装(可选)，⽀持安全协议的站点

htp:/ w.opensl.org/

# tar zxvf opensl-0.9.8l.tar.gz

# cd opensl-0.9.8l

#./config

# make & make instal

- 3)nginx的安装


# tar zxvf nginx-0.7.64.tar.gz

# cd nginx-0.7.64

配置安装和不安装组件： -with-MODULE_NAME or-without-MODULE_NAME

# ./configure-prefix=/usr/local/nginx/nginx801-with-opensl=/usr/include/opensl -withhtp_stub_status_module

# make & make instal

- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.


⽬录结构：

conf 配置⽂件

html 静态⻚⾯

logs ⽇志⽂件

sbin 主程序

- 4)启动

# /usr/local/nginx/nginx801/sbin/nginx/启动

启动参数：

- -c </path/to/config> 为 Nginx 指定⼀个配置⽂件，来代替缺省的。
- -t 不运⾏，⽽仅仅测试配置⽂件。nginx 将检查配置⽂件的语法的正确性，并尝试打开配置⽂件中 所引⽤到的⽂件。
- -v 显示 nginx 的版本。
- -V 显示 nginx 的版本，编译器版本和配置参数。


不启动，仅测试配置⽂件：/usr/bin/nginx -t -c ~/mynginx.conf

- 5)配置⾃启动


# = ⼀个简单的配置⽂件 =

[python]

view plaincopyprint? # -基本模块

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


# 使⽤的⽤户和组

user w w;

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
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.


# 指定⼯作进程数

worker_proceses 1;

# 可以使⽤ [ debug | info | notice | warn | eror | crit ]参数

#eror_log logs/eror.log;

#eror_log logs/eror.log notice;

# 指定 pid 存放的路径

#pid logs/nginx.pid;

# -事件模块

events {

#每个worker的最⼤连接数

worker_conections 1024;

}

# -HTP 模块

htp {

#包含⼀个⽂件描述了：不同⽂件后缀对应的MIME，⻅案例分析

include mime.types;

#制定默认MIME类型为⼆进制字节流

default_type aplication/octet-stream;

#指令 aces_log 指派路径、格式和缓存⼤⼩。

- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.


#aces_log of;

#开启调⽤Linux的sendfile()，提供⽂件传输效率

sendfile on;

#是否允许使⽤socket的TCP_NOPUSH或TCP_CORK选项

#tcp_nopush on;

#指定客户端连接保持活动的超时时间，在这个时间之后，服务器会关掉连接。

kepalive_timeout65;

#设置gzip，压缩⽂件

#gzip on;

#为后端服务器提供简单的负载均衡

upstream apaches {

- server 127.0.0.1 801;
- server 127.0.0.1 802;


}

#配置⼀台虚拟机

server {

listen 8012;

server_name localhost;

- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.


location / {

proxy_pas htp:/apaches;

} }

}

# = 模块介绍 =

模块划分： #Core 核⼼模块 #Events 事件模块 #HTP HTP模块 #Mail 邮件模块

- *核⼼模块的常⽤组件 [python]


view plaincopyprint? user

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


语法: user user [group]

缺省值: nobody nobody

指定Nginx Worker进程运⾏⽤户，默认是nobody帐号。

eror_log

语法: eror_log file [ debug | info | notice | warn | eror | crit ]

缺省值: ${prefix}/logs/eror.log

制定错误⽇志的存放位置和级别。

include

语法: include file | *

- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.


缺省值: none

include 指令还⽀持像下⾯配置⼀样的全局包含的⽅法，例如包含⼀个⽬录下所有以".conf"结尾的 ⽂件: include vhosts/*.conf;

pid

语法: pid file

进程id存储⽂件。可以使⽤ kil -HUP cat /var/log/nginx.pid/ 对Nginx进⾏配置⽂件重新加载。

worker_proceses

语法: worker_proceses number

缺省值: 1

指定⼯作进程数。nginx可以使⽤多个worker进程。

- *事件模块的常⽤组件 [python]


view plaincopyprint? worker_conections

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


语法：worker_conections number

通过worker_conections和worker_proceses可以计算出 maxclients： max_clients = worker_proceses * worker_conections

作为反向代理，max_clients为： max_clients = worker_proceses * worker_conections/4 ，因 为浏览器访问时会通过连接池建⽴多个连接。

use

语法：use [ kqueue | rtsig | epol | /dev/pol | select | pol | eventport ]

- 13.
- 14.
- 15.


如果在./configure的时候指定了不⽌⼀种事件模型，那么可以设置其中⼀个，以便告诉nginx使⽤ 哪种事件模型。默认情况下nginx会在./configure时找出最适合系统的事件模型。

事件模型是指Nginx处理连接的⽅法。

- *HTP模块的核⼼组件和变量 [python]


view plaincopyprint? 三个作⽤域：htp, server, location

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
- 24.
- 25.
- 26.
- 27.
- 28.


server

语法:server {.}

作⽤域: htp

配置⼀台虚拟机。

location

语法: location [=|~|~*|^~] /uri/ {. }

作⽤域: server

配置访问路径的处理⽅法。

listen

语 法: listen adres:port [ default [ backlog=num | rcvbuf=size | sndbuf=size | acept_filter=filter

| defered | bind | sl ]

默认值： listen 80

作⽤域: server

指定当前虚拟机的监听端⼝。

- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.


alias

语法: alias file-path|directory-path;

作⽤域: location

该指令设置指定location使⽤的路径.注意它跟 rot 相似,但是不改变⽂件的根路径,仅仅是使⽤⽂件 系统路径

rot

语法: rot path

默认值：rot html

作⽤域：htp, server, location

alias指定的⽬录是准确的，rot是指定⽬录的上级⽬录，并且该上级⽬录要含有location指定名称 的同名⽬录。

区别：

location /abc/ {

alias /home/html/abc/;

}

在这段配置下， htp:/test/abc/a.html就指定的是/home/html/abc/a.html。这段配置亦可改成

location /abc/ {

rot /home/html/;

}

这样，nginx就会去找/home/html/⽬录下的abc⽬录了，得到的结果是相同的。

- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.


HTP模块的其他基本组件将结合案例介绍。

变量：

HTP header ⾥边 特定HEADER的值,变量会转成⼩写,⽐ 如 $htp_user_agent, $htp_referer. header信息 "YOUR-STRANGE-HEADER: values" 能通 过 $htp_your_strange_header获得.

$arg_PARAMETER

$htp_HEADER

$query_string = $args

- *邮件模块的常⽤组件（略）


# = 常⽤场景配置 =

- 1.多台服务器配置负载均衡


[python]

view plaincopyprint? htp {

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


include mime.types;

default_type aplication/octet-stream;

sendfile on;

kepalive_timeout65;

upstream alserver {

#ip_hash;

- server 127.0.0.1 8083 down;
- server 127.0.0.1 8084 weight=3;


- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.


- server 127.0.0.1 801;
- server 127.0.0.1 802 backup;


}

server {

listen 8012;

server_name localhost;

location / {

proxy_pas htp:/alserver;

} }

}

ip_hash; nginx中的ip_hash技术能够将某个ip的请求定向到同⼀台后端，这样⼀来这个ip下的某个客户 端和某个后端就能建⽴起稳固的sesion

- 1.down 表示单前的 server 暂时不参与负载
- 2.weight 默认为 1.weight 越⼤，负载的权重就越⼤。
- 3.backup： 其它所有的⾮ backup 机器 down 或者忙的时候，请求 backup机器。所以这台机器压⼒ 会最轻。


- 2.通过⼿机客户端的头信息或者请求的参数转发到不⽤⽬录 [python]


view plaincopyprint? htp {

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


include mime.types;

default_type aplication/octet-stream;

sendfile on;

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
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.


kepalive_timeout65;

upstream apaches {

- server 127.0.0.1 801;
- server 127.0.0.1 802;


}

upstream tomcats {

- server 127.0.0.1 8083;
- server 127.0.0.1 8084;


}

server {

listen 8012;

server_name localhost;

location / {

- set $ismob 0;

# 注意if后的空格

if ( $htp_chip ~* "(NOKIA350)|(NOKIA320)" )

{

- set $ismob 1;


proxy_pas htp:/apaches;

- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.


}

if ( $htp_chip ~* "(NOKIA350)|(NOKIA320)" )

{

set $ismob 1;

proxy_pas htp:/tomcats;

}

if ( $ismob = 0 )

{

rot /usr/local/nginx/nginx8012/html;

}

}

location ~* /rewrite/testXID.jsp {

if ( $arg_XID = "1380138 0")

{

rewrite ^(.*)$htp:/192.168.0.190 8084/testSID.jsp break;

}

}

}

}

1、正则表达式匹配，其中：

= 完全相等； ~为区分⼤⼩写匹配； ~*为不区分⼤⼩写匹配； !~和!~*分别为区分⼤⼩写不匹配及不区分⼤⼩写不匹配。

2、⽂件及⽬录匹配，其中：

- -f和!-f⽤来判断是否存在⽂件；
- -d和!-d⽤来判断是否存在⽬录；
- -e和!-e⽤来判断是否存在⽂件或⽬录；
- -x和!-x⽤来判断⽂件是否可执⾏。 if (-d $request_filename){. }


哪些地⽅会出现正则表达式：

- 1.location ~* /.(gif|jpg|png|swf|flv)${.}
- 2.rewrite ^(.*)$ /nginx-ie/$1 break;


正则表达式举例：

- 1.多⽬录转成参数 abc.domian.com/sort/2 => abc.domian.com/index.php?act=sort&name=abc&id=2 if ($host ~* (.*)/.domain/.com) { set $sub_name $1; rewrite ^/sort/(/d+)/?$ /index.php?act=sort&cid=$sub_name&id=$1 last; }
- 2.⽬录对换 /123456/ x -> / x?id=123456 rewrite ^/(/d+)/(.+)/ /$2?id=$1 last;
- 3.防盗链 [python]view plaincopyprint?


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


htp {

include mime.types;

default_type aplication/octet-stream;

sendfile on;

kepalive_timeout65;

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
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.


server {

listen 8012;

server_name localhost;

location / {

rot html;

}

location ~* ^.+/.(gif|jpg|png|swf|flv|rar|zip)$ {

valid_referers none blocked server_nameshtp:/localhost baidu.com;

if ($invalid_referer) {

rewrite ^/ html/50x.html;

}

}

}

}

- 4.访问控制：身份验证、限制IP [python]


view plaincopyprint? htp {

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


include mime.types;

default_type aplication/octet-stream;

sendfile on;

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
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.


kepalive_timeout65;

upstream tomcats {

- server 127.0.0.1 8083;
- server 127.0.0.1 8084;


}

server {

listen 8012;

server_name localhost;

location / {

alow 192.168.4.8;

deny al;

auth_basic "index";

auth_basic_user_file./htpaswd;

proxy_pas htp:/tomcats;

} }

}

cp /usr/local/apache/apache801/bin/htpaswd /usr/local/bin/ /usr/local/bin/htpaswd -c htpaswd rot

- 5.查看Nginx的运⾏状态 [python]


view plaincopyprint? htp {

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
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.


include mime.types;

default_type aplication/octet-stream;

sendfile on;

kepalive_timeout65;

upstream apaches {

- server 127.0.0.1 801;
- server 127.0.0.1 802;


}

upstream tomcats {

- server 127.0.0.1 8083;
- server 127.0.0.1 8084;


}

server {

listen 8012;

server_name localhost;

location / {

proxy_pas htp:/tomcats;

}

- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.


location /NginxStatus {

stub_status on;

aces_log of;

auth_basic "NginxStatus";

auth_basic_user_file./htpaswd;

} }

}

# = 进阶内容 =

- 1.查看Nginx的运⾏状态

Active conections: 364 server acepts handled requests 547919 547919 17515830 Reading: 10 Writing: 26 Waiting: 328

意思如下： active conections – 当前 Nginx 正处理的活动连接数。 serveracepts handled requests- 总共处理了 547919 个连接 , 成功创建 547919 次握⼿ (证明中 间没有失败的 ), 总共处理了 17515830 个请求 ( 平均每次握⼿处理了 3.2 个数据请求 )。 reading- nginx 读取到客户端的 Header 信息数。 writing- nginx 返回给客户端的 Header 信息数。 waiting- 开启 kep-alive 的情况下，这个值等于 active - (reading + writing)，意思就是 Nginx 已经 处理完正在等候下⼀次请求指令的驻留连接。

- 2.案例分析：


将web server由apache换为nginx后,却带来意想不到的问题.多个⻚⾯显示模块显示"正在加载中 ."然 后⼀直停顿,使⽤FireBug调试前端,XSL⽂件解析失败.但载⼊⼜是HTP 20 的正常状态. 继续⽤FireBug调试,发现XSL⽂件下载时的HTP响应头中,

Content-Type是oct/stream ,⽽在原来的apache中,是text/xml,于是修改/etc/nginx/mime.types⽂件.将 XSL的扩展名加到xml组中.问题解决.

- 3. 通过系统的信号控制 Nginx 使⽤信号加载新的配置 平滑升级到新的⼆进制代码
- 4. 使⽤Nginx限制下载速率和并发数 limit_zone limit_con limit_rate
- 5. 使⽤Nginx进⾏地址转发 rewrite nginx rewrite中last和break的区别：
- 6.Nginx Internals: Nginx源代码、内部机制的分析


htp:/blog.sina.com.cn/s/blog_4b01279a010hd4c.html

htp:/blog.zhuzhaoyuan.com/209/09/nginx-internals-slides-video/

# = 参考资料 =

Nginx中⽂⽂档：

htp:/wiki.nginx.org/NginxChs

服务器系统架构分析⽇志:

htp:/ w.sudone.com/

使⽤ Nginx 提升⽹站访问速度:

htp:/ w.ibm.com/developerworks/cn/web/wa-lo-nginx/

3、Nginx的模块与⼯作原理 Nginx由内核和模块组成，其中，内核的设计⾮常微⼩和简洁，完成的⼯作也⾮常简单，仅仅通过查找 配置⽂件将客户端请求映射到⼀个location block（location是Nginx配置中的⼀个指令，⽤于URL匹 配），⽽在这个location中所配置的每个指令将会启动不同的模块去完成相应的⼯作。 Nginx的模块从结构上分为核⼼模块、基础模块和第三⽅模块， HTP模块、EVENT模块和MAIL模块 等属于核⼼模块，HTP Aces模块、HTP FastCGI模块、HTP Proxy模块和HTP Rewrite模块属 于基本模块，⽽HTP Upstream Request Hash模块、Notice模块和HTP Aces Key模块属于第三⽅ 模块，⽤户根据⾃⼰的需要开发的模块都属于第三⽅模块。正是有了这么多模块的⽀撑，Nginx的功能 才会如此强⼤。 Nginx的模块从功能上分为三类，分别是：

- (1) Handlers（处理器模块）。此类模块直接处理请求，并进⾏输出内容和修改headers信息等操作。 handlers处理器模块⼀般只能有⼀个。
- (2) Filters （过滤器模块）。此类模块主要对其他处理器模块输出的内容进⾏修改操作，最后由Nginx 输出。


- (3) Proxies （代理类模块）。就是Nginx的HTP Upstream之类的模块，这些模块主要与后端⼀些服 务⽐如fastcgi等操作交互，实现服务代理和负载均衡等功能。 下图展示了Nginx的模块下⼀次常规的HTP请求和响应的过程。


在⼯作⽅式上，Nginx分为单⼯作进程和多⼯作进程两种模式。在单⼯作进程模式下，除主进程外，还 有⼀个⼯作进程，⼯作进程是单线程的；在多⼯作进程模式下，每个⼯作进程包含多个线程。Nginx默 认为单⼯作进程模式。 Nginx的模块直接被编译进Nginx，因此属于静态编译⽅式。启动Nginx后，Nginx的模块被⾃动加载， 不像在Apache⼀样，⾸先将模块编译为⼀个so⽂件，然后在配置⽂件中指定是否进⾏加载。在解析配 置⽂件时，Nginx的每个模块都有可能去处理某个请求，但是同⼀个处理请求只能由⼀个模块来完成。

