htps:/ w.cnblogs.com/stevensfolower/p/9806428.html

官⽹⽂档地址:

htps:/docs.cloudera.com/HDPDocuments/HDP3/HDP-3.0.1/hbase-data-aces/content/hdag_con figuring_hbase_and_hive.html

集群环境 ambari 2.7.3 hdp/hortonwork 2.6.0.3

![image 1](<Hortonwork Ambari配置Hive集成Hbase的java开发maven配置.note_images/imageFile1.png>)

maven

<table>
  <tr>
    <th>![image 2](<Hortonwork Ambari配置Hive集成Hbase的java开发maven配置.note_images/imageFile2.png>)</th>
  </tr>
</table>


- 1 <dependency>

- 2 <groupId>org.apache.hive</groupId>

- 3 <artifactId>hive-jdbc</artifactId>

- 4 <version>1.2.1000.2.6.0.3-8</version>

- 5 <classifier>standalone</classifier>

- 6 </dependency>

- 7 <dependency>

- 8 <groupId>org.apache.hbase</groupId>

- 9 <artifactId>hbase-client</artifactId>

- 10 <version>1.1.2.2.6.0.3-8</version>

- 11 </dependency>


<table>
  <tr>
    <th>![image 3](<Hortonwork Ambari配置Hive集成Hbase的java开发maven配置.note_images/imageFile3.png>)</th>
  </tr>
</table>


# 代码

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9

- 10

- 11

- 12

- 13

- 14

- 15

- 16

- 17

- 18

- 19

- 20

- 21

- 22

- 23

- 24

- 25

- 26

- 27

- 28

- 29

- 30

- 31

- 32

- 33

- 34

- 35

- 36

- 37

- 38

- 39

- 40

- 41

- 42

- 43

- 44

- 45

- 46

- 47

- 48

- 49

- 50

- 51

- 52

- 53

- 54

- 55

- 56


package com.yingzi.com.dmh; import java.io.IOException; import java.net.URL; import java.sql.SQLException; import java.sql.Connection; import java.sql.ResultSet; import java.sql.Statement; import java.sql.DriverManager; import java.util.Enumeration; //import org.apache.hadoop.hbase.client.Connection;

public class HiveJdbcClient {

private static String driverName = "org.apache.hive.jdbc.HiveDriver";

public static void main(String[] args) throws SQLException {

try {

Class.forName(driverNam ); } catch (ClassNotFoundException e) {

// TODO Auto-generated catch block e.printStackTrace(); System.exit(1);

}

ClassLoader classLoader = App.class.getClassLoader();

Enumeration<URL> paths = null; try {

paths = classLoader.getResources("METAINF");

} catch (IOException e) { e.printStackTrace(); } finally { } int count = 0;

while (paths.hasMoreElements()){

String path =

paths.nextElement().toString();

if (path.indexOf("jdk") == -1){ count++; System.out.println(path);

}

} System.out.println(count);

// Connection con =

DriverManager.getConnection("jdbc:hive2://hdfs0 3.yingzi.com:2181,hdfs04.yingzi.com:2181,hdfs05. yingzi.com:2181/;serviceDiscoveryMode=zooKeeper;

zooKeeperNamespace=hiveserver2"); Statement stmt = con.cr ateStatement(); ResultSet res = stmt.executeQuery("show

databases"); if (res.next()) {

System.out.println(res.getString(1));

} //create table

- 57

- 58

- 59


String sql = "CREATE TABLEIF NOT EXISTS hbase_hive_table(key string, valuestring)\n" +

"STORED BY

'org.apache.hadoop.hive.hbase.HBaseStorageHandle r'\n" +

"WITH SERDEPROPERTIES (\"hbase.columns.mapping\" = \":key,cf:json\")\n" +

"TBLPROPERTIES (\"hbase.table.name\"

= \"hbase_hive_table\")";

Sysstmtem.out.println(sql)t.execute(sql); ; }

}

运⾏报错： org.apache.hive.service.cli.HiveSQLException: java.lang.NoClasDefFoundEror: org/apache/hadop/hbase/client/Conection 解决办法： 参考：htps:/docs.hortonworks.com/HDPDocuments/HDP3/HDP-3.0.1/hbase-dataaces/content/hdag_configuring_hbase_and_hive.html ambari->hive->configs->advanced->Custom hive-site->adproperity

![image 4](<Hortonwork Ambari配置Hive集成Hbase的java开发maven配置.note_images/imageFile4.png>)

ambari->hive->configs->advanced->Custom hive-site->adproperity

