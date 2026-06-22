htps:/mp.weixin.q.com/s/zwSGKkFBLH7h6RhMKUMClg

Spring事务的基本原理

Spring事务的本质其实就是数据库对事务的⽀持，没有数据库的事务⽀持，spring是⽆法提供事务功能的。对 于纯JDBC操作数据库，想要⽤到事务，可以按照以下步骤进⾏：

- 1.
- 2.
- 3.
- 4.
- 5.


获取连接 Conection con = DriverManager.getConection() 开启事务con.setAutoComit(true/false); 执⾏CRUD 提交事务/回滚事务 con.comit() / con.rolback(); 关闭连接 con.close();

使⽤Spring的事务管理功能后，我们可以不再写步骤 2 和 4 的代码，⽽是由Spirng ⾃动完成。那么Spring是 如何在我们书写的 CRUD 之前和之后开启事务和关闭事务的呢？解决这个问题，也就可以从整体上理解 Spring的事务管理实现原理了。 下⾯简单地介绍下，注解⽅式为例⼦

- 1.
- 2.
- 3.


配置⽂件开启注解驱动，在相关的类和⽅法上通过注解@Transactional标识。 spring 在启动的时候会去解析⽣成相关的bean，这时候会查看拥有相关注解的类和⽅法，并且为 这些类和⽅法⽣成代理，并根据@Transaction的相关参数进⾏相关配置注⼊，这样就在代理中为 我们把相关的事务处理掉了（开启正常提交事务，异常回滚事务）。 真正的数据库层的事务提交和回滚是通过binlog或者redo log实现的。

Spring的事务机制

所有的数据访问技术都有事务处理机制，这些技术提供了API⽤来开启事务、提交事务来完成数据操作，或者 在发⽣错误的时候回滚数据。 ⽽Spring的事务机制是⽤统⼀的机制来处理不同数据访问技术的事务处理。Spring的事务机制提供了⼀个 PlatformTransactionManager接⼝，不同的数据访问技术的事务使⽤不同的接⼝实现，如表所示。 数据访问技术及实现

![image 1](<一文带你深入理解 Spring 事务原理.note_images/imageFile1.png>)

在程序中定义事务管理器的代码如下：

@Bean public PlatformTransactionManager transactionManager() {

JpaTransactionManager transactionManager = new JpaTransactionManager(); transactionManager.setDataSource(dataSource()); return transactionManager;

}

声明式事务

Spring⽀持声明式事务，即使⽤注解来选择需要使⽤事务的⽅法，它使⽤@Transactional注解在⽅法上表明 该⽅法需要事务⽀持。这是⼀个基于AOP的实现操作。

@Transactional public void saveSomething(Long id, String name) {

//数据库操作 }

在此处需要特别注意的是，此@Transactional注解来⾃org.springframework.transaction.anotation包，⽽ 不是javax.transaction。

AOP 代理的两种实现：

jdk是代理接⼝，私有⽅法必然不会存在在接⼝⾥，所以就不会被拦截到； cglib是⼦类，private的⽅法照样不会出现在⼦类⾥，也不能被拦截。

Java 动态代理。

具体有如下四步骤：

- 1.
- 2.
- 3.
- 4.


通过实现 InvocationHandler 接⼝创建⾃⼰的调⽤处理器； 通过为 Proxy 类指定 ClasLoader 对象和⼀组 interface 来创建动态代理类； 通过反射机制获得动态代理类的构造函数，其唯⼀参数类型是调⽤处理器接⼝类型； 通过构造函数创建动态代理类实例，构造时调⽤处理器对象作为参数被传⼊。

GCLIB代理

cglib（Code Generation Library）是⼀个强⼤的,⾼性能,⾼质量的Code⽣成类库。它可以在运⾏期扩展Java 类与实现Java接⼝。

cglib封装了asm，可以在运⾏期动态⽣成新的clas（⼦类）。 cglib⽤于AOP，jdk中的proxy必须基于接⼝，cglib却没有这个限制。

原理区别：

java动态代理是利⽤反射机制⽣成⼀个实现代理接⼝的匿名类，在调⽤具体⽅法前调⽤InvokeHandler来处 理。⽽cglib动态代理是利⽤asm开源包，对代理对象类的clas⽂件加载进来，通过修改其字节码⽣成⼦类来 处理。

- 1.
- 2.
- 3.


如果⽬标对象实现了接⼝，默认情况下会采⽤JDK的动态代理实现AOP 如果⽬标对象实现了接⼝，可以强制使⽤CGLIB实现AOP 如果⽬标对象没有实现了接⼝，必须采⽤CGLIB库，spring会⾃动在JDK动态代理和CGLIB之间转 换

如果是类内部⽅法直接不是⾛代理，这个时候可以通过维护⼀个⾃身实例的代理。

@Service public class PersonServiceImpl implements PersonService {

@Autowired PersonRepository personRepository;

// 注⼊⾃身代理对象，在本类内部⽅法调⽤事务的传递性才会⽣效 @Autowired PersonService selfProxyPersonService;

/**

- * 测试事务的传递性

*

- * @param person

- * @return

- */


@Transactional public Person save(Person person) {

Person p = personRepository.save(person); try {

// 新开事务 独⽴回滚 selfProxyPersonService.delete();

} catch (Exception e) { e.printStackTrace();

} try {

// 使⽤当前事务 全部回滚 selfProxyPersonService.save2(person);

} catch (Exception e) { e.printStackTrace();

} personRepository.save(person);

return p; }

@Transactional public void save2(Person person) {

personRepository.save(person); throw new RuntimeException();

}

@Transactional(propagation = Propagation.REQUIRES_NEW) public void delete() {

personRepository.delete(1L); throw new RuntimeException();

} }

Spring 事务的传播属性

所谓spring事务的传播属性，就是定义在存在多个事务同时存在的时候，spring应该如何处理这些事务的⾏ 为。这些属性在TransactionDefinition中定义，具体常量的解释⻅下表：

![image 2](<一文带你深入理解 Spring 事务原理.note_images/imageFile2.png>)

数据库隔离级别

![image 3](<一文带你深入理解 Spring 事务原理.note_images/imageFile3.png>)

脏读：⼀事务对数据进⾏了增删改，但未提交，另⼀事务可以读取到未提交的数据。如果第⼀个事务这时候 回滚了，那么第⼆个事务就读到了脏数据。 不可重复读：⼀个事务中发⽣了两次读操作，第⼀次读操作和第⼆次操作之间，另外⼀个事务对数据进⾏了 修改，这时候两次读取的数据是不⼀致的。 幻读：第⼀个事务对⼀定范围的数据进⾏批量修改，第⼆个事务在这个范围增加⼀条数据，这时候第⼀个事 务就会丢失对新增数据的修改。 总结： 隔离级别越⾼，越能保证数据的完整性和⼀致性，但是对并发性能的影响也越⼤。 ⼤多数的数据库默认隔离级别为 Read Comited，⽐如 SqlServer、Oracle 少数数据库默认隔离级别为：Repeatable Read ⽐如：MySQL I noDB

Spring中的隔离级别

![image 4](<一文带你深入理解 Spring 事务原理.note_images/imageFile4.png>)

事务的嵌套

通过上⾯的理论知识的铺垫，我们⼤致知道了数据库事务和spring事务的⼀些属性和特点，接下来我们通过分 析⼀些嵌套事务的场景，来深⼊理解spring事务传播的机制。 假设外层事务 Service A 的 Method A() 调⽤ 内层Service B 的 Method B() PROPAGATION_REQUIRED(spring 默认) 如果ServiceB.methodB() 的事务级别定义为 PROPAGATION_REQUIRED，那么执⾏ ServiceA.methodA() 的 时候spring已经起了事务，这时调⽤ ServiceB.methodB()，ServiceB.methodB() 看到⾃⼰已经运⾏在

- ServiceA.methodA() 的事务内部，就不再起新的事务。 假如 ServiceB.methodB() 运⾏的时候发现⾃⼰没有在事务中，他就会为⾃⼰分配⼀个事务。 这样，在 ServiceA.methodA() 或者在 ServiceB.methodB() 内的任何地⽅出现异常，事务都会被回滚。 PROPAGATION_REQUIRES_NEW ⽐如我们设计 ServiceA.methodA() 的事务级别为 PROPAGATION_REQUIRED，ServiceB.methodB() 的事务 级别为 PROPAGATION_REQUIRES_NEW。 那么当执⾏到 ServiceB.methodB() 的时候，ServiceA.methodA() 所在的事务就会挂起，

- ServiceB.methodB() 会起⼀个新的事务，等待 ServiceB.methodB() 的事务完成以后，它才继续执⾏。 他与 PROPAGATION_REQUIRED 的事务区别在于事务的回滚程度了。因为 ServiceB.methodB() 是新起⼀个 事务，那么就是存在两个不同的事务。如果 ServiceB.methodB() 已经提交，那么 ServiceA.methodA() 失败 回滚，ServiceB.methodB() 是不会回滚的。如果 ServiceB.methodB() 失败回滚，如果他抛出的异常被


- ServiceA.methodA() 捕获，ServiceA.methodA() 事务仍然可能提交(主要看B抛出的异常是不是A会回滚的异 常)。 PROPAGATION_SUPORTS 假设ServiceB.methodB() 的事务级别为 PROPAGATION_SUPORTS，那么当执⾏到ServiceB.methodB() 时，如果发现ServiceA.methodA()已经开启了⼀个事务，则加⼊当前的事务，如果发现ServiceA.methodA() 没有开启事务，则⾃⼰也不开启事务。这种时候，内部⽅法的事务性完全依赖于最外层的事务。 PROPAGATION_NESTED 现在的情况就变得⽐较复杂了, ServiceB.methodB() 的事务属性被配置为 PROPAGATION_NESTED, 此时两 者之间⼜将如何协作呢? ServiceB#methodB 如果 rolback, 那么内部事务(即 ServiceB#methodB) 将回滚到 它执⾏前的 SavePoint ⽽外部事务(即 ServiceA#methodA) 可以有以下两种处理⽅式:


- a、捕获异常，执⾏异常分⽀逻辑 void methodA() {


try {

ServiceB.methodB();

} catch (SomeException) {

// 执⾏其他业务, 如 ServiceC.methodC();

}

}

这种⽅式也是嵌套事务最有价值的地⽅, 它起到了分⽀执⾏的效果, 如果 ServiceB.methodB 失败, 那么执⾏

- ServiceC.methodC(), ⽽ ServiceB.methodB 已经回滚到它执⾏之前的 SavePoint, 所以不会产⽣脏数据(相当 于此⽅法从未执⾏过), 这种特性可以⽤在某些特殊的业务中, ⽽ PROPAGATION_REQUIRED 和 PROPAGATION_REQUIRES_NEW 都没有办法做到这⼀点。


- b、 外部事务回滚/提交 代码不做任何修改， 那么如果内部事务(ServiceB#methodB) rolback, 那么⾸先


- ServiceB.methodB 回滚到它执⾏之前的 SavePoint(在任何情况下都会如此), 外部事务(即 ServiceA#methodA) 将根据具体的配置决定⾃⼰是 comit 还是 rolback 另外三种事务传播属性基本⽤不到，在此不做分析。 总结 对于项⽬中需要使⽤到事务的地⽅，我建议开发者还是使⽤spring的TransactionCalback接⼝来实现事务， 不要盲⽬使⽤spring事务注解，如果⼀定要使⽤注解，那么⼀定要对spring事务的传播机制和隔离级别有个详 细的了解，否则很可能发⽣意想不到的效果。 Spring Bot 对事务的⽀持 通过 org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration 类。我们可以看出Spring Bot⾃动开启了对注解事务的⽀持 Spring 只读事务（@Transactional(readOnly = true)）的⼀些概念 概念： 从这⼀点设置的时间点开始（时间点a）到这个事务结束的过程中，其他事务所提交的数据，该事务将看不 ⻅！（查询中不会出现别⼈在时间点a之后提交的数据）。 @Transcational(readOnly=true) 这个注解⼀般会写在业务类上，或者其⽅法上，⽤来对其添加事务控制。当 括号中添加readOnly=true, 则会告诉底层数据源，这个是⼀个只读事务，对于JDBC⽽⾔，只读事务会有⼀定 的速度优化。 ⽽这样写的话，事务控制的其他配置则采⽤默认值，事务的隔离级别(isolation) 为DEFAULT,也就是跟随底层 数据源的隔离级别，事务的传播⾏为(propagation)则是REQUIRED，所以还是会有事务存在，⼀代在代码中 抛出RuntimeException，依然会导致事务回滚。 应⽤场合：


如果你⼀次执⾏单条查询语句，则没有必要启⽤事务⽀持，数据库默认⽀持SQL执⾏期间的读⼀致 性； 如果你⼀次执⾏多条查询语句，例如统计查询，报表查询，在这种场景下，多条查询SQL必须保证 整体的读⼀致性，否则，在前条SQL查询之后，后条SQL查询之前，数据被其他⽤户改变，则该次 整体的统计查询将会出现读数据不⼀致的状态，此时，应该启⽤事务⽀持。

【注意是⼀次执⾏多次查询来统计某些信息，这时为了保证数据整体的⼀致性，要⽤只读事务】 参考:

htp:/ w.codeceo.com/article/spring-transactions.html htp:/ w.cnblogs.com/fenglie/articles/4097759.html htps:/ w.zhihu.com/question/3907428/answer/ 8581202 htp:/blog.csdn.net/andyzhaojianhui/article/details/51984157

