- 1.解压缩redis-2.8.3.tar.gz
- 2.make
- 3.make instal
- 4.启动服务redis-server redis.conf
- 5.测试服务安装，运⾏客户端：redis-cli
- 6.主从配置 修改从服务器的redis.conf,添加slaveof 192.168.137.18 6379 运⾏过程中，通过redis-cli中命令可以动态不停机切换主从 从：slaveof no one 主：slaveof 192.168.137.19 6379
- 7.配置从的持久化aof⽅式： 关闭⾃动snapshot： #save 90 1 #save 30 10 #save 60 1 0 开启snapshot： apendonly yes 此处可以动态在redis-cli中不停机运⾏命令：CONFIG SET APENDONLY YES


