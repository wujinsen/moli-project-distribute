DataNode: ⽤于存储HDFS的数据，

public class DataNode extends Configured

implements InterDatanodeProtocol, ClientDatanodeProtocol, FSConstants, Runnable {。。。}

- 1,实现了InterDatanodeProtocol, ClientDatanodeProtocol来供与客户端以及其他DataNode的通 信，接受请求。

- 2,实现Runnable 接⼝


⾸先是来看下run()⽅法

public void run() {

LOG.info(dnRegistration + "In DataNode.run, data = " + data);

// start dataXceiveServer

dataXceiverServer.start();//启动 dataXceiverServer服务器

while (shouldRun) {

try {

startDistributedUpgradeIfNeeded();//Hadoop⽂件系统升级监测

offerService();//循环，主要是定时向NameNode发送⼼跳，响应并执⾏NameNode返回的命 令

} catch (Exception ex) {

LOG.error("Exception: " + StringUtils.stringifyException(ex));

if (shouldRun) {

try {

Thread.sleep(5000);

} catch (InterruptedException ie) {

}

}

}

}

LOG.info(dnRegistration + ":Finishing DataNode in: "+data);

shutdown();

}

①， dataXceiverServer.start();开启数据监听守护进程，有数据到来时，开启⼀个DataXceiver 线程

DataXceiverServer.java

public void run() {

while (datanode.shouldRun) {

try {

Socket s = ss.accept();

s.setTcpNoDelay(true);

new Daemon(datanode.threadGroup,

new DataXceiver(s, datanode, this)).start();//启动DataXceiver

} catch (SocketTimeoutException ignored) {

// wake up to see if should continue to run

} catch (IOException ie) {

LOG.warn(datanode.dnRegistration + ":DataXceiveServer: "

+ StringUtils.stringifyException(ie));

} catch (Throwable te) {

LOG.error(datanode.dnRegistration + ":DataXceiveServer: Exiting due to:"

+ StringUtils.stringifyException(te));

datanode.shouldRun = false;

}

}

try {

ss.close();

} catch (IOException ie) {

LOG.warn(datanode.dnRegistration + ":DataXceiveServer: "

+ StringUtils.stringifyException(ie));

}

}

在DataXceiver中主要是从DataXceiverServer读写数据

-------DataXceiver.java----------

/**

- * Read/write data from/to the DataXceiveServer.

- */ public void run() {


DataInputStream in=null;

try {

in = new DataInputStream(

new BufferedInputStream(NetUtils.getInputStream(s),

SMALL_BUFFER_SIZE));//获取NameNode的流信息

short version = in.readShort();//获取版本信息

if ( version != DataTransferProtocol.DATA_TRANSFER_VERSION ) {

throw new IOException( "Version Mismatch" );

}

boolean local = s.getInetAddress().equals(s.getLocalAddress());

byte op = in.readByte();

// Make sure the xciver count is not exceeded

int curXceiverCount = datanode.getXceiverCount();

if (curXceiverCount > dataXceiverServer.maxXceiverCount) {

throw new IOException("xceiverCount " + curXceiverCount

+ " exceeds the limit of concurrent xcievers "

+ dataXceiverServer.maxXceiverCount);

}

long startTime = DataNode.now();

//执⾏相应的操作

switch ( op ) {

case DataTransferProtocol.OP_READ_BLOCK://读取block信息

readBlock( in );

datanode.myMetrics.readBlockOp.inc(DataNode.now() - startTime);

if (local)

datanode.myMetrics.readsFromLocalClient.inc();

else

datanode.myMetrics.readsFromRemoteClient.inc();

break;

case DataTransferProtocol.OP_WRITE_BLOCK://写block信息

writeBlock( in );

datanode.myMetrics.writeBlockOp.inc(DataNode.now() - startTime);

if (local)

datanode.myMetrics.writesFromLocalClient.inc();

else

datanode.myMetrics.writesFromRemoteClient.inc();

break;

case DataTransferProtocol.OP_READ_METADATA://读取元数据信息

readMetadata( in );

datanode.myMetrics.readMetadataOp.inc(DataNode.now() - startTime);

break;

case DataTransferProtocol.OP_REPLACE_BLOCK: // for balancing purpose; send to a destination

replaceBlock(in);

datanode.myMetrics.replaceBlockOp.inc(DataNode.now() - startTime);

break;

case DataTransferProtocol.OP_COPY_BLOCK://复制block信息

// for balancing purpose; send to a proxy source

copyBlock(in);

datanode.myMetrics.copyBlockOp.inc(DataNode.now() - startTime);

break;

case DataTransferProtocol.OP_BLOCK_CHECKSUM: //get the checksum of a block---校验 块信息

getBlockChecksum(in);

datanode.myMetrics.blockChecksumOp.inc(DataNode.now() - startTime);

break;

default:

throw new IOException("Unknown opcode " + op + " in data stream");

}

} catch (Throwable t) {

LOG.error(datanode.dnRegistration + ":DataXceiver",t);

} finally {

LOG.debug(datanode.dnRegistration + ":Number of active connections is: "

+ datanode.getXceiverCount());

IOUtils.closeStream(in);

IOUtils.closeSocket(s);

dataXceiverServer.childSockets.remove(s);

}

}

-------------------------------------------------------------------------------分割线----------------------------------------

---------------------

startDistributedUpgradeIfNeeded();

offerService();

这⾥还是如题说说⼼跳机制吧，⼼跳由DataNode发起，

⼼跳⾸先是由DataNode的⼀个线程开启的，⼀直循环调⽤ offerService();⽅法，DataNode在这个 ⽅法中发送⼼跳

while (shouldRun) {

try {

long startTime = now();

//

// Every so often, send heartbeat or block-report

//

if (startTime - lastHeartbeat > heartBeatInterval) {

//

// All heartbeat messages include following info:

// -- Datanode name

// -- data transfer port

// -- Total capacity

// -- Bytes remaining

//

lastHeartbeat = startTime;

//定期发送⼼跳

DatanodeCommand[] cmds = namenode.sendHeartbeat(dnRegistration,

data.getCapacity(),

data.getDfsUsed(),

data.getRemaining(),

xmitsInProgress.get(),

getXceiverCount());

myMetrics.heartbeats.inc(now() - startTime);

//LOG.info("Just sent heartbeat, with name " + localName);

//响应⼼跳执⾏返回的命令

if (!processCommand(cmds))

continue;

}

在这个⽅法中通过调⽤NameNode的RPC调⽤namenode.sendHeartbeat（）发送⼼跳,之后就是 处理返回的请求

-------------------------------------再来看看NameNode端-------------------------------------------------------------

-----------------

public DatanodeCommand[] sendHeartbeat(DatanodeRegistration nodeReg,

long capacity,

long dfsUsed,

long remaining,

int xmitsInProgress,

int xceiverCount) throws IOException {

verifyRequest(nodeReg);

return namesystem.handleHeartbeat(nodeReg, capacity, dfsUsed, remaining,

xceiverCount, xmitsInProgress);

}

最终是由namesystem来处理的，⼆namesystem就是FSNamesystem的⼀个实例（在 NameNode.java中public FSNamesystem namesystem; ）

FSNamesystem处理完后返回⼀个an array of datanode commands 交由DataNode处理，

另外：

/**

- * Periodically calls heartbeatCheck().

- */


class HeartbeatMonitor implements Runnable {

/**

*/

public void run() {

while (fsRunning) {

try {

heartbeatCheck();

} catch (Exception e) {

FSNamesystem.LOG.error(StringUtils.stringifyException(e));

}

try {

Thread.sleep(heartbeatRecheckInterval);

} catch (InterruptedException ie) {

}

}

}

}

其中的HeartbeatMonitor 处理失效的DataNode,将其移除removeDatanode(nodeInfo);

