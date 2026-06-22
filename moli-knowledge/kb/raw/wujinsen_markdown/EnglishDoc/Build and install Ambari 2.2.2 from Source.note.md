# Build and install Ambari 2.2.2

## Step 1: Download and build Ambari 2.2.2 source

Go to and find the suggested mirror for download. The process to verify the download is described is at

http://www.apache.org/dyn/closer.cgi/ambari/ambari-2.2.2

http://www.apache.org/dyn/cl oser.cgi#verify

<table>
  <tr>
    <th>wget http://www.apache.org/dist/ambari/ambari-2.2.2/apache-ambari-2.2.2src.tar.gz (use the suggested mirror from above) tar xfvz apache-ambari-2.2.2-src.tar.gz cd apache-ambari-2.2.2-src mvn versions:set -DnewVersion=2.2.2<br><br>pushd ambari-metrics mvn versions:set -DnewVersion=2.2.2 popd</th>
  </tr>
</table>


Note: If running into errors while compiling the ambari-metrics package due to missing the artifacts of jms, jmxri, jmxtools:

<table>
  <tr>
    <th>[ERROR] Failed to execute goal on project ambari-metrics-kafka-sink: Could not resolve dependencies for project org.apache.ambari:ambarimetrics-kafka-sink:jar:2.2.2-0: The following artifacts could not be resolved: javax.jms:jms:jar:1.1, com.sun.jdmk:jmxtools:jar:1.2.1, com.sun.jmx:jmxri:jar:1.2.1: Could not transfer artifact javax.jms:jms:jar:1.1 from/to java.net (https://mavenrepository.dev.java.net/nonav/repository): No connector available to access repository java.net (<br><br>of type legacy using the available factories WagonRepositoryConnectorFactory<br><br>https://maven-repository.dev.java.net/nonav/r epository)</th>
  </tr>
</table>


The work around is to manually install the three missing artifacts:

<table>
  <tr>
    <th>mvn install:install-file -Dfile=jms-1.1.pom -DgroupId=javax.jms DartifactId=jms -Dversion=1.1 -Dpackaging=jar<br><br>mvn install:install-file -Dfile=jmxtools-1.2.1.pom -DgroupId=com.sun.jdmk<br><br>-DartifactId=jmxtools-Dversion=1.2.1 -Dpackaging=jar mvn install:install-file -Dfile=jmxri-1.2.1.pom -DgroupId=com.sun.jmx DartifactId=jmxri -Dver ion=1.2.1 -Dpac ing=jar</th>
  </tr>
</table>


s kag

The three poms are:

<table>
  <tr>
    <th>$ cat jms-1.1.pom <project><br><br><modelVersion>4.0.0</modelVersion> <groupId>javax.jms</groupId> <artifactId>jms</artifactId> <version>1.1</version> <name>Java Message Service</name> <description><br><br>The Java Message Service(JMS) API is a messaging standard that allows application components based on the Java 2 Platform, Enterprise Edition (J2EE) to create, send, receive, and readmessages. It enables distributed communication that is loosely coupled, reliable, and asynchronous.<br><br></description> <url>http://java.sun.com/products/jms</url> <distributionManagement><br><br><downloadUrl>http://java.sun.com/products/jms/docs.html</downloadUrl> </distributionM nagement></th>
  </tr>
</table>


a

<table>
  <tr>
    <th>$ cat jmxri-1.2.1.pom <?xml version="1.0" encoding="UTF-8"?><project><br><br><modelVersion>4.0.0</modelVersion> <groupId>com.sun.jmx</groupId> <artifactId>jmxri</artifactId> <version>1.2.1</version> <distributionManagement><br><br><status>deployed</status> </distributionManag ment></th>
  </tr>
</table>


e

<table>
  <tr>
    <th>$ cat jmxtools-1.2.1.pom <?xml version="1.0" encoding="UTF-8"?><project><br><br><modelVersion>4.0.0</modelVersion> <groupId>com.sun.jdmk</groupId> <artifactId>jmxtools</artifactId> <version>1.2.1</version> <distributionManagement><br><br><status>deployed</status> </distributionManageme t></th>
  </tr>
</table>


n

## RHEL (CentOS 5 or 6) & SUSE (SLES 11):

<table>
  <tr>
    <th>mvn -B clean install package rpm:rpm -DnewVersion=2.2.2 -DskipTests Dpython.ver="python >= 2.6"</th>
  </tr>
</table>


## Ubuntu/Debian:

<table>
  <tr>
    <th>mvn -B clean install package jdeb:jdeb -DnewVersion=2.2.2 -DskipTests Dpython.ver="python >= 2.6"</th>
  </tr>
</table>


Note: You need to have tools such as rpm-build tool, brunch, etc. For details on prerequisites, please see .

Ambari Development

## Step 2: Install Ambari Server

Install the rpm package from ambari-server/target/rpm/ambari-server/RPMS/noarch/

[For CentOS 5 or 6]

<table>
  <tr>
    <th>yum install ambari-server*.rpm #This should also pull in postgres packages as well.</th>
  </tr>
</table>


[For SLES 11]

<table>
  <tr>
    <th>zypper install ambari-server*.rpm #This should also pull in postgres packages as well.</th>
  </tr>
</table>


[For Ubuntu/Debian]

<table>
  <tr>
    <th>apt-get install ambari-server*.deb #This should also pull in postgres packages as well.</th>
  </tr>
</table>


## Step 3: Setup and Start Ambari Server

Run the setup command to configure your Ambari Server, Database, JDK, LDAP, and other options:

<table>
  <tr>
    <th>ambari-server setup</th>
  </tr>
</table>


Follow the on-screen instructions to proceed.

Once set up is done, start Ambari Server:

<table>
  <tr>
    <th>ambari-server start</th>
  </tr>
</table>


- Step 4: Install and Start Ambari Agent on All Hosts


Note: This step needs to be run on all hosts that will be managed by Ambari.

Copy the rpm package from ambari-agent/target/rpm/ambari-agent/RPMS/x86_64/ and run:

[For CentOS 5 or 6]

<table>
  <tr>
    <th>yum install ambari-agent*.rpm</th>
  </tr>
</table>


[For SLES 11]

<table>
  <tr>
    <th>zypper install ambari-agent*.rpm</th>
  </tr>
</table>


[Ubuntu/Debian]

<table>
  <tr>
    <th>apt-get install ambari-agent*.rpm</th>
  </tr>
</table>


Edit /etc/ambari-agent/ambari.ini

<table>
  <tr>
    <th>... [server] hostname=localhost<br><br>...</th>
  </tr>
</table>


Make sure hostname under the [server] section points to the actual Ambari Server host, rather than "localhost".

<table>
  <tr>
    <th>ambari-agent start</th>
  </tr>
</table>


- Step 5: Deploy Cluster using Ambari Web UI Open up a web browser and go to http://<ambari-server-host>:8080.


Log in with username admin and password admin and follow on-screen instructions. Secure your environment by ensuring your administrator details are changed from the default values as soon as possible.

Under Install Options page, enter the hosts to add to the cluster. Do not supply any SSH key, and check "Perform manual registration on hosts and do not use SSH" and hit "Next".

