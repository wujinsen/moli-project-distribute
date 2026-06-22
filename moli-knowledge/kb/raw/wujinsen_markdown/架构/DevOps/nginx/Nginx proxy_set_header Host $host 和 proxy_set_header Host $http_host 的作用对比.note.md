htps:/ w.jianshu.com/p/7a8a7eb3707a

- 1、浏览器直接访问服务，获取到的 Host 包含浏览器请求的 IP 和端⼝

# cat ngx_header.py from flask import Flask, request, jsonify ap = Flask(_name_)

@ap.route('/') def get_host():

host = request.headers.get('Host') return jsonify({'Host': host}), 20

if _name_ = '_main_': ap.run(host='10.1.20.107', port=5 0)

# python ngx_header.py 结果如下：

- 2、配置 nginx 代理服务后


- 2.1 不设置 proxy_set_header Host 时，浏览器直接访问 nginx，获取到的 Host 是 proxy_pas 后⾯的 值，即 $proxy_host 的值，参考 htp:/nginx.org/en/docs/htp/ngx_htp_proxy_module.html#proxy_set_header # cat ngx_header.conf server {


listen 8090; server_name _; location / {

proxy_pas htp:/10.1.20.107 5 0; }

}

结果如下：

![image 1](<Nginx proxy_set_header Host $host 和 proxy_set_header Host $http_host 的作用对比.note_images/imageFile1.png>)

- 2.2 设置 proxy_set_header Host $host 时，浏览器直接访问 nginx，获取到的 Host 是 $host 的值， 没有端⼝信息

# cat ngx_header.conf server {

listen 8090; server_name _; location / {

proxy_set_header Host $host; proxy_pas htp:/10.1.20.107 5 0;

}

} 结果如下：

- 2.3 设置 proxy_set_header Host $host:$proxy_port 时，浏览器直接访问 nginx，获取到的 Host 是 $host:$proxy_port 的值


![image 2](<Nginx proxy_set_header Host $host 和 proxy_set_header Host $http_host 的作用对比.note_images/imageFile2.png>)

# cat ngx_header.conf server {

listen 8090; server_name _; location / {

proxy_set_header Host $host:$proxy_port; proxy_pas htp:/10.1.20.107 5 0;

} }

结果如下：

![image 3](<Nginx proxy_set_header Host $host 和 proxy_set_header Host $http_host 的作用对比.note_images/imageFile3.png>)

- 2.4 设置 proxy_set_header Host $htp_host 时，浏览器直接访问 nginx，获取到的 Host 包含浏览器 请求的 IP 和端⼝

server { listen 8090; server_name _; location / {

proxy_set_header Host $htp_host; proxy_pas htp:/10.1.20.107 5 0;

}

} 结果如下：

- 2.5 设置 proxy_set_header Host $host 时，浏览器直接访问 nginx，获取到的 Host 是 $host 的值， 没有端⼝信息。此时代码中如果有重定向路由，那么重定向时就会丢失端⼝信息，导致 404


![image 4](<Nginx proxy_set_header Host $host 和 proxy_set_header Host $http_host 的作用对比.note_images/imageFile4.png>)

# tre .

. ├── ngx_header.py └── templates

├── bar.html └── fo.html

1 directory, 3 files

/ ngx_header.py 代码 # cat ngx_header.py from flask import Flask, request, render_template, redirect ap = Flask(_name_)

@ap.route('/') def get_header():

host = request.headers.get('Host') return render_template('fo.html',Host=host)

@ap.route('/bar')

- def get_header2(): host = request.headers.get('Host') return render_template('bar.html',Host=host)

@ap.route('/2bar')

- def get_header3(): # 代码层实现的重定向 return redirect('/bar')


if _name_ = '_main_': ap.run(host='10.1.20.107', port=5 0)

/ fo.html 代码 # cat templates/fo.html <!DOCTYPE html> <html lang="en"> <head>

<meta charset="UTF-8"> <title>fo</title>

</head> <body> Host: { Host } </br> <a href="2bar">⻚⾯跳转</a> </body> </html>

/ bar.html 代码 # cat templates/bar.html <!DOCTYPE html> <html lang="en"> <head>

<meta charset="UTF-8"> <title>bar</title>

</head> <body> Host: { Host } </body> </html>

# python ngx_header.py

# cat ngx_header.conf server {

listen 8090; server_name _; location / {

proxy_set_header Host $host; proxy_pas htp:/10.1.20.107 5 0;

}

} 结果如下：

![image 5](<Nginx proxy_set_header Host $host 和 proxy_set_header Host $http_host 的作用对比.note_images/imageFile5.png>)

![image 6](<Nginx proxy_set_header Host $host 和 proxy_set_header Host $http_host 的作用对比.note_images/imageFile6.png>)

