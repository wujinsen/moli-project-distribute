- 1.下载apache-ambari-2.2.2-src.tar.gz，解压tar -zxvfapache-ambari-2.2.2-src.tar.gz -C /usr/local/

- 2. cdapache-ambari-2.2.2-src mvn versions:set -DnewVersion=2.2.2 pushd ambari-metrics

- 3. 在apache-ambari-2.2.2-src⽬录下建⽴3个⽂件: vi jms-1.1.pom


mvn versions:set -DnewVersion=2.2.2

popd

<table>
  <tr>
    <th><project> <modelVersion>4.0.0</modelVersion> <groupId>javax.jms</groupId> <artifactId>jms</artifactId> <version>1.1</version> <name>Java Message Service</name> <description><br><br>The Java Message Service (JMS) API is a messaging standard that allows application components based on the Java 2 Platform, Enterprise Edition (J2EE) to create, send, receive, and read messages. It enables distributed communication that is loosely coupled, reliable, and asynchronous.<br><br></description> <url>http://java.sun.com/products/jms</url> <distributionManagement><br><br><downloadUrl>http://java.sun.com/products/jms/docs.html</downloadUrl> </distributionManagement></th>
  </tr>
</table>


vi jmxri-1.2.1.pom

<table>
  <tr>
    <th><?xml version="1.0" encoding="UTF-8"?><project> <modelVersion>4.0.0</modelVersion> <groupId>com.sun.jmx</groupId> <artifactId>jmxri</artifactId> <version>1.2.1</version> <distributionManagement><br><br><status>deployed</status> </distributionManagement></th>
  </tr>
</table>


vi jmxtools-1.2.1.pom

<table>
  <tr>
    <th><?xml version="1.0" encoding="UTF-8"?><project> <modelVersion>4.0.0</modelVersion> <groupId>com.sun.jdmk</groupId> <artifactId>jmxtools</artifactId> <version>1.2.1</version> <distributionManagement><br><br><status>deployed</status> </distributionManagement></th>
  </tr>
</table>


执⾏: mvn install:install-file -Dfile=jms-1.1.pom -DgroupId=javax.jms DartifactId=jms -Dversion=1.1 -Dpackaging=jar

mvn install:install-file -Dfile=jmxtools-1.2.1.pom -DgroupId=com.sun.jdmk

-DartifactId=jmxtools -Dversion=1.2.1 -Dpackaging=jar

mvn install:install-file -Dfile=jmxri-1.2.1.pom -DgroupId=com.sun.jmx DartifactId=jmxri -Dversion=1.2.1 -Dpackaging=jar

- 4.执⾏ mvn -B clean install package rpm:rpm -DnewVersion=2.2.2 -DskipTests Dpython.ver="python >= 2.6"


