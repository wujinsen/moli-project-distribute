import org.apache.spark.SparkContext import org.apache.spark._ import org.apache.spark.graphx._ import org.apache.spark.graphx.Graph import org.apache.spark.graphx.Edge import org.apache.spark.graphx.VertexRD import org.apache.spark.graphx.util.GraphGenerators import org.apache.spark.graphx.GraphLoader import org.apache.spark.storage.StorageLevel import org.apache.spark.rd.RD

object SparkGraphx1 {

def main(args: Aray[String]) {

val sc = new SparkContext("spark:/centos.host1 707", "Spark Graphx")

/创建点RD

val users: RD[(VertexId, (String, String)] = sc.paralelize(Aray( (3L, ("rxin", "student"), (7L, ("jgonzal", "postdoc"), (5L, ("franklin", "prof"), (2L, ("istoica", "prof" )

/创建边RD

val relationships: RD[Edge[String] = sc.paralelize(Aray( Edge(3L, 7L, "colab"), Edge(5L, 3L, "advisor"), Edge(2L, 5L, "coleague"), Edge(5L, 7L, "pi")

/定义⼀个默认⽤户，避免有不存在⽤户的关系 val defaultUser = ("John Doe", "Mising")

/构造Graph val graph = Graph(users, relationships, defaultUser)

/点RD、边RD过滤

- val fcount1 = graph.vertices.filter { case (id, (name, pos) => pos = "postdoc" }.count println("postdocs users count: " + fcount1)
- val fcount2 = graph.edges.filter(edge => edge.srcId > edge.dstId).count

- println("srcId > dstId edges count: " + fcount2)

val fcount3 = graph.edges.filter { case Edge(src, dst, prop) => src > dst }.count

- println("srcId > dstId edges count: " + fcount3)




/Triplets(三元组)，包含源点、源点属性、⽬标点、⽬标点属性、边属性 val triplets: RD[String] = graph.triplets.map(triplet => triplet.srcId + "-" +

triplet.srcAtr._1 + "-" + triplet.atr + "-" + triplet.dstId + "-" + triplet.dstAtr._1) triplets.colect().foreach(println(_)

/度、⼊度、出度 val degres: VertexRD[Int] = graph.degres; degres.colect().foreach(println) val inDegres: VertexRD[Int] = graph.inDegres inDegres.colect().foreach(println) val outDegres: VertexRD[Int] = graph.outDegres outDegres.colect().foreach(println)

/构建⼦图 val subGraph = graph.subgraph(vpred = (id, atr) => atr._2 != "Mising") subGraph.vertices.colect().foreach(println(_) subGraph.triplets.map(triplet => triplet.srcAtr._1 + " is the " + triplet.atr + " of " + triplet.dstAtr.

_1)

.colect().foreach(println(_)

/Map操作，根据原图的⼀些特性得到新图，原图结构是不变的，下⾯两个逻辑是等价的，但是第⼀ 个不会被graphx系统优化

val newVertices = graph.vertices.map { case (id, atr) => (id, (atr._1 + "-1", atr._2 + "-2") }

- val newGraph1 = Graph(newVertices, graph.edges)
- val newGraph2 = graph.mapVertices(id, atr) => (id, (atr._1 + "-1", atr._2 + "-2")


/构造⼀个新图，顶点属性是出度 val inputGraph: Graph[Int, String] =

graph.outerJoinVertices(graph.outDegres)(vid, _, degOpt) => degOpt.getOrElse(0) /根据顶点属性为出度的图构造⼀个新图，依据PageRank算法初始化边与点

val outputGraph: Graph[Double, Double] = inputGraph.mapTriplets(triplet => 1.0 / triplet.srcAtr).mapVertices(id, _) => 1.0)

/图的反向操作，新的图形的所有边的⽅向相反，不修改顶点或边性属性、不改变的边的数⽬，它可 以有效地实现不必要的数据移动或复制

var rGraph = graph.reverse

/Mask操作也是根据输⼊图构造⼀个新图，达到⼀个限制制约的效果 val cGraph = graph.conectedComponents() val validGraph = graph.subgraph(vpred = (id, atr) => atr._2 != "Mising") val validCGraph =cGraph.mask(validGraph)

/Join操作，原图外连出度点构造⼀个新图 ，出度为顶点属性 val degreGraph2 = graph.outerJoinVertices(outDegres) { (id, atr, outDegreOpt) =>

outDegreOpt match { case Some(outDeg) => outDeg case None => 0/没有出度标识为零

} }

/缓存。默认情况下,缓存在内存的图会在内存紧张的时候被强制清理，采⽤的是LRU算法 graph.cache() graph.persist(StorageLevel.MEMORY_ONLY) graph.unpersistVertices(true)

/GraphLoader构建Graph var path = "/user/hadop/data/temp/graph/graph.txt" var minEdgePartitions = 1 var canonicalOrientation = false/ if sourceId < destId this value is true val graph1 = GraphLoader.edgeListFile(sc, path, canonicalOrientation, minEdgePartitions,

StorageLevel.MEMORY_ONLY, StorageLevel.MEMORY_ONLY)

val verticesCount = graph1.vertices.count println(s"verticesCount: $verticesCount") graph1.vertices.colect().foreach(println)

val edgesCount = graph1.edges.count println(s"edgesCount: $edgesCount") graph1.edges.colect().foreach(println)

/PageRank val pageRankGraph = graph1.pageRank(0.01)

pageRankGraph.vertices.sortBy(_._2, false).saveAsTextFile("/user/hadop/data/temp/graph/grap h.pr")

pageRankGraph.vertices.top(5)(Ordering.by(_._2).foreach(println)

/Conected Components val conectedComponentsGraph = graph1.conectedComponents() conectedComponentsGraph.vertices.sortBy(_._2, false).saveAsTextFile("/user/hadop/data/tem

p/graph/graph.c") conectedComponentsGraph.vertices.top(5)(Ordering.by(_._2).foreach(println)

/TriangleCount主要⽤途之⼀是⽤于社区发现 保持sourceId⼩于destId val graph2 = GraphLoader.edgeListFile(sc, path, true) val triangleCountGraph = graph2.triangleCount() triangleCountGraph.vertices.sortBy(_._2, false).saveAsTextFile("/user/hadop/data/temp/graph/g

raph.tc") triangleCountGraph.vertices.top(5)(Ordering.by(_._2).foreach(println)

sc.stop() }

}

