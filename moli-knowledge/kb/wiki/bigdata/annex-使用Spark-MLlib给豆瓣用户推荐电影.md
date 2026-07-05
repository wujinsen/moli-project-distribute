---
title: 使用Spark MLlib给豆瓣用户推荐电影.note（原文插图 annex）
slug: annex-使用Spark-MLlib给豆瓣用户推荐电影
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/spark(1)/使用Spark MLlib给豆瓣用户推荐电影.note.md
related: [spark-核心概念与实践]
created: 2026-07-05
updated: 2026-07-05
---

# 使⽤Spark MLlib给⾖瓣⽤户推荐电影

推荐算法就是利⽤⽤户的⼀些⾏为，通过⼀些数学算法，推测出⽤户可能喜欢的东⻄。 随着电⼦商务规模的不断扩⼤，商品数量和种类不断增⻓，⽤户对于检索和推荐提出了更⾼的要求。 由于不同⽤户在兴趣爱好、关注领域、个⼈经历等⽅⾯的不同，以满⾜不同⽤户的不同推荐需求为⽬ 的、不同⼈可以获得不同推荐为重要特征的个性化推荐系统应运⽽⽣。 推荐系统成为⼀个相对独⽴的研究⽅向⼀般被认为始⾃1994年明尼苏达⼤学GroupLens研究组推出 的GroupLens系统。该系统有两⼤重要贡献：⼀是⾸次提出了基于协同过滤(Collaborative Filtering)来完成推荐任务的思想，⼆是为推荐问题建⽴了⼀个形式化的模型。基于该模型的协同过滤 推荐引领了之后推荐系统在今后⼗⼏年的发展⽅向。 ⽬前，推荐算法已经已经被⼴泛集成到了很多商业应⽤系统中，⽐较著名的有Netflix在线视频推荐系 统、Amazon⽹络购物商城等。实际上，⼤多数的电⼦商务平台尤其是⽹络购物平台，都不同程度地 集成了推荐算法，如淘宝、京东商城等。Amazon发布的数据显示，亚⻢逊⽹络书城的推荐算法为亚 ⻢逊每年贡献近三⼗个百分点的创收。

### 常⽤的推荐算法

基于⼈⼝统计学的推荐(Demographic-Based Recommendation):该⽅法所基于的基本假设是 “⼀个⽤户有可能会喜欢与其相似的⽤户所喜欢的物品”。当我们需要对⼀个User进⾏个性化推荐 时，利⽤User Profile计算其它⽤户与其之间的相似度，然后挑选出与其最相似的前K个⽤户，之 后利⽤这些⽤户的购买和打分信息进⾏推荐。 基于内容的推荐(Content-Based Recommendation):Content-Based⽅法所基于的基本假设 是“⼀个⽤户可能会喜欢和他曾经喜欢过的物品相似的物品”。 基于协同过滤的推荐(Collaborative Filtering-Based Recommendation)是指收集⽤户过去的 ⾏为以获得其对产品的显式或隐式信息，即根据⽤户对

- 1.
- 2.
- 3.


物品或者信息的偏好，发现物品或者内容本身的相关性、或⽤户的相关性，然后再基于这些关联性进 ⾏推荐。基于协同过滤的推荐可以分基于⽤户的推荐（User-based Recommendation），基于物品 的推荐（Item-based Recommendation），基于模型的推荐（Model-based Recommendation）等⼦类。

以上内容copy⾃参考⽂档1

## ALS算法

LS是alternating least squares的缩写 , 意为交替最⼩⼆乘法。该⽅法常⽤于基于矩阵分解的推荐系 统中。例如：将⽤户(user)对商品(item)的评分矩阵分解为两个矩阵：⼀个是⽤户对商品隐含特征的 偏好矩阵，另⼀个是商品所包含的隐含特征的矩阵。在这个矩阵分解的过程中，评分缺失项得到了填 充，也就是说我们可以基于这个填充的评分来给⽤户最商品推荐了。 由于评分数据中有⼤量的缺失项，传统的矩阵分解SVD（奇异值分解）不⽅便处理这个问题，⽽ALS 能够很好的解决这个问题。对于R(m×n)的矩阵，ALS旨在找到两个低维矩阵X(m×k)和矩阵 Y(n×k)，来近似逼近R(m×n)，即：

#### R~=XY

R~=XY ，其中 ，

- X∈Rm×d

- X∈Rm×d，
- Y∈Rd×n


- Y∈Rd×n，d 表示降维后的维度，⼀般 d<<r，r表示矩阵 R 的秩， r<<min(m,n) r<<min(m,n)。 为了找到低维矩阵X,Y最⼤程度地逼近矩分矩阵R，最⼩化下⾯的平⽅误差损失函数。


L(X,Y)=∑u,i(rui−xTuyi)2

L(X,Y)=∑u,i(rui−xuTyi)2 为防⽌过拟合给公式 (1) 加上正则项，公式改下为：

#### L(X,Y)=∑u,i(rui−xTuyi)2+λ(|xu|2+ |yi|2)......(2)

L(X,Y)=∑u,i(rui−xuTyi)2+λ(|xu|2+ |yi|2)......(2)

其中 xu∈Rd，yi∈Rd xu∈Rd，yi∈Rd， 1⩽u⩽m 1⩽u⩽m， 1⩽i⩽n 1⩽i⩽n， λ

λ是正则项的系数。 MLlib 的实现算法中有以下⼀些参数：

##### numBlocks

⽤于并⾏化计算的分块个数 (-1为⾃动分配)

##### rank

模型中隐藏因⼦的个数，也就是上⾯的r

##### iterations

迭代的次数，推荐值：10-20

##### lambda

惩罚函数的因数，是ALS的正则化参数，推荐值：0.01

##### implicitPrefs

决定了是⽤显性反馈ALS的版本还是⽤适⽤隐性反馈数据集的版本

##### alpha

是⼀个针对于隐性反馈 ALS 版本的参数，这个参数决定了偏好⾏为强度的基准

隐性反馈 vs 显性反馈 基于矩阵分解的协同过滤的标准⽅法⼀般将⽤户商品矩阵中的元素作为⽤户对商品的显性偏好。 在许 多的现实⽣活中的很多场景中，我们常常只能接触到隐性的反馈（例如游览，点击，购买，喜欢，分 享等等）在 MLlib 中所⽤到的处理这种数据的⽅法来源于⽂献：

Collaborative Filtering for Implic it Feedback Datasets

。 本质上，这个⽅法将数据作为⼆元偏好值和偏好强度的⼀个结合，⽽不是对 评分矩阵直接进⾏建模。因此，评价就不是与⽤户对商品的显性评分⽽是和所观察到的⽤户偏好强度 关联了起来。然后，这个模型将尝试找到隐语义因⼦来预估⼀个⽤户对⼀个商品的偏好。 以上的介绍带着浓重的学术⽓息，需要阅读更多的背景知识才能了解这些算法的奥秘。Spark MLlib为 我们提供了很好的协同算法的封装。 当前MLlib⽀持基于模型的协同过滤算法，其中user和product 对应上⾯的user和item，user和product之间有⼀些隐藏因⼦。MLlib使⽤

ALS(alternating least s quares)

来学习/得到这些潜在因⼦。 下⾯我们就以实现⼀个⾖瓣电影推荐系统为例看看如何使⽤Spark实现此类推荐系统。以此类推，你 也可以尝试实现⾖瓣图书，⾖瓣⾳乐，京东电器商品推荐系统。

### ⾖瓣数据集

⼀般学习Spark MLlib ALS会使⽤ 数据集。这个数据集保存了⽤户对电影的评分。 但是这个数据集对于国内⽤户来说有点不接地⽓，事实上国内有⼀些⽹站可以提供这样的数据集，⽐ 如⾖瓣，它的⼈⽓还是挺⾼的。 但是⾖瓣并没有提供这样⼀个公开的数据集，所以我⽤抓取了⼀些数据做测试。 数据集分为两个⽂件：

movielens

hot_movies.csv: 这个⽂件包含了热⻔电影的列表，⼀种166个热⻔电影。格式为 <movieID>,<评 分>,<电影名>，如

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br></th>
    <th>20645098,8.2,⼩王⼦ 26259677,8.3,垫底辣妹 11808948,7.2,海绵宝宝 26253733,6.4,突然变异 25856265,6.7,烈⽇迷踪 26274810,6.6,侦探：为了原点</th>
  </tr>
</table>


user_movies.csv: 这个⽂件包含⽤户对热⻔电影的评价，格式为<userID>:<movieID>:<评分>

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br></th>
    <th>adamwzw,20645098,4 baka_mono,20645098,3 iRayc,20645098,2 blueandgreen,20645098,3 130992805,20645098,4 134629166,20645098,5 wangymm,20645098,3</th>
  </tr>
</table>


可以看到，⽤户名并不完全是整数类型的，但是MLlib ALS算法要求user,product都是整型的，所以 我们在编程的时候需要处理⼀下。 有些⽤户只填写了评价，并没有打分，⽂件中将这样的数据记为-1。在ALS算法中，把它转换成3.0， 也就是及格60分。虽然可能和⽤户的实际情况不相符，但是为了简化运算，我在这⾥做了简化处理。 ⽤户的评分收集了⼤约100万条，实际⽤户⼤约22万。这个矩阵还是相当的稀疏。 注意这个数据集完全基于⾖瓣公开的⽹⻚，不涉及任何个⼈的隐私。

### 模型实现

本系统使⽤Scala实现。 ⾸先读⼊这两个⽂件，得到相应的弹性分布数据集RDD (第7⾏和第8⾏)。

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br>11<br>12<br>13<br>14<br>15<br></th>
    <th>object DoubanRecommender {<br><br>def main(args: Array[String]): Unit = {<br><br>val sc = new SparkContext(new<br><br>SparkConf().setAppName("DoubanRecomender")) m //val base = "/opt/douban/" val base = if (args.length > 0)<br><br>args(0) else "/opt/douban/" //获取RDD<br><br>sc.textFile(baseval rawUserMoviesDat+ a = "user_movies.csv")<br><br>val rawHotMoviesData =<br><br>sc.textFile(base + "hot_movies.csv") //准备数据 preparation(rawUserMoviesData,<br><br>rawHotMoviesData) println("准备完数据") model(sc, rawUserMoviesData,<br><br>rawHotMoviesData) }<br><br>...... }</th>
  </tr>
</table>


第10⾏调⽤preparation⽅法，这个⽅法主要⽤来检查分析数据，得到数据集的⼀些基本的统计信息， 还没有到协同算法那⼀步。

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br></th>
    <th>def preparation( rawUserMoviesData: RDD[String],<br><br>rawHotMoviesData: RDD[String]) ={<br><br>val userIDStats = rawUserMoviesData.map(_.split(',')<br><br>(0).trim).distinct().zipWithUniqueId ().map(_._2.toDouble).stats()<br><br>val itemIDStats = rawUserMoviesData.map(_.split(',')<br><br>(1).trim.toDouble).distinct().stats( )<br><br><br>println(userIDStats) println(itemIDStats) val moviesAndName =<br><br>buildMovies(rawHotMoviesData)<br><br>val (movieID, movieName) = moviesAndName.head<br><br>println(movieID + " -> " + movieName) }</th>
  </tr>
</table>


第5⾏和第6⾏打印RDD的statCounter的值，主要是最⼤值，最⼩值等。 第9⾏输出热⻔电影的第⼀个值。 输出结果如下：

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th>(count: 223239, mean: 111620.188663, stdev: 64445.607152, max: 223966.000000, min: 0.000000) (count: 165, mean: 20734733.139394, stdev: 8241677.225813, max: 26599083.000000, min: 1866473.000000) 6866928 -> 进击的巨⼈真⼈版：前篇</th>
  </tr>
</table>


⽅法buildMovies读取rawHotMoviesData，因为rawHotMoviesData的每⼀⾏是⼀条类似20645098,8.2,⼩ 王⼦的字符串，需要按照,分割，得到第⼀个值和第三个值：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br></th>
    <th>def buildMovies(rawHotMoviesData: RDD[String]): Map[Int, String] =<br><br>rawHotMoviesData.flatMap { line => val tokens = line.split(',') if (tokens(0).isEmpty) {<br><br>None } else {<br><br>Some((tokens(0).toInt, tokens(2)))<br><br>} }.collectAsMap()</th>
  </tr>
</table>


我们使⽤这个Map可以根据电影的ID得到电影实际的名字。 下⾯就重点看看如何使⽤算法建⽴模型的：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br>11<br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br>22<br>23<br>24<br></th>
    <th>def model(sc: SparkContext,<br><br>rawUserMoviesData: RDD[String],<br><br>rawHotMoviesData: RDD[String]): Unit = {<br><br>val moviesAndName =<br><br>buildMovies(rawHotMoviesData) val bMoviesAndName =<br><br>sc.broadcast(moviesAndName) val data = buildRatings(rawUserMoviesData)<br><br>val userIdToInt: RDD[(String, Long)] = data.map(_.userID).distinct().zipWit hUniqueId()<br><br>val reverseUserIDMapping: RDD[(Long, String)] =<br><br>userIdToInt map { case(l, r)<br><br>=> (r, l) } val userIDMap: Map[String, Int]<br><br>= caseuserIdToInt.collectAsMap().map(n, l) => (n, l.toInt) } { val bUserIDMap = sc.broadcast(userIDMap)<br><br>val ratings: RDD[Rating] = data.map { r =><br><br>Rating(bUserIDMap.value.get(r.userID ).get, r.movieID, r.rating)}.cache()<br><br>// //val使⽤协同过滤算法建模model = ALS.trainImplicit(ratings, 10, 10, 0.01, 1.0)<br><br>val model = ALS.train(ratings,<br><br>50, 10, 0.0001) ratings.unpersist() println("输出第⼀个userFeature")<br><br>println(model.userFeatures.mapValues (_.mkString(", ")).first())<br><br>for (userID <Array(100,1001,10001,100001,110000)) {<br><br>checkRecommenderResult(userID, rawUserMoviesData, bMoviesAndName, reverseUserIDMapping, model)<br><br>} unpersist(model)<br><br>}</th>
  </tr>
</table>


###### 第4⾏到第12⾏是准备辅助数据，第13⾏准备好ALS算法所需的数据RDD[Rating]。 第16⾏设置⼀些参数训练数据。这些参数可以根据下⼀节的评估算法挑选⼀个较好的参数集合作为最 终的模型参数。

第21⾏是挑选⼏个⽤户，查看这些⽤户看过的电影，以及这个模型推荐给他们的电影。

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br>11<br>12<br>13<br>14<br>15<br>16<br>17<br></th>
    <th>def checkRecommenderResult(userID: Int, rawUserMoviesData: RDD[String], bMoviesAndName: Broadcast[Map[Int, String]], reverseUserIDMapping: RDD[(Long, String)], model: MatrixFactorizationModel): Unit = {<br><br>val userName = reverseUserIDMapping.lookup(userID). head<br><br>val recommendations = model.recommendProducts(userID, 5)<br><br>// val给此⽤户的推荐的电影recommendedMovieIDsID集合= recommendations.map(_.product).toSet<br><br>// val得到⽤户点播的电影rawMoviesForUserID集合= rawUserMoviesData.map(_.split(',')).<br><br>filter { case Array(user, _, _) => user.trim == userName }<br><br>val existingUserMovieIDs = rawMoviesForUser.map { case Array(_, movieID, _) => movieID.toInt }.<br><br>collect().toSet<br><br>println("⽤户" + userName + "点播 过的电影名")<br><br>// bMoviesAndName.value.filter点播的电影名 { case (id, name) => existingUserMovieIDs.contains(id) }.values.foreach(println)<br><br>println("推荐给⽤户" + userName + "的电影名")<br><br>// bMoviesAndName.value.filter推荐的电影名 { case (id, name) => recommendedMovieIDs.contains(id) }.values.foreach(println)<br><br>}</th>
  </tr>
</table>


⽐如⽤户yimiao曾经点评过以下的电影：

![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

然后这个模型为他推荐

![image 3](assets/imageFile3.png)

![image 4](assets/imageFile4.png)

![image 5](assets/imageFile5.png)

![image 6](assets/imageFile6.png)

![image 7](assets/imageFile7.png)

基本都属于喜剧动作，爱情类的，看起来还不错。

### 评价

当然，我们不能凭着⾃⼰的感觉评价模型的好坏，尽管我们直觉告诉我们，这个结果看不错。我们需 要量化的指标来评价模型的优劣。 我们可以通过计算均⽅差（Mean Squared Error, MSE）来衡量模型的好坏。数理统计中均⽅误差是 指参数估计值与参数真值之差平⽅的期望值，记为MSE。MSE是衡量“平均误差”的⼀种较⽅便的⽅ 法，MSE可以评价数据的变化程度，MSE的值越⼩，说明预测模型描述实验数据具有更好的精确度。 我们可以调整rank，numIterations，lambda，alpha这些参数，不断优化结果，使均⽅差变⼩。⽐ 如：iterations越多，lambda较⼩，均⽅差会较⼩，推荐结果较优。

def evaluate( sc: SparkContext,

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10
- 11
- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21
- 22
- 23
- 24
- 25
- 26
- 27
- 28
- 29
- 30
- 31
- 32
- 33
- 34
- 35
- 36
- 37
- 38
- 39
- 40
- 41
- 42
- 43
- 44
- 45
- 46
- 47
- 48
- 49
- 50
- 51
- 52
- 53
- 54
- 55
- 56


rawUserMoviesData: RDD[String],

rawHotMoviesData: RDD[String]): Unit = {

val moviesAndName = buildMovies(rawHotMoviesData) val bMoviesAndName = sc.broadcast(moviesAndName) val data = buildRatings(rawUserMoviesData)

val userIdToInt: RDD[(String, Long)] = data.map(_.userID).distinct().zipWit hUniqueId()

val userIDMap: Map[String, Int]

=

userIdToInt.collectAsMap().map { case (n, l) => (n, l.toInt) }

val bUserIDMap = sc.broadcast(userIDMap)

val ratings: RDD[Rating] = data.map { r =>

Rating(bUserIDMap.value.get(r.userID ).get, r.movieID, r.rating)

}.cache()

val numIterations = 10 for (rank <- Array(10, 50); lambda <- Array(1.0, 0.01,0.0001)) {

val model = ALS.train(ratings, rank, numIterations, lambda)

// Evaluate the model on rating data

val usersMovies = ratings.map { case Rating(user, movie, rate) => (user, movie)

} val predictions =

model.predict(usersMovies).map { case Rating(user, movie, rate) =>

((user, movie), rate) }

val ratesAndPreds = ratings.map { case Rating(user, movie, rate) =>

((user, movie), rate) }.join(predictions) val MSE = ratesAndPreds.map {

case ((user, movie), (r1, r2)) => val err = (r1 - r2)

57 err * err }.mean() println(s"(rank:$rank,

lambda: $lambda, Explicit ) Mean Squared Error = " + MSE)

} for (rank <- Array(10, 50); lambda <- Array(1.0, 0.01,0.0001);

alpha <- Array(1.0, 40.0)) {

val model = ALS.trainImplicit(ratings, rank, numIterations, // Evaluatelambda,the modelalpha)on rating data

val usersMovies = ratings.map { case Rating(user, movie, rate) => (user, movie)

} val predictions =

model.predict(usersMovies).map { case Rating(user, movie, rate) =>

((user, movie), rate) }

val ratesAndPreds = ratings.map { case Rating(user, movie, rate) =>

((user, movie), rate) }.join(predictions) val MSE = ratesAndPreds.map {

case ((user, movie), (r1, r2)) => val err = (r1 - r2) err * err

}.mean() println(s"(rank:$rank,

lambda: $lambda,alpha:$alpha ,implicit ) Mean Squared Error = "

+ MSE)

} }

第16⾏到第35⾏评估显性反馈的参数的结果，第36⾏到第56⾏评估隐性反馈的参数的结果。 评估的结果如下：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br>11<br>12<br>13<br>14<br>15<br>16<br>17<br>18<br></th>
    <th>(rank:10, lambda: 1.0, Explicit ) Mean Squared Error = 1.5592024394027315<br><br>(rank:10, lambda: 0.01, Explicit ) Mean Squared Error = 0.1597401855959523<br>(rank:10, lambda: 1.0E-4, Explicit ) Mean Squared Error =<br><br><br>0.12000266211936791 (rank:50, lambda: 1.0, Explicit ) Mean Squared Error = 1.559198310777233<br><br>(rank:50, lambda: 0.01, Explicit ) Mean Squared Error = 0.015537276558121003<br>(rank:50, lambda: 1.0E-4, Explicit ) Mean Squared Error = 0.0029577581713741545 (rank:10, lambda: 1.0,alpha:1.0 ,implicit ) Mean Squared Error = 10.352420717999916 (rank:10, lambda: 1.0,alpha:40.0 ,implicit ) Mean Squared Error = 7.37758192206552<br><br><br>(rank:10, lambda: 0.01,alpha:1.0 ,implicit ) Mean Squared Error = 9.138333638388543<br><br>(rank:10, lambda: 0.01,alpha:40.0 ,implicit ) Mean Squared Error = 7.288950103420938<br>(rank:10, lambda: 1.0E-4,alpha:1.0 ,implicit ) Mean Squared Error = 9.090678049662575<br><br><br>(rank:10, lambda: 1.0E-4,alpha:40.0 ,implicit ) Mean Squared Error = 7.20726197573743 (rank:50, lambda: 1.0,alpha:1.0 ,implicit ) Mean Squared Error = 9.920570381082038 (rank:50, lambda: 1.0,alpha:40.0 ,implicit ) Mean Squared Error = 7.202627234339378<br><br><br>(rank:50, lambda: 0.01,alpha:1.0 ,implicit ) Mean Squared Error = 7.756830091892575<br><br>(rank:50, lambda: 0.01,alpha:40.0 ,implicit ) Mean Squared Error = 7.054065456899226<br>(rank:50, lambda: 1.0E-4,alpha:1.0 ,implicit ) Mean Squared Error = 7.599617817478698<br><br><br>(rank:50, lambda: 1.0E-4,alpha:40.0 ,implicit ) Mean Squared Error = 7.0397787030727645<br></th>
  </tr>
</table>


可以看到rank为50, lambda为0.0001的显性反馈时的MSE最⼩。我们就已这组参数作为我们的推荐 模型。

### 模型应⽤

既然我们已经得到了⼀个很好的推荐模型，下⼀步就是使⽤它为所有的⽤户⽣成推荐集合。

def recommend(sc: SparkContext,

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10
- 11
- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21
- 22
- 23
- 24
- 25
- 26
- 27
- 28
- 29
- 30
- 31
- 32
- 33
- 34
- 35
- 36
- 37
- 38
- 39


rawUserMoviesData: RDD[String],

rawHotMoviesData: RDD[String],

base:String): Unit = {

val moviesAndName = buildMovies(rawHotMoviesData) val bMoviesAndName = sc.broadcast(moviesAndName) val data = buildRatings(rawUserMoviesData)

val userIdToInt: RDD[(String, Long)] = data.map(_.userID).distinct().zipWit hUniqueId()

val reverseUserIDMapping: RDD[(Long, String)] =

userIdToInt map { case (l, r)

=> (r, l) } val userIDMap: Map[String, Int]

=

userIdToInt.collectAsMap().map { case (n, l) => (n, l.toInt) }

val bUserIDMap = sc.broadcast(userIDMap)

val bReverseUserIDMap = sc.broadcast(reverseUserIDMapping.co llectAsMap())

val ratings: RDD[Rating] = data.map { r =>

Rating(bUserIDMap.value.get(r.userID ).get, r.movieID, r.rating)

}.cache()

// //val使⽤协同过滤算法建模model = ALS.trainImplicit(ratings, 10, 10, 0.01, 1.0)

val model = ALS.train(ratings,

50, 10, 0.0001) ratings.unpersist() //model.save(sc, base+"model") //val sameModel =

MatrixFactorizationModel.load(sc, base + "model")

val allRecommendations = model.recommendProductsForUsers(5) map {

case (userID, recommendations)

=> {

var recommendationStr = ""

for (r <- recommendations) {

recommendationStr += r.product + ":" + bMoviesAndName.value.getOrElse(r.pro duct, "") + ","

} if

(recommendationStr.endsWith(","))

recommendationStr = recommendationStr.substring(0,recomm endationStr.length-1)

(bReverseUserIDMap.value.get(userID)

.get,recommendationStr)

} }

allRecommendations.saveAsTextFile(ba se + "result.csv")

unpersist(model) }

这⾥将推荐结果写⼊到⽂件中，更实际的情况是把它写⼊到HDFS中，或者将这个RDD写⼊到关系型 数据库中如Mysql, Postgresql,或者NoSQL数据库中，如MongoDB, cassandra等。 这样我们就可 以提供接⼝为指定的⽤户提供推荐的电影。 查看本例⽣成的推荐结果，下⾯是其中的⼀个⽚段，第⼀个字段是⽤户名，后⾯是五个推荐的电影(电 影ID:电影名字)

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br>11<br>12<br>13<br></th>
    <th>(god8knows,25986688:流浪者年代<br><br>记,26582787:⽃地主,24405378:王牌特⼯： 特⼯学院,22556810:猛⻰特囧,25868191:极 道⼤战争)<br><br>(60648596,25853129:瑞奇和闪 电,26582787:⽃地主,3445457:⽆境之 兽,3608742:冲出康普顿,26297388:这时对那 时错) (120501579,25856265:烈⽇迷踪,3608742: 冲出康普顿,26275494:橘⾊,26297388:这时 对那时错,25868191:极道⼤战争) (xrzsdan,24405378:王牌特⼯：特⼯学<br><br>院,26599083:妈妈的朋友,10440076:最后的 ⼥巫猎⼈,25868191:极道⼤战争,25986688: 流浪者年代记)<br><br>(HoldonBoxer,10604554:躲藏,26297388: 这时对那时错,26265099:⽩河夜 船,26275494:橘⾊,3608742:冲出康普顿) (46896492,1972724:斯坦福监狱实 验,26356488:1944,25717176:新宿天 鹅,26582787:⽃地主,25919385:⻓寿商会) (blankscreen,24405378:王牌特⼯：特⼯学 院,26599083:妈妈的朋友,25955372:1980年 代的爱情,25853129:瑞奇和闪电,25856265: 烈⽇迷踪) (linyiqing,3608742:冲出康普 顿,25868191:极道⼤战争,26275494:橘 ⾊,25955372:1980年代的爱情,26582787:⽃ 地主) (1477412,25889465:抢劫,25727048:福尔 摩斯先⽣,26252196:卫⽣间的圣⺟ 像,26303865:维多利亚,26276359:酷毙了) (130875640,24405378:王牌特⼯：特⼯学<br><br>院,25856265:烈⽇迷踪,25986688:流浪者年 代记,25868191:极道⼤战争,25898213:军⽝ ⻨克斯)<br><br>(49996306,25919385:⻓寿商会,26582787: ⽃地主,26285777:有客到,25830802:对⻛说 爱你,25821461:旅程终点)<br><br>(fanshuren,10604554:躲藏,26582787:⽃ 地主,25856265:烈⽇迷踪,25843352:如此美 好,26275494:橘⾊)<br><br>(sweetxyy,26582787:⽃地主,25868191:极 道⼤战争,3608742:冲出康普顿,25859495:思 悼,22556810:猛⻰特囧)</th>
  </tr>
</table>


### 综述

通过前⾯的介绍，我们可以了解如何使⽤Spark MLlib的ALS算法为22万⾖瓣⽤户实现⼀个可⽤的推 荐系统，如何加载数据集和输出数据结果，以及如何对模型进⾏有效的评估。 你可以使⽤本⽂的算法实现其它的推荐系统，如图书，⽂章，商品等。
