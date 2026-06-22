<table>
  <tr>
    <th>Apache Nutch是⼀个⽤Java编写的开源⽹络爬 ⾍。通过它，我们就能够⾃动地找到⽹⻚中的超 链接，从⽽极⼤地减轻了维护⼯作的负担，例如 检查那些已经断开了的链接，或是对所有已经访 问过的⽹⻚创建⼀个副本以便⽤于搜索。接下来 就是Apache Solr所要做的。Solr是⼀个开源的全 ⽂搜索框架，通过Solr我们能够搜索Nutch已经访 问过的⽹⻚。幸运的是，关于Nutch和Solr之间的 整合在下⽅已经解释得相当清楚了。 Apache Nutch对于Solr已经⽀持得很好，这⼤⼤ 简化了Nutch与Solr的整合。这也消除了过去依赖 于Apache Tomcat来运⾏⽼的Nutch⽹络应⽤以 及依赖于Apache Lucene来进⾏索引的麻烦。只 需要从 下载⼀个⼆进制的发⾏版即可。<br><br>介绍<br><br>这⾥</th>
    <th>2⼈顶 翻译的不错哦!<br><br>kzjnet翻译于 1年前<br><br>顶</th>
  </tr>
</table>


<table>
  <tr>
    <th>。<br><br>下载⼆进制包（apache-nutch-1.X-bin.zip）。<br><br>从现在开始，我们将会使⽤ ${NUTCH_RUNTIME_HOME}来代替当前⽬录 （apache-nutch-1.X/）。<br><br>⾼级⽤户也可能会使⽤源代码发⾏包：<br><br>）<br><br>当使⽤源代码包时，我们会⽤ ${NUTCH_RUNTIME_HOME}代替⽬录apachenutch-1.X/runtime/local/。记住这些：<br><br>步骤<br><br>这篇教程描述了Nutch 1.x（当前版本是1.6） 的安装和使⽤。关于如何编译和安装Nutch 2.x，请查看<br><br>Nutch2Tutorial<br><br>1.从⼆进制发⾏包安装Nutch<br><br>从 这⾥<br><br>解压缩您的Nutch包。那应该会有⼀个新⽂件 夹apache-nutch-1.X。 cd apache-nutch-1.X/<br><br>从源代码安装Nutch<br><br>下载⼀个源代码包（apache-nutch-1.Xsrc.tar.gz）<br><br>解压缩<br><br>cd apache-nutch-1.X/<br><br>在这个⽬录⾥运⾏ant（参⻅： RunNutchInEclipse<br><br>现在那会有⼀个⽬录runtime/local，它包含了 准备使⽤的Nutch安装<br><br>配置⽂件在apache-nutch1.X/runtime/local/conf/⽬录⾥⾯<br><br>ant clean将会移除这个⽬录（并保留被更改的 配置⽂件的备份）</th>
    <th>1⼈顶 翻译的不错哦!<br><br>kzjnet翻译于 1年前<br><br>顶</th>
  </tr>
</table>


<table>
  <tr>
    <th>Usage: nutch [-core] COMAND ⼀些解决问题的提示：<br><br>chmod +x bin/nutch<br><br>export JAVA_HOME=/System/Library/Frameworks/Java<br><br>2.检验您的Nutch安装<br><br>运⾏”bin/nutch“。如果您能看⻅下列内容说明 您的安装是正确的：<br><br>如果您看⻅”Permision denied”那么请运⾏下 列命令：<br><br>如果您看⻅JAVA_HOME没有设置那么请设置 JAVA_HOME环境变量。在Mac上，您可以运 ⾏下述命令或者把它添加到~/.bashrc⾥⾯去：</th>
    <th>1⼈顶 翻译的不错哦!<br><br>kzjnet翻译于 1年前<br><br>顶</th>
  </tr>
</table>


#### VM.framework/Versions/1.6/Home

## 3.抓取您的第⼀个⽹站

将您的代理的名称添加到conf/nutch-site.xml 的htp.agent.name属性的Value字段⾥，例 如：

kzjnet翻译于 1年前

0⼈顶 翻译的不错哦!

顶

property>

<name>htp.agent.name</name> <value>My Nutch Spider</value> </property>

mkdir -p urls

cd urls

touch sed.txt

这样⼦就在urls/⽬录下创建了⼀个⽂本⽂档 sed.txt。它需要包含像下⾯这样的内容（每⾏⼀ 个⽹站URL来告诉Nutch您想要抓取的⽹站）：

htp:/nutch.apache.org/

编辑⽂件conf/regex-urlfilter.txt并且替换 # acept anything else

+. 为⼀条与您要抓取的域名相对应的正则表达式。 例如，如果您想要限制为抓取nutch.apache.org 这⼀域名，这⼀⾏读起来应该像是这样⼦的：

+^ [a-z0-9]*\.)*nutch.apache.org/ 这会包括在nutch.apache.org下的任何URL。

htp:/(

### 3.1使⽤抓取命令

现在我们已经准备好开始⼀次抓取，可以使⽤以 下的参数：

- -dirdir 指定⽤于存放抓取⽂件的⽬录名称。

- -threadsthreads 决定将会在获取是并⾏的线 程数。

- -depthdepth 表明从根⽹⻚开始那应该被抓 取的链接深度。

- -topNN 决定在每⼀深度将会被取回的⽹⻚的 最⼤数⽬


运⾏下⾯的命令：

bin/nutch crawl urls -dir crawl -depth 3 -topN 5

现在您应该能够看⻅下列⽬录被创建了：

crawlcrawldb llinkdb crawl/segmentsThis

请记住：如果您有⼀个已经设置好了的Solr并 且想要建⽴索引到那⾥⾯去，您必须添加-solr <solrUrl>参数到您的crawl命令⾥⾯。例如：

bin/nutch crawl urls -solr

htp:/localhost:8983/ solr/

-depth 3 -topN 5

然后请直接跳到后⾯–为 搜 索 设 置 Solr 。

通常⼀开始测试⼀个配置都是通过抓取在较浅深 度来进⾏，⼤⼤地限制了每⼀级所获取的⽹⻚数 （-topN），并且观察输出来检查所需要的⻚⾯ 是否已经得到以及不需要的⻚⾯是否被阻挡。要 想查看某⼀配置是否正确，对于全⽂搜索来说较 为适当的深度设置⼤约是10左右。每⼀级所获取 的⽹⻚数 （ -topN）可以从⼏万上到⼏百万，这 取决于您的资源。

<table>
  <tr>
    <th>整个⽹络的抓取被设计成⽤来处理那些可能需要 耗费⼏个星期来完成，在许多台机器上运⾏的⾮ 常⼤的抓取。这也允许在抓取的过程中进⾏更多 的控制，还有增量抓取。最重要的是要记住整个 ⽹络的抓取并不⼀定意味着要抓取整个万维⽹。 我们可以限制整个⽹络的抓取只是抓取我们列出 的想要抓取的URL。这是通过使⽤⼀个就像我们 命令时⼀样的过滤器来完成的。<br><br>3.2使⽤特别的命令对整个⽹络进⾏抓取<br><br>请记住：如果您先前更改并覆盖了⽂件 conf/regex-urlfilter.txt在这⾥您需要将它改回 去。</th>
    <th>0⼈顶 翻译的不错哦!<br><br>kzjnet翻译于 1年前<br><br>顶</th>
  </tr>
</table>


⽤ crawl

<table>
  <tr>
    <th>Nutch数据是由这些组成的：<br><br>循序渐进之–概念<br><br>抓取数据库，或者说是crawldb。它包含了关 于每⼀个Nutch已知的URL的信息，包括它是 否已经被获取，甚⾄是何时被获取的。<br><br>链接数据库，或者说是linkdb。它包含了每⼀ 个已知URL的链接，包括源的URL以及链接的 锚⽂本。<br><br>⼀系列的分段，或者说是segments。每⼀个 segments都是⼀组被作为⼀个单元来获取的 URL。segments是它本身这个⽬录以及它下 ⾯的⼦⽬录：<br><br>⼀个crawl_generate确定了将要被获取的 ⼀组URL；<br><br>⼀个crawl_fetch包含了获取的每个URL的 状态；<br><br>⼀个content包含了从每个URL获取回来的 原始的内容；<br><br>⼀个parse_text包含了每个URL解析以后 的⽂本；<br><br>⼀个parse_data包含来⾃每个URL被解析 后内容中的外链和元数据；<br><br>⼀个crawl_parse包含了外链的URL，⽤来 。</th>
    <th>0⼈顶 翻译的不错哦!<br><br>kzjnet翻译于 1年前<br><br>顶</th>
  </tr>
</table>


#### 更新crawldb

<table>
  <tr>
    <th>选择1：从DMOZ数据库⾃举。 由injector添加URL到crawldb⾥。让我们从 DMOZ开放式分类⽬录添加URL吧。⾸先我们必 须下载并且解压缩这个DMOZ所有⽹⻚的列表 （这是⼀个20多MB的⽂件，所以这会消耗⼏分 钟）。 wget gunzip content.rdf.u8.gz 接下来我们选择这些⽹⻚当中随机的⼀些⼦集 （我们使⽤随机的⼦集所以所有在跟着这⼀个教 程做的⼈就不会伤害到同样的⽹站）。DMOZ包 含了⼤约三百万个URL。我们从每5 0个URL中 选择出⼀个，因此我们就有⼤约1 0个URL： mkdir dmoz bin/nutch org.apache.nutch.tols.DmozParser content.rdf.u8 -subset 5 0 > dmoz/urls 这⼀分析器也需要⼏分钟来完成，因为它必须要 分析整个⽂件。最后，我们⽤这些选出的URL来 初始化crawldb。 bin/nutch inject crawl/crawldb dmoz 现在我们有了⼀个⼤约有1 0个未被获取的URL 的⽹络数据库。<br>选择2：从初始列表⾥⾃举。 这⼀选项不为⼈们所了解的地⽅在于创建初始列 表并覆盖在urls/⽬录⾥。<br><br><br>循序渐进之–⽤⼀组URL列表确定crawldb<br><br>htp:/rdf.dmoz.org/rdf/content.rdf.u8.gz</th>
    <th>0⼈顶 翻译的不错哦!<br><br>kzjnet翻译于 1年前<br><br>顶</th>
  </tr>
</table>


#### bin/nutch inject crawl/crawldb urls

<table>
  <tr>
    <th>要获取，我们⾸先要从数据库⾥产⽣⼀个获取的 列表。 bin/nutch geerate crawl/crawldb crawl/segments 这会为所有预定要被获取的⽹⻚产⽣⼀个获取列 表。获取列表放在⼀个新创建的分段⽬录⾥。分 段⽬录的名称取决于它被创建时的时间。 我们将这个分段的名字放在shel的变量s1⾥⾯：<br><br>s1=`ls -d crawl/segments/2* | tail -1`<br><br>echo $s1 现在我们能以下⾯的命令在这个分段⾥进⾏获 取： bin/nutch fetch $s1 然后我们就能解析条⽬： bin/nutch parse $s1 当这⼀切完成以后，我们就以获取回来的结果更 新数据库：<br><br>bin/nutch updatedb crawl/crawldb $s1 现在，数据库包含了刚刚更新的条⽬的所有初始 ⻚，除此之外，新的⽹⻚条⽬对于链接到初始的 集合来进⾏新条⽬的发现是相符合的。 所以我们对包含得分最⾼的1 0⻚提取出来产⽣ ⼀个新的分段： bin/nutch geerate crawl/crawldb craw/segments -topN 1 0<br><br>s2=`ls -d crawl/segments/2* | tail -1` echo $s2<br><br>nntch fetch $s2 nn parse $s2<br><br>bin/nutch updatedb crawl/crawldb $s2 让我们再来获取⼀次吧： bin/nutch geerate crawl/crawldb craw/segments -topN 1 0<br><br>s3=`ls -d crawl/segments/2* | tail -1` echo $s3<br><br>nntch fetch $s3 nn parse $s3<br><br>bin/nutch updatedb crawl/crawldb $s3 通过这⼀点我们已经获取了⼏千⻚的⽹⻚。让我 们索引它们吧！<br><br><br><br><br><br><br>循序渐进之–获取</th>
    <th>0⼈顶 翻译的不错哦!<br><br>kzjnet翻译于 1年前<br><br>顶</th>
  </tr>
</table>


<table>
  <tr>
    <th>在我们进⾏索引之前，我们⾸先要反转所有的链 接，以便我们能够以这些⽹⻚来索引进⼊的锚⽂ 本。 bin/nutch invertlinks crawl/linkdb -dir crawl/segments 我们现在准备好要⽤Apache Solr进⾏搜索了。<br><br>下载⼆进制⽂件。<br><br>在您启动Solr管理员控制台以后，您应该能够访 问下列这些链接：<br><br>循序渐进之–反向链接<br><br>4.为搜索设置Solr<br><br>从 这⾥<br><br>解压缩到$HOME/apache-solr-3.X，从现在 起，我们将会⽤${APACHE_SOLR_HOME}代 替它。<br><br>cd ${APACHE_SOLR_HOME}/example<br><br>java -jar start.jar<br><br>5.检验Solr的安装<br><br>ht:/loalhost:88/sol/a /</th>
    <th>0⼈顶 翻译的不错哦!<br><br>kzjnet翻译于 1年前<br><br>顶</th>
  </tr>
</table>


#### htp:/localhost:8983/solr/admin/stats.jsp

<table>
  <tr>
    <th>我们已经将Nutch和Solr正确地安装设置好了。并 且Nutch已经从URL列表⾥创建并抓取了数据。 以下步骤是⼀个以Solr来搜索要搜索的链接的代 表：<br><br>bin/nutch solrindex crawl/crawldb -linkdb crawl/linkdb crawl/segments/*<br><br>运⾏solrindex的⼀些细节已经被改变了。linkdb 现在是可选的，所以您需要在命令⾏中⽤⼀个”linkdb”明确地表示它。 这会发送所有的抓取数据给Solr进⾏索引。更多 信息请运⾏命令bin/nutch solrindex。 如果⼀切顺利，我们现在已经准备好在<br><br>进⾏搜索。如果您想要看 到有Solr创建的原始HTML索引，您需要更改 schema.xml当中定义的content字段为： <field name="content" type="text"<br><br>6.将Solr与Nutch进⾏整合<br><br>cp ${NUTCH_RUNTIME_HOME}/conf/schema.x ml ${APACHE_SOLR_HOME}/example/solr/conf /<br><br>在⽬录${APACHE_SOLR_HOME}/example下 使⽤命令”java -jar start.jar“来重启Solr<br><br>运⾏Solr索引命令： htp:/127.0.0.1 8983/solr/<br><br>htp:/loca lhost:8983/solr/admin/</th>
    <th>0⼈顶 翻译的不错哦!<br><br>kzjnet翻译于 1年前<br><br>顶</th>
  </tr>
</table>


stored="true" indexed="true"/>

本⽂中的所有译⽂仅⽤于学习和交流⽬的，转载请务必注明⽂章译者、出处、和本⽂链接 我们的翻译⼯作遵照 ，如果我们的⼯作有侵犯到您的权益，请及时联系我们

C 协议

# 回⻚⾯顶部发表评论⽹友评论共5条

kzjnet 发表于 2013-01-31 13 59 本翻译中我翻译的部分⾸发于Kzjnet博客（

htp:/blog.kzjnet.com/2013/01/%e7%bf%b%e8%af%91

-nutch%e5%85%a5%e9%97%a8%e6%95%9%e7%a8%8b%ef%bc%8%e4%b8%80%ef%b c%89/

），这是本⼈翻译的第⼀篇⽂章，⽀持⼀下呗。

kzjnet 发表于 2013-07-2 19 16 地址改了。

htp:/ w.kzjnet.com/2013/07/nutch-tutorial-1/

吕明明 发表于 2013-1-01 15 56 楼主好⼈~

guyezhai 发表于 2014-02-14 13 40Exception in thread "main" java.io.IOException: Segment already parsed!

guyezhai 发表于 2014-02-14 17 26

### 引⽤来⾃“guyezhai”的评论

Exception in thread "main" java.io.IOException: Segment already parsed!问题以解决。测试服务器的 外⽹连接出了问题造成抓取失败。

