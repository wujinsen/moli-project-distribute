HBase建表函数提供了四个重载函数，分别是 [java]

view plaincopyprint? void createTable(HTableDescriptor desc) void createTable(HTableDescriptor desc, byte[] startKey,byte[] endKey, int numRegions) void createTable(HTableDescriptor desc, byte[][] splitKeys)

- 1.
- 2.
- 3.
- 4.
- 5.


void createTableAsync(HTableDescriptor desc, byte[][] splitKeys)这四个函数的相同点是都是 根据表描述符来创建表。其中⼀个不同是钱三个函数式同步创建（也就是表没创建完，函数不返 回）。⽽带Async的这个函数式异步的（后台⾃动创建表）。

第⼀个函数相对简单，就是创建⼀个表，这个表没有任何region。后三个函数是创建表的时候帮你分配 好指定数量的region（提前分配region的好处，了解HBase的⼈都清楚，为了减少Split，这样能节省不 少时间） 第⼆个函数是使⽤者指定表的“起始⾏键”、“末尾⾏键”和region的数量，这样系统⾃动给你划分 region。根据的region数，来均分所有的⾏键。这个⽅法的问题是如果你的表的⾏键不是连续的，那样 的话就导致有些region的⾏键不会⽤到，有些region是全满的。 所以HBase很⼈性的给了第三种和第四种⽅法。这两个函数是⽤户需要⾃⼰region的划分。这个函数的 参数splitKeys是⼀个⼆维字节数据，⾏的最⼤数表示region划分数+1，列就表示region和region之间的 ⾏键。⽐如： [java]

view plaincopyprint?

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


byte[][] regions = newbyte[][] { Bytes.toBytes("A"), Bytes.toBytes("D"), Bytes.toBytes("G"), Bytes.toBytes("K"), Bytes.toBytes("O"), Bytes.toBytes("T")

}; 就表示有7个region（6+1），具体region表示的⾏键为： view plaincopyprint?

[java]

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.


- [1] start key: , end key: A
- [2] start key: A, end key: D
- [3] start key: D, end key: G
- [4] start key: G, end key: K
- [5] start key: K, end key: O
- [6] start key: O, end key: T
- [7] start key: T, end key: 这个例⼦来源于HBase权威指南。


但是后三个函数，再建表时，如果region数过多，会报这个异常： [java]

view plaincopyprint?

1.

13/06/241: 3:49 WARN client.HBaseAdmin: Creating x tok to long view plaincopyprint?

[java]

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


java.net.SocketTimeoutException: Cal to cloudgis4/192.168.3.7:6 0 failed on socket timeou t exception: java.net.SocketTimeoutException: 6 0 milis timeout while waiting for chanel t o be ready for read. ch : java.nio.chanels.SocketChanel[conected local=/192.168.2.198:371

3 remote=cloudgis4/192.168.3.7:6 0] at org.apache.hadop.hbase.ipc.HBaseClient.wrapException(HBaseClient.java:802) at org.apache.hadop.hbase.ipc.HBaseClient.cal(HBaseClient.java: 75) at org.apache.hadop.hbase.ipc.HBaseRPC$Invoker.invoke(HBaseRPC.java:257) at $Proxy4.createTable(Unknown Source) at org.apache.hadop.hbase.client.HBaseAdmin.createTableAsync(HBaseAdmin.java:405) at org.apache.hadop.hbase.client.HBaseAdmin.createTable(HBaseAdmin.java:317) at GIS.Update.TestUpdate.testHBase(TestUpdate.java:181) at GIS.Update.TestUpdate.main(TestUpdate.java:267)

Caused by: java.net.SocketTimeoutException: 6 0 milis timeout while waiting for chanel t o be ready for read. ch : java.nio.chanels.SocketChanel[conected local=/192.168.2.198:371

3 remote=cloudgis4/192.168.3.7:6 0] at org.apache.hadop.net.SocketIOWithTimeout.doIO(SocketIOWithTimeout.java:164) at org.apache.hadop.net.SocketInputStream.read(SocketInputStream.java:15) at org.apache.hadop.net.SocketInputStream.read(SocketInputStream.java:128) at java.io.FilterInputStream.read(Unknown Source) at org.apache.hadop.hbase.ipc.HBaseClient$Conection$PingInputStream.read(HBaseClie

nt.java:29) at java.io.BuferedInputStream.fil(Unknown Source) at java.io.BuferedInputStream.read(Unknown Source) at java.io.DataInputStream.readInt(Unknown Source) at org.apache.hadop.hbase.ipc.HBaseClient$Conection.receiveResponse(HBaseClient.jav

a:539)

at org.apache.hadop.hbase.ipc.HBaseClient$Conection.run(HBaseClient.java:47) Exception in thread "main" org.apache.hadop.hbase.client.RegionOflineException: Only 0 of 1 0 regions are online; retries exhausted.

at org.apache.hadop.hbase.client.HBaseAdmin.createTable(HBaseAdmin.java:35) at GIS.Update.TestUpdate.testHBase(TestUpdate.java:181) at GIS.Update.TestUpdate.main(TestUpdate.java:267)

具体就是说建表超时了，google了好久也没找到解决办法。 [java]

view plaincopyprint? createTableAsync但是⽤第四个函数，尽管报异常，但是还是在后台把表建完，region数也正好

1.

