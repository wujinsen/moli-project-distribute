在上⼀篇中，使⽤了 java Socket+Tcp/IP 协议来实现应⽤程序或客户端--服务器间的实时双向通信，本篇中，将使⽤ UDP 协议来实现 Socket 的通信。

- 1. 关于UDP UDP协议（⽤户数据报协议）是⽆连接的、不可靠的、⽆序的,速度快，进⾏数据传输时，⾸先将要传输的数据定义成数

据报（Datagram），⼤⼩限制在64k，在数据报中指明数据索要达到的Socket（主机地址和端⼝号），然后再将数据报发送 出去，Java 对UDP 协议通信提供了两个主要的类，DatagramPacket类:表示数据报包，DatagramSocket类：进⾏端到端 通信的类。

- 2. 实现⽅法


- 2.1 服务器端
- 2.2 客户端


创建DatagramSocket，指定端⼝号 创建DatagramPacket 接受客户端发送的数据信息 读取数据

- 1.
- 2.
- 3.
- 4.


<table>
  <tr>
    <th>/服务器端，实现基于UDP的⽤户登录<br><br>/1、创建服务器端DatagramSocket，指定端⼝<br><br>DatagramSocket socket =new datagramSocket(1010);<br><br>/2、创建数据报，⽤于接受客户端发送的数据<br><br>byte[] data =newbyte[1024];/ DatagramPacket packet =newDatagramPacket(data,data.length);<br><br>/3、接受客户端发送的数据<br><br>socket.receive(packet);/此⽅法在接受数据报之前会⼀致阻塞<br><br>/4、读取数据<br><br><br>String info =newString(data,o,data.length); System.out.println("Helo,我是服务器，客户端告诉我"+info);<br><br>/ = /向客户端响应数据<br><br>/1、定义客户端的地址、端⼝号、数据<br><br>InetAdres adres = packet.getAdres(); int port = packet.getPort(); byte[] data2 = "欢迎您！".geyBytes();<br><br>/2、创建数据报，包含响应的数据信息<br><br>DatagramPacket packet2 = new DatagramPacket(data2,data2.length,adres,port);<br><br>/3、响应客户端<br><br>socket.send(packet2);<br><br>/4、关闭资源<br></th>
  </tr>
</table>


socket.close();

- 1.
- 2.
- 3.
- 4.


定义发送信息 创建DatagramPacket，包含将要发送的信息 创建DatagramSocket 发送数据

<table>
  <tr>
    <th>/客户端<br><br>/1、定义服务器的地址、端⼝号、数据<br><br>InetAdres adres =InetAdres.getByName("localhost"); int port =1010; byte[] data ="⽤户名：admin;密码：123".getBytes();<br><br>/2、创建数据报，包含发送的数据信息<br><br>DatagramPacket packet = newDatagramPacket(data,data,length,adres,port);<br><br>/3、创建DatagramSocket对象<br><br>DatagramSocket socket =newDatagramSocket();<br><br>/4、向服务器发送数据<br><br><br>socket.send(packet);<br><br>/接受服务器端响应数据 / =<br><br>/1、创建数据报，⽤于接受服务器端响应数据<br><br>byte[] data2 = new byte[1024]; DatagramPacket packet2 = new DatagramPacket(data2,data2.length);<br><br>/2、接受服务器响应的数据<br><br><br>socket.receive(packet2); String raply = new String(data2,0,packet2.getLenth(); System.out.println("我是客户端，服务器说："+reply);<br><br>/4、关闭资源</th>
  </tr>
</table>


socket.close();

3. 注意问题

- 1.
- 2.
- 3.
- 4.


多线程的优先级问题：根据实际的经验，适当的降低优先级，否侧可能会有程序运⾏效率低的情况 是否关闭输出流和输⼊流：对于同⼀个socket，如果关闭了输出流，则与该输出流关联的socket也会被关闭，所以⼀般 不⽤关闭流，直接关闭socket即可 使⽤TCP通信传输对象，IO中序列化部分 socket编程传递⽂件，IO流部分

