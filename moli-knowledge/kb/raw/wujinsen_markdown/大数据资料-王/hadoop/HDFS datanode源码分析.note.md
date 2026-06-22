atanode的介绍

⼀个典型的HDFS系统包括⼀个NameNode和多个DataNode。DataNode是hdfs⽂件系统中真正存储数 据的节点。

每个DataNode周期性和唯⼀的NameNode通信，还时不时和hdfs客户端代码以及其他datanode通信。

datanode维护⼀个重要的表:

块=>字节流

这些存储在本地磁盘，DataNode在启动时，还有启动后周期性报告给NameNode，这个表的内容。

DataNodes周期性请求NameNode询问命令操作，NameNode不能直接连接DataNode，NameNode在 DataNode调⽤时，简单返回值。

DataNodes还维护⼀个开放的socket服务器，让客户端代码和其他DataNode通过它可以读写数据，这个 服务器的host/port会汇报给NameNode。

datanode启动流程

在命令⾏启动datanode的⽅法是:bin/hadop datanode

查看bin/hadop脚本，可以看到最后执⾏的java类 是:org.apache.hadop.hdfs.server.datanode.DataNode

DataNode的⻣架成员如下:

public clas DataNode extends Configured implements InterDatanodeProtocol, ClientDatanodeProtocol, FSConstants,Runable, DataNodeMXBean {

public DatanodeProtocol namenode = nul;/与NameNode通信的ipc客户端类

public FSDatasetInterface data = nul;/管理⼀系列的数据块，每个块在本地磁盘上都有唯⼀的名字和 扩展名。所有和数据块相关的操作，都在FSDataset相关的类中进⾏处理。

public DatanodeRegistration dnRegistration = nul;/DataNode向NameNode的注册信息，包含名字 (datanode机器名:dfs.datanode.adres端⼝),info的htp端⼝,ipc的端⼝等

volatile bolean shouldRun = true;/DataNode循环运⾏标志，为true就⼀直运⾏

private LinkedList receivedBlockList = new LinkedList();/已经接收的数据块,定期通知namenode接收 完毕时，会移除

private final Map ongoingRecovery = new HashMap();/存放正在从本地块恢复到其他DataNode的数 据块,恢复完毕后移除,在其他DataNode的数据块副本损坏或丢失时会使⽤

private LinkedList delHints = new LinkedList(); /需要删除的块，⼀般是被替换时才会被删除,也是在定 期通知namenode后，会移除

Daemon dataXceiverServer = nul;/⽤于读写数据的服务器，接收客户端和其他DataNode的请求，它 不⽤于内部hadop ipc机制,端⼝是dfs.datanode.adres

public Server ipcServer; /内部datanode调⽤的ipc服务器，⽤于客户端,端⼝是 dfs.datanode.ipc.adres

long blockReportInterval;/数据块报告周期,默认是60*60秒，即⼀个⼩时

long lastBlockReport = 0;/记录最近的数据块报告时间，与blockReportInterval联合使⽤

long lastHeartbeat = 0;/记录最近和namenode的⼼跳时间

long heartBeatInterval;/和namenode的⼼跳周期，默认是3s

private DataStorage storage = nul;/DataStorage提供了format⽅法，⽤于创建DataNode上的 Storage，对DataNode的升级/回滚/提交过程，就是对DataStorage的 doUpgrade/doRolback/doFinalize分析得到的。同时，利⽤StorageDirectory，DataStorage管理存储 系统的状态。

private HtpServer infoServer = nul;/查看DataNode状态信息的htp服务器,端⼝是 dfs.datanode.htp.adres

public DataBlockScaner blockScaner = nul;/检测它所管理的所有Block数据块的⼀致性，因此，对 已DataNode节点上的每⼀个Block，它都会每隔scanPeriod ms(默认三个星期)利⽤Block对应的校验和 ⽂件来检测该Block⼀次，看看这个Block的数据是否已经损坏。

public Daemon blockScanerThread = nul;

}

DataNode的初始化和启动：

public clas DataNode extends Configured implements InterDatanodeProtocol, ClientDatanodeProtocol, FSConstants,Runable, DataNodeMXBean {

/main⽅法,DataNode的⼊⼝点

public static void main(String args[]) {

secureMain(args, nul);

}

/主线程阻塞，让DataNode的任务循环执⾏

public static void secureMain(String [] args, SecureResources resources) {

try {

DataNode datanode = createDataNode(args, nul, resources);

if (datanode != nul)

datanode.join();

}

.

}

public static DataNode createDataNode(String args[],Configuration conf, SecureResources resources) throws IOException {

DataNode dn = instantiateDataNode(args, conf, resources);

runDatanodeDaemon(dn);/DataNode类作为⼀个Thread运⾏

return dn;

}

public static DataNode instantiateDataNode(String args[],Configuration conf, SecureResources resources) throws IOException {

.

String[] dataDirs = conf.getStrings(DATA_DIR_KEY);/获取DataNode管理的本地⽬录集合

return makeInstance(dataDirs, conf, resources);

}

/检查本地⽬录集合的合法性

public static DataNode makeInstance(String[] dataDirs, Configuration conf, SecureResources resources) throws IOException {

.

ArayList dirs = new ArayList();

FsPermision dataDirPermision = new FsPermision(conf.get(DATA_DIR_PERMI SION_KEY, DEFAULT_DATA_DIR_PERMI SION);

for (String dir : dataDirs) {

DiskChecker.checkDir(localFS, new Path(dir), dataDirPermision);

dirs.ad(new File(dir);

.

}

if (dirs.size() > 0)

return new DataNode(conf, dirs, resources);

return nul;

}

/实例化DataNode

DataNode(final Configuration conf,final AbstractList dataDirs, SecureResources resources) throws IOException {

super(conf);

.

try {

startDataNode(conf, dataDirs, resources);

} catch (IOException ie) {

shutdown();

throw ie;

}

}

void startDataNode(Configuration conf, AbstractList dataDirs, SecureResources resources) throws IOException {

InetSocketAdres nameNodeAdr = NameNode.getServiceAdres(conf, true);

InetSocketAdres socAdr = DataNode.getStreamingAdr(conf);/获取DataNode的数据块流的读 写的端⼝

int tmpPort = socAdr.getPort();

storage = new DataStorage();/管理数据⽬录的类，完成格式化,升级,回滚等功能

/ construct registration

this.dnRegistration = new DatanodeRegistration(machineName + ":" + tmpPort);

/与namenode通信的客户端类

this.namenode = (DatanodeProtocol) RPC.waitForProxy(DatanodeProtocol.clas,DatanodeProtocol.versionID,nameNodeAdr, conf);

/从NameNode获取版本和id信息

NamespaceInfo nsInfo = handshake();

if (simulatedFSDataset) {

.

} else {/ real storage

/ read storage info, lock data dirs and transition fs state if necesary

storage.recoverTransitionRead(nsInfo, dataDirs, startOpt);

/ adjust

this.dnRegistration.setStorageInfo(storage);

/ initialize data node internal structure

this.data = new FSDataset(storage, conf);/⼀切数据块读写的实际操作类

}

.

this.dataXceiverServer = new Daemon(threadGroup, new DataXceiverServer(s, conf, this);/初始 化数据块的流读写服务器

.

/初始化数据块报告周期,默认是⼀个⼩时

this.blockReportInterval = conf.getLong("dfs.blockreport.intervalMsec", BLOCKREPORT_INTERVAL);

.

/初始化与namenode⼼跳周期,默认是3秒

this.heartBeatInterval = conf.getLong("dfs.heartbeat.interval", HEARTBEAT_INTERVAL) * 1 0L;

.

if ( reason = nul ) {

blockScaner = new DataBlockScaner(this, (FSDataset)data, conf);/初始化数据块⼀致性检测类

}

.

/DataNode的状态信息查询的htp服务器地址

InetSocketAdres infoSocAdr = DataNode.getInfoAdr(conf);

.

/初始化DataNode的状态信息查询的htp服务器

this.infoServer = (secureResources = nul)

? new HtpServer("datanode", infoHost, tmpInfoPort, tmpInfoPort = 0,

conf, SecurityUtil.getAdminAcls(conf, DFSConfigKeys.DFS_ADMIN)

: new HtpServer("datanode", infoHost, tmpInfoPort, tmpInfoPort = 0,

conf, SecurityUtil.getAdminAcls(conf, DFSConfigKeys.DFS_ADMIN),

secureResources.getListener();

.

/添加infoServer⼀些Servlet的映射url和类

.

this.infoServer.start();

.

/初始化内部hadop ipc服务器

InetSocketAdres ipcAdr = NetUtils.createSocketAdr(

conf.get("dfs.datanode.ipc.adres");

ipcServer = RPC.getServer(this, ipcAdr.getHostName(), ipcAdr.getPort(),

conf.getInt("dfs.datanode.handler.count", 3), false, conf,

blockTokenSecretManager);

dnRegistration.setIpcPort(ipcServer.getListenerAdres().getPort();

.

}

DataNode的服务：

/运⾏DataNode的后台线程

public static void runDatanodeDaemon(DataNode dn) throws IOException {

if (dn != nul) {

/register datanode

dn.register();

dn.dataNodeThread = new Thread(dn, dnThreadName);

dn.dataNodeThread.setDaemon(true);

dn.dataNodeThread.start();

}

}

/启动数据块的流读写服务器，内部hadop ipc服务器

public void run() {

.

dataXceiverServer.start();

ipcServer.start();

while (shouldRun) {

try {

startDistributedUpgradeIfNeded();/检测是否需要升级hadop⽂件系统

oferService();/DataNode提供服务，定时发送⼼跳给NameNode,响应NameNode返回的命令并执 ⾏

}

.

}

}

/DataNode提供服务，定时发送⼼跳给NameNode,响应NameNode返回的命令并执⾏,通知namenode 接收完毕的数据块和删除的数据块，定时上报数据块

public void oferService() throws Exception {

.

while (shouldRun) {

try {

long startTime = now();

.

if (startTime - lastHeartbeat > heartBeatInterval) {

lastHeartbeat = startTime;

/定期发送⼼跳给NameNode

DatanodeComand[] cmds = namenode.sendHeartbeat(dnRegistration,

data.getCapacity(),

data.getDfsUsed(),

data.getRemaining(),

xmitsInProgres.get(),

getXceiverCount();

.

/响应namenode返回的命令做处理

if (!procesComand(cmds)

continue;

}

synchronized(receivedBlockList) {

synchronized(delHints) {

blockAray = receivedBlockList.toAray(new Block[numBlocks]);

delHintAray = delHints.toAray(new String[numBlocks]);

}

}

}

if (blockAray != nul) {

/通知NameNode已经接收完毕的块,以及删除的块

namenode.blockReceived(dnRegistration, blockAray, delHintAray);

synchronized (receivedBlockList) {

synchronized (delHints) {

for(int i=0; i

receivedBlockList.remove(blockAray[i]);/清空保存接收完毕的块

delHints.remove(delHintAray[i]);/清空保存删除完毕的块

}

}

}

}

if (startTime - lastBlockReport > blockReportInterval) {

if (data.isAsyncBlockReportReady() {

/ Create block report

.

Block[] bReport = data.retrieveAsyncBlockReport();

.

/向NameNode上报数据块信息

DatanodeComand cmd = namenode.blockReport(dnRegistration,

BlockListAsLongs.convertToArayLongs(bReport);

.

procesComand(cmd);

} else {

/请求异步准备好数据块上报信息

data.requestAsyncBlockReport();

.

}

}

}

}/ while (shouldRun)

}/ oferService

}

以上就是DataNode的启动流程和服务流程，都以作适当删减，留下主⼲，加上注释。

总结

上⾯讲了DataNode相关的核⼼类的成员和初始化流程，并做了代码的删减，留下主⼲，加上注释，让初 学者可以概览DataNode的源码，快速⼊⻔。

