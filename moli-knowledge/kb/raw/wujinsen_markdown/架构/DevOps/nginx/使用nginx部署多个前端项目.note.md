### htps:/ w.cnblogs.com/zhaoxnbsp/p/12691398.html

# 使⽤nginx部署多个前端项⽬

个⼈总结了3种⽅法来实现在⼀台服务器上使⽤nginx部署多个前端项⽬的⽅法。

基于域名配置 基于端⼝配置 基于location配置

在正式开始之前，我们先来看⼀下nginx安装的默认配置⽂件： /etc/nginx/nginx.conf ⽂件

![image 1](<使用nginx部署多个前端项目.note_images/imageFile1.png>)

可以看到图中的：include /usr/nginx/modules/*.conf，这句话的作⽤就是可以在nginx启动加载所有 /usr/nginx/modules/ ⽬录下的 *.conf ⽂件。 所以，平时我们为了⽅便管理，可以在此⽬录下⾯定义⾃⼰的

x.conf ⽂件即可。但是注意，⼀定要以.conf 结尾。 介绍完毕，下⾯我们先来说⼀下最常⽤，也是许多公司线上使⽤的⽅式。

## 基于域名配置

基于域名配置，前提是先配置好了域名解析。⽐如说你⾃⼰买了⼀个域名： w.fly.com。 然后你在后台配 置了2个它的⼆级域名： a.fly.com、 b.fly.com。 配置⽂件如下：

配置 a.fly.com 的配置⽂件：

- vim /usr/nginx/modules/a.conf


listen 80; server_name a.fly.com;

location / { root /data/web-a/dist; index index.html;

} }

配置 b.fly.com 的配置⽂件：

- vim /usr/nginx/modules/b.conf


server {

listen 80; server_name b.fly.com;

location / { root /data/web-b/dist; index index.html;

}

} ⼀个IP多个域名配置 ：

server {

listen 80; server_name b.fly.com;

location / {

proxy_pas htp:/127.0.0.1 808; proxy_set_header Host $htp_host; proxy_set_header X-Real-IP $remote_adr; proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for;

}

} ⼀个IP多个域名配置 ：

server {

listen 80; server_name a.fly.com;

location / {

proxy_pas htp:/127.0.0.1 808; proxy_set_header Host $htp_host;

proxy_set_header X-Real-IP $remote_adr; proxy_set_header X-Forwarded-For $proxy_ad_x_forwarded_for;

} }

这种⽅式的好处是，主机只要开放80端⼝即可。然后访问的话直接访问⼆级域名就可以访问。

## 基于端⼝配置

配置⽂件如下：

- 配置 a.fly.com 的配置⽂件：

- 配置 b.fly.com 的配置⽂件：


- vim /usr/nginx/modules/a.conf

server {

listen 8000;

location / { root /data/web-a/dist; index index.html;

} }

- # nginx 80端 ⼝ 配 置 （ 监 听 a⼆ 级 域 名 ） server {


listen 80; server_name a.fly.com;

location / {

proxy_pass http://localhost:8000; #转 发 }

}

- vim /usr/nginx/modules/b.conf


listen 8001;

location / { root /data/web-b/dist; index index.html;

} }

- # nginx 80端 ⼝ 配 置 （ 监 听 b⼆ 级 域 名 ） server {


listen 80; server_name b.fly.com;

location / {

proxy_pass http://localhost:8001; #转 发 }

}

可以看到，这种⽅式⼀共启动了4个server，⽽且配置远不如第⼀种简单，所以不推荐。

## 基于location配置

配置⽂件如下：

配置 a.fly.com 的配置⽂件：

vim /usr/nginx/modules/ab.conf

server {

listen 80;

location / { root /data/web-a/dist; index index.html;

}

location /web-b { alias /data/web-b/dist; index index.html;

}

} 注意： 这种⽅式配置的话，location / ⽬录是rot，其他的要使⽤alias。 可以看到，这种⽅式的好处就是我们只有⼀个server，⽽且我们也不需要配置⼆级域名。并且前端项⽬⾥要配 置⼆级⽬录

react 配置请参考：

htps:/blog.csdn.net/molerlala/article/details/9642751?depth_1-utm_source=distrib ute.pc_relevant.none-task-blog-BlogComendFromBaidu-2&utm_source=distribute.pc_relevant.none-t ask-blog-BlogComendFromBaidu-2

vue 配置请参考：

htps:/blog.csdn.net/weixin_3868027/article/details/92139392 返回顶部↑

本⽂作者：直⻆漫步 本⽂链接：htps:/ w.cnblogs.com/zhaoxnbsp/p/12691398.html 版权声明：本作品采⽤知识共享署名-⾮商业性使⽤-禁⽌演绎 2.5 中国⼤陆许可协议进⾏许可。

