- 1、进程、线程 进程： windows每次启动⼀个应⽤就会启动⼀个进程，jvm就是个进程，tomcat启动后会启动jvm 虚拟机，这是⼀个进程，此进程中的变量共享 线程： 进程中最⼩的执⾏单元，c可以操作进程，创建进程等，jvm⽆法操作进程，只能操作线程， 每个进程中可以跑多个线程，多线程执⾏任务，效率⾼，每个线程做⾃⼰的事情，当线程同 时处理共享资源时，会涉及到线程安全问题，就涉及到锁的概念。
- 2、错误⼀：启动thrift接⼝后，⽤⼀个test⽅法去测试，获取thrift中常量⽅法中的静态变量。 误以为是同⼀静态变量，test改变常量的值，thrift就能取到，殊不知，test和thrift是两个 进程，test和thrift获取的同⼀静态变量是存到两个进程的两个静态块中，不能共⽤。 那为什么⽤client调⽤thrift后，改变静态变量后，thrift就会⽣效呢？ 是因为client端调⽤的是thrift提供的接⼝，是⽤tcp协议提供的接⼝，实现了线程间的通信， ⽤test测试的时候，直接调⽤的是thrift应⽤源码的⽅法，实际是创建了另⼀个进程。
- 3、 OpenSL is not properly instaled on your system.

./configure时⽼是报！ checking opensl/sl.h usability. no checking opensl/sl.h presence. no checking for opensl/sl.h. no configure: eror:

! OpenSL is not properly instaled on your system. ! ! Can not include OpenSL headers files.

解决： yum instal -y opensl opensl-devel

- 4、java.net.SocketException: To many open files 问题的解决办法


linux 上tomcat 服务器抛出socket异常“⽂件打开太多”的问题 java.net.SocketException: To many open files

at java.net.PlainSocketImpl.socketAcept(Native Method) at java.net.PlainSocketImpl.acept(PlainSocketImpl.java:384) at java.net.ServerSocket.implAcept(ServerSocket.java:450) at java.net.ServerSocket.acept(ServerSocket.java:421) at org.apache.tomcat.util.net.DefaultServerSocketFactory.aceptSocket(DefaultServerSocketFacto ry.java:60) at org.apache.tomcat.util.net.PolTcpEndpoint.aceptSocket(PolTcpEndpoint.java:407) at org.apache.tomcat.util.net.LeaderFolowerWorkerThread.runIt(LeaderFolowerWorkerThread.java :70) at org.apache.tomcat.util.threads.ThreadPol$ControlRunable.run(ThreadPol.java:684) at java.lang.Thread.run(Thread.java:595) 原本以为是tomcat的配置或是应⽤本身的问题，"⾕歌"⼀把后才发现，该问题的根本原因是由于系统 ⽂件资源的限制导致的。

htp:/ w.bea.com.cn/suport_patern/To_Many_Open_Files_Patern.html

具体可以参考 的说明。具体的解决⽅式可以参考⼀下： 1。ulimit -a 查看系统⽬前资源限制的设定。

[ security]# umlimit -a -bash: umlimit: comand not found [ security]# ulimit -a core file size (blocks, -c) 0 data seg size (kbytes, -d) unlimited file size (blocks, -f) unlimited max locked memory (kbytes, -l) unlimited max memory size (kbytes, -m) unlimited open files (-n) 1024 pipe size (512 bytes, -p) 8 stack size (kbytes, -s) 8192 cpu time (seconds, -t) unlimited max user proceses (-u) 7168 virtual memory (kbytes, -v) unlimited [ security]# 通过以上命令，我们可以看到open files 的最⼤数为1024 那么我们可以通过⼀下命令修改该参数的最⼤值

rot@test

rot@test

rot@test

2. ulimit -n 4096 [ security]# ulimit -n 4096 [ security]# ulimit -a

rot@test rot@test

core file size (blocks, -c) 0 data seg size (kbytes, -d) unlimited file size (blocks, -f) unlimited max locked memory (kbytes, -l) unlimited max memory size (kbytes, -m) unlimited open files (-n) 4096 pipe size (512 bytes, -p) 8 stack size (kbytes, -s) 8192 cpu time (seconds, -t) unlimited max user proceses (-u) 7168 virtual memory (kbytes, -v) unlimited

这样我们就修改了系统在同⼀时间打开⽂件资源的最⼤数，基本解决以上问题。

以上部分是查找⽹络上的解决⽅法。设置了之后段时间内有作⽤。

后来仔细想来，问题还是要从根本上解决，于是把以前的代码由认真地看了⼀遍。终于找到了，罪魁 祸⾸。

在读取⽂件时，有⼀些使⽤的BuferedReader 没有关闭。导致⽂件⼀直处于打开状态。造成资源的严 重浪费。

修改之后的简单代码如下：

public void test(){ BuferedReader reader =nul; try{

reader = 读取⽂件; String line ="; while( ( ine=reader.readLine()!=nul){

其他操作 }

} catch (IOException e){

System.out.println(e); } finaly{

if(reader !=nul){ try { reader.close(); } catch (IOException e) {

e.printStackTrace(); }

} }

}

