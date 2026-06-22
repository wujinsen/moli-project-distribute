今天研究了Nutch， 差不多已经好⼏个⼩时了， 到现在还没有搞定，也这么晚了， 先记录下来，明天 继续吧。

⼀开始很多时间都浪费在了cygwin的安装上了，bs这个软件的开发者了， ⼀个不伦不类的软件安 装程序，安装的时候还要从⽹上下载东东。。。。。不过最后终于装成功了， 先下载到本地后，再安 装的（建议 下载站点中选 TW的⽐较块）。

下⾯是我安装CYGWIN和NUTCH的过程， 都块成功了， 但最后卡在了⽤户查询界⾯， 输⼊东⻄什 么都查不出来，不知怎么回事。

NUTCH的⼤致原理如下 ：

安装步骤参考了 ⼀、环境：

该⽂章

- 1.操作系统：windowsXp,windows2 0+
- 2.javaVM：java1.5.x，设置JAVA_HOME到环境变量
- 3. ,当然这个不是必需的，只是nutch提供的脚本只能在shel环境下使⽤，所以使⽤cygwin

来虚拟shel命令。

- 4.nutch版本：0.8
- 5.tomcat：5.0


cygwin

⼆、cygwin的安装：

Nutch 在 Windows 中安装之细解

cygwin的安装在 ⼀⽂中有较为详细的介绍，此处不再介绍安装步 骤，只介绍安装后需要如何判断是否能够使⽤：在cygwin的安装⽬录下，查找 x:/cygwin/cygwin/bin/sh.exe，存在此命令即可使⽤。

cygwin在删除后会发现⽆法再次成功安装的问题，可以通过注册表内的查找功能，删除所有包含 cygwin内容的键值即可。 三、nutch的安装和配置：

- 1。从 下载0.8或更⾼的版本，解压缩后，放置到cygwin的


htp:/lucene.apache.org/nutch/release/

根⽬录下，如图：

<table>
  <tr>
    <th>![image 1](<Nutch.note_images/imageFile1.png>)</th>
  </tr>
</table>


图中可以看到nutch⽬录在cygwin的根⽬录下。

- 2。在nutch/bin下，建⽴urls⽬录，然后建⽴⼀个url.txt⽂件，在url.txt⽂件内写⼊⼀个希望爬⾏的


url，例如： w.sina.com.cn ，⽬录结构如图：

<table>
  <tr>
    <th>![image 2](<Nutch.note_images/imageFile2.png>)</th>
  </tr>
</table>


- 3。打开nutch/conf/crawl-urlfilter.txt⽂件，把MY.DOMAIN.NAME字符替换为url.txt内的url的

域名，其实更简单点，直接删除MY.DOMAIN.NAME这⼏个字就可以了，也就是说，只保存

+^ [a-z0-9]*/.)*这⼏个字就可以了，表示所有http的⽹站都同意爬⾏。

- 4 。打开nutch/conf/conf/nutch-site.xml⽂件，在<configuration></configuration>内插⼊⼀下内


htp:/(

容： <property><o:p></o:p>

<name>htp.agent.name</name><o:p></o:p> <value></value><o:p></o:p> <description>HTP 'User-Agent' request header. MUST NOT be empty - <o:p></o:p> please set this to a single word uniquely related to your organization.<o:p></o:p>

<o:p> </o:p> NOTE: You should also check other related properties:<o:p></o:p>

<o:p> </o:p> htp.robots.agents<o:p></o:p> htp.agent.description<o:p></o:p> htp.agent.url<o:p></o:p> htp.agent.email<o:p></o:p> htp.agent.version<o:p></o:p>

<o:p> </o:p>

and set their values apropriately.<o:p></o:p> <o:p> </o:p>

</description><o:p></o:p> </property><o:p></o:p> <o:p> </o:p> <property><o:p></o:p>

<name>htp.agent.description</name><o:p></o:p> <value></value><o:p></o:p> <description>Further description of our bot- this text is used in<o:p></o:p> the User-Agent header. It apears in parenthesis after the agent name.<o:p></o:p> </description><o:p></o:p>

</property><o:p></o:p> <o:p> </o:p> <property><o:p></o:p>

<name>htp.agent.url</name><o:p></o:p> <value></value><o:p></o:p> <description>A URL to advertise in the User-Agent header. This wil <o:p></o:p>

apear in parenthesis after the agent name. Custom dictates that this<o:p></o:p> should be a URL of a page explaining the purpose and behavior of this<o:p></o:p> crawler.<o:p></o:p>

</description><o:p></o:p> </property><o:p></o:p> <o:p> </o:p> <property><o:p></o:p>

<name>htp.agent.email</name><o:p></o:p> <value></value><o:p></o:p> <description>An email adres to advertise in the HTP 'From' request<o:p></o:p>

header and User-Agent header. A god practice is to mangle this<o:p></o:p> adres (e.g. 'info at example dot com') to avoid spa ming.<o:p></o:p>

</description><o:p></o:p> </property><o:p></o:p>

把<name> X</name>之间的内容替换为其他字符，当然就算是不替换也⽆所谓，这⾥的设置， 是因为nutch遵守了robots协议，在获取response时，把⾃⼰的相关信息提交给被爬⾏的⽹站，以供识 别。

以上配置，是爬取intranet的配置⽅式。 四、执⾏nutch

由于配置nutch采⽤的是单独⽹站的配置⽅式，所以执⾏上我们也采⽤的是单⽹查询，全⽹查询在以 后的内容中介绍。

先看⼀看nutch给出的命令：nutch crawl urls -dir crawl -depth 3 -topN 50crawl：通知nutch.jar， 执⾏crawl的main⽅法。 urls：存放需要爬⾏的url.txt⽂件的⽬录，注意，这个名字需要和你的⽂件夹 ⽬录相同，如果你的⽂件夹为search，那这⾥也应该改成search。 -dir crawl：爬⾏后⽂件保存的位 置，可以在nutch/bin⽬录下找到。 -depth 3：爬⾏次数，或者成为深度，不过还是觉得次数更贴 切，建议测试时改为1。 -topN 50：⼀个⽹站保存的最⼤⻚⾯数。

执⾏命令的步骤：

- 1。进⼊cygwin界⾯。
- 2。使⽤cd命令，进⼊nutch/bin路径下。
- 3。执⾏：sh nutch crawl urls -dir crawl -depth 3 -topN 50具体的爬⾏⽇志可以在nutch/logs⽬


录下看到，注意查找“INFO fetcher.Fetcher - fetching ”这样的内容，这⾥是抓去过 程⽇志。

htp:/ X

五、查询搜索：（⽬前我还没有通过此步,输⼊关键字⽼是没有结果？？？？） nutch 提供了类似 gogle、baidu的⽹⻚⻚⾯，在nutch压缩包下找到nutch-0.8.war⽂件，放到tomcat/webaps⽬录 下，修 改webaps/nutch/WEB-INF/clases/nutch-site.xml⽂件内容如下：<property> <name>searcher.dir</name> <value>C:/cygwin/nutch/bin/crawl </value> </property><value/> 的内容是刚才爬⾏后的crawl⽬录位置，提供给客户端来查询。 配置完成后，启动ｔｏｍｃａｔ， 输⼊ ，输⼊关键字，就会看到结果了，下图是我抓去ｗａｐ⽹站的测试结 果：

htp:/localhost:8080/nutch

<table>
  <tr>
    <th>![image 3](<Nutch.note_images/imageFile3.png>)</th>
  </tr>
</table>


六、总结： ntuch 提供了⼀个⾼效、开源、易操作的搜索引擎，内部有许多细微之处都是值得借鉴的，例如采⽤

了hadop的分布式⽂件系统，类似eclipse的插件技术， apache的htpclient来访问⽹站， org.cyberneko.html得HtmlParse来解析⻚⾯等等，在以后会逐个介绍

