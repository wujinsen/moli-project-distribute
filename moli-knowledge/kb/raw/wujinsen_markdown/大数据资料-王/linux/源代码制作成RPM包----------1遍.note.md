由于⾃动化运维的需求，需要把所有的源代码打成RPM包，⽅便以后运维安装管理，⼀般来说源代码 制作成RPM⼀般需要⼀下 的步骤即可。

- 1，确定Linux 环境中安装了gcc rpmbuild make install等

- 2，从源代码中找到spec⽂件，⽤来控制包建⽴的过程

- 3，打包

- 4，查看制作完成的RPM包


以下是⼀个实例： [html]

view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.


- 1，rpm -ivh rpm-build-4.4.2.3-2.el5.x86_64.rpm
- 2，tar -zxvf nagios-plugins-1.4.15.tar.gz cd nagios-plugins-1.4.15 cp nagios-plugins.spec /usr/src/redhat/SOURCES/ cp nagios-plugins-1.4.15.tar.gz /usr/src/redhat/SOURCES/
- 3，rpmbuild -b nagios-plugins.spec
- 4，cd /usr/src/redhat/RPMS/


[rot@pupet_master RPMS]# tre

. |- noarch `- x86_64

|- nagios-plugins-1.4.15-1.x86_64.rpm `- nagios-plugins-debuginfo-1.4.15-1.x86_64.rpm

2 directories, 2 files

其中/usr/src/redhat 是默认⽬录可以设置。

不管你想打什么RPM包，这个应该能帮上你。 RPMBuilder, ⼀个Linux下的C+开源⼯具，⾃⼰make和make instal. 然后⽤命令rpmbuilder: > rpmbuilder # 此次运⾏，⽣成⼀个RPMBuilder.xml > vi RPMBuilder.xml # 配置 > rpmbuilder# 打包 完成！ Link:

htps:/sourceforge.net/projects/xml2rpm/files/rpmbuilder-1.0.1/

提问者评价 谢谢！期间公司在做Migration的工作，经常遇到这样的场景：需要对现有的二进制第三方库和头文件打 包成RPM包，以便临时测试用或者第三方库管理。但是，修改SPEC文件并编写Makeﬁle是个郁闷的事 情，总是重复去做，对RPM新手来说是个灾难。于是乎，小强就创建了一个开源项目RPMBuilder来完成 这件事相对郁闷的事情，理由很简单，每个童鞋只要下载、编译、安装RPMBuilder以后，就只剩下两步 了，第一步：运行rpmbuilder命令直接生成一个RPMBuilder.xml的配置文件第二部：按照需要配置要打包 的文件、包的基本信息就哦了。然后再次运行命令rpmbuilder，就看刷刷的直到打包完成。它的优势就 是：一个配置文件、一个简单命令，就可以打出RPM包（可以打一个包，还有N个子包，例如：mytest 包和mytest-devel包）了。目前的不足是：XML对SPEC文件的配置不是全集支持，只包含90%场景下能 用到的。但是经过测试还是靠得住的，比较专业的打包和更多的信息可能就需要用户抛弃RPMBuilder， 自己些SPEC专业化的配置了。不多说了，现在分享这个开源工具，有啥意见和想法，欢迎诸位IT同仁 们的留言支持！！如对您有用，请留下宝贵足迹，谢谢~~开源项⽬：

htp:/sourceforge.net/projects/ xml2rpm htp:/sourceforge.net/projects/xml2rpm/files/rpmbuilder-1.1.0/RPMBuilder-1.1. 2-20120615T0621.tar.gz/download htp:/sourceforge.net/projects/xml2rpm/files/

最新版本：

下载页⾯：

