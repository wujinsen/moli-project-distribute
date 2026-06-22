Spring Bot在编译的时候，是有默认JDK版本的，如果我们期望使⽤我们要的JDK版本的话，那么要 怎么配置呢？ 这个只需要修改pom.xml⽂件的<build>- <plugins>加⼊⼀个plugin即可。 <plugin> <artifactId>maven-compiler-plugin</artifactId> <configuration> <source>1.8</source> <target>1.8</target> </configuration> </plugin> 添加了plugin之后，需要右键Maven à Update Projects,这时候你可以看到⼯程根⽬录下的JRE System Library 版本

