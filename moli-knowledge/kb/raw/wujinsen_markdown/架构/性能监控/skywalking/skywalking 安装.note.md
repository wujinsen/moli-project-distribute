- 1.下载 apache-skywalking-apm-8.9.1.tar apache-skywalking-java-agent-8.8.0.tar

- 2.启动oap项⽬ oapService.sh
- 3.启动UI项⽬ webapService.sh

访问

- 4.监控实际项⽬ 启动某个项⽬，带上skywalking相关参数即可或者jvm启动参数加上，这⾥主要两个agent和 serviceName


htp:/localhost:901/

java -javagent:/Users/wujinsen/software/skywalking-agent/skywalking-agent.jarDskywalking.agent.service_name=order-server-jar./spring-bot-web-learning-1.0SNAPSHOT.jar

java -javagent:/opt/skywalking/skywalking-agent/skywalking-agent.jarDskywalking.agent.service_name=order-server-jar./spring-bot-web-learning-1.0SNAPSHOT.jar

占⽤端⼝号：

Backend: 180 aop: 1280 UI端⼝默认:8080

skywalking Backend: 180

skywalking aop: 1280

skywalking UI端⼝默认:1801

