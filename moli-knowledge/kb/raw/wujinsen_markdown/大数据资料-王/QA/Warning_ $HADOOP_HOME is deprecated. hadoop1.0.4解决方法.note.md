启动Hadop时报了⼀个警告信息，我安装的Hadop版本是hadop1.0.4，具体警告信息如下： Shel代码

- 1.
- 2.


[rot@localhost hadop-1.0.4]# ./bin/start-al.sh Warning: $HADOP_HOME is deprecated.

⽹上的说法是因为Hadop本身对HADOP_HOME做了判断，具体在bin/hadop和bin/hadopconfig.sh⾥。在hadop-config.sh⾥有如下的配置： Shel代码

- 1.
- 2.
- 3.
- 4.


if [ "$HADOP_HOME_WARN_SUPRES" = " ] & [ "$HADOP_HOME" != " ]; then echo "Warning: \$HADOP_HOME is deprecated."1>&2 echo 1>&2

fi

对于这个警告问题，解决⽅法如下：

- 1.注释掉hadop-config.sh⾥的上⾯给出的这段if fi配置（不推荐）
- 2.在当前⽤户home/.bash_profile⾥增加⼀个环境变量： export HADOP_HOME_WARN_SUPRES=1 注：修改完.bash_profile后需要执⾏source操作使其⽣效 Shel代码


1.

[rot@localhost ~]# source .bash_profile

执⾏完后我们可以检验⼀下配置是否成功，重新执⾏start-al.sh脚本： Shel代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


[rot@localhost hadop-1.0.4]# ./bin/start-al.sh starting namenode, loging to /rot/hadop-1.0.4/libexec/./logs/hadop-rot-namenodelocalhost.out localhost: starting datanode, loging to /rot/hadop-1.0.4/libexec/./logs/hadop-rotdatanode-localhost.out localhost: starting secondarynamenode, loging to /rot/hadop-1.0.4/libexec/./logs/hadoprot-secondarynamenode-localhost.out starting jobtracker, loging to /rot/hadop-1.0.4/libexec/./logs/hadop-rot-jobtrackerlocalhost.out localhost: starting tasktracker, loging to /rot/hadop-1.0.4/libexec/./logs/hadop-rottasktracker-localhost.out

没有出现Warning: $HADOP_HOME is deprecated，说明问题已经解决。

