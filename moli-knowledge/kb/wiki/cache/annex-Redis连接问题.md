---
title: Redis连接问题.note（原文插图 annex）
slug: annex-Redis连接问题
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/DataBase/Redis/教程/Redis连接问题.note.md
related: [redis-面试题]
created: 2026-07-05
updated: 2026-07-05
---

今天使⽤jedis客户端api连接远程连接redis的时候，⼀直报错，如下：

![image 1](assets/imageFile1.png)

redis.clients.jedis.exceptions.JedisConnectionException: java.net.ConnectException: Connection refused: connect 省略.....

原来是redis默认只能localhost登录，所以需要开启远程登录。解决⽅法如下：

在redis的配置⽂件redis.conf中，找到bind localhost注释掉。 注释掉本机,局域⽹内的所有计算机都能访问。 band localhost 只能本机访问,局域⽹内计算机不能访问。 bind 局域⽹IP 只能局域⽹内IP的机器访问, 本地localhost都⽆法访问。

验证⽅法：

[root@mch ~]# ps -ef | grep redis root 2175 1 0 08:15 ? 00:00:05 /usr/local/bin/redis-server *:6379

/usr/local/bin/redis-server *:6379 中通过"*"就可以看出此时是允许所有的ip连接登录到这 台redis服务上。

注意事项：

今天再设置远程访问的时候，在启动Redis的时候报错：Creating Server TCP listening socket *:6379: unable to bind socket（Redis⼀定不能设置成后台运⾏，否则终端不会有任何错 误显示）。

上⽹搜索说各种原因的都有，有的说是Redis版本的bug(我⽤的版本是3.2.0)，我是参考以下⼏

篇⽂章解决了这个问题： https://github.com/antirez/redis/issues/3241 http://blog.csdn.net/qq_25797077/article/details/51986455 http://stackoverflow.com/questions/8537254/redis-connect-to-remote-server 我没有注释掉bind 127.0.0.1，⽽是将bind 127.0.0.1 改成了bind 0.0.0.0。

参考⽂章： 1.http://www.oschina.net/question/579073_113004?fromerr=YBv8RRYl
