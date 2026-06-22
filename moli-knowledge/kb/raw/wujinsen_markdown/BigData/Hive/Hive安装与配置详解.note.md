既然是详解，那么我们就不能只知道怎么安装hive了，下⾯从hive的基本说起，如果你了解了，那么请 直接移步安装与配置 hive是什么 hive安装和配置 hive的测试

# hive

这⾥简单说明⼀下，好对⼤家配置hive有点帮助。hive是建⽴在hadop上的，当然，你如果只搭 建hive也没⽤什么错。说简单⼀点，hadop中的mapreduce调⽤如果⾯向DBA的时候，那么问题也就 显现了，因为不是每个DBA都能明⽩mapreduce的⼯作原理，如果为了管理数据⽽需要学习⼀⻔新的 技术，从现实⽣活中来说，公司⼜需要花钱请更有技术的⼈来了。

开个玩笑，hadop是为了存储数据和计算⽽推⼴的技术，⽽和数据挂钩的也就属于数据库的领域 了，所以hadop和DBA挂钩也就是情理之中的事情，在这个基础之上，我们就需要为了DBA创作适合 的技术。

hive正是实现了这个，hive是要类SQL语句（HiveQL）来实现对hadop下的数据管理。hive属于 数据仓库的范畴，那么，数据库和数据仓库到底有什么区别了，这⾥简单说明⼀下：数据库侧重于 OLTP（在线事务处理），数据仓库侧重OLAP（在线分析处理）；也就是说，例如mysql类的数据库更 侧重于短时间内的数据处理，反之。 ⽆hive：使⽤者 .->mapreduce.->hadop数据（可能需要会mapreduce） 有hive：使⽤者 .->HQL（SQL）->hive.->mapreduce.->hadop数据（只需要会SQL语句）

# hive安装和配置

安装 ⼀：下载hive⸺地址：htp:/miror.bit.edu.cn/apache/hive/

![image 1](<Hive安装与配置详解.note_images/imageFile1.png>)

这⾥以hive-2.1.1为例⼦，如图：

![image 2](<Hive安装与配置详解.note_images/imageFile2.png>)

将hive解压到/usr/local下：

[root@s100 local]# tar -zxvf apache-hive-2.1.1-bin.tar.gz -C /usr/local/

![image 3](<Hive安装与配置详解.note_images/imageFile3.png>)

将⽂件重命名为hive⽂件：

[root@s100 local]# mv apache-hive-2.1.1-bin hive

![image 4](<Hive安装与配置详解.note_images/imageFile4.png>)

修改环境变量/etc/profile：

[root@s100 local]# vim /etc/profile

- 1 #hive

- 2 export HIVE_HOME=/usr/local/hive

- 3 export PATH=$PATH:$HIVE_HOME/bin


![image 5](<Hive安装与配置详解.note_images/imageFile5.png>)

执⾏source /etc.profile： 执⾏hive-version

[root@s100 local]# hive --version

有hive的版本显现，安装成功！ 配置

[root@s100 conf]# cd /usr/local/hive/conf/

修改hive-site.xml：

![image 6](<Hive安装与配置详解.note_images/imageFile6.png>)

这⾥没有，我们就以模板复制⼀个：

[root@s100 conf]# cp hive-default.xml.template hive-site.xml [root@s100 conf]# vim hive-site.xml

- 1.配置hive-site.xml（第5点的后⾯有⼀个单独的hive-site.xml配置⽂件，这个如果有疑问可以⽤后⾯的 配置⽂件，更容易明⽩） 主要是mysql的连接信息（在⽂本的最开始位置）


<table>
  <tr>
    <th>![image 7](<Hive安装与配置详解.note_images/imageFile7.png>)</th>
  </tr>
</table>


<?xml version="1.0" encoding="UTF-8" standalone="no"?> <?xml-stylesheet type="text/xsl" href="configuration.xsl"?><!--

Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

--><configuration>

<!-- WARNING!!! This file is auto generated for documentation purposes ONLY! --> <!-- WARNING!!! Any changes you make to this file will be ignored by Hive. --> <!-- WARNING!!! You must make your changes in hive-site.xml instead. --> <!-- Hive Execution Parameters -->

<!-- 插⼊⼀下代码 -->

<property> <name>javax.jdo.option.ConnectionUserName</name>⽤户名（这4是新添加的，记住删除配置⽂件原有的哦！） <value>root</value>

</property> <property>

<name>javax.jdo.option.ConnectionPassword</name>密码 <value>123456</value>

</property>

<property> <name>javax.jdo.option.ConnectionURL</name>mysql <value>jdbc:mysql://192.168.1.68:3306/hive</value>

</property> <property>

<name>javax.jdo.option.ConnectionDriverName</name>mysql驱动程序 <value>com.mysql.jdbc.Driver</value>

</property> <!-- 到此结束代码 -->

<property> <name>hive.exec.script.wrapper</name> <value/> <description/>

</property>

<table>
  <tr>
    <th>![image 8](<Hive安装与配置详解.note_images/imageFile8.png>)</th>
  </tr>
</table>


![image 9](<Hive安装与配置详解.note_images/imageFile9.png>)

## 2.复制mysql的驱动程序到hive/lib下⾯（这⾥已经拷⻉好了） [root@s100 lib]# ll mysql-connector-java-5.1.18-bin.jar

-rw-r--r-- 1 root root 789885 1⽉ 4 01:43 mysql-connector-java-5.1.18-bin.jar

## 3.在mysql中hive的schema（在此之前需要创建mysql下的hive数据库）

- 1 [root@s100 bin]# pwd

- 2 /usr/local/hive/bin

- 3 [root@s100 bin]# schematool -dbType mysql -initSchema


## 4.执⾏hive命令 [root@localhost hive]# hive

![image 10](<Hive安装与配置详解.note_images/imageFile10.png>)

成功进⼊hive界⾯，hive配置完成

## 5.查询mysql（hive这个库是在 schematool -dbType mysql -initSchema 之前创建的！）

<table>
  <tr>
    <th>![image 11](<Hive安装与配置详解.note_images/imageFile11.png>)</th>
  </tr>
</table>


- 1 [root@localhost ~]# mysql -uroot -p123456

- 2 Welcome to the MySQL monitor. Commands end with ; or \g.

- 3 Your MySQL connection id is 10

- 4 Server version: 5.1.73 Source distribution

- 5

- 6 Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.

- 7

- 8 Oracle is a registered trademark of Oracle Corporation and/or its

- 9 affiliates. Other names may be trademarks of their respective

- 10 owners.

- 11

- 12 Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

- 13

- 14 mysql> use hive

- 15 Reading table information for completion of table and column names

- 16 You can turn off this feature to get a quicker startup with -A

- 17

- 18 Database changed

- 19 mysql> show tables;

- 20 +---------------------------+

- 21 | Tables_in_hive |

- 22 +---------------------------+

- 23 | AUX_TABLE |

- 24 | BUCKETING_COLS |

- 25 | CDS |

- 26 | COLUMNS_V2 |

- 27 | COMPACTION_QUEUE |

- 28 | COMPLETED_COMPACTIONS |


<table>
  <tr>
    <th>![image 12](<Hive安装与配置详解.note_images/imageFile12.png>)</th>
  </tr>
</table>


备注 （这⾥不计⼊正⽂不要重复配置hive-site.xml） 配置⽂件hive-site.xml 这⾥不得不说⼀下，如果你的 schematool -dbType mysql -initSchema 并没有执⾏成功怎么办，⼩博主昨 天在这卡了⼀天，最后根据伟⼤的百度和hive官⽅⽂档，直接写了⼀个hive-site.xml配置⽂本：

<table>
  <tr>
    <th>![image 13](<Hive安装与配置详解.note_images/imageFile13.png>)</th>
  </tr>
</table>


<?xml version="1.0" encoding="UTF-8" standalone="no"?> <?xml-stylesheet type="text/xsl" href="configuration.xsl"?> <configuration>

<property> <name>javax.jdo.option.ConnectionURL</name> <value>jdbc:mysql://localhost:3306/hahive</value>（mysql地址localhost）

</property>

<property> <name>javax.jdo.option.ConnectionDriverName</name>（mysql的驱动） <value>com.mysql.jdbc.Driver</value>

</property>

<property> <name>javax.jdo.option.ConnectionUserName</name>（⽤户名） <value>root</value>

</property>

<property> <name>javax.jdo.option.ConnectionPassword</name>（密码） <value>123456</value>

</property>

<property> <name>hive.metastore.schema.verification</name> <value>false</value>

</property> </configuration>

<table>
  <tr>
    <th>![image 14](<Hive安装与配置详解.note_images/imageFile14.png>)</th>
  </tr>
</table>


那我们做这些事⼲什么的呢，下⾯⼩段测试⼤家感受⼀下

# hive测试：

备注：这⾥是第⼆个配置⽂件的演示：所以数据库名称是hahive数据库！

- 1.需要知道现在的hadop中的HDFS存了什么 [root@localhost conf]# hadoop fs -lsr /

- 2.进⼊hive并创建⼀个测试库和测试表 [root@localhost conf]# hive

创建库：

- 1 hive> create database hive_1;

- 2 OK

- 3 Time taken: 1.432 seconds 显示库：


- 1 hive> show databases;

- 2 OK

- 3 default

- 4 hive_1

- 5 Time taken: 1.25 seconds, Fetched: 2 row(s) 创建库成功！


- 3.查询⼀下HDFS有什么变化


![image 15](<Hive安装与配置详解.note_images/imageFile15.png>)

![image 16](<Hive安装与配置详解.note_images/imageFile16.png>)

- 多了⼀个库hive_1 娜莫喔们的mysql下的hahive库有什么变化
- 4.在hive_1下创建⼀个表hive_01


<table>
  <tr>
    <th>1</th>
    <th>mysql> use hahive;</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br></th>
    <th>mysql> select * from DBS;<br><br>+-------+-----------------------+-----------------------------------------------+---------+-----------+------------+<br><br>| DB_ID | DESC | DB_LOCATION_URI | NAME | OWNER_NAME | OWNER_TYPE |<br><br>+-------+-----------------------+-----------------------------------------------+---------+-----------+------------+<br><br>| 1 | Default Hive database | hdfs://localhost/user/hive/warehouse | default | public | ROLE |<br><br>| 6 | NULL | hdfs://localhost/user/hive/warehouse/hive_1.db | hive_1 | root | USER |<br><br>+-------+-----------------------+-----------------------------------------------+---------+-----------+------------+ 2 rows in set (0.00 sec)<br><br></th>
  </tr>
</table>


![image 17](<Hive安装与配置详解.note_images/imageFile17.png>)

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br><br>9<br><br>10<br><br>11<br></th>
    <th>hive> use hive_1; OK<br><br>Time taken: 0.754 seconds hive> create tablehive_01 (id int,name string); OK Time taken: 2.447 seconds<br><br>hive>OK show tables; hive_01 （表创建成功） Time taken: 0.31 seconds, Fetched: 2 row(s) hive><br><br></th>
  </tr>
</table>


HDFS下的情况：

![image 18](<Hive安装与配置详解.note_images/imageFile18.png>)

mysql下：

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br></th>
    <th>mysql> select * from TBLS;<br><br>+--------+-------------+-------+-----------------+-------+-----------+-------+----------+--------------+--------------------+-------------------+<br><br>| TBL_ID | CREATE_TIME | DB_ID | LAST_ACCESS_TIME | OWNER | RETENTION | SD_ID | TBL_NAME | TBL_TYPE | VIEW_EXPANDED_TEXT | VIEW_ORIGINAL_TEXT |<br><br>+--------+-------------+-------+-----------------+-------+-----------+-------+----------+--------------+--------------------+-------------------+<br><br>| 6 | 1514286051 | 6 | 0 | root | 0 | 6 | hive_01 | MANAGED_TABLE | NULL | NULL |<br><br>+--------+-------------+-------+-----------------+-------+-----------+-------+----------+--------------+--------------------+-------------------+ 2 rows in set (0.00 sec)<br><br></th>
  </tr>
</table>


娜莫在web端是什么样⼦的呢！

![image 19](<Hive安装与配置详解.note_images/imageFile19.png>)

总的来说，hive其实就和mysql差不多呢！那么后⾯就不说了 最后，浏览别⼈博客的时候都会有版权声明，感觉好6的样⼦，⼩博主以后也写⼀段╭(╯^╰)╮

版权声明： 本⽂作者：魁·帝⼩仙

htp:/ w.cnblogs.com/dxblog/p/8193967.html

博⽂地址： 欢迎对⼩博主的博⽂内容批评指点，如果问题，可评论或邮件联系（23528250@q.com） 欢迎转载，转载请在⽂章⻚⾯明显位置给出原⽂链接，谢谢

