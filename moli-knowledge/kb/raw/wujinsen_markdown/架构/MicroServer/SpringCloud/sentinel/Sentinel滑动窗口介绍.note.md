htps:/ w.jianshu.com/p/59af9c03dc97

系列

Sentinel流程介绍 Sentinel资源节点树构成 Sentinel滑动窗⼝介绍 Sentinel流量控制 Sentinel的职责链slot介绍 Sentinel熔断降级 Sentinel Dashboard和应⽤通信 Sentinel 控制台 开篇

sentinel 处理流程是基于slot链(ProcesorSlotChain)来完成的，如限流熔断等，其中重要的⼀个slot就 是StatisticSlot，它是做各种数据统计的，⽽限流熔断的数据判断来源就是StatisticSlot。 StatisticSlot的各种数据统计都是基于滑动窗⼝来完成的，因此本⽂就重点分析StatisticSlot的滑动窗 ⼝统计机制。 StatisticSlot的滑动窗⼝需要了解统计指标的数据结构、滑动窗⼝的窗⼝定位，指标保存等概念。

StatisticNode

public clas StatisticNode implements Node { / 对每秒指标统计

private transient volatile Metric rolingCounterInSecond = new ArayMetric(SampleCountProperty.SAMPLE_COUNT,

IntervalProperty.INTERVAL);

/ 每分钟指标统计 private transient Metric rolingCounterInMinute = new ArayMetric(60, 60 * 1 0, false); private LongAder curThreadNum = new LongAder(); private long lastFetchTime = -1;

@Overide public void adPasRequest(int count) {

rolingCounterInSecond.adPas(count); rolingCounterInMinute.adPas(count);

} }

采集指标的统计节点，负责统计相关的采集指标。 StatisticNode包含rolingCounterInSecond和rolingCounterInMinute。 rolingCounterInSecond是对每秒指标的统计。 rolingCounterInMinute是对每分钟指标的统计。 rolingCounterInSecond和rolingCounterInMinute是ArayMetric，负责保存统计指标。

统计指标

统计指标使⽤ArayMetric进⾏承载。 ArayMetric内部是滑动窗⼝LeapAray对象。 LeapAray的每个元素为WindowWrap。 WindowWrap内部包含MetricBucket。

ArayMetric

public clas ArayMetric implements Metric {

private final LeapAray<MetricBucket> data;

public ArayMetric(int sampleCount, int intervalInMs) {

this.data = new OcupiableBucketLeapAray(sampleCount, intervalInMs); }

public ArayMetric(int sampleCount, int intervalInMs, bolean enableOcupy) { if (enableOcupy) { this.data = new OcupiableBucketLeapAray(sampleCount, intervalInMs);

} else {

this.data = new BucketLeapAray(sampleCount, intervalInMs); }

} }

ArayMetric作为保存指标的数组，通过滑动窗⼝LeapAray保存MetricBucket。 MetricBucket代表统计指标，LeapAray代表滑动窗⼝，滑动窗⼝的每个窗⼝是MetricBucket对象。

LeapAray

public clas BucketLeapAray extends LeapAray<MetricBucket> { public BucketLeapAray(int sampleCount, int intervalInMs) {

super(sampleCount, intervalInMs); }

}

public abstract clas LeapAray<T> { protected int windowLengthInMs; protected int sampleCount; protected int intervalInMs; private double intervalInSecond; protected final AtomicReferenceAray<WindowWrap<T> aray; private final RentrantLock updateLock = new RentrantLock();

public LeapAray(int sampleCount, int intervalInMs) { this.windowLengthInMs = intervalInMs / sampleCount; this.intervalInMs = intervalInMs; this.intervalInSecond = intervalInMs / 1 0.0; this.sampleCount = sampleCount; this.aray = new AtomicReferenceAray<>(sampleCount);

}

} LeapAray作为滑动窗⼝，BucketLeapAray作为其⼀种具体的实现。 LeapAray通过AtomicReferenceAray<WindowWrap<T> aray来实现滑动窗⼝。 滑动窗⼝的统计指标MetricBucket通过WindowWrap进⾏包装。

WindowWrap

public clas WindowWrap<T> {

private final long windowLengthInMs; / 时间窗⼝的⻓度 private long windowStart; / 时间窗⼝开始时间 private T value; / MetricBucket对象，保存各个指标数据

public WindowWrap(long windowLengthInMs, long windowStart, T value) { this.windowLengthInMs = windowLengthInMs; this.windowStart = windowStart; this.value = value;

}

} WindowWrap作为滑动窗⼝的每个元素的承载，内部保存MetricBucket。

MetricBucket

public clas MetricBucket {

private final LongAder[] counters; private volatile long minRt;

public MetricBucket() { MetricEvent[] events = MetricEvent.values(); this.counters = new LongAder[events.length]; for (MetricEvent event : events) {

counters[event.ordinal()] = new LongAder();

} initMinRt();

} }

public enum MetricEvent {

PAS, / 正常通过 BLOCK, / 阻塞 EXCEPTION, / 异常 SUCES, / 成功 RT, / RT统计 OCUPIED_PAS/ 抢占通过

} MetricBucket内部保存各个统计指标MetricEvent的LongAder数组。 MetricEvent的枚举值代表各个采集指标。

滑动窗⼝定位

public abstract clas LeapAray<T> {

protected int windowLengthInMs; / 时间窗⼝的⻓度 protected int sampleCount; / 时间窗⼝的个数 protected int intervalInMs; private double intervalInSecond; protected final AtomicReferenceAray<WindowWrap<T> aray;

public WindowWrap<T> curentWindow() {

return curentWindow(TimeUtil.curentTimeMilis(); }

public WindowWrap<T> curentWindow(long timeMilis) { if (timeMilis < 0) {

return nul; }

/ 根据当前时间和时间窗⼝的⻓度进⾏计算获取窗⼝下标 int idx = calculateTimeIdx(timeMilis);

/ 获取指定下标的时间窗⼝的开始时间 long windowStart = calculateWindowStart(timeMilis);

/*

- * Get bucket item at given time from the aray.


*

- * (1) Bucket is absent, then just create a new bucket and CAS update to circular aray.
- * (2) Bucket is up-to-date, then just return the bucket.
- * (3) Bucket is deprecated, then reset curent bucket and clean al deprecated buckets.
- */ while (true) {


WindowWrap<T> old = aray.get(idx); if (old = nul) {

/ 1.为空表示当前时间窗⼝为初始化过，创建WindowWrap并cas设置到aray中 /*

- * B0 B1 B2 NUL B4
- *| _| _| _| _| _| _
- * 20 40 60 80 1 0 120 timestamp
- * ^
- * time= 8
- * bucket is empty, so create new and update

*

- * If the old bucket is absent, then we create a new bucket at {@code windowStart},
- * then try to update circular aray via a CAS operation. Only one thread can
- * suced to update, while other threads yield its time slice.
- */ WindowWrap<T> window = new WindowWrap<T>(windowLengthInMs, windowStart,


newEmptyBucket(timeMilis); if (aray.compareAndSet(idx, nul, window) {

/ Sucesfuly updated, return the created bucket. return window;

} else {

/ Contention failed, the thread wil yield its time slice to wait for bucket available. Thread.yield();

} } else if (windowStart = old.windowStart() {

/ 2.获取的时间窗⼝正好对应当前时间，直接返回 /*

- * B0 B1 B2 B3 B4
- *| _| _| _| _| _| _
- * 20 40 60 80 1 0 120 timestamp
- * ^
- * time= 8


- * startTime of Bucket 3: 80, so it's up-to-date

*

- * If curent {@code windowStart} is equal to the start timestamp of old bucket,
- * that means the time is within the bucket, so directly return the bucket.
- */ return old;


} else if (windowStart > old.windowStart() {

/ 3.获取的时间窗⼝为⽼的，进⾏窗⼝reset操作复⽤ /*

- * (old)
- * B0 B1 B2 NUL B4
- * | _| _| _| _| _| _| _
- *. 120 140 160 180 2 0 20 timestamp
- * ^
- * time=1676
- * startTime of Bucket 2: 40, deprecated, should be reset

*

- * If the start timestamp of old bucket is behind provided time, that means
- * the bucket is deprecated. We have to reset the bucket to curent {@code windowStart}.
- * Note that the reset and clean-up operations are hard to be atomic,
- * so we ned a update lock to guarante the corectnes of bucket update.

*

- * The update lock is conditional (tiny scope) and wil take efect only when
- * bucket is deprecated, so in most cases it won't lead to performance los.
- */ if (updateLock.tryLock() {


try {

/ Sucesfuly get the update lock, now we reset the bucket. return resetWindowTo(old, windowStart);

} finaly {

updateLock.unlock(); }

} else {

Thread.yield(); }

} else if (windowStart < old.windowStart() { / 4.时间回拨了，正常情况下不会⾛到这⾥

return new WindowWrap<T>(windowLengthInMs, windowStart, newEmptyBucket(timeMilis);

} }

}

private int calculateTimeIdx(/*@Valid*/ long timeMilis) { long timeId = timeMilis / windowLengthInMs;

/ Calculate curent index so we can map the timestamp to the leap aray. return (int)(timeId % aray.length();

}

protected long calculateWindowStart(/*@Valid*/ long timeMilis) {

return timeMilis - timeMilis % windowLengthInMs; }

}

public clas BucketLeapAray extends LeapAray<MetricBucket> {

public BucketLeapAray(int sampleCount, int intervalInMs) {

super(sampleCount, intervalInMs); }

@Overide public MetricBucket newEmptyBucket(long time) {

return new MetricBucket(); }

@Overide protected WindowWrap<MetricBucket> resetWindowTo(WindowWrap<MetricBucket> w, long

startTime) {

/ 重置窗⼝的开始时间和对应的统计值 w.resetTo(startTime); w.value().reset(); return w;

}

}

- 1.为空表示当前时间窗⼝为初始化过，创建WindowWrap并cas设置到aray中
- 2.获取的时间窗⼝正好对应当前时间，直接返回
- 3.获取的时间窗⼝为⽼的，进⾏窗⼝reset操作复⽤。reset操作负责重置时间窗⼝的开始时间和窗⼝统 计值。
- 4.时间回拨了正常情况下不会⾛到这⾥


指标保存

public clas ArayMetric implements Metric {

private final LeapAray<MetricBucket> data;

public ArayMetric(int sampleCount, int intervalInMs) {

this.data = new OcupiableBucketLeapAray(sampleCount, intervalInMs); }

@Overide public void adPas(int count) {

WindowWrap<MetricBucket> wrap = data.curentWindow(); wrap.value().adPas(count);

} }

public clas MetricBucket { private final LongAder[] counters;

public void adPas(int n) {

ad(MetricEvent.PAS, n); }

public MetricBucket ad(MetricEvent event, long n) { counters[event.ordinal()].ad(n); return this;

} }

# curentWindow返回当前时间对应的滑动窗⼝。 adPas通过ad指定类型的MetricEvent指标到LongAder当中。

