在研究mycat源码之前必须先把环境搭建好。这篇⽂章的⽬标就是搭建mycat源码调试环境。环境主要 包括:

git jdk maven eclipse mysql

这⾥假设你知道上⾯的知识点。我们搭建的环境所处于的操作系统是Windows7+。下⾯将⼀步⼀步讲 解如何搭建源码调试环境:

## 1. 环境搭建

### 1.1 获取mycat源码

mycat源码托管在github上⾯，为了以后实时获取源码，我们需要先安装并配置好git客户端，请根据以 下教程安装配置好git:

git安装教程

安装成功后，我们在本地某个⽂件路径下，⿏标右键打开Git Bash，执⾏以下命令将代码克隆到当前 ⽂件路径:

git clone https://github.com/MyCATApache/Mycat-Server.git

使⽤以下命令切换到最新1.6分⽀:

git checkout -b 1.6 origin/1.6

如果你对这个项⽬感兴趣并希望为mycat贡献代码，那么可以fork mycat代码到你个⼈的github上， 然后从你⾃⼰fork的项⽬地址上⾯拉取代码。

### 1.2 安装jdk

jdk版本要求7或者更⾼。我们需要安装jdk并配置java环境变量。jdk的安装和环境变量的配置请参考:

JDK安装与环境变量配置

### 1.3 安装maven

mycat项⽬源码使⽤maven进⾏依赖管理、编译打包。因此我们需要安装maven并配置好，请参考以下 教程进⾏配置:

Maven的安装及配置

### 1.4 安装配置eclipse

eclipse是开发Java项⽬常⽤的IDE，我们使⽤它来进⾏源码阅读和调试。eclipse可以在eclipse官⽹下 载:

https://www.eclipse.org/downloads/

我个⼈习惯使⽤STS——Spring Tool Suite，也是eclipse的⼀个扩展，可以在这个地址下载:

http:// spring.io/tools/sts/

完成后打开eclipse，配置maven使⽤我们⾃⼰下载的maven: Windows -> Preferences -> Maven -> Installations 点击Add添加你的maven根⽬录，勾选你⾃⼰的maven，如下所示:

![image 1](<Mycat源码篇 起步,Mycat源码阅读调试环境搭建.note_images/imageFile1.png>)

eclipse-maven-seting

最后点击OK确定即可。 eclipse和maven都依赖jdk环境，所以务必在安装之前先安装好jdk并配置好相应的环境变量。

### 1.5 导⼊mycat源码

完成上⾯配置后，就可以导⼊我们之前拉取的mycat源码:

File -> Import 选择 Existing Maven Projects，浏览选择我们clone的mycat源码，点击确定开始进⾏ 导⼊。第⼀次导⼊需要等待maven下载插件以及mycat项⽬需要⽤到的第三⽅依赖库，等待时间相对⻓ ⼀点。成功完成导⼊后的项⽬如下所示:

![image 2](<Mycat源码篇 起步,Mycat源码阅读调试环境搭建.note_images/imageFile2.png>)

eclipse-mycat-project-structure

### 1.6 安装配置mysql

为了能够更⽅便地在本地开发环境调试mycat，我们最后还需要在我们的机器上安装mysql。建议 windows下下载mysql绿⾊版，然后通过简单配置完成mysql的安装，具体参考以下教程:

Windows 下绿⾊安装Mysql 5.7 （zip压缩包）

根据上⾯教程使⽤root账号成功登陆mysql之后，建议修改root密码，eg:

mysql > set password = password('your password');

好了，到这⾥，我们的环境就算搭建完成了，下⾯我们将介绍如何在eclipse中根据mycat源码启动 mycat。

# 2. IDE启动mycat

- 2.1 配置mycat


为 了 能 够 让 mycat 顺 利 启 动 ， 我 们 ⾸ 先 需 要 进 ⾏ 必 要 的 配 置 。 从 github 上 ⾯ clone 的 源 码 在 src/main/resources ⽬ 录 下 存 在 mycat 运 ⾏ 所 需 要 配 置 的 ⽂ 件 ， 有 schema.xml 、 rule.xml 和 server.xml。这⾥我们采⽤最简⽅式进⾏配置:

- (1) schema.xml <?xml version="1.0"?> <!DOCTYPE mycat:schema SYSTEM "schema.dtd"> <mycat:schema xmlns:mycat="http://io.mycat/">

<schema name="TESTDB" checkSQLschema="false" sqlMaxLimit="100"> <table name="hotnews" primaryKey="ID" autoIncrement="true" dataNode="dn1,dn2,dn3"

rule="mod-long" /> </schema>

- <dataNode name="dn1" dataHost="localhost1" database="db1" />

- <dataNode name="dn2" dataHost="localhost1" database="db2" />

- <dataNode name="dn3" dataHost="localhost1" database="db3" />


<dataHost name="localhost1" maxCon="1000" minCon="10" balance="0" writeType="0" dbType="mysql" dbDriver="native" switchType="1"

slaveThreshold="100"> <heartbeat>select user()</heartbeat> <!-- can have multi write hosts --> <writeHost host="hostM1" url="localhost:3306" user="root"

password="mysql"> <!-- can have multi read hosts --> <!-- <readHost host="hostS2" url="192.168.1.200:3306" user="root" password="xxx"

/> -->

</writeHost> <!-- <writeHost host="hostS1" url="localhost:3316" user="root"

password="123456" /> --> <!-- <writeHost host="hostM2" url="localhost:3316" user="root" password="123456"/> -

->

</dataHost>

</mycat:schema>

这⾥使⽤本地mysql（localhost:3306）进⾏测试，user为root，密码是mysql，根据实际情况修 改。

- (2) rule.xml


<!DOCTYPE mycat:rule SYSTEM "rule.dtd"> <mycat:rule xmlns:mycat="http://io.mycat/">

<tableRule name="mod-long">

<rule> <columns>id</columns> <algorithm>mod-long</algorithm>

</rule> </tableRule>

<function name="mod-long" class="io.mycat.route.function.PartitionByMod"> <!-- how many data nodes --> <property name="count">3</property>

</function>

</mycat:rule>

##### (3) server.xml

<!DOCTYPE mycat:server SYSTEM "server.dtd"> <mycat:server xmlns:mycat="http://io.mycat/">

<system> <property name="useSqlStat">1</property> <!-- 1为开启实时统计、0为关闭 --> <property name="useGlobleTableCheck">0</property> <!-- 1为开启全加班⼀致性检测、0为关闭 --> <property name="defaultSqlParser">druidparser</property> <property name="sequnceHandlerType">0</property>

<!-- <property name="useCompression">1</property>--> <!--1为开启mysql压缩协议--> <!-- <property name="fakeMySQLVersion">5.6.20</property>--> <!--设置模拟的MySQL版本

号--> <!-- <property name="processorBufferChunk">40960</property> --> <!-<property name="processors">1</property> <property name="processorExecutor">32</property>

-->

<!--默认为type 0: DirectByteBufferPool | type 1 ByteBufferArena--> <property name="processorBufferPoolType">0</property> <!--默认是65535 64K ⽤于sql解析时最⼤⽂本⻓度 --> <!--<property name="maxStringLiteralLength">65535</property>--> <!--<property name="sequnceHandlerType">0</property>--> <!--<property name="backSocketNoDelay">1</property>--> <!--<property name="frontSocketNoDelay">1</property>--> <!--<property name="processorExecutor">16</property>--> <!--

<property name="mutiNodeLimitType">1</property> 0：开启⼩数量级（默认） ；1：开启亿级 数据排序

<property name="mutiNodePatchSize">100</property> 亿级数量排序批量 <property name="processors">32</property> <property

name="processorExecutor">32</property>

<property name="serverPort">8066</property> <property name="managerPort">9066</property>

<property name="idleTimeout">300000</property> <property name="bindIp">0.0.0.0</property>

<property name="frontWriteQueueSize">4096</property> <property name="processors">32</property> -->

<!--分布式事务开关，0为不过滤分布式事务，1为过滤分布式事务（如果分布式事务内只涉及全局表，则不过 滤），2为不过滤分布式事务,但是记录分布式事务⽇志-->

<property name="handleDistributedTransactions">0</property>

<!-off heap for merge/order/group/limit 1开启 0关闭

--> <property name="useOffHeapForMerge">1</property>

<!--

单位为m

--> <property name="memoryPageSize">1m</property>

<!--

单位为k

--> <property name="spillsFileBufferSize">1k</property>

<property name="useStreamOutput">0</property>

<!--

单位为m

--> <property name="systemReserveMemorySize">384m</property>

</system>

<user name="root">

<property name="password">mysql</property> <property name="schemas">TESTDB</property>

</user>

</mycat:server>

#### 2.2 mysql数据库准备

登录到本地mysql，创建需要的database和table:

- create database db1 default charset 'utf8';

- create database db2 default charset 'utf8';

- create database db3 default charset 'utf8';


分别在db1、db2、db3下创建hotnews表:

create table hotnews ( id int primary key, title varchar(100), author_id int, create_tm datetime, content text

) engine = innodb default character set = 'utf8';

### 2.3 启动mycat

到了这⼀步，所有的准备已经到位，在Eclipse的mycat项⽬下找到MycatStartup这个类，Run As -> Java Application即可跑起⼀个mycat server。默认mycat server端⼝8066，管理端⼝9066。

Eclipse⾥⾯使⽤Ctrl+Shift+T快捷键，输⼊类名，就可以快速定位到对应的源码⽂件。 这个时候使⽤mysql客户端⼯具，执⾏以下命令登录mycat:

> mysql -uroot -hlocalhost -P8066 -p > 键⼊密码mysql

成功则表示mycat server调试环境已经部署成功了。好了，接下来以Debug默认运⾏，然后在你希望 debug的位置打上断点，就可以尽情地阅读mycat源码了。 eg:

所有发往mycat的sql都会经过ServerQueryHandler的query⽅法，在这个⽅法⾸⾏代码处打上断点，然 后使⽤mysql客户端⼯具登录mycat并执⾏⼀个sql，程序就会定位到你设置的断点那⾥。

## 3. (可选)编译mycat软件包

我们可以通过mycat源码来编译得到mycat软件包，只要我们安装了maven即可。⽅法如下: cmd进⼊到mycat源码根⽬录，即pom.xml所在⽬录，执⾏以下命令进⾏编译打包: mvn package -Dmaven.test.skip=true

-Dmaven.test.skip=true表示忽略mycat的单元测试的执⾏，这样可以节省编译打包时间。 等待执⾏成功后，可以在mycat源码根⽬录的target⼦⽬录⾥⾯，看到如下格式的压缩包: Mycat-server-${version}-${buildtime}-${platform}.tar.gz

其中${version}表示mycat版本号，${buildtime}为我们执⾏编译打包的⽇期时间，${platform}表示使 ⽤的操作系统平台，取值有win、linux、mac、solaris和unix。这样我们就可以拿对应的软件包到对应 的平台上去部署了。

