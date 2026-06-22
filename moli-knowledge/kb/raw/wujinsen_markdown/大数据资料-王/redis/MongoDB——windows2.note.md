⼀、前⾔

最近开始学习⾮关系型数据库MongoDB，却在博客园上找不到⽐较系统的教程，很多资料都要去 查阅英⽂⽹站，效率⽐较低下。本⼈不才，借着⾃学的机会把⼼得体会都记录下来，⽅便感兴趣的童 鞋分享讨论。部分资源出⾃其他博客，旨将零散知识点集中到⼀起，如果有侵犯您的权利，请联系lipan2@163.com。⼤部分内容均系原创，欢迎⼤家转载分享，但转载的同时别忘了注明作者和原⽂链 接哦。

⼆、MongoDB简介

MongoDB是⼀个⾼性能，开源，⽆模式的⽂档型数据库，是当前NoSql数据库中⽐较热⻔的⼀ 种。它在许多场景下可⽤于替代传统的关系型数据库或键/值存储⽅式。Mongo使⽤C+开发。Mongo 的官⽅⽹站地址是： ，读者可以在此获得更详细的信息。 ⼩插曲：什么是NoSql？

htp:/ w.mongodb.org/

NoSql，全称是 Not Only Sql,指的是⾮关系型的数据库。下⼀代数据库主要解决⼏个要点：⾮关 系型的、分布式的、开源的、⽔平可扩展的。原始的⽬的是为了⼤规模web应⽤，这场运动开始于 209年初，通常特性应⽤如：模式⾃由、⽀持简易复制、简单的API、最终的⼀致性（⾮ACID）、⼤ 容量数据等。NoSQL被我们⽤得最多的当数key-value存储，当然还有其他的⽂档型的、列存储、图型 数据库、xml数据库等。

特点: ⾼性能、易部署、易使⽤，存储数据⾮常⽅便。主要功能特性有：

⾯向集合存储，易存储对象类型的数据。

模式⾃由。

⽀持动态查询。

⽀持完全索引，包含内部对象。

⽀持查询。

⽀持复制和故障恢复。

使⽤⾼效的⼆进制数据存储，包括⼤型对象（如视频等）。

⾃动处理碎⽚，以⽀持云计算层次的扩展性

⽀持Python，PHP，Ruby，Java，C，C#，Javascript，Perl及C+语⾔的驱动程序，社区中也提 供了对Erlang及.NET等平台的驱动程序。

⽂件存储格式为BSON（⼀种JSON的扩展）。

可通过⽹络访问。

功能:

⾯向集合的存储：适合存储对象及JSON形式的数据。

动态查询：Mongo⽀持丰富的查询表达式。查询指令使⽤JSON形式的标记，可轻易查询⽂档中内 嵌的对象及数组。

完整的索引⽀持：包括⽂档内嵌对象及数组。Mongo的查询优化器会分析查询表达式，并⽣成⼀个 ⾼效的查询计划。

查询监视：Mongo包含⼀个监视⼯具⽤于分析数据库操作的性能。

复制及⾃动故障转移：Mongo数据库⽀持服务器之间的数据复制，⽀持主-从模式及服务器之间的 相互复制。复制的主要⽬标是提供冗余及⾃动故障转移。

⾼效的传统存储⽅式：⽀持⼆进制数据及⼤型对象（如照⽚或图⽚）

⾃动分⽚以⽀持云级别的伸缩性：⾃动分⽚功能⽀持⽔平的数据库集群，可动态添加额外的机器。

适⽤场合:

⽹站数据：Mongo⾮常适合实时的插⼊，更新与查询，并具备⽹站实时数据存储所需的复制及⾼度 伸缩性。

缓存：由于性能很⾼，Mongo也适合作为信息基础设施的缓存层。在系统重启之后，由Mongo搭建 的持久化缓存层可以避免下层的数据源 过载。

⼤尺⼨，低价值的数据：使⽤传统的关系型数据库存储⼀些数据时可能会⽐较昂贵，在此之前，很 多时候程序员往往会选择传统的⽂件进⾏存储。

⾼伸缩性的场景：Mongo⾮常适合由数⼗或数百台服务器组成的数据库。Mongo的路线图中已经包 含对MapReduce引擎的内置⽀持。

⽤于对象及JSON数据的存储：Mongo的BSON数据格式⾮常适合⽂档化格式的存储及查询。

# 三、下载安装和配置

安装Mongo数据库： 在发布本⽂的时间官⽅提供的最新版本是：1.6.5 ，如果不做特殊声明，本教程所⽤的版本将会是

这个版本。

- 1.
- 2.
- 3.


第⼀步：下载安装包： ←单击此处,如果是win系统，注意是64位还是32位版本的， 请选择正确的版本。

官⽅下载地址

第⼆步：新建⽬录“D:\MongoDB”，解压下载到的安装包，找到bin⽬录下⾯全部.exe⽂件，拷⻉到 刚创建的⽬录下。 第三步：在“D:\MongoDB”⽬录下新建“data”⽂件夹，它将会作为数据存放的根⽂件夹。

配置Mongo服务端： 打开CMD窗⼝，按照如下⽅式输⼊命令： > d: > cd D:\MongoDB > mongod-dbpath D:\MongoDB\data 配置成功后会看到如下画⾯：

![image 1](<MongoDB——windows2.note_images/imageFile1.png>)

htp:/localhost:27017/

在浏览器输⼊： ，可以看到如下提示： You are trying to aces MongoDB on the native driver port. For htp diagnostic aces, ad 1 0 to the port number

如此，MongoDB数据库服务已经成功启动了。

四、后记

现在我们已经初步实现了MongoDB的安装和服务的启动⼯作。后⾯我们还有很多的⼯作，要使 MongoDB能在我们的C#代码中被操作，还需要驱动，经常有朋友在⽹上copy代码后发现编译不通 过，那估计是驱动不⼀致的问题了，下篇⽂章会详细讲解，未完待续。。

传统的关系数据库⼀般由数据库（database）、表（table）、记录（record）三个层次概念组成， MongoDB是由（database）、集合（colection）、⽂档对象（document）三个层次组成。 MongoDB对于关系型数据库⾥的表，但是集合中没有列、⾏和关系概念，这体现了模式⾃由的特点。

⼀、关于MongoDB的驱动

MongoDB⽀持多种语⾔的驱动，在此我们只介绍C#的驱动。仅C#驱动都有很多种，每种驱动的 形式⼤致相同，但是细节各有千秋，因此代码不能通⽤。⽐较常⽤的是官⽅驱动和samus驱动。 samus驱动除了⽀持⼀般形式的操作之外，还⽀持linq⽅式操纵数据。各⼈⽐较喜欢这种⽅式。

官⽅驱动下载地址： samus驱动下载地址：

点击下载 点击下载

本篇将从samus驱动⼊⼿讲解数据库访问，国际惯例，存取“Helo World!”。

# ⼆、通过samus驱动实现HeloWorld存取

在进⾏下述操作之前，请先确定MongoDB服务已经开启，不知道怎么开启服务，请看上篇。下载 驱动，新建控制台项⽬，并添加对MongoDB.dl的引⽤，如果你下载的是驱动源码，编译⼀遍引⽤⽣成 的DL即可。

基本代码如下：

?

/链接字符串 string conectionString = " " ;

mongodb:/localhost

/数据库名 string databaseName = "myDatabase" ;

/集合名 string colectionName = "myColection" ;

/定义Mongo服务 Mongo mongo = new Mongo(conectionString);

/获取databaseName对应的数据库，不存在则⾃动创建 MongoDatabase mongoDatabase = mongo.GetDatabase(databaseName) as MongoDatabase;

/获取colectionName对应的集合，不存在则⾃动创建 MongoColection<Document> mongoColection = mongoDatabase.GetColection<Document>(cole as MongoColection<Document>;

/链接数据库 mongo.Conect(); try {

/定义⼀个⽂档对象，存⼊两个键值对

Document doc = new Document();

doc[

"ID" ] = 1;

doc[

"Msg" ] = "Helo World!" ;

/将这个⽂档对象插⼊集合 mongoColection.Insert(doc);

/在集合中查找键值对为ID=1的⽂档对象

Document docFind = mongoColection.FindOne( new Document { { "ID" , 1 } });

/输出查找到的⽂档对象中键“Msg”对应的值，并输出

Console.WriteLine(Convert.ToString(docFind[ "Msg" ]); } finaly {

/关闭链接

mongo.Disconect(); }

运⾏程序，成功打印heloword。同时，我们打开数据⽂件夹，发现多了两个⽂件 “myDatabase.ns”和“myDatabase.0”。

# 三、⼩结

htp:/files.cnblogs.com/lipan/MongoDB_01.rar

代码下载： 本篇简洁的讲解了基本存取操作，下篇将结合MVC框架通过MongoDB实现model层单个集合的基

本增删查改操作。

看到下图，是通过Jqgrid实现表格数据的基本增删查改的操作。表格数据增删改是⼀般企业应⽤系 统开发的常⻅功能，不过不同的是这个表格数据来源是⾮关系型的数据库MongoDB。nosql虽然概念 新颖，但是MongoDB基本应⽤实现起来还是⽐较轻松的，甚⾄代码⽐基本的ADO.net访问关系数据源 还要简洁。由于其本身的“⾮关系”的数据存储⽅式，使得对象关系映射这个环节对于MongoDB来讲显 得毫⽆意义，因此我们也不会对MongoDB引⼊所谓的“ORM”框架。

![image 2](<MongoDB——windows2.note_images/imageFile2.png>)

下⾯我们将逐步讲解怎么在MVC模式下将MongoDB数据读取，并展示在前台Jqgrid表格上。这个 “简易系统”的基本设计思想是这样的：我们在视图层展示表格，Jqgrid相关Js逻辑全部放在⼀个Js⽂件 中，控制层实现了“增删查改”四个业务，MongoDB的基本数据访问放在了模型层实现。下⾯我们⼀步 步实现。

# ⼀、实现视图层Jqgrid表格逻辑

⾸先，我们新建⼀个MVC空⽩项⽬，添加好jQuery、jQueryUI、Jqgrid的前端框架代码： 然后在Views的Home⽂件夹下新建视图“Index.aspx”，在视图的body标签中添加如下HTML代

码：

?

< div >

< table id = "table1" >

</ table >

< div id = "div1" >

</ div > </ div >

接着新建Scripts\Home⽂件夹，在该⽬录新建“Index.js”⽂件，并再视图中引⽤，代码如下：

View Code

# ⼆、实现控制层业务

在Controlers⽬录下新建控制器“HomeControler.cs”，Index.js中产⽣了四个ajax请求，对应控制 层也有四个业务⽅法。HomeControler代码如下：

?

public clas HomeControler : Controler {

UserModel userModel = new UserModel();

public ActionResult Index()

{

return View();

} / <sumary> / 获取全部⽤户列表，通过json将数据提供给jqGrid / </sumary>

public JsonResult UserList(

tring ord, tring idx, string rows, string page) {

var list = userModel.FindAl(); nt

i = 0; var query = from u

in list

select new

{

id = i +, cel =

new string []{

u[

"UserId" ].ToString(),

u[

"UserName" ].ToString(),

u[

"Age" ].ToString(),

u[

"Tel" ].ToString(),

u[

"Email" ].ToString(),

"-"

} };

var data = new

{

total = query.Count() / Convert.ToInt32(rows) + 1, page = Convert.ToInt32(page), records = query.Count(), rows = query.Skip(Convert.ToInt32(rows) * (Convert.ToInt32(page) - 1).Take(Convert.ToInt3

};

return Json(data, JsonRequestBehavior.AlowGet);

} / <sumary> / 响应Js的“Ad”ajax请求，执⾏添加⽤户操作 / </sumary>

public ContentResult Ad( string UserId, string UserName, int Age, string Tel, string Email) {

Document doc = new Document();

doc[ "UserId" ] = UserId;

doc[

"UserName" ] = UserName; doc[ "Age" ] = Age;

doc[ "Tel" ] = Tel;

doc[ "Email" ] = Email;

try

{

userModel.Ad(doc);

return Content( "添加成功" );

} catch {

return Content( "添加失败" );

}

} / <sumary> / 响应Js的“Delete”ajax请求，执⾏删除⽤户操作 / </sumary>

public ContentResult Delete( string UserId)

{ try

{

userModel.Delete(UserId);

return Content( "删除成功"

"删除失败" );

}

} / <sumary> / 响应Js的“Update”ajax请求，执⾏更新⽤户操作 / </sumary>

public ContentResult Update( string UserId, string UserName, int Age, string Tel, string Email) {

Document doc = new Document();

doc[ "UserId" ] = UserId;

doc[ "UserName" ] = UserName; doc[ "Age" ] = Age;

doc[ "Tel" ] = Tel;

doc[ "Email" ] = Email;

try

{

userModel.Update(doc);

return Content( "修改成功"

"修改失败" );

} }

}

# 三、实现模型层数据访问

最后，我们在Models新建⼀个Home⽂件夹，添加模型“UserModel.cs”，实现MongoDB数据库访 问代码如下：

?

public clas UserModel {

/链接字符串(此处三个字段值根据需要可为读配置⽂件) public string conectionString = " " ;

mongodb:/localhost

/数据库名

public string databaseName = "myDatabase" ;

/集合名

public string colectionName = "userColection" ;

private Mongo mongo;

private MongoDatabase mongoDatabase;

private MongoColection<Document> mongoColection;

public UserModel()

{

mongo = new Mongo(conectionString);

mongoDatabase = mongo.GetDatabase(databaseName)

as MongoDatabas;

mongoColection = mongoDatabase.GetColection<Document>(colectionName)

as MongoColection<Document>;

mongo.Conect();

} ~UserModel() {

mongo.Disconect();

} / <sumary> / 增加⼀条⽤户记录 / </sumary> / <param name="doc"></param>

public void Ad(Document doc)

{

mongoColection.Insert(doc);

} / <sumary> / 删除⼀条⽤户记录 / </sumary>

public void Delete( string UserId)

{

mongoColection.Remove( new Document { { "srI " , UserId } });

} / <sumary> / 更新⼀条⽤户记录

/ </sumary> / <param name="doc"></param>

public voi Update(Document doc)

{

mongoColection.FindAndModify(doc, new Document { { "UserId" , doc[ "UserId" ].ToString() } });

} / <sumary> / 查找所有⽤户记录 / </sumary> / <returns></returns>

public IEnumerable<Document> FindAl()

{

return mongoColection.FindAl().Documents;

} }

# 四、⼩结

htp:/files.cnblogs.com/lipan/MongoDB_03.rar

代码下载： ⾃此为⽌⼀个简单MongoDB表格数据操作的功能就实现完毕了，相信读者在看完这篇⽂章后，差

不多都可以轻松实现MongoDB项⽬的开发应⽤了。聪明的你⼀定会⽐本⽂做的功能更完善，更好。下 篇计划讲解linq的⽅式访问数据集合。

MongoDB的集合（colection）可以看做关系型数据库的表，⽂档对象（document）可以看做关系型 数据库的⼀条记录。但两者并不完全对等。表的结构是固定的，MongoDB集合并没有这个约束；另 外，存⼊集合的⽂档对象甚⾄可以嵌⼊⼦⽂档，或者“⼦集合”。他们最终都可以⽤类似于BJSON的格 式描述。我们今天就来分析MongoDB这⼀特性带来的独特数据管理⽅式。我们还是以samus驱动为例 来分析，samus驱动⽀持两种⽅式访问数据库，基本⽅式和linq⽅式，基本⽅式在上篇以介绍过，linq ⽅式我不想单独讲解应⽤实例，这篇我会⽤两种⽅式来对⽐介绍。

# ⼀、包含⼦⽂档的集合操作

有这么⼀个应⽤场景，某⽹站提供会员登录的功能，⽤户需要注册账号才能享受会员服务，但是 注册者可能会因为⽤户资料表单输⼊项过⼤⽽放弃填写，因此⽤户信息分为主要资料和详细资料两 项，初次注册只需要填写主要资料就⾏了。我们打算把详细信息设计为⼦⽂档存储。

1) linq⽅式实现

1. 新建数据描述类，描述⽤户信息

?

/ <sumary> / ⽤户主要资料 / </sumary>

public clas UserInfo {

public string UserId { get ; set ; }

public string UserName { get ; set ; }

public string PasWord { get ; set ; }

public Detail Detail { get ; set ; } }

/ <sumary> / ⽤户详细资料 / </sumary>

public clas Detail

{

public string Adres { get ; set ; }

public int Age { get ; set ; }

public string Email { get ; set ; } }

2. 我们要新建⼀个⽤户业务操作类“UserBL”。这个时候要让驱动知道UserInfo类描述了“⽤户资 料”的字段信息，在GetMongo()⽅法实现了配置步骤，UserBL完整代码如下：

?

public clas UserBL {

public string conectionString = " " ;

mongodb:/localhost

public string databaseName = "myDatabase" ;

private Mongo mongo;

private MongoDatabase mongoDatabase;

/注意这⾥泛型类型为“UserInfo”

private MongoColection<UserInfo> mongoColection;

public UserBL()

{

ongo = GetMongo(); mongoDatabase = mongo.GetDatabase(databaseName)

as MongoDatabas;

mongoColection = mongoDatabase.GetColection<UserInfo>()

as MongoColection<UserInfo>;

mongo.Conect();

} ~UserBL() {

mongo.Disconect();

} / <sumary> / 配置Mongo,将类UserInfo映射到集合 / </sumary>

private Mongo GetMongo()

{

var config = new MongoConfigurationBuilder();

config.Maping(maping => {

maping.DefaultProfile(profile => {

profile.SubClasesAre(t => t.IsSubclasOf(

typeof (UserInfo);

}); maping.Map<UserInfo>();

}); config.ConectionString(conectionString);

return new Mongo(config.BuildConfiguration();

} }

- 3. 接着，在“UserBL”类中定义⼀个⽅法“InsertSomeData()”来插⼊⼀些数据：
- 4. 定义⼀个查找数据的⽅法“Select”，它将查找⽤户详细信息中，地址在湖北的全部⽤户：


View Code

?

/ <sumary> / 查询详细资料地址为湖北的⽤户信息 / </sumary>

public List<UserInfo> Select() {

return mongoColection.Linq().Where(x => x.Detail.Adres = "湖北" ).ToList(); }

- 5. 还定义⼀个删除数据的⽅法，将删除集合全部数据：
- 6. 在Main⽅法中添加如下代码：


?

/ <sumary> / 删除全部⽤户信息 / </sumary>

public void DeleteAl() {

mongoColection.Remove(x => true ); }

?

static void Main( string [] args) {

UserBL userBl = new UserBL();

userBl.InsertSomeData(); var users = userBl.Select();

foreach (var user in users) {

Console.WriteLine(user.UserName + "是湖北⼈" );

}; userBl.DeleteAl();

}

7. 最后执⾏程序，打印如下信息：

?

李四是湖北⼈ 赵六是湖北⼈

1) 普通实现 普通⽅式实现不想多讲，直接贴代码，看看与linq⽅式有什么区别：

View Code

最后，我们通过这段代码输出全部⽤户资料信息的BJSON格式：

?

/ <sumary> / 打印数据BJSON / </sumary>

public void PrintBJSON() {

string BJSON = string

.Empty; foreach (var documet in mongoColection.FindAl().Documents)

{

BJSON += documet.ToString();

} Console.WriteLine(BJSON);

}

结果如下：

?

{ "UserId" :

- "101" , "UserName" : "张三" , "PasWord" : "123456" , "_id" :

- "4d80ec1ab8a473138 01" } { "UserId" :


- "102" , "UserName" : "李四" , "PasWord" : "123456"


, "Detail" : { "Adres" : "湖北" , "Age" : 20, "Email" : "lisi@163.com" }, "_id" :

- "4d80ec1ab8a473138 02" } { "UserId" :

"103" , "UserName" : "王五" , "PasWord" : "123456" , "Detail" : { "Adres" : "⼴东" , "Age" : 20, "Email" : "wangwu@163.com" }, "_id" :

- "4d80ec1ab8a473138 03" } { "UserId" :


- "104" , "UserName" : "赵六" , "PasWord" :


"123456" , "Detail" : { "Adres" : "湖北" }, "_id" : "4d80ec1ab8a473138 04" }

# ⼆、包含“⼦集合”的集合操作

同样举个例⼦：有⼀个学校⼈事管理系统要统计班级和学⽣的信息，现在定义了⼀个“班级集 合”，这个集合⾥⾯的学⽣字段是⼀个“学⽣集合”，包含了本班全部学⽣。

1) linq⽅式实现 基础配置我就不多说了，数据类定义如下：

?

/ <sumary> / 班级信息 / </sumary>

public clas ClasInfo {

public string ClasName { get ; set ; }

public List<Student> Students { get ; set ; } }

/ <sumary> / 学⽣信息 / </sumary>

public clas Student {

public string Name { get ; set ; }

public int Age { get ; set ; } }

查询叫“张三”的学⽣在哪个班级，以及他的详细信息： (这⾥其实是ToList后在内存中查的,linq⽅式直接查询好像驱动不⽀持。)

?

public List<ClasInfo> Select() {

return mongoColection.Linq().ToList().Where(x => x.Students.Exists(s => s.Name = "张三"

).ToList(); }

1) 普通实现 查询叫“张三”的学⽣在哪个班级，以及他的详细信息：

?

public List<Document> Select() {

var mongocolection = mongoDatabase.GetColection( "ClasInfo" );

return mongocolection.Find( new Document { { "Students.Name" , "张三" } }).Documents.ToList(); }

打印数据的BJSON：

?

{ "_id" : "4d814bae5c5f 05f63" , "ClasName" : "101" , "Students" : [ { "Name" : "张三" , "Age" : 10 }, { "Name" :

"李四" , "Age"

- : 0 } ] } { "_id" :

- "4d814bae5c5f 05f64" , "ClasName" :

- "102" , "Students" : [ ] } { "_id" :

"4d814bae5c5f 05f65" , "ClasName" :

- "103" , "Students" : [ { "Name" : "王五" , "Age"




- : 1 }, { "Name" : "赵六" , "Age" : 9 } ] }


# 三、⼩结

通过本节例⼦我们发现，MongoDB有它独特的⽂档结构可以描述数据对象之间的⼀些关系特征。 它虽然没有关系型数据库多表符合查询那样强⼤的表间查询⽅式，但也可以通过⽂档结构描述更灵活 的关系特性，可以这么说，关系型数据库能做的，MongoDB基本上也可以做到。甚⾄有些关系数据库 不容易做到的，MongoDB也可以轻松做到，⽐如，描述数据类的继承关系等。

由于MongoDB的⽂档结构为BJSON格式（BJSON全称：Binary JSON），⽽BJSON格式本身就⽀ 持保存⼆进制格式的数据，因此可以把⽂件的⼆进制格式的数据直接保存到MongoDB的⽂档结构中。 但是由于⼀个BJSON的最⼤⻓度不能超过4M，所以限制了单个⽂档中能存⼊的最⼤⽂件不能超过 4M。为了提供对⼤容量⽂件存取的⽀持，samus驱动提供了“GridFS”⽅式来⽀持，“GridFS”⽅式⽂件 操作需要引⼊新的程序集“MongoDB.GridFS.dl”。下⾯我们分别⽤两种⽅式来实现。

# ⼀、在⽂档对象中存取⽂件

当⽂件⼤⼩较⼩的时候，直接存⼊⽂档对象实现起来更简洁。⽐如⼤量图⽚⽂件的存取等，⼀般 图⽚⽂件都不会超过4M。我们先实现⼀个上传图⽚存⼊数据库，再取出来写回⻚⾯的例⼦：

- 1. 把图⽚存到BJSON中
- 2. 获取BJSON⽅式存储的图⽚字节数据


?

/ <sumary> / 把图⽚存到BJSON中 / </sumary>

public void SaveImgBJSON( byte [] byteImg) {

Document doc = new Document();

doc[

"ID" ] = 1;

doc[ "Img" ] = byteImg;

mongoColection.Save(doc); }

?

/ <sumary> / 获取BJSON⽅式存储的图⽚字节数据 / </sumary>

public byte [] GetImgBJSON() {

Document doc= mongoColection.FindOne( new Document { { "ID" , 1 } }); return doc[ "Img" ] as Binary; }

上⾯两段代码是在对MongoDB相关操作进⾏BL封装类中添加的两个⽅法，封装⽅式查看上节内 容。下⾯看看在webform中如何调⽤：

在界⾯拖出⼀个FileUpload控件和⼀个Buton控件，⻚⾯cs类加如下⽅法：

?

protected void Buton1_Click( object sender, EventArgs e) {

ImgBL imgBl = new ImgBL();

lDeleteAl(); imgBl.SaveImgBJSON(FileUpload1.FileBytes); Response.BinaryWrite(imgBl.GetImgBJSON();

}

# ⼆、⽤GridFS⽅式存取⽂件

在实现GridFS⽅式前我先讲讲它的原理，为什么可以存⼤⽂件。驱动⾸先会在当前数据库创建两 个集合："fs.files"和"fs.chunks"集合，前者记录了⽂件名，⽂件创建时间，⽂件类型等基本信息；后 者分块存储了⽂件的⼆进制数据（并⽀持加密这些⼆进制数据）。分块的意思是把⽂件按照指定⼤⼩ 分割，然后存⼊多个⽂档中。"fs.files"怎么知道它对应的⽂件⼆进制数据在哪些块呢？那是因为 在"fs.chunks"中有个"files_id"键，它对应"fs.files"的"_id"。"fs.chunks"还有⼀个键(int型)"n"，它表 明这些块的先后顺序。这两个集合名中的"fs"也是可以通过参数⾃定义的。

如果你只是想知道怎么⽤，可以忽略上⾯这段话，下⾯将⽤法：

## 1. GridFS⽅式的⽂件新建，读取，删除 ?

private string GridFsSave( byte [] byteFile) {

stri g filename = Guid.NewGuid().ToString();

/这⾥GridFile构造函数有个重载，bucket参数就是⽤来替换那个创建集合名中默认的"fs"的。

GridFile gridFile = new GridFile(mongoDatabase);

using (GridFileStream gridFileStream = gridFile.Create(filename)

{

gridFileStream.Write(byteFile, 0, byteFile.Length); }

return filename; } private byte [] GridFsRead( stri g filename) {

GridFile gridFile = new GridFile(mongoDatabase);

GridFileStream gridFileStream = gridFile.OpenRead(filename);

byte [] bytes = new byte [gridFileStream.Length];

gridFileStream.Read(bytes, 0, bytes.Length);

return bytes; } private vo GridFsDelete( stri g filename) {

GridFile gridFile = new

GridFile(mongoDatabase);

gridFile.Delete( new Document( "file e" , filename); }

2. 再次封装GridFS操作，新⽂档只存储⽂件名称，相当于只是⼀个键，新⽂档还可以有除“⽂件 名”之外其他的键。

?

/ <sumary> / 把图⽚存到GridFS中 / </sumary>

public void SaveImgGridFS( byte [] byteImg) {

stri g filename = GridFsSave(byteImg);

Document doc = new Document();

doc[

"ID" ] = 1;

doc[ "filename" ] = filename;

mongoColection.Save(doc); }

/ <sumary> / 获取GridFS⽅式存储的图⽚ / </sumary>

public byte [] GetImgGridFS() {

Document doc = mongoColection.FindOne( new Document { { "ID" , 1 } }); stri g filename = doc[ "filename" ].ToString(); return GridFsRead(filename); }

# 三、⼩结

⽂件存取应该不是很难，值得注意的地⽅是：⽤第⼀种⽅式从⽂档中读出⼆进制数据时，⼀定要 将类型转换为“Binary”类型；还有系统⾃带的键“_id”，它也不是string类型，是“Oid”类型的。

MongoDB中的索引其实类似于关系型数据库，都是为了提⾼查询和排序的效率的，并且实现原理也 基本⼀致。由于集合中的键(字段)可以是普通数据类型，也可以是⼦⽂档。MongoDB可以在各种类型 的键上创建索引。下⾯分别讲解各种类型的索引的创建，查询，以及索引的维护等。

# ⼀、创建索引

1. 默认索引

MongoDB有个默认的“_id”的键，他相当于“主键”的⻆⾊。集合创建后系统会⾃动创建⼀个索引在 “_id”键上，它是默认索引，索引名叫“_id_”，是⽆法被删除的。我们可以通过以下⽅式查看：

?

var _idIndex = mongoColection.Metadata.Indexes.Single(x => x.Key = "_id_" ); Console.WriteLine(_idIndex);

2. 单列索引

在单个键上创建的索引就是单列索引，例如我们要在“UserInfo”集合上给“UserName”键创建⼀个 单列索引，语法如下：（1表示正序，-1逆序）

?

mongoColection.Metadata.CreateIndex( new Document { { "UserName" , 1 } }, false );

接着，我们⽤同样⽅法查找名为“_UserName_”的索引

?

var _UserName_Index = mongoColection.Metadata.Indexes.Single(x => x.Key = "_UserName_" ); Console.WriteLine(_UserName_Index);

3.组合索引

另外，我们还可以同时对多个键创建组合索引。如下代码创建了按照“UserId”正序，“UserName” 逆序的组合索引:

?

mongoColection.Metadata.CreateIndex( new Document { { "UserId" , 1 }, { "UserName" , -1 } }, false );

4.⼦⽂档索引

我们可以对⽂档类型的键创建各种索引，例如单列索引，如下创建⽤户详细信息“Detail”的单列索 引：

?

mongoColection.Metadata.CreateIndex( new Document { { "Detail" , 1 } }, false );

对⼦⽂档的键创建组合索引：例如在“Detail.Adres”和“Detail.Age”上创建组合索引：

?

mongoColection.Metadata.CreateIndex( new Document { { "Detail.Adres" , 1 }, { "Detail.Age" , -1 } }, false );

5.唯⼀索引

唯⼀索引限制了对当前键添加值时，不能添加重复的信息。值得注意的是，当⽂档不存在指定键 时，会被认为键值是“nul”，所以“nul”也会被认为是重复的，所以⼀般被作为唯⼀索引的键，最好都 要有键值对。

对“UserId”创建唯⼀索引(这时候最后⼀个参数为“true”)：

?

mongoColection.Metadata.CreateIndex( new Document { { "UserId" , 1 } }, true );

# ⼆、维护索引

1. 查询索引 通过索引名查询的⽅式已有介绍。但有时候，我们可能忘记了索引名，怎么查询呢？ 下⾯提供⼀个遍历全部索引的⽅法，打印全部索引信息：

?

foreach (var index in mongoColection.Metadata.Indexes)

{

Console.WriteLine(index.Value); }

输出结果示例：

?

{ "name" : "_id_" , "ns" : "myDatabase.UserInfo" , "key" : { "_id" : 1 } } { "name" : "_UserId_unique_" , "ns" : "myDatabase.UserInfo" , "key" : { "UserId" : 1 }, "unique" : true , "_id" :

- "4d8f406ab8a4730b78 05" } { "name"


- "_UserName_" , "ns" : "myDatabase.UserInfo" , "key" : { "UserName" : 1 }, "unique" : false , "_id" :
- "4d8f406ab8a4730b78 06" } { "name" : "_Detail.Adres_Detail.Age_" , "ns" : "myDatabase.UserInfo" , "key" : { "Detail.Adres" : 1, "Detail.Age" : -1 }, "unique" : false , "_id" :

- "4d8f406ab8a4730b78 07" } { "name" : "_UserId_UserName_" , "ns" : "myDatabase.UserInfo" , "key" : { "UserId" : 1, "UserName" : -1 }, "unique"


false , "_id" :

- "4d8f406ab8a4730b78 08" } { "name" : "_Detail_" , "ns" : "myDatabase.UserInfo" , "key" : { "Detail" : 1 }, "unique" : false , "_id" :

- "4d8f406ab8a4730b78 09" }


可⻅，集合的索引也是通过⼀个集合来维护的。name表示索引名，ns表示索引属于哪个库哪个集 合，key表示索引在哪个键上，正序还是逆序，unique表示是否为唯⼀索引，等等 .

2. 删除索引

新⼿常陷⼊的误区是，认为集合被删除，索引就不存在了。关系型数据库中，表被删除了，索引 也不会存在。在MongoDB中不存在删除集合的说法，就算集合数据清空，索引都是还在的，要移除索 引还需要⼿⼯删除。

例如，删除名为“_UserName_”的索引：

?

mongoColection.Metadata.DropIndex( "_UserName_" );

下⾯提供删除除默认索引外其他全部索引的⽅法:

?

DropAlIndex() {

var listIndexes = mongoColection.Metadata.Indexes.ToList();

for (

nt i = 0; i < listIndexes.Count; i +) {

if (listIndexes[i].Key != "_id_" )

{

mongoColection.Metadata.DropIndex(listIndexes[i].Key); }

} }

# 三、索引的效率

MongoDB的索引到底能不能提⾼查询效率呢？我们在这⾥通过⼀个例⼦来测试。⽐较同样的数据 在⽆索引和有索引的情况下的查询速度。

⾸先，我们通过这样⼀个⽅法插⼊10W条数据:

?

InsertBigData() {

var random = new Random();

for (

nt i = 1; i < 1 0; i +) {

Document doc = new Document();

doc[

"ID" ] = i;

doc[ "Data" ] = "data"

+ randm.Next(1 0); mongoColection.Save(doc);

} Console.WriteLine(

"当前有"

+ mongoColection.FindAl().Documents.Count() + "条数据"

); }

然后，实现⼀个⽅法⽤来创建索引:

?

public void CreateIndexForData() {

mongoColection.Metadata.CreateIndex( new Document { { "Data" , 1 } }, false ); }

还有排序的⽅法：

?

SortForData() {

mongoColection.FindAl().Sort( new Document { { "Data" , 1 } }); }

运⾏测试代码如下:

?

static void Main( string [] args) {

IndexBL indexBl = new IndexBL();

ndex l ropAlIndex();

e lDeleteAl(); indexBl.InsertBigData(); Stopwatch watch1 =

new Stopwatch();

watch1.Start();

for (

nt i = 0; i < 1; i +) indexBl.SortForData(); Console.WriteLine( "⽆索引排序执⾏时间："

- + watch1.Elapsed); indexBl.CreateIndexForData(); Stopwatch watch2 =

new Stopwatch();

watch2.Start();

for (

nt i = 0; i < 1; i +) indexBl.SortForData(); Console.WriteLine( "有索引排序执⾏时间："

- + watch2.Elapsed); }


最后执⾏程序查看结果：

![image 3](<MongoDB——windows2.note_images/imageFile3.png>)

## 多次测试表明在有索引的情况下，查询效率要⾼于⽆索引的效率。

