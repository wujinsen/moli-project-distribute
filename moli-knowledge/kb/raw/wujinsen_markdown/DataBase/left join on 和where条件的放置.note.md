select * from

td

## left join (

select case_id as sup_case_id , count(*) supervise_number from

td_kcdc_case_sup_info

group by case_id

) sup

## on

sup.sup_case_id = td.case_id

where 1=1 /*不能去掉， 否则认为and 后的条件为 联合查询时的条件， 不能起过滤作⽤，由于 left join因此td表中记录将全部查出来*/

and td.con = 'xxxx'

总结：

- 1. 对于left join，不管on后⾯跟什么条件，左表的数据全部查出来，因此要想过滤需把条件放到 where后⾯

- 2. 对于inner join，满⾜on后⾯的条件表的数据才能查出，可以起到过滤作⽤。也可以把条件放到 where后⾯。


参考：

# JOIN关联表中ON,WHERE后⾯跟条件的区别

http://wenku.baidu.com/view/fa341ad4c1c708a1284a4450.html

SQL中on条件与where条件的区别 (having)(转)

http://apps.hi.baidu.com/share/detail/20768615

SQL中on条件与where条件的区别

数据库在通过连接两张或多张表来返回记录时，都会⽣成⼀张中间的临时表，然后再将这张 临时表返回给⽤户。

在使⽤left jion时，on和where条件的区别如下：

- 1、 on条件是在⽣成临时表时使⽤的条件，它不管on中的条件是否为真，都会返回左边表中的记 录。

- 2、where条件是在临时表⽣成好后，再对临时表进⾏过滤的条件。这时已经没有left join的含义 （必须返回左边表的记录）了，条件不为真的就全部过滤掉。


假设有两张表：

- 表1：tab2

- 表2：tab2


<table>
  <tr>
    <th>id</th>
    <th>size</th>
  </tr>
  <tr>
    <td>1</td>
    <td>10</td>
  </tr>
  <tr>
    <td>2</td>
    <td>20</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
  </tr>
</table>


3 30

<table>
  <tr>
    <th>size</th>
    <th>name</th>
  </tr>
  <tr>
    <td>10</td>
    <td>A</td>
  </tr>
  <tr>
    <td>20</td>
    <td>B</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
  </tr>
</table>


20 C

两条SQL:

- 1、select * form tab1 left join tab2 on (tab1.size = tab2.size) where tab2.name=’AAA’

- 2、select * form tab1 left join tab2 on (tab1.size = tab2.size and tab2.name=’AAA’)


<table>
  <tr>
    <th>的过程：</th>
  </tr>
</table>


第⼀条SQL

<table>
  <tr>
    <th>1、中间表 on条件:<br><br></th>
    <th> </th>
  </tr>
  <tr>
    <td>tab1.size = tab2.size</td>
    <td> </td>
  </tr>
  <tr>
    <td>|<br><br>2、再对中间表过滤 where 条件：<br><br></td>
    <td>|</td>
  </tr>
  <tr>
    <td>tab2.name=ʼ Aʼ</td>
    <td> </td>
  </tr>
</table>


<table>
  <tr>
    <th>tab1.id</th>
    <th>tab1.size</th>
    <th>tab2.size</th>
    <th>tab2.name</th>
  </tr>
  <tr>
    <td>1</td>
    <td>10</td>
    <td>10</td>
    <td>A</td>
  </tr>
  <tr>
    <td>2</td>
    <td>20</td>
    <td>20</td>
    <td>B</td>
  </tr>
  <tr>
    <td>2</td>
    <td>20</td>
    <td>20</td>
    <td>C</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


3 30 (nul) (nul)

<table>
  <tr>
    <th>tab1.id</th>
    <th>tab1.size</th>
    <th>tab2.size</th>
    <th>tab2.name</th>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


1 10 10 A

<table>
  <tr>
    <th>的过程：</th>
  </tr>
</table>


第⼆条SQL

<table>
  <tr>
    <th>1、中间表 on条件: tab1.size = tab2.size and tab2.name=ʼ Aʼ<br><br>条件不为真<br><br></th>
    <th> </th>
  </tr>
</table>


( 也会返回左表中的记录)

<table>
  <tr>
    <th>tab1.id</th>
    <th>tab1.size</th>
    <th>tab2.size</th>
    <th>tab2.name</th>
  </tr>
  <tr>
    <td>1</td>
    <td>10</td>
    <td>10</td>
    <td>A</td>
  </tr>
  <tr>
    <td>2</td>
    <td>20</td>
    <td>(nul)</td>
    <td>(nul)</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


3 30 (nul) (nul)

其实以上结果的关键原因就是left join,right join,full join的特殊性，不管on上的条件是否为真 都会返回left或right表中的记录，full则具有left和right的特性的并集。 ⽽inner jion没这个特殊性， 则条件放在on中和where中，返回的结果集是相同的。

on、where、having的区别

on、where、having这三个都可以加条件的⼦句中，on是最先执⾏，where次之，having最后。 有时候如果这先后顺序不影响中间结果的话，那最终结果是相同的。但因为on是先把不符合条件 的记录过滤后才进⾏统计，它就可以减少中间运算要处理的数据，按理说应该速度是最快的。

根据上⾯的分析，可以知道where也应该⽐having快点的，因为它过滤数据后才进⾏sum，所以 having是最慢的。但也不是说having没⽤，因为有时在步骤3还没出来都不知道那个记录才符合要 求时，就要⽤having了。

在两个表联接时才⽤on的，所以在⼀个表的时候，就剩下where跟having⽐较了。在这单表查 询统计的情况下，如果要过滤的条件没有涉及到要计算字段，那它们的结果是⼀样的，只是 where可以使⽤rushmore技术，⽽having就不能，在速度上后者要慢。

如果要涉及到计算的字段，就表示在没计算之前，这个字段的值是不确定的，根据上篇写的⼯作 流程，where的作⽤时间是在计算之前就完成的，⽽having就是在计算后才起作⽤的，所以在这种 情况下，两者的结果会不同。

在多表联接查询时，on⽐where更早起作⽤。系统⾸先根据各个表之间的联接条件，把多个表 合成⼀个临时表后，再由where进⾏过滤，然后再计算，计算完后再由having进⾏过滤。由此可 ⻅，要想过滤条件起到正确的作⽤，⾸先要明⽩这个条件应该在什么时候起作⽤，然后再决定放 在那⾥

JOIN联表中ON,WHERE后⾯跟条件的区别

对于JOIN的连表操作，这⾥就不细述了，当我们在对表进⾏JOIN关联操作时，对于ON和WHERE后⾯的条件，不清楚⼤家有没有 注意过，有什么区别，可能有的朋友会认为跟在它们后⾯的条件是⼀样的，你可以跟在ON后⾯，如果愿意，也可以跟在WHERE后 ⾯。它们在ON和WHERE后⾯究竟有⼀个什么样的区别呢？

在JOIN操作⾥，有⼏种情况。LEFT JOIN,RIGHT JOIN,INNER JOIN等。

为了清楚的表达主题所描述的问题，我简要的对LEFT,RIGHT,INNER这⼏种连接⽅式作⼀个说 明。

下⾯就拿⼀个普通的博客系统的⽇志表(post)和分类表(category)来描述吧。

这⾥我们规定有的⽇志可能没有分类，有的分类可能⽬前没有属于它的⽂章。

- 1. LEFT JOIN: （保证找出左联表中的所有⾏） 查出所有⽂章，并显示出他们的分类：

复制代码

SELECT p.title,c.category_name FROM post p LEFT JOIN category c ON p.cid = c.cid

- 2. RIGHT JOIN: （保证找出右联表中的所有⾏） 查询所有的分类，并显示出该分类所含有的⽂章数。

复制代码

SELECT COUNT(p.id),c.category_name FROM post p RIGHTJOIN category c ON p.pid = c.cid

- 3. INNER JOIN （找出两表中关联相等的⾏）


查询有所属分类的⽇志。（即那些没有所性分类的⽇志⽂章将不要我们的查询范围之内）。

复制代码

SELECT p.title,c.category_name FROM post p INNER JOIN category c ON p.cid = c.cid.

这种情况和直接两表硬关联等价。

现在我们回过头来看上⾯的问题。

对于第⼀种情况，如果我们所ON 的条件写在WHERE 后⾯，将会出现什么情况呢？

即：

复制代码

SELECT p.title,c.category_name FROM post p LEFT JOIN category c WHERE p.cid = c.cid

对于第⼆种情况，我们同样按照上⾯的书写⽅式。

复制代码

SELECT COUNT(p.id),c.category_name FROM post p RIGHTJOIN category c WHERE p.pid = c.cid

如果运⾏上⾯的SQL语句，就会发现，它们已经过滤掉了⼀些不满⾜条件的记录，可能在这⾥， ⼤家会产⽣疑问了，不是⽤了LEFT和RIGHT吗？它们可以保证左边或者右边的所有⾏被全部查询 出来，为什么现在不管⽤了呢？对于出现这种的问题，呵呵！是不是觉得有些不可思议。

出现这种的问题，原因就在WHERE和ON这两个关键字后⾯跟条件。

好了，现在我也不调⼤家味⼝了，给⼤家提示答案吧。

对于JOIN参与的表的关联操作，如果需要不满⾜连接条件的⾏也在我们的查询范围内的话，我们 就必需把连接条件放在ON后⾯，⽽不能放在WHERE后⾯，如果我们把连接条件放在了WHERE 后⾯，那么所有的LEFT,RIGHT,等这些操作将不起任何作⽤，对于这种情况，它的效果就完全等 同于INNER连接。对于那些不影响选择⾏的条件，放在ON或者WHERE后⾯就可以。

记住：所有的连接条件都必需要放在ON后⾯，不然前⾯的所有LEFT,和RIGHT关联将作为摆设， ⽽不起任何作⽤。

这个问题是前⼏天在我们phpoo讨论区⾥提的问题，⼀直没有写出⽐较详细的区别，其实这个问题 完全可以⽤⼀句话描述清楚，那么，为什么我要在这⾥写上这么多啰嗦的话，主要是因为在对⾃ ⼰的知识进⾏巩固的同时，我也希望能给更多的朋友带来⽅便，⽽且现在我们的phpoo团队⾥各成 员的⽔平参差不⻬，所以为了照顾更多的⼈，才多啰嗦了这么⼏句，希望团⾥⾯的⾼⼿们不了笑 话。

