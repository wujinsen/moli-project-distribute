- 1.创建线程 在Java中创建线程有两种⽅法：使⽤Thread类和使⽤Runable接⼝。在使⽤Runable接⼝时需要建


⽴⼀个Thread实例。因此，⽆论是通过Thread类还是Runable接⼝建⽴线程，都必须建⽴Thread类或 它的⼦类的实例。Thread构造函数：

public Thread( );

public Thread(Runable target);

public Thread(String name);

public Thread(Runable target, String name);

public Thread(ThreadGroup group, Runable target);

public Thread(ThreadGroup group, String name);

public Thread(ThreadGroup group, Runable target, String name);

public Thread(ThreadGroup group, Runable target, String name, long stackSize);

⽅法⼀：继承Thread类覆盖run⽅法 代码如下:

复制代码

- public clas ThreadDemo1 { public static void main(String[] args){

Demo d = new Demo(); d.start(); for(int i=0;i<60;i +){

System.out.println(Thread.curentThread().getName()+i); }

}

} clas Demo extends Thread{

public void run(){ for(int i=0;i<60;i +){

System.out.println(Thread.curentThread().getName()+i); }

} }

⽅法⼆：

代码如下:

- public clas ThreadDemo2 { public static void main(String[] args){


复制代码

Demo2 d =new Demo2();

Thread t = new Thread(d); t.start(); for(int x=0;x<60;x+){

System.out.println(Thread.curentThread().getName()+x); }

}

} clas Demo2 implements Runable{

public void run(){ for(int x=0;x<60;x+){

System.out.println(Thread.curentThread().getName()+x); }

} }

- 2.线程的⽣命周期 与⼈有⽣⽼病死⼀样，线程也同样要经历开始（等待）、运⾏、挂起和停⽌四种不同的状态。这四种


状态都可以通过Thread类中的⽅法进⾏控制。下⾯给出了Thread类中和这四种状态相关的⽅法。

/ 开始线程

publicvoid start( );

publicvoid run( ); / 挂起和唤醒线程

publicvoid resume( ); / 不建议使⽤

publicvoid suspend( ); / 不建议使⽤

publicstaticvoid sl ep(long milis);

publicstaticvoid sl ep(long milis, int nanos);

/ 终⽌线程

publicvoid stop( ); / 不建议使⽤

publicvoid interupt( );

/ 得到线程状态

publicbolean isAlive( );

publicbolean isInterupted( );

publicstaticbolean interupted( );

/ join⽅法

publicvoid join( ) throws InteruptedException;

线程在建⽴后并不⻢上执⾏run⽅法中的代码，⽽是处于等待状态。线程处于等待状态时，可以通过 Thread类的⽅法来设置线程不各种属性，如线程的优先级（setPriority）、线程名(setName)和线程的 类型（setDaemon）等。

当调⽤start⽅法后，线程开始执⾏run⽅法中的代码。线程进⼊运⾏状态。可以通过Thread类的 isAlive⽅法来判断线程是否处于运⾏状态。当线程处于运⾏状态时，isAlive返回true，当isAlive返回 false时，可能线程处于等待状态，也可能处于停⽌状态。下⾯的代码演示了线程的创建、运⾏和停⽌ 三个状态之间的切换，并输出了相应的isAlive返回值。

⼀但线程开始执⾏run⽅法，就会⼀直到这个run⽅法执⾏完成这个线程才退出。但在线程执⾏的过程 中，可以通过两个⽅法使线程暂时停⽌执⾏。这两个⽅法是suspend和sl ep。在使⽤suspend挂起线 程后，可以通过resume⽅法唤醒线程。⽽使⽤sl ep使线程休眠后，只能在设定的时间后使线程处于就 绪状态（在线程休眠结束后，线程不⼀定会⻢上执⾏，只是进⼊了就绪状态，等待着系统进⾏调 度）。 在使⽤sl ep⽅法时有两点需要注意：

- 1. sl ep⽅法有两个重载形式，其中⼀个重载形式不仅可以设毫秒，⽽且还可以设纳秒(1, 0, 0纳秒 等于1毫秒)。但⼤多数操作系统平台上的Java虚拟机都⽆法精确到纳秒，因此，如果对sl ep设置了纳 秒，Java虚拟机将取最接近这个值的毫秒。
- 2. 在使⽤sl ep⽅法时必须使⽤throws或try{.}catch{.}。因为run⽅法⽆法使⽤throws，所以只能使 ⽤try{.}catch{.}。当在线程休眠的过程中，使⽤interupt⽅法中断线程时sl ep会抛出⼀个 InteruptedException异常。sl ep⽅法的定义如下：

有三种⽅法可以使终⽌线程。

- 1. 使⽤退出标志，使线程正常退出，也就是当run⽅法完成后线程终⽌。
- 2. 使⽤stop⽅法强⾏终⽌线程（这个⽅法不推荐使⽤，因为stop和suspend、resume⼀样，也可能发 ⽣不可预料的结果）。
- 3. 使⽤interupt⽅法中断线程。


1. 使⽤退出标志终⽌线程

当run⽅法执⾏完后，线程就会退出。但有时run⽅法是永远不会结束的。如在服务端程序中使⽤线程 进⾏监听客户端请求，或是其他的需要循环处理的任务。在这种情况下，⼀般是将这些任务放在⼀个 循环中，如while循环。如果想让循环永远运⾏下去，可以使⽤while(true){.}来处理。但要想使while 循环在某⼀特定条件下退出，最直接的⽅法就是设⼀个bolean类型的标志，并通过设置这个标志为 true或false来控制while循环是否退出。下⾯给出了⼀个利⽤退出标志终⽌线程的例⼦。

join⽅法的功能就是使异步执⾏的线程变成同步执⾏。也就是说，当调⽤线程实例的start⽅法后，这 个⽅法会⽴即返回，如果在调⽤start⽅法后后需要使⽤⼀个由这个线程计算得到的值，就必须使⽤join ⽅法。如果不使⽤join⽅法，就不能保证当执⾏到start⽅法后⾯的某条语句时，这个线程⼀定会执⾏ 完。⽽使⽤join⽅法后，直到这个线程退出，程序才会往下执⾏。下⾯的代码演示了join的⽤法。

- 3.多线程安全问题


- 1.
- 2.


publicstaticvoid sl ep(long milis) throws InteruptedException publicstaticvoid sl ep(long milis, int nanos) throws InteruptedException

问题原因：当多条语句在操作同⼀个线程共享数据时，⼀个线程对多条语句只执⾏了⼀部分，还没执 ⾏完，另⼀个线程参与进来执⾏，导致共享数据的错误。

解决办法：对多条操作共享数据的语句，只能让⼀个线程都执⾏完，在执⾏过程中，其他线程不执 ⾏。 同步代码块：

代码如下:

复制代码

- public clas ThreadDemo3 { public static void main(String[] args){


Ticket t =new Ticket();

- Thread t1 = new Thread(t,"窗⼝⼀");
- Thread t2 = new Thread(t,"窗⼝⼆");
- Thread t3 = new Thread(t,"窗⼝三");
- Thread t4 = new Thread(t,"窗⼝四");


- t1.start();
- t2.start();
- t3.start();
- t4.start();


}

} clas Ticket implements Runable{

private int ticket =40; public void run(){

while(true){ synchronized (new Object() { try { Thread.sl ep(1); } catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} if(ticket<=0)

break;

System.out.println(Thread.curentThread().getName()+" -卖出"+ticket-); }

} }

}

同步函数

代码如下: public clas ThreadDemo3 { public static void main(String[] args){ Ticket t =new Ticket();

复制代码

- Thread t1 = new Thread(t,"窗⼝⼀");
- Thread t2 = new Thread(t,"窗⼝⼆");
- Thread t3 = new Thread(t,"窗⼝三");
- Thread t4 = new Thread(t,"窗⼝四"); t1.start(); t2.start(); t3.start(); t4.start();


}

} clas Ticket implements Runable{

private int ticket = 4 0; public synchronized void saleTicket(){

if(ticket>0) System.out.println(Thread.curentThread().getName()+"卖出了"+ticket-);

} public void run(){

while(true){

saleTicket(); }

} }

同步函数锁是this 静态同步函数锁是class 线程间的通信

代码如下: public clas ThreadDemo3 { public static void main(String[] args){ clas Person{ public String name;

复制代码

private String gender; public void set(String name,String gender){

this.name =name; this.gender =gender;

} public void get(){

System.out.println(this.name+"."+this.gender); }

} final Person p =new Person(); new Thread(new Runable(){

public void run(){ int x=0; while(true){

if(x=0){

p.set("张三", "男"); }else{

p.set("lili", "nv");

} x=(x+1)%2;

}

} }).start(); new Thread(new Runable(){

public void run(){

while(true){ p.get(); }

}

}).start(); }

} /* 张三 .男 张三 .男 lili .nv lili .男

张三 .nv lili .男

*/ 修改上⾯代码

代码如下:

复制代码

- public clas ThreadDemo3 { public static void main(String[] args){


clas Person{ public String name; private String gender; public void set(String name,String gender){

this.name =name; this.gender =gender;

} public void get(){

System.out.println(this.name+"."+this.gender); }

} final Person p =new Person(); new Thread(new Runable(){

public void run(){ int x=0; while(true){

synchronized (p) { if(x=0){

p.set("张三", "男"); }else{

p.set("lili", "nv");

} x=(x+1)%2;

}

}

} }).start(); new Thread(new Runable(){

public void run(){ while(true){ synchronized (p) {

p.get(); }

} }

}).start(); }

} /* lili .nv lili .nv lili .nv lili .nv lili .nv lili .nv 张三 .男 张三 .男 张三 .男 张三 .男 */

等待唤醒机制

代码如下: /*

复制代码

- *线程等待唤醒机制
- *等待和唤醒必须是同⼀把锁
- */


- public clas ThreadDemo3 { private static bolean flags =false; public static void main(String[] args){


clas Person{ public String name; private String gender;

public void set(String name,String gender){ this.name =name; this.gender =gender;

} public void get(){

System.out.println(this.name+"."+this.gender); }

} final Person p =new Person(); new Thread(new Runable(){

public void run(){ int x=0; while(true){

synchronized (p) { if(flags) try { p.wait(); } catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

}; if(x=0){

p.set("张三", "男"); }else{

p.set("lili", "nv");

} x=(x+1)%2; flags =true; p.notifyAl();

} }

} }).start(); new Thread(new Runable(){

public void run(){ while(true){ synchronized (p) {

if(!flags) try {

p.wait(); } catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

}; p.get(); flags =false; p.notifyAl(); }

} }

}).start(); }

} ⽣产消费机制⼀

代码如下:

复制代码

- public clas ThreadDemo4 { private static bolean flags =false; public static void main(String[] args){


clas Gods{ private String name; private int num; public synchronized void produce(String name){

if(flags) try { wait(); } catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} this.name =name+"编号："+num+; System.out.println("⽣产了 ."+this.name); flags =true; notifyAl();

public synchronized void consume(){

if(!flags) try {

wait(); } catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} System.out.println("消费了 *"+name); flags =false; notifyAl();

}

} final Gods g =new Gods(); new Thread(new Runable(){

public void run(){ while(true){

g.produce("商品"); }

} }).start(); new Thread(new Runable(){

public void run(){ while(true){

g.consume(); }

}

}).start(); }

} ⽣产消费机制2

代码如下:

复制代码

- public clas ThreadDemo4 { private static bolean flags =false;


public static void main(String[] args){

clas Gods{ private String name; private int num; public synchronized void produce(String name){

while(flags) try { wait(); } catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} this.name =name+"编号："+num+; System.out.println(Thread.curentThread().getName()+"⽣产了 ."+this.name); flags =true; notifyAl();

} public synchronized void consume(){

while(!flags) try { wait(); } catch (InteruptedException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} System.out.println(Thread.curentThread().getName()+"消费了 *"+name); flags =false; notifyAl();

}

} final Gods g =new Gods(); new Thread(new Runable(){

public void run(){ while(true){

g.produce("商品"); }

},"⽣产者⼀号").start(); new Thread(new Runable(){

public void run(){ while(true){

g.produce("商品"); }

} },"⽣产者⼆号").start(); new Thread(new Runable(){

public void run(){ while(true){

g.consume(); }

} },"消费者⼀号").start(); new Thread(new Runable(){

public void run(){ while(true){

g.consume(); }

}

},"消费者⼆号").start(); }

} /* 消费者⼆号消费了 *商品编号：48049

- ⽣产者⼀号⽣产了 .商品编号：48050 消费者⼀号消费了 *商品编号：48050
- ⽣产者⼀号⽣产了 .商品编号：48051


- 消费者⼆号消费了 *商品编号：48051 ⽣产者⼆号⽣产了 .商品编号：48052
- 消费者⼆号消费了 *商品编号：48052


- ⽣产者⼀号⽣产了 .商品编号：48053 消费者⼀号消费了 *商品编号：48053
- ⽣产者⼀号⽣产了 .商品编号：48054 消费者⼆号消费了 *商品编号：48054


⽣产者⼆号⽣产了 .商品编号：4805 消费者⼆号消费了 *商品编号：4805

*/

