在使⽤Hive的--auxpath过程中，如果我使⽤的是相对路径（例如，--auxpath=abc.jar），会产⽣下⾯的⼀个异常：

java.lang.IlegalArgumentException: Can not create a Path from an empty string at org.apache.hadop.fs.Path.checkPathArg(Path.java:91) at org.apache.hadop.fs.Path.<init>(Path.java: 9) at org.apache.hadop.fs.Path.<init>(Path.java:58) at org.apache.hadop.mapred.JobClient.copyRemoteFiles(JobClient.java:619) at org.apache.hadop.mapred.JobClient.copyAndConfigureFiles(JobClient.java:724) at org.apache.hadop.mapred.JobClient.copyAndConfigureFiles(JobClient.java:648)

从异常的内容来看，是由于使⽤了⼀个空字符串来创建⼀个Path对象。 经过分析发现，使⽤"-auxpath=abc.jar"来启动Hive时，Hive会⾃动在abc.jar前⾯补上"file:/"。也就 是说Hive最后使⽤的路径是"file:/abc.jar"。 当我们使⽤"file:/abc.jar"来⽣成⼀个Path时，调⽤这个Path的getName将会返回 "(空字符串)。⽽ Hive在提交MapReduce的Job时，会使⽤getName来获取⽂件名，并创建⼀个新的Path对象。下⾯的 示例代码演示了⼀下这个过程，会抛出上⽂提到的异常（Hadop的代码本身⽐较复杂，有兴趣看源码 的可以点 ）。

这⾥

<table>
  <tr>
    <th>1</th>
    <th>Path path = new Path( " file://abc.jar " );<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>System.out.println( "path name:"<br><br>+ path.getName());</th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>System.out.println( "authority:"<br><br>+ path.toUri().getAuthority());</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>Path newPath = new Path(path.getName());<br><br></th>
  </tr>
</table>


上⽂的代码输出path name: authority:abc.jar 并抛出了异常"Can not create a Path from an empty string" 那么为什么"file:/abc.jar"⽣成的Path的getName返回的是 "⽽不是"abc.jar"呢，⽽且"abc.jar"却成了 authority？在 中的处理代码如下：

Path

<table>
  <tr>
    <th>0<br><br>1<br></th>
    <th>if (pathString.startsWith( "//" , start) &&<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 2<br><br></th>
    <th>(pathString.length()-start > 2 )) { // has authority<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 3<br><br></th>
    <th>int nextSlash = pathString.indexOf( '/' , start+ 2 );<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 4<br><br></th>
    <th>int authEnd = nextSlash > 0 ? nextSlash : pathString.length();<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 5<br><br></th>
    <th>authority = pathString.substring(start+ 2 , authEnd);<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>0 6<br><br></th>
    <th>start = authEnd;</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 7<br><br></th>
    <th>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>0 8<br><br></th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>0 9<br><br></th>
    <th>// uri path is the rest of the string -- query & fragment not supported</th>
  </tr>
</table>


<table>
  <tr>
    <th>1 0<br><br></th>
    <th>String path = pathString.substring(start, pathString.length());</th>
  </tr>
</table>


# pathString就是传进去的"file:/abc.jar"，由于我们只有两个"/"因此，从第⼆个"/"到结尾的字符串 （"abc.jar"）都被当成了authority，path（内部的成员）则设置成了 "⽽getName返回的就是path， 因此也就为 "了。

# 因此，如果使⽤Hive的 -auxpath来设置jar，必须使⽤绝对路径，或者使⽤"file:/.abc.jar"这样的表示 法。这个才是Hadop的Path⽀持的⽅式。事实上，hadop许多相关的Path的设置，都存在这个问 题，所以在⽆法确定的情况下，就不要使⽤相对路径了。

