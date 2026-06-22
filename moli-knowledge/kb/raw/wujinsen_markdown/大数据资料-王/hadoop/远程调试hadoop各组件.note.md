远程调试对应⽤程序开发⼗分有⽤。例如，为不能托管开发平台的低端机器开发程序，或在专⽤的机 器上（⽐如服务不能中断的 Web 服务器）调试程序。其他情况包括：运⾏在内存⼩或 CUP 性能低的 设备上的 Java 应⽤程序（⽐如移动设备），或者开发⼈员想要将应⽤程序和开发环境分开，等等。 为了进⾏远程调试，必须使⽤ Java Virtual Machine (JVM) V5.0 或更新版本。

# JPDA 简介

Sun Microsystem 的 Java Platform Debugger Architecture (JPDA) 技术是⼀个多层架构，使您能够在 各种环境中轻松调试 Java 应⽤程序。JPDA 由两个接⼝（分别是 JVM Tool Interface 和 JDI）、⼀个 协议（Java Debug Wire Protocol）和两个⽤于合并它们的软件组件（后端和前端）组成。它的设计⽬ 的是让调试⼈员在任何环境中都可以进⾏调试。 更详细的介绍，您可以参考

使⽤ Eclipse 远程调试 Java 应⽤程序

# JDWP 设置

JVM本身就⽀持远程调试，Eclipse也⽀持JDWP，只需要在各模块的JVM启动时加载以下参数：

- -Xdebug -Xrunjdwp:transport=dt_socket, address=8000,server=y,suspend=y 各参数的含义：
- -Xdebug 启⽤调试特性
- -Xrunjdwp 启⽤JDWP实现，包含若⼲⼦选项： transport=dt_socket JPDA front-end和back-end之间的传输⽅法。dt_socket表示使⽤套接字传输。 address=8000 JVM在8000端⼝上监听请求，这个设定为⼀个不冲突的端⼝即可。 server=y y表示启动的JVM是被调试者。如果为n，则表示启动的JVM是调试器。 suspend=y y表示启动的JVM会暂停等待，直到调试器连接上才继续执⾏。suspend=n，则JVM不会暂停等待。


## 配置hbase远程调试

打开/etc/hbase/conf/hbase-env.sh，找到以下内容： # Enable remote JDWP debugging of major HBase processes. Meant for Core Developers # export HBASE_MASTER_OPTS="$HBASE_MASTER_OPTS -Xdebug -

- Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8070" # export HBASE_REGIONSERVER_OPTS="$HBASE_REGIONSERVER_OPTS -Xdebug -
- Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8071" # export HBASE_THRIFT_OPTS="$HBASE_THRIFT_OPTS -Xdebug -
- Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8072" # export HBASE_ZOOKEEPER_OPTS="$HBASE_ZOOKEEPER_OPTS -Xdebug -
- Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8073"


如果想远程调式hbase-master进程，请去掉对HBASE_MASTER_OPTS的注释，其他依次类推。注意，我这 ⾥使⽤的是cdh-4.3.0中的hbase。

## 配置hive远程调试

停⽌hive-server2进程，然后以下⾯命令启动hive-server2 hive --service hiveserver --debug 进程会监听在8000端⼝等待调试连接。如果想更改监听端⼝，可以修改配置⽂ 件:${HIVE_HOME}bin/ext/debug.sh 如果Hadoop是0.23以上版本，debug模式启动Cli会报错： ERROR: Cannot load this JVM TI agent twice, check your java command line for duplicate jdwp options. 打开${Hadoop_HOME}/bin/hadoop，注释掉以下代码 # Always respect HADOOP_OPTS and HADOOP_CLIENT_OPTS HADOOP_OPTS="$HADOOP_OPTS $HADOOP_CLIENT_OPTS"

## 配置yarn远程调试

请在以下代码添加调试参数：

if [ "$COMMAND" = "classpath" ] ; then if $cygwin; then CLASSPATH=`cygpath -p -w "$CLASSPATH"` fi echo $CLASSPATH exit elif [ "$COMMAND" = "rmadmin" ] ; then CLASS='org.apache.hadoop.yarn.client.RMAdmin' YARN_OPTS="$YARN_OPTS $YARN_CLIENT_OPTS" elif [ "$COMMAND" = "application" ] ; then class="org".apache.hadoop.yarn.client.cli.ApplicationCLI YARN_OPTS="$YARN_OPTS $YARN_CLIENT_OPTS" elif [ "$COMMAND" = "node" ] ; then class="org".apache.hadoop.yarn.client.cli.NodeCLI YARN_OPTS="$YARN_OPTS $YARN_CLIENT_OPTS" elif [ "$COMMAND" = "resourcemanager" ] ; then CLASSPATH=${CLASSPATH}:$YARN_CONF_DIR/rm-config/log4j.properties CLASS='org.apache.hadoop.yarn.server.resourcemanager.ResourceManager' YARN_OPTS="$YARN_OPTS $YARN_RESOURCEMANAGER_OPTS" if [ "$YARN_RESOURCEMANAGER_HEAPSIZE" != "" ]; then JAVA_HEAP_MAX="-Xmx""$YARN_RESOURCEMANAGER_HEAPSIZE""m" fi elif [ "$COMMAND" = "nodemanager" ] ; then CLASSPATH=${CLASSPATH}:$YARN_CONF_DIR/nm-config/log4j.properties CLASS='org.apache.hadoop.yarn.server.nodemanager.NodeManager' YARN_OPTS="$YARN_OPTS -server $YARN_NODEMANAGER_OPTS" if [ "$YARN_NODEMANAGER_HEAPSIZE" != "" ]; then JAVA_HEAP_MAX="-Xmx""$YARN_NODEMANAGER_HEAPSIZE""m" fi elif [ "$COMMAND" = "proxyserver" ] ; then CLASS='org.apache.hadoop.yarn.server.webproxy.WebAppProxyServer' YARN_OPTS="$YARN_OPTS $YARN_PROXYSERVER_OPTS" if [ "$YARN_PROXYSERVER_HEAPSIZE" != "" ]; then JAVA_HEAP_MAX="-Xmx""$YARN_PROXYSERVER_HEAPSIZE""m" fi

例如： 如果你想调试resourcemanager代码，请在elif [ "$COMMAND" = "resourcemanager" ] 分⽀内 添加如下代码： YARN_RESOURCEMANAGER_OPTS="$YARN_RESOURCEMANAGER_OPTS -Xdebug Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=6001" 其他进程，参照上⾯即可。 注意：端⼝不要冲突。

## 配置mapreduce远程调试

如果想要调试Map 或Reduce Task，则修改bin/hadoop已经没⽤了，因为bin/hadoop中没有Map Task 的启动参数。 此时需要修改mapred-site.xml <property>

<name>mapred.child.java.opts</name> <value>-Xmx800m -Xdebug -

Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000</value> </property> 在⼀个TaskTracker上，只能启动⼀个Map Task或⼀个Reduce Task，否则启动时会有端⼝冲突。因 此要修改所有TaskTracker上的conf/hadoop-site.xml中的配置项： <property>

<name>mapred.tasktracker.map.tasks.maximum</name> <value>1</value>

</property> <property>

<name>mapred.tasktracker.reduce.tasks.maximum</name> <value>1</value>

</property>

在Eclipse中使⽤⽅法：

- 1.
- 2.
- 3.
- 4.


打开eclipse，找到Debug Configurations...，添加⼀个Remout Java Application: 在source中可以关联到hive的源代码,然后，单击Debug按钮进⼊远程debug模式。 编写个jdbc的测试类，运⾏代码，这时候因为hive-server2端没有设置端点，故程序可以正常运⾏ 直到结束。 在hive代码中设置⼀个断点，如ExecDriver.java的execute⽅法中设置断点，然后再运⾏jdbc测试 类。

参考⽂章

- 1.
- 2.
- 3.


在Eclipse中远程调试Hadoop hive远程调试 使⽤ Eclipse 远程调试 Java 应⽤程序

上⼀篇Hadop系列 之Terasort 下⼀篇eclipse调试时⿏标移动到变量上不显示值的问题

