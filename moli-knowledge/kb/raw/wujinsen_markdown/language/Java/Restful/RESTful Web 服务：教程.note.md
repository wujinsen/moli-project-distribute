随着 REST 成为⼤多数 Web 和 Mobile 应⽤的默认选择，势必要对它的基本原理有所了解。 在它提出⼗多年后的今天，REST 已经成为最重要的 Web 应⽤技术之⼀。随着所有技术朝着 API ⽅向 发展，它的重要性有可能持续快速地增⻓。每⻔主要编程语⾔现在已经包含构建 RESTful Web 服务的 框架。同样地，Web 开发者和架构师对 REST 和 RESTful 服务有⼀个清晰的理解是很重要的。这篇 教程解释了 REST 架构，然后研究使⽤它构建通⽤地基于API的任务的细节。

## 什么是 REST

REST 代表表述性状态转移（representational state transfer），它是⼀种⽹络化超媒体应⽤的架构 ⻛格。它主要是⽤于构建轻量级的、可维护的、可伸缩的 Web 服务。基于 REST 的服务被称为 RESTful 服务。REST 不依赖于任何协议，但是⼏乎每个 RESTful 服务使⽤ HTTP 作为底层协议。 RESTful 使⽤HTTP post（创建、更新）数据、读取数据、删除数据。使⽤HTTP实现CRUD（创建、 读取、更新、删除）操作。

## RESTful 服务特点：

每个系统都使⽤资源。这些资源可以是图⽚，视频⽂件，⽹⻚，商业信息，或者在基于计算机的系统 中可以被代表的任何事物。服务的⽬的是提供⼀个窗⼝给客户端以便客户端能访问这些资源。服务架 构师和开发⼈员想要这些服务变得易于实现、维护、扩展、伸缩。RESTful 架构允许这些，甚⾄更 多。⼀般来说，RESTful 服务应该有下⾯的属性和特征，也就是我要详细描述的内容：

模型表示（Representations） 消息（Messages） URIs ⼀致接⼝（Uniform interface） （⽆状态）Stateless 资源之间的链接（Links between resources） 缓存（Caching）

## 模型表示（Representations）

RESTful 服务的焦点在资源上和怎么提供对资源的访问。资源很容易被认为和OOP中的对象⼀样。⼀ 个资源能由其他资源组成。当设计⼀个系统的时候，第⼀件要做的事情是定义资源和决定资源之间的 关系。这有点像设计数据库的第⼀步。定义实体和关系。 ⼀旦我们定义了资源，接下来我们需要找到⼀种⽤于在系统中表示这些资源的⽅法。你可以使⽤任何 格式来表示资源。REST 对此没有限制。 例如，根据你的需求，你可以决定使⽤ JSON 或者 XML。如果你在构建 Web 服务，此服务⽤于 Web ⻚⾯中的 AJAX 调⽤，那 JSON 是很好地选择。 XML 可以⽤来表示⽐较复杂的资源。例如⼀个 被称为“Person”的资源可以表示如下：

- 列表1：资源的JSON 表示。


{

"ID": "1", "Name": "M Vaqqas", "Email": "m.vaqqas@gmail.com", "Country": "India"

}

## 列表2：资源的XML 表示。 <Person>

<ID>1</ID> <Name>M Vaqqas</Name> <Email>m.vaqqas@gmail.com</Email> <Country>India</Country>

</Person>

实际上，你可以使⽤不⽌⼀种的格式并且决定使⽤其中哪⼀种⽤于依赖于客户端类型或⼀些请求参数 的响应。⽆论使⽤哪个格式，好的模型表示（representation ）应该具有以下明显的特征：

客户端和服务端应该能够理解这种模型表示（representation ）的格式。 模型表示（representation ）应该能够完整的表示资源。如果需要表示部分资源，然后你需要考虑 将资源分解成⼦资源。分割⼤资源到更⼩的资源同样允许你传递更⼩的表现。较⼩的模型表示 （representation）意味着更少的时间来创建和传输。这也意味着更快的服务。 模型表示（representation）应该能够互相链接资源。可以通过替换 URI 或者是唯⼀ ID。

消息（Messages）

客户端和服务端经由消息相互沟通。客户端发送请求到服务器，服务器使⽤响应答复。除了实际的数 据，这些信息也包含⼀些关于消息的元数据。对于设计 RESTful 服务了解 HTTP 1.1的请求格式和响应 格式是很重要的。

HTTP 请求

- 图1中展示了HTTP请求格式。


- 图1：HTTP 请求格式 GET, PUT, POST, DELETE, OPTIONS等等 HTTP ⽅法的⼀种。 资源的URI。操作将在这个 URI 上执⾏。 HTTP 版本，通常是“HTTP v1.1”。 包含 header 键值对集合的元数据。这些设置包含消息的信息和发送者像客户端的类型，客户端⽀持的 格式，消息体的格式类型，响应的缓存设置，和许多信息。 是实际的消息内容。在 RESTful 服务中，这就是消息中资源表示的位置。 在 HTML 消息中没有标签和标识标记区块的开始或结束。 列表三是简单的 POST 请求消息，这个请求想要插⼊⼀条新的 Person 资源。


## 列表3：简单 POST 请求

POST http://MyService/Person/ Host: MyService Content-Type: text/xml; charset=utf-8 Content-Length: 123 <?xml version="1.0" encoding="utf-8"?> <Person>

<ID>1</ID> <Name>M Vaqqas</Name> <Email>m.vaqqas@gmail.com</Email> <Country>India</Country>

</Person>

- 列表4：GET 请求 GET http://www.w3.org/Protocols/rfc2616/rfc2616.html HTTP/1.1 Host: www.w3.org Accept: text/html,application/xhtml+xml,application/xml; … User-Agent: Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 … Accept-Encoding: gzip,deflate,sdch Accept-Language: en-US,en;q=0.8,hi;q=0.6 HTTP Response

- 图2展示了 HTTP 响应的格式：


- 图2：HTTP 响应格式。 服务器返回 ，包含请求的状态。通常是三位数字的HTTP状态码（ ）。 包含关于响应消息的元数据和设置。 包含如果请求成功的模型表示（representation）。 列表5是我从清单三的请求中得到的真实响应。


- 列表5：真实的 GET 请求的响应。 HTTP/1.1 200 OK Date: Sat, 23 Aug 2014 18:31:04 GMT Server: Apache/2 Last-Modified: Wed, 01 Sep 2004 13:24:52 GMT Accept-Ranges: bytes Content-Length: 32859 Cache-Control: max-age=21600, must-revalidate Expires: Sun, 24 Aug 2014 00:31:04 GMT Content-Type: text/html; charset=iso-8859-1 <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> <html xmlns='http://www.w3.org/1999/xhtml'> <head><title>Hypertext Transfer Protocol -- HTTP/1.1</title></head> <body>


3-digit HTTP status code

...

响应码 200 OK 表示⼀切正常，并且消息体包含我请求的有效的模型表示（representation）。在这 个例⼦中，模型表示（representation）是 HTML ⽂档，HTML ⽂档是通过在响应头中的 ContentType 声明地。在这个消息中的 header 是不解⾃明地，但是我将会在接下来的⽂章中讨论他们中的⼀ 些。这有很多其他 。你可以使⽤叫 的免费⼯具抓取调试这些HTTP请求和响应。

属性 Fiddle

## 资源定位

REST 要求每个资源⾄少有⼀个 URI。 RESTful 服务使⽤⼈类可读的URIs层级⽬录来定位资源。URI 要做的⼯作是定义⼀个资源或资源集合。实际的操作由 HTTP 动作决定。URI 应该没有任何关于处理 和动作的内容。这使我们能够调⽤相同的 URI 使⽤不同的 HTTP 动词来执⾏不同的操作。 假设我们有⼀个 person 的数据库并且我们希望通过服务器暴露给外部。Person 资源可以像下⾯这样 被定位到：

http://MyService/Persons/1

此URL遵循格式：Protocol://ServiceName/ResourceType/ResourceID 对于构建良好的 URIs 这有些重要的推荐：

使⽤复数名词命名你的资源。 避免使⽤制造混乱的空格。使⽤_或者-代替。 URI 不区分⼤⼩写。为了更清晰我使⽤驼峰写法。你也可以使⽤全部⼩写的URIs。 你也能够有你⾃⼰的约定，但是要在整个服务保持⼀致。确保你的客户端都知道这个约定。你的客 户端 URIs 程序构建将更简单如果它们知道你遵循的资源层级和URI约定。 好的 URI 是不会变更的。再决定服务的 URIs 之前要先思考思考。如果你需要改变资源的定位，不 要放弃⽼的 URI。如果请求来⾃⽼的 URI，使⽤状态码300重定向客户端到新的location。 避免使⽤动词命名你的资源直到你的资源是⼀个实际地操作或过程。动词更加适合操作的命名。例 如，RESTful 服务不应该有 或

http://MyService/FetcthPerson/1 http://MyService/DeletePerso n?id=1

类似的 URI。

## URI中的查询参数

前⾯的URI是⽤查询参数帮助构建的。

http://MyService/Persons?id=1

查询参数⽅法运⾏良好⽽且 REST 不会阻⽌你使⽤查询参数。然⽽，这种⽅式有⼀些劣势：

增加了复杂性，降低了可读性。如果你使⽤更多的参数问题会更加明显。 像Google这样的搜索引擎爬⽹程序和索引器忽略uri查询参数。如果你正在进⾏Web开发，这是你 的Web服务⼀部分很⼤的劣势，导致搜索引擎屏蔽。

查询参数的基本⽬的是提供参数给需要的数据项的操作。例如，如果你想要模型表示（presentation） 格式由客户端决定，你可以通过参数实现像下⾯这样。

http://MyService/Persons/1?format=xml&encoding=UTF8 http://MyService/Persons/1?format=json&encoding=UTF8

包含format和encoding参数的⽗⼦层级URI看上去逻辑上不正确因为它们没有这种关系。

http://MyService/Persons/1/json/UTF8

查询参数也允许可选参数。在URI中显然是不可能的。你仅仅应该在提供参数值给处理过程的时候使 ⽤。

## 统⼀接⼝

RESTful应该有统⼀接⼝。HTTP 1.1 提供了⼀系列⽅法，被称为动作。在这其中⽐较重要的动作是：

![image 1](<RESTful Web 服务：教程.note_images/imageFile1.png>)

安全的操作是指对原始资源值不会产⽣影响的操作。列如，数学上的操作除以1就是安全的操作，因为 ⽆论多少次⽤1除⼀个数，原始数值都不会改变。幂等操作是指⽆论多少次执⾏都给出相同结果的操 作。例如，数学上的乘以0就是幂等的，因为⽆论计算多少次结果都是零，结果都是⼀样的。类似的， ⼀个安全的HTTP⽅法不会使服务器上的资源发⽣变化。⼀个幂等的HTTP⽅法⽆论执⾏多少次都会有 相同的响应。把⽅法分类成安全和幂等的可以使客户端在不稳定的Web环境中再次触发相同的请求的 结果变得更可预测。 在 Web 上 GET 可能是最流⾏的⽅法。它⽤来获取资源。 HEAD 仅返回响应头和空的响应体。这个⽅法可以⽤在你不需要全部的资源模型表示 （representation）的时候。例如，HEAD 可以快速检测服务器上的资源是否存在。 OPTIONS ⽤于获取资源允许的操作。例如，思考下⾯的请求：

OPTIONS http://MyService/Persons/1 HTTP/1.1 HOST: MyService

在服务验证之后请求返回下⾯内容：

200 OK Allow: HEAD, GET, PUT

第⼆⾏包含客户端可以使⽤的⽅法。 你应该仅仅出于它们的实际意义使⽤这些⽅法。例如：绝不要使⽤ GET 在服务器上创建或删除资源。 如果你没这样做，将会扰乱你的客户端导致他们做出意外的操作。举例说明，然我们考虑下⾯的请 求：

GET http://MyService/DeletePersons/1 HTTP/1.1 HOST: MyService

根据 HTTP 1.1 规范，GET 请求的⽬的是从服务器获取资源。但是它很容易实现⼀个删除 Person 的 请求。这个请求也许运⾏的很棒，但是这不是RESTful 设计。换⾔之，使⽤ DELETE ⽅法来删除资源 像下⾯这样：

DELETE http://MyService/Persons/1 HTTP/1.1 HOST: MyService

REST 建议统⼀接⼝，HTTP 提供了统⼀接⼝。然⽽，这由服务架构师和开发⼈员保持它的统⼀。

## PUT 和 POST 的区别

对于这两个⽅法我提供了⼏乎相同的简短描述。这两个⽅法困扰着许多开发⼈员。所以让我们单独地 讨论他们。 PUT 和 POST 的关键不同在于 PUT 是幂等的，⽽ POST 不是。 另⼀个不同，使⽤ PUT 你需要定义资源完整的 URI。这意味着客户端能构造资源的URI哪怕资源不存 在于服务器上。客户端选择资源唯⼀的名字或 ID 是可能的。就像在服务器上创建⼀个⽤户需要客户端 选择⽤户 ID。如果客户端不能猜测出资源完整的URI，你别⽆选择，只能使⽤ POST。

![image 2](<RESTful Web 服务：教程.note_images/imageFile2.png>)

很明显，PUT 请求不会修改或创建超过⼀个资源，⽆论触发多少次（如果URI相同）。当资源存在时 PUT 和 POST 是没有区别的，都是更新已存在资源。第三个请求（POST

http://MyService/Person s/

）会在每次触发都创建资源。许多开发⼈员认为 REST 不允许 POST 被⽤于更新操作。然⽽， REST 并没有这样的限制。

## ⽆状态

RESTful 服务是⽆状态的并且不会为任何客户端保持状态。⼀个请求不应该依赖过去的请求，服务对 待每个请求都是独⽴的。HTTP 是⽆状态协议的设计，你需要做⼀些额外的事情实现状态服务。使⽤ 当前的技术真的很容易实现状态服务。我们需要清楚的理解⽆状态和有状态设计以便我们避免误解。 ⽆状态设计像这样：

- Request1: GET HTTP/1.1

- Request2: GET HTTP/1.1 每个请求都能被单独对待。 有状态设计，像这样：


- http://MyService/Persons/1
- http://MyService/Persons/2


- Request1: GET HTTP/1.1

- Request2: GET HTTP/1.1 为了处理第⼆个请求，服务器需要记住客户端最后获取的 PersonID。换句话说，服务器需要记住当前 状态————否则请求2⽆法处理。设计你的服务的⽅式是⼀个请求绝不要涉及前⼀个请求。⽆状态服 务更容易集群，更容易维护，更容易伸缩。这样的服务提供了更好的响应时间，因为它们能容易的负 载均衡。 资源之间的链接


http://MyService/Persons/1 http://MyService/NextPerson

资源模型表示（representation ）可以包含其他资源的链接就像 HTML ⻚⾯包含到其他⻚⾯的链接⼀ 样。被服务返回的模型表示（representations ）应该能驱动处理流就像⽹站的情况⼀样。当访问⽹站 的时候，⾸先是索引⻚⾯，单击其中的⼀个链接跳转到另外⼀个⻚⾯等等。 让我们考虑下客户端请求⼀个包含许多其他资源的资源。替代输⼊所有资源，你可能会列出资源的链 接。 例如，如果多个Person是Club的⼀部分，那么Club能像列表6中⼀样表示。

## 列表6：Club <Club>

<Name>Authors Club</Name> <Persons>

<Person>

<Name>M. Vaqqas</Name> <URI>http://MyService/Persons/1</URI>

</Person> <Person>

<Name>S. Allamaraju</Name> <URI>http://MyService/Persons/12</URI>

</Person> </Persons>

</Club>

## 缓存

缓存是存贮⽣成结果的概念，使⽤存储结果替代在不久的将来重复的请求⽣成的结果。缓存可以在客 户端、服务端或者他们之间的任何组件上完成，⽐如代理服务器。缓存是提升服务器性能的很棒的⽅ 法。但如果不妥善处理,会导致客户端使⽤失效的结果。 缓存可以由下⾯的HTTP头控制：

![image 3](<RESTful Web 服务：教程.note_images/imageFile3.png>)

这些头部的值可以组合起来⽤在Cache-Control指令中来检查缓存结果是否有效。最通⽤的⽤于 Cache-Control的指令如下：

![image 4](<RESTful Web 服务：教程.note_images/imageFile4.png>)

服务决定这些头和指令的值是根据资源的特性。

## ⽂档化 RESTful 服务

RESTful 服务不必包含⽤于帮助客户端发现它的⽂档。因为URIs、链接、统⼀接⼝，在运⾏时极其容 易发现 RESTful 服务。客户端仅需要简单地知道服务的基础地址，并且从这个地址客户端通过遍历资 源正在使⽤的链接就能发现服务。OPTION ⽅法能被有效地⽤在发现服务的处理过程中。 这不意味着 RESTful 服务⼀点也不需要⽂档化。没有理由不⽂档化你的服务。你应该为开发⼈员和客 户端⽂档化每个资源和URI。你可以⽤任何格式构建你的⽂档，但是它应该包含⾜够关于资源、URIs、 可⽤⽅法的信息，和其他需要访问你的服务使⽤的信息。下⾯的 table 是我的 MyService 的简单⽂ 档。这个简单短⼩的⽂档包含了 MyService 的各⽅⾯并且⾜够开发⼀个客户端。 Service Name：MyService Address: http://MyService/

![image 5](<RESTful Web 服务：教程.note_images/imageFile5.png>)

你也可以⽂档化每个资源的模型表示（representations ）并且提供⼀些简单的模型表示 （representations ）。

## 结论

开发轻量级的 Web 服务 REST 是极好的⽅法，容易实现，维护、暴露开放。HTTP 提供了卓越的接 ⼝来实现 RESTful 服务，⽐如统⼀接⼝和缓存。然⽽，那要开发⼈员正确地实现和利⽤这些特性。如 果我们对基础有了正确地理解，那么使⽤现有的技术⽐如Python、.net、java实现REST服务是很容 的。我希望这篇⽂章为你开始开发你⾃⼰的RESTful服务提供了⾜够的信息。

# 参考

http://rest.elkstein.org/2008/02/what-is-rest.html http://www.drdobbs.com/web-development/restful-web-services-a-tutorial/240169069 https://github.com/wanbei/blog/blob/master/html/restful-web-services.md

