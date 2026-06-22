Java NIO开始⽀持scater/gather，scater/gather⽤于描述从Chanel中读取或者写⼊到Chanel的操 作。 分散（scater）：从Chanel中读操作时，将读取的数据写⼊多个bufer中。 聚集（gather）：向Chanel中写操作时，将多个bufer的数据写⼊同⼀个Chanel。

scater / gather经常⽤于需要将传输的数据分开处理的场合，例如传输⼀个由消息头和消息体组成的 消息，你可能会将消息体和消息头分散到不同的bufer中，这样你可以⽅便的处理消息头和消息体。 Scatering Reads是指数据从⼀个chanel读取到多个bufer中。如下图描述：

![image 1](<Java NIO Scatter Gather.note_images/imageFile1.png>)

代码示例如下：

<table>
  <tr>
    <th>ByteBufer header = ByteBufer.alocate(128);ByteBufer body =</th>
  </tr>
</table>


ByteBufer.alocate(1024);ByteBufer[] buferAray = { header, body };chanel.read(buferAray);

- 1、bufer⾸先被插⼊到数组
- 2、然后再将数组作为chanel.read() 的输⼊参数。
- 3、read()⽅法按照bufer在数组中的顺序将从chanel中读取的数据写⼊到bufer，当⼀个bufer被写 满后，chanel紧接着向另⼀个bufer中写。
- 4、Scatering Reads在移动下⼀个bufer前，必须填满当前的bufer，这也意味着它不适⽤于动态消息 (消息⼤⼩不固定)。换句话说，如果存在消息头和消息体，消息头必须完成填充（例如 128byte）， Scatering Reads才能正常⼯作。


Gathering Writes是指数据从多个bufer写⼊到同⼀个chanel。如下图描述：

![image 2](<Java NIO Scatter Gather.note_images/imageFile2.png>)

代码示例如下：

<table>
  <tr>
    <th>1 ByteBuffer header = ByteBuffer.allocate(128);<br><br>2 ByteBuffer body = ByteBuffer.allocate(1024);<br><br>3<br><br>4 //write data into buffers<br><br>5 ByteBuffer[] bufferArray = { header, body };<br><br>6 channel.write(bufferArray);<br></th>
  </tr>
</table>


- 1、bufers数组是write()⽅法的⼊参
- 2、write()⽅法会按照bufer在数组中的顺序，将数据写⼊到chanel
- 3、注意只有position和limit之间的数据才会被写⼊。
- 4、因此，如果⼀个bufer的容量为128byte，但是仅仅包含58byte的数据，那么这58byte的数据将被 写⼊到chanel中。
- 5、因此与Scatering Reads相反，Gathering Writes能较好的处理动态消息。


