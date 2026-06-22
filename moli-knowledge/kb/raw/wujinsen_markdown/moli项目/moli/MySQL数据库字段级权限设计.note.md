# MySQL数据库字段级权限设计

htps:/ w.jianshu.com/p/89d39625c2

⼀、 引⾔

业务场景

在⼀个类似数据统计的系统中，由于统计的数据较多，就有较多的表，每个表有较多的字段， 但是⼜不想让每个⽤户都看到全部的表或者表的全部字段，⽐如⼀些重要的统计数据，应该是 只有管理员才能看到的。

举个例⼦(⽆实际意义)

<table>
  <tr>
    <th>ID</th>
    <th>name</th>
    <th>day_amount</th>
    <th>wek_amount</th>
    <th>month_amount</th>
  </tr>
  <tr>
    <td> </td>
    <td>脑点⼦</td>
    <td> </td>
    <td> </td>
    <td> </td>
  </tr>
</table>


1 1, 0 7, 0 30, 0

A⽤户只能看到name，day_amount 两个字段；B⽤户只能看到name，month_amount两个字段。 ⼆、 ⽅案

⽅案⼀

⽤户表、⽤户-表名-字段映射表

优点：实现简单 缺点：每个⽤户，每个表，每个字段都需单独设置，较为繁琐

⽅案⼆

⽤户表、⻆⾊表、⽤户-⻆⾊映射表、⻆⾊-表名-字段映射表(优先级低)、⽤户-表名-字段映射 表(优先级⾼)

优点：可通过⻆⾊实现批量设置权限；且也可对某个⽤户权限进⾏单独设置 缺点：权限局限于数据库单个表。

⽅案三

⽤户表、⻆⾊表、⽤户-⻆⾊映射表、数据类型表、类型-表名-字段映射表(优先级低)、⻆⾊-类 型映射表、⽤户-表名-字段映射表(优先级⾼)

优点：在⽅案⼆的基础上，可实现跨表的权限控制。 缺点：实现较为繁琐，要不要采⽤主要还是根据⽤户的需求

对外权限分配接⼝

三种都按照表名作为⼀级节点，字段作为⼆级节点来分配权限。

⽅案⼀只能按照单个⽤户来分配权限

⽅案⼆按照⻆⾊/单个⽤户来分配 ⽅案三按照类型/单个⽤户来分配

存在的问题

- 1）数据库结构变更时，与该表相关的所有权限都需要重新设置

- 2）部分不在数据库的字段，如根据多列的值计算出来的属性字段的权限不好处理。

- 3）只⽀持单张表的查询，不⽀持多表查询


部分解决思路

- 问题2）的暂时的解决思路是：再单独建⼀张表⸺⽤户/⻆⾊/类型-JAVA类名-属性名映射 表，再配合JSON序列化的属性过滤来实现权限控制。这种⽅式基本跟完全写死没什么区 别，对后期的维护及扩展极不友好！

- 问题3）的解决思路是：放弃掉SQL语句的连接查询，全部改成由代码控制


三、 代码 最初是打算在Spring Aop的前置通知中通过修改⽬标⽅法参数来实现，但是通过源码发现封装⽬标⽅ 法参数的类是⽤final修饰的，所以后⾯换了种思路。 使⽤Spring的AbstractAutoProxyCreator⾃动代理实现，思路是通过条件判断决定是否要使⽤⾃动代 理，要使⽤代理的话，就需要⾃⼰实现MethodInterceptor接⼝并重写其中invoke⽅法。 下⾯我贴出核⼼代码，⽂章最后会给出整个demo的链接 继承AbstractAutoProxyCreator类，重写getAdvicesAndAdvisorsForBean()⽅法

public class BeanTypeAutoProxyCreator extends AbstractAutoProxyCreator {

@Override protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass,

String beanName, TargetSource customTargetSource) throws BeansException { return isMatch(beanClass) ? PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS : DO_NOT_PROXY;

}

/**

- * 判断是否是需要被代理的对象
- * @param clazz 代理对象的类型
- * @return
- */


private boolean isMatch(Class<?> clazz) { //有两个Class类型的类象，⼀个是调⽤isAssignableFrom⽅法的类对象（后称对象a）， // 以及⽅法中作为参数的这个类对象(称之为对象b)，这两个对象如果满⾜以下条件则返回true，否则返回

false：

//a对象所对应类信息是b对象所对应的类信息的⽗类或者是⽗接⼝，简单理解即a是b的⽗类或接⼝ //a对象所对应类信息与b对象所对应的类信息相同，简单理解即a和b为同⼀个类或同⼀个接⼝ if (BaseMapper.class.isAssignableFrom(clazz)) {

return true;

} return false;

} }

实现MethodInterceptor接⼝，重写invoke()⽅法

public class MyMethodInterceptor implements MethodInterceptor {

@Autowired private SysAccess sysAccess;

@Override public Object invoke(MethodInvocation invocation) throws Throwable {

// 权限封装类 SysAccessCriteria result = null; int flag = -1;

// ⽬标⽅法的参数 Object[] args = invocation.getArguments(); for (int i=0; i<args.length; i++) {

// 只修改权限条件类型的参数 if(args[i] instanceof SysAccessCriteria){

SysAccessCriteria sysAccessCriteria = (SysAccessCriteria) args[i]; result = sysAccess.getUserAceess(sysAccessCriteria); flag = i;

}

} // 修改⽬标参数 if(flag >= 0 && result != null){

args[flag] = result; }

// 执⾏⽬标⽅法 Object object = invocation.proceed();

return object; }

}

配置到Spring配置⽂件中

<bean id="myMethodInterceptor" class="com.ysl.access.proxy.MyMethodInterceptor"></bean> <!--配置⾃动代理--> <bean id="myBeanTypeAutoProxyCreator" class="com.ysl.access.proxy.BeanTypeAutoProxyCreator">

<!--⽗类属性--> <property name="interceptorNames">

<list>

<value>myMethodInterceptor</value> </list>

</property> </bean>

Demo完整代码(数据库⽂件在resources⽬录下)：htps:/github.com/andus-top/columns-ac s

单纯个⼈设计，应该有很多不⾜，有不好的地⽅可以指出，或者有更好的想法可以评论，⼀起交流。 参考：

htps:/blog.csdn.net/lilongjiu/article/details/78047051

作者：andus 链接：htps:/ w.jianshu.com/p/89d39625c2 来源：简书 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

