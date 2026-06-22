前⾔：

从IT跨度到DT,如今的数据每天都在海量的增⻓。⾯对如此巨⼤的数据，如何能让搜索引擎更 好的⼯作呢？本⽂作为Hadoop系列的第⼆篇，将介绍分布式情况下搜索引擎的基础实现，即“倒排 索引”。

- 1.问题描述

将所有不同⽂件⾥⾯的关键词进⾏存储，并实现快速检索。下⾯假设有3个⽂件的数据如下：

- file1.txt:MapReduce is simple

- file2.txt:mapReduce is powerful is simple

- file3.txt:Hello MapReduce bye MapReduce


最终应⽣成如下索引结果:

![image 1](<Hadoop之倒排索引.note_images/imageFile1.png>)

Hello file3.txt:1 MapReduce file3.txt:2;file2.txt:1;file1.txt:1 bye file3.txt:1 is file2.txt:2;file1.txt:1 powerful file2.txt:1 simple file2.txt:1;file1.txt:1

![image 2](<Hadoop之倒排索引.note_images/imageFile2.png>)

--------------------------------------------------------

- 2.设计


⾸先，我们对读⼊的数据利⽤Map操作进⾏预处理，如图1：

![image 3](<Hadoop之倒排索引.note_images/imageFile3.png>)

对⽐之前的单词计数（WorldCount.java），要实现倒排索引单靠Map和Reduce操作明显⽆法完 成，因此中间我们加⼊'Combine'，即合并操作；具体如图2：

![image 4](<Hadoop之倒排索引.note_images/imageFile4.png>)

--------------------------------------------------------------

- 3.代码实现


package cn.itcast.bigdata.hadoop.mapreduce;

import org.apache.hadoop.conf.Conﬁguration; import org.apache.hadoop.fs.Path; import org.apache.hadoop.io.Text; import org.apache.hadoop.mapreduce.Job; import org.apache.hadoop.mapreduce.Mapper; import org.apache.hadoop.mapreduce.Reducer; import org.apache.hadoop.mapreduce.lib.input.FileInputFormat; import org.apache.hadoop.mapreduce.lib.input.FileSplit; import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException; import java.util.StringTokenizer;

# public class Index { ﬁnal static String INPUT_PATH= "hdfs://hadoop0:9000/index_in"; ﬁnal static String OUTPUT_PATH= "hdfs://hadoop0:9000/index_out";

## public static class Map extends Mapper<Object, Text, Text, Text> {

private Text keyInfo = new Text(); // 存储单词和URL组合

## private Text valueInfo = new Text(); // 存储词频 private FileSplit split; // 存储Split对象

// 实现map函数 public void map(Object key, Text value, Context context)

throws IOException, InterruptedException { // 获得<key,value>对所属的FileSplit对象 split = (FileSplit) context.getInputSplit(); StringTokenizer itr = new StringTokenizer(value.toString()); while (itr.hasMoreTokens()) {

// 只获取⽂件的名称。 int splitIndex = split.getPath().toString().indexOf("ﬁle"); keyInfo.set(itr.nextToken() + ":"

+ split.getPath().toString().substring(splitIndex)); // 词频初始化为1 valueInfo.set("1"); context.write(keyInfo, valueInfo);

} }

}

public static class Combine extends Reducer<Text, Text, Text, Text> { private Text info = new Text();

// 实现reduce函数 public void reduce(Text key, Iterable<Text> values, Context context)

throws IOException, InterruptedException { // 统计词频 int sum = 0; for (Text value : values) {

sum += Integer.parseInt(value.toString()); }

int splitIndex = key.toString().indexOf(":"); // 重新设置value值由URL和词频组成 info.set(key.toString().substring(splitIndex + 1) + ":" + sum); // 重新设置key值为单词 key.set(key.toString().substring(0, splitIndex)); context.write(key, info);

} }

public static class Reduce extends Reducer<Text, Text, Text, Text> {

private Text result = new Text();

// 实现reduce函数 public void reduce(Text key, Iterable<Text> values, Context context)

throws IOException, InterruptedException { // ⽣成⽂档列表 String ﬁleList = new String(); for (Text value : values) {

ﬁleList += value.toString() + ";";

} result.set(ﬁleList);

context.write(key, result); }

}

public static void main(String[] args) throws Exception {

Conﬁguration conf = new Conﬁguration();

Job job = new Job(conf, "Inverted Index"); job.setJarByClass(InvertedIndex.class);

// 设置Map、Combine和Reduce处理类 job.setMapperClass(Map.class); job.setCombinerClass(Combine.class); job.setReducerClass(Reduce.class);

// 设置Map输出类型 job.setMapOutputKeyClass(Text.class); job.setMapOutputValueClass(Text.class);

// 设置Reduce输出类型 job.setOutputKeyClass(Text.class); job.setOutputValueClass(Text.class);

// 设置输⼊和输出⽬录 FileInputFormat.addInputPath(job, new Path(INPUT_PATH)); FileOutputFormat.setOutputPath(job, new Path(OUTPUT_PATH)); System.exit(job.waitForCompletion(true) ? 0 : 1);

} }

- 4.测试结果


![image 5](<Hadoop之倒排索引.note_images/imageFile5.png>)

Hello file3.txt:1; MapReduce file3.txt:2;file1.txt:1;file2.txt:1; bye file3.txt:1; is file1.txt:1;file2.txt:2; powerful file2.txt:1; simple file2.txt:1;file1.txt:1;

![image 6](<Hadoop之倒排索引.note_images/imageFile6.png>)

Reference:

- [1]Hadoop权威指南【A】Tom Wbite

- [2]深⼊云计算·Hadoop应⽤开发实战详解【A】万川梅 谢正兰


--------------

结语：

从上⾯的Map---> Combine ----> Reduce操作过程中，我们可以体会到“倒排索引”的过程其实 也就是不断组合并拆分字符串的过程，⽽这也就是Hadoop中MapReduce并⾏计算的体现。在现今 的⼤部分企业当中，Hadoop主要应⽤之⼀就是针对⽇志进⾏处理，所以想进军⼤数据领域的朋 友，对于Hadoop的Map/Reduce实现原理可以通过更多的实战操作加深理解。本⽂仅仅只是⽜⼑⼩ 试，对于Hadoop的深层应⽤本⼈也正在慢慢摸索~~

-------- 以上内容纯属个⼈学习总结，不代表任何团体或单位。若有理解不到之处请⻅谅！---------

htp:/ w.cnblogs.com/SeaSky0606/p/4820786.html?utm_source=tuicol&utm_medium=referal

