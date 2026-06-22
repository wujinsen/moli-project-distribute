基本使⽤ 如下⾯这个shel脚本： # 的连接字符串，其中包含了 的地址，SID,和端⼝号 CONECTURL=jdbc:oracle:thin:@20.135.60.21 1521 DWRAC2 #使⽤的⽤户名 ORACLENAME=ka #使⽤的密码 ORACLEPASWORD=ka123 #需要从Oracle中导⼊的表名 oralceTableName=t #需要从Oracle中导⼊的表中的字段名 columns=AREA_ID,TEAM_NAME #将Oracle中的数据导⼊到HDFS后的存放路径 hdfsPath=aps/as/hive/$oralceTableName #执⾏导⼊逻辑。将Oracle中的数据导⼊到HDFS中 sqop import -apend-conect $CONECTURL-username $ORACLENAME-pasword

Oracle Oracle

$ORACLEPASWORD-target-dir $hdfsPath -num-mapers 1-table $oralceTableNamecolumns $columns-fields-terminated-by '\ 01'

执⾏这个脚本之后，导⼊程序就完成了。 接下来，⽤户可以⾃⼰创建外部表，将外部表的路径和HDFS中存放Oracle数据的路径对应上即可。 注意：这个程序导⼊到HDFS中的数据是⽂本格式，所以在创建Hive外部表的时候，不需要指定⽂件

的格式为RCFile,⽽使⽤默认的TextFile即可。数据间的分隔符为'\ 01'.如果多次导⼊同⼀个表中的数 据，数据以apend的形式插⼊到HDFS⽬录中。

并⾏导⼊ 假设有这样这个sqop命令，需要将Oracle中的数据导⼊到HDFS中： sqop import -apend-conect $CONECTURL-username $ORACLENAME-pasword

$ORACLEPASWORD-target-dir $hdfsPath -m 1-table $oralceTableName-columns $columns-fields-terminated-by '\ 01' -where "data_desc='201-02-26'"

请注意，在这个命令中，有⼀个参数"-m",代表的含义是使⽤多少个并⾏，这个参数的值是1,说明没

有开启并⾏功能。 现在，我们可以将"-m"参数的值调⼤，使⽤并⾏导⼊的功能，如下⾯这个命令： sqop import -apend-conect $CONECTURL-username $ORACLENAME-pasword

$ORACLEPASWORD-target-dir $hdfsPath -m 4-table $oralceTableName-columns $columns-fields-terminated-by '\ 01' -where "data_desc='201-02-26'"

⼀般来说，Sqop就会开启4个进程，同时进⾏数据的导⼊操作。 但是，如果从Oracle中导⼊的表没有主键，那么会出现如下的错误提示：

EROR tol.ImportTol: Eror during import: No primary key could be found for table creater_user.popt_cas_redirect_his. Please specify one with-split-by or perform a sequential import with '-m 1'.

在这种情况下，为了更好的使⽤Sqop的并⾏导⼊功能，我们就需要从原理上理解Sqop并⾏导⼊

的实现机制。 如果需要并⾏导⼊的Oracle表的主键是id,并⾏的数量是4,那么Sqop⾸先会执⾏如下⼀个查询： select max（id） as max, select min（id） as min from table [where 如果指定了where⼦句]; 通过这个查询，获取到需要拆分字段（id）的最⼤值和最⼩值，假设分别是1和1 0.

然后，Sqop会根据需要并⾏导⼊的数量，进⾏拆分查询，⽐如上⾯的这个例⼦，并⾏导⼊将拆分为

如下4条SQL同时执⾏： select * from table where 0 <= id < 250; select * from table where 250 <= id < 50; select * from table where 50 <= id < 750; select * from table where 750 <= id < 1 0; 注意，这个拆分的字段需要是整数。 从上⾯的例⼦可以看出，如果需要导⼊的表没有主键，我们应该如何⼿动选取⼀个合适的拆分字

段，以及选择合适的并⾏数。 再举⼀个实际的例⼦来说明： 我们要从Oracle中导⼊creater_user.popt_cas_redirect_his. 这个表没有主键，所以我们需要⼿动选取⼀个合适的拆分字段。 ⾸先看看这个表都有哪些字段： 然后，我假设ds_name字段是⼀个可以选取的拆分字段，然后执⾏下⾯的sql去验证我的想法： select min（ds_name）， max（ds_name） from creater_user.popt_cas_redirect_his where

data_desc='201-02-26' 发现结果不理想，min和max的值都是相等的。所以这个字段不合适作为拆分字段。 再 ⼀下另⼀个字段：CLIENTIP select min（CLIENTIP）， max（CLIENTIP） from creater_user.popt_cas_redirect_his where

测试

data_desc='201-02-26' 这个结果还是不错的。所以我们使⽤CLIENTIP字段作为拆分字段。 所以，我们使⽤如下命令并⾏导⼊： sqop import -apend-conect $CONECTURL-username $ORACLENAME-pasword

$ORACLEPASWORD-target-dir $hdfsPath -m 12-split-by CLIENTIP-table $oralceTableName-columns $columns-fields-terminated-by '\ 01' -where "data_desc='20102-26'"

这次执⾏这个命令，可以看到，消耗的时间为：20mins, 35sec,导⼊了 3, 2,896条数据。 另外，如果觉得这种拆分不能很好满⾜我们的需求，可以同时执⾏多个Sqop命令，然后在where的

参数后⾯指定拆分的规则。如：

sqop import -apend-conect $CONECTURL-username $ORACLENAME-pasword $ORACLEPASWORD-target-dir $hdfsPath -m 1-table $oralceTableName-columns $columns-fields-terminated-by '\ 01' -where "data_desc='201-02-26' logtime<10  0  0"

sqop import -apend-conect $CONECTURL-username $ORACLENAME-pasword $ORACLEPASWORD-target-dir $hdfsPath -m 1-table $oralceTableName-columns $columns-fields-terminated-by '\ 01' -where "data_desc='201-02-26' logtime>=10  0  0"

从⽽达到并⾏导⼊的⽬的。

