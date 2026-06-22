基本格式 :

* * * * * comand 分 时 ⽇ ⽉ 周 命令

- 第1列表示分钟1～59 每分钟⽤*或者 */1表示
- 第2列表示⼩时1～23（0表示0点）
- 第3列表示⽇期1～31
- 第4列表示⽉份1～12
- 第5列标识号星期0～6（0表示星期天）
- 第6列要运⾏的命令 crontab⽂件的⼀些例⼦： 30 21 * * * /usr/local/etc/rc.d/lightpd restart 上⾯的例⼦表示每晚的21:30重启apache。 45 4 1,10,2 * * /usr/local/etc/rc.d/lightpd restart 上⾯的例⼦表示每⽉1、10、 2⽇的4 : 45重启apache。 10 1 * * 6,0 /usr/local/etc/rc.d/lightpd restart 上⾯的例⼦表示每周六、周⽇的1 : 10重启apache。 0,30 18-23 * * * /usr/local/etc/rc.d/lightpd restart 上⾯的例⼦表示在每天18 : 0⾄23 : 0之间每隔30分钟重启apache。


- 0 23 * * 6 /usr/local/etc/rc.d/lightpd restart 上⾯的例⼦表示每星期六的 1 : 0 pm重启apache。

- * */1 * * * /usr/local/etc/rc.d/lightpd restart 每⼀⼩时重启apache
- * 23-7/1 * * * /usr/local/etc/rc.d/lightpd restart 晚上 1点到早上7点之间，每隔⼀⼩时重启apache


- 01 4 * mon-wed /usr/local/etc/rc.d/lightpd restart 每⽉的4号与每周⼀到周三的 1点重启apache 0 4 1 jan * /usr/local/etc/rc.d/lightpd restart ⼀⽉⼀号的4点重启apache 名称 : crontab 使⽤权限 : 所有使⽤者 使⽤⽅式 : crontab file [-u user]-⽤指定的⽂件替代⽬前的crontab。 crontab-[-u user]-⽤标准输⼊替代⽬前的crontab. crontab-1[user]-列出⽤户⽬前的crontab. crontab-e[user]-编辑⽤户⽬前的crontab. crontab-d[user]-删除⽤户⽬前的crontab. crontab-c dir- 指定crontab的⽬录。


crontab⽂件的格式：M H D m d cmd. M: 分钟（0-59）。 H：⼩时（0-23）。 D：天（1-31）。 m: ⽉（1-12）。 d: ⼀星期内的天（0~6，0为星期天）。 cmd要运⾏的程序，程序被送⼊sh执⾏，这个shel只有USER,HOME,SHEL这三个环境变量 说明 : crontab 是⽤来让使⽤者在固定时间或固定间隔执⾏程序之⽤，换句话说，也就是类似使⽤者的时程 表。-u user 是指设定指定 user 的时程表，这个前提是你必须要有其权限(⽐如说是 rot)才能够指定他⼈的时程表。如果不使⽤ u user 的话，就是表示设 定⾃⼰的时程表。 参数 : crontab -e : 执⾏⽂字编辑器来设定时程表，内定的⽂字编辑器是 VI，如果你想⽤别的⽂字编辑器，则 请先设定 VISUAL 环境变数 来指定使⽤那个⽂字编辑器(⽐如说 setenv VISUAL joe) crontab -r : 删除⽬前的时程表 crontab -l : 列出⽬前的时程表 crontab file [-u user]-⽤指定的⽂件替代⽬前的crontab。 时程表的格式如下 : f1 f2 f3 f4 f5 program 其中 f1 是表示分钟，f2 表示⼩时，f3 表示⼀个⽉份中的第⼏⽇，f4 表示⽉份，f5 表示⼀个星期中的 第⼏天。program 表示要执 ⾏的程序。 当 f1 为 * 时表示每分钟都要执⾏ program，f2 为 * 时表示每⼩时都要执⾏程序，其馀类推 当 f1 为 a-b 时表示从第 a 分钟到第 b 分钟这段时间内要执⾏，f2 为 a-b 时表示从第 a 到第 b ⼩时都 要执⾏，其馀类推 当 f1 为 */n 时表示每 n 分钟个时间间隔执⾏⼀次，f2 为 */n 表示每 n ⼩时个时间间隔执⾏⼀次，其馀 类推 当 f1 为 a, b, c,. 时表示第 a, b, c,. 分钟要执⾏，f2 为 a, b, c,. 时表示第 a, b, c.个⼩时要执⾏，其 馀类推 使⽤者也可以将所有的设定先存放在档案 file 中，⽤ crontab file 的⽅式来设定时程表。 例⼦ : #每天早上7点执⾏⼀次 /bin/ls : 0 7 * * * /bin/ls 在 12 ⽉内, 每天的早上 6 点到 12 点中，每隔3个⼩时执⾏⼀次 /usr/bin/backup :

0 6-12/3 * 12 * /usr/bin/backup 周⼀到周五每天下午 5  0 寄⼀封信给 alex@domain.name : 0 17 * * 1-5 mail -s "hi" alex@domain.name < /tmp/maildata 每⽉每天的午夜 0 点 20 分, 2 点 20 分, 4 点 20 分 .执⾏ echo "haha" 20 0-23/2 * * * echo "haha" 注意 : 当程序在你所指定的时间执⾏后，系统会寄⼀封信给你，显示该程序执⾏的内容，若是你不希望收到 这样的信，请在每⼀⾏空⼀格之 后加上 > /dev/nul 2>&1 即可 例⼦2 : #每天早上6点10分 10 6 * * * date #每两个⼩时 0 */2 * * * date #晚上 1点到早上8点之间每两个⼩时，早上8点

- 0 23-7/2，8 * * * date #每个⽉的4号和每个礼拜的礼拜⼀到礼拜三的早上 1点
- 01 4 * mon-wed date #1⽉份⽇早上4点 0 4 1 jan * date 范例 $crontab -l 列出⽤户⽬前的crontab.


