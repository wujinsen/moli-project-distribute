hwi(hive web interface)是hive命令⾏接⼝的⼀个补充，主要功能包括：

- 1.shema browsing ：获取table的信息，包括serde、columen name、column type

- 2.detached query execution：在hive命令⾏界⾯，⽤户如果需要执⾏多个查询，则需要同时打开 多个命令⾏界⾯。hwi允许⽤户同时开始多个查询，并且查看执⾏状态。

- 3.no local installation：如何⼈，只要有web browser就可以连接hive，⽽⽆需运⾏安装运⾏任何客 户端。


从hive 0.8.1开始，⾃带hwi，简单配置下即可使⽤

运⾏：

- 1.配置：


$ cd /home/work/hive/hive-0.8.1/conf;

$ cp hive-default.xml.template hive-site.xml

<table>
  <tr>
    <th><property> <name>hive.hwi.listen.host</name> <value>0.0.0.0</value> <description>This is the host address the Hive Web Interface will listen<br><br>on</description> </property><br><br><property> <name>hive.hwi.listen.port</name> <value>9999</value> <description>This is the port the Hive Web Interface will listen<br><br>on</description> </property><br><br><property> <name>hive.hwi.war.file</name> <value>lib/hive_hwi.war</value> <description>This is the WAR file with the jsp content for Hive Web<br><br>Interface</description> </property<br><br></th>
  </tr>
</table>


>

测试环境下，使⽤默认值即可，需要hive-site.xml⽂件，否则报错。

- 2，现在要做的就是添加apache ant的编译⼯具，不安装则会报错


Problem accessing /hwi/. Reason:

No Java compiler available

- 2.1安装ant：


下载安装包wget http://apache.mirrors.tds.net/ant/binaries/apache-ant-1.9.4-bin.tar.gz

.解压缩：tar -xvf apache-ant-1.9.4-bin.tar.gz

切换到解压⽬录：cd apache-ant-1.9.4

复制：cp -arp * /usr/local/ant

PATH 设置：echo 'export PATH=$PATH:/usr/local/ant/bin'>>/etc/profile

ANT_HOME 设置：echo 'export ANT_HOME=/usr/local/ant'>>/etc/profile

本次登陆 PATH 设置：export PATH=$PATH:/usr/local/ant/bin

本次登陆 ANT_HOME 设置：export ANT_HOME=/usr/local/ant

现在可以执⾏ ant -v 或者 ant --version 来验证你的 ant 是否已安装好了呢？

- 2.2 安装完后需要设置path：

设置ANT_LIB路径

export ANT_LIB= /usr/local/ant/lib

- 2.3 需要把


ant-launcher.jar ant.jar jasper-compiler-5.5.23.jar jasper-runtime-5.5.23.jar

这两个jar包拷⻉到$HIVE_HOME/lib下，并且需要将相应的权限修改为777，

否则会报错：

hive hwi Compile failed; see the compiler error output for details.

- 3，异常: Caused by:


Unable to find a javac compiler;

com.sun.tools.javac.Main is not on the classpath.

Perhaps JAVA_HOME does not point to the JDK.

It is currently set to "/opt/jdk/jre"

# cp $JAVA_HOME/lib/tools.jar $HIVE_HOME/lib/

2.后台运⾏hwi：

$ nohup hive --config $HIVE_CONF_DIR --service hwi > /dev/null 2> /dev/null &

测试：

hive运⾏机器ip地址：192.168.29.81

打开web browser，

输⼊：http://192.168.29.81:9999/hwi/

如下：

![image 1](<No Java compiler available.note_images/imageFile1.png>)

hwi⻚⾯的使⽤请参⻅：

http://blog.csdn.net/skywalker_only/article/details/26601779

http://www.cnblogs.com/gpcuster/archive/2010/02/25/1673480.html

其他：

[hadoop@namenode1 ~]$ 14/06/11 13:04:34 INFO hwi.HWIServer: HWI is starting up

14/06/11 13:04:35 WARN conf.Configuration: mapred.max.split.size is deprecated. Instead, use mapreduce.input.fileinputformat.split.maxsize

14/06/11 13:04:35 WARN conf.Configuration: mapred.min.split.size is deprecated. Instead, use mapreduce.input.fileinputformat.split.minsize

14/06/11 13:04:35 WARN conf.Configuration: mapred.min.split.size.per.rack is deprecated. Instead, use mapreduce.input.fileinputformat.split.minsize.per.rack

14/06/11 13:04:35 WARN conf.Configuration: mapred.min.split.size.per.node is deprecated. Instead, use mapreduce.input.fileinputformat.split.minsize.per.node

14/06/11 13:04:35 WARN conf.Configuration: mapred.reduce.tasks is deprecated. Instead, use mapreduce.job.reduces

14/06/11 13:04:35 WARN conf.Configuration: mapred.reduce.tasks.speculative.execution is deprecated. Instead, use mapreduce.reduce.speculative

14/06/11 13:04:35 WARN conf.Configuration: org.apache.hadoop.hive.conf.LoopingByteArrayInputStream@644bffe5:an attempt to override final parameter: mapreduce.job.end-notification.max.retry.interval; Ignoring.

14/06/11 13:04:35 WARN conf.Configuration: org.apache.hadoop.hive.conf.LoopingByteArrayInputStream@644bffe5:an attempt to override final parameter: mapreduce.job.end-notification.max.attempts; Ignoring.

14/06/11 13:04:35 INFO mortbay.log: Logging to org.slf4j.impl.Log4jLoggerAdapter(org.mortbay.log) via org.mortbay.log.Slf4jLog

14/06/11 13:04:35 INFO mortbay.log: jetty-6.1.26.cloudera.2

- 14/06/11 13:04:35 INFO mortbay.log: Extract /opt/hive/hive-0.10.0-cdh4.5.0/lib/hive-hwi-0.10.0cdh4.5.0.war to /tmp/Jetty_0_0_0_0_9999_hive.hwi.0.10.0.cdh4.5.0.war__hwi__igaget/webapp

- 14/06/11 13:04:36 INFO mortbay.log: Started SocketConnector@0.0.0.0:9999


14/06/11 13:04:41 ERROR mortbay.log: /hwi/

The following error occurred while executing this line:

jar:file:/usr/local/ant/lib/ant.jar!/org/apache/tools/ant/antlib.xml:37: Problem: failed to create task or type componentdef

Cause: The name is undefined.

Action: Check the spelling.

Action: Check that any custom tasks/types have been declared.

Action: Check that any <presetdef>/<macrodef> declarations have taken place.

at org.apache.tools.ant.ProjectHelper.addLocationToBuildException(ProjectHelper.java:508)

at org.apache.tools.ant.taskdefs.Definer.loadAntlib(Definer.java:434)

at org.apache.tools.ant.taskdefs.Definer.execute(Definer.java:281)

at org.apache.tools.ant.ComponentHelper.checkNamespace(ComponentHelper.java:790)

at org.apache.tools.ant.ComponentHelper.getDefinition(ComponentHelper.java:260)

at org.apache.tools.ant.ComponentHelper.getComponentClass(ComponentHelper.java:250)

at org.apache.tools.ant.ComponentHelper.createNewTask(ComponentHelper.java:486)

at org.apache.tools.ant.ComponentHelper.createTask(ComponentHelper.java:462)

at org.apache.tools.ant.Project.createTask(Project.java:1119)

at org.apache.jasper.compiler.AntCompiler.generateClass(AntCompiler.java:134)

at org.apache.jasper.compiler.Compiler.compile(Compiler.java:298)

at org.apache.jasper.compiler.Compiler.compile(Compiler.java:277)

at org.apache.jasper.compiler.Compiler.compile(Compiler.java:265)

at org.apache.jasper.JspCompilationContext.compile(JspCompilationContext.java:564)

at org.apache.jasper.servlet.JspServletWrapper.service(JspServletWrapper.java:299)

at org.apache.jasper.servlet.JspServlet.serviceJspFile(JspServlet.java:315)

at org.apache.jasper.servlet.JspServlet.service(JspServlet.java:265)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:820)

at org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:511)

at org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:401)

at org.mortbay.jetty.security.SecurityHandler.handle(SecurityHandler.java:216)

at org.mortbay.jetty.servlet.SessionHandler.handle(SessionHandler.java:182)

at org.mortbay.jetty.handler.ContextHandler.handle(ContextHandler.java:766)

at org.mortbay.jetty.webapp.WebAppContext.handle(WebAppContext.java:450)

at org.mortbay.jetty.servlet.Dispatcher.forward(Dispatcher.java:327)

at org.mortbay.jetty.servlet.Dispatcher.forward(Dispatcher.java:126)

at org.mortbay.jetty.servlet.DefaultServlet.doGet(DefaultServlet.java:503)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:707)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:820)

at org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:511)

at org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:401)

at org.mortbay.jetty.security.SecurityHandler.handle(SecurityHandler.java:216)

at org.mortbay.jetty.servlet.SessionHandler.handle(SessionHandler.java:182)

at org.mortbay.jetty.handler.ContextHandler.handle(ContextHandler.java:766)

at org.mortbay.jetty.webapp.WebAppContext.handle(WebAppContext.java:450)

at org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)

at org.mortbay.jetty.handler.RequestLogHandler.handle(RequestLogHandler.java:49)

at org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)

at org.mortbay.jetty.Server.handle(Server.java:326)

at org.mortbay.jetty.HttpConnection.handleRequest(HttpConnection.java:542)

at org.mortbay.jetty.HttpConnection$RequestHandler.headerComplete(HttpConnection.java:928)

at org.mortbay.jetty.HttpParser.parseNext(HttpParser.java:549)

at org.mortbay.jetty.HttpParser.parseAvailable(HttpParser.java:212)

at org.mortbay.jetty.HttpConnection.handle(HttpConnection.java:404)

at org.mortbay.jetty.bio.SocketConnector$Connection.run(SocketConnector.java:228)

at org.mortbay.thread.QueuedThreadPool$PoolThread.run(QueuedThreadPool.java:582)

Caused by: jar:file:/usr/local/ant/lib/ant.jar!/org/apache/tools/ant/antlib.xml:37: Problem: failed to create task or type componentdef

Cause: The name is undefined.

Action: Check the spelling.

Action: Check that any custom tasks/types have been declared.

Action: Check that any <presetdef>/<macrodef> declarations have taken place.

at org.apache.tools.ant.UnknownElement.getNotFoundException(UnknownElement.java:484)

at org.apache.tools.ant.UnknownElement.makeObject(UnknownElement.java:416)

at org.apache.tools.ant.UnknownElement.maybeConfigure(UnknownElement.java:160)

at org.apache.tools.ant.taskdefs.Antlib.execute(Antlib.java:146)

at org.apache.tools.ant.taskdefs.Definer.loadAntlib(Definer.java:432)

... 44 more

--- Nested Exception ---

jar:file:/usr/local/ant/lib/ant.jar!/org/apache/tools/ant/antlib.xml:37: Problem: failed to create task or type componentdef

Cause: The name is undefined.

Action: Check the spelling.

Action: Check that any custom tasks/types have been declared.

Action: Check that any <presetdef>/<macrodef> declarations have taken place.

at org.apache.tools.ant.UnknownElement.getNotFoundException(UnknownElement.java:484)

at org.apache.tools.ant.UnknownElement.makeObject(UnknownElement.java:416)

at org.apache.tools.ant.UnknownElement.maybeConfigure(UnknownElement.java:160)

at org.apache.tools.ant.taskdefs.Antlib.execute(Antlib.java:146)

at org.apache.tools.ant.taskdefs.Definer.loadAntlib(Definer.java:432)

at org.apache.tools.ant.taskdefs.Definer.execute(Definer.java:281)

at org.apache.tools.ant.ComponentHelper.checkNamespace(ComponentHelper.java:790)

at org.apache.tools.ant.ComponentHelper.getDefinition(ComponentHelper.java:260)

at org.apache.tools.ant.ComponentHelper.getComponentClass(ComponentHelper.java:250)

at org.apache.tools.ant.ComponentHelper.createNewTask(ComponentHelper.java:486)

at org.apache.tools.ant.ComponentHelper.createTask(ComponentHelper.java:462)

at org.apache.tools.ant.Project.createTask(Project.java:1119)

at org.apache.jasper.compiler.AntCompiler.generateClass(AntCompiler.java:134)

at org.apache.jasper.compiler.Compiler.compile(Compiler.java:298)

at org.apache.jasper.compiler.Compiler.compile(Compiler.java:277)

at org.apache.jasper.compiler.Compiler.compile(Compiler.java:265)

at org.apache.jasper.JspCompilationContext.compile(JspCompilationContext.java:564)

at org.apache.jasper.servlet.JspServletWrapper.service(JspServletWrapper.java:299)

at org.apache.jasper.servlet.JspServlet.serviceJspFile(JspServlet.java:315)

at org.apache.jasper.servlet.JspServlet.service(JspServlet.java:265)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:820)

at org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:511)

at org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:401)

at org.mortbay.jetty.security.SecurityHandler.handle(SecurityHandler.java:216)

at org.mortbay.jetty.servlet.SessionHandler.handle(SessionHandler.java:182)

at org.mortbay.jetty.handler.ContextHandler.handle(ContextHandler.java:766)

at org.mortbay.jetty.webapp.WebAppContext.handle(WebAppContext.java:450)

at org.mortbay.jetty.servlet.Dispatcher.forward(Dispatcher.java:327)

at org.mortbay.jetty.servlet.Dispatcher.forward(Dispatcher.java:126)

at org.mortbay.jetty.servlet.DefaultServlet.doGet(DefaultServlet.java:503)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:707)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:820)

at org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:511)

at org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:401)

at org.mortbay.jetty.security.SecurityHandler.handle(SecurityHandler.java:216)

at org.mortbay.jetty.servlet.SessionHandler.handle(SessionHandler.java:182)

at org.mortbay.jetty.handler.ContextHandler.handle(ContextHandler.java:766)

at org.mortbay.jetty.webapp.WebAppContext.handle(WebAppContext.java:450)

at org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)

at org.mortbay.jetty.handler.RequestLogHandler.handle(RequestLogHandler.java:49)

at org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)

at org.mortbay.jetty.Server.handle(Server.java:326)

at org.mortbay.jetty.HttpConnection.handleRequest(HttpConnection.java:542)

at org.mortbay.jetty.HttpConnection$RequestHandler.headerComplete(HttpConnection.java:928)

at org.mortbay.jetty.HttpParser.parseNext(HttpParser.java:549)

at org.mortbay.jetty.HttpParser.parseAvailable(HttpParser.java:212)

at org.mortbay.jetty.HttpConnection.handle(HttpConnection.java:404)

at org.mortbay.jetty.bio.SocketConnector$Connection.run(SocketConnector.java:228)

at org.mortbay.thread.QueuedThreadPool$PoolThread.run(QueuedThreadPool.java:582)

14/06/11 13:04:41 ERROR mortbay.log: Nested in The following error occurred while executing this line:

jar:file:/usr/local/ant/lib/ant.jar!/org/apache/tools/ant/antlib.xml:37: Problem: failed to create task or type componentdef

Cause: The name is undefined.

Action: Check the spelling.

Action: Check that any custom tasks/types have been declared.

Action: Check that any <presetdef>/<macrodef> declarations have taken place.

:

jar:file:/usr/local/ant/lib/ant.jar!/org/apache/tools/ant/antlib.xml:37: Problem: failed to create task or type componentdef

Cause: The name is undefined.

Action: Check the spelling.

Action: Check that any custom tasks/types have been declared.

Action: Check that any <presetdef>/<macrodef> declarations have taken place.

at org.apache.tools.ant.UnknownElement.getNotFoundException(UnknownElement.java:484)

at org.apache.tools.ant.UnknownElement.makeObject(UnknownElement.java:416)

at org.apache.tools.ant.UnknownElement.maybeConfigure(UnknownElement.java:160)

at org.apache.tools.ant.taskdefs.Antlib.execute(Antlib.java:146)

at org.apache.tools.ant.taskdefs.Definer.loadAntlib(Definer.java:432)

at org.apache.tools.ant.taskdefs.Definer.execute(Definer.java:281)

at org.apache.tools.ant.ComponentHelper.checkNamespace(ComponentHelper.java:790)

at org.apache.tools.ant.ComponentHelper.getDefinition(ComponentHelper.java:260)

at org.apache.tools.ant.ComponentHelper.getComponentClass(ComponentHelper.java:250)

at org.apache.tools.ant.ComponentHelper.createNewTask(ComponentHelper.java:486)

at org.apache.tools.ant.ComponentHelper.createTask(ComponentHelper.java:462)

at org.apache.tools.ant.Project.createTask(Project.java:1119)

at org.apache.jasper.compiler.AntCompiler.generateClass(AntCompiler.java:134)

at org.apache.jasper.compiler.Compiler.compile(Compiler.java:298)

at org.apache.jasper.compiler.Compiler.compile(Compiler.java:277)

at org.apache.jasper.compiler.Compiler.compile(Compiler.java:265)

at org.apache.jasper.JspCompilationContext.compile(JspCompilationContext.java:564)

at org.apache.jasper.servlet.JspServletWrapper.service(JspServletWrapper.java:299)

at org.apache.jasper.servlet.JspServlet.serviceJspFile(JspServlet.java:315)

at org.apache.jasper.servlet.JspServlet.service(JspServlet.java:265)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:820)

at org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:511)

at org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:401)

at org.mortbay.jetty.security.SecurityHandler.handle(SecurityHandler.java:216)

at org.mortbay.jetty.servlet.SessionHandler.handle(SessionHandler.java:182)

at org.mortbay.jetty.handler.ContextHandler.handle(ContextHandler.java:766)

at org.mortbay.jetty.webapp.WebAppContext.handle(WebAppContext.java:450)

at org.mortbay.jetty.servlet.Dispatcher.forward(Dispatcher.java:327)

at org.mortbay.jetty.servlet.Dispatcher.forward(Dispatcher.java:126)

at org.mortbay.jetty.servlet.DefaultServlet.doGet(DefaultServlet.java:503)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:707)

at javax.servlet.http.HttpServlet.service(HttpServlet.java:820)

at org.mortbay.jetty.servlet.ServletHolder.handle(ServletHolder.java:511)

at org.mortbay.jetty.servlet.ServletHandler.handle(ServletHandler.java:401)

at org.mortbay.jetty.security.SecurityHandler.handle(SecurityHandler.java:216)

at org.mortbay.jetty.servlet.SessionHandler.handle(SessionHandler.java:182)

at org.mortbay.jetty.handler.ContextHandler.handle(ContextHandler.java:766)

at org.mortbay.jetty.webapp.WebAppContext.handle(WebAppContext.java:450)

at org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)

at org.mortbay.jetty.handler.RequestLogHandler.handle(RequestLogHandler.java:49)

at org.mortbay.jetty.handler.HandlerWrapper.handle(HandlerWrapper.java:152)

at org.mortbay.jetty.Server.handle(Server.java:326)

at org.mortbay.jetty.HttpConnection.handleRequest(HttpConnection.java:542)

at org.mortbay.jetty.HttpConnection$RequestHandler.headerComplete(HttpConnection.java:928)

at org.mortbay.jetty.HttpParser.parseNext(HttpParser.java:549)

at org.mortbay.jetty.HttpParser.parseAvailable(HttpParser.java:212)

at org.mortbay.jetty.HttpConnection.handle(HttpConnection.java:404)

at org.mortbay.jetty.bio.SocketConnector$Connection.run(SocketConnector.java:228)

at org.mortbay.thread.QueuedThreadPool$PoolThread.run(QueuedThreadPool.java:582)

在访问http://192.168.29.81:9999/hwi/时，报这种错误，主要是因为拷⻉到$HIVE_HOME/lib中的 ant.jar和ant-lancher.jar的版本与linux安装的ant版本不⼀致引起的。

