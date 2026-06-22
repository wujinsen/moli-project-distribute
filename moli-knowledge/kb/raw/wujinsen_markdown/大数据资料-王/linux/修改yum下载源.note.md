htp:/mirors.163.com/.help/centos.html

使⽤说明

⾸先备份/etc/yum.repos.d/CentOS-Base.repo

mv /etc/yum.repos.d/CentOS-Base.repo /etc/yum.repos.d/CentOSBase.repo.backup

1

下载对应版本repo⽂件, 放⼊/etc/yum.repos.d/(操作前请做好相应备份)

- CentOS5

- CentOS6


运⾏以下命令⽣成缓存

- 1 yum clean all

- 2 yum makecache


