⾸先使⽤ --help 参数查看⼀下。basename命令参数很少，很容易掌握。

1.

$ basename --help

⽤法⽰例： $ basename /usr/bin/sort 输出"sort"。 $ basename ./include/stdio.h .h 输出"stdio"。

为basename指定⼀个路径，basename命令会删掉所有的前缀包括最后⼀个slash（‘/’） 字符，然后将字符串显⽰出来。

basename命令格式： basename [pathname] [sufﬁx] basename [string] [sufﬁx]

sufﬁx为后缀，如果sufﬁx被指定了，basename会将pathname或string中的sufﬁx去掉。 ⽰例：

- 1.
- 2.
- 3.
- 4.


$ basename /tmp/test/ﬁle.txt ﬁle.txt $ basename /tmp/test/ﬁle.txt .txt ﬁle

注意点：

- 1、如果像下⾯脚本中传递参数给basename，参数为空，basename会将参数左移

- 2、basename最多接受两个参数，如果设置的参数多于两个，会提⽰错误。


参考：

# http://en.wikipedia.org/wiki/Basename

以下是⼀个简单的脚本，测试了⼀下basename：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.


#!/bin/bash # basename.sh

echo Testing basename echo ------------echo "basename \$1/\$2 .txt; sufﬁx is .txt" ﬁlename=`basename $1/$2 .txt` echo $ﬁlename echo ------------echo "basename ab.c .c; sufﬁx is .c" basename ab.c .c echo "basename ab b; sufﬁx is b" basename ab b echo ------------echo Testing \$\@ and \$\# echo Output \$\@ echo $@ echo Output \$\# echo $#

19.

# end of basename.sh

脚本运⾏结果：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.


没有参数传递的情况：

$./basename.sh Testing basename

------------basename $1/$2 .txt; sufﬁx is .txt /

------------basename ab.c .c; sufﬁx is .c ab basename ab b; sufﬁx is b a Testing $@ and $#

------------Output $@

Output $#

- 0

传递参数的情况：

$ ./basename.sh 1.txt 2.txt Testing basename

-----------basename $1 .txt; sufﬁx is .txt

- 1


------------basename ab.c .c; sufﬁx is .c ab basename ab b; sufﬁx is b a Testing $@ and $#

------------Output $@

- 1.txt 2.txt Output $#

- 2


额外补充：

- 1、$@ $@ 为传递的参数

- 2、$# $# 为传递参数的数量


就像脚本执⾏后的结果：

- 1.
- 2.
- 3.
- 4.
- 5.


Testing $@ and $#

------------Output $@ 1.txt 2.txt Output $#

- 6.


2

- 3、$? 是shell变量,表⽰"最后⼀次执⾏命令"的退出状态，⼀般0表⽰成功，⾮0数值表⽰没有

成功。

切记: $?永远表⽰shell命令最后⼀次执⾏后的退出状态,当函数执⾏完毕后,如果又执⾏了其 它命令,则$?不再表⽰函数执⾏后的状态,⽽表⽰其它命令的退出状态.

- 4、$! 代表pid,进程id

- 5、$$ 代表ppid,⽗进程id


- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


$ ./skype &

[2] 13549 $ echo $! 13549

$ echo $$ 13032 $ ps -ef | grep skype luck 13549 13032 4 19:19 pts/0 00:00:00 skype

