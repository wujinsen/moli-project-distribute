12/04/24 15 32  4 WARN util.NativeCodeLoader: Unable to load native-hadop library for your platform. using builtin-java clases where aplicable12/04/24 15 32  4 EROR security.UserGroupInformation: PriviledgedActionException as:Administrator cause:java.io.IOException: Failed to set permisions of path: \tmp\hadopAdministrator\mapred\staging\Administrator-519341271\.staging to 070Exception in thread "main" java.io.IOException: Failed to set permisions of path: \tmp\hadopAdministrator\mapred\staging\Administrator-519341271\.staging to 070 at org.apache.hadop.fs.FileUtil.checkReturnValue(FileUtil.java:682) at org.apache.hadop.fs.FileUtil.setPermision(FileUtil.java:65) at org.apache.hadop.fs.RawLocalFileSystem.setPermision(RawLocalFileSystem.java:509) at org.apache.hadop.fs.RawLocalFileSystem.mkdirs(RawLocalFileSystem.java:34) at org.apache.hadop.fs.FilterFileSystem.mkdirs(FilterFileSystem.java:189) at org.apache.hadop.mapreduce.JobSubmisionFiles.getStagingDir(JobSubmisionFiles.java:16) at org.apache.hadop.mapred.JobClient$2.run(JobClient.java:856) at org.apache.hadop.mapred.JobClient$2.run(JobClient.java:850) at java.security.AcesControler.doPrivileged(Native Method) at javax.security.auth.Subject.doAs(Subject.java:396) at org.apache.hadop.security.UserGroupInformation.doAs(UserGroupInformation.java:1093) at org.apache.hadop.mapred.JobClient.submitJobInternal(JobClient.java:850) at org.apache.hadop.mapreduce.Job.submit(Job.java:50) at org.apache.hadop.mapreduce.Job.waitForCompletion(Job.java:530) at com.hadop.learn.test.WordCountTest.main(WordCountTest.java:85) 这个是Windows下⽂件权限问题，在Linux下可以正常运⾏，不存在这样的问题。解决⽅法是，修 改/hadoop-1.0.2/src/core/org/apache/hadoop/fs/FileUtil.java⾥⾯的checkReturnValue，注释掉即可 （有些粗暴，在Window下，可以不⽤检查）

[java]view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.


.

privatestaticvoid checkReturnValue(bolean rv, File p, FsPermision permision ) throws IOException {

/* if (!rv) { throw new IOException("Failed to set permisions of path: " + p + " to " + String.format("%04o", permision.toShort( );

- 10.
- 11.
- 12.
- 13.


}

*/ }

.

重新编译打包hadop-core-1.0.2.jar，替换掉hadop-1.0.2根⽬录下的hadop-core-1.0.2.jar即可。 这⾥提供⼀份修改版的 ⽂件，替换原hadop-core-1.0.2.jar即可。 替换之后，刷新项⽬，设置好正确的jar包依赖，现在再运⾏WordCountTest，即可。

hadop-core-1.0.2-modified.jar

