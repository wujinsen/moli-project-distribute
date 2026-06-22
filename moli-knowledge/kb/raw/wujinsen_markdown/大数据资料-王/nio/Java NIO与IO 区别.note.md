当学习了Java NIO和IO的API后，⼀个问题⻢上涌⼊脑海： 我应该何时使⽤IO，何时使⽤NIO呢？在本⽂中，我会尽量清晰地解析Java NIO和IO的差异、它们的使 ⽤场景，以及它们如何影响您的代码设计。

JavaNIO和IO的主要区别

下表总结了Java NIO和IO之间的主要差别，我会更详细地描述表中每部分的差异。 IO NIO⾯向流 ⾯向缓冲阻塞IO ⾮阻塞IO⽆ 选择器

⾯向流与⾯向缓冲

Java NIO和IO之间第⼀个最⼤的区别是，IO是⾯向流的，NIO是⾯向缓冲区的。 Java IO⾯向流意味着 每次从流中读⼀个或多个字节，直⾄读取所有字节，它们没有被缓存在任何地⽅。此外，它不能前后 移动流中的数据。如果需要前后移动从流中读取的数据，需要先将它缓存到⼀个缓冲区。 Java NIO的 缓冲导向⽅法略有不同。数据读取到⼀个它稍后处理的缓冲区，需要时可在缓冲区中前后移动。这就 增加了处理过程中的灵活性。但是，还需要检查是否该缓冲区中包含所有您需要处理的数据。⽽且， 需确保当更多的数据读⼊缓冲区时，不要覆盖缓冲区⾥尚未处理的数据。

阻塞与⾮阻塞IO

Java IO的各种流是阻塞的。这意味着，当⼀个线程调⽤read() 或 write()时，该线程被阻塞，直到有⼀ 些数据被读取，或数据完全写⼊。该线程在此期间不能再⼲任何事情了。 Java NIO的⾮阻塞模式，使 ⼀个线程从某通道发送请求读取数据，但是它仅能得到⽬前可⽤的数据，如果⽬前没有数据可⽤时， 就什么都不会获取。⽽不是保持线程阻塞，所以直⾄数据变的可以读取之前，该线程可以继续做其他 的事情。 ⾮阻塞写也是如此。⼀个线程请求写⼊⼀些数据到某通道，但不需要等待它完全写⼊，这个 线程同时可以去做别的事情。 线程通常将⾮阻塞IO的空闲时间⽤于在其它通道上执⾏IO操作，所以⼀ 个单独的线程现在可以管理多个输⼊和输出通道（chanel）。

选择器（Selectors）

Java NIO的选择器允许⼀个单独的线程来监视多个输⼊通道，你可以注册多个通道使⽤⼀个选择器， 然后使⽤⼀个单独的线程来“选择”通道：这些通道⾥已经有可以处理的输⼊，或者选择已准备写⼊的 通道。这种选择机制，使得⼀个单独的线程很容易来管理多个通道。

NIO和IO如何影响应⽤程序的设计

⽆论您选择IO或NIO⼯具箱，可能会影响您应⽤程序设计的以下⼏个⽅⾯：

- 1.
- 2.


对NIO或IO类的API调⽤。 数据处理。

3.

⽤来处理数据的线程数。

API调⽤

当然，使⽤NIO的API调⽤时看起来与使⽤IO时有所不同，但这并不意外，因为并不是仅从⼀个 InputStream逐字节读取，⽽是数据必须先读⼊缓冲区再处理。

数据处理

使⽤纯粹的NIO设计相较IO设计，数据处理也受到影响。 在IO设计中，我们从InputStream或 Reader逐字节读取数据。假设你正在处理⼀基于⾏的⽂本数据 流，例如：

- 1 Name: Anna

- 2 Age: 25

- 3 Email: anna@mailserver.com

- 4 Phone: 1234567890


该⽂本⾏的流可以这样处理： InputStream input = … ; / get the InputStream from the client socket

<table>
  <tr>
    <th>1</th>
    <th>BufferedReader reader = new BufferedReader( new InputStreamReader(input));<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>String nameLine = reader.readLine();</th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th>String ageLine = reader.readLine();</th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>String emailLine = reader.readLine();</th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th>String phoneLine = reader.readLine();</th>
  </tr>
</table>


请注意处理状态由程序执⾏多久决定。换句话说，⼀旦reader.readLine()⽅法返回，你就知道肯定⽂本 ⾏就已读完， readline()阻塞直到整⾏读完，这就是原因。你也知道此⾏包含名称；同样，第⼆个 readline()调⽤返回的时候，你知道这⾏包含年龄等。 正如你可以看到，该处理程序仅在有新数据读⼊ 时运⾏，并知道每步的数据是什么。⼀旦正在运⾏的线程已处理过读⼊的某些数据，该线程不会再回 退数据（⼤多如此）。下图也说明了这条原则： （Java IO: 从⼀个阻塞的流中读数据） ⽽⼀个NIO的实现会有所不同，下⾯是⼀个简单的例⼦：

<table>
  <tr>
    <th>1</th>
    <th>ByteBuffer buffer = ByteBuffer.allocate(<br><br>48 );<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>int bytesRead = inChannel.read(buffer);<br><br></th>
  </tr>
</table>


注意第⼆⾏，从通道读取字节到ByteBufer。当这个⽅法调⽤返回时，你不知道你所需的所有数据是否 在缓冲区内。你所知道的是，该缓冲区包含⼀些字节，这使得处理有点困难。 假设第⼀次 read(bufer)调⽤后，读⼊缓冲区的数据只有半⾏，例如，“Name:An”，你能处理数据吗？ 显然不能，需要等待，直到整⾏数据读⼊缓存，在此之前，对数据的任何处理毫⽆意义。 所以，你怎么知道是否该缓冲区包含⾜够的数据可以处理呢？好了，你不知道。发现的⽅法只能查看 缓冲区中的数据。其结果是，在你知道所有数据都在缓冲区⾥之前，你必须检查⼏次缓冲区的数据。 这不仅效率低下，⽽且可以使程序设计⽅案杂乱不堪。例如：

<table>
  <tr>
    <th>1</th>
    <th>ByteBuffer buffer = ByteBuffer.allocate(<br><br>48 );<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>2</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>3</th>
    <th>int bytesRead = inChannel.read(buffer);<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>4</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>5</th>
    <th>while (! bufferFull(bytesRead) ) {<br><br></th>
  </tr>
</table>


<table>
  <tr>
    <th>6</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>7</th>
    <th>bytesRead = inChannel.read(buffer);</th>
  </tr>
</table>


<table>
  <tr>
    <th>8</th>
    <th> </th>
  </tr>
</table>


<table>
  <tr>
    <th>9</th>
    <th>}</th>
  </tr>
</table>


buferFul()⽅法必须跟踪有多少数据读⼊缓冲区，并返回真或假，这取决于缓冲区是否已满。换句话 说，如果缓冲区准备好被处理，那么表示缓冲区满了。 buferFul()⽅法扫描缓冲区，但必须保持在buferFul（）⽅法被调⽤之前状态相同。如果没有，下⼀ 个读⼊缓冲区的数据可能⽆法读到正确的位置。这是不可能的，但却是需要注意的⼜⼀问题。 如果缓冲区已满，它可以被处理。如果它不满，并且在你的实际案例中有意义，你或许能处理其中的 部分数据。但是许多情况下并⾮如此。下图展示了“缓冲区数据循环就绪”：

Java NIO:从⼀个通道⾥读数据，直到所有的数据都读到缓冲区⾥.

3) ⽤来处理数据的线程数 NIO可让您只使⽤⼀个（或⼏个）单线程管理多个通道（⽹络连接或⽂件），但付出的代价是解析数据 可能会⽐从⼀个阻塞流中读取数据更复杂。 如果需要管理同时打开的成千上万个连接，这些连接每次只是发送少量的数据，例如聊天服务器，实 现NIO的服务器可能是⼀个优势。同样，如果你需要维持许多打开的连接到其他计算机上，如P2P⽹络 中，使⽤⼀个单独的线程来管理你所有出站连接，可能是⼀个优势。⼀个线程多个连接的设计⽅案如 下图所示：

Java NIO: 单线程管理多个连接 如果你有少量的连接使⽤⾮常⾼的带宽，⼀次发送⼤量的数据，也许典型的IO服务器实现可能⾮常契 合。下图说明了⼀个典型的IO服务器设计：

Java IO: ⼀个典型的IO服务器设计- ⼀个连接通过⼀个线程处理.

