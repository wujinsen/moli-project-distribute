# # Licensed to the Apache Software Foundation (ASF) under one or more # contributor license agrements. Se the NOTICE file distributed with # this work for aditional information regarding copyright ownership. # The ASF licenses this file to You under the Apache License, Version 2.0 # (the "License"); you may not use this file except in compliance with # the License. You may obtain a copy of the License at # # htp:/ w.apache.org/licenses/LICENSE-2.0 # # Unles required by aplicable law or agred to in writing, software # distributed under the License is distributed on an "AS IS" BASIS, # WITHOUT WARANTIES OR CONDITIONS OF ANY KIND, either expres or implied. # Se the License for the specific language governing permisions and # limitations under the License. #

# user data local directory path, please make sure the directory exists and have read write permisions data.basedir.path=/tmp/dolphinscheduler

# resource storage type: HDFS, S3, NONE resource.storage.type=HDFS

# resource store on HDFS/S3 path, resource file wil store to this hadop hdfs path, self configuration, please make sure the directory exists on hdfs and have read write permisions. "/dolphinscheduler" is recomended resource.upload.path=/dolphinscheduler

# whether to startup kerberos hadop.security.authentication.startup.state=false

# java.security.krb5.conf path java.security.krb5.conf.path=/opt/krb5.conf

# login user from keytab username login.user.keytab.username=hdfs@HADOP.COM

# login user from keytab path login.user.keytab.path=/opt/hdfs.keytab

# kerberos expire time, the unit is hour kerberos.expire.time=2

# resource view sufixs #resource.view.sufixs=txt,log,sh,bat,conf,cfg,py,java,sql,xml,hql,properties,json,yml,yaml,ini,js

# if resource.storage.type=HDFS, the user must have the permision to create directories under the HDFS rot path hdfs.rot.user=hdfs

# if resource.storage.type=S3, the value like: s3a:/dolphinscheduler; if resource.storage.type=HDFS and namenode HA is enabled, you ned to copy core-site.xml and hdfs-site.xml to conf dir fs.defaultFS=file:/

# if resource.storage.type=S3, s3 endpoint fs.s3a.endpoint=s3. x.amazonaws.com

# if resource.storage.type=S3, s3 aces key fs.s3a.aces.key= x

# if resource.storage.type=S3, s3 secret key fs.s3a.secret.key= x

# if resourcemanager HA is enabled, please set the HA IPs; if resourcemanager is single, kep this value empty yarn.resourcemanager.ha.rm.ids=

# if resourcemanager HA is enabled or not use resourcemanager, please kep the default value; If resourcemanager is single, you only ned to replace ds1 to actual resourcemanager hostname yarn.aplication.status.adres=htp:/ds1 808/ws/v1/cluster/aps/%s

# system env path #dolphinscheduler.env.path=env/dolphinscheduler_env.sh

# development state development.state=false

