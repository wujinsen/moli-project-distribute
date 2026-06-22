我们深谙信息交流的价值，那⽹络中进程之间如何通信，如我们每天打开浏览器浏览⽹⻚时，浏览器 的进程怎么与web服务器通信的？当你⽤QQ聊天时，QQ进程怎么与服务器或你好友所在的QQ进程通 信？这些都得靠socket？那什么是socket？socket的类型有哪些？还有socket的基本函数，这些都是本⽂ 想介绍的。本⽂的主要内容如下：

- 1、⽹络中进程之间如何通信？

- 2、Socket是什么？

- 3、socket的基本操作

- 3.1、socket()函数

- 3.2、bind()函数

- 3.3、listen()、connect()函数

- 3.4、accept()函数

- 3.5、read()、write()函数等

- 3.6、close()函数


- 4、socket中TCP的三次握⼿建⽴连接详解

- 5、socket中TCP的四次握⼿释放连接详解

- 6、⼀个例⼦（实践⼀下）

- 7、留下⼀个问题，欢迎⼤家回帖回答！！！


- 1、⽹络中进程之间如何通信？

本地的进程间通信（IPC）有很多种⽅式，但可以总结为下⾯4类：

但这些都不是本⽂的主题！我们要讨论的是⽹络中进程之间如何通信？⾸要解决的问题是如何唯⼀标 识⼀个进程，否则通信⽆从谈起！在本地可以通过进程PID来唯⼀标识⼀个进程，但是在⽹络中这是⾏ 不通的。其实TCP/IP协议族已经帮我们解决了这个问题，⽹络层的“ip地址”可以唯⼀标识⽹络中的主机，⽽ 传输层的“协议+端⼝”可以唯⼀标识主机中的应⽤程序（进程）。这样利⽤三元组（ip地址，协议，端⼝）就 可以标识⽹络的进程了，⽹络中的进程通信就可以利⽤这个标志与其它进程进⾏交互。 使⽤TCP/IP协议的应⽤程序通常采⽤应⽤编程接⼝：UNIX BSD的套接字（socket）和UNIX System V 的TLI（已经被淘汰），来实现⽹络进程之间的通信。就⽬前⽽⾔，⼏乎所有的应⽤程序都是采⽤ socket，⽽现在⼜是⽹络时代，⽹络中进程通信是⽆处不在，这就是我为什么说“⼀切皆socket”。

- 2、什么是Socket？


消息传递（管道、FIFO、消息队列） 同步（互斥量、条件变量、读写锁、⽂件和写记录锁、信号量） 共享内存（匿名的和具名的） 远程过程调⽤（Solaris⻔和Sun RPC）

上⾯我们已经知道⽹络中的进程是通过socket来通信的，那什么是socket呢？socket起源于Unix，⽽ Unix/ 基本哲学之⼀就是“⼀切皆⽂件”，都可以⽤“打开open –> 读写write/read –> 关闭close”模式来 操作。我的理解就是Socket就是该模式的⼀个实现，socket即是⼀种特殊的⽂件，⼀些socket函数就是对 其进⾏的操作（读/写IO、打开、关闭），这些函数我们在后⾯进⾏介绍。

### Linux

socket⼀词的起源

在组⽹领域的⾸次使⽤是在1970年2⽉12⽇发布的⽂献 中发现的，撰写者为Stephen Carr、Steve Crocker和Vint Cerf。根据美国计算机历史博物馆的记载，Croker写道：“命名空间的元素都可称为套接字接⼝。⼀个套接字接⼝构成⼀个 连接的⼀端，⽽⼀个连接可完全由⼀对套接字接⼝规定。”计算机历史博物馆补充道：“这⽐BSD的套接字接⼝定义早了⼤ 约12年。”

IETF RFC33

# 3、socket的基本操作

既然socket是“open—write/read—close”模式的⼀种实现，那么socket就提供了这些操作对应的函数接 ⼝。下⾯以TCP为例，介绍⼏个基本的socket接⼝函数。

- 3.1、socket()函数

int socket(int domain, int type, int protocol); socket函数对应于普通⽂件的打开操作。普通⽂件的打开操作返回⼀个⽂件描述字，⽽socket()⽤于创建 ⼀个socket描述符（socket descriptor），它唯⼀标识⼀个socket。这个socket描述字跟⽂件描述字⼀样， 后续的操作都有⽤到它，把它作为参数，通过它来进⾏⼀些读写操作。 正如可以给fopen的传⼊不同参数值，以打开不同的⽂件。创建socket的时候，也可以指定不同的参数创 建不同的socket描述符，socket函数的三个参数分别为：

注意：并不是上⾯的type和protocol可以随意组合的，如SOCK_STREAM不可以跟IPPROTO_UDP组 合。当protocol为0时，会⾃动选择type类型对应的默认协议。 当我们调⽤socket创建⼀个socket时，返回的socket描述字它存在于协议族（address family，AF_XXX） 空间中，但没有⼀个具体的地址。如果想要给它赋值⼀个地址，就必须调⽤bind()函数，否则就当调⽤ connect()、listen()时系统会⾃动随机分配⼀个端⼝。

- 3.2、bind()函数


domain：即协议域，⼜称为协议族（family）。常⽤的协议族有，AF_INET、AF_INET6、 AF_LOCAL（或称AF_UNIX，Unix域socket）、AF_ROUTE等等。协议族决定了socket的地址类 型，在通信中必须采⽤对应的地址，如AF_INET决定了要⽤ipv4地址（32位的）与端⼝号（16位 的）的组合、AF_UNIX决定了要⽤⼀个绝对路径名作为地址。 type：指定socket类型。常⽤的socket类型有，SOCK_STREAM、SOCK_DGRAM、SOCK_RAW、 SOCK_PACKET、SOCK_SEQPACKET等等（socket的类型有哪些？）。 protocol：故名思意，就是指定协议。常⽤的协议有，IPPROTO_TCP、IPPTOTO_UDP、 IPPROTO_SCTP、IPPROTO_TIPC等，它们分别对应TCP传输协议、UDP传输协议、STCP传输协 议、TIPC传输协议（这个协议我将会单独开篇讨论！）。

正如上⾯所说bind()函数把⼀个地址族中的特定地址赋给socket。例如对应AF_INET、AF_INET6就是把 ⼀个ipv4或ipv6地址和端⼝号组合赋给socket。

int bind(int sockfd, const struct sockaddr *addr, socklen_t addrlen); 函数的三个参数分别为：

sockfd：即socket描述字，它是通过socket()函数创建了，唯⼀标识⼀个socket。bind()函数就是将给这 个描述字绑定⼀个名字。 addr：⼀个const struct sockaddr *指针，指向要绑定给sockfd的协议地址。这个地址结构根据地址创 建socket时的地址协议族的不同⽽不同，如ipv4对应的是：

struct sockaddr_in { sa_family_t sin_family; /* address family: AF_INET */ in_port_t sin_port; /* port in network byte order */ struct in_addr sin_addr; /* internet address */

};

/* Internet address. */ struct in_addr {

uint32_t s_addr; /* address in network byte order */

}; ipv6对应的是： struct sockaddr_in6 {

sa_family_t sin6_family; /* AF_INET6 */ in_port_t sin6_port; /* port number */ uint32_t sin6_flowinfo; /* IPv6 flow information */ struct in6_addr sin6_addr; /* IPv6 address */ uint32_t sin6_scope_id; /* Scope ID (new in 2.4) */

};

struct in6_addr { unsigned char s6_addr[16]; /* IPv6 address */

}; Unix域对应的是： #define UNIX_PATH_MAX 108

struct sockaddr_un { sa_family_t sun_family; /* AF_UNIX */ char sun_path[UNIX_PATH_MAX]; /* pathname */

};

addrlen：对应的是地址的⻓度。

通常服务器在启动的时候都会绑定⼀个众所周知的地址（如ip地址+端⼝号），⽤于提供服务，客户就 可以通过它来接连服务器；⽽客户端就不⽤指定，有系统⾃动分配⼀个端⼝号和⾃身的ip地址组合。这 就是为什么通常服务器端在listen之前会调⽤bind()，⽽客户端就不会调⽤，⽽是在connect()时由系统随机⽣ 成⼀个。

⽹络字节序与主机字节序

主机字节序就是我们平常说的⼤端和⼩端模式：不同的CPU有不同的字节序类型，这些字节序是指整数在内存中保存的顺 序，这个叫做主机序。引⽤标准的Big-Endian和Little-Endian的定义如下：

- a) Little-Endian就是低位字节排放在内存的低地址端，⾼位字节排放在内存的⾼地址端。

- b) Big-Endian就是⾼位字节排放在内存的低地址端，低位字节排放在内存的⾼地址端。


⽹络字节序：4个字节的32 bit值以下⾯的次序传输：⾸先是0～7bit，其次8～15bit，然后16～23bit，最后是24~31bit。这种 传输次序称作⼤端字节序。由于TCP/IP⾸部中所有的⼆进制整数在⽹络中传输时都要求以这种次序，因此它⼜称作⽹络 字节序。字节序，顾名思义字节的顺序，就是⼤于⼀个字节类型的数据在内存中的存放顺序，⼀个字节的数据没有顺序的 问题了。 所以：在将⼀个地址绑定到socket的时候，请先将主机字节序转换成为⽹络字节序，⽽不要假定主机字节序跟⽹络字节序 ⼀样使⽤的是Big-Endian。由于这个问题曾引发过⾎案！公司项⽬代码中由于存在这个问题，导致了很多莫名其妙的问 题，所以请谨记对主机字节序不要做任何假定，务必将其转化为⽹络字节序再赋给socket。

## 3.3、listen()、connect()函数

如果作为⼀个服务器，在调⽤socket()、bind()之后就会调⽤listen()来监听这个socket，如果客户端这时调⽤ connect()发出连接请求，服务器端就会接收到这个请求。 int listen(int sockfd, int backlog); int connect(int sockfd, const struct sockaddr *addr, socklen_t addrlen); listen函数的第⼀个参数即为要监听的socket描述字，第⼆个参数为相应socket可以排队的最⼤连接个 数。socket()函数创建的socket默认是⼀个主动类型的，listen函数将socket变为被动类型的，等待客户的 连接请求。 connect函数的第⼀个参数即为客户端的socket描述字，第⼆参数为服务器的socket地址，第三个参数为 socket地址的⻓度。客户端通过调⽤connect函数来建⽴与TCP服务器的连接。

## 3.4、accept()函数

TCP服务器端依次调⽤socket()、bind()、listen()之后，就会监听指定的socket地址了。TCP客户端依次调⽤ socket()、connect()之后就想TCP服务器发送了⼀个连接请求。TCP服务器监听到这个请求之后，就会调⽤ accept()函数取接收请求，这样连接就建⽴好了。之后就可以开始⽹络I/O操作了，即类同于普通⽂件的读 写I/O操作。 int accept(int sockfd, struct sockaddr *addr, socklen_t *addrlen); accept函数的第⼀个参数为服务器的socket描述字，第⼆个参数为指向struct sockaddr *的指针，⽤于返 回客户端的协议地址，第三个参数为协议地址的⻓度。如果accpet成功，那么其返回值是由内核⾃动⽣ 成的⼀个全新的描述字，代表与返回客户的TCP连接。

注意：accept的第⼀个参数为服务器的socket描述字，是服务器开始调⽤socket()函数⽣成的，称为监听socket 描述字；⽽accept函数返回的是已连接的socket描述字。⼀个服务器通常通常仅仅只创建⼀个监听socket描述字， 它在该服务器的⽣命周期内⼀直存在。内核为每个由服务器进程接受的客户连接创建了⼀个已连接 socket描述字，当服务器完成了对某个客户的服务，相应的已连接socket描述字就被关闭。

## 3.5、read()、write()等函数

万事具备只⽋东⻛，⾄此服务器与客户已经建⽴好连接了。可以调⽤⽹络I/O进⾏读写操作了，即实现 了⽹咯中不同进程之间的通信！⽹络I/O操作有下⾯⼏组：

read()/write() recv()/send() readv()/writev()

recvmsg()/sendmsg()

#### recvfrom()/sendto()

我推荐使⽤recvmsg()/sendmsg()函数，这两个函数是最通⽤的I/O函数，实际上可以把上⾯的其它函数都替换 成这两个函数。它们的声明如下：

#include <unistd.h>

ssize_t read(int fd, void *buf, size_t count); ssize_t write(int fd, const void *buf, size_t count);

#include <sys/types.h> #include <sys/socket.h>

ssize_t send(int sockfd, const void *buf, size_t len, int flags); ssize_t recv(int sockfd, void *buf, size_t len, int flags);

ssize_t sendto(int sockfd, const void *buf, size_t len, int flags,

const struct sockaddr *dest_addr, socklen_t addrlen); ssize_t recvfrom(int sockfd, void *buf, size_t len, int flags,

struct sockaddr *src_addr, socklen_t *addrlen);

ssize_t sendmsg(int sockfd, const struct msghdr *msg, int flags); ssize_t recvmsg(int sockfd, struct msghdr *msg, int flags);

read函数是负责从fd中读取内容.当读成功时，read返回实际所读的字节数，如果返回的值是0表示已经 读到⽂件的结束了，⼩于0表示出现了错误。如果错误为EINTR说明读是由中断引起的，如果是 ECONNREST表示⽹络连接出了问题。

write函数将buf中的nbytes字节内容写⼊⽂件描述符fd.成功时返回写的字节数。失败时返回-1，并设置 errno变量。 在⽹络程序中，当我们向套接字⽂件描述符写时有俩种可能。1)write的返回值⼤于0，表示 写了部分或者是全部的数据。2)返回的值⼩于0，此时出现了错误。我们要根据错误类型来处理。如果 错误为EINTR表示在写的时候出现了中断错误。如果为EPIPE表示⽹络连接出现了问题(对⽅已经关闭 了连接)。 其它的我就不⼀⼀介绍这⼏对I/O函数了，具体参⻅man⽂档或者baidu、Google，下⾯的例⼦中将使⽤ 到send/recv。

- 3.6、close()函数


在服务器与客户端建⽴连接之后，会进⾏⼀些读写操作，完成了读写操作就要关闭相应的socket描述 字，好⽐操作完打开的⽂件要调⽤fclose关闭打开的⽂件。 #include <unistd.h> int close(int fd); close⼀个TCP socket的缺省⾏为时把该socket标记为以关闭，然后⽴即返回到调⽤进程。该描述字不能 再由调⽤进程使⽤，也就是说不能再作为read或write的第⼀个参数。 注意：close操作只是使相应socket描述字的引⽤计数-1，只有当引⽤计数为0的时候，才会触发TCP客户 端向服务器发送终⽌连接请求。

# 4、socket中TCP的三次握⼿建⽴连接详解

我们知道tcp建⽴连接要进⾏“三次握⼿”，即交换三个分组。⼤致流程如下：

客户端向服务器发送⼀个SYN J 服务器向客户端响应⼀个SYN K，并对SYN J进⾏确认ACK J+1 客户端再想服务器发⼀个确认ACK K+1

只有就完了三次握⼿，但是这个三次握⼿发⽣在socket的那⼏个函数中呢？请看下图：

![image 1](<网络编程学习笔记一：Socket编程.note_images/imageFile1.png>)

image

图1、socket中发送的TCP三次握⼿

从图中可以看出，当客户端调⽤connect时，触发了连接请求，向服务器发送了SYN J包，这时connect进 ⼊阻塞状态；服务器监听到连接请求，即收到SYN J包，调⽤accept函数接收请求向客户端发送SYN K ， ACK J+1，这时accept进⼊阻塞状态；客户端收到服务器的SYN K ，ACK J+1之后，这时connect返回， 并对SYN K进⾏确认；服务器收到ACK K+1时，accept返回，⾄此三次握⼿完毕，连接建⽴。

总结：客户端的connect在三次握⼿的第⼆个次返回，⽽服务器端的accept在三次握⼿的第三次返回。

# 5、socket中TCP的四次握⼿释放连接详解

上⾯介绍了socket中TCP的三次握⼿建⽴过程，及其涉及的socket函数。现在我们介绍socket中的四次握 ⼿释放连接的过程，请看下图：

![image 2](<网络编程学习笔记一：Socket编程.note_images/imageFile2.png>)

image

图2、socket中发送的TCP四次握⼿ 图示过程如下：

某个应⽤进程⾸先调⽤close主动关闭连接，这时TCP发送⼀个FIN M； 另⼀端接收到FIN M之后，执⾏被动关闭，对这个FIN进⾏确认。它的接收也作为⽂件结束符传递给 应⽤进程，因为FIN的接收意味着应⽤进程在相应的连接上再也接收不到额外数据； ⼀段时间之后，接收到⽂件结束符的应⽤进程调⽤close关闭它的socket。这导致它的TCP也发送⼀个 FIN N； 接收到这个FIN的源发送端TCP对它进⾏确认。

这样每个⽅向上都有⼀个FIN和ACK。

# 6、⼀个例⼦（实践⼀下）

说了这么多了，动⼿实践⼀下。下⾯编写⼀个简单的服务器、客户端（使⽤TCP）——服务器端⼀直监 听本机的6666号端⼝，如果收到连接请求，将接收请求并接收客户端发来的消息；客户端与服务器端 建⽴连接并发送⼀条消息。 服务器端代码：

![image 3](<网络编程学习笔记一：Socket编程.note_images/imageFile3.png>)

服务器端

![image 4](<网络编程学习笔记一：Socket编程.note_images/imageFile4.png>)

复制代码

int listenfd, connfd; struct sockaddr_in servaddr; char buff[4096]; int n;

if( (listenfd = socket(AF_INET, SOCK_STREAM, 0)) == -1 ){ printf("create socket error: %s(errno: %d)\n",strerror(errno),errno); exit(0); }

memset(&servaddr, 0, sizeof(servaddr)); servaddr.sin_family = AF_INET; servaddr.sin_addr.s_addr = htonl(INADDR_ANY); servaddr.sin_port = htons(6666);

if( bind(listenfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) == -1){ printf("bind socket error: %s(errno: %d)\n",strerror(errno),errno); exit(0); }

if( listen(listenfd, 10) == -1){ printf("listen socket error: %s(errno: %d)\n",strerror(errno),errno); exit(0); }

printf("======waiting for client's request======\n"); while(1){ if( (connfd = accept(listenfd, (struct sockaddr*)NULL, NULL)) == -1){

printf("accept socket error: %s(errno: %d)",strerror(errno),errno);

continue;

} n = recv(connfd, buff, MAXLINE, 0); buff[n] = '\0'; printf("recv msg from client: %s\n", buff); close(connfd); }

close(listenfd); }

![image 5](<网络编程学习笔记一：Socket编程.note_images/imageFile5.png>)

复制代码

客户端代码：

![image 6](<网络编程学习笔记一：Socket编程.note_images/imageFile6.png>)

客户端

![image 7](<网络编程学习笔记一：Socket编程.note_images/imageFile7.png>)

复制代码

int sockfd, n; char recvline[4096], sendline[4096]; struct sockaddr_in servaddr;

if( argc != 2){ printf("usage: ./client <ipaddress>\n"); exit(0); }

if( (sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0){ printf("create socket error: %s(errno: %d)\n", strerror(errno),errno); exit(0); }

memset(&servaddr, 0, sizeof(servaddr)); servaddr.sin_family = AF_INET; servaddr.sin_port = htons(6666); if( inet_pton(AF_INET, argv[1], &servaddr.sin_addr) <= 0){ printf("inet_pton error for %s\n",argv[1]); exit(0); }

if( connect(sockfd, (struct sockaddr*)&servaddr, sizeof(servaddr)) < 0){ printf("connect error: %s(errno: %d)\n",strerror(errno),errno); exit(0); }

printf("send msg to server: \n"); fgets(sendline, 4096, stdin);

if( send(sockfd, sendline, strlen(sendline), 0) < 0) { printf("send msg error: %s(errno: %d)\n", strerror(errno), errno); exit(0); }

close(sockfd); exit(0);

}

![image 8](<网络编程学习笔记一：Socket编程.note_images/imageFile8.png>)

复制代码

当然上⾯的代码很简单，也有很多缺点，这就只是简单的演示socket的基本函数使⽤。其实不管有多复 杂的⽹络程序，都使⽤的这些基本函数。上⾯的服务器使⽤的是迭代模式的，即只有处理完⼀个客户 端请求才会去处理下⼀个客户端的请求，这样的服务器处理能⼒是很弱的，现实中的服务器都需要有 并发处理能⼒！为了需要并发处理，服务器需要fork()⼀个新的进程或者线程去处理请求等。

# 7、动动⼿

留下⼀个问题，欢迎⼤家回帖回答！！！是否熟悉Linux下⽹络编程？如熟悉，编写如下程序完成如下 功能： 服务器端： 接收地址192.168.100.2的客户端信息，如信息为“Client Query”，则打印“Receive Query” 客户端： 向地址192.168.100.168的服务器端顺序发送信息“Client Query test”，“Cleint Query”，“Client Query Quit”，然后退出。 题⽬中出现的ip地址可以根据实际情况定。

