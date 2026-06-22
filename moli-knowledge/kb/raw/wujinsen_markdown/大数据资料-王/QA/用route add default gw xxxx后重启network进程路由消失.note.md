⽤route add default gw xxxx后重启network进程路由消失rt,为什么会这样?我把ifcfg-eth0配置中的 GATEWAY去掉后⽤route add default gw 看能不能添加默认⽹关,在没有重启network进程前有效,在重 启完 network进程后就没有该路由了⽤route add default gw xxxx后重启network进程路由消失当然重启就没 有了，还是写到ifcfg-eth0中去吧⽤route add default gw xxxx后重启network进程路由消失写到 /etc/sysconfig/network⾥： GATEWAY=xxx.xxx.xxx.xxx⽤route add default gw xxxx后重启ne

