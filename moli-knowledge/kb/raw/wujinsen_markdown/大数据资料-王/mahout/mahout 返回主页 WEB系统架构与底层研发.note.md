# mahout learning 代码示例

⼀， Introduction

- 2 //=分析导⼊包可以看出mahout的包分为主要类以及它们的实现类=

- 3 import org.apache.mahout.cf.taste.impl.model.file.*;

- 4 import org.apache.mahout.cf.taste.impl.neighborhood.*;

- 5 import org.apache.mahout.cf.taste.impl.recommender.*;

- 6 import org.apache.mahout.cf.taste.impl.similarity.*;

- 7 import org.apache.mahout.cf.taste.model.*;

- 8 import org.apache.mahout.cf.taste.neighborhood.*;

- 9 import org.apache.mahout.cf.taste.recommender.*;

- 10 import org.apache.mahout.cf.taste.similarity.*;

- 11 import java.io.*;

- 12 import java.util.*;

- 13

- 14 class RecommenderIntro {

- 15

- 16 public static void main(String[] args) throws Exception {

//=如何从csv的⽂件中构建mahout的数据表示，DataModel就是来表示<user，item，rating>的知识 的=

- 17

- 18 DataModel model = new FileDataModel(new File("intro.csv"));

//=user-based的第⼀步就是找相似⽤户，所以要定义⽤户的相似性，包括⽤什么相似性度量，以及邻居的 参数=

- 19

- 20 UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

UserNeighborhood neighborhood = new NearestNUserNeighborhood(2, similarity, model);

- 21

- 22 //=⼀旦确定了相邻⽤户，那么⼀个普通的user-based推荐器就可以被构建起来了=

- 23 Recommender recommender = new GenericUserBasedRecommender(

- 24 model, neighborhood, similarity);

- 25 //=我们可以来使⽤它，这⾥是向⽤户1推荐1个商品=

- 26 List<RecommendedItem> recommendations =

- 27 recommender.recommend(1, 1);

- 28 //=推荐的结果可以输出，这⾥是：RecommendedItem[item:104, value:4.257081]=

- 29 for (RecommendedItem recommendation : recommendations) {

- 30 System.out.println(recommendation);

- 31 }

- 32 }

- 33 }


⼆， Evaluation

- 12 import java.util.*;

- 13 import org.apache.mahout.cf.taste.common.TasteException;

- 14 import org.apache.mahout.cf.taste.eval.RecommenderBuilder;

- 15 import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;

import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluat or;

- 16

- 17 import org.apache.mahout.common.RandomUtils;

- 18

- 19 /**

- 20 *

- 21 * @author wentingtu <wentingtu09 at gmail dot com>

- 22 */

- 23 public class RecommenderEvalu

- 24 {

- 25 public static void main(String[] args) throws IOException, TasteException

- 26 {

- 27 //=导⼊org.apache.mahout.common.RandomUtils;=

- 28 //这个是产⽣唯⼀的种⼦使得在划分训练和测试数据的时候具有唯⼀性=

- 29 RandomUtils.useTestSeed();

- 30

- 31 DataModel model = new FileDataModel(new File("intro.csv"));

- 32 //构建评估器，这⾥⽤到的性能度量是每个sum( |预测值 - 真实值| ) / 值的个数

RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();

- 33

- 34 //=导⼊ org.apache.mahout.cf.taste.eval.RecommenderBuilder;=

- 35 //这⾥要涉及⽤到了⼀个定义推荐器构造⽅法的类：RecommenderBuilder

- 36 RecommenderBuilder builder = new RecommenderBuilder()

- 37 {


- 38 //使⽤⽅法是重载buildRecommender函数，函数⾥是构造推荐器的⽅法

- 39 @Override

- 40 public Recommender buildRecommender(DataModel model)

- 41 throws TasteException

- 42 {

UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

- 43

- 44 UserNeighborhood neighborhood =

- 45 new NearestNUserNeighborhood(2, similarity, model);

return new GenericUserBasedRecommender(model, neighborhood, similarity);

- 46

- 47 }

- 48 };

- 49 //=导⼊ org.apache.mahout.cf.taste.eval.RecommenderEvaluator;=

- 50 //调⽤评估器，输⼊有上⾯构造的推荐器⽅法，数据模型，训练/全部 ⽐例，验证数据/数据 ⽐例

- 51 double score = evaluator.evaluate(builder, null, model, 0.7, 1.0);

- 52 //输出评价结果：1.0 证明最后的估计结果是 AverageAbsoluteDifference = 1.0

- 53 System.out.println(score);

- 54 }

- 55 }


- 12 import org.apache.mahout.cf.taste.common.TasteException;

- 13 import org.apache.mahout.cf.taste.eval.IRStatistics;

- 14 import org.apache.mahout.cf.taste.eval.RecommenderBuilder;

- 15 import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;

- 16 import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;

- 17 import org.apache.mahout.common.RandomUtils;

- 18

- 19 /**

- 20 *

- 21 * @author Administrator

- 22 */

- 23 public class RecommenderEvaluPrecisionRecall {

- 24 public static void main(String[] args) throws IOException, TasteException {

- 25 RandomUtils.useTestSeed();

- 26 DataModel model = new FileDataModel(new File("intro.csv"));

- 27 //=导⼊org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;=

- 28 //构建评估器

- 29 RecommenderIRStatsEvaluator evaluator =

- 30 new GenericRecommenderIRStatsEvaluator();

- 31

- 32 RecommenderBuilder recommenderBuilder = new RecommenderBuilder() {

- 33 @Override

- 34 public Recommender buildRecommender(DataModel model)

- 35 throws TasteException {

UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

- 36

- 37 UserNeighborhood neighborhood =

- 38 new NearestNUserNeighborhood(2, similarity, model);


return new GenericUserBasedRecommender(model, neighborhood, similarity);

- 39

- 40 }

- 41 };

- 42 //使⽤评估器，并设定评估期的参数

//2表示"precision and recall at 2"即相当于推荐top2，然后在top-2的推荐上计算准 确率和召回率

- 43

- 44 //既然涉及到准确率和召回率，这⾥就有⼀个"hit"的定义，就是怎样的⼀个推荐算是good

- 45 //下⾯的参数设置是这样定义"good"的：利⽤阈值threshold = µ + σ

- 46 //即 user's average preference value µ plus one standard deviation σ

- 47 //如果⼀个推荐，它的真实分值是⾼于threshold的，那么它就是"good"

- 48 IRStatistics stats = evaluator.evaluate(

- 49 recommenderBuilder, null, model, null, 2,

- 50 GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD,

- 51 1.0);

- 52 //输出为0.75 1.0

- 53 System.out.println(stats.getPrecision());

- 54 System.out.println(stats.getRecall());

- 55 }

- 56 }


三，Set preference

- 1 package mia.recommender.ch03;

- 2

- 3 import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;

- 4 import org.apache.mahout.cf.taste.model.Preference;

- 5 import org.apache.mahout.cf.taste.model.PreferenceArray;

- 6

- 7 /**

- 8 *

- 9 * @author Administrator

- 10 */

- 11 public class SetPrefinPreferenceArray {

- 12

- 13 /**

- 14 * @param args the command line arguments

- 15 */

- 16 public static void main(String[] args) {

- 17 PreferenceArray user1Prefs = new GenericUserPreferenceArray(2);

- 18 user1Prefs.setUserID(0, 1L);

- 19 user1Prefs.setItemID(0, 101L);

- 20 user1Prefs.setValue(0, 2.0f);

- 21 user1Prefs.setItemID(1, 102L);

- 22 user1Prefs.setValue(1, 3.0f);

- 23 Preference pref = user1Prefs.get(1);

- 24 }

- 25 }


四，User-based CF

- 1 package mia.recommender.ch05;

- 2

- 3 import java.io.File;

- 4 import java.io.IOException;

- 5 import org.apache.mahout.cf.taste.common.TasteException;

- 6 import org.apache.mahout.cf.taste.eval.RecommenderBuilder;

- 7 import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;

import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluat or;

- 8

- 9 import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

- 10 import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;

- 11 import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;

- 12 import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;

- 13 import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;

- 14 import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;

- 15 import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;

- 16 import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;

- 17 import org.apache.mahout.cf.taste.model.DataModel;

- 18 import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;

- 19 import org.apache.mahout.cf.taste.recommender.Recommender;

- 20 import org.apache.mahout.cf.taste.similarity.UserSimilarity;

- 21 import org.apache.mahout.common.RandomUtils;

- 22

- 23 /**

- 24 *

- 25 * @author Administrator

- 26 */

- 27 public class UserBasedCF {

- 28

public static void recommenderModelEvaluation(DataModel model) throws TasteException {

- 29

- 30

RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();

- 31

- 32 RandomUtils.useTestSeed();

- 33

- 34 RecommenderBuilder builder = new RecommenderBuilder() {

- 35 //=============实验参数设置===============

- 36 //1.K近邻 or 阈值近邻

- 37 //近邻：K？


- 38 //阈值近邻：threshold？

- 39 //2.相似度量：Euclidean ， Pearson ， Log-likelihood ， Tanimoto

- 40 char similarityPattern = 'E';//'E' or 'P' or 'L' or 'T'

- 41 char neighborhoodPattern = 'K';//'K' or 'T'

- 42 int k = 2;

- 43 double threshold = 0.5;

- 44

- 45 @Override

public Recommender buildRecommender(DataModel dm) throws TasteException {

- 46

- 47 UserSimilarity similarity = null;

- 48 UserNeighborhood neighborhood = null;

- 49 switch (similarityPattern) {

- 50 case 'E': {

- 51 similarity = new EuclideanDistanceSimilarity(dm);

- 52 }

- 53 case 'P': {

- 54 similarity = new PearsonCorrelationSimilarity(dm);

- 55 }

- 56 case 'L': {

- 57 similarity = new LogLikelihoodSimilarity(dm);

- 58 }

- 59 case 'T': {

- 60 similarity = new TanimotoCoefficientSimilarity(dm);

- 61 }

- 62 }

- 63

- 64 switch (neighborhoodPattern) {

- 65 case 'K': {

neighborhood = new NearestNUserNeighborhood(k, similarity, dm);

- 66

- 67 }

- 68

- 69 case 'T': {

neighborhood = new ThresholdUserNeighborhood(threshold, similarity, dm);

- 70

- 71 }

- 72 }

return new GenericUserBasedRecommender(dm, neighborhood, similarity);

- 73

- 74 }

- 75 };


- 76

- 77 double score = evaluator.evaluate(builder, null, model, 0.7, 1.0);

- 78 System.out.println(score);

- 79 }

- 80

- 81 /**

- 82 * @param args the command line arguments

- 83 */

- 84 public static void main(String[] args) throws IOException, TasteException {

DataModel model = new FileDataModel(new File("data/dating/ratings.dat"));

- 85

- 86 recommenderModelEvaluation(model);

- 87

- 88 }

- 89 }


五，Item-based CF

- 1 package mia.recommender.ch05;

- 2

- 3 import java.io.File;

- 4 import java.io.IOException;

- 5 import org.apache.mahout.cf.taste.common.TasteException;

- 6 import org.apache.mahout.cf.taste.eval.RecommenderBuilder;

- 7 import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;

import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluat or;

- 8

- 9 import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

- 10 import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;

- 11 import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;

- 12 import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;

- 13 import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;

- 14 import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;

- 15 import org.apache.mahout.cf.taste.model.DataModel;

- 16 import org.apache.mahout.cf.taste.recommender.Recommender;

- 17 import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

- 18 import org.apache.mahout.common.RandomUtils;

- 19

- 20 /**

- 21 *

- 22 * @author Administrator

- 23 */

- 24 public class ItemBasedCF {

- 25

public static void recommenderModelEvaluation(DataModel model) throws TasteException {

- 26

- 27

RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();

- 28

- 29 RandomUtils.useTestSeed();

- 30

- 31 RecommenderBuilder builder = new RecommenderBuilder() {

- 32 //=============实验参数设置===============

- 33 //1.K近邻 or 阈值近邻

- 34 //近邻：K？

- 35 //阈值近邻：threshold？

- 36 //2.相似度量：Euclidean ， Pearson ， Log-likelihood ， Tanimoto

- 37 char similarityPattern = 'E';//'E' or 'P' or 'L' or 'T'


- 38 @Override

public Recommender buildRecommender(DataModel dm) throws TasteException {

- 39

- 40 ItemSimilarity similarity = null;

- 41 switch (similarityPattern) {

- 42 case 'E': {

- 43 similarity = new EuclideanDistanceSimilarity(dm);

- 44 }

- 45 case 'P': {

- 46 similarity = new PearsonCorrelationSimilarity(dm);

- 47 }

- 48 case 'L': {

- 49 similarity = new LogLikelihoodSimilarity(dm);

- 50 }

- 51 case 'T': {

- 52 similarity = new TanimotoCoefficientSimilarity(dm);

- 53 }

- 54 }

- 55

- 56 return new GenericItemBasedRecommender(dm, similarity);

- 57 }

- 58 };

- 59

- 60 double score = evaluator.evaluate(builder, null, model, 0.7, 1.0);

- 61 System.out.println(score);

- 62 }

- 63

- 64 /**

- 65 * @param args the command line arguments

- 66 */

- 67 public static void main(String[] args) throws IOException, TasteException {

DataModel model = new FileDataModel(new File("data/dating/ratings.dat"));

- 68

- 69 recommenderModelEvaluation(model);

- 70

- 71 }

- 72 }


## 六，Slope one CF

- 5 import org.apache.mahout.cf.taste.common.TasteException;

- 6 import org.apache.mahout.cf.taste.common.Weighting;

- 7 import org.apache.mahout.cf.taste.eval.RecommenderBuilder;

- 8 import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;

import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluat or;

- 9

- 10 import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

- 11 import org.apache.mahout.cf.taste.impl.recommender.slopeone.MemoryDiffStorage;

- 12 import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;

- 13 import org.apache.mahout.cf.taste.model.DataModel;

- 14 import org.apache.mahout.cf.taste.recommender.Recommender;

- 15 import org.apache.mahout.cf.taste.recommender.slopeone.DiffStorage;

- 16 import org.apache.mahout.common.RandomUtils;

- 17

- 18 /**

- 19 *

- 20 * @author Administrator

- 21 */

- 22 public class SlopeOneCF {

- 23

public static void recommenderModelEvaluation(DataModel model) throws TasteException {

- 24

RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();

- 25

- 26 RandomUtils.useTestSeed();

- 27 RecommenderBuilder builder = new RecommenderBuilder() {

- 28

- 29 long diffStorageNb = 100000;

- 30

- 31 @Override

public Recommender buildRecommender(DataModel dm) throws TasteException {

- 32

DiffStorage diffStorage = new MemoryDiffStorage(dm, Weighting.WEIGHTED, diffStorageNb);

- 33

return new SlopeOneRecommender(dm, Weighting.WEIGHTED, Weighting.WEIGHTED, diffStorage);

- 34


- 35 }

- 36 };

- 37 }

- 38

- 39 /**

- 40 * @param args the command line arguments

- 41 */

- 42 public static void main(String[] args) throws IOException, TasteException {

DataModel model = new FileDataModel(new File("data/dating/ratings.dat"));

- 43

- 44 recommenderModelEvaluation(model);

- 45 }

- 46 }


七，⼀个示例

- 5 import java.util.List;

- 6 import org.apache.mahout.cf.taste.common.TasteException;

- 7 import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;

- 8 import org.apache.mahout.cf.taste.impl.model.PlusAnonymousUserDataModel;

- 9 import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

- 10 import org.apache.mahout.cf.taste.model.DataModel;

- 11 import org.apache.mahout.cf.taste.model.PreferenceArray;

- 12 import org.apache.mahout.cf.taste.recommender.RecommendedItem;

- 13

- 14 /**

- 15 *

- 16 * @author Administrator

- 17 */

- 18 public class LibimsetiWithAnonymousRecommender extends LibimsetiRecommender {

- 19

- 20 private final PlusAnonymousUserDataModel plusAnonymousModel;

- 21

- 22 public LibimsetiWithAnonymousRecommender()

- 23 throws TasteException, IOException {

this((DataModel) new FileDataModel(new File("data/dating/ratings.dat")));

- 24

- 25 }

- 26

- 27 public LibimsetiWithAnonymousRecommender(DataModel model)

- 28 throws TasteException, IOException {

- 29 //调⽤⽗类LibimsetiRecommender的构造函数

- 30 super(new PlusAnonymousUserDataModel(model));

- 31 //得到PlusAnonymousUserDataModel对象

- 32 plusAnonymousModel =

- 33 (PlusAnonymousUserDataModel) getDataModel();

- 34 }

- 35 //设计这个推荐器的recommend⽅法：输⼊：匿名⽤户的评分信息 输出：对此匿名⽤户的推荐

- 36 public synchronized List<RecommendedItem> recommend(

- 37 PreferenceArray anonymousUserPrefs, int topN)

- 38 throws TasteException {


- //利⽤PlusAnonymousUserDataModel对象的setTempPrefs⽅法为将匿名⽤户加⼊到数据 中，
- 39

- 40 //并且利⽤PlusAnonymousUserDataModel.TEMP_USER_ID作为其userID

- 41 plusAnonymousModel.setTempPrefs(anonymousUserPrefs);

- 42 //调⽤⽗类LibimsetiRecommender的recommend⽅法

- 43 //userID现在被PlusAnonymousUserDataModel.TEMP_USER_ID所代替了

- 44 List<RecommendedItem> recommendations =

- 45 recommend(PlusAnonymousUserDataModel.TEMP_USER_ID, topN, null);

- 46 //删除PlusAnonymousUserDataModel.TEMP_USER_ID与匿名⽤户的关联

- 47 plusAnonymousModel.clearTempPrefs();

- 48 return recommendations;

- 49 }

- 50 //创建当前匿名⽤户的伪数据

- 51 public PreferenceArray creatAnAnonymousPrefs() {

- 52 PreferenceArray anonymousPrefs =

- 53 new GenericUserPreferenceArray(3);

- 54 anonymousPrefs.setUserID(0, PlusAnonymousUserDataModel.TEMP_USER_ID);

- 55 anonymousPrefs.setItemID(0, 123L);

- 56 anonymousPrefs.setValue(0, 1.0f);

- 57 anonymousPrefs.setItemID(1, 123L);

- 58 anonymousPrefs.setValue(1, 3.0f);

- 59 anonymousPrefs.setItemID(2, 123L);

- 60 anonymousPrefs.setValue(2, 2.0f);

- 61 return anonymousPrefs;

- 62 }

- 63

- 64 public static void main(String[] args) throws Exception {

- 65

- 66 LibimsetiWithAnonymousRecommender recommender =

- 67 new LibimsetiWithAnonymousRecommender();

- 68 List<RecommendedItem> recommendations =

- 69 recommender.recommend(recommender.creatAnAnonymousPrefs(), 10);

- 70 System.out.println(recommendations);

- 71 }

- 72 }


- 1 package mia.recommender.ch05;

- 2

- 3 import java.io.File;

- 4 import java.io.IOException;

- 5 import java.util.Collection;

- 6 import java.util.List;

- 7 import org.apache.mahout.cf.taste.common.Refreshable;

- 8 import org.apache.mahout.cf.taste.common.TasteException;

- 9 import org.apache.mahout.cf.taste.impl.common.FastIDSet;

- 10 import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

- 11 import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;

- 12 import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;

- 13 import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;

- 14 import org.apache.mahout.cf.taste.model.DataModel;

- 15 import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;

- 16 import org.apache.mahout.cf.taste.recommender.IDRescorer;

- 17 import org.apache.mahout.cf.taste.recommender.RecommendedItem;

- 18 import org.apache.mahout.cf.taste.recommender.Recommender;

- 19 import org.apache.mahout.cf.taste.similarity.UserSimilarity;

- 20

- 21 /**

- 22 *

- 23 * @author Administrator

- 24 */

- 25 public class LibimsetiRecommender implements Recommender {

- 26

- 27 private final Recommender libimsetiRecommender;

- 28 private final DataModel model;

- 29 private final FastIDSet men;

- 30 private final FastIDSet women;

- 31 //构造函数：⼀般⽽⾔，⼀个普适的⾃定义推荐器的输⼊应该是：DataModel和额外的知识

- 32 //应该将独⽴于数据的东⻄构建好：基本的pure CF推荐器

- 33

- 34 public LibimsetiRecommender() throws TasteException, IOException {

this((DataModel) new FileDataModel(new File("data/dating/ratings.dat")));

- 35

- 36 }

//应该将独⽴于数据的东⻄构建好：基本的pure CF推荐器,即将libimsetiRecommender设为pure CF

- 37


- public LibimsetiRecommender(DataModel model) throws TasteException, IOException {
- 38

- 39 UserSimilarity similarity = new EuclideanDistanceSimilarity(model);

- 40 UserNeighborhood neighborhood =

- 41 new NearestNUserNeighborhood(2, similarity, model);

libimsetiRecommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

- 42

- 43 this.model = model;

- 44 FastIDSet[] menWomen = GenderRescorer.generateMenWomen(

- 45 new File(("gender.dat")));

- 46 men = menWomen[0];

- 47 women = menWomen[1];

- 48 }

- 49 //⽤libimsetiRecommender进⾏推荐时就加⼊了由gender信息定义的GenderRescorer

public List<RecommendedItem> recommend(long userID, int topN) throws TasteException {

- 50

- 51 IDRescorer rescorer = new GenderRescorer(men, women, userID, model);

- 52 return libimsetiRecommender.recommend(userID, topN, rescorer);

- 53

- 54 }

- 55 //⽤libimsetiRecommender也提供了⾃定义IDRescorer进⾏推荐的⽅法

public List<RecommendedItem> recommend(long userID, int topN, IDRescorer idr) throws TasteException {

- 56

- 57 return libimsetiRecommender.recommend(userID, topN, idr);

- 58 }

//这⾥要注意，由于libimsetiRecommender真正进⾏preference的估计是要受到 GenderRescorer的rescore的影响的

- 59

public float estimatePreference(long userID, long itemID) throws TasteException {

- 60

- 61 IDRescorer rescorer = new GenderRescorer(men, women, userID, model);

- 62 return (float) rescorer.rescore(

- 63 itemID, libimsetiRecommender.estimatePreference(userID, itemID));

- 64 }

- 65 //这个可以直接借助于libimsetiRecommender的setPreference

public void setPreference(long userID, long itemID, float value) throws TasteException {

- 66

- 67 libimsetiRecommender.setPreference(userID, itemID, value);

- 68 }

- 69 //这个可以直接借助于libimsetiRecommender的removePreference

public void removePreference(long userID, long itemID) throws TasteException {

- 70

- 71 libimsetiRecommender.removePreference(userID, itemID);

- 72 }


- 73 //这个可以直接借助于libimsetiRecommender的getDataModel

- 74 public DataModel getDataModel() {

- 75 return libimsetiRecommender.getDataModel();

- 76 }

- 77 //这个可以直接借助于libimsetiRecommender的refresh

- 78 public void refresh(Collection<Refreshable> alreadyRefreshed) {

- 79 libimsetiRecommender.refresh(alreadyRefreshed);

- 80 }

- 81 }


- 1 package mia.recommender.ch05;

- 2

- 3 import java.io.File;

- 4 import java.io.IOException;

- 5 import org.apache.mahout.cf.taste.common.TasteException;

- 6 import org.apache.mahout.cf.taste.impl.common.FastIDSet;

- 7 import org.apache.mahout.cf.taste.model.DataModel;

- 8 import org.apache.mahout.cf.taste.model.PreferenceArray;

- 9 import org.apache.mahout.cf.taste.recommender.IDRescorer;

- 10 import org.apache.mahout.common.iterator.FileLineIterable;

- 11

- 12 /**

- 13 *

- 14 * @author Administrator

- 15 */

- 16 public class GenderRescorer implements IDRescorer {

- 17

- 18 private final FastIDSet men;//存放当前数据模型对应的所有male selectableUser

- 19 private final FastIDSet women;//存放当前数据模型对应的所有female selectableUser

- 20 private final FastIDSet usersRateMoreMen;//

- 21 private final FastIDSet usersRateLessMen;

private final boolean likeMen;//表明针对⼀个⽤户（userID定义）⼀个profileID是否应 该过滤

- 22

- 23

- 24 public GenderRescorer(

- 25 FastIDSet men,

- 26 FastIDSet women,

- 27 long userID, DataModel model)

- 28 throws TasteException {

- 29 this.men = men;

- 30 this.women = women;

- 31 this.usersRateMoreMen = new FastIDSet();

- 32 this.usersRateLessMen = new FastIDSet();

- 33 this.likeMen = ratesMoreMen(userID, model);

- 34 }

- 35 //产⽣数据对应的men和women集合

- 36 public static FastIDSet[] generateMenWomen(File genderFile)

- 37 throws IOException {

- 38 FastIDSet men = new FastIDSet(50000);

- 39 FastIDSet women = new FastIDSet(50000);


- 40 for (String line : new FileLineIterable(genderFile)) {

- 41 int comma = line.indexOf(',');

- 42 char gender = line.charAt(comma + 1);

- 43 if (gender == 'U') {

- 44 continue;

- 45 }

- 46 long profileID = Long.parseLong(line.substring(0, comma));

- 47 if (gender == 'M') {

- 48 men.add(profileID);

- 49 } else {

- 50 women.add(profileID);

- 51 }

- 52 }

- 53 men.rehash();

- 54 women.rehash();

- 55 return new FastIDSet[]{men, women};

- 56 }

- 57 //判断userID对应的⽤户是不是更喜欢男性，从他/她评过分的那些⽤户的性别来统计

- 58 private boolean ratesMoreMen(long userID, DataModel model)

- 59 throws TasteException {

- 60 if (usersRateMoreMen.contains(userID)) {

- 61 return true;

- 62 }

- 63 if (usersRateLessMen.contains(userID)) {

- 64 return false;

- 65 }

- 66 PreferenceArray prefs = model.getPreferencesFromUser(userID);

- 67 int menCount = 0;

- 68 int womenCount = 0;

- 69 for (int i = 0; i < prefs.length(); i++) {

- 70 long profileID = prefs.get(i).getItemID();

- 71 if (men.contains(profileID)) {

- 72 menCount++;

- 73 } else if (women.contains(profileID)) {

- 74 womenCount++;

- 75 }

- 76 }

- 77 boolean ratesMoreMen = menCount > womenCount;

- 78 if (ratesMoreMen) {

- 79 usersRateMoreMen.add(userID);


- 80 } else {

- 81 usersRateLessMen.add(userID);

- 82 }

- 83 return ratesMoreMen;

- 84 }

- 85 //对于需要过滤的推荐，设置其值为NaN，这是因为他们不是不能推荐的，⽽是最差的推荐

- 86 public double rescore(long profileID, double originalScore) {

- 87 return isFiltered(profileID) ? Double.NaN : originalScore;

- 88 }

- 89 //如果⼀个⽤户是喜欢男性的，⽽推荐的⼜是⼥性，则这个推荐是应该过滤掉的，反之亦然

- 90 public boolean isFiltered(long profileID) {

- 91 return likeMen ? women.contains(profileID) : men.contains(profileID);

- 92 }

- 93 }


- 1 package mia.recommender.ch05;

- 2

- 3 import java.util.Collection;

- 4 import org.apache.mahout.cf.taste.common.Refreshable;

- 5 import org.apache.mahout.cf.taste.common.TasteException;

- 6 import org.apache.mahout.cf.taste.impl.common.FastIDSet;

- 7 import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

- 8

- 9 /**

- 10 *

- 11 * @author Administrator

- 12 */

- 13 public class GenderItemSimilarity implements ItemSimilarity {

- 14 private final FastIDSet men;

- 15 private final FastIDSet women;

- 16

- 17 public GenderItemSimilarity(FastIDSet men, FastIDSet women) {

- 18 this.men = men;

- 19 this.women = women;

- 20 }

- 21

public double itemSimilarity(long profileID1, long profileID2) throws TasteException {

- 22

- 23 Boolean profile1IsMan = isMan(profileID1);

- 24 if (profile1IsMan == null) {

- 25 return 0.0;

- 26 }

- 27 Boolean profile2IsMan = isMan(profileID2);

- 28 if (profile2IsMan == null) {

- 29 return 0.0;

- 30 }

- 31 return profile1IsMan == profile2IsMan ? 1.0 : -1.0;

- 32 }

- 33

- 34 private Boolean isMan(long profileID) {

- 35 if (men.contains(profileID)) {

- 36 return Boolean.TRUE;

- 37 }

- 38 if (women.contains(profileID)) {

- 39 return Boolean.FALSE;


- 40 }

- 41 return null;

- 42 }

- 43

public double[] itemSimilarities(long itemID1, long[] itemID2s) throws TasteException{

- 44

- 45 double[] result = new double[itemID2s.length];

- 46 for (int i = 0; i < itemID2s.length; i++) {

- 47 result[i] = itemSimilarity(itemID1, itemID2s[i]);

- 48 }

- 49 return result;

- 50 }

- 51

- 52

- 53 public long[] allSimilarItemIDs(long l) throws TasteException {

- 54 throw new UnsupportedOperationException("Not supported yet.");

- 55 }

- 56

- 57 public void refresh(Collection<Refreshable> clctn) {

- 58 throw new UnsupportedOperationException("Not supported yet.");

- 59 }

- 60

- 61 }


