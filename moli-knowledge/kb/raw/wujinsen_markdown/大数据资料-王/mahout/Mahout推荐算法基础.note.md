Mahout推荐算法分为以下⼏⼤类 GenericUserBasedRecomender 算法：

- 1.基于⽤户的相似度
- 2.相近的⽤户定义与数量 特点：


- 1.易于理解
- 2.⽤户数较少时计算速度快


GenericItemBasedRecomender 算法： 1.基于item的相似度 特点：

- 1.item较少时就算速度更快
- 2.当item的外部概念易于理解和获得是⾮常有⽤


SlopeOneRecomender（itemBased） 算法： 1基于SlopeOne算法（打分差异规则） 特点 速度快 需要预先计算 当item数⽬⼗分少了也很有效 需要限制difs的存储数⽬否则内存增长太快

SVDRecomender （item-based） 算法 基于⽀持向量机（item的特征以向量表⽰，每个维度的评价值） 特点 需要预计算 推荐效果佳

KnItemBasedRecomender （item-based） 类似于GenericUserBasedRecomender 中基于相似⽤户的实现（基于相似的item）

与GenericItemBasedRecomender 的主要区别是权重⽅式计算的不同（but, the weights are not the results of some similarity metric. Instead, the algorithm calculates the optimal set of weights to use betwen al pairs of items=>看的费劲）

TreClusteringRecomender 算法 基于树形聚类的推荐算法 特点 ⽤户数⽬少的时候⾮常合适 计算速度快 需要预先计算

基于模型的推荐算法、基于满意度得推荐算法（未实现）

Mahout中的数据输⼊ DataModel 以下包含 GenericDataModel 数据接⼜类 基于内存 内部使⽤FastByIDMap 保存PreferenceAray，在PreferenceAray内保存⽤户->Item的评价值

GenericBoleanPrefDataModel. 基于内存的数据接⼜类 但是⽆⽤户偏好值 使⽤FastByIDMap<FastIDSet>为⽤户或者Item保存相关的Item或者⽤户。

FileDataModel 基于⽂件的数据接⼜内，内部使⽤GenericDataModel 保存实际的⽤户评价数据 增加了压缩⽂件（.zip .gz）等⽂件类型的⽀持 ⽀持动态更新（更新⽂件⽂件名必须保存为⼀定的格式 例如 fo.txt.gz 后续更新⽂件必须为 fo.1.txt.gz） 查了以下代码 好像是⾃定义时间间隔后可以更新，但是好像是全部更新（以后看代码）

JDBCDataModel 基于数据库的数据接⼜ ⽬前已经实现MySQLJDBCDataModel（⽀持MySQL 5.x）可以使⽤ MysqlDataSource⽣成MySQLJDBCDataModel

注：0.7版本⾥⾯没有找到MySQLJDBCDataModel类多了⼀个MySQLJDBCIDMigrator 不知道关系如何

PlusAnonymousUserDataModel. ⽤于匿名⽤户推荐的数据类 将全部匿名⽤户视为⼀个⽤户（内部包装其他的DataModel类型）

Mahout中的相似度计算 主要按照基于User，基于Item等

包含内部类 包含内部类

GenericItemSimilarity GenericItemSimilarity.ItemItemSimilarity GenericUserSimilarity GenericUserSimilarity.UserUserSimilarity

以内存⽅式保存相似度计算结果 使⽤FastByIDMap<FastByIDMap<Double>保存计算结果

CachingItemSimilarity CachingUserSimilarity

以cache⽅式保存相似度计算结果防⽌每次请求是重复计算 内部使⽤ Cache<LongPair,Double> similarityCache保存相似度 与 ⽤法和区别暂时看不懂

GenericUserSimilarity

Mathout中实现的基于不同算法相似度度量的： PearsonCorelationSimilarity ⽪尔逊距离 EuclideanDistanceSimilarity 欧⼏⾥德距离 CosineMeasureSimilarity 余弦距离（0.7变成了 ） SpearmanCorelationSimilarity 斯⽪尔曼等级相关 TanimotoCoeficientSimilarity ⾕本相关系数 LogLikelihodSimilarity ⼀般好于TanimotoCoeficientSimilarity（不懂）

UncenteredCosineSimilarity

基于曼哈顿距离

CityBlockSimilarity

相似度使⽤的典型⽤法 UserSimilarity similarity = new CachingUserSimilarity( new SpearmanCorelationSimilarity(model), model);

对缺失数据的处理 PreferenceInferer 数据丢失或者数据太少时可能⽤到 具体实现有 AveragingPreferenceInferer 以平 均值填充缺失数据 ⼀般来说PreferenceInferer除了增加计算量对推荐结果⽆任何影响（缺失值根据已有数据得出）所以 ⼀般只⽤于研究领域。

聚类的相似度 ClusterSimilarity 聚类的相似度⽤于两个不同的聚类之间的距离（类似坐标系内的距离） ⽬前聚类之间的距离计算只包含以下两个实现（暂时没有更好的实现算法）

NearestNeighborClusterSimilarity 计算两个聚类中所有项距离中的最⼩距离 FarthestNeighborClusterSimilarity 计算两个聚类中所有项距离中的最⼤距离

