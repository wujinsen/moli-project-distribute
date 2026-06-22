1 /**

CountDownLatch类是⼀个同步计数器,构造时传⼊int参数,该参数就是计数器的初始值，每调⽤⼀次 countDown()⽅法，计数器减1,计数器⼤于0 时，await()⽅法会阻塞程序继续执⾏

1

CountDownLatch如其所写，是⼀个倒计数的锁存器，当计数减⾄0时触发特定的事件。利⽤这种特性，可以 让主线程等待⼦线程的结束。下⾯以⼀个模拟运动员⽐赛的例⼦加以说明。

1

- 1 */


- 2 import java.util.concurrent.Executor;

- 3 import java.util.concurrent.ExecutorService;

- 4 import java.util.concurrent.Executors;

- 5

- 6 public class CountDownLatchDemo {

- 7 private static final int PLAYER_AMOUNT = 5;

- 8 public CountDownLatchDemo() {

- 9 // TODO Auto-generated constructor stub

- 10 }

- 11 /**

- 12 * @param args

- 13 */

- 14 public static void main(String[] args) {

- 15 // TODO Auto-generated method stub

- 16 //对于每位运动员，CountDownLatch减1后即结束⽐赛

- 17 CountDownLatch begin = new CountDownLatch(1);

- 18 //对于整个⽐赛，所有运动员结束后才算结束

- 19 CountDownLatch end = new CountDownLatch(PLAYER_AMOUNT);

- 20 Player[] plays = new Player[PLAYER_AMOUNT];

- 21

- 22 for(int i=0;i<PLAYER_AMOUNT;i++)

- 23 plays[i] = new Player(i+1,begin,end);

- 24

- 25 //设置特定的线程池，⼤⼩为5

- 26 ExecutorService exe = Executors.newFixedThreadPool(PLAYER_AMOUNT);

- 27 for(Player p:plays)

- 28 exe.execute(p); //分配线程

- 29 System.out.println("Race begins!");

- 30 begin.countDown();

- 31 try{

- 32 end.await(); //等待end状态变为0，即为⽐赛结束

- 33 }catch (InterruptedException e) {

- 34 // TODO: handle exception

- 35 e.printStackTrace();

- 36 }finally{

- 37 System.out.println("Race ends!");

- 38 }

- 39 exe.shutdown();


- 40 }

- 41 }


接下来是Player类

- 2

- 3

- 4 public class Player implements Runnable {

- 5

- 6 private int id;

- 7 private CountDownLatch begin;

- 8 private CountDownLatch end;

- 9 public Player(int i, CountDownLatch begin, CountDownLatch end) {

- 10 // TODO Auto-generated constructor stub

- 11 super();

- 12 this.id = i;

- 13 this.begin = begin;

- 14 this.end = end;

- 15 }

- 16

- 17 @Override

- 18 public void run() {

- 19 // TODO Auto-generated method stub

- 20 try{

- 21 begin.await(); //等待begin的状态为0

Thread.sleep((long)(Math.random()*100)); //随机分配时间，即运动员完 成时间

- 22

- 23 System.out.println("Play"+id+" arrived.");

- 24 }catch (InterruptedException e) {

- 25 // TODO: handle exception

- 26 e.printStackTrace();

- 27 }finally{

- 28 end.countDown(); //使end状态减1，最终减⾄0

- 29 }

- 30 }

- 31 }


