Linux下nodejs安装：

推荐下载官⽹已经编译好的，⾃⼰编译安装会有各种各样的问题，⽐如：linux⾃带python版本过低， gc版本过低等等。 下载最新安装包 node-v6.9.2-linux-x64.tar.xz 已经编译好的了 解压xz需要下载xz-5.2.3.tar.bz2 解压tar -xvf xz-5.2.3.tar.bz2 这⾥出现系统时间不⼀致问题 修改时间：date -s "2017-01-14" 把系统时间写⼊CMOS： block -w

./configure-prefix=/opt/gnu/xz make 编译 make instal 安装 xz加⼊环境变量 vi /etc/porfile PATH=$PATH:/opt/gnu/xz/bin xz -d node-v6.9.2-linux-x64.tar.xz 得到 node-v6.9.2-linux-x64.tar tar -xvf node-v6.9.2-linux-x64.tar.xz -C /usr/local/nodejs node加⼊环境变量： export PATH=$PATH:/usr/local/nodejs/node-v6.9.2-linux-x64/bin/

输⼊node -v 显示版本号安装正确。

