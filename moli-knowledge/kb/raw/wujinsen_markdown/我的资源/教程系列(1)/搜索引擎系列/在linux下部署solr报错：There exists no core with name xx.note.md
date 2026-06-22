在linux下部署solr报错：There exists no core with namex。

![image 1](<在linux下部署solr报错：There exists no core with name xx.note_images/imageFile1.png>)

根据问题分析思路如下： 1，solr在启动的时候会寻找solrhome下的core的配置，core的配置有两种：⼀种是在solr.xml下进⾏配 置、另⼀种是配置core.properties

solr启动流程

- 1、⼊⼝在servlet类SolrDispatchFilter.init()⽅法
- 2、加载solrhome的路径、启动SolrResourceLoader，加载ConfigSolr，加载 CorePropertiesLocator。
- 3、启动core container容器
- 4、启动HtpShardHandlerFactory等⼀些⼯⼚类，进⾏初始化
- 5、CorePropertiesLocator开始加载core，如果加载到core就会开始加载solrconfig.xml中配置的jar 包；然后读取solrconfig.xml配置⽂件和schema.xml；然后是各种其他的配置⽂件。
- 6、然后是打开某个core的索引库⽂件 Opening new SolrCore。
- 7、然后是实例化各种handler和responsewriter。其中有些的懒加载有些是⽴即加载。


