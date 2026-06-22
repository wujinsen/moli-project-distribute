# @Transactional失效的⼏种场景

- 1.⼀个有@Transactional的⽅法被没有@Transactional⽅法调⽤时，会导致Transactional作⽤失 效。也是最容易出现的情况。

那为啥会出现这种情况？其实这还是由于使⽤Spring AOP代理造成的，因为只有当事务⽅法被当前 类以外的代码调⽤时，才会由Spring⽣成的代理对象来管理。

- 2.对⾮public⽅法进⾏事务注解。@Transactional 将会失效。 原因：是应为在Spring AOP代理时，事务拦截器在⽬标⽅法前后进⾏拦截，

DynamicAdvisedInterceptor的intercept ⽅法会获取Transactional注解的事务配置信息， 因为在Spring AOP 代理时，如上图所示 TransactionInterceptor （事务拦截器）在⽬标⽅法执⾏前 后进⾏拦截，DynamicAdvisedInterceptor（CglibAopProxy 的内部类）的 intercept ⽅法 或 JdkDynamicAopProxy的 invoke ⽅法会间接调⽤ AbstractFallbackTransactionAttributeSource 的 computeTransactionAttribute ⽅法会间接调⽤ AbstractFallbackTransactionAttributeSource 的 computeTransactionAttribute ⽅法，这个⽅法会获取Transactional 注解的事务配置信息。他会⾸ 先校验事务⽅法的修饰符是不是public，不是 public则不会获取@Transactional 的属性配置信息。

- 3.Transactional 事务配置属性中的propagation 属性配置的问题。 当propagation属性配置为：

TransactionDefinition.PROPAGATION_SUPPORTS：如果当前存在事务，则加⼊该事务；如果当前没有事 务，则以⾮事务的⽅式继续运⾏。

TransactionDefinition.PROPAGATION_NOT_SUPPORTED：以⾮事务⽅式运⾏，如果当前存在事务，则把 当前事务挂起。 TransactionDefinition.PROPAGATION_NEVER：以⾮事务⽅式运⾏，如果当前存 在事务，则抛出异常

- 4.还存在⼀种情况： 在⼀个类中A⽅法被事务注释，B⽅法也被事务注释。


@ Transactional

public void A(){ try{ this.B(); }catch(Exception e)

logger.error(); }

} 但在执⾏B⽅法是报错，但是异常被A catch 住，此时事务也会失效。

