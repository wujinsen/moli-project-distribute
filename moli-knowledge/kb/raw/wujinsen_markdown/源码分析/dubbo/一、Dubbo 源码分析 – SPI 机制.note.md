htps:/tech.souyunku.com/?p=580

# 1.简介

SPI 全称为 Service Provider Interface，是⼀种服务发现机制。SPI 的本质是将接⼝实现类的全限定名 配置在⽂件中，并由服务加载器读取配置⽂件，加载实现类。这样可以在运⾏时，动态为接⼝ 加载实 现类。正因此特性，我们可以很容易的通过 SPI 机制为我们的程序提供拓展功能。SPI 机制在第三⽅框 架中也有所应⽤，⽐如 Dubo 就是通过 SPI 机制加载所有的组件。不过，Dubo 并未使⽤ Java 原⽣ 的 SPI 机制，⽽是对其进⾏了增强，使其能够更好的满⾜需求。在 Dubo 中，SPI 是⼀个⾮常重要的 模块。如果⼤家想要学习 Dubo 的源码，SPI 机制务必弄懂。下⾯，我们先来了解⼀下 Java SPI 与 Dubo SPI 的使⽤⽅法，然后再来分析 Dubo SPI 的源码。

# 2.SPI 示例

- 2.1 Java SPI 示例


前⾯简单介绍了 SPI 机制的原理，本节通过⼀个示例来演示 JAVA SPI 的使⽤⽅法。⾸先，我们定义⼀ 个接⼝，名称为 Robot。

public interface Robot { void sayHello(); }

接下来定义两个实现类，分别为擎天柱 OptimusPrime 和⼤⻩蜂 Bumblebe。

public class OptimusPrime implements Robot {

@Override public void sayHello() {

System.out.println("Hello, I am Optimus Prime."); }

}

public class Bumblebee implements Robot {

@Override public void sayHello() {

System.out.println("Hello, I am Bumblebee."); }

}

接下来 META-INF/services ⽂件夹下创建⼀个⽂件，名称为 Robot 的全限定名 com.tianxiaobo.spi.Robot。⽂件内容为实现类的全限定的类名，如下：

com.tianxiaobo.spi.OptimusPrime com.tianxiaobo.spi.Bumblebee

做好了所需的准备⼯作，接下来编写代码进⾏测试。

public class JavaSPITest {

@Test public void sayHello() throws Exception {

ServiceLoader<Robot> serviceLoader = ServiceLoader.load(Robot.class); System.out.println("Java SPI"); serviceLoader.forEach(Robot::sayHello);

} }

最后来看⼀下测试结果，如下：

![image 1](<一、Dubbo 源码分析 – SPI 机制.note_images/imageFile1.png>)

从测试结果可以看出，我们的两个实现类被成功的加载，并输出了相应的内容。关于 Java SPI 的演示 先到这，接下来演示 Dubo SPI。

## 2.2 Dubo SPI 示例

Dubo 并未使⽤ Java SPI，⽽是重新实现了⼀套功能更强的 SPI 机制。Dubo SPI 的相关逻辑被封装 在了 ExtensionLoader 类中，通过 ExtensionLoader，我们可以加载指定的实现类。Dubo SPI 的实 现类配置放置在 META-INF/dubo 路径下，下⾯来看⼀下配置内容。

optimusPrime = com.tianxiaobo.spi.OptimusPrime bumblebee = com.tianxiaobo.spi.Bumblebee

与 Java SPI 实现类配置不同，Dubo SPI 是通过键值对的⽅式进⾏配置，这样我们就可以按需加载指 定的实现类了。另外，在测试 Dubo SPI 时，需要在 Robot 接⼝上标注 @SPI 注解。下⾯来演示⼀下 Dubo SPI 的使⽤⽅式：

public class DubboSPITest {

@Test public void sayHello() throws Exception {

ExtensionLoader<Robot> extensionLoader =

ExtensionLoader.getExtensionLoader(Robot.class); Robot optimusPrime = extensionLoader.getExtension("optimusPrime"); optimusPrime.sayHello(); Robot bumblebee = extensionLoader.getExtension("bumblebee"); bumblebee.sayHello();

} }

测试结果如下：

![image 2](<一、Dubbo 源码分析 – SPI 机制.note_images/imageFile2.png>)

演示完 Dubo SPI，下⾯来看看 Dubo SPI 对 Java SPI 做了哪些改进，以下内容引⽤⾄ Dubo 官⽅ ⽂档。

JDK 标准的 SPI 会⼀次性实例化扩展点所有实现，如果有扩展实现初始化很耗时，但如果没⽤上也 加载，会很浪费资源。

如果扩展点加载失败，连扩展点的名称都拿不到了。⽐如：JDK 标准的 ScriptEngine，通过 getName() 获取脚本类型的名称，但如果 RubyScriptEngine 因为所依赖的 jruby.jar 不存在，导致 RubyScriptEngine 类加载失败，这个失败原因被吃掉了，和 ruby 对应不起来，当⽤户执⾏ ruby 脚 本时，会报不⽀持 ruby，⽽不是真正失败的原因。

增加了对扩展点 IOC 和 AOP 的⽀持，⼀个扩展点可以直接 seter 注⼊其它扩展点。

在以上改进项中，第⼀个改进项⽐较好理解。第⼆个改进项没有进⾏验证，就不多说了。第三个改进 项是增加了对 IOC 和 AOP 的⽀持，这是什么意思呢？这⾥简单解释⼀下，Dubo SPI 加载完拓展实例 后，会通过该实例的 seter ⽅法解析出实例依赖项的名称。⽐如通过 setProtocol ⽅法名，可知道⽬ 标实例依赖 Protocal。知道了具体的依赖，接下来即可到 IOC 容器中寻找或⽣成⼀个依赖对象，并通 过 seter ⽅法将依赖注⼊到⽬标实例中。说完 Dubo IOC，接下来说说 Dubo AOP。Dubo AOP 是 指使⽤ Wraper 类（可⾃定义实现）对拓展对象进⾏包装，Wraper 类中包含了⼀些⾃定义逻辑，这 些逻辑可在⽬标⽅法前⾏前后被执⾏，类似 AOP。Dubo AOP 实现的很简单，其实就是个代理模式。 这个官⽅⽂档中有所说明，⼤家有兴趣可以查阅⼀下。 关于 Dubo SPI 的演示，以及与 Java SPI 的对⽐就先这么多，接下来加⼊源码分析阶段。

# 3. Dubo SPI 源码分析

上⼀章，我简单演示了 Dubo SPI 的使⽤⽅法。我们⾸先通过 ExtensionLoader 的 getExtensionLoader ⽅法获取⼀个 ExtensionLoader 实例，然后再通过 ExtensionLoader 的 getExtension ⽅法获取拓展类对象。这其中，getExtensionLoader ⽤于从缓存中获取与拓展类对应的 ExtensionLoader，若缓存未命中，则创建⼀个新的实例。该⽅法的逻辑⽐较简单，本章就不就⾏分析 了。下⾯我们从 ExtensionLoader 的 getExtension ⽅法作为⼊⼝，对拓展类对象的获取过程进⾏详细 的分析。

public T getExtension(String name) { if (name == null || name.length() == 0) throw new IllegalArgumentException("Extension name == null");

if ("true".equals(name)) { // 获取默认的拓展实现类 return getDefaultExtension();

} // Holder 仅⽤于持有⽬标对象，没其他什么逻辑 Holder<Object> holder = cachedInstances.get(name); if (holder == null) {

cachedInstances.putIfAbsent(name, new Holder<Object>()); holder = cachedInstances.get(name);

} Object instance = holder.get(); if (instance == null) {

synchronized (holder) { instance = holder.get(); if (instance == null) {

// 创建拓展实例，并设置到 holder 中 instance = createExtension(name); holder.set(instance);

} }

} return (T) instance;

}

上⾯代码的逻辑⽐较简单，⾸先检查缓存，缓存未命中则创建拓展对象。下⾯我们来看⼀下创建拓展 对象的过程是怎样的。

private T createExtension(String name) { // 从配置⽂件中加载所有的拓展类，形成配置项名称到配置类的映射关系 Class<?> clazz = getExtensionClasses().get(name); if (clazz == null) {

throw findException(name);

} try {

T instance = (T) EXTENSION_INSTANCES.get(clazz); if (instance == null) {

// 通过反射创建实例 EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance()); instance = (T) EXTENSION_INSTANCES.get(clazz);

} // 向实例中注⼊依赖 injectExtension(instance); Set<Class<?>> wrapperClasses = cachedWrapperClasses; if (wrapperClasses != null && !wrapperClasses.isEmpty()) {

// 循环创建 Wrapper 实例 for (Class<?> wrapperClass : wrapperClasses) {

// 将当前 instance 作为参数创建 Wrapper 实例，然后向 Wrapper 实例中注⼊属性值， // 并将 Wrapper 实例赋值给 instance instance = injectExtension(

(T) wrapperClass.getConstructor(type).newInstance(instance)); }

} return instance;

} catch (Throwable t) {

throw new IllegalStateException("..."); }

}

createExtension ⽅法的逻辑稍复杂⼀下，包含了如下的步骤：

- 1.
- 2.
- 3.
- 4.


通过 getExtensionClases 获取所有的拓展类 通过反射创建拓展对象 向拓展对象中注⼊依赖 将拓展对象包裹在相应的 Wraper 对象中

以上步骤中，第⼀个步骤是加载拓展类的关键，第三和第四个步骤是 Dubo IOC 与 AOP 的具体实 现。在接下来的章节中，我将会重点分析 getExtensionClases ⽅法的逻辑，以及简单分析 Dubo IOC 的具体实现。

## 3.1 获取所有的拓展类

我们在通过名称获取拓展类之前，⾸先需要根据配置⽂件解析出名称到拓展类的映射，也就是 Map<名 称, 拓展类>。之后再从 Map 中取出相应的拓展类即可。相关过程的代码分析如下：

private Map<String, Class<?>> getExtensionClasses() { // 从缓存中获取已加载的拓展类 Map<String, Class<?>> classes = cachedClasses.get(); if (classes == null) {

synchronized (cachedClasses) { classes = cachedClasses.get(); if (classes == null) {

// 加载拓展类 classes = loadExtensionClasses(); cachedClasses.set(classes);

} }

} return classes;

}

这⾥也是先检查缓存，若缓存未命中，则通过 synchronized 加锁。加锁后再次检查缓存，并判空。此 时如果 clases 仍为 nul，则加载拓展类。以上代码的写法是典型的双重检查锁，前⾯所分析的 getExtension ⽅法中有相似的代码。关于双重检查就说这么多，下⾯分析 loadExtensionClases ⽅法 的逻辑。

private Map<String, Class<?>> loadExtensionClasses() { // 获取 SPI 注解，这⾥的 type 是在调⽤ getExtensionLoader ⽅法时传⼊的 final SPI defaultAnnotation = type.getAnnotation(SPI.class); if (defaultAnnotation != null) {

String value = defaultAnnotation.value(); if ((value = value.trim()).length() > 0) {

// 对 SPI 注解内容进⾏切分 String[] names = NAME_SEPARATOR.split(value); // 检测 SPI 注解内容是否合法，不合法则抛出异常 if (names.length > 1) {

throw new IllegalStateException("..."); }

// 设置默认名称，cachedDefaultName ⽤于加载默认实现，参考 getDefaultExtension ⽅法 if (names.length == 1) {

cachedDefaultName = names[0]; }

} }

Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>(); // 加载指定⽂件夹配置⽂件 loadDirectory(extensionClasses, DUBBO_INTERNAL_DIRECTORY); loadDirectory(extensionClasses, DUBBO_DIRECTORY); loadDirectory(extensionClasses, SERVICES_DIRECTORY); return extensionClasses;

}

loadExtensionClases ⽅法总共做了两件事情，⼀是对 SPI 注解进⾏解析，⼆是调⽤ loadDirectory ⽅ 法加载指定⽂件夹配置⽂件。SPI 注解解析过程⽐较简单，⽆需多说。下⾯我们来看⼀下 loadDirectory 做了哪些事情。

private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir) { // fileName = ⽂件夹路径 + type 全限定名 String fileName = dir + type.getName(); try {

Enumeration<java.net.URL> urls; ClassLoader classLoader = findClassLoader(); if (classLoader != null) {

// 根据⽂件名加载所有的同名⽂件 urls = classLoader.getResources(fileName);

} else { urls = ClassLoader.getSystemResources(fileName);

} if (urls != null) {

while (urls.hasMoreElements()) { java.net.URL resourceURL = urls.nextElement(); // 加载资源 loadResource(extensionClasses, classLoader, resourceURL);

} }

} catch (Throwable t) {

logger.error("..."); }

}

loadDirectory ⽅法代码不多，理解起来不难。该⽅法先通过 clasLoader 获取所有资源链接，然后再 通过 loadResource ⽅法加载资源。我们继续跟下去，看⼀下 loadResource ⽅法的实现。

private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL resourceURL) { try {

BufferedReader reader = new BufferedReader( new InputStreamReader(resourceURL.openStream(), "utf-8"));

try { String line; // 按⾏读取配置内容 while ((line = reader.readLine()) != null) {

final int ci = line.indexOf('#'); if (ci >= 0) {

// 截取 # 之前的字符串，# 之后的内容为注释 line = line.substring(0, ci);

} line = line.trim(); if (line.length() > 0) {

try { String name = null; int i = line.indexOf('='); if (i > 0) {

// 以 = 为界，截取键与值。⽐如 dubbo=com.alibaba....DubboProtocol name = line.substring(0, i).trim(); line = line.substring(i + 1).trim();

} if (line.length() > 0) {

// 加载解析出来的限定类名 loadClass(extensionClasses, resourceURL,

Class.forName(line, true, classLoader), name); }

} catch (Throwable t) {

IllegalStateException e = new IllegalStateException("..."); }

} }

} finally {

reader.close(); }

} catch (Throwable t) {

logger.error("..."); }

}

loadResource ⽅法⽤于读取和解析配置⽂件，并通过反射加载类，最后调⽤ loadClas ⽅法进⾏其他 操作。loadClas ⽅法有点名不副实，它的功能只是操作缓存，⽽⾮加载类。该⽅法的逻辑如下：

private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL resourceURL, Class<?> clazz, String name) throws NoSuchMethodException {

if (!type.isAssignableFrom(clazz)) {

throw new IllegalStateException("..."); }

if (clazz.isAnnotationPresent(Adaptive.class)) { // 检测⽬标类上是否有 Adaptive 注解

if (cachedAdaptiveClass == null) { // 设置 cachedAdaptiveClass缓存 cachedAdaptiveClass = clazz;

} else if (!cachedAdaptiveClass.equals(clazz)) {

throw new IllegalStateException("..."); }

} else if (isWrapperClass(clazz)) { // 检测 clazz 是否是 Wrapper 类型 Set<Class<?>> wrappers = cachedWrapperClasses; if (wrappers == null) {

cachedWrapperClasses = new ConcurrentHashSet<Class<?>>(); wrappers = cachedWrapperClasses;

} // 存储 clazz 到 cachedWrapperClasses 缓存中 wrappers.add(clazz);

} else { // 程序进⼊此分⽀，表明是⼀个普通的拓展类 // 检测 clazz 是否有默认的构造⽅法，如果没有，则抛出异常 clazz.getConstructor(); if (name == null || name.length() == 0) {

// 如果 name 为空，则尝试从 Extension 注解获取 name，或使⽤⼩写的类名作为 name name = findAnnotationName(clazz); if (name.length() == 0) {

throw new IllegalStateException("..."); }

} // 切分 name String[] names = NAME_SEPARATOR.split(name); if (names != null && names.length > 0) {

Activate activate = clazz.getAnnotation(Activate.class); if (activate != null) {

// 如果类上有 Activate 注解，则使⽤ names 数组的第⼀个元素作为键， // 存储 name 到 Activate 注解对象的映射关系 cachedActivates.put(names[0], activate);

} for (String n : names) {

if (!cachedNames.containsKey(clazz)) { // 存储 Class 到名称的映射关系 cachedNames.put(clazz, n);

} Class<?> c = extensionClasses.get(n); if (c == null) {

// 存储名称到 Class 的映射关系

extensionClasses.put(n, clazz); } else if (c != clazz) {

throw new IllegalStateException("..."); }

} }

} }

如上，loadClas ⽅法操作了不同的缓存，⽐如 cachedAdaptiveClas、cachedWraperClases 和 cachedNames 等等。除此之外，该⽅法没有其他什么逻辑了，就不多说了。 到此，关于缓存类加载的过程就分析完了。整个过程没什么特别复杂的地⽅，⼤家按部就班的分析就 ⾏了，不懂的地⽅可以调试⼀下。接下来，我们来聊聊 Dubo IOC ⽅⾯的内容。

## 3.2 Dubo IOC

Dubo IOC 是基于 seter ⽅法注⼊依赖。Dubo ⾸先会通过反射获取到实例的所有⽅法，然后再遍历 ⽅法列表，检测⽅法名是否具有 seter ⽅法特征。若有，则通过 ObjectFactory 获取依赖对象，最后 通过反射调⽤ seter ⽅法将依赖设置到⽬标对象中。整个过程对应的代码如下：

private T injectExtension(T instance) { try {

if (objectFactory != null) { // 遍历⽬标类的所有⽅法 for (Method method : instance.getClass().getMethods()) {

// 检测⽅法是否以 set 开头，且⽅法仅有⼀个参数，且⽅法访问级别为 public if (method.getName().startsWith("set")

&& method.getParameterTypes().length == 1 && Modifier.isPublic(method.getModifiers())) { // 获取 setter ⽅法参数类型 Class<?> pt = method.getParameterTypes()[0]; try {

// 获取属性名 String property = method.getName().length() > 3 ?

method.getName().substring(3, 4).toLowerCase() +

method.getName().substring(4) : ""; // 从 ObjectFactory 中获取依赖对象 Object object = objectFactory.getExtension(pt, property); if (object != null) {

// 通过反射调⽤ setter ⽅法设置依赖 method.invoke(instance, object);

} } catch (Exception e) {

logger.error("..."); }

} }

} } catch (Exception e) { logger.error(e.getMessage(), e);

} return instance;

}

在上⾯代码中，objectFactory 变量的类型为 AdaptiveExtensionFactory，AdaptiveExtensionFactory 内部维护了⼀个 ExtensionFactory 列表，⽤于存储其他类型的 ExtensionFactory。Dubo ⽬前提供了 两种 ExtensionFactory，分别是 SpiExtensionFactory 和 SpringExtensionFactory。前者⽤于创建⾃适 应的拓展，关于⾃适应拓展，我将会在下⼀篇⽂章中进⾏说明。SpringExtensionFactory 则是到 Spring 的 IOC 容器中获取所需拓展，该类的实现并不复杂，⼤家⾃⾏分析源码，这⾥就不多说了。 Dubo IOC 的实现⽐较简单，仅⽀持 seter ⽅式注⼊。总的来说，逻辑简单易懂。

# 4.总结

本篇⽂章简单介绍了 Java SPI 与 Dubo SPI ⽤法与区别，并对 Dubo SPI 的部分源码进⾏了分析。 在 Dubo SPI 中还有⼀块重要的逻辑没有进⾏分析，那就是 Dubo SPI 的扩展点⾃适应机制。该机制 的逻辑较为复杂，我将会在下⼀篇⽂章中进⾏分析。好了，其他的就不多说了，本篇⽂件就先到这⾥ 了。

# 附录：Dubo 源码分析系列⽂章

⼀、Dubo 源码分析 – SPI 机制

⼆、Dubo 源码分析 – ⾃适应拓展原理

三、Dubo 源码分析 – 服务导出 四、Dubo 源码分析 – 服务引⽤ 五、Dubo 源码分析 – 集群容错之Directory

六、Dubo 源码分析 – 集群容错之 Router

七、Dubo 源码分析 – 集群容错之 Cluster

⼋、Dubo 源码分析 – 集群容错之 LoadBalance

