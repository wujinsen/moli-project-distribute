1.1.1. Could not bufer record

在import的时候设置 -inline-lob-limit <n> 设置内联的LOB对象的⼤⼩

- 1.1.1.数据库àHDFS

- 1.1.2. Packet for query is to large (13685 > 1048576). You can change this value on the server by seting the max_alowed_packet' variable.


sqop import -conect jdbc:mysql:/192.168.56.204  306/sqop-username hive-pasword hive-table jobinfo-target-dir /sqop/test5-inline-lob-limit 16 7216-fields-terminated-by '\t'

-m 1

sqop export -conect "jdbc:mysql:/192.168.56.204  306/sqop? useUnicode=true&characterEncoding=utf-8"-username hive-pasword hive-table jobinfo2export-dir /sqop/test -input-fields-terminated-by '\t'

有时候⼤的插⼊和更新会受max_allowed_packet 参数限制，导致写⼊或者更新失败。 查看⽬前配置 show VARIABLES like '%max_allowed_packet%'; 显示的结果为：

+--------------------+---------+ | Variable_name | Value | +--------------------+---------+ | max_allowed_packet | 1048576 |

+--------------------+---------+ 以上说明⽬前的配置是：1M

修改⽅法

修改⽅法

- 1、修改配置⽂件 可以编辑my.cnf来修改（windows下my.ini）,在[mysqld]段或者mysql的server配置段进⾏修改。 max_allowed_packet = 20M 如果找不到my.cnf可以通过 mysql --help | grep my.cnf 去寻找my.cnf⽂件。 linux下该⽂件在/etc/下。
- 2、在mysql命令⾏中修改 在mysql 命令⾏中运⾏


set global max_allowed_packet = 2*1024*1024*10 然后退出命令⾏，重启mysql服务，再进⼊。 show VARIABLES like '%max_allowed_packet%'; 查看下max_allowed_packet是否编辑成功

注意：该值设置过⼩将导致单个记录超过限制后写⼊数据库失败，且后续记录写⼊也将失败。

