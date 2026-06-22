本期操作⽬标操

操作HDFS的常⽤api创建⽬录、上传⽂件、下传⽂件移动⽂件、重命名⽂件、删除⽂件查看HDFS 存储的⽂件列表查看HDFS存储的所有⽂件信息

- 1、创建Maven项⽬（略）
- 2、导⼊Hadop的maven依赖
- 3、创建HDFS客户端 HDFS是⼀个⽂件系统，如同windows的⽂件夹⼀样，与windows不同的是，HDFS⽀持数据分布


- 1 <dependency>

- 2 <groupId>org.apache.hadoop</groupId>

- 3 <artifactId>hadoop-client</artifactId>

- 4 <version>2.6.1</version>

- 5 </dependency>


1

式存储。

- 1 Configuration conf = new Configuration();

- 2 conf.set("fs.defaultFS", "hdfs://hadoop01:9000");

- 3 conf.set("dfs.replication", "3");

- 4 fileSystem = FileSystem.get(conf);


- 1

- 2


1 以上代码在Windows及⾮hadoop集群⽤户组的电脑上，会出错。建议采⽤以下代码进⾏访问

Configuration conf =newConfiguration(); conf.set("dfs.replication","3"); fileSystem= FileSystem.get(newURI("hdfs:/hadop01 9 0"), conf,"rot");4、在HDFS上创建⽬录、上传⽂件、下传⽂件 boleanisDone =fileSystem.mkdirs(newPath("/software/hadop"); Path srcPath =new Path("E:\software\instal\hadop-2.6.1.tar.gz"); Path dstPath =newPath("/software/hadop/hadop2.6.1.tar.gz"); fileSystem.copyFromLocalFile(srcPath, dstPath); Path hadopPath =new Path("/ a/hadop-2.6.1.tar.gz"); Path dstPath =newPath("G:/"); fileSystem.copyToLocalFile(hadopPath, dstPath); 以上第三部分的代码（下传⽂件）在windows⽂件下如果不 适⽤winutils.exe⽂件可能会报错。建议采⽤下⾯的代码进⾏操作。 Path hadoopPath = new Path("/aaa/hadoop-2.6.1.tar.gz"); Path dstPath = new Path("G://");

fileSystem.copyToLocalFile(false, hadopPath, dstPath,true); 四个参数的含义分别是：是否删除HDFS集群下 的原始⽂件、HDFS⽂件地址、本地⽂件地址、是否使⽤本地⽂件系统进⾏操作。 四个参数中，起作⽤的就是是否 使⽤本地⽂件系统进⾏操作这个参数，如果配置了winutils.exe，下传⽂件就会使⽤winutils.exe，如果没有配置建议 使⽤该重载⽅法。 5、在HDFS上移动⽂件、重命名⽂件、删除⽂件 Path srcPath =newPath("/ a/hadop-

2.6.1.tar.gz"); Path disPath =newPath("/itcast/hadop/hadop-2.6.1.tar.gz"); fileSystem.rename(srcPath,disPath); fileSystem.delete(newPath("/software"),true); rename⽅法可以⽤ 来移动⽂件，也可以⽤来重命名⽂件。 delete⽅法可以⽤来递归删除某个⽬录下的⽂件信息6、查看HDFS上的⽂件 信息 RemoteIterator<LocatedFileStatus> fileLists =fileSystem.listFiles(newPath("/"),true); while (fileLists.hasNext() { LocatedFileStatus locatedFileStatus = fileLists.next(); /按照以下格式打印HDFS上 的⽂件信息 / drwxr-xr-x - rot supergroup 0 2015-12-18 0 24 /itcast/hadop String fileType = "-"; System.out.print(fileType);/但因⽂件的类型 String authority = locatedFileStatus.getPermision().toString(); System.out.print(authority +"\t");/打印⽂件的权限 String user = locatedFileStatus.getOwner(); System.out.print(user +"\t");/打印⽂件所属的的⽤户 longsize = locatedFileStatus.getLen(); System.out.print(size +"\t");/打印⽂件的⼤⼩ longdate = locatedFileStatus.getModificationTime(); System.out.print(date +"\t");/打印⽂件的时间戳 String path

= locatedFileStatus.getPath().toString(); System.out.print(path +"\t");/打印⽂件的路径 System.out.println(); for(BlockLocation blockLocation : locatedFileStatus.getBlockLocations() { System.out.print("cacheHosts: "); for(String hosts : blockLocation.getCachedHosts() { System.out.print(hosts +"，");/打印block的cachehosts } System.out.println(); System.out.print("hosts: "); for(String hosts : blockLocation.getHosts() { System.out.print(hosts

+"，");/打印⽂件block所在的服务器 } System.out.println(); System.out.print("block size: "); System.out.println(blockLocation.getLength();/打印block的⼤⼩ System.out.print("block start ofset: "); System.out.println(blockLocation.getOfset(); /⽂件开始偏移量 System.out.println(" -

-"); } } 为什么使⽤RemoteIterator<LocatedFileStatus>，这是⼀个远程的迭代 器，由于HDFS服务器数据量⽐较多，不能⼀次性返回所有⽂件信息(可能撑爆内存)，所以⼀边迭代⼀边从远程服务 器上获取。分治的思想。 程序运⾏后，打印效果如下：

![image 1](<第四节 HDFS Java API 使用.note_images/imageFile1.png>)

7、查看HDFS的所有⽂件信息(使⽤递归算法) public voidfileAl()throwsException { printContent(new Path("/"); } public voidprintContent(Path path)throwsException { FileStatus[] fileStatuses = fileSystem.listStatus(path); for(FileStatus fileStatus : fileStatuses) { String fileType ="d"; if (fileStatus.isFile() { fileType ="-"; }else if(fileStatus.isSymlink() { fileType ="l"; }

System.out.println(fileType + fileStatus.getPermission() + "\t" + fileStatus.getOwner () + "\t" + fileStatus.getLen() + "\t" + fileStatus.getModificationTime() + "\t" + fi leStatus.getPath()); if(fileStatus.isDirectory() { printContent(fileStatus.getPath();

} } } 运⾏程序后，打印效果如下：

