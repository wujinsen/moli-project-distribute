- 1、⽹卡信息配置是否正确
- 2、路由器加⼊设置⽹关 route ad default gw 192.168.0.1
- 3、在、etc/resolv.conf⾥加⼊ nameserver 8.8.8.8
- 4、如果上不去⽹，配置是不是如下


- eth0：动态获取
- eth1：静态ip，只能配置IPADR，其他的都别配置。


或者：

- eth0：静态，配置IPADR GATEWAY DNS都要写

- eth1：静态ip，只能配置IPADR，其他的都别配置。


