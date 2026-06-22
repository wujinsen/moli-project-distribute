如果⾮要在window上做客户端应⽤开发，需要设置以下环境：

在windows的某个⽬录下解压⼀个hadop的安装包 将安装包下的lib和bin⽬录⽤对应windows版本平台编译的本地库替换 在window系统中配置HADOP_HOME指向你解压的安装包 在windows系统的path变量中加⼊hadop的bin⽬录

- 1、准备针对windows平台的hadop编译安装包
- 2、准备hadop安装包 请⾃⾏下载Hadop安装包，如果使⽤本⽂提供的编译包，请下载Hadop2.6.x
- 3、配置环境变量 下载后，解压⽂件并重命名为： E:\ x\instal\hadop


![image 1](<第三节：windows开发环境搭建.note_images/imageFile1.png>)

配置环境变量：

![image 2](<第三节：windows开发环境搭建.note_images/imageFile2.png>)

将环境变量配置到path⾥⾯：

![image 3](<第三节：windows开发环境搭建.note_images/imageFile3.png>)

- 4、将本⽂提供的编译⽂件解压出来，⾥⾯的内容如下：


![image 4](<第三节：windows开发环境搭建.note_images/imageFile4.png>)

为了⽅便起⻅，请全选复制以上⽂件到hadop的bin⽬录下和lib\native⽬录下。

![image 5](<第三节：windows开发环境搭建.note_images/imageFile5.png>)

