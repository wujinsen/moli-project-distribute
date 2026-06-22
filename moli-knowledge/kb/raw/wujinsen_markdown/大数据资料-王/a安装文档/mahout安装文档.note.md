- 1：下载⼆进制解压安装。 到 下载，我选择下载⼆进制包，直接解压及可。

- 2：配置环境变量：在/etc/profile，/home/hadop/.bashrc中添加如下红⾊信息 export MAHOUT_HOME=/home/hadop/mahout-distribution-0.9 export PATH=$PATH:$MAHOUT_HOME/bin #set java environment MAHOUT_HOME=/home/hadop/mahout-distribution-0.7 PIG_HOME=/home/hadop/pig-0.9.2 HBASE_HOME=/home/hadop/hbase-0.94.3 HIVE_HOME=/home/hadop/hive-0.9.0 HADOP_HOME=/home/hadop/hadop-1.1.1 JAVA_HOME=/home/hadop/jdk1.7.0 PATH=$JAVA_HOME/bin:$PIG_HOME/bin:$MAHOUT_HOME/bin:$HBASE_HOME/bin:$HIVE_HOM E/bin:$HADOP_HOME/bin:$HADOP_HOME/conf:$PATH CLASPATH=.:$JAVA_HOME/lib/dt.jar:$HBASE_HOME/lib:$MAHOUT_HOME/lib:$PIG_HOME/lib:$ HIVE_HOME/lib:$JAVA_HOME/lib/tols.jar export MAHOUT_HOME export PIG_HOME export HBASE_HOME export HADOP_HOME export JAVA_HOME export HIVE_HOME export PATH export CLASPATH

- 3：启动hadop，也可以⽤伪分布式来测试

- 4：#检查Mahout是否安装完好，看是否列出了⼀些算法 mahout -help5：mahout使⽤准备


htp:/labs.renren.com/apache-miror/mahout/0.7

1 hadoop@ubuntu:~$ tar -zxvf mahout-distribution-0.7.tar.gz

- a.下载⼀个⽂件synthetic_control.data，

下载地址 ，并把这个⽂ 件放在$MAHOUT_HOME⽬录下。

- b.启动Hadoop： $HADOOP_HOME/bin/start-all.sh
- c.创建测试⽬录testdata，并把数据导⼊到这个tastdata⽬录中(这⾥的⽬录的名字只能是testdata)


htp:/archive.ics.uci.edu/ml/databases/synthetic_control/synthetic_control.data

- 1 hadoop@ubuntu:~/$ hadoop fs -mkdir testdata #

hadoop@ubuntu:~/$ hadoop fs -put /home/hadoop/mahout-distribution0.7/synthetic_control.data testdata

- 2


- d.使⽤kmeans算法(这会运⾏⼏分钟左右)
- e.查看结果


hadoop@ubuntu:~/$ hadoop jar /home/hadoop/mahout-distribution-0.7/mahoutexamples-0.7-job.jar org.apache.mahout.clustering.syntheticcontrol.kmeans.Job

1

1 hadoop@ubuntu:~/$ hadoop fs -lsr output

如果看到以下结果那么算法运⾏成功，你的安装也就成功了。 clusteredPoints clusters-0 clusters-1 clusters-10 clusters-2 clusters-3 clusters-4 clusters-5 clusters-6 clusters-7 clusters-8 clusters-9 data

