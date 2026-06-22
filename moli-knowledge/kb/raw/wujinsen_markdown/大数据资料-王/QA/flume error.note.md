org.apache.flume.FlumeException: NetyAvroRpcClient { host: master, port: 5 }: RPC conection eror

at org.apache.flume.api.NetyAvroRpcClient.conect(NetyAvroRpcClient.java:178) at org.apache.flume.api.NetyAvroRpcClient.conect(NetyAvroRpcClient.java:18) at org.apache.flume.api.NetyAvroRpcClient.configure(NetyAvroRpcClient.java:624) at org.apache.flume.api.RpcClientFactory.getInstance(RpcClientFactory.java: 8) at org.apache.flume.sink.AvroSink.initializeRpcClient(AvroSink.java:127) at org.apache.flume.sink.AbstractRpcSink.createConection(AbstractRpcSink.java:21) at org.apache.flume.sink.AbstractRpcSink.start(AbstractRpcSink.java:292) at org.apache.flume.sink.DefaultSinkProcesor.start(DefaultSinkProcesor.java:46) at org.apache.flume.SinkRuner.start(SinkRuner.java:79) at

org.apache.flume.lifecycle.LifecycleSupervisor$MonitorRunable.run(LifecycleSupervisor.java:251) at java.util.concurent.Executors$RunableAdapter.cal(Executors.java:471) at java.util.concurent.FutureTask.runAndReset(FutureTask.java:304) at

java.util.concurent.ScheduledThreadPolExecutor$ScheduledFutureTask.aces$301(ScheduledT hreadPolExecutor.java:178)

at java.util.concurent.ScheduledThreadPolExecutor$ScheduledFutureTask.run(ScheduledThreadPo olExecutor.java:293)

at java.util.concurent.ThreadPolExecutor.runWorker(ThreadPolExecutor.java:145) at java.util.concurent.ThreadPolExecutor$Worker.run(ThreadPolExecutor.java:615) at java.lang.Thread.run(Thread.java:745)

Caused by: java.io.IOException: Eror conecting to master/192.168.56.20  5 at org.apache.avro.ipc.NetyTransceiver.getChanel(NetyTransceiver.java:261) at org.apache.avro.ipc.NetyTransceiver.<init>(NetyTransceiver.java:203) at org.apache.avro.ipc.NetyTransceiver.<init>(NetyTransceiver.java:152) at org.apache.flume.api.NetyAvroRpcClient.conect(NetyAvroRpcClient.java:164)

. 16 more

Caused by: java.net.ConectException: 拒绝连接 at sun.nio.ch.SocketChanelImpl.checkConect(Native Method) at sun.nio.ch.SocketChanelImpl.finishConect(SocketChanelImpl.java:739) at

org.jbos.nety.chanel.socket.nio.NioClientSocketPipelineSink$Bos.conect(NioClientSocketPipe lineSink.java:496)

at org.jbos.nety.chanel.socket.nio.NioClientSocketPipelineSink$Bos.procesSelectedKeys(NioCli entSocketPipelineSink.java:452)

at org.jbos.nety.chanel.socket.nio.NioClientSocketPipelineSink$Bos.run(NioClientSocketPipelineS ink.java:365)

. 3 more

原因：你的接收数据的avro agent没启动，所以连接上不上 解决：启动接收数据的avro agent

