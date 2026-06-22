- 1 import groovy.json.JsonSlurper

- 2 final def branch = env.branch

- 3 final def environment = env.environment

- 4 def asIpListGlobal; //弹性伸缩机器ip列表

- 5 final def projectId = "1552922d99cd4940ac94fdcaa29b8ca6";

- 6 final def poolId = "67e5900c-e5a4-415d-9bb6-4b0c8f0660f8"; // 后端服务器组id

- 7 final def imageHost = "172.16.15.33"; //23.249.125.253 镜像服务器

- 8 final def scalingGroupId = "b7ba101f-ca30-4ffc-8180-6ac056dbe0ea"; //弹性伸缩组id

- 9 def responseToken

- 10 def serverList = []

- 11 def asServerList = []

- 12 def asIpList = []

- 13

- 14 pipeline {

- 15 agent any

- 16 stages {

- 17

- 18 stage('下载代码') {

- 19 steps {

- 20 echo "开始拉取代码,分⽀:${branch}"

- 21

- 22 checkout([$class: 'GitSCM', branches: [

- 23 [name: '*/${branch}']

- 24 ], extensions: [], userRemoteConfigs: [

[credentialsId: '2aefdd94-7618-44be-a026-155dd02dea78', url: 'https://git.code.tencent.com/ssp-project/xinwu-api.git']

- 25

- 26 ]])

- 27

- 28 }

- 29 }

- 30

- 31 stage('编译打包') {

- 32 steps {

// sh '${MAVEN_HOME}/bin/mvn clean package -P${environment} -U -D maven.test.skip=true -s ${MAVEN_HOME}/conf/shushan-setting.xml '

- 33

- 34 println "aaa"

- 35 }

- 36 }

- 37

- 38


- 39 stage('流量下线') {

- 40 steps {

- 41 script {

- 42

- 43 //1. 获取华为云token

- 44 def tokenReq = '''

- 45 {

- 46 "auth": {

- 47 "identity": {

- 48 "methods": [

- 49 "password"

- 50 ],

- 51 "password": {

- 52 "user": {

- 53 "domain": {

- 54 "name": "hid_e2hk8hwatanq6f2"

- 55 },

- 56 "name": "zongcheng",

- 57 "password": "ShuMiao@)!*0812"

- 58 }

- 59 }

- 60 },

- 61 "scope": {

- 62 "project": {

- 63 "name": "cn-north-4"

- 64 }

- 65 }

- 66 }

- 67 }

- 68 '''

def tokenUrl = "https://iam.myhuaweicloud.com/v3/auth/tokens? nocatalog=true"

- 69

def tokenJsonBody = groovy.json.JsonOutput.toJson(new groovy.json.JsonSlurperClassic().parseText(tokenReq))

- 70

- 71 println tokenJsonBody

def response = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: tokenJsonBody, url: tokenUrl

- 72

- 73 println('response status: '+response.status)

- 74 responseToken = response.headers["X-Subject-Token"][0]

- 75

- 76


- 77 //查询后端云服务器列表，获取member_id

def membersUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members"

- 78

def responseMember = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'GET', customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: membersUrl

- 79

- 80

def responseConent = new groovy.json.JsonSlurperClassic().parseText(responseMember.content)

- 81

- 82 println('后端云服务器列表返回内容: ' + responseConent)

- 83 //查询弹性伸缩组中的实例列表

def listScalingUrl = "https://as.cn-north4.myhuaweicloud.com/autoscalingapi/v1/"+projectId+"/scaling_group_instance/"+scalingGroupId+"/list"

- 84

def responselistScalingInstances = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'GET', customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: listScalingUrl

- 85

def responselistScalingInstancesContent = new groovy.json.JsonSlurperClassic().parseText(responselistScalingInstances.content)

- 86

- 87

- 88

- 89 //选中的服务器IP

- 90 def serverArr = servers.tokenize('---')

- 91

if(responselistScalingInstancesContent.scaling_group_instances.size() > 0){

- 92

for(scalelist in responselistScalingInstancesContent.scaling_group_instances){

- 93

def listServerInterfacesUrl = "https://ecs.cn-north4.myhuaweicloud.com/v1/"+projectId+"/cloudservers/"+scalelist.instance_id+"/osinterface"

- 94

def listServerInterfacesResponse = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'GET', customHeaders: [ [name: "XAuth-Token", value: responseToken] ], url: listServerInterfacesUrl

- 95

def listServerInterfaces = new groovy.json.JsonSlurperClassic().parseText(listServerInterfacesResponse.content)

- 96

println listServerInterfaces.interfaceAttachments[0].fixed_ips[0].ip_address

- 97

asIpList.add(listServerInterfaces.interfaceAttachments[0].fixed_ips[0].ip_addres s)

- 98

- 99 }

- 100 }

- 101 asIpListGlobal = asIpList.join(",")

- 102

- 103


- 104 for(list in responseConent.members){

- 105

- 106 if(serverArr.contains(list.address)){

serverList = serverList.plus([id:list.id, address:list.address])

- 107

- 108 }

- 109 if(asIpList.contains(list.address)){

asServerList = asServerList.plus([id:list.id, address:list.address])

- 110

- 111 }

- 112 }

- 113

- 114

- 115 println("固定服务器IP: " + serverList)

- 116 println('弹性服务器:' + asServerList)

- 117

- 118 //修改权重为0

- 119 println("start 修改权重为0")

- 120 def weightReq = '''

- 121 {

- 122 "member": {

- 123 "weight": 0

- 124 }

- 125 }

- 126 '''

- 127

def weightReqBody = groovy.json.JsonOutput.toJson(new groovy.json.JsonSlurperClassic().parseText(weightReq))

- 128

- 129

- 130 for (int i = 0; i < serverList.size(); i++) {

- 131 echo "项⽬发布前修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+serverList[i ].id;

- 132

- 133 println weightUrl

- 134 println weightReqBody

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBody, customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: weightUrl

- 135

- 136 if(responseWeight.status){

- 137 println(serverList[i].address+"成功修改权重为0")

- 138

- 139 }else{


- 140 println(serverList[i].address+"修改权重为0失败")

- 141 }

- 142

- 143 }

- 144

- 145 println("end 修改权重为0")

- 146

- 147 }

- 148 }

- 149 }

- 150

- 151 stage('部署项⽬') {

- 152 steps {

- 153 script {

- 154

- 155 println("start 发布固定服务器")

- 156 for (int i = 0; i < serverList.size(); i++) {

- 157 echo "拷⻉jar包⾄服务器: ${serverList[i].address} "

- 158

- 159 sh """

- 160 echo "开始scp拷⻉jar包⾄服务器: ${serverList[i].address.trim()}"

scp /var/lib/jenkins/workspace/stats-web-pro/statsweb/target/*.war root@${serverList[i].address.trim()}:/home/xinwu/www/xwuadstats/wars

- 161

ssh -p 22 -o 'StrictHostKeyChecking=no' root@${serverList[i].address.trim()} '/home/xinwu/www/xwuad-stats/startup.sh'

- 162

- 163 """

- 164

// sshPublisher(publishers: [sshPublisherDesc(configName: "${serverList[i].address.trim()}", transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''#!/bin/bash

- 165

- 166 // source /etc/profile

- 167 // echo "开始启动项⽬"

- 168 // port=8080

// pid=$(netstat -nlp | grep :$port | awk '{print $7}' | awk F"/" '{ print $1 }');

- 169

- 170 // echo $pid

- 171 // # 杀掉对应的进程 如果PID不存在,即该端⼝没有开启,则不执⾏

- 172 // if [ -n "$pid" ]; then

- 173 // echo '调⽤tomcat命令停⽌项⽬'

- 174 // kill -9 $pid

- 175 // fi


- 176 // daemonize /home/xinwu/www/xwuad-stats/bin/startup.sh

// echo "启动项⽬完成" ''', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/www/xwuad-stats/wars', remoteDirectorySDF: false, removePrefix: 'stats-web/target/', sourceFiles: 'stats-web/target/*.war', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

- 177

- 178

- 179

- 180

- 181 }

- 182

- 183 println("end 发布固定服务器")

- 184

- 185 //发布弹性项⽬

- 186

- 187 if(AsServer=="true"){

- 188

- 189 println("start 发布弹性服务")

- 190 //修改权重为0

- 191 println("start 修改权重为0")

- 192

- 193 for (int i = 0; i < asServerList.size(); i++) {

- 194 echo "项⽬发布前修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+asServerList [i].id;

- 195

- 196 println weightUrl

- 197 println weightReqBody

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBody, customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: weightUrl

- 198

- 199 if(responseWeight.status){

- 200

- 201 println(asServerList[i].address+"成功修改权重为0")

- 202 echo "拷⻉jar包⾄服务器: ${imageHost} "

- 203

sshPublisher(publishers: [sshPublisherDesc(configName: "${imageHost}", transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: """#!/bin/bash

- 204

- 205

- 206 echo "开始scp拷⻉jar包⾄服务器: ${asServerList[i].address}"


- scp -P 52077 -o 'StrictHostKeyChecking=no' /home/xinwu/www/xwuad-stats/wars/stats-web.war root@${asServerList[i].address}:/home/xinwu/www/xwuad-stats/wars/
- 207

ssh -p 52077 -o 'StrictHostKeyChecking=no' root@${asServerList[i].address} 'kill -9 8080; daemonize /home/xinwu/www/xwuadstats/bin/startup.sh'

- 208

- 209 echo "${asServerList[i].address} 启动项⽬完成"

""", execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/www/xwuad-stats/wars', remoteDirectorySDF: false, removePrefix: 'stats-web/target/', sourceFiles: 'stats-web/target/*.war', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

- 210

- 211

- 212 }else{

- 213 println(asServerList[i].address+"修改权重为0失败")

- 214 }

- 215

- 216 }

- 217

- 218 println("end 修改权重为0")

- 219

- 220 //修改权重为10

- 221 for (int i = 0; i < asServerList.size(); i++) {

- 222 echo "项⽬发布后修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+asServerList [i].id;

- 223

- 224 println weightUrl

- 225 println weightReqBody

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBodyTen, customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: weightUrl

- 226

- 227 if(responseWeight.status){

- 228 println(asServerList[i].address+"成功修改权重为10")

- 229 }else{

- 230 println(asServerList[i].address+"修改权重为10失败")

- 231 }

- 232 }

- 233

- 234

- 235 println("end 修改权重为10")

- 236

- 237 println("end 发布弹性服务")

- 238


- 239 }else{

- 240

- 241 }

- 242

- 243

- 244 }

- 245 }

- 246 }

- 247

- 248 stage('流量上线') {

- 249 steps {

- 250 script {

- 251 //修改权重为10

- 252 println("start 修改权重为10")

- 253 def weightReqTen = '''

- 254 {

- 255 "member": {

- 256 "weight": 10

- 257 }

- 258 }

- 259 '''

def weightReqBodyTen = groovy.json.JsonOutput.toJson(new groovy.json.JsonSlurperClassic().parseText(weightReqTen))

- 260

- 261 for (int i = 0; i < serverList.size(); i++) {

- 262 echo "项⽬发布后修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+serverList[i ].id;

- 263

- 264 println weightUrl

- 265 println weightReqBodyTen

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBodyTen, customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: weightUrl

- 266

- 267 if(responseWeight.status){

- 268 println(serverList[i].address+"成功修改权重为10")

- 269 }else{

- 270 println(serverList[i].address+"修改权重为10失败")

- 271 }

- 272 }

- 273 println("end 修改权重为10")

- 274 }

- 275 }


- 276 }

- 277

- 278

- 279 stage('设置构建信息') {

- 280 steps {

- 281 script {

- 282 //设置buildName

- 283 wrap([$class: 'BuildUser']) {

- 284 buildName "#${BUILD_NUMBER} -- ${BUILD_USER}"

buildDescription " <span style='padding-left: 0px; fontsize: 10px;background:yellow ;color:black;font-weight:bold'> 分⽀: ${branch} <br/> 环境: ${environment} <br/> 发布固定机器: ${servers} <br/> 发布弹性机器: ${asIpListGlobal} <br/> 发布⼈: ${BUILD_USER_ID} </span> "

- 285

- 286 }

- 287 }

- 288 }

- 289 }

- 290

- 291 }

- 292 }

- 293


startup.sh

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

- 13 /home/xinwu/www/xwuad-stats/bin/shutdown.sh

- 14 else

- 15 echo '进程不在停⽌关闭命令并退出'

- 16 break

- 17 fi

- 18 sleep 5

- 19 done

- 20

- 21 echo '调⽤tomcat命令启动项⽬'

- 22 /home/xinwu/www/xwuad-stats/bin/startup.sh

- 23

- 24 echo "启动项⽬完成"

- 25


