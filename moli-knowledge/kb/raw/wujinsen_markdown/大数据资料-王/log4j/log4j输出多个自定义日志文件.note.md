log4j的强⼤功能⽆可置疑，但实际应⽤中免不了遇到某个功能需要输出独⽴的⽇志⽂件的情况，怎样 才能把所需的内容从原有⽇志中分离，形成单独的⽇志⽂件呢？其实只要在现有的log4j基础上稍加配 置即可轻松实现这⼀功能。

先看⼀个常见的log4j.properties⽂件，它是在控制台和myweb.log⽂件中记录⽇志： log4j.rootLogger=DEBUG, stdout, logﬁle log4j.category.org.springframework=ERROR log4j.category.org.apache=INFO log4j.appender.stdout=org.apache.log4j.ConsoleAppender log4j.appender.stdout.layout=org.apache.log4j.PatternLayout log4j.appender.stdout.layout.ConversionPattern=%d %p [%c]- %m%n log4j.appender.logﬁle=org.apache.log4j.RollingFileAppender log4j.appender.logﬁle.File=${myweb.root}/WEB-INF/log/myweb.log log4j.appender.logﬁle.MaxFileSize=512KB log4j.appender.logﬁle.MaxBackupIndex=5 log4j.appender.logﬁle.layout=org.apache.log4j.PatternLayout log4j.appender.logﬁle.layout.ConversionPattern=%d %p[%c] - %m%n

如果想对不同的类输出不同的⽂件(以cn.com.Test为例)，先要在Test.java中定义: private static Log logger = LogFactory.getLog(Test.class);

然后在log4j.properties中加⼊: log4j.logger.cn.com.Test=DEBUG, test log4j.appender.test=org.apache.log4j.FileAppender log4j.appender.test.File=${myweb.root}/WEB-INF/log/test.log log4j.appender.test.layout=org.apache.log4j.PatternLayout log4j.appender.test.layout.ConversionPattern=%d%p [%c] - %m%n

也就是让cn.com.Test中的logger使⽤log4j.appender.test所做的配置。 但是，如果在同⼀类中需要输出多个⽇志⽂件呢？其实道理是⼀样的，先在Test.java中定义:

- private static Log logger1 = LogFactory.getLog("myTest1");
- private static Log logger2 = LogFactory.getLog("myTest2"); 然后在log4j.properties中加⼊:


- log4j.logger.myTest1=DEBUG, test1

- log4j.appender.test1=org.apache.log4j.FileAppender


- log4j.appender.test1.File=${myweb.root}/WEB-INF/log/test1.log


- log4j.appender.test1.layout=org.apache.log4j.PatternLayout


- log4j.appender.test1.layout.ConversionPattern=%d%p [%c] - %m%n


- log4j.logger.myTest2=DEBUG, test2


- log4j.appender.test2=org.apache.log4j.FileAppender


- log4j.appender.test2.File=${myweb.root}/WEB-INF/log/test2.log


- log4j.appender.test2.layout=org.apache.log4j.PatternLayout


- log4j.appender.test2.layout.ConversionPattern=%d%p [%c] - %m%n 也就是在⽤logger时给它⼀个⾃定义的名字(如这⾥的"myTest1")，然后在log4j.properties中做出相应


配置即可。别忘了不同⽇志要使⽤不同的logger(如输出到test1.log的要⽤logger1.info("abc"))。

还有⼀个问题，就是这些⾃定义的⽇志默认是同时输出到log4j.rootLogger所配置的⽇志中的，如何 能只让它们输出到⾃⼰指定的⽇志中呢？别急，这⾥有个开关： log4j.additivity.myTest1= false

它⽤来设置是否同时输出到log4j.rootLogger所配置的⽇志中，设为false就不会输出到其它地⽅啦！ 注意这⾥的"myTest1"是你在程序中给logger起的那个⾃定义的名字！ 如果你说，我只是不想同时输出这个⽇志到log4j.rootLogger所配置的logﬁle中，stdout⾥我还想同时输出 呢！那也好办，把你的log4j.logger.myTest1= DEBUG, test1改为下式就OK啦！ log4j.logger.myTest1=DEBUG, test1, stdout

--------------------------------------------------------------------------------------------------------------------------------------

log4j使⽤⽰例--by blues(zhaochaohua@sina.com)PART 1 介绍log4j的好处在于：1.通过修改配置⽂件，就 可以决定log信息输出到何处(console,⽂件,...),是否输出。这样，在系统开发阶段可以打印详细的log信息 以跟踪系统运⾏情况,⽽在系统稳定后可以关闭log输出,从⽽在能跟踪系统运⾏情况的同时,又减少了垃 圾代码(System.out.println(...)等)。2.使⽤log4j，需要整个系统有⼀个统⼀的log机制，有利于系统的规 划。log4j的使⽤本⾝很简单。但合理地规划⼀个系统的统⼀log机制需要周全的考虑。其他关于log4j的 信息参看log4j⾃带的⽂档。PART II 配置⽂件详细解释先看⼀个配置⽂件的例⼦:1.配置⽂件的例⼦ log4j.rootLogger=DEBUG#将DAO层log记录到DAOLog,allLog中log4j.logger.DAO=DEBUG,A2,A4#将逻 辑层log记录到BusinessLog,allLog中log4j.logger.Businesslog=DEBUG,A3,A4#A1--打印到屏幕上

- log4j.appender.A1=org.apache.log4j.ConsoleAppenderlog4j.appender.A1.layout=org.apache.log4j.PatternLayo utlog4j.appender.A1.layout.ConversionPattern=%-5p [%t] %37c %3x - %m%n#A2--打印到⽂件DAOLog中-专门为DAO层服务
- log4j.appender.A2=org.apache.log4j.DailyRollingFileAppenderlog4j.appender.A2.ﬁle=DAOLoglog4j.appende r.A2.DatePattern='.'yyyy-MM-

- ddlog4j.appender.A2.layout=org.apache.log4j.PatternLayoutlog4j.appender.A2.layout.ConversionPattern=

- [%-5p] %d{yyyy-MM-dd HH:mm:ss,SSS}method:%l%n%m%n#A3--打印到⽂件BusinessLog中--专门记录逻 辑处理层服务log信息

log4j.appender.A3=org.apache.log4j.DailyRollingFileAppenderlog4j.appender.A3.ﬁle=BusinessLoglog4j.appen der.A3.DatePattern='.'yyyy-MMddlog4j.appender.A3.layout=org.apache.log4j.PatternLayoutlog4j.appender.A3.layout.ConversionPattern=

- [%-5p] %d{yyyy-MM-dd HH:mm:ss,SSS}method:%l%n%m%n#A4--打印到⽂件alllog中--记录所有log信息


- ddlog4j.appender.A4.layout=org.apache.log4j.PatternLayoutlog4j.appender.A4.layout.ConversionPattern= [%-5p] %d{yyyy-MM-dd HH:mm:ss,SSS}method:%l%n%m%n2.Appender的使⽤⼀个Appender代表log信息 要写向的⼀个地⽅。log4j可使⽤的Appender有很多类型,这⾥只考虑3 种:ConsoleAppender,FileAppender,DailyRollFileAppender2.1 ConsoleAppender如果使⽤ConsoleAppender， 那么log信息将写到Console。就是直接把信息打印到System.out上了。2.2 FileAppender使⽤ FileAppender，那么log信息将写到指定的⽂件中。这应该是⽐较经常使⽤到的情况。相应地，在配置⽂ 件中应该指定log输出的⽂件名。如下配置指定了log⽂件名为demo.txtlog4j.appender.A2.File=demo.txt注 意将A2替换为具体配置中Appender的别名。2.3 DailyRollingAppender使⽤FileAppender可以将log信息输 出到⽂件中，但是如果⽂件太⼤了读起来就不⽅便了。这时就可以使⽤DailyRollingAppender。 DailyRollingAppender可以把Log信息输出到按照⽇期来区分的⽂件中。如下配置⽂件就会每天产⽣⼀个 log⽂件，每个log⽂件只记录当天的log信息： log4j.appender.A2=org.apache.log4j.DailyRollingFileAppenderlog4j.appender.A2.ﬁle=demolog4j.appender.A2


- log4j.appender.A4=org.apache.log4j.DailyRollingFileAppenderlog4j.appender.A4.ﬁle=allloglog4j.appender.A4


.DatePattern='.'yyyy-MM-

.DatePattern='.'yyyy-MMddlog4j.appender.A2.layout=org.apache.log4j.PatternLayoutlog4j.appender.A2.layout.ConversionPattern=%m %n3.Layout的配置Layout指定了log信息输出的样式。详细信息请查看PatternLayout的javadoc。例⼦1：

显⽰⽇期和log信息 log4j.appender.A2.layout=org.apache.log4j.PatternLayoutlog4j.appender.A2.layout.ConversionPattern=%d{yy yy-MM-dd HH:mm:ss,SSS} %m%n打印的信息是：2002-11-12 11:49:42,866 SELECT * FROM Role WHERE 1=1 order by createDate desc例⼦2：显⽰⽇期，log发⽣地⽅和log信息 log4j.appender.A2.layout=org.apache.log4j.PatternLayoutlog4j.appender.A2.layout.ConversionPattern=%d{yy yy-MM-dd HH:mm:ss,SSS} %l"#" %m%n2002-11-12 11:51:46,313cn.net.unet.weboa.system.dao.RoleDAO.select(RoleDAO.java:409) "#"SELECT * FROM Role WHERE 1=1 order by createDate desc 例⼦3：显⽰log级别,时间,调⽤⽅法,log信息 log4j.appender.A2.layout=org.apache.log4j.PatternLayoutlog4j.appender.A2.layout.ConversionPattern=[%-5p] %d{yyyy-MM-dd HH:mm:ss,SSS}method:%l%n%m%nlog信息:[DEBUG] 2002-11-12 12:00:57,376 method:cn.net.unet.weboa.system.dao.RoleDAO.select(RoleDAO.java:409)SELECT * FROM Role WHERE 1=1 order by createDate desc PART 3 log4j的使⽤log4j使⽤步骤有3个：3.1.根据配置⽂件初始化log4j配置 ⽂件如PART 2所叙述。现在讲的是如何在程序中配置log4j。log4j可以使⽤3中配置器来初始化： BasicConﬁgurator,DOMConﬁgurator,PropertyConﬁgurator这⾥⽤的是PropertyConﬁgurator。使⽤ PropertyConﬁgurator适⽤于所有的系统。如下的语句PropertyConﬁgurator.conﬁgure("log4j.properties");就 以log4j.properties为配置⽂件初始化好了log4j环境。注意⼀点：这个语句只需要在系统启动的时候执⾏ ⼀次。例如:在unetwebOA项⽬中可以这么⽤:在ActionServlet的init()⽅法中调⽤⼀次。public class ActionServlet extends HttpServlet{.../*** Initialize global variables*/public void init() throws ServletException {// 初始化Action资源try{initLog4j();...}catch(IOException e){throw new ServletException("Load ActionRes is Error");}}...protected void initLog4j() {PropertyConﬁgurator.conﬁgure("log4j.properties");}...}//end class ActionServlet3.2 在需要使⽤log4j的地⽅ 获取Logger实例如下是RoleDAO类中的使⽤例⼦:static Logger log = Logger.getLogger("DAO");注意这⾥ 使⽤"DAO"标识符，那么对应的在配置⽂件中对应的配置信息如下：#定义DAO Loggerlog4j.logger.DAO=DEBUG,A2#设置Appender A2的属性 log4j.appender.A2=org.apache.log4j.DailyRollingFileAppenderlog4j.appender.A2.ﬁle=demolog4j.appender.A2

.DatePattern='.'yyyy-MMddlog4j.appender.A2.layout=org.apache.log4j.PatternLayoutlog4j.appender.A2.layout.ConversionPattern=%-5 p %d{yyyy-MM-dd HH:mm:ss}%l%n%m%npublic class RoleDAO extends BaseDBObject{...static Logger log

= Logger.getLogger("DAO");...public BeanCollection selectAll() throws SQLException{StringBuffer sql = new StringBuffer(SQLBUF_LEN);sql.append("SELECT * FROM " + tableName + " order byroldId");//System.out.println(sql.toString());log.debug(sql);...}...}3.3 使⽤Logger对象的debug,info,fatal...⽅ 法log.debug("it is the debug info");附件1：log4j的⼀个bug当这样使⽤时，DailyRollingFileAppender不能正 确使⽤：public Class RoleDAO(){static Logger log = Logger.getLogger("DAO");//在每⼀次new RoleDAO 对象的时候都执⾏⼀次conﬁgure()操作public RoleDAO(TransactionManager transMgr) throws SQLException{...PropertyConﬁgurator.conﬁgure("log4j.properties");...}public void select(){...//使⽤log4j进⾏ log记录log.debug("...");...}}怎么解决:在系统启动时执⾏⼀次 PropertyConﬁgurator.conﬁgure("log4j.properties");之后就不再执⾏。

--------------------------------------------------------------------------------------------------------------------------⼀、log4j.properties ### 设置org.zblog域对应的级别INFO,DEBUG,WARN,ERROR和输出地A1，A2 ## log4j.category.org.zblog=ERROR,A1 log4j.category.org.zblog=INFO,A2 log4j.appender.A1=org.apache.log4j.ConsoleAppender ### 设置输出地A1，为ConsoleAppender(控制台) ## log4j.appender.A1.layout=org.apache.log4j.PatternLayout ### 设置A1的输出布局格式PatterLayout,(可 以灵活地指定布局模式）## log4j.appender.A1.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss,SSS} [%c]-[%p] %m%n ### 配置⽇志输出的格式## log4j.appender.A2=org.apache.log4j.RollingFileAppender ### 设置输出地A2到⽂件（⽂件⼤⼩到达指定尺⼨的时候产⽣⼀个新的⽂件）## log4j.appender.A2.File=E:/study/log4j/zhuwei.html ### ⽂件位置## log4j.appender.A2.MaxFileSize=500KB ### ⽂件⼤⼩## log4j.appender.A2.MaxBackupIndex=1 log4j.appender.A2.layout=org.apache.log4j.HTMLLayout ##指定采⽤html⽅式输出 ⼆、log4j.xml <?xml version="1.0" encoding="GB2312" ?> <!DOCTYPE log4j:conﬁguration SYSTEM "log4j.dtd"> <log4j:conﬁgurationxmlns:log4j=" "> <appender name="org.zblog.all"class="org.apache.log4j.RollingFileAppender"> <!-- 设置通道ID:org.zblog.all和输出⽅ 式：org.apache.log4j.RollingFileAppender --> <param name="File" value="E:/study/log4j/all.output.log"/><!-- 设置File参数：⽇志输出⽂件名--> <param name="Append" value="false" /><!-- 设置是否在重新启动服务时，在原有⽇志的基础添加新⽇志 --> <layout class="org.apache.log4j.PatternLayout"> <param name="ConversionPattern" value="%p (%c:%L)%m%n"/><!-- 设置输出⽂件项⽬和格式 --> </layout> </appender> <appender name="org.zblog.zcw"class="org.apache.log4j.RollingFileAppender"> <param name="File" value="E:/study/log4j/zhuwei.output.log"/> <param name="Append" value="true" /> <param name="MaxFileSize" value="10240" /> <!-- 设置⽂件⼤⼩ --> <layout class="org.apache.log4j.PatternLayout"> <param name="ConversionPattern" value="%p (%c:%L)%m%n"/> </layout> </appender> <logger name="zcw.log"> <!-- 设置域名限制，即zcw.log域及以下的 ⽇志均输出到下⾯对应的通道中 --> <level value="debug" /><!-- 设置级别--> <appender-ref ref="org.zblog.zcw" /><!-- 与前⾯的通道id相对应 --> </logger> <root> <!-- 设置接收所有输出的通道 -> <appender-ref ref="org.zblog.all" /><!-- 与前⾯的通道id相对应 --> </root> </log4j:conﬁguration> 三、配置⽂件加载⽅法： import org.apache.log4j.Logger; import org.apache.log4j.PropertyConﬁgurator; import org.apache.log4j.xml.DOMConﬁgurator; fsdafdsfpublic class Log4jApp { public static void main(String[] args) { DOMConﬁgurator.conﬁgure("E:/study/log4j/log4j.xml");//加载.xml⽂件 //PropertyConﬁgurator.conﬁgure("E:/study/log4j/log4j.properties");//加载.properties⽂件 Logger log=Logger.getLogger("org.zblog.test"); log.info("测试"); } } 四、项⽬使⽤log4j 在web应⽤中，可以将配置 ⽂件的加载放在⼀个单独的servlet中，并在web.xml中配置该servlet在应⽤启动时候加载。对于在多⼈项 ⽬ 中，可以给每⼀个⼈设置⼀个输出通道，这样在每个⼈在构建Logger时，⽤⾃⼰的域名称，让调试 信息输出到⾃⼰的log⽂件中。 五、常⽤输出格式 # -X号:X信息输出时左对齐； # %p:⽇志信息级别 # %d{}:⽇志信息产⽣时间 # %c:⽇志信息所在地（类名） # %m:产⽣的⽇志具体信息 # %n:输出⽇志信息 换⾏

htp:/jakarta.apache.org/log4j/

例⼦3：显⽰log级别,时间,调⽤⽅法,log信息 log4j.appender.A2.layout=org.apache.log4j.PatternLayoutlog4j.appender.A2.layout.ConversionPattern=[%-5p] %d{yyyy-MM-dd HH:mm:ss,SSS} method:%l%n%m%nlog信息:[DEBUG] 2002-11-12 12:00:57,376 method:cn.net.unet.weboa.system.dao.RoleDAO.select(RoleDAO.java:409)SELECT * FROM Role WHERE 1=1 order by createDate desc

# Log4j输出格式控制参数说明例⼦

<table>
  <tr>
    <th>%c</th>
    <th>列出loger名字空间的 全称，如果加上{<层数 >}表示列出从最内层算 起的指定层数的名字空 间</th>
    <th>log4j配置⽂件参数举例</th>
    <th>输出显示媒介</th>
  </tr>
  <tr>
    <td>假设当前loger名字空</td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>间是"a.b.c" %c</td>
    <td>a.b.c</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>%c{2}</td>
    <td>b.c</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>%20c</td>
    <td>（若名字空间⻓度⼩于 20，则左边⽤空格填 充）</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>%-20c</td>
    <td>（若名字空间⻓度⼩于 20，则右边⽤空格填 充）</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>%.30c</td>
    <td>（若名字空间⻓度超过 ，截去多余字符）</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>%20.30c</td>
    <td>30 （若名字空间⻓度⼩于 20，则左边⽤空格填 充；若名字空间⻓度超<br><br>，截去多余字符）</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>%-20.30c</td>
    <td>过30 （若名字空间⻓度⼩于 20，则右边⽤空格填 充；若名字空间⻓度超<br><br>，截去多余字符）</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>%C</td>
    <td>过30<br><br>列出调⽤loger的类的 全名（包含包路径）</td>
    <td>假设当前类 是"org.apache.xyz.So</td>
    <td> </td>
  </tr>
  <tr>
    <td>%C</td>
    <td>org.apache.xyz.SomeC</td>
    <td>meClas"</td>
    <td> </td>
  </tr>
  <tr>
    <td>%C{1}</td>
    <td>las SomeClas</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>%d</td>
    <td>显示⽇志记录时间，{< ⽇期格式>}使⽤ ISO8601定义的⽇期格 式</td>
    <td>%d{ y/ M/ d H: m:s, S}</td>
    <td>205/10/12 2 23 30,17</td>
  </tr>
  <tr>
    <td>%d{ABSOLUTE}</td>
    <td>2 23 30,17</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>%d{DATE}</td>
    <td>12 Oct 205</td>
    <td> </td>
    <td> </td>
  </tr>
</table>


## 2 23 30,17

<table>
  <tr>
    <th>%d{ISO8601}</th>
    <th>205-10-2</th>
    <th> </th>
    <th> </th>
  </tr>
  <tr>
    <td>%F</td>
    <td>2 23 30,17<br><br>显示调⽤loger的源⽂ 件名</td>
    <td>%F</td>
    <td>MyClas.java</td>
  </tr>
  <tr>
    <td>%l</td>
    <td>输出⽇志事件的发⽣位 置，包括类⽬名、发⽣ 的线程，以及在代码中 的⾏数</td>
    <td>%l</td>
    <td>MyClas.main(MyClas<br><br>.java:129)</td>
  </tr>
  <tr>
    <td>%L</td>
    <td>显示调⽤loger的代码 ⾏</td>
    <td>%L</td>
    <td>129</td>
  </tr>
  <tr>
    <td>%m</td>
    <td>显示输出消息</td>
    <td>%m</td>
    <td>This is a mesage for</td>
  </tr>
  <tr>
    <td>%M</td>
    <td>显示调⽤loger的⽅法 名</td>
    <td>%M</td>
    <td>debug.<br><br>main</td>
  </tr>
  <tr>
    <td>%n</td>
    <td>当前平台下的换⾏符</td>
    <td>%n</td>
    <td>Windows平台下表示rn</td>
  </tr>
  <tr>
    <td>%p</td>
    <td>显示该条⽇志的优先级</td>
    <td>%p</td>
    <td>UNIX平台下表示n INFO</td>
  </tr>
  <tr>
    <td>%r</td>
    <td>显示从程序启动时到记 录该条⽇志时已经经过 的毫秒数</td>
    <td>%r</td>
    <td>1215</td>
  </tr>
  <tr>
    <td>%t</td>
    <td>输出产⽣该⽇志事件的 线程名</td>
    <td>%t</td>
    <td>MyClas</td>
  </tr>
  <tr>
    <td>%x</td>
    <td>按NDC（Nested Diagnostic Context， 线程堆栈）顺序输出⽇ 志</td>
    <td>假设某程序调⽤顺序是 MyAp调⽤ com.fo.Bar</td>
    <td> </td>
  </tr>
  <tr>
    <td>%c %x - %m%n</td>
    <td>MyAp - Cal com.fo.Bar.<br><br>com.fo.Bar - Log in Bar<br><br>p - Return to</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>%X</td>
    <td>MyAp. 按MDC（Maped Diagnostic Context， 线程映射表）输出⽇ 志。通常⽤于多个客户 端连接同⼀台服务器， ⽅便服务器区分是那个 客户端访问留下来的⽇ 志。</td>
    <td>%X{5}</td>
    <td>（记录代号为5的客户 端的⽇志）</td>
  </tr>
</table>


<table>
  <tr>
    <th> </th>
    <th>显示⼀个百分号</th>
    <th> </th>
    <th> </th>
  </tr>
</table>


% % %

列了这么多，举⼏个实际的例⼦吧，⽐如log4j.properties的内容为：

<table>
  <tr>
    <th>#log4j config log4j.rotLoger=DEBUG,OUTPUT<br><br>log4j.apender.OUTPUT.layout=org.apache.log4j.PaternLayout log4j.apender.OUTPUT.layout.ConversionPatern=%d{DATE} %-4r [%t] %-5p %c %x - %m%n</th>
  </tr>
</table>


…

那么⼀个可能的输出是： 12 Oct 205 2 23 30,17 0 [main] INFO MyAp - Entering aplication.

… 12 Oct 205 2 23 30,162 45 [main] INFO MyAp - Exiting aplication.

