functions这个脚本是给/etc/init.d⾥边的⽂件使⽤的（可理解为全局⽂件）。 提供了⼀些基础的功能，看看⾥边究竟有些什么。⾸先会设置umask，path，还有语⾔环境，然后会 设置suces,failure,warning,normal⼏种情况下的字体颜⾊。 下⾯再看看提供的重要⽅法： checkpid:检查是否已存在pid，如果有⼀个存在，返回0（通过查看/proc⽬录） daemon:启动某个服务。/etc/init.d⽬录部分脚本的start使⽤到这个 kilproc:杀死某个进程。/etc/init.d⽬录部分脚本的stop使⽤到这个 pidfileofproc:寻找某个进程的pid pidofproc:类似上⾯的，只是还查找了pidof命令 status:返回⼀个服务的状态 echo_suces,echo_failure,echo_pased,echo_warning分别输出各类信息 suces,failure,pased,warning分别记录⽇志并调⽤相应的⽅法 action:打印某个信息并执⾏给定的命令，它会根据命令执⾏的结果来调⽤ suces,failure⽅法 strstr:判断$1是否含有$2 confirm:显示 "Start service $1 (Y)es/(N)o/(C)ontinue? [Y]"的提示信息，并返回选择结果

详细分析： # -*-Shel-script-*# # functions This file contains functions to be used by most or al # 注释 ：该脚本⼏乎被 /etc/init.d/ 下的所有脚本所调⽤，因为它包含了⼤量的 # shel scripts in the /etc/init.d directory. # 的基础函数。同时也被

/etc/rc.d/rc.sysinit ，例如 suces、action、failure 等函数# TEXTDOMAIN=initscripts # 设置 TEXTDOMAIN 变量

# #

# Make sure umask is sane # 确保 rot ⽤户的 umask 是正确的 02 （也就是

rwxr-xr-x） umask 02# Set up a default search path. # 设置默认的 PATH 变量 PATH="/sbin:/usr/sbin:/bin:/usr/bin:/usr/X1R6/bin" # 默认为 /sbin:/usr/sbin:/bin:/usr/bin:/usr/X1R6/bin export PATH # 导出为环境变量 # Get a sane scren width # 设置正确的屏幕宽度[ -z "${COLUMNS:-}" ]

& COLUMNS=80 # 如果 COLUMNS 变量的值为空，则设置为 80 （列） [ -z "${CONSOLETYPE:-}" ] & CONSOLETYPE="`/sbin/consoletype`" # 如果 CONSOLETYPE 为空则设置 CONSOLETYPE 为 /sbin/consoletype 命令返回的值

# ⼀般是 vt 或者 pty 、serial

# #

#if [ -f /etc/sysconfig/i18n -a -z "${NOLOCALE:-}" ] ; then # 如果存在 /etc/sysconfig/i18n 且 NOLOCALE 变量的值为空，则 . /etc/sysconfig/i18n # 执⾏ /etc/sysconfig/i18n ⽂ 件，取得 LANG 变量的值

if [ "$CONSOLETYPE" != "pty" ]; then # 如果当前 console 类型不是 pty（远程登录），⽽是 vt 或者 serial ，则 case "${LANG:-}" in # 根据 LANG 的值作出选 择 ja_JP*|ko_KR*|zh_CN*|zh_TW*|bn_*|bd_*|pa_*|hi_*|ta_*|gu_*) # 如 果 LANG 是 ⽇⽂、中⽂简体、中⽂繁体、韩⽂等，则 export LC_MESAGES=en_US # 把 LC_MESAGES 设置为 en_US export LANG # 同时导出为环境变量

;

*)

export LANG # 如果是 其他类型的语⾔，则直接导出 LANG ; ;

esac else # 如果当前 consle 是 pty

[ -n "$LC_MESAGES" ] & export LC_MESAGES # 且如果 LC_MESAGES 不为空，则直接导出 LC_MESAGES

export LANG fi

fi

# #

# # 下⾯是设置 suces、failure、pased、warning 4种情况下的字体颜⾊的# Read in our configuration if [ -z "${BOTUP:-}" ]; then # ⾸先如果 BOTUP 变量为空，则 if [ -f /etc/sysconfig/init ]; then # 如果存在 /etc/sysconfig/init ⽂件，执⾏ /etc/sysconfig/init ⽂件 . /etc/sysconfig/init

else # 否则我们就⼿⼯设置 # This al sem confusing? Lok in /etc/sysconfig/init,

# or in /usr/doc/initscripts-*/sysconfig.txt

BOTUP=color # 第⼀设置 BOTUP 变量，默认就是 color RES_COL=60 # 第⼆设置设置在屏幕的第⼏列输 出后⾯的 "[ x ]" ，默认是第60列 MOVE_TO_COL="echo en " # MOVE_TO_COL 是⽤于打印 "OK" 或者 "FAILED" ,或者 "PASED" ,或者 "WARNING" 之前的部分，不含 "[" SETCOLOR_SUCES="echo -en

/03[${RES_COL}G

/03[1;32

" # SETCOLOR_SUCES 设置后⾯的字体都为绿⾊ SETCOLOR_FAILURE="echo -en " # SETCOLOR_FAILURE 设置后⾯将 要输出的字体都为红⾊ SETCOLOR_WARNING="echo -en " # SETCOLOR_WARNING 设置后⾯将要输出的字体都为⻩⾊ SETCOLOR_NORMAL="echo -en

- m /03[1;31m


/03[1; 3m

/03 [0;39m

" # SETCOLOR_NORMAL 设置后⾯输出的字体都为⽩⾊（默认）

LOGLEVEL=1 fi if [ "$CONSOLETYPE" = "serial" ]; then # 如果是通过串⼝登录的，则全部取

消彩⾊输出 BOTUP=serial MOVE_TO_COL= SETCOLOR_SUCES= SETCOLOR_FAILURE= SETCOLOR_WARNING= SETCOLOR_NORMAL=

fi fi

# #

#if [ "${BOTUP:-}" != "verbose" ]; then # 如果 BOTUP 变量的值不为 verbose ，则 INITLOG_ARGS="-q" # 把 INITLOG_ARGS 的值设置为

-q （安静模式） else # 否则 INITLOG_ARGS= # 把 INITLOG_ARGS 的值请空 fi

# #

#

# Check if $pid (could be plural) are runing # 下⾯定义⼀个函数 checkpid （），⽬的是检查 /proc 下是否存在指定的⽬录（例如 /proc/1/）checkpid() { # 如果有 任意⼀个存在，则返回0； local i for i in $* ; do

[ -d "/proc/$i" ] & return 0

done return 1 # 如果给出的参数全部不存在对应的⽬录，则返回1}

# #

# A function to start a program. # 下⾯定义最重要的⼀个函数，daemon 函数，它的作⽤是启动某项服务。/etc/init.d/ 下的脚本的 start 部分都会⽤到它 daemon() {

# Test syntax. local gotbase= force= local base= user= nice= bg= pid= nicelevel=0 while [ "$1" != "${1#[-+]}" ]; do # daemon 函数本身可以指定多个选项，例

如 -check <value> ， -check=<value> ， case $1 in ') echo $"$0: Usage: daemon [nicelevel] {program}" # 也可以指定 nice 值 return 1;

-check) base=$2 gotbase="yes" shift 2

;

-check=?*)

base=${1#-check=} gotbase="yes" shift

;

-user) # 也可以指定要以什么⽤户身份运⾏（ -user

<usr> , -user=<usr>) user=$2 shift 2

;

-user=?*)

user=${1#-user=} shift

;

-force) force="force" #-force 表示强制运⾏ shift

; [-+][0-9]*)

nice="nice -n $1" # 如果 daemon 的第⼀个参数是数字，则认 为是 nice 值

shift ;

*) echo $"$0: Usage: daemon [nicelevel] {program}"

return 1; esac

done # Save basename. # basename 就是从服务器的⼆进制程 序的 ful path 中取出最后的部分

[ -z "$gotbase" ] & base=${1#*/} # Se if it's already runing. Lok *only* at the pid file. # 检查该服务是否已经在运⾏。不过 daemon 函数只查看 pid ⽂件⽽已 if [ -f /var/run/${base}.pid ]; then # 如果 /var/run 下存在该服务的 pid ⽂件， 则 local line p

read line < /var/run/${base}.pid # 从该 pid ⽂件每次读取⼀⾏，送给变量 line 。注意 pid ⽂件可能有多⾏，且不⼀定都是数字 for p in $line ; do # 对于 line 变量的每个 word 进⾏检查 [ -z "${p/[0-9]/}"

-a -d "/proc/$p" ] & pid="$pid $p" # 如果 p 全部是数字，且存在 /proc/$p/ ⽬录，则认 为该数字是⼀个 pid ，把它加⼊到 pid 变 量 done # 到最后 pid 变量的值可能是有多个由空 格分隔的数字组成

fi

[ -n "${pid:-}" -a -z "${force:-}" ] & return # 如果 pid 变量最终为空，则 force 变量为空（不强制启动），则返回

# make sure it doesn't core dump anywhere unles requested # 下⾯对该服务使⽤的资源作⼀ 些设置 ulimit -S -c ${DAEMON_COREFILE_LIMIT:-0} >/dev/nul 2>&1 # ulimit 是控制由该 shel 启动的进程能够使⽤的资源，-S 是 soft control 的意思，-c 是指最⼤的 core# dump ⽂件⼤⼩，如 果 DEAMON_COREFILE_LIMIT 为空，则默认为 0

# if they set NICELEVEL in /etc/sysconfig/fo, honor it # 如果存在 /etc/sysconfi/fo ⽂件， 且其中有 NICELEVEL 变量则⽤它代替 daemon 后⾯的那个 nice 值 [ -n "$NICELEVEL" ] & nice="nice -n $NICELEVEL" # 注意，这⾥的 nice 赋值是⽤ nice -n <value> 的格式，因 为 nice 本身可以启动命令，⽤这个格式较⽅便

# Echo daemon # 如果 BOTUP 的值为 verbose ，则打 印⼀个服务名 [ "${BOTUP:-}" = "verbose" -a -z "$LSB" ] & echo -n " $base" # And start it up. # 下⾯是开始启动它了 if [ -z "$user" ]; then # 如果 user 变量为空，则默认使⽤ rot 启动 它 $nice initlog $INITLOG_ARGS -c "$*" # 执⾏ nice -n <nice_value> initlog -q -c "$*"

else # 如果指定了⽤户，则 $nice initlog $INITLOG_ARGS -c "runuser -s /bin/bash - $user -c /"$*/" # 执⾏ nice -n <nice_value> initlog -q -c "runuser -s /bin/bash - <user> -c "$*" fi

[ "$?" -eq 0 ] & suces $"$base startup"| failure $"$base startup" # 如果上⾯的命令 成功，则显示⼀个绿⾊的 [ OK ] ，否则显示 [ FAILURE ] }

# #

# A function to stop a program. # 下⾯定义另外⼀个很重要的函数 kilproc ，/etc/init.d/ 下⾯的脚本的 stop 部分都会⽤到它 kilproc() {

RC=0 # RC 是最终返回的值，初始化为 0 # Test syntax. if [ "$#" -eq 0 ]; then # kilproc 函数的语法格式是 kilproc <service>

[<signal>] ，例如 kilproc sm-client 9 echo $"Usage: kilproc {program} [signal]" return 1

fi notset=0 # noset 是⽤于检查⽤户是否指定了 kil 要使⽤的信 号 # check for second arg to be kil level

if [ -n "$2" ]; then # 如果 $2 不为空，则表示⽤户有设定信号， 则 kilevel=$2 # 把 $2 的值赋予 kilevel 变 量 else # 否 则 notset=1 # notset 变量的值为1，同时 kilevel 为 '-9' （KI L 信号） kilevel="-9"

fi # 补充 ：注意，并不是说⽤户没有指定信号地停⽌某项服务时，就会⽴即⽤ kil -9 这样的⽅式强制杀 死，⽽是先⽤ TERM 信号，然后再⽤ KI L # Save basename.

base=${1#*/} # basename 就是得出服务的名称 # Find pid. pid= # 把 pid 变量的值清空。注意，不是指 pid 变量的值等于 下⾯脚本的执⾏结果，要看清楚 if [ -f /var/run/${base}.pid ]; then # 下⾯和上⾯的 daemon 函数⼀样找出 pid local line p

read line < /var/run/${base}.pid

for p in $line ; do

[ -z "${p/[0-9]/}" -a -d "/proc/$p" ] & pid="$pid $p" done

fi if [ -z "$pid" ]; then # 不过和 daemon 不同的是，⼀旦 pid 为空不会直接

return ⽽是尝试⽤ pid 命令再次查找 pid=`pidof -o $ -o $PID -o %PID -x $1| / # -o 是⽤ 于忽略某个 pid ，-o $ 是忽略当前 shel 的 pid、-o $PID 是忽略 shel 的 pid pidof -o $ -o $PID -o %PID -x $base` # -o %PID 是忽略 pidof 命令的⽗进程，要查询的进程是 $1 (fulpath) 或者 $base

fi # Kil it. if [ -n "${pid:-}" ] ; then # 如果 pid 的值最终不为空，则

[ "$BOTUP" = "verbose" -a -z "$LSB" ] & echo -n "$base " # 且 BOTUP 的值为 verbose ，且 LSB 变量不为空，则打印⼀个服务名

if [ "$notset" -eq "1" ] ; then # 如果 notset 变量不为1，表示⽤户没有指定 信号，则 if checkpid $pid 2>&1; then # 调⽤ checkpid $pid 检查是否在 /proc/ 下存在进程⽬录，如果有 # TERM first, then KI L if not dead # 先尝试 ⽤ TERM 信息，不⾏再⽤ KI L 信号 kil -TERM $pid >/dev/nul 2>&1 # 执⾏ kil -TERM $pid usl ep 1 0 # usl ep 和 sl ep ⼀样，不 过单位是百万分之1秒。这⾥休眠1秒

if checkpid $pid & sl ep 1 & # 如果 checkpid $pid 还是查到有 /proc/<pid>/ ⽬录存在，则表示还没有杀死，继续等待1秒 checkpid $pid & sl ep 3

& # 如果1秒后⽤ checkpid 检查还是有，则再等待3秒； checkpid $pid ; then # 如果还是没有杀死，则⽤ KI L 信号

kil -KI L $pid >/dev/nul 2>&1 # 执⾏ kil -KI L 杀死

它 usl ep 1 0 # 等待1秒种 fi fi checkpid $pid # 再次检查 pid ⽬录 RC=$? # 并把结果返回给 RC ，这就算是 kilproc 的最后

状态了 [ "$RC" -eq 0 ] & failure $"$base shutdown"| suces $"$base shutdown" # 如果 RC 的值为0，则表示kil -9 没有杀死了进程，则调⽤ failure 函数，否则调⽤ suces RC=$(! $RC)

# use specified level only # 上⾯都是在没有指定信号的情况的，下⾯是⽤ 户指定了信号的。例如 restart）或者 reload）部分

else # 这个 else 是针对 if [ "$notset" -eq "1" ] 的 if checkpid $pid; then # 如果检查到进程存在，则 kil $kilevel $pid >/dev/nul 2>&1 # 执⾏kil命令，但使⽤指定的信号 $kilevel RC=$? # 并把状态值返回给变量 RC

[ "$RC" -eq 0 ] & suces $"$base $kilevel"| failure $"$base $kilevel" # 如果 RC 为0 则表示成功，调⽤ suces；否则调⽤ failure 函数 fi

fi else # 这个 else 是针对 if [ -n "${pid:-}" ] 的，也就是说没有 pid ⽂件，pidof 命令也没有找到 pid

，则 failure $"$base shutdown" # 调⽤ failure 函数，表示停⽌服务失败 RC=1 # 同时 RC 的值为1 fi # Remove pid file if any. # 根据具

体情况可能需要删除 pid ⽂件 if [ "$notset" = "1" ]; then # 如果 notset 不为1 ，也就是⽤ 户没有指定信号的情况 rm -f /var/run/$base.pid # ⾃动删除 /var/run 下的 pid ⽂件 fi

return $RC # 并把 RC 作为 exit status 返回} # 补充 ：⾃所以删除 pid ⽂件只针对 notset 为1 的情况，是因为 -HUP 信号（重读配置），并不杀死 进程，所以不能删除它的 pid ⽂件 # 例如下⾯ ：[rot@mail init.d]# ps -ef |grep xinetd rot 2635 1 0 12 25 ? 0  0  0 xinetd -stayalive -pidfile /var/run/xinetd.pid [rot@mail init.d]# ./xinetd reload Reloading configuration: [ OK ] [rot@mail init.d]# ps -ef |grep xinetd rot 2635 1 0 12 25 ? 0  0  0 xinetd -stayalive -pidfile /var/run/xinetd.pid rot 3927 3412 0 16 43 pts/0 0  0  0 grep xinetd [rot@mail init.d]# 可以看到 pid 在 reload 后并没有变

# #

# A function to find the pid of a program. Loks *only* at the pidfile # 下⾯的 pidfileofproc 函数和 checkpid 类似，但不执⾏ pidof 命令，只查询 pid ⽂件 pidfileofproc() {

local base=${1#*/}

# Test syntax. if [ "$#" = 0 ] ; then

echo $"Usage: pidfileofproc {program}" return 1

fi # First try "/var/run/*.pid" files if [ -f /var/run/$base.pid ] ; then

local line p pid= read line < /var/run/$base.pid for p in $line ; do

[ -z "${p/[0-9]/}" -a -d /proc/$p ] & pid="$pid $p" done

if [ -n "$pid" ]; then echo $pid return 0

fi fi

}

# #

# A function to find the pid of a program. # 下⾯的 pidofproc 函数和上⾯的 pidfileofproc 函数类似，但多了⼀步 pidof 命令 pidofproc() {

base=${1#*/} # Test syntax. if [ "$#" = 0 ]; then

echo $"Usage: pidofproc {program}" return 1

fi # First try "/var/run/*.pid" files if [ -f /var/run/$base.pid ]; then

local line p pid= read line < /var/run/$base.pid for p in $line ; do

[ -z "${p/[0-9]/}" -a -d /proc/$p ] & pid="$pid $p" done

if [ -n "$pid" ]; then echo $pid return 0

fi

fi pidof -o $ -o $PID -o %PID -x $1| /

pidof -o $ -o $PID -o %PID -x $base }

# #

#status() { # 注释 ：下⾯的 status 函数是判断服务的 状态，总共有4种 local base=${1#*/}

local pid # Test syntax.

if [ "$#" = 0 ] ; then echo $"Usage: status {program}" return 1

fi # First try "pidof" # 同样是查找 pid 先。直接使⽤ pidof 命 令 pid=`pidof -o $ -o $PID -o %PID -x $1| /

pidof -o $ -o $PID -o %PID -x ${base}`

if [ -n "$pid" ]; then # 如果 pid 变量的值不为空，则表示找到进 程， echo $"${base} (pid $pid) is runing." # 则打印 " x (pid n) is runing " , return 0 # 并返回 0 fi # Next try "/var/run/*.pid" files # 如果 pidof 命令没有找到，则尝试从 pid ⽂件找 if [ -f /var/run/${base}.pid ] ; then

read pid < /var/run/${base}.pid if [ -n "$pid" ]; then # 如果 pidof 命令找不到，但从 pid ⽂件找到了

pid ，则 echo $"${base} dead but pid file exists" # 打印 " x dead but pid file exists"， return 1 # 并返回 1 fi

fi # Se if /var/lock/subsys/${base} exists # 如果 pidof 命令和 pid ⽂件都没有找到 pid

，则 if [ -f /var/lock/subsys/${base} ]; then # 如果在 /var/lock/subsys 下存在对应的 ⽂件，则 echo $"${base} dead but subsys locked" # 打印 “ x dead but subsys locked”， return 2 # 并返回 2 fi

echo $"${base} is stoped" # 如果 pidof 命令、pidf ⽂件都没有找到pid ，且没有别锁，则打印 “ x is stoped”

return 3 # 并返回3}

# #

# # 注释 ：下⾯的 echo_ x 函数就是真正在屏幕上打印 [ ok ] 、[ PASED ]、[ FAILURE ]、[ WARNING ] 的部分了echo_suces() { # 下⾯是 echo_suces 部分

[ "$BOTUP" = "color" ] & $MOVE_TO_COL # ⾸先是打印 “[” 之前的空格 echo -

- n "[ " # 然后打印 "[" [ "$BOTUP" = "color" ] & $SETCOLOR_SUCES # 设置字体为红⾊ echo -n $"OK" # 打印 OK [ "$BOTUP" = "color" ] & $SETCOLOR_NORMAL # 返回字体为⽩⾊ echo -n " ]" # 打印 "]"


echo -ne "/r" # 换⾏。

- return 0 # 返回 0，其他⼀律返回 1echo_failure() {


- [ "$BOTUP" = "color" ] & $MOVE_TO_COL echo -n "[" [ "$BOTUP" = "color" ] & $SETCOLOR_FAILURE echo -n $"FAILED" [ "$BOTUP" = "color" ] & $SETCOLOR_NORMAL echo -n "]" echo -ne "/r"
- return 1


}echo_pased() { [ "$BOTUP" = "color" ] & $MOVE_TO_COL echo -n "[" [ "$BOTUP" = "color" ] & $SETCOLOR_WARNING echo -n $"PASED" [ "$BOTUP" = "color" ] & $SETCOLOR_NORMAL echo -n "]" echo -ne "/r" return 1

}echo_warning() { [ "$BOTUP" = "color" ] & $MOVE_TO_COL echo -n "[" [ "$BOTUP" = "color" ] & $SETCOLOR_WARNING echo -n $"WARNING" [ "$BOTUP" = "color" ] & $SETCOLOR_NORMAL echo -n "]" echo -ne "/r" return 1

}

# #

# Inform the graphical bot of our curent state update_bot_stage() {

if [ "$GRAPHICAL" = "yes" -a -x /usr/bin/rhgb-client ]; then /usr/bin/rhgb-client -update="$1"

fi return 0

}

# #

# Log that something suceded suces() { # suces 函数除了打印 [ x ] 之外，还会使⽤ initlog 记录信息 if [ -z "${IN_INITLOG:-}" ]; then

initlog $INITLOG_ARGS -n $0 -s "$1" -e 1 # -n 是 -name 的意

思，-s 是 -string ，-e 是 -event ，1 表示完全成功 else # sily hack to avoid EPIPE kiling rc.sysinit trap" SIGPIPE echo "$INITLOG_ARGS -n $0 -s /"$1/" -e 1" >&21 trap - SIGPIPE

fi [ "$BOTUP" != "verbose" -a -z "$LSB" ] & echo_suces return 0

}# Log that something failed failure() { rc=$? if [ -z "${IN_INITLOG:-}" ]; then

initlog $INITLOG_ARGS -n $0 -s "$1" -e 2 # failure 的话 -event 是

- 2 是失败 else trap" SIGPIPE echo "$INITLOG_ARGS -n $0 -s /"$1/" -e 2" >&21 trap - SIGPIPE


fi [ "$BOTUP" != "verbose" -a -z "$LSB" ] & echo_failure [ -x /usr/bin/rhgb-client ] & /usr/bin/rhgb-client -details=yes return $rc

}# Log that something pased, but may have had erors. Useful for fsck pased() {

rc=$? if [ -z "${IN_INITLOG:-}" ]; then

initlog $INITLOG_ARGS -n $0 -s "$1" -e 1 # pased 的话 -event 还 是1

else trap" SIGPIPE echo "$INITLOG_ARGS -n $0 -s /"$1/" -e 1" >&21 trap - SIGPIPE

fi [ "$BOTUP" != "verbose" -a -z "$LSB" ] & echo_pased return $rc

} # Log a warning warning() {

rc=$? if [ -z "${IN_INITLOG:-}" ]; then

initlog $INITLOG_ARGS -n $0 -s "$1" -e 1 # warning 的话 -event 也

是 1 else trap" SIGPIPE echo "$INITLOG_ARGS -n $0 -s /"$1/" -e 1" >&21 trap - SIGPIPE

fi [ "$BOTUP" != "verbose" -a -z "$LSB" ] & echo_warning return $rc

}

# #

# Run some action. Log its output. # action 函数是另外 ⼀个最重要的函数，它的作⽤是打印某个提示信息并执⾏给定命令 tion() {

STRING=$1 echo -n "$STRING " if [ "${RHGB_STARTED}" !=" -a -w /etc/rhgb/temp/rhgb-console ]; then

echo -n "$STRING " > /etc/rhgb/temp/rhgb-console

fi shift initlog $INITLOG_ARGS -c "$*" & suces $"$STRING"| failure $"$STRING" rc=$? echo if [ "${RHGB_STARTED}" !=" -a -w /etc/rhgb/temp/rhgb-console ]; then

if [ "$rc" = "0" ]; then

echo_suces > /etc/rhgb/temp/rhgb-console else

echo_failed > /etc/rhgb/temp/rhgb-console [ -x /usr/bin/rhgb-client ] & /usr/bin/rhgb-client -details=yes fi

echo

fi return $rc

}

# #

# returns OK if $1 contains $2 # strstr 函数是判断 $1 字符串是否含有 $2 字 符串，是则返回0，否则返回1 () {

[ "${1#*$2*}" = "$1" ] & return 1

- return 0

}

# #

# Confirm whether we realy want to run this service # confirm 函 数是⽤于交互式的启动服务 nfirm() {

[ -x /usr/bin/rhgb-client ] & /usr/bin/rhgb-client -details=yes while : ; do

echo -n $"Start service $1 (Y)es/(N)o/(C)ontinue? [Y] " # 会打印⼀个提示信 息 read answer

if strstr $"yY" "$answer"| [ "$answer" =" ] ; then # 如果 answer 变量是 y 或者 Y 则 return 0 # 返回 0 （但未真正启动） elif strstr $"cC" "$answer" ; then # 如果 answer 是 c 或者 C ，则 rm f /var/run/confirm # 删除 /var/run/confirm ⽂件

[ -x /usr/bin/rhgb-client ] & /usr/bin/rhgb-client -details=no return 2 # 返回2 elif strstr

$"nN" "$answer" ; then # 如果 answer 是 n 或者 N，则

- return 1 # 直接返回1 fi done


}

