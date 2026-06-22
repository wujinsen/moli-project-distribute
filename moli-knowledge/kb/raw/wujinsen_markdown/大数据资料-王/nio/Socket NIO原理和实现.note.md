传统的I/O 使⽤传统的I/O程序读取⽂件内容, 并写⼊到另⼀个⽂件(或Socket), 如下程序:File.read(fileDesc, buf, len); Socket.send(socket, buf, len);会有较⼤的性能开销, 主要表现在⼀下两⽅⾯:1. 上下⽂切

换(context switch), 此处有4次⽤户态和内核态的切换2. Bufer内存开销, ⼀个是应⽤程序bufer, 另⼀个是系统读 取bufer以及socket bufer其运⾏示意图如下 1) 先将⽂件内容从磁盘中拷⻉到操作系统bufer2) 再从操作系统 bufer拷⻉到程序应⽤bufer3) 从程序bufer拷⻉到socket bufer4) 从socket bufer拷⻉到协议引擎.

## NIO

zerocopy技术省去了将操作系统的read bufer拷⻉到程序的bufer, 以及从程序bufer拷⻉到socket bufer的步 骤, 直接将 read bufer 拷⻉到 socket bufer. java 的 FileChanel.transferTo() ⽅法就是这样的实现, 这个实现 是依赖于操作系统底层的sendFile()实现的.publicvoid transferTo(long position, long count, WritableByteChannel target);他的底层调⽤的是系统调⽤sendFile()

# ⽅法#include <sys/socket.h> ssize_t sendfile(int out_fd, int in_fd, off_t *offset, size_t count);如下图 这样, 省去了两次bufer的copy, Linux 2.4 及以后的内核, ⼜做了改进, 不再使⽤

socket bufer, ⽽是直接将read bufer数据拷⻉到协议引擎, ⽽socket bufer只会记录数据位置的描述符和数据⻓ 度,如下

