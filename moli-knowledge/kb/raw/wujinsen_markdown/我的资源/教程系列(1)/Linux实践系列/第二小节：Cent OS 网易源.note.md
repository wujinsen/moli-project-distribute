![image 1](<第二小节：Cent OS 网易源.note_images/imageFile1.png>)

官⽅链接【CentOS镜像使⽤帮助】：

htp:/mirors.163.com/.help/centos.html

⾸先备份/etc/yum.repos.d/CentOS-Base.repo

1 mv /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOS-Base.repo.backup

下载对应版本repo⽂件, 放⼊/etc/yum.repos.d/(操作前请做好相应备份)

CentOS7 wget htp:/mirors.163.com/.help/CentOS7-Base-163.repo CentOS6 wget htp:/mirors.163.com/.help/CentOS6-Base-163.repo CentOS5 wget htp:/mirors.163.com/.help/CentOS5-Base-163.repo

运⾏以下命令⽣成缓存

- 1 yum clean all

- 2 yum makecache


