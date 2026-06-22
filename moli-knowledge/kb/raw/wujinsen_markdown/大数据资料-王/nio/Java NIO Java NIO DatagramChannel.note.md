Java NIO中的DatagramChanel是⼀个能收发UDP包的通道。因为UDP是⽆连接的⽹络协议，所以不 能像其它通道那样读取和写⼊。它发送和接收的是数据包。

# 打开 DatagramChanel

下⾯是 DatagramChanel 的打开⽅式：

- 1 DatagramChannel channel = DatagramChannel.open();

- 2 channel.socket().bind(new InetSocketAddress(9999));

- 3


这个例⼦打开的 DatagramChanel可以在UDP端⼝ 9上接收数据包。

# 接收数据

通过receive()⽅法从DatagramChanel接收数据，如：

- 1 ByteBuffer buf = ByteBuffer.allocate(48);

- 2 buf.clear();

- 3 channel.receive(buf);

- 4


receive()⽅法会将接收到的数据包内容复制到指定的Bufer. 如果Bufer容不下收到的数据，多出的数 据将被丢弃。

# 发送数据

通过send()⽅法从DatagramChanel发送数据，如:

- 1 String newData = "New String to write to file..." + System.currentTimeMillis();

- 2

- 3 ByteBuffer buf = ByteBuffer.allocate(48);

- 4 buf.clear();

- 5 buf.put(newData.getBytes());

- 6 buf.flip();

- 7

- 8 int bytesSent = channel.send(buf, new InetSocketAddress("jenkov.com", 80));

- 9


这个例⼦发送⼀串字符到”jenkov.com”服务器的UDP端⼝80。 因为服务端并没有监控这个端⼝，所以 什么也不会发⽣。也不会通知你发出的数据包是否已收到，因为UDP在数据传送⽅⾯没有任何保证。

# 连接到特定的地址

可以将DatagramChanel“连接”到⽹络中的特定地址的。由于UDP是⽆连接的，连接到特定地址并不 会像TCP通道那样创建⼀个真正的连接。⽽是锁住DatagramChanel ，让其只能从特定地址收发数 据。 这⾥有个例⼦:

- 1 channel.connect(new InetSocketAddress("jenkov.com", 80));

- 2


当连接后，也可以使⽤read()和write()⽅法，就像在⽤传统的通道⼀样。只是在数据传送⽅⾯没有任何 保证。这⾥有⼏个例⼦：

- 1 int bytesRead = channel.read(buf);

- 2 int bytesWritten = channel.write(but);

- 3


