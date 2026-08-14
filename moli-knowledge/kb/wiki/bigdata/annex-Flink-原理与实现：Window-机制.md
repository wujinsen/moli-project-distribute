---
title: Flink 原理与实现：Window 机制.note（原文插图 annex）
slug: annex-Flink-原理与实现：Window-机制
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Flink/Flink 原理与实现：Window 机制.note.md
related: [flink-流批一体入门]
created: 2026-07-05
updated: 2026-07-05
---

htp:/wuchong.me/blog/2016/05/25/flink-internals-window-mechanism/

Flink 认为 Batch 是 Streaming 的⼀个特例，所以 Flink 底层引擎是⼀个流式引擎，在上⾯实现了流处 理和批处理。⽽窗⼝（window）就是从 Streaming 到 Batch 的⼀个桥梁。Flink 提供了⾮常完善的窗 ⼝机制，这是我认为的 Flink 最⼤的亮点之⼀（其他的亮点包括消息乱序处理，和 checkpoint 机 制）。本⽂我们将介绍流式处理中的窗⼝概念，介绍 Flink 内建的⼀些窗⼝和 Window API，最后讨论 下窗⼝在底层是如何实现的。

# 什么是 Window

在流处理应⽤中，数据是连续不断的，因此我们不可能等到所有数据都到了才开始处理。当然我们可 以每来⼀个消息就处理⼀次，但是有时我们需要做⼀些聚合类的处理，例如：在过去的1分钟内有多少 ⽤户点击了我们的⽹⻚。在这种情况下，我们必须定义⼀个窗⼝，⽤来收集最近⼀分钟内的数据，并 对这个窗⼝内的数据进⾏计算。 窗⼝可以是时间驱动的（Time Window，例如：每30秒钟），也可以是数据驱动的（Count Window，例如：每⼀百个元素）。⼀种经典的窗⼝分类可以分成：翻滚窗⼝（Tumbling Window，⽆ 重叠），滚动窗⼝（Sliding Window，有重叠），和会话窗⼝（Sesion Window，活动间隙）。 我们举个具体的场景来形象地理解不同窗⼝的概念。假设，淘宝⽹会记录每个⽤户每次购买的商品个 数，我们要做的是统计不同窗⼝中⽤户购买商品的总数。下图给出了⼏种经典的窗⼝切分概述图：

![image 1](assets/imageFile1.png)

上图中，raw data stream 代表⽤户的购买⾏为流，圈中的数字代表该⽤户本次购买的商品个数，事件 是按时间分布的，所以可以看出事件之间是有time gap的。Flink 提供了上图中所有的窗⼝类型，下⾯ 我们会逐⼀进⾏介绍。

Time Window

就如名字所说的，Time Window 是根据时间对数据流进⾏分组的。这⾥我们涉及到了流处理中的时间 问题，时间问题和消息乱序问题是紧密关联的，这是流处理中现存的难题之⼀，我们将在后续 的 EventTime 和消息乱序处理中对这部分问题进⾏深⼊探讨。这⾥我们只需要知道 Flink 提出了三种 时间的概念，分别是event time（事件时间：事件发⽣时的时间），ingestion time（摄取时间：事件 进⼊流处理系统的时间），procesing time（处理时间：消息被计算处理的时间）。Flink 中窗⼝机制 和时间类型是完全解耦的，也就是说当需要改变时间类型时不需要更改窗⼝逻辑相关的代码。

Tumbling Time Window

如上图，我们需要统计每⼀分钟中⽤户购买的商品的总数，需要将⽤户的⾏为事件按每⼀分钟进⾏切 分，这种切分被成为翻滚时间窗⼝（Tumbling Time Window）。翻滚窗⼝能将数据流切分成不重叠的 窗⼝，每⼀个事件只能属于⼀个窗⼝。通过使⽤ DataStream API，我们可以这样实现：

<table>
  <tr>
    <th>/ Stream of (userId, buyCnt) val buyCnts: DataStream[(Int,Int)] = . val tumblingCnts: DataStream[(Int, Int)] = buyCnts<br><br>/ key stream by userId<br><br>.keyBy(0)<br><br>/ tumbling time window of 1 minute length<br><br>.timeWindow(Time.minutes(1))<br><br>/ compute sum over buyCnt<br><br></th>
  </tr>
</table>


.sum(1)

Sliding Time Window

但是对于某些应⽤，它们需要的窗⼝是不间断的，需要平滑地进⾏窗⼝聚合。⽐如，我们可以每30秒 计算⼀次最近⼀分钟⽤户购买的商品总数。这种窗⼝我们称为滑动时间窗⼝（Sliding Time Window）。在滑窗中，⼀个元素可以对应多个窗⼝。通过使⽤ DataStream API，我们可以这样实 现：

<table>
  <tr>
    <th>val slidingCnts: DataStream[(Int, Int)] = buyCnts<br><br>.keyBy(0)<br><br>/ sliding time window of 1 minute length and 30 secs tri ger interval<br><br>.timeWindow(Time.minutes(1), Time.seconds(30)<br><br></th>
  </tr>
</table>


.sum(1)

## Count Window

Count Window 是根据元素个数对数据流进⾏分组的。

### Tumbling Count Window

当我们想要每10个⽤户购买⾏为事件统计购买总数，那么每当窗⼝中填满10个元素了，就会对窗⼝ 进⾏计算，这种窗⼝我们称之为翻滚计数窗⼝（Tumbling Count Window），上图所示窗⼝⼤⼩为3 个。通过使⽤ DataStream API，我们可以这样实现：

<table>
  <tr>
    <th>/ Stream of (userId, buyCnts) val buyCnts: DataStream[(Int, Int)] = . val tumblingCnts: DataStream[(Int, Int)] = buyCnts<br><br>/ key stream by sensorId<br><br>.keyBy(0)<br><br>/ tumbling count window of 10elements size<br><br>.countWindow(10) / compute the buyCnt sum<br><br></th>
  </tr>
</table>


.sum(1)

Sliding Count Window

当然Count Window 也⽀持 Sliding Window，虽在上图中未描述出来，但和Sliding Time Window含义 是类似的，例如计算每10个元素计算⼀次最近10个元素的总和，代码示例如下。

<table>
  <tr>
    <th>val slidingCnts: DataStream[(Int, Int)] = vehicleCnts<br><br>.keyBy(0)<br><br>/ sliding count window of 10 elements size and 10elementstri ger interval<br><br>.countWindow(10, 10)<br><br></th>
  </tr>
</table>


.sum(1)

Sesion Window

在这种⽤户交互事件流中，我们⾸先想到的是将事件聚合到会话窗⼝中（⼀段⽤户持续活跃的周 期），由⾮活跃的间隙分隔开。如上图所示，就是需要计算每个⽤户在活跃期间总共购买的商品数 量，如果⽤户30秒没有活动则视为会话断开（假设raw data stream是单个⽤户的购买⾏为流）。 Sesion Window 的示例代码如下：

<table>
  <tr>
    <th>/ Stream of (userId, buyCnts) val buyCnts: DataStream[(Int, Int)] = . val sesionCnts: DataStream[(Int,Int)] = vehicleCnts<br><br>.keyBy(0) / sesion window based on a30seconds sesion gap interval<br><br>.window(ProcesingTimeSesionWindows.withGap(Time.seconds(30)<br><br></th>
  </tr>
</table>


.sum(1)

⼀般⽽⾔，window 是在⽆限的流上定义了⼀个有限的元素集合。这个集合可以是基于时间的，元素个 数的，时间和个数结合的，会话间隙的，或者是⾃定义的。Flink 的 DataStream API 提供了简洁的算 ⼦来满⾜常⽤的窗⼝操作，同时提供了通⽤的窗⼝机制来允许⽤户⾃⼰定义窗⼝分配逻辑。下⾯我们 会对 Flink 窗⼝相关的 API 进⾏剖析。

# 剖析 Window API

得益于 Flink Window API 松耦合设计，我们可以⾮常灵活地定义符合特定业务的窗⼝。Flink 中定义⼀ 个窗⼝主要需要以下三个组件。

### Window Asigner：⽤来决定某个元素被分配到哪个/哪些窗⼝中去。

如下类图展示了⽬前内置实现的 Window Asigners：

![image 2](assets/imageFile2.png)

Tri ger：触发器。决定了⼀个窗⼝何时能够被计算或清除，每个窗⼝都会拥有⼀个⾃⼰的 Tri ger。

如下类图展示了⽬前内置实现的 Tri gers：

![image 3](assets/imageFile3.png)

Evictor：可以译为“驱逐者”。在Tri ger触发之后，在窗⼝被处理之前，Evictor（如果有Evictor的 话）会⽤来剔除窗⼝中不需要的元素，相当于⼀个filter。

如下类图展示了⽬前内置实现的 Evictors：

![image 4](assets/imageFile4.png)

上述三个组件的不同实现的不同组合，可以定义出⾮常复杂的窗⼝。Flink 中内置的窗⼝也都是基于这 三个组件构成的，当然内置窗⼝有时候⽆法解决⽤户特殊的需求，所以 Flink 也暴露了这些窗⼝机制的 内部接⼝供⽤户实现⾃定义的窗⼝。下⾯我们将基于这三者探讨窗⼝的实现机制。

# Window的实现

下图描述了 Flink 的窗⼝机制以及各组件之间是如何相互⼯作的。

![image 5](assets/imageFile5.png)

⾸先上图中的组件都位于⼀个算⼦（window operator）中，数据流源源不断地进⼊算⼦，每⼀个到达 的元素都会被交给 WindowAsigner。WindowAsigner 会决定元素被放到哪个或哪些窗⼝ （window），可能会创建新窗⼝。因为⼀个元素可以被放⼊多个窗⼝中，所以同时存在多个窗⼝是可 能的。注意，Window本身只是⼀个ID标识符，其内部可能存储了⼀些元数据，如TimeWindow中有开始和 结束时间，但是并不会存储窗⼝中的元素。窗⼝中的元素实际存储在 Key/Value State 中，key为 Window，value为元素集合（或聚合值）。为了保证窗⼝的容错性，该实现依赖了 Flink 的 State 机制 （参⻅ ）。 每⼀个窗⼝都拥有⼀个属于⾃⼰的 Tri ger，Tri ger上会有定时器，⽤来决定⼀个窗⼝何时能够被计算 或清除。每当有元素加⼊到该窗⼝，或者之前注册的定时器超时了，那么Tri ger都会被调⽤。Tri ger 的返回结果可以是 continue（不做任何操作），fire（处理窗⼝数据），purge（移除窗⼝和窗⼝中的 数据），或者 fire + purge。⼀个Tri ger的调⽤结果只是fire的话，那么会计算窗⼝并保留窗⼝原样， 也就是说窗⼝中的数据仍然保留不变，等待下次Tri ger fire的时候再次执⾏计算。⼀个窗⼝可以被重 复计算多次知道它被 purge 了。在purge之前，窗⼝会⼀直占⽤着内存。 当Tri ger fire了，窗⼝中的元素集合就会交给Evictor（如果指定了的话）。Evictor 主要⽤来遍历窗⼝ 中的元素列表，并决定最先进⼊窗⼝的多少个元素需要被移除。剩余的元素会交给⽤户指定的函数进 ⾏窗⼝的计算。如果没有 Evictor 的话，窗⼝中的所有元素会⼀起交给函数进⾏计算。 计算函数收到了窗⼝的元素（可能经过了 Evictor 的过滤），并计算出窗⼝的结果值，并发送给下游。 窗⼝的结果值可以是⼀个也可以是多个。DataStream API 上可以接收不同类型的计算函数，包括预定 义的sum(),min(),max()，还有 ReduceFunction，FoldFunction，还有WindowFunction。 WindowFunction 是最通⽤的计算函数，其他的预定义的函数基本都是基于该函数实现的。

state ⽂档

Flink 对于⼀些聚合类的窗⼝计算（如sum,min）做了优化，因为聚合类的计算不需要将窗⼝中的所有 数据都保存下来，只需要保存⼀个result值就可以了。每个进⼊窗⼝的元素都会执⾏⼀次聚合函数并修 改result值。这样可以⼤⼤降低内存的消耗并提升性能。但是如果⽤户定义了 Evictor，则不会启⽤对 聚合窗⼝的优化，因为 Evictor 需要遍历窗⼝中的所有元素，必须要将窗⼝中所有元素都存下来。

# 源码分析

上述的三个组件构成了 Flink 的窗⼝机制。为了更清楚地描述窗⼝机制，以及解开⼀些疑惑（⽐如 purge 和 Evictor 的区别和⽤途），我们将⼀步步地解释 Flink 内置的⼀些窗⼝（Time Window， Count Window，Sesion Window）是如何实现的。

Count Window 实现

Count Window 是使⽤三组件的典范，我们可以在 KeyedStream 上创建 Count Window，其源码如下所 示：

<table>
  <tr>
    <th>/ tumbling count window public WindowedStream<T,KEY, GlobalWindow> countWindow(long size) {<br><br>return window(GlobalWindows.create() / create window stream using GlobalWindows<br><br>.tri ger(PurgingTri ger.of(CountTri ger.of(size); / tri geris window size }<br><br>/ sliding count window public WindowedStream<T,KEY, GlobalWindow> countWindow(long size, long slide) {<br><br>return window(GlobalWindows.create()<br><br>.evictor(CountEvictor.of(size) / evictor is window size<br><br>.tri ger(CountTri ger.of(slide); / tri ger is slide size<br><br></th>
  </tr>
</table>


}

第⼀个函数是申请翻滚计数窗⼝，参数为窗⼝⼤⼩。第⼆个函数是申请滑动计数窗⼝，参数分别为窗 ⼝⼤⼩和滑动⼤⼩。它们都是基于 GlobalWindows 这个 WindowAsigner 来创建的窗⼝，该asigner会 将所有元素都分配到同⼀个global window中，所有GlobalWindows的返回值⼀直是 GlobalWindow 单 例。基本上⾃定义的窗⼝都会基于该asigner实现。 翻滚计数窗⼝并不带evictor，只注册了⼀个tri ger。该tri ger是带purge功能的 CountTri ger。也就 是说每当窗⼝中的元素数量达到了 window-size，tri ger就会返回fire+purge，窗⼝就会执⾏计算并清 空窗⼝中的所有元素，再接着储备新的元素。从⽽实现了tumbling的窗⼝之间⽆重叠。 滑动计数窗⼝的各窗⼝之间是有重叠的，但我们⽤的 GlobalWindows asinger 从始⾄终只有⼀个窗 ⼝，不像 sliding time asigner 可以同时存在多个窗⼝。所以tri ger结果不能带purge，也就是说计算 完窗⼝后窗⼝中的数据要保留下来（供下个滑窗使⽤）。另外，tri ger的间隔是slide-size，evictor的 保留的元素个数是window-size。也就是说，每个滑动间隔就触发⼀次窗⼝计算，并保留下最新进⼊窗 ⼝的window-size个元素，剔除旧元素。 假设有⼀个滑动计数窗⼝，每2个元素计算⼀次最近4个元素的总和，那么窗⼝⼯作示意图如下所示：

![image 6](assets/imageFile6.png)

图中所示的各个窗⼝逻辑上是不同的窗⼝，但在物理上是同⼀个窗⼝。该滑动计数窗⼝，tri ger的触 发条件是元素个数达到2个（每进⼊2个元素就会触发⼀次），evictor保留的元素个数是4个，每次计 算完窗⼝总和后会保留剩余的元素。所以第⼀次触发tri ger是当元素5进⼊，第三次触发tri ger是当元 素2进⼊，并驱逐5和2，计算剩余的4个元素的总和（ 2）并发送出去，保留下2,4,9,7元素供下个逻辑 窗⼝使⽤。

## Time Window 实现

同样的，我们也可以在 KeyedStream 上申请 Time Window，其源码如下所示：

<table>
  <tr>
    <th>/ tumbling time window<br><br>public WindowedStream<T, KEY,TimeWindow> timeWindow(Time size) { if (environment.getStreamTimeCharacteristic() = TimeCharacteristic.ProcesingTime) {<br><br>return window(TumblingProcesingTimeWindows.of(size); } else {<br><br>return window(TumblingEventTimeWindows.of(size); }<br><br>} / sliding time window<br><br>public WindowedStream<T, KEY,TimeWindow> timeWindow(Time size, Time slide){ if (environment.getStreamTimeCharacteristic() = TimeCharacteristic.ProcesingTime) {<br><br>return window(SlidingProcesingTimeWindows.of(size, slide); } else {<br><br>return window(SlidingEventTimeWindows.of(size, slide); }<br><br></th>
  </tr>
</table>


}

在⽅法体内部会根据当前环境注册的时间类型，使⽤不同的WindowAsigner创建window。可以看 到，EventTime和IngestTime都使⽤了XXXEventTimeWindows这个asigner，因为EventTime和 IngestTime在底层的实现上只是在Source处为Record打时间戳的实现不同，在window operator中的 处理逻辑是⼀样的。 这⾥我们主要分析sliding proces time window，如下是相关源码：

<table>
  <tr>
    <th>publicclasSlidingProcesingTimeWindowsextendsWindowAsigner<Object,TimeWindow> { private static final long serialVersionUID = 1L;<br><br>private final long size;<br><br>private final long slide;<br><br>private SlidingProcesingTimeWindows(long size,long slide) { this.size = size; this.slide = slide;<br><br>}<br><br>@Overide public Colection<TimeWindow> asignWindows(Object element, long timestamp) {<br><br>timestamp = System.curentTimeMilis(); List<TimeWindow> windows = new ArayList<>(int) (size / slide);<br><br>/对⻬时间戳 long lastStart = timestamp - timestamp % slide; for (long start = lastStart;<br><br>start> timestamp - size; start-= slide) {<br><br>/ 当前时间戳对应了多个window windows.ad(new TimeWindow(start, start + size);<br><br>} return windows;<br><br>}<br><br>.<br><br>} publicclasProcesingTimeTri ger extends Tri ger<Object, TimeWindow>{<br><br>@Overide / 每个元素进⼊窗⼝都会调⽤该⽅法<br><br>public Tri gerResult onElement(Object element,long timestamp,TimeWindow window, Tri gerContext ctx){<br><br>/注册定时器，当系统时间到达window end timestamp时会回调该tri ger的onProcesingTime⽅法 ctx.registerProcesingTimeTimer(window.getEnd(); return Tri gerResult.CONTINUE;<br><br>}<br><br>@Overide / 返回结果表示执⾏窗⼝计算并清空窗⼝<br><br>public Tri gerResult onProcesingTime(long time, TimeWindow window, Tri gerContext ctx) {<br><br>return Tri gerResult.FIRE_AND_PURGE; }<br><br>.<br><br></th>
  </tr>
</table>


}

⾸先，SlidingProcessingTimeWindows会对每个进⼊窗⼝的元素根据系统时间分配到(size / slide)个 不同的窗⼝，并会在每个窗⼝上根据窗⼝结束时间注册⼀个定时器（相同窗⼝只会注册⼀份），当定 时器超时时意味着该窗⼝完成了，这时会回调对应窗⼝的Tri ger的onProcessingTime⽅法，返回 FIRE_AND_PURGE，也就是会执⾏窗⼝计算并清空窗⼝。整个过程示意图如下：

![image 7](assets/imageFile7.png)

如上图所示横轴代表时间戳（为简化问题，时间戳从0开始），第⼀条record会被分配到[-5,5)和[0,10) 两个窗⼝中，当系统时间到5时，就会计算[-5,5)窗⼝中的数据，并将结果发送出去，最后清空窗⼝中 的数据，释放该窗⼝资源。

## Sesion Window 实现

Sesion Window 是⼀个需求很强烈的窗⼝机制，但Sesion也⽐之前的Window更复杂，所以 Flink 也 是在即将到来的 1.1.0 版本中才⽀持了该功能。由于篇幅问题，我们将在后续的 Sesion Window 的实 现 中深⼊探讨 Sesion Window 的实现。
