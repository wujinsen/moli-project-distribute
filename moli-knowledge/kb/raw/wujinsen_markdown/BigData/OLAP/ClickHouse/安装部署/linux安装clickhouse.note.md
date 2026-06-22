htps:/blog.csdn.net/weixin_458683/article/details/1236734

今天简单安装⼀下clickhouse

⼀、下载

选择Tgz安装包安装

下载地址：Index of /clickhouse/tgz/

选择stable⽬录下的安装包，采⽤21.9.4.35版本，分别是：

上传到liunx服务器

⼆、解压安装

依次将这四个安装包解压，并且每解压⼀个，执⾏⼀下解压⽂件夹下的instal下的doinst.sh脚本

解压顺序：

clickhouse-comon-static-21.9.4.35.tgz clickhouse-comon-static-dbg-21.9.4.35.tgz clickhouse-server-21.9.4.35.tgz clickhouse-client-21.9.4.35.tgz

-

# 解压 tar -zxvf clickhouse-comon-static-21.9.4.35.tgz

cd clickhouse-comon-static-21.9.4.35/instal/

# 运⾏doinst.sh

./doinst.sh

tar -zxvf clickhouse-comon-static-dbg-21.9.4.35.tgz

./clickhouse-comon-static-dbg-21.9.4.35/instal/doinst.sh

tar -zxvf clickhouse-server-21.9.4.35.tgz

./clickhouse-server-21.9.4.35/instal/doinst.sh

tar -zxvf clickhouse-client-21.9.4.35.tgz

./clickhouse-client-21.9.4.35/instal/doinst.sh

在解压clickhouse-server-21.9.4.35.tgz并运⾏./clickhouse-server-21.9.4.35/instal/doinst.sh

后，clickhouse会默认创建⼀个default的⽤户，让你设置密码，不设置密码可以按回⻋

三、启动

#查看命令 clickhouse-help

#启动 clickhouse start

连接clickhouse

clickhouse-client

# -m ⽀持多⾏语句

clickhouse-client -m

好了，clickhouse就简单安装成功了！

四、clickhouse相关⽬录

-

# 命令⽬录

/usr/bin

l |grep clickhouse

-

# 配置⽂件⽬录

cd /etc/clickhouse-server/

-

# ⽇志⽬录

cd /var/log/clickhouse-server/

-

# 数据⽂件⽬录

cd /var/lib/clickhouse/

五、允许远程访问

clickhouse 默认不允许远程访问，需要修改配置⽂件

cd /etc/clickhouse-server/

vim config.xml 把listen 注释打开

强⾏保存wq!

重启clickhouse

clickhouse restart

在浏览器输⼊服务器ip+8123验证⼀下

这样就可以远程访问了！

六、使⽤DBeaver连接Clickhouse

先安装好DBeaver

创建连接

选择ClickHouse

刚开始，要按照驱动，根据提示安装完驱动，在主机上填写对应IP和端⼝，⽤户名，密码（没有设

置可以不填）

测试链接

这样就可以通过DBeaver连接ClickHouse了！

⸻版权声明：本⽂为CSDN博主「苡~」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原⽂出处 链接及本声明。 原⽂链接：htps:/blog.csdn.net/weixin_458683/article/details/1236734

