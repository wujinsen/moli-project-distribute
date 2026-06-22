htps:/segmentfault.com/a/19 08649090

版本信息

jenkins：2.23.3

sonarqube：5.6.6

maven加速

<mirror> <id>nexus-aliyun</id> <mirrorOf>*</mirrorOf> <name>Nexus aliyun</name> <url>http://maven.aliyun.com/nexus/content/groups/public</url>

</mirror>

# 复⽤本地maven仓库

docker run -p 8080:8080 -p 50000:50000 \

- -e JAVA_OPTS=-Duser.timezone=Asia/Shanghai \

- -v /Users/xixicat/.m2/repository:/var/repository \

- -v /Users/xixicat/jenkins:/var/jenkins_home \ jenkins 这⾥顺便设置了时区 运⾏sonar docker run -d --name sonarqube \
- -p 9000:9000 -p 9092:9092 \

- -e SONARQUBE_JDBC_USERNAME=sonar \

- -e SONARQUBE_JDBC_PASSWORD=sonar \ sonarqube:lts-alpine


这⾥采⽤默认的内嵌数据库

# 配置sonar

设置server地址

configuration-general setings-Server base URL 改为

htp:/192.168. 9.10 9 0

⽣成token

我的账户-security-generate tokens

jenkins设置sonar

安装sonar的plugin

SonarQube Scaner for Jenkins

系统管理-系统设置-SonarQube servers

设置ServerURL： 设置Server authentication token为上⼀步⽣成的token

htp:/192.168. 9.10 9 0

系统管理-Global Tol Configuration

新增SonarQube Scaner安装，⽤来在命令⾏扫描代码上报给sonarqube server

# 配置job 查看sonar

构建完job之后，有链接可以跳转到sonarqube

doc

sonar-user-token

aliyun阿⾥云Maven仓库地址⸺加速你的maven构建

Jenkins2.1集成Sonar5.4进⾏持续代码分析

