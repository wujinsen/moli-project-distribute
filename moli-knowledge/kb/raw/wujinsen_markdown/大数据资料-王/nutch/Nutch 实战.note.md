# 基本信息

Nutch是⼀个开放源代码（open-source）的Java搜索引擎包，它提供了构建⼀个搜索引擎所需要的全 部⼯具和功能。使⽤Nutch不仅可以建⽴⾃⼰内部⽹的搜索引擎，同时也可以针对整个⽹络建⽴搜索引 擎。除了基本的功能之外，Nutch也还有不少⾃⼰的特⾊，如Map-Reduce、Hadop、Plugin等。

回⻚⾸

# Nutch的总体结构

Nutch从总体上看来，分为三个主要的部分：爬⾏、索引和搜索，各部分之间的关系如图1所示。Web db是Nutch初始运⾏的URL集合；Fetcher是⽤来抓取⽹⻚的爬⾏器，也就是平时常说的Crawler； indexer是⽤来建⽴索引的部分，它将会⽣成的索引⽂件并存放在系统之中；searcher是查询器，⽤来 完成对某⼀词条的搜索并返回结果。

- 图 1. Nutch 总体结构


![image 1](<Nutch 实战.note_images/imageFile1.png>)

回⻚⾸

# Nutch的运⾏流程

在了解了 Nutch 的总体结构之后，再详细的看看 Nutch 具体是如何运⾏的？Nutch 的运⾏流程如图2 所示。

- 1. 将起始 URL 集合注⼊到 Nutch 系统之中。
- 2. ⽣成⽚段⽂件，其中包含了将要抓取的 URL 地址。
- 3. 根据URL地址在互联⽹上抓取相应的内容。
- 4. 解析所抓取到的⽹⻚，并分析其中的⽂本和数据。
- 5. 根据新抓取的⽹⻚中的URL集合来更新起始URL集合，并再次进⾏抓取。
- 6. 同时，对抓取到的⽹⻚内容建⽴索引，⽣成索引⽂件存放在系统之中。


- 图 2. Nutch 的运⾏流程

![image 2](<Nutch 实战.note_images/imageFile2.png>)

回⻚⾸

Nutch的配置和运⾏

下载 Nuch 软件包

下载⻚⾯

解压缩

- 图 3. Nutch 的⽬录结构


从⽤户端来看，Nutch 提供了⼀个基于 Tomcat 的应⽤程序，它允许⽤户输⼊词条，然后 Nutch 会在 已经建⽴好的索引⽂件中进⾏搜索，并将相应的结果返回给⽤户。

Nutch 既可以在 Linux 下运⾏，可以在 Windows 下运⾏，同时还可以在 Eclipse 环境中运⾏。在本部 分中，主要介绍如何在 Eclipse 环境下运⾏ Nutch。

⾸先，应该在 Nutch 的 中下载相应的Nutch软件包，现在最新的版本号是0.9, 通常使⽤的版 本号是 0.8.1。

下载后得到的是⼀个名为 nutch-0.9.tar.gz 的压缩包，使⽤7-Zip可以将其解压缩，解压后得到的⽂件 结构如图3所示。

![image 3](<Nutch 实战.note_images/imageFile3.png>)

在bin⽂件夹下存放的是⽤于命令⾏运⾏的⽂件；Nutch的配置⽂件都放在了conf下，lib是⼀些运⾏所 需要的jar⽂件；plugins下存放的相应的插件；在src⽂件夹中的是Nutch的所有源⽂件；webaps⽂件 夹中存放的是web运⾏相关⽂件；nutch-0.9.war是Nutch所提供的基于Tomcat的应⽤程序包。

## 导⼊源代码

在获得Nutch的源代码之中，就可以将其导⼊到Eclipse环境中，并⽣成⼀个新的java⼯程。导⼊后的代 码结构如图4所示。在导⼊过程中应该注意的是，需要把lib下的所有jar⽂件以及conf⽂件夹都添加到⼯ 程的build path之中。 另外，Nutch还需要另外两个jar⽂件，jid3lib-0.5.1.jar和rtf-parser.jar，请分到到下⾯两个链接下载。

htp:/nutch.cvs.sourceforge.net/nutch/nutch/src/plugin/parse-mp3/lib/ htp:/nutch.cvs.sourceforge.net/nutch/nutch/src/plugin/parse-rtf/lib/

- 图 4. Nutch 的包结构


![image 4](<Nutch 实战.note_images/imageFile4.png>)

## 配置

在正式开始运⾏Nutch之前，还需要做⼀些必要的配置，不然在运⾏时会出错，⽆法按照要求抓取到相 应的⻚⾯。 第⼀个需要修改的⽂件是 nutch-default.xml, 需要将 HTP properties 部分的 htp.agent.name 赋予⼀ 个有意思的字符串；还需要将 plugin properties 部分的 plugin.folders 按照具体的情况做必要修改。清 单 1 和清单 2 分别是本⽂中的 Demo 运⾏时的具体配置情况，供⼤家参考。

- 清单1.

- 1 <!-- HTTP properties -->

- 2 <name>http.agent.name</name>

- 3 <value>testNutch</value>

- 4 <description>Just for Testing

- 5 </description>

- 6 </property>


- 清单2.

- 1 <!-- plugin properties -->

- 2 <property>

- 3 <name>plugin.folders</name>

- 4 <value>plugin</value>

- 5 <description>Directories where nutch plugins are located. Each

- 6 element may be a relative or absolute path. If absolute, it is used

- 7 as is. If relative, it is searched for on the classpath.</description>

- 8 </property>


- 清单3.

- 1 # accept hosts in MY.DOMAIN.NAME

- 2 +^http://([a-z0-9]*\.)*ibm.com/


- 清单4.


其次，需要修改的⽂件是crawl-urlfilter.txt, 将其中的MY.DOMAIN.NAME部分按照实际的域名进⾏修 改。清单3中的配置是对*.ibm.com/域进⾏抓取。

另外，还需要的⼀个操作是在conf⽂件夹下，建⽴⼀个名为prefix-urlfilter.txt的⽂本⽂件，其中的内容 很简单，如清单4所示。

- 1 # prefix-urlfilter.txt file starts here

- 2 http

- 3 # prefix-urlfilter.txt file ends here


## 抓取

在配置完成之后，就可以开始运⾏Nutch的Crawler了，不过，正如本⽂前⾯所述，开始运⾏前还需要 设定初始URL集合。具体的⽅法是建⽴⼀个⽂件夹（本⽂建⽴的⽂件夹名为url），并在其中建⽴⼀个 纯⽂本⽂件（本⽂建⽴的⽂件名为urls.txt），⽂件⽂件中存放了需要抓取的其实URL地址，如“

htp:/ w.ibm.com/

”。 然后在org.apache.nutch.crawl包下的Crawl.java⽂件上点击右键，选择“Run as”，再选择“open run dialog”，在如图5所示的对话框中输⼊运⾏参数，然后点击“Run”。这样系统就可以运⾏了。

- 图 5. 运⾏ Crawler


![image 5](<Nutch 实战.note_images/imageFile5.png>)

在运⾏过程中，会出现很多的log信息，图6和图7是系统运⾏过程中的⼀些截图，从中可以看出正在抓 取的⽹⻚URL地址和抓取速度等⼀些信息。等抓取任务成后，系统会⾃动⽣成相应的索引⽂件，以后 查询器使⽤。在以后的⽂章中，会深⼊探讨相应的话题。

- 图 6. Nutch 运⾏信息 1

![image 6](<Nutch 实战.note_images/imageFile6.png>)

- 图 7. Nutch 运⾏信息 2


![image 7](<Nutch 实战.note_images/imageFile7.png>)

回⻚⾸

# 深⼊分析 Crawl 源代码

在了解了 Nutch 的运⾏过程之后，再来分析 Nutch 内部的运⾏流程是什么样⼦的，以及各个类之间是 如何协同配置的？

## Crawl的⼊⼝

正如在前⽂中所提到的，在运⾏ Crawl 时需要输⼊⼀些必要的参数，并且格式也是⼀定的。具体的⽤ 法是 Crawl <urlDir> [-dir d] [-threads n] [-depth i] [-topN N]。其中，<urlDir> 是必须有的参数； Crawl 是运⾏的主⽂件；-dir 表示存放的⽬标⽂件夹；-threads 表示抓取过程中其中的线程数；depth 表示要抓取的深度层次。 如果在运⾏时不指定这些参数，那么Nutch会默认设定这个参数值。详⻅清单。

- 清单 5.

- 1 Path dir = new Path("crawl-" + getDate());

- 2 int threads = job.getInt("fetcher.threads.fetch", 10);

- 3 int depth = 5;

- 4 int topN = Integer.MAX_VALUE;


- 清单 6.


如果指定了运⾏参数，Nutch会按照以下的⽅式来处理。

- 1 for (int i = 0; i < args.length; i++) {

- 2 if ("-dir".equals(args[i])) {

- 3 dir = new Path(args[i+1]);

- 4 i++;

- 5 } else if ("-threads".equals(args[i])) {

- 6 threads = Integer.parseInt(args[i+1]);

- 7 i++;

- 8 } else if ("-depth".equals(args[i])) {

- 9 depth = Integer.parseInt(args[i+1]);

- 10 i++;

- 11 } else if ("-topN".equals(args[i])) {

- 12 topN = Integer.parseInt(args[i+1]);

- 13 i++;

- 14 } else if (args[i] != null) {

- 15 rootUrlDir = new Path(args[i]);

- 16 }

- 17 }


## ⽣成⽬标⽂件夹

在设定运⾏参数后，经过⼀个必要的处理，Nutch会⽣成若⼲个⽬标⽂件夹⽤来存储不同的⽂件内容， 具体包括：crawlDb，linkDb，segments，indexes和index。

- 清单 7.


- 1 Path crawlDb = new Path(dir + "/crawldb");

- 2 Path linkDb = new Path(dir + "/linkdb");

- 3 Path segments = new Path(dir + "/segments");

- 4 Path indexes = new Path(dir + "/indexes");

- 5 Path index = new Path(dir + "/index");


## 注⼊、抓取和更新

当⽣成了所需要的⽬标⽂件夹之后，Nutch就可以开始抓取⼯作了。当然，在抓取⽹⻚过程中会使⽤功 能类来完成相应的单元⼯作。具体来讲，在注⼊、抓取和更新过程中，会⽤来的功能类有Injector、 Generator、Fetcher、ParseSegment和CrawlDb。 整个过程分为以下⼏个步骤：

注⼊

1 injector.inject(crawlDb, rootUrlDir);

抓取

- 1 Path segment = generator.generate(crawlDb, segments, -1, topN, System

- 2 .currentTimeMillis(), false, false);

- 3 if (segment == null) {

- 4 LOG.info("Stopping at depth=" + i + " - no more URLs to fetch.");

- 5 break;

- 6 }

- 7 fetcher.fetch(segment, threads);


更新

- 1 if (!Fetcher.isParsing(job)) {

- 2 parseSegment.parse(segment);

- 3 }

- 4 crawlDbTool.update(crawlDb, new Path[]{segment}, true, true);


4. 反转、索引、去重及合并 最后的⼯作就是⽣成索引，去重并合并索引。不过，现在⼀般都是会⽣成倒排索引⽂件，所以在建⽴ 索引之前还会有⼀个反转的操作，如清单所示。

- 清单 8.


- 1 linkDbTool.invert(linkDb, segments, true, true, false); // invert links

- 2

- 3 // 索引

- 4 indexer.index(indexes, crawlDb, linkDb, fs.listPaths(segments));

- 5 //去重

- 6 dedup.dedup(new Path[] { indexes });

- 7 //合并

- 8 merger.merge(fs.listPaths(indexes), index, tmpDir);


回⻚⾸

总结

本⽂主要介绍了使⽤Nutch进⾏⽹⻚抓取和建⽴索引⽅⾯的内容，⾸先分析了⽹⻚抓取的过程，然后结 合源代码分析了整个抓取过程的组织和实现。当然了，Nutch是⼀款功能⾮常丰富的开源搜索引擎，关 于其他⽅⾯的内容，将在后⾯的⽂章中⼀⼀介绍。

参考资料

Nutch： Nutch 的官⽅⽹站，上⾯有关于 Nutch 的⼤量资料。

Nutch – Tutorial： Nutch 教程。

Nutch Wiki - Front Page： Nutch 的 Wiki。

Lucene：Lucene 的官⽅⽹站，Lucene 是⼀个与 Nutch 有着密切联系的开源软件包。

Eclispe：htp:/ w.eclipse.org/

Tomcat：htp:/tomcat.apache.org/

# 条评论

请 或 后发表评论。 添加评论: 注意：评论中不⽀持 HTML 语法 有新评论时提醒我剩余 1 0 字符

登录 注册

共有评论 (1) 您好~ 图⽚挂了~看不到呀，现在最新版本是nutch 1.4 ，从⽼版本到1.4的升级过程中，软件包的⽬录结构变 化⽐较⼤，希望您更新下这篇技术⽂档到新版本呀~

