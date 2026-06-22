内部表&外部表

未被external修饰的是内部表（managed table），被external修饰的为外部表（external table）； 区别： 内部表数据由Hive⾃身管理，外部表数据由HDFS管理； 内部表数据存储的位置是hive.metastore.warehouse.dir（默认：/user/hive/warehouse），外部表数据 的存储位置由⾃⼰制定（如果没有LOCATION，Hive将在HDFS上的/user/hive/warehouse⽂件夹下以 外部表的表名创建⼀个⽂件夹，并将属于这个表的数据存放在这⾥）； 删除内部表会直接删除元数据（metadata）及存储数据；删除外部表仅仅会删除元数据，HDFS上的 ⽂件并不会被删除； 对内部表的修改会将修改直接同步给元数据，⽽对外部表的表结构和分区进⾏修改，则需要修复 （MSCK REPAIR TABLE table_name;）

如下，进⾏试验进⾏理解

试验理解

创建内部表t1

create table t1(

id int ,name string ,hoby aray<string> ,ad map<String,string>

) row format delimited fields terminated by ',' colection items terminated by '-' map keys terminated by ':' ;

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
- 2. 查看表的描述：desc t1;


- 装载数据（t1）


注：⼀般很少⽤insert （不是insert overwrite）语句，因为就算就算插⼊⼀条数据，也会调⽤ MapReduce，这⾥我们选择Load Data的⽅式。

LOAD DATA [LOCAL] INPATH 'filepath' [OVERWRITE] INTO TABLE tablename [PARTITION (partcol1=val1, partcol2=val2.)] 1 创建⼀个⽂件粘贴上述记录，并上载即可，如下图：

⽂件内容如下

- 1,xiaoming,bok-TV-code,beijing:chaoyang-shagnhai:pudong
- 2,lilei,bok-code,nanjing:jiangning-taiwan:taibei
- 3,lihua,music-bok,heilongjiang:haerbin


- 1
- 2
- 3 然后上载


- load data local inpath '/home/hadop/Desktop/data' overwrite into table t1; 1 别忘记写⽂件名/data,笔者第⼀次忘记写，把整个Desktop上传了，⼀查全是nul和乱码。。。。 查看表内容：


select * from t1; 1

创建⼀个外部表t2

create external table t2(

id int ,name string ,hoby aray<string> ,ad map<String,string>

) row format delimited fields terminated by ',' colection items terminated by '-' map keys terminated by ':' location '/user/t2' ;

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


12

- 装载数据（t2）


- load data local inpath '/home/hadop/Desktop/data' overwrite into table t2; 1


查看⽂件位置

如下图，我们在NameNode:5070/explorer.html#/user/⽬录下，可以看到t2⽂件

t1在哪呢？在我们之前配置的默认路径⾥

同样我们可以通过命令⾏获得两者的位置信息：

desc formated table_name; 1

注：图中managed table就是内部表，⽽external table就是外部表。 #分别删除内部表和外部表 下⾯分别删除内部表和外部表，查看区别

观察HDFS上的⽂件

发现t1已经不存在了

但是t2仍然存在

因⽽外部表仅仅删除元数据

重新创建外部表t2

create external table t2(

id int ,name string ,hoby aray<string> ,ad map<String,string>

) row format delimited

fields terminated by ',' colection items terminated by '-' map keys terminated by ':' location '/user/t2';

不往⾥⾯插⼊数据，我们select * 看看结果

可⻅数据仍然在！！！

官⽹解释

以下是官⽹中关于external表的介绍：

A table created without the EXTERNAL clause is caled a managed table because Hive manages its data. Managed and External Tables By default Hive creates managed tables, where files, metadata and statistics are managed by internal Hive proceses. A managed table is stored under the hive.metastore.warehouse.dir path property, by default in a folder path similar to /aps/hive/warehouse/databasename.db/tablename/. The default location can be overi den by the location property during table creation. If a managed table or partition is droped, the data and metadata asociated with that table or partition are deleted. If the PURGE option is not specified, the data is moved to a trash folder for a defined duration. Use managed tables when Hive should manage the lifecycle of the table, or when generating temporary tables. An external table describes the metadata / schema on external files. External table files can be acesed and managed by proceses outside of Hive. External tables can aces data stored in sources such as Azure Storage Volumes (ASV) or remote HDFS locations. If the structure or partitioning of an external table is changed, an MSCK REPAIR TABLE table_name statement can be used to refresh metadata information. Use external tables when files are already present or in remote locations, and the files should remain even if the table is droped. Managed or external tables can be identified using the DESCRIBE FORMATED table_name comand, which wil display either MANAGED_TABLE or EXTERNAL_TABLE depending on table type.

Statistics can be managed on internal and external tables and partitions for query optimization. Hive官⽹介绍：

htps:/cwiki.apache.org/confluence/display/Hive/LanguageManual+DL#LanguageManual DL-De scribeTable/View/Column

⸻版权声明：本⽂为CSDN博主「刘⾦宝_Arvin」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附 上原⽂出处链接及本声明。 原⽂链接：

htps:/blog.csdn.net/ q_36743482/article/details/78393678

