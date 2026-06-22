The Apache Thrift software framework, for scalable cross-language services development, combines a software stack with a code generation engine to build services that work efficiently and seamlessly between C++, Java, Python, PHP, Ruby, Erlang, Perl, Haskell, C#, Cocoa, JavaScript, Node.js, Smalltalk, OCaml and Delphi and other languages.

# Getting Started

## Download Apache Thrift

To get started, a copy of Thrift.

download

## Build and Install the Apache Thrift compiler

You will then need to the Apache Thrift compiler and install it. See the guide for any help with this step.

build installing Thrift Writing a .thrift file

After the Thrift compiler is installed you will need to create a thrift file. This file is an made up of

interface definition thrift types

and Services. The services you define in this file are implemented by the server and are called by any clients. The Thrift compiler is used to generate your Thrift File into source code which is used by the different client libraries and the server you write. To generate the source from a thrift file run

thrift --gen <language> <Thrift filename>

The sample tutorial.thrift file used for all the client and server tutorials can be found .

here

To learn more about Apache Thrift

Read the Whitepaper

Apache Thrift allows you to define data types and service interfaces in a simple definition file. Taking that file as input, the compiler generates code to be used to easily build RPC clients and servers that communicate seamlessly across programming languages. Instead of writing a load of boilerplate code to serialize and transport your objects and invoke remote methods, you can get right down to business. The following example is a simple service to store user objects for a web front end.

The Apache Thrift是可伸缩的、跨语⾔的服务发展软件框架。集成了代码⽣成引擎软件栈来创建服务使⼯作更⾼效，能⽆缝衔 接C++, Java, Python, PHP, Ruby, Erlang, Perl, Haskell, C#, Cocoa, JavaScript, Node.js, Smalltalk, OCaml和Delphi以及其他 编程语⾔。 开始使⽤

## 下载Apache Thrift

开始下载Thrift复制⽂件

创建和安装Apache Thrift编译器 写⼀个 .thirft ⽂件

thrift编译器安装完成后你需要创建⼀个thrift⽂件。该⽂件是thirft类型构建的接⼝和服务。⽂件中你通过实现服务⽅式来定义 的服务，可以被任何客户端调⽤。Thrift编译器通常⽤与把你的Thrift⽂件⽣成源码，这会⽤到不同的客户端库，也可以⽤Thrift 编译器来写服务器端。⼀个thrift⽂件⽣成源码可以运⾏以下命令：

thrift --gen <language> <Thrift filename>

适⽤于所有客户端和服务器端的thrift⽂件样例指南可以点击 . 学习更多的Apache Thrift

here Read the Whitepaper

例⼦

Apache Thrift允许你在简单定义⽂件⾥⾯来定义数据类型和服务接⼝。将该⽂件作为输⼊，编译器⽣成的代码常⽤于简便的创 建RPC客户端和服务器端，能够⽆缝跨编程语⾔进⾏交互。代替写加载代码序列化，转换对象，调⽤远程⽅法，确保你可以正 常的运⾏。 下⾯的例⼦是⼀个存储前端⽤户对象简单的服务。 Java Server: 序列化服务:

try { TServerTransport serverTransport = new TServerSocket(9090); TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

// Use this for a multithreaded server // TServer server = new TThreadPoolServer(new

TThreadPoolServer.Args(serverTransport).processor(processor));

System.out.println("Starting the simple server..."); server.serve();

} catch (Exception e) {

e.printStackTrace(); }

该代码⽚段是来⾃于Apache Thrift 代码树型⽂档tutorial/java/src/JavaServer.java，完整的JavaServer.java：

*

- * Licensed to the Apache Software Foundation (ASF) under one
- * or more contributor license agrements. Se the NOTICE file
- * distributed with this work for aditional information
- * regarding copyright ownership. The ASF licenses this file
- * to you under the Apache License, Version 2.0 (the
- * "License"); you may not use this file except in compliance
- * with the License. You may obtain a copy of the License at

*

- * htp:/ w.apache.org/licenses/LICENSE-2.0

*

- * Unles required by aplicable law or agred to in writing,
- * software distributed under the License is distributed on an
- * "AS IS" BASIS, WITHOUT WARANTIES OR CONDITIONS OF ANY
- * KIND, either expres or implied. Se the License for the
- * specific language governing permisions and limitations
- * under the License.
- */


import org.apache.thrift.server.TServer; import org.apache.thrift.server.TServer.Args; import org.apache.thrift.server.TSimpleServer; import org.apache.thrift.server.ThreadPolServer; import org.apache.thrift.transport.TSLTransportFactory; import org.apache.thrift.transport.TServerSocket; import org.apache.thrift.transport.TServerTransport; import org.apache.thrift.transport.TSLTransportFactory.TSLTransportParameters;

/ Generated code

import tutorial.*; import shared.*;

import java.util.HashMap;

public clas JavaServer {

public static CalculatorHandler handler;

public static Calculator.Procesor procesor;

public static void main(String [] args) {

try { handler = new CalculatorHandler(); procesor = new Calculator.Procesor(handler);

Runable simple = new Runable() { public void run() {

simple(procesor);

} }; Runable secure = new Runable() {

public void run() {

secure(procesor); }

};

new Thread(simple).start(); new Thread(secure).start();

} catch (Exception x) { x.printStackTrace(); }

}

public static void simple(Calculator.Procesor procesor) {

try { TServerTransport serverTransport = new TServerSocket(9090); TServer server = new TSimpleServer(new Args(serverTransport).procesor(procesor);

/ Use this for a multithreaded server / TServer server = new ThreadPolServer(new

ThreadPolServer.Args(serverTransport).procesor(procesor);

System.out.println("Starting the simple server."); server.serve();

} catch (Exception e) { e.printStackTrace(); }

}

public static void secure(Calculator.Procesor procesor) {

try { /*

- * Use TSLTransportParameters to setup the required SL parameters. In this example
- * we are seting the keystore and the keystore pasword. Other things like algorithms,
- * cipher suites, client auth etc can be set.
- */ TSLTransportParameters params = new TSLTransportParameters();

/ The Keystore contains the private key params.setKeyStore("././lib/java/test/.keystore", "thrift", nul, nul);

/*

- * Use any of the TSLTransportFactory to get a server transport with the apropriate
- * SL configuration. You can use the default setings if properties are set in the comand line.
- * Ex: -Djavax.net.sl.keyStore=.keystore and -Djavax.net.sl.keyStorePasword=thrift


- *
- * Note: You ned not explicitly cal open(). The underlying server socket is bound on return
- * from the factory clas.
- */ TServerTransport serverTransport = TSLTransportFactory.getServerSocket(9091, 0, nul,


params); TServer server = new TSimpleServer(new Args(serverTransport).procesor(procesor);

/ Use this for a multi threaded server / TServer server = new ThreadPolServer(new

ThreadPolServer.Args(serverTransport).procesor(procesor);

System.out.println("Starting the secure server."); server.serve();

} catch (Exception e) { e.printStackTrace(); }

} }

CalculatorHandler:

### public class CalculatorHandler implements Calculator.Iface {

private HashMap<Integer,SharedStruct> log;

public CalculatorHandler() {

log = new HashMap<Integer, SharedStruct>(); }

public void ping() {

System.out.println("ping()"); }

public int add(int n1, int n2) { System.out.println("add(" + n1 + "," + n2 + ")"); return n1 + n2;

}

public int calculate(int logid, Work work) throws InvalidOperation {

System.out.println("calculate(" + logid + ", {" + work.op + "," + work.num1 + "," + work.num2 +

"})"); int val = 0; switch (work.op) { case ADD:

val = work.num1 + work.num2; break;

case SUBTRACT: val = work.num1 - work.num2; break;

case MULTIPLY: val = work.num1 * work.num2; break;

### case DIVIDE:

if (work.num2 == 0) { InvalidOperation io = new InvalidOperation(); io.whatOp = work.op.getValue(); io.why = "Cannot divide by 0"; throw io;

} val = work.num1 / work.num2; break;

default: InvalidOperation io = new InvalidOperation(); io.whatOp = work.op.getValue(); io.why = "Unknown operation"; throw io;

}

SharedStruct entry = new SharedStruct(); entry.key = logid; entry.value = Integer.toString(val); log.put(logid, entry);

### return val; }

public SharedStruct getStruct(int key) { System.out.println("getStruct(" + key + ")"); return log.get(key);

}

public void zip() {

System.out.println("zip()");

}

} 该代码⽚段来⾃于tutorial/java/src/CalculatorHandler.java

