htps:/mp.weixin.q.com/s/c5gcRtQVQfXnosp-MqHIkg

好看请赞，养成习惯

你有⼀个思想，我有⼀个思想，我们交换后，⼀个⼈就有两个思想 If you can NOT explain it simply, you do NOT understand it well enough

现陆续将Demo代码和技术⽂章整理在⼀起 Github实践精选 ，⽅便⼤家阅读查看，本⽂同样收录在此， 觉得不错，还请Star🌟

横看成岭侧成峰，远近⾼低各不同，并发编程理论系列基本已经结束，相信⼤家有了理论的铺垫，近 看源码才能发现其设计之美，不会⼀头雾⽔

本来是要介绍 AQS 作为我们⾛进并发编程源码环节的第⼀步，但 AQS 涉及的知识点也还真有点多， 每⼀个都够单独拿出来说⼀说，恰巧有朋友私信我“不理解线程的中断机制”，中断机制⼜恰巧是 AQS API实现的⼀部分，更贯穿于整个并发编程内容中。于是就打算单独说⼀说这个⼩机制，先让⼤家做到

⼼中有 number 在学习/编写并发程序时，总会听到/看到如下词汇：

线程被中断或抛出InterruptedException

设置了中断标识 清空了中断标识 判断线程是否被中断

在 Java Thread 类⼜提供了⻓相酷似，让⼈傻傻分不清的三个⽅法来处理并发中断问题：

interrupt() interrupted() isInterrupted()

![image 1](<Java并发编程的中断机制.note_images/imageFile1.png>)

看到这我不禁会问⾃⼰：

![image 2](<Java并发编程的中断机制.note_images/imageFile2.png>)

## 什么是中断机制？

![image 3](<Java并发编程的中断机制.note_images/imageFile3.png>)

刚刚接触【中断】这个词时，先⼊为主的概念就是“直接中断/打断”正在做的事，使其停⽌。我的理解 是这样的：

你：在打游戏 ⼥朋友：别打游戏了，赶快过来吃饭 你：听到⼥朋友招呼之后⽴⻢中断⼿中的游戏乖乖过去吃饭

![image 4](<Java并发编程的中断机制.note_images/imageFile4.png>)

在多线程编程中，中断是⼀种【协同】机制，怎么理解这么⾼⼤上的词呢？就是⼥朋友叫你吃饭，你 收到了中断游戏通知，但是否⻢上放下⼿中的游戏去吃饭看你⼼情 。在程序中怎样演绎这个⼼情就看 具体的业务逻辑了，Java 的中断机制就是这么简单

如果还没改变这个先⼊为主的概念，我怀你没有⼥朋友（😭 ）我们拥抱⼀下

## 为什么会有中断机制？

中断是⼀种协同机制，我觉得就是解决【当局者迷】的状况 现实中，你努⼒忘我没有昼夜的⼯作，如果再没有⼈告知你中断，你身体是吃不消的。 在多线程的场景中，有的线程可能迷失在怪圈⽆法⾃拔（⾃旋浪费资源），这时就可以⽤其他线程在 恰当的时机给它个中断通知，被“中断”的线程可以选择在恰当的时机选择跳出怪圈，最⼤化的利⽤资 源 那程序中如何中断？怎样识别是否中断？⼜如何处理中断呢？这就与上⽂提到的三个⽅法有关了

# interrupt() VS isInterrupted() VS interrupted()

Java 的每个线程对象⾥都有⼀个 boolean 类型的标识，代表是否有中断请求，可你寻遍 Thread 类你也 不会找到这个标识，因为这是通过底层 native ⽅法实现的。

### interrupt()

interrupt() ⽅法是 唯⼀⼀个 可以将上⾯提到中断标志设置为 true 的⽅法，从这⾥可以看出，这是⼀个 Thread 类 public 的对象⽅法，所以可以推断出任何线程对象都可以调⽤该⽅法，进⼀步说明就是可以 ⼀个线程 interrupt 其他线程，也可以 interrupt ⾃⼰。其中，中断标识的设置是通过 native ⽅ 法 interrupt0 完成的

![image 5](<Java并发编程的中断机制.note_images/imageFile5.png>)

在 Java 中，线程被中断的反应是不⼀样的，脾⽓不好的直接就抛出了 InterruptedException() ，

![image 6](<Java并发编程的中断机制.note_images/imageFile6.png>)

该⽅法注释上写的很清楚，当线程被阻塞在：

- 1.
- 2.
- 3.


wait() join() sleep()

这些⽅法时，如果被中断，就会抛出 InterruptedException 受检异常（也就是必须要求我们 catch 进⾏处 理的） 熟悉 JUC 的朋友可能知道，其实被中断抛出 InterruptedException 的远远不⽌这⼏个⽅法，⽐如：

![image 7](<Java并发编程的中断机制.note_images/imageFile7.png>)

反向推理，这些可能阻塞的⽅法如果声明有 throws InterruptedException ， 也就暗示我们它们是可 中断的 调⽤ interrput() ⽅法后，中断标识就被设置为 true 了，那我们怎么利⽤这个中断标识，来判断某个线程 中断标识到底什么状态呢？

### isInterrupted()

![image 8](<Java并发编程的中断机制.note_images/imageFile8.png>)

这个⽅法名起的⾮常好，因为⽐较符合我们 bean boolean 类型字段的 get ⽅法规范，没错，该⽅法就是 返回中断标识的结果：

true：线程被中断，

false：线程没被中断或被清空了中断标识（如何清空我们⼀会⼉看）

拿到这个标识后，线程就可以判断这个标识来执⾏后续的逻辑了。有起名好的，也有起名不好的，就 是下⾯这个⽅法：

### interrupted()

按照常规翻译，过去时时态，这就是“被打断了/被打断的”，其实和上⾯的 isInterrupted() ⽅法差不多， 两个⽅法都是调⽤ private 的 isInterrupted() ⽅法， 唯⼀差别就是会清空中断标识（这是从⽅法名中怎 么也看不出来的）

![image 9](<Java并发编程的中断机制.note_images/imageFile9.png>)

因为调⽤该⽅法，会返回当前中断标识，同时会清空中断标识，就有了那⼀段有点让⼈迷惑的⽅法注 释：

![image 10](<Java并发编程的中断机制.note_images/imageFile10.png>)

来段程序你就会明⽩上⾯注释的意思了：

Thread.currentThread().isInterrupted(); // true Thread.interrupted() // true， 返 回 true后 清 空 了中 断 标 识 将 其 置 为 false Thread.currentThread().isInterrupted(); // false Thread.interrupted() // false

这个⽅法总觉得很奇怪，现实中有什么⽤呢？

当你可能要被⼤量中断并且你想确保只处理⼀次中断时，就可以使⽤这个⽅法了

该⽅法在 JDK 源码中应⽤也⾮常多，⽐如（后续⽂章会具体分析，这⾥知道该⽅法的作⽤和使⽤场景 就好）：

![image 11](<Java并发编程的中断机制.note_images/imageFile11.png>)

相信到这⾥你已经能明确分辨三胞胎都是谁，并发挥怎样的作⽤了，那么有哪些场景我们可以使⽤中 断机制呢？

## 中断机制的使⽤场景

通常，中断的使⽤场景有以下⼏个

点击某个桌⾯应⽤中的关闭按钮时（⽐如你关闭 IDEA，不保存数据直接中断好吗？）；

某个操作超过了⼀定的执⾏时间限制需要中⽌时；

多个线程做相同的事情，只要⼀个线程成功其它线程都可以取消时；

⼀组线程中的⼀个或多个出现错误导致整组都⽆法继续时；

因为中断是⼀种协同机制，提供了更优雅中断⽅式，也提供了更多的灵活性，所以当遇到如上场景 等，我们就可以考虑使⽤中断机制了

## 使⽤中断机制有哪些注意事项

其实使⽤中断机制⽆⾮就是注意上⾯说的两项内容：

- 1.
- 2.


中断标识 InterruptedException

前浪已经将其总结为两个通⽤原则，我们后浪直接站在肩膀上⽤就可以了，来看⼀下这两个原则是什 么：

- 原则-1 如果遇到的是可中断的阻塞⽅法, 并抛出 InterruptedException，可以继续向⽅法调⽤栈的上层抛出该异常；如 果检测到中断，则可清除中断状态并抛出 InterruptedException，使当前⽅法也成为⼀个可中断的⽅法


- 原则-2 若有时候不太⽅便在⽅法上抛出 InterruptedException，⽐如要实现的某个接⼝中的⽅法签名上没有 throws InterruptedException，这时就可以捕获可中断⽅法的 InterruptedException 并通过 Thread.currentThread.interrupt() 来重新设置中断状态。 再通过个例⼦来加深⼀下理解：


本意是当前线程被中断之后，退出while(true), 你觉得代码有问题吗？（先不要向下看）

Thread th = Thread.currentThread(); while(true) {

if(th.isInterrupted()) { break;

} // 省 略 业 务 代 码 try {

Thread.sleep(100); }catch (InterruptedException e){

e.printStackTrace(); }

}

打开 Thread.sleep ⽅法：

![image 12](<Java并发编程的中断机制.note_images/imageFile12.png>)

sleep ⽅法抛出 InterruptedException后，中断标识也被清空置为 false，我们在catch 没有通过调⽤ th.interrupt() ⽅法再次将中断标识置为 true，这就导致⽆限循环了 这两个原则很好理解。总的来说，我们应该留意 InterruptedException，当我们捕获到该异常时，绝不可 以默默的吞掉它，什么也不做，因为这会导致上层调⽤栈什么信息也获取不到。其实在编写程序时， 捕获的任何受检异常我们都不应该吞掉

## JDK 中有哪些使⽤中断机制的地⽅呢？

中断机制贯穿整个并发编程中，这⾥只简单列觉⼤家经常会使⽤的，我们可以通过阅读JDK源码来进⼀ 步了解中断机制以及学习如何使⽤中断机制

### ThreadPoolExecutor

ThreadPoolExecutor 中的 shutdownNow ⽅法会遍历线程池中的⼯作线程并调⽤线程的 interrupt ⽅法来中 断线程

![image 13](<Java并发编程的中断机制.note_images/imageFile13.png>)

![image 14](<Java并发编程的中断机制.note_images/imageFile14.png>)

### FutureTask

FutureTask 中的 cancel ⽅法，如果传⼊的参数为 true，它将会在正在运⾏异步任务的线程上调⽤ interrupt ⽅法，如果正在执⾏的异步任务中的代码没有对中断做出响应，那么 cancel ⽅法中的参数将不 会起到什么效果

![image 15](<Java并发编程的中断机制.note_images/imageFile15.png>)

## 总结

到这⾥你应该理解Java 并发编程中断机制的含义了，它是⼀种协同机制，和你先⼊为主的概念完全不 ⼀样。区分了三个相近⽅法，说明了使⽤场景以及使⽤原则，同时⼜给出JDK源码⼀些常⻅案例，相信 你已经胸中有沟壑了，接下来，跟上节奏，我们陆续⾛进源码吧

## 灵魂追问

- 1.
- 2.
- 3.
- 4.


抛出 InterruptedException 后，中断标识就⼀定被清空吗？ 处在死锁状态的线程是否可以被中断呢？ 进⼊临界区的线程能否被中断呢？如果不能有什么办法能响应中断吗？ 个⼈感觉interrupted这个⽅法名称不是特别好，如果你也觉得不好，让你设计这个地⽅，你有什么 想法？

有朋友可能会问⽂章开头的图，同时看⼀个类的不同部分怎么实现的？不等您开⼝，我就全盘招了，其实就 是屏幕分割（在⽂件上⿏标右键->选择⽔平/垂直分割），这样在同时查看某些代码时还是很⽅便的（带⻥屏 垂直分割真是爽翻天），保姆式演示如下（由于公众号限制，完整动图查看原⽂吧）：

![image 16](<Java并发编程的中断机制.note_images/imageFile16.png>)

参考

- 1.
- 2.
- 3.
- 4.
- 5.


Java 并发编程实战 Java并发编程的艺术 https://www.infoq.cn/article/java-interrupt-mechanism https://coderanch.com/t/237332/certification/explain-interrupt-isInterrupted-interrupted-method https://dzone.com/articles/waiting-for-coroutines

