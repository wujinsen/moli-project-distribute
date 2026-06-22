https://www.runoob.com/mongodb/mongodb-osx-install.html

MongoDB 提供了 OSX 平台上 64 位的安装包，你可以在官⽹下载安装包。 下载地址：

https://www.mongodb.com/download-center#community

从 MongoDB 3.0 版本开始只⽀持 OS X 10.7 (Lion) 版本及更新版本的系统。 接下来我们使⽤ curl 命令来下载安装： # 进⼊ /usr/local cd /usr/local

# 下载 sudo curl -O https://fastdl.mongodb.org/osx/mongodb-osx-ssl-x86_64-4.0.9.tgz

# 解压 sudo tar -zxvf mongodb-osx-ssl-x86_64-4.0.9.tgz

# 重命名为 mongodb ⽬录

sudo mv mongodb-osx-x86_64-4.0.9/ mongodb 安装完成后，我们可以把 MongoDB 的⼆进制命令⽂件⽬录（安装⽬录/bin）添加到 PATH 路径中： export PATH=/usr/local/mongodb/bin:$PATH 创建⽇志及数据存放的⽬录：

数据存放路径：

sudo mkdir -p /usr/local/var/mongodb

⽇志⽂件路径：

sudo mkdir -p /usr/local/var/log/mongodb 接下来要确保当前⽤户对以上两个⽬录有读写的权限： sudo chown runoob /usr/local/var/mongodb sudo chown runoob /usr/local/var/log/mongodb 以上 runoob 是我电脑上对⽤户，你这边需要根据你当前对⽤户名来修改。 接下来我们使⽤以下命令在后台启动 mongodb： mongod --dbpath /usr/local/var/mongodb --logpath /usr/local/var/log/mongodb/mongo.log --fork

--dbpath 设置数据存放⽬录

--logpath 设置⽇志存放⽬录

--fork 在后台运⾏

如果不想在后端运⾏，⽽是在控制台上查看运⾏过程可以直接设置配置⽂件启动： mongod --conﬁg /usr/local/etc/mongod.conf 查看 mongod 服务是否启动： ps aux | grep -v grep | grep mongod 使⽤以上命令如果看到有 mongod 的记录表⽰运⾏成功。 启动后我们可以使⽤ mongo 命令打开⼀个终端： $ cd /usr/local/mfongodb/bin $ ./mongo MongoDB shell version v4.0.9 connecting to: mongodb://127.0.0.1:27017/?gssapiServiceName=mongodb Implicit session: session { "id" : UUID("3c12bf4f-695c-48b2-b160-8420110ccdcf") } MongoDB server version: 4.0.9 …… > 1 + 1 2 >

使⽤ brew 安装 此外你还可以使⽤ OSX 的 brew 来安装 mongodb： brew tap mongodb/brew brew install mongodb-community@4.4 @ 符号后⾯的 4.4 是最新版本号。 安装信息：

配置⽂件：/usr/local/etc/mongod.conf ⽇志⽂件路径：/usr/local/var/log/mongodb 数据存放路径：/usr/local/var/mongodb

运⾏ MongoDB 我们可以使⽤ brew 命令或 mongod 命令来启动服务。 brew 启动： brew services start mongodb-community@4.4 brew 停⽌： brew services stop mongodb-community@4.4 mongod 命令后台进程⽅式： mongod --conﬁg /usr/local/etc/mongod.conf --fork 这种⽅式启动要关闭可以进⼊ mongo shell 控制台来实现：

> db.adminCommand({ "shutdown" : 1 })

config:

<table>
  <tr>
    <th>procesManagement: fork: true<br><br>net: bindIp: localhost port: 27017<br><br>storage: dbPath: /usr/local/var/mongodb<br><br>systemLog: destination: file path: "/usr/local/var/log/mongodb/mongo.log" logApend: true<br><br>storage: journal:</th>
  </tr>
</table>


enabled: true

