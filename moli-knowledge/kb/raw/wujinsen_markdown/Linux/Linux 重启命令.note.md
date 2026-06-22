⼀、Linux 的五个重启命令

- 1、shutdown

- 2、poweroff

- 3、init

- 4、reboot

- 5、halt ⼆、五个重启命令的具体说明 shutdown reboot 在linux下⼀些常⽤的关机/重启命令有shutdown、halt、reboot、及init，它们都可以达到重启


系统的⽬的，但每个命令的内部⼯作过程是不同的，通过本⽂的介绍，希望你可以更加灵活的运⽤各 种关机命令。

1.shutdown shutdown命令安全地将系统关机。 有些⽤户会使⽤直接断掉电源的⽅式来关闭linux，这是⼗分 危险的。因为linux与windows不同，其后台运⾏着许多进程，所以强制关机可能会导致进程的数据丢 失﹐使系统处于不稳定的状态﹐甚⾄在有的系统中会损坏硬件设备。⽽在系统关机前使⽤shutdown命 令﹐系统管理员会通知所有登录的⽤户系统将要关闭。并且login指令会被冻结﹐即新的⽤户不能再登 录。直接关机或者延迟⼀定的时间才关机都是可能的﹐还可能重启。这是由所有进程〔process〕都会 收到系统所送达的信号〔signal〕

决定的。这让像vi之类的程序有时间储存⽬前正在编辑的⽂档﹐⽽像处理邮件〔mail〕和新闻

〔news〕的程序则可以正常地离开等等。 shutdown执⾏它的⼯作是送信号〔signal〕给init程序﹐要求它改变runlevel。 Runlevel 0被⽤来停机〔halt〕﹐runlevel 6是⽤来重新激活〔reboot〕系统﹐⽽runlevel 1则

是被⽤来让系统进⼊管理⼯作可以进⾏的状态﹔这是预设的﹐假定没有-h也没有-r参数给shutdown。 要想了解在停机〔halt〕或者重新开机〔reboot〕过程中做了哪些动作﹐你可以在这个⽂ 件/etc/inittab⾥看到这些runlevels相关的资料。

shutdown 参数说明: [-t] 在改变到其它runlevel之前﹐告诉init多久以后关机。 [-r] 重启计算器。 [-k] 并不真正关机﹐只是送警告信号给 每位登录者〔login〕。 [-h] 关机后关闭电源〔halt〕。 [-n] 不⽤init﹐⽽是⾃⼰来关机。不⿎励使⽤这个选项﹐⽽且该选项所产⽣的后果往往不总是你

所预期得到的。

[-c] cancel current process取消⽬前正在执⾏的关机程序。所以这个选项当然没有时间参数﹐ 但是可以输⼊⼀个⽤来解释的讯息﹐⽽这信息将会送到每位使⽤者。

[-f] 在重启计算器〔reboot〕时忽略fsck。

[-F] 在重启计算器〔reboot〕时强迫fsck。 [-time] 设定关机〔shutdown〕前的时间。 2.halt----最简单的关机命令 其实halt就是调⽤shutdown -h。halt执⾏时﹐杀死应⽤进程﹐执⾏sync系统调⽤﹐⽂件系统写

操作完成后就会停⽌内核。 参数说明: [-n] 防⽌sync系统调⽤﹐它⽤在⽤fsck修补根分区之后﹐以阻⽌内核⽤⽼版本的超级块

〔superblock〕覆盖修补过的超级块。 [-w] 并不是真正的重启或关机﹐只是写 wtmp〔/var/log/wtmp〕纪录。 [-d] 不写wtmp纪录〔已包含在选项[-n]中〕。 [-f] 没有调⽤shutdown⽽强制关机或重启。 [-i] 关机〔或重启〕前﹐关掉所有的⽹络接⼝。 [-p] 该选项为缺省选项。就是关机时调⽤poweroff。 3.reboot reboot的⼯作过程差不多跟halt⼀样﹐不过它是引发主机重启﹐⽽halt是关机。它 的参数与halt

相差不多。 4.init init是所有进程的祖先﹐它的进程号始终为1﹐所以发送TERM信号给init会终⽌所有的 ⽤户进程

﹑守护进程等。shutdown 就是使⽤这种机制。init定义了8个运⾏级别(runlevel)， init 0为关机﹐ init 1为重启。关于init可以⻓篇⼤论﹐这⾥就不再叙述。另外还有telinit命令可以改变init的运⾏级 别﹐⽐如﹐telinit -iS可使系统进⼊单⽤户模式﹐ 并且得不到使⽤shutdown时的信息和等待时间。

