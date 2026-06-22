- 1、在命令⾏输⼊以下命令：

/export/servers/jdk/bin/java -cp /export/servers/spark/conf/:/export/servers/spark/lib/sparkasembly-1.6.1-hadop2.6.0.jar:/export/servers/spark/lib/datanucleus-api-jdo3.2.6.jar:/export/servers/spark/lib/datanucleus-core3.2.10.jar:/export/servers/spark/lib/datanucleus-rdbms-3.2.9.jar -Dscala.usejavacp=true -Xms1g Xmx1g -Dcom.sun.management.jmxremote Dcom.sun.management.jmxremote.port=10207 Dcom.sun.management.jmxremote.authenticate=false Dcom.sun.management.jmxremote.sl=false org.apache.spark.deploy.SparkSubmit clas org.apache.spark.repl.Main-name Spark shel spark-shel

- 2、在windows平台下，启动jvisualvm.exe⼯具

该⼯具在你的jdk安装⽬录的bin⽬录下。⽐如：D:\Program Files\Java\jdk1.8.0_73\bin

- 3、双击启动jvisualvm.exe⼯具之后，依次点击 ⽂件 ->添加jmi链接

- 4、在 添加JMX连接 填写你spark-shel脚本运⾏的服务器


![image 1](<4、使用工具查看SparkSubmit 启动时的调用顺序.note_images/imageFile1.png>)

![image 2](<4、使用工具查看SparkSubmit 启动时的调用顺序.note_images/imageFile2.png>)

- 5、填写完毕之后，直接点击连接，并快速切换到线程的选项卡，看看下图中的箭头执⾏的⽅向。能够 明显感觉到spark相关线程启动的顺序。
- 6、看完整体的启动顺序后，点击main⽅法，dump线程，可以观察到SparkSubmit 启动时，初始化的 ⼏个⽅法。


![image 3](<4、使用工具查看SparkSubmit 启动时的调用顺序.note_images/imageFile3.png>)

![image 4](<4、使用工具查看SparkSubmit 启动时的调用顺序.note_images/imageFile4.png>)

- 7、下图中左下⻆的是dump下的main线程的信息，右边是启动顺序，依次是。 org.apache.spark.deploy.SparkSubmit、反射org.apache.spark.repl.Main、反射


org.apache.spark.repl.SparkLop.proces

![image 5](<4、使用工具查看SparkSubmit 启动时的调用顺序.note_images/imageFile5.png>)

注：jvisualvm.exe⼯具如果⽤的好，对了解各种java框架的运⾏步骤帮助多多呀。

声明：本系列博⽂是在学习耿嘉安《深⼊理解Spark 核⼼思想与源码分析》、⾼彦杰《Spark⼤数据处 理》、张安战《Spark技术内幕》及互联⽹公开博客资料后，摘抄或者拷⻉相关内容整理⽽成，个别知 识点会有⾃⼰的理解并输出。欢迎转载、使⽤、重新发布，但务必保留相关图书的信息，并且不得⽤ 于商业⽬的，基于本⽂修改后的作品务必以相同的声明及许可发布。如有任何疑问，请与我联系。

技术讨论群： 138712835（需付费-会定期以发群红包的⽅式，将⼊群⾦额返回到群⾥⾯，本⼈不 赚取⼀分钱）

