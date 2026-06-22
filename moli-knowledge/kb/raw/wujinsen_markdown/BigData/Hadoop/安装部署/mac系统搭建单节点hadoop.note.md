htps:/ w.jianshu.com/p/7f56e3a9c80a

- 1.下载hadop
- 2.解压下载的hadop⽂件 tar -zxvf /HADOOP-VERSION-FULL-PATH.tar.gz
- 3.配置hadop环境 # vim /etc/profile ##添加HADOOP_HOME路径 # export HADOOP_HOME=/HADOOP-VERSION-FULL-PATH # export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin # wq # source /etc/profile ## 创建hadoop数据⽬录 # sudo mkdir /var/hadoop # sudo chmod 777 /var/hadoop
- 4.hadop需要java环境的⽀持，配置java环境略
- 5.修改hadop配置⽂件，主要包括以下⼏个⽂件


htp:/hadop.apache.org/releases.html

core.site.xml

# vim /HADOOP-VERSION-FULL-PATH/etc/hadoop/core.site.xml 在configuration节点中添加以下内容

<property> <name>fs.defaultFS</name> <value>hdfs://hadoop01:9000</value>

</property> hadoop01需要在/etc/hosts配置本地映射: 127.0.0.1 hadoop01

hdfs.site.xml⽂件

# vim /HADOOP-VERSION-FULL-PATH/etc/hadoop/hdfs.site.xml 在configuration节点中添加以下内容

<!-- 单节点hadoop不需要复制 --> <property>

<name>dfs.replication</name> <value>1</value>

</property>h <property>

<name>dfs.namenode.name.dir</name> <value>file:/var/hadoop/data/hdfs/nn</value>

</property>

<property> <name>fs.checkpoint.dir</name> <value>file:/var/hadoop/data/hdfs/snn</value>

</property> <property>

<name>fs.checkpoint.edits.dir</name> <value>file:/var/hadoop/data/doop/hdfs/snn</value>

</property> <property>

<name>dfs.datanode.data.dir</name> <value>file:/var/hadoop/data/hdfs/dn</value>

</property>

mapred-site.xml

# cp /HADOOP-VERSION-FULL-PATH/etc/hadoop/mapred-site.xml.template /HADOOP-VERSION-FULL-PATH/etc/hadoop/mapred-site.xml 在configuration节点中添加以下内容

<property> <name>mapreduce.frameword.name</name> <value>yarn</value>

</property>

yarn-site.xml

# vim /HADOOP-VERSION-FULL-PATH/etc/hadoop/yarn-site.xml 在configuration节点中添加以下内容

<property> <name>yarn.nodemanager.aux-services</name> <value>mapreduce_shuffle</value>

</property> <property>

<name>yarn.nodemanager.aux-services.shuffle.class</name> <value>org.apache.hadoop.mapred.ShuffleHandler</value>

</property>

hadop-env.sh

# vim /HADOOP-VERSION-FULL-PATH/etc/hadoop/hadoop-env.sh 覆盖正确的JAVA_HOME export JAVA_HOME=JAVA_HOME_PATH

- 6.格式化hdfs⽂件系统 # /HADOOP-VERSION-FULL-PATH/bin/hdfs namenode -format


看到这条信息就表示成功了: Storage: Storage directory /var/hadop/data/hdfs/ n has ben sucesfuly formated.

- 7.启动hdfs服务 # /HADOOP-VERSION-FULL-PATH/sbin/start-dfs.sh
- 8.启动yran服务 # /HADOOP-VERSION-FULL-PATH/sbin/start-yarn.sh
- 9.通过web接⼝验证正在运⾏的服务 htp:/127.0.0.1 5070 htp:/127.0.0.1 808
- 10.配置 sh免秘钥登录 # cd ~/.ssh/ # 若没有该⽬录，请先执⾏⼀次ssh localhost # ssh-keygen -t rsa # 会有提示，都按回⻋就可以 # cat id_rsa.pub >> authorized_keys # 加⼊授权 # chmod 600 ./authorized_keys # 修改⽂件权限


htps:/ w.jianshu.com/p/7f56e3a9c80a

