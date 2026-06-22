# 操作流程：

- 1.得到Configuration对象
- 2.得到FileSystem对象
- 3.进⾏⽂件操作


# Hadop URL读取数据

public clas HDFSURLReader{ static{ URL.setURLStreamHandlerFactory(new FSUrlStreamHandlerFactory();

} public static void main(String args[]){

InputStream is=nul; try{

is=new URL(args[0]).openStream(); IOUtils.copyBytes(is,System.out,1024,false);

}catch(Exception e){

IOUtils.closeStream(is); }

} }

# FileSystem类

public clas FileSystemReader{

public static void main(String args[]){ Configuration con=newConfiguration(); conf.set(“fs.default.name”, “hdfs:/master:9 0”);/如果不写就只能本地操作了

conf.set(“hadop.job.ugi”, “hadop,hadop”);/如果不写系统将按照默认的⽤户进⾏操作 String uri=args[0]; FileSystem fs=FileSystem.get(URI.create(uri),conf); try{

InputStream is=fs.open(new Path(uri); IOUtils.copyBytes(is,System.out,1024,false);

}catch(Exception e){

IOUtils.closeStream(is); }

}

}

# FileSystem类

public clas FileSystemReader{

public static void main(String args[]){ Configuration con=newConfiguration(); conf.set(“fs.default.name”, “hdfs:/master:9 0”);/如果不写就只能本地操作了

conf.set(“hadop.job.ugi”, “hadop,hadop”);/如果不写系统将按照默认的⽤户进⾏操作 String uri=args[0]; FileSystem fs=FileSystem.get(URI.create(uri),conf); try{

fs.copyFromLocalFile(src, dst);/ mkdirs等hdfs操作⽅法 }catch(Exception e){ }

}

}

# FileStatus类

public clas FileStatusMetadata{

public static void main(String args[]){ Configuration con=newConfiguration(); conf.set(“fs.default.name”, “hdfs:/master:9 0”);/如果不写就只能本地操作了

conf.set(“hadop.job.ugi”, “hadop,hadop”);/如果不写系统将按照默认的⽤户进⾏操作

/查看元数据： String uri=args[0]; FileSystem fs=FileSystem.get(URI.create(uri),conf); FileStatus fst=fs.getFileStatus(new Path(uri); if(!fst.isDir(){

System.out.println(“这是个⽂件”);

} System.out.println(“路径：”+fst.getPath(); System.out.println(“长度：”+fst.getLen();

System.out.println(“⽂件修改⽇期：”+new Timestamp(fst.getModificationTime().toString(); System.out.println(“上次⽂件访问⽇期：”+ new Timestamp(fst.getAcesTime().toString(); System.out.println(“⽂件备份数：”+fst.getReplication(); System.out.println(“⽂件块⼤⼩：”+fst.getBlockSize(); System.out.println(“⽂件所有者：”+fst.getOwner(); System.out.println(“⽂件所在分组：”+fst.getGroup(); System.out.println(“⽂件权限：”+fst.getPermision().toString();

}

}

# FileStatus类

public clas FileStatusMetadata{

public static void main(String args[]){ Configuration con=newConfiguration(); conf.set(“fs.default.name”, “hdfs:/master:9 0”);/如果不写就只能本地操作了

conf.set(“hadop.job.ugi”, “hadop,hadop”);/如果不写系统将按照默认的⽤户进⾏操 作

/查看HDFS⽂件的元信息： String uri=args[0]; FileSystem fs=FileSystem.get(URI.create(uri),conf); FileStatus fst=fs.getFileStatus(new Path(uri); if(fst.isDir(){

System.out.println(“这是个⽬录”);

} System.out.println(“⽬录路径：”+fst.getPath(); System.out.println(“⽬录长度：”+fst.getLen(); System.out.println(“⽬录修改⽇期：”+new

Timestamp(fst.getModificationTime().toString(); System.out.println(“上次⽬录访问⽇期：”+ new

Timestamp(fst.getAcesTime().toString(); System.out.println(“⽬录备份数：”+fst.getReplication(); System.out.println(“⽬录块⼤⼩：”+fst.getBlockSize(); System.out.println(“⽬录所有者：”+fst.getOwner();

System.out.println(“⽬录所在分组：”+fst.getGroup(); System.out.println(“⽬录权限：”+fst.getPermision().toString(); for(FileStatus fs:fst.listStatus(new Path(uri ){

System.out.println(fs.getPath(); }

}

# FileStatus类

public clas FileStatusMetadata{

public static void main(String args[]){ Configuration con=newConfiguration(); conf.set(“fs.default.name”, “hdfs:/master:9 0”);/如果不写就只能本地操作了

conf.set(“hadop.job.ugi”, “hadop,hadop”);/如果不写系统将按照默认的⽤户进⾏操 作

/查看某个⽂件Block在HDFS集群的位置： String uri=args[0]; FileSystem fs=FileSystem.get(URI.create(uri),conf); FileStatus fst=fs.getFileStatus(new Path(uri); BlockLocation blks[]=fs.getFileBlockLocations(fst,0,fst.getLen(); int blklen=blks.length; for(int i=0;i<blklen;i +){

String hosts[]=blks[i].getHosts() System.out.println(“block_”+i+”在：”+hosts[0]);

} }

}

# FileStatus类、PathFilter接⼜

clas RegexExcludePathFilter implements PathFilter{ private final String regex; public RegexExcludePathFilter(String regex) {

this.regex = regex;

} public bolean acept(Path path) {

return !path.toString().matches(regex); } }

public clas ListFiles{

public static void main(String args[]){ Configuration con=newConfiguration(); conf.set(“fs.default.name”, “hdfs:/master:9 0”);/如果不写就只能本地操作了

conf.set(“hadop.job.ugi”, “hadop,hadop”);/如果不写系统将按照默认的⽤户进⾏操 作

/查看某个⽂件Block在HDFS集群的位置： String uri=args[0]; FileSystem fs=FileSystem.get(URI.create(uri),conf); FileStatus fst=fs.globStatus(new

Path("hdfs:/master:9 0/user/hadop/test/*"),newRegexExcludePathFilter(".*txt"); /FileStatus[] fst= fs.globStatus(new

Path("hdfs:/master:9 0/user/hadop/test/*"); Path[] listedPaths= FileUtil.stat2Paths(fst); for (Path p : listedPaths){

System.out.println(p);

}

# FSDataInputStream类

public clas FileStatusMetadata{

public static void main(String args[]){ Configuration con=newConfiguration(); conf.set(“fs.default.name”, “hdfs:/master:9 0”);/如果不写就只能本地操作了

conf.set(“hadop.job.ugi”, “hadop,hadop”);/如果不写系统将按照默认的⽤户进⾏操 作

String uri=args[0]; FileSystem fs=FileSystem.get(URI.create(uri),conf);

FSDataInputStream in=nul; try{

in=fs.open(newPath(uri); in.sek(0);/也有sek(int pos)，getPos()⽅法，还有⼀个⽤户不使⽤的⽅法：

sekToNewSource(int pos) /该⽅法在读取失败的时候，hadop系统调⽤从副本中读取数据， /因此hadop有了副本基本是稳定可靠的；读取可以sek，后⾯的写不可以，hadop只能

顺序写，不能随机写

IOUtils.copyBytes(in,System.out,4096,true); }catch(Exception e){

IOUtils.closeStream(in); }

}

}

# FSDataOutputStream类

public clas FileStatusMetadata{

public static void main(String args[]){ Configuration con=newConfiguration(); conf.set(“fs.default.name”, “hdfs:/master:9 0”);/如果不写就只能本地操作了

conf.set(“hadop.job.ugi”, “hadop,hadop”);/如果不写系统将按照默认的⽤户进⾏操 作

String urin=args[0]; InputStream in=new BuferedInputStream(new FileInputStream(utin); String uri=args[1];

FileSystem fs=FileSystem.get(URI.create(uri),conf); FSDataOutputStream out=nul; try{

out=fs.create(newPath(uri),newProgresable(){ public void progres(){

System.out.println(“.”); }

});

IOUtils.copyBytes(in,out,4096,true); }catch(Exception e){

IOUtils.closeStream(out); }

}

}

# DistributedFileSystem类

public clas FileStatusMetadata{

public static void main(String args[]){ Configuration con=newConfiguration(); conf.set(“fs.default.name”, “hdfs:/master:9 0”);/如果不写就只能本地操作了

conf.set(“hadop.job.ugi”, “hadop,hadop”);/如果不写系统将按照默认的⽤户进⾏操 作

String urin=args[0]; FileSystem fs=FileSystem.get(URI.create(uri),conf); try{

DistributedFileSystem hdfs = (DistributedFileSystem) fs; DatanodeInfo[] dataNodeStats = hdfs.getDataNodeStats(); for (int i = 0; i < dataNodeStats.length; i +) {

System.out.println("DataNode_" + i + "_Name:"+ dataNodeStats[i].getHostName();

} } catch (Exception e) { }

}

}

