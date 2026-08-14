---
title: RabbitMQ安装教程.note（原文插图 annex）
slug: annex-RabbitMQ安装教程
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/消息队列/RabbitMQ/RabbitMQ安装教程.note.md
related: [rabbitmq-入门与使用场景]
created: 2026-07-05
updated: 2026-07-05
---

# RabitMQ安装教程

简介:RabbitMQ是流⾏的开源消息队列系统，⽤erlang语⾔开发。RabbitMQ是AMQP（⾼级消息队列协议）的标准 实现。 如果不熟悉AMQP，直接看RabbitMQ的⽂档会⽐较困难。不过它也只有⼏个关键概念，这⾥简单介绍。 安装:

⾸先需要安装 Erlang环境 官⽹: htp:/ w.erlang.org/ Windows版下载地址: Linux版: 使⽤yum安装

htp:/ w.erlang.org/download/otp_win64_17.3.exe

Windows安装步骤； 第⼀步运⾏：

![image 1](assets/imageFile1.png)

第⼆步:

![image 2](assets/imageFile2.png)

第三步:

![image 3](assets/imageFile3.png)

第四步:

![image 4](assets/imageFile4.png)

第五步:

![image 5](assets/imageFile5.png)

这样 就安装完了 Erlang

然后就可以安装RabitMQ了

⾸先下载RabitMQ 的Windows版本

下载地址: htp:/ w.rabitmq.com/

### 打开安装程序 按照下⾯步骤安装:

![image 6](assets/imageFile6.png)

![image 7](assets/imageFile7.png)

![image 8](assets/imageFile8.png)

安装都⽐较简单 如此就安装完成了

![image 9](assets/imageFile9.png)

这样就是安装完成后的开始菜单的效果 都是⼀些⼯具 然后进⼊管理⼯具

![image 10](assets/imageFile10.png)

运⾏命令:[plain]

view plain copy

1. rabbitmq-plugins enable rabbitmq_management

就OK了

![image 11](assets/imageFile11.png)

经过上⾯ 的步骤 Windows版本的安装就Ok 了 ，接下来我再写写 Linux版本的安装步骤

下⾯就是Linux的安装步骤：

## 安装都是需要Erlang环境

⾸先需要Y[plain] um源⽀持:

view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


cd /usr/local/src/ mkdir rabbitmq cd rabbitmq

wget http://packages.erlang-solutions.com/erlang-solutions-1.0-1.noarch.rpm rpm -Uvh erlang-solutions-1.0-1.noarch.rpm

rpm --import http://packages.erlang-solutions.com/rpm/erlang_solutions.asc

sudo yum install erlang

运⾏以上代码 效果如下:

![image 12](assets/imageFile12.png)

上⾯都成功后 安装RabitMQ 上传rabitmq-server-3.4.1-1.noarch.rpm⽂件到/usr/soft/rabitmq/ 安装： rpm -ivh rabitmq-server-3.4.1-1.noarch.rpm 这样就安装Ok了 都⽐较简单

R[plain]abitMQ 服务的启动:

view plain copy

- 1.
- 2.
- 3.


service rabbitmq-server start service rabbitmq-server stop service rabbitmq-server restart

[plain] 开机⾃启动:

view plain copy

1.

chkconfig rabbitmq-server on

配置配置⽂件:[plain]

view plain copy

- 1.
- 2.
- 3.
- 4.


cd /etc/rabbitmq cp /usr/share/doc/rabbitmq-server-3.4.1/rabbitmq.config.example /etc/rabbitmq/

mv rabbitmq.config.example rabbitmq.config

开启⽤户远程访问:[plain]

view plain copy

1.

vi /etc/rabbitmq/rabbitmq.config

![image 13](assets/imageFile13.png)

注意要去掉后⾯的逗号。

## 开启web界⾯管理⼯具

#### [plain]

view plain copy

- 1.
- 2.


rabbitmq-plugins enable rabbitmq_management service rabbitmq-server restart

切记开放端⼝:15672/5672

好了 全部安装完成

进如WEBUI界⾯:

![image 14](assets/imageFile14.png)
