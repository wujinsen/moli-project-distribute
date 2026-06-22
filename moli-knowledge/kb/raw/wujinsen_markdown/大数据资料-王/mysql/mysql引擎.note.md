%40%2Csz%401320_20 1%2Cta%40iphone_1_8.0_3_60/baiduid=B8B0E3525FD48F3FE3DCBCB0C8B/w=0_10_my sql引擎/t=iphone/l=3/tc? ref= w_iphone&lid=10203560262404231353&order=1&vit=osres&tj= w_normal_1_0_10&m=8 &srd=1&cltj=cloud_title&dict=20&fm=wnor_p1&sec=4279&di=31ce520f7b7a57e&bdenc=1&nsr c=IlPT2AEptyoA_yixCFOxXnANedT62v3IGx3PMiJR0zu5mla7gbrmF2FzZ8wL8LTUS4ruG72xBt8x H_f_WAl

htp:/m.baidu.com/from=84b/bd_page_type=1/sid=0/uid=0/pu=usm

MySQL常⽤的存储引擎为MyISAM、InnoDB、MEMORY、MERGE，其中InnoDB提供事 务安全表，其他存储引擎都是⾮事务安全表。

MyISAM是MySQL的默认存储引擎。MyISAM不⽀持事务、也不⽀持外键，但其访问速度 快，对事务完整性没有要求。 innoDB存储引擎提供了具有提交、回滚和崩溃恢复能⼒的事务安全。但是⽐起MyISAM存 储引擎，InnoDB写的处理效率差⼀些并且会占⽤更多的磁盘空间以保留数据和索引 MEMORY存储引擎使⽤存在内存中的内容来创建表。每个MEMORY表只实际对应⼀个磁 盘⽂件。 MEMORY类型的表访问⾮常得快，因为它的数据是放在内存中的，并且默认使⽤HASH索 引。但是⼀旦服务关闭，表中的数据就会丢失掉。 MERGE存储引擎是⼀组MyISAM表的组合，这些MyISAM表必须结构完全相同。MERGE表 本身没有数据，对MERGE类型的表进⾏查询、更新、删除的操作，就是对内部的MyISAM 表进⾏的。

