htps:/ w.cnblogs.com/Dhouse/p/72857.html

以加⼊elsaticsearch-hadop-2.1.2.jar为例，讲述在Hive中加⼊第三⽅jar的⼏种⽅式。

- 1，在hive shel中加⼊

[java]

- 2，Jar放⼊${HIVE_HOME}/auxlib⽬录 在${HIVE_HOME}中创建⽂件夹auxlib，然后将⾃定义jar⽂件放⼊该⽂件夹中。 此⽅法添加不需要重启Hive。⽽且⽐较便捷。
- 3，HIVE.AUX.JARS.PATH和hive.aux.jars.path hive-env.sh中的HIVE.AUX.JARS.PATH和hive-site.xml的hive.aux.jars.path配置对服务器⽆效，仅对当 前hive shel有效，不同的hive shel相互不影响，每个hive shel都需要配置，可以配置成⽂件夹形式。


view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


[hadoop@hadoopcluster78 bin]$ ./hive

Logging initialized using configuration in file:/home/hadoop/apache/hive-0.13.1/conf/hivelog4j.properties hive> add jar /home/hadoop/elasticsearch-hadoop-hive2.1.2.jar; //elasticsearch-hadoop-hive-2.1.2.jar放在本地⽂件系统 的/home/hadoop⽬录。 Added /home/hadoop/elasticsearch-hadoop-hive-2.1.2.jar to class path Added resource: /home/hadoop/elasticsearch-hadoop-hive-2.1.2.jar

<table>
  <tr>
    <th>连接⽅式</th>
    <th>是否有效</th>
  </tr>
  <tr>
    <td>Hive Shel</td>
    <td>不需要重启Hive服务就有效</td>
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
    <td>不需要重启Hive服务就有效</td>
  </tr>
  <tr>
    <td> </td>
    <td>服务才⽣效</td>
  </tr>
</table>


Hive Server 重启Hive

HIVE.AUX.JARS.PATH和hive.aux.jars.path仅⽀持本地⽂件。可配置成⽂件，也可配置为⽂件夹。

在${HIVE_HOME}/conf/hive-env.sh下配置：

[java]

view plain copy

1.

export HIVE_AUX_JARS_PATH=/home/hadoop/apache/hive-0.13.1/lib/mysql-connector-java-5.1.7bin.jar #本地⽂件路径，不⽀持HDFS路径

类似的，可以在${HIVE_HOME}/conf/hive-site.xml下配置：

[java]

view plain copy

- 1.
- 2.
- 3.
- 4.


<property> <name>hive.aux.jars.path</name> <value>/home/hadoop/elasticsearch-hadoop-hive-2.1.2.jar</value>

</property>

<table>
  <tr>
    <th>连接⽅式</th>
    <th>是否有效</th>
  </tr>
  <tr>
    <td>Hive Shel</td>
    <td>重启Hive服务才⽣效且不同的Hive Shel互不影响</td>
  </tr>
  <tr>
    <td> </td>
    <td>⽆效</td>
  </tr>
</table>


Hive Server

注：hive-env.sh中配置的HIVE_AUX_JARS_PATH和hive-site.xml配置hive.aux.jars.path参数会 有冲突，在使⽤的时候要特别注意。

- 4，直接将Jar加⼊${HIVE_HOME}/lib⽬录


<table>
  <tr>
    <th>连接⽅式</th>
    <th>是否有效</th>
  </tr>
  <tr>
    <td>Hive Shel</td>
    <td>重启Hive服务才⽣效</td>
  </tr>
  <tr>
    <td> </td>
    <td>服务才⽣效</td>
  </tr>
</table>


Hive Server 重启Hive

总结：只有第2种和第4种⽅式才能在Hive Server中起效。其他的在Hive Server中都⽆效，⽹上看了⼀ 些⽂章，都说hive-site.xml配置hive.aux.jars.path是可以起作⽤的，但是试过很多次都没⽤，百思不得 其姐，期待⼤神答疑。

