hbase安装⽂档

- 1. 上传

- 2. 解压

- 3. 重命名

- 4.

修改环境变量

在master机器上执⾏下⾯命令：

<table>
  <tr>
    <th>epo HBASE_HOME=/home/hadop/hbase</th>
  </tr>
</table>


export PATH=$PATH:$HBASE_HOME/bin

- 5. 修改配置⽂件


⾸先确保⽤户是hadop，⽤⼯具将hbase安装包hbase-0. 9.2-bin.tar.gz上传到/home/hadop下，确 保hbase-0. 9.2-bin.tar.gz的⽤户是hadop，如果不是，执⾏chown命令，见上⽂

su – hadop tar –zxvf hbase-0. 9.2-bin.tar.gz

mv hbase-0. 9.2 hbase

1.

su – rot vi/etc/profile 添加内容：

执⾏命令： source /etc/profile su – hadop 2、在其他机器上执⾏上述操作。

su – hadop 将配置⽂件上传到/home/hadop/hbase/conf⽂件夹下。 每个⽂件的解释如下：

<table>
  <tr>
    <th>hbase-env.sh<br><br>r JAV_HOME=/usr/jdk /jdk安装⽬录 epo HBASE_CLASPATH=/home/hadop/hadop/conf /hadop配置⽂件的位置 export HBASE_MANAGES_ZK=true #如果使⽤独⽴安装的zokeper这个地⽅就是false<br><br>hbase-site.xml <configuration> property> <name>hbase.master</name> #hbasemaster的主机和端⼜ <value>master1 6 0</value><br><br>/property> property><br><br><name>hbase.master.maxclockskew</name> #时间同步允许的时间差 <value>18 0</value><br><br>/property> property><br><br><name>hbase.rotdir</name><br><br><value>hdfs:/ hadop-cluster1/hbase</value>#hbase共享⽬录，持久化hbase数据 /property> property><br><br><name>hbase.cluster.distributed</name> #是否分布式运⾏，false即为单机 <value>true</value><br><br>/property> property><br><br><name>hbase.zokeper.quorum</name>#zokeper地址 <value>slave1, slave2,slave3</value><br><br>/property> property><br><br><name>hbase.zokeper.property.dataDir</name>#zokeper配置信息快照的位置 <value>/home/hadop/hbase/tmp/zokeper</value> </propety><br><br></configuration> Regionservers /是从机器的域名<br><br>1<br>2<br><br><br>slave3</th>
  </tr>
</table>


# 6. 把hadop的hdfs-site.xml和core-site.xml放到hbase/conf下

cp/home/hadop/hadop/etc/hadop/hdfs-site.xml /home/hadop/hbase/conf cp/home/hadop/hadop/etc/hadop/core-site.xml /home/hadop/hbase/conf

- 7. 发送到其他机器

- 8. 启动

- 9. 查看


su - hadop

- scp –r /home/hadop/hbasehadop@slave1:/home/hadop
- scp –r /home/hadop/hbasehadop@slave2:/home/hadop
- scp –r /home/hadop/hbasehadop@slave3:/home/hadop


su – hadop start-hbase.sh

进程：jps 进⼊hbase的shel：hbase shel 退出hbase的shel：quit 页⾯：

htp:/master:6010/

