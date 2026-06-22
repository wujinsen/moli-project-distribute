brew update

brew instal

第⼀步，先安装 brew Brew 是 Mac 下⾯的包管理⼯具，通过 Github 托管适合 Mac 的编译配置以 及 Patch，可以⽅便的安装开发⼯具。 Mac ⾃带ruby 所以安装起来很⽅便，同时它也会⾃动把git也 给你装上。官⽅⽹站： 在mac下终端⾥直接输⼊命令⾏： ruby -e "$(curl -fsSL htps:/raw.githubusercontent.com/Homebrew/instal/master/instal)"

htp:/brew.sh

安装完成之后，建议执⾏⼀下⾃检：brew doctor 如果看到Your system is ready to brew. 那么你 的brew已经可以开始使⽤了。 常⽤命令： （所有软件以PHP5.5为例⼦） brew update #更新brew可安装包，建议每次执⾏⼀下 brew search php5 #搜索php5.5 brew tap josegonzalez/php #安装扩展<gi hub_user/repo> brew tap #查看安装的扩展列表 brew instal php5 #安装php5.5 brew remove php5 #卸载php5.5 brew upgrade php5 #升级php5.5 brew options php5 #查看php5.5安装选项 brew info php5 #查看php5.5相关信息 brew home php5 #访问php5.5官⽅⽹站 brew services list #查看系统通过 brew 安装的服务 brew services cleanup #清除已卸载⽆⽤的启动配置⽂件 brew services restart php5 #重启php-fpm

第⼆步，安装PHP

先添加brew的PHP扩展库： brew update brew tap homebrew/dupes brew tap homebrew/php brew tap josegonzalez/homebrew-php

可以使⽤ brew options php53 命令来查看安装php5.3的选项，这⾥我⽤下⾯的选项安装： brew instal php53-with-apache-with-gmp-with-imap-with-tidy-with-debug 切换PHP版本：

brew install php-[version]

1 1

[version]=56|70等 brew unlink -之前版本 brew link php-现在版本 相应的配置都在/usr/local/etc/php/中 管理PHP拓展： ⾸先：brew tap homebrew/php 查看相关拓展：brew search 安装拓展：brew instal php-[version]-拓展名称 如:brew instal php56卸载拓展：brew uninstal php-[version]-拓展名称 删除相应的配置⽂件：rm -rf /usr/local/etc/php/5.6/conf.d/ext-imagick.ini 备注：若需要安装pthreads等多线程拓展，由于此类拓展依赖PHP ZTS版本（Zend Thread Safety），⽽brew默认安装的是NTS版本（Thread Safety），会造成拓展和PHP版本冲突。这时只能 从源码重新遍历了，即在安装命令增加 --build-from-source 如brew install --build-from-source php56redis

PHP

Redis

配置环境变量：

export PATH="/usr/local/opt/php@7.4/bin:$PATH" export PATH="/usr/local/opt/php@7.4/sbin:$PATH"

