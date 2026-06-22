⼀、什么是 ThreadLocal

ThreadLocal 提供了线程的局部变量，每个线程都可以通过 set() 和 get() 来对这个局部变量进⾏操 作，但不会和其他线程的局部变量冲突，实现了线程间的据隔离。 简单讲：⼀个获取⽤户的请求线程 A，如果向 ThreadLocal 填充变量 AValue（只能被线程 A 操作）， 该变量对其他获取⽤户的请求线程 B、C.是隔离的.

最简单的使⽤⽅式：

类似⼀次 HTP 请求线程中，利⽤ ThreadLocal 存储 Cokie 对象，进⾏状态管理。set Cokie： private ThreadLocal httpThreadLocal = new ThreadLocal();

httpThreadLocal.set(“Cookie: sid=13420771402233”) 上⾯存储格式是 String ，实际场景存储的是具体的对象。在这次 HTP 请求过程中，任何时候都可以 获取 Cokie 。获取⽅式很简单 get Cokie： String cookieValue = (String) httpThreadLocal.get(); Thread 与 ThreadLocal 对象引⽤关系图

⼆、你熟悉的场景

- 2.1 数据库连接池

⽐如⼀次请求线程进来，业务 Dao 需要更新 user 表和 user-detail 表。如果是 new 出两个数据库 Conection ，分别不同的 Conection 操作 user 表和 user-detail 表，就⽆法保证事务。那么数据库 连接池是如何保证的？ 答案是：利⽤ ThreadLocal 存储唯⼀ Conection 对象。每次请求线程，pol.getConection 获取连 接的时候都会这样操作：

错误的做法

public class XXXService {

private Connection conn;

} 因为 con 是线程不安全的。这样会导致多个请求公⽤⼀个连接。请求量很⼤的情况下，延迟各种。你 懂。 因此，使⽤ ThreadLocal 保证每个请求线程的 Conection 是唯⼀的。即每个线程有⾃⼰的连接。 继续讲到 Spring 框架，在事务开始时，会给当前线程⼀个Jdbc Conection,在整个事务过程，都是使 ⽤该线程绑定的conection来执⾏数据库操作，实现了事务的隔离性。Spring框架⾥⾯就是⽤的 ThreadLocal来实现这种隔离

- 2.2 HTP Cokie


会从 ThreadLocal 获取 Conection 对象。如果有，则保证了后⾯多个数据库操作共⽤同⼀个 Conection ，从⽽保证了事务。

如果没有，往 ThreadLocal 新增Conection 对象，并返回到线程

⽐如你访问百度、我访问百度，会有不同 Cokie 。⽽且你不能访问我的 Cokie，我也不能。顾名思 义，使⽤ ThreadLocal 保证每个 HTP 请求线程的 Cokie 是唯⼀的。

Cokie 这样才能做 Sesion 等状态管理。

三、实战场景

总结⼀下就是：ThreadLocal 可以让同⼀个线程中上下⽂之间数据共享 在上⾯章节 ⼆、你熟悉的场景 其实介绍了很多现有场景。那么我这边具体的实战场景是什么？

简单的例⼦：

适⽤满⾜这两个条件的场景：1.每个线程独有的⼀些信息，2.这些信息⼜会在多个⽅法或类中⽤到。

- 1.
- 2.
- 3.


⼀个请求线程，⾥⾯有两个异步⼩线程，各有⼀个⽅法。分别处理 A 或 B 业务 ⼀种⽅法是传递不可变的⼊参 另⼀种就是 ThreadLocal，放在 ThreadLocal 的⼊参，会被各个⽅法共享。⽽且多个请求线程互 不影响

复杂的例⼦：

⼀次发货操作：会根据⼊参，进⾏组件化、流程编排话。那么⼊参会被各个地⽅⽤到，⽽且有些流程 组件是异步的（类似 new thread 操作的）。这时候可以定⼀个 XContext 上下⽂：

public class XXContext {

private static ThreadLocal<Map<Class<?>, Object>> context = new InheritableThreadLocal<>();

/**

- * 把 参 数 设 置 到 上下 ⽂ 的 Map中

- */


public static void put(Object obj) { Map<Class<?>, Object> map = context.get(); if (map == null) {

map = new HashMap<>(); context.set(map);

} if (obj instanceof Enum) {

map.put(obj.getClass().getSuperclass(), obj); } else {

map.put(obj.getClass(), obj); }

}

/**

- * 从 上下 ⽂ 中 ， 根 据 类 名取 出 参 数

- */


@SuppressWarnings("unchecked") public static <T> T get(Class<T> c) {

Map<Class<?>, Object> map = context.get(); if (map == null) {

# return null;

} return (T) map.get(c);

}

/**

- * 清 空 ThreadLocal的 数据

- */


# public static void clean() {

context.remove(); }

} 代码解析：

都是 static 操作，类似 DateUtil 玩法

记得每次请求线程后清理。可以 AOP 去清理，加个注解就⾏。因为同⼀个请求线程可能被业务⽅公 ⽤。

（完）

