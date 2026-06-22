Spring事务管理在编程中实现主要分为两种⽅式 ⼀、编程式事务管理

- *⼿动编写代码进⾏事务管理。（与业务代码融合在⼀起，很少使⽤） ⼆、声明式事务管理

- *基于TransactionProxyFactoryBean的⽅式。（配置和管理起来麻烦，需要每个事务管理的类配置 TransactionProxyFactoryBean）
- *基于AspectJ的XML⽅式。（经常使⽤）

- *基于注解的⽅式。（经常使⽤）


摘 要 : 主 要说 明 的 ⼏ 个 问题 1、 关 于事 务 管 理 。 2、 Spring如 何 提 供事 务 管 理 。 3、 Spring提 供 了 哪 些事 务 管 理 的 ⽅ 式 ⽅ 法 。 4、 Spring实 现 事 务 管 理 。 5、 注 意 事 项 及 常 ⻅ 问题 。

本⽂基于Spring4.1.7所写，主要说明以下⼏个问题：

- 1）、 关于事务管理。

- 2）、 Spring如何提供事务管理。

- 3）、 Spring提供了哪些事务管理的⽅式⽅法。

- 4）、 Spring实现事务管理。

- 5）、 注意事项及常⻅问题。


# 1、关于事务管理

- 1.1、为什么需要事务

为了使软件系统的数据库中的数据更可靠，更可信。

- 1.2、事务管理的分类


全局事务管理

这种事务管理⽅式通过Java Transaction API（JTA）实现，由容器管理，它可以管理多个资源（多数 据源、多应⽤服务），通常需要以JNDI的⽅式提供数据源。通常集群的分布式数据库服务通常会使⽤ 这样的⽅式。

本地事务管理

本地事务管理也称局部事务管理，按其实现⽅式的不同可以分为编程式事务管理和声明式事务管理。 编程式事务管理，它通过对资源的操作实现，⽐如联合JDBC连接实现事务管理，本地事务对于开发者 来说更容易实现，但是也有重⼤的缺点：不能对多资源实现事务管理，因为它与JDBC的连接不是由应 ⽤服务器来进⾏管理的；另外⼀个不⾜是本地事务管理是⼀种代码⼊侵的模式。 声明式事务管理是编程式事务管理的升级版，通过代理、切⾯等⽅式实现对业务代码的分离，声明式 事务管理可以在不同的开发环境中使⽤统⼀的编程模式，因此其复⽤性较好，对系统的影响较⼩，因 ⽽受到⼴泛的应⽤。

# 2、Spring如何提供事务管理

Spring提供了两种事务管理实现⽅式：编程式事务管理和声明式事务管理。

## 2.1、编程式事务管理

Spring中提供了⼀个核⼼的接⼝，PlatformTransactionManager接⼝：

public interface PlatformTransactionManager { TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionEx

ception; void commit(TransactionStatus status) throws TransactionException; void rollback(TransactionStatus status) throws TransactionException;

}

该接⼝通过TransactionDeﬁnition对象参数定义对事务的不同策略配置，通过TransactionStatus对象 参数与事务执⾏的线程关联来知晓⼀个事务的存在与否及其状态，并根据TransactionDeﬁnition 对象 参数的策略配置来决定是否⽣创建新的事务、事务传播等⼀系列操作。TransactionDeﬁnition的相关 属性如下：

事务隔离（Isolation）：当前事务和其他运⾏的事务是相互隔离的。 传播（Propagation）：可以向已经存在的事务中添加事务。 失效时间（Timeout）：指明⼀个事务从开始执⾏后多少时间失效，等待这个时间段后⽆法完成提

交的事务将⾃动回滚。

只读状态（Read-only status）：对只读去数据⽽不变更数据的操作可以将事务设置为只读，这样 将提⾼系统的性能。 Spring中提供了两种编程式事务实现⽅式：

使⽤TransactionTemplate模板； 直接使⽤⼀个PlatformTransactionManager的实现。

使⽤模板实现编程式事务管理的基本步骤如下：

- 1.
- 2.


在需要使⽤事务管理的地⽅声明⼀个TransactionTemplate对象，其构造的参数是⼀个 PlatformTransactionManager实例； 调⽤transactionTemplate对象的execute⽅法，该⽅法的参数为⼀个TransactionCallback实例， 它作为⼀个回调传递给模板，TransactionCallback只是⼀个接⼝，其实现即是所要执⾏的业务操 作。具体实现示例代码如下：

public Object someServiceMethod() { PlatformTransactionManager tm = new DataSourceTransactionManager(); TransactionTemplate tt = new TransactionTemplate(tm); //通过模板设置事务策略 tt.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED); tt.setTimeout(30); // 30 seconds return tt.execute(new TransactionCallback() {

// 下⾯的这部分代码是执⾏在事务的上下⽂中的 public Object doInTransaction(TransactionStatus status) { try{

updateOperation1(); //要执⾏的业务操作1 catch(MyException e){

status.setRollbackOnly(); //对关⼼的异常回滚事务

} return resultOfUpdateOperation2();//要执⾏的业务操作2 }

}); }

上⾯的示例中的回调是有返回值的，若不需要返回值可以使⽤TransactionCallbackWithoutResult作 为回调。 直接使⽤PlatformTransactionManager实现事务管理 直接使⽤PlatformTransactionManager实现事务管理与使⽤模板相似，其步骤相同，只不过事务的执 ⾏、提交、回滚等操作需要⾃⼰实现，实现的示例代码如下：

public Object someServiceMethod() { DefaultTransactionDefinition def = new DefaultTransactionDefinition(); def.setName("SomeTxName"); def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); TransactionStatus status = txManager.getTransaction(def); try {

// 执⾏业务操作

}catch (MyException ex) { txManager.rollback(status); throw ex;

} txManager.commit(status);

}

## 2.2、声明式事务管理

从上⾯对编程式事务管理的实现来看，其实现的事务管理⽐较精确，可以在同⼀个⽅法中实现不

同的事务管理，这对于有相关⽅⾯需求的系统来说，使⽤编程式事务管理是可⾏的。但编程式事务管 理也存在诸多弊端，如编码量⼤、侵⼊了业务代码、变更不灵活等，因此Spring提供了声明式的事务 管理。

Spring的声明式事务管理使⽤AOP的⽅式实现，通过动态代理，将对应的事务管理代码织⼊对应 的切点，从⽽实现声明式的事务管理，但其事务管理的实际执⾏与编程式事务管理是相同的，其执⾏ 的流程如下图所示：

![image 1](<Spring 事务管理探究.note_images/imageFile1.png>)

### Spring声明式事务管理实现的相关基本配置如下：

<?xml version="1.0" encoding="UTF-8"?> <beans xmlns="http://www.springframework.org/schema/beans"

xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:aop="http://www.springframework.org/schema/aop" xmlns:tx="http://www.springframework.org/schema/tx" xsi:schemaLocation="

http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd">

<!—配置Spring事务的传播特性 --> <tx:advice id="txAdvice" transaction-manager="txManager">

<tx:attributes>

<!—以get开头的⽅法只进⾏读操作，可将其设置为只读事务，可提升系统性能 --> <tx:method name="get*" read-only="true"/> <!—其他⽅法默认 为 进⾏读写操作 --> <tx:method name="*" rollback-for="Exception"/>

</tx:attributes> </tx:advice>

<!--将事务传播特性与切⾯关联，使动态代理在合适的切点织⼊相应事务管理代码 --> <aop:config proxy-target-class="true">

<aop:pointcut id="fooServiceOperation" expression="execution(* x.y.service.FooServi ce.*(..))"/>

<aop:advisor advice-ref="txAdvice" pointcut-ref="fooServiceOperation"/> </aop:config> <!-- 定义数据源 --> <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-

method="close"> <property name="driverClassName" value="oracle.jdbc.driver.OracleDriver"/> <property name="url" value="jdbc:oracle:thin:@rj-t42:1521:elvis"/> <property name="username" value="scott"/> <property name="password" value="tiger"/>

</bean> <!—定义PlatformTransactionManager --> <bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionMa

nager">

<property name="dataSource" ref="dataSource"/> </bean>

</beans>

注：Spring中的事务管理器都是PlatformTransactionManager接⼝的实现，如DataSourceTransactionManager、 HibernateTransactionManager、JMSTransactionManager等。

上述的配置是最基本的Spring声明式事务管理配置，其具体各项配置说明如下：

⾸先需引⼊aop、tx标签的命名空间及对应的XSD⽂件； 事务的管理实质上即是对数据源的控制，因此⾸先应该声明⼀个需要⽤于事务管理的数据源：

<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroymethod="close">

<property name="driverClassName" value="oracle.jdbc.driver.OracleDriver"/> <property name="url" value="jdbc:oracle:thin:@rj-t42:1521:elvis"/> <property name="username" value="scott"/> <property name="password" value="tiger"/>

</bean>

Spring提供⽤于事务管理的接⼝为PlatformTransactionManager接⼝，它的实现是依赖于数据源 （datasource）的，因此需要声明⼀个对应的事务管理器对象：

<bean id="txManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManage r">

<property name="dataSource" ref="dataSource"/> </bean>

对不同的数据源，使⽤的事务管理器是不同的，如Hibernate使⽤的是 HibernateTransactionManager，其依赖的是sessionFactory，配置如下：

<bean id="sessionFactory" class="org.springframework.orm.hibernate3.LocalSessionFactoryBean ">

<property name="dataSource" ref="dataSource" /> <property name="mappingResources">

<list>

<value>org/springframework/samples/petclinic/hibernate/petclinic.hbm.xml</value >

</list>

</property> <property name="hibernateProperties"> <value>hibernate.dialect=${hibernate.dialect}</value>

</property> </bean> <bean id="txManager" class="org.springframework.orm.hibernate3.HibernateTransactionManager" >

<property name="sessionFactory" ref="sessionFactory" /> </bean>

由于Spring的声明式事务管理采⽤AOP的⽅式实现，因此Spring的声明式事务管理必须定义事务切 ⼊点，从⽽使事务管理代码能够正确织⼊适当的点，以实现事务管理。事务的切⼊点包含两部分信 息：⼀个是切⼊的点（⽅法），⼀个是切⼊的点需要⼀个什么样的事务。这两部分的配置如下：

<!—配置Spring事务的传播特性 --> <tx:advice id="txAdvice" transaction-manager="txManager"> <tx:attributes>

<!—以get开头的⽅法只进⾏读操作，可将其设置为只读事务，可提升系统性能 --> <tx:method name="get*" read-only="true"/> <!—其他⽅法默认 为 进⾏读写操作 --> <tx:method name="*" rollback-for="Exception"/>

</tx:attributes> </tx:advice> <aop:config proxy-target-class="true">

<aop:pointcut id="fooServiceOperation" expression="execution(* x.y.service.FooServi ce.*(..))"/>

<aop:advisor advice-ref="txAdvice" pointcut-ref="fooServiceOperation"/> </aop:config>

advice标签定义事务的传播特性，它表示要将transaction-manager指定的TransactionManager的事 务管理代码织⼊指定点，其具体配置信息由attributes属性标签设置，attributes标签的详细信息如 下：

<table>
  <tr>
    <th>属性</th>
    <th>是否必须</th>
    <th>默认</th>
    <th>描述</th>
  </tr>
  <tr>
    <td>name</td>
    <td>Yes</td>
    <td> </td>
    <td>指切⼊点对应的⽅法的名 称，可以使⽤通配符表示。</td>
  </tr>
  <tr>
    <td>propagation</td>
    <td>No</td>
    <td>REQUIRED</td>
    <td>定义事务的传播⾏为： PROPAGATION_REQUIRE D-⽀持当前事务，如果当 前没有事务，就新建⼀个事 务。这是最常⻅的选择。 PROPAGATION_SUPOR TS-⽀持当前事务，如果 当前没有事务，就以⾮事务 ⽅式执⾏。 PROPAGATION_MANDAT ORY-⽀持当前事务，如 果当前没有事务，就抛出异 常。 PROPAGATION_REQUIRE S_NEW-新建事务，如果 当前存在事务，把当前事务 挂起。 PROPAGATION_NOT_SUP PORTED-以⾮事务⽅式 执⾏操作，如果当前存在事 务，就把当前事务挂起。 PROPAGATION_NEVER以⾮事务⽅式执⾏，如果当 前存在事务，则抛出异常。 PROPAGATION_NESTED<br><br>-如果当前存在事务，则在 嵌套事务内执⾏。如果当前 没有事务，则进⾏与 PROPAGATION_REQUIRE<br><br>类似的操作。</td>
  </tr>
</table>


#### D

定义事务隔离级别： ISOLATION_DEFAULT – Spring的默认事务隔离级

别，它使⽤的是数据库的事 务隔离级别，与使⽤的数据 库有关。 ISOLATION_READ_UNCO MI TED –该隔离级别允 许所有事务读取当前事务未 提交（或回滚）的事务，该 隔离级别会导致“脏读”、 “不可重复读”、“幻读”，在 实际中很少使⽤。 ISOLATION_READ_COM I TED –可以阻⽌ “脏读”， 但可能会出现“不可重复 读”、“幻读”。 ISOLATION_REPEATABLE _READ –可以阻⽌“脏读”、 “不可重复读”，但可能会出 现“幻读”。 ISOLATION_SERIALIZABL

- E –该级别的事务是顺序执 ⾏的，上述的三种现象都能 阻⽌，但消耗系统性能最 ⼤。 名词解释： 脏读：⼀个事务读取了另⼀ 个未提交的事务修改的数 据，这时前⼀个事务读取到 的数据可能是不正确的数 据。 不可重复读：指在⼀个事务 中多次读取⼀条记录，在这 个事务第⼀次读取和第⼆次 读取之间另⼀个事务也访问 了该条记录并修改，则前⼀ 个事务两次读取的数据不⼀ 致。 幻读：幻读与不可重复读有 些类似，在同⼀个事务中以 同⼀条件操作某⼀数据集两 次（或多次），在其进⾏两 个操作之间，另⼀个事务操 作了另⼀个数据集，⽽这个 数据集被操作后包含于前⼀ 个事务操作的数据集，因此 引起了第⼀个事务中的两次 操作的数据集是不⼀致的， 因⽽出现了前⼀次操作不正 确的幻觉。


isolation No DEFAULT

<table>
  <tr>
    <th>timeout</th>
    <th>No</th>
    <th>-1</th>
    <th>指定⼀个事务的失效时间， 在失效时限内如果⼀个事务 未能执⾏完成，则被⾃动回 滚，单位为秒。默认与数据 库⼀致。</th>
  </tr>
  <tr>
    <td>read-only</td>
    <td>No</td>
    <td>false</td>
    <td>定义⼀个事务为只读事务， 可提⾼系统性能。</td>
  </tr>
  <tr>
    <td>rolback-for</td>
    <td>No</td>
    <td> </td>
    <td>对什么样的异常（及其⼦异 常）进⾏事务回滚。</td>
  </tr>
  <tr>
    <td>no-rolback-for</td>
    <td>No</td>
    <td> </td>
    <td>对什么样的异常（及其⼦异 常）不进⾏回滚，rolbackfor 和no-rolback-for可同 时使⽤。</td>
  </tr>
</table>


注：

- 1) 使⽤XML based conﬁguration的⽅式实现的声明式事务管理依然可以在具体的代码中通过 TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()回滚事务；

- 2) 在配置中可以针对不同的模块、不同的类配置不同的切点和事务传播的特性。


## 2.3、通过注解驱动实现声明式事务管理

Spring同时还提供了通过注解实现事务管理的实现⽅式，这种⽅式的实现与XML based conﬁguration 的⽅式类同，即是在对应的实现类或⽅法中（或两者⼀起）添加事务属性的设置即可。 其具体实现⽅法如下：

⾸先在XML配置⽂件中添加AOP和TX标签的命名空间及对应的.XSD⽂件，然后启⽤事务管理注 解：<tx:annotation-driven transaction-manager="txManager"/>，同时需要指定⼀个事务管理 器，事务管理器的配置与XML-base⽅式的配置相同，如果事务管理器的Bean id为 transactionManager，则这⾥的transaction-manager可以省略，因为这是默认的名称。 在⽬标实现类和⽬标⽅法上添加事务注解，实例代码如下：

@Transactional(readOnly = true) public class SomeService implements ISomeService {

@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW) public Object someMethod(String param) { // do something

} @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW) public void someMethod 2(Object obj) {

// do something }

}

# 3、注意事项

- 1、由于Spring AOP使⽤代理实现，ﬁnale修饰的类和static、ﬁnale修饰的将不能织⼊，因Spring AOP 是运⾏时植⼊，这将导致运⾏时异常。

- 2、若数据库存在不⽀持事务的数据库引擎，应当切换到⽀持事务的引擎，否则事务管理将起不到应有 的作⽤。如MySql数据库的MyISAM引擎不⽀持事务，应改为⽀持事务的引擎，如INNODB等。


### 3、使⽤Spring事务管理时，其动态代理模式默认为JDK动态代理，此时依赖注⼊都必须使⽤接⼝注⼊ ⽅式，不能使⽤类注⼊；使⽤CGLib动态代理模式可以使⽤类注⼊，使⽤CGLib代理的配置为： <aop:conﬁg proxy-target-class="true">，其默认值为false。

