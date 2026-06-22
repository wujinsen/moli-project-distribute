Callable 和 Runnable 的使⽤⽅法⼤同⼩异， 区别在于：

- 1.Callable 使⽤ call（） ⽅法， Runnable 使⽤ run() ⽅法

- 2.call() 可以返回值， ⽽ run()⽅法不能返回。

- 3.call() 可以抛出受检查的异常，⽐如ClassNotFoundException， ⽽run()不能抛出受检查的异常。 Callable示例如下：


Java代码 clas TaskWithResult implements Calable<String> { privateint id;

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.
- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.


public TaskWithResult(int id) {

this.id = id; }

@Overide public String cal() throws Exception {

return"result of TaskWithResult " + id; }

}

publicclas CalableTest { publicstaticvoid main(String[] args) throws InteruptedException,

ExecutionException { ExecutorService exec = Executors.newCachedThreadPol(); ArayList<Future<String> results = new ArayList<Future<String>(); /Future 相当于是

⽤来存放Executor执⾏的结果的⼀种容器 for (int i = 0; i < 10; i +) { results.ad(exec.submit(new TaskWithResult(i );

} for (Future<String> fs : results) {

if (fs.isDone() {

System.out.println(fs.get(); } else {

System.out.println("Future result is not yet complete"); }

} exec.shutdown();

- 30.
- 31.


} }

执⾏结果

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.


- result of TaskWithResult 0
- result of TaskWithResult 1
- result of TaskWithResult 2
- result of TaskWithResult 3
- result of TaskWithResult 4
- result of TaskWithResult 5
- result of TaskWithResult 6
- result of TaskWithResult 7
- result of TaskWithResult 8
- result of TaskWithResult 9


Runnable示例：

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.
- 12.
- 13.
- 14.
- 15.
- 16.
- 17.
- 18.


publicclas LiftOf implements Runable {

protectedint countDown = 10; privatestaticint taskCount = 0; privatefinalint id = taskCount+;

public LiftOf() {

}

public LiftOf(int countDown) {

this.countDown = countDown; }

public String status() {

return"#" + id + "(" + (countDown > 0 ? countDown : "LiftOf! ") + ")"; }

- 19.
- 20.
- 21.
- 22.
- 23.
- 24.
- 25.
- 26.
- 27.
- 28.
- 29.
- 30.
- 31.
- 32.
- 33.
- 34.
- 35.


@Overide publicvoid run() {

while (countDown- > 0) { System.out.print(status(); Thread.yield();

} System.out.println();

}

publicstaticvoid main(String[] args) { ExecutorService exec = Executors.newFixedThreadPol(1); for (int i = 0; i < 5; i +) {

exec.execute(new LiftOf();

} exec.shutdown();

} }

执⾏结果

Java代码

- 1.
- 2.
- 3.
- 4.
- 5.


- #0(9)#0(8)#0(7)#0(6)#0(5)#0(4)#0(3)#0(2)#0(1)#0(LiftOf! )
- #1(9)#1(8)#1(7)#1(6)#1(5)#1(4)#1(3)#1(2)#1(1)#1(LiftOf! )
- #2(9)#2(8)#2(7)#2(6)#2(5)#2(4)#2(3)#2(2)#2(1)#2(LiftOf! )
- #3(9)#3(8)#3(7)#3(6)#3(5)#3(4)#3(3)#3(2)#3(1)#3(LiftOf! )
- #4(9)#4(8)#4(7)#4(6)#4(5)#4(4)#4(3)#4(2)#4(1)#4(LiftOf! )


注意ExecutorService 在Callable中使⽤的是submit()， 在Runnable中使⽤的是 execute()

