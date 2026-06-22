linxu服务器安装软件时提示说要Autoconf 2.64或更⾼的版本 升级autofonf的步骤：

查询autoconf当前版本的命令

[root@BobServerStation twemproxy]# rpm -qf /usr/bin/autoconf autoconf-2.63-5.1.el6.noarch

- 1.
- 2.


卸载当前autoconf 2.63版本

[root@BobServerStation twemproxy]# rpm -e --nodeps autoconf-2.63

1.

安装最新autoconf 2.68版本

[root@BobServerStation twemproxy]#wget ftp://ftp.gnu.org/gnu/autoconf/autoconf-2.68.tar.gz [root@BobServerStation twemproxy]# tar zxvf autoconf-2.68.tar.gz [root@BobServerStation twemproxy]# cd autoconf-2.68 [root@BobServerStation twemproxy]# ./configure --prefix=/usr/ [root@BobServerStation twemproxy]# make&& make install

- 1.
- 2.
- 3.
- 4.
- 5.


查看当前autoconf版本

[root@BobServerStation autoconf-2.68]# /usr/bin/autoconf -V autoconf (GNU Autoconf) 2.68 Copyright (C) 2010 Free Software Foundation, Inc. License GPLv3+/Autoconf: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>, <http://gnu.org/licenses/exceptions.html> This is free software: you are free to change and redistribute it. There is NO WARRANTY, to the extent permitted by law. Written byDavid J. MacKenzie and Akim Demaille.

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


⾄此，autoconf 已升级到2.68最新版本了。

