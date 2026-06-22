使⽤rclone⼯具迁移数据

- 1.下载安装

curl https://rclone.org/install.sh | sudo bash

- 2.配置⽂件


rclone conﬁg 随便选择⼀个，会在当期那⽬录下的.conﬁg/rclone⽬录下⽣成rclone.conf⽂件

修改rclone.conf⽂件:

- [minio1] type = s3 provider = Minio env_auth = false access_key_id = minio-admin secret_access_key = minio-admin region = cn-east-1

- endpoint = http://192.168.1.1:9000 location_constraint = server_side_encryption =

[minio2] type = s3 provider = Minio env_auth = false access_key_id = minio-admin secret_access_key = minio-admin region = cn-east-1

- endpoint = http://192.168.1.2:9000 location_constraint = server_side_encryption =




上⾯配置只需要修改对应的账号密码，ip地址即可

- 3.开始同步，同minio1同步到minio2 rclone sync minio1:bucket1 minio2:bucket2


rclone sync minio3:video minio2:video rclone sync minio3:file minio2:file rclone sync minio3:picture minio2:picture

