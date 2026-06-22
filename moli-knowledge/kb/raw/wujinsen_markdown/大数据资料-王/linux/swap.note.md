# 简介

Linux中Swap（即：交换分区），类似于Windows的虚拟内存，就是当内存不⾜的时候，把⼀部分硬 盘空间虚拟成内存使⽤,从⽽解决内存容量不⾜的情况。Android是基于Linux的操作系统，所以也可以 使⽤Swap分区来提升系统运⾏效率[1] 。 交换分区，英⽂的说法是swap，意思是“交换”、“实物交易”。它的功能就是在内存不够的情况下，操 作系统先把内存中暂时不⽤的数据，存到硬盘的交换空间，腾出内存来让别的程序运⾏，和Windows 的虚拟内存（pagefile.sys）的作⽤是⼀样的。 2SWAP分区分析 SWAP就是LINUX下的虚拟内存分区，它的作⽤是在物理内存使⽤完之后,将磁盘空间(也就是SWAP分 区)虚拟成内存来使⽤[1] 。 它和Windows系统的交换⽂件作⽤类似，但是它是⼀段连续的磁盘空间，并且对⽤户不可⻅。 需要注意的是,虽然这个SWAP分区能够作为"虚拟"的内存,但它的速度⽐物理内存可是慢多了,因此如果 需要更快的速度的话,并不能寄厚望于SWAP,最好的办法仍然是加⼤物理内存。SWAP分区只是临时的 解决办法. 交换分区（swap）的合理值⼀般在内存的2倍左右？ ⼀种流⾏的、以讹传讹的说法是，安装Linux系统时，交换分区swap的⼤⼩应该是内存的两倍。也就是 说，如果内存是2G，那么就应该分出4G的硬盘空间作为交换空间。其实这是严重的浪费。真实的情况 是：可以根据你系统内存的⼤⼩，以及所使⽤的程序，⾃⾏决定交换分区的⼤⼩，甚⾄可以完全不⽤ 交换分区！

-

# 查看swap的空间⼤⼩

[jony@localhost~]$fre-m totalusedfresharedbuferscached Mem: 98972 053

-/+bufers/cache:9275 Swap:20157208Mem⾏显示了从系统⻆度看来内存使⽤的情况,total是系统可⽤的内存⼤⼩,数量上等 于系统物理内存减去内核保留的内存。

bufers和cached是系统⽤做缓冲的内存。bufers与某个块设备关联,包含了⽂件系统元数据,并且 跟踪了块的变化。cache只包含了⽂件本身。

-/+bufers/cache⾏则从⽤户⻆度显示内存信息,可⽤内存从数量上等于mem⾏used列值减去bufers和 cached内存的⼤⼩。 因为bufers和cached是操作系统为加快系统运⾏⽽设置的，当⽤户需要时,可以只接为⽤户使⽤。 Swap⾏便是系统的swap信息。 在⽇常应⽤中，通过上述命令看到交换空间的使⽤情况为0，那么你就不需要很⼤的虚拟内存，甚⾄可 以完全不需要另辟硬盘空间作为虚拟内存。那么，万⼀有⼀天你需要了呢，难道要重装系统？⼤可不 必，在Linux下虚拟内存不单可以放在单独的交换分区，也可以是⼀个在正常分区下的交换⽂件。

查看swap空间(file(s)/partition(s) 包括⽂件和分区的详细信息#swapon-s

等价于 #cat/proc/swap

[jony@localhost~]$cat/proc/swaps FilenameTypeSizeUsedPriority /dev/sda3partition206434126768-1 [jony@localhost~]$swapon-s FilenameTypeSizeUsedPriority /dev/sda3partition206434126768-1 swap越⼤越好吗？有⼈对Swap分区⼤⼩的设置这样评论过：“只要不怕浪费硬盘的话越⼤越好，因为 linux内核在物理内存完全⽤完之前不会去动swap” 不过根据我的经验，可能不是这样喔！太⼤的 swap 空间会造成 kernel 以为有巨⼤的内存空间⽽毫不节制的想要把数据捉进内存中，从⽽导致 kernel ⼀直 在做 memory swap，连带拖慢系统响应时间。 ⽼实说，1G RAM 如果不跑 p2p 之类的东⻄，那设个 256MB 就够⽤了；有 2G 的话连设都不要设。 另外如果说真的因为某⼀软件确实需要巨⼤内存空间才能运作的话，那只好在 swap 上动⼿脚，但为了 效能最好分散在多个实体硬盘上（等于类似 raid 效果）！ 其实如何设置Swap分区的⼤⼩是最能检查 ⼀个Linux系统管理员的⽔平的测试，Swap到底该如何设置呢？我是这样认为的：⾸先我们需要了解 这台服务器都要运⾏哪些程序、他们各⾃占⽤的内存⼤⼩为多少，经过确切的检查后，Swap分区的⼤ ⼩可以这样确定： ( 内存⼤⼩ + Swap分区⼤⼩ ) * 80%或70% = 程序需要占⽤总内存数 Swap分区在 程序测试期间也有很⼤的⽤途，例如管理员能够通过Swap分区的使⽤状况，监测系统内存是否出现泄 露，同时对Web项⽬等应⽤也可以提供⼀个⽐较好的流量峰值缓冲作⽤。⼀个Linux系统管理员要能够 通过监测Swap分区的使⽤情况，对系统、程序有⼀个合理的评价。

linux下调整swap空间

在SWAP空间不够⽤的情况下，如何⼿⼯添加SWAP空间 以下的操作都要在rot⽤户下进⾏，⾸先先 建⽴⼀个分区，采⽤ d命令⽐如 d if=/dev/zero of=/home/swap bs=1024 count=1024 0这样就会 创建/home/swap这么⼀个分区⽂件。⽂件的⼤⼩是1024 0个block，⼀般情况下1个block为1K，所 以这⾥空间是1024M。接着再把这个分区变成swap分区。/sbin/mkswap /home/swap再接着使⽤这个 swap分区。使其成为有效状态。/sbin/swapon /home/swap现在再⽤fre -m命令查看⼀下内存和swap 分区⼤⼩，就发现增加了1024M的空间了。不过当计算机重启了以后，发现swap还是原来那么⼤，新 的swap没有⾃动启动，还要⼿动启动。那我们需要修改/etc/fstab⽂件，增加如下⼀⾏/home/swap swap swap defaults 0 0你就会发现你的机器⾃动启动以后swap空间也增⼤了。 -

-⽅法⼀：如果磁盘有剩余的空 间，⽤分区⼯具新建⼀个swap分区.并写到/etc/fstab⾥⾯.再 #swapon -a⽅法⼆：可以⽤⼀个⽂件做交 换分区.1、建⽴swap⽂件，⽐如在/tmp下建⽴swapfre作为交换⽂件。建⽴#d if=/dev/zero of=swapfre bs=32k count=8192(bs=32k指定每个扇区占⽤32kb,读⼊了8192+0个区段,输出了 8192+0个区段) 注意:bs参数的⽬的在于指定每次读取及输⼊多少个bytes;由于磁盘存取的最⼩单位为 扇区,因此设置bs也等于设置每个扇区的⼤⼩;⽽count的⽬ 的则在指定可以使⽤多少个扇区.因此,可以 使⽤的硬盘空间就等于bs*count.以上范例为例,可以使⽤的硬盘空间等于32*8192=26214 (KB),亦等 于256MB. )执⾏上述命令后,会在/tmp⽬录中创建⼀个256MB的swapfre的⽂件 2、 格式华及启动 swap⽂件 接下来执⾏mkswap命令,将myswap⽂件格式化成s⽂件系统,系统才能使⽤,切换到/tmp⽬录, 并执⾏以下命令: # mkswap swapfre (#将⽂件格式化为swap⽂件格式) seting up swapspace version 1 , size = 26214 KB # swapon /tmp/swapfre (#启动swap分区) 要停⽌使⽤新创建的swap⽂ 件,只要执⾏ swapof /tmp/swapfre命令即可,如果swap交换⽂件不再使⽤，可以删除此⽂件。3、检 查swap#swapon -s4、 开机时⾃动启动新添加的swap分区 如果每次开机后都要执⾏swapon命令启动 swap分区或者⽂件,这太麻烦了.这时可以利⽤⽂字编辑器在/etc/fstab⽂件加⼀⾏,好让开机时⾃动启动 swap分区及⽂件: /dec/hdb5 swap swap defaults 0 0 (开机时启动此swap分区) /tmp/swapfre swap swap defaults 0 0 (开机时启动此swap⽂件) .swap空间⼤⼩：通常情况下，Swap空间应⼤于或等于物 理内存的⼤⼩，最⼩不应⼩于64M，通常Swap空间的⼤⼩应是物理内存的2-2.5倍。但根据不同的应 ⽤，应有 不同的配置：如果是⼩的桌⾯系统，则只需要较⼩的Swap空间，⽽⼤的服务器系统则视情况 不同需要不同⼤⼩的Swap空间。特别是数据库服务器和Web服 务器，随着访问量的增加，对Swap空 间的要求也会增加，具体配置参⻅各服务器产品的说明。swap数量：Swap分区的数量对性能也有很⼤ 的影响。因为Swap交换的操作是磁盘IO的操作，如果有多个Swap交换区，Swap空间的分配会以轮流 的⽅式操作于 所有的Swap，这样会⼤⼤均衡IO的负载，加快Swap交换的速度。如果只有⼀个交换 区，所有的交换操作会使交换区变得很忙，使系统⼤多数时间处于等待 状态，效率很低。⽤性能监视 ⼯具就会发现，此时的CPU并不很忙，⽽系统却慢。这说明，瓶颈在IO上，依靠提⾼CPU的速度是解 决不了问题的。 swapines

swapines的值的⼤⼩对如何使⽤swap分区是有着很⼤的联系的。swapines=0的时候表示最⼤限 度使⽤物理内存，然后才是 swap空间，swapines＝10的时候表示积极的使⽤swap分区，并且把 内存上的数据及时的搬运到swap空间⾥⾯。linux的基本默认设置为60，具体如下：

[rot@timeserver ~]# cat /proc/sys/vm/swapines60也就是说，你的内存在使⽤到10-60=40% 的时候，就开始出现有交换分区的使⽤。⼤家知道，内存的速度会⽐磁盘快很多，这样⼦会加⼤系统 io，同时造的成⼤量⻚的换进换出，严重影响系统的性能，所以我们在操作系统层⾯，要尽可能使⽤内 存，对该参数进⾏调整。临时调整的⽅法如下，我们调成10：[rot@timeserver ~]# sysctl vm.swapines=10vm.swapines = 10[rot@timeserver ~]# cat /proc/sys/vm/swapines10这只是 临时调整的⽅法，重启后会回到默认设置的要想永久调整的话，需要将需要在/etc/sysctl.conf修改， 加上：[rot@timeserver ~]# cat /etc/sysctl.conf# Controls the maximum number of shared memory segments, in pageskernel.shmal = 4294967296vm.swapines=10[rot@timeserver ~]# sysctl -p这样便完成修改设置！

