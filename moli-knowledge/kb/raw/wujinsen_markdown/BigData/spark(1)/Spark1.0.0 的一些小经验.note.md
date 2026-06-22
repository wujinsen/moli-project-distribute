Spark1.0.0 的⼀些⼩经验 spark 评论 收藏举报 spark经验

分类： 2014-05-13 15 23 58⼈阅读

(0)

1：关于读取本地⽂件 使⽤spark-shel连接Spark集群，然后在运⾏应⽤程序中读取本地⽂件时，会经常碰上⽂件不存在的错 误。 主要原因是由于： spark-shel作为应⽤程序，是将提交作业给spark集群，然后spark集群分配到具体的worker来处理， worker在处理作业的时候会读取本地⽂件。这时候冲突就发⽣了，运⾏spark-shel的机器可能和运⾏ worker的机器不是同⼀台，⽽⽂件是放在运⾏spark-shel的机器上，运⾏worker的机器上没有，就出 现了上⾯的错误。 解决⽅法：将⽂件复制到所有节点相同的⽬录上，或者将⽂件复制到worker相同的⽬录上（这个有点 难度，要先看⽇志来判断，

![image 1](<Spark1.0.0 的一些小经验.note_images/imageFile1.png>)

）。

2：关于Core数量的设置 缺省的情况下，spark-shel会使⽤spark集群中的所有剩余的Core，但可以通过设置 -c 参数来指定使 ⽤的Core数量。这样就可以将多个spark-shel连接到spark集群上了。

