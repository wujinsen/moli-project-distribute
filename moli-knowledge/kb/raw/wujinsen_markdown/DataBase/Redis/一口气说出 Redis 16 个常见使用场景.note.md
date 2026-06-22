htps:/mp.weixin.q.com/s/jAsZtRM1YhIvqmHLV-Nj-Q

⽬录

缓存

数据共享分布式

分布式锁

全局ID

计数器

限流

位统计 购物⻋ ⽤户消息时间线timeline

消息队列

抽奖

点赞、签到、打卡

商品标签 商品筛选 ⽤户关注、推荐模型

排⾏榜

- 1、缓存 String类型 例如：热点数据缓存（例如报表、明星出轨），对象缓存、全⻚缓存、可以提升热点数据的访问数 据。
- 2、数据共享分布式 String 类型，因为 Redis 是分布式的独⽴服务，可以在多个应⽤之间共享 例如：分布式Sesion <dependency>

<groupId>org.springframework.sesion</groupId> <artifactId>spring-sesion-data-redis</artifactId>

</dependency>

- 3、分布式锁 String 类型setnx⽅法，只有不存在时才能添加成功，返回true publicstaticboleangetLock(String key) {


Long flag = jedis.setnx(key,"1"); if (flag = 1) {

jedis.expire(key, 10);

} return flag = 1;

}

publicstaticvoidreleaseLock(String key) {

jedis.del(key); }

- 4、全局ID int类型，incrby，利⽤原⼦性 incrby userid 1 0 分库分表的场景，⼀次性拿⼀段
- 5、计数器 int类型，incr⽅法 例如：⽂章的阅读量、微博点赞数、允许⼀定的延迟，先写⼊Redis再定时同步到数据库
- 6、限流 int类型，incr⽅法 以访问者的ip和其他信息作为key，访问⼀次增加⼀次计数，超过次数则返回false
- 7、位统计 String类型的bitcount（1.6.6的bitmap数据结构介绍） 字符是以8位⼆进制存储的 set k1 a


- setbit k1 6 1

- setbit k1 7 0 get k1 /* 6 7 代表的a的⼆进制位的修改


- a 对应的ASCI码是97，转换为⼆进制数据是01 01

- b 对应的ASCI码是98，转换为⼆进制数据是01 010


因为bit⾮常节省空间（1 MB=838608 bit），可以⽤来做⼤数据量的统计。

*/ 例如：在线⽤户统计，留存⽤户统计

setbit onlineusers 01 setbit onlineusers1 setbit onlineusers 20

⽀持按位与、按位或等等操作 BITOPANDdestkeykey[key.] ，对⼀个或多个 key 求逻辑并，并将结果保存到 destkey 。 BITOPORdestkeykey[key.] ，对⼀个或多个 key 求逻辑或，并将结果保存到 destkey 。 BITOPXORdestkeykey[key.] ，对⼀个或多个 key 求逻辑异或，并将结果保存到 destkey 。 BITOPNOTdestkeykey ，对给定 key 求逻辑⾮，并将结果保存到 destkey 。 计算出7天都在线的⽤户 BITOP"AND""7_days_both_online_users""day_1_online_users""day_2_online_users". "day_7_o nline_users"

- 8、购物⻋ String 或hash。所有String可以做的hash都可以做


![image 1](<一口气说出 Redis 16 个常见使用场景.note_images/imageFile1.png>)

key：⽤户id；field：商品id；value：商品数量。

+1：hincr。-1：hdecr。删除：hdel。全选：hgetal。商品数：hlen。

- 9、⽤户消息时间线timeline list，双向链表，直接作为timeline就好了。插⼊有序
- 10、消息队列 List提供了两个阻塞的弹出操作：blpop/brpop，可以设置超时时间


blpop：blpop key1 timeout 移除并获取列表的第⼀个元素，如果列表没有元素会阻塞列表直到等待 超时或发现可弹出元素为⽌。

brpop：brpop key1 timeout 移除并获取列表的最后⼀个元素，如果列表没有元素会阻塞列表直到 等待超时或发现可弹出元素为⽌。

上⾯的操作。其实就是java的阻塞队列。学习的东⻄越多。学习成本越低

队列：先进先除：rpush blpop，左头右尾，右边进⼊队列，左边出队列

栈：先进后出：rpush brpop

1、抽奖 ⾃带⼀个随机获得值 spop myset

- 12、点赞、签到、打卡

假如上⾯的微博ID是t101，⽤户ID是u301 ⽤ like:t101 来维护 t101 这条微博的所有点赞⽤户

是不是⽐数据库简单多了。

- 13、商品标签

⽼规矩，⽤ tags:i501 来维护商品所有的标签。

- 14、商品筛选 / 获取差集


![image 2](<一口气说出 Redis 16 个常见使用场景.note_images/imageFile2.png>)

点赞了这条微博：sad like:t101 u301

取消点赞：srem like:t101 u301

是否点赞：sismember like:t101 u301

点赞的所有⽤户：smembers like:t101

点赞数：scard like:t101

![image 3](<一口气说出 Redis 16 个常见使用场景.note_images/imageFile3.png>)

sad tags:i501 画⾯清晰细腻

sad tags:i501 真彩清晰显示屏

sad tags:i501 流程⾄极

sdif set1 set2

/ 获取交集（intersection ） sinter set1 set2

/ 获取并集 sunion set1 set2

![image 4](<一口气说出 Redis 16 个常见使用场景.note_images/imageFile4.png>)

假如：iPhone1 上市了 sad brand:aple iPhone1

sad brand:ios iPhone1

sad scrensize:6.0-6.24 iPhone1

sad screntype:lcd iPhone1 赛选商品，苹果的、ios的、屏幕在6.0-6.24之间的，屏幕材质是LCD屏幕 sinter brand:aple brand:ios scrensize:6.0-6.24 screntype:lcd

- 15、⽤户关注、推荐模型 folow 关注 fans 粉丝 相互关注：

我关注的⼈也关注了他(取交集)：

可能认识的⼈：

- 16、排⾏榜


- sad 1:folow 2

- sad 2:fans 1


- sad 1:fans 2

- sad 2:folow 1


sinter 1:folow 2:fans

- ⽤户1可能认识的⼈(差集)：sdif 2:folow 1:folow

- ⽤户2可能认识的⼈：sdif 1:folow 2:folow


id 为601 的新闻点击数加1：

zincrby hotNews:20190926 1 n6001

获取今天点击最多的15条：

zrevrange hotNews:20190926 0 15 withscores

![image 5](<一口气说出 Redis 16 个常见使用场景.note_images/imageFile5.png>)

# 地址：https://blog.csdn.net/weixin_43878826/article/details/119461093

