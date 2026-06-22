⼀、事务的基本原理 Spring事务 的本质其实就是数据库对事务的⽀持，没有数据库的事务⽀持，spring是⽆法提供事务功 能的。对于纯JDBC操作数据库，想要⽤到事务，可以按照以下步骤进⾏：

获取连接 Connection con = DriverManager.getConnection() 开启事务con.setAutoCommit(true/false); 执⾏CRUD 提交事务/回滚事务 con.commit() / con.rollback(); 关闭连接 conn.close();

使⽤Spring的事务管理功能后，我们可以不再写步骤 2 和 4 的代码，⽽是由Spirng ⾃动完成。 那 么Spring是如何在我们书写的 CRUD 之前和之后开启事务和关闭事务的呢？解决这个问题，也就可以 从整体上理解Spring的事务管理实现原理了。下⾯简单地介绍下，注解⽅式为例⼦

配置⽂件开启注解驱动，在相关的类和⽅法上通过注解@Transactional标识。 spring 在启动的时候会去解析⽣成相关的bean，这时候会查看拥有相关注解的类和⽅法，并且为

这些类和⽅法⽣成代理，并根据@Transaction的相关参数进⾏相关配置注⼊，这样就在代理中为我们 把相关的事务处理掉了（开启正常提交事务，异常回滚事务）。

真正的数据库层的事务提交和回滚是通过binlog或者redo log实现的。 ⼆、Spring 事务的传播属性 所谓spring事务的传播属性，就是定义在存在多个事务同时存在的时候，spring应该如何处理这些事 务的⾏为。这些属性在TransactionDefinition中定义，具体常量的解释⻅下表： 常量名称 常量解释 PROPAGATION_REQUIRED ⽀持当前事务，如果当前没有事务，就新建⼀个事务。这是最常⻅ 的选择，也是 Spring 默认的事务的传播。

PROPAGATION_REQUIRES_NEW 新建事务，如果当前存在事务，把当前事务挂起。新建的事 务将和被挂起的事务没有任何关系，是两个独⽴的事务，外层事务失败回滚之后，不能回滚内层事务 执⾏的结果，内层事务失败抛出异常，外层事务捕获，也可以不处理回滚操作

PROPAGATION_SUPPORTS ⽀持当前事务，如果当前没有事务，就以⾮事务⽅式执⾏。 PROPAGATION_MANDATORY ⽀持当前事务，如果当前没有事务，就抛出异常。 PROPAGATION_NOT_SUPPORTED 以⾮事务⽅式执⾏操作，如果当前存在事务，就把当前事务 挂起。 PROPAGATION_NEVER 以⾮事务⽅式执⾏，如果当前存在事务，则抛出异常。 PROPAGATION_NESTED 如果⼀个活动的事务存在，则运⾏在⼀个嵌套的事务中。如果没有活动事务，则按REQUIRED属性执 ⾏。它使⽤了⼀个单独的事务，这个事务拥有多个可以回滚的保存点。内部事务的回滚不会对外部事 务造成影响。它只对DataSourceTransactionManager事务管理器起效。 三、数据库隔离级别 隔离级别 隔离级别的值 导致的问题 Read-Uncommitted 0 导致脏读

Read-Committed 1 避免脏读，允许不可重复读和幻读 Repeatable-Read 2 避免脏读，不可重复读，允许幻读 Serializable 3 串⾏化读，事务只能⼀个⼀个执⾏，避免了脏读、不可重复读、幻读。执⾏效

率慢，使⽤时慎重 脏读：⼀事务对数据进⾏了增删改，但未提交，另⼀事务可以读取到未提交的数据。如果第⼀个事务 这时候回滚了，那么第⼆个事务就读到了脏数据。 不可重复读：⼀个事务中发⽣了两次读操作，第⼀次读操作和第⼆次操作之间，另外⼀个事务对数据 进⾏了修改，这时候两次读取的数据是不⼀致的。 幻读：第⼀个事务对⼀定范围的数据进⾏批量修改，第⼆个事务在这个范围增加⼀条数据，这时候第 ⼀个事务就会丢失对新增数据的修改。 总结： 隔离级别越⾼，越能保证数据的完整性和⼀致性，但是对并发性能的影响也越⼤。 ⼤多数的数据库默认隔离级别为 Read Commited，⽐如 SqlServer、Oracle 少数数据库默认隔离级别为：Repeatable Read ⽐如： MySQL InnoDB 四、Spring中的隔离级别 常量 解释 ISOLATION_DEFAULT 这是个 PlatfromTransactionManager 默认的隔离级别，使⽤数据库默 认的事务隔离级别。另外四个与 JDBC 的隔离级别相对应。 ISOLATION_READ_UNCOMMITTED 这是事务最低的隔离级别，它充许另外⼀个事务可以看到 这个事务未提交的数据。这种隔离级别会产⽣脏读，不可重复读和幻像读。 ISOLATION_READ_COMMITTED 保证⼀个事务修改的数据提交后才能被另外⼀个事务读取。另 外⼀个事务不能读取该事务未提交的数据。 ISOLATION_REPEATABLE_READ 这种事务隔离级别可以防⽌脏读，不可重复读。但是可能出现 幻像读。 ISOLATION_SERIALIZABLE 这是花费最⾼代价但是最可靠的事务隔离级别。事务被处理为顺序 执⾏。 五、事务的嵌套 通过上⾯的理论知识的铺垫，我们⼤致知道了数据库事务和spring事务的⼀些属性和特点，接下来我 们通过分析⼀些嵌套事务的场景，来深⼊理解spring事务传播的机制。 假设外层事务 Service A 的 Method A() 调⽤ 内层Service B 的 Method B() PROPAGATION_REQUIRED(spring 默认) 如果ServiceB.methodB() 的事务级别定义为 PROPAGATION_REQUIRED，那么执⾏

- ServiceA.methodA() 的时候spring已经起了事务，这时调⽤ ServiceB.methodB()，

- ServiceB.methodB() 看到⾃⼰已经运⾏在 ServiceA.methodA() 的事务内部，就不再起新的事 务。 假如 ServiceB.methodB() 运⾏的时候发现⾃⼰没有在事务中，他就会为⾃⼰分配⼀个事务。


这样，在 ServiceA.methodA() 或者在 ServiceB.methodB() 内的任何地⽅出现异常，事务都会被 回滚。 PROPAGATION_REQUIRES_NEW ⽐如我们设计 ServiceA.methodA() 的事务级别为 PROPAGATION_REQUIRED， ServiceB.methodB() 的事务级别为 PROPAGATION_REQUIRES_NEW。 那么当执⾏到 ServiceB.methodB() 的时候，ServiceA.methodA() 所在的事务就会挂起， ServiceB.methodB() 会起⼀个新的事务，等待 ServiceB.methodB() 的事务完成以后，它才继续 执⾏。 他与 PROPAGATION_REQUIRED 的事务区别在于事务的回滚程度了。因为 ServiceB.methodB() 是新起⼀个事务，那么就是存在两个不同的事务。如果 ServiceB.methodB() 已经提交，那么

- ServiceA.methodA() 失败回滚，ServiceB.methodB() 是不会回滚的。如果

- ServiceB.methodB() 失败回滚，如果他抛出的异常被 ServiceA.methodA() 捕获，


- ServiceA.methodA() 事务仍然可能提交(主要看B抛出的异常是不是A会回滚的异常)。 PROPAGATION_SUPPORTS 假设ServiceB.methodB() 的事务级别为 PROPAGATION_SUPPORTS，那么当执⾏到

- ServiceB.methodB()时，如果发现ServiceA.methodA()已经开启了⼀个事务，则加⼊当前的事 务，如果发现ServiceA.methodA()没有开启事务，则⾃⼰也不开启事务。这种时候，内部⽅法的事 务性完全依赖于最外层的事务。 PROPAGATION_NESTED 现在的情况就变得⽐较复杂了, ServiceB.methodB() 的事务属性被配置为 PROPAGATION_NESTED, 此时两者之间⼜将如何协作呢? ServiceB#methodB 如果 rollback, 那么内部事务(即 ServiceB#methodB) 将回滚到它执⾏前的 SavePoint ⽽外部事务(即 ServiceA#methodA) 可以有以下两种处理⽅式:


- a、捕获异常，执⾏异常分⽀逻辑 void methodA() {


try {

ServiceB.methodB(); } catch (SomeException) {

// 执⾏其他业务, 如 ServiceC.methodC(); }

} 这种⽅式也是嵌套事务最有价值的地⽅, 它起到了分⽀执⾏的效果, 如果 ServiceB.methodB 失败, 那么执⾏ ServiceC.methodC(), ⽽ ServiceB.methodB 已经回滚到它执⾏之前的 SavePoint, 所 以不会产⽣脏数据(相当于此⽅法从未执⾏过), 这种特性可以⽤在某些特殊的业务中, ⽽ PROPAGATION_REQUIRED 和 PROPAGATION_REQUIRES_NEW 都没有办法做到这⼀点。

- b、 外部事务回滚/提交 代码不做任何修改, 那么如果内部事务(ServiceB#methodB) rollback, 那 么⾸先 ServiceB.methodB 回滚到它执⾏之前的 SavePoint(在任何情况下都会如此), 外部事务(即 ServiceA#methodA) 将根据具体的配置决定⾃⼰是 commit 还是 rollback 另外三种事务传播属性基本⽤不到，在此不做分析。 六、总结 对于项⽬中需要使⽤到事务的地⽅，我建议开发者还是使⽤spring的TransactionCallback接⼝来实 现事务，不要盲⽬使⽤spring事务注解，如果⼀定要使⽤注解，那么⼀定要对spring事务的传播机制 和隔离级别有个详细的了解，否则很可能发⽣意想不到的效果。


