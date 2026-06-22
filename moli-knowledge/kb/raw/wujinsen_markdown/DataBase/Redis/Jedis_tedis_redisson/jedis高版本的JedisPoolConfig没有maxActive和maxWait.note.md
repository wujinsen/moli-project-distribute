通过maven pom下载新版jedis jar包，包括2.4.1，2.5.1等⾼版本jedis，当我们试图按照以前的⽅式 配置maxActive属性时，或者spring配置⽂件传⼊redis.pool.maxActive，项⽬启动会报错，原因是 jedis⾼版本的JedisPoolConﬁg没有maxActive和maxWait属性。

jedis包括2.4.1，2.5.1等⾼版本的JedisPoolConﬁg没有maxActive属性，不能按照⽹上那些⽅式去配 置redis了，⽹上⼤部分搜索出来的redis配置都是基于旧版本的jedis，在jedis新版本， JedisPoolConﬁg没有maxActive属性，JedisPoolConﬁg没有maxWait属性，以及被替换成其他的命 名。

下⾯是⽹上的转载，转载之后是jedis⾼版本JedisPoolConﬁg没有maxActive，maxWait的解决⽅ 法。 “

使⽤ 提供的jedis template类感觉操作挺不爽的，⾄于模板其它优点暂不想去升级，准备直接 使⽤jedis api操作。

spring

下⾯是⽹上随处可⻅的⼀段代码。

?

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12</th>
    <th>JedisPolConfig config = newJedisPolConfig(); config.setMaxActive(Integer.valueOf(bundle<br><br>.getString("redis.pol.maxActive"); config.setMaxIdle(Integer.valueOf(bundle<br><br>.getString("redis.pol.maxIdle");<br><br>config.setMaxWait(Long.valueOf(bundle.getString("re dis.pol.maxWait");<br><br>config.setTestOnBorow(Bolean.valueOf(bundle<br><br>.getString("redis.pol.testOnBorow"); config.setTestOnReturn(Bolean.valueOf(bundle<br><br>.getString("redis.pol.testOnReturn"); pol = newJedisPol(config,<br><br>bundle.getString("redis.ip1"),</th>
  </tr>
</table>


Integer.valueOf(bundle.getString("redis.port");

构造 配置⽂件，但是让我⼗分蛋疼的就是，setMaxActive提示没这个⽅法，查看源码 JedisPoolConﬁg继承⾄GenericObjectPoolConﬁg,其⽗类中确实也没有MaxActive这个属性，WHY？ 难道⽹上疯传的都是以讹传讹？暂时不去想这个可能性不⼤的问题，看了下GenericObjectPoolConﬁg 类所在的jar包，org. .commons.pool2.impl.GenericObjectPoolConﬁg,apache提供的xx池,当 然平时⽤的多的是另⼀个包，我⾸先就猜测是不是有同名的类⽂件，Ctrl+T,果然有，继续看，还真存 在MaxActive属性，WHY？难道是JedisPoolConﬁg继承错了，果断⾃⼰ 此类，然⽽JedisPool

连接池

apache

重载 构 造函数

有出错，提示必须是org.apache.commons.pool2.impl.GenericObjectPoolConﬁg的实例，抓 狂了叫喊，各种纠结，最后没辙，只能从开源仓库中下载⼀个个不同版本的jar，找到jedis-2.2.0时， 眼前⼀亮，靠，JedisPoolConﬁg继承的就是我们熟悉的 org.apache.commons.pool.impl.GenericObjectPool.Conﬁg。

jedis的⼤神们做扩展时，能不能考虑下代码的兼容性。。。。 ”

通过这个链接，我们知道commons-pool2 的maxactive，maxWait已经更改命名。 http://mail-archives.apache.org/mod_mbox/tomcat-

dev/201403.mbox/<20140305154712.6B9E123889E2@eris.apache.org>

的修改⽇志显示：change "maxActive" -> "maxTotal" and "maxWait" -> "maxWaitMillis" in all examples.

dbcp

所以⾼版本jedis配置JedisPoolConﬁg的maxActive，maxWait应该为：

<beanid="jedisPoolConﬁg"class="redis.clients.jedis.JedisPoolConﬁg"> <propertyname="maxIdle"value="${redis.pool.maxIdle}"/> <propertyname="maxTotal"value="${redis.pool.maxActive}"/> <propertyname="maxWaitMillis"value="${redis.pool.maxWait}"/> <propertyname="testOnBorrow"value="${redis.pool.testOnBorrow}"/> <propertyname="testOnReturn"value="${redis.pool.testOnReturn}"/>

</bean>

