<table>
  <tr>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
  </tr>
  <tr>
    <td>协议</td>
    <td>PRC 协议</td>
    <td>RMI<br><br>协议</td>
    <td>WebService 协议</td>
    <td>JMS</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>htp</td>
    <td>tcp/ip</td>
    <td>htp</td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td>紧耦合</td>
    <td> </td>
    <td>松耦合</td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>平台⽀持</td>
    <td>跨语⾔</td>
    <td>只⽀持java</td>
    <td>跨语⾔</td>
    <td>⽀持java，⽀持 跨语⾔</td>
    <td> </td>
    <td> </td>
  </tr>
</table>


RPC（Remote Procedure Call Protocol） RPC使⽤C/S⽅式，采⽤http协议,发送请求到服务器，等待服务器返回结果。这个请求包括⼀个参数集 和⼀个⽂本集，通常形成“classname.methodname”形式。优点是跨语⾔跨平台，C端、S端有更⼤的 独⽴性，缺点是不⽀持对象，⽆法在编译器检查错误，只能在运⾏期检查。

Web Service Web Service提供的服务是基于web容器的，底层使⽤http协议，类似⼀个远程的服务提供者，⽐如天 ⽓预报服务，对各地客户端提供天⽓预报，是⼀种请求应答的机制，是跨系统跨平台的。就是通过⼀ 个servlet，提供服务出去。

⾸先客户端从服务器的到WebService的WSDL，同时在客户端声称⼀个代理类(Proxy Class) 这个代理 类负责与WebService 服务器进⾏Request 和Response 当⼀个数据（XML格式的）被封装成SOAP格式的数据流发送到服务 器端的时候，就会⽣成⼀个进程对象并且把接收到这个Request的SOAP包进⾏解析，然后对事物进⾏ 处理，处理结束以后再对这个计算结果进⾏SOAP 包装，然后把这个包作为⼀个Response发送给客户端的代理类(Proxy Class)，同样地，这个代理类也 对这个SOAP包进⾏解析处理，继⽽进⾏后续操作。这就是WebService的⼀个运⾏过程。

Web Service⼤体上分为5个层次:

- 1. Http传输信道

- 2. XML的数据格式

- 3. SOAP封装格式

- 4. WSDL的描述⽅式

- 5. UDDI UDDI是⼀种⽬录服务，企业可以使⽤它对Webservices进⾏注册和搜索


RMI （Remote Method Invocation）

RMI 采⽤stubs 和 skeletons 来进⾏远程对象(remote object)的通讯。stub 充当远程对象的客户端代 理，有着和远程对象相同的远程接⼝，远程对象的调⽤实际是通过调⽤该对象的客户端代理对象stub 来完成的，通过该机制RMI就好⽐它是本地⼯作，采⽤tcp/ip协议，客户端直接调⽤服务端上的⼀些⽅ 法。优点是强类型，编译期可检查错误，缺点是只能基于 语⾔，客户机与服务器紧耦合。

Java

JMS（ Messaging Service） JMS是Java的消息服务，JMS的客户端之间可以通过JMS服务进⾏异步的消息传输。JMS⽀持两种消 息模型：Point-to-Point（P2P）和Publish/Subscribe（Pub/Sub），即点对点和发布订阅模型。

Java

# ⼏者的区别与联系

## 1、RPC与RMI

- （1）RPC 跨语⾔，⽽ RMI只⽀持Java。

- （2）RMI 调⽤远程对象⽅法，允许⽅法返回 Java 对象以及基本数据类型，⽽RPC 不⽀持对象的概 念，传送到 RPC 服务的消息由外部数据表示 (External Data Representation, XDR) 语⾔表示，这种语 ⾔抽象了字节序类和数据类型结构之间的差异。只有由 XDR 定义的数据类型才能被传递， 可以说 RMI 是⾯向对象⽅式的 Java RPC 。

- （3）在⽅法调⽤上，RMI中，远程接⼝使每个远程⽅法都具有⽅法签名。如果⼀个⽅法在服务器上执 ⾏，但是没有相匹配的签名被添加到这个远程接⼝上，那么这个新⽅法就不能被RMI客户⽅所调⽤。 在RPC中，当⼀个请求到达RPC服务器时，这个请求就包含了⼀个参数集和⼀个⽂本值，通常形成 “classname.methodname”的形式。这就向RPC服务器表明，被请求的⽅法在为 “classname”的类中， 名叫“methodname”。然后RPC服务器就去搜索与之相匹配的类和⽅法，并把它作为那种⽅法参数类型 的输⼊。这⾥的参数类型是与RPC请求中的类型是匹配的。⼀旦匹配成功，这个⽅法就被调⽤了，其 结果被编码后返回客户⽅。


## 2、JMS和RMI

采⽤JMS 服务，对象是在物理上被异步从⽹络的某个JVM 上直接移动到另⼀个JVM 上（是消息通知机 制） ⽽RMI 对象是绑定在本地JVM 中，只有函数参数和返回值是通过⽹络传送的（是请求应答机制）。

RMI⼀般都是同步的，也就是说，当client调⽤Server的⼀个⽅法的时候，需要等到对⽅的返回，才能 继续执⾏client端，这个过程调⽤本地⽅法感觉上是⼀样的，这也是RMI的⼀个特点。

JMS ⼀般只是⼀个点发出⼀个Message到Message Server,发出之后⼀般不会关⼼谁⽤了这个 message。 所以，⼀般RMI的应⽤是紧耦合，JMS的应⽤相对来说是松散耦合应⽤。

## 3、Webservice与RMI

RMI是在tcp协议上传递可序列化的java对象，只能⽤在java虚拟机上，绑定语⾔，客户端和服务端都必 须是java webservice没有这个限制，webservice是在http协议上传递xml⽂本⽂件，与语⾔和平台⽆关

## 4、Webservice与JMS

Webservice专注于远程服务调⽤，jms专注于信息交换。

⼤多数情况下Webservice是两系统间的直接交互（Consumer <--> Producer），⽽⼤多数情况下jms是 三⽅系统交互（Consumer <- Broker -> Producer）。当然，JMS也可以实现request-response模式的 通信，只要Consumer或Producer其中⼀⽅兼任broker即可。

JMS可以做到异步调⽤完全隔离了客户端和服务提供者，能够抵御流量洪峰； WebService服务通常为 同步调⽤，需要有复杂的对象转换，相⽐SOAP，现在JSON，rest都是很好的http ⽅案；（举⼀个 例⼦，电⼦商务的分布式系统中，有⽀付系统和业务系统，⽀付系统负责⽤户付款，在⽤户在银⾏付 款后需要通知各个业务系统，那么这个时候，既可以⽤同步也可以⽤异步，使⽤异步的好处就能抵御 ⽹站暂时的流量⾼峰，或者能应对慢消费者。）

架构

JMS是java平台上的消息规范。⼀般jms消息不是⼀个xml，⽽是⼀个java对象，很明显，jms没考虑异 构系统，说⽩了，JMS就没考虑⾮java的东⻄。但是好在现在⼤多数的jms provider（就是JMS的各种 实现产品）都解决了异构问题。相⽐WebService的跨平台各有千秋吧。

