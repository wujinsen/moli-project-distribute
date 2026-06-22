left join的困惑：⼀旦加上where条件，则显示的结果等于i ner join 将where 换成 and ⽤where 是先连接然后再筛选 ⽤and 是先筛选再连接 数据库在通过连接两张或多张表来返回记录时，都会⽣成⼀张中间的临时表，然后再将这张临时表返 回给⽤户。

在使⽤left jion时，on和where条件的区别如下：

- 1、 on条件是在⽣成临时表时使⽤的条件，它不管on中的条件是否为真，都会返回左边表中的记录。

- 2、where条件是在临时表⽣成好后，再对临时表进⾏过滤的条件。这时已经没有left join的含义（必须 返回左边表的记录）了，条件不为真的就全部过滤掉。


假设有两张表：

- 表1 tab1： id size

- 1 10

- 2 20

- 3 30


- 表2 tab2： size name 10 A


- 20 B

- 20 C


两条SQL:

- 1、select * form tab1 left join tab2 on (tab1.size = tab2.size) where tab2.name=ʼ Aʼ

- 2、select * form tab1 left join tab2 on (tab1.size = tab2.size and tab2.name=ʼ Aʼ) 第⼀条SQL的过程：


- 1、中间表 on条件: tab1.size = tab2.size

- tab1.id tab1.size tab2.size tab2.name

- 1 10 10 A

- 2 20 20 B


- 2 20 20 C

- 3 30 (nul) (nul)


2、再对中间表过滤 where 条件：

- tab2.name=ʼ Aʼ tab1.id tab1.size tab2.size tab2.name 1 10 10 A




第⼆条SQL的过程： 1、中间表 on条件: tab1.size = tab2.size and tab2.name=ʼ Aʼ (条件不为真也会返回左表中的记录) tab1.id tab1.size tab2.size tab2.name

- 1 10 10 A

- 2 20 (nul) (nul)

- 3 30 (nul) (nul) 其实以上结果的关键原因就是left join,right join,ful join的特殊性，不管on上的条件是否为真都会返


回left或right表中的记录，ful则具有left和right的特性的并集。 ⽽i ner jion没这个特殊性，则条件放 在on中和where中，返回的结果集是相同的。

