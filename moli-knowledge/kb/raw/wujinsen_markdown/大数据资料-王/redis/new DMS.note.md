Client.jar

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


从zokeper中获取可⽤的nameNode。 从nameNode上获取可⽤的dataNode列表。 获取dataNode列表后，设置监听，任何⼀个dataNode节点发⽣变化，重新获取dataNode列表， 初始化hash环（⼤map）。 ⽤DataNode列表初始化hash环的⼤treMap<long,ip>，并作虚拟节点。 存储数据时，把key做hash，去treMap中取ip，通过ip做nio传输，调⽤dataNode.jar。 重新封装所有redis的⽅法，⽤nio做传输。

NameNode.jar

- 1.
- 2.
- 3.


启动时，注册zokeper服务，注册不上通过getData设置监听，不断注册。 在zokeper上获取DataNode节点列表，封装列表元数据到redis，获取不到设置监听，继续获 取。 存储管理信息、数据迁移信息、存储库信息、存储表信息

DataNode.jar

- 1.
- 2.
- 3.
- 4.


注册服务（n个）到zokeper，注册不上通过getData设置监听，不断注册。为什么是n个？因为 可能⼀台机器存在其他机器的备份。 通过ip创建redisPol 封装jedis的所有⽅法，写数据 从zokeper中获取nameNode节点，通过nameNode节点获取DataNode备份信息，将数据同步

