# pipeline script：

/ checkout([$clas: 'GitSCM', branches: [ / [name: '*/master'] / ], extensions: [], userRemoteConfigs: [ / [credentialsId: '2a9b28ab-c4f3-41d8-a191-40c58cb43f19', url:

'htps:/git.code.tencent.com/XW202.03.1/shushan-ap-project.git']

/ ]) }

} stage('构建环境') {

steps {

sh ' ${MAVEN_HOME}/bin/mvn clean instal -Pprod -s ${MAVEN_HOME}/conf/shushanseting.xml '

}

} stage('编译打包') {

steps {

sh '${MAVEN_HOME}/bin/mvn clean package -Pprod -D maven.test.skip=true -s ${MAVEN_HOME}/conf/shushan-seting.xml '

} }

stage('部署项⽬') { steps { script { def serverAr = servers.tokenize(',') for (int i = 0; i < serverAr.size(); +i) { echo "拷⻉jar包⾄服务器: ${serverAr[i]} "

shPublisher(publishers: [shPublisherDesc(configName: "${serverAr[i]}", transfers: [shTransfer(cleanRemote: false, excludes: ', execComand: '#!/bin/bash source /etc/profile echo "开始启动项⽬"

/home/xinwu/ w/read-web/bin/shutdown.sh ort=8081

pid=$(netstat -nlp | grep :$port | awk '{print $7}' | awk -F"/" '{ print $1 }'); echo $pid if [-n "$pid" ]; then

kil -9 $pid;

fi daemonize /home/xinwu/ w/read-web/bin/startup.sh

/ tail -f /home/xinwu/ w/read-web/logs/catalina.out| sed /started/q

echo "启动项⽬完成"', execTimeout: 12 0, flaten: false, makeEmptyDirs: false, noDefaultExcludes: false, paternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/ w/readweb/wars', remoteDirectorySDF: false, removePrefix: 'read-web/target/', sourceFiles: 'readweb/target/*.war', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

} }

}

} }

checkout([$clas: 'GitSCM', branches: [ [name: '*/master'] ], extensions: [], userRemoteConfigs: [ [credentialsId: '2a9b28ab-c4f3-41d8-a191-40c58cb43f19', url: 'htps:/git.code.tencent.com/XW202.03.1/shushan-ap-project.git']

]) }

} stage('构建环境') {

steps {

sh ' ${MAVEN_HOME}/bin/mvn clean instal -Pprod -s ${MAVEN_HOME}/conf/shushanseting.xml '

}

} stage('编译打包') {

steps {

sh '${MAVEN_HOME}/bin/mvn clean package -Pprod -D maven.test.skip=true -s ${MAVEN_HOME}/conf/shushan-seting.xml '

} }

stage('部署项⽬') { steps { script { def serverAr = servers.tokenize(',') for (int i = 0; i < serverAr.size(); +i) { echo "拷⻉jar包⾄服务器: ${serverAr[i]} "

shPublisher(publishers: [shPublisherDesc(configName: "${serverAr[i]}", transfers: [shTransfer(cleanRemote: false, excludes: ', execComand: '#!/bin/bash source /etc/profile echo "开始启动项⽬" while true d port=8081 # 根据端⼝号去查询对应的PID pid=$(netstat -nlp | grep :$port | awk '{print $7}' | awk -F"/" '{ print $1 }'); echo $pid # 杀掉对应的进程 如果PID不存在,即该端⼝没有开启,则不执⾏ if [ -n "$pid" ]; then

echo '调⽤tomcat命令停⽌项⽬' /home/xinwu/ w/read-web/bin/shutdown.sh

else echo '调⽤tomcat命令启动项⽬' daemonize /home/xinwu/ w/read-web/bin/startup.sh

break sl ep 5 fi done

echo "启动项⽬完成"', execTimeout: 12 0, flaten: false, makeEmptyDirs: false, noDefaultExcludes: false, paternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/ w/readweb/wars', remoteDirectorySDF: false, removePrefix: 'read-web/target/', sourceFiles: 'readweb/target/*.war', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

} }

}

} }

<table>
  <tr>
    <th>{<br><br>"brand": "vivo", "hanel_name": "vivo", "create_time": 168612658459, "device_id": ", "event_cont": "{\"reviewed_number\":63,\"item_zon\":\"⼥频<br><br>\",\"item_id\":\"12452\",\"item_series\":\"现代⾔情\",\"module_type\":\"最近阅读 \",\"item_author\":\"许微笑\",\"chapter_name\":\"第60章 没想到唐⼆爷居然好这⼝ \",\"item_name\":\"千亿盛宠：闪婚⽼公超能⼲\",\"item_status\":\"已完结 \",\"read_number\":376704,\"referer_pageName\":\"⾸⻚ \",\"gold_number\":0,\"read_schedule\":\"4.2\",\"read_time\":30,\"chapter_index\":60,\"item_scor e\":\"9.0\"}",<br><br>"event_id": "itemRead_slyd", "imei": ", " ac": ", "moel": "V2046A", "oaid": "bec5253b61d57472d4 3107a29bc51c0c569d769e73d217515e7bc4c31f46e", "": "1", "product_type": "2", "user_id": "2361829", "e o code": "47 028", "version_name": "4.9.7.2107"</th>
  </tr>
</table>


}

