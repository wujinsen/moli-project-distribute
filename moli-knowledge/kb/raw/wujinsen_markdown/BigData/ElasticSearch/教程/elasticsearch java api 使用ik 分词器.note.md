htps:/blog.csdn.net/zhanlanmg/article/details/48732121

本⽂主要说明如何在java中使⽤ ik 分词器 安装分词器⻅：

elasticsearch 安装 analysis-ik

ElasticSearchjavaAPI–创建maping

package Index; import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest; import org.elasticsearch.client.Client; import org.elasticsearch.client.Requests; import org.elasticsearch.common.xcontent.XContentBuilder; import org.elasticsearch.common.xcontent.XContentFactory; import Client.ServerClient;

public class createIndex {

private static Client client=ServerClient.getClient();

/**

- * 创建索引名称

- * @param indices 索引名称

- */


public static void createCluterName(String indices){ client.admin().indices().prepareCreate(indices).execute().actionGet(); client.close();

}

/**

* 创建mapping(feid("indexAnalyzer","ik")该字段分词IK索引 ；feid("searchAnalyzer","ik")该字 段分词ik查询；具体分词插件请看IK分词插件说明)

- * @param indices 索引名称；

- * @param mappingType 索引类型

- * @throws Exception

- */


public static void createMapping(String indices,String mappingType)throws Exception{ new XContentFactory(); XContentBuilder builder=XContentFactory.jsonBuilder()

.startObject()

.startObject(indices)

.startObject("properties")

.startObject("id").field("type", "integer").field("store", "yes").endObject()

.startObject("kw").field("type", "string").field("store", "yes").field("indexAnalyzer", "ik").field("searchAnalyzer", "ik").endObject()

.startObject("edate").field("type", "date").field("store", "yes").field("indexAnalyzer", "ik").field("searchAnalyzer", "ik").endObject()

.endObject() .endObject() .endObject();

PutMappingRequest mapping =

Requests.putMappingRequest(indices).type(mappingType).source(builder); client.admin().indices().putMapping(mapping).actionGet(); client.close();

} public static void main(String[] args)throws Exception {

createMapping("lianan", "lianan"); createCluterName("lianan");

}

} //field("store", "yes")并不是必需的，但是field("type", "string")是必需要存在的，不然会报错。

注 ： 分 词 是 没 有 结 果 的 ， 所 以 存 ⼊ 的 字 符 串 还 是 我 们 写 ⼊ 的 ， 只 是 在 查 询 时 会 去 调 ⽤ 对 应 的 分 词 器 去 处 理

# java中调⽤ik去分词

## java查询分词结果

IndicesAdminClient indicesAdminClient = ElasticFactory.getClient().admin().indices(); AnalyzeRequestBuilder request = new AnalyzeRequestBuilder(indicesAdminClient,"cloud_repair","中华⼈⺠共和国国歌"); // request.setAnalyzer("ik"); request.setTokenizer("ik"); // Analyzer（分析器）、Tokenizer（分词器） List listAnalysis = request.execute().actionGet().getTokens();

System.out.println(listAnalysis); // listAnalysis中的结果就是分词的结果

## 查询

for (AnalyzeResponse.AnalyzeToken term : listAnalysis) { System.out.print(term.getTerm()); System.out.print(','); queryBuilder.should(QueryBuilders.queryString(term.getTerm()).field("search_keys_ik")); //这⾥可以⽤must 或者 should 视情况⽽定

} System.out.print('\n');

分词结果 listAnalysis 中的内容和 中的结果⼀样

RestApi

# 分词器和分析器

Analyzer（分析器）、Tokenizer（分词器）这两个概念以后弄懂了再发出来，可以参考其它⽂章去了 解。 在 IK中 ， 只 有 Tokenizer。 Analyzer只 是 构 造 了 ⼀ 个 Tokenizer去 处 理 的 。 ⻅ 源 码 ： org.wltea.analyzer.lucene.IKAnalyzer 此类中有⼀个覆盖⽅法：

/**

- * 重载Analyzer接⼝，构造分词组件

- */


@Override protected TokenStreamComponents createComponents(String fieldName, final Reader in) {

Tokenizer _IKTokenizer = new IKTokenizer(in , settings, environment); return new TokenStreamComponents(_IKTokenizer);

}

参考原⽂：

-

elasticsearch maping ElasticSearch java API–创建maping solr学习之六⸺–Analyzer（分析器）、Tokenizer（分词器） Lucene源码解析–Analyzer之Tokenizer 全⽂检索的⼏个重要概念: Analyzer, tokenizer, token filter, char filter

