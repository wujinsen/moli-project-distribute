⼀、如果配置了HDFS的federation，使⽤eclipse插件配置完M/R Master的端⼜（5020）和DFS Master的端⼜ （9 0）后，⽆法连接到HDFS看远程⽬录结构 解决办法：切换到Advanced parameters标签，修改fs.defaultFS为hdfs:/192.168.0.14 9 0/tmp ⼆、使⽤Maven开发hadop2 mapreduce时，下载不了hadop2.6.0的⼀些相关包的解决办法 在pom.xml中增加如下配置： <repositories> <repository> <id>maven.oschina.net</id> <url>

# htp:/maven.oschina.net/conten t/groups/public/

</url> </repository> </repositories> 三、引⼊hadop2的mapreduce⼯程报错Mising artifact jdk.tols:jdk.tols:jar:1.7 在pom.xml⽂件中增加如下配置： <dependency>

<groupId>jdk.tols</groupId> <artifactId>jdk.tols</artifactId> <version>1.7</version> <scope>system</scope> <systemPath>${JAVA_HOME}/lib/tols.jar</systemPath>

</dependency> 四、Container * is runing beyond virtual memory limits. Curentusage:498.0MB od 1GB physical memory used;2.7GB of 2.1GB virtual memoryused.Kiling container.

修改yarn-site.xml中yarn.scheduler.minimum-alocation-mb（默认1024）为2048或更⼤，根据程序需求调整。 五、Container * is runing beyond physical memory limits.Curent usage: 2.5 GB of 2.5 GB physical memory used; 3.1 GB of 12.5 GB virtualmemory used. Kiling container.

在提交作业的命令中加⼊hadop jar <jarName> -Dmapreduce.reduce.memory.mb=4096或hadopjar <jarName>

-Dmapreduce.map.memory.mb=4096 或者修改mapred-site.xml中的mapreduce.map.memory.mb（默认1024）或mapreduce.reduce.memory.mb（默 认1024）参数。 六、org.apache.hadop.mapred.YarnChild: Eror runing child :java.lang.OutOfMemoryEror: Java heap space 修改mapred-site.xml，增加如下配置： <property>

<name>mapred.child.java.opts</name> <value>-Xmx1024m</value>

</property> 七、 [ContainerLauncher #4] org.apache.hadop.ipc.Client: Retryingconect to server: data-

- 17/192.168.0.17 37595. Already tried 0 time(s); retrypolicy is RetryUpToMaximumCountWithFixedSl ep(maxRetries=10, sl epTime=1 0MILISECONDS) [ContainerLauncher #4]org.apache.hadop.ipc.Client: Retrying conect to server:data-

- 17/192.168.0.17 37595. Already tried 1 time(s); retry policy is RetryUpToMaximumCountWithFixedSl ep(maxRetries=10,sl epTime=1 0 MILISECONDS) . 可能是防⽕墙没有关闭，关闭防⽕墙可以解决。（⽬前没有找到相应的通信端⼜，若找到开启端⼜即可 (37595 ?)，不需要关闭防⽕墙）


