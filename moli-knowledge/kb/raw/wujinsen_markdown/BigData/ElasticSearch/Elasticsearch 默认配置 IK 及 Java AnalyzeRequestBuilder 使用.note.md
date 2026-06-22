本⽂提纲 ⼀、什么是 Elasticsearch-analysis-ik ⼆、默认配置 IK 三、使⽤ AnalyzeRequestBuilder 获取分词结果 四、⼩结 运⾏环境：JDK 7 或 8、Maven 3.0+、ElasticSearch 2.3.2、Elasticsearch-analysis-ik 1.9.2 技术栈：SpringBot 1.5+、Spring-data-elasticsearch 2.1.0

# 前⾔

在 Elasticsearch 和插件 elasticsearch-head 安装详解 ⽂章中，我使⽤ 的是 Elasticsearch 5.3.x。这⾥我改成了 ElasticSearch 2.3.2。是因为版本对应关系

htp:/ w.bysocket.com/?p=174

htps:/github.com/spri ng-projects/spring-data-elasticsearch/wiki/Spring-Data-Elasticsearch—Spring-Bot—version-matrix

：

Spring Bot Version (x) Spring Data Elasticsearch Version (y) Elasticsearch Version (z) x <= 1.3.5 y <= 1.3.4 z <= 1.7.2* x >= 1.4.x 2.0.0 <=y < 5.0.0* 2.0.0 <= z < 5.0.0*

- * – 只 需 要 你修 改 下 对 应 的 pom ⽂ 件 版 本 号

- * – 下 ⼀ 个 ES 的 版 本 会 有 重 ⼤ 的 更 新


这⾥可以看出，5.3.x 不在第⼆⾏范围内。因此这⾥我讲下，如何在 ElasticSearch 2.3.2 中默认配置 IK。

# ⼀、什么是 Elasticsearch-analysis-ik

了解什么是 Elasticsearch-analysis-ik，⾸先了解什么是 IK Analyzer。 IK Analyzer 是基于 lucene 实现的分 词开源框架。官⽅地址： 。 Elasticsearch-analysis-ik 则是将 IK Analyzer 集成 Elasticsearch 的插件，并⽀持⾃定义词典。GitHub 地 址： 。特性⽀持：

htps:/code.gogle.com/p/ik-analyzer/

htps:/github.com/medcl/elasticsearch-analysis-ik

分析器 Analyzer: ik_smart 或 ik_max_word 分词器 Tokenizer: ik_smart 或 ik_max_word

# ⼆、默认配置 IK

在 Elasticsearch-analysis-ik 官⽹中可以看到，其中版本需要对应：

IK版 ES版本 主 5.x -> master 5.3.2 5.3.2 5.2.2 5.2.2 5.1.2 5.1.2 1.10.1 2.4.1 1.9.5 2.3.5 1.8.1 2.2.1 1.7.0 2.1.1 1.5.0 2.0.0 1.2.6 1.0.0 1.2.5 0.90.x 1.1.3 0.20.x 1.0.0 0.16.2 -> 0.19.0

htps:/github.com/m edcl/elasticsearch-analysis-ik/releases/download/v1.9.2/elasticsearch-analysis-ik-1.9.2.zip

这⾥使⽤的是 Elasticsearch-analysis-ik 1.9.2，⽀持 ElasticSearch 2.3.2。下载地址：

，下载成功后

进⾏安装。 解压 zip ⽂件，复制⾥⾯的内容到 elasticsearch-2.3.2/plugins/ik。

cd elasticsearch-2.3.2/plugins mkdir ik cp ...

在 elasticsearch-2.3.2/config/elasticsearch.yml 增加配置：

index.analysis.analyzer.default.tokenizer : "ik_max_word" index.analysis.analyzer.default.type: "ik"

配置默认分词器为 ik，并指定分词器为 ik_max_word。

然后重启 ES 即可。验证 IK 是否成功安装，访问下

localhost:920/_analyze?analyzer=ik&pretty=true&text=泥瓦匠的博客是bysocket.com

可以得到下⾯的结果集：

{

"tokens": [ {

"token": "泥瓦匠", "start_offset": 0, "end_offset": 3, "type": "CN_WORD",

- "position": 0

}, {

"token": "泥", "start_offset": 0, "end_offset": 1, "type": "CN_WORD",

- "position": 1

}, {

"token": "瓦匠", "start_offset": 1, "end_offset": 3, "type": "CN_WORD",

- "position": 2

}, {

"token": "匠", "start_offset": 2, "end_offset": 3, "type": "CN_WORD",

- "position": 3

}, {

"token": "博客", "start_offset": 4, "end_offset": 6, "type": "CN_WORD",

- "position": 4

}, {

"token": "bysocket.com",

"start_offset": 8, "end_offset": 20, "type": "LETER",

- "position": 5

}, {

"token": "bysocket", "start_offset": 8, "end_offset": 16, "type": "ENGLISH",

- "position": 6

}, {

"token": "com", "start_offset": 17, "end_offset": 20, "type": "ENGLISH",

- "position": 7


} ]

}

记得在Docker 容器安装时，需要对应的端⼝开发。

# 三、使⽤ AnalyzeRequestBuilder 获取分词结果

ES 中默认配置 IK 后，通过 Rest HTP 的⽅式我们可以进⾏得到分词结果。那么在 Spring Bot 和提供的客 户端依赖 spring-data-elasticsearch 中如何获取到分词结果。 加⼊依赖 pom.xml

<!-- Spring Bot Elasticsearch 依赖 -->

<dependency> <groupId>org.springframework.bot</groupId> <artifactId>spring-bot-starter-data-elasticsearch</artifactId>

</dependency>

在 aplication.properties 配置 ES 的地址：

# ES spring.data.elasticsearch.repositories.enabled = true spring.data.elasticsearch.cluster-nodes = 127.0.0.1 930

然后创建⼀个⽅法，⼊参是搜索词，返回的是分词结果列表。

@Autowired private ElasticsearchTemplate elasticsearchTemplate;

/**

- * 调⽤ ES 获取 IK 分词后结果

*

- * @param searchContent

- * @return

- */ private List<String> getIkAnalyzeSearchTerms(String searchContent) {


// 调⽤ IK 分词分词 AnalyzeRequestBuilder ikRequest = new AnalyzeRequestBuilder(elasticsearchTemplate.getClient(),

AnalyzeAction.INSTANCE,"indexName",searchContent); ikRequest.setTokenizer("ik"); List<AnalyzeResponse.AnalyzeToken> ikTokenList = ikRequest.execute().actionGet().getTokens();

// 循环赋值 List<String> searchTermList = new ArrayList<>(); ikTokenList.forEach(ikToken -> { searchTermList.ad(ikToken.getTerm()); });

return searchTermList; }

indexName 这⾥是指在 ES 设置的索引名称。 从容器注⼊的 ElasticsearchTemplate Bean 中获取 Client ，再通过 AnalyzeRequestBuilder 分析请求类型中 进⾏分词并获取分词结果 AnalyzeResponse.AnalyzeToken 列表。

# 四、⼩结

默认配置了 IK 分词器，则 DSL 去 ES 查询时会⾃动调⽤ IK 分词。 如果想要⾃定义词库，⽐如⽐较偏的领域性。可以参考 Elasticsearch-analysis-ik GiHub 地址去具体查阅。 推荐开源项⽬：《 》 spring bot 实践学习案例，是 spring bot 初学者及核⼼技术巩固的最佳实践

springbot-learning-example

欢迎扫⼀扫我的公众号关注 — 及时得到博客订阅哦！

— — —

htp:/ w.bysocket.com/ htps:/github.com/JefLi193

