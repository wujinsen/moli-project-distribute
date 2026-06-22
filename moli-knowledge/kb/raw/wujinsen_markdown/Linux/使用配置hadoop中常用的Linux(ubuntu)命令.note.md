⽣成key： $sh-keygen -t dsa -P' -f ~/.sh/id_dsa $ cat ~/.sh/id_dsa.pub > ~/.sh/authorized_keys

- -t 密钥类型可以⽤ -t 选项指定。如果没有指定则默认⽣成⽤于 SH-2的RSA密钥。
- -f filename 指定密钥⽂件名。c


htp:/ w.aboutyun.com/thread-6487-1-1.html

来源：

远程登录执⾏shel命令key

sh远程登录，并在远程创建⽂件ssh user@host 'mkdir -p .ssh && cat >> .ssh/authorized_keys' < ~/.ssh/id_rsa.pub

htp:/ w.aboutyun.com/thread-697-1-1.html

来源：

远程复制 scp authorized_keys tan@ubuntu:~/.sh/authorized_keys_from_yang sudo scp -r /usr/hadop aboutyun@slave1:~/

⽂件追加内容 cat authorized_keys_from_yang > authorized_keys

解压包： sudo tar zxvf ./jdk-7-linux-i586.tar.gz -C /usr/lib/jvm

⽂件复制：（jdk复制到opt中） sudo cp -r jdk/ /opt

⽂件移动 sudo mv jdk opt (有的时候没有权限，所以必须加上sudo)

⽂件更改所有者(下⾯为更改hadop⽂件夹的权限)

sudo chown -R aboutyun:aboutyun hadop查看端⼝是否被暂⽤sudo netstat -ap | grep 8080sudo netstat -ant|grep 306Proto Recv-Q Send-Q Local Adres Foreign Adres State

PID/Program nametcp 0 0 0.0.0.0  306 0.0.0.0:* LISTEN

1651/mysqld表示的含义mysql的默认端⼝ 306 打开着0.0.0.0 代表你的本地⽹络地址 后⼀个代表外 部⽹络地址 有连接的话就有真正的IP地址了hadop开启调试开启debug export HADOP_ROT_LOGER=DEBUG,consoleLinux打包命令tar czvf my.tar.gz hadop-2.4.0-src杀掉 ⼀个进程kil 进程号kil -9 进程号rpm -qa|grep softname表示的含义是：就是从安装的软件中查询出 softname这个软件详解如下：grep 内容 对象表示从“对象”中查找“内容”，并打印|管道符号，前⼀个命 令的输出（即结果）作为下个命令的输⼊rpm -qarpm管理命令 查询所有安装的软件所以rpm -qa|grep softname就是从安装的软件中查询出softname这个软件hive安装mysql常⽤命令查看软件是否安装 netstat -tap | grep mysql测试mysql远程连接成功：mysql -h172.16.7.15 -urot -p123mysql -h主机 地址 -u⽤户名 －p⽤户密码查看字符集 show variables like '%char%';修改字符集：vi /etc/my.cnf在 [client]下添加default-character-set=utf8 创建sudo⽆密码登陆给aboutyun⽤户设置⽆密码sudo权 限：chmode u+w /etc/sudoersaboutyun AL=(rot)NOPASWD ALchmod u-w /etc/sudoers测试： sudo ifconfigubuntu查看服务列表代码 sudo service-status-alsudo initctl list查看⽂件⼤⼩：du sh hadop-2.7.0-src打包zipzip -r myfile.zip ./*

