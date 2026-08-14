---
title: RxJava 从入门到放弃再到不离不弃.note（原文插图 annex）
slug: annex-RxJava-从入门到放弃再到不离不弃
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/MicroServer/SpringCloud/Hystrix/RxJava/RxJava 从入门到放弃再到不离不弃.note.md
related: [dubbo-调用原理与分层]
created: 2026-07-05
updated: 2026-07-05
---

# 作者寄语

很久之前就想写⼀个专题，专写Android开发框架，专题的名字叫 X 从⼊⻔到放弃 ，沉淀了这么久，看过 ⽹络诸多⼤神的博客，静下⼼来开始写这个专题，为什么叫⼊⻔到放弃呢；相信⼤家学习新框架的时候， 尤其是像Rxjava或者Dager等等这种新的编程思想；需要⼀定的阅读理解能⼒和思维逻辑；那么本专题 旨在帮助⼤家不要太过急功近利，不要被冗⻓的代码和⽂章，晦涩的思想所打败，相信⼤家只要坚持 看完，⼀定会有所收获的；废话不多说，那么这个专题开篇就以RxJava来讲吧，预计后⾯还会有⼏篇⼤ 型框架的讲解，想想还有点⼩激动； 友情提示：⽂章较⻓，请耐⼼看完；

![image 1](assets/imageFile1.png)

# 前⾔

RxJava等编程思想正在Android开发者中变的越来越流⾏。唯⼀的问题就是上⼿不容易，尤其是⼤部分⼈ 之前都是使⽤命令式编程语⾔。 ⾸先要先理清这么⼀个问题：Rxjava和我们平时写的程序有什么不同。相信稍微对Rxjava有点认知的朋 友都会深深感受到⽤这种⽅式写的程序和我们⼀般写的程序有很明显的不同。我们⼀般写的程序 统称 为命令式程序，是以流程为核⼼的，每⼀⾏代码实际上都是机器实际上要执⾏的指令。⽽Rxjava这样的编 程⻛格，称为函数响应式编程。函数响应式编程是以数据流为核⼼，处理数据的输⼊，处理以及输出的。 这种思路写出来的代码就会跟机器实际执⾏的指令⼤相径庭。所以对于已经习惯命令式编程的我们来 说，刚开始接触Rxjava的时候必然会很不适应，⽽且也不太符合我们平时的思维习惯。但是久⽽久之你 会发现这个框架的精髓，尤其是你运⽤到⼤项⽬中的时候，简直爱不释⼿，随着程序逻辑变得越来越 复杂，它依然能够保持代码简洁。

# RxJava是什么

a library for composing asynchronous and event-based programs using observable sequences for the Java VM 解释：⼀个对于构成使⽤的Java虚拟机观察序列异步和基于事件的程序库

RxJava 是⼀个响应式编程框架，采⽤观察者设计模式。所以⾃然少不了 Observable 和 Subscriber 这 两个东东了。 RxJava 是⼀个开源项⽬，地址： RxAndroid，⽤于 Android 开发，添加了 Android ⽤的接⼝。地址：

htps:/github.com/ReactiveX/RxJava

htps:/github.com/ReactiveX/Rx Android

# 基本概念

⽹上关于RxJava的博⽂也有很多，我也看过许多，其中不乏有优秀的⽂章，但绝⼤部分⽂章都有⼀ 个共同点，就是侧重于讲RxJava中各种强⼤的操作符，⽽忽略了最基本的东⻄⸺概念，所以⼀开 始我也看的⼀脸懵逼，看到后⾯⼜忘了前⾯的，脑⼦⾥全是问号，这个是什么，那个⼜是什么，这 两个⻓得怎么那么像。举个不太恰当的例⼦，概念之于初学者，就像⻝物之于⼈，当你饿了，你会 想吃⾯包、⽜奶，那你为什么不去吃⼟呢，因为你知道⾯包⽜奶是⽤来⼲嘛的，⼟是⽤来⼲嘛的。 同理，前⾯已经说过，RxJava⽆⾮是发送数据与接收数据，那么什么是发射源，什么是接收源，这 就是你应该明确的事，也是RxJava的⼊⻔条件之⼀，下⾯就依我个⼈理解，对发射源和接收源做个 归类，以及RxJava中频繁出现的⼏个“单词”解释⼀通;

Observable：发射源，英⽂释义“可观察的”，在观察者模式中称为“被观察者”或“可观察对象”； Observer：接收源，英⽂释义“观察者”，没错！就是观察者模式中的“观察者”，可接收Observable、 Subject发射的数据； Subject：Subject是⼀个⽐较特殊的对象，既可充当发射源，也可充当接收源，为避免初学者被混 淆，本章将不对Subject做过多的解释和使⽤，重点放在Observable和Observer上，先把最基本⽅法的 使⽤学会，后⾯再学其他的都不是什么问题； Subscriber：“订阅者”，也是接收源，那它跟Observer有什么区别呢？Subscriber实现了Observer接 ⼝，⽐Observer多了⼀个最重要的⽅法unsubscribe( )，⽤来取消订阅，当你不再想接收数据了， 可以调⽤unsubscribe( )⽅法停⽌接收，Observer 在 subscribe() 过程中,最终也会被转换成 Subscriber 对象，⼀般情况下，建议使⽤Subscriber作为接收源； Subscription ：Observable调⽤subscribe( )⽅法返回的对象，同样有unsubscribe( )⽅法，可以⽤来取消订 阅事件； Action0：RxJava中的⼀个接⼝，它只有⼀个⽆参cal（）⽅法，且⽆返回值，同样还有Action1， Action2…Action9等，Action1封装了含有 1 个参的cal（）⽅法，即cal（T t），Action2封装了含 有 2 个参数的cal⽅法，即cal（T1 t1，T2 t2），以此类推； Func0：与Action0⾮常相似，也有cal（）⽅法，但是它是有返回值的，同样也有Func0、Func1… Func9;

RxJava最核⼼的两个东⻄是Observables（被观察者，事件源）和Subscribers（观察者）。Observables发 出⼀系列事件，Subscribers处理这些事件。这⾥的事件可以是任何你感兴趣的东⻄（触摸事件，web接 ⼝调⽤返回的数据…） ⼀个Observable可以发出零个或者多个事件，知道结束或者出错。每发出⼀个事件，就会调⽤它的 Subscriber的onNext⽅法，最后调⽤Subscriber.onNext()或者Subscriber.onEror()结束。 Rxjava的看起来很想设计模式中的观察者模式，但是有⼀点明显不同，那就是如果⼀个Observerble没有任 何的的Subscriber，那么这个Observable是不会发出任何事件的。

# 基本⽤法

Observable的创建

使⽤create( ),最基本的创建⽅式：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br></th>
    <th>Observable<String> myObservable = Observable.create(new Observable.OnSubscribe<String> () {<br><br>@Overide public void cal(Subscriber<? super String> subscriber) { subscriber.onNext("Helo, world!"); /发射⼀个"Helo,<br><br>world!"的String<br><br>subscriber.onCompleted();/发射完成,这种⽅法需要⼿动调 ⽤onCompleted，才会回调Observer的onCompleted⽅法</th>
  </tr>
</table>


});

可以看到，这⾥传⼊了⼀个 OnSubscribe 对象作为参数。OnSubscribe 会被存储在返回的 Observable 对象 中，它的作⽤相当于⼀个计划表，当 Observable 被订阅的时候，OnSubscribe 的 cal() ⽅法会⾃动被调 ⽤，事件序列就会依照设定依次触发（对于上⾯的代码，就是观察者Subscriber将会被调⽤⼀次 onNext() 和⼀次 onCompleted()）。这样，由被观察者调⽤了观察者的回调⽅法，就实现了由被观察 者向观察者的事件传递，即观察者模式。 这个例⼦很简单：事件的内容是字符串，⽽不是⼀些复杂的对象；事件的内容是已经定好了的，⽽不 像有的观察者模式⼀样是待确定的（例如⽹络请求的结果在请求返回之前是未知的）；所有事件在⼀ 瞬间被全部发送出去，⽽不是夹杂⼀些确定或不确定的时间间隔或者经过某种触发器来触发的。总 之，这个例⼦看起来毫⽆实⽤价值。但这是为了便于说明，实质上只要你想，各种各样的事件发送规 则你都可以⾃⼰来写。⾄于具体怎么做，后⾯都会讲到，但现在不⾏。只有把基础原理先说明⽩了， 上层的运⽤才能更容易说清楚。

Subscriber的创建

上⾯定义的Observable对象仅仅发出⼀个HeloWorld字符串，然后就结束了。接着我们创建⼀个 Subscriber来处理Observable对象发出的字符串：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12</th>
    <th>Subscriber<String> mySubscriber = new Subscriber<String>() {<br><br>@Overide public void onNext(String s) { System.out.println(s); /打印出"Helo, world!" }<br><br>@Overide public void onCompleted() { }<br><br>@Overide public void onEror(Throwable e) { }</th>
  </tr>
</table>


};

除 了 Observer 接 ⼝ 之 外 ， RxJava还 内 置 了 ⼀ 个 实 现 了 Observer 的 抽 象 类 ： Subscriber。 Subscriber 对 Observer 接⼝进⾏了⼀些扩展，但他们的基本使⽤⽅式是完全⼀样的：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12</th>
    <th>Observer<String> myObserver = new Observer<String>() {<br><br>@Overide public void onNext(String s) { System.out.println(s); /打印出"Helo, world!" }<br><br>@Overide public void onCompleted() { }<br><br>@Overide public void onEror(Throwable e) { }</th>
  </tr>
</table>


};

不仅基本使⽤⽅式⼀样，实质上，在 RxJava的 subscribe过程中，Observer 也总是会先被转换成⼀ 个 Subscriber 再使⽤。所以如果你只想使⽤基本功能，选择 Observer 和 Subscriber 是完全⼀样的。它们的 区别对于使⽤者来说主要有两点：

- 1.
- 2.


onStart(): 这是 Subscriber 增加的⽅法。它会在 subscribe 刚开始，⽽事件还未发送之前被调⽤，可 以⽤于做⼀些准备⼯作，例如数据的清零或重置。这是⼀个可选⽅法，默认情况下它的实现为 空。需要注意的是，如果对准备⼯作的线程有要求（例如弹出⼀个显示进度的对话框，这必须在 主线程执⾏），onStart() 就不适⽤了，因为它总是在 subscribe 所发⽣的线程被调⽤，⽽不能指定 线程。要在指定的线程来做准备⼯作，可以使⽤ doOnSubscribe() ⽅法，具体可以在后⾯的⽂中看 到。 unsubscribe(): 这是 Subscriber 所实现的另⼀个接⼝ Subscription 的⽅法，⽤于取消订阅。在这个⽅法 被调⽤后，Subscriber 将不再接收事件。⼀般在这个⽅法调⽤前，可以使⽤ isUnsubscribed() 先判断 ⼀下状态。 unsubscribe()这个⽅法很重要，因为在 subscribe() 之后， Observable 会持 有 Subscriber 的引⽤，这个引⽤如果不能及时被释放，将有内存泄露的⻛险。所以最好保持⼀个原 则：要在不再使⽤的时候尽快在合适的地⽅（例如 onPause() onStop() 等⽅法中）调⽤ unsubscribe() 来解除引⽤关系，以避免内存泄露的发⽣。

Observable与Subscriber的关联

这⾥subscriber仅仅就是打印observable发出的字符串。通过subscribe函 数 就 可 以 将 我 们 定 义 的 myObservable对象和mySubscriber对象关联起来，这样就完成了subscriber对observable的订阅。 myObservable.subscribe(myObserver);

/ 或者： myObservable.subscribe(mySubscriber); ⼀旦mySubscriber订阅了myObservable，myObservable就是调⽤mySubscriber对象的onNext和onComplete⽅ 法，mySubscriber 就会打印出Helo World！

## 订阅（Subscriptions）

当调⽤Observable.subscribe()，会返回⼀个Subscription对象。这个对象代表了被观察者和订阅者之间的联 系。

<table>
  <tr>
    <th>1<br>2<br></th>
    <th>Subscription subscription = Observable.just("Helo, World!")</th>
  </tr>
</table>


.subscribe(s -> System.out.println(s);

你可以在后⾯使⽤这个Subscription对象来操作被观察者和订阅者之间的联系.

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th>subscription.unsubscribe();/接触订阅关系 System.out.println("Unsubscribed=" + subscription.isUnsubscribed();</th>
  </tr>
</table>


/ Outputs "Unsubscribed=true"

RxJava的另外⼀个好处就是它处理unsubscribing的时候，会停⽌整个调⽤链。如果你使⽤了⼀串很复杂 的操作符，调⽤unsubscribe将会在他当前执⾏的地⽅终⽌。不需要做任何额外的⼯作！

## 简化代码（Observable与Subscriber）

简化Observable： 是不是觉得仅仅为了打印⼀个helo world要写这么多代码太啰嗦？我这⾥主要是为了展示RxJava背后 的原理⽽采⽤了这种⽐较啰嗦的写法，RxJava其实提供了很多便捷的函数来帮助我们减少代码。 ⾸先来看看如何简化Observable对象的创建过程。RxJava内置了很多简化创建Observable对象的函数，⽐ 如Observable.just就是⽤来创建只发出⼀个事件就结束的Observable对象，上⾯创建Observable对象的代码 可以简化为⼀⾏： Observable<String> myObservable = Observable.just("Helo, world!"); /发送"Helo, world!" 其他⽅法：

- 1.使⽤just( )，将为你创建⼀个Observable并⾃动为你调⽤onNext( )发射数据： justObservable = Observable.just("just1","just2");/依次发送"just1"和"just2"

- 2.使⽤from( )，遍历集合，发送每个item：


List<String> list = new ArayList<>(); list.ad("from1"); list.ad("from2"); list.ad("from3"); fromObservable = Observable.from(list); /遍历list 每次发送⼀个 /* 注意，just()⽅法也可以传list，但是发送的是整个list对象，⽽from（）发送的是list的⼀个item* /

- 3.使⽤defer( )，有观察者订阅时才创建Observable，并且为每个观察者创建⼀个新的Observable： deferObservable = Observable.defer(new Func0<Observable<String>() {

@Overide

/注意此处的cal⽅法没有Subscriber参数 public Observable<String> cal() {

return Observable.just("deferObservable"); });

- 4.使⽤interval( ),创建⼀个按固定时间间隔发射整数序列的Observable，可⽤作定时器： intervalObservable = Observable.interval(1, TimeUnit.SECONDS);/每隔⼀秒发送⼀次

- 5.使⽤range( ),创建⼀个发射特定整数序列的Observable，第⼀个参数为起始值，第⼆个为发送的个 数，如果为0则不发送，负数则抛异常： rangeObservable = Observable.range(10, 5);/将发送整数10， 1，12，13，14

- 6.使⽤timer( ),创建⼀个Observable，它在⼀个给定的延迟后发射⼀个特殊的值，等同于Android中 Handler的postDelay( )⽅法： timeObservable = Observable.timer(3, TimeUnit.SECONDS); /3秒后发射⼀个值

- 7.使⽤repeat( ),创建⼀个重复发射特定数据的Observable: repeatObservable = Observable.just("repeatObservable").repeat(3);/重复发射3次 简化Subscriber： 接下来看看如何简化Subscriber，上⾯的例⼦中，我们其实并不关⼼OnComplete和OnEror，我们只 需要在onNext的时候做⼀些处理，这时候就可以使⽤Action1类。


<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br></th>
    <th>Action1<String> onNextAction = new Action1<String>() { @Overide public void cal(String s) { System.out.println(s); }</th>
  </tr>
</table>


6 };

subscribe⽅法有⼀个重载版本，接受三个Action1类型的参数，分别对应OnNext，OnComplete， OnEror函数: myObservable.subscribe(onNextAction, onErorAction, onCompleteAction); 这⾥我们并不关⼼onEror和onComplete，所以只需要第⼀个参数就可以 myObservable.subscribe(onNextAction);

/ Outputs "Helo, world!"

上⾯的代码最终可以写成这样:

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br></th>
    <th>Observable.just("Helo, world!")<br><br>.subscribe(new Action1<String>() { @Overide public void cal(String s) { System.out.println(s); }</th>
  </tr>
</table>


7 });

使⽤java8的 可以使代码更简洁: 不熟悉Lambda的可以看我之前写的：

lambda

Java8之Lambda表达式(Android⽤法)

<table>
  <tr>
    <th>1</th>
    <th>Observable.just("Helo, world!")</th>
  </tr>
</table>


2 .subscribe(s -> System.out.println(s);

简单解释⼀下这段代码中出现的 Action1和 Action0。 Action0是 RxJava的⼀个接⼝，它只有⼀个⽅ 法 cal()，这个⽅法是⽆参⽆返回值的；由于 onCompleted() ⽅法也是⽆参⽆返回值的，因此 Action0 可以 被当成⼀个包装对象，将 onCompleted() 的内容打包起来将⾃⼰作为⼀个参数传⼊ subscribe() 以实现不 完整定义的回调。这样其实也可以看做将onCompleted() ⽅法作为参数传进了 subscribe()，相当于其他 某些语⾔中的『闭包』。 Action1 也是⼀个接⼝，它同样只有⼀个⽅法 cal(T param)，这个⽅法也⽆返 回值，但有⼀个参数；与 Action0 同理，由于 onNext(T obj) 和 onEror(Throwable eror)也是单参数⽆返回值 的，因此 Action1 可以将 onNext(obj) 和 onEror(eror) 打包起来传⼊ subscribe() 以实现不完整定义的 回调。事实上，虽然 Action0 和 Action1 在 API 中使⽤最⼴泛，但 RxJava 是提供了多个 ActionX 形式的 接⼝ (例如 Action2, Action3) 的，它们可以被⽤以包装不同的⽆返回值的⽅法。 注：正如前⾯所提到的，Observer 和 Subscriber 具有相同的⻆⾊，⽽且 Observer 在 subscribe() 过程中最 终会被转换成Subscriber 对象，因此，从这⾥开始，后⾯的描述我将⽤ Subscriber 来代替 Observer ，这 样更加严谨。

# 操作符(Operators)

操作符就是为了解决对Observable对象的 变换(关键词) 的问题，操作符⽤于在Observable和最终的 Subscriber之间修改Observable发出的事件。RxJava提供了很多很有⽤的操作符。 ⽐如map操作符，就是⽤来把把⼀个事件转换为另⼀个事件的。

map()操作符：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br><br><br>12<br>13<br></th>
    <th>Observable.just("images/logo.png")/ 输⼊类型 String<br><br>.map(new Func1<String, Bitmap>() { @Overide public Bitmap cal(String filePath) {/ 参数类型 String return getBitmapFromPath(filePath); / 返回类型 Bitmap } })<br><br>.subscribe(new Action1<Bitmap>() { @Overide public void cal(Bitmap bitmap) {/ 参数类型 Bitmap showBitmap(bitmap); }</th>
  </tr>
</table>


});

使⽤lambda可以简化为:

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br></th>
    <th>Observable.just("images/logo.png")/ 输⼊类型 String<br><br>.map( filePath -> getBitmapFromPath(filePath); / 返回类型<br><br>Bitmap )<br><br>.subscribe( bitmap -> showBitmap(bitmap);</th>
  </tr>
</table>


);

可以看到，map() ⽅法将参数中的 String 对象转换成⼀个 Bitmap 对象后返回，⽽在经过 map() ⽅法后， 事件的参数类型也由String 转为了 Bitmap。这种直接变换对象并返回的，是最常⻅的也最容易理解的变 换。不过 RxJava的变换远不⽌这样，它不仅可以针对事件对象，还可以针对整个事件队列，这使 得 RxJava 变得⾮常灵活。

map()操作符进阶：

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th>Observable.just("Helo, world!")<br><br>.map(s -> s.hashCode()<br><br>.map(i -> Integer.toString(i)</th>
  </tr>
</table>


4 .subscribe(s -> System.out.println(s);

是不是很酷？map()操作符就是⽤于变换Observable对象的，map操作符返回⼀个Observable对象，这样就 可以实现链式调⽤，在⼀个Observable对象上多次使⽤map操作符，最终将最简洁的数据传递给 Subscriber对象。

## flatMap()操作符：

假设我有这样⼀个⽅法： 这个⽅法根据输⼊的字符串返回⼀个⽹站的url列表

Observable<List<String> query(String text);

Observable.flatMap()接收⼀个Observable的输出作为输⼊，同时输出另外⼀个Observable。直接看 代码：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br></th>
    <th>query("Helo, world!")<br><br>.flatMap(new Func1<List<String>, Observable<String><br><br>() { @Overide public Observable<String> cal(List<String> urls) { return Observable.from(urls); } })</th>
  </tr>
</table>


.subscribe(url -> System.out.println(url);

这⾥我贴出了整个的函数代码，以⽅便你了解发⽣了什么，使⽤lambda可以⼤⼤简化代码⻓度：

<table>
  <tr>
    <th>1<br>2<br></th>
    <th>query("Helo, world!")<br><br>.flatMap(urls -> Observable.from(urls)</th>
  </tr>
</table>


3 .subscribe(url -> System.out.println(url);

flatMap()是不是看起来很奇怪？为什么它要返回另外⼀个Observable呢？理解flatMap的关键点在于， flatMap输出的新的Observable正是我们在Subscriber想要接收的。现在Subscriber不再收到List<String>，⽽ 是收到⼀些列单个的字符串，就像Observable.from()的输出⼀样。

flatMap() 和map()有⼀个相同点：它也是把传⼊的参数转化之后返回另⼀个对象。但需要注意， 和 map() 不同的是，flatMap() 中返回的是个 Observable 对象，并且这个 Observable 对象并不是被直接发 送到了 Subscriber 的回调⽅法中。flatMap() 的原理是这样的：

- 1.
- 2.
- 3.


使⽤传⼊的事件对象创建⼀个 Observable 对象； 并不发送这个 Observable, ⽽是将它激活，于是它开始发送事件； 每⼀个创建出来的 Observable 发送的事件，都被汇⼊同⼀个 Observable ，⽽这个 Observable 负责将 这些事件统⼀交给Subscriber 的回调⽅法。这三个步骤，把事件拆成了两级，通过⼀组新创建 的 Observable 将初始的对象『铺平』之后通过统⼀路径分发了下去。⽽这个『铺平』就是 flatMap() 所 谓的 flat。

值得注意的是.from()是Observable创建时候⽤的，.flatMap()才是操作符；

## 其他操作符：

⽬前为⽌，我们已经接触了两个操作符，RxJava中还有更多的操作符，那么我们如何使⽤其他的操作 符来改进我们的代码呢？

更多RxJava的操作符请查看： getTitle()返回nul如果url不存在。我们不想输出”nul”，那么我们可以从返回的title列表中过滤掉nul 值！

RxJava操作符⼤全

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br></th>
    <th>query("Helo, world!")<br><br>.flatMap(urls -> Observable.from(urls)<br><br>.flatMap(url -> getTitle(url)<br><br>.filter(title -> title != nul)</th>
  </tr>
</table>


- 5 .subscribe(title -> System.out.println(title);

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br></th>
    <th>query("Helo, world!")<br><br>.flatMap(urls -> Observable.from(urls)<br><br>.flatMap(url -> getTitle(url)<br><br>.filter(title -> title != nul)<br><br>.take(5)</th>
  </tr>
</table>


- 6 .subscribe(title -> System.out.println(title);


filter()输出和输⼊相同的元素，并且会过滤掉那些不满⾜检查条件的。 如果我们只想要最多5个结果：

take()输出最多指定数量的结果。 如果我们想在打印之前，把每个标题保存到磁盘：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br></th>
    <th>query("Helo, world!")<br><br>.flatMap(urls -> Observable.from(urls)<br><br>.flatMap(url -> getTitle(url)<br><br>.filter(title -> title != nul)<br><br>.take(5)<br><br>.doOnNext(title -> saveTitle(title)</th>
  </tr>
</table>


7 .subscribe(title -> System.out.println(title);

doOnNext()允许我们在每次输出⼀个元素之前做⼀些额外的事情，⽐如这⾥的保存标题。 看到这⾥操作数据流是多么简单了么。你可以添加任意多的操作，并且不会搞乱你的代码。 RxJava包含了⼤量的操作符。操作符的数量是有点吓⼈，但是很值得你去挨个看⼀下，这样你可以知道 有哪些操作符可以使⽤。弄懂这些操作符可能会花⼀些时间，但是⼀旦弄懂了，你就完全掌握了RxJava 的威⼒。 感觉如何？ 好吧，你是⼀个怀疑主义者，并且还很难被说服，那为什么你要关⼼这些操作符呢？ 因为操作符可以让你对数据流做任何操作。 将⼀系列的操作符链接起来就可以完成复杂的逻辑。代码被分解成⼀系列可以组合的⽚段。这就是响应 式函数编程的魅⼒。⽤的越多，就会越多的改变你的编程思维。

# 线程控制(Scheduler)

假设你编写的Android ap需要从⽹络请求数据。⽹络请求需要花费较⻓的时间，因此你打算在另外⼀个 线程中加载数据。那么问题来了！ 编写多线程的Android应⽤程序是很难的，因为你必须确保代码在正确的线程中运⾏，否则的话可能会 导致ap崩溃。最常⻅的就是在⾮主线程更新UI。 在不指定线程的情况下， RxJava 遵循的是线程不变的原则，即：在哪个线程调⽤ subscribe()，就在哪个 线程⽣产事件；在哪个线程⽣产事件，就在哪个线程消费事件。如果需要切换线程，就需要⽤ 到 Scheduler （调度器）。 使⽤RxJava，你可以使⽤subscribeOn()指定观察者代码运⾏的线程，使⽤observerOn()指定订阅者运⾏的 线程

Scheduler 的 API

在RxJava 中，Scheduler ⸺调度器，相当于线程控制器，RxJava 通过它来指定每⼀段代码应该运⾏在 什么样的线程。RxJava已经内置了⼏个 Scheduler ，它们已经适合⼤多数的使⽤场景：

Schedulers.i mediate(): 直接在当前线程运⾏，相当于不指定线程。这是默认的 Scheduler。 Schedulers.newThread(): 总是启⽤新线程，并在新线程执⾏操作。 Schedulers.io(): I/O 操作（读写⽂件、读写数据库、⽹络信息交互等）所使⽤的 Scheduler。⾏为 模式和 newThread() 差不多，区别在于 io() 的内部实现是是⽤⼀个⽆数量上限的线程池，可以重⽤ 空闲的线程，因此多数情况下 io() ⽐ newThread() 更有效率。不要把计算⼯作放在 io() 中，可以避 免创建不必要的线程。

Schedulers.computation(): 计算所使⽤的 Scheduler。这个计算指的是 CPU 密集型计算，即不会 被 I/O 等操作限制性能的操作，例如图形的计算。这个 Scheduler 使⽤的固定的线程池，⼤⼩为 CPU 核数。不要把 I/O 操作放在 computation() 中，否则 I/O 操作的等待时间会浪费 CPU。 另外， Android 还有⼀个专⽤的 AndroidSchedulers.mainThread()，它指定的操作将在 Android 主 线程运⾏。

有了以上这⼏个 Scheduler ，就可以使⽤ subscribeOn() 和 observeOn() 两个⽅法来对线程进⾏控制了。

subscribeOn(): 指定 subscribe() 所发⽣的线程，即 Observable.OnSubscribe 被激活时所处的线程。或 者叫做事件产⽣的线程。 observeOn(): 指定 Subscriber 所运⾏在的线程。或者叫做事件消费的线程。

注意：observeOn() 指定的是 Subscriber 的线程，⽽这个 Subscriber 并不⼀定是 subscribe() 参数中 的 Subscriber（这块参考RxJava变换部分），⽽是 observeOn() 执⾏时的当前 Observable所对应 的 Subscriber ，即它的直接下级 Subscriber 。 换句话说，observeOn() 指定的是它之后的操作所在的线程。因此如果有多次切换线程的需求，只要在 每个想要切换线程的位置调⽤⼀次 observeOn() 即可。 代码示例：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br></th>
    <th>Observable.just(1, 2, 3, 4)<br><br>.subscribeOn(Schedulers.io()/ 指定 subscribe() 发⽣在 IO 线程<br><br>.observeOn(AndroidSchedulers.mainThread()/ 指定 Subscriber 的回调发⽣在主线程<br><br>.subscribe(new Action1<Integer>() { @Overide public void cal(Integer number) { Log.d(tag, "number:" + number); }</th>
  </tr>
</table>


});

上⾯这段代码中，由于 subscribeOn(Schedulers.io() 的指定，被创建的事件的内容 1、2、3、4将会 在 IO 线程发出； ⽽由于 observeOn(AndroidScheculers.mainThread() 的指定，因此 subscriber 数字的打印将发⽣在主线程 。 事 实 上 ， 这 种 在 subscribe() 之 前 写 上 两 句 subscribeOn(Scheduler.io() 和 observeOn(AndroidSchedulers.mainThread() 的使⽤⽅式⾮常常⻅，它适⽤于多 数的 『后台线程取数据，主线程显示』的程序策略。 下⾯的实例，在Observable.OnSubscribe的cal()中模拟了⻓时间获取数据过程，在Subscriber的noNext()中显 示数据到UI。

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br>23<br>24<br>25<br>26<br>27<br>28<br>29<br>30<br>31<br>32<br><br><br>3<br></th>
    <th>Observable.create(new Observable.OnSubscribe<String> () { @Overide public void cal(Subscriber<? super String> subscriber) { subscriber.onNext("info1");<br><br>SystemClock.sl ep(2 0);<br><br>subscriber.onNext("info2-sl ep 2s");<br><br>SystemClock.sl ep(3 0);<br><br>subscriber.onNext("info2-sl ep 3s");<br><br><br><br><br>SystemClock.sl ep(5 0); subscriber.onCompleted(); } })<br><br>.subscribeOn(Schedulers.io()/指定 subscribe() 发⽣在 IO 线程<br><br>.observeOn(AndroidSchedulers.mainThread()/指定 Subscriber 的回调发⽣在主线程<br><br>.subscribe(new Subscriber<String>() { @Overide public void onCompleted() { Log.v(TAG, "onCompleted()"); }<br><br>@Overide public void onEror(Throwable e) { Log.v(TAG, "onEror() e=" + e); }<br><br>@Overide public void onNext(String s) { showInfo(s); /UI view显示数据 }</th>
  </tr>
</table>


});

⾄此，我们可以看到cal()将会发⽣在 IO 线程，⽽showInfo(s)则被设定在了主线程。这就意味着，即使加 载cal()耗费了⼏⼗甚⾄⼏百毫秒的时间，也不会造成丝毫界⾯的卡顿。 值得注意：subscribeOn () 与 observeOn()都会返回了⼀个新的Observable，因此若不是采⽤上⾯这种直接 流⽅式，⽽是分步调⽤⽅式，需要将新返回的Observable赋给原来的Observable，否则线程调度将不会起 作⽤。

![image 2](assets/imageFile2.png)

使⽤下⾯⽅式，最后发现“OnSubscribe”还是在默认线程中运⾏；原因是subscribeOn这类操作后，返 回的是⼀个新的Observable。

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th>observable.subscribeOn(Schedulers.io(); observable.observeOn(AndroidSchedulers.mainThread() ;</th>
  </tr>
</table>


observable .subscribe(subscribe);

可以修改为下⾯两种⽅式：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br></th>
    <th>observable = observable.subscribeOn(Schedulers.io(); observable = observable.observeOn(AndroidSchedulers.mainThread() ; observable .subscribe(subscribe);<br><br>/OR observable.subscribeOn(Schedulers.io()<br><br>.observeOn(AndroidSchedulers.mainThread()</th>
  </tr>
</table>


.subscribe(subscribe);

前⾯讲到了，可以利⽤ subscribeOn() 结合 observeOn() 来实现线程控制，让事件的产⽣和消费发⽣在不 同的线程。可是在了解了 map()flatMap() 等变换⽅法后，有些好事的（其实就是当初刚接触 RxJava 时 的我）就问了：能不能多切换⼏次线程？ 答案是：能。 因为 observeOn() 指定的是 Subscriber 的线程，⽽这个 Subscriber 并不是（严格说应该为『不⼀定是』， 但 这 ⾥ 不 妨 理 解 为 『 不 是 』 ） subscribe() 参数中的 Subscriber ，⽽是 observeOn() 执⾏时的当 前 Observable 所对应的 Subscriber ，即它的直接下级 Subscriber 。换句话说，observeOn() 指定的是它之 后的操作所在的线程。因此如果有多次切换线程的需求，只要在每个想要切换线程的位置调⽤⼀ 次 observeOn() 即可。上代码：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br></th>
    <th>Observable.just(1, 2, 3, 4)/ IO 线程，由 subscribeOn() 指 定<br><br>.subscribeOn(Schedulers.io()<br><br>.observeOn(Schedulers.newThread()<br><br>.map(mapOperator)/ 新线程，由 observeOn() 指定<br><br>.observeOn(Schedulers.io()<br><br>.map(mapOperator2)/ IO 线程，由 observeOn() 指定<br><br>.observeOn(AndroidSchedulers.mainThread) .subscribe(subscriber); / Android 主线程，由<br><br>指定</th>
  </tr>
</table>


observeOn()

如上，通过 observeOn() 的多次调⽤，程序实现了线程的多次切换。 不过，不同于 observeOn() ， subscribeOn() 的位置放在哪⾥都可以，但它是只能调⽤⼀次的。 ⼜有好事的（其实还是当初的我）问了：如果我⾮要调⽤多次 subscribeOn() 呢？会有什么效果？ 这个问题先放着，我们还是从 RxJava 线程控制的原理说起吧。

## Scheduler 的原理

其实， subscribeOn() 和 observeOn() 的内部实现，也是⽤的 lift()。具体看图（不同颜⾊的箭头表示不同 的线程）： subscribeOn()原理图：

![image 3](assets/imageFile3.png)

observeOn() 原理图：

![image 4](assets/imageFile4.png)

从图中可以看出，subscribeOn() 和 observeOn() 都做了线程切换的⼯作（图中的 “schedule…” 部位）。 不同的是，subscribeOn() 的线程切换发⽣在 OnSubscribe 中，即在它通知上⼀级 OnSubscribe 时，这时事 件 还 没 有 开 始 发 送 ， 因 此 subscribeOn() 的线程控制可以从事件发出的开端就造成影响； ⽽ observeOn() 的线程切换则发⽣在它内建的 Subscriber 中，即发⽣在它即将给下⼀级 Subscriber 发送事 件时，因此 observeOn() 控制的是它后⾯的线程。 最后，我⽤⼀张图来解释当多个 subscribeOn() 和 observeOn() 混合使⽤时，线程调度是怎么发⽣的（由 于图中对象较多，相对于上⾯的图对结构做了⼀些简化调整）：

![image 5](assets/imageFile5.png)

图中共有 5 处含有对事件的操作。由图中可以看出，①和②两处受第⼀个 subscribeOn() 影响，运⾏在红 ⾊线程；③和④处受第⼀个 observeOn() 的影响，运⾏在绿⾊线程；⑤处受第⼆个 onserveOn() 影响，运 ⾏在紫⾊线程；⽽第⼆个 subscribeOn() ，由于在通知过程中线程就被第⼀个 subscribeOn() 截断，因此对 整个流程并没有任何影响。这⾥也就回答了前⾯的问题：当使⽤了多个 subscribeOn() 的时候，只有第 ⼀个 subscribeOn() 起作⽤。

## 延伸：doOnSubscribe()

doOnSubscribe()⼀般⽤于执⾏⼀些初始化操作. 然⽽，虽然超过⼀个的 subscribeOn() 对事件处理的流程没有影响，但在流程之前却是可以利⽤的。

在前⾯讲 Subscriber 的时候，提到过 Subscriber 的 onStart() 可以⽤作流程开始前的初始化。然 ⽽ onStart() 由于在subscribe() 发⽣时就被调⽤了，因此不能指定线程，⽽是只能执⾏在 subscribe() 被调 ⽤时的线程。这就导致如果 onStart()中含有对线程有要求的代码（例如在界⾯上显示⼀个 ProgresBar， 这必须在主线程执⾏），将会有线程⾮法的⻛险，因为有时你⽆法预测 subscribe() 将会在什么线程执 ⾏。 ⽽与 Subscriber.onStart() 相对应的，有⼀个⽅法 Observable.doOnSubscribe() 。它和 Subscriber.onStart() 同 样是在subscribe() 调⽤后⽽且在事件发送前执⾏，但区别在于它可以指定线程。默认情况 下， doOnSubscribe() 执⾏在 subscribe() 发⽣的线程；⽽如果在 doOnSubscribe() 之后有 subscribeOn() 的 话，它将执⾏在离它最近的 subscribeOn() 所指定的线程。 示例：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10 1<br></th>
    <th>Observable.create(onSubscribe)<br><br>.subscribeOn(Schedulers.io()<br><br>.doOnSubscribe(new Action0() { @Overide public void cal() { progresBar.setVisibility(View.VISIBLE); / 需要在主线程执<br><br>⾏ } })<br><br>.subscribeOn(AndroidSchedulers.mainThread()/ 指定主 线程<br><br>.observeOn(AndroidSchedulers.mainThread()</th>
  </tr>
</table>


.subscribe(subscriber);

如上，在 doOnSubscribe() 的后⾯跟⼀个 subscribeOn() ，就能指定准备⼯作的线程了。

# RxJava的适⽤场景和使⽤⽅式

RxJava + Retrofit

Retrofit 是 Square 的⼀个著名的⽹络请求库。对于Retrofit不了解的同学 可以参考我之前写的⽂章：

全新的⽹络加载框架Retrofit2，上位的⼩三

Retrofit 除了提供了传统的 Calback 形式的 API，还有 RxJava 版本的 Observable 形式 API。下⾯我⽤对⽐ 的⽅式来介绍Retrofit 的 RxJava 版 API 和传统版本的区别。 以获取⼀个 MovieEntity 对象的接⼝作为例⼦。使⽤Retrofit 的传统 API，你可以⽤这样的⽅式来定义请 求：

<table>
  <tr>
    <th>1<br>2<br></th>
    <th>@GET("top250") Cal<MovieEntity> getTopMovie(@Query("start") int<br><br>对象</th>
  </tr>
</table>


start, @Query("count") int count);/正常返回Cal

我们来写getMovie⽅法的代码:

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br><br>23</th>
    <th>/进⾏⽹络请求<br><br>private void getMovie(){ String baseUrl = "htps:/api.douban.com/v2/movie/"; Retrofit retrofit = new Retrofit.Builder()<br><br>.baseUrl(baseUrl)<br><br>.adConverterFactory(GsonConverterFactory.create()<br><br>.build(); MovieService movieService =<br><br>retrofit.create(MovieService.clas); Cal<MovieEntity> cal = movieService.getTopMovie(0,<br><br>10); cal.enqueue(new Calback<MovieEntity>() { @Overide public void onResponse(Cal<MovieEntity> cal,<br><br>Response<MovieEntity> response) { resultTV.setText(response.body().toString(); } @Overide public void onFailure(Cal<MovieEntity> cal, Throwable<br><br>t) { resultTV.setText(t.getMesage(); } });</th>
  </tr>
</table>


}

以上为没有经过封装的、原⽣态的Retrofit写⽹络请求的代码。 ⽽使⽤ RxJava 形式的 API，定义同样的请求是这样的：

<table>
  <tr>
    <th>1<br>2<br></th>
    <th>@GET("top250")<br><br>Observable<MovieEntity> getTopMovie(@Query("start") int start, @Query("count") int count);/RxJava返回<br><br>对象</th>
  </tr>
</table>


Observable

Retrofit本身对Rxjava提供了⽀持，getMovie⽅法改为：

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br>9<br>10<br><br><br>1<br><br>12<br>13<br>14<br>15<br>16<br>17<br>18<br>19<br>20<br>21<br><br><br>2<br><br><br>23<br>24<br>25<br>26<br>27<br>28<br>29<br>30<br>31<br></th>
    <th>/进⾏⽹络请求<br><br>private void getMovie(){ String baseUrl = "htps:/api.douban.com/v2/movie/"; Retrofit retrofit = new Retrofit.Builder()<br><br>.baseUrl(baseUrl)<br><br>.adConverterFactory(GsonConverterFactory.create()<br><br>.adCalAdapterFactory(RxJavaCalAdapterFactory.create ()/提供RXjava⽀持<br><br>.build(); MovieService movieService =<br><br>retrofit.create(MovieService.clas); movieService.getTopMovie(0, 10)/返回Observable对象<br><br>.subscribeOn(Schedulers.io()<br><br>.observeOn(AndroidSchedulers.mainThread()<br><br>.subscribe(new Subscriber<MovieEntity>() { @Overide public void onCompleted() { Toast.makeText(MainActivity.this, "Get Top Movie<br><br>Completed", Toast.LENGTH_SHORT).show(); } @Overide public void onEror(Throwable e) { resultTV.setText(e.getMesage(); } @Overide public void onNext(MovieEntity movieEntity) { resultTV.setText(movieEntity.toString(); } });<br><br>}</th>
  </tr>
</table>


32

这样基本上就完成了Retrofit和Rxjava的结合，⼤家可以⾃⼰进⾏封装；那么⽤上了RxJava,我们就可以⽤ 它强⼤的操作符来对数据进⾏处理和操作，各位看官可以具体去实现，我在这⾥不做多做赘述。 参考⽂章：

RxJava 与 Retrofit 结合的最佳实践

## RxBinding

是 Jake Wharton 的⼀个开源库，它提供了⼀套在 Android 平台上的基于 RxJava 的 Binding API。 所谓 Binding，就是类似设置 OnClickListener 、设置 TextWatcher 这样的注册绑定对象的 API。 举个设置点击监听的例⼦。使⽤ RxBinding ，可以把事件监听⽤这样的⽅法来设置：

RxBinding

<table>
  <tr>
    <th>1<br>2<br>3<br>4<br>5<br>6<br>7<br>8<br></th>
    <th>Buton buton = .; RxView.clickEvents(buton)/ 以 Observable 形式来反馈点 击事件<br><br>.subscribe(new Action1<ViewClickEvent>() { @Overide public void cal(ViewClickEvent event) {<br><br>/ Click handling }</th>
  </tr>
</table>


});

看起来除了形式变了没什么区别，实质上也是这样。甚⾄如果你看⼀下它的源码，你会发现它连实现 都没什么惊喜：它的内部是直接⽤⼀个包裹着的 setOnClickListener() 来实现的。然⽽，仅仅这⼀个形式 的改变，却恰好就是 RxBinding 的⽬的：扩展性。通过 RxBinding 把点击监听转换成 Observable 之后，就 有 了 对 它 进 ⾏ 扩 展 的 可 能 。 扩 展 的 ⽅ 式 有 很 多 ， 根 据 需 求 ⽽ 定 。 ⼀ 个 例 ⼦ 是 前 ⾯ 提 到 过 的 throtleFirst() 操作符，⽤于去抖动，也就是消除⼿抖导致的快速连环点击：

<table>
  <tr>
    <th>1<br>2<br></th>
    <th>RxView.clickEvents(buton)<br><br>.throtleFirst(50, TimeUnit.MI LISECONDS)</th>
  </tr>
</table>


3 .subscribe(clickAction); GitHub

如果想对 RxBinding 有更多了解，可以去它的 项⽬ 下⾯看看。

## RxLifecyle

配合 Activity/Fragment ⽣命周期来管理订阅的。 由于 RxJavaObservable订阅后（调 ⽤ subscribe 函数），⼀般会在后台线程执⾏⼀些操作（⽐如访问⽹络请求数据），当后台操作返回 后，调⽤ Observer 的 onNext 等函数，然后在 更新 UI 状态。 但是后台线程请求是需要时间的，如果⽤ 户点击刷新按钮请求新的微博信息，在刷新还没有完成的时候，⽤户退出了当前界⾯返回前⾯的界 ⾯，这个时候刷新的 Observable 如果不取消订阅，则会导致之前的 Activity ⽆法被 JVM 回收导致内存泄 露。 这就是 Android ⾥⾯的⽣命周期管理需要注意的地⽅，RxLifecycle 就是⽤来⼲这事的。⽐如下⾯的 示例：

RxLifecycle

<table>
  <tr>
    <th>1<br>2<br>3<br></th>
    <th>myObservable<br><br>.compose(RxLifecycle.bindUntilEvent(lifecycle, ActivityEvent.DESTROY)</th>
  </tr>
</table>


.subscribe();

这样Activity在destroy的时候就会⾃动取消这个observer

## RxBus

RxBus并不是⼀个库，⽽是⼀种模式。相信⼤多数开发者都使⽤过EventBus或者Oto，作为事件总线通信 库，如果你的项⽬已经加⼊RxJava和EventBus，不妨⽤RxBus代替EventBus，以减少库的依赖。RxJava也可 以轻松实现事件总线，因为它们都依据于观察者模式。 拓展链接：

⽤RxJava实现事件总线(Event Bus) [深⼊RxBus]：⽀持Sticky事件

## RxPermision

RxPermision

是基于RxJava开发的⽤于帮助在Android 6.0中处理运⾏时权限检测的框架。在Android 6.0中， 系统新增了部分权限的运⾏时动态获取。⽽不再是在以前的版本中安装的时候授予权限。 拓展链接：

使⽤RxPermision框架对android6.0权限进⾏检测

# 总结

简⽽⾔之Rxjava是⼀个很⽜逼的库，如果你的项⽬中还没有使⽤RxJava的话，建议可以尝试去集成使 ⽤；对⼤多数⼈⽽已RxJava是⼀个⽐较难上⼿的库了，不亚于Dager的上⼿难度；不过当你认识学习使 ⽤过了，你就会发现RxJava的魅⼒所在；如果看⼀遍没有看懂的童鞋，建议多看⼏次；动⼿写写代码， 我想信本⽂可以给到你们⼀些帮助；你们真正的体会到什么是 从⼊⻔到放弃再到不离不弃 ；这就是RxJava 的魅⼒所在。

![image 6](assets/imageFile6.png)

拓展阅读：

我所理解的RxJava⸺上⼿其实很简单 深⼊浅出RxJava - ⼤头⻤ 给 Android 开发者的 RxJava 详解 - 抛物线

如果有疑问和⻅解，也欢迎⼤家在下⾯留⾔，我会⼀⼀回复⼤家 以上

本⽂作者： 戴定康 本⽂链接： htp:/daidingkang.c/2017/05/19/Rxjava/ 版权声明： 本博客所有⽂章除特别声明外，均采⽤ 许可协议。转载请注明 出处！

C BY-NC-SA 4.0 CN
