linux 上tomcat 服务器抛出socket异常“⽂件打开太多”的问题 java.net.SocketException: To many open files at java.net.PlainSocketImpl.socketAcept(Native Method) at java.net.PlainSocketImpl.acept(PlainSocketImpl.java:384) at java.net.ServerSocket.implAcept(ServerSocket.java:450) at java.net.ServerSocket.acept(ServerSocket.java:421) at org.apache.tomcat.util.net.DefaultServerSocketFactory.aceptSocket(DefaultServerSocketFactory. java:60) at org.apache.tomcat.util.net.PolTcpEndpoint.aceptSocket(PolTcpEndpoint.java:407) at org.apache.tomcat.util.net.LeaderFolowerWorkerThread.runIt(LeaderFolowerWorkerThread.java:7 0) at org.apache.tomcat.util.threads.ThreadPol$ControlRunable.run(ThreadPol.java:684) at java.lang.Thread.run(Thread.java:595) 原本以为是tomcat的配置或是应⽤本身的问题，"⾕歌"⼀把后才发现，该问题的根本原因是由于系统 ⽂件资源的限制导致的。 具体可以参考 的说明。具体的解决⽅式可以参考⼀下： 1。ulimit -a 查看系统⽬前资源限制的设定。

htp:/ w.bea.com.cn/suport_patern/To_Many_Open_Files_Patern.html

[rot@test security]# umlimit -a -bash: umlimit: comand not found [rot@test security]# ulimit -a core file size (blocks, -c) 0 data seg size (kbytes, -d) unlimited file size (blocks, -f) unlimited max locked memory (kbytes, -l) unlimited max memory size (kbytes, -m) unlimited open files (-n) 1024 pipe size (512 bytes, -p) 8 stack size (kbytes, -s) 8192 cpu time (seconds, -t) unlimited max user proceses (-u) 7168 virtual memory (kbytes, -v) unlimited [rot@test security]# 通过以上命令，我们可以看到open files 的最⼤数为1024 那么我们可以通过⼀下命令修改该参数的最⼤值

2. ulimit -n 4096 [rot@test security]# ulimit -n 4096 [rot@test security]# ulimit -a core file size (blocks, -c) 0 data seg size (kbytes, -d) unlimited file size (blocks, -f) unlimited max locked memory (kbytes, -l) unlimited max memory size (kbytes, -m) unlimited open files (-n) 4096 pipe size (512 bytes, -p) 8 stack size (kbytes, -s) 8192 cpu time (seconds, -t) unlimited max user proceses (-u) 7168 virtual memory (kbytes, -v) unlimited 这样我们就修改了系统在同⼀时间打开⽂件资源的最⼤数，基本解决以上问题。 以上部分是查找⽹络上的解决⽅法。设置了之后段时间内有作⽤。 后来仔细想来，问题还是要从根本上解决，于是把以前的代码由认真地看了⼀遍。终于找到了，罪魁 祸⾸。 在读取⽂件时，有⼀些使⽤的BuferedReader 没有关闭。导致⽂件⼀直处于打开状态。造成资源的严 重浪费。 修改之后的简单代码如下： public void test(){

BuferedReader reader =nul; try{

reader = 读取⽂件; String line ="; while( ( ine=reader.readLine()!=nul){

其他操作 }

} catch (IOException e){

System.out.println(e); } finaly{

if(reader !=nul){ try { reader.close(); } catch (IOException e) { e.printStackTrace();

} }

} }

