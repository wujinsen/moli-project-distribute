---
title: Sqoop 数据导出：全量、增量、更新.note（原文插图 annex）
slug: annex-Sqoop-数据导出：全量、增量、更新
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/BigData/Sqoop/Sqoop 数据导出：全量、增量、更新.note.md
related: [数据采集与-etl-工具选型]
created: 2026-07-05
updated: 2026-07-05
---

htps:/blog.csdn.net/ q_3549539/article/details/95620740

# 背景信息

SQOP⽀持直接从Hive表到RDBMS表的导出操作，也⽀持HDFS到RDBMS表的操作， 当前需求是从Hive中导出数据到RDBMS，有如下两种⽅案： Ø 从Hive表到RDBMS表的直接导出： 该种⽅式效率较⾼，但是此时相当于直接在Hive表与RDBMS表的数据之间做全量、增量和更新对⽐， 当Hive表记录较⼤时，或者RDBMS有多个分区表时，⽆法做精细的控制，因此暂时不考虑该⽅案。 Ø 从HDFS到RDBMS表的导出： 该⽅式下需要先将数据从Hive表导出到HDFS，再从HDFS将数据导⼊到RDBMS。虽然⽐直接导出多了 ⼀步操作，但是可以实现对数据的更精准的操作，特别是在从Hive表导出到HDFS时，可以进⼀步对数 据进⾏字段筛选、字段加⼯、数据过滤操作，从⽽使得HDFS上的数据更“接近”或等于将来实际要导⼊ RDBMS表的数据。在从HDFS导⼊RDBMS时，也是将⼀个“⼩数据集”与⽬标表中的数据做对⽐，会提 ⾼导出速度。示意图如下所示：

![image 1](assets/imageFile1.png)

# 不同导出模式介绍

全量导出

Ø 应⽤场景：将Hive表中的全部记录（可以是全部字段也可以部分字段）导出到⽬标表。 Ø 实现逻辑：

![image 2](assets/imageFile2.png)

Ø 使⽤限制：⽬标表中不能有与Hive中相同的记录，⼀般只有当⽬标表为空表时才使⽤该模式进⾏⾸ 次数据导出。 Ø 参数：源表、⽬标表、导出字段（select的字段）、映射关系（–column后的参数） Ø 适⽤的数据库：Oracle、DB2、SQL Server、PG、MySQL

### 增量导出

Ø 应⽤场景：将Hive表中的增量记录以及有修改的记录同步到⽬标表中。 Ø 实现逻辑：

![image 3](assets/imageFile3.png)

Ø 使⽤限制：update-key可以是多个字段，但这些字段的记录都应该是未被更新过的，若该参数指定 的字段有更新，则对应记录的更新不会被同步到⽬标表中。 Ø 参数：源表、⽬标表、筛选字段及其取值范围、导出字段（select的字段）、映射关系（–column后 的参数）、更新的参考字段（–update-key后的参数） Ø 适⽤的数据库：Oracle、SQL Server、MySQL

### 更新导出

Ø 应⽤场景：将Hive表中的有更新的记录同步到⽬标表。 Ø 实现逻辑：

![image 4](assets/imageFile4.png)

Ø 使⽤限制：update-key可以是多个字段，但这些字段的记录都应该是未被更新过的，若该参数指定 的字段有更新，则对应记录的更新不会被同步到⽬标表中。 Ø 参数：源表、⽬标表、筛选字段及其取值范围、导出字段（select的字段）、映射关系（–column后 的参数）、更新的参考字段（–update-key后的参数） Ø 适⽤的数据库：Oracle、DB2、SQL Server、PG、MySQL

## 相关脚本

全量导出 HQL示例：insert overwrite directory ‘/user/rot/export/testʼ row format delimited fields terminated by ‘,ʼ STORED AS textfile select F1,F2,F3 from <sourceHiveTable>;

SQOP脚本：sqop export -conect jdbc:mysql:/localhost: 306/wht -username rot pasword cloudera-table <targetTable>-fields-terminated-by ',' -columns F1,F2,F3-exportdir /user/rot/export/test

增量导出（insert模式） HQL示例：insert overwrite directory ‘/user/rot/export/testʼ row format delimited fields terminated by ‘,ʼ STORED AS textfile select F1,F2,F3 from <sourceHiveTable> where <condition>;

SQOP脚本：sqop export -conect jdbc:mysql:/localhost: 306/wht -username rot pasword cloudera-table <targetTable>-fields-terminated-by ‘,ʼ -columns F1,F2,F3-updatekey F4-update-mode alowinsert -export-dir /user/rot/export/test

更新导出（update模式） HQL示例：insert overwrite directory ‘/user/rot/export/testʼ row format delimited fields terminated by ‘,ʼ STORED AS textfile select F1,F2,F3 from <sourceHiveTable> where <condition>;

SQOP脚本：sqop export -conect jdbc:mysql:/localhost: 306/wht -username rot pasword cloudera-table <targetTable>-fields-terminated-by ‘,ʼ -columns F1,F2,F3-updatekey F4-update-mode updateonly-export-dir /user/rot/export/test
