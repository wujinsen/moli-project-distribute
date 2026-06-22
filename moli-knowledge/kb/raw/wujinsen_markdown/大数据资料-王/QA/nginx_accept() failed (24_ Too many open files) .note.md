有⼀台服务器访问量⾮常⾼，使⽤的是nginx ，错误⽇志不停报以下错误：

2010/05/26 08:53:49 [alert] 13576#0: accept() failed (24: Too many open files)

2010/05/26 08:53:49 [alert] 13576#0: accept() failed (24: Too many open files)

2010/05/26 08:53:49 [alert] 13576#0: accept() failed (24: Too many open files)

2010/05/26 08:53:49 [alert] 13576#0: accept() failed (24: Too many open files)

2010/05/26 08:53:49 [alert] 13576#0: accept() failed (24: Too many open files)

2010/05/26 08:53:49 [alert] 13576#0: accept() failed (24: Too many open files)

2010/05/26 08:53:49 [alert] 13576#0: accept() failed (24: Too many open files)

2010/05/26 08:53:49 [alert] 13576#0: accept() failed (24: Too many open files)

2010/05/26 08:53:49 [alert] 13576#0: accept() failed (24: Too many open files)

2010/05/26 08:53:49 [alert] 13576#0: accept() failed (24: Too many open files)

解决⽅法：

centos5.3 中 ulimit -n 为1024， 当Nginx连接数超过1024时，error.log中就出现以下错误：

[alert] 12766#0: accept() failed (24: Too many open files)

使⽤ ulimit -n 655350 可以把打开⽂件数设置⾜够⼤， 同时修改nginx.conf ， 添加 worker_rlimit_nofile 655350； （与error_log同级 别）

worker_processes 2; worker_rlimit_nofile 10240; events { # worker_connections 10240; }

这样就可以解决Nginx连接过多的问题，Nginx就可以⽀持⾼并发。<还要修改nginx>

另外， ulimit -n 还会影响到mysql 的并发连接数。把他提⾼，也就提⾼了mysql并发。

注意： ⽤ulimit -n 2048 修改只对当前的shell有效，退出后失效。

修改⽅法

若要令修改ulimits的数值永久⽣效,则必须修改配置⽂档,可以给ulimit修改命令放⼊/etc/profile⾥⾯，这个⽅法实在是不⽅便,

还有⼀个⽅法是修改/etc/security/limits.conf

/etc/security/limits.conf 格式，⽂件⾥⾯有很详细的注释，⽐如

- * soft nofile 655360

- * hard nofile 655360


星号代表全局， soft为软件，hard为硬件，nofile为这⾥指可打开⽂件数。

把以上两⾏内容加到 limits.conf⽂件中即可。

另外，要使 limits.conf ⽂件配置⽣效，必须要确保 pam_limits.so ⽂件被加⼊到启动⽂件中。查看 /etc/pam.d/login ⽂件中有：

session required /lib/security/pam_limits.so

修改完重新登录就可以⻅到效果，可以通过 ulimit -n 查看。

