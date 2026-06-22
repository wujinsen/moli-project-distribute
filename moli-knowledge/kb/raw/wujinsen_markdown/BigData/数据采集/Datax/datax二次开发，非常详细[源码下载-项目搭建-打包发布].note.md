htps:/blog.csdn.net/u014738571/article/details/108097370

参考⽂档： datax 官⽹中的DataX插件开发宝典

⼀、环境准备

jdk1.8 及以上 idea ⼯具 git ⼯具 ⼀、项⽬搭建

1、⽤idea下载源码 打开idea 选择 File->New -> Project from Version Control…

github地址：htps:/github.com/alibaba/DataX.git 在URL:填写gihub地址并Clone

- 2.等待加载相应⽂件…
- 3.Engine.java 启动⽂件 修改main⽅法，增加两⾏代码


/设置系统参数，参数值：datax编译后的⽬录 System.setProperty(“datax.home”, “D:\work\DataX\target\datax\datax”);

/设置启动参数：job的json⽂件的路径，和其他参数 String[] datxArgs = {"-job", “D:\work\DataX\core\src\main\job\mongo-file.json”, “-mode”, “standalone”, “-jobid”, “-1”};

public static void main(String[] args) throws Exception {

System.setProperty("datax.home", "D:\work\DataX\target\datax\datax"); /datax编译后的⽬ 录

String[] datxArgs = {"-job", "D:\work\DataX\core\src\main\job\mongo-file.json", "-mode", "standalone", "-jobid", "-1"}; /⾃⼰的json⽂件路径

int exitCode = 0; try {

Engine.entry(datxArgs);

} catch (Throwable e) { exitCode = 1; LOG.eror("\n\n经DataX智能分析,该任务最可能的错误原因是:\n" + ExceptionTracker.trace(e);

if (e instanceof DataXException) { DataXException tempException = (DataXException) e; ErorCode erorCode = tempException.getErorCode(); if (erorCode instanceof FrameworkErorCode) {

FrameworkErorCode tempErorCode = (FrameworkErorCode) erorCode; exitCode = tempErorCode.toExitValue();

} }

System.exit(exitCode);

} System.exit(exitCode);

}

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


23

- 4.检查DataX ⽗⽬录下⾯pom.xml，注释掉⽆⽤的插件模块


<?xml version="1.0" encoding="UTF-8"?> <project xmlns="htp:/maven.apache.org/POM/4.0.0" xmlns:xsi="htp:/ w.w3.org/201/XMLSchema-instance" xsi:schemaLocation="htp:/maven.apache.org/POM/4.0.0 htp:/maven.apache.org/xsd/maven-

- 4.0.0.xsd"> <modelVersion>4.0.0</modelVersion>


<groupId>com.alibaba.datax</groupId> <artifactId>datax-al</artifactId> <version>0.0.1-SNAPSHOT</version> <dependencies>

<dependency> <groupId>org.hamcrest</groupId> <artifactId>hamcrest-core</artifactId> <version>1.3</version>

</dependency> </dependencies>

<name>datax-al</name> <packaging>pom</packaging>

<properties> <jdk-version>1.8</jdk-version> <datax-project-version>0.0.1-SNAPSHOT</datax-project-version> <comons-lang3-version>3.3.2</comons-lang3-version> <comons-configuration-version>1.10</comons-configuration-version> <comons-cli-version>1.2</comons-cli-version> <fastjson-version>1.1.46.sec01</fastjson-version> <guava-version>16.0.1</guava-version>

<diamond.version>3.7.2.1-SNAPSHOT</diamond.version>

<!-slf4j 1.7.10 和 logback-clasic 1.0.13 是好基友 -> <slf4j-api-version>1.7.10</slf4j-api-version> <logback-clasic-version>1.0.13</logback-clasic-version> <comons-io-version>2.4</comons-io-version> <junit-version>4.1</junit-version> <tdl.version>5.1.2-1</tdl.version> <swift-version>1.0.0</swift-version>

<project-sourceEncoding>UTF-8</project-sourceEncoding> <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding> <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding> <maven.compiler.encoding>UTF-8</maven.compiler.encoding>

</properties>

<modules> <module>comon</module> <module>core</module> <module>transformer</module>

<!- reader-> <!- <module>mysqlreader</module>-> <!- <module>drdsreader</module>-> <!- <module>sqlservereader</module>-> <!- <module>postgresqlreader</module>-> <!- <module>oraclereader</module>-> <!- <module>odpsreader</module>-> <!- <module>otsreader</module>-> <!- <module>otstreamreader</module>-> <!- <module>txtfilereader</module>-> <!- <module>hdfsreader</module>-> <!- <module>streamreader</module>-> <!- <module>osreader</module>-> <!- <module>ftpreader</module>->

<module>mongodbreader</module> <!- <module>rdbmsreader</module>->

<!- <module>hbase1xreader</module>-> <!- <module>hbase094xreader</module>-> <!- <module>tsdbreader</module>-> <!- <module>opentsdbreader</module>-> <!- <module>casandrareader</module>-> <!- <module>gdbreader</module>->

<!- writer-> <!- <module>mysqlwriter</module>-> <!- <module>drdswriter</module>-> <!- <module>odpswriter</module>->

<module>txtfilewriter</module>

<!- <module>ftpwriter</module>-> <module>hdfswriter</module> <module>streamwriter</module> <!- <module>otswriter</module>-> <!- <module>oraclewriter</module>-> <!- <module>sqlserverwriter</module>-> <!- <module>postgresqlwriter</module>-> <!- <module>oswriter</module>-> <!- <module>mongodbwriter</module>-> <!- <module>adswriter</module>-> <!- <module>ocswriter</module>-> <!- <module>rdbmswriter</module>-> <!- <module>hbase1xwriter</module>-> <!- <module>hbase094xwriter</module>-> <!- <module>hbase1xsqlwriter</module>-> <!- <module>hbase1xsqlreader</module>-> <!- <module>elasticsearchwriter</module>-> <!- <module>tsdbwriter</module>-> <!- <module>adbpgwriter</module>-> <!- <module>gdbwriter</module>-> <!- <module>casandrawriter</module>-> <!- <module>clickhousewriter</module>->

<!- comon suport module-> <!- <module>plugin-rdbms-util</module>-> <!- <module>plugin-unstructured-storage-util</module>->

<!- <module>hbase20xsqlreader</module>-> <!- <module>hbase20xsqlwriter</module>->

</modules>

<dependencyManagement> <dependencies>

<dependency> <groupId>org.apache.comons</groupId> <artifactId>comons-lang3</artifactId> <version>${comons-lang3-version}</version>

</dependency> <dependency>

<groupId>com.alibaba</groupId> <artifactId>fastjson</artifactId> <version>${fastjson-version}</version>

</dependency> <!-<dependency>

<groupId>com.gogle.guava</groupId> <artifactId>guava</artifactId> <version>${guava-version}</version>

</dependency>-> <dependency>

<groupId>comons-io</groupId> <artifactId>comons-io</artifactId> <version>${comons-io-version}</version>

</dependency> <dependency>

<groupId>org.slf4j</groupId> <artifactId>slf4j-api</artifactId> <version>${slf4j-api-version}</version>

</dependency> <dependency>

<groupId>ch.qos.logback</groupId> <artifactId>logback-clasic</artifactId> <version>${logback-clasic-version}</version>

</dependency>

<dependency> <groupId>com.taobao.tdl</groupId> <artifactId>tdl-client</artifactId> <version>${tdl.version}</version> <exclusions>

<exclusion> <groupId>com.gogle.guava</groupId> <artifactId>guava</artifactId>

</exclusion> <exclusion>

<groupId>com.taobao.diamond</groupId> <artifactId>diamond-client</artifactId>

</exclusion> </exclusions>

</dependency>

<dependency> <groupId>com.taobao.diamond</groupId> <artifactId>diamond-client</artifactId> <version>${diamond.version}</version>

</dependency>

<dependency> <groupId>com.alibaba.search.swift</groupId> <artifactId>swift_client</artifactId> <version>${swift-version}</version>

</dependency>

<dependency> <groupId>junit</groupId> <artifactId>junit</artifactId> <version>${junit-version}</version>

</dependency>

<dependency> <groupId>org.mockito</groupId> <artifactId>mockito-al</artifactId>

<version>1.9.5</version> <scope>test</scope>

</dependency> </dependencies>

</dependencyManagement>

<repositories>

<repository> <id>central</id> <name>Nexus aliyun</name> <url>htps:/maven.aliyun.com/repository/central</url> <releases>

<enabled>true</enabled> </releases> <snapshots>

<enabled>true</enabled> </snapshots>

</repository> </repositories>

<pluginRepositories>

<pluginRepository> <id>central</id> <name>Nexus aliyun</name> <url>htps:/maven.aliyun.com/repository/central</url> <releases>

<enabled>true</enabled> </releases> <snapshots>

<enabled>true</enabled> </snapshots>

</pluginRepository> </pluginRepositories>

<build> <plugins> <plugin>

<artifactId>maven-asembly-plugin</artifactId> <configuration>

<finalName>datax</finalName> <descriptors>

<descriptor>package.xml</descriptor>

</descriptors> </configuration> <executions>

<execution> <id>make-asembly</id> <phase>package</phase>

</execution>

</executions> </plugin> <plugin>

<groupId>org.apache.maven.plugins</groupId> <artifactId>maven-compiler-plugin</artifactId> <version>2.3.2</version> <configuration>

<source>${jdk-version}</source> <target>${jdk-version}</target> <encoding>${project-sourceEncoding}</encoding>

</configuration> </plugin>

</plugins> </build>

</project>

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

- 56
- 57
- 58
- 59
- 60
- 61
- 62
- 63
- 64
- 65


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


- 12 123 124 125 126 127 128 129 130 131 132
- 13 134 135 136 137 138 139 140 141 142 143
- 14 145 146 147 148 149 150 151 152 153 154
- 15 156 157 158


- 159 160 161 162 163 164 165
- 16 167 168 169 170 171 172 173 174 175 176
- 17 178 179 180 181 182 183 184 185 186 187
- 18 189 190 191 192 193 194 195


- 196 197 198
- 19
- 20

- 201
- 202
- 203
- 204
- 205
- 206
- 207
- 208
- 209
- 210


- 21


- 212
- 213
- 214
- 215
- 216
- 217
- 218
- 219


- 20
- 21 2


- 23
- 24
- 25
- 26
- 27
- 28
- 29


- 230
- 231
- 232


23

- 234
- 235
- 236
- 237
- 238


- 5.调bug 我的版本3.0 存在bug: ClickhouseWriter.java 中引⽤的类(import ru.yandex.clickhouse.ClickHouseTuple;)⽆效

import ru.yandex.clickhouse.ClickHouseTuple;

- 1 解决办法：直接删除该⾏即可

6.打包发布： DataX使⽤asembly打包，asembly的使⽤⽅法请咨询⾕哥或者度娘。打包命令如下：

mvn clean package -DskipTests asembly:asembly

- 1 编译后结果

D:\ProgramFiles\Java\jdk1.8.0_241\bin\java.exe Dmaven.multiModuleProjectDirectory=D:\work\DataX Dmaven.home=D:\ProgramFiles\JetBrains\InteliJIDEA2020.1.2\plugins\maven\lib\maven3 Dclasworlds.conf=D:\ProgramFiles\JetBrains\InteliJIDEA2020.1.2\plugins\maven\lib\maven3\bin\m

- 2.conf Dmaven.ext.clas.path=D:\ProgramFiles\JetBrains\InteliJIDEA2020.1.2\plugins\maven\lib\mavenevent-listener.jar javagent:D:\ProgramFiles\JetBrains\InteliJIDEA2020.1.2\lib\idea_rt.jar=60517 D:\ProgramFiles\Jet Brains\InteliJIDEA2020.1.2\bin -Dfile.encoding=UTF-8 -claspath D:\ProgramFiles\JetBrains\InteliJIDEA2020.1.2\plugins\maven\lib\maven3\bot\plexus-clasworlds-


- 2.6.0.jar;D:\ProgramFiles\JetBrains\InteliJIDEA2020.1.2\plugins\maven\lib\maven3\bot\plexusclasworlds.license org.codehaus.clasworlds.Launcher -Didea.version2020.1.3 -s D:\work\conf\setings.xml -Dmaven.repo.local=D:\work\repository -DskipTests=true clean package




-DskipTests asembly:asembly [INFO] Scaning for projects. [INFO] -

[INFO] Reactor Build Order: [INFO] [INFO] datax-al [pom] [INFO] datax-comon [jar] [INFO] datax-transformer [jar] [INFO] datax-core [jar] [INFO] plugin-unstructured-storage-util [jar] [INFO] mongodbreader [jar] [INFO] txtfilewriter [jar] [INFO] hdfswriter [jar] [INFO] streamwriter [jar] [INFO] plugin-rdbms-util [jar] [INFO] hbase20xsqlreader [jar] [INFO] hbase20xsqlwriter [jar] [INFO] [INFO] -< com.alibaba.datax:datax-al > [INFO] Building datax-al 0.0.1-SNAPSHOT [1/12] [INFO] -[ pom ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ datax-al [INFO] Deleting D:\work\DataX\target [INFO] [INFO] -< com.alibaba.datax:datax-comon > [INFO] Building datax-comon 0.0.1-SNAPSHOT [2/12] [INFO] -[ jar ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ datax-comon [INFO] Deleting D:\work\DataX\comon\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ datax-comon [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\comon\src\main\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ datax-comon [INFO] Compiling 39 source files to D:\work\DataX\comon\target\clases

[INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ datax-comon -

- [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\comon\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ datax-comon [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ datax-comon [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ datax-comon [INFO] Building jar: D:\work\DataX\comon\target\datax-comon-0.0.1-SNAPSHOT.jar [INFO] [INFO] -< com.alibaba.datax:datax-transformer > [INFO] Building datax-transformer 0.0.1-SNAPSHOT [3/12] [INFO] -[ jar ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ datax-transformer [INFO] Deleting D:\work\DataX\transformer\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ datax-transformer [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\transformer\src\main\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ datax-transformer -

- [INFO] Compiling 2 source files to D:\work\DataX\transformer\target\clases [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ dataxtransformer [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\transformer\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ datax-transformer-


- [INFO] No sources to compile


[INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ datax-transformer [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ datax-transformer [INFO] Building jar: D:\work\DataX\transformer\target\datax-transformer-0.0.1-SNAPSHOT.jar [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ datax-transformer [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\transformer\target\datax [WARNING] Asembly file: D:\work\DataX\transformer\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO] -< com.alibaba.datax:datax-core > [INFO] Building datax-core 0.0.1-SNAPSHOT [4/12] [INFO] -[ jar ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ datax-core [INFO] Deleting D:\work\DataX\core\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ datax-core [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\core\src\main\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ datax-core [INFO] Compiling 70 source files to D:\work\DataX\core\target\clases [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ datax-core [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\core\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ datax-core [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ datax-core [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ datax-core -

[INFO] Building jar: D:\work\DataX\core\target\datax-core-0.0.1-SNAPSHOT.jar [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (default) @ datax-core [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\core\target\datax [WARNING] Asembly file: D:\work\DataX\core\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO] -< com.alibaba.datax:plugin-unstructured-storage-util > [INFO] Building plugin-unstructured-storage-util 0.0.1-SNAPSHOT [5/12] [INFO] -[ jar ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ plugin-unstructured-storage-util [INFO] Deleting D:\work\DataX\plugin-unstructured-storage-util\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ plugin-unstructuredstorage-util [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\plugin-unstructured-storageutil\src\main\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ plugin-unstructured-storageutil [INFO] Compiling 13 source files to D:\work\DataX\plugin-unstructured-storage-util\target\clases [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ pluginunstructured-storage-util [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\plugin-unstructured-storageutil\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ plugin-unstructuredstorage-util [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ plugin-unstructured-storage-util [INFO] Tests are ski ped.

[INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ plugin-unstructured-storage-util [INFO] Building jar: D:\work\DataX\plugin-unstructured-storage-util\target\plugin-unstructuredstorage-util-0.0.1-SNAPSHOT.jar [INFO] [INFO] -< com.alibaba.datax:mongodbreader > [INFO] Building mongodbreader 0.0.1-SNAPSHOT [6/12] [INFO] -[ jar ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ mongodbreader [INFO] Deleting D:\work\DataX\mongodbreader\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ mongodbreader [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 2 resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ mongodbreader -

- [INFO] Compiling 5 source files to D:\work\DataX\mongodbreader\target\clases [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ mongodbreader


[INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\mongodbreader\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ mongodbreader [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ mongodbreader [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ mongodbreader [INFO] Building jar: D:\work\DataX\mongodbreader\target\mongodbreader-0.0.1-SNAPSHOT.jar [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ mongodbreader [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\mongodbreader\target\datax

[WARNING] Asembly file: D:\work\DataX\mongodbreader\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO] -< com.alibaba.datax:txtfilewriter > [INFO] Building txtfilewriter 0.0.1-SNAPSHOT [7/12] [INFO] -[ jar ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ txtfilewriter [INFO] Deleting D:\work\DataX\txtfilewriter\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ txtfilewriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 2 resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ txtfilewriter -

- [INFO] Compiling 3 source files to D:\work\DataX\txtfilewriter\target\clases [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ txtfilewriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\txtfilewriter\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ txtfilewriter [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ txtfilewriter [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ txtfilewriter [INFO] Building jar: D:\work\DataX\txtfilewriter\target\txtfilewriter-0.0.1-SNAPSHOT.jar [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ txtfilewriter [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\txtfilewriter\target\datax [WARNING] Asembly file: D:\work\DataX\txtfilewriter\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO] -< com.alibaba.datax:hdfswriter > -


[INFO] Building hdfswriter 0.0.1-SNAPSHOT [8/12] [INFO] -[ jar ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ hdfswriter [INFO] Deleting D:\work\DataX\hdfswriter\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ hdfswriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 2 resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ hdfswriter -

- [INFO] Compiling 6 source files to D:\work\DataX\hdfswriter\target\clases [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ hdfswriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\hdfswriter\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ hdfswriter [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ hdfswriter [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ hdfswriter [INFO] Building jar: D:\work\DataX\hdfswriter\target\hdfswriter-0.0.1-SNAPSHOT.jar [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ hdfswriter [INFO] Reading asembly descriptor: src/main/asembly/package.xml Downloading from alimaven: htp:/maven.aliyun.com/nexus/content/groups/public/eigenbase/eigenbaseproperties/1.1.4/eigenbase-properties-1.1.4.pom Downloading from alimaven: htp:/maven.aliyun.com/nexus/content/groups/public/org/pentaho/pentaho-agdesigneralgorithm/5.1.5-jhyde/pentaho-agdesigner-algorithm-5.1.5-jhyde.pom [INFO] Copying files to D:\work\DataX\hdfswriter\target\datax [WARNING] Asembly file: D:\work\DataX\hdfswriter\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment.


[INFO] -< com.alibaba.datax:streamwriter > [INFO] Building streamwriter 0.0.1-SNAPSHOT [9/12] [INFO] -[ jar ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ streamwriter [INFO] Deleting D:\work\DataX\streamwriter\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ streamwriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 2 resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ streamwriter [INFO] Compiling 3 source files to D:\work\DataX\streamwriter\target\clases [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ streamwriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\streamwriter\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ streamwriter [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ streamwriter [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ streamwriter [INFO] Building jar: D:\work\DataX\streamwriter\target\streamwriter-0.0.1-SNAPSHOT.jar [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ streamwriter [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\streamwriter\target\datax [WARNING] Asembly file: D:\work\DataX\streamwriter\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO] -< com.alibaba.datax:plugin-rdbms-util > [INFO] Building plugin-rdbms-util 0.0.1-SNAPSHOT [10/12] [INFO] -[ jar ] -

[INFO] - maven-clean-plugin:2.5:clean (default-clean) @ plugin-rdbms-util [INFO] Deleting D:\work\DataX\plugin-rdbms-util\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ plugin-rdbms-util [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\plugin-rdbms-util\src\main\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ plugin-rdbms-util [INFO] Compiling 24 source files to D:\work\DataX\plugin-rdbms-util\target\clases [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ plugin-rdbmsutil [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\plugin-rdbms-util\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ plugin-rdbms-util [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ plugin-rdbms-util [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ plugin-rdbms-util [INFO] Building jar: D:\work\DataX\plugin-rdbms-util\target\plugin-rdbms-util-0.0.1-SNAPSHOT.jar [INFO] [INFO] -< com.alibaba.datax:hbase20xsqlreader > [INFO] Building hbase20xsqlreader 0.0.1-SNAPSHOT [1/12] [INFO] -[ jar ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ hbase20xsqlreader [INFO] Deleting D:\work\DataX\hbase20xsqlreader\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ hbase20xsqlreader [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 0 resource [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ hbase20xsqlreader -

- [INFO] Compiling 6 source files to D:\work\DataX\hbase20xsqlreader\target\clases [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ hbase20xsqlreader [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\hbase20xsqlreader\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ hbase20xsqlreader -

[INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ hbase20xsqlreader [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ hbase20xsqlreader [INFO] Building jar: D:\work\DataX\hbase20xsqlreader\target\hbase20xsqlreader-0.0.1SNAPSHOT.jar [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ hbase20xsqlreader [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\hbase20xsqlreader\target\datax [WARNING] Asembly file: D:\work\DataX\hbase20xsqlreader\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO] -< com.alibaba.datax:hbase20xsqlwriter > [INFO] Building hbase20xsqlwriter 0.0.1-SNAPSHOT [12/12] [INFO] -[ jar ] [INFO] [INFO] - maven-clean-plugin:2.5:clean (default-clean) @ hbase20xsqlwriter [INFO] Deleting D:\work\DataX\hbase20xsqlwriter\target [INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ hbase20xsqlwriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 0 resource [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ hbase20xsqlwriter -

- [INFO] Compiling 7 source files to D:\work\DataX\hbase20xsqlwriter\target\clases


[INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ hbase20xsqlwriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\hbase20xsqlwriter\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ hbase20xsqlwriter-

[INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ hbase20xsqlwriter [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ hbase20xsqlwriter [INFO] Building jar: D:\work\DataX\hbase20xsqlwriter\target\hbase20xsqlwriter-0.0.1SNAPSHOT.jar [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ hbase20xsqlwriter [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\hbase20xsqlwriter\target\datax [WARNING] Asembly file: D:\work\DataX\hbase20xsqlwriter\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO] -< com.alibaba.datax:datax-al > [INFO] Building datax-al 0.0.1-SNAPSHOT [13/12] [INFO] -[ pom ] [INFO] [INFO] > maven-asembly-plugin:2.2-beta-5:asembly (default-cli) > package @ datax-al > [INFO] [INFO]

>

[INFO] Forking datax-comon 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ datax-comon [INFO] Using 'UTF-8' encoding to copy filtered resources.

[INFO] skip non existing resourceDirectory D:\work\DataX\comon\src\main\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ datax-comon [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ datax-comon -

[INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\comon\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ datax-comon [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ datax-comon [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ datax-comon [INFO] [INFO]

>

[INFO] Forking datax-transformer 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ datax-transformer [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\transformer\src\main\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ datax-transformer [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ dataxtransformer [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\transformer\src\test\resources [INFO]

[INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ datax-transformer-

[INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ datax-transformer [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ datax-transformer [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ datax-transformer [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\transformer\target\datax [WARNING] Asembly file: D:\work\DataX\transformer\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO]

>

[INFO] Forking datax-core 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ datax-core [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\core\src\main\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ datax-core [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ datax-core [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\core\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ datax-core [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ datax-core -

[INFO] - maven-jar-plugin:2.4:jar (default-jar) @ datax-core [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (default) @ datax-core [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\core\target\datax [WARNING] Asembly file: D:\work\DataX\core\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO]

>

[INFO] Forking plugin-unstructured-storage-util 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ plugin-unstructuredstorage-util [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\plugin-unstructured-storageutil\src\main\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ plugin-unstructured-storageutil [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ pluginunstructured-storage-util [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\plugin-unstructured-storageutil\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ plugin-unstructuredstorage-util [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ plugin-unstructured-storage-util -

[INFO] - maven-jar-plugin:2.4:jar (default-jar) @ plugin-unstructured-storage-util [INFO] [INFO]

>

[INFO] Forking mongodbreader 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ mongodbreader [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 2 resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ mongodbreader [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ mongodbreader

[INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\mongodbreader\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ mongodbreader [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ mongodbreader [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ mongodbreader [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ mongodbreader [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\mongodbreader\target\datax [WARNING] Asembly file: D:\work\DataX\mongodbreader\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO]

>

[INFO] Forking txtfilewriter 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ txtfilewriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 2 resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ txtfilewriter [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ txtfilewriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\txtfilewriter\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ txtfilewriter [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ txtfilewriter [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ txtfilewriter [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ txtfilewriter [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\txtfilewriter\target\datax [WARNING] Asembly file: D:\work\DataX\txtfilewriter\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO]

>

[INFO] Forking hdfswriter 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ hdfswriter [INFO] Using 'UTF-8' encoding to copy filtered resources.

[INFO] Copying 2 resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ hdfswriter [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ hdfswriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\hdfswriter\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ hdfswriter [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ hdfswriter [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ hdfswriter [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ hdfswriter [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\hdfswriter\target\datax [WARNING] Asembly file: D:\work\DataX\hdfswriter\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO]

>

[INFO] Forking streamwriter 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ streamwriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 2 resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ streamwriter [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ streamwriter -

[INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\streamwriter\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ streamwriter [INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ streamwriter [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ streamwriter [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ streamwriter [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\streamwriter\target\datax [WARNING] Asembly file: D:\work\DataX\streamwriter\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO]

>

[INFO] Forking plugin-rdbms-util 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ plugin-rdbms-util [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\plugin-rdbms-util\src\main\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ plugin-rdbms-util [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ plugin-rdbmsutil [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\plugin-rdbms-util\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ plugin-rdbms-util [INFO] No sources to compile

[INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ plugin-rdbms-util [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ plugin-rdbms-util [INFO] [INFO]

>

[INFO] Forking hbase20xsqlreader 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ hbase20xsqlreader [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 0 resource [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ hbase20xsqlreader [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ hbase20xsqlreader [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\hbase20xsqlreader\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ hbase20xsqlreader -

[INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ hbase20xsqlreader [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ hbase20xsqlreader [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ hbase20xsqlreader [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\hbase20xsqlreader\target\datax

[WARNING] Asembly file: D:\work\DataX\hbase20xsqlreader\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO]

>

[INFO] Forking hbase20xsqlwriter 0.0.1-SNAPSHOT [INFO]

>

[INFO] [INFO] - maven-resources-plugin:2.6:resources (default-resources) @ hbase20xsqlwriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] Copying 0 resource [INFO] [INFO] - maven-compiler-plugin:2.3.2:compile (default-compile) @ hbase20xsqlwriter [INFO] Nothing to compile - al clases are up to date [INFO] [INFO] - maven-resources-plugin:2.6:testResources (default-testResources) @ hbase20xsqlwriter [INFO] Using 'UTF-8' encoding to copy filtered resources. [INFO] skip non existing resourceDirectory D:\work\DataX\hbase20xsqlwriter\src\test\resources [INFO] [INFO] - maven-compiler-plugin:2.3.2:testCompile (default-testCompile) @ hbase20xsqlwriter-

[INFO] No sources to compile [INFO] [INFO] - maven-surefire-plugin:2.12.4:test (default-test) @ hbase20xsqlwriter [INFO] Tests are ski ped. [INFO] [INFO] - maven-jar-plugin:2.4:jar (default-jar) @ hbase20xsqlwriter [INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:single (dwzip) @ hbase20xsqlwriter [INFO] Reading asembly descriptor: src/main/asembly/package.xml [INFO] Copying files to D:\work\DataX\hbase20xsqlwriter\target\datax [WARNING] Asembly file: D:\work\DataX\hbase20xsqlwriter\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO] < maven-asembly-plugin:2.2-beta-5:asembly (default-cli) < package @ datax-al <

[INFO] [INFO] - maven-asembly-plugin:2.2-beta-5:asembly (default-cli) @ datax-al [INFO] Reading asembly descriptor: package.xml [INFO] datax/lib\comons-io-2.4.jar already aded, ski ping [INFO] datax/lib\comons-lang3-3.3.2.jar already aded, ski ping [INFO] datax/lib\comons-math3-3.1.1.jar already aded, ski ping [INFO] datax/lib\datax-comon-0.0.1-SNAPSHOT.jar already aded, ski ping [INFO] datax/lib\datax-transformer-0.0.1-SNAPSHOT.jar already aded, ski ping [INFO] datax/lib\fastjson-1.1.46.sec01.jar already aded, ski ping [INFO] datax/lib\hamcrest-core-1.3.jar already aded, ski ping [INFO] datax/lib\logback-clasic-1.0.13.jar already aded, ski ping [INFO] datax/lib\logback-core-1.0.13.jar already aded, ski ping [INFO] datax/lib\slf4j-api-1.7.10.jar already aded, ski ping [INFO] Building tar : D:\work\DataX\target\datax.tar.gz [INFO] datax/lib\comons-io-2.4.jar already aded, ski ping [INFO] datax/lib\comons-lang3-3.3.2.jar already aded, ski ping [INFO] datax/lib\comons-math3-3.1.1.jar already aded, ski ping [INFO] datax/lib\datax-comon-0.0.1-SNAPSHOT.jar already aded, ski ping [INFO] datax/lib\datax-transformer-0.0.1-SNAPSHOT.jar already aded, ski ping [INFO] datax/lib\fastjson-1.1.46.sec01.jar already aded, ski ping [INFO] datax/lib\hamcrest-core-1.3.jar already aded, ski ping [INFO] datax/lib\logback-clasic-1.0.13.jar already aded, ski ping [INFO] datax/lib\logback-core-1.0.13.jar already aded, ski ping [INFO] datax/lib\slf4j-api-1.7.10.jar already aded, ski ping [INFO] datax/lib\comons-io-2.4.jar already aded, ski ping [INFO] datax/lib\comons-lang3-3.3.2.jar already aded, ski ping [INFO] datax/lib\comons-math3-3.1.1.jar already aded, ski ping [INFO] datax/lib\datax-comon-0.0.1-SNAPSHOT.jar already aded, ski ping [INFO] datax/lib\datax-transformer-0.0.1-SNAPSHOT.jar already aded, ski ping [INFO] datax/lib\fastjson-1.1.46.sec01.jar already aded, ski ping [INFO] datax/lib\hamcrest-core-1.3.jar already aded, ski ping [INFO] datax/lib\logback-clasic-1.0.13.jar already aded, ski ping [INFO] datax/lib\logback-core-1.0.13.jar already aded, ski ping [INFO] datax/lib\slf4j-api-1.7.10.jar already aded, ski ping [INFO] Copying files to D:\work\DataX\target\datax [INFO] datax/lib\comons-io-2.4.jar already aded, ski ping

[INFO] datax/lib\comons-lang3-3.3.2.jar already aded, ski ping [INFO] datax/lib\comons-math3-3.1.1.jar already aded, ski ping [INFO] datax/lib\datax-comon-0.0.1-SNAPSHOT.jar already aded, ski ping [INFO] datax/lib\datax-transformer-0.0.1-SNAPSHOT.jar already aded, ski ping [INFO] datax/lib\fastjson-1.1.46.sec01.jar already aded, ski ping [INFO] datax/lib\hamcrest-core-1.3.jar already aded, ski ping [INFO] datax/lib\logback-clasic-1.0.13.jar already aded, ski ping [INFO] datax/lib\logback-core-1.0.13.jar already aded, ski ping [INFO] datax/lib\slf4j-api-1.7.10.jar already aded, ski ping [WARNING] Asembly file: D:\work\DataX\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. [INFO] [INFO] Reactor Sumary for datax-al 0.0.1-SNAPSHOT: [INFO] [INFO] datax-al . SUCES [01 36 min] [INFO] datax-comon . SUCES [1.939 s] [INFO] datax-transformer . SUCES [2.208 s] [INFO] datax-core . SUCES [5.849 s] [INFO] plugin-unstructured-storage-util . SUCES [1.479 s] [INFO] mongodbreader . SUCES [9.187 s] [INFO] txtfilewriter . SUCES [8.526 s] [INFO] hdfswriter . SUCES [ 32.463 s] [INFO] streamwriter . SUCES [1.248 s] [INFO] plugin-rdbms-util . SUCES [0.907 s] [INFO] hbase20xsqlreader . SUCES [2.312 s] [INFO] hbase20xsqlwriter . SUCES [1.865 s] [INFO] [INFO] BUILD SUCES [INFO] [INFO] Total time: 02 45 min [INFO] Finished at: 2020-08-19T15  0 06+08  0 [INFO] -

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

23 24 25 26 27 28 29 30 31 32

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

- 56
- 57
- 58
- 59
- 60
- 61
- 62
- 63
- 64
- 65


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


- 12 123 124 125 126 127 128 129 130 131 132
- 13 134 135 136 137 138 139 140 141 142 143
- 14 145 146 147 148 149 150 151 152


- 153 154
- 15 156 157 158 159 160 161 162 163 164 165
- 16 167 168 169 170 171 172 173 174 175 176
- 17 178 179 180 181 182 183 184 185 186 187
- 18 189


- 190 191 192 193 194 195 196 197 198
- 19
- 20

- 201
- 202
- 203
- 204
- 205
- 206
- 207
- 208
- 209
- 210


- 21


- 212
- 213
- 214
- 215
- 216
- 217
- 218
- 219


- 20
- 21 2


- 23
- 24
- 25
- 26


- 27
- 28
- 29


- 230
- 231
- 232


- 23 234 235 236 237 238 239 240 241 242 243
- 24 245 246 247 248 249 250 251 252 253 254
- 25 256 257 258 259 260 261 262 263


- 264 265
- 26 267 268 269 270 271 272 273 274 275 276
- 27 278 279 280 281 282 283 284 285 286 287
- 28 289 290 291 292 293 294 295 296 297 298
- 29
- 30


- 301
- 302
- 303
- 304
- 305
- 306
- 307
- 308
- 309
- 310


- 31 312 313 314 315 316 317 318 319 320 321
- 32


- 323
- 324
- 325
- 326
- 327
- 328
- 329


- 30
- 31
- 32 3


- 34
- 35
- 36
- 37


- 38
- 39


- 340
- 341
- 342
- 343


- 34 345 346 347 348 349 350 351 352 353 354
- 35 356 357 358 359 360 361 362 363 364 365
- 36 367 368 369 370 371 372 373 374


- 375 376
- 37 378 379 380 381 382 383 384 385 386 387
- 38 389 390 391 392 393 394 395 396 397 398
- 39
- 40

- 401
- 402
- 403
- 404
- 405
- 406
- 407
- 408
- 409
- 410


- 41


- 412 413 414 415 416 417 418 419 420 421
- 42 423 424 425 426 427 428 429 430 431 432
- 43


- 434
- 435
- 436
- 437
- 438
- 439


- 40
- 41
- 42
- 43 4


- 45
- 46
- 47
- 48


- 49


450 451 452 453 454

- 45 456 457 458 459 460 461 462 463 464 465
- 46 467 468 469 470 471 472 473 474 475 476
- 47 478 479 480 481 482 483 484 485


- 486 487
- 48 489 490 491 492 493 494 495 496 497 498
- 49
- 50

- 501
- 502
- 503
- 504
- 505
- 506
- 507
- 508
- 509
- 510


- 51 512 513 514 515 516 517 518 519 520 521
- 52


- 523 524 525 526 527 528 529 530 531 532
- 53 534 535 536 537 538 539 540 541 542 543
- 54


- 545
- 546
- 547
- 548
- 549


- 50
- 51
- 52
- 53
- 54 5


- 56
- 57
- 58
- 59


- 560
- 561
- 562
- 563
- 564
- 565


- 56 567 568 569 570 571 572 573 574 575 576
- 57 578 579 580 581 582 583 584 585 586 587
- 58 589 590 591 592 593 594 595 596


- 597 598
- 59
- 60 601 602 603 604 605 606 607 608 609 610
- 61 612 613 614 615 616 617 618 619 620 621
- 62 623 624 625 626 627 628 629 630 631 632
- 63


- 634 635 636 637 638 639 640 641 642 643
- 64 645 646 647 648 649 650 651 652 653 654
- 65


- 656
- 657
- 658
- 659


- 60
- 61
- 62
- 63
- 64
- 65 6


- 67
- 68
- 69


- 670


- 671
- 672
- 673
- 674
- 675
- 676


- 67 678 679 680 681 682 683 684 685 686 687
- 68 689 690 691 692 693 694 695 696 697 698
- 69
- 70 701 702 703 704 705 706 707


- 708 709 710
- 71 712 713 714 715 716 717 718 719 720 721
- 72 723 724 725 726 727 728 729 730 731 732
- 73 可能会有报红，不⽤管即可 找到[WARNING] Asembly file: D:\work\DataX\target\datax is not a regular file (it may be a directory). It canot be atached to the project build for instalation or deployment. 该⽬录就是System.setProperty(“datax.home”, “D:\work\DataX\target\datax\datax”);中设置的路径 接下来就可以进⾏⾃⼰代码的开发了


⼆、项⽬开发

先说下我的需求：由于mongodbreader读取时Document和Aray类型没有识别，⽽是直接存⼊的obj 的输出字符串，这个类型我们的数据库不能识别。所以需要特殊转换，⽬标转换成json类型。

我扩展的是mongodbreader项⽬，由于图简单，没有在其基础上进⾏扩展，⽽是直接简单粗暴的修改 源码，我需要将未识别的参数类型Document、List类型转换成josn字符串类型。

分析

⽬录清晰，直接修改MongoDBReader类即可 MongoDBReader源码

package com.alibaba.datax.plugin.reader.mongodbreader;

import java.util.ArayList; import java.util.Arays; import java.util.Date; import java.util.Iterator; import java.util.List;

import com.alibaba.datax.comon.element.BolColumn; import com.alibaba.datax.comon.element.DateColumn; import com.alibaba.datax.comon.element.DoubleColumn; import com.alibaba.datax.comon.element.LongColumn; import com.alibaba.datax.comon.element.Record; import com.alibaba.datax.comon.element.StringColumn; import com.alibaba.datax.comon.exception.DataXException; import com.alibaba.datax.comon.plugin.RecordSender; import com.alibaba.datax.comon.spi.Reader; import com.alibaba.datax.comon.util.Configuration; import com.alibaba.datax.plugin.reader.mongodbreader.util.ColectionSplitUtil; import com.alibaba.datax.plugin.reader.mongodbreader.util.MongoUtil; import com.alibaba.fastjson.JSON; import com.alibaba.fastjson.JSONAray; import com.alibaba.fastjson.JSONObject;

import com.gogle.comon.base.Joiner; import com.gogle.comon.base.Strings; import com.mongodb.MongoClient; import com.mongodb.client.MongoColection; import com.mongodb.client.MongoCursor;

import com.mongodb.client.MongoDatabase; import org.bson.Document; import org.bson.types.ObjectId;

/*

- * Created by jianying.wcj on 2015/3/19 019.
- * Modified by mingyan.zc on 2016/6/13.
- * Modified by mingyan.zc on 2017/7/5.
- */ public clas MongoDBReader extends Reader {


public static clas Job extends Reader.Job {

private Configuration originalConfig = nul;

private MongoClient mongoClient;

private String userName = nul; private String pasword = nul;

@Overide public List<Configuration> split(int adviceNumber) {

return ColectionSplitUtil.doSplit(originalConfig,adviceNumber,mongoClient); }

@Overide public void init() {

this.originalConfig = super.getPluginJobConf(); this.userName = originalConfig.getString(KeyConstant.MONGO_USER_NAME,

originalConfig.getString(KeyConstant.MONGO_USERNAME);

this.pasword = originalConfig.getString(KeyConstant.MONGO_USER_PASWORD, originalConfig.getString(KeyConstant.MONGO_PASWORD);

String database = originalConfig.getString(KeyConstant.MONGO_DB_NAME, originalConfig.getString(KeyConstant.MONGO_DATABASE);

String authDb = originalConfig.getString(KeyConstant.MONGO_AUTHDB, database); if(!Strings.isNulOrEmpty(this.userName) & !Strings.isNulOrEmpty(this.pasword) {

this.mongoClient = MongoUtil.initCredentialMongoClient(originalConfig,userName,pasword,authDb); } else {

this.mongoClient = MongoUtil.initMongoClient(originalConfig); }

}

@Overide public void destroy() {

} }

public static clas Task extends Reader.Task {

private Configuration readerSliceConfig;

private MongoClient mongoClient;

private String userName = nul; private String pasword = nul;

private String authDb = nul; private String database = nul; private String colection = nul;

private String query = nul;

private JSONAray mongodbColumnMeta = nul; private Object lowerBound = nul; private Object uperBound = nul; private bolean isObjectId = true;

@Overide public void startRead(RecordSender recordSender) {

if(lowerBound= nul | uperBound = nul | mongoClient = nul | database = nul | colection = nul | mongodbColumnMeta = nul) { throw DataXException.asDataXException(MongoDBReaderErorCode.I LEGAL_VALUE,

MongoDBReaderErorCode.I LEGAL_VALUE.getDescription();

} MongoDatabase db = mongoClient.getDatabase(database); MongoColection col = db.getColection(this.colection);

MongoCursor<Document> dbCursor = nul; Document filter = new Document(); if (lowerBound.equals("min") {

if (!uperBound.equals("max") {

filter.apend(KeyConstant.MONGO_PRIMARY_ID, new Document("$lt", isObjectId ? new ObjectId(uperBound.toString() : uperBound);

} } else if (uperBound.equals("max") {

filter.apend(KeyConstant.MONGO_PRIMARY_ID, new Document("$gte", isObjectId ? new ObjectId(lowerBound.toString() : lowerBound);

} else {

filter.apend(KeyConstant.MONGO_PRIMARY_ID, new Document("$gte", isObjectId ? new ObjectId(lowerBound.toString() : lowerBound).apend("$lt", isObjectId ? new ObjectId(uperBound.toString() : uperBound);

} if(!Strings.isNulOrEmpty(query) {

Document queryFilter = Document.parse(query); filter = new Document("$and", Arays.asList(filter, queryFilter);

} dbCursor = col.find(filter).iterator(); while (dbCursor.hasNext() {

Document item = dbCursor.next(); Record record = recordSender.createRecord(); Iterator columnItera = mongodbColumnMeta.iterator(); while (columnItera.hasNext() {

JSONObject column = (JSONObject)columnItera.next(); Object tempCol = item.get(column.getString(KeyConstant.COLUMN_NAME); if (tempCol = nul) {

if (KeyConstant.isDocumentType(column.getString(KeyConstant.COLUMN_TYPE) { String[] name = column.getString(KeyConstant.COLUMN_NAME).split("\."); if (name.length > 1) {

Object obj; Document nestedDocument = item; for (String str : name) {

obj = nestedDocument.get(str); if (obj instanceof Document) {

nestedDocument = (Document) obj; }

}

if (nul != nestedDocument) { Document doc = nestedDocument; tempCol = doc.get(name[name.length - 1]);

} }

}

} if (tempCol = nul) {

/continue; 这个不能直接continue会导致record到⽬的端错位 record.adColumn(new StringColumn(nul);

}else if (tempCol instanceof Double) { /TODO deal with Double.isNaN()

record.adColumn(new DoubleColumn(Double) tempCol); } else if (tempCol instanceof Bolean) {

record.adColumn(new BolColumn(Bolean) tempCol); } else if (tempCol instanceof Date) {

record.adColumn(new DateColumn(Date) tempCol); } else if (tempCol instanceof Integer) {

record.adColumn(new LongColumn(Integer) tempCol); }else if (tempCol instanceof Long) {

record.adColumn(new LongColumn(Long) tempCol); } else {

if(KeyConstant.isArayType(column.getString(KeyConstant.COLUMN_TYPE) { String spliter = column.getString(KeyConstant.COLUMN_SPLI TER); if(Strings.isNulOrEmpty(spliter) {

throw DataXException.asDataXException(MongoDBReaderErorCode.I LEGAL_VALUE, MongoDBReaderErorCode.I LEGAL_VALUE.getDescription();

} else { ArayList aray = (ArayList)tempCol; String tempArayStr = Joiner.on(spliter).join(aray); record.adColumn(new StringColumn(tempArayStr);

} } else {

record.adColumn(new StringColumn(tempCol.toString( ); }

}

} recordSender.sendToWriter(record);

} }

@Overide public void init() {

this.readerSliceConfig = super.getPluginJobConf(); this.userName = readerSliceConfig.getString(KeyConstant.MONGO_USER_NAME,

readerSliceConfig.getString(KeyConstant.MONGO_USERNAME);

this.pasword = readerSliceConfig.getString(KeyConstant.MONGO_USER_PASWORD, readerSliceConfig.getString(KeyConstant.MONGO_PASWORD);

this.database = readerSliceConfig.getString(KeyConstant.MONGO_DB_NAME,

readerSliceConfig.getString(KeyConstant.MONGO_DATABASE); this.authDb = readerSliceConfig.getString(KeyConstant.MONGO_AUTHDB, this.database); if(!Strings.isNulOrEmpty(userName) & !Strings.isNulOrEmpty(pasword) {

mongoClient = MongoUtil.initCredentialMongoClient(readerSliceConfig,userName,pasword,authDb); } else {

mongoClient = MongoUtil.initMongoClient(readerSliceConfig); }

this.colection = readerSliceConfig.getString(KeyConstant.MONGO_COLECTION_NAME); this.query = readerSliceConfig.getString(KeyConstant.MONGO_QUERY);

this.mongodbColumnMeta = JSON.parseAray(readerSliceConfig.getString(KeyConstant.MONGO_COLUMN);

this.lowerBound = readerSliceConfig.get(KeyConstant.LOWER_BOUND); this.uperBound = readerSliceConfig.get(KeyConstant.UPER_BOUND); this.isObjectId = readerSliceConfig.getBol(KeyConstant.IS_OBJECTID);

}

@Overide public void destroy() {

}

}

} 1 2 3 4 5 6 7 8 9 10

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

34 35 36 37 38 39 40 41 42 43

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


- 56
- 57
- 58
- 59
- 60


- 61
- 62
- 63
- 64
- 65


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


- 12 123 124 125 126 127 128 129 130 131 132
- 13 134


- 135 136 137 138 139 140 141 142 143
- 14 145 146 147 148 149 150 151 152 153 154
- 15 156 157 158 159 160 161 162 163 164 165
- 16 167 168 169 170 171


- 172 173 174 175 176
- 17 178 179 180 181 182 183 184 185 186 187
- 18 189 190 191 192 193 194 195 196 197 198
- 19
- 20 201 202 203 204 205 206 207 208


- 209 210
- 21 212 通过上⾯代码找到类型判断逻辑如下


. else if (tempCol instanceof Double) { /TODO deal with Double.isNaN()

record.adColumn(new DoubleColumn(Double) tempCol); } else if (tempCol instanceof Bolean) {

record.adColumn(new BolColumn(Bolean) tempCol); }.

- 1
- 2
- 3
- 4
- 5
- 6
- 7 增加⼀下逻辑


else if (tempCol instanceof Document | tempCol instanceof List) {

record.adColumn(new StringColumn(JSONObject.toJSONString(tempCol ); }

- 1
- 2
- 3 重新打包发布： 由于不会部分插件⽅式打包，所以只能先打包mongodbreader然后将其 打包mongodbreader模块： mvn clean package -pl mongodbreader -DskipTests asembly:asembly 如果其他模块，直接将mongodbreader配置成对应的模块名（pom.xml中 <module></module>内名 称）即可。


谢谢，阅读！

⸻—

# 版权声明：本⽂为CSDN博主「莫天幽」的原创⽂章，遵循 C 4.0 BY-SA版权协议，转载请附上原⽂ 出处链接及本声明。 原⽂链接：htps:/blog.csdn.net/u014738571/article/details/108097370

