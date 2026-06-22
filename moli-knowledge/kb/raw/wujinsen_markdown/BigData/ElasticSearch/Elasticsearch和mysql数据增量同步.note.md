# 1.如何实现mysql与elasticsearch的数据同步？

逐条转换为json显然不合适，需要借助第三⽅⼯具或者⾃⼰实现。核⼼功能点：同步增、删、改、查同 步。

# 2、mysql与elasticsearch同步的⽅法有哪些？ 优缺点对⽐？

⽬前该领域⽐较⽜的插件有：

- 1）、elasticsearch-jdbc，严格意义上它已经不是第三⽅插件。已经成为独⽴的第三⽅⼯具。

- 2）、elasticsearch-river-mysql插件

- 3）、go-mysql-elasticsearch（国内作者si dontang）


htps:/gi thub.com/jprante/elasticsearch-jdbc

htps:/github.com/scharon/elasticsearch-river-mysql

htps:/github.com/si dontang/go-mysql-ela sticsearch

1-3同步⼯具/插件对⽐：

go-mysql-elasticsearch仍处理开发不稳定阶段。 为什么选择elasticsearch-jdbc⽽不是elasticsearch-river-mysql插件的原因？（参考：

htp:/stackove rflow.com/questions/23658534/using-elasticsearch-river-mysql-to-stream-data-from-mysql-data base-to-elasticsea

）

- 1）通⽤性⻆度：elasticsearch-jdbc更通⽤，

- 2）版本更新⻆度：elasticsearch-jdbc GitHub活跃度很⾼，最新的版本2.3.3.02016年5⽉28⽇兼容 Elasticsearch2.3.3版本。 ⽽elasticsearch-river-mysql 2012年12⽉13⽇后便不再更新。 综上，选择elasticsearch-jdbc作为mysql同步Elasticsearch的⼯具理所当然。


elasticsearch-jdbc的缺点与不⾜（他⼭之⽯）：

- 1）、go-mysql-elasticsearch作者si dontang在博客提到的： elasticsearch-river-jdbc的功能是很强⼤，但并没有很好的⽀持增量数据更新的问题，它需要对应的表 只增不减，⽽这个⼏乎在项⽬中是不可能办到的。

- 2）、 博主leotse90在博⽂中提到elasticsearch-jdbc的缺点：那就是删除操作不能同步（物理删除）！


htp:/ w.jianshu.com/p/05cf717563c

htp:/leotse90.com/2015/1/1/ElasticSearch

与MySQL数据同步以及修改表结构/ 我截⽌2016年6⽉16⽇没有测试到，不妄加评论。

![image 1](<Elasticsearch和mysql数据增量同步.note_images/imageFile1.png>)

这⾥写图⽚描述

# 3、elasticsearch-jdbc如何使⽤？要不要安 装？

- 3.1 和早期版本不同点

elasticsearch-jdbcV2.3.2.0版本不需要安装。以下笔者使⽤的elasticsearch也是2.3.2测试。 操作系统：CentOS release 6.6 (Final) 看到这⾥，你可能会问早期的版本有什么不同呢？很⼤不同。从我搜集资料来看，不同点如下：

- 1）早期1.x版本，作为插件，需要安装。

- 2）配置也会有不同。


- 3.2 elasticsearch-jdbc使⽤(同步⽅法⼀）

前提：

- 1）elasticsearch 2.3.2 安装成功，测试ok。

- 2）mysql安装成功，能实现增、删、改、查。


可供测试的数据库为test，表为 c，具体信息如下：

mysql> select * from cc;

+----+------------+ | id | name |

+----+------------+

- | 1 | laoyang |
- | 2 | dluzhang |
- | 3 | dlulaoyang |


+----+------------+

- 3 rows in set (0.00 sec)


- 1

- 2

- 3


- 4

- 5

- 6

- 7

- 8

- 9


第⼀步：下载⼯具。 址：

htp:/xbib.org/repository/org/xbib/elasticsearch/importer/elasticsearch-jdbc/2.3.2.0/elasticsear ch-jdbc-2.3.2.0-dist.zip

第⼆步：导⼊Centos。路径⾃⼰定，笔者放到根⽬录下，解压。unzip elasticsearch-jdbc-2.3.2.0dist.zip 第三步：设置环境变量。 [rot@5b9db a148a /]# vi /etc/profile export JDBC_IMPORTER_HOME=/elasticsearch-jdbc-2.3.2.0 使环境变量⽣效： [rot@5b9db a148a /]# source /etc/profile 第四步：配置使⽤。详细参考：

htps:/github.com/jprante/elasticsearch-jdbc

- 1）、根⽬录下新建⽂件夹odbc_es 如下：

[root@5b9dbaaa148a /]# ll /odbc_es/ drwxr-xr-x 2 root root 4096 Jun 16 03:11 logs

-rwxrwxrwx 1 root root 542 Jun 16 04:03 mysql_import_es.sh

- 2）、新建脚本mysql_import_es.sh，内容如下；


[root@5b9dbaaa148a odbc_es]# cat mysql_import_es.sh ’#!/bin/sh

bin=$JDBC_IMPORTER_HOME/bin lib=$JDBC_IMPORTER_HOME/lib echo '{

"type" : "jdbc", "jdbc": { "elasticsearch.autodiscover":true, "elasticsearch.cluster":"my-application", #簇名，详 ⻅：/usr/local/elasticsearch/config/elasticsearch.yml "url":"jdbc:mysql://10.8.5.101:3306/test", #mysql数据库地址 "user":"root", #mysql⽤户名 "password":"123456", #mysql密码 "sql":"select * from cc", "elasticsearch" : {

"host" : "10.8.5.101", "port" : 9300

}, "index" : "myindex", #新的index "type" : "mytype" #新的type } }'| java \

- -cp "${lib}/*" \
- -Dlog4j.configurationFile=${bin}/log4j2.xml \ org.xbib.tools.Runner \ org.xbib.tools.JDBCImporter


- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9

- 10 1


- 12

- 13

- 14

- 15

- 16

- 17

- 18


19 20 21

2

- 23

- 24

- 25


- 3）、为 mysql_import_es.sh 添加可执⾏权限。 [rot@5b9db a148a odbc_es]# chmod a+x mysql_import_es.sh

- 4）执⾏脚本mysql_import_es.sh [rot@5b9db a148a odbc_es]# ./mysql_import_es.sh 第五步：测试数据同步是否成功。 使⽤elasticsearch检索查询：


[root@5b9dbaaa148a odbc_es]# curl -XGET 'http://10.8.5.101:9200/myindex/mytype/_search?pretty' {

"took" : 4, "timed_out" : false, "_shards" : { "total" : 8, "successful" : 8, "failed" : 0 }, "hits" : { "total" : 3, "max_score" : 1.0, "hits" : [ { "_index" : "myindex", "_type" : "mytype",

- "_id" : "AVVXKgeEun6ksbtikOWH", "_score" : 1.0, "_source" : {

- "id" : 1, "name" : "laoyang" } }, { "_index" : "myindex", "_type" : "mytype",

"_id" : "AVVXKgeEun6ksbtikOWI", "_score" : 1.0, "_source" : {

- "id" : 2, "name" : "dluzhang" } }, { "_index" : "myindex", "_type" : "mytype",

"_id" : "AVVXKgeEun6ksbtikOWJ", "_score" : 1.0, "_source" : {

- "id" : 3, "name" : "dlulaoyang" } } ] }




}

- 1

- 2

- 3

- 4

- 5

- 6


- 7

- 8

- 9

- 10


- 1

- 12

- 13

- 14

- 15

- 16

- 17

- 18

- 19

- 20

- 21


- 2

- 23

- 24

- 25

- 26

- 27

- 28

- 29

- 30

- 31

- 32


- 3


- 34

- 35

- 36

- 37

- 38

- 39

- 40

- 41

- 42


出现以上包含mysql数据字段的信息则为同步成功。

# 4、 elasticsearch-jdbc同步⽅法⼆

[root@5b9dbaaa148a odbc_es]# cat mysql_import_es_simple.sh #!/bin/sh

bin=$JDBC_IMPORTER_HOME/bin lib=$JDBC_IMPORTER_HOME/lib

java \

- -cp "${lib}/*" \
- -Dlog4j.configurationFile=${bin}/log4j2.xml \ org.xbib.tools.Runner \ org.xbib.tools.JDBCImporter statefile.json


[root@5b9dbaaa148a odbc_es]# cat statefile.json { "type" : "jdbc", "jdbc": { "elasticsearch.autodiscover":true, "elasticsearch.cluster":"my-application", "url":"jdbc:mysql://10.8.5.101:3306/test", "user":"root", "password":"123456", "sql":"select * from cc", "elasticsearch" : {

"host" : "10.8.5.101", "port" : 9300

}, "index" : "myindex_2", "type" : "mytype_2"

} }

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9

- 10 1


- 20

- 21 2


23 24 25 26 27 28

脚本和json⽂件分开，脚本执⾏前先加载json⽂件。 执⾏⽅式：直接运⾏脚本 ./mysql_import_es_simple.sh 即可。

# 5、Mysql与elasticsearch等价查询

⽬标：实现从表 c中查询id=3的name信息。

- 1）MySQL中sql语句查询： mysql> select * from cc where id=3;

+----+------------+ | id | name |

+----+------------+ | 3 | dlulaoyang |

+----+------------+ 1 row in set (0.00 sec)

- 2）elasticsearch检索：


- 1

- 2

- 3

- 4

- 5

- 6

- 7


[root@5b9dbaaa148a odbc_es]# curl http://10.8.5.101:9200/myindex/mytype/_search?pretty -d ' { "filter" : { "term" : { "id" : "3" } } }' {

"took" : 3, "timed_out" : false, "_shards" : { "total" : 8, "successful" : 8, "failed" : 0 }, "hits" : { "total" : 1, "max_score" : 1.0, "hits" : [ { "_index" : "myindex", "_type" : "mytype", "_id" : "AVVXKgeEun6ksbtikOWJ", "_score" : 1.0, "_source" : { "id" : 3, "name" : "dlulaoyang" } } ] }

}

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9

- 10 1


- 12

- 13


2

- 23

- 24

- 25

- 26

- 27


# 常⻅错误：

错误⽇志位置：/odbc_es/logs ⽇志内容： [rot@5b9db a148a logs]# tail -f jdbc.log [04 03 39,570][INFO ][org.xbib.elasticsearch.helper.client.BaseTransportClient][pol-3-thread-1] after auto-discovery conected to [{5b9db a148a}{aksn2ErNRlWjUECnp_8JmA}{10.8.5.101} {10.8.5.101 930}{master=true}]

- Bug1、[02 46 23,894][EROR][importer.jdbc ][pol-3-thread-1] eror while procesing request: cluster state is RED and not YELOW, from here on, everything wil fail! 原因： you created an index with replicas but you had only one node in the cluster. One way to solve this problem is by alocating them on a second node. Another way is by turning replicas of. 你创建了带副本 replicas 的索引，但是在你的簇中只有⼀个节点。 解决⽅案： ⽅案⼀：允许分配‘它们ʼ到第⼆个节点。


⽅案⼆：关闭副本replicas（⾮常可⾏）。如下：

curl -XPUT 'localhost:9200/_settings' -d ' {

"index" : { "number_of_replicas" : 0 }

}

- 1

- 2

- 3

- 4

- 5

- 6


‘

- Bug2、[13  0 37,137][EROR][importer.jdbc ][pol-3-thread-1] eror while procesing request: no cluster nodes available, check setings {autodiscover=false, client.transport.ignore_cluster_name=false, client.transport.nodes_sampler_interval=5s, client.transport.ping_timeout=5s, cluster.name=elasticsearch, org.elasticsearch.client.transport.NoNodeAvailableException: no cluster nodes available, check 解决⽅案： ⻅上脚本中新增： “elasticsearch.cluster”:”my-aplication”, #簇名，和/usr/local/elasticsearch/config/elasticsearch.yml 簇名保持⼀致。 参考： htp:/stackoverflow.com/questions/194915/geting-an-elasticsearch-cluster-to-gren-cluster-s etup-on-os-x


