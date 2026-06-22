1，在tomcat的官⽅⽹站上下载tomcat wget 2，下载

htp:/apache.fayea.com/tomcat/tomcat-8/v8.0.26/bin/apache-tomcat-8.0.26.tar.gz

完成之后，接下到instal⽬录下 tar -zxvf apache-tomcat-8.0.26.tar.gz -C./instal/3，进⼊tomcat的bin⽬录 cd./instal/apache-tomcat-8.0.26/bin4，启动tomcat

./startup.sh

![image 1](<第八节：Cent OS 安装Tomcat.note_images/imageFile1.png>)

- 5、查看当前服务器的ip地址： ifconfig
- 6、访问tomcat地址：192.168.0.129 8080 如果访问不了，请关闭防⽕墙。 7、关闭防⽕墙 service iptables stop service iptables status


![image 2](<第八节：Cent OS 安装Tomcat.note_images/imageFile2.png>)

8、再次访问tomcat

![image 3](<第八节：Cent OS 安装Tomcat.note_images/imageFile3.png>)

