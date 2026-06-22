htp:/ w.bubuko.com/infodetail-3646704.html

- 1.环境准备 Jdk 1.8 Scala 2.11.12：下载scala-2.11.12.msi并配置环境变量 Gradle 5.6.4： 下载Gradle-5.6.4并配置环境变量

- 2.配置⽂件修改

- -gradle.properties⽂件中

scalaVersion=2.10.6 修改为 scalaVersion=2.1.12

- -gradle/dependencies.gradle⽂件（如果安装的是2.11.8就不⽤改）：

def defaultScala21Version = ‘2.1.8‘ 修改为2.1.12

- -build.gradle⽂件配置国内的中央仓库：

- --buildscript节点下 repositories {


maven { url ‘htps:/mirors.huaweicloud.com/repository/maven/‘ } maven { url "https://maven.aliyun.com/repository/public" } mavenCentral() jcenter()

}

--allprojects节点下 repositories {

maven { url ‘htps:/mirors.huaweicloud.com/repository/maven/‘ } maven { url "https://maven.aliyun.com/repository/public" } mavenCentral()

}

- 3. 运⾏gradle，再运⾏gradle idea或者gradle eclipse


报错：Failed to aply plugin [id ‘org.scoverage‘]

Deprecated Gradle features were used in this build, making it incompatible with Gradle 6.0.

⼤概的意思是版本不兼容。 解决：build.gradle⽂件中：

claspath ‘org.scoverage:gradle-scoverage:2.1.0‘ 修改为 2.5.0

# 4. 导⼊idea/eclipse中（eclipse没试） 等待下载完包即可。

