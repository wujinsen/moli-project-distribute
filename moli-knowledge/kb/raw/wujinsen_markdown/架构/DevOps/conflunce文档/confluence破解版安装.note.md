htp:/ w.manongjc.com/detail/24-vyhfdfhqckzomls.html

Confluence是⼀款由JAVA编写⽤于企业知识库管理协同软件，多⽤于构建企业内部WIKI，⽀持多⼈协 作，共享信息等。 当前系统环境Centos7.9，内存⾄少2G以上，数据库采⽤MySQL5.7，本机电脑系统Windows10 安装前准备⼯作

- 1、请将confluence7.4破解包下载到本机电脑上
- 2、请将java8程序下载安装到本机电脑上

注：安装java⽤于运⾏confluence7.4破解程序

- 3、将MySQL驱动插件下载⾄CentOS服务器上
- 4、在CentOS服务器上添加 源

rpm-ivh htps:/mirors.wlnmp.com/centos/wlnmp-release-centos.noarch.rpm

- 5、下载confluence7.4安装包⾄CentOS服务器上


htps:/down.whsir.com/downloads/confluence7.4pojie.zip

htps:/down.whsir.com/downloads/jdk-8u172-windows-x64.exe

htps:/down.whsir.com/downloads/mysql-conector-java-5.1.46.tar.gz wlnmp⼀键安装包

htps:/down.whsir.com/downloads/atlasian-confluence-7.4.6-x64.bin

正式安装部署开始

- 1、通过wlnmp包安装jdk1.8及MySQL5.7

yum instal jdk1.8 wmysql57 -y 注：该jdk是oracle提供的包，wlnmp⼀键包将其镜像了过来

- 2、修改MySQL配置 编辑my.cnf⽂件

vi /etc/my.cnf 将i nodb_log_file_size = 32M改成i nodb_log_file_size = 256M 将max_alowed_packet = 16M改成max_alowed_packet = 34M，注意此字段有两个 然后在[mysqld]字段下添加⼀⾏：transaction-isolation=READ-COMI TED 保存退出，重启MySQL：/etc/init.d/mysql restart

- 3、创建数据库 注：通过wlnmp⼀键包安装的MySQL5.7默认密码是空


mysql -urot -p

mysql> create user confluence@localhost identified by 'blog.whsir.com'; mysql> create database confluence character set utf8 colate utf8_bin; mysql> grant al privileges on confluence.* to confluence@'localhost'; mysql> set pasword = pasword('whsir'); mysql> quit 此时数据库rot默认密码更改为whsir，confluence使⽤单独的库、账号密码

- 4、安装confluence


chmod +x atlasian-confluence-7.4.6-x64.bin

./atlasian-confluence-7.4.6-x64.bin Unpacking JRE. Starting Instaler.

This wil instal Confluence7.4.6 on your computer. OK [o, Enter], Cancel [c] 输⼊字⺟o或者直接回⻋ Click Next tocontinue, or Cancel to exit Setup.

Chose the apropriate instalation or upgrade option. Please chose one of the folowing: Expres Instal (usesdefault setings) [1], Custom Instal (recomendedfor advanced users) [2, Enter], Upgrade an existing Confluence instalation [3] 输⼊数字1，然后回⻋ Se where Confluence wil be instaled and the setings that wil be used. Instalation Directory:/opt/atlasian/confluence Home Directory:/var/atlasian/aplication-data/confluence HTP Port:8090 RMI Port:8 0 Instal as service: Yes Instal [i, Enter], Exit [e] 输⼊字⺟i或者直接回⻋ Extracting files.

Please wait a few momentswhile we configure Confluence.

Instalation of Confluence7.4.6 is complete Start Confluence now? Yes [y, Enter], No [n] 输⼊字⺟y或者直接回⻋ Please wait a few momentswhile Confluence starts up. Launching Confluence.

Instalation of Confluence7.4.6 is complete Your instalation of Confluence7.4.6 is now ready and can be acesed via

your browser. Confluence7.4.6 can be acesed at htp:/localhost:8090 Finishing instalation. 安装完成后confluence会⾃动启动

- 5、配置MySQL驱动

tar zxf mysql-conector-java-5.1.46.tar.gz cd mysql-conector-java-5.1.46 mv mysql-conector-java-5.1.46-bin.jar /opt/atlasian/confluence/confluence/WEB-INF/lib

- 6、开始破解 破解需要两部，⼀是破解⽂件，⼆是获取授权码 ⼀、破解⽂件 在CentOS服务器上下载/opt/atlasian/confluence/confluence/WEB-INF/lib/atlasian-extrasdecoder-v2-3.4.1.jar⽂件到本地重命名为atlasian-extras-2.4.jar 通过java运⾏confluence_keygen.jar（⽂中最开始的那个破解包，在windows上运⾏，需要在windows 上安装好java） 选择.patch!找到刚才重命名的那个⽂件打开 打开后在当前⽬录下可以看到atlasian-extras-2.4.jar和atlasian-extras-2.4.bak两个⽂件，这⾥ atlasian-extras-2.4.jar已经是破解好的了，将atlasian-extras-2.4.jar名字改回atlasian-extrasdecoder-v2-3.4.1.jar 然后上传回CentOS服务器/opt/atlasian/confluence/confluence/WEB-INF/lib/⽬录，覆盖掉原来的 atlasian-extras-decoder-v2-3.4.1.jar⽂件 此时要重启confluence服务：/etc/init.d/confluence restart ⼆、获取授权码 访问web⻚⾯，获取服务器ID，htp:/IP 8090

复制⽹⻚中的服务器ID，运⾏破解⼯具confluence_keygen.jar，破解复制Key到Confluence⾥，获得 授权码，进⾏下⼀步

选择MySQL配置项，配置数据库即可

- 7、验证授权 右上⻆配置→⼀般配置→授权细节


请⼀定按照操作步骤来，不建议⼀上来就改来改去。 原⽂地址：htps:/ w.cnblogs.com/tiechui2015/p/14964214.html

