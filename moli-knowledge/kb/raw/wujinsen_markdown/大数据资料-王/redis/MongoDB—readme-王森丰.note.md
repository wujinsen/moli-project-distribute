htp:/ w.cnblogs.com/hojo/

mongoDB

⼀、准备⼯作

- 1、 下载mongoDB 下载地址： 选择合适你的版本 相关⽂档：

- 2、 安装mongoDB

- A、 不解压模式： 将下载下来的mongoDB-xxx.zip打开，找到bin⽬录，运⾏mongod.exe就可以启动服务，默认端⼝27017，db保存 的路径是系统C硬盘⽬录的根⽬录的/data/db⽬录。也就是说，如果你的mongoDB-xxx.zip在E盘，那么你需要在C盘 下建⽴data/db⽬录。mongoDB不会帮你建⽴这个⽬录的。 然后运⾏mongo即可连接到test数据库，你就可以进⾏数据操作。运⾏help显示帮助命令⾏。

- B、 解压模式


将下载下来的mongoDB-xxx.zip解压到任意⽬录，找到bin⽬录，运⾏mongod.exe就可以启动mongoDB，默认端⼝ 27017，db保存的路径是当前zip所在硬盘⽬录的根⽬录的/data/db⽬录。也就是说，如果你的mongoDB-xxx.zip在 E盘，那么你需要在E盘下建⽴data/db⽬录。mongoDB不会帮你建⽴这个⽬录的。

然后运⾏mongo即可连接到test数据库，你就可以进⾏数据操作。运⾏help显示帮助命令⾏。

- 3、 简单测试 > 2+4


http://www.mongodb.org/downloads

http://www.mongodb.org/display/DOCS/Tutorial

6

> db

test

> //第⼀次插⼊数据会创建数据库

Fri May 20 16:47:39 malformed UTF-8 character sequence at offset 27

error2:(shellhelp1) exec failed: malformed UTF-8 character sequence at offset 27

> db.foo.insert({id: 2011, userName: 'hoojo', age: 24, email: "hoojo_@126.com"});

> db.foo.find();

{ "_id" : ObjectId("4dd62b0352a70cbe79e04f81"), "id" : 2011, "userName" : "hoojo",

"age" : 24, "email" : "hoojo_@126.com" }

>

上⾯完成了简单运算，显示当前使⽤的数据库，以及添加数据、查询数据操作

⼆、DB shell数据操作 shell命令操作语法和JavaScript很类似，其实控制台底层的查询语句都是⽤JavaScript脚本完成操作的。 Ø 数据库

- 1、Help查看命令提示

help

db.help();

db.yourColl.help();

db.youColl.find().help();

rs.help();

- 2、切换/创建数据库

>use yourDB;

当创建⼀个集合(table)的时候会⾃动创建当前数据库

- 3、查询所有数据库


- show dbs;
- 4、删除当前使⽤数据库

db.dropDatabase();

- 5、从指定主机上克隆数据库

db.cloneDatabase(“127.0.0.1”);

将指定机器上的数据库的数据克隆到当前数据库

- 6、从指定的机器上复制指定数据库数据到某个数据库

db.copyDatabase("mydb", "temp", "127.0.0.1");

将本机的mydb的数据复制到temp数据库中

- 7、修复当前数据库

db.repairDatabase();

- 8、查看当前使⽤的数据库


db.getName();

db;

db和getName⽅法是⼀样的效果，都可以查询当前使⽤的数据库

- 9、显示当前db状态

db.stats();

- 10、当前db版本

db.version();

- 11、查看当前db的链接机器地址


db.getMongo();

Ø Collection聚集集合

- 1、创建⼀个聚集集合（table）

db.createCollection(“collName”, {size: 20, capped: 5, max: 100});

- 2、得到指定名称的聚集集合（table）


db.getCollection("account");

- 3、得到当前db的所有聚集集合

db.getCollectionNames();

- 4、显示当前db所有聚集索引的状态


db.printCollectionStats();

Ø ⽤户相关

- 1、添加⼀个⽤户

db.addUser("name");

db.addUser("userName", "pwd123", true);

添加⽤户、设置密码、是否只读

- 2、数据库认证、安全模式

db.auth("userName", "123123");

- 3、显示当前所有⽤户

show users;

- 4、删除⽤户


db.removeUser("userName");

Ø 其他

- 1、查询之前的错误信息

db.getPrevError();

- 2、清除错误记录


db.resetError();

三、Collection聚集集合操作 Ø 查看聚集集合基本信息

- 1、查看帮助

db.yourColl.help();

- 2、查询当前集合的数据条数

db.yourColl.count();

- 3、查看数据空间⼤⼩

db.userInfo.dataSize();

- 4、得到当前聚集集合所在的db


- db.userInfo.getDB();
- 5、得到当前聚集的状态

db.userInfo.stats();

- 6、得到聚集集合总⼤⼩

db.userInfo.totalSize();

- 7、聚集集合储存空间⼤⼩

db.userInfo.storageSize();

- 8、Shard版本信息

db.userInfo.getShardVersion()

- 9、聚集集合重命名


db.userInfo.renameCollection("users");

将userInfo重命名为users

- 10、删除当前聚集集合


db.userInfo.drop();

Ø 聚集集合查询

- 1、查询所有记录

db.userInfo.find();

相当于：select * from userInfo;

默认每⻚显示20条记录，当显示不下的情况下，可以⽤it迭代命令查询下⼀⻚数据。注意：键⼊it命令不能带“；”

但是你可以设置每⻚显示数据的⼤⼩，⽤DBQuery.shellBatchSize = 50;这样每⻚就显示50条记录了。

- 2、查询去掉后的当前聚集集合中的某列的重复数据

db.userInfo.distinct("name");

会过滤掉name中的相同数据

相当于：select distict name from userInfo;

- 3、查询age = 22的记录

db.userInfo.find({"age": 22});

相当于： select * from userInfo where age = 22;

- 4、查询age > 22的记录


db.userInfo.find({age: {$gt: 22}});

相当于：select * from userInfo where age > 22;

- 5、查询age < 22的记录

db.userInfo.find({age: {$lt: 22}});

相当于：select * from userInfo where age < 22;

- 6、查询age >= 25的记录

db.userInfo.find({age: {$gte: 25}});

相当于：select * from userInfo where age >= 25;

- 7、查询age <= 25的记录

db.userInfo.find({age: {$lte: 25}});

- 8、查询age >= 23 并且 age <= 26

db.userInfo.find({age: {$gte: 23, $lte: 26}});

- 9、查询name中包含 mongo的数据


db.userInfo.find({name: /mongo/});

//相当于%%

select * from userInfo where name like ‘%mongo%’;

- 10、查询name中以mongo开头的

db.userInfo.find({name: /^mongo/});

select * from userInfo where name like ‘mongo%’;

- 11、查询指定列name、age数据

db.userInfo.find({}, {name: 1, age: 1});

相当于：select name, age from userInfo;

当然name也可以⽤true或false,当⽤ture的情况下河name:1效果⼀样，如果⽤false就是排除name，显示name以 外的列信息。

- 12、查询指定列name、age数据, age > 25

db.userInfo.find({age: {$gt: 25}}, {name: 1, age: 1});

相当于：select name, age from userInfo where age > 25;

- 13、按照年龄排序


升序：db.userInfo.find().sort({age: 1});

降序：db.userInfo.find().sort({age: -1});

- 14、查询name = zhangsan, age = 22的数据

db.userInfo.find({name: 'zhangsan', age: 22});

相当于：select * from userInfo where name = ‘zhangsan’ and age = ‘22’;

- 15、查询前5条数据

db.userInfo.find().limit(5);

相当于：select top 5 * from userInfo;

- 16、查询10条以后的数据

db.userInfo.find().skip(10);

相当于：select * from userInfo where id not in (

select top 10 * from userInfo

);

- 17、查询在5-10之间的数据


db.userInfo.find().limit(10).skip(5);

可⽤于分⻚，limit是pageSize，skip是第⼏⻚*pageSize

- 18、or与 查询

db.userInfo.find({$or: [{age: 22}, {age: 25}]});

相当于：select * from userInfo where age = 22 or age = 25;

- 19、查询第⼀条数据

db.userInfo.findOne();

相当于：select top 1 * from userInfo;

db.userInfo.find().limit(1);

- 20、查询某个结果集的记录条数

db.userInfo.find({age: {$gte: 25}}).count();

相当于：select count(*) from userInfo where age >= 20;

- 21、按照某列进⾏排序


db.userInfo.find({sex: {$exists: true}}).count();

相当于：select count(sex) from userInfo;

Ø 索引

- 1、创建索引

db.userInfo.ensureIndex({name: 1});

db.userInfo.ensureIndex({name: 1, ts: -1});

- 2、查询当前聚集集合所有索引

db.userInfo.getIndexes();

- 3、查看总索引记录⼤⼩

db.userInfo.totalIndexSize();

- 4、读取当前集合的所有index信息

db.users.reIndex();

- 5、删除指定索引

db.users.dropIndex("name_1");

- 6、删除所有索引索引


db.users.dropIndexes();

Ø 修改、添加、删除集合数据

- 1、添加

db.users.save({name: ‘zhangsan’, age: 25, sex: true});

添加的数据的数据列，没有固定，根据添加的数据为准

- 2、修改

db.users.update({age: 25}, {$set: {name: 'changeName'}}, false, true);

相当于：update users set name = ‘changeName’ where age = 25;

db.users.update({name: 'Lisi'}, {$inc: {age: 50}}, false, true);

相当于：update users set age = age + 50 where name = ‘Lisi’;

db.users.update({name: 'Lisi'}, {$inc: {age: 50}, $set: {name: 'hoho'}}, false, true);

相当于：update users set age = age + 50, name = ‘hoho’ where name = ‘Lisi’;

- 3、删除


db.users.remove({age: 132});

- 4、查询修改删除


db.users.findAndModify({

query: {age: {$gte: 25}},

sort: {age: -1},

update: {$set: {name: 'a2'}, $inc: {age: 2}},

remove: true

});

db.runCommand({ findandmodify : "users",

query: {age: {$gte: 25}},

sort: {age: -1},

update: {$set: {name: 'a2'}, $inc: {age: 2}},

remove: true

});

update 或 remove 其中⼀个是必须的参数; 其他参数可选。

<table>
  <tr>
    <th>参数</th>
    <th>详解</th>
    <th>默认值</th>
  </tr>
  <tr>
    <td>query</td>
    <td>查询过滤条件</td>
    <td>{}</td>
  </tr>
  <tr>
    <td>sort</td>
    <td>如果多个⽂档符合查询过滤条 件，将以该参数指定的排列⽅式 选择出排在⾸位的对象，该对象 将被操作</td>
    <td>{}</td>
  </tr>
  <tr>
    <td>remove</td>
    <td>若为true，被选中对象将在返回 前被删除</td>
    <td>N/A</td>
  </tr>
  <tr>
    <td>update</td>
    <td>⼀个 修改器对象</td>
    <td>N/A</td>
  </tr>
  <tr>
    <td>new</td>
    <td>若为true，将返回修改后的对象 ⽽不是原始对象。在删除操作 中，该参数被忽略。</td>
    <td>false</td>
  </tr>
  <tr>
    <td>fields</td>
    <td>参⻅Retrieving a Subset of Fiel</td>
    <td>Al fields</td>
  </tr>
  <tr>
    <td>upsert</td>
    <td>ds (1.5.0+) 创建新对象若查询结果为空。 示例</td>
    <td>false</td>
  </tr>
</table>


(1.5.4+)

Ø 语句块操作

- 1、简单Hello World

print("Hello World!");

这种写法调⽤了print函数，和直接写⼊"Hello World!"的效果是⼀样的；

- 2、将⼀个对象转换成json

tojson(new Object());

tojson(new Object('a'));

- 3、循环添加数据


> for (var i = 0; i < 30; i++) {

... db.users.save({name: "u_" + i, age: 22 + i, sex: i % 2});

... };

这样就循环添加了30条数据，同样也可以省略括号的写法

> for (var i = 0; i < 30; i++) db.users.save({name: "u_" + i, age: 22 + i, sex: i % 2});

也是可以的，当你⽤db.users.find()查询的时候，显示多条数据⽽⽆法⼀⻚显示的情况下，可以⽤it查看下⼀⻚的 信息；

- 4、find 游标查询

>var cursor = db.users.find();

> while (cursor.hasNext()) {

printjson(cursor.next());

}

这样就查询所有的users信息，同样可以这样写

var cursor = db.users.find();

while (cursor.hasNext()) { printjson(cursor.next); }

同样可以省略{}号

- 5、forEach迭代循环


db.users.find().forEach(printjson);

forEach中必须传递⼀个函数来处理每条迭代的数据信息

- 6、将find游标当数组处理

var cursor = db.users.find();

cursor[4];

取得下标索引为4的那条数据

既然可以当做数组处理，那么就可以获得它的⻓度：cursor.length();或者cursor.count();

那样我们也可以⽤循环显示数据

for (var i = 0, len = c.length(); i < len; i++) printjson(c[i]);

- 7、将find游标转换成数组

> var arr = db.users.find().toArray();

> printjson(arr[2]);

⽤toArray⽅法将其转换为数组

- 8、定制我们⾃⼰的查询结果


只显示age <= 28的并且只显示age这列数据

db.users.find({age: {$lte: 28}}, {age: 1}).forEach(printjson);

db.users.find({age: {$lte: 28}}, {age: true}).forEach(printjson);

排除age的列

db.users.find({age: {$lte: 28}}, {age: false}).forEach(printjson);

- 9、forEach传递函数显示信息


db.things.find({x:4}).forEach(function(x) {print(tojson(x));});

上⾯介绍过forEach需要传递⼀个函数，函数会接受⼀个参数，就是当前循环的对象，然后在函数体重处理传⼊的参数 信息。

开发环境： System：Windows IDE：eclipse、MyEclipse 8 Database：mongoDB 开发依赖库： JavaEE5、mongo-2.5.3.jar、junit-4.8.2.jar Email：hoojo_@126.com Blog：

http://blog.csdn.net/IBM_hoojo http://hoojo.cnblogs.com/

⼀、准备⼯作

- 1、 ⾸先，下载mongoDB对Java⽀持的驱动包 驱动包下载地址： mongoDB对Java的相关⽀持、技术： 驱动源码下载： 在线查看源码：

- 2、 下⾯建⽴⼀个JavaProject⼯程，导⼊下载下来的驱动包。即可在Java中使⽤mongoDB，⽬录如下：


https://github.com/mongodb/mongo-java-driver/downloads

http://www.mongodb.org/display/DOCS/Java+Language+Center https://download.github.com/mongodb-mongo-java-driver-r2.6.1-7-g6037357.zip https://github.com/mongodb/mongo-java-driver

![image 1](<MongoDB—readme-王森丰.note_images/imageFile1.png>)

# ⼆、Java操作MongoDB示例

在本示例之前你需要启动mongod.exe的服务，启动后，下⾯的程序才能顺利执⾏；

- 1、 建⽴SimpleTest.java，完成简单的mongoDB数据库操作 Mongo mongo = new Mongo(); 这样就创建了⼀个MongoDB的数据库连接对象，它默认连接到当前机器的localhost地址，端⼝是27017。 DB db = mongo.getDB(“test”);


这样就获得了⼀个test的数据库，如果mongoDB中没有创建这个数据库也是可以正常运⾏的。如果你 就知道，

读过上⼀篇⽂章

mongoDB可以在没有创建这个数据库的情况下，完成数据的添加操作。当添加的时候，没有这个库，mongoDB会⾃ 动创建当前数据库。 得到了db，下⼀步我们要获取⼀个“聚集集合DBCollection”，通过db对象的getCollection⽅法来完成。 DBCollection users = db.getCollection("users"); 这样就获得了⼀个DBCollection，它相当于我们数据库的“表”。 查询所有数据 DBCursor cur = users.find(); while (cur.hasNext()) { System.out.println(cur.next()); }

完整源码 package com.hoo.test;

import java.net.UnknownHostException;

import com.mongodb.DB;

import com.mongodb.DBCollection;

import com.mongodb.DBCursor;

import com.mongodb.Mongo;

import com.mongodb.MongoException;

import com.mongodb.util.JSON;

/**

- * <b>function:</b>MongoDB 简单示例

- * @author hoojo

- * @createDate 2011-5-24 下午02:42:29

- * @file SimpleTest.java

- * @package com.hoo.test

- * @project MongoDB

- * @blog http://blog.csdn.net/IBM_hoojo

- * @email hoojo_@126.com

- * @version 1.0

- */


public class SimpleTest {

public static void main(String[] args) throws UnknownHostException, MongoException {

Mongo mg = new Mongo();

//查询所有的Database

for (String name : mg.getDatabaseNames()) {

System.out.println("dbName: " + name);

}

DB db = mg.getDB("test");

//查询所有的聚集集合

for (String name : db.getCollectionNames()) {

System.out.println("collectionName: " + name);

}

DBCollection users = db.getCollection("users");

//查询所有的数据

DBCursor cur = users.find();

while (cur.hasNext()) {

System.out.println(cur.next());

}

System.out.println(cur.count());

System.out.println(cur.getCursorId());

System.out.println(JSON.serialize(cur));

}

}

- 2、 完成CRUD操作，⾸先建⽴⼀个MongoDB4CRUDTest.java，基本测试代码如下： package com.hoo.test;


import java.net.UnknownHostException;

import java.util.ArrayList;

import java.util.List;

import org.bson.types.ObjectId;

import org.junit.After;

import org.junit.Before;

import org.junit.Test;

import com.mongodb.BasicDBObject;

import com.mongodb.Bytes;

import com.mongodb.DB;

import com.mongodb.DBCollection;

import com.mongodb.DBCursor;

import com.mongodb.DBObject;

import com.mongodb.Mongo;

import com.mongodb.MongoException;

import com.mongodb.QueryOperators;

import com.mongodb.util.JSON;

/**

- * <b>function:</b>实现MongoDB的CRUD操作

- * @author hoojo

- * @createDate 2011-6-2 下午03:21:23

- * @file MongoDB4CRUDTest.java

- * @package com.hoo.test

- * @project MongoDB

- * @blog http://blog.csdn.net/IBM_hoojo

- * @email hoojo_@126.com

- * @version 1.0


- */


public class MongoDB4CRUDTest {

private Mongo mg = null;

private DB db;

private DBCollection users;

@Before

public void init() {

try {

mg = new Mongo();

//mg = new Mongo("localhost", 27017);

} catch (UnknownHostException e) {

e.printStackTrace();

} catch (MongoException e) {

e.printStackTrace();

}

//获取temp DB；如果默认没有创建，mongodb会⾃动创建

db = mg.getDB("temp");

//获取users DBCollection；如果默认没有创建，mongodb会⾃动创建

users = db.getCollection("users");

}

@After

public void destory() {

if (mg != null)

mg.close();

mg = null;

db = null;

users = null;

System.gc();

}

public void print(Object o) {

System.out.println(o);

}

}

- 3、 添加操作 在添加操作之前，我们需要写个查询⽅法，来查询所有的数据。代码如下： /**


- * <b>function:</b> 查询所有数据

- * @author hoojo

- * @createDate 2011-6-2 下午03:22:40

- */


private void queryAll() {

print("查询users的所有数据：");

//db游标

DBCursor cur = users.find();

while (cur.hasNext()) {

print(cur.next());

}

}

@Test

public void add() {

//先查询所有数据

queryAll();

print("count: " + users.count());

DBObject user = new BasicDBObject();

user.put("name", "hoojo");

user.put("age", 24);

//users.save(user)保存，getN()获取影响⾏数

//print(users.save(user).getN());

//扩展字段，随意添加字段，不影响现有数据

user.put("sex", "男");

print(users.save(user).getN());

//添加多条数据，传递Array对象

print(users.insert(user, new BasicDBObject("name", "tom")).getN());

//添加List集合

List<DBObject> list = new ArrayList<DBObject>();

list.add(user);

DBObject user2 = new BasicDBObject("name", "lucy");

user.put("age", 22);

list.add(user2);

//添加List集合

print(users.insert(list).getN());

//查询下数据，看看是否添加成功

print("count: " + users.count());

queryAll();

}

- 4、 删除数据 @Test


public void remove() {

queryAll();

print("删除id = 4de73f7acd812d61b4626a77：" + users.remove(new BasicDBObject("_id", new ObjectId("4de73f7acd812d61b4626a77"))).getN());

print("remove age >= 24: " + users.remove(new BasicDBObject("age", new BasicDBObject("$gte", 24))).getN());

}

- 5、 修改数据 @Test


public void modify() {

print("修改：" + users.update(new BasicDBObject("_id", new ObjectId("4dde25d06be7c53ffbd70906")), new BasicDBObject("age", 99)).getN());

print("修改：" + users.update(

new BasicDBObject("_id", new ObjectId("4dde2b06feb038463ff09042")),

new BasicDBObject("age", 121),

true,//如果数据库不存在，是否添加

false//多条修改

).getN());

print("修改：" + users.update(

new BasicDBObject("name", "haha"),

new BasicDBObject("name", "dingding"),

true,//如果数据库不存在，是否添加

true//false只修改第⼀天，true如果有多条就不修改

).getN());

//当数据库不存在就不修改、不添加数据，当多条数据就不修改

//print("修改多条：" + coll.updateMulti(new BasicDBObject("_id", new ObjectId("4dde23616be7c19df07db42c")), new BasicDBObject("name", "199")));

}

- 6、 查询数据 @Test


public void query() {

//查询所有

//queryAll();

//查询id = 4de73f7acd812d61b4626a77

print("find id = 4de73f7acd812d61b4626a77: " + users.find(new BasicDBObject("_id", new ObjectId("4de73f7acd812d61b4626a77"))).toArray());

//查询age = 24

print("find age = 24: " + users.find(new BasicDBObject("age", 24)).toArray());

//查询age >= 24

print("find age >= 24: " + users.find(new BasicDBObject("age", new BasicDBObject("$gte", 24))).toArray());

print("find age <= 24: " + users.find(new BasicDBObject("age", new BasicDBObject("$lte", 24))).toArray());

print("查询age!=25：" + users.find(new BasicDBObject("age", new BasicDBObject("$ne", 25))).toArray());

print("查询age in 25/26/27：" + users.find(new BasicDBObject("age", new BasicDBObject(QueryOperators.IN,

new int[] { 25, 26, 27 }))).toArray());

print("查询age not in 25/26/27：" + users.find(new BasicDBObject("age", new BasicDBObject(QueryOperators.NIN, new int[] { 25, 26, 27 }))).toArray());

print("查询age exists 排序：" + users.find(new BasicDBObject("age", new BasicDBObject(QueryOperators.EXISTS, true))).toArray());

print("只查询age属性：" + users.find(null, new BasicDBObject("age", true)).toArray());

print("只查属性：" + users.find(null, new BasicDBObject("age", true), 0, 2).toArray());

print("只查属性：" + users.find(null, new BasicDBObject("age", true), 0, 2, Bytes.QUERYOPTION_NOTIMEOUT).toArray());

//只查询⼀条数据，多条去第⼀条

print("findOne: " + users.findOne());

print("findOne: " + users.findOne(new BasicDBObject("age", 26)));

print("findOne: " + users.findOne(new BasicDBObject("age", 26), new BasicDBObject("name", true)));

//查询修改、删除

print("findAndRemove 查询age=25的数据，并且删除: " + users.findAndRemove(new BasicDBObject("age", 25)));

//查询age=26的数据，并且修改name的值为Abc

print("findAndModify: " + users.findAndModify(new BasicDBObject("age", 26),

new BasicDBObject("name", "Abc")));

print("findAndModify: " + users.findAndModify(

new BasicDBObject("age", 28), //查询age=28的数据

new BasicDBObject("name", true), //查询name属性

new BasicDBObject("age", true), //按照age排序

false, //是否删除，true表示删除

new BasicDBObject("name", "Abc"), //修改的值，将name修改成Abc

true,

true));

queryAll();

}

mongoDB不⽀持联合查询、⼦查询，这需要我们⾃⼰在程序中完成。将查询的结果集在Java查询中进⾏需要的过滤即 可。

- 7、 其他操作 public void testOthers() {


DBObject user = new BasicDBObject();

user.put("name", "hoojo");

user.put("age", 24);

//JSON 对象转换

print("serialize: " + JSON.serialize(user));

//反序列化

print("parse: " + JSON.parse("{ \"name\" : \"hoojo\" , \"age\" : 24}"));

print("判断temp Collection是否存在: " + db.collectionExists("temp"));

//如果不存在就创建

if (!db.collectionExists("temp")) {

DBObject options = new BasicDBObject();

options.put("size", 20);

options.put("capped", 20);

options.put("max", 20);

print(db.createCollection("account", options));

}

//设置db为只读

db.setReadOnly(true);

//只读不能写⼊数据

db.getCollection("test").save(user);

}

好了，这⾥基本上就介绍这么多Java操作MongoDB的⽅法。其他的东⻄还需要你⾃⼰多多研究。上⾯操作MongoDB的 ⽅法都是⼀些常⽤的⽅法，

⽐较简单。如果有什么问题，可以给我留⾔或是发Email:hoojo_@126.com

