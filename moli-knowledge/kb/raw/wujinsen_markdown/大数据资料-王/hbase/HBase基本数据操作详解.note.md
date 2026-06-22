概述 对于建表，和RDBMS类似，HBase也有namespace的概念，可以指定表空间创建表，也可以直接创建 表，进⼊default表空间。 对于数据操作，HBase⽀持四类主要的数据操作，分别是：

Put ：增加⼀⾏，修改⼀⾏；

Delete ：删除⼀⾏，删除指定列族，删除指定column的多个版本，删除指定column的制定版本 等；

Get ：获取指定⾏的所有信息，获取指定⾏和指定列族的所有colunm，获取指定column，获取指 定column的⼏个版本， 获取指定column的指定版本等；

Scan ：获取所有⾏，获取指定⾏键范围的⾏，获取从某⾏开始的⼏⾏，获取满⾜过滤条件的⾏ 等。

这四个类都是 org.apache.hadop.hbase.client的⼦类，可以到官⽹API去查看详细信息，本⽂仅总结 常⽤⽅法，⼒争让读者⽤20%的时间掌握80%的常⽤功能。

⽬录

- 1.命名空间Namespace
- 2.创建表
- 3.删除表
- 4.修改表
- 5.新增、更新数据Put
- 6.删除数据Delete
- 7.获取单⾏Get
- 8.获取多⾏Scan


- 1. 命名空间Namespace 在关系数据库系统中，命名空间 namespace指的是⼀个 表的逻辑分组 ，同⼀组中的表有类似的⽤途。命名空间的概念为 即将到来 的 多租户特性打下基础：


配额管理（ Quota Management (HBASE-8410)）：限制⼀个namespace可以使⽤的资源，资源包 括region和table等；

命名空间安全管理（ Namespace Security Administration (HBASE-9206)）：提供了另⼀个层⾯的 多租户安全管理；

Region服务器组（Region server groups (HBASE-6721)）：⼀个命名空间或⼀张表，可以被固定 到⼀组 regionservers上，从⽽保证了数据隔离性。

- 1.1.命名空间管理 命名空间可以被创建、移除、修改。


表和命名空间的⾪属关系 在在创建表时决定，通过以下格式指定： <namespace>:<table> Example：hbase shel中创建命名空间、创建命名空间中的表、移除命名空间、修改命名空间 #Create a namespacecreate_namespace'my_ns'

- 1 #create my_table in my_ns namespace

- 2 create 'my_ns:my_table', 'fam'

- 3


#drop namespacedrop_namespace'my_ns' #alter namespacealter_namespace'my_ns', {METHOD =>'set','PROPERTY_NAME' => 'PROPERTY_VALUE'}

- 1.2. 预定义的命名空间 有两个系统内置的预定义命名空间：


hbase ：系统命名空间，⽤于包含hbase的内部表

default ： 所有未指定命名空间的表都⾃动进⼊该命名空间

Example：指定命名空间和默认命名空间

- 1 #namespace=foo and table qualifier=bar

- 2 create 'foo:bar', 'fam'

- 3

- 4 #namespace=default and table qualifier=bar

- 5 create 'bar', 'fam'


- 2.创建表 废话不多说，直接上样板代码，代码后再说明注意事项和知识点：


Configuration conf = HBaseConfiguration. create (); HBaseAdmin admin = new HBaseAdmin(conf);

/create namespace named "my_ns" admin.createNamespace(NamespaceDescriptor. create ( "my_ns" ).build();

/create tableDesc, with namespace name "my_ns" and table name " mytable "

HTableDescriptor tableDesc = new HTableDescriptor(TableName. valueOf ("my_ns:mytable" );

tableDesc.setDurability(Durability. SYNC_WAL );

/ad a column family " mycf " HColumnDescriptor hcd = new HColumnDescriptor( "mycf" ); tableDesc.adFamily(hcd); admin.createTable(tableDesc); admin.close();

关键知识点： 必须将HBase集群的hbase-site.xml⽂件添加进⼯程的claspath中，否则 Configuration conf = HBaseConfiguration. create () 代码获取不到需要的集群相关信息，也就⽆ 法找到集群，运⾏程序时会报错； HTableDescriptor tableDesc = new HTableDescriptor(TableName. valueOf ("my_ns:mytable" ) 代码是描述表mytable，并将mytable放到了my_ns命名空间中，前提是该命名空间已存在，如 果指定的是不存在命名空间，则会报 错 org.apache.hadop.hbase.NamespaceNotFoundException； 命名空间⼀般在建模阶段通过命令⾏创建，在java代码中通过 admin.createNamespace(NamespaceDescriptor. create ( "my_ns" ).build() 创建的机会不多； 创建 HBaseAdmin 对象时就已经建⽴了客户端程序与HBase集群的conection ，所以在程序执⾏ 完成后，务必通过 admin.close() 关闭conection； 可以通过 HTableDescriptor 对象设置 表的特性 ，⽐如： 通过 tableDesc.setMaxFileSize(512) 设置⼀个region中的store⽂件的最⼤size，当⼀个region中的最 ⼤store⽂件达到这个size时，region就开始分裂； 通过 tableDesc.setMemStoreFlushSize(512) 设置region内存中的memstore的最⼤值，当memstore达 到这个值时，开始往磁盘中刷数据。 更多特性请⾃⾏查阅官⽹API； 可以通过 HColumnDescriptor 对象设置 列族的特性 ，⽐如：通过 hcd.setTimeToLive(5184 0) 设置数据保存的最⻓时间；通过 hcd.setInMemory(true ) 设置数 据保存在内存中以提⾼响应速度；通过 hcd .setMaxVersions(10) 设置数据保存的最⼤版本数； 通过 hcd.setMinVersions(5) 设置数据保存的最⼩版本数（配合TimeToLive使⽤）。更多特性请⾃ ⾏查阅官⽹API； 数据的版本数只能通过 HColumnDescriptor 对象设置，不能通过 HTableDescriptor对象设置； 由于HBase的数据是先写⼊内存，数据累计达到内存阀值时才往磁盘中flush数据，所以，如果在 数据还没有flush进硬盘时，regionserver down掉了，内存中的数据将丢失。要想解决这个场景的 问题就需要⽤到WAL（Write-Ahead-Log），tableDesc.setDurability(Durability. SYNC_WAL ) 就 是设置写WAL⽇志的级别，示例中设置的是同步写WAL，该⽅式安全性较⾼，但⽆疑会⼀定程度 影响性能，请根据具体场景选择使⽤；

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


9.

setDurability (Durability d)⽅法可以在相关的三个对象中使⽤，分别是： HTableDescriptor， Delete， Put（其中Delete和Put的该⽅法都是继承⾃⽗类 org.apache.hadop.hbase.client.Mutation） 。分别针对表、插⼊操作、删除操作设定WAL⽇ 志写⼊级别。需要注意的是， Delete和Put并不会继承Table的Durability级别（已实测验证） 。 Durability是⼀个枚举变量，可选值参⻅4.2节。如果不通过该⽅法指定WAL⽇志级别，则为 默 认 USE_DEFAULT 级别。

- 3.删除表 删除表没创建表那么多学问，直接上代码：

Configuration conf = HBaseConfiguration. create (); HBaseAdmin admin = new HBaseAdmin(conf); String tablename = "my_ns:mytable" ;

if (admin.tableExists(tablename) {

try { admin.disableTable(tablename); admin.deleteTable(tablename);

}catch (Exception e) {

/ TODO : handle exception e.printStackTrace();

}

} admin.close();

说明 ：删除表前必须先disable表。

- 4.修改表


- 4.1.实例代码


- （1）删除列族、新增列族 修改之前，四个列族：


- hbase(main):014 0> describe 'rd_ns:itable' DESCRIPTION ENABLED 'rd_ns:itable', {NAME => ' info ', DATA_BLOCK_ENCODING => 'NONE', BLOMFILTER => 'ROW', REPLICATION_SCOPE => '0', V true ERSIONS => '10', COMPRESION => 'NONE', MIN_VERSIONS => '0', TL => '2147483647', KEP_DELETED_CELS => 'false', BLOCKSIZE => '6536', IN_MEMORY => 'false', BLOCKCACHE => 'true'}, {NAME => ' newcf ', DATA_BLOCK_ENCODING => 'NONE


', BLOMFILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESION => 'NONE', VERSIONS => '10', TL => '2147483647', MIN_VERSIONS => '0', KEP_DELETED_CELS => 'false', BLOCKSIZE => '6536', IN_MEMORY => 'false', BLOCKCACHE => 'tr ue'}, {NAME => ' note ', DATA_BLOCK_ENCODING => 'NONE', BLOMFILTER => 'ROW', REPLICATION_SCOPE => '0', VERSIONS => '10', COMPRESION => 'NONE', MIN_VERSIONS => '0', TL => '2147483647', KEP_DELETED_CELS => 'false', BLOCKSIZE

=> '6536', IN_MEMORY => 'false', BLOCKCACHE => 'true'}, {NAME => ' sysinfo ', DATA_BLOCK_ENCODING => 'NONE', BLOM FILTER => 'ROW', REPLICATION_SCOPE => '0', COMPRESION => 'NONE', VERSIONS => '10', TL

=> '2147483647', MIN_VERS IONS => '0', KEP_DELETED_CELS => 'true', BLOCKSIZE => '6536', IN_MEMORY => 'false', BLOCKCACHE => 'true'} 1 row(s) in 0.0450 seconds 修改表，删除三个列族，新增⼀个列族，代码如下：

Configuration conf = HBaseConfiguration. create (); HBaseAdmin admin = new HBaseAdmin(conf); String tablename = "rd_ns:itable" ;

if (admin.tableExists(tablename) { try { admin.disableTable(tablename); /get the TableDescriptor of target table HTableDescriptor newtd = admin.getTableDescriptor (Bytes. toBytes ("rd_ns:itable");

/remove 3 useles column families newtd.removeFamily(Bytes. toBytes ( "note"); newtd.removeFamily(Bytes. toBytes ( "newcf"); newtd.removeFamily(Bytes. toBytes ( "sysinfo");

/create HColumnDescriptor for new column family HColumnDescriptor newhcd = new HColumnDescriptor( "action_log" ); newhcd.setMaxVersions(10); newhcd.setKepDeletedCels( true );

/ad the new column family(HColumnDescriptor) to HTableDescriptor newtd.adFamily(newhcd);

/modify target table struture admin. modifyTable (Bytes. toBytes ( "rd_ns:itable" ),newtd);

admin.enableTable(tablename); }catch (Exception e) {

/ TODO : handle exception e.printStackTrace();

}

} admin.close();

修改之后：

- hbase(main):015 0> describe 'rd_ns:itable' DESCRIPTION ENABLED 'rd_ns:itable', {NAME => ' action_log ', DATA_BLOCK_ENCODING => 'NONE', BLOMFILTER => 'ROW', REPLICATION_SCOPE => true '0', COMPRESION => 'NONE', VERSIONS => '10', TL => '2147483647', MIN_VERSIONS => '0', KEP_DELETED_CELS => 'tr ue', BLOCKSIZE => '6536', IN_MEMORY => 'false', BLOCKCACHE => 'true'}, {NAME => ' info ', DATA_BLOCK_ENCODING => ' NONE', BLOMFILTER => 'ROW', REPLICATION_SCOPE => '0', VERSIONS => '10', COMPRESION


=> 'NONE', MIN_VERSIONS => ' 0', TL => '2147483647', KEP_DELETED_CELS => 'false', BLOCKSIZE => '6536', IN_MEMORY

=> 'false', BLOCKCACHE => 'true'} 1 row(s) in 0.040 seconds 逻辑很简单：

- 1.
- 2.
- 3.


通过 admin.getTableDescriptor (Bytes. toBytes ( "rd_ns:itable") 取得⽬标表的描述对象，应 该就是取得指向该对象的指针了； 修改⽬标表描述对象； 通过 admin. modifyTable (Bytes. toBytes ( "rd_ns:itable" ),newtd) 将修改后的描述对象应⽤到 ⽬标表。

- （2）修改现有列族的属性（setMaxVersions） Configuration conf = HBaseConfiguration. create (); HBaseAdmin admin = new HBaseAdmin(conf); String tablename = "rd_ns:itable" ;


if (admin.tableExists(tablename) {

try { admin.disableTable(tablename);

/get the TableDescriptor of target table HTableDescriptor htd = admin.getTableDescriptor(Bytes. toBytes ("rd_ns:itable"); HColumnDescriptor infocf = htd.getFamily(Bytes. toBytes ( "info"); infocf.setMaxVersions(10);

/modify target table struture admin.modifyTable(Bytes. toBytes ( "rd_ns:itable" ),htd); admin.enableTable(tablename);

}catch (Exception e) {

/ TODO : handle exception e.printStackTrace();

}

} admin.close();

- 5.新增、更新数据Put


- 5.1.常⽤构造函数：


- （1）指定⾏键 public Put(byte[] row) 参数： row ⾏键
- （2）指定⾏键和时间戳 public Put(byte[] row, long ts) 参数： row ⾏键， ts 时间戳
- （3）从⽬标字符串中提取⼦串，作为⾏键 Put(byte[] rowAray, int rowOfset, int rowLength)
- （4）从⽬标字符串中提取⼦串，作为⾏键，并加上时间戳 Put(byte[] rowAray, int rowOfset, int rowLength, long ts)


- 5.2.常⽤⽅法：


- （1）指定 列族、限定符 ，添加值 ad(byte[] family, byte[] qualifier, byte[] value)
- （2）指定 列族、限定符、时间戳 ，添加值 ad(byte[] family, byte[] qualifier, long ts, byte[] value)
- （3） 设置写WAL （Write-Ahead-Log）的级别 public void setDurability(Durability d) 参数是⼀个枚举值，可以有以下⼏种选择：


ASYNC_WAL ： 当数据变动时，异步写WAL⽇志

SYNC_WAL ： 当数据变动时，同步写WAL⽇志

FSYNC_WAL ： 当数据变动时，同步写WAL⽇志，并且，强制将数据写⼊磁盘

SKIP_WAL ： 不写WAL⽇志

USE_DEFAULT ： 使⽤HBase全局默认的WAL写⼊级别，即 SYNC_WAL

- 5.3.实例代码


- （1）插⼊⾏ Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:l etable" ); Put put = new Put(Bytes. toBytes ( "1 01"); put.ad(Bytes. toBytes ( "info" ), Bytes. toBytes ( "name" ), Bytes. toBytes ( "lion"); put.ad(Bytes. toBytes ( "info" ), Bytes. toBytes ( "adres" ), Bytes. toBytes ("shangdi"); put.ad(Bytes. toBytes ( "info" ), Bytes. toBytes ( "age" ), Bytes. toBytes ( "30"); put.setDurability(Durability. SYNC_WAL ); table.put(put); table.close();
- （2）更新⾏ Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:l etable" );

Put put = new Put(Bytes. toBytes ( "1 01"); put.ad(Bytes. toBytes ( "info" ), Bytes. toBytes ( "name" ), Bytes. toBytes ( "l e"); put.ad(Bytes. toBytes ( "info" ), Bytes. toBytes ( "adres" ), Bytes. toBytes ("longze"); put.ad(Bytes. toBytes ( "info" ), Bytes. toBytes ( "age" ), Bytes. toBytes ( "31"); put.setDurability(Durability. SYNC_WAL ); table.put(put); table.close();

注意：

- （3） 从⽬标字符串中提取⼦串，作为⾏键，构建Put Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:l etable" );


- 1.
- 2.
- 3.
- 4.


Put的构造函数都需要指定⾏键，如果是全新的⾏键，则新增⼀⾏；如果是已有的⾏键，则更新现 有⾏。 创建Put对象及put.ad过程都是在构建⼀⾏的数据，创建Put对象时相当于创建了⾏对象，ad的 过程就是往⽬标⾏⾥添加cel，直到table.put才将数据插⼊表格； 以上代码创建Put对象⽤的是构造函数1，也可⽤构造函数2，第⼆个参数是时间戳； Put还有别的构造函数，请查阅官⽹API。

Put put = new Put(Bytes. toBytes ( "1 01_1 02" ),7,6); put.ad(Bytes. toBytes ( "info" ), Bytes. toBytes ( "name" ), Bytes. toBytes ("show"); put.ad(Bytes. toBytes ( "info" ), Bytes. toBytes ( "adres" ), Bytes. toBytes ("caofang"); put.ad(Bytes. toBytes ( "info" ), Bytes. toBytes ( "age" ), Bytes. toBytes ( "30");

table.put(put); table.close();

注意，关于： Put put = new Put(Bytes. toBytes ( "1 01_1 02" ),7,6)

- 1.
- 2.
- 3.


第⼆个参数是偏移量，也就是⾏键从第⼀个参数的第⼏个字符开始截取； 第三个参数是截取⻓度； 这个代码实际是从 1 01_1 02 中截取了1 02⼦串作为⽬标⾏的⾏键。

- 6.删除数据Delete Delete类⽤于删除表中的⼀⾏数据，通过HTable.delete来执⾏该动作。 在执⾏Delete操作时，HBase并不会⽴即删除数据，⽽是对需要删除的数据打上⼀个“墓碑”标记，直到 当Storefile合并时，再清除这些被标记上“墓碑”的数据。 如果希望删除整⾏，⽤⾏键来初始化⼀个Delete对象即可。如果希望进⼀步定义删除的具体内容，可 以使⽤以下这些Delete对象的⽅法：


为了删除指定的列族，可以使⽤ deleteFamily

为了删除指定列的多个版本，可以使⽤ deleteColumns

为了删除指定列的 指定版本 ，可以使⽤ deleteColumn，这样的话就只会删除版本号（时间戳）与 指定版本相同的列。如果不指定时间戳，默认只删除最新的版本

下⾯详细说明构造函数和常⽤⽅法：

- 6.1.构造函数


- （1）指定要删除的⾏键 Delete(byte[] row) 删除⾏键指定⾏的数据。 如果没有进⼀步的操作，使⽤该构造函数将删除⾏键指定的⾏中 所有列族中所有列的所有版本 ！
- （2）指定要删除的⾏键和时间戳 Delete(byte[] row, long timestamp) 删除⾏键和时间戳共同确定⾏的数据。 如果没有进⼀步的操作，使⽤该构造函数将删除⾏键指定的⾏中，所有列族中所有列的 时间戳 ⼩于等 于 指定时间戳的数据版本 。 注意 ：该时间戳仅仅和删除⾏有关，如果需要进⼀步指定列族或者列，你必须分别为它们指定时间 戳。
- （3）给定⼀个字符串，⽬标⾏键的偏移，截取的⻓度 Delete(byte[] rowAray, int rowOfset, int rowLength)


- （4）给定⼀个字符串，⽬标⾏键的偏移，截取的⻓度，时间戳 Delete(byte[] rowAray, int rowOfset, int rowLength, long ts)


- 6.2.常⽤⽅法
- 6.3.实例代码


Delete deleteColumn (byte[] family, byte[] qualifier) 删除指定列的 最新版本 的数据。

Delete deleteColumn s (byte[] family, byte[] qualifier) 删除指定列的 所有版本 的数据。

Delete deleteColumn (byte[] family, byte[] qualifier, long timestamp ) 删除指定列的 指定版 本 的数据。

Delete deleteColumn s (byte[] family, byte[] qualifier, long timestamp ) 删除指定列的，时间 戳 ⼩于等于给定时间戳 的 所有 版本的数据。

Delete deleteFamily (byte[] family) 删除指定列族的所有列的 所有 版本数据。

Delete deleteFamily (byte[] family, long timestamp) 删除指定列族的所有列中 时间戳 ⼩于等 于 指定时间戳 的所有数据。

Delete deleteFamilyVersion (byte[] family, long timestamp) 删除指定列族中所有列的时间戳 等 于 指定时间戳 的版本数据。

void setTimestamp (long timestamp) 为Delete对象设置时间戳。

- （1）删除整⾏的所有列族、所有⾏、所有版本 Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:l etable" );

Delete delete = new Delete(Bytes. toBytes ( " 0"); table.delete(delete); table.close();

- （2）删除 指定列的最新版本 以下是删除之前的数据，注意看1 03⾏的info:adres，这是该列最新版本的数据，值是 caofang1，在这之前的版本值是caofang：


- hbase(main): 07 0> scan 'rd_ns:l etable' ROW COLUMN+CEL


- 1 01 column=info:adres, timestamp=140530484314, value=longze

- 1 01 column=info:age, timestamp=140530484314, value=31

- 1 01 column=info:name, timestamp=140530484314, value=leon
- 1 02 column=info:adres, timestamp=1405305471343, value=caofang

- 1 02 column=info:age, timestamp=1405305471343, value=30 1 02 column=info:name, timestamp=1405305471343, value=show
- 1 03 column=info:adres, timestamp=1405390959464, value=caofang1 1 03 column=info:age, timestamp=1405390959464, value=301


- 1 03 column=info:name, timestamp=1405390959464, value=show1






- 3 row(s) in 0.0270 seconds 执⾏以下代码：


Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:l etable" ); Delete delete = new Delete(Bytes. toBytes ( "1 03"); delete.deleteColumn(Bytes. toBytes ( "info" ), Bytes. toBytes ( "adres");

table.delete(delete); table.close();

然后查看数据，发现1 03列的info:adres列的值显示为前⼀个版本的caofang了！其余值均不变：

- hbase(main): 08 0> scan 'rd_ns:l etable' ROW COLUMN+CEL

- 1 01 column=info:adres, timestamp=140530484314, value=longze

- 1 01 column=info:age, timestamp=140530484314, value=31

- 1 01 column=info:name, timestamp=140530484314, value=leon
- 1 02 column=info:adres, timestamp=1405305471343, value=caofang

- 1 02 column=info:age, timestamp=1405305471343, value=30 1 02 column=info:name, timestamp=1405305471343, value=show
- 1 03 column=info:adres, timestamp=1405390728175, value=caofang 1 03 column=info:age, timestamp=1405390959464, value=301


- 1 03 column=info:name, timestamp=1405390959464, value=show1 3 row(s) in 0.0560 seconds






（3）删除 指定列的所有版本 接以上场景，执⾏以下代码：

Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:l etable" ); Delete delete = new Delete(Bytes. toBytes ( "1 03"); delete. deleteColumns (Bytes. toBytes ( "info" ), Bytes. toBytes ( "adres");

table.delete(delete); table.close();

然后我们会发现，1 03⾏的整个info:adres列都没了：

- hbase(main): 09 0> scan 'rd_ns:l etable' ROW COLUMN+CEL


- 1 01 column=info:adres, timestamp=140530484314, value=longze


- 1 01 column=info:age, timestamp=140530484314, value=31


- 1 01 column=info:name, timestamp=140530484314, value=leon


- 1 02 column=info:adres, timestamp=1405305471343, value=caofang

- 1 02 column=info:age, timestamp=1405305471343, value=30 1 02 column=info:name, timestamp=1405305471343, value=show
- 1 03 column=info:age, timestamp=1405390959464, value=301 1 03 column=info:name, timestamp=1405390959464, value=show1


- 3 row(s) in 0.0240 seconds


- （4） 删除指定列族中所有 列的时间戳 等于 指定时间戳 的版本数据 为了演示效果，我已经向1 03⾏的info:adres列新插⼊⼀条数据


- hbase(main):010 0> scan 'rd_ns:l etable' ROW COLUMN+CEL


- 1 01 column=info:adres, timestamp=140530484314, value=longze

- 1 01 column=info:age, timestamp=140530484314, value=31

- 1 01 column=info:name, timestamp=140530484314, value=leon
- 1 02 column=info:adres, timestamp=1405305471343, value=caofang

1 02 column=info:age, timestamp=1405305471343, value=30 1 02 column=info:name, timestamp=1405305471343, value=show 1 03 column=info:adres, timestamp= 14053918386 , value=shangdi 1 03 column=info:age, timestamp= 1405390959464 , value=301

- 1 03 column=info:name, timestamp= 1405390959464 , value=show1 3 row(s) in 0.0250 seconds 现在，我们的⽬的是删除info列族中，时间戳为1405390959464的所有列数据：






Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:l etable" );

Delete delete = new Delete(Bytes. toBytes ( "1 03"); delete.deleteFamilyVersion (Bytes. toBytes ( "info" ), 1405390959464L);

table.delete(delete); table.close();

hbase(main):01 0> scan 'rd_ns:l etable' ROW COLUMN+CEL

- 1 01 column=info:adres, timestamp=140530484314, value=longze

- 1 01 column=info:age, timestamp=140530484314, value=31

- 1 01 column=info:name, timestamp=140530484314, value=leon
- 1 02 column=info:adres, timestamp=1405305471343, value=caofang


- 1 02 column=info:age, timestamp=1405305471343, value=30


- 1 02 column=info:name, timestamp=1405305471343, value=show


- 1 03 column=info:adres, timestamp= 14053918386 , value=shangdi


- 1 03 column=info:age, timestamp= 1405390728175 , value=30


- 1 03 column=info:name, timestamp= 1405390728175 , value=show 3 row(s) in 0.0250 seconds 可以看到，1 03⾏的info列族，已经不存在时间戳为 1405390959464的数据，⽐它更早版本的数 据被查询出来，⽽info列族中时间戳不等于 1405390959464的adres列，不受该delete的影响 。


- 7.获取单⾏Get 如果希望获取整⾏数据，⽤⾏键初始化⼀个Get对象就可以，如果希望进⼀步缩⼩获取的数据范围，可 以使⽤Get对象的以下⽅法：


如果希望取得指定列族的所有列数据，使⽤ adFamily 添加所有的⽬标列族即可；

如果希望取得指定列的数据，使⽤ adColumn 添加所有的⽬标列即可；

如果希望取得⽬标列的指定时间戳范围的数据版本，使⽤ setTimeRange ；

如果仅希望获取⽬标列的指定时间戳版本，则使⽤ setTimestamp ；

如果希望限制每个列返回的版本数，使⽤ setMaxVersions ；

如果希望添加过滤器，使⽤ setFilter

下⾯详细描述构造函数及常⽤⽅法：

- 7.1.构造函数 Get的构造函数很简单，只有⼀个构造函数： Get(byte[] row) 参数是⾏键。


- 7.2.常⽤⽅法
- 7.3.实测代码 测试表的所有数据：


Get adFamily(byte[] family) 指定希望获取的列族

Get adColumn(byte[] family, byte[] qualifier) 指定希望获取的列

Get setTimeRange(long minStamp, long maxStamp) 设置获取数据的 时间戳范围

Get setTimeStamp(long timestamp) 设置获取数据的时间戳

Get setMaxVersions(int maxVersions) 设定获取数据的版本数

Get setMaxVersions() 设定获取数据的 所有版本

Get setFilter(Filter filter) 为Get对象添加过滤器，过滤器详解请参⻅：htp:/blog.csdn.net/u0109 67382/article/details/3765317

void setCacheBlocks(bolean cacheBlocks) 设置该Get获取的数据是否缓存在内存中

- hbase(main):016 0> scan 'rd_ns:l etable' ROW COLUMN+CEL


- 1 01 column=info:adres, timestamp=140530484314, value=longze


- 1 01 column=info:age, timestamp=140530484314, value=31


- 1 01 column=info:name, timestamp=140530484314, value=leon
- 1 02 column=info:adres, timestamp=1405305471343, value=caofang


- 1 02 column=info:age, timestamp=1405305471343, value=30

1 02 column=info:name, timestamp=1405305471343, value=show 1 03 column=info:adres, timestamp=140540783218, value=qinghe

- 1 03 column=info:age, timestamp=140540783218, value=28


- 1 03 column=info:name, timestamp=140540783218, value=shichao 3 row(s) in 0.0250 seconds


- （1）获取⾏键指定⾏的 所有列族、所有列 的 最新版本 数据 Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:l etable" ); Get get = new Get(Bytes. toBytes ( "1 03"); Result r = table.get(get);

for(Cel cel : r.rawCels() {

System. out .println( "Rowkey : " +Bytes. toString (r.getRow()+ " Familiy:Quilifier : " +Bytes. toString (CelUtil. cloneQualifier (cel)+ " Value : " +Bytes. toString (CelUtil. cloneValue (cel)

);

} table.close();

代码输出： Rowkey : 1 03Familiy:Quilifier : adres Value : qinghe

- Rowkey : 1 03Familiy:Quilifier : ageValue : 28 Rowkey : 1 03Familiy:Quilifier : nameValue : shichao


- （2）获取⾏键指定⾏中， 指定列 的最新版本数据 Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:l etable" ); Get get = new Get(Bytes. toBytes ( "1 03");


get.adColumn(Bytes. toBytes ( "info" ), Bytes. toBytes ( "name"); Result r = table.get(get);

for(Cel cel : r.rawCels() {

System. out .println( "Rowkey : " +Bytes. toString (r.getRow()+ " Familiy:Quilifier : " +Bytes. toString (CelUtil. cloneQualifier (cel)+ " Value : " +Bytes. toString (CelUtil. cloneValue (cel)

);

} table.close();

代码输出： Rowkey : 1 03Familiy:Quilifier : nameValue : shichao

- （3）获取⾏键指定的⾏中， 指定时间戳 的数据 Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:l etable" ); Get get = new Get(Bytes. toBytes ( "1 03");

get.setTimeStamp(1405407854374L); Result r = table.get(get);

for(Cel cel : r.rawCels() {

System. out .println( "Rowkey : " +Bytes. toString (r.getRow()+ " Familiy:Quilifier : " +Bytes. toString (CelUtil. cloneQualifier (cel)+ " Value : " +Bytes. toString (CelUtil. cloneValue (cel)

);

} table.close();

代码输出了上⾯scan命令输出中没有展示的历史数据： Rowkey : 1 03Familiy:Quilifier : adres Value : huangzhuang Rowkey : 1 03Familiy:Quilifier : ageValue : 32 Rowkey : 1 03Familiy:Quilifier : nameValue : lily

- （4）获取⾏键指定的⾏中， 所有版本 的数据 Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:itable" ); Get get = new Get(Bytes. toBytes ( "1 03"); get.setMaxVersions(); Result r = table.get(get);


for(Cel cel : r.rawCels() {

System. out .println( "Rowkey : " +Bytes. toString (r.getRow()+ " Familiy:Quilifier : " +Bytes. toString (CelUtil. cloneQualifier (cel)+ " Value : " +Bytes. toString (CelUtil. cloneValue (cel)+ " Time : " +cel.getTimestamp()

);

} table.close();

代码输出：

Rowkey : 1 03Familiy:Quilifier : adres Value : xierqi Time : 140541750485 Rowkey : 1 03Familiy:Quilifier : adres Value : shangdi Time : 140541747465 Rowkey : 1 03Familiy:Quilifier : adres Value : longzeTime : 140541748414

- Rowkey : 1 03Familiy:Quilifier : ageValue : 29Time : 140541750485
- Rowkey : 1 03Familiy:Quilifier : ageValue : 30Time : 140541747465
- Rowkey : 1 03Familiy:Quilifier : ageValue : 31 Time : 140541748414 Rowkey : 1 03Familiy:Quilifier : nameValue : leonTime : 140541750485 Rowkey : 1 03Familiy:Quilifier : nameValue : l eTime : 140541747465 Rowkey : 1 03Familiy:Quilifier : nameValue : lionTime : 140541748414 注意： 能输出多版本数据的前提是当前列族能保存多版本数据，列族可以保存的数据版本数通过 HColumnDescriptor的setMaxVersions(Int)⽅法设置。


- 8.获取多⾏Scan Scan对象可以返回满⾜给定条件的多⾏数据。 如果希望获取所有的⾏，直接初始化⼀个Scan对象即 可。 如果希望限制扫描的⾏范围，可以使⽤以下⽅法：


如果希望获取指定列族的所有列，可使⽤ adFamily ⽅法来添加所有希望获取的列族

如果希望获取指定列，使⽤ adColumn ⽅法来添加所有列

通过 setTimeRange ⽅法设定获取列的时间范围

通过 setTimestamp ⽅法指定具体的时间戳，只返回该时间戳的数据

通过 setMaxVersions ⽅法设定最⼤返回的版本数

通过 setBatch ⽅法设定返回数据的最⼤⾏数

通过 setFilter ⽅法为Scan对象添加过滤器，过滤器详解请参⻅：htp:/blog.csdn.net/u01096738 2/article/details/3765317

Scan的结果数据是可以缓存在内存中的，可以通过 getCaching ()⽅法来查看当前设定的缓存条 数，也可以通过 setCaching (int caching)来设定缓存在内存中的⾏数，缓存得越多，以后查询结果 越快，同时也消耗更多内存。此外， 通过setCacheBlocks ⽅法设置是否缓存Scan的结果数据块， 默认为true

我们可以通过 setMaxResultSize(long)⽅法来设定Scan返回的结果⾏数。

下⾯是官⽹⽂档中的⼀个⼊⻔示例：假设表有⼏⾏键值为 "row1", "row2", "row3"，还有⼀些⾏有键值 "abc1", "abc2", 和 "abc3"，⽬标是返回"row"打头的⾏： HTable htable =. / instantiate HTable Scan scan = new Scan(); scan.adColumn(Bytes.toBytes("cf"),Bytes.toBytes("atr"); scan.setStartRow( Bytes.toBytes("row"); / start key is inclusive scan.setStopRow( Bytes.toBytes("row" + (char)0); / stop key is exclusive ResultScaner rs = htable.getScaner(scan);

try { for (Result r = rs.next(); r != nul; r = rs.next() {

/ proces result. } finaly { rs.close(); / always close the ResultScaner! }

- 8.1.常⽤构造函数


- （1）创建扫描所有⾏的Scan Scan()
- （2）创建Scan，从指定⾏开始扫描 ， Scan(byte[] startRow) 参数： startRow ⾏键 注意 ：如果指定⾏不存在，从下⼀个最近的⾏开始
- （3）创建Scan，指定起⽌⾏ Scan(byte[] startRow, byte[] stopRow) 参数： startRow起始⾏， stopRow终⽌⾏ 注意 ： startRow <= 结果集 < stopRow
- （4）创建Scan，指定起始⾏和过滤器 Scan(byte[] startRow, Filter filter) 参数： startRow 起始⾏， filter 过滤器


htp:/blog.csdn.net/u010967382/article/details/3765317

注意：过滤器的功能和构造参⻅

- 8.2.常⽤⽅法


Scan setStartRow (byte[] startRow) 设置Scan的开始⾏， 默认 结果集 包含 该⾏。 如果希望结 果集不包含该⾏，可以在⾏键末尾加上0。

Scan setStopRow (byte[] stopRow) 设置Scan的结束⾏， 默认 结果集 不包含该⾏。 如果希望 结果集包含该⾏，可以在⾏键末尾加上0。

Scan setTimeRange (long minStamp, long maxStamp) 扫描指定 时间范围 的数据

Scan setTimeStamp (long timestamp) 扫描指定 时间 的数据

Scan adColumn (byte[] family, byte[] qualifier) 指定扫描的列

Scan adFamily (byte[] family) 指定扫描的列族

Scan setFilter (Filter filter) 为Scan设置过滤器

Scan setReversed (bolean reversed) 设置Scan的扫描顺序，默认是正向扫描（false），可以 设置为逆向扫描（true）。注意：该⽅法0.98版本以后才可⽤！！

Scan setMaxVersions () 获取所有版本的数据

Scan setMaxVersions (int maxVersions) 设置获取的最⼤版本数

void setCaching (int caching) 设定缓存在内存中的⾏数，缓存得越多，以后查询结果越快，同 时也消耗更多内存

void setRaw (bolean raw) 激活或者禁⽤raw模式。如果raw模式被激活，Scan将返回 所有已经被 打上删除标记但尚未被真正删除 的数据。该功能仅⽤于激活了KEP_DELETED_ROWS的列族，即 列族开启了 hcd.setKepDeletedCels(true)

。Scan激活raw模式后，就不能指定任意的列，否则会报错 Enable/disable "raw" mode for this scan. If "raw" is enabled the scan wil return al delete marker and deleted rows that have not ben colected, yet. This is mostly useful for Scan on column families that have KEP_DELETED_ROWS enabled. It is an eror to specify any column when "raw" is set. hcd.setKepDeletedCels(true);

- 8.3.实测代码


- （1）扫描表中的 所有⾏ 的最新版本数据 Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:itable" );


Scan s = new Scan(); ResultScaner rs = table.getScaner(s);

for(Result r : rs) { for(Cel cel : r.rawCels() {

System. out .println( "Rowkey : " +Bytes. toString (r.getRow()+ " Familiy:Quilifier : " +Bytes. toString (CelUtil. cloneQualifier (cel)+ " Value : " +Bytes. toString (CelUtil. cloneValue (cel)+ " Time : " +cel.getTimestamp()

); }

} table.close();

代码输出：

- Rowkey : 1 01 Familiy:Quilifier : adres Value : anywhereTime : 1405417403438

- Rowkey : 1 01 Familiy:Quilifier : ageValue : 24Time : 1405417403438

- Rowkey : 1 01 Familiy:Quilifier : nameValue : zhangtaoTime : 1405417403438
- Rowkey : 1 02Familiy:Quilifier : adres Value : shangdi Time : 140541742693

Rowkey : 1 02Familiy:Quilifier : ageValue : 28Time : 140541742693 Rowkey : 1 02Familiy:Quilifier : nameValue : shichaoTime : 140541742693 Rowkey : 1 03Familiy:Quilifier : adres Value : xierqi Time : 140541750485 Rowkey : 1 03Familiy:Quilifier : ageValue : 29Time : 140541750485

- Rowkey : 1 03Familiy:Quilifier : nameValue : leonTime : 140541750485






- （2） 扫描指定⾏键范围，通过末尾加0，使得结果集包含StopRow Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:itable" ); Scan s = new Scan(); s. setStartRow (Bytes. toBytes ( "1 01"); s. setStopRow (Bytes. toBytes ( " 1 020 ");

ResultScaner rs = table.getScaner(s); for(Result r : rs) { for(Cel cel : r.rawCels() {

System. out .println( "Rowkey : " +Bytes. toString (r.getRow()+ " Familiy:Quilifier : " +Bytes. toString (CelUtil. cloneQualifier (cel)+ " Value : " +Bytes. toString (CelUtil. cloneValue (cel)+ " Time : " +cel.getTimestamp()

); }

} table.close();

代码输出：

- Rowkey : 1 01 Familiy:Quilifier : adres Value : anywhereTime : 1405417403438

- Rowkey : 1 01 Familiy:Quilifier : ageValue : 24Time : 1405417403438

- Rowkey : 1 01 Familiy:Quilifier : nameValue : zhangtaoTime : 1405417403438
- Rowkey : 1 02Familiy:Quilifier : adres Value : shangdi Time : 140541742693

Rowkey : 1 02Familiy:Quilifier : ageValue : 28Time : 140541742693 Rowkey : 1 02Familiy:Quilifier : nameValue : shichaoTime : 140541742693 （3） 返回 所有已经被打上删除标记但尚未被真正删除 的数据 本测试针对rd_ns:itable表的1 03⾏。 如果使⽤get结合 setMaxVersions() ⽅法能返回所有未删除的数据，输出如下： Rowkey : 1 03Familiy:Quilifier : adres Value : huilonguanTime : 140549414152 Rowkey : 1 03Familiy:Quilifier : adres Value : shangdi Time : 140541747465

- Rowkey : 1 03Familiy:Quilifier : ageValue : new29Time : 140549414152 Rowkey : 1 03Familiy:Quilifier : nameValue : liyangTime : 140549414152








然⽽，使⽤Scan强⼤的 s.setRaw( true ) ⽅法，可以获得所有 已经被打上删除标记但尚未被真正删 除 的数据。 代码如下：

Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:itable" ); Scan s = new Scan(); s.setStartRow(Bytes. toBytes ( "1 03"); s.setRaw( true ); s.setMaxVersions();

ResultScaner rs = table.getScaner(s); for(Result r : rs) { for(Cel cel : r.rawCels() {

System. out .println( "Rowkey : " +Bytes. toString (r.getRow()+ " Familiy:Quilifier : " +Bytes. toString (CelUtil. cloneQualifier (cel)+ " Value : " +Bytes. toString (CelUtil. cloneValue (cel)+ " Time : " +cel.getTimestamp()

); }

} table.close();

输出结果如下： Rowkey : 1 03Familiy:Quilifier : adres Value : huilonguanTime : 140549414152 Rowkey : 1 03Familiy:Quilifier : adres Value : Time : 140541750485 Rowkey : 1 03Familiy:Quilifier : adres Value : xierqi Time : 140541750485 Rowkey : 1 03Familiy:Quilifier : adres Value : shangdi Time : 140541747465 Rowkey : 1 03Familiy:Quilifier : adres Value : Time : 140541748414 Rowkey : 1 03Familiy:Quilifier : adres Value : longzeTime : 140541748414 Rowkey : 1 03Familiy:Quilifier : ageValue : new29Time : 140549414152 Rowkey : 1 03Familiy:Quilifier : ageValue : Time : 140541750485 Rowkey : 1 03Familiy:Quilifier : ageValue : Time : 140541750485

- Rowkey : 1 03Familiy:Quilifier : ageValue : 29Time : 140541750485
- Rowkey : 1 03Familiy:Quilifier : ageValue : 30Time : 140541747465
- Rowkey : 1 03Familiy:Quilifier : ageValue : 31 Time : 140541748414 Rowkey : 1 03Familiy:Quilifier : nameValue : liyangTime : 140549414152 Rowkey : 1 03Familiy:Quilifier : nameValue : Time : 1405493879419 Rowkey : 1 03Familiy:Quilifier : nameValue : leonTime : 140541750485 Rowkey : 1 03Familiy:Quilifier : nameValue : l eTime : 140541747465


Rowkey : 1 03Familiy:Quilifier : nameValue : lionTime : 140541748414

- （4） 结合过滤器，获取所有age在25到30之间的⾏ ⽬前的数据： hbase(main):049 0> scan 'rd_ns:itable' ROW COLUMN+CEL


- 1 01 column=info:adres, timestamp=1405417403438, value=anywhere

- 1 01 column=info:age, timestamp=1405417403438, value=24

- 1 01 column=info:name, timestamp=1405417403438, value=zhangtao
- 1 02 column=info:adres, timestamp=140541742693, value=shangdi

1 02 column=info:age, timestamp=140541742693, value=28 1 02 column=info:name, timestamp=140541742693, value=shichao 1 03 column=info:adres, timestamp=140549414152, value=huilonguan 1 03 column=info:age, timestamp=1405494 9631, value=29

- 1 03 column=info:name, timestamp=140549414152, value=liyang 3 row(s) in 0.0240 seconds 代码：






Configuration conf = HBaseConfiguration. create (); HTable table = new HTable(conf, "rd_ns:itable" ); FilterList filterList = new FilterList(FilterList.Operator. MUST_PAS_AL );

- SingleColumnValueFilter filter1 = new SingleColumnValueFilter( Bytes. toBytes ( "info" ), Bytes. toBytes ( "age" ), CompareOp. GREATER_OR_EQUAL , Bytes. toBytes ( "25" ) );
- SingleColumnValueFilter filter2 = new SingleColumnValueFilter( Bytes. toBytes ( "info" ), Bytes. toBytes ( "age" ), CompareOp. LES_OR_EQUAL , Bytes. toBytes ( "30" ) );


- filterList.adFilter(filter1);
- filterList.adFilter(filter2);


Scan scan = new Scan(); scan.setFilter(filterList);

ResultScaner rs = table.getScaner(scan); for(Result r : rs) { for(Cel cel : r.rawCels() {

System. out .println( "Rowkey : " +Bytes. toString (r.getRow()+ " Familiy:Quilifier : " +Bytes. toString (CelUtil. cloneQualifier (cel)+ " Value : " +Bytes. toString (CelUtil. cloneValue (cel)+ " Time : " +cel.getTimestamp()

); }

} table.close();

代码输出：

- Rowkey : 1 02Familiy:Quilifier : adres Value : shangdi Time : 140541742693

- Rowkey : 1 02Familiy:Quilifier : ageValue : 28 Time : 140541742693

- Rowkey : 1 02Familiy:Quilifier : nameValue : shichaoTime : 140541742693
- Rowkey : 1 03Familiy:Quilifier : adres Value : huilonguanTime : 140549414152


- Rowkey : 1 03Familiy:Quilifier : ageValue : 29 Time : 1405494 9631


- Rowkey : 1 03Familiy:Quilifier : nameValue : liyangTime : 140549414152 注意：


1. HBase对列族、列名⼤⼩写敏感

