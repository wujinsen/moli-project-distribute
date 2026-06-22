当我们下载IDEA后 安装并启动后，我们需要安装⼀个scala Plugin,操作步骤如下：

![image 1](<IDEA安装scala插件.note_images/imageFile1.png>)

![image 2](<IDEA安装scala插件.note_images/imageFile2.png>)

输⼊plugins会出现下⾯界⾯：

![image 3](<IDEA安装scala插件.note_images/imageFile3.png>)

点击 Instal JetBrains plugin.按钮

![image 4](<IDEA安装scala插件.note_images/imageFile4.png>)

输⼊ scala 回出现以下界⾯：

![image 5](<IDEA安装scala插件.note_images/imageFile5.png>)

# 点击 Instal Plugin 按钮 就会⾃动下载scala插件了，然后⾃动安装后重启就会⽣效了 创建scala⼯程：File->New Project->Scala->SBT->Next->输⼊项⽬名称->Finish

![image 6](<IDEA安装scala插件.note_images/imageFile6.png>)

![image 7](<IDEA安装scala插件.note_images/imageFile7.png>)

![image 8](<IDEA安装scala插件.note_images/imageFile8.png>)

因为我们使⽤的SBT⽅式，所以需要IDEA⾃动构建

![image 9](<IDEA安装scala插件.note_images/imageFile9.png>)

SBT⾃动构建完成后：

![image 10](<IDEA安装scala插件.note_images/imageFile10.png>)

创建scala类：右击src下的main下的scala弹出“New”选择 “Scala Clas”

![image 11](<IDEA安装scala插件.note_images/imageFile11.png>)

Name:输⼊MyFirstScala,Kind:选择 Object ,点击确定

![image 12](<IDEA安装scala插件.note_images/imageFile12.png>)

通过使⽤Ctrl+J快捷键，可以⽣成main⽅法和println⽅法

![image 13](<IDEA安装scala插件.note_images/imageFile13.png>)

按快捷键Ctrl+Shift+F10或者在本类右击点击 Run"MyFirstScala"运⾏

![image 14](<IDEA安装scala插件.note_images/imageFile14.png>)

# 如果提示要配置JDK,只要按照流程正常配置下就可以了，这⾥我就不在详细叙述了

