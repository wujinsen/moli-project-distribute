- 1、下载Hadop安装包 1）登陆hadop.apache.org ⽹站进⼊⾸⻚，下拉找到Geting Started。点击Download


![image 1](<第三节：搭建Hadoop集群（废弃）.note_images/imageFile1.png>)

- 2）进⼊新的⻚⾯，找到 miror site。
- 3）进⼊新的⻚⾯，即下载⻚⾯。随意点击进⼊某个镜像站点
- 4）进⼊下载界⾯，点击某⼀个版本，进⾏下载。


![image 2](<第三节：搭建Hadoop集群（废弃）.note_images/imageFile2.png>)

![image 3](<第三节：搭建Hadoop集群（废弃）.note_images/imageFile3.png>)

htp:/apache.fayea.com/hadop/comon/

![image 4](<第三节：搭建Hadoop集群（废弃）.note_images/imageFile4.png>)

5）通过浏览器获取真实的下载URL

htp:/125.39.35.137/files/318 07139075/miror.bit.edu.cn/apache/hadop/comon/hado op-2.6.2/hadop-2.6.2.tar.gz

![image 5](<第三节：搭建Hadoop集群（废弃）.note_images/imageFile5.png>)

- 2、进⼊linux服务器，使⽤wget命令下载安装包。 wget
- 3、创建安装⽬录并解压⽂件 mkdir -p /export/servers mkdir -p /export/software mv hadop-2.6.2.tar.gz /export/software/ cd /export/software/ tar -zxvf hadop-2.6.2.tar.gz -C /export/servers/ cd /export/servers/ ln -s hadop-2.6.2 hadop
- 4、配置环境变量 export HADOP_HOME=/export/servers/hadop export PATH=${HADOP_HOME}/bin:$PATH
- 5、修改Hadop配置⽂件 第⼀个：hadop-env.sh 第⼆个：core-site.xml


htp:/125.39.35.137/files/318 07139075/miror.bit.edu.cn/apache/hadop/comon/ha dop-2.6.2/hadop-2.6.2.tar.gz

![image 6](<第三节：搭建Hadoop集群（废弃）.note_images/imageFile6.png>)

第三个：hdfs-site.xml 第四个：mapred-site.xml

第五个：yarn-site.xml

