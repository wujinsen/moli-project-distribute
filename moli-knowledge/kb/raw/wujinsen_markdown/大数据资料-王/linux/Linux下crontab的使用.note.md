编辑：crontab –u rot –e 列表：crontab –u rot –l 删除：crontab –u rot –r

每五分钟执⾏ */5 * * * * 每⼩时执⾏ 0 * * * * 每天执⾏ 0 0 * * * 每周执⾏ 0 0 * * 0 每⽉执⾏ 0 0 1 * * 每年执⾏ 0 0 1 1 *

*/1 * * * * ls >/tmp/ls.txt

htp:/yangqijun.iteye.com/blog/173016

详细⽤法可以参考该博⽂

- 1.作⽤ 使⽤crontab命令可以修改crontab配置⽂件，然后该配置由cron公⽤程序在适当的时间执⾏，该命令 使⽤权限是所有⽤户。
- 2.格式 crontab [-u user] {-l | -r | -e}
- 3.主要参数


- -e:执⾏⽂字编辑器来设定时程表，内空的⽂字编辑器是vi
- -r:删除⽬前的时程表
- -l列出⽬前的时程表。 Crontab⽂件的格式为”M H D m d cmd”。其中，M代表分钟（0~59）,H代表⼩时(0~23),D代表天 (1~31),m代表⽉(1~12)，d代表⼀星期内的天(0~6,0为星期天)。Cmd表示要运⾏的程序，它被送⼊sh 执⾏，这个shel只有USER、HOME、SHEL三个环境变量。 使⽤cron服务，⽤server crond status查看cron服务状态，如果没有启动则service crond start启动 它，cron服务是⼀个定时执⾏的服务，可以通过crontab命令添加或编辑需要定时执⾏的任务： crontab –u/设定某个⽤户的cron服务，⼀般rot⽤户在执⾏这个命令的时候需要此参数 crontab –l /列出某个⽤户cron服务的详细内容 crontab –r/删除某个⽤户的cron服务 crontab –e/编辑某个⽤户的cron服务 crontab filename/以filename作为crontab的任务列表⽂件并载⼊ ⽐如说rot查看⾃⼰的cron设置：crontab –u rot –l 再例如，rot想删除fred的cron设置：crontab –u fred –r 在编辑cron服务时，编辑的内容有⼀些格式和约定，输⼊：crontab –u rot –e


进⼊vi编辑模式，编辑的内容⼀定要符合下⾯的格式：

*/1 * * * * ls >/tmp/ls.txt 编辑/etc/crontab⽂件，在末尾加上⼀⾏：30 5 * * * rot init 6这样就将系统配置为每天早上5点30分 ⾃动重新启动。 crontab ⽂件中的⾏由6个字段组成，不同字段间⽤空格或tab键分隔。前5个字段指定命令要运⾏的时 间 分钟（0-59） ⼩时（0-23） ⽇期（1-31） ⽉份（1-12） 星期⼏（0-6，其中0代表星期⽇，好像7也代表星期⽇） 第6个字段是⼀个要在适当时间执⾏的字符串。 例⼦： #MIN HOUR DAY MONTH DAYOFWEK COMAND #每天早上6点10分 10 6 * * * date #每两个⼩时 0 */2 * * * date #晚上 1点到早上8点之间每两个⼩时，早上8点 0 23-7/2，8 * * * date #每个⽉的4号和每个礼拜⼀到礼拜三的早上 1点 0 1 4 * 1-3 date #1⽉1⽇早上4点 0 4 1 1 * date 补充：在使⽤crontab的时候，要特别注意的是运⾏脚本中能够访问到的环境变量和当前测试环境中环 境变量未必⼀致，⼀个⽐较保险的做法是在运⾏的脚本程序中⾃⾏设置环境变量（export）

- （1） 先建⼀个⽂件crond.txt如下，每天早上5点36分重新启动 36 5 * * * rebot
- （2）上传到/opt⽬录
- （3）运⾏命令 crontab /opt/crond.txt crontab –l 让配置⽂件⽣效：如果让配置⽂件⽣效，还得重新启动cron，切记，既然每个⽤户下的cron配置⽂件 修改后，也要重新启动cron服务器。 在Fedora和Redhat中，我们应该⽤： /etc/rc.d/crond restart 如果让crond在开机时运⾏，应该改变其运⾏级别：


# chkconfig –level 35 crond on service crond status查看cron服务状态，如果没有启动则service crond start启动它，cron服务是⼀个 定时执⾏的服务，可以通过crontab命令添加或者编辑需要定时执⾏的任务

