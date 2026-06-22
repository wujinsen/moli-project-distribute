两个表join 连接，去掉重复的数据

-------distinct 去重复查询 select * from accounts acc join (select distinct accid from roles) r on r.accid=acc.ID

-----不需要distinct select * from (select MAX(ID)roleid,accid from roles group by accid) rr join (select * from accounts) acc on acc.ID=rr.accid

--------解释⼀下不⽤distinct 去重复查询语句 Select * from (Select max(不重复的字段就⾏) as roleid,要去重复字段名 From 数据表 Where 条件 Group by 要去重复字段名) as A join 数据库表 on 条件

