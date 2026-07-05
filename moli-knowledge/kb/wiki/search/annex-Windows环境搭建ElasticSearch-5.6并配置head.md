---
title: Windows环境搭建ElasticSearch 5.6并配置head.note（原文插图 annex）
slug: annex-Windows环境搭建ElasticSearch-5.6并配置head
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/ElasticSearch/安装/Windows环境搭建ElasticSearch 5.6并配置head.note.md
related: [elasticsearch-搜索]
created: 2026-07-05
updated: 2026-07-05
---

前⾔： ES5*以上版本需要jdk1.8，jdk1.8，jdk1.8.重要的事情说三遍

- 1、下载ElasticSearch htps:/ w.elastic.co/cn/downloads/elasticsearch#ga-release 因为是windows版本，所以下载zip即可

- 2、解压，我的⽬录位置：“E:\elasticsearch-5.4.1\” 在该⽂件夹的bin⽬录下双击elasticsearch.bat执⾏，完成后任意浏览器键⼊127.0.0.1 920,出现下⾯界 ⾯，证明成功

- 3、安装node 5以上版本安装head需要安装node和grunt(1.*,2.*直接⽤plugin命令即可安装)


![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

https://nodejs.org/en/download/

下载地址： 根据⾃⼰系统下载相应的msi，双击安装。 安装完成⽤cmd进⼊安装⽬录执⾏ node -v可查看版本号

![image 3](assets/imageFile3.png)

![image 4](assets/imageFile4.png)

在同⼀⽬录下执⾏ npm instal -g grunt-cli命令，安装grunt 完出现⼀堆⻩字 证明安装成功(我安装完忘 记截图了

![image 5](assets/imageFile5.png)

尴尬

)

⽤grunt -version查看版本号

![image 6](assets/imageFile6.png)

- 4、安装head


ctrl+c退出es 修改elasticsearch.yml⽂件 在⽂件最后加⼊ http.cors.enabled: true http.cors.allow-origin: "*" node.master: true node.data: true 放开network.host: 192.168.0.1的注释并改为network.host: 0.0.0.0 放开cluster.name；node.name；http.port的注释 双击elasticsearch.bat重启es

https://github.com/mobz/elasticsearch-head

下载zip⽂件

![image 7](assets/imageFile7.png)

解压到指定⽂件夹下，我的⽂件夹是E:\elasticsearch-5.4.1\elasticsearch-head-master\ 进⼊该⽂件夹，修改E:\elasticsearch-5.4.1\elasticsearch-head-master\Gruntfile.js

![image 8](assets/imageFile8.png)

进⼊E:\elasticsearch-5.4.1\elasticsearch-head-master\_site修改app.js 中下⽂内容为服务器 地址，如果是本机部署不修改也可以。

![image 9](assets/imageFile9.png)

cmd进⼊E:\elasticsearch-5.4.1\elasticsearch-head-master⽂件夹 执⾏ npm install 安装完成执⾏grunt server 或者npm run start(以后每次)，出现下图证明安装成功，如果还不成 功，退出es再执⾏⼀次npm install -g grunt-cli

![image 10](assets/imageFile10.png)

6、安装完成查看结果127.0.0.1:9100，下图是我建了索引后的，没建索引时候没节点（⻅最后⼀ 图）

![image 11](assets/imageFile11.png)

最后⼀图，没建节点的

![image 12](assets/imageFile12.png)

ps：如果关闭了 下次再127.0.0.1：9100之前都要npm run start，才可正常启动head

/编辑elasticsearch.yml⽂件

# 集群的名字 cluster.name: elasticsearch # 节点名字 node.name: node-1 # 索引分⽚个数，默认为5⽚ index.number_of_shards: 5 # 索引副本个数，默认为1个副本 index.number_of_replicas: 1 # 数据存储⽬录（多个路径⽤逗号分隔） path.data: /home/ntc/es/data # ⽇志⽬录 path.logs: /home/ntc/es/logs # 修改⼀下ES的监听地址，这样别的机器才可以访问 network.host: 192.168.40.133 # 设置节点间交互的tcp端⼝（集群）,默认是9300

transport.tcp.port: 9300 # 监听端⼝（默认的就好） http.port: 9200 # 增加新的参数，这样head插件才可以访问es http.cors.enabled: true http.cors.allow-origin: "*"

index.analysis.analyzer.default.tokenizer : "ik_max_word" index.analysis.analyzer.default.type: "ik"
