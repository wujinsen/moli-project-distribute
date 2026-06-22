前段时间⾯试Hadop,有碰到⼀个题⽬,给⼤家⼀个参考 Hadop笔试题: 找出不同⼈的共同好友(要考虑数据去重) 例⼦: 张三:李四,王五,赵六 李四:张三,⽥七,王五 实际⼯作中,数据去重⽤的还是挺多的,包括空值的过滤等等,本⽂就 数据去重 与 倒排索引 详细讲解⼀ 下. ⼀、数据去重[模拟某运营商呼叫详单去重] 项⽬中统计数据集的种类个数、⽹站⽇志⽂件计算访问地等案例都会涉及到数据去重,重复数据删除等 都是经常使⽤的存储数据缩减技术.通过⼀个简单案例来说明MapReduce怎么实现数据去重.

①原始模拟数据[c呼出,b呼⼊]

- 137 1 c

- 136 1 b
- 137 1 b


- 137 2 c


- 136 1 c
- 137 1 b


- 136 1 b
- 137 1 b


- 137 2 b


- 136 1 c 将同⼀条数据所有记录交给⼀台Reduce,最终结果输出⼀次即可. Map阶段采⽤Hadop默认作业输⼊⽅式后,输⼊的value作为输出的key.


/Maper任务

static clas DMap extends Maper<LongWritable,Text,Text,Text>{ private static Text line = new Text(); protected void map(LongWritable k1,Text v1,Context context){

line = v1; Text text = new Text(“”);

try {

context.write(line,text); } catch (IOException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} };

} /Reducer任务 static clas DReduce extends Reducer<Text,Text,Text,Text>{

protected void reduce(Text k2,Iterable<Text> v2s,Context context){ Text text = new Text(“”); try {

context.write(k2, text); } catch (IOException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} };

} } /初始化参数

public static final String HOST_PATH=”hdfs:/v:9 0″; /读取⽂件路径【需要⼿动创建】 public static final String INPUT_PATH=HOST_PATH+”/ Din”; /输出⽂件路径

public static final String OUTPUT_PATH=HOST_PATH+”/ Dout”; /执⾏mapreduce任务驱动

public static void main (String[] args) throws Exception{ final Configuration conf = new Configuration();

FileSystem fs = FileSystem.get(new URI(HOST_PATH), conf); if(fs.exists(new Path(OUTPUT_PATH){

fs.delete(new Path(OUTPUT_PATH), true); }

/创建job对象 final Job job = new Job(conf);

/通知job⽂件输⼊路径 FileInputFormat.setInputPaths(job, INPUT_PATH); /通知job⽂件输出路径 FileOutputFormat.setOutputPath(job, new Path(OUTPUT_PATH);

/通知job将输⼊⽂件解析成键值对的⽅式【默认可省略】 job.setInputFormatClas(TextInputFormat.clas);

/调⽤⾃定义的Maper函数 job.setMaperClas( DMap.clas);

/设置k2,v2类型,如果<k2,v2><k3,v3>类型⼀致,可以省略 job.setMapOutputKeyClas(Text.clas); job.setMapOutputValueClas(Text.clas);

/调⽤⾃定义的Reducer函数 job.setReducerClas( DReduce.clas);

/设置k3,v3类型 job.setOutputKeyClas(Text.clas); job.setOutputValueClas(Text.clas);

/通知job将<k3,v3>写⼊HDFS中的⽅式[默认值,可省略] job.setOutputFormatClas(TextOutputFormat.clas);

/执⾏job job.waitForCompletion(true);

}

- 136 1 b
- 136 1 c


- 137 1 b


- 137 1 c
- 137 2 b


- 137 2 c ⼆、倒排索引(Inverted Index) 倒排索引是⽂档检索系统最常⽤的数据结构,⼴泛⽤于 全⽂搜索引擎 . 主要⽤来存储某个单词或词组在某个⽂档或⼀组⽂档中存储位置的映射,即提供了⼀种根据内容来查找 ⽂档的⽅式.


因为不是根据⽂档来确定⽂档内容,⽽是进⾏相反操作,所以叫做倒排索引. 实际应⽤中, 每个⽂档对应⼀个权重值,此⽤来指每个⽂档与搜索内容的相关度. 最常⽤使⽤词频作为权重值 ,即记录单词在⽂档中出现的次数.

![image 1](<详解MapReduce实现数据去重与倒排索引应用场景案例.note_images/imageFile1.png>)

更复杂的权重或还要记录单词在多个⽂档中出现过,以实现TF-IDF(Term Frequency-Inverse Document Frequency)算法,或考虑单词在⽂档中的位置等等.

- File1 MapReduce is simple
- File2 MapReduce is powerful is simple
- File3 Helo MapReduce bye MapReduce 关注的信息:单词、⽂档URL、词频. <key,value>类似:<”MapReduce” File1.txt 1> <key,value>对只能有两个值,根据需求需要将 File1.txt 1合并作为value . 单词作为key的好处 :利⽤MR框架默认的排序,将同⼀⽂档的相同单词的词频组成列表,传递给Combine 过程. URL与词频合并为value的好处 :利⽤MR框架默认的HashPartitioner类完成Shufle过程,将相同单词的 所有记录发送给同⼀个Reducer处理. 通过⼀个Reduce⽆法同时完成词频统计与⽣成⽂档列表,需要添加Combine过程完成词频统计. Combine过程将key值相同的value值累加,获取该key(单词)在本⽂档中的词频数. 将相同key的value组合成倒排索引⽂件所需的格式即可. 单词⽂件不宜过⼤,要保证每个⽂件对应⼀个split,否则由于Reduce过程没有进⼀步统计词频,最终结果 可能会出现词频未统计完全的单词,可通过重写InputFormat类将每个⽂件作为⼀个split.还可利⽤复合 键值对等实现包含更多信息的倒排索引.


/Maper任务

static clasIMap extends Maper<LongWritable,Text,Text,Text>{ private static Text key = new Text();/单词和URL private static Text value = new Text();/词频 private FileSplit fileSplit;/Split对象 protected void map(LongWritable k1,Text v1,Context context){

/获取<k1,v1>对所属的FileSplit对象 FileSplit fileSplit = (FileSplit) context.getInputSplit(); StringTokenizer stringTokenizer = new StringTokenizer(v1.toString(); while(stringTokenizer.hasMoreTokens(){

int indexOf = fileSplit.getPath().toString().indexOf(“File”); key.set(stringTokenizer.nextToken()+”:”+fileSplit.getPath().toString().substring(indexOf); value.set(“1″); try {

context.write(key, value); } catch (IOException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} }

}; }

/Combiner任务 static clasICombiner extends Reducer<Text,Text,Text,Text>{ private Text text = new Text(); protected void reduce(Text key, Iterable<Text> values, Context context) {

/统计词频 int sum = 0; for(Text value : values){ sum += Integer.parseInt(value.toString(); } int splitIndex = key.toString().indexOf(“:”);

/重设value值 URL词频合并 text.set(key.toString().substring(splitIndex+1)+”:”+sum);

/重设key为单词 key.set(key.toString().substring(0,splitIndex); try { context.write(key, text); } catch (IOException e) {

/ TODO Auto-generated catch block e.printStackTrace(); } catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} }; }

/Reducer任务

static clasIReduce extends Reducer<Text,Text,Text,Text>{ private Text v3 = new Text(); protected void reduce(Text k2,Iterable<Text> v2s,Context context){

/⽣成⽂档列表 String fileList = new String(); for (Text value : v2s) {

fileList +=value.toString()+”;”; } v3.set(fileList); try {

context.write(k2, v3); } catch (IOException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

}

}; } /初始化参数 public static final String HOST_PATH=”hdfs:/v:9 0″;

/读取⽂件路径【需要⼿动创建】 public static final String INPUT_PATH=HOST_PATH+”/Iin”; /输出⽂件路径 public static final String OUTPUT_PATH=HOST_PATH+”/Iout”;

/执⾏mapreduce任务驱动 public static void main (String[] args) throws Exception{ final Configuration conf = new Configuration();

FileSystem fs = FileSystem.get(new URI(HOST_PATH), conf); if(fs.exists(new Path(OUTPUT_PATH){

fs.delete(new Path(OUTPUT_PATH), true); }

/创建job对象 final Job job = new Job(conf);

/通知job⽂件输⼊路径 FileInputFormat.setInputPaths(job, INPUT_PATH); /通知job⽂件输出路径 FileOutputFormat.setOutputPath(job, new Path(OUTPUT_PATH);

/通知job将输⼊⽂件解析成键值对的⽅式【默认可省略】 job.setInputFormatClas(TextInputFormat.clas);

/调⽤⾃定义的Maper函数 job.setMaperClas(IMap.clas);

/设置k2,v2类型,如果<k2,v2><k3,v3>类型⼀致,可以省略 job.setMapOutputKeyClas(Text.clas); job.setMapOutputValueClas(Text.clas);

job.setCombinerClas(ICombiner.clas);

/调⽤⾃定义的Reducer函数 job.setReducerClas(IReduce.clas);

/设置k3,v3类型 job.setOutputKeyClas(Text.clas); job.setOutputValueClas(Text.clas);

/通知job将<k3,v3>写⼊HDFS中的⽅式[默认值,可省略] job.setOutputFormatClas(TextOutputFormat.clas);

/执⾏job job.waitForCompletion(true);

} Helo File3.txt:1; MapReduce File3.txt:2;File1.txt:1;File2.txt:1;

bye File3.txt:1; is File1.txt:1;File2.txt:2; powerful File2.txt:1; simple File2.txt:1;File1.txt:1;

