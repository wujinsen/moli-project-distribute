本⽂发表于本⼈博客。

今天接着上次【Hadop mapreduce⾃定义排序WritableComparable】⽂章写，按照顺序那么这次 应该是讲解⾃定义分组如何实现，关于操作顺序在这⾥不多说了，需要了解的可以看看我在博客园的 评论，现在开始。

⾸先我们查看下Job这个类，发现有setGroupingComparatorClas()这个⽅法，具体源码如下：

- 01. /**

- 02. * Define the comparator that controls which keys are grouped together

- 03. * for a single call to

- 04. * {@link Reducer#reduce(Object, Iterable,

- 05. * org.apache.hadoop.mapreduce.Reducer.Context)}

- 06. * @param cls the raw comparator to use

- 07. * @throws IllegalStateException if the job is submitted

- 08. */

- 09. public void setGroupingComparatorClass(Class<? extends RawComparator> cls

- 10. ) throws IllegalStateException {

- 11. ensureState(JobState.DEFINE);

- 12. conf.setOutputValueGroupingComparator(cls);

- 13. }


从⽅法的源码可以看出这个⽅法是定义⾃定义键分组功能。设置这个⾃定义分组类必须满⾜extends RawComparator，那我们可以看下这个类的源码：

- 01. /**

- 02. * <p>

- 03. * A {@link Comparator} that operates directly on byte representations of

- 04. * objects.

- 05. * </p>

- 06. * @param <T>

- 07. * @see DeserializerComparator

- 08. */

- 09. public interface RawComparator<T> extends Comparator<T> {

- 10. public int compare( byte [] b1, int s1, int l1, byte []

b2, int s2, int l2);

- 11. }


然⽽这个RawComparator是泛型继承Comparator接⼝的，简单看了下那我们来⾃定义⼀个类继承 RawComparator，代码如下：

- 01. public class MyGrouper implements RawComparator<SortAPI> {

- 02. @Override

- 03. public int compare(SortAPI o1, SortAPI o2) {

- 04. return ( int )(o1.first - o2.first);

- 05. }

- 06. @Override

- 07. public int compare( byte [] b1, int s1, int l1, byte []

b2, int s2, int l2) {

- 08. int compareBytes = WritableComparator.compareBytes(b1, s1, 8 , b2, s2, 8 );

- 09. return compareBytes;

- 10. }


- 11.

- 12. }


源码中SortAPI是上节⾃定义排序中的定义对象，第⼀个⽅法从注释可以看出是⽐较2个参数的⼤⼩， 返回的是⾃然整数；第⼆个⽅法是在反序列化时⽐较，所以需要是⽤字节⽐较。接下来我们继续看看 ⾃定义MyMaper类：

- 01. public class MyMapper extends Mapper<LongWritable, Text, SortAPI, LongWritable>

{

- 02. @Override

- 03. protected void map(LongWritable key, Text value,Context

context) throws IOException, InterruptedException {

- 04. String[] splied = value.toString().split( "\t" );

- 05. try {

- 06. long first = Long.parseLong(splied[ 0 ]);

- 07. long second = Long.parseLong(splied[ 1 ]);

- 08. context.write( new SortAPI(first,second), new LongWritable( 1 ));

- 09. } catch (Exception e) {

- 10. System.out.println(e.getMessage());

- 11. }

- 12. }

- 13. }


⾃定义MyReduce类：

- 1. public class MyReduce extends Reducer<SortAPI, LongWritable, LongWritable,

LongWritable> {

- 2. @Override

- 3. protected void reduce(SortAPI key, Iterable<LongWritable> values, Context

context) throws IOException, InterruptedException {

- 4. context.write( new LongWritable(key.first), new LongWritable(key.second));

- 5. }

- 6.

- 7. }


⾃定义SortAPI类：

- 01. public class SortAPI implements WritableComparable<SortAPI> {

- 02. public Long first;

- 03. public Long second;

- 04. public SortAPI(){

- 05.

- 06. }

- 07. public SortAPI( long first, long second){

- 08. this .first = first;

- 09. this .second = second;

- 10. }

- 11.

- 12. @Override

- 13. public int compareTo(SortAPI o) {

- 14. return ( int ) ( this .first - o.first);


- 15. }

- 16.

- 17. @Override

- 18. public void write(DataOutput out) throws IOException {

- 19. out.writeLong(first);

- 20. out.writeLong(second);

- 21. }

- 22.

- 23. @Override

- 24. public void readFields(DataInput in) throws IOException {

- 25. this .first = in.readLong();

- 26. this .second = in.readLong();

- 27.

- 28. }

- 29.

- 30. @Override

- 31. public int hashCode() {

- 32. return this .first.hashCode() + this .second.hashCode();

- 33. }

- 34.

- 35. @Override

- 36. public boolean equals(Object obj) {

- 37. if (obj instanceof SortAPI){

- 38. SortAPI o = (SortAPI)obj;

- 39. return this .first == o.first && this .second == o.second;

- 40. }

- 41. return false ;

- 42. }

- 43.

- 44. @Override

- 45. public String toString() {

- 46. return "输出：" + this .first + ";" + this .second;

- 47. }

- 48.

- 49. }


接下来准备数据，数据如下：

- 1. 1 2

- 2. 1 1

- 3. 3 0

- 4. 3 2

- 5. 2 2

- 6. 1 2


上传⾄hdfs:/hadop-master:9 0/grouper/input/test.txt，main代码如下：

01. public class Test { 02.

static final String OUTPUT_DIR = " hdfs://hadoop-master :9000/grouper/output/" ; 03. static final String INPUT_DIR = " hdfs://hadoop-master :9000/grouper/input/test.txt" ;

- 04. public static void main(String[] args) throws Exception {

- 05. Configuration conf = new Configuration();

- 06. Job job = new Job(conf, Test. class .getSimpleName());

- 07. job.setJarByClass(Test. class );

- 08. deleteOutputFile(OUTPUT_DIR);

- 09. //1设置输⼊⽬录

- 10. FileInputFormat.setInputPaths(job, INPUT_DIR);

- 11. //2设置输⼊格式化类

- 12. job.setInputFormatClass(TextInputFormat. class );

- 13. //3设置⾃定义Mapper以及键值类型

- 14. job.setMapperClass(MyMapper. class );

- 15. job.setMapOutputKeyClass(SortAPI. class );

- 16. job.setMapOutputValueClass(LongWritable. class );

- 17. //4分区

- 18. job.setPartitionerClass(HashPartitioner. class );

- 19. job.setNumReduceTasks( 1 );

- 20. //5排序分组

- 21. job.setGroupingComparatorClass(MyGrouper. class );

- 22. //6设置在⼀定Reduce以及键值类型

- 23. job.setReducerClass(MyReduce. class );

- 24. job.setOutputKeyClass(LongWritable. class );

- 25. job.setOutputValueClass(LongWritable. class );

- 26. //7设置输出⽬录

- 27. FileOutputFormat.setOutputPath(job, new Path(OUTPUT_DIR));

- 28. //8提交job

- 29. job.waitForCompletion( true );

- 30. }

- 31.

- 32. static void deleteOutputFile(String path) throws Exception{

- 33. Configuration conf = new Configuration();

- 34. FileSystem fs = FileSystem.get( new URI(INPUT_DIR),conf);

- 35. if (fs.exists( new Path(path))){

- 36. fs.delete( new Path(path));

- 37. }

- 38. }

- 39. }


执⾏代码，然后在节点上⽤终端输⼊：hadop fs -text /grouper/output/part-r- 0查看结果：

- 1. 1 2

- 2. 2 2

- 3. 3 0


接下来我们修改下SortAPI类的compareTo()⽅法：

- 01. @Override

- 02. public int compareTo(SortAPI o) {

- 03. long mis = ( this .first - o.first) * - 1 ;

- 04. if (mis != 0 ){

- 05. return ( int )mis;

- 06. }


- 07. else {

- 08. return ( int )( this .second - o.second);

- 09. }

- 10. }


再次执⾏并查看/grouper/output/part-r- 0⽂件：

- 1. 3 0

- 2. 2 2

- 3. 1 1


这样我们就得出了同样的数据分组结果会受到排序算法的影响，⽐如排序是倒序那么分组也是先按照 倒序数据源进⾏分组输出。我们还可以在map函数以及reduce函数中打印记录（过程省略）这样经过 对⽐也得出分组阶段：键值对中key相同(即compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2)⽅ 法返回0)的则为⼀组，当前组再按照顺序选择第⼀个往缓冲区输出(也许会存储到硬盘)。其它的相同 key的键值对就不会再往缓冲区输出了。在百度上检索到这边⽂章，其中它的分组是把map函数输出的 value全部迭代到同⼀个key中，就相当于上⾯{key，value}:{1,{2,1,2},这个结果跟最开始没有⾃定义分 组时是⼀样的，我们可以在reduce函数输出Iterable<LongWritable> values进⾏查看，其实我觉得这 样的才算是分组吧就像数据查询⼀样。

在这⾥我们应该要弄懂分组与分区的区别。分区是对输出结果⽂件进⾏分类拆分⽂件以便更好查

看，⽐如⼀个输出⽂件包含所有状态的htp请求，那么为了⽅便查看通过分区把请求状态分成⼏个结果 ⽂件。分组就是把⼀些相同键的键值对进⾏计算减少输出；分区之后数据全部还是照样输出到reduce 端，⽽分组的话就有所减少了；当然这2个步骤也是不同的阶段执⾏。

这次先到这⾥。坚持记录点点滴滴！

