前⾔

由于macos不⽀持直接安装clickhouse，只能通过docker容器本地安装clickhouse。

⼀、安装Docker

两种安装⽅式：参考 MacOS Docker 安装 | 菜⻦教程

⼆、Docker镜像源配置

新版Docker的镜像配置为Preferences -> Docker Engine，在⽂本框中加⼊镜像地址：

"registry-mirors": [

"htps:/6kx4zyno.miror.aliyuncs.com" ]

如图：

三、docker下载clickhouse客户端

终端输⼊指令 $ docker pul yandex/clickhouse-client

四、docker下载clickhouse服务端

终端输⼊指令 $ docker pul yandex/clickhouse-server

五、启动Clickhouse

执⾏ $ docker network create ck_net ，创建⽤于clickhouse的⽹络给docker⽤

执⾏ $ docker network ls ，查看⽹络

可以看到 ck-net的⽹络已经创建成功

接下来运⾏clickhouse-server容器

docker run -d-name ck-server-network=ck_net -ulimit nofile=26214 26214volume=/Users/wujinsen/clickhouse/some_clickhouse_database:/var/lib/clickhouse yandex/clickhouse-server

查看服务 $ docker ps

进⼊到这个容器中 $ docker exec -it ck-server /bin/bash

然后执⾏ $ clickhouse-client

进⼊容器成功，再执⾏ :) show databases; 即可查看数据库

执⾏ :)CREATE TABLE default.user_table(id UInt16, name String, age UInt16 ) ENGINE = TinyLog(); 创建数据表

执⾏ :) SELECT * FROM default.user_table; 查询表数据

关闭容器：执⾏ $ docker stop 后⾯加上容器ID

之后再启动这个容器的时候，执⾏$ docker start 加容器的ID 就可以了

如果想删除该容器，可执⾏ $ docker container rm 加容器的ID 就可以了

六、宿主机连接docker中的clickhouse

docker创建的clickhouse由于默认配置原因⽆法被宿主机直接访问，所以需要修改clickhouse的配置⽂ 件

配置 config.xml

修改配置有两种⽅式：

不挂载配置⽂件, 每次进⼊容器修改配置, ⽐较拉跨 (注意 这个镜像基于不完整的ubuntu, 需要⼿动装⼀ 下vim编辑器，$ apt-get update $ apt-get instal vim -y) 先不挂载配置⽂件启动⼀个容器，执⾏ $ docker cp clickhouse-server:/etc/clickhouse-server/ /Users/woxingwosu010/clickhouse/etc 将容器中的 /etc/clickhouse-server下的内容复制到宿主机, 修改配置⽂件/etc/clickhouse-server/config.xml 中 65⾏ 注释去掉<listen_host>:</listen_host>，然 后再基于这个复制过来的⽂件挂载配置⽂件也可以解决，执⾏ $ docker run -d-name ck-servernetwork=ck_net -ulimit nofile=26214 26214 -p 8123 8123 -p 9 0 9 0 -p 909 909volume=/Users/woxingwosu010/Documents/clickhouse/some_clickhouse_database:/var/lib/clickh ouse -v /Users/woxingwosu010/Documents/clickhouse/etc/config.xml:/etc/clickhouseserver/config.xml yandex/clickhouse-server

