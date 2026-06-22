- 1.服务器ip
- 2.发布分⽀
- 3.Flag (git参数)
- 4. DisFlag ()


1.发布环境

if(Flag.equals(') {

inputBox="<input name='value' type='text' style='width: 30%;height:32px;' value='Select Branch or Tag as listed above '>"

} else {

inputBox="<input name='value' type='text' style='width: 30%;height:32px;' value=\"${Flag}\" disabled>"

}

wrap([$clas: 'BuildUser']) {

Name "#${BUILD_NUMBER} - ${BUILD_USER}" buildDescription " <span style='pading-left: 0px; font-size: 15px;background:yelow ;color:black;font-weight:bold'> ${branch} <br/> ${environment} <br/> ${BUILD_USER_ID} </span> "

} }

}

} stage('下载代码') {

steps { echo '开始拉取代码' checkout([$clas: 'GitSCM', branches: [ [name: '*/master'] ], extensions: [], userRemoteConfigs: [ [credentialsId: '2a9b28ab-c4f3-41d8-a191-40c58cb43f19', url: 'htps:/git.code.tencent.com/wujinsen/shushan-buried-point-data.git']

]) }

} stage('构建环境') {

steps {

sh ' ${MAVEN_HOME}/bin/mvn clean instal -Pprod -s ${MAVEN_HOME}/conf/shushanseting.xml '

}

} stage('编译打包') {

steps { sh '${MAVEN_HOME}/bin/mvn clean package -Pprod -D maven.test.skip=true -s

def serverAr = servers.tokenize(',') for (int i = 0; i < serverAr.size(); +i) { echo "拷⻉jar包⾄服务器: ${serverAr[i]} "

shPublisher(publishers: [shPublisherDesc(configName: "${serverAr[i]}", transfers: [shTransfer(cleanRemote: false, excludes: ', execComand: '#!/bin/bash source /etc/profile echo "开始启动项⽬" cd /home/xinwu/ w/shushan-buried-point-data-server pwd

./startup.sh restart echo "启动项⽬完成"', execTimeout: 12 0, flaten: false, makeEmptyDirs: false, noDefaultExcludes: false, paternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/ w/shushanburied-point-data-server', remoteDirectorySDF: false, removePrefix: 'shushan-buried-pointdata-server/target/', sourceFiles: 'shushan-buried-point-data-server/target/*.jar', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

} }

}

} }

202127:

wrap([$clas: 'BuildUser']) {

Name "#${BUILD_NUMBER} - ${BUILD_USER}" buildDescription " <span style='pading-left: 0px; font-size: 10px;background:yelow ;color:black;font-weight:bold'> 分⽀: ${branch} <br/> 环境: ${environment} <br/> 发布机器: ${servers} <br/> 发布⼈: ${BUILD_USER_ID} </span> "

} }

}

} stage('下载代码') {

steps { echo '开始拉取代码,分⽀:${branch}' checkout([clas: 'GitSCM', branches: [ [name: '*/${branch}'] ], extensions: [], userRemoteConfigs: [

[credentialsId: 'ba14d728-9f4b-458-843c-3d0ba56cda09', url: 'htps:/git.code.tencent.com/XW202.03.1/shushan-ap-project.git']

]) }

} stage('构建环境') {

steps {

sh ' ${MAVEN_HOME}/bin/mvn clean instal -Pbeta -s ${MAVEN_HOME}/conf/shushanseting.xml '

}

} stage('编译打包') {

steps { sh '${MAVEN_HOME}/bin/mvn clean package -Pbeta -D maven.test.skip=true -s

def serverAr = servers.tokenize(';') for (int i = 0; i < serverAr.size(); +i) { echo "拷⻉jar包⾄服务器: ${serverAr[i]} "

shPublisher(publishers: [shPublisherDesc(configName: "${serverAr[i]}", transfers: [shTransfer(cleanRemote: false, excludes: ', execComand: '#!/bin/bash source /etc/profile echo "开始启动项⽬"

port=8085 # 根据端⼝号去查询对应的PID pid=$(netstat -nlp | grep :$port | awk '{print $7}' | awk -F"/" '{ print $1 }'); echo $pid # 杀掉对应的进程 如果PID不存在,即该端⼝没有开启,则不执⾏ if [ -n "$pid" ]; then

echo '调⽤tomcat命令停⽌项⽬'

kil -9 $pid fi

echo '调⽤tomcat命令启动项⽬' daemonize /home/xinwu/ w/read-web-beta/bin/startup.sh

echo "启动项⽬完成"', execTimeout: 12 0, flaten: false, makeEmptyDirs: false, noDefaultExcludes: false, paterneparator: '[, ]+', remoteDirectory: '/home/xinw/ w/readweb-beta/wars', remoteDirectorySDF: false, removePrefix: 'read-web/target/', sourceFiles: 'readweb/target/*.war', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

} }

}

} }

202-12-07

wrap([$clas: 'Buildser']) { buildName "#${BUILD_NUMBER} - ${BUILD_USER}"

buildDescription " <span style='pading-left: 0px; font-size: 10px;background:yelow ;color:black;font-weight:bold'> 分⽀: ${branch} <br/> 环境: ${environment} <br/> 发布机器: ${servers} <br/> 发布⼈: ${BUILD_USER_ID} </span> "

} }

}

} stage('下载代码') {

steps { echo '开始拉取代码' checkout([$clas: 'GitSCM', branches: [ [name: '*/master'] ], extensions: [], userRemoteConfigs: [ [credentialsId: '2a9b28ab-c4f3-41d8-a191-40c58cb43f19', url: 'htps:/git.code.tencent.com/XW202.03.1/shushan-ap-project.git']

]) }

} stage('构建环境') {

steps {

sh ' ${MAVEN_HOME}/bin/mvn clean instal -Pprod -s ${MAVEN_HOME}/conf/shushanseting.xml '

}

} stage('编译打包') {

steps {

sh '${MAVEN_HOME}/bin/mvn clean package -Pprod -D maven.test.skip=true -s ${MAVEN_HOME}/conf/shushan-seting.xml '

} }

stage('部署项⽬') { steps {

script {

def serverAr = servers.tokenize(' -') for (int i = 0; i < serverAr.size(); +i) {

echo "拷⻉jar包⾄服务器: ${serverAr[i]} "

shPublisher(publishers: [shPublisherDesc(configName: "${serverAr[i].trim()}", transfers: [shTransfer(cleanRemote: false, excludes: ', execComand: '#!/bin/bash source /etc/profile echo "开始启动项⽬" while true d port=8081 # 根据端⼝号去查询对应的PID pid=$(netstat -nlp | grep :$port | awk '{print $7}' | awk -F"/" '{ print $1 }'); echo $pid # 杀掉对应的进程 如果PID不存在,即该端⼝没有开启,则不执⾏ if [ -n "$pid" ]; then

echo '调⽤tomcat命令停⽌项⽬' /home/xinwu/ w/read-web/bin/shutdown.sh

else echo '进程不在停⽌关闭命令并退出' break

fi sl ep 5 done

echo '调⽤tomcat命令启动项⽬' daemonize /home/xinwu/ w/read-web/bin/startup.sh echo "启动项⽬完成"', execTimeout: 12 0, flaten: false, makeEmptyDirs: false, noDefaultExcludes: false, paternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/ w/readweb/wars', remoteDirectorySDF: false, removePrefix: 'read-web/target/', sourceFiles: 'readweb/target/*.war', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

} }

}

} }

202-01-09 pipeline 发布回滚

wrap([$clas: 'Buildser']) { buildName "#${BUILD_NUMBER} - ${BUILD_USER}"

buildDescription " <span style='pading-left: 0px; font-size: 10px;background:yelow ;color:black;font-weight:bold'> 分⽀: ${branch} <br/> 环境: ${environment} <br/> 发布机器: ${servers} <br/> 发布⼈: ${BUILD_USER_ID} </span> "

} }

}

} stage('下载代码') {

steps { echo '开始拉取代码,分⽀:${branch}' checkout([clas: 'GitSCM', branches: [ [name: '*/${branch}'] ], extensions: [], userRemoteConfigs: [

[credentialsId: 'ba14d728-9f4b-458-843c-3d0ba56cda09', url: 'htps:/git.code.tencent.com/qyw202/shushan-admin-operate.git']

]) }

}

stage('编译打包') { steps {

sh '${MAVEN_HOME}/bin/mvn clean package -U -D maven.test.skip=true -s ${MAVEN_HOME}/conf/shushan-seting.xml '

} }

stage('构建环境') { steps { sh'case $status in

deploy)

path="${WORKSPACE}/shushan-admin-auth/bak/${BUILD_NUMBER}" if [ -d $path ];

then echo "The files is already exists "

else

mkdir -p $path fi

p -r ${WORKSPACE}/shushan-admin-auth/target/*.jar $path cd ${WORKSPACE}/shushan-admin-auth/bak/ pwd # 保留历史5个版本 ls -t |awk 'NR>5'|xargs rm -rf

; rolback) echo ' b' pwd

;

*) exit

; esac'

} }

stage('部署项⽬') { steps {

script { /发布

if("${status}"='deploy'){ echo 'helo'

def serverAr = servers.tokenize(' -') for (int i = 0; i < serverAr.size(); +i) {

echo "拷⻉jar包⾄服务器: ${serverAr[i]} "

shPublisher(publishers: [shPublisherDesc(configName: "${serverAr[i].trim()}", transfers: [shTransfer(cleanRemote: false, excludes: ', execComand: '#!/bin/bash source /etc/profile echo "开始启动项⽬" cd /home/xinwu/ w/shushan-admin-operate pwd

./startup.sh restart

echo "启动项⽬完成"', execTimeout: 12 0, flaten: false, makeEmptyDirs: false, noDefaultExcludes: false, paternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/ w/shushanadmin-operate', remoteDirectorySDF: false, removePrefix: 'shushan-admin-auth/target/', sourceFiles: 'shushan-admin-auth/target/*.jar', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

} }

/回滚 if("${status}"='rolback'){ o 'world' echo "${version}"

def serverAr = servers.tokenize(' -') for (int i = 0; i < serverAr.size(); +i) {

echo "拷⻉jar包⾄服务器: ${serverAr[i]} "

shPublisher(publishers: [shPublisherDesc(configName: "${serverAr[i].trim()}", transfers: [shTransfer(cleanRemote: false, excludes: ', execComand: '#!/bin/bash source /etc/profile echo "开始启动项⽬" cd /home/xinwu/ w/shushan-admin-operate pwd

./startup.sh restart

echo "启动项⽬完成"', execTimeout: 12 0, flaten: false, makeEmptyDirs: false, noDefaultExcludes: false, paternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/ w/shushanadmin-operate', remoteDirectorySDF: false, removePrefix: 'shushan-admin-auth/bak/${version}/', sourceFiles: 'shushan-admin-auth/bak/${version}/*.jar', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

}

} }

}

} }

stage('代码质量检测') { steps { script {

def scanerHome = tol 'sonarscaner';/这个名字⼀定是对应全局⼯具配置中sonar扫描器的 名字

withSonarQubeEnv(credentialsId: '1c647d9c-8e47-46c-97ec-64a0ad5310') {

sh "${scanerHome}/bin/sonar-scaner " + "-Dsonar.branch.name=${branch} " + "-Dsonar.projectKey=shushan-admin-operate " + "-Dsonar.projectName=shushan-admin-operate " + "-Dsonar.projectVersion=1.0.0 " + "-Dsonar.language=java " + "-Dsonar.sourceEncoding=utf8 " + "-Dsonar.sources=. " + "-Dsonar.java.binaries=."/这⾥将sonarQube的属性定义在这⾥，可以定义在项⽬⽂件中，在

这⾥引⽤配置⽂件 }

} }

}

