htps:/blog.csdn.net/a1035082174/article/details/84974869

Dokuwiki安装（linux） ⼀、简介 dokuwiki是⼀个开源wiki引擎程序，运⾏于PHP环境下。⽆需数据库。Doku Wiki 程序⼩巧⽽功能强 ⼤、灵活，适合中⼩团队和个⼈⽹站知识库的管理。 ⼆、安装步骤

- 1.安装前需要确保以下相依组件是否已经安装（如果未安装依次执⾏以下命令）

yum instal gc-c+ yum instal make yum instal expat-devel yum instal perl yum instal curl-devel yum instal libxml2-devel yum instal libjpeg-devel yum instal libpng-devel yum instal fretype-deve

- 2.由于dokuwiki需要PHP环境，故要先装PHP（注：php版本⼀定要在5.4以上）

#yum list php/查找可安装php程序包

#yum instal *.（如php.x86_64） /安装相应php程序包

注：如果安装的是php5.3的版本，可能会出现语法错误

进⼊到报错的⽂件lib/plugins/authplaincas/auth.php中修改语法，按照如图所指。

- 3.dokuwiki还需要Apache服务，故还需装htpd


#yum list htpd/查找可安装htpd程序包 #yum instal *.（如htpd.x86_64） /安装相应htpd程序包

- 4.然后安装dokuwiki，dokuwiki是没有linux和Windows之分，所以安装的时候可以⾃⼰下载下再导⼊ 虚拟机中，也可以直接执⾏以下命令

#wget -c htp:/download.dokuwiki.org/src/dokuwiki/dokuwiki-stable.tgz/下载⽹站安装压缩包 #tar -zvxf dokuwiki-stable.tgz/解包解压缩 #cp -r dokuwiki-2017-02-19e /var/ w/html/wiki /复制解压的⽹站源码⽂件到⽹站根⽬录 （注意：上⾯下载为默认最新版，安装的时候需要注意dokuwiki-2017-02-19e为解压后dokuwiki的名 称）

- 5.更改htpd.conf

vi /etc/htpd/conf/htpd.conf

将 DocumentRot ⽬录更改到第4步中的dokuwiki路径下 默认为/var/ w/html 下⾯配置作⽤是对URL进⾏过滤以保证数据安全 <LocationMatch “/wiki/(data|conf|bin|inc)/”> Order alow,deny Deny from al Satisfy al

由于我在第4步中，在/var/ w/html下建了⼀个wiki⽬录，所以在 DocumentRot ⽬录中我改为 DocumentRot “/var/ w/html/wiki”

- 6.修改权限 根据htpd.conf 中下述两个配置值进⾏修改 User apache Group apache 权限不修改会导致下⾯的访问⻚⾯报错 #chown -R apache.apache /var/ w/html/wiki/将⽂件所有者由rot⽤户改为apache⽤户
- 7.调整Apache mime设定，让Apache 可以⽀援 PHP (/etc/mime.types ) ，在配置中加⼊ aplication/x-htpd-php php php4 phtml aplication/x-htpd-php-source phps


- 8.重启 Apache (service htpd restart)或者(/bin/systemctl restart htpd.service)，连接到 Wiki 下的⽬ 录执⾏ instal.php 进⾏安装设定（htp:/IP或 者域名/instal.php）如出现以下红框内提示说明数据不 安全，需返回步骤5进⾏URL过滤配置。

然后就开始安装dokuwiki。

- 9.由于需要进⾏cas登录验证，dokuwiki⾃⼰有对应的cas登录验证插件，故直接安装⼀个dokuwiki插 件即可。经过调研authplaincas⽐较好⽤，在dokuwiki中，点管 理然后点击扩展管理器，进⼊插件安 装列表
- 10.安装插件的时候，可以直接在下⾯进⾏搜索并安装，也可以在官⽹下载插件放⼊dokuwiki 的/var/ w/html/wiki/lib/plugins⽬录下


1.插件安装完成后需要进⾏配置。

⾸先，这个插件需要⼀个phpCAS库，所以需要先下载htps:/wiki.jasig.org/display/CASC/phpCAS， 将下载下的phpcas放到/var/ w/html/wiki/lib/plugins/authplaincas/下，并重命名为phpCAS，然后到 htps:/ w.dokuwiki.org/auth:cas 下载dokuwiki_inc.zip解压到/var/ w/html/wiki/inc⽬录下。 然后再打开管理，配置设置，对插件进⾏最后的配置

先在在认证设置中，选择插件名称authplaincas

然后在插件设置中，点击authplaincas进⼊插件配置

- 踩坑1：


初次访问可能会出现以下错误：

解决htpd: Could not reliably determine the serverʼs fuly qualified domain name 解决⽅法： ⽤记事本打开 htpd.conf(/etc/htpd/conf/htpd.conf) 将⾥⾯的 #ServerName localhost:80 注释去掉即可。

- 踩坑2：

PhpCAS在此会默认为htps请求，⽽我们的为htp，所以需要⾃⼰再phpCAS/CAS/Client.php(313⾏) 中，在function _getServerBaseURL（）函数中把htps改为htp

- 踩坑3：

如果出现了找不到clas“phpCAS”的错误

解决⽅法：

在lib/plugins/authplaincas/phpCAS中，将CAS的相对路径修改为绝对路径，如图中框定所示

- 踩坑4：


点击登录后会提示PHP Fatal eror: Clas ‘DOMDocumentʼ not found in /var/ w/html/wiki/lib/plugins/authplaincas

解决⽅法：

执⾏以下命令

yum -y instal php-xml

然后重启服务

/bin/systemctl restart htpd.service

未完待续。。。。

⸻版权声明：本⽂为CSDN博主「a1035082174」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附 上原⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/a1035082174/article/details/84974869

