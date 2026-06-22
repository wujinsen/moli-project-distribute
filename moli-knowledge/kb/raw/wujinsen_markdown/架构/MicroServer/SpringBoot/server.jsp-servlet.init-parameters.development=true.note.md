问题描述

公司项⽬⽤的 Bot，⾃⼰也就对他多研究⼀些，之前⾃⼰练习的Spring Bot项⽬，都是使⽤的JSP， 在修改后直接刷新⻚⾯就可以看到效果，今天升级Spring Bot版本后，发现修改JSP后直接刷新⻚⾯没有⽤ 了。。。重启才能⽣效。。。

spring

问题定位

什么问题都抵不住爱折腾的⼼，我原以为更换了IDE（试了试IDEA这个⼯具）导致的，换回原来的Eclipse惊 奇的发现也是⽆效的。 有想了想，觉得是不是因为修改POM⽂件添加了其他的jar包导致的（使⽤了shiro做权限，和 做缓 存），直接重新建⼀个Spring Bot项⽬，写了最简单的Controler，发现还是不⾏。。。哔了狗了。 各种尝试，想着难不成是Spring Bot版本升级导致的（从1.3.1升到了1.5.1）？换回1.3.1，擦！真的可以了

Redis

问题原因

百度。。。⽊有答案，看来遇到的问题⼈还不多。。。 直接上stackoverflow，终于看到原因。在Spring Bot的GitHub上，有个⼤神（好吧，看来真的是很 ⽜ 逼的 ⼤神）建议

spring-boot recompiles JSPs periodically leading to an unacceptable performance loss in production environments. This behaviour is counter intuitive and hard to find.

The recompilation is behind a switch now but it's turned on (means it's in dev mode all the time). I suggest to turn it off by default and enable it via the spring-boot-devtools.

The switch I am talking about is:

server.jsp-servlet.init-parameters.development

⼤致是说：spring-bot对JSP的重新编译会导致不可接受性能降低在⽣产环境上。找出这个现象是啥啥啥并 且艰难的。 。。。 我建议默认关闭并且通过spring-bot-devtols来开启它（指JSP修改后⽴即重新编译） 。。。 ⽽这个开关我觉得可以是这样的： server.jsp-servlet.init-parameters.development 官⽅⽂档：

JSP servlet

The JSP servlet is no longer in development mode by default. Development mode is automatically enabled when using DevTools. It can also be enabled explicitly by setting server.jsp-servlet.init-parameters.development=true.

解决⽅案

好吧，废话多了，解决⽅案如下 在配置⽂件aplication.properties中添加如下配置：

server.jsp-servlet.init-parameters.development=true

解决问题。

