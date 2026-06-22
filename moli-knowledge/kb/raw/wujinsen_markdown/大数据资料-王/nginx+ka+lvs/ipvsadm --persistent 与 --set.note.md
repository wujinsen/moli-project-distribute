最近受困于ipvsadm的两个参数，经过⼀些实验观察他们的区别（未查看ipvs源码）。 ⾸先show⼀下ipvsadm -h对这两个参数的注释

- -persistent -p [timeout] persistent service/持久服务
- -set tcp tcpfin udp set conection timeout values/链接的超时时间


- 1. -persistent -p [timeout] 持久服务超时时间设置参数，真对⼀些需要保持状态的应⽤，例如⼀些htp应⽤、ftp、 sl等。 在参

数的时间范围内同⼀⽤户（client IP）的多次访问会被ipvs分配到同⼀台realserver上。

- 2. -set tcp tcpfin udp 真对链接的超时时间。以tcp为例，⼀个tcp连接建⽴后会传输N个报⽂， 当两个报⽂相继到达的时间

差在超时时间内就会被转发到同⼀台realserver上进⾏处理， 若时间差⼤于超时时间就会根据调度算法 重新选择realserver，连接就有可能出现异常。 ipvs是根据client IP 和 client port来识别是不是同⼀个 链接发的报⽂。

- 3. 两者的区别与联系 区别：


persistent 是提供对有持久服务需要的⽀撑， 是在超时时间内将同⼀个client IP的链接分发到同⼀ 个realserver上，⽐较宏观⼀些；

set 是针对⼀次链接两个相继到达报⽂的超时时间定义， 这个值在单⼀⼀次链接内有效，⽐较微观 ⼀些。

联系： persistent值⼤于等于set时，持久服务分发超时以persistent的设置为准。 persistent值⼩于set时，持久服务分发超时会以(s/60)*60 + p%60 + 60为准（当persistent值超

时后， 会将persistent⾃动赋值为60，超时后继续将persistent⾃动赋值为60.直到set超时persistent 再次超时未知）。

