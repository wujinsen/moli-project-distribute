前些⽇⼦琢磨着想搭建⼀个搜索引擎，⾃⼰写成本有点⾼，虽然以前写过爬⾍，但是索引排序估计要 烦得多

nutch

是⼀个开源的、Java 实现的搜索引擎。它提供了我们运⾏⾃⼰的搜索引擎所需的全部⼯具。是 ⼀个应⽤程序，可以以 Lucene 为基础实现搜索引擎应⽤。 选定nutch之后，开始着⼿学习使⽤nutch，英⽂⽔平还不够，只能看看nutch的简单的tutorial，但是 真正当教程，我还是选择了中⽂，可以让第⼀个搜索跑起来之后再选择学习英⽂的⽂档，以便更深的 理解。 我选择的教程是

nutch⼊⻔学习

# 准备⼯作：

我的系统是Ubuntu 9.10，java -version 1.6.0_20-b02，nutch 1.0，以及tomcat 6.0.26

- 1. a.
- 2.


jdk和tomcat⼀般⼤家做过java和web开发都会有装，不赘述，有⼏点需要注意的列出来 tomcat的bin/catalina.sh中加⼊JAVA_HOME=/usr/lib/jvm/java-6-sun-1.6.0.20，这点我深受 其害，开始没有设置，运⾏bin/nutch crawl的时候总是说JAVA_HOME is not set，我⼀想我明 明设置了java环境变量的，java-version也是正常的，各种gogle，确定各种地⽅可以设置 JAVA_HOME的地⽅，都⽆济于事，最后在⼀个⻆落找到，在此⽂件中可以添加 JAVA_HOME，然后运⾏，居然可以，但是我不明⽩，nutch爬⾍的运⾏应该是不依赖于 tomcat的，tomcat只是⽤于搜索。这点未参透。

tomcat，jdk搞定之后是nutch，我直接将nutch放在⽤户名下⾯的nutch⽬录，然后将其中的 nutch.war复制到tomcat的webap中，并取代ROT（解压，重命名⽬录）

# 配置nutch：

nutch⼊⻔ 学习

这⾥参考 ，我把改的地⽅说明出来。

- 1.

- a.
- b.
- c.


- 2.
- 3.


增加要抓取的⻚⾯(以 w.163.com为例) [rot@localhost nutch]#mkdir urls [rot@localhost nutch]#echohtp:/ w.163.com/ >urls/163 163⽂件中输⼊htp:/news.163.com/

编辑conf/crawl-urlfilter.txt⽂件，设定要抓取的⽹址信息。[rot@localhost nutch]#vi conf/crawlurlfilter.txt修改MY.DOMAIN.NAME为:# acept hosts in MY.DOMAIN.NAME+^ [a-z09]*\.)*163.com/

htp:/(

编辑conf/nutch-site.xml⽂件，增加代理的属性，并编辑相应的属性值Xml代码

![image 1](<Nutch 初体验 爬行企业内部网.note_images/imageFile1.png>)

- 1.
- 2.


<property> <name>htp.agent.name</name>

- <value></value> <description>HTP 'User-Agent' request header. MUST NOT be empty please set this to a single word uniquely related to your organization. NOTE: You should also check other related properties: htp.robots.agents htp.agent.description htp.agent.url htp.agent.email htp.agent.version and set their values apropriately. </description> </property> <property> <name>htp.agent.description</name> <value></value> <description>Further description of our bot- this text is used in the User-Agent header. It apears in parenthesis after the agent name. </description> </property> <property> <name>htp.agent.url</name> <value></value> <description>A URL to advertise in the User-Agent header. This wil apear in parenthesis after the agent name. Custom dictates that this should be a URL of a page explaining the purpose and behavior of this crawler. </description> </property> <property> <name>htp.agent.email</name> <value></value> <description>An email adres to advertise in the HTP 'From' request header and User-Agent header. A god practice is to mangle this adres (e.g. 'info at example dot com') to avoid spa ming. </description>
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.


nutch⼊⻔ 学习

中说这⾥就算是不修改也⽆所谓，这⾥的设置，是因为nutch遵守了robots协议，在获

取response时，把⾃⼰的相关信息提交给被爬⾏的⽹站，以供识别。但是我这样设置出现了错误提 示，即htp.agent.name需要设置，我将value设置成 xusulong*（记住有*）即可。其他可以不设置 了。

# 配置tomcat：

- 1.

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


- 2.


设定搜索⽬录(是由于默认的segment路径与我们实际的路径不符所造成的)[rot@localhost nutch]#cd ~/tomcat[rot@localhost tomcat]#vi webaps/ROT/WEB-INF/clases/nutchsite.xml增加四⾏代码，修改成为Xml代码

![image 2](<Nutch 初体验 爬行企业内部网.note_images/imageFile2.png>)

<configuration> <property> <name>searcher.dir</name> <value>/home/whu/nutch/crawl.demo</value> </property> </configuration> 这⾥的/home/whu/nutch/crawl.demo是我的nutch路径，爬⾍到时候的数据就 会放在程序新建的crawl.demo下⾯，即nutch抓取的⻚⾯的保存⽬录。 nutch对中⽂的⽀持还不完善，需要修改tomcat⽂件夹下conf/server.xml⽂件[rot@localhost tomcat]#vi conf/server.xml增加两句，修改为<Conector port="8080"maxThreads="150" minSpareThreads="25" maxSpareThreads="75"enableLokups="false" redirectPort="843" aceptCount="10"conectionTimeout="2 0" disableUploadTimeout="true"URIEncoding="UTF-8" useBodyEncodingForURI="true" />

抓取⽹⻚：

whu@leopard:~/nutch$ bin/nutch crawl urls -dir crawl.demo -depth 2 -threads 4 -topN 5 >& crawl.log

具体的参数nutch⼊⻔ 学习 有解释，也可以参⻅nutch的官⽅⽹站。这⾥只抓取少量站点。

![image 3](<Nutch 初体验 爬行企业内部网.note_images/imageFile3.png>)

这时候 crawl.log会记录抓取的信息，我中间遇到过 如下⼏个错误：

- 1.
- 2.


htp.agent.name需要设置问题 Input path does not exist问题，这个多试⼏次路径即可，只要这⾥的crawl.demo和配置tomcat中 的路径对应，记得出错的时候把出错的⽬录删除，否则下次还是出错。

测试结果：

运⾏tomcat，进⼊⾸⻚，搜索⽹易，结果如下：

搞了⼀个下午和晚上，泪流满⾯，中途还有其他的错误我记不⼤清楚了，总之严重的错误我列出来 了，仔细看系统如何报错，gogle之，仔细发现错误才是王道。

