# ⼀、前⾔

Dubbo默认的底层⽹络通讯是使⽤Netty来做的，在服务提供⽅NettyServer使⽤两级线程池，其中 EventLoopGroup(boss)主要⽤来接受客户端的链接请求，并把接受的请求分发给EventLoopGroup （worker）来处理，boss和worker线程组我们称为IO线程。 如果服务提供⽅的逻辑能迅速完成，并且不会发起新的 IO 请求，则直接在 IO 线程上处理更快，因为 这减少了线程池调度。 但如果处理逻辑较慢，或者需要发起新的 IO 请求，⽐如需要查询数据库，则必须派发到新线程池，否 则 IO 线程阻塞，将导致不能接收其它请求。

# ⼆、Dubbo提供的线程模型

all 所有消息都派发到线程池，包括请求，响应，连接事件，断开事件，⼼跳等,模型如下图

![image 1](<Dubbo剖析-线程模型.note_images/imageFile1.png>)

image.png

direct 所有消息都不派发到线程池，全部在 IO 线程上直接执⾏，模型如下图

![image 2](<Dubbo剖析-线程模型.note_images/imageFile2.png>)

image.png

execution 只请求消息派发到线程池，不含响应，响应和其它连接断开事件，⼼跳等消息，直接在 IO 线程上执⾏，模型如下图

![image 3](<Dubbo剖析-线程模型.note_images/imageFile3.png>)

image.png

connection 在 IO 线程上，将连接断开事件放⼊队列，有序逐个执⾏，其它消息派发到线程池。

![image 4](<Dubbo剖析-线程模型.note_images/imageFile4.png>)

image.png

其中ThreadPool的spi实现有如下：

fixed 固定⼤⼩线程池，启动时建⽴线程，不关闭，⼀直持有。(缺省) cached 缓存线程池，空闲⼀分钟⾃动删除，需要时重建。 limited 可伸缩线程池，但池中的线程数只会增⻓不会收缩。只增⻓不收缩的⽬的是为了避免收缩时 突然来了⼤流量引起的性能问题。

# 三、总结

dubbo提供了常⽤的线程模型和线程池扩展各有利弊，如果您有定制化需要，可以按照spi规范进⾏定 制。

