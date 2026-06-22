设置参数：-Xverify:none -Xms512m -Xmx512m -Xmn128m -X PermSize=96m -X MaxPermSize=96m X:+UseConcMarkSwepGC -X:+UseParNewGC -Xnoclasgc -X CMSInitiatingOcupancyFraction=85

参数解释：

- -Xverify:none 禁⽌字节码验证过程
- -Xms512m 初始化对内存
- -Xmx512m 最⼤堆内存
- -Xmn128m 年轻代内存
- -X PermSize=96m 永久代内存
- -X MaxPermSize=96m 最⼤永久代
- -X:+UseConcMarkSwepGC ⽼年代CMS收集器
- -X:+UseParNewGC 新⽣代ParNew收集器

- -Xnoclasgc 关闭CLAS的垃圾回收功能,就是虚拟机加载的类,即便是不使⽤,没有实例也不会回收
- -X CMSInitiatingOcupancyFraction=85 使⽤cms作为垃圾回收使⽤70％后开始CMS收集

在eclipse中的设置⽅法

- ⽅法1：对应在Eclipse中的设置为:窗⼝->⾸选项->JAVA->已安装的JRE,在缺省的VM⾃变量中增加:

-Xmx1024M -server -d64 -X:+NewRatio=12 -X:+UseParalelGC -X UseParalelOldGC

- ⽅法2：修改eclipse.ini-vmargs -Xms128M -Xmx512M -X PermSize=64M X MaxPermSize=128M




