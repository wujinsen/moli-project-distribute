# 概述

在服务器硬件资源额定有限的情况下，最⼤的压榨服务器的性能，提⾼服务器的并发处理能⼒，是很 多运维技术⼈员思考的问题。要提⾼Linux系统下的负载能⼒，可以使⽤Nginx等原⽣并发处理能⼒就 很强的Web服务器，如果使⽤Apache的可以启⽤其Worker模式，来提⾼其并发处理能⼒。除此之外， 在考虑节省成本的情况下，可以修改Linux的内核相关TCP参数，来最⼤的提⾼服务器性能。当然，最 基础的提⾼负载问题，还是升级服务器硬件了，这是最根本的。 Linux系统下，TCP连接断开后，会以TIME_WAIT状态保留⼀定的时间，然后才会释放端⼝。当并发请 求过多的时候，就会产⽣⼤量的TIME_WAIT状态的连接，⽆法及时断开的话，会占⽤⼤量的端⼝资源 和服务器资源。这个时候我们可以优化TCP的内核参数，来及时将TIME_WAIT状态的端⼝清理掉。 本⽂介绍的⽅法只对拥有⼤量TIME_WAIT状态的连接导致系统资源消耗有效，如果不是这种情况下， 效果可能不明显。可以使⽤netstat命令去查TIME_WAIT状态的连接状态，输⼊下⾯的组合命令，查看 当前TCP连接的状态和对应的连接数量： # netstat -n | awk '/^tcp/ {+S[$NF]} END {for(a in S) print a, S[a]}' 这个命令会输出类似下⾯的结果： LAST_ACK 16 SYN_RECV 348 ESTABLISHED 70

- FIN_WAIT1 29
- FIN_WAIT2 30 CLOSING 3 TIME_WAIT 18098 我们只⽤关⼼TIME_WAIT的个数，在这⾥可以看到，有18 0多个TIME_WAIT，这样就占⽤了18 0 多个端⼝。要知道端⼝的数量只有6535个，占⽤⼀个少⼀个，会严重的影响到后继的新连接。这种 情况下，我们就有必要调整下Linux的TCP内核参数，让系统更快的释放TIME_WAIT连接。


# 修改⽅式

修改： vim /etc/sysctl.conf 在这个⽂件中，加⼊下⾯的⼏⾏内容： net.ipv4.tcp_syncokies = 1 net.ipv4.tcp_tw_reuse = 1 net.ipv4.tcp_tw_recycle = 1 net.ipv4.tcp_fin_timeout = 30 输⼊下⾯的命令，让内核参数⽣效： # sysctl -p 解释：

简单的说明上⾯的参数的含义： net.ipv4.tcp_syncokies = 1 #表示开启SYN Cokies。当出现SYN等待队列溢出时，启⽤cokies来处理，可防范少量SYN攻击， 默认为0，表示关闭； net.ipv4.tcp_tw_reuse = 1 #表示开启重⽤。允许将TIME-WAIT sockets重新⽤于新的TCP连接，默认为0，表示关闭； net.ipv4.tcp_tw_recycle = 1 #表示开启TCP连接中TIME-WAIT sockets的快速回收，默认为0，表示关闭； net.ipv4.tcp_fin_timeout #修改系統默认的 TIMEOUT 时间。在经过这样的调整之后，除了会进⼀步提升服务器的负载能⼒之 外，还能够防御⼩流量程度的DoS、 C和SYN攻击。 修改： 此外，如果你的连接数本身就很多，我们可以再优化⼀下TCP的可使⽤端⼝范围，进⼀步提升服务器 的并发能⼒。依然是往上⾯的参数⽂件中，加⼊下⾯这些配置： net.ipv4.tcp_kepalive_time = 120 net.ipv4.ip_local_port_range = 1 0 65 0 net.ipv4.tcp_max_syn_backlog = 8192

- net.ipv4.tcp_max_tw_buckets = 5 0 解释： #这⼏个参数，建议只在流量⾮常⼤的服务器上开启，会有显著的效果。⼀般的流量⼩的服务器上，没 有必要去设置这⼏个参数。 net.ipv4.tcp_kepalive_time = 120 #表示当kepalive起⽤的时候，TCP发送kepalive消息的频度。缺省是2⼩时，改为20分钟。 net.ipv4.ip_local_port_range = 1 0 65 0 #表示⽤于向外连接的端⼝范围。缺省情况下很⼩：32768到61 0，改为1 0到65 0。（注意： 这⾥不要将最低值设的太低，否则可能会占⽤掉正常的端⼝！） net.ipv4.tcp_max_syn_backlog = 8192 #表示SYN队列的⻓度，默认为1024，加⼤队列⻓度为8192，可以容纳更多等待连接的⽹络连接数。
- net.ipv4.tcp_max_tw_buckets = 6 0 #表示系统同时保持TIME_WAIT的最⼤数量，如果超过这个数字，TIME_WAIT将⽴刻被清除并打印警 告信息。默 认为18 0，改为6 0。对于Apache、Nginx等服务器，上⼏⾏的参数可以很好地减少 TIME_WAIT套接字数量，但是对于 Squid，效果却不⼤。此项参数可以控制TIME_WAIT的最⼤数量， 避免Squid服务器被⼤量的TIME_WAIT拖死。


内核其他TCP参数说明： net.ipv4.tcp_max_syn_backlog = 6536

#记录的那些尚未收到客户端确认信息的连接请求的最⼤值。对于有128M内存的系统⽽⾔，缺省值是 1024，⼩内存的系统则是128。

net.core.netdev_max_backlog = 32768 #每个⽹络接⼝接收数据包的速率⽐内核处理这些包的速率快时，允许送到队列的数据包的最⼤数⽬。

net.core.somaxcon = 32768 #web应⽤中listen函数的backlog默认会给我们内核参数的net.core.somaxcon限制到128，⽽nginx定 义的NGX_LISTEN_BACKLOG默认为51，所以有必要调整这个值。

net.core.wmem_default = 838608 net.core.rmem_default = 838608 net.core.rmem_max = 16 7216 #最⼤socket读bufer,可参考的优化值:87320

net.core.wmem_max = 16 7216 #最⼤socket写bufer,可参考的优化值:87320

net.ipv4.tcp_timestsmps = 0 #时间戳可以避免序列号的卷绕。⼀个1Gbps的链路肯定会遇到以前⽤过的序列号。时间戳能够让内核 接受这种“异常”的数据包。这⾥需要将其关掉。

net.ipv4.tcp_synack_retries = 2 #为了打开对端的连接，内核需要发送⼀个SYN并附带⼀个回应前⾯⼀个SYN的ACK。也就是所谓三次 握⼿中的第⼆次握⼿。这个设置决定了内核放弃连接之前发送SYN+ACK包的数量。

net.ipv4.tcp_syn_retries = 2 #在内核放弃建⽴连接之前发送SYN包的数量。

#net.ipv4.tcp_tw_len = 1 net.ipv4.tcp_tw_reuse = 1 # 开启重⽤。允许将TIME-WAIT sockets重新⽤于新的TCP连接。

net.ipv4.tcp_wmem = 8192 4360 87320 # TCP写bufer,可参考的优化值: 8192 4360 87320

net.ipv4.tcp_rmem = 32768 4360 87320

# TCP读bufer,可参考的优化值: 32768 4360 87320

net.ipv4.tcp_mem = 945 0 915 0 927 0 # 同样有3个值,意思是:

- net.ipv4.tcp_mem[0]:低于此值，TCP没有内存压⼒。
- net.ipv4.tcp_mem[1]:在此值下，进⼊内存压⼒阶段。
- net.ipv4.tcp_mem[2]:⾼于此值，TCP拒绝分配socket。 上述内存单位是⻚，⽽不是字节。可参考的优化值是:786432 1048576 1572864


net.ipv4.tcp_max_orphans = 327680 #系统中最多有多少个TCP套接字不被关联到任何⼀个⽤户⽂件句柄上。 如果超过这个数字，连接将即刻被复位并打印出警告信息。 这个限制仅仅是为了防⽌简单的DoS攻击，不能过分依靠它或者⼈为地减⼩这个值， 更应该增加这个值(如果增加了内存之后)。

net.ipv4.tcp_fin_timeout = 30 #如果套接字由本端要求关闭，这个参数决定了它保持在FIN-WAIT-2状态的时间。对端可以出错并永 远不关闭连接，甚⾄意外当机。缺省值是60秒。2.2 内核的通常值是180秒，你可以按这个设置，但要 记住的是，即使你的机器是⼀个轻载的WEB服务器，也有因为⼤量的死套接字⽽内存溢出的⻛险， FIN- WAIT-2的危险性⽐FIN-WAIT-1要⼩，因为它最多只能吃掉1.5K内存，但是它们的⽣存期⻓些。经 过这样的优化配置之后，你的服务器的TCP并发处理能⼒会显著提⾼。以上配置仅供参考，⽤于⽣产 环境请根据⾃⼰的实际情况。

