本⽂以Twiter Storm官⽅Wiki为基础，详细描述如何快速搭建⼀个Storm集群，其中，项⽬实践中遇到 的问题及经验总结，在相应章节以“注意事项”的形式给出。 ⼀、Storm集群组件 Storm集群中包含两类节点：主控节点（Master Node）和⼯作节点（Work Node）。其分别对应的⻆ ⾊如下：

- 1. 主控节点（Master Node）上运⾏⼀个被称为Nimbus的后台程序，它负责在Storm集群内分发代 码，分配任务给⼯作机器，并且负责监控集群运⾏状态。Nimbus的作⽤类似于Hadop中JobTracker 的⻆⾊。
- 2. 每个⼯作节点（Work Node）上运⾏⼀个被称为Supervisor的后台程序。Supervisor负责监听从 Nimbus分配给它执⾏的任务，据此启动或停⽌执⾏任务的⼯作进程。每⼀个⼯作进程执⾏⼀个 Topology的⼦集；⼀个运⾏中的Topology由分布在不同⼯作节点上的多个⼯作进程组成。


Storm集群组件 Nimbus和Supervisor节点之间所有的协调⼯作是通过Zokeper集群来实现的。此外，Nimbus和 Supervisor进程都是快速失败（fail-fast)和⽆状态（stateles）的；Storm集群所有的状态要么在 Zokeper集群中，要么存储在本地磁盘上。这意味着你可以⽤kil -9来杀死Nimbus和Supervisor进 程，它们在重启后可以继续⼯作。这个设计使得Storm集群拥有不可思议的稳定性。 ⼆、安装Storm集群 这⼀章节将详细描述如何搭建⼀个Storm集群。下⾯是接下来需要依次完成的安装步骤：

- 1. 搭建Zokeper集群；
- 2. 安装Storm依赖库；
- 3. 下载并解压Storm发布版本；
- 4. 修改storm.yaml配置⽂件；
- 5. 启动Storm各个后台进程。 搭建Zokeper集群 Storm使⽤Zokeper协调集群，由于Zokeper并不⽤于消息传递，所以Storm给Zokeper带来的 压⼒相当低。⼤多数情况下，单个节点的Zokeper集群⾜够胜任，不过为了确保故障恢复或者部署⼤ 规模Storm集群，可能需要更⼤规模节点的Zokeper集群（对于Zokeper集群的话，官⽅推荐的最 ⼩节点数为3个）。在Zokeper集群的每台机器上完成以下安装部署步骤：


- 1. 下载安装Java JDK，官⽅下载链接为 ，JDK版本为 JDK 6或以上。
- 2. 根据Zokeper集群的负载情况，合理设置Java堆⼤⼩，尽可能避免发⽣swap，导致Zokeper性 能下降。保守起⻅，4GB内存的机器可以为Zokeper分配3GB最⼤堆空间。
- 3. 下载后解压安装Zokeper包，官⽅下载链接为 。
- 4. 根据Zokeper集群节点情况，在conf⽬录下创建Zokeper配置⽂件zo.cfg：


htp:/java.sun.com/javase/downloads/index.jsp

htp:/hadop.apache.org/zokeper/releases.html

tickTime=2 0 dataDir=/var/zokeper/ clientPort=2181 initLimit=5 syncLimit=2 server.1=zo1 28 8 3 8 server.2=zo2 2 8 3 8 server.3=zo3 2 8 3 8 其中，dataDir指定Zokeper的数据⽂件⽬录；其中server.id=host:port:port，id是为每个Zokeper 节点的编号，保存在dataDir⽬录下的myid⽂件中，zo1~zo3表示各个Zokeper节点的hostname， 第⼀个port是⽤于连接leader的端⼝，第⼆个port是⽤于leader选举的端⼝。

- 5. 在dataDir⽬录下创建myid⽂件，⽂件中只包含⼀⾏，且内容为该节点对应的server.id中的id编号。
- 6. 启动Zokeper服务： java -cp zokeper.jar:lib/log4j-

- 1.2.15.jar:conf \ org.apache.zokeper.server.quorum.QuorumPerMain zo.cfg 或者 bin/zkServer.sh start

7. 通过Zokeper客户端测试服务是否可⽤： java -cp zokeper.jar:src/java/lib/log4j-1.2.15.jar:conf:src/java/lib/jline-

- 0.9.94.jar \ org.apache.zokeper.ZoKeperMain -server 127.0.0.1 2181 或者 bin/zkCli.sh -server 127.0.0.1 2181 注意事项：

安装Storm依赖库 接下来，需要在Nimbus和Supervisor机器上安装Storm的依赖库，具体如下：

- 1. 2.1.7 – 请勿使⽤2.1.10版本，因为该版本的⼀些严重bug会导致Storm集群运⾏时出现奇怪 的问题。少数⽤户在2.1.7版本会遇到”IlegalArgumentException”的异常，此时降为2.1.4版本可修复这 ⼀问题。
- 2.
- 3. Java 6
- 4. Python 2.6.6
- 5. unzip 以上依赖库的版本是经过Storm测试的，Storm并不能保证在其他版本的Java或Python库下可运⾏。 安装ZMQ 2.1.7 下载后编译安装ZMQ： wget tar -xzf zeromq-2.1.7.tar.gz cd zeromq-


- 2.1.7 ./configure make sudo make instal




<table>
  <tr>
    <th>由于Zokeper是快速失败（fail-fast)的，且遇到任何错误情况，进程均会退出，因此，最好能通过 监控程序将Zokeper管理起来，保证Zokeper退出后能被⾃动重启。详情参考这⾥。 Zokeper运⾏过程中会在dataDir⽬录下⽣成很多⽇志和快照⽂件，⽽Zokeper运⾏进程并不负责 定期清理合并这些⽂件，导致占⽤⼤量磁盘空间，因此，需要通过cron等⽅式定期清除没⽤的⽇志和 快照⽂件。详情参考这⾥。具体命令格式如下：java -cp zokeper.jar:log4j.jar:conf<br><br>。</th>
  </tr>
</table>


org.apache.zokeper.server.PurgeTxnLog <dataDir> <snapDir> -n <count>

ZeroMQ

JZMQ

htp:/download.zeromq.org/zeromq-2.1.7.tar.gz

注意事项： 如果安装过程报错 uid找不到，则通过如下的包安装 uid库： sudo yum instal e2fsprogsl -b curent sudo yum instal e2fsprogs-devel -b curent 安装JZMQ 下载后编译安装JZMQ： git clone cd jzmq ./autogen.sh ./configure make sudo make

htps:/github.com/nathanmarz/jzmq.git

instal 为了保证JZMQ正常⼯作，可能需要完成以下配置：

- 1.
- 2.
- 3.
- 4.


正确设置 JAVA_HOME环境变量 安装Java开发包 升级autoconf 如果你是Mac OSX，参考这⾥

注意事项： 如果运⾏ ./configure 命令出现问题，参考 。 安装Java 6

这⾥

- 1. 下载并安装JDK 6，参考这⾥；
- 2. 配置JAVA_HOME环境变量；
- 3. 运⾏java、javac命令，测试java正常安装。 安装Python2.6.6


- 1. 下载Python2.6.6： wget
- 2. 编译安装Python2.6.6： tar –jxvf Python-2.6.6.tar.bz2 cd Python-2.6.6 ./configure make make instal
- 3. 测试Python2.6.6： python -V Python 2.6.6 安装unzip


htp:/ w.python.org/ftp/python/2.6.6/Python-2.6.6.tar.bz2

- 1. 如果使⽤RedHat系列Linux系统，执⾏以下命令安装unzip： apt-get instal unzip
- 2. 如果使⽤Debian系列Linux系统，执⾏以下命令安装unzip： yum instal unzip 下载并解压Storm发布版本 下⼀步，需要在Nimbus和Supervisor机器上安装Storm发⾏版本。


- 1. 下载Storm发⾏版本，推荐使⽤Storm0.8.1： wget
- 2. 解压到安装⽬录下： unzip storm-0.8.1.zip 修改storm.yaml配置⽂件


htps:/github.com/downloads/nathanmarz/storm/storm-0.8.1.zip

Storm发⾏版本解压⽬录下有⼀个conf/storm.yaml⽂件，⽤于配置Storm。默认配置在这⾥可以查看。 conf/storm.yaml中的配置选项将覆盖defaults.yaml中的默认配置。以下配置选项是必须在 conf/storm.yaml中进⾏配置的：

- 1) storm.zokeper.servers: Storm集群使⽤的Zokeper集群地址，其格式如下： storm.zokeper.servers: - “ 1. 2. 3. 4″ - “ 5. 6. 7. 8″ 如果Zokeper集群使⽤的不是默认端⼝，那么还需要storm.zokeper.port选项。
- 2) storm.local.dir: Nimbus和Supervisor进程⽤于存储少量状态，如jars、confs等的本地磁盘⽬录，需 要提前创建该⽬录并给以⾜够的访问权限。然后在storm.yaml中配置该⽬录，如： storm.local.dir: "/home/admin/storm/workdir"
- 3) java.library.path: Storm使⽤的本地库（ZMQ和JZMQ）加载路径，默认 为”/usr/local/lib:/opt/local/lib:/usr/lib”，⼀般来说ZMQ和JZMQ默认安装在/usr/local/lib 下，因此不需 要配置即可。
- 4) nimbus.host: Storm集群Nimbus机器地址，各个Supervisor⼯作节点需要知道哪个机器是 Nimbus，以便下载Topologies的jars、confs等⽂件，如： nimbus.host: " 1. 2. 3. 4"
- 5) supervisor.slots.ports: 对于每个Supervisor⼯作节点，需要配置该⼯作节点可以运⾏的worker数 量。每个worker占⽤⼀个单独的端⼝⽤于接收消息，该配置选项即⽤于定义哪些端⼝是可被worker使 ⽤的。默认情况下，每个节点上可运⾏4个workers，分别在670、6701、6702和6703端⼝，如： supervisor.slots.ports:


- - 670
- - 6701
- - 6702
- - 6703 启动Storm各个后台进程 最后⼀步，启动Storm的所有后台进程。和Zokeper⼀样，Storm也是快速失败（fail-fast)的系统， 这样Storm才能在任意时刻被停⽌，并且当进程重启后被正确地恢复执⾏。这也是为什么Storm不在进 程内保存状态的原因，即使Nimbus或Supervisors被重启，运⾏中的Topologies不会受到影响。 以下是启动Storm各个后台进程的⽅式： Nimbus: 在Storm主控节点上运⾏”bin/storm nimbus >/dev/nul 2>&1 &”启动Nimbus后台程序，并放 到后台执⾏； Supervisor: 在Storm各个⼯作节点上运⾏”bin/storm supervisor >/dev/nul 2>&1 &”启动Supervisor后 台程序，并放到后台执⾏； UI: 在Storm主控节点上运⾏”bin/storm ui >/dev/nul 2>&1 &”启动UI后台程序，并放到后台执⾏，启动 后可以通过htp:/{nimbus host}:8080观察集群的worker资源使⽤情况、Topologies的运⾏状态等信 息。 注意事项： 启动Storm后台进程时，需要对conf/storm.yaml配置⽂件中设置的storm.local.dir⽬录具有写权限。


Storm后台进程被启动后，将在Storm安装部署⽬录下的logs/⼦⽬录下⽣成各个进程的⽇志⽂件。 经测试，Storm UI必须和Storm Nimbus部署在同⼀台机器上，否则UI⽆法正常⼯作，因为UI进程会检 查本机是否存在Nimbus链接。 为了⽅便使⽤，可以将bin/storm加⼊到系统环境变量中。 ⾄此，Storm集群已经部署、配置完毕，可以向集群提交拓扑运⾏了。 向集群提交任务

- 1. 启动Storm Topology： storm jar almycode.jar org.me.MyTopology arg1 arg2 arg3 其中，almycode.jar是包含Topology实现代码的jar包，org.me.MyTopology的main⽅法是Topology的 ⼊⼝，arg1、arg2和arg3为org.me.MyTopology执⾏时需要传⼊的参数。
- 2. 停⽌Storm Topology： storm kil {toponame} 其中，{toponame}为Topology提交到Storm集群时指定的Topology任务名称。 参考资料


- 1.
- 2. 原⽂链接：


htps:/github.com/nathanmarz/storm/wiki/Tutorial htps:/github.com/nathanmarz/storm/wiki/Seting-up-a-Storm-cluster

htp:/blog.linezing.com/2013/01/how-to-instal-and-deploy-storm-cluster?spm=0.0.0.0. cTpkWf

【编辑推荐】

- 1.
- 2.
- 3.


Storm⼊⻔教程：前⾔ Storm⼊⻔教程：构建Topology Twiter利⽤Storm系统处理实时⼤数据

