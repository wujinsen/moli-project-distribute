在安装QTP或LoadRuner时，可能会遇到“安装程序已确定挂起重新启动，是否要⽴即退出安装以重新 启动系统”的提示，但是重启电脑后再次启动安装程序，仍然有此错误提示，以下是我安装QTP时遇到 的⼀个提示，如下图所示：

![image 1](<LoadRunner时提示“安装程序已确定挂起重新启动”解决方案.note_images/imageFile1.png>)

这⾥给出问题产⽣原因以及可能的解决⽅案： 原因: 这是因为第⼀次安装失败(或者之前卸载不⼲净)，但已经安装了部分软件造成的。 解决⽅法:

- 1、如果重新启动后，⼀样⽆效，
- 2、那么就进⼊注册表编辑器，通过修改注册表的⽅法来解决。除了删除QTP或LoadRuner安装时遗 留下的垃圾⽂件，还必须进⾏以下操作： 在"开始"－"运⾏"中输⼊regedit，进⼊注册表编辑器，依次 查找 HKEY_LOCAL_MACHINE\SYSTEM\CurentControlSet\Control\Sesion Manager，找到 “PendingFileRenameOperations”值，并删除其中所有数据，之后就可以正常安装QTP或LoadRuner


![image 2](<LoadRunner时提示“安装程序已确定挂起重新启动”解决方案.note_images/imageFile2.png>)

PS：这类问题不仅会出现在QTP与LoadRuner的安装过程中，也有可能出现在类似SQL Server的安装 过程中，问题产⽣原因都是⼀样的。

