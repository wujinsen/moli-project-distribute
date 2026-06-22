- 1、报错信息如下：

[plain]

错误原因：错误信息描述为 yum 所依赖的python 不相符，请安装相对应的python即可

- 2、查看yum版本 [root@develop local]# rpm -qa |grep yum yum-3.2.8-9.el5.centos.1 yum-metadata-parser-1.1.2-2.el5

- 3、查看python版本


view plain copy

print?

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
- 18.
- 19.
- 20.
- 21.
- 22.


[root@develop bin]# yum [root@develop local]# yum -y install prce There was a problem importing one of the Python modules required to run yum. The error leading to this problem was:

No module named yum

Please install a package which provides this module, or verify that the module is installed correctly.

It's possible that the above module doesn't match the current version of Python, which is: 2.6.1 (r261:67515, Aug 7 2010, 11:36:17) [GCC 4.1.2 20080704 (Red Hat 4.1.2-44)]

If you cannot solve this problem yourself, please go to the yum faq at: http://wiki.linux.duke.edu/YumFaq

[plain]

view plain copy

print?

- 1.
- 2.


[root@develop local]# whereis python python: /usr/bin/python2.4 /usr/bin/python /usr/lib/python2.4 /usr/local/bin/python2.6 /usr/loca l/bin/python2.6config /usr/local/bin/python /usr/local/lib/python2.6 /usr/share/man/man1/python.1.gz

果然装了两个版本python

- 4、执⾏python，查看到使⽤2.6.1的版本

[plain]

- 5、猜测yum调⽤了⾼版本的python。

- 6、解决⽅法： 查找yum和 yum-updatest⽂件,并编辑此py⽂件


view plain copy

print?

- 1.
- 2.
- 3.
- 4.
- 5.


[root@develop local]# python Python 2.6.1 (r261:67515, Aug 7 2010, 11:36:17) [GCC 4.1.2 20080704 (Red Hat 4.1.2-44)] on linux2 Type "help", "copyright", "credits" or "license" for more information. >>>

[plain]

view plain copy

print?

- 1.
- 2.
- 3.


[root@develop local]# which yum /usr/bin/yum [root@develop local]# vi /usr/bin/yum

[plain]

view plain copy

print?

1.

[root@develop local]# vi /usr/bin/yum-updatest

将 #!/usr/bin/python 改为: #!/usr/bin/python2.4

然后保存OK.

如果不修改/usr/bin/yum ,则yum⽆法使⽤ 如果不修改/usr/bin/yum-updatest 会出现如下错误

File "/usr/sbin/yum-updatesd", line 35, in <module> import dbus ImportError: No module named dbus

当然，修改完后记着

# [plain]

view plain copy

print?

- 1.
- 2.
- 3.


[root@localhost google_appengine]# /sbin/service yum-updatesd restart Stopping yum-updatesd: [FAILED] Starting yum-updatesd: [ OK ]

补充：yum基于python写的。

