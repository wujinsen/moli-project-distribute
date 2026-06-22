Java NIO 管道是2个线程之间的单向数据连接。 Pipe 有⼀个source通道和⼀个sink通道。数据会被 写到sink通道，从source通道读取。 这⾥是Pipe原理的图示：

![image 1](<Java NIO Pipe.note_images/imageFile1.png>)

# 创建管道

通过 Pipe.open() ⽅法打开管道。例如：

- 1 Pipe pipe = Pipe.open();

- 2


# 向管道写数据

要向管道写数据，需要访问sink通道。像这样：

- 1 Pipe.SinkChannel sinkChannel = pipe.sink();

- 2


通过调⽤SinkChanel的 write() ⽅法，将数据写⼊ SinkChannel ,像这样：

- 1 String newData = "New String to write to file..." + System.currentTimeMillis();

- 2 ByteBuffer buf = ByteBuffer.allocate(48);

- 3 buf.clear();

- 4 buf.put(newData.getBytes());

- 5

- 6 buf.flip();

- 7

- 8 while(buf.hasRemaining()) {

- 9 sinkChannel.write(buf);

- 10 }

- 11

- 12


# 从管道读取数据

从读取管道的数据，需要访问source通道，像这样：

- 1 Pipe.SourceChannel sourceChannel = pipe.source();

- 2


调⽤source通道的 read() ⽅法来读取数据，像这样：

- 1 ByteBuffer buf = ByteBuffer.allocate(48);

- 2

- 3 int bytesRead = sourceChannel.read(buf);

- 4


read() ⽅法返回的int值会告诉我们多少字节被读进了缓冲区。

