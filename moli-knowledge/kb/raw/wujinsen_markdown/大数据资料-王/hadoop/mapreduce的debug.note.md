步骤：书的171⻚

- 1、选⼀台tasktracker，修改mapred-site.xml⽂件 添加如下配置： <property>

<name>mapred.child.java.opts</name> <value>-agentlib:jdwp=transport=dt_socket,adres= 83,server=y,suspend=y</value>

</property>

- 2、关闭所有的tasktracker，只保留上⾯配置的⼀台需要调试的tasktracker

- 3、启动Mapreduce job

- 4、修改Debug Configurations 右键“Debug As”，选择“Debug Configurations”，选择“Remote Java Application”， 添加⼀个新的测试，输⼊远程host ip和监听端⼜，上例为8883， 然后点击“Debug”按钮。此时应该连接到远程tasktracker child进程，并进⼊断点位置， 可以单步调试了。

- 5、可以debugger了


Eclipse 连接套接字模式下的 VM 调⽤⽰例 java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,adres="8 0" -jar test.jar 使⽤远程启动配置启动 Eclipse，并指定远程应⽤程序的⽬标 VM 地址。为此，单 击 Run > Debug Configurations，然后在 Eclipse 菜单中双击 Remote Java Application。从最新创建的启动 配置中为⽬标应⽤程序指定 IP 和端⼜。为了在同⼀台机器上运⾏远程应⽤程序，仅需将主机 IP 指定 为 localhost 或 127.0.0.1。

- 问题1： ？ 答： 把 项⽬引⽤的 hadoop-core-1.0.2.jar 换成 hadoop-core-0.20.2.jar，了事。 或者下载修改的jar包


windows hadop HDFS Failed to set permisions of path

htps:/skydrive.live.com/?cid=cf746837803bc50&id=CF746837803BC50%21276&authkey=!A JCcrNRX9RCF6FA

⾃⼰把hadoop源代码中的org.apache.hadoop.fs.FileUtil放到项⽬中去，⾃⼰修改 FileUtil.checkReturnValue，使得在WIndows下不报出这个异常。 或者⼲脆⾃⼰重新编译hadoop的jar。

- 问题2： ？ 答：


hadop:Input path does not exist异常

htp:/blog.csdn.net/longzaitianguo/article/details/673468

是因为本地的input⽬录并没有上传到HDFS上，所出现 org.apache.hadoop.mapred.InvalidInputException: Input path does not exist: hdfs:/localhost:9 0/user/rot/input 解决⽅法，在eclipse下⾯建输⼊⽬录，配置到程序输⼊参数中即可。

