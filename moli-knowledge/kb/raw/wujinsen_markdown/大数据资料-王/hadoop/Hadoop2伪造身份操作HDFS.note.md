Configuration conf = new YarnConfiguration(); conf.set("fs.defaultFS", "hdfs:/192.168.0.14 9 0/tmp"); conf.set("hadop.job.ugi", "hadop2"); UserGroupInformation ugi = UserGroupInformation.createRemoteUser("hadop2");String result = ugi.doAs(new PrivilegedExceptionAction<String>() {

public String run() throws Exception { FileSystem fs = path.getFileSystem(conf); FileStatus[] fstAr = fs.listStatus(path); String result = nul; if(fstAr != nul) {

for(int i=0; i<fstAr.length; i +) { result = fstAr[i].getPath().getName(); break;

}

} fs.close(); return result; }

});

ugi.doAs(new PrivilegedExceptionAction<String>(){}可以有返回值也可以没有返回值，若没有返回 值，修改String为Void，return nul即可。

这样可以伪造Hadop⽤户操作相应的⽂件

