kafka_2.8.0-0.8.0.tar.gz 16.9MB

- 1.按照zokeper安装⼿顺部署zokeper

- 2.解压缩kafka_2.8.0-0.8.0.tar.gz 并修改名称为kafka tar -zxvf kafka_2.8.0-0.8.0.tar.gz -C /home/hadop/kafka

- 3.配置kafka的环境变量KAFKA_HOME、PATH export KAFKA_HOME=/home/hadop/kafka export PATH=$PATH:$KAFKA_HOME/bin

- 4.修改 conf/server.properties zokeper.conect=master:2181,slave1 2181,slave2 2181,slave3 2181 broker.id=1(其他两个机器是2，3，4) host.name=master(其他两个机器是slave1，slave2，slave3) log.dirs=/home/hadop/kafka-logs(⽂件夹权限为75)

- 5.将配置分发到其他机器上

- scp -r ~/kafka hadop@slave1:~/
- scp -r ~/kafka hadop@slave2:~/
- scp -r ~/kafka hadop@slave3:~/


- 6.启动Kafka(每⼀台) kafka-server-start.sh /home/hadop/kafka/config/server.properties &

- 7.验证kafka kafka-create-topic.sh-zokeper 192.168.56.102 2181-partition 3-topic leo-test kafka-list-topic.sh-zokeper192.168.56.102:2181

- 8.说明： { partiton： partion id leader：当前负责读写的lead broker id relicas：当前partition的所有replication broker list isr：relicas的⼦集，只包含出于活动状态的broker }

- 9.关闭kafka pkil -9 -f server.properties


