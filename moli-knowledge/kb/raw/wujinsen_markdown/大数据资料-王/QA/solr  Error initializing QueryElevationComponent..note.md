在去年时候学习 ，现在solr版本最新已经到了4.3了，前两天因为⼯作需要在⼀台服务器 上⾯新安装solr，但是⽣产环境是4.0，不过想到是内部测试⽤的，且主要功能就是写⼊，删除，搜 索，与程序上⾯没有太多的深⼊开发，于是还是安装了最新的4.3版本 解压安装启动后，就可以了；这时需要添加colection，添加的colection配置需要与⽣产环境保持⼀ 致，于是复制默认的colection1 的配置信息作为新的colection 复制完成，新的colection events 也添加完成；但是加载时总是报错不能正确加载solrconfig.xml信 息，也知道schema.xml等数据肯定是要修改的，schema.xml配置信息修改完成后还是有这样的问题， 在往solr写⼊数据时⼜再次报错 undefined filed mesage，但是mesage字段确实已经配置好了；检查之后再次重启solr，查看刚才的 events 直接显示 “There exists no core with name “events” 这时去查看⽇志，显示信息

使⽤了solr4.0

<table>
  <tr>
    <th>1</th>
    <th>Caused by: org.apache.solr.common.SolrException: Error initializing QueryElevationComponent.</th>
  </tr>
</table>


<table>
  <tr>
    <th>2<br><br></th>
    <th>at org.apache.solr.handler.component.QueryElevationComponent.inform(Que ryElevationComponent.java:218)</th>
  </tr>
</table>


<table>
  <tr>
    <th>3<br><br></th>
    <th>at org.apache.solr.core.SolrResourceLoader.inform(SolrResourceLoader.ja va:616)</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>at org.apache.solr.core.SolrCore.<init>(SolrCore.java:816)</th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>... 34 more</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>Caused by: java.lang.NumberFormatException: For input string: "MA147LL/A"<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>7<br><br></th>
    <th>at java.lang.NumberFormatException.forInputString(NumberFormatException<br><br>.java:65)</th>
  </tr>
</table>


<table>
  <tr>
    <th>8</th>
    <th>... 36 more</th>
  </tr>
</table>


<table>
  <tr>
    <th>9</th>
    <th>ERROR - 2013-05-22 16:07:06.752; org.apache.solr.common.SolrException; org.apache.solr.common.SolrException: Error CREATEing SolrCore<br><br>'events' : Unable to create core: events<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>at org.apache.solr.handler.admin.CoreAdminHandler.handleCreateAction(Co reAdminHandler.java:524)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th>...</th>
  </tr>
</table>


<table>
  <tr>
    <th>1</th>
    <th>ERROR - 2013-05-22 16:48:46.272; org.apache.solr.common.SolrException; org.apache.solr.common.SolrException: ERROR: [doc=100946] unknown field<br><br>'message'</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>at org.apache.solr.update.DocumentBuilder.toDocument(DocumentBuilder.ja va:313)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3<br><br></th>
    <th>at org.apache.solr.update.AddUpdateCommand.getLuceneDocument(AddUpdateC ommand.java:73)</th>
  </tr>
</table>


正在排查中是，同事Y看到了，直接把solr 默认的colection1 改名为 events ，再次刷新直接挂了

There are no SolrCores running. Using the Solr Admin UI currently requires at least one SolrCore.

1

再次去查找这个问题，很快找到

htp:/stackoverflow.com/questions/13295208/i-unloaded-the-default-solr-colection-by-mistake-f rom-the-solr-admin-ui

编辑example/solr/solr.xml配置⽂件 可以看到已经变为

1 <core name="events" instanceDir="events" />

改为

1 <core name="collection1" instanceDir="collection1" />

保存，重启solr即可； 分析为什么出现这个问题，events colection的配置是错误的，solr初始化所有colection时跳过了 events，⽽默认的colection1⼜改为了events则使⽤的是events⽬录下的配置信息了， ⽽这个配置信息⼜是错误的，所以solr admin 默认为没有 cores 的；修复过程中需要⼿动去修改配置 ⽂件，同事Y打呼⽤户体验太不友好了，不过我说Solr Admin⽤户体验很好啊，很早就不⽀持IE6了 现在⼜回到了上⾯的那个错误，org.apache.solr.comon.SolrException: Eror initializing QueryElevationComponent. 不能初始化，也不能添加events colection core，继续gogle，终于找到⼀篇提示的⽂章

htps:/coderwal.com/p/kwvxhq

Note that if you have enabled the QueryElevationComponent in solrconfig.xml it requires the schema to have a uniqueKey of typeStrField . It canot be, for example, an int field. Otherwise, you wil get exception like:

1 java.lang.NumberFormatException: For input string: "MA147LL/A"

⼤意就是如果你开启了 QueryElevationComponent 功能，但是schema 的uniqueKey类型⼜不 是 string，则报如下错误

java.lang.NumberFormatException: For input string: "MA147LL/A" 这个不就是我 的⽇志⾥⾯的那个错误信息么， 于是编辑example/solr/events/conf/solrconfig.xml配置⽂件 搜索 QueryElevationComponent 关键字，可以看到如下，果然有这个信息

<table>
  <tr>
    <th>1</th>
    <th><!-- Query Elevation Component</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>http: //wiki.apache.org/solr/QueryElevationComponent<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>a search component that enables you to configure the top</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>results for a given query regardless of the normal lucene<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>7</th>
    <th>scoring.</th>
  </tr>
</table>


<table>
  <tr>
    <th>8</th>
    <th>--></th>
  </tr>
</table>


<table>
  <tr>
    <th>9</th>
    <th><searchComponent name= "elevator" ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th><!-- pick a fieldType to analyze queries --></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 1<br><br></th>
    <th><str name= "queryFieldType" >string</str><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1<br><br>2<br></th>
    <th><str name= "config-file" >elevate.xml</str><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>1 3<br><br></th>
    <th></searchComponent></th>
  </tr>
</table>


htp:/wiki.apache.org/solr/QueryElevationComponent

查看⼀下，类似于关键字搜索后，⼀些项的配 置置顶显示 ⽐如百度搜索某个关键字时，搜索框下⾯的推⼴，⼴告相关信息总是被置顶显示

![image 1](<solr  Error initializing QueryElevationComponent..note_images/imageFile1.png>)

# 要配置启⽤这项组件，需要配置elevate.xml，同样是位于example/solr/events/conf/⽬录下 Elevated query results are configured in an external .xml file determined by the configfile argument. An elevate.xml file may l ok like this:

- 1 <elevate>

- 2

- 3 <query text="AAA">

- 4 <doc id="A" />

- 5 <doc id="B" />

- 6 </query>

- 7

- 8 <query text="ipod">

- 9 <doc id="A" />

- 10

- 11 <!-- you can optionally exclude documents from a query result -->

- 12 <doc id="B" exclude="true" />

- 13 </query>

- 14

- 15 </elevate>


For the above configuration, the query “ A” would first return documents A and B, then whatever normaly apears for the same query. For the query “ipod”, it would first return A, and would make sure that B is not in the result set. Note: The uniqueKey field must curently be of type string for the QueryElevationComponent to operate properly. 这就是答案了，uniquekey必须是string类型；⽬前我们项⽬中没有⽤到这项功 能，所以可以选择注释不启⽤

<table>
  <tr>
    <th>1</th>
    <th><!--</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th><searchComponent name= "elevator" ><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th><!-- pick a fieldType to analyze queries --></th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th><str name= "queryFieldType" >string</str><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th><str name= "config-file" >elevate.xml</str><br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th></searchComponent></th>
  </tr>
</table>


<table>
  <tr>
    <th>7</th>
    <th>--></th>
  </tr>
</table>


重启之后，没有初始化失败的错误了，再次往solr加⼊数据⼜有⼀个错误信息 undefined field text

<table>
  <tr>
    <th>1</th>
    <th>ERROR - 2013-05-22 17:59:51.107; org.apache.solr.common.SolrException; org.apache.solr.common.SolrException: undefined field text</th>
  </tr>
</table>


<table>
  <tr>
    <th>2<br><br></th>
    <th>at org.apache.solr.schema.IndexSchema.getDynamicFieldType(IndexSchema.j ava:1211)</th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>at org.apache.solr.schema.IndexSchema$SolrQueryAnalyzer.getWrappedAnaly zer(IndexSchema.java:425)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>at org.apache.lucene.analysis.AnalyzerWrapper.initReader(AnalyzerWrappe r.java:81)<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>5<br><br></th>
    <th>at org.apache.lucene.analysis.Analyzer.tokenStream(Analyzer.java:132)</th>
  </tr>
</table>


gogle 得到结果，就是默认字段需要替换的问题，编辑 example/solr/events/conf/solrconfig.xml 检索 到text内容

- 1 <lst name="defaults">

- 2 <str name="echoParams">explicit</str>

- 3 <int name="rows">10</int>

- 4 <str name="df">text</str>

- 5 </lst>


因为solrconfig.xml等配置⽂件时从colection1复制过来的，默认的default字段匹配是text，所以⽬前 改为我们项⽬所⽤到的字段值mesage 保存⽂件，重启solr，写⼊数据没有问题了，search也正常的 有数据内容返回了 版本的不同，配置⽂件内容也会做⼀些变动与修改；所以可能需要修改的配置不仅仅只是与项⽬ search有关的内容，还有版本与版本之间，新版本默认启⽤的模块所需的配置有关；还有⼀点，多看 ⽇志，多⽤Gogle！

