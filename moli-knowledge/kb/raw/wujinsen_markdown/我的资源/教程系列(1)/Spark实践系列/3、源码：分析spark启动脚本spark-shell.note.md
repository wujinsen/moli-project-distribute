- 1、我们通过控制台输⼊：spark-shel

- 2、spark-shel脚本调⽤了bin⽬录的spark-submit脚本
- 3、spark-submit脚本调⽤了spark-clas脚本，并指定⼀个参数：org.apache.spark.deploy.SparkSubmit

- 4、spark-clas脚本经过⼀系列的处理，执⾏了 exec "${CMD[@]}"

- 5、通过在spark-clas脚本中增添⼀个echo语句，打印了"${CMD[@]}"的内容，如下： /export/servers/jdk/bin/java -cp /export/servers/spark/conf/:/export/servers/spark/lib/spark-


![image 1](<3、源码：分析spark启动脚本spark-shell.note_images/imageFile1.png>)

![image 2](<3、源码：分析spark启动脚本spark-shell.note_images/imageFile2.png>)

![image 3](<3、源码：分析spark启动脚本spark-shell.note_images/imageFile3.png>)

asembly-1.6.1-hadop2.6.0.jar:/export/servers/spark/lib/datanucleus-api-jdo3.2.6.jar:/export/servers/spark/lib/datanucleus-core3.2.10.jar:/export/servers/spark/lib/datanucleus-rdbms-3.2.9.jar -Dscala.usejavacp=true -Xms1g Xmx1g org.apache.spark.deploy.SparkSubmit -clas org.apache.spark.repl.Mainname Spark shel spark-shel

补充： java -cp 是什么意思？

-cp 和 -claspath ⼀样，是指定类运⾏所依赖其他类的路径，通常是类库，jar包之类，需要全路径

到jar包。 window上分号“;”分隔， linux上是分号“:”分隔。

# 不⽀持通配符，需要列出所有jar包，⽤⼀点“.”代表当前路径。

- 6、通过步骤5的分析，发现在启动spark-shel时，实际上启动了⼀个JVM，类似结果如下： /export/servers/jdk/bin/java org.apache.spark.deploy.SparkSubmit


声明：本系列博⽂是在学习耿嘉安《深⼊理解Spark 核⼼思想与源码分析》、⾼彦杰《Spark⼤数据处理》、张安战 《Spark技术内幕》及互联⽹公开博客资料后，摘抄或者拷⻉相关内容整理⽽成，个别知识点会有⾃⼰的理解并输 出。欢迎转载、使⽤、重新发布，但务必保留相关图书的信息，并且不得⽤于商业⽬的，基于本⽂修改后的作品务必 以相同的声明及许可发布。如有任何疑问，请与我联系。

# 技术讨论群： 138712835（需付费-会定期以发群红包的⽅式，将⼊群⾦额返回到群⾥⾯，本⼈不赚取 ⼀分钱）

