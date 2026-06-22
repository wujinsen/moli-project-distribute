Observable 数据流有两种类型：hot 和 cold。这两种类型有很⼤的不同。本节介绍他们的区别，以及作为 Rx 开发者应该如何正确的使⽤他们。

# Coldobservables

只有当有订阅者订阅的时候， Cold Observable 才开始执⾏发射数据流的代码。并且每个订阅者订阅的时候 都独⽴的执⾏⼀遍数据流代码。 Observable.interval 就是⼀个 Cold Observable。每⼀个订阅者都会独⽴的 收到他们的数据流。

Observable<Long> cold = Observable.interval(200, TimeUnit.MILLISECONDS);

cold.subscribe(i -> System.out.println("First: " + i)); Thread.sleep(500); cold.subscribe(i -> System.out.println("Second: " + i));

结果：

- First: 0
- First: 1
- First: 2

- Second: 0

First: 3

- Second: 1

First: 4

- Second: 2




...

虽然这两个 Subscriber 订阅到同⼀个Observable 上，只是订阅的时间不同，他们都收到同样的数据流，但 是同⼀时刻收到的数据是不同的。 在本教程中之前所⻅到的 Observable 都是 Cold Observable。 Observable.create 创建的也是 Cold Observable，⽽ just, range, timer 和 from 这些创建的同样是 Cold Observable。

# Hotobservables

Hot observable 不管有没有订阅者订阅，他们创建后就开发发射数据流。 ⼀个⽐较好的示例就是 ⿏标事件。 不管系统有没有订阅者监听⿏标事件，⿏标事件⼀直在发⽣，当有订阅者订阅后，从订阅后的事件开始发送 给这个订阅者，之前的事件这个订阅者是接受不到的；如果订阅者取消订阅了，⿏标事件依然继续发射。

# Publish

Cold Observable 和 Hot Observable 之间可以相互转化。使⽤ publish 操作函数可以把 Cold Observable 转 化为 Hot Observable。

public final ConnectableObservable<T> publish()

![image 1](<RxJava 驯服数据流之 hot & cold Observable.note_images/imageFile1.png>)

这⾥写图⽚描述

publish 返回⼀个 ConectableObservable 对象，这个对象是 Observable 的之类，多了三个函数：

public final Subscription connect() public abstract void connect(Action1<? super Subscription> connection) public Observable<T> refCount()

另外还有⼀个重载函数，可以在发射数据之前对数据做些处理：

public final <R> Observable<R> publish(Func1<? super Observable<T>,? extends Observable<R>> selector)

之前介绍的所有对 Observable 的操作都可以在 selector 中使⽤。你可以通过 selector 参数创建⼀个 Subscription ，后来的订阅者都订阅到这⼀个 Subscription 上，这样可以确保所有的订阅者都在同⼀时刻收 到同样的数据。 这个重载函数返回的是 Observable ⽽不是 ConectableObservable， 所以下⾯讨论的操作函数⽆法在这个 重载函数返回值上使⽤。

connect

ConectableObservable 如果不调⽤ conect 函数则不会触发数据流的执⾏。当调⽤ conect 函数以后，会 创建⼀个新的 subscription 并订阅到源 Observable （调⽤ publish 的那个 Observable）。这个 subscription 开始接收数据并把它接收到的数据转发给所有的订阅者。这样，所有的订阅者在同⼀时刻都可 以收到同样的数据。

ConnectableObservable<Long> cold = Observable.interval(200, TimeUnit.MILLISECONDS).publish(); cold.connect();

cold.subscribe(i -> System.out.println("First: " + i)); Thread.sleep(500); cold.subscribe(i -> System.out.println("Second: " + i));

结果：

- First: 0
- First: 1
- First: 2

- Second: 2

First: 3

- Second: 3

First: 4

- Second: 4

First: 5

- Second: 5




# Disconnecting

conect 函数返回的是⼀个 Subscription，和 Observable.subscribe返回的结果⼀样。 可以使⽤这个 Subscription 来取消订阅到 ConectableObservable。 如果调⽤ 这个 Subscription 的 unsubscribe 函数， 可以停⽌把数据转发给 Observer，但是这些 Observer 并没有从 ConectableObservable 上取消注册，只是 停⽌接收数据了。如果再次调⽤ conect ， 则 ConectableObservable 开始⼀个新的订阅，在 ConectableObservable 上订阅的 Observer 会再次开始接收数据。

ConnectableObservable<Long> connectable = Observable.interval(200, TimeUnit.MILLISECONDS).publish(); Subscription s = connectable.connect();

connectable.subscribe(i -> System.out.println(i));

Thread.sleep(1000); System.out.println("Closing connection"); s.unsubscribe();

Thread.sleep(1000); System.out.println("Reconnecting"); s = connectable.connect();

结果：

- 0
- 1
- 2
- 3
- 4 Closing connection Reconnecting


- 0
- 1
- 2


...

通过调⽤ conect 来重新开始订阅，会创建⼀个新的订阅。如果源 Observable 为 Cold Observable 则数据 流会重新执⾏⼀遍。 如果你不想结束数据流，只想从 publish 返回的 Hot Observable 上取消注册，则可以使⽤ subscribe 函数返 回的 Subscription 对象。

ConnectableObservable<Long> connectable = Observable.interval(200, TimeUnit.MILLISECONDS).publish(); Subscription s = connectable.connect();

- Subscription s1 = connectable.subscribe(i -> System.out.println("First: " + i)); Thread.sleep(500);
- Subscription s2 = connectable.subscribe(i -> System.out.println("Second: " + i));


Thread.sleep(500); System.out.println("Unsubscribing second"); s2.unsubscribe();

结果：

- First: 0
- First: 1
- First: 2

- Second: 2

First: 3

- Second: 3

First: 4

- Second: 4 Unsubscribing second


- First: 5
- First: 6


# refCount

ConectableObservable.refCount 返回⼀个特殊的 Observable， 这个 Observable 只要有订阅者就会继续 发射数据。

Observable<Long> cold = Observable.interval(200, TimeUnit.MILLISECONDS).publish().refCount();

- Subscription s1 = cold.subscribe(i -> System.out.println("First: " + i)); Thread.sleep(500);
- Subscription s2 = cold.subscribe(i -> System.out.println("Second: " + i)); Thread.sleep(500); System.out.println("Unsubscribe second"); s2.unsubscribe(); Thread.sleep(500); System.out.println("Unsubscribe first"); s1.unsubscribe();


System.out.println("First connection again"); Thread.sleep(500); s1 = cold.subscribe(i -> System.out.println("First: " + i));

结果：

- First: 0
- First: 1
- First: 2

- Second: 2

First: 3

- Second: 3 Unsubscribe second


- First: 4
- First: 5
- First: 6 Unsubscribe first First connection again


- First: 0
- First: 1
- First: 2
- First: 3
- First: 4


如果没有订阅者订阅到 refCount 返回的 Observable，则不会执⾏数据流的代码。如果所有的订阅者都取消 订阅了，则数据流停⽌。重新订阅再回重新开始数据流。

# replay

public final ConnectableObservable<T> replay()

1 1

![image 2](<RxJava 驯服数据流之 hot & cold Observable.note_images/imageFile2.png>)

这⾥写图⽚描述

replay 和 ReplaySubject 类似。当和源 Observable 链接后，开始收集数据。当有 Observer 订阅的时候，就 把收集到的数据线发给 Observer。然后和其他 Observer 同时接受数据。

ConnectableObservable<Long> cold = Observable.interval(200, TimeUnit.MILLISECONDS).replay(); Subscription s = cold.connect();

System.out.println("Subscribe first");

- Subscription s1 = cold.subscribe(i -> System.out.println("First: " + i)); Thread.sleep(700); System.out.println("Subscribe second");
- Subscription s2 = cold.subscribe(i -> System.out.println("Second: " + i)); Thread.sleep(500);


结果：

Subscribe first

- First: 0
- First: 1
- First: 2 Subscribe second

- Second: 0
- Second: 1
- Second: 2

First: 3

- Second: 3




replay 和 publish ⼀样也返回⼀个 ConectableObservable 。所以我们可以在上⾯使⽤ refCount 来创建新 的 Observable 也可以取消注册。 replay 有 8个重载函数：

ConnectableObservable<T> replay() <R> Observable<R> replay(Func1<? super Observable<T>,? extends Observable<R>> selector) <R> Observable<R> replay(Func1<? super Observable<T>,? extends Observable<R>> selector, int bufferSize) <R> Observable<R> replay(Func1<? super Observable<T>,? extends Observable<R>> selector, int bufferSize, long time, java.util.concurrent.TimeUnit unit) <R> Observable<R> replay(Func1<? super Observable<T>,? extends Observable<R>> selector, long time, java.util.concurrent.TimeUnit unit) ConnectableObservable<T> replay(int bufferSize) ConnectableObservable<T> replay(int bufferSize, long time, java.util.concurrent.TimeUnit unit) ConnectableObservable<T> replay(long time, java.util.concurrent.TimeUnit unit)

有三个参数 buferSize、 selector 和 time （以及指定时间单位的 unit）

buferSize ⽤来指定缓存的最⼤数量。当新的 Observer 订阅的时候，最多只能收到 buferSize 个之前缓 存的数据。 time, unit ⽤来指定⼀个数据存货的时间，新订阅的 Observer 只能收到时间不超过这个参数的数据。 selector 和 publish(selector) ⽤来转换重复的 Observable。

下⾯是⼀个 buferSize 的示例：

ConnectableObservable<Long> source = Observable.interval(1000, TimeUnit.MILLISECONDS)

.take(5)

.replay(2);

source.connect(); Thread.sleep(4500); source.subscribe(System.out::println);

结果：

- 2
- 3
- 4


# cache

cache 操作函数和 replay 类似，但是隐藏了 ConectableObservable ，并且不⽤管理 subscription 了。当 第⼀个 Observer 订阅的时候，内部的 ConectableObservable 订阅到源 Observable。后来的订阅者会收到 之前缓存的数据，但是并不会重新订阅到源 Observable 上。

public final Observable<T> cache() public final Observable<T> cache(int capacity)

![image 3](<RxJava 驯服数据流之 hot & cold Observable.note_images/imageFile3.png>)

这⾥写图⽚描述

Observable<Long> obs = Observable.interval(100, TimeUnit.MILLISECONDS)

.take(5)

.cache();

Thread.sleep(500); obs.subscribe(i -> System.out.println("First: " + i)); Thread.sleep(300); obs.subscribe(i -> System.out.println("Second: " + i));

结果：

- First: 0
- First: 1
- First: 2

- Second: 0
- Second: 1
- Second: 2

First: 3

- Second: 3

First: 4

- Second: 4




从上⾯示例中可以看到，只有当有订阅者订阅的时候，源 Observable 才开始执⾏。当第⼆个订阅者订阅的时 候，会收到之前缓存的数据。

需要注意的是，如果所有的订阅者都取消订阅了 内部的 ConectableObservable 不会取消订阅，这点和 refCount 不⼀样。只要第⼀个订阅者订阅了，内部的 ConectableObservable 就链接到源 Observable上了 并且不会取消订阅了。 这点⾮常重要，因为当我们⼀单订阅了，就没法取消源 Observable了， 直到源 Observable 结束或者程序内存溢出。 可以指定缓存个数的重载函数也没法解决这个问题，缓存限制只是作为 ⼀个优化的提示，并不会限制内部的缓存⼤⼩。

Observable<Long> obs = Observable.interval(100, TimeUnit.MILLISECONDS)

.take(5)

.doOnNext(System.out::println)

.cache()

.doOnSubscribe(() -> System.out.println("Subscribed"))

.doOnUnsubscribe(() -> System.out.println("Unsubscribed"));

Subscription subscription = obs.subscribe(); Thread.sleep(150); subscription.unsubscribe();

结果：

Subscribed

- 0 Unsubscribed
- 1
- 2
- 3
- 4


上⾯的示例中，doOnNext 打印源 Observable 发射的每个数据。⽽ doOnSubscribe 和doOnUnsubscribe 打 印缓存后的 Observable 的订阅和取消订阅事件。可以看到当订阅者订阅的时候，数据流开始发射，取消订阅 数据流并不会停⽌。

# Multicast

share 函数是 Observable.publish().refCount() 的别名。可以让你的订阅者分享⼀个 subscription，只要还有 订阅者在，这个 subscription 就继续⼯作。 本⽂出⾃ 云在千峰 htp:/blog.chengyunfeng.com/?p=975

