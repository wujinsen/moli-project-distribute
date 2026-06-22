1.使⽤yum安装和卸载软件，有个前提是yum安装的软件包都是rpm格式的。

安装的命令是，yuminstall ~，yum会查询数据库，有⽆这⼀软件包，如果有，则检查其依赖冲突关系，如果没有依 赖冲突，那么最好，下载安装;如果有，则会给出提示，询问是否要同时安装依赖，或删除冲突的包，你可以⾃⼰作出 判断； 删除的命令是，yum remove ~，同安装⼀样，yum也会查询数据库，给出解决依赖关系的提示。

其中~代表软件名

- 1.⽤YUM安装软件包命令：yuminstall ~
- 2.⽤YUM删除软件包命令：yumremove ~


- 2.⽤yum查询想安装的软件 我们常会碰到这样的情况，想安装⼀个软件，只知道它和某⽅⾯有关，但⼜不能确

切知道它的名字。这时yum的查询功能就起作⽤了。我们可以⽤yum search keyword这样的命令来进⾏搜索，⽐如我 们要则安装⼀个InstantMessenger，但⼜不知到底有哪些，这时不妨⽤yum search messenger这样的指令进⾏搜 索，yum会搜索所有可⽤rpm的描述，列出所有描述中和messeger有关的rpm包，于是我们可能得到 gaim，kopete等 等，并从中选择。有时我们还会碰到安装了⼀个包，但⼜不知道其⽤途，我们可以⽤yuminfo packagename这个指令 来获取信息。

1.使⽤YUM查找软件包 命令：yumsearch ~ 2.列出所有可安装的软件包 命令：yumlist 3.列出所有可 更新的软件包 命令：yumlist updates 4.列出所有已安装的软件包 命令：yumlist installed 5.列出所有已 安装但不在Yum Repository內的软件包 命令：yumlist extras 6.列出所指定软件包 命令：yumlist～ 7.使 ⽤YUM获取软件包信息 命令：yuminfo～ 8.列出所有软件包的信息 命令：yuminfo 9.列出所有可更新的 软件包信息 命令：yuminfo updates 10.列出所有已安裝的软件包信息 命令：yuminfo installed 11.列出所 有已安裝但不在Yum Repository內的软件包信息 命令：yuminfo extras 12.列出软件包提供哪些⽂件 命令： yumprovides~

- 3.清除YUM缓存 yum会把下载的软件包和header存储在cache中，⽽不会⾃动删除。如果我们觉得它们占⽤了磁盘空间，可以使⽤

yumclean指令进⾏清除，更精确的⽤法是yumclean headers清除header，yum cleanpackages清除下载的rpm包， yum cleanall 清除所有。

- 1.清除缓存⽬录(/var/cache/yum)下的软件包 命令：yum cleanpackages
- 2.清除缓存⽬录(/var/cache/yum)下的 headers 命令：yum cleanheaders
- 3.清除缓存⽬录(/var/cache/yum)下旧的 headers 命令：yum cleanoldheaders
- 4.清除缓存⽬录(/var/cache/yum)下的软件包及旧的headers 命令：yumclean, yum clean all (= yum clean packages; yum clean oldheaders)


- 4.yum命令⼯具使⽤举例 yum update 升级系统 yum install ～安装指定软件包 yum update～升级指定软件包 yum remove～卸载指定软件 yum grouplist 查看系统中已经安装的和可⽤的软件组，可⽤的可以安装 yum grooupinstall～安装上⼀个命令显示的可⽤的软件组中的⼀个 yum grooupupdate～更新指定软件组的软件包 yum grooupremove～卸载指定软件组中的软件包 yum deplist～查询指定软件包的依赖关系


- yum list yum\*列出所有以yum开头的软件包 yum localinstall～从硬盘安装rpm包并使⽤yum解决依赖
- 5.yum⾼级管理应⽤技巧


- 技巧1:加快你的yum的速度.使⽤yum的扩展插件yum-fastestmirror，个⼈认为这个插件⾮常有效，速度真的是明显

提⾼， #yum -y install yum-fastestmirror 注意，在Centos 4上,名字叫yum-plugin-fastestmirror

- 技巧2:扩展你的rpm包好多包官⽅没有,怎么搞定他.要我⾃⼰编译吗?好了，你安装这个包,这个是redhat5的哦。你可

以⾃⼰到php#B"> 这来找 # Red HatEnterprise Linux 5 / i386: rpm-Uhv

# Red HatEnterprise Linux 5 / x86_64: rpm-Uhv

#ATrpms [atrpms] name=CentOS-$releasever – ATrpms baseurl= $releasever-$basearch/atrpms/stable gpgcheck=1 gpgkey=

- 技巧3:rpm查找.还是有rpm包找不到怎么办,到下⾯这个⽹站。基本上都收集全了，你可以⽤⾼级查找看看.
- 技巧4:通过yum⼯具下载RPM源码包。前提是有安装yum-utils这个软件包.如果有安装的话。 #yum downloader --source ; RPM源码包 #yum downloader --source vsftpd 当然,没有源包的话,还要加⼊⼀个源


htp:/dag.wi ers.com/rpm/FAQ.php#B

htp:/apt.sw.be/redhat/el5/en/i386/rpmforge/RPMS/rpmforge-release-0.3.6-1.el5.rf.i386.rp m

htp:/apt.sw.be/redhat/el5/en/x86_64/rpmforge/RPMS/rpmforge-release-0.3.6-1.el5.rf.x86 _64.rpm

htp:/dl.atrpms.net/el

htp:/ATrpms.net/RPM-GPG-KEY.atrpms

htp:/rp m.pbone.net/

[linux-src] name=Centos$releasever-$basearch-Source baseurl= $releasever/os/SRPMS/ enabled=1 gpgcheck=1 gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-redhat-release

htp:/mirors.163.com/centos/

技巧5:软件组安装有时我们安装完系统，管理有⼀类软件没有安装，⽐如⽤于开发的开发包,我们可以⽤软件包来安

装。 #yum grouplist这样可以列出所有的软件包 ⽐如我们要安装开发有关的包 #yum groupinstall "Development Libraries" #yum groupinstall "Development Tools" ⽐如我们要安装中⽅⽀持 #yum groupinstall "Chinese Support"

#yum deplist package1 #查看程序package1依赖情况 以上所有命令参数的使⽤都可以⽤man来查看： [root@F7常⽤⽂档]$ man yum

yum -y install 包名（⽀持*）：⾃动选择y，全⾃动yum install 包名（⽀持*）：⼿动选择y or nyum remove 包名（不 ⽀持*）rpm -ivh 包名（⽀持*）：安装rpm包rpm -e 包名（不⽀持*）：卸载rpm包 升级内核：#yuminstall kernel-headers kernel-devel

