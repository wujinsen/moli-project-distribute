# 1、基础软件安装(必装项请⾃⾏安装)

PostgreSQL (8.2.15+) or MySQL (5.7系列)：两者任选其⼀即可，如 MySQL 则需要 JDBC Driver 5.1.47+

JDK (1.8+)：必装，请安装好后在/etc/profile下配置 JAVA_HOME 及 PATH 变量

ZoKeper (3.4.6+)：必装

Hadop (2.6+) or MinIO：选装， 如果需要⽤到资源上传功能，针对单机可以选择本地⽂件⽬录作 为上传⽂件夹(此操作不需要部署 Hadop )；当然也可以选择上传到 Hadop or MinIO 集群上

注意：DolphinScheduler 本身不依赖 Hadoop、Hive、Spark，仅会调⽤它们的 Client，⽤于运⾏对应的任务

# 2、下载⼆进制tar.gz包

请下载最新版本的后端安装包⾄服务器部署⽬录，⽐如创建 /opt/dolphinscheduler 做为安装部署⽬ 录，下载地址： 下载，下载后上传 tar 包到该⽬录中，并进⾏解压

# 创建部署⽬录，部署⽬录请不要创建在 /root、/home 等⾼权限⽬录 mkdir -p /opt/dolphinscheduler cd /opt/dolphinscheduler

# 解压缩 tar -zxvf apache-dolphinscheduler-1.3.6-bin.tar.gz -C /opt/dolphinscheduler

mv apache-dolphinscheduler-1.3.6-bin dolphinscheduler-bin

# 3、创建部署⽤户并赋予⽬录操作权限

创建部署⽤户，并且⼀定要配置 sudo 免密。以创建 dolphinscheduler ⽤户为例

# 创建⽤户需使⽤ root 登录 useradd dolphinscheduler

# 添加密码 echo "dolphinscheduler" | passwd --stdin dolphinscheduler

# 配置 sudo 免密 sed -i '$adolphinscheduler ALL=(ALL) NOPASSWD: NOPASSWD: ALL' /etc/sudoers sed -i 's/Defaults requirett/#Defaults requirett/g' /etc/sudoers

# 修改⽬录权限，使得部署⽤户对 dolphinscheduler-bin ⽬录有操作权限 chown -R dolphinscheduler:dolphinscheduler dolphinscheduler-bin

注意：

因为任务执⾏服务是以 sudo -u {linux-user} 切换不同 linux ⽤户的⽅式来实现多租户运⾏作业，所 以部署⽤户需要有 sudo 权限，⽽且是免密的。初学习者不理解的话，完全可以暂时忽略这⼀点

如果发现 /etc/sudoers ⽂件中有 "Default requirety" 这⾏，也请注释掉

如果⽤到资源上传的话，还需要给该部署⽤户分配操作本地⽂件系统或者 HDFS 或者 MinIO的权限

# 4、 sh免密配置

切换到部署⽤户并配置 sh 本机免密登录

su dolphinscheduler

ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys chmod 600 ~/.ssh/authorized_keys 注 意 ： 正 常 设 置 后 ， dolphinscheduler ⽤ 户 在 执 ⾏ 命 令 ssh localhost 是 不 需 要 再 输 ⼊ 密 码的

# 5、数据库初始化

进⼊数据库，默认数据库是 PostgreSQL，如选择 MySQL 的话，后续需要添加 mysql-conectorjava 驱动包到 DolphinScheduler 的 lib ⽬录下

mysql -uroot -p

进⼊数据库命令⾏窗⼝后，执⾏数据库初始化命令，设置访问账号和密码。注: {user} 和 {pasword} 需要替换为具体的数据库⽤户名和密码

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';

mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost' IDENTIFIED BY '{password}';

mysql> flush privileges;

创建表和导⼊基础数据

修改 conf ⽬录下 datasource.properties 中的下列配置

vi conf/datasource.properties

mysql-con ector-java 驱动 jar

如果选择 MySQL，请注释掉 PostgreSQL 相关配置(反之同理)，还需要⼿动添加 [

] 包到 lib ⽬录下，这⾥下载的是 mysql-conector-java-5.1.47.jar，然后正 确配置数据库连接相关信息

# postgre # spring.datasource.driver-class-name=org.postgresql.Driver # spring.datasource.url=jdbc:postgresql://localhost:5432/dolphinscheduler # mysql spring.datasource.driver-class-name=com.mysql.jdbc.Driver spring.datasource.url=jdbc:mysql://xxx:3306/dolphinscheduler?

useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true 需要修改ip，本机localhost即 可

## spring.datasource.username=xxx 需要修改为上⾯ 的{user}值

spring.datasource.password=xxx 需要修改为上⾯ 的{password}值

修改并保存完后，执⾏ script ⽬录下的创建表及导⼊基础数据脚本

sh script/create-dolphinscheduler.sh

注 意 : 如 果 执 ⾏ 上 述 脚 本 提 示 “/bin/java: No such file or directory” 错 误 ， 请 在 /etc/profile 下 配 置 JAVA_HOME 及 PATH 变 量

# 6、修改运⾏参数

修改 conf/env ⽬录下的 dolphinscheduler_env.sh 环境变量(以相关⽤到的软件都安装在 /opt/soft 下为例)

export HADOOP_HOME=/opt/soft/hadoop export HADOOP_CONF_DIR=/opt/soft/hadoop/etc/hadoop # export SPARK_HOME1=/opt/soft/spark1 export SPARK_HOME2=/opt/soft/spark2 export PYTHON_HOME=/opt/soft/python export JAVA_HOME=/opt/soft/java export HIVE_HOME=/opt/soft/hive export FLINK_HOME=/opt/soft/flink export DATAX_HOME=/opt/soft/datax/bin/datax.py export PATH=$HADOOP_HOME/bin:$SPARK_HOME2/bin:$PYTHON_HOME:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOM E/bin:$DATAX_HOME:$PATH

注 意 : 这 ⼀ 步 ⾮ 常 重 要 ， 例 如 JAVA_HOME 和 PATH 是 必 须 要 配 置 的 ， 没 有 ⽤ 到 的 可 以 忽 略 或 者 注 释 掉 ； 如 果 找 不 到 dolphinscheduler_env.sh， 请 运 ⾏ ls -a

将jdk软链到 /usr/bin/java 下(仍以 JAVA_HOME=/opt/soft/java 为例)

sudo ln -s /opt/soft/java/bin/java /usr/bin/java

修改⼀键部署配置⽂件 conf/config/install_config.conf 中的各参数，特别注意以下参数的配置

# 这⾥填 mysql or postgresql dbtype="mysql"

# 数据库连接地址 dbhost="localhost:3306"

# 数据库名 dbname="dolphinscheduler"

# 数据库⽤户名，此处需要修改为上⾯设置的 {user} 具体值 username="xxx"

# 数据库密码，如果有特殊字符，请使⽤ \ 转义，需要修改为上⾯设置的 {password} 具体值 password="xxx"

# Zookeeper地址，单机本机是 localhost:2181，记得把 2181 端⼝带上 zkQuorum="localhost:2181"

# 将 DS 安装到哪个⽬录，如: /opt/soft/dolphinscheduler，不同于现在的⽬录 installPath="/opt/soft/dolphinscheduler"

# 使⽤哪个⽤户部署，使⽤第 3 节创建的⽤户 deployUser="dolphinscheduler"

# 邮件配置，以 qq 邮箱为例 # 邮件协议 mailProtocol="SMTP"

# 邮件服务地址 mailServerHost="smtp.qq.com"

# 邮件服务端⼝ mailServerPort="25"

# mailSender 和 mailUser 配置成⼀样即可 # 发送者 mailSender="xxx@qq.com"

# 发送⽤户 mailUser="xxx@qq.com"

# 邮箱密码 mailPassword="xxx"

# TLS 协议的邮箱设置为 true，否则设置为 false

starttlsEnable="true"

# 开启 SSL 协议的邮箱配置为 true，否则为 false。注意: starttlsEnable 和 sslEnable 不能同时为 true sslEnable="false"

# 邮件服务地址值，参考上⾯ mailServerHost sslTrust="smtp.qq.com"

# 业务⽤到的⽐如 sql 等资源⽂件上传到哪⾥，可以设置：HDFS,S3,NONE，单机如果想使⽤本地⽂件系统，请配置为 HDFS，因为 HDFS ⽀持本地⽂件系统；如果不需要资源上传功能请选择 NONE。强调⼀点：使⽤本地⽂件系统不需要部署 hadoop resourceStorageType="HDFS"

# 这⾥以保存到本地⽂件系统为例 # 注：但是如果你想上传到 HDFS 的话，NameNode 启⽤了 HA，则需要将 hadoop 的配置⽂件 core-site.xml 和 hdfs-site.xml 放到 conf ⽬录下，本例即是放到 /opt/dolphinscheduler/conf 下⾯，并配置 namenode cluster 名称；如果 NameNode 不是 HA，则修改为具体的 ip 或者主机名即可 defaultFS="file:///data/dolphinscheduler" #hdfs://{具体的ip/主机名}:8020

# 如果没有使⽤到 Yarn，保持以下默认值即可；如果 ResourceManager 是 HA，则配置为 ResourceManager 节点 的主备 ip 或者 hostname，⽐如 "192.168.xx.xx,192.168.xx.xx" ;如果是单 ResourceManager 请配置 yarnHaIps="" 即可 # 注：依赖于yarn执⾏的任务，为了保证执⾏结果判断成功，需要确保yarn信息配置正确 yarnHaIps="192.168.xx.xx,192.168.xx.xx"

# 如果 ResourceManager 是 HA 或者没有使⽤到 Yarn 保持默认值即可；如果是单 ResourceManager，请配置真实 的 ResourceManager 主机名或者 ip singleYarnIp="yarnIp1"

# 资源上传根路径，⽀持 HDFS 和 S3，由于 hdfs ⽀持本地⽂件系统，需要确保本地⽂件夹存在且有读写权限 resourceUploadPath="/data/dolphinscheduler"

# 具备权限创建 resourceUploadPath的⽤户 hdfsRootUser="hdfs"

# 配置 api server port apiServerPort="12345"

# 在哪些机器上部署 DS 服务，本机选 localhost ips="localhost"

# ssh端⼝，默认22 sshPort="22"

# master服务部署在哪台机器上

masters="localhost"

# worker服务部署在哪台机器上，并指定此 worker 属于哪⼀个 worker 组，下⾯示例的 default 即为组名 workers="localhost:default"

# 报警服务部署在哪台机器上 alertServer="localhost"

# 后端api服务部署在在哪台机器上 apiServers="localhost"

注 ： 如 果 打 算 ⽤ 到 资 源 中 ⼼ 功 能 ， 请 执 ⾏ 以 下 命 令 ：

sudo mkdir /data/dolphinscheduler sudo chown -R dolphinscheduler:dolphinscheduler /data/dolphinscheduler

- 7、⼀键部署

sh install.sh 注意：第⼀次部署的话，在运⾏中第3步 3,stop server 出现 5 次以下信息，此信息可以忽略 sh: bin/dolphinscheduler-daemon.sh: No such file or directory

MasterServer ----- master服务 WorkerServer ----- worker服务 LoggerServer ----- logger服务 ApiApplicationServer ----- api服务 AlertServer ----- alert服务

如果以上服务都正常启动，说明⾃动部署成功 部署成功后，可以进⾏⽇志查看，⽇志统⼀存放于 logs ⽂件夹内

logs/ ├── dolphinscheduler-alert-server.log ├── dolphinscheduler-master-server.log |—— dolphinscheduler-worker-server.log |—— dolphinscheduler-api-server.log |—— dolphinscheduler-logger-server.log

- 8、登录系统
- 9、启停服务


切换到部署⽤户，执⾏⼀键部署脚本

脚本完成后，会启动以下 5 个服务，使⽤ jps 命令查看服务是否启动(jps 为 JDK ⾃带)

访问前端⻚⾯地址，接⼝ ip (⾃⾏修改) htp:/192.168.x.x:12345/dolphinscheduler

⼀键停⽌集群所有服务

sh ./bin/stop-all.sh

⼀键开启集群所有服务

sh ./bin/start-all.sh

启停 Master

sh ./bin/dolphinscheduler-daemon.sh start master-server sh ./bin/dolphinscheduler-daemon.sh stop master-server

启停 Worker

sh ./bin/dolphinscheduler-daemon.sh start worker-server sh ./bin/dolphinscheduler-daemon.sh stop worker-server

启停 Api

sh ./bin/dolphinscheduler-daemon.sh start api-server sh ./bin/dolphinscheduler-daemon.sh stop api-server

启停 Loger

sh ./bin/dolphinscheduler-daemon.sh start logger-server sh ./bin/dolphinscheduler-daemon.sh stop logger-server

启停 Alert

sh ./bin/dolphinscheduler-daemon.sh start alert-server sh ./bin/dolphinscheduler-daemon.sh stop alert-server 注：服务⽤途请具体参⻅《系统架构设计》⼩节

