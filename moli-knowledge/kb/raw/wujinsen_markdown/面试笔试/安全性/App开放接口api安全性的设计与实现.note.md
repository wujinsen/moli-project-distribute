# 前⾔

在app开放接⼝api的设计中，避免不了的就是安全性问题，因为⼤多数接⼝涉及到⽤户的个⼈信息以及⼀些敏感的数据，所 以对这些接⼝需要进⾏身份的认证， 那么这就需要⽤户提供⼀些信息，⽐如⽤户名密码等，但是为了安全起⻅让⽤户暴露的明⽂密码次数越少越好，我们⼀般在 web项⽬中，⼤多数采⽤保存的session中， 然后在存⼀份到cookie中，来保持⽤户的回话有效性。但是在app提供的开放接⼝中，后端服务器在⽤户登录后如何去验证和 维护⽤户的登陆有效性呢？

# 设计

对于敏感的api接⼝，需使⽤https协议 https是在http超⽂本传输协议加⼊SSL层，它在⽹络间通信是加密的，所以需要加密证书。https协议需要ca证书，⼀般需要 交费。

- 1、原理

⽤户登录后向服务器提供⽤户认证信息（如账户和密码），服务器认证完后给客户端返回⼀个PID令牌，⽤户再次获取信息 时， 带上此令牌，如果令牌正取，则返回数据。对于获取Token信息后，访问⽤户相关接⼝，客户端请求的url需要带上如下参 数：

然后将所有⽤户请求的参数（包括timestamp，pid），然后更具MD5加密（可以加点盐），⽣成动态的url。 然后登陆后每次调⽤⽤户信息时，带上timestamp，pid参数。 加上时间戳和pid后的URL：http://127.0.0.1:8888/index? pid=d073dae99f70b0cda2fa1ef8d25c527f|1475117419.5424652|0 就变成⼀个动态的⽽且相对的具有⾼安全的，保证数据安全的访问。

- 2、具体实现


<table>
  <tr>
    <th>① 时间戳：timestamp<br><br>）</th>
  </tr>
</table>


② PID令牌：PID（在这我们给定义为PID

- 1. api请求客户端想服务器端⼀次发送⽤⽤户认证信息（⽤户名和密码），服务器端请求到改请求后，验证⽤户信息是否正 确。

如果正确：则返回⼀个唯⼀不重复的字符串，然后在Redis（任意缓存服务器）中维护这个⽤户信息关系，以便其他api对 pid的校验。

如果错误：则返回错误码。

- 2.服务器设计⼀个url请求拦截规则

- 3.定期处理保存下来的动态请求URL


<table>
  <tr>
    <th>①判断是否包含timestamp，pid参数，如果不含有返回错误码。<br>②根据⽤户请求的url参数，服务器端按照同样的规则⽣成动态的URL，对⽐请求的动态url与服务端⽣成的是否相等，相等则放⾏允许 访问。<br>③判断服务器接到请求的时间和参数中的时间戳是否相差很⻓⼀段时间（时间⾃定义如⼗秒），如果超过则说明该url已经过期。<br>④记录下每次请求的动态URL，规定⼀个动态的URL只能访问⼀次，检测每次请求的url是否请求过，去过存在就返回错误代码（处理 url被拦截并且在⼗秒内请求的访问）。<br><br><br>都需拦截。</th>
  </tr>
</table>


⑤此url拦截只需对获取身份认证的url放⾏（如登陆url），剩余所有的url

# 代码实现

服务端规定的规则

<table>
  <tr>
    <th>#!/usr/bin/env python # -*- coding:utf-8 -*import tornado.iol op import tornado.web import hashlib import time<br><br>aces_record = [] # 创建第⼀次登录过URL列表<br><br>PID_LIST = [ # pid列表 'qwe', 'ioui', '234s',<br><br>]<br><br>clas MainHandler(tornado.web.RequestHandler): def get(self): # 获取url中全部数据 pid = self.get_argument('pid', None) # 获取变量 m5, client_time, i = pid.split('|') # 获得数据，以“|”分割开 print(m5, client_time, i) server_time = time.time() # 服务端的当前时间 # 时间超过10s禁⽌ if server_time > float(client_time) + 10: # 服务端的当前时间⼤于客户端当前时间加10秒，表示过期不允许访问 self.write('gun') return # 处理10s内容重复的请求 if pid in aces_record: # 如果客户端请求的动态URL在第⼀次登录过的URL列表中 self.write('gun') return aces_record.apend(pid) # 允许通过的url添加到列表中<br><br>pid = PID_LIST[int(i)] # 获得客户端发来的pid后⾯携带的数字 ramdom_str = "%s|%s" % (pid, client_time) # 把客户的pid与当前时间戳拼接 h = hashlib.md5() # MD5加密值 h.update(bytes(ramdom_str, encoding='utf-8') # 把客户的pid与当前时间戳拼接⼀个字符串再尽⼼md5加密 server_m5 = h.hexdigest() # 服务端⽣成的动态URL # print(m5,server_m5) if m5 = server_m5: # 客户⽣成的与服务端⽣成的进⾏对⽐ self.write("Helo, world") else: self.write('gun')<br><br>aplication = tornado.web.Aplication([<br><br>(r"/index", MainHandler), ])<br><br>if _name_ = "_main_": aplication.listen( 8)</th>
  </tr>
</table>


tornado.iol op.IOLop.instance().start()

客户端按规则⽣成符合的

<table>
  <tr>
    <th>#!/usr/bin/env python # -*- coding:utf-8 -*import time import requests import hashlib<br><br>PID = 'qwe' # 客户的PID<br><br>current_time = time.time() # 当前时间戳 ramdom_str = "%s|%s" % (PID, current_time) # 把pid与当前时间戳拼接成⼀个字符串 h = hashlib.md5() # md5加密 h.update(bytes(ramdom_str, encoding='utf-8') # 把pid与当前时间戳拼接成⼀个字符串再进⾏md5加密 UID = h.hexdigest() # 加密后的字符串<br><br>q = "%s|%s|0" % (UID, current_time) # 在把这个字符串后⾯拼接⼀个数值 0 url = 'htp:/127.0.0.1  8/index?pid=%s' % q # ⽣成最后⽣成的动态url print(url) ret = requests.get(url)</th>
  </tr>
</table>


print(ret.text)

测试效果代码

<table>
  <tr>
    <th>#!/usr/bin/env python # -*- coding:utf-8 -*import requests<br><br>ret = requests.get('htp:/127.0.0.1  8/index?pid=c253948ca7b7fe0d0fcd9d75b7574|1474341577.493872|0')</th>
  </tr>
</table>


print(ret.text)

这是⽐较粗超的API认证机制，可以初步了解。

