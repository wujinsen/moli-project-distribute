htps:/blog.csdn.net/ q_34021712/article/details/80871015

在多服务统⼀帐号的应⽤集中，单点登录是必不可少的。CAS就是成熟的单点登录框架之⼀。Github 地址 htps:/github.com/apereo/cas。现在我们就通过⼀系列快速简单的构建⽅式实现⼀个简单的单点 登录系统集。 ⾸先下载cas，下载最新版本 htps:/github.com/apereo/cas-overlay-template

域名映射

修改/etc/hosts⽂件，添加服务端域名(server.cas.com) 以及两个客户端的域名(ap1.cas.com , ap2.cas.com)

编译

解压zip，命令⾏进去，执⾏mvn clean package 结束之后会出现 target ⽂件夹，⾥⾯有⼀个cas.war包,这个war包就是我们要运⾏的程序。

本地配置tomcat通过htps访问

⽣成keystore

keytol -genkey -alias tomcat -keyalg RSA -validity 3650 -keystore /Users/wangsaichao/Desktop/tomcat.keystore 1

- -alias tomcat ：表示秘钥库的别名是tomcat，实际操作都⽤别名识别，所以这个参数很重要。
- -validity 3650 ： 表示证书有效期10年。 秘钥库⼝令 我输⼊的是 changeit 。 名字与姓⽒输⼊服务器域名,其它⼀路回⻋，最后如果显示正确 输⼊ ‘yʼ 就⾏了。 tomcat秘钥⼝令我采⽤与秘钥库相同，因此也是⼀路回⻋。


之后可以使⽤以下命令查看⽣成秘钥库的⽂件内容：

keytol -list -keystore /Users/wangsaichao/Desktop/tomcat.keystore 1

根据keystore⽣成crt⽂件

#输⼊第⼀步中keystore的密码changeit keytol -export -alias tomcat -file /Users/wangsaichao/Desktop/tomcat.cer -keystore /Users/wangsaichao/Desktop/tomcat.keystore -validity 3650

- 1
- 2


信任授权⽂件到jdk

sudo keytol -import -keystore /Library/Java/JavaVirtualMachines/jdk1.8.0_14.jdk/Contents/Home/jre/lib/security/cacerts -file /Users/wangsaichao/Desktop/tomcat.cer -alias tomcat -storepas changeit 1 证书库cacerts的缺省⼝令为changeit ，这也是为什么我上⾯的密码都是⽤的它,防⽌混淆,直接都设成 ⼀样的。

注意：我在命令的最前⾯加了 sudo 是因为我的环境是mac 直接操作jdk没有权限。 删除授权⽂件命令如下，删除证书也需要输⼊密码：changeit

sudo keytol -delete -alias tomcat -keystore /Library/Java/JavaVirtualMachines/jdk1.8.0_14.jdk/Contents/Home/jre/lib/security/cacerts 1 查看cacerts中证书 命令如下：

keytol -list -v -keystore /Library/Java/JavaVirtualMachines/jdk1.8.0_14.jdk/Contents/Home/jre/lib/security/cacerts 1 修改tomcat的配置⽂件server.xml

添加以下内容：

<Conector port="843" protocol="org.apache.coyote.htp1.Htp1NioProtocol" maxThreads="20" SLEnabled="true" scheme="htps" secure="true" clientAuth="false"slProtocol="TLS" keystoreFile="/Users/wangsaichao/Desktop/tomcat.keystore"

keystorePas="changeit"/>

- 1
- 2
- 3
- 4
- 5


让chrome浏览器信任证书

启动CAS服务

将第⼀步编译好的cas.war部署到tomcat中启动,然后访问htps:/server.cas.com:843/cas/login 如果 提示签名不正确之类的就点击⾼级/详细信息，继续访问。

旁边Static Authentication 提示你：你现在只有⼀个写死的⽤户默认账号：casuser 默认密码：Melon 仅有这⼀个⽤户，⽬前这个服务端只能看看，没什么实际⽤途。建议您将CAS连接到LDAP、JDBC 等。

什么是Overlay

overlay可以把多个项⽬war合并成为⼀个项⽬，并且如果项⽬存在同名⽂件，那么主项⽬中的⽂件将覆 盖掉其他项⽬的同名⽂件。使⽤maven 的Overlay配置实现⽆侵⼊的改造cas。

使⽤Overlay⽣成真正有⽤的服务端

新建项⽬

pom.xml

pom是从解压的cas.war中拷⻉出来的，将⽆⽤的配置删除。

<?xml version="1.0" encoding="UTF-8"?> <project xmlns="htp:/maven.apache.org/POM/4.0.0"

xmlns:xsi="htp:/ w.w3.org/201/XMLSchema-instance" xsi:schemaLocation="htp:/maven.apache.org/POM/4.0.0

htp:/maven.apache.org/xsd/maven-4.0.0.xsd "> <modelVersion>4.0.0</modelVersion> <groupId>org.apereo.cas</groupId> <artifactId>cas-server-base</artifactId> <packaging>war</packaging> <version>1.0</version>

<build> <plugins>

<plugin> <groupId>org.springframework.bot</groupId> <artifactId>spring-bot-maven-plugin</artifactId> <version>${springbot.version}</version> <configuration>

<mainClas>org.springframework.bot.loader.WarLauncher</mainClas> <adResources>true</adResources>

</configuration> </plugin> <plugin>

<groupId>org.apache.maven.plugins</groupId> <artifactId>maven-war-plugin</artifactId> <version>2.6</version> <configuration>

<warName>cas</warName> <failOnMisingWebXml>false</failOnMisingWebXml> <recompresZi pedFiles>false</recompresZi pedFiles> <archive>

<compres>false</compres> <manifestFile>${project.build.directory}/war/work/org.apereo.cas/cas-server-

webap${ap.server}/META-INF/MANIFEST.MF </manifestFile>

</archive> <overlays>

<overlay> <groupId>org.apereo.cas</groupId>

<artifactId>cas-server-webap${ap.server}</artifactId> </overlay>

</overlays> </configuration> </plugin> <plugin>

<groupId>org.apache.maven.plugins</groupId> <artifactId>maven-compiler-plugin</artifactId> <version>3.3</version>

</plugin> </plugins> <finalName>cas</finalName>

</build>

<dependencies>

<dependency> <groupId>org.apereo.cas</groupId> <artifactId>cas-server-webap${ap.server}</artifactId> <version>${cas.version}</version> <type>war</type> <scope>runtime</scope>

</dependency> <dependency>

<groupId>org.apereo.cas</groupId> <artifactId>cas-server-suport-jdbc</artifactId> <version>${cas.version}</version>

</dependency> <dependency>

<groupId>org.apereo.cas</groupId> <artifactId>cas-server-suport-jdbc-drivers</artifactId> <version>${cas.version}</version>

</dependency> <dependency>

<groupId>mysql</groupId> <artifactId>mysql-conector-java</artifactId> <version>5.1.36</version>

</dependency>

<!-<dependency> <groupId>org.jasig.cas</groupId> <artifactId>cas-server-core-authentication</artifactId> <version>4.2.7</version>

</dependency>-> <!-<dependency>

<groupId>org.apereo.cas</groupId> <artifactId>cas-server-core-util</artifactId> <version>${cas.version}</version>

</dependency>-> </dependencies>

<properties> <cas.version>5.3.2</cas.version> <springbot.version>2.0.0.RELEASE</springbot.version> <!- ap.server could be -jety, -undertow, -tomcat, or blank if you plan to provide apserver -

->

<ap.server>-tomcat</ap.server> <maven.compiler.source>1.8</maven.compiler.source> <maven.compiler.target>1.8</maven.compiler.target> <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

</properties>

<repositories>

<repository> <id>sonatype-releases</id> <url>htp:/os.sonatype.org/content/repositories/releases/</url> <snapshots>

<enabled>false</enabled> </snapshots> <releases>

<enabled>true</enabled>

</releases> </repository> <repository>

<id>sonatype-snapshots</id> <url>htps:/os.sonatype.org/content/repositories/snapshots/</url>

<snapshots>

<enabled>true</enabled> </snapshots> <releases>

<enabled>false</enabled>

</releases> </repository> <repository>

<id>shi boleth-releases</id> <url>htps:/build.shi boleth.net/nexus/content/repositories/releases</url>

</repository> <repository>

<id>spring-milestones</id> <url>htps:/repo.spring.io/milestone</url>

</repository> </repositories>

</project>

最终项⽬⽬录如下：

其中aplication.properties和META-INF⽂件夹从 cs.war ⾥⾯拷⻉出来,还有log4j2.xml也是从cas.war 中拷⻉出来的,因为默认的⽇志⽣成位置是在/etc/cas/logs下,启动会报错。

修改aplication.properties

server.sl.enabled=true server.sl.key-store=file:/Users/wangsaichao/Desktop/tomcat.keystore server.sl.key-store-pasword=changeit server.sl.key-pasword=changeit server.sl.keyAlias=tomcat

在InteliJ IDEA配置Tomcat

- 1.点击Run-Edit Configurations…


- 2.添加tomcat
- 3.配置tomcat如下：
- 4.第⼀次启动会出现以下界⾯，选acept就⾏了


然后访问htps:/server.cas.com:843/cas/login,出现登录界⾯,就可以了，跟之前直接部署cas.war是 ⼀样的。

作者：这个名字想了很久 来源：CSDN 原⽂：htps:/blog.csdn.net/ q_34021712/article/details/80871015 版权声明：本⽂为博主原创⽂章，转载请附上博⽂链接！

