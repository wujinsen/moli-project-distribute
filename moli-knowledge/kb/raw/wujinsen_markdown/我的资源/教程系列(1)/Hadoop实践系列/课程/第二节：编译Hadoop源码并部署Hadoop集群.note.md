- 1、为什么要⾃⼰编译
- 2、安装编译环境 下载源代码：svn checkout svn checkout

进⼊项⽬根⽬录，进⾏编译： mvn package -Pdist,native -DskipTests -Dtar

耐⼼等待1⼩时左右 在hadop项⽬的hadop-dist/target就有了安装包

- 3、创建安装⽬录并解压⽂件


企业的线上环境（⽣产环境）⼀般都是64位

hadop官⽅⽹站上并没有⽀持64位的安装包

htps:/github.com/apache/hadop.git/tags/release-2.6. 1

![image 1](<第二节：编译Hadoop源码并部署Hadoop集群.note_images/imageFile1.png>)

![image 2](<第二节：编译Hadoop源码并部署Hadoop集群.note_images/imageFile2.png>)

mkdir -p /export/servers mkdir -p /export/software mv hadop-2.6.2.tar.gz /export/software/ cd /export/software/ tar -zxvf hadop-2.6.2.tar.gz -C /export/servers/ cd /export/servers/ ln -s hadop-2.6.2 hadop

- 4、配置环境变量 vi/etc/profile

#set hadop env export HADOP_HOME=/export/servers/hadop export PATH=${HADOP_HOME}/bin:${HADOP_HOME}/sbin:$PATH

- 5、修改Hadop配置⽂件 第⼀个：hadop-env.sh 第⼆个：core-site.xml

第三个：hdfs-site.xml 第四个：mapred-site.xml

第五个：yarn-site.xml

- 6、格式化nameonde hadop namenode -format
- 7、启动HDFS服务


⼿动⼀台⼀台地启动

在相应服务器上启动hdfs的相关进程： 启动namenode进程—— sbin/hadop-daemon.sh startnamenode 启动datanode进程——sbin/hadop-daemon.sh start datanode 然后，验证hdfs的服务是否能正常提供： bin/hdfs dfsadmin -report 查看hdfs集群的统计信息

Shel脚本批量启动⽅式：

在任意⼀台服务器上执⾏命令： 启动hdfs服务：sbin/start-dfs.sh 启动yarn服务：sbin/start-yarn.sh 或者：直接启动hdfs+yarn服务：sbin/start-al.sh

yarn-daemon.sh start nodemanager

htp:/ w.ibm.com/developerworks/cn/data/library/bd-yarn-intro/

htp:/hadop01 5070/dfshealth.html#tab-overview htp:/hadop01 808/cluster

HDFS管理界⾯ 任务管理界⾯

hadop dfsadmin -safemode leave

