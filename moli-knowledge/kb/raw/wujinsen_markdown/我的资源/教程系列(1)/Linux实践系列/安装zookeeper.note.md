- 1、下载zokeper安装包，建议下载3.4.5及以上的版本
- 2、下载完毕之后，解压⽂件 tar -zxvf zokeper-3.4.7.tar.gz -C /export/servers/

cd /export/servers ln -s zokeper-3.4.7 zokeper

- 3、修改配置⽂件 cd /export/servers/zokeper/conf mv zo_sample.cfg zo.cfg vi zo.cfg

参⻅：

- 4、创建zk的数据⽬录和⽇志⽬录 mkdir -p /export/servers/data/zookeeper mkdir -p /export/servers/logs/zookeeper
- 5、在数据目录下创建zk节点的编号 在上文中 /export/servers/data/zookeeper 的目录下，创建myid文件。 myid文件的内容，根据所属主机编号来编写。 解释： 创建数据⽬录,也就是在你zoo.cfg配置⽂件⾥dataDir指定的那个⽬录下创建myid⽂件,并且指定id,改id为你zoo.cfg⽂

件中server.1=localhost:2887:3887中的1.只要在myid头部写⼊1即可.

- 6、分发修改后的安装⽂件

- scp zokeper-3.4.6 hadop02:/export/servers/
- scp zokeper-3.4.6 hadop03:/export/servers/


- 7、在分发后的机器上，执⾏步骤5的操作。 创建数据⽬录,也就是在你zoo.cfg配置⽂件⾥dataDir指定的那个⽬录下创建myid⽂件,并且指定id,改id为你zoo.cfg⽂

件中server.1=localhost:2887:3887中的1.只要在myid头部写⼊1即可.

- 8、在所有 所有 所有机器上配置环境变量 #set ZK env


htp:/ w.apache.org/dyn/closer.cgi/zokeper/

htp:/note.youdao.com/share/?id=ce2ea7f9e64c41873e38de3125bca&type=note

export ZK_HOME=/export/servers/zk export PATH=${ZK_HOME}/bin:$PATH

- 9、在所有 所有 所有机器上让配置⽂件⽣效 source /etc/profile
- 10、启动zk集群 依次在不同的节点上，输⼊zkServers.sh start


htp:/maoxiangyi.cn/index.php/archives/121

出现错误后，可参考：

1、查看zk集群的状态

依次在不同的节点上，输⼊zkServers.sh status 只有⼀个主节点，leader 其他都是follow

