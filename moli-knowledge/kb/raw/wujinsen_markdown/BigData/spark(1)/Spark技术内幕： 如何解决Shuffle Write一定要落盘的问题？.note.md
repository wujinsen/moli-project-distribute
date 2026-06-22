# Spark技术内幕： 如何解决ShufleWrite⼀定要落盘的问题？

分类： 2015-01-1 15 13 717⼈阅读

Spark架构探索

(3)

评论 收藏 举报 sparkshufle

在Spark 0.6和0.7时，Shufle的结果都需要先存储到内存中（有可能要写⼊磁盘），因此对于⼤数据量的情况下，发 ⽣GC和 OM的概率⾮常⼤。因此在Spark 0.8的时候，Shufle的每个record都会直接写⼊磁盘，并且为下游的每个 Task都⽣成⼀个单独的⽂件。这样解决了Shufle解决都需要存⼊内存的问题，但是⼜引⼊了另外⼀个问题：⽣成的 ⼩⽂件过多，尤其在每个⽂件的数据量不⼤⽽⽂件特别多的时候，⼤量的随机读会⾮常影响性能。Spark 0.8.1为了解 决0.8中引⼊的问题，引⼊了FileConsolidation机制，在⼀定程度上解决了这个问题。由此可⻅，Hash Based Shufle在Scalability⽅⾯的确有局限性。⽽Spark1.0中引⼊的Shufle Plugable Framework，为加⼊新的Shufle机 制和引⼊第三⽅的Shufle机制奠定了基础。在Spark1.1的时候，引⼊了Sort Based Shufle；并且在Spark1.2.0时， Sort Based Shufle已经成为Shufle的默认选项。但是，随着内存成本的不断下降和容量的不断上升，Spark Core会 在未来重新将Shufle的过程全部是in memory的吗？我认为这个不太可能也没太⼤必要，如果⽤户对于性能有⽐较苛 刻的要求⽽Shufle的过程的确是性能优化的重点，那么可以尝试以下实现⽅式：

- 1) Worker的节点采⽤固态硬盘
- 2) Woker的Shufle结果保存到RAMDisk上
- 3) 根据⾃⼰的应⽤场景，实现⾃⼰的Shufle机制


