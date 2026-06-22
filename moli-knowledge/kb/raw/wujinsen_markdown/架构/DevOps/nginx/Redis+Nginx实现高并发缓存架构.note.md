htps:/ w.aboutyun.com/forum.php?mod=viewthread&tid=3245

问题导读：

- 1、怎样设计⾼并发缓存架构？
- 2、什么是Htp缓存控制头？
- 3、怎样配置Nginx缓存？


# 1缓存架构设计

⼀谈到缓存架构，很多⼈想到的是Redis，但其实整套体系的缓存架构并⾮只有Redis，⽽应该是多个 层⾯多个软件结合形成⼀套⾮常良性的缓存体系。⽐如下⾯缓存架构设计就涉及到了多个层⾯的缓存 软件。本⽂只提供思路不提供整体代码

1.1 缓存架构设计

架构图综合了多种缓存和多层⾯的缓存设计，从前端⻚⾯缓存到代理服务器lvs和nginx缓存，以及后端 服务redis缓存，包括缓存数据同步等。 对上述架构，我们来个宏观解说：

HTML⻚⾯做缓存，浏览器端可以缓存HTML⻚⾯和其他静态资源，防⽌⽤户频繁刷新对后端造成巨 ⼤压⼒

Lvs实现记录不同协议以及不同⽤户请求链路缓存

Nginx这⾥会做HTML⻚⾯缓存配置以及Nginx⾃身缓存配置

数据查找这⾥⽤Lua取代了其他语⾔查找，提⾼了处理的性能效率，并发处理能⼒将⼤⼤提升

数据缓存采⽤了Redis集群+主从架构，并实现缓存读写分离操作

集成Canal实现数据库数据增量实时同步Redis

# 2Redis集群⾼级应⽤

这⾥安装6个redis，配置如下：

# 3Nginx缓存

为了提升⽹站的整体性能，我们⼀般会采⽤缓存，从宏观层⾯来说，会采⽤浏览器缓存和后端缓存， Nginx处于Web⽹站的服务最外层，⽽且⽀持浏览器缓存配置和后端数据缓存，⽤它来做部分数据缓 存，效率更⾼。

Web缓存是可以⾃动保存常⻅⽂档副本的HTP 设备。当Web请求抵达缓存时，如果本地有“已缓存的” 副本，就可以从本地设备⽽不是服务器中提取这个⽂ 档。

# 3.1OpenRestry安装

OpenResty&#174; 是⼀个基于 Nginx 与 Lua 的⾼性能 Web 平台，其内部集成了⼤量精良的 Lua 库、 第三⽅模块以及⼤多数的依赖项。⽤于⽅便地搭建能够处理超⾼并发、扩展性极⾼的动态 Web 应⽤、 Web 服务和动态⽹关。

OpenResty 通过lua脚本扩展 nginx 功能，可提供负载均衡、请求路由、安全认证、服务鉴权、流量控 制与⽇志监控等服务。

OpenResty&#174; 通过汇聚各种设计精良的 Nginx 模块（主要由 OpenResty 团队⾃主开发），从⽽ 将 Nginx 有效地变成⼀个强⼤的通⽤ Web 应⽤平台。这样， Web 开发⼈员和系统⼯程师可以使⽤ Lua 脚本语⾔调动 Nginx ⽀持的各种 C 以及 Lua 模块，快速构造出⾜以胜任 10K 乃⾄ 1 0K 以上单 机并发连接的⾼性能 Web 应⽤系统。

关于 OpenRestry 的学习，⼤家可以参考：

htp:/openresty.org/cn/

安装依赖库

- 1.
- 2.


yum install wget libtermcap-devel ncurses-devel libevent-devel readline-devel pcredevel gcc openssl openssl-devel per

复制代码

下载安装包

wget

htps:/openresty.org/download/openresty-1.1.2.5.tar.gz

解压安装

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


tar -xf openresty-1.11.2.5.tar.gz cd openresty-1.11.2.5

./configure --prefix=/usr/local/openresty --with-luajit --without-http_redis2_module -with-

http_stub_status_module --with-http_v2_module --with-http_gzip_static_module --withhttp_sub_module --add-module=/usr/local/server/ngx_cache_purge-2.3/ make && make install

复制代码

安装完成后，在 /usr/local/openrestry/nginx ⽬录下是安装好的nginx。

- 3.2 浏览器缓存


客户端侧缓存⼀般指的是浏览器缓存、ap缓存等等，⽬的就是加速各种静态资源的访问，降低服务器 压⼒。 我们通过配置Nginx设置⽹⻚缓存信息，从⽽降低⽤户对服务器频繁访问造成的巨⼤压⼒。我们先配置 ⼀个案例，再 基于案例去讲解Nginx缓存。

- 3.2.1 Nginx Web缓存配置


nginx 提供了 expires 、 etag 、 if-modified-since 指令来进⾏浏览器缓存控制。我们使⽤ expires 来 配置Nginx对⽹⻚的缓存。

语法: expires [modified] time; 默认值: expires of; 上下⽂: htp, server, location, if in location

上传html

将1.html上传到服务器的 /usr/local/server/html ⽬录下。

配置nginx

修改 /usr/local/openrestry/nginx/conf/nginx.conf ⽂件，配置如下：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


server { listen 80; server_name localhost; location / { #静 态 ⽂ 件 路 径

root /usr/local/server/html;

#缓 存 10秒

expires 10s; }

}

复制代码

过期时间配置说明

- 1.
- 2.
- 3.
- 4.


expires 30s; #30秒 expires 30m; #30分 钟 expires 2h; #2个 ⼩ 时 expires 30d; #30天

复 制 代 码

第⼀次请求<

htp:/192.168.21.141/1.html>

第⼆次请求 <

htp:/192.168.21.141/1.html>

- 3.2.2 Htp缓存控制头


参数说明：

HTP 中最基本的缓存机制，涉及到的 HTP 头字段，包括 Cache-Control, Last-Modified, IfModified-Since, Etag,If-None-Match 等。

Last-Modified/If-Modified-Since

Etag是服务端的⼀个资源的标识，在 HTP 响应头中将其传送到客户端。所谓的服务端资源可以是⼀ 个Web⻚⾯，也可以是JSON或XML等。服务器单独负责判断记号是什么及其含义，并在HTP响应头 中将其传送到客户端。⽐如，浏览器第⼀次请求⼀个资源的时候，服务端给予返回，并且返回了ETag: “50b1c1d4f75c61:df3” 这样的字样给浏览器，当浏览器再次请求这个资源的时候，浏览器会将IfNone-Match: W/“50b1c1d4f75c61:df3” 传输给服务端，服务端拿到该ETAG，对⽐资源是否发⽣变 化，如果资源未发⽣改变，则返回304HTP状态码，不返回具体的资源。

Last-Modified ：标示这个响应资源的最后修改时间。web服务器在响应请求时，告诉浏览器资源的最 后修改时间。

If-Modified-Since ：当资源过期时（使⽤Cache-Control标识的max-age），发现资源具有 LastModified 声明，则再次向web服务器请求时带上头。

If-Modified-Since ，表示请求时间。web服务器收到请求后发现有头 If-Modified-Since 则与被请求资 源的最后修改时间进⾏⽐对。若最后修改时间较新，说明资源有被改动过，则响应整⽚资源内容（写 在响应消息包体内），HTP 20；若最后修改时间较旧，说明资源⽆新修改，则响应 HTP 304 (⽆ 需包体，节省浏览)，告知浏览器继续使⽤所保存的 cache。

Pragma⾏是为了兼容 HTP1.0 ，作⽤与 Cache-Control: no-cache 是⼀样的

Etag/If-None-Match

Etag ：web服务器响应请求时，告诉浏览器当前资源在服务器的唯⼀标识（⽣成规则由服务器决定),如 果给定URL中的资源修改，则⼀定要⽣成新的Etag值。

If-None-Match ：当资源过期时（使⽤Cache-Control标识的max-age），发现资源具有Etage声明， 则再次向web服务 器请求时带上头 If-None-Match （Etag的值）。web服务器收到请求后发现有头 If-None-Match 则与 被请求资源的相应校验串进⾏⽐对，决定返回20或304。

Etag：

Last-Modified 标注的最后修改只能精确到秒级，如果某些⽂件在1秒钟以内，被修改多次的话，它将 不能准确标注⽂件的修改时间，如果某些⽂件会被定期⽣成，当有时内容并没有任何变化，但 LastModified 却改变了，导致⽂件没法使⽤缓存有可能存在服务器没有准确获取⽂件修改时间，或者与代 理服务器时间不⼀致等情形 Etag是服务器⾃动⽣成或者由开发者⽣成的对应资源在服务器端的唯⼀标 识符，能够更加准确的控制缓存。 Last-Modified 与 ETag 是可以⼀起使⽤的，服务器会优先验证 ETag ，⼀致的情况下，才会继续⽐对 Last-Modified ，最后才决定是否返回304。

- 3.3 代理缓存


⽤户如果请求获取的数据不是需要后端服务器处理返回，如果我们需要对数据做缓存来提⾼服务器的 处理能⼒，我们 可以按照如下步骤实现：

请求Nginx，Nginx将请求路由给后端服务

后端服务查询Redis或者MySQL，再将返回结果给Nginx

Nginx将结果存⼊到Nginx缓存，并将结果返回给⽤户

⽤户下次执⾏同样请求，直接在Nginx中获取缓存数据

- 3.3.1 proxy_cache


proxy_cache 是⽤于 proxy 模式（⼀般也可称为反代）的缓存功能，proxy_cache 在 Nginx 配置的 htp 段、server 段（location 段）中分别写⼊不同的配置。htp 段中的配置⽤于定义 proxy_cache 空间，server 段中 的配置⽤于调 ⽤ htp 段中的定义，启⽤对 server 的缓存功能。

属性使⽤说明proxy_cache_path：

Example

proxy_cache_path /usr/local/openresty/nginx/cache levels=1 2 keys_zone=openresty_cache:10m max_size=10g inactive=60m use_temp_path=of;

【作⽤】指定缓存存储的路径，缓存存储在/usr/local/openresty/nginx/cache⽬录

【levels=1 2】设置⼀个两级⽬录层次结构存储缓存，在单个⽬录中包含⼤量⽂件会降低⽂件访问速 度，因此我们建议对⼤多数部署使⽤两级⽬录层次结构。如果 levels 未包含该参数，Nginx 会将所有 ⽂件放在同⼀⽬录中。

【keys_zone=openresty_cache:10m】设置共享内存区域，⽤于存储缓存键和元数据，例如使⽤计时 器。拥有内存中的密钥副本，Nginx 可以快速确定请求是否是⼀个 HIT 或 MI S 不必转到磁盘，从⽽⼤ ⼤加快了检查速度。1 MB 区域可以存储⼤约 8, 0 个密钥的数据，因此示例中配置的 10 MB 区域可 以存储⼤约 80, 0 个密钥的数据。

【max_size=10g】设置缓存⼤⼩的上限。它是可选的; 不指定值允许缓存增⻓以使⽤所有可⽤磁盘空 间。当缓存⼤⼩达到限制时，⼀个称为缓存管理器的进程将删除最近最少使⽤的缓存，将⼤⼩恢复到 限制之下的⽂件。

【inactive=60m】指定项⽬在未被访问的情况下可以保留在缓存中的时间⻓度。在此示例中，缓存管 理器进程会⾃动从缓存中删除 60 分钟未请求的⽂件，⽆论其是否已过期。默认值为 10 分钟 （10m）。⾮活动内容与过期内容不同。Nginx 不会⾃动删除缓存 header 定义为已过期内容（例如 Cache-Control:max-age=120）。过期（陈旧）内容仅在指定时间内未 被访问时被删除。访问过期内容时，Nginx 会从原始服务器刷新它并重置 inactive 计时器。

【use_temp_path=of】表示NGINX会将临时⽂件保存在缓存数据的同⼀⽬录中。这是为了避免在更 新缓存时，磁盘之间互相复制响应数据，我们⼀般关闭该功能。

proxy_cache：

设置是否开启对后端响应的缓存，如果开启的话，参数值就是zone的名称，⽐如:proxy_cache openresty_cache;

proxy_cache_valid：

针对不同的response code设定不同的缓存时间，如果不设置code，默认为20,301,302,也可以⽤any 指定所有code Example： 【proxy_cache_valid 20 304 10s;】所有20/304响应的数据都缓存10秒。 【proxy_cache_valid any 1m;】所有请求响应的值都缓存1分钟。

proxy_cache_min_uses：

指定在多少次请求之后才缓存响应内容,这⾥表示将缓存内容写⼊到磁盘。 Example： 【proxy_cache_min_uses 3;】同⼀个请求达到了3次，才将缓存写⼊磁盘。

proxy_cache_lock：

默认不开启，开启的话则每次只能有⼀个请求更新相同的缓存，其他请求要么等待缓存有数据要么限 时等待锁释放;nginx1.1.12才开始有。

proxy_cache_key：

缓存⽂件的唯⼀key，可以根据它实现对缓存⽂件的清理操作

- 3.3.2 缓存操作


我们在 nginx.conf 中添加如下配置：

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.


#缓 存 配 置 proxy_cache_path /usr/local/openresty/nginx/cache levels=1:2

keys_zone=openresty_cache:10m max_size=10g inactive=60m use_temp_path=off; server { listen 80; server_name localhost; #html配 置 location ~ \.html { #静 态 ⽂ 件 路 径 root /usr/local/server/html; #缓 存 10秒 expires 10s; } #⾮ html配 置 location / { #启 ⽤ 缓 存 openresty_cache proxy_cache openresty_cache; #针 对 指 定 请 求 缓 存 #proxy_cache_methods GET; #设 置 指 定 请 求 会 缓 存 proxy_cache_valid 200 304 10s;

- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.


#最 少 请 求 1次 才 会 缓 存 proxy_cache_min_uses 3; #如 果 并 发 请 求 ， 只 有 第 1个 请 求 会 去 服 务 器 获 取 数据 #proxy_cache_lock on; #唯 ⼀ 的 key proxy_cache_key $host$uri$is_args$args; proxy_pass http://myip:18081;

} }

复制代码

此时 /usr/local/openresty/nginx/cache ⽬录下只有1个temp⽂件夹。

我们执⾏3次请求 < ，可以发现此时多了⼀些其他⽬录，这些⽬ 录就是存 储每个请求对应的缓存。

htp:/192.168.21.141/user/wangwu>

