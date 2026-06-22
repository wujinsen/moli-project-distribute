本次安装均使⽤rot⽤户

⼀、安装环境 yum -y instal kernel-devel* yum -y instal opensl-* yum -y instal popt-devel ⼆、安装kepalived

- 1.解压缩kepalived-1.2.2.tar.gz并重命名为kepalived cd kepalived

./configure-prefix=/usr/local/kepalived -enable-lvs-syncd-enable-lvs-with-kerneldir=/lib/modules/2.6.32-431.el6.x86_64/build make make instal ln -s /usr/src/kernels/2.6.32-431.5.1.el6.x86_64/ /usr/src/linux cp /usr/local/kepalived/etc/rc.d/init.d/kepalived /etc/init.d/ cp /usr/local/kepalived/etc/sysconfig/kepalived /etc/sysconfig/ mkdir -p /etc/kepalived cp /usr/local/kepalived/etc/kepalived/kepalived.conf /etc/kepalived/ ln -s /usr/local/kepalived/sbin/kepalived /sbin/

- 2.加⼊开机启动：chkconfig kepalived on
- 3.vi /etc/kepalived/kepalived.conf


MASTER: BACKUP:

详解：

=

! Configuration File for kepalived global_defs { #notification_email { ##指定keepalived在发⽣切换时需要发送email到的对象，⼀⾏⼀个# a @ a.com.cn #}#notification_email_from a @ a.com.cn#指定发件⼈ #smtp_server 127.0.0.1#指定smtp服务器地址

#smtp_conect_timeout 30#指定smtp连接超时时间 router_id LVS_DEVEL#运⾏keepalived机器的⼀个标识

} vrp_instance VI_1 { state

MASTER ##指定那个为master，那个为backup，如果设置了nopreempt这个值不起作⽤，主备考 priority决 interface

em1#设置实例绑定的⽹卡 virtual_router_id 51 #同一实例下virtual_router_id必须相同 priority 10 #定义优先级，数字越大，优先级越高,备机要⼩于主

advert_int 1 #MASTER与BACKUP负载均衡器之间同步检查的时间间隔，单位是秒 # noprempt #设置为不抢占,从启动后主不会⾃动切换回来, 注：这个配置只能设置在backup主机 上，⽽且这个主机优先级要⽐另外⼀台⾼

authentication {#设置认证 auth_type PAS auth_pas

1 } virtual_ipadres {#设置vip

192.168.56.70 #虚拟IP }

}virtual_server 192.168.56.70 8080 { delay_loop 6 #健康检查时间间隔 lb_algo rr #lvs调度算法rr|wrr|lc|wlc|lblc|sh|dh lb_kind DR #负载均衡转发规则NAT|DR|RUN persistence_timeout 5 #会话保持时间 protocol TCP #使⽤的协议 persistence_granularity <NETMASK> #lvs会话保持粒度 virtualhost <string> #检查的web服务器的虚拟主机（host：头） sorry_server<IPADDR> <port> #备⽤机，所有realserver失效后启⽤

real_server 192.168.56.97 8080 { weight 1 #默认为1,0为失效 inhibit_on_failure #在服务器健康检查失效时，将其设为0，⽽不是直接从ipvs中删除 notify_up <string> | <quoted-string> #在检测到server up后执⾏脚本 notify_down <string> | <quoted-string> #在检测到server down后执⾏脚本

TCP_CHECK { connect_timeout 3 #连接超时时间 nb_get_retry 3 #重连次数

delay_before_retry 3 #重连间隔时间 connect_port 23 健康检查的端⼝的端⼝ bindto <ip>

} HTTP_GET | SSL_GET{

url{ #检查url，可以指定多个 path / digest <string> #检查后的摘要信息 status_code 200 #检查的返回状态码

} connect_port <port> bindto <IPADD> connect_timeout 5 nb_get_retry 3 delay_before_retry 2

}

SMTP_CHECK{ host{ connect_ip <IP ADDRESS> connect_port <port> #默认检查25端⼝ bindto <IP ADDRESS>

} connect_timeout 5 retry 3 delay_before_retry 2 helo_name <string> | <quoted-string> #smtp helo请求命令参数，可选

} MISC_CHECK{

misc_path <string> | <quoted-string> #外部脚本路径 misc_timeout #脚本执⾏超时时间 misc_dynamic #如设置该项，则退出状态码会⽤来动态调整服务器的权重，返回0 正常，不修改；

返回1，

检查失败，权重改为0；返回2-255，正常，权重设置为：返回状态码-2 }

}

=

=

5、两台机器启动kepalived： service kepalived start

三、验证 ip a

