背景 经历了前⾯两篇的介绍MySQL索引，相信⼤家也可以很清晰的认识到索引。这⼀节想分享⼀下在 MySQL中给字段加了索引，但是查询的时候却不⽣效索引的情况，让更多的开发者可以少踩坑，接下 来直接进⼊正⽂～～～ 为什么索引不⽣效 在上⼀篇MySQL（⼆）如何设计索引我们有提到过，MySQL使⽤的是基于成本的优化器，但是由于查 询优化技术是关系型数据库实现中的难点，因此总会有⼀些索引不⽣效的情况。 接下来我们先建⽴⼀张表，并且插⼊模拟数据，来分析什么情况索引不⽣效。

CREATE TABLE `t4` ( `id` int NOT NULL AUTO_INCREMENT, `account` varchar(50) DEFAULT NULL, `client_type` tinyint DEFAULT NULL, `security_code` varchar(50) DEFAULT NULL, PRIMARY KEY (`id`), KEY `idx_client_type` (`client_type`), KEY `idx_account` (`account`), KEY `idx_security_code` (`security_code`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8

- 1、在索引字段上运算 查询数据库表的时候，已经创建了索引，WHERE条件中也包含了索引列，但是列对象上有函数或者运 算符，这样会导致索引失效。 ⽐如下⾯这条SQL语句： select * from t4 where id-1 = 1;

从上⾯实验的执⾏计划可以得出，在索引列上使⽤函数或者运算符，会导致索引⽆法⽣效。

- 2、多个索引字段进⾏运算 查询数据库表时，已经创建了索引，WHERE条件中也包含了索引列，但是列对象进⾏了运算操作。


![image 1](<MySQL索引索引不生效的情况.note_images/imageFile1.png>)

⽐如下⾯这条SQL语句：

select * from t4 where id + client_type = 1;

![image 2](<MySQL索引索引不生效的情况.note_images/imageFile2.png>)

从以上实验来看，即使两个列上都有索引字段，MySQL仍然⽆法在表达式中使⽤这些索引。

- 3、隐性转换 如果索引列是INT类型，隐性转换可以使⽤到索引。但是如果索引列是字符型，隐性转换⽆法使⽤索 引。 ⽐如下⾯这条SQL语句： # 不能使⽤索引，因为security_code字段是字符，它要变成INT型才能和688688⽐较，所以索引失效 select * from t4 where security_code = 688688;

# 可以使⽤索引，因为查询优化器是把'1'变成1，然后索引列没有变化，可以使⽤索引。 select * from t4 where id = '1';

在当前版本中，MySQL查询优化器已经可以转换字符型数字了，从⽽使⽤索引。但是反过来，索引失 效。

- 4、Like LIKE关键字，如果值是ʼ% Xʼ或者ʼ% X%ʼ，则⽆法使⽤索引。 如果值是ʼ X%ʼ，可以正常使⽤索引。这是因为通配符ʼ%'位于前⾯，会导致查询优化器不得不使⽤全 表查询，导致索引失效。


![image 3](<MySQL索引索引不生效的情况.note_images/imageFile3.png>)

⽐如下⾯的SQL语句：

select * from t4 where id like '%1';

![image 4](<MySQL索引索引不生效的情况.note_images/imageFile4.png>)

如果业务中必须要⽤到模糊查询的话，我们可以试着引⼊全⽂搜索引擎ElasticSearch。

- 5、OR操作符 篇幅原因，我就不⼀⼀演示了，直接说结论，你们也可以去试试。
- 6、GROUP BY⼦句 查询数据库表，WHERE条件不包含索引列，但是GROUP BY⼦句的条件中包含索引列。这个时候即使 explain会显示它是⾛group by字句的索引，但是扫描的rows也是接近于全表扫描。 你可以⾃⼰对⽐⼀下，WHERE字句中的条件有索引和⽆索引的SQL性能将会差距⾮常⼤，在全表扫描 的情况下SQL的性能惨不忍睹。
- 7、ORDER BY⼦句 和上⾯的GROUP BY⼦句类似，在MySQL查询优化器的代价估算模型中， ORDER BY和GROUP BY的 代价，相对来说⾮常⾼，如果有索引就会尽可能的使⽤它。
- 8、联合索引 根据上⾯的第6条和第7条，只要给SQL语句中的WHERE⼦句和ORDER BY/GROUP BY⼦句加上⼀个联 合索引就可以解决全表扫描的问题。 联合索引中索引失效的情况：


- 1.
- 2.
- 3.
- 4.


OR条件的两边都是同⼀个索引列的情况下，如果WHERE条件是主键，则可以使⽤索引 OR条件的两边都是同⼀个索引列的情况下，如果WHERE条件不是主键，则是否使⽤索引取决于 MySQL查询优化器的代价估算。 OR条件的两边是不同的索引列，是否使⽤索引取决于MySQL查询优化器的代价估算。如果能使⽤ 索引，MySQL会使⽤索引，如果代价太⾼，仍然会⾛全表索引 如果多个OR条件中有其中⼀个条件没有索引，则必须进⾏全表索引

没有使⽤索引前缀，就是没有遵循联合索引的最左匹配原则

使⽤了联合索引的全部列，但是索引键不是AND操作，可能使⽤了OR操作符

总结

这⼀节讨论了MySQL中⽆法使⽤索引的⼀些场景，可能会有遗漏，有错误的地⽅可以评论区提出 来。

对于WHERE⼦句来说，建议不要把运算操作放到SQL语句中，能在代码⾥⾯去运算尽量在代码⾥⾯ 运算，可以避免索引失效

如果模糊查询⽐较多，可以引⼊ES来帮助你进⾏模糊查询

ORDER BY和GROUP BY这两个⼦句，需要防范的问题是没有给WHERE条件设计索引，你在查看执 ⾏计划时也会⽐较迷惑，所以⼀定要注意

希望你们读完这篇⽂章， 可以让你们在MySQL的这条路上少⾛弯路～～～

