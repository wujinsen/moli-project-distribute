# ⼀、现象描述

在利⽤librdkafka同kafka broker通信过程中，当kafka broker意外退出时（如kill -9），librdkafka接⼝ 的sendmsg接⼝报出了“Program received signal SIGPIPE, Broken pipe.” 这个错误具有典型性，根据 ⽹络搜索的结果，这个⼀般是由于向⼀个被破坏的socket连接或者pipe读写数据造成的，向有经验的同 事请教，他们说这种场景不会出现SIGPIPE信号，⽽是直接send， write， sendmsg等返回-1，同时 errno会被设置成EPIPE。

实践是检验真理的唯⼀标准，找个例⼦⼀试便知。

# htp://hi. baidu.com/dlpucat/item/97ab75c5243b8761f6c95d75

⼆、例⼦程序为了快速检验，从⽹上上借了⼀个简单的客户端、服务器程序，

，多 谢原作者。

服务器端程序 server.c

点击(此处)折叠或打开

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


#include <netinet/in.h> #include <sys/types.h> #include <sys/socket.h> #include <stdio.h> #include <stdlib.h> #include <string.h>

#define HELO_WORLD_SERVER_PORT 6 #define LENGTH_OF_LISTEN_QUEUE 20 #define BUFER_SIZE 1024

int main(int argc, char *argv) {

struct sockadr_in server_adr; bzero(&server_adr,sizeof(server_adr); server_adr.sin_family = AF_INET; server_adr.sin_adr.s_adr = htons(INADR_ANY); server_adr.sin_port = htons(HELO_WORLD_SERVER_PORT);

- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.


int server_socket = socket(AF_INET,SOCK_STREAM,0); if( server_socket < 0) {

printf("Create Socket Failed!"); exit(1);

}

if( bind(server_socket,(struct sockadr*)&server_adr,sizeof(server_adr ) {

printf("Server Bind Port : %d Failed!", HELO_WORLD_SERVER_PORT); exit(1);

}

if( listen(server_socket, LENGTH_OF_LISTEN_QUEUE)) {

printf("Server Listen Failed!"); exit(1);

}

while(1) {

struct sockadr_in client_adr; socklen_t length = sizeof(client_adr);

int new_server_socket = acept(server_socket,(struct sockadr*)&client_adr,&length); if( new_server_socket < 0) {

printf("Server Acept Failed!\n"); break;

}

char bufer[BUFER_SIZE]; bzero(bufer, BUFER_SIZE); strcpy(bufer,"Helo,World from server!"); strcat(bufer,"\n"); send(new_server_socket,bufer,BUFER_SIZE,0);

- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.


bzero(bufer,BUFER_SIZE);

while(1){ length = recv(new_server_socket,bufer,BUFER_SIZE,0); if(length < 0) {

printf("Server Recieve Data Failed!\n"); exit(1);

} printf("\n%s",bufer);

} close(new_server_socket);

} close(server_socket); return 0;

}

客户端程序

点击(此处)折叠或打开

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


#include <netinet/in.h> #include <sys/types.h> #include <sys/socket.h> #include <stdio.h> #include <stdlib.h> #include <string.h> #include <signal.h> #include <erno.h>

#define HELO_WORLD_SERVER_PORT 6 #define BUFER_SIZE 1024

int main(int argc, char *argv) {

if(argc != 2) {

printf("Usage: ./%s ServerIPAdres\n",argv[0]);

- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.


exit(1); }

struct sockadr_in client_adr; bzero(&client_adr,sizeof(client_adr); client_adr.sin_family = AF_INET; client_adr.sin_adr.s_adr = htons(INADR_ANY); client_adr.sin_port = htons(0);

int client_socket = socket(AF_INET,SOCK_STREAM,0);

if( client_socket < 0) {

printf("Create Socket Failed!\n"); exit(1);

}

if( bind(client_socket,(struct sockadr*)&client_adr,sizeof(client_adr ) {

printf("Client Bind Port Failed!\n"); exit(1);

}

struct sockadr_in server_adr; bzero(&server_adr,sizeof(server_adr); server_adr.sin_family = AF_INET; if(inet_aton(argv[1],&server_adr.sin_adr) = 0) {

printf("Server IP Adres Eror!\n"); exit(1);

} server_adr.sin_port = htons(HELO_WORLD_SERVER_PORT); socklen_t server_adr_length = sizeof(server_adr); if(conect(client_socket,(struct sockadr*)&server_adr, server_adr_length)< 0) {

printf("Can Not Conect To %s!\n",argv[1]); exit(1);

- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.
- 78.
- 79.
- 80.


}

char bufer[BUFER_SIZE]; bzero(bufer,BUFER_SIZE); int length = recv(client_socket,bufer,BUFER_SIZE,0); if(length < 0) {

printf("Recieve Data From Server %s Failed!\n", argv[1]); exit(1);

} printf("From Server %s :\t%s",argv[1],bufer);

bzero(bufer,BUFER_SIZE); strcpy(bufer,"Helo, World! From Client\n");

while(1){ sl ep(1); int ret = send(client_socket,bufer,BUFER_SIZE,0);

if(ret =-1 & erno = EPIPE){ printf("receive sigpipe\n");

} }

close(client_socket); return 0;

}

三、重现⽅法step1）编译： gc-oserverserver.c

gc -o -g client client.c （通过gdb直接看到异常退出）

step 2）启动服务器端：./server

- step 3) 启动客户端：（这⾥假设客户端和服务器部署在同⼀台服务器） gdb ./client (gdb) r 127.0.0.1


- step 4) 观察正常运⾏结果：⾸先是客户端收到服务器端的消息：From Server 127.0.0.1 : Helo,World from server!


然后是服务器端每隔1s收到客户端的消息： Helo, World! From Client

- step 5）通过ctrl+c关闭服务器端
- step 6）观察客户端结果 Program received signal SIGPIPE, Broken pipe. 0x 03a7fcd5f5 in send () from /lib64/libc.so.6


重现了！！

# 四、解决办法解决办法很多，也很简单。

- 4.1 client中忽略SIGPIPE信号

点击(此处)折叠或打开

- 4.2 阻⽌SIGPIPE信号（后来追查，原来同事的程序框架中已经有了这种机制，所以没有经历过程序退 出的问题）

点击(此处)折叠或打开

- 4.3 为SIGPIPE添加信号处理函数，处理完程序继续执⾏


1.

signal(SIGPIPE, SIG_IGN);

- 1.
- 2.
- 3.
- 4.


sigset_t set; sigemptyset(&set); sigadset(&set, SIGPIPE); sigprocmask(SIG_BLOCK,&set,NUL);

点击(此处)折叠或打开

1.

signal(SIGPIPE, pipesig_handler);

多种选择，总有⼀款适合您。

