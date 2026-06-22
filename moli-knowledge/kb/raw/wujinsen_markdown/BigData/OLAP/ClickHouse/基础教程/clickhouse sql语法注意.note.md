clickhouse早起版本不提供update和delete操作

更新限制 索引列不能进⾏更新 分布式表不能进⾏更新 该命令是异步执⾏的，可以通过查看表 system.mutations 来查看命令的是否执⾏完毕

update不带where条件会报错

