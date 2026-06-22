推荐 logstash-input-jdbc

ElasticSearch同步Mysql的插件选择了elasticsearch-jdbc,理由是活跃度⾼，持续更新,最新版本兼容 elasticsearch-2.3.3.

# ⼀、下载

下载地址： 下载后解压,⾥⾯有bin、lib2个⽬录.

https://github.com/jprante/elasticsearch-jdbc

![image 1](<ElasticSearch同步MySql.note_images/imageFile1.png>)

这⾥写图⽚描述

# mysql配置">⼆、mysql配置

确保mysql能⽤，在mysql中新建⼀个test

数据库

?

<table>
  <tr>
    <th>1</th>
    <th><code clas="hljs livecodeserver">mysql>create</th>
  </tr>
</table>


database test；</code>

新建⼀张user表

?

<table>
  <tr>
    <th>1</th>
    <th><code clas="hljs cs">mysql> create table user(id</th>
  </tr>
</table>


int(10) Not nul,name char(10);</code>

插⼊⼏条数据.

?

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br></th>
    <th><code clas="hljs cs">mysql> insert into test values("1","zhangsan");<br><br>sl insert int user lues"2","LiSi"); sl insert int useralues"3","WangWu");</th>
  </tr>
</table>


mysql> insert into user values("4","MaLiu");</code>

查看所有数据：

?

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br></th>
    <th><code clas="hljs ascidoc">mysql> select * from user;<br><br>+ -+ -+ | id | name |<br><br>+ -+ -+<br><br>| 1 | zhangsan |<br>| 2 | LiSi |<br>| 3 | WangWu |<br>| 4 | MaLiu |<br><br><br>+ -+ -+</th>
  </tr>
</table>


4 rows in set (0. 0 sec)</code>

这样mysql中的数据就准备好了.

# 三、导⼊数据

新建⼀个odbc_es⽂件夹,新建mysql_import_es.sh脚本，脚本内容：

?

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12<br>13<br>14<br>15<br>16<br>17 8<br><br><br>19<br>20<br>21<br><br><br>2<br>3<br><br><br>24</th>
    <th><code clas="hljs cmake">bin=/Users/yaopan/Documents/bropen/elasti csearch-jdbc-2.3.2.0/bin lib=/Users/yaopan/Documents/bropen/elasticsearchjdbc-2.3.2.0/lib echo '{<br><br>"type" : "jdbc", "jdbc" : { "easticsearc autodiscover":true, "elasticsearch.cluster":"bropen", "rl" : "jdbc:mysql:/localhost: 306/test", " r" : "rot", "useSL":"true", "pasword" : "123456", "sql" : "select *, id as _id from user", "elasticsearch" : { "host" : "127.0.0.1", "port" : 930 }, "index" : "test", "type" : "user" }<br><br>}' | java \ cp "${lib}/*" \<br><br>-Dlog4j.configurationFile=${bin}/log4j2.xml \ or.bb. o.Runer \</th>
  </tr>
</table>


org.xbib.tols.JDBCImporter</code>

其中bin和lib⽤了绝对路径. 添加可执⾏权限：

?

<table>
  <tr>
    <th>1</th>
    <th><code clas="hljs livecodeserver">chmod a+x</th>
  </tr>
</table>


mysql_import_es.sh </code>

执⾏脚本：

?

<table>
  <tr>
    <th>1</th>
    <th><code clas="hljs avrasm"></th>
  </tr>
</table>


## ./mysql_import_es.sh</code>

![image 2](<ElasticSearch同步MySql.note_images/imageFile2.png>)

这⾥写图⽚描述

报了⼀个SSL连接的警告，没有错误.如果出现ErrZ喎 "/kf/ware/vc/" target="_blank" class="keylink">vcjogQ291bGQgbm90IGZpbmQgb3IgbG9hZCBtYWluIGNsYXNzIG9yZy54YmliLnRvb2xzLlJ1bm5 lcjwvY29kZT7WrsDgtcS07c7zo6y63NPQv8nE3MrHYmluus1saWLCt762s/bP1s7KzOIuPC9wPg0KPHA+sum/tLW8yOu 94bn7o7o8L3A+DQo8cHJlIGNsYXNzPQ=="brush:java;">https://localhost:9200/test/user/_search? pretty head插件中查看：

![image 3](<ElasticSearch同步MySql.note_images/imageFile3.png>)

这⾥写图⽚描述

