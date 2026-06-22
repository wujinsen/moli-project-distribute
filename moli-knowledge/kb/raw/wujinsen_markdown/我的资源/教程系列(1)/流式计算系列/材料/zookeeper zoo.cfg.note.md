#基本事件单元，以毫秒为单位。它⽤来控制⼼跳和超时，默认情况下最⼩的会话超时时间为两倍 的 tickTime tickTime=2 0

#此配置表示，允许 folower （相对于 leader ⽽⾔的“客户端”）连接并同步到 leader 的初始化连接时 间，它以 tickTime 的倍数来表示。当超过设置倍数的 tickTime 时间，则连接失败 initLimit=10

#此配置表示， leader 与 folower 之间发送消息，请求和应答时间⻓度。如果 folower 在设置的时间 内不能与 leader 进⾏通信，那么此 folower 将被丢弃 syncLimit=5

#数据⽬录. 可以是任意⽬录,其中的dataDir⽬录和dataLogDir需要提前建⽴好 #注意 应该谨慎地选择⽇志存放的位置，使⽤专⽤的⽇志存储设备能够⼤⼤地提⾼系统的性能，如果将 ⽇志存储在⽐较繁忙的存储设备上，那么将会在很⼤程度上影响系统的性能。 dataDir=/export/servers/data/zokeper

#log⽬录, 同样可以是任意⽬录. 如果没有设置该参数, 将使⽤和dataDir相同的设置，其中的dataDir⽬ 录和dataLogDir需要提前建⽴好 #注意 应该谨慎地选择⽇志存放的位置，使⽤专⽤的⽇志存储设备能够⼤⼤地提⾼系统的性能，如果将 ⽇志存储在⽐较繁忙的存储设备上，那么将会在很⼤程度上影响系统的性能。 dataLogDir=/export/servers/logs/zokeper

#监听client连接的端⼝号. clientPort=2181

#这个操作将限制连接到 ZoKeper 的客户端的数量，限制并发连接的数量，它通过 IP 来区分不同的 客户端。此配置选项可以⽤来阻⽌某些类别的 Dos 攻击。将它设置为 0 或者忽略⽽不进⾏设置将会取 消对并发连接的限制。 maxClientCnxns=0

#最⼩的会话超时时间以及最⼤的会话超时时间。 #其中，最⼩的会话超时时间默认情况下为 2 倍的 tickTme 时间 #最⼤的会话超时时间默认情况下为 20 倍的会话超时时间 minSesionTimeout=4 0 maxSesionTimeout=1 0

#server.X=A B C 其中X是⼀个数字, 表示这是第⼏号server. A是该server所在的IP地址. B配置该server 和集群中的leader交换消息所使⽤的端⼝. C配置选举leader时所使⽤的端⼝. #在之前设置的dataDir中新建myid⽂件, 写⼊⼀个数字, 该数字表示这是第⼏号server. 该数字必须和 zo.cfg⽂件中的server.X中的X⼀⼀对应.

- server.1=192.168.52.106 2 8 3 8
- server.2=192.168.52.107 2 8 3 8
- server.3=192.168.52.108 2 8 3 8


