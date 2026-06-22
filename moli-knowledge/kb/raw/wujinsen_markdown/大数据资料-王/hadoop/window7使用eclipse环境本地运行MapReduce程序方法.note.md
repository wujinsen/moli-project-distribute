⼀、编写⽬的

开发的MapReduce在提交到Hadop集群运⾏之前，测试是否有bug，希望能在本地使⽤启动main⽅法的形式查看 是否有错误存在，⽅便程序的检查和修改。本⽂档主要针对Windows环境下进⾏MapReduce开发。 ⼆、环境

系统：Windows7 开发环境：eclipse Hadop版本：2.6.0 准备⼯作：下载hadop-2.6.0.tar.gz，解压到磁盘某⽬录下，然后需要将Hadop2.6.0加⼊到环境变量中，设置如

下：

然后再Path中增加：%HADOP_HOME%\bin; 三、以WordCount为例详述运⾏过程及遇到的问题

- 1、开发WordCount程序 public clas WordCount {


public static clas TokenizerMaperextends Maper<Object, Text, Text, IntWritable>{ private final staticIntWritable one = new IntWritable(1); private Text word = newText(); public void map(Objectkey, Text value, Context context) throws IOException, InterruptedException {

StringTokenizer itr =new StringTokenizer(value.toString(); while(itr.hasMoreTokens() {

word.set(itr.nextToken(); context.write(word, one); }

} }

public static clas IntSumReducerextends Reducer<Text,IntWritable,Text,IntWritable> { private IntWritable result = newIntWritable(); public void reduce(Text key,Iterable<IntWritable> values, Context context) throws IOException,InterruptedException {

int sum = 0; for (IntWritable val : values){

sum += val.get(); } result.set(sum); context.write(key, result);

} } public static void main(String[] args)throws Exception {

Configuration conf = new Configuration(); / 这⾥这么设置就可以了 String[] otherArgs ={"hdfs:/imageHandler1 9 0/tmp/log/test.log","hdfs:/imageHandler1 9 0/tmp/testout 1"}; / 可以是

hdfs上的路径 /String[] otherArgs ={"D:/test.log", "D:/test/wordcountout"}; / 也可以是本地路径

Job job = Job.getInstance(conf,"word count"); job.setJarByClas(LocalWordCount.clas); job.setMaperClas(TokenizerMaper.clas); job.setCombinerClas(IntSumReducer.clas); job.setReducerClas(IntSumReducer.clas); job.setOutputKeyClas(Text.clas);

job.setOutputValueClas(IntWritable.clas); for (int i = 0; i <otherArgs.length - 1; +i) { FileInputFormat.adInputPath(job, new Path(otherArgs[i]); } FileOutputFormat.setOutputPath(job,new Path(otherArgs[otherArgs.length - 1]);

System.exit(job.waitForCompletion(true) ? 0 : 1);

} }

- 2、运⾏WordCount


- （1）此时使⽤Run as->Java Aplication运⾏，会报如下类似错误： 2015-01-2 15 31 47,782 [main] WARN org.apache.hadop.util.NativeCodeLoader(NativeCodeLoader.java:62) - Unable to load native-hadop library for yourplatform. using builtin-java clases where aplicable 2015-01-2 15 31 47,793 [main] ERORorg.apache.hadop.util.Shel (Shel.java:373) - Failed to locate the winutilsbinary in the hadop binary path java.io.IOException: Could not locate executableD:\hbl_study\hadop2\hadop-2.6.0\bin\winutils.exe in the Hadop binaries.

atorg.apache.hadop.util.Shel.getQualifiedBinPath(Shel.java:35) atorg.apache.hadop.util.Shel.getWinUtilsPath(Shel.java:370)

.

该错误是找不到winutils.exe，需要将winutils.exe拷贝到hadop2.6.0/bin⽬录下，winutils.exe如下：

- （2）再次运⾏报错类似： Exception in thread "main" java.lang.UnsatisfiedLinkError: org.apache.hadop.io.nativeio.NativeIO$Windows.aces0(Ljava/lang/String;I)Z


atorg.apache.hadop.io.nativeio.NativeIO$Windows.aces0(Native Method) atorg.apache.hadop.io.nativeio.NativeIO$Windows.aces(NativeIO.java: 57) at org.apache.hadop.fs.FileUtil.canRead(FileUtil.java:977)

.

该错误是缺少hadop.dl(hadop2.6.0编译的版本)⽂件，需要将hadop.dl拷贝到hadop2.6.0/bin⽬录下， hadop.dl如下：

再次运⾏没有报错。 说明：在⽹上有很多hadop.dl资源，我开始下载了⼀个，放⼊hadop2.6.0/bin后报错如下：

java.lang.UnsatisfiedLinkError:org.apache.hadop.util.NativeCrc32.nativeComputeChunkedSumsByteArray(I[BI[BILjava/lang/S tring;JZ)V

atorg.apache.hadop.util.NativeCrc32.nativeComputeChunkedSumsByteArray(NativeMethod) at org.apache.hadop.util.NativeCrc32.calculateChunkedSumsByteArray(NativeCrc32.java:86) atorg.apache.hadop.util.DataChecksum.calculateChunkedSums(DataChecksum.java:430)

.

这是由于我下载的hadop.dl是hadop2.2.0编译成的⽂件，⽹上⼤部分hadop.dl都是hadop2.2.0编译⽽成的，因 此在使⽤hadop2.6.0运⾏程序时会报错，推测可能是版本不匹配或者对应的类已经发⽣了改变，原来版本编译的 hadop.dl已经不适⽤。因此我⾃⼰编译了⼀个hadop2.6.0对应的hadop.dl，问题得到解决。如果以后hadop继 续进⾏升级，我编译好的hadop.dl也不再使⽤，因此下⾯我分享⼀下我的编译⽅法，以供版本变化后可以⾃⼰编译 该⽂件。 四、Window7编译Hadop2.6.0源码⽣成hadop.dl 说明：在Windows7环境中我并没有将源码完全编译成功，只是成功⽣成了hadop.dl。我暂没有找到在Windows7 下编译全部hadop源码成功的⽅法。

- 1、准备⼯作：

- （1）下载hadop-2.6.0-src.tar.gz
- （2）Microsoft Windows SDK v7.1或Visual Studio 2010
- （3）Maven3.0以上，我使⽤的3.1.1，安装后需要配置环境变量如下

在Path中加⼊：%maven_home%\bin; 输⼊mvn -version验证。

- （4）Protocol Bufers 2.5.0，现在已经下载不到，附上该附件如下：

安装⽅法：解压protobuf-2.5.0.tar.gz到某⽬录下，例如D:\protobuf-2.5.0，解压protoc-2.5.0-win32.zip获得 protoc.exe，将protoc.exe放⼊D:/protobuf-2.5.0⽬录下，并在环境变量Path中加⼊D:\protobuf-2.5.0。打开命令⾏ 输⼊“protoc-version”验证，若显⽰libprotoc 2.5.0代表安装成功。

- （5）Cygwin

- （6）JDK 1.6+，我使⽤的是JDK1.7.0_60
- （7）CMake2.6以上，我⽤的版本是3.1.0，cmake-3.1.0-win32-x86.zip 解压后配置环境变量：

在Path中加⼊%CMAKE_HOME%\bin;

- （8）畅通的⽹络


- 2、开始编译 如果使⽤Microsoft Windows SDK v7.1，需要打开“开始” -“所有程序” -“Microsoft Windows SDK v7.1” -“Windows SDK 7.1 Comand Prompt”，进⼊VC+的命令⾏⼯具（⼀定要从此处进⼊⽅可顺利编译Hadop源代码，记着是以 管理员⾝份运⾏）。 切换⾄源代码根⽬录，执⾏编译命令：mvn package -Pdist,native-win-DskipTests -Dtar 等待⼀段时间会有⼀个类似下⾯的报错： [EROR] Failed to execute goalorg.codehaus.mojo:exec-maven-plugin:1.2:exec (compile-ms-winutils) on projecthadopco mon: Co mand execution failed. Proces exited with an error: 1(Exitvalue: 1) -> [Help 1] [EROR] [EROR] To se the ful stack trace of the errors, re-run Maven with the-e switch. [EROR] Re-run Maven using the -X switch to enable ful debug loging. [EROR] [EROR] For more information about the errors and posible solutions,please read the folowing articles: [EROR] [Help 1] [EROR] [EROR] After correcting the problems, you can resume the build with theco mand [EROR] mvn <goals> -rf:hadop-co mon ⽬前我没有找到好的解决办法，只能修改hadop2.6.0\hadop-comon-project\hadop-comon⽬录下的 pom.xml⽂件：搜索“${basedir}/src/main/winutils/winutils.sln”，将这段代码所在的<execution>注释掉。


# htp:/cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException

<!-<execution>

<id>compile-ms-winutils</id> <phase>compile</phase> <goals> <goal>exec</goal>

</goals> <configuration> <executable>msbuild</executable> <arguments> <argument>${basedir}/src/main/winutils/winutils.sln</argument> <argument>/nologo</argument> <argument>/p:Configuration=Release</argument> <argument>/p:OutDir=${project.build.directory}/bin/</argument> <argument>/p:IntermediateOutputPath=${project.build.directory}/winutils/</argument> <argument>/p:WsceConfigDir=${wsce.config.dir}</argument> <argument>/p:WsceConfigFile=${wsce.config.file}</argument> </arguments> </configuration> </execution>-> 再进⾏编译，后来会报错，但是此时在hadop2.6.0\hadop-comon-project\hadop-comon\target\hadopcomon-2.6.0\bin⽬录下已经⽣成了hadop.dl⽂件，我们的⽬的达到了。 如果使⽤VS2010，需要在Path中加⼊“C:\Windows\Microsoft.NET\Framework64\v4.0.30319”。然后打开命令提⽰ 符进⼊到源码根⽬录输⼊编译命令即可。 这两种⽅式我都亲测过。 以上任何对环境变量的修改，都需要重新启动电脑使配置⽣效，因此可将所需软件全部安装配置好后再重启电脑。

