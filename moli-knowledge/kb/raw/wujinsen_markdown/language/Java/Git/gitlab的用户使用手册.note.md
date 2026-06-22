最近⽐较忙，没时间写博客，今天来的早所以写⼀篇关于gitlab的使⽤⼿册分享给⼤家。

⽬录

⼀、账户/项⽬申请

⼆、登陆与修改密码

三、SSH Key导⼊

四、上传/下载代码

- 1、上传（⽤户系统为linux）

- 2、下载（⽤户系统为linux）

- 3、上传（⽤户系统为windows）

- 4、下载（⽤户系统为windows）


⼀、账户/项⽬申请

- 1、帐号的申请需要⽤公司的邮箱发邮件申请，发送的要求参考下⾯2-3项；

- 2、新项⽬仓库的新建、新团队成员的授权邮件除了cc给公共组以外，每个新⼈的授权都需要cc给 vcp，否则运维组团队成员不如给该⽤户授权；

- 3、项⽬组的权限分配申请时邮件需要cc到当前项⽬的负责⼈和vcp，然后权限统⼀授权由运维组 成员来完成。


申请账户/项⽬的格式为

姓名：

职务：

项⽬组：

仓库名：

项⽬负责⼈： 账户/项⽬由运维组创建成功后，会进⾏邮件通知，反馈邮件⾥包括完成情况、仓库名等。 ⼆、登陆与修改密码 在gitlab的web界⾯ http://ip ，输⼊账号与密码登陆

![image 1](<gitlab的用户使用手册.note_images/imageFile1.png>)

如果想修改密码的话，选择右侧的My Profile

![image 2](<gitlab的用户使用手册.note_images/imageFile2.png>)

然后选择Account

![image 3](<gitlab的用户使用手册.note_images/imageFile3.png>)

最后在Password⾥修改你的密码

![image 4](<gitlab的用户使用手册.note_images/imageFile4.png>)

如果密码忘记，请发邮件给运维组来重置你的账户密码。

三、SSH Key导⼊

如果你想进⾏代码的上传与下载等操作，需要你把⾃⼰的ssh key导⼊到gitlab⾥，⽅法如下：

- 1、把id_rsa.pub⾥的内容复制（打开⽅法为⿏标右键——打开⽅式——记事本）；

- 2、选择My Porfile


![image 5](<gitlab的用户使用手册.note_images/imageFile5.png>)

选择SSH Keys

![image 6](<gitlab的用户使用手册.note_images/imageFile6.png>)

选择Add new

然后把之前复制的id_rsa.pub⾥的内容复制到key⾥

![image 7](<gitlab的用户使用手册.note_images/imageFile7.png>)

然后选择 Save 。

四、上传/下载代码

- 1、上传（⽤户系统为linux） 如果gitlab库为新库，打开后界⾯应该类似以下界⾯


![image 8](<gitlab的用户使用手册.note_images/imageFile8.png>)

如果是在 linux 系统下进⾏上传代码，先进⾏设置 git global 设置

git config --global user.name "Administrator" git config --global user.email "mail address"

其中user.name与user.email都需要输⼊你⾃⼰的信息

之后进⼊到⼀个已经存在的库⾥⾯（⽐如你已经已经存在的库名为test）

cd test

然后在使⽤下⾯操作

git remote add origin gitlab@ip:root/test.git git push -u origin master

# 上 ⾯ 的 操 作 ⾥origin后 的gitlab@ip:root/test.git

请记住， 为你⾃⼰收到界⾯的信息，不⽤跟 我上⾯的⼀样。

如果在进⾏git remote add origin 出现下⾯错误

# gitlab@ip:root/test.git

fatal: remote origin already exists.

那么请输⼊以下命令

git remote rm origin

然后在输⼊之前的命令

git remote add origin gitlab@ip:root/test.git

如果不报错在输⼊

git push -u origin master

但请注意，这样的操作是创建⼀个分⽀为master的，并且只有⼀个分⽀。

请特别注意，如果你本地有很多分⽀，并且都想上传到服务端的话，

git remote add origin gitlab@ip:root/test.git

在这步之后不进⾏下⼀步操作，输⼊以下命令

git push --all

这样就会把所有分⽀都上传到服务端。

如果你还想把你所有的tag都上传到服务端，在输⼊完git push --all后，在输⼊下⾯命令即可完成 上传所有的tags到服务端。

git push --tags

完成后，在打开web界⾯会出现

![image 9](<gitlab的用户使用手册.note_images/imageFile9.png>)

不是之前的提示操作界⾯

然后你可以选择Files来查看当前⽂件信息

![image 10](<gitlab的用户使用手册.note_images/imageFile10.png>)

还可以选择Commits来查看提交信息

![image 11](<gitlab的用户使用手册.note_images/imageFile11.png>)

- 2、下载（⽤户系统为linux）

当你的⽤户系统为linux，请先找个存放⽬录（⽐如我在tmp⽬录），然后使⽤git clone gitlab库地 址来进⾏下载库操作

⽐如刚才的test项⽬，

下⾯进⾏git clone

gitlab⾥ 的 地址 为gitlab@ip:root/test.git

![image 12](<gitlab的用户使用手册.note_images/imageFile12.png>)

这样就代表你git clone成功。

请注意，如果git库⾥有多个分⽀的话，下载的时候需要加上--bare，完整格式为

git clone –bare git库地址

下载完成后可以进⼊⽬录，使⽤git branch查看有多少分⽀，使⽤git tag查看有多少tags。

- 3、上传（⽤户系统为windows）


如果你的⽤户系统为windows，请进⼊已经存在库的⽬录⾥，然后使⽤git的window⼯具，点击⿏ 标右键，选择Git commit-àMaster（当前测试的分⽀为Master，如果你有其他分⽀，请选择好）, 然后在出现的对话框⾥输⼊相应信息

![image 13](<gitlab的用户使用手册.note_images/imageFile13.png>)

输⼊相应的信息，然后选择OK

![image 14](<gitlab的用户使用手册.note_images/imageFile14.png>)

出现下⾯情况代表完成操作

![image 15](<gitlab的用户使用手册.note_images/imageFile15.png>)

然后登陆到gitlab⾥的Commits，可以看到提交的信息

![image 16](<gitlab的用户使用手册.note_images/imageFile16.png>)

⽂件的话需要到Files⾥查看

- 4、下载（⽤户系统为windows）


在win下找到⼀个位置，然后使⽤git clone来把上⾯gitlab⾥项⽬的的地址给下载

⽐如刚才的test项⽬，

# gitlab⾥ 的 地址 为gitlab@ip:root/test.git

进⾏git clone（⽐如我在E盘下）

![image 17](<gitlab的用户使用手册.note_images/imageFile17.png>)

然后选择ok

如果出现

![image 18](<gitlab的用户使用手册.note_images/imageFile18.png>)

也选择是（Y）

然后会出现

![image 19](<gitlab的用户使用手册.note_images/imageFile19.png>)

证明在git clone你的test项⽬

如果git clone完成会出现

![image 20](<gitlab的用户使用手册.note_images/imageFile20.png>)

选择Close即可

