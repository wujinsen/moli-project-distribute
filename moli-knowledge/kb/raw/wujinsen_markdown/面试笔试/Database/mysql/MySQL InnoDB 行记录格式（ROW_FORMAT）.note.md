# ⼀、⾏记录格式的分类和介绍

在早期的I noDB版本中，由于⽂件格式只有⼀种，因此不需要为此⽂件格式命名。随着I noDB引擎的发展，开发出了不兼容 早期版本的新⽂件格式，⽤于⽀持新的功能。为了在升级和降级情况下帮助管理系统的兼容性，以及运⾏不同的MySQL版 本，I noDB开始使⽤命名的⽂件格式。

![image 1](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile1.png>)

- 1. Antelope: 先前未命名的，原始的InnoDB⽂件格式。它⽀持两种⾏格式：COMPACT 和 REDUNDANT。MySQL5.6的 默认⽂件格式。可以与早期的版本保持最⼤的兼容性。不⽀持 Barracuda ⽂件格式。
- 2. Barracuda: 新的⽂件格式。它⽀持InnoDB的所有⾏格式，包括新的⾏格式：COMPRESSED 和 DYNAMIC。与这两个 新的⾏格式相关的功能包括：InnoDB表的压缩，⻓列数据的⻚外存储和索引建前缀最⼤⻓度为3072字节。 在 msyql 5.7.9 及以后版本，默认⾏格式由innodb_default_row_format变量决定，它的默认值是DYNAMIC，也可以在 create table 的时候指定ROW_FORMAT=DYNAMIC。⽤户可以通过命令 SHOW TABLE STATUS LIKE'table_name' 来查 看当前表使⽤的⾏格式，其中 row_format 列表示当前所使⽤的⾏记录结构类型。 PS：如果要修改现有表的⾏模式为compressed或dynamic，必须先将⽂件格式设置成Barracuda：set global innodb_file_format=Barracuda;，再⽤ALTER TABLE tablename ROW_FORMAT=COMPRESSED;去修改才能⽣效。


<table>
  <tr>
    <th>![image 2](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile2.png>)</th>
  </tr>
</table>


mysql> show variables like "innodb_file_format";

+--------------------+-----------+ | Variable_name | Value | +--------------------+-----------+ | innodb_file_format | Barracuda | +--------------------+-----------+ 1 row in set (0.00 sec)

<table>
  <tr>
    <th>![image 3](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile3.png>)</th>
  </tr>
</table>


<table>
  <tr>
    <th>![image 4](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile4.png>)</th>
  </tr>
</table>


mysql> show table status like "test%"\G

*************************** 1. row ***************************

Name: test Engine: MyISAM Version: 10

Row_format: Dynamic Rows: 4

Avg_row_length: 20 Data_length: 80

Max_data_length: 281474976710655

Index_length: 1024 Data_free: 0

Auto_increment: NULL

Create_time: 2018-08-07 13:07:59 Update_time: 2018-08-07 13:08:01

Check_time: NULL Collation: utf8_general_ci Checksum: NULL Create_options: row_format=DYNAMIC Comment:

<table>
  <tr>
    <th>![image 5](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile5.png>)</th>
  </tr>
</table>


# ⼆、I noDB⾏存储

I noDB表的数据存储在⻚（page）中，每个⻚可以存放多条记录。这些⻚以树形结构组织，这颗树称 为B树索引。表中数据和辅助索引都是使⽤B树结构。维护表中所有数据的这颗B树索引称为聚簇索 引，通过主键来组织的。聚簇索引的叶⼦节点包含⾏中所有字段的值，辅助索引的叶⼦节点包含索引 列和主键列。 变⻓字段是个例外，例如对于BLOB和VARCHAR类型的列，当⻚不能完全容纳此列的数据时，会将此 列的数据存放在称为溢出⻚(overflow page)的单独磁盘⻚上，称这些列为⻚外列(of-page column)。 这些列的值存储在以单链表形式存在的溢出⻚列表中，每个列都有⾃⼰溢出⻚列表。某些情况下，为 了避免浪费存储空间和消除读取分隔⻚，列的所有或前缀数据会存储在B+树索引中。

# 三、Compact 和 Redundant （⼀）Compact

Compact⾏记录是在MySQL5.0中引⼊的，为了⾼效的存储数据，简单的说，就是为了让⼀个⻚（Page）存放的⾏数据越 多，这样性能就越⾼。⾏记录格式如下：

![image 6](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile6.png>)

- 1. 变⻓字段⻓度列表：变⻓字段⻓度最⼤不超过2字节（MySQL数据库varcahr类型的最⼤⻓度限制为65535）
- 2. NULL标识位：该位指示了该⾏数据中是否有NULL值，有则⽤1。
- 3. 记录头信息：固定占⽤5字节（40位）
- 4. 列N数据：实际存储每列的数据，NULL不占该部分任何空间，即NULL占有NULL标志位，实际存储不占任何空间。 PS：每⼀⾏数据除了⽤户定义的例外，还有两个隐藏列，事物ID列和回滚指针列，分别位6字节和7字节的⼤⼩，若InnoDB 表没有定义主键，每⾏还未增加⼀个6字节的rowid列。


![image 7](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile7.png>)

（⼆）Redundant

MySQL5.0之前的⾏记录格式：

![image 8](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile8.png>)

- 1. 字段偏移列表：同样是按照列的顺序逆序放置的，若列的⻓度⼩于255字节，⽤1字节表示，若⼤于255字节，⽤2字节表 示。
- 2. 记录头信息：占⽤6字节（48位）


![image 9](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile9.png>)

（三）⾏溢出数据

- 1. 当⾏记录的⻓度没有超过⾏记录最⼤⻓度时，所有数据都会存储在当前⻚。
- 2. 当⾏记录的⻓度超过⾏记录最⼤⻓度时，变⻓列（variable-length column）会选择外部溢出⻚（overflow page，⼀般是Uncompressed BLOB Page）进⾏存储。 Compact + Redundant：保留前768Byte在当前⻚（B+Tree叶⼦节点），其余数据存放在溢出⻚。768Byte后⾯跟着 20Byte的数据，⽤来存储指向溢出⻚的指针。 （四）概述 对于 Compact 和 Redundant ⾏格式，InnoDB将变⻓字段(VARCHAR, VARBINARY, BLOB 和 TEXT)的前786字节存储 在B+树节点中，其余的数据存放在溢出⻚(off-page)，如下图：


![image 10](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile10.png>)

上⾯所讲的讲的blob或变⻓⼤字段类型包括blob,text,varchar，其中varchar列值⻓度⼤于某数N时也会存溢出⻚，在latin1 字符集下N值可以这样计算：innodb的块⼤⼩默认为16kb，由于innodb存储引擎表为索引组织表，树底层的叶⼦节点为⼀双 向链表，因此每个⻚中⾄少应该有两⾏记录，这就决定了innodb在存储⼀⾏数据的时候不能够超过8k，减去其它列值所占字 节数，约等于N。 使⽤Antelope⽂件格式，若字段的值⼩于等于786字节，不需要溢出⻚，因为字段的值都在B+树节点中，所以会降低I/O操 作。这对于相对较短的BLOB字段有效，但可能由于B+树节点存储过多的数据⽽导致效率低下。

四、Compressed 和 Dynamic InnoDB1.0x开始引⼊⼼的⽂件格式（file format，⽤户可以理解位新的⻚格式）——Barracuda（图1），这个新的格式拥 有两种新的⾏记录格式：Compressed和Dynamic。 新的两种记录格式对于存放BLOB中的数据采⽤了完全的⾏溢出的⽅式。如图：

![image 11](<MySQL InnoDB 行记录格式（ROW_FORMAT）.note_images/imageFile11.png>)

Dynamic⾏格式，列存储是否放到off-page⻚，主要取决于⾏⼤⼩，他会把⾏中最⻓的⼀列放到off-page，直到数据⻚能存 放下两⾏。TEXT或BLOB列<=40bytes时总是存在于数据⻚。这种⽅式可以避免compact那样把太多的⼤列值放到B-tree Node（数据⻚中只存放20个字节的指针，实际的数据存放在Off Page中，之前的Compact 和 Redundant 两种格式会存放 768个字前缀字节）。 Compressed物理结构上与Dynamic类似，Compressed⾏记录格式的另⼀个功能就是存储在其中的⾏数据会以zlib的算法 进⾏压缩，因此对于BLOB、TEXT、VARCHAR这类⼤⻓度数据能够进⾏有效的存储（减少40%，但对CPU要求更⾼）。

