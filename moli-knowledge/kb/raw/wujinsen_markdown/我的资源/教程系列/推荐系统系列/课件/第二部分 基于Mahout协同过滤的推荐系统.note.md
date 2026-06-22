Mahout是什么

Mahout是⼀个算法库,集成了很多算法。

Apache Mahout 是 Apache Software Foundation（ASF）旗下的⼀个开源项⽬，提供⼀些可扩展 的机器学习领域经典算法的实现，旨在帮助开发⼈员更加⽅便快捷地创建智能应⽤程序。

Mahout项⽬⽬前已经有了多个公共发⾏版本。Mahout包含许多实现，包括聚类、分类、推荐过 滤、频繁⼦项挖掘。

通过使⽤ Apache Hadop 库，Mahout 可以有效地扩展到云中。

Mahout 的创始⼈ Grant Ingersol 介绍了机器学习的基本概念，并演示了如何使⽤ Mahout 来实现 ⽂档集群、提出建议和组织内容。

Mahout能做什么

推荐引擎

在⽬前采⽤的机器学习技术中，推荐引擎是最容易被⼀眼认出来的，也是应⽤范围最⼴的。服务商 或⽹站会根据你过去的⾏为为你推荐书籍、电影或⽂章。 在部署了推荐系统的电⼦商务中，亚⻢逊⼤概是最有名的。亚⻢逊基于⽤户的交易⾏为和⽹站记录 为你推荐你可能喜欢的商品。 ⽽facebok这样的社交⽹络则利⽤推荐技术为你找到最可能尚未关联的朋友。 同时，这⼀技术也被各⼤知名国内⽹站所使⽤，如腾讯、⼈⼈、京东、淘宝。

聚类

顾名思义，物以类聚，⼈以群分。聚类是把具有共同属性的物品进⾏归类。 Google news使⽤聚类技术通过标题把新闻⽂章进⾏分组，从⽽按照逻辑线索来显示新闻，⽽并⾮ 给出所有新闻的原始列表。

分类

分类技术决定了⼀个事物多⼤程度上从属于某种类别或类型，或者多⼤程度上具有或者不具有某些 属性。与聚类⼀样，分类⽆处不在，但更多隐身于幕后。通常这些系统会考察类别中的⼤量实例， 来学习推到出分类的规则。 雅⻁邮箱基于⽤户以前对正常右键和垃圾邮件的报告，以及电⼦右键⾃身的特征，来判别到来的消 息是否是垃圾邮件。

Mahout协同过滤算法 Mahout使⽤了Taste来提⾼协同过滤算法的实现，它是⼀个基于Java实现的可扩展的，⾼效的推荐 引擎。Taste既实现了最基本的基于⽤户的和基于内容的推荐算法，同时也提供了扩展接⼝，使⽤ 户可以⽅便的定义和实现⾃⼰的推荐算法。同时，Taste不仅仅只适⽤于Java应⽤程序，它可以作 为内部服务器的⼀个组件以HTP和Web Service的形式向外界提供推荐的逻辑。Taste的设计使它 能满⾜企业对推荐引擎在性能、灵活性和可扩展性等⽅⾯的要求。 Taste主要包括以下⼏个接⼝：

DataModel 是⽤户喜好信息的抽象接⼝，它的具体实现⽀持从任意类型的数据源抽取⽤户 喜好信息。Taste 默认提供 JDBCDataModel 和 FileDataModel，分别⽀持从数据库和⽂件中读 取⽤户的喜好信息。

u1 item1 0.9 u1 item1 0.9

UserSimilarity 和 ItemSimilarity 。UserSimilarity ⽤于定义两个⽤户间的相似 度，它是基于协同过滤的推荐引擎的核⼼部分，可以⽤来计算⽤户的“邻居”，这⾥我们将与当 前⽤户⼝味相似的⽤户称为他的邻居。ItemSimilarity 类似的，计算内容之间的相似度。

计算⼆维矩阵中的距离

UserNeighborhood ⽤于基于⽤户相似度的推荐⽅法中，推荐的内容是基于找到与当前⽤ 户喜好相似的邻居⽤户的⽅式产⽣的。UserNeighborhod 定义了确定邻居⽤户的⽅法，具体 实现⼀般是基于 UserSimilarity 计算得到的。

两种，⼀种是基于固定数量，⼀种是基于某个阈值的。

Recommender 是推荐引擎的抽象接⼝，Taste 中的核⼼组件。程序中，为它提供⼀个 DataModel，它可以计算出对不同⽤户的推荐内容。实际应⽤中，主要使⽤它的实现类 GenericUserBasedRecomender 或者 GenericItemBasedRecomender，分别实现基于⽤户 相似度的推荐引擎或者基于内容的推荐引擎。

RecommenderEvaluator ：评分器。 RecommenderIRStatsEvaluator ：搜集推荐性能相关的指标，包括准确率、召回率等

等。

DataModel提供了以下⼏种实现：

org.apache.mahout.cf.taste.impl.model.GenericDataModel

org.apache.mahout.cf.taste.impl.model.GenericBoleanPrefDataModel

org.apache.mahout.cf.taste.impl.model.PlusAnonymousUserDataModel

org.apache.mahout.cf.taste.impl.model.file.FileDataModel

org.apache.mahout.cf.taste.impl.model.hbase.HBaseDataModel

org.apache.mahout.cf.taste.impl.model.casandra.CasandraDataModel

org.apache.mahout.cf.taste.impl.model.mongodb.MongoDBDataModel

org.apache.mahout.cf.taste.impl.model.jdbc.SQL92JDBCDataModel org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLJDBCDataModel

org.apache.mahout.cf.taste.impl.model.jdbc.GenericJDBCDataModel

org.apache.mahout.cf.taste.impl.model.jdbc.SQL92BoleanPrefJDBCDataModel org.apache.mahout.cf.taste.impl.model.jdbc.MySQLBoleanPrefJDBCDataModel org.apache.mahout.cf.taste.impl.model.jdbc.PostgreBoleanPrefSQLJDBCDataModel

org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel

从类名上就可以⼤概猜出来每个DataModel的⽤途，奇怪的是竟然没有HDFS的DataModel，有 ⼈实现了⼀个，请参考 。

MAHOUT-1579

UserSimilarity 和 ItemSimilarity 相似度实现有以下⼏种：

CityBlockSimilarity ：基于Manhatan距离相似度 EuclideanDistanceSimilarity ：基于欧⼏⾥德距离计算相似度 LogLikelihoodSimilarity ：基于对数似然⽐的相似度 PearsonCorrelationSimilarity ：基于⽪尔逊相关系数计算相似度 SpearmanCorrelationSimilarity ：基于⽪尔斯曼相关系数相似度 TanimotoCoefficientSimilarity ：基于⾕本系数计算相似度 UncenteredCosineSimilarity ：计算 Cosine 相似度

UserNeighborhod 主要实现有两种：

NearestNUserNeighborhod：对每个⽤户取固定数量N个最近邻居

ThresholdUserNeighborhod：对每个⽤户基于⼀定的限制，取落在相似度限制以内的所有⽤ 户为邻居

Recomender分为以下⼏种实现：

GenericUserBasedRecomender：基于⽤户的推荐引擎

GenericBoleanPrefUserBasedRecomender：基于⽤户的⽆偏好值推荐引擎

GenericItemBasedRecomender：基于物品的推荐引擎

GenericBoleanPrefItemBasedRecomender：基于物品的⽆偏好值推荐引擎

RecomenderEvaluator有以下⼏种实现：

AverageAbsoluteDifferenceRecommenderEvaluator ：计算平均差值 RMSRecommenderEvaluator ：计算均⽅根差

Mahout协同过滤算法编程创建Maven项⽬：详⻅《创建⼀个Maven项⽬》导⼊Mahout依赖

![image 1](<第二部分 基于Mahout协同过滤的推荐系统.note_images/imageFile1.png>)

下载电影评分数据 GroupLens ( 是⼀个研究项⽬，它提供⼏个不同型 号的数据集合，每⼀个都来⾃于真实的⽤户对电影的评分。这是⼏个有效的⼤型的真实世界的数据库 中之⼀，在这本书中我们将会探究更多这种数据集合。从grouplens.org查找并下载“10K data set”，

htp:/grouplens.org/)

htp:/grouplens.org/datasets/movielens/

下载地址： 数据类别：7.2万⽤户对1万部电影的百万级评价和10万个标签数据

![image 2](<第二部分 基于Mahout协同过滤的推荐系统.note_images/imageFile2.png>)

本例数据：本例中只需要使⽤评分数据

![image 3](<第二部分 基于Mahout协同过滤的推荐系统.note_images/imageFile3.png>)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


user_id movie_id rating timestamp

- 0 1 193 5 97830760

- 1 1 61 3 978302109

- 2 1 914 3 978301968

- 3 1 3408 4 97830275

- 4 1 235 5 97824291


编写基于⽤户的推荐

![image 4](<第二部分 基于Mahout协同过滤的推荐系统.note_images/imageFile4.png>)

![image 5](<第二部分 基于Mahout协同过滤的推荐系统.note_images/imageFile5.png>)

# 编写基于物品的推荐

![image 6](<第二部分 基于Mahout协同过滤的推荐系统.note_images/imageFile6.png>)

![image 7](<第二部分 基于Mahout协同过滤的推荐系统.note_images/imageFile7.png>)

评估推荐模型

![image 8](<第二部分 基于Mahout协同过滤的推荐系统.note_images/imageFile8.png>)

获取推荐的查准率和查全率

![image 9](<第二部分 基于Mahout协同过滤的推荐系统.note_images/imageFile9.png>)

![image 10](<第二部分 基于Mahout协同过滤的推荐系统.note_images/imageFile10.png>)

Mahout运⾏在Hadop集群

# Hadop 执⾏脚本

hadop jar /path/mahout-examples-

*.jar org.apache.mahout.cf.taste.hadop.item.RecomenderJob-input /tmp/mahout/part0-output /tmp/mahout-out -s SIMILARITY_LOGLIKELIHOD

参数说明

--input(path) : 存储⽤户偏好数据的⽬录，该⽬录下可以包含⼀个或多个存储⽤户偏好 数据的⽂本⽂件；

--output(path) : 结算结果的输出⽬录

--numRecommendations (integer) : 为每个⽤户推荐的item数量，默认为10

--usersFile (path) : 指定⼀个包含了⼀个或多个存储userID的⽂件路径，仅为该路径 下所有⽂件包含的userID做推荐计算 (该选项可选)

--itemsFile (path) : 指定⼀个包含了⼀个或多个存储itemID的⽂件路径，仅为该路径下 所有⽂件包含的itemID做推荐计算 (该选项可选)

--filterFile (path) : 指定⼀个路径，该路径下的⽂件包含了 [userID,itemID] 值对，userID和itemID⽤逗号分隔。计算结果将不会为user推

荐 [userID,itemID] 值对中包含的item (该选项可选)

--booleanData (boolean) : 如果输⼊数据不包含偏好数值，则将该参数设置为true，默 认为false

--maxPrefsPerUser (integer) : 在最后计算推荐结果的阶段，针对每⼀个user使⽤的 偏好数据的最⼤数量，默认为10

--minPrefsPerUser (integer) : 在相似度计算中，忽略所有偏好数据量少于该值的⽤ 户，默认为1

--maxSimilaritiesPerItem (integer) : 针对每个item的相似度最⼤值，默认为100

--maxPrefsPerUserInItemSimilarity (integer) : 在item相似度计算阶段，针对每 个⽤户考虑的偏好数据最⼤数量，默认为1000

--similarityClassname (classname) : 向量相似度计算类 outputPathForSimilarityMatrix ：SimilarityMatrix输出⽬录

--randomSeed ：随机种⼦ -- sequencefileOutput ：序列⽂件输出路径

--tempDir (path) : 存储临时⽂件的⽬录，默认为当前⽤户的home⽬录下的temp⽬录

--threshold (double) : 忽略相似度低于该阀值的item对

执⾏结果上⾯命令运⾏完成之后，会在当前⽤户的hdfs主⽬录⽣成temp⽬录，该⽬录可由 -tempDir (path) 参数设置

