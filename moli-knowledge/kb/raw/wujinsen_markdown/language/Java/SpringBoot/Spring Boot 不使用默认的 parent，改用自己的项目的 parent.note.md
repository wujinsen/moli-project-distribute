在初学 boot时，官⽅示例中，都是让我们继承⼀个spring的 spring-bot-starter-parent 这个 parent： <parent>

# spring

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-parent</artifactId> <version>1.5.1.RELEASE</version>

</parent>

<dependencies>

<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-web</artifactId>

</dependency> </dependencies>

但是，⼀般情况下，在我们⾃⼰的项⽬中，会定义⼀下⾃⼰的 parent 项⽬，这种情况下，上⾯的这种做法就 ⾏不通了。那么，该如何来做呢？其实，在spring的官⽹也给出了变通的⽅法的：

在我们⾃⼰ parent 项⽬中，加下下⾯的声明

<dependencyManagement> <dependencies>

<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-dependencies</artifactId> <version>1.5.1.RELEASE</version> <type>pom</type> <scope>import</scope>

</dependency> </dependencies> </dependencyManagement>

请注意，它的 type 是 pom，scope 是 import，这种类型的 dependency 只能 在 dependencyManagement 标签中声明。

然后，把我们项⽬中的 ⼦项⽬ 中，parent 的声明，修改为我们⾃⼰项⽬的 parent 项⽬就可以了，⽐ 如，我的是：

<parent> <groupId>org.test</groupId> <artifactId>spring</artifactId> <version>0.1-SNAPSHOT</version>

</parent>

有⼀点，需要注意⼀下。 在 ⼦项⽬ 的 dependencies 中，不需要(也不能)再次添加对 spring-bot-dependencies 的声明了，否则 ⼦项⽬ 将⽆法编译通过。 即，在 ⼦项⽬ 中，下⾯的配置是多余的：

<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-dependencies</artifactId>

</dependency>

为什么会这个样⼦呢？ 因为 spring-bot-dependencies 根本就没有对应的jar包，它只是⼀个 pom 配置，可以去 看 ⼀下。 它⾥⾯定义了 ⾮常多 的依赖声明。 所以，有了它之后，我们在 ⼦项⽬ 中使⽤到的相关依赖，就不需要声明version了，如：

maven仓库

<dependencies>

<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-web</artifactId>

</dependency>

<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-test</artifactId> <scope>test</scope>

</dependency> </dependencies>

如，spring-boot-starter-web 和 spring-boot-starter-test 在 spring-boot-dependencies 中的声明分 别为： <dependency>

<groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-web</artifactId> <version>1.5.1.RELEASE</version>

</dependency>

<dependency> <groupId>org.springframework.boot</groupId> <artifactId>spring-boot-starter-test</artifactId> <version>1.5.1.RELEASE</version> <exclusions>

<exclusion> <groupId>commons-logging</groupId> <artifactId>commons-logging</artifactId>

</exclusion> </exclusions>

</dependency>

参考⽂档

-

spring 官⽅⽂档

