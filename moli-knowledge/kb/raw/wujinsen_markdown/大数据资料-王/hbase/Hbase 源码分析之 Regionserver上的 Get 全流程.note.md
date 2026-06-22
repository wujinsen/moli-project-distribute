当regionserver收到来⾃客户端的Get请求时，调⽤接⼝ public Result get(byte[] regionName, Get get) {

... HRegion region = getRegion(regionName); return region.get(get, getLockFromId(get.getLockId()));

... }

我们看HRegion.get接⼝，其⾸先会做family检测，保证Get中的family与Table的相符，然后通过 RegionScanner.next来返回result

⽽Scanner是Hbase读流程中的主要类，先做⼀个⼤概描述： 从Scanner的scan范围来分有RegionScanner，StoreScanner，MemstoreScanner，HFileScanner； 根据名称很好理解他们的作⽤，⽽他们之间的关系：RegionScanner由⼀个或多个StoreScanner组 成，StoreScanner由MemstoreScanner和HFileScanner组成；

再看RegionScanner类的构造形成过程： List<KeyValueScanner> scanners = new ArrayList<KeyValueScanner>(); for (Map.Entry<byte[], NavigableSet<byte[]>> entry : scan.getFamilyMap().entrySet()) {

Store store = stores.get(entry.getKey()); scanners.add(store.getScanner(scan, entry.getValue()));

}

this.storeHeap = new KeyValueHeap(scanners, comparator);

这段代码为RegionScanner类内部属性storeHeap初始化，其内容就是Region下⾯所有StoreScanner的 和；storeHeap是⼀个KeyValueHeap，从字⾯可以理解result就是从中获取的

接着看store.getScanner(scan, entry.getValue())即StoreScanner类的构造形成过程： Java代码

![image 1](<Hbase 源码分析之 Regionserver上的 Get 全流程.note_images/imageFile1.png>)

- 1.
- 2.
- 3.
- 4.


/StoreScaner is a scaner for both the memstore and the HStore files List<KeyValueScaner> scaners = new LinkedList<KeyValueScaner>();

/ First the store file scaners if (memOnly = false) {

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


List<StoreFileScaner> sfScaners = StoreFileScaner

.getScanersForStoreFiles(store.getStorefiles(), cacheBlocks, isGet);

/ include only those scan files which pas al filters for (StoreFileScaner sfs : sfScaners) {

if (sfs.shouldSek(scan, columns) {

scaners.ad(sfs); }

} }

/ Then the memstore scaners if(filesOnly = false) & (this.store.memstore.shouldSek(scan) {

scaners.adAl(this.store.memstore.getScaners(); }

return scaners;

⼀般情况下StoreScanner中添加了HFileScanner和MemStoreScanner； StoreFileScanner的内部属性包括HFileScanner和Hfile.Reader，在添加前会根据timestamp， columns，bloomfilter过滤掉⼀部分

Scanner构造完毕以后，当最上层的RegionScanner.next时，⾸先会先从MemStoreScanner中获取， 如果没有或者版本数不⾜，则再从HfileScanner中获取，⽽从HfileScanner获取时，先查看是否在 blockcache中，如果MISS则再从底层的HDFS中获取block，并根据设置决定是否将Block cache到 LruBlockCache中

