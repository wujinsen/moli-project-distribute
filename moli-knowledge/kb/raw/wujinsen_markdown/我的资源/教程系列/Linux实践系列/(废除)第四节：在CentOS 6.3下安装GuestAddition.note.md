在CentOS 6.3下安装GuestAdition 在安装GuestAdition之间，需要让CentOS为编译⽣成外置核⼼模块做好准备。 (原⽂：Before instaling the Guest Aditions, you wil have to prepare your guest system for building external kernel modules.) 不同的Linux distribution在这步的操作⾮常不同，这⾥只说CentOS 6.3的操作。 下列操作对CentOS, Red Hat Enterprise Linux and Oracle Enterprise Linux都适⽤： 建议以rot身份运⾏下列命令。

- 1.如果你的CentOS 版本早于 6，那么需要在 /etc/grub.conf 中添加⼀⾏ divider=10，以将这个参数传 递给核⼼，以减少 idle CPU load。
- 2.#yum update 把系统的所有软件升级到最新版本。
- 3.#yum instal gc 安装编译系统
- 4.#yum instal kernel-devel 安装外置核⼼模块。
- 5.#rebot 重启以使所有的升级或安装⽣效。

⾄此，所有的准备⼯作就完成了，下⾯开始安装GuestAdition。

- 6.在虚拟机窗⼝的菜单条下选择 "Device"-“Mount CD/DVD-ROM” ，选择 "CD/DVD-ROM image" ， 会弹出 Virtual Media Manager 窗⼝，然后在资源 中找到 VBoxGuestAditions.iso ⽂件(这⾥假 设 Host OS 是 Windows)，VBoxGuestAditions.iso 通常位于 VirtualBox 的安装⽬录下。 注意：VirtualBox 窗⼝的菜单很多时候是隐藏的，显示的快捷键是HostKey+C。我找这个急出⼀ 身汗。 ⾄此，就相当于在 CentOS 虚拟机中插⼊了 GuestAdition 的光盘。接下来需要挂载该光盘。

- 7.#mount -t auto /dev/cdrom1 /mnt 挂载光盘。 注意：VirtualBox默认有2个光驱，分别是 /dev/cdrom 和 /dev/cdrom1 。 如果你挂载命令执⾏后卡了很久，最后出现：you must specify the filesystem type，说明你需要换个 设备名。 我就是在这⼀步卡了很久，尿都憋出来了，后来发现 VirtualBox 有2个光驱， nd。
- 8.#cd /mnt 挂载成功后进⼊挂载⽬录。
- 9.#sh ./VBox Aditions.run 安装 GuestAdition。安装过程⽐较久，慢慢等。 安装成功的画⾯如下：


浏览器

虚拟机

Linux

![image 1](<(废除)第四节：在CentOS 6.3下安装GuestAddition.note_images/imageFile1.png>)

好了，整个安装完成。最后需要重启以使 GuestAdition ⽣效。

#rebot

unable to find the sources of your curent linux kernel

-

⼀；先安装下⾯的程序； gc kernel kernel-devel 程序说明 gc.i686 : 各类编译器（C、C+、Objective-C、Java, .） kernel.i686 : Linux 内核（Linux 操作系统的核⼼） kernel-devel.i686 : ⽤来构建与内核匹配的内核模块的开发软件包。 程序安装 [rot@localhost VBOXADITIONS_4.1.12_7245]#yum instal gc.i686 kernel.i686 kerneldevel.i686 （⽤yum安装时最好先⽤yum search 搜索下相应的程序,不同的版本可能会有不⼀样的后缀如有的可能 是.i386） ⼆；重新启动电脑，启动时系统会⾃动从新的内核⽂件选项启动不要改回到原来的选项默认就好了 （回到原来的选项还会出现上⾯的错误）。从新安装增强程序问题可以解决。

