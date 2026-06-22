shell中可能经常能看到：>/dev/null 2>&1

命令的结果可以通过%>的形式来定义输出

/dev/null 代表空设备⽂件 > 代表重定向到哪⾥，例如：echo "123" > /home/123.txt

- 1 表示stdout标准输出，系统默认值是1，所以">/dev/null"等同于"1>/dev/null"

- 2 表示stderr标准错误 & 表示等同于的意思，2>&1，表示2的输出重定向等同于1


那么本⽂标题的语句：

- 1>/dev/null ⾸先表示标准输出重定向到空设备⽂件，也就是不输出任何信息到终端，说⽩了就是不显 示任何信息。

- 2>&1 接着，标准错误输出重定向等同于 标准输出，因为之前标准输出已经重定向到了空设备⽂件， 所以标准错误输出也重定向到空设备⽂件。


- A. 1> /dev/null 表示将命令的标准输出重定向到 /dev/null2>/dev/null 表示将命令的错误输出重定向到 /dev/null1 - denotes stdout ( standard output )2 - denotes stderr ( standard error )/dev/null就相当与 windows⾥的回收站，只是进去了不能再出来了。>/dev/null 就是将标准输出和标准出错的信息屏蔽不 显示

- B.>/dev/null 2>&1 also can write as 1>/dev/null 2>&1 - stdout redirect to /dev/null (no stdout) ,and redirect stderr to stdout (stderr gone as well) . end up it turns both stderr and stdout off

- C.a little practice may help to undstand above . #ls /usr /nothing #ls /usr /nothing 2>/dev/null #ls /usr /nothing >/dev/null 2>&1


我们经常会在UNIX系统下的⼀些脚本中看到类似”2>&1″这样的⽤法，例如“/path/to/prog 2>&1 > /dev/null &”，那么它的具体含义是什么呢？

UNIX有⼏种输⼊输出流，它们分别与⼏个数字有如下的对应关系：0-标准输⼊流(stdin)，1-标准 输出流(stdout)，2-标准错误流 (stderr)。”2>&1″的意思就是将stderr重定向⾄stdout，并⼀起在屏幕上 显示出来。如果不加数字，那么默认的重定向动作是针对stdout(1)的，⽐如”ls -l > result”就等价于”ls -l

- 1 > result”。这样便于我们更普遍性的理解重定向过程。 下⾯举例说明：


#cat std.sh #!/bin/sh echo “stdout” echo “stderr” >&2

#/bin/sh std.sh 2>&1 > /dev/null

stderr

#/bin/sh std.sh > /dev/null 2>&1

第⼀条命令的输出结果是stderr，因为stdout和stderr合并后⼀同重定向到/dev/null，但stderr并未 被清除，因此仍将在屏幕中显示出来；第⼆条命令⽆输出，因为当stdout重定向⾄/dev/null后，stderr ⼜重定向到了stdout，这样stderr也被输出到了/dev/null。

今天在做例⾏⼯作的时候，发现机器上的sendmail进程奇多⽆⽐，并且机器IO好像也很慢。后来发现 在/var/spool/clientmqueue⽬录下ls⼏乎要死⼈ – 最少有10万个⽂件

ps|grep sendmail看这些sendmail进程⾥⾯都有/var/spool/clientmqueue

cd过去随便打开了个⽂件看了下，发现是我crontab⾥⾯执⾏的程序的exception，估计是我的crontab 每次执⾏，linux都试图发邮件给crontab的⽤户但是⼜没有配sendmail，所以东⻄就都被扔 到/var/spool/clientmqueue下⾯了。然后我才明⽩为啥以前别⼈写的crontab要加上> /dev/null 2>&1， 原来这样就不会每次执⾏crontab都把结果或者excetion发邮件了。

把这10万个⽂件删掉后，⼀切恢复正常

问题现象: linux操作系统中的/var/spool/clientmqueue/⽬录下存在⼤量⽂件。 原因分析：系统中有⽤户开启了cron，⽽cron中执⾏的程序有输出内容，输出内容会以邮件形式发给 cron的⽤户，⽽sendmail没有启动所以就产⽣了这些⽂件； 解决办法: 1、 将crontab⾥⾯的命令后⾯加上> /dev/null 2>&1

- 2、知识点：

2>：重定向错误。 2>&1：把错误重定向到输出要送到的地⽅。即把上述命令的执⾏结果重定向到/dev/null，即抛弃，同 时，把产⽣的错误也抛弃。

- 3、具体代码：


- （1）、# crontab -u cvsroot -l

- 01 01 * * * /opt/bak/backup

- 01 02 * * * /opt/bak/backup2


- （2）、# vi /opt/bak/backup #!/bin/sh cd / getfacl -R repository > /opt/bak/backup.acl


- （3）、# vi /opt/bak/backup2 #!/bin/sh week=`date +%w` tar zcvfp /opt/bak/cvs$week/cvs.tar.gz /repository >/dev/null 2>&1

- 4、清除/var/spool/clientmqueue/⽬录下的⽂件： # cd /var/spool/clientmqueue # rm -rf * 如果⽂件太多，占⽤空间太⼤，⽤上⾯命令删除慢的话，就执⾏下⾯的命令： # cd /var/spool/clientmqueue # ls | xargs rm -f 在⼀個⾵和⽇麗的夜晚，我坐在家裡看著電視，後來⼿機⼀陣響起，結果是楊⽼師發現⼀台主機發⽣ 異常，伺服器的 /var/spool/mqueue ⽬錄被塞了⼀堆還沒有寄出的信件，⽽當時沒有把 /var/spool 另外 分割出來，所以也影響到了系統 root (/) 區塊，只剩六百多 MB 可以使⽤，這時⼀想會有幾個可能.


這台 server 有幫學校的 PC 做寄送信件，所以可能是廣告信在寄出.

使⽤這台 server 做 mail 寄信的機器，可能是中毒，於是就不斷的送信出去. ⼀開始只有想到這兩個原因，但是可要把被吞掉的空間給吐出來，所以就打算把所有的 mail queue 都 先砍了，當然，要先停掉 mail service. 在砍這些正在排隊的信件時，發現⼀件事，就是裡⾯的檔案太多了，使⽤ ls 命令就變得超級遲頓，沒 有反應，使⽤ mailq 來看看到底是那些信被 queue 住也沒辦法，後來想想算了，只好全剖砍了，不要 再玩下去，之後，很順⼿的下了 rm -rf * 這下⼦呢，發⽣了⼀件很離奇的事，居然檔案太多無法刪除， 第⼀次聽到 rm 在 complain (我是聽到的，楊⽼師是實作者，所以他有看到 ^^). 那個 error 是: bash: /bin/rm: Argument list too long 雖然無法刪除，但是楊兄並不放棄，到主機⾯前，開啟了 X Window 之後使⽤那 Linuxer 最常使⽤的 鸚鵡螺 (nautilus) 開啟到 /var/spool/mqueue. 喔 ~ 可以使⽤ X Window 來刪呢 ! 後來想說即然 X Window 有這麼⼤的本事，那麼就⽤它來刪了其它的 queue files 就好啦，於是掛上電話，放楊兄⼀個 ⼈努⼒的在機房刪著 ... 當然我也沒有閒著，電視劇剛好演完，於是開啟我的⼯作伙伴，再度當網路潛⽔艇 ... 游著游著，突然 想到，何不使⽤ find 來刪除看看 ? 於是刪回歷史⽂件，發現⼀個命令就是 find ./ | xargs rm -rf 千萬別 ⼩看這⼩⼩的指令，因為在我看完之後不久，楊兄打進來，說已經刪到⼿軟，這時也是晚上⼗點了， 於是我就推薦了這個這道指令，嗯，很好，全都刪了，還頗快的 ... 喔，還沒說為什麼會刪到⼿軟，是因為 nautilus 在 Load ⽬錄時，是分批的，不是⼀次全部讀，所以⼀ 次⼤約是幾千封在讀，刪了之後，沒想到⼜冒出了還有幾千封 ... 真是嚇死⼈，後來推論應該是分批的 關係. 在下了 find ./ | xargs rm -rf 之後，還在訝異快速之餘，就發現時間不多了，學校也要關⾨，所以就先 say bye bye，在現場苦命的楊兄也回家休息了.

分析: rm 有最⼤⼀次刪除的數量，所以當⼀個⽬錄裡有太多的檔案或⽬錄時，就會出現錯誤，⼩弟試過應該 是在⼆萬以下，⽽使⽤ find ./ | xargs rm -rf 的⽬的是先使⽤ find 列出檔案，再導向到 xargs，xargs 再 喂給 rm，在這裡，xargs 會分批依照 rm 的最⼤數量餵給 rm，然後就可以順利刪除檔案了 。⽽真正的原因，有可能是 rm 的版本或是檔案系統的問題，我也不再繼續追就，反正能辦好事就好

![image 1](<null 2_&1 的作用 .note_images/imageFile1.png>)

下⾯提供當時⼩弟測試的⼀個⼩⼩ shell script 下載： mk-file.sh (這個 shell script 會有⽬錄下產⽣ 20000 個檔案。) 接下來來做個⼩⼩測試： root # mkfile.sh root # 會產⽣ 20000 個⼩檔案，名稱為 test-file-{1~19999} 直接使⽤ rm 去刪除： root # rm -rf test-file-*

- -bash: /bin/rm: Argument list too long (會回應引數過⻑的訊息) 改搭配 find 來刪除 root # find ./ -iname 'test-file-*' | xargs rm -rf root # ls mk-file.sh root # 這樣就順利被刪除了。

- --------------------------------#tool_action


- 45 4 * * * /bin/sh /data/stat/crontab/exec_tool_action_analysis_db.sh >> /data/stat/logs/exec_tool_action_analysis_db.sh.log > /dev/null 2>&1

- 45 5 * * * /bin/sh /data/stat/crontab/exec_tool_action_analysis_user.sh >> /data/stat/logs/exec_tool_action_analysis_user.sh.log > /dev/null 2>&1


否则在/var/spool/clientmqueue 下会产⽣以下⽂件：

-rw-rw---- 1 smmsp smmsp 975 Jan 17 10:50 qfq0H2o4ei031197

