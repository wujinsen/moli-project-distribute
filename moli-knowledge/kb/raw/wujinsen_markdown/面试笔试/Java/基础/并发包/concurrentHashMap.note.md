concurentHashMap由Senment数组结构和HashEntry数组结构组成.

get过程不需要加锁，读到空值才加锁。因为get使⽤的共享变量都是⽤volatile定义。 根据java内存模型(J M)，volatile关键字写⼊操作优先于读取操作

