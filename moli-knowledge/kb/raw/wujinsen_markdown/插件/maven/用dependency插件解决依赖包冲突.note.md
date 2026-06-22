## htps:/segmentfault.com/a/19 014938685

偶然翻阅了下 maven dependency 插件的官⽅⽂档，颇有收获，记录⼀下。可能有些标题党的嫌疑，并没有 具体介绍怎么解决依赖冲突问题，不过既然你都打印出了依赖树，冲突关系已然在树中显示的清清楚楚了。

# 依赖树

dependency:tree ⼤概是⽤的最多的功能，⽤来排查依赖冲突，没有指定任何参数执⾏时打印是所有依赖信 息，信息量略⼤，可以通过includes参数指定想看哪些依赖，也可以通过excludes参数指定不想看的。 includes和excludes可以配合使⽤。举个例⼦吧： # 只想看依赖树中包含 groupId 为 javax.serlet 的枝⼲ mvn dependency:tree -Dincludes=javax.servlet # 不想看依赖树中包含 groupId 为 javax.serlet 的枝⼲ mvn dependency:tree -Dexcludes=javax.servlet 参数的格式(patern)定义如下: [groupId]:[artifactId]:[type]:[version] 每个部分（冒号分割的部分）是⽀持*通配符的，如果要指定多个格式则可以⽤,分割，如： mvn dependency:tree -Dincludes=javax.servlet,org.apache.* 默认情况下 dependency:tree 打印出来的是 maven 解决了冲突后的树（解决冲突的策略是：就近原则，即离根近的依赖被采纳），通过指定 -Dverbose 参数则可以显示原始的依赖树，让你显式地看出某个包都在哪些枝⼲上出现了。

# 清空被本地仓库(purge-local-repository)

有时候打包时会遇到⼀些莫名其妙的问题，百思不得其解，但是清空本地仓库后问题就解决了（就像重启电 脑⼀般神奇）。之前都是去本地私服⽬录把某个groupId对应的jar包都删了或者 把所有的都给删了，难免删了⼀些⽆辜的依赖，dependency 插件提供了⼀个goal可以⽅便的删除本地⽬录下该项⽬依赖的jar包：

mvn dependency:purge-local-repository

# 复制依赖或某些jar包到指定⽬录

使⽤ dependency 的 copy-depenecis goal 把依赖的jar复制到指定⽬录前，在pom⽂件配置如下：

<id>copy-dependencies</id> <phase>package</phase> <goals>

<goal>copy-dependencies</goal> </goals> <configuration>

<outputDirectory>/path/to/dest</outputDirectory> <overWriteReleases>false</overWriteReleases> <overWriteSnapshots>false</overWriteSnapshots> <overWriteIfNewer>true</overWriteIfNewer>

</configuration> </execution>

</executions> </plugin>

</plugins>

</build> [...] </project>

dependency:copy-dependencies

更多参数信息参⻅ 如果只想复制极少的⼏个jar包到指定⽬录的话可以使⽤ copy goal:

<id>copy</id> <phase>package</phase> <goals>

<goal>copy</goal> </goals> <configuration>

<artifactItems>

<artifactItem> <groupId>junit</groupId> <artifactId>junit</artifactId> <version>3.8.1</version>

</artifactItem> </artifactItems> <outputDirectory>/path/to</outputDirectory> <overWriteReleases>false</overWriteReleases> <overWriteSnapshots>true</overWriteSnapshots>

</configuration> </execution>

</executions> </plugin>

</plugins>

</build> [...] </project>

据说analyze goal 可以分析出声明的依赖中哪些未被使⽤，未被声明的包中哪些被依赖了，试了下好像并不 是很好使。

