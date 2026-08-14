---
title: 堡垒机.note（原文插图 annex）
slug: annex-堡垒机
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/linux/堡垒机.note.md
related: [linux-运维基础]
created: 2026-07-05
updated: 2026-07-05
---

![image 1](assets/imageFile1.png)

开源跳板机(堡垒机)Jumpserver v2.0.0 使⽤说明2015-04-21 23 03 47 标签： 原创作品，允许转载，转载时请务必以超链接形式标明⽂章 、作者信息和本声明。否则将追 究法律责任。

Jumpserver开源跳板机

原始出处 htp:/laoguang.blog.51cto.com/601350/1636708

说明视频： ⽤户管理：

htp:/v.youku.com/v_show/id_XOTM5Mzc3NDE2.html htp:/v.youku.com/v_show/id_XOTM5Mzg1MTY0.html

授权管理：

部署篇：

htp:/laoguang.blog.51cto.com/601350/1636273 htp:/laoguang.blog.51cto.com/601350/1635853

更新log截图篇

本篇是使⽤篇

⼀. ⽤户管理

Jumpserver 2.0.0 版本中增加了部⻔管理员⻆⾊，可以负责管理⼀个部⻔的成员和该部⻔的主机，如 果有需要请添加部⻔，如果服务器或⽤户较少可以不添加部⻔和部⻔管理员

- 1.1 添加部⻔ ⽤户管理 - 添加部⻔
- 1.2 添加部⻔管理员⽤户 ⽤户管理 - 添加⽤户

⽤户的web登录密码， sh密钥密码等以邮件发送给所填写的邮箱

查看添加后的⽤户

- 1.3 添加普通⽤户 ⽤户管理 - 添加⽤户

查收邮件

- 1.4 添加⽤户组

- 2.0.0版本的jumpserver授权主机或者sudo是以组的形式组织的，所以要建⽴⽤户组 ⽤户管理 - 添加⼩组 (有⼈问为何不是添加⽤户组？ 因为四个字⽐较好看)


- 1.5 测试添加的⽤户 根据邮件说明，登录web


下载 sh密钥，⽤来登录jumpserver

导⼊到⼯具或者使⽤ sh命令登录jumpserver，本例使⽤xshel导⼊

登录jumpserver

⼆. 资产管理

- 2.1 添加IDC机房 (重新登录管理员账户)如果有多个IDC机房，可以分别添加IDC机房，如果就那么⼀个可以不添加，使 ⽤默认的即可 资产管理 - 添加IDC

查看IDC机房

- 2.2 添加资产 登录⽅式： 有两种，⼀中是LDAP也是最主要的⽅式，服务器需要安装ldap client，另⼀种是map，也 就是映射，该模式⽤于不能安装ldap的机器，选择该模式后，需要⼿动填写主机的账号密码，⽤户从 跳板机跳转到该服务器，会以这个⽤户登录 部⻔：选择服务器输⼊哪个部⻔，也相当于把服务器授权给某个部⻔，将来该部⻔管理员可以管理该 服务器及授权 所属主机组：刚开始可不填，当选择主机组后，如果该主机组已授权给⽤户组，则该主机授权给⽤户 组的各个⽤户

查看资产

- 2.3 批量添加资产 资产管理 - 添加资产 - 批量添加 批量添加资产可以按照格式批量添加资产，对应的各个字段有说明，也有实例


查看资产

- 2.4 添加主机组 前⾯也讲过授权是基于组的，最终需要以组形式授权，所以添加主机组 资产管理 - 添加主机组


查看主机组

三. 授权管理

授权管理是⽤来授权主机或者sudo，查看⽤户权限申请并处理的模块

- 3.1 授权主机组给⽤户组 授权管理 - ⼩组授权 - 选择⽤户组 - 授权编辑


将刚才建⽴的主机组授权给该⽤户组

查看授权详情

- 3.2 测试授权 web登录建⽴的那个普通⽤户，查看授权的主机


该⽤户登录jumpserver，使⽤jumpserver登录授权主机 注： jumpserver正常使⽤会让 conect.py脚本登录⾃启动，部署⽂档后⾯有说明， 下⾯的操作为试 了⽅便测试 # cd /opt/jumpserver # python conect.py

输⼊p或P 查看所有授权主机 输⼊g或G 查看授权主机组 输⼊g或G加上组的ID，查看该组下的主机

输⼊e 可以进⼊⼆级菜单批量在主机执⾏命令，根据提示输⼊IP，⽀持通配符，可以逗号分隔，下⾯输 ⼊执⾏的命令 注意：报错可能提示没有⽬录权限，添加该⽬录并修改权限 # mkdir –p /opt/jumpserver/logs/exec_cmds # chmod 7 /opt/jumpserver/logs/exec_cmds -p

输⼊q 可以退出到上⼀层菜单或者退出

输⼊ip地址，或者ip的⼀部分，或者输⼊主机的备注，或者输⼊主机的别名(别名是⽤户在web端对主 机的⾃定义备注) 注意：报错可能提示没有⽬录权限，添加该⽬录并修改权限 # mkdir /opt/jumpserver/logs/conect/ # chmod 7 /opt/jumpserver/logs/conect/

- 3.3 Sudo授权 (重新登录管理员账户) 添加sudo可执⾏的命令组 授权管理 – sudo授权 - 添加命令组

查看命令组

sudo授权 授权管理 – sudo授权 - 查看sudo授权 - sudo授权

查看sudo授权

可以查看授权了那些主机上执⾏哪些sudo 命令

- 3.4 测试sudo命令 想必刚才的终端你还没⽤退出，使⽤jumpserver登录后端主机后，sudo测试


四. ⽇志审计

- 4.1 监控在线⽤户操作 ⽇志审计 - 在线 这时如果你的终端没⽤退出的话，会看到测试账户


点击监控，可以实时查看⽤户的操作⾏为和历史操作记录 (如果不能弹出监控窗，应该是 node index.js 程序没有启动)

点击阻断，强⾏⽤户断开

- 4.2 查看历史记录


⽇志审计 - 历史记录 - 命令统计 查看本次登录⽤户操作的记录 （如果没有⽇志 可能是log_handler.py程序没有运⾏）

五. 部⻔管理员⻆⾊的职能

将主机授权给部⻔管理员后，部⻔管理员可以管理本部⻔⽤户， 可以授权该部⻔下的主机，上⾯添加 ⽤户时已经添加了 乔峰 为部⻔管理员，下⾯将主机授权给乔峰所在部⻔

- 5.1 部⻔授权 在添加主机时，如果将主机设置为某个部⻔，则直接将主机授权给该部⻔，可省略下⾯⼯作 授权管理 - 部⻔授权 - 授权编辑

- 5.2 部⻔管理员登陆 (什么，你忘记密码了？ 去查看邮件吧)

- 5.3 查看部⻔管理员相关功能 部⻔管理员相⽐超级管理员功能要少些，只能负责该部⻔的主机授权，⽤户管理，需要说明的是，新 建的⽤户会默认属于本部⻔，新添加的主机会属于本部⻔ 快去试试吧！

六. 普通⽤户web操作

普通⽤户也可以登录jumpserver web系统，进⾏⼀些操作哦

- 6.1 登录


- 6.2 浏览浏览 可以四处浏览⼀下，试试各个功能，仪表盘，个⼈信息


- 6.3 申请主机权限 申请主机权限，可以选择申请的主机或者组，发邮件给管理员，管理员收到后会处理申请(对不起，⽬ 前申请处理还不是⾃动的) 权限申请 - 申请主机


查看申请记录

这时乔峰应该收到了邮件，可以点击链接，或者登陆jumpserver处理申请

登陆乔峰账户，查看权限申请 授权管理 - 权限审批 - 未审批

这时苦逼的管理员需要⼿动为该⽤户授权，授权完成后点击确认，嘿嘿 6.4 上传⽂件 上传下载 - ⽂件上传 填写ip地址，多个ip逗号隔开，将需要上传的⽂件或者⽬录拖拽上去，点击全部上传，上传⽂件在服务 器的/tmp⽬录下，去看看吧

到此基本的使⽤已经介绍完了，⼀些功能⽐如修改⽤户信息，删除⽤户，回收权限没有讲解，⾃⼰试 试吧，有问题可以群⾥讨论，Jumpserver是⼀个年轻的项⽬，可能存在⼀些BUG，需要您的及时反 馈，帮助我们⼀起完善项⽬！ 本⽂出⾃ “ ” 博客，请务必保留此出处

Fre Linux, Share Linux htp:/laoguang.blog.51cto.com/60135 0/1636708

<table>
  <tr>
    <th>![image 2](assets/imageFile2.png)</th>
    <th> </th>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 4](assets/imageFile4.png)<br><br>王家骏V、watchsky126、jie78</th>
    <th>16⼈</th>
    <th>了这篇⽂章</th>
  </tr>
</table>


3213507 Jumpserver 返回博主⾸⻚ 返回博客⾸⻚ 开源跳板机(堡垒机)Jumpserver v2.0.0 部署篇 Jumpserver v2.0.0 ⼀键安装脚本

类别： ┆阅读(8197)┆评论(23) ┆ ┆ 上⼀篇 下⼀篇

# 相关⽂章

⼴⽼师领衔-开源跳板机Jumpserver v2.0.0发布

开源跳板机(堡垒机)Jumpserver v2.0.0 发布

- 开源跳板机(堡垒机)Jumpserver v1.1发布

- 开源跳板机(堡垒机)Jumpserver v2.0.0 部署篇


运维堡垒机(跳板机)系统 部署篇 (⼆)

开源跳板机(堡垒机)Jumpserver v1.1部署篇

运维堡垒机(跳板机)系统 部署篇 (⼀)

CentOS7 开源跳板机(堡垒机) . 开源跳板机(堡垒机)Jumpserver部署详解

本⽂收录⾄博客专题：《 》

# ⽂章评论

< 2 >

1 ⻚数 ( 1/2 ) [1楼]

![image 5](assets/imageFile5.png)

dl52 8 回复

2015-04-2 14 39 25 功能很全，模板很好。

- [2楼]

2015-04-2 20 21 14 ＋ 1

- [3楼]

- 2015-04-23 21 54 34 你好，我下来试了下，不能添加⽤户 添加⽤户 xbzy 失败 generate_c() takes at least 2 arguments (1 given)


- [4楼]

- 2015-04-25 2 59 18感觉这个跳板机跟我们即将要设计的，在整体功能上差不多，你们已经在⽣产环 境正式使⽤了吗？


- [5楼]

- 2015-04-29 18 08  4 ⽼⼴求带。。。。

[6楼]

- 2015-05-12 09  4 45回复 workming:




![image 6](assets/imageFile6.png)

techcto 回复

![image 7](assets/imageFile7.png)

xbzy7 回复

![image 8](assets/imageFile8.png)

workming 回复

![image 9](assets/imageFile9.png)

jie783213507 回复

楼主

![image 10](assets/imageFile10.png)

⽼⼴ 回复

[4楼]

已经使⽤

- [7楼]

- 2015-05-12 09  4 57回复 xbzy7:

查看FAQ [8楼]

- 2015-05-28 14 31 54 你好,我在添加⽤户点提交的时候，⽆法发送邮件，⽹⻚报错，535, 'Eror: authentication failed'，不 知道什么原因，能帮助解答⼀下吗？ [9楼]
- 2015-06-01 20 57 29回复 wushengbao:


邮件账号密码不对 [10楼]

- 2015-06-02 10 57 35 ⾮常好的⼯具 [1楼]




楼主

![image 11](assets/imageFile11.png)

⽼⼴ 回复

[3楼]

![image 12](assets/imageFile12.png)

wushengbao 回复

楼主

![image 13](assets/imageFile13.png)

⽼⼴ 回复

[8楼]

![image 14](assets/imageFile14.png)

tonyzhang828 回复

![image 15](assets/imageFile15.png)

limengbo 回复

- 2015-06-03 13  5 50You matched ip: [u'192.168.1.10', u'192.168.1.1'] Input the Comand , The comand wil be Execute on servers, q/Q to quit. Cmd(s): w Traceback (most recent cal last): File "/opt/jumpserver/conect.py", line 392, in <module>


exec_cmd_servers(LOGIN_NAME) File "/opt/jumpserver/conect.py", line 36, in exec_cmd_servers

multi_remote_exec_cmd(hosts, username, cmd) File "/opt/jumpserver/conect.py", line 32, in multi_remote_exec_cmd username, pasword, ip, port = get_conect_item(username, host) File "/opt/jumpserver/jumpserver/api.py", line 374, in get_conect_item user = get_object(User, username=username) File "/opt/jumpserver/jumpserver/api.py", line 187, in get_object

raise ServerEror('Object get %s failed.' % str(kwargs.values( ) jumpserver.api.ServerEror: Object get [u'rot'] failed. 批量执⾏命令有报错~

- [12楼]

- 2015-06-04 15 18 04回复 ⽼⼴:

再请教⼀下，js如何使⽤django模板中的变量，尤其是在有for循环的时候，是否和在html中⼀样？希 望能解答⼀下，不甚感激。 [13楼]

- 2015-06-05 13 50 08回复 wushengbao:


- [14楼]

2015-06-08 16 45 1回复 ⽼⼴:

我看了代码，在⾸⻚ 作折线图的时候调⽤了js，⽽js中的数据是直接⽤ {var}形式来使⽤的，我试验过 了，好像可以在中括号中进⾏调⽤，如data:[{%for i in var%}{i},]，还是⾮常感谢。

- [15楼]


![image 16](assets/imageFile16.png)

wushengbao 回复

[9楼]

楼主

![image 17](assets/imageFile17.png)

⽼⼴ 回复

[12楼]

这个，这个 .我也不太会js

![image 18](assets/imageFile18.png)

wushengbao 回复

[13楼]

![image 19](assets/imageFile19.png)

wushengbao 回复

2015-06-08 16 52 35我看了源码，在⾸⻚画折线图的时候调⽤了js，⽽登录次数的数据是直接以

{var}形式传递进去的，经过实践验证，在js脚本中，[]内可以使⽤django模板，如categories: [{%for j in na%}

'{j}', {%endfor%}],

感谢你抽空回答我的问题，很愿意和⼤⽜交流学习，再次感谢。
