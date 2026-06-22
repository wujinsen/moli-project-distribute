1、简介： 查询缓存存储SELECT查询的⽂本以及发送给客户端的相应结果。如果随后收到⼀个相同的查询，服务 器从查询缓存中重新得到查询结果，⽽不再需要解析和执⾏查询。如果你有⼀个不经常改变的表并且 服务器收到该表的⼤量相同查询，查询缓存在这样的应⽤环境中⼗分有⽤。对于许多Web服务器来说 存在这种典型情况，它根据数据库内容⽣成⼤量的动态⻚⾯。 注释：查询缓存不返回旧的数据。当表更改后，查询缓存值的相关条⽬被清空。 注释：如果你有许多mysqld服务器更新相同的MyISAM表，在这种情况下查询缓存不起作⽤。 注释：查询缓存不适⽤于服务器⽅编写的语句。如果正在使⽤服务器⽅编写的语句，要考虑到这些语 句将不会应⽤查询缓存。参⻅ 。2、性能 下⾯是查询缓存的⼀些性能数据。这些结果是在Linux Alpha 2 x 50MHz系统（2GB RAM，64MB查 询缓存）上运⾏MySQL基准组件产⽣的。 · 如果执⾏的所有查询是简单的(如从只有⼀⾏数据的表中选取⼀⾏)，但查询是不同的，查询不能 被缓存，查询缓存激活率是13%。这可以看作是最坏的情形。在实际应⽤中，查询要复杂得多，因 此，查询缓存使⽤率⼀般会很低。 · 从只有⼀⾏的表中查找⼀⾏数据时，使⽤查询缓存⽐不使⽤速度快238%。这可以看作查询使⽤ 缓存时速度提⾼最⼩的情况。 服务器启动时要禁⽤查询缓存，设置query_cache_size系统变量为0。禁⽤查询缓存代码后，没有明显 的速度提⾼。编译MySQL时，通过在configure中使⽤ -without-query-cache选项，可以从服务器中 彻底去除查询缓存能⼒。

25.2.4节，“C API预处理语句”

# 3、⼯作原理

本节描述查询缓存的⼯作原理。 下⾯的两个查询被查询缓存认为是不相同的： SELECT * FROM tbl_name Select * from tbl_name 查询必须是完全相同的(逐字节相同)才能够被认为是相同的。另外，同样的查询字符串由于其它原因可 能认为是不同的。使⽤不同的数据库、不同的协议版本或者不同 默认字符集的查询被认为是不同的查 询并且分别进⾏缓存。 从查询缓存中提取⼀个查询之前，MySQL检查⽤户对所有相关数据库和表的SELECT权限。如果没有 权限，不使⽤缓存结果。 如果从查询缓存中返回⼀个查询结果，服务器把Qcache_hits状态变量的值加⼀，⽽不是Com_select 变量。 如果⼀个表被更改了，那么使⽤那个表的所有缓冲查询将不再有效，并且从缓冲区中移出。这包括那 些映射到改变了的表的使⽤MERGE表的查询。⼀个表可以被许多类型的语句更改，例如INSERT、 UPDATE、DELETE、TRUNCATE、ALTER TABLE、DROP TABLE或DROP DATABASE。 COMIT执⾏完后，被更改的事务I noDB表不再有效。

使⽤I noDB表时，查询缓存也在事务中⼯作，使⽤该表的版本号来检测其内容是否仍旧是当前的。 在MySQL 5.1中，视图产⽣的查询被缓存。 SELECT SQL_CALC_FOUND_ROWS.和SELECT FOUND_ROWS() type类型的查询使⽤查询缓存。 即使因创建的⾏数也被保存在缓冲区内，前⾯的查询从缓存中提取，FOUND_ROWS()也返回正确的 值。 如果⼀个查询包含下⾯函数中的任何⼀个，它不会被缓存：

<table>
  <tr>
    <th>BENCHMARK()</th>
    <th>CONECTION_ID()</th>
    <th>CURDATE()</th>
  </tr>
  <tr>
    <td>CURENT_DATE()</td>
    <td>CURENT_TIME()</td>
    <td>CURENT_TIMESTAMP()</td>
  </tr>
  <tr>
    <td>CURTIME()</td>
    <td>DATABASE()</td>
    <td>带⼀个参数的ENCRYPT()</td>
  </tr>
  <tr>
    <td>FOUND_ROWS()</td>
    <td>GET_LOCK()</td>
    <td>LAST_INSERT_ID()</td>
  </tr>
  <tr>
    <td>LOAD_FILE()</td>
    <td>MASTER_POS_WAIT()</td>
    <td>NOW()</td>
  </tr>
  <tr>
    <td>RAND()</td>
    <td>RELEASE_LOCK()</td>
    <td>SYSDATE()</td>
  </tr>
  <tr>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


不带参数的UNIX_TIMESTAMP() USER()

在下⾯的这些条件下，查询也不会被缓存： · 引⽤⾃定义函数(UDFs)。 · 引⽤⾃定义变量。 · 引⽤mysql系统数据库中的表。 · 下⾯⽅式中的任何⼀种：

1 SELECT ...IN SHARE MODE

1 SELECT ...FOR UPDATE

1 SELECT ...INTO OUTFILE ...

1 SELECT ...INTO DUMPFILE ...

1 SELECT * FROM ...WHERE autoincrement_col IS NULL

最后⼀种⽅式不能被缓存是因为它被⽤作为ODBC⼯作区来获取最近插⼊的ID值。 被作为编写好的语句，即使没有使⽤占位符。例如，下⾯使⽤的查询：

1 char *my_sql_stmt = "SELECT a，b FROM table_c";

1 /* ...*/

1 mysql_stmt_prepare(stmt，my_sql_stmt，strlen(my_sql_stmt));

不被缓存。 · 使⽤TEMPORARY表。 · 不使⽤任何表。 · ⽤户有某个表的列级权限。

# 4、如何使⽤查询缓存

可以在SELECT语句中指定查询缓存相关选项： SQL_CACHE：如果query_cache_type系统变量的值是ON或DEMAND，查询结果被缓存。 SQL_NO_CACHE：查询结果不被缓存。 示例：

1 SELECT SQL_CACHE id, name FROM customer;

1 SELECT SQL_NO_CACHE id, name FROM customer;

# 5、查看查询⾼速缓冲是否可⽤

mysql>SHOW VARIABLES LIKE 'have_query_cache';

1 +------------------+-------+

1 | Variable_name | Value |

1 +------------------+-------+

1 | have_query_cache | YES |

1 +------------------+-------+

即使禁⽤查询缓存，当使⽤标准 MySQL⼆进制时，这个值总是YES。 其它⼏个系统变量控制查询缓存操作。当启动mysqld时，这些变量可以在选项⽂件或者命令⾏中设 置。所有查询缓存系统变量名以query_cache_ 开头。

- 6、设置查询缓冲区⼤⼩ mysql> SET GLOBAL query_cache_size = 41984; mysql> SHOW VARIABLES LIKE 'query_cache_size';


+ -+ -+ | Variable_name | Value |

+ -+ -+ | query_cache_size | 41984 |

+ -+ -+

为了设置查询缓存⼤⼩，设置query_cache_size系统变量。设置为0表示禁⽤查询缓存。 默认缓存⼤ ⼩设置为0；也就是禁⽤查询缓存。 当设置query_cache_size变量为⾮零值时，应记住查询缓存⾄少⼤约需要40KB来分配其数据结构。 (具体⼤⼩取决于系统结构）。如果你把该值设置的太⼩，将会得到⼀个警告，如本例所示： mysql> SET GLOBAL query_cache_size = 4 0; Query OK, 0 rows afected, 1 warning (0.0 sec)

mysql> SHOW WARNINGS\G

* 1. row * Level: Warning

Code: 1282 Mesage: Query cache failed to set size 3936; new query cache size is 0

mysql> SET GLOBAL query_cache_size = 41984; Query OK, 0 rows afected (0.0 sec)

mysql> SHOW VARIABLES LIKE 'query_cache_size';

+ -+ -+ | Variable_name | Value |

+ -+ -+ | query_cache_size | 41984 |

+ -+ -+

## 7、query_cache_type的设置mysql> SET SESSION query_cache_type = ON;

如果查询缓存⼤⼩设置为⼤于0，query_cache_type变量影响其⼯作⽅式。这个变量可以设置为下⾯ 的值：

- 0或OF：将阻⽌缓存或查询缓存结果。
- 1或ON：将允许缓存，以SELECT SQL_NO_CACHE开始的查询语句除外。
- 2或DEMAND：仅对以SELECT SQL_CACHE开始的那些查询语句启⽤缓存。 设置query_cache_type变量的GLOBAL值将决定更改后所有连接客户端的缓存⾏为。具体客户端可以 通过设置query_cache_type变量的会话值控制它们本身连接的缓存⾏为。 例如，⼀个客户可以禁⽤⾃⼰的查询缓存，⽅法如下： mysql> SET SESION query_cache_type = OF;


## 8、设置缓存结果的最⼤值最⼩值SET GLOBAL query_cache_limit=10485760;#10MSET GLOBAL query_cache_min_res_unit=41984;

要控制可以被缓存的具体查询结果的最⼤值，应设置query_cache_limit变量。 默认值是1MB。

当⼀个查询结果（返回给客户端的数据）从查询缓冲中提取期间，它在查询缓存中排序。因此，数据 通常不在⼤的数据块中处理。查询缓存根据数据排序要求分配数据块，因此，当⼀个数据块⽤完后分 配⼀个新的数据块。因为内存分配操作是昂贵的(费时的)，所以通过query_cache_min_res_unit系统 变量给查询缓存分配最⼩值。当查询执⾏时，最新的结果数据块根据实际数据⼤⼩来确定，因此可以 释放不使⽤的内存。根据你的服务器执⾏查询的类型，你会发现调整query_cache_min_res_unit变量 的值是有⽤的： · query_cache_min_res_unit默认值是4KB。这应该适合⼤部分情况。 · 如果你有⼤量返回⼩结果数据的查询，默认数据块⼤⼩可能会导致内存碎⽚，显示为⼤量空闲内 存块。由于缺少内存，内存碎⽚会强制查询缓存从缓存内存中修整（删除）查询。这时，你应该减少 query_cache_min_res_unit变量的值。空闲块和由于修整⽽移出的查询的数量通过 Qcache_fre_blocks和Qcache_lowmem_prunes变量的值给出。 · 如果⼤量查询返回⼤结果（检查 Qcache_total_blocks和Qcache_queries_in_cache状态变 量），你可以通过增加query_cache_min_res_unit变量的值来提⾼性能。但是，注意不要使它变得太 ⼤（参⻅前⾯的条⽬）。

## 9、查询⾼速缓冲状态和维护

可以使⽤下⾯的语句检查MySQL服务器是否提供查询缓存功能： mysql> SHOW VARIABLES LIKE 'have_query_cache';

+ -+ -+ | Variable_name | Value |

+ -+ -+ | have_query_cache | YES |

+ -+ -+ FLUSH QUERY CACHE：语句来清理查询缓存碎⽚以提⾼内存使⽤性能。该语句不从缓存中移出任何 查询。 RESET QUERY CACHE：语句从查询缓存中移出所有查询。FLUSH TABLES语句也执⾏同样的⼯作。 SHOW STATUS：为了监视查询缓存性能，使⽤SHOW STATUS查看缓存状态变量，例如： mysql> SHOW STATUS LIKE 'Qcache%';

1 +-------------------------+--------+

1 |变量名 |值 |

1 +-------------------------+--------+

1 | Qcache_free_blocks | 36 |

1 | Qcache_free_memory | 138488 |

1 | Qcache_hits | 79570 |

1 | Qcache_inserts | 27087 |

1 | Qcache_lowmem_prunes | 3114 |

1 | Qcache_not_cached | 22989 |

1 | Qcache_queries_in_cache | 415 |

1 | Qcache_total_blocks | 912 |

1 +-------------------------+--------+

### 5.3.4节，“服务器状态变量”

这些变量的描述⻅ 。这⾥描述它们的⼀些应⽤。 SELECT查询的总数量等价于：

1 Com_select

1 + Qcache_hits

1 + queries with errors found by parser

Com_select的值等价于：

1 Qcache_inserts

1 + Qcache_not_cached

1 + queries with errors found during columns/rights check

查询缓存使⽤⻓度可变块，因此Qcache_total_blocks和Qcache_fre_blocks可以显示查询缓存内存碎 ⽚。执⾏FLUSH QUERY CACHE后，只保留⼀个空闲块。 每个缓存查询⾄少需要两个块（⼀个块⽤于查询⽂本，⼀个或多个块⽤于查询结果）。并且，每⼀个 查询使⽤的每个表需要⼀个块。但是，如果两个或多个查询使⽤相同的表，仅需要分配⼀个块。 Qcache_lowmem_prunes状态变量提供的信息能够帮助你你调整查询缓存的⼤⼩。它计算为了缓存新 的查询⽽从查询缓冲区中移出到⾃由内存中的查询的数⽬。查询缓冲区使⽤最近最少使⽤(LRU)策略来 确定哪些查询从缓冲区中移出。调整信息在 中给出。

### 5.13.3节，“查询⾼速缓冲配置”

