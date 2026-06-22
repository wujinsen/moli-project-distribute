安装步骤如下：

- 1、yum install subversion

- 2、输⼊rpm -ql subversion查看安装位置，如下图：


![image 1](<linux（centos）搭建SVN服务器.note_images/imageFile1.png>)

我们知道svn在bin⽬录下⽣成了⼏个⼆进制⽂件。 输⼊ svn --help可以查看svn的使⽤⽅法，如下图。

![image 2](<linux（centos）搭建SVN服务器.note_images/imageFile2.png>)

- 3、创建svn版本库⽬录 mkdir -p /var/svn/svnrepos

- 4、创建版本库 svnadmin create /var/svn/svnrepos 执⾏了这个命令之后会在/var/svn/svnrepos⽬录下⽣成如下这些⽂件

- 5、进⼊conf⽬录（该svn版本库配置⽂件） authz⽂件是权限控制⽂件 passwd是帐号密码⽂件 svnserve.conf SVN服务配置⽂件

- 6、设置帐号密码 vi passwd 在[users]块中添加⽤户和密码，格式：帐号=密码，如dan=dan

- 7、设置权限 vi authz 在末尾添加如下代码： [/] dan=rw w=r 意思是版本库的根⽬录dan对其有读写权限，w只有读权限。

- 8、修改svnserve.conf⽂件 vi svnserve.conf 打开下⾯的⼏个注释： anon-access = read #匿名⽤户可读 auth-access = write #授权⽤户可写


![image 3](<linux（centos）搭建SVN服务器.note_images/imageFile3.png>)

- password-db = passwd #使⽤哪个⽂件作为账号⽂件 authz-db = authz #使⽤哪个⽂件作为权限⽂件 realm = /var/svn/svnrepos # 认证空间名，版本库所在⽬录
- 9、启动svn版本库 svnserve -d -r /var/svn/svnrepos

- 10、在windows上测试 新建⼀个测试⽂件夹，在该⽂件夹下右键选择 SVN checkout如下图(要事先安装TortoiseSVN)：


![image 4](<linux（centos）搭建SVN服务器.note_images/imageFile4.png>)

填写SVN的地址，如下图：

![image 5](<linux（centos）搭建SVN服务器.note_images/imageFile5.png>)

输⼊密码，如下图：

![image 6](<linux（centos）搭建SVN服务器.note_images/imageFile6.png>)

svn:/host:port/var/svn/svnrepos 这样似乎⽆法访问 svn:/host:port/svnrepos 这样应该可以，版本库⽬录下有conf等⽂件

