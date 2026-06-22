解决⽅案⼀: 或者是把版本直接写成0.0.1进⾏打包deploy SNAPSHOT版本是快照保本–不稳定，尚处于开发中的版本，maven会⾃动加时间戳 RELEASE 发布版本–稳定版本

解决⽅案⼆： 找到你nexus的配置⽂件 把其中的unique改为non-unique，如下 <snapshotVersionBehavior>non-unique</snapshotVersionBehavior>

解决⽅案三:(试过没效果)

打包后依赖的包全部是以时间戳存在的，这样在做增量包的时候就必须要删除服务器上的原⽂件，⽽ 不能直接覆盖替换，想要打成SNAPSHOT的包需要执⾏以下命令： mvn clean deploy -DuseUniqueVersions=false

解决⽅案四:(试了没效果)

- 1.
- 2.
- 3.
- 4.
- 5.
- 6.
- 7.
- 8.
- 9.
- 10.
- 11.


<plugin> <artifactId>maven-war-plugin</artifactId> <version>3.1.0</version> <configuration>

<archive> <manifest>

<useUniqueVersions>false</useUniqueVersions> </manifest>

</archive>

</configuration> </plugin>

