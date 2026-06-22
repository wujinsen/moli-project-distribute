反引号位 (`) 位于键盘的Tab键的上⽅、1键的左⽅。注意与单引号(')位于Enter键的左⽅的区 别。

在 中起着命令替换的作⽤。命令替换是指shell能够将⼀个命令的标准输出插在⼀

# Linux

个命令⾏中任何位置。 如下，shell会执⾏反引号中的date命令，把结果插⼊到echo命令显⽰的内容中。 [root@localhost sh]# echo The date is `date` The date is 2011年 03⽉ 14⽇ 星期⼀ 21:15:43 CST

单引号、双引号⽤于⽤户把带有空格的字符串赋值给变量事的分界符。 [root@localhost sh]# str="Today is Monday" [root@localhost sh]# echo $str Today is Monday 如果没有单引号或双引号，shell会把空格后的字符串解释为命令。 [root@localhost sh]# str=Today is Monday bash: is: command not found 单引号和双引号的区别。单引号告诉shell忽略所有特殊字符，⽽双引号忽略⼤多数，但

不包括$、\、`。 [root@localhost sh]# testvalue=100 [root@localhost sh]# echo 'The testvalue is $testvalue' The testvalue is $testvalue [root@localhost sh]# echo "The testvalue is $testvalue" The testvalue is 100

