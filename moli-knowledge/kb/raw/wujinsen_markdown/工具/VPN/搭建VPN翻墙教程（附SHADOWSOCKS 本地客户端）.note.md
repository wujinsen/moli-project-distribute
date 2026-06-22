htps:/ w.mysonu.com/13/04/ 0/sonu/25/

## 服务器

购买⼀台境外的服务器，只有在境外的服务器才能访问外⽹，这⾥推荐 最便宜的VPS主机只要 $2.5/⽉，好像⽬前限制了只能ipv6 国内ipv6只能访问教育⽹，所以我们买$3.5的。

Vultr

## ⼀、Vultr VPS账号注册：

Vultr官⽹ Vultr官⽹

点击前往 注册： ，填写邮箱和密码

![image 1](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile1.png>)

如果提示注册成功，需要邮箱验证的话，请到注册邮箱查看邮件并点击邮箱中 Verify Your E-mail按钮 验证邮箱（收件箱如果没有收到，看下垃圾箱）注册可能遇到的问题，请按要求填写密码，请使⽤没 注册过的邮箱。

![image 2](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile2.png>)

注册完成后我们登录，来到后台 点击Billing->Alipay（⽀付宝）->选择充值得⾦额，冲$10，还有⼀个要强调的是如果⽤信⽤卡付得话 会⾃动续费。

![image 3](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile3.png>)

买服务器VPS 点击 Servers-> + 按钮

![image 4](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile4.png>)

具体选择VULTR的哪个VPS机房是需要根据⾃⼰的需求来判断的，如果做外贸，可以根据⽬标客户群 选择vultr的欧洲、美洲等节点；如果是⾃⼰使⽤、搭梯⼦或者做⾯向国内的⽹站，则建议选择vultr亚太 的节点，例如东京、新加坡、美国洛杉矶的⼏个节点。 服务器类型 默认centos就好 服务器⼤⼩选$3.5/⽉ 其他的选项不管了默认就好了。点击蓝⾊按钮 Deploy Now创建服务器实例 创建好后如下图

![image 5](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile5.png>)

点击服务器查看服务器信息，复制器的ip、⽤户和密码

![image 6](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile6.png>)

## 服务器安装搭建VPN

连接到我们的服务器 Windows

提取码: whg7 安装完成后新建会话（左上⻆的+按钮）（Alt+N）。依次填写名称可以是Vultr或者其他，协议选择 SSH，主机填写之前的IP，端⼝号选择22。

下载 xshell

![image 7](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile7.png>)

点击左侧的⽤户身份验证，填写信息。⽅法选择Password，⽤户名为之前的root ，密码为之前的 Password （这个建议直接复制粘贴过来） Mac Mac就简单了，打开终端 输⼊ ,请把IP 替换成你⾃⼰的IP 然后回⻋会提示你输⼊密码 输⼊密码 然后就 可以了

sh rot@ip

# 安装ss

第⼀种⽅法

⼀键搭建shaodowsocks

- 1.下载⼀键搭建ss脚本⽂件（直接复制这段代码运⾏即可） git clone htps:/github.com/Flyzy205/s-fly


![image 8](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile8.png>)

- 2.运⾏搭建ss脚本代码 s-fly/s-fly.sh -i pasword（你的密码） 1024（可以⾃定义⾃⼰的端⼝）


![image 9](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile9.png>)

其中password换成你要设置的shadowsocks的密码即可，密码最好只包含密码+数字，⼀些特殊字符可 能会导致冲突。⽽第⼆个参数1024是端⼝号，也可以不加，不加默认是1024~（举个例⼦，脚本命令 可以是ss-fly/ss-fly.sh -i 12345a，也可以是ss-fly/ss-fly.sh -i 12345a 8585，后 者指定了服务器端⼝为8585，前者则是默认的端⼝号1024）。 出现如下界⾯就说明搭建好了~

![image 10](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile10.png>)

注：如果需要改密码或者改端⼝，只需要重新再执⾏⼀次搭建ss脚本代码就可以了

## 开启BBR加速

BBR是Google开源的⼀套内核加速算法，可以让你搭建的shadowsocks速度上⼀个台阶。

- 1.检测Ubuntu内核版本 BBR⽀持4.9以上的，如果你的版本⾼于这个则会直接开启BBR加速，如果低于这个版本则会⾃动下载 4.10的并重启，执⾏如下脚本命令：


s-fly/s-fly.sh -br

![image 11](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile11.png>)

第⼀次会检测内核版本并⾃动更新，更新后会重启VPS，再根据连接VPS部分教程重新连接VPS即 可。

- 2.开启BBR加速 s-fly/s-fly.sh -br


<table>
  <tr>
    <th>![image 12](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile12.png>)</th>
  </tr>
</table>


重新连接后，再次运⾏⼀次这个命令即可开启bbr加速。

若服务器没配置成功 ，先卸载

./shadowsocks.sh uninstal

————————————-分————–割—————线—————————————–

## 第⼆种⽅法

连上了服务器 开始安装 ss wget -no-check-certificate -O shadowsocks.sh htps:/raw.githubusercontent.com/tedysun/shadowsocks_instal/master/shadowsocks.sh chmod +x shadowsocks.sh

./shadowsocks.sh 2>&1 | te shadowsocks.log 填写ss密码

<table>
  <tr>
    <th>![image 13](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile13.png>)</th>
  </tr>
</table>


##### 填写端⼝，以后使⽤，默认为8989（有可能不⼀样） 默认就可以了 回⻋就⾏

<table>
  <tr>
    <th>![image 14](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile14.png>)</th>
  </tr>
</table>


选择加密⽅式，我选的7）aes-256-cfb，默认⽅式是aes-256-gcm ⼤家输⼊数字就⾏ 安装完成后，脚本提示如下：

![image 15](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile15.png>)

若服务器没配置成功 ，先卸载

./shadowsocks.sh uninstal

————————————-分————–割—————线—————————————–

## 第三种⽅法搭建Shadowsocks

通过 SSH ⼯具，使⽤ root ⽤户登录，运⾏以下命令执⾏ Shadowsocks 安装配置： wget -no-check-certificate -O shadowsocks-al.sh htps:/raw.githubusercontent.com/tedysun/shadowsocks_instal/master/shadowsocks-al.sh

chmod +x shadowsocks-al.sh

./shadowsocks-al.sh 2>&1 | te shadowsocks-al.log 注：上述服务端安装的 Shadowsocks 是秋⽔逸冰编译的Shadowsocks ⼀键安装脚本（四合⼀），能 够实现⼀键安装 Shadowsocks-Python， ShadowsocksR， Shadowsocks-Go， Shadowsocks-libev 版（四选⼀）服务端。 安装完成后，脚本提示如下 Congratulations, your_shadowsocks_version install completed! Your Server IP :your_server_ip Your Server Port :your_server_port Your Password :your_password Your Encryption Method:aes-256-cfb 你可以根据上⾯你选装的 Shadowsocks 版本执⾏修改⽤户配置⽂件，各版本配置⽂件对应的服务器路 径为：

Shadowsocks-Python 版：/etc/shadowsocks-python/config.json

ShadowsocksR 版：/etc/shadowsocks-r/config.json

Shadowsocks-Go 版：/etc/shadowsocks-go/config.json

Shadowsocks-libev 版：/etc/shadowsocks-libev/config.json

## 单⽤户配置⽂件事例：

{

"server":"0.0.0.0", "server_port":8989, "local_adres":"127.0.0.1", "local_port":1080, "pasword":"yourpasword", "timeout":30, "method":"aes-256-cfb", "fast_open": false

}

## 多⽤户多端⼝配置事例：

配置⽂件路径：/etc/shadowsocks.json {

"server":"0.0.0.0",

"local_adres":"127.0.0.1",

"local_port":1080,

"port_pasword":{

"8989":"pasword0",

- "901":"pasword1",

- "902":"pasword2",

- "903":"pasword3",

- "904":"pasword4"


},

"timeout":30,

"method":"aes-256-cfb",

"fast_open": false

}

*修改配置⽂件注意事项：

- （1）对 SSH 命令不熟悉的朋友，建议使⽤FTP ⼯具访问配置⽂件路径下载配置⽂件修改，修改完毕 后上传覆盖原配置⽂件；
- （2）配置完毕后，可能需要重启 Shadowsocks 服务，启动脚本后⾯的参数含义，从左⾄右依次为： 启动，停⽌，重启，查看状态。 Shadowsocks-Python 版： /etc/init.d/shadowsocks-python start | stop | restart | status

ShadowsocksR 版： /etc/init.d/shadowsocks-r start | stop | restart | status

Shadowsocks-Go 版： /etc/init.d/shadowsocks-go start | stop | restart | status

Shadowsocks-libev 版： /etc/init.d/shadowsocks-libev start | stop | restart | status 举个例⼦，你安装的是 Shadowsocks-Python 版，需要重启 Shadowsocks 服务，则使⽤ SSH ⼯具登 录服务器后， 输⼊命令：/etc/init.d/shadowsocks-python restart

- （3）修改配置⽂件的时候需要注意以下问题：


需要修改的只是端⼝和密码，其他都不需要动，⽐如单⽤户配置只需要修改 server_port 和 password 对应的参数，多⽤户配置只需要修改 port_password 对应的参数；

server_port 对应参数，即端⼝不要与服务器其他服务端⼝相冲突，否则可能会导致服务器其他服务 不可⽤，⽐如443这样的端⼝就不要⽤了。

若已安装多个版本，则卸载时也需多次运⾏（每次卸载⼀种） 使⽤root⽤户登录，运⾏以下命令：

./shadowsocks-al.sh uninstal

配置 shadowsocks 本地客户端

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


Windows客户端提取码: yz2b Mac客户端提取码: in27 Chrome 扩展程序SwitchyOmega提取码: gg47 Linux客户端下载地址：https://github.com/shadowsock…。 Android/安卓客户端下载地址：https://github.com/shadowsock…。 苹果客户端，shadowsocks苹果客户端经常会被App Store下架，可以在App Store搜索关键字 wingy，找到截图中包括填写ip，加密⽅式，密码的软件

<table>
  <tr>
    <th>![image 16](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile16.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 17](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile17.png>)</th>
  </tr>
</table>


## SS使⽤教程

#### Windows（shadowsocks电脑版（windows）客户端配置）：

在状态栏右击shadowsocks，勾选开机启动和启动系统代理，在系统代理模式中选择PAC模式，服务 器->编辑服务器，⼀键安装shadowsocks的脚本默认服务器端⼝是刚刚配置的端⼝，加密⽅式是aes256-cfb，密码是你设置的密码，ip是你⾃⼰的VPS ip，保存即可~

![image 18](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile18.png>)

或

<table>
  <tr>
    <th>![image 19](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile19.png>)</th>
  </tr>
</table>


PAC模式是指国内可以访问的站点直接访问，不能直接访问的再⾛shadowsocks代理~ Mac ⽤户

![image 20](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile20.png>)

![image 21](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile21.png>)

### 浏览器设置 Shadowsocks 代理

- （1）配置 Mac 系统代理 如果你准备将 Mac 系统配置代理，安装以下配置： 点击左上⻆的 Mac icon，在下拉菜单中选择“系统偏好设置”，选择选项卡“代理”，勾选“SOCKS 代 理”：


⽹⻚代理服务器-输⼊127.0.0.1；

local_port 本地端⼝-输⼊前⾯配置服务器 Shadowsocks 时设置的 local_port 参数。

![image 22](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile22.png>)

- （2）配置 Windows 系统代理 以 Windows 10 为例，在资源管理器地址栏中输⼊“控制⾯板\⽹络和 Internet”，选择“Internet 设置” 如果你安装任何扩展⽀持 Shadowsocks，你只需要打开 Chrome 浏览器，在地址栏中输⼊ chrome://settings/，拉到⻚⾯最后选择“显示⾼级设置”；找到⽹络，点击“更改代理服务器设置”，在 Internet 设置窗⼝中选择“链接”-“局域⽹设置”，勾选中“为 LAN 使⽤代理服务器”。


地址-127.0.0.1

端⼝为配置服务器 Shadowsocks 时设置的 local_port ，默认应该为1080.

![image 23](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile23.png>)

### 使⽤ SwitchyOmega 配置代理（推荐）

推荐使⽤ Chrome+SwitchyOmega，实现 Shadowsocks ⽹络访问，直接使⽤系统代理会对平时的正常 ⽹络访问造成诸多不便，来回设置⽐较麻烦。 使⽤ Chrome+SwitchyOmega，平⽇可以按需选择使⽤正常⽹络访问还是 Shadowsocks 代理访问。

- （1）为你的 Chrome 浏览器安装扩展程序 SwitchyOmega，你只需要将前⾯提前为你准备好的 SwitchyOmega 扩展程序安装⽂件拖拽到 Chrome 界⾯即可安装；
- （2）安装完毕后，点击 Chrome 右上⻆的 SwitchyOmega 图标，在下拉菜单选择“选项”进⼊ SwitchyOmega 设置界⾯；选择“新建情景模式”，设置“情景模式名称”及选择“代理服务器”，点击“创建” 按钮进⼊下⼀步；


<table>
  <tr>
    <th>![image 24](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile24.png>)</th>
  </tr>
</table>


配置代理，与前⾯配置系统代理差不多。

代理协议-选择为SOCKS5；

代理服务器-输⼊127.0.0.1（如果你配置服务器端 Shadowsocks 时设置不⼀致，请按照你的配置输 ⼊）；

代理端⼝-默认为1080，如果你配置为其他，请更改为其他端⼝（切记，这是本地端⼝，不是服务 器的端⼝）。

<table>
  <tr>
    <th>![image 25](<搭建VPN翻墙教程（附SHADOWSOCKS 本地客户端）.note_images/imageFile25.png>)</th>
  </tr>
</table>


