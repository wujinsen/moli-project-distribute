# nutch安装

安装软件准备 安装环境：centos 6.5 nutch：v2.2.1 hbase:v0.94.18 本篇重点讲述nutch的安装和nutch与hbase的集成，hbase的安装请参考其他资料； 安装步骤：

- 1.
- 2.

1.

- 1.
- 2.
- 3.
- 4.


- 3.

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


- 4.


安装ant：因编译nutch源码，需要ant⼯具，下载apache-ant 设置 系统变量

写道 [hadop@master nutch]$ vim /etc/profile 添加：ANT_HOME=/usr/local/ant 变量，并将AN_HOMT添 加到PATH

下载nutch安装包： ，下载⽬前最新的apache-nutch2.2.1-src.tar.gz

htp:/nutch.apache.org/downloads.html

Java代码

[hadop@master nutch]$ wget -P /usr/local/ 下载压缩包到/usr/local/⽬录下，软后解压赋予权限

htp:/ w.apache.org/dyn/closer.cgi/nutch/2.2.1/ apache-nutch-2.2.1-src.tar.gz

Java代码

[hadop@master local]$ chmod 7 apache-nutch-2.2.1-src.tar.gz [hadop@master local]$ tar zxvf apache-nutch-2.2.1-src.tar.gz [hadop@master local]$ mv apache-nutch-2.2.1 nutch [hadop@master local]$ cd nutch/ 修改nutch的conf/nutch-site.xml⽂件，添加如下代码：

Java代码

<property> <name>storage.data.store.clas</name> <value>org.apache.gora.hbase.store.HBaseStore</value> <description>Default clasfor storing data</description> </property> <property> <name>htp.agent.name</name> <value>Mozila/5.0 (Macintosh; Intel Mac OS X 10_8_4) ApleWebKit/537.36 (KHTML, like Gec ko) Chrome/28.0.150.95 Safari/537.36</value> </property> 修改ivy/ivy.xml⽂件，找到：

Java代码

- 1.


<dependency org="org.apache.gora" name="gora-hbase" rev="0.3"

- 2.


conf="*->default" />并把原有的注释去掉 修改conf/gola.properies：

- 5.

1.

- 6.

- 1.
- 2.


- 7.

- 1.
- 2.


1.

1.

1.

- 8.

- 1.
- 2.
- 3.


1.

- 9.


Java代码

gora.datastore.default=org.apache.gora.hbase.store.HBaseStore ant编译nutch：切换到nutch⽬录：

Java代码

[hadop@master local]$ cd nutch [hadop@master nutch]$ ant 编译过程会等待⼀段时间。 修改nutch配置⽂件：在编译nutch源⽂件前，为了⽀持hbase存储，需要修改相应的配置：

Java代码

#拷⻉hbase的配置⽂件到nutch cp /usr/local/hbase/conf/hbase-site.xml /usr/local/nutch/conf/ 复制hbase的jar包到nutch，本⼈ 安装的hbase是hbase0.94.18，nutch⾃带的gora0.3是只能⽀持到最⾼hbase0.92，默认是 hbase0.90，⽽默认的0.90jar包去操作0.94的hbase，导致⼀个异常：

Java代码

java.lang.IlegalArgumentException: Not a host:port pair 应该是低版本hbase client操作⾼版本 hbase server的常⻅错误，但也不能直接⽤0.94的hbase jar包去替换，不然⼜会导致另⼀个错 误：

Java代码

java.lang.NoSuchMethodEror:org.apache.hadop.hbase.HColumnDescriptor.setMaxVersions(I )V解决办法：我们选择hbase 0.92 到 0.93之间的版本，⾸先尝试0.92版本，可以从maven中⼼ 库下载：

Java代码

htp:/central.maven.org/maven2/org/apache/hbase/hbase/0.92.2/hbase-0.92.2.jar

然后将 hbase-0.92.2.jar包替换nutch

设置抓取⽹址： 编译后切换到⽬录：

Java代码

[hadop@master nutch]$ cd runtime/local/ [hadop@master local]$ mkdir -p urls [hadop@master local]$ vim urls/sed.txt 填写sed.txt内容： 每⼀ ⾏为⼀个⽬标地址；并将urls⽬录放到hdfs⽂件系统上：

htp:/ w.apache.org/

Java代码

hadop fs -copyFromLocal urls /home/hadop/urls 运⾏nutch测试：执⾏nutch inject将⽹⻚种⼦放到hbase中

Java代码

1.

[hadop@master local]$ bin/nutch inject /home/hadop/urls 查看hbase中表：

Java代码

- 1.
- 2.
- 3.
- 4.


hbase shel

进⼊到hbaseshel后查看表 >list 看到有表“webpage”则表示成功； 然后⼀次执⾏

- 10.

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


- 11.


Java代码

[hadop@master local]$ bin/nutch generate -topN 3 [hadop@master local]$ bin/nutch fetch -al

[hadop@master local]$ bin/nutch parse -al

[hadop@master local]$ bin/nutch updatedb 切换到hbase shel或使⽤hbase client查看数据

