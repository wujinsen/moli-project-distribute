- 1 // stage('下载代码') {

- 2 // steps {

- 3 // echo "开始拉取代码,分⽀:${branch}"

- 4

- 5 // checkout([$class: 'GitSCM', branches: [

- 6 // [name: '*/${branch}']

- 7 // ], extensions: [], userRemoteConfigs: [

// [credentialsId: '6784a066-4ba6-4fcd-9671-ad4a3d8a3215', url: 'https://git.code.tencent.com/wujinsen/shushan-system-admin.git']

- 8

- 9 // ]])

- 10

- 11 // }

- 12 // }

- 13 // stage('编译打包') {

- 14 // steps {

// sh '${MAVEN_HOME}/bin/mvn clean package -D maven.test.skip=true -s ${MAVEN_HOME}/conf/shushan-setting.xml '

- 15

- 16 // }

- 17 // }

- 18

- 19

- 20 // stage('部署项⽬') {

- 21

- 22 // steps {

- 23

- 24 // script {

- 25

- 26 // def serverArr = servers.tokenize('---')

- 27 // for (int i = 0; i < serverArr.size(); ++i) {

- 28 // echo "拷⻉jar包⾄服务器: ${serverArr[i]} "

// sshPublisher(publishers: [sshPublisherDesc(configName: "${serverArr[i].trim()}", transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''#!/bin/bash

- 29

- 30 // source /etc/profile

- 31 // echo "开始启动项⽬"

- 32 // cd /home/xinwu/www/shushan-system-admin-server

- 33 // pwd

- 34 // ./startup.sh restart

- 35


- // echo "启动项⽬完成"''', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/www/shushan-system-admin-server', remoteDirectorySDF: false, removePrefix: 'shushan-system-admin-server/target/', sourceFiles: 'shushansystem-admin-server/target/*.jar', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])
- 36

- 37

- 38 // }

- 39 // }

- 40 // }

- 41 // }


