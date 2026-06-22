- 1 import groovy.json.JsonSlurper

- 2 final def branch = env.branch

- 3 final def environment = env.environment

- 4 def asIpListGlobal

- 5 final def projectId = "1552922d99cd4940ac94fdcaa29b8ca6";

- 6 final def poolId = "a2d99c80-a3fd-49b9-91db-a2045e5dce87"; // 后端服务器组id

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

sh '${MAVEN_HOME}/bin/mvn clean package -U -D maven.test.skip=true -s ${MAVEN_HOME}/conf/shushan-setting.xml '

- 33

- 34

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

sshPublisher(publishers: [sshPublisherDesc(configName: "${serverList[i].address.trim()}", transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''#!/bin/bash

- 158

- 159 source /etc/profile

- 160 echo "开始启动项⽬"

- 161 port=8080

pid=$(netstat -nlp | grep :$port | awk '{print $7}' | awk -F"/" '{ print $1 }');

- 162

- 163 echo $pid

- 164 # 杀掉对应的进程 如果PID不存在,即该端⼝没有开启,则不执⾏

- 165 if [ -n "$pid" ]; then

- 166 echo '调⽤tomcat命令停⽌项⽬'

- 167 kill -9 $pid

- 168 fi

- 169 daemonize /home/xinwu/www/xwuad-stats/bin/startup.sh

echo "启动项⽬完成" ''', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/www/xwuad-stats/wars', remoteDirectorySDF: false, removePrefix: 'stats-web/target/', sourceFiles: 'stats-web/target/*.war', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

- 170

- 171 println("end 发布固定服务器")

- 172 }

- 173

- 174


- 175

- 176 //发布弹性项⽬

- 177

- 178 if(AsServer=="true"){

- 179

- 180 println("start 发布弹性服务")

- 181 //修改权重为0

- 182 println("start 修改权重为0")

- 183

- 184 for (int i = 0; i < asServerList.size(); i++) {

- 185 echo "项⽬发布前修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+asServerList [i].id;

- 186

- 187 println weightUrl

- 188 println weightReqBody

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBody, customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: weightUrl

- 189

- 190 if(responseWeight.status){

- 191

- 192 println(asServerList[i].address+"成功修改权重为0")

- 193 echo "拷⻉jar包⾄服务器: ${imageHost} "

- 194

sshPublisher(publishers: [sshPublisherDesc(configName: "${imageHost}", transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: """#!/bin/bash

- 195

- 196

- 197 echo "开始scp拷⻉jar包⾄服务器: ${asServerList[i].address}"

scp -P 52077 -o 'StrictHostKeyChecking=no' /home/xinwu/www/xwuad-stats/wars/stats-web.war root@${asServerList[i].address}:/home/xinwu/www/xwuad-stats/wars/

- 198

ssh -p 52077 -o 'StrictHostKeyChecking=no' root@${asServerList[i].address} 'kill -9 8080; daemonize /home/xinwu/www/xwuadstats/bin/startup.sh'

- 199

- 200 echo "${asServerList[i].address} 启动项⽬完成"

""", execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/www/xwuad-stats/wars', remoteDirectorySDF: false, removePrefix: 'stats-web/target/', sourceFiles: 'stats-web/target/*.war', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

- 201

- 202

- 203 }else{

- 204 println(asServerList[i].address+"修改权重为0失败")

- 205 }


- 206

- 207 }

- 208

- 209 println("end 修改权重为0")

- 210

- 211 //修改权重为10

- 212 for (int i = 0; i < asServerList.size(); i++) {

- 213 echo "项⽬发布后修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+asServerList [i].id;

- 214

- 215 println weightUrl

- 216 println weightReqBody

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBodyTen, customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: weightUrl

- 217

- 218 if(responseWeight.status){

- 219 println(asServerList[i].address+"成功修改权重为10")

- 220 }else{

- 221 println(asServerList[i].address+"修改权重为10失败")

- 222 }

- 223 }

- 224

- 225

- 226 println("end 修改权重为10")

- 227

- 228 println("end 发布弹性服务")

- 229

- 230 }else{

- 231

- 232 }

- 233

- 234

- 235 }

- 236 }

- 237 }

- 238

- 239 stage('流量上线') {

- 240 steps {

- 241 script {

- 242 //修改权重为10

- 243 println("start 修改权重为10")


- 244 def weightReqTen = '''

- 245 {

- 246 "member": {

- 247 "weight": 10

- 248 }

- 249 }

- 250 '''

def weightReqBodyTen = groovy.json.JsonOutput.toJson(new groovy.json.JsonSlurperClassic().parseText(weightReqTen))

- 251

- 252 for (int i = 0; i < serverList.size(); i++) {

- 253 echo "项⽬发布后修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+serverList[i ].id;

- 254

- 255 println weightUrl

- 256 println weightReqBodyTen

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBodyTen, customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: weightUrl

- 257

- 258 if(responseWeight.status){

- 259 println(serverList[i].address+"成功修改权重为10")

- 260 }else{

- 261 println(serverList[i].address+"修改权重为10失败")

- 262 }

- 263 }

- 264 println("end 修改权重为10")

- 265 }

- 266 }

- 267 }

- 268

- 269

- 270 stage('设置构建信息') {

- 271 steps {

- 272 script {

- 273 //设置buildName

- 274 wrap([$class: 'BuildUser']) {

- 275 buildName "#${BUILD_NUMBER} -- ${BUILD_USER}"

buildDescription " <span style='padding-left: 0px; fontsize: 10px;background:yellow ;color:black;font-weight:bold'> 分⽀: ${branch} <br/> 环境: ${environment} <br/> 发布固定机器: ${servers} <br/> 发布弹性机器: ${asIpListGlobal} <br/> 发布⼈: ${BUILD_USER_ID} </span> "

- 276

- 277 }

- 278 }


- 279 }

- 280 }

- 281

- 282 }

- 283 }

- 284


