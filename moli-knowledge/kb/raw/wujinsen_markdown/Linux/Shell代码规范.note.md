关于注释

程序头应加注版本与功能说明的注释。但程序第⼀⾏不能汉字。 程序体中应包含必要的注释，注释说明如下： 单⾏注释，可以放在代码⾏的尾部或代码⾏的上部； 多⾏注释，⽤于注解复杂的功能说明，可以放在程序体中，也可以放在代码块的开始部分 代码修改时，对修改的内容要加必要版本注释及功能说明。

命名约定

- 1.本⽂档的命名约定是系统配置⽂件、脚本⽂件；
- 2.⽂件名、变量名、函数名不超过20个字符；
- 3.命名只能使⽤英⽂字⺟，数字和下划线，只有⼀个英⽂单词时使⽤全拼，有多个单词时，使⽤下划线分隔，⻓度较⻓时，可以取单词 前3～4个字⺟。
- 4.⽂件名全部以⼩写命名，不能⼤⼩写混⽤（通过U盘交换⽂件时，⼤⼩写可能会丢失，即：⼤写⽂件名可能会全部变成⼩写⽂件名）；
- 5.避免使⽤Linux的保留字如true、关键字如PWD等（⻅附表）；
- 6.从配置⽂件导出配置时，要注意过滤空⾏和注释


函数约定

函数名称应该采⽤⼩写的形式，并且有⼀个很好的意义。函数名称应该容易让⼈理解，⽐如f1这个名称虽然容 易输⼊但是对调试和其它⼈阅读代码造成了很⼤的困难，它说明不了任何东⻄。好的函数名称可以帮助说明 代码，⽽不需要额外的注释。 ⼀个或多或少有趣的是：如果你⽆意这样做，不要把函数名称命名为常⻅的命令名，新⼿往往⽐较容易将脚 本或者函数名命名成test，这样就和UNIX的test命令冲突了。

除⾮绝对必要，仅使⽤字⺟、数字和下划线作为函数名称。 每个函数控制在50－100⾏，超出⾏数建议分成两个函数 多次反复调⽤的程序最好分成函数，可以简化程序，使程序条理更清楚 所有函数定义应该在脚本主要代码执⾏之前，这样可以给⼈全局的印象，并且确保所有函数在使⽤之前它 是已知的。 你应该使⽤可移植性⾼的函数定义形式，即不带function关键字的形式。

代码开头约定

- 1、第⼀⾏⼀般为调⽤使⽤的语⾔
- 2、下⾯要有这个程序名，避免更改⽂件名为⽆法找到正确的⽂件
- 3、版本号
- 4、更改后的时间
- 5、作者相关信息
- 6、该程序的作⽤，及注意事项
- 7、版权与是否开放共享GNU说明
- 8、最后是各版本的更新简要说明 如下⾯的例⼦：


#!/bin/bash # ------------------------------------------------------------------------------# Filename: check_mem.sh # Revision: 1.1 # Date: 2009/02/10 # Author: Ajian # Email: ajian521#gmail.com # Website: www.ohlinux.com # Description: Plugin to monitor the memory of the system # Notes: This plugin uses the "" command # ------------------------------------------------------------------------------# Copyright: 2009 (c) Ajian # License: GPL # # This program is free software; you can redistribute it and/or # modify it under the terms of the GNU General Public License # as published by the Free Software Foundation; either version 2 # of the License, or (at your option) any later version. # # This program is distributed in the hope that it will be useful, # but WITHOUT ANY WARRANTY; without even the implied warranty # of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the # GNU General Public License for more details. # # you should have received a copy of the GNU General Public License # along with this program (or with Nagios); # # Credits go to Ethan Galstad for coding Nagios # If any changes are made to this script, please mail me a copy of the changes # -------------------------------------------------------------------------------

- #Version 1.0 #The ﬁrst one , can monitor the system memory
- #Version 1.1 #Modify the method of the script ,more fast 缩进 由于Shell没有很好的编辑环境，所以，建议⽤四个空格为基数进⾏缩进，好处在不同的环境下TAB可能代表 的空格数不同，造成代码的错乱。⽤TAB他的优点是速度快⽅便，可以在编辑的时候也⽤TAB，但需要转换。 可以在更改编辑器，Windows的就不说了，主要是VIM :set softtabstop=4 注意不要使⽤ :set tabstop=4 上⾯那个是同时把这⼀个TAB转换为四个空格，⽽这⼀条是定义TAB为四个空 格，如果到其它编辑器上就会看到默认8个空格的情况，那就会不美观了。 另外将原有的TAB转换为空格，:retab 如果想让刚才的配置永久⽣效需要改动vim的配置⽂件 vim ~/.vimrc,更多详细的有⽤的配置⻅“VIM配置总结” 分隔⻓⾏ 每⾏不要超过80字，如果超出，建议⽤“\”折⾏，有管道的命令⾏除外。 如果需要分隔过⻓的代码，你可以使⽤下⾯的任意⼀种⽅法：


- 1） 使⽤与命令宽度相同的缩进 activate some_very_long_option \

some_other_option

- 2） 使⽤2个空格缩进 activate some_very_long_option \


some_other_option

从个⼈的⻆度来说，除⾮有特别的需要，我更倾向于第⼀种形式，因为它突出“上下两⾏的内容是⼀起的”这⼀ 联系。

# 分离复合命令

译者注：其实这⾥的复合命令就是指块语句，例如for/while循环, if分⽀结构等等。

HEAD_KEYWORD parameters; BODY_BEGIN

BODY_COMMANDS BODY_END

我习惯于：

将HEAD_KEYWORD和初始化命令或者参数放在第⼀⾏； 将BODY_BEGIN同样放在第⼀⾏； 复合命令中的BODY部分以2个空格缩进； BODY_END部分独⽴⼀⾏放在最后；

- 1）if/then/elif/else分⽀语句

if ...; then

... elif ...; then

... else

... ﬁ

- 2）for循环

for f in /etc/*; do

... done

- 3） while/until循环

while [[ $answer != [YyNn] ]]; do

... done

- 4） case分⽀语句


case $input in hello) echo "You said hello"

;; bye)

echo "You said bye" if foo; then

bar ﬁ

;;

*)

echo "You said something weird..." ;; esac

⼏点注意的地⽅：

如果不是100%需要，匹配部分左右的括号不需要写（译者注：例如写成hello)⽽不是(hello)）； 匹配模式与分⽀的终⽌符号;;位于同⼀缩进级别 分⽀内部的命令多缩进⼀层； 尽管是可选的，这⾥还是把最后⼀个分⽀的终⽌符号也写上了；

# 参数展开

除⾮你知道⾃⼰做的事情，请在参数展开的地⽅使⽤双引号 当然，也有⼀些地⽅并不需要使⽤双引号，例如：

[[ ]]测试表达式内部是不会展开的； 在case $WORD in语法中WORD也不会展开的； 在变量赋值var=$WORD的地⽅也是不会展开的 但是在这些地⽅使⽤引号并不会出错，如果你习惯于在每 个可能展开参数的地⽅使⽤引号，你写得代码会很安全。

如果你要传递⼀个参数作为⼀个单词列表，你可以不使⽤引号，例如：

list="one two three"

# you MUST NOT quote $list here for word in $list; do

... done

# 命令替换

正如⽂章the article about command substitution [Bash Hackers Wiki]中提及的，你应该使⽤$( .. )形式。 不过，如果可移植性是⼀个问题，你可能必须使⽤反引号的形式...。 在任何情况，如果其它展开或者单词分隔并不是你期望的，你应该将命令替换⽤双引号引起来。

# 环境变量

变量：全部是⼤写字⺟ 变量引⽤：全部以变量名加双引号引⽤，如”$TERMTYPE”，或“${TERMTYPE}”，如果变量类型是数值型不 引⽤，如: 如果需要从配置⽂件导出变量，则在变量前加⼀⼤写字⺟，以识别导出变量与⾃定义环境变量的区别，如： 变量值的引⽤尽量以$开头，如$(ls inst_.sh)，避免使⽤`ls inst_。sh` 循环控制变量可以命名为单个字⺟， ⽐如 i、j等。 也可以是更有意义的名称， ⽐如 UserIndex。 环境变量和全局变量 在脚本开头定义。 函数中使⽤较多的⽂件，以环境变量的形式在⽂件开头定义，仅函数中使⽤的变量在函数开头定义

# 配置变量

在这⾥，我将这⼀类变量——可以被⽤户更改的——叫做配置变量。 让这类变量容易找到，⼀般放在脚本的头部，给它们有意义的名称并且加上注释说明。正如上⾯说的，仅当 你知道你为什么这么做的时候，才⽤⼤写的变量名形式，否则⼩写形式更加安全。

# 语句

if 语句 if/then/else 语句中最可能被执⾏的部分应该放在 then ⼦句中， 不太可能被执⾏的部分应该放在 else ⼦句 中。 如果可能， 尽量不要使⽤⼀连串的 if 语句， ⽽应该以 case 语句替代。 不要使 if 语句嵌套超过5层以上， 尽量以更清楚的代码替代。 case 语句 概要 case 语句中的单个⼦句应该以 case 常数的数字顺序或字⺟顺序排列。 ⼦句中的执⾏语句应该尽量保持简 单， ⼀般不要超过4到5⾏代码。 如果执⾏语句过于复杂， 应该将它放置在独⽴的函数中。 case 语句的 *) ⼦句应该只在正常的默认情况或检测到错误的情况下使⽤。 格式

case 语句遵循同样的缩进和命名约定。 while 语句 使⽤ Exit 过程退出 while 循环是不好的; 如果可能， 应该只使⽤循环条件来结束循环。 while 循环的所有初始化代码应该紧贴在进⼊ while 循环之前， 不要被其他⽆关语句分隔开。 循环结束后的处理应该紧跟在循环之后。 for 语句 如果需要执⾏确定次数的增量循环， 应该⽤ for 语句替代 while 语句。

# 脚本的基本结构

⼀个脚本的基本结构是这样的：

#!SHEBANG

CONFIGURATION_VARIABLES

FUNCTION_DEFINITIONS

MAIN_CODE Shebang

如果可能，请不要忘记shebang。 请⼩⼼使⽤/bin/sh作为shebang，在 系统中，/bin/sh就是Bash这是⼀个错误的观点。 于我⽽⾔，shebang有两个⽬的：

## Linux

说明直接执⾏时以哪个解释器来执⾏； 明确该脚本应该以哪个解释器来执⾏；

# 脚本⾏为和健壮性

当脚本检测到问题时尽早退出，以免执⾏潜在的问题； 如果你需要⽤到的命令可能并没有安装在系统上，在 脚本执⾏的时候最好检查命令是否存在并且提醒⽤户缺少什么； 采⽤有意义的脚本返回值，例如0代码成功， 1代码错误或者失败；

# 其它 输出内容

if the script is interactive, if it works for you and if you think this is a nice feature, you can try to save the terminal content and restore it after execution；（译者注：不理解这⼀点是什么意思） 在屏幕中输出简单易 理解的消息； 使⽤颜⾊或者特别的前缀区分错误和警告信息； 输出正常的内容到STDOUT，⽽输出错误、警 告或者诊断的信息到STDERR； 在⽇志⽂件中输出所有详细的信息；

# 输⼊

不要盲⽬地假设任何事情，如果你希望⽤户输⼊⼀个数字，请在脚本中主动检查它是否真得是⼀个数字，检 查头部是否包含0，等等。我们都应该知道这⼀点，⽤户仅仅是⽤户⽽不是程序员，他们会做他们想要的，⽽ 不是程序想要的。

