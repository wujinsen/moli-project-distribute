# 简介

最近遇到很多⼈来咨询我关于浏览器缓存的⼀些问题，⽽这些问题都是类似的，因此总结本⽂来解答 以后遇到类似问题的朋友。

因本⽂主要以浏览器缓存场景介绍，所以⾮浏览器场景下的⼀些⽤法本⽂不会介绍，⽽且本⽂以 chrome为测试浏览器。

浏览器缓存是指当我们使⽤浏览器访问⼀些⽹站⻚⾯或者htp服务时，根据服务端返回的缓存设置响应 头将响应内容缓存到浏览器，下次可以直接使⽤缓存内容或者仅需要去服务端验证内容是否过期即 可。这样的好处可以减少浏览器和服务端之间来回传输的数据量，节省带宽提升性能。

⾸先看个例⼦；当我们第⼀次访问htp:/item.jd.com/185658.html时将得到如下响应头：

![image 1](<聊聊高并发系统之HTTP缓存.note_images/imageFile1.png>)

然后接着按F5刷新⻚⾯，将得到如下响应头

![image 2](<聊聊高并发系统之HTTP缓存.note_images/imageFile2.png>)

第⼆次返回的相应状态码为304，表示服务端⽂档没有修过过，浏览器缓存的内容还是最新的。

接下来我们看下如何在Java应⽤层控制浏览器缓存。

# 示例

Last-Modified

如下是我们的spring mvc缓存测试代码：

@RequestMaping("/cache") publicResponseEntity<String> cache(

HtpServletRequest request,

/为了 ⽅ 便 测 试 ， 此 处 传 ⼊ ⽂ 档最 后 修 改 时 间

@RequestParam("milis")longlastModifiedMilis,

/浏 览 器 验 证 ⽂ 档 内 容 是 否 修 改 时 传 ⼊ 的 Last-Modified

@RequestHeader(value ="If-Modified-Since", required =false) Date ifModifiedSince) {

/当 前 系统 时 间

longnow = System.curentTimeMilis();

/⽂ 档 可 以 在 浏 览 器 端 /proxy上 缓 存 多 久 longmaxAge = 20;

/判 断 内 容 是 否 修 改 了 ， 此 处 使 ⽤ 等 值 判 断

if(ifModifiedSince !=nul & ifModifiedSince.getTime() = lastModifiedMilis) {

return newResponseEntity<String>(HtpStatus.NOT_MODIFIED); }

DateFormat gmtDateFormat =newSimpleDateFormat(" E, d M y H: m:s 'GMT'", Locale.US);

String body ="<a href='>点击访问当前链接</a>"; MultiValueMap<String, String> headers =newHtpHeaders();

/⽂ 档 修 改 时 间

headers.ad("Last-Modified", gmtDateFormat.format(newDate(lastModifiedMilis);

/当 前 系统 时 间

headers.ad("Date", gmtDateFormat.format(newDate(now);

/过 期时 间 htp 1.0⽀ 持

headers.ad("Expires", gmtDateFormat.format(newDate(now + maxAge);

/⽂ 档 ⽣ 存 时 间 htp 1.1⽀ 持 headers.ad("Cache-Control","max-age="+ maxAge); return newResponseEntity<String>(body, headers, HtpStatus.OK);

}

为了⽅便测试，测试时将⽂档的修改时间通过milis参数传⼊，实际应⽤时可以使⽤如商品的最后修改 时间等替代。

⾸次访问 ⾸次访问htp:/localhost:9080/cache?milis=147134916709，将得到如下响应头：

![image 3](<聊聊高并发系统之HTTP缓存.note_images/imageFile3.png>)

响应状态码20表示请求内容成功，另外有如下⼏个缓存控制参数： Last-Modified：表示⽂档的最后修改时间，当去服务器验证时会拿这个时间去； Expires：htp/1.0规范定义，表示⽂档在浏览器中的过期时间，当缓存的内容超过这个时间则需要重 新去服务器获取最新的内容； Cache-Control：htp/1.1规范定义，表示浏览器缓存控制，max-age=20表示⽂档可以在浏览器中缓 存20秒。

根据规范定义Cache-Control优先级⾼于Expires；实际使⽤时可以两个都⽤，或仅使⽤Cache-Control 就可以了（⽐如京东的活动⻚sale.jd.com）。⼀般情况下Expires=当前系统时间（Date） + 缓存时间 （Cache-Control: max-age）。⼤家可以在如上测试代码进⾏两者单独测试，缓存都是可⾏的。

F5刷新 接着按F5刷新当前⻚⾯，将看到浏览器发送如下请求头：

![image 4](<聊聊高并发系统之HTTP缓存.note_images/imageFile4.png>)

### 此处发送时有⼀个If-Modified-Since请求头，其值是上次请求响应中的Last-Modified，即浏览器会拿 这个时间去服务端验证内容是否发⽣了变更。接着收到如下响应信息：

![image 5](<聊聊高并发系统之HTTP缓存.note_images/imageFile5.png>)

响应状态码为304，表示服务端告诉浏览器说“浏览器你缓存的内容没有变化，直接使⽤缓存内容展示 吧”。

注：在测试时要过⼀段时间更改下参数milis来表示内容修改了，要不然会⼀直看到304响应。

Ctrl+F5强制刷新 如果你想强制从服务端获取最新的内容，可以按Ctrl+F5：

![image 6](<聊聊高并发系统之HTTP缓存.note_images/imageFile6.png>)

浏览器在请求时不会带上If-Modified-Since，并带上Cache-Control:no-cache和Pragma:no-cache， 这是为了告诉服务端说我请给我⼀份最新的内容。

## from cache

当我们按F5刷新、Ctrl+F5强制刷新、地址栏输⼊地址刷新时都会去服务端验证内容是否发⽣了变更。 那什么情况才不去服务端验证呢？即有些朋友还会发现有⼀些“from cache”的情况，这是什么情况下 发⽣的呢？

从A⻚⾯跳转到A⻚⾯或者从A⻚⾯跳转到B⻚⾯时：

![image 7](<聊聊高并发系统之HTTP缓存.note_images/imageFile7.png>)

⼤家可以通过如上⽅式模拟，即从A⻚⾯跳转到A⻚⾯也是情况1。此时如果内容还在缓存时间之内，直 接从浏览器获取的内容，⽽不去服务端验证。

访问⻚⾯htp:/item.jd.com/105656.html，然后点击⾯包屑中的HTP权威指南时会跳转到当前⻚ ⾯，此时看到如下结果，⻚⾯及⻚⾯异步加载的⼀些js、cs、图⽚都from cache了。

![image 8](<聊聊高并发系统之HTTP缓存.note_images/imageFile8.png>)

还有如通过浏览器历史记录进⾏前进后退时也会⾛from cache。本⽂是基于chrome 52.0.2743.16 m 版本测试，不同浏览器⾏为可能存在差异。

Age ⼀般⽤于代理层（如CDN），⼤家在访问京东⼀些⻚⾯时会发现有⼀个Age响应头，然后强制刷新 (Ctrl+F5)后会发现其不断的变化；其表示此内容在代理层从缓存到现在经过了多⻓时间了，即在代理 层缓存了多⻓时间了。

![image 9](<聊聊高并发系统之HTTP缓存.note_images/imageFile9.png>)

Vary ⼀般⽤于代理层（如CDN），⽤于代理层和浏览器协商什么情况下使⽤哪个版本的缓存内容（⽐如压 缩版和⾮压缩版），即什么情况下后续请求才能使⽤代理层缓存的该版本内容，⽐如如下响应是告知 浏览器Content-Encoding:gzip，即缓存代理层缓存了gzip版本的内容；那么后续的请求在请求时 Acept-Encoding头部中包含gzip时才能使⽤改代理层缓存。

![image 10](<聊聊高并发系统之HTTP缓存.note_images/imageFile10.png>)

Via ⼀般⽤于代理层（如CDN），表示访问到最终内容经过了哪些代理层，⽤的什么协议，代理层是否缓 存命中等等；通过它可以进⾏⼀些故障诊断。

![image 11](<聊聊高并发系统之HTTP缓存.note_images/imageFile11.png>)

## ETag

@RequestMaping("/cache/etag") publicResponseEntity<String> cache(

HtpServletRequest request, HtpServletResponse response,

/浏 览 器 验 证 ⽂ 档 内 容 的 实 体 If-None-Match

@RequestHeader(value ="If-None-Match", required =false) String ifNoneMatch) {

/当 前 系统 时 间

longnow = System.curentTimeMilis();

/⽂ 档 可 以 在 浏 览 器 端 /proxy上 缓 存 多 久 longmaxAge =10;

String body ="<a href='>点击访问当前链接</a>";

/弱 实 体

String etag ="W/\ "+ md5(body) +"\ ";

if(StringUtils.equals(ifNoneMatch, etag) {

return newResponseEntity<String>(HtpStatus.NOT_MODIFIED); }

DateFormat gmtDateFormat =newSimpleDateFormat(" E, d M y H: m:s 'GMT'", Locale.US); MultiValueMap<String, String> headers =newHtpHeaders();

/ETag htp 1.1⽀ 持

headers.ad("ETag", etag);

/当 前 系统 时 间

headers.ad("Date", gmtDateFormat.format(newDate(now);

/⽂ 档 ⽣ 存 时 间 htp 1.1⽀ 持 headers.ad("Cache-Control","max-age="+ maxAge); return newResponseEntity<String>(body, headers, HtpStatus.OK);

}

其中ETag⽤于发送到服务端进⾏内容变更验证的，⽽Catch-Control是⽤于控制缓存时间的（浏览器、 代理层等）。此处我们使⽤了弱实体W\”343sda”，弱实体（”343sda”）只要内容语义没变即可，⽐如 内容的gzip版和⾮gzip版可以使⽤弱实体验证；⽽强实体指字节必须完全⼀致（gzip和⾮gzip情况是不 ⼀样的），因此建议⾸先选择使⽤弱实体。nginx在⽣成etag时使⽤的算法是Last-Modified + Content-Length计算的： ngx_sprintf(etag->value.data,"\"%xT-%xO\",

r->headers_out.last_modified_time, r->headers_out.content_length_n)

到此简单的基于⽂档修改时间和过期时间的缓存控制就介绍完了，在内容型响应我们⼤多数根据内容 的修改时间来进⾏缓存控制，ETag根据实际需求⽽定（⽐如）。另外还可以使⽤html Meta标签控制浏 览器缓存，但是对代理层缓存⽆效，因此不建议使⽤。

## 总结

![image 12](<聊聊高并发系统之HTTP缓存.note_images/imageFile12.png>)

- 1、服务端响应的Last-Modified会在下次请求时以If-Modified-Since请求头带到服务端进⾏⽂档是否修 改的验证，如果没有修改则返回304，浏览器可以直接使⽤缓存内容；


- 2、Cache-Control:max-age和Expires⽤于决定浏览器端内容缓存多久，即多久过期，过期后则删除缓 存重新从服务端获取最新的；另外可以⽤于from cache场景；

- 3、htp/1.1规范定义的Cache-Control优先级⾼于htp/1.0规范定义的Expires；

- 4、⼀般情况下Expires=当前系统时间 + 缓存时间（Cache-Control:max-age）；

- 5、htp/1.1规范定义了ETag来通过⽂档摘要的⽅式控制。


Last-Modified与ETag同时使⽤时，浏览器在验证时会同时发送If-Modified-Since和If-None-Match， 按照htp/1.1规范，如果同时使⽤If-Modified-Since和If-None-Match则服务端必须两个都验证通过后 才能返回304；且nginx就是这样做的。因此实际使⽤时应该根据实际情况选择。还有If-Match和IfUnmodified-Since本⽂就不介绍了。

接下来我们看下如何使⽤nginx进⾏缓存控制。

# nginx缓存设置

nginx提供了expires、etag、if-modified-since指令来进⾏浏览器缓存控制。

expires

假设我们使⽤nginx作为静态资源服务器，此时可以使⽤expires进⾏缓存控制。

location /img { alias /export/img/; expires 1d;

} 当我们访问静态资源时，如htp:/192.168.61.129/img/1.jpg，将得到类似如下的响应头：

![image 13](<聊聊高并发系统之HTTP缓存.note_images/imageFile13.png>)

对于静态资源会⾃动添加ETag，可以通过添加“etag of”指令禁⽌⽣成ETag。如果是静态⽂件LastModified是⽂件的最后修改时间；Expires是根据当前服务端系统时间算出来的。如上nginx配置的计算 逻辑（实际计算逻辑⽐这个多，具体参考官⽅⽂档）： if (expires = NGX_HTP_EXPIRES_ACES|r->headers_out.last_modified_time = -1) {

max_age = expires_time;

expires_time += now; }

## if-modified-since

此指令⽤于表示nginx如何拿服务端的Last-Modified和浏览器端的If-Modified-Since时间进⾏⽐较，默 认“if_modified_since exact”表示精确匹配，也可以使⽤“if_modified_sincebefore”表示只要⽂件的上 次修改时间早于或等于浏览器短的If-Modified-Since时间，就返回304。

## nginx proxy expires

使⽤nginx作为反向代理时，请求会先进⼊nginx，然后nginx将请求转发给后端应⽤。如下图所示： ⾸先配置upstream： upstream backend_tomcat {

server 192.168.61.1 9080 max_fails=10 fail_timeout=10s weight=5; }

接着配置location： location = /cache {

proxy_pas htp:/backend_tomcat/cache$is_args$args; }

接下来我们可以通过如htp:/192.168.61.129/cache?milis=147134916709访问nginx，nginx会将请 求转发给后端java应⽤。也就是说nginx只是做了相关的转发（负载均衡），并没有对请求和响应做什 么处理。

假设对后端返回的过期时间需要调整，可以添加expires指令到location： location = /cache {

proxy_pas htp:/backend_tomcat/cache$is_args$args;

expires 5s; }

然后再请求相关的URL，将得到如下响应：

![image 14](<聊聊高并发系统之HTTP缓存.note_images/imageFile14.png>)

过期时间相关的响应头被expires指令更改了，但是Last-Modified是没有变的。

即使我们更改了缓存过期头，但nginx本身没有对这些内容做缓存，每次请求还是要到后端验证的，假 设在过期时间内，这些验证在nginx这⼀层验证就可以了，不需要到后端验证，这样可以减少后端的很 ⼤压⼒。即整体流程是：

- 1、浏览器发起请求，⾸先到nginx，nginx根据url在nginx本地查找是否有⽂档缓存；

- 2、nginx没有找到本地缓存，则去后端获取最新的⽂档，并放⼊到nginx本地缓存中；返回20状态码 和最新的⽂档给浏览器；

- 3、nginx找到本地缓存了，⾸先验证⽂档是否过期(Cache-Control:max-age=5)，如果过期则去后端 获取最新的⽂档，并放⼊nginx本地缓存中，返回20状态码和最新的⽂档给浏览器；如果⽂档没有过 期，如果If-Modified-Since与缓存⽂档的Last-Modified匹配，则返回30状态码给浏览器，否则返回 20状态码和最新的⽂档给浏览器。


即内容不需要动态（计算、渲染等）速度更快，内容越接近于⽤户速度越快。像apache trafic server、squid、varnish、nginx等技术都可以来进⾏内容缓存。还有CDN就是⽤来加速⽤户访问的：

![image 15](<聊聊高并发系统之HTTP缓存.note_images/imageFile15.png>)

即⽤户⾸先访问到全国各地的CDN节点（使⽤如ATS、Squid实现），如果CDN没命中，会回源到中央 nginx集群，该集群如果没有命中缓存（该集群的缓存不是必须的，要根据实际命中情况等决定），最 后回源到后端应⽤集群。

像我们商品详情⻚的⼀些服务就⼤量使⽤了nginx缓存减少回源到后端的请求量，从⽽提升访问速度。 可以参考《 》、《 》和《

构建需求响应式亿级商品详情⻚ 京东商品详情⻚服务闭环实践 应⽤多级缓存 模式⽀撑海量读服务

》。

## nginx代理层缓存

htp模块配置： proxy_bufering on; proxy_bufer_size 4k; proxy_bufers 512 4k; proxy_busy_bufers_size 64k; proxy_cache_path /export/cache/proxy_cachelevels=1 2 keys_zone=cache:512m inactive=5m max_size=8g use_temp_path=of; #proxy timeout proxy_conect_timeout 3s;

proxy_read_timeout 5s; proxy_send_timeout 5s;

其中红⾊部分是proxy_cache_path指令相关配置： levels=1 2 ：表示创建两级⽬录结构，⽐如/export/cache/proxy_cache/7/3c/，将所有⽂件放在⼀级⽬ 录结构中如果⽂件量很⼤会导致访问⽂件慢； keys_zone=cache:512m ：设置存储所有缓存key和相关信息的共享内存区，1M⼤约能存储8 0个 key； inactive=5m：inactive指定被缓存的内容多久不被访问将从缓存中移除，以保证内容的新鲜；默认10 分钟； max_size=8g：最⼤缓存阀值，“cachemanager”进程会监控最⼤缓存⼤⼩，当缓存达到该阀值，该 进程将从缓存中移除最近最少使⽤的内容； use_temp_path：如果为on，则内容⾸先被写⼊临时⽂件（proxy_temp_path ），然后重命名到 proxy_cache_path指定的⽬录；如果设置为of，则内容直接被写⼊到proxy_cache_path指定的⽬录， 如果需要cache建议of，该特性是1.7.10提供的。

## location配置

location = /cache { proxy_cache cache; proxy_cache_key $scheme$proxy_host$request_uri; proxy_cache_valid 20 5s; proxy_pas htp:/backend_tomcat/cache$is_args$args;

ad_header cache-status $upstream_cache_status;

} 缓存相关配置： proxy_cache ：指定使⽤哪个共享内存区域存储缓存键和相关信息； proxy_cache_key：设置缓存使⽤的key，默认为访问的完整URL，根据实际情况设置缓存key； proxy_cache_valid：为不同的响应状态码设置缓存时间；如果是proxy_cache_valid 5s 则20、 301、302响应将被缓存；

## proxy_cache_valid

proxy_cache_valid不是唯⼀设置缓存时间的，还可以通过如下⽅式（优先级从上到下）：

- 1、以秒为单位的“X-Acel-Expires”响应头来设置响应缓存时间；

- 2、如果没有“X-Acel-Expires”，可以根据“Cache-Control”、“Expires”来设置响应缓存时间；

- 3、否则使⽤proxy_cache_valid设置的缓存时间；


如果响应头包含Cache-Control：private/no-cache/no-store、Set-Cokie或者只有⼀个Vary响应头且 其值为*，则响应内容将不会被缓存。可以使⽤proxy_ignore_headers来忽略这些响应头。

ad_headercache-status $upstream_cache_status在响应头中添加缓存命中的状态：

HIT：缓存命中了，直接返回缓存中内容，不回源到后端； MI S：缓存没有命中，回源到后端获取最新的内容； EXPIRED：缓存命中但过期了，回源到后端获取最新的内容； UPDATING：缓存已过期但正在被别的nginx进程更新；配置了proxy_cache_use_staleupdating指令 时会存在该状态； STALE：缓存已过期，但因后端服务出现了问题（⽐如后端服务挂了）返回过期的响应；配置了如 proxy_cache_use_stale eror timeout指令后会存在该状态； REVALIDATED：启⽤proxy_cache_revalidate指令后，当缓存内容过期时nginx通过⼀次If-ModifiedSince的请求头去验证缓存内容是否过期，此时会返回该状态； BYPAS：proxy_cache_bypas指令有效时强制回源到后端获取内容，即使已经缓存了；

## proxy_cache_min_uses

⽤于控制请求多少次后响应才被缓存；默认“proxy_cache_min_uses 1;”，如果缓存热点⽐较集中、存 储有限，可以考虑修改该参数以减少缓存数量和写磁盘次数；

## proxy_no_cache

⽤于控制什么情况下响应将不被缓存；⽐如配置“proxy_no_cache $args_nocache”，如果带的参数值 ⾄少有⼀个不为空或者0，则响应将不被缓存；

## proxy_cache_bypas

类似于proxy_no_cache，但是其控制什么情况不从缓存中获取内容，⽽是直接到后端获取内容；如果 命中则$upstream_cache_status为BYPAS；

## proxy_cache_use_stale

当对缓存内容的过期时间不敏感，或者后端服务出问题时即使缓存的内容不新鲜也总⽐返回错误给⽤ 户强（类似于托底），此时可以配置该参数，如“proxy_cache_use_stale eror timeout htp_50 htp_502 htp_503htp_504”：即如果超时、后端连接出错、50、502、503等错误时即使缓存内容 已过期也先返回给⽤户，此时$upstream_cache_status为STALE；还有⼀个updating表示缓存已过期 但正在被别的nginx进程更新将先返回过期的内容，此时 $upstream_cache_status为UPDATING；

## proxy_cache_revalidate

当缓存过期后，如果开启了proxy_cache_revalidate，则会发出⼀次If-Modified-Since和If-NoneMatch条件请求，如果后端返回304则会得到两个好处：节省带宽和减少写磁盘的次数；此时 $upstream_cache_status为REVALIDATED；

## proxy_cache_lock

当多个客户端同时请求同⼀份内容时，如果开启proxy_cache_lock（默认of）则只有⼀个请求被发送 ⾄后端；其他请求将等待该内容返回；当第⼀个请求返回时，其他请求将从缓存中获取内容返回；当 第⼀个请求超过了proxy_cache_lock_timeout超时时间（默认5s），则其他请求将同时请求到后端来 获取响应，且响应不会被缓存（在1.7.8版本之前是被缓存的）；启⽤proxy_cache_lock可以应对Dogpile efect（当某个缓存失效时，同时⼜⼤量相同的请求没命中缓存，⽽同时请求到后端，从⽽导致后 端压⼒太⼤，此时限制⼀个请求去拿即可）。

proxy_cache_lock_age是1.7.8新添加的，如果在proxy_cache_lock_age指定的时间内（默认5s），最 后⼀个发送到后端进⾏新缓存构建的请求还没有完成，则下⼀个请求将被发送到后端来构建缓存（因 为1.7.8版本之后，proxy_cache_lock_timeout超时之后返回的内容是不缓存的，需要下⼀次请求来构 建响应缓存）。

清理缓存

有时候缓存的内容是错误的，需要⼿⼯清理，nginx plus版本提供了purger的功能，但是对于⾮plus版 本的nginx可以考虑使⽤ngx_cache_purge（htps:/github.com/FRiCKLE/ngx_cache_purge）模块进 ⾏清理缓存，如： location ~ /purge(/.*) {

alow 127.0.0.1; deny al; proxy_cache_purge cache$1$is_args$args;

} 注意该⽅法应该只允许内⽹可以访问，如有必要可以考虑需要密码才能访问。

到此代理层缓存就介绍完了，通过代理层缓存可以解决很多问题，可以参考《 》和《 》。

京东商品详情⻚服务闭 环实践 京东商品详情⻚服务闭环实践

# ⼀些经验

- 1、只缓存20状态码的响应，像302等要根据实际场景决定（⽐如当系统出错时⾃动302到错误⻚⾯， 此时缓存302就不对了）；

- 2、有些⻚⾯不需要强⼀致，可以进⾏⼏秒的缓存（⽐如商品详情⻚展示的库存，可以缓存⼏秒钟，短 时间的不⼀致对于⽤户来说是没有影响的）；

- 3、js/cs/image等⼀些内容缓存时间可以设置的很久（⽐如1个⽉甚⾄1年），通过在⻚⾯修改版本来 控制过期，不建议随机数⽅式；

- 4、假设商品详情⻚异步加载的⼀些数据使⽤的是Last-Modified进⾏的过期控制，⽽服务端做了逻辑 修改但内容是没有修改的，即内容的最后修改时间没变，如果想过期这些异步加载的数据，可以考虑 在商品详情⻚添加异步加载数据的版本号，通过添加版本号来加载最新的数据，或者将Last-Modified 时间加1来解决；⽽这种情况⽐较适合使⽤ETag；

- 5、商品详情⻚异步加载的⼀些数据，可以考虑更⻓时间的缓存（⽐如1个⽉⽽不是⼏分钟），可以通 过MQ将修改时间推送商品详情⻚，从⽽实现按需过期数据；


- 6、服务端考虑使⽤内存缓存（tmpfs）、 SD缓存；考虑服务端负载均衡算法，如⼀致性哈希提升缓 存命中率；

- 7、缓存KEY要合理设计（⽐如去掉参数/排序参数保证代理层缓存命中），要有清理缓存的⼯具，出问 题时能快速清理掉问题KEY；

- 8、AB测试/个性化需求时应禁⽤掉浏览器缓存，但考虑服务端缓存；

- 9、为了便于查找问题，⼀般会在响应头中添加源服务器信息，如访问京东商品详情⻚会看到ser响应 头，此头存储了源服务器IP，以便出现问题时知道哪台服务器有问题。


