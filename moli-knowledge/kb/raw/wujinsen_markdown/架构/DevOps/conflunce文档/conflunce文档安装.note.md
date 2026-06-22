htps:/blog.csdn.net/a1035082174/article/details/ 9453174

⼀、准备⼯作

- 1.安装jdk


在官⽹下载Linux环境下的jdk1.8： htps:/ w.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-213151.html

将下载下的压缩包解压

tar -xvf jdk-8u14-linux-x64.tar.gz

编辑配置⽂件,添加环境变量

vim /etc/profile

输⼊以下内容 JAVA_HOME 路径根据实际解压路径

JAVA_HOME=/home/java/jdk1.8.0_14 CLASPATH=$JAVA_HOME/lib/ PATH=$PATH:$JAVA_HOME/bin export PATH JAVA_HOME CLASPATH

重启配置⽂件

source /etc/profile

测试 java -version

- 1
- 2
- 3
- 4
- 5
- 6
- 7


- 8
- 9
- 10


- 1

- 12
- 13
- 14
- 15
- 16
- 17
- 18
- 19
- 20
- 21


- 2


- 23
- 24


发现显示的版本不对，这是因为系统⾃带的jdk导致配置⽂件失效

这时需要移除已有的配置 yum -y remove java 然后执⾏which java 删除对应⽬录下的Java⽂件 然后执⾏ source /etc/profile 然后重新检测Java -version

⾄此jdk安装完成。

- 2.安装MySQL (注：confluence⽬前不⽀持8.0版本，8.0版本的驱动为com.mysql.cj.jdbc.Driver，因此我们安装5.7版 本的MySQL) 略
- 3. 下载confluence


windows：htps:/product-downloads.atlasian.com/software/confluence/downloads/atlasianconfluence-6.15.4-x64.exe

linux：htps:/product-downloads.atlasian.com/software/confluence/downloads/atlasianconfluence-6.15.4-x64.bin

- 4.下载破解⼯具

链接：htps:/pan.baidu.com/s/1Pi_ClXR6T4bLbJ-mPxYXtw 提取码：g3p5

- 5.下载MySQL对应的驱动

MySql驱动 htps:/mvnrepository.com/artifact/mysql/mysql-conector-java/5.1.48

- 6.MySql配置


在数据库中跑如下SQL：

1)创建⼀个confluence⽤户

create user 'confluence'@'%' identified by '123456';

如果报错

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9
- 10


- 1


- 12
- 13
- 14


- 15
- 16
- 17
- 18
- 19
- 20
- 21


- 2


23

出现这个问题的原因是：密码过于简单。刚安装的mysql的密码默认强度是最⾼的，如果想要设置简单 的密码就要修改validate_pasword_policy的值

validate_pasword_policy有以下取值：

默认是1，即MEDIUM，所以刚开始设置的密码必须符合⻓度，且必须含有数字，⼩写或⼤写字⺟，特 殊字符。

有时候，只是为了⾃⼰测试，不想密码设置得那么复杂，⽐如说，我只想设置confluence的密码为 123456。

必须修改两个全局参数：

⾸先，修改validate_pasword_policy参数的值

set global validate_pasword_policy=0;

- 1
- 2
- 3


这样，判断密码的标准就基于密码的⻓度了。这个由validate_pasword_length参数来决定。

validate_pasword_length参数默认为8，它有最⼩值的限制，最⼩值为4，由于要设密码为 123456，⻓度为6,

setglobal validate_pasword_length=6;

- 1
- 2
- 3
- 4
- 5


此时，我们在执⾏开始的SQL就可以了

create user ‘confluenceʼ@ʼ%ʼ identified by ‘123456ʼ;

- 2）创建⼀个数据库

CREATE DATABASE confluence CHARACTER SET utf8 COLATE utf8_bin; 1

（注：创建数据库的时候编码必须为utf-8） 1

- 3）给予数据库权限


grant al privileges on confluence.* to confluence@'%'; 1

4）设置默认隔离级别

SET GLOBAL tx_isolation='READ-COMI TED';

- 1
- 2
- 3


FLUSH PRIVILEGES;

1 ⼆、安装confluence

1.给⽂件可执⾏权

chmod +x atlasian-confluence-6.15.7-x64.bin 1

2.执⾏⽂件并根据提示输⼊对应的值，然后回⻋

./atlasian-confluence-6.10.0-x64.bin

- 1
- 2
- 3


- 3.安装完成后就可以访问⻚⾯进⾏后续操作了，confluence默认端⼝为8090，因此访问ip:8090即 可。


htp:/192.168.56.101 8090

如果⻚⾯访问失败

- 1
- 2
- 3
- 4
- 5


关闭防⽕墙然后刷新⻚⾯即可

systemctl stop firewald.service

1 点击右上⻆的language可以选择语⾳，在这⾥我们选择中⽂

选择产品安装然后点击下⼀步

在获取应⽤这个⻚⾯我们不选任何选项直接下⼀步（这个根据个⼈需求来定）

到授权码⻚⾯，这个需要授权码，就需要通过前⾯的破解⼯具来获取授权码，服务器ID需要记下

停⽌confluence，从安装⽬录（默认安装到/opt⽬录下）/opt/atlasian/confluence/confluence/WEBINF/lib下找到atlasian-extras-decoder-v2-3.4.1.jar包，然后拷⻉出来到电脑上 1 重命名为atlasian-extras-2.4.jar

利⽤ confluence_keygen.jar 加 服务器ID 破解，win+R cmd 回⻋，找到jar的⽬录 ，输⼊命令 java jar confluence_keygen.jar 运⾏jar 1

name可以随便填，server ID为上⾯的服务器ID，点击.patch! ，选择atlasian-extras-2.4.jar 后点 击.gen! 后⽣产授权码：

进⼊atlasian-extras-2.4.jar所在的⽬录会看到 有多了⼀个⽂件atlasian-extras-2.4.bak ，不⽤管bak ⽂件，将atlasian-extras-2.4.jar 改为原来的名字 如：atlasian-extras-decoder-v2-3.4.1.jar

将⽂件放进拿出来的⽬录覆盖。（顺便将MySQL的jar也放进去，mysql-conector-java-5.1.48.jar）

然后重新启动confluence服务

service confluence restart

刷新授权码⻚⾯，把获取到的授权码填⼊，然后点击下⼀步

然后选择⾃⼰的数据库点击下⼀步

我们选⽤MySQL数据库

然后填⼊对应信息，数据库和⽤户名密码为我们开始设置的内容，然后点击测试连接看有没有错误信 息。

如果连接成功就点击下⼀步

我们可以点击空⽩站点，选择在Confluence中管理⽤户和组

然后填⼊信息点击下⼀步

在此我设置的⽤户名为admin，密码为admin123

⾄此就安装设置完成了，可以开始使⽤了。

遇到的问题

1。登录后部分中⽂乱码，都是？？？问号乱码

解决办法：修改xml⽂件

vim /var/atlasian/aplication-data/confluence/confluence.cfg.xml

编辑此配置⽂件中“hibernate.conection.url”，注意连接符“&amp;”此处可能变为了“&amp;amp;”， 要修改为“&amp;”修改完后重启。

jdbc:mysql:/ip: 306/confluence?useUnicode=true&amp;characterEncoding=UTF8&amp;useSL=false

修改完成后重启confluence服务

service confluence restart

- 1
- 2
- 3
- 4
- 5
- 6
- 7
- 8
- 9


⸻版权声明：本⽂为CSDN博主「a1035082174」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附 上原⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/a1035082174/article/details/ 9453174

