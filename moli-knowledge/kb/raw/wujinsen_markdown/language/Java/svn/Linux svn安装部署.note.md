- 1.创建svn版本库⽬录 mkdir –p /opt/svn
- 2.创建资源本库 svnadmin create /opt/svn/svnrepos
- 3.进⼊conf⽬录（该svn版本库配置⽂件） authz⽂件是权限控制⽂件 paswd是帐号密码⽂件 svnserve.conf SVN服务配置⽂件
- 4.设置帐号密码，权限，服务配置 vi paswd 在[users]块中添加⽤户和密码，格式：帐号=密码，admin=admin

vi svnserve.conf 打开下⾯的⼏个注释： anon-aces = read #匿名⽤户可读 auth-aces = write #授权⽤户可写 pasword-db = /opt/svn/svnrepos/paswd #使⽤哪个⽂件作为账号⽂件 authz-db = /opt/svn/svnrepos/authz #使⽤哪个⽂件作为权限⽂件 realm = /var/svn/svnrepos # 认证空间名，版本库所在⽬录

vi authz 在末尾添加如下代码： [/] dan=rw w=r 意思是版本库的根⽬录dan对其有读写权限，w只有读权限。

- 5.启动svn版本库 svnserve -d -r /opt/svn/svnrepos/


问题解决: ⾸先不能连接是因为：修改svnserve.conf ⽂件时。解开注释时要注意，同时要删除#后⾯的空格。也 就是说要全部顶置。 然后认证失败是要注意前⾯的[/]. 然后重启下SVN就好了 ，不会重启的 rebot吧 vi authz

在末尾添加如下代码： [/] dan=rw

w=r

