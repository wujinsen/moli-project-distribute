摘要： 其实从rxjava14年出现到现在，我是去年从⼀个朋友那⾥听到的，特别是随着现在app项⽬越来 越⼤，分层越来越不明确的情况下，rxjava出现了，以⾄于出现了rxandroid。其实如果你了解观察者模 式的话，rxjava并没有你说的那么神秘。再次，我对rxjava并不崇拜，我的原则是怎么写代码简单，代 码结构清晰，维护简单，就是好框架。 讲rxjava之前⾸先说⼀下Android mvp开

其实从rxjava14年出现到现在，我是去年从⼀个朋友那⾥听到的，特别是随着现在app项⽬越来越⼤，分层越来越不明确的情况下， rxjava出现了，以⾄于出现了rxandroid。其实如果你了解观察者模式的话，rxjava并没有你说的那么神秘。再次，我对rxjava并不崇拜， 我的原则是怎么写代码简单，代码结构清晰，维护简单，就是好框架。 讲rxjava之前⾸先说⼀下Android mvp开发模式。

# MVP的⼯作流程

Presenter负责逻辑的处理， Model提供数据， View负责显示。

作为⼀种新的模式，在MVP中View并不直接使⽤Model，它们之间的通信是通过Presenter来进⾏的， 所有的交互都发⽣在Presenter内部，⽽在MVC中View会从直接Model中读取数据⽽不是通过 Controler。

![image 1](<彻底搞清楚 RxJava 是什么东西.note_images/imageFile1.png>)

接下来说说rxjava

RxJava 到底是什么 RxJava 好在哪 API 介绍和原理简析

- 1. 概念：扩展的观察者模式 观察者模式 RxJava 的观察者模式

- 2. 基本实现

- 1) 创建 Observer

- 2) 创建 Observable

- 3) Subscribe (订阅)

- 4) 场景示例


- a. 打印字符串数组

- b. 由 id 取得图⽚并显示


- 3. 线程控制 —— Scheduler (⼀)

- 1) Scheduler 的 API (⼀)

- 2) Scheduler 的原理 (⼀)


- 4. 变换

- 1) API

- 2) 变换的原理：lift()

- 3) compose: 对 Observable 整体的变换


- 5. 线程控制：Scheduler (⼆)


- 1) Scheduler 的 API (⼆)

- 2) Scheduler 的原理（⼆）

- 3) 延伸：doOnSubscribe()


RxJava 的适⽤场景和使⽤⽅式

- 1. 与 Retrofit 的结合

- 2. RxBinding

- 3. 各种异步操作

- 4. RxBus


最后

关于作者： 为什么写这个？

如果你要了解rxjava是什么，由来，以及作⽤和原理，请点击上⾯的链接。 针对上⾯的问题，我们简单的了解下⼀些基本的概念。

## 什么是rxJava

⼀种帮助你做异步的框架. 类似于 AsyncTask. 但其灵活性和扩展性远远强于前者. 从能⼒上讲, 如果说 AsycnTask 是 DOS 操作系统, RxJava 是 Window 操作系统。

## rxJava的好处

异步操作很关键的⼀点是程序的简洁性，因为在调度过程⽐较复杂的情况下，异步代码经常会既难写也难被读懂。 Android 创造的 AsyncTask 和Handler ，其实都是为了让异步代码更加简洁。RxJava 的优势也是简洁，但它的简洁的与众不同之处在于，随着程序逻 辑变得越来越复杂，它依然能够保持简洁。

看下rxjava的例⼦

![image 2](<彻底搞清楚 RxJava 是什么东西.note_images/imageFile2.png>)

## rxjava原理简析

我想⼤家听说过如下Java的都知道如下Java采⽤的是⼀种扩展的观察者模式实现的，何为观察者模式：观察者模式是⼀种⼀对多的依赖 关系，当⼀个对象改变状态时，它会通知所有依赖者接受通知，并决定数据是否改变。 如果需要详细了解的请：http://blog.csdn.net/xiangzhihong8/article/details/52075547 但是rxjava和传统的观察者模式⼜不完全相同，传统的观察者模式是涉及到两个对象观察者（Observer ）和被观察者 （Observable ）。观察者通过将被观察 的对象加到⾃⼰的观察队列中，当被观察者发⽣改变时，就会通知观察者东⻄已经改变。 ⽽rxJava中涉及到4个概念：Observable (可观察者，即被观察者)、 Observer (观察者)、 subscribe (订阅)、事件。 Observable 和Observer 通过 subscribe() ⽅法实现订阅关系，从⽽ Observable 可以在需要的时候发出事件来通知 Observer 数据刷新。 注意：重点来了

与传统观察者模式不同， RxJava 的事件回调⽅法除了普通事件 onNext() （相当于 onClick() / onEvent()）之外，还定义了两个 特殊的事件：onCompleted()

和 onError()。

onCompleted(): 事件队列完结。RxJava 不仅把每个事件单独处理，还会把它们看做⼀个队列。RxJava 规定，当不会再有新的 onNext() 发出时，需要触发 onCompleted() ⽅法作为标志。 onError(): 事件队列异常。在事件处理过程中出异常时，onError() 会被触发，同时队列⾃动终⽌，不允许再有事件发出。

注意：在⼀个正确运⾏的事件序列中, onCompleted() 和 onError() 有且只有⼀个，也就是说onCompleted() 和 onError() ⼆ 者也是互斥的。在响应的队列中只能调⽤⼀个。 rxjava事件处理的模型图：

![image 3](<彻底搞清楚 RxJava 是什么东西.note_images/imageFile3.png>)

rxjava的基本实现

- 1) 创建 Observer(被观察者对象) //Observable部分,被观察者部分

Observable<String> myObservable=Observable.create(new Observable.OnSubscribe<String> () {

@Override public void call(Subscriber<? super String> subscriber) {

subscriber.onNext("我是被观察的对象"); subscriber.onCompleted();

} });

- 2) 创建Subscriber(观察者对象) //Subscriber部分，观察者部分

Subscriber<String> mySubscriber=new Subscriber<String>() { @Override public void onCompleted() {

}

@Override public void onError(Throwable e) {

}

@Override public void onNext(String s) {

text.setText(s); }

};

- 3) Observer和Subscriber关联 myObservable.subscribe(mySubscriber);


这样就完成了⼀个简单的rxjava，是不是很简单。 注意：如果你⽤的是android studio作为ide⼯具的话，请务必添加rxjava依赖

![image 4](<彻底搞清楚 RxJava 是什么东西.note_images/imageFile4.png>)

除了 subscribe(Observer) 和 subscribe(Subscriber) ，subscribe() 还⽀持不完整定义的回调，RxJava 会⾃动根据定义创 建出Subscriber 。 其实如果看上⾯的写法，代码是显得⽐较难看的，这是为了⽅便⼤家理解rxjava的订阅者模式。 其实上⾯的代码可以这么写：

Observable.just("Hello, world!")

.subscribe(new Action1<String>() { @Override public void call(String s) {

System.out.println(s); }

});

使⽤java8的lambda可以使代码更简洁

Observable.just("Hello, world!")

.subscribe(s -> System.out.println(s));

然⽽如果你认为rxjava只有这个⽤处，那么也什么⽜逼的，在 RxJava 的默认规则中，事件的发出和消费都是在同⼀ 个线程的。观察者模式本身的⽬的就是『后台处理，前台回调』的异步机制，因此异步对于 RxJava 是⾄关重要的。⽽要实现异步，则 需要⽤到 RxJava 的另⼀个概念： Scheduler 。

## Scheduler （线程调度器）

### 线程控制与调度

RxJava 遵循的是线程不变的原则，即：在哪个线程调⽤ subscribe()，就在哪个线程⽣产事件；在哪个线程⽣产事件，就在哪个线程 消费事件。⽽如果要实现线程的调度，就需要scheduler(线程调度器)。 RxJava 已经内置了⼏个 Scheduler ，它们已经适合⼤多数的使⽤场景：

Schedulers.immediate(): 直接在当前线程运⾏，相当于不指定线程。这是默认的 Scheduler。 Schedulers.newThread(): 总是启⽤新线程，并在新线程执⾏操作。

Schedulers.io(): I/O 操作（读写⽂件、读写数据库、⽹络信息交互等）所使⽤的 Scheduler。⾏为模式和 newThread() 差不 多，区别在于 io() 的内部实现是是⽤⼀个⽆数量上限的线程池，可以重⽤空闲的线程，因此多数情况下 io() ⽐ newThread() 更 有效率。不要把计算⼯作放在 io() 中，可以避免创建不必要的线程。

Schedulers.computation(): 计算所使⽤的 Scheduler。这个计算指的是 CPU 密集型计算，即不会被 I/O 等操作限制性能的操 作，例如图形的计算。这个 Scheduler 使⽤的固定的线程池，⼤⼩为 CPU 核数。不要把 I/O 操作放在 computation() 中，否则 I/O 操作的等待时间会浪费 CPU。 另外， Android 还有⼀个专⽤的 AndroidSchedulers.mainThread()，它指定的操作将在 Android 主线程运⾏。

Sceeduler默认给我们提供了subscribeOn() 和 observeOn() 两个⽅法来对线程进⾏控制 。 举个例⼦：

Observable.just(1, 2, 3, 4)

.subscribeOn(Schedulers.io()) // 指定 subscribe() 发⽣在 IO 线程

.observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发⽣在主线 程

.subscribe(new Action1<Integer>() { @Override public void call(Integer number) { }

});

上⾯这段代码中，由于 subscribeOn(Schedulers.io()) 的指定，被创建的事件的内容 1、2、 3、4 将会在 IO 线程发出；⽽由于

observeOn(AndroidScheculers.mainThread()) 的指定，因此 subscriber 数字的打印将发⽣在主线程 。事实上，这种 在 subscribe() 之前写上两句 subscribeOn(Scheduler.io()) 和 observeOn(AndroidSchedulers.mainThread()) 的使⽤⽅式⾮常常⻅， 它适⽤于多数的 『后台线程取数据，主线程显示』的程序策略。

说到这⾥，有⼀个常⽤的场景：加载⼏⼗个图⽚到UI上，这⾥说说rxjava的写法

int drawableRes = ...; ImageView imageView = ...; Observable.create(new OnSubscribe<Drawable>() {

@Override public void call(Subscriber<? super Drawable> subscriber) {

Drawable drawable = getTheme().getDrawable(drawableRes)); subscriber.onNext(drawable); subscriber.onCompleted();

} })

.subscribeOn(Schedulers.io()) // 指定 subscribe() 发⽣在 IO 线程

.observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发⽣在主线程

.subscribe(new Observer<Drawable>() { @Override public void onNext(Drawable drawable) {

imageView.setImageDrawable(drawable); }

@Override public void onCompleted() { }

@Override public void onError(Throwable e) {

Toast.makeText(activity, "Error!", Toast.LENGTH_SHORT).show(); }

});

这样，加载图⽚发⽣在UI线程，⽽设置显示放到⼦线程出来，这样就不会出现卡顿。

### 变换

这个概念我也不知道怎么解释：RxJava 提供了对事件序列进⾏变换的⽀持，这是它的核⼼功能之⼀。所谓变换，就是将事件 序列中的对象或整个序列进⾏加⼯处理，转换成不同的事件或事件序列。 来看⼀个例⼦：

Observable.just("images/logo.png") // 输⼊类型 String

.map(new Func1<String, Bitmap>() { @Override public Bitmap call(String filePath) { // 参数类型 String

return getBitmapFromPath(filePath); // 返回类型 Bitmap }

})

.subscribe(new Action1<Bitmap>() { @Override public void call(Bitmap bitmap) { // 参数类型 Bitmap

showBitmap(bitmap); }

});

这⾥出现了⼀个 Func1 的类。它和 Action1 ⾮常相似，也是 RxJava 的⼀个接⼝，⽤于包装含有⼀ 个参数的⽅法。 Func1 和 Action的区别在于， Func1 包装的是有返回值的⽅法。FuncX 和 ActionX 的区别在 FuncX 包装的是有返回值的⽅法。

通过上⾯的代码我们看到：map() ⽅法将参数中的 String 对象转换成⼀个 Bitmap 对象后返回，⽽ 在经过 map() ⽅法后，事件的参数类型也由 String转为了 Bitmap。这就是最⻓久的转换。

map(): 事件对象的直接变换示意图：

![image 5](<彻底搞清楚 RxJava 是什么东西.note_images/imageFile5.png>)

flatMap(): 这是⼀个很有⽤但⾮常难理解的变换 ⾸先假设这么⼀种需求：假设有⼀个数据结构『学⽣』，现在需要打印出⼀组学⽣的属性（我选择属性，是因为如果对象可以打印，你 们单个属性肯定不是问题）。

Student[] students = ...; Subscriber<Student> subscriber = new Subscriber<Student>() {

@Override public void onNext(Student student) {

List<Course> courses = student.getCourses(); for (int i = 0; i < courses.size(); i++) {

Course course = courses.get(i); Log.d(tag, course.getName());

} }

...

}; Observable.from(students)

.subscribe(subscriber);

写法也很简单，看得也很明⽩。

flatmap运⾏原理图：

![image 6](<彻底搞清楚 RxJava 是什么东西.note_images/imageFile6.png>)

变换的原理：lift()

这些变换虽然功能各有不同，但实质上都是针对事件序列的处理和再发送。⽽在 RxJava 的内部，它们是基于同⼀个基础的变换⽅法： lift(Operator)。⾸先看⼀下 lift() 的内部实现（仅核⼼代码）：

// 注意：这不是 lift() 的源码，⽽是将源码中与性能、兼容性、扩展性有关的代码剔除后的核⼼代码。 // 如果需要看源码，可以去 RxJava 的 GitHub 仓库下载。 public <R> Observable<R> lift(Operator<? extends R, ? super T> operator) {

return Observable.create(new OnSubscribe<R>() { @Override public void call(Subscriber subscriber) {

Subscriber newSubscriber = operator.call(subscriber); newSubscriber.onStart(); onSubscribe.call(newSubscriber);

} });

}

