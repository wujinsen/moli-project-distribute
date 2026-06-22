> unzip rocketmq-all-4.7.0-source-release.zip > cd rocketmq-all-4.7.0/ > mvn -Prelease-all -DskipTests clean install -U > cd distribution/target/rocketmq-4.7.0/rocketmq-4.7.0

#Start Name Server

> nohup sh bin/mqnamesrv & > tail -f ~/logs/rocketmqlogs/namesrv.log The Name Server boot success...

# Start broker

> nohup sh bin/mqbroker -n localhost:9876 & > tail -f ~/logs/rocketmqlogs/broker.log The broker[%s, 172.30.30.233:10911] boot success...

# Shutdown broker sh mqshutdown broker

# Start Nameserver

*Unix platform

`nohup sh mqnamesrv &`

# Shutdown Nameserver sh mqshutdown namesrv

# Update or create Topic sh mqadmin updateTopic -b 127.0.0.1 1091 -t TopicA

# Update or create subscription group sh mqadmin updateSubGroup -b 127.0.0.1 1091 -g SubGroupA

