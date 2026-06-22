# 前⾔

相信各位看官对RxJava早有⽿闻，那么关于什么是RxJava我就不再赘述了，不知道的可⾃⾏百度。如 果你已经⼤致了解过什么是RxJava，想开始学习，那么本⽂不失为你良好的选择，为什么这么说呢， 因为我也是刚学⼏天，正所谓“知⼰知彼，百战不殆”。⽹上流传，RxJava的⼊⻔⻔槛⾼，⽽经过我这 ⼏天的学习，我反⽽不那么认为，精通我不敢说，但⼊⻔确实也不难，不信？我先来个简单的例⼦预 热⼀下。 先创建个数据发射源,很好理解，就是发射数据⽤的：

Observable<String> sender = Observable.create(new Observable.OnSubscribe<String>() {

@Override public void call(Subscriber<? super String> subscriber) {

subscriber.onNext("Hi，Weavey！"); //发送数据"Hi，Weavey！" }

});

再创建个数据接收源，同理，接收数据⽤的：

Observer<String> receiver = new Observer<String>() {

@Override public void onCompleted() {

//数据接收完成时调⽤ }

@Override public void onError(Throwable e) {

//发⽣错误调⽤ }

@Override public void onNext(String s) {

//正常接收数据调⽤

System.out.print(s); //将接收到来⾃sender的问候"Hi，Weavey！" }

};

好了，将发射源和接收源关联起来：

sender.subscribe(receiver);

这样就形成RxJava⼀个简单的⽤法，sender发射"Hi，Weavey！"，将会被receiver的onNext的接收，通 过这个例⼦，也许你会想到“异步”、“观察者模式”，没错，这些都是RxJava所做的事情，并且让他们 变得更简单和简洁，⽽RxJava所有的⼀切都将围绕这两个点展开，⼀个是发射数据，⼀个是接收数 据，是不是很通俗易懂？如果你理解了这点或者你已经知道RxJava就是这么⼀回事，那么恭喜你，你 已经⼀只脚跨进RxJava的⼤⻔了，如果不是！！！！那也⽆所谓，请继续往下看 .

# 论概念的重要性

⽹上关于RxJava的博⽂也有很多，我也看过许多，其中不乏有优秀的⽂章，但绝⼤部分⽂章都有⼀个 共同点，就是侧重于讲RxJava中各种强⼤的操作符，⽽忽略了最基本的东⻄⸺概念，所以⼀开始我 也看的⼀脸懵逼，看到后⾯⼜忘了前⾯的，脑⼦⾥全是问号，这个是什么，那个⼜是什么，这两个⻓ 得怎么那么像。举个不太恰当的例⼦，概念之于初学者，就像⻝物之于⼈，当你饿了，你会想吃⾯ 包、⽜奶，那你为什么不去吃⼟呢，因为你知道⾯包⽜奶是⽤来⼲嘛的，⼟是⽤来⼲嘛的。同理，前 ⾯已经说过，RxJava⽆⾮是发送数据与接收数据，那么什么是发射源，什么是接收源，这就是你应该 明确的事，也是RxJava的⼊⻔条件之⼀，下⾯就依我个⼈理解，对发射源和接收源做个归类，以及 RxJava中频繁出现的⼏个“单词”解释⼀通，说的不好还请海涵，欢迎补充。

# 基本概念

Observable：发射源，英⽂释义“可观察的”，在观察者模式中称为“被观察者”或“可观察对象”； Observer：接收源，英⽂释义“观察者”，没错！就是观察者模式中的“观察者”，可接收Observable、 Subject发射的数据； Subject：Subject是⼀个⽐较特殊的对象，既可充当发射源，也可充当接收源，为避免初学者被混 淆，本章将不对Subject做过多的解释和使⽤，重点放在Observable和Observer上，先把最基本⽅法的 使⽤学会，后⾯再学其他的都不是什么问题； Subscriber：“订阅者”，也是接收源，那它跟Observer有什么区别呢？Subscriber实现了Observer接 ⼝，⽐Observer多了⼀个最重要的⽅法unsubscribe( )，⽤来取消订阅，当你不再想接收数据了，可 以调⽤unsubscribe( )⽅法停⽌接收，Observer 在 subscribe() 过程中,最终也会被转换成 Subscriber 对象，⼀般情况下，建议使⽤Subscriber作为接收源； Subscription ：Observable调⽤subscribe( )⽅法返回的对象，同样有unsubscribe( )⽅法，可以⽤ 来取消订阅事件； Action0：RxJava中的⼀个接⼝，它只有⼀个⽆参cal（）⽅法，且⽆返回值，同样还有Action1， Action2.Action9等，Action1封装了含有 1 个参的cal（）⽅法，即cal（T t），Action2封装了含有 2 个参数的cal⽅法，即cal（T1 t1，T2 t2），以此类推； Func0：与Action0⾮常相似，也有cal（）⽅法，但是它是有返回值的，同样也有Func0、 Func1.Func9;

# 基本⽤法

Observable的创建

- 1.使⽤create( ),最基本的创建⽅式： normalObservable = Observable.create(new Observable.OnSubscribe<String>() {

@Override public void call(Subscriber<? super String> subscriber) {

- subscriber.onNext("create1"); //发射⼀个"create1"的String
- subscriber.onNext("create2"); //发射⼀个"create2"的String subscriber.onCompleted();//发射完成,这种⽅法需要⼿动调⽤onCompleted，才会回调Observer的


onCompleted⽅法 }});

- 2.使⽤just( )，将为你创建⼀个Observable并⾃动为你调⽤onNext( )发射数据： justObservable = Observable.just("just1","just2");//依次发送"just1"和"just2"
- 3.使⽤from( )，遍历集合，发送每个item： List<String> list = new ArrayList<>(); list.add("from1"); list.add("from2"); list.add("from3"); fromObservable = Observable.from(list); //遍历list 每次发送⼀个 /** 注意，just()⽅法也可以传list，但是发送的是整个list对象，⽽from（）发送的是list的⼀个item** /
- 4.使⽤defer( )，有观察者订阅时才创建Observable，并且为每个观察者创建⼀个新的Observable： deferObservable = Observable.defer(new Func0<Observable<String>>() {

@Override //注意此处的call⽅法没有Subscriber参数 public Observable<String> call() {

return Observable.just("deferObservable"); }});

- 5.使⽤interval( ),创建⼀个按固定时间间隔发射整数序列的Observable，可⽤作定时器： intervalObservable = Observable.interval(1, TimeUnit.SECONDS);//每隔⼀秒发送⼀次
- 6.使⽤range( ),创建⼀个发射特定整数序列的Observable，第⼀个参数为起始值，第⼆个为发送的个

数，如果为0则不发送，负数则抛异常：

rangeObservable = Observable.range(10, 5);//将发送整数10，11，12，13，14

- 7.使⽤timer( ),创建⼀个Observable，它在⼀个给定的延迟后发射⼀个特殊的值，等同于Android中

Handler的postDelay( )⽅法：

timeObservable = Observable.timer(3, TimeUnit.SECONDS); //3秒后发射⼀个值

- 8.使⽤repeat( ),创建⼀个重复发射特定数据的Observable: repeatObservable = Observable.just("repeatObservable").repeat(3);//重复发射3次


Observer的创建

mObserver = new Observer<String>() { @Override public void onCompleted() {

LogUtil.log("onCompleted");

} @Override public void onError(Throwable e) { } @Override public void onNext(String s) {

LogUtil.log(s); }};

ok，有了Observable和Obsever，我们就可以随便玩了，任取⼀个已创建的Observable和Observer关 联上，即形成⼀个RxJava的例⼦，如：

justObservable.subscribe(mObserver); mObserver的onNext⽅法将会依次收到来⾃justObservable的数据"just1"、"just2"，另外，如果你不 在意数据是否接收完或者是否出现错误，即不需要Observer的onCompleted()和onError()⽅法，可使 ⽤Action1，subscribe()⽀持将Action1作为参数传⼊,RxJava将会调⽤它的call⽅法来接收数据，代码 如下： justObservable.subscribe(new Action1<String>() {

@Override public void call(String s) {

LogUtil.log(s); }});

以上就是RxJava最简单的⽤法。看到这⾥，我也不知道我写的是否简单明了，也许你会想，“哎呀，写 个异步的东⻄，怎么这么麻烦，为什么不⽤Thread+Handler呢”,那你就错了，RxJava也以代码的简洁 深受⼴⼤⽤户喜爱，简洁不能理解为代码量少，⽽是随着逻辑的复杂，需求的更改，代码可依然能保 持极强的阅读性，举个简单的例⼦（前⽅⾼能预警 ~），领导要我从数据库的⽤户表查找出所有⽤户 数据，我⼆话不说拿出⼼仪的RxJava就写：

}).subscribe(new Action1<List<User>>() { @Override public void call(List<User> users) {

//获取到⽤户信息列表 }

});

但是，领导突然⼜不想要所有⽤户了，只要名字叫“⼩明”的⽤户，⾏吧，领导最⼤，我改（假设名字 唯⼀）：

Observable.create(new Observable.OnSubscribe<List<User>>() { @Override public void call(Subscriber<? super List<User>> subscriber) {

List<User> userList = null; ··· //从数据库获取⽤户表数据并赋给userList ··· subscriber.onNext(userList);

}

}).flatMap(new Func1<List<User>, Observable<User>>() { @Override public Observable<User> call(List<User> users) {

return Observable.from(users); }

}).filter(new Func1<User, Boolean>() { @Override public Boolean call(User user) {

return user.getName().equals("⼩明"); }

}).subscribe(new Action1<User>() { @Override public void call(User user) {

//拿到谜之⼩明的数据 }

});

搞定，这时候领导⼜说，我不要⼩明了，我要⼩明的爸爸的数据，（坑爹啊 ~），我继续改：

}).flatMap(new Func1<List<User>, Observable<User>>() { @Override public Observable<User> call(List<User> users) {

return Observable.from(users); }

}).filter(new Func1<User, Boolean>() { @Override public Boolean call(User user) {

return user.getName().equals("⼩明"); }

}).map(new Func1<User, User>() { @Override public User call(User user) {

//根据⼩明的数据user从数据库查找出⼩明的⽗亲user2 return user2;

}

}).subscribe(new Action1<User>() { @Override public void call(User user2) {

//拿到谜之⼩明的爸爸的数据 }

});

搞定，“还想怎么改？领导请说 ·”。 以上例⼦，涉及到⼏个操作符，初学者可能⽆法理解，但是⽆所谓，这不是重点，我的⽬的只是为了 向你展示RxJava在需求不断变更、逻辑愈加复杂的情况下，依旧可以保持代码简洁、可阅读性强的⼀ ⾯，没有各种回调，也没有谜之缩进！

# 总结

看了以上所讲，如果你已经爱上了RxJava，如果你已经全部理解，我确信，你已经跨⼊RxJava的⼤ ⻔，剩下的只是时间的问题以及如何在实际开发场景中去应⽤的问题，那么下篇⽂章，我将继续讲解 RxJava中各种强⼤的操作符，并尽量以实际开发过程中遇到的问题作为例⼦，提升⾃⼰的同时，帮助 初学者迅速上⼿RxJava，如有写的不好的地⽅，还请⻅谅，真⼼欢迎各路⼤神指点，探讨相关技术。

作者：Weavey 链接：htp:/ w.jianshu.com/p/5e93c9101dc5 來源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

