- 1.下载linux minio
- 2.启动 chmod +x minio

./minio server /home/develop/opt/software/minio

- 3.进⼊控制台 http://192.168.2.13:9000 RootUser: minioadmin RootPass: minioadmin


http://dl.minio.org.cn/server/minio/release/linux-amd64/minio

API: http://192.168.2.13:9000 http://192.168.122.1:9000 http://127.0.0.1:9000 RootUser: minioadmin RootPass: minioadmin

Console: http://192.168.2.13:33197 http://192.168.122.1:33197 http://127.0.0.1:33197 RootUser: minioadmin RootPass: minioadmin

Command-line: https://docs.min.io/docs/minio-client-quickstart-guide $ mc alias set myminio http://192.168.2.13:9000 minioadmin minioadmin

Documentation:

https://docs.min.io

linux服务器开发console端⼝9001

./minio server ./minioData --address 0.0.0.0:9000 --console-address 0.0.0.0:9001

nohup ./minio server /opt/minio/minioData --address 123.57.242.120:9000 --consoleaddress 123.57.242.120:9001 &

nohup ./minio server /opt/minio/minioData --address 0.0.0.0:9000 --console-address 0.0.0.0:9001 &

./minio server /opt/minio/minioData --address 49.233.58.141:9000 --console-address 49.233.58.141:9001

./minio server /opt/minioData --address 49.233.58.141:9000 --console-address 49.233.58.141:9001

./minio server /opt/minioData --address 0.0.0.0:9000 --console-address 0.0.0.0:9001

export MINIO_ACCESS_KEY=minioadmin export MINIO_SECRET_KEY=QNX}}zWn4r

新版本安装路径:

./minio server ./mnt/data --address 0.0.0.0:9000 --console-address 0.0.0.0:9001

MINIO_ROT_USER=minio-admin export MINIO_ROT_PASWORD=69vAL4Wl

