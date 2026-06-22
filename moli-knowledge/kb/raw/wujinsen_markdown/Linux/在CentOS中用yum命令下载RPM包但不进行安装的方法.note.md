yum是基于Red Hat的系统(如CentOS、Fedora、RHEl)上的默认包管理器。使⽤yum，你可以安装或 者更新⼀个RPM包，并且他会⾃动解决包依赖关系。但是如果你只想将⼀个RPM包下载到你的系统上 该怎么办呢? 例如，你可能想要获取⼀些RPM包在以后使⽤，或者将他们安装在另外的机器上。 这⾥说明了如何从yum仓库上下载⼀个RPM包。 ⽅法⼀:yum yum命令本身就可以⽤来下载⼀个RPM包，标准的yum命令提供了 -downloadonly(只下载)的选项来 达到这个⽬的。

复制代码代码如下: $ sudo yum instal -downloadonly <package-name> 默认情况下，⼀个下载的RPM包会保存在下⾯的⽬录中:

复制代码代码如下: /var/cache/yum/x86_64/[centos/fedora-version]/[repository]/packages 以上的[repository]表示下载包的来源仓库的名称(例如：base、fedora、updates) 如果你想要将⼀个包下载到⼀个指定的⽬录(如/tmp)：

复制代码代码如下: $ sudo yum instal -downloadonly-downloadir=/tmp <package-name> 注意，如果下载的包包含了任何没有满⾜的依赖关系，yum将会把所有的依赖关系包下载，但是都不 会被安装。 另外⼀个重要的事情是，在CentOS/RHEL 6或更早期的版本中，你需要安装⼀个单独yum插件(名称为 yum-plugin-downloadonly)才能使⽤ -downloadonly命令选项：

复制代码代码如下: $ sudo yum instal yum-plugin-downloadonly 如果没有该插件，你会在使⽤yum时得到以下错误：

复制代码代码如下: Comand line eror: no such option: -downloadonly

![image 1](<在CentOS中用yum命令下载RPM包但不进行安装的方法.note_images/imageFile1.png>)

⽅法⼆: Yumdownloader 另外⼀个下载RPM包的⽅法就是通过⼀个专⻔的包下载⼯具 -yumdownloader。 这个⼯具是yum⼯具 包(包含了⽤来进⾏yum包管理的帮助⼯具套件)的⼦集。

复制代码代码如下: $ sudo yum instal yum-utils 下载⼀个RPM包：

复制代码代码如下: $ sudo yumdownloader <package-name> 下载的包会被保存在当前⽬录中。你需要使⽤rot权限，因为yumdownloader会在下载过程中更新包 索引⽂件。与yum命令不同的是，任何依赖包不会被下载。

