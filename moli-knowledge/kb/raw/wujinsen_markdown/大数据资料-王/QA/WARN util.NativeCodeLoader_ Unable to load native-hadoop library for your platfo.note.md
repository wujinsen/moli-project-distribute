[rot@db96 hadop]# hadop dfs -put ./in DEPRECATED: Use of this script to execute hdfs comand is deprecated. Instead use the hdfs comand for it.

14/07/17 17 07  2 WARN util.NativeCodeLoader: Unable to load native-hadop library for your platform. using builtin-java clases where aplicable put: `./in': No such file or directory 原因查找： 查看本地⽂件： [rot@db96 hadop]# file /usr/local/hadop/lib/native/libhadop.so.1.0.0 /usr/local/hadop/lib/native/libhadop.so.1.0.0: ELF 32-bit LSB shared object,

Intel 80386, version 1 (SYSV), dynamicaly linked, not stri ped 是32位的hadop，安装在了64位的linux系统上。lib包编译环境不⼀样，所以不能使⽤。 悲剧了，装好的集群没法⽤。

解决办法：重新编译hadop./就是重新编译hadop软件。 (本例⽂是在从库db9上编译。你也可以 在master db96上编译

/只要机器的环境⼀直。)

参考⼿动编译步骤：64位的linux装的hadop是32位的，需要⼿⼯编译 htp:/blog.csdn.net/wulantian/article/details/381 9

上⼀篇64位的linux装的hadop是32位的，需要⼿⼯编译

下⼀篇mysql5.6主从复制新特性测试

