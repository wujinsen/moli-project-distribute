- 1、连接操作相关的命令

quit：关闭连接（conection）

auth：简单密码认证

- 2、对value操作的命令


exists(key)：确认⼀个key是否存在

del(key)：删除⼀个key

type(key)：返回值的类型

keys(patern)：返回满⾜给定patern的所有key

randomkey：随机返回key空间的⼀个key

rename(oldname, newname)：将key由oldname重命名为newname，若newname存在则删除 newname表示的key

dbsize：返回当前数据库中key的数⽬

expire：设定⼀个key的活动时间（s）

tl：获得⼀个key的活动时间

select(index)：按索引查询

move(key, dbindex)：将当前数据库中的key转移到有dbindex索引的数据库

flushdb：删除当前选择数据库中的所有key

flushal：删除所有数据库中的所有key

- 3、对String操作的命令


APEND key value 如果该Key已经存在，APEND命令将参数Value的数据追加到已存在 Value的末尾。如果该Key不存在，APEND命令将会创建⼀个新的Key/Value。 追加后Value的⻓ 度。

DECR key 将指定Key的Value原⼦性的递减1。如果该Key不存在，其初始值为0，在 decr之后其值为-1。如果Value的值不能转换为整型值，如Helo，该操作将执⾏失败并返回相应的 错误信息。注意：该操作的取值范围是64位有符号整型。 递减后的Value值。

INCR key 将指定Key的Value原⼦性的递增1。如果该Key不存在，其初始值为0，在incr 之后其值为1。如果Value的值不能转换为整型值，如Helo，该操作将执⾏失败并返回相应的错误信 息。注意：该操作的取值范围是64位有符号整型。 递增后的Value值。

DECRBY key decrement 将指定Key的Value原⼦性的减少decrement。如果该Key不存在，其 初始值为0，在decrby之后其值为-decrement。如果Value的值不能转换为整型值，如Helo，该操 作将执⾏失败并返回相应的错误信息。注意：该操作的取值范围是64位有符号整型。 减少后的 Value值。

INCRBY key increment 将指定Key的Value原⼦性的增加increment。如果该Key不存在，其初 始值为0，在incrby之后其值为increment。如果Value的值不能转换为整型值，如Helo，该操作将 执⾏失败并返回相应的错误信息。注意：该操作的取值范围是64位有符号整型。 增加后的Value 值。

GET key 获取指定Key的Value。如果与该Key关联的Value不是string类型，Redis将返 回错误信息，因为GET命令只能⽤于获取string Value。 与该Key相关的Value，如果该Key不存 在，返回nil。

SET key value 设定该Key持有指定的字符串Value，如果该Key已经存在，则覆盖其原有 值。 总是返回"OK"。

GETSET key value 原⼦性的设置该Key为指定的Value，同时返回该Key的原有值。和GET命 令⼀样，该命令也只能处理string Value，否则Redis将给出相关的错误信息。 返回该Key的原有 值，如果该Key之前并不存在，则返回nil。

STRLEN key 返回指定Key的字符值⻓度，如果Value不是string类型，Redis将执⾏失败 并给出相关的错误信息。 返回指定Key的Value字符⻓度，如果该Key不存在，返回0。

SETEX key seconds value 原⼦性完成两个操作，⼀是设置该Key的值为指定字符串，同时设 置该Key在Redis服务器中的存活时间(秒数)。该命令主要应⽤于Redis被当做Cache服务器使⽤时。

SETNX key value 如果指定的Key不存在，则设定该Key持有指定字符串Value，此时其效果 等价于SET命令。相反，如果该Key已经存在，该命令将不做任何操作并返回。 1表示设置成功， 否则0。

SETRANGE key ofset value 替换指定Key的部分字符串值。从ofset开始，替换的⻓度为该命令 第三个参数value的字符串⻓度，其中如果ofset的值⼤于该Key的原有值Value的字符串⻓度， Redis将会在Value的后⾯补⻬(ofset - strlen(value)数量的0x0，之后再追加新值。如果该键不存 在，该命令会将其原值的⻓度假设为0，并在其后添补ofset个0x0后再追加新值。鉴于字符串 Value的最⼤⻓度为512M，因此ofset的最⼤值为53687091。最后需要注意的是，如果该命令在 执⾏时致使指定Key的原有值⻓度增加，这将会导致Redis重新分配⾜够的内存以容纳替换后的全部 字符串，因此就会带来⼀定的性能折损。 修改后的字符串Value⻓度。

GETRANGE key start end 如果截取的字符串⻓度很短，我们可以该命令的时间复杂度视为 O(1)，否则就是O(N)，这⾥N表示截取的⼦字符串⻓度。该命令在截取⼦字符串时，将以闭区间的 ⽅式同时包含start(0表示第⼀个字符)和end所在的字符，如果end值超过Value的字符⻓度，该命令 将只是截取从start开始之后所有的字符数据。 ⼦字符串 取整个字符串，可以将start和end定 为 0 , -1

SETBIT key ofset value 设置在指定Ofset上BIT的值，该值只能为1或0，在设定后该命令返回该 Ofset上原有的BIT值。如果指定Key不存在，该命令将创建⼀个新值，并在指定的Ofset上设定参 数中的BIT值。如果Ofset⼤于Value的字符⻓度，Redis将拉⻓Value值并在指定Ofset上设置参数 中的BIT值，中间添加的BIT值为0。最后需要说明的是Ofset值必须⼤于0。 在指定Ofset上的 BIT原有值。

GETBIT key ofset 返回在指定Ofset上BIT的值，0或1。如果Ofset超过string value的⻓ 度，该命令将返回0，所以对于空字符串始终返回0。 在指定Ofset上的BIT值。 (布隆过滤器)

MGET key [key.] 返回所有指定Keys的Values，如果其中某个Key不存在，或者其值不 为string类型，该Key的Value将返回nil。 返回⼀组指定Keys的Values的列表。

MSET key value [key value.] 该命令原⼦性的完成参数中所有key/value的设置操作，其具体 ⾏为可以看成是多次迭代执⾏SET命令。 该命令不会失败，始终返回OK。

MSETNX key value [key value.] 该命令原⼦性的完成参数中所有key/value的设置操作，其具体 ⾏为可以看成是多次迭代执⾏SETNX命令。然⽽这⾥需要明确说明的是，如果在这⼀批Keys中有任 意⼀个Key已经存在了，那么该操作将全部回滚，即所有的修改都不会⽣效。 1表示所有Keys都设 置成功，0则表示没有任何Key被修改。

- 4、对List操作的命令


LPUSH key value [value.] 在指定Key所关联的List Value的头部插⼊参数中给出的所有 Values。如果该Key不存在，该命令将在插⼊之前创建⼀个与该Key关联的空链表，之后再将数据从 链表的头部插⼊。如果该键的Value不是链表类型，该命令将返回相关的错误信息。 插⼊后链表 中元素的数量。

LPUSHX key value 仅有当参数中指定的Key存在时，该命令才会在其所关联的List Value的头部 插⼊参数中给出的Value，否则将不会有任何操作发⽣。 插⼊后链表中元素的数量。

LRANGE key start stop 该命令的参数start和end都是0-based。即0表示链表头部(leftmost)的 第⼀个元素。其中start的值也可以为负值，-1将表示链表中的最后⼀个元素，即尾部元素，-2表示 倒数第⼆个并以此类推。该命令在获取元素时，start和end位置上的元素也会被取出。如果start的 值⼤于链表中元素的数量，空链表将会被返回。如果end的值⼤于元素的数量，该命令则获取从 start(包括start)开始，链表中剩余的所有元素。 返回指定范围内元素的列表。

LPOP key 返回并弹出指定Key关联的链表中的第⼀个元素，即头部元素，。如果该Key不存，返 回nil。 链表头部的元素。

LEN key 返回指定Key关联的链表中元素的数量，如果该Key不存在，则返回0。如果与该Key关 联的Value的类型不是链表，则返回相关的错误信息。 链表中元素的数量。

LREM key count value 在指定Key关联的链表中，删除前count个值等于value的元素。如果count ⼤于0，从头向尾遍历并删除，如果count⼩于0，则从尾向头遍历并删除。如果count等于0，则删 除链表中所有等于value的元素。如果指定的Key不存在，则直接返回0。 返回被删除的元素数 量。

LSET key index value 设定链表中指定位置的值为新值，其中0表示第⼀个元素，即头部元 素，-1表示尾部元素。如果索引值Index超出了链表中元素的数量范围，该命令将返回相关的错误信 息。

(对指定脚标的值进⾏设置)

LINDEX key index 该命令将返回链表中指定位置(index)的元素，index是0-based，表示头部元 素，如果index为-1，表示尾部元素。如果与该Key关联的不是链表，该命令将返回相关的错误信 息。 返回请求的元素，如果index超出范围，则返回nil。

(读出指定脚标的值)

LTRIM key start end 该命令将仅保留指定范围内的元素，从⽽保证链接中的元素数量相对恒定。 start和stop参数都是0-based，0表示头部元素。和其他命令⼀样，start和stop也可以为负值，-1表 示尾部元素。如果start⼤于链表的尾部，或start⼤于stop，该命令不错报错，⽽是返回⼀个空的链 表，与此同时该Key也将被删除。如果stop⼤于元素的数量，则保留从start开始剩余的所有元素。

LINSERT key BEFORE|AFTER pivot value 该命令的功能是在pivot元素的前⾯或后⾯插⼊参数中的 元素value。如果Key不存在，该命令将不执⾏任何操作。如果与Key关联的Value类型不是链表，相 关的错误信息将被返回。 成功插⼊后链表中元素的数量，如果没有找到pivot，返回-1，如果key 不存在，返回0。

(在指定的某个value前或后插⼊⼀个新的value)

RPUSH key value [value.] 在指定Key所关联的List Value的尾部插⼊参数中给出的所有 Values。如果该Key不存在，该命令将在插⼊之前创建⼀个与该Key关联的空链表，之后再将数据从 链表的尾部插⼊。如果该键的Value不是链表类型，该命令将返回相关的错误信息。 插⼊后链表 中元素的数量。

RPUSHX key value 仅有当参数中指定的Key存在时，该命令才会在其所关联的List Value的尾部 插⼊参数中给出的Value，否则将不会有任何操作发⽣。 插⼊后链表中元素的数量。

RPOP key 返回并弹出指定Key关联的链表中的最后⼀个元素，即尾部元素，。如果该Key不存， 返回nil。 链表尾部的元素。

RPOPLPUSH source destination 原⼦性的从与source键关联的链表尾部弹出⼀个元素，同时再 将弹出的元素插⼊到与destination键关联的链表的头部。如果source键不存在，该命令将返回nil， 同时不再做任何其它的操作了。如果source和destination是同⼀个键，则相当于原⼦性的将其关联 链表中的尾部元素移到该链表的头部。 返回弹出和插⼊的元素。

- 5、对Map操作的命令


HSET key field value 为指定的Key设定Field/Value对，如果Key不存在，该命令将创建新Key以参 数中的Field/Value对，如果参数中的Field在该Key中已经存在，则⽤新值覆盖其原有值。 1表示新 的Field被设置了新值，0表示Field已经存在，⽤新值覆盖原有值。

HGET key field 返回指定Key中指定Field的关联值。 返回参数中Field的关联值，如果参数中的 Key或Field不存，返回nil。

HEXISTS key field 判断指定Key中的指定Field是否存在。 1表示存在，0表示参数中的Field或 Key不存在。

HLEN key 获取该Key所包含的Field的数量。 返回Key包含的Field数量，如果Key不存在，返回 0。

HDEL key field [field.] 从指定Key的Hashes Value中删除参数中指定的多个字段，如果不存在 的字段将被忽略。如果Key不存在，则将其视为空Hashes，并返回0. 实际删除的Field数量。

HSETNX key field value 只有当参数中的Key或Field不存在的情况下，为指定的Key设定 Field/Value对，否则该命令不会进⾏任何操作。 1表示新的Field被设置了新值，0表示Key或Field 已经存在，该命令没有进⾏任何操作。

HINCRBY key field increment 增加指定Key中指定Field关联的Value的值。如果Key或Field不存 在，该命令将会创建⼀个新Key或新Field，并将其关联的Value初始化为0，之后再指定数字增加的 操作。该命令⽀持的数字是64位有符号整型，即increment可以负数。 返回运算后的值。

HGETAL key 获取该键包含的所有Field/Value。其返回格式为⼀个Field、⼀个Value，并以此类 推。 Field/Value的列表。

HKEYS key 返回指定Key的所有Fields名。 Field的列表。

HVALS key 返回指定Key的所有Values名。 Value的列表。

HMGET key field [field.] 获取和参数中指定Fields关联的⼀组Values。如果请求的Field不存 在，其值返回nil。如果Key不存在，该命令将其视为空Hash，因此返回⼀组nil。 返回和请求 Fields关联的⼀组Values，其返回顺序等同于Fields的请求顺序。

HMSET key field value [field value.] 逐对依次设置参数中给出的Field/Value对。如果其中某个 Field已经存在，则⽤新值覆盖原有值。如果Key不存在，则创建新Key，同时设定参数中的 Field/Value。

- 6、对Set操作的命令


SAD key member [member.] 如果在插⼊的过程⽤，参数中有的成员在Set中已经存在，该成 员将被忽略，⽽其它成员仍将会被正常插⼊。如果执⾏该命令之前，该Key并不存在，该命令将会 创建⼀个新的Set，此后再将参数中的成员陆续插⼊。如果该Key的Value不是Set类型，该命令将返 回相关的错误信息。 本次操作实际插⼊的成员数量。

SCARD key 获取Set中成员的数量。 返回Set中成员的数量，如果该Key并不存在，返回0。

SISMEMBER key member 判断参数中指定成员是否已经存在于与Key相关联的Set集合中。 1表 示已经存在，0表示不存在，或该Key本身并不存在。

SMEMBERS key 获取与该Key关联的Set中所有的成员。返回Set中所有的成员。

SREM key member [member.] 从与Key关联的Set中删除参数中指定的成员，不存在的参数成 员将被忽略，如果该Key并不存在，将视为空Set处理。 从Set中实际移除的成员数量，如果没有 则返回0。

SRANDMEMBER key 和SPOP⼀样，随机的返回Set中的⼀个成员，不同的是该命令并不会删除 返回的成员。 返回随机位置的成员，如果Key不存在则返回nil。

SMOVE source destination member 原⼦性的将参数中的成员从source键移⼊到destination键所 关联的Set中。因此在某⼀时刻，该成员或者出现在source中，或者出现在destination中。如果该 成员在source中并不存在，该命令将不会再执⾏任何操作并返回0，否则，该成员将从source移⼊ 到destination。如果此时该成员已经在destination中存在，那么该命令仅是将该成员从source中移 出。如果和Key关联的Value不是Set，将返回相关的错误信息。 1表示正常移动，0表示source中 并不包含参数成员。

SDI F key [key.] 返回参数中第⼀个Key所关联的Set和其后所有Keys所关联的Sets中成员的差 异。如果Key不存在，则视为空Set。 差异结果成员的集合。

SDI FSTORE destination key [key.] 该命令和SDI F命令在功能上完全相同，两者之间唯⼀的 差别是SDI F返回差异的结果成员，⽽该命令将差异成员存储在destination关联的Set中。如果 destination键已经存在，该操作将覆盖它的成员。 返回差异成员的数量。

SINTER key [key.] 该命令将返回参数中所有Keys关联的Sets中成员的交集。因此如果参数中 任何⼀个Key关联的Set为空，或某⼀Key不存在，那么该命令的结果将为空集。 交集结果成员的 集合。

SINTERSTORE destination key [key.] 该命令和SINTER命令在功能上完全相同，两者之间唯⼀ 的差别是SINTER返回交集的结果成员，⽽该命令将交集成员存储在destination关联的Set中。如果 destination键已经存在，该操作将覆盖它的成员。 返回交集成员的数量。

SUNION key [key.] 该命令将返回参数中所有Keys关联的Sets中成员的并集。 并集结果成员 的集合。

SUNIONSTORE destination key [key.] 该命令和SUNION命令在功能上完全相同，两者之间唯 ⼀的差别是SUNION返回并集的结果成员，⽽该命令将并集成员存储在destination关联的Set中。如 果destination键已经存在，该操作将覆盖它的成员。 返回并集成员的数量。

- 7、对SortSet操作的命令


ZAD key score member [score] [member] 添加参数中指定的所有成员及其分数到指定key的 Sorted-Set中，在该命令中我们可以指定多组score/member作为参数。如果在添加时参数中的某 ⼀成员已经存在，该命令将更新此成员的分数为新值，同时再将该成员基于新值重新排序。如果键 不存在，该命令将为该键创建⼀个新的Sorted-Sets Value，并将score/member对插⼊其中。如果 该键已经存在，但是与其关联的Value不是Sorted-Sets类型，相关的错误信息将被返回。 本次操 作实际插⼊的成员数量。

ZCARD key 获取与该Key相关联的Sorted-Sets中包含的成员数量。 返回Sorted-Sets中的成员 数量，如果该Key不存在，返回0。

ZCOUNT key min max 该命令⽤于获取分数(score)在min和max之间的成员数量。针对min和 max参数需要额外说明的是，-inf和+inf分别表示Sorted-Sets中分数的最⾼值和最低值。缺省情况 下，min和max表示的范围是闭区间范围，即min <= score <= max内的成员将被返回。然⽽我们可 以通过在min和max的前⾯添加"("字符来表示开区间，如(min max表示min < score <= max，⽽ (min (max表示min < score < max。 分数指定范围内成员的数量。

ZINCRBY key increment member 该命令将为指定Key中的指定成员增加指定的分数。如果成员 不存在，该命令将添加该成员并假设其初始分数为0，此后再将其分数加上increment。如果Key不 存，该命令将创建该Key及其关联的Sorted-Sets，并包含参数指定的成员，其分数为increment参 数。如果与该Key关联的不是Sorted-Sets类型，相关的错误信息将被返回。 以字符串形式表示的 新分数。

ZRANGE key start stop [WITHSCORES] 该命令返回顺序在参数start和stop指定范围内的成员， 这⾥start和stop参数都是0-based，即0表示第⼀个成员，-1表示最后⼀个成员。如果start⼤于该 Sorted-Set中的最⼤索引值，或start > stop，此时⼀个空集合将被返回。如果stop⼤于最⼤索引 值，该命令将返回从start到集合的最后⼀个成员。如果命令中带有可选参数WITHSCORES选项，该 命令在返回的结果中将包含每个成员的分数值，如value1,score1,value2,score2.。 返回索引 在start和stop之间的成员列表。

ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT ofset count] 该命令将返回分数在min 和max之间的所有成员，即满⾜表达式min <= score <= max的成员，其中返回的成员是按照其分数 从低到⾼的顺序返回，如果成员具有相同的分数，则按成员的字典顺序返回。可选参数LIMIT⽤于限 制返回成员的数量范围。可选参数ofset表示从符合条件的第ofset个成员开始返回，同时返回 count个成员。可选参数WITHSCORES的含义参照ZRANGE中该选项的说明。最后需要说明的是参 数中min和max的规则可参照命令ZCOUNT。 返回分数在指定范围内的成员列表。

ZRANK key member Sorted-Set中的成员都是按照分数从低到⾼的顺序存储，该命令将返回参 数中指定成员的位置值，其中0表示第⼀个成员，它是Sorted-Set中分数最低的成员。 如果该成 员存在，则返回它的位置索引值。否则返回nil。

ZREM key member [member.] 该命令将移除参数中指定的成员，其中不存在的成员将被忽 略。如果与该Key关联的Value不是Sorted-Set，相应的错误信息将被返回。 实际被删除的成员数 量。

ZREVRANGE key start stop [WITHSCORES] 该命令的功能和ZRANGE基本相同，唯⼀的差别在 于该命令是通过反向排序获取指定位置的成员，即从⾼到低的顺序。如果成员具有相同的分数，则 按降序字典顺序排序。 返回指定的成员列表。

ZREVRANK key member 该命令的功能和ZRANK基本相同，唯⼀的差别在于该命令获取的索引 是从⾼到低排序后的位置，同样0表示第⼀个元素，即分数最⾼的成员。 如果该成员存在，则返 回它的位置索引值。否则返回nil。

ZSCORE key member 获取指定Key的指定成员的分数。 如果该成员存在，以字符串的形式返 回其分数，否则返回nil。

ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT ofset count] 该命令除了排序⽅式 是基于从⾼到低的分数排序之外，其它功能和参数含义均与ZRANGEBYSCORE相同。 返回分数在 指定范围内的成员列表。

ZREMRANGEBYRANK key start stop 删除索引位置位于start和stop之间的成员，start和stop都 是0-based，即0表示分数最低的成员，-1表示最后⼀个成员，即分数最⾼的成员。 被删除的成 员数量。

ZREMRANGEBYSCORE key min max 删除分数在min和max之间的所有成员，即满⾜表达式 min <= score <= max的所有成员。对于min和max参数，可以采⽤开区间的⽅式表示，具体规则参 照ZCOUNT。 被删除的成员数量。

- 8、对Key操作的命令


KEYS patern 获取所有匹配patern参数的Keys。需要说明的是，在我们的正常操作中应该尽量 避免对该命令的调⽤，因为对于⼤型数据库⽽⾔，该命令是⾮常耗时的，对Redis服务器的性能打击 也是⽐较⼤的。patern⽀持glob-style的通配符格式，如*表示任意⼀个或多个字符，?表示任意字 符，[abc]表示⽅括号中任意⼀个字⺟。 匹配模式的键列表。

DEL key [key.] 从数据库删除中参数中指定的keys，如果指定键不存在，则直接忽略。还需要 另⾏指出的是，如果指定的Key关联的数据类型不是String类型，⽽是List、Set、Hashes和 Sorted Set等容器类型，该命令删除每个键的时间复杂度为O(M)，其中M表示容器中元素的数量。 ⽽对于String类型的Key，其时间复杂度为O(1)。 实际被删除的Key数量。

EXISTS key 判断指定键是否存在。 1表示存在，0表示不存在。

MOVE key db 将当前数据库中指定的键Key移动到参数中指定的数据库中。如果该Key在⽬标数 据库中已经存在，或者在当前数据库中并不存在，该命令将不做任何操作并返回0。 移动成功返 回1，否则0。（切换到另⼀个库 select 1）

RENAME key newkey 为指定指定的键重新命名，如果参数中的两个Keys的名字相同，或者是源 Key不存在，该命令都会返回相关的错误信息。如果newKey已经存在，则直接覆盖。

RENAMENX key newkey 如果新值不存在，则将参数中的原值修改为新值。其它条件和RENAME ⼀致。 1表示修改成功，否则0。

PERSIST key 如果Key存在过期时间，该命令会将其过期时间消除，使该Key不再有超时，⽽是可 以持久化存储。 1表示Key的过期时间被移出，0表示该Key不存在或没有过期时间。

EXPIRE key seconds 该命令为参数中hao 指定的Key设定超时的秒数，在超过该时间后，Key被 ⾃动的删除。如果该Key在超时之前被修改，与该键关联的超时将被移除。 1表示超时被设置，0 则表示Key不存在，或不能被设置。

EXPIREAT key timestamp 该命令的逻辑功能和EXPIRE完全相同，唯⼀的差别是该命令指定的超时 时间是绝对时间，⽽不是相对时间。该时间参数是Unix timestamp格式的，即从1970年1⽉1⽇开始 所流经的秒数。 1表示超时被设置，0则表示Key不存在，或不能被设置。

TL key 获取该键所剩的超时描述。 返回所剩描述，如果该键不存在或没有超时设置，则返 回-1。

RANDOMKEY 从当前打开的数据库中随机的返回⼀个Key。 返回的随机键，如果该数据库是空 的则返回nil。

TYPE key 获取与参数中指定键关联值的类型，该命令将以字符串的格式返回。 返回的字符串 为string、list、set、hash和zset，如果key不存在返回none。

