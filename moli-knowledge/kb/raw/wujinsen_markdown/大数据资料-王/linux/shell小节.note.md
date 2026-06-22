- 1、shel中，获取⼀个命令的执⾏结果 a=`ps -ef | grep tomcat` 注意：“ ` ”这个符号不是单引号，⽽是esc下⾯的那个⼩撇
- 2、读取配置⽂件[rot@master local]# vi host.conf master="192.168.56.20 master"

- slave1="192.168.56.201 slave1"
- slave2="192.168.56.202 slave2"
- slave3="192.168.56.203 slave3" 读取： source ./host.conf 读取每个变量： echo $master > /etc/hosts


- 3、vi显示⾏号 :set nu


4'string' 单引号和"string" 双引号双引号 如果想在定义的变量中加⼊空格，就必须使⽤单引号或双引号，

单、双引号的区别在于双引号转义特殊字符⽽单引号不转义特殊字符 eg: $ heyou=home

$ echo '$heyou' $ $heyou （$没有转义）

eg: $ heyou=home $ echo "$heyou" $ home （很明显，$转义了输出了heyou变量的值）

- 5、对字符串判空

-z string 如果 string ⻓度为零，则为真 [ -z $myvar ]

-n string 如果 string ⻓度⾮零，则为真 [ -n $myvar ]

- 6、将字符串转换为数组 #host config for node in "$MASTER" "$MASTER_HA" "$SLAVE1" "$SLAVE2" "$SLAVE3"; do


aray=( $node ) # if host or ip is not exist in /etc/hosts, then ad. if [ -z "`grep "${aray[0]}" /etc/hosts`" -o -z "`grep "${aray[1]}" /etc/hosts`" ]; then

echo ${aray[@]} > /etc/hosts

# fi done

