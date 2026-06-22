<c3p0-config> <!- 默认配置，如果没有指定则使⽤这个配置 -> <default-config>

<property name="checkoutTimeout">3 0</property><!- 超时时间 -> <property name="idleConectionTestPeriod">30</property><!- 每30秒检查⼀下是否有空闲连接

-> <property name="initialPolSize">2</property> <property name="maxIdleTime">30</property> <property name="maxPolSize">5</property> <property name="minPolSize">2</property> <property name="maxStatements">50</property> <property name="driverClas">com.mysql.jdbc.Driver</property> <property name="jdbcUrl">

<![CDATA[jdbc:mysql:/127.0.0.1  306/student?useUnicode=true&characterEncoding=UTF-8]> </property> <property name="user">rot</property> <property name="pasword">rot</property>

</default-config> <!- 命名的配置以连接其他数据库 -> <named-config name="wj">

<property name="driverClas">com.mysql.jdbc.Driver</property> <property name="jdbcUrl">

<![CDATA[jdbc:mysql:/127.0.0.1  306/student?useUnicode=true&characterEncoding=UTF-8]> </property> <property name="user">rot</property> <property name="pasword">rot</property> <property name="acquireIncrement">2</property><!- 如果池中数据连接不够时⼀次增⻓多少个 -

->

<property name="initialPolSize">2</property> <property name="minPolSize">50</property> <property name="maxPolSize">10</property> <property name="maxStatements">50</property>

</named-config> </c3p0-config>

