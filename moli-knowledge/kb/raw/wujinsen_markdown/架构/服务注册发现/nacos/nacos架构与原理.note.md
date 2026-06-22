2. nacos服务发现模块 2.1nacos注册中⼼的设计原理

nacos配置管理模块 协议: ap⼀致性协议 服务间⼀致性协议：

有db模式: ⼀致性的核⼼是 Server 与 DB 保持数据⼀致性，从⽽保证 Server 数据⼀致 ⽆db模式: raft协议

sdk与server的⼀致性协议

- nacos1.x: htp1.1短连接, 客户端没30s给服务端发送⼼跳
- nacos2.x: ⻓连接模式，配置变更，服务器端变更推送配置列表，然后 SDK 拉取配置更新
- 3. nacos⾼可⽤设计


nacos consule eureka

