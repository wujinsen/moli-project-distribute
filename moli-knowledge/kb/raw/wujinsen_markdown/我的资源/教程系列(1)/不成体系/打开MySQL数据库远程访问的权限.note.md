在我们使⽤mysql数据库时，有时我们的程序与数据库不在同⼀机器上，这时我们需要远程访问数据库。缺省状态下， mysql的⽤户没有远程访问的权限。下⾯介绍两种⽅法，解决这⼀问题。

- 1、改表法

可能是你的帐号不允许从远程登陆，只能在localhost。这个时候只要在localhost的那台电脑，登⼊mysql后，更改 "mysql" 数据库⾥的 "user" 表⾥的 "host" 项，从"localhost"改称"%"

mysql -u root -p

mysql>use mysql;

mysql>update user set host = '%' where user = 'root';

mysql>select host, user from user;

- 2、授权法


在安装mysql的机器上运⾏：

- 1、d:\mysql\bin\>mysql -h localhost -u root

//这样应该可以进⼊MySQL服务器

- 2、mysql>GRANT ALL PRIVILEGES ON *.* TO 'root'@'%'WITH GRANT OPTION


//赋予任何主机访问数据的权限

例如，你想myuser使⽤mypassword从任何主机连接到mysql服务器的话。

GRANT ALL PRIVILEGES ON *.* TO 'myuser'@'%'IDENTIFIED BY 'mypassword' WI

TH GRANT OPTION;

如果你想允许⽤户myuser从ip为192.168.1.6的主机连接到mysql服务器，并使⽤mypassword作为密码

GRANT ALL PRIVILEGES ON *.* TO 'myuser'@'192.168.1.3'IDENTIFIED BY

'mypassword' WITH GRANT OPTION;

- 3、mysql>FLUSH PRIVILEGES

//修改⽣效

- 4、mysql>EXIT


退出MySQL服务器，这样就可以在其它任何的主机上以root身份登录

