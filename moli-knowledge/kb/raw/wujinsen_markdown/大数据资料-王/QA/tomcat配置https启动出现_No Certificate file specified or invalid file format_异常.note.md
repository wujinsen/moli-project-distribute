# 分类： java2012-05-04 2 50906⼈阅读

(2)

评论 收藏举报 filetomcatdescriptorinitializationschemeaplication

[html]

view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.


<Conectorport="843"protocol="HTP/1.1" SLEnabled="true" maxThreads="150"scheme="htps"secure="true" clientAuth="false" slProtocol="TLS" keystoreFile="D:/Tomcat6/server.keystore" keystorePas="changeit"/>

这是tomcat的 SL配置。 但是启动的时候报"No Certificate file specified or invalid file format"异常，完整的启动信息如下

2012-4-28 9:17:07 org.apache.catalina.core.AprLifecycleListener init信息: Loaded APR based Apache Tomcat Native library 1.1.20.2012-4-28 9:17:07 org.apache.catalina.core.AprLifecycleListener init信息: APR capabilities: IPv6 [true], sendfile [true], accept filters [false], random [true].20124-28 9:17:07 org.apache.catalina.startup.SetAllPropertiesRule begin警告: [SetAllPropertiesRule]{Server/Service/Connector} Setting property 'clientAuth' to 'false' did not find a matching property.2012-4-28 9:17:07 org.apache.catalina.startup.SetAllPropertiesRule begin警告: [SetAllPropertiesRule]{Server/Service/Connector} Setting property 'keystoreFile' to 'D:/Tomcat6/server.keystore' did not find a matching property.2012-4-28 9:17:07 org.apache.catalina.startup.SetAllPropertiesRule begin警告: [SetAllPropertiesRule]{Server/Service/Connector} Setting property 'keystorePass' to 'changeit' did not find a matching property.2012-4-28 9:17:08 org.apache.coyote.http11.Http11AprProtocol init严重: Error initializing endpointjava.lang.Exception: No Certificate file specified or invalid file formatat org.apache.tomcat.jni.SSLContext.setCertificate(Native Method)at org.apache.tomcat.util.net.AprEndpoint.init(AprEndpoint.java:733)at org.apache.coyote.http11.Http11AprProtocol.init(Http11AprProtocol.java:107 )at org.apache.catalina.connector.Connector.initialize(Connector.java:1022)at org.apache.catalina.core.StandardService.initialize(StandardService.java:7 03)at org.apache.catalina.core.StandardServer.initialize(StandardServer.java:838 )at org.apache.catalina.startup.Catalina.load(Catalina.java:538)at org.apache.catalina.startup.Catalina.load(Catalina.java:562)at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java: 39)at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorIm pl.java:25)at java.lang.reflect.Method.invoke(Method.java:597)at org.apache.catalina.startup.Bootstrap.load(Bootstrap.java:261)at

- org.apache.catalina.startup.Bootstrap.main(Bootstrap.java:413)2012-4-28


- 9:17:08 org.apache.catalina.core.StandardService initialize严重: Failed to initialize connector [Connector[HTTP/1.1-8443]]LifecycleException:


Protocol handler initialization failed: java.lang.Exception: No

Certificate file specified or invalid file formatat org.apache.catalina.connector.Connector.initialize(Connector.java:1024)at org.apache.catalina.core.StandardService.initialize(StandardService.java:7 03)at org.apache.catalina.core.StandardServer.initialize(StandardServer.java:838 )at org.apache.catalina.startup.Catalina.load(Catalina.java:538)at org.apache.catalina.startup.Catalina.load(Catalina.java:562)at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java: 39)at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorIm pl.java:25)at java.lang.reflect.Method.invoke(Method.java:597)at org.apache.catalina.startup.Bootstrap.load(Bootstrap.java:261)at

- org.apache.catalina.startup.Bootstrap.main(Bootstrap.java:413)2012-4-28


- 9:17:08 org.apache.coyote.ajp.AjpAprProtocol init信息: Initializing Coyote


- AJP/1.3 on ajp-80092012-4-28 9:17:08 org.apache.catalina.startup.Catalina load信息: Initialization processed in 804 ms2012-4-28 9:17:08 org.apache.catalina.core.StandardService start信息: Starting service Catalina2012-4-28 9:17:08 org.apache.catalina.core.StandardEngine start信 息: Starting Servlet Engine: Apache Tomcat/6.0.332012-4-28 9:17:08 org.logicalcobwebs.proxool.ProxoolFacade registerConnectionPool信息: Proxool 0.9.1 (23-Aug-2008 11:10)2012-4-28 9:17:08 org.logicalcobwebs.proxool.ConnectionPoolDefinition setAnyProperty警告: Use of proxool.maximum-new-connections is deprecated. Use more descriptive proxool.simultaneous-build-throttle instead.2012-4-28 9:17:08 org.apache.catalina.startup.HostConfig deployDescriptor信息: Deploying configuration descriptor host-manager.xml2012-4-28 9:17:08 org.apache.catalina.startup.HostConfig deployDescriptor信息: Deploying configuration descriptor manager.xml2012-4-28 9:17:08 org.apache.catalina.startup.HostConfig deployDirectory信息: Deploying web application directory docs2012-4-28 9:17:08 org.apache.catalina.startup.HostConfig deployDirectory信息: Deploying web application directory examples2012-4-28 9:17:09 org.apache.coyote.http11.Http11AprProtocol start严重: Error starting endpointjava.lang.Exception: Socket bind failed: [730048] ?????????×???(Э? é/???????/???)????í??at org.apache.tomcat.util.net.AprEndpoint.init(AprEndpoint.java:649)at


org.apache.tomcat.util.net.AprEndpoint.start(AprEndpoint.java:766)at org.apache.coyote.http11.Http11AprProtocol.start(Http11AprProtocol.java:13 7)at org.apache.catalina.connector.Connector.start(Connector.java:1095)at org.apache.catalina.core.StandardService.start(StandardService.java:540)at org.apache.catalina.core.StandardServer.start(StandardServer.java:754)at org.apache.catalina.startup.Catalina.start(Catalina.java:595)at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java: 39)at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorIm pl.java:25)at java.lang.reflect.Method.invoke(Method.java:597)at org.apache.catalina.startup.Bootstrap.start(Bootstrap.java:289)at

- org.apache.catalina.startup.Bootstrap.main(Bootstrap.java:414)2012-4-28


- 9:17:09 org.apache.catalina.core.StandardService start严重: Failed to start connector [Connector[HTTP/1.1-8443]]LifecycleException:


service.getName(): "Catalina"; Protocol handler start failed: java.lang.Exception: Socket bind failed: [730048] ?????????×???(Э? é/???????/???)????í??at org.apache.catalina.connector.Connector.start(Connector.java:1102)at org.apache.catalina.core.StandardService.start(StandardService.java:540)at org.apache.catalina.core.StandardServer.start(StandardServer.java:754)at org.apache.catalina.startup.Catalina.start(Catalina.java:595)at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java: 39)at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorIm pl.java:25)at java.lang.reflect.Method.invoke(Method.java:597)at org.apache.catalina.startup.Bootstrap.start(Bootstrap.java:289)at

- org.apache.catalina.startup.Bootstrap.main(Bootstrap.java:414)2012-4-28


- 9:17:09 org.apache.coyote.ajp.AjpAprProtocol start信息: Starting Coyote


- AJP/1.3 on ajp-80092012-4-28 9:17:09 org.apache.catalina.startup.Catalina start信息: Server startup in 534 ms 后来在⽹上查找了相关资料，应该和tomcat的版本有光，⽤tomcat6.0.18就不会报这个错误，⽤ tomcat6.0. 3就会出现这个错误。 由于6.0. 3版本中默认启⽤了APR（APR是通过JNI访问的可移植库，可以提⾼Tomcat的性能和伸缩 性），所以采⽤传统的配置⽅式（如下）会报异常 解决办法是采⽤下⾯的配置


[html]view plaincopy <Conectorport="843"protocol="org.apache.coyote.htp1.Htp1Protocol" SLEnabled="t rue"

- 1.
- 2.
- 3.
- 4.
- 5.


maxThreads="150"scheme="htps"secure="true" clientAuth="false" slProtocol="TLS" keystoreFile="D:/Tomcat6/server.keystore" keystorePas="changeit"/>

