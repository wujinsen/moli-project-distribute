如果⼀个线程没有明确设置 Uncaught Exception Handler的话，那么该线程所属的线程组实例会作为 handler。如果线程组没有对如何处理UE有特别要求的话，就会调⽤线程组默认的UEH来处理异常了。

setUncaughtExceptionHandler()的括号当中，必须要传⼊⼀个对象。 该对象将被设置成 指定线程 实例的 UncaughtExceptionHandler。 ⽽ UncaughtExceptionHandler本身就是⼀个类。 所以需要进⾏创建。

于是有 Thread t = new Thread ("thread1"); t.setUncaughtExceptionHandler(

new Thread.UncaughtExceptionHandler(){ …

注意红⾊部分。 在new UEH的时候，前⾯要加上Thread. 表明这是Thread的UEH

然后，UEH⾥最主要的⽅法呢，就是：UncaughtException了。 ⽽这个⽅法呢，JDK的解释是：pasing the thread and the exception as arguments 也就是把出现 未抓获异常的 线程实例 和 异常 ⼀起作为参数传进来。 然后，根据UncaughtException⽅法的具体内容，进⾏操作。

