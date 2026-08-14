---
title: Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note（原文插图 annex）
slug: annex-Linux-Elasticsearch初步使用(安装、Head配置、分词器配置)
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/ElasticSearch/安装/Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note.md
related: [elasticsearch-搜索]
created: 2026-07-05
updated: 2026-07-05
---

# Elasticsearch初步使⽤(安装、Head配置、分词器配置)

## 阅读⽬录

⽬录

- 1.ElasticSearch简单说明

- 2.准备安装⽂件

- 3.ElasticSearch安装

- 4.ElasticSearch_Head配置

- 5.分词插件配置 回到顶部


### ⽬录

http://www.cnblogs.com/hanyinglong/p/5464604.html

返回⽬录：

回到顶部

### 1.ElasticSearch简单说明

- a.ElasticSearch是⼀个基于Lucene开发的搜索服务器，具有分布式多⽤户的能⼒，

ElasticSearch是⽤Java开发的开源项⽬(Apache许可条款)，基于Restful Web接⼝，能够达到实时 搜索、稳定、可靠、快速、⾼性能、安装使⽤⽅便，同时它的横向扩展能⼒⾮常强，不需要重启服 务。

- b.ElasticSearch是⼀个⾮常好⽤的实时分布式搜索和分析引擎，可以帮助我们快速的处理⼤规模

数据，也可以⽤于全⽂检索，结构化搜索以及分析等。

- c.⽬前很多⽹站都在使⽤ElasticSearch进⾏全⽂检索，例如：GitHub、StackOverflow、Wiki

等。

- d.ElasticSearch式建⽴在全⽂检索引擎Lucene基础上的，⽽Lucene是最先进、⾼效的开元搜索

引擎框架，但是Lucene只是⼀个框架，要充分利⽤它的功能，我们需要很⾼的学习成本，⽽ ElasticSearch使⽤Lucene作为内部引擎，在其基础上封装了功能强⼤的Restful API，让开发⼈员可 以在不需要了解背后复杂的逻辑，即可实现⽐较⾼效的搜索。

- e.关于Lucene我在前⾯写过⼏篇博客，并且在GitHub上开源了⼀个Demo，博客地址是：

- f.ElasticSearch官⽹：

- g.ElasticSearch权威指南


htt p://www.cnblogs.com/hanyinglong/p/5387816.html

https://www.elastic.co/products/elasticsearch/ http://www.learnes.net/

回到顶部

### 2.准备安装⽂件 a.⼯欲善其事必先利其器，通过上⾯简单的描述想必⼤家已经知道ElasticSearch是⼲什么的了，

那么这时候我们就需要去使⽤它，⽽在⽤它之前则必须先将其安装，故⽽在这篇博客我将简单描述⼀ 下EasticSearch的安装，ElasticSearch_Head的配置，分词插件的配置。

- b. ElasticSearch的安装包，下载地址： ，下载最新的tar包即可。

- c. ElasticSearch_Head配置包，下载地址： ，下载最新的Zip压缩包即可。

- d.分词插件，下载地址： ，克隆源码进

⾏操作。

- e.因Elasticsearch是基于java写的，所以它的运⾏环境中需要java的⽀持，在Linux下执⾏命

令：java -version，检查Jar包是否安装，如果安装，则可以继续操作安装⼯作，否则安装java jar 包，如何安装请参看博客： 。(JDK安 装7以上)

- f. ElasticSearch_ServiceWrapper配置包，下载地址：

- g. 本次操作需要⽤到的软件以及系统如下：虚拟机(Vmware)、虚拟机中安装的Centos系统、


https://www.elastic.co/downloads/elasticsearch

https://github.com/mobz/elasticsearchhead

https://github.com/medcl/elasticsearch-analysis-ik

http://www.cnblogs.com/hanyinglong/p/5025635.html

https://github.com/elastic/elastic search-servicewrapper

Xshell、Xftp、上⾯的安装包、Git、Maven，⾄于如何使⽤它们我们下⾯会说到。

回到顶部

### 3.ElasticSearch安装

- a.通过上⾯简单的准备⼯作之后，现在已经拥有了可以安装和发布的环境，如果没有，请参考上

⾯的说明，⾃⾏查询安装。

- b.使⽤XShell连接Centos，连接成功后使⽤命令跳转到local下⾯创建属于⾃⼰的⽂件夹


kencery,在此⽂件夹下创建elasticsearch⽂件夹，命令如下；

b.1 (1)：cd usr/local/ (2)：mkdir kencery (3)：cd kencery/ (4)：mkdir elasticsearch (5)：cd elasticsearch/

c. 然后使⽤Xftp将在准备安装⽂件中下载的Elasticsearch包复制到elasticsearch⽂件夹⻄⾯， 如图所示：

![image 1](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile1.png>)

359161-20160419171056101-58379673.png

- d. 将上传的的elasticsearch-2.3.1.tar.gz包解压，解压之后命名为：elasticsearch，⾄于安装

包⾥⾯含有上⾯内容，请⾃⾏使⽤命令ls -l查看。

- d.1 tar -zxvf elasticsearch-2.3.1.tar.gz

- d.2 mv elasticsearch-2.3.1 elasticsearch


- e. 进⼊elasticsearch⽂件后运⾏脚本启动，命令如下： e.1 cd elasticsearch


- e.2 调⽤启动命令：./bin/elasticsearch(如果以root⽤户启动，正常情况下这⾥会报错)。

f.在root账户下⾯调⽤启动命令出错的解决⽅案

- f.1 当使⽤root账户调⽤启动命令出现错误信息，错误提示信息如下:


![image 2](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile2.png>)

359161-201604191714707-1540706804.png

- f.2 为什么会这样呢？这是因为处于系统安装考虑的设置，由于Elasticsearch可以接收⽤户

输⼊的脚本并且执⾏，为了系统安全考虑，不允许root账号启动，所以建议给Elasticsearch单独创建 ⼀个⽤户来运⾏Elasticsearch。

- f.3 创建elasticsearch⽤户组以及elasticsearch⽤户，命令如下： groupadd elasticsearch useradd elasticsearch(⽤户名) -g elasticsearch(组名) -p elasticsearch(密码)

- f.4 更改Elasticsearch⽂件夹以及内部⽂件的所属⽤户以及组为elasticsearch，修改完成之

后如图所示：

chown -R elasticsearch:elasticsearch elasticsearch

- f.5 切换到elasticsearch⽤户下，再次执⾏启动命令，如图所示，则说明启动成功


![image 3](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile3.png>)

359161-2016041917121782-7485961.png

![image 4](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile4.png>)

359161-20160419171306976-124850612.png

- g.Elasticsearch后端启动命令为：./bin/elasticsearch -d

- h.安装完成后使⽤IP访问 h.1 当安装完成之后我们当然希望他在其他局域⽹内通过IP可以访问，可是执⾏：


http://19 2.168.37.137:9200/

，始终不能连接成功，⽽且centos下⽤localhost、127.0.0.1都能够连接成 功。

- h.2 这时候我们就需要修改配置⽂件了，⾸先使⽤ifconfig查询你的linux的IP是多少，得到

IP。

- h.3 跳转到Elasticsearch的config配置⽂件下，使⽤vim打开elasticsearch.yml，找到⾥

⾯的"network.host",将其改为你刚才查询得到的IP,保存。 cd elasticsearch/config/ vim elasticsearch.yml

- h.4 重启ElasticSearch，然后使⽤ 访问,如果连接不成功

则需要考虑是不是端⼝的原因，配置端⼝，重启防⽕墙即可。

- h.5 使⽤ 访问，访问结果如图所示：，则说明


http://192.168.37.137:9200/

http://192.168.37.137:9200/

ElasticSearch安装成功。'

![image 5](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile5.png>)

359161-2016041917134628-907845039.png

h.6 Elasticsearch安装完成之后，希望能有⼀个可视化的环境来操作它，那么下来配置： Elasticsearch Head

i. 如果是使⽤命令./bin/elasticsearch来启动的Elasticsearch，如果想要停⽌Elasticsearch的 执⾏，则直接按住键盘Ctrl+C则会停⽌，停⽌之后你在浏览器中再次测试发现已不能操作。

回到顶部

### 4.ElasticSearch_Head配置

- a.Elasticsearch Head是集群管理、数据可视化、增删改查、查询语句可视化⼯具，它的安装⽅

式有两种，⼀种是使⽤命令安装，⼀种是下载包安装。

- b.命令安装


- b.1 cd /usr/local/kencery/elasticsearch/elasticsearch

- b.2 ./bin/plugin -install mobz/elasticsearch-head(*) 提示错误，错误信息是：ERROR: unknown command [-install]. Use [-


h] option to list available commands，这是因为Elasticsearch在2.0以上的版本将-install变成 了install。

- b.3 故⽽执⾏命令 ./bin/plugin install mobz/elasticsearch-head即可。

- b.4 详细信息请看： 下⾯的README.md


https://github.com/mobz/elasticsearch-head

⽂件。 c.下载包安装

- c.1 在准备下载包的时候我们已经将包下载到电脑本地了，所以讲下载下来的包

(elasticsearch-head-master)解压elasticsearch-head-master⽂件夹。

- c.2 在Elasticsearch的安装的plugin下创建⽬录head cd /usr/local/kencery/elasticsearch/elasticsearch/plugins/ mkdir head

- c.3 跳转到head⽂件夹下，将刚才解压的elasticsearch-head-master⽂件夹下的所有⽂件


拷⻉到head⽬录下，如图所示：

![image 6](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile6.png>)

359161-20160419171504851-97426289.png

- c.4 重新启动ElasticSearch，使⽤ 访问浏


http://192.168.37.137:9200/_plugin/head/

览器，如图所示，则说明安装成功。

![image 7](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile7.png>)

359161-20160419171520632-203549277.png

d.安全问题

如图就可以看出，该插件可以对数据进⾏任何增删改查，所以不建议在正式环境中使⽤它， 如果使⽤，也必须限制规定的IP能够使⽤。

回到顶部

### 5.分词插件配置

- a. IK Analyzer是⼀个开源的，基于Java语⾔开发的轻量级的中⽂分词⼯具包，最初的时候，它

是以开源项⽬Lucene为应⽤主体的，结合词典分词和⽂法分析算法的中⽂分词组件，从3.0版本之 后，IK逐渐成为⾯向java的公⽤分词组件，独⽴于Lucene项⽬，同时提供了对Lucene的默认优化实 现，IK实现了简单的分词 歧义排除算法，标志着IK分词器从单纯的词典分词向模拟语义分词衍化

- b. 当安装完Elasticsearch之后，默认已经含有⼀个分词法，就是standard，这个分词法对英⽂


的⽀持还可以，但是对中⽂的⽀持⾮常差劲，如图所示：

http://192.168.37.137:9200/_analyze? analyzer=standard

&pretty=true&text=helloworld,%E6%AC%A2%E8%BF%8E%E6%82 %A8

![image 8](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile8.png>)

359161-2016041917160191-150207936.png

c.安装IK分词法。

- c.1 ⾸先通过Git将源码下载下来，打开git客户端输⼊命令：git clone

https://github.com/medcl/elasticsearch-analysis-ik，如果没有安装git,则直接下载zip包。

- c.2 下载之后进⼊到下载的⽂件夹下，如图所示：

- c.3 因为其源码使⽤的maven开发，故⽽使⽤maven编译项⽬，如果没有安装maven，参


![image 9](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile9.png>)

359161-2016041917163291-205770 62.png

http://www.cnblogs.com/hanyinglong/p/5030907.html

考博客安装： ，命令提示符以管理员的 身份运⾏，如图所示：

![image 10](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile10.png>)

359161-201604191716 585-706086071.png

编译成功在下⾯会提示Succes。

- c.4 打开编译后的target\releases，解压压缩包，然后进⼊解压的压缩包⾥⾯可以看到⼏个


jar包和配置⽂件。

- d.在Elasticsearch的安装的plugin下创建⽂件夹ik cd /usr/local/kencery/elasticsearch/elasticsearch/plugins/ mkdir ik

- e. 跳转到ik⽂件夹下，将c.4中所说的⽂件拷⻉到ik⽂件夹下，如图所示：

- f. 重新启动ElasticSearch，使⽤ 访问浏览器，如果分


![image 11](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile11.png>)

359161-20160419171750523-8450820.png

http://192.168.37.137:9200/_analyze?analyzer=ik&pret ty=true&text=helloworld,%E6%AC%A2%E8%BF%8E%E6%82%A8

词，则说明配置成功。

![image 12](<Linux--Elasticsearch初步使用(安装、Head配置、分词器配置).note_images/imageFile12.png>)

359161-2016041917180763-76259243.png

每天⼀点点，都是进步
