htps:/blog.csdn.net/hxcaifly/article/details/86307213

⽂章⽬录

前⾔

- 1. 克隆Flink源码

- 2. 直接进⾏打包编译

- 3. 在intelij IDE编译

- 4. 最后验证


# 前⾔

最近在预研Flink Gely源码中，给Gely源码添加了⼏个功能点，想打包发布，应⽤在⾃⼰的项⽬中部 署。然⽽源码编译的过程中，不是⼀帆⻛顺了，踩了些坑，所以本⽂记录了⼀次源码编译的过程。

# 1. 克隆Flink源码

⾸先，把Flink源码克隆到本地： git clone htps:/github.com/apache/flink.git

1

# 2. 直接进⾏打包编译

执⾏： mvn clean package -DskipTests

1

报错了，有些包在htp:/maven.aliyun.com/nexus/content/groups/public下载不到。 因为阿⾥云的镜像有些包估计还不全⾯。所以需要修改maven的setings.xml⽂件，⽤国际化的镜像来 做为maven的公共库：

<miror>

<id>UK</id> <mirorOf>central</mirorOf> <name>UK Central</name> <url>htp:/uk.maven.org/maven2</url>

</miror>

- 1

- 2

- 3


- 4

- 5

- 6


注意这⾥还需要做步操作，删除mavenReposity/org/apache/flink⽬录。 然后再⼀次执⾏编译，报错了，错误⽇志：

Failed to execute goal org.apache.maven.plugins:maven-checkstyle-plugin:2.17

1

错误⽇志消息很明显，是maven-checkstyle-plugin出错了。 maven checkstyle plugin的简单介绍 为了在提交代码之前做⼀些必要的代码检查，我们需要使⽤⼀些⼯具来辅助我们的⼯作⸺对于maven ⼯程我们可以使⽤maven checkstyle plugin。 执⾏： mvn checkstyle:checkstyle

1

命令检查⼯程是否满⾜checkstyle。 采取对策：就是把根⽬录flink-release-1.7⾥的pom.xml⽂件的这部分注释掉：

<!-<plugin> <groupId>org.apache.maven.plugins</groupId> <artifactId>maven-checkstyle-plugin</artifactId> <version>2.17</version> <dependencies>

<dependency> <groupId>com.pupycrawl.tols</groupId> <artifactId>checkstyle</artifactId> <version>8.9</version>

</dependency> </dependencies> <executions>

<execution> <id>validate</id> <phase>validate</phase> <goals>

<goal>check</goal> </goals>

</execution> </executions> <configuration>

<supresionsLocation>/tols/maven/supresions.xml</supresionsLocation> <includeTestSourceDirectory>true</includeTestSourceDirectory> <configLocation>/tols/maven/checkstyle.xml</configLocation> <logViolationsToConsole>true</logViolationsToConsole> <failOnViolation>true</failOnViolation>

</configuration> </plugin>->

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9


10

- 1

- 12

- 13

- 14

- 15

- 16

- 17

- 18

- 19

- 20

- 21


- 2


- 23

- 24

- 25

- 26

- 27

- 28


# 3. 在intellij IDE编译

Flink的编译需要scala环境。因为我的电脑没有安装全局的scala环境。所以我才⽤IDE来编译，在IDE⾥ 安装scala插件即可。 第⼀步：将整个Flink项⽬导⼊IDE。 第⼆步：在IDE⾥执⾏maven的instal指令。报错了： Failed to execute goal org.apache.maven.plugins . There aretest failures

- 1

- 2


这是因为某些模块的test⽤例测试运⾏失败，导致我们的编译终⽌了。 解决对策：在flink-parent的pom.xml配上：

<build> <plugins>

<plugin> <groupId>org.apache.maven.plugins</groupId> <artifactId>maven-surefire-plugin</artifactId> <configuration>

<testFailureIgnore>true</testFailureIgnore> </configuration>

</plugin> </plugins>

</build>

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8

- 9

- 10 1


然后还有个错误，就是license问题，因为我在源码⾥添加了些⽂件，这些⽂件是没有Licensed to the Apache Software Foundation (ASF)注释的，然后源码编译时，报license错误。那么怎么解决呢？ 解决对策：注释掉flink-parent的pom.xml⽂件的如下部分：

<!-

<plugin> <groupId>org.apache.rat</groupId> <artifactId>apache-rat-plugin</artifactId> <version>0.12</version> <inherited>false</inherited> <executions>

<execution> <phase>verify</phase> <goals>

<goal>check</goal> </goals>

</execution> </executions> <configuration>

<excludeSubProjects>false</excludeSubProjects> <numUnaprovedLicenses>0</numUnaprovedLicenses> <licenses>

<license

implementation="org.apache.rat.analysis.license.SimplePaternBasedLicense"> <licenseFamilyCategory>AL2 </licenseFamilyCategory> <licenseFamilyName>Apache License

2.0</licenseFamilyName>

<notes /> <paterns>

<patern>Licensed to the Apache Software Foundation (ASF) under one</patern>

</paterns>

</license> </licenses> <licenseFamilies>

<licenseFamily implementation="org.apache.rat.license.SimpleLicenseFamily">

<familyName>Apache License 2.0</familyName> </licenseFamily>

</licenseFamilies>

<excludes>

<exclude>*/.*/*</exclude> <exclude>*/*.prefs</exclude> <exclude>*/*.log</exclude>

<exclude>docs/*/jquery*</exclude> <exclude>docs/*/botstrap*</exclude> <exclude>docs/Gemfile.lock</exclude> <exclude>docs/ruby2/Gemfile.lock</exclude> <exclude>docs/img/*.svg</exclude> <exclude>*/docs/page/font-awesome/*</exclude> <exclude>*/resources/*/font-awesome/*</exclude> <exclude>*/resources/*/jquery*</exclude> <exclude>*/resources/*/botstrap*</exclude> <exclude>flink-clients/src/main/resources/web-

docs/js/*d3.js</exclude>

<exclude>*/packaged_licenses/LICENSE.*.txt</exclude> <exclude>*/licenses/LICENSE*</exclude> <exclude>*/licenses-binary/LICENSE*</exclude>

<exclude>flink-runtime-web/webdashboard/package.json</exclude>

<exclude>flink-runtime-web/web-dashboard/bower.json</exclude>

<exclude>flink-runtime-web/web-dashboard/vendor-local/d3timeline.js</exclude>

<exclude>flink-runtime-web/webdashboard/asets/fonts/FontAwesome.otf</exclude>

<exclude>flink-runtime-web/webdashboard/asets/fonts/fontawesome*</exclude>

<exclude>flink-runtime-web/webdashboard/asets/images/manifest.json</exclude>

<exclude>flink-runtime-web/web-dashboard/asets/images/safaripi ned-tab.svg</exclude>

<exclude>flink-runtime-web/web-dashboard/web/*</exclude>

<exclude>flink-runtime-web/web-dashboard/node_modules/* </exclude>

<exclude>flink-runtime-web/web-dashboard/bower_components/* </exclude>

<exclude>flink-runtime-web/web-dashboard/tmp/*</exclude>

<exclude>*/src/test/resources/*-data</exclude> <exclude>flink-

tests/src/test/resources/testdata/terainput.txt</exclude>

<exclude>flink-formats/flink-avro/src/test/resources/flink_1kryo_registrations</exclude>

<exclude>flink-runtime/src/test/resources/flink_1kryo_registrations</exclude>

<exclude>flink-core/src/test/resources/kryo-serializer-configsnapshot-v1</exclude>

<exclude>flink-formats/flink-

avro/src/test/resources/avro/*.avsc</exclude> <exclude>out/test/flink-avro/avro/user.avsc</exclude> <exclude>flink-libraries/flink-

table/src/test/scala/resources/*.out</exclude> <exclude>flink-yarn/src/test/resources/krb5.keytab</exclude> <exclude>flink-end-to-end-tests/test-scripts/test-data/*</exclude> <exclude>flink-end-to-end-tests/test-scripts/docker-hadop-

secure-cluster/config/keystore.jks</exclude>

<exclude>*/src/test/resources/*-snapshot</exclude>

<exclude>*/src/test/resources/*.snapshot</exclude> <exclude>*/src/test/resources/*-savepoint</exclude> <exclude>flink-core/src/test/resources/serialized-kryo-serializer-

1.3</exclude>

<exclude>flink-core/src/test/resources/type-without-avroserialized-using-kryo</exclude>

<exclude>flink-formats/flink-avro/src/test/resources/flink-1.4serializer-java-serialized</exclude>

<exclude>flink-end-to-end-tests/flink-state-evolutiontest/src/main/java/org/apache/flink/avro/generated/*</exclude>

<exclude>flink-end-to-end-tests/flink-state-evolutiontest/savepoints/*</exclude>

<exclude>flink-formats/flinkavro/src/test/resources/testdata.avro</exclude>

<exclude>flink-formats/flinkavro/src/test/java/org/apache/flink/formats/avro/generated/*.java</exclude> <exclude>flink-formats/flinkparquet/src/test/java/org/apache/flink/formats/parquet/generated/*.java</exclude>

<exclude>flink-formats/flink-parquet/src/test/resources/avro/* </exclude>

<exclude>flink-libraries/flinkpython/src/test/python/org/apache/flink/python/api/data_csv</exclude> <exclude>flink-libraries/flinkpython/src/test/python/org/apache/flink/python/api/data_text</exclude>

<exclude>flinkruntime/src/test/java/org/apache/flink/runtime/io/network/bufer/AbstractByteBufTest.java</exclu de>

<exclude>*/flink-bin/conf/slaves</exclude> <exclude>*/flink-bin/conf/masters</exclude>

<exclude>*/README.md</exclude> <exclude>.github/*</exclude>

<exclude>*/*.iml</exclude>

<exclude>flink-quickstart/*/testArtifact/goal.txt</exclude>

<exclude>out/*</exclude> <exclude>*/target/*</exclude> <exclude>docs/content/*</exclude> <exclude>*/scalastyle-output.xml</exclude> <exclude>build-target/*</exclude> <exclude>docs/_includes/generated/*</exclude>

<exclude>tols/artifacts/*</exclude> <exclude>tols/flink*/*</exclude>

<exclude>tols/releasing/release/*</exclude>

<exclude>apache-maven-3.2.5/*</exclude>

<exclude>*/.idea/*</exclude>

<exclude>flink-end-to-end-tests/flink-confluent-schemaregistry/src/main/java/example/avro/*</exclude>

<exclude>flink-end-to-end-tests/flink-datastream-alroundtest/src/main/java/org/apache/flink/streaming/tests/avro/*</exclude>

<exclude>flink-jepsen/store/*</exclude> <exclude>flink-jepsen/docker/id_rsa*</exclude> <exclude>flink-jepsen/docker/nodes</exclude>

</excludes> </configuration>

</plugin>->

- 1

- 2

- 3

- 4

- 5

- 6

- 7

- 8


- 9

- 10


- 1

- 12

- 13

- 14

- 15

- 16

- 17

- 18

- 19

- 20

- 21


- 2

- 23

- 24

- 25

- 26

- 27

- 28

- 29

- 30

- 31

- 32


- 3

- 34

- 35

- 36

- 37

- 38

- 39

- 40

- 41

- 42

- 43


- 4


- 45


- 46

- 47

- 48

- 49

- 50

- 51

- 52

- 53

- 54


- 5

56 57 58 59 60 61 62 63 64 65

- 6

- 67

- 68

- 69

- 70

- 71

- 72

- 73

- 74

- 75

- 76


- 7


- 78

- 79

- 80

- 81

- 82


- 83

- 84

- 85

- 86

- 87


- 8

- 89

- 90

- 91

- 92

- 93

- 94

- 95

- 96

- 97

- 98


- 9

- 10


- 101

- 102

- 103

- 104

- 105

- 106

- 107

- 108

- 109 10


1

- 12

- 13

- 14

- 15

- 16

- 17

- 18

- 19


- 120

- 121


- 12

- 123

- 124

- 125

- 126

- 127

- 128

- 129

- 130

- 131

- 132


- 13


- 134

- 135

- 136

- 137

- 138

- 139

- 140

- 141

- 142


最后在IDE对flink-parent执⾏： mvninstal -DskipTests

1

之后。build suces。

# 4. 最后验证

我在某些模块添加的Demo类，然后执⾏instal编译。在mavenReposity/org/apache/flink仓库⽬录⾥⾯ ⽣成的jar⾥包含了我的Demo.clas⽂件。 备注：这个其实还有些细⼩的坑，我这⾥没有记录下来，在编译过程中，要注意调试。以后⼀定随时 做好笔记，把细节流程记录下来，防⽌下次在同⼀个坑中消耗时间。

