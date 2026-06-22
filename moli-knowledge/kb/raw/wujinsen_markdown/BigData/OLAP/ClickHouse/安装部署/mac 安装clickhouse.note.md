- 1、前提 安装 homebrew
- 2、安装docker brew instal -cask-apdir=/Aplications docker 安装完成后在aplication找到docker图标 启动
- 3、安装ClickHouse 客户端：docker pul yandex/clickhouse-client 服务端：docker pul yandex/clickhouse-server
- 4、启动镜像 docker run -d-name ch-server-ulimit nofile=26214 26214 -p 8123 8123 -p 9 0 9 0 -p 909 909 yandex/clickhouse-server
- 5、连接ClickHouse镜像 我们使⽤ Datagraid idea内嵌版本 clickhouse默认⽤户名是defult 没有密码


