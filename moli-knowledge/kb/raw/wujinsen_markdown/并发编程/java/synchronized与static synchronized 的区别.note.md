1.synchronized与static synchronized 的区别

synchronized是对类的当前实例进⾏加锁，防⽌其他线程同时访问该类的该实例的所有 synchronized块，注意这⾥是“类的当前实例”， 类的两个不同实例就没有这种约束了。那么static synchronized恰好就是要控制类的所有实例的访问了，static synchronized是限制线程同时访问 jvm中该类的所有实例同时访问对应的代码快。实际上，在类中某⽅法或某代码块中有 synchronized，那么在⽣成⼀个该类实例后，改类也就有⼀个监视快，放置线程并发访问改实例 synchronized保护快，⽽static synchronized则是所有该类的实例公⽤⼀个监视快了，也也就是两 个的区别了,也就是synchronized相当于 this.synchronized，⽽ static synchronized相当于Something.synchronized.

⼀个⽇本作者-结成浩的《java多线程设计模式》有这样的⼀个列⼦： pulbic class Something(){

public synchronized void isSyncA(){} public synchronized void isSyncB(){} public static synchronized void cSyncA(){} public static synchronized void cSyncB(){}

} 那么，加⼊有Something类的两个实例a与b，那么下列组⽅法何以被1个以上线程同时访问呢

- a. x.isSyncA()与x.isSyncB()
- b. x.isSyncA()与y.isSyncA()
- c. x.cSyncA()与y.cSyncB()
- d. x.isSyncA()与Something.cSyncA() 这⾥，很清楚的可以判断：


- a，都是对同⼀个实例的synchronized域访问，因此不能被同时访问
- b，是针对不同实例的，因此可以同时被访问
- c，因为是static synchronized，所以不同实例之间仍然会被限制,相当于Something.isSyncA()


与 Something.isSyncB()了，因此不能被同时访问。

那么，第d呢?，书上的 答案是可以被同时访问的，答案理由是synchronzied的是实例⽅法与 synchronzied的类⽅法由于锁定（lock）不同的原因。

个⼈分析也就是synchronized 与static synchronized 相当于两帮派，各⾃管各⾃，相互之间就 ⽆约束了，可以被同时访问。⽬前还不是分清楚java内部设计synchronzied是怎么样实现的。

结论：A: synchronized static是某个类的范围，synchronized static cSync{}防⽌多个线程同 时访问这个 类中的synchronized static ⽅法。它可以对类的所有对象实例起作⽤。

B: synchronized 是某实例的范围，synchronized isSync(){}防⽌多个线程同时访问 这个实例中的synchronized ⽅法。

2.synchronized⽅法与synchronized代码快的区别

synchronized methods(){} 与synchronized（this）{}之间没有什么区别，只是 synchronized methods(){} 便于阅读理 解，⽽synchronized（this）{}可以更精确的控制冲突限制访问区域，有时候表现更⾼效率。

3.synchronized关键字是不能继承的

http://www.learndiary.com/archives/diaries/2910.htm

这个在 ⼀⽂中看到的，我想这⼀点 也是很值得注意的，继承时⼦类的覆盖⽅法必须显示定义成synchronized

<table>
  <tr>
    <th>![image 1](<synchronized与static synchronized 的区别.note_images/imageFile1.png>)</th>
  </tr>
</table>


