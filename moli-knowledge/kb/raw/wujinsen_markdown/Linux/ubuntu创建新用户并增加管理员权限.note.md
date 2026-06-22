问题导读：

- 1.adduser与useradd有什么区别？

- 2.那种⽅式会⾃动创建组、⽤户组等信息？

- 3.如何新建⽤户具有管理员权限？


![image 1](<ubuntu创建新用户并增加管理员权限.note_images/imageFile1.png>)

$是普通管员，#是系统管理员，在Ubuntu下，root⽤户默认是没有密码的，因此也就⽆法使⽤（据说 是为了安全）。想⽤root的话，得给root⽤户设置⼀个密码： sudo passwd root 然后登录时⽤户名输⼊root，再输⼊密码就⾏了。 ubuntu建⽤户最好⽤adduser，虽然adduser和useradd是⼀样的在别的linux⽷统下，但是我在ubuntu下 ⽤useradd时，并没有创建同名的⽤户主⽬录。 例⼦：adduser user1 这样他就会⾃动创建⽤户主⽬录，创建⽤户同名的组。 root@ubuntu:~# sudo adduser db [sudo] password for xx: 输⼊xx⽤户的密码，出现如下信息 正在添加⽤户"db"… 正在添加新组"db" (1006)… 正在添加新⽤户"db" (1006) 到组"db"… 创建主⽬录"/home/db"… 正在从"/etc/skel"复制⽂件… 输⼊新的 UNIX ⼝令： 重新输⼊新的 UNIX ⼝令： 两次输⼊db的初始密码，出现的信息如下 passwd: password updated successfully Changing the user information for db Enter the new value, or press ENTER for the default Full Name []: Room Number []: Work Phone []: Home Phone []:

Other []: Full Name []:等信息⼀路回⻋ 这个信息是否正确？ [Y/n] y 到此，⽤户添加成功。如果需要让此⽤户有root权限，执⾏命令： root@ubuntu:~# sudo vim /etc/sudoers 修改⽂件如下： # User privilege specification root ALL=(ALL) ALL db ALL=(ALL) ALL 保存退出，db⽤户就拥有了root权限。

