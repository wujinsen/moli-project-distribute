hive-service hwi [niy@niy-computer /]$ $HIVE_HOME/bin/hive-service hwi

- 13/04/26 0 21 17 INFO hwi.HWIServer: HWI is starting up
- 13/04/26 0 21 18 FATAL hwi.HWIServer: HWI WAR file not found at /usr/local/hive/usr/local/hive/lib/hive-hwi-0.12.0-SNAPSHOT.war 可以看出/usr/local/hive/usr/local/hive/lib/hive-hwi-0.12.0-SNAPSHOT.war 肯定 不是正确路径，真正 路径是/usr/local/hive/lib/hive-hwi-0.12.0-SNAPSHOT.war 断定是配置的问题


# 解决办法

将hive-default.xml中关于 hwi的设置拷⻉到hive-site.xml中即可 [plain]

view plaincopy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.


<property> <name>hive.hwi.war.file</name> <value>lib/hive-hwi-0.12.0-SNAPSHOT.war</value> <description>This sets the path to the HWI war file, relative to ${HIVE_HOME}. </description>

</property>

<property> <name>hive.hwi.listen.host</name> <value>0.0.0.0</value> <description>This is the host adres the Hive Web Interface wil listen on</description>

</property>

<property> <name>hive.hwi.listen.port</name> <value> 9</value> <description>This is the port the Hive Web Interface wil listen on</description>

</property>

再次运⾏上⾯的命令 [niy@niy-computer hive]$ bin/hive-service hwi 13/04/26 0 24 51 INFO hwi.HWIServer: HWI is starting up 13/04/26 0 24 51 INFO mortbay.log: Loging to org.slf4j.impl.Log4jLogerAdapter(org.mortbay.log) via org.mortbay.log.Slf4jLog

13/04/26 0 24 51 INFO mortbay.log: jety-6.1.14

- 13/04/26 0 24 51 INFO mortbay.log: Extract jar:file:/home/niy/workspace1/hive/trunk/build/dist/lib/hive-hwi-0.12.0-SNAPSHOT.war!/ to /tmp/Jety_0_0_0_0_ 9_hive.hwi.0.12.0.SNAPSHOT.war_hwi _.bt0qvz/webap
- 13/04/26 0 24 52 INFO mortbay.log: Started SocketConector@0.0.0.0  9 这时打开浏览器，输⼊


htp:/localhost: 9/hwi

即可验证服务已正常开启

