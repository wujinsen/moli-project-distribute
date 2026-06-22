. cascade⽅式 在⽗表上update/delete记录时，同步update/delete掉⼦表的匹配记录

. set null⽅式 在⽗表上update/delete记录时，将⼦表上匹配记录的列设为null 要注意⼦表的外键列不能为not null

. No action⽅式 如果⼦表中有匹配的记录,则不允许对⽗表对应候选键进⾏update/delete操作

. Restrict⽅式 同no action, 都是⽴即检查外键约束

. Set default⽅式 ⽗表有变更时,⼦表将外键列设置成⼀个默认的值 但Innodb不能识别

