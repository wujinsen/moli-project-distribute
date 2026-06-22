htp:/cache.baiducontent.com/c?m=9d78d513d98203ef03b1c1690c6c0 6f438136c8a8d536 8d5e35f93124c403739b0f707e0705a3d27c 16ae391eb4b763296b1420c6cf9bd3148aed8529 5f9f2647671cf7064e8049f19c5125b06cd4de9de4dbce6a62593df8d8084084ca25424d3b1 ca4e47549460a527a1b1983b084251fcbe613ca21d2069cb7d1df350f976 3589cb59a0b0d8 5718d3d508af3eba3f1be14a502470f710a6082767b171e8 4b50c1b03bc762c5723c

0de9e9da1eb3ed98b 6d8a98df473c07e6b3afa3157024b876ef2 e76a7345748d979e 01c 4fdb8ace10bc03d760ce7030386b838d6c0d10ca31cafba34&p=8b2a970c8a9e1af9 08e2947d0a498c&newp=8f6ade16d9c102f908e2947d0a5f96231610db2151d4d6126b82c825d7

31b01c3bfb423231304d4c27d6d0ba54d56ebf4317534072ba3da5c91d9fb4c57479cb71&us er=baidu&fm=sc&query=spring-bot-maven-plugin+failed%3A+Unable+to+find+main+clas&qid

=9c0367fc 038ae&p1=1

This is an extension to my previous question at SO. As per this post, main method is NOT required to generate adeployable war

here

I am trying to generate a deployable war for simple aplication of uploading files. The source code can be for this example application can be found here. Folowing the instructions from spring bot f ar to war conversion, I changed my pom.xml to reflect the folowing. (Just added tomcat dependency with scopeprovided). Reference: htp:/docs.spring.io/springbot/ /curent/reference/htmlsingle/#buildtol-plugins-maven-packaging

this

or j

docs

<?xml version="1.0" encoding="UTF-8"?> <project xmlns="htp:/maven.apache.org/POM/4

.

" xmlns:xsi="htp:/ w.w3.org/201/XMLSchemainstance"

- 0.0


rg/ P

xsi:schemaLocation="htp:/maven.apache.o OM/4.0.0 htp:/maven.apache.org/xsd/maven-

4.0.0.xsd"> <modelVersion>4.0.0</modelVersion> <groupId>org.springframework</groupId> <artifactId>gs-uploading-files</artifactI <version>0.1.0</version> <packaging>war</packaging> <parent>

d>

<groupId>org.springframework.bot</gr d>

oupI t</a

<artifactId>spring-bot-starterparen rtifactId>

<version>1.2.3.RELEASE</version> </parent>

<build> <plugins> <plugin>

<groupId>org.springframework. </groupId>

bot

plu

<artifactId>spring-bot-maven gin</artifactId>

</plugin>

</plugins> </build> <dependencies>

<dependency> <groupId>org.springframework.bot oupId>

</gr

<artifactId>spring-bot-starter-

w artifactId> </dependency> <dependency>

eb</

<groupId>org.springframework.bot oupId> artifactId>spring-bot-startert t/artifactId>

</gr omca

<scope>provided</scope>

</dependency> </dependencies> <properties>

<java.version>1.7</java.version> </properties> <repositories>

<repository> <id>spring-releases</id> <name>Spring Releases</name> <url>htps:/repo.spring.io/libs-

rele

ase</url>

</repository> /repositories>

<pluginRepositories>

<pluginRepository> <id>spring-releases</id> <name>Spring Releases</name> <url>htps:/repo.spring.io/libs-

rele

ase</url> </pluginRepository>

</pluginRepositories> </project> Then I changed Aplication.java as folows Reference: htp:/docs.spring.io/springbot/ /curent/reference/htmlsingle/#howtocreate-a-deployable-war-file @SpringBotAplication public clas Aplication extends SpringBotSe tInitializer{

docs

rvle

@Overide protected SpringAplicationBuilder

config SpringAplicationBuilder aplication) { return aplication.sources(Aplicatio as);

ure(

n.cl

} /*public static void mainString[] args)

{

SpringAplication.run(Aplication.cla args);

s,

} */

} I tried two scenarios. both fail with the fol ng eror (Unable to find main clas). No main method (NOTE I have comented out mai thod above)

lowi

n me

<table>
  <tr>
    <th>No aplication.Java file (comented out every g from this file - not shown here) Eror:<br><br>I F BUILD FAILURE [INFO] - -<br><br>-<br><br>Total time: 9.218s F Fi ished at: Thu Apr 231 46 24 PDT 2 F Final Memory: 2M/ 2M<br><br>[INFO] - -<br><br>[EROR] Failed to execute goal org.springfram<br><br>k.bot:spring-bot-mavenplugin:1.2.3.RELEASE:repackage (default) on project gs-uploading-files: Execution default of goal org.springframework.bot:spring-<br><br>in:1.2.3.RELEASE:repackage failed:Unable to find main clas -> [Hel<br><br>RR [EROR] To se the ful stack trace of the er , re-run Maven with the -e switch. [EROR] Re-run Maven using the -X switch to e<br><br>e ful debug loging. RR [EROR] For more information about the erors<br><br>posible solutions, please read the folowing articles: [EROR] [Help 1] htp:/cwiki.apache.org/conf ce/display/MAVEN /PluginExecutionException PS: when I have main method intact, the build sucesful<br><br>thin<br><br>-<br><br>015 -<br><br>ewo r<br><br>bot-mav en-plug<br><br>p 1]<br><br>rors<br><br>na bl<br><br>an d<br><br>luen<br><br>is</th>
    <th> </th>
  </tr>
  <tr>
    <td>spring maven tomcat spring-bot</td>
    <td> </td>
  </tr>
</table>


ad a coment

<table>
  <tr>
    <th>shareimprove this question</th>
    <th>edited Apr 23 '15 at 23 47</th>
    <th>asked Apr 23 '15 at 18 50 brain storm</th>
  </tr>
</table>


5,624956125

![image 1](<springboot maven编译错误解决.note_images/imageFile1.png>)

<table>
  <tr>
    <th> </th>
    <th>Any chance you stil have a reference to your main clas in the pom.xml? –ci_ Apr 23 '15 at</th>
  </tr>
  <tr>
    <td> </td>
    <td>19 39<br><br>@ci_: I am not sure what you mean exactly. I have updated with my entire pom.xml –brain st<br><br></td>
  </tr>
  <tr>
    <td> </td>
    <td>ormApr 23 '15 at 19  4<br><br>Other than the answers to your linked question, I couldn't find any evidence that you can omit the main clas, i.e. a clas with main method. Thespring-bot-maven-plugin sems to be l oking for it. Maybe if you didn't use the plugin, but not sure if that's worth it. –ci_ Apr 23 '15 at<br><br></td>
  </tr>
  <tr>
    <td> </td>
    <td>20 08<br><br>If you only want to run the aplication as a standard war file, you could either remove the Bot plugin entirely or reconfigure it so that the repackage goal isn't bound to Maven's</td>
  </tr>
  <tr>
    <td> </td>
    <td>lifecycle –Andy Wilkinson Apr 23 '15 at 21 27 for testing localy, I would prefer embeded Tomcat. but for deployment, I wil ned war. I am not sure how can I get both the benefits<br><br></td>
  </tr>
</table>


here –brain storm Apr 23 '15 at 21 31

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


- 1

- 2 Answers


activeoldestvotes

<table>
  <tr>
    <th>up vote8down voteacepted</th>
    <th>If you want to get both the benefits - i.e. standalone executable war with embeded<br><br>omat and the normal war deployable in external Tomcat - you ned to have a clas with main method. So, Enable the main() method in your Aplication.java. Configure spring-boot-maven-plugin to specify the clas with the main clas (Spring should find it anyway if you have one, but god to be explicit I gues):<br><br>plugin> <groupId>org.springframework.bot</groupId><br><br><artifactId>spring-bot-mavenplugin</artifactId> <version>${spring-botversion}</version> <configuration> <mainClas>the.package.of.Aplication</mainClas s> </configuration> <executions><execution> <goals><br><br><goal>repackage</goal> goals> ee > </executions> </plugin> Remove spring-bot-starter-tomcat dependency in your pom with provided scope. Spring bot magicaly knows how to enable/disable embeded Tomcat. With this you should be able to launch your Spring ap from IDE by just runing the Aplication.java clas as a normal Java ap, build a war file that is both standalone executable and deployable in external Tomcat as usual to. I am curently building REST APIs with Spring Bot 1.0.1.RELEASE with this kind of setup and it works</th>
  </tr>
  <tr>
    <td> </td>
    <td>great in al thre modes.</td>
  </tr>
</table>


show 2 more coments

<table>
  <tr>
    <th>shareimprove this answer</th>
    <th>answered Apr 23 '15 at 23 48 Naymesh Mistry</th>
  </tr>
</table>


2319

![image 2](<springboot maven编译错误解决.note_images/imageFile2.png>)

<table>
  <tr>
    <th> </th>
    <th>even without configuringspring-bot-mavenplugin (as you sugested), if I have main method, It works fine with both embeded and<br><br>eloy ent wa. If I want to enable only deployment war mode, it sems I ned to stil havemain. Otherwise I canot get it working. –<br><br></th>
  </tr>
  <tr>
    <td> </td>
    <td>brain stormApr 23 '15 at 23 53<br><br>Thats right, if you donʼt specify a main clas the plugin wil search for a clas with a public static void main(String[] args) method. I personaly prefer to specify the clas explicitly though, for readability. But yeah, it is not required to be specified explicitly. If you want to enable deployment only in normal war mode, you don't ned to usespring-bootmaven-plugin at al in your pom. –<br><br>as the document says<br><br>Naymesh</td>
  </tr>
  <tr>
    <td> </td>
    <td>MistryApr 24 '15 at 0  0<br><br>Thanks, In production, I doubt any body uses embeded Tomcat server. so, I would preferwar to be deployed. is removingspring-boot-maven-plugin, do I only get rid ofmain method search or something important is lost to? –brain<br><br></td>
  </tr>
  <tr>
    <td> </td>
    <td>stormApr 24 '15 at 0 06<br><br>True. Embeded Tomcat is more for easy distribution (e.g. your testers won't ned any Tomcat instalation first) and deployment while under development and testing. In production<br><br>he usual war should be used. Removal of thespring-boot-maven-plugin and the main method should only take away the standalone executable/embeded Tomcat feature from war, nothing else (just tested this on my aplication and worked fine :) – Naymes<br><br></td>
  </tr>
  <tr>
    <td> </td>
    <td>h MistryApr 24 '15 at 0 29 Thanks for the tip –brain storm Apr 24 '15 at<br><br></td>
  </tr>
</table>


4 45

<table>
  <tr>
    <th> </th>
    <th> </th>
  </tr>
</table>


1

<table>
  <tr>
    <th>up vote2down vote</th>
    <th>Note that one can get a somewhat similar mesage along the lines of Failed to execute goal org.springframework.bot:spring-bot-mavenplugin:1.3.2.RELEASE:repackage (default) on project Xyz: Execution default of goal org.springframework.bot:spring-bot-mavenplugin:1.3.2.RELEASE:repackage failed: Unable to find a single main clas from the folowing candidates [com.a.Aplication, com.a.fake.Aplication, com.a.main.Main1, com.a.runer.DataImportRuner, com.a.temp.dependencyinjection.MainAp, com.a.fi alap.facade.impl.V3DataImports, com.a.finalap.service.impl.Ap2] (Which I saw in my aplication when atempting to run a Maven Install) In my case, I had multiple main methods and Spring could not automaticaly chose the one I wanted. The solution is the same outlined earlier by Naymesh Mistry (explicitly specify<br><br></th>
  </tr>
</table>


your.main.clas.with.package.prefix in pom.xml)

<table>
  <tr>
    <th> </th>
    <th>answered Apr 14 at 1 34</th>
  </tr>
</table>


shareimprove this answer Tothles Ser

![image 3](<springboot maven编译错误解决.note_images/imageFile3.png>)

