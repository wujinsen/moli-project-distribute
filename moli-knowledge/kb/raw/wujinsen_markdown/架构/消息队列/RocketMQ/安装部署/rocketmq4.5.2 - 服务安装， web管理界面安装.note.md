Quick Start

This quick start guide is a detailed instruction of seting up RocketMQ mesaging system on your local machine to send and receive mesages. More Details:

English：htps:/github.com/apache/rocketmq/tre/master/docs/en

中⽂：htps:/github.com/apache/rocketmq/tre/master/docs/cn

ON THIS PAGE

PREREQUISITE DOWNLOAD&BUILDFROMRELEASE LINUX

START NAME SERVER START BROKER SEND & RECEIVE MESSAGES SHUTDOWN SERVERS

WINDOWS ADD ENVIRONMENT VARIABLES START NAME SERVER START BROKER SEND & RECEIVE MESSAGES

SEND MESSAGES RECEIVE MESSAGES

SHUTDOWN SERVERS

# Prerequisite

The folowing softwares are asumed instaled:

- 1.
- 2.
- 3.
- 4.
- 5.


64bit OS, Linux/Unix/Mac is recomended;(Windows user se guide below) 64bit JDK 1.8+; Maven 3.2.x; Git; 4g+ fre disk for Broker server

# Download & Build from Release

Click to download the 4.8.0 source release. Also you could download a binary release from . Now execute the folowing comands to unpack 4.8.0 source release and build the binary artifact.

here

here

> unzip rocketmq-all-4.8.0-source-release.zip > cd rocketmq-all-4.8.0/ > mvn -Prelease-all -DskipTests clean install -U > cd distribution/target/rocketmq-4.8.0/rocketmq-4.8.0

# Linux

## StartNameServer

> nohup sh bin/mqnamesrv & > tail -f ~/logs/rocketmqlogs/namesrv.log The Name Server boot success...

## StartBroker

> nohup sh bin/mqbroker -n localhost:9876 & > tail -f ~/logs/rocketmqlogs/broker.log The broker[%s, 172.30.30.233:10911] boot success...

Send&ReceiveMesages

Before sending/receiving mesages, we ned to tel clients the location of name servers. RocketMQ provides multiple ways to achieve this. For simplicity, we use environment variableNAMESRV_ADDR

> export NAMESRV_ADDR=localhost:9876 > sh bin/tools.sh org.apache.rocketmq.example.quickstart.Producer SendResult [sendStatus=SEND_OK, msgId= ...

> sh bin/tools.sh org.apache.rocketmq.example.quickstart.Consumer ConsumeMessageThread_%d Receive New Messages: [MessageExt...

## ShutdownServers

> sh bin/mqshutdown broker The mqbroker(36695) is running... Send shutdown request to mqbroker(36695) OK

> sh bin/mqshutdown namesrv The mqnamesrv(36664) is running... Send shutdown request to mqnamesrv(36664) OK

# Windows

The guide is working for windows 10 , please make sure you have powershel instaled. Download latest binary release. and extract zip file into your local disk. Such as:D:\rocketmq

## AdEnvironmentVariables

### You ned set environment variables

- 1.
- 2.
- 3.
- 4.
- 5.


From the desktop, right click the Computer icon. Chose Properties from the context menu. Click the Advanced system setings link. Click Environment Variables. Then ad or change Environment Variables.

ROCKETMQ_HOME="D:\rocketmq" NAMESRV_ADDR="localhost:9876"

Or just in the opening powershel, type the neded environment variables.

$Env:ROCKETMQ_HOME="D:\rocketmq" $Env:NAMESRV_ADDR="localhost:9876"

If you chose the powershel way. you should do it for every new open powershel window.

## StartNameServer

Open new powershel window, after set the corect environment variable. then change directory to rocketmq type and run:

.\bin\mqnamesrv.cmd

## StartBroker

Open new powershel window, after set the corect environment variable. then change directory to rocketmq type and run:

.\bin\mqbroker.cmd -n localhost:9876 autoCreateTopicEnable=true

## Send&ReceiveMesages

Send Mesages

Open new powershel window, after set the corect environment variable. then change directory to rocketmq type and run:

.\bin\tools.cmd org.apache.rocketmq.example.quickstart.Producer

Receive Mesages Then you wil se mesages produced. and now we can try consumer mesages. Open new powershel window, after set the corect environment variable. then change directory to rocketmq type and run:

.\bin\tools.cmd org.apache.rocketmq.example.quickstart.Consumer

## ShutdownServers

### Normaly, you can just closed these powershel windows. (Do not do it at production environment)

Updated: February 13, 2020

