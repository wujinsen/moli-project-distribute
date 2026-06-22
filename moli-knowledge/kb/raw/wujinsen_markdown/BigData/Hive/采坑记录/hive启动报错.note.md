# hive配置遇到的问题 Relative path in absolute URI

解决⽅案如下：

- 1.查看hive-site.xml配置，会看到配置值含有"system:java.io.tmpdir"的配置项 ，应该有四个
- 2.新建⽂件夹/home/grid/hive-0.14.0-bin/iotmp
- 3.将含有"system:java.io.tmpdir"的配置项的值修改为如上地址 启动hive，成功！


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.


<property>

<name>hive.exec.local.scratchdir</name> <value>hive主⽬录/iotmp</value> <description>Local scratch spacefor Hive jobs</description>

</property>

<property>

<name>hive.querylog.location</name> <value>hive主⽬录/iotmp</value> <description>Location of Hive run time structured log file</description>

</property>

<property>

<name>hive.downloaded.resources.dir</name> <value>hive主⽬录/iotmp</value> <description>Temporary local directoryfor aded resources in the remote file system.

</description> </property>

