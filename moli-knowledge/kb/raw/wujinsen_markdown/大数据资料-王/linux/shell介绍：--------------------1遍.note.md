## shel介绍：

#为什么使⽤shel脚本？ shel脚本实现⾃动化运维 可以⾃动化管理系统

#查看系统中⽀持的所有shel类型 #cat /etc/shels #chsh -l

#如何切换shel 直接输⼊shel的名字 例： #bash 切换到bash sh，bash，nologin，tcsh，csh，ksh

#查看当前使⽤的shel #echo $SHEL

#bash功能和特点

- 1 命令和⽂件⾃动补⻬
- 2 历史记录功能 上下键、!number、!string、!$、 !、^r
- 3 别名功能 alias、unalias cp、~username/.bashrc、\cp -rf /etc/hosts .
- 4 快捷键 ^c、^d、^a、^e、^l、^s、^q
- 5 前后台作业控制 &、nohup、^c、^z、bg、fg %1、kil %3
- 6 输⼊输出重定向 0,1,2, >, >, 2>, 2>, 2>&1, &>, <, cat <EOF, cat >file1 <EOF
- 7 管道 | te ip adr |grep 'inet ' |te -a test |grep eth0 -a 追加
- 8 命令排序 ; 不具备逻辑判断

& 具备逻辑判断 | 具备逻辑判断

- 9 shel元字符表示的不是本意

* ? & ; $ [] () {} \

- 10 变量 1 shel script 脚本


#如何执⾏shel脚本

- 1 # source demo.sh 不需要执⾏权限 在当前shel中执⾏


- 2 # . demo.sh 不需要执⾏权限 在当前shel中执⾏
- 3 # sh demo.sh 不需要执⾏权限 在⼦shel中执⾏ # sh -x demo.sh 以调试的⽅式执⾏ # sh -n demo.sh 仅调试 syntax eror
- 4 # bash demo.sh 不需要执⾏权限 在⼦shel中执⾏
- 5 # ./demo.sh 需要执⾏权限 在⼦shel中执⾏


#修改shel脚本执⾏权限 #vim demo.sh #chmod a+x demo.sh

# shel变量

#什么是变量？ ⽤⼀串固定的字符来保存经常变化的字符

#变量的类型

- 1 ⾃定义变量 ⽤户在编写脚本时根据⾃⼰的需求定义的变量
- 2 系统环境变量 系统在启动的过程中读取⾃⼰的配置⽂件定义的变量 PATH＝/bin/:$PATH
- 3 位置变量 $1 $2 $3 $4 $5 $6 $7 $8 $9 ${10}
- 4 预定义变量 $* $@ $# $ $?(上⼀个命令的返回值 0表示成功)


#变量的定义 例： #name=gaoyaohua 变量名字＝变量值 注： 变量名命名必须以字⺟或下划线开头 区分⼤⼩写字⺟ 例： #export name=gaoyaohua 例： #wo=gaoyaohua #export wo export把变量定义为全局环境变量（当前shel跟⼦shel都有效）

#变量的取消 例： #unset name

#变量的查看 #set /显示系统内的所有变量，包括⾃定义变量 #env/只显示系统环境变量，不会显示⾃定义变量

#变量的调⽤ 例： #echo $name

$变量名 例： #echo 'your name is $name.' ' ' 单引号 变量原样输出(弱引⽤) 例： #echo "your name is $name." " " 双引号 变量被变量值取代(强引⽤)

#创建数组变量 ⽅法⼀： ⼀次赋⼀个值 数组名[下标]=变量值

- # aray[0]=pear
- # aray[1]=aple
- # aray[2]=orange
- # aray[3]=peach


⽅法⼆： ⼀次赋多个值 # aray=(tom jack alice) # aray=(`cat /etc/paswd`) 希望是将该⽂件中的每⼀个⾏作为⼀个元数赋值给数组aray3 # aray=(`ls /var/ftp/shel/for*`) # aray=(tom jack alice "bash shel")

# declare -a declare -a aray='([0]="pear" [1]="aple" [2]="orange" [3]="peach")' declare -a aray='([0]="tom" [1]="jack" [2]="alice")'

访问数组元数： # echo ${aray[0]} 访问数组中的第⼀个元数 # echo ${aray[@]} 访问数组中所有元数 等同于 echo ${aray1[*]} # echo ${#aray[@]} 统计数组元数的个数

# echo ${aray[@]:1} 从数组下标1开始 # echo ${aray[@]:1 2} 从数组下标1开始，访问两个元素

# 条件语句：

#整数值⽐较 [ 整数1 选项 整数2 ]

- -ne 不等于
- -eq 等于
- -ge ⼤于等于
- -le ⼩于等于
- -gt ⼤于
- -lt ⼩于 #字符串⽐较 [ 字符串1 ⽐较符 字符串2 ]


= 或 = !=不等于

-z 测试字符串是nul的时候为真 #逻辑⽐较 ［ 表达式1 ］ 符号 ［ 表达式2 ］ 符号 ［ 表达式3 ］

& -a and | -o or

# 流程控制：

# 条件语句 单分⽀ if［ 条件表达式 ];then echo "demo 1" fi 双分⽀ if [ 条件表达式 ]; then

- echo "demo2" else
- echo "demo3" fi 多分⽀结构


if [ 条件表达式 ];then

- echo "demo 1" elif [ 条件表达式 ];then
- echo "demo 2" elif [ 条件表达式 ];then
- echo "demo 3" elif [ 条件表达式 ];then
- echo "demo 4" else
- echo "demo 5" fi


#循环语句 for for 变量名 in 变量值列表 do echo "demo 1" done

while while 条件表达式 do echo "demo 1" done until until 条件表达式 do echo "demo 1" done

#选择语句 case case $变量名 in

- 模式1) echo "demo 1"

;

- 模式2)


- echo "demo 1" ;
- 模式3) echo "demo 1"


; *) echo "demo 1"

; esac

例⼦： select choice in gren blue red quit "bash shel" do

case $choice in red)

echo "红⾊" ; gren) echo "绿⾊"

; blue)

echo "蓝⾊" ;

*)

exit esac

done

# 函数：

#函数 完成特定功能的代码⽚段（块） 在shel中定义函数可以使⽤代码模块化，便于复⽤代码 函数必须先定义才可以使⽤

#定义函数的⽅法

⽅法⼀： 函数名() { 函数要实现的功能代码 }

⽅法⼆： function 函数名 { 函数要实现的功能代码 }

#调⽤函数的⽅法 函数名 函数名 参数1 参数2

exit break continue shift

shift 作⽤：使位置参数向左移动，默认移动1位，可以使⽤shift 2 exit 退出整个程序 break 结束当前循环，或跳出本层循环 continue 忽略本次循环剩余的代码，直接进⾏下⼀次循环

