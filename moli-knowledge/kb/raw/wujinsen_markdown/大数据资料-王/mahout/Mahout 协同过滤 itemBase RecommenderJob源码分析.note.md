Mahout⽀持2种 M/R 的jobs实现itemBase的协同过滤

- I.ItemSimilarityJob

- II.RecommenderJob 下⾯我们对RecommenderJob进⾏分析，版本是mahout-distribution-0.7 源码包位置：org.apache.mahout.cf.taste.hadop.item.RecomenderJob


RecommenderJob前⼏个阶段和ItemSimilarityJob是⼀样的，不过ItemSimilarityJob 计算出item的相似 度矩阵就结束了，⽽RecommenderJob 会继续使⽤相似度矩阵，对每个user计算出应该推荐给他的top N 个items。RecommenderJob 的输⼊也是userID, itemID[, preferencevalue]格式的。 JobRecommenderJob主要由以下⼀系列的Job组成：

- 1 PreparePreferenceMatrixJob（同ItemSimilarityJob） 输⼊： (userId, itemId, pref)

- 1.1 itemIDIndex 将Long型的itemID转成⼀个int型的index

- 1.2 toUserVectors 将输⼊的 (userId, itemId, pref) 转成user向量 USER_VECTORS (userId, VectorWritable<itemId, pref>)

- 1.3 toItemVectors 使⽤ USER_VECTORS 构建item向量 RATING_MATRIX (itemId,VectorWritable<userId,pref>)


- 2 RowSimilarityJob（同ItemSimilarityJob）2.1 normsAndTranspose 计算每个item的norm，并转成user向量 输⼊：RATING_MATRIX


- （1）使⽤similarity.normalize处理每个item向量，使⽤similarity.norm计算每个item的norm，写到 hdfs；

- （2）根据item向量进⾏转置，即输⼊：item-（user，pref），输出：user-（item，pref）。这⼀步的 ⽬的是将同⼀个user喜欢的item对找出来，因为只有两个item有相同的user喜欢，我们才认为它们是相 交的，下⾯才有对它们计算相似度的必要。


- 2.2 pairwiseSimilarity 计算item对之间的相似度


- 输⼊：2.1（2）计算出的user向量user-（item，pref） map：CooccurrencesMapper 使⽤⼀个两层循环，对user向量中两两item，以itemM为key，所有itemM之后的itemN与 itemM的 similarity.aggregate计算值组成的向量为value。 reduce：SimilarityReducer


- （1）叠加相同的两个item在不同⽤户之间的aggregate值，得到itemM-（（ item M+1， aggregate M+1），（ item M+2， aggregate M+2），（ item M+3， aggregate M+3）。。。）


- （2）然后计算itemM和之后所有item之间的相似度。相似度计算使⽤similarity.similarity，第⼀个参数 是两个item的aggregate值，后两个参数是两个item的norm值，norm值在上⼀个Job已经得到。结果是 以itemM为key，所有itemM之后的itemN与 itemM 相似度组成的向量为value，即itemM-（（ item M+1， simi M+1），（ item M+2， simi M+2），（ item M+3， simi M+ 3）。。。） 到这⾥我们实际上是得到了相似度矩阵的斜半部分。


- 2.3 asMatrix 构造完整的相似度矩阵（上⾯得到的只是⼀个斜半部分）


- 输⼊：2.2reduce（2）输出的以itemM为key，所有itemM之后的itemN与之相似度组成的向量 map：UnsymmetrifyMapper

- （1）反转，根据item M-（item M+1，simiM+1）记录item M+1 -（item M，simiM+1）

- （2）使⽤⼀个优先队列求出itemM的top maxSimilaritiesPerRow（可设置参数）个相似item，⽐如 maxSimilaritiesPerRow =2时，可能输出 itemM-（（ item M+1， simi M+1），（ item M+3， simi M+3）） reduce：MergeToTopKSimilaritiesReducer


- （1）对相同的item M，合并上⾯两种向量，这样就形成了完整的相似度矩阵，itemM-（（ item 1， simi 1），（ item 2， simi 2））。。。，（ item N， simi N））。

- （2）使⽤Vectors.topKElements对每个item求top maxSimilaritiesPerRow（可设置参数）个相似 item。可⻅map（2）中的求topN是对这⼀步的⼀个预先优化。 最终输出的是itemM-（（ item A， simi A），（ item B， simi B））。。。，（ item N， simi N））,A到N的个数是maxSimilaritiesPerRow。 ⾄此RowSimilarityJob结束。下⾯就进⼊了和ItemSimilarityJob不同的地⽅。


3 prePartialMultiply1 + prePartialMultiply2 + partialMultiply 这三个job的⼯作是将1.2⽣成的user向量和2.3reduce（2）⽣成的相似度矩阵使⽤相同的item作为key 聚合到⼀起，实际上是为下⾯会提到的矩阵乘法做准备。VectorOrPrefWritable是两种value的统⼀结 构，它包含了相似度矩阵中某个item的⼀列和user向量中对应这个item的（userID， prefValue ）。 [java]

下⾯依次介绍：

- 3.1 prePartialMultiply1

输⼊：2.3reduce（2）⽣成的相似度矩阵。 以item为key，相似度矩阵的⼀⾏包装成⼀个VectorOrPrefWritable为value。矩阵相乘应该使⽤列，但 是对于相似度矩阵，⾏和列是⼀样的。

- 3.2 prePartialMultiply2




view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.


publicfinalclas VectorOrPrefWritable implements Writable { private Vector vector; privatelong userID; privatefloat value;

}

输⼊：1.2⽣成的USER_VECTORS 对user，以每个item为key，userID和对应这个item的prefValue包装成⼀个VectorOrPrefWritable为 value。

- 3.3 partialMultiply 以3.1和3.2的输出为输⼊，聚合到⼀起，⽣成item为key，VectorAndPrefsWritable为value为value。 VectorAndPrefsWritable包含了相似度矩阵中某个item⼀列和⼀个List<Long> userIDs，⼀个 List<Float> values。 [java]

- 4 itemFiltering ⽤户设置过滤某些user，需要将user/item pairs也转成（itemID，VectorAndPrefsWritable）形式

- 5 aggregateAndRecommend ⼀切就绪后，下⾯就开始计算推荐向量了。推荐计算公式如下： Prediction(u,i) = sum(all n from N: similarity(i,n) * rating(u,n)) / sum(all n from N: abs(similarity(i,n))) u = a user i = an item not yet rated by u N = all items similar to i 可以看到，分⼦部分就是⼀个相似度矩阵和user向量的矩阵乘法。对于这个矩阵乘法，实现代码和传 统的矩阵乘法不⼀样，其伪代码： assign R to be the zero vector for each column i in the co-occurrence matrix multiply column vector i by the ith element of the user vector add this vector to R 假设相似度矩阵的⼤⼩是N，则以上代码实际上是对某个user的所有item，将这个item在相似度矩阵中 对应列和user对这个item的prefValue相乘，得到N个向量后，再将这些向量相加，就得到了针对这个⽤ 户的N个item的推荐向量。要实现这些，⾸先要把某个user对所有item的prefValue以及这个item在相似 度矩阵中对应列聚合到⼀起。下⾯看实现：


view plaincopy

1. 2. 3. 4. 5.

publicfinalclas VectorAndPrefsWritable implements Writable { private Vector vector; private List<Long> userIDs; private List<Float> values;

}

- 输⼊：3.3和4的输出 map：PartialMultiplyMapper 将（itemID，VectorAndPrefsWritable）形式转成以userID为key，PrefAndSimilarityColumnWritable为 value。PrefAndSimilarityColumnWritable包含了这个user对⼀个item的prefValue和item在相似度矩阵 中的那列，其实还是使⽤的VectorAndPrefsWritable中的vector和value。 [java]


view plaincopy

- 1.
- 2.
- 3.
- 4.


publicfinalclas PrefAndSimilarityColumnWritable implements Writable { privatefloat prefValue; private Vector similarityColumn;

}

reduce：AggregateAndRecommendReducer 收集到属于这个user的所有 PrefAndSimilarityColumnWritable 后，下⾯就是进⾏矩阵相乘的⼯作。 根据是否设置booleanData，有以下两种操作：

- （1）reduceBooleanData 只是单纯的将所有的PrefAndSimilarityColumnWritable 中的SimilarityColumn相加，没有⽤到itempref。

- （2）reduceNonBooleanData ⽤到item-pref的计算⽅法， 分⼦部分，是矩阵相乘的结果，根据上⾯的伪代码，它是将每个PrefAndSimilarityColumnWritable 中 的SimilarityColumn和 prefValue 的相乘，⽣成多个向量后再将这些向量相加；⽽分⺟是所有的 SimilarityColumn和。下⾯看代码： 代码：


[java]

view plaincopy

- 1.
- 2.
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


for (PrefAndSimilarityColumnWritable prefAndSimilarityColumn : values) { Vector simColumn = prefAndSimilarityColumn.getSimilarityColumn(); float prefValue = prefAndSimilarityColumn.getPrefValue();

/分⼦部分，每个SimilarityColumn和item-pref的乘积⽣成多个向量，然后将这些向量相加 numerators = numerators = nul

? prefValue = BOLEAN_PREF_VALUE ? simColumn.clone() : simColumn.times(prefValu e)

: numerators.plus(prefValue = BOLEAN_PREF_VALUE ? simColumn : simColumn.times (prefValue);

simColumn.asign(ABSOLUTE_VALUES); /分⺟是所有的SimilarityColumn和

denominators = denominators = nul ? simColumn : denominators.plus(simColumn); }

两者相除，就得到了反映推荐可能性的数值。 之后还会使⽤writeRecommendedItems使⽤⼀个优先队列取top推荐，并且将index转成真正的 itemID，最终完成。 在以上分析中，similarity是⼀个VectorSimilarityMeasure接⼝实现，它是⼀个相似度算法接⼝，主要⽅ 法有：

- （1）Vector normalize(Vector vector);

- （2）double norm(Vector vector);

- （3）double aggregate(double nonZeroValueA, double nonZeroValueB);

- （4）double similarity(double summedAggregations, double normA, double normB, int numberOfColumns);

- （5）boolean consider(int numNonZeroEntriesA, int numNonZeroEntriesB, double maxValueA, double maxValueB,


double threshold); 众多的相似度算法就是实现了这个接⼝，⽐如TanimotoCoefficientSimilarity的similarity实现就是： public double similarity(double dots, double normA, double normB, int numberOfColumns) {

return dots / (normA + normB - dots); }

