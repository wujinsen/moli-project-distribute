Java 8 API添加了⼀个新的抽象称为流Stream，可以让你以⼀种声明的⽅式处理数据。 Stream 使⽤⼀种类似⽤ SQL 语句从数据库查询数据的直观⽅式来提供⼀种对 Java 集合运算和表达的⾼阶抽象。 Stream API可以极⼤提供Java程序员的⽣产⼒，让程序员写出⾼效率、⼲净、简洁的代码。 这种⻛格将要处理的元素集合看作⼀种流， 流在管道中传输， 并且可以在管道的节点上进⾏处理， ⽐如筛选， 排序，聚合 等。 元素流在管道中经过中间操作（intermediate operation）的处理，最后由最终操作(terminal operation)得到前⾯处理的结 果。

+--------------------+ +------+ +------+ +---+ +-------+ | stream of elements +-----> |filter+-> |sorted+-> |map+-> |collect| +--------------------+ +------+ +------+ +---+ +-------+

以上的流程转换为 Java 代码为：

List<Integer> transactionsIds = widgets.stream()

.filter(b -> b.getColor() == RED)

.sorted((x,y) -> x.getWeight() - y.getWeight())

.mapToInt(Widget::getWeight)

.sum();

## 什么是 Stream？

Stream（流）是⼀个来⾃数据源的元素队列并⽀持聚合操作

元素是特定类型的对象，形成⼀个队列。 Java中的Stream并不会存储元素，⽽是按需计算。 数据源 流的来源。 可以是集合，数组，I/O channel， 产⽣器generator 等。 聚合操作 类似SQL语句⼀样的操作， ⽐如filter, map, reduce, find, match, sorted等。

和以前的Colection操作不同， Stream操作还有两个基础的特征：

Pipelining: 中间操作都会返回流对象本身。 这样多个操作可以串联成⼀个管道， 如同流式⻛格（fluent style）。 这样做 可以对操作进⾏优化， ⽐如延迟执⾏(laziness)和短路( short-circuiting)。 内部迭代： 以前对集合遍历都是通过Iterator或者For-Each的⽅式, 显式的在集合外部进⾏迭代， 这叫做外部迭代。 Stream提供了内部迭代的⽅式， 通过访问者模式(Visitor)实现。

## ⽣成流

在 Java 8 中, 集合接⼝有两个⽅法来⽣成流：

stream() − 为集合创建串⾏流。 paralelStream() − 为集合创建并⾏流。

List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl"); List<String> filtered = strings.stream().filter(string -> !string.isEmpty()).collect(Collectors.toList());

# forEach

Stream 提供了新的⽅法 'forEach' 来迭代流中的每个数据。以下代码⽚段使⽤ forEach 输出了10个随机数： Random random = new Random(); random.ints().limit(10).forEach(System.out::println);

# map

map ⽅法⽤于映射每个元素到对应的结果，以下代码⽚段使⽤ map 输出了元素对应的平⽅数：

List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5); // 获取对应的平⽅数 List<Integer> squaresList = numbers.stream().map( i -> i*i).distinct().collect(Collectors.toList());

# filter

filter ⽅法⽤于通过设置的条件过滤出元素。以下代码⽚段使⽤ filter ⽅法过滤出空字符串： List<String>strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl"); // 获取空字符串的数量 int count = strings.stream().filter(string -> string.isEmpty()).count();

# limit

limit ⽅法⽤于获取指定数量的流。 以下代码⽚段使⽤ limit ⽅法打印出 10 条数据： Random random = new Random(); random.ints().limit(10).forEach(System.out::println);

# sorted

sorted ⽅法⽤于对流进⾏排序。以下代码⽚段使⽤ sorted ⽅法对输出的 10 个随机数进⾏排序： Random random = new Random(); random.ints().limit(10).sorted().forEach(System.out::println);

## 并⾏（parallel）程序

paralelStream 是流并⾏处理程序的代替⽅法。以下实例我们使⽤ paralelStream 来输出空字符串的数量： List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl"); // 获取空字符串的数量

int count = strings.parallelStream().filter(string -> string.isEmpty()).count(); 我们可以很容易的在顺序运⾏和并⾏直接切换。

# Collectors

Colectors 类实现了很多归约操作，例如将流转换成集合和聚合元素。Colectors 可⽤于返回列表或字符串： List<String>strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl"); List<String> filtered = strings.stream().filter(string -> !string.isEmpty()).collect(Collectors.toList());

System.out.println("筛选列表: " + filtered); String mergedString = strings.stream().filter(string -> !string.isEmpty()).collect(Collectors.joining(", ")); System.out.println("合并字符串: " + mergedString);

## 统计

另外，⼀些产⽣统计结果的收集器也⾮常有⽤。它们主要⽤于int、double、long等基本类型上，它们可以⽤来产⽣类似如下 的统计结果。 List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5);

IntSummaryStatistics stats = integers.stream().mapToInt((x) -> x).summaryStatistics();

System.out.println("列表中最⼤的数 : " + stats.getMax()); System.out.println("列表中最⼩的数 : " + stats.getMin()); System.out.println("所有数之和 : " + stats.getSum()); System.out.println("平均数 : " + stats.getAverage());

## Stream 完整实例

将以下代码放⼊ Java8Tester.java ⽂件中：

### Java8Tester.java ⽂件

import java.util.ArrayList; import java.util.Arrays; import java.util.IntSummaryStatistics; import java.util.List; import java.util.Random; import java.util.stream.Collectors; import java.util.Map;

public class Java8Tester { public static void main(String args[]){

- System.out.println("使⽤ Java 7: ");


// 计算空字符串 List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd","", "jkl"); System.out.println("列表: " +strings); long count = getCountEmptyStringUsingJava7(strings);

System.out.println("空字符数量为: " + count); count = getCountLength3UsingJava7(strings);

System.out.println("字符串⻓度为 3 的数量为: " + count);

// 删除空字符串 List<String> filtered = deleteEmptyStringsUsingJava7(strings); System.out.println("筛选后的列表: " + filtered);

// 删除空字符串，并使⽤逗号把它们合并起来 String mergedString = getMergedStringUsingJava7(strings,", "); System.out.println("合并字符串: " + mergedString); List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5);

// 获取列表元素平⽅数 List<Integer> squaresList = getSquares(numbers); System.out.println("平⽅数列表: " + squaresList); List<Integer> integers = Arrays.asList(1,2,13,4,15,6,17,8,19);

System.out.println("列表: " +integers); System.out.println("列表中最⼤的数 : " + getMax(integers)); System.out.println("列表中最⼩的数 : " + getMin(integers)); System.out.println("所有数之和 : " + getSum(integers)); System.out.println("平均数 : " + getAverage(integers)); System.out.println("随机数: ");

// 输出10个随机数 Random random = new Random();

for(int i=0; i < 10; i++){

System.out.println(random.nextInt()); }

- System.out.println("使⽤ Java 8: "); System.out.println("列表: " +strings);


count = strings.stream().filter(string->string.isEmpty()).count(); System.out.println("空字符串数量为: " + count);

count = strings.stream().filter(string -> string.length() == 3).count(); System.out.println("字符串⻓度为 3 的数量为: " + count);

filtered = strings.stream().filter(string >!string.isEmpty()).collect(Collectors.toList());

System.out.println("筛选后的列表: " + filtered);

mergedString = strings.stream().filter(string >!string.isEmpty()).collect(Collectors.joining(", ")); System.out.println("合并字符串: " + mergedString);

squaresList = numbers.stream().map( i ->i*i).distinct().collect(Collectors.toList()); System.out.println("Squares List: " + squaresList); System.out.println("列表: " +integers);

IntSummaryStatistics stats = integers.stream().mapToInt((x) ->x).summaryStatistics();

System.out.println("列表中最⼤的数 : " + stats.getMax()); System.out.println("列表中最⼩的数 : " + stats.getMin()); System.out.println("所有数之和 : " + stats.getSum()); System.out.println("平均数 : " + stats.getAverage()); System.out.println("随机数: ");

random.ints().limit(10).sorted().forEach(System.out::println);

// 并⾏处理 count = strings.parallelStream().filter(string -> string.isEmpty()).count(); System.out.println("空字符串的数量为: " + count);

}

private static int getCountEmptyStringUsingJava7(List<String> strings){ int count = 0;

for(String string: strings){

if(string.isEmpty()){

count++; }

} return count;

}

private static int getCountLength3UsingJava7(List<String> strings){ int count = 0;

for(String string: strings){

if(string.length() == 3){

count++; }

} return count;

}

private static List<String> deleteEmptyStringsUsingJava7(List<String> strings){ List<String> filteredList = new ArrayList<String>();

for(String string: strings){

if(!string.isEmpty()){

filteredList.add(string); }

} return filteredList;

}

private static String getMergedStringUsingJava7(List<String> strings, String separator){ StringBuilder stringBuilder = new StringBuilder();

for(String string: strings){

if(!string.isEmpty()){ stringBuilder.append(string); stringBuilder.append(separator);

}

} String mergedString = stringBuilder.toString(); return mergedString.substring(0, mergedString.length()-2);

}

private static List<Integer> getSquares(List<Integer> numbers){ List<Integer> squaresList = new ArrayList<Integer>();

for(Integer number: numbers){ Integer square = new Integer(number.intValue() * number.intValue());

if(!squaresList.contains(square)){

squaresList.add(square); }

} return squaresList;

}

private static int getMax(List<Integer> numbers){ int max = numbers.get(0);

for(int i=1;i < numbers.size();i++){

Integer number = numbers.get(i);

if(number.intValue() > max){

max = number.intValue(); }

} return max;

}

private static int getMin(List<Integer> numbers){ int min = numbers.get(0);

for(int i=1;i < numbers.size();i++){ Integer number = numbers.get(i);

if(number.intValue() < min){

min = number.intValue(); }

} return min;

}

private static int getSum(List numbers){ int sum = (int)(numbers.get(0));

for(int i=1;i < numbers.size();i++){

sum += (int)numbers.get(i);

} return sum;

}

private static int getAverage(List<Integer> numbers){

return getSum(numbers) / numbers.size(); }

} 执⾏以上脚本，输出结果为：

$ javac Java8Tester.java $ java Java8Tester

- 使⽤ Java 7: 列表: [abc, , bc, efg, abcd, , jkl] 空字符数量为: 2 字符串⻓度为 3 的数量为: 3 筛选后的列表: [abc, bc, efg, abcd, jkl] 合并字符串: abc, bc, efg, abcd, jkl 平⽅数列表: [9, 4, 49, 25] 列表: [1, 2, 13, 4, 15, 6, 17, 8, 19] 列表中最⼤的数 : 19 列表中最⼩的数 : 1 所有数之和 : 85 平均数 : 9 随机数:

- -393170844

- -963842252 447036679

- -1043163142

- -881079698 221586850

- -1101570113 576190039

- -1045184578 1647841045


- 使⽤ Java 8: 列表: [abc, , bc, efg, abcd, , jkl] 空字符串数量为: 2 字符串⻓度为 3 的数量为: 3 筛选后的列表: [abc, bc, efg, abcd, jkl] 合并字符串: abc, bc, efg, abcd, jkl Squares List: [9, 4, 49, 25] 列表: [1, 2, 13, 4, 15, 6, 17, 8, 19] 列表中最⼤的数 : 19 列表中最⼩的数 : 1 所有数之和 : 85 平均数 : 9.444444444444445 随机数:


- -1743813696

- -1301974944

- -1299484995

- -779981186 136544902 555792023 1243315896 1264920849 1472077135 1706423674 空字符串的数量为: 2


