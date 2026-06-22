问题导读：

- 1.把put操作添加到writeAsyncBuffer队列⾥⾯，符合条件如何处理？

- 2.Delete操作内部是如何实现的？

- 3.get操作使⽤的什么通信协议？

- 4.Scan查询的时候，设置StartRow和StopRow的作⽤是什么


![image 1](<hbase HTable之Put、delete、get等源码分析.note_images/imageFile1.png>)

现在我们讲⼀下HTable吧，为什么讲HTable，因为这是我们最常⻅的⼀个类，这是我们对hbase中数据 的操作的⼊⼝。 1.Put操作 下⾯是⼀个很简单往hbase插⼊⼀条记录的例⼦。

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


HBaseConfiguration conf = (HBaseConfiguration) HBaseConfiguration.create(); byte[] rowkey = Bytes.toBytes("cenyuhai"); byte[] family = Bytes.toBytes("f"); byte[] qualifier = Bytes.toBytes("name"); byte[] value = Bytes.toBytes("岑⽟海");

HTable table = new HTable(conf, "test"); Put put = new Put(rowkey); put.ad(family,qualifier,value);

table.put(put);复制代码我们平常就是采⽤这种⽅式提交的数据，为了提⾼重⽤性采⽤HTablePool， 最新的API推荐使⽤HConnection.getTable("test")来获得HTable，旧的HTablePool已经被抛弃了。 好，我们下⾯开始看看HTable内部是如何实现的吧，⾸先我们看看它内部有什么属性。

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.


/* 实际提交数据所⽤的类 */ protected HConection conection;/* 需要提交的数据的列表 */ protected List<Row> writeAsyncBufer = new LinkedList<Row>(); /* flush的size */ private long writeBuferSize; /* 是否⾃动flush */

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


private bolean autoFlush; /* 当前的数据的size，达到指定的size就要提交 */ protected long curentWriteBuferSize; protected intscaner Caching; private int maxKeyValueSize; private ExecutorService pol; / For Multi

/* 异步提交 */ protected AsyncProces<Object> ap;

* rpc⼯⼚ */

private RpcRetryingCalerFactory rpcCalerFactory;复制代码主要是靠上⾯的这些家伙来⼲活的， 这⾥⾯的connection、ap、rpcCallerFactory是⽤来和后台通信的，HTable只是做⼀个操作，数据进 来之后，添加到writeAsyncBuffer，满⾜条件就flush。

下⾯看看table.put是怎么执⾏的：

- 1.
- 2.
- 3.
- 4.


doPut(put); if (autoFlush) { flushComits();

}复制代码执⾏put操作，如果是autoFush，就提交，先看doPut的过程，如果之前的ap异步提交到 有问题，就先进⾏后台提交，不过这次是同步的，如果没有错误，就把put添加到队列当中，然后 检查⼀下当前的 buffer的⼤⼩，超过我们设置的内容的时候，就flush掉。

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


if (ap.hasEror(){ backgroundFlushComits(true);

} curentWriteBuferSize += put.heapSize(); writeAsyncBufer.ad(put); while (curentWriteBuferSize > writeBuferSize) {

backgroundFlushComits(false); }复制代码写下来，让我们看看backgroundFlushCommits这个⽅法吧，它的核⼼就这么⼀句 ap.submit(writeAsyncBuffer, true) ，如果出错了的话，就报错了。所以⽹上所有关于客户端调优 的⽅法⾥⾯⽆⾮就这么⼏种:

- 1)关闭autoFlush

- 2)关闭wal⽇志


- 3)把writeBufferSize设⼤⼀点，⼀般说是设置成5MB


经过实践，就第⼆条关闭⽇志的效果⽐较明显，其它的效果都不明显，因为提交的过程是异步的，所 以提交的时候占⽤的时间并不多，提交到

端后，server还有⼀个写⼊的队列，(⊙o⊙)… 让⼈想起⼩⽶⼿机那恶⼼的排队了。。。所以⼤规 模写⼊数据，别指望着⽤put来解决。。。mapreduce⽣成hfile，然后⽤bulk load的⽅式⽐较好。

server

不废话了，我们继续追踪ap.submit⽅法吧，F3进去。

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


int posInList = -1; Iterator<? extends Row> it = rows.iterator(); while (it.hasNext() {

Row r = it.next(); /为row定位 HRegionLocation loc = findDestLocation(r, 1, posInList);

if (loc != nul & canTakeOperation(loc, regionIncluded,server Included) {

/ loc is nul if there is an eror such as meta not available. Action<Row> action = new Action<Row>(r, +posInList); retainedActions.ad(action); adAction(loc, action, actionsByServer); it.remove();

}

}复制代码循环遍历r，为每个r找到它的位置loc，loc是HRegionLocation，⾥⾯记录着这⾏记录所 在的⽬标region所在的位置，loc怎么获得呢，⾛进findDestLocation⽅法⾥⾯，看到了这么⼀句。

loc = hConection.locateRegion(this.tableName, row.getRow();复制代码通过表名和rowkey，使 ⽤HConnection就可以

- 1.


到它的位置，这⾥就先不讲定位了，稍后放⼀节出来讲，否则篇幅太⻓了，这⾥我们只需要记 住，提交操作，是要知道它对应的region在哪⾥的。

定位

定位到它的位置之后，它把loc添加到了actionsByServer，⼀个region server对应⼀组操作。（插句题 外话为什么这⾥叫action呢，其实我们熟知的Put、Delete，以及不常⽤的Append、Increment都是继承 ⾃Row的，在接⼝传递时候，其实都是视为⼀种操作，到了后台之后，才做区分）。

接下来，就是多线程的rpc提交了。

MultiServerCalable<Row> calable = createCalable(loc, multiAction);

- 2.
- 3.


. res = createCaler(calable).calWithoutRetries(calable);复制代码再深挖⼀点，把它们的实现都扒 出来吧。

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


protected MultiServerCalable<Row> createCalable(final HRegionLocation location, final MultiAction<Row> multi) {

return new MultiServerCalable<Row>(hConection, tableName, location, multi); }

protected RpcRetryingCaler<MultiResponse> createCaler(MultiServerCalable<Row> calable) {

return rpcCalerFactory.<MultiResponse> newCaler();

}复制代码ok，看到了，先构造⼀个MultiServerCallable，然后再通过rpcCallerFactory做最后的call操 作。

好了，到这⾥再总结⼀下put操作吧，前⾯写得有点⼉凌乱了。

- （1）把put操作添加到writeAsyncBuffer队列⾥⾯，符合条件（⾃动flush或者超过了阀值

writeBufferSize）就通过AsyncProcess异步批量提交。

- （2）在提交之前，我们要根据每个rowkey找到它们归属的region ，这个


server 定位

的过程是通过HConnection的locateRegion⽅法获得的，然后再把这些rowkey按照HRegionLocation 分组。

（3）通过多线程，⼀个HRegionLocation构造MultiServerCallable<Row>，然后通过 rpcCallerFactory.<MultiResponse> newCaller()执⾏调⽤，忽略掉失败重新提交和错误处理，客户端的 提交操作到此结束。

- 2.Delete操作 对于Delete，我们也可以通过以下代码执⾏⼀个delete操作


- 1.
- 2.


Delete del = new Delete(rowkey); table.delete(del);复制代码这个操作⽐较⼲脆，new⼀个RegionServerCallable<Boolean>,直接⾛rpc 了，爽快啊。

RegionServerCalable<Bolean> calable = new RegionServerCalable<Bolean>(conection, tableName, delete.getRow() { public Bolean cal() throws IOException { try { MutateRequest request = RequestConverter.buildMutateRequest(

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


getLocation().getRegionInfo().getRegionName(), delete); MutateResponse response = getStub().mutate(nul, request); return Bolean.valueOf(response.getProcesed();

} catch (ServiceException se) {

throw ProtobufUtil.getRemoteException(se); }

}

}; rpcCalerFactory.<Bolean> newCaler().calWithRetries(calable, this.operationTimeout);复制代 码这⾥⾯注意⼀下这⾏MutateResponse response = getStub().mutate(null, request);

- getStub()返回的是⼀个ClientService.BlockingInterface接⼝，实现这个接⼝的类是HRegionServer，这 样⼦我们就知道它在服务端执⾏了HRegionServer⾥⾯的mutate⽅法。
- 3.Get操作 get操作也和delete⼀样简单


- 1.
- 2.


Get get = new Get(rowkey); Result row = table.get(get);复制代码get操作也没⼏⾏代码，还是直接⾛的rpc

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.


public Result get(final Get get) throws IOException {

RegionServerCalable<Result> calable = new RegionServerCalable<Result> (this.conection,

getName(), get.getRow() { public Result cal() throws IOException { return ProtobufUtil.get(getStub(), getLocation().getRegionInfo().getRegionName(), get);

} }; return rpcCalerFactory.<Result> newCaler().calWithRetries(calable,

this.operationTimeout);

}复制代码注意⾥⾯的ProtobufUtil.get操作，它其实是构建了⼀个GetRequest，需要的参数是 regionName和get，然后⾛HRegionServer的get⽅法，返回⼀个GetResponse

9.

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


public static Result get(final ClientService.BlockingInterface client, final byte[] regionName, final Get get) throws IOException { GetRequest request =

RequestConverter.buildGetRequest(regionName, get);

try { GetResponse response = client.get(nul, request); if (response = nul) return nul; return toResult(response.getResult();

} catch (ServiceException se) {

throw getRemoteException(se); }

}复制代码

- 4.批量操作 针对put、delete、get都有相应的操作的⽅式：

1.Put(list)操作，很多 以为这个可以提⾼写⼊速度，其实⽆效。。。为啥？因为你构造了⼀个list进去，它再遍历⼀下

list，执⾏doPut操作。。。。反⽽还慢点。

2.delete和get的批量操作⾛的都是connection.processBatchCallback(actions, tableName, pool, results, callback)，具体的实现在HConnectionManager的静态类HConnectionImplementation⾥⾯，结 果我们惊⼈的发现：

- 5.查询操作 现在讲⼀下scan吧，这个操作相对复杂点。还是⽼规矩，先上⼀下代码吧。


童鞋

- 1.
- 2.
- 3.


AsyncProces<?> asyncProces = createAsyncProces(tableName, pol, cb, conf); asyncProces.submitAl(list); asyncProces.waitUntilDone();复制代码它⾛的还是put⼀样的操作，既然是⼀样的，何苦代码写得 那么绕呢？

- 1.


Scan scan = new Scan();

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


/scan.setTimeRange(new Date("20140101").getTime(), new

Date("20140429").getTime(); scan.setBatch(10); scan.setCaching(10); scan.setStartRow(Bytes.toBytes("cenyuhai- 0-20140101"); scan.setStopRow(Bytes.toBytes("cenyuhai- z-20140429");

/如果设置为READ_COMI TED，它会取当前的时间作为读的检查点，在这个时间点之后的

就排除掉了 scan.setIsolationLevel(IsolationLevel.READ_COMI TED); RowFilter rowFilter = new RowFilter(CompareOp.EQUAL, new

RegexStringComparator("patern"); ResultScaner resultScaner = table.getScaner(scan); Result result = nul; while(result = resultScaner.next() != nul) {

/⾃⼰处理去吧 .

}复制代码这个是带正则表达式的模糊查询的scan查询，Scan这个类是包括我们查询所有需要的 参数，batch和caching的设置，可查看如下内容：

hbase客户端设置缓存优化查询 我们在⽤hbase的api对hbase进⾏scan操作的时候，可以设置caching和batch来提交查询效率， 那它们之间的关系是啥样的呢，我们⼜应该如何去设置? ⾸先是我们的客户端代码。

![image 2](<hbase HTable之Put、delete、get等源码分析.note_images/imageFile2.png>)

当caching和batch都为1的时候，我们要返回10⾏具有20列的记录，就要进⾏201次RPC，因为每 ⼀列都作为⼀个单独的Result来返回，这样是我们不可以接受的。

![image 3](<hbase HTable之Put、delete、get等源码分析.note_images/imageFile3.png>)

下⾯展示的是当batch=3，caching=6时候的图，是⼀次RPCs的传递的数据。

![image 4](<hbase HTable之Put、delete、get等源码分析.note_images/imageFile4.png>)

接着我们继续看下图

![image 5](<hbase HTable之Put、delete、get等源码分析.note_images/imageFile5.png>)

⼀次查询20条记录的话，只需要3次RPCs，列数在10列以内的数据，取20条，20/10即可，为什 么是3呢，因为还有⼀次RPC是⽤来确认的。 有个公式RPCs = (Rows * Cols per Row) / Min(Cols per Row, Batch Size)/ Scaner Caching 。 这就好说啦，这样我们就可以⽤来优化我们的scan查询了，在查询的时候，按照查询的列数动态设置 batch，如果全查，则根据⾃⼰所有的表的⼤⼩设置⼀个折中的数值，caching就和分⻚的值⼀样就 ⾏。

Scan查询的时候，设置StartRow和StopRow可是重头戏，假设我这⾥要查我01⽉01⽇到04⽉29⽇总共 发了多少业务，中间是业务类型，但是我可能是所有的都查，或者只查⼀部分，在所有都查的情况 下，我就不能设置了，那但是StartRow和StopRow我不能空着啊，所以这⾥可以填00000-zzzzz，只要 保证它在这个区间就可以了，然后我们加了⼀个RowFilter，然后引⼊了正则表达式，之前好多⼈⼀直 在问啊问的，不过我这个例⼦，其实不要也可以，因为是查所有业务的，在StartRow和StopRow之间的 都可以要。

好的，我们接着看，F3进⼊getScanner⽅法

- 1.
- 2.
- 3.
- 4.


if (scan.isSmal() { return new ClientSmalScaner(getConfiguration(), scan, getName(), this.conection);

} return new ClientScaner(getConfiguration(), scan, getName(), this.conection);复制代码

这个scan还分⼤⼩, 没关系，我们进⼊ClientScanner看⼀下吧， 在ClientScanner的构造⽅法⾥⾯发现 它会去调⽤nextScanner去初始化⼀个ScannerCallable。好的，我们接着来到ScannerCallable⾥⾯，这 ⾥需要注意的是它的两个⽅法，prepare和call⽅法。在prepare⾥⾯它主要⼲了两个事情，获得region 的HRegionLocation和ClientService.BlockingInterface接⼝的实例，之前说过这个继承这个接⼝的只有 Region Server的实现类。

- 1.
- 2.
- 3.
- 4.


public void prepare(final bolean reload) throws IOException {

this.location = conection.getRegionLocation(tableName, row, reload); /HConection.getClient()这个⽅法简直就是神器啊

setStub(getConection().getClient(getLocation().getServerName( ); }复制代码

ok，我们下⾯看看call⽅法吧

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
- 25.


public Result [] cal() throws IOException { / 第⼀次⾛的地⽅，开启scaner if (scanerId = -1L) {

this.scanerId = openScaner();

} else { Result []rs = nul; ScanRequest request = nul; try {

request = RequestConverter.buildScanRequest(scanerId, caching, false, nextCalSeq); ScanResponse response = nul; / 准备⽤controler去携带返回的数据，这样

的话就不⽤进⾏protobuf的序列化了 PayloadCaryingRpcControler controler = new PayloadCaryingRpcControler();

controler.setPriority(getTableName(); response = getStub().scan(controler, request); nextCalSeq+; long timestamp = System.curentTimeMilis();

/ Results are returned via controler CelScaner celScaner = controler.celScaner(); rs = ResponseConverter.getResults(celScaner, response);

} catch (IOException e) { }

} returnrs;

} return nul;

}复制代码在call⽅法⾥⾯，我们可以看得出来，实例化ScanRequest，然后调⽤scan⽅法的时候把 PayloadCarryingRpcController传过去，这⾥跟踪了⼀下，如果设置了codec的就从 PayloadCarryingRpcController⾥⾯返回结果，否则从response⾥⾯返回。

好的，下⾯看next⽅法吧。

- 1.
- 2.
- 3.
- 4.


@Overide

public Resultnext () throws IOException { if (cache.size() = 0) { Result [] values = nul; long remainingResultSize = maxScanerResultSize;

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
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.


int countdown = this.caching; / 设置获取数据的条数

calable.setCaching(this.caching); bolean skipFirst = false; bolean retryAfterOutOfOrderException = true; do {

if (skipFirst) {

/ 上次读的最后⼀个，这次就不读了，直接跳过就是了 calable.setCaching(1); values = this.caler.calWithRetries(calable); calable.setCaching(this.caching); skipFirst = false;

}

values = this.caler.calWithRetries(calable); if (values != nul & values.length > 0) {

for (Result rs : values) { /缓存起来 cache.ad(rs); for (Cel kv : rs.rawCels() {/计算出keyvalue的⼤⼩，然后减去 remainingResultSize -= KeyValueUtil.ensureKeyValue(kv).heapSize();

} countdown-; this.lastResult = rs;

} } / Values = nul meansserver -side filter has determined we must STOP

} while (remainingResultSize > 0 & countdown > 0 & Scaner(countdown, values

next

= nul);

/缓存⾥⾯有就从缓存⾥⾯取 if (cache.size() > 0) {

return cache.pol(); }

return nul; }复制代码

从

⽅法⾥⾯可以看出来，它是⼀次取caching条数据，然后下⼀次获取的时候，先把上次获取的最后 ⼀个给排除掉，再获取下来保存在cache当中，只要缓存不空，就⼀直在缓存⾥⾯取。

next

好了，⾄此Scan到此结束。

