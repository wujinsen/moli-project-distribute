# 第⼀篇blog，欢迎⼤家批评指正。

⼀ 前⾔

Thrift是facebok技术核⼼框架之⼀，不同开发语⾔开发的服务可以 通过该框架实现通信。Thrift通过接⼝定义语⾔ (interface definition language，IDL) 来定义数据类型和服务，Thrift接⼝定义⽂件由Thrift代码编译器⽣成thrift ⽬标语⾔的代码（⽬前⽀持C+,Java, Python,

PHP, Ruby, Erlang, Perl, Haskel, C#, Cocoa, Smaltalk和OCaml），并由⽣成的代码负责RPC协议层和传输层的实现。

简⽽⾔之，开发者只需准备⼀份thrift脚本，通过thrift code generator（像gc那样输⼊⼀个命令）就能⽣成所要求的开发语⾔代码。 不⽀持windows。

Thrift侧重点是构建跨语⾔的可伸缩的服务，特点就是⽀持的语⾔多， 同时提供了完整的RPC service framework，可以很⽅便的直接构建服务，不需要做太多其他的⼯作。服 务端可以根据需要编译成simple | thread-pol | threaded | nonblocking等⽅式；

本⽂档参考：Thrift types, Thrift IDL， Thrift:The Mising Guide.

⼆ 语法参考

2.1类型

Thrift类型系统包括预定义基本类型，⽤户⾃定义结构体，容器类型， 异常和服务定义。 2.1.1基本类型

bol:布尔值 (true or false), one byte

byte:有符号字节

i16: 16位有符号整型

i32: 32位有符号整型

i64: 64位有符号整型

double: 64位浮点型

string: Encoding agnostic text or binary string

Note that： Thrift不⽀持⽆符号整型，因为Thrift⽬标语⾔没有⽆符号整 型，⽆法转换。

2.1.2容器（Containers）

Thrift容器与流⾏编程语⾔的容器类型相对应，采⽤Java泛型⻛格。 它有3种可⽤容器类型：

list<t1>:元素类型为t1的有序表，容许元素重复。（有序表ordered list不 知道如何理解？排序的？c+的vector不排序）

set<t1>:元素类型为t1的⽆序表，不容许元素重复。

map<t1,t2>:键类型为t1，值类型为t2的kv对，键不容许重复。

容器中元素类型可以是除了service外的任何合法Thrift类型（包括结 构体和异常）。

2.1.3 结构体和异常（Structs and Exceptions）

Thrift结构体在概念上类似于（similar to）C语⾔结构体类型 -将相关 属性封装在⼀起的简便⽅式。Thrift结构体将会被转换成⾯向对象语⾔的 类。

异常在语法和功能上类似于（equivalent to）结构体，差别是异常使 ⽤关键字exception⽽不是struct声明。但它在语义上不同于结构体：当定 义⼀个RPC服务时，开发者可能需要声明⼀个远程⽅法抛出⼀个异常。

结构体和异常的声明将在下⼀节介绍。

2.1.4 服务（Services）

# 服务的定义⽅法在语义(semanticaly)上等同于⾯向对象语⾔中的接 ⼝。Thrift编译器会产⽣执⾏这些接⼝的client和server stub。具体参⻅下 ⼀节。

- 2.2 类型定义（Typedef)


Thrift⽀持C/C+类型定义。

typedef i32 MyInteger / a typedef T ReT / b

说明：a. 末尾没有逗号。b. struct也可以使⽤typedef。

- 2.3 枚举（Enums）


很多语⾔都有枚举，意义都⼀样。⽐如，当定义⼀个消息类型时，它 只能是预定义的值列表中的⼀个，可以⽤枚举实现。

enum TwetType { TWET, / (1) RETWET = 2, / (2) DM = 0xa, / (3) REPLY} / (4)struct Twet { 1: required i32 userId; 2: required string userName; 3: required string text; 4: optional Location loc; 5: optional TwetType twetType = TwetType.TWET; / (5) 16: optional string language = "english"}

说明：

- (1). 编译器默认从0开始赋值
- (2). 可以赋予某个常量某个整数
- (3). 允许常量是⼗六进制整数
- (4). 末尾没有分号
- (5). 给常量赋缺省值时，使⽤常量的全称 注意，不同于protocal bufer，thrift不⽀持枚举类嵌套，枚举常量必


须是32位的正整数

2.4 注释（Coment）

Thrift⽀持shel⻛格, C多⾏⻛格和Java/C+单⾏⻛格。

# This is a valid co ment./* * This is a multi-line co ment. * Just like in C. */ C+/Java style single-line co ments work just as wel.

2.5 名字空间（Namespace）

Thrift中的命名空间类似于C+中的namespace和java中的 package，它们提供了⼀种组织（隔离）代码的简便⽅式。名字空间也可 以⽤于解决类型定义中的名字冲突。

由于每种语⾔均有⾃⼰的命名空间定义⽅式（如python中有 module）, thrift允许开发者针对特定语⾔定义namespace：

namespace cp com.example.project / (1)namespace java com.example.project / (2)namespace php com.example.project

- (1)． 转化成namespace com { namespace example { namespace

project {

- (2)． 转换成package com.example.project


2.6 Includes

便于管理、重⽤和提⾼模块性/组织性，我们常常分割Thrift定义在不 同的⽂件中。包含⽂件搜索⽅式与c+⼀样。Thrift允许⽂件包含其它 thrift⽂件，⽤户需要使⽤thrift⽂件名作为前缀访问被包含的对象，如：

include "twet.thrift" /（1） .struct TwetSearchResult { 1: twet.Twet twet; /（2）}

说明：

- （1）． thrift⽂件名要⽤双引号包含，末尾没有逗号或者分号
- （2）． 注意twet前缀


2.7 常量（Constant)

Thrift允许定义跨语⾔使⽤的常量，复杂的类型和结构体可使⽤JSON 形式表示。

const i32 INT_CONST = 1234; /（1）

说明： （1） 分号可有可⽆。⽀持16进制。

- 2.8 结构体定义（Defining Struct）


struct是Thrift IDL中的基本组成块，由域组成，每个域有唯⼀整数标 识符，类型，名字和可选的缺省参数组成。如定义⼀个类似于Twiter服 务：

struct Twet { 1: required i32 userId; / (1) 2: required string userName; / (2) 3: required string text; 4: optional Location loc; / (3) 16: optional string language = "english"/ (4)}struct Location { / (5) 1: required double latitude; 2: required double longitude;}

- (1)每个域有⼀个唯⼀的正整数标识符；
- (2)每个域可标识为required或optional；
- (3)结构体可以包含其它结构体
- (4) 域可有默认值，与required或optional⽆关。
- (5) Thrift⽂件可以定义多个结构体，并在同⼀⽂件中引⽤，也可加⼊⽂件限定词在其它Thrift⽂件中引⽤。


如上所⻅，消息定义中的每个域都有⼀个唯⼀数字标签，这些数字标签在传输时⽤来确定域，⼀旦使⽤消息类

型，标签不可改变。（随着项⽬的进展，可以要变更Thrift⽂件，最好不要改变原有的数字标签）

规范的struct定义中的每个域均会使⽤required或者 optional关键字进⾏标识。如果required标识的域没有赋值，Thrift将给予提示；如果optional标识的域没有赋值， 该域将不会被

序列化传输；如果某个optional标识域有缺省值⽽⽤户没有重新赋值，则该域的值⼀直为缺省值；如果某个 optional标识域有缺省值或者⽤户已经重新赋值，⽽不设置它的 _iset为true，也不会被序列化传输。（不被 序列化传输的后果是什么？为空为零？还是默认值，下次试试）

# 与services不同，结构体不⽀持继承。

- 2.9 服务定义（Defining Services）


在流⾏的序列化/反序列化框架（如protocal bufer）中，Thrift是少 有的提供多语⾔间RPC服务的框架。这是Thrift的⼀⼤特⾊。

Thrift编译器会根据选择的⽬标语⾔为server产⽣服务接⼝代码，为 client产⽣stubs。

service Twiter { / A method definition l oks like C code. It has a return type, arguments, / and optionaly a list of exceptions that it may throw. Note that argument / lists and exception list are specified using the exact same syntax as / field lists in structs. void ping(), / (1) bol postTwet(1:Twet twet);

/ (2) TwetSearchResult searchTwets(1:string query); / (3) / The 'oneway' modifier indicates that the client only makes a request and

/ does not wait for any response at al. Oneway methods MUST be void. oneway void zip() / (4)}

- (1)有点乱，接⼝⽀持以逗号和分号结束；
- (2)参数可以是基本类型和结构体；（参数是cosnt的，转换为c+语⾔

是const&）

- (3)返回值同参数⼀样；
- (4)返回值是void，注意oneway；


Note that:参数列表的定义与结构体⼀样。服务⽀持继承。

# 慢慢写，下次继续

