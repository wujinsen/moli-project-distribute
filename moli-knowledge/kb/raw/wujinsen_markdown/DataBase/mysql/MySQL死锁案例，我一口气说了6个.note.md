htps:/mp.weixin.q.com/s/BNsSjBhvGgtSGxipoQYjbQ

最近⾯试了不少⼈，总结了⼀波死锁问题，⼀共6个案例，和⼤家分享⼀下。

# Mysql 锁类型和加锁分析

MySQL有三种锁的级别：⻚级、表级、⾏级。

表级锁：开销⼩，加锁快；不会出现死锁；锁定粒度⼤，发⽣锁冲突的概率最⾼,并发度最低。

⾏级锁：开销⼤，加锁慢；会出现死锁；锁定粒度最⼩，发⽣锁冲突的概率最低,并发度也最 ⾼。

⻚⾯锁：开销和加锁时间界于表锁和⾏锁之间；会出现死锁；锁定粒度界于表锁和⾏锁之间，并 发度

算法：

next KeyLocks锁，同时锁住记录(数据)，并且锁住记录前⾯的Gap Gap锁，不锁记录，仅仅记录前⾯的Gap Recordlock锁（锁数据，不锁Gap） 所以其实 Next-KeyLocks=Gap锁+ Recordlock锁

# 死锁产⽣原因和示例

产⽣原因 所谓死锁<DeadLock>：是指两个或两个以上的进程在执⾏过程中,因争夺资源⽽造成的⼀种互相等待的 现象,若⽆外⼒作⽤，它们都将⽆法推进下去.此时称系统处于死锁状态或系统产⽣了死锁，这些永远在 互相等待的进程称为死锁进程。表级锁不会产⽣死锁.所以解决死锁主要还是针对于最常⽤的I noDB。 死锁的关键在于：两个(或以上)的Sesion加锁的顺序不⼀致。 那么对应的解决死锁问题的关键就是：让不同的sesion加锁有次序 产⽣示例 案例⼀ 需求：将投资的钱拆成⼏份随机分配给借款⼈。 起初业务程序思路是这样的： 投资⼈投资后，将⾦额随机分为⼏份，然后随机从借款⼈表⾥⾯选⼏个，然后通过⼀条条select for update 去更新借款⼈表⾥⾯的余额等。 例如两个⽤户同时投资，A⽤户⾦额随机分为2份，分给借款⼈1，2 B⽤户⾦额随机分为2份，分给借款⼈2，1 由于加锁的顺序不⼀样，死锁当然很快就出现了。

对于这个问题的改进很简单，直接把所有分配到的借款⼈直接⼀次锁住就⾏了。 Select * from x where id in (x,x,x) for update 在in⾥⾯的列表值mysql是会⾃动从⼩到⼤排序，加锁也是⼀条条从⼩到⼤加的锁

例如（以下会话id为主键）：

- Session1:

mysql> select * from t3 where id in (8,9) for update;

+----+--------+------+---------------------+ | id | course | name | ctime | +----+--------+------+---------------------+

- | 8 | WA | f | 2016-03-02 11:36:30 |

- | 9 | JX | f | 2016-03-01 11:36:30 |

+----+--------+------+---------------------+ rows in set (0.04 sec) Session2: select * from t3 where id in (10,8,5) for update; 锁等待中……

其实这个时候id=10这条记录没有被锁住的，但id=5的记录已经被锁住了，锁的等待在id=8的这⾥ 不信请看

- Session3: mysql> select * from t3 where id=5 for update; 锁等待中

- Session4: mysql> select * from t3 where id=10 for update;


+----+--------+------+---------------------+ | id | course | name | ctime | +----+--------+------+---------------------+

- | 10 | JB | g | 2016-03-10 11:45:05 |




+----+--------+------+---------------------+ row in set (0.00 sec) 在其它session中id=5是加不了锁的，但是id=10是可以加上锁的。

案例⼆ 在开发中，经常会做这类的判断需求：根据字段值查询（有索引），如果不存在，则插⼊；否则更 新。

以id为主键为例，⽬前还没有id=22的⾏

- Session1:


- select * from t3 where id=22 for update; Empty set (0.00 sec)

session2:

- select * from t3 where id=23 for update; Empty set (0.00 sec)


- Session1:

- insert into t3 values(22,'ac','a',now()); 锁等待中……

Session2:

- insert into t3 values(23,'bc','b',now()); ERROR 1213 (40001): Deadlock found when trying to get lock; try restarting transaction 当对存在的⾏进⾏锁的时候(主键)，mysql就只有⾏锁。 当对未存在的⾏进⾏锁的时候(即使条件为主键)，mysql是会锁住⼀段范围（有gap锁） 锁住的范围为： (⽆穷⼩或⼩于表中锁住id的最⼤值，⽆穷⼤或⼤于表中锁住id的最⼩值) 如：如果表中⽬前有已有的id为（ 1 ， 12） 那么就锁住（12，⽆穷⼤） 如果表中⽬前已有的id为（ 1 ， 30） 那么就锁住（ 1，30） 对于这种死锁的解决办法是： insert into t3(x,x) on duplicate key updatexx=' X'; ⽤mysql特有的语法来解决此问题。因为insert语句对于主键来说，插⼊的⾏不管有没有存在，都会只 有⾏锁




案例三

mysql> select * from t3 where id=9 for update;

+----+--------+------+---------------------+ | id | course | name | ctime | +----+--------+------+---------------------+ | 9 | JX | f | 2016-03-01 11:36:30 | +----+--------+------+---------------------+

row in set (0.00 sec)

- Session2: mysql> select * from t3 where id<20 for update; 锁等待中


Session1: mysql> insert into t3 values(7,'ae','a',now());

ERROR 1213 (40001): Deadlock found when trying to get lock; try restarting transaction

这个跟案例⼀其它是差不多的情况，只是sesion1不按常理出牌了， Sesion2在等待Sesion1的id=9的锁，sesion2⼜持了1到8的锁（注意9到19的范围并没有被sesion2 锁住），最后，sesion1在插⼊新⾏时⼜得等待sesion2,故死锁发⽣了。 这种⼀般是在业务需求中基本不会出现，因为你锁住了id=9，却⼜想插⼊id=7的⾏，这就有点跳了， 当然肯定也有解决的⽅法，那就是重理业务需求，避免这样的写法。 案例四

![image 1](<MySQL死锁案例，我一口气说了6个.note_images/imageFile1.png>)

⼀般的情况，两个sesion分别通过⼀个sql持有⼀把锁，然后互相访问对⽅加锁的数据产⽣死锁。 案例五

![image 2](<MySQL死锁案例，我一口气说了6个.note_images/imageFile2.png>)

两个单条的sql语句涉及到的加锁数据相同，但是加锁顺序不同，导致了死锁。 案例六 死锁场景如下：

CREATE TABLE dltask ( id bigint unsigned NOT NULL AUTO_INCREMENT COMMENT ‘auto id’,

- a varchar(30) NOT NULL COMMENT ‘uniq.a’,

- b varchar(30) NOT NULL COMMENT ‘uniq.b’,

- c varchar(30) NOT NULL COMMENT ‘uniq.c’, x varchar(30) NOT NULL COMMENT ‘data’, PRIMARY KEY (id), UNIQUE KEY uniq_a_b_c (a, b, c)


) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT=’deadlock test’;

a，b，c三列，组合成⼀个唯⼀索引，主键索引为id列。 事务隔离级别：

R (Repeatable Read) 每个事务只有⼀条SQL:

delete from dltask where a=? and b=? and c=?;

SQL的执⾏计划

![image 3](<MySQL死锁案例，我一口气说了6个.note_images/imageFile3.png>)

死锁⽇志

![image 4](<MySQL死锁案例，我一口气说了6个.note_images/imageFile4.png>)

众所周知，I noDB上删除⼀条记录，并不是真正意义上的物理删除，⽽是将记录标识为删除状态。 (注：这些标识为删除状态的记录，后续会由后台的Purge操作进⾏回收，物理删除。但是，删除状态 的记录会在索引中存放⼀段时间。) 在 R隔离级别下，唯⼀索引上满⾜查询条件，但是却是删除记 录，如何加锁？I noDB在此处的处理策略与前两种策略均不相同，或者说是前两种策略的组合：对于 满⾜条件的删除记录，I noDB会在记录上加next key lock X(对记录本身加X锁，同时锁住记录前的 GAP，防⽌新的满⾜条件的记录插⼊。) Unique查询，三种情况，对应三种加锁策略，总结如下：

此处，我们看到了next key锁，是否很眼熟？对了，前⾯死锁中事务1，事务2处于等待状态的锁，均为next key锁。明⽩了这三个加锁策略，其实构造⼀定的并发场景，死锁的原因已经呼之欲出。但是，还有⼀个前提 策略需要介绍，那就是I noDB内部采⽤的死锁预防策略。

找到满⾜条件的记录，并且记录有效，则对记录加X锁，No Gap锁(lock_mode X locks rec but not gap)； 找到满⾜条件的记录，但是记录⽆效(标识为删除的记录)，则对记录加next key锁(同时锁住记录本 身，以及记录之前的Gap：lock_mode X);

未找到满⾜条件的记录，则对第⼀个不满⾜条件的记录加Gap锁，保证没有满⾜条件的记录插⼊ (locks gap before rec)；

# 死锁预防策略

I noDB引擎内部(或者说是所有的数据库内部)，有多种锁类型：事务锁(⾏锁、表锁)，Mutex(保护内 部的共享变量操作)、RWLock(⼜称之为Latch，保护内部的⻚⾯读取与修改)。

I noDB每个⻚⾯为16K，读取⼀个⻚⾯时，需要对⻚⾯加S锁，更新⼀个⻚⾯时，需要对⻚⾯加上X锁。任何 情况下，操作⼀个⻚⾯，都会对⻚⾯加锁，⻚⾯锁加上之后，⻚⾯内存储的索引记录才不会被并发修改。

因此，为了修改⼀条记录，I noDB内部如何处理：

根据给定的查询条件，找到对应的记录所在⻚⾯； 对⻚⾯加上X锁(RWLock)，然后在⻚⾯内寻找满⾜条件的记录； 在持有⻚⾯锁的情况下，对满⾜条件的记录加事务锁(⾏锁：根据记录是否满⾜查询条件，记录是否 已经被删除，分别对应于上⾯提到的3种加锁策略之⼀)；

死锁预防策略：相对于事务锁，⻚⾯锁是⼀个短期持有的锁，⽽事务锁(⾏锁、表锁)是⻓期持有的锁。 因此，为了防⽌⻚⾯锁与事务锁之间产⽣死锁。I noDB做了死锁预防的策略：持有事务锁(⾏锁、表 锁)，可以等待获取⻚⾯锁；但反之，持有⻚⾯锁，不能等待持有事务锁。 根据死锁预防策略，在持有⻚⾯锁，加⾏锁的时候，如果⾏锁需要等待。则释放⻚⾯锁，然后等待⾏ 锁。此时，⾏锁获取没有任何锁保护，因此加上⾏锁之后，记录可能已经被并发修改。因此，此时要 重新加回⻚⾯锁，重新判断记录的状态，重新在⻚⾯锁的保护下，对记录加锁。如果此时记录未被并 发修改，那么第⼆次加锁能够很快完成，因为已经持有了相同模式的锁。但是，如果记录已经被并发 修改，那么，就有可能导致本⽂前⾯提到的死锁问题。 以上的I noDB死锁预防处理逻辑，对应的函数，是row0sel.c:row_search_for_mysql()。感兴趣的朋 友，可以跟踪调试下这个函数的处理流程，很复杂，但是集中了I noDB的精髓。

# 剖析死锁的成因

做了这么多铺垫，有了Delete操作的3种加锁逻辑、I noDB的死锁预防策略等准备知识之后，再回过头 来分析本⽂最初提到的死锁问题，就会⼿到拈来，事半⽽功倍。 ⾸先，假设dltask中只有⼀条记录：(1, ‘aʼ, ‘bʼ, ‘cʼ, ‘dataʼ)。三个并发事务，同时执⾏以下的这条SQL：

delete from dltask where a=’a’ and b=’b’ and c=’c’;

并且产⽣了以下的并发执⾏逻辑，就会产⽣死锁：

![image 5](<MySQL死锁案例，我一口气说了6个.note_images/imageFile5.png>)

上⾯分析的这个并发流程，完整展现了死锁⽇志中的死锁产⽣的原因。其实，根据事务1步骤6，与事 务0步骤3/4之间的顺序不同，死锁⽇志中还有可能产⽣另外⼀种情况，那就是事务1等待的锁模式为记 录上的X锁 + No Gap锁(lock_mode X locks rec but not gap waiting)。这第⼆种情况，也是”润洁”同 学给出的死锁⽤例中，使⽤MySQL 5.6.15版本测试出来的死锁产⽣的原因。 此类死锁，产⽣的⼏个前提：

Delete操作，针对的是唯⼀索引上的等值查询的删除；(范围下的删除，也会产⽣死锁，但是死锁的 场景，跟本⽂分析的场景，有所不同) ⾄少有3个(或以上)的并发删除操作； 并发删除操作，有可能删除到同⼀条记录，并且保证删除的记录⼀定存在； 事务的隔离级别设置为Repeatable Read，同时未设置i nodb_locks_unsafe_for_binlog参数(此参 数默认为FALSE)；(Read Comited隔离级别，由于不会加Gap锁，不会有next key，因此也不会 产⽣死锁) 使⽤的是I noDB存储引擎；(废话！MyISAM引擎根本就没有⾏锁)

参考

htps:/blog.csdn.net/mine_song/article/details/7106410 htp:/hedengcheng.com/?p=84 htp:/ w.cnblogs.com/sesionbest/articles/8689082.html

