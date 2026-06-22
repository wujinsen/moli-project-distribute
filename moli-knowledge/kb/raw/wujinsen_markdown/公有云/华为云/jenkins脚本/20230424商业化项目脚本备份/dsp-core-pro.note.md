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

[credentialsId: '2aefdd94-7618-44be-a026-155dd02dea78', url: 'https://git.code.tencent.com/dsp-project/pairmb-dsp-service.git']

- 31

- 32 ]])

- 33

- 34 }

- 35 }

- 36


- 37 stage('编译打包') {

- 38 steps {

sh '${MAVEN_HOME}/bin/mvn clean package -P${environment} -U -D maven.test.skip=true -s ${MAVEN_HOME}/conf/shushan-setting.xml '

- 39

- 40

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

- 53 sh """

- 54 echo "开始scp拷⻉jar包⾄服务器: ${serverArr[i]}"

scp /var/lib/jenkins/workspace/dsp-core-pro/dspcore/target/dsp-core-runner.jar root@${serverArr[i]}:/home/xinwu/www/dsp-core

- 55

ssh -p 22 -o 'StrictHostKeyChecking=no' root@${serverArr[i]} '/home/xinwu/www/dsp-core/startup.sh restart'

- 56

- 57 """

- 58 }

- 59 }

- 60 }

- 61 }

- 62

- 63 }

- 64 }

- 65


startup.sh:

- 1 #!/bin/bash

- 2 source /etc/profile

- 3 #java虚拟机启动参数

JAVA_OPTS="-server -Xms2048m -Xmx2048m -Xmn1024m -XX:MetaspaceSize=512m XX:MaxMetaspaceSize=512m -XX:SurvivorRatio=8 -XX:+UseParNewGC XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection XX:MaxTenuringThreshold=15 -XX:CMSFullGCsBeforeCompaction=5 "

- 4

- 5 #这⾥可替换为你⾃⼰的执⾏程序，其他代码⽆需更改

- 6 APP_NAME=dsp-core-runner.jar

- 7

- 8 #SHUTDOWN_WAIT is wait time in seconds for java proccess to stop

- 9 SHUTDOWN_WAIT=35

- 10

- 11 workdir=$(cd $(dirname $0); pwd)

- 12

- 13 #使⽤说明，⽤来提示输⼊参数

- 14 usage() {

- 15 echo "Usage: sh 执⾏脚本.sh [start|stop|restart|status]"

- 16 exit 1

- 17 }

- 18

- 19 #检查程序是否在运⾏

- 20 is_exist(){

- 21 pid=`ps -ef|grep $APP_NAME|grep -v grep|awk '{print $2}' `

- 22 #如果不存在返回1，存在返回0

- 23 if [ -z "${pid}" ]; then

- 24 return 1

- 25 else

- 26 return 0

- 27 fi

- 28 }

- 29

- 30 # @args <beg> <end>

- 31 # return random integer in [<beg>, <end>)

- 32 function random_range() {

- 33 local beg=$1

- 34 local end=$2

- 35 echo $((RANDOM % ($end - $beg) + $beg))

- 36 }

- 37


- 38 #启动⽅法

- 39 start(){

- 40 is_exist

- 41 if [ $? -eq "0" ]; then

- 42 echo "${APP_NAME} is already running. pid=${pid} ."

- 43 else

- 44 #port=$(random_range 30000 60000)

- 45

nohup java $JAVA_OPTS -jar $workdir/$APP_NAME spring.profiles.active=prod &>> /home/xinwu/www/dsp-core/nohup.out &

- 46

- 47 is_exist

- 48 if [ $? -eq "0" ]; then

- 49 echo "${APP_NAME} started. pid=${pid}, port=$port."

- 50 fi

- 51 fi

- 52 }

- 53

- 54 #停⽌⽅法

- 55 stop(){

- 56 is_exist

- 57 if [ $? -eq "0" ]; then

- 58 kill -9 $pid

- 59

- 60

- 61 let kwait=$SHUTDOWN_WAIT

- 62 count=0;

- 63 until [ `ps -p $pid | grep -c $pid` = '0' ] || [ $count -gt $kwait ]

- 64 do

- 65 echo -n -e "\n\e[00;31mwaiting for processes to exit\e[00m";

- 66 sleep 1

- 67 let count=$count+1;

- 68 done

- 69

- 70 if [ $count -gt $kwait ]; then

echo -n -e "\n\e[00;31mkilling processes didn't stop after $SHUTDOWN_WAIT seconds\e[00m"

- 71

- 72 kill -9 $pid

- 73 fi

- 74

- 75 echo "${APP_NAME} stoped."

- 76 else


- 77 echo "${APP_NAME} is not running"

- 78 fi

- 79 }

- 80

- 81 #输出运⾏状态

- 82 status(){

- 83 is_exist

- 84 if [ $? -eq "0" ]; then

- 85 echo "${APP_NAME} is running. Pid is ${pid}"

- 86 else

- 87 echo "${APP_NAME} is NOT running."

- 88 fi

- 89 }

- 90

- 91 #重启

- 92 restart(){

- 93 stop

- 94 start

- 95 }

- 96

- 97 #根据输⼊参数，选择执⾏对应⽅法，不输⼊则执⾏使⽤说明

- 98 case "$1" in

- 99 "start")

- 100 start

- 101 ;;

- 102 "stop")

- 103 stop

- 104 ;;

- 105 "status")

- 106 status

- 107 ;;

- 108 "restart")

- 109 restart

- 110 ;;

- 111 *)

- 112 usage

- 113 ;;

- 114 esac

- 115


# 116

