htps:/my.oschina.net/u/4397371/blog/4061035

这个⽅案采集⽇志有些问题： 容器内的⽇志，容器重启⽇志则会丢失，需要log-pilot将容器内⽇志持 久化到硬盘，在通过flume采集硬盘上的⽇志

利⽤ Log-Pilot + Kafka+Elasticsearch + Kibana 搭建 kubernetes⽇志解决⽅案

- 1、前提条件 已有kafka、elk、k8s集群，这3套集群搭建⽹上资料很多，这⾥不写，IP规划如下所示： kafka集群 10.6.11.22:9092 10.6.11.23:9092 10.6.11.24:9092

ELK集群 10.6.11.25:9200 10.6.11.26:9200 10.6.11.27:9200

k8s集群 10.6.11.28(master01) 10.6.11.29(master02) 10.6.11.30(master03) 10.6.11.31(node01) 10.6.11.32(node02)

- 2、log-pilot介绍 log-Pilot 是⼀个智能容器⽇志采集⼯具，它不仅能够⾼效便捷地将容器⽇志采集输出到多种存储⽇志后端，同时还能够动 态地发现和采集容器内部的⽇志⽂件。

针对前⾯提出的⽇志采集难题，Log-Pilot 通过声明式配置实现强⼤的容器事件管理，可同时获取容器标准输出和内部⽂件 ⽇志，解决了动态伸缩问题，此外，Log-Pilot 具有⾃动发现机制，CheckPoint 及句柄保持的机制，⾃动⽇志数据打 标，有效应对动态配置、⽇志重复和丢失以及⽇志源标记等问题。

⽬前 log-pilot 在 Github 完全开源，项⽬地址是 https://github.com/AliyunContainerService/logpilot 。您可以深⼊了解更多实现原理。

- 3、⽇志收集系统架构


![image 1](<利用 Log-Pilot + Kafka + Elasticsearch + Kibana 搭建 kubernetes日志解决方案.note_images/imageFile1.png>)

# 4、log-pilot部署 阿⾥提供的例⼦是把⽇志输出给es，这⾥因为使⽤了kafka，所以部署的yaml较官⽅的来说，有⼀点点 的变化，如下所示：

apiVersion: extensions/v1beta1 kind: DaemonSet metadata:

name: log-pilot labels:

k8s-app: log-pilot namespace: kube-system spec:

updateStrategy:

type: RollingUpdate template:

metadata: labels:

k8s-app: log-pilot spec:

tolerations:

- key: node-role.kubernetes.io/master

effect: NoSchedule containers:

- name: log-pilot

image: registry.cn-hangzhou.aliyuncs.com/acs/log-pilot:0.9.5-filebeat #没⽤最新镜像， 是因为为了收集多⾏⽇志，需要修改log-pilot的源码，最新的镜像测试修改完后，pod⽆法启动，所以就放弃了，这个版本 测试没有问题，修改配置会在下⾯介绍

env:

- - name: "LOGGING_OUTPUT" value: "kafka" #输出到kafka，官⽅的例⼦是输出到es

- - name: "KAFKA_BROKERS" #和官⽅不⼀致的地⽅ value: "10.6.11.22:9092;10.6.11.23:9092;10.6.11.24:9092" #kafka地址

- - name: "NODE_NAME" valueFrom:


fieldRef:

fieldPath: spec.nodeName volumeMounts:

- - name: sock mountPath: /var/run/docker.sock

- - name: logs mountPath: /var/log/filebeat

- - name: state mountPath: /var/lib/filebeat

- - name: root mountPath: /host readOnly: true

- - name: localtime mountPath: /etc/localtime


securityContext:

capabilities: add:

- SYS_ADMIN terminationGracePeriodSeconds: 30 volumes:

- - name: sock hostPath:

path: /var/run/docker.sock

- - name: logs hostPath:

path: /var/log/filebeat

- - name: state hostPath:

path: /var/lib/filebeat

- - name: root hostPath:

path: /

- - name: localtime hostPath:


path: /etc/localtime

- 5、配置服务的yaml⽂件


apiVersion: apps/v1 kind: Deployment metadata:

name: accounting namespace: kube-ops labels:

app: accounting

spec: minReadySeconds: 30 strategy:

type: RollingUpdate rollingUpdate:

maxSurge: 1 maxUnavailable: 0

revisionHistoryLimit: 9 selector:

matchLabels:

app: accounting replicas: 1 template:

metadata: labels:

app: accounting spec:

containers:

- name: accounting image: test-accounting:v2 imagePullPolicy: Always ports:

- - containerPort: 8080 env:

- - name: aliyun_logs_info #当然如果你不想使⽤aliyun这个关键字，Log-Pilot 也提供了环境变量


PILOT_LOG_PREFIX可以指定⾃⼰的声明式⽇志配置前缀，⽐如 PILOT_LOG_PREFIX: "aliyun,custom"，最好是和官 ⽅⼀致，省去多余的配置

value: /data/home/logs/accounting/accounting.log #需要收集的⽇志路径

- name: aliyun_logs_info_tags #定义⼀个tag

value: "topic=k8s-accounting-info" #kafka topic的名字，这个定义是关 键，不定义这个，⽇志是⽆法输出到kafka内的

volumeMounts:

- name: accounting-log

mountPath: /data/home/logs/accounting volumes:

- name: accounting-log emptyDir: {}

- 6、利⽤logstash消费kafka内的数据


filter { if [topic] =~ "k8s-accounting-info" {

mutate { remove_field => ["input","beat","prospector","logmsg","log","thread","class"]

} }

}

output { if [topic] =~ "k8s-accounting-info" {

elasticsearch { hosts => ["10.6.11.25:9200","10.6.11.26:9200","10.6.11.27:9200"] user => "elastic" password => "密码" index => "k8s-accounting-info-%{+YYYY.MM.dd}" #按⽇期⽣成索引

} }

}

- 7、修改log-pilot源码使其可以收集多⾏⽇志(以⽇期开头，刑如2020-02-29)


- 7.1 拉取v0.9.5这个tag的代码 git clone https://github.com/AliyunContainerService/log-pilot.git cd log-pilot git tag git checkout v0.9.5 #指定v0.9.5这个版本

- 7.2 修改filebeat模板


vim log-pilot/assets/filebeat/filebeat.tpl

{{range .configList}}

- type: log enabled: true paths:

- {{ .HostDir }}/{{ .File }} multiline.pattern: '^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' #新增正则条件，以⽇期开头 multiline.negate: true #新增 multiline.match: after #新增 multiline.max_lines: 10000 #新增 scan_frequency: 10s fields_under_root: true {{if .Stdout}} docker-json: true {{end}} {{if eq .Format "json"}} json.keys_under_root: true

{{end}} fields:

{{range $key, $value := .Tags}} {{ $key }}: {{ $value }} {{end}} {{range $key, $value := $.container}} {{ $key }}: {{ $value }} {{end}}

tail_files: false close_inactive: 2h close_eof: false close_removed: true clean_removed: true close_renamed: false

{{end}}

- 7.3 重新打包镜像 cd log-pilot/ && ./build-image.sh


打包成功后，镜像打tag ，并push到私有仓库 docker tag 原镜像名称 新镜像名称 docker push 新镜像名称

- 8 、最后展示⼀张kibana收集⽇志的图例


![image 2](<利用 Log-Pilot + Kafka + Elasticsearch + Kibana 搭建 kubernetes日志解决方案.note_images/imageFile2.png>)

- 9、参考博客 https://help.aliyun.com/document_detail/86552.html https://github.com/AliyunContainerService/log-pilot/issues/101 https://www.iyunw.cn/archives/k8s-tong-guo-log-pilot-cai-ji-ying-yong-ri-zhi-ding-zhi-huatomcat-duo-xing/


kuberneteselasticsearchkafkakibanayaml

htps:/ w.cnblogs.com/uglyliu/p/1238214.html

本⽂转载⾃：

