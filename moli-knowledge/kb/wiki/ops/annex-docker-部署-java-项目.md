---
title: docker 部署 java 项目.note（原文插图 annex）
slug: annex-docker-部署-java-项目
type: article
status: active
tags: [wujinsen, annex, 插图]
sources:
  - raw/wujinsen_markdown/架构/容器/Docker/docker 部署 java 项目.note.md
related: [容器与-docker]
created: 2026-07-05
updated: 2026-07-05
---

htp:/t.zoukankan.com/ming-blogs-p-1067076.html

Docker

Docker官⽅⽹址: 英⽂地址 Docker中⽂⽹址: 中⽂地址 Docker是基于Go语⾔实现的云开源项⽬，诞⽣于2013年初，最初发起者是dotClouw公司。Docker ⾃ 开源后受到⼴泛的关注和讨论，⽬前已有多个相关项⽬，逐断形成了围Docker的⽣态体系。dotCloud 公司后来也改名为Docker Ine。 Docker是⼀个开源的容器引擎，它有助于更快地交付应⽤。 Docker可将应⽤程序和基础设施层隔离， 并且能将基础设施当作程序⼀样进⾏管理。使⽤ Docker可更快地打包、测试以及部署应⽤程序，并可 以缩短从编写到部署运⾏代码的周期。

htps:/docs.docker.com/ htp:/ w.docker.org.cn/

# Docker优点

简化程序 Docker 让开发者可以打包他们的应⽤以及依赖包到⼀个可移植的容器中，然后发布到任何流⾏的 Linux 机器上，便可以实现虚拟化。Docker改变了虚拟化的⽅式，使开发者可以直接将⾃⼰的成果放 ⼊Docker中进⾏管理。⽅便快捷已经是 Docker的最⼤优势，过去需要⽤数天乃⾄数周的 任务，在 Docker容器的处理下，只需要数秒就能完成。 避免选择恐惧症 如果你有选择恐惧症，还是资深患者。Docker 帮你 打包你的纠结！⽐如 Docker 镜像；Docker 镜像 中包含了运⾏环境和配置，所以 Docker 可以简化部署多种应⽤实例⼯作。⽐如 Web 应⽤、后台应 ⽤、数据库应⽤、⼤数据应⽤⽐如 Hadop 集群、消息队列等等都可以打包成⼀个镜像部署。 节省开⽀ ⼀⽅⾯，云计算时代到来，使开发者不必为了追求效果⽽配置⾼额的硬件，Docker 改变了⾼性能必然 ⾼价格的思维定势。Docker 与云的结合，让云空间得到更充分的利⽤。不仅解决了硬件管理的问题， 也改变了虚拟化的⽅式。

# Docker架构

Docker daemon（ Docker守护进程） Docker daemon是⼀个运⾏在宿主机（ DOCKER-HOST）的后台进程。可通过 Docker客户端与之通 信。 Client（ Docker客户端） Docker客户端是 Docker的⽤户界⾯，它可以接受⽤户命令和配置标识，并与 Docker daemon通信。 图中， docker build等都是 Docker的相关命令。

Images（ Docker镜像） Docker镜像是⼀个只读模板，它包含创建 Docker容器的说明。它和系统安装光盘有点像，使⽤系统安 装光盘可以安装系统，同理，使⽤Docker镜像可以运⾏ Docker镜像中的程序。 Container（容器） 容器是镜像的可运⾏实例。镜像和容器的关系有点类似于⾯向对象中，类和对象的关系。可通过 Docker API或者 CLI命令来启停、移动、删除容器。 Registry Docker Registry是⼀个集中存储与分发镜像的服务。构建完 Docker镜像后，就可在当前宿主机上运 ⾏。但如果想要在其他机器上运⾏这个镜像，就需要⼿动复制。此时可借助 Docker Registry来避免镜 像的⼿动复制。 ⼀个 Docker Registry可包含多个 Docker仓库，每个仓库可包含多个镜像标签，每个标签对应⼀个 Docker镜像。这跟 Maven的仓库有点类似，如果把 Docker Registry⽐作 Maven仓库的话，那么 Docker仓库就可理解为某jar包的路径，⽽镜像标签则可理解为jar包的版本号。

# Docker与虚拟机⽐较

作为⼀种轻量级的虚拟化⽅式，Docker在运⾏应⽤上跟传统的虚拟机⽅式相⽐具有显著优势： Docker容器很快，启动和停⽌可以在秒级实现，这相⽐传统的虚拟机⽅式要快得多。 Docker容器对系统资源需求很少，⼀台主机上可以同时运⾏数千个Docker容器。 Docker通过类似Git的操作来⽅便⽤户获取、分发和更新应⽤镜像，指令简明，学习成本较低。 Docker通过Dockerfile配置⽂件来⽀持灵活的⾃动化创建和部署机制，提⾼⼯作效率。

# Docker安装

Docker 是⼀个开源的商业产品，有两个版本：社区版（Comunity Edition，缩写为 CE）和企业版 （Enterprise Edition，缩写为 E）。企业版包含了⼀些收费服务，个⼈开发者⼀般⽤不到 Docker 要求 CentOS 系统的内核版本在 3.10以上 ，查看本⻚⾯的前提条件来验证你的CentOS 版本是 否⽀持 Docker 。

- 1、通过 uname -r 命令查看你当前的内核版本

- 2、 使⽤ rot 权限登录 Centos。确保 yum 包更新到最新。

- 3、 卸载旧版本(如果安装过旧版本的话)

- 4、 安装需要的软件包， yum-util 提供yum-config-manager功能，另外两个是devicemaper驱动依 赖的


<table>
  <tr>
    <th>uname -r</th>
  </tr>
</table>


<table>
  <tr>
    <th>yum -y update</th>
  </tr>
</table>


<table>
  <tr>
    <th>yum remove docker docker-comon docker-selinux docker-engine</th>
  </tr>
</table>


<table>
  <tr>
    <th>yum instal -y yum-utils device-maper-persistent-data lvm2</th>
  </tr>
</table>


- 5、 设置yum源

- 6、 可以查看所有仓库中所有docker版本，并选择特定版本安装

- 7、 安装docker

- 8、 启动并加⼊开机启动

- 9、 验证安装是否成功(有client和service两部分表示docker安装启动都成功了)

- 10、 卸载docker


<table>
  <tr>
    <th>yum-config-manager-ad-repo htps:/download.docker.com/linux/centos/docker-ce.repo</th>
  </tr>
</table>


<table>
  <tr>
    <th>yum list docker-ce-showduplicates | sort -r</th>
  </tr>
</table>


<table>
  <tr>
    <th>sudo yum instal -y docker-ce #由于repo中默认只开启stable仓库，故这⾥安装的是最新稳定版</th>
  </tr>
</table>


18.03.1

<table>
  <tr>
    <th>sstectstart docker</th>
  </tr>
</table>


systemctl enable docker

<table>
  <tr>
    <th>docker version</th>
  </tr>
</table>


# 镜像相关命令

- 1、搜索镜像 # docker search java 可使⽤ docker search命令搜索存放在 (这是docker官⽅提供的存放所有docker镜像软件 的地⽅，类似maven的中央仓库)中的镜像。执⾏该命令后， Docker就会在Docker Hub中搜索含有 java这个关键词的镜像仓库。

官⽹htps:/hub.docker.com/search?q=java&type=image

- 2、下载镜像docker pul java:8 使⽤命令docker pul命令即可从 Docker Registry上下载镜像，执⾏该命令后，Docker会从 Docker Hub中的 java仓库下载最新版本的 Java镜像。如果要下载指定版本则在java后⾯加冒号指定版本

- 3、列出镜像使⽤ docker images命令即可列出已下载的镜像

- 4、删除镜像 使⽤ docker rmi java 命令即可删除指定镜像 配置阿⾥云镜像加速 详细参考: htps:/cr.console.aliyun.com/cn-hangzhou/mirors 容器相关命令 启动容器 docker run -d -p 81 80 nginx


Docker Hub

Docker Hub

在本例中，为 docker run添加了两个参数，含义如下：

- -d 后台运⾏

- -p 宿主机端⼝:容器端⼝ #开放容器端⼝到宿主机端⼝ 访问 htp:/Docker宿主机 IP 81/，将会看到nginx的主界⾯如下： 需要注意的是，使⽤ docker run命令创建容器时，会先检查本地是否存在指定镜像。如果本地不存在 该名称的镜像， Docker就会⾃动从 Docker Hub下载镜像并启动⼀个 Docker容器。


- 2. 列出容器⽤ docker ps命令即可列出运⾏中的容器

- 3. 查看容器的信息 docker inspect 3af513d208e 构建⾃⼰的docker镜像


- 1、将jar包上传linux服务器/usr/local/dockerap⽬录，在jar包所在⽬录创建名为Dockerfile的⽂件

- 2、在Dockerfile中添加以下内容


<table>
  <tr>
    <th>![image 1](assets/imageFile1.png)</th>
  </tr>
</table>


###指定java8环境镜像 FROM java:8

### 复制 jar (docker-springboot-0.0.1.jar) 到容器中并命名为 app-springboot.jar ADD docker-springboot-0.0.1.jar /app-springboot.jar

###声明启动端⼝号 EXPOSE 8080

###配置容器启动后执⾏的命令 ENTRYPOINT ["java","-jar","/app-springboot.jar"]

<table>
  <tr>
    <th>![image 2](assets/imageFile2.png)</th>
  </tr>
</table>


## 新增 WORKDIR 属性 例⼦

<table>
  <tr>
    <th>![image 3](assets/imageFile3.png)</th>
  </tr>
</table>


###指定java8环境镜像 FROM java:8

### 指定存储在容器内的⽬录 WORKDIR /usr/local/custom

### 复制 jar (docker-springboot-0.0.1.jar) 到容器中并命名为 app-springboot.jar ADD docker-springboot-0.0.1.jar app-springboot.jar

###声明启动端⼝号 EXPOSE 8080

###配置容器启动后执⾏的命令

ENTRYPOINT ["java","-jar","app-springboot.jar"]

<table>
  <tr>
    <th>![image 4](assets/imageFile4.png)</th>
  </tr>
</table>


app-springboot.jar 包在容器内⽬录为 /usr/local/custom/app-springboot.jar 注意：暴露(EXPOSE 8080)的端⼝号应该与 springboot 项⽬的端⼝号(server.port)保持⼀致，否 则⽆法访问。 使⽤docker build命令构建镜像，'.' 表示当前⽬录，指 jar包与Dockerfile 在同⼀⽬录下。 docker build -t docker-test.

# 格式： docker build -t 镜像名称 Dockerfile的相对位置 -t 参数是指定镜像的 tag 名

![image 5](assets/imageFile5.png)

#查看构建镜像是否成功

![image 6](assets/imageFile6.png)

# 运⾏镜像⽂件 docker run -p 8080 8080 docker-test 通过 docker ps -a 命令查询容器是否启动成功。 #查看⽇志 docker logs -f name 重启systemctl restart docker 关闭防⽕墙 systemctl stop firewald

相关阅读: 使⽤jQuery实现伪分⻚ 使⽤jQuery实现option的上移和下移 理解Flux架构 React ⼊⻔学习笔记1 ES6新特性6：模块Module ES6新特性5：类(Class)和继承(Extends) ES6新特性4：字符串的扩展 ES6新特性3：函数的扩展 ES6新特性2：变量的解构赋值 ES6新特性1：let和const
