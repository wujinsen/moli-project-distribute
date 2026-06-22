## match查询是如何使⽤bool查询的

现在，你也许意识到了使⽤了 只是简单地将⽣成的term查询包含在了⼀个bool查 询中。通过默认的or操作符，每个term查询都以⼀个语句被添加，所以⾄少⼀个should语句需要被匹 配。以下两个查询是等价的：

match查询的多词查询

{

"match": { "title": "brown fox"} }

{

"bool": {

"should": [ { "term": { "title": "brown" }}, { "term": { "title": "fox" }}

] }

} 使⽤and操作符时，所有的term查询都以must语句被添加，因此所有的查询都需要匹配。以下两个查询 是等价的： {

"match": {

"title": { "query": "brown fox", "operator": "and"

} }

}

{

"bool": {

"must": [ { "term": { "title": "brown" }}, { "term": { "title": "fox" }}

] }

} 如果指定了minimum_should_match参数，它会直接被传⼊到bool查询中，因此下⾯两个查询是等价的：

{

"match": {

"title": { "query": "quick brown fox", "minimum_should_match": "75%"

} }

}

{

"bool": {

"should": [ { "term": { "title": "brown" }}, { "term": { "title": "fox" }}, { "term": { "title": "quick" }}

], "minimum_should_match": 2

}

} 因为只有3个查询语句，minimum_should_match的值75%会被向下舍⼊到2。即⾄少两个should语句需要 匹配。 当然，我们可以通过match查询来编写这类查询，但是理解match查询的内部⼯作原理能够让你根据需 要来控制该过程。有些⾏为⽆法通过⼀个match查询完成，⽐如对部分查询词条给予更多的权重。在下 ⼀节中我们会看到⼀个例⼦。

# 提升查询⼦句(Boosting Query Clause)

当然，bool查询并不是只能合并简单的单词(One-word)match查询。它能够合并任何其它的查询，包括 其它的bool查询。它通常被⽤来通过合并数个单独的查询的分值来调优每份⽂档的相关度_score。 假设我们需要搜索和"full-text search"相关的⽂档，但是我们想要给予那些提到了"Elasticsearch"或 者"Lucene"的⽂档更多权重。更多权重的意思是，对于提到了"Elasticsearch"或者"Lucene"的⽂档，它 们的相关度_score会更⾼，即它们会出现在结果列表的前⾯。 ⼀个简单的bool查询能够让我们表达较为复杂的逻辑：

{ "match": { "content": "Elasticsearch" }}, { "match": { "content": "Lucene" }}

] }

} }

- 1.
- 2.


content字段必须含有full，text和search这三个词条 如果content字段也含有了词条Elasticsearch或者Lucene，那么该⽂档会有⼀个较⾼的_score

should查询⼦句的匹配数量越多，那么⽂档的相关度就越⾼。⽬前为⽌还不错。 但是如果我们想给含有Lucene的⽂档多⼀些权重，同时给含有Elasticsearch的⽂档更多⼀些权重呢？ 我们可以通过指定⼀个boost值来控制每个查询⼦句的相对权重，该值默认为1。⼀个⼤于1的boost会增 加该查询⼦句的相对权重。因此我们可以将上述查询重写如下：

{ "match": {

"content": { "query": "Elasticsearch", "boost": 3

}

}}, { "match": {

"content": { "query": "Lucene", "boost": 2

} }}

] }

} }

NOTE boost参数被⽤来增加⼀个⼦句的相对权重(当boost⼤于1时)，或者减⼩相对权重(当boost介于0到1 时)，但是增加或者减⼩不是线性的。换⾔之，boost设为2并不会让最终的_score加倍。 相反，新的_score会在适⽤了boost后被归⼀化(Normalized)。每种查询都有⾃⼰的归⼀化算法 (Normalization Algorithm)，算法的细节超出了本书的讨论范围。但是能够说⼀个⾼的boost值会产 ⽣⼀个⾼的_score。 如果你在实现你⾃⼰的不基于TF/IDF的相关度分值模型并且你需要对提升过程拥有更多的控制，你 可以使⽤ ，它不通过归⼀化步骤对⽂档的boost进⾏操作。

function_score查询

多字段查询(Multifield Search)

在下⼀章中，我们会介绍其它的⽤于合并查询的⽅法， 。但是，⾸先让 我们看看查询的另⼀个重要特定：⽂本分析(Text Analysis)。

