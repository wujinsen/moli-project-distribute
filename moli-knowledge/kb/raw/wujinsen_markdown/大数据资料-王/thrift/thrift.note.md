htp:/gemantic.iteye.com/blog/19214

⼀、About thrift ⼆、什么是thrift，怎么⼯作？ 三、Thrift IDL 四、Thrift Demo 五、Thrift 协议栈 以及各层的使⽤（java 为例） 六、与protocolbufer的区别

⼀、About thrift

thrift是⼀种可伸缩的跨语⾔服务的发展软件框架。它结合了功能强⼤的软件堆栈的代码⽣成引 擎，以建设服务，⼯作效率和⽆缝地与C + +，C#，Java，Python和PHP和Ruby结合。thrift是 facebok开发的，我们现在把它作为开源软件使⽤。thrift允许你定义⼀个简单的定义⽂件中的数据类 型和服务接⼝。以作为输⼊⽂件，编译器⽣成代码⽤来⽅便地⽣成RPC客户端和服务器通信的⽆缝跨 编程语⾔（来⾃百度百科）。

>最初由facebok开发⽤做系统内个语⾔之间的RPC通信 。 >207年由facebok贡献到apache基⾦ ，现在是apache下的opensource之⼀ 。 >⽀持多种语⾔之间的RPC⽅式的通信：php语⾔client可以构造⼀个对象，调⽤相应的服务⽅法来

调⽤java语⾔的服务 ，跨越语⾔的C/S rpc 调⽤ 。

⼆、什么是thrift，怎么⼯作？

java rmi的例⼦，代码⻅附件，建⽴⼀个java rmi的流程 ： >定义⼀个服务调⽤接⼝ 。 >server端：接⼝实现 -impl的实例 -注册该服务实现（端⼝） -启动服务。 >client端：通过ip、端⼝、服务名，得到服务，通过接⼝来调⽤ 。 >rmi数据传输⽅式：java对象序列化 。

Thrift 服务 >例同rmi ，需要定义通信接⼝、实现、注册服务、绑定端⼝ … >如何多种语⾔之间通信 ？ >数据传输⾛socket（多种语⾔均⽀持），数据再以特定的格式（String ），发送，接收⽅语⾔解

析 。 Object -> String -> Object 。

问题：编码、解析完全需要⾃⼰做 ，复杂的数据结构会编码困难 .

Thrift 服务 ：thrift的中间编码层 >java Object -> Thrift Object -> php Object > 定义thrift的⽂件 ，由thrift⽂件（IDL）⽣成 双⽅语⾔的接⼝、model ，在⽣成的model以及接

⼝中会有解码编码的代码 。

>thrift ⽂件例⼦ thrift-0.7.0.exe -r -gen java TestThrift.thrift ⽣成java 代码 thrift-0.7.0.exe -r -gen php TestThrift.thrift ⽣成php代码 thrift-0.7.0.exe -r -gen py TestThrift.thrift ⽣成python代码 thrift-0.7.0.exe -r -gen as3 TestThrift.thrift ⽣成as3代码 thrift-0.7.0.exe -r -gen cp TestThrift.thrift ⽣成C+代码

三、Thrift IDL

htp:/ w.cnblogs.com/tianhuilove/archive/201/09/05/216769.html

htp:/wiki.apache.org/thrift/

htp:/wiki.apache.org/thrift/ThriftTypes

四、Thrift Demo Thrift IDL ⽂件

Java代码

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


namespace java com.gemantic.analyse.thrift.index

structNewsModel{

- 1:i32 id ;
- 2:string title;
- 3:string content;
- 4:string media_from;
- 5:string author; }


service IndexNewsOperatorServices {

- 12.
- 13.
- 14.


bol indexNews(1 NewsModel indexNews), bol deleteArtificialyNews(1:i32 id ) }

java server

Java代码

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


package com.gemantic.analyse.thrift.index;

import java.net.InetSocketAdres;

import org.apache.thrift.protocol.TBinaryProtocol; import org.apache.thrift.server.TServer; import org.apache.thrift.server.ThreadPolServer; import org.apache.thrift.server.ThreadPolServer.Args; import org.apache.thrift.transport.TServerSocket; import org.apache.thrift.transport.TServerTransport; import org.apache.thrift.transport.TransportFactory;

public clas ThriftServerTest {

/*

- * @param args
- */ public static void main(String[] args) {


/ TODO Auto-generated method stub

IndexNewsOperatorServices.Procesor procesor = new IndexNewsOperatorServices.Pro cesor(new IndexNewsOperatorServicesImpl();

try{ TServerTransport serverTransport = new TServerSocket( new InetSocketAdres("0.0.0

.0",9813); Args trArgs=new Args(serverTransport); trArgs.procesor(procesor);

/使⽤⼆进制来编码应⽤层的数据 trArgs.protocolFactory(new TBinaryProtocol.Factory(true, true); /使⽤普通的socket来传输数据 trArgs.transportFactory(new TransportFactory();

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


TServer server = new ThreadPolServer(trArgs); System.out.println("server begin ."); server.serve(); System.out.println(" -"); server.stop();

}catch(Exception e){

throw new RuntimeException("index thrift server start failed!"+"/n"+e.getMesage(); }

}

}

java client

Java代码

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
- 19.
- 20.
- 21.


package com.gemantic.analyse.thrift.index;

import org.apache.thrift.TException; import org.apache.thrift.protocol.TBinaryProtocol; import org.apache.thrift.protocol.TProtocol; import org.apache.thrift.transport.TSocket; import org.apache.thrift.transport.Transport;

public clas ThriftClientTest {

/*

- * @param args
- * @throws TException
- */ public static void main(String[] args) throws TException {


/ TODO Auto-generated method stub

Transport transport = new TSocket("10.0.0.41", 9813); long start=System.curentTimeMilis();

/ Transport transport = new TSocket("218.1.178.10",9090); TProtocol protocol = new TBinaryProtocol(transport); IndexNewsOperatorServices.Client client=new IndexNewsOperatorServices.Client(protoc

ol);

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


transport.open();

client.deleteArtificialyNews(123456); NewsModel newsModel=new NewsModel(); newsModel.setId(789456); newsModel.setTitle("this from java client"); newsModel.setContent(" 世界杯⽐赛前，由于塞尔维亚和⿊⼭突然宣布分裂，国际⾜联开

会决定剔除塞⿊，由世界上球迷最多的国家顶替，名额恰巧来到中国。举国上下⼀⽚欢腾，中国 ⾜协决定由“成世铎”（成⻰+阎世铎）组队，进军世界杯。");

newsModel.setAuthor("dc"); newsModel.setMedia_from("新华08"); client.indexNews(newsModel); transport.close(); System.out.println(System.curentTimeMilis()-start); System.out.println("client suces!");

}

}

php client

Php代码

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


<?php $GLOBALS['THRIFT_ROT'] = '/home/tjiang/demo/thrift/lib/php/src'; require_once $GLOBALS['THRIFT_ROT'].'/Thrift.php'; require_once $GLOBALS['THRIFT_ROT'].'/protocol/TBinaryProtocol.php'; require_once $GLOBALS['THRIFT_ROT'].'/transport/TSocket.php'; require_once $GLOBALS['THRIFT_ROT'].'/transport/THtpClient.php'; require_once $GLOBALS['THRIFT_ROT'].'/transport/TBuferedTransport.php'; include_once $GLOBALS['THRIFT_ROT'].'/packages/TestThrift/TestThrift_types.php'; include_once $GLOBALS['THRIFT_ROT'].'/packages/TestThrift/IndexNewsOperatorServices. php'; $data=aray( 'id'=>'1', 'title'=>'demo-标题', 'content'=>'demo-内容',

- 14.
- 15.
- 16.
- 17.
- 18.
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


'media_from'=>'hexun', 'author'=>'xiaodi 67' ); $thrif_server_url = '10.0.0.41'; $transport = new TSocket($thrif_server_url, 9813); $transport->open();

$protocol = new TBinaryProtocol($transport);

$client= new IndexNewsOperatorServicesClient($protocol, $protocol); $obj = new NewsModel($data); $result = $client->indexNews($obj);

$transport->close(); ?>

python client

Python代码

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


#!/usr/bin/env python

# # Licensed to the Apache Software Foundation (ASF) under one # or more contributor license agrements. Se the NOTICE file # distributed with this work for aditional information # regarding copyright ownership. The ASF licenses this file # to you under the Apache License, Version 2.0 (the # "License"); you may not use this file except in compliance # with the License. You may obtain a copy of the License at # #htp:/ w.apache.org/licenses/LICENSE-2.0 # # Unles required by aplicable law or agred to in writing, # software distributed under the License is distributed on an # "AS IS" BASIS, WITHOUT WARANTIES OR CONDITIONS OF ANY # KIND, either expres or implied. Se the License for the # specific language governing permisions and limitations

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
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.


# under the License. #

import sys

from TestThrift.types import NewsModel from TestThrift.IndexNewsOperatorServices import Client

from thrift import Thrift from thrift.transport import TSocket from thrift.transport import Transport from thrift.protocol import TBinaryProtocol

try:

# Make socket transport = TSocket.TSocket('10.0.0.41', 9813)

# Bufering is critical. Raw sockets are very slow transport = Transport.TBuferedTransport(transport)

# Wrap in a protocol protocol = TBinaryProtocol.TBinaryProtocol(transport)

# Create a client to use the protocol encoder client = Client(protocol)

# Conect! transport.open()

client.deleteArtificialyNews(123)

newsModel=NewsModel() newsModel.id=123456 newsModel.title="python Test" newsModel.content="client testcome from python"; newsModel.media_from="xinhua08"

- 56.
- 57.
- 58.
- 59.
- 60.
- 61.
- 62.


client.indexNews(newsModel)

#close transport.close()

except Thrift.TException, tx: print '%s' % (tx.mesage)

Csharp client

C#代码

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


Transport transport = new TSocket("10.0.0.41", 9813); TProtocol protocol = new TBinaryProtocol(transport); IndexNewsOperatorServices.Client client = new IndexNewsOperatorServices.Client(protocol);

transport.Open(); NewsModel model = new NewsModel(); model.Author = "j w"; model.Title = "title"; model.Content = "client ComeFrom CSharp"; model.Id = 1;

client.deleteArtificialyNews(123); Console.WriteLine(client.indexNews(model);

五、Thrift 协议栈 以及各层的使⽤（java 为例）

![image 1](<thrift.note_images/imageFile1.png>)

- 1、model interface 服务的调⽤接⼝以及接⼝参数model、返回值model
- 2、Tprotocol 协议层 将数据（model）编码 、解码 。
- 3、Ttramsport 传输层 编码后的数据传输（简单socket、htp）


5、Tserver 服务的Tserver类型，实现了⼏种rpc调⽤（单线程、多线程、⾮阻塞IO）

六、与protocolbufer的区别

htp:/liuchangit.com/development/346.html

htp:/stackoverflow.com/questions/69316/bi gest-diferences-of-thrift-vs-protocol-bufers

区别：

- 1、Another important diference are the languages suported by default. protobuf: Java, C+, Python Thrift: Java, C+, Python, PHP, Ruby, Erlang, Perl, Haskel, C#, Cocoa, Smaltalk, Ocaml ⽀持语⾔不同，thrift⽀持着更多的语⾔ 。
- 2、Thrift suports ‘exceptions 。 thrift⽀持服务的异常 。
- 3、Protocol Bufers much easier to read 。Protobuf API l oks cleaner, though the generated clases are al packed as an i ner clases which is not so nice.


Protocol Bufers 在⽂档⽅⾯⽐thrift丰富，⽽且⽐thrift简单 。

- 4、Protobuf serialized objects are about 30% smaler then Thrift. Protocol Bufers在序列化/反序列化、传输上性能更优 。
- 5、RPC is another key diference. Thrift generates code to implement RPC clients and servers wheres Protocol Bufers sems mostly designed as a data-interchange format alone.

thrift提供了⼀套完整的rpc服务实现（多线程socket、⾮阻塞的socket .）

- 6、And acording to the wiki the Thrift runtime doesn't run on Windows. thrift 对有些语⾔在windows上不⽀持：C+ .


