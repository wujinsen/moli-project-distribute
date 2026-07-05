---
title: 前端解决跨域问题的8种方案.note（原文插图 annex）
slug: annex-前端解决跨域问题的8种方案
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/前端/前端解决跨域问题的8种方案.note.md
related: [前端基础面试题]
created: 2026-07-05
updated: 2026-07-05
---

# 1.同源策略如下：

<table>
  <tr>
    <th> </th>
    <th>说明</th>
    <th>是否允许通信</th>
  </tr>
  <tr>
    <td>URL t / w.a. /a.j</td>
    <td>同⼀域名下</td>
    <td>允许</td>
  </tr>
  <tr>
    <td>htp:/ w.a.com/b.js t / w.a. /lab/a.js</td>
    <td>同⼀域名下不同⽂件夹</td>
    <td>允许</td>
  </tr>
  <tr>
    <td>htp:/ w.a.com/script/b.js t:/ w.a. :8 0/a.js</td>
    <td>同⼀域名，不同端⼝</td>
    <td>不允许</td>
  </tr>
  <tr>
    <td>htp:/ w.a.com/b.js t:/ w.a.com/a.js</td>
    <td>同⼀域名，不同协议</td>
    <td>不允许</td>
  </tr>
  <tr>
    <td>htps:/ w.a.com/b.js t / w.a.com/a.js</td>
    <td>域名和域名对应ip</td>
    <td>不允许</td>
  </tr>
  <tr>
    <td>htp:/70.32.92.74/b.js t / w.a.com/a.js</td>
    <td>主域相同，⼦域不同</td>
    <td>不允许</td>
  </tr>
  <tr>
    <td>htp:/script.a.com/b.js<br><br>t / w.a.com/a.js htp:/a.com/b.js</td>
    <td>同⼀域名，不同⼆级域名（同 上）</td>
    <td>不允许（cokie这种情况下也不 允许访问）</td>
  </tr>
  <tr>
    <td>t / w.cnblogs.com/a.js</td>
    <td>不同域名</td>
    <td>不允许</td>
  </tr>
</table>


htp:/ w.a.com/b.js

特别注意两点： 第⼀，如果是协议和端⼝造成的跨域问题“前台”是⽆能为⼒的， 第⼆：在跨域问题上，域仅仅是通过“URL的⾸部”来识别⽽不会去尝试判断相同的ip地址对应着两个域 或两个域是否在同⼀个ip上。 “URL的 ⾸ 部 ”指 window.location.protocol +window.location.host， 也 可 以 理 解 为 “Domains, protocols and ports must match”。

# 2. 前端解决跨域问题

- 1> document.domain + iframe (只有在主域相同的时候才能使⽤该⽅法)

- 1) 在www.a.com/a.html中：

document.domain = 'a.com'; var ifr = document.createElement('iframe'); ifr.src = 'http://www.script.a.com/b.html'; ifr.display = none; document.body.appendChild(ifr); ifr.onload = function(){

var doc = ifr.contentDocument || ifr.contentWindow.document; //在这⾥操作doc，也就是b.html ifr.onload = null;

};

- 2) 在www.script.a.com/b.html中： document.domain = 'a.com';


- 2> 动态创建script


![image 1](assets/imageFile1.png)

![image 2](assets/imageFile2.png)

这个没什么好说的，因为script标签不受同源策略的限制。

![image 3](assets/imageFile3.png)

function loadScript(url, func) { var head = document.head || document.getElementByTagName('head')[0]; var script = document.createElement('script'); script.src = url;

script.onload = script.onreadystatechange = function(){

if(!this.readyState || this.readyState=='loaded' || this.readyState=='complete'){ func(); script.onload = script.onreadystatechange = null;

} };

head.insertBefore(script, 0);

} window.baidu = {

sug: function(data){

console.log(data); }

} loadScript('http://suggestion.baidu.com/su?wd=w',function(){console.log('loaded')}); //我们请求的内容在哪⾥？ //我们可以在chorme调试⾯板的source中看到script引⼊的内容

![image 4](assets/imageFile4.png)

- 3> location.hash + iframe 原理是利⽤location.hash来进⾏传值。 假设域名a.com下的⽂件cs1.html要和cnblogs.com域名下的cs2.html传递信息。


- 1) cs1.html⾸先创建⾃动创建⼀个隐藏的iframe，iframe的src指向cnblogs.com域名下的cs2.html⻚⾯

- 2) cs2.html响应请求后再将通过修改cs1.html的hash值来传递数据

- 3) 同时在cs1.html上加⼀个定时器，隔⼀段时间来判断location.hash的值有没有变化，⼀旦有变化则获 取获取hash值 注：由于两个⻚⾯不在同⼀个域下IE、Chrome不允许修改parent.location.hash的值，所以要借助于 a.com域名下的⼀个代理iframe


代码如下： 先是a.com下的⽂件cs1.html⽂件：

![image 5](assets/imageFile5.png)

function startRequest(){ var ifr = document.createElement('iframe'); ifr.style.display = 'none'; ifr.src = 'http://www.cnblogs.com/lab/cscript/cs2.html#paramdo'; document.body.appendChild(ifr);

}

function checkHash() {

try { var data = location.hash ? location.hash.substring(1) : ''; if (console.log) {

console.log('Now the data is '+data); }

} catch(e) {};

} setInterval(checkHash, 2000);

![image 6](assets/imageFile6.png)

cnblogs.com域名下的cs2.html:

![image 7](assets/imageFile7.png)

//模拟⼀个简单的参数处理操作 switch(location.hash){

case '#paramdo': callBack(); break;

case '#paramset': //do something…… break;

}

function callBack(){ try { parent.location.hash = 'somedata';

} catch (e) { // ie、chrome的安全机制⽆法修改parent.location.hash， // 所以要利⽤⼀个中间的cnblogs域下的代理iframe var ifrproxy = document.createElement('iframe'); ifrproxy.style.display = 'none'; ifrproxy.src = 'http://a.com/test/cscript/cs3.html#somedata'; // 注意该⽂件在"a.com"域下 document.body.appendChild(ifrproxy);

} }

![image 8](assets/imageFile8.png)

a.com下的域名cs3.html

//因为parent.parent和⾃身属于同⼀个域，所以可以改变其location.hash的值 parent.parent.location.hash = self.location.hash.substring(1);

- 4> window.name + iframe window.name 的美妙之处：name 值在不同的⻚⾯（甚⾄不同域名）加载后依旧存在，并且可以⽀持 ⾮常⻓的 name 值（2MB）。


- 1) 创建a.com/cs1.html

- 2) 创建a.com/proxy.html，并加⼊如下代码


![image 9](assets/imageFile9.png)

<head> <script> function proxy(url, func){

var isFirst = true, ifr = document.createElement('iframe'), loadFunc = function(){

if(isFirst){ ifr.contentWindow.location = 'http://a.com/cs1.html'; isFirst = false;

}else{ func(ifr.contentWindow.name); ifr.contentWindow.close(); document.body.removeChild(ifr); ifr.src = ''; ifr = null;

} };

ifr.src = url; ifr.style.display = 'none'; if(ifr.attachEvent) ifr.attachEvent('onload', loadFunc); else ifr.onload = loadFunc;

document.body.appendChild(iframe);

} </script> </head> <body>

<script> proxy('http://www.baidu.com/', function(data){

console.log(data); });

</script> </body>

![image 10](assets/imageFile10.png)

- 3 在b.com/cs1.html中包含： <script>


window.name = '要传送的内容'; </script>

## 5> postMessage（HTML5中的XMLHttpRequest Level 2中的API）

- 1) a.com/index.html中的代码：

<iframe id="ifr" src="b.com/index.html"></iframe> <script type="text/javascript"> window.onload = function() {

var ifr = document.getElementById('ifr'); var targetOrigin = 'http://b.com'; // 若写成'http://b.com/c/proxy.html'效果⼀样

// 若写成'http://c.com'就不会执⾏postMessage了 ifr.contentWindow.postMessage('I was there!', targetOrigin);

}; </script>

- 2) b.com/index.html中的代码：


![image 11](assets/imageFile11.png)

![image 12](assets/imageFile12.png)

![image 13](assets/imageFile13.png)

<script type="text/javascript">

window.addEventListener('message', function(event){ // 通过origin属性判断消息来源地址 if (event.origin == 'http://a.com') {

alert(event.data); // 弹出"I was there!" alert(event.source); // 对a.com、index.html中window对象的引⽤

// 但由于同源策略，这⾥event.source不可以访问window对象 }

}, false); </script>

![image 14](assets/imageFile14.png)

- 6> CORS CORS背后的思想，就是使⽤⾃定义的HTTP头部让浏览器与服务器进⾏沟通，从⽽决定请求或响应是 应该成功，还是应该失败。 IE中对CORS的实现是xdr


![image 15](assets/imageFile15.png)

var xdr = new XDomainRequest(); xdr.onload = function(){

console.log(xdr.responseText);

} xdr.open('get', 'http://www.baidu.com');

...... xdr.send(null);

![image 16](assets/imageFile16.png)

其它浏览器中的实现就在xhr中

![image 17](assets/imageFile17.png)

var xhr = new XMLHttpRequest(); xhr.onreadystatechange = function () {

if(xhr.readyState == 4){ if(xhr.status >= 200 && xhr.status < 304 || xhr.status == 304){

console.log(xhr.responseText); }

}

} xhr.open('get', 'http://www.baidu.com');

...... xhr.send(null);

![image 18](assets/imageFile18.png)

实现跨浏览器的CORS

![image 19](assets/imageFile19.png)

function createCORS(method, url){ var xhr = new XMLHttpRequest(); if('withCredentials' in xhr){

xhr.open(method, url, true);

}else if(typeof XDomainRequest != 'undefined'){ var xhr = new XDomainRequest(); xhr.open(method, url);

}else{ xhr = null;

} return xhr;

} var request = createCORS('get', 'http://www.baidu.com'); if(request){

request.onload = function(){

......

}; request.send();

}

![image 20](assets/imageFile20.png)

- 7> JSONP JSONP包含两部分：回调函数和数据。 回调函数是当响应到来时要放在当前⻚⾯被调⽤的函数。 数据就是传⼊回调函数中的json数据，也就是回调函数的参数了。

function handleResponse(response){ console.log('The responsed data is: '+response.data);

} var script = document.createElement('script'); script.src = 'http://www.baidu.com/json/?callback=handleResponse'; document.body.insertBefore(script, document.body.firstChild); /*handleResonse({"data": "zhe"})*/ //原理如下： //当我们通过script标签请求时 //后台就会根据相应的参数(json,handleResponse) //来⽣成相应的json数据(handleResponse({"data": "zhe"})) //最后这个返回的json数据(代码)就会被放在当前js⽂件中被执⾏ //⾄此跨域通信完成

jsonp虽然很简单，但是有如下缺点：

- 1）安全问题(请求代码中可能存在安全隐患)

- 2）要确定jsonp请求是否失败并不容易


- 8> web sockets web sockets是⼀种浏览器的API，它的⽬标是在⼀个单独的持久连接上提供全双⼯、双向通信。(同源 策略对web sockets不适⽤) web sockets原理：在JS创建了web socket之后，会有⼀个HTTP请求发送到浏览器以发起连接。取得 服务器响应后，建⽴的连接会使⽤HTTP升级从HTTP协议交换为web sockt协议。 只有在⽀持web socket协议的服务器上才能正常⼯作。


![image 21](assets/imageFile21.png)

![image 22](assets/imageFile22.png)

var socket = new WebSockt('ws://www.baidu.com');//http->ws; https->wss socket.send('hello WebSockt'); socket.onmessage = function(event){

var data = event.data; }
