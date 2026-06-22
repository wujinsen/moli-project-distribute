使⽤的软件： VirtualBox 4.1.2 CentOS 5.6 x86_64 刻录的光盘

VirtualBox是开源的虚拟机软件，免费⽽且占⽤资源⼩，功能强⼤。先将VirtualBox安装上，这个和平 常安装软件⼀样，就不详述了。

第⼀部分：安装前的准备⼯作

- 步骤1：点击“新建”按钮，创建⼀个新虚拟机。
- 步骤2：给虚拟机命名，选择操作系统及版本。


![image 1](<VirtualBox安装CentOS.note_images/imageFile1.png>)

![image 2](<VirtualBox安装CentOS.note_images/imageFile2.png>)

- 步骤3：选择内存⼤⼩，我这⾥设置的是512M

- 步骤4：选择创建新虚拟机。


![image 3](<VirtualBox安装CentOS.note_images/imageFile3.png>)

![image 4](<VirtualBox安装CentOS.note_images/imageFile4.png>)

- 步骤5：选择虚拟硬盘的类型。
- 步骤6：选择动态分配磁盘容量。


![image 5](<VirtualBox安装CentOS.note_images/imageFile5.png>)

![image 6](<VirtualBox安装CentOS.note_images/imageFile6.png>)

- 步骤7：选择⽂件存储的位置及容量⼤⼩。
- 步骤8：点击create即可。


![image 7](<VirtualBox安装CentOS.note_images/imageFile7.png>)

![image 8](<VirtualBox安装CentOS.note_images/imageFile8.png>)

- 步骤9：选择安装系统⽂件的位置
- 步骤10：选择安装介质，可以选择从光盘启动，也可以使⽤iso⽂件作为安装⽂件。


![image 9](<VirtualBox安装CentOS.note_images/imageFile9.png>)

![image 10](<VirtualBox安装CentOS.note_images/imageFile10.png>)

步骤 1：设置⽹络，默认是NAT的⽹络连接⽅式，修改成桥接(Bridged Adapter)⽅式。

![image 11](<VirtualBox安装CentOS.note_images/imageFile11.png>)

第⼆部分：开始正式安装系统

- 步骤1：选择刚创建的虚拟机，然后点击“开始”按钮。


![image 12](<VirtualBox安装CentOS.note_images/imageFile12.png>)

# 步骤2：按“Enter ”键，进⼊图形安装界⾯。

![image 13](<VirtualBox安装CentOS.note_images/imageFile13.png>)

# 步骤3：cd或iso⽂件检测，没什么⽤，skip即可。

![image 14](<VirtualBox安装CentOS.note_images/imageFile14.png>)

- 步骤4：进⼊图形安装界⾯，点击下⼀步
- 步骤5：选择安装过程中使⽤的语⾔。


![image 15](<VirtualBox安装CentOS.note_images/imageFile15.png>)

![image 16](<VirtualBox安装CentOS.note_images/imageFile16.png>)

# 步骤6：选择键盘类型。

![image 17](<VirtualBox安装CentOS.note_images/imageFile17.png>)

- 步骤7：初始化所定义的分区。
- 步骤8：选择“建⽴⾃定义的分区结构”，然后点击下⼀步。


![image 18](<VirtualBox安装CentOS.note_images/imageFile18.png>)

![image 19](<VirtualBox安装CentOS.note_images/imageFile19.png>)

# 步骤9：创建/bot分区。⾸先选择“空闲”的磁盘块，然后点击“新建”按钮。在弹出框中键⼊挂载点名 称” /bot ”，类型选择ext3，⼤⼩10M。

![image 20](<VirtualBox安装CentOS.note_images/imageFile20.png>)

# 步骤10：创建物理卷，该⽅法适⽤于多硬盘，可以在多硬盘之间动态分配容量。也可按步骤9的⽅法， 创建其他的分区。我们这⾥还是选择创建LVM。⽅法：选择“空闲”，点击“新建”，在弹出框中的⽂件系 统类型中选择“physical volume（LVM）”，然后选择使⽤全部可⽤空间，点击确定。

![image 21](<VirtualBox安装CentOS.note_images/imageFile21.png>)

# 步骤 1：先选择刚创建的LVM分区块，然后点击“LVM”，进⾏进⼀步分区。

![image 22](<VirtualBox安装CentOS.note_images/imageFile22.png>)

# 步骤12：分别创建根分区( / )，数据分区 ( /data )，swap分区。⾄于分区的空间⼤⼩，⾃⼰拿捏。⼀般 swap分区为内存的1.5到2倍，所以我分配了1 0M的空间。

![image 23](<VirtualBox安装CentOS.note_images/imageFile23.png>)

![image 24](<VirtualBox安装CentOS.note_images/imageFile24.png>)

![image 25](<VirtualBox安装CentOS.note_images/imageFile25.png>)

![image 26](<VirtualBox安装CentOS.note_images/imageFile26.png>)

所有分区做完后的情况，如下图：

![image 27](<VirtualBox安装CentOS.note_images/imageFile27.png>)

- 步骤13：选择引导程序，⼀般默认的情况就可以，点击下⼀步


![image 28](<VirtualBox安装CentOS.note_images/imageFile28.png>)

# 步骤14：设置⽹络及ip地址。⾸先选择需要设置的⽹卡，我这⾥只有⼀块(eth0)，然后点“编辑”，⼿⼯ 设置ip地址及⼦⽹掩码，取消对ipv6的⽀持，现在ipv6还是⽤得很少吧。

![image 29](<VirtualBox安装CentOS.note_images/imageFile29.png>)

# 步骤15：设置主机名rot，⽹关及DNS等。第⼀个DNS是⾹港的，第⼆个是⾕歌的DNS服务器。

![image 30](<VirtualBox安装CentOS.note_images/imageFile30.png>)

- 步骤16：设置时区。当然是选择上海咯。
- 步骤17：设置rot⽤户的密码


![image 31](<VirtualBox安装CentOS.note_images/imageFile31.png>)

![image 32](<VirtualBox安装CentOS.note_images/imageFile32.png>)

# 步骤18：选择默认安装的软件。将默认的“Desktop-Gnome”选项关闭，CentOS还是不适合⽤来做桌 ⾯的，使⽤最简安装即可。不安装图形界⾯。

![image 33](<VirtualBox安装CentOS.note_images/imageFile33.png>)

# 步骤19：点击下⼀步，开始安装系统。根据硬件的不同，⼤概⼗分钟左右就可以安装好系统，点重新 引导，即重启系统，安装完成。

![image 34](<VirtualBox安装CentOS.note_images/imageFile34.png>)

![image 35](<VirtualBox安装CentOS.note_images/imageFile35.png>)

![image 36](<VirtualBox安装CentOS.note_images/imageFile36.png>)

![image 37](<VirtualBox安装CentOS.note_images/imageFile37.png>)

- 步骤20：进⼊系统后，会有⼀个设置界⾯，exit即可。这个设置包括认证、防⽕墙、⽹络、系统服务的 设置。进⼊命令⾏后，可以通过setup命令再次调出这个设置界⾯。
- 步骤21：⽤户名和密码，进⼊系统。安全性考虑，第⼀步是新建⼀个普通⽤户，平常使⽤普通⽤户登 录和使⽤系统。


![image 38](<VirtualBox安装CentOS.note_images/imageFile38.png>)

![image 39](<VirtualBox安装CentOS.note_images/imageFile39.png>)

