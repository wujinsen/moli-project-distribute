- 1. 下载hive 2.3.8

- 2.环境变量

export HIVE_HOME=/opt/bigdata/hive-2.3.8 PATH=$PATH:$HIVE_HOME/bin

- 3.配置⽂件


mv conf/hive-env.sh.template conf/hive-env.sh # 重命名环境⽂件 mv conf/hive-log4j2.properties.template conf/hive-log4j2.properties # 重命名⽇志⽂件 cp conf/hive-default.xml.template conf/hive-site.xml # 拷⻉⽣成 xml ⽂件

修改hive-env.sh:

修改hive-site.xml⽂件，修改内容如下：

<table>
  <tr>
    <th><!- hive元数据地址，默认是/user/hive/warehouse-> <property><br><br><name>hive.metastore.warehouse.dir</name> <value>/user/hive/warehouse</value><br><br></property> <!- hive查询时输出列名 -> <property><br><br><name>hive.cli.print.header</name> <value>true</value><br><br></property> <!- 显示当前数据库名 -> <property><br><br><name>hive.cli.print.curent.db</name> <value>true</value><br><br></property> <!- 开启本地模式，默认是false-> <property><br><br><name>hive.exec.mode.local.auto</name> <value>true</value><br><br></property> <property><br><br><name>javax.jdo.option.ConectionUserName</name> <value>r t</value> <description>Username to use against metastore database</description><br><br>/property><br><br><property> <name>javax.jdo.option.ConectionPasword</name> <value>123456</value> <description>pasword to use against metastore database</description><br><br></property> <!- URL⽤于连接远程元数据 -> <property><br><br><name>hive.metastore.uris</name> <value>thrift:/127.0.0.1 9083</value><br><br></property> <!- 元数据使⽤mysql数据库 -> <property><br><br><name>javax.jdo.option.ConectionURL</name> <value>jdbc:mysql:/master1  306/hivedb?<br><br>createDatabaseIfNotExist=true&amp;userSL=false</value> /property><br><br><property> <name>javax.jdo.option.ConectionDriverName</name> <value>com.mysql.cj.jdbc.Driver</value> <description>Driver clas name for a JDBC metastore</description></th>
  </tr>
</table>


</property>

拷⻉⼀个mysql的连接jar包到lib⽬录下,我⽤的是 mysql-conector-java-5.1.30.jar

7. hive启动

然后到hdfs上建⽴⼀些基础⽬录hive-site.xml中配置的仓库地址等,⼿⼯创建(包括配置的hive的数据⽬ 录,仓库地址,⽇志等,并赋权):

bin/hadop fs -mkdir -p /user/hive/warehouse bin/hadop fs -mkdir -p /user/hive/tmp bin/hadop fs -mkdir -p /user/hive/log bin/hadop fs -chmod -R 777 /user/hive/warehouse bin/hadop fs -chmod -R 777 /user/hive/tmp bin/hadop fs -chmod -R 777 /user/hive/log

这样就可以开始初始化了,先启动hadop,然后在hive/bin⽬录下执⾏命令:

./schematol -initSchema -dbType mysql

./hive 启动hive元数据服务

./hive-service metastore &

hive2访问路径:

htp:/localhost:1 02/

