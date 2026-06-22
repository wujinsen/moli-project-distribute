htps:/blog.csdn.net/zxsoft/article/details/80730898

众所周知，apache的80端⼝为系统保留端⼝，如果通过其他⾮rot⽤户启动，会报错如下：

(13)Permision denied: make_sock: could not bind to adres [:]:80

(13)Permision denied: make_sock: could not bind to adres 0.0.0.0 80 no listening sockets available, shuting down Unable to open logs

因为普通⽤户只能⽤1024以上的端⼝，1024以内的端⼝只能由rot⽤户使⽤。

但是为了避免每次启动都通过rot⽤户，可以通过set UID的⽅式来解决此问题。

⼀次性进⾏如下操作即可完成。 在rot⽤户环境中做如下操作 cd …/apache/bin chown rot htpd chmod u+s htpd 再 su - USERNAME 到普通⽤户下，通过

…/apache/bin/apachectl start即可

为何不chmod u+s apachectl呢？ 因为set UID这种⽅式只针对⼆进制⽂件有效，⽽tail⼀下apachectl发现： apachectl是⼀个脚本⽂件，仔细查阅发现有如下⼀句

HTPD='/home/ …/apache/bin/htpd'

得出结论：apachectl脚本是通过启动htpd⽂件来启动整个htpd服务。 再次cat htpd，出现各种不可读乱码，ctrl+c结束输出之后，断定htpd为⼆进制⽂件。 最后chmod u+s htpd即可，当然得保证htpd的所属者为rot⽤户，如果不是，执⾏： chown rot htpd即可。

同样，nginx启动也如此，⽤rot⽤户进⼊ .nginx/sbin 然后chown rot nginx chmod u+s nginx 然后通过普通⽤户就可以启动了。

再同样，tomcat也如此。

当然，修改默认端⼝到⼤于1024也是可以的。

- 2.通过反向代理
- 3.iptables端⼝转发(⼤并发影响⽹络性能)


⾸先程序绑定1024以上的端⼝，然后rot权限下做转发注意有些系统需要⼿动开启IP FORWARD功能

- 1.
- 2.
- 3.
- 4.
- 5.


vi /etc/sysctl.conf #修 改 net.ipv4.ip_forward = 1 #重 新 加 载 sysctl -p /etc/sysctl.conf

- 1

- 2

- 3

- 4

- 5


iptables -A PREROUTING -t nat -p tcp-dport80 -j REDIRECT-to-port8080

