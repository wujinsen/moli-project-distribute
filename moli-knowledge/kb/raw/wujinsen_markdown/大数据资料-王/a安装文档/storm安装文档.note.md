- 1.安装环境的依赖库 yum instal gc* yum instal uid* yum instal e2fsprogs* yum instal libuid* yum instal libtol*
- 2.安装zeromq，解压缩zeromq-2.1.7.tar.gz并改名为zeromq cd zeromq

./configure make su - rot make instal

- 3.安装jzmq,解压缩jzmq-master.zip unzip jzmq-master.zip并改名为jzmq cd jzmq

./autogen.sh

./configure make su - rot make instal

- 4.安装python，解压缩Python-2.6.6.tar.bz2,tar -jxvf Python-2.6.6.tar.bz2 并改名为python cd python

./configure make su - rot make instal 检查python环境安装是否成功python -V

- 5.参考zokeper环境在3台机器上安装zokeper
- 6.安装storm，解压缩storm-0.8.1.zip unzip storm-0.8.1.zip 并改名为storm
- 7.创建⼀个storm存储状态信息的⽂件夹，本例在根⽬录中：mkdir /home/hadop/storm-data 修改权限为75
- 8.修改conf/storm.yaml的配置 storm.zokeper.servers:


- - "nimbus"
- - "supervisor01"


- - "supervisor02"


storm.local.dir: "/home/hadop/storm-data"(‘:ʼ后⾯⼀定有⼀个空格，否则报错) nimbus.host: "nimbus"

- 9.将配置好的storm分发到supervisor中
- 10.确保jdk版本，如果有问题需要修改path，本例报错找不到conjore的⼀个包，就是这个问题。 1.启动storm


启动nimbus：storm nimbus >o.txt 2>&1 & 启动supervisor：storm supervisor >o.txt 2>&1 & 启动监控ui：storm ui >o.txt 2>&1 & 查看启动成功失败： 12.提交任务 启动Storm Topology：storm jar almycode.jar org.me.MyTopology arg1 arg2 arg3 停⽌Storm Topology：storm kil {toponame} {toponame}为Topology提交到Storm集群时指定的Topology任务名称。

htp:/nimbus:8080

