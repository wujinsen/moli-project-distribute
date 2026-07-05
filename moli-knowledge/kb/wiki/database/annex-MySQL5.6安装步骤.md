---
title: MySQL5.6安装步骤.note（原文插图 annex）
slug: annex-MySQL5.6安装步骤
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/DataBase/mysql/安装/MySQL5.6安装步骤.note.md
related: [mysql-索引面试题]
created: 2026-07-05
updated: 2026-07-05
---

- 1.下载MySQL Community Server 5.6.37 https://dev.mysql.com/downloads/mysql/5.6.html#downloads

- 2.解压MySQL压缩包 将以下载的MySQL压缩包解压到⾃定义⽬录下。例如 D:\Program Files\mysql-5.6.37-winx64

- 3.添加环境变量 我的电脑---属性---⾼级---环境变量

- 1. 变量名：MYSQL_HOME 变量值：D:\Program Files\mysql-5.6.37-winx64 即为mysql的⾃定义解压⽬录。

- 2.在Path中添加 %MYSQL_HOME%\bin （⽤分号隔开）


- 4.注册windows系统服务


![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

将mysql注册为windows系统服务 操作如下：

- 1）从cmd进⼊到MySQL解压⽬录下的 bin ⽬录下：

- 2）输⼊服务安装命令： 管理员⽅式打开cmd.exe


<table>
  <tr>
    <th>:\ sers\ dministratorcd D:\Program Files\mysql-5.6.37-winx64 C:\Users\Administrator>d:<br><br>\ r rm il s\msl-.6. - in6>cd bin</th>
  </tr>
</table>


D:\Program Files\mysql-5.6.37-winx64\bin>

在管理员权限下

<table>
  <tr>
    <th>mysqld instal MySQL-defaults-file="D:\Program Files\mysql-5.6.37-winx64\my-default.ini"</th>
  </tr>
</table>


安装成功后会提示服务安装成功。

<table>
  <tr>
    <th>Service sucesfuly instaled.</th>
  </tr>
</table>


注：my-default.ini⽂件在MySQL解压后的根⽬录下 移除服务命令为：mysqld remove

- 5.启动MySQL服务 ⽅法⼀：启动服务命令为：net start mysql ⽅法⼆：打开管理⼯具 服务，找到MySQL服务


![image 3](assets/imageFile3.png)

通过右键选择启动或者直接点击左边的启动来启动服务。

- 6.修改ROOT账号的密码 刚安装完成时root账号默认密码为空，此时可以将密码修改为指定的密码。如：123456 ⽅法⼀： 在cmd中输⼊ c:>mysql –uroot mysql>show databases; mysql>use mysql; mysql>UPDATE user SET password=PASSWORD("123456") WHERE user='root'; mysql>FLUSH PRIVILEGES; mysql>QUIT ⽅法⼆： 利⽤第三⽅管理⼯具进⾏密码修改。如Navicat for MySQL
- 7 ROOT远程访问 在安装mysql的机器上运⾏： //进⼊MySQL服务器 d:\mysql\bin\>mysql -h localhost -u root -p


- //赋予任何主机访问数据的权限 mysql>GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION; //使修改⽣效 mysql>FLUSH PRIVILEGES; //退出MySQL服务器 mysql>EXIT
- 8 授权 你想root使⽤123456从任何主机连接到mysql服务器。


mysql>GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '123456' WITH GRANT OPTION;

如果你想允许⽤户jack从ip为10.10.50.127的主机连接到mysql服务器，并使⽤654321作为密码 mysql>GRANT ALL PRIVILEGES ON *.* TO 'jack'@’10.10.50.127’ IDENTIFIED BY '654321' WITH GRANT OPTION; mysql> FLUSH PRIVILEGES;
