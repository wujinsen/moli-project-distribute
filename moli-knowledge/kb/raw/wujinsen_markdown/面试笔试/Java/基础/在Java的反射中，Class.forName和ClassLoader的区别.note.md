# 前⾔

最近在⾯试过程中有被问到，在Java反射中Clas.forName()加载类和使⽤ClasLoader加载类的区 别。当时没有想出来后来⾃⼰研究了⼀下就写下来记录⼀下。

# 解释

在java中Clas.forName()和ClasLoader都可以对类进⾏加载。ClasLoader就是遵循双亲委派模型最 终调⽤启动类加载器的类加载器，实现的功能是“通过⼀个类的全限定名来获取描述此类的⼆进制字节 流”，获取到⼆进制流后放到JVM中。Clas.forName()⽅法实际上也是调⽤的CLasLoader来实现的。 Clas.forName(String clasName)；这个⽅法的源码是

@CalerSensitive publicstaticClas<?> forName(String clasName)

throwsClasNotFoundException { Clas<?>caler =Reflection.getCalerClas(); return forName0(clasName,true,ClasLoader.getClasLoader(caler),caler);

} 最后调⽤的⽅法是forName0这个⽅法，在这个forName0⽅法中的第⼆个参数被默认设置为了true，这个参数代表是否对加载的类进⾏ 初始化，设置为true时会类进⾏初始化，代表会执⾏类中的静态代码块，以及对静态变量的赋值等操作。 也可以调⽤Clas.forName(String name, bolean initialize,ClasLoader loader)⽅法来⼿动选择在加载类的时候是否要对类进⾏初始 化。Clas.forName(String name, bolean initialize,ClasLoader loader)的源码如下：

/* @param name fuly qualified name of the desired clas

- * @param initialize if {@code true} the clas wil be initialized.

- * Se Section 12.4 of <em>The Java Language Specification</em>.

- * @param loader clas loader from which the clas must be loaded

- * @return clas object representing the desired clas

*

- * @exception LinkageError if the linkage fails

- * @exception ExceptionInInitializerError if the initialization provoked

- * by this method fails

- * @exception ClasNotFoundException if the clas canot be located by

- * the specified clas loader

*

- * @se java.lang.Clas#forName(String)

- * @se java.lang.ClasLoader

- * @since 1.2

- */ @CalerSensitive publicstaticClas<?> forName(String name,bolean initialize,


ClasLoader loader) throwsClasNotFoundException

{

Clas<?>caler =nul; SecurityManager sm =System.getSecurityManager(); if (sm !=nul) {

/ Reflective cal to get caler clas is only neded if a security manager / is present. Avoid the overhead of making this cal otherwise.

caler =Reflection.getCalerClas(); if (sun.misc.VM.isSystemDomainLoader(loader) {

ClasLoader cl =ClasLoader.getClasLoader(caler); if (!sun.misc.VM.isSystemDomainLoader(cl) {

sm.checkPermi sion(

SecurityConstants.GET_CLASLOADER_PERMI SION); }

}

} return forName0(name, initialize, loader,caler);

} 源码中的注释只摘取了⼀部分，其中对参数initialize的描述是：if {@code true} the clas wil be initialized.意思就是说：如果参数为 true，则加载的类将会被初始化。

举例

下⾯还是举例来说明结果吧： ⼀个含有静态代码块、静态变量、赋值给静态变量的静态⽅法的类

publicclasClasForName {

/静态代码块 static {

System.out.println("执⾏了静态代码块"); }

/静态变量 privatestaticString staticFiled = staticMethod();

/赋值静态变量的静态⽅法

publicstaticString staticMethod(){ System.out.println("执⾏了静态⽅法"); return"给静态字段赋值了";

}

} 使⽤Clas.forName()的测试⽅法： @Test publicvoid test45(){

try { ClasLoader.getSystemClasLoader().loadClas("com.eurekaclient2.client2.ClasForName"); System.out.println(" # -结束符 - #");

}catch (ClasNotFoundException e) {

e.printStackTrace(); }

} 运⾏结果： 执⾏了静态代码块 执⾏了静态⽅法

# -结束符 - # 使⽤ClasLoader的测试⽅法： @Test publicvoid test45(){

try { ClasLoader.getSystemClasLoader().loadClas("com.eurekaclient2.client2.ClasForName"); System.out.println(" # -结束符 - #");

}catch (ClasNotFoundException e) {

e.printStackTrace(); }

} 运⾏结果：

# -结束符 - # 根据运⾏结果得出Clas.forName加载类时将类进了初始化，⽽ClasLoader的loadClas并没有对类进⾏初始化，只是把类加载到了虚 拟机中。

应⽤场景

在我们熟悉的Spring框架中的IOC的实现就是使⽤的ClasLoader。 ⽽在我们使⽤JDBC时通常是使⽤Clas.forName()⽅法来加载数据库连接驱动。这是因为在JDBC规范中明确要求Driver(数据库驱动)类 必须向DriverManager注册⾃⼰。 以MySQL的驱动为例解释：

publicclasDriverextendsNonRegisteringDriverimplements java.sql.Driver { / ~ Static fields/initializers / -

/ / Register ourselves with the DriverManager /

static { try {

java.sql.DriverManager.registerDriver(newDriver(); }catch (SQLException E) {

thrownewRuntimeException("Can't register driver!"); }

}

/ ~ Constructors / -

/*

- * Construct a new driver and register it with DriverManager

*

- * @throws SQLException

- * if a database error ocurs.

- */ publicDriver()throwsSQLException {


/ Required for Clas.forName().newInstance() }

} 我们看到Driver注册到DriverManager中的操作写在了静态代码块中，这就是为什么在写JDBC时使⽤Clas.forName()的原因了。 好了，今天就写到这了，最近在⾯试，遇到了很多问题，也学习了不少，虽然很累，但是也让⼈成⻓了不少，毕竟⾯试就是⼀个脱⽪的 过程，会遇到各种企业各种⾯试官各种问题，各种场景。给⾃⼰加油吧，找⼀个最少能让⾃⼰⼲个⼏年的公司，别总是让我遇到⼯作了 没多久公司就垮掉的这种就⾏了。要不我也很⽆奈啊。

等找到⼯作后，会总结⾃⼰⾯经的。

