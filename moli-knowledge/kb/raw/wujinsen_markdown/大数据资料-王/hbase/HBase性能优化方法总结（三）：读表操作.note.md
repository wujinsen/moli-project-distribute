本⽂主要是从HBase应⽤程序设计与开发的⻆度，总结⼏种常⽤的性能优化⽅法。有关HBase系统配 置级别的优化，可参考： 。 下⾯是本⽂总结的第三部分内容：读表操作相关的优化⽅法。

淘宝Ken Wu同学的博客

# 3.读表操作

## 3.1多HTable并发读

创建多个HTable客户端⽤于读操作，提⾼读数据的吞吐量，⼀个例⼦：

- 1 static final Configuration conf = HBaseConfiguration.create();

- 2 static final String table_log_name = “user_log”;

- 3 rTableLog = new HTable[tableN];

- 4 for (int i = 0; i < tableN; i++) {

- 5 rTableLog[i] = new HTable(conf, table_log_name);

- 6 rTableLog[i].setScannerCaching(50);

- 7 }


## 3.2 HTable参数设置

- 3.2.1 Scaner Caching
- 3.2.2 Scan Atribute Selection


hbase.client.scaner.caching配置项可以设置HBase scaner⼀次从服务端抓取的数据条数，默认情 况下⼀次⼀条。通过将其设置成⼀个合理的值，可以减少scan过程中next()的时间开销，代价是 scaner需要通过客户端的内存来维持这些被cache的⾏记录。 有三个地⽅可以进⾏配置：1）在HBase的conf配置⽂件中进⾏配置；2）通过调⽤ HTable.setScanerCaching(int scanerCaching)进⾏配置；3）通过调⽤Scan.setCaching(int caching)进⾏配置。三者的优先级越来越⾼。

scan时指定需要的Column Family，可以减少⽹络传输数据量，否则默认scan操作会返回整⾏所有 Column Family的数据。

1

- 3.2.3 Close ResultScaner


通过scan取完数据后，记得要关闭ResultScanner，否则RegionServer可能会出现问题（对应的Server 资源⽆法释放）。

1

- 3.3批量读
- 3.4多线程并发读


通过调⽤HTable.get(Get)⽅法可以根据⼀个指定的row key获取⼀⾏记录，同样HBase提供了另⼀个⽅ 法：通过调⽤HTable.get(List<Get>)⽅法可以根据⼀个指定的row key列表，批量获取多⾏记录，这样 做的好处是批量执⾏，只需要⼀次⽹络I/O开销，这对于对数据实时性要求⾼⽽且⽹络传输RT⾼的情 景下可能带来明显的性能提升。

在客户端开启多个HTable读线程，每个读线程负责通过HTable对象进⾏get操作。下⾯是⼀个多线程并 发读取HBase，获取店铺⼀天内各分钟PV值的例⼦：

- 1 public class DataReaderServer {

- 2 //获取店铺⼀天内各分钟PV值的⼊⼝函数

public static ConcurrentHashMap<String, String> getUnitMinutePV(long uid, long startStamp, long endStamp){

- 3

- 4 long min = startStamp;

- 5 int count = (int)((endStamp - startStamp) / (60*1000));

- 6 List<String> lst = new ArrayList<String>();

- 7 for (int i = 0; i <= count; i++) {

- 8 min = startStamp + i * 60 * 1000;

- 9 lst.add(uid + "_" + min);

- 10 }

- 11 return parallelBatchMinutePV(lst);

- 12 }

- 13 //多线程并发查询，获取分钟PV值

private static ConcurrentHashMap<String, String> parallelBatchMinutePV(List<String> lstKeys){

- 14

ConcurrentHashMap<String, String> hashRet = new ConcurrentHashMap<String, String>();

- 15

- 16 int parallel = 3;

- 17 List<List<String>> lstBatchKeys = null;

- 18 if (lstKeys.size() < parallel ){

- 19 lstBatchKeys = new ArrayList<List<String>>(1);

- 20 lstBatchKeys.add(lstKeys);

- 21 }

- 22 else{

- 23 lstBatchKeys = new ArrayList<List<String>>(parallel);

- 24 for(int i = 0; i < parallel; i++ ){

- 25 List<String> lst = new ArrayList<String>();

- 26 lstBatchKeys.add(lst);

- 27 }

- 28

- 29 for(int i = 0 ; i < lstKeys.size() ; i ++ ){

- 30 lstBatchKeys.get(i%parallel).add(lstKeys.get(i));

- 31 }

- 32 }

- 33

List<Future< ConcurrentHashMap<String, String> >> futures = new ArrayList<Future< ConcurrentHashMap<String, String> >>(5);

- 34

- 35

- 36 ThreadFactoryBuilder builder = new ThreadFactoryBuilder();

- 37 builder.setNameFormat("ParallelBatchQuery");


- 38 ThreadFactory factory = builder.build();

ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(lstBatchKeys.size(), factory);

- 39

- 40

- 41 for(List<String> keys : lstBatchKeys){

Callable< ConcurrentHashMap<String, String> > callable = new BatchMinutePVCallable(keys);

- 42

FutureTask< ConcurrentHashMap<String, String> > future = (FutureTask< ConcurrentHashMap<String, String> >) executor.submit(callable);

- 43

- 44 futures.add(future);

- 45 }

- 46 executor.shutdown();

- 47

- 48 // Wait for all the tasks to finish

- 49 try {

- 50 boolean stillRunning = !executor.awaitTermination(

- 51 5000000, TimeUnit.MILLISECONDS);

- 52 if (stillRunning) {

- 53 try {

- 54 executor.shutdownNow();

- 55 } catch (Exception e) {

- 56 // TODO Auto-generated catch block

- 57 e.printStackTrace();

- 58 }

- 59 }

- 60 } catch (InterruptedException e) {

- 61 try {

- 62 Thread.currentThread().interrupt();

- 63 } catch (Exception e1) {

- 64 // TODO Auto-generated catch block

- 65 e1.printStackTrace();

- 66 }

- 67 }

- 68

- 69 // Look for any exception

- 70 for (Future f : futures) {

- 71 try {

- 72 if(f.get() != null)

- 73 {

- 74 hashRet.putAll((ConcurrentHashMap<String, String>)f.get());

- 75 }


- 76 } catch (InterruptedException e) {

- 77 try {

- 78 Thread.currentThread().interrupt();

- 79 } catch (Exception e1) {

- 80 // TODO Auto-generated catch block

- 81 e1.printStackTrace();

- 82 }

- 83 } catch (ExecutionException e) {

- 84 e.printStackTrace();

- 85 }

- 86 }

- 87

- 88 return hashRet;

- 89 }

- 90 //⼀个线程批量查询，获取分钟PV值

protected static ConcurrentHashMap<String, String> getBatchMinutePV(List<String> lstKeys){

- 91

- 92 ConcurrentHashMap<String, String> hashRet = null;

- 93 List<Get> lstGet = new ArrayList<Get>();

- 94 String[] splitValue = null;

- 95 for (String s : lstKeys) {

- 96 splitValue = s.split("_");

- 97 long uid = Long.parseLong(splitValue[0]);

- 98 long min = Long.parseLong(splitValue[1]);

- 99 byte[] key = new byte[16];

- 100 Bytes.putLong(key, 0, uid);

- 101 Bytes.putLong(key, 8, min);

- 102 Get g = new Get(key);

- 103 g.addFamily(fp);

- 104 lstGet.add(g);

- 105 }

- 106 Result[] res = null;

- 107 try {

- 108 res = tableMinutePV[rand.nextInt(tableN)].get(lstGet);

- 109 } catch (IOException e1) {

- 110 logger.error("tableMinutePV exception, e=" + e1.getStackTrace());

- 111 }

- 112

- 113 if (res != null && res.length > 0) {

- 114 hashRet = new ConcurrentHashMap<String, String>(res.length);

- 115 for (Result re : res) {


- 116 if (re != null && !re.isEmpty()) {

- 117 try {

- 118 byte[] key = re.getRow();

- 119 byte[] value = re.getValue(fp, cp);

- 120 if (key != null && value != null) {

- 121 hashRet.put(String.valueOf(Bytes.toLong(key,

- 122 Bytes.SIZEOF_LONG)), String.valueOf(Bytes

- 123 .toLong(value)));

- 124 }

- 125 } catch (Exception e2) {

- 126 logger.error(e2.getStackTrace());

- 127 }

- 128 }

- 129 }

- 130 }

- 131

- 132 return hashRet;

- 133 }

- 134 }

- 135 //调⽤接⼝类，实现Callable接⼝

class BatchMinutePVCallable implements Callable<ConcurrentHashMap<String, String>>{

- 136

- 137 private List<String> keys;

- 138

- 139 public BatchMinutePVCallable(List<String> lstKeys ) {

- 140 this.keys = lstKeys;

- 141 }

- 142

- 143 public ConcurrentHashMap<String, String> call() throws Exception {

- 144 return DataReadServer.getBatchMinutePV(keys);

- 145 }

- 146 }


## 3.5缓存查询结果

对于频繁查询HBase的应⽤场景，可以考虑在应⽤程序中做缓存，当有新的查询请求时，⾸先在缓存 中查找，如果存在则直接返回，不再查询HBase；否则对HBase发起读请求查询，然后在应⽤程序中 将查询结果缓存起来。⾄于缓存的替换策略，可以考虑LRU等常⽤的策略。

## 3.6 Blockcache

HBase上Regionserver的内存分为两个部分，⼀部分作为Memstore，主要⽤来写；另外⼀部分作为 BlockCache，主要⽤于读。 写请求会先写⼊Memstore，Regionserver会给每个region提供⼀个Memstore，当Memstore满64MB 以后，会启动 flush刷新到磁盘。当Memstore的总⼤⼩超过限制时（heapsize * hbase.regionserver.global.memstore.uperLimit * 0.9），会强⾏启动flush进程，从最⼤的Memstore 开始flush直到低于限制。 读请求先到Memstore中查数据，查不到就到BlockCache中查，再查不到就会到磁盘上读，并把读的 结果放⼊BlockCache。由于BlockCache采⽤的是LRU策略，因此BlockCache达到上限(heapsize * hfile.block.cache.size * 0.85)后，会启动淘汰机制，淘汰掉最⽼的⼀批数据。 ⼀个Regionserver上有⼀个BlockCache和N个Memstore，它们的⼤⼩之和不能⼤于等于heapsize * 0.8，否则HBase不能启动。默认BlockCache为0.2，⽽Memstore为0.4。对于注重读响应时间的系 统，可以将 BlockCache设⼤些，⽐如设置BlockCache=0.4，Memstore=0.39，以加⼤缓存的命中 率。 有关BlockCache机制，请参考这⾥： ， ，

HBase的Block cache HBase的blockcache机制 hbase中的缓 存的计算与使⽤

。

