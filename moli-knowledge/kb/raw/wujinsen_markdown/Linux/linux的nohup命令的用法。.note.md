在应⽤Unix/Linux时，我们⼀般想让某个程序在后台运⾏，于是我们将常会⽤ & 在程序结尾来让程序⾃动运⾏。⽐如我们要 运⾏mysql在后台： /usr/local/mysql/bin/mysqld_safe –user=mysql &。可是有很多程序并不想mysqld⼀样，这样我 们就需要nohup命令，怎样使⽤nohup命令呢？这⾥讲解nohup命令的⼀些⽤法。

nohup /root/start.sh & 在shell中回⻋后提示： [~]$ appending output to nohup.out 原程序的的标准输出被⾃动改向到当前⽬录下的nohup.out⽂件，起到了log的作⽤。 但是有时候在这⼀步会有问题，当把终端关闭后，进程会⾃动被关闭，察看nohup.out可以看到在关闭终端瞬间服务⾃动关 闭。 咨询红旗Linux⼯程师后，他也不得其解，在我的终端上执⾏后，他启动的进程竟然在关闭终端后依然运⾏。 在第⼆遍给我演示时，我才发现我和他操作终端时的⼀个细节不同：他是在当shell中提示了nohup成功后还需要按终端上键 盘任意键退回到shell输⼊命令窗⼝，然后通过在shell中输⼊exit来退出终端；⽽我是每次在nohup执⾏成功后直接点关闭程 序按钮关闭终端.。所以这时候会断掉该命令所对应的session，导致nohup对应的进程被通知需要⼀起shutdown。 这个细节有⼈和我⼀样没注意到，所以在这⼉记录⼀下了。 附：nohup命令参考 nohup 命令 ⽤途：不挂断地运⾏命令。 语法：nohup Command [ Arg … ] [ & ] 描述：nohup 命令运⾏由 Command 参数和任何相关的 Arg 参数指定的命令，忽略所有挂断（SIGHUP）信号。在注销后 使⽤ nohup 命令运⾏后台中的程序。要运⾏后台中的 nohup 命令，添加 & （ 表示”and”的符号）到命令的尾部。 ⽆论是否将 nohup 命令的输出重定向到终端，输出都将附加到当前⽬录的 nohup.out ⽂件中。如果当前⽬录 的 nohup.out ⽂件不可写，输出重定向到 $HOME/nohup.out ⽂件中。如果没有⽂件能创建或打开以⽤于追加，那么 Command 参数指定的命令不可调⽤。如果标准错误是⼀个终端，那么把指定的命令写给标准错误的所有输出作为标准输出 重定向到相同的⽂件描述符。 退出状态：该命令返回下列出⼝值：

- 126 可以查找但不能调⽤ Command 参数指定的命令。

- 127 nohup 命令发⽣错误或不能查找由 Command 参数指定的命令。 否则，nohup 命令的退出状态是 Command 参数指定命令的退出状态。 nohup命令及其输出⽂件 nohup命令：如果你正在运⾏⼀个进程，⽽且你觉得在退出帐户时该进程还不会结束，那么可以使⽤nohup命令。该命令可 以在你退出帐户/关闭终端之后继续运⾏相应的进程。nohup就是不挂起的意思( n ohang up)。 该命令的⼀般形式为：nohup command & 使⽤nohup命令提交作业 如果使⽤nohup命令提交作业，那么在缺省情况下该作业的所有输出都被重定向到⼀个名为nohup.out的⽂件中，除⾮另外 指定了输出⽂件： nohup command > myout.file 2>&1 & 在上⾯的例⼦中，输出被重定向到myout.file⽂件中。 使⽤ jobs 查看任务。 使⽤ fg %n 关闭。 另外有两个常⽤的ftp⼯具ncftpget和ncftpput，可以实现后台的ftp上传和下载，这样就可以利⽤这些命令在后台上传和下载 ⽂件了。


