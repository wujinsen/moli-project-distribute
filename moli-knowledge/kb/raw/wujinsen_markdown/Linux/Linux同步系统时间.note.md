时间修改 date ⽉⽇时分年.秒 date -s可以直接设置系统时间

<table>
  <tr>
    <th>1</th>
    <th>-s, --set=STRING set time described by STRING</th>
  </tr>
</table>


⽐如下⾯的例⼦将系统时间设定成2014年 1⽉27⽇的命令如下。 #date -s1/27/2014 将系统时间设定成下午1点12分0秒的命令如下。 #date -s 13 12  0 如果联⽹的情况下，我们还可以使电脑的时间和互联⽹上⼀些服务器⼀样：

<table>
  <tr>
    <th>1</th>
    <th>sudo ntpdate time.windows.com</th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th>27 Nov 23 : 06 : 48 ntpdate[ 5042 ]: adjust time server 64.4<br><br>. 10.33<br><br>offset 0.061032 sec<br><br></th>
  </tr>
</table>


上⾯是微软公司授时主机(美国)time.windows.com。其实台警⼤授时中⼼(台湾)的服务器也是可以更新 的： 查看源代码打印帮助

<table>
  <tr>
    <th>1</th>
    <th>sudo ntpdate asia.pool.ntp.org</th>
  </tr>
</table>


