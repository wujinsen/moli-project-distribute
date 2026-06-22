- 1）NameNode、DataNode和Client NameNode可以看作是分布式⽂件系统中的管理者，主要负责管理⽂件系统的命名空间、集群配

置信息和存储块的复制等。NameNode会将⽂件系统的Meta-data存储在内存中，这些信息主要包括了 ⽂件信息、每⼀个⽂件对应的⽂件块的信息和每⼀个⽂件块在DataNode的信息等。 DataNode是⽂件存储的基本单元，它将Block存储在本地⽂件系统中，保存了Block的Meta-data，同时 周期性地将所有存在的Block信息发送给NameNode。 Client就是需要获取分布式⽂件系统⽂件的应⽤程序。

- 2）⽂件写⼊ Client向NameNode发起⽂件写⼊的请求。 NameNode根据⽂件⼤⼩和⽂件块配置情况，返回给Client它所管理部分DataNode的信息。 Client将⽂件划分为多个Block，根据DataNode的地址信息，按顺序写⼊到每⼀个DataNode块中。

- 3）⽂件读取 Client向NameNode发起⽂件读取的请求。 NameNode返回⽂件存储的DataNode的信息。 Client读取⽂件信息。


---------------------------------------------------------------------------------------------------------------------------

-------------------------------------

通信⽅式介绍：

在hadoop系统中，master/slaves/client的对应关系是： master---namenode； slaves---datanode； client---dfsclient； 那究竟是通过什么样的⽅式进⾏通信的呢，在这⾥从⼤体介绍⼀下： 简单地讲： client和namenode之间是通过rpc通信； datanode和namenode之间是通过rpc通信； client和datanode之间是通过简单的socket通信。

# 随便拔⼀下DFSClient的代码，可以看到它有⼀个成员变量public final ClientProtocolnamenode; ⽽再拔⼀下DataNode的代码，可以看到它也有⼀个成员变量public DatanodeProtocolnamenode

