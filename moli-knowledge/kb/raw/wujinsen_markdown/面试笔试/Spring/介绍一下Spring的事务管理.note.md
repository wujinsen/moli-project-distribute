# 介绍⼀下Spring的事务管理

事务就是对⼀系列的数据库操作（⽐如插⼊多条数据）进⾏统⼀的提交或回滚操作，如果插⼊成功， 那么⼀起成功，如果中间有⼀条出现异常，那么回滚之前的所有操作。 这样可以防⽌出现脏数据，防⽌数据库数据出现问题。 开发中为了避免这种情况⼀般都会进⾏事务管理。Spring中也有⾃⼰的事务管理机制，⼀般是使⽤ TransactionMananger进⾏管理，可以通过Spring的注⼊来完成此功能。 spring提供了⼏个关于事务处理的类： TransactionDefinition //事务属性定义 TranscationStatus //代表了当前的事务，可以提交，回滚。 PlatformTransactionManager 这个是spring提供的⽤于管理事务的基础接⼝，其下有⼀个实现的抽象类 AbstractPlatformTransactionManager,我们 使⽤的事务管理类例如DataSourceTransactionManager等 都是这个类的⼦类。 ⼀般事务定义步骤： TransactionDefinition td = new TransactionDefinition(); TransactionStatus ts = transactionManager.getTransaction(td); try { //do sth transactionManager.commit(ts); } catch(Exception e){transactionManager.rollback(ts);} spring提供的事务管理可以分为两类：编程式的和声明式的。编程式的，⽐较灵活，但是代码量⼤，存 在重复的代码⽐较多；声明式的⽐编程式的更灵活。 编程式主要使⽤transactionTemplate。省略了部分的提交，回滚，⼀系列的事务对象定义，需注⼊事 务管理对象. void add(){ transactionTemplate.execute( new TransactionCallback(){ pulic Object doInTransaction(TransactionStatus ts) { //do sth} } } 声明式： 使⽤TransactionProxyFactoryBean:

围绕Poxy的动态代理 能够⾃动的提交和回滚事务 org.springframework.transaction.interceptor.TransactionProxyFactoryBean PROPAGATION_REQUIRED–⽀持当前事务，如果当前没有事务，就新建⼀个事务。这是最常⻅的选 择。

PROPAGATION_SUPPORTS–⽀持当前事务，如果当前没有事务，就以⾮事务⽅式执⾏。 PROPAGATION_MANDATORY–⽀持当前事务，如果当前没有事务，就抛出异常。 PROPAGATION_REQUIRES_NEW–新建事务，如果当前存在事务，把当前事务挂起。 PROPAGATION_NOT_SUPPORTED–以⾮事务⽅式执⾏操作，如果当前存在事务，就把当前事务挂 起。 PROPAGATION_NEVER–以⾮事务⽅式执⾏，如果当前存在事务，则抛出异常。 PROPAGATION_NESTED–如果当前存在事务，则在嵌套事务内执⾏。如果当前没有事务，则进⾏与 PROPAGATION_REQUIRED类似的操作。

