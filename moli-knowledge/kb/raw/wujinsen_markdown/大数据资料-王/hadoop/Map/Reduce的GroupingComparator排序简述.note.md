⼀、背景 排序对于MR来说是个核⼼内容，如何做好排序⼗分的重要，这⼏天写了⼀些，总结⼀下，以供以 后读阅。

⼆、准备

- 1、hadop版本是0.20.2
- 2、输⼊的数据格式（这个很重要，看清楚格式），名称是secondary.txt：

[java] view plaincopyabc 123 acb 124 cbd 523 abc 234 nbc 563 fds 235 khi 234

cbd 675 fds 971 hka 862 ubd 621 khi 123 fds 321

仔细看下，数据⽂件第⼀列是字⺟，第⼆列是数字，我要做的就是结合这组数据进⾏⼀些排序的测 试。

- 3、代码框架，因为接下来的测试改动都是针对部分代码的修改，框架的代码是不会改变的，所以 先把主要代码贴在这⾥。 代码分为2部分：⾃定义的key和主框架代码(注意看下红⾊部分)。先贴上主框架代码： MyGrouping.java [java]view plaincopy


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.


import org.apache.hadop.conf.Configuration;

import org.apache.hadop.fs.Path;

import org.apache.hadop.io.LongWritable;

import org.apache.hadop.io.Text;

import org.apache.hadop.io.WritableComparator;

import org.apache.hadop.mapreduce.Job;

import org.apache.hadop.mapreduce.Maper;

import org.apache.hadop.mapreduce.Partitioner;

import org.apache.hadop.mapreduce.Reducer;

import org.apache.hadop.mapreduce.lib.input.FileInputFormat;

import org.apache.hadop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadop.util.GenericOptionsParser;

import com.run.lenged.busines.TextPair;

publicclas MyGrouping {

/*

- * Map

*

- * @author Administrator

- */


publicstaticclas MyGroupingMap extends Maper<LongWritable, Text, TextPair, Tex t> {

protectedvoid map(LongWritable key, Text value,

- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.


org.apache.hadop.mapreduce.Maper<LongWritable, Text, TextPair, Text>.Cont ext context)

throws java.io.IOException, InteruptedException {

String ar[] = value.toString().split("/t");

if (ar.length != 2) {

return;

}

TextPair tp = new TextPair();

tp.set(new Text(ar[0]), new Text(ar[1]);

context.write(tp, new Text(ar[1]);

}

}

/*

- * 按照Hashcode值来进⾏切分

*

- * @author Administrator

- */


publicstaticclas MyGroupingPartition extends Partitioner<TextPair, Text> {

@Overide

publicint getPartition(TextPair key, Text value, int numPartitions) { return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;

}

}

/*

- * group进⾏排序

*

- * @author Administrator

- */


@SupresWarnings("unchecked")

publicstaticclas MyGroupingGroup extends WritableComparator {

/代码变动部分

}

/*

* reduce

- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.
- 81.
- 82.
- 83.
- 84.
- 85.
- 86.
- 87.
- 88.
- 89.
- 90.
- 91.
- 92.
- 93.
- 94.
- 95.


*

- * @author Administrator

- */


publicstaticclas MyGroupingReduce extends Reducer<TextPair, Text, Text, Text> {

protectedvoid reduce(TextPair key, java.lang.Iterable<Text> value,

org.apache.hadop.mapreduce.Reducer<TextPair, Text, Text, Text>.Context cont ext)

throws java.io.IOException, InteruptedException {

StringBufer sb = new StringBufer();

while (value.iterator().hasNext() {

sb.apend(value.iterator().next().toString() + "_");

}

context.write(key.getFirst(), new Text(sb.toString().substring(0, sb.toString().length () - 1 );

}

}

publicstaticvoid main(String args[]) throws Exception {

Configuration conf = new Configuration();

GenericOptionsParser parser = new GenericOptionsParser(conf, args);

String[] otherArgs = parser.getRemainingArgs();

if (args.length != 2) {

System.er.println("Usage: NewlyJoin <inpath> <output>");

System.exit(2);

}

Job job = new Job(conf, "MyGrouping");

/ 设置运⾏的job

job.setJarByClas(MyGrouping.clas);

/ 设置Map相关内容

job.setMaperClas(MyGroupingMap.clas);

job.setMapOutputKeyClas(TextPair.clas);

job.setMapOutputValueClas(Text.clas);

job.setPartitionerClas(MyGroupingPartition.clas);

job.setGroupingComparatorClas(MyGroupingGroup.clas);

- 96.
- 97.
- 98.
- 99.
- 100.
- 101.
- 102.
- 103.
- 104.
- 105.
- 106.


/ 设置reduce

job.setReducerClas(MyGroupingReduce.clas);

job.setOutputKeyClas(Text.clas);

job.setOutputValueClas(Text.clas);

/ 设置输⼊和输出的⽬录

FileInputFormat.adInputPath(job, new Path(otherArgs[0]);

FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]);

/ 执⾏，直到结束就退出

System.exit(job.waitForCompletion(true) ? 0 : 1);

}

}

TextPair.java [java]view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.


import java.io.DataInput;

import java.io.DataOutput; import java.io.IOException;

import org.apache.hadop.io.Text;

import org.apache.hadop.io.WritableComparable;

publicclas TextPair implements WritableComparable<TextPair> {

private Text first;

private Text second;

public TextPair() {

set(new Text(), new Text();

}

publicvoid set(Text first, Text second) {

this.first = first;

this.second = second;

}

public Text getFirst() {

return first;

}

public Text getSecond() {

return second;

}

@Overide

publicvoid readFields(DataInput in) throws IOException {

first.readFields(in);

second.readFields(in);

}

- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.


@Overide

publicvoid write(DataOutput out) throws IOException {

first.write(out);

second.write(out);

}

@Overide

publicint compareTo(TextPair o) {

int cmp = first.compareTo(o.first);

if (cmp != 0) {

return cmp;

} else {

return second.compareTo(o.second);

}

}

}

三、测试前提

- 1、⾸先提⼀个需求，我们结合需求来测试，然后再扩散开。 需求内容是：如果第⼀列值相同，第⼆列值叠加，并对第⼆列值进⾏升序排序。最后输出的时候， 按照第⼀列值的升序排序输出。

- 2、需求实现。 根据上⾯的需求，我们可以分析⼀下： 需要对第⼀个字段和第⼆个字段都进⾏排序，那么单纯的利⽤MR框架对key迭代输出，value累加 是不⾏的。因为value是没有进⾏排序。 所以我们需要做⼀些改动，定义key为符合组建。TextPair.java类就是⾃定义的key。 ⼀般来说如果要对key和value同时做排序，那么，⾃定义的组合key的格式第⼀个值是第⼀个字 段，第⼆个值就是第⼆个字段。

- 3、那么我们就定义⼀个job.setGroupingComparatorClas(MyGroupingGroup.clas);代码如下： [javascript]


view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.


publicstaticclas MyGroupingGroup extends WritableComparator {

publicint compare(WritableComparable a, WritableComparable b) {

return mip1.getFirst().compareTo(mip2.getFirst();

}

protected MyGroupingGroup() { super(TextPair.clas, true);

}

@Overide

- TextPair mip1 = (TextPair) a;

- TextPair mip2 = (TextPair) b;


}

只对输出的复合组建第⼀项值进⾏排序。输出的结果如下：

[java]

view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


abc 123_234 cbd 523_675 khi 123_234

ubd 621

nbc 563 acb 124

- 7.
- 8.


fds 235_321_971

hka 862

4、查看结果，我们可以看出，基本满⾜了上⾯的需求。那么接下来，我们就将做个测试，来实现⼀下 MR的排序功能。

四、Group按第⼆个字段值进⾏排序测试

- 1、修改⼀下group的排序⽅式，针对第⼆个值进⾏合并排序，代码如下： [java]
- 2、reduce的输出稍微改下，将第2个字段也输出，⽅便查看，代码如下： [java]

reduce输出的结果： [html]

- 3、看到结果，第⼀反应就是没有按照我的要求，按第⼆个值进⾏排序操作。


view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.


publicstaticclas MyGroupingGroup extends WritableComparator {

protected MyGroupingGroup() { super(TextPair.clas, true);

}

@Overide

publicint compare(WritableComparable a, WritableComparable b) {

- TextPair mip1 = (TextPair) a;

- TextPair mip2 = (TextPair) b;


return mip1.getSecond().compareTo(mip2.getSecond();

/return mip1.getFirst().compareTo(mip2.getFirst();

}

}

view plaincopy

1.

context.write(key.getFirst(), new Text(sb.toString().substring(0, sb.toString().length() - 1 ) ;

view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.


abc_123 123

abc_234 234

acb_124 124

cbd_523 523 cbd_675 675 fds_235 235

fds_321 321 fds_971 971 hka_862 862

khi_123 123

khi_234 234

nbc_563 563

ubd_621 621

其实不是，这个结果确实是进⾏了group的排序，只是说遇到没有符合合并结果数据。所以，看起 来没有进⾏排序。 在这⾥有个概念，就是group到底是在什么时候做的排序，原⽂是这样写的： Job.setGroupingComparatorClas(Clas<? extends RawComparator> cls) Define the comparator that controls which keys are grouped together for a single cal to Reducer.reduce(Object, Iterable, org.apache.hadop.mapreduce.Reducer.Context) 我尝试翻译了⼀下（英⽂⽔平实在是有限，不对的地⽅还望各位指出）： 在⼀个reduce的调⽤过程中，定义⼀个comparator，对分组在⼀起的key进⾏排序。 通过上⾯这句话就可以理解，为什么khi_123 123和abc_123 123没有叠加在⼀起。

五、总结

- 1、这⾥只写了group的排序，没有写sort，后⾯将会写⼀个，说不定就是今天晚上吧！
- 2、过⼏天写个MR的执⾏流程，并画个图，贴出来⼤家看看。
- 3、对于这块的排序我也是接触不久，可能有写的不对的地⽅。还望朋友们跟贴指出来。
- 4、如果有疑问或是不好跟贴，可以发邮件交流：dajuezhao@gmail.com


