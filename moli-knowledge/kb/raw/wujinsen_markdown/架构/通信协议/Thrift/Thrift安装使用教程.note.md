Apache官⽹下载Thrift: thrift-0.10.0.tar.gz

linux系统：CentOS6.5

安装thrift之前先安装或升级依赖包

sudo yum -y groupinstall "Development Tools"

yum install automake libtool ﬂex bison pkgconﬁg gcc-c++ boost-devel libevent-devel zlibdevel python-devel ruby-devel

autoconf

wget http://ftp.gnu.org/gnu/autoconf/autoconf-2.69.tar.gz tar xvf autoconf-2.69.tar.gz cd autoconf-2.69

./configure --prefix=/usr make sudo make install

# automake

wget http://ftp.gnu.org/gnu/automake/automake-1.14.tar.gz tar xvf automake-1.14.tar.gz cd automake-1.14

./configure --prefix=/usr make sudo make install

# bison

wget http://ftp.gnu.org/gnu/bison/bison-2.5.1.tar.gz tar xvf bison-2.5.1.tar.gz cd bison-2.5.1

./configure --prefix=/usr make sudo make install 安装c++依赖库 sudo yum -y install libevent-devel zlib-devel openssl-devel 安装boost,⾄少1.53版本 wget wget http://sourceforge.net/projects/boost/files/boost/1.53.0/boost_1_53_0.tar.gz --no-checkcertificate

http://sourceforge.net/projects/boost/files/boost/1.53.0/boost_1_53_0.tar.gz

./bootstrap.sh sudo ./b2 install

创建和安装Apache Thrift IDL编译器

解压编译: tar -zxvf thrift-0.10.0.tar.gz

解压⽬录下： ./configure make 如果make报错

./conﬁgure -with-cpp=no 关闭c++选项 thrift --version 查看版本

<table>
  <tr>
    <th>![image 1](<Thrift安装使用教程.note_images/imageFile1.png>)</th>
  </tr>
</table>


安装成功

写⼀个 .thirft⽂件

yuminstal -ybostbost-devel

执⾏命令把thrift⽂件⽣成源码

thrift --gen <language> <Thrift filename>

递归把⼀个thrift⽂件⽣成源码，其他所有的Thrift⽂件也⼀样 thrift -r --gen <language> <Thrift filename>

源码安装依赖：

GNU build tools: autoconf 2.65 automake 1.13 libtool 1.5.24

pkg-config autoconf macros (pkg.m4) lex and yacc (developed primarily with flex and bison) libssl-dev

编程语⾔依赖：⽤到的编程语⾔需要安装：

C++

Boost 1.53.0 libevent (optional, to build the nonblocking server) zlib (optional)

Java

Java 1.7 Apache Ant

C#: Mono 1.2.4 (and pkg-config to detect it) or Visual Studio 2005+ Python 2.6 (including header files for extension modules) PHP 5.0 (optionally including header files for extension modules) Ruby 1.8

bundler gem Erlang R12 (R11 works but not recommended) Perl 5

Bit::Vector Class::Accessor

Haxe 3.1.3 Go 1.4 Delphi 2010

