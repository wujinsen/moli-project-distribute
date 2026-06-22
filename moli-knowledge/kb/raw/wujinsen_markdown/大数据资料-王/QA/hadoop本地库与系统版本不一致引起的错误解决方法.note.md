部署hadop的集群环境为 操作系统 centos 5.8 hadop版本为cloudera hadop-0.20.2-cdh3u3 集群中设置⽀持gzip lzo压缩后，在对压缩⽂件进⾏读取或者对输⼊⽂件压缩的时候要使⽤到hadop 的本地库，本地库的默认位置在 $HADOP_HOME/lib/native/Linux-amd64-64 (64位操作系统) $HADOP_HOME/lib/native/Linux-i386-32 （32位操作系统） ⽂件夹中的libhadop.so⽂件，就是hadop的本地库。 如果本地库不存在，或者本地库与当前操作系统的版本不⼀致的时候，会报下⾯的错误：

- 1/09/20 17 29 49 WARN util.NativeCodeLoader: Unable to load native-hadop library for your


platform… using builtin-java clases where aplicable 增加调试信息设置 $ export HADOP_ROT_LOGER=DEBUG,console $ hadop fs -text /test/data/origz/aces.log.gz 2012-04-24 15  5 43,269 WARN org.apache.hadop.util.NativeCodeLoader: Unable to load native-hadop library for your platform. using builtin-java clases where aplicable eror libhadop.so /lib64/libc.so.6 required (libc 2.6) /usr/local/hadop/lib/native/Linux-amd6464 说明系统中的glibc的版本和libhadop.so需要的版本不⼀致导致 查看系统的libc版本 #l /lib64/libc.so.6 lrwxrwxrwx 1 rot rot 1 Apr 24 16 49 /lib64/libc.so.6 -> libc-2.5.so 系统中的版本为2.5 将系统中的glibc升级为2.9 下载glibcwget 下载glibc-linuxthreadswget

htp:/ftp.gnu.org/gnu/glibc/glibc-2.9.tar.bz2 htp:/ftp.gnu.or g/gnu/glibc/glibc-linuxthreads-2.5.tar.bz2

解压$tar -jxvf glibc-2.9.tar.bz2$cd glibc-2.9$tar -jxvf ../glibc-linuxthreads-2.5.tar.bz2$cd ..$export CFLAGS="g -O2"$./glibc-2.9/conﬁgure --preﬁx=/usr --disable-proﬁle --enable-add-ons --with-headers=/usr/include -with-binutils=/usr/bin$make#make install 安装编译过程中需要注意三点：1、要将glibc-linuxthreads解压到glibc⽬录下。2、不能在glibc当前⽬录 下运⾏conﬁgure。3、加上优化开关，export CFLAGS="-g -O2"，否则会出现错误安装完后，可以查看ls

-l /lib/libc.so.6已升级lrwxrwxrwx 1 root root 11 Apr 24 16:49 /lib64/libc.so.6 -> libc-2.9.so 测试本地库是否升级 $ export HADOP_ROT_LOGER=DEBUG,console $ hadop fs -text /test/data/origz/aces.log.gz12/04/25 08 54 47 INFO lzo.LzoCodec: Sucesfuly loaded & initialized native-lzo library [hadop-lzo rev 6b1b7f8b904d8df9b4d2b641db7658ab3cf8]

12/04/25 08 54 47 DEBUG util.NativeCodeLoader: Trying to load the custom-built native-hadop library. 12/04/25 08 54 47 INFO util.NativeCodeLoader: Loaded the native-hadop library12/04/25 08 54 47 INFO zlib.ZlibFactory: Sucesfuly loaded & initialized native-zlib library 12/04/25 08 54 47 DEBUG fs.FSInputChecker: DFSClient readChunk got seqno 0 ofsetInBlock 0 lastPacketInBlock false packetLen 13210

可以看到将glibc升级后不再报错，已经成功加载本地库

上⼀篇centos 5.8升级python 2.4到2.7

下⼀篇yum卡住/rpm -qa卡住的解决⽅法

顶

- 2 踩

- 0 主题推荐


猜你在找

查看评论 4楼 2013-12-04 09 17发表

可以留个您的联系⽅式吗？ Q或邮箱 谢谢

- 3楼 2013-12-04 09 16发表


hadop库操作系统32位64位

Eclipse下使⽤Hadop单机模式调试MapReduce程序 hadop dfsadmin -refreshNodes 命令详解 maven 打包可执⾏jar的⽅法 KMP算法原理与实现（精简） Ganglia安装详解（CentOS_5.5_Final版） ThreadGroup基本⽤法 storm环境搭建 -Linux Cobar的架构与实践 去哪⼉⽹⾯试问题 C+学习之深⼊理解虚函数 -虚函数表解析

执着前⾏的冰 [回复]

执着前⾏的冰 [回复]

您好，我是Hadop初学者，我也出现WARN util.NativeCodeLoader: Unable to load native-hadop library for your platform. using builtin-java clases where aplicable

您的那个“增加调试信息设置”是怎么调试的我不理解，望您指教。谢谢！ 2楼 2013-05-21 14 50发表

vigiles [回复]

你好！ 我在win7中访问ubuntu中的hadop，发⽣ WARN util.NativeCodeLoader:

Unable to load native-hadop library for your platform. using builtin-java clases where aplicable 但不知道具体如何重新编译这个jar？

- 1楼 2013-04-1 0 36发表


pf1234321 [回复][引⽤][举报]

$./glibc-2.7/configure-prefix=/usr-disable-profile-enable-ad-ons-withheaders=/usr/include-with-binutils=/usr/bin 这⼀步是不是把2.7改成2.9 然后这条语句是什么意思呢 我执⾏报错了 搞不懂？能帮忙解答下吗 谢谢！ Re: 2013-04-1 12 06发表

jiedushi [回复][引⽤][举报]

回复pf1234321：谢谢提醒，已经修改为2.9 这个语句就是配置glibc的安装选项

