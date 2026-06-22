Eclipse⾥⾯写了个测试程序：把HDFS中的数据批量导⼊到HBase中

写好后，在本地测试遇到了如下问题： 14/04/21 16 49 53 WARN util.NativeCodeLoader: Unable to load native-hadop library for your platform. using builtin-java clases where aplicable 14/04/21 16 49 53 EROR security.UserGroupInformation: PriviledgedActionException as:admin cause:java.io.IOException: Failed to set permisions of path: \tmp\hadopadmin\mapred\staging\admin-151842785\.staging to 070

# Exception in thread "main" java.io.IOException: Failed to set permisions of path: \tmp\hadop-admin\mapred\staging\admin-151842785\.staging to 070

at org.apache.hadop.fs.FileUtil.checkReturnValue(FileUtil.java:690) at org.apache.hadop.fs.FileUtil.setPermision(FileUtil.java: 62) at org.apache.hadop.fs.RawLocalFileSystem.setPermision(RawLocalFileSystem.java:509) at org.apache.hadop.fs.RawLocalFileSystem.mkdirs(RawLocalFileSystem.java:34) at org.apache.hadop.fs.FilterFileSystem.mkdirs(FilterFileSystem.java:189) at org.apache.hadop.mapreduce.JobSubmisionFiles.getStagingDir(JobSubmisionFiles.java:16) at org.apache.hadop.mapred.JobClient$2.run(JobClient.java:918) at org.apache.hadop.mapred.JobClient$2.run(JobClient.java:1) at java.security.AcesControler.doPrivileged(Native Method) at javax.security.auth.Subject.doAs(Subject.java:396) at org.apache.hadop.security.UserGroupInformation.doAs(UserGroupInformation.java:149) at org.apache.hadop.mapred.JobClient.submitJobInternal(JobClient.java:912) at org.apache.hadop.mapreduce.Job.submit(Job.java:50) at org.apache.hadop.mapreduce.Job.waitForCompletion(Job.java:530) at com.ploc.hadop.hbase.BatchImport.main(BatchImport.java:104)

此时我把程序打包到Linux上运⾏是okey的，那说明是Windows下⽂件权限问题， 然后根据at org.apache.hadoop.fs.FileUtil.checkReturnValue(FileUtil.java:690)这个错误信息找到了源码， 然后⼀步步调试到Win32FileSystem.class中的 public native boolean setPermission(File f, int access, boolean enable, boolean owneronly);⽅法， 这个native格式的⽅法可能在c或者c++⾥⾯调⽤了，没法继续跟踪了，所以我这⾥的解决⽅法是把 private static void checkReturnValue(boolean rv, File p, FsPermission permission

) throws IOException { if (!rv) { throw new IOException("Failed to set permissions of path: " + p +

" to " + String.format("%04o", permission.toShort())); } }

标记经⾊部分的代码先注释掉，保证在windows下测试通过。

