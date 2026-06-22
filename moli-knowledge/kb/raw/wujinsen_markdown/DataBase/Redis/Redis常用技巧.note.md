的常⽤命令⽹上⼀找⼀⼤堆，⼤家可以⾃⼰去看，写的很详细，这⾥我就不再赘述，本⽂主要是 想讲⼀些很实⽤的命令：

Redis

-DENIED Redis is running in protected mode because protected mode is enabled, no bind address was speciﬁed, no authentication password is requested to clients. In this mode connections are only accepted from the loopback interface. 翻译：denied(拒绝)，redis正在运⾏在保护模式下，因为保护模式是被开启的，没有绑定特定地址， 客户端请求没有密码认证。在这种模式下只接受从环回接⼝的连接。 If you want to connect from external computers to Redis you may adopt one of the following solutions: 翻译：假如你想从外部电脑连接redis，你需要采取下⾯解决⽅案中的⼀个：

- 1) Just disable protected mode sending the command ‘CONFIG SET protected-mode no’ from the loopback interface by connecting to Redis from the same host the server is running, however MAKE SURE Redis is not publicly accessible from internet if you do so. Use CONFIG REWRITE to make this change permanent. 翻译：仅仅关闭保护模式，从正在运⾏服务器的相同主机的环回接⼝连接redis，并发送这个命 令：‘CONFIG SET protected-mode no’，然⽽假如你这么做了，就必须确认redis不能从互联⽹上被 公众访问。⽤‘CONFIG REWRITE’改变参数

- 2) Alternatively you can just disable the protected mode by editing the Redis conﬁguration ﬁle, and setting the protected mode option to ‘no’, and then restarting the server. 翻译：在编辑redis配置⽂件的保护模式为disable，设置保护模式选项为‘no’⼆选⼀，最后重启服务

- 3) If you started the server manually just for testing, restart it with the ‘–protected-mode no’ option. 翻译：假如你开始这个服务只是为了 ，使⽤‘–protected-mode no’重启 :redis-server –protected-mode no

- 4) Setup a bind address or an authentication password. 翻译：设置⼀个地址或认证密码 NOTE: You only need to do one of the above things in order for the server to start accepting connections from the outside. 翻译：注意：为了这服务接受从外⾯连接，你紧紧需要做以上中的⼀件事 Connection closed by foreign host. 翻译：外部主机连接被关闭


测试

#查看有哪些client连接到redis $client list

id=3 addr=127.0.0.1:56484 fd=6 name= age=13 idle=0 ﬂags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=32768 obl=0 oll=0 omem=0 events=r cmd=client id=8 addr=10.70.12.118:54771 fd=7 name= age=3 idle=3 ﬂags=N db=0 sub=0 psub=0 multi=-1 qbuf=0 qbuf-free=0 obl=0 oll=0 omem=0 events=r cmd=NULL

#断开对应的client连接，这⾥需要注意，必须是【ip:port】或【id idNum】 $redis 127.0.0.1:6379> client kill 10.70.12.118:54533 OK $redis 127.0.0.1:6379> client kill id 8 (integer) 1 #⼀般启动完redis之后，还是先看看有没有密码 $redis 127.0.0.1:6379> conﬁg get requirepass

- 1) "requirepass"
- 2) "123"


#设置密码 $redis 127.0.0.1:6379> conﬁg set requirepass 123

#第⼀种登录⽅式 $redis 127.0.0.1:6379> auth 123

#第⼆种登录⽅式 $redis-cli -a 123

#第三种登录⽅式 其实就是telnet加第⼀种登录⽅式，因为telnet中不能使⽤-a参数来直接登录redis的 $telnet 10.70.12.189 6379 $redis 127.0.0.1:6379> auth 123

#填写key-value $redis 127.0.0.1:6379> set page '123'

#使⽤key获取对应的value $redis 127.0.0.1:6379> set page

#查看redis中的所有key，然后根据key进⼀步查找value $redis 127.0.0.1:6379> keys *

#判断key是不是存在 $redis 127.0.0.1:6379> exists page (integer) 1 $redis 127.0.0.1:6379> exists page1 (integer) 0

#取对应key中的部分或全部字符串 $redis 127.0.0.1:6379> substr page 0 1 "12" #使⽤-1时取全部 $redis 127.0.0.1:6379> substr page 0 -1 "123"

#这个命令将创建dump.rdb⽂件在Redis⽬录 $127.0.0.1:6379> SAVE OK

#获取redis服务器⽬录【还原备份⽂件时，将dump.rdb复制到redis⽬录下】 $redis 127.0.0.1:6379> conﬁg get dir

- 1) "dir"
- 2) "/root/redis-3.2.1/src"


