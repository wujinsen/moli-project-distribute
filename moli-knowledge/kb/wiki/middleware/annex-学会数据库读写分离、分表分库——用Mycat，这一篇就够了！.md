---
title: 学会数据库读写分离、分表分库——用Mycat，这一篇就够了！.note（原文插图 annex）
slug: annex-学会数据库读写分离、分表分库——用Mycat，这一篇就够了！
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/中间件/MyCat/学会数据库读写分离、分表分库——用Mycat，这一篇就够了！.note.md
related: [消息队列]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/joyl e/p/7513038.html

# 安装

Mycat官⽹： 可以了解下Mycat的背景和应⽤情况，这样使⽤起来⽐较有信⼼。 Mycat下载地址： 官⽹有个⽂档，属于详细的介绍，初次⼊⻔，看起来⽐较花时间。 下载： 建议⼤家选择 1.6-RELEASE 版本，毕竟是⽐较稳定的版本。 安装： 根据不同的系统选择不同的版本。包括linux、windows、mac,作者考虑还是⾮常周全的，当然，也有 源码版的。（ps:源码版的下载后，只要配置正确，就可以正常运⾏调试，这个赞⼀下。）

http://www.mycat.io/

http://dl.mycat.io/

![image 1](assets/imageFile1.png)

Mycat的安装其实只要解压下载的⽬录就可以了，⾮常简单。 安装完成后，⽬录如下：

<table>
  <tr>
    <th>⽬录</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>bin</td>
    <td>mycat命令，启动、重启、停⽌等</td>
  </tr>
  <tr>
    <td>catlet</td>
    <td>catlet为Mycat的⼀个扩展功能</td>
  </tr>
  <tr>
    <td>conf</td>
    <td>Mycat 配置信息,重点关注</td>
  </tr>
  <tr>
    <td>lib</td>
    <td>Mycat引⽤的jar包，Mycat是java开发的</td>
  </tr>
  <tr>
    <td>logs</td>
    <td>⽇志⽂件，包括Mycat启动的⽇志和运⾏的⽇志。</td>
  </tr>
</table>


# 配置

Mycat的配置⽂件都在conf⽬录⾥⾯，这⾥介绍⼏个常⽤的⽂件：

<table>
  <tr>
    <th>⽂件</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>server.xml</td>
    <td>Mycat的配置⽂件，设置账号、参数等</td>
  </tr>
  <tr>
    <td>schema.xml</td>
    <td>Mycat对应的物理数据库和数据库表的配置</td>
  </tr>
  <tr>
    <td>rule.xml</td>
    <td>Mycat分⽚（分库分表）规则</td>
  </tr>
</table>


Mycat的架构其实很好理解，Mycat是代理，Mycat后⾯就是物理数据库。和Web服务器的Nginx类似。 对于使⽤者来说，访问的都是Mycat，不会接触到后端的数据库。 我们现在做⼀个主从、读写分离，简单分表的示例。结构如下图：

![image 2](assets/imageFile2.png)

<table>
  <tr>
    <th>服务器</th>
    <th>IP</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>Mycat</td>
    <td>192.168.0.2</td>
    <td>mycat服务器，连接数据库时，连接 此服务器</td>
  </tr>
  <tr>
    <td>database1</td>
    <td>192.168.0.3</td>
    <td>物理数据库1，真正存储数据的数据 库</td>
  </tr>
  <tr>
    <td>database2</td>
    <td>192.168.0.4</td>
    <td>物理数据库2，真正存储数据的数据 库</td>
  </tr>
</table>


Mycat作为主数据库中间件，肯定是与代码弱关联的，所以代码是不⽤修改的，使⽤Mycat后，连接数 据库是不变的，默认端⼝是8066。连接⽅式和普通数据库⼀样，如：jdbc:mysql://192.168.0.2:8066/ server.xml 示例

<user name="test">

<property name="password">test</property> <property name="schemas">lunch</property> <property name="readOnly">false</property>

<!-- 表级 DML 权限设置 --> <!-<privileges check="false">

<schema name="TESTDB" dml="0110" >

- <table name="tb01" dml="0000"></table>

- <table name="tb02" dml="1111"></table>


</schema> </privileges>

--> </user>

重点关注下⾯这段，其他默认即可。

<table>
  <tr>
    <th>参数</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>user</td>
    <td>⽤户配置节点</td>
  </tr>
  <tr>
    <td>-name</td>
    <td>登录的⽤户名，也就是连接Mycat的⽤户名</td>
  </tr>
  <tr>
    <td>-pasword</td>
    <td>登录的密码，也就是连接Mycat的密码</td>
  </tr>
  <tr>
    <td>-schemas</td>
    <td>数据库名，这⾥会和schema.xml中的配置关联，多个⽤ 逗号分开，例如需要这个⽤户需要管理两个数据库</td>
  </tr>
  <tr>
    <td>-privileges</td>
    <td>db1,db2，则配置db1,dbs<br><br>配置⽤户针对表的增删改查的权限，具体⻅⽂档吧</td>
  </tr>
</table>


我这⾥配置了⼀个账号test 密码也是test,针对数据库lunch,读写权限都有，没有针对表做任何特殊的权 限。 schema.xml schema.xml是最主要的配置项，⾸先看我的配置⽂件。

</schema>

<!-- 分⽚配置 -->

- <dataNode name="dn1" dataHost="test1" database="lunch" />

- <dataNode name="dn2" dataHost="test2" database="lunch" />


<!-- 物理数据库配置 --> <dataHost name="test1" maxCon="1000" minCon="10" balance="0" writeType="0" dbType="mysql"

dbDriver="native"> <heartbeat>select user();</heartbeat> <writeHost host="hostM1" url="192.168.0.2:3306" user="root" password="123456"> </writeHost>

</dataHost>

<dataHost name="test2" maxCon="1000" minCon="10" balance="0" writeType="0" dbType="mysql"

dbDriver="native"> <heartbeat>select user();</heartbeat> <writeHost host="hostS1" url="192.168.0.3:3306" user="root" password="123456"> </writeHost>

</dataHost>

</mycat:schema>

<table>
  <tr>
    <th>参数</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>schema</td>
    <td>数 据 库 设 置 ， 此 数 据 库 为 逻 辑 数 据 库 ， name与 对应</td>
  </tr>
  <tr>
    <td>dataNode</td>
    <td>server.xml中schema<br><br>分⽚信息，也就是分库相关配置</td>
  </tr>
  <tr>
    <td>dataHost</td>
    <td>物理数据库，真正存储数据的数据库</td>
  </tr>
</table>


每个节点的属性逐⼀说明： schema:

<table>
  <tr>
    <th>属性</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>name</td>
    <td>逻辑数据库名，与server.xml中的schema对应</td>
  </tr>
  <tr>
    <td>checkSQLschema</td>
    <td>数据库前缀相关设置，建议看⽂档，这⾥暂时设为folse</td>
  </tr>
  <tr>
    <td>sqlMaxLimit</td>
    <td>select 时默认的limit，避免查询全表</td>
  </tr>
</table>


table:

<table>
  <tr>
    <th>属性</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>name</td>
    <td>表名，物理数据库中表名</td>
  </tr>
  <tr>
    <td>dataNode</td>
    <td>表存储到哪些节点，多个节点⽤逗号分隔。节点为下⽂</td>
  </tr>
  <tr>
    <td>primaryKey</td>
    <td>dataNode设置的name<br><br>主键字段名，⾃动⽣成主键时需要设置</td>
  </tr>
  <tr>
    <td>autoIncrement</td>
    <td>是否⾃增</td>
  </tr>
  <tr>
    <td>rule</td>
    <td>分⽚规则名，具体规则下⽂rule详细介绍</td>
  </tr>
</table>


dataNode

<table>
  <tr>
    <th>属性</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>name</td>
    <td>节点名，与table中dataNode对应</td>
  </tr>
  <tr>
    <td>datahost</td>
    <td>物理数据库名，与datahost中name对应</td>
  </tr>
  <tr>
    <td>database</td>
    <td>物理数据库中数据库名</td>
  </tr>
</table>


dataHost

<table>
  <tr>
    <th>属性</th>
    <th>说明</th>
  </tr>
  <tr>
    <td>name</td>
    <td>物理数据库名，与dataNode中dataHost对应</td>
  </tr>
  <tr>
    <td>balance</td>
    <td>均衡负载的⽅式</td>
  </tr>
  <tr>
    <td>writeType</td>
    <td>写⼊⽅式</td>
  </tr>
  <tr>
    <td>dbType</td>
    <td>数据库类型</td>
  </tr>
  <tr>
    <td>heartbeat</td>
    <td>⼼跳检测语句，注意语句结尾的分号要加。</td>
  </tr>
</table>


# 应⽤场景

数据库分表分库 配置如下：

</schema>

<!-- 分⽚配置 -->

<dataNode name="dn1" dataHost="test1" database="lunch" /> <dataNode name="dn2" dataHost="test2" database="lunch" />

<!-- 物理数据库配置 --> <dataHost name="test1" maxCon="1000" minCon="10" balance="0" writeType="0" dbType="mysql"

dbDriver="native"> <heartbeat>select user();</heartbeat> <writeHost host="hostM1" url="192.168.0.2:3306" user="root" password="123456"> </writeHost>

</dataHost>

<dataHost name="test2" maxCon="1000" minCon="10" balance="0" writeType="0" dbType="mysql"

dbDriver="native"> <heartbeat>select user();</heartbeat> <writeHost host="hostS1" url="192.168.0.3:3306" user="root" password="123456"> </writeHost>

</dataHost>

</mycat:schema>

我在192.168.0.2、192.168.0.3均有数据库lunch。 lunchmenu、restaurant、userlunch、users这些表都只写⼊节点dn1，也就是192.168.0.2这个服务， ⽽dictionary写⼊了dn1、dn2两个节点，也就是192.168.0.2、192.168.0.3这两台服务器。分⽚的规则 为：mod-long。 主要关注rule属性，rule属性的内容来源于rule.xml这个⽂件，Mycat⽀持10种分表分库的规则，基本能 满⾜你所需要的要求，这个必须赞⼀个，其他数据库中间件好像都没有这么多。

table中的rule属性对应的就是rule.xml⽂件中tableRule的name,具体有哪些分表和分库的实现，建议还 是看下⽂档。我这⾥选择的mod-long就是将数据平均拆分。因为我后端是两台物理库，所以rule.xml中 mod-long对应的function count为2，⻅下⾯部分代码：

<tableRule name="mod-long">

<rule> <columns>id</columns> <algorithm>mod-long</algorithm>

</rule> </tableRule>

<function name="mod-long" class="io.mycat.route.function.PartitionByMod"> <!-- how many data nodes --> <property name="count">2</property>

</function>

数据库读写分离 配置如下：

<?xml version="1.0"?> <!DOCTYPE mycat:schema SYSTEM "schema.dtd"> <mycat:schema xmlns:mycat="http://io.mycat/">

<!-- 数据库配置，与server.xml中的数据库对应 -->

<schema name="lunch" checkSQLschema="false" sqlMaxLimit="100"> <table name="lunchmenu" dataNode="dn1" /> <table name="restaurant" dataNode="dn1" /> <table name="userlunch" dataNode="dn1" /> <table name="users" dataNode="dn1" /> <table name="dictionary" primaryKey="id" autoIncrement="true" dataNode="dn1" />

</schema>

<!-- 分⽚配置 --> <dataNode name="dn1" dataHost="test1" database="lunch" />

<!-- 物理数据库配置 --> <dataHost name="test1" maxCon="1000" minCon="10" balance="1" writeType="0" dbType="mysql"

dbDriver="native"> <heartbeat>select user();</heartbeat> <writeHost host="hostM1" url="192.168.0.2:3306" user="root" password="123456"> <readHost host="hostM1" url="192.168.0.3:3306" user="root" password="123456"> </readHost> </writeHost>

</dataHost>

</mycat:schema>

这样的配置与前⼀个示例配置改动如下： 删除了table分配的规则,以及datanode只有⼀个 datahost也只有⼀台，但是writehost总添加了readhost,balance改为1，表示读写分离。

以上配置达到的效果就是102.168.0.2为主库，192.168.0.3为从库。 注意：Mycat主从分离只是在读的时候做了处理，写⼊数据的时候，只会写⼊到writehost，需要通过 mycat的主从复制将数据复制到readhost，这个问题当时候我纠结了好久，数据写⼊writehost后， readhost⼀直没有数据，以为是⾃⼰配置的问题，后⾯才发现Mycat就没有实现主从复制的功能，毕竟 数据库本身⾃带的这个功能才是最⾼效稳定的。

⾄ 于 其 他 的 场 景 ， 如 同 时 主 从 和 分 表 分 库 也 是 ⽀ 持 的 了 ， 只 要 了 解 这 个 实 现 以 后 再 去 修 改 配 置 ， 都 是 可 以 实 现 的 。 ⽽ 热 备 及 故 障 专业 官 ⽅ 推 荐 使 ⽤ haproxy配 合 ⼀ 起 使 ⽤ ， ⼤ 家 可 以 试试 。

# 使⽤

Mycat的启动也很简单，启动命令在Bin⽬录：

##启动 mycat start

##停⽌ mycat stop

##重启 mycat restart

如果在启动时发现异常，在logs⽬录中查看⽇志。

wrapper.log 为程序启动的⽇志，启动时的问题看这个 mycat.log 为脚本执⾏时的⽇志，SQL脚本执⾏报错后的具体错误内容,查看这个⽂件。mycat.log是 最新的错误⽇志，历史⽇志会根据时间⽣成⽬录保存。

mycat启动后，执⾏命令不成功，可能实际上配置有错误，导致后⾯的命令没有很好的执⾏。 Mycat带来的最⼤好处就是使⽤是完全不⽤修改原有代码的，在mycat通过命令启动后，你只需要将数 据库连接切换到Mycat的地址就可以了。如下⾯就可以进⾏连接了：

mysql -h192.168.0.1 -P8806 -uroot -p123456

连接成功后可以执⾏sql脚本了。 所以，可以直接通过sql管理⼯具（如：navicat、datagrip）连接，执⾏脚本。我⼀直⽤datagrip来进⾏ ⽇常简单的管理，这个很⽅便。 Mycat还有⼀个管理的连接，端⼝号是9906.

mysql -h192.168.0.1 -P9906 -uroot -p123456

连接后可以根据管理命令查看Mycat的运⾏情况，当然，喜欢UI管理⽅式的⼈，可以安装⼀个MycatWeb来进⾏管理，有兴趣⾃⾏搜索。 简⽽⾔之，开发中使⽤Mycat和直接使⽤Mysql机会没有差别。

# 常⻅问题

使⽤Mycat后总会遇到⼀些坑，我将⾃⼰遇到的⼀些问题在这⾥列⼀下，希望能与⼤家有共鸣：

Mycat是不是配置以后，就能完全解决分表分库和读写分离问题？

Mycat配合数据库本身的复制功能，可以解决读写分离的问题，但是针对分表分库的问题，不是完美的 解决。或者说，⾄今为⽌，业界没有完美的解决⽅案。

分表分库写⼊能完美解决，但是，不能完美解决主要是联表查询的问题，Mycat⽀持两个表联表的查 询，多余两个表的查询不⽀持。 其实，很多数据库中间件关于分表分库后查询的问题，都是需要⾃⼰ 实现的，⽽且节本都不⽀持联表查询，Mycat已经算做地⾮常先进了。 分表分库的后联表查询问题，⼤家通过合理数据库设计来避免。

Mycat⽀持哪些数据库，其他平台如 .net、PHP能⽤吗？

官⽅说了，⽀持的数据库包括MySQL、SQL Server、Oracle、DB2、PostgreSQL 等主流数据库，很 赞。 尽量⽤Mysql,我试过SQL Server，会有些⼩问题，因为部分语法有点差异。

Mycat ⾮JAVA平台如 .net、PHP能⽤吗？

可以⽤。这⼀点MyCat做的也很棒。

# 参考

《Mycat权威指南》： 官⽹ ：

http://www.mycat.io/document/Mycat_V1.6.0.pdf http://www.mycat.io/
