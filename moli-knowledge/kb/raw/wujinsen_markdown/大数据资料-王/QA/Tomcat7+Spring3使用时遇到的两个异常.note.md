今天突然注意到Tomcat7⽀持servlet3，就想把现有的项⽬部署到tomcat7上试试，于是就在官⽹下载 apache-tomcat-7.0.34-windows-x86.zip，经过⼀顿折腾后，把项⽬部署好了。

异常⼀：

⽤eclipse-helios启动TOMCAT7的时候，出现了异常，如下： java.lang.NoClasDefFoundEror: org/apache/juli/loging/LogFactory at org.apache.catalina.startup.Botstrap.<clinit>(Botstrap.java:60) Cau sed by: java.lang.ClasNotFoundException: org.apache.juli.loging.LogFactory TOMCAT6使⽤的时候⼀切正常，为什么到了TOMCAT7后就不能正常加载此类了呢？我⼜直接在bin⽬ 录下执⾏startup.bat，发现没有这个异常，所以有种感觉是与eclipse有关。最后发现此类 在 ./tomcat/bin/⽬录下，经过⼀顿gogle后，有⼈说是要⽤最新版本的eclipse，于是⼜下载了 eclipse-juno版本，结果还是不⾏。

最后终于在⼀⽂章中找到了解决的⽅法，可以在eclipse的 Preferences>Tomcat>JVM Setings>Claspath(Before generated claspath)>Jar/Zip，在弹出框中 选择 ./tomcat/bin/tomcat-juli.jar,保存之后，再从eclipse中启动Tomcat7就不会出现此异常了。

⾄于这个异常的原因，估计是现在的eclipse还不能很好的⽀持Tomcat7吧，因为在最新版本eclipsejuno中也只⽀持到Tomcat6，⽽我在配置tomcat7的时候也是在tomcat6选项中进⾏的。

异常⼆： 上⾯的异常解决后，再启动tomcat后，出现了另⼀个异常，如下： 2012-12-21 16 13 12 org.apache.catalina.core.ContainerBase adChildInternal 严重: ContainerBase.adChild: start: org.apache.catalina.LifecycleException: Failed to start component [StandardEngine[Catalina].Stand ardHost[localhost].StandardContext[] at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:154) at org.apache.catalina.core.ContainerBase.adChildInternal(ContainerBase.java:901) at org.apache.catalina.core.ContainerBase.adChild(ContainerBase.java:87) at org.apache.catalina.core.StandardHost.adChild(StandardHost.java:63) at org.apache.catalina.startup.HostConfig.deployDirectory(HostConfig.java: 14) at org.apache.catalina.startup.HostConfig$DeployDirectory.run(HostConfig.java:1673) at java.util.concurent.Executors$RunableAdapter.cal(Executors.java: 41) at java.util.concurent.FutureTask$Sync.i nerRun(FutureTask.java:303) at java.util.concurent.FutureTask.run(FutureTask.java:138) at java.util.concurent.ThreadPolExecutor$Worker.runTask(ThreadPolExecutor.java: 86) at java.util.concurent.ThreadPolExecutor$Worker.run(ThreadPolExecutor.java:908) at java.lang.Thread.run(Thread.java:619) Caused by: java.lang.NulPointerException at org.springframework.web.SpringServletContainerInitializer.onStartup(SpringServletContainerIniti alizer.java:142)

at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5274) at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:150)

. 1 more

仔细看异常抛出过程就会发现，⼀定是与Spring有关系，当前我使⽤的Spring版本为3.1.0.M2，⼜是⼀ 顿折腾，半天⼜过去了，最后解决问题的还是gogle，看来以后有问题还是直接gogle的好，不要指 望其它了，不过或许我写了这个记录后，可以有其它选择了。

原来这是spring的⼀个BUG吧，可去 查看⼀下，当前 需要做的就是更新Spring到新的版本应该就可以了，于是到官⽹下载Spring3.2.0.RELEASE版本，等 待。。。（下载中） 下载后，包结构有⼀点点变化，对⽐后更新相应的jar包，再次启动tomcat7，⼀切正常！！！

htps:/jira.springsource.org/browse/SPR-8496

