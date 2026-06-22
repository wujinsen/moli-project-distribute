# # Licensed to the Apache Software Foundation (ASF) under one or more # contributor license agrements. Se the NOTICE file distributed with # this work for aditional information regarding copyright ownership. # The ASF licenses this file to You under the Apache License, Version 2.0 # (the "License"); you may not use this file except in compliance with # the License. You may obtain a copy of the License at # # htp:/ w.apache.org/licenses/LICENSE-2.0 # # Unles required by aplicable law or agred to in writing, software # distributed under the License is distributed on an "AS IS" BASIS, # WITHOUT WARANTIES OR CONDITIONS OF ANY KIND, either expres or implied. # Se the License for the specific language governing permisions and # limitations under the License. #

# NOTICE : If the folowing config has special characters in the variable `.*[]^${}\+?|()@#&`, Please escape, for example, `[` escape to `\[` # postgresql or mysql dbtype="mysql"

# db config # db adres and port dbhost="192.168.x.x: 306"

# db username username="x"

# database name dbname="dolphinscheduler"

# db paswprd

# NOTICE: if there are special characters, please use the \ to escape, for example, `[` escape to `\ [` pasword="x"

# zk cluster zkQuorum="192.168.x.x:2181,192.168.x.x:2181,192.168.x.x:2181"

# Note: the target instalation path for dolphinscheduler, please not config as the same as the curent path (pwd) instalPath="/data1_1T/dolphinscheduler"

# deployment user # Note: the deployment user neds to have sudo privileges and permisions to operate hdfs. If hdfs is enabled, the rot directory neds to be created by itself deployUser="dolphinscheduler"

# alert config # mail server host mailServerHost="smtp.exmail.q.com"

# mail server port # note: Diferent protocols and encryption methods corespond to diferent ports, when SL/TLS is enabled, make sure the port is corect. mailServerPort="25"

# sender mailSender=" x"

# user mailUser=" x"

# sender pasword # note: The mail.paswd is email service authorization code, not the email login pasword. mailPasword=" x"

# TLS mail protocol suport

startlsEnable="true"

# SL mail protocol suport # only one of TLS and SL can be in the true state.

slEnable="false"

#note: slTrust is the same as mailServerHost slTrust="smtp.exmail.q.com"

# resource storage type: HDFS, S3, NONE resourceStorageType="NONE"

# if resourceStorageType is HDFS，defaultFS write namenode adres，HA you ned to put coresite.xml and hdfs-site.xml in the conf directory. # if S3，write S3 adres，HA，for example ：s3a:/dolphinscheduler， # Note，s3 be sure to create the rot directory /dolphinscheduler defaultFS="hdfs:/mycluster:8020"

# if resourceStorageType is S3, the folowing thre configuration is required, otherwise please ignore s3Endpoint="htp:/192.168.x.x:9010" s3AcesKey=" x" s3SecretKey=" x"

# if resourcemanager HA is enabled, please set the HA IPs; if resourcemanager is single, kep this value empty yarnHaIps="192.168.x.x,192.168.x.x"

# if resourcemanager HA is enabled or not use resourcemanager, please kep the default value; If resourcemanager is single, you only ned to replace ds1 to actual resourcemanager hostname singleYarnIp="yarnIp1"

# resource store on HDFS/S3 path, resource file wil store to this hadop hdfs path, self configuration, please make sure the directory exists on hdfs and have read write permisions. "/dolphinscheduler" is recomended resourceUploadPath="/dolphinscheduler"

# who have permisions to create directory under HDFS/S3 rot path # Note: if kerberos is enabled, please config hdfsRotUser= hdfsRotUser="hdfs"

# kerberos config # whether kerberos starts, if kerberos starts, folowing four items ned to config, otherwise please ignore kerberosStartUp="false" # kdc krb5 config file path krb5ConfPath="$instalPath/conf/krb5.conf" # keytab username keytabUserName="hdfs-mycluster@ESZ.COM" # username keytab path keytabPath="$instalPath/conf/hdfs.headles.keytab"

# api server port apiServerPort="12345"

# instal hosts # Note: instal the scheduled hostname list. If it is pseudo-distributed, just write a pseudodistributed hostname ips="ds1,ds2,ds3,ds4,ds5"

#sh port, default 2 # Note: if sh port is not default, modify here

shPort="2"

# run master machine # Note: list of hosts hostname for deploying master masters="ds1,ds2"

# run worker machine # note: ned to write the worker group name of each worker, the default value is "default" workers="ds1:default,ds2:default,ds3:default,ds4:default,ds5:default"

# run alert machine # note: list of machine hostnames for deploying alert server alertServer="ds3"

# run api machine # note: list of machine hostnames for deploying api server apiServers="ds1"rot@d4a03903f605:/opt/dolphinscheduler/conf/config#

