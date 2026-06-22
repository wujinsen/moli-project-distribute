# Mac安装elasticsearch-- head插件

htps:/blog.csdn.net/hunandexingkong/article/details/86492860

需要nodejs和git的⽀持

brew instal node

查看是否安装成功 node -v

➜ ~ node -v v6.9.2

➜ ~ 看到版本，说明安装成功

➜ ~ git -version git version 2.9.3 (Aple Git-75)

➜ ~ 本机已经有git

下载head插件 打开 htps:/github.com/mobz/elasticsearch-head git clone git:/github.com/mobz/elasticsearch-head.git

cd elasticsearch-head

npm instal

npm run start

open htp:/localhost:910/

配置elasticsearch,允许head插件访问

进⼊配置⽂件修改head权限

cd /usr/local/etc/elasticsearch/

vim elasticsearch.yml

添加,保存

htp.cors.enabled: true # elasticsearch中启⽤CORS htp.cors.alow-origin: "*" # 允许访问的IP地址段，* 为所有IP都

需要重新启动elasticsearch，

进⼊elasticsearch-head⽬录，⼀定要进⼊这个⽬录才能执⾏npm run start命令，实际上是找⽬录下的 package.json⽂件。

重新执⾏npm run start。否则会出现跨域问题。授权后要重启ES和head插件

重新进⼊htp:/localhost:910/

记录原始状态

开始操作

操作⼀：创建索引

复合查询，类型PUT，URL：htp:/localhost:920/helei

提交请求

{ "acknowledged": true, "shards_acknowledged": true, "index": "helei" } 从返回看到我应该是创建了⼀个索引 helei , ES中索引是最⼤⼀级，然后是type，document， field。

回到⾸⻚验证

操作⼆：创建索引

进⼊索引⻚签创建索引

问题：分⽚数，副本数，代表了什么？

操作三：添加⽂档

使⽤post⽅式添加，url: htp:/localhost:920/helei/first/12

这⾥helei是索引，first是类别，12是ID

如果12不写，是什么情况？试⼀下 htp:/localhost:920/helei/first

系统给随机⽣成了⼀个ID，其他和指定ID的结果⼀样。

那我再执⾏⼀次ID：12的URL会是什么结果？

能够看到三处变化"result": "updated" "_version": 2 "_seq_no": 1 说明这是个修改操作。

添加json继续提交，看到添加了索引⽂档

操作四：修改⽂档

修改过程与上⾯⼀样，指定URL到具体ID，post提交

操作五：查询⽂档

指定URL，get提交 htp:/localhost:920/helei/first/12/

去掉ID，尝试 htp:/localhost:920/helei/first/ 结果报错了。。看来查询只能具体到ID

操作六：删除⽂档

指定URL，delete提交 htp:/localhost:920/helei/first/12/

再次查询

尝试能否删除类型first？报错，类型的查询和删除都报错。

尝试删除索引，成功。

操作七：打开关闭索引

关闭索引_close htp:/localhost:920/helei/_close/

打开索引_open htp:/localhost:920/helei/_open/

操作⼋：添加索引映射

PUT htp:/localhost:920/helei3/

添加了索引helei3 ⽂档类型 first 添加字段name 字段类型是keyword

(keyword类型适合短词汇内容，⽐如邮件，姓名，性别等等，text类型适合⻓⽂本，可以分词，⽐如 ⽂章标题，⽂章内容等)

操作九

⼀、例⼦

{

"query": { "bol": {

"must": [/ = 的关系 {

"range": { "create_time": {

"gt": "191-01-01", "lt": "2019-01-01"

} }

} ], "must_not": [], / ！= 的关系 "should": [] / | 的关系

} }, "from": 0, / 开始 "size": 50, / ⻚⼤⼩ "sort": [ / ⽤什么排序

{

"clue_create_time": "desc"

} ], "ags": {}

} ⼆、must, must not,should的区别

must 返回的⽂档必须满⾜must⼦句的条件，类似于 = and

must not返回的⽂档必须不满⾜must not ⼦句的条件 类似于!= not

should 返回的⽂档只要满⾜should中的⼀个条件即可 类似于 | or

三、各类查询参数

prefix 前缀 wildcard 通配符查询 例：*商品* term 分词， long int 类型 ， 17 的 分词就是 17 ， string 类型 为 空格 分割的单词 。 test data 分词之 后是 test ,data . 【test data】 不是

range 区间 范围 只对整形 有作⽤ ，string ⽆效。

fuzy 区间 value +- min_similarity 本实例中 是 -19 ~21 之间

{"query":{"bol":{"must":[],"must_not":[],"should":[{"fuzy":{"10120.token_id": {"value":"1","min_similarity":"20"}]},"from":0,"size":10,"sort":[],"facets":{}:

query_string 可以对 int long string 进⾏ 查询。 对 int long 只能 本身查询; 对 string 进⾏ 分词查询 本身 也可以查询。

mising 对 那列中 没有 值的列 进⾏显示。

总结：

PUT新增，POST修改，GET查询，DELETE删除；

类型不能删除和查询；

图形操作和URL命令都能创建索引和删除索引，可以⽤POST命令打开关闭索引；

添加⽂档不指定ID，系统会随机ID；

对同⼀ID的多次post操作是修改操作，版本号递增；

命令_close _open _maping _search;

maping是映射关键字 properties是添加指定⽂档类型的字段的关键字;

keyword类型适合短词汇内容，⽐如邮件，姓名，性别等等，text类型适合⻓⽂本，可以分词，⽐如⽂ 章标题，⽂章内容等;

text⽤于全⽂搜索的, ⽽keyword⽤于关键词搜索；

⸻版权声明：本⽂为CSDN博主「hunandexingkong」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载 请附上原⽂出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/hunandexingkong/article/details/86492860

