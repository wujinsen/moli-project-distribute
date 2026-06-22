htp:/tutorials.jenkov.com/java-nio/index.html

原⽂地址： 作者：Jakob Jenkov 译者：郭蕾 校对：⽅腾⻜ Java NIO(New IO)是⼀个可以替代标准Java IO API的IO API（从Java 1.4开始)，Java NIO提供了与标 准IO不同的IO⼯作⽅式。 Java NIO: Chanels and Bufers（通道和缓冲区） 标准的IO基于字节流和字符流进⾏操作的，⽽NIO是基于通道（Chanel）和缓冲区（Bufer）进⾏操 作，数据总是从通道读取到缓冲区中，或者从缓冲区写⼊到通道中。 Java NIO: Asynchronous IO（异步IO） Java NIO可以让你异步的使⽤IO，例如：当线程从通道读取数据到缓冲区时，线程还是可以进⾏其他 事情。当数据被写⼊到缓冲区时，线程可以继续处理它。从缓冲区写⼊通道也类似。 Java NIO: Selectors（选择器） Java NIO引⼊了选择器的概念，选择器⽤于监听多个通道的事件（⽐如：连接打开，数据到达）。因 此，单个的线程可以监听多个数据通道。 下⾯是Java NIO系列⽂章的⽬录：

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


Java NIO概述 Java NIO Chanel Java NIO Bufer Java NIO Scater / Gather Java NIO 通道之间的数据传输 Java NIO Selector Java NIO FileChanel Java NIO SocketChanel Java NIO ServerSocketChanel Java NIO DataGramChanel Java NIO Pipe Java NIO 与IO

