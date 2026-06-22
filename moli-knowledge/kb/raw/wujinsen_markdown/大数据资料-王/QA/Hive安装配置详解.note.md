本⽂主要是在Hadop单机模式中演示Hive默认（嵌⼊式derby 模式）安装配置过程，⽬录结构如下：

基础环境

Hive安装配置

启动及演示

[⼀]、基础环境

Mac OSX 10.9.1

Java 1.6+

Hadop 2.2.0 （单机模式安装配置详⻅： ）

htp:/ w.micmiu.com/opensource/hadop/hadop2x

-single-node-setup/

Hive 0.12.0 （截⽌2014-02-09最新的发布版本）

[⼆]、Hive安装配置

- 1、下载发布包 到官⽅下载最近发布包以 0.12.0为例：

本⽂中 HIVE_HOME = “/usr/local/share/”

- 2、设置环境变量 执⾏ vi ~/.profile ,添加如下内容：


<table>
  <tr>
    <th>1</th>
    <th>$ tar<br><br>-zxf hive-0.12.0-bin. tar<br><br>.gz -C /usr/ local /share<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>$ cd<br><br>/usr/ local /share<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>$ ln<br><br>-s hive-0.12.0-bin hive</th>
  </tr>
</table>


<table>
  <tr>
    <th>1</th>
    <th>#Hive @micmiu.com</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>export HIVE_HOME= "/usr/local/share/hive"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>export PATH=$HIVE_HOME/bin:$PATH<br><br></th>
  </tr>
</table>


- 3、配置⽂件 在⽬录 <HIVE_HOME>/conf ⽬录下有4个模板⽂件：


<table>
  <tr>
    <th>1</th>
    <th>hive-default.xml.template</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>hiveenv<br><br>.sh.template</th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>hiveexec<br><br>-log4j.properties.template</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>hive-log4j.properties.template</th>
  </tr>
</table>


copy ⽣成四个配置⽂件然后既可⾃定义相关属性：

<table>
  <tr>
    <th>1</th>
    <th>$ cd<br><br>/usr/ local /share/hive/conf<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>$ copy hive-default.xml.template hive-site.xml</th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>$ copy hiveenv<br><br>.sh.template hive-<br><br>env .sh<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>$ copy hiveexec<br><br>-log4j.properties.template hiveexec<br><br>-log4j.properties<br></th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>$ copy hive-log4j.properties.template hive-log4j.properties</th>
  </tr>
</table>


ps：注意⽂件名称： hive-site.xml ,本⽂以嵌⼊式derby 模式做演示，故以默认配置即可⽆效修改 相关参数。 不过官⽅0.12.0的发布版本中的 hive-default.xml.template 中有 bug，在 2 0⾏：

<value>auth</auth> 修改为： <value>auth</value> 有关 hive.metastore.schema.verification 版本检查的问题，有两个解决办法 ⽅法⼀：修改配置⽂件 第⼀次运⾏前先将 hive.metastore.schema.verification 设为false

<table>
  <tr>
    <th>1</th>
    <th>......</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th><!-- 设为false 不做验证--></th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>< name >hive.metastore.schema.verification</ name ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>< value >false</ value ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>......</th>
  </tr>
</table>


⽅法⼆：不改配置，先初始化好数据 执⾏初始化命令： schematool -dbType derby -initSchema

<table>
  <tr>
    <th>1</th>
    <th>micmiu-mbp:~ micmiu$ schematool -dbType derby -initSchema</th>
  </tr>
</table>


<table>
  <tr>
    <th>2<br><br></th>
    <th>Metastore connection URL: jdbc:derby:;databaseName=metastore_db;create= true<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3<br><br></th>
    <th>Metastore Connection Driver : org.apache.derby.jdbc.EmbeddedDriver</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>Metastore connection User: APP</th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>Starting metastore schema initialization to 0.12.0</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>Initialization script hive-schema-0.12.0.derby.sql</th>
  </tr>
</table>


<table>
  <tr>
    <th>7</th>
    <th>Initialization script completed</th>
  </tr>
</table>


<table>
  <tr>
    <th>8</th>
    <th>schemaTool completeted</th>
  </tr>
</table>


查看初始化后的信息： schematool -dbType derby -info

<table>
  <tr>
    <th>1</th>
    <th>micmiu-mbp:~ micmiu$ schematool -dbType derby -info</th>
  </tr>
</table>


<table>
  <tr>
    <th>2<br><br></th>
    <th>Metastore connection URL: jdbc:derby:;databaseName=metastore_db;create= true<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3<br><br></th>
    <th>Metastore Connection Driver : org.apache.derby.jdbc.EmbeddedDriver</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>Metastore connection User: APP</th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>Hive distribution version: 0.12.0</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>Metastore schema version: 0.12.0</th>
  </tr>
</table>


<table>
  <tr>
    <th>7</th>
    <th>schemaTool completeted</th>
  </tr>
</table>


htps:/cwiki.apache.org/confluence/display/Hive/Hive+Schema+Tol

详⻅： 以上⽅法都可以，否则第⼀次运⾏时会类似如下的报错信息：

<table>
  <tr>
    <th>1</th>
    <th>ERROR exec<br><br>.DDLTask (DDLTask.java:execute(435)) -<br><br>org.apache.hadoop.hive.ql.metadata.HiveException: java.lang.RuntimeException: Unable to instantiate org.apache.hadoop.hive.metastore.HiveMetaStoreClient</th>
  </tr>
</table>


<table>
  <tr>
    <th>2<br><br></th>
    <th>at org.apache.hadoop.hive.ql.metadata.Hive.getDatabase(Hive.java:1143)</th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>......</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>Caused by: java.lang.RuntimeException: Unable to instantiate org.apache.hadoop.hive.metastore.HiveMetaStoreClient</th>
  </tr>
</table>


<table>
  <tr>
    <th>5<br><br></th>
    <th>at org.apache.hadoop.hive.metastore.MetaStoreUtils.newInstance(MetaStor eUtils.java:1212)</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>......</th>
  </tr>
</table>


<table>
  <tr>
    <th>7</th>
    <th>Caused by: java.lang.reflect.InvocationTargetException</th>
  </tr>
</table>


<table>
  <tr>
    <th>8</th>
    <th>......</th>
  </tr>
</table>


<table>
  <tr>
    <th>9</th>
    <th>Caused by: MetaException(message:Version information not found in metastore. )<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>at org.apache.hadoop.hive.metastore.ObjectStore.checkSchema(ObjectStore<br><br>.java:5638)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>at org.apache.hadoop.hive.metastore.ObjectStore.verifySchema(ObjectStor e.java:5622)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>......</th>
  </tr>
</table>


- 4、配置dfs中得⽬录和权限


<table>
  <tr>
    <th>1</th>
    <th>$ hdfs dfs mkdir /tmp<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>$ hdfs dfs mkdir /user/hive/warehouse<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>$ hdfs dfs chmod g+w /tmp<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>$ hdfs dfs chmod g+w /user/hive/warehouse<br><br></th>
  </tr>
</table>


[三]、运⾏和测试 确保HADOP_HOME 在环境变量中配置好，然后以CLI（comand line interface）⽅式下运⾏，直 接执⾏命令 hive 即可，然后执⾏⼀些测试命令如下：

<table>
  <tr>
    <th>1</th>
    <th>hive> show databases;</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>OK</th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>default</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>Time taken: 4.966 seconds, Fetched: 1 row(s)</th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>hive> show tables;</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>OK</th>
  </tr>
</table>


<table>
  <tr>
    <th>7</th>
    <th>Time taken: 0.186 seconds</th>
  </tr>
</table>


<table>
  <tr>
    <th>8</th>
    <th>hive> CREATE TABLE micmiu_blog ( id INT, siteurl STRING);<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>9</th>
    <th>OK</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>Time taken: 0.359 seconds</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>hive> SHOW TABLES;</th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th>OK</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th>micmiu_blog</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 4<br><br></th>
    <th>Time taken: 0.023 seconds, Fetched: 1 row(s)</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 5<br><br></th>
    <th>hive></th>
  </tr>
</table>


到此嵌⼊式derby 模式下的Hive安装配置已经成功。 参考：

htps:/cwiki.apache.org/confluence/display/Hive/Home

htps:/cwiki.apache.org/confluence/display/Hive/GetingStarted

htps:/cwiki.apache.org/confluence/display/Hive/Hive+Schema+Tol

htps:/cwiki.apache.org/confluence/display/Hive/AdminManual+MetastoreAdmin Michael Sun

⸻⸺– EOF @ ⸻⸺–

