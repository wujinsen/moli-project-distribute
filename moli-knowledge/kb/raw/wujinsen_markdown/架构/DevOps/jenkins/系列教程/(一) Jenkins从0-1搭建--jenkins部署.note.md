⼀、下载jenkins

htps:/mirors.jenkins-ci.org/redhat/

2.346.1 (June 202) and newer Java 8, Java1, or Java 17

因为jdk是1.8，所以下载lts为2.346 # 国内环境不是那么好，下载要科学 wget htp:/mirors.jenkins-ci.org/redhat/jenkins-2.346-1.1.noarch.rpm # 安装 rpm -ivh jenkins-2.346-1.1.noarch.rpm

⼆、修改配置

vim /etc/sysconfig/jenkins # 修改内容如下 # 能执⾏jenkins的⽤户权限 JENKINS_USER="rot" # ⻚⾯访问端⼝ JENKINS_PORT="8080"

三、启动

systemctl start jenkins

其他： jenkins home⽬录: /var/lib/jenkins

修改配置: vi /etc/init.d/jenkins

/usr/lib/jenkins/ jenkins安装⽬录，war包会放在这⾥。 /etc/sysconfig/jenkins jenkins配置⽂件，“端⼝”，“JENKINS_HOME”等都可以在这⾥配置。 /var/lib/jenkins/ 默认的JENKINS_HOME。 /var/log/jenkins/jenkins.log jenkins⽇志⽂件。

