Sqoop是⼀个转换⼯具，⽤于在关系型数据库与HDFS之间进⾏数据转 换。强⼤功能⻅下图以下操作就是使⽤sqoop在mysql和hdfs之间转换数 据。

- 1.安装
- 2.重命名配置⽂件

1 mv sqoop-env-template.sh sqoop-env.sh

- 3.修改配置⽂件sqop-env.sh


我们使⽤的版本是sqop-1.4.3.bin_hadop-1.0.0.tar.gz，打算安装在/usr/local⽬录下。 ⾸先就是解压缩，重命名为sqop，然后在⽂件/etc/profile中设置环境变量SQOP_HOME。 把mysql的jdbc驱动mysql-conector-java-5.1.10.jar复制到sqop项⽬的lib⽬录下。

在${SQOP_HOME}/conf中执⾏命令

在conf⽬录下，有两个⽂件sqop-site.xml和sqop-site-template.xml内容是完全⼀样的，不必在 意，我们只关⼼sqop-site.xml即可。

内容如下

- 1 #Set path to where bin/hadoop is available

- 2 export HADOOP_COMMON_HOME=/usr/local/hadoop/

- 3

- 4 #Set path to where hadoop-*-core.jar is available

- 5 export HADOOP_MAPRED_HOME=/usr/local/hadoop

- 6

- 7 #set the path to where bin/hbase is available

- 8 export HBASE_HOME=/usr/local/hbase

- 9

- 10 #Set the path to where bin/hive is available

- 11 export HIVE_HOME=/usr/local/hive

- 12

- 13 #Set the path for where zookeper config dir is

- 14 export ZOOCFGDIR=/usr/local/zk


好了，搞定了，下⾯就可以运⾏了。

# 4.把数据从mysql导⼊到hdfs中

在mysql中数据库webdemo中有⼀张表是act_user，表中的数据如下图所示

![image 1](<sqoop使用.note_images/imageFile1.png>)

现在我们要做的是把act_user中的数据导⼊到hdfs中，执⾏命令如下

sqop #sqop命令 import #表示导⼊

- -conect jdbc:mysql:/ip: 306/sqop #告诉jdbc，连接mysql的url
- -username rot #连接mysql的⽤户名
- -pasword admin #连接mysql的密码
- -tablea #从mysql导出的表名称
- -fields-terminated-by '\t' #指定输出⽂件中的⾏的字段分隔符
- -m 1 #复制过程使⽤1个map作业


以上的命令中后⾯的 #部分是注释，执⾏的时候需要删掉；另外，命令的所有内容不能换⾏，只能⼀ ⾏才能执⾏。以下操作类似。 该命令执⾏结束后，观察hdfs的⽬录/user/{USER_NAME}，下⾯会有⼀个⽂件夹是 a，⾥⾯有个⽂件 是part-m- 0。该⽂件的内容就是数据表 a的内容，字段之间是使⽤制表符分割的。

## sqoop import -connect jdbc:mysql://192.168.56.10:3306/webdemo --username root --password admin --query "SELECT * FROM acct_user WHERE \$CONDITIONS AND id='1'" -m 1 -target-dir /sqoop/test2

- 5.把数据从hdfs导出到mysql中 把上⼀步导⼊到hdfs的数据导出到mysql中。我们已知该⽂件有两个字段，使⽤制表符分隔的。那么， 我们现在数据库test中创建⼀个数据表叫做 b，⾥⾯有两个字段。然后执⾏下⾯的命令 sqop export #表示数据从hive复制到mysql中


- -conect jdbc:mysql:/192.168.1.13  306/test
- -username rot
- -pasword admin
- -table b #mysql中的表，即将被导⼊的表名称
- -export-dir '/user/rot/a/part-m- 0' #hive中被导出的⽂件
- -fields-terminated-by '\t' #hive中被导出的⽂件字段的分隔符


命令执⾏完后，再去观察表 b中的数据，是不是已经存在了！

## sqoop export -connect jdbc:mysql://192.168.56.10:3306/webdemo --username root --password admin --table acct_user2 --export-dir /sqoop/test -input-fields-terminated-by '\t'

### Hadop启动时，出现 Warning:$HADOP_HOME is deprecated的原因

我们在执⾏脚本start-al.sh，启动hadop时，有时会出现如下图的警告信息

![image 2](<sqoop使用.note_images/imageFile2.png>)

虽然不影响程序运⾏，但是看到这样的警告信息总是觉得⾃⼰做得不够好，怎么去掉哪？ 我们⼀步步分享，先看⼀下启动脚本start-al.sh的源码，如下图

![image 3](<sqoop使用.note_images/imageFile3.png>)

虽然我们看不懂shel脚本的语法，但是可以猜到可能和⽂件hadop-config.sh有关，我们再看⼀下这 个⽂件的源码。该⽂件特⼤，我们只截取最后⼀部分，⻅下图

![image 4](<sqoop使用.note_images/imageFile4.png>)

从图中的红⾊框框中可以看到，脚本判断变量HADOP_HOME_WARN_SUPRES和 HADOP_HOME的值，如果前者为空，后者不为空，则显示警告信息“Warning…”。 我们在安装hadop是，设置了环境变量HADOP_HOME造成的。 ⽹上有的说新的hadop版本使⽤HADOP_INSTAL作为环境变量，我还没有看到源代码，并且担⼼ 其他框架与hadop的兼容性，所以暂时不修改，那么只好设置HADOP_HOME_WARN_SUPRES 的值了。 修改配置⽂件/etc/profile（我原来⼀直在这⾥设置环境变量，操作系统是rhel6.3），增加环境变量 HADOP_HOME_WARN_SUPRES，如下图

![image 5](<sqoop使用.note_images/imageFile5.png>)

保存退出，再次启动hadop，就不会出现警告信息了，如下图

![image 6](<sqoop使用.note_images/imageFile6.png>)

#### 1、列出mysql数据库中的所有数据库

sqop list-databases-conect jdbc:mysql:/localhost: 306/ -username dyh -pasword 0

- 2、连接mysql并列出数据库中的表

- 3、将关系型数据的表结构复制到hive中

- 4、将数据从关系数据库导⼊⽂件到hive表中

- 5、将hive中的表数据导⼊到mysql数据库表中


sqop list-tables-conect jdbc:mysql:/localhost: 306/test -username dyh-pasword 0

sqop create-hive-table-conect jdbc:mysql:/192.168.56.10  306/webdemo-table act_userusername rot -pasword admin-hive-table act_user-fields-terminated-by "\t"-linesterminated-by "\n"; 参数说明：

-fields-terminated-by "\ 01" 是设置每列之间的分隔符，"\ 01"是ASCI码中的1，它也是hive的默 认⾏内分隔符， ⽽sqop的默认⾏内分隔符为"，"

- -lines-terminated-by "\n" 设置的是每⾏之间的分隔符，此处为换⾏符，也是默认的分隔符；

注意：只是复制表的结构，表中的内容没有复制

sqop import -conectjdbc:mysql:/192.168.56.10  306/webdemo-username rot pasword admin-table act_user-hive-import -hive-table act_user -m 2-fields-terminatedby "\t"; 参数说明：

- -m 2 表示由两个map作业执⾏；
- -fields-terminated-by "\ 01" 需同创建hive表时保持⼀致；


sqop export -conect jdbc:mysql:/192.168.56.10  306/webdemo-username rot pasword admin-table act_user2-export-dir /user/hive/warehouse/act_user/part-m- 0input-fields-terminated-by '\t'

注意：

- 1、在进⾏导⼊之前，mysql中的表userst必须已经提起创建好了。
- 2、jdbc:mysql:/192.168.20.18  306/test中的IP地址改成localhost会报异常，具体见本⼈上⼀篇帖⼦


- 6、将数据从关系数据库导⼊⽂件到hive表中， -query 语句使⽤

- 7、将数据从关系数据库导⼊⽂件到hive表中， -columns -where 语句使⽤


sqop import -apend-conectjdbc:mysql:/192.168.56.10  306/webdemo-username rot pasword admin-query "select id,email fromact_user2 where \$CONDITIONS AND id='1'" -m 1

-target-dir /user/hive/warehouse/act_user-fields-terminated-by "\t";

sqop import -apend-conectjdbc:mysql:/192.168.56.10  306/webdemo-username rot pasword admin-table act_user -columns "id,name" -where "id > 0 and (name = 'User' or name = 'Admin')" -m 1 -target-dir /user/hive/warehouse/act_user-fields-terminated-by "\t"; 注意： -target-dir /user/hive/warehouse/userinfos2 可以⽤ -hive-import -hive-table userinfos2 进⾏替换

