---
title: hbase源码系列（十二）Get、Scan在服务端是如何处理.note（原文插图 annex）
slug: annex-hbase源码系列（十二）Get、Scan在服务端是如何处理
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/大数据资料-王/hbase/hbase源码系列（十二）Get、Scan在服务端是如何处理.note.md
related: [hbase-列式存储入门]
created: 2026-07-05
updated: 2026-07-05
---

### 继上⼀篇讲了Put和Delete之后，这⼀篇我们讲Get和Scan, 因为我发现这两个操作⼏乎是⼀样的过 程，就像之前的Put和Delete⼀样，上⼀篇我本来只打算写Put的，结果发现Delete也可以⾛这个过 程，所以就⼀起写了。

# Get

我们打开HRegionServer找到get⽅法。Get的⽅法处理分两种，设置了ClosestRowBefore和没有 设置的，⼀般来讲，我们都是知道了明确的rowkey，不太会设置这个参数，它默认是false的。

view sourceprint?

- 01.if (get.hasClosestRowBefore() && get.getClosestRowBefore()) {

- 02.byte[] row = get.getRow().toByteArray();

- 03.byte[] family = get.getColumn(0).getFamily().toByteArray();

- 04.r = region.getClosestRowBefore(row, family);

- 05.} else {

- 06.Get clientGet = ProtobufUtil.toGet(get);

- 07.if (existence == null) {

- 08.r = region.get(clientGet);

- 09.}

- 10.} 所以我们⾛的是HRegion的get⽅法，杀过去。


view sourceprint?

- 1.public Result get(final Get get) throws IOException {

- 2.checkRow(get.getRow(), 'Get');

- 3.// 检查列族，以下省略代码⼀百字

- 4.List<Cell> results = get(get, true);

- 5.return Result.create(results, get.isCheckExistenceOnly() ? !results.isEmpty() : null);

- 6.}


先检查get的row是否在这个region⾥⾯，然后检查列族，如果没有的话，它会根据表定义给补全 的，然后它转身⼜进⼊了另外⼀个get⽅法，真是狠⼼啊！

view sourceprint?

- 01.List<Cell> results = new ArrayList<Cell>();

- 02.Scan scan = new Scan(get);

- 03.RegionScanner scanner = null;

- 04.try {

- 05.scanner = getScanner(scan);

- 06.scanner.next(results);


- 07.} finally {

- 08.if (scanner != null)

- 09.scanner.close();

- 10.}


从上⾯可以看得出来，为什么我要把get和Scanner⼀起讲了吧，因为get也是⼀种特殊的Scan的⽅ 法，它只寻找⼀个row的数据。

# Scan

下⾯开始讲Scan，在《HTable探秘》⾥⾯有个细节不知道注意到没，在查询之前，它要先 OpenScanner获得要给ScannerId，这个OpenScanner其实也调⽤了scan⽅法，但是它过去不是 ⼲活的，⽽是先过去注册⼀个Scanner，订个租约，然后再把这个返回的ScannerId再次发送⼀个 scan请求，这次才开始调⽤开始扫描。

扫描的时候，⾛的是这⼀段

![image 1](assets/imageFile1.png)

- 01.if (!done) {

- 02.long maxResultSize = scanner.getMaxResultSize();

- 03.if (maxResultSize <= 0) {

- 04.maxResultSize = maxScannerResultSize;

- 05.}

- 06.List<Cell> values = new ArrayList<Cell>();

- 07.MultiVersionConsistencyControl.setThreadReadPoint(scanner.getMvccReadPoint());

- 08.region.startRegionOperation(Operation.SCAN);

- 09.try {

- 10.int i = 0;

- 11.synchronized(scanner) {

- 12.for (; i < rows && currentScanResultSize < maxResultSize; i++) {

- 13.// 它⽤的是这个nextRaw⽅法

- 14.boolean moreRows = scanner.nextRaw(values);

- 15.if (!values.isEmpty()) {

- 16.results.add(Result.create(values));

- 17.}

- 18.if (!moreRows) {

- 19.break;

- 20.}

- 21.values.clear();

- 22.}

- 23.}

- 24.} finally {

- 25.region.closeRegionOperation();

- 26.}

- 27.}

- 28.

- 29.// 没找到设置moreResults为false，找到了把结果添加到builder⾥⾯去

- 30.if (scanner.isFilterDone() && results.isEmpty()) {


- 31.moreResults = false;

- 32.results = null;

- 33.} else {

- 34.addResults(builder, results, controller);

- 35.}

- 36.}

- 37.}


这⾥⾯有controller和result，这块的话，我求证了⼀下RpcServer那块，如果Rpc传输的时候使⽤ 了codec来压缩的话，就⽤controller返回结果，否则⽤response返回。

这块就不管了不是重点，下⾯我们看⼀下RegionScanner。

# RegionScanner详解与代码拆分

我们冲过去看RegionScannerImpl吧，它在HRegion⾥⾯，我们直接去看nextRaw⽅法就可以了， get⽅法的那个next⽅法也是调⽤了nextRaw⽅法。

view sourceprint?

- 1.if (outResults.isEmpty()) {// 把结果存到outResults当中

- 2.returnResult = nextInternal(outResults, limit);

- 3.} else {

- 4.List<Cell> tmpList = new ArrayList<Cell>();

- 5.returnResult = nextInternal(tmpList, limit);

- 6.outResults.addAll(tmpList);

- 7.} 去nextInternal⽅法吧，这⽅法真⼤，尼玛，我要歇菜了，我们进⼊下⼀个阶段吧。


![image 2](assets/imageFile2.png)

- 001./** 把查询出来的结果保存到results当中 */

- 002.private boolean nextInternal(List<Cell> results, int limit)

- 003.throws IOException {

- 004.

- 005.while (true) {

- 006.//从storeHeap⾥⾯取出⼀个来

- 007.KeyValue current = this.storeHeap.peek();

- 008.

- 009.byte[] currentRow = null;

- 010.int offset = 0;

- 011.short length = 0;

- 012.if (current != null) {

- 013.currentRow = current.getBuffer();

- 014.offset = current.getRowOffset();

- 015.length = current.getRowLength();

- 016.}

- 017.//检查⼀下到这个row是否应该停⽌了

- 018.boolean stopRow = isStopRow(currentRow, offset, length);

- 019.if (joinedContinuationRow == null) {

- 020.// 如果要停⽌了，就⽤filter的filterRowCells过滤⼀下results.

- 021.if (stopRow) {

- 022.if (filter != null && filter.hasFilterRow()) {

- 023.//使⽤filter过滤掉⼀些cells

- 024.filter.filterRowCells(results);

- 025.}

- 026.return false;

- 027.}

- 028.// 如果有filter的话，过滤通过

- 029.if (filterRowKey(currentRow, offset, length)) {

- 030.boolean moreRows = nextRow(currentRow, offset, length);


- 031.if (!moreRows) return false;

- 032.results.clear();

- 033.continue;

- 034.}

- 035.//把结果保存到results当中

- 036.KeyValue nextKv = populateResult(results, this.storeHeap, limit, currentRow, offset,

- 037.length);

- 038.// Ok, we are good, let's try to get some results from the main heap.

- 039.// 在populateResult找到了⾜够limit数量的

- 040.if (nextKv == KV_LIMIT) {

- 041.if (this.filter != null && filter.hasFilterRow()) {

- 042.throw new IncompatibleFilterException(

- 043.'Filter whose hasFilterRow() returns true is incompatible with scan with limit!');

- 044.}

- 045.return true; // We hit the limit.

- 046.}

- 047.

- 048.stopRow = nextKv == null ||

- 049.isStopRow(nextKv.getBuffer(), nextKv.getRowOffset(), nextKv.getRowLength());

- 050.// save that the row was empty before filters applied to it.

- 051.final boolean isEmptyRow = results.isEmpty();

- 052.

- 053.// We have the part of the row necessary for filtering (all of it, usually).

- 054.// First filter with the filterRow(List). 过滤⼀下刚才找出来的

- 055.if (filter != null && filter.hasFilterRow()) {

- 056.filter.filterRowCells(results);

- 057.}

- 058.//如果result的空的，啥也没找到，这是。。。悲剧啊

- 059.if (isEmptyRow) {

- 060.boolean moreRows = nextRow(currentRow, offset, length);

- 061.if (!moreRows) return false;

- 062.results.clear();


- 063.// This row was totally filtered out, if this is NOT the last row,

- 064.// we should continue on. Otherwise, nothing else to do.

- 065.if (!stopRow) continue;

- 066.return false;

- 067.}

- 068.

- 069.// Ok, we are done with storeHeap for this row.

- 070.// Now we may need to fetch additional, non-essential data into row.

- 071.// These values are not needed for filter to work, so we postpone their

- 072.// fetch to (possibly) reduce amount of data loads from disk.

- 073.if (this.joinedHeap != null) {

- 074.KeyValue nextJoinedKv = joinedHeap.peek();

- 075.// If joinedHeap is pointing to some other row, try to seek to a correct one.

- 076.boolean mayHaveData =

- 077.(nextJoinedKv != null && nextJoinedKv.matchingRow(currentRow, offset, length))

- 078.|| (this.joinedHeap.requestSeek(KeyValue.createFirstOnRow(currentRow, offset, length),

- 079.true, true)

- 080.&& joinedHeap.peek() != null

- 081.&& joinedHeap.peek().matchingRow(currentRow, offset, length));

- 082.if (mayHaveData) {

- 083.joinedContinuationRow = current;

- 084.populateFromJoinedHeap(results, limit);

- 085.}

- 086.}

- 087.} else {

- 088.// Populating from the joined heap was stopped by limits, populate some more.

- 089.populateFromJoinedHeap(results, limit);

- 090.}

- 091.

- 092.// We may have just called populateFromJoinedMap and hit the limits. If that is

- 093.// the case, we need to call it again on the next next() invocation.

- 094.if (joinedContinuationRow != null) {


- 095.return true;

- 096.}

- 097.

- 098.// Finally, we are done with both joinedHeap and storeHeap.

- 099.// Double check to prevent empty rows from appearing in result. It could be

- 100.// the case when SingleColumnValueExcludeFilter is used.

- 101.if (results.isEmpty()) {

- 102.boolean moreRows = nextRow(currentRow, offset, length);

- 103.if (!moreRows) return false;

- 104.if (!stopRow) continue;

- 105.}

- 106.

- 107.// We are done. Return the result.

- 108.return !stopRow;

- 109.}

- 110.}


上⾯那段代码真的很⻓很臭，尼玛。。被我折叠起来了，有兴趣的看⼀眼就⾏，我们先分解开来 看吧，这⾥⾯有两个Heap，⼀个是storeHeap，⼀个是JoinedHeap，他们啥时候⽤呢？看⼀下它 的构造⽅法吧

view sourceprint?

- 01.for (Map.Entry<byte[], NavigableSet<byte[]>> entry :

- 02.scan.getFamilyMap().entrySet()) {

- 03.//遍历列族和列的映射关系，设置store相关的内容

- 04.Store store = stores.get(entry.getKey());

- 05.KeyValueScanner scanner = store.getScanner(scan, entry.getValue());

- 06.if (this.filter == null || !scan.doLoadColumnFamiliesOnDemand()

- 07.|| this.filter.isFamilyEssential(entry.getKey())) {

- 08.scanners.add(scanner);

- 09.} else {

- 10.joinedScanners.add(scanner);


- 11.}

- 12.}

- 13.this.storeHeap = new KeyValueHeap(scanners, comparator);

- 14.if (!joinedScanners.isEmpty()) {

- 15.this.joinedHeap = new KeyValueHeap(joinedScanners, comparator);

- 16.}

- 17.}


如果joinedScanners不空的话，就new⼀个joinedHeap出来，但是我们看看它的成⽴条件，有点⼉ 难吧。

- 1、filter不为null

- 2、scan设置了doLoadColumnFamiliesOnDemand为true

- 3、设置了的filter的isFamilyEssential⽅法返回false，这个估计得⾃⼰写⼀个，因为我刚才去看了 ⼏个filter的这个⽅法默认都是⽤的FilterBase的⽅法返回false。


好的，到这⾥我们有可以把上⾯那段代码砍掉很⼤⼀部分了，它的成⽴条件⽐较困难，所以很难 出现了，那我们就挑重点的storeHeap来讲吧，我们先看着这三⾏。

view sourceprint?

- 1.Store store = stores.get(entry.getKey());

- 2.KeyValueScanner scanner = store.getScanner(scan, entry.getValue());

- 3.this.storeHeap = new KeyValueHeap(scanners, comparator);


通过列族获得相应的Store，然后通过getScanner返回scanner加到KeyValueHeap当中，我们应该 去刺探⼀下HStore的getScanner⽅法，它new了⼀个StoreScanner返回，继续看StoreScanner。

![image 3](assets/imageFile3.png)

view sourceprint?

- 01.public StoreScanner(Store store, ScanInfo scanInfo, Scan scan, final NavigableSet<byte[]> columns) throws IOException {

- 02.

- 03.matcher = new ScanQueryMatcher(scan, scanInfo, columns,

- 04.ScanType.USER_SCAN, Long.MAX_VALUE, HConstants.LATEST_TIMESTAMP,

- 05.oldestUnexpiredTS);

- 06.

- 07.// 返回MemStore、所有StoreFile的Scanner.

- 08.List<KeyValueScanner> scanners = getScannersNoCompaction();

- 09.

- 10.//explicitColumnQuery:是否过滤列族 lazySeekEnabledGlobally默认是true 如果⽂件数量超过1个， isParallelSeekEnabled就是true

- 11.if (explicitColumnQuery && lazySeekEnabledGlobally) {

- 12.for (KeyValueScanner scanner : scanners) {

- 13.scanner.requestSeek(matcher.getStartKey(), false, true);

- 14.}

- 15.} else {

- 16.if (!isParallelSeekEnabled) {

- 17.for (KeyValueScanner scanner : scanners) {

- 18.scanner.seek(matcher.getStartKey());

- 19.}

- 20.} else {

- 21. //⼀般⾛这⾥，并⾏查

- 22.parallelSeek(scanners, matcher.getStartKey());

- 23.}

- 24.}

- 25.

- 26.// ⼀个堆⾥⾯包括了两个scanner，MemStore、StoreFile的Scanner

- 27.heap = new KeyValueHeap(scanners, store.getComparator());

- 28.

- 29.this.store.addChangedReaderObserver(this);


- 30.}


对上⾯的代码，我们再慢慢来分解。

- 1、先new了⼀个ScanQueryMatcher，它是⼀个⽤来过滤的类，传参数的时候，需要传递scan和 oldestUnexpiredTS进去，oldestUnexpiredTS是个参数，是（当前时间-列族的⽣存周期），⼩于 这个时间戳的kv视为已经过期了，在它初始化的时候，我们注意⼀下它的startKey和stopRow，这 个startKey要注意，它可不是我们设置的那个startRow，⽽是⽤这个startRow来new了⼀个 DeleteFamily类型的KeyValue。

- 1.this.stopRow = scan.getStopRow();

- 2.this.startKey = KeyValue.createFirstDeleteFamilyOnRow(scan.getStartRow())


- 2、接着我们看getScannersNoCompaction这个⽅法，它这⾥是返回了两个Scanner， MemStoreScanner和所有StoreFile的Scanner，在从StoreHeap中peak出来⼀个kv的时候，是从 他们当中交替取出kv来的，StoreHeap从它的名字上⾯来看像是⽤了堆排序的算法，它的peek⽅ 法和next⽅法真有点⼉复杂，下⼀章讲MemStore的时候再讲吧。

- 1.//获取所有的storefile，默认的实现没有⽤上startRow和stopRow

- 2.startRow和stopRow storeFilesToScan =

- 3.this.storeEngine.getStoreFileManager().getFilesForScanOrGet(isGet, startRow, stopRow);

- 4.memStoreScanners = this.memstore.getScanners();


默认的getStoreFileManager的getFilesForScanOrGet是返回了所有的StoreFile的Scanner，⽽不 是通过startRow和stopRow做过滤，它的注释⾥⾯给出的解释，⾥⾯的files默认是按照seq id来排 序的，⽽不是startKey，需要优化的可以从这⾥下⼿。

- 3、然后就开始先seek⼀下，⽽不是全表扫啊！


view sourceprint?

view sourceprint?

view sourceprint?

- 1.//过滤列族的情况

- 2.scanner.requestSeek(matcher.getStartKey(), false, true);

- 3.//⼀般⾛这⾥，并⾏查

- 4.parallelSeek(scanners, matcher.getStartKey());


scanner.requestSeek不是所有情况都要seek，是查询Delete的时候，如果查询的kv的时间戳⽐⽂ 件的最⼤时间戳⼩，就seek到上次未查询到的kv；它这⾥可能会⽤上DeleteFamily删除真个family 这种情况。

parallelSeek就是开多线程去调⽤Scanner的seek⽅法, MemStore的seek很简单，因为它的kv集合 是⼀个排序好的集合，HFile的seek⽐较复杂，下⾯我⽤⼀个图来表达吧。

![image 4](assets/imageFile4.png)

在搜索HFile的时候，key先从⼀级索引找，通过它定位到细的⼆级索引，然后再定位到具体的

block上⾯，到了HFileBlock之后，就不是seek了，就是遍历，遍历没什么好说的，不熟悉的朋友 建议先回去看看《StoreFile存储格式》。注意哦，这个key就是我们的startKey哦，所以⼤家知道 为什么要在scan的时候要设置StartKey了吗？

# nextInternal的流程

通过前⾯的分析，我们可以把nextInternal分解与拆分、抹去⼀些不必要的代码，我发现代码还是 很难懂，所以我画了⼀个过程图出来代替那段代码。

![image 5](assets/imageFile5.png)

特别注意事项：

- 1、个图是被我处理过的简化之后的图，还有在放弃该row的kv们 之后并⾮都要进⾏是StopRow的 判断，只是为了合并这个流程，我加上去的isStopRow的判断，但并不影响整个流程。

- 2、！isStopRow代表返回代码的(!isStopRow)的意思, 根据isStopRow的当前值来返回true或者 false

- 3、true意味着退出，并且还有结果，false意味着退出，没有结果


诶，看到这⾥，还是没看到它是怎么⽤ScanQueryMatcher去过滤被删除的kv们啊，好，接下来我 们重点考察这个问题。

# ScanQueryMatcher如何过滤已经被删除的KeyValue

这个过程屏蔽在了filterRow之后通过的把该row的kv接到结果集的这⼀步⾥⾯去了。它在⾥⾯不停 的调⽤KeyValueHeap的next⽅法，match的调⽤正好在这个⽅法。我们现在就去追踪这遗失的部 分。

我们直接去看它的match⽅法就好了，别的不⽤看了，它处理的情况好多好多，尼玛，这是要死⼈ 的节奏啊。

ScanQueryMatcher是⽤来处理⼀⾏数据之间的版本问题的，在每遇到⼀个新的row的时候，它都 会先被设置matcher.setRow(row, offset, length)。

view sourceprint?

- 1.if (limit < 0 || matcher.row == null || !Bytes.equals(row, offset, length, matcher.row,

- 2.matcher.rowOffset, matcher.rowLength)) {

- 3.this.countPerRow = 0;

- 4.matcher.setRow(row, offset, length);

- 5.} 上⾯这段代码在StoreScanner的next⽅法⾥⾯，每当⼀⾏结束之后，都会调⽤这个⽅法。


在讲match⽅法之前，我先讲⼀下rowkey的排序规则，rowkey 正序->family 正序->qualifier 正序>ts 降序->type 降序，那么对于同⼀个⾏、列族、列的数据，时间越近的排在前⾯，类型越⼤的排 在前⾯，⽐如Delete就在Put前⾯，下⾯是它的类型表。

view sourceprint?

- 01.//search⽤

- 02.Minimum((byte)0),

- 03.Put((byte)4),


- 04.Delete((byte)8),

- 05.DeleteFamilyVersion((byte)10),

- 06.DeleteColumn((byte)12),

- 07.DeleteFamily((byte)14),

- 08.//search⽤

- 09.Maximum((byte)255);


为什么这⾥先KeyValue的排序规则呢，这当然有关系了，这关系着扫描的时候，谁先谁后的问 题，如果时间戳⼩的在前⾯，下⾯这个过滤就不⽣效了。

下⾯我们看看它的match⽅法的检查规则。

- 1、和当前⾏⽐较

- 1.//和当前的⾏进⾏⽐较，只有相等才继续，⼤于当前的⾏就要跳到下⼀⾏，⼩于说明有问题，停⽌

- 2.int ret = this.rowComparator.compareRows(row, this.rowOffset, this.rowLength,

- 3.bytes, offset, rowLength);

- 4.if (ret <= -1) {

- 5.return MatchCode.DONE;

- 6.} else if (ret >= 1) {

- 7.return MatchCode.SEEK_NEXT_ROW;

- 8.}


- 2、检查是否所有列都查过了

- 1.//所有的列都扫描过来

- 2.if (this.columns.done()) {

- 3.stickyNextRow = true;

- 4.return MatchCode.SEEK_NEXT_ROW;

- 5.}


- 3、检查列的时间戳是否过期


view sourceprint?

view sourceprint?

view sourceprint?

- 1.long timestamp = kv.getTimestamp();

- 2.// 检查列的时间是否过期

- 3.if (columns.isDone(timestamp)) {


- 4.return columns.getNextRowOrNextColumn(bytes, offset, qualLength);

- 5.}


- 4a、如果是Delete的类型，加到ScanDeleteTraker。


view sourceprint?

- 1.if (kv.isDelete()) {

- 2.this.deletes.add(bytes, offset, qualLength, timestamp, type);

- 3.}


- 4b、如果不是，如果ScanDeleteTraker⾥⾯有Delete，就要让它经历ScanDeleteTraker的检验了 （进宫前先验⼀下身）


view sourceprint?

- 01.DeleteResult deleteResult = deletes.isDeleted(bytes, offset, qualLength,

- 02.timestamp);

- 03.switch (deleteResult) {

- 04.case FAMILY_DELETED:

- 05.case COLUMN_DELETED:

- 06.return columns.getNextRowOrNextColumn(bytes, offset, qualLength);

- 07.case VERSION_DELETED:

- 08.case FAMILY_VERSION_DELETED:

- 09.return MatchCode.SKIP;

- 10.case NOT_DELETED:

- 11.break;

- 12.default:

- 13.throw new RuntimeException('UNEXPECTED');

- 14.} 这⾥就要说⼀下刚才那⼏个Delete的了：


- 1）DeleteFamily是最凶狠的，⽣命周期也⻓，整个列族全删，基本上会⼀直存在

- 2）DeleteColum只删掉⼀个列，出现这个列的都会被⼲掉

- 3）DeleteFamilyVersion没遇到过

- 4）Delete最差劲⼉了，只能删除指定时间戳的，时间戳⼀定要对哦，否则⼀旦发现不对的，这个 Delete就失效了，可以说，⽣命周期只有⼀次，下⾯是源代码。


![image 6](assets/imageFile6.png)

view sourceprint?

- 01.public DeleteResult isDeleted(byte [] buffer, int qualifierOffset,

- 02.int qualifierLength, long timestamp) {

- 03.//时间戳⼩于删除列族的时间戳，说明这个列族被删掉是后来的事情

- 04.if (hasFamilyStamp && timestamp <= familyStamp) {

- 05.return DeleteResult.FAMILY_DELETED;

- 06.}

- 07.//检查时间戳

- 08.if (familyVersionStamps.contains(Long.valueOf(timestamp))) {

- 09.return DeleteResult.FAMILY_VERSION_DELETED;

- 10.}

- 11.

- 12.if (deleteBuffer != null) {

- 13.

- 14.int ret = Bytes.compareTo(deleteBuffer, deleteOffset, deleteLength,

- 15.buffer, qualifierOffset, qualifierLength);

- 16.

- 17.if (ret == 0) {

- 18.if (deleteType == KeyValue.Type.DeleteColumn.getCode()) {

- 19.return DeleteResult.COLUMN_DELETED;

- 20.}

- 21.// 坑爹的Delete它只删除相同时间戳的，遇到不想的它就pass了

- 22.if (timestamp == deleteTimestamp) {

- 23.return DeleteResult.VERSION_DELETED;

- 24.}

- 25.

- 26.//时间戳不对，这个Delete失效了

- 27.deleteBuffer = null;

- 28.} else if(ret < 0){

- 29.// row⽐当前的⼤，这个Delete也失效了

- 30.deleteBuffer = null;


- 31.} else {

- 32.throw new IllegalStateException(...);

- 33.}

- 34.}

- 35.

- 36.return DeleteResult.NOT_DELETED;


上⼀章说过，Delete new出来之后什么都不设置，就是DeleteFamily级别的选⼿，所以在它之后 的会全部被⼲掉，所以你们懂的，我们也会⽤DeleteColum来删除某⼀列数据，只要时间戳在它之 前的kv就会被⼲掉，删某个指定版本的少，因为你得知道具体的时间戳，否则你删不了。

# 例⼦详解DeleteFamily

假设我们有这些数据

view sourceprint?

- 1.KeyValue [] kvs1 = new KeyValue[] {

- 2.KeyValueTestUtil.create('R1', 'cf', 'a', now, KeyValue.Type.Put, 'dont-care'),

- 3.KeyValueTestUtil.create('R1', 'cf', 'a', now, KeyValue.Type.DeleteFamily, 'dont-care'),

- 4.KeyValueTestUtil.create('R1', 'cf', 'a', now-500, KeyValue.Type.Put, 'dont-care'),

- 5.KeyValueTestUtil.create('R1', 'cf', 'a', now+500, KeyValue.Type.Put, 'dont-care'),

- 6.KeyValueTestUtil.create('R1', 'cf', 'a', now, KeyValue.Type.Put, 'dont-care'),

- 7.KeyValueTestUtil.create('R2', 'cf', 'z', now, KeyValue.Type.Put, 'dont-care')

- 8.}; Scan的参数是这些。


view sourceprint?

- 1.Scan scanSpec = new Scan(Bytes.toBytes('R1'));

- 2.scanSpec.setMaxVersions(3);

- 3.scanSpec.setBatch(10);

- 4.StoreScanner scan = new StoreScanner(scanSpec, scanInfo, scanType, getCols('a','z'), scanners); 然后，我们先将他们排好序，是这样的。


view sourceprint?

- 1.R1/cf:a/1400602376242(now+500)/Put/vlen=9/mvcc=0,


- 2.R1/cf:a/1400602375742(now)/DeleteFamily/vlen=9/mvcc=0,

- 3.R1/cf:a/1400602375742(now)/Put/vlen=9/mvcc=0,

- 4.R1/cf:a/1400602375742(now)/Put/vlen=9/mvcc=0,

- 5.R1/cf:a/1400602375242(now-500)/Put/vlen=9/mvcc=0,

- 6.R2/cf:z/1400602375742(now)/Put/vlen=9/mvcc=0


所以到最后，⻩⾊的三⾏会被删除，只剩下第⼀⾏和最后⼀⾏，但是最后⼀⾏也会被排除掉，因 为它已经换⾏了，不是同⼀个⾏的，不在这⼀轮进⾏⽐较，返回MatchCode.DONE。

## ---->回到前⾯是match过程

- 5、检查时间戳，即设置给Scan的时间戳，这个估计⼀般很少设置，时间戳国企，就返回下⼀个 MatchCode.SEEK_NEXT_ROW。

- 6、检查列是否是Scan⾥⾯设置的需要查询的列。

- 7、检查列的版本，Scan设置的MaxVersion，超过了这个version就要赶紧闪⼈了哈，返回 MatchCode.SEEK_NEXT_COL。


对于match的结果，有⼏个常⻅的：

- 1、MatchCode.INCLUDE_AND_SEEK_NEXT_COL 包括当前这个，跳到下⼀列，会引发 StoreScanner的reseek⽅法。

- 2、MatchCode.SKIP 忽略掉，继续调⽤next⽅法。

- 3、MatchCode.SEEK_NEXT_ROW 不包括当前这个，继续调⽤next⽅法。

- 4、MatchCode.SEEK_NEXT_COL 不包括它，跳过下⼀列，会引发StoreScanner的reseek⽅法。

- 5、MatchCode.DONE rowkey变了，要留到下次进⾏⽐较了 Delete讲到这⾥基本算结束了。 关于测试


呵呵，有兴趣测试的童鞋可以打开下hbase源码，找到TestStoreScanner这个类⾃⼰调试看下结 果。
