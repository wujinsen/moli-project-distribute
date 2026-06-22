Lucene 3.5最新版在201-1-26⽇发布了。 下载地址： Lucene进⾏了⼤量优化、改进和Bug的修复，包括：

htp:/labs.renren.com/apache-miror/lucene/java/3.5.0/

- 1. ⼤⼤降低了控制开放的IndexReader上的协议索引的RAM占⽤（3~5倍）。
- 2. 新增IndexSearcher.searchAfter，可在指定ScoreDoc后返回结果（例如之前⻚⾯的最后⼀个⽂ 档），以⽀持dep⻚⽤例。
- 3. 新增SearcherManager，以管理共享和重新开始跨多个搜索线程的IndexSearchers。 基本的IndexReader实例如果不再进⾏引⽤，则会被安全关闭。
- 4. 新增SearcherLifetimeManager，为跨多个请求（例如：paging/drildown）的索引安 全地提供了⼀个⼀致的视图。
- 5. 将IndexWriter.optimize重命名为forceMerge，以便去阻⽌使⽤这种⽅法，因为它的 使⽤代价较⾼，且也不需要使⽤。 6. 新增NGramPhraseQuery，当使⽤n-gram分析时，可提升 30%-50%的短语查询速度。 7. 重新开放了⼀个API（IndexReader.openIfChanged），如果索引没有 变化，则返回空值，⽽不是旧的reader。


- 8. Vector改进：⽀持更多查询，如通配符和⽤于产⽣摘要的边界分析。
- 9. 修复了若⼲Bug。


针对做出⼀个简单的搜索引擎，笔者针对遇到的问题进⾏探讨：

- 1. 关于查询关键字的问题： String queryStr =”中国”; QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_35, fields, luceneAnalyzer); Query query = queryParser.parse(queryString); Lucene对这个查询是不分⼤⼩写的，当搜索关键字为英⽂加数字或汉字或其他字符的时候， 例如：“swing12”、“swing我sd”等，Lucene会先对这个关键字进⾏分词，即分成英⽂+数字或汉字的 形式，然后去索引， 这样docment中含有”swing”和”12”的Field都被索引出来了。可以达到模糊查询，若想要精确查询请往 下看。
- 2. 针对Lucene在显示查询结果时，通过⾼亮显示功能把doc.get( "Content ")中的内容显示字符不带标 点的问题。 因为lucene在做索引的时候是要先切分的，你如果事先切分的时候就去掉了标点符号，那么你搜索出 结果就不会有标点了。 所以当你使⽤lucene⾃带的分析器的时候要注意，笔者使⽤的是IK分词即可解决这⼀问题。
- 3. 针对Lucene搜素时查询出结果的显示个数问题。 创建结果⽂档收集器： public static TopScoreDocColector create(int numHits, bolean docsScoredInOrder);


意思是其根据是否按照⽂档号从⼩到⼤返回⽂档⽽创建，false不按照⽂档号从⼩到⼤。numHits返回 定义要取出的⽂档数。 ⽽搜集⽂档号函数： public void score(Colector colector); 当创建完毕Scorer对象树和SumScorer对象树后，⽤： scorer.score(colector) 其不断的得到合并的倒排表后的⽂档号，并收集它们。

例：

publicstaticvoid search() throws Exception{ String queryString = "test1"; String[] fields = {"id","content"}; QueryParser queryParser = new

MultiFieldQueryParser(Version.LUCENE_35, fields, luceneAnalyzer); Query query = queryParser.parse(queryString); IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexpath);

/IndexReader reader = IndexReader.open(indexDir); IndexSearcher searcher = new IndexSearcher(reader); TopScoreDocColector results = TopScoreDocColector.create(10, false); /判断是否按照从⼩

到⼤顺序排列⽂档号,并取出10条记录

(随即取)；如果为True，则取前10条。 searcher.search(query, results);

TopDocs topDocs = results.topDocs(0, 10); /查询前10条放⼊结果集。

for(int j=0 j<topDocs.scoreDocs.length; j +) { ScoreDoc scoreDoc = topDocs.scoreDocs[j]; Document doc = searcher.doc(scoreDoc.doc);

} }

} 由代码我们可以知道： colector的作⽤就是⾸先计算⽂档的打分，然后根据打分，将⽂档放⼊优先级队列(最⼩堆)中，最后在 优先级队列中取前N篇⽂档。 然⽽存在⼀个问题，如果要取10篇⽂档，⽽第8,9,10,1,12篇⽂档的打分都相同，则抛弃那些？ Lucene的策略是，在⽂档打分相同的情况下，⽂档号⼩的优先。 也即8,9,10被保留， 1,12被抛弃。 由上⾯的叙述可知，创建colector的时候，根据⽂档是否将按照⽂档号从⼩到⼤的顺序返回⽽创建 InOrderTopScoreDocColector或者OutOfOrderTopScoreDocColector。

对于InOrderTopScoreDocColector，由于⽂档是按照顺序返回的，后来的⽂档号肯定⼤于前⾯的⽂档 号，

因⽽当score <= pqTop.score的时候，直接抛弃。 对于OutOfOrderTopScoreDocColector，由于⽂档不是按顺序返回的， 因⽽当score<pqTop.score，⾃然直接抛弃，当score=pqTop.score的时候，则要⽐较后来的⽂档和 前⾯的⽂档的⼤⼩，如果⼤于，则抛弃， 如果⼩于则⼊队列。 4. Lucene的打分机制： BoleanScorer2的打分函数如下： 将⼦语句的打分乘以cord(⼀个⽂章中包含的关键字越多则打分越 ⾼) public float score() throws IOException { cordinator.nrMatchers = 0;

float sum = countingSumScorer.score();/当前⽂档的得分 return sum * cordinator.cordFactors[cordinator.nrMatchers];/cord }

ConjunctionScorer的打分函数如下： 将取交集的⼦语句的打分相加，然后乘以 cord public float score() throws IOException { float sum = 0.0f;

for (int i = 0; i < scorers.length; i +) { sum += scorers[i].score(); } return sum * cord; }

DisjunctionSumScorer的打分函数如下： public float score() throws IOException { return curentScore; } curentScore计算如下： curentScore += scorerDocQueue.topScore(); 以上计算是在DisjunctionSumScorer的倒排表合并算法中进⾏的，其是取堆顶的打分函 数。 public final float topScore() throws IOException { return topHSD.scorer.score(); } ReqExclScorer的打分函数如下： 仅仅取required语句的打分 public float score() throws IOException { return reqScorer.score(); } ReqOptSumScorer的打分函数如下： 上⾯曾经指出，ReqOptSumScorer的nextDoc()函数仅仅返回required语句的⽂档号。⽽optional的部 分仅仅在打分的时候有所体现，从下⾯的实现可以看出optional的语句的分数加

到required语句的分数上，也即⽂档还是required语句包含的⽂档，只不过是当此⽂档能够满⾜ optional的语句的时候，打分得到增加。 public float score() throws IOException { int curDoc = reqScorer.docID(); float reqScore = reqSco rer.score(); if (optScorer = nul) { return reqScore; }

int optScorerDoc = optScorer.docID();

if (optScorerDoc < curDoc & (optScorerDoc = optScorer.advance(curDoc) = NO_MORE_DOCS) { optScorer = nul; return reqScore; }

return optScorerDoc = curDoc ? reqScore + optScorer.score() : reqScore; } TermScorer的打分函数如下： 整个Scorer及SumScorer对象树的打分计算，最终都会源⾃叶⼦节点TermScorer上。从TermScorer的 计算可以看出，它计算出 tf * norm * weightValue = tf * norm * queryNorm * idf^2 * t.getBost() public float score() { int f = freqs[pointer];

float raw = f < SCORE_CACHE_SIZE ? scoreCache[f] : getSimilarity().tf(f)*weightValue; return norms = nul ? raw : raw * SIM_NORM_DECODER[norms[doc] & 0xF]; }

Lucene的打分公式整体如下，2.4.1计算了图中的红⾊的部分，此步计算了蓝⾊的部分： Cord(q,d)因⼦ d 代表docment中的filed个数，q为查询匹配的个数。 Cord(q,d) = q/d 即为⼀个docment中关键字多 少的得分。 queryNorm(q)是查询权重对得分的影响。 queryNorm(q) = queryNorm(sumOfSquaredWeights)=1/(sumOfSquaredWeights^(1/2) sumOfSquaredWeights= q.getBost()^2·∑( idf(t)·t.getBost() )^2 t即为term，t in q 即为在查询中 出现的term。 q.getBost()是⼀个查询⼦句被赋予的bost值，因为Lucene中任何⼀个Query对象是可以通过 setBost(bost)⽅法设置⼀个bost值的。例如：

- BoleanQuery bq1 = new BoleanQuery(); / 第⼀个BoleanQuery查询⼦句

- TermQuery tq1 = new TermQuery(new Term("title", "search"); tq1.setBost(2.0f);

- bq1.ad(tq1, Ocur.MUST);


- TermQuery tq2 = new TermQuery(new Term("content", "lucene"); tq2.setBost(5.0f); bq1.ad(t q2, Ocur.MUST); bq1.setBost(0.1f);


/ 给第⼀个查询⼦句乘上0.1，实际是减弱了其贡献得分的重要性

- BoleanQuery bq2 = new BoleanQuery(); / 第⼆个BoleanQuery查询⼦ 句 TermQuery tq3 = new TermQuery(new Term("title", "bok"); tq3.setBost(8.0f); bq2.ad(tq
- 3, Ocur.MUST);


TermQuery tq4 = new TermQuery(new Term("content", "lucene"); tq4.setBost(5.0f); bq2.ad(t q4, Ocur.MUST);

- bq2.setBost(10.0f); / 给第⼆个查询⼦句乘上10.0，该⼦句更重 要 BoleanQuery bq = new BoleanQuery(); / 对上述两个BoleanQuery查询⼦句再进⾏OR运 算 bq.ad(bq1, Ocur.SHOULD); bq.ad(bq2, Ocur.SHOULD); 例⼦代码意思：：“我想要查询包含Lucene的⽂章，但标题最好是含有bok的”，也就是说“我想查找 介绍Lucene的书籍，如果没有没有关于Lucene的书籍，包含介绍Lucene查询search的⽂章也可以”。 所以上述两个布尔查询⼦句设置的bost值（0.1<10.0），就对应于我们上述公式中的 q.getBost()。 idf(t)就是反转⽂档频率，含义是如果⽂档中出现Term的频率越⾼显得⽂档越不重要，Lucene中计算该 值的公式如下： idf(t) = 1.0 + log(numDocs/(docFreq+1) 其中，numDocs表示索引中⽂档的总数，docFreq表示查询中Term在多个⽂档中出现。 t.getBost()表示查询中的Term给予的bost值，例如上⾯代码中：


- TermQuery tq3 = new TermQuery(new Term("title", "bok"); tq3.setBost(8.0f); title中包含bok的Term，对匹配上的⽂档，通过上⾯公式计算，乘上t.getBost()的值。 ∑( tf(t in d)·idf(t)^2·t.getBost()·norm(t,d) )因⼦ 上⾯t还是在q中出现的Term即t in q。 norm(t,d)的含义，计算公式如下所示： norm(t,d) = doc.getBost()· lengthNorm· ∏ f.getBost() norm(t,d)是在索引时（index-time）进⾏计算并存储的，在查询时（search-time）是⽆法再改变的， 除⾮再重建索引。另外，Lucene在索引时存储norm值，⽽且是被压缩存储的，在查询时取出该值进⾏ ⽂档相关度计算，即⽂档得分计算。


