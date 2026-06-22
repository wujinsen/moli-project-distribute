- 1.下载sonar-scaner
- 2. 配置环境变量: bash_profile
- 3.扫描⽅式:直接扫描项⽬⽬录(推荐)

从sonarqube⽹⻚新建project，⽣产token，扫描需要⽤到

- 4.sonar-scaner扫描命令:

- - 进⼊项⽬pom⽬录下
- -执⾏: sonar-scaner \
- -Dsonar.projectKey=store-server \
- -Dsonar.sources=. \
- -Dsonar.host.url=htp:/localhost:9 0 \
- -Dsonar.login=1d417439eac43bfd1b8279860f304d39cdfa1 \
- -Dsonar.java.binaries=./target/clases


- 5. maven⽅式扫描:


htps:/docs.sonarqube.org/latest/analysis/scan/sonarscaner/

htps:/docs.sonarqube.org/latest/analysis/scan/sonarscaner-for-maven/

- 1.需要修改seting.xml
- 2.项⽬需要引⼊


<table>
  <tr>
    <th><settings><br><br><pluginGroups><br><br><pluginGroup>org.sonarsource.scanner.maven</pluginGroup> </pluginGroups> <profiles><br><br><profile> <id>sonar</id> <activation><br><br><activeByDefault>true</activeByDefault> </activation> <properties><br><br><!-- Optional URL to server. Default value is http://localhost:9000 --> <sonar.host.u l><br><br>http://myserver:9000 </sonar.host.url><br><br></properties> </profile><br><br></profiles> </settings<br><br></th>
  </tr>
</table>


>

执⾏: mvn clean verify sonar:sonar -Dsonar.login=1d417439eac43bfd1b8279860f304d39cdfa1

- 6.SonarScaner for Jenkins


