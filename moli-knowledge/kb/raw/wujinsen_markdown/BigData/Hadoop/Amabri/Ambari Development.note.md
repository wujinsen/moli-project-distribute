需要配好JDK，Python(版本.26及以上)

安装好以下软件: rpmbuild g++ (gcc-c++ package)

NodeJS 执⾏:sudo yum --enablerepo=updates-testing install nodejs npm Brunch 1.7.20 npm install -g brunch@1.7.20

创建Ambari:

<table>
  <tr>
    <th>mvn -Dfile=jms-1.1.pom -DgroupId=javax.jms DartifactId=jms -Dversion=1.1 -Dpackaging=jar mvn -Dfile=jmxtools-1.2.1.pom -DgroupId=com.sun.jdmk<br><br>-DartifactId=jmxtools -Dversion=1.2.1-Dp mvn -Dfile=jmxri-1.2.1.pom -DgroupId=com.sun.jmx DartifactId=jmxri -Dversion=1.2.1 -Dpackagin<br><br>install:install-file install:install-file<br><br>ackaging=jar install:install-file<br><br>g=jar</th>
  </tr>
</table>


设置ambari版本号,4位数,⽐如 -DnewVersion=2.2.2.2

mvn versions:set -DnewVersion=${AMBARI_VERSION}

pushd ambari-metrics

mvn versions:set -DnewVersion=${AMBARI_VERSION}

popd

mvn -B clean install package rpm:rpm -DskipTests -Dpython.ver="python >= 2.6" -Preplaceurl

创建Ambari Metrics

<table>
  <tr>
    <th>yum install ambari-server/target/rpm/ambari-server/RPMS/noarch/ambariserver-*.noarch.rpm<br><br></th>
  </tr>
</table>


初始化Ambari Server

<table>
  <tr>
    <th>ambari-server setup</th>
  </tr>
</table>


启动

<table>
  <tr>
    <th>ambari-server start</th>
  </tr>
</table>


查看Ambari Server log:

<table>
  <tr>
    <th>htp:/{ambari-server-hostname}:8080</th>
  </tr>
</table>


⽤户名密码默认admin admin

安装Ambari Agent RPM:

<table>
  <tr>
    <th>yum instal ambari-agent/target/rpm/ambari-agent/RPMS/x86_64/ambari-agent-*.rpm</th>
  </tr>
</table>


