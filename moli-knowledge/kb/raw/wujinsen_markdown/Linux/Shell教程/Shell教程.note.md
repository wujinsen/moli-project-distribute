# Shell 教程

Shell 是⼀个⽤C语⾔编写的程序，它是⽤户使⽤Linux的桥梁。Shell既是⼀种命令语⾔，⼜是⼀种程序设计语⾔。 Shell 是指⼀种应⽤程序，这个应⽤程序提供了⼀个界⾯，⽤户通过这个界⾯访问操作系统内核的服务。 Ken Thompson的sh是第⼀种Unix Shell，Windows Explorer是⼀个典型的图形界⾯Shell。

Shell 在线⼯具

Shell 脚本

Shell 脚本（shell script），是⼀种为shell编写的脚本程序。 业界所说的shell通常都是指shell脚本，但读者朋友要知道，shell和shell script是两个不同的概念。 由于习惯的原因，简洁起⻅，本⽂出现的"shell编程"都是指shell脚本编程，不是指开发shell⾃身。

Shell 环境

Shell 编程跟java、php编程⼀样，只要有⼀个能编写代码的⽂本编辑器和⼀个能解释执⾏的脚本解释器就可以了。 Linux的Shell种类众多，常⻅的有：

Bourne Shell（/usr/bin/sh或/bin/sh）

Bourne Again Shell（/bin/bash）

C Shell（/usr/bin/csh） K Shell（/usr/bin/ksh） Shell for Root（/sbin/sh） ……

本教程关注的是 Bash，也就是 Bourne Again Shell，由于易⽤和免费，Bash在⽇常⼯作中被⼴泛使⽤。同时，Bash也是⼤多数Linux系 统默认的Shell。 在⼀般情况下，⼈们并不区分 Bourne Shell 和 Bourne Again Shell，所以，像 #!/bin/sh，它同样也可以改为#!/bin/bash。 #!告诉系统其后路径所指定的程序即是解释此脚本⽂件的Shell程序。

第⼀个shell脚本

打开⽂本编辑器(可以使⽤vi/vim命令来创建⽂件)，新建⼀个⽂件test.sh，扩展名为sh（sh代表shell），扩展名并不影响脚本执⾏，⻅名 知意就好，如果你⽤php写shell 脚本，扩展名就⽤php好了。 输⼊⼀些代码，第⼀⾏⼀般是这样：

实例

#!/bin/bash echo "Hello World !"

运⾏实例 »

"#!" 是⼀个约定的标记，它告诉系统这个脚本需要什么解释器来执⾏，即使⽤哪⼀种Shell。 echo命令⽤于向窗⼝输出⽂本。

运⾏Shell脚本有两种⽅法：

- 1、作为可执⾏程序 将上⾯的代码保存为test.sh，并cd到相应⽬录： chmod +x ./test.sh #使脚本具有执⾏权限

./test.sh #执⾏脚本 注意，⼀定要写成./test.sh，⽽不是test.sh，运⾏其它⼆进制的程序也⼀样，直接写test.sh，linux系统会去PATH⾥寻找有没有叫 test.sh的，⽽只有/bin, /sbin, /usr/bin，/usr/sbin等在PATH⾥，你的当前⽬录通常不在PATH⾥，所以写成test.sh是会找不到命令 的，要⽤./test.sh告诉系统说，就在当前⽬录找。

- 2、作为解释器参数 这种运⾏⽅式是，直接运⾏解释器，其参数就是shell脚本的⽂件名，如：


/bin/sh test.sh /bin/php test.php 这种⽅式运⾏的脚本，不需要在第⼀⾏指定解释器信息，写了也没⽤。 ← linux yum 命令

