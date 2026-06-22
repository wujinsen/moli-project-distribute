Java NIO中的 ServerSocketChanel 是⼀个可以监听新进来的TCP连接的通道, 就像标准IO中的 ServerSocket⼀样。ServerSocketChanel类在 java.nio.chanels包中。 这⾥有个例⼦：

ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();serverSocketChannel.socket().bind(new InetSocketAddress(9999));while(true){ SocketChannel socketChannel = serverSocketChannel.accept(); //do something with socketChannel...}

打开 ServerSocketChanel

通过调⽤ ServerSocketChanel.open() ⽅法来打开ServerSocketChanel.如：

ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

关闭 ServerSocketChanel

通过调⽤ServerSocketChanel.close() ⽅法来关闭ServerSocketChanel. 如：

<table>
  <tr>
    <th>serverSocketChannel.close();</th>
  </tr>
</table>


# 监听新进来的连接

通常不会仅仅只监听⼀个连接,在while循环中调⽤ acept()⽅法. 如下⾯的例⼦：

while(true){ SocketChannel socketChannel = serverSocketChannel.accept(); //do something with socketChannel...} 通过 ServerSocketChannel.accept() ⽅法监听新进来的连接。

当 accept()⽅法返回的时候,它返回⼀个包含新进来的连接的 SocketChannel。因此, accept()⽅法 会⼀直阻塞到有新连接到达。

- 1

- 2


当然,也可以在while循环中使⽤除了true以外的其它退出准则。

# ⾮阻塞模式

ServerSocketChanel可以设置成⾮阻塞模式。在⾮阻塞模式下，acept() ⽅法会⽴刻返回，如果还没 有新进来的连接,返回的将是nul。 因此，需要检查返回的SocketChanel是否是nul.如：

ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();serverSocketChannel.socket().bind(new InetSocketAddress(9999));serverSocketChannel.configureBlocking(false);while(true){ SocketChannel socketChannel =serverSocketChannel.accept(); if(socketChannel != null){ //do something with socketChannel... }}

