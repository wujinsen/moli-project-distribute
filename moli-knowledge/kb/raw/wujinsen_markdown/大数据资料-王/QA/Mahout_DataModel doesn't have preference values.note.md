INFO: Processed 943 users Feb 5 , 201110 : 54 : 31 AM org.slf4j.impl.JCLLoggerAdapter

info INFO: Beginning evaluation using 0.9 of GenericBooleanPrefDataModel[users: 1 , 2 , 3 ...]

Exception in thread "main" java.lang.IllegalArgumentException: DataModel doesn't have preference values

at

com.google.common.base.Preconditions.checkArgument(Preconditions.java: 90 ) at

org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity. <init>(PearsonCorrelationSimilarity.java: 74 )

at org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity. <init>(PearsonCorrelationSimilarity.java: 66 )

at mia.recommender.ch02.RecommenderIntro$ 6 .buildRecommender(RecommenderI ntro.java: 163 )

at org.apache.mahout.cf.taste.impl.eval.AbstractDifferenceRecommenderEvaluat or.evaluate(AbstractDifferenceRecommenderEvaluator.java: 124 )

at mia.recommender.ch02.RecommenderIntro.eg6(RecommenderIntro.java: 175 ) at mia.recommender.ch02.RecommenderIntro.main(RecommenderIntro.java: 38 )

这个其实,耐⼼点往后读⼀点就能看到作者的解释.不过,话说回来,怎样才能将这段代码运⾏通过呢?究 其原因是选择了PearsonCorelationSimilarity相似度算法,⽽这个算法是要求偏好值的,所以抛出 了" DataModel doesn't have preference values"的异常,我们只需要选适当的相似度算法(或者说不需 要偏好值的算法)就可以解决这个问题.这⾥可选的⽅案有: Tanimoto coeficient算法和 log-likelihod算 法,对应到具体的类:TanimotoCoeficientSimilarity和 LogLikelihodSimilarity

