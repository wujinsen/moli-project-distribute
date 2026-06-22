- 1、使⽤命令安装Ftp服务 yum instal vsftpd
- 2、修改配置⽂件 mv /etc/vsftpd/vsftpd.conf /etc/vsftpd/vsftpd.conf.bak vi /etc/vsftpd/vsftpd.conf 创建⽂件之后，在⽂档中添加附件中的内容。

-

针对每个⽤户进⾏配置，可以不配置 mkdir /etc/vsftpd/user_conf/ vi /etc/vsftpd/chrot_list 在⽂档中 插⼊luming vi/etc/vsftpd/user_conf/luming 在⽂档中插⼊ local_rot=/export/data/ftp

- 3、创建⽤户 userad -d /export/data/ftp/luming -s /sbin/nologin luming
- 4、关闭⽬录控制（极度重要） setenforce 0

-

建议永久管理selinux⽬录控制 vi /etc/selinux/config 将 SELINUX=enforcing ->SELINUX=disabled

- 5、重启ftp服务，让配置⽂件⽣效 service vsftpd restart


![image 1](<linux下安装服务FTP(使用本地登陆的方式).note_images/imageFile1.png>)

附件1：

