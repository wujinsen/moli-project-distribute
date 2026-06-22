shift 作用：使位置参数向左移动，默认移动1位，可以使用shift 2 exit 退出整个程序 break 结束当前循环，或跳出本层循环 continue 忽略本次循环剩余的代码，直接进行下一次循环

⽂件的第⼀⾏： #!/bin/sh 注释

以#开头的句⼦表⽰注释，直到这⼀⾏的结束。 局部变量

#!/bin/sh

#对变量赋值，不需要声明： a="heloworld" #现在打印变量a的内容： echo"A is:" echo$a

结果："A is:

helo world 在字符串中打印变量： num=2 echo "this is the ${num}nd"

全局变量。 由export关键字处理过的变量叫做环境变量。

简单介绍⼀下export的作⽤:

- 1 当Bash shell执⾏⼀个程序时,将⾸先为该程序建⽴⼀个新的执⾏环境,称为⼦shell。

- 2 在Bash Shell中变量都是局部的,它们只在创建它们的⼦Shell中有意义。

- 3 使⽤export后,变量被设置为全局变量,这时可以被其它⼦Shell所识别 。


1)Unix命令:

echo"some text": 将⽂字内容打印在屏幕上

ls:⽂件列表

wc-l filewc -w filewc -c file&: 计算⽂件⾏数计算⽂件中的单词数计算⽂件中的字符数

cpsourcefile destfile&: ⽂件拷贝

mvoldname newname : 重命名⽂件或移动⽂件

rmfile&: 删除⽂件

grep'patern' file&: 在⽂件内搜索字符串⽐如：grep'searchstring' file.txt

cut-b colnum file&: 指定欲显⽰的⽂件内容范围，并将它们输出到标准输出设备

⽐如：输出每⾏第5个到第9个字符cut-b5-9 file.txt千万不要和cat命令混淆，这是 两个完全不同的命令

catfile.txt: 输出⽂件内容到标准输出设备（屏幕）上-------查看⽂件

filesomefile&: 得到⽂件类型

readvar: 提⽰⽤户输⼊，并将输⼊赋值给变量

sortfile.txt: 对file.txt⽂件中的⾏进⾏排序

uniq:删除⽂本⽂件中出现的⾏列，⽐如：sort file.txt | uniq

expr:进⾏数学运算，Example:ad 2 and 3expr 2 "+" 3

find:搜索⽂件，⽐如：根据⽂件名搜索find. -name filename -print

te:将数据输出到标准输出设备(屏幕) 和⽂件，⽐如：somecomand| te outfile

basenamefile&: 返回不包含路径的⽂件名⽐如：basename /bin/tux将返回tux

dirnamefile&: 返回⽂件所在路径⽐如：dirname/bin/tux将返回/bin

headfile&: 打印⽂本⽂件开头⼏⾏

tailfile : 打印⽂本⽂件末尾⼏⾏

sed:Sed是⼀个基本的查找替换程序。格式：sed 's/要替换的字符串/新的字符串/g' ⽐如：将linuxfocus替换为LinuxFocus ： cat text.file | sed 's/linuxfocus/LinuxFocus/g' > newtext.file

awk:awk ⽤来从⽂本⽂件中提取字段。缺省地，字段分割符是空格，可以使⽤-F指

定其他分割符。catfile.txt | awk -F, '{print $1 "," $3 }'这⾥我们使 ⽤，

作为字 段分割符，同时打印第⼀个和第三个字段。如果该⽂件内 容如下：

Adam Bor, 34, IndiaKery Miler, 2, USA命令输出结果为：Adam Bor,

IndiaKery Miler,USA

- 2)概念:管道,重定向和backtick 管道(|) ,将⼀个命令的输出作为另外⼀个命令的输⼊。


grep"helo" file.txt | wc -l 在file.txt中搜索包含有"helo"的⾏并计算其⾏数。 在这⾥grep命令的输出作为wc命令的输⼊。当然您可以使⽤多个命令。

重定向：将命令的结果输出到⽂件，⽽不是标准输出（屏幕）。

> : 写⼊⽂件并覆盖旧⽂件 >: 加到⽂件的尾部，保留旧⽂件内容。

反短斜线:使⽤反短斜线可以将⼀个命令的输出作为另外⼀个命令的⼀个命令⾏参数。

命令：find. -mtime -1 -type f -print

⽤来查找过去24⼩时（-mtime-2则表⽰过去48⼩时）内修改过的⽂件。如果您想将

所有查找到的⽂件打⼀个包，则可以使⽤以下脚本：

#!/bin/sh #The ticks are backticks (`) not normal quotes ('): tar-zcvf lastmod.tar.gz `find . -mtime -1 -type f -print`

- 3)流程控制


if语句内判断参数

–b 当file存在并且是块⽂件时返回真

- -c 当file存在并且是字符⽂件时返回真
- -d 当pathname存在并且是⼀个⽬录时返回真
- -e 当pathname指定的⽂件或⽬录存在时返回真
- -f 当file存在并且是正规⽂件时返回真
- -g 当由pathname指定的⽂件或⽬录存在并且设置了SGID位时返回为真
- -h 当file存在并且是符号链接⽂件时返回真，该选项在⼀些⽼系统上⽆效
- -k 当由pathname指定的⽂件或⽬录存在并且设置了“粘滞”位时返回真
- -p 当file存在并且是命令管道时返回为真
- -r 当由pathname指定的⽂件或⽬录存在并且可读时返回为真
- -s 当file存在⽂件⼤⼩⼤于0时返回真
- -u 当由pathname指定的⽂件或⽬录存在并且设置了SUID位时返回真
- -w 当由pathname指定的⽂件或⽬录存在并且可执⾏时返回真。⼀个⽬录为了它的内容被访问必


然是可执⾏的。

-o 当由pathname指定的⽂件或⽬录存在并且被⼦当前进程的有效⽤户ID所指定的⽤户拥有时返回 真。

UNIX Shel ⾥⾯⽐较字符写法：

- -eq 等于
- -ne 不等于
- -gt ⼤于
- -lt ⼩于
- -le ⼩于等于
- -ge ⼤于等于
- -z 空串

= 两个字符相等 != 两个字符不等

- -n ⾮空串


更为详细的说明：

运算符 描述 示例 ⽂件⽐较运算符

- -e filename 如果 filename 存在，则为真 [ -e /var/log/syslog ]
- -d filename 如果 filename 为⽬录，则为真 [ -d /tmp/mydir ]
- -f filename 如果 filename 为常规⽂件，则为真 [ -f /usr/bin/grep ]
- -L filename 如果 filename 为符号链接，则为真 [ -L /usr/bin/grep ]
- -r filename 如果 filename 可读，则为真 [ -r /var/log/syslog ]
- -w filename 如果 filename 可写，则为真 [ -w /var/mytmp.txt ]
- -x filename 如果 filename 可执⾏，则为真 [ -L /usr/bin/grep ] filename1 -nt filename2 如果 filename1 ⽐ filename2 新，则为真 [ /tmp/instal/etc/services -nt


/etc/services ]

filename1 -ot filename2 如果 filename1 ⽐ filename2 旧，则为真 [ /bot/bzImage -ot arch/i386/bot/bzImage ]

字符串⽐较运算符 （请注意引号的使⽤，这是防⽌空格扰乱代码的好⽅法）

-z string 如果 string ⻓度为零，则为真 [ -z $myvar ]

-n string 如果 string ⻓度⾮零，则为真 [ -n $myvar ] string1 = string2 如果 string1 与 string2 相同，则为真 [ $myvar = one two thre ] string1 != string2 如果 string1 与 string2 不同，则为真 [ $myvar != one two thre ] 算术⽐较运算符 num1 -eq num2 等于 [ 3 -eq $mynum ] num1 -ne num2 不等于 [ 3 -ne $mynum ] num1 -lt num2 ⼩于 [ 3 -lt $mynum ] num1 -le num2 ⼩于或等于 [ 3 -le $mynum ] num1 -gt num2 ⼤于 [ 3 -gt $mynum ] num1 -ge num2 ⼤于或等于 [ 3 -ge $mynum ] 脚本示例： #!/bin/bash # This script prints a mesage about your weight if you give it your # weight in kilos and hight in centimeters. if [ ! $# = 2 ]; then echo "Usage: $0 weight_in_kilos length_in_centimeters" exit fi weight="$1" height="$2" idealweight=$[$height -10] if [ $weight -le $idealweight ] ; then

echo "You should eat a bit more fat." else echo "You should eat a bit more fruit." fi # weight.sh 70 150 You should eat a bit more fruit. # weight.sh 70 150 3 Usage: ./weight.sh weight_in_kilos length_in_centimeters 位置参数 $1， $2,., $N，$#代表了命令⾏的参数数量， $0代表了脚本的名字， 第⼀个参数代表$1，第⼆个参数代表$2，以此类推，参数数量的总数存在$#中，上⾯的例⼦显示

了怎么改变脚本，如果参数少于或者多余2个来打印出⼀条消息。 执⾏，并查看情况。 # bash -x tijian.sh 60 170

+ weight=60 + height=170 + idealweight=60

+ '[' 60 -le 60 ']'

+ echo 'You should eat a bit more fat.' You should eat a bit more fat. 其中-x⽤来检查脚本的执⾏情况。 "if"表达式 如果条件为真则执⾏then后⾯的部分： if [. ]; then

. elif[. ]; then

. else .

fi 通常⽤"[ ] "来表⽰条件测试。注意这⾥的空格很重要。要确保⽅括号的空格。 [-f "somefile" ] ：判断是否是⼀个⽂件 [-x "/bin/ls" ] ：判断/bin/ls是否存在并有可执⾏权限 [-n "$var" ] ：判断$var变量是否有值 ["$a" = "$b" ] ：判断$a和$b是否相等

执⾏mantest可以查看所有测试表达式可以⽐较和判断的类型。 直接执⾏以下脚本： #!/bin/sh

if[ "$SHEL" = "/bin/bash" ]; then

echo "your login shel is the bash (bourne again shel)" else

echo "your login shel is not bash but $SHEL"

fi 变量$SHEL包含了登录shel的名称，我们和/bin/bash进⾏了⽐较。

快捷操作符

熟悉C语⾔的朋友可能会很喜欢下⾯的表达式： [-f "/etc/shadow" ] & echo "This computer uses shadowpaswors"

这⾥ & 就是⼀个快捷操作符，如果左边的表达式为真则执⾏右边的语句。 您也可以认为是逻辑运算中的与操作。上例中表⽰如果/etc/shadow⽂件存在 则打印"This computer uses shadow paswors"。 同样或操作(|)在shel编程中也是可⽤的。 这⾥有个例⼦：

#!/bin/sh

mailfolder=/var/spol/mail/james [ -r "$mailfolder" ]' '{ echo "Can not read$mailfolder" exit 1; } echo "$mailfolder has mail from:" grep "^From " $mailfolder

该脚本⾸先判断mailfolder是否可读。如果可读则打印该⽂件中的"From"⼀⾏。如 果不可读则或操作⽣效，打印错误信息后脚本退出。这⾥有个问题，那就是我们必须有

两个命令：

- -打印错误信息
- -退出程序 我们使⽤花括号以匿名函数的形式将两个命令放到⼀起作为⼀个命令使⽤。⼀般函


数将在下⽂提及。

不⽤与和或操作符，我们也可以⽤if表达式作任何事情，但是使⽤与或操作符会更 便利很多。

case表达式可以⽤来匹配⼀个给定的字符串，⽽不是数字。 case. in

.) do something here esac

让我们看⼀个例⼦。file命令可以辨别出⼀个给定⽂件的⽂件类型， ⽐如：

file lf.gz

这将返回：

lf.gz: gzip compresed data, deflated, original filename,

last modified: Mon Aug 27 23 09 18 201, os: Unix 我们利⽤这⼀点写了⼀个叫做smartzip的脚本，该脚本可以⾃动解压bzip2,gzip 和zip

类型的压缩⽂件：

#!/bin/sh

ftype=`file "$1"` case "$ftype" in "$1: Zip archive"*) unzip"$1"

"$1: gzip compresed"*) gunzip"$1" "$1: bzip2 compresed"*) bunzip2"$1"

*) eror "File $1 can not be uncompresed withsmartzip"; esac

您可能注意到我们在这⾥使⽤了⼀个特殊的变量$1。该变量包含了传递给该程序的

第⼀个参数值。也就是说，当我们运⾏：

smartzip articles.zip

$1 就是字符串articles.zip

select表达式是⼀种bash的扩展应⽤，尤其擅长于交互式使⽤。 ⽤户可以从⼀组不同的值中进⾏选择。

select var in. do

break done

. now $var can be used . 下⾯是⼀个例⼦：

#!/bin/sh echo "What is your favourite OS?" select var in "Linux" "Gnu Hurd"Fre BSD" "Other"; do

break

done echo "You have selected $var"

下⾯是该脚本运⾏的结果：

What is your favourite OS?

- 1) Linux
- 2) Gnu Hurd
- 3) Fre BSD
- 4) Other #? 1 You have selected Linux


l op表达式： while.; do

.

done while-l op 将运⾏直到表达式测试为真。wil run while thexpresion that wetest for is true. 关键字"break" ⽤来跳出循环。 ⽽关键字"continue"⽤来不执⾏余下的部分⽽直接跳到下⼀个循环。

for-l op表达式 查看⼀个字符串列表(字符串⽤空格分隔)然后将其赋给⼀个变量：

for var in .; do

. done 在下⾯的例⼦中，将分别打印ABC到屏幕上：

#!/bin/sh

for var in A B C do

echo"var is $var" done

下⾯是⼀个更为有⽤的脚本showrpm，其功能是打印⼀些RPM包的统计信息：

#!/bin/sh

# list a content sumary of a number of RPM packages # USAGE: showrpm rpmfile1 rpmfile2. # EXAMPLE: showrpm /cdrom/RedHat/RPMS/*.rpm for rpmpackage in $*; do

if[ -r "$rpmpackage" ];then echo" = $rpmpackage =" rpm-qi -p $rpmpackage

else

echo"EROR: canot read file $rpmpackage" fi done

这⾥出现了第⼆个特殊的变量$*，该变量包含了所有输⼊的命令⾏参数值。 如果您运⾏ showrpm opensh.rpm w3m.rpm webgrep.rpm

此时$* 包含了3 个字符串，即opensh.rpm,w3m.rpm and webgrep.rpm.

引号 在向程序传递任何参数之前，程序会扩展通配符和变量。这⾥所谓扩展的意思是程序会

把通配符（⽐如*）替换成合适的⽂件名，它变量替换成变量值。为了防⽌程序作这种替

换，您可以使⽤引号：让我们来看⼀个例⼦，假设在当前⽬录下有⼀些⽂件，两个jpg⽂

件，mail.jpg 和tux.jpg。

#!/bin/sh

echo *.jpg 这将打印出"mail.jpgtux.jpg"的结果。 引号(单引号和双引号)将防⽌这种通配符扩展： #!/bin/sh echo "*.jpg" echo '*.jpg' 这将打印"*.jpg"两次。

单引号更严格⼀些。它可以防⽌任何变量扩展。双引号可以防⽌通配符扩展但允许变量扩展。

#!/bin/sh

echo $SHEL echo "$SHEL" echo '$SHEL'

运⾏结果为：

/bin/bash /bin/bash $SHEL

最后，还有⼀种防⽌这种扩展的⽅法，那就是使⽤转义字符——反斜杆：

echo *.jpg

echo $SHEL

这将输出：

*.jpg $SHEL

Here document. 当要将⼏⾏⽂字传递给⼀个命令时，heredocument.（译者注：⽬前还没有见到过对该

词适合的翻译）⼀种不错的⽅法。对每个脚本写⼀段帮助性的⽂字是很有⽤的，此时如

果我们四有那个heredocument.就不必⽤echo函数⼀⾏⾏输出。 ⼀个"Here

document.quot; 以 < 开头，后⾯接上⼀个字符串，这个字符串还必须出现在here

document.末尾。下⾯是⼀个例⼦，在该例⼦中，我们对多个⽂件进⾏重命名，并且使⽤

here document.打印帮助：

#!/bin/sh # we have les than 3 arguments. Print the help text: if [ $# -lt 3 ] then cat <HELP ren- renames a number of files using sed regularexpresions

USAGE: ren 'regexp' 'replacement' files.

EXAMPLE: rename al *.HTM files in *.html: ren'HTM$' 'html' *.HTM

HELP exit0

fi OLD="$1" NEW="$2" # The shift comand removes one argument from the list of # comand line arguments. shift

shift # $* contains now al the files: for file in $*; do

if[ -f "$file" ] then newfile=`echo"$file" | sed "s/${OLD}/${NEW}/g"` if[ -f "$newfile" ]; then

echo"EROR: $newfile exists already"

else echo"renaming $file to $newfile." mv"$file" "$newfile"

fi fi

done 这是⼀个复杂⼀些的例⼦。让我们详细讨论⼀下。第⼀个if表达式判断输⼊命令⾏参数

是否⼩于3个 (特殊变量$# 表⽰包含参数的个数)。 如果输⼊参数⼩于3个，则将帮助⽂ 字传递给cat命令，然后由cat命令将其打印在屏幕上。 打印帮助⽂字后程序退出。 如果 输⼊参数等于或⼤于3个，我们就将第⼀个参数赋值给变量OLD，第⼆个参数赋值给 变量NEW。 下⼀步，我们使⽤shift命令将第⼀个和第⼆个参数从参数列表中删除，这样原来

的第三个参数就成为参数列表$*的第⼀个参数。然后我们开始循环，命令⾏参数列表被

⼀个接⼀个地被赋值给变量$file。接着我们判断该⽂件是否存在，如果存在则通过sed

命令搜索和替换来产⽣新的⽂件名。然后将反短斜线内命令结果赋值给newfile。这样我

们就达到了我们的⽬的：得到了旧⽂件名和新⽂件名。然后使⽤mv命令进⾏重命名。

函数

如果您写了⼀些稍微复杂⼀些的程序，您就会发现在程序中可能在⼏个地⽅使⽤了相同

的代码，并且您也会发现，如果我们使⽤了函数，会⽅便很多。 ⼀个函数是这个样⼦的 ：

functioname()

{ # inside the body $1 is the first argument given to thefunction # $2 the second. body }

您需要在每个程序的开始对函数进⾏声明。

下⾯是⼀个叫做xtitlebar的脚本，使⽤这个脚本您可以改变终端窗⼜的名称。这⾥使⽤ 了⼀个叫做help的函数。正如您可以看到的那样，这个定义的函数被使⽤了两次。

#!/bin/sh

# vim: set sw=4 ts=4 et:

help() {

cat<HELP xtitlebar- change the name of an xterm, gnome-terminal orkde konsole USAGE: xtitlebar [-h] "string_for_titelbar"

OPTIONS: -h help text

EXAMPLE: xtitlebar "cvs"

HELP

exit0 }

# in case of eror or if -h is given we cal the functionhelp: [ -z "$1" ] & help [ "$1" = "-h" ] & help

# send the escape sequence to change the xterm titelbar: echo -e "3]0;$107" #在脚本中提供帮助是⼀种很好的编程习惯，这样⽅便其他⽤户（和您）使⽤和理解脚本。

命令⾏参数

我们已经见过$*和$1, $2. $9 等特殊变量，这些特殊变量包含了⽤户从命令 ⾏输⼊的参数。迄今为⽌，我们仅仅了解了⼀些简单的命令⾏语法（⽐如⼀些强制性的

参数和查看帮助的-h选项）。但是在编写更复杂的程序时，您可能会发现您需要更多的

⾃定义的选项。通常的惯例是在所有可选的参数之前加⼀个减号，后⾯再加上参数值(

⽐如⽂件名)。

有好多⽅法可以实现对输⼊参数的分析，但是下⾯的使⽤case表达式的例⼦⽆遗是 ⼀个不错的⽅法。

#!/bin/sh

help() {

cat<HELP This is a generic comand line parser demo. USAGE EXAMPLE: cmdparser -l helo -f - -somefile1 somefile2 HELP

exit0 }

while [ -n "$1" ]; do

case $1 in

- -h)help;shift 1; # function help is caled
- -f)opt_f=1;shift 1; # variable opt_f is set
- -l)opt_l=$2;shift 2; # -l takes an argument -> shift by 2


- -)shift;break; # end of options
- -*)echo "eror: no such option $1. -h for help";exit 1;


*)break;

esac done

echo "opt_f is $opt_f"

echo "opt_l is $opt_l" echo "first arg is $1" echo "2nd arg is $2"

您可以这样运⾏该脚本：

cmdparser -l helo -f - -somefile1 somefile2

返回的结果是：

opt_f is 1 opt_l is helo first arg is -somefile1 2nd arg is somefile2

这个脚本是如何⼯作的呢？脚本⾸先在所有输⼊命令⾏参数中进⾏循环，将输⼊参 数与case表达式进⾏⽐较，如果匹配则设置⼀个变量并且移除该参数。根据unix系统的

惯例，⾸先输⼊的应该是包含减号的参数。

实例

⼀般编程步骤

现在我们来讨论编写⼀个脚本的⼀般步骤。任何优秀的脚本都应该具有帮助和输⼊ 参数。并且写⼀个伪脚本（framework.sh），该脚本包含了⼤多数脚本都需要的框架结

构，是⼀个⾮常不错的主意。这时候，在写⼀个新的脚本时我们只需要执⾏⼀下copy命

令：

cp framework.sh myscript 然后再插⼊⾃⼰的函数。

让我们再看两个例⼦：

⼆进制到⼗进制的转换

脚本b2d 将⼆进制数(⽐如 101) 转换为相应的⼗进制数。这也是⼀个⽤expr命

令进⾏数学运算的例⼦：

#!/bin/sh

# vim: set sw=4 ts=4 et: help() {

cat<HELP b2h- convert binary to decimal USAGE: b2h [-h] binarynum

OPTIONS: -h help text

EXAMPLE: b2h 1010

wil return 58 HELP

exit0 }

eror()

{

#print an eror and exit echo"$1" exit1

} lastchar() { #该函数使⽤wc -c计算字符个 数，然后使⽤cut命令取出末尾⼀个字符。

#return the last character of a string in $rval if[ -z "$1" ]; then

#empty string rval=" return

fi #wc puts some space behind the output this is why we ned sed: numofchar=`echo-n "$1" | wc -c | sed 's/ /g' ` #now cut out the last char rval=`echo-n "$1" | cut -b $numofchar`

} chop() { #Chop函数的功能则是移除最后⼀个字符。

#remove the last character in string and return it in $rval if[ -z "$1" ]; then

#empty string rval=" return

fi #wc puts some space behind the output this is why we ned sed: numofchar=`echo-n "$1" | wc -c | sed 's/ /g' ` if[ "$numofchar" = "1" ]; then

#only one char in string rval=" return

fi numofcharminus1=`expr$numofchar "-" 1` #now cut al but the last char: rval=`echo-n "$1" | cut -b 0-${numofcharminus1}`

}

while [ -n "$1" ]; do

case $1 in

- -h)help;shift 1; # function help is caled
- -)shift;break; # end of options
- -*)eror "eror: no such option $1. -h for help";


*)break;

esac done

# The main program

sum=0 weight=1 # one arg must be given: [ -z "$1" ] & help bi num="$1" bi numorig="$1"

while [ -n "$bi num" ]; do lastchar"$bi num" if[ "$rval" = "1" ]; then

sum=`expr"$weight" "+" "$sum"`

fi #remove the last position in $bi num chop"$bi num" bi num="$rval" weight=`expr"$weight" "*" 2`

done

echo "binary $bi numorig is decimal $sum" #

该脚本使⽤的算法是利⽤⼗进制和⼆进制数权值(1,2,4,8,16,.)，⽐如⼆进 制"10"可以这样转换成⼗进制： 0* 1 + 1 * 2 = 2

为了得到单个的⼆进制数我们是⽤了lastchar函数。

⽂件循环程序

或许您是想将所有发出的邮件保存到⼀个⽂件中的⼈们中的⼀员，但是在过了⼏个 ⽉以后，这个⽂件可能会变得很⼤以⾄于使对该⽂件的访问速度变慢。下⾯的脚本

rotatefile 可以解决这个问题。这个脚本可以重命名邮件保存⽂件（假设为outmail）

为outmail.1，⽽对于outmail.1就变成了outmail.2等等等等 .

#!/bin/sh

# vim: set sw=4 ts=4 et: ver="0.1" help() {

cat<HELP

rotatefile- rotate the file name USAGE: rotatefile [-h] filename

OPTIONS: -h help text

EXAMPLE: rotatefile out

This wil e.g rename out.2 to out.3, out.1 to out.2, out tout.1 and create an empty out-file

The max number is 10

version $ver

HELP

- exit0

}

eror() {

echo"$1"

- exit1


} while [ -n "$1" ]; do case $1 in

- -h)help;shift 1;
- -)break;
- -*)echo "eror: no such option $1. -h for help";exit 1;


*)break;

esac done

# input check: if [ -z "$1" ]then eror "EROR: you must specify a file, use -h forhelp" fi filen="$1" # rename any .1 , .2 etc file&: for n in 9 8 7 6 5 4 3 2 1; do

if[ -f "$filen.$n" ]; then p=`expr$n + 1` echo"mv $filen.$n $filen.$p" mv$filen.$n $filen.$p

fi done # rename the original file&: if [ -f "$filen" ]; then

echo"mv $filen $filen.1" mv$filen $filen.1

fi echo touch $filen touch $filen

这个脚本是如何⼯作的呢？在检测⽤户提供了⼀个⽂件名以后，我们进⾏⼀个9到1 的循环。⽂件9被命名为10，⽂件8重命名为9等等。循环完成之后，我们将原始⽂件命名

为⽂件1同时建⽴⼀个与原始⽂件同名的空⽂件。

调试

最简单的调试命令当然是使⽤echo命令。您可以使⽤echo在任何怀疑出错的地⽅打

印任何变量值。这也是绝⼤多数的shel程序员要花费80%的时间来调试程序的原因。

Shel程序的好处在于不需要重新编译，插⼊⼀个echo命令也不需要多少时间。

shel也有⼀个真实的调试模式。如果在脚本"strangescript"中有错误，您可以这

样来进⾏调试：

sh-x strangescript 这将执⾏该脚本并显⽰所有变量的值。 shel还有⼀个不需要执⾏脚本只是检查语法的模式。可以这样使⽤： sh-n your_script 这将返回所有语法错误。 我们希望您现在可以开始写您⾃⼰的shel脚本，希望您玩得开⼼。

