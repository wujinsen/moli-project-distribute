---
title: MySql的时区（serverTimezone）引发的血案.note（原文插图 annex）
slug: annex-MySql的时区（serverTimezone）引发的血案
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/DataBase/mysql/采坑/MySql的时区（serverTimezone）引发的血案.note.md
related: [mysql-索引面试题]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/zhuitian/p/1243630.html

# 前⾔

mysql8.x的jdbc升级了，增加了时区（serverTimezone）属性，并且不允许为空。

回到顶部

# ⾎案现场

配置jdbc的URL：jdbc:mysql://[IP]:[PORT]/[DB]? characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true

应⽤运⾏⼀段时间后，发现数据库中登记的时间和正常的时间不⼀致。 查询表字段值：

![image 1](assets/imageFile1.png)

⽽现在电脑的时间是：

![image 2](assets/imageFile2.png)

回到顶部

# 问题排查

- 1、服务器时间不同步 使⽤命令：date，查看linux服务器时间 [root@abc ~]# date Sat Mar 7 18:43:30 CST 2020 服务器的机器时间没有问题

- 2、程序问题 uLog.setLogTime(new Date()); 程序是使⽤的机器时间，不会有问题

- 3、数据库时间 查看数据库时间：select sysdate()


<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


mysql> select sysdate();

+---------------------+ | sysdate() | +---------------------+

- | 2020-03-07 18:48:01 |


+---------------------+ 1 row in set

<table>
  <tr>
    <th>![image 4](assets/imageFile4.png)</th>
  </tr>
</table>


时间也是没有问题，最后考虑到jdbc增加了时区属性

回到顶部

# 问题根源

UTC是什么时区 不属于任意时区 。协调世界时，⼜称世界统⼀时间，世界标准时间，国际协调时间，简称UTC。 时区(Time Zone)是地球上的区域使⽤同⼀个时间定义。184年在华盛顿召开国际经度会议时，为了克 服时间上的混乱，规定将全球划分为24个时区。在中国采⽤⾸都北京所在地东⼋区的时间为全国统⼀ 使⽤时间。 例:已知东京(东九区)时间为5⽉1⽇12: 0，求北京(东⼋区)的区时?北京时间=12  0-(9-8)=1  0(即北 京时间为5⽉1⽇ 1: 0)。

![image 5](assets/imageFile5.png)

问题找到了，就是时区字段的问题

回到顶部

# 解决⽅法

修改jdbc时区，改成服务器所在地的真实时区

修改前：jdbc:mysql://[IP]:[PORT]/[DB]? characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true 修改后：jdbc:mysql://[IP]:[PORT]/[DB]? characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true

重启应⽤，发现时间是正确的

<table>
  <tr>
    <th>![image 6](assets/imageFile6.png)</th>
  </tr>
</table>


+---------------------+ | log_time | +---------------------+

- | 2020-03-07 19:04:06 | | 2020-03-07 19:04:03 | | 2020-03-07 19:04:03 | | 2020-03-07 19:04:03 | | 2020-03-07 19:04:03 | | 2020-03-07 19:04:03 | | 2020-03-07 19:04:02 | | 2020-03-07 19:04:02 | | 2020-03-07 19:03:51 | | 2020-03-07 10:40:35 | | 2020-03-07 10:40:35 | | 2020-03-07 10:40:35 |
