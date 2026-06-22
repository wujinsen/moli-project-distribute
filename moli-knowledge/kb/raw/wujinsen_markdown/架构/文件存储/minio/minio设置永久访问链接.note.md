https://www.cnblogs.com/f-society/p/14019352.html

minio设置永久访问链接

1.通过minio分享的链接只能⽀持7天。 解决⽅案是设置对应的bucket 可通过路径直接访问。（必须通过minio client才能设置下载策略） wget https://dl.min.io/client/mc/release/linux-amd64/mc //下载minio client chmod a+x mc

./mc conﬁg host add minio http://172.12.3.1:9999 admin passwd //添加minio server

./mc policy set download minio/yourbucket //设置需要开放下载的bucket, 注意需要带minio

http://172.16.3.1:9999/yourbucket/test.png //浏览器访问, 注意不需要带minio

