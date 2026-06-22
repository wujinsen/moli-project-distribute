问题锦集1 关于Storm集群1.1 关于storm集群的环境变量配置问题 安装好JDK后，需要配置环境变量，通常情况下出于经验，我们往往会修改/etc/profile的值进⾏环境变 量配置，但这在安装JDK以及后⾯安装的storm集群、zokeper集群以及metaq集群时会出问题，这 时候我们需要在/etc/.bashrc⽂件中加⼊环境变量，不然安装的java和ZK集群等就⽆法使⽤，尤其这个 问题在我⽤shel写调度脚本去启动storm集群的时候就遇到过，如果没有将java的环境变量配置 在/etc/.bashrc⽂件中，就会报⼀个错，这个问题在后⾯我会提到。1.2 关于zokeper集群安装问题 记得刚刚接触storm，在安装zokeper集群的时候有这样的考虑：为什么不可以把zokeper只安装 在nimbus上，然后让其他的supervisor来它这⾥读取任务？如果在每台机器上都安装zokeper，那 nimbus分配任务的时候，是每台机器上的zokeper都收到同⼀份的任务，还是只是将分配给每个 supervisor节点的那部分写到同⼀节点上的zokeper中？ 有朋友解答说：ZK也是以集群的⽅式⼯作的，ZK集群内部有他⾃⼰的⼀套相互通信机制，⽽storm正 是要借助其通讯机制，例如任务下发等，往往在执⾏⼀个任务的时候，storm会把任务及相关执⾏的代 码经过序列化之后发送到各个ZK节点供supervisor去下载，然后才会各⾃执⾏⾃⼰部分的代码或者任 务。说的直接⼀点就是每个ZK节点收到的任务是⼀样的，⽽supervisor只需要下载属于⾃⼰的任务即

- 可。1.3 关于Storm中tuple 的可靠处理问题 Storm 为了保证tuple 的可靠处理，需要保存tuple 信息，这样会不会导致内存泄漏？ 关于这个问题，其实⽹上是有资料进⾏了详细的解释的。这⾥只是⼤概将⼀下，如果还不明⽩，可以 上⽹搜搜“storm可靠处理”。Storm 为了保证tuple 的可靠处理，acker 会保存该节点创建的tuple id的 xor （异或）值，这个值称为ack value，那么每ack ⼀次，就将tuple id 和ack value做异或(xor)。当所 有产⽣的tuple 都被ack 的时候，ack value 必定为0。这是个很简单的策略，对于每⼀个tuple 也只要 占⽤约20 个字节的内存。对于10万tuple，也才20M 左右，所以⼀般情况下是不⽤考虑内存泄漏问题 的。1.4 关于storm计算结果的存放问题 很多⼈在刚刚学习Storm 的时候都会有这个问题：storm处理后的结果保存在哪⾥？ 内存中？还是其 他地⽅？ 官⽅解释说： Storm 是不负责保存计算结果的，这是应⽤程序⾥需要负责的事情，如果数据不⼤，你 可以简单地保存在内存⾥，也可以每次都更新数据库，也可以采⽤NoSQL存储。storm 并没有像s4 那 样提供⼀个Persist API，根据时间或者容量来做存储输出。这部分事情完全交给⽤户。数据存储之后的 展现，也是你需要⾃⼰处理的，storm UI 只提供对topology 的监控和统计。1.5 关于Storm如何处理重 复的tuple问题 有⼈问到Storm 是怎么处理重复的tuple？ 因为Storm 要保证tuple 的可靠处理，当tuple 处理失败或者超时的时候，spout 会fail 并重新发送该 tuple，那么就会有tuple 重复计算的问题。这个问题是很难解决的，storm 也没有提供机制帮助你解 决。不过也有⼀些可⾏的策略：


- （1）不处理，这也算是种策略。因为实时计算通常并不要求很⾼的精确度，后 续的批处理计算会更正实时计算的误差。
- （2）使⽤第三⽅集中存储来过滤，⽐如利⽤MySQL、MemCached 或者Redis 根据逻辑主键来去重。
- （3）使⽤bl om filter 做过滤，简单⾼效。1.6 关于task与executor的关系问题


在storm的学习过程中，有许多⼈问到task与executor的关系问题。 在我们安装配置storm的时候，不知⼤家是否主要到了⼀个问题，就是我们在配置的时候会加⼏个 worker的端⼝( supervisor.slots.ports:)，⽐如众多⽂档中提到的670/6701等等类似的东⻄。没错， 这就是我们定义了该supervisor最多的worker数，worker中执⾏⼀个bolt或者spout线程，我们就称之 为task，⽽executor是物理上的线程概念，我们可以将其称为执⾏线程；⽽task更多是逻辑概念上的， 有时候bolt与spout的task会共⽤⼀个executor，特别是在系统负荷⽐较⾼的时候。1.7 关于Storm UI显 示内容的问题 Storm UI ⾥spout 统计的complete latency 的具体含义是什么？为什么emit 的数⽬会是acked 的两 倍？ 简单地说，complete latency 表示了tuple 从emit 到被acked 经过的时间，可以认为是tuple 以及该 tuple 的后续⼦孙（形成⼀棵树）整个处理时间。其次spout 的emit 和transfered 还统计了spout 和 acker 之间内部的通信信息，⽐如对于可靠处理的spout 来说，会在emit 的时候同时发送⼀个 _ack_init 给acker，记录tuple id 到task id 的映射，以便ack 的时候能找到正确的acker task。1.8 关于 Storm的ack和fail问题 在学习storm的过程中，有不少⼈对storm的Spout组件中的ack及fail相关的问题存在困惑，这⾥做⼀个 简要的概述。 Storm保证每⼀个数据都得到有效处理，这是如何保证的呢？正是ack及fail机制确保数据都得到处理的 保证，但是storm只是提供给我们⼀个接⼝，⽽具体的⽅法得由我们⾃⼰来实现。例如在spout下⼀个 拓扑节点的bolt上，我们定义某种情况下为数据处理失败，则调⽤fail，则我们可以在fail⽅法中进⾏数 据重发，这样就保证了数据都得到了处理。其实，通过读storm的源码，⾥⾯有讲到，有些类 （BaseBasicBolt？）是会⾃动调⽤ack和fail的，不需要我们程序员去ack和fail，但是其他Bolt就没有 这种功能了。1.9 关于IRichBolt与IBasicBolt接⼝的区别 ⾸先从类的组成上进⾏分析可以看到，IBasicBolt接⼝只有execute⽅法和declareOutputFields⽅法， ⽽IRichBolt接⼝上除了以上⼏个⽅法还有prepare⽅法和cleanup及map⽅法。⽽且其中execute⽅法是 有些不⼀样的，其参数列表不同。 总体来说Rich⽅法⽐较完善，我们可以使⽤prepare⽅法进⾏该Bolt类的初始化⼯作，例如我们链接数 据库时，需要进⾏⼀次数据库连接操作，我们就可以把该操作放⼊prepare中，只需要执⾏⼀次就可以 了。⽽cleanup⽅法能在该类调⽤结束时进⾏收尾⼯作，往往在处理数据的时候⽤到，例如在写hdfs （hadop的⽂件系统）数据的时候，在结束时需要进⾏数据clear，则需要进⾏数据收尾。当然，根据 官⽹及实际的测验，该⽅法往往是执⾏失败的。2 关于Topology发布2.1 发布topologies 到远程集群 时，出现Nimbus host is not set 异常 原因是Nimbus 没有被正确启动起来，可能是storm.yaml ⽂件没有配置，或者配置有问题。 解决⽅法：打开storm.yaml ⽂件正确配置：nimbus.host: " x. x. x. x"，重启nimbus 后台程序即

- 可。2.2 发布topology到远程集群时，出现AlreadyAliveException(msg: x is already active)异常 原因是提供的topology 与已经在运⾏的topology 重名。 解决⽅法：发布时换⼀个拓扑名称即可。2.3 启动Supervisor 时，出现java.lang.UnsatisfiedLinkEror 具体信息：启动Supervisor 时，出现java.lang.UnsatisfiedLinkEror:


/usr/local/lib/libjzmq.so.0.0.0: libzmq.so.1: canot open shared object file: No such file or directory 异常。 原因是未找到zmq 动态链接库。

- 解决⽅法1：配置环境变量 export LD_LIBRARY_PATH=/usr/local/lib
- 解决⽅法2：编辑/etc/ld.so.conf ⽂件，增加⼀⾏：/usr/local/lib。再执⾏ sudo ldconfig 命令，重启Supervisor。2.4 发布topologies 时，出现不能序列化log4j.Loger 的异常 原因是⽇志系统⽆法正确⽀付序列化。 解决⽅法：使⽤slf4j 代替log4j。2.5 bolt 在处理消息时，worker 的⽇志中出现Failing mesage 原因：可能是因为Topology 的消息处理超时所致。 解决⽅法：提交Topology 时设置适当的消息超时时间，⽐默认消息超时时间（30 秒）更⻓。⽐如： conf.setMesageTimeoutSecs(60);2.6 在打包toplogy⼯程的时候, 如果采⽤asembly⽅式, 对于相关 的依赖的配置⼀般要这样: Xml代码


- 1. <dependencySets>
- 2. <dependencySet>
- 3. <outputDirectory>/</outputDirectory>
- 4. <unpack>true</unpack>
- 5. <excludes>
- 6. <exclude>storm:storm</exclude>
- 7. </excludes>
- 8. </dependencySet>
- 9. </dependencySets>


wiki上说可以⽤<scope>compile</scope>。然后将storm依赖设置为runtime, 貌似不⾏。 另外就是所 有的依赖包将全部解压, 然后将所有依赖的配置和clas⽂件⽣成⼀个⽂件。这个是通过 <unpack>true</unpack>参数来控制的。2.7 在提交topology的时候有时可能出现如下异常:Exception in thread "main" java.lang.IlegalArgumentException: Nimbus host is not set at backtype.storm.utils.NimbusClient.<init>(NimbusClient.java:30) at backtype.storm.utils.NimbusClient.getConfiguredClient(NimbusClient.java:17) at backtype.storm.StormSubmiter.submitJar(StormSubmiter.java:78) at backtype.storm.StormSubmiter.submitJar(StormSubmiter.java:71) at backtype.storm.StormSubmiter.submitTopology(StormSubmiter.java:50) at com.taobao.kaleidoscope.storm.IcdbTopology.main(IcdbTopology.java:59)

但是启动nimbus是没有问题的, 这个主要因为conf_dir路径设置不正确, 在bin/storm脚本中需要加上这 样⼀句: Python代码

1. CONF_DIR = STORM_DIR + "/conf"

3 关于DRPC3.1 发布drpc 类型的topologies 到远程集群时，出现空指针异常，连接drpc服务器失败 原因是未正确配置drpc 服务器地址。 解决⽅法：在conf/storm.yaml ⽂件中增加drpc 服务器配置，启动配置⽂件中 指定的所有drpc 服务。内容如下： drpc.servers:

- "drpc 服务器ip"3.2 客户端调⽤drpc 服务时，worker 的⽇志中出现Failing mesage，⽽bolt都未收 到数据 错误⽇志如下所示： 201-12-02 09 59 16 task [INFO] Failing mesage backtype.storm.drpc.DRPCSpout$DRPCMesageId@370bdf7: source: 1 27, stream: 1, id: {-591945153131571689=-591945153131571689}, [fo.com/blog/1, {"port":372,"id":"5","host":"10.0.0.24"}] 原因是主机名，域名，hosts ⽂件配置不正确会引起这类错误。 解决⽅法：检查并修改storm 相关机器的主机名，域名，hosts ⽂件。重启⽹络服务：service network restart。重启storm，再次调⽤drpc 服务，成功。Hosts ⽂件中必须包含如下 内容： [nimbus 主机ip] [nimbus 主机名] [nimbus 主机别名] [supervisor 主机ip] [supervisor 主机名] [supervisor 主机别名] [zokeper 主机ip] [zokeper 主机名] [zokeper 主机别名]4 关于jzmq安装4.1 storm 启动时报no jzmq in java.library.path 错误 原因是找不到jzmq，默认情况下在执⾏instal_zmq.sh 时，那些.so ⽂件 安装路径在/usr/local/lib，但是实际安装时可能装在其他的路径下了。 解决⽅法：在storm.yaml 中添加： java.library.path: "/opt/storm/jzmq/lib:/opt/storm/zeromq/lib:/usr/local/lib:/opt/local/ lib:/usr/lib"4.2 安装jzmq 时遇到No rule to make target ‘clasdist_noinst.stampʼ的make 错误 具体的make 错误信息： make[1]: * No rule to make target `clasdist_noinst.stamp',neded by `org/zeromq/ZMQ.clas'. Stop. 解决⽅法：⼿动创建clasdist_noinst.stamp 空⽂件。 touch src/clasdist_noinst.stamp4.3 安装jzmq 时遇到canot aces org.zeromq.ZMQ 的make 错误 具体的make 错误信息： eror: canot aces org.zeromq.ZMQ clas file for org.zeromq.ZMQ not found javadoc: eror - Clas org.zeromq.ZMQ not found. 解决⽅法：⼿动编译，然后重新make 即可通过。

cd src javac -d . org/zeromq/*.java cd.4.4 在部署storm节点的时候需要安装jzmq和zeromq, 在安装这两个依赖包之后, 需要执⾏sudo -u rot ldconfig. 否则会出现异常:2012-02-24 16 30 30 worker [EROR] Eror on initialization of server mk-worker java.lang.UnsatisfiedLinkEror: /usr/local/lib/libjzmq.so.0.0.0: libzmq.so.1: canot open shared object file: No such file or directory

at java.lang.ClasLoader$NativeLibrary.load(Native Method) at java.lang.ClasLoader.loadLibrary0(ClasLoader.java:1803) at java.lang.ClasLoader.loadLibrary(ClasLoader.java:1728) at java.lang.Runtime.loadLibrary0(Runtime.java:823) at java.lang.System.loadLibrary(System.java:1028) at org.zeromq.ZMQ.<clinit>(ZMQ.java:34)5 关于Storm的配置问题

- 1. yaml跟我们⼀般⽤的属性配置⽂件有所不同, 它的要求更严格⼀些, 因此在往conf/storm.yaml中 添加配置的时候必须注意，⽐如必须注意开始位置和冒号后⾯的空格, 否则配置不会⽣效。
- 2. 如何检查配置是否⽣效？ 可以使⽤命令: storm localconfvalue 配置关键字 但是这个命令只能在nimbus上⽣效, 在supervisor看到的还是默认值. 不知道为什么 。6 关闭storm相 关进程6.1 关闭nimbus相关进程: kil `ps aux | egrep '(daemon\.nimbus)|(storm\.ui\.core)' | fgrep -v egrep | awk '{print $2}'` 备注：这是在⽹上看到的，没有经过实际测试，有兴趣的朋友可以⾃⼰测试⼀下。6.2 ⼲掉supervisor 上的所有storm进程: kil `ps aux | fgrep storm | fgrep -v 'fgrep' | awk '{print $2}'` 备注：这是在⽹上看到的，没有经过实际测试，有兴趣的朋友可以⾃⼰测试⼀下。7 关于Topology发 布之后的log


- 1) ⽤storm jar.将项⽬提交给storm集群后，想查看本项⽬的log信息，要到supervisor机器的： storm安装路径/logs/worker-number.log（其中的number视实际情况⽽定）中查看。
- 2) 如果是⽤daemontols启动的storm，daemontols监控的⽬录是/service/storm，那么 到/service/storm/logs中查看worker-number.log⽇志。
- 3) 若要更改log的级别，是debug还是info等，在storm安装路径/log4j下有个配置⽂件，按需要修 改即可。
- 4) Storm的debug模式下，它本身的log⾮常庞⼤，所以我觉得⾃⼰的代码中有些重要的信息，⽤ info⽐较好，这样将storm的log级别调整为info⽐较⽅便查看。8 关于maven打包问题8.1 ⾸先maven的 pom⽂件中的storm依赖，要么加exclude storm的相关语句（github有说明），要么加<scope>，如 下：


<dependency>

<groupId>storm</groupId> <artifactId>storm</artifactId> <scope>test</scope>

</dependency> 加scope可以使打jar包时，不包含storm。如果包含了storm，那么提交到storm集群，会运⾏出错。官 ⽅要求打jar包时，要去除storm的依赖。8.2 使⽤maven插件，在打jar包时，包含依赖。 在pom中加⼊： <plugin>

<artifactId>maven-asembly-plugin</artifactId> <configuration>

<descriptorRefs>

<descriptorRef>jar-with-dependencies</descriptorRef> </descriptorRefs> <archive>

<manifest>

<mainClas>com.path.to.main.Clas</mainClas> </manifest>

</archive>

</configuration> </plugin> 打jar包时使⽤命令：mvn asembly:asembly8.3 依赖的jar冲突问题 如果本地依赖的jar与storm的lib下的jar有冲突，即都⽤了⼀个jar，但是版本不同，那么貌似⽬前只能 改为跟storm保持统⼀。官⽅的讨论组是这样说的。9 关于nimbus的启动问题9.1 Storm nimbus启动失 败 在使⽤了storm⼀段时间后，需要重新部署storm的集群，主要是想将storm部署在其它机器上。做了 以下错误操作：

- 1) 没有kil 正在运⾏的topology，kil nimbus和supervisor的storm进程
- 2) 删除了配置中"storm.local.dir"的⽂件夹内的内容
- 3) 启动storm nimbus报错：


backtype.storm.daemon.nimbus $fn_2692$exec_fn_945_auto _2693$this_2731@6213513 java.io.FileNotFoundException: File '/opt/aps-instal/storm/ storm_local/nimbus/stormdist/apFailed-6-1325065153/stormconf.ser' does not exist

at org.apache.comons.io.FileUtils.openInputStream(FileUtils.java:137) at

org.apache.comons.io.FileUtils.readFileToByteAray(FileUtils.java: 135) at backtype.storm.daemon.nimbus $read_storm_conf.invoke(nimbus.clj:128) at backtype.storm.daemon.nimbus $compute_new_task_GT_node_PLUS_port.invoke(nimbus.clj:24) at backtype.storm.daemon.nimbus $mk_asignments.invoke(nimbus.clj:28) at backtype.storm.daemon.nimbus $fn_2692$exec_fn_945_auto _2693$this_2731.invoke(nimbus.clj:460)

at backtype.storm.event$event_manager $fn_2068$fn_2069.invoke(event.clj:25)

at backtype.storm.event$event_manager

$fn_2068.invoke(event.clj: 2) at clojure.lang.AFn.run(AFn.java:24) at java.lang.Thread.run(Thread.java: 62)

201-12-29 16 15 02 util [INFO] Halting proces: ("Eror when procesing an event") 报错原因：因为没有先kil topology，所以在启动nimbus时，zokeper中依然保留了上次运⾏着的 topology的信息。 解决办法：⽤zokeper的zkCli.sh清理⼀下，我直接重装了zokeper。但是据说在storm 0.6.1中解 决了该bug。⽽我⽤的是storm 0.6.0。10 Storm使⽤JVM参数 在配置⽂件storm.yaml中，有：# to nimbus nimbus.childopts: "-Xmx1024m"# to supervisor supervisor.childopts: "-Xmx1024m"# to worker worker.childopts: "-Xmx768m" 如果worker在运⾏时，需要⽤指定的JVM参数，那么可以像这样配置： worker.childopts: "-Dworker=worker -Xmx768m -Xdebug –Xnoagent -Djava.compiler=NONE Xrunjdwp:transport=dt_socket,adres=81,suspend=y,server=y "1 关于spout/bolt的⽣命周期 ⼀般来说spout/bolt的⽣命周期如下:

- 1 在提交了⼀个topology之后(在nimbus所在的机器), 创建spout/bolt实例(spout/bolt在storm中统 称为component)并进⾏序列化；
- 2 将序列化的component发送给所有的任务所在的机器；
- 3 在每⼀个任务上反序列化component；
- 4 在开始执⾏任务之前, 先执⾏component的初始化⽅法(bolt是prepare, spout是open)； 因此component的初始化操作应该在prepare/open⽅法中进⾏, ⽽不是在实例化component的时候进 ⾏。12关于storm与spring框架集成问题


⾸先声明⼀下，这个问题是当时有考虑到是否可以将storm与spring集成时，在⽹上看到的⼀点介绍， 只是为了⽇后做参考。 在进⾏storm与spring集成时，本来想着⼀次就能成功，抱着很⼤的希望可是运⾏时竟然报了个 java.io.NotSerializableException的异常。该异常要求被依赖注⼊的jar包实现序列化接⼝，但那些jar包 都是别⼈开发的你不能⼀个⼀个都改掉源码才能⽤到项⽬⾥。 再⽹上找⼀下还真有⼈遇到类似的问题，具体原因是对storm的spout和bolt的⽣命周期理解的不够深 刻。 ⼀般来说spout/bolt的⽣命周期如下:

- 1.在提交了⼀个topology之后(在nimbus所在的机器), 创建spout/bolt实例(spout/bolt在storm中统称为 component)并进⾏序列化.
- 2.将序列化的component发送给所有的任务所在的机器
- 3.在每⼀个任务上反序列化component.
- 4.在开始执⾏任务之前, 先执⾏component的初始化⽅法(bolt是prepare, spout是open). 因此component的初始化操作应该在prepare/open⽅法中进⾏, ⽽不是在实例化component的时候进 ⾏. 按照这种说法进⾏改造，结构该问题消失了。但接下来⼜有了新的问题： Caused by: org.xml.sax.SAXParseException: Content is not alowed in prolog. 这个异常⽹上搜索之后发现原来是由于*.xml⽂件编码的问题。原因是在从其他项⽬⾥或者编辑⼯具编 辑时，在⽂件编码中加⼊了BOM头的原因，于是⽤notePad+打开xml⽂件选择去掉BOM头信息，重 新进⾏保存即可。13 关于java.lang.NoClasDefFoundEror: clojure.core.protocols$ 原因：JDK版本不匹配，安装虚拟机时系统⾃带⼀个jdk.1.5.0。 解决办法：检查jdk版本，卸载系统⾃带的JDK，使⽤⾃⼰安装的JDK版本。


＃ rpm –qa | grep java # rpm –e –nodeps java-*

配置环境变量，vi /etc/profile 重新执⾏⼀遍试试，貌似问题解决了。14 关于storm连接Mysql 连接远程mysql是报如下错误： mesage from server:"Host FILTER" is not alowed to conect to this MySQL server 解决⽅案： 很可能是你没有给其他IP访问你数据库的权限，你可以试试： 在MySql数据库的主机上，在mysql命令⾏中输⼊以下命令： grant al on.* to rot@'%' identified by " 1" ; 这样，给任何IP都赋予了访问的权限， 任何IP都能以，⽤户名：rot ，密码： 1 来进⾏局域⽹的访问！ (命令中*.*是通配任何IP，你也可以指定IP)15 关于metaq启动的出现服务拒绝连接的问题

解决办法：在metaq安装⽬录下，删掉之前的⽇志⽂件，测试⽹络是否正常连接。将之前的服务的 metaq进程kil掉，然后重启。16 关于topology的spout与bolt 之前有问到，⼀个topology中可不可以有多个spout？这个问题貌似很幼稚啊，呵呵。关于这个问题， 我是这样考虑的：实际应⽤中，如果我们每⼀条应⽤都创建⼀个topology的话，未免也太夸张了。如 果是同⼀个应⽤，同⼀个数据来源，但是你想分⼏种⽅式对这个数据做处理的话，这时候就应该是建 多个spout了，让这些spout并⾏去读数据，然后交给订阅这个spout的bolt去处理就⾏，没必要⼀种处 理⽅式建⼀个topology。17 关于shel脚本编码格式问题 这是我在写启动storm集群的shel脚本时遇到的⼀个实际问题。shel脚本运⾏时报错误：/bin/bash^M: bad interpreter 出现原因：windows上写的脚本，直接拷⻉到linux系统上运⾏由于格式不兼容导致。17.1 解决⽅案 （⼀）：

- 1. ⽐如⽂件名为myshel.sh，vim myshel.sh
- 2. 执⾏vim中的命令 : set f?查看⽂件格式，如果显示fileformat=dos，证明⽂件格式有问题。
- 3. 执⾏vim中的命令 :set fileformat=unix 将⽂件格式改过来就可以了，然后:wq保存退出就可以了。 17.2 解决⽅案（⼆） 或者使⽤最笨的⽅法：将windows下编辑好的脚本通过txt⽂本格式转换，然后在拷⻉到linux下。 如果是使⽤Notepad编辑器进⾏编辑的话，可以在菜单栏上选择“编辑”—“档案格式转换”—“转换为 UNIX 格式”。 最后说明⼀下，这些问题只是storm应⽤过程中遇到的⼀⼩部分问题，其实还有很多问题是涉及到实际 项⽬的考虑的，⽐如集群硬件要求，参数配置，⽇志处理等等，具体问题具体分析吧，也希望哪些在 实际项⽬中⽤到storm的⼤神们，能多多和⼤家分享你们的实际经验，毕竟实践出真知，任何新技术， 只有经过实际应⽤和实际检验，分享出来的东⻄才有说服⼒。


