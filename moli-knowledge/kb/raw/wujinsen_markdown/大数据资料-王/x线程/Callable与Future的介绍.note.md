Calable与 Future 两功能是Java在后续版本中为了适应多并法才加⼊的，Calable是类似于Runable的接⼝，实现 Calable接⼝的类和实现Runable的类都是可被其他线程执⾏的任务。 Calable的接⼝定义如下； public interface Calable<V> {

V cal() throws Exception;

} Calable和Runable的区别如下： I Calable定义的⽅法是cal，⽽Runable定义的⽅法是run。

I Calable的cal⽅法可以有返回值，⽽Runable的run⽅法不能有返回值。

I Calable的cal⽅法可抛出异常，⽽Runable的run⽅法不能抛出异常。 Future 介绍 Future表示异步计算的结果，它提供了检查计算是否完成的⽅法，以等待计算的完成，并检索计算的结果。Future的 cancel⽅法可以取消任务的执⾏，它有⼀布尔参数，参数为 true 表示⽴即中断任务的执⾏，参数为 false 表示允许正 在运⾏的任务运⾏完成。Future的 get ⽅法等待计算完成，获取计算结果 import java.util.concurent.Calable; import java.util.concurent.ExecutorService; import java.util.concurent.Executors; import java.util.concurent.Future; /*

- * Calable 和 Future接⼝
- * Calable是类似于Runable的接⼝，实现Calable接⼝的类和实现Runable的类都是可被其它线程执⾏的任务。
- * Calable和Runable有⼏点不同：
- * （1）Calable规定的⽅法是cal()，⽽Runable规定的⽅法是run().
- * （2）Calable的任务执⾏后可返回值，⽽Runable的任务是不能返回值的。
- * （3）cal()⽅法可抛出异常，⽽run()⽅法是不能抛出异常的。
- * （4）运⾏Calable任务可拿到⼀个Future对象，
- * Future 表示异步计算的结果。它提供了检查计算是否完成的⽅法，以等待计算的完成，并检索计算的结果。
- * 通过Future对象可了解任务执⾏情况，可取消任务的执⾏，还可获取任务执⾏的结果。
- */ public clas CalableAndFuture {


public static clas MyCalable implements Calable{ private int flag = 0; public MyCalable(int flag){

this.flag = flag;

} public String cal() throws Exception{

- if (this.flag = 0){ return "flag = 0";

}

- if (this.flag = 1){ try {


while (true) {

System.out.println("l oping."); Thread.sl ep(2 0);

} } catch (InteruptedException e) { System.out.println("Interupted");

} return "false";

} else {

throw new Exception("Bad flag value!"); }

}

} public static void main(String[] args) {

/ 定义3个Calable类型的任务

- MyCalable task1 = new MyCalable(0);
- MyCalable task2 = new MyCalable(1);
- MyCalable task3 = new MyCalable(2);


/ 创建⼀个执⾏任务的服务 ExecutorService es = Executors.newFixedThreadPol(3); try {

/ 提交并执⾏任务，任务启动时返回了⼀个Future对象， / 如果想得到任务执⾏的结果或者是异常可对这个Future对象进⾏操作

- Future future1 = es.submit(task1); / 获得第⼀个任务的结果，如果调⽤get⽅法，当前线程会等待任务执⾏完毕后才往下执⾏

- System.out.println("task1: " + future1.get();

Future future2 = es.submit(task2); / 等待5秒后，再停⽌第⼆个任务。因为第⼆个任务进⾏的是⽆限循环 Thread.sl ep(5 0);

- System.out.println("task2 cancel: " + future2.cancel(true);

/ 获取第三个任务的输出，因为执⾏第三个任务会引起异常

/ 所以下⾯的语句将引起异常的抛出 Future future3 = es.submit(task3);

- System.out.println("task3: " + future3.get();




} catch (Exception e){ System.out.println(e.toString();

} / 停⽌任务执⾏服务 es.shutdownNow();

}

# }

