# Apache Kafka

⾸先，你需要安装 和 。 Kafka需要 Gradle 2.0 或更⾼的版本。 ⾄少 Java 7，以便⽀持 Java 7 和Java 8。 原⽂地址：

Gradle Java

htps:/github.com/apache/kafka

⾸先引导并下载

git clone git@github.com:apache/kafka.git

cd kafka_source_dir gradle

构建jar并运⾏它

./gradlew jar

构建源码jar

./gradlew srcJar

构建聚合javadoc

./gradlew aggregatedJavadoc

构建javadoc和scaladoc

./gradlew javadoc

./gradlew javadocJar # builds a javadoc jar for each module

./gradlew scaladoc

./gradlew scaladocJar # builds a scaladoc jar for each module

./gradlew docsJar # builds both (if applicable) javadoc and scaladoc jars for each module

运⾏单元/集成测试

./gradlew test # runs both unit and integration tests

./gradlew unitTest

./gradlew integrationTest

强制重新运⾏测试，⽆需变更代码

./gradlew cleanTest test

./gradlew cleanTest unitTest

./gradlew cleanTest integrationTest

运⾏指定的单元/集成测试

./gradlew -Dtest.single=RequestResponseSerializationTest core:test

在单元/集成测试中运⾏指定的测试⽅法

./gradlew core:test --tests kafka.api.ProducerFailureHandlingTest.testCannotSendToInternalTopic

./gradlew clients:test --tests org.apache.kafka.clients.MetadataTest.testMetadataUpdateWaitTime

使⽤log4j输出运⾏特定的单元/集成测试

在clients/src/test/resources/log4j.properties或core/src/test/resources/log4j.properties中变 更log4j设置

./gradlew -i -Dtest.single=RequestResponseSerializationTest core:test

## ⽣成测试覆盖率报告

为整个项⽬⽣成覆盖率报告：

./gradlew reportCoverage

⽣成单个模块的覆盖范围，即：

./gradlew clients:reportCoverage

## 构建⼀个⼆进制的 gzi ped tar 包

./gradlew clean

./gradlew releaseTarGz

如果您尚未设置签名密钥，上述命令将失败。 要绕过⼯件的签名，可以运⾏：

./gradlew releaseTarGz -x signArchives 发布⽂件可以在./core/build/distributions/内找到。

## 清除构建

./gradlew clean

## 在特定版本的Scala（2.1.x或2.12.x）上运⾏任务

请 注 意 ， 如 果 使 ⽤ 2.1.1以 外 的 版 本构 建 jar， 则 需 要设 置 SCALA_VERSION变 量 或 者 在 bin/kafka-runclass.sh中 更 改 它 以 运 ⾏ 快 速 启 动 。 您可以传递主要版本(例如2.1)或完整版本(例如2.1.1)：

./gradlew -PscalaVersion=2.11 jar

./gradlew -PscalaVersion=2.11 test

./gradlew -PscalaVersion=2.11 releaseTarGz

Scala 2.12.x 需要 Java 8.

## 为特定的项⽬运⾏任务

这⽤于 core, examples 和 clients

./gradlew core:jar

./gradlew core:test

## 列出所有gradle任务

./gradlew tasks

## IDE构建项⽬

请 注 意 ， 这 并 不 是 绝 对 必 要 的 （ 例 如 ， InteliJ IDEA对 Gradle项 ⽬ 有 良 好 的 内 置 ⽀ 持 ） 。

./gradlew eclipse

./gradlew idea eclipse任务配置使⽤${project_dir}/build_eclipse作为Eclipse的构建⽬录。 Eclipse的默认构建⽬录 (${project_dir}/bin)与Kafka的脚本⽬录冲突，我们不使⽤Gradle的构建⽬录来避免这个问题。

## 为所有的Scala版本和所有项⽬构建jar

./gradlew jarAll

## 运⾏所有scala版本和所有项⽬的单元/集成测试

./gradlew testAll

## 为所有scala版本构建⼆进制发布gzi ped tar包

./gradlew releaseTarGzAll

## 将所有版本的Scala和所有项⽬的jar发布到maven

./gradlew uploadArchivesAll 请注意这个⼯作，你应该创建/更新${GRADLE_USER_HOME}/gradle.properties (通 常, ~/.gradle/gradle.properties)并分配以下变量： mavenUrl= mavenUsername= mavenPassword= signing.keyId= signing.password= signing.secretKeyRingFile=

## 将流快速启动原型artifact发布到maven

对于Streams原始项⽬，不能使⽤gradle上传到maven; ⽽需要在quickstart⽂件夹中调⽤mvn deploy命 令: cd streams/quickstart mvn deploy 请注意，为此，您应该创建/更新⽤户maven设置（通常为${USER_HOME}/.m2/settings.xml）以分配以 下变量 <settings xmlns="https://maven.apache.org/SETTINGS/1.0.0"

xmlns:xsi="https://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="https://maven.apache.org/SETTINGS/1.0.0

https://maven.apache.org/xsd/settings-1.0.0.xsd">

... <servers> ... <server>

<id>apache.snapshots.https</id> <username>${maven_username}</username> <password>${maven_password}</password>

</server> <server>

<id>apache.releases.https</id> <username>${maven_username}</username> <password>${maven_password}</password>

</server>

... </servers>

...

## 将jar安装到本地的Maven存储库

./gradlew installAll

## 构建测试jar

./gradlew testJar

## 确定如何添加传递依赖关系

./gradlew core:dependencies --configuration runtime

## 确定是否可以更新依赖关系

./gradlew dependencyUpdates

## 运⾏代码质量检查

我们经常运⾏两个代码质量分析⼯具，findbugs和checkstyle。 Checkstyle Checkstyle在kafka执⾏⼀致的编码⻛格。可以使⽤以下⽅式运⾏checkstyle：

./gradlew checkstyleMain checkstyleTest

checkstyle警告将在⼦项⽬构建⽬录中的“reports/checkstyle/reports/main.html”和 “reports/checkstyle/reports/test.html”⽂件中找到。也会打印到控制台。 如果Checkstyle失败，构建 将失败。 Findbugs Findbugs使⽤静态分析来查找代码中的错误。您可以使⽤以下命令运⾏findbugs：

./gradlew findbugsMain findbugsTest -x test

findbugs警告将在⼦报表⽣成⽬录中的“reports/findbugs/main.html”和“reports/findbugs/test.html”⽂ 件中找到。 使⽤-PxmlFindBugsReport = true⽣成XML报告，⽽不是HTML。

## 常⻅的构建选项

应使⽤-P开关设置以下选项，例如./gradlew -PmaxParallelForks=1 test。

commitId：如果构建⽬的添加了本地提交，则将build comit ID设置为.git/HEAD可能不正确。

mavenUrl: 设置maven部署存储库的URL（file://path/to/repo可⽤于指向本地存储库）。

maxParallelForks: 限制每个任务的最⼤进程数。

showStandardStreams: 在控制台上显示测试JVM的标准错误和标准错误。

skipSigning: 跳过artifacts的签名。

testLoggingEvents: 单元测试事件要记录，⽤逗号分隔。 例如./gradlew PtestLoggingEvents=started,passed,skipped,failed test。

xmlFindBugsReport: 启⽤findBugs的XML报告。同时会禁⽤HTML报告，因为⼀次只能启⽤⼀个。

作者：半兽⼈ 链接：htps:/ w.orchome.com/621 来源：OrcHome 著作权归作者所有。商业转载请联系作者获得授权，⾮商业转载请注明出处。

