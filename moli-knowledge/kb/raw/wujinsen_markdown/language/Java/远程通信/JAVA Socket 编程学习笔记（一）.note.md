- 1. Socket 通信简介及模型 Java Socket 可实现客户端--服务器间的双向实时通信。java.net包中定义的两个类socket和ServerSocket，分别

⽤来实现双向连接的client和server端。

- 2. Socket 通信实现⽅法 2.1 服务器端（⾮多线程）


![image 1](<JAVA Socket 编程学习笔记（一）.note_images/imageFile1.png>)

⽤指定的端⼝实例化⼀个SeverSocket对象。服务器就可以⽤这个端⼝监听从客户端发来的连接请求。 调⽤ServerSocket的accept()⽅法，以在等待连接期间造成阻塞，监听连接从端⼝上发来的连接请求。 利⽤accept⽅法返回的客户端的Socket对象，进⾏读写IO的操作 关闭打开的流和Socket对象

- 1.
- 2.
- 3.
- 4.


<table>
  <tr>
    <th>/*<br><br>* 基于TCP协议的Socket通信，实现⽤户登录，服务端<br>*/<br><br><br>/1、创建⼀个服务器端Socket，即ServerSocket，指定绑定的端⼝，并监听此端⼝<br><br>ServerSocket serverSocket =newServerSocket(1086);/1024-6535的某个端⼝<br><br>/2、调⽤acept()⽅法开始监听，等待客户端的连接<br><br>Socket socket = serverSocket.acept();<br><br>/3、获取输⼊流，并读取客户端信息<br><br>InputStream is = socket.getInputStream(); InputStreamReader isr =newInputStreamReader(is); BuferedReader br =newBuferedReader(isr); String info =nul; while(info=br.readLine()!=nul){ System.out.println("Helo,我是服务器，客户端说："+info)； } socket.shutdownInput();/关闭输⼊流<br><br>/4、获取输出流，响应客户端的请求<br><br>OutputStream os = socket.getOutputStream(); PrintWriter pw = new PrintWriter(os); pw.write("Helo World！"); pw.flush();<br><br>/5、关闭资源<br><br><br>pw.close(); os.close(); br.close(); isr.close(); is.close(); socket.close();</th>
  </tr>
</table>


serverSocket.close();

2.2 客户端

⽤服务器的IP地址和端⼝号实例化Socket对象。 调⽤connect⽅法，连接到服务器上。 获得Socket上的流，把流封装进BufferedReader/PrintWriter的实例，以进⾏读写 利⽤Socket提供的getInputStream和getOutputStream⽅法，通过IO流对象，向服务器发送数据流 关闭打开的流和Socket。

- 1.
- 2.
- 3.
- 4.
- 5.


<table>
  <tr>
    <th>/客户端<br><br>/1、创建客户端Socket，指定服务器地址和端⼝<br><br>Socket socket =newSocket("127.0.0.1",1086);<br><br>/2、获取输出流，向服务器端发送信息<br><br>OutputStream os = socket.getOutputStream();/字节输出流 PrintWriter pw =newPrintWriter(os);/将输出流包装成打印流 pw.write("⽤户名：admin；密码：admin"); pw.flush(); socket.shutdownOutput();<br><br>/3、获取输⼊流，并读取服务器端的响应信息<br><br><br>InputStream is = socket.getInputStream(); BuferedReader br = new BuferedReader(new InputStreamReader(is); String info = nul; while(info=br.readLine()!nul){<br><br>System.out.println("Helo,我是客户端，服务器说："+info); }<br><br>/4、关闭资源 br.close(); is.close(); pw.close(); os.close();</th>
  </tr>
</table>


socket.close();

2.2 服务器端 （多线程）

服务器端创建ServerSocket，循环调⽤accept()等待客户端连接 客户端创建⼀个socket并请求和服务器端连接 服务器端接受客户端请求，创建socket与该客户建⽴专线连接 建⽴连接的两个socket在⼀个单独的线程上对话 服务器端继续等待新的连接

- 1.
- 2.
- 3.
- 4.
- 5.


<table>
  <tr>
    <th>/服务器线程处理 和本线程相关的socket Socket socket =nul; public serverThread(Socket socket){ this.socket = socket; } ServerSocket serverSocket =newServerSocket(1086); Socket socket =nul; int count =0;/记录客户端的数量 while(true){ socket = serverScoket.acept(); ServerThread serverThread =newServerThread(socket); serverThread.start(); count+; System.out.println("客户端连接的数量："+count);</th>
  </tr>
</table>


}

