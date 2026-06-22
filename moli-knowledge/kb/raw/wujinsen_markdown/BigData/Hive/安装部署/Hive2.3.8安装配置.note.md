htps:/blog.hvnobug.com/post/hive-instal.html#安装

前⾯我们已经搭建了 Hadop 环境,现在开始搭建 Hive 环境 - Hive2.3.8

下载地址

# 安装

下载并解压 BASH

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br></th>
    <th>wget htps:/downloads.apache.org/hive/hive2.3.8/apache-ive-2.3.8-bin.tar.gz tar -zxvf apache-hive-2.3.8-bin.tar.gz -C /opt/bigdata mv /opt/bigdata/apache-hive-2.3.8-bin<br><br></th>
  </tr>
</table>


/opt/bigdata/hive-2.3.8

安装 mysql BASH

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 1 sudo apt-get instal -y mysql-server-5.7

<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>cd /opt/bigdata/hive-2.3.8/lib wget htps:/maven.aliyun.com/repository/public/mys ql/mysql-conector-java/8.0.20/mysql-<br><br></th>
  </tr>
</table>


conector-java-8.0.20.jar

<table>
  <tr>
    <th>1<br><br></th>
    <th>export HIVE_HOME=/opt/bigdata/hive-2.3.8<br><br></th>
  </tr>
</table>


- 2 PATH=$PATH:$HIVE_HOME/bin


输⼊ rot 密码完成安装 下载 mysql jdbc driver BASH

配置环境变量 在 /etc/profile 添加如下配置: SHEL

source /etc/profile 使配置⽣效 配置 hive BASH

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br></th>
    <th>mv conf/hive-env.sh.template conf/hive-env.sh # 重命名环境⽂件 mv conf/hive-log4j2.properties.template conf/hive-log4j2.properties # 重命名⽇志⽂件 cp conf/hive-default.xml.template conf/hive-<br><br>拷 ⽂件<br><br></th>
  </tr>
</table>


site.xml # ⻉⽣成 xml

然后修改 hive-site.xml ⽂件如下配置 XML

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9


<!- hive元数据地址，默认 是/user/hive/warehouse-> <property>

<name>hive.metastore.warehouse.dir</name>

<value>/user/hive/warehouse</value> </property> <!- hive查询时输出列名 -> <property>

- 0

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8


<name>hive.cli.print.header</name> <value>true</value>

</property> <!- 显示当前数据库名 -> <property>

<name>hive.cli.print.curent.db</name> <value>true</value>

</property> <!- 开启本地模式，默认是false-> <property>

19

- 0

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8


<name>hive.exec.mode.local.auto</name> <value>true</value>

</property> <property> <name>javax.jdo.option.ConectionUserName< /name>

29

<value>r t</value> <description>Username to use against

- 0

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8


metastore database</description>

/property> <property> <name>javax.jdo.option.ConectionPasword</ name>

<value>r t</vlue> <description>pasword to use against

39

metastore database</description> </property> <!- URL⽤于连接远程元数据 -> <property>

- 0

- 1

- 2

- 3

- 4

- 5


<name>hive.metastore.uris</name> <value>thrift:/master1 9083</value>

</property> <!- 元数据使⽤mysql数据库 -> <property>

46

<name>javax.jdo.option.ConectionURL</name >

<value>jdbc:mysql:/master1  306/hivedb? createDatabaseIfNotExist=true&amp;userSL=f alse</value> /property> <property>

name>javax.jdo.option.ConectionDriverName

</name> <value>com.mysql.cj.jdbc.Driver</value> <description>Driver clas name for a JDBC

metastore</description> </property>

开启 mysql 远程连接 CODE

<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>GRANT AL PRIVILEGES ON.* TO 'rot'@'%' IDENTIFIED BY '密码' WITH GRANT OPTION;<br><br></th>
  </tr>
</table>


flush privileges;

再进⼊ /etc/mysql/mysql.conf.d/mysqld.cnf ⽂件将 bind-adres 设置成 0.0.0.0 重启 mysql 服务 BASH

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


1 sudo systemctl restart mysql

验证 Hive 安装 运⾏Hive之前，需要创建 /tmp ⽂件夹在 HDFS 独⽴的 Hive ⽂件夹。在这⾥使 ⽤ /user/hive/warehouse ⽂件夹。 BASH

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br><br></th>
    <th>dop md /tmp hadops -mkdir -p /user/hive/warehouse hdop hmod tmp<br><br></th>
  </tr>
</table>


4 hadop fs -chmod g+w /user/hive/warehouse

初始化 Hive BASH

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


1 schematol -dbType mysql -initSchema

出现 schemeTol completed 表示初始化成功 CODE

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br></th>
    <th>Metastore conection URL: jdbc:mysql:/master1  306/hivedb? createDatabaseIfNotExist=true&userSL=false Metastore Conection Driver : com.mysql.cj.jdbc.Driver Metastore conection User: rot Starting metastore schema initialization to 2.3.0 nitialiati n sci t hive-schema-2.3.0.mysql.sql Initialization script completed<br><br></th>
  </tr>
</table>


schemaTol completed

开启元数据服务

BASH

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


1 nohup hive-service metastore &

进⼊ Hive Shel CODE

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br></th>
    <th>benie@master1:/opt/bigdata/hive-2.3.8$ hive Loging initialized using configuration in file:/opt/bigdata/hive-2.3.8/conf/hivelog4j2.properties Async: true Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a diferent execution engine (i.e. spark, tez) or using Hive 1.X releases. hive> show databases; OK default Tme taken: 8.601 seconds, Fetched: 1 row(s)<br><br></th>
  </tr>
</table>


hive>

# Hiveserver2

开启 hiveserver2,修改 hive-site.xml 配置⽂件,增加如下配置: XML

<table>
  <tr>
    <th>1<br><br>2<br><br>3<br><br>4<br><br>5<br><br>6<br><br>7<br><br>8<br><br><br></th>
    <th><!- 这是hiveserver2-> <property><br><br><name>hive.server2.thrift.port</name> <value>1 0</value><br><br>/property><br><br><property> <name>hive.server2.thrift.bind.host</name> <value>master1</value><br><br></th>
  </tr>
</table>


9 </property>

配置 hadop 中的 core-site.xml ⽂件,并同步带其他主机 XML

<property> <name>hadop.proxyuser.rot.hosts</name> <value>*</value>

</property>

<property> <name>hadop.proxyuser.rot.groups</name> <value>*</value>

</property> <property>

<name>hadop.proxyuser.benie.hosts</name> <value>*</value>

</property> <property>

<name>hadop.proxyuser.benie.groups</name> <value>*</value>

</property>

