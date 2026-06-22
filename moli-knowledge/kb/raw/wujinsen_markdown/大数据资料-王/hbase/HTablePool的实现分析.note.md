- 1）基本概念 HTablePol
- 2）样例代码 [java]


3种类型

PolType.Reusable（默认）⼀个实例池，多线程复⽤，内部是每个table⼀个 ConcurentLinkedQueue装多个实例

PolType.ThreadLocal，很奇怪的实现，每个线程只能有⼀个实例，感觉在多线程的场景没有 意义

PolType.RoundRobin （没有被使⽤，就算设置了该类型也没⽤，⻅HTablePol的构造函数）

PolMap<String, HTableInterface> tables ：⽤于存放table实例，正如上⾯提到的默认是每个table 对应⼀个ConcurentLinkedQueue

maxSize:pol的最⼤尺⼨

view plaincopy publicclas HTablePolTest {

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


protectedstatic String TEST_TABLE_NAME = "testable";

protectedstatic String ROW1_STR = "row1"; protectedstatic String COLFAM1_STR = "colfam1"; protectedstatic String QUAL1_STR = "qual1";

privatefinalstaticbyte[] ROW1 = Bytes.toBytes(ROW1_STR); privatefinalstaticbyte[] COLFAM1 = Bytes.toBytes(COLFAM1_STR); privatefinalstaticbyte[] QUAL1 = Bytes.toBytes(QUAL1_STR);

privatestatic HTablePol pol;

@BeforeClas publicstaticvoid runBeforeClas() throws IOException {

Configuration conf = HBaseConfiguration.create(); pol = new HTablePol(conf, 10);

/ 填充pol HTableInterface[] tables = new HTableInterface[10]; for (int n = 0; n < 10; n+) {

tables[n] = pol.getTable(TEST_TABLE_NAME); }

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
- 36.
- 37.
- 38.
- 39.
- 40.
- 41.
- 42.
- 43.
- 44.
- 45.
- 46.
- 47.
- 48.
- 49.
- 50.
- 51.
- 52.
- 53.
- 54.
- 55.
- 56.
- 57.
- 58.
- 59.
- 60.
- 61.


for (HTableInterface table : tables) {

table.close(); }

}

@Test publicvoid testHTablePol() throws IOException, InteruptedException,

ExecutionException {

Calable<Result> calable = new Calable<Result>() { public Result cal() throws Exception {

return get(); }

};

- FutureTask<Result> task1 = new FutureTask<Result>(calable);
- FutureTask<Result> task2 = new FutureTask<Result>(calable);


Thread thread1 = new Thread(task1, "THREAD-1"); thread1.start(); Thread thread2 = new Thread(task2, "THREAD-2"); thread2.start();

- Result result1 = task1.get();

- asertThat(Bytes.toString(result1.getValue(COLFAM1, QUAL1), is("val1");

Result result2 = task2.get();

- asertThat(Bytes.toString(result2.getValue(COLFAM1, QUAL1), is("val1");




}

private Result get() { HTableInterface table = pol.getTable(TEST_TABLE_NAME); Get get = new Get(ROW1); try {

Result result = table.get(get);

- 62.
- 63.
- 64.
- 65.
- 66.
- 67.
- 68.
- 69.
- 70.
- 71.
- 72.
- 73.
- 74.
- 75.
- 76.
- 77.


return result; } catch (IOException e) {

/ TODO Auto-generated catch block e.printStackTrace(); returnnul;

} finaly { try {

table.close(); } catch (IOException e) {

/ TODO Auto-generated catch block e.printStackTrace();

} }

}

}

使⽤多线程访问HTablePol

- 3）关键点分析


- 3.1）初始化 HTablePol pol = new HTablePol(conf, 5);
- 3.2）获取table实例pol.getTable(TEST_TABLE_NAME);
- 3.3）table.get(get);
- 3.4）table.close();


实例化PolMap

实例化HTablePol，此时还没有任何HTable实例，tables为空

查看tables是否含有table,如果没有，创建⼀个HTable实例，HTable的初始化具体的细节⻅我的博 ⽂htp:/blog.csdn.net/pwlazy/article/details/7417135

将返回HTable实例封装成PoledHTable实例返回

PoledHTable是HTable的⼀个Wraper,除了close()不⼀样，PoledHTable的close会将HTable实例 返回到上⾯提到的tables中

所以tables确实存放的是HTable实例，但取出来丢给应⽤程序的就是PoledHTable实例

从regionserver获取数据

该动作具体的详细细节⻅我的博⽂htp:/blog.csdn.net/pwlazy/article/details/7417135

将table归还到HTablePol中，如果此时HTablePol尺⼨超过最⼤尺⼨，释放该实例，

关于释放HTable实例与释放连接的问题

HTable实例相关的两个连接，⼀个是对zokeper,⼀个是regionServer

如果没有其他HTable实例（在HTablePol尺⼨⼤于0的情况不可能出现这种情况），及没有 zokeper的连接计数为0，此时才会释放zokeper连接

regionServer的连接有HBaseClient$Conection这个线程单独维护，与HTable实例基本没啥关 系，注意HBaseClient$Conection这个线程绑定了连接4）总体看HTablePol

容纳了多个HTable实例

多个HTable实例会共享同⼀个zokeper连接

多个HTable实例，如果同在⼀个RegionServer会共享同⼀个连接HBaseClient$Conection

很容易让⼈误解每个HTable实例都有⼀个HBaseClient$Conection，就像连接池那样，其实不是

虽然HTablePol有最⼤尺⼨，但并没有限制HTable实例不得⼤于这个尺⼨，⼀旦超过这个尺⼨就会 实例化，但归还到实例池的时候，如果池满了会弃⽤，因此HTablePol就是⼀个对象池⽽不是连接 池

使⽤HTablePol的意义？《 HBase-The-Definitive-Guide 》 作者是这么说的

实例化HTable实例⽐较耗时，最好启动时初始化（这个理由不是很充分，完全可以使⽤HTable 单例）

HTable实例线程不安全，特别是在auto flash为false的情况，因为存在本地的write bufer ，即 使auto flash为true， 也不建议使⽤（对此作者并没说为什么）

建议每个线程⼀个HTable实例

HTablePol存在的问题

PoledHTable的代码很恶⼼，PoledHTable作为⼀个HTable的wraper,两者的关系应该是包 含，但源码中却是继承

HTablePol并不是连接池，就是直接使⽤HBaseClient$Conection【如果是同⼀个region的话 就是单线程】来完成⽹络通讯的，后者的问题在我的博⽂

htp:/blog.csdn.net/pwlazy/article/de tails/7417135

有提到， 的确存在多个线程使⽤单个HBaseClient$Conection⽽带来同步和阻塞 的问题，线上使⽤必须好好的压⼒测试⼀下

