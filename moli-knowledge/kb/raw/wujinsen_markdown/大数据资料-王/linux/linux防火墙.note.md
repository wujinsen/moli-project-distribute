- 1、关闭iptables防⽕墙： 查看：chkconfig --list iptables

- （1） 重启后永久性⽣效： 开启：chkconfig iptables on 关闭：chkconfig iptables off

- （2） 即时⽣效，重启后失效： 开启：service iptables start 关闭：service iptables stop


- 2、查看SELinux状态：

- 1）/usr/sbin/sestatus -v #如果SELinux status参数为enabled即为开启状态

SELinux status: enabled

- 2）getenforce #也可以⽤这个命令检查


- 3、关闭SELinux：


- 1、临时关闭（不⽤重启机器）：

setenforce 0 #设置SELinux 成为permisive模式

#setenforce 1 设置SELinux 成为enforcing模式

- 2、修改配置⽂件需要重启机器：


修改/etc/selinux/config ⽂件

将SELINUX=enforcing改为SELINUX=disabled

重启机器即可

