Hadop2.6.0的回收站Trash机制跟Hadop1变化不⼤，建议提前打开该功能（默认关闭），防⽌误删时欲哭⽆ 泪。

- 1、修改/etc/hadop/core-site.xml，增加如下配置： <!-开启hdfs⽂件删除⾃动转移到垃圾箱，值为垃圾箱⽂件清除时间，单位是分钟。⼀般开启该配置⽐较好，防⽌

删除重要⽂件。 ->

<property> <name>fs.trash.interval</name> <value>140</value><!- 默认为0，单位为分钟，这⾥设置了⼀天 ->

</property>

- 2、然后测试该功能，随便找个⽬录执⾏删除操作：hdfs dfs -rm -r /tmp/input。删除后会发现有如下类似提⽰： Moved:'viewfs:/hCluster/tmp/input' to trashat: hdfs:/hadop-cluster1/user/hadop2/.Trash/Curent

hdfs:/hadop-cluster1/为core-site.xml中引⼊的mountTable.xml⾥的设置 查看hdfs:/hadop-cluster1/，使⽤命令：

hdfs dfs -ls hdfs:/hadop-cluster1/

会发现其中多了hdfs:/hadop-cluster1/user⽬录，刚删除的⽬录在hdfs:/hadopcluster1/user/hadop2/.Trash/Curent/tmp/input

- 3、从Trash恢复删除⽂件，执⾏： hdfs dfs -mvhdfs:/hadop-cluster1/user/hadop2/.Trash/Curent/tmp/input hdfs:/hadop-

cluster1/tmp/input

如果直接执⾏hdfs dfs -mvhdfs:/hadop-cluster1/user/hadop2/.Trash/Curent/tmp/input /tmp/input会提⽰ mv:`hdfs:/hadop-cluster1/user/hadop2/.Trash/Curent/tmp/input': Does not matchtarget filesystem，具体原 因应该是⽂件系统不匹配。

- 4、直接删除⽂件 开启垃圾箱后，如果希望⽂件直接被删除，可以在使⽤删除命令时添加“–skipTrash” 参数，如下： hadop fs -rm [-r]-skipTrash / x


