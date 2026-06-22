import grovy.json.JsonSlurper final def branch = env.branch final def environment = env.environment final def asIpListGlobal pipeline { agent any stages {

- 1 stage('下载代码') {

- 2 steps {

- 3 echo "开始拉取代码,分⽀:${branch}"

- 4

- 5 checkout([$class: 'GitSCM', branches: [

- 6 [name: '*/${branch}']

- 7 ], extensions: [], userRemoteConfigs: [

[credentialsId: '6784a066-4ba6-4fcd-9671-ad4a3d8a3215', url: 'https://git.code.tencent.com/wujinsen/shushan-system-admin.git']

- 8

- 9 ]])

- 10

- 11 }

- 12 }

- 13 stage('编译打包') {

- 14 steps {

sh '${MAVEN_HOME}/bin/mvn clean package -U -D maven.test.skip=true -s ${MAVEN_HOME}/conf/shushan-setting.xml '

- 15

- 16 }

- 17 }

- 18

- 19

- 20 stage('部署项⽬') {

- 21

- 22 steps {

- 23

- 24 script {

- 25

- 26 def projectId = "1552922d99cd4940ac94fdcaa29b8ca6";

- 27 def poolId = "42a03452-0b15-4872-9c86-745db49982e8"; // 后端服务器组id

- 28 def imageHost = "172.16.1.186"; //123.249.98.192

// def scalingGroupId = "11f5b807-64a4-483a-8718-78672af846f8"; //弹 性伸缩组id

- 29

def scalingGroupId = "62c21f95-36b8-4ee9-8851-e6895fcdf1e3"; //弹性伸 缩组id

- 30

- 31 //1. 获取华为云token

- 32 def tokenReq = '''

- 33 {

- 34 "auth": {

- 35 "identity": {

- 36 "methods": [

- 37 "password"


- 38 ],

- 39 "password": {

- 40 "user": {

- 41 "domain": {

- 42 "name": "hid_e2hk8hwatanq6f2"

- 43 },

- 44 "name": "zongcheng",

- 45 "password": "ShuMiao@)!*0812"

- 46 }

- 47 }

- 48 },

- 49 "scope": {

- 50 "project": {

- 51 "name": "cn-north-4"

- 52 }

- 53 }

- 54 }

- 55 }

- 56 '''

def tokenUrl = "https://iam.myhuaweicloud.com/v3/auth/tokens? nocatalog=true"

- 57

def tokenJsonBody = groovy.json.JsonOutput.toJson(new groovy.json.JsonSlurperClassic().parseText(tokenReq))

- 58

- 59 println tokenJsonBody

def response = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'POST', requestBody: tokenJsonBody, url: tokenUrl

- 60

- 61 println('response status: '+response.status)

- 62 def responseToken = response.headers["X-Subject-Token"][0]

- 63

- 64 //查询后端云服务器列表，获取member_id

def membersUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members"

- 65

def responseMember = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'GET', customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: membersUrl

- 66

def responseConent = new groovy.json.JsonSlurperClassic().parseText(responseMember.content)

- 67

- 68 //查询弹性伸缩组中的实例列表

def listScalingUrl = "https://as.cn-north4.myhuaweicloud.com/autoscalingapi/v1/"+projectId+"/scaling_group_instance/"+scalingGroupId+"/list"

- 69

def responselistScalingInstances = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'GET', customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: listScalingUrl

- 70


- def responselistScalingInstancesContent = new groovy.json.JsonSlurperClassic().parseText(responselistScalingInstances.content)
- 71

- 72 def asServerList = []

- 73 def asIpList = []

- 74 def serverList = []

- 75 //选中的服务器IP

- 76 def serverArr = servers.tokenize('---')

- 77

if(responselistScalingInstancesContent.scaling_group_instances.size() > 0){

- 78

for(scalelist in responselistScalingInstancesContent.scaling_group_instances){

- 79

def listServerInterfacesUrl = "https://ecs.cn-north4.myhuaweicloud.com/v1/"+projectId+"/cloudservers/"+scalelist.instance_id+"/osinterface"

- 80

def listServerInterfacesResponse = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'GET', customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: listServerInterfacesUrl

- 81

def listServerInterfaces = new groovy.json.JsonSlurperClassic().parseText(listServerInterfacesResponse.content)

- 82

println listServerInterfaces.interfaceAttachments[0].fixed_ips[0].ip_address

- 83

asIpList.add(listServerInterfaces.interfaceAttachments[0].fixed_ips[0].ip_addres s)

- 84

- 85 }

- 86 }

- 87

- 88 asIpListGlobal = asIpList.join(",")

- 89 println('后端云服务器列表返回内容: ' + responseConent)

- 90

- 91 for(list in responseConent.members){

- 92

- 93 if(serverArr.contains(list.address)){

- 94 serverList = serverList.plus([id:list.id, address:list.address])

- 95 }

- 96 if(asIpList.contains(list.address)){

asServerList = asServerList.plus([id:list.id, address:list.address])

- 97

- 98 }

- 99 }

- 100

- 101

- 102


- 103 println("固定服务器IP: " + serverList)

- 104 println('弹性服务器:' + asServerList)

- 105

- 106 println("start 发布固定服务器")

- 107 //修改权重为0

- 108 println("start 修改权重为0")

- 109 def weightReq = '''

- 110 {

- 111 "member": {

- 112 "weight": 0

- 113 }

- 114 }

- 115 '''

def weightReqBody = groovy.json.JsonOutput.toJson(new groovy.json.JsonSlurperClassic().parseText(weightReq))

- 116

- 117 for (int i = 0; i < serverList.size(); i++) {

- 118 echo "项⽬发布前修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+serverList[i ].id;

- 119

- 120 println weightUrl

- 121 println weightReqBody

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBody, customHeaders: [ [name: "XAuth-Token", value: responseToken] ], url: weightUrl

- 122

- 123 if(responseWeight.status){

- 124

- 125 println(serverList[i].address+"成功修改权重为0")

- 126

- 127 echo "拷⻉jar包⾄服务器: ${serverList[i].address} "

sshPublisher(publishers: [sshPublisherDesc(configName: "${serverList[i].address.trim()}", transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''#!/bin/bash

- 128

- 129 source /etc/profile

- 130 echo "开始启动项⽬"

- 131 cd /home/xinwu/www/shushan-system-admin-server

- 132 pwd

- 133 echo "get helloworld"

- 134

- 135 ./startup.sh restart


- echo "启动项⽬完成"''', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/www/shushan-system-admin-server', remoteDirectorySDF: false, removePrefix: 'shushan-system-admin-server/target/', sourceFiles: 'shushan-system-admin-server/target/*.jar', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])
- 136

- 137

- 138 }else{

- 139 println(serverList[i].address+"修改权重为0失败")

- 140 }

- 141

- 142 }

- 143

- 144 println("end 修改权重为0")

- 145

- 146

- 147

- 148 //修改权重为10

- 149 println("start 修改权重为10")

- 150 def weightReqTen = '''

- 151 {

- 152 "member": {

- 153 "weight": 10

- 154 }

- 155 }

- 156 '''

- 157

def weightReqBodyTen = groovy.json.JsonOutput.toJson(new groovy.json.JsonSlurperClassic().parseText(weightReqTen))

- 158

- 159 for (int i = 0; i < serverList.size(); i++) {

- 160 echo "项⽬发布后修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+serverList[i ].id;

- 161

- 162 println weightUrl

- 163 println weightReqBody

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBodyTen, customHeaders: [ [name: "X-AuthToken", value: responseToken] ], url: weightUrl

- 164

- 165 if(responseWeight.status){

- 166 println(serverList[i].address+"成功修改权重为10")

- 167 }else{

- 168 println(serverList[i].address+"修改权重为10失败")


- 169 }

- 170 }

- 171

- 172

- 173

- 174 println("end 修改权重为10")

- 175

- 176 println("end 发布固定服务器")

- 177

- 178 //发布弹性项⽬

- 179

- 180 if(AsServer=="true"){

- 181

- 182 println("start 发布弹性服务")

- 183 //修改权重为0

- 184 println("start 修改权重为0")

- 185

- 186 for (int i = 0; i < asServerList.size(); i++) {

- 187 echo "项⽬发布前修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+asServerList [i].id;

- 188

- 189 println weightUrl

- 190 println weightReqBody

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBody, customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: weightUrl

- 191

- 192 if(responseWeight.status){

- 193

- 194 println(asServerList[i].address+"成功修改权重为0")

- 195 echo "拷⻉jar包⾄服务器: ${imageHost} "

- 196

sshPublisher(publishers: [sshPublisherDesc(configName: "${imageHost}", transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: """#!/bin/bash

- 197

- 198

- 199 echo "开始scp拷⻉jar包⾄服务器: ${asServerList[i].address}"

scp -P 52077 -o 'StrictHostKeyChecking=no' /home/xinwu/www/shushan-system-admin-server/shushan-system-admin-server-1.0.0SNAPSHOT.jar root@${asServerList[i].address}:/home/xinwu/www/shushan-systemadmin-server/

- 200

scp -P 52077 -o 'StrictHostKeyChecking=no' /home/xinwu/www/shushan-system-admin-server/startup.sh root@${asServerList[i].address}:/home/xinwu/www/shushan-system-admin-server/

- 201


- 202

ssh -p 52077 -o 'StrictHostKeyChecking=no' root@${asServerList[i].address} '/home/xinwu/www/shushan-system-adminserver/startup.sh restart'

- 203

- 204 echo "${asServerList[i].address} 启动项⽬完成"

""", execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '/home/xinwu/www/shushan-system-admin-server', remoteDirectorySDF: false, removePrefix: 'shushan-system-admin-server/target/', sourceFiles: 'shushansystem-admin-server/target/*.jar', usePty: true)], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: true)])

- 205

- 206

- 207 }else{

- 208 println(asServerList[i].address+"修改权重为0失败")

- 209 }

- 210

- 211 }

- 212

- 213 println("end 修改权重为0")

- 214

- 215 //修改权重为10

- 216 for (int i = 0; i < asServerList.size(); i++) {

- 217 echo "项⽬发布后修改权重"

def weightUrl = "https://elb.cn-north4.myhuaweicloud.com/v2/"+projectId+"/elb/pools/"+poolId+"/members/"+asServerList [i].id;

- 218

- 219 println weightUrl

- 220 println weightReqBody

def responseWeight = httpRequest contentType: 'APPLICATION_JSON', httpMode: 'PUT', requestBody: weightReqBodyTen, customHeaders: [ [name: "X-Auth-Token", value: responseToken] ], url: weightUrl

- 221

- 222 if(responseWeight.status){

- 223 println(asServerList[i].address+"成功修改权重为10")

- 224 }else{

- 225 println(asServerList[i].address+"修改权重为10失败")

- 226 }

- 227 }

- 228

- 229

- 230 println("end 修改权重为10")

- 231

- 232 println("end 发布弹性服务")

- 233

- 234 }else{


- 235

- 236 }

- 237

- 238

- 239 }

- 240 }

- 241 }

- 242

- 243 stage('设置构建信息') {

- 244 steps {

- 245 // ⾃定义设置构建历史显示的名称和描述信息

// 不同的部署⽅式设置构建历史显示的名称和描述信息⽅式不⼀样，根据⾃⼰的部署⽅式⾃⾏ 百度找到设置⽅法

- 246

- 247 script {

- 248

- 249 //设置buildName

- 250 wrap([$class: 'BuildUser']) {

- 251 buildName "#${BUILD_NUMBER} -- ${BUILD_USER}"

buildDescription " <span style='padding-left: 0px; fontsize: 10px;background:yellow ;color:black;font-weight:bold'> 分⽀: ${branch} <br/> 环境: ${environment} <br/> 发布固定机器: ${servers} <br/> 发布弹性机器: ${asIpListGlobal} <br/> 发布⼈: ${BUILD_USER_ID} </span> "

- 252

- 253 }

- 254 }

- 255 }

- 256 }

- 257


} }

