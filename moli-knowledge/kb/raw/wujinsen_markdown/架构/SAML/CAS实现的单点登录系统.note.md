单点登录(single sign on )，简称 SO。

SO的定义是在多个应⽤系统中，⽤户只需要登录⼀次就可以访问所有相互信任的应⽤系统，⽽不需 要重新登录。 ⽤⼀个现实中的例⼦做⽐较。颐和园是北京著名的旅游景点，也是我常去的地⽅。在颐和园内部有许 多独⽴的景点，例如“苏州街”、“佛⾹阁”和“德和园”，都可以在各个景点⻔⼝单独买票。很多游客需要 游玩所有德景点，这种买票⽅式很不⽅便，需要在每个景点⻔⼝排队买票，钱包拿进拿出的，容易丢 失，很不安全。于是绝⼤多数游客选择在⼤⻔⼝买⼀张通票（也叫套票），就可以玩遍所有的景点⽽ 不需要重新再买票。他们只需要在每个景点⻔⼝出示⼀下刚才买的套票就能够被允许进⼊每个独⽴的 景点。 单点登录的机制也⼀样，如下图所示，当⽤户第⼀次访问应⽤系统1的时候，因为还没有登录，会被引 导到认证系统中进⾏登录（1）；根据⽤户提供的登录信息，认证系统进⾏身份效验，如果通过效验， 应该返回给⽤户⼀个认证的凭据－－ticket（2）；⽤户再访问别的应⽤的时候（3，5）就会将这个 ticket带上，作为⾃⼰认证的凭据，应⽤系统接受到请求之后会把ticket送到认证系统进⾏效验，检查 ticket的合法性（4，6）。如果通过效验，⽤户就可以在不⽤再次登录的情况下访问应⽤系统2和应⽤ 系统3了。

要实现 SO，需要实现以下主要的功能： 所有的应⽤系统共享⼀个身份认证系统。 统⼀的身份认证系统是 SO的前提，认证系统的主要功能是将⽤户的登录信息和⽤户信息库相⽐较， 对⽤户进⾏登录认证。认证成功后，认证系统应该⽣成统⼀的认证标志（ticket），返还给⽤户。另 外，认证系统还应该对ticket进⾏效验，判断其有效性。 所有应⽤系统能够识别和提取ticket信息 要实现 SO的功能，让⽤户只登录⼀次，就必须让应⽤系统能够识别已经登录过的⽤户。应⽤系统应 该能对ticket进⾏识别和提取，通过与认证系统的通讯，能⾃动判断当前⽤户是否登录过，从⽽完成单 点登录的功能。 以上内容都来⾃⽹络。

如何使⽤CAS实现单点登录

# ⼀、简介

CAS（Central Authentication Server中央验证系统）是耶鲁⼤学研发的单点登录系统。系统为了安装 考虑默认是需要证书验证的。 本⼈使⽤的环境为：

apache-tomcat-6.0.30(原来⽤的是tomcat7，但中途遇到了843端⼝⽆法验证的问题，怀疑是版本的 原因，因此换成了tomcat6。PS：最后找出了原因是域名的问题，后⾯将会提到)。 JDK6 CAS Server版本：cas-server-3.4.2-release（下载地址： ）解压后， 会发现⼀个modules⽂件夹，⾥⾯包含了配置CAS server所需要的jar包和war⽂件。

htp:/downloads.jasig.org/cas/

CAS Client版本：cas-client-3.2.1（下载地址： ）解压后，会发现⼀个moudules⽂件夹，⾥⾯包含了配置CAS Client所需要的jar包。 PS：选择低版本的客户端可能会出现少jar包的情况。

htp:/downloads.jasig.org/cas-clients/

# ⼆、安装证书：

- 1、⽤JDK⾃带的keytol⽣成证书 keytol -genkey -alias smalove -keyalg RSA -keystore D:/keys/smalkey

这⼀步尤其要注意的是对名字与姓⽒的填写：不要写localhost或者其他的什么东⻄，最好是写域名， ⽐如我输⼊localhost的话，后⾯843端⼝就访问不到系统了，就这个问题我弄了⼀天时间才发现。所 以，最好是输⼊⼀个域名，然后修改C:\Windows\System32\drivers\etc\hosts⽂件，在⾥⾯添加映 射。

- 2、导出证书： keytol -export -file d:/keys/smal.crt -alias smalove -keystore d:/keys/smalkey
- 3、将证书导⼊到JDK中 进⼊C:\Program Files\Java\jdk1.6.0_24\jre\lib\security⽬录 keytol -import -keystore cacerts -file D:/keys/smal.crt -alias smalove 此处如果某个⽂件夹包含了空格的话，绝对路径是不⾏的。刚开始我根据⽹上的资料，⽤绝对路劲来 定位，总是出现错误，后来仔细看菜发现时我的Program Files⽬录⾥有空格。所以必须进⼊到这个⽬ 录下，否则会出现错误。


![image 1](<CAS实现的单点登录系统.note_images/imageFile1.png>)

# 三、HTPS访问CAS

将CAS Server解压出来的war⽂件部署到eclipse⾥⾯。 将tomcat的server.xml的配置修改⼀下，

<Conectorport="843"protocol="HTP/1.1"SLEnabled="true" maxThreads="150"scheme="htps"secure="true" clientAuth="false"slProtocol="TLS" keystoreFile="D:/keys/smalkey" keystorePas="123456"/>

注意⼀定要改对地⽅，如果是⽤eclipse配置的tomcat就在对应的服务器的⽂件⾥改。⽐如我war⽂件 的部署在tomcat v6.0 server at localhost服务器上，就应该改的tomcat v6.0 server at localhostconfig⾥的server.xml。

![image 2](<CAS实现的单点登录系统.note_images/imageFile2.png>)

htps:/so.wsria.com:843/cas

好了，此时在浏览器⾥输⼊ 就会显示出如下⻚⾯

可以点击继续浏览或者将证书安装到信任区⾥。

然后输⼊相同的账户密码就OK。如果进⼊系统后显示登录成功⻚⾯，并且在地址栏⾥给出了 sedionid=…说明CAS Service已经安装成功。否则的话⾃⼰找问题吧^_^。

# 四、客户端的配置。

完成了服务端，现在我们来解决客户端。 新建⼀个两个web⼯程myap1和myap2，然后修改其web.xml如下： <?xmlversion="1.0"encoding="UTF-8"?> <web-apxmlns:xsi=" "xmlns="

htp:/ w.w3.org/201/XMLSchema-instance htp:/java.sun.com/x ml/ns/javae htp:/java.sun.com/xml/ns/javae/webap_2_5.xsd htp:/java.sun.com/xml/ns/javaehtp:/java.sun.com/xml/ns/ja vae/web-ap_2_5.xsd

"xmlns:web=" "xsi:schemaLocation="

"id="WebAp_ID"version="2.5"> <display-name>myap2</display-name>

<filter>

<filter-name>CAS Authentication Filter</filter-name> <filter-clas>org.jasig.cas.client.authentication.AuthenticationFilter</filter-clas> <init-param>

<param-name>casServerLoginUrl</param-name>

<param-value> </param-value> </init-param>

htps:/so.wsria.com:843/cas/

<init-param>

<param-name>serverName</param-name>

<param-value> </param-value> </init-param>

htp:/localhost:8082

</filter>

<filter-maping> <filter-name>CAS Authentication Filter</filter-name> <url-patern>/*</url-patern>

</filter-maping>

<welcome-file-list>

<welcome-file>index.jsp</welcome-file> </welcome-file-list>

</web-ap> 我特地将myap2放在了另外⼀个tomcat服务器⾥⾯，并改了端⼝。 引⼊jar包：cas-client-core-3.2.1.jar、comons-loging-1.1.jar。将这两个包复制到lib⽬录下即可， 如果⽤build path ⾥的ad external jar来加⼊这两个jar包就会提示clas not found的错误。 此时，登录myap1就会跳转到CAS Server的验证界⾯，输⼊相同的⽤户名密码，进⼊myap1，然后 再进⼊myap2，此时就不需要验证了。

# 五、通过数据库和CAS系统⼀起实现单点登录

- 1、服务器的配置 我安装的是mysql5.5，这个就不⽤说了 …安装完成之后，建⽴⼀个名为usr_info的数据库。然后建表 并插⼊数据： create table usr(name char(20) not nul,pasword char(20) not nul);


- insert into usr values("test1" ,"123456");
- insert into usr values("test2" ,"23456"); 将cas-server-3.4.2⾥的modules⾥cas-server-suport-jdbc-3.4.2.jar和jdbc连接MySQL的mysqlconector-java-5.1.18-bin.jar复制到cas项⽬的WEB-INF的lib⽬录下。 修改WEB-INF⽬录下的deployerConfigContext.xml⽂件，将 <beanclas="org.jasig.cas.authentication.handler.suport.SimpleTestUsernamePaswordAuthentic ationHandler" /> 屏蔽掉，然后加⼊以下配置⽂件： <beanclas="org.jasig.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandler"


abstract="false"lazy-init="default"autowire="default"> <propertyname="tableUsers">

<value>usr</value> </property> <propertyname="fieldUser">

<value>name</value> </property> <propertyname="fieldPasword">

<value>pasword</value> </property>

<propertyname="dataSource"ref="dataSource"/> </bean> 上⾯这段配置⽂件主要是添加对⽤户名和密码的校验⼯作，常⽤的有三种校验⽅式 lSimpleTestUsernamePaswordAuthenticationHandler

这个就是默认的简单的⽤户名密码校验，只要⽤户名和密码相同就能登录。 lQueryDatabaseAuthenticationHandler 这个是⽤select语句来验证，具体配置⽂件可以参考⽹上写的。我刚开始时⽤这种⽅法，但每次尽管输 ⼊的⽤户名和密码是正确的，还会提示failed to authenticate the user which provided the folowing credentials：test1，所以我后来改⽤了指定表和字段来连接。这是⼀个很容易让⼈很纠结的问题。 lSearchModeSearchDatabaseAuthenticationHandler 这个是通过指定表盒字段来连接数据库，也就是我的配置⽂件⾥选⽤的连接⽅法。

最后，在⽂件的末尾加上以下配置⽂件： <beanid="dataSource"clas="org.springframework.jdbc.datasource.DriverManagerDataSource"> <property name="driverClasName"><value>com.mysql.jdbc.Driver</value></property> <property name="url"><value>jdbc:mysql:/usr_info</value></property> <property name="username"><value>rot</value></property> <property name="pasword"><value>123</value></property>

</bean>

<beanid="MD5PaswordEncoder"clas="org.jasig.cas.authentication.handler.DefaultPaswordE ncoder">

<constructor-argindex="0">

<value>MD5</value> </constructor-arg>

</bean> 上⾯的这段配置⽂件实际上就是配置数据源和加密算法，很容易理解。

- 2、客户端的配置 在myap1和myap2的web.xml配置⽂件⾥加⼊如下的filter


<!-该过滤器负责对Ticket的校验⼯作，必须启⽤它 -> <filter>

<filter-name>CAS Validation Filter</filter-name> <filter-clas>

org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter</filter-clas>

<init-param> <param-name>casServerUrlPrefix</param-name> <param-value> </param-value>

htps:/so.wsria.com:843/cas

</init-param> <init-param>

<param-name>serverName</param-name>

<param-value> </param-value>

htp:/localhost:8081

</init-param> </filter> <filter-maping>

<filter-name>CAS Validation Filter</filter-name> <url-patern>/*</url-patern>

</filter-maping> 这样就完成了客户端的配置。运⾏服务器和两个客户端，发现输⼊admin和admin已经不能登录系统。 只有当输⼊⽤户名：test1，密码：123456时才能登录。 PS：CAS提供的所谓的内存cokie。也就是，如果将所有的浏览器关闭，那么CAS的cokie会⾃动消 失，需要重新登录。OK 功能完成！

# 六、客户端接收参数，以及取得当前登录的⽤户名

如果我在myap1系统⾥有如下超链接： <a href=" ">进⼊myap2,并携带参数value=helo </a> 当某个超链接携带参数经过CAS验证后，会不会使得参数丢失呢？答案是不会。并且，其接受参数的 ⽅法与没有集成CAS系统的⽅法⼀模⼀样。 那么，在客户端⼜应该如何获得当前登录⽤户的⽤户名呢？ <%@page import="org.jasig.cas.client.util.AbstractCasFilter"%> <%@page import="org.jasig.cas.client.validation.Asertion"%>

htp:/localhost:8082/myap2/index.jsp?value=helo

<% Asertion asertion1= (Asertion)sesion.getAtribute(AbstractCasFilter.CONST_CAS_ASERTION);

String username = asertion1.getPrincipal().getName(); %> Usernam就是当前登录⽤户的⽤户名。

到这⾥就告⼀段落了先。 以后将会进⼀步研究如何在CAS⾥实现权限的管理，⽐如test1⽤户只对A系统和B系统有访问权限，⽽ 对C系统没有访问权限，那么test1在登录过A系统之后，进⼊B系统就不需要验证了，进⼊C系统的话还 是需要验证。可现在是不管进⼊哪个系统都不需要验证。那⼜应该去如何设置呢？

