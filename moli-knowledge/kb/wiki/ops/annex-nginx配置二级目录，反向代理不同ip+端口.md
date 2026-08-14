---
title: nginx配置二级目录，反向代理不同ip+端口.note（原文插图 annex）
slug: annex-nginx配置二级目录，反向代理不同ip+端口
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/DevOps/nginx/nginx配置二级目录，反向代理不同ip+端口.note.md
related: [jenkins-ci入门]
created: 2026-07-05
updated: 2026-07-05
---

htps:/ w.cnblogs.com/bayu/p/8041453.html

场景描述： 通过⼆级⽬录（虚拟⽬录，应⽤程序）的⽅式访问同⼀ip+端⼝的不同应⽤，例如location是⽤户使⽤ ⻚⾯，location/admin/是管理⻚⾯，location部署在192.168.1.10的80端⼝，location/admin部署在 172.20.1.32的8080端⼝上。 解决⽅案： 使⽤nginx反向代理，配置如下：

server {

listen 80; server_name demo.domain.com; #通过访问service⼆级⽬录来访问后台

location /service { #DemoBackend1后⾯的斜杠是⼀个关键，没有斜杠的话就会传递service到后端节点导致404 proxy_pass http://DemoBackend1/; proxy_redirect off; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

} #其他路径默认访问前台⽹站 location / {

proxy_pass http://DemoBackend2; proxy_redirect off; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

} }

#简单的负载均衡节点配置 upstream DemoBackend1 {

- server 192.168.1.1;

- server 192.168.1.2; ip_hash;


} upstream DemoBackend2 {

- server 192.168.2.1;

- server 192.168.2.2; ip_hash;


}

<table>
  <tr>
    <th>![image 1](assets/imageFile1.png)</th>
  </tr>
</table>


但是这种⽅式，⼆级⽬录的样式⽂件都不会正常显示，他们不会⾃动在⼆级⽬录下查找，⽽是在根⽬ 录中查找，在跳转⻚⾯的时候也会报404错误。不知道是不是配置有误，在server块中配置了rot或是 rewrite都不能解决。 试着在proxy_pas后⾯加上⼆级⽬录，并且和location块的⼆级⽬录相同，配置如下：

<table>
  <tr>
    <th>![image 2](assets/imageFile2.png)</th>
  </tr>
</table>


server {

listen 80; server_name demo.domain.com; #通过访问service⼆级⽬录来访问后台

location /service { #DemoBackend1后⾯的斜杠是⼀个关键，没有斜杠的话就会传递service到后端节点导致404 proxy_pass http://DemoBackend1/service;#DemoBackend1⽹站中要配置⼀个名称为service的虚拟

⽬录，并且和location的⼆级⽬录名称⼀致 proxy_redirect off; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

} #其他路径默认访问前台⽹站 location / {

proxy_pass http://DemoBackend2; proxy_redirect off; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

} }

#简单的负载均衡节点配置 upstream DemoBackend1 {

- server 192.168.1.1;

- server 192.168.1.2; ip_hash;


} upstream DemoBackend2 {

- server 192.168.2.1;

- server 192.168.2.2; ip_hash;


}

<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


问题解决 另外，在实际应⽤中，我使⽤了asp.net 的mvc，将mvc设置为⽹站的⽅式没有问题，如果是虚拟⽬录 的⽅式就会找不到路径，是因为⾃⼰在⽹站中的地址很多写的都不规范，正确的⽅式应该是： Here's a typical example of what you should never do: <script type="text/javascript">

$.ajax({ url: '/home/index'

}); </script> and here's how this should be done: <script type="text/javascript">

$.ajax({ url: '@Url.Action("index", "home")'

}); </script> Here's another typical example of something that you should never do:

<a href="/home/index">Foo</a> and here's how this should be writen: @Html.ActionLink("Foo", "Index", "Home")

Here's another example of something that you should never do: <form action="/home/index" method="opst">

</form> and here's how this should be writen: @using (Html.BeginForm("Index", "Home")) {

}

引⽤： https://zhangge.net/5054.html http://blog.csdn.net/lusyoe/article/details/52928649
