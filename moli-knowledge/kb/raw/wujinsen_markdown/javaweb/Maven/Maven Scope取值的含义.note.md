maven依赖关系中Scope的作⽤

Dependency Scope

在POM 4中，<dependency>中还引⼊了<scope>，它主要管理依赖的部署。⽬前<scope>可以使⽤5个 值：

- * compile，缺省值，适⽤于所有阶段，会随着项⽬⼀起发布。

- * provided，类似compile，期望JDK、容器或使⽤者会提供这个依赖。如servlet.jar。

- * runtime，只在运⾏时使⽤，如JDBC驱动，适⽤运⾏和测试阶段。

- * test，只在测试时使⽤，⽤于编译和运⾏测试代码。不会随项⽬发布。

- * system，类似provided，需要显式提供包含依赖的jar，Maven不会在Repository中查找它。 依赖范围控制哪些依赖在哪些classpath 中可⽤，哪些依赖包含在⼀个应⽤中。让我们详细看⼀下每⼀种范 围： compile （编译范围） compile是默认的范围；如果没有提供⼀个范围，那该依赖的范围就是编译范围。编译范围依赖在所有的 classpath 中可⽤，同时它们也会被打包。 provided （已提供范围） provided 依赖只有在当JDK 或者⼀个容器已提供该依赖之后才使⽤。例如， 如果你开发了⼀个web 应⽤，你 可能在编译 classpath 中需要可⽤的Servlet API 来编译⼀个servlet，但是你不会想要在打包好的WAR 中包含 这个Servlet API；这个Servlet API JAR 由你的应⽤服务器或者servlet 容器提供。已提供范围的依赖在编译 classpath （不是运⾏时）可⽤。它们不是传递性的，也不会被打包。 runtime （运⾏时范围） runtime 依赖在运⾏和测试系统的时候需要，但在编译的时候不需要。⽐如，你可能在编译的时候只需要 JDBC API JAR，⽽只有在运⾏的时候才需要JDBC 驱动实现。 test （测试范围） test范围依赖 在⼀般的编译和运⾏时都不需要，它们只有在测试编译和测试运⾏阶段可⽤。 system （系统范围） system范围依赖与provided 类似，但是你必须显式的提供⼀个对于本地系统中JAR ⽂件的路径。这么做是为 了允许基于本地对象编译，⽽这些对象是系统类库的⼀部分。这样的构件应该是⼀直可⽤的，Maven 也不会 在仓库中去寻找它。如果你将⼀个依赖范围设置成系统范围，你必须同时提供⼀个 systemPath 元素。注意该 范围是不推荐使⽤的（你应该⼀直尽量去从公共或定制的 Maven 仓库中引⽤依赖）。


