作者： ⽇期： ⽹络应⽤程序，分为前端和后端两个部分。当前的发展趋势，就是前端设备层出不 穷（⼿机、平板、桌⾯电脑、其他专⽤设备 .）。

阮⼀峰 2014年5⽉ 2⽇

因此，必须有⼀种统⼀的机制，⽅便不同的前端设备与后端进⾏通信。这导致API 构架的流⾏，甚⾄出现 的设计思想。 是⽬前⽐较成熟的⼀套 互联⽹应⽤程序的API设计理论。我以前写过⼀篇 ，探讨如何 理解这个概念。

"API First" RESTful API 《理解RESTful架构》

今天，我将介绍RESTful API的设计细节，探讨如何设计⼀套合理、好⽤的API。我 的主要参考了两篇⽂章（ ， ）。

1 2

![image 1](<RESTful API 设计指南.note_images/imageFile1.png>)

# ⼀、协议

API与⽤户的通信协议，总是使⽤ 。

HTPs协议

# ⼆、域名

应该尽量将API部署在专⽤域名之下。

https://api.example.com

## 如果确定API很简单，不会有进⼀步扩展，可以考虑放在主域名下。

https://example.org/api/

# 三、版本（Versioning）

## 应该将API的版本号放⼊URL。

https://api.example.com/v1/

Githu b

另⼀种做法是，将版本号放在HTP头信息中，但不如放⼊URL⽅便和直观。 采⽤这种做法。

# 四、路径（Endpoint）

路径⼜称"终点"（endpoint），表示API的具体⽹址。 在RESTful架构中，每个⽹址代表⼀种资源（resource），所以⽹址中不能有动 词，只能有名词，⽽且所⽤的名词往往与数据库的表格名对应。⼀般来说，数据库 中的表都是同种记录的"集合"（colection），所以API中的名词也应该使⽤复数。 举例来说，有⼀个API提供动物园（zo）的信息，还包括各种动物和雇员的信 息，则它的路径应该设计成下⾯这样。

htps:/api.example.com/v1/zos htps:/api.example.com/v1/animals htps:/api.example.com/v1/employes

# 五、HTP动词

对于资源的具体操作类型，由HTP动词表示。 常⽤的HTP动词有下⾯五个（括号⾥是对应的SQL命令）。

GET（SELECT）：从服务器取出资源（⼀项或多项）。 POST（CREATE）：在服务器新建⼀个资源。 PUT（UPDATE）：在服务器更新资源（客户端提供改变后的完整资源）。 PATCH（UPDATE）：在服务器更新资源（客户端提供改变的属性）。 DELETE（DELETE）：从服务器删除资源。

还有两个不常⽤的HTP动词。

HEAD：获取资源的元数据。 OPTIONS：获取信息，关于资源的哪些属性是客户端可以改变的。

下⾯是⼀些例⼦。

GET /zos：列出所有动物园 POST /zos：新建⼀个动物园 GET /zos/ID：获取某个指定动物园的信息 PUT /zos/ID：更新某个指定动物园的信息（提供该动物园的全部信息） PATCH /zos/ID：更新某个指定动物园的信息（提供该动物园的部分信息） DELETE /zos/ID：删除某个动物园 GET /zos/ID/animals：列出某个指定动物园的所有动物 DELETE /zos/ID/animals/ID：删除某个指定动物园的指定动物

# 六、过滤信息（Filtering）

如果记录数量很多，服务器不可能都将它们返回给⽤户。API应该提供参数，过滤 返回结果。

下⾯是⼀些常⻅的参数。

?limit=10：指定返回记录的数量 ?ofset=10：指定返回记录的开始位置。 ?page=2&per_page=10：指定第⼏⻚，以及每⻚的记录数。 ?sortby=name&order=asc：指定返回结果按照哪个属性排序，以及排序顺 序。 ?animal_type_id=1：指定筛选条件

参数的设计允许存在冗余，即允许API路径和URL参数偶尔有重复。⽐如，GET /zo/ID/animals 与 GET /animals?zo_id=ID 的含义是相同的。

# 七、状态码（Status Codes）

服务器向⽤户返回的状态码和提示信息，常⻅的有以下⼀些（⽅括号中是该状态码 对应的HTP动词）。

20 OK - [GET]：服务器成功返回⽤户请求的数据，该操作是幂等的 （Idempotent）。

- 201 CREATED - [POST/PUT/PATCH]：⽤户新建或修改数据成功。

- 202 Acepted - [*]：表示⼀个请求已经进⼊后台排队（异步任务） 204 NO CONTENT - [DELETE]：⽤户删除数据成功。 40 INVALID REQUEST - [POST/PUT/PATCH]：⽤户发出的请求有错误，服 务器没有进⾏新建或修改数据的操作，该操作是幂等的。 401 Unauthorized - [*]：表示⽤户没有权限（令牌、⽤户名、密码错误）。


- 403 Forbi den - [*] 表示⽤户得到授权（与401错误相对），但是访问是被 禁⽌的。

- 404 NOT FOUND - [*]：⽤户发出的请求针对的是不存在的记录，服务器没 有进⾏操作，该操作是幂等的。 406 Not Aceptable - [GET]：⽤户请求的格式不可得（⽐如⽤户请求JSON 格式，但是只有XML格式）。 410 Gone -[GET]：⽤户请求的资源被永久删除，且不会再得到的。 42 Unprocesable entity - [POST/PUT/PATCH] 当创建⼀个对象时，发⽣⼀ 个验证错误。 50 INTERNAL SERVER EROR - [*]：服务器发⽣错误，⽤户将⽆法判断发 出的请求是否成功。


状态码的完全列表参⻅ 。

这⾥

# ⼋、错误处理（Error handling）

如果状态码是4x，就应该向⽤户返回出错信息。⼀般来说，返回的信息中将eror 作为键名，出错信息作为键值即可。

{

error: "Invalid API key" }

# 九、返回结果

针对不同操作，服务器向⽤户返回的结果应该符合以下规范。

GET /colection：返回资源对象的列表（数组） GET /colection/resource：返回单个资源对象 POST /colection：返回新⽣成的资源对象 PUT /colection/resource：返回完整的资源对象 PATCH /colection/resource：返回完整的资源对象 DELETE /colection/resource：返回⼀个空⽂档

# ⼗、Hypermedia API

RESTful API最好做到Hypermedia，即返回结果中提供链接，连向其他API⽅法， 使得⽤户不查⽂档，也知道下⼀步应该做什么。

⽐如，当⽤户向api.example.com的根⽬录发出请求，会得到这样⼀个⽂档。

{"link": { "rel": "collection ", "href": " ", "title": "List of zoos", "type": "application/vnd.yourformat+json"

https://www.example.com/zoos https://api.example.com/zoos

}}

上⾯代码表示，⽂档中有⼀个link属性，⽤户读取这个属性就知道下⼀步该调⽤什 么API了。rel表示这个API与当前⽹址的关系（colection关系，并给出该colection 的⽹址），href表示API的路径，title表示API的标题，type表示返回类型。

Hypermedia API的设计被称为 。Github的API就是这种设计，访问 会得到⼀个所有可⽤API的⽹址列表。

HATEOAS api.git hub.com

{

"current_user_url": " ", "authorizations_url": " ", // ...

https://api.github.com/user https://api.github.com/authorizations

}

api.github.com/user

从上⾯可以看到，如果想获取当前⽤户的信息，应该去访问 ， 然后就得到了下⾯结果。

{

"message": "Requires authentication", "documentation_url": " "

https://developer.github.com/v3

}

上⾯代码表示，服务器给出了提示信息，以及⽂档的⽹址。

# ⼗⼀、其他

- （1）API的身份认证应该使⽤ 框架。

- （2）服务器返回的数据格式，应该尽量使⽤JSON，避免使⽤XML。 （完）


OAuth 2.0

