ava命令中⽤-d32和-d64来表明程序运⾏在32位或者64位环境。如果JVM本身不⽀持该参数，会报告 错误： Java代码

- 1.
- 2.
- 3.


java -d32 Unrecognized option: -d32 Could not create the Java virtual machine.

当前仅有java hotspot server VM⽀持64位模式。选择 "-server"选项必须使⽤-d64；"-client"选项会 忽略使⽤-d64；如果没有指定-d32或者-d64，则默认运⾏在32位模式。除⾮仅有64位系统。

⽬前在windows平台下jdk1.5,JDK1.6不⽀持-d32,-d64的选项，jdk1.7⽀持-d32,-d64的选项 在linux平台下jdk1.5, 1.6, 1.7均⽀持-d32,-d64的选项

