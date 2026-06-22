MySQL使⽤规范

explain: 这个命令来查看⼀个这些SQL语句的执⾏计划，查看该SQL语句有没有使⽤上了索引，有没有做全表 扫描，这都可以通过explain命令来查看。

expain出来的信息有10列，分别是id、select_type、table、type、possible_keys、key、 key_len、ref、rows、Extra

![image 1](<mysql规范.note_images/imageFile1.png>)

<table>
  <tr>
    <th>列</th>
    <th>列值</th>
    <th>说明</th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
  </tr>
  <tr>
    <td> </td>
    <td>SIMPLE</td>
    <td>简单 SELECT， 不使⽤ UNION或 ⼦查询等</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>PRIMARY</td>
    <td>⼦查询中 最外层查 询，查询 中若包含 任何复杂 的⼦部 分，最外 层的 select被 标记为 PRIMARY</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>UNION</td>
    <td>UNION中<br><br>的第⼆个 或后⾯的 SELECT语<br><br>句</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>select_t ype</td>
    <td>DEPENDE NT UNION</td>
    <td>UNION中<br><br>的第⼆个 或后⾯的 SELECT语<br><br>句，取决 于外⾯的 查询</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>UNION RESULT</td>
    <td>UNION的 结果， union语句<br><br>中第⼆个 select开 始后⾯所 有select</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>SUBQUE RY</td>
    <td>⼦查询中 的第⼀个 SELECT，<br><br>结果不依 赖于外部 查询</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


<table>
  <tr>
    <th> </th>
    <th>DEPENDE NT SUBQUE RY</th>
    <th>⼦查询中 的第⼀个 SELECT，<br><br>依赖于外 部查询</th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
  </tr>
  <tr>
    <td> </td>
    <td>DERIVED</td>
    <td>派⽣表的 SELECT, FROM⼦ 句的⼦查 询</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>UNCACH EABLE SUBQUE RY</td>
    <td>⼀个⼦查 询的结果 不能被缓 存，必须 重新评估 外链接的 第⼀⾏</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>al</td>
    <td>Full Table Scan， MySQL将<br><br>遍历全表 以找到匹 配的⾏</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>index</td>
    <td>Full Index Scan， index与 ALL区别为 index类型 只遍历索 引树</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>type</td>
    <td>range</td>
    <td>只检索给 定范围的 ⾏，使⽤ ⼀个索引 来选择⾏</td>
    <td>依次从最 优到最差 分别 为: system > const > eq_ref ><br><br>ef ><br><br>range > index ></td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


# AL

<table>
  <tr>
    <th> </th>
    <th>ref</th>
    <th>表示上述 表的连接 匹配条 件，即哪 些列或常 量被⽤于 查找索引 列上的值</th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
  </tr>
  <tr>
    <td> </td>
    <td>eq_ref</td>
    <td>类似ref， 区别就在 使⽤的索 引是唯⼀ 索引，对 于每个索 引键值， 表中只有 ⼀条记录 匹配，简 单来说， 就是多表 连接中使 ⽤ primary key或者 unique key作为关 联条件</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


<table>
  <tr>
    <th> </th>
    <th>const、 system</th>
    <th>当MySQL 对查询某 部分进⾏ 优化，并 转换为⼀ 个常量 时，使⽤ 这些类型 访问。如 将主键置 于where 列表中， MySQL就 能将该查 询转换为 ⼀个常 量，<br><br>system是 const类型<br><br>的特例， 当查询的 表只有⼀ ⾏的情况 下，使⽤ system</th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
  </tr>
  <tr>
    <td> </td>
    <td>NULL</td>
    <td>MySQL在<br><br>优化过程 中分解语 句，执⾏ 时甚⾄不 ⽤访问表 或索引， 例如从⼀ 个索引列 ⾥选取最 ⼩值可以 通过单独 索引查找 完成。</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


<table>
  <tr>
    <th>rows</th>
    <th> </th>
    <th>估算出结 果集⾏ 数，表示 MySQL根 据表统计 信息及索 引选⽤情 况，估算 的找到所 需的记录 所需要读 取的⾏数 这⼀列是 mysql估计 要读取并 检测的⾏ 数，注意 这个不是 结果集⾥ 的⾏数。</th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>Using where</td>
    <td>查询的列 未被索引 覆盖， where筛选 条件⾮索 引的前导 列</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td>Extra</td>
    <td>Using temporar y</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
  <tr>
    <td> </td>
    <td>Using filesort</td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


<table>
  <tr>
    <th> </th>
    <th>Using index</th>
    <th>查询的列 被索引覆 盖，并且 where筛选 条件是索 引的前导 列，是性 能⾼的表 现。⼀般 是使⽤了 覆盖索引 (索引包含 了所有查 询的字 段)。对于 i nodb来 说，如果 是辅助索 引性能会 有不少提 ⾼</th>
    <th>能够触发 覆盖索引</th>
    <th> </th>
    <th> </th>
    <th> </th>
    <th> </th>
  </tr>
  <tr>
    <td> </td>
    <td>Using index condition<br><br></td>
    <td>表示使⽤的索 引⽅式为⼆级 检索(⾮聚簇 索引树)<br><br></td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


降低磁盘IO和CPU计算

⼀张student表，表设计结构，索引，内容如下图

![image 2](<mysql规范.note_images/imageFile2.png>)

![image 3](<mysql规范.note_images/imageFile3.png>)

![image 4](<mysql规范.note_images/imageFile4.png>)

- 1.尽量不⽤join，必须要保证字符集属性类型和⻓度相同，并建⽴索引 字符集不同会触发全表扫描
- 2.禁⽌%开头查询，禁⽌ != , not in等负向查询，会全表扫描

第⼀条语句%开头,type类型为AL,key没有命中索引, rows为6全表扫描，性能会很差。 第⼆条语句没有%开头, type类型为range, key命中索引， rows为1命中数据，性能会很好。

!=同理

- 3.字段类型, 与查询字段赋值类型必须相同


![image 5](<mysql规范.note_images/imageFile5.png>)

![image 6](<mysql规范.note_images/imageFile6.png>)

![image 7](<mysql规范.note_images/imageFile7.png>)

![image 8](<mysql规范.note_images/imageFile8.png>)

- name类型为varchar，name=1类型不同，会有强制类型转换;name='1'类型相同。尽管命中了索引，但 是第⼀条语句rows=6，全表扫描。
- 4.字段必须定义为not nul,并提供默认值


![image 9](<mysql规范.note_images/imageFile9.png>)

![image 10](<mysql规范.note_images/imageFile10.png>)

表中6条数据，通过sql查询age!=18的数据，可以预想会有5条数据，然⽽结果只有四条，漏掉的⼀条 age允许为nul 因此得对sql进⾏改造: select * from student where age!=18 or age is nul; 这样才能查询出默认值为nul的数据，此为⼤坑。

- 5.禁⽌在列上进⾏函数或表达式计算
- 6.联合索引，区分度最⾼的放在最左边 过滤⼤量数据
- 7.联合索引，列个数不超过5个

降低数据库CPU计算

- 8.禁⽌使⽤外键约束，有服务端保证完整性
- 9.禁⽌使⽤存储过程，视图、触发器、Event
- 10. 禁⽌使⽤ SELECT * 必须使⽤ SELECT <字段列表> 查询 消耗更多的 CPU 和 IO 以⽹络带宽资源 ⽆法使⽤覆盖索引 可减少表结构变更带来的影响


1.orderby, group by ,dictinct要加索引

- 12. 垂直拆分，将字段短、访问频率⾼的字段放在⼀张表内

- 1)数据库本身有⾃⼰的内存缓冲池，以row为单位缓存数据，短row能缓存更多数据
- 2)⾼频率访问row，能直接访问缓存池数据，减少访问磁盘 举例:


- 13.分⻚查询优化


- 1)select返回列较少或列宽较⼩的时候，我们可以通过建⽴复合索引的⽅式优化分⻚查 覆盖索引(covering index): 将查询的字段建⽴到联合索引⾥⾯。 此时只有name建⽴索引，explain结果如下, type为al全表扫描


![image 11](<mysql规范.note_images/imageFile11.png>)

- 优化: 通过name,age建⽴索引，explain结果如下, type为index, extra为Using index
- 2)select返回列较多或列宽较⼤的时候，可以先查询⾮聚簇索引树，在查询聚簇索引树，防⽌频繁回表操作


![image 12](<mysql规范.note_images/imageFile12.png>)

![image 13](<mysql规范.note_images/imageFile13.png>)

必须使⽤i nodb(⾼并发数据量⼤) 必须要有注释 必须使⽤utf8或utf8mb4 数据库和表的字符集统⼀

禁⽌select *

不具备扩展性, 表结构会变更, 全部查出来传输字段也过⻓，需要多少列具体协商查询多少列 insert into 必须制定列

拒绝复杂sql,将⼤sql拆分多条简单sql

关于i nodb和mysiam选型最关键两点: 事务：对⼀致性帮助很⼤ ⾏锁: 对提⾼并发帮助很⼤ 不命中索引,i nodb不能⽤⾏锁，会⾛表锁，因为⾏锁是实现在索引上，⽽⾮锁在物理⾏记录上。

降低数据库磁盘IO 读多写少⽤缓存 前台后台分离架构 良好SQL 存储和索引

其余规范(线上环境)： 禁⽌在服务器上私⾃安装mysql客户端来访问数据库 禁⽌在业务⾼峰期批量操作 禁⽌跳过⼯单跳过审批私⾃操作线上数据库

