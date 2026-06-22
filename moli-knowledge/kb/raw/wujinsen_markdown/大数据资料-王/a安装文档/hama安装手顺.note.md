- 1.准备环境 按照hadop1⼿顺和zokeper⼿顺安装好hadop1和zokeper(安装在hadop1的slave上)
- 2.在slave1上解压缩hama-0.6.2.tar.gz并且重命名为hama
- 3.配置环境变量HAMA_HOME、PATH
- 4.进⼊conf⽬录


- 4.配置hama-env.sh⽂件，找到JAVA_HOME进⾏配置 并且找到HAMA_MANAGES_ZK取消注释并改为false（默认是true）
- 5.配置gromservers，该⽂件中列出了gromserver守护进程所在的节点，每个⼀⾏

- slave1
- slave2
- slave3


- 6.配置hama-site.xml <configuration>

<property> <name>bsp.master.adres</name> <value>master:4 0</value>

</property> <property>

<name>fs.default.name</name> <value>hdfs:/master:9 0/</value>

</property> <property>

<name>hama.zokeper.quorum</name> <value>slave1，slave2，slave3</value>

</property> </configuration>

- 7.将hama分发到各slave中
- 8.启动hadop1和zokeper
- 9.启动hama start-bsp.sh htp:/master:4013查看是否安装成功


