htps:/ w.jianshu.com/p/b1e3062af7d

# docker官⽹

https://hub.docker.com/_/elasticsearch https://hub.docker.com/_/kibana https://hub.docker.com/_/logstash

# ELK官⽹

可以通过官⽹去了解es集群部署的⽅法

https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html

# 基础镜像配置

# ELK官⽹镜像 docker pull docker.elastic.co/elasticsearch/elasticsearch:7.7.0 docker pull bolingcavalry/elasticsearch-head:6

注意事项

- 1.
- 2.


开发环境下载docker官⽹或者elastic官⽅仓库⾥的镜像都可以。 运维环境⼤咖们推荐不要下载docker官⽅的镜像，最好使⽤ elastic官⽅仓库⾥的镜像。

docker官⽹镜像

docker pul elasticsearch:7.7.0 docker pul kibana:7.7.0 docker pul bolingcavalry/elasticsearch-head:6

# 安装前置条件

⽂件创建数

修改Linux系统的限制配置，将⽂件创建数修改为6536个 ：

- 1）修改系统中允许应⽤最多创建多少⽂件等的限制权限。Linux默认来说，⼀般限制应⽤最多创建的⽂ 件是6535个。但是ES⾄少需要6536的⽂件创建数的权限。
- 2）修改系统中允许⽤户启动的进程开启多少个线程。默认的Linux限制rot⽤户开启的进程可以开启任 意数量的线程，其他⽤户开启的进程可以开启1024个线程。必须修改限制数为4096+。因为ES⾄少需


要4096的线程池预备。

vi /etc/security/limits.conf #新增如下内容在limits.conf⽂件中 es soft nofile 65536 es hard nofile 65536 es soft nproc 4096 es hard nproc 4096 系统控制权限

修改系统控制权限，ElasticSearch需要开辟⼀个6536字节以上空间的虚拟内存。Linux默认不允许任 何⽤户和应⽤程序直接开辟这么⼤的虚拟内存。

vi /etc/sysctl.conf

添加参数:新增如下内容在sysctl.conf⽂件中，当前⽤户拥有的内存权限⼤⼩ vm.max_map_count=262144

重启⽣效:让系统控制权限配置⽣效 sysctl -p

# 试运⾏

docker run -itd --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:7.7.0 docker cp elasticsearch:/usr/share/elasticsearch/config/elasticsearch.yml /data

elasticsearch.yml配置

cluster.name: "docker-cluster" network.host: 0.0.0.0 http.cors.enabled: true http.cors.allow-origin: "*"

# ⾃作镜像

Dockfile配置 FROM elasticsearch:7.7.0 # 作者信息 MAINTAINER elasticsearch-zh from date UTC by Asia/Shanghai "laosiji@lagou.com" ENV TZ Asia/Shanghai COPY elasticsearch.yml /usr/share/elasticsearch/config/ 制作镜像 docker build -t lagou/elasticsearch:7.7.0 .

# docker-compose

挂载⽬录授权 mkdir -p /data/elasticsearch chmod 777 -R /data/elasticsearch 启动服务 docker-compose up -d

# 访问测试

直接输⼊集群的地址验证，如下：

http://192.168.198.100:9200 http://192.168.198.100:9100 http://192.168.198.100:5601

# ik分词器

官⽹地址 https://github.com/medcl/elasticsearch-analysis-ik 安装分词器

cd /data/elasticsearch/plugins mkdir -p ik unzip elasticsearch-analysis-ik-7.7.0.zip 重启es docker-compose restart

作者：david161 链接：htps:/ w.jianshu.com/p/b1e3062af7d 来源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

