htps:/segmentfault.com/a/19 0231483

你有⼀个思想，我有⼀个思想，我们交换后，⼀个⼈就有两个思想

If you can NOT explain it simply, you do NOT understand it wel enough

![image 1](<搞定 CompletableFuture，并发异步编程和编写串行程序还有什么区别？你们要的多图长文.note_images/imageFile1.png>)

# 前⾔

上⼀篇⽂章 全⾯分析了 Future，通过它我们可以获取线程 的执⾏结果，它虽然解决了 Runable 的 “三⽆” 短板，但是它⾃身还是有短板： 不能⼿动完成计算 假设你使⽤ Future 运⾏⼦线程调⽤远程 API 来获取某款产品的最新价格，服务器由于洪灾宕机了，此 时如果你想⼿动结束计算，⽽是想返回上次缓存中的价格，这是 Future 做不到的 调⽤ get() ⽅法会阻塞程序 Future 不会通知你它的完成，它提供了⼀个get()⽅法，程序调⽤该⽅法会阻塞直到结果可⽤为⽌，没 有办法利⽤回调函数附加到Future，并在Future的结果可⽤时⾃动调⽤它 不能链式执⾏ 烧⽔泡茶中，通过构造函数传参做到多个任务的链式执⾏，万⼀有更多的任务，或是任务链的执⾏顺 序有变，对原有程序的影响都是⾮常⼤的 整合多个 Future 执⾏结果⽅式笨重

不会⽤Java Future，我怀疑你泡茶没我快

假设有多个 Future 并⾏执⾏，需要在这些任务全部执⾏完成之后做后续操作，Future 本身是做不到 的，需要借助⼯具类 Executors 的⽅法

- <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)


- <T> T invokeAny(Collection<? extends Callable<T>> tasks) 没有异常处理 Future 同样没有提供很好的异常处理⽅案 上⼀篇⽂章看 Future 觉得是发现了新天地，这么⼀说有感觉回到了解放前 对于 Java 后端的同学，在 Java1.8 之前想实现异步编程，还想避开上述这些烦恼， 应该是 ⼀个常⻅解决⽅案（做Android 的应该会有了解）。如果熟悉前端同学， ES6 Promise（男朋友的承 诺）也解决了异步编程的烦恼 天下语⾔都在彼此借鉴相应优点，Java 作为⽼牌劲旅⾃然也要解决上述问题。⼜是那个男⼈，并发⼤ 师 Doug Lea 忧天下程序员之忧，解天下程序员之困扰，在 Java1.8 版本（Lambda 横空出世）中，新 增了⼀个并发⼯具类 CompletableFuture，它的出现，让⼈在泡茶过程中，品尝到了不⼀样的味 道 . ⼏个重要 Lambda 函数 CompletableFuture 在 Java1.8 的版本中出现，⾃然也得搭上 Lambda 的顺⻛⻋，为了更好的理 解 CompletableFuture，这⾥我需要先介绍⼀下⼏个 Lambda 函数，我们只需要关注它们的以下⼏点 就可以：


ReactiveX

参数接受形式

返回值形式

函数名称

Runable

Runable 我们已经说过⽆数次了，⽆参数，⽆返回值 @FunctionalInterface public interface Runnable {

### public abstract void run(); }

Function

Function<T, R> 接受⼀个参数，并且有返回值 @FunctionalInterface public interface Function<T, R> {

R apply(T t); }

Consumer

Consumer<T> 接受⼀个参数，没有返回值

@FunctionalInterface public interface Consumer<T> {

void accept(T t); }

Suplier

### Suplier<T> 没有参数，有⼀个返回值 @FunctionalInterface public interface Supplier<T> {

T get(); }

BiConsumer

BiConsumer<T, U> 接受两个参数（Bi， 英⽂单词词根，代表两个的意思），没有返回值 @FunctionalInterface public interface BiConsumer<T, U> {

void accept(T t, U u); 好了，我们做个⼩汇总 有些同学可能有疑问，为什么要关注这⼏个函数式接⼝，因为 CompletableFuture 的函数命名以及其 作⽤都是和这⼏个函数式接⼝⾼度相关的，⼀会你就会发现了 前戏做⾜，终于可以进⼊正题了 CompletableFuture

# CompletableFuture

类结构

⽼规矩，先从类结构看起：

实现了 Future 接⼝

实现了 Future 接⼝，那就具有 Future 接⼝的相关特性，请脑补 Future 那少的可怜的 5 个⽅法，这⾥ 不再赘述，具体请查看

不会⽤Java Future，我怀疑你泡茶没我快

实现了 CompletionStage 接⼝

CompletionStage 这个接⼝还是挺陌⽣的，中⽂直译过来是【竣⼯阶段】，如果将烧⽔泡茶⽐喻成⼀ 项⼤⼯程，他们的竣⼯阶段体现是不⼀样的

- 1.
- 2.
- 3.


单看线程1 或单看线程 2 就是⼀种串⾏关系，做完⼀步之后做下⼀步 ⼀起看线程1 和 线程 2，它们彼此就是并⾏关系，两个线程做的事彼此独⽴互补⼲扰 泡茶就是线程1 和 线程 2 的汇总/组合，也就是线程 1 和 线程 2 都完成之后才能到这个阶段（当然 也存在线程1 或 线程 2 任意⼀个线程竣⼯就可以开启下⼀阶段的场景）

所以，CompletionStage 接⼝的作⽤就做了这点事，所有函数都⽤于描述任务的时序关系，总结起来 就是这个样⼦： CompletableFuture 既然实现了两个接⼝，⾃然也就会实现相应的⽅法充分利⽤其接⼝特性，我们⾛ 进它的⽅法来看⼀看

CompletableFuture ⼤约有50种不同处理串⾏，并⾏，组合以及处理错误的⽅法。⼩弟屏幕不争⽓， ⽅法之多，⼀个屏幕装不下，看到这么多⽅法，是不是瞬间要直接 收藏——>吃灰 2连⾛⼈？别担⼼， 我们按照相应的命名和作⽤进⾏分类，分分钟搞定50多种⽅法

串⾏关系

then 直译【然后】，也就是表示下⼀步，所以通常是⼀种串⾏关系体现, then 后⾯的单词（⽐如 run /aply/acept）就是上⾯说的函数式接⼝中的抽象⽅法名称了，它的作⽤和那⼏个函数式接⼝的作⽤ 是⼀样⼀样滴 CompletableFuture<Void> thenRun(Runnable action) CompletableFuture<Void> thenRunAsync(Runnable action) CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor)

- <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)


- <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn) <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)


CompletableFuture<Void> thenAccept(Consumer<? super T> action) CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor)

<U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor)

聚合 And 关系

combine... with... 和 both...and... 都是要求两者都满⾜，也就是 and 的关系了

<U,V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn) <U,V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn) <U,V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn, Executor executor)

<U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) <U> CompletableFuture<Void> thenAcceptBothAsync( CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor)

CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor)

聚合 Or 关系

Either...or... 表示两者中的⼀个，⾃然也就是 Or 的体现了

<U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) <U> CompletableFuture<U> applyToEitherAsync(、CompletionStage<? extends T> other, Function<? super T, U> fn) <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor)

CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor)

CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor)

异常处理

CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) CompletableFuture<T> exceptionallyAsync(Function<Throwable, ? extends T> fn) CompletableFuture<T> exceptionallyAsync(Function<Throwable, ? extends T> fn, Executor executor)

CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor)

<U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor)

这个异常处理看着还挺吓⼈的，拿传统的 try/catch/finaly 做个对⽐也就瞬间秒懂了 whenComplete 和 handle 的区别如果你看接受的参数函数式接⼝名称你也就能看出差别了，前者使⽤ Comsumer, ⾃然也就不会有返回值；后者使⽤ Function，⾃然也就会有返回值 这⾥并没有全部列举，不过相信很多同学已经发现了规律： CompletableFuture 提供的所有回调⽅法都有两个异步（Async）变体，都像这样 // thenApply() 的 变 体 <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn) <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn) <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)

另外,⽅法的名称也都与前戏中说的函数式接⼝完全匹配，按照这中规律分类之后，这 50 多个⽅法看 起来是不是很轻松了呢？ 基本⽅法已经罗列的差不多了，接下来我们通过⼀些例⼦来实际演示⼀下：

案例演示

创建⼀个 CompletableFuture 对象

创建⼀个 CompletableFuture 对象并没有什么稀奇的，依旧是通过构造函数构建

CompletableFuture<String> completableFuture

= new CompletableFuture<String>(); 这是最简单的 CompletableFuture 对象创建⽅式，由于它实现了 Future 接⼝，所以⾃然就可以通过 get() ⽅法获取结果 String result = completableFuture.get();

⽂章开头已经说过，get()⽅法在任务结束之前将⼀直处在阻塞状态，由于上⾯创建的 Future 没有返 回，所以在这⾥调⽤ get() 将会永久性的堵塞 这时就需要我们调⽤ complete() ⽅法⼿动的结束⼀个 Future completableFuture.complete("Future's Result Here Manually"); 这时，所有等待这个 Future 的 client 都会返回⼿动结束的指定结果

runAsync

使⽤ runAsync 进⾏异步计算 CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {

### try {

TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) {

throw new IllegalStateException(e);

} System.out.println("运⾏在⼀个单独的线程当中");

});

future.get(); 由于使⽤的是 Runable 函数式表达式，⾃然也不会获取到结果

suplyAsync

使⽤ runAsync 是没有返回结果的，我们想获取异步计算的返回结果需要使⽤ supplyAsync() ⽅法

CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {

### try {

TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) {

throw new IllegalStateException(e);

} log.info("运⾏在⼀个单独的线程当中"); return "我有返回值";

});

log.info(future.get());

由于使⽤的是 Suplier 函数式表达式，⾃然可以获得返回结果 我们已经多次说过，get() ⽅法在Future 计算完成之前会⼀直处在 blocking 状态下，对于真正的异步 处理，我们希望的是可以通过传⼊回调函数，在Future 结束时⾃动调⽤该回调函数，这样，我们就不 ⽤等待结果 CompletableFuture<String> comboText = CompletableFuture.supplyAsync(() -> {

## //可 以 注 释 掉 做 快 速返 回 start

### try {

TimeUnit.SECONDS.sleep(3); } catch (InterruptedException e) {

throw new IllegalStateException(e);

} log.info("👍 ");

## //可 以 注 释 掉 做 快 速返 回 end

return "赞"; })

.thenApply(first -> { log.info("在看"); return first + ", 在看";

})

.thenApply(second -> second + ", 转发");

log.info("三连有没有？"); log.info(comboText.get());

对 thenAply 的调⽤并没有阻塞程序打印log，也就是前⾯说的通过回调通知机制， 这⾥你看到 thenAply 使⽤的是suplyAsync所⽤的线程，如果将suplyAsync 做快速返回，我们再来看⼀下运⾏ 结果： thenAply 此时使⽤的是主线程，所以： 串⾏的后续操作并不⼀定会和前序操作使⽤同⼀个线程

thenAcept

如果你不想从回调函数中返回任何结果，那可以使⽤ thenAcept

final CompletableFuture<Void> voidCompletableFuture =

CompletableFuture.supplyAsync( // 模 拟 远 端 API调 ⽤ ， 这 ⾥ 只 返 回 了 ⼀ 个 构 造 的 对 象 () -> Product.builder().id(12345L).name("颈椎/腰椎治疗

仪").build())

.thenAccept(product -> {

log.info("获取到远程API产品名称 " + product.getName()); });

voidCompletableFuture.get();

thenRun

thenAccept 可以从回调函数中获取前序执⾏的结果，但thenRun 却不可以，因为它的回调函数式表 达式定义中没有任何参数 CompletableFuture.supplyAsync(() -> {

## //前 序 操 作

}).thenRun(() -> {

## //串 ⾏ 的 后 需 操 作 ， ⽆ 参 数 也 ⽆ 返 回 值

}); 我们前⾯同样说过了，每个提供回调⽅法的函数都有两个异步（Async）变体，异步就是另外起⼀个线 程

CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {

log.info("前序操作"); return "前需操作结果";

}).thenApplyAsync(result -> {

log.info("后续操作"); return "后续操作结果";

}); 到这⾥，相信你串⾏的操作你已经⾮常熟练了

thenCompose

⽇常的任务中，通常定义的⽅法都会返回 CompletableFuture 类型，这样会给后续操作留有更多的余 地，假如有这样的业务（X呗是不是都有这样的业务呢？）：

## //获 取 ⽤ 户 信 息 详 情

CompletableFuture<User> getUsersDetail(String userId) {

return CompletableFuture.supplyAsync(() ->

User.builder().id(12345L).name("⽇拱⼀兵").build()); }

//获 取 ⽤ 户 信 ⽤ 评 级 CompletableFuture<Double> getCreditRating(User user) {

return CompletableFuture.supplyAsync(() ->

CreditRating.builder().rating(7.5).build().getRating());

} 这时，如果我们还是使⽤ thenAply() ⽅法来描述串⾏关系，返回的结果就会发⽣ CompletableFuture 的嵌套

CompletableFuture<CompletableFuture<Double>> result = completableFutureCompose.getUsersDetail(12345L)

.thenApply(user -> completableFutureCompose.getCreditRating(user)); 显然这不是我们想要的，如果想“拍平” 返回结果，thenCompose ⽅法就派上⽤场了 CompletableFuture<Double> result = completableFutureCompose.getUsersDetail(12345L)

.thenCompose(user -> completableFutureCompose.getCreditRating(user)); 这个和 Lambda 的map 和 flatMap 的道理是⼀样⼀样滴

thenCombine

如果要聚合两个独⽴ Future 的结果，那么 thenCombine 就会派上⽤场了

CompletableFuture<Double> weightFuture = CompletableFuture.supplyAsync(() -> 65.0);

CompletableFuture<Double> heightFuture = CompletableFuture.supplyAsync(() -> 183.8);

CompletableFuture<Double> combinedFuture = weightFuture

.thenCombine(heightFuture, (weight, height) -> { Double heightInMeter = height/100; return weight/(heightInMeter*heightInMeter);

});

log.info("身体BMI指标 - " + combinedFuture.get());

当然这⾥多数时处理两个 Future 的关系，如果超过两个Future，如何处理他们的⼀些聚合关系呢？

alOf ｜ anyOf

相信你看到⽅法的签名，你已经明⽩他的⽤处了，这⾥就不再介绍了 static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs) static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs) 接下来就是异常的处理了

exceptionaly

Integer age = -1;

CompletableFuture<String> maturityFuture = CompletableFuture.supplyAsync(() -> {

if( age < 0 ) { throw new IllegalArgumentException("何⽅神圣？");

} if(age > 18) {

return "⼤家都是成年⼈"; } else {

return "未成年禁⽌⼊内"; }

}).thenApply((str) -> { log.info("游戏开始"); return str;

}).exceptionally(ex -> { log.info("必有蹊跷，来者" + ex.getMessage()); return "Unknown!";

});

log.info(maturityFuture.get()); exceptionaly 就相当于 catch，出现异常，将会跳过 thenAply 的后续操作，直接捕获异常，进⾏⼀ 场处理

handle

⽤多线程，良好的习惯是使⽤ try/finaly 范式，handle 就可以起到 finaly 的作⽤，对上述程序做⼀个 ⼩⼩的更改， handle 接受两个参数，⼀个是正常返回值，⼀个是异常 注意：handle的写法也算是范式的⼀种

Integer age = -1;

CompletableFuture<String> maturityFuture = CompletableFuture.supplyAsync(() -> {

if( age < 0 ) { throw new IllegalArgumentException("何⽅神圣？");

} if(age > 18) {

return "⼤家都是成年⼈"; } else {

return "未成年禁⽌⼊内"; }

}).thenApply((str) -> { log.info("游戏开始"); return str;

}).handle((res, ex) -> { if(ex != null) { log.info("必有蹊跷，来者" + ex.getMessage()); return "Unknown!";

} return res;

});

log.info(maturityFuture.get()); 到这⾥，关于 CompletableFuture 的基本使⽤你已经了解的差不多了，不知道你是否注意，我们前 ⾯说的带有 Sync 的⽅法是单独起⼀个线程来执⾏，但是我们并没有创建线程，这是怎么实现的呢？ 细⼼的朋友如果仔细看每个变种函数的第三个⽅法也许会发现⾥⾯都有⼀个 Executor 类型的参数，⽤ 于指定线程池，因为实际业务中我们是严谨⼿动创建线程的，这在

我会⼿动创建线程，为什么要使⽤ 线程池?

⽂章中明确说明过；如果没有指定线程池，那⾃然就会有⼀个默认的线程池，也就是 ForkJoinPol private static final Executor ASYNC_POOL = USE_COMMON_POOL ?

ForkJoinPool.commonPool() : new ThreadPerTaskExecutor(); ForkJoinPol 的线程数默认是 CPU 的核⼼数。但是，在前序⽂章中明确说明过： 不要所有业务共⽤⼀个线程池，因为，⼀旦有任务执⾏⼀些很慢的 I/O 操作，就会导致线程池中所有线 程都阻塞在 I/O 操作上，从⽽造成线程饥饿，进⽽影响整个系统的性能

总结

CompletableFuture 的⽅法并没有全部介绍完全，也没必要全部介绍，相信⼤家按照这个思路来理 解 CompletableFuture 也不会有什么⼤问题了，剩下的就交给实践/时间以及⾃⼰的体会了

# 后记

你以为 JDK1.8 CompletableFuture 已经很完美了是不是，但追去完美的道路上永⽆⽌境，Java 9 对 CompletableFuture ⼜做了部分升级和改造

- 1.
- 2.
- 3.


添加了新的⼯⼚⽅法 ⽀持延迟和超时处理

orTimeout() completeOnTimeout()

改进了对⼦类的⽀持

详情可以查看： . 怎样快速的切换不同 Java 版本来尝 鲜？ 这篇⽂章的⽅法送给你 最后咱们再泡⼀壶茶，感受⼀下新变化吧

Java 9 CompletableFuture API Improvements SDKMAN 统⼀灵活管理多版本Java

灵魂追问

- 1.
- 2.


听说 ForkJoinPol 线程池效率更⾼，为什么呢？ 如果批量处理异步程序，有什么可⽤的⽅案吗？

参考

- 1.
- 2.
- 3.
- 4.
- 5.


Java 并发编程实战 Java 并发编程的艺术 Java 并发编程之美 htps:/ w.baeldung.com/java. htps:/ w.calicoder.com/ja.

⽇拱⼀兵 ｜ 原创

