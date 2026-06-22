安装及使⽤：

⾸先在Ubuntu上安装MongoDB。 下载MongoDB, 现在最新的⽣产版本1.7.0

- 1. 解压⽂件. $ tar -xvf mongodb-linux-i686-1.4.3.tgz
- 2. 为MongoDB创建数据⽬录，默认情况下它将数据存储在/data/db $ sudo mkdir -p /data/db/

$ sudo chown `id -u` /data/db

- 3. 启动MongoDB服务. $ cd mongodb-linux-i686-1.4.3/bin $ ./mongod
- 4. 打开另⼀个终端，并确保你在MongoDB的bin⽬录，输⼊如下命令. $ ./mongo


# ⼀些概念

⼀个mongod服务可以有建⽴多个数据库，每个数据库可以有多张表，这⾥的表名叫collection，每个 collection可以存放多个⽂档（document），每个⽂档都以BSON（binary json）的形式存放于硬盘中， 因此可以存储⽐较复杂的数据类型。它是以单⽂档为单位存储的，你可以任意给⼀个或⼀批⽂档新增 或删除字段，⽽不会对其它⽂档造成影响，这就是所谓的schema-free，这也是⽂档型数据库最主要的 优点。跟⼀般的key-value数据库不⼀样的是，它的value中存储了结构信息，所以你⼜可以像关系型数 据库那样对某些域进⾏读写、统计等操作。Mongo最⼤的特点是他⽀持的查询语⾔⾮常强⼤，其语法 有点类似于⾯向对象的查询语⾔，⼏乎可以实现类似关系数据库单表查询的绝⼤部分功能，⽽且还⽀ 持对数据建⽴索引。Mongo还可以解决海量数据的查询效率，根据官⽅⽂档，当数据量达到50GB以上 数据时，Mongo数据库访问速度是MySQL10 倍以上。

## BSON

BSON是Binary JSON 的简称，是⼀个JSON⽂档对象的⼆进制编码格式。BSON同JSON⼀样⽀持往其它 ⽂档对象和数组中再插⼊⽂档对象和数组，同时扩展了JSON的数据类型。如：BSON有Date类型和 BinDate类型。 BSON被⽐作⼆进制的交换格式，如同Protocol Buffers，但BSON⽐它更“schema-less”，⾮常好的灵活性 但空间占⽤稍微⼤⼀点。 BSON有以下三个特点：

- 1． 轻量级
- 2． 跨平台
- 3． 效率⾼ 命名空间


MongoDB存储BSON对象到colections,这⼀系列的数据库名和colection名被称为⼀个命名空间。如 同：java.util.List;⽤来管理数据库中的数据。

# 索引

mongodb可以对某个字段建⽴索引，可以建⽴组合索引、唯⼀索引，也可以删除索引，建⽴索引就意 味着增加空间开销。默认情况下每个表都会有⼀个唯⼀索引：_id，如果插⼊数据时没有指定_id，服务 会⾃动⽣成⼀个_id，为了充分利⽤已有索引，减少空间开销，最好是⾃⼰指定⼀个unique的key为 _id，通常⽤对象的ID⽐较合适，⽐如商品的ID。

shel操作数据库：

1. 超级⽤户相关：

- 1. #进⼊数据库admin use admin
- 2. #增加或修改⽤户密码 db.adUser('name','pwd')
- 3. #查看⽤户列表 db.system.users.find()
- 4. #⽤户认证 db.auth('name','pwd')
- 5. #删除⽤户 db.removeUser('name')
- 6. #查看所有⽤户 show users
- 7. #查看所有数据库 show dbs
- 8. #查看所有的colection show colections
- 9. #查看各colection的状态 db.printColectionStats()
- 10. #查看主从复制状态 db.printReplicationInfo()


1. #修复数据库 db.repairDatabase()

- 12. #设置记录profiling，0=of 1=slow 2=al db.setProfilingLevel(1)
- 13. #查看profiling


show profile

- 14. #拷⻉数据库 db.copyDatabase('mail_adr','mail_adr_tmp')
- 15. #删除colection db.mail_adr.drop()
- 16. #删除当前的数据库 db.dropDatabase()


2. 增删改

1. #存储嵌套的对象

db.fo.save({'name':'ysz','adres':{'city':'beijing','post':1 096},'phone': [138,139]})

2. #存储数组对象

db.user_adr.save({'Uid':'yushunzhi@sohu.com','Al':['test-1@sohu.com','test2@sohu.com']})

- 3. #根据query条件修改，如果不存在则插⼊，允许修改多条记录 db.fo.update({'y':5},{'$set':{'x':2},upsert=true,multi=true)
- 4. #删除 y=5的记录 db.fo.remove({'y':5})
- 5. #删除所有的记录 db.fo.remove()


- 3. 索引

- 1. #增加索引：1(ascending),-1(descending)
- 2. db.fo.ensureIndex({firstname: 1, lastname: 1}, {unique: true});
- 3. #索引⼦对象
- 4. db.user_adr.ensureIndex({'Al.Em': 1})
- 5. #查看索引信息
- 6. db.fo.getIndexes()
- 7. db.fo.getIndexKeys()
- 8. #根据索引名删除索引
- 9. db.user_adr.dropIndex('Al.Em_1')


- 4. 查询


- 1. #查找所有


- 2. db.fo.find()
- 3. #查找⼀条记录
- 4. db.fo.findOne()
- 5. #根据条件检索10条记录
- 6. db.fo.find({'msg':'Helo 1'}).limit(10)
- 7. #sort排序
- 8. db.deliver_status.find({'From':'ixigua@sina.com'}).sort({'Dt',-1})
- 9. db.deliver_status.find().sort({'Ct':-1}).limit(1)
- 10. #count操作


1. db.user_adr.count()

- 12. #distinct操作,查询指定列，去重复
- 13. db.fo.distinct('msg')
- 14. #”>=”操作
- 15. db.fo.find({"timestamp": {"$gte" : 2})
- 16. #⼦对象的查找
- 17. db.fo.find({'adres.city':'beijing'})


- 5. 管理


- 1. #查看colection数据的⼤⼩
- 2. db.deliver_status.dataSize()
- 3. #查看coleciont状态
- 4. db.deliver_status.stats()
- 5. #查询所有索引的⼤⼩
- 6. db.deliver_status.totalIndexSize()


- 5. advanced queries:⾼级查询


条件操作符 $gt : > $lt : < $gte: >= $lte: <= $ne : !=、<> $in : in $nin: not in $al: al $not: 反匹配 (1.3.3及以上版本) 查询 name <> "bruce" and age >= 18 的数据 db.users.find({name: {$ne: "bruce"}, age: {$gte: 18}); 查询 creation_date > '2010-01-01' and creation_date <= '2010-12-31' 的数 据 db.users.find({creation_date:{$gt:new Date(2010,0,1), $lte:new Date(2010,1,31)}); 查询 age in (20,2,24,26) 的数据 db.users.find({age: {$in: [20,2,24,26]}); 查询 age取模10等于0 的数 据 db.users.find('this.age % 10 = 0'); 或者 db.users.find({age : {$mod : [10, 0]}); 匹配所 有 db.users.find({favorite_number : {$al : [6, 8]}); 可以查询出{name: 'David', age: 26, favorite_number: [ 6, 8, 9 ] } 可以不查询出{name: 'David', age: 26, favorite_number: [ 6, 7, 9 ] } 查询 不匹配name=B*带头的记录 db.users.find({name: {$not: /^B.*/}); 查询 age取模10不等于0 的数 据 db.users.find({age : {$not: {$mod : [10, 0] }); #返回部分字段 选择返回age和_id字段(_id字段总是 会被返回) db.users.find({}, {age:1}); db.users.find({}, {age:3}); db.users.find({}, {age:true}); db.users.find({ name : "bruce" }, {age:1}); 0为false, ⾮0为true 选择返回age、adres和 _id字段 db.users.find({ name : "bruce" }, {age:1, adres:1}); 排除返回age、adres和_id字 段 db.users.find({}, {age:0, adres:false}); db.users.find({ name : "bruce" }, {age:0, adres:false}); 数组元素个数判断 对于{name: 'David', age: 26, favorite_number: [ 6, 7, 9 ] }记录 匹 配db.users.find({favorite_number: {$size: 3}); 不匹配db.users.find({favorite_number: {$size: 2}); $exists判断字段是否存在 查询所有存在name字段的记录 db.users.find({name: {$exists: true}); 查询所有不存在phone字段的记录 db.users.find({phone: {$exists: false}); $type判断字段类 型 查询所有name字段是字符类型的 db.users.find({name: {$type: 2}); 查询所有age字段是整型 的 db.users.find({age: {$type: 16}); 对于字符字段，可以使⽤正则表达式 查询以字⺟b或者B带头的所 有记录 db.users.find({name: /^b.*/i}); $elemMatch(1.3.1及以上版本) 为数组的字段中匹配其中某个元 素 Javascript查询和$where查询 查询 age > 18 的记录，以下查询都⼀样 db.users.find({age: {$gt: 18}); db.users.find({$where: "this.age > 18"}); db.users.find("this.age > 18"); f = function() {return this.age > 18} db.users.find(f); 排序sort() 以年龄升序asc db.users.find().sort({age: 1}); 以年龄降序 desc db.users.find().sort({age: -1}); 限制返回记录数量limit() 返回5条记录 db.users.find().limit(5); 返 回3条记录并打印信息 db.users.find().limit(3).forEach(function(user) {print('my age is ' + user.age)}); 结果 my age is 18 my age is 19 my age is 20 限制返回记录的开始点skip() 从第3条记录 开始，返回5条记录(limit 3, 5) db.users.find().skip(3).limit(5); 查询记录条数 count() db.users.find().count(); db.users.find({age:18}).count(); 以下返回的不是5，⽽是user表中所 有的记录数量 db.users.find().skip(10).limit(5).count(); 如果要返回限制之后的记录数量，要使⽤ count(true)或者count(⾮0) db.users.find().skip(10).limit(5).count(true); 分组group() 假设test表只有 以下⼀条数据 { domain: " w.mongodb.org" , invoked_at: {d:"209-1-03", t:"17 14 05"} , response_time: 0.05 , htp_action: "GET /display/DOCS/Agregation" } 使⽤group统计test表 1⽉份 的数据count:count(*)、total_time:sum(response_time)、 avg_time:total_time/count; db.test.group( { cond: {"invoked_at.d": {$gt: "209-1", $lt: "20912"} , key: {htp_action: true} , initial: {count: 0, total_time:0} , reduce: function(doc, out){ out.count+; out.total_time+=doc.response_time } , finalize: function(out){ out.avg_time =

out.total_time / out.count } } ); [ { "htp_action" : "GET /display/DOCS/Agregation", "count" : 1, "total_time" : 0.05, "avg_time" : 0.05 } ]

Java 应⽤示例 要使⽤Java操作MongoDB的话，要到官⽅⽹站下载⼀个驱动包，把包导⼊后，可以尝试来操作了（记 得⼀定要开着服务器） ⾸先介绍⼀下⽐较常⽤的⼏个类 Mongo：连接服务器，执⾏⼀些数据库操作的选项，如新建⽴⼀个数据库等 DB：对应⼀个数据库，可以⽤来建⽴集合等操作 DBColection：对应⼀个集合（类似表），可能是我们⽤得最多的，可以添加删除记录等 DBObjec：接⼝和BasicDBObject对象：表示⼀个具体的记录，BasicDBObject实现了DBObject，因为是 key-value的数据结构，所以⽤起来其实和HashMap是基本⼀致的 DBCursor：⽤来遍历取得的数据，实现了Iterable和Iterator 接下来实际的操作⼀下，代码如下： import java.net.UnknownHostException; import java.util.List; import java.util.Set; import com.mongodb.BasicDBObject; import com.mongodb.DB; import com.mongodb.DBColection; import com.mongodb.DBCursor; import com.mongodb.DBObject; import com.mongodb.Mongo; import com.mongodb.MongoException; publicclas MongoDbTest {

publicstaticvoid main(String[] args) throws UnknownHostException, MongoException { /Mongo m = new Mongo();

/Mongo m = new Mongo("localhost"); /获得数据库服务

Mongo m = new Mongo("localhost", 27017); /得到数据库mytest

DB db = m.getDB("mytest");

/得到mytest数据库下所有表名 Set<String> cols = db.getColectionNames(); for (String s : cols) {

System.out.println(s);

}

/得到testColection表 DBColection col = db.getColection("testColection"); /new ⼀个BasicDBObject对象doc BasicDBObject doc = new BasicDBObject();

/赋值 doc.put("name", "MongoDB"); doc.put("type", "database");

doc.put("count", 1); /⼜new ⼀个BasicDBObject对象info BasicDBObject info = new BasicDBObject();

- info.put("x", 203);
- info.put("y", 102); /把info放⼊doc


doc.put("info", info);

/向testColection表中插⼊⼀条数据 col.insert(doc);

/查询⼀条数据 DBObject myDoc = col.findOne(); System.out.println(myDoc);

/循环插⼊10条数据到testColection for (int i=0; i < 10; i +) {

col.insert(new BasicDBObject().apend("i", i); }

/Counting Documents in A Colection System.out.println(col.getCount();

/Using a Cursor to Get Al the Documents DBCursorcur = col.find(); while(cur.hasNext() {<span st

