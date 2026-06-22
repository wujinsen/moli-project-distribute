# sp-web-pro:

- 1 final def branch = env.branch

- 2 final def environment = env.environment

- 3

- 4 pipeline {

- 5 agent any

- 6 stages {

- 7 stage('设置构建信息') {

- 8 steps {

- 9 // ⾃定义设置构建历史显示的名称和描述信息

// 不同的部署⽅式设置构建历史显示的名称和描述信息⽅式不⼀样，根据⾃⼰的部署⽅ 式⾃⾏百度找到设置⽅法

- 10

- 11 script {

- 12

- 13 //设置buildName

- 14 wrap([$class: 'BuildUser']) {

- 15 buildName "#${BUILD_NUMBER} -- ${BUILD_USER}"

buildDescription " <span style='padding-left: 0px; fontsize: 10px;background:yellow ;color:black;font-weight:bold'> 分⽀: ${branch} <br/> 环境: ${environment} <br/> 发布机器: ${servers} <br/> 发布⼈: ${BUILD_USER_ID} </span> "

- 16

- 17 }

- 18 }

- 19

- 20 }

- 21 }

- 22

- 23

- 24 stage('下载代码') {

- 25 steps {

- 26 echo "开始拉取代码,分⽀:${branch}"

- 27

- 28 checkout([$class: 'GitSCM', branches: [

- 29 [name: '*/${branch}']

- 30 ], extensions: [], userRemoteConfigs: [

[credentialsId: '2aefdd94-7618-44be-a026-155dd02dea78', url: 'https://git.code.tencent.com/ssp-project/xinwu-ssp.git']

- 31

- 32 ]])

- 33

- 34 }

- 35 }

- 36


- 37 stage('编译打包') {

- 38 steps {

sh '/opt/software/apache-maven-3.2.5/bin/mvn clean package -P${environment}

-U -D maven.test.skip=true -s /opt/software/apache-maven-3.2.5/conf/shushansetting.xml '

- 39

- 40 println 'aaa'

- 41 }

- 42 }

- 43

- 44

- 45 stage('部署项⽬') {

- 46

- 47 steps {

- 48

- 49 script {

- 50

- 51 def serverArr = servers.tokenize('---')

- 52 for (int i = 0; i < serverArr.size(); ++i) {

- 53 echo "拷⻉jar包⾄服务器: ${serverArr[i]} "

- 54 sh """

- 55 echo "开始scp拷⻉jar包⾄服务器: ${serverArr[i]}"

scp /var/lib/jenkins/workspace/ssp-web-admin-pro/sspweb/target/*.war root@${serverArr[i]}:/home/xinwu/www/ssp-web/wars;

- 56

ssh -p 22 -o 'StrictHostKeyChecking=no' root@${serverArr[i]} '/home/xinwu/www/ssp-web/startup.sh'

- 57

- 58 """

- 59

- 60 }

- 61 }

- 62 }

- 63 }

- 64

- 65 }

- 66 }

- 67


# war包启动脚本:

- 1 source /etc/profile

- 2 echo "开始启动项⽬"

- 3

- 4 while true

- 5 do

- 6 port=8080

- 7 # 根据端⼝号去查询对应的PID

- 8 pid=$(netstat -nlp | grep :$port | awk '{print $7}' | awk -F"/" '{ print $1 }');

- 9 echo $pid

- 10 # 杀掉对应的进程 如果PID不存在,即该端⼝没有开启,则不执⾏

- 11 if [ -n "$pid" ]; then

- 12 echo '调⽤tomcat命令停⽌项⽬'

- 13 /home/xinwu/www/ssp-web/bin/shutdown.sh

- 14 else

- 15 echo '进程不在停⽌关闭命令并退出'

- 16 break

- 17 fi

- 18 sleep 5

- 19 done

- 20

- 21 echo '调⽤tomcat命令启动项⽬'

- 22 /home/xinwu/www/ssp-web/bin/startup.sh

- 23

- 24 echo "启动项⽬完成"

- 25


