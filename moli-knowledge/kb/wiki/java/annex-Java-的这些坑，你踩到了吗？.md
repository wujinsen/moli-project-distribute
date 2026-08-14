---
title: Java 的这些坑，你踩到了吗？.note（原文插图 annex）
slug: annex-Java-的这些坑，你踩到了吗？
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/面试笔试/Java/基础/Java 的这些坑，你踩到了吗？.note.md
related: [java-并发面试题]
created: 2026-07-05
updated: 2026-07-05
---

# htps:/mp.weixin.q.com/s/KITwpPQJu4K3wAQ3rTjS-A

前⾔

中国有句⽼话叫"事不过三"，指⼀个⼈犯了同样的错误，⼀次两次还可以原谅，再多就不可原谅了。写代码 也是如此，同⼀个代码“坑”，踩第⼀次叫"⻓了经验"，踩第⼆次叫"加深印象"，踩第三次叫"不⻓记性"，踩三 次以上就叫"不可救药"。在本⽂中，笔者总结了⼀些 Java 坑，描述了问题现象，进⾏了问题分析，给出了避 坑⽅法。希望⼤家在⽇常⼯作中，遇到了这类 Java 坑，能够提前避让开来。

- 1 对象⽐较⽅法


JDK 1.7 提供的 Objects.equals ⽅法，⾮常⽅便地实现了对象的⽐较，有效地避免了繁琐的空指针检查。

问题现象

在 JDK1.7 之前，在判断⼀个短整型、整型、⻓整型包装数据类型与常量是否相等时，我们⼀般这样写：

Short shortValue = (short)12345;System.out.println(shortValue == 12345); // trueInteger intValue = 12345;System.out.println(intValue == 12345); // trueLong longValue = 12345L;System.out.println(longValue == 12345); // true

从 JDK1.7 之后，提供了 Objects.equals ⽅法，并推荐使⽤函数式编程，更改代码如下：

Short shortValue = (short)12345;System.out.println(Objects.equals(shortValue, 12345)); // falseInteger intValue = 12345;System.out.println(Objects.equals(intValue, 12345)); // trueLong longValue = 12345L;System.out.println(Objects.equals(longValue, 12345)); // false

为什么直接把 = 替换为 Objects.equals ⽅法就会导致输出结果不⼀样？

问题分析

通过反编译第⼀段代码，我们得到语句 System.out.println(shortValue = 12345); 的字节码指令如下：

getstatic java.lang.System.out : java.io.PrintStream [22]aload_1 [shortValue]invokevirtual java.lang.Short.shortValue() : short [28]sipush 12345if_icmpne 24iconst_1goto 25iconst_0invokevirtual java.io.PrintStream.println(boolean) : void [32]

原来，编译器会判断包装数据类型对应的基本数据类型，并采⽤这个基本数据类型的指令进⾏⽐较（⽐如上 ⾯字节码指令中的 sipush 和 if_icmpne 等），相当于编译器⾃动对常量进⾏了数据类型的强制转化。

为什么采⽤ Objects.equals ⽅法后，编译器不⾃动对常量进⾏数据类型的强制转化？通过反编译第⼆段代 码，我们得到语句 System.out.println(Objects.equals(shortValue, 12345); 的字节码指令如下：

getstatic java.lang.System.out : java.io.PrintStream [22]aload_1 [shortValue]sipush 12345invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [28] invokestatic java.util.Objects.equals(java.lang.Object, java.lang.Object) : boolean [33]invokevirtual java.io.PrintStream.println(boolean) : void [39]

原来，编译器根据字⾯意思，认为常量 12345 默认基本数据类型是 int，所以会⾃动转化为包装数据类型 Integer。

在 Java 语⾔中，整数的默认数据类型是 int，⼩数的默认数据类型是 double。

通过分析Objects.equals⽅法的源代码可知：语句 System.out.println(Objects.equals(shortValue, 12345)，因为 Objects.equals 的两个参数对象类型不⼀致，⼀个是包装数据类型 Short，另⼀个是包装数据 类型 Integer，所以最终的⽐较结果必然是false；⽽语句 System.out.println(Objects.equals(intValue, 12345)，因为 Objects.equals 的两个参数对象类型⼀致，都是包装数据类型 Integer 且取值相同，所以最终 的⽐较结果必然是 true。

避坑⽅法

- 1）保持良好的编码习惯，避免数据类型的⾃动转化


为了避免数据类型⾃动转化，更科学的写法是直接声明常量为对应的基本数据类型。

第⼀段代码可以这样写：

Short shortValue = (short)12345;System.out.println(shortValue == (short)12345); // trueInteger intValue = 12345;System.out.println(intValue == 12345); // trueLong longValue = 12345L;System.out.println(longValue == 12345L); // true

第⼆段代码可以这样写：

Short shortValue = (short)12345;System.out.println(Objects.equals(shortValue, (short)12345)); // trueInteger intValue = 12345;System.out.println(Objects.equals(intValue, 12345)); // trueLong longValue = 12345L;System.out.println(Objects.equals(longValue, 12345L)); // true

- 2）借助开发⼯具或插件，及早地发现数据类型不匹配问题

在 Eclipse 的问题窗⼝中，我们会看到这样的提示：

Unlikely argument type for equals(): int seems to be unrelated to ShortUnlikely argument type for equals(): int seems to be unrelated to Long

- 3）进⾏常规性单元测试，尽量把问题发现在研发阶段


“勿以善⼩⽽不为”，不要因为改动很⼩就不需要进⾏单元测试了，往往 Bug 都出现在⾃⼰过度⾃信的代码 中。像这种问题，只要进⾏⼀次单元测试，是完全可以发现问题的。

注意：进⾏必要单元测试，适⽤于以下所有案例，所以下⽂不再累述。

- 2 三元表达式拆包


三元表达式是 Java 编码中的⼀个固定语法格式：

条件表达式？表达式1：表达式2

三元表达式的逻辑为：如果条件表达式成⽴，则执⾏表达式 1，否则执⾏表达式 2。

问题现象

boolean condition = false;Double value1 = 1.0D;Double value2 = 2.0D;Double value3 = null;Double result = condition ? value1 * value2 : value3; // 抛 出 空 指 针 异常

当条件表达式 condition 等于 false 时，直接把 Double 对象 value3 赋值给 Double 对象 result，按道理没有 任何问题，为什么会抛出空指针异常？

问题分析

通过反编译代码，我们得到语句：

Double result = condition ? value1 * value2 : value3;

的字节码指令如下：

iload_1 [condition]ifeq 33aload_2 [value1]invokevirtual java.lang.Double.doubleValue() : double [24]aload_3 [value2]invokevirtual java.lang.Double.doubleValue() : double [24]dmulgoto 38aload 4 [value3]invokevirtual java.lang.Double.doubleValue() : double [24]invokestatic java.lang.Double.valueOf(double) : java.lang.Double [16]astore 5 [result]

在第 9 ⾏，加载 Double 对象 value 3 到操作数栈中；在第 10 ⾏，调⽤ Double 对象 value 3 的 doubleValue ⽅法。这个时候，由于 value 3 是空对象 nul，调⽤ doubleValue ⽅法必然抛出抛出空指针 异常。但是，为什么要把空对象 value 3 转化为基础数据类型 double 呢？

查阅相关资料，得到三元表达式的类型转化规则：

若两个表达式类型相同，返回值类型为该类型；

若两个表达式类型不同，但类型不可转换，返回值类型为 Object 类型；

若两个表达式类型不同，但类型可以转化，先把包装数据类型转化为基本数据类型，然后按照基本数据类 型的转换规则 （byte < short(char)< int < long < float < double） 来转化，返回值类型为优先级最⾼的基 本数据类型。

根据规则分析，表达式 1（value1 * value2）的类型为基础数据类型 double，表达式 2（value 3）的类型为 包装数据类型 Double，根据三元表达式的类型转化规则判断，最终的表达式类型为基础数据类型 double。 所以，当条件表达式 condition 为 false 时，需要把空 Double 对象 value 3 转化为基础数据类型 double，于 是就调⽤了 value 3 的 doubleValue ⽅法进⾏拆包，当然会抛出空指针异常。

避坑⽅法

- 1）尽量避免使⽤三元表达式，可以采⽤ if-else 语句代替

如果三元表达式中有包装数据类型的算术计算，可以考虑利⽤ if-else 语句代替。改写代码如下：

if (condition) { result = value1 * value2;} else { result = value3;}

- 2）尽量使⽤基本数据类型，避免包装数据类型的拆装包


如果在三元表达式中有算术计算，尽量使⽤基本数据类型，避免包装数据类型的拆装包。改写代码如下：

boolean condition = false;double value1 = 1.0D;double value2 = 2.0D;double value3 = 3.0D;double result = condition ? value1 * value2 : value3;

- 3 泛型对象赋值


Java 泛型是 JDK 1.5 中引⼊的⼀个新特性，其本质是参数化类型，即把数据类型做为⼀个参数使⽤。

问题现象

在做⽤户数据分⻚查询时，因为笔误编写了如下代码：

- 1）PageDataVO.java

/** 分 ⻚ 数据 VO类 */@Getter@Setter@ToString@NoArgsConstructor@AllArgsConstructorpublic class PageDataVO<T> { /** 总 共 数 量 */ private Long totalCount; /** 数据 列 表 */ private List<T> dataList;}

- 2）UserDAO.java

/** ⽤ 户 DAO接 ⼝ */@Mapperpublic interface UserDAO { /** 统 计 ⽤ 户 数 量 */ public Long countUser(@Param("query") UserQueryVO query); /** 查 询 ⽤ 户 信 息 */ public List<UserDO> queryUser(@Param("query") UserQueryVO query);}

- 3）UserService.java


/** ⽤ 户 服 务 类 */@Servicepublic class UserService { /** ⽤ 户 DAO */ @Autowired private UserDAO userDAO; /** 查 询 ⽤ 户 信 息 */ public PageDataVO<UserVO> queryUser(UserQueryVO query) { List<UserDO> dataList = null; Long totalCount = userDAO.countUser(query); if (Objects.nonNull(totalCount) && totalCount.compareTo(0L) > 0) { dataList = userDAO.queryUser(query); } return new PageDataVO(totalCount, dataList); }}

以上代码没有任何编译问题，但是却把 UserDO 中⼀些涉密字段返回给前端。细⼼的读者可能已经发现了， 在 UserService 类的 queryUser ⽅法的语句 return new PageDataVO(totalCount, dataList); 中，我们把 List<UserDO> 对象 dataList 赋值给了 PageDataVO<UserVO> 的 List<UserVO> 字段 dataList。

问题是：为什么开发⼯具不报编译错误啦？

问题分析

由于历史原因，参数化类型和原始类型需要兼容。我们以 ArayList 举例⼦，来看看如何兼容的。

以前的写法：

ArrayList list = new ArrayList();

现在的写法：

ArrayList<String> list = new ArrayList<String>();

考虑到与以前的代码兼容，各种对象引⽤之间传值，必然会出现以下的情况：

// 第 ⼀ 种 情 况 ArrayList list1 = new ArrayList<String>();// 第 ⼆ 种 情 况 ArrayList<String> list2 = new ArrayList();

所以，Java 编译器对以上两种类型进⾏了兼容，不会出现编译错误，但会出现编译告警。但是，我的开发⼯ 具在编译时真没出现过告警。

再来分析我们遇到的问题，实际上同时命中了两种情况：

把 List<UserDO> 对象赋值给 List，命中了第⼀种情况；

把 PageDataVO 对象赋值给 PageDataVO<UserVO>，命中了第⼆种情况。

最终的效果就是：我们神奇地把 List<UserDO> 对象赋值给了 List<UserVO>。

问题的根源就是：我们在初始化 PageDataVO 对象时，没有要求强制进⾏类型检查。

避坑⽅法

1）在初始化泛型对象时，推荐使⽤ diamond 语法

在《 Java 开发⼿册》中，有这么⼀条推荐规则：

【推荐】集合泛型定义时，在 JDK7 及以上，使⽤ diamond 语法或全省略。 说明：菱形泛型，即 diamond，直接使⽤<>来指代前边已经指定的类型。

正例：

// <> diamond ⽅ 式 HashMap<String, String> userCache = new HashMap<>(16);// 全 省 略 ⽅ 式 ArrayList<User> users = new ArrayList(10);

其实，初始化泛型对象时，全省略是不推荐的。这样会避免类型检查，从⽽造成上⾯的问题。

在初始化泛型对象时，推荐使⽤ diamond 语法，代码如下：

return new PageDataVO<>(totalCount, dataList);

现在，在 Eclipse 的问题窗⼝中，我们会看到这样的错误：

Cannot infer type arguments for PageDataVO<>

于是，我们就知道忘记把 List<UserDO> 对象转化为 List<UserVO> 对象了。

- 4 泛型属性拷⻉


Spring 的 BeanUtils.copyProperties ⽅法，是⼀个很好⽤的属性拷⻉⼯具⽅法。

问题现象

根据数据库开发规范，数据库表格必须包含 id，gmt_create，gmt_modified 三个字段。其中，id 这个字 段，可能根据数据量不同，采⽤ int 或 long 类型。

⾸先，定义了⼀个 BaseDO 基类：

/** 基 础 DO类 */@Getter@Setter@ToStringpublic class BaseDO<T> { private T id; private Date gmtCreate; private Date gmtModified;}

针对 user 表，定义了⼀个 UserDO 类：

/** ⽤ 户 DO */@Getter@Setter@ToStringpublic static class UserDO extends BaseDO<Long> { private String name; private String description;}

对于查询接⼝，定义了⼀个 UserVO 类：

/** ⽤ 户 VO类 */@Getter@Setter@ToStringpublic static class UserVO { private Long id; private String name; private String description;}

实现查询⽤户服务接⼝，实现代码如下：

/** ⽤ 户 服 务 类 */@Servicepublic class UserService { /** ⽤ 户 DAO */ @Autowired private UserDAO userDAO;

/** 查 询 ⽤ 户 */ public List<UserVO> queryUser(UserQueryVO query) { // 查 询 ⽤ 户 信 息 List<UserDO> userDOList = userDAO.queryUser(query); if (CollectionUtils.isE mpty()) { return Collections.emptyList(); } // 转 化 ⽤ 户 列 表 List<UserVO> userVOList = new ArrayList<>(userDOList.size()); for (UserDO userDO : userDOList) { UserVO userVO = new UserVO(); BeanUtils.copyProperties(userDO, userVO); userVOList.add(userVO); }

// 返 回 ⽤ 户 列 表 return userVOList; }}

通过测试，我们会发现⼀个问题⸺调⽤查询⽤户服务接⼝，⽤户 ID 的值并没有返回。

[{"description":"This is a tester.","name":"tester"},...]

问题分析

通过 Debug 模式运⾏，进⼊到 BeanUtils.copyProperties ⼯具⽅法内部，得到以下内容：

![image 1](assets/imageFile1.png)

原来，UserDO 类的 getId ⽅法返回类型不是 Long 类型，⽽是被泛型还原成了 Object 类型。⽽下⾯ 的 ClasUtils.isAsignable ⼯具⽅法，判断是否能够把 Object 类型赋值给 Long 类型，当然会返回false导致 不能进⾏属性拷⻉。

为什么作者不考虑"先获取属性值，再判断能否赋值”？建议代码如下：

Object value = readMethod.invoke(source);if (Objects.nonNull(value) && ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], value.getClass())) {

... // 赋 值 相 关 代 码 }

避坑⽅法

- 1）不要盲⽬地相信第三⽅⼯具包，任何⼯具包都有可能存在问题

在 Java 中，存在很多第三⽅⼯具包，⽐如：Apache 的 comons-lang3、comons-colections，Gogle 的 guava…都是很好⽤的第三⽅⼯具包。但是，不要盲⽬地相信第三⽅⼯具包，任何⼯具包都有可能存在问 题。

- 2）如果需要拷⻉的属性较少，可以⼿动编码进⾏属性拷⻉


⽤ BeanUtils.copyProperties 反射拷⻉属性，主要优点是节省了代码量，主要缺点是导致程序性能下降。所 以，如果需要拷⻉的属性较少，可以⼿动编码进⾏属性拷⻉。

- 5 Set 对象排重


在 Java 语⾔中，Set 数据结构可以⽤于对象排重，常⻅的 Set 类有 HashSet、LinkedHashSet 等。

问题现象

编写了⼀个城市辅助类，从 CSV ⽂件中读取城市数据：

/** 城 市 辅 助 类 */@Slf4jpublic class CityHelper { /** 读 取 城 市 */ public static Collection<City> readCities(String fileName) { try (FileInputStream stream = new FileInputStream(fileName); InputStreamReader reader = new InputStreamReader(stream, "GBK"); CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) { Set<City> citySet = new HashSet<>(1024); Iterator<CSVRecord> iterator = parser.iterator(); while (iterator.hasNext()) { citySet.add(parseCity(iterator.next())); } return citySet; } catch (IOException e) { log.warn("读取所有城市异常", e); } return Collections.emptyList(); }

/** 解 析 城 市 */ private static City parseCity(CSVRecord record) { City city = new City(); city.setCode(record.get(0)); city.setName(record.get(1)); return city; }

/** 城 市 类 */ @Getter @Setter @ToString private static class City { /** 城 市 编 码 */ private String code; /** 城 市 名 称 */ private String name; }}

代码中使⽤ HashSet 数据结构，⽬的是为了避免城市数据重复，对读取的城市数据进⾏强制排重。

当输⼊⽂件内容如下时：

编码,名称010,北京020,⼴州010,北京

解析后的 JSON 结果如下：

[{"code":"010","name":"北京"},{"code":"020","name":"⼴州"},{"code":"010","name":"北京"}]

但是，并没有对城市“北京”进⾏排重。

问题分析

当向集合 Set 中增加对象时，⾸先集合计算要增加对象的 hashCode，根据该值来得到⼀个位置⽤来存放当 前对象。如在该位置没有⼀个对象存在的话，那么集合 Set 认为该对象在集合中不存在，直接增加进去。如 果在该位置有⼀个对象存在的话，接着将准备增加到集合中的对象与该位置上的对象进⾏ equals ⽅法⽐较： 如果该 equals ⽅法返回 false，那么集合认为集合中不存在该对象，就把该对象放在这个对象之后；如果 equals ⽅法返回 true，那么就认为集合中已经存在该对象了，就不会再将该对象增加到集合中了。所以，在 哈希表中判断两个元素是否重复要使⽤到 hashCode ⽅法和 equals ⽅法。hashCode ⽅法决定数据在表中的 存储位置，⽽ equals ⽅法判断表中是否存在相同的数据。

分析上⾯的问题，由于没有重写 City 类的 hashCode ⽅法和 equals ⽅法，就会采⽤ Object 类的 hashCode ⽅法和 equals ⽅法。其实现如下：

public native int hashCode();public boolean equals(Object obj) { return (this == obj);}

可以看出：Object 类的 hashCode ⽅法是⼀个本地⽅法，返回的是对象地址；Object 类的 equals ⽅法只⽐ 较对象是否相等。所以，对于两条完全⼀样的北京数据，由于在解析时初始化了不同的 City 对象，导致 hashCode ⽅法和 equals ⽅法值都不⼀样，必然被 Set 认为是不同的对象，所以没有进⾏排重。

那么，我们就重写把 City 类的 hashCode ⽅法和 equals ⽅法，代码如下：

/** 城 市 类 */@Getter@Setter@ToStringprivate static class City { /** 城 市 编 码 */ private String code; /** 城 市 名 称 */ private String name;

/** 判 断 相 等 */ @Override public boolean equals(Object obj) { if (obj == this) { return true; } if (Objects.isNull(obj)) { return false; } if (obj.getClass() != this.getClass()) { return false; } return Objects.equals(this.code, ((City)obj).code); }

/** 哈 希 编 码 */ @Override public int hashCode() { return Objects.hashCode(this.code); }}

重新⽀持测试程序，解析后的 JSON 结果如下：

[{"code":"010","name":"北京"},{"code":"020","name":"⼴州"}]

结果正确，已经对城市“北京”进⾏排重。

避坑⽅法

- 1）当确定数据唯⼀时，可以使⽤ List 代替 Set

当确定解析的城市数据唯⼀时，就没有必要进⾏排重操作，可以直接使⽤ List 来存储。

List<City> citySet = new ArrayList<>(1024);Iterator<CSVRecord> iterator = parser.iterator();while (iterator.hasNext()) { citySet.add(parseCity(iterator.next()));}return citySet;

- 2）当确定数据不唯⼀时，可以使⽤ Map 代替 Set

当确定解析的城市数据不唯⼀时，需要安装城市名称进⾏排重操作，可以直接使⽤ Map 进⾏存储。为什么不 建议实现 City 类的 hashCode ⽅法，再采⽤ HashSet 来实现排重呢？⾸先，不希望把业务逻辑放在模型 DO 类中；其次，把排重字段放在代码中，便于代码的阅读、理解和维护。

Map<String, City> cityMap = new HashMap<>(1024);Iterator<CSVRecord> iterator = parser.iterator();while (iterator.hasNext()) { City city = parseCity(iterator.next()); cityMap.put(city.getCode(), city);}return cityMap.values();

- 3）遵循 Java 语⾔规范，重写 hashCode ⽅法和 equals ⽅法


不重写 hashCode ⽅法和 equals ⽅法的⾃定义类不应该在 Set 中使⽤。

- 6 公有⽅法代理


SpringCGLIB 代理⽣成的代理类是⼀个继承被代理类，通过重写被代理类中的⾮ final 的⽅法实现代理。所 以，SpringCGLIB 代理的类不能是 final 类，代理的⽅法也不能是 final ⽅法，这是由继承机制限制的。

问题现象

这⾥举例⼀个简单的例⼦，只有超级⽤户才有删除公司的权限，并且所有服务函数被 AOP 拦截处理异常。例 ⼦代码如下：

- 1）UserService.java

/** ⽤ 户 服 务 类 */@Servicepublic class UserService { /** 超 级 ⽤ 户 */ private User superUser;

/** 设 置 超 级 ⽤ 户 */ public void setSuperUser(User superUser) { this.superUser = superUser; }

/** 获 取 超 级 ⽤ 户 */ public final User getSuperUser() { return this.superUser; }}

- 2）CompanyService.java


/** 公 司 服 务 类 */@Servicepublic class CompanyService { /** 公 司 DAO */ @Autowired private CompanyDAO companyDAO; /** ⽤ 户 服 务 */ @Autowired private UserService userService;

/** 删 除 公 司 */ public void deleteCompany(Long companyId, Long operatorId) { // 设 置 超 级 ⽤ 户 userService.setSuperUser(new User(0L, "admin", "超级⽤户"));

// 验 证 超 级 ⽤ 户 if (!Objects.equals(operatorId, userService.getSuperUser().getId())) { throw new ExampleException("只有超级⽤户才能 删除公司"); }

// 删 除 公 司 信 息 companyDAO.delete(companyId, operatorId); }}

当我们调⽤ CompanyService 的 deleteCompany ⽅法时，居然也抛出空指针异常 （NulPointerException），因为调⽤ UserService 类的 getSuperUser ⽅法获取的超级⽤户为 nul。但是， 我们在 CompanyService 类的 deleteCompany ⽅法中，每次都通过 UserService 类的 setSuperUser ⽅法强 制指定了超级⽤户，按道理通过 UserService 类的 getSuperUser ⽅法获取到的超级⽤户不应该为 nul。其 实，这个问题也是由 AOP 代理导致的。

问题分析

使⽤ SpringCGLIB 代理类时，Spring 会创建⼀个名 为 UserService$EnhancerBySpringCGLIB$ ? 的代理类。反编译这个代理类，得到以下主要代码：

public class UserService$$EnhancerBySpringCGLIB$$a2c3b345 extends UserService implements SpringProxy, Advised, Factory { ...... public final void setSuperUser(User var1) { MethodInterceptor var10000 = this.CGLIB$CALLBACK_0; if (var10000 == null) { CGLIB$BIND_CALLBACKS(this); var10000 = this.CGLIB$CALLBACK_0; }

if (var10000 != null) { var10000.intercept(this, CGLIB$setSuperUser$0$Method, new Object[]{var1}, CGLIB$setSuperUser$0$Proxy); } else { super.setSuperUser(var1); } } ......}

可以看出，这个代理类继承了 UserService 类，只代理了 setSuperUser ⽅法，但是没有代理 getSuperUser ⽅法。所以，当我们调⽤ setSuperUser ⽅法时，设置的是原始对象实例的 superUser 字段值；⽽当我们调 ⽤ getSuperUser ⽅法时，获取的是代理对象实例的 superUser 字段值。如果把这两个⽅法的 final 修饰符互 换，同样存在获取超级⽤户为 nul 的问题。

避坑⽅法

- 1）严格遵循 CGLIB 代理规范，被代理的类和⽅法不要加 final 修饰符

严格遵循 CGLIB 代理规范，被代理的类和⽅法不要加 final 修饰符，避免动态代理操作对象实例不同（原始对 象实例和代理对象实例），从⽽导致数据不⼀致或空指针问题。

- 2）缩⼩ CGLIB 代理类的范围，能不⽤被代理的类就不要被代理


缩⼩ CGLIB 代理类的范围，能不⽤被代理的类就不要被代理，即可以节省内存开销，⼜可以提⾼函数调⽤效 率。

- 7 公有字段代理


在 fastjson 强制升级到 1.2.60 时踩过⼀个坑，作者为了开发快速，在 ParseConfig 中定义了：

public class ParseConfig { public final SymbolTable symbolTable = new SymbolTable(4096);

......}

在我们的项⽬中继承了该类，同时⼜被 AOP 动态代理了，于是⼀⾏代码引起了⼀场“⾎案”。

问题现象

仍然使⽤上章的例⼦，但是把获取、设置⽅法删除，定义了⼀个公有字段。例⼦代码如下：

- 1）UserService.java


/** ⽤ 户 服 务 类 */@Servicepublic class UserService { /** 超 级 ⽤ 户 */ public final User superUser = new User(0L, "admin", "超级⽤户"); ......}

- 2）CompanyService.java


/** 公 司 服 务 类 */@Servicepublic class CompanyService { /** 公 司 DAO */ @Autowired private CompanyDAO companyDAO; /** ⽤ 户 服 务 */ @Autowired private UserService userService;

/** 删 除 公 司 */ public void deleteCompany(Long companyId, Long operatorId) { // 验 证 超 级 ⽤ 户 if (!Objects.equals(operatorId, userService.superUser.getId())) { throw new ExampleException("只有超级⽤户才能删除公司"); }

// 删 除 公 司 信 息 companyDAO.delete(companyId, operatorId); }}

当我们调⽤ CompanyService 的 deleteCompany ⽅法时，居然抛出空指针异常 （NulPointerException）。 经过调试打印，发现是 UserService 的 superUser 变量为 nul。如果把代理删除，就不会出现空指针异常， 说明这个问题是由 AOP 代理导致的。

问题分析

使⽤ SpringCGLIB 代理类时，Spring 会创建⼀个名 为 UserService$EnhancerBySpringCGLIB$ ? 的代理类。这个代理类继承了 UserService 类，并 覆盖了 UserService 类中的所有⾮ final 的 public 的⽅法。但是，这个代理类并不调⽤ super 基类的⽅法； 相反，它会创建的⼀个成员 userService 并指向原始的 UserService 类对象实例。现在，内存中存在两个对 象实例：⼀个是原始的 UserService 对象实例，另⼀个指向 UserService 的代理对象实例。这个代理类只是 ⼀个虚拟代理，它继承了 UserService 类，并且具有与 UserService 相同的字段，但是它从来不会去初始化 和使⽤它们。所以，⼀但通过这个代理类对象实例获取公有成员变量时，将返回⼀个默认值 nul。

![image 2](assets/imageFile2.png)

避坑⽅法

- 1）当确定字段不可变时，可以定义为公有静态常量


- 当确定字段不可变时，可以定义为公有静态常量，并⽤类名称 + 字段名称访问。类名称 + 字段名称访问公有 静态常量，与类实例的动态代理⽆关。
- 2）当确定字段不可变时，可以定义为私有成员变量

当确定字段不可变时，可以定义为私有成员变量，提供⼀个公有 Geter ⽅法获取该变量值。当该类实例被动 态代理时，代理⽅法会调⽤被代理的 Geter ⽅法，从⽽返回被代理类的成员变量值。

- 3）遵循 JavaBean 编码规范，不要定义公有成员变量


遵循 JavaBean 编码规范，不要定义公有成员变量。JavaBean 规范如下：

JavaBean 类必须是⼀个公共类，并将其访问属性设置为 public，如：public clas User{ .}

JavaBean 类必须有⼀个空的构造函数：类中必须有⼀个不带参数的公⽤构造器

⼀个 JavaBean 类不应有公共实例变量，类变量都为 private，如：private Integer id;

属性应该通过⼀组 geter / seter ⽅法来访问

后记

最后，推荐⼤家阅读⼀下《Java 开发⼿册》，这本⼿册让我受益匪浅。只要学习理解了《Java 开发⼿册》， 就能在⽇常的 Java 开发⼯作中，避免踩到很多常识性的 Java 坑。

![image 3](assets/imageFile3.png)

重磅

![image 4](assets/imageFile4.png)

泰⼭版《Java 开发⼿册》 即将发布

去年 6 ⽉，《Java 开发⼿册》发布了华⼭版，同时还发布了配套的 IDE 插件，在全球 Java 开发者的 共同努⼒下，已成为业界普遍遵循的开发规范，帮助⼤家⾼效开发。《Java 开发⼿册》背后有哪些故 事？作者孤尽对华⼭版有哪些解读？插件该如何使⽤？哪⾥可以下载⼿册的所有版本？

继往开来，在《Java 开发⼿册》泰⼭版即将发布之际，让我们再来全⾯回顾⼀下华⼭版，识别下⽅⼆ 维码或点击⽂末“阅读原⽂”查看：
