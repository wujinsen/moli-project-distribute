三种模式

内嵌模式：元数据保持在内嵌的derby模式，只允许⼀个会话连接

本地独⽴模式：在本地安装Mysql，吧元数据放到mySql内

远程模式：元数据放置在远程的Mysql数据库

- 1

- 2


- 1、下载Hive安装包

- 2、将hive⽂件上传到HADOOP集群，并解压 将⽂件上传到：/export/software tar -zxvf apache-hive-1.2.1-bin.tar.gz -C /export/servers/ cd /export/servers/ ln -s apache-hive-1.2.1-bin hive
- 3、配置环境变量，编辑/etc/profile


# htp:/hive.apache.org/downloads.html

![image 1](<第八节 安装Hive远程模式.note_images/imageFile1.png>)

- 1

- 2


#set hive env export HIVE_HOME=/export/servers/hive export PATH=${HIVE_HOME}/bin:$PATH

#让环境变量⽣效 source /etc/profile

1 4、修改hive配置⽂件

进⼊配置⽂件的⽬录

cd /export/servers/hive/conf/

修改hive-env.sh⽂件 cp hive-env.sh.template hive-env.sh

将以下内容写⼊到hive-env.sh⽂件中 export JAVA_HOME=/export/servers/jdk export HADOP_HOME=/export/servers/hadop export HIVE_HOME=/export/servers/hive

修改log4j⽂件

cp hive-log4j.properties.template hive-log4j.properties

将EventCounter修改成org.apache.hadop.log.metrics.EventCounter #log4j.apender.EventCounter=org.apache.hadop.hive.shims.HiveEventCounter log4j.apender.EventCounter=org.apache.hadop.log.metrics.EventCounter

配置远程登录模式

touch hive-site.xml 将以下信息写⼊到hive-site.xml⽂件中 <configuration>

<property> <name>javax.jdo.option.ConectionURL</name> <value>jdbc:mysql:/hadop02  306/hivedb?createDatabaseIfNotExist=true</value>

</property> <property>

<name>javax.jdo.option.ConectionDriverName</name> <value>com.mysql.jdbc.Driver</value>

</property> <property>

<name>javax.jdo.option.ConectionUserName</name> <value>rot</value>

</property> <property>

<name>javax.jdo.option.ConectionPasword</name> <value>rot</value>

</property> </configuration>

- 5、安装mysql并配置hive数据库及权限


安装mysql数据库及客户端

yum instal mysql-server yum instal mysql service mysqld start

配置hive元数据库

mysql -u root -p create database hivedb;

对hive元数据库进⾏赋权，开放远程连接，开放localhost连接

grant al privileges on.* to rot@"%" identified by "rot" with grant option; grant al privileges on.* to rot@"localhost" identified by "rot" with grant option;

- 6、运⾏hive命令即可启动hive hive


- 附录1：如果报错Terminal initialization failed; falling back to unsupported 将/export/servers/hive/lib ⾥⾯的jline2.12替换了hadoop 中/export/servers/hadoop/hadoop-

2.6.1/share/hadoop/yarn/lib/jline-0.09*.jar

- 附录2：jdbc驱动类

- 附录3：异常信息Loging initialized using configuration in jar:file:/export/servers/apache-hive2.0.0-bin/lib/hive-comon-2.0.0.jar!/hive-log4j2.properties Exception in thread "main" java.lang.RuntimeException: Hive metastore database is not initialized. Please use schematol (e.g. ./schematol -initSchema dbType.) to create the schema. If neded, don't forget to include the option to autocreate the underlying database in your JDBC conection string (e.g. ? createDatabaseIfNotExist=true for mysql)


处理⽅法： schematol -dbType mysql -initSchema

