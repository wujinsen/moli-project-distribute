---
title: 如何更规范化编写Java 代码.note（原文插图 annex）
slug: annex-如何更规范化编写Java-代码
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/编码规范/程序编码/如何更规范化编写Java 代码.note.md
related: [mybatis-plus-用法与注入防护]
created: 2026-07-05
updated: 2026-07-05
---

### Many of the happiest people are those who own the least. But are we really so happy with our IPhones, our big houses, our fancy cars?

## 忘 川 如 斯 ， 拥 有 ⼀ 切 的 ⼈ 才 更 怕 失 去 。

背 景 ： 如何更规范化编写Java 代码的重要性想必毋需多⾔，其中最重要的⼏点当属提⾼代码性能、使代码远 离Bug、令代码更优雅。

## ⼀ 、MyBatis 不 要 为了 多 个 查 询 条 件 ⽽ 写 1 = 1

当遇到多个查询条件，使⽤where 1=1 可以很⽅便的解决我们的问题，但是这样很可能会造成⾮常⼤ 的性能损失，因为添加了 “where 1=1 ”的过滤条件之后，数据库系统就⽆法使⽤索引等查询优化策略，数 据库系统将会被迫对每⾏数据进⾏扫描（即全表扫描） 以⽐较此⾏是否满⾜过滤条件，当表中的数据量较⼤ 时查询速度会⾮常慢；此外，还会存在SQL 注⼊的⻛险。

#### 反 例 ：

<select id="queryBookInfo" parameterType="com.tjt.platform.entity.BookInfo" resultType="java.lang.Integer">

select count(*) from t_rule_BookInfo t where 1=1 <if test="title !=null and title !='' ">

AND title = #{title} </if> <if test="author !=null and author !='' ">

AND author = #{author} </if> </select>

<table>
  <tr>
    <th>![image 1](assets/imageFile1.png)</th>
  </tr>
</table>


#### 正 例 ：

<table>
  <tr>
    <th>![image 2](assets/imageFile2.png)</th>
  </tr>
</table>


<select id="queryBookInfo" parameterType="com.tjt.platform.entity.BookInfo" resultType="java.lang.Integer">

select count(*) from t_rule_BookInfo t <where> <if test="title !=null and title !='' ">

title = #{title} </if> <if test="author !=null and author !='' ">

AND author = #{author} </if> </where> </select>

<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


UPDATE 操作也⼀样，可以⽤<set> 标记代替 1=1。

# ⼆ 、 迭 代 entrySet() 获 取 Map 的 key 和 value

当循环中只需要获取Map 的主键key时，迭代keySet() 是正确的；但是，当需要主键key 和取值value 时，迭代entrySet() 才是更⾼效的做法，其⽐先迭代keySet() 后再去通过get 取值性能更佳。

反 例 ：

- 1 //Map 获取value 反例:

- 2 HashMap<String, String> map = new HashMap<>();

- 3 for (String key : map.keySet()){

- 4 String value = map.get(key);

- 5 } 正 例 ：


- 1 //Map 获取key & value 正例:

- 2 HashMap<String, String> map = new HashMap<>();

- 3 for (Map.Entry<String,String> entry : map.entrySet()){

- 4 String key = entry.getKey();

- 5 String value = entry.getValue();

- 6 } 三 、使 ⽤ Collection.isEmpty() 检 测 空


使⽤Collection.size() 来检测是否为空在逻辑上没有问题，但是使⽤Collection.isEmpty() 使得代码 更易读，并且可以获得更好的性能；除此之外，任何Collection.isEmpty() 实现的时间复杂度都是O(1) ， 不需要多次循环遍历，但是某些通过Collection.size() ⽅法实现的时间复杂度可能是O(n)。

O(1)纬度减少 循环次数 例⼦

反 例 ：

- 1 LinkedList<Object> collection = new LinkedList<>();

- 2 if (collection.size() == 0){

- 3 System.out.println("collection is empty.");

- 4 } 正 例 ：


<table>
  <tr>
    <th>![image 4](assets/imageFile4.png)</th>
  </tr>
</table>


- 1 LinkedList<Object> collection = new LinkedList<>();

- 2 if (collection.isEmpty()){

- 3 System.out.println("collection is empty.");

- 4 }

- 5

- 6 //检测是否为null 可以使⽤CollectionUtils.isEmpty()

- 7 if (CollectionUtils.isEmpty(collection)){

- 8 System.out.println("collection is null.");

- 9

- 10 }


<table>
  <tr>
    <th>![image 5](assets/imageFile5.png)</th>
  </tr>
</table>


## 四 、初 始 化 集 合 时 尽 量 指 定 其 ⼤ ⼩

尽量在初始化时指定集合的⼤⼩，能有效减少集合的扩容次数，因为集合每次扩容的时间复杂度很可能 时O(n)，耗费时间和性能。

反 例 ：

- 1 //初始化list，往list 中添加元素反例：

- 2 int[] arr = new int[]{1,2,3,4};

- 3 List<Integer> list = new ArrayList<>();

- 4 for (int i : arr){

- 5 list.add(i);

- 6 } 正 例 ：


<table>
  <tr>
    <th>![image 6](assets/imageFile6.png)</th>
  </tr>
</table>


- 1 //初始化list，往list 中添加元素正例：

- 2 int[] arr = new int[]{1,2,3,4};

- 3 //指定集合list 的容量⼤⼩

- 4 List<Integer> list = new ArrayList<>(arr.length);

- 5 for (int i : arr){

- 6 list.add(i);

- 7 }


<table>
  <tr>
    <th>![image 7](assets/imageFile7.png)</th>
  </tr>
</table>


# 五 、使 ⽤ StringBuilder 拼 接 字 符 串

⼀般的字符串拼接在编译期Java 会对其进⾏优化，但是在循环中字符串的拼接Java 编译期⽆法执⾏优 化，所以需要使⽤StringBuilder 进⾏替换。

反 例 ：

- 1 //在循环中拼接字符串反例

- 2 String str = "";

- 3 for (int i = 0; i < 10; i++){

- 4 //在循环中字符串拼接Java 不会对其进⾏优化

- 5 str += i;

- 6 } 正 例 ：


<table>
  <tr>
    <th>![image 8](assets/imageFile8.png)</th>
  </tr>
</table>


- 1 //在循环中拼接字符串正例

- 2 String str1 = "Love";

- 3 String str2 = "Courage";

- 4 String strConcat = str1 + str2; //Java 编译器会对该普通模式的字符串拼接进⾏优化

- 5 StringBuilder sb = new StringBuilder();

- 6 for (int i = 0; i < 10; i++){

- 7 //在循环中，Java 编译器⽆法进⾏优化，所以要⼿动使⽤StringBuilder

- 8 sb.append(i);

- 9 }


<table>
  <tr>
    <th>![image 9](assets/imageFile9.png)</th>
  </tr>
</table>


# 六 、若 需频 繁 调 ⽤ Collection.contains ⽅ 法 则 使 ⽤ Set

在Java 集合类库中，List的contains ⽅法普遍时间复杂度为O(n)，若代码中需要频繁调⽤contains ⽅ 法查找数据则先将集合list 转换成HashSet 实现，将O(n) 的时间复杂度将为O(1)。

反 例 ：

<table>
  <tr>
    <th>![image 10](assets/imageFile10.png)</th>
  </tr>
</table>


- 1 //频繁调⽤Collection.contains() 反例

- 2 List<Object> list = new ArrayList<>();

- 3 for (int i = 0; i <= Integer.MAX_VALUE; i++){

- 4 //时间复杂度为O(n)

- 5 if (list.contains(i))

- 6 System.out.println("list contains "+ i);

- 7 }


<table>
  <tr>
    <th>![image 11](assets/imageFile11.png)</th>
  </tr>
</table>


#### 正 例 ：

<table>
  <tr>
    <th>![image 12](assets/imageFile12.png)</th>
  </tr>
</table>


- 1 //频繁调⽤Collection.contains() 正例

- 2 List<Object> list = new ArrayList<>();

- 3 Set<Object> set = new HashSet<>();

- 4 for (int i = 0; i <= Integer.MAX_VALUE; i++){

- 5 //时间复杂度为O(1)

- 6 if (set.contains(i)){

- 7 System.out.println("list contains "+ i);

- 8 }

- 9 }


<table>
  <tr>
    <th>![image 13](assets/imageFile13.png)</th>
  </tr>
</table>


## 七 、使 ⽤ 静 态 代 码 块 实 现 赋 值 静 态 成 员变 量

对于集合类型的静态成员变量，应该使⽤静态代码块赋值，⽽不是使⽤集合实现来赋值。

反 例 ：

<table>
  <tr>
    <th>![image 14](assets/imageFile14.png)</th>
  </tr>
</table>


- 1 //赋值静态成员变量反例

- 2 private static Map<String, Integer> map = new HashMap<String, Integer>(){

- 3 {

- 4 map.put("Leo",1);

- 5 map.put("Family-loving",2);

- 6 map.put("Cold on the out side passionate on the inside",3);

- 7 }

- 8 };

- 9 private static List<String> list = new ArrayList<>(){

- 10 {

- 11 list.add("Sagittarius");

- 12 list.add("Charming");

- 13 list.add("Perfectionist");

- 14 }

- 15 };


<table>
  <tr>
    <th>![image 15](assets/imageFile15.png)</th>
  </tr>
</table>


#### 正 例 ：

<table>
  <tr>
    <th>![image 16](assets/imageFile16.png)</th>
  </tr>
</table>


- 1 //赋值静态成员变量正例

- 2 private static Map<String, Integer> map = new HashMap<String, Integer>();

- 3 static {

- 4 map.put("Leo",1);

- 5 map.put("Family-loving",2);

- 6 map.put("Cold on the out side passionate on the inside",3);

- 7 }

- 8

- 9 private static List<String> list = new ArrayList<>();

- 10 static {

- 11 list.add("Sagittarius");

- 12 list.add("Charming");

- 13 list.add("Perfectionist");

- 14 }


<table>
  <tr>
    <th>![image 17](assets/imageFile17.png)</th>
  </tr>
</table>


## ⼋ 、删 除 未 使 ⽤ 的 局 部 变 量 、⽅ 法 参 数 、私 有 ⽅ 法 、字 段 和 多 余 的 括 号 。 九 、⼯ 具 类 中 屏 蔽 构 造 函 数

⼯具类是⼀堆静态字段和函数的集合，其不应该被实例化；但是，Java 为每个没有明确定义构造函数的 类添加了⼀个隐式公有构造函数，为了避免不必要的实例化，应该显式定义私有构造函数来屏蔽这个隐式公 有构造函数。

反 例 ：

<table>
  <tr>
    <th>![image 18](assets/imageFile18.png)</th>
  </tr>
</table>


- 1 public class PasswordUtils {

- 2 //⼯具类构造函数反例

- 3 private static final Logger LOG = LoggerFactory.getLogger(PasswordUtils.class);

- 4

- 5 public static final String DEFAULT_CRYPT_ALGO = "PBEWithMD5AndDES";

- 6

- 7 public static String encryptPassword(String aPassword) throws IOException {

- 8 return new PasswordUtils(aPassword).encrypt();

- 9 }


<table>
  <tr>
    <th>![image 19](assets/imageFile19.png)</th>
  </tr>
</table>


#### 正 例 ：

<table>
  <tr>
    <th>![image 20](assets/imageFile20.png)</th>
  </tr>
</table>


- 1 public class PasswordUtils {

- 2 //⼯具类构造函数正例

- 3 private static final Logger LOG =

LoggerFactory.getLogger(PasswordUtils.class);

- 4

- 5 //定义私有构造函数来屏蔽这个隐式公有构造函数

- 6 private PasswordUtils(){}

- 7

- 8 public static final String DEFAULT_CRYPT_ALGO = "PBEWithMD5AndDES";

- 9

- 10 public static String encryptPassword(String aPassword) throws IOException {

- 11 return new PasswordUtils(aPassword).encrypt();

- 12 }


<table>
  <tr>
    <th>![image 21](assets/imageFile21.png)</th>
  </tr>
</table>


## ⼗ 、删 除 多 余 的 异常 捕 获 并 抛 出

⽤catch 语句捕获异常后，若什么也不进⾏处理，就只是让异常重新抛出，这跟不捕获异常的效果⼀ 样，可以删除这块代码或添加别的处理。

反 例 ：

<table>
  <tr>
    <th>![image 22](assets/imageFile22.png)</th>
  </tr>
</table>


- 1 //多余异常反例

- 2 private static String fileReader(String fileName)throws IOException{

- 3

- 4 try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))

{

- 5 String line;

- 6 StringBuilder builder = new StringBuilder();

- 7 while ((line = reader.readLine()) != null) {

- 8 builder.append(line);

- 9 }

- 10 return builder.toString();

- 11 } catch (Exception e) {

- 12 //仅仅是重复抛异常 未作任何处理

- 13 throw e;

- 14 }

- 15 }


<table>
  <tr>
    <th>![image 23](assets/imageFile23.png)</th>
  </tr>
</table>


#### 正 例 ：

<table>
  <tr>
    <th>![image 24](assets/imageFile24.png)</th>
  </tr>
</table>


- 1 //多余异常正例

- 2 private static String fileReader(String fileName)throws IOException{

- 3

- 4 try (BufferedReader reader = new BufferedReader(new FileReader(fileName)))

{

- 5 String line;

- 6 StringBuilder builder = new StringBuilder();

- 7 while ((line = reader.readLine()) != null) {

- 8 builder.append(line);

- 9 }

- 10 return builder.toString();

- 11 //删除多余的抛异常，或增加其他处理：

- 12 /*catch (Exception e) {

- 13 return "fileReader exception";

- 14 }*/

- 15 }

- 16 }


<table>
  <tr>
    <th>![image 25](assets/imageFile25.png)</th>
  </tr>
</table>


# ⼗ ⼀ 、字 符 串 转 化 使 ⽤ String.valueOf(value) 代 替 " " + value

把其它对象或类型转化为字符串时，使⽤String.valueOf(value) ⽐ ""+value 的效率更⾼。

反 例 ：

- 1 //把其它对象或类型转化为字符串反例：

- 2 int num = 520;

- 3 // "" + value

- 4 String strLove = "" + num; 正 例 ：


- 1 //把其它对象或类型转化为字符串正例：

- 2 int num = 520;

- 3 // String.valueOf() 效率更⾼

- 4 String strLove = String.valueOf(num); ⼗ ⼆ 、避 免 使 ⽤ BigDecimal(double)


BigDecimal(double) 存在精度损失⻛险，在精确计算或值⽐较的场景中可能会导致业务逻辑异常。

反 例 ：

- 1 // BigDecimal 反例

- 2 BigDecimal bigDecimal = new BigDecimal(0.11D); 正 例 ：


- 1 // BigDecimal 正例

- 2 BigDecimal bigDecimal1 = bigDecimal.valueOf(0.11D);


##### 图1. BigDecimal 精度丢失

![image 26](assets/imageFile26.png)

⼗ 三 、返 回 空 数 组 和 集 合 ⽽ ⾮ null

若程序运⾏返回null，需要调⽤⽅强制检测null，否则就会抛出空指针异常；返回空数组或空集合，有效 地避免了调⽤⽅因为未检测null ⽽抛出空指针异常的情况，还可以删除调⽤⽅检测null 的语句使代码更简 洁。

反 例 ：

<table>
  <tr>
    <th>![image 27](assets/imageFile27.png)</th>
  </tr>
</table>


- 1 //返回null 反例

- 2 public static Result[] getResults() {

- 3 return null;

- 4 }

- 5

- 6 public static List<Result> getResultList() {

- 7 return null;

- 8 }

- 9

- 10 public static Map<String, Result> getResultMap() {

- 11 return null;

- 12 }


<table>
  <tr>
    <th>![image 28](assets/imageFile28.png)</th>
  </tr>
</table>


#### 正 例 ：

<table>
  <tr>
    <th>![image 29](assets/imageFile29.png)</th>
  </tr>
</table>


- 1 //返回空数组和空集正例

- 2 public static Result[] getResults() {

- 3 return new Result[0];

- 4 }

- 5

- 6 public static List<Result> getResultList() {

- 7 return Collections.emptyList();

- 8 }

- 9

- 10 public static Map<String, Result> getResultMap() {

- 11 return Collections.emptyMap();

- 12 }


<table>
  <tr>
    <th>![image 30](assets/imageFile30.png)</th>
  </tr>
</table>


## ⼗ 四 、优 先 使 ⽤ 常 量 或 确 定 值 调 ⽤ equals ⽅ 法

对象的equals ⽅法容易抛空指针异常，应使⽤常量或确定有值的对象来调⽤equals ⽅法。

反 例 ：

- 1 //调⽤ equals ⽅法反例

- 2 private static boolean fileReader(String fileName)throws IOException{

- 3

- 4 // 可能抛空指针异常

- 5 return fileName.equals("Charming");

- 6 } 正 例 ：


<table>
  <tr>
    <th>![image 31](assets/imageFile31.png)</th>
  </tr>
</table>


- 1 //调⽤ equals ⽅法正例

- 2 private static boolean fileReader(String fileName)throws IOException{

- 3

- 4 // 使⽤常量或确定有值的对象来调⽤ equals ⽅法

- 5 return "Charming".equals(fileName);

- 6

- 7 //或使⽤： java.util.Objects.equals() ⽅法

- 8 return Objects.equals("Charming",fileName);

- 9 }


<table>
  <tr>
    <th>![image 32](assets/imageFile32.png)</th>
  </tr>
</table>


## ⼗ 五 、枚 举 的 属 性 字 段 必 须 是 私 有 且不 可变

枚举通常被当做常量使⽤，如果枚举中存在公共属性字段或设置字段⽅法，那么这些枚举常量的属性很 容易被修改；理想情况下，枚举中的属性字段是私有的，并在私有构造函数中赋值，没有对应的Setter ⽅ 法，最好加上final 修饰符。

反 例 ：

<table>
  <tr>
    <th>![image 33](assets/imageFile33.png)</th>
  </tr>
</table>


- 1 public enum SwitchStatus {

- 2 // 枚举的属性字段反例

- 3 DISABLED(0, "禁⽤"),

- 4 ENABLED(1, "启⽤");

- 5

- 6 public int value;

- 7 private String description;

- 8

- 9 private SwitchStatus(int value, String description) {

- 10 this.value = value;

- 11 this.description = description;

- 12 }

- 13

- 14 public String getDescription() {

- 15 return description;

- 16 }

- 17

- 18 public void setDescription(String description) {

- 19 this.description = description;

- 20 }

- 21 }


<table>
  <tr>
    <th>![image 34](assets/imageFile34.png)</th>
  </tr>
</table>


#### 正 例 ：

<table>
  <tr>
    <th>![image 35](assets/imageFile35.png)</th>
  </tr>
</table>


- 1 public enum SwitchStatus {

- 2 // 枚举的属性字段正例

- 3 DISABLED(0, "禁⽤"),

- 4 ENABLED(1, "启⽤");

- 5

- 6 // final 修饰

- 7 private final int value;

- 8 private final String description;

- 9

- 10 private SwitchStatus(int value, String description) {

- 11 this.value = value;

- 12 this.description = description;

- 13 }

- 14

- 15 // 没有Setter ⽅法

- 16 public int getValue() {

- 17 return value;

- 18 }

- 19

- 20 public String getDescription() {

- 21 return description;

- 22 }

- 23 }


<table>
  <tr>
    <th>![image 36](assets/imageFile36.png)</th>
  </tr>
</table>


# ⼗ 六 、tring.split(String regex)部 分关 键 字 需 要 转 译

使⽤字符串String 的plit ⽅法时，传⼊的分隔字符串是正则表达式，则部分关键字（⽐如 .[]()\| 等） 需要转义。

反 例 ：

- 1 // String.split(String regex) 反例

- 2 String[] split = "a.ab.abc".split(".");

- 3 System.out.println(Arrays.toString(split)); // 结果为[]

- 4

- 5 String[] split1 = "a|ab|abc".split("|");

- 6 System.out.println(Arrays.toString(split1)); // 结果为["a", "|", "a", "b", "|", "a", "b", "c"] 正 例 ：


<table>
  <tr>
    <th>![image 37](assets/imageFile37.png)</th>
  </tr>
</table>


- 1 // String.split(String regex) 正例

- 2 // . 需要转译

- 3 String[] split2 = "a.ab.abc".split("\\.");

- 4 System.out.println(Arrays.toString(split2)); // 结果为["a", "ab", "abc"]

- 5

- 6 // | 需要转译

- 7 String[] split3 = "a|ab|abc".split("\\|");

- 8 System.out.println(Arrays.toString(split3)); // 结果为["a", "ab", "abc"]


<table>
  <tr>
    <th>![image 38](assets/imageFile38.png)</th>
  </tr>
</table>


##### 图2. String.split(String regex) 正反例

![image 39](assets/imageFile39.png)

拥 有⼀切 的 ⼈ 才 更 怕 失 去
