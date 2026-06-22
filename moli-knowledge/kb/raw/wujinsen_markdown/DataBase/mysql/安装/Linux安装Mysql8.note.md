# Linux安装MySQL5.7

- 1.下载mysql8

tar -xvf mysql-8.0.26-linux-glibc2.12-x86_64.tar.xz mv mysql-5.7.26-linux-glibc2.12-x86_64 /usr/local/mysql

- 2. 创建mysql⽤户组和⽤户并修改权限 groupad mysql userad -r -g mysql mysql 创建数据⽬录并赋予权限 mkdir -p /data/mysql #创建⽬录 chown mysql:mysql -R /data/mysql #赋予权限
- 3.配置my.cnf vim /etc/my.cnf

内容如下 [mysqld] bind-adres=0.0.0.0 port=306 user=mysql basedir=/usr/local/mysql datadir=/data/mysql socket=/tmp/mysql.sock log-eror=/data/mysql/mysql.er pid-file=/data/mysql/mysql.pid #character config character_set_server=utf8mb4 symbolic-links=0 explicit_defaults_for_timestamp=true

- 4. 初始化数据库


进⼊mysql的bin⽬录

cd /usr/local/mysql/bin/ 初始化

./mysqld-defaults-file=/etc/my.cnf -basedir=/usr/local/mysql/ -datadir=/data/mysql/ user=mysql -initialize

- 有的机器缺少libaio，需要安装: yum instal -y libaio

- 查看密码 cat /data/mysql/mysql.er: ehygt5hfVm-g


先将mysql.server放置到/etc/init.d/mysql中: cp /usr/local/mysql/suport-files/mysql.server /etc/init.d/mysql 启动！！！

设置软连: ln -s /usr/local/mysql/bin/mysql /usr/bin/

mysql启动:

service mysql start

ps -ef|grep mysql

ZzRWgW8Z

到这⾥说明mysql已经安装成功了！！

下⾯修改密码

⾸先登录mysql，前⾯的那个是随机⽣成的。

./mysql -u rot -p#bin⽬录下

再执⾏下⾯三步操作，然后重新登录。

SET PASWORD = PASWORD('v6|8@I36G@'); ALTER USER 'rot'@'localhost' PASWORD EXPIRE NEVER; FLUSH PRIVILEGES;

mysql8: SETPASWORD='v6|8@I36G@';

/授予rot权限 grant system_user on.* to 'rot';

这时候你如果使⽤远程连接 …你会发现你⽆法连接。

这⾥主要执⾏下⾯三个命令(先登录数据库)

use mysql #访问mysql库 update user set host = '%' where user = 'rot'; #使rot能再任何host访问 FLUSH PRIVILEGES; #刷新

ok！！！！MySQL5.7就装好了 …坑是真的多 …但是如果按这个流程⾛应该是能顺利装下来的。 （因为我装了两遍 …）

如果不希望每次都到bin⽬录下使⽤mysql命令则执⾏以下命令

ln -s /usr/local/mysql/bin/mysql /usr/bin

为了防⽌版本不同⽽导致安装失败 …这⾥我把MySQL的包放在百度云上。

链接: htps:/pan.baidu.com/s/1oZLaBAELK9tuB1FCfQfzUg 提取码: mvji 复制这段内容后打开百度⽹ 盘⼿机Ap，操作更⽅便哦

⸻版权声明：本⽂为CSDN博主「蛇⽪⽪蛋」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原 ⽂出处链接及本声明。 原⽂链接：

htps:/blog.csdn.net/ q_3759801/article/details/93489404

vi /etc/my.cnf

[mysqld] bind-adres=0.0.0.0 port=306 user=mysql basedir=/usr/local/mysql datadir=/data/mysql socket=/tmp/mysql.sock logeror=/data/mysql/mysql.er pid-file=/data/mysql/mysql.pid #character config character_set_server=utf8mb4 symbolic-links=0 explicit_defaults_for_timestamp=true

explicit_defaults_for_timestamp=1 lower_case_table_names=1

table_open_cache = 8 0

# time out

#

conect_timeout = 20 wait_timeout = 60

# conection

# conections = 2 0

_user_conections = 190 _conect_erors = 1 0

max_alowed_packet = 1G

# character set

#

character-set-server = utf8mb4 colation-server = utf8mb4_bin

# log bin

#

server-id = 1 log_bin = mysql-bin # ROW、STATEMENT、MIXED binlog_format = row sync_binlog = 1 expire_logs_days = 7 binlog_cache_size = 128m ma_i _cache_size = 512m

x_binlog_size = 256M master_info_repository=TABLE relay_log_info_repository=TABLE log_slave_updates=ON binlog_checksum=none

default-storage-engine=I NODB

i nodb

#

i n b_file_per_table=1 i no _lo_file_size=1024M i nodb_log_bufer_size=64M sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,EROR_FOR_DIVISIO N_BY_ZERO,NO_ENGINE_SUBSTITUTION

