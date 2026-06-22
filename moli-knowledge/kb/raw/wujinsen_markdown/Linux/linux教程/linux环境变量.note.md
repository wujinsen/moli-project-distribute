# /etc/profile 和~/.bash_profile区别

/etc/profile 和~/.bash_profile区别

/etc/profile

为系统的每个⽤户设置环境信息和启动程序，当⽤户第⼀次登录时，该⽂件被执⾏，其配置对所有登 录的⽤户都有效。当被修改时，重启或使⽤命令 source /etc/profile 才会⽣效。英⽂描述：”System wide environment and startup programs, for login setup.”

~/.bash_profile

为当前⽤户设置专属的环境信息和启动程序，当⽤户登录时该⽂件执⾏⼀次。默认情况下，它⽤于设 置环境变量，并执⾏当前⽤户的 .bashrc ⽂件。理念类似于 /etc/profile，只不过只对当前⽤户有效， 需要重启或使⽤命令 source ~/.bash_profile 才能⽣效。(注意：Centos7系统命名为.bash_profile，其 他系统可能是.bash_login或.profile。)

Note: /etc/profile ~/ .bash_profile ~/.bashrc 是按照这个顺序加载环境的 后⾯的会覆盖前⾯的

⸻版权声明：本⽂为CSDN博主「Shylin」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原⽂ 出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/Shylin/article/details/905 91

