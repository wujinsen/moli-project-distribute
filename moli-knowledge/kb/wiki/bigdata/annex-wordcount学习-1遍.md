---
title: wordcount学习--------------1遍.note（原文插图 annex）
slug: annex-wordcount学习-1遍
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hadoop/wordcount学习--------------1遍.note.md
related: [hadoop-生态入门]
created: 2026-07-05
updated: 2026-07-05
---

昨天在⾃⼰的电脑上配置了hadop，也运⾏了第⼀个MapReduce程序WordCount程序。但是对 mapreduce的编程还很不清楚，在⽹上转了⼀段对wordcount的解释，转载学习下。 Wordcount的输⼊是⽂件夹，⽂件夹内是多个⽂件，内容是以空格作分隔符的单词序列，输出为单 词，以及他们的数量。 ⾸先，在mapreduce程序中，程序会按照setInputFormat中设置的⽅法为将输⼊切分成⼀个个 InputSplit。 在Map过程中，程序会为每⼀个InputSplit调⽤map函数，这⾥即以空格作分隔符将单词切开。并以单 词作为key，1作为value。 需要特别指出的是，mapreduce的<key,value>⽆论是key还是value都是mapreduce预先定义好的格 式， 因此在wordcount这个程序中，我们要把String转换成text格式，int转换为IntWritable格式。 如下： private final static IntWritable one = new IntWritable(1); private Text word = new Text(); 再做 word.set(tokenizer.nextToken(); 将这些<key,value>对作为Map的结果传递下去 output.colect(word, one); 在Reduce过程中，程序会对每组<key,list of values>调⽤reduce函数，在我们这个程序中，只需让 value相加即可以。 最后调⽤output.colect输出Reduce结果。 以下是程序内容及注释： package com.felix;

import java.io.IOException; import java.util.Iterator; import java.util.StringTokenizer;

import org.apache.hadop.fs.Path; import org.apache.hadop.io.IntWritable; import org.apache.hadop.io.LongWritable; import org.apache.hadop.io.Text; import org.apache.hadop.mapred.FileInputFormat; import org.apache.hadop.mapred.FileOutputFormat; import org.apache.hadop.mapred.JobClient; import org.apache.hadop.mapred.JobConf; import org.apache.hadop.mapred.MapReduceBase;

import org.apache.hadop.mapred.Maper; import org.apache.hadop.mapred.OutputColector; import org.apache.hadop.mapred.Reducer; import org.apache.hadop.mapred.Reporter; import org.apache.hadop.mapred.TextInputFormat; import org.apache.hadop.mapred.TextOutputFormat; /* *

- * 描述：WordCount explains by Felix
- * @author Hadop Dev Group
- */ public clas WordCount {


/*

* MapReduceBase类:实现了Maper和Reducer接⼝的基类（其中的⽅法只是实现接⼝，⽽未作任 何事情）

- * Maper接⼝：
- * WritableComparable接⼝：实现WritableComparable的类可以相互⽐较。所有被⽤作key的类应


该实现此接⼝。

- * Reporter 则可⽤于报告整个应⽤的运⾏进度，本例中未使⽤。

*

- */ public static clas Map extends MapReduceBase implements


Maper<LongWritable, Text, Text, IntWritable> /设定了map函数输⼊的形式为 longwritable<key>text<value>输出地形式为text<key>intwritable<value>

{

/*

- * LongWritable, IntWritable, Text 均是 Hadop 中实现的⽤于封装 Java 数据类型的类，
- * 这些类实现了WritableComparable接⼝，
- * 都能够被串⾏化从⽽便于在分布式环境中进⾏数据交换，你可以将它们分别视为long,int,String


的替代品。

*/

private final static IntWritable one = new IntWritable(1); /定义⼀个intwritable型的常量，⽤来 说明出现过⼀次

private Text word = new Text(); /定义⼀个text型的变量，⽤来保存单词

/*

- * Maper接⼝中的map⽅法：
- * void map(K1 key, V1 value, OutputColector<K2,V2> output, Reporter reporter)
- * 映射⼀个单个的输⼊k/v对到⼀个中间的k/v对
- * 输出对不需要和输⼊对是相同的类型，输⼊对可以映射到0个或多个输出对。
- * OutputColector接⼝：收集Maper和Reducer输出的<k,v>对。
- * OutputColector接⼝的colect(k, v)⽅法:增加⼀个(k,v)对到output
- */ public void map(LongWritable key, Text value,


OutputColector<Text, IntWritable> output, Reporter reporter) /map中的参变量说明map 输⼊时的keyvalue对的形式，以及map输出和reduce接收的keyvalue数据类型

throws IOException {

String line = value.toString(); /将输⼊中的⼀⾏保存到line中

StringTokenizer tokenizer = new StringTokenizer(line); /将⼀⾏保存到准备切词的⼯具中 while (tokenizer.hasMoreTokens() /判断是否到⼀⾏的结束 {

word.set(tokenizer.nextToken(); /设定key即word的值为从每⼀⾏切下来的单词 output.colect(word, one); /设定map函数输出的keyvalue对

} }

}

public static clas Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> /设定reduce函数中输⼊对的数据类型是text和intwritable，输出对的数据类型是text 和intwritable

{

public void reduce(Text key, Iterator<IntWritable> values,

OutputColector<Text, IntWritable> output, Reporter reporter) throws IOException /设 定reduce函数中输⼊对的数据类型是text和intwritable，输出对的数据类型是text和intwritable

{

int sum = 0; while (values.hasNext() /计算同⼀个key下，所有value的总和 {

sum += values.next().get(); /获取下⼀个value的值 }

output.colect(key, new IntWritable(sum); /收集reduce输出结果 }

}

public static void main(String[] args) throws Exception {

/*

- * JobConf：map/reduce的job配置类，向hadop框架描述map-reduce执⾏的⼯作
- * 构造⽅法：JobConf()、JobConf(Clas exampleClas)、JobConf(Configuration conf)等
- */ JobConf conf = new JobConf(WordCount.clas); conf.setJobName("wordcount"); /设置⼀个⽤户定义的job名称


conf.setOutputKeyClas(Text.clas); /为job的输出数据设置Key类 conf.setOutputValueClas(IntWritable.clas); /为job输出设置value类

conf.setMaperClas(Map.clas); /为job设置Maper类 conf.setCombinerClas(Reduce.clas); /为job设置Combiner类 conf.setReducerClas(Reduce.clas); /为job设置Reduce类

conf.setInputFormat(TextInputFormat.clas); /为map-reduce任务设置InputFormat实现类 conf.setOutputFormat(TextOutputFormat.clas); /为map-reduce任务设置OutputFormat实现

类

/*

- * InputFormat描述map-reduce中对job的输⼊定义
- * setInputPaths():为map-reduce job设置路径数组作为输⼊列表
- * setInputPath()：为map-reduce job设置路径数组作为输出列表
- */ FileInputFormat.setInputPaths(conf, new Path(args[0]); /new Path(args[0]):输⼊路径，调⽤


main⽅法时，空格传值

FileOutputFormat.setOutputPath(conf, new Path(args[1]); /new Path(args[1]):输出路径，调⽤ main⽅法时，空格传值

JobClient.runJob(conf); /运⾏⼀个job }

# }

![image 1](assets/imageFile1.png)
