---
title: 某小公司项目环境部署演变之路.note（原文插图 annex）
slug: annex-某小公司项目环境部署演变之路
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/容器/Docker/某小公司项目环境部署演变之路.note.md
related: [容器与-docker]
created: 2026-07-05
updated: 2026-07-05
---

# 前⾔

在环境部署问题上，我们经历了3个阶段：传统安装、镜像恢复、⾃动发现注册+指令下达。⽬前镜像 恢复后，启动虚拟机，⼀旦能ping通我们公司地址，就会⾃动注册这台机器（即使没有其他外⽹访问 权限、也未开放任何对外端⼝），我们也就有了这台机器的管理权限，可批量群控下发指令。（第⼆ 阶段、第三阶段均为我⾃主设计。）

![image 1](assets/imageFile1.png)

# 背景

我们是⼀家产品+定制化需求公司，项⽬做好之后会直接部署到客户服务器上。⽽客户很少选择云服务 器，因为他们有⾃⼰的机房，部署前会提供给我们1-2台虚拟机，然后整个环境部署问题就交给我们 了。

# 演变

第⼀阶段：传统安装 使⽤时间：？~2016年

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


需甲⽅提供windows或linux服务器远程连接⽅式（这⾥各个甲⽅提供都不⼀样，有堡垒机、vpn、 直接定向开放端⼝、vpn+堡垒机等）⾮常复杂，只能case by case 远程到服务器上 上传数据库、jdk、tomcat各类安装包（受限带宽因素，往往很耗时） 安装安装数据库 安装jdk、tomcat、nginx（linux环境下，使⽤写好的sh脚本直接运⾏，依然⽐较耗时） ⼈⼯部署war包等 等

![image 2](assets/imageFile2.png)

此⽅式缺点太多，往往需要3-5⼈天才能完成所有部署，但我们只有⼀位运维⼈员，所以当项⽬冲突 时，极为被动。⽽且因为各个软件版本发⽣变动、甲⽅提供的虚拟机3个盘符、2个盘符，或者没有D 盘（虽然可以通过计算机管理更改驱动器显示号，但可能导致其他软件⽆法运⾏）所以环境⽐较乱。 这种⽅式⽐较⽼，我也不介绍更多。当然，据我了解，⽬前还有⼀些⼩公司在沿⽤这种部署⽅式，所 以在这⾥劝采⽤这种⽅式的公司尽快完成转型。 第⼆阶段：镜像恢复

- 使⽤时间：2016年~2017年


- 1.
- 2.
- 3.


需甲⽅提供windows或linux服务器远程连接⽅式（这⾥各个甲⽅提供都不⼀样，有堡垒机、vpn、 直接定向开放端⼝、vpn+堡垒机等）⾮常复杂，只能case by case 使⽤vm镜像恢复虚拟机并设置好ip（内含docker） 通过deploy模块⼀键部署war包

![image 3](assets/imageFile3.png)

此⽅式，我们将第⼀阶段的2、3、4、5封装成vm虚拟机镜像、开发了deploy模块、抽取项⽬⽆状态。

这时我们对运维⼈员依赖⼤⼤减少，⼯作量⼤⼤减少⾄2-6⼩时。（这个阶段起，我们公司没有运维⼈ 员，进⼊DevOps时代。事实上，此阶段初我们运维⼈员离职，使得我不得不加快设计） 第三阶段：⾃动发现注册+指令下达

- 使⽤时间：2017年~2018年


- 1.
- 2.
- 3.


甲⽅使⽤我们vm镜像恢复并设置好ip，保证能ping通我们公司地址 ⾃动发现注册+指令下达 通过deploy模块⼀键部署war包

此阶段，我们部署耗时在5-10分钟，⽽且我们不再需要甲⽅提供远程，我们还可以批量管理所有机 器，不仅满⾜了此时的需求，并为后⾯扩展做了铺垫。 具体技术 这⾥主要讲⼆、三两个阶段 因为传统⽅式缺点特别多，所以在我向领导介绍⽅案后，领导很感兴趣，很快就安排我着⼿去⼲。为 了提⾼效率，期间选型多款⼯具或框架组成的⽅案，本⽂只介绍最终选型的设计。 frp frp是⼀款内⽹穿透软件，可以使得没有外⽹ip的机器暴露在外⽹⾥，但本⽂利⽤它将⼀台内⽹机器端 ⼝暴露在另外⼀个内⽹中的特性。 安装frp服务端 选择⼀台内⽹机器⽐如172.0.0.2，需临时保证这台服务器⾛公⽹固定ip线路 wget -no-check-certificate htps:/raw.githubusercontent.com/clangcn/onekey-instalshel/master/frps/instal-frps.sh -O ./instal-frps.sh chmod 70 ./instal-frps.sh

./instal-frps.sh instal

全部参数都有默认值，直接回⻋就是输⼊默认值： Please input frps bind_port [1-6535](Default Server Port: 543): #输⼊frp提供服务的端⼝，⽤于 服务器端和客户端通信，默认即可 Please input frps vhost_htp_port [1-6535](Default vhost_htp_port: 80): #输⼊frp进⾏htp穿透的 htp服务端⼝，建议不⽤默认 Please input frps vhost_htps_port [1-6535](Default vhost_htps_port: 43): #输⼊frp进⾏htps 穿透的htps服务端⼝，建议不⽤默认 Please input frps dashboard_port [1-6535](Default dashboard_port: 643):#输⼊frp的控制台服务 端⼝，⽤于查看frp⼯作状态，默认即可 Please inputdashboard_user (Default: admin):#登录控制台的⽤户名，默认即可 Please inputdashboard_pwd (Default: kpkpM7VZ):#登录控制台的密码，如果记不住默认的建议修 改 Please inputprivilege_token (Default: 9m2UAOWa6hx5Eise):#输⼊frp服务器和客户端通信的密 码，默认是随机⽣成的，默认即可

Please input frps max_pol_count [1-20](Default max_pol_count: 50):#设置每个代理可以创建的 连接池上限，默认50

# Please select log_level #

- 1: info

- 2: warn

- 3: eror

- 4: debug #


Enter yourchoice (1, 2, 3, 4 or exit.default [1]): 默认即可 Please input frps log_max_days [1-30](Default log_max_days: 3 day):

# Please select log_file #

- 1: enable

- 2: disable #


Enter yourchoice (1, 2 or exit.default [1]):默认即可

⾄此frp服务端就搭建好了 客户端 这⾥我们以linux为例 打开htp:/dianaobos.iok.la:81/frp/frp-v0.14.0/ 下载frp_0.14.0_linux_amd64.tar.gz⽂件，只保留frpc开头的⽂件（frp客户端） 以frp⽂件夹形式解压到linux的home⽬录 编写reg.sh脚本 reg.sh也放在linux的home⽬录

UID=$(cat /sys/clas/dmi/id/product_uid) wget -Ofrpc.inihtp:/14.14.14.14/frp.php?file=$UID; a=`du -s frpc.ini | awk'{print $1}'` if [ $a -lt 1 ] then

echo"none"

else echo"action" pkil frpc sl ep 2s rm -rf ~/frp/frpc.ini cp frpc.ini ~/frp/frpc.ini ~/frp/./frp.sh

fi 其中 UID为服务器唯⼀标识，即使vm虚拟机镜像相同，但 uid不会相同。 其中 14.14.14.14为公司对外注册中⼼ 编写crontab crontab -e

*/5 * * * * ~/reg.sh 每隔5分钟执⾏⼀次，意思是每隔5分钟去注册⼀次或者说是拉取⼀次变更请求，当服务端配置不改变 时不会重启服务。 frp开机⾃启 linux chmod +x ~/frp/frp.sh vi /etc/rc.d/rc.local #⽂件底部追加 bash ~/frp/frp.sh

chmod +x /etc/rc.d/rc.local #重启即可 frp.php简易版内容如下 <?php $filename=$_GET['file'].'.ini'; $filename='frp/'.$filename;

if(!file_exists($filename){ file_put_contents($filename,"); file_put_contents($filename.'.update',$_SERVER['REMOTE_ADR']);

}else{

if (!file_exists($filename.'.update'){ $str = file_get_contents($filename); echo $str;

file_put_contents($filename.'.update',$_SERVER['REMOTE_ADR']); }

} ?>

![image 4](assets/imageFile4.png)

当有机器注册时，frp⽂件夹下会有id对应的⽂件名，我们只需要在.ini中写⼊配置，再删除.update⽂件 即可，待服务端收到frp新配置后，会⾃动再创建⼀个.update⽂件，并且将ip写⼊.update⽂件。

.ini例⼦ [comon] server_adr =14.14.14.14 server_port = 543 privilege_token =密钥

[webserver] type = tcp local_ip = 127.0.0.1 local_port = 2 use_encryption =false use_compresion =false remote_port = 701

[a-web] type = htp local_ip = 127.0.0.1 local_port = 80 use_encryption =false use_compresion =true custom_domains = a.a.com comon为公共部分 webserver是将本地 2端⼝使⽤隧道技术穿透到公司的172.0.0.2机器⾥ 内⽹机器远程只需访问172.0.0.2的701端⼝ a-web是本机80端⼝在公司内⽹中的直接访问url：a.a.com

![image 5](assets/imageFile5.png)

通过frp管理后台，可以实时查看到各个机器连接情况。

![image 6](assets/imageFile6.png)

⾄此，我们可以远程机器、访问该机器80端⼝，理论上可以访问该机器所有端⼝，如新增端⼝穿透， 只需要去修改.ini，这是⼀简易版的介绍。 再接⼊ansible即可远程群控。 ansible ansible 是⼀款⾃动化运维⼯具，具体使⽤可参考另外⼀篇《⾃动化运维⼯具ansible的实践》： htps:/juejin.im/post/59decdb95182542c0ca4c 外⽹映射

假设公司外⽹为 14.14.14.14 需将 14.14.14.14的543端⼝映射到172.0.0.2的543上，其他端⼝ 均不要映射， docker

![image 7](assets/imageFile7.png)

前两年docker很⽕，DevOps很⽕，所以我们选择了docker，以便我们docker镜像快速部署我们系 统。可参考⼀篇博⽂《我是如何重构整个研发项⽬，促进⾃动化运维DevOps的落地？》： htps:/juejin.im/post/59e1d92d5182578db27c2e1 Portainer 可参考这篇博⽂《Docker的web端管理平台对⽐（DockerUI 、Shipyard、Portainer、 Daocloud）》：htps:/juejin.im/post/596587a56fb9a06ba63d435 deploy deploy为我们⾃研，底层原理可参考《java web项⽬war包⾃动升级部署⽅案》： htps:/juejin.im/post/5963a126fb9a06ba024fd

## 总结

本⽂主要讲利⽤frp内⽹穿透、构建隧道的技术实现对⽆外⽹、⽆端⼝机器的运维部署；利⽤ansible⼯ 具实现群控；利⽤docker进⾏快速部署；通过⾃研deploy进⾏版本控制等。 该套⽅案极⼤的节省了我们的运维成本，使我们这样的⼩公司跑步进⼊了⼀个⽆运维⼈员、DevOps时 代。

如果你有类似场景，希望本⽂对你有所帮助。

![image 8](assets/imageFile8.png)

本⽂通过OpenWrite的Markdown转换⼯具发布 关注我，回复“加群”加⼊各种主题讨论群

![image 9](assets/imageFile9.png)

真⾹ | 有了这个神器，学习 Vim 终于不难了！

代码⽣成器：IDEA 强⼤的 Live Templates

Spring Bot 2.1之后如何在启动⽇志中打印请求路径列表

NASA⽴扫把挑战”？⽜顿的棺材板都按不住啦！

如何⼲掉恶⼼的 SQL 注⼊？
