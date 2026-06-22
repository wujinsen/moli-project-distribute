htps:/ w.cnblogs.com/xuwc/p/1387361.html

参考： htps:/blog.csdn.net/SnailMan/article/details/94724197 htps:/blog.csdn.net/DILIGENT203/article/details/1075175 htps:/blog.csdn.net/whoamiyang/article/details/51901 8 htps:/techlog.cn/article/list/10183403

# 正确的理解MySQL的MVC及实现原理

！⾸先声明，MySQL的测试环境是5.7

前提概要

什么是MVC

什么是当前读和快照读？

当前读，快照读和MVC的关系

MVC实现原理

隐式字段

undo⽇志

Read View(读视图)

整体流程

MVC相关问题

R是如何在RC级的基础上解决不可重复读的？

RC, R级别下的I noDB快照读有什么不同？

前提概要

什么是MVC?

MVC MVCC，全称Multi-Version Concurrency Control，即多版本并发控制。MVC是⼀种并发控制的⽅法，⼀般在数据库管 理系统中，实现对数据库的并发访问，在编程语⾔中实现事务内存。

mvc - @百度百科

多版本控制: 指的是⼀种提⾼并发的技术。最早的数据库系统，只有读读之间可以并发，读写，写读，写写都要阻塞。引⼊多 版本之后，只有写写之间相互阻塞，其他三种操作都可以并⾏，这样⼤幅度提⾼了I noDB的并发度。在内部实现中，与 Postgres在数据⾏上实现多版本不同，I noDB是在undolog中实现的，通过undolog可以找回数据的历史版本。找回的数据历 史版本可以提供给⽤户读(按照隔离级别的定义，有些读请求只能看到⽐较⽼的数据版本)，也可以在回滚的时候覆盖数据⻚上 的数据。在I noDB内部中，会记录⼀个全局的活跃读写事务数组，其主要⽤来判断事务的可⻅性。 MVC是⼀种多版本并发控制机制。 MVC在MySQL I noDB中的实现主要是为了提⾼数据库并发性能，⽤更好的⽅式去处理读-写冲突，做到即使有读写冲突 时，也能做到不加锁，⾮阻塞并发读

什么是当前读和快照读？

在学习MVC多版本并发控制之前，我们必须先了解⼀下，什么是MySQL I noDB下的当前读和快照读?

当前读

像select lock in share mode(共享锁), select for update ; update, insert ,delete(排他锁)这些操作都是⼀种当前读，为什么叫当前 读？就是它读取的是记录的最新版本，读取时还要保证其他并发事务不能修改当前记录，会对读取的记录进⾏加锁

快照读

像不加锁的select操作就是快照读，即不加锁的⾮阻塞读；快照读的前提是隔离级别不是串⾏级别，串⾏级别下的快照读会退化成当前 读；之所以出现快照读的情况，是基于提⾼并发性能的考虑，快照读的实现是基于多版本并发控制，即MVC,可以认为MVC是⾏锁的 ⼀个变种，但它在很多情况下，避免了加锁操作，降低了开销；既然是基于多版本，即快照读可能读到的并不⼀定是数据的最新版本， ⽽有可能是之前的历史版本 说⽩了MVC就是为了实现读-写冲突不加锁，⽽这个读指的就是快照读, ⽽⾮当前读，当前读实际上是⼀种加锁的操作，是悲 观锁的实现

当前读，快照读和MVC的关系

准确的说，MVC多版本并发控制指的是 “维持⼀个数据的多个版本，使得读写操作没有冲突” 这么⼀个概念。仅仅是⼀个理想概念

⽽在MySQL中，实现这么⼀个MVC理想概念，我们就需要MySQL提供具体的功能去实现它，⽽快照读就是MySQL为我们实现 MVC理想模型的其中⼀个具体⾮阻塞读功能。⽽相对⽽⾔，当前读就是悲观锁的具体功能实现

要说的再细致⼀些，快照读本身也是⼀个抽象概念，再深⼊研究。MVC模型在MySQL中的具体实现则是由 3个隐式字段，undo⽇ 志 ，Read View 等去完成的，具体可以看下⾯的MVC实现原理

MVC能解决什么问题，好处是？

数据库并发场景有三种，分别为：

读-读：不存在任何问题，也不需要并发控制 读-写：有线程安全问题，可能会造成事务隔离性问题，可能遇到脏读，幻读，不可重复读 写-写：有线程安全问题，可能会存在更新丢失问题，⽐如第⼀类更新丢失，第⼆类更新丢失

备注：第1类丢失更新：事务A撤销时，把已经提交的事务B的更新数据覆盖了；第2类丢失更新：事务A覆盖事务B已经提交的 数据，造成事务B所做的操作丢失

MVC带来的好处是？ 多版本并发控制（MVC）是⼀种⽤来解决读-写冲突的⽆锁并发控制，也就是为事务分配单向增⻓的时间戳，为每个修改保存 ⼀个版本，版本与事务时间戳关联，读操作只读该事务开始前的数据库的快照。 所以MVC可以为数据库解决以下问题

在并发读写数据库时，可以做到在读操作时不⽤阻塞写操作，写操作也不⽤阻塞读操作，提⾼了数据库并发读写的性能

同时还可以解决脏读，幻读，不可重复读等事务隔离问题，但不能解决更新丢失问题

⼩结⼀下咯 总之，MVC就是因为⼤⽜们，不满意只让数据库采⽤悲观锁这样性能不佳的形式去解决读-写冲突问题，⽽提出的解决⽅ 案，所以在数据库中，因为有了MVC，所以我们可以形成两个组合：

MVCC + 悲观锁

MVC解决读写冲突，悲观锁解决写写冲突

MVCC + 乐观锁

MVC解决读写冲突，乐观锁解决写写冲突

这种组合的⽅式就可以最⼤程度的提⾼数据库并发性能，并解决读写冲突，和写写冲突导致的问题

MVCC的实现原理

MVC的⽬的就是多版本并发控制，在数据库中的实现，就是为了解决读写冲突，它的实现原理主要是依赖记录中的 3个隐式字 段，undo⽇志 ，Read View 来实现的。所以我们先来看看这个三个point的概念

隐式字段

每⾏记录除了我们⾃定义的字段外，还有数据库隐式定义的DB_TRX_ID,DB_ROLL_PTR,DB_ROW_ID等字段

DB_TRX_ID

- 6byte，最近修改(修改/插⼊)事务ID：记录创建这条记录/最后⼀次修改该记录的事务ID

- 7byte，回滚指针，指向这条记录的上⼀个版本（存储于rolback segment⾥）


DB_ROLL_PTR

DB_ROW_ID

6byte，隐含的⾃增ID（隐藏主键），如果数据表没有主键，I noDB会⾃动以DB_ROW_ID产⽣⼀个聚簇索引

实际还有⼀个删除flag隐藏字段, 既记录被更新或删除并不代表真的删除，⽽是删除flag变了

![image 1](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile1.png>)

如上图，DB_ROW_ID是数据库默认为该⾏记录⽣成的唯⼀隐式主键，DB_TRX_ID是当前操作该记录的事务ID,⽽DB_ROLL_PTR 是⼀个回滚指针，⽤于配合undo⽇志，指向上⼀个旧版本

undo⽇志

undo log主要分为两种：

insert undo log

代表事务在insert新记录时产⽣的undo log,只在事务回滚时需要，并且在事务提交后可以被⽴即丢弃

update undo log

事务在进⾏update或delete时产⽣的undo log;不仅在事务回滚时需要，在快照读时也需要；所以不能随便删除，只有在快速读或事 务回滚不涉及该⽇志时，对应的⽇志才会被purge线程统⼀清除

purge

从前⾯的分析可以看出，为了实现I noDB的MVC机制，更新或者删除操作都只是设置⼀下⽼记录的deleted_bit，并不真正将过时 的记录删除。

为了节省磁盘空间，I noDB有专⻔的purge线程来清理deleted_bit为true的记录。为了不影响MVC的正常⼯作，purge线程⾃⼰也 维护了⼀个read view（这个read view相当于系统中最⽼活跃事务的read view）;如果某个记录的deleted_bit为true，并且 DB_TRX_ID相对于purge线程的read view可⻅，那么这条记录⼀定是可以被安全清除的。

对MVC有帮助的实质是update undo log ，undo log实际上就是存在rollback segment中旧记录链，它的执⾏流程如 下： ⼀、 ⽐如⼀个有个事务插⼊persion表插⼊了⼀条新记录，记录如下，name为Jery,age为24岁，隐式主键是1，事务ID和回滚 指针，我们假设为NUL

![image 2](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile2.png>)

⼆、 现在来了⼀个事务1对该记录的name做出了修改，改为Tom

- 在事务1修改该⾏(记录)数据时，数据库会先对该⾏加排他锁

然后把该⾏数据拷⻉到undo log中，作为旧记录，既在undo log中有当前⾏的拷⻉副本

拷⻉完毕后，修改该⾏name为Tom，并且修改隐藏字段的事务ID为当前事务1的ID, 我们默认从1开始，之后递增，回滚指针指向拷⻉ 到undo log的副本记录，既表示我的上⼀个版本就是它

事务提交后，释放锁

![image 3](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile3.png>)

- 在事务2修改该⾏数据时，数据库也先为该⾏加锁


三、 ⼜来了个事务2修改person表的同⼀个记录，将age修改为30岁

然后把该⾏数据拷⻉到undo log中，作为旧记录，发现该⾏记录已经有undo log了，那么最新的旧数据作为链表的表头，插在该 ⾏记录的undo log最前⾯

修改该⾏age为30岁，并且修改隐藏字段的事务ID为当前事务2的ID, 那就是2，回滚指针指向刚刚拷⻉到undo log的副本记录

事务提交，释放锁

![image 4](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile4.png>)

从上⾯，我们就可以看出，不同事务或者相同事务的对同⼀记录的修改，会导致该记录的undo log成为⼀条记录版本线性 表，既链表，undo log的链⾸就是最新的旧记录，链尾就是最早的旧记录（当然就像之前说的该undo log的节点可能是会 purge线程清除掉，向图中的第⼀条insert undo log，其实在事务提交之后可能就被删除丢失了，不过这⾥为了演示，所以还 放在这⾥）

Read View(读视图)

什么是Read View? 什么是Read View，说⽩了Read View就是事务进⾏快照读操作的时候⽣产的读视图(Read View)，在该事务执⾏的快照读的那 ⼀刻，会⽣成数据库系统当前的⼀个快照，记录并维护系统当前活跃事务的ID(当每个事务开启时，都会被分配⼀个ID, 这个ID 是递增的，所以最新的事务，ID值越⼤) 所以我们知道 Read View主要是⽤来做可⻅性判断的, 即当我们某个事务执⾏快照读的时候，对该记录创建⼀个Read View读 视图，把它⽐作条件⽤来判断当前事务能够看到哪个版本的数据，既可能是当前最新的数据，也有可能是该⾏记录的undo log⾥⾯的某个版本的数据。 Read View遵循⼀个可⻅性算法，主要是将要被修改的数据的最新记录中的DB_TRX_ID（即当前事务ID）取出来，与系统当前 其他活跃事务的ID去对⽐（由Read View维护），如果DB_TRX_ID跟Read View的属性做了某些⽐较，不符合可⻅性，那就通 过DB_ROLL_PTR回滚指针去取出Undo Log中的DB_TRX_ID再⽐较，即遍历链表的DB_TRX_ID（从链⾸到链尾，即从最近的⼀ 次修改查起），直到找到满⾜特定条件的DB_TRX_ID, 那么这个DB_TRX_ID所在的旧记录就是当前事务能看⻅的最新⽼版本 那么这个判断条件是什么呢？

![image 5](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile5.png>)

我们这⾥盗窃 ⼀张源码图，如上，它是⼀段MySQL判断可⻅性的⼀段源码，即changes_visible⽅法（不 完全哈，但能看出⼤致逻辑），该⽅法展示了我们拿DB_TRX_ID去跟Read View某些属性进⾏怎么样的⽐较 在展示之前，我先简化⼀下Read View，我们可以把Read View简单的理解成有三个全局属性

@呵呵⼀笑百媚⽣

trx_list（名字我随便取的）

⼀个数值列表，⽤来维护Read View⽣成时刻系统正活跃的事务ID

up_limit_id

记录trx_list列表中事务ID最⼩的ID

low_limit_id

ReadView⽣成时刻系统尚未分配的下⼀个事务ID，也就是⽬前已出现过的事务ID的最⼤值+1

⾸先⽐较DB_TRX_ID < up_limit_id, 如果⼩于，则当前事务能看到DB_TRX_ID 所在的记录，如果⼤于等于进⼊下⼀个判断

接下来判断 DB_TRX_ID ⼤于等于 low_limit_id , 如果⼤于等于则代表DB_TRX_ID 所在的记录在Read View⽣成后才出现的， 那对当前事务肯定不可⻅，如果⼩于则进⼊下⼀个判断

判断DB_TRX_ID 是否在活跃事务之中，trx_list.contains(DB_TRX_ID)，如果在，则代表我Read View⽣成时刻，你这个事 务还在活跃，还没有Co mit，你修改的数据，我当前事务也是看不⻅的；如果不在，则说明，你这个事务在Read View⽣成之前就 已经Co mit了，你修改的结果，我当前事务是能看⻅的

整体流程

我们在了解了隐式字段，undo log， 以及Read View的概念之后，就可以来看看MVC实现的整体流程是怎么样了 整体的流程是怎么样的呢？我们可以模拟⼀下

当事务2对某⾏数据执⾏了快照读，数据库为该⾏数据⽣成⼀个Read View读视图，假设当前事务ID为2，此时还有事务1和事务3在 活跃中，事务4在事务2快照读前⼀刻提交更新了，所以Read View记录了系统当前活跃事务1，3的ID，维护在⼀个列表上，假设我们 称为trx_list

<table>
  <tr>
    <th>事务1</th>
    <th>事务2</th>
    <th>事务3</th>
    <th>事务4</th>
  </tr>
  <tr>
    <td>事务开始</td>
    <td>事务开始</td>
    <td>事务开始</td>
    <td>事务开始</td>
  </tr>
  <tr>
    <td>…</td>
    <td>…</td>
    <td>…</td>
    <td>修改且已提交</td>
  </tr>
  <tr>
    <td>进⾏中</td>
    <td>快照读</td>
    <td>进⾏中</td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


… … …

Read View不仅仅会通过⼀个列表trx_list来维护事务2执⾏快照读那刻系统正活跃的事务ID，还会有两个属性up_limit_id（记 录trx_list列表中事务ID最⼩的ID），low_limit_id(记录trx_list列表中事务ID最⼤的ID，也有⼈说快照读那刻系统尚未分配的下⼀ 个事务ID也就是⽬前已出现过的事务ID的最⼤值+1，我更倾向于后者 ) ；所以在这⾥例⼦中 up_limit_id就是1，low_limit_id就是4 + 1 = 5，trx_list集合的值是1,3，Read View如下图

>资料传送⻔ | 呵呵⼀笑百媚⽣的回答

![image 6](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile6.png>)

我们的例⼦中，只有事务4修改过该⾏记录，并在事务2执⾏快照读前，就提交了事务，所以当前该⾏当前数据的undo log如下图所 示；我们的事务2在快照读该⾏记录的时候，就会拿该⾏记录的DB_TRX_ID去跟up_limit_id,low_limit_id和活跃事务ID列表 (trx_list)进⾏⽐较，判断当前事务2能看到该记录的版本是哪个。

![image 7](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile7.png>)

所以先拿该记录DB_TRX_ID字段记录的事务ID4去跟Read View的的up_limit_id⽐较，看4是否⼩于up_limit_id(1)，所以不 符合条件，继续判断 4 是否⼤于等于 low_limit_id(5)，也不符合条件，最后判断4是否处于trx_list中的活跃事务, 最后发现事 务ID为4的事务不在当前活跃事务列表中, 符合可⻅性条件，所以事务4修改后提交的最新结果对事务2快照读时是可⻅的，所以事务2 能读到的最新数据记录是事务4所提交的版本，⽽事务4提交的版本也是全局⻆度上最新的版本

![image 8](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile8.png>)

也正是Read View⽣成时机的不同，从⽽造成RC, R级别下快照读的结果的不同

MVCC相关问题

R是如何在RC级的基础上解决不可重复读的？

当前读和快照读在 R级别下的区别：

- 表1:


<table>
  <tr>
    <th>事务A</th>
    <th>事务B</th>
  </tr>
  <tr>
    <td>开启事务</td>
    <td>开启事务</td>
  </tr>
  <tr>
    <td>快照读(⽆影响)查询⾦额为50</td>
    <td>快照读查询⾦额为50</td>
  </tr>
  <tr>
    <td>更新⾦额为40</td>
    <td> </td>
  </tr>
  <tr>
    <td>提交事务</td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>select 快照读⾦额为50</td>
  </tr>
  <tr>
    <td> </td>
    <td>当前读</td>
  </tr>
</table>


select lock in share mode ⾦额为40

在上表的顺序下，事务B的在事务A提交修改后的快照读是旧版本数据，⽽当前读是实时新数据40

- 表2:


<table>
  <tr>
    <th>事务A</th>
    <th>事务B</th>
  </tr>
  <tr>
    <td>开启事务</td>
    <td>开启事务</td>
  </tr>
  <tr>
    <td>快照读（⽆影响）查询⾦额为50</td>
    <td> </td>
  </tr>
  <tr>
    <td>更新⾦额为40</td>
    <td> </td>
  </tr>
  <tr>
    <td>提交事务</td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>select 快照读⾦额为40</td>
  </tr>
  <tr>
    <td> </td>
    <td>当前读</td>
  </tr>
</table>


select lock in share mode ⾦额为40

⽽在表2这⾥的顺序中，事务B在事务A提交后的快照读和当前读都是实时的新数据40，这是为什么呢？

这⾥与上表的唯⼀区别仅仅是表1的事务B在事务A修改⾦额前快照读过⼀次⾦额数据，⽽表2的事务B在事务A修改⾦额前没有进⾏过 快照读。

所以我们知道事务中快照读的结果是⾮常依赖该事务⾸次出现快照读的地⽅，即某个事务中⾸次出现快照读的地⽅⾮常关键， 它有决定该事务后续快照读结果的能⼒ 我们这⾥测试的是更新，同时删除和更新也是⼀样的，如果事务B的快照读是在事务A操作之后进⾏的，事务B的快照读也是能 读取到最新的数据的

RC, R级别下的I noDB快照读有什么不同？

正是Read View⽣成时机的不同，从⽽造成RC, R级别下快照读的结果的不同

在 R级别下的某个事务的对某条记录的第⼀次快照读会创建⼀个快照及Read View, 将当前系统活跃的其他事务记录起来，此后在调 ⽤快照读的时候，还是使⽤的是同⼀个Read View，所以只要当前事务在其他事务提交更新之前使⽤过快照读，那么之后的快照读使 ⽤的都是同⼀个Read View，所以对之后的修改不可⻅； 即 R级别下，快照读⽣成Read View时，Read View会记录此时所有其他活动事务的快照，这些事务的修改对于当前事务都是不可⻅ 的。⽽早于Read View创建的事务所做的修改均是可⻅ ⽽在RC级别下的，事务中，每次快照读都会新⽣成⼀个快照和Read View, 这就是我们在RC级别下的事务中可以看到别的事务提交的 更新的原因

总之在RC隔离级别下，是每个快照读都会⽣成并获取最新的Read View；⽽在 R隔离级别下，则是同⼀个事务中的第⼀个快 照读才会创建Read View, 之后的快照读获取的都是同⼀个Read View。

### MySQL系列

【MySQL笔记】正确的理解MySQL的乐观锁与悲观锁,MVC

【MySQL笔记】正确的理解MySQL的MVC及实现原理

【MySQL笔记】正确的理解MySQL的事务和隔离级别

参考资料

I noDB多版本(MVC)实现简要分析 - @作者：何登成

MySQL I noDB MVC深度分析 - @作者：stevenczp

I noDB存储引擎MVC的⼯作原理 - @作者：秋⻛醉了

MySQL 在 RC 隔离级别下是如何实现读不阻塞的？ - @作者：知乎

MVC read view的问题 - @作者：PHP中⽂⽹

MySQL数据库事务各隔离级别加锁情况–read co mited & MVC - @作者：mark_fork

乐观锁与CAS，MVC - @作者：shuf1e

悲观锁，乐观锁以及MVC - @作者：wezheng

【数据库】悲观锁与乐观锁与MySQL的MVC实现简述 - @作者：Nick Huang

⼀⽂讲透 MVC实现原理

- 1. 引⾔

上⼀篇⽂章中，我们介绍了 mysql 的 crash safe 机制，也是 ACID 中原⼦性的实现 – redolog 的原理和配置⽅法。

本⽂，我们来介绍 mysql 在可重复读隔离级别下事务的实现⽅式 – MVC，以及他的实现原理 – undolog

- 2. undolog


mysql 异常情况下的事务安全 – 详解 mysql redolog

undo log 是 MVC 实现的⼀个重要依赖，所以在详细介绍 MVC 前，我们先来介绍 undo log 是什么。 undo log 与 redo log ⼀起构成了 MySQL 事务⽇志，并且我们上篇⽂章中提到的⽇志先⾏原则 WAL 除了包含 redo log 外， 也包括 undo log，事务中的每⼀次修改，i nodb 都会先记录对应的 undo log 记录。 那么 undo log 是什么呢？顾名思义，与 redo log ⽤于数据的灾后重新提交不同，undo log 主要⽤于数据修改的回滚。 与 redo log 记录的是物理⻚的修改不同，undo log 记录的是逻辑⽇志。

当 delete ⼀条记录时，undo log 中会记录⼀条对应的 insert 记录，反之亦然，当 update ⼀条记录时，它记录⼀条对应相反 的 update 记录，如果 update 的是主键，则是对先删除后插⼊的两个事件的反向逻辑操作的记录。

![image 9](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile9.png>)

这样，在事务回滚时，我们就可以从 undo log 中反向读取相应的内容，并进⾏回滚，同时，我们也可以根据 undo log 中记录 的⽇志读取到⼀条被修改后数据的原值。 正是依赖 undo log，i nodb 实现了 ACID 中的 C – Consistency 即⼀致性。

# 3. undolog的存储与相关配置

i nodb 通过段的⽅式来管理 undo log，每⼀条记录占⽤⼀个 undo log segment，每 1024 个 undo log segment 被组织为 ⼀个回滚段（rolback segment） mysql 5.6 版本以后可以通过 i nodb_undo_logs 配置项设置系统⽀持的最⼤回滚段个数，默认为 128。 通过 i nodb_undo_directory 配置可以设置 undo log 存储的⽬录。 通过 i nodb_undo_tablespaces 可以设置将 undo log 平均分配到多少个⽂件中，默认为 0，即全部写⼊同⼀个⽂件中。 这⾥顺便说⼀下，在 mysql 5.6 的早期版本及之前的版本中，并没有限制回滚段的⼤⼩，这就造成了⼀个⾮常严重的漏洞，攻 击者可以通过反复更新⼀个字段造成 undo log 占⽤⼤量的磁盘空间，可以参看： htps:/blog.jcole.us/2014/04/16/a-litle-fun-with-i nodb-multi-versioning/ htps:/bugs.mysql.com/bug.php?id=72362。

# 4. MVC

此前的⽂章中，我们介绍了 mysql 事务隔离级别，其中⾮常粗略的介绍了 MVC：

mysql 锁机制与四种隔离级别

MVC 全称是 multiversion concurency control，即多版本并发控制，是 i nodb 实现事务并发与回滚的重要功能。 具体的实现是，在数据库的每⼀⾏中，添加额外的三个字段：

- 1.
- 2.
- 3.


DB_TRX_ID – 记录插⼊或更新该⾏的最后⼀个事务的事务 ID DB_ROL_PTR – 指向改⾏对应的 undolog 的指针 DB_ROW_ID – 单调递增的⾏ ID，他就是 AUTO_INCREMENT 的主键 ID

![image 10](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile10.png>)

# 5. 快照读与当前读

i nodb 拥有⼀个⾃增的全局事务 ID，每当⼀个事务开启，在事务中都会记录当前事务的唯⼀ id，⽽全局事务 ID 会随着新事 务的创建⽽增⻓。

同时，新事务创建时，事务系统会将当前未提交的所有事务 ID 组成的数组传递给这个新事务，本⽂的下⾯段落我们成这个数 组为 TRX_ID 集合。

- 5.1. 快照读

正如我们前⾯介绍的，每当⼀个事务更新⼀条数据时，都会在写⼊对应 undo log 后将这⾏记录的隐藏字段 DB_TRX_ID 更新 为当前事务的事务 ID，⽤来表明最新更新该数据的事务是该事务。 当另⼀个事务去 select 数据时，读到该⾏数据的 DB_TRX_ID 不为空并且 DB_TRX_ID 与当前事务的事务 ID 是不同的，这就 说明这⼀⾏数据是另⼀个事务修改并提交的。 那么，这⾏数据究竟是在当前事务开启前提交的还是在当前事务开启后提交的呢？

如上图所示，有了上⽂提到的 TRX_ID 集合，就很容易判断这个问题了，如果这⼀⾏数据的 DB_TRX_ID 在 TRX_ID 集合中或 ⼤于当前事务的事务 ID，那么就说明这⾏数据是在当前事务开启后提交的，否则说明这⾏数据是在当前事务开启前提交的。 对于当前事务开启后提交的数据，当前事务需要通过隐藏的 DB_ROL_PTR 字段找到 undo log，然后进⾏逻辑上的回溯才能 拿到事务开启时的原数据。 这个通过 undo log + 数据⾏获取到事务开启时的原始数据的过程就是“快照读”。

- 5.2. 当前读


![image 11](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile11.png>)

很多时候，我们在读取数据库时，需要读取的是⾏的当前数据，⽽不需要通过 undo log 回溯到事务开启前的数据状态，主要 包含以下操作：

- 1.
- 2.
- 3.
- 4.


insert update select … lock in share mode select … for update

# 6. MVC与不可重复读、幻读的问题

## 6.1. 不可重复读与幻读

“不可重复读”与“幻读”是两个数据库常⻅的极易混淆的问题。 不可重复读指的是，在⼀个事务开启过程中，当前事务读取到了另⼀事务提交的修改。 幻读则指的是，在⼀个事务开启过程中，读取到另⼀个事务提交导致的数据条⽬的新增或删除。

## 6.2. 可重复读解决不可重复读与幻读问题的原理

那么，可重复读的隔离级别是否解决了不可重复读与幻读问题呢？ 上⾯我们提到，对于正常的 select 查询 i nodb 实际上进⾏的是快照读，即通过判断读取到的⾏ 的 DB_TRX_ID 与 DB_ROL_PTR 字段指向的 undo log 回溯到事务开启前或当前事务最后⼀次更新的数据版本，从⽽在这样 的场景下避免了可重复读与幻读的问题。 针对已存在的数据，insert 和 update 操作虽然是进⾏当前读，但 insert 与 update 操作后，该⾏的最新修改事务 ID 为当前 事务 ID，因此读到的值仍然是当前事务所修改的数据，不会产⽣不可重复读的问题。

但如果当前事务更新到了其他事务新插⼊并提交了的数据，这就会造成该⾏数据的 DB_TRX_ID 被更新为当前事务 ID，此后即 便进⾏快照读，依然会查出该⾏数据，产⽣幻读（其他事务插⼊或删除但未提交该⾏数据的情况下会锁定该⾏，造成当前事务 对该⾏的更新操作被阻塞，所以这种情况不会产⽣幻读问题，有关事务间的锁，不在本篇⽂章的讨论范围内，接下来的⽂章我 们会进⼀步讨论）

## 6.3. 实证

我们实际来看⼀个例⼦。 ⾸先，我们创建⼀个表：

CREATE TABLE `test` ( `id` int(10) unsigned NOT NULL AUTO_INCREMENT, `value` int(10) unsigned NOT NULL, PRIMARY KEY (`id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8

然后我们插⼊三条初始数据：

INSERT INTO `test` (`value`) VALUES (1), (2), (3)

接下来我们在两个窗⼝中分别开启⼀个事务并查询出现有数据：

![image 12](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile12.png>)

我们在其中⼀个事务中先更新 id 为 1 的数据，再插⼊⼀条 id 为 4 的数据，再删除 id 为 2 的数据，然后，在另⼀个事务中查 询，可以看到此时查询出来的仍然是事务开启时的初始数据，说明当前隔离级别和场景下并没有脏读的问题存在：

![image 13](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile13.png>)

此时，我们提交所有的修改，接着在另⼀个事务中查询，可以看到此时查询到的结果仍然是事务开启前的原始数据，说明当前 隔离级别和场景下并没有不可重复读和幻读的问题存在：

![image 14](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile14.png>)

那么接下来，我们在未提交的这个事务中执⾏⼀条修改，可以看到，本应在事务中只影响⼀⾏的 update 操作返回 了 changed: 2，接着，我们查询结果出现了 id 为 4 的⾏，说明了幻读问题的存在【update当前读会读最新数据】：

![image 15](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile15.png>)

# 7. undolog的清理

在回滚段中，每个 undo log 段都有⼀个类型字段，共有两种类型：insert undo logs 和 update undo logs。 对于执⾏ insert 语句插⼊的数据，其回滚段类型为 insert undo logs，⽤来在事务中回滚当前的插⼊操作。 对于执⾏ delete 语句删除和 update 语句更新的数据，其回滚段类型为 update undo logs。 如果事务 rolback，i nodb 通过执⾏ undo log 中的所有反向操作，实现事务中所有操作的回滚，随后就会删除该事务关联的 所有 undo log 段。 如果事务 comit，对于 insert undo logs，i nodb 会直接清除，但对于 update undo logs，只有当前没有任何事务存在 时，i nodb 的 purge 线程才会清理这些 undo log 段。

这⾥提到了 purge 线程，他是⼀个周期运⾏的垃圾收集线程，主要⽤来收集 undo log 段，以及已经被废弃的索引。 在事务提交时，i nodb 会将所有需要清理的任务添加到 purge 队列中，可以通过 i nodb_max_purge_lag 配置项设 定 purge 队列的⼤⼩。 purge 线程会在周期执⾏时，对 purge 队列中的任务进⾏清理，i nodb_max_purge_lag_delay 配置项说明了 purge 线程的 执⾏周期间隔。 所以，尽量缩短使⽤中每个事务的持续时间，可以让 purge 线程有更⼤概率回收已经没有存在必要的 undo log 段，从⽽尽量 释放磁盘空间的占⽤。

# 8. 《⾼性能 MySQL》中的谬误

主⻚君在多年以前曾经就 MVC 的实现阅读过相对⾮常权威的著作《⾼性能 MySQL》，其中有着下⾯的⼀段话：

![image 16](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile16.png>)

主⻚君看到⽹上⽬前许许多多的博客都是按照上述⽂字中介绍的原理来讲述的。 但当如今主⻚君仔细去深究其中的原理，参阅官⽅⽂档之后，发现各版本 i nodb MVC 的原理并不是书上所描述的这样，毕 竟官⽅⽂档是除源码外的第⼀⼿资料，同时，参阅⼀些⽂章贴出的源码来看，确实是按照官⽅⽂档中介绍的原理实现的，因 此，本⽂主要参阅官⽅的相关源码进⾏详细的总结和讲述。 那么，《⾼性能 MySQL》中的描述是来源于哪⾥呢？事实上，它讲述的是 PostgreSQL 的实现⽅式。 与 I noDB 类似，PostgreSQL 为每⼀⾏数据添加了 4 个额外的字段：

- 1.
- 2.
- 3.


xmin – 插⼊与更新数据时写⼊的事务 ID xmax – 删除数据时写⼊的事务 ID cmin – 插⼊与更新数据时写⼊的命令 ID

4.

cmax – 删除数据时写⼊的命令 ID

在每⼀个事务中，都维护了⼀个从 0 开始单调递增的命令 ID（COMAND_ID），每当⼀个命令执⾏后，COMAND_ID 都 会⾃增。 当⼀个事务更新⼀条数据，PostgreSQL 会创建⼀条新的记录，并将新的记录的 xmin 更新为当前事务的事务 ID。 当⼀个事务删除⼀条数据，PostgreSQL 不会创建⼀条新纪录，⽽是将该⾏记录的 xmax 更新为当前事务的 ID。 因为 cmin 和 cmax 的记录，PostgreSQL 可以以此排列出同⼀事务中所有更新、删除操作的先后。 这样，在⼀个事物读取数据时，只需要读取 xmin ⼩于当前事务 ID 且 xmin 不在 TRX_ID 集合中的数据即可实现快照读的功 能。

8.1. 优缺点

PostgreSQL 的 MVC 实现与 i nodb 的 MVC 实现相⽐，最⼤的优点在于其查询⽆需解析 undo log 进⾏回溯。 对于数据回滚，只需要删除所有 xmin 为当前事务 ID 的记录，清除所有 xmax 为当前事务 ID 的 xmax 字段即可。 但其缺点也很明显，那就是随着更新操作，数据库中会产⽣⼤量的额外数据，这些数据同时也对数据库其他的操作例如索引的 建⽴等都带来了额外的性能消耗。

![image 17](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile17.png>)

# 轻松理解MYSQLMVC实现机制

### 1. MVCC简介

- 1.1 什么是MVC MVC是⼀种多版本并发控制机制。
- 1.2 MVC是为了解决什么问题?
- 1.3 MVC实现


⼤多数的MYSQL事务型存储引擎,如,I noDB，Falcon以及PBXT都不使⽤⼀种简单的⾏锁机制.事实上,他们都和MVC–多版本并发控 制来⼀起使⽤.

⼤家都应该知道,锁机制可以控制并发操作,但是其系统开销较⼤,⽽MVC可以在⼤多数情况下代替⾏级锁,使⽤MVC,能降低其系统 开销.

MVC是通过保存数据在某个时间点的快照来实现的. 不同存储引擎的MVC. 不同存储引擎的MVC实现是不同的,典型的有乐 观并发控制和悲观并发控制.

- 2.MVCC 具体实现分析 下⾯,我们通过I noDB的MVC实现来分析MVC使怎样进⾏并发控制的. I noDB的MVC,是通过在每⾏记录后⾯保存两个隐藏的列来实现的,这两个列，分别保存了这个⾏的创建时间，⼀个保存的是 ⾏的删除时间。这⾥存储的并不是实际的时间值,⽽是系统版本号(可以理解为事务的ID)，没开始⼀个新的事务，系统版本号就 会⾃动递增，事务开始时刻的系统版本号会作为事务的ID.下⾯看⼀下在REPEATABLE READ隔离级别下,MVC具体是如何操 作的. 2.1简单的⼩例⼦ create table yang( id int primary key auto_increment, name varchar(20); 假设系统的版本号从1开始. INSERT I noDB为新插⼊的每⼀⾏保存当前系统版本号作为版本号. 第⼀个事务ID为1； start transaction; insert into yang values(NULL,'yang') ; insert into yang values(NULL,'long'); insert into yang values(NULL,'fei'); commit;


对应在数据中的表如下(后⾯两列是隐藏列,我们通过查询语句并看不到)

<table>
  <tr>
    <th>id</th>
    <th>name</th>
    <th>创建时间(事务ID)</th>
    <th>删除时间(事务ID)</th>
  </tr>
  <tr>
    <td>1</td>
    <td>yang</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td>2</td>
    <td>long</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


- 3 fei 1 undefined


SELECT

I noDB会根据以下两个条件检查每⾏记录:

- a.I noDB只会查找版本早于当前事务版本的数据⾏(也就是,⾏的系统版本号⼩于或等于事务的系统版本号)，这样可以确保事 务读取的⾏，要么是在事务开始前已经存在的，要么是事务⾃身插⼊或者修改过的.
- b.⾏的删除版本要么未定义,要么⼤于当前事务版本号,这可以确保事务读取到的⾏，在事务开始之前未被删除. 只有a,b同时满⾜的记录，才能返回作为查询结果. DELETE I noDB会为删除的每⼀⾏保存当前系统的版本号(事务的ID)作为删除标识. 看下⾯的具体例⼦分析:


第⼆个事务,start transactionID为2; ;

- select * from yang; //(1)

- select * from yang; //(2) commit;


- 假设1 假设在执⾏这个事务ID为2的过程中,刚执⾏到(1),这时,有另⼀个事务ID为3往这个表⾥插⼊了⼀条数据; 第三个事务ID为3; start transaction; insert into yang values(NULL,'tian'); commit;

这时表中的数据如下:

然后接着执⾏事务2中的(2),由于id=4的数据的创建时间(事务ID为3),执⾏当前事务的ID为2,⽽I noDB只会查找事务ID⼩于等于 当前事务ID的数据⾏,所以id=4的数据⾏并不会在执⾏事务2中的(2)被检索出来,在事务2中的两条select 语句检索出来的数据 都只会下表:

- 假设2 假设在执⾏这个事务ID为2的过程中,刚执⾏到(1),假设事务执⾏完事务3后，接着⼜执⾏了事务4;

第四个事务:start transaction; delete from yang where id=1; commit;

此时数据库中的表如下:

<table>
  <tr>
    <th>id</th>
    <th>name</th>
    <th>创建时间(事务ID)</th>
    <th>删除时间(事务ID)</th>
  </tr>
  <tr>
    <td>1</td>
    <td>yang</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td>2</td>
    <td>long</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td>3</td>
    <td>fei</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


4 tian 3 undefined

<table>
  <tr>
    <th>id</th>
    <th>name</th>
    <th>创建时间(事务ID)</th>
    <th>删除时间(事务ID)</th>
  </tr>
  <tr>
    <td>1</td>
    <td>yang</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td>2</td>
    <td>long</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


- 3 fei 1 undefined


<table>
  <tr>
    <th>id</th>
    <th>name</th>
    <th>创建时间(事务ID)</th>
    <th>删除时间(事务ID)</th>
  </tr>
  <tr>
    <td>1</td>
    <td>yang</td>
    <td>1</td>
    <td>4</td>
  </tr>
  <tr>
    <td>2</td>
    <td>long</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td>3</td>
    <td>fei</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


4 tian 3 undefined

接着执⾏事务ID为2的事务(2),根据SELECT 检索条件可以知道,它会检索创建时间(创建事务的ID)⼩于当前事务ID的⾏和删除时 间(删除事务的ID)⼤于当前事务的⾏,⽽id=4的⾏上⾯已经说过,⽽id=1的⾏由于删除时间(删除事务的ID)⼤于当前事务的ID,所 以事务2的(2)select * from yang也会把id=1的数据检索出来.所以,事务2中的两条select 语句检索出来的数据都如下:

<table>
  <tr>
    <th>id</th>
    <th>name</th>
    <th>创建时间(事务ID)</th>
    <th>删除时间(事务ID)</th>
  </tr>
  <tr>
    <td>1</td>
    <td>yang</td>
    <td>1</td>
    <td>4</td>
  </tr>
  <tr>
    <td>2</td>
    <td>long</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


3 fei 1 undefined

UPDATE

I noDB执⾏UPDATE，实际上是新插⼊了⼀⾏记录，并保存其创建时间为当前事务的ID，同时保存当前事务ID到要UPDATE的 ⾏的删除时间.

假设3

假设在执⾏完事务2的(1)后⼜执⾏,其它⽤户执⾏了事务3,4,这时，⼜有⼀个⽤户对这张表执⾏了UPDATE操作:

第5start个事务:transaction; update yang set name='Long' where id=2; commit;

根据update的更新原则:会⽣成新的⼀⾏,并在原来要修改的列的删除时间列上添加本事务ID,得到表如下:

<table>
  <tr>
    <th>id</th>
    <th>name</th>
    <th>创建时间(事务ID)</th>
    <th>删除时间(事务ID)</th>
  </tr>
  <tr>
    <td>1</td>
    <td>yang</td>
    <td>1</td>
    <td>4</td>
  </tr>
  <tr>
    <td>2</td>
    <td>long</td>
    <td>1</td>
    <td>5</td>
  </tr>
  <tr>
    <td>3</td>
    <td>fei</td>
    <td>1</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td>4</td>
    <td>tian</td>
    <td>3</td>
    <td>undefined</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


2 Long 5 undefined

继续执⾏事务2的(2),根据select 语句的检索条件,得到下表:

<table>
  <tr>
    <th>id</th>
    <th>name</th>
    <th>创建时间(事务ID)</th>
    <th>删除时间(事务ID)</th>
  </tr>
  <tr>
    <td>1</td>
    <td>yang</td>
    <td>1</td>
    <td>4</td>
  </tr>
  <tr>
    <td>2</td>
    <td>long</td>
    <td>1</td>
    <td>5</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


3 fei 1 undefined

还是和事务2中(1)select 得到相同的结果.

# redolog 引⾔

上⼀篇⽂章中，我们介绍了 mysql 的⼆进制⽇志 binlog，他为数据的同步、恢复和回滚提供了⾮常便利的⽀持

怎么避免从删库到跑路 - 详解 mysql binlog 的配置与使⽤

⽆论我们使⽤的是什么存储引擎，只要通过配置开启，mysql 都会记录 binlog 在⼯程存储项⽬中，有⼀个重要的概念，那就是 crash safe，即当服务器突然断电或宕机，需要保证已提交的数据或修改不会 丢失，未提交的数据能够⾃动回滚，这就是 mysql ACID 特性中的⼀个⼗分重要的特性 - Atomicity 原⼦性 根据我们上⼀篇⽂章中的讲解，依靠 binlog 是⽆法保证 crash safe 的，因为 binlog 是事务提交时写⼊的，如果在 binlog 缓 存中的数据持久化到硬盘之前宕机或断电 在服务器恢复⼯作后，由于 binlog 缺失⼀部分已提交的操作数据，⽽主数据库中实际上这部分操作已经存在，从数据库因此 ⽆法同步这部分操作，从⽽造成主从数据库数据不⼀致，这是很严重的 但实际上，i nodb 存储引擎是拥有 crash safe 能⼒的，那么他是⽤什么机制来实现呢？本⽂我们就来详细说明

# mysql 的执⾏过程

⽆论使⽤任何存储引擎，只要开启相应配置，mysql 都会记录 binlog 但 MyISAM 引擎并没有提供 crash safe 能⼒，⽽ I noDB 则提供了灾后恢复能⼒，这是为什么呢？ 这和 mysql 整体的分层有关，我们需要⾸先了解⼀下⼀条 sql 语句是如何执⾏的

![image 18](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile18.png>)

mysql 主要分为两层，与客户端直接交互的是 server 层，包括连接的简历和管理、词法分析、语法分析、执⾏计划与具 体 sql 的选择都是在 server 层中进⾏的，binlog 就是在 server 层中由 mysql server 实现的 ⽽ i nodb 作为具体的⼀个存储引擎，他通过 redolog 实现了 crash safe 的⽀持

# redolog的写⼊

mysql 有⼀个基本的技术理念，那就是 WAL，即 Write-Ahead Loging，先写⽇志，再写磁盘，从⽽保证每⼀次操作都有据 可查，这⾥所说的“先写⽇志”中的⽇志就包括 i nodb 的 redolog redolog 与持续向后添加的 binlog 不同，他只占⽤预先分配的⼀块固定⼤⼩的磁盘空间，在这⽚空间中，redolog 采⽤循环写 ⼊的⽅式写⼊新的数据

![image 19](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile19.png>)

同时，binlog 是以每条操作语句为单位进⾏记录的，⽽ redolog 则是以数据⻚来进⾏记录的，他记录了每个⻚上的修改，所 以⼀个事务中可能分多次多条写⼊ redolog

# crashsafe与两阶段提交

每条 redolog 都有两个状态 - prepare 与 comit 状态 例如对于⼀张 mysql 表 （CREATE TABLE `A` (`ID` int(10) unsigned NOT NUL AUTO_INCREMENT, `C` int(10) NOT NUL DEFAULT 0, PRIMA RY KEY (`ID`) ENGINE=I noDB），我们执⾏⼀条 SQL 语句： UPDATE A set C=C+1 WHERE ID=2

实际上，mysql 数据库会进⾏以下操作（下图中深⾊的是 mysql server 层所做的操作，浅⾊部分则是 i nodb 存储引擎进⾏ 的操作）：：

![image 20](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile20.png>)

可以看到，在写⼊ binlog 及事务提交前，i nodb 先记录了 redolog，并标记为 prepare 状态，在事务提交后，i nodb 会 将 redolog 更新为 comit 状态，这样在异常发⽣时，就可以按照下⾯两条策略来处理：

a.

当异常情况发⽣时，如果第⼀次写⼊ redolog 成功，写⼊ binlog 失败，MySQL 会当做事务失败直接回滚，保证了 后续 redolog 和 binlog 的准确性 如果第⼀次写⼊ redolog 成功，binlog 也写⼊成功，当第⼆次写⼊ redolog 时候失败了，那数据恢复的过程中， MySQL 判断 redolog 状态为 prepare，且存在对应的 binlog 记录，则会重放事务提交，数据库中会进⾏相应的修改 操作

a.

整个过程是⼀个典型的两阶段提交过程，由 binlog 充当了协调者的⻆⾊，针对每⼀次⽇志写⼊，i nodb 都会随之记录⼀ 个 8 字节序列号 - LSN（⽇志逻辑序列号 log sequence number），他会随着⽇志写⼊不断单调递增 binlog、DB 中的数据、redolog 三者就是通过 LSN 关联到⼀起的，因为数据⻚上记录了 LSN、⽇志开始与结束均记录 了 LSN、刷盘节点 checkpoint 也记录了 LSN，因此 LSN 成为了整套系统中的全局版本信息 当异常发⽣并重新启动后，i nodb 会根据出在 prepare 状态的 redo log 记录去查找相同 LSN 的 binlog、数据记录，从⽽实 现异常后的恢复

# redolog的组织

redo log 是以“块”为单位进⾏存储的，称之为“redo log block”，每个块的⼤⼩是 512 字节 以块为单位存储的原因是他和磁盘扇区的⼤⼩是相同的，从⽽保证在异常情况发⽣时不会出现部分写⼊成功产⽣的脏数据

# 相关配置

### innodb_log_file_size

redo log 磁盘空间⼤⼩，默认为 5M

### innodb_log_buffer_size

redo log 缓存⼤⼩，默认为 8M

### innodb_flush_log_at_trx_commit

此前我们曾经介绍过，操作系统为了减少了磁盘的读写次数，提升系统的 IO 性能，会在内存空间中分配⼀个缓冲区，这就是 ⻚⾯⾼速缓冲，虽然⾼速缓冲让 IO 性能得以⼤幅提升，但在宕机等异常发⽣时，这部分在⾼速缓冲区中的数据就会丢失，因 此 unix 提供了系统调⽤ fsync来让我们⼿动执⾏⾼速缓冲到磁盘的刷新⼯作 对于 redolog 来说，由于他的存在就是为了避免异常情况造成的已提交事务的丢失，所以⾼速缓冲引起的未刷盘数据丢失是不 能容忍的，i nodb_flush_log_at_trx_comit 配置项就是指定具体的刷盘策略的 他有以下值可以选择：

a.

- 0- 以固定间隔将缓存中的数据写⼊系统⾼速缓存并调⽤⼀次 fsync 强制刷新⾼速缓冲，系统崩溃可能丢失最⼤1秒 的数据
- 1- 默认值，每次事务提交时调⽤ fsync，这种⽅式即使系统崩溃也不会丢失任何数据，但是因为每次提交都写⼊磁 盘，IO的性能较差

- 2- 每次事务提交都将数据写⼊系统⾼速缓存，但仅在固定间隔调⽤⼀次 fsync 强制刷新⾼速缓冲，安全性⾼于配 置为 0


a.

a.

通常，为了绝对的安全性，我们会配置为 1，但在追求最⾼的写⼊性能时，我们通常配置为 2，因为设置为 2 与设置为 0 在性 能上差异不⼤，但配置为 2 却在安全性上⾼于配置为 0 同时为了保证 binlog 的安全性，我们同时要配置 sync_binlog 为 1，保证每次 binlog 都直接写⼊磁盘，⽽不进⾏缓存

![image 21](<正确的理解MySQL的MVCC及实现原理.note_images/imageFile21.png>)

### innodb_flush_log_at_timeout

上⾯提到了刷新告诉缓存的固定间隔，这个“固定间隔”就是通过 i nodb_flush_log_at_timeout 配置项指定的，默认是 1 秒 但实际上，如果 redo log 的缓存占⽤超过⼀半，也会⽴即触发缓冲的刷新

