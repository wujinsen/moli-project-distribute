---
title: mysql 同一张表查询 left join.note（原文插图 annex）
slug: annex-mysql-同一张表查询-left-join
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/DataBase/mysql/mysql 同一张表查询 left join.note.md
related: [mysql-索引面试题]
created: 2026-07-05
updated: 2026-07-05
---

今天群⾥有同学发了⼀个题: ⼀张表,如图

![image 1](assets/imageFile1.png)

需要写⼀个sql ，输出如下结果

![image 2](assets/imageFile2.png)

对这个表进⾏⼀下简单解释，其实就是省市区的关系，放在了同⼀张表中，level=1表示省, level=2表 示市,level=3表示区 code是他们进⾏关系的⼀种表现。

就利⽤code做⽂章

sql如下: SELECT IF(t.name=t1.name,t.name,IF(t1.name=t2.name,CONCAT(t.name,"",t1.name),CONCAT(t.name,"-",t1.name,"-",t2.name)FROM Test t LEFT JOIN Test t1 ON(t1.codet.code)<=9 0 AND (t1.code-t.code)>=1 0 AND (t1.code-t.code)%1 0=0) OR t1.code-t.code=0 LEFT JOIN Test t2ON(t2.code-t1.code)<1 0 AND (t2.code-t1.code)>0) OR t2.code-t1.code=0 WHERE t.level=1

这个sql肯定需要left join 连表，因为需要3个字段，所以连3次表，利⽤好code直接的关系，但是不要 忘记code相等的情况 code间的关系，是省市的前缀是⼀样的，市区的前缀是⼀样的，⾪属关系就这样判断: 省市:(t1.code-t.code)<=9000 AND (t1.code-t.code)>=1000 AND (t1.code-t.code)%1000=0 市区:(t2.code-t1.code)<1000 AND (t2.code-t1.code)>0

⾸先我先写了这样的sql： SELECT t.name,t1.name,t2.name FROM Test t LEFT JOIN Test t1 ON(t1.code-t.code)<=9 0 AND (t1.code-t.code)>=1 0 AND (t1.code-t.code)%1 0=0) OR t1.code-t.code=0 LEFT JOIN Test t2 ON(t2.code-t1.code)<1 0 AND (t2.code-t1.code)>0) OR t2.code-t1.code=0 WHERE t.level=1

结果如图:

![image 3](assets/imageFile3.png)

看到这个结果，只需要在select 中使⽤IF函数进⾏判断了，结果就出来了
