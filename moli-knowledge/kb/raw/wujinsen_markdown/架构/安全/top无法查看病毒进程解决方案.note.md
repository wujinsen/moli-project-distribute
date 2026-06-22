htps:/ w.cnblogs.com/keington/p/1523067.html

分享⼀则Linux系统邮件提示 /usr/local/lib/libprocesshider.so ＞ /etc/ld.so.preload 的中病 毒解决⽅法

⾸先根据rot⽤户发送给系统的邮件内容的路径去查看⽂件，

cat /etc/ld.so.preload

发现内容是/usr/local/lib/libproceshider.so，是linux系统的⼀个链接库，在这个⽂件⾥⾯写下的地址 系统在运⾏程序时会⾃动去这些个⽬录⾥⾯找需要的动态库⽂件，先删除试⼀下

rm /etc/ld.so.preload

发现⽆法删除，查找资料后得知Linux系统还有⼀个叫⽂件锁定保护的命令，具体参数如下：

![image 1](<top无法查看病毒进程解决方案.note_images/imageFile1.png>)

使⽤chatr命令解除锁定然后删除

chattr -i /etc/ld.so.preload rm -rf /etc/ld.so.preload

然后再对/usr/local/lib/libproceshider.so进⾏操作，直接⼀步到位，解除锁定，然后删除

chattr -i /usr/local/lib/libprocesshider.so rm -rf /usr/local/lib/libprocesshider.so

执⾏成功，继续下⼀步，查看定时任务，并清理，查询cron.d、cron.hourly、crontab⽬录或⽂件的异 常

lockr -i /etc/cron.d/phps rm -rf /etc/cron.d/phps lockr -i /sbin/httpss rm -rf /sbin/httpss

查看/etc/crontab⽂件内容,/etc/crontab是linux系统定时任务配置⽂件所在，⽤vim编辑器删除最后3 ⾏，最后3⾏就是病毒链接所在，还是⼀样，不管有没有，先解除锁定，再修改

chattr -i /etc/crontab vim /etc/crontab

查看定时任务并修改

crontab -l crontab -e

回显提示crontab: eror renaming /var/spol/cron/#tmp.localhost.localdomain. XPL0tU3 to /var/spol/cron/rot rename: 不允许的操作

crontab: edits left in /tmp/crontab.IFed5j，说明/var/spol/cron/rot，/tmp/crontab.IFed5j这两个⽬ 录⽂件都有问题，跟定时任务是相关联的，先清除这⼏个⽂件，防⽌上锁，先解锁，再删除

chattr -ia /var/spool/cron/root rm -rf /var/spool/cron/root chattr -ia /tmp/crontab.IFed5j rm -rf /tmp/crontab.IFed5j

进⼊到/tmp⽬录下查看是否还有其他的缓存⽂件，如果有，⼀并删除（crontab -e所产⽣） # 服务清理及⾃启动清理，查看/etc/rc.d/init.d/⽬录，/etc/rc.d/rc.local⽂件，/lib/systemd/system⽂件 cd /etc/rc.d/init.d/ #⽆异常 cat /etc/rc.d/rc.local #⽆异常 cd /lib/systemd/system #发现异常服务⽂件

vim pwnriglhttps.service systemctl stop pwnriglhttps.service systemctl disable pwnriglhttps.service

删除服务

rm -rf pwnriglhttps.service

查看系统hosts解析⽂件有⽆异常，如有异常⽤vim编辑器修改

cat /etc/hosts vim /etc/hosts

清理各⽬录下的病毒⽂件

rm -rf /usr/bin/.sh rm -rf /bin/.sh lockr -i /bin/.funzip rm -rf /bin/.funzip

查看/etc/profile⽂件

cat /etc/profile

![image 2](<top无法查看病毒进程解决方案.note_images/imageFile2.png>)

![image 3](<top无法查看病毒进程解决方案.note_images/imageFile3.png>)

回显显示最后4⾏⽂件有问题，⽤vim删除

vim /etc/profile

发现⽬录/etc/profile.d/下出现异常⽂件：php.sh、supervisor.sh 查看内容

cd /etc/profile.d/

cat php.sh

cat supervisor.sh

查看supervisor.sh显示/etc/.supervisor/supervisord.conf 删除删除php.sh，supervisor.sh，/etc/.supervisor/supervisord.conf

lockr -i php.sh supervisor.sh

rm -rf php.sh supervisor.sh

lockr -i /etc/.supervisor/supervisord.conf

rm -rf /etc/.supervisor/supervisord.conf

最后删除/etc/.sh /usr/bin/.sh

chattr -ia /etc/.sh /usr/bin/.sh

rm -rf /etc/.sh /usr/bin/.sh

最后清除邮件并重启

echo "d *" |mail -N reboot

开机后查看⽹络连接信息发现刚开始的那条IP已经没有了，说明残留的病毒⽂件已经清理完成 为了再次防⽌这个矿池来搞事情，最好在出⼝区域的安全设备的对该地址及域名相关进⾏封禁 ⸻—本⽂为原创，转载请禀明出处

本⽂来⾃博客园，作者：许怀安，尊重原创，转载请注明原⽂链接：htps:/ w.cnblogs.com/keingt on/p/1523067.html

