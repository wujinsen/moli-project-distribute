关于bostrap aplication aplication-dev 三者之间的诡异关系

配置如下: bostrap.yml:

spring: profiles:

active: dev cloud:

nacos:

username: nacos pasword: nacos config:

prefix: user-center-server server-adr: 192.168.1.1  848 namespace: x file-extension: yml

discovery: server-adr: 192.168.1.1  848 namespace: x

aplication-dev.yml: spring:

profiles:

active: dev cloud:

nacos:

username: nacos pasword: nacos config:

prefix: user-center-server server-adr: 192.168.1.2  848 namespace: x file-extension: yml

discovery: server-adr: 192.168.1.2  848 namespace: x

bostrap aplication-dev 都有相同的配置，则aplication-dev会覆盖bostrap的配置

两者的区别

botstrap 配置⽂件先于 aplication 配置⽂件被加载，会先创建初始化 Botstrap Context，再创 建初始化 Aplication Context，应⽤于更早期的配置信息，可以理解为系统配置

botstrap 和 aplication 共享同⼀个 Environment，默认情况下，botstrap 的配置不会被覆盖， ⽽ aplication 的配置项可以被覆盖（⽐如被 aplication-dev 覆盖，或被 java 命令⾏覆盖）

并且如果两个⽂件有相同的配置项，那么 aplication.yml 的会被 aplication-dev.yml 的覆盖

