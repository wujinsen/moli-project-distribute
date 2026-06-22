## @transactional注解在什么情况下会失效，为什么。

这⼏天在项⽬⾥⾯发现我使⽤@Transactional注解事务之后，抛了异常居然不回滚。后来终于找到了 原因。 如果你也出现了这种情况，可以从下⾯开始排查。

# ⼀、特性

先来了解⼀下@Transactional注解事务的特性吧，可以更好排查问题

- 1、service类标签(⼀般不建议在接⼝上)上添加@Transactional，可以将整个类纳⼊spring事务管理， 在每个业务⽅法执⾏时都会开启⼀个事务，不过这些事务采⽤相同的管理⽅式。
- 2、@Transactional 注解只能应⽤到 public 可⻅度的⽅法上。 如果应⽤在protected、private或者 package可⻅度的⽅法上，也不会报错，不过事务设置不会起作⽤。
- 3、默认情况下，Spring会对unchecked异常进⾏事务回滚；如果是checked异常则不回滚。 辣么什么是checked异常，什么是unchecked异常 java⾥⾯将派⽣于Error或者RuntimeException（⽐如空指针，1/0）的异常称为unchecked异常，其他 继承⾃java.lang.Exception得异常统称为Checked Exception，如IOException、TimeoutException等 辣么再通俗⼀点：你写代码出现的空指针等异常，会被回滚，⽂件读写，⽹络出问题，spring就没法回 滚了。然后我教⼤家怎么记这个，因为很多同学容易弄混，你写代码的时候有些IOException我们的编 译器是能够检测到的，说以叫checked异常，你写代码的时候空指针等死检测不到的，所以叫 unchecked异常。这样是不是好记⼀些啦
- 4、只读事务： @Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true) 只读标志只在事务启动时应⽤，否则即使配置也会被忽略。 启动事务会增加线程开销，数据库因共享读取⽽锁定(具体跟数据库类型和事务隔离级别有关)。通常情 况下，仅是读取数据时，不必设置只读事务⽽增加额外的系统开销。 ⼆：事务传播模式 Propagation枚举了多种事务传播模式，部分列举如下：


- 1、REQUIRED(默认模式)：业务⽅法需要在⼀个容器⾥运⾏。如果⽅法运⾏时，已经处在⼀个事务 中，那么加⼊到这个事务，否则⾃⼰新建⼀个新的事务。

- 2、NOT_SUPPORTED：声明⽅法不需要事务。如果⽅法没有关联到⼀个事务，容器不会为他开启 事务，如果⽅法在⼀个事务中被调⽤，该事务会被挂起，调⽤结束后，原先的事务会恢复执⾏。

- 3、REQUIRESNEW：不管是否存在事务，该⽅法总汇为⾃⼰发起⼀个新的事务。如果⽅法已经运 ⾏在⼀个事务中，则原有事务挂起，新的事务被创建。

- 4、 MANDATORY：该⽅法只能在⼀个已经存在的事务中执⾏，业务⽅法不能发起⾃⼰的事务。如 果在没有事务的环境下被调⽤，容器抛出例外。

- 5、SUPPORTS：该⽅法在某个事务范围内被调⽤，则⽅法成为该事务的⼀部分。如果⽅法在该事 务范围外被调⽤，该⽅法就在没有事务的环境下执⾏。


- 6、NEVER：该⽅法绝对不能在事务范围内执⾏。如果在就抛例外。只有该⽅法没有关联到任何事 务，才正常执⾏。

- 7、NESTED：如果⼀个活动的事务存在，则运⾏在⼀个嵌套的事务中。如果没有活动事务，则按 REQUIRED属性执⾏。它使⽤了⼀个单独的事务，这个事务拥有多个可以回滚的保存点。内部事务 的回滚不会对外部事务造成影响。它只对DataSourceTransactionManager事务管理器起效。


上⾯引⽤⾄

事务传播模式

# ⼆：解决Transactional注解不回滚

- 1、检查你⽅法是不是public的
- 2、你的异常类型是不是unchecked异常 如果我想check异常也想回滚怎么办，注解上⾯写明异常类型即可 @Transactional(rollbackFor=Exception.class)

类似的还有norollbackFor，⾃定义不回滚的异常

- 3、数据库引擎要⽀持事务，如果是MySQL，注意表要使⽤⽀持事务的引擎，⽐如innodb，如果是 myisam，事务是不起作⽤的
- 4、是否开启了对注解的解析 <tx:annotation-driven transaction-manager="transactionManager" proxy-target-class="true"/>

- 5、spring是否扫描到你这个包，如下是扫描到org.test下⾯的包 <context:component-scan base-package="org.test" ></context:component-scan>

- 6、检查是不是同⼀个类中的⽅法调⽤（如a⽅法调⽤同⼀个类中的b⽅法）
- 7、异常是不是被你catch住了


