# 1.Windows下安装RabbitMQ需要以下⼏个步骤

- (1)：下载erlang，原因在于RabbitMQ服务端代码是使用并发式语⾔erlang编写的，下载地址：

http://www.erlang.org/downloads ， 双 击 .exe ⽂ 件 进 ⾏ 安 装 就 好 ， 安 装 完 成 之 后 创 建 ⼀ 个 名 为 ERLANG_HOME的环境变量，其值指向erlang的安装目录，同时将%ERLANG_HOME%\bin加⼊到 Path中，最后打开命令⾏，输⼊erl，如果出现erlang的版本信息就表示erlang语⾔环境安装成功；

- (2)：下载RabbitMQ，下载地址：http://www.rabbitmq.com/，同样双击.exe进⾏安装就好(这里需要注

意⼀点，默认的安装目录是C:/Program Files/....，这个目录中是存在空格符的，我们需要改变安装目 录，貌似RabbitMQ安装目录中是不允许有空格的，我之前踩过这个⼤坑)；

- (3)：安装RabbitMQ-Plugins，这个相当于是⼀个管理界面，⽅便我们在浏览器界面查看RabbitMQ各


![image 1](<RabbitMQ系列(一)：Windows下RabbitMQ安装及入门.note_images/imageFile1.png>)

![image 2](<RabbitMQ系列(一)：Windows下RabbitMQ安装及入门.note_images/imageFile2.png>)

个消息队列以及exchange的⼯作情况，安装⽅法是：打开命令⾏cd进⼊rabbitmq的sbin目录(我的目录 是 ： E:\software\rabbitmq\rabbitmq_server-3.6.5\sbin) ， 输 ⼊ ： rabbitmq-plugins enable rabbitmq_management命令，稍等会会发现出现plugins安装成功的提示，默认是安装6个插件，如果你 在安装插件的过程中出现了下面的错误：

![image 3](<RabbitMQ系列(一)：Windows下RabbitMQ安装及入门.note_images/imageFile3.png>)

解决⽅法是：首先在命令⾏输⼊：rabbitmq-service stop，接着输⼊rabbitmq-service remove，再接着 输⼊rabbitmq-service install，接着输⼊rabbitmq-service start，最后重新输⼊rabbitmq-plugins enable rabbitmq_management试试，我是这样解决的；

(4)：插件安装完之后，在浏览器输⼊http://localhost:15672进⾏验证，你会看到下面界面，输⼊用户 名：guest，密码：guest你就可以进⼊管理界面，当然用户名密码你都可以变的；

![image 4](<RabbitMQ系列(一)：Windows下RabbitMQ安装及入门.note_images/imageFile4.png>)

# 2.安装完RabbitMQ之后，我们先来简单了解下RabbitMQ中涉及到的⼏个概念 producer：消息⽣产者 consumer：消息消费者

virtual host：虚拟主机，在RabbitMQ中，用户只能在虚拟主机的层面上进⾏⼀些权限设置，比如我 可以访问哪些队列，我可以处理哪些请求等等；

broker：消息转发者，也就是我们RabbitMQ服务端充当的功能了，那么消息是按照什么规则进⾏转 发的呢？需要用到下面⼏个概念；

exchange：交换机，他是和producer直接进⾏打交道的，有点类似于路由器的功能，主要就是进⾏转 发操作的呗，那么producer到底用哪个exchange进⾏路由呢？这个取决于routing key(路由键)，每个消 息都有这个键，我们也可以自⼰设定，其实就是⼀字符串；

queue：消息队列，用于存放消息，他接收exchange路由过来的消息，我们可以对队列内容进⾏持久 化操作，那么queue到底接收那个exchange路由的消息呢？这个时候就要用到binding key(绑定键)了，绑 定键会将队列和exchange进⾏绑定，⾄于绑定⽅式，RabbitMQ提供了多种⽅式，⼤家可以看看鸿洋⼤ 神的RabbitMQ博客系列( )；

点击查看

以上就是RabbitMQ涉及到的⼀些概念了，用⼀张图表示这些概念之间的关系就是：

![image 5](<RabbitMQ系列(一)：Windows下RabbitMQ安装及入门.note_images/imageFile5.png>)

- 3.RabbitMQ简单使用 producer(⽣产者)端步骤：


- (1)：创建ConnectionFactory，并且设置⼀些参数，比如hostname,portNumber等等

- (2)：利用ConnectionFactory创建⼀个Connection连接

- (3)：利用Connection创建⼀个Channel通道

- (4)：创建queue并且和Channel进⾏绑定

- (5)：创建消息，并且发送到队列中 注意，在我们当前的例⼦中，并没有用到exchange交换机，RabbitMQ默认情况下是会创建⼀个空字


符串名字的exchange的，如果我们没有创建自⼰的exchange的话，默认就是使用的这个exchange； producer端代码：

[java] view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.


public class Sender { private final static String QUEUE_NAME = "MyQueue";

public static void main(String[] args) {

send(); }

## public static void send() {

ConnectionFactory factory = null; Connection connection = null; Channel channel = null; try {

factory = new ConnectionFactory(); factory.setHost("localhost"); connection = factory.newConnection(); channel = connection.createChannel();

channel.queueDeclare(QUEUE_NAME, false, false, false, null);

- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.


String message = "my first message .....";

channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8")); System.out.println("已经发送消息....."+message);

} catch (IOException e) { e.printStackTrace();

} catch (TimeoutException e) {

e.printStackTrace(); }finally{

try { //关闭资源 channel.close(); connection.close();

} catch (IOException e) { e.printStackTrace();

} catch (TimeoutException e) {

e.printStackTrace(); }

} }

}

consumer(消费者)端步骤：

- (1)：创建ConnectionFactory，并且设置⼀些参数，比如hostname,portNumber等等

- (2)：利用ConnectionFactory创建⼀个Connection连接

- (3)：利用Connection创建⼀个Channel通道

- (4)：将queue和Channel进⾏绑定，注意这里的queue名字要和前面producer创建的queue⼀致

- (5)：创建消费者Consumer来接收消息，同时将消费者和queue进⾏绑定


consumer端代码：

[java]

view plain copy

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.


public class Receiver { private final static String QUEUE_NAME = "MyQueue";

public static void main(String[] args) {

receive(); }

## public static void receive() {

ConnectionFactory factory = null; Connection connection = null; Channel channel = null;

## try {

factory = new ConnectionFactory(); factory.setHost("localhost"); connection = factory.newConnection(); channel = connection.createChannel();

- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.


channel.queueDeclare(QUEUE_NAME, false, false, false, null); Consumer consumer = new DefaultConsumer(channel){

@Override

public void handleDelivery(String consumerTag, Envelope envelope, BasicProp erties properties,

byte[] body) throws IOException { System.out.println("11111111111");

String message = new String(body, "UTF-8"); System.out.println("收到消息....."+message);

}};

channel.basicConsume(QUEUE_NAME, true,consumer); } catch (IOException e) { e.printStackTrace();

} catch (TimeoutException e) {

e.printStackTrace(); }finally{

try { //关闭资源 channel.close(); connection.close();

} catch (IOException e) { e.printStackTrace();

} catch (TimeoutException e) {

e.printStackTrace(); }

} }

}

好了，这篇先到这了，下⼀篇我会简单介绍点更深⼊的东西，后续也会对RabbitMQ原⽣API进⾏封 装，便于我们自⼰开发；

