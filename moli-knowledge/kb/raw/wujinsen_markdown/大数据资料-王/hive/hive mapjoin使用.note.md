今天遇到⼀个hive的问题，如下hive sql： select f.a,f.b from A t join B f on ( f.a=t.a and f.ftime=20110802) 该语句中B表有30亿⾏记录，A表只有100⾏记录，⽽且B表中数据倾斜特别严重，有⼀个key上有15亿⾏记录，在运⾏过程中特别的慢，⽽ 且在reduece的过程中遇有内存不够⽽报错。

为了解决⽤户的这个问题，考虑使⽤mapjoin,mapjoin的原理： MAPJION会把⼩表全部读⼊内存中，在map阶段直接拿另外⼀个表的数据和内存中表数据做匹配，由于在map是进⾏了join操作， 省去了reduce运⾏的效率也会⾼很多

这样就不会由于数据倾斜导致某个reduce上落数据太多⽽失败。于是原来的sql可以通过使⽤hint的⽅ 式指定join时使⽤mapjoin。

select /*+ mapjoin(A)*/ f.a,f.b from A t join B f on ( f.a=t.a and f.ftime=20110802)

再运⾏发现执⾏的效率⽐以前的写法⾼了好多。

mapjoin还有⼀个很⼤的好处是能够进⾏不等连接的join操作，如果将不等条件写在where中，那么 mapreduce过程中会进⾏笛卡尔积，运⾏效率特别低，如果使⽤mapjoin操作，在map的过程中就完成了 不等值的join操作，效率会⾼很多。 例⼦：

select A.a ,A.b from A join B where A.a>B.a

简单总结⼀下，mapjoin的使⽤场景：

- 1. 关联操作中有⼀张表⾮常⼩
- 2.不等值的链接操作


