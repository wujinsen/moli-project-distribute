⼀、场景: SQL: select ip from test order by ip desc 数据量：1亿多条

⼆、问题分析排查： 分区问题： 分区不能太多也不能太少，正常按⽇期day分区即可 主键排序键问题： 主键是user_id，排序键user_id, ip

- 1 CREATE TABLE test.test

- 2 (

- 3 `user_id` Int64 COMMENT '⽤户id',

- 4 `ip` String COMMENT 'ip地址'

- 5 )

ENGINE = ReplicatedMergeTree('/clickhouse/tables/{shard}/test.test', '{replica}')

- 6

- 7 PARTITION BY toDate(create_time)

- 8 PRIMARY KEY user_id

- 9 ORDER BY (user_id, ip)

- 10 SETTINGS index_granularity = 8192


三、问题解决 正常排序键查询: 毫秒级 select user_id, ip from test order by user_id, ip desc 不按排序键查询会很慢: select ip from test order by ip desc

查询分区数量SQL：

- 1 SELECT database,

- 2 table,

- 3 count() AS parts,

- 4 uniq(partition) AS partitions,

- 5 sum(marks) AS marks,

- 6 sum(rows) AS rows,

- 7 formatReadableSize(sum(data_compressed_bytes)) AS compressed,

- 8 formatReadableSize(sum(data_uncompressed_bytes)) AS uncompressed,

round((sum(data_compressed_bytes) / sum(data_uncompressed_bytes)) * 100.,2) AS percentage

- 9

- 10 FROM system.parts

- 11 WHERE active

- 12 and database = 'database'

- 13 and table = 'user_action_sequence_data4'

- 14 GROUP BY database, table


