各种问题：

- 1.max virtual memory areas vm.max_map_count[6530] is to low, increase to at least[26214]

原因分析 系统 默认最⼤映射数为6530，⽆法满⾜ES系统要求，需要调整为26214以上。 处理办法 设置vm.max_map_count参数 #修改⽂件 sudo vim /etc/sysctl.conf

#添加参数

. vm.max_map_count = 26214

- 2. sonar es can not run elasticsearch as rot


虚拟内存

错误原因：因为安全问题 不让⽤rot⽤户直接运⾏ 解决⽅法： 创建新⽤户sonarUser,使⽤该⽤户（sonarUser）运⾏sonar即可。

elasticsearch liunx

创建⽤户 $ aduser sonarUser 为⽤户创建密码 $ paswd sonarUser 修改sonar的⽬录和⽤户组为sonarUser $ chown -R sonarUser:sonarUser sonarqube-6.7.2

