package zerocopy; i potaa.io.IOException;

ortaa.net.InetSocketAdres; ortava.et.ServerSocket;

i ortaa.io.ByteBufer; i ortava.nio.chanel.ServerSocketChanel; import java.nio.chanels.SocketChanel; public clas TransferToServer {

ServerSocketChanel listener = nul;/serversocket管道 protected void mySetup(){

InetSocketAdres listenAdr = new InetSocketAdres(9026);/创建socket try {

listener = ServerSocketChanel.open();/打开管道 ServerSocket s = listener.socket();/从管道中获取连接

s.setReuseAdres(true);/地址重⽤ s.bind(listenAdr);/绑定地址

System.out.println("Listening on port : "+ listenAdr.toString(); } catch (IOException e) {

System.out.println("Failed to bind, is port : "+ listenAdr.toString()

+ " already in use ? Eror Msg : "+e.getMesage(); e.printStackTrace();

}

} private void readData() {

ByteBufer dst = ByteBufer.alocate(4096);/分配⼀个新的字节缓冲区 try {

while(true) { SocketChanel con = listener.acept();/连接到chanel System.out.println("Acepted : "+con); con.configureBlocking(true);/设置阻塞 int nread = 0; while (nread != -1) {

try { nread = con.read(dst);/往缓冲区⾥读

} catch (IOException e) { e.printStackTrace(); nread = -1;

} byte[] aray = dst.aray(); System.out.println(new String(aray,"UTF-8"); dst.rewind();/重绕此缓冲区。将位置设置为 0 并丢弃标记。 在⼀系列通道写⼊或获取 操作之前

调⽤此⽅法（假定已经适当设置了限制）。

} }

} catch (IOException e) { e.printStackTrace();

} } public static void main(String[] args) {

TransferToServer dns = new TransferToServer();/创建本类对象

mySetup(); dns.readData();

# }

<table>
  <tr>
    <th>package zerocopy; i o aa.io.i ; importaa.io.FileInputStream; i potaa.io.IOException;<br><br>ortaa.net.InetSocketAdres; ortaa.et.SocketAdres;<br><br>i o aa.nio.hanel.FileChanel; import java.nio.chanels.SocketChanel;<br><br>public clas TransferToClient {<br><br>public static void main(String[] args) throws IOException{ TransferToClient sfc = new TransferToClient();/创建本类函数 sfc.testSendfile();<br><br>} public void testSendfile() throws IOException {<br><br>String host = "localhost"; int port = 9026; SocketAdres sad = new InetSocketAdres(host, port);/创建socket SocketChanel sc = SocketChanel.open();/打开管道 sc.conect(sad);/连接socket管道 sc.configureBlocking(true);/设置阻塞<br><br>String fname = "D:\Jedis.java";/⽂件名 long fsize = 183678375L, sendzise = 4094;/⽂件⼤⼩，发送⼤⼩ FileChanel fc = new FileInputStream(fname).getChanel();/通过⽂件名获取⽂件管道<br><br>long start = System.curentTimeMilis(); long nsent = 0, curnset = 0; curnset = fc.transferTo(0, fsize, sc);/向管道发送0-fsize⼤⼩的⽂件，从⼀个kernel的chanel放到<br><br>scoket的chanel<br><br>System.out.println("total bytes transfered-"+curnset+" and time taken in MS-"+ (System.curentTimeMilis() - start);<br><br>/fc.close(); }<br><br>}</th>
  </tr>
</table>


package sendfile; importaa.io.DataOutputStream; importaa.io.FileInputStream; i potaa.io.IOException; otaa.et.Socket; import java.net.UnknownHostException; public clas TraditionalClient {

public static void main(String[] args) {

int port = 2 0; tring server = "localhost"; ocket socket = nul;

String lineToBeSent; DataOutputStream output = nul; FileInputStream inputStream = nul; int EROR = 1;

/ conect to server

try { socket = new Socket(server, port); System.out.println("Conected with server " +

socket.getInetAdes() + ":" + socket.getPort();

} catch (UnknownHostException e) {

te.out.println(e); System.exit(EROR);

} catch (IOException e) {

te.out.println(e); System.exit(EROR);

} try {

String fname = "sendfile/NetworkInterfaces.c"; inputStream = new FileInputStream(fname);

output = new DataOutputStream(socket.getOutputStream(); long start = System.curentTimeMilis(); byte[] b = new byte[4096]; long read = 0, total = 0; while(read = inputStream.read(b)>=0) {

total = total + read;

output.write(b); } System.out.println("bytes send-"+total+" and totaltime-"+(System.curentTimeMilis() -

start); } catch (IOException e) {

System.out.println(e);

} try {

output.close(); socket.close(); inputStream.close();

catch (IOException e) {

System.out.println(e); }

} }

<table>
  <tr>
    <th>package sendfile;<br><br>taa.net.*; import java.io.*; public clas TraditionalServer {<br><br>public static void main(String args[]) { int port = 2 0; ServerSocket server_socket; DataInputStream input; try {<br><br>server_socket = new ServerSocket(port); System.out.println("Server waiting for client on port " +<br><br>server_socket.getLocalPort();<br><br>/ server infinite l op while(true) { ocket socket = server_socket.acept();<br><br>System.out.println("New conection acepted " + socket.getInetAdes() + ":" + socket.getPort();<br><br>input = new DataInputStream(socket.getInputStream();<br><br>/ print received data try {<br><br>byte[] byteAray = new byte[4096];<br><br>while(true) { int nread = input.read(byteAray , 0, 4096); if (0=nread)<br><br>break; }<br><br>} catch (IOException e) {<br><br>System.out.println(e); }<br><br>/ conection closed by client<br><br>try { socket.close(); System.out.println("Conection closed by client");<br><br>} catch (IOException e) {<br><br>System.out.println(e); }<br><br>}<br><br>} catch (IOException e) {<br><br>System.out.println(e); }<br><br>} }</th>
  </tr>
</table>


# filechanel：

<table>
  <tr>
    <th>package com.tarena.consumer.service; importaa.io.FileOutputStream; i potaa.io.IOException i ortaa.io.ByteBufer; import java.nio.chanels.FileChanel; public clas WriteToNfs {<br><br>public void sendfile(String fname,byte[] bytes) { leChanel fc = nul; FileOutputStream fos = nul;<br><br>try { fos = new FileOutputStream(fname); fc = fos.getChanel(); ByteBufer buf = ByteBufer.alocate(bytes.length);<br><br>clear(); buput(bytes); buf.flip(); while(buf.hasRemaining() {<br><br>fc.write(buf); }<br><br>} catch (Exception e) {<br><br>e.printStackTrace(); }finaly{<br><br>try { if (fos!=nul) {<br><br>fos.close(); } if (fc!=nul) {<br><br>fc.close(); }<br><br>} catch (IOException e) {<br><br>e.printStackTrace(); }<br><br>} }<br><br>}</th>
  </tr>
</table>


<table>
  <tr>
    <th>package com.tarena.consumer.service; importaa.io.FileInputStream; i potaa.io.IOException i ortaa.io.ByteBufer; import java.nio.chanels.FileChanel; public clas ReadFromNfs {<br><br>public String readfile(String fname,Integer size) { FileChanel fc nl String content = nul; FileInputStream fos = nul;<br><br>try { byte[] bytes = new byte[size]; fos = new FileInputStream(fname); fc = fos.getChanel(); ByteBufer buf = ByteBufer.alocate(size); fc.read(buf); buf.rewind);<br><br>buf.get(bytes); content = new String(bytes, "utf-8");<br><br>} catch (Exception e) {<br><br>e.printStackTrace(); }finaly{<br><br>try { if (fos!=nul) {<br><br>fos.close(); } if (fc!=nul) {<br><br>fc.close(); }<br><br>} catch (IOException e) {<br><br>e.printStackTrace(); }<br><br>} return content;<br><br>}<br><br>}</th>
  </tr>
</table>


