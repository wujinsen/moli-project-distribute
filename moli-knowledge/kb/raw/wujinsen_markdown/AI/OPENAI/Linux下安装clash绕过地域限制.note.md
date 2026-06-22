昨天ChatGpt接⼝上线，晚上API中国全都挂了，⽬前想访问需要绕过地区限制。 ⽬前⾹港是可以访问gpt的接⼝的，所以在linux上安装⼀个clash去路由⼀下就⾏了。

# 准备内容

- 1.
- 2.

- a.
- b.


- 3.
- 4.


linux服务器 连接服务器的软件 transmit iterm

⼀个⻜机的配置url ⼀个⽉10块钱

# 步骤：

1.

安装clash的linux版本

htps:/github.com/Dreamacro/clash/releases 将clash部署到服务器上，并且打开对应的端⼝，7890以及9090这俩个端⼝ 这⾥⼀般⼤家将⼆进制⽂件改成clash名字就ok了，因为clash⽂件你上传之后⼀般是由后缀的 将clash的配置⽂件加载进来，⽬录选择和clash⼆进制⽂件在⼀个⽬录

- 1.
- 2.
- 3.


- 1 # 第⼀个这⾥你需要花钱买⼀个代理

- 2 sudo wget -O config.yaml [订阅链接]

- 3 # 这个是基本的⼀些ip数据库信息

sudo wget -O Country.mmdb <https://www.sub-speeder.com/clientdownload/Country.mmdb>

- 4

- 5


1.

配置为全局的代理，进⼊/etc/profile的最下⾯加⼊

- 1 vim /etc/profile

- 2 ## 到最下⾯

- 3 export http_proxy="<http://127.0.0.1:7890>"

- 4 export https_proxy="<http://127.0.0.1:7890>"

- 5 ## 保存出来

- 6 source /etc/profile

- 7


1.

启动clash

- 1 ## 进⼊clash⽬录

- 2 ./clash -d ./

- 3 ## 意思就是启动clash并且加载相对路径下⾯的配置⽂件

- 4


1.

这⾥其实就可以⽤了，但是这个clash是启动在前台的所以我们要去使⽤另外⼀种技术在后台启动

安装systemctl让clash在后台运⾏

- 1 ## 1.编辑clash.service信息

- 2 vim /etc/systemd/system/clash.service

- 3

- 4 ## 2.复制下⾯的这些命令

- 5 [Unit]

- 6 Description=Clash Service

- 7 After=network.target

- 8

- 9 [Service]

- 10 Type=simple

- 11 User=root ## 我这⾥是root，应该放你们的⽤户服务器⽤户名

- 12 ExecStart=/www/clash/clash -d /www/clash ## 这⾥放你们启动clash时候的输⼊的命令

- 13 Restart=always

- 14

- 15 [Install]

- 16 WantedBy=multi-user.target

- 17

- 18 ## 3.保存上⾯的⽂件然后按:wq退出来

- 19 ## 设置clash开机⾃启

- 20 sudo systemctl enable --now clash.service

- 21 ## 设置启动clash

- 22 sudo systemctl start clash.service

- 23 ## 检查clash是否启动成功

- 24 systemctl status clash.service

- 25


systemctl的⼀些基本命令

.

# 安装仪表盘

1.

安装clash的仪表盘 htps:/github.com/Dreamacro/clash-dashboard 把项⽬拉下来，然后需要切换⼀下分⽀

1.

- 1 git clone <https://github.com/Dreamacro/clash-dashboard.git>

- 2 cd clash-dashboard

- 3 git checkout -b gh-pages origin/gh-pages

- 4


1.

修改clash的配置⽂件，在相对路径下⾯有⼀个config.yaml，加上下⾯的这⼏句话

- 1 external-controller: 0.0.0.0:9090

- 2 secret: "你的dashboard的密码"

- 3 external-ui: /www/clash/clash-dashboard

- 4


- 1 启动服务：sudo systemctl start <service_name>

- 2 停⽌服务：sudo systemctl stop <service_name>

- 3 重启服务：sudo systemctl restart <service_name>

- 4 重新加载配置⽂件：sudo systemctl reload <service_name>

- 5 显示服务状态：sudo systemctl status <service_name>

- 6 启⽤服务：sudo systemctl enable <service_name>

- 7 禁⽤服务：sudo systemctl disable <service_name>

- 8 查看所有已启动的服务：sudo systemctl list-units --type=service

- 9


1.

退出之后，重启clash就ok了，输⼊[你的ip地址]:9090/ui就可以看到具体的board了

# 遇⻅的⼀些问题

1.

java项⽬部署后，不⾛clash进⾏路由，我的解决⽅案是

- 1 System.setProperty("http.proxyHost", "127.0.0.1");

- 2 System.setProperty("http.proxyPort", "7890");

- 3 System.setProperty("https.proxyHost", "127.0.0.1");

- 4 System.setProperty("https.proxyPort", "7890");

- 5


- 1.


ip:9090/ui⽆法访问

- a.
- b.
- c.


可能是配置⽂件的原因，需要检查⼀下配置⽂件，我的配置⽂件是显示的127.0.0.1，改成 0.0.0.0后好了 购买的服务器需要开端⼝，不开端⼝是没办法访问的 安装bt的同学，bt也加了⼀层防⽕墙，

- 2.


在哪⾥购买ip地址，我⼀直⽤的是饿饭

