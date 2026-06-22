htps:/ w.cnblogs.com/yangykaifa/p/738967.html

以增加elsaticsearch-hadoop-2.1.2.jar为例，讲述在Hive中增加第三⽅jar的⼏种⽅式。

- 1，在hive shell中增加 [hadoop@hadoopcluster78 bin]$ ./hive

Logging initialized using configuration in file:/home/hadoop/apache/hive0.13.1/conf/hive-log4j.properties hive> add jar /home/hadoop/elasticsearch-hadoop-hive-2.1.2.jar; //elasticsearch-hadoop-hive-2.1.2.jar放在本地⽂件系统的/home/hadoop⽂件夹。 Added /home/hadoop/elasticsearch-hadoop-hive-2.1.2.jar to class path Added resource: /home/hadoop/elasticsearch-hadoop-hive-2.1.2.jar

- 2，Jar放⼊${HIVE_HOME}/auxlib⽂件夹 在${HIVE_HOME}中创建⽬录auxlib，然后将⾃⼰定义jar⽂件放⼊该⽬录中。 此⽅法加⼊不须要重新启动Hive。并且⽐較便捷。
- 3。HIVE.AUX.JARS.PATH和hive.aux.jars.path hive-env.sh中的HIVE.AUX.JARS.PATH和hive-site.xml的hive.aux.jars.path配置对server⽆效，仅对当前 hive shell有效。不同的hive shell相互不影响。每⼀个hive shell都须要配置，能够配置成⽬录形式。


<table>
  <tr>
    <th>连接⽅式</th>
    <th>是否有效</th>
  </tr>
  <tr>
    <td>Hive Shel</td>
    <td>不须要重新启动Hive服务就有效</td>
  </tr>
  <tr>
    <td> </td>
    <td>⽆效</td>
  </tr>
</table>


Hive Server

<table>
  <tr>
    <th>连接⽅式</th>
    <th>是否有效</th>
  </tr>
  <tr>
    <td>Hive Shel</td>
    <td>不须要重新启动Hive服务就有效</td>
  </tr>
  <tr>
    <td> </td>
    <td>服务才⽣效</td>
  </tr>
</table>


Hive Server 重新启动Hive

HIVE.AUX.JARS.PATH和hive.aux.jars.path仅⽀持本地⽂件。可配置成⽂件，也可配置为⽬录。

在${HIVE_HOME}/conf/hive-env.sh下配置： export HIVE_AUX_JARS_PATH=/home/hadoop/apache/hive-0.13.1/lib/mysqlconnector-java-5.1.7-bin.jar #本地⽂件路径，不⽀持HDFS路径

类似的。能够在${HIVE_HOME}/conf/hive-site.xml下配置： <property>

<name>hive.aux.jars.path</name> <value>/home/hadoop/elasticsearch-hadoop-hive-2.1.2.jar</value>

</property>

<table>
  <tr>
    <th>连接⽅式</th>
    <th>是否有效</th>
  </tr>
  <tr>
    <td>Hive Shel</td>
    <td>重新启动Hive服务才⽣效且不同的Hive Shel互不 影响</td>
  </tr>
  <tr>
    <td> </td>
    <td>⽆效</td>
  </tr>
</table>


Hive Server

注：hive-env.sh中配置的HIVE_AUX_JARS_PATH和hive-site.xml配置hive.aux.jars.path參数 会有冲突。在使⽤的时候要特别注意。

- 4。直接将Jar增加${HIVE_HOME}/lib⽂件夹


<table>
  <tr>
    <th>连接⽅式</th>
    <th>是否有效</th>
  </tr>
  <tr>
    <td>Hive Shel</td>
    <td>重新启动Hive服务才⽣效</td>
  </tr>
  <tr>
    <td> </td>
    <td>服务才⽣效</td>
  </tr>
</table>


Hive Server 重新启动Hive

总结：仅仅有第2种和第4种⽅式才⼲在Hive Server中起效。其它的在Hive Server中都⽆效，⽹上看了 ⼀些⽂章。都说hive-site.xml配置hive.aux.jars.path是能够起作⽤的，可是试过⾮常多次都没⽤。百思 不得其姐。期待⼤神答疑。

