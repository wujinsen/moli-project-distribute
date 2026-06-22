# 1. Log4jApender

- 1.1. 使⽤说明


- 1.1.2.Client端Log4j配置⽂件

<table>
  <tr>
    <th>（⻩⾊⽂字为需要配置的内容） log4j.rotLoger=INFO,A1,R<br><br># ConsoleApender out log4j.apender.A=org.apache.log4j.ConsoleApender log4j.apener. .layout=org.apache.log4j.PaternLayout log4j.apender.A1.layout.ConversionPatern=%d{ y/ M/ dH: m:s}%-5p%-10C{1}%m%n # File out<br><br>/⽇志Apender修改为flume提供的Log4jApender log4j.apender. =org.apache.flume.clients.log4japender.Log4jApender log4j.apender.R.File=${catalina.home}/logs/ultraIDCPServer.log<br><br>/⽇志需要发送到的端⼝号，该端⼝要有ARVO类型的source在监听 log4j.apender.R.Port = 4<br><br>/⽇志需要发送到的主机ip，该主机运⾏着ARVO类型的source lo .apene. .Hostname =localhost log4j.apender.R.MaxFileSize=10240KB # log4j.apender.R.MaxBackupIndex=5 log4j.apener. .layout=org.apache.log4j.PaternLayout lo .apender. .layout.ConversionPatern=%d{ y/ M/ dH\: m\:s}%-5p%-10C{1}%m%n log4j.apender.R.encoding=UTF-8<br><br>log4j.loger.com.ultrapower.ultracolector.webservice.MesageIntercomunionInterfaceImpl=INF O,webservice log4j.apender. e erice=org.apache.log4j.FileApender log.apener. eserice.File=${catalina.home}/logs/logsMsgIntercomunionInterface.log log4j.apener. e er ce.layout=org.apache.log4j.PaternLayout log4j.apender.webservice.layout.ConversionPatern=%d{ y/ M/ dH\: m\:s}%-5p[%t]%l% X-%m%n log4j.apender.webservice.encoding=UTF-8<br><br>注：Log4jApender继承⾃ApenderSkeleton，没有⽇志⽂件达到特定⼤⼩，转换到新的⽂件的功 能</th>
  </tr>
</table>


- 1.1.3.flume agent配置


<table>
  <tr>
    <th>e 1sources = source1<br><br>n1sinks = sink1 agent1.chanels = chanel1 # Describe/configure source1 aet1.sources.source1.type avro<br><br>en1.sources.source1.bind = 192.168.0.141 agent1.sources.source1.port = 4 # Describe sink1<br><br>ent1.sinks.sink1.type = FILE_ROL agent1.sinks.sink1.sink.directory = /home/yubojie/flume/apache-flume-1.2.0/flume-out # Use a chanel which bufers events in memory aent1.chanel.chanel1.type = memory aent1.chanel.chanel1.capacity = 1 0 agent1.chanels.chanel1.transactionCapactiy = 10 # Bind the source and sink to the chanel aen1.sources.source1.chanels = chanel1 agent1.sinks.sink1.chanel = chanel1 注：⽣成的⽂件的规则为每隔固定时间间隔⽣成⼀个新的⽂件，⽂件⾥⾯保存该时间段agent接收到 的信息</th>
  </tr>
</table>


- 1.2. 分析
- 1.3. ⽇志代码

<table>
  <tr>
    <th> </th>
  </tr>
</table>


Log.info(“this mesage has DEBUG in it”);

- 1.4. 采集到的数据样例


- 1. 使⽤简便，⼯作量⼩。
- 2. ⽤户应⽤程序使⽤log4j作为⽇志记录jar包，⽽且项⽬中使⽤的jar包要在log4j-1.2.15版本以上，
- 3. 应⽤系统必须将flume所需jar包引⼊到项⽬中。如下所示为所有必须jar包：可能会存在jar冲突， 影响应⽤运⾏
- 4. 能够提供可靠的数据传输，使⽤flume log4jApender采集⽇志可以不在客户机上启动进程，⽽ 只通过修改loga pender直接把⽇志信息发送到采集机（参⻅图⼀），此种情况可以保证采集机接受 到数据之后的数据可靠性，但是客户机与采集机连接失败时候数据会丢失。改进⽅案是在客户机上启 动⼀个agent，这样可以保证客户机和采集机不能连通时，当能连通是⽇志也被采集上来，不会发送数 据的丢失（参⻅图⼆），为了可靠性，需在客户机上启动进程


<table>
  <tr>
    <th>this mesage has DEBUG in it</th>
  </tr>
</table>


this mesage has DEBUG in it

# 2. Execsource（放弃）

The problem with ExecSource and other asynchronous sources is that thesource can not guarante that if there is a failure to put the event into theChanel the client knows about it. In such cases, the data wil be lost. As afor instance, one of the most comonly requested features is thetail -F [file]-like use casewhere an aplication writes to a log file on disk and Flume tails the file,sending each line as an event. While this is posible, thereʼs an obviousproblem; what hapens if the chanel fils up and Flume canʼt send an event?Flume has no way of indicating to the aplication writing the log file that itneds to retain the log or that the event hasnʼt ben sent, for some reason. Ifthis doesnʼt make sense, you ned only know this: Your aplication can neverguarante data has ben received when using a unidirectional asynchronousinterface such as ExecSource! As an extension of this warning - and to becompletely clear - there is absolutely zero guarante of event delivery whenusing this source. You have ben warned. 注：即使是agent内部的可靠性都不能保证

- 2.1. 使⽤说明


- 2.1.1.flume agent配置


# The configuration file neds to define the sources, # the chanels and the sinks. # Sources, chanels and sinks are defined per agent, # in this case caled 'agent' # example.conf: A single-node Flume configuration

# Name the components on this agent

agent1.sources = source1 agent1.sinks = sink1 agent1.chanels = chanel1 # Describe/configure source1 #agent1.sources.source1.type = avro agent1.sources.source1.type = exec agent1.sources.source1.comand = tail -f /home/yubojie/logs/ultraIDCPServer.log #agent1.sources.source1.bind = 192.168.0.146 #agent1.sources.source1.port = 4

agent1.sources.source1.interceptors = a agent1.sources.source1.interceptors.a.type = org.apache.flume.interceptor.HostInterceptor$Builder agent1.sources.source1.interceptors.a.preserveExisting = false agent1.sources.source1.interceptors.a.hostHeader = hostname

# Describe sink1 #agent1.sinks.sink1.type = FILE_ROL

#agent1.sinks.sink1.sink.directory = /home/yubojie/flume/apache-flume-

- 1.2.0/flume-out agent1.sinks.sink1.type = hdfs agent1.sinks.sink1.hdfs.path = hdfs:/localhost:9 0/user/ agent1.sinks.sink1.hdfs.fileType = DataStream

# Use a chanel which bufers events in memory agent1.chanels.chanel1.type = memory agent1.chanels.chanel1.capacity = 1 0 agent1.chanels.chanel1.transactionCapactiy = 10

# Bind the source and sink to the chanel agent1.sources.source1.chanels = chanel1 agent1.sinks.sink1.chanel = chanel1

- 2.2. 分析
- 2.3. 采集到的数据样例

<table>
  <tr>
    <th>2012/10/26 02 36 34 INFO LogTest this mesage has DEBUG 中⽂ in it</th>
  </tr>
</table>


2012/10/26 02 40 12 INFO LogTest this mesage has DEBUG 中⽂ in it

- 2.4. ⽇志代码


- 1. tail⽅式采集⽇志需要宿主主机能够执⾏tail命令，应该是只有linux系统可以执⾏，不⽀持 window系统⽇志采集
- 2. EXEC采⽤异步⽅式采集，会发⽣⽇志丢失，即使在节点内的数据也不能保证数据的完整
- 3. tail⽅式采集需要宿主操作系统⽀持tail命令，即原始的windows操作系统不⽀持tail命令采集


<table>
  <tr>
    <th> </th>
  </tr>
</table>


Log.info(“this mesage has DEBUG 中⽂ in it”);

# 3.Syslog

Pasing mesages using syslogprotocol doesn't work wel for longer mesages. The syslog apender forLog4j is hardcoded to linewrap around 1024 characters in order to comply withthe RFC. I got a sample program loging to syslog, picking it up with asyslogUdp source, with a JSON layout (to avoid new-lines in stack traces) onlyto find that anything but the smalest stack trace line-wraped anyway. Ican't se a way to reliably reconstruct the stack trace once it is wraped andsent through the flume chain.（注：内容不确定是否1.2版本）

Syslog TCP需要指定eventsize，默认为250 Syslog UDP为不可靠传输，数据传输过程中可能出现丢失数据的情况。

## 3.1. 使⽤说明

- 3.1.1.Client端示例代码


<table>
  <tr>
    <th>i pot java.io.IOException; importjava.io.OutputStream;<br><br>otaa.et.Socket; import java.net.UnknownHostException;<br><br>publi clas SyslogTcp {<br><br>publicstaticvoid main(String args[]){ Socket cli nt = nul; OutputStream out =nul;<br><br>try { client = new Socket("127.0.0.1", 5140); out= client.getOutputStream();<br><br>String event = "<4>helo\n";<br><br>t.write(event.getBytes(); out.flush(); System.out.println("发送成功 "); } catch (UnknownHostException e) {<br><br>/TODO Auto-generated catch block e.printStackTrace();<br><br>} catch (IOException e) {<br><br>/TODO Auto-generated catch block e.printStackTrace();<br><br>} finaly{ try {<br><br>out.close(); } catch (IOException e) {<br><br>/TODO Auto-generated catch block e.printStackTrace();<br><br>} try { client.close(); } catch (IOException e) {<br><br>/TODO Auto-generated catch block e.printStackTrace();<br><br>} }<br><br>} }</th>
  </tr>
</table>


### 3.1.2.⽇志接收的flume agent配置

<table>
  <tr>
    <th>e 1sources = source1<br><br>n1sinks = sink1 agent1.chanels = chanel1 # Describe/configure source1<br><br>get1.sources.source1.type syslogtcp en1.sources.source1.bind = 127.0.0.1 agent1.sources.source1.port = 5140<br><br>Describe sink1 aent1.sinks.sink1.type =avro aen1.sinks.sink1.chanels = chanel1 aent1.sinks.sink1.hstname = 192.168.0.14<br><br>#agent1.sinks.sink1.port = 4 ent1.sinks.sink1.type = FILE_ROL agent1.sinks.sink1.sink.directory = E:\file-out<br><br># Use a chanel which bufers events in memory aent1.chanel.chanel1.type = memory aent1.chanel.chanel1.capacity = 1 0 agent1.chanels.chanel1.transactionCapactiy = 10<br><br># Bind the source and sink to the chanel agent1.sources.source1.chanels = chanel1</th>
  </tr>
</table>


agent1.sinks.sink1.chanel = chanel1

- 3.2. 分析


需要编写Client采集代码，增量采集⽇志信息通过socket发送到flume agent；对于⻓数据处理不是很 理想。可靠性可以参考log4j apender的⽅式来保证。

# 4. ⽇志过滤Interceptor（FLUME-1358）

Flume⽀持依据正则表达式过滤event，但是在1.2.0的源代码中没有发现具体实现的代码，根据 FLUME-1358的说明信息，可以将RegexFilteringInterceptor类加⼊到代码中使⽤。 需要的操作为： 添加类RegexFilteringInterceptor 修改InterceptorType，添加type与类的映射关系： REGEX_FILTER(org.apache.flume.interceptor.RegexFilteringInterceptor.Builder.clas)

- 4.1. Regex FilteringInterceptor说明


This interceptor filters events selectively by interpreting the eventbody as text and matching the text against a configured regular expresion. Thesuplied regular expresion can be used to include events or exclude events.

<table>
  <tr>
    <th>Property Name</th>
    <th>Default</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>type</td>
    <td>–</td>
    <td>The component type name has<br><br></td>
  </tr>
  <tr>
    <td>regex</td>
    <td>”.*”</td>
    <td>to be REGEX_FILTER Regular expresion for</td>
  </tr>
  <tr>
    <td>excludeRegex</td>
    <td>false</td>
    <td>matching against events If true, regex determines events to exclude, otherwise regex determines events to</td>
  </tr>
</table>


include.

## 4.2. 使⽤说明（测试配置）

- 4.2.1.⽇志接收的Flume agent配置


<table>
  <tr>
    <th>e 1sources = source1<br><br>n1sinks = sink1 agent1.chanels = chanel1 # Describe/configure source1 aet1.sources.source1.type avro aent1.sources.source1.bind =localhost agent1.sources.source1.port = 5140<br><br>ent1.sources.source1.intercetors = inter1 ent1.sources.source1.interceptors.inter1.type = REGEX_FILTER<br><br>gent1.sources.source1.intercetors.inter1.regex = .*DEBUG.* agent1.sources.source1.interceptors.inter1.excludeRegex = false<br><br>Describe sink1 aent1.sinks.sink1.type =avro aen1.sinks.sink1.chanels = chanel1 aent1.sinks.sink1.hstname = 192.168.0.14<br><br>#agent1.sinks.sink1.port = 4 ent1.sinks.sink1.type = FILE_ROL agent1.sinks.sink1.sink.directory = E:\file-out<br><br># Use a chanel which bufers events in memory aent1.chanel.chanel1.type = memory aent1.chanel.chanel1.capacity = 1 0 agent1.chanels.chanel1.transactionCapactiy = 10<br><br># Bind the source and sink to the chanel aen1.sources.source1.chanels = chanel1</th>
  </tr>
</table>


agent1.sinks.sink1.chanel = chanel1

# 5. HDFSSINK

- 5.1. 使⽤说明


输出到hdfs的数据，⾸先在hdfs上创建⽂件.tmp,然后⽂件关闭时，将tmp后缀去掉，存储⽅案与file输 出类似，可以设定时间间隔、⽂件⼤⼩、接受事件条数作为滚动⽣成新⽂件的依据，默认30s

- 5.2. 可配置项


<table>
  <tr>
    <th>Name</th>
    <th>Default</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>chanel</td>
    <td>–</td>
    <td> </td>
  </tr>
  <tr>
    <td>type</td>
    <td>–</td>
    <td>The component type name,<br><br></td>
  </tr>
  <tr>
    <td>hdfs.path</td>
    <td>–</td>
    <td>neds to be hdfs HDFS directory path (eg hdfs:/namenode/flume/webdat</td>
  </tr>
  <tr>
    <td>hdfs.filePrefix</td>
    <td>FlumeData</td>
    <td>a/) Name prefixed to files created</td>
  </tr>
  <tr>
    <td>hdfs.rolInterval</td>
    <td>30</td>
    <td>by Flume in hdfs directory Number of seconds to wait before roling curent file (0 = never rol based on time</td>
  </tr>
  <tr>
    <td>hdfs.rolSize</td>
    <td>1024</td>
    <td>interval) File size to tri ger rol, in bytes</td>
  </tr>
  <tr>
    <td>hdfs.rolCount</td>
    <td>10</td>
    <td>(0: never rol based on file size)<br><br>Number of events writen to file efore it roled (0 = never rol based on number of</td>
  </tr>
  <tr>
    <td>hdfs.batchSize</td>
    <td>1</td>
    <td>events) number of events writen to file</td>
  </tr>
  <tr>
    <td>hdfs.txnEventMax</td>
    <td>10</td>
    <td>before it flushed to HDFS</td>
  </tr>
  <tr>
    <td>hdfs.codeC</td>
    <td>–</td>
    <td>Compresion codec. one of folowing : gzip, bzip2, lzo,</td>
  </tr>
  <tr>
    <td>hdfs.fileType</td>
    <td>SequenceFile</td>
    <td>snapy File format: curentlySequenceFile,DataStr eam orCompresedStream(1)D ataStream wil not compres output file and please donʼt set codeC (2)CompresedStream requires set hdfs.codeC with<br><br></td>
  </tr>
  <tr>
    <td>hdfs.maxOpenFiles</td>
    <td>5 0</td>
    <td>an available codeC</td>
  </tr>
  <tr>
    <td>hdfs.writeFormat</td>
    <td>–</td>
    <td>“Text” or “Writable”</td>
  </tr>
  <tr>
    <td>hdfs.apendTimeout</td>
    <td>1 0</td>
    <td> </td>
  </tr>
  <tr>
    <td>hdfs.calTimeout</td>
    <td>1 0</td>
    <td> </td>
  </tr>
  <tr>
    <td>hdfs.threadsPolSize</td>
    <td>10</td>
    <td>Number of threads per HDFS sink for HDFS IO ops (open,</td>
  </tr>
</table>


#### write, etc.)

<table>
  <tr>
    <th>hdfs.rolTimerPolSize</th>
    <th>1</th>
    <th>Number of threads per HDFS sink for scheduling timed file</th>
  </tr>
  <tr>
    <td>hdfs.kerberosPrincipal</td>
    <td>–</td>
    <td>roling Kerberos user principal for</td>
  </tr>
  <tr>
    <td>hdfs.kerberosKeytab</td>
    <td>–</td>
    <td>acesing secure HDFS Kerberos keytab for acesing</td>
  </tr>
  <tr>
    <td>hdfs.round</td>
    <td>false</td>
    <td>secure HDFS Should the timestamp be rounded down (if true, afects al time based escape</td>
  </tr>
  <tr>
    <td>hdfs.roundValue</td>
    <td>1</td>
    <td>sequences except %t) Rounded down to the highest multiple of this (in the unit configured usinghdfs.roundUnit), les than<br><br></td>
  </tr>
  <tr>
    <td>hdfs.roundUnit</td>
    <td>second</td>
    <td>curent time. The unit of the round down<br><br></td>
  </tr>
  <tr>
    <td>serializer</td>
    <td>TEXT</td>
    <td>value - second,minute orhour. Other posible options includeAVRO_EVENT or the fuly-qualified clas name of an implementation of theEventSerializer.Builder interf<br><br></td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td>ace.</td>
  </tr>
</table>


serializer.*

## 5.3. Agent配置样例

# The configuration file neds to define the sources, # the chanels and the sinks. # Sources, chanels and sinks are defined per agent, # in this case caled 'agent' # example.conf: A single-node Flume configuration

# Name the components on this agent

agent1.sources = source1 agent1.sinks = sink1 agent1.chanels = chanel1 # Describe/configure source1 #agent1.sources.source1.type = avro agent1.sources.source1.type = exec agent1.sources.source1.comand = tail -f /home/yubojie/logs/ultraIDCPServer.log #agent1.sources.source1.bind = 192.168.0.146 #agent1.sources.source1.port = 4

agent1.sources.source1.interceptors = a agent1.sources.source1.interceptors.a.type = org.apache.flume.interceptor.HostInterceptor$Builder agent1.sources.source1.interceptors.a.preserveExisting = false agent1.sources.source1.interceptors.a.hostHeader = hostname

# Describe sink1 #agent1.sinks.sink1.type = FILE_ROL

#agent1.sinks.sink1.sink.directory = /home/yubojie/flume/apache-flume1.2.0/flume-out

agent1.sinks.sink1.type = hdfs agent1.sinks.sink1.hdfs.path = hdfs:/192.168.98.20 9 0/user/hadop/yubojietest agent1.sinks.sink1.hdfs.fileType = DataStream

# Use a chanel which bufers events in memory agent1.chanels.chanel1.type = memory agent1.chanels.chanel1.capacity = 1 0 agent1.chanels.chanel1.transactionCapactiy = 10

# Bind the source and sink to the chanel agent1.sources.source1.chanels = chanel1

agent1.sinks.sink1.chanel = chanel1

# 6. 多agent采集⽂件到hdfs

- 6.1. 准备⼯作


- 1. ⽂件采集类打包成jar放到flume/apache-flume-1.2.0/lib⽬录下
- 2. 创建fileSourceRecorder.properties空⽂件放到flume/apache-flume-1.2.0/conf下（将要修改为 如果⽂件不存在则创建该⽂件，后续将不⽤再创建这个⽂件）


- 6.2. agent配置⽂件


- 6.2.1.agent1


e 1sources = source1

n1sinks = sink1 agent1.chanels = chanel1 # Describe/configure source1 agent1.sources.source1.type = com.ultrapower.ultracolector.flume.source.file.FileSource agent1.sources.source1.path = /home/yubojie/logs/ultraIDCPServer.log #bk,utf-8

gent1.sources.source1.encoding = utf-8 aent1.sources.source1.onceMaxReadByte = 9 ae 1.sources.source1.cacheQueueSize = 10 agen1.sources.source1.noChangeSl epTime = 1 0 aet1.sources.source1. atcComitSize = 5 agent1.sources.source1.batchWaitTime = 50

aet1.sources.source1.type avro aent1.sources.source1.bind = localhost

#agent1.sources.source1.port = 4 Describe sink1

gent1.sinks.sink1.t e loger ent1.sinks.sink1.type = FILE_ROL ent1.sinks.sink1.sink.directory E:/file-out

#agent1.sinks.sink1.sink.fileName = a.log agent1.sinks.sink1.type = hdfs #agent1.sinks.sink1.hdfs.path = hdfs:/192.168.98.20 9 0/user/hadop/yubojietest aent1.sinks.sink1.hdfs.path = hdfs:/192.168.0.153 9 0/user/file aent1.sinks.sink1. s.calTimeout = 2 0 agent1.sinks.sink1.hdfs.fileType = DataStream #agent1.sinks.sink1.sink.rolInterval = 30

# Use a chanel which bufers events in memory aent1.chanel.chanel1.type = memory aent1.chanel.chanel1.capacity = 1 0 agent1.chanels.chanel1.transactionCapactiy = 10

# Bind the source and sink to the chanel aen1.sources.source1.chanels = chanel1

- agent1.sinks.sink1.chanel = chanel1


### 6.2.2.agent2

e sources = source1 n sinks = sink1

- agent2.chanels = chanel1


# Describe/configure source1 agent2.sources.source1.type = com.ultrapower.ultracolector.flume.source.file.FileSource

agent2.sources.source1.path = /home/yubojie/logtest/logs/ultraIDCPServer.log #bk,utf-8

gent.sources.source.encoding = utf-8 aent.sources.source.onceMaxReadByte = 9 ae .sources.source1.cacheQueueSize = 10 agen .sources.source1.noChangeSl epTime = 1 0 aet.sources.source. atcComitSize = 5 agent2.sources.source1.batchWaitTime = 50

aet1.sources.source1.type avro

- aent1.sources.source1.bind = localhost

#agent1.sources.source1.port = 4 Describe sink1

gent1.sinks.sink1.t e loger ent1.sinks.sink1.type = FILE_ROL ent1.sinks.sink1.sink.directory E:/file-out

#agent1.sinks.sink1.sink.fileName = a.log agent2.sinks.sink1.type = hdfs #agent1.sinks.sink1.hdfs.path = hdfs:/192.168.98.20 9 0/user/hadop/yubojietest

- aent2.sinks.sink1.hdfs.path = hdfs:/192.168.0.153 9 0/user/file aent2.sinks.sink. s.calTimeout = 2 0 agent2.sinks.sink1.hdfs.fileType = DataStream #agent1.sinks.sink1.sink.rolInterval = 30


# Use a chanel which bufers events in memory aent.chanel.chanel.type = memory aent.chanel.chanel1.capacity = 1 0 agent2.chanels.chanel1.transactionCapactiy = 10

# Bind the source and sink to the chanel aen .sources.source1.chanels = chanel1 agent2.sinks.sink1.chanel = chanel1

- 6.3. 启动命令

<table>
  <tr>
    <th>flume-ng agent -name agent1 -c conf -f ./conf/flume-conf.properties /agent1监控/home/yubojie/logs/ultraIDCPServer.log<br>flume-ng agent -name agent2 -c conf -f ./conf/flume-conf2.properties<br></th>
  </tr>
</table>


/agent2监控/home/yubojie/logtest/logs/ultraIDCPServer.log

- 6.4. 测试结果


- 1. agent1和agent2各⾃监控相应⽂件，互不⼲涉
- 2. ⽂件各⾃输出到hdfs⽣成各⾃的⽂件


# 6. 参考资料：

资料

<table>
  <tr>
    <th>⽇志采集<br><br>htps:/isues.cloudera.org/browse/FLUME-27 htp:/archive.cloudera.com/cdh/3/flume-ng-1.2.0-cdh3u5/FlumeUserGuide.html#execsource htp:/ w.quora.com/Flume/What-Flume-sources-do-people-use-in-production htp:/blog.csdn.net/rzhzhz/article/details/7610252 过滤：htps:/isues.apache.org/jira/secure/atachment/12537520/FLUME-1358.patch.v 4.txt htps:/isues.apache.org/jira/browse/FLUME-1358</th>
  </tr>
</table>


RegexFilteringInterceptor源代码

packageorg.apache.flume.interceptor; importstaticorg.apache.flume.interceptor.RegexFilteringInterceptor.Constants.DEFAULT_EXCLU DE_EVENTS; importstatic org.apache.flume.interceptor.RegexFilteringInterceptor.Constants.DEFAULT_REGEX ; importstatic org.apache.flume.interceptor.RegexFilteringInterceptor.Constants.EXCLUDE_EVENT S; importstatic org.apache.flume.interceptor.RegexFilteringInterceptor.Constants.REGEX; i taa.ti.List; import java.util.regex.Patern;

mportor.apa e. me.Context; mportor.apache.flume.Event;

or org.slf4j.Loger; importorg.slf4j.LogerFactory; import com.gogle.comon.colect.Lists; publi clas RegexFilteringInterceptorimplements Interceptor {

privatestaticfinal Logerloger =LogerFactory .getLoger(RegexFilteringInterceptor.clas);

riateina Paternregex; privatefinalboleanexcludeEvents; /*

Only{@link RegexFilteringInterceptor.Builder}canbuildme

*/ private RegexFilteringInterceptor(Patern regex,bolean excludeEvents) { regex = regex; this.excludeEvents = excludeEvents;

} @Overide publicvoid initialize() {

/ no-op }

@Overide /*

Returnstheventifitpasestheregularexpresionfilterandnul otherwise.

*/

public Event intercept(Event event) { / We've already ensured here that at most one of includeRegex and / excludeRegex are defined.

if (!excludeEvents) { if (regex.matcher(new String(event.getBody( ).find() { return event;

} else {

returnul; }

} else {

if (regex.matcher(new String(event.getBody( ).find() { returnul;

} else {

return event; }

}

} /*

Returnsthesetofeventswhichpasfilters,acordingto {@link #intercept(Event)}. @paramevents @return

*/ @Overide public List<Event> intercept(List<Event> events) {

List<Event> out = Lists.newArayList(); for (Event event : events) {

Event outEvent = intercept(event); if (outEvent !=nul) { out.ad(outEvent); }

} return out;

} @Overide publicvoid close() {

/ no-op

} /*

BuilderwhichbuildsnewinstanceoftheStaticInterceptor.

*/ publicstati clas Builderimplements Interceptor.Builder {

r ate Paternregex; privateboleanexcludeEvents; @Overide publicvoid configure(Context context) {

String regexString = context.getString(REGEX,DEFAULT_REGEX); regex = Patern.compile(regexString); excludeEvents = context.getBolean(EXCLUDE_EVENTS,

DEFAULT_EXCLUDE_EVENTS);

} @Overide public Interceptor build() {

loger.info(String.format( "Creating RegexFilteringInterceptor: regex=%s,excludeEvents=%s", regex,excludeEvents);

returnew RegexFilteringInterceptor(regex,excludeEvents); }

} publicstati clas Constants {

publicstaticfinal StringREGEX ="regex";

lictaticinal tinDEFALT_REGEX =".*";

ulicstaticinal StringEXCLUDE_EVENTS ="excludeEvents"; publicstaticfinalboleanDEFAULT_EXCLUDE_EVENTS = false; }

}

### InterceptorType源代码

<table>
  <tr>
    <th>⻩⾊为添加内容 package org.apache.flume.interceptor;<br><br>public enum InterceptorType {<br><br>TIMESTAMP(org.apache.flume.interceptor.TimestampInterceptor.Builder.clas), HOST(org.apache.flume.interceptor.HostInterceptor.Builder.clas), REGEX_FILTER(org.apache.flume.interceptor.RegexFilteringInterceptor.Builder.clas), ;<br><br>private final Clas<? extends Interceptor.Builder> builderClas; private InterceptorType(Clas<? extends Interceptor.Builder> builderClas) {<br><br>this.builderClas = builderClas;<br><br>} public Clas<? extends Interceptor.Builder> getBuilderClas() {<br><br>return builderClas; }</th>
  </tr>
</table>


}

