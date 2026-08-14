---
title: hbase安装手顺(ok).note（原文插图 annex）
slug: annex-hbase安装手顺(ok)
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/a安装文档/hbase安装手顺(ok).note.md
related: [hbase-列式存储入门]
created: 2026-07-05
updated: 2026-07-05
---

- 1.解压缩hbase-0.94.6.tar.gz
- 2.配置hbase环境变量/etc/profile exportHBASE_HOME=/home/hadop/hbase exportPATH=$PATH:$HBASE_HOME/bin

- 3.修改hbase/conf/的配置⽂件： hbase-env.sh exportJAVA_HOME=/opt/soft/jdk1.6.0_41exportHBASE_CLASPATH=/home/hadop/hadop/conf exportHBASE_MANAGES_ZK=true #如果使⽤独⽴安装的zokeper这个地⽅就是false


hbase-site.xml

<configuration> <property> <name>hbase.master</name> <value>hadop1master:6 0</value> </property> <property> <name>hbase.master.maxclockskew</name> #时间同步允许的时间差 <value>18 0</value> </property> <property> <name>hbase.rotdir</name> <value>hdfs:/master:9 0/hbase</value> </property> <property> <name>hbase.cluster.distributed</name> #是否分布式运⾏ <value>true</value> </property> <property> <name>hbase.zokeper.quorum</name> <value>slave1,slave2</value> </property> <property> <name>hbase.zokeper.property.dataDir</name> <value>/home/${user.name}/tmp/zokeper</value> </property>

</configuration>

regionservers

slave1slave2

- 4.分发到3个从节点

- scp -r ~/hbase hadop@slave1:~/
- scp -r ~/hbase hadop@slave2:~/
- scp -r ~/hbase hadop@slave3:~/


- 5.配置其他三台hbase环境变量/etc/profile export HBASE_HOME=/home/hadop/hbase export PATH=$PATH:$HBASE_HOME/bin

- 6.启动hbase： start-hbase.sh

- 7.命令⾏以及界⾯验证hbase安装： hbase shel


>listhtp:/master:6010/

![image 1](<hbase安装手顺(ok).note_images/imageFile1.png>)
