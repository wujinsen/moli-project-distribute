- （1） 重启后永久性⽣效： 开启：chkconﬁg iptables on 关闭：chkconﬁg iptables off
- （2） 即时⽣效，重启后失效： 开启：service iptables start 关闭：service iptables stop 需要说明的是对于Linux下的其它服务都可以⽤以上命令执⾏开启和关闭操作。 在开启了防⽕墙时，做如下设置，开启相关端⼜， 修改/etc/sysconﬁg/iptables ⽂件，添加以下内容：


- -A RH-Firewall-1-INPUT -m state ——state NEW -m tcp -p tcp ——dport 80 -j ACCEPT
- -A RH-Firewall-1-INPUT -m state ——state NEW -m tcp -p tcp ——dport 22 -j ACCEPT 或者： /etc/init.d/iptables status 会得到⼀系列信息，说明防⽕墙开着。 /etc/rc.d/init.d/iptables stop 关闭防⽕墙 最后： 在根⽤户下输⼊setup，进⼊⼀个图形界⾯，选择Firewall conﬁguration，进⼊下⼀界⾯，选择Security Level为Disabled，保存。重启即可。


====================================================== fedora下 /etc/init.d/iptables stop

======================================================= ubuntu下： 由于UBUNTU没有相关的直接命令 请⽤如下命令 iptables -P INPUT ACCEPT iptables -P OUTPUT ACCEPT 暂时开放所有端⼜ Ubuntu上没有关闭iptables的命令

======================================================= iptables 是linux下⼀款强⼤的防⽕墙，在不考虑效率的情况下，功能强⼤到⾜可以替代⼤多数硬件防⽕ 墙，但是强⼤的防⽕墙如果应⽤不当，可能挡住的可不光是那些潜在的攻击，还有可能是你⾃⼰哦。 这个带来的危害对于普通的个⼈PC来说可能⽆关紧要，但是想象⼀下，如果这是⼀台服务器，⼀旦发 ⽣这样的情况，不光是影院正常的服务，还需要到现场去恢复，这会给你带来多少损失呢？ 所以我想说的是，当你敲⼊每⼀个iptables 相关命令的时候都要万分⼩⼼。

- 1.应⽤每⼀个规则到DROP target时，都要仔细检查规则，应⽤之前要考虑他给你带来的影响。


- 2.在redhat中我们可以使⽤service iptables stop来关闭防⽕墙，但是在有些版本如ubuntu中这个命令却不 起作⽤，⼤家可能在⽹上搜索到不少⽂章告诉你⽤iptables -F这个命令来关闭防⽕墙，但是使⽤这个命 令前，千万记得⽤iptables -L查看⼀下你的系统中所有链的默认target，iptables -F这个命令只是清除所 有规则，只不会真正关闭iptables.想象⼀下，如果你的链默认target是DROP，本来你有规则来允许⼀些 特定的端⼜，但⼀旦应⽤iptables -L ，清除了所有规则以后，默认的target就会阻⽌任何访问，当然包 括远程ssh管理服务器的你。 所以我建议的关闭防⽕墙命令是 iptables -P INPUT ACCEPT iptables -P FORWARD ACCEPT iptables -P OUTPUT ACCEPT iptables -F 总之，当你要在你的服务器上做任何变更时，最好有⼀个测试环境做过充分的测试再应⽤到你的服务 器。除此之外，要⽤好iptables，那就要理解iptables的运⾏原理，知道对于每⼀个数据包iptables是怎么 样来处理的。这样才能准确地书写规则，避免带来不必要的⿇烦。


